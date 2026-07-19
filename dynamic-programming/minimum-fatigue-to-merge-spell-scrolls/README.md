# Minimum Fatigue to Merge Spell Scrolls

**Difficulty:** Hard &nbsp;|&nbsp; **Topic:** Dynamic Programming &nbsp;|&nbsp; **Tags:** Dynamic Programming, Interval DP, Prefix Sum

---

## 🗂 Problem Overview
Given an array `energy`, repeatedly merge exactly `k` consecutive scrolls into one. Each merge costs the sum of that block, and the merged scroll keeps that sum as its new energy. Return the minimum total fatigue required to reduce the row to one scroll, or `-1` if no valid sequence exists. The non-trivial part is that every local merge changes future merge costs, so only a global optimization over intervals works.

## 🌍 Engineering Impact
This pattern shows up anywhere local aggregation decisions reshape future cost: compaction in LSM storage engines, batch fusion in streaming pipelines, hierarchical job coalescing, and expression-tree reassociation in compilers. At scale, greedy heuristics often look attractive because they minimize immediate work, but they can amplify downstream cost or make later reductions impossible under structural constraints. Interval DP matters when operations are order-sensitive, contiguous, and state-transforming. It enables deterministic global optimization under fixed merge rules, which is exactly what you need when cost accumulation is path-dependent and operational legality depends on intermediate structure, not just final state.

## 🔍 Problem Statement
You are given `energy`, where `energy[i]` is the energy of the `i`-th scroll, and an integer `k`. In one operation, you may merge any contiguous block of exactly `k` current scrolls. That operation costs the sum of those `k` energies, and the merged scroll replaces the block with energy equal to that same sum.

Return the minimum total fatigue needed to end with exactly one scroll. If no sequence of valid merges can reduce the array to one scroll, return `-1`.

Constraints:
- `1 <= energy.length <= 30`
- `1 <= energy[i] <= 10^4`
- `2 <= k <= 30`

Examples:
- `energy = [3,2,4,1], k = 2` → `20`
- `energy = [3,2,4,1], k = 3` → `-1`
- `energy = [3,5,1,2,6], k = 3` → `25`

The key structural constraint is feasibility: each merge reduces the number of scrolls by `k - 1`, so ending at one scroll is possible only when `(n - 1) % (k - 1) == 0`.

## 🪜 How to Solve This
1. Read the operation carefully → merges are **contiguous** and always of **exactly `k` items**. That rules out greedy sorting or heap-based merging.
2. Notice each merge reduces pile count by `k - 1` → before optimizing cost, check feasibility: if `(n - 1) % (k - 1) != 0`, the answer is immediately `-1`.
3. Costs depend on merge order → a cheap local merge can create an expensive future scroll, so this is not a myopic optimization problem.
4. Contiguous subarrays plus “best way to reduce this segment” is the classic signal for **Interval DP**.
5. Define `dp[i][j]` as the minimum cost to reduce `energy[i..j]` as much as legally possible. We do not force it to become one scroll unless the segment length allows it.
6. To combine intervals, split at valid boundaries only: because merges happen in groups of `k`, we advance split points by `k - 1`.
7. When a segment can be reduced to exactly one scroll, add its total sum once. Prefix sums make that O(1).
8. Build answers bottom-up by increasing interval length until `dp[0][n-1]` is known.

## 🧩 Algorithm Walkthrough
1. **Feasibility check**  
   A merge removes `k - 1` scrolls. Starting from `n`, reaching `1` requires `n - 1` removals, so `(n - 1) % (k - 1) == 0` is necessary and sufficient. If it fails, return `-1`.

2. **Prefix Sum preprocessing**  
   Build `prefix`, where `prefix[t + 1] = prefix[t] + energy[t]`. This gives interval sum `sum(i, j) = prefix[j + 1] - prefix[i]` in O(1). That matters because every successful full merge of an interval adds exactly its total energy.

3. **State definition: Interval DP**  
   Let `dp[i][j]` be the minimum cost to reduce subarray `i..j` into the minimum number of scrolls achievable under the merge rule. Initialize `dp[i][i] = 0`; one scroll needs no work.

