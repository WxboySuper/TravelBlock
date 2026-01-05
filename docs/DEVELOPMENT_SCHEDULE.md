# TravelBlock Development Schedule

**Target v1.0 Release**: June 1st, 2026

**Last Updated**: January 4, 2026

---

## Schedule Overview

This schedule accounts for:

- **Winter Break** (Jan 4-19): High productivity window (1-2 PRs/day)
- **Spring Semester** (Jan 19 - May 11): Limited time (1-2 hrs/day avg, ~1 PR per 1-2 days)
- **Spring Break** (Mar 14-22): High productivity window
- **Finals Week** (May 11-15): No deadlines, minimal expected progress
- **Summer Classes**: Start after finals, lower capacity
- **Other Projects**: 4-5 concurrent projects competing for time

---

## Detailed Timeline

### Phase 1: Winter Break Sprint 🏃‍♂️ (Jan 4-19)

**High Productivity Window** — 1-2 PRs/day possible

#### v0.2.0-alpha: Airport Data Layer

- **PRs**: 4 total (Distance utilities, Airport service, UI components, Integration)
- **Target Completion**: January 9th
- **Status**: PR #1 (Distance) in progress, targeting completion by Jan 5-6
- **Effort**: ~3-4 days at current pace
- **Async Agent**: Not needed; you're making excellent progress

#### v0.3.0-alpha: Home Base

- **PRs**: 4 total (AsyncStorage service, GPS service, First run UI, Home airport management)
- **Dates**: January 9-13
- **Target Completion**: January 13th
- **Effort**: ~4-5 days with possible async agent help for PR #1 and #2
- **Async Agent**: Recommended for PRs #1-2 to accelerate if you want to move faster

#### v0.4.0-alpha: Time Slider (Core Mechanic)

- **PRs**: 5 total (Slider component, Snap logic, Radius filtering, Destination list display, Integration)
- **Dates**: January 13-18
- **Target Completion**: January 18th ⚠️ (before school starts Jan 19)
- **Effort**: ~5-6 days
- **Async Agent**: **Highly Recommended** for PRs #1-3 (component implementation)
  - You can have agents working on implementation while you handle reviews/testing
  - Allows you to start school with v0.4.0 complete

---

### Phase 2: Early Semester (Jan 19 - Feb 16) 📚

**School Pace** — 1-2 hrs/day, ~1 PR every 1-2 days

#### v0.5.0-alpha: Flight Engine (Real-Time) ⚠️

- **PRs**: 5-6 total (Cockpit view, Timer logic, Distance/progress tracking, No-pause rules, Arrival handling, Optimization)
- **Dates**: January 19 - February 2
- **Target Completion**: February 2nd
- **Effort**: ~14-15 days of work (distributed over ~2 weeks with school)
- **Complexity**: HIGH (real-time simulation logic)
- **Async Agent**: **Strongly Recommended**
  - This is a complex feature with multiple interdependencies
  - Agent can handle PR #2-4 (logic implementation) while you handle architecture/testing
  - PR #1 (Cockpit UI) could be done by agent with your specs
  - **Suggested Setup**: Assign PRs early (Jan 15-18), review and integrate during school

#### v0.6.0-alpha: Map Visualization

- **PRs**: 4 total (Map setup, Polyline rendering, Plane marker, View-type switching)
- **Dates**: February 2-16
- **Target Completion**: February 16th
- **Effort**: ~14 days
- **Complexity**: MEDIUM (react-native-maps integration)
- **Async Agent**: Recommended for PRs #1-3 (map implementation details)

---

### Phase 3: Mid Semester (Feb 16 - Mar 14) 📚

**School Pace** — 1-2 hrs/day

#### v0.7.0-alpha: Audio Immersion

- **PRs**: 3 total (Audio playback service, Sound effects, Settings/configuration)
- **Dates**: February 16-28
- **Target Completion**: February 28th
- **Effort**: ~12-14 days
- **Complexity**: LOW-MEDIUM (straightforward audio implementation)
- **Async Agent**: Recommended for PR #1 (audio service boilerplate)

#### v0.8.0-alpha: Pivot Point (Post-Flight)

- **PRs**: 4 total (Landed screen UI, Continue journey flow, Return home flow, State transitions)
- **Dates**: February 28 - March 14
- **Target Completion**: March 14th (Spring Break starts)
- **Effort**: ~14 days
- **Complexity**: MEDIUM (navigation and state management)
- **Async Agent**: Recommended for PRs #2-3 (flow implementation)

