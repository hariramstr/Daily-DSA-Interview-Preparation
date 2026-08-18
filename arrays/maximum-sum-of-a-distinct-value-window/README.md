# Maximum Sum of a Distinct-Value Window

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Arrays &nbsp;|&nbsp; **Tags:** Arrays, Sliding Window, Hash Map

---

## 🗂 Problem Overview
Given an integer array `nums` and a window size `k`, compute the maximum sum across all contiguous subarrays of length exactly `k` whose elements are all distinct. If no such window exists, return `0`. The non-trivial part is enforcing uniqueness and tracking sums efficiently under large input sizes: `nums.length` can reach `200000`, so recomputing each window’s sum and duplicate status independently is too expensive.

## 🌍 Engineering Impact
This pattern shows up anywhere systems evaluate fixed-size contiguous slices under uniqueness constraints: streaming dedup pipelines, fraud detection over event windows, search ranking over recent impressions, telemetry aggregation, and distributed rate-limiters keyed by recent request IDs. At scale, naive rescans turn a linear pass into a quadratic bottleneck and destroy latency budgets. The sliding-window plus frequency-map approach enables single-pass evaluation with bounded per-step work, which is exactly what production systems need when processing high-volume streams, rolling metrics, or online feature computation without materializing or revalidating every candidate range from scratch.

## 🔍 Problem Statement
You are given an integer array `nums` and an integer `k`, where:

- `1 <= nums.length <= 200000`
- `1 <= nums[i] <= 1000000000`
- `1 <= k <= nums.length`

A valid window is any contiguous subarray of length exactly `k` in which all `k` values are pairwise distinct. Return the maximum sum among all valid windows. If no valid window exists, return `0`.

Examples:

- `nums = [5,2,3,5,4,6], k = 3` → `15`  
  Valid windows: `[5,2,3]`, `[2,3,5]`, `[3,5,4]`, `[5,4,6]`  
  Sums: `10, 10, 12, 15`

- `nums = [4,4,2,1,2], k = 3` → `7`  
  Windows: `[4,4,2]` invalid, `[4,2,1]` valid, `[2,1,2]` invalid

The key constraint is input size: checking every window from scratch would be too slow, so the algorithm must maintain rolling state incrementally.

## 🪜 How to Solve This
1. Read the problem → we need every contiguous range of fixed length `k`, so this is immediately a sliding-window candidate.

2. Notice two things must be maintained simultaneously:
   - the sum of the current window
   - whether the current window has duplicates

3. Recomputing either from scratch per window is wasteful. A window moves by one element, so most of its state is unchanged. That suggests incremental updates.

4. For the sum, keep a running total:
   - add the incoming value
   - subtract the outgoing value

5. For distinctness, keep a frequency map of values inside the current window. A window is valid exactly when the map contains `k` keys, because the window length is already `k`.

6. Slide left to right once:
   - expand by one element
   - if the window grows beyond `k`, evict the leftmost element and update its count
   - when window size becomes `k`, check whether all values are distinct and update the answer

7. This yields a single-pass solution with constant work per move, aside from hash map operations.

## 🧩 Algorithm Walkthrough
1. **Use the Sliding Window + Hash Map pattern.**  
   Sliding Window is the right abstraction because the problem asks about all contiguous subarrays of fixed size. A hash map is required because validity depends on duplicate detection, not ordering.

2. **Initialize state.**  
   Track:
   - `left` pointer for the window start
   - `windowSum` as a running sum
   - `freq` map from value → count in the current window
   - `best` as the maximum valid sum seen so far

3. **Iterate `right` from `0` to `nums.length - 1`.**  
   Add `nums[right]` to `windowSum` and increment its count in `freq`.  
   Invariant: after insertion, `freq` and `windowSum` describe the window `[left, right]`.

4. **Shrink when the window exceeds size `k`.**  
   If `right - left + 1 > k`, remove `nums[left]`:
   - subtract it from `windowSum`
   - decrement its count in `freq`
   - delete the key if the count reaches zero
   - increment `left`  
   Invariant: after shrinking, window size is at most `k`, and state remains exact.

5. **Evaluate only full windows.**  
   When `right - left + 1 == k`, the window is valid iff `freq.size == k`.  
   Why this works: the window already has `k` elements, so having `k` distinct keys means every element appears exactly once.

6. **Update the answer.**  
   If valid, set `best = max(best, windowSum)`.

7. **Return `best`.**  
   If no valid window was found, `best` remains `0`, which matches the required output.

## 📊 Worked Example
Example: `nums = [4,4,2,1,2]`, `k = 3`

| Step | Window | `windowSum` | `freq` | Valid? | `best` |
|---|---|---:|---|---|---:|
| Add `4` | `[4]` | 4 | `{4:1}` | No, size < 3 | 0 |
| Add `4` | `[4,4]` | 8 | `{4:2}` | No, size < 3 | 0 |
| Add `2` | `[4,4,2]` | 10 | `{4:2,2:1}` | No, `freq.size = 2` | 0 |
| Add `1`, remove left `4` | `[4,2,1]` | 7 | `{4:1,2:1,1:1}` | Yes | 7 |
| Add `2`, remove left `4` | `[2,1,2]` | 5 | `{2:2,1:1}` | No, `freq.size = 2` | 7 |

Only `[4,2,1]` is a valid length-3 window. Its sum is `7`, so the answer is `7`.

## ⏱ Complexity Analysis
### Time Complexity
`O(n)` where `n = nums.length`. Each element enters the window once and leaves once, and each update performs `O(1)` expected-time hash map operations. At `10^6` elements this remains practical in a single pass; at `10^9`, the algorithmic shape is still optimal, though memory bandwidth and runtime become system-level constraints.

### Space Complexity
`O(min(n, k))`, dominated by the frequency map for values currently inside the window. In the worst case, all `k` elements are distinct, so the map holds `k` keys. This cannot be meaningfully reduced without giving up constant-time duplicate tracking.

## 💡 Key Takeaways
- Fixed-size contiguous subarray + “evaluate every window” is a strong signal for Sliding Window rather than nested loops.
- If validity depends on duplicates, counts, or membership inside the current range, pair the window with a hash map or frequency table.
- The validity check is `freq.size == k`, not “no count > 1” scanned each time; rescanning the map would quietly reintroduce extra cost.
- Be careful about update order: add the new element, shrink if size exceeds `k`, then evaluate only when the window size is exactly `k`.
- The production lesson is broader than this problem: when adjacent candidate ranges overlap heavily, maintain incremental state instead of recomputing derived properties per range.

## 🚀 Variations & Further Practice
- **Maximum sum of a window with at most `m` distinct values** — same sliding-window core, but validity is based on an upper bound rather than exact uniqueness.
- **Longest subarray with all distinct values** — removes fixed-size windows, so the shrink logic becomes data-dependent and the optimization target changes from sum to length.
- **Maximum sum of a distinct-value subsequence under ordering constraints** — contiguous structure disappears, so the local incremental window invariant no longer applies directly.