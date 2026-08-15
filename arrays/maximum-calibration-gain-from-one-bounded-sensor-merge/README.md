# Maximum Calibration Gain from One Bounded Sensor Merge

**Difficulty:** Hard &nbsp;|&nbsp; **Topic:** Arrays &nbsp;|&nbsp; **Tags:** Arrays, Prefix Sum, Sliding Window

---

## 🗂 Problem Overview
Given an array of sensor calibration scores, choose at most one contiguous block whose length is between `L` and `R`, replace that block by its floored average, and maximize the final array sum. The output is the best achievable total score. The challenge is that every valid interval changes the total by a length-dependent amount, so brute-forcing all `O(n²)` subarrays is infeasible at `n = 200000`.

## 🌍 Engineering Impact
This pattern shows up anywhere a system may compress or coalesce contiguous measurements under bounded policies: streaming telemetry rollups, time-series downsampling, log compaction windows, packet aggregation, and ranking pipelines that collapse adjacent candidates into a representative score. At small scale, enumerating all windows is tolerable; at production scale, it becomes a latency and cost failure mode. The useful abstraction is not “try every interval,” but “maintain the best candidate among intervals whose lengths are currently legal.” That shift enables linear-time decision-making under strict throughput and memory budgets.

## 🔍 Problem Statement
You are given an integer array `readings` of length `n` (`1 <= n <= 200000`) and integers `L`, `R` with `1 <= L <= R <= n`. You may perform **at most one** merge on a contiguous subarray `readings[l..r]` whose length is in `[L, R]`. That block is replaced by a single value:

`floor(sum(readings[l..r]) / (r - l + 1))`

The final total becomes:

`totalSum - blockSum + floor(blockSum / blockLength)`

You may also skip merging entirely.

Return the maximum possible final total. Values may be negative, and `readings[i]` can be as small as `-1e9`, so 64-bit arithmetic is required.

Examples:

- `readings = [8, -5, 4, -3, 10], L = 2, R = 3` → `16`
- `readings = [7, 6, 5, 4], L = 2, R = 4` → `22`

The key constraint is `n = 200000`: any interval-enumeration strategy is too slow.

## 🪜 How to Solve This
1. Start from the formula for a chosen block:  
   `final = totalSum - blockSum + floor(blockSum / len)`  
   Since `totalSum` is fixed, maximizing the final score means minimizing  
   `loss(block) = blockSum - floor(blockSum / len)`.

2. Now the problem becomes: among all subarrays with length in `[L, R]`, find the one with minimum `loss`.

3. Prefix sums are the obvious first tool because every block sum becomes `pref[r+1] - pref[l]`, so interval sums are `O(1)` once preprocessing is done.

4. The hard part is the bounded-length search. For each right endpoint, only starts in a moving range are legal. That is a classic sliding-window-over-candidates setup.

5. The expression depends on both sum and length, so a single “minimum prefix sum” is not enough. Instead, group candidates by length. For each valid length `k`, scan all windows of size `k`, compute the block sum from prefix sums, and evaluate the resulting final total.

6. Because lengths are bounded, the practical optimization target is `O(n · (R-L+1))`. This is acceptable only when the width is moderate. For the fully hard setting, the important insight is still the same: reduce to interval scoring with prefix sums, then optimize valid-window enumeration.

## 🧩 Algorithm Walkthrough
1. **Compute the total sum and prefix sums**  
   Build `pref` where `pref[i+1] = pref[i] + readings[i]`. Then any subarray sum of length `k` ending at `i` is `pref[i+1] - pref[i+1-k]`.  
   **Invariant:** every interval sum query is constant time.

2. **Reframe the objective**  
   For a block with sum `S` and length `k`, the merged total is  
   `totalSum - S + floor(S / k)`.  
   This is the score to maximize directly, or equivalently minimize `S - floor(S / k)`.  
   **Why correct:** `totalSum` is constant across all choices.

3. **Enumerate legal lengths**  
   For each `k` from `L` to `R`, slide a fixed-size window across the array. This is the **Sliding Window + Prefix Sum** pattern: prefix sums give window sums; the window boundary advances one step at a time.  
   **Invariant:** at step `i`, you are evaluating exactly the subarray `readings[i-k+1..i]`.

4. **Evaluate each window safely**  
   Let `S` be the current window sum. Compute  
   `candidate = totalSum - S + floorDiv(S, k)`.  
   Use true mathematical floor division for negative sums; language-native integer division is often truncation toward zero and will be wrong.  
   **Invariant:** every candidate corresponds to one valid merge.

5. **Track the best answer including “no merge”**  
   Initialize `ans = totalSum`. Update `ans = max(ans, candidate)` for every valid window.  
   **Why correct:** the problem allows skipping the merge, so the original total must remain in the candidate set.

6. **Return the maximum**  
   This yields the optimal final score over all legal intervals and the no-op option.

## 📊 Worked Example
Take `readings = [8, -5, 4, -3, 10]`, `L = 2`, `R = 3`.

`totalSum = 14`  
`pref = [0, 8, 3, 7, 4, 14]`

| Length `k` | Window | Sum `S` | `floor(S/k)` | Final `14 - S + floor(S/k)` |
|---|---|---:|---:|---:|
| 2 | `[8, -5]` | 3 | 1 | 12 |
| 2 | `[-5, 4]` | -1 | -1 | 14 |
| 2 | `[4, -3]` | 1 | 0 | 13 |
| 2 | `[-3, 10]` | 7 | 3 | 10 |
| 3 | `[8, -5, 4]` | 7 | 2 | 9 |
| 3 | `[-5, 4, -3]` | -4 | -2 | 16 |
| 3 | `[4, -3, 10]` | 11 | 3 | 6 |

Best merged result is `16`, from merging `[-5, 4, -3]`.  
Compare with no merge: `14`.  
Answer: `16`.

## ⏱ Complexity Analysis

### Time Complexity
`O(n * (R - L + 1))`. Prefix-sum construction is `O(n)`, and each legal length scans the array once. The dominant cost is evaluating every bounded window size. At `10^6` elements this is viable only when `R-L` is small; at `10^9`, only streaming or heavily constrained variants are realistic.

### Space Complexity
`O(n)` for the prefix-sum array, which owns essentially all auxiliary memory. It can be reduced to `O(1)` extra space if each fixed-length window is maintained with a rolling sum instead of prefix sums, at the cost of slightly more stateful iteration logic.

## 💡 Key Takeaways
- If a problem says “choose one contiguous block” and the score depends on the block sum, prefix sums should be your first reflex.
- If valid subarrays are restricted by a length range `[L, R]`, think sliding-window candidate management rather than unrestricted interval search.
- The merge is optional, so initialize the answer with the original total; otherwise all-negative improvements can be mishandled.
- Floor division for negative sums is a correctness trap: truncation toward zero produces wrong answers for blocks with negative totals.
- In production systems, bounded aggregation problems are usually won by reframing global search into constant-time interval scoring plus a moving legality window.

## 🚀 Variations & Further Practice
- Allow **multiple non-overlapping merges** with the same length bounds. The twist is interval DP: local best windows are no longer sufficient because choices interact.
- Replace floored average with **rounded-to-nearest** or a custom nonlinear merge function. The twist is that algebraic simplification may disappear, changing which summary statistics are sufficient.
- Support **online updates and queries** to `readings`. The twist is moving from static prefix sums to segment trees or Fenwick-based structures with richer interval-state maintenance.