# Longest Subtitle Segment Within Reading Burst Limit

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Sliding Window &nbsp;|&nbsp; **Tags:** Sliding Window, Two Pointers, Array

---

## 🗂 Problem Overview
Given an array `words`, find the maximum length of a contiguous segment whose total word count is at most `maxWords` and which contains at most `k` dense frames, where a frame is dense if `words[i] >= denseThreshold`. Return only the segment length. The non-trivial part is satisfying two window constraints simultaneously under input sizes up to `200000`, which rules out quadratic enumeration and pushes toward a linear sliding-window strategy.

## 🌍 Engineering Impact
This pattern shows up anywhere systems must maximize contiguous throughput under multiple bounded resources: media playback buffering, API burst control, log ingestion windows, ad-serving pacing, and streaming analytics with per-batch caps. In production, brute-force range evaluation collapses under high-cardinality streams because every new event would trigger too much recomputation. A sliding window turns the problem into incremental state maintenance: admit one item, evict until valid, update the best answer. That enables predictable latency, bounded memory, and online processing, which matters when decisions must be made continuously rather than after full-batch aggregation.

## 🔍 Problem Statement
You are given:

- `words[i]`: number of words in the `i`-th subtitle frame
- `maxWords`: maximum total words allowed in a contiguous segment
- `k`: maximum number of dense frames allowed in that segment
- `denseThreshold`: a frame is dense if `words[i] >= denseThreshold`

Return the length of the longest contiguous segment satisfying both:

1. `sum(segment) <= maxWords`
2. `denseFrameCount(segment) <= k`

Constraints:

- `1 <= words.length <= 200000`
- `0 <= words[i] <= 100000`
- `0 <= maxWords <= 10^14`
- `0 <= k <= words.length`
- `1 <= denseThreshold <= 100000`

Examples:

- `words = [4, 9, 3, 8, 2, 7], maxWords = 20, k = 2, denseThreshold = 7` → `4`
- `words = [6, 1, 7, 2, 9, 1, 1], maxWords = 10, k = 1, denseThreshold = 6` → `3`

The key algorithmic driver is the `200000` upper bound: checking all subarrays is infeasible.

## 🪜 How to Solve This
1. Read the problem → we need the longest **contiguous** segment, so this is immediately a window problem, not sorting or DP.

2. Notice both constraints are additive over a range:
   - total words in the window
   - count of dense frames in the window

3. For additive constraints on a contiguous range with non-negative values, think **sliding window / two pointers**. Why? Expanding the right side only increases or preserves both metrics, so once a window becomes invalid, moving `left` forward is the only way to restore validity.

4. Maintain running state for the current window:
   - `currentSum`
   - `denseCount`

5. For each `right` index:
   - include `words[right]`
   - if the frame is dense, increment `denseCount`
   - while either constraint is violated, move `left` rightward and subtract its contribution

6. After shrinking, the window is valid again. Its length is a candidate answer.

7. Because each index enters and leaves the window at most once, the whole process is linear.

The key mental move is recognizing that the constraints are monotone under expansion and shrinkage, which makes two pointers the natural abstraction.

## 🧩 Algorithm Walkthrough
1. **Initialize the window state.**  
   Set `left = 0`, `currentSum = 0`, `denseCount = 0`, and `best = 0`.  
   This represents an empty window `[left, right]` before processing begins.

2. **Expand with the right pointer.**  
   Iterate `right` from `0` to `n - 1`. Add `words[right]` to `currentSum`. If `words[right] >= denseThreshold`, increment `denseCount`.  
   This applies the standard **Two Pointers / Sliding Window** pattern: grow optimistically, then repair if invalid.

3. **Restore validity by shrinking from the left.**  
   While `currentSum > maxWords` or `denseCount > k`, remove `words[left]` from the window:
   - subtract it from `currentSum`
   - if it was dense, decrement `denseCount`
   - increment `left`  
   This is correct because all values are non-negative, so removing leftmost elements is the only way to reduce the violated metrics.

4. **Maintain the invariant.**  
   After the `while` loop, the current window `[left, right]` is valid:
   - `currentSum <= maxWords`
   - `denseCount <= k`  
   Also, `left` is the smallest index that makes this `right`-anchored window valid after repair.

5. **Update the answer.**  
   Compute `right - left + 1` and update `best` if larger. Since every valid window is considered exactly when its right boundary is processed, no candidate is missed.

6. **Why this abstraction fits.**  
   The problem asks for the longest contiguous range under monotone constraints. That is exactly where sliding windows dominate: constant-time state updates, no backtracking, and linear total pointer movement.

## 📊 Worked Example
Example: `words = [6, 1, 7, 2, 9, 1, 1]`, `maxWords = 10`, `k = 1`, `denseThreshold = 6`

| right | words[right] | currentSum | denseCount | left after shrink | valid window | best |
|---|---:|---:|---:|---:|---|---:|
| 0 | 6 | 6 | 1 | 0 | `[6]` | 1 |
| 1 | 1 | 7 | 1 | 0 | `[6,1]` | 2 |
| 2 | 7 | 14 | 2 | 1 | `[1,7]` | 2 |
| 3 | 2 | 10 | 1 | 1 | `[1,7,2]` | 3 |
| 4 | 9 | 19 | 2 | 4 | `[9]` | 3 |
| 5 | 1 | 10 | 1 | 4 | `[9,1]` | 3 |
| 6 | 1 | 11 | 1 | 5 | `[1,1]` | 3 |

The interesting step is `right = 4`: adding `9` breaks both constraints, so the window shrinks repeatedly until only `[9]` remains valid. The maximum valid length seen during the scan is `3`.

## ⏱ Complexity Analysis
### Time Complexity
`O(n)`. Each frame is added to the window once by `right` and removed at most once by `left`, so the dominant work is linear pointer movement with constant-time updates. At `10^6` elements this remains practical; at `10^9`, even linear scans become throughput- and memory-bandwidth-bound.

### Space Complexity
`O(1)`. The algorithm stores only scalar window state: two pointers, a running sum, dense-frame count, and the best length. Space cannot be meaningfully reduced further; the main trade-off is using wider integer types for safety, especially for `maxWords` and cumulative sums.

## 💡 Key Takeaways
- If the problem asks for the longest valid **contiguous** range under additive or count-based constraints, sliding window should be your first candidate.
- When all values are non-negative, window validity is monotone under expansion and shrinkage, which is the signal that two pointers will work in linear time.
- Use a 64-bit integer for the running sum; `words.length * words[i]` can exceed 32-bit range comfortably.
- Shrink while **either** constraint is violated, not just one; forgetting the combined condition produces subtly invalid windows.
- In production stream processors, this pattern is valuable because it converts repeated range recomputation into incremental state maintenance with stable latency.

## 🚀 Variations & Further Practice
- Allow negative `words[i]` values. The monotonicity that makes sliding window work disappears, pushing the solution toward prefix sums plus balanced structures or binary search over feasibility.
- Replace “at most `k` dense frames” with “exactly `k` dense frames.” Now you need counting logic or a transformed at-most/exactly relationship rather than a single validity window.
- Ask for the number of valid segments instead of the longest one. Same window mechanics, but the accumulation rule changes from `max(length)` to counting all valid suffixes ending at each `right`.