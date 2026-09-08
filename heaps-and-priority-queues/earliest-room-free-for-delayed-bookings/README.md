# Earliest Room Free for Delayed Bookings

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Heaps and Priority Queues &nbsp;|&nbsp; **Tags:** heap, priority-queue, simulation

---

## 🗂 Problem Overview
Given `n` numbered rooms and meeting requests `[start, duration]`, schedule each request in ascending `start` order. If any room is free at `start`, assign the smallest-numbered free room immediately; otherwise delay the meeting until the earliest room release, breaking ties again by smallest room number. Return the actual start time for every request. The non-trivial part is handling up to `2 * 10^5` requests efficiently while preserving both time-order and tie-breaking rules.

## 🌍 Engineering Impact
This is the same scheduling primitive used in job dispatchers, cluster schedulers, RPC worker pools, CI executors, and stream-processing slots. You have a fixed-capacity resource pool, arrivals with preferred start times, and deterministic tie-breaking requirements. At small scale, a linear scan over resources works; at production scale, it collapses under queue depth and high churn. The heap-based approach gives predictable `O(log n)` assignment and release behavior, which matters when scheduling decisions sit on the hot path. It also preserves fairness and reproducibility, both critical for debugging, auditability, and capacity planning.

## 🔍 Problem Statement
You are given `n` identical meeting rooms numbered `0` through `n - 1`, and a list of meeting requests `meetings[i] = [start, duration]`. Requests must be processed in increasing `start` order; if multiple requests share the same `start`, keep their original input order.

For each request:
- If one or more rooms are free at `start`, assign the meeting immediately to the free room with the smallest index.
- If all rooms are busy, delay the meeting until the earliest room becomes free. If multiple rooms free at that same earliest time, use the smallest room index.
- The meeting always runs for its full `duration` from its actual start time.

Return `answer[i]`, the actual start time of the `i`-th request.

Constraints:
- `1 <= n <= 10^5`
- `1 <= meetings.length <= 2 * 10^5`
- `0 <= start <= 10^9`, `1 <= duration <= 10^9`
- Actual start times may exceed `10^9`, so 64-bit arithmetic is required.

Examples:
- `n = 2, meetings = [[1,4],[2,3],[3,2],[10,1]]` → `[1,2,5,10]`
- `n = 1, meetings = [[0,5],[1,2],[1,4],[8,3]]` → `[0,5,7,11]`

## 🪜 How to Solve This
1. Read the rules carefully → this is not just “find a free room.” It is a simulation with strict tie-breaking on both time and room index.

2. Notice the operations we need repeatedly:
   - find all rooms that have become free by time `start`
   - pick the smallest free room
   - if none are free, pick the room that frees earliest, then smallest index

3. A linear scan over all rooms per meeting is too expensive: `O(m * n)` is dead on arrival for `10^5` rooms and `2 * 10^5` meetings.

4. That suggests splitting state into two priority queues:
   - free rooms ordered by room number
   - busy rooms ordered by `(endTime, roomNumber)`

5. Process meetings in sorted arrival order. Before scheduling a request, release every busy room whose end time is `<= start` into the free-room heap.

6. If the free heap is non-empty, start immediately. Otherwise, pop the earliest-finishing busy room and delay the meeting to that end time.

7. Push the chosen room back into the busy heap with its new end time.

That gives the exact behavior the problem specifies, with logarithmic updates and deterministic tie resolution.

## 🧩 Algorithm Walkthrough
1. **Sort requests by `(start, originalIndex)`**.  
   This preserves the required processing order, including stable handling of equal start times. The invariant is: when handling a request, every earlier request has already been fully scheduled.

2. **Initialize two heaps**.  
   - `freeRooms`: min-heap of room indices  
   - `busyRooms`: min-heap of `(endTime, roomIndex)`  
   Initially, all room indices go into `freeRooms`. This models total room availability at time `0`.

