# Longest Sensor Batch With Limited Duplicate Device IDs

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Sliding Window &nbsp;|&nbsp; **Tags:** Sliding Window, Hash Map, Two Pointers

---

## 🗂 Problem Overview
Given an integer array `deviceIds` and an integer `k`, find the maximum length of a contiguous subarray in which every distinct device ID appears at most `k` times. The output is a single integer: the longest valid window length. The challenge is that the array can contain up to 200,000 readings, so brute-force enumeration of all subarrays is too slow; the solution must exploit window structure and incremental frequency tracking.

## 🌍 Engineering Impact
This pattern shows up anywhere systems need the longest valid contiguous slice under per-key frequency constraints: streaming observability pipelines, fraud detection windows, log ingestion, ad-serving session analysis, and distributed rate-limiters. At scale, naive rescans or per-window recomputation collapse under high-cardinality streams and tight latency budgets. A sliding-window plus hash-map approach enables single-pass processing, bounded incremental state, and predictable throughput. Architecturally, it is the difference between a design that can operate inline on hot paths and one that requires expensive batch post-processing or approximate heuristics.

## 🔍 Problem Statement
You are given:

- `deviceIds`: an integer array of length `1 <= n <= 200000`
- `k`: an integer where `1 <= k <= n`

Return the length of the longest **contiguous** subarray such that, within that subarray, every distinct device ID appears at most `k` times.

Important constraints:

- `deviceIds[i]` can be as large as `10^9`, so direct indexing by value is not practical.
- The solution should run in `O(n)` or `O(n log n)` time.
- Only continuous segments are allowed; reordering or skipping elements is not allowed.

Examples:

- `deviceIds = [4, 1, 4, 2, 4, 1, 2, 2], k = 2` → `5`
- `deviceIds = [7, 7, 3, 7, 3, 3, 8], k = 1` → `2`

The key algorithmic pressure comes from the input size: quadratic subarray checks are immediately disqualified.

## 🪜 How to Solve This
1. Read the problem → the word **contiguous** is the main signal. That usually points to a window, not global counting.
2. The validity rule is local to the current segment: every device ID must have frequency `<= k`. That means we need fast updates as the segment expands and shrinks.
3. Fast per-ID counting → use a `HashMap<deviceId, count>`, because IDs are large and sparse.
4. Start with a window `[left, right]` and grow `right` one step at a time. Each new reading increases exactly one count.
5. If adding `deviceIds[right]` makes its count exceed `k`, the window becomes invalid. Since only one count changed, only that violation needs to be repaired.
6. Repair by moving `left` forward, decrementing counts, until the offending device is back within limit.
7. After each repair, the window is valid again, so record its length.
8. This works because both pointers move only forward. No element is added or removed more than once, which gives linear time.

## 🧩 Algorithm Walkthrough
1. **Choose the pattern: Sliding Window with Two Pointers.**  
   The problem asks for the longest contiguous segment under a mutable validity condition. That is the canonical use case for two pointers: one pointer expands the candidate window, the other contracts it only when needed.

2. **Maintain a frequency map for the current window.**  
   Let `count[id]` be the number of times a device ID appears between `left` and `right`, inclusive. This map is the complete state needed to evaluate validity.

3. **Expand the window by advancing `right`.**  
   For each `deviceIds[right]`, increment its frequency. At this moment, only one invariant can break: `count[deviceIds[right]] <= k`.

4. **Restore validity by advancing `left`.**  
   While the just-added device has count greater than `k`, decrement `count[deviceIds[left]]` and move `left` rightward. This is correct because any valid window ending at `right` must exclude enough earlier elements to remove the excess occurrence.

5. **Record the best valid length.**  
   Once the loop stops, the window `[left, right]` is valid: every count is `<= k`. Update `best = max(best, right - left + 1)`.

6. **Preserve the invariant throughout the scan.**  
   After each iteration, the maintained invariant is: the current window is valid, and `left` is the smallest index that makes it valid for this `right`. That minimality matters because it guarantees the longest valid window ending at `right`.

7. **Finish in linear time.**  
   Each index enters the window once via `right` and leaves at most once via `left`. That bounded movement is why the abstraction is the right one here.

## 📊 Worked Example
Example: `deviceIds = [4, 1, 4, 2, 4, 1, 2, 2]`, `k = 2`

| right | value | action | left | counts after repair | window | best |
|---|---:|---|---:|---|---|---:|
| 0 | 4 | add 4 | 0 | {4:1} | [4] | 1 |
| 1 | 1 | add 1 | 0 | {4:1,1:1} | [4,1] | 2 |
| 2 | 4 | add 4 | 0 | {4:2,1:1} | [4,1,4] | 3 |
| 3 | 2 | add 2 | 0 | {4:2,1:1,2:1} | [4,1,4,2] | 4 |
| 4 | 4 | add 4, invalid | 1 | {4:2,1:1,2:1} | [1,4,2,4] | 4 |
| 5 | 1 | add 1 | 1 | {4:2,1:2,2:1} | [1,4,2,4,1] | 5 |
| 6 | 2 | add 2 | 1 | {4:2,1:2,2:2} | [1,4,2,4,1,2] | 6 |
| 7 | 2 | add 2, invalid | 4 | {4:1,1:1,2:2} | [4,1,2,2] | 6 |

The longest valid batch has length `6`: `[1, 4, 2, 4, 1, 2]`.

## ⏱ Complexity Analysis
### Time Complexity
`O(n)` expected time with a hash map. Each element is processed once when `right` expands and at most once when `left` contracts, so the dominant work is constant-time map updates per pointer movement. This scales comfortably to `10^6` elements; at `10^9`, linear time is still the only viable exact approach, though memory and I/O become the real bottlenecks.

### Space Complexity
`O(m)`, where `m` is the number of distinct device IDs present in the current window or scan prefix, owned by the frequency map. In the worst case, `m = O(n)`. Space can only be reduced by using approximate counting or value compression, both of which introduce trade-offs in correctness or preprocessing cost.

## 💡 Key Takeaways
- If the problem asks for the **longest contiguous segment** under a condition that can be updated incrementally, think sliding window before considering anything heavier.
- If validity depends on **per-key frequencies inside the current range**, a hash map plus two pointers is usually the right first abstraction.
- Shrink only while the window is invalid; shrinking earlier loses candidate length and often breaks correctness.
- Be careful with when you update the answer: do it only **after** restoring validity, using `right - left + 1`.
- In production stream processing, this pattern is the exact move from repeated recomputation to bounded incremental state and stable throughput.

## 🚀 Variations & Further Practice
- Longest subarray with **at most `K` distinct values**: same window pattern, but the constraint is on cardinality rather than per-value frequency.
- Longest subarray where **total replacements needed to homogenize the window** is at most `K`: harder because validity depends on window size and the maximum frequency, not just a direct threshold check.
- Count the **number of valid subarrays** instead of the longest one: same mechanics, but the aggregation changes from `max length` to summing valid suffix counts per `right`.