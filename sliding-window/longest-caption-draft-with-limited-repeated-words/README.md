# Longest Caption Draft With Limited Repeated Words

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Sliding Window &nbsp;|&nbsp; **Tags:** Sliding Window, Hash Map, Frequency Counting

---

## 🗂 Problem Overview
Given an array of lowercase words and an integer `k`, find the maximum length of a contiguous subarray where every distinct word appears at most `k` times. The output is a single integer: the size of the longest valid window. The challenge is scale: with up to `200000` words, enumerating all subarrays is infeasible, so the solution must maintain validity incrementally as the window moves.

## 🌍 Engineering Impact
This pattern shows up anywhere systems enforce bounded repetition inside a moving range: streaming moderation pipelines limiting repeated tokens, search/query analytics detecting bursty terms, log processing windows tracking noisy keys, and distributed rate-limiters enforcing per-identity caps over recent events. At scale, brute-force rescans turn linear ingestion into quadratic collapse, blowing latency budgets and cache behavior. Sliding-window frequency tracking preserves locality, supports single-pass processing, and gives a reusable mental model for online validation problems where constraints apply to the current segment rather than the full dataset.

## 🔍 Problem Statement
You are given an array `words` of lowercase English strings and an integer `k`. You must return the length of the longest contiguous subarray `words[l...r]` such that, within that window, the frequency of every distinct word is at most `k`.

Constraints:

- `1 <= words.length <= 200000`
- `1 <= words[i].length <= 20`
- `words[i]` contains only lowercase English letters
- `1 <= k <= words.length`

Examples:

- `words = ["sale","new","sale","trend","sale","new"], k = 2` → `4`
- `words = ["a","b","a","c","b","b","d"], k = 1` → `3`

Edge cases matter: if all words are unique, the answer can be the full array; if one word dominates, the window must repeatedly shrink. The decisive constraint is input size: `O(n^2)` subarray checking will not pass, so the algorithm must update counts as the window expands and contracts in near-constant time per step.

## 🪜 How to Solve This
1. Read the requirement carefully → the subarray must be **contiguous**, so sorting or global counting is irrelevant.
2. Notice the rule is about **frequencies inside the current window** → that suggests tracking counts as the window moves.
3. If we extend the right boundary by one word, only that word’s count changes → we can update validity incrementally instead of recomputing the whole window.
4. When a word’s count exceeds `k`, the current window becomes invalid → the only fix is to move the left boundary rightward until that count is back within limit.
5. This gives the standard **sliding window / two pointers** shape:
   - expand right to include more words
   - shrink left only when invalid
   - record the largest valid width seen
6. Why this works: both pointers move forward at most `n` times, and a hash map gives `O(1)` average frequency updates.
7. The key insight is that validity is monotonic under shrinking: once a window is invalid because one word appears too often, removing words from the left is the minimal repair operation.

## 🧩 Algorithm Walkthrough
1. **Initialize state**  
   Use two pointers: `left = 0`, and iterate `right` from `0` to `n - 1`. Maintain a hash map `freq[word]` for counts inside the current window and a variable `best` for the maximum valid length seen.

2. **Expand the window**  
   For each `right`, add `words[right]` to `freq`. This represents including the new word in the active window `[left, right]`.

3. **Detect invalidity locally**  
   Only the newly added word can cause the constraint to fail. If `freq[words[right]] <= k`, the window is still valid. If it becomes `k + 1`, the window violates the rule.

4. **Shrink until repaired**  
   While `freq[words[right]] > k`, decrement `freq[words[left]]` and advance `left`. This is correct because removing words from the left is the only allowed way to restore validity while preserving contiguity.  
   **Invariant maintained:** after the loop, every word in `[left, right]` appears at most `k` times.

5. **Update the answer**  
   Once the window is valid again, compute its length: `right - left + 1`, and update `best`.

6. **Why this abstraction fits**  
   This is a classic **Sliding Window / Two Pointers** problem: contiguous range, local updates, and a validity condition that can be restored by monotonic shrinking. The pattern is optimal here because each index enters and leaves the window at most once, avoiding repeated rescans.

## 📊 Worked Example
Example: `words = ["sale","new","sale","trend","sale","new"], k = 2`

| right | word   | action after add         | left after shrink | window                              | best |
|------:|--------|--------------------------|-------------------|-------------------------------------|-----:|
| 0     | sale   | `sale=1` valid           | 0                 | `["sale"]`                          | 1 |
| 1     | new    | `new=1` valid            | 0                 | `["sale","new"]`                    | 2 |
| 2     | sale   | `sale=2` valid           | 0                 | `["sale","new","sale"]`             | 3 |
| 3     | trend  | `trend=1` valid          | 0                 | `["sale","new","sale","trend"]`     | 4 |
| 4     | sale   | `sale=3` invalid         | 1                 | `["new","sale","trend","sale"]`     | 4 |
| 5     | new    | `new=2` valid            | 1                 | `["new","sale","trend","sale","new"]` | 5 |

The longest valid window is length `5`, because after shrinking at `right = 4`, all counts are again within `k = 2`.

## ⏱ Complexity Analysis
### Time Complexity
`O(n)` average time, where `n = words.length`. Each word is added once when `right` advances and removed at most once when `left` advances. Hash map updates dominate and are constant-time on average. This stays practical at `10^6` elements; `O(n^2)` would be completely non-viable, and at `10^9` even linear scans become infrastructure decisions.

### Space Complexity
`O(m)` space, where `m` is the number of distinct words in the current or total input. The hash map owns this cost. It cannot be meaningfully reduced without losing constant-time updates; replacing it with rescans trades memory savings for unacceptable runtime.

## 💡 Key Takeaways
- If the problem asks for the longest **contiguous** segment satisfying a frequency constraint, think sliding window before considering any nested-loop approach.
- If validity changes only when one new element enters the range, that is a strong signal that a hash map plus two pointers will give a linear solution.
- Shrink only while the violating word’s count exceeds `k`; shrinking more than necessary silently loses valid window length.
- Be careful with window length calculation: after restoring validity, it is always `right - left + 1`, not `right - left`.
- In production systems, this pattern is the difference between batch-style recomputation and online constraint enforcement with predictable throughput.

## 🚀 Variations & Further Practice
- **Longest subarray with at most `K` distinct words** — same sliding-window skeleton, but the constraint is on distinct-key cardinality rather than per-key frequency.
- **Longest substring where every character appears at least `K` times** — harder because shrinking does not monotonically repair the condition; divide-and-conquer or multi-pass strategies are often needed.
- **Streaming window with time-based expiry** — replace index boundaries with timestamps, which introduces out-of-order data, eviction policy complexity, and operational concerns around clock semantics.