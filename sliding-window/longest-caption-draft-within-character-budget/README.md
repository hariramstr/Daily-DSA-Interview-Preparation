# Longest Caption Draft Within Character Budget

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Sliding Window &nbsp;|&nbsp; **Tags:** Sliding Window, Two Pointers, Array

---

## 🗂 Problem Overview
Given an ordered array of non-empty strings `words` and an integer `maxChars`, find the maximum number of consecutive fragments whose total character count is at most `maxChars`. Only fragment lengths matter; spaces between fragments are free. The output is the largest valid window size. The challenge is scale: with up to `100000` fragments, checking every subarray is too slow, so the solution must reuse work as the window moves.

## 🌍 Engineering Impact
This pattern shows up anywhere systems enforce rolling resource budgets over ordered events: log batching under payload caps, streaming pipelines grouping records into bounded micro-batches, search/snippet assembly under character limits, and API gateways packing requests below size thresholds. At scale, brute-force window evaluation turns linear ingestion into quadratic latency and destroys throughput predictability. A sliding window preserves order, supports online processing, and gives deterministic memory behavior. That matters in production paths where inputs are large, streams are continuous, and budget checks sit directly on hot request or ingestion loops.

## 🔍 Problem Statement
You are given `words`, an array of lowercase non-empty strings, and an integer `maxChars`. Each fragment contributes `words[i].length` characters. Fragments must remain contiguous and in original order. Return the maximum number of consecutive fragments whose combined character count does not exceed `maxChars`.

Constraints:

- `1 <= words.length <= 100000`
- `1 <= words[i].length <= 10000`
- `1 <= maxChars <= 10^9`

Spaces between fragments do **not** count toward the budget.

Examples:

- `words = ["go","viral","now","x","trend"], maxChars = 8` → `2`
- `words = ["a","bb","c","ddd","e"], maxChars = 4` → `3`

Edge cases worth noting: a single fragment may already exceed the budget, in which case it cannot belong to any valid window; `maxChars` can be very large, so integer overflow is not a concern in most languages with 64-bit arithmetic, but cumulative sums should still be handled carefully. The input size rules out `O(n^2)` enumeration.

## 🪜 How to Solve This
1. Read the problem → we need the **longest contiguous block** under a **sum constraint**. That is a classic sliding-window signal.
2. Translate strings into costs → each word contributes only `length(word)`. The actual characters are irrelevant once lengths are known.
3. Ask what changes when we extend a window by one element → the total cost only increases by that new length.
4. Notice the key monotonic property → all lengths are positive, so if a window exceeds `maxChars`, expanding it further will never fix it. The only repair is to move the left boundary rightward.
5. That suggests two pointers: grow the right pointer one step at a time, add the new length, and while the sum is too large, shrink from the left.
6. After revalidating the window, record its size. Because each index enters and leaves the window at most once, the full scan is linear.
7. This is the important leap: instead of evaluating every subarray independently, maintain one evolving valid window and reuse prior work.

## 🧩 Algorithm Walkthrough
1. **Initialize state.**  
   Set `left = 0`, `currentSum = 0`, and `best = 0`. These represent the current window `[left, right]`, its total character count, and the maximum valid window length seen so far.

2. **Iterate with the right pointer.**  
   For each `right` from `0` to `words.length - 1`, add `words[right].length` to `currentSum`. This extends the candidate window by one fragment.

3. **Restore validity by shrinking from the left.**  
   While `currentSum > maxChars`, subtract `words[left].length` from `currentSum` and increment `left`. This is the core **Sliding Window / Two Pointers** move: because all fragment lengths are positive, removing items from the left is the only way to reduce the sum.

4. **Maintain the invariant.**  
   After the shrinking loop finishes, the window `[left, right]` is valid: `currentSum <= maxChars`. More importantly, `left` is the smallest index that makes this specific `right` valid, so the window is the longest valid one ending at `right`.

5. **Update the answer.**  
   Compute `windowLength = right - left + 1` and update `best = max(best, windowLength)`.

6. **Why this is correct.**  
   Every fragment is added once and removed at most once. No longer valid window ending at `right` is skipped, because shrinking stops at the first valid position. That guarantees `best` captures the maximum over all contiguous windows.

## 📊 Worked Example
Example: `words = ["a","bb","c","ddd","e"]`, `maxChars = 4`

| right | word  | len | currentSum after add | shrink? | left after shrink | valid window      | best |
|------:|-------|----:|---------------------:|---------|------------------:|-------------------|-----:|
| 0     | a     | 1   | 1                    | No      | 0                 | ["a"]             | 1    |
| 1     | bb    | 2   | 3                    | No      | 0                 | ["a","bb"]        | 2    |
| 2     | c     | 1   | 4                    | No      | 0                 | ["a","bb","c"]    | 3    |
| 3     | ddd   | 3   | 7                    | Yes     | 2                 | ["c","ddd"]       | 3    |
| 4     | e     | 1   | 5                    | Yes     | 3                 | ["ddd","e"]       | 3    |

Trace summary: the window grows until it violates the budget at `"ddd"`. Then the left pointer advances just enough to restore validity. The maximum valid span found is length `3`, from `["a","bb","c"]`.

## ⏱ Complexity Analysis
### Time Complexity
`O(n)`, where `n = words.length`. The dominant work is advancing the two pointers: each fragment is added to the window once and removed at most once. This remains practical at `10^6` elements, whereas `O(n^2)` becomes infeasible long before that and is completely non-viable near `10^9`.

### Space Complexity
`O(1)` auxiliary space. The algorithm stores only pointer indices, the running sum, and the best length. No prefix array or secondary structure is required. You could precompute lengths for readability, but that would raise space to `O(n)` without improving asymptotic runtime.

## 💡 Key Takeaways
- If the problem asks for the longest or shortest **contiguous** range under a threshold and all contributions are non-negative, think sliding window immediately.
- Ordered input plus a running budget is a strong signal that two pointers can replace nested subarray scans.
- Do not count spaces or separators here; only `words[i].length` contributes to the budget.
- Update the answer only **after** shrinking the window back to a valid state, or you will record invalid lengths.
- In production systems, this pattern is valuable because it converts repeated full-range recomputation into incremental state maintenance with stable linear cost.

## 🚀 Variations & Further Practice
- **Longest subarray with sum at most `K` when values can be negative** — sliding window breaks because the monotonicity disappears; requires prefix sums plus a more advanced structure.
- **Minimum-length subarray with sum at least `K`** — same two-pointer family for positive values, but the optimization target flips from maximizing width to minimizing it.
- **Window budget with multiple constraints** — e.g., total characters and distinct fragment count; still a sliding window, but validity now depends on maintaining additional state such as frequency maps.