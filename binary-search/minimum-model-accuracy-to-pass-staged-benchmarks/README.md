# Minimum Model Accuracy to Pass Staged Benchmarks

**Difficulty:** Hard &nbsp;|&nbsp; **Topic:** Binary Search &nbsp;|&nbsp; **Tags:** Binary Search, Prefix Sum, Dynamic Programming

---

## 🗂 Problem Overview
Given stage difficulties, stage weights, and a limit of at most `k` contiguous groups, find the minimum integer threshold `A` such that after mapping each stage to `+weights[i]` when `difficulties[i] <= A` and `-weights[i]` otherwise, the array can be partitioned into at most `k` non-empty contiguous segments, each with sum at least zero. The challenge is that feasibility depends on both thresholding and segmentation, so brute-force over thresholds and partitions is intractable at `n = 2e5`.

## 🌍 Engineering Impact
This pattern shows up when a global control parameter must satisfy local contiguous quality constraints: rollout thresholds across time windows, search-ranking quality gates across query buckets, streaming pipeline health over shard ranges, or SLA certification over staged workloads. The production issue is rarely “is the total good enough?”; it is “can every operational slice be made acceptable under limited grouping or batching?” Without the right approach, teams reach for quadratic DP or repeated rescans that collapse under large benchmark suites, long event streams, or high-cardinality evaluation pipelines. Binary search over a monotone control variable plus linear feasibility checking is the scalable design.

## 🔍 Problem Statement
You are given:

- `difficulties[i]`: difficulty score of stage `i`
- `weights[i]`: contribution weight of stage `i`
- `k`: maximum number of contiguous non-empty segments allowed

For a chosen integer threshold `A`, transform the array into:

- `+weights[i]` if `difficulties[i] <= A`
- `-weights[i]` otherwise

`A` is feasible if the transformed array can be split into at most `k` contiguous non-empty segments and every segment has sum `>= 0`. Return the minimum feasible `A`, or `-1` if none exists.

Constraints:

- `1 <= n <= 200000`
- `1 <= k <= n`
- `1 <= difficulties[i], weights[i] <= 10^9`

Examples:

- `difficulties = [4,2,7,3,6], weights = [5,2,4,3,6], k = 2` → `4`
- `difficulties = [8,9,10], weights = [3,4,5], k = 3` → `10`

The key constraint is `n = 2e5`, which rules out per-threshold quadratic partition DP.

## 🪜 How to Solve This
1. Read the threshold condition → the transformed value of each stage only changes when `A` crosses a difficulty value. That strongly suggests binary search on `A`, or on sorted unique difficulties.

2. For a fixed `A`, ignore the original ML framing. The real subproblem is: can this signed array be partitioned into at most `k` contiguous non-empty segments, each with non-negative sum?

3. Rephrase with prefix sums. A segment `[l+1..r]` is valid iff `prefix[r] >= prefix[l]`. So we need a chain of indices from `0` to `n` using at most `k` segments where prefix sums never decrease across chosen cut points.

4. That becomes dynamic programming on prefixes: from each reachable cut position `j`, any later `i` with `prefix[i] >= prefix[j]` can end one more valid segment.

5. The expensive part is finding, for each `i`, the best reachable prior cut among all smaller-or-equal prefix sums. That is a prefix-maximum query over compressed prefix sums.

6. Once feasibility is `O(n log n)`, wrap it in binary search over sorted unique difficulties for `O(n log^2 n)` or over value range if desired. The monotonicity is straightforward: increasing `A` only flips `-w` to `+w`, never the reverse.

## 🧩 Algorithm Walkthrough
1. **Binary Search on Answer**  
   Use the sorted unique values of `difficulties` as candidate thresholds. If threshold `A` is feasible, any larger threshold is also feasible because some negative entries may become positive, which can only improve segment sums. This monotonicity makes **Binary Search** the correct outer pattern.

2. **Build the Transformed Prefix Sums**  
   For a fixed `A`, define `val[i] = +weights[i]` if `difficulties[i] <= A`, else `-weights[i]`. Compute prefix sums `pref[0..n]`, where `pref[0] = 0`. A segment `(j, i]` is valid exactly when `pref[i] - pref[j] >= 0`, i.e. `pref[i] >= pref[j]`.

3. **Define the DP State**  
   Let `dp[i]` be the minimum number of valid segments needed to partition the prefix ending at position `i`, or infinity if impossible. Transition:  
   `dp[i] = 1 + min(dp[j])` over all `j < i` with `pref[j] <= pref[i]`.  
   This is a classic **Prefix Sum + Dynamic Programming** formulation.

4. **Accelerate the Transition**  
   Compress all prefix sums. Maintain a Fenwick tree or segment tree where each compressed prefix rank stores the minimum `dp[j]` seen so far. Query the minimum `dp[j]` among all ranks `<= rank(pref[i])`, then set `dp[i]` accordingly. This preserves the invariant: after processing index `i`, the data structure contains optimal DP values for all prior cut positions.

5. **Check Feasibility**  
   If `dp[n] <= k`, threshold `A` is feasible. Otherwise it is not. Because every segment is non-empty, transitions only use earlier positions, so the DP respects the partition constraints exactly.

## 📊 Worked Example
Take `difficulties = [4,2,7,3,6]`, `weights = [5,2,4,3,6]`, `k = 2`, and test `A = 4`.

Transformed array: `[+5, +2, -4, +3, -6]`

| i | val[i] | pref[i] | best prior `dp[j]` with `pref[j] <= pref[i]` | dp[i] |
|---|--------|---------|-----------------------------------------------|-------|
| 0 | —      | 0       | —                                             | 0     |
| 1 | 5      | 5       | `dp[0]=0`                                     | 1     |
| 2 | 2      | 7       | min of `dp[0], dp[1]` = 0                     | 1     |
| 3 | -4     | 3       | `dp[0]=0`                                     | 1     |
| 4 | 3      | 6       | min reachable = 0                             | 1     |
| 5 | -6     | 0       | `dp[0]=0`                                     | 1     |

`dp[5] = 1`, so the whole array can be one valid segment. Since `1 <= k`, `A = 4` is feasible. Testing smaller thresholds via binary search shows `A = 3` is infeasible, so the answer is `4`.

## ⏱ Complexity Analysis
### Time Complexity
For one feasibility check, building prefix sums is `O(n)`, coordinate compression is `O(n log n)`, and each DP query/update is `O(log n)`, so total `O(n log n)`. Binary searching over `m` unique difficulties gives `O(n log n log m)`, typically written as `O(n log^2 n)`. This is viable at `2e5`; anything quadratic is not.

### Space Complexity
`O(n)` space for the transformed prefix sums, compressed coordinates, DP array, and Fenwick/segment tree. You can reduce some arrays by streaming the DP and prefix construction, but asymptotically the coordinate set still keeps the solution linear-space.

## 💡 Key Takeaways
- If the problem asks for the minimum threshold and feasibility only improves as the threshold increases, treat it as binary search on the answer.
- If segment validity depends on subarray sum being non-negative, prefix sums usually convert the condition into an order relation between cut points.
- The DP transition is over `j < i` with `pref[j] <= pref[i]`; forgetting the strict index ordering leads to illegal zero-length segments.
- Use 64-bit integers everywhere: prefix sums can reach `2e5 * 1e9`, far beyond 32-bit range.
- The production-grade insight is to separate a monotone control parameter from a high-volume feasibility kernel, then optimize the kernel with the right indexed aggregate structure.

## 🚀 Variations & Further Practice
- Require **exactly** `k` segments instead of at most `k`; the twist is that feasibility no longer follows directly from minimizing segment count, and the DP state must track segment counts more explicitly.
- Allow **online updates** to difficulties or weights with repeated threshold queries; the harder part is maintaining feasibility under dynamic changes rather than recomputing prefix-order DP from scratch.
- Replace non-negative segment sum with segment sum at least `T`; the conceptual change is small, but every transition becomes `pref[i] - pref[j] >= T`, shifting the prefix-query boundary and testing whether the abstraction still holds cleanly.