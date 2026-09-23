# Locate First Warehouse Shelf With Required Capacity

**Difficulty:** Easy &nbsp;|&nbsp; **Topic:** Binary Search &nbsp;|&nbsp; **Tags:** Binary Search, Array, Lower Bound

---

## 🗂 Problem Overview
Given a sorted array `capacities` and a target `requiredWeight`, return the index of the first shelf whose capacity is at least the target. If no such shelf exists, return `-1`. The key constraint is that the array is already sorted in non-decreasing order, which makes this a lower-bound search problem. The non-trivial part is not finding any valid shelf, but the earliest valid one, especially when duplicates exist.

## 🌍 Engineering Impact
This pattern shows up anywhere a system needs the first resource, threshold, or version satisfying a monotonic condition. Examples include shard selection in distributed rate-limiters, picking the first index block in storage engines with enough free space, locating the first compiler symbol version valid for a source position, or finding the first ranking bucket above a score cutoff. At scale, replacing linear scans with logarithmic lookup changes tail latency and CPU burn materially. More importantly, lower-bound search is a reusable abstraction for monotonic decision spaces, which is a common systems design building block.

## 🔍 Problem Statement
You are given an integer array `capacities` of length `n`, sorted in non-decreasing order, where `capacities[i]` is the maximum weight shelf `i` can support. You are also given an integer `requiredWeight`.

Return the index of the first shelf whose capacity is greater than or equal to `requiredWeight`. If every shelf has capacity less than `requiredWeight`, return `-1`.

Constraints:

- `1 <= capacities.length <= 100000`
- `1 <= capacities[i] <= 1000000000`
- `capacities` is sorted in non-decreasing order
- `1 <= requiredWeight <= 1000000000`

Examples:

- `capacities = [5, 8, 8, 12, 15], requiredWeight = 8` → `1`
- `capacities = [3, 4, 6, 9], requiredWeight = 10` → `-1`

The decisive constraint is sorted order. Without it, binary search is invalid; with it, scanning all elements is unnecessary.

## 🪜 How to Solve This
1. Read the requirement carefully → we do **not** want any shelf that works; we want the **first** one that works.

2. Notice the array is sorted → that means once a shelf can hold `requiredWeight`, every shelf to its right can also hold it or exceed it.

3. That creates a monotonic boundary:
   - left side: capacities `< requiredWeight`
   - right side: capacities `>= requiredWeight`

4. Whenever a problem asks for the first position where a monotonic predicate becomes true, think **binary search for lower bound**.

5. Start with the full search interval. At each midpoint:
   - if `capacities[mid] >= requiredWeight`, the answer could be `mid` or something earlier, so move left
   - otherwise, `mid` is definitely too small, so move right

6. Keep shrinking until the interval collapses. At that point, you have the first candidate position where the condition could hold.

7. Finally, verify that the candidate is in bounds and actually satisfies the requirement; otherwise return `-1`.

This is the standard “first true” binary search pattern.

## 🧩 Algorithm Walkthrough
1. **Recognize the pattern: Lower Bound Binary Search.**  
   The predicate is `capacities[i] >= requiredWeight`. Because `capacities` is sorted, this predicate is false for some prefix and true for the remaining suffix. That monotonic structure is exactly what binary search exploits.

2. **Initialize search bounds.**  
   Set `left = 0` and `right = capacities.length`. Using a half-open interval `[left, right)` simplifies edge handling, especially when the answer may not exist. The invariant is that the first valid index, if any, always lies within this interval.

3. **Compute the midpoint safely.**  
   Use `mid = left + (right - left) / 2`. This avoids overflow in languages where `left + right` could exceed integer range, even though the current constraints are modest.

4. **Evaluate the midpoint against the predicate.**  
   If `capacities[mid] >= requiredWeight`, then `mid` is a valid candidate, but there may be an earlier one. Set `right = mid`. This preserves the invariant that the first valid index remains in `[left, right)`.

5. **Discard impossible prefixes.**  
   If `capacities[mid] < requiredWeight`, then every index `<= mid` is invalid. Set `left = mid + 1`. The invariant still holds because the first valid index must be strictly to the right.

6. **Terminate when bounds converge.**  
   When `left == right`, the search space is empty. That position is the smallest index where the predicate could be true.

7. **Validate the result.**  
   If `left == capacities.length`, no shelf satisfies the requirement, so return `-1`. Otherwise return `left`.

## 📊 Worked Example
Example: `capacities = [5, 8, 8, 12, 15]`, `requiredWeight = 8`

| Step | left | right | mid | capacities[mid] | Decision |
|------|------|-------|-----|-----------------|----------|
| 1 | 0 | 5 | 2 | 8 | Valid candidate, move left: `right = 2` |
| 2 | 0 | 2 | 1 | 8 | Valid candidate, move left: `right = 1` |
| 3 | 0 | 1 | 0 | 5 | Too small, move right: `left = 1` |

Now `left == right == 1`, so the search stops.

Interpretation:
- Index `0` is invalid because capacity `5 < 8`
- Index `1` is valid because capacity `8 >= 8`
- Since binary search kept pushing left whenever it found a valid shelf, index `1` is guaranteed to be the first valid answer

Return `1`.

## ⏱ Complexity Analysis
### Time Complexity
Binary search runs in **O(log n)** time because each comparison halves the remaining search interval. The dominant operation is the midpoint comparison against `requiredWeight`. At `10^6` elements this is about 20 iterations; at `10^9`, about 30. That is the practical value of exploiting sorted structure instead of scanning linearly.

### Space Complexity
The algorithm uses **O(1)** extra space. It stores only a few index variables and does not allocate auxiliary structures. This cannot be meaningfully reduced further; the main trade-off is readability versus boundary-management style, not memory.

## 💡 Key Takeaways
- If the input is sorted and the question asks for the **first** index meeting a condition, think **lower bound** immediately.
- If a predicate is false on a prefix and true on a suffix, you are looking at a monotonic search space suited for binary search.
- Returning on the first match is wrong here; duplicates mean you must keep searching left after finding a valid midpoint.
- Be explicit about interval semantics: `[left, right)` avoids many off-by-one errors and makes the “not found” case clean.
- In production systems, lower-bound search is the reusable primitive behind threshold routing, version lookup, and first-fit selection over ordered metadata.

## 🚀 Variations & Further Practice
- Find the **last** shelf with capacity less than or equal to a target. Twist: switch from lower bound to upper-bound-style reasoning and adjust boundary updates carefully.
- Search in a **rotated sorted array** for the first valid threshold. Twist: sortedness is no longer global, so the monotonic region must be recovered before binary search applies.
- Handle **multiple queries** over the same `capacities` array. Twist: the per-query algorithm stays `O(log n)`, but system design shifts toward preprocessing, caching, and API-level throughput considerations.