---

### Phase 4: Spring Break Boost 🏖️ (Mar 14-22)

**High Productivity Window** — 1-2 PRs/day

#### v0.9.0-alpha: Logbook & Analytics (Partial)

- **PRs**: 3-4 completed (Database setup, Session logging, Logbook screen)
- **Dates**: March 14-22 (8 days)
- **Expected**: 3-4 PRs done during break
- **Async Agent**: Not needed; you'll have time to work on this

---

### Phase 5: Late Semester (Mar 23 - May 11) 📚

**School Pace** — 1-2 hrs/day, some days off for exams

#### v0.9.0-alpha: Logbook & Analytics (Completion)

- **PRs**: 1-2 remaining (Charts/analytics, Performance optimization)
- **Dates**: March 23 - April 1
- **Target Completion**: April 1st
- **Async Agent**: Optional for performance optimization PR

#### v0.10.0-alpha: Final Polish

- **PRs**: 4-5 total (UI refinement, Dark mode cockpit, Cross-device testing, Accessibility, App icon/metadata)
- **Dates**: April 1-20
- **Target Completion**: April 20th
- **Effort**: ~19 days
- **Complexity**: MEDIUM (testing and polish)
- **Async Agent**: Recommended for PRs #1-2 (UI refinement) to free up your time for testing

#### v1.0: Release Build & Submission

- **PRs**: 3 total (EAS build setup, QA & testing, Store submission)
- **Dates**: April 20 - May 10
- **Target Completion**: May 10th ⚠️ (before finals May 11-15)
- **Effort**: ~20 days (mostly testing/validation)
- **Complexity**: LOW (build/deployment tasks)
- **Async Agent**: Not needed; straightforward tasks

**Finals Week (May 11-15)**: No deadlines — take breaks from studying as needed; any work on TravelBlock is bonus

---

### Phase 6: Post-Release (May 16 - June 1)

**Variable** — Depends on summer class schedule

- Post-release monitoring and quick-fix handling
- Summer classes may start; capacity will be reduced
- No hard deadline pressure; v1.0 ships by June 1st at latest

---

## Recommended Milestones

| Version | Target Date | Days Available | PRs | Async Agent? | Notes |
| --- | --- | --- | --- | --- | --- |
| v0.2.0 | Jan 9 | 5 days | 4 | No | You're already crushing this |
| v0.3.0 | Jan 13 | 4 days | 4 | Optional | Use agents if you want to push to v0.4 faster |
| v0.4.0 | Jan 18 | 5 days | 5 | Recommended | Must complete before school starts |
| v0.5.0 | Feb 2 | 14 days | 5-6 | Strongly Recommended | Complex real-time logic; agents handle implementation |
| v0.6.0 | Feb 16 | 14 days | 4 | Recommended | Standard school pace |
| v0.7.0 | Feb 28 | 12 days | 3 | Recommended | Simpler feature; quick turnaround |
| v0.8.0 | Mar 14 | 14 days | 4 | Recommended | Finish before spring break boost |
| v0.9.0 | Apr 1 | 18 days | 4-5 | Optional | 8 days break + 10 days school; split work |
| v0.10.0 | Apr 20 | 20 days | 4-5 | Recommended | Polish phase; agents handle UI refinement |
| v0.11.0 | May 5 | 14 days | 5 | Recommended | Seat & Ticket Rewards System (points, seat selection) |
| v1.0 | May 25 | 20 days | 3 | No | Build/submission/QA; straightforward work |

---

## Key Strategy Notes

### Winter Break (Jan 4-19) — CRITICAL WINDOW 🎯

You have 15 days of high productivity. Goal: **Complete v0.2.0, v0.3.0, and v0.4.0 by January 18th.**

- This sets you up perfectly before school starts
- v0.4.0 is the last version before school kicks in fully
- Current rate (1-2 PRs/day) is perfect; leverage it aggressively

**Action**: Use async agents for v0.4.0 PRs #1-3 starting Jan 13-15 so you can focus on architecture/review instead of implementation details.

### School Year (Jan 19 - May 11) — AGENT-HEAVY 🤖

You have limited time but steady schedule. Strategy:

