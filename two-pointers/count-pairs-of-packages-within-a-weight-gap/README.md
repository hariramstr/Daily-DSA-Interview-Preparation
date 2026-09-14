# Count Pairs of Packages Within a Weight Gap

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Two Pointers &nbsp;|&nbsp; **Tags:** Two Pointers, Sorting, Array

---

## 🗂 Problem Overview
Given an unsorted array of package weights and an inclusive gap range `[lowGap, highGap]`, count how many index pairs `(i, j)` with `i < j` satisfy `lowGap <= |weights[i] - weights[j]| <= highGap`. The output is a single integer count. The non-trivial part is scale: with up to `10^5` elements, the obvious all-pairs check is `O(n^2)`, which is too slow, so the solution must exploit ordering and monotonicity.

## 🌍 Engineering Impact
This pattern shows up anywhere systems need to count pairwise relationships under bounded distance constraints: similarity windows in search ranking, latency-gap analysis in distributed tracing, fraud detection over transaction amounts, and telemetry pipelines computing bounded deltas across large event sets. At production scale, naive pair enumeration explodes quadratically and becomes both CPU- and cache-hostile. Sorting plus two-pointer counting converts pairwise comparison into a monotonic scan, which is the difference between a batch job that misses its SLA and one that can run inline or near-real-time on six-figure input sizes.

## 🔍 Problem Statement
You are given:

- `weights`: an integer array where `weights[i]` is the weight of the `i`th package
- `lowGap` and `highGap`: inclusive bounds on allowed absolute weight difference

A pair `(i, j)` is compatible if:

- `i < j`
- `lowGap <= |weights[i] - weights[j]| <= highGap`

Return the total number of compatible pairs.

Constraints:

- `1 <= weights.length <= 100000`
- `0 <= weights[i] <= 1000000000`
- `0 <= lowGap <= highGap <= 1000000000`

Examples:

- `weights = [4, 1, 7, 3], lowGap = 2, highGap = 4` → `4`
- `weights = [5, 5, 8, 10], lowGap = 0, highGap = 3` → `5`

Important edge cases:

- Duplicate weights are allowed
- `lowGap = 0` means equal-weight pairs may be valid
- The array is unsorted, so any efficient solution must first create order
- The `10^5` bound rules out quadratic enumeration

## 🪜 How to Solve This
1. Start with the brute-force interpretation: every pair contributes if its absolute difference is inside a range. That is immediately `O(n^2)`, so the real question is how to count many pairs at once.

2. Notice that absolute difference becomes much easier after sorting. Once `weights` is sorted, for any `i < j`, we have `weights[j] - weights[i] >= 0`, so the absolute value disappears.

3. Reframe the task: instead of counting pairs with difference in `[lowGap, highGap]` directly, count pairs with difference `<= highGap`, then subtract pairs with difference `< lowGap`.

4. That subproblem has strong monotonicity. In sorted order, if `weights[right] - weights[left] <= limit`, then every index between `left` and `right - 1` also works with `right`.

5. Monotonicity suggests two pointers. Sweep `right` forward once, advance `left` only when the gap exceeds the current limit, and accumulate how many valid starts remain.

6. Run that counting routine twice with different limits and subtract. Same sorted array, same pointer logic, linear scan after sort.

## 🧩 Algorithm Walkthrough
1. **Sort the array.**  
   This is the enabling transformation. After sorting, pair differences for `i < j` become `weights[j] - weights[i]`, eliminating the need for absolute value handling. The invariant is ordered weights, which makes the valid window for each endpoint contiguous.

2. **Define a helper `countAtMost(limit)`.**  
   It returns the number of pairs whose difference is `<= limit`. This is the core counting primitive. If `limit < 0`, return `0`, since no non-negative difference can satisfy it.

3. **Use the Two Pointers pattern inside `countAtMost`.**  
   Maintain `left` and iterate `right` from `0` to `n - 1`. While `weights[right] - weights[left] > limit`, increment `left`. This preserves the invariant that all indices in `[left, right - 1]` form valid pairs with `right`, and all indices before `left` do not.

4. **Accumulate counts per `right`.**  
   Once the window is valid, add `right - left` to the answer. That counts every pair `(k, right)` for `left <= k < right` exactly once.

5. **Convert range counting into subtraction.**  
   The desired answer is:  
   `countAtMost(highGap) - countAtMost(lowGap - 1)`  
   This works because inclusive range counting is equivalent to prefix-count subtraction.

6. **Why this is correct.**  
   Sorting creates a monotone difference function. Two pointers exploit that monotonicity so each pointer moves only forward. Every valid pair is counted once when its larger index is the current `right`, and no invalid pair survives the `left` adjustment.

## 📊 Worked Example
Example: `weights = [4, 1, 7, 3], lowGap = 2, highGap = 4`

Sorted: `[1, 3, 4, 7]`

We compute `countAtMost(4)` and `countAtMost(1)`.

| right | value | left after shrink (`<=4`) | add | running total |
|---|---:|---:|---:|---:|
| 0 | 1 | 0 | 0 | 0 |
| 1 | 3 | 0 | 1 | 1 |
| 2 | 4 | 0 | 2 | 3 |
| 3 | 7 | 1 | 2 | 5 |

So `countAtMost(4) = 5`.

Now `countAtMost(1)`:

| right | value | left after shrink (`<=1`) | add | running total |
|---|---:|---:|---:|---:|
| 0 | 1 | 0 | 0 | 0 |
| 1 | 3 | 1 | 0 | 0 |
| 2 | 4 | 1 | 1 | 1 |
| 3 | 7 | 3 | 0 | 1 |

So `countAtMost(1) = 1`.

Final answer: `5 - 1 = 4`.

## ⏱ Complexity Analysis
### Time Complexity
Sorting costs `O(n log n)`. Each `countAtMost` pass is `O(n)` because both pointers move forward at most `n` times, so the total remains `O(n log n)`. At `10^6`, this is still practical; at `10^9`, even linear scans and sorting become infeasible without distribution or approximation.

### Space Complexity
Space is `O(1)` auxiliary if sorting in place is allowed, excluding the sort implementation’s internal stack or runtime-specific overhead. If the language’s sort allocates buffers, practical space may be `O(log n)` or higher. Reducing that further usually means trading simplicity for custom in-place sorting.

## 💡 Key Takeaways
- If the condition is based on pairwise numeric distance and the input is unsorted, sorting is often the first move to expose monotonic structure.
- If you need counts within an inclusive range, think “prefix counts then subtract” instead of trying to count the interval directly.
- Be careful with `lowGap = 0`: the lower prefix becomes `countAtMost(-1)`, which must return `0`, not underflow into incorrect logic.
- Use a 64-bit accumulator for the answer; the number of valid pairs can be about `n(n-1)/2`, which exceeds 32-bit range at `10^5`.
- The transferable design insight is to replace explicit pair enumeration with ordered window counting whenever a global sort can turn a symmetric relation into a one-sided monotone scan.

## 🚀 Variations & Further Practice
- Count pairs whose **sum** lies in `[L, R]` instead of difference bounds; same sort-plus-count idea, but the pointer movement is driven by additive thresholds rather than gaps.
- Count valid pairs in a **streaming** setting with insertions over time; the harder twist is that full sorting is no longer free, so you need balanced trees, Fenwick trees, or bucketed approximations.
- Return the **actual pairs** or support repeated queries with different gap ranges; the twist is shifting from one-shot counting to output-sensitive or preprocessed query-efficient designs.