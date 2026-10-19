# Longest Checkout Stretch With One Coupon Reset

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Arrays &nbsp;|&nbsp; **Tags:** Arrays, Sliding Window, Hash Map

---

## 🗂 Problem Overview
Given an array `coupons`, return the maximum length of a contiguous subarray that can be made entirely equal after changing at most one element inside that subarray. The input is large enough that checking all subarrays is not viable. The core challenge is to detect, in one pass, whether a window contains at most one value that differs from the dominant coupon code.

## 🌍 Engineering Impact
This pattern shows up in streaming anomaly suppression, log normalization, clickstream session cleanup, and telemetry pipelines where you want the longest nearly-uniform run with limited correction budget. At scale, brute-force segment validation collapses under high-cardinality event streams and long retention windows. A sliding-window formulation enables online processing with bounded per-element work, which matters in real-time fraud detection, observability backends, and ranking pipelines. The broader architectural win is recognizing when a local “tolerate up to k errors” rule can be enforced incrementally instead of recomputing segment validity from scratch.

## 🔍 Problem Statement
You are given an integer array `coupons` where `coupons[i]` is the coupon code used by the `i`-th customer in checkout order. A contiguous stretch is valid if all values are equal, or can be made equal by modifying at most one element within that stretch.

Return the maximum possible length of such a contiguous stretch.

Constraints:

- `1 <= coupons.length <= 200000`
- `1 <= coupons[i] <= 1000000000`

Examples:

- `coupons = [5,5,3,5,5,5]` → `6`  
  Change the `3` to `5`.

- `coupons = [1,2,2,3,2,2,2]` → `6`  
  The subarray `[2,2,3,2,2,2]` becomes all `2` after one change.

The key constraint is array size: `O(n^2)` subarray enumeration is too slow, so the solution must work in linear or near-linear time.

## 🪜 How to Solve This
1. Start from the validity condition → a window is acceptable if all but at most one element are the same.

2. Rephrase that mathematically → for any window, if `window_size - max_frequency_in_window <= 1`, then one reset is enough to make the entire window uniform.

3. That immediately suggests a **Sliding Window** → expand the right boundary greedily, and only shrink from the left when the window needs more than one modification.

4. To evaluate validity efficiently, maintain counts of coupon codes inside the current window with a `HashMap`, plus the highest frequency seen in the window.

5. Why this works → the only thing that matters is how many elements disagree with the dominant value. We do not need to know which value we would reset to until after the fact.

6. Why not brute force → every subarray would require recomputing frequencies, which is quadratic or worse. Sliding window converts repeated work into incremental updates.

7. The mental model is: “longest contiguous segment with at most one mismatch relative to its majority value.”

## 🧩 Algorithm Walkthrough
1. **Use the Sliding Window / Two Pointers pattern.**  
   Maintain a window `[left, right]` over `coupons`. As `right` advances, include `coupons[right]` in a frequency map. This pattern is appropriate because the problem asks for the longest contiguous segment satisfying a local validity rule.

2. **Track frequencies with a Hash Map.**  
   Increment the count for the current coupon code. Also maintain `maxFreq`, the largest count of any single value seen in the current window. This captures the best target value to normalize the window to.

3. **Check the window invariant.**  
   A window is valid if  
   `window_length - maxFreq <= 1`.  
   Why: all elements except the majority value would need to be changed, and we are allowed at most one change.

4. **Shrink when invalid.**  
   If the invariant fails, move `left` forward and decrement the count of `coupons[left]` until the window becomes valid again. This preserves the invariant that the active window always needs at most one reset.

5. **Update the answer after each expansion.**  
   Once valid, compute `right - left + 1` and maximize the result. Since each pointer moves at most `n` times, total work stays linear.

6. **About stale `maxFreq`.**  
   In many sliding-window frequency problems, `maxFreq` is allowed to stay slightly stale when the window shrinks. That is still safe here for the same reason as in “Longest Repeating Character Replacement”: it may delay shrinking, but it never causes us to miss the optimal maximum length.

## 📊 Worked Example
Example: `coupons = [1,2,2,3,2,2,2]`

| right | value | freq map (relevant)     | maxFreq | window `[left..right]` | valid? | best |
|------:|------:|-------------------------|--------:|-------------------------|:------:|-----:|
| 0 | 1 | `{1:1}` | 1 | `[0..0]` len=1 | yes | 1 |
| 1 | 2 | `{1:1,2:1}` | 1 | `[0..1]` len=2 | yes | 2 |
| 2 | 2 | `{1:1,2:2}` | 2 | `[0..2]` len=3 | yes | 3 |
| 3 | 3 | `{1:1,2:2,3:1}` | 2 | `[0..3]` len=4 | no | 3 |
| shrink | remove `1` | `{1:0,2:2,3:1}` | 2 | `[1..3]` len=3 | yes | 3 |
| 4 | 2 | `{2:3,3:1}` | 3 | `[1..4]` len=4 | yes | 4 |
| 5 | 2 | `{2:4,3:1}` | 4 | `[1..5]` len=5 | yes | 5 |
| 6 | 2 | `{2:5,3:1}` | 5 | `[1..6]` len=6 | yes | 6 |

Best window is `[2,2,3,2,2,2]`, which becomes uniform by changing `3` to `2`.

## ⏱ Complexity Analysis

### Time Complexity
`O(n)` expected time. Each element enters the window once and leaves it at most once, so the dominant cost is Hash Map increment/decrement during pointer movement. At `10^6` elements this is practical in memory-resident processing; at `10^9`, the algorithmic shape is still right, but throughput becomes constrained by memory bandwidth and streaming architecture.

### Space Complexity
`O(m)` where `m` is the number of distinct coupon codes in the current window, bounded by `O(n)` in the worst case. The Hash Map owns this space. You cannot generally reduce it below distinct-value tracking without losing exact frequency information.

## 💡 Key Takeaways
- If the problem asks for the **longest contiguous segment** under a bounded-error condition, think sliding window before considering nested loops.
- If validity depends on the **most frequent value inside a window**, that is a strong signal for a frequency map plus a running `maxFreq`.
- The validity check is `window_size - maxFreq <= 1`, not “there are at most two distinct values”; two-value windows can still require multiple changes.
- Be careful when shrinking: decrement the count for `coupons[left]` before incrementing `left`, or window state and boundaries drift out of sync.
- The production lesson is broader than this problem: once a tolerance rule can be expressed as a maintained window invariant, you can turn expensive recomputation into single-pass streaming logic.

## 🚀 Variations & Further Practice
- Allow up to `k` resets instead of one. Same sliding-window pattern, but the invariant becomes `window_size - maxFreq <= k`; the harder part is seeing the generalization cleanly.
- Return the actual subarray bounds and target coupon code, not just the length. This adds reconstruction concerns and forces careful handling of stale `maxFreq`.
- Solve the non-contiguous version: longest subsequence that can be made equal with one change. The contiguous-window abstraction breaks, and the problem shifts toward frequency analysis rather than two pointers.