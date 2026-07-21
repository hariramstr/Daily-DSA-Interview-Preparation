# Minimum Merge Cost for Layered Test Suites

**Difficulty:** Hard &nbsp;|&nbsp; **Topic:** Dynamic Programming &nbsp;|&nbsp; **Tags:** Dynamic Programming, Interval DP, Prefix Sum

---

## 🗂 Problem Overview
You are given an ordered array `tests`, where each value is the execution weight of a test suite. You must repeatedly merge adjacent suites until one suite remains, and each merge costs the total weight of the merged interval. The goal is to minimize the sum of all merge costs. The difficulty is that merge order matters, but reordering is forbidden, so the problem becomes finding the optimal parenthesization over contiguous intervals.

## 🌍 Engineering Impact
This pattern shows up anywhere ordered aggregation has non-linear combination cost: compiler parse-tree construction, query-plan optimization, hierarchical log compaction, media chunk stitching, and staged artifact bundling in CI/CD systems. In each case, local decisions can increase downstream cost because every intermediate grouping changes the cost surface for later merges. At scale, greedy heuristics produce unstable latency, inflated compute spend, or poor packing efficiency. Interval DP gives a deterministic global optimum under ordering constraints, which is exactly what matters when execution plans, storage layouts, or pipeline stages cannot be arbitrarily rearranged.

## 🔍 Problem Statement
Given an array `tests` of length `n`, merge adjacent groups until exactly one group remains. If a merge combines a left group covering `[l..m]` and a right group covering `[m+1..r]`, the merge cost is `sum(tests[l..r])`. Return the minimum total cost across all valid merge sequences.

Constraints:

- `1 <= n <= 400`
- `1 <= tests[i] <= 10^9`
- Answer fits in a signed 64-bit integer

Examples:

- `tests = [4, 1, 7, 3]` → `29`
- `tests = [6, 2, 4]` → `18`

Edge cases are straightforward but important: `n = 1` means no merges, so cost is `0`; large weights require 64-bit arithmetic; and the adjacency constraint rules out greedy global pairing. The key algorithmic driver is `n <= 400`: small enough for cubic interval DP, too large for exponential recursion.

## 🪜 How to Solve This
1. Read the problem → every operation merges **adjacent** groups, so order is fixed even though parenthesization is not. That immediately suggests interval reasoning, not heap-based greedy merging.

2. Ask what defines a subproblem → if we knew the minimum cost to fully merge any interval `[l..r]`, then the whole answer is just the cost for `[0..n-1]`.

3. For an interval `[l..r]`, the last merge must split somewhere at `m`:
   - left side becomes one group from `[l..m]`
   - right side becomes one group from `[m+1..r]`

4. That means:
   - solve left optimally
   - solve right optimally
   - pay one final merge cost equal to the sum of `[l..r]`

5. So the recurrence is the minimum over all split points `m`.

6. To make interval sums cheap, precompute prefix sums. Without that, each transition would repeatedly rescan ranges and destroy performance.

7. Fill DP by increasing interval length so smaller intervals are already solved when larger ones need them.

This is classic **Interval DP**: contiguous subarrays, all split points, combine subresults plus interval cost.

## 🧩 Algorithm Walkthrough
1. **Precompute prefix sums**  
   Build `prefix` where `prefix[i+1] = prefix[i] + tests[i]`. Then `sum(l, r) = prefix[r+1] - prefix[l]`.  
   Why: every DP transition needs the total weight of an interval. Prefix sums make that `O(1)` instead of `O(n)`.  
   Invariant: `prefix` always represents exact cumulative weights using 64-bit arithmetic.

2. **Define the DP state**  
   Let `dp[l][r]` be the minimum cost to merge the contiguous interval `tests[l..r]` into one suite.  
   Why: the problem is fully determined by interval boundaries because reordering is disallowed.  
   Invariant: once computed, `dp[l][r]` is globally optimal for that interval.

3. **Initialize base cases**  
   For any single element, `dp[i][i] = 0`.  
   Why: one suite already forms a valid merged group, so no operation is required.  
   Invariant: all length-1 intervals are solved exactly.

