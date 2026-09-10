# Maximum Score from One Interior Buffer Removal

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Arrays &nbsp;|&nbsp; **Tags:** Arrays, Kadane's Algorithm, Prefix Sum

---

## 🗂 Problem Overview
Given an integer array, remove exactly one contiguous subarray that lies strictly inside the array, so the first and last elements must remain. After concatenating the remaining left and right parts, return the maximum possible sum. The key observation is that maximizing the final score is equivalent to minimizing the sum of the removed interior subarray. The non-trivial part is enforcing the interior-only constraint while still handling large arrays and negative values in linear time.

## 🌍 Engineering Impact
This pattern shows up anywhere a system must excise one bad contiguous region while preserving boundary integrity: streaming pipelines dropping a corrupt middle window, time-series analytics excluding one anomalous burst, ranking systems removing one low-quality segment from a feature stream, or media/data processing preserving required start/end markers. At scale, brute-force interval evaluation collapses under quadratic cost and cache-unfriendly scans. The linear approach matters because it turns a potentially explosive search over all interior ranges into a single pass, which is the difference between something deployable in hot-path processing and something that times out or forces pre-aggregation compromises.

## 🔍 Problem Statement
You are given an integer array `nums` of length `n`, where `3 <= n <= 200000` and each value is in `[-10^9, 10^9]`. You must remove exactly one contiguous subarray `nums[l..r]` such that `1 <= l <= r <= n - 2` using 0-based indexing. This means the removed segment must be strictly interior: `nums[0]` and `nums[n-1]` must remain.

Let `total = sum(nums)`. If the removed subarray has sum `removed`, the final score is `total - removed`. Therefore, the problem reduces to finding the minimum-sum contiguous subarray within indices `[1, n-2]`.

Examples:

- `nums = [5, -2, 3, -4, 6]` → output `12`
- `nums = [4, 7, 2, 9]` → output `20`

The constraint driving the solution is `n = 2e5`: enumerating all interior subarrays is `O(n^2)` and not viable.

## 🪜 How to Solve This
1. Start from the scoring formula → final score is `total sum - removed sum`.
2. Since `total sum` is fixed, maximizing the result means minimizing what you remove.
3. The removed segment must be contiguous and strictly interior → we are not looking for any subset, only a subarray inside `nums[1..n-2]`.
4. That immediately suggests a subarray-optimization pattern. Normally Kadane’s algorithm finds a maximum-sum subarray, but the same idea works for minimum-sum subarrays by flipping the recurrence.
5. Restrict the scan to indices `1` through `n-2`. This is the only boundary condition that matters; once the scan is confined there, every candidate is valid.
6. Track the minimum subarray sum ending at the current index, and the best minimum seen globally.
7. Subtract that minimum interior sum from the total array sum.

The key mental move is reframing: this is not “remove something” but “find the worst interior contiguous contribution.” Once seen that way, it becomes a constrained Kadane scan.

## 🧩 Algorithm Walkthrough
1. **Compute the total array sum.**  
   This gives the baseline score before removal. Every valid answer is `total - removedSum`, so we only need the minimum valid `removedSum`.

2. **Restrict the search space to the interior window `[1, n-2]`.**  
   This enforces the problem’s structural invariant: the first and last elements cannot be removed. No candidate outside this window is legal.

3. **Apply Kadane’s Algorithm in its minimum-subarray form.**  
   Let `currentMin` be the minimum subarray sum ending at index `i`, and `bestMin` be the minimum subarray sum seen anywhere so far in the interior range.  
   Recurrence:  
   `currentMin = min(nums[i], currentMin + nums[i])`  
   `bestMin = min(bestMin, currentMin)`

4. **Why this recurrence is correct.**  
   Any minimum-sum subarray ending at `i` either:
   - starts fresh at `i`, or
   - extends the minimum-sum subarray ending at `i-1`.  
   There is no third option for a contiguous segment ending at `i`.

5. **Maintain the invariant during the scan.**  
   After processing index `i`, `currentMin` is the minimum sum of any interior subarray ending exactly at `i`, and `bestMin` is the minimum sum of any valid interior subarray within `[1..i]`.

6. **Return `total - bestMin`.**  
   Since removing the smallest-sum valid interior subarray maximizes what remains, this is optimal.

This is a classic **Kadane / dynamic programming on contiguous ranges** problem, with a boundary restriction layered on top.

## 📊 Worked Example
Use `nums = [5, -2, 3, -4, 6]`.

- Total sum = `8`
- Interior range = indices `1..3` → values `[-2, 3, -4]`

| i | nums[i] | currentMin = min(nums[i], currentMin + nums[i]) | bestMin |
|---|---------|--------------------------------------------------|---------|
| 1 | -2      | -2                                               | -2      |
| 2 | 3       | min(3, -2 + 3) = 1                               | -2      |
| 3 | -4      | min(-4, 1 + -4) = -4                             | -4      |

Trace interpretation:

1. At index `1`, the best subarray ending there is `[-2]`.
2. At index `2`, extending gives sum `1`, which is still better than starting fresh at `3`, so `currentMin = 1`.
3. At index `3`, starting fresh with `[-4]` beats extending to `-3`, so the minimum interior subarray is `[-4]`.

Final answer = `total - bestMin = 8 - (-4) = 12`.

## ⏱ Complexity Analysis
### Time Complexity
`O(n)`. One pass computes the total sum, and one pass over the interior range computes the minimum subarray sum; these can also be fused into a single pass. The dominant operation is a constant-time update per element. At `10^6` elements this is routine; at `10^9`, linear work is still expensive but remains the only asymptotically viable exact approach.

### Space Complexity
`O(1)`. The algorithm stores only running aggregates: total sum, current minimum ending here, and global minimum. No auxiliary arrays or prefix tables are required. You could derive the same result with prefix sums, but that would not improve asymptotic space or clarity here.

## 💡 Key Takeaways
- If the objective is “maximize remaining sum after removing a contiguous block,” rewrite it as “minimize the removed block’s sum.”
- When the problem asks for an optimal contiguous segment under linear-time expectations, Kadane’s algorithm should be one of the first patterns you test.
- The valid search range is `1..n-2`, not the whole array; including index `0` or `n-1` silently violates the problem.
- Initialize from the first interior element, not from `0`, or all-positive interiors can produce an illegal empty-removal interpretation.
- In production systems, constrained local optimization often reduces to running a known primitive over a filtered window rather than inventing a new global algorithm.

## 🚀 Variations & Further Practice
- **Remove up to `k` interior subarrays**: the twist is combining multiple disjoint minimum-sum segments, which pushes the problem toward DP over intervals or stateful Kadane variants.
- **Remove one subarray, but preserve at least `m` elements on each side**: same core idea, but the legal interval set becomes length- and boundary-constrained.
- **Circular array version**: the interior notion changes under wraparound, and minimum-subarray logic must be reconciled with circular segment handling.