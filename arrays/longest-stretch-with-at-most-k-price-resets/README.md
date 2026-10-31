# Longest Stretch With At Most K Price Resets

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Arrays &nbsp;|&nbsp; **Tags:** Arrays, Sliding Window, Hash Map

---

## 🗂 Problem Overview
Given an integer array `prices` and an integer `k`, find the maximum length of a contiguous subarray such that deleting at most `k` elements from that subarray leaves all remaining values equal. Equivalently, within any candidate window, the count of elements not equal to the most frequent value must be at most `k`. The challenge is doing this under `prices.length <= 100000`, which rules out quadratic enumeration of all subarrays.

## 🌍 Engineering Impact
This pattern shows up anywhere a system must measure dominant behavior while tolerating bounded noise: pricing telemetry with transient resets, observability pipelines with outlier samples, fraud models over event streams, ranking systems smoothing temporary score perturbations, and rate-limit analytics over bursty traffic. At scale, brute-force recomputation per interval collapses under cardinality and latency pressure. A sliding-window frequency model enables online evaluation with bounded memory growth and predictable throughput. Architecturally, it supports stream processing, incremental dashboards, and near-real-time anomaly suppression without materializing every candidate segment or re-scanning historical data.

## 🔍 Problem Statement
You are given an array `prices` where `prices[i]` is the displayed product price at hour `i`, and an integer `k`. A contiguous stretch `prices[l...r]` is valid if you can delete at most `k` elements from inside that stretch so that every remaining element has the same value. The deleted elements may be anywhere inside the window; they do not need to be adjacent.

Return the maximum valid window length.

Constraints:

- `1 <= prices.length <= 100000`
- `1 <= prices[i] <= 1000000000`
- `0 <= k <= prices.length`

Examples:

- `prices = [7,7,8,7,7,9,7], k = 1` → `5`
- `prices = [4,5,5,5,6,5,5], k = 2` → `7`

The key observation is that a window is valid iff:

`window_length - max_frequency_in_window <= k`

That constraint drives the algorithmic choice: we need a linear or near-linear way to maintain frequencies and the current dominant value inside a moving window.

## 🪜 How to Solve This
1. Start from the validity rule → after deleting up to `k` elements, all survivors must match one value. So inside any window, everything except the majority value must be removable.

2. Rewrite that as a testable condition → if the window length is `W` and the most frequent price appears `M` times, then we need `W - M <= k`.

3. Now ask what changes as we scan left to right → only one new element enters at a time, and occasionally one old element leaves. That strongly suggests a sliding window.

4. To evaluate `M` efficiently, maintain a frequency map of prices inside the current window and track the largest frequency seen for any value in the window.

5. Expand the right pointer greedily. If the window becomes invalid, shrink from the left until it is valid again.

6. Record the largest valid window length seen during the scan.

Why this works: for each right boundary, the best answer ending there is the widest valid left boundary. Two pointers let us maintain exactly that in one pass, without revisiting subarrays or sorting values.

## 🧩 Algorithm Walkthrough
1. **Use the Sliding Window / Two Pointers pattern.**  
   Maintain a window `[left, right]` over `prices`. This is the right abstraction because validity depends on a contiguous range and can be updated incrementally as the range grows or shrinks.

2. **Track frequencies with a hash map.**  
   When `right` advances, increment `freq[prices[right]]`. This gives constant-average-time updates even though price values are large (`<= 1e9`) and sparse.

3. **Maintain `maxFreq`.**  
   After adding the new rightmost value, update `maxFreq = max(maxFreq, freq[prices[right]])`. This represents the highest count of any single price seen in the current or recent window state.

4. **Check the window invariant.**  
   A window is valid when  
   `windowSize - maxFreq <= k`.  
   Interpretation: all non-majority elements can be deleted within budget.

5. **Shrink when invalid.**  
   While `windowSize - maxFreq > k`, decrement `freq[prices[left]]` and move `left` forward. This restores validity by discarding elements from the left edge.

6. **Why stale `maxFreq` is acceptable.**  
   `maxFreq` may remain higher than the true current maximum after shrinking, but that does not break correctness. It can only delay shrinking, never cause us to miss the optimal answer; the standard sliding-window proof for this pattern still holds because `right` only moves forward and the recorded answer corresponds to a window size achievable under some valid dominant count.

7. **Update the answer.**  
   After restoring validity, set `best = max(best, right - left + 1)`.

This yields a single pass with bounded per-element work.

## 📊 Worked Example
Example: `prices = [7,7,8,7,7,9,7]`, `k = 1`

| right | prices[right] | freq(7,8,9) | maxFreq | window `[left..right]` | size | size-maxFreq | action |
|---|---:|---|---:|---|---:|---:|---|
| 0 | 7 | 1,0,0 | 1 | `[0..0]` | 1 | 0 | valid |
| 1 | 7 | 2,0,0 | 2 | `[0..1]` | 2 | 0 | valid |
| 2 | 8 | 2,1,0 | 2 | `[0..2]` | 3 | 1 | valid |
| 3 | 7 | 3,1,0 | 3 | `[0..3]` | 4 | 1 | valid |
| 4 | 7 | 4,1,0 | 4 | `[0..4]` | 5 | 1 | valid, best=5 |
| 5 | 9 | 4,1,1 | 4 | `[0..5]` | 6 | 2 | invalid → shrink |
| 5 | 9 | 3,1,1 | 4 | `[1..5]` | 5 | 1 | valid |
| 6 | 7 | 4,1,1 | 4 | `[1..6]` | 6 | 2 | invalid → shrink to valid |

The longest valid stretch found is length `5`, corresponding to `[7,7,8,7,7]`.

## ⏱ Complexity Analysis
### Time Complexity
`O(n)` average time. Each element is added to the window once by `right` and removed at most once by `left`, and each frequency-map update is `O(1)` average. At `10^6` elements this remains practical in memory-resident workloads; at `10^9`, the algorithm is still linear but throughput and I/O dominate, so streaming execution becomes the real constraint.

### Space Complexity
`O(u)`, where `u` is the number of distinct prices inside the active window, bounded by `O(n)` in the worst case. The hash map owns this space. You cannot generally reduce it below distinct-value tracking without losing constant-time majority-frequency updates.

## 💡 Key Takeaways
- If a subarray becomes valid by deleting a bounded number of elements so the rest match, translate it to `window_size - max_frequency <= k`; that is the pattern-recognition trigger.
- When the problem asks for the longest contiguous range under a locally checkable constraint, default to Sliding Window / Two Pointers before considering DP or brute force.
- The validity check uses the current window length `right - left + 1`; off-by-one mistakes here silently corrupt both shrinking and answer updates.
- `maxFreq` is updated on expansion but typically not recomputed on shrink; trying to “fix” that with full rescans often degrades the solution to quadratic behavior.
- In production systems, this is the broader design move: maintain dominant-state summaries incrementally so bounded noise can be filtered online without replaying history.

## 🚀 Variations & Further Practice
- **Longest Repeating Character Replacement**: same core pattern, but over characters and replacements instead of deletions; the conceptual twist is interpreting edits as substitutions rather than removals.
- **Max Consecutive Ones III**: binary-specialized version where the “non-majority” count collapses to counting zeros; useful for recognizing when the hash map can be eliminated.
- **Longest Subarray with Absolute Diff Less Than or Equal to Limit**: still sliding window, but validity depends on dynamic min/max rather than majority frequency, requiring monotonic deques instead of a frequency map.