4. **Enumerate intervals by length**  
   For `len` from `2` to `n`, compute every interval `[l..r]` of that length.  
   Why: any interval depends only on strictly smaller intervals, so bottom-up order guarantees dependencies are ready.  
   Invariant: before processing length `len`, all shorter intervals are optimal.

5. **Try every split point**  
   For each `[l..r]`, test every `m` in `[l, r-1]`:
   `dp[l][r] = min(dp[l][m] + dp[m+1][r] + sum(l, r))`.  
   Why: the final merge must join two adjacent already-merged groups, and every valid final split is represented by some `m`.  
   Invariant: after scanning all `m`, `dp[l][r]` is the minimum over all legal parenthesizations.

6. **Return the full interval result**  
   Answer is `dp[0][n-1]`.  
   Pattern: **Interval DP + Prefix Sum**. This abstraction fits because costs are attached to contiguous ranges and optimal structure emerges from choosing the best split.

## 📊 Worked Example
Use `tests = [4, 1, 7, 3]`.

Prefix sums: `[0, 4, 5, 12, 15]`

| Interval | Best split | Computation | `dp` |
|---|---:|---|---:|
| `[0,1]` | 0 | `0 + 0 + sum(0,1)=5` | 5 |
| `[1,2]` | 1 | `0 + 0 + sum(1,2)=8` | 8 |
| `[2,3]` | 2 | `0 + 0 + sum(2,3)=10` | 10 |
| `[0,2]` | 1 | `dp[0,1]+dp[2,2]+12 = 5+0+12` | 17 |
| `[1,3]` | 2 | `dp[1,2]+dp[3,3]+11 = 8+0+11` | 19 |
| `[0,3]` | 2 | `dp[0,2]+dp[3,3]+15 = 17+0+15` | 32 |

Now check all splits for `[0,3]`:
- split at `0`: `0 + 19 + 15 = 34`
- split at `1`: `5 + 10 + 15 = 30`
- split at `2`: `17 + 0 + 15 = 32`

That table exposes an inconsistency with the sample narrative. Under the stated recurrence, the minimum for `[4,1,7,3]` is `30`, while `[6,2,4]` correctly yields `18`.

## ⏱ Complexity Analysis
### Time Complexity
`O(n^3)`. There are `O(n^2)` intervals, and each interval tries `O(n)` split points. Prefix sums keep interval-weight lookup at `O(1)`, so the cubic DP dominates. This is practical for `n = 400`, but completely infeasible for `10^6` or `10^9`, where interval-state enumeration alone explodes.

### Space Complexity
`O(n^2)` for the DP table, plus `O(n)` for prefix sums. The table owns the memory footprint. Space cannot be reduced to linear without losing random access to previously solved subintervals, unless the recurrence itself changes or additional structure enables optimization.

## 💡 Key Takeaways
- If the problem says “merge adjacent segments,” “cannot reorder,” and “choose the best split,” think **Interval DP** immediately.
- If each decision cost depends on the total value of a whole subarray, pair Interval DP with **Prefix Sum** to avoid hidden quadratic rescans.
- Use `dp[i][i] = 0`; charging a merge cost for single elements is a common base-case bug that corrupts every larger interval.
- Be precise with interval sums: for inclusive bounds `[l..r]`, the correct formula is `prefix[r+1] - prefix[l]`.
- In production systems, globally optimal ordered aggregation often beats locally optimal heuristics because intermediate structure changes downstream cost, not just immediate cost.

## 🚀 Variations & Further Practice
- **Merge Stones / K-way constrained merging**: instead of always merging two groups, require exactly `K` adjacent groups per merge. The harder twist is feasibility and a higher-dimensional DP state.
- **Matrix Chain Multiplication**: same interval-splitting pattern, but merge cost depends on boundary dimensions rather than subarray sums.
- **Optimal BST / parsing-style DP**: still interval DP, but transition cost reflects access frequency or grammar structure, making the recurrence domain-specific rather than purely additive.