# Longest Packing Streak With Limited Heavy Boxes

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Arrays &nbsp;|&nbsp; **Tags:** Sliding Window, Two Pointers, Subarray

---

## 🗂 Problem Overview
Given an array `weights`, a threshold `limit`, and an integer `k`, find the maximum length of a contiguous subarray containing at most `k` values strictly greater than `limit`. The output is a single integer: the longest valid streak length. The challenge is that `weights.length` can reach `200000`, so any approach that checks many subarrays explicitly becomes too slow. The key is to maintain validity incrementally as the window expands and contracts.

## 🌍 Engineering Impact
This pattern shows up anywhere systems must preserve order while enforcing bounded violations inside a moving range: stream processing with limited malformed events, rate-limited request bursts with a capped number of expensive operations, log pipelines tolerating a small number of high-severity records per batch, or warehouse and fulfillment scheduling under equipment constraints. At scale, brute-force range evaluation collapses under quadratic behavior and destroys latency budgets. A sliding-window formulation enables single-pass processing, predictable memory use, and online decision-making, which matters for real-time pipelines, backpressure control, and operational dashboards built over continuously arriving data.

## 🔍 Problem Statement
You are given:

- `weights`: an integer array where `weights[i]` is the weight of the `i`-th box
- `limit`: a box is **heavy** if `weights[i] > limit`
- `k`: the maximum number of heavy boxes allowed in a contiguous streak

Return the length of the longest contiguous subarray whose count of heavy boxes is at most `k`.

Constraints:

- `1 <= weights.length <= 200000`
- `0 <= weights[i] <= 1000000000`
- `0 <= limit <= 1000000000`
- `0 <= k <= weights.length`

Examples:

- `weights = [4, 9, 3, 12, 7, 15, 2], limit = 8, k = 2` → `5`
- `weights = [11, 5, 6, 13, 14, 2, 1], limit = 10, k = 1` → `3`

Edge cases matter: `k = 0` means no heavy boxes are allowed, all boxes may be light, or all may be heavy. The input size rules out nested subarray scans; that constraint drives the algorithmic choice.

## 🪜 How to Solve This
1. Read the problem → we need a **contiguous** range, not an arbitrary subset. That immediately suggests subarray techniques, not sorting or greedy reordering.

2. The rule on a range is simple: count how many elements satisfy `weights[i] > limit`. If that count stays `<= k`, the range is valid.

3. Brute force would examine every start/end pair and count heavy boxes repeatedly. With up to `200000` elements, that is dead on arrival.

4. Notice the validity condition is monotonic with respect to window growth: when you extend the right edge, the heavy count either stays the same or increases by one. If the window becomes invalid, moving the left edge rightward is the only way to restore validity.

5. That monotonic behavior is the signal for a **sliding window / two pointers** solution.

6. Maintain a window `[left, right]` and the number of heavy boxes inside it. Expand `right` one step at a time. Whenever heavy boxes exceed `k`, advance `left` until the window is valid again.

7. After each adjustment, the current window is the longest valid one ending at `right`, so update the best length.

## 🧩 Algorithm Walkthrough
1. **Initialize state**  
   Set `left = 0`, `heavy = 0`, and `best = 0`. The active window is empty.  
   Invariant: before processing each new position, `heavy` equals the number of heavy boxes in `weights[left..right]` once the window is valid.

2. **Expand the window with `right`**  
   Iterate `right` from `0` to `n - 1`. If `weights[right] > limit`, increment `heavy`.  
   Why correct: this updates the window summary in O(1) as the right boundary grows.

3. **Restore validity when needed**  
   While `heavy > k`, move `left` rightward. If `weights[left] > limit`, decrement `heavy` before incrementing `left`.  
   Why correct: any valid window ending at `right` must start at or after the first position that brings heavy count back to `<= k`. Shrinking from the left is the minimal repair.

4. **Record the best valid length**  
   Once the loop exits, `weights[left..right]` is valid. Compute `right - left + 1` and update `best`.  
   Invariant: after shrinking, the current window is the longest valid window ending at `right`, because any earlier start would violate the heavy-box limit.

5. **Return `best`**  
   This is the maximum over all valid windows encountered.

This is a classic **Sliding Window / Two Pointers** problem: ordered data, contiguous range, and a constraint that can be maintained incrementally with local updates.

## 📊 Worked Example
Example: `weights = [4, 9, 3, 12, 7, 15, 2]`, `limit = 8`, `k = 2`

| right | weights[right] | heavy after add | left after shrink | window        | length | best |
|------:|----------------:|----------------:|------------------:|---------------|-------:|-----:|
| 0     | 4               | 0               | 0                 | `[4]`         | 1      | 1    |
| 1     | 9               | 1               | 0                 | `[4,9]`       | 2      | 2    |
| 2     | 3               | 1               | 0                 | `[4,9,3]`     | 3      | 3    |
| 3     | 12              | 2               | 0                 | `[4,9,3,12]`  | 4      | 4    |
| 4     | 7               | 2               | 0                 | `[4,9,3,12,7]`| 5      | 5    |
| 5     | 15              | 3               | 2                 | `[3,12,7,15]` | 4      | 5    |
| 6     | 2               | 2               | 2                 | `[3,12,7,15,2]`| 5     | 5    |

At `right = 5`, the window becomes invalid with 3 heavy boxes, so `left` advances until only 2 remain. The maximum valid streak length is `5`.

## ⏱ Complexity Analysis
### Time Complexity
`O(n)`, where `n = weights.length`. Each element is processed once when `right` advances and at most once again when `left` advances. There is no nested rescanning. At `10^6` elements this is routine; at `10^9`, linear time is still expensive but remains the only viable asymptotic shape for a single-pass exact solution.

### Space Complexity
`O(1)`. The algorithm stores only pointer indices, a heavy-count accumulator, and the best length. No auxiliary arrays or maps are required. Space cannot be meaningfully reduced further; the main trade-off is not memory but whether you need online processing versus storing richer per-window diagnostics.

## 💡 Key Takeaways
- If the problem asks for the longest or shortest **contiguous** range under a bounded-count constraint, sliding window should be your first candidate.
- A strong signal is a window property that can be updated incrementally when one element enters or leaves the range.
- Heavy is defined as `weights[i] > limit`, not `>= limit`; that comparison is the easiest place to silently change the answer.
- Update `best` only after shrinking until `heavy <= k`; recording length too early captures invalid windows.
- The production lesson is broader than arrays: when a policy over ordered events is locally maintainable, you can usually replace expensive global rescans with streaming state and bounded repair.

## 🚀 Variations & Further Practice
- Find the **shortest** contiguous streak containing at least `k` heavy boxes. The conceptual twist is that the optimization direction flips, so you shrink aggressively once the constraint is satisfied.
- Allow multiple thresholds, such as at most `k1` boxes above `limit1` and at most `k2` above `limit2`. The harder part is maintaining several coupled window invariants correctly.
- Replace the heavy-box count with a weighted cost budget, where each box contributes variable penalty instead of `0/1`. This generalizes the window state from a simple count to an additive metric.