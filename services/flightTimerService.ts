/**
 * Flight timer service
 * 
 * Core engine for real-time flight simulation. Manages the countdown timer,
 * calculates flight progress, and emits events for UI updates.
 * 
 * Features:
 * - 100ms tick interval for smooth 60fps UI updates
 * - Progress calculation with position interpolation
 * - Flight phase detection and transitions
 * - App backgrounding/foregrounding handling
 * - Crash recovery support
 * 
 * @module services/flightTimerService
 */

import type { FlightBooking, FlightPhase, FlightProgress } from '@/types/flight';
import { calculateIntermediateBearing, interpolatePosition } from '@/utils/flightInterpolation';
import {
    calculateAltitude,
    calculateSpeed,
    getFlightPhase,
    getMaxAltitude
} from '@/utils/flightMetrics';
import { AppState, AppStateStatus } from 'react-native';

/**
 * Flight timer state
 */
type TimerState = 'idle' | 'active' | 'paused' | 'completed';

/**
 * Event listener types
 */
type TickListener = (progress: FlightProgress) => void;
type PhaseChangeListener = (phase: FlightPhase, progress: FlightProgress) => void;
type ArrivalListener = () => void;
type DivertListener = () => void;

/**
 * Flight timer service class
 * 
 * Singleton service that manages active flight simulation.
 * Use shared instance via `flightTimerService`.
 * 
 * @example
 * ```typescript
 * // Initialize flight
 * flightTimerService.startFlight(booking);
 * 
 * // Listen to updates
 * flightTimerService.onTick((progress) => {
 *   console.log(`${progress.progressPercent}% complete`);
 * });
 * 
 * // Cleanup when done
 * flightTimerService.cleanup();
 * ```
 */
class FlightTimerService {
  private state: TimerState = 'idle';
  private booking: FlightBooking | null = null;
  private startTime: number = 0;
  private elapsedSeconds: number = 0;
  private intervalId: ReturnType<typeof setInterval> | null = null;
  private lastPhase: FlightPhase | null = null;
  private appStateSubscription: any = null;
  private pausedAt: number | null = null;

  // Event listeners
  private tickListeners: TickListener[] = [];
  private phaseChangeListeners: PhaseChangeListener[] = [];
  private arrivalListeners: ArrivalListener[] = [];
  private divertListeners: DivertListener[] = [];

  /**
   * Start flight simulation
   * 
   * @param booking - Flight booking to simulate
   * @param resumeFromSeconds - Resume from specific elapsed time (for crash recovery)
   */
  startFlight(booking: FlightBooking, resumeFromSeconds: number = 0): void {
    if (this.state === 'active') {
      console.warn('[FlightTimer] Flight already active');
      return;
    }

    this.booking = booking;
    this.startTime = Date.now() - (resumeFromSeconds * 1000);
    this.elapsedSeconds = resumeFromSeconds;
    this.state = 'active';
    this.lastPhase = null;

    // Start timer with 100ms interval (10 ticks/second for smooth UI)
    this.intervalId = setInterval(() => {
      this.tick();
    }, 100);

    // Listen for app state changes (backgrounding/foregrounding)
    this.appStateSubscription = AppState.addEventListener('change', this.handleAppStateChange);

    console.log('[FlightTimer] Flight started', {
      flight: booking.flightNumber,
      duration: booking.durationSeconds,
      resumeFrom: resumeFromSeconds,
    });
  }

  /**
   * Pause flight (used when backgrounding or diverted)
   */
  pauseFlight(): void {
    if (this.state !== 'active') {
      return;
    }

    this.state = 'paused';
    this.pausedAt = Date.now();
    
    if (this.intervalId) {
      clearInterval(this.intervalId);
      this.intervalId = null;
    }

    console.log('[FlightTimer] Flight paused', {
      elapsedSeconds: this.elapsedSeconds,
    });
  }

  /**
   * Resume paused flight
   */
  resumeFlight(): void {
    if (this.state !== 'paused' || !this.booking) {
      return;
    }

    // Adjust start time to account for pause duration
    if (this.pausedAt) {
      const pauseDuration = Date.now() - this.pausedAt;
      this.startTime += pauseDuration;
    }

    this.state = 'active';
    this.pausedAt = null;

    // Restart timer
    this.intervalId = setInterval(() => {
      this.tick();
    }, 100);

    console.log('[FlightTimer] Flight resumed');
  }

  /**
   * Update flight booking (used when diverting)
   * 
   * @param newBooking - New flight booking with updated destination
   * @param newElapsedSeconds - New elapsed time accounting for divert
   */
  updateFlight(newBooking: FlightBooking, newElapsedSeconds: number): void {
    this.booking = newBooking;
    this.startTime = Date.now() - (newElapsedSeconds * 1000);
    this.elapsedSeconds = newElapsedSeconds;
    this.lastPhase = null; // Reset phase to trigger new phase change events

    console.log('[FlightTimer] Flight updated (diverted)', {
      newFlight: newBooking.flightNumber,
      newDestination: newBooking.destination.icao,
    });
  }

  /**
   * Stop flight simulation and cleanup
   */
  cleanup(): void {
    if (this.intervalId) {
      clearInterval(this.intervalId);
      this.intervalId = null;
    }

    if (this.appStateSubscription) {
      this.appStateSubscription.remove();
      this.appStateSubscription = null;
    }

    this.state = 'idle';
    this.booking = null;
    this.startTime = 0;
    this.elapsedSeconds = 0;
    this.lastPhase = null;
    this.pausedAt = null;

    // Clear event listeners
    this.tickListeners = [];
    this.phaseChangeListeners = [];
    this.arrivalListeners = [];
    this.divertListeners = [];

    console.log('[FlightTimer] Cleanup complete');
  }

