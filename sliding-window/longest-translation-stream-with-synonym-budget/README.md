# Longest Translation Stream With Synonym Budget

**Difficulty:** Hard &nbsp;|&nbsp; **Topic:** Sliding Window &nbsp;|&nbsp; **Tags:** Sliding Window, Hash Map, Frequency Counting

---

## 🗂 Problem Overview
Given an array `words`, a per-ID occurrence cap `limit`, and a global budget `B`, find the longest contiguous subarray whose total over-limit frequency cost is at most `B`. For any ID, each occurrence beyond `limit[id]` contributes 1 to the window’s cost; missing IDs have limit 0. The challenge is that validity depends on aggregate frequency excess across the current window, so brute-force enumeration of all subarrays is too slow for `n` up to `2 * 10^5`.

## 🌍 Engineering Impact
This pattern shows up in streaming quality monitors, adaptive rate-limiters, noisy-event suppression, search/query deduplication, and observability pipelines where local violations are tolerated up to a global error budget. The operational requirement is usually: maximize useful contiguous throughput while enforcing bounded aggregate drift. Without an incremental windowed approach, systems fall back to rescanning state per interval, which collapses under high-cardinality streams. The sliding-window + frequency-accounting model enables online decisions, predictable memory growth, and latency that scales linearly with event volume rather than quadratically with candidate segment count.

## 🔍 Problem Statement
You are given:

- `words`: an array of normalized meaning IDs, length `1 .. 2 * 10^5`
- `limit`: a map from meaning ID to allowed count inside a segment
- `B`: a global synonym budget, `0 .. 2 * 10^5`

For any contiguous window `[l, r]`, let `freq[x]` be the count of ID `x` in that window. The window’s violation cost is:

`sum(max(0, freq[x] - limit[x]))`

If an ID is absent from `limit`, treat its allowed count as `0`. Return the maximum window length whose total cost is `<= B`.

Example 1:
- `words = [4, 1, 4, 2, 4, 1, 3]`
- `limit = {4: 2, 1: 1, 2: 1, 3: 1}`
- `B = 1`
- Output: `5`

Example 2:
- `words = [8, 8, 5, 8, 5, 6, 5, 5]`
- `limit = {8: 1, 5: 2, 6: 1}`
- `B = 2`
- Output: `6`

The decisive constraint is the required runtime: better than `O(n^2)`.

## 🪜 How to Solve This
1. Read the cost definition carefully → it is additive across IDs, but only depends on counts inside a contiguous window.

2. Contiguous segment + “longest valid” usually suggests a sliding window. The question is whether validity can be updated incrementally instead of recomputed from scratch.

3. Observe what happens when you add one word `x` to the right:
   - `freq[x]` increases by 1.
   - The total cost only changes for `x`.
   - If the new `freq[x]` is now greater than `limit[x]`, cost increases by exactly 1.

4. Similarly, when removing one word `y` from the left:
   - `freq[y]` decreases by 1.
   - If `freq[y]` was above `limit[y]` before removal, total cost decreases by exactly 1.

5. That means the window’s violation cost is maintainable in `O(1)` per pointer move. So use two pointers:
   - expand right greedily,
   - while cost exceeds `B`, shrink left,
   - after restoration, update the best length.

6. This works because once a window is invalid, moving `right` further cannot reduce cost; only advancing `left` can restore validity.

## 🧩 Algorithm Walkthrough
1. **Use the Two Pointers / Sliding Window pattern.**  
   Maintain a window `[left, right]`, a frequency map `freq`, and a scalar `cost` representing total excess occurrences. This is the right abstraction because the objective is the longest contiguous range under a monotone validity constraint.

2. **Expand the window by moving `right` one step at a time.**  
   Let `x = words[right]`. Increment `freq[x]`. Fetch `allowed = limit.get(x, 0)`. If `freq[x] > allowed`, then this newly added occurrence is an excess one, so increment `cost` by 1.  
   **Invariant:** `cost` always equals `sum(max(0, freq[id] - limit[id]))` for the current window.

