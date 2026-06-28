# Shortest Restock Span Covering All Essential Products

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Arrays &nbsp;|&nbsp; **Tags:** Arrays, Sliding Window, Hash Map

---

## 🗂 Problem Overview
Given a restock stream and a distinct set of essential product IDs, find the minimum-length contiguous subarray that contains every essential ID at least once. Return only that length, or `-1` if coverage is impossible. The challenge is scale: both arrays can reach `200,000` elements, so any approach that checks many candidate spans explicitly or recomputes membership from scratch will not survive production-sized inputs.

## 🌍 Engineering Impact
This pattern shows up anywhere systems need the smallest interval proving full coverage of required entities: observability pipelines finding the shortest log segment containing all critical event types, ad-tech or search systems locating the minimum impression span covering mandatory features, and warehouse or supply-chain analytics proving all regulated SKUs were replenished within a bounded period. At scale, brute-force interval scans collapse under quadratic behavior and cache-unfriendly rescans. A sliding-window design enables single-pass processing, bounded memory, and predictable latency, which matters in streaming services, online dashboards, and batch jobs operating over millions of ordered events.

## 🔍 Problem Statement
You are given:

- `restocks`: an integer array where `restocks[i]` is the product ID of the `i`-th restocked item
- `essentials`: an integer array of distinct product IDs that must all appear in the chosen span

Return the length of the shortest contiguous subarray of `restocks` that contains every value in `essentials` at least once. If no such subarray exists, return `-1`.

Key constraints:

- `1 <= restocks.length <= 200000`
- `1 <= essentials.length <= 200000`
- `1 <= product ID <= 1000000000`
- `essentials` contains distinct values

Examples:

- `restocks = [7, 2, 5, 2, 9, 5, 1, 9, 7]`, `essentials = [5, 1, 7]` → `4`
- `restocks = [4, 4, 3, 8, 6, 3, 2]`, `essentials = [3, 2, 5]` → `-1`

The decisive constraint is input size: anything worse than near-linear time is not viable.

## 🪜 How to Solve This
1. Read the problem → we need a **contiguous** segment, so sorting is off the table. Order matters because the answer is a span in the original stream.

2. We do not care about counts of every product, only whether the current window contains all required IDs at least once. That points to a membership structure: put `essentials` into a hash-based set or map.

3. Once you think “smallest contiguous window containing a target set,” the standard pattern is **sliding window / two pointers**:
   - expand the right pointer until the window becomes valid
   - then shrink the left pointer as much as possible while keeping it valid

4. To know when the window is valid, track counts only for essential products currently inside the window. Also track how many distinct essentials are currently satisfied.

5. Every element enters the window once and leaves once. That gives linear traversal instead of testing all subarrays.

6. If the window never satisfies all essentials, return `-1`; otherwise return the minimum length seen during contraction.

## 🧩 Algorithm Walkthrough
1. **Preprocess required products**  
   Store all values from `essentials` in a hash set for O(1) membership checks. Maintain a hash map `freq` for counts of essential IDs inside the current window. Also keep `formed`, the number of essential IDs whose count is currently at least 1.  
   **Invariant:** `formed` accurately reflects how many required IDs are covered by the current window.

2. **Initialize the sliding window**  
   Set `left = 0`, `best = +∞`. Iterate `right` from `0` to `restocks.length - 1`.  
   This is the classic **Two Pointers / Sliding Window** pattern because the target property is monotonic: adding elements can make a window valid, and removing elements can invalidate it.

3. **Expand the window with `right`**  
   If `restocks[right]` is essential, increment `freq[id]`. If its count changes from `0` to `1`, increment `formed`.  
   **Why correct:** a required product contributes to coverage exactly when it first appears in the window.

4. **Shrink from the left while valid**  
   While `formed == essentials.length`, the window covers all required IDs. Update `best` with `right - left + 1`. Then inspect `restocks[left]` and move `left` forward:
   - if it is non-essential, discard it freely
   - if it is essential, decrement its count
   - if its count drops from `1` to `0`, decrement `formed` and stop shrinking  
   **Invariant:** when the inner loop ends, the window is minimal for that `right` or just became invalid.

5. **Finish and return**  
   After scanning all positions, return `best` if updated; otherwise `-1`.  
   **Why linear:** each index is processed at most twice—once by `right`, once by `left`.

## 📊 Worked Example
Example: `restocks = [7, 2, 5, 2, 9, 5, 1, 9, 7]`, `essentials = [5, 1, 7]`

| Step | right | value | left | formed | Window | best |
|---|---:|---:|---:|---:|---|---:|
| 1 | 0 | 7 | 0 | 1 | [7] | ∞ |
| 2 | 2 | 5 | 0 | 2 | [7,2,5] | ∞ |
| 3 | 6 | 1 | 0 | 3 | [7,2,5,2,9,5,1] | 7 |
| 4 | shrink | - | 1 | 3 | [2,5,2,9,5,1] | 6 |
| 5 | shrink | - | 2 | 3 | [5,2,9,5,1] | 5 |
| 6 | shrink | - | 3 | 2 | removed 5, invalid | 5 |
| 7 | 8 | 7 | 3 | 3 | [2,9,5,1,9,7] | 5 |
| 8 | shrink | - | 5 | 3 | [5,1,9,7] | 4 |
| 9 | shrink | - | 6 | 2 | removed 5, invalid | 4 |

Shortest valid span length is `4`.

## ⏱ Complexity Analysis
### Time Complexity
`O(n + m)`, where `n = restocks.length` and `m = essentials.length`. Building the essential set costs `O(m)`, and the sliding window scans `restocks` in amortized `O(n)` because each element is added and removed at most once. This remains practical at `10^6` scale; at `10^9`, even linear scans become throughput-bound and require streaming or distributed partitioning.

### Space Complexity
`O(m)` for the essential set and the frequency map over required IDs. Space is owned entirely by hash-based membership/count structures. You cannot reduce it below the number of required distinct products without sacrificing O(1) lookups or reintroducing repeated scans.

## 💡 Key Takeaways
- If the problem asks for the **smallest contiguous segment** satisfying a coverage condition, think **sliding window with two pointers** immediately.
- If validity depends on whether a window contains a required set of values, a **hash set + frequency map + satisfied-counter** is the usual signal.
- Be precise about when `formed` changes: increment only when a required count goes `0 → 1`, decrement only on `1 → 0`.
- The answer is `right - left + 1`; most off-by-one bugs come from updating length before or after moving `left`.
- In production systems, this pattern is valuable because it converts repeated interval validation into a single-pass state machine with predictable latency and memory.

## 🚀 Variations & Further Practice
- Return the actual subarray boundaries, not just the length; same pattern, but now you must preserve the best `[left, right]` pair and reason carefully about tie-breaking.
- Require each essential product to appear with a minimum multiplicity, e.g. product `5` at least twice; this changes validity from set coverage to multiset coverage.
- Solve the streaming version where `restocks` is unbounded and you need rolling shortest valid spans online; the conceptual twist is maintaining correctness under continuous ingestion and eviction.