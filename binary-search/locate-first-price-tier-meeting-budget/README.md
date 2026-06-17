# Locate First Price Tier Meeting Budget

**Difficulty:** Easy &nbsp;|&nbsp; **Topic:** Binary Search &nbsp;|&nbsp; **Tags:** Binary Search, Sorted Array, Lower Bound

---

## 🗂 Problem Overview
Given a sorted array `tiers` and an integer `budget`, return the smallest index `i` where `tiers[i] >= budget`. If no such index exists, return `-1`. The challenge is not the comparison itself but exploiting the sorted order to avoid scanning the full array. Because duplicates are allowed, the goal is specifically the *first* qualifying position, not just any match.

## 🌍 Engineering Impact
This is the lower-bound lookup pattern, and it shows up everywhere: pricing engines selecting the first applicable tier, distributed rate-limiters mapping usage to thresholds, storage systems locating the first SSTable key range that may contain a target, search infrastructure finding insertion points in posting lists, and compiler/runtime metadata lookups over sorted symbol ranges. At scale, replacing linear scans with logarithmic search changes tail latency and CPU cost materially. More importantly, it enables predictable performance under growth: doubling data size adds one comparison level, not another full pass through hot-path state.

## 🔍 Problem Statement
You are given a sorted integer array `tiers`, where `tiers[i]` is the minimum order amount required to unlock the `i`-th discount tier, and an integer `budget`. Return the smallest index `i` such that `tiers[i] >= budget`. If every value in `tiers` is smaller than `budget`, return `-1`.

The array is sorted in non-decreasing order, may contain duplicates, and can be empty.

**Constraints**
- `0 <= tiers.length <= 100000`
- `0 <= tiers[i] <= 1000000000`
- `tiers` is sorted in non-decreasing order
- `0 <= budget <= 1000000000`

**Examples**
- `tiers = [20, 35, 50, 50, 80], budget = 50` → `2`
- `tiers = [10, 15, 15, 30], budget = 16` → `3`

The key constraint is sorted order. That makes binary search the intended solution, reducing lookup time from `O(n)` to `O(log n)`.

## 🪜 How to Solve This
1. Read the requirement carefully → we do **not** need to find an exact match; we need the **first value greater than or equal to** `budget`.

2. Notice the array is already sorted → that immediately suggests binary search, because the predicate “is `tiers[i] >= budget`?” becomes monotonic:
   - once it becomes true,
   - it stays true for all later indices.

3. Translate the problem into boundary finding → we are looking for the transition point between:
   - values `< budget`
   - values `>= budget`

4. Because duplicates may exist, a normal “find target” binary search is insufficient. Even if we hit `budget`, we must keep searching left to find the first valid index.

5. Maintain a candidate answer while shrinking the search space:
   - if `tiers[mid] >= budget`, record `mid` and move left
   - otherwise move right

6. When the search ends, the recorded candidate is the leftmost valid tier. If no candidate was recorded, return `-1`.

That is the standard **lower bound** pattern.

## 🧩 Algorithm Walkthrough
1. **Use the Binary Search / Lower Bound pattern.**  
   This problem is not about membership; it is about finding the leftmost index satisfying a monotonic condition. The condition is `tiers[i] >= budget`. Since `tiers` is sorted, all indices before the answer fail the condition, and the answer plus everything after it satisfy it.

2. **Initialize search boundaries.**  
   Set `left = 0`, `right = tiers.length - 1`, and `answer = -1`.  
   `answer` stores the best valid index seen so far. Starting with `-1` cleanly handles the case where no tier meets the budget.

3. **Compute the midpoint and inspect the value.**  
   While `left <= right`, compute `mid = left + (right - left) / 2`.  
   This avoids overflow in languages where `left + right` could exceed integer limits.

4. **If `tiers[mid] >= budget`, move left.**  
   Record `answer = mid`, then set `right = mid - 1`.  
   Why: `mid` is valid, but there may be an earlier valid index.  
   Invariant maintained: any index stored in `answer` is valid, and any better answer must lie to its left.

5. **If `tiers[mid] < budget`, move right.**  
   Set `left = mid + 1`.  
   Why: `mid` and everything before it cannot be the answer because sorted order guarantees they are also `< budget`.

6. **Return the final answer.**  
   When the loop terminates, the search space is exhausted. If `answer != -1`, it is the smallest valid index. Otherwise, no element satisfies the condition.

This works because each step preserves the boundary between impossible and still-possible answer regions.

## 📊 Worked Example
Example: `tiers = [20, 35, 50, 50, 80]`, `budget = 50`

| Step | left | right | mid | tiers[mid] | Action | answer |
|------|------|-------|-----|------------|--------|--------|
| 1 | 0 | 4 | 2 | 50 | valid, search left | 2 |
| 2 | 0 | 1 | 0 | 20 | too small, search right | 2 |
| 3 | 1 | 1 | 1 | 35 | too small, search right | 2 |

Loop ends when `left = 2` and `right = 1`.

Trace interpretation:
1. Index `2` is a valid candidate because `50 >= 50`.
2. We do **not** stop there, because duplicates or earlier qualifying values could exist.
3. The left half contains only `20` and `35`, both smaller than `50`.
4. Therefore index `2` is confirmed as the first tier meeting the budget.

Final result: `2`.

## ⏱ Complexity Analysis
### Time Complexity
`O(log n)`. Each iteration halves the remaining search interval, and the dominant work per iteration is a constant-time comparison plus pointer updates. At `10^6` elements, this is about 20 steps; at `10^9`, about 30. That predictability is why binary search remains a core primitive in latency-sensitive systems.

### Space Complexity
`O(1)`. The algorithm uses only a few scalar variables: boundaries, midpoint, and the candidate answer. No auxiliary data structure is required. Space cannot be meaningfully reduced further without changing the execution model; the main trade-off is iterative versus recursive implementation, where recursion would add call-stack overhead.

## 💡 Key Takeaways
- If the input is sorted and the requirement is “first index where condition becomes true,” think **lower bound**, not generic search.
- Duplicates are a strong signal that “find any match” is insufficient; you are usually searching for a boundary.
- When `tiers[mid] >= budget`, do not return immediately — record `mid` and continue left.
- Be precise about loop conditions and updates: `left <= right`, then move `right = mid - 1` or `left = mid + 1` to avoid infinite loops and missed indices.
- In production code, boundary-search primitives are often more reusable than exact-match lookups because many systems care about threshold crossing, insertion points, and first-applicable policy selection.

## 🚀 Variations & Further Practice
- Return the insertion index instead of `-1` when no value qualifies; the twist is distinguishing API semantics between “not found” and “append position.”
- Find the last index where `tiers[i] <= budget`; same pattern, but now you are computing an upper-bound-style boundary.
- Search in a rotated sorted array or a 2D sorted matrix; the harder twist is that the monotonic structure is weaker, so the search invariant becomes more subtle.