  /**
   * Timer tick - calculate progress and emit events
   */
  private tick(): void {
    if (!this.booking || this.state !== 'active') {
      return;
    }

    // Calculate elapsed time
    const now = Date.now();
    this.elapsedSeconds = (now - this.startTime) / 1000;

    // Check if flight is complete
    if (this.elapsedSeconds >= this.booking.durationSeconds) {
      this.handleArrival();
      return;
    }

    // Calculate progress
    const progress = this.calculateProgress();

    // Emit tick event
    this.tickListeners.forEach(listener => {
      try {
        listener(progress);
      } catch (error) {
        console.error('[FlightTimer] Error in tick listener:', error);
      }
    });

    // Check for phase changes
    if (this.lastPhase !== progress.currentPhase) {
      this.lastPhase = progress.currentPhase;
      this.phaseChangeListeners.forEach(listener => {
        try {
          listener(progress.currentPhase, progress);
        } catch (error) {
          console.error('[FlightTimer] Error in phase change listener:', error);
        }
      });
    }
  }

  /**
   * Calculate current flight progress
   */
  private calculateProgress(): FlightProgress {
    if (!this.booking) {
      throw new Error('No active flight booking');
    }

    const { durationSeconds, distanceKm, origin, destination } = this.booking;

    // Calculate progress percentage
    const progressPercent = Math.min(
      (this.elapsedSeconds / durationSeconds) * 100,
      100
    );

    // Calculate remaining time
    const remainingSeconds = Math.max(durationSeconds - this.elapsedSeconds, 0);

    // Calculate progress ratio (0-1)
    const progressRatio = Math.min(this.elapsedSeconds / durationSeconds, 1);

    // Calculate distances
    const distanceFlown = distanceKm * progressRatio;
    const distanceRemaining = distanceKm * (1 - progressRatio);

    // Determine flight phase
    const currentPhase = getFlightPhase(progressPercent);

    // Calculate position along route
    const currentPosition = interpolatePosition(origin, destination, progressRatio);

    // Calculate heading
    const heading = calculateIntermediateBearing(origin, destination, progressRatio);

    // Calculate altitude
    const maxAltitude = getMaxAltitude(distanceKm);
    const currentAltitude = calculateAltitude(progressPercent, currentPhase, maxAltitude);

    // Calculate speed
    const currentSpeed = calculateSpeed(progressPercent, currentPhase);

    return {
      elapsedSeconds: Math.round(this.elapsedSeconds),
      remainingSeconds: Math.round(remainingSeconds),
      distanceFlown: Math.round(distanceFlown * 10) / 10, // 1 decimal place
      distanceRemaining: Math.round(distanceRemaining * 10) / 10,
      progressPercent: Math.round(progressPercent * 10) / 10,
      currentPhase,
      currentPosition,
      currentAltitude: Math.round(currentAltitude),
      currentSpeed: Math.round(currentSpeed),
      heading: Math.round(heading),
    };
  }

  /**
   * Handle flight arrival (completion)
   */
  private handleArrival(): void {
    this.state = 'completed';
    
    if (this.intervalId) {
      clearInterval(this.intervalId);
      this.intervalId = null;
    }

    console.log('[FlightTimer] Flight arrived');

    // Emit arrival event
    this.arrivalListeners.forEach(listener => {
      try {
        listener();
      } catch (error) {
        console.error('[FlightTimer] Error in arrival listener:', error);
      }
    });
  }

  /**
   * Handle app state changes (backgrounding/foregrounding)
   */
  private handleAppStateChange = (nextAppState: AppStateStatus): void => {
    if (nextAppState === 'background' || nextAppState === 'inactive') {
      // App moving to background - pause the timer
      if (this.state === 'active') {
        this.pauseFlight();
        console.log('[FlightTimer] App backgrounded, timer paused');
      }
    } else if (nextAppState === 'active') {
      // App returning to foreground - resume the timer
      if (this.state === 'paused') {
        this.resumeFlight();
        console.log('[FlightTimer] App foregrounded, timer resumed');
      }
    }
  };

  // Event listener registration methods

  /**
   * Register tick event listener
   * 
   * @param listener - Function called on each timer tick (~10 times/second)
   * @returns Unsubscribe function
   */
  onTick(listener: TickListener): () => void {
    this.tickListeners.push(listener);
    return () => {
      this.tickListeners = this.tickListeners.filter(l => l !== listener);
    };
  }

  /**
   * Register phase change event listener
   * 
   * @param listener - Function called when flight phase changes
   * @returns Unsubscribe function
   */
  onPhaseChange(listener: PhaseChangeListener): () => void {
    this.phaseChangeListeners.push(listener);
    return () => {
      this.phaseChangeListeners = this.phaseChangeListeners.filter(l => l !== listener);
    };
  }

  /**
   * Register arrival event listener
   * 
   * @param listener - Function called when flight arrives (0:00)
   * @returns Unsubscribe function
   */
  onArrival(listener: ArrivalListener): () => void {
    this.arrivalListeners.push(listener);
    return () => {
      this.arrivalListeners = this.arrivalListeners.filter(l => l !== listener);
    };
  }

  /**
   * Get current state
   */
  getState(): TimerState {
    return this.state;
  }

  /**
   * Get current elapsed seconds
   */
  getElapsedSeconds(): number {
    return this.elapsedSeconds;
  }

  /**
   * Get current booking
   */
  getBooking(): FlightBooking | null {
    return this.booking;
  }
}

/**
 * Shared flight timer service instance
 */
export const flightTimerService = new FlightTimerService();
