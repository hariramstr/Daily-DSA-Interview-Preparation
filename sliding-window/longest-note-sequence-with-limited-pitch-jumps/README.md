# Longest Note Sequence With Limited Pitch Jumps

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Sliding Window &nbsp;|&nbsp; **Tags:** Sliding Window, Monotonic Queue, Array

---

## 🗂 Problem Overview
Given an integer array `notes` and an integer `limit`, find the maximum length of a contiguous subarray whose pitch range stays bounded: `max(window) - min(window) <= limit`. The output is a single integer: the longest valid window length. The challenge is maintaining window validity while scanning up to `200,000` elements. Recomputing min and max for every candidate window is too slow, so the solution must update range information incrementally as the window moves.

## 🌍 Engineering Impact
This pattern shows up anywhere systems need longest valid spans under bounded variance: telemetry smoothing in streaming pipelines, fraud/risk windows over transaction scores, QoS monitoring over latency samples, and ranking or recommendation systems that segment stable behavior intervals. At scale, naive rescans turn a linear ingest path into quadratic collapse. The monotonic-queue sliding window preserves exact min/max under continuous append-and-evict operations, which is the same operational shape as stream processors, online anomaly detectors, and real-time observability backends. The value is not just asymptotic improvement; it enables predictable throughput and bounded per-event work.

## 🔍 Problem Statement
You are given:

- `notes`, an array of integers where `notes[i]` is the pitch played at time `i`
- `limit`, the maximum allowed difference between the highest and lowest pitch in a contiguous segment

Return the length of the longest contiguous subarray `notes[l..r]` such that:

- `max(notes[l..r]) - min(notes[l..r]) <= limit`

Constraints:

- `1 <= notes.length <= 200000`
- `0 <= notes[i] <= 1000000000`
- `0 <= limit <= 1000000000`

Examples:

- `notes = [12, 14, 13, 18, 15, 16], limit = 3` → `3`
- `notes = [7, 7, 8, 9, 6, 7, 8], limit = 2` → `4`

Edge conditions matter: arrays of length `1` always return `1`; repeated values are valid; `limit = 0` means every valid segment must contain identical values only. The key constraint is `notes.length = 200000`, which rules out any approach that repeatedly scans windows to recompute min and max.

## 🪜 How to Solve This
1. Start with the brute-force mental model → for every right endpoint, expand leftward and track whether the window range stays within `limit`.  
2. Notice what makes brute force expensive → the validity test depends on the current minimum and maximum, and recomputing those for each window is the bottleneck.  
3. Recognize the shape → this is a contiguous-window optimization problem with a condition that becomes invalid as the window grows, so sliding window / two pointers should be the first candidate.  
4. Ask what state the window needs → only the current min and max matter, not the full ordering.  
5. Now the real issue becomes data structure choice → we need to support:
   - append a new value on the right
   - remove expired values from the left
   - query min and max in O(1)
6. That points directly to two monotonic deques:
   - one decreasing deque for the maximum
   - one increasing deque for the minimum
7. As the right pointer advances, push the new note into both deques while preserving monotonic order. If `max - min > limit`, move the left pointer forward until valid again, evicting stale indices from deque fronts.
8. Track the largest valid window length seen. Linear scan, no rescans, exact result.

## 🧩 Algorithm Walkthrough
1. **Use the Sliding Window / Two Pointers pattern.**  
   Maintain a window `[left, right]` that always represents the current candidate segment. This is the right abstraction because the problem asks for the longest **contiguous** range under a constraint that can be restored by advancing `left`.

2. **Maintain a decreasing deque for maximum candidates.**  
   Store indices, not values. Before pushing `right`, pop from the back while `notes[back] < notes[right]`. The deque front is always the index of the maximum value in the current window. This works because smaller trailing values can never become the max while the new larger value remains in the window.

3. **Maintain an increasing deque for minimum candidates.**  
   Symmetrically, pop from the back while `notes[back] > notes[right]`. The front is always the current minimum. Larger trailing values are dominated and can be discarded.

4. **Expand the window by advancing `right`.**  
   After inserting `notes[right]` into both deques, the window may become invalid if `notes[maxDeque.front] - notes[minDeque.front] > limit`.

5. **Shrink from the left until valid.**  
   While invalid, increment `left`. If either deque front points to an index now left of the window, pop it from the front. The invariant after this loop: both deque fronts lie inside `[left, right]`, and their values define the exact max and min of the window.

6. **Update the answer.**  
   Once valid, compute `right - left + 1` and keep the maximum. Every index enters and leaves each deque at most once, giving linear time.

## 📊 Worked Example
Example: `notes = [12, 14, 13, 18, 15, 16]`, `limit = 3`

| right | note | maxDeque (values) | minDeque (values) | left | valid? | best |
|---|---:|---|---|---:|---|---:|
| 0 | 12 | [12] | [12] | 0 | yes | 1 |
| 1 | 14 | [14] | [12, 14] | 0 | yes (`14-12=2`) | 2 |
| 2 | 13 | [14, 13] | [12, 13] | 0 | yes (`14-12=2`) | 3 |
| 3 | 18 | [18] | [12, 13, 18] | 0 | no (`18-12=6`) | 3 |
| shrink | — | [18] | [13, 18] | 1 | no (`18-13=5`) | 3 |
| shrink | — | [18] | [18] | 3 | yes (`18-18=0`) | 3 |
| 4 | 15 | [18, 15] | [15] | 3 | yes (`18-15=3`) | 3 |
| 5 | 16 | [18, 16] | [15, 16] | 3 | yes (`18-15=3`) | 3 |

Longest valid length is `3`.

## ⏱ Complexity Analysis
### Time Complexity
`O(n)`, where `n = notes.length`. Each index is pushed and popped from each deque at most once, so the dominant work is amortized constant-time deque maintenance during a single left-to-right scan. At `10^6` elements this remains practical; at `10^9`, linear time is still the lower-bound shape, but memory bandwidth and streaming architecture dominate.

### Space Complexity
`O(n)` in the worst case, owned by the two deques storing candidate indices. In practice, deque size is bounded by window structure, but worst-case monotonic input can retain many indices. You cannot reduce this to `O(1)` while preserving exact online min/max under arbitrary evictions.

## 💡 Key Takeaways
• If the problem asks for the longest **contiguous** region under a threshold and validity can be restored by moving one boundary, think sliding window immediately.  
• If window validity depends on dynamic `min` and `max`, monotonic deques are the signal that plain two pointers alone are insufficient.  
• Store **indices**, not just values, or you cannot evict elements correctly when `left` advances past duplicates.  
• Update deques before checking validity, and when shrinking, remove stale front indices only after `left` moves; this is where most off-by-one bugs land.  
• The production-grade insight is to pair a moving window with data structures that preserve exactly the aggregates you need under append-and-evict, rather than recomputing state on every adjustment.

## 🚀 Variations & Further Practice
- **Return the actual window bounds, not just length.** Same core pattern, but now tie-breaking and stable index tracking matter when multiple optimal windows exist.  
- **Replace `max - min <= limit` with a median- or percentile-based constraint.** Harder because monotonic deques no longer suffice; you need balanced trees, heaps with lazy deletion, or order-statistics structures.  
- **Count how many valid subarrays exist instead of the longest one.** Same sliding-window validity logic, but the aggregation changes from `max length` to summing `right - left + 1` for each endpoint.