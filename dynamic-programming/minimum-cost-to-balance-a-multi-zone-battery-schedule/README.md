# Minimum Cost to Balance a Multi-Zone Battery Schedule

**Difficulty:** Hard &nbsp;|&nbsp; **Topic:** Dynamic Programming &nbsp;|&nbsp; **Tags:** dynamic-programming, interval-dp, prefix-optimization

---

## 🗂 Problem Overview
Given an array `delta` of length `n`, split it into exactly `k` contiguous zones. For each zone, choose a single target drift `t`, then pay `|delta[i] - t|` for every slot in that zone. The objective is to minimize total adjustment cost across all zones.

The non-trivial part is that both decisions interact: where to cut the timeline, and what target each zone should use. With `n <= 300`, brute-forcing all partitions is infeasible, so the solution hinges on interval cost preprocessing plus dynamic programming.

## 🌍 Engineering Impact
This pattern shows up anywhere a noisy time-ordered signal must be segmented into a small number of stable operating regimes: battery dispatch planning, autoscaling windows, traffic shaping, anomaly bucketing, and piecewise-constant compression in telemetry pipelines. The production concern is not just optimality, but predictable cost under bounded latency.

Without interval precomputation, repeated recomputation of segment cost turns a manageable DP into an expensive nested-loop system that does not scale even at moderate batch sizes. With the right decomposition, you separate local segment scoring from global partition choice, which is exactly how many planning and scheduling systems stay both explainable and fast.

## 🔍 Problem Statement
You are given an integer array `delta` where `delta[i]` is the battery drift during time slot `i`: positive means discharge, negative means charge. Partition the array into exactly `k` contiguous zones. For each zone, choose one target value `t`, and adjust every element in that zone to `t` at cost `|delta[i] - t|`. Return the minimum total cost.

For any fixed zone, the optimal `t` is a median of the values in that subarray, because the objective is sum of absolute deviations.

Constraints:

- `1 <= n <= 300`
- `1 <= k <= min(n, 30)`
- `-10^6 <= delta[i] <= 10^6`
- Answer fits in signed 64-bit integer

Examples:

- `delta = [4, 1, 7, 3, 6], k = 2` → `5`
- `delta = [-5, -2, -4, 8, 9, 7], k = 3` → `4`

The key algorithmic constraint is that we need optimal costs for many subarrays, so recomputing medians and absolute-deviation sums inside the DP is too expensive.

## 🪜 How to Solve This
1. Read the objective carefully → we are not choosing arbitrary edits per element; we are forcing each contiguous zone to collapse to one common target.
2. Fixed zone cost uses `sum |x - t|` → that immediately signals the median. So each subarray has a well-defined optimal cost independent of the global partitioning.
3. Once interval cost is isolated, the remaining problem becomes: choose exactly `k` cuts so the sum of chosen interval costs is minimal.
4. That is classic **prefix dynamic programming**:  
   `dp[z][i] = minimum cost to partition first i elements into z zones`.
5. Transition by ending the last zone at `i` and trying every possible previous cut `p`:  
   `dp[z][i] = min(dp[z-1][p] + cost[p+1][i])`.
6. The real implementation question is how to get `cost[l][r]` efficiently for all intervals.
7. Since `n` is only 300, an `O(n^3 log n)` or `O(n^3)` preprocessing strategy is acceptable: build each interval’s sorted multiset incrementally, compute its median, and derive absolute-deviation cost.
8. Then run the DP over prefixes and zone counts. The decomposition is the whole trick: local median-optimal interval scoring, global partition optimization.

## 🧩 Algorithm Walkthrough
1. **Precompute interval costs (`cost[l][r]`)**  
   For every starting index `l`, extend the interval to the right one element at a time. Maintain the values in that interval in sorted order. For each `r`, the optimal target is the median of `delta[l..r]`, and `cost[l][r]` is the sum of absolute differences to that median.  
   **Why correct:** for absolute loss, any median minimizes total deviation.  
   **Invariant:** `cost[l][r]` stores the minimum possible adjustment cost for subarray `l..r`.

