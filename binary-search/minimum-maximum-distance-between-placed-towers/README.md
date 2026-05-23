# Minimum Maximum Distance Between Placed Towers

**Difficulty:** Hard &nbsp;|&nbsp; **Topic:** Binary Search &nbsp;|&nbsp; **Tags:** Binary Search, Greedy, Array

---

## 🗂 Problem Overview

Given a sorted array of candidate positions and an integer `k`, place exactly `k` towers — anchored at the first and last positions — to minimize the maximum gap between any two consecutively placed towers. The non-trivial constraint: towers must land on candidate positions, not arbitrary points, which eliminates the clean closed-form solution available in the continuous version and forces a search over a discrete feasibility space.

---

## 🌍 Engineering Impact

This pattern governs placement and partitioning decisions across distributed infrastructure. CDN edge-node placement minimizes worst-case latency to any user segment. Database shard boundary selection minimizes the maximum partition size under a fixed shard count. Sensor network deployment along pipelines or highways optimizes coverage under a budget constraint. Without this approach, greedy heuristics produce locally optimal placements that fail at scale — a single oversized gap becomes a hotspot, a coverage blind spot, or an SLA violation that no amount of tuning elsewhere can compensate for.

---

## 🔍 Problem Statement

**Input:** A strictly increasing integer array `positions` of length `n` (2 ≤ k ≤ n ≤ 10⁵, 0 ≤ positions[i] ≤ 10⁹) and an integer `k`.  
**Output:** The minimum possible maximum distance between consecutively placed towers, as a float rounded to 5 decimal places.  
**Rules:** `positions[0]` and `positions[n-1]` always receive a tower. Place the remaining `k - 2` towers at any subset of the other candidates.

**Example 1:** `positions = [0, 5, 10, 15, 20]`, `k = 4` → `10.00000`  
Place at `[0, 5, 15, 20]` or `[0, 10, 15, 20]`; maximum gap is 10.

**Edge case driver:** Because positions are discrete candidates, the answer is always one of the gaps between existing positions (or a sub-gap thereof when a segment is split by an intermediate candidate). This discreteness is what makes binary search over the answer the right lever — feasibility is monotone in the candidate threshold.

---

## 🪜 How to Solve This

1. **Reframe the question** → Instead of "where do I place towers?", ask "is a given maximum gap `d` achievable with at most `k` towers?" This converts an optimization problem into a decision problem.

2. **Recognize monotonicity** → If gap `d` is feasible, any `d' > d` is also feasible (you can always use fewer or equal towers). This monotone feasibility is the binary search trigger.

3. **Define the search space** → The answer lies between `0` and `positions[n-1] - positions[0]`. Since positions are integers, binary search over this integer range with floating-point precision at the end.

4. **Build the feasibility check** → For a candidate max gap `d`, greedily scan left to right. Each time the distance from the last placed tower to the current candidate exceeds `d`, place a tower at the previous candidate. Count towers used; if ≤ `k`, `d` is feasible.

5. **Converge** → Binary search narrows to the smallest feasible `d`. The greedy check is correct because placing a tower as late as possible (rightmost valid candidate before a gap is violated) never wastes capacity — it's the classic interval covering argument.

---

## 🧩 Algorithm Walkthrough

**Pattern: Binary Search on Answer + Greedy Feasibility Check**

This is the right abstraction because the objective (minimize the maximum) has a monotone feasibility structure, and the greedy check runs in O(n), keeping the overall complexity tractable.

**Steps:**

1. **Initialize bounds:** `lo = 0`, `hi = positions[n-1] - positions[0]`. These are the tightest possible bounds on the answer.

2. **Binary search loop:** While `hi - lo > 1e-6` (or for a fixed number of iterations, e.g., 100, to guarantee precision):
   - Compute `mid = (lo + hi) / 2.0`
   - Run feasibility check with `mid` as the maximum allowed gap.

3. **Feasibility check (greedy):** Initialize `tower_count = 1`, `last = positions[0]`. Iterate through positions left to right. When `positions[i] - last > mid`, place a tower at `positions[i]` (update `last = positions[i]`, increment `tower_count`). After the loop, return `tower_count <= k`.
   - **Invariant:** At every step, `last` is the rightmost position where a tower has been placed, and we've used the minimum towers needed to cover up to that point.

