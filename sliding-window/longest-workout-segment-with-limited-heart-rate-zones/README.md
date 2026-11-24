# Longest Workout Segment With Limited Heart Rate Zones

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Sliding Window &nbsp;|&nbsp; **Tags:** Sliding Window, Hash Map, Two Pointers

---

## 🗂 Problem Overview
Given an integer array `zones` and an integer `k`, find the maximum length of any contiguous subarray containing at most `k` distinct values. The output is a single integer: the length of that longest valid segment. The challenge is scale: with up to `2 * 10^5` elements, enumerating all subarrays is quadratic and infeasible, so the solution must maintain validity incrementally as it scans the array.

## 🌍 Engineering Impact
This pattern shows up anywhere systems need the longest recent interval satisfying a bounded-cardinality constraint: streaming analytics over event types, observability windows with limited error classes, fraud detection over transaction categories, or search/session analysis constrained by intent diversity. At scale, brute-force rescans collapse under throughput and latency targets because every new event would trigger repeated work over historical data. A sliding-window design turns this into online processing: one pass, bounded state, predictable memory, and easy integration into real-time pipelines, edge services, and telemetry processors where per-record cost must stay effectively constant.

## 🔍 Problem Statement
You are given an array `zones` where `zones[i]` is the heart rate zone observed during minute `i` of a workout, and an integer `k`. Return the length of the longest contiguous subarray containing no more than `k` distinct zone values.

Constraints:

- `1 <= zones.length <= 2 * 10^5`
- `1 <= zones[i] <= 10^9`
- `1 <= k <= zones.length`

Examples:

- `zones = [2, 2, 1, 3, 2, 2, 1], k = 2` → `3`
- `zones = [5, 5, 5, 4, 4, 6, 6, 4], k = 2` → `5`

Important edge conditions:
- `k = 1` reduces to the longest run with a single repeated value.
- If `k` is at least the number of distinct values in the whole array, the answer is `zones.length`.
- Values can be large, so indexing by value is not practical; use a hash-based frequency structure.

The key algorithmic driver is the input size: any `O(n^2)` subarray enumeration is too slow.

## 🪜 How to Solve This
1. Read the requirement carefully → this is about a **contiguous** segment, so sorting or global counting is irrelevant.

2. Notice the validity rule depends only on the current window: “at most `k` distinct values.” That strongly suggests a **sliding window** rather than checking every subarray.

3. Ask what state is needed to know whether a window is valid → not just which values appear, but how many times each appears, so we know when removing from the left actually eliminates a distinct zone. That implies a `HashMap<zone, count>`.

4. Expand the window to the right one element at a time. Each expansion is optimistic: include the new minute, update its count, and see if the window is still valid.

5. If distinct values exceed `k`, shrink from the left until validity is restored. This is the key insight: once a window becomes invalid, only moving the left boundary can fix it.

6. After each valid state, update the best length. Because each index enters and leaves the window at most once, the whole process stays linear.

That chain of reasoning gets you from problem statement to an `O(n)` solution naturally.

## 🧩 Algorithm Walkthrough
1. **Choose the pattern: Sliding Window with Two Pointers.**  
   Maintain a window `[left, right]` representing the current contiguous segment. This is the right abstraction because the constraint is local to a contiguous range and can be repaired by moving boundaries incrementally.

2. **Track frequencies with a hash map.**  
   When `right` advances, increment `freq[zones[right]]`. If this value was previously absent, the number of distinct zones increases by one.  
   **Invariant:** `freq` exactly represents counts for elements inside the current window.

3. **Detect invalid windows immediately.**  
   If the number of keys in `freq` exceeds `k`, the window violates the problem constraint. At this point, extending further right cannot restore validity; only shrinking left can.

4. **Shrink from the left until valid again.**  
   Repeatedly decrement `freq[zones[left]]`, and if a count drops to zero, remove that key from the map. Then increment `left`.  
   **Invariant:** after the shrink loop, the window contains at most `k` distinct zones.

5. **Record the best valid length.**  
   Once valid, compute `right - left + 1` and update the maximum. This is correct because for the current `right`, the maintained `left` is the furthest left boundary that still yields a valid window after necessary shrinking.

6. **Why linear time holds.**  
   `right` moves from `0` to `n - 1` once. `left` also moves forward only, never backward. Each element is added once and removed once, so total pointer movement is `O(n)`, with hash-map updates amortized `O(1)`.

## 📊 Worked Example
Example: `zones = [2, 2, 1, 3, 2, 2, 1]`, `k = 2`

| right | zones[right] | action | left | freq | valid? | best |
|---|---:|---|---:|---|---|---:|
| 0 | 2 | add 2 | 0 | {2:1} | yes | 1 |
| 1 | 2 | add 2 | 0 | {2:2} | yes | 2 |
| 2 | 1 | add 1 | 0 | {2:2,1:1} | yes | 3 |
| 3 | 3 | add 3, too many distinct | 0 | {2:2,1:1,3:1} | no | 3 |
| 3 | 3 | shrink left twice | 2 | {1:1,3:1} | yes | 3 |
| 4 | 2 | add 2, too many distinct | 2 | {1:1,3:1,2:1} | no | 3 |
| 4 | 2 | shrink left once | 3 | {3:1,2:1} | yes | 3 |
| 5 | 2 | add 2 | 3 | {3:1,2:2} | yes | 3 |
| 6 | 1 | add 1, shrink until valid | 4 | {2:2,1:1} | yes | 3 |

Maximum valid length is `3`.

## ⏱ Complexity Analysis
### Time Complexity
`O(n)` amortized, where `n = zones.length`. Each element is inserted into the window once by `right` and removed once by `left`; the dominant operations are hash-map increments, decrements, and occasional key removals. At `10^6` elements this remains practical; at `10^9`, even linear scans become infrastructure-bound rather than algorithm-bound.

### Space Complexity
`O(min(n, k))` in practice, bounded by the number of distinct values currently tracked in the hash map. The map owns essentially all auxiliary space. It cannot be meaningfully reduced below this without losing constant-time updates or switching to value-compression plus alternative indexing.

## 💡 Key Takeaways
- If the problem asks for a **longest contiguous segment** under an “at most `k` distinct” constraint, think sliding window immediately.
- If validity depends on counts changing as elements enter and leave a range, pair two pointers with a frequency map.
- Remove keys when their count reaches zero; leaving zero-count entries behind corrupts the distinct-count invariant.
- Update window length with `right - left + 1` only after restoring validity, or you will overcount invalid windows.
- The production-grade insight: incremental state maintenance beats recomputation when processing high-volume streams with local window constraints.

## 🚀 Variations & Further Practice
- **Longest substring with at most `k` distinct characters** — same pattern, but over strings; useful for recognizing the abstraction independent of domain vocabulary.
- **Longest repeating character replacement** — sliding window plus a dominant-frequency invariant; harder because validity depends on transform cost, not just distinct count.
- **Subarrays with exactly `k` distinct integers** — typically solved as `atMost(k) - atMost(k - 1)`; the twist is counting all valid windows, not just maximizing one.