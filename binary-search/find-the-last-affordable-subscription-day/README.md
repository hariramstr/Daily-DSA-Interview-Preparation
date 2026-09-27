# Find the Last Affordable Subscription Day

**Difficulty:** Easy &nbsp;|&nbsp; **Topic:** Binary Search &nbsp;|&nbsp; **Tags:** Binary Search, Array, Sorted Array

---

## 🗂 Problem Overview
Given a non-decreasing array `prices` and a `budget`, return the index of the last day whose price is `<= budget`. If no day is affordable, return `-1`. The challenge is not the comparison itself but finding the **rightmost valid index** efficiently, especially when duplicate prices exist. Since the array is already sorted, the key constraint is to avoid linear scanning and exploit order with binary search.

## 🌍 Engineering Impact
This pattern shows up anywhere a system must find the latest acceptable threshold crossing in ordered data: rate-limit windows, feature-flag rollout cutoffs, time-series retention boundaries, search ranking score thresholds, and storage tier selection. In production, replacing a scan with binary search changes lookup cost from `O(n)` to `O(log n)`, which matters when the operation sits on hot paths or runs per request. The broader architectural value is recognizing when monotonicity exists and turning it into a contract: once a predicate flips from true to false, you can search boundaries instead of inspecting every element.

## 🔍 Problem Statement
You are given an integer array `prices` of length `n`, where `1 <= n <= 100000`, `0 <= prices[i] <= 1000000000`, and `prices` is sorted in non-decreasing order. You are also given an integer `budget` with `0 <= budget <= 1000000000`.

Return the index of the **last** position `i` such that `prices[i] <= budget`. If no such index exists, return `-1`.

This is a boundary-search problem, not a membership test. If `budget` matches several repeated values, you must return the largest index among them.

Examples:

- `prices = [5, 7, 7, 10, 14]`, `budget = 7` → `2`
- `prices = [4, 6, 9, 12]`, `budget = 3` → `-1`

The constraint that drives the algorithmic choice is the sorted input: it creates a monotonic predicate, making binary search the correct tool.

## 🪜 How to Solve This
1. Read the requirement carefully → we do **not** need any affordable day; we need the **last** affordable day.
2. Translate that into a predicate: for index `i`, is `prices[i] <= budget`? Because `prices` is sorted, this predicate is true for some prefix of the array, then false afterward.
3. A “true...true...false...false” shape is a boundary-search signal → think binary search, not linear scan.
4. Since we want the **rightmost true**, every time `prices[mid] <= budget`, that index is a valid candidate, but there may be a later one. Record it and move right.
5. If `prices[mid] > budget`, then `mid` and everything to its right are invalid, so move left.
6. Continue until the search space is exhausted. The saved candidate is the answer; if none was ever saved, return `-1`.

The core insight is that sorted order converts the problem from “inspect values” into “locate the transition point.”

## 🧩 Algorithm Walkthrough
1. **Identify the pattern: Binary Search on Answer Boundary.**  
   The array is sorted, and the condition `prices[i] <= budget` is monotonic: once it becomes false, it stays false. That makes this a classic **rightmost valid position** search.

2. **Initialize search bounds.**  
   Set `left = 0`, `right = prices.length - 1`, and `answer = -1`.  
   `answer` stores the best valid index seen so far. The invariant is: every recorded `answer` satisfies `prices[answer] <= budget`.

3. **Pick the midpoint safely.**  
   Compute `mid = left + (right - left) / 2`.  
   This avoids overflow in languages where `left + right` may exceed integer range.

4. **Evaluate the midpoint against the budget.**  
   If `prices[mid] <= budget`, then `mid` is affordable. It is a valid candidate, so set `answer = mid`. But because we need the **last** affordable day, continue searching the right half with `left = mid + 1`.

5. **Discard invalid suffixes.**  
   If `prices[mid] > budget`, then `mid` cannot be part of the answer, and neither can any index to its right. Set `right = mid - 1`.

6. **Terminate when bounds cross.**  
   When `left > right`, the search space is empty. By construction, `answer` is the largest index found with `prices[index] <= budget`, or `-1` if none exists.

This works because each step preserves the boundary invariant: the answer, if it exists, always remains inside the remaining search space or in `answer`.

## 📊 Worked Example
Example: `prices = [5, 7, 7, 10, 14]`, `budget = 7`

| Step | left | right | mid | prices[mid] | Condition        | answer | Next move      |
|------|------|-------|-----|-------------|------------------|--------|----------------|
| 1    | 0    | 4     | 2   | 7           | `7 <= 7` true    | 2      | search right   |
| 2    | 3    | 4     | 3   | 10          | `10 <= 7` false  | 2      | search left    |

Stop when `left = 3`, `right = 2`.

Trace summary:
1. Midpoint `2` is affordable, so it becomes the current best answer.
2. Because we need the **last** affordable day, we continue right.
3. Midpoint `3` is too expensive, so the boundary must be left of it.
4. Search ends; the last recorded valid index is `2`.

This example matters because it includes duplicates, proving that “found a match” is not enough—you must keep pushing right.

## ⏱ Complexity Analysis
### Time Complexity
`O(log n)`. Each iteration halves the remaining search interval, and the dominant operation is a constant-time midpoint comparison. At `10^6` elements, this is roughly 20 comparisons; at `10^9`, roughly 30. That difference is why binary search remains viable on large ordered datasets where linear scans do not.

### Space Complexity
`O(1)`. The algorithm uses a fixed number of scalar variables: `left`, `right`, `mid`, and `answer`. No auxiliary structure is allocated. Space cannot be meaningfully reduced further without changing the execution model; the trade-off is already optimal for an in-memory iterative search.

## 💡 Key Takeaways
- If the input is sorted and the condition looks like a prefix of valid values followed by invalid values, treat it as a boundary-search problem.
- Phrases like “last index,” “rightmost valid,” or “largest position satisfying a condition” are strong binary-search signals.
- When `prices[mid] <= budget`, do not return immediately; save `mid` and continue right to find the last valid occurrence.
- Initialize `answer = -1` so the “no affordable day exists” case falls out naturally without special post-processing.
- In production systems, monotonic predicates are leverage: once recognized, they let you replace repeated full scans with logarithmic boundary lookups.

## 🚀 Variations & Further Practice
- Find the **first** day where `prices[i] > budget`; same monotonic structure, but now you search for the leftmost invalid position instead of the rightmost valid one.
- Given many customer budgets, answer each query efficiently against the same sorted `prices`; the twist is optimizing repeated lookups, potentially with batched processing or precomputed boundaries.
- Search in a rotated sorted array for the last affordable day; the harder part is that global monotonicity is broken, so the boundary logic must be adapted before binary search applies.