# Longest Recipe Video Segment With Limited Step Repeats

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Sliding Window &nbsp;|&nbsp; **Tags:** Sliding Window, Hash Map, Two Pointers

---

## 🗂 Problem Overview
Given an array `steps`, where `steps[i]` is the recipe step shown at second `i`, find the maximum length of a contiguous segment in which every distinct step ID appears at most `k` times. Return only that length. The challenge is that `steps.length` can reach `200000`, so any approach that checks many subarrays explicitly is too slow. The solution must maintain frequency constraints incrementally while scanning once.

## 🌍 Engineering Impact
This pattern shows up anywhere a system needs the longest contiguous interval under bounded repetition: media clipping, abuse detection, request shaping, telemetry burst analysis, and stream quality heuristics. In streaming pipelines, you often need to preserve order while enforcing local frequency constraints without materializing every candidate interval. Without a linear-time sliding window, implementations degrade into quadratic scans, which collapse under high-throughput event streams. The underlying design lesson is broader: when constraints are local to a moving interval, incremental state plus monotonic pointer movement usually beats recomputation and enables predictable latency at scale.

## 🔍 Problem Statement
You are given an integer array `steps` and an integer `k`. Each value represents the step ID visible in a cooking tutorial at a particular second. A contiguous segment is valid if no step ID occurs more than `k` times inside that segment. Return the maximum possible length of such a segment.

Constraints:

- `1 <= steps.length <= 200000`
- `1 <= steps[i] <= 1000000000`
- `1 <= k <= steps.length`

Examples:

- `steps = [4, 2, 4, 3, 2, 4, 1], k = 2` → `5`
- `steps = [7, 7, 7, 8, 8, 9, 7], k = 1` → `3`

Important edge cases:

- All values already satisfy the limit across the full array.
- A single value repeats many times consecutively.
- Large step IDs rule out array-indexed counting unless compressed.

The key algorithmic constraint is the input size: full credit requires roughly linear time, so enumerating subarrays is not viable.

## 🪜 How to Solve This
1. Read the requirement carefully → this is about a **contiguous** segment, so sorting is illegal because order matters.

2. The validity rule depends only on frequencies inside the current segment → that strongly suggests a **sliding window** with a frequency map.

3. Start with an empty window and expand the right boundary one element at a time. Each new step updates its count.

4. Ask: when does the window become invalid? Only when the just-added step’s frequency exceeds `k`. No other count can suddenly break at that moment.

5. Once invalid, shrink from the left until that offending step is back within the limit. This restores validity without restarting the scan.

6. Because each index enters the window once and leaves once, both pointers move monotonically → linear work overall.

7. After every valid expansion, compute `right - left + 1` and keep the maximum.

The mental model is simple: maintain the largest valid suffix ending at each `right` position, then take the best over all suffixes.

## 🧩 Algorithm Walkthrough
1. **Choose the pattern: Two Pointers + Hash Map.**  
   We need the longest contiguous region under a frequency cap. This is the canonical sliding-window shape: expand to include more data, contract only when constraints break.

2. **Initialize state.**  
   Keep `left = 0`, `best = 0`, and a hash map `freq` from step ID to count. The window is always `steps[left..right]`.

3. **Expand the window with `right`.**  
   For each `right` from `0` to `n - 1`, increment `freq[steps[right]]`. This tentatively adds the new second to the current segment.

4. **Detect invalidity locally.**  
   If `freq[steps[right]] <= k`, the window is still valid. If it becomes `k + 1`, the only violated constraint is for `steps[right]`, because all previous counts were already valid before this insertion.

5. **Shrink until valid again.**  
   While `freq[steps[right]] > k`, decrement `freq[steps[left]]` and advance `left`. This removes elements from the front until the repeated step is back within the allowed limit.

6. **Maintain the invariant.**  
   After the shrink loop, every value in `steps[left..right]` appears at most `k` times. That invariant is what makes `best = max(best, right - left + 1)` correct.

7. **Why this is optimal.**  
   For each `right`, the algorithm finds the longest valid window ending at `right`. Since every candidate maximum must end somewhere, taking the maximum across all `right` positions yields the global optimum.

## 📊 Worked Example
Example: `steps = [4, 2, 4, 3, 2, 4, 1]`, `k = 2`

| right | steps[right] | Action | left | Window | Counts | best |
|---|---:|---|---:|---|---|---:|
| 0 | 4 | add 4 | 0 | `[4]` | `{4:1}` | 1 |
| 1 | 2 | add 2 | 0 | `[4,2]` | `{4:1,2:1}` | 2 |
| 2 | 4 | add 4 | 0 | `[4,2,4]` | `{4:2,2:1}` | 3 |
| 3 | 3 | add 3 | 0 | `[4,2,4,3]` | `{4:2,2:1,3:1}` | 4 |
| 4 | 2 | add 2 | 0 | `[4,2,4,3,2]` | `{4:2,2:2,3:1}` | 5 |
| 5 | 4 | add 4, invalid | 1 | after shrink: `[2,4,3,2,4]` | `{4:2,2:2,3:1}` | 5 |
| 6 | 1 | add 1 | 1 | `[2,4,3,2,4,1]` | `{4:2,2:2,3:1,1:1}` | 6 |

The longest valid segment is length `6`, namely `[2,4,3,2,4,1]`.

## ⏱ Complexity Analysis
### Time Complexity
`O(n)` expected time with a hash map. Each element is added once by the right pointer and removed at most once by the left pointer, so total pointer movement is linear. At `10^6` elements this is practical; at `10^9`, even linear scans become infrastructure problems rather than algorithm problems.

### Space Complexity
`O(m)`, where `m` is the number of distinct step IDs currently tracked, bounded by the number of distinct values in `steps`. The hash map owns this space. You could reduce constant factors with coordinate compression or specialized maps, but not the asymptotic bound.

## 💡 Key Takeaways
- If the problem asks for a **longest contiguous subarray** under a per-window constraint, sliding window should be your first candidate.
- If validity depends on **frequency counts that can be updated incrementally**, pair two pointers with a hash map instead of recomputing counts.
- Shrink only while the newly added value violates the limit; you do not need to rescan all frequencies after every insertion.
- Be careful with window length calculation: for inclusive bounds, it is always `right - left + 1`.
- The production-grade insight is to preserve incremental state and monotonic progress; that is the difference between stable linear throughput and recomputation-driven collapse.

## 🚀 Variations & Further Practice
- Allow at most `k` repeats for each value **and** require at most `d` distinct values in the segment; this combines two independent window constraints.
- Find the **number** of valid subarrays instead of the longest one; same window idea, but the counting logic changes from max-tracking to accumulation.
- Make the limit value-specific, e.g. each step ID has its own allowed repeat budget; the window remains sliding, but validity now depends on per-key thresholds.