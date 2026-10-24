# Longest Toll Segment Within Daily Pass Limit

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Sliding Window &nbsp;|&nbsp; **Tags:** Sliding Window, Two Pointers, Heap

---

## 🗂 Problem Overview
Given an array `tolls`, a spending `budget`, and a pass that can waive at most `k` tolls inside any chosen contiguous interval, return the maximum interval length whose paid total stays within budget after optimally waiving up to `k` segments. The challenge is not checking one window, but doing it efficiently across up to `200,000` elements. A brute-force scan of all subarrays is infeasible; the solution must maintain window cost incrementally.

## 🌍 Engineering Impact
This pattern shows up in streaming cost control, ad-serving budget enforcement, query planning, and network-path optimization: maximize a continuous run while allowing a bounded number of “free” or discounted outliers. In production, the hard part is not the local decision but maintaining the best feasible interval under dynamic constraints without rescanning history. Without a sliding-window formulation plus an efficient way to track the top-`k` removable costs, systems degrade into quadratic behavior or expensive recomputation. With it, you get predictable throughput, online processing, and a design that scales to high-cardinality event streams.

## 🔍 Problem Statement
You are given:

- `tolls[i]`: toll cost for highway segment `i`
- `budget`: maximum amount the driver may actually pay
- `k`: maximum number of tolls that can be fully waived inside one contiguous trip segment

For any subarray `tolls[l..r]`, the driver may choose up to `k` elements in that subarray and treat them as zero. The remaining tolls must sum to at most `budget`. Return the largest valid subarray length.

Constraints:

- `1 <= tolls.length <= 200000`
- `0 <= tolls[i] <= 1000000000`
- `0 <= budget <= 100000000000000`
- `0 <= k <= tolls.length`

Examples:

- `tolls = [7, 2, 5, 10, 3], budget = 10, k = 1` → `3`
- `tolls = [4, 8, 1, 9, 2, 6], budget = 11, k = 2` → `5`

The key constraint is input size: any `O(n^2)` enumeration of subarrays is dead on arrival.

## 🪜 How to Solve This
1. Read the objective → we need the **longest contiguous** interval, which is a strong signal for **sliding window / two pointers**.

2. Rewrite the condition. For a window, if total sum is `windowSum` and we can waive up to `k` tolls, we should obviously waive the `k` largest values. So the paid amount is:

   `windowSum - sumOfKLargestInWindow`

3. That means the real problem is not “which subarray is best,” but “can I maintain, for the current window, the sum of its top `k` elements efficiently?”

4. Expand the right pointer one segment at a time. If the paid amount exceeds `budget`, shrink from the left until the window becomes valid again.

5. To support inserts/removals while tracking the top `k`, split window elements into two groups:
   - waived set: the current `k` largest elements
   - paid set: everything else

6. Maintain these groups with heaps plus lazy deletion. Rebalance after every add/remove so the waived set always contains the largest eligible elements.

7. Once validity can be checked in near-constant amortized time per move, the whole scan becomes `O(n log n)`.

## 🧩 Algorithm Walkthrough
1. **Use the Two Pointers / Sliding Window pattern.**  
   Let `left` and `right` define the current contiguous interval. We only move each pointer forward, which guarantees linear window movement. This is the right abstraction because the target is the maximum valid contiguous range.

2. **Track total window sum.**  
   Maintain `windowSum` as elements enter and leave. This gives the raw cost before any waivers.

3. **Maintain the waived set as the top `k` values in the window.**  
   Keep two dynamic partitions:
   - `large`: elements currently waived
   - `small`: elements currently paid  
   Also maintain `largeSum`, the sum of values in `large`. The actual paid amount is `windowSum - largeSum`.

4. **Support dynamic updates with heaps and lazy deletion.**  
   Since elements both enter and leave the window, direct heap deletion is awkward. Use delayed-removal maps keyed by `(value, index)` or equivalent identity. This preserves `O(log n)` updates without needing balanced BSTs.

5. **Rebalance after every mutation.**  
   Invariant:
   - `large` contains at most `k` valid elements
   - every valid element in `large` is `>=` every valid element in `small`  
   If `large` has too few elements, promote the largest from `small`. If ordering is violated, swap boundary elements.

6. **Validate and shrink.**  
   After adding `tolls[right]`, while `windowSum - largeSum > budget`, remove `tolls[left]`, advance `left`, and rebalance. This is correct because any super-window of an invalid window is also invalid unless we shrink.

7. **Record the maximum length.**  
   Whenever the window is valid, update `best = max(best, right - left + 1)`. Because all valid windows ending at `right` are considered through left-shrinking, no candidate is missed.

## 📊 Worked Example
Example: `tolls = [4, 8, 1, 9, 2, 6]`, `budget = 11`, `k = 2`

| Step | Add | Window        | `windowSum` | Waived top-2 | `largeSum` | Paid | Valid |
|------|-----|---------------|-------------|--------------|------------|------|-------|
| 1    | 4   | `[4]`         | 4           | `[4]`        | 4          | 0    | Yes   |
| 2    | 8   | `[4,8]`       | 12          | `[8,4]`      | 12         | 0    | Yes   |
| 3    | 1   | `[4,8,1]`     | 13          | `[8,4]`      | 12         | 1    | Yes   |
| 4    | 9   | `[4,8,1,9]`   | 22          | `[9,8]`      | 17         | 5    | Yes   |
| 5    | 2   | `[4,8,1,9,2]` | 24          | `[9,8]`      | 17         | 7    | Yes   |
| 6    | 6   | `[4,8,1,9,2,6]` | 30        | `[9,8]`      | 17         | 13   | No    |

Now shrink from the left:

- Remove `4` → window `[8,1,9,2,6]`
- `windowSum = 26`, waived still `[9,8]`, paid `= 26 - 17 = 9`

Valid again, length `5`, which is the maximum.

## ⏱ Complexity Analysis

### Time Complexity
`O(n log n)`. Each segment enters the window once and leaves once. Every insert, lazy delete, promotion, or demotion across the two heaps costs `O(log n)`. At `10^6` elements this is still practical in optimized runtimes; at `10^9`, even `O(n log n)` becomes a systems problem rather than just an algorithm choice.

### Space Complexity
`O(n)` in the worst case from the heaps plus lazy-deletion bookkeeping for elements still awaiting cleanup. You can reduce constant factors with balanced trees or specialized indexed heaps, but not the asymptotic bound without giving up efficient arbitrary window deletions.

## 💡 Key Takeaways
- If the prompt asks for the **longest contiguous range** under a budget-like constraint, start with **sliding window / two pointers**.
- If validity depends on “ignore/discount the largest `k` items,” think in terms of maintaining a **top-`k` partition** inside the window.
- The paid amount is `windowSum - sum(top k)`, not “subtract any arbitrary `k`”; the optimal waived set is always the largest tolls.
- Rebalancing is the fragile part: after every add/remove, ensure the waived set has size at most `k` and really contains the largest valid elements.
- In production, this is the broader pattern of turning a global optimization over a stream into an online invariant maintained incrementally.

## 🚀 Variations & Further Practice
- Replace “waive up to `k` segments” with “waive segments whose total waived value is at most `W`” — now the top-`k` partition is no longer enough; the state constraint becomes knapsack-like.
- Ask for the number of valid subarrays instead of the maximum length — same sliding-window core, but counting logic changes and requires careful monotonicity reasoning.
- Make tolls mutable with online updates and range queries — the problem shifts from one-pass windowing to segment trees / order-statistics structures over dynamic intervals.