4. **Transition over interval splits**  
   For each interval `[i, j]`, try partition points `m = i, i + (k - 1), i + 2(k - 1), ... , j - 1`.  
   Update: `dp[i][j] = min(dp[i][m] + dp[m + 1][j])`.  
   The step size is not an optimization trick; it preserves legal pile-count alignment.

5. **Add merge cost only when one pile is possible**  
   After combining subproblems, if `(len - 1) % (k - 1) == 0`, then interval `[i, j]` can collapse to one scroll, so add `sum(i, j)` to `dp[i][j]`.

6. **Bottom-up evaluation order**  
   Compute by increasing interval length. The invariant is: before evaluating `dp[i][j]`, all smaller intervals needed by its splits are already optimal.

This is **Interval DP with Prefix Sums**: the right abstraction because legality and cost are both functions of contiguous subproblems and their composition.

## 📊 Worked Example
Example: `energy = [3, 5, 1, 2, 6]`, `k = 3`

Feasibility: `(5 - 1) % (3 - 1) = 0`, so a full merge is possible.

| Interval | Best split cost | Can become 1 pile? | Added sum | Final `dp` |
|---|---:|---:|---:|---:|
| `[1,2,3] = [5,1,2]` | `0` | Yes | `8` | `8` |
| `[0,1,2] = [3,5,1]` | `0` | Yes | `9` | `9` |
| `[2,3,4] = [1,2,6]` | `0` | Yes | `9` | `9` |
| `[0..3] = [3,5,1,2]` | `min(dp[0][0]+dp[1][3], dp[0][2]+dp[3][3]) = min(8,9)` | No | `0` | `8` |
| `[1..4] = [5,1,2,6]` | `min(9,8)` | No | `0` | `8` |
| `[0..4] = [3,5,1,2,6]` | `dp[0][0]+dp[1][4] = 8` or `dp[0][2]+dp[3][4] = 9` | Yes | `17` | `25` |

Optimal sequence: merge `[5,1,2]` → cost `8`, then merge `[3,8,6]` → cost `17`, total `25`.

## ⏱ Complexity Analysis

### Time Complexity
`O(n^3 / (k - 1))`, typically stated as `O(n^3)`. There are `O(n^2)` intervals, and each interval tries `O(n / (k - 1))` split points. With `n <= 30`, this is trivial in practice. At `10^6` or `10^9` scale, cubic interval DP is categorically infeasible; the problem structure, not constant factors, is the limit.

### Space Complexity
`O(n^2)` for the DP table, plus `O(n)` for prefix sums. The DP matrix owns essentially all memory. Space cannot be reduced to linear without losing needed cross-interval state, because transitions depend on many overlapping subranges, not just adjacent lengths.

## 💡 Key Takeaways
- If the problem asks for minimum cost over **contiguous merges** and local choices affect future costs, think **Interval DP** before considering greedy strategies.
- If every operation reduces item count by a fixed amount, check the **count-modulo feasibility condition** first; it often eliminates impossible cases immediately.
- The merge-cost sum is added **only when an interval can legally collapse to one pile**; adding it at every split overcounts.
- Split points must advance by `k - 1`, not by `1`, or you combine subproblems with incompatible residual pile counts and get invalid states.
- In production systems, this is the broader lesson: when transformations are order-sensitive and stateful, optimize over the full composition graph, not the cheapest immediate operation.

## 🚀 Variations & Further Practice
- **Generalized merge arity:** allow a set of valid merge sizes instead of one fixed `k`. Harder because feasibility and state transitions are no longer governed by a single modulo invariant.
- **Circular scroll layout:** scrolls form a ring, so intervals wrap around. Harder because you must duplicate the array or evaluate all cut positions while preserving interval semantics.
- **Weighted merge penalties:** merge cost becomes `sum + overhead(length, position, depth)`. Harder because prefix sums alone no longer capture all merge economics, and the DP state may need extra dimensions.