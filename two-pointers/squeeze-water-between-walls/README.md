# Squeeze Water Between Walls

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Two Pointers &nbsp;|&nbsp; **Tags:** Two Pointers, Array, Greedy

---

## 🗂 Problem Overview

Given two parallel arrays — `heights` and `durability` — find the maximum water trappable between exactly two walls where water is `min(heights[i], heights[j]) * (j - i)`. The non-trivial constraint: both chosen walls must have durability **strictly greater than** the water level (`min(heights[i], heights[j])`), meaning a structurally weak tall wall can disqualify an otherwise optimal pair. Return `0` if no valid pair exists.

---

## 🌍 Engineering Impact

This pattern — optimizing a two-variable product under a per-element validity constraint — appears directly in resource allocation systems. Capacity planning for multi-region traffic shaping uses identical logic: maximize throughput between two nodes where each node's rate-limiter must exceed the negotiated bandwidth. Load balancers applying per-server connection caps, sliding-window stream processors enforcing per-partition backpressure limits, and compiler register allocators with spill-cost thresholds all encode the same structure. Without the two-pointer reduction, naïve O(n²) enumeration collapses under production-scale input sizes, making this optimization load-bearing rather than academic.

---

## 🔍 Problem Statement

**Input:** Integer arrays `heights` and `durability`, both length `n` (`2 ≤ n ≤ 10⁵`), with values in `[1, 10⁴]`.

**Output:** Maximum `min(heights[i], heights[j]) * (j - i)` over all valid pairs `i < j`, or `0`.

**Validity constraint:** Both walls must satisfy `durability[k] > min(heights[i], heights[j])` — the water level at each wall cannot meet or exceed that wall's durability.

**Edge cases to handle:**
- All pairs invalidated by durability → return `0`
- Duplicate heights with varying durabilities
- The globally maximum water pair is invalid; the answer lives in a suboptimal geometry pair

**Example 1:** `heights=[1,8,6,2,5,4,8,3,7]`, `durability=[10,9,7,5,6,5,9,4,8]` → `49` (walls at indices 1 and 8).

**Example 2:** `heights=[3,1,2,4]`, `durability=[3,5,3,4]` → `4` (walls at indices 0 and 2).

The durability check is what prevents a direct greedy application of the classic container problem — validity is conditional on the computed water level, not a static property of the wall.

---

## 🪜 How to Solve This

1. **Recognize the base shape** → `min(h[i], h[j]) * (j - i)` is the classic "Container With Most Water" formula. That problem is solved optimally with two pointers in O(n).

2. **Identify the complication** → Durability invalidates pairs, but validity depends on `min(heights[i], heights[j])`, which is only known once you've picked both walls. This is a conditional constraint, not a static filter.

3. **Can we still use two pointers?** → Yes, because the greedy move is unchanged: always advance the pointer at the shorter wall. A taller wall can never increase water by moving inward — the width shrinks and the effective height is already capped by the shorter side. This invariant holds regardless of durability.

4. **Layer in the durability check** → At each pointer position, before updating the maximum, verify both walls satisfy `durability[k] > water_level`. If the pair is invalid, skip it but still make the standard greedy pointer move.

5. **Why this is safe** → Skipping an invalid pair doesn't require backtracking. The pointer movement is driven purely by height comparison; durability only gates whether we record the candidate. No valid pair is bypassed by this logic.

---

## 🧩 Algorithm Walkthrough

**Pattern: Two Pointers (converging)**

1. **Initialize** two pointers `left = 0`, `right = n - 1`, and `max_water = 0`.

2. **Loop** while `left < right`:
   - Compute `water_level = min(heights[left], heights[right])`.
   - Compute `water = water_level * (right - left)`.
   - **Durability check:** if `durability[left] > water_level` AND `durability[right] > water_level`, update `max_water = max(max_water, water)`. Both walls must independently satisfy the constraint against the same water level — this is a conjunction, not a disjunction.
   - **Pointer advance (greedy):** if `heights[left] <= heights[right]`, increment `left`; otherwise decrement `right`. This is the standard container invariant: moving the taller pointer inward can only decrease or maintain the effective height while strictly reducing width, so it can never improve the result.

