# Longest Pantry Restock Run Within Box Capacity

**Difficulty:** Easy &nbsp;|&nbsp; **Topic:** Sliding Window &nbsp;|&nbsp; **Tags:** Sliding Window, Array, Two Pointers

---

## 🗂 Problem Overview
Given an array `sizes` of positive integers and an integer `capacity`, find the maximum length of a contiguous subarray whose sum is at most `capacity`. The output is only the length, not the subarray itself. The challenge is to do this efficiently for up to `100000` items. The key property is that all values are positive, which makes a shrinking/expanding sliding window valid and eliminates the need for backtracking or prefix-sum search structures.

## 🌍 Engineering Impact
This pattern shows up anywhere systems need the longest contiguous segment under a resource budget: batching events into network frames, packing jobs into memory-constrained executors, sizing Kafka fetch windows, limiting request bursts in API gateways, or grouping records for vectorized processing. At scale, brute force fails immediately because every candidate range becomes prohibitively expensive. The sliding-window formulation enables single-pass admission control over ordered streams, preserving sequence semantics while staying within latency and memory budgets. The broader architectural value is recognizing when monotonicity lets you replace quadratic search with linear-time streaming logic.

## 🔍 Problem Statement
You are given:

- `sizes`, an array of positive integers where `sizes[i]` is the size of the `i`th pantry item
- `capacity`, the maximum total size a chosen box can hold

You must return the length of the longest **contiguous** subarray whose sum is `<= capacity`.

Constraints:

- `1 <= sizes.length <= 100000`
- `1 <= sizes[i] <= 10000`
- `1 <= capacity <= 1000000000`
- All `sizes[i]` are positive integers

Examples:

- `sizes = [2, 1, 3, 2, 1], capacity = 6` → `3`
- `sizes = [5, 4, 2, 1], capacity = 3` → `2`

Edge cases matter:

- If every individual item exceeds `capacity`, return `0`
- Order cannot change, so sorting is invalid
- The positivity constraint is what drives the algorithmic choice: once a window sum is too large, moving `left` forward is the only useful correction, and it will always decrease the sum.

## 🪜 How to Solve This
1. Read the problem carefully → we need a **contiguous** run, so this is about ranges, not subsets.
2. Notice all values are **positive** → expanding a range can only increase the sum, and shrinking it can only decrease the sum.
3. That monotonic behavior is the signal for a **sliding window / two pointers** approach.
4. Start with an empty window `[left..right]` and grow `right` one step at a time, adding each new item into a running sum.
5. If the sum stays within `capacity`, the current window is valid, so update the best length.
6. If the sum exceeds `capacity`, do not restart from scratch. Instead, move `left` forward until the window becomes valid again.
7. Why this works: with positive numbers, any oversized window cannot be fixed except by removing items from the left. No skipped state can become better by waiting.
8. This gives a single linear scan: each element enters the window once and leaves once, which is exactly what you want under `10^5+` input sizes.

## 🧩 Algorithm Walkthrough
1. **Initialize the sliding window state.**  
   Set `left = 0`, `windowSum = 0`, and `maxLen = 0`. The invariant is: before recording a candidate answer, the window `[left..right]` must satisfy `windowSum <= capacity`.

2. **Advance the right pointer across the array.**  
   For each `right` from `0` to `n - 1`, add `sizes[right]` to `windowSum`. This represents expanding the current contiguous run by one item.

3. **Repair invalid windows by shrinking from the left.**  
   While `windowSum > capacity`, subtract `sizes[left]` and increment `left`. This is the core Two Pointers / Sliding Window step. It is correct because all values are positive: removing items from the left is guaranteed to reduce the sum, and keeping an invalid prefix can never help future windows.

4. **Record the best valid length.**  
   Once the while-loop ends, the current window is valid again. Compute `right - left + 1` and update `maxLen` if larger. The maintained invariant is that this is the longest valid window ending at `right`, because `left` is the smallest index that makes the sum legal.

5. **Return `maxLen`.**  
   If no item fits, the window repeatedly collapses and `maxLen` remains `0`, which matches the required behavior.

This abstraction is the right one because the problem asks for an extremal contiguous range under a monotone budget constraint. That is exactly where sliding windows dominate.

## 📊 Worked Example
Example: `sizes = [2, 1, 3, 2, 1]`, `capacity = 6`

| Step | `right` | Added | `windowSum` before shrink | `left` after shrink | Valid window | Length | `maxLen` |
|---|---:|---:|---:|---:|---|---:|---:|
| 1 | 0 | 2 | 2 | 0 | `[2]` | 1 | 1 |
| 2 | 1 | 1 | 3 | 0 | `[2,1]` | 2 | 2 |
| 3 | 2 | 3 | 6 | 0 | `[2,1,3]` | 3 | 3 |
| 4 | 3 | 2 | 8 | 2 | `[3,2]` | 2 | 3 |
| 5 | 4 | 1 | 6 | 2 | `[3,2,1]` | 3 | 3 |

Trace notes:

- At `right = 3`, adding `2` makes the sum `8`, which exceeds capacity.
- Shrink from the left: remove `2`, then remove `1`, leaving sum `5` and `left = 2`.
- The best length remains `3`.
- Final answer: `3`.

## ⏱ Complexity Analysis

### Time Complexity
`O(n)`. Each element is added to the window once when `right` advances and removed at most once when `left` advances. There is no nested reprocessing despite the inner while-loop. At `10^6` elements this is still practical in a single pass; at `10^9`, throughput and I/O dominate, but the algorithmic shape remains optimal.

### Space Complexity
`O(1)`. The algorithm stores only pointer indices, a running sum, and the best length. No auxiliary arrays, heaps, or maps are required. Space cannot be meaningfully reduced further without losing the running-sum state needed for constant-time window updates.

## 💡 Key Takeaways
- If the problem asks for a **longest/shortest contiguous range** under a threshold and all numbers are positive, sliding window should be your first candidate.
- The strongest recognition signal is **monotonicity**: expanding only increases cost, shrinking only decreases it.
- Update `maxLen` only **after** restoring `windowSum <= capacity`; doing it earlier records invalid windows.
- Be careful with window length calculation: for an inclusive range, it is `right - left + 1`, not `right - left`.
- In production systems, this pattern matters because monotone constraints let you turn expensive range search into deterministic single-pass stream processing.

## 🚀 Variations & Further Practice
- **Longest subarray with sum exactly equal to `k`**: harder because positivity may not be enough; with arbitrary integers, sliding window breaks and prefix-sum hashing becomes necessary.
- **Minimum size subarray sum at least `target`**: same pattern, but the optimization goal flips from longest valid to shortest satisfying window.
- **Longest subarray with at most `k` distinct values**: still sliding window, but validity is no longer tracked by a scalar sum; you need a frequency map and distinct-count maintenance.