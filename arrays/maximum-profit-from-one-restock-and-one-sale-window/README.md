# Maximum Profit from One Restock and One Sale Window

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Arrays &nbsp;|&nbsp; **Tags:** Arrays, Dynamic Programming, Prefix Sum

---

## 🗂 Problem Overview
Given an integer array `prices`, choose exactly one non-empty contiguous restock window and one non-empty contiguous sale window such that the sale window starts strictly after the restock window ends. Profit is the sum of the sale window minus the sum of the restock window. Return the maximum possible profit, including negative values when every valid plan loses money. The challenge is finding the best ordered pair of subarrays under a strict non-overlap constraint without brute-forcing all possibilities.

## 🌍 Engineering Impact
This pattern shows up anywhere a system must optimize two ordered phases over a time series: buy/sell intervals in trading analytics, ingest/serve windows in streaming pipelines, low-cost/high-yield scheduling in cloud capacity planning, and prefetch/evict decisions in storage systems. At scale, brute-force interval pairing collapses under quadratic growth and becomes unusable for online scoring or large batch jobs. The useful abstraction is not “find two subarrays,” but “precompute the best prefix decision and combine it with each suffix decision.” That shift enables linear scans, stable latency, and predictable memory behavior.

## 🔍 Problem Statement
You are given an array `prices` with `2 <= prices.length <= 200000` and `-100000 <= prices[i] <= 100000`. Select exactly one contiguous, non-empty restock window and exactly one contiguous, non-empty sale window. The sale window must start after the restock window ends, so the two windows cannot overlap and must preserve order.

Profit is:

`sum(sale window) - sum(restock window)`

Return the maximum possible profit. If every valid choice is negative, return the least negative result. The answer fits in a signed 64-bit integer, which matters because subarray sums can exceed 32-bit range.

Examples:

- `prices = [4, -2, 3, -1, 5]` → `9`
- `prices = [7, 6, 5, 4]` → `-1`

The key constraint is the input size: `O(n^2)` enumeration of all subarray pairs is not viable, so the solution must be linear or near-linear.

## 🪜 How to Solve This
1. Read the problem → this is not a single max-subarray problem. We need **two** contiguous windows with a strict left-to-right dependency.

2. Rewrite the objective → for any split point `i`, the restock window must lie entirely in `prices[0..i]`, and the sale window must lie entirely in `prices[i+1..n-1]`.

3. That suggests precomputation → if we knew:
   - the minimum subarray sum ending anywhere in each prefix, and
   - the maximum subarray sum starting anywhere in each suffix,
   then every valid split becomes easy to evaluate.

4. Why minimum on the left? Because restocking is a cost we subtract. Minimizing the left subarray sum maximizes profit.

5. Why maximum on the right? Because selling is revenue we add. We want the best possible suffix-side gain.

6. So the plan is:
   - scan left to right with a Kadane-style DP for minimum subarray sums,
   - scan right to left with a symmetric Kadane-style DP for maximum subarray sums,
   - combine them across every split.

7. This works because each split reduces the global problem into two independent optimal subproblems, one on each side of the boundary.

## 🧩 Algorithm Walkthrough
1. **Use Dynamic Programming with Kadane-style prefix/suffix aggregation.**  
   This is the right abstraction because the problem asks for optimal contiguous windows, and Kadane’s algorithm is the standard linear-time primitive for optimal subarray sums.

2. **Build `leftMin[i]`: the minimum subarray sum anywhere in `prices[0..i]`.**  
   Maintain `currMinEnd`, the minimum subarray sum that must end at index `i`.  
   Transition: `currMinEnd = min(prices[i], currMinEnd + prices[i])`.  
   Then store `leftMin[i] = min(leftMin[i-1], currMinEnd)`.  
   Invariant: after processing `i`, `leftMin[i]` is the cheapest valid restock window fully contained in the prefix.

3. **Build `rightMax[i]`: the maximum subarray sum anywhere in `prices[i..n-1]`.**  
   Scan from right to left. Maintain `currMaxStart`, the maximum subarray sum that must start at index `i`.  
   Transition: `currMaxStart = max(prices[i], currMaxStart + prices[i])`.  
   Then store `rightMax[i] = max(rightMax[i+1], currMaxStart)`.  
   Invariant: after processing `i`, `rightMax[i]` is the best valid sale window fully contained in the suffix.

4. **Evaluate every split `i` between `i` and `i+1`.**  
   Profit for that split is `rightMax[i+1] - leftMin[i]`.  
   This is correct because the left and right windows are guaranteed non-overlapping and non-empty.

5. **Return the maximum over all splits.**  
   Since every legal solution has exactly one boundary between the two windows, enumerating all `n-1` boundaries covers the full search space.

## 📊 Worked Example
Take `prices = [4, -2, 3, -1, 5]`.

| i | price | leftMin[i] | rightMax[i] |
|---|------:|-----------:|------------:|
| 0 | 4     | 4          | 9           |
| 1 | -2    | -2         | 7           |
| 2 | 3     | -2         | 7           |
| 3 | -1    | -2         | 5           |
| 4 | 5     | -2         | 5           |

Build `leftMin` left-to-right:
- best restock in prefix `[4]` is `4`
- in `[4, -2]`, cheapest window becomes `[-2]`, so `-2`
- later prefixes never beat `-2`

Build `rightMax` right-to-left:
- from index `4`, best sale is `[5] = 5`
- from index `2`, best sale is `[3, -1, 5] = 7`
- from index `0`, best suffix-side sale is `9`, but that cannot pair with a left window before index `0`

Now test splits:
- split after `0`: `7 - 4 = 3`
- split after `1`: `7 - (-2) = 9`
- split after `2`: `5 - (-2) = 7`
- split after `3`: `5 - (-2) = 7`

Answer: `9`.

## ⏱ Complexity Analysis
### Time Complexity
`O(n)`. There are three linear passes: one to compute prefix minimum subarray sums, one to compute suffix maximum subarray sums, and one to evaluate all split points. At `10^6` elements this is practical in memory-resident workloads; at `10^9`, even linear scans become infrastructure-bound and require partitioned or streaming execution strategies.

### Space Complexity
`O(n)` for the `leftMin` and `rightMax` arrays. Those structures own the extra memory. It can be reduced to `O(n)` with only one auxiliary array plus scalar state, but not to true `O(1)` without sacrificing the ability to combine arbitrary prefix and suffix optima cleanly in one pass.

## 💡 Key Takeaways
- If a problem asks for two ordered, non-overlapping contiguous windows, look for a split-point formulation rather than direct interval pairing.
- When the objective is “best right side minus best left side,” think prefix-optimal + suffix-optimal precomputation.
- The split is between `i` and `i+1`; using `rightMax[i] - leftMin[i]` accidentally allows overlap at `i`.
- Use 64-bit arithmetic for running sums and answers; subarray totals can exceed 32-bit limits even when individual values do not.
- The transferable design insight is to decompose constrained global optimization into independent prefix/suffix summaries that can be merged in linear time.

## 🚀 Variations & Further Practice
- Allow up to `k` restock/sale phase pairs in order. The harder twist is managing multiple non-overlapping interval decisions with stateful DP instead of a single split.
- Require a minimum or maximum window length for either phase. The twist is that plain Kadane no longer suffices; you need prefix sums plus constrained range minima/maxima.
- Generalize to circular arrays or streaming input with rolling retention. The twist is preserving ordered non-overlap when the data is not a simple static linear sequence.