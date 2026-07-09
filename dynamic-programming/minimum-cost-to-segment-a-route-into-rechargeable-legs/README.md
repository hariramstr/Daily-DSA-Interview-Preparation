# Minimum Cost to Segment a Route into Rechargeable Legs

**Difficulty:** Hard &nbsp;|&nbsp; **Topic:** Dynamic Programming &nbsp;|&nbsp; **Tags:** Dynamic Programming, Convex Hull Trick, Prefix Sum

---

## 🗂 Problem Overview
Given checkpoint-to-checkpoint distances, per-stop recharge fees, and a quadratic battery cost for each uninterrupted flight leg, compute the minimum total cost to travel from checkpoint `0` to checkpoint `n - 1`. You may stop at any intermediate checkpoints, paying their service fees. The challenge is that the natural DP transition considers every prior stop for every destination, which is `O(n^2)` and infeasible for `n = 200000`.

## 🌍 Engineering Impact
This pattern shows up anywhere segmentation decisions trade off fixed overhead against superlinear run cost. Examples include batching in streaming pipelines, checkpoint placement in distributed data processing, compaction scheduling in storage engines, and route partitioning in logistics or robotics. At small scale, brute-force DP is acceptable; at production scale, it collapses under quadratic state transitions. The value of this approach is not just asymptotic improvement — it converts a planning problem from “offline only” to “operationally usable,” enabling larger horizons, tighter SLAs, and more predictable resource envelopes.

## 🔍 Problem Statement
You are given `n` checkpoints ordered west to east. The drone starts at checkpoint `0` and must reach checkpoint `n - 1`. For `1 <= i < n`, `dist[i]` is the distance from checkpoint `i - 1` to `i`. If the drone flies directly from checkpoint `j` to checkpoint `i`, the leg length is the prefix-distance difference between them, and the battery cost is `batteryCost * L^2`. Any intermediate checkpoint used as a recharge stop adds `fee[i]`, with `fee[0] = fee[n - 1] = 0`.

Choose an increasing sequence `0 = p0 < p1 < ... < pk = n - 1` minimizing total leg cost plus intermediate stop fees.

- `2 <= n <= 200000`
- `1 <= dist[i] <= 10^6`
- `0 <= fee[i] <= 10^12`
- `1 <= batteryCost <= 10^6`

Examples:
- `n=5, dist=[0,3,2,4,2], fee=[0,5,1,6,0], batteryCost=1` → `30`
- `n=4, dist=[0,1,1,1], fee=[0,100,100,0], batteryCost=2` → `18`

The decisive constraint is `n = 200000`: `O(n^2)` DP will time out.

## 🪜 How to Solve This
1. Start with the obvious DP: let `dp[i]` be the minimum cost to reach checkpoint `i`.
2. Transition from any prior recharge point `j`:
   `dp[i] = min(dp[j] + batteryCost * (pos[i] - pos[j])^2 + fee[i])`, except no fee at `i = n - 1`.
3. Expand the square:
   `batteryCost * pos[i]^2 - 2 * batteryCost * pos[i] * pos[j] + batteryCost * pos[j]^2`.
4. For fixed `i`, `batteryCost * pos[i]^2` is constant. The minimization is really over:
   `(-2 * batteryCost * pos[j]) * pos[i] + (dp[j] + batteryCost * pos[j]^2)`.
5. That is a set of lines queried at `x = pos[i]`:
   - slope `m = -2 * batteryCost * pos[j]`
   - intercept `b = dp[j] + batteryCost * pos[j]^2`
6. Prefix positions are nondecreasing, so inserted slopes are monotonic and query `x` values are monotonic.
7. That is the exact signal for the monotonic Convex Hull Trick: maintain the lower hull of candidate lines and query in amortized `O(1)`.
8. Add each computed `dp[i]` back as a new line for future checkpoints.

Once you see “DP over previous indices + quadratic distance term + monotone prefix sums,” the optimization path is straightforward.

## 🧩 Algorithm Walkthrough
1. **Build prefix positions (`Prefix Sum`)**  
   Compute `pos[0] = 0`, `pos[i] = pos[i-1] + dist[i]`. This converts leg length from a range sum into `pos[i] - pos[j]`. The invariant is that every route leg cost can now be expressed using two point coordinates.

2. **Define the DP state (`Dynamic Programming`)**  
   Let `dp[i]` be the minimum cost to arrive at checkpoint `i`, including any fee paid at `i` if it is an intermediate stop. For the terminal checkpoint, fee is zero by definition. This captures optimal substructure: any optimal route to `i` ends with some last recharge point `j`.

3. **Rewrite the transition algebraically**  
   `dp[i] = min_j(dp[j] + C * (pos[i] - pos[j])^2) + extraFee(i)`, where `C = batteryCost`. Expanding the square isolates all `j`-dependent terms into a linear form in `pos[i]`. That is the key transformation from quadratic DP to line queries.

4. **Map prior states to lines (`Convex Hull Trick`)**  
   Each prior checkpoint `j` contributes a line:
   `y = m_j * x + b_j`, with  
   `m_j = -2 * C * pos[j]` and  
   `b_j = dp[j] + C * pos[j]^2`.  
   Querying the minimum line value at `x = pos[i]` gives the best predecessor contribution. The invariant is that the hull contains exactly the non-dominated prior transitions.

5. **Exploit monotonicity for linear-time hull maintenance**  
   Since `pos[j]` increases with `j`, slopes are inserted in monotone order. Since checkpoints are processed left to right, query `x = pos[i]` is also monotone. This allows a deque-based lower hull with pointer advancement, avoiding binary search and keeping the total cost `O(n)` after prefix computation.

6. **Apply fee handling carefully**  
   After querying the hull, compute  
   `dp[i] = C * pos[i]^2 + bestLine(pos[i]) + (i == n - 1 ? 0 : fee[i])`.  
   Then insert the line derived from this `dp[i]`. The invariant is that future states see the fully settled minimum cost to reach `i`.

## 📊 Worked Example
Use Example 2: `n=4`, `dist=[0,1,1,1]`, `fee=[0,100,100,0]`, `C=2`

Prefix positions: `pos = [0,1,2,3]`

| i | pos[i] | Best predecessor | Raw leg cost | fee add | dp[i] |
|---|--------|------------------|--------------|---------|-------|
| 0 | 0 | start | 0 | 0 | 0 |
| 1 | 1 | 0 | `2*(1-0)^2 = 2` | 100 | 102 |
| 2 | 2 | 0 | `2*(2-0)^2 = 8` | 100 | 108 |
| 3 | 3 | 0 | `2*(3-0)^2 = 18` | 0 | 18 |

Hull view:
1. Insert line from checkpoint `0`: slope `0`, intercept `0`.
2. Query at `x=1` and `x=2`: direct flight from `0` still beats routes through expensive recharge stops.
3. Even after inserting lines for checkpoints `1` and `2`, querying at `x=3` still favors checkpoint `0`.

Result: direct route `0 -> 3`, total cost `18`.

## ⏱ Complexity Analysis
### Time Complexity
`O(n)` with a monotonic deque-based Convex Hull Trick. Each checkpoint generates one line insertion and one query; each line is added once and removed at most once from the hull. At `10^6` scale this remains practical; at `10^9`, memory and input volume dominate before algorithmic cost does.

### Space Complexity
`O(n)` for prefix positions, DP values if stored, and the hull. The hull itself is linear in the worst case. DP storage can be reduced if only the current value is needed for line construction, but retaining arrays usually improves debuggability and traceability.

## 💡 Key Takeaways
- If a DP transition is `min over j` and includes a quadratic term like `(x_i - x_j)^2`, try expanding it and looking for a linear form suitable for Convex Hull Trick.
- Monotone prefix sums plus left-to-right processing are a strong signal that the deque variant of CHT may replace `O(log n)` queries with amortized `O(1)`.
- Be explicit about fee semantics: intermediate recharge stops pay `fee[i]`, but checkpoint `0` and checkpoint `n - 1` do not.
- Use 64-bit arithmetic everywhere; `pos[i]^2 * batteryCost` can overflow 32-bit types long before the final answer does.
- The production lesson is broader than this problem: once a cost model becomes convex, the right abstraction is often “optimize the transition geometry,” not “micro-optimize the loop.”

## 🚀 Variations & Further Practice
- Add a maximum leg-length constraint. The DP still uses CHT, but candidate predecessors now expire by distance window, forcing hull eviction logic tied to feasibility, not just dominance.
- Replace the quadratic battery model with piecewise-linear or piecewise-convex costs. The recurrence remains optimization over prior states, but the data structure shifts from simple line hulls to Li Chao trees or multi-hull handling.
- Require exactly `k` recharge stops. This introduces an additional DP dimension, turning a single optimized hull into layered hulls and making state management, not algebra alone, the hard part.