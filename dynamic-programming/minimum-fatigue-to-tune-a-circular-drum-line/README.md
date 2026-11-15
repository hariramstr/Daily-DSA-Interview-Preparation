# Minimum Fatigue to Tune a Circular Drum Line

**Difficulty:** Hard &nbsp;|&nbsp; **Topic:** Dynamic Programming &nbsp;|&nbsp; **Tags:** dynamic-programming, interval-dp, circular-array

---

## 🗂 Problem Overview
You are given two length-`n` arrays, `current` and `target`, for drums arranged on a circle. Each drum must be finalized exactly once, and the order matters because the last drum chosen inside any active contiguous block pays penalties based on the nearest still-unfinalized neighbors in that block. The goal is to minimize total fatigue. The hard part is circular adjacency combined with interval-dependent costs, which rules out greedy local decisions.

## 🌍 Engineering Impact
This pattern shows up anywhere local decisions change the boundary conditions of what remains: query-plan optimization in compilers, segmented merge scheduling in storage engines, hierarchical batch finalization in streaming pipelines, and dependency collapse in build systems. The cost of “resolving one item last” depends on which unresolved neighbors are still exposed. At scale, greedy heuristics produce unstable tail behavior because they optimize immediate cost instead of future boundary shape. Interval DP matters when the state is not just “what is left,” but “what contiguous region remains and what its boundaries imply.” The circular variant is especially common in ring topologies, token schedulers, and cyclic resource layouts.

## 🔍 Problem Statement
There are `n` drums in a circle, with `1 <= n <= 300`. Drum `i` starts at `current[i]` and must end at `target[i]`, where all values are in `[0, 10^9]`. Finalizing drum `k` always incurs base cost `abs(current[k] - target[k])`. In addition, if `k` is finalized as the last drum within a current contiguous block `[l..r]`, it may pay a left penalty to the nearest still-unfinalized drum on the left within that block and a right penalty to the nearest still-unfinalized drum on the right.

Return the minimum total fatigue over all valid orders.

Examples:

- `current = [5,1,4], target = [3,2,6]` → `7`
- `current = [8,8,8,8], target = [1,10,1,10]` → `23`

The key constraint is `n <= 300`: large enough that exponential search is impossible, small enough that `O(n^3)` interval DP is the intended regime.

## 🪜 How to Solve This
1. Read the cost formula → notice `abs(current[i] - target[i])` is unavoidable for every drum, regardless of order. Strip that out first.
2. What remains is purely structural: extra fatigue depends on which target values are adjacent when a drum is finalized.
3. The phrase “contiguous block” is the tell. If the remaining unresolved drums always form intervals after some choices, this is interval DP territory.
4. For a linear array, define a DP over subarrays: the minimum extra fatigue needed to finalize everything in `target[l..r]`.
5. Ask what the last operation inside `[l..r]` looks like. If drum `k` is finalized last, then `[l..k-1]` and `[k+1..r]` must already be solved independently.
6. That gives the recurrence: solve left interval + solve right interval + penalty paid by `k` from the surviving interval boundaries.
7. The array is circular, so a single linear DP is not enough. Break the ring at every possible position, solve the resulting length-`n` linear instance, and take the minimum.
8. `n = 300` makes `O(n^4)` too expensive but `O(n^3)` acceptable, so the recurrence must be implemented carefully.

## 🧩 Algorithm Walkthrough
1. **Separate mandatory and optimizable cost.**  
   Compute `base = Σ abs(current[i] - target[i])`. This is order-independent, so optimization only concerns neighbor penalties. This reduces the problem to minimizing extra structural fatigue.

2. **Linearize the circular problem.**  
   Duplicate `target` into length `2n`. For each start index `s` in `[0, n-1]`, consider the linear segment `target[s..s+n-1]` as one possible break of the circle. This preserves original circular adjacency while letting us run interval DP on a line.

3. **Define the interval DP state.**  
   Let `dp[l][r]` be the minimum extra fatigue to finalize all drums in the linear interval `[l..r]`. Pattern: **Interval DP**. The abstraction fits because after choosing the last finalized drum in an interval, the remaining work splits into two independent subintervals.

