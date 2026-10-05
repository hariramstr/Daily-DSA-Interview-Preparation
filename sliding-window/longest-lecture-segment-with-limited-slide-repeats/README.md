# Longest Lecture Segment With Limited Slide Repeats

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Sliding Window &nbsp;|&nbsp; **Tags:** Sliding Window, Hash Map, Two Pointers

---

## 🗂 Problem Overview
Given an array `slides`, where `slides[i]` is the slide shown at minute `i`, return the length of the longest contiguous segment in which every slide ID appears at most `k` times. The output is a single integer: the maximum valid window length. The non-trivial part is the contiguous constraint combined with large input size, which rules out brute-force enumeration of all subarrays and pushes toward a linear-time windowing strategy.

## 🌍 Engineering Impact
This pattern shows up anywhere systems need the longest valid interval under bounded repetition: stream analytics, sessionization, observability pipelines, fraud detection windows, media playback summarization, and rate-control logic. In production, the key challenge is preserving order while enforcing frequency constraints over a moving interval. Without a sliding-window approach, implementations degrade into quadratic scans or expensive recomputation, which collapses under high-throughput streams. The right abstraction enables single-pass processing, predictable memory growth tied to active keys, and clean integration into online systems where events arrive continuously and decisions must be made incrementally.

## 🔍 Problem Statement
You are given an integer array `slides` of length up to `200000`, where `slides[i]` is the slide ID shown at minute `i`, and an integer `k` such that `1 <= k <= slides.length`. A contiguous segment `slides[l...r]` is valid if every distinct slide ID appears no more than `k` times inside that segment. Return the maximum possible length of such a segment.

Constraints:
- `1 <= slides.length <= 200000`
- `1 <= slides[i] <= 1000000000`
- `1 <= k <= slides.length`

Examples:

- `slides = [4, 2, 4, 3, 2, 4, 2, 5], k = 2` → `5`
- `slides = [7, 7, 7, 1, 2, 1, 2, 3], k = 1` → `4`

The decisive constraint is input size: with `O(n^2)` subarray checks, worst-case work is far too large. The solution must exploit contiguity and update state incrementally.

## 🪜 How to Solve This
1. Read the problem → the answer must be a **contiguous** interval, so this is not a counting or sorting problem. Reordering destroys the meaning.

2. The validity rule is local to a window: for every slide ID, frequency in the current segment must stay `<= k`. That immediately suggests maintaining counts as the window grows.

3. Start with a right pointer expanding the segment one minute at a time. Each new slide increases one frequency.

4. If that addition makes some slide appear `k + 1` times, the window is no longer valid. Since the segment must remain contiguous, the only repair is to move the left pointer rightward until the violation disappears.

5. This gives a classic **sliding window with a hash map**: expand greedily, shrink only when necessary, and track the largest valid width seen.

6. Why this works: each pointer moves in one direction only. We never reconsider earlier positions, so we avoid nested rescans while still exploring every maximal valid window.

## 🧩 Algorithm Walkthrough
1. **Initialize state**  
   Use two pointers: `left = 0` and `right` iterating from `0` to `n - 1`. Maintain a hash map `freq` from slide ID to count within the current window. Also track `best = 0`.

2. **Expand the window**  
   For each `right`, increment `freq[slides[right]]`. This represents adding the new minute to the current contiguous segment.

3. **Detect invalidity**  
   The only frequency that can become invalid after expansion is the one for `slides[right]`. No other count changed. If `freq[slides[right]] <= k`, the window is still valid.

4. **Shrink until valid**  
   While `freq[slides[right]] > k`, decrement `freq[slides[left]]` and advance `left`. This restores the invariant: every slide ID in `slides[left...right]` appears at most `k` times.

5. **Record the candidate answer**  
   Once the window is valid again, compute its length as `right - left + 1` and update `best`.

6. **Why the pattern fits**  
   This is a textbook **Two Pointers / Sliding Window** problem because validity is monotonic under left-shrinking: once a window is invalid due to excess occurrences, removing elements from the left is the only contiguous repair. Each index enters and leaves the window at most once, which yields linear time.

## 📊 Worked Example
Example: `slides = [4, 2, 4, 3, 2, 4, 2, 5]`, `k = 2`

| right | slides[right] | action | left | freq summary | valid window | best |
|---|---:|---|---:|---|---|---:|
| 0 | 4 | add 4 | 0 | {4:1} | [4] | 1 |
| 1 | 2 | add 2 | 0 | {4:1,2:1} | [4,2] | 2 |
| 2 | 4 | add 4 | 0 | {4:2,2:1} | [4,2,4] | 3 |
| 3 | 3 | add 3 | 0 | {4:2,2:1,3:1} | [4,2,4,3] | 4 |
| 4 | 2 | add 2 | 0 | {4:2,2:2,3:1} | [4,2,4,3,2] | 5 |
| 5 | 4 | add 4, invalid | 1→2 | after shrinking: {4:2,2:1,3:1} | [4,3,2,4] | 5 |
| 6 | 2 | add 2 | 2 | {4:2,2:2,3:1} | [4,3,2,4,2] | 5 |
| 7 | 5 | add 5 | 2 | {4:2,2:2,3:1,5:1} | [4,3,2,4,2,5] | 6 |

Final answer: `6`.

## ⏱ Complexity Analysis
### Time Complexity
`O(n)` where `n = slides.length`. Each element is added to the window once by `right` and removed at most once by `left`, so the dominant work is linear hash map updates. At `10^6` elements this is operationally practical; at `10^9`, linear time is still the lower bound but memory bandwidth and runtime become system-level concerns.

### Space Complexity
`O(m)` where `m` is the number of distinct slide IDs in the current window, bounded by `O(n)` in the worst case. The hash map owns this space. You cannot generally reduce it without losing constant-time frequency updates; compression or approximate counting changes the problem semantics.

## 💡 Key Takeaways
- If the problem asks for the longest or shortest **contiguous** segment under a mutable validity rule, sliding window should be your first candidate.
- If validity depends on per-value frequencies inside the current interval, pair the window with a hash map of counts.
- Only the count of the newly added `slides[right]` can create a violation; shrinking based on anything broader is unnecessary work.
- Update the answer only after the shrink loop finishes; recording length before restoring validity causes subtle off-by-one bugs.
- The production-grade insight is incremental state maintenance: preserve order, update local counts, and avoid recomputing global validity from scratch on every step.

## 🚀 Variations & Further Practice
- Return the actual window bounds or the segment itself, not just the length; the core algorithm is unchanged, but tie-breaking and output semantics add edge-case complexity.
- Allow at most `k` repeats for only a subset of “tracked” slide IDs while others are unrestricted; this introduces selective constraint enforcement and more nuanced validity logic.
- Generalize from a fixed array to an online event stream with expirations or time-based windows; the conceptual twist is that window movement is driven by timestamps and retention policy, not just array indices.