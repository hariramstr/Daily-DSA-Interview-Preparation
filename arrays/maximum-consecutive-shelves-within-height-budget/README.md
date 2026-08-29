# Maximum Consecutive Shelves Within Height Budget

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Arrays &nbsp;|&nbsp; **Tags:** Sliding Window, Monotonic Queue, Deque

---

## 🗂 Problem Overview
Given an array `heights` and an integer `limit`, find the maximum length of a contiguous subarray whose maximum value minus minimum value is at most `limit`. The output is a single integer: the longest valid window size. The challenge is not checking one window, but maintaining min and max efficiently as the window expands and contracts. With up to `100000` elements, recomputing extrema per window is too slow.

## 🌍 Engineering Impact
This pattern shows up anywhere systems need bounded variability over a moving range: streaming anomaly detection, market data smoothing, observability pipelines, adaptive bitrate windows, and search/ranking freshness guards. The operational requirement is usually: keep the longest recent span whose spread stays within policy. Without an efficient sliding min/max structure, implementations degrade into repeated rescans, causing latency spikes and poor tail behavior under sustained throughput. Monotonic deques enable linear-time processing with predictable memory, which matters in stream processors, edge services, and online decision systems where windows shift continuously and recomputation is unacceptable.

## 🔍 Problem Statement
You are given:

- `heights`, an array of product heights
- `limit`, the maximum allowed difference between the tallest and shortest product in a chosen contiguous block

Return the length of the longest subarray `heights[l..r]` such that:

`max(heights[l..r]) - min(heights[l..r]) <= limit`

Constraints:

- `1 <= heights.length <= 100000`
- `1 <= heights[i] <= 1000000000`
- `0 <= limit <= 1000000000`

Examples:

- `heights = [4, 7, 6, 8, 5, 9], limit = 3` → `4`
- `heights = [10, 1, 2, 4, 7, 2], limit = 5` → `4`

Edge cases matter: a single element is always valid, `limit = 0` means all values in the window must be identical, and large values rule out any approach that depends on value-range bucketing. The key constraint is array length `100000`, which forces better than quadratic behavior.

## 🪜 How to Solve This
1. Start with the brute-force instinct → for every left boundary, extend right and track max/min.  
   That works logically, but repeated max/min recomputation makes it `O(n^2)` in the worst case.

2. Notice the structure → we need the **longest contiguous window** satisfying a condition.  
   That is a classic signal for **sliding window / two pointers**.

3. Ask what blocks a normal sliding window → when we add a new rightmost element, we must know the current window’s min and max quickly.  
   A plain window does not give that for free.

4. Reach for the right data structure → maintain:
   - a decreasing deque for candidate maxima
   - an increasing deque for candidate minima

5. As the right pointer advances, remove dominated values from the back of each deque.  
   This keeps only useful candidates.

6. If `max - min > limit`, move the left pointer right until the window is valid again, removing expired indices from deque fronts.

7. Track the largest valid window length seen.  
   Each index enters and leaves each deque once, giving linear time.

## 🧩 Algorithm Walkthrough
1. **Use the Sliding Window + Monotonic Deque pattern.**  
   Maintain a window `[left, right]` that is always adjusted toward validity. This is the right abstraction because the problem asks for a longest contiguous segment under a window-local constraint.

2. **Track maxima with a decreasing deque of indices.**  
   Before pushing `right`, pop from the back while `heights[back] < heights[right]`. Those smaller values can never become the maximum while the new element remains in the window.  
   **Invariant:** deque front always holds the index of the current maximum.

3. **Track minima with an increasing deque of indices.**  
   Before pushing `right`, pop from the back while `heights[back] > heights[right]`. Those larger values are dominated for future minimum queries.  
   **Invariant:** deque front always holds the index of the current minimum.

4. **Expand the window by moving `right` one step at a time.**  
   After inserting the new index into both deques, compute the spread using deque fronts:  
   `heights[maxDeque[0]] - heights[minDeque[0]]`.

5. **Shrink from the left while invalid.**  
   If the spread exceeds `limit`, increment `left` until valid again. Before or after incrementing, remove deque fronts whose indices are now left of the window.  
   **Invariant:** all indices in both deques lie within `[left, right]`.

6. **Update the answer after restoring validity.**  
   Every valid window ending at `right` is represented by the current `[left, right]`; its length is `right - left + 1`. Keep the maximum over all `right`.

7. **Why correctness holds.**  
   The deques always expose exact min and max for the current window, and `left` only moves forward when required. Therefore every candidate longest valid window is considered once, without redundant rescans.

## 📊 Worked Example
Example: `heights = [4, 7, 6, 8, 5, 9]`, `limit = 3`

| right | value | maxDeque values | minDeque values | left | valid? | best |
|---|---:|---|---|---:|---|---:|
| 0 | 4 | [4] | [4] | 0 | yes | 1 |
| 1 | 7 | [7] | [4, 7] | 0 | yes (`7-4=3`) | 2 |
| 2 | 6 | [7, 6] | [4, 6] | 0 | yes (`7-4=3`) | 3 |
| 3 | 8 | [8] | [4, 6, 8] | 1 | yes after shrink (`8-6=2`) | 3 |
| 4 | 5 | [8, 5] | [5] | 1 | yes (`8-5=3`) | 4 |
| 5 | 9 | [9] | [5, 9] | 4 | no → shrink to valid (`9-5=4`, then `9-9=0`) | 4 |

The longest valid window found is `[7, 6, 8, 5]`, length `4`.

## ⏱ Complexity Analysis
### Time Complexity
`O(n)`. Each index is pushed into each deque once and popped from each deque at most once. The dominant work is deque maintenance plus a single left/right scan. At `10^6` elements this remains practical; at `10^9`, linear time is still expensive but fundamentally optimal for one-pass exact evaluation.

### Space Complexity
`O(n)` in the worst case for the two deques, though in practice each stores only active candidates within the current window. This cannot be reduced to `O(1)` while preserving exact online min/max updates; the trade-off would be slower recomputation or approximate summaries.

## 💡 Key Takeaways
- If the problem asks for the longest/shortest **contiguous** range under a window-local constraint, start with two pointers before considering heavier structures.
- If validity depends on both current **minimum and maximum**, that is a strong signal for paired monotonic deques rather than heaps or rescanning.
- Store **indices**, not values, so you can evict elements that fall out of the left side of the window.
- Shrink in a `while`, not an `if`; one left move may not be enough after adding a large outlier.
- In production systems, monotonic queues are the right tool when you need exact rolling extrema with bounded latency and no per-window recomputation.

## 🚀 Variations & Further Practice
- **Sliding Window Maximum / Minimum**: same deque machinery, but now emit the extremum for every fixed-size window instead of finding the longest valid one.
- **Longest subarray with absolute difference less than or equal to limit using heaps or balanced trees**: same objective, but compare operational trade-offs between `O(n)` deques and `O(n log n)` ordered structures.
- **2D window constraints over matrices or images**: extend rolling min/max to rectangular regions; the conceptual twist is composing monotonic structures across two dimensions.