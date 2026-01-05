# v0.10.0-alpha Implementation Plan

**Goal**: Seat & Ticket Rewards System (Points economy, seat selection, ticket tiers)

**Status**: Not Started

**Timeline**: April 21 - May 5 (14 days, school pace with async agent support)

---

## Overview

v0.10.0 introduces a user-facing rewards economy: users earn points for completed trips (longer trips = more points) and can spend points to upgrade ticket level (economy → economy plus → business → first) or to select preferred seats (window/aisle/row). Random seat assignment remains free; seat/tier purchases are optional cosmetic/UX rewards that increase retention.

This is a medium-complexity feature touching backend storage, flight-completion hooks, UI for store/checkout and seat selection, and analytics. Split into **5 focused PRs**.

---

## PR Breakdown

### PR #1: Points Economy & Backend 🔁

**Branch**: `feature/points-economy`

**Size**: Medium (~200 lines)

**Priority**: High (Foundation)

**Dependencies**: `v0.9.0` logbook (for awarding points on completed flights), `v0.5.0` flight completion events

**Description**: Implement points accounting, earning rules, persistence, and migrations.

#### Tasks (PR #1)

- [ ] Create `services/pointsService.ts`:
  - `awardPoints(userId: string, amount: number, reason?: string): Promise<void>`
  - `getPointsBalance(userId: string): Promise<number>`
  - `spendPoints(userId: string, amount: number): Promise<boolean>`
  - `getTransactionHistory(userId: string): Promise<PointTransaction[]>`
- [ ] Define earning rules:
  - Points = f(distance, duration) (e.g., basePoints = floor(distance / 10) + floor(durationMinutes / 30))
  - Bonus multipliers for long flights (>500 miles) or streaks
- [ ] Persist points in SQLite via `logbookService` or a new `points` table
- [ ] Add migrations for existing data
- [ ] Add unit tests for calculations and persistence

#### Acceptance Criteria (PR #1)

- Points awarded and persisted after completed flights
- Balance queries return correct values
- Transactions are auditable
- TypeScript strict mode passes

### PR #2: Seat Selection & Ticket Store UI 🪑

**Branch**: `feature/seat-ticket-ui`

**Size**: Medium (~250 lines)

**Priority**: High (User-facing)

**Dependencies**: PR #1

**Description**: Build UI for viewing ticket tiers and selecting seats. Includes checkout flow to spend points.

#### Tasks (PR #2)

- [ ] Create `screens/TicketStoreScreen.tsx`:
  - Display ticket tiers, costs (points), and benefits (cosmetic)
  - Purchase flow with confirmation modal
- [ ] Create `components/seat-selector/SeatSelector.tsx`:
  - Simple grid view of plane seats for small plane model
  - Allow selecting window/aisle/middle and row preferences
  - Show seat availability (if simulated)
  - Support 'random seat' free option
- [ ] Integrate `pointsService` to spend points
- [ ] Persist ticket/seat selection with flight state (so cockpit shows chosen seat/tier)
- [ ] Add tests for UI flows and edge cases

#### Acceptance Criteria (PR #2)

- Users can view and purchase ticket tiers with points
- Seat selector UI works and persists choice for the flight
- Checkout deducts points and shows a transaction receipt

### PR #3: Awarding Points on Flight Completion ⚡

**Branch**: `feature/award-points-on-arrival`

**Size**: Small-Medium (~150 lines)

**Priority**: High (Integration)

**Dependencies**: PR #1, v0.5.0 arrival service

**Description**: Hook into arrival handling to compute and award points automatically.

#### Tasks (PR #3)

- [ ] On arrival, call `pointsService.awardPoints()` with calculated amount and reason `flight_complete`
- [ ] Add optional UI toast/confirmation showing points earned
- [ ] Track analytics event for points awarded for v0.9.0 analytics
- [ ] Add tests for correct point calculation per flight

#### Acceptance Criteria (PR #3)

- Points awarded automatically at flight completion
- Users see updated balances in-store immediately

### PR #4: Account UI & History 📜

**Branch**: `feature/points-history-ui`

**Size**: Small (~150 lines)

**Priority**: Medium

**Dependencies**: PR #1, PR #2

**Description**: Add account section showing points balance and transaction history, and optional monthly leaderboards.

#### Tasks (PR #4)

- [ ] Add `components/account/PointsSummary.tsx`
- [ ] Add `screens/PointsHistoryScreen.tsx` showing transactions
- [ ] Add small leaderboard prototype based on local data
- [ ] Tests for data rendering

#### Acceptance Criteria (PR #4)

- Users can view current points and recent transactions
- UI is responsive and respects themes

### PR #5: Analytics, Testing & Polish 📈

**Branch**: `feature/points-analytics`

**Size**: Small (~150 lines)

**Priority**: Medium

**Dependencies**: PR #1-4

**Description**: Add analytics hooks, ensure awarding rules are fair, finalize tests and edge-case handling.

#### Tasks (PR #5)

- [ ] Add analytics events for points earned/spent
- [ ] Add AB test toggles for default point formula (if needed)
- [ ] Comprehensive tests for fraud/edge-cases
- [ ] Performance checks and DB indexing

#### Acceptance Criteria (PR #5)

- Analytics capture points flow
- No major performance regressions
- Tests and type checks pass

---

## Timeline Estimate

- **PR #1** (Points backend): 2 days
- **PR #2** (Ticket store + seat selector): 3 days
- **PR #3** (Award on arrival): 1 day
- **PR #4** (Account UI/history): 1.5 days
- **PR #5** (Analytics & polish): 1.5 days

**Total**: ~9-10 days (can be compressed with async agents to ~6-7 days)

**Recommended**: Start after v0.9.0 completes and before final v1.0 polish — slot as v0.10.0 (Apr 21-May 5)

---

## Integration Points

-- ⬅️ **v0.9.0**: Logbook & completed flights (awarding points)
-- ⬅️ **v0.5.0**: Arrival detection triggers points award
-- 📍 **v0.10.0**: Points economy, seat selector, store
-- ➡️ **v0.11.0 / v1.0**: Polishing and release (reflect new UI/flows)

---

## Success Criteria

✅ Points can be earned and spent reliably

✅ Seat/tier purchases persist and reflect in cockpit UI

✅ Store UX is clear and non-blocking for core flows

✅ No database or performance regressions

✅ Tests cover core math and persistence

---

### Last Updated

January 4, 2026

### Author

Development Team