4. **Update bounds:** If feasible, `hi = mid` (try smaller); else `lo = mid` (need more room).

5. **Return `round(hi, 5)`:** `hi` converges to the minimum feasible maximum gap.

---

## 📊 Worked Example

`positions = [0, 5, 10, 15, 20]`, `k = 4`

| Iteration | `lo`  | `hi`  | `mid` | Towers Used | Feasible? | Action      |
|-----------|-------|-------|-------|-------------|-----------|-------------|
| 1         | 0     | 20    | 10.0  | 3           | ✅ (≤4)   | `hi = 10.0` |
| 2         | 0     | 10.0  | 5.0   | 5           | ❌ (>4)   | `lo = 5.0`  |
| 3         | 5.0   | 10.0  | 7.5   | 4           | ✅ (≤4)   | `hi = 7.5`  |
| 4         | 5.0   | 7.5   | 6.25  | 4           | ✅ (≤4)   | `hi = 6.25` |
| ...       | →     | →     | →     | converges   | →         | `hi → 10.0` |

After sufficient iterations, `hi` converges to `10.00000`. At `mid = 10.0`, towers at `[0, 10, 20]` use 3 ≤ 4 — feasible. The search correctly identifies 10.0 as the minimum achievable maximum gap.

---

## ⏱ Complexity Analysis

### Time Complexity

**O(n log(max\_gap / ε))** — The binary search runs ~50 iterations for 1e-9 precision over a 10⁹ range (log₂(10⁹/1e-6) ≈ 50), and each feasibility check is O(n). At n = 10⁵, this is roughly 5×10⁶ operations — well within real-time constraints. At n = 10⁶ it remains practical; at n = 10⁹ you'd need a streaming or sampled approach.

### Space Complexity

**O(1)** auxiliary space — the feasibility check is a single linear scan with scalar state (`last`, `tower_count`). No additional data structures are allocated regardless of input size. The input array itself is read-only; no reduction trade-off exists here.

---

## 💡 Key Takeaways

- **Pattern signal — optimization over a monotone feasibility space:** Whenever the problem asks "minimize the maximum" or "maximize the minimum" and you can write a cheap `canAchieve(threshold)` predicate, binary search on the answer is almost certainly the intended approach.
- **Pattern signal — discrete candidates with a continuous-feeling answer:** When the answer is a distance or ratio derived from integer inputs but returned as a float, the search space is continuous but bounded by integer structure — binary search with floating-point `lo/hi` and an iteration count (rather than `lo < hi`) is cleaner than integer bisection.
- **Implementation gotcha — greedy direction matters:** The feasibility check must place towers as *late* as possible (rightmost valid candidate), not as early as possible. Placing early wastes coverage and overcounts required towers, producing false negatives.
- **Implementation gotcha — termination condition:** Using `while hi - lo > 1e-6` can loop indefinitely near the boundary due to floating-point rounding. Prefer a fixed iteration count (50–100) which guarantees O(log(1/ε)) convergence without risk of infinite loops.
- **Architectural insight:** This "binary search on the answer" pattern is the algorithmic equivalent of capacity planning via load testing — instead of solving the placement directly, you probe whether a target SLA (max gap) is satisfiable, then binary search to the tightest achievable SLA. The same structure applies to rate limiter threshold tuning, replica placement, and partition sizing in distributed systems.

---

## 🚀 Variations & Further Practice

- **Continuous placement (no candidate restriction):** If towers can be placed anywhere on the road (not just at candidates), the answer is `(positions[n-1] - positions[0]) / (k - 1)` — a closed form. The interesting twist is proving why the discrete version can't use this formula and quantifying the gap between the continuous optimum and the discrete-constrained answer.
- **Minimize maximum gap with weighted segments:** Each segment between consecutive candidates has a traversal cost; placing a tower in a segment reduces cost proportionally. This breaks the simple greedy feasibility check — you need a cost-aware scan or a priority-queue-based approach, pushing the complexity toward O(n log n) per feasibility check.
- **K-server / multi-dimensional placement:** Extend to a 2D grid where towers must cover circular regions and candidate positions are graph nodes. The feasibility check becomes a geometric coverage problem, and binary search on the answer still applies but the inner check requires a greedy interval-cover on sorted projections — a direct extension of this pattern into computational geometry.