- **Start async agents early** (Jan 15-18) for v0.4.0 work to finish before school
- **Use agents for v0.5.0** (Jan 19-Feb 2) — complex logic; agent does implementation, you do architecture/testing
- **Use agents selectively** for v0.6-v0.8 — simpler features, you can handle more manually
- **Use agents for v0.10.0** (Apr 1-20) — polish phase where agent handles UI refinement
- **School breaks** (Spring Break Mar 14-22) — you handle complex work; agents rest

### Spring Break Boost (Mar 14-22) — PRODUCTIVITY SURGE 📈

You have 8 free days while still working during spring break period. Knock out v0.9.0 PRs 3-4 here and start v0.10.0. This gives you a big buffer before finals.

### Pre-Finals Push (Apr 1-20) — BALANCED 📋

- v0.10.0 should be mostly done by Apr 20
- v1.0 build/submission from Apr 20-May 10
- This gives you 1 day buffer before finals start
- Finals week (May 11-15): take breaks studying, no deadlines

### Post-Release (May 16+) — MAINTENANCE MODE 🔧

- v1.0 ships by May 10-15
- Summer classes start soon; reduced capacity
- Focus on post-release monitoring, quick fixes
- By June 1st, v1.0 should be stable and submitted to stores

---

## Buffer & Risk Mitigation

**Built-in Buffers:**

- v0.2.0: 3-day buffer (target Jan 9, deadline ~Jan 12 before v0.3 starts)
- v0.3.0: Part of winter break; good buffer
- v0.4.0: Must complete by Jan 18 (1 day buffer before school starts Jan 19)
- Spring Break: 8 free days used to boost v0.9.0 progress
- Post-Finals: ~20 days for v1.0 build/submission/polish

**Risk Scenarios:**

| Scenario | Impact | Mitigation |
| --- | --- | --- |
| School is busier than expected | Delays v0.5-v0.8 | Use async agents more aggressively for implementation work |
| Bug discovered in v0.4.0 | Delays v0.5 start | Keep 3-4 days buffer between versions for hotfixes |
| Other projects need attention | Reduces TravelBlock time | Prioritize TravelBlock Mon-Fri, other projects on weekends |
| Map implementation more complex | Delays v0.6 | Spike on map library early; use agents for implementation |
| Async agents need iteration | Delays versions | Plan 3-5 day reviews + iteration cycles |

**If Behind Schedule:**

- v0.7.0 (audio) is simplest — could compress from 12 days to 8 if needed
- v0.8.0 (post-flight) could be delayed 1 week into late March if needed
- v0.10.0 (polish) is flexible — could extend into early May
- **Do NOT skip async agent use** — it's critical to staying on schedule

---

## Recommended Async Agent Setup

**Google Jules / GitHub Copilot Agent Usage Plan:**

**Winter Break (Jan 4-19):**

- Minimal use; you're productive manually
- Use agents only for v0.4.0 PRs #1-3 if you want to move faster

**Spring Semester (Jan 19 - May 11):**

- v0.5.0 (Feb 2 deadline): Assign PRs #2-4 to agent on Jan 15-18 (before school starts)
  - You handle PR #1 (architecture) during break, agent does complex logic
- v0.6.0 (Feb 16 deadline): Assign PRs #1-3 to agent around Feb 5
- v0.7.0 (Feb 28 deadline): Assign PR #1 to agent around Feb 18
- v0.8.0 (Mar 14 deadline): Assign PRs #2-3 to agent around Mar 5
- v0.9.0 (Apr 1 deadline): Partial agent for charts/analytics PR if needed around Mar 25
- v0.10.0 (Apr 20 deadline): Assign PRs #1-2 (UI refinement) to agent around Apr 5

**Pattern:**

- Assign work to agent **1 week before deadline**
- Review/iterate during the week
- Handle final integration/testing before deadline
- Allows you to spread work across school schedule

---

## Success Metrics

✅ **v0.2.0 by Jan 9** — Sets foundation for everything
✅ **v0.3.0 by Jan 13** — Persistent home airport works
✅ **v0.4.0 by Jan 18** — Time slider working before school
✅ **v0.5.0 by Feb 2** — Core flight simulation logic complete
✅ **v0.6.0 by Feb 16** — Map visualization working
✅ **v0.7-v0.8 by Mar 14** — Audio and post-flight flows
✅ **v0.9-v0.10 by Apr 20** — Logbook and polish complete
✅ **v1.0 by May 10** — Ready for store submission before finals
✅ **Live on stores by June 1** — Post-release monitoring
