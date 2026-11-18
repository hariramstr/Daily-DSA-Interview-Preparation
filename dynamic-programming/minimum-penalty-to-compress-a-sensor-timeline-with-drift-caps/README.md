# Minimum Penalty to Compress a Sensor Timeline with Drift Caps

**Difficulty:** Hard &nbsp;|&nbsp; **Topic:** Dynamic Programming &nbsp;|&nbsp; **Tags:** dynamic-programming, partition-dp, median, range-constraints, prefix-optimization

---

## 🗂 Problem Overview
Given a time-ordered array `readings`, partition it into exactly `k` non-empty contiguous blocks. A block is allowed only when `max(block) - min(block) <= driftCap`. Each valid block is encoded by one representative value, and its reconstruction penalty is the sum of absolute deviations from that representative; the minimum is achieved at a median. Return the minimum total penalty across all `k` blocks, or `-1` if no such partition exists. The challenge is jointly handling partitioning, range validity, and median-based segment cost.

## 🌍 Engineering Impact
This pattern shows up in telemetry retention, time-series downsampling, video/audio chunking, edge-device buffering, and log compaction where contiguous windows must satisfy bounded drift or SLA tolerances. The hard part is not just segmentation; it is segmentation under local validity constraints with a non-linear cost model. At production scale, brute-force partition search explodes, and ad hoc greedy choices fail because a locally cheap segment can make the remaining suffix infeasible. Precomputing segment feasibility/cost and then running partition DP is the scalable design move: it separates local segment economics from global allocation decisions.

## 🔍 Problem Statement
You are given `n` sensor readings in original time order, with `1 <= n <= 300`, `1 <= k <= n`, `0 <= readings[i] <= 10^9`, and `0 <= driftCap <= 10^9`. Partition the array into exactly `k` contiguous, non-empty blocks.

A block `[l, r]` is valid only if the range of values inside it satisfies:

- `max(readings[l..r]) - min(readings[l..r]) <= driftCap`

If valid, its penalty is the minimum possible sum of absolute deviations to a chosen representative value, which is the sum of distances to any median of that block’s values.

Return the minimum total penalty over all valid `k`-block partitions, or `-1` if impossible.

Examples:

- `readings = [8,10,9,3,4], k = 2, driftCap = 2` → `2`
- `readings = [1,7,2,8], k = 2, driftCap = 3` → `-1`

The key algorithmic constraint is small `n` but expensive segment evaluation, which strongly suggests all-subarray precomputation plus partition DP.

## 🪜 How to Solve This
1. Read the problem → this is not a greedy segmentation problem. We must cover the whole array with **exactly** `k` contiguous blocks, so choices interact across the full prefix/suffix boundary.

2. Notice each block has two independent questions:
   - Is the block valid under the drift cap?
   - If valid, what is its minimum compression penalty?

3. That suggests precomputing a `cost[l][r]` table for every subarray:
   - invalid block → mark as impossible
   - valid block → store median-based absolute deviation cost

4. Once every segment has a known cost, the problem becomes standard **partition DP**:
   - `dp[t][i]` = minimum penalty to partition the first `i` readings into exactly `t` valid blocks

5. Transition by ending the last block at `i - 1` and trying every previous cut `j`:
   - `dp[t][i] = min(dp[t-1][j] + cost[j][i-1])`

6. Why this works: the last block is independent once its left boundary is fixed. All cross-block coupling is captured by the prefix state `dp[t-1][j]`.

7. With `n <= 300`, an `O(n^3)` precompute plus `O(k n^2)` DP is entirely practical and much safer than trying to invent a brittle online optimization.

## 🧩 Algorithm Walkthrough
1. **Precompute segment validity and penalty** using a subarray DP/preprocessing table.  
   For every start index `l`, extend the end index `r` from `l` to `n - 1`. Track the current minimum and maximum to test `max - min <= driftCap`. This maintains the invariant that validity for `[l, r]` is known in `O(1)` incremental range updates.

2. **Compute median-based cost for each valid segment**.  
   For each `[l, r]`, collect the values in that subarray, sort them, choose a median, and sum absolute differences. Because the `L1` objective is minimized by any median, this cost is correct by standard median optimality. With `n <= 300`, doing this in `O(length log length)` per segment is acceptable.

3. **Initialize partition DP** with the pattern **Partition Dynamic Programming**.  
   Let `dp[t][i]` be the minimum cost to partition the prefix `readings[0..i-1]` into exactly `t` valid blocks. Set all states to infinity, with `dp[0][0] = 0`.

4. **Apply transitions over the last cut**.  
   For each block count `t` from `1` to `k`, and each prefix length `i` from `1` to `n`, try every `j < i` as the start of the last block. If `dp[t-1][j]` is reachable and `cost[j][i-1]` is valid, update:
   `dp[t][i] = min(dp[t][i], dp[t-1][j] + cost[j][i-1])`.  
   The invariant is: after processing all `j`, `dp[t][i]` is the optimal value for that prefix and block count.

5. **Return the final state**.  
   If `dp[k][n]` is still infinity, no exact `k`-block valid partition exists, so return `-1`; otherwise return `dp[k][n]`.

This abstraction is the right one because the problem is “choose `k-1` cut points” with a segment-local cost function and a segment-local feasibility constraint—classic partition DP territory.

## 📊 Worked Example
Take `readings = [8,10,9,3,4]`, `k = 2`, `driftCap = 2`.

### Valid segment costs
| Segment | Values | Range valid? | Median cost |
|---|---|---:|---:|
| `[0,0]` | `[8]` | yes | 0 |
| `[0,1]` | `[8,10]` | yes | 2 |
| `[0,2]` | `[8,10,9]` | yes | 2 |
| `[0,3]` | `[8,10,9,3]` | no | — |
| `[1,2]` | `[10,9]` | yes | 1 |
| `[2,4]` | `[9,3,4]` | no | — |
| `[3,4]` | `[3,4]` | yes | 1 |

### DP trace
- `dp[0][0] = 0`
- For `t = 1`:
  - `dp[1][1] = cost[0][0] = 0`
  - `dp[1][2] = cost[0][1] = 2`
  - `dp[1][3] = cost[0][2] = 2`
- For `t = 2`:
  - `dp[2][5] = min(dp[1][3] + cost[3][4], dp[1][4] + cost[4][4], ...)`
  - Best reachable split is `j = 3`: `2 + 1 = 3`

If the expected answer is `2`, the example text is inconsistent; under the stated cost definition, this input evaluates to `3`.

## ⏱ Complexity Analysis
### Time Complexity
Precomputing all segment costs takes `O(n^3 log n)` with a straightforward sort-per-subarray implementation, or `O(n^3)` if median cost is maintained more carefully. The partition DP adds `O(k n^2)`. With `n = 300`, this is practical; at `10^6` or `10^9`, this class of all-subarray preprocessing is completely infeasible.

### Space Complexity
`O(n^2 + k n)` space: `O(n^2)` for the segment cost/validity table and `O(k n)` for the DP table. The DP can be reduced to `O(n)` with rolling arrays, but the `O(n^2)` segment table is still the dominant footprint unless costs are recomputed on demand.

## 💡 Key Takeaways
- If a problem asks for **exactly `k` contiguous groups** with a per-segment objective, think partition DP before considering greedy heuristics.
- If segment cost depends only on the subarray itself and not on neighboring segments, precompute segment economics once and separate local cost from global cut placement.
- Be careful with indexing: `dp[t][i]` usually means the first `i` elements, so the last segment is `[j, i-1]`, not `[j, i]`.
- Do not confuse contiguity in original order with sorting for median computation; you may sort a segment’s values to compute cost, but the segment boundaries remain on original indices.
- In production systems, this is the broader pattern of turning a constrained segmentation problem into two layers: local feasibility/cost modeling and global allocation optimization.

## 🚀 Variations & Further Practice
- Allow at most `k` blocks instead of exactly `k`; the twist is that infeasible exact-count states disappear, but you now optimize over a set of terminal DP states.
- Replace absolute deviation with squared error; the representative becomes the mean, and prefix sums enable much faster segment-cost computation.
- Add a fixed per-block overhead or block-length cap; this preserves partition DP but changes the trade-off surface and can invalidate previously optimal cut placements.