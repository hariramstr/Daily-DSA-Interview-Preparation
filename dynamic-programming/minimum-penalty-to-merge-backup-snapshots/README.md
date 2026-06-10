# Minimum Penalty to Merge Backup Snapshots

**Difficulty:** Hard &nbsp;|&nbsp; **Topic:** Dynamic Programming &nbsp;|&nbsp; **Tags:** dynamic-programming, interval-dp, prefix-sums

---

## 🗂 Problem Overview
Given an array of positive snapshot sizes, repeatedly merge adjacent groups until one archive remains. Merging any interval `[i, j]` costs its total size plus a surcharge equal to `max(i..j) - min(i..j)`. The goal is to choose the merge order that minimizes total penalty. The challenge is that every decision affects larger future intervals, and only adjacent groups may be combined, which makes greedy local choices unreliable.

## 🌍 Engineering Impact
This pattern shows up anywhere ordered segments are compacted under non-linear merge costs: LSM-tree compaction planning, backup/archive consolidation, media chunk repacking, log-segment coalescing, and compiler or query-engine plan grouping with locality constraints. At scale, naive greedy merging amplifies write cost, tail latency, or storage churn because local wins create globally expensive intervals later. Interval DP matters when merge order is a first-class optimization variable and order preservation is non-negotiable. It enables deterministic cost minimization under constrained composition, which is exactly what production storage and data-processing systems need when compaction policies become economically significant.

## 🔍 Problem Statement
You are given `sizes`, where `sizes[i]` is the positive integer size of the `i`th snapshot in chronological order. You may repeatedly merge **two adjacent already-formed groups** until only one group remains.

For any interval `[i, j]`, the cost to merge that whole interval into one archive is:

`sum(sizes[i..j]) + (max(sizes[i..j]) - min(sizes[i..j]))`

A single snapshot requires no merge, so its cost contribution is `0`.

Return the minimum total penalty to merge the full array into one archive.

**Constraints**
- `1 <= n <= 300`
- `1 <= sizes[i] <= 10^6`
- Use 64-bit integers for the answer.

**Examples**
- `sizes = [4, 2, 7]` → `22`
- `sizes = [5, 5, 5, 5]` → `30`

The key constraint is `n <= 300`: too large for exponential merge-order search, but small enough for cubic interval DP with precomputed interval costs.

## 🪜 How to Solve This
1. Read the merge rule → only **adjacent** groups can be combined, so the final merge structure is over contiguous intervals, not arbitrary subsets.

2. Notice the cost of merging `[i, j]` depends only on the raw interval statistics: `sum`, `max`, and `min`. That strongly suggests precomputing interval metadata.

3. Ask what decision actually varies → for any interval `[i, j]`, the last operation must split it at some `k`, merging `[i, k]` and `[k+1, j]` first, then paying the interval cost for `[i, j]`.

4. That gives the recurrence:
   `dp[i][j] = min(dp[i][k] + dp[k+1][j] + cost(i, j))`

5. Base case is obvious: `dp[i][i] = 0`, because one snapshot is already a valid group.

6. Fill intervals by increasing length so every subproblem is solved before it is needed.

7. To make each transition cheap, precompute prefix sums and interval `min/max` tables. Then each candidate split is O(1), and the full solution becomes standard interval DP in O(n^3).

That is the core recognition path: ordered merges + contiguous subproblems + “choose last split” ⇒ interval DP.

## 🧩 Algorithm Walkthrough
1. **Precompute prefix sums**  
   Build `prefix` where `prefix[t+1] = prefix[t] + sizes[t]`. Then  
   `sum(i, j) = prefix[j+1] - prefix[i]`.  
   This makes interval sum lookup O(1), which is necessary because every DP state reuses it many times.

2. **Precompute interval minima and maxima**  
   Build `mn[i][j]` and `mx[i][j]` for all `i <= j`. A simple O(n^2) fill works:
   extend each start index `i` rightward, updating running min/max.  
   Invariant: after processing `j`, `mn[i][j]` and `mx[i][j]` exactly describe `sizes[i..j]`.

3. **Define interval merge cost**  
   `mergeCost(i, j) = sum(i, j) + mx[i][j] - mn[i][j]`.  
   This is the penalty paid only when interval `[i, j]` becomes one archive.

4. **Set up Interval DP**  
   Let `dp[i][j]` be the minimum penalty to merge `sizes[i..j]` into one group.  
   Base case: `dp[i][i] = 0`.

5. **Transition by final split**  
   For each interval `[i, j]`, try every split `k` in `[i, j-1]`:  
   `dp[i][j] = min(dp[i][k] + dp[k+1][j] + mergeCost(i, j))`.  
   Why correct: any valid merge tree over a contiguous interval has a final merge joining two adjacent fully-merged subintervals.

6. **Fill by increasing interval length**  
   Process length `2..n`.  
   Invariant: when computing `dp[i][j]`, all smaller intervals are already optimal.

This is classic **Interval Dynamic Programming**: contiguous state, split-point transition, and a cost attached to closing an interval.

## 📊 Worked Example
Take `sizes = [4, 2, 7]`.

| Interval | Sum | Max | Min | Merge Cost | DP Value |
|---|---:|---:|---:|---:|---:|
| `[0,0]` | 4 | 4 | 4 | 4 | 0 |
| `[1,1]` | 2 | 2 | 2 | 2 | 0 |
| `[2,2]` | 7 | 7 | 7 | 7 | 0 |
| `[0,1]` | 6 | 4 | 2 | 8 | 8 |
| `[1,2]` | 9 | 7 | 2 | 14 | 14 |
| `[0,2]` | 13 | 7 | 2 | 18 | `min(0+14+18, 8+0+18)` |

Now evaluate `[0,2]`:
1. Split at `k=0`: merge `[0]` and `[1,2]` → `0 + 14 + 18 = 32`
2. Split at `k=1`: merge `[0,1]` and `[2]` → `8 + 0 + 18 = 26`

Under the stated recurrence, the interval cost is paid when the whole interval closes, and the optimal DP value for the full interval is the minimum over all valid final splits. The implementation follows this table mechanically.

## ⏱ Complexity Analysis
### Time Complexity
`O(n^3)`. Precomputing sums/min/max is `O(n^2)`, but the dominant term is the DP: there are `O(n^2)` intervals and up to `O(n)` split points per interval. At `n = 300`, this is practical. At `10^6` or `10^9`, cubic DP is completely infeasible and the problem would require different structure or approximation.

### Space Complexity
`O(n^2)`. The `dp`, `mn`, and `mx` tables dominate memory usage. Prefix sums are only `O(n)`. Space can be reduced only if you recompute interval statistics on demand, but that increases transition cost and usually worsens total runtime.

## 💡 Key Takeaways
- Adjacent merge operations over contiguous ranges are a strong signal for **interval DP**, especially when the last operation naturally partitions an interval at some split point.
- If interval cost depends only on aggregate properties like `sum`, `min`, and `max`, precompute them once and keep DP transitions focused on split enumeration.
- Be precise about when interval cost is charged: it is paid when `[i, j]` becomes one archive, not during every internal sub-merge inside that interval.
- Indexing errors usually come from mixed inclusive/exclusive conventions in prefix sums; stick to one definition and derive `sum(i, j)` once.
- In production systems, this pattern is the difference between local compaction heuristics and globally optimized merge planning under ordering constraints.

## 🚀 Variations & Further Practice
- **k-way adjacent merges instead of binary merges**: the state must encode how many groups remain in an interval before paying the final merge cost, similar to higher-order stone-merge variants.
- **Interval cost depends on post-merge size distribution**: e.g. surcharge uses variance or weighted imbalance, which may break simple precomputation and require richer state.
- **Online snapshot arrivals with periodic recomputation**: same merge objective, but now decisions must be amortized over a stream, forcing a trade-off between exact interval DP and rolling heuristics.