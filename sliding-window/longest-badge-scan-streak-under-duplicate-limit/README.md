# Longest Badge Scan Streak Under Duplicate Limit

**Difficulty:** Easy &nbsp;|&nbsp; **Topic:** Sliding Window &nbsp;|&nbsp; **Tags:** Sliding Window, Hash Map, Array

---

## 🗂 Problem Overview
Given a badge scan log `scans` and a limit `k`, find the maximum length of a contiguous subarray where every badge ID appears at most `k` times. The input is an array of up to `100,000` scans and an integer `k`; the output is a single integer length. The non-trivial part is enforcing per-ID frequency limits over contiguous ranges without re-counting the same elements repeatedly.

## 🌍 Engineering Impact
This pattern shows up anywhere systems need to maintain a valid rolling segment under per-key frequency constraints: stream processing, fraud detection, clickstream analysis, log anomaly detection, and API abuse controls. In production, the difference between recomputing counts for every candidate range and maintaining them incrementally is the difference between linear throughput and collapse under load. Sliding-window frequency tracking enables online processing, bounded latency, and predictable memory growth. Without it, systems drift toward quadratic scans, oversized buffers, or expensive re-aggregation passes that fail at high event volume.

## 🔍 Problem Statement
You are given an integer array `scans`, where `scans[i]` is the badge ID recorded at time `i`, and an integer `k`. Return the length of the longest contiguous subarray such that every distinct badge ID inside that subarray appears at most `k` times.

Constraints:

- `1 <= scans.length <= 100000`
- `1 <= scans[i] <= 1000000000`
- `1 <= k <= scans.length`
- The answer fits in a 32-bit integer

Examples:

- `scans = [5, 7, 5, 7, 5, 8], k = 2` → `5`
- `scans = [3, 3, 3, 2, 2, 1], k = 1` → `2`

The key constraint is input size: `100,000` elements is large enough that checking all subarrays or repeatedly recomputing frequencies is too expensive. The algorithm must process the array in near-linear time while preserving exact counts inside a moving contiguous window.

## 🪜 How to Solve This
1. Read the requirement carefully → we need a **contiguous** subarray, so sorting is off the table because it destroys order.

2. Notice the rule is about **frequency inside the current range** → that suggests tracking counts dynamically as the range grows and shrinks.

3. If we extend the right boundary by one element, only one badge count changes. That is a strong signal for a **sliding window** with a frequency map.

4. Start with an empty window. Move `right` forward, increment the count for `scans[right]`, and check whether that badge now violates the limit `k`.

5. If it does, do not restart from scratch. Shrink from the left until the violating badge count is back within limit. This works because removing items from the left is the minimal repair needed to restore validity.

6. After each repair, the window is valid again, so its length is a candidate answer.

7. Because each index enters and leaves the window at most once, the whole process stays linear. That is the reason this approach scales and the reason nested-loop enumeration is unnecessary.

## 🧩 Algorithm Walkthrough
1. **Choose the pattern: Sliding Window / Two Pointers with a Hash Map.**  
   We need the longest contiguous region satisfying a local frequency constraint. Two pointers model the active region, and a hash map stores counts for badge IDs currently inside it.

2. **Initialize state.**  
   Set `left = 0`, `best = 0`, and create `count = {}`. The invariant is: before measuring window length, `count` reflects frequencies for `scans[left...right]`.

3. **Expand the window with `right`.**  
   For each `right` from `0` to `n - 1`, increment `count[scans[right]]`. This is the only new information introduced at this step, so only that badge can create an invalid window.

4. **Repair invalid state by shrinking from the left.**  
   While `count[scans[right]] > k`, decrement `count[scans[left]]` and move `left` forward. This is correct because the violation is caused by too many occurrences of one badge, and removing elements from the left monotonically reduces counts until validity is restored.

5. **Maintain the core invariant.**  
   After the inner loop, every badge in the current window appears at most `k` times. That means `scans[left...right]` is a valid candidate, and no earlier `left` would be valid for this same `right`.

6. **Update the answer.**  
   Compute `right - left + 1` and update `best`. Since every valid window ending at `right` has a left boundary at or after the current `left`, this captures the maximum valid window for that endpoint.

7. **Return `best`.**  
   Each element is added once and removed once, so the algorithm is `O(n)` with hash-map operations assumed `O(1)` on average.

## 📊 Worked Example
Example: `scans = [5, 7, 5, 7, 5, 8]`, `k = 2`

| right | scans[right] | action | left | counts | valid? | best |
|---|---:|---|---:|---|---|---:|
| 0 | 5 | add 5 | 0 | {5:1} | yes | 1 |
| 1 | 7 | add 7 | 0 | {5:1, 7:1} | yes | 2 |
| 2 | 5 | add 5 | 0 | {5:2, 7:1} | yes | 3 |
| 3 | 7 | add 7 | 0 | {5:2, 7:2} | yes | 4 |
| 4 | 5 | add 5 | 0 | {5:3, 7:2} | no | 4 |
| 4 | 5 | shrink: remove scans[0]=5 | 1 | {5:2, 7:2} | yes | 4 |
| 5 | 8 | add 8 | 1 | {5:2, 7:2, 8:1} | yes | 5 |

Final answer: `5`, from window `[7, 5, 7, 5, 8]`.

## ⏱ Complexity Analysis
### Time Complexity
`O(n)` average time, where `n = scans.length`. Each element is processed once when the right pointer expands and at most once when the left pointer contracts. At `10^6` elements this remains practical in a single pass; at `10^9`, linear work is still substantial, but anything superlinear is operationally unrealistic.

### Space Complexity
`O(m)`, where `m` is the number of distinct badge IDs in the current window or array, whichever is smaller. The hash map owns the space. It cannot be meaningfully reduced without losing constant-time frequency updates; trading it away usually forces slower recomputation.

## 💡 Key Takeaways
- If the problem asks for a **longest contiguous segment** under a constraint that can be updated incrementally, think sliding window before considering brute force.
- If validity depends on **per-value counts inside the current range**, pair two pointers with a hash map of frequencies.
- Shrink only while the just-added badge exceeds `k`; you do not need to rescan all counts after every insertion.
- Update the answer after restoring validity, not before, or you will record invalid window lengths.
- The production-grade insight is incremental state maintenance: preserve just enough metadata to repair local violations instead of recomputing global properties repeatedly.

## 🚀 Variations & Further Practice
- Longest subarray with **at most `K` distinct values**: same window pattern, but the constraint is on unique-key cardinality rather than per-key frequency.
- Longest substring where **each character appears at least `k` times**: superficially similar, but the validity condition is non-monotonic and usually needs divide-and-conquer or multi-pass windowing.
- Count the number of subarrays where no value appears more than `k` times: same repair logic, but instead of tracking only the maximum length, accumulate how many valid windows end at each `right`.