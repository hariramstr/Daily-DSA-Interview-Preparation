# Minimum Cooldown for Battery Cell Assembly

**Difficulty:** Hard &nbsp;|&nbsp; **Topic:** Binary Search &nbsp;|&nbsp; **Tags:** Binary Search, Greedy, Simulation

---

## 🗂 Problem Overview
Given `stations`, where `stations[i]` cells must be processed at station `i`, and a total time budget `T`, compute the smallest integer cooldown limit `C` that lets one robot finish all work in order. Time includes processing, movement between adjacent stations, and mandatory 1-second rests whenever the next processed cell would exceed `C` consecutive cells. The non-trivial part is state carry-over: the current consecutive-cell streak survives movement, so rest decisions couple adjacent stations.

## 🌍 Engineering Impact
This pattern shows up in throughput-constrained schedulers where local batching decisions affect downstream feasibility: CPU thermal throttling, GPU kernel batching, warehouse robot task planning, token-bucket style rate limiters with carry-over state, and streaming systems that amortize expensive resets or flushes. At scale, naive simulation per candidate policy is too slow, and station-local optimization is wrong because residual state crosses boundaries. The useful abstraction is monotone feasibility: if a cooldown limit works, any larger one works. That enables binary search over policy space while keeping the inner check linear and predictable under large input sizes.

## 🔍 Problem Statement
You are given an array `stations` of length up to `2 * 10^5`, where `stations[i]` is the number of battery cells processed at station `i`, and an integer `T` up to `10^18`. The robot starts at station `0`, processes all cells in order, and moves from station `i` to `i + 1` in 1 second. Processing one cell takes 1 second. If the robot has already processed `x` consecutive cells since its last rest, then processing the next cell is allowed only if the streak stays at most `C`; otherwise it must rest for 1 second, resetting the streak to 0.

Return the minimum feasible integer `C`. If even unlimited cooldown cannot fit in `T`, return `-1`.

Examples:

- `stations = [3,2,4], T = 12` → `2`
- `stations = [5,1,5], T = 15` → `3`

The key constraint is large input size plus `stations[i]` up to `10^9`, which rules out per-cell simulation.

## 🪜 How to Solve This
1. Start with the obvious lower bound: total processing time plus movement time is unavoidable. If that already exceeds `T`, return `-1`.

2. Notice the decision variable is not the schedule itself but the cooldown limit `C`. Larger `C` can only help, never hurt. That is the monotonicity signal for binary search on the answer.

3. Now ask: for a fixed `C`, how do we check feasibility without simulating every cell? We only need the minimum number of rests required, because total time is `baseTime + rests`.

4. The subtlety is that the current processed streak carries across station boundaries. So we need a greedy rule that minimizes rests globally, not station by station.

5. For a station with `a` cells and incoming streak `s`:
   - If `s + a <= C`, consume everything with no rest.
   - Otherwise, delaying the first rest as long as possible is always optimal; resting earlier only wastes remaining capacity.
   - After that first forced split, the remaining cells are packed into blocks of size `C`, which gives a closed-form rest count.

6. That yields an `O(n)` feasibility check, wrapped in `O(log answer)` binary search.

## 🧩 Algorithm Walkthrough
1. **Compute unavoidable base time.**  
   `base = sum(stations) + (n - 1)`. This covers all processing and movement. If `base > T`, no cooldown can help because rests only add time. Invariant: any feasible schedule must spend at least `base`.

2. **Binary search the minimum feasible `C`.**  
   Search `C` in `[1, max(stations)]`. `max(stations)` is always sufficient if `base <= T`, because no station alone forces an internal rest, and carrying streak across movement never creates a new violation unless more cells are processed. Pattern: **Binary Search on Monotone Predicate**.

3. **Define `feasible(C)` as “minimum rests needed under cooldown `C` fits in budget.”**  
   Track:
   - `streak`: consecutive processed cells since last rest after finishing the current station
   - `rests`: total mandatory rests so far  
   Invariant: after each station, `(rests, streak)` is the lexicographically optimal state minimizing rests, then maximizing remaining usable capacity.

4. **Process each station greedily.**  
   For station load `a`:
   - If `streak + a <= C`, set `streak += a`.
   - Else one rest is eventually unavoidable before finishing this station. Use the remaining capacity `C - streak` first, then rest once, then pack the remaining cells into full blocks of size `C`.  
     Let `rem = a - (C - streak)`. After consuming the carry-over capacity, add:
     - `1 + (rem - 1) / C` rests total for this station transition
     - final `streak = rem % C`, except when divisible by `C`, where `streak = C` because the robot ends immediately after a full block without needing an extra rest

5. **Check budget.**  
   Feasible iff `base + rests <= T`. Because the predicate is monotone, binary search returns the minimum valid `C`.

## 📊 Worked Example
Take `stations = [5,1,5]`, `T = 15`.

Base time = `5 + 1 + 5 + 2 moves = 13`, so we can afford at most `2` rests.

Check `C = 3`:

| Station | Incoming streak | Cells | Action | Added rests | Outgoing streak |
|---|---:|---:|---|---:|---:|
| 0 | 0 | 5 | Process 3, rest, process 2 | 1 | 2 |
| 1 | 2 | 1 | Fits exactly | 0 | 3 |
| 2 | 3 | 5 | No capacity left, rest, process 3, process 2 | 1 | 2 |

Total rests = `2`, so total time = `13 + 2 = 15`, feasible.

Check `C = 2`:
- Station 0 needs 2 rests to process `5`
- Station 1 ends with streak `2`
- Station 2 starts full, forcing more splits  
Total rests exceed `2`, so `C = 2` is infeasible.

Therefore the minimum feasible cooldown is `3`.

## ⏱ Complexity Analysis
### Time Complexity
`O(n log M)`, where `n = stations.length` and `M = max(stations)`. Each feasibility check is a single linear pass with constant work per station, and binary search performs `log M` checks. At `n = 10^6`, this remains practical; at `10^9`, only streaming or distributed variants would be viable.

### Space Complexity
`O(1)` auxiliary space beyond the input array. The algorithm stores only running totals such as `base`, `rests`, and `streak`. This is already optimal; reducing further is meaningless unless the input itself is streamed rather than materialized.

## 💡 Key Takeaways
- If the problem asks for the minimum integer limit such that a schedule becomes feasible, look for a monotone predicate and binary search the answer.
- If local actions affect future capacity through carry-over state, expect a greedy feasibility check rather than independent per-segment optimization.
- The final streak after a station is part of the state; dropping it and counting rests per station independently gives wrong answers.
- When a remainder is exactly divisible by `C`, the outgoing streak is `C`, not `0`; `0` would incorrectly imply a free reset without paying a rest.
- In production schedulers, the scalable move is often to search policy space and make the inner simulation state-minimal and monotone.

## 🚀 Variations & Further Practice
- Allow variable movement times or station-specific rest costs. The monotonicity may still hold, but the closed-form station transition becomes more delicate.
- Add optional proactive rests with different costs or cooldown decay during movement. This turns the greedy check into a richer state-transition problem, potentially requiring DP.
- Generalize to multiple robot arms sharing a global thermal budget. The core twist becomes resource coupling across workers rather than a single carry-over streak.