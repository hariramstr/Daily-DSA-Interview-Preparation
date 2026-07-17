# Longest Translation Draft With Terminology Budget

**Difficulty:** Hard &nbsp;|&nbsp; **Topic:** Sliding Window &nbsp;|&nbsp; **Tags:** Sliding Window, Hash Map, Frequency Counting

---

## 🗂 Problem Overview
Given an array `terms` and an integer budget `k`, find the maximum length of a contiguous subarray that can be converted to a single repeated terminology ID using at most `k` rewrites. For any window, the rewrite cost is `window_length - max_frequency_in_window`. The challenge is that the array can contain up to `2 * 10^5` elements with large terminology IDs, so recomputing frequencies or rescanning every candidate window is too expensive.

## 🌍 Engineering Impact
This pattern shows up anywhere a system needs the longest contiguous segment that is “close enough” to uniform under a bounded correction budget. Examples include log stream normalization, noisy sensor event smoothing, session-level anomaly suppression, OCR/token cleanup pipelines, and search-query rewrite analysis. At scale, brute force collapses because every expansion would trigger rescans over high-cardinality state. The sliding-window plus frequency-map approach turns an otherwise quadratic scan into a linear pass, which is the difference between something usable in streaming or online review systems and something that falls over under production traffic.

## 🔍 Problem Statement
You are given an integer array `terms`, where `terms[i]` is the terminology ID used in the `i`th sentence, and an integer `k` representing the maximum allowed cleanup budget.

A contiguous block is valid if it can be made terminology-consistent by rewriting at most `k` sentences. For any window, let `f` be the maximum frequency of any single terminology ID inside that window. Then the cleanup cost is:

`window_length - f`

Return the maximum length of any contiguous block whose cleanup cost is at most `k`.

Constraints:
- `1 <= terms.length <= 2 * 10^5`
- `1 <= terms[i] <= 10^9`
- `0 <= k <= terms.length`

Examples:
- `terms = [4, 7, 7, 4, 7, 9, 7], k = 2` → `6`
- `terms = [1, 2, 3, 2, 2, 3, 3, 3, 2], k = 3` → `7`

The key constraint is input size: anything worse than near-linear time is not viable.

## 🪜 How to Solve This
1. Read the condition carefully → a window is valid when all non-dominant values can be rewritten within budget. That immediately gives the formula: `window_size - max_freq <= k`.

2. We need the **longest contiguous** block → this is a classic signal for a sliding window, because once a window becomes invalid, moving the left edge is the only way to restore validity without discarding useful right-side progress.

3. To evaluate validity efficiently, we need frequency counts inside the current window → use a hash map keyed by terminology ID.

4. The expensive part would be recomputing the current maximum frequency every time the window changes. Instead, track a running `maxFreq` as the window expands. It may become stale when the window shrinks, but that does not break correctness for the final maximum-length computation.

5. Expand right one step at a time, update counts and `maxFreq`, and shrink left while `window_size - maxFreq > k`.

6. After restoring validity, record the window length. One pass, bounded work per element, no nested rescans.

## 🧩 Algorithm Walkthrough
1. **Use the Sliding Window / Two Pointers pattern.**  
   Maintain a window `[left, right]` over `terms`. This is the right abstraction because we need the longest **contiguous** segment satisfying a budget constraint that changes incrementally as elements enter and leave.

2. **Track per-term frequencies with a hash map.**  
   When `right` advances, increment `freq[terms[right]]`. This gives O(1) average-time updates even though terminology IDs can be as large as `10^9`.

3. **Maintain `maxFreq` as the highest count ever seen during expansion.**  
   Update `maxFreq = max(maxFreq, freq[terms[right]])`. This captures the dominant term count needed for the cleanup formula.

4. **Check validity using the rewrite budget invariant.**  
   The window is acceptable if  
   `window_length - maxFreq <= k`.  
   If this fails, the window needs too many rewrites, so increment `left` and decrement the outgoing term’s count until the inequality holds again.

5. **Why stale `maxFreq` is acceptable.**  
   When shrinking, we do **not** recompute `maxFreq` downward. That means `maxFreq` can overestimate the true dominant count in the current window. This may delay shrinking, but it never causes us to miss the optimal answer. The algorithm still records the maximum achievable valid length because `right` only moves forward, and every candidate length is justified by some earlier or current dominant-frequency state.

6. **Update the answer after each expansion/shrink cycle.**  
   Once the window satisfies the budget condition, compute `right - left + 1` and maximize the result.

## 📊 Worked Example
Example: `terms = [4, 7, 7, 4, 7, 9, 7]`, `k = 2`

| right | term | freq summary         | maxFreq | left | window         | size | size - maxFreq | valid |
|------:|-----:|----------------------|--------:|-----:|----------------|-----:|---------------:|:-----:|
| 0     | 4    | {4:1}                | 1       | 0    | [4]            | 1    | 0              | yes   |
| 1     | 7    | {4:1,7:1}            | 1       | 0    | [4,7]          | 2    | 1              | yes   |
| 2     | 7    | {4:1,7:2}            | 2       | 0    | [4,7,7]        | 3    | 1              | yes   |
| 3     | 4    | {4:2,7:2}            | 2       | 0    | [4,7,7,4]      | 4    | 2              | yes   |
| 4     | 7    | {4:2,7:3}            | 3       | 0    | [4,7,7,4,7]    | 5    | 2              | yes   |
| 5     | 9    | {4:2,7:3,9:1}        | 3       | 1    | [7,7,4,7,9]    | 5    | 2              | yes   |
| 6     | 7    | {4:1,7:4,9:1}        | 4       | 1    | [7,7,4,7,9,7]  | 6    | 2              | yes   |

Maximum valid length: `6`.

## ⏱ Complexity Analysis
### Time Complexity
`O(n)` average time, where `n = terms.length`. Each element enters the window once and leaves once, so pointer movement is linear. Hash map updates are O(1) average. At `10^6` elements this is practical; at `10^9`, even linear scans become infrastructure-bound and require streaming or partitioned execution.

### Space Complexity
`O(m)`, where `m` is the number of distinct terminology IDs in the current scan, owned by the frequency hash map. In the worst case `m = n`. You cannot meaningfully reduce this without sacrificing constant-time frequency updates or constraining the value domain.

## 💡 Key Takeaways
- If the problem asks for the longest contiguous segment satisfying “window size minus something dominant/frequent is bounded,” think sliding window with incremental state.
- Large value ranges plus per-window counting is a strong signal for a hash map rather than array-indexed frequency storage.
- The validity check is `right - left + 1 - maxFreq > k`; getting that `+1` wrong is the most common off-by-one bug.
- Do not recompute `maxFreq` when shrinking unless you accept worse complexity; the standard optimized solution intentionally allows it to be stale.
- In production systems, this is the broader pattern of maintaining approximate-enough window state to preserve throughput while still guaranteeing the correct optimization target.

## 🚀 Variations & Further Practice
- **Longest Repeating Character Replacement**: same pattern on strings with a smaller alphabet; the conceptual twist is recognizing that the exact same invariant applies despite the different surface form.
- **Max Consecutive Ones III**: binary version where the “dominant value” is fixed in advance; simpler state, but useful for seeing when the frequency map can be collapsed.
- **At most K distinct elements**: still sliding window, but the constraint is on cardinality rather than rewrite budget, so the maintained invariant and shrink condition change materially.