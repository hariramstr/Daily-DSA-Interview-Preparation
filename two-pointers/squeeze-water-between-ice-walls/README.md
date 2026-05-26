# Squeeze Water Between Ice Walls

**Difficulty:** Easy &nbsp;|&nbsp; **Topic:** Two Pointers &nbsp;|&nbsp; **Tags:** Two Pointers, Array, Greedy

---

## 🗂 Problem Overview

Given an array of wall heights, find the pair of indices `(i, j)` that maximizes `min(heights[i], heights[j]) * (j - i)`. The tension is that wider pairs sacrifice height and taller pairs sacrifice width — you can't optimize both simultaneously. The non-trivial constraint is that a brute-force O(n²) scan of all pairs is too slow for `n = 10^5`, forcing you to prove that a linear scan with intelligent pointer movement never misses the optimal pair.

---

## 🌍 Engineering Impact

This exact trade-off — maximizing the product of a limiting dimension and a span — appears across systems design. Sliding-window rate limiters balance token bucket depth against window width. Video transcoding pipelines select the bitrate-resolution pair that maximizes perceived quality within a bandwidth cap. In search ranking, relevance-score × recency-decay optimizations mirror the same bounded-product structure. Without a linear strategy, any of these degrade to O(n²) candidate evaluation at scale, which collapses throughput under production load and makes real-time SLA guarantees impossible.

---

## 🔍 Problem Statement

**Input:** Integer array `heights` of length `n`, where `heights[i]` is the height of the wall at position `i`.

**Output:** Maximum water volume across all valid pairs `(i, j)` with `i < j`, computed as `min(heights[i], heights[j]) * (j - i)`.

**Constraints:** `2 <= n <= 10^5`, `1 <= heights[i] <= 10^4`.

**Examples:**

| Input | Output | Winning Pair | Calculation |
|---|---|---|---|
| `[2, 7, 4, 1, 6, 3]` | `18` | indices (1, 4) | `min(7,6) * 3 = 18` |
| `[3, 1, 2, 4, 5]` | `12` | indices (0, 4) | `min(3,5) * 4 = 12` |

**Key edge case:** The optimal pair is not necessarily the two tallest walls — distance is a first-class factor. The constraint that drives the algorithmic choice is that every inward pointer move shrinks `(j - i)`, so you must only move when the current wall cannot possibly contribute to a better answer.

---

## 🪜 How to Solve This

1. **Read the formula** → `min(heights[i], heights[j]) * (j - i)`. Two competing factors. Maximizing one tends to hurt the other.

2. **Brute force is obvious** → check all `O(n²)` pairs. Works, but too slow at `n = 10^5`. Need to eliminate candidates without evaluating them.

3. **Start at maximum width** → place pointers at both ends. This is the widest possible span. Any move inward strictly reduces `(j - i)`.

4. **Ask: which pointer should move?** → If you move the taller pointer inward, the width shrinks *and* the height can only stay the same or decrease (the shorter wall still caps it). That's strictly worse or equal. Moving the shorter pointer inward at least gives the new pair a chance at a taller limiting wall.

5. **This is the greedy invariant** → always advance the pointer at the shorter wall. You provably discard no candidate that could beat the current best.

6. **One pass, O(n)** → the two pointers meet in the middle after exactly `n - 1` moves. Track the running maximum throughout.

The insight crystallizes: moving the taller pointer is never strictly beneficial, so it's always safe to move the shorter one.

---

## 🧩 Algorithm Walkthrough

**Pattern:** Two Pointers (converging from both ends).

This is the right abstraction because the search space is a triangular matrix of `(i, j)` pairs. Two pointers let you navigate that matrix in a single linear sweep by exploiting a monotonic elimination property: once you determine a pointer cannot improve the current best, you advance it permanently.

**Steps:**

1. **Initialize** `left = 0`, `right = n - 1`, `max_water = 0`. The pointers define the current candidate pair.

2. **Loop while `left < right`:** compute `water = min(heights[left], heights[right]) * (right - left)`. Update `max_water` if `water` is larger.

