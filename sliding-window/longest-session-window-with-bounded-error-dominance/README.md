# Longest Session Window With Bounded Error Dominance

**Difficulty:** Hard &nbsp;|&nbsp; **Topic:** Sliding Window &nbsp;|&nbsp; **Tags:** Sliding Window, Hash Map, Frequency Counting

---

## 🗂 Problem Overview
Given an integer array `events` and a tolerance `k`, find the maximum length of a contiguous subarray where the most frequent event code is not excessively dominant. For any window, if `maxFreq` is the highest count of a single value and `len` is the window length, the window is stable when `2 * maxFreq <= len + k`. The challenge is to evaluate this condition over all contiguous windows under `2 * 10^5` input size, which rules out quadratic rescanning.

## 🌍 Engineering Impact
This pattern shows up in streaming telemetry, fraud detection, session-quality scoring, log anomaly suppression, and online moderation pipelines. You are continuously evaluating whether one category is overwhelming a recent slice of activity, but you need to do it incrementally, not by recomputing histograms for every candidate range. At production scale, the difference is architectural: an `O(n^2)` design collapses under sustained event streams, while a sliding-window frequency tracker supports near-real-time decisions, bounded memory growth per active window, and predictable latency under bursty workloads.

## 🔍 Problem Statement
You are given `events`, where `events[i]` is a positive integer event code observed at second `i`, and a non-negative integer `k`. A contiguous window `events[l..r]` is stable if, letting `len = r - l + 1` and `maxFreq` be the maximum frequency of any event code in that window, the condition `2 * maxFreq <= len + k` holds.

Return the length of the longest stable contiguous window.

Constraints:

- `1 <= events.length <= 2 * 10^5`
- `1 <= events[i] <= 10^9`
- `0 <= k <= events.length`

Examples:

- `events = [4, 1, 4, 2, 4, 3, 2], k = 1` → `5`
- `events = [7, 7, 7, 2, 3, 7, 4, 5], k = 0` → `5`

Important edge cases: `k = 0`, all values identical, all values distinct, and very large event codes requiring hash-based counting rather than array indexing. The input bound is what forces a linear or near-linear solution.

## 🪜 How to Solve This
1. Read the condition carefully → the only window property that matters is its length and the highest frequency inside it. That immediately suggests frequency counting, not sorting or prefix sums.

2. We need the **longest contiguous** window → that is a strong signal for a sliding window / two-pointers approach.

3. As we expand the right pointer, frequencies only increase for one event code at a time → easy to maintain with a hash map.

4. The hard part is `maxFreq`: recomputing it from scratch on every shrink would be too expensive. The standard move is to maintain a **non-decreasing upper bound** on `maxFreq` as the window grows.

5. If the window violates `2 * maxFreq <= len + k`, move the left pointer right and decrement counts until the window becomes valid again.

6. Why does the stale `maxFreq` trick still work? Because it may delay shrinking less aggressively, but it never causes us to miss the optimal answer. This is the same idea used in high-performance “longest replaceable substring” style windows.

7. Track the maximum valid window length seen during the scan. One pass, local updates, no nested rescans.

## 🧩 Algorithm Walkthrough
1. **Use the Sliding Window / Two Pointers pattern.**  
   Maintain a window `[left, right]` and expand `right` one step at a time. This is the right abstraction because the problem asks for the longest contiguous region satisfying a dynamic frequency constraint.

2. **Track frequencies with a hash map.**  
   Increment `freq[events[right]]` when adding a new event. This gives constant-average-time updates even when event codes are as large as `10^9`.

3. **Maintain `maxFreq` as the largest count ever seen for any value in the current expansion phase.**  
   Update with  
   `maxFreq = max(maxFreq, freq[events[right]])`.  
   This value may become stale after shrinking, but it is still safe for this longest-window formulation.

4. **Check the stability condition.**  
   Let `len = right - left + 1`. If `2 * maxFreq > len + k`, the window is too dominated by one code, so it cannot be accepted in its current size.

5. **Shrink from the left until the window is valid.**  
   Decrement `freq[events[left]]`, then increment `left`. Repeat while the inequality is violated. The invariant is: after shrinking, the current window satisfies the stability rule under the maintained bound.

6. **Record the best length.**  
   Once valid, update `answer = max(answer, len)`.

7. **Why correctness holds despite stale `maxFreq`.**  
   A stale `maxFreq` can only make the window appear more dominated than it really is, which may postpone recognizing validity but does not fabricate a larger impossible answer that survives the shrink logic. Each pointer moves at most `n` times, so the full scan is linear.

## 📊 Worked Example
Take `events = [4, 1, 4, 2, 4, 3, 2]`, `k = 1`.

| right | event | window `[left..right]` | freq summary | maxFreq | len | stable? |
|---|---:|---|---|---:|---:|---|
| 0 | 4 | `[0..0]` | `{4:1}` | 1 | 1 | no |
| 1 | 1 | `[0..1]` | `{4:1,1:1}` | 1 | 2 | yes |
| 2 | 4 | `[0..2]` | `{4:2,1:1}` | 2 | 3 | yes |
| 3 | 2 | `[0..3]` | `{4:2,1:1,2:1}` | 2 | 4 | yes |
| 4 | 4 | `[0..4]` | `{4:3,1:1,2:1}` | 3 | 5 | no → shrink |
| 4 | 4 | `[1..4]` | `{4:2,1:1,2:1}` | 3* | 4 | no → shrink |
| 4 | 4 | `[2..4]` | `{4:2,2:1}` | 3* | 3 | no → shrink |
| 4 | 4 | `[3..4]` | `{4:1,2:1}` | 3* | 2 | yes |
| 5 | 3 | `[3..5]` | `{4:1,2:1,3:1}` | 3* | 3 | yes |
| 6 | 2 | `[3..6]` | `{2:2,4:1,3:1}` | 3* | 4 | yes |

The best valid length encountered is `5`, from window `[1, 4, 2, 4, 3]`.

## ⏱ Complexity Analysis
### Time Complexity
`O(n)` average time, where `n = events.length`. Each element is added to the window once and removed at most once, so both pointers advance monotonically. Hash map updates dominate and are constant on average. This remains practical at `10^6` scale; at `10^9`, even linear scans become throughput- and memory-bound.

### Space Complexity
`O(m)`, where `m` is the number of distinct event codes inside the active scan, worst-case `O(n)`. The hash map owns the space. It cannot be reduced asymptotically without sacrificing constant-time frequency updates or requiring value compression as a preprocessing trade-off.

## 💡 Key Takeaways
- If the problem asks for the **longest contiguous range** under a frequency-based constraint, start by testing whether a sliding window can maintain the needed statistics incrementally.
- When the validity rule depends on the **maximum frequency of any value**, a hash map plus a monotonic `maxFreq` tracker is a strong pattern signal.
- The stability check is `2 * maxFreq <= windowLen + k`; getting the inequality direction wrong will silently accept invalid windows.
- Window length is `right - left + 1`; update it after shrinking, not before, or you will overcount stale invalid states.
- In production systems, the transferable idea is to preserve just enough summary state to make local admission/eviction decisions online, instead of recomputing global properties for every candidate interval.

## 🚀 Variations & Further Practice
- Require returning the actual window boundaries, with tie-breaking by earliest start or lexicographically smallest interval; the conceptual twist is preserving correctness while multiple optimal windows exist.
- Make `k` vary per window position or per event class; the harder part is that validity is no longer a simple scalar inequality over one global tolerance.
- Extend from one dominant-code constraint to top-`t` heavy hitters inside the window; now maintaining validity requires richer frequency structure than a single `maxFreq` summary.