3. **For each meeting `(start, duration)`**, first release rooms.  
   While `busyRooms.top.endTime <= start`, pop that room and push its index into `freeRooms`.  
   Why correct: any room ending on or before `start` is available for immediate reuse.  
   Invariant: after this step, `busyRooms` contains only rooms still occupied at `start`.

4. **Choose a room using the scheduling rule**.  
   - If `freeRooms` is non-empty, pop the smallest room index and set `actualStart = start`.  
   - Otherwise, pop `(earliestEnd, room)` from `busyRooms` and set `actualStart = earliestEnd`.  
   This exactly matches the problem’s priority order: immediate assignment if possible, otherwise earliest release, then smallest room index.

5. **Compute the new end time and record the answer**.  
   `actualEnd = actualStart + duration`, store `answer[originalIndex] = actualStart`, then push `(actualEnd, room)` into `busyRooms`.  
   Invariant: every scheduled meeting reserves exactly one room for its full duration.

This is a classic **dual-heap simulation** pattern: one heap for available resources, one for future releases. It is the right abstraction because the problem is fundamentally about maintaining two ordered frontiers under continuous state transitions.

## 📊 Worked Example
Example: `n = 2`, `meetings = [[1,4],[2,3],[3,2],[10,1]]`

| Step | Request | Released before scheduling | Free rooms | Busy rooms before pick | Chosen room | Actual start | New end |
|---|---|---|---|---|---|---|---|
| 1 | `[1,4]` | none | `[0,1]` | `[]` | `0` | `1` | `5` |
| 2 | `[2,3]` | none | `[1]` | `[(5,0)]` | `1` | `2` | `5` |
| 3 | `[3,2]` | none | `[]` | `[(5,0),(5,1)]` | `0` | `5` | `7` |
| 4 | `[10,1]` | rooms `1` at `5`, `0` at `7` | `[0,1]` | `[]` | `0` | `10` | `11` |

Recorded starts by original request index: `[1,2,5,10]`.

The important transition is step 3: no room is free at time `3`, so the meeting is delayed to the earliest release time `5`, and room `0` wins because both rooms free at `5` and `0 < 1`.

## ⏱ Complexity Analysis
### Time Complexity
Sorting meetings costs `O(m log m)`, where `m = meetings.length`. Each meeting causes a constant number of heap pushes and pops, each `O(log n)`, so the simulation is `O(m log n)`. Overall: `O(m log m + m log n)`. At million-scale inputs this is practical; quadratic scanning is not. At billion-scale, even heap-based exact simulation becomes throughput-bound and would need partitioning or approximation.

### Space Complexity
`O(n + m)` in the straightforward implementation: `O(n)` for the two room heaps and `O(m)` for the sorted meeting list plus answer array. The dominant owned state is the event ordering and active-room tracking. You can reduce auxiliary sorting overhead with in-place index sorting, but not below the answer storage requirement.

## 💡 Key Takeaways
- If a problem says “smallest available resource now, otherwise earliest resource to free later,” think dual heaps immediately.
- If arrivals are processed in time order and resources transition between available and busy states, this is a simulation problem, not a greedy scan.
- Release rooms with `endTime <= start`, not `< start`; meetings ending exactly at arrival time are immediately reusable.
- Use 64-bit integers for actual start and end times; chained delays can push values far beyond the original timestamp bounds.
- The production-grade insight is to separate present availability from future availability: one ordered structure for what can be assigned now, another for when blocked capacity returns.

## 🚀 Variations & Further Practice
- Return the assigned room for each meeting, or the room used most often; same dual-heap core, but now you must track per-room utilization and tie-breaking over aggregate counts.
- Add cancellations or dynamic room capacity changes; this turns simple heap simulation into mutable event management, often requiring lazy deletion or indexed priority queues.
- Schedule weighted or priority meetings where delayed jobs can overtake earlier arrivals; the harder twist is that FIFO by arrival no longer holds, so you need a second policy queue over waiting work.