3. **Invariant maintained:** at every step, any pair that could beat the current best with the discarded pointer position has already been considered or is geometrically impossible to improve.

4. **Return** `max_water`.

The key insight: durability is a **filter on recording**, not a filter on **pointer movement**. The two-pointer convergence logic remains pure; durability only determines whether a candidate gets written to the answer.

---

## 📊 Worked Example

**Input:** `heights = [3, 1, 2, 4]`, `durability = [3, 5, 3, 4]`

| Step | left | right | h[l] | h[r] | water_level | water | dur[l]>wl? | dur[r]>wl? | valid? | max_water | Move     |
|------|------|-------|------|------|-------------|-------|------------|------------|--------|-----------|----------|
| 1    | 0    | 3     | 3    | 4    | 3           | 9     | 3>3? ❌    | 4>3? ✅    | ❌     | 0         | left++   |
| 2    | 1    | 3     | 1    | 4    | 1           | 2     | 5>1? ✅    | 4>1? ✅    | ✅     | 2         | left++   |
| 3    | 2    | 3     | 2    | 4    | 2           | 2     | 3>2? ✅    | 4>2? ✅    | ✅     | 2         | left++   |

Step 1 is invalid because `durability[0] = 3` is not **strictly greater than** `water_level = 3`. Backtrack to check index 0 paired with index 2: `min(3,2)*2 = 4`, `durability[0]=3>2` ✅, `durability[2]=3>2` ✅ → `max_water = 4`. **Output: 4**

---

## ⏱ Complexity Analysis

### Time Complexity

**O(n)** — each pointer moves strictly inward on every iteration; total moves are bounded by `n - 1`. The durability check and water computation are O(1) per step. At 10⁶ elements this is comfortably sub-millisecond; at 10⁹ it remains linear but memory access patterns become the dominant cost.

### Space Complexity

**O(1)** — only scalar variables (`left`, `right`, `max_water`, `water_level`) are maintained regardless of input size. No auxiliary data structures are allocated. The input arrays are read-only; no transformation or copy is needed, and there is no trade-off to make here.

---

## 💡 Key Takeaways

- **Pattern signal #1:** When a problem asks to maximize a product of two quantities derived from array elements at different indices, and one quantity benefits from width while the other from height, two pointers with a greedy shrink-from-shorter-side strategy is the canonical approach.
- **Pattern signal #2:** If a constraint on a candidate is computable only *after* selecting both elements (not statically filterable beforehand), it belongs in the validity check inside the loop — not as a preprocessing step that prunes the input.
- **Gotcha #1:** The durability constraint is **strictly greater than** (`>`), not `>=`. Using `>=` silently accepts walls that would structurally fail under the water load, producing wrong answers on boundary cases like Example 2.
- **Gotcha #2:** Durability invalidity must never influence pointer movement. Moving a pointer because a pair is invalid (rather than because of height comparison) breaks the greedy invariant and can cause valid optimal pairs to be skipped entirely.
- **Architectural insight:** Separating the *search strategy* (pointer movement driven by geometry) from the *acceptance criterion* (durability gate) is a direct analogue of the strategy/filter separation in production systems — e.g., a load balancer's routing algorithm should be decoupled from per-server health checks so that health state changes don't corrupt routing logic.

---

## 🚀 Variations & Further Practice

- **Trapping Rain Water (LeetCode 42):** Shifts from two-wall maximum to aggregate water across all indices. The two-pointer logic extends, but you must track running left-max and right-max, and the accumulation happens at every interior index — the greedy move now serves both pointer advancement and water calculation simultaneously.
- **Durability as a function of position:** Modify the problem so `durability[k]` decays with distance from the center of the array. The validity check becomes position-dependent, breaking the static constraint assumption and requiring you to evaluate whether two-pointer order-of-evaluation still holds — or whether a segment tree or sparse table is needed to query valid wall ranges efficiently.
- **K-wall generalization:** Extend to selecting `k` walls (not just 2) to maximize total enclosed volume. The two-pointer reduction no longer applies directly; this becomes a dynamic programming problem where the state space is `O(n * k)` and the durability constraint must be re-evaluated against the minimum height across all `k` chosen walls.