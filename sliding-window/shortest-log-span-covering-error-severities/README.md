# Shortest Log Span Covering Error Severities

**Difficulty:** Hard &nbsp;|&nbsp; **Topic:** Sliding Window &nbsp;|&nbsp; **Tags:** Sliding Window, Hash Map, Two Pointers

---

## 🗂 Problem Overview
Given an integer array `logs` and a multiset-like requirement array `required`, find the length of the shortest contiguous subarray whose frequency counts cover every value in `required`, including duplicates. Return `-1` if no such span exists. The challenge is not membership but multiplicity under contiguity constraints. With input sizes up to `2 * 10^5`, any approach that enumerates candidate subarrays or repeatedly rescans counts is too slow.

## 🌍 Engineering Impact
This pattern shows up in log analytics, security event correlation, streaming observability, ad-query matching, and packet inspection: find the smallest time or sequence window that satisfies a required signature with multiplicities. In production, brute-force window enumeration collapses under high-cardinality streams and long retention slices. The sliding-window + frequency-map approach enables single-pass processing, bounded memory proportional to the requirement set, and predictable latency. Without it, alert triage pipelines, SIEM correlation rules, and online anomaly detectors either miss real-time SLAs or require expensive pre-aggregation that reduces fidelity.

## 🔍 Problem Statement
You are given two arrays:

- `logs`, an array of severity levels
- `required`, an array of severity levels that must all be covered by one contiguous span of `logs`

Coverage is count-based, not set-based. If `required = [2, 2, 5]`, then a valid span must contain at least two `2`s and one `5`. Return the length of the shortest valid contiguous subarray, or `-1` if no such subarray exists.

Constraints:

- `1 <= logs.length <= 2 * 10^5`
- `1 <= required.length <= 2 * 10^5`
- `1 <= logs[i], required[i] <= 10^9`

Examples:

- `logs = [4, 2, 7, 2, 5, 1, 2, 5]`, `required = [2, 5, 2]` → `4`
- `logs = [3, 1, 4, 1, 5, 9]`, `required = [1, 1, 2]` → `-1`

The key constraint is scale: `O(n^2)` subarray checking is infeasible, so the solution must be near-linear.

## 🪜 How to Solve This
1. Read the problem → the word “shortest contiguous subarray” is the first signal for a sliding window.
2. Notice duplicates in `required` matter → this is not a set-membership problem; we need frequency accounting.
3. Build a `need` map from `required` → for each severity, store how many times it must appear.
4. Expand a right pointer across `logs` → maintain a `window` frequency map for the current span.
5. Track when the window becomes valid → not by comparing whole maps each time, but by counting how many required keys currently meet their target counts.
6. Once valid, shrink from the left as aggressively as possible → this is where the minimum-length answer emerges.
7. Every left move must preserve correctness → if removing a value causes a required count to drop below target, the window stops being valid and expansion resumes.

The core idea is monotonic progress: each pointer only moves forward. That gives linear scanning instead of repeated rescans over overlapping subarrays.

## 🧩 Algorithm Walkthrough
1. **Count required frequencies.**  
   Build `need[value] = required count`. Also compute `targetKinds = need.size()`. This captures the exact multiplicity constraint and avoids sorting or nested comparisons.

2. **Initialize the sliding window.**  
   Use the **Two Pointers / Sliding Window** pattern with `left = 0`. Maintain `window[value]` for counts inside `logs[left..right]`, plus `formed`, the number of distinct required severities whose window count currently meets `need`.

3. **Expand with the right pointer.**  
   For each `right`, add `logs[right]` into `window`. If that value is required and its count just reached `need[value]`, increment `formed`.  
   **Invariant:** `formed == targetKinds` iff the current window satisfies all required counts.

4. **Shrink with the left pointer while valid.**  
   While `formed == targetKinds`, update the best answer with `right - left + 1`, then remove `logs[left]` from the window and advance `left`. If removing that value causes a required count to fall below `need`, decrement `formed`.  
   This guarantees we record the minimum valid window ending at `right`.

5. **Continue until the scan ends.**  
   Because both pointers move only forward, each element is inserted and removed at most once. That is why the approach is `O(n + m)` rather than quadratic.

6. **Return the result.**  
   If no valid window was ever found, return `-1`; otherwise return the minimum length recorded.

## 📊 Worked Example
Example: `logs = [4, 2, 7, 2, 5, 1, 2, 5]`, `required = [2, 2, 5]`  
`need = {2: 2, 5: 1}`, `targetKinds = 2`

| right | logs[right] | window(2) | window(5) | formed | action |
|---|---:|---:|---:|---:|---|
| 0 | 4 | 0 | 0 | 0 | irrelevant value |
| 1 | 2 | 1 | 0 | 0 | need for `2` not met yet |
| 2 | 7 | 1 | 0 | 0 | irrelevant value |
| 3 | 2 | 2 | 0 | 1 | `2` requirement satisfied |
| 4 | 5 | 2 | 1 | 2 | window valid: `[4,2,7,2,5]` |
| — | shrink | 2 | 1 | 2 | drop `4`, still valid, length `4` |
| — | shrink | 1 | 1 | 1 | drop left `2`, invalid |

Best length so far is `4`, from span `[2, 7, 2, 5]`. Continuing the scan finds no shorter valid window, so the answer is `4`.

## ⏱ Complexity Analysis
### Time Complexity
`O(n + m)`, where `n = logs.length` and `m = required.length`. Building the requirement map costs `O(m)`. The sliding window moves each pointer across `logs` at most once, so total updates are linear. At million-scale inputs this is practical; at billion-scale, even linear scans become throughput- and memory-bandwidth-bound.

### Space Complexity
`O(k)`, where `k` is the number of distinct severity values in `required` plus any tracked window entries. The hash maps own the space. You can restrict `window` updates to required keys only, reducing constants but not asymptotic complexity.

## 💡 Key Takeaways
- If the prompt asks for the **shortest/longest contiguous span** satisfying count constraints, think sliding window before anything else.
- If duplicates in the target matter, model the target as a **frequency map**, not a set.
- Only increment the “satisfied” counter when a count reaches the exact target; extra occurrences must not over-count validity.
- When shrinking, update the answer **before** removing `logs[left]`, or you will miss the current valid window length.
- The production lesson is broader than interviews: incremental state maintenance beats recomputation when scanning high-volume streams under latency constraints.

## 🚀 Variations & Further Practice
- **Minimum window substring / token span:** same pattern, but over characters or tokens; harder because input is often sparse, Unicode-heavy, or embedded in larger parsing pipelines.
- **Shortest span with weighted requirements:** each severity contributes a weight or score threshold instead of exact counts; harder because validity is no longer a simple per-key equality check.
- **Streaming version with online queries:** logs arrive continuously and multiple requirement sets must be evaluated concurrently; harder because one-pass local state is no longer sufficient without indexing or shared summaries.