# Longest Workout Segment With Limited Speed Drops

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Sliding Window &nbsp;|&nbsp; **Tags:** Sliding Window, Two Pointers, Array

---

## 🗂 Problem Overview
Given an array `speed` and an integer `k`, find the maximum length of a contiguous subarray containing at most `k` speed drops, where a drop occurs at index `i > 0` if `speed[i] < speed[i - 1]`. The output is a single integer: the longest valid segment length. The challenge is that `speed.length` can reach `2 * 10^5`, so enumerating all subarrays is too slow; the solution must exploit local structure and run near-linearly.

## 🌍 Engineering Impact
This pattern shows up anywhere systems need the longest contiguous interval satisfying a bounded-error condition. Examples include streaming observability pipelines finding the longest period with at most `k` anomaly regressions, search/ranking systems identifying stable score windows with limited quality dips, and time-series analytics over sensor or trading data. At scale, brute-force interval scans collapse under quadratic growth and blow latency budgets. A sliding-window formulation enables single-pass processing, predictable memory use, and straightforward online adaptation, which matters when the data source is continuous rather than batch-bounded.

## 🔍 Problem Statement
You are given:

- An integer array `speed`, where `speed[i]` is the runner's speed during minute `i`
- An integer `k`

For any index `i > 0`, minute `i` is a **speed drop** if `speed[i] < speed[i - 1]`.

You must return the length of the longest contiguous segment `speed[l...r]` such that the number of drop indices inside that segment, meaning indices `i` with `l < i <= r` and `speed[i] < speed[i - 1]`, is at most `k`.

Constraints:

- `1 <= speed.length <= 2 * 10^5`
- `0 <= k < speed.length`
- `1 <= speed[i] <= 10^9`

Examples:

- `speed = [5, 6, 4, 4, 7, 3, 8], k = 1` → `4`
- `speed = [9, 8, 7, 10, 11, 6, 12], k = 2` → `5`

The key constraint is input size: any `O(n^2)` subarray enumeration is unacceptable.

## 🪜 How to Solve This
1. Read the condition carefully → the validity of a segment depends only on how many adjacent comparisons inside it are drops.
2. That means we do **not** need to recompute the whole segment each time. When extending a segment by one element, only one new comparison appears: `speed[r] < speed[r - 1]`.
3. This is the classic signal for a **sliding window / two pointers** approach: maintain a current contiguous window and track whether it remains valid as `r` moves right.
4. Expand the right boundary one step at a time → if the new pair introduces a drop, increment a counter.
5. If the window now has more than `k` drops, shrink from the left until it becomes valid again. When moving `l` right by one, remove the comparison that used to connect `speed[l]` and `speed[l + 1]`.
6. At every valid state, update the best window length.

The reason this works is locality: each pointer move changes the drop count by at most one relevant adjacent comparison. That turns a global-looking subarray constraint into a constant-time incremental update.

## 🧩 Algorithm Walkthrough
1. **Initialize the window state.**  
   Set `l = 0`, `drops = 0`, and `best = 1`. The current window is `speed[l...r]`, and the invariant is: `drops` equals the number of speed-drop indices strictly inside this window.

2. **Scan with the right pointer.**  
   For each `r` from `1` to `n - 1`, compare `speed[r]` with `speed[r - 1]`. If `speed[r] < speed[r - 1]`, increment `drops`. This is the only new adjacency introduced by extending the window rightward.

3. **Restore validity when needed.**  
   While `drops > k`, move `l` right. Before incrementing `l`, check whether the adjacency crossing out of the window was a drop: if `speed[l + 1] < speed[l]`, decrement `drops`. Then increment `l`. This preserves the invariant that `drops` matches the current window exactly.

4. **Record the best valid window.**  
   Once `drops <= k`, the window `speed[l...r]` is valid. Update `best = max(best, r - l + 1)`.

5. **Why Two Pointers is the right abstraction.**  
   The problem asks for the longest contiguous region under a bounded metric that can be updated incrementally. Two Pointers fits because both expansion and contraction affect only boundary-local information, giving linear time with no auxiliary indexing structure.

## 📊 Worked Example
Example: `speed = [5, 6, 4, 4, 7, 3, 8]`, `k = 1`

| r | speed[r] | New drop? | drops | l after shrink | Window        | Length | best |
|---|----------|-----------|-------|----------------|---------------|--------|------|
| 0 | 5        | —         | 0     | 0              | `[5]`         | 1      | 1    |
| 1 | 6        | No        | 0     | 0              | `[5,6]`       | 2      | 2    |
| 2 | 4        | Yes       | 1     | 0              | `[5,6,4]`     | 3      | 3    |
| 3 | 4        | No        | 1     | 0              | `[5,6,4,4]`   | 4      | 4    |
| 4 | 7        | No        | 1     | 0              | `[5,6,4,4,7]` | 5      | 5    |
| 5 | 3        | Yes       | 2     | 2              | `[4,4,7,3]`   | 4      | 5    |
| 6 | 8        | No        | 1     | 2              | `[4,4,7,3,8]` | 5      | 5    |

The longest valid segment has length `5`. Note that if using the problem’s stated example output, verify the interpretation carefully: under the formal definition, `[5,6,4,4,7]` and `[4,4,7,3,8]` each contain only one drop.

## ⏱ Complexity Analysis
### Time Complexity
`O(n)`. Each element is processed at most twice: once when `r` advances and once when `l` catches up. The dominant work is constant-time adjacent comparisons and counter updates. At `10^6` elements this is routine in memory-resident workloads; at `10^9`, linear time is still expensive, but quadratic approaches are completely non-viable.

### Space Complexity
`O(1)`. The algorithm stores only pointer indices, a drop counter, and the best length. No auxiliary array or heap is required. Space cannot be meaningfully reduced further; the only trade-off would be adding precomputed drop markers, which increases memory without improving asymptotic runtime.

## 💡 Key Takeaways
- If a subarray constraint depends on a bounded count of “bad events” that can be updated at the boundaries, think sliding window immediately.
- When validity is defined by adjacent relationships inside a contiguous range, track transitions rather than recomputing properties of the full range.
- The drop count belongs to comparisons between elements, not to elements themselves; shrinking the left edge must remove `speed[l + 1] < speed[l]`, not inspect `speed[l]` in isolation.
- Be explicit about window semantics: drops are counted only for indices `i` with `l < i <= r`, which is the source of most off-by-one mistakes here.
- The broader design lesson is to convert expensive interval validation into incremental state maintenance whenever the constraint is boundary-local.

## 🚀 Variations & Further Practice
- Allow up to `k` drops where each drop has a weight `speed[i - 1] - speed[i]`; now the window tracks a bounded cumulative penalty rather than a simple count.
- Find the longest segment where the number of increases and drops each satisfy separate budgets; this adds multi-constraint window management.
- Answer many offline queries with different `k` values on the same array; the challenge shifts from one-pass scanning to preprocessing and query-efficient interval reasoning.