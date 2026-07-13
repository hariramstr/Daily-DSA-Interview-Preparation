# Longest Stable Price Span After One Correction

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Arrays &nbsp;|&nbsp; **Tags:** Arrays, Sliding Window, Hash Map

---

## 🗂 Problem Overview
Given an integer array `prices`, find the maximum length of a contiguous subarray that can be turned into all one value after changing at most one element. You may choose any subarray and optionally replace one value inside it with any integer. The challenge is to do this efficiently for up to `2 * 10^5` elements, where checking every subarray would be prohibitively expensive.

## 🌍 Engineering Impact
This pattern shows up anywhere a system must detect the longest nearly-uniform run under limited correction: telemetry smoothing, fraud-resistant event streams, sensor pipelines, clickstream sessionization, and log anomaly suppression. In production, brute-force window validation collapses under sustained throughput because each candidate range redoes work already seen. A sliding-window plus frequency-tracking approach turns repeated recomputation into incremental state updates. That matters when windows are evaluated continuously over millions of records, or when the same logic sits inside ranking, alerting, or quality-control pipelines where latency and memory ceilings are fixed.

## 🔍 Problem Statement
You are given an array `prices` where `prices[i]` is the observed price on day `i`. A contiguous span is considered stable if all values in that span are equal. You may correct at most one element inside a chosen span to any integer.

Return the maximum length of a contiguous subarray that can become entirely equal after zero or one correction.

Constraints:

- `1 <= prices.length <= 2 * 10^5`
- `1 <= prices[i] <= 10^9`

Examples:

- `prices = [5,5,7,5,5,5]` → `6`
  - Change `7` to `5`, making the whole array stable.

- `prices = [3,3,4,4,4,3]` → `4`
  - The best span is `[4,4,4,3]`; change the last `3` to `4`.

The key constraint is array size: any `O(n^2)` enumeration of subarrays is too slow, so the solution must validate windows incrementally in near-linear time.

## 🪜 How to Solve This
1. Read the condition carefully → a window is valid if all but at most one element already match some dominant value.

2. Rephrase it mathematically → for any window, if `window_size - max_frequency_in_window <= 1`, then one correction is enough to make the whole window equal.

3. That immediately suggests a frequency-aware window, not brute force. We do not care which exact value wins globally; we only care about the most frequent value inside the current range.

4. Since the subarray must be contiguous, think **Sliding Window / Two Pointers**. Expand the right boundary, update counts, and track the highest frequency seen in the window.

5. If the window needs more than one correction, shrink from the left until it becomes valid again.

6. Record the largest valid window length during the scan.

The key insight is that “one correction allowed” converts the problem from exact equality checking into “all but one match the modal value.” Once you see that, the standard variable-size sliding window with a hash map becomes the obvious fit.

## 🧩 Algorithm Walkthrough
1. **Use the Sliding Window / Two Pointers pattern.**  
   Maintain a window `[left, right]` and a hash map `freq` storing counts of values inside the window. This pattern fits because the validity of a larger window can be repaired by moving `left` forward rather than recomputing from scratch.

2. **Expand the window one element at a time.**  
   For each `right`, increment `freq[prices[right]]`. Also maintain `maxFreq`, the largest count of any single value seen in the current window growth process.

3. **Check whether the window is still fixable with one correction.**  
   A window of size `len = right - left + 1` is valid when `len - maxFreq <= 1`.  
   Why: `maxFreq` elements already match the best target value; every other element must be corrected. If there is at most one such element, the window is feasible.

4. **Shrink when invalid.**  
   While `len - maxFreq > 1`, decrement `freq[prices[left]]` and advance `left`. This restores the invariant that the active window requires at most one correction.

5. **Track the best answer.**  
   After revalidating the window, update `answer = max(answer, right - left + 1)`.

6. **Why stale `maxFreq` still works.**  
   `maxFreq` may remain higher than the true current maximum after shrinking, but that only delays shrinking; it never causes us to miss the optimal answer. This is the same correctness argument used in the classic longest-repeating-character-replacement problem: every recorded answer corresponds to some window size achievable during expansion, and the algorithm remains linear.

## 📊 Worked Example
Example: `prices = [3,3,4,4,4,3]`

| right | value | freq snapshot | maxFreq | window `[left..right]` | size | valid? |
|---|---:|---|---:|---|---:|---|
| 0 | 3 | {3:1} | 1 | [0..0] | 1 | yes |
| 1 | 3 | {3:2} | 2 | [0..1] | 2 | yes |
| 2 | 4 | {3:2,4:1} | 2 | [0..2] | 3 | yes |
| 3 | 4 | {3:2,4:2} | 2 | [0..3] | 4 | no → shrink |
| 3 | 4 | {3:1,4:2} | 2 | [1..3] | 3 | yes |
| 4 | 4 | {3:1,4:3} | 3 | [1..4] | 4 | yes |
| 5 | 3 | {3:2,4:3} | 3 | [1..5] | 5 | no → shrink |
| 5 | 3 | {3:1,4:3} | 3 | [2..5] | 4 | yes |

Best valid length is `4`, from window `[4,4,4,3]`, which becomes all `4`s after one correction.

## ⏱ Complexity Analysis
### Time Complexity
`O(n)` expected time. Each element enters the window once when `right` advances and leaves at most once when `left` advances. Hash map updates are constant time on average, so the dominant cost is the single linear scan. At `10^6` elements this is practical; at `10^9`, even linear time becomes operationally expensive.

### Space Complexity
`O(k)` where `k` is the number of distinct values currently represented in the frequency map, bounded by `O(n)` in the worst case. The map owns essentially all auxiliary space. You cannot reduce this to constant space without giving up efficient frequency tracking for arbitrary integers.

## 💡 Key Takeaways
- If a contiguous-window problem says “change at most `k` items to make the window uniform,” translate it to `window_size - max_frequency <= k`.
- When the answer is the longest valid contiguous range under a mutable constraint, variable-size sliding window is usually the first abstraction to test.
- The validity check must use the current window length `right - left + 1`; off-by-one errors here silently produce wrong shrink behavior.
- Do not recompute the exact maximum frequency on every shrink; that turns a linear solution into something much slower.
- At scale, the win comes from incremental state maintenance: preserve enough summary information per window so validation is constant-time instead of re-scanning historical data.

## 🚀 Variations & Further Practice
- Allow up to `k` corrections instead of one. Same sliding-window core, but the validity condition becomes `window_size - max_frequency <= k`.
- Find the longest subarray that can be made alternating after one correction. The twist is that the target pattern is no longer a single repeated value, so frequency alone is insufficient.
- Support online updates and range queries over a changing price array. The harder part is moving from one-pass window logic to data structures such as segment trees or block decomposition.