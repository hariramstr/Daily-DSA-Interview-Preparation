# Longest Gaming Session With Limited Rating Swings

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Sliding Window &nbsp;|&nbsp; **Tags:** Sliding Window, Monotonic Queue, Deque

---

## 🗂 Problem Overview
Given an array `ratings` and an integer `limit`, return the maximum length of a contiguous subarray whose maximum rating minus minimum rating is at most `limit`. The output is a single integer: the longest stable segment length. The challenge is not defining validity but maintaining window `max` and `min` efficiently while scanning up to `200000` elements. Any approach that recomputes extrema for many subarrays degrades too quickly.

## 🌍 Engineering Impact
This pattern shows up anywhere systems need bounded variability over a moving interval: matchmaking quality analysis, streaming anomaly detection, SLO burn-rate smoothing, market data volatility windows, and adaptive rate control. At scale, the issue is not just correctness but maintaining rolling extrema without repeatedly rescanning state. A naive implementation turns a linear stream into quadratic work, which is unacceptable in telemetry pipelines, online ranking diagnostics, or real-time fraud heuristics. The monotonic-queue approach enables single-pass processing with predictable memory, making it viable for high-throughput event streams and latency-sensitive services.

## 🔍 Problem Statement
You are given an integer array `ratings`, where `ratings[i]` is the opponent skill rating in the `i`-th consecutive match, and an integer `limit`. A contiguous segment `ratings[l..r]` is stable if:

`max(ratings[l..r]) - min(ratings[l..r]) <= limit`

Return the maximum possible length of any stable contiguous segment.

Constraints:

- `1 <= ratings.length <= 200000`
- `1 <= ratings[i] <= 1000000000`
- `0 <= limit <= 1000000000`

Examples:

- `ratings = [1200, 1215, 1190, 1220, 1265, 1230], limit = 40` → `4`
- `ratings = [1500, 1500, 1500, 1510, 1498, 1499], limit = 12` → `6`

Edge cases matter: `limit = 0` means all values in the window must be identical; repeated values are common; and the input size rules out checking every subarray or recomputing min/max after each pointer move.

## 🪜 How to Solve This
1. Read the condition carefully → validity depends on a **contiguous** segment and only on that segment’s current `max` and `min`.

2. Brute force is the obvious baseline → for every left boundary, extend right and track extrema. That is still too slow in the worst case because there are `O(n^2)` windows.

3. Since the segment is contiguous, think **sliding window / two pointers** → expand the right edge one match at a time, and only shrink the left edge when the window becomes invalid.

4. The real problem is not moving pointers; it is answering: “what are the current window max and min?” after each move, without rescanning the window.

5. That suggests **monotonic deques**:
   - one deque keeps candidates for the maximum in decreasing order,
   - one deque keeps candidates for the minimum in increasing order.

6. When a new rating enters, remove weaker candidates from the back because they can never become the max/min while the new value remains in the window.

7. When the window violates `max - min <= limit`, advance `left` and evict expired indices from deque fronts.

8. Every index enters and leaves each deque once, so the whole pass stays linear.

## 🧩 Algorithm Walkthrough
1. **Initialize the sliding window.**  
   Set `left = 0`, `best = 0`, and create two deques storing indices:
   - `maxDeque`: ratings at those indices are in decreasing order.
   - `minDeque`: ratings at those indices are in increasing order.  
   These deques represent the current window’s extrema candidates.

2. **Expand the window with `right`.**  
   For each `right` from `0` to `n - 1`, insert `ratings[right]` into both deques:
   - While the back of `maxDeque` points to a value smaller than `ratings[right]`, pop it.
   - While the back of `minDeque` points to a value larger than `ratings[right]`, pop it.  
   This preserves monotonic order and ensures deque fronts always hold the current max/min.

3. **Check validity.**  
   The window `[left, right]` is invalid if  
   `ratings[maxDeque[0]] - ratings[minDeque[0]] > limit`.

4. **Shrink until valid.**  
   While invalid, increment `left`. Before or after moving it, remove deque fronts whose indices are now left of the window.  
   This maintains the invariant that both deque fronts belong to the active window.

5. **Record the answer.**  
   Once valid, update `best = max(best, right - left + 1)`.

6. **Why this is correct.**  
   This is a **Two Pointers + Monotonic Queue** solution. Two pointers give the maximal valid window ending at each `right`; monotonic deques give exact window extrema in `O(1)` amortized time. Because each index is pushed and popped at most once per deque, the algorithm is `O(n)` and preserves the exact validity condition throughout.

## 📊 Worked Example
Example: `ratings = [1200, 1215, 1190, 1220, 1265, 1230]`, `limit = 40`

| right | rating | left after shrink | max | min | valid window | best |
|---|---:|---:|---:|---:|---|---:|
| 0 | 1200 | 0 | 1200 | 1200 | `[1200]` | 1 |
| 1 | 1215 | 0 | 1215 | 1200 | `[1200,1215]` | 2 |
| 2 | 1190 | 0 | 1215 | 1190 | `[1200,1215,1190]` | 3 |
| 3 | 1220 | 0 | 1220 | 1190 | `[1200,1215,1190,1220]` | 4 |
| 4 | 1265 | 4 | 1265 | 1265 | `[1265]` | 4 |
| 5 | 1230 | 4 | 1265 | 1230 | `[1265,1230]` | 4 |

At `right = 4`, the window range becomes `1265 - 1190 = 75`, which exceeds `40`. Shrinking from the left continues until only `[1265]` remains. The best valid length observed is `4`.

## ⏱ Complexity Analysis
### Time Complexity
`O(n)`. Each index is appended once to `maxDeque` and `minDeque`, and removed at most once from each. The dominant work is deque maintenance during the single left-to-right scan. This remains practical at `10^6` elements; at `10^9`, linear time is still theoretically optimal but operationally constrained by memory bandwidth and I/O.

### Space Complexity
`O(n)` in the worst case for the two deques, though typically less. The space is owned entirely by monotonic candidate indices for the active window. It cannot be reduced to `O(1)` while preserving exact rolling min/max under arbitrary insertions and expirations without sacrificing time complexity.

## 💡 Key Takeaways
- If the problem asks for the longest/shortest **contiguous** range under a threshold, start with a sliding-window mental model.
- If window validity depends on both rolling `min` and `max`, that is a strong signal for **monotonic deques**, not heaps or rescans.
- Store **indices**, not values, so you can evict elements that fall out of the left side of the window.
- Be precise about the shrink condition: invalidate only when `max - min > limit`; equality is still valid.
- The production lesson is broader than this problem: maintaining compact, ordered summaries of a moving window is often the difference between stream-native linear processing and systems that collapse under repeated recomputation.

## 🚀 Variations & Further Practice
- Return the actual window boundaries, not just the length; the twist is preserving deterministic tie-breaking while keeping the same `O(n)` structure.
- Count how many subarrays satisfy `max - min <= limit`; the harder part is turning the maximal valid window at each `right` into a combinatorial count.
- Replace `max - min` with median- or percentile-based stability; the conceptual jump is that monotonic queues no longer suffice, so you need balanced trees, indexed heaps, or order-statistics structures.