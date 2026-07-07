# Longest Camera Feed With Limited Motion Zones

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Sliding Window &nbsp;|&nbsp; **Tags:** Sliding Window, Hash Map, Two Pointers

---

## 🗂 Problem Overview
Given an array `zones`, where each element is the motion zone reported for one second, return the maximum length of any contiguous interval containing at most `k` distinct zone IDs. The interval must be continuous, so reordering or skipping elements is not allowed. The challenge is scale: with up to `2 * 10^5` readings, any approach that checks many subarrays explicitly will time out, so the solution must maintain validity incrementally.

## 🌍 Engineering Impact
This pattern shows up anywhere systems need the longest recent span that satisfies a bounded-diversity constraint: streaming telemetry, fraud detection windows, noisy sensor aggregation, CDN request classification, and observability pipelines. In production, the difference between rescanning windows and maintaining them incrementally is the difference between linear throughput and collapse under load. Sliding-window state lets you process event streams online, bound memory by active diversity rather than total history, and make low-latency decisions without materializing every candidate interval or replaying data repeatedly.

## 🔍 Problem Statement
You are given an integer array `zones` and an integer `k`. Each `zones[i]` is the motion zone ID detected at second `i`. You must return the length of the longest contiguous subarray containing at most `k` distinct values.

Constraints:

- `1 <= zones.length <= 2 * 10^5`
- `1 <= zones[i] <= 10^9`
- `1 <= k <= zones.length`

Examples:

- `zones = [4, 2, 4, 3, 2, 2, 4], k = 2` → `3`
- `zones = [7, 7, 8, 9, 8, 8, 7, 7], k = 2` → `4`

Important edge cases:

- All values identical → answer is the full array length.
- `k = 1` → longest run containing only one distinct zone.
- `k >= number of distinct zones in the whole array` → answer is the full array.

The key constraint is the input size: quadratic enumeration of subarrays is not viable, which forces a linear-time or near-linear streaming approach.

## 🪜 How to Solve This
1. Read the problem → the word *contiguous* immediately rules out set-based or sorting-based tricks. This is a subarray problem.
2. The condition is “at most `k` distinct values” → that suggests tracking frequencies inside a moving range, not recomputing distinct counts from scratch.
3. If a range is valid and you extend it rightward, it may become invalid only when a new distinct zone appears → that is the classic signal for a sliding window.
4. Use two pointers: `left` and `right`. Expand `right` one step at a time, adding the new zone into a frequency map.
5. If the map now contains more than `k` distinct zones, shrink from the left until the window becomes valid again.
6. At every step, once the window is valid, its length is a candidate answer.
7. Why this works: each pointer only moves forward. You never revisit work, and the map gives constant-time updates to the distinct-zone count.

Once you see “longest contiguous segment with at most K distinct,” the right mental model is: maintain the largest valid window ending at each position.

## 🧩 Algorithm Walkthrough
1. **Initialize state**  
   Set `left = 0`, `best = 0`, and create a hash map `freq` from zone ID to count.  
   **Invariant:** `freq` always represents counts for the current window `zones[left..right]`.

2. **Expand the window with `right`**  
   For each index `right`, increment `freq[zones[right]]`.  
   **Why:** this incorporates the newest second into the candidate interval.  
   **Invariant:** before shrinking, the window may be invalid, but it includes every element from `left` through `right`.

3. **Detect invalidity by distinct count**  
   If `freq.size() > k`, the window has too many distinct zones.  
   **Why:** the problem constraint is on distinct values, not total length or sum, so the map size is the validity test.

4. **Shrink from the left until valid**  
   While `freq.size() > k`, decrement `freq[zones[left]]`, remove the key if its count reaches zero, then increment `left`.  
   **Why it’s correct:** removing from the left is the only way to restore validity while preserving contiguity for a window ending at `right`.  
   **Invariant:** after the loop, `zones[left..right]` is the longest valid window ending at `right`.

5. **Update the best answer**  
   Compute `right - left + 1` and update `best`.  
   **Why:** once valid, this window is the maximal valid suffix ending at `right`; any shorter valid suffix is dominated.

6. **Pattern fit**  
   This is a **Sliding Window + Hash Map + Two Pointers** problem. The abstraction is right because the validity condition is monotonic under left-shrinking: once a window is invalid, advancing `left` can only reduce distinctness, never increase it.

## 📊 Worked Example
Example: `zones = [4, 2, 4, 3, 2, 2, 4]`, `k = 2`

| right | zones[right] | action                         | left | freq              | valid? | best |
|------:|--------------:|--------------------------------|-----:|-------------------|:------:|-----:|
| 0     | 4             | add 4                          | 0    | {4:1}             | yes    | 1    |
| 1     | 2             | add 2                          | 0    | {4:1, 2:1}        | yes    | 2    |
| 2     | 4             | add 4                          | 0    | {4:2, 2:1}        | yes    | 3    |
| 3     | 3             | add 3, shrink until valid      | 2    | {4:1, 3:1}        | yes    | 3    |
| 4     | 2             | add 2, shrink until valid      | 3    | {3:1, 2:1}        | yes    | 3    |
| 5     | 2             | add 2                          | 3    | {3:1, 2:2}        | yes    | 3    |
| 6     | 4             | add 4, shrink until valid      | 4    | {2:2, 4:1}        | yes    | 3    |

The maximum valid window length observed is `3`.

## ⏱ Complexity Analysis
### Time Complexity
The algorithm runs in `O(n)` time. Each element is added to the window once by `right` and removed at most once by `left`, so the total number of pointer moves is linear. At `10^6` elements this is practical; at `10^9`, even linear scans become infrastructure decisions rather than just algorithm choices.

### Space Complexity
The space complexity is `O(min(n, k))`, driven by the frequency map for the current window’s distinct zone IDs. In practice it is bounded by the number of distinct values simultaneously present. You cannot reduce this much further without losing constant-time distinct-count maintenance.

## 💡 Key Takeaways
- If the problem asks for a **longest contiguous subarray** under an **“at most K distinct”** constraint, think sliding window immediately.
- When validity depends on element frequencies inside a moving range, a hash map plus two pointers is usually the right primitive.
- Remove keys from the map when their count reaches zero; otherwise `freq.size()` overstates distinctness and the window never becomes valid.
- Update the answer after restoring validity, not before, or you will count illegal windows and introduce off-by-one errors.
- At scale, incremental window maintenance is the production lesson: preserve just enough state to answer online, instead of rescanning history.

## 🚀 Variations & Further Practice
- **Longest substring with exactly `k` distinct characters** — same window mechanics, but you must distinguish “at most” from “exactly,” often by deriving exactly-`k` from two at-most windows.
- **Minimum window containing all required values** — still sliding window, but the objective flips from maximizing length to minimizing it, and validity depends on satisfying target counts.
- **Subarrays with at most `k` distinct values: count all of them** — harder because you aggregate counts of valid endings rather than just tracking the maximum length.