# Minimum Cost to Compress Event Timeline

**Difficulty:** Hard &nbsp;|&nbsp; **Topic:** Dynamic Programming &nbsp;|&nbsp; **Tags:** dynamic-programming, partition-dp, array

---

## 🗂 Problem Overview
Given an ordered array `events`, split it into exactly `k` non-empty contiguous blocks. Each block costs `fixedCost + d²`, where `d` is the number of distinct event types inside that block. Return the minimum total cost over all valid partitions.

The challenge is that block cost is not additive per element and not locally monotonic: extending a block may or may not increase distinct count, while every extra split pays another `fixedCost`. With `n` up to 2000, brute-force partition enumeration is infeasible.

## 🌍 Engineering Impact
This pattern shows up anywhere ordered data must be segmented under a non-linear encoding cost: log compaction, telemetry rollups, columnar storage chunking, stream window materialization, compiler IR regioning, and search/index shard packing. The production issue is not just compression ratio; it is choosing boundaries when per-segment metadata overhead competes with segment heterogeneity.

Without dynamic programming, teams often ship heuristics that look reasonable on local samples but fail under skewed distributions, long-tail keys, or bursty traffic. A correct partition-DP formulation gives predictable optimality, makes cost models explicit, and creates a clean seam for later replacing `distinct²` with richer storage or compute pricing functions.

## 🔍 Problem Statement
You are given an integer array `events` of length `n`, where `events[i]` is the type of the `i`-th event in timeline order. Partition the array into exactly `k` contiguous, non-empty blocks. For a block covering indices `l..r`, its cost is:

`cost(l, r) = fixedCost + (distinct(events[l..r]))²`

Return the minimum total cost across all valid partitions.

Constraints:

- `1 <= n <= 2000`
- `1 <= events[i] <= 10^9`
- `1 <= k <= min(100, n)`
- `0 <= fixedCost <= 10^6`

Examples:

- `events = [1,2,1,3,2], k = 2, fixedCost = 4` → minimum total cost is **16**
- `events = [5,5,5,6,7,6], k = 3, fixedCost = 2` → minimum total cost is **12**

The decisive constraint is `n = 2000`: exponential partition search is impossible, but `O(k·n²)` dynamic programming is practical.

## 🪜 How to Solve This
1. Read the problem → this is a **partitioning** problem over an ordered array. Order is fixed, so no sorting or regrouping tricks apply.

2. Notice the objective → total cost is the sum of costs of contiguous segments. That is the classic signal for **partition DP**: “best answer for first `i` elements using `p` blocks”.

3. Define the transition → if the last block starts at `j`, then:
   `dp[p][i] = min(dp[p-1][j] + cost(j, i-1))`
   for all valid `j < i`.

4. The hard part is segment cost → `cost(j, i-1)` depends on the number of distinct values in that subarray. Recomputing distinct counts inside every transition would be too slow.

5. Precompute all segment costs → for each left boundary `l`, extend `r` rightward, maintain a frequency map and current distinct count, then store `segmentCost[l][r]`.

6. Once segment costs are available in `O(n²)`, the DP becomes straightforward `O(k·n²)` minimization.

7. This works because every optimal partition has an optimal prefix. That optimal-substructure property is exactly why DP is the right tool here.

## 🧩 Algorithm Walkthrough
1. **Precompute segment costs with incremental distinct counting**  
   Pattern: **array precomputation + hash-frequency tracking**.  
   For each start index `l`, scan `r = l..n-1`, maintain a map `freq` and an integer `distinct`. When `events[r]` is first seen for this `l`, increment `distinct`. Then set `segmentCost[l][r] = fixedCost + distinct * distinct`.  
   Invariant: after processing `(l, r)`, `distinct` equals the number of unique values in `events[l..r]`.

2. **Define DP state**  
   Pattern: **partition DP**.  
   Let `dp[p][i]` be the minimum cost to partition the first `i` events (`events[0..i-1]`) into exactly `p` non-empty blocks. This prefix-based indexing avoids boundary ambiguity.

3. **Initialize base cases**  
   Set all states to infinity. `dp[0][0] = 0` because zero elements split into zero blocks costs nothing. `dp[0][i > 0]` is invalid.

4. **Apply the transition**  
   For each `p` from `1` to `k`, and each `i` from `p` to `n`, try all last-cut positions `j` from `p-1` to `i-1`:  
   `dp[p][i] = min(dp[p][i], dp[p-1][j] + segmentCost[j][i-1])`  
   Why correct: every valid `p`-block partition of the first `i` elements ends with exactly one last block `j..i-1`, and the prefix `0..j-1` must itself be optimally partitioned into `p-1` blocks.

5. **Return the final answer**  
   The result is `dp[k][n]`.  
   Invariant at completion: each DP state stores the true optimum over all legal partitions of that prefix and block count.

## 📊 Worked Example
Use `events = [5,5,5,6,7,6]`, `k = 3`, `fixedCost = 2`.

First, relevant segment costs:

| Segment | Distinct | Cost |
|---|---:|---:|
| `[5]` | 1 | 3 |
| `[5,5]` | 1 | 3 |
| `[5,5,5]` | 1 | 3 |
| `[6,7]` | 2 | 6 |
| `[7,6]` | 2 | 6 |
| `[6]` | 1 | 3 |

Now DP over prefixes (`i` = number of events consumed):

1. `dp[1][1..6]` is just one block over each prefix:  
   `[3,3,3,6,11,11]`

2. `dp[2][4]` checks splits of `[5,5,5,6]`:  
   best is `dp[1][3] + cost(3,3) = 3 + 3 = 6`

3. `dp[2][5]`:  
   best is `dp[1][3] + cost(3,4) = 3 + 6 = 9`

4. `dp[3][6]`:  
   try final cut positions; best is  
   `dp[2][5] + cost(5,5) = 9 + 3 = 12`

Optimal partition: `[5,5,5] | [6,7] | [6]`, total cost `3 + 6 + 3 = 12`.

## ⏱ Complexity Analysis
### Time Complexity
Precomputing all `segmentCost[l][r]` values takes `O(n²)` because each left boundary expands right once. The DP then evaluates `O(k·n²)` transitions. Total time is `O(n² + k·n²) = O(k·n²)`. At `n = 2000`, this is practical; at `10^6`, it is completely intractable and would require a different cost structure or optimization.

### Space Complexity
`segmentCost` uses `O(n²)` space, which dominates. The DP table uses `O(k·n)`, or `O(n)` if rolled by partition count. You can reduce DP memory, but not the `O(n²)` segment-cost matrix unless you recompute costs on demand and accept higher runtime.

## 💡 Key Takeaways
- If the problem asks for splitting an ordered array into exactly `k` contiguous groups with additive per-group cost, think **partition DP** immediately.
- If segment cost depends on a subarray property like distinct count, inversions, or max/min, look for **precompute-all-interval-costs** before writing transitions.
- Use prefix length `i` in DP states, not ending index, to avoid off-by-one errors when mapping the last block to `j..i-1`.
- Enforce non-empty blocks by iterating `i >= p` and `j >= p-1`; otherwise you silently allow illegal empty partitions.
- In production systems, this pattern is the difference between heuristic boundary selection and an explicit, optimizable cost model that can evolve with storage economics.

## 🚀 Variations & Further Practice
- Replace `distinct²` with a more complex block score such as `distinct * length` or a convex penalty over frequency histograms; the DP remains, but interval-cost precomputation becomes more involved.
- Allow up to `k` blocks instead of exactly `k`; this introduces a decision between paying another fixed segment overhead or absorbing more heterogeneity into an existing block.
- Scale `n` far beyond 2000 and ask for faster-than-`O(k·n²)` solutions; the harder twist is determining whether the cost function admits divide-and-conquer DP optimization, Knuth optimization, or no useful structure at all.