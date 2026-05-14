# Maximum Flavor Score in a Tasting Window

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Sliding Window &nbsp;|&nbsp; **Tags:** Sliding Window, Monotonic Deque, Array

---

## 🗂 What Is This Problem? *(For Everyone)*

A food critic tastes dishes one by one in a line. They can only judge a group of consecutive dishes at a time, but there is a rule: the tastiest and the least tasty dish in the group cannot differ by more than a set amount. The goal is to find the largest group of consecutive dishes that still satisfies this rule.

---

## 🌍 Why Does This Matter in the Real World? *(For Business Readers)*

This type of "bounded window" logic appears throughout modern technology. Streaming platforms like Spotify use it to build playlists where song tempos stay within a comfortable range, keeping listeners engaged longer. Financial analysts apply the same idea to detect stable trading windows, flagging periods where price volatility stays below a risk threshold. Quality control systems in manufacturing use it to identify the longest uninterrupted production run where measurements stay within acceptable tolerances — directly reducing waste, improving yields, and saving costs without slowing down the line.

---

## 🎯 The Challenge in Plain English *(For Everyone)*

Imagine a buffet line of dishes, each rated on taste. A critic walks along and wants to sample the longest consecutive stretch of dishes where no single dish is dramatically better or worse than another — the gap between the best and worst in the group must stay within a comfort zone. Think of it like choosing seats on a train: you want the longest block of adjacent seats where no seat is too far from the aisle or window beyond your preference.

---

## 🔍 Technical Problem Statement *(For Developers)*

Given an integer array `flavors` of length `n` (where `flavors[i]` is the flavor score of dish `i`) and a non-negative integer `t`, return the **maximum length** of any contiguous subarray such that `max(window) - min(window) <= t`.

**Constraints:**
- `1 <= flavors.length <= 10^5`
- `0 <= flavors[i] <= 10^4`
- `0 <= t <= 10^4`

**Example 1:**
```
Input:  flavors = [4, 8, 5, 1, 7, 9, 6], t = 4
Output: 4
Explanation: Window [5, 7, 9, 6] → max=9, min=5, diff=4 ≤ 4, length=4.
```

**Example 2:**
```
Input:  flavors = [10, 10, 10, 10], t = 0
Output: 4
Explanation: All scores equal; difference is always 0. Entire array is valid.
```

---

## 🧩 Approach: How We Solve It *(For Developers)*

We use a **sliding window** with two **monotonic deques** — one tracking the current maximum and one tracking the current minimum — so we can query both in O(1) at any point.

1. **Initialize two deques and two pointers.**
   Set `left = 0`. Create `max_dq` (decreasing order) and `min_dq` (increasing order) to efficiently track the window's max and min without rescanning.

2. **Expand the window by advancing `right`.**
   For each new element `flavors[right]`, update both deques:
   - Remove from the back of `max_dq` any values smaller than the new element (they can never be the max while the new element is present).
   - Remove from the back of `min_dq` any values larger than the new element (same logic for min).
   - Append `right` to both deques.

3. **Check the window constraint.**
   The current max is `flavors[max_dq[0]]` and min is `flavors[min_dq[0]]`. If their difference exceeds `t`, the window is invalid.

4. **Shrink the window from the left.**
   Advance `left` until the constraint is satisfied again. Remove stale indices from the fronts of both deques whenever `max_dq[0] < left` or `min_dq[0] < left`.

5. **Record the maximum valid window length.**
   After each adjustment, update `best = max(best, right - left + 1)`.

6. **Return `best`** after processing all elements.

---

## 📊 Worked Example *(For Developers)*

**Input:** `flavors = [4, 8, 5, 1, 7, 9, 6]`, `t = 4`

| Step | right | left | Window          | max | min | diff | best |
|------|-------|------|-----------------|-----|-----|------|------|
| 1    | 0     | 0    | [4]             | 4   | 4   | 0    | 1    |
| 2    | 1     | 0    | [4, 8]          | 8   | 4   | 4    | 2    |
| 3    | 2     | 0    | [4, 8, 5]       | 8   | 4   | 4    | 3    |
| 4    | 3     | 0    | [4, 8, 5, 1]    | 8   | 1   | 7 ❌ | 3    |
| 4a   | 3     | 3    | [1]             | 1   | 1   | 0    | 3    |
| 5    | 4     | 3    | [1, 7]          | 7   | 1   | 6 ❌ | 3    |
| 5a   | 4     | 4    | [7]             | 7   | 7   | 0    | 3    |
| 6    | 5     | 4    | [7, 9]          | 9   | 7   | 2    | 3    |
| 7    | 6     | 3    | [5, 7, 9, 6] ✅ | 9   | 5   | 4    | **4**|

> At step 7, `left` is rewound because earlier shrinking left room; the deques correctly surface index 2 (`flavors[2]=5`) as the minimum, giving a valid window of length 4.

---

## ⏱ Performance Analysis *(For Developers + Technical Managers)*

### Time Complexity

**O(n)** — Each element is added to and removed from each deque at most once, so the total work across all iterations is proportional to the number of dishes. Doubling the input size doubles the runtime linearly — this scales comfortably to 100,000+ elements without performance concerns.

### Space Complexity

**O(n)** in the worst case — both deques can hold up to `n` indices simultaneously (e.g., a strictly sorted array). In practice the deques stay small. No additional data structures are needed beyond the two deques and a handful of integer variables.

---

## 💡 Key Takeaways *(For Everyone)*

- **Constraint-bounded windows are everywhere in business** — from risk management in finance to quality thresholds in manufacturing; this pattern solves them efficiently.
- **Bigger is not always better** — the algorithm finds the *longest* valid group, helping businesses maximize throughput while staying within acceptable limits.
- **Monotonic deques are the key insight** — maintaining sorted order at the edges of a deque lets you find max and min in O(1) without rescanning the window each time.
- **Two pointers + two deques = O(n)** — what might naively require O(n²) comparisons collapses to linear time by never revisiting discarded elements.
- **Deque indices, not values** — storing *positions* in the deque (not raw values) is critical so you can correctly evict elements that have scrolled out of the window's left boundary.

---

## 🚀 Try It Yourself *(For Developers)*

- **Variation 1 — Minimum window:** Instead of the longest valid window, find the *shortest* contiguous subarray where `max - min >= t`. How does the shrink/expand logic invert?
- **Variation 2 — Sliding window maximum only:** Solve [LeetCode #239 — Sliding Window Maximum](https://leetcode.com/problems/sliding-window-maximum/) to master the single-deque version of this technique before combining both deques.
- **Variation 3 — 2D flavor grid:** Extend the problem to a matrix where you must find the largest rectangular sub-grid satisfying the same `max - min <= t` constraint — a common interview follow-up that tests whether you truly understand the deque mechanics.

---