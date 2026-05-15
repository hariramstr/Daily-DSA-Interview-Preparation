# Pair Sum Closest to Target After Removing One Element

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Two Pointers &nbsp;|&nbsp; **Tags:** Two Pointers, Array, Greedy

---

## 🗂 What Is This Problem? *(For Everyone)*

You have a sorted list of numbers and a goal number (the "target"). Your job is to pick the best possible pair of numbers from the list that add up as close to the target as possible — but first, you must cross out exactly one number from the list. The puzzle is figuring out *which* number to remove so your remaining pair gets as close to the target as it can.

---

## 🌍 Why Does This Matter in the Real World? *(For Business Readers)*

This kind of "remove one, then optimise" logic appears constantly in business systems. Airlines use similar reasoning when re-routing passengers after a cancellation: drop one flight leg and find the best two-leg combination that still gets travellers closest to their destination on time. Budget planners use it when one cost item is cut and remaining line items must still hit a spending target as closely as possible. Solving this efficiently means faster decisions, lower computational costs, and better outcomes for customers — all without manually testing every possible combination.

---

## 🎯 The Challenge in Plain English *(For Everyone)*

Imagine you have a row of price tags on a shelf, already sorted from cheapest to most expensive. A customer wants two items that together cost as close to $10 as possible. But one price tag is smudged and must be removed before shopping begins. Your job: decide which tag to remove, then find the best two-item combination from what remains. The goal is to get the total as close to $10 as possible.

---

## 🔍 Technical Problem Statement *(For Developers)*

Given a **sorted integer array** `nums` and an integer `target`, remove **exactly one element** (at any index) from `nums`, then find a pair of elements (at two distinct indices) from the remaining array whose sum is closest to `target`. Return the **minimum absolute difference** between any achievable pair sum and `target`.

**Constraints:**
- `3 <= nums.length <= 10^5`
- `-10^5 <= nums[i] <= 10^5`
- `-2 * 10^5 <= target <= 2 * 10^5`
- `nums` is sorted in non-decreasing order
- The array after removal must still contain at least 2 elements

**Examples:**

| Input | Target | Output |
|---|---|---|
| `[1, 3, 5, 8, 12]` | `10` | `0` |
| `[1, 2, 4, 7, 9]` | `6` | `0` |
| `[1, 1, 2, 3]` | `100` | `95` |

For Example 2: remove `9`, leaving `[1, 2, 4, 7]`. The pair `(2, 4)` sums to exactly `6`. Absolute difference = `0`.

---

## 🧩 Approach: How We Solve It *(For Developers)*

The key insight is that we don't need to literally remove each element and re-run a full search from scratch. Instead, we iterate over every possible "removed" index and run a two-pointer pair search on the virtual subarray that skips that index.

**Step-by-step algorithm:**

1. **Iterate over each candidate removal index `k`** from `0` to `n-1`. For each `k`, we conceptually remove `nums[k]` and search the rest.

2. **Set up two pointers** on the remaining array: `left = 0` and `right = n - 1`, skipping index `k` as needed (advance `left` past `k` if `left == k`; retreat `right` past `k` if `right == k`).

3. **Run the classic two-pointer closest-pair search**: compute `sum = nums[left] + nums[right]`. Track the minimum `|sum - target|` seen so far.
   - If `sum < target`, move `left` forward (we need a larger sum).
   - If `sum > target`, move `right` backward (we need a smaller sum).
   - If `sum == target`, return `0` immediately — perfect match found.

4. **Skip the removed index** whenever a pointer lands on `k` during traversal.

5. **Return the global minimum** absolute difference across all `n` removal choices.

This greedy two-pointer scan within each removal pass ensures we never miss the optimal pair for a given removal.

---

## 📊 Worked Example *(For Developers)*

**Input:** `nums = [1, 2, 4, 7, 9]`, `target = 6`, remove index `k = 4` (value `9`)

**Virtual array:** `[1, 2, 4, 7]`

| Step | left (idx) | right (idx) | nums[L] | nums[R] | Sum | \|Sum − 6\| | Action |
|------|-----------|------------|---------|---------|-----|------------|--------|
| 1 | 0 | 3 | 1 | 7 | 8 | 2 | sum > target → right-- |
| 2 | 0 | 2 | 1 | 4 | 5 | 1 | sum < target → left++ |
| 3 | 1 | 2 | 2 | 4 | 6 | **0** | **Exact match → return 0** |

**Result:** `0` ✅

The algorithm finds the perfect pair `(2, 4)` in just three steps after choosing the optimal removal.

---

## ⏱ Performance Analysis *(For Developers + Technical Managers)*

### Time Complexity

**O(n²)** — For each of the `n` possible removals, the two-pointer scan runs in O(n). This means for an array of 100,000 elements, roughly 10 billion operations in the worst case. For very large inputs, further optimisation (e.g., precomputed prefix/suffix best pairs) can reduce this to O(n log n).

### Space Complexity

**O(1)** extra space — The algorithm uses only a handful of pointer variables and a running minimum. No additional arrays or data structures are allocated, regardless of input size. This makes it memory-friendly even on constrained hardware.

---

## 💡 Key Takeaways *(For Everyone)*

- **Real-world decisions often require "remove one option, then optimise"** — this algorithm models that pattern efficiently and can be adapted to scheduling, budgeting, and recommendation systems.
- **Small algorithmic improvements compound at scale** — a solution that avoids brute-force testing of every triple of indices can mean the difference between a responsive app and a frozen one.
- **Two pointers work because the array is sorted** — sorting gives structure that lets us make intelligent directional moves instead of checking all pairs blindly.
- **Skipping the removed index cleanly is the implementation crux** — handling pointer collisions with the removed index correctly is what separates a working solution from a buggy one.
- **Early termination on zero difference is a free win** — if a perfect pair sum is found, no further work is needed; always exploit this in closest-sum problems.

---

## 🚀 Try It Yourself *(For Developers)*

- **Variation 1 — Remove Two Elements:** Extend the problem to remove exactly two elements before finding the closest pair. How does the complexity change, and can two-pointers still be applied effectively?
- **Variation 2 — Closest Triplet Sum:** Instead of a pair, find three elements (after one removal) whose sum is closest to the target. This is a classic extension that combines the removal twist with the 3Sum Closest pattern.
- **Variation 3 — Unsorted Input:** Remove the sorted constraint. What preprocessing step would you add, and how does it affect the overall time complexity guarantee?

---