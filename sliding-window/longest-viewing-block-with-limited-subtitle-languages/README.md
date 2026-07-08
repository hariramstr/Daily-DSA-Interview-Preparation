# Longest Viewing Block With Limited Subtitle Languages

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Sliding Window &nbsp;|&nbsp; **Tags:** sliding window, hash map, two pointers

---

## 🗂 Problem Overview
Given an array `languages` where each element is the subtitle language used for one minute of a broadcast, return the length of the longest contiguous segment containing at most `k` distinct language codes. The input is an ordered stream, so contiguity matters. The non-trivial part is scale: with up to 200,000 entries, enumerating all subarrays is too expensive, so the solution must exploit structure in valid ranges.

## 🌍 Engineering Impact
This pattern shows up anywhere systems need the longest recent interval satisfying a bounded-cardinality constraint: streaming analytics over event types, sessionization with limited context switches, observability pipelines tracking distinct error classes, and search or recommendation systems enforcing diversity caps inside ranked windows. At scale, brute-force range scans collapse under quadratic behavior and destroy latency budgets. A sliding-window design turns the problem into a single-pass online computation, which matters for live dashboards, edge processing, and memory-bounded services where you cannot materialize or re-evaluate every candidate segment.

## 🔍 Problem Statement
You are given an array `languages` of length `n`, where `languages[i]` is a lowercase string representing the subtitle language used during minute `i` of a live broadcast. You must compute the maximum length of a contiguous segment containing at most `k` distinct language codes.

Constraints:

- `1 <= languages.length <= 200000`
- `1 <= languages[i].length <= 10`
- `languages[i]` consists of lowercase English letters
- `1 <= k <= languages.length`

Examples:

- `languages = ["en","en","es","es","fr","es","es"]`, `k = 2` → `4`
- `languages = ["jp","kr","jp","cn","cn","jp","jp"]`, `k = 1` → `2`

Repeated occurrences of the same language do not increase the distinct count; only unique codes inside the current segment matter. The key algorithmic constraint is the input size: checking all `O(n^2)` contiguous ranges is not viable, so the solution must run in linear or near-linear time.

## 🪜 How to Solve This
1. Read the problem → the phrase “longest contiguous segment” is the first signal for a window over the array rather than a global set or sorting-based approach.

2. Notice the validity rule → a segment is valid if it contains at most `k` distinct languages. That condition changes incrementally as the window grows or shrinks, which strongly suggests maintaining state instead of recomputing it.

3. Ask what state is needed → we need to know how many times each language appears in the current window. A hash map gives constant-time updates as the window moves.

4. Expand to the right → keep adding minutes to the current segment, updating counts. As long as distinct languages stay `<= k`, the window is valid.

5. When the window becomes invalid → move the left pointer rightward until the window is valid again. Decrement counts, and remove a language from the map when its count drops to zero.

6. Track the best valid window length seen during this process. Because each pointer only moves forward, the whole scan is linear.

## 🧩 Algorithm Walkthrough
1. **Use the Sliding Window / Two Pointers pattern.**  
   Maintain a window `[left, right]` representing the current contiguous viewing block. This abstraction fits because the constraint is local to a range and can be updated incrementally as the range changes.

2. **Track per-language frequency in a hash map.**  
   When `right` advances, increment `freq[languages[right]]`. If this language was not already present, the number of distinct languages increases implicitly through the map size. The invariant is: the map always reflects exact counts for the current window.

3. **Detect invalid windows immediately.**  
   If `freq.size() > k`, the current window violates the subtitle-tolerance constraint. At this point, extending further right cannot fix it; only shrinking from the left can reduce distinct count.

4. **Shrink until the invariant is restored.**  
   Repeatedly decrement `freq[languages[left]]`, and if a count reaches zero, remove that key from the map. Then increment `left`. Continue until `freq.size() <= k`. The invariant after this loop: the window is valid again and is the smallest valid window ending at `right`.

5. **Update the maximum length.**  
   After restoring validity, compute `right - left + 1` and update the best answer. This is correct because every valid window ending at `right` and starting left of the current `left` would have been invalid.

6. **Why linear time holds.**  
   Each element enters the window once when `right` moves forward and leaves at most once when `left` moves forward. No pointer ever moves backward, so total work is `O(n)`.

## 📊 Worked Example
Example: `languages = ["en","en","es","es","fr","es","es"]`, `k = 2`

| right | lang | action | left | freq | distinct | best |
|---|---|---|---:|---|---:|---:|
| 0 | en | add `en` | 0 | `{en:1}` | 1 | 1 |
| 1 | en | add `en` | 0 | `{en:2}` | 1 | 2 |
| 2 | es | add `es` | 0 | `{en:2, es:1}` | 2 | 3 |
| 3 | es | add `es` | 0 | `{en:2, es:2}` | 2 | 4 |
| 4 | fr | add `fr`, invalid | 0 | `{en:2, es:2, fr:1}` | 3 | 4 |
| 4 | fr | shrink twice, drop `en` | 2 | `{es:2, fr:1}` | 2 | 4 |
| 5 | es | add `es` | 2 | `{es:3, fr:1}` | 2 | 4 |
| 6 | es | add `es` | 2 | `{es:4, fr:1}` | 2 | 5 |

Longest valid block is length `5`: `["es","es","fr","es","es"]`.

## ⏱ Complexity Analysis
### Time Complexity
`O(n)` where `n = languages.length`. Each language code is processed when the right pointer includes it and at most once more when the left pointer excludes it. At `10^6` elements this remains practical in a single pass; at `10^9`, the bottleneck becomes I/O and memory locality rather than algorithmic shape.

### Space Complexity
`O(min(n, k))` in practice, bounded by the number of distinct language codes inside the current window, stored in the hash map. You cannot reduce this below tracking active distinct elements without losing constant-time updates; alternatives trade memory for slower membership and count maintenance.

## 💡 Key Takeaways
- If the problem asks for a **longest contiguous range** under a constraint that can be updated as elements enter and leave, think sliding window immediately.
- If validity depends on the **number of distinct values** in the current range, a hash map of frequencies is usually the right state structure.
- Remove a language from the map exactly when its count reaches zero; leaving zero-count keys behind breaks the distinct-count invariant.
- Update the answer only after restoring `distinct <= k`; measuring before the shrink loop records invalid windows.
- In production systems, this pattern is valuable because it converts expensive repeated range evaluation into an online, single-pass state machine with predictable latency.

## 🚀 Variations & Further Practice
- **Longest substring with at most `k` distinct characters**: same pattern on strings; the twist is recognizing that the array/string distinction is irrelevant to the abstraction.
- **Longest repeating character replacement**: sliding window with a frequency map, but validity depends on window size minus max-frequency, not just distinct count.
- **Minimum window substring**: still two pointers plus counts, but now the goal is the shortest valid window and the validity condition is asymmetric and requirement-driven rather than cardinality-bounded.