3. **Advance the shorter pointer:** if `heights[left] <= heights[right]`, increment `left`; otherwise decrement `right`. Ties can go either direction — both are safe.

4. **Why this is correct:** the invariant is that when you move a pointer, every pair that pointer could form with anything between it and the opposite pointer is dominated. The limiting wall is the shorter one; keeping it stationary while moving the taller pointer can only reduce width without improving the height cap. Therefore, all those skipped pairs are guaranteed non-optimal.

5. **Termination:** pointers meet after `n - 1` iterations. Return `max_water`.

No auxiliary data structures are needed. The algorithm is purely index arithmetic and comparisons.

---

## 📊 Worked Example

Input: `heights = [2, 7, 4, 1, 6, 3]`

| Step | left | right | heights[left] | heights[right] | water | max_water | Move |
|------|------|-------|---------------|----------------|-------|-----------|------|
| 1 | 0 | 5 | 2 | 3 | 2×5=10 | 10 | left++ |
| 2 | 1 | 5 | 7 | 3 | 3×4=12 | 12 | right-- |
| 3 | 1 | 4 | 7 | 6 | 6×3=18 | **18** | right-- |
| 4 | 1 | 3 | 7 | 1 | 1×2=2 | 18 | right-- |
| 5 | 1 | 2 | 7 | 4 | 4×1=4 | 18 | right-- |

Pointers meet → return `18`.

---

## ⏱ Complexity Analysis

### Time Complexity

**O(n).** Each pointer moves inward exactly once; the two pointers together traverse `n - 1` steps total. At `n = 10^6` this is a single fast linear scan. At `n = 10^9` it remains tractable in memory-mapped or streaming contexts where O(n log n) would already be marginal.

### Space Complexity

**O(1).** Only three scalar variables (`left`, `right`, `max_water`) are maintained regardless of input size. No auxiliary arrays, stacks, or hash maps. There is no meaningful space/time trade-off available here — the algorithm is already optimal on both dimensions.

---

## 💡 Key Takeaways

- **Pattern signal #1:** When the objective is a product of two factors pulled from opposite ends of an array and those factors trade off against each other, converging two pointers is the canonical approach — not sorting, not a hash map.
- **Pattern signal #2:** If a brute-force solution scans all pairs in a 1D array and the problem has a monotonic elimination property (moving one pointer can never help), two pointers will reduce it to O(n).
- **Implementation gotcha:** The termination condition is `left < right`, not `left <= right`. When they're equal, there's no valid pair — including that step computes a zero-width container with `water = 0`, which is harmless but signals a logic error in reasoning.
- **Implementation gotcha:** On a height tie (`heights[left] == heights[right]`), moving either pointer is correct. Moving both simultaneously is a bug — you skip a valid candidate pair at the next step.
- **Architectural insight:** The greedy "discard the weaker constraint" heuristic transfers directly to resource allocation problems — when optimizing a throughput metric bounded by two competing limits (bandwidth × latency, core count × memory bandwidth), always invest in improving the binding constraint first.

---

## 🚀 Variations & Further Practice

- **Trapping Rain Water (LeetCode 42):** Instead of choosing one optimal pair, compute the total water trapped across *all* positions simultaneously. The conceptual twist is that every cell's contribution depends on the maximum wall to its left *and* right, requiring either a two-pass precomputation or a more nuanced two-pointer invariant that tracks running maximums on both sides.
- **Container With Most Water — 3D variant:** Extend to a 2D grid of heights and find the maximum volume enclosed by four walls. The elimination argument no longer applies cleanly; this typically requires a min-heap or priority-queue-based approach, pushing the complexity to O(n² log n) and testing whether you can adapt a 1D greedy insight to higher dimensions.
- **Maximum Width Ramp (LeetCode 962):** Find the maximum `j - i` such that `heights[i] <= heights[j]`. The distance factor is now the sole objective, but the monotonic stack precomputation required to eliminate candidates efficiently uses the same "discard dominated elements" reasoning that underpins the two-pointer greedy here.