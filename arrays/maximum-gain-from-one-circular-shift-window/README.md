# Maximum Gain from One Circular Shift Window

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Arrays &nbsp;|&nbsp; **Tags:** Arrays, Kadane's Algorithm, Prefix Sum

---

## 🗂 Problem Overview
Given an integer array `gain`, return the maximum sum of any **non-empty contiguous subarray** when the array is treated as **circular**. That means a valid block may stay within the array bounds or wrap from the end back to the beginning. The challenge is distinguishing ordinary maximum subarrays from wrap-around ones without enumerating all windows. With up to `10^5` elements, any quadratic scan is too slow; the solution must run in linear time.

## 🌍 Engineering Impact
This pattern shows up anywhere metrics or events are sampled on repeating timelines: rotating log buffers, cyclic sensor streams, manufacturing shift analytics, token-bucket refill windows, and ring-buffer-backed telemetry pipelines. At scale, the wrong approach degenerates into repeated rescans or duplicated-array tricks that amplify memory traffic and latency. The right abstraction lets you evaluate best contiguous gain across a cycle in one pass, which matters when this logic sits inside anomaly detectors, capacity planners, or online scoring systems. It enables predictable `O(n)` behavior and avoids pathological performance on long periodic traces.

## 🔍 Problem Statement
You are given an integer array `gain` where `gain[i]` represents the net productivity change during hour `i`. Because the schedule repeats, the array is circular: after the last index, the next hour is index `0`.

Choose exactly one **non-empty** contiguous block of hours. The block may:

- lie entirely within the array, or
- wrap from the end of the array to the beginning.

Return the maximum possible sum among all such circular subarrays.

**Constraints:**
- `1 <= gain.length <= 100000`
- `-100000 <= gain[i] <= 100000`
- Result fits in a 32-bit signed integer

**Examples:**

```text
Input:  gain = [5, -3, 5]
Output: 10
Explanation: Wrap-around block [5] + [5] gives 10.
```

```text
Input:  gain = [-2, -3, -1]
Output: -1
Explanation: All values are negative, so the best non-empty block is [-1].
```

The key constraint is `n = 10^5`, which rules out checking all circular windows and pushes toward a linear-time dynamic programming approach.

## 🪜 How to Solve This
1. Start with the non-circular version → that is classic **maximum subarray sum**, solved by Kadane’s algorithm in one pass.

2. Then notice circularity changes only one thing: the best answer might wrap. A wrap-around subarray is equivalent to taking **the whole array minus one contiguous middle segment** that you exclude.

3. If you want to maximize the wrapped sum, you should exclude the **minimum-sum** contiguous subarray. So the wrapped candidate becomes:

   `totalSum - minSubarraySum`

4. That gives two candidates:
   - best standard subarray
   - best wrapped subarray

5. One edge case breaks the formula: if all numbers are negative, then the minimum subarray is the entire array, and `totalSum - minSubarraySum = 0`, which corresponds to choosing an empty subarray — forbidden by the problem.

6. So the final logic is:
   - compute max subarray sum with Kadane
   - compute min subarray sum with a mirrored Kadane
   - if `maxSum < 0`, return `maxSum`
   - otherwise return `max(maxSum, totalSum - minSum)`

That is the whole idea: reduce circularity to “best normal segment” vs “everything except worst middle segment.”

## 🧩 Algorithm Walkthrough
1. **Compute the total array sum.**  
   This is needed for the wrap-around case. If a circular subarray wraps, it includes a suffix and a prefix, which is equivalent to excluding one contiguous interior segment. The invariant is that `totalSum` remains fixed while we search for the best segment to exclude.

2. **Run Kadane’s Algorithm for the maximum subarray sum.**  
   Maintain `currentMax` = best sum ending at the current index, and `maxSum` = best seen globally.  
   Update rule: `currentMax = max(gain[i], currentMax + gain[i])`.  
   This is correct because any optimal subarray ending at `i` either starts fresh at `i` or extends the best subarray ending at `i-1`.

3. **Run a mirrored Kadane for the minimum subarray sum.**  
   Maintain `currentMin` and `minSum` with  
   `currentMin = min(gain[i], currentMin + gain[i])`.  
   This finds the contiguous block whose removal would maximize the wrapped result. The invariant is symmetric to standard Kadane, but for minima.

4. **Handle the all-negative case explicitly.**  
   If `maxSum < 0`, every element is negative, so the best non-empty subarray is the largest single element. Returning `totalSum - minSum` here would incorrectly produce `0`, representing an empty selection.

5. **Return the better of the two valid candidates.**  
   Final answer is `max(maxSum, totalSum - minSum)` when at least one non-negative value exists.  
   The pattern here is **Kadane’s Algorithm + complement reasoning via prefix-sum intuition**: maximize either a direct segment or the complement of the minimum segment.

## 📊 Worked Example
Take `gain = [5, -3, 5]`.

| i | gain[i] | currentMax | maxSum | currentMin | minSum | totalSum |
|---|---------|------------|--------|------------|--------|----------|
| 0 | 5       | 5          | 5      | 5          | 5      | 5        |
| 1 | -3      | 2          | 5      | -3         | -3     | 2        |
| 2 | 5       | 7          | 7      | 2          | -3     | 7        |

Now evaluate both candidates:

1. **Non-wrap candidate** = `maxSum = 7` from subarray `[5, -3, 5]`
2. **Wrap candidate** = `totalSum - minSum = 7 - (-3) = 10`

Why does that work? Removing the minimum subarray `[-3]` leaves the prefix `[5]` and suffix `[5]`, which form a valid circular subarray. So the answer is `10`.

This example captures the core trick: the best circular window is often “everything except the worst contiguous dip.”

## ⏱ Complexity Analysis

### Time Complexity
`O(n)`. We scan the array once while maintaining running values for total sum, maximum subarray, and minimum subarray. There are no nested loops or auxiliary passes over candidate windows. At `10^6` elements this is routine; at `10^9`, linear time is still expensive but remains the only viable asymptotic class for a single-machine pass.

### Space Complexity
`O(1)`. The algorithm stores a fixed number of scalar accumulators: total sum, current/global max, and current/global min. No heap-backed structure grows with input size. Space can’t meaningfully be reduced further without sacrificing readability rather than asymptotics.

## 💡 Key Takeaways
- If an array problem says “contiguous” and “maximum sum,” Kadane should be your first reflex.
- If the array is circular, look for a complement transformation: wrap-around often means `total - excluded middle`.
- Do not return `totalSum - minSum` when all values are negative; that silently selects an empty subarray.
- The minimum subarray may equal the whole array, which is exactly why the all-negative guard is necessary.
- In production systems over cyclic data, many “circular window” problems collapse into combining a linear pass with a complement view instead of simulating wrap-around explicitly.

## 🚀 Variations & Further Practice
- **Maximum sum subarray in a circular array with length constraints** — harder because Kadane alone is insufficient; you need prefix sums plus a monotonic deque to enforce window bounds.
- **Maximum product circular subarray** — harder because sign flips destroy the simple complement argument; you must track both max and min products.
- **Maximum subarray over `k` concatenations of the same array** — harder because the optimal segment may span multiple copies, requiring reasoning about prefix/suffix maxima and total sum.