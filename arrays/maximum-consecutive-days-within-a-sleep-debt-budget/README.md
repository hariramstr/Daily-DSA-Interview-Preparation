# Maximum Consecutive Days Within a Sleep Debt Budget

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Arrays &nbsp;|&nbsp; **Tags:** Arrays, Sliding Window, Two Pointers

---

## 🗂 Problem Overview
Given an array `sleepHours`, a daily sleep target, and a total debt budget, find the longest contiguous range of days whose accumulated sleep debt does not exceed the budget. Each day contributes `max(0, target - sleepHours[i])` debt, so oversleeping never offsets prior shortages. The challenge is not computing debt, but finding the maximum-length subarray under a sum constraint efficiently for up to `10^5` days, which rules out quadratic scanning.

## 🌍 Engineering Impact
This is the same shape as production problems that ask for the longest contiguous interval under a bounded cost: API request bursts within rate-limit debt, streaming windows under error budgets, ad-serving sessions under spend caps, and log ingestion segments under backpressure tolerance. At scale, brute-force interval evaluation collapses under quadratic behavior and destroys latency predictability. The sliding-window formulation enables single-pass processing, constant auxiliary memory, and straightforward online execution. That matters in systems where data arrives continuously and decisions must be made incrementally without materializing or rescanning historical windows.

## 🔍 Problem Statement
You are given:

- `sleepHours`, where `sleepHours[i]` is the number of hours slept on day `i`
- `target`, the recommended daily sleep
- `budget`, the maximum total sleep debt allowed for a contiguous block

For each day, debt is:

```text
max(0, target - sleepHours[i])
```

Return the maximum length of a contiguous subarray whose total debt is at most `budget`.

Constraints:

- `1 <= sleepHours.length <= 100000`
- `0 <= sleepHours[i] <= 24`
- `1 <= target <= 24`
- `0 <= budget <= 1000000000`

Examples:

```text
Input:  sleepHours = [7, 5, 8, 4, 6, 7], target = 7, budget = 3
Output: 3
```

```text
Input:  sleepHours = [6, 6, 7, 7, 5, 8, 6], target = 7, budget = 2
Output: 4
```

The key constraint is `n = 10^5`: you need an `O(n)` or near-linear solution, not nested subarray enumeration.

## 🪜 How to Solve This
1. Read the definition of debt → notice each day contributes a non-negative value: `0` if sleep meets target, otherwise the shortfall.

2. Rewrite the problem mentally: this is no longer about sleep hours, but about the longest contiguous subarray whose sum is `<= budget`.

3. Once every element is non-negative, a strong signal appears: **sliding window / two pointers**. Why? Because expanding the right boundary can only keep or increase the window sum, never decrease it.

4. Start with a window `[left..right]` and maintain its current debt sum.  
   - Extend `right` one day at a time.  
   - If the sum exceeds `budget`, shrink from the left until the window becomes valid again.

5. At every step, the current window is the longest valid window ending at `right`, so update the best length.

6. No prefix sums with binary search are needed here because non-negativity gives a simpler invariant: once a window is too expensive, only moving `left` can fix it.

That chain of reasoning gets you from problem statement to an `O(n)` pass almost immediately.

## 🧩 Algorithm Walkthrough
1. **Transform each day into debt on the fly.**  
   For day `right`, compute `debt = max(0, target - sleepHours[right])` and add it to `currentDebt`.  
   This is correct because the problem’s scoring function is additive across the window.

2. **Use the Sliding Window / Two Pointers pattern.**  
   Maintain a window `[left, right]` whose total debt is tracked by `currentDebt`.  
   This abstraction fits because all per-day debts are non-negative, so window expansion is monotonic with respect to total cost.

3. **Expand the window by moving `right` forward.**  
   Each iteration considers one new endpoint.  
   The invariant before shrinking: `currentDebt` equals the total debt of the current window.

4. **Shrink while the budget is violated.**  
   While `currentDebt > budget`, remove the debt contribution of `sleepHours[left]`, then increment `left`.  
   This is necessary and sufficient: removing from the left is the only way to restore validity for a fixed `right`.

5. **Record the best valid length.**  
   After shrinking, the window is valid again, so update:  
   `best = max(best, right - left + 1)`  
   This works because the maintained window is the longest valid window ending at `right`; any earlier `left` would have exceeded budget.

6. **Return `best`.**  
   Each index enters and leaves the window at most once, which gives linear runtime and stable behavior under large input sizes.

## 📊 Worked Example
Example: `sleepHours = [6, 6, 7, 7, 5, 8, 6]`, `target = 7`, `budget = 2`

Debt per day: `[1, 1, 0, 0, 2, 0, 1]`

| right | sleepHours[right] | debt added | currentDebt after add | action if > budget | left | valid window | best |
|------:|-------------------:|-----------:|----------------------:|-------------------|-----:|--------------|-----:|
| 0 | 6 | 1 | 1 | none | 0 | `[0..0]` | 1 |
| 1 | 6 | 1 | 2 | none | 0 | `[0..1]` | 2 |
| 2 | 7 | 0 | 2 | none | 0 | `[0..2]` | 3 |
| 3 | 7 | 0 | 2 | none | 0 | `[0..3]` | 4 |
| 4 | 5 | 2 | 4 | shrink twice | 2 | `[2..4]` | 4 |
| 5 | 8 | 0 | 2 | none | 2 | `[2..5]` | 4 |
| 6 | 6 | 1 | 3 | shrink once | 3 | `[3..6]` | 4 |

Answer: `4`.

## ⏱ Complexity Analysis
### Time Complexity
`O(n)`. Each day is processed once when `right` expands the window, and each day is removed at most once when `left` advances. There is no nested rescanning. At `10^6` elements this remains practical; at `10^9`, linear work is still expensive but asymptotically optimal for exact single-pass evaluation.

### Space Complexity
`O(1)` auxiliary space. The algorithm stores only pointer indices, the running debt sum, and the best length. No extra array is required because daily debt can be computed inline. You could materialize the debt array for readability, but that increases space to `O(n)` without improving asymptotic time.

## 💡 Key Takeaways
- If the problem asks for the **longest contiguous segment with sum at most K** and each contribution is non-negative, sliding window should be your default candidate.
- A strong recognition signal is when the raw input can be transformed into a non-negative cost array, turning a domain-specific story into a standard subarray-budget problem.
- Do not let days with `sleepHours[i] > target` subtract debt; they contribute exactly `0`, not a negative value.
- Update the best length only **after** shrinking the window back into a valid state, or you will count invalid ranges.
- In production systems, this pattern matters because non-negative budget accounting enables exact online decisions with bounded memory and predictable linear throughput.

## 🚀 Variations & Further Practice
- Allow oversleep to create negative debt that offsets prior shortages. The sliding-window invariant breaks because costs are no longer non-negative; prefix sums plus ordered structures become necessary.
- Ask for the **number** of valid subarrays instead of the maximum length. Same window mechanics, but the counting logic changes from tracking a max to accumulating valid suffix counts.
- Support point updates to `sleepHours` and repeated longest-range queries. The challenge shifts from one-pass scanning to range-query data structures or offline processing.