2. **Define the DP state**  
   Let `dp[z][i]` be the minimum cost to partition the first `i` elements into exactly `z` contiguous zones. Use 1-based prefix length for clean transitions.  
   **Why correct:** every valid solution over a prefix ends with one final zone.

3. **Initialize base cases**  
   Set `dp[0][0] = 0` and all other states to infinity. No elements with zero zones costs zero; any other zero-zone configuration is invalid.  
   **Invariant:** unreachable states remain infinite and never contaminate minima.

4. **Transition over the last cut**  
   For each `z` from `1..k`, for each `i` from `1..n`, try every `p < i` as the end of the previous prefix:  
   `dp[z][i] = min(dp[z][i], dp[z-1][p] + cost[p+1][i])`.  
   **Pattern:** **Interval DP / Prefix Partition DP**.  
   **Why right abstraction:** the objective is additive across contiguous segments, and each segment’s score is independent once its boundaries are fixed.

5. **Return `dp[k][n]`**  
   This is the minimum cost to cover the full array with exactly `k` zones.

## 📊 Worked Example
Take `delta = [-5, -2, -4, 8, 9, 7]`, `k = 3`.

First compute a few interval costs:

| Interval | Sorted values | Median | Cost |
|---|---:|---:|---:|
| `1..1` | `[-5]` | `-5` | `0` |
| `2..3` | `[-4, -2]` | `-4` or `-2` | `2` |
| `4..6` | `[7, 8, 9]` | `8` | `2` |
| `1..3` | `[-5, -4, -2]` | `-4` | `3` |

Now DP:

- `dp[1][3] = cost[1][3] = 3`
- `dp[2][3] = min(dp[1][1] + cost[2][3], dp[1][2] + cost[3][3]) = min(0+2, 3+0) = 2`
- `dp[3][6]` tries all last cuts:
  - after `3`: `dp[2][3] + cost[4][6] = 2 + 2 = 4`
  - other cuts are worse

So the optimal partition is `[-5]`, `[-2, -4]`, `[8, 9, 7]` with total cost `4`.

## ⏱ Complexity Analysis
### Time Complexity
Precomputing all interval costs takes `O(n^3)` with a straightforward sorted-interval or median-evaluation approach at this constraint size. The DP takes `O(k * n^2)`. Overall complexity is dominated by interval preprocessing. At `n = 300`, this is practical; at `10^6` or `10^9`, this class of exact interval DP is completely intractable without stronger structure.

### Space Complexity
`O(n^2 + k*n)`. The `cost[l][r]` table owns most of the space, with the DP table adding `O(k*n)`. You can reduce DP to two rows, but `cost` still remains quadratic unless you recompute intervals on demand, which usually loses more time than it saves.

## 💡 Key Takeaways
- If a problem asks for exactly `k` contiguous groups with additive segment scores, think **prefix partition DP** immediately.
- If a segment cost is `sum |x - t|` with free choice of `t`, the median is the signal that interval preprocessing is possible.
- Be explicit about indexing: `cost[p+1][i]` in 1-based DP is the most common off-by-one failure point.
- For even-length intervals, any value between the two middle elements is optimal; when using actual array values, either median works and yields the same minimum cost.
- The transferable design insight is to separate **local scoring** from **global composition**; that decomposition is what makes many planning systems both optimizable and maintainable.

## 🚀 Variations & Further Practice
- Allow each zone to use a **mean-based** target with squared error instead of absolute error; the optimal representative changes, and prefix sums replace median-based interval logic.
- Add a fixed penalty per cut or allow up to `k` zones instead of exactly `k`; this changes the DP objective and can alter monotonicity or optimization opportunities.
- Move from 1D contiguous partitions to 2D grid zoning or hierarchical segmentation; the same local-vs-global decomposition remains, but state explosion becomes the central challenge.