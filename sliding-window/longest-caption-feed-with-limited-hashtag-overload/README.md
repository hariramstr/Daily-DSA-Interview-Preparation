# Longest Caption Feed With Limited Hashtag Overload

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Sliding Window &nbsp;|&nbsp; **Tags:** Sliding Window, Hash Map, Two Pointers

---

## 🗂 Problem Overview
Given an integer array `hashtags` and an integer `limit`, return the length of the longest contiguous subarray where every distinct value appears at most `limit` times. The output is a single integer: the maximum valid window size. The challenge is scale: with up to `200000` elements, checking all subarrays is prohibitively expensive, so the solution must update validity incrementally as the window moves.

## 🌍 Engineering Impact
This pattern shows up anywhere systems enforce bounded repetition over a rolling range: feed deduplication, abuse detection, per-key rate limiting, streaming anomaly detection, log compaction heuristics, and query/session analysis. In production, brute-force window validation collapses under high-throughput streams because each new event would trigger repeated rescans. A sliding-window plus frequency-map design turns that into incremental state maintenance: constant-time updates per event, bounded memory proportional to active cardinality, and predictable latency. That matters in pipelines where windows are evaluated continuously and tail latency, not just aggregate throughput, determines system behavior.

## 🔍 Problem Statement
You are given:

- `hashtags`: an array of integers representing hashtag IDs in chronological order
- `limit`: the maximum allowed frequency for any single hashtag inside a contiguous segment

Return the maximum length of a contiguous subarray such that every distinct value in that subarray appears no more than `limit` times.

Constraints:

- `1 <= hashtags.length <= 200000`
- `1 <= hashtags[i] <= 1000000000`
- `1 <= limit <= hashtags.length`

Examples:

- Input: `hashtags = [4, 1, 4, 2, 2, 4, 3]`, `limit = 2`  
  Output: `5`
- Input: `hashtags = [7, 7, 7, 8, 8, 9]`, `limit = 1`  
  Output: `3`

The key constraint is input size. An `O(n^2)` enumeration of subarrays is too slow, so the algorithm must process the array in essentially one pass while maintaining window validity dynamically.

## 🪜 How to Solve This
1. Read the problem → the word “contiguous” is the first signal. That usually points to a window, not a global counting strategy.

2. Notice the validity rule depends only on frequencies inside the current segment. That means if we know counts for the current window, we can decide whether adding one more element breaks the rule.

3. Expanding a window is easy: include `hashtags[right]` and increment its count.

4. If that count now exceeds `limit`, the window is invalid — but only because of the value we just added. We do not need to revalidate every key.

5. To restore validity, move `left` forward and decrement counts for elements removed from the window until the offending value’s count is back within the limit.

6. At every point after cleanup, the window is valid. Its length is `right - left + 1`, so update the best answer.

7. Because each pointer only moves forward, the total work stays linear. That is the core reason sliding window is the right mental model here: local violations can be repaired incrementally without restarting the scan.

## 🧩 Algorithm Walkthrough
1. **Initialize the sliding window state.**  
   Use two pointers: `left = 0` and `right` iterating from `0` to `n - 1`. Maintain a hash map `freq` from hashtag ID to its count in the current window, plus `best = 0`.  
   **Invariant:** before processing each answer update, `freq` matches exactly the subarray `hashtags[left..right]`.

2. **Expand the window to the right.**  
   For each `right`, increment `freq[hashtags[right]]`. This is the only new information introduced into the window.  
   **Why correct:** only the newly added value can cause a previously valid window to become invalid.

3. **Repair the window while invalid.**  
   If `freq[hashtags[right]] > limit`, advance `left` and decrement `freq[hashtags[left]]` for elements being removed until `freq[hashtags[right]] <= limit`.  
   **Invariant:** after this loop, every value in the window satisfies the frequency bound. We do not need to check all keys because all others were already valid before the insertion.

4. **Record the best valid length.**  
   Once repaired, compute `right - left + 1` and update `best`.  
   **Why correct:** this is the longest valid window ending at `right`, because any earlier `left` would still include too many copies of the offending value.

5. **Continue until the array is exhausted.**  
   This is the standard **Two Pointers / Sliding Window** pattern: one pointer grows the candidate range, the other pointer shrinks only when constraints are violated. Each element enters and leaves the window at most once, which gives linear time.

## 📊 Worked Example
Example: `hashtags = [4, 1, 4, 2, 2, 4, 3]`, `limit = 2`

| right | value | action | left | freq snapshot | window length | best |
|---|---:|---|---:|---|---:|---:|
| 0 | 4 | add 4 | 0 | {4:1} | 1 | 1 |
| 1 | 1 | add 1 | 0 | {4:1,1:1} | 2 | 2 |
| 2 | 4 | add 4 | 0 | {4:2,1:1} | 3 | 3 |
| 3 | 2 | add 2 | 0 | {4:2,1:1,2:1} | 4 | 4 |
| 4 | 2 | add 2 | 0 | {4:2,1:1,2:2} | 5 | 5 |
| 5 | 4 | add 4 → invalid | 1 | after shrinking: {4:2,1:0,2:2} | 5 | 5 |
| 6 | 3 | add 3 | 1 | {4:2,2:2,3:1} | 6 | 6 |

The longest valid segment is `[1, 4, 2, 2, 4, 3]`, length `6`. Every hashtag appears at most twice.

## ⏱ Complexity Analysis
### Time Complexity
`O(n)` where `n = hashtags.length`. Each element is added to the window once by `right` and removed at most once by `left`, so the dominant work is linear hash map updates. At `10^6` elements this is practical; at `10^9`, linear scan cost becomes infrastructure-bound rather than algorithmically avoidable.

### Space Complexity
`O(k)` where `k` is the number of distinct hashtag IDs in the current window, backed by the frequency hash map. In the worst case, `k` can be `O(n)`. You can trade generality for tighter memory only if the value domain is small enough for array indexing.

## 💡 Key Takeaways
- If the problem asks for the longest or shortest **contiguous** range under a frequency constraint, think sliding window before considering prefix or sorting approaches.
- If validity changes only when one element enters or leaves the range, that is a strong signal that incremental state with two pointers will beat rescanning.
- Shrink only while the just-added value violates `limit`; checking every key on each step is unnecessary and turns a linear solution into a slower one.
- Be precise with window length: after restoring validity, the current size is `right - left + 1`, not `right - left`.
- In production systems, this pattern is really about maintaining local invariants cheaply under streaming updates rather than recomputing global state.

## 🚀 Variations & Further Practice
- Return the actual longest subarray, not just its length. Same window logic, but now you must persist best boundaries and handle tie-breaking rules.
- Allow at most `K` distinct hashtags instead of per-hashtag frequency limits. The abstraction stays sliding window, but the invariant changes from bounded counts per key to bounded key cardinality.
- Find the number of valid subarrays instead of the longest one. The harder twist is converting each valid window into a count contribution without double-counting.