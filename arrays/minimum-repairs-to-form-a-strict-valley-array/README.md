# Minimum Repairs to Form a Strict Valley Array

**Difficulty:** Hard &nbsp;|&nbsp; **Topic:** Arrays &nbsp;|&nbsp; **Tags:** Arrays, Dynamic Programming, Binary Search

---

## 🗂 Problem Overview
Given an array `nums`, change the fewest elements so the final array becomes a **strict valley**: strictly decreasing to some interior index `p`, then strictly increasing after `p`. Because modified values can be set arbitrarily, the real objective is to **keep as many original indices unchanged as possible** while preserving valley order constraints. With `n` up to `200000`, brute-force valley testing or quadratic subsequence DP is infeasible; the solution must exploit monotonic subsequence structure in `O(n log n)`.

## 🌍 Engineering Impact
This pattern shows up whenever you want to preserve the maximum amount of original data while enforcing a global shape constraint. Examples include search-ranking re-scoring with monotonic business rules, telemetry smoothing in streaming pipelines, compiler optimization passes that retain stable instruction ordering, and time-series repair in observability systems. At scale, the wrong approach degenerates into pairwise compatibility checks or full reconstruction, which is too expensive and too destructive. The right abstraction—maximize a valid subsequence, repair the rest—separates immutable signal from adjustable noise and yields predictable `O(n log n)` behavior under large input volumes.

## 🔍 Problem Statement
You are given an integer array `nums` of length `n`, where `3 <= n <= 200000` and `-10^9 <= nums[i] <= 10^9`. A strict valley array has some index `p` with `0 < p < n - 1` such that:

- `nums[0] > nums[1] > ... > nums[p]`
- `nums[p] < nums[p+1] < ... < nums[n-1]`

In one repair, any single element may be changed to any integer. Return the minimum number of repairs needed to transform `nums` into some strict valley array.

Equivalent view: keep the largest set of original positions that already fit a valley-shaped subsequence; repair all others.

Examples:

- `nums = [9, 7, 5, 6, 8]` → `0`
- `nums = [4, 4, 3, 2, 5, 5]` → `2`

The key constraint is `n = 200000`, which rules out `O(n^2)` dynamic programming and forces an `O(n log n)` subsequence-based solution.

## 🪜 How to Solve This
1. Read the operation carefully → changing a value is unconstrained. That means modified elements do not impose structure; only **unchanged** elements matter.

2. Reframe the goal → minimize repairs = maximize the number of indices we can keep unchanged.

3. Ask what “kept unchanged” must satisfy → around some valley position `p`, kept elements on the left must form a **strictly decreasing subsequence ending at `p`**, and kept elements on the right must form a **strictly increasing subsequence starting at `p`**.

4. Convert the left side into a standard pattern → strict decreasing in original values is the same as strict increasing on negated values, or LIS on a reversed ordering.

5. Compute two arrays:
   - `left[i]`: longest strict decreasing subsequence ending at `i`
   - `right[i]`: longest strict increasing subsequence starting at `i`

6. For each possible valley `i`, combine them as `left[i] + right[i] - 1`. The `-1` avoids counting the valley twice.

7. The best valid valley must have at least one element on each side, so require `left[i] > 1` and `right[i] > 1`.

8. Maximum keepable indices gives minimum repairs: `n - bestValleyLength`.

## 🧩 Algorithm Walkthrough
1. **Model the problem as subsequence preservation.**  
   We do not need the final repaired values, only how many original positions can survive unchanged. This turns the problem into a longest valid valley-shaped subsequence problem.

2. **Compute `left[i]` using the LIS pattern with Binary Search.**  
   `left[i]` is the length of the longest **strictly decreasing** subsequence ending at `i`. A standard trick is to run LIS on `-nums[i]`, maintaining the minimal tail value for each subsequence length.  
   **Invariant:** after processing prefix `0..i`, `tails[len-1]` is the smallest possible tail for an increasing subsequence of length `len` over negated values.

3. **Compute `right[i]` symmetrically from the right.**  
   We need the longest **strictly increasing** subsequence starting at `i`. Process the array from right to left, again using LIS tails, or equivalently run LIS on the reversed suffix.  
   **Invariant:** after processing suffix `i..n-1`, the structure encodes optimal increasing subsequences that can start within that suffix.

4. **Evaluate every index as the valley.**  
   For each `i`, a valid valley requires one decreasing step on the left and one increasing step on the right, so `left[i] >= 2` and `right[i] >= 2`. The keepable count is `left[i] + right[i] - 1`.

5. **Take the best valley and convert to repairs.**  
   Let `best = max(left[i] + right[i] - 1)` over valid valleys. Then the answer is `n - best`. This is correct because every non-kept index can always be repaired to any needed value without violating the preserved subsequence.

Pattern-wise, this is **Dynamic Programming + Binary Search LIS**, not local greedy repair. The global optimum depends on subsequence compatibility across the entire array.

## 📊 Worked Example
Consider `nums = [4, 4, 3, 2, 5, 5]`.

| i | nums[i] | `left[i]` = LDS ending at i | `right[i]` = LIS starting at i | valid valley? | combined |
|---|---------|-----------------------------|--------------------------------|---------------|----------|
| 0 | 4       | 1                           | 3                              | no            | —        |
| 1 | 4       | 1                           | 3                              | no            | —        |
| 2 | 3       | 2                           | 2                              | yes           | 3        |
| 3 | 2       | 3                           | 2                              | yes           | 4        |
| 4 | 5       | 1                           | 1                              | no            | —        |
| 5 | 5       | 1                           | 1                              | no            | —        |

Best valley is at `i = 3` with length `4`, keeping a subsequence like `[4, 3, 2, 5]`.  
So minimum repairs = `n - best = 6 - 4 = 2`.

One repaired array is `[6, 4, 3, 2, 5, 7]`, but construction is unnecessary; only the count matters.

## ⏱ Complexity Analysis
### Time Complexity
`O(n log n)`. We perform two LIS-style passes, each doing one binary search per element over a tails array of size at most `n`, then one linear scan to combine results. At `10^6` elements this remains practical; at `10^9`, even linear memory and scan costs become the real bottleneck.

### Space Complexity
`O(n)` for the `left` and `right` DP arrays, plus `O(n)` worst-case tails storage across passes. You can reduce some auxiliary overhead by reusing buffers, but not the need to retain per-index left/right lengths if you want a single final combine pass.

## 💡 Key Takeaways
- If edits are unconstrained and cost per element is uniform, first ask whether the real problem is “maximize unchanged positions,” not “simulate repairs.”
- “Strictly decreasing on one side, strictly increasing on the other” is a strong signal for combining two monotonic subsequence DPs around a pivot.
- Enforce **strict** inequalities with the correct binary-search variant; using non-strict LIS logic will overcount duplicates and produce invalid valleys.
- The valley must be interior and must have both sides present: reject indices where `left[i] == 1` or `right[i] == 1`.
- At scale, preserving the largest compatible subsequence is often the right architecture: isolate immutable signal, rewrite only the minimum inconsistent surface.

## 🚀 Variations & Further Practice
- **Minimum repairs to form a strict mountain array**: same bidirectional subsequence idea, but increasing then decreasing; the twist is flipping which monotonic DP is computed on each side.
- **Weighted repairs instead of unit-cost repairs**: each index has a modification cost, so maximize preserved total weight rather than count; this pushes the problem toward weighted LIS / Fenwick-tree DP with coordinate compression.
- **Count the number of optimal valley configurations**: after finding the maximum keepable length, count how many distinct optimal valleys or subsequences exist; the harder twist is combining length DP with multiplicity under duplicate values.