# Minimum Dock Bays for Delayed Cargo Unloading

**Difficulty:** Hard &nbsp;|&nbsp; **Topic:** Heaps and Priority Queues &nbsp;|&nbsp; **Tags:** heap, priority-queue, binary-search, simulation

---

## 🗂 Problem Overview
Given unsorted ship arrivals, unloading durations, and a maximum allowed delay `T`, compute the minimum number of dock bays required so every ship starts unloading within `T` time units of its scheduled arrival. The difficulty is not just capacity sizing: dispatch order is constrained. Whenever a bay frees up, the terminal must choose the waiting ship with the smallest original arrival time, breaking ties by input index. That rule eliminates greedy reordering and forces an event-driven simulation.

## 🌍 Engineering Impact
This pattern shows up in admission control and constrained schedulers: Kubernetes pod placement under fairness rules, warehouse dock assignment, NIC queue servicing, database connection pools, and streaming systems with SLA-bound backlog. The hard part is not resource counting in isolation; it is resource counting under a mandatory dispatch policy. At scale, naive “just add workers until latency looks fine” approaches fail because queue discipline changes feasibility. The combination of binary search over capacity and heap-based simulation gives a decision procedure you can trust, making it useful for capacity planning, SLO validation, and what-if modeling before production incidents.

## 🔍 Problem Statement
You are given `n` ships, where `arrival[i]` is ship `i`’s scheduled arrival time and `unload[i]` is its unloading duration. A ship occupies one dock bay continuously from its start time until completion. If no bay is free at arrival, the ship waits.

Dispatch is constrained: whenever a dock becomes available and ships are waiting, the terminal must start the waiting ship with the smallest original arrival time; if multiple ships share that arrival time, choose the smaller input index.

Find the minimum number of bays such that every ship starts within `T` time units of arrival, i.e. `start[i] - arrival[i] <= T`. Return `-1` if no bay count can satisfy the rule.

Constraints: `1 <= n <= 200000`, `0 <= arrival[i] <= 1e9`, `1 <= unload[i] <= 1e9`, `0 <= T <= 1e9`, and `arrival` is unsorted.

Examples:
- `arrival = [1,2,4], unload = [5,2,3], T = 2` → `2`
- `arrival = [0,1,1,3], unload = [4,2,5,1], T = 1` → `3`

The `n = 2e5` bound rules out brute-force over bay counts or quadratic queue simulation.

## 🪜 How to Solve This
1. Read the rule carefully → this is not ordinary interval overlap. Ships can wait, but once a bay frees, the next ship is forced by queue order: smallest arrival time, then smallest index.

2. That suggests simulation, not formulae → we need to know which ships have arrived, which bays free next, and which waiting ship is legally next.

3. But the question asks for the **minimum** number of bays → that usually means a monotone decision problem. If `k` bays work, then any `k+1` bays also work.

4. Monotonicity immediately suggests binary search on the answer over `1..n`.

5. Now define `feasible(k)` → simulate the terminal with exactly `k` bays and check whether any ship starts later than `arrival[i] + T`.

6. To simulate efficiently, sort ships by `(arrival, index)`, keep busy bays in a min-heap by finish time, and process time as events:
   - add newly arrived ships to the waiting queue,
   - release bays that have finished,
   - assign free bays to the earliest waiting ships.

7. The key insight: the waiting queue order is exactly the sorted arrival/index order, so a FIFO pointer over that sorted stream is enough. No arbitrary reprioritization is allowed.

## 🧩 Algorithm Walkthrough
1. **Sort ships by dispatch priority**  
   Build tuples `(arrival[i], i, unload[i])` and sort by `(arrival, index)`. This matches the terminal’s mandatory tie-breaking rule. Invariant: among all ships that have arrived but not yet started, the earliest tuple in this order is the only legal next choice.

2. **Binary search the number of bays**  
   Search `k` in `[1, n]`. The predicate is monotone: if a schedule is feasible with `k` bays, adding more bays cannot delay any start. This is the standard **binary search on answer** pattern.

