# Minimum Recharge Cost for a Courier Drone Route

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Dynamic Programming &nbsp;|&nbsp; **Tags:** dynamic-programming, array, path-optimization

---

## 🗂 Problem Overview
Given two arrays, `cost` and `blocked`, compute the minimum total landing cost for a drone to reach checkpoint `n - 1`. The drone starts before the route, may first land on checkpoint `0` or `1`, and then can move forward by 1 or 2 checkpoints at a time. It pays only for checkpoints it lands on, and blocked checkpoints cannot be used. Return the minimum cost, or `-1` if no valid route exists. The non-trivial part is that local cheapest moves can lead to dead ends.

## 🌍 Engineering Impact
This pattern shows up in constrained path selection across linear workflows: packet forwarding across partially degraded network hops, job progression through staged pipelines with unavailable workers, storage tier transitions with skip-level promotion, and robotic motion planning on sparse safe states. At scale, greedy routing fails because a locally cheap transition can force an unreachable or expensive suffix. Dynamic programming gives deterministic optimality with linear cost, which matters when this logic sits inside schedulers, admission controllers, or route planners processing millions of decisions where backtracking or graph search would add unnecessary latency and operational complexity.

## 🔍 Problem Statement
You are given:

- `cost[i]`: recharge cost paid if the drone lands on checkpoint `i`
- `blocked[i]`: whether checkpoint `i` is unavailable (`1`) or usable (`0`)

The drone starts before checkpoint `0`, pays nothing initially, and must reach exactly checkpoint `n - 1`. It may first land on checkpoint `0` or `1`, then from any landed checkpoint `i` move to `i + 1` or `i + 2`. It may never land on blocked checkpoints. The first and last checkpoints are guaranteed unblocked.

Return the minimum total recharge cost, including the final checkpoint. If no valid route exists, return `-1`.

Constraints:

- `2 <= n <= 100000`
- `cost.length == blocked.length == n`
- `0 <= cost[i] <= 1000000000`

Examples:

- `cost = [4,2,7,3,5]`, `blocked = [0,0,1,0,0]` → `10`
- `cost = [6,1,9,2]`, `blocked = [0,1,1,0]` → `-1`

The key algorithmic driver is `n = 100000`: exponential search is impossible, and even quadratic exploration is unnecessary.

## 🪜 How to Solve This
1. Read the movement rules → each checkpoint depends only on the previous two checkpoints. That is the strongest signal for linear dynamic programming.

2. Define the subproblem: “What is the minimum cost to land on checkpoint `i`?” If you can answer that for every `i`, the final answer is just the value at `n - 1`.

3. Work backward from the transition rule:
   - To land on `i`, the drone must come from `i - 1` or `i - 2`.
   - So the best cost for `i` is `cost[i] + min(dp[i - 1], dp[i - 2])`.

4. Add feasibility:
   - If `blocked[i] == 1`, then `dp[i]` is unreachable.
   - If both predecessors are unreachable, `i` is also unreachable.

5. Handle the special start condition carefully:
   - The drone starts before the array, so it may begin at `0` or `1`.
   - That means `dp[0] = cost[0]`, and `dp[1] = cost[1]` if unblocked, not necessarily `cost[1] + dp[0]`.

6. Once the recurrence is clear, the implementation is straightforward: one left-to-right pass, maintaining minimum reachable cost per checkpoint.

## 🧩 Algorithm Walkthrough
1. **Use Dynamic Programming on a linear path.**  
   This is a classic 1D DP problem because each state depends on a fixed, small predecessor set. The right abstraction is: `dp[i] = minimum cost to land on checkpoint i`. That captures both optimization and reachability.

2. **Initialize unreachable states with infinity.**  
   Use a large sentinel such as `INF = Long.MAX_VALUE / 4`. This lets the recurrence stay uniform: blocked or unreachable checkpoints simply never contribute a valid minimum.

3. **Set the base cases from the start position.**  
   Since the drone starts before the route and pays nothing before landing:
   - `dp[0] = cost[0]`
   - `dp[1] = cost[1]` if checkpoint `1` is unblocked  
   This is correct because the drone may directly begin on either of those positions.

4. **Iterate from left to right.**  
   For each `i >= 2`:
   - If `blocked[i] == 1`, leave `dp[i] = INF`
   - Otherwise compute `bestPrev = min(dp[i - 1], dp[i - 2])`
   - If `bestPrev != INF`, set `dp[i] = bestPrev + cost[i]`

5. **Maintain the invariant.**  
   After processing index `i`, every `dp[k]` for `k <= i` is the minimum achievable landing cost for that checkpoint, or `INF` if unreachable. This holds because all legal incoming edges to `i` originate only from already-finalized states `i - 1` and `i - 2`.

6. **Return the result.**  
   If `dp[n - 1] == INF`, return `-1`; otherwise return `dp[n - 1]`.

Because each state is solved once from optimal predecessor states, the result is globally optimal without revisiting decisions.

## 📊 Worked Example
Example: `cost = [4, 2, 7, 3, 5]`, `blocked = [0, 0, 1, 0, 0]`

| i | blocked[i] | cost[i] | dp[i-2] | dp[i-1] | dp[i] | Reason |
|---|------------|---------|---------|---------|------:|--------|
| 0 | 0 | 4 | - | - | 4 | Start directly at 0 |
| 1 | 0 | 2 | - | 4 | 2 | Start directly at 1 |
| 2 | 1 | 7 | 4 | 2 | INF | Blocked |
| 3 | 0 | 3 | 2 | INF | 5 | Reach from 1 |
| 4 | 0 | 5 | INF | 5 | 10 | Reach from 3 |

Trace:
1. `dp[0] = 4`
2. `dp[1] = 2`
3. Checkpoint `2` is blocked, so unreachable.
4. Checkpoint `3` can only use `min(dp[1], dp[2]) = 2`, so `dp[3] = 2 + 3 = 5`.
5. Checkpoint `4` uses `min(dp[2], dp[3]) = 5`, so `dp[4] = 5 + 5 = 10`.

Answer: `10`.

## ⏱ Complexity Analysis

### Time Complexity
The algorithm runs in **O(n)** time: one pass over the arrays, with constant work per checkpoint. That is the dominant operation. At `10^6` elements this is routine in production code; at `10^9`, even linear scans become expensive, so the main lever is reducing I/O and memory pressure rather than changing asymptotic behavior.

### Space Complexity
Using a full DP array costs **O(n)** space. The array owns essentially all additional memory. Since each state depends only on the previous two, this can be reduced to **O(1)** space by keeping two rolling values, at the cost of losing full traceability for path reconstruction.

## 💡 Key Takeaways
- If each position’s optimal value depends only on a small fixed window of earlier positions, that is a strong signal for 1D dynamic programming.
- When the problem asks for a minimum-cost path on a linear structure with forbidden states, think “DP with unreachable sentinels,” not greedy choice.
- The start condition is easy to misread: checkpoint `1` can be an initial landing, so `dp[1]` is not necessarily derived from `dp[0]`.
- Blocked checkpoints must be treated as unreachable states, not as high-cost states; allowing landing with a penalty changes the problem.
- In production systems, this pattern matters because explicit state modeling separates feasibility from optimization, which is exactly what keeps schedulers and planners correct under partial outages.

## 🚀 Variations & Further Practice
- Allow jumps of up to `k` checkpoints instead of just 1 or 2. The twist is that the recurrence now depends on a sliding minimum over the previous `k` states, which invites deque-based optimization.
- Return not only the minimum cost but also the actual route. The harder part is preserving predecessor information while still handling unreachable states cleanly.
- Add a constraint on the maximum number of landings or total battery capacity. This turns the problem into multi-dimensional DP, where state explosion and pruning become the core challenge.