# Minimum Station Range to Relay a Mountain Convoy

**Difficulty:** Hard &nbsp;|&nbsp; **Topic:** Binary Search &nbsp;|&nbsp; **Tags:** binary-search, greedy, dynamic-programming

---

## 🗂 Problem Overview
Given sorted station positions on a line, a destination distance `D`, and a budget of at most `k` activated stations, compute the minimum integer transmission range `R` that allows a relay chain from checkpoint `0` to checkpoint `D`. Consecutive points in the chain must be within `R`. The challenge is not choosing a locally good station, but proving global feasibility under large constraints: up to `2e5` stations and distances up to `1e18`.

## 🌍 Engineering Impact
This pattern shows up anywhere you need the smallest capability threshold that makes an end-to-end path possible under a resource cap. Examples include WAN repeater placement, edge cache promotion, multi-hop wireless backhaul, streaming pipeline checkpoint insertion, and distributed workflow retries with bounded intermediates. At scale, brute-force search over configurations collapses immediately: the state space is combinatorial, while the decision boundary is monotone. Binary-searching the capability and using a linear feasibility check turns an intractable design-space exploration problem into something predictable, testable, and production-safe under large inputs and tight latency budgets.

## 🔍 Problem Statement
You are given a strictly increasing array `positions` of length `n`, where each value lies in `(0, D)`, plus integers `D` and `k`. Checkpoints `0` and `D` are always present and do not count toward the station budget. You may activate at most `k` stations from `positions`.

A range `R` is feasible if there exists a chain starting at `0`, ending at `D`, and using at most `k` activated stations such that every consecutive hop is at most `R`.

Return the minimum feasible integer `R`.

Constraints:
- `1 <= n <= 200000`
- `1 <= D <= 10^18`
- `0 <= k <= n`
- `positions` is strictly increasing
- answer fits in signed 64-bit

Examples:
- `positions = [2,5,8,12], D = 15, k = 2` → `7`
- `positions = [4,9,14,20,27], D = 30, k = 3` → `10`

The decisive constraint is scale: `n = 2e5` rules out exponential search and pushes toward `O(n log D)`.

## 🪜 How to Solve This
1. Read the problem → the output is a minimum possible threshold, not a path itself. That is a strong signal for **binary search on the answer**.

2. Ask the decision version: for a fixed `R`, can we reach `D` from `0` using at most `k` stations? If that can be checked efficiently, the optimization problem is solved.

3. Notice monotonicity → if range `R` works, any larger range also works. Larger hops never reduce feasibility.

4. Now solve the fixed-`R` problem → among all valid chains with hop limit `R`, we want the **minimum number of activated stations**. If that minimum is `<= k`, then `R` is feasible.

5. The right greedy idea is “jump as far as possible each time.” From the current point, choose the farthest reachable next station; only go directly to `D` when possible. This minimizes the number of intermediate stops, exactly like minimizing refuels or interval jumps.

6. With sorted positions, that greedy check is linear. Combine it with binary search over `R` in `[1, D]`, and you get an efficient solution.

## 🧩 Algorithm Walkthrough
1. **Model the line explicitly**  
   Treat checkpoints and stations as ordered points on a 1D axis. We start at `cur = 0` and want to reach `D`. The only thing that matters is hop length, so sorted order gives a natural left-to-right traversal.

2. **Use Binary Search on Answer**  
   Search `R` over the integer range `[1, D]`.  
   Invariant:  
   - all values below the first feasible `R` are impossible  
   - all values at or above it are feasible  
   This works because feasibility is monotone.

3. **Define the feasibility check**  
   For a candidate `R`, compute the minimum number of stations needed to reach `D`. If that minimum is `<= k`, return feasible.

4. **Apply the Greedy Jump pattern**  
   From the current point `cur`, if `D - cur <= R`, we are done. Otherwise, among all stations with `position <= cur + R`, choose the farthest one. Activate it, increment the station count, and continue from there.  
   Why correct: any nearer choice cannot improve future reach compared with the farthest reachable one, so it cannot reduce the number of remaining stations.

5. **Detect impossibility early**  
   If no station lies in `(cur, cur + R]` and `D` is still farther than `R`, then `R` is infeasible.

6. **Maintain the key invariant**  
   After each greedy step, `cur` is the farthest position reachable using exactly the counted number of activated stations while respecting hop limit `R`. That invariant is what makes the greedy proof work.

7. **Complexity shape**  
   The feasibility check scans `positions` once with a moving index, so it is `O(n)`. Binary search contributes a `log D` factor.

## 📊 Worked Example
Use `positions = [2,5,8,12], D = 15, k = 2`, and test `R = 7`.

| Step | cur | Reachable within `R` | Greedy choice | stations used |
|---|---:|---|---:|---:|
| 1 | 0 | 2, 5 | 5 | 1 |
| 2 | 5 | 8, 12 | 12 | 2 |
| 3 | 12 | `D = 15` is within 7 | finish | 2 |

Trace:
1. Start at checkpoint `0`. We cannot reach `15` directly because `15 > 7`.
2. Among stations at distance at most `7`, the farthest is `5`. Choosing `2` would only reduce future reach.
3. From `5`, stations `8` and `12` are reachable; greedy picks `12`.
4. From `12`, destination `15` is within range.
5. Total activated stations = `2`, which matches the budget, so `R = 7` is feasible.

Trying smaller values fails, so the answer is `7`.

## ⏱ Complexity Analysis
### Time Complexity
`O(n log D)`. Each feasibility check is `O(n)` because the station pointer only moves forward once, and binary search performs `O(log D)` checks. With `D <= 1e18`, `log2(D)` is about 60, so even at `n = 2e5`, the total work is practical. At `10^9` elements, the linear scan dominates and the approach becomes bandwidth-bound rather than algorithmically expensive.

### Space Complexity
`O(1)` auxiliary space beyond the input array. The algorithm stores only indices, counters, and current position. You could wrap endpoints into a new array for convenience, but that raises space to `O(n)` without improving asymptotic runtime.

## 💡 Key Takeaways
- If the problem asks for the **minimum threshold** that makes a constraint satisfiable, check immediately for a monotone decision function and binary-search the answer.
- If a fixed threshold asks for the **fewest jumps/stops/segments** on a sorted line, the farthest-reachable greedy is a strong candidate.
- Be careful not to count checkpoints `0` and `D` against `k`; only activated stations consume budget.
- The feasibility check must choose the farthest reachable station, not the first reachable one; otherwise you can overcount stations and reject valid ranges.
- In production systems, this is the standard move for turning a combinatorial placement problem into a threshold search plus a linear verifier.

## 🚀 Variations & Further Practice
- Allow activating stations with different costs and require total cost `<= B` instead of count `<= k`; the feasibility check becomes shortest-path or DP rather than pure greedy.
- Extend from a line to a graph of relay nodes; binary search still applies, but feasibility becomes reachability with a hop or node-budget constraint.
- Require exactly `k` stations or support online station insertions/removals; the monotone threshold remains, but maintaining feasibility efficiently needs more sophisticated data structures.