3. **Simulate `feasible(k)` with event-driven heaps**  
   Use a min-heap `busy` storing bay release times. Track:
   - `i`: next ship not yet arrived into the waiting set,
   - `q`: index of next waiting ship in sorted order,
   - `waitingCount`,
   - `freeBays`,
   - `time`.

4. **Advance by the next relevant event**  
   If no waiting ship can start now, jump `time` to the earlier of:
   - next ship arrival,
   - next bay release.  
   This avoids per-time-unit simulation and keeps runtime logarithmic per event.

5. **Materialize arrivals and releases at `time`**  
   Add all ships with `arrival <= time` into the waiting region by advancing `i`. Pop all `busy` entries with `finish <= time` and increase `freeBays`. Invariant: after this step, state reflects the exact system snapshot at `time`.

6. **Assign ships while both waiting and capacity exist**  
   Start ships in sorted order from pointer `q`. For each started ship, verify `time - arrival <= T`; if violated, return false immediately. Push `time + unload` into `busy`, decrement `freeBays`, increment `q`.

7. **Terminate correctly**  
   If all ships start without violating delay, return true. Overall, this is a **heap-based discrete-event simulation** wrapped in binary search, the right abstraction because both queue discipline and resource release are event ordered.

## 📊 Worked Example
Example: `arrival = [0,1,1,3]`, `unload = [4,2,5,1]`, `T = 1`, test `k = 2`.

Sorted ships: `(0,0,4), (1,1,2), (1,2,5), (3,3,1)`

| time | arrivals added | bays freed | waiting order | starts now | busy until |
|---|---|---:|---|---|---|
| 0 | ship 0 | 0 | [0] | 0 at 0 | [4] |
| 1 | ships 1,2 | 0 | [1,2] | 1 at 1 | [3,4] |
| 3 | ship 3 | bay from ship 1 | [2,3] | 2 at 3 | [4,8] |

At `time = 3`, ship 2 starts. Its wait is `3 - 1 = 2`, which exceeds `T = 1`, so `k = 2` is infeasible. With `k = 3`, ships 0, 1, and 2 start at `0, 1, 1`, and ship 3 starts at `3`, so all waits are within bound.

## ⏱ Complexity Analysis
### Time Complexity
Sorting costs `O(n log n)` once. Each feasibility check processes every ship arrival, start, and bay release once, with heap operations costing `O(log k)`, so `O(n log k)`. Wrapped in binary search over `k`, total complexity is `O(n log n + n log n log n)` in the worst case, effectively `O(n log^2 n)`.

### Space Complexity
`O(n)` space: the sorted ship list dominates, and the busy-bay heap holds at most `k <= n` finish times. You can avoid some tuple duplication with index indirection, but asymptotically it stays linear; reducing below that would complicate ordering logic without changing the bound.

## 💡 Key Takeaways
- If a problem asks for the **minimum resource count** and feasibility improves as resources increase, look for binary search on the answer.
- If dispatch order is constrained by arrival order plus tie-breaks, think discrete-event simulation with heaps rather than interval overlap formulas.
- Be careful to release **all** bays with `finish <= time` before assigning new ships; using `< time` is a classic off-by-one bug.
- The waiting order is by original `(arrival, index)`, not by shortest unload time or current delay; any alternative queue discipline makes the simulation incorrect.
- In production schedulers, capacity planning is only meaningful when modeled with the real queue discipline; fairness and ordering rules can change the required fleet size materially.

## 🚀 Variations & Further Practice
- Add bay heterogeneity: each ship can unload only at a subset of bays, turning a single-queue simulation into constrained matching with event ordering.
- Allow preemption or pause/resume unloading, which breaks the simple finish-time heap and pushes the model toward more complex scheduling theory.
- Replace fixed `T` with per-ship deadlines or penalties, forcing feasibility checks to reason about heterogeneous SLA budgets instead of one global bound.