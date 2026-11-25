# Maximize Scenic Stops Within Fuel Budget

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Two Pointers &nbsp;|&nbsp; **Tags:** Two Pointers, Sliding Window, Array

---

## 🗂 Problem Overview
Given a sorted array `miles` and an integer `budget`, find the largest contiguous segment of stops whose span fits within the fuel constraint. Formally, maximize `right - left + 1` such that `miles[right] - miles[left] <= budget`. The output is that maximum count. The problem is non-trivial because `n` can reach `100000`, which rules out checking all subarrays and forces a linear or near-linear strategy.

## 🌍 Engineering Impact
This pattern shows up anywhere you need the largest valid range under a bounded spread: event windows in streaming pipelines, latency buckets in observability systems, time-cluster detection in fraud analysis, and shard-local compaction windows in storage engines. At scale, brute-force range evaluation collapses under quadratic growth and destroys tail latency. The two-pointer window works because validity is monotonic as endpoints move through sorted data. That property enables single-pass range expansion, predictable memory use, and easy integration into online processing paths where you cannot afford rescanning historical elements.

## 🔍 Problem Statement
You are given a sorted, non-decreasing array `miles` of length `n`, where each value is a scenic stop’s mile marker, and an integer `budget`. You may choose any contiguous block of stops `[left, right]`. That block is valid only if the distance between its farthest and nearest stop is at most `budget`, meaning:

`miles[right] - miles[left] <= budget`

Return the maximum number of stops in any valid block.

Constraints:
- `1 <= n <= 100000`
- `0 <= miles[i] <= 10^9`
- `miles` is sorted in non-decreasing order
- `0 <= budget <= 10^9`

Examples:
- `miles = [2, 5, 6, 7, 11, 15], budget = 4` → `3`
- `miles = [1, 1, 3, 4, 8, 9, 10], budget = 2` → `3`

The key algorithmic driver is the input size: `O(n^2)` subarray enumeration is too expensive, so the solution must exploit sorted order.

## 🪜 How to Solve This
1. Read the condition carefully → validity depends only on the first and last elements of a consecutive segment, not on everything inside it.
2. Notice the array is already sorted → for any fixed `right`, moving `left` rightward can only shrink the span `miles[right] - miles[left]`.
3. That monotonic behavior is the signal for a sliding window / two-pointer approach.
4. Start with both pointers at the beginning. Expand `right` one stop at a time to consider larger segments.
5. If the window becomes invalid, do not restart from scratch. Just advance `left` until the span fits the budget again.
6. After restoring validity, the current window is the largest valid segment ending at this `right`, because any smaller `left` would violate the constraint.
7. Track the maximum window length seen during the scan.

The key insight is that each pointer only moves forward. That converts what looks like a range-search problem into a single linear pass.

## 🧩 Algorithm Walkthrough
1. **Initialize state**  
   Set `left = 0` and `best = 1`. We will iterate `right` from `0` to `n - 1`. The active window is always `[left, right]`.

2. **Expand the window with `right`**  
   For each new stop at index `right`, include it in the current segment. Because the array is sorted, the window span is simply `miles[right] - miles[left]`.

3. **Restore validity by moving `left`**  
   While `miles[right] - miles[left] > budget`, increment `left`. This is the core Two Pointers / Sliding Window step. It is correct because increasing `left` is the only way to reduce the span for a fixed `right`.

4. **Maintain the invariant**  
   After the while-loop, the window `[left, right]` is valid, and `left` is the smallest index that makes it valid. That invariant matters: it guarantees this window is the longest valid segment ending at `right`.

5. **Update the answer**  
   Compute `right - left + 1` and update `best` if larger. Since every valid maximal-ending-at-`right` window is considered, the global maximum will be found.

6. **Why this abstraction fits**  
   This is a textbook sliding window because the validity predicate is monotonic over a sorted sequence. Once a window is too wide, extending it further cannot fix it; only shrinking from the left can. That monotonicity is what makes linear-time scanning possible.

## 📊 Worked Example
Example: `miles = [2, 5, 6, 7, 11, 15]`, `budget = 4`

| right | miles[right] | left after shrink | window values   | span | length | best |
|------:|-------------:|------------------:|-----------------|-----:|-------:|-----:|
| 0     | 2            | 0                 | [2]             | 0    | 1      | 1    |
| 1     | 5            | 0                 | [2, 5]          | 3    | 2      | 2    |
| 2     | 6            | 0                 | [2, 5, 6]       | 4    | 3      | 3    |
| 3     | 7            | 1                 | [5, 6, 7]       | 2    | 3      | 3    |
| 4     | 11           | 3                 | [7, 11]         | 4    | 2      | 3    |
| 5     | 15           | 4                 | [11, 15]        | 4    | 2      | 3    |

At `right = 3`, `[2, 5, 6, 7]` is invalid because span `= 5`, so `left` moves to `1`. The valid window becomes `[5, 6, 7]`, length `3`, which matches the final answer.

## ⏱ Complexity Analysis
### Time Complexity
`O(n)`. Each element is visited once by `right`, and `left` also advances at most `n` times across the entire run. There is no nested rescanning. At `10^6` elements this remains practical; at `10^9`, linear work is still expensive but fundamentally better than any quadratic alternative.

### Space Complexity
`O(1)`. The algorithm uses only a few integer variables: `left`, `right`, and `best`. No auxiliary data structure grows with input size. Space cannot be meaningfully reduced further without changing the execution model, since the input array itself dominates memory.

## 💡 Key Takeaways
- If the input is sorted and window validity depends only on endpoint difference, think sliding window immediately.
- If shrinking one side monotonically restores validity, you likely have a two-pointer linear-pass solution.
- Be careful to shrink with `while`, not `if`; one left move may not be enough to restore `miles[right] - miles[left] <= budget`.
- Window length is `right - left + 1`; this is the common off-by-one bug in inclusive index ranges.
- In production systems, monotonic validity predicates are worth designing for because they turn expensive global searches into streaming-friendly local updates.

## 🚀 Variations & Further Practice
- Allow arbitrary unsorted input and ask for the largest valid subset by value range. The twist is that you must sort first or use an ordered structure, which changes both complexity and data-movement costs.
- Replace count maximization with maximizing total score/weight of stops under the same span constraint. The harder part is maintaining aggregate value while preserving window validity.
- Ask for the number of valid segments instead of the longest one. The conceptual twist is combinatorial counting: once a window is valid, all suffixes ending at `right` may also be valid.