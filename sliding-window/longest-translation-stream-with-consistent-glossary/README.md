# Longest Translation Stream With Consistent Glossary

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Sliding Window &nbsp;|&nbsp; **Tags:** Sliding Window, Hash Map, Two Pointers

---

## 🗂 Problem Overview
Given an array of translated terms `words` and an integer `k`, find the maximum length of a contiguous subarray where every distinct word appears at most `k` times. Return that length.

The challenge is not counting frequencies once, but maintaining them over all possible contiguous segments efficiently. With up to `100000` elements, checking every subarray is too expensive, so the solution must update validity incrementally as the window moves.

## 🌍 Engineering Impact
This pattern shows up anywhere systems must enforce bounded repetition over a moving stream: rate-limiters keyed by tenant or token, streaming anomaly detection, log pipelines detecting bursty event classes, and ranking systems limiting repeated features in a candidate window. At scale, brute-force rescans collapse under throughput and latency requirements because each new event would trigger work proportional to prior history. A sliding-window frequency model turns that into constant-amortized update cost, which is what enables online enforcement, low-latency review tooling, and memory-bounded stream processing instead of batch-only post-analysis.

## 🔍 Problem Statement
You are given a string array `words` and an integer `k`. You must return the length of the longest contiguous subarray such that each distinct string inside that subarray appears no more than `k` times.

Constraints:
- `1 <= words.length <= 100000`
- `0 <= k <= words.length`
- `1 <= words[i].length <= 20`
- `words[i]` contains lowercase English letters only

Edge case: if `k = 0`, no word may appear even once, so the answer is always `0`.

Examples:

- `words = ["uno","dos","uno","tres","dos","uno","cuatro"], k = 2`  
  Output: `5`

- `words = ["bleu","bleu","bleu","vert","vert","jaune"], k = 1`  
  Output: `3`

The key constraint is the input size: `O(n^2)` subarray enumeration is not acceptable, so the algorithm must process the stream in near-linear time.

## 🪜 How to Solve This
1. Read the requirement carefully → this is about a **contiguous** segment, so sorting or global counting is irrelevant.
2. Notice the validity rule depends only on **frequencies inside the current segment** → that strongly suggests a sliding window.
3. Expand a right pointer one word at a time and record counts in a hash map.
4. Ask what makes a window invalid → only one thing: some word’s count becomes `k + 1`.
5. Once invalid, don’t restart from scratch. Move the left pointer forward, decrementing counts, until the violation disappears.
6. At every step, the current window is the longest valid window ending at `right`, because we shrink only as much as needed.
7. Track the maximum window length seen during the scan.

Why this works: each element enters the window once and leaves once. That gives linear behavior, while the hash map gives constant-average frequency updates. This is the standard “grow until invalid, shrink until valid” pattern for local frequency constraints over contiguous data.

## 🧩 Algorithm Walkthrough
1. **Handle the zero-cap case early.**  
   If `k == 0`, return `0`. No non-empty subarray can satisfy the constraint, so this avoids unnecessary work.

2. **Initialize the sliding window state.**  
   Use two pointers: `left = 0` and a loop variable `right`. Maintain a hash map `freq` from word to count, plus `best = 0`.

3. **Expand the window to the right.**  
   For each `words[right]`, increment `freq[words[right]]`. This adds one new element to the current candidate segment.

4. **Restore validity when a count exceeds `k`.**  
   The pattern here is **Two Pointers / Sliding Window**. If the newly added word now has count `k + 1`, the window is invalid. Move `left` forward, decrementing `freq[words[left]]`, until the offending word’s count is back to `<= k`.

5. **Maintain the invariant.**  
   After shrinking, the window `[left, right]` is valid: every word appears at most `k` times. This invariant holds after every iteration.

6. **Update the answer.**  
   Compute `right - left + 1` and update `best`. Because `left` is minimal after restoration, this is the longest valid window ending at `right`.

7. **Why it is correct.**  
   We never discard a potentially better valid window unnecessarily. The left boundary moves only when required to fix a violation, so every maximal valid suffix for each `right` is considered exactly once.

## 📊 Worked Example
Example: `words = ["uno","dos","uno","tres","dos","uno","cuatro"]`, `k = 2`

| right | word     | freq after add                  | left after shrink | valid window                              | best |
|------:|----------|----------------------------------|-------------------|-------------------------------------------|-----:|
| 0     | uno      | {uno:1}                          | 0                 | ["uno"]                                   | 1    |
| 1     | dos      | {uno:1, dos:1}                   | 0                 | ["uno","dos"]                             | 2    |
| 2     | uno      | {uno:2, dos:1}                   | 0                 | ["uno","dos","uno"]                       | 3    |
| 3     | tres     | {uno:2, dos:1, tres:1}           | 0                 | ["uno","dos","uno","tres"]                | 4    |
| 4     | dos      | {uno:2, dos:2, tres:1}           | 0                 | ["uno","dos","uno","tres","dos"]          | 5    |
| 5     | uno      | {uno:3, dos:2, tres:1}           | 1                 | ["dos","uno","tres","dos","uno"]          | 5    |
| 6     | cuatro   | {uno:2, dos:2, tres:1, cuatro:1} | 1                 | ["dos","uno","tres","dos","uno","cuatro"] | 6    |

Final answer: `6`.

## ⏱ Complexity Analysis
### Time Complexity
`O(n)` average time, where `n = words.length`. Each word is added to the window once by the right pointer and removed at most once by the left pointer. Hash map increments/decrements are constant-average cost. At `10^6` elements this remains practical; at `10^9`, linear scan cost dominates and becomes a system-level throughput concern.

### Space Complexity
`O(m)`, where `m` is the number of distinct words in the current window, bounded by `n`. The hash map owns the extra space. You can’t meaningfully reduce this without losing constant-time frequency updates; the trade-off would be slower lookups or repeated rescans.

## 💡 Key Takeaways
- If the problem asks for the longest **contiguous** region under a frequency constraint, think sliding window before considering any global counting strategy.
- If validity can be restored by moving one boundary monotonically forward, that is a strong signal for a Two Pointers solution.
- Handle `k = 0` explicitly; otherwise it is easy to accidentally return `1` for a non-empty input.
- Update the answer only after shrinking back to a valid state; recording length too early captures invalid windows.
- The production-grade insight is to maintain local invariants incrementally instead of recomputing state for every candidate range.

## 🚀 Variations & Further Practice
- Longest subarray with **at most `K` distinct words**: same sliding-window pattern, but the constraint is on cardinality rather than per-key frequency.
- Longest subarray where **every word appears exactly `k` times**: harder because validity is no longer monotonic under expansion alone.
- Streaming version with **time-based eviction** instead of index-based eviction: same frequency accounting, but the left boundary is driven by timestamps and out-of-order handling becomes the real complexity.