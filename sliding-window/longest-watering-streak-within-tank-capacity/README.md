# Longest Watering Streak Within Tank Capacity

**Difficulty:** Easy &nbsp;|&nbsp; **Topic:** Sliding Window &nbsp;|&nbsp; **Tags:** Sliding Window, Array, Two Pointers

---

## 🗂 Problem Overview
Given an array `usage` of daily water consumption and an integer `capacity`, find the maximum length of a contiguous block of days whose total usage does not exceed `capacity`. The output is that maximum length, or `0` if even a single day exceeds the tank limit. The challenge is efficiency: with up to `100000` days, enumerating every possible subarray is too slow, so the solution must exploit structure in contiguous ranges.

## 🌍 Engineering Impact
This pattern shows up anywhere systems must maximize a continuous interval under a cumulative budget. Examples include API rate-limit windows, streaming pipelines constrained by memory or latency budgets, log ingestion batches bounded by payload size, and ad-serving or search ranking systems that process contiguous event slices. At scale, brute-force range evaluation collapses under quadratic behavior and destroys tail latency. Sliding-window reasoning enables single-pass admission control, online optimization, and predictable resource usage. Architecturally, it turns a batch-style search problem into a streaming-friendly primitive that composes well with backpressure, bounded buffers, and real-time monitoring.

## 🔍 Problem Statement
You are given:

- `usage`, where `usage[i]` is the liters of water needed on day `i`
- `capacity`, the maximum liters available from one tank fill

Return the length of the longest contiguous subarray whose sum is `<= capacity`.

Constraints:

- `1 <= usage.length <= 100000`
- `0 <= usage[i] <= 10000`
- `0 <= capacity <= 10^9`

Important edge cases:

- Days must be consecutive; you cannot skip or reorder.
- If every individual `usage[i] > capacity`, the answer is `0`.
- Zero-usage days are valid and can extend a window.

Examples:

- `usage = [2, 1, 3, 2, 1]`, `capacity = 5` → `2`
- `usage = [1, 1, 1, 2, 1]`, `capacity = 4` → `3`

The key constraint is input size: `O(n^2)` subarray checking is not acceptable, which strongly suggests a linear-time windowing strategy.

## 🪜 How to Solve This
1. Read the problem → we need the **longest contiguous range** under a **sum constraint**. That is a classic signal for a sliding window.
2. Notice all values are non-negative (`usage[i] >= 0`) → this is the property that makes two pointers work cleanly. Expanding the window can only increase or preserve the sum; shrinking can only decrease or preserve it.
3. Start with a window `[left..right]` and maintain its running sum.
4. Move `right` forward one day at a time, adding `usage[right]` into the sum.
5. If the sum exceeds `capacity`, the current window is invalid → move `left` forward until the sum is valid again.
6. After re-validating, the window is the longest valid window ending at `right`, so update the best length.
7. Repeat once across the array.

Why this works: instead of recomputing sums for every subarray, you reuse prior work. Each element enters the window once and leaves once, giving linear time with constant extra space.

## 🧩 Algorithm Walkthrough
1. **Choose the pattern: Sliding Window / Two Pointers.**  
   This problem asks for the longest contiguous segment satisfying `sum <= capacity`. Because all `usage` values are non-negative, window validity is monotonic: extending right never reduces the sum, and advancing left never increases it. That monotonicity is exactly why two pointers are the right abstraction.

2. **Initialize state.**  
   Set `left = 0`, `windowSum = 0`, and `best = 0`.  
   Invariant: before processing each position, `windowSum` equals the sum of `usage[left..right]` for the current window.

3. **Expand the window.**  
   For each `right` from `0` to `n - 1`, add `usage[right]` to `windowSum`.  
   This considers every possible window endpoint exactly once.

4. **Restore validity when over capacity.**  
   While `windowSum > capacity`, subtract `usage[left]` and increment `left`.  
   Invariant after this loop: the window `[left..right]` is valid, and any window ending at `right` but starting left of `left` would be invalid.

5. **Record the best valid length.**  
   Compute `right - left + 1` and update `best` if larger.  
   This is correct because once the window is valid, it is the longest valid window ending at `right`.

6. **Return `best`.**  
   If no single day fits, the window repeatedly collapses and `best` remains `0`, which matches the requirement.

## 📊 Worked Example
Example: `usage = [1, 1, 1, 2, 1]`, `capacity = 4`

| Step | `right` | Added | `windowSum` before shrink | `left` after shrink | Valid window | Length | `best` |
|---|---:|---:|---:|---:|---|---:|---:|
| 1 | 0 | 1 | 1 | 0 | `[1]` | 1 | 1 |
| 2 | 1 | 1 | 2 | 0 | `[1,1]` | 2 | 2 |
| 3 | 2 | 1 | 3 | 0 | `[1,1,1]` | 3 | 3 |
| 4 | 3 | 2 | 5 | 1 | `[1,1,2]` | 3 | 3 |
| 5 | 4 | 1 | 5 | 2 | `[1,2,1]` | 3 | 3 |

Trace notes:

- At `right = 3`, adding `2` makes the sum `5`, so we remove the leftmost `1`.
- At `right = 4`, adding `1` again makes the sum `5`, so we shrink once more.
- The longest valid contiguous window found has length `3`.

## ⏱ Complexity Analysis

### Time Complexity
`O(n)`. Each element is added to the running sum once when `right` advances and removed at most once when `left` advances. There is no nested reprocessing of the array. At `10^6` elements this is practical in a single pass; at `10^9`, runtime becomes infrastructure-bound rather than algorithmically quadratic.

### Space Complexity
`O(1)`. The algorithm stores only pointer indices, a running sum, and the best length. No auxiliary array or prefix-sum structure is required. Space cannot be meaningfully reduced further without losing the ability to maintain the current window incrementally.

## 💡 Key Takeaways
- If the problem asks for a **longest/shortest contiguous subarray** under a threshold and all values are non-negative, sliding window should be your first candidate.
- “Contiguous + cumulative constraint + large `n`” is a strong pattern-recognition signal that brute-force subarray enumeration is the wrong shape.
- Update `best` **after** shrinking the window back to validity; doing it before can record invalid ranges.
- Be careful with the window length formula: for inclusive pointers, it is `right - left + 1`, not `right - left`.
- In production systems, this pattern matters because it converts budget-constrained range selection from quadratic batch work into a linear streaming primitive.

## 🚀 Variations & Further Practice
- **Shortest subarray with sum at least `K`**: harder because the optimization target flips, and negative numbers often require prefix sums plus a monotonic deque.
- **Longest subarray with at most `K` distinct values**: same sliding-window skeleton, but validity depends on frequency counts rather than a numeric sum.
- **Longest subarray with sum `<= capacity` when negatives are allowed**: standard sliding window breaks because monotonicity disappears, forcing more advanced prefix-sum or balanced-structure techniques.