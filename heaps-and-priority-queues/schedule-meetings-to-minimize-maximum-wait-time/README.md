# Schedule Meetings to Minimize Maximum Wait Time

**Difficulty:** Hard &nbsp;|&nbsp; **Topic:** Heaps and Priority Queues &nbsp;|&nbsp; **Tags:** Heap, Priority Queue, Greedy, Simulation, Sorting

---

## 🗂 Problem Overview

Given `k` meeting rooms and `n` arrival-ordered requests `[arrivalTime, duration]`, assign each meeting greedily to the room that becomes free earliest — breaking ties by lowest room index. Track how long each meeting waits before it can start (`max(0, roomAvailableTime - arrivalTime)`), and return the single largest wait time observed. The non-trivial constraint: arrivals can be arbitrarily sparse or dense relative to room capacity, so the bottleneck room and maximum wait are not statically predictable.

---

## 🌍 Engineering Impact

This pattern is the core of any work-stealing or least-loaded dispatcher: Kubernetes scheduler assigns pods to the least-utilized node, thread pools route tasks to the shortest queue, and database connection poolers hand off queries to the first available connection. At scale, a naive linear scan over rooms per request degrades from acceptable to catastrophic — O(n·k) at n=10⁵, k=100 is 10⁷ operations, but the heap formulation keeps it at O(n log k), which is the difference between a 10ms and a 1s p99 in a hot scheduling loop.

---

## 🔍 Problem Statement

**Input:** Integer `k` (rooms), list `requests` where `requests[i] = [arrivalTime, duration]`, sorted in non-decreasing order of `arrivalTime`.

**Output:** Maximum wait time across all meetings, where `waitTime = max(0, roomAvailableTime - arrivalTime)`.

**Assignment rule:** Each incoming meeting goes to the room with the earliest `availableTime`. Ties (multiple rooms free at or before arrival) resolve to the lowest room index.

**Constraints:**
- `1 ≤ k ≤ 100`
- `1 ≤ n ≤ 10⁵`
- `0 ≤ arrivalTime ≤ 10⁹`
- `1 ≤ duration ≤ 10⁴`

**Edge cases to handle:** All meetings arrive simultaneously (maximum contention), a single room (`k=1`, fully serialized), and sparse arrivals where every meeting finds a free room (all waits are zero). The constraint driving algorithmic choice is the need to repeatedly extract the minimum `availableTime` across `k` rooms — a textbook priority queue operation.

---

## 🪜 How to Solve This

1. **Read the assignment rule** → "room that becomes free earliest" is a repeated minimum extraction over a dynamic set. That's a min-heap, not a linear scan.

2. **Notice the tie-breaking** → when multiple rooms are free at or before arrival, pick the lowest index. This means the heap key isn't just `availableTime` — it must be `(availableTime, roomIndex)` so the heap naturally resolves ties correctly.

3. **Model room state minimally** → each room only needs its next `availableTime`. Initialize all rooms to `0` (all free at time zero). The heap starts as `[(0, 0), (0, 1), ..., (0, k-1)]`.

4. **Per meeting, pop the heap minimum** → that's the earliest-free room. Compute `wait = max(0, availableTime - arrivalTime)`. Update `availableTime = max(availableTime, arrivalTime) + duration`. Push back.

5. **Track the running maximum wait** → a single variable updated each iteration.

6. **Recognize why greedy is optimal here** → there's no future information that would change which room to assign now; the earliest-free room always minimizes the current meeting's wait, and we're asked for the global maximum, not a sum.

---

## 🧩 Algorithm Walkthrough

**Pattern: Greedy Simulation with Min-Heap**

The min-heap maintains the invariant that the room with the smallest `(availableTime, roomIndex)` is always at the top, enabling O(log k) extraction and reinsertion per meeting.

**Steps:**

1. **Initialize the heap** with `(0, i)` for `i in 0..k-1`. All rooms start free at time 0. Heapify is O(k).

2. **Iterate over requests in arrival order** (already sorted per constraints, no pre-sort needed).

3. **Pop `(roomAvail, roomIdx)`** — the globally earliest-free room. This is O(log k).

4. **Compute wait:** `wait = max(0, roomAvail - arrivalTime)`. If `roomAvail ≤ arrivalTime`, the room was idle and wait is zero. Otherwise, the meeting queues behind the current occupant.

5. **Compute new availability:** `newAvail = max(roomAvail, arrivalTime) + duration`. The `max` handles the case where the room was already idle — the meeting starts immediately at `arrivalTime`, not at `roomAvail`.

6. **Push `(newAvail, roomIdx)`** back onto the heap. O(log k).

7. **Update `maxWait = max(maxWait, wait)`.**

8. **Return `maxWait`** after processing all requests.

**Why this is correct:** At every step, assigning to the earliest-free room minimizes the current meeting's wait. Since we want the maximum across all meetings, and no assignment choice can reduce a future meeting's wait by accepting a longer wait now (rooms are independent), greedy is globally optimal.

---

## 📊 Worked Example

**Input:** `k = 2`, `requests = [[0,5],[1,3],[2,4],[6,2]]`

| Step | Meeting | Arrival | Heap (pop) | roomAvail | wait | newAvail | maxWait | Heap after push |
|------|---------|---------|------------|-----------|------|----------|---------|-----------------|
| 1 | 0 | 0 | (0, 0) | 0 | 0 | 5 | 0 | [(0,1),(5,0)] |
| 2 | 1 | 1 | (0, 1) | 0 | 0 | 4 | 0 | [(4,1),(5,0)] |
| 3 | 2 | 2 | (4, 1) | 4 | 2 | 8 | 2 | [(5,0),(8,1)] |
| 4 | 3 | 6 | (5, 0) | 5 | 0 | 8 | 2 | [(8,0),(8,1)] |

**Output:** `2`

Meeting 2 waits for Room 1 to free at t=4, arriving at t=2 — a 2-unit wait. Meeting 3 finds Room 0 already idle at t=6, so no wait. Maximum is 2.

---

## ⏱ Complexity Analysis

### Time Complexity

**O(n log k).** Each of the `n` meetings triggers one heap pop and one heap push, each costing O(log k). Initialization is O(k). At n=10⁵ and k=100, this is roughly 700K operations — well within a 10ms budget. At n=10⁹ (streaming scenario), the log k factor keeps it tractable where O(n·k) would not.

### Space Complexity

**O(k).** The heap holds exactly `k` entries at all times — one per room. No auxiliary structures scale with `n`. This cannot be meaningfully reduced further; you need at least one state variable per room to track availability.

---

## 💡 Key Takeaways

- **Pattern signal — repeated minimum extraction over a bounded set:** Any time the assignment rule is "pick the current minimum of a dynamic set and reinsert an updated value," reach for a min-heap before considering any other structure.
- **Pattern signal — tie-breaking encoded in the key:** When the problem specifies a secondary sort criterion for ties (here, room index), encode it as a tuple key in the heap rather than adding post-hoc logic — the heap's natural ordering handles it for free.
- **Gotcha — the `max(roomAvail, arrivalTime)` in `newAvail`:** Forgetting this produces incorrect end times when a room is idle. The meeting starts at `arrivalTime`, not at `roomAvail`, when the room is already free.
- **Gotcha — initializing all rooms to `(0, i)`:** If you initialize to `(0, 0)` for all rooms (omitting the index), the heap won't break ties by room index correctly, and the first `k` meetings may be assigned in wrong order, corrupting downstream state.
- **Architectural insight:** This heap-per-resource pattern generalizes directly to any dispatcher where resources have heterogeneous cooldown times — rate limiters, connection pools, worker threads. The heap replaces a polling loop, converting O(k) scans into O(log k) decisions and making the system scale horizontally without redesign.

---

## 🚀 Variations & Further Practice

- **Minimize total wait time instead of maximum:** The greedy assignment rule stays the same, but the objective changes from `max` to `sum`. The twist: this is now an optimization problem where you can prove greedy is still optimal via an exchange argument, but the proof is non-trivial and worth working through explicitly.
- **Rooms have different capacities (each room holds up to `c` concurrent meetings):** The heap entry must now track a count of active meetings per room, and a room becomes "full" rather than "busy." This introduces a two-level scheduling decision and maps directly to multi-tenant resource allocation in cloud schedulers.
- **Meetings can be preempted or rescheduled:** If a higher-priority meeting can evict a running one, the problem becomes a preemptive scheduling problem (related to LeetCode 2402 — Meeting Rooms III), requiring both a min-heap for room availability and a second heap to track preempted meetings waiting for re-admission.