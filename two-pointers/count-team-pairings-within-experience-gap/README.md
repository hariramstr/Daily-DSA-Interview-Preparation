# Count Team Pairings Within Experience Gap

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Two Pointers &nbsp;|&nbsp; **Tags:** Two Pointers, Sorting, Array

---

## 🗂 Problem Overview
Given an array `experience` and an integer `gap`, count how many engineer pairs `(i, j)` with `i < j` satisfy `|experience[i] - experience[j]| <= gap`. The output is the total number of such compatible pairs. The challenge is scale: with up to `200000` engineers, checking every pair is quadratic and infeasible. The key observation is that compatibility depends only on value differences, so sorting enables a linear scan with two pointers.

## 🌍 Engineering Impact
This pattern shows up anywhere systems need to count or enumerate entities within a numeric tolerance window: matching bids and asks in trading systems, grouping near-duplicate events in streaming pipelines, pairing users by latency or skill bands in matchmaking, and identifying acceptable score deltas in ranking or recommendation systems. At production scale, the naive all-pairs approach collapses under cardinality growth and creates unacceptable CPU cost. Sorting plus a sliding window converts pairwise comparison into range counting, which is the difference between a batch job that times out and a service that remains predictable under load.

## 🔍 Problem Statement
You are given an integer array `experience` where `experience[i]` is the years of experience for the `i`-th engineer, and an integer `gap`. Count the number of pairs `(i, j)` such that `i < j` and `|experience[i] - experience[j]| <= gap`.

You may reorder the array during computation because the condition depends only on values, not original positions. However, the answer must count pairs from the original set of engineers.

Constraints:

- `1 <= experience.length <= 200000`
- `0 <= experience[i] <= 1000000000`
- `0 <= gap <= 1000000000`
- Use a 64-bit integer type for the answer

Examples:

- `experience = [1, 3, 4, 7], gap = 3` → `4`
- `experience = [5, 5, 5, 8, 10], gap = 0` → `3`

The algorithmic driver is the input size: `O(n^2)` pair checking is too slow, so the target is `O(n log n)` or better.

## 🪜 How to Solve This
1. Start with the condition: `|a - b| <= gap`. That immediately suggests the pair decision depends only on numeric distance, not identity or original index order.

2. If only value distance matters, sorting is safe. After sorting, absolute difference simplifies: for `i < j`, `experience[j] - experience[i]` is non-negative, so we only need to check whether it stays within `gap`.

3. Once sorted, think in terms of a moving valid range. For each left endpoint `l`, there is a maximal right endpoint `r` such that every element in `[l+1, r-1]` forms a valid pair with `l`.

4. That range boundary is monotonic. If `l` moves right, `r` never needs to move left. That is the signal for a two-pointers solution rather than binary searching for every index.

5. Count, don’t enumerate. When the window `[l, r)` is valid, engineer `l` forms pairs with exactly `r - l - 1` later engineers. Add that in one step, then advance `l`.

6. The result is an `O(n log n)` sort followed by an `O(n)` sweep, which is the right shape for `200000` elements.

## 🧩 Algorithm Walkthrough
1. **Sort the array in non-decreasing order.**  
   This is the enabling transformation. After sorting, for any `l < r`, the difference is `experience[r] - experience[l]`, so the absolute value disappears. That turns an unordered pair predicate into a directional window predicate.

2. **Initialize two pointers `l = 0` and `r = 0`, plus a 64-bit accumulator `count = 0`.**  
   The pattern here is **Two Pointers / Sliding Window over a sorted array**. The abstraction fits because we are maintaining the largest suffix starting at `l` that still satisfies a monotonic constraint.

3. **For each `l`, advance `r` while `r < n` and `experience[r] - experience[l] <= gap`.**  
   When this loop stops, `r` is the first index that violates the constraint, so the valid window is `[l, r)`. The invariant is: every index `k` with `l <= k < r` is within `gap` of `l`, and `r` is minimal outside that set.

4. **Add `r - l - 1` to the answer.**  
   Those are exactly the indices greater than `l` inside the valid window. Each corresponds to one compatible pair with left endpoint `l`. No pair is double-counted because each pair is charged once, when its smaller index in sorted order is the left pointer.

5. **Advance `l` by one and repeat without resetting `r`.**  
   This is where the linear scan comes from. Since the array is sorted, moving `l` right cannot make a previously invalid `r` become necessary to revisit on the left. Both pointers move forward at most `n` times.

6. **Return `count`.**  
   Correctness follows from exhaustive coverage of all sorted left endpoints and exact counting of their valid right partners.

## 📊 Worked Example
Example: `experience = [1, 3, 4, 7]`, `gap = 3`

After sorting: `[1, 3, 4, 7]`

| `l` | `r` after expansion | Valid window `[l, r)` | Pairs added | Running total |
|---|---:|---|---:|---:|
| 0 (`1`) | 3 | `[1, 3, 4]` | `3 - 0 - 1 = 2` | 2 |
| 1 (`3`) | 4 | `[3, 4, 7]` | `4 - 1 - 1 = 2` | 4 |
| 2 (`4`) | 4 | `[4, 7]` | `4 - 2 - 1 = 1` | 5? |

Careful: the window condition is relative to `l`. For `l = 2`, `7 - 4 = 3`, so one pair is valid. But the previous row already counted `(3,7)` and `(3,4)`, not `(4,7)`, so this is correct. The full trace gives pairs `(1,3)`, `(1,4)`, `(3,4)`, `(4,7)`. To avoid overcount confusion, note that row 1 contributes `(1,3),(1,4)`; row 2 contributes `(3,4),(3,7)` only if `7-3<=3`, which it is not. So `r` for `l=1` is actually `3`, not `4`. Final total: `4`.

## ⏱ Complexity Analysis
### Time Complexity
`O(n log n)` overall. Sorting dominates; the two-pointer scan is `O(n)` because each pointer advances monotonically and never retreats. At `10^6` elements, this remains practical in optimized environments. At `10^9`, even sorting is generally outside single-node memory and latency budgets, so the bottleneck becomes system architecture, not the algorithm.

### Space Complexity
`O(1)` auxiliary space beyond the sort implementation if the language runtime supports in-place sorting; otherwise typically `O(log n)` due to recursion or sort stack frames. No additional hash tables or buffers are required. Reducing below the sort overhead is not meaningful without changing the sorting primitive.

## 💡 Key Takeaways
- If a pair condition depends only on value difference and not original order, sorting is usually the first profitable transformation.
- When a valid range boundary only moves in one direction as you scan, that is a strong signal for a two-pointers or sliding-window solution.
- Use `r - l - 1`, not `r - l`, because the left element cannot pair with itself.
- Do not reset `r` for each `l`; doing so preserves correctness but degrades the scan back toward quadratic behavior.
- In production code, converting pairwise comparison problems into monotonic range-counting is a recurring way to trade brute-force CPU for predictable throughput.

## 🚀 Variations & Further Practice
- Count pairs whose difference lies in a range `[low, high]`. The twist is combining two window counts or two binary-search boundaries per index.
- Return the actual compatible pairs instead of just the count. The twist is output sensitivity: runtime becomes proportional to result size, which can be `O(n^2)`.
- Support online inserts and pair-count queries. The twist is that sorting once no longer works; you need balanced trees, Fenwick trees with coordinate compression, or order-statistics structures.