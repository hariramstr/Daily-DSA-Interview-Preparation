# Longest Reading Lamp Stretch Within Battery Budget

**Difficulty:** Easy &nbsp;|&nbsp; **Topic:** Sliding Window &nbsp;|&nbsp; **Tags:** Sliding Window, Array, Two Pointers

---

## 🗂 Problem Overview
Given a non-negative integer array `batteryUse` and an integer `budget`, find the maximum length of any contiguous subarray whose total sum is at most `budget`. The output is a single integer: the longest valid window size. The challenge is efficiency: `batteryUse.length` can reach `100000`, so brute-force enumeration of all subarrays is too slow. The key structural property is that all values are non-negative, which makes a shrinking sliding window sufficient.

## 🌍 Engineering Impact
This pattern shows up anywhere systems need the longest contiguous interval under a cumulative resource cap: API rate-limit windows, streaming jobs bounded by memory or latency budgets, mobile telemetry batching, and log ingestion pipelines constrained by payload size. At scale, the difference between rescanning prefixes and maintaining a rolling window is the difference between linear throughput and collapse under bursty input. The non-negative-cost assumption is operationally important: it lets you use monotonic window contraction instead of heavier structures like prefix-sum trees, enabling predictable latency, low memory overhead, and simple correctness reasoning in hot paths.

## 🔍 Problem Statement
You are given:

- `batteryUse`, where `batteryUse[i]` is the battery units consumed during minute `i`
- `budget`, the maximum total battery usage allowed before recharge

Return the length of the longest contiguous block of minutes whose total battery consumption is `<= budget`.

Constraints:

- `1 <= batteryUse.length <= 100000`
- `0 <= batteryUse[i] <= 10000`
- `0 <= budget <= 1000000000`

Examples:

- `batteryUse = [2, 1, 3, 2, 1], budget = 5` → `2`
- `batteryUse = [0, 1, 1, 0, 2, 1], budget = 3` → `4`

Edge cases matter:

- `budget = 0` means only all-zero windows are valid
- zero values can extend a valid window without increasing sum
- a single element may already exceed `budget`, forcing the window to collapse

The algorithmic choice is driven by one constraint: all values are non-negative, so once a window exceeds budget, advancing the left pointer is the only move needed to restore validity.

## 🪜 How to Solve This
1. Read the problem → we need a **contiguous** segment, so this is not a sorting or subset problem. It is about windows over an array.

2. Notice the condition is on the **sum of the current block**. That suggests maintaining a running total instead of recomputing sums for every candidate subarray.

3. Check the value domain → all `batteryUse[i]` values are non-negative. That is the decisive signal for a sliding window:
   - expanding right can only keep the sum the same or increase it
   - if the sum becomes too large, moving left forward can only decrease it

4. So maintain a window `[left, right]` and its sum.
   - Extend `right` one step at a time
   - Add the new value to the sum
   - While the sum exceeds `budget`, move `left` rightward and subtract values
   - After the window is valid again, update the best length

5. Why this works: every index enters the window once and leaves once. No backtracking, no nested rescans, no prefix-sum search structure needed.

That chain of reasoning leads directly to an `O(n)` solution with constant extra space.

## 🧩 Algorithm Walkthrough
1. **Choose the pattern: Sliding Window / Two Pointers.**  
   We need the longest contiguous range satisfying a monotonic constraint: window sum `<= budget`. Because all numbers are non-negative, expanding the right pointer never decreases the sum, and shrinking the left pointer never increases it. That monotonicity is exactly why Two Pointers is the right abstraction.

2. **Initialize state.**  
   Set `left = 0`, `windowSum = 0`, and `best = 0`.  
   Invariant before each iteration: `windowSum` equals the sum of elements in the current window `[left, right]` after any needed shrinking.

3. **Expand the window from the right.**  
   For each `right` from `0` to `n - 1`, add `batteryUse[right]` to `windowSum`.  
   This considers every possible ending position exactly once.

4. **Restore validity when over budget.**  
   While `windowSum > budget`, subtract `batteryUse[left]` and increment `left`.  
   This is correct because with non-negative values, no prefix of the current invalid window can become valid unless we remove elements from the left. We never need to move `right` backward.

5. **Record the best valid length.**  
   Once the while-loop ends, the window is valid again. Update `best = max(best, right - left + 1)`.  
   Invariant maintained: after each iteration, `[left, right]` is the longest valid window ending at `right` that can be obtained without reconsidering prior positions.

6. **Return `best`.**  
   Correctness follows from exhaustive right-end exploration plus minimal left contraction. Every valid maximal window is observed when its right boundary is processed.

## 📊 Worked Example
Example: `batteryUse = [0, 1, 1, 0, 2, 1]`, `budget = 3`

| right | value | action                         | left | windowSum | best | window |
|------:|------:|--------------------------------|-----:|----------:|-----:|--------|
| 0     | 0     | add 0                          | 0    | 0         | 1    | [0] |
| 1     | 1     | add 1                          | 0    | 1         | 2    | [0,1] |
| 2     | 1     | add 1                          | 0    | 2         | 3    | [0,1,1] |
| 3     | 0     | add 0                          | 0    | 2         | 4    | [0,1,1,0] |
| 4     | 2     | add 2 → sum 4, shrink from left| 1    | 4         | 4    | [1,1,0,2] |
| 4     | -     | shrink again                   | 2    | 3         | 4    | [1,0,2] |
| 5     | 1     | add 1 → sum 4, shrink          | 3    | 3         | 4    | [0,2,1] |

The maximum valid length seen is `4`, from subarray `[0,1,1,0]`.

## ⏱ Complexity Analysis
### Time Complexity
`O(n)`. Each element is added to the running sum once when `right` advances and removed at most once when `left` advances. The dominant work is therefore linear pointer movement, not repeated summation. At `10^6` elements this is routine; at `10^9`, linear time is still expensive but fundamentally viable in streaming form, unlike quadratic alternatives.

### Space Complexity
`O(1)`. The algorithm stores only scalar state: two pointers, a running sum, and the best length. No auxiliary array or heap is required. Space cannot be meaningfully reduced below constant without losing the ability to track the active window sum.

## 💡 Key Takeaways
- If the problem asks for a **longest contiguous subarray** under a threshold and all values are non-negative, sliding window should be your default first check.
- The tell is **monotonic validity**: expanding the window can only worsen an over-budget sum, so shrinking from the left is enough to recover.
- Update the answer **after** shrinking, not before; otherwise you may record invalid windows.
- Be careful with `right - left + 1`; this is the most common off-by-one error in inclusive window boundaries.
- In production systems, non-negative cost models enable single-pass admission-control logic with predictable latency and no indexing overhead.

## 🚀 Variations & Further Practice
- **Minimum size subarray sum**: instead of the longest valid window, find the shortest window meeting a lower bound; same pattern, but the optimization target flips.
- **Longest subarray with sum exactly `k` allowing negative numbers**: sliding window breaks because monotonicity disappears; requires prefix sums plus a hash map.
- **Maximum consecutive requests under weighted multi-resource budgets**: extend from one scalar budget to CPU/memory/network vectors, where validity checks become multidimensional and shrinking logic is still local but more operationally nuanced.