3. **Restore validity when the budget is exceeded.**  
   While `cost > B`, remove `words[left]` from the window. Suppose that value is `y`. Before decrementing `freq[y]`, check whether `freq[y] > limit.get(y, 0)`. If true, the removed occurrence was contributing one unit of excess, so decrement `cost` by 1. Then decrement `freq[y]` and advance `left`.  
   **Invariant:** after the loop, the window is valid: `cost <= B`.

4. **Record the best window length.**  
   Once valid, compute `right - left + 1` and maximize the answer. This is correct because for each `right`, the algorithm keeps the leftmost valid boundary after necessary shrinking, yielding the largest valid window ending at `right`.

5. **Why linear time holds.**  
   Each element enters the window once and leaves once. Both pointers move only forward, so total updates are `O(n)` with hash-map operations on top.

## 📊 Worked Example
Take `words = [4, 1, 4, 2, 4, 1, 3]`, `limit = {4:2, 1:1, 2:1, 3:1}`, `B = 1`.

| right | word | freq after add         | cost | left after shrink | valid window           | best |
|------:|------|------------------------|-----:|------------------:|------------------------|-----:|
| 0     | 4    | {4:1}                  | 0    | 0                 | [4]                    | 1    |
| 1     | 1    | {4:1,1:1}              | 0    | 0                 | [4,1]                  | 2    |
| 2     | 4    | {4:2,1:1}              | 0    | 0                 | [4,1,4]                | 3    |
| 3     | 2    | {4:2,1:1,2:1}          | 0    | 0                 | [4,1,4,2]              | 4    |
| 4     | 4    | {4:3,1:1,2:1}          | 1    | 0                 | [4,1,4,2,4]            | 5    |
| 5     | 1    | {4:3,1:2,2:1}          | 2    | 1                 | [1,4,2,4,1]            | 5    |
| 6     | 3    | {4:2,1:2,2:1,3:1}      | 1    | 1                 | [1,4,2,4,1,3]          | 6    |

At `right = 6`, the valid window length becomes `6`, so the maximum is `6`.

## ⏱ Complexity Analysis
### Time Complexity
`O(n)` expected time, where `n = words.length`. Each index is processed at most twice: once when `right` expands and once when `left` contracts. The dominant operations are hash-map reads and writes. At `10^6` elements this remains practical; at `10^9`, linear scan cost becomes the limiting factor, not algorithmic blow-up.

### Space Complexity
`O(k)` where `k` is the number of distinct IDs present in the current or total stream, bounded by `n`. The frequency map owns the extra space. You cannot meaningfully reduce this without sacrificing constant-time count updates or assuming a dense bounded ID domain.

## 💡 Key Takeaways
- If the problem asks for the **longest contiguous segment** under a constraint that changes locally when one element enters or leaves, think sliding window immediately.
- If validity depends on **frequency excess over per-key thresholds**, look for a hash map plus a single aggregated violation counter instead of recomputing totals.
- Missing IDs in `limit` must behave as limit `0`; forgetting the default changes both correctness and edge-case behavior.
- When shrinking, decrement `cost` based on the **pre-decrement frequency**; checking after decrement causes an off-by-one bug.
- The transferable design insight is to maintain **incremental violation state**, not full recomputation, which is the difference between online scalability and per-window rescans.

## 🚀 Variations & Further Practice
- **Weighted excess cost:** each extra occurrence of ID `x` costs `weight[x]` instead of `1`. Same window pattern, but updates become weighted and overflow handling matters.
- **Dynamic limits per position:** `limit` changes over time or depends on `right`. The harder part is preserving a monotone validity condition as the policy shifts during the scan.
- **Return the segment itself with tie-breaking rules:** maximize length, then minimize cost, then choose earliest start. The window logic stays linear, but answer maintenance becomes multi-criteria.