4. **Transition on the last finalized drum.**  
   For every `k in [l..r]`, treat `k` as finalized last. Then:
   - left subproblem cost: `dp[l][k-1]` if `k > l`, else `0`
   - right subproblem cost: `dp[k+1][r]` if `k < r`, else `0`
   - left penalty: `abs(target[k] - target[l])` if `k > l`, else `0`
   - right penalty: `abs(target[k] - target[r])` if `k < r`, else `0`

   So:
   `dp[l][r] = min over k { dp[l][k-1] + dp[k+1][r] + (k>l ? |t[k]-t[l]| : 0) + (k<r ? |t[k]-t[r]| : 0) }`

   Correctness invariant: `dp[l][r]` always represents the optimal extra cost for exactly that unresolved interval, independent of decisions outside it.

5. **Fill by increasing interval length.**  
   Base case: `dp[i][i] = 0`, because a single drum has no unresolved neighbors inside its block. Process lengths from `2` to `n`.

6. **Take the best circular break.**  
   For each start `s`, evaluate the linear interval `[s, s+n-1]`. The answer is `base + min_s dp[s][s+n-1]`.

## 📊 Worked Example
Use `current = [5,1,4]`, `target = [3,2,6]`.

Base cost:
`|5-3| + |1-2| + |4-6| = 2 + 1 + 2 = 5`

Try break at index `0`, so linear target is `[3,2,6]`.

| Interval | Best last `k` | Extra cost |
|---|---:|---:|
| `[0,0]` = `[3]` | 0 | 0 |
| `[1,1]` = `[2]` | 1 | 0 |
| `[2,2]` = `[6]` | 2 | 0 |
| `[0,1]` = `[3,2]` | 0 or 1 | `|3-2| = 1` |
| `[1,2]` = `[2,6]` | 1 or 2 | `|2-6| = 4` |
| `[0,2]` = `[3,2,6]` | 0 | `dp[1,2] + |3-6| = 4 + 3 = 7` |
|  | 1 | `dp[0,0]+dp[2,2]+|2-3|+|2-6| = 5` |
|  | 2 | `dp[0,1] + |6-3| = 1 + 3 = 4` |

Best extra cost for this break is `4`. Checking all circular breaks gives minimum extra cost `2`, so total answer is `5 + 2 = 7`.

## ⏱ Complexity Analysis
### Time Complexity
For each of `n` break positions, the naive linear interval DP is `O(n^3)` because there are `O(n^2)` intervals and each scans all possible last positions `k`. A straightforward implementation is therefore `O(n^4)`, which is too slow. The practical solution is to run the DP once on the duplicated array and compute all length-`<= n` intervals together in `O(n^3)` total.

### Space Complexity
`O(n^2)` for the interval DP table over the duplicated target array window structure. The table dominates memory. Space can be reduced only marginally because transitions require access to many shorter subintervals; this is not a rolling-array-friendly DP.

## 💡 Key Takeaways
- If a problem says “choose the last element in a contiguous segment” and subproblems split left/right, it is almost certainly interval DP.
- If the structure is circular, expect either array duplication or “try every break” layered on top of the core linear DP.
- The penalty for finalizing `k` last depends on interval boundaries `l` and `r`, not immediate original neighbors; using `k-1` and `k+1` is a correctness bug.
- Be precise about base cases: `dp[i][i] = 0` for extra cost, because the unavoidable retuning cost was already factored out.
- The transferable design insight is to separate invariant cost from order-dependent cost first; that often collapses a messy optimization problem into a clean state model.

## 🚀 Variations & Further Practice
- **Weighted neighbor exposure:** each left/right penalty is multiplied by a drum-specific weight. Same interval DP shape, but transition cost becomes asymmetric and more error-prone.
- **Exactly `m` phases allowed:** add a phase-count dimension to the interval DP. Harder because the split now allocates budget across subintervals.
- **2D cyclic layout instead of a ring:** contiguous blocks become regions, and interval DP no longer applies directly; this forces a different decomposition strategy or approximation.