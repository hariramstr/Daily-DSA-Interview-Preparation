# Maximum Visitors Covered by One Billboard Move

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Arrays &nbsp;|&nbsp; **Tags:** Arrays, Sliding Window, Prefix Sum

---

## 🗂 Problem Overview
Given an array `visitors`, an initial billboard position `start`, a fixed billboard length `k`, and a relocation budget `m`, compute the maximum exposure obtainable by keeping or moving the billboard once. Any destination must also be a length-`k` contiguous segment. The non-trivial constraint is that relocation is limited by overlap: if the new segment overlaps the original in `overlap` blocks, then `k - overlap` blocks are considered moved and must be `<= m`.

## 🌍 Engineering Impact
This pattern shows up anywhere a fixed-capacity resource can be repositioned within bounded churn: ad-slot reassignment, shard rebalancing, cache warming windows, search index segment promotion, and stream processor partition migration. At scale, brute-force comparison of all candidate placements against all covered elements collapses under latency and cost targets. The right abstraction is “score every fixed-size window once, then constrain reachable windows cheaply.” That separation—precompute value, then filter by movement policy—lets systems optimize placement decisions without repeatedly rescanning data, which is exactly what keeps schedulers, allocators, and ranking pipelines predictable under large input volumes.

## 🔍 Problem Statement
You are given:

- `visitors`: an integer array where `visitors[i]` is the expected pedestrian count for block `i`
- `start`: the left index of the current billboard
- `k`: the billboard length
- `m`: the maximum number of moved blocks allowed during one relocation

The billboard always covers exactly `k` contiguous blocks. If moved from left index `start` to left index `j`, the overlap is:

`max(0, k - |j - start|)`

So moved blocks are:

`k - overlap = min(k, |j - start|)`

Because both windows have the same length, relocation is allowed exactly when `|j - start| <= m`.

Return the maximum sum of any valid destination window of length `k`, including the original window if no move is made.

**Constraints**

- `1 <= visitors.length <= 100000`
- `1 <= visitors[i] <= 10000`
- `1 <= k <= visitors.length`
- `0 <= start <= visitors.length - k`
- `0 <= m <= k`

**Examples**

- `visitors = [5,1,3,8,2,6,4], start = 1, k = 3, m = 2` → `16`
- `visitors = [4,7,2,9,1,5], start = 2, k = 2, m = 0` → `11`

The `10^5` input bound rules out quadratic window comparisons.

## 🪜 How to Solve This
1. Read the movement rule carefully → the overlap formula looks complicated, but for two equal-length windows it simplifies.  
   If you shift the left edge by `d = |j - start|`, you lose exactly `d` old blocks and gain `d` new ones, capped by `k`. Since `m <= k`, validity becomes `d <= m`.

2. That means the candidate destinations are not arbitrary → they are just window starts in a bounded index range around `start`:  
   `j ∈ [start - m, start + m]`, clipped to valid window starts.

3. Now the problem becomes: among all length-`k` windows whose left index lies in that range, find the maximum window sum.

4. Computing each window sum from scratch would cost `O(k)` per window. With up to `O(n)` windows, that is too slow.

5. This is the standard fixed-size sliding-window / prefix-sum pattern → precompute the sum of every length-`k` window in one pass.

6. Then scan only the reachable window-start interval and take the maximum.  
   The key insight is separating **window valuation** from **movement feasibility**.

## 🧩 Algorithm Walkthrough
1. **Model the relocation constraint correctly.**  
   Let the original window be `[start, start + k - 1]` and a candidate window start at `j`.  
   Their overlap is `max(0, k - |j - start|)`, so moved blocks are `k - overlap`. For equal-length windows and `m <= k`, this reduces to `|j - start| <= m`.  
   **Invariant:** every valid relocation corresponds to a candidate start within distance `m` of `start`.

2. **Enumerate all length-`k` window sums once.**  
   Use the **Sliding Window** pattern: compute the first window sum over `[0, k-1]`, then for each next start `i`, update by subtracting `visitors[i-1]` and adding `visitors[i+k-1]`.  
   **Why correct:** each update transforms the previous length-`k` window into the next one exactly.  
   **Invariant:** after processing index `i`, `windowSums[i]` equals `sum(visitors[i..i+k-1])`.

3. **Compute the reachable start range.**  
   Valid window starts are in `[0, n-k]`. Intersect that with `[start-m, start+m]`:  
   `left = max(0, start - m)`  
   `right = min(n - k, start + m)`  
   **Invariant:** every feasible relocation start lies in `[left, right]`, and every index in that interval is feasible.

4. **Select the best reachable window.**  
   Scan `windowSums[left..right]` and return the maximum.  
   **Why correct:** all feasible destinations are represented exactly once, and each has its exact exposure precomputed.

5. **Pattern summary.**  
   This is a combination of **Sliding Window** for fixed-size segment scoring and a simple **range filter** induced by the movement constraint. Prefix sums would also work, but sliding window is more direct and uses constant auxiliary space if you do not materialize all sums.

## 📊 Worked Example
Use `visitors = [5,1,3,8,2,6,4]`, `start = 1`, `k = 3`, `m = 2`.

There are `n-k+1 = 5` windows:

| Window start `j` | Covered blocks | Sum | `|j - start|` | Valid? |
|---|---|---:|---:|---|
| 0 | `[0..2]` | 9  | 1 | yes |
| 1 | `[1..3]` | 12 | 0 | yes |
| 2 | `[2..4]` | 13 | 1 | yes |
| 3 | `[3..5]` | 16 | 2 | yes |
| 4 | `[4..6]` | 12 | 3 | no |

Trace:

1. First window sum = `5+1+3 = 9`
2. Slide right → `9-5+8 = 12`
3. Slide right → `12-1+2 = 13`
4. Slide right → `13-3+6 = 16`
5. Slide right → `16-8+4 = 12`

Reachable starts are `[max(0,1-2), min(4,1+2)] = [0,3]`.  
Maximum among sums at starts `0..3` is `16`.

## ⏱ Complexity Analysis
### Time Complexity
`O(n)`, where `n = visitors.length`. Computing all length-`k` window sums takes one linear pass, and scanning the reachable start range is another linear pass in the worst case. This remains practical at `10^6` elements; at `10^9`, the bottleneck becomes memory bandwidth and data locality, not algorithmic shape.

### Space Complexity
`O(n)` if you store all window sums explicitly, specifically `n-k+1` integers. It can be reduced to `O(1)` extra space by computing sliding sums on the fly and only evaluating windows whose starts fall in the reachable range, at the cost of slightly tighter control flow.

## 💡 Key Takeaways
- If a problem asks for the best fixed-length segment under a local movement or distance constraint, think “precompute all window scores, then filter valid starts.”
- When two compared segments have equal length, overlap-based movement rules often collapse into simple index-distance bounds.
- The main off-by-one trap is the valid window-start range: it is `0..n-k`, not `0..n-1`.
- Be careful not to misread moved blocks as `2 * shift`; for equal-length windows, shifting by `d` replaces exactly `d` blocks.
- In production systems, separating **score computation** from **feasibility constraints** is a scalable design pattern: expensive valuation runs once, policy checks stay cheap.

## 🚀 Variations & Further Practice
- Allow up to `t` relocations over time, with each move constrained by a movement budget. The twist is dynamic optimization over state transitions rather than one-shot selection.
- Make `k` variable per query and answer many `(start, k, m)` requests. The harder part is supporting fast range/window aggregation across heterogeneous window sizes.
- Add blocked or forbidden destination intervals. The conceptual twist is combining sliding-window scoring with interval exclusion or segment-tree-based max queries over valid ranges.