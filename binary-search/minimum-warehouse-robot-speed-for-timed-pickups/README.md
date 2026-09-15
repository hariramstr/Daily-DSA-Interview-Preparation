# Minimum Warehouse Robot Speed for Timed Pickups

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Binary Search &nbsp;|&nbsp; **Tags:** Binary Search, Monotonic Predicate, Arrays

---

## 🗂 Problem Overview
Given two equal-length arrays, `distances` and `deadlines`, find the smallest positive integer speed `s` such that traversing aisles in order finishes each prefix by its corresponding deadline. The completion time for aisle `i` is the cumulative sum of `distances[j] / s` for `0..i`. Return that minimum speed, or `-1` if no integer speed works. The non-trivial part is that feasibility depends on every cumulative prefix, not just the total route time.

## 🌍 Engineering Impact
This pattern shows up anywhere you need the minimum provisioned capacity that satisfies ordered latency or throughput SLOs: streaming pipelines meeting stage deadlines, batch schedulers sizing workers for chained jobs, network shaping under cumulative delay budgets, and robotics or fulfillment systems with sequential service constraints. At scale, brute-force search over capacities is operationally useless because the search space is large and checks may already be expensive. The monotonic-feasibility framing turns “capacity planning under deadlines” into a predictable `O(n log U)` decision process, which is exactly the kind of structure that enables safe autoscaling, admission control, and deterministic planning.

## 🔍 Problem Statement
You are given `distances[i]`, the length of aisle `i`, and `deadlines[i]`, the latest allowed cumulative completion time for aisle `i`. A robot traverses aisles strictly in order at one constant integer speed `s > 0`. The cumulative time after aisle `i` is:

`distances[0] / s + distances[1] / s + ... + distances[i] / s`

The robot is valid only if this cumulative time is `<= deadlines[i]` for every index `i`. Return the minimum such integer speed, or `-1` if none exists.

Constraints:
- `1 <= n <= 100000`
- `distances.length == deadlines.length == n`
- `1 <= distances[i] <= 1000000`
- `1 <= deadlines[i] <= 1000000000`

Examples:
- `distances = [4,3,6], deadlines = [2,4,7]` → `2`
- `distances = [5,8,4], deadlines = [1,2,3]` → `-1`

The key algorithmic driver is input size: `10^5` elements rules out linear scanning over candidate speeds.

## 🪜 How to Solve This
1. Read the problem → notice the output is not a schedule or path, just the **minimum integer speed** that makes all constraints pass.

2. That usually suggests a **decision-and-search** split:
   - “If speed `s` were given, can I check whether it works?”
   - If yes, search for the smallest such `s`.

3. Build the feasibility check:
   - Traverse aisles in order.
   - Maintain cumulative time.
   - After each aisle, compare cumulative time against `deadlines[i]`.
   - If any prefix misses, `s` fails immediately.

4. Ask whether feasibility is monotonic:
   - Increasing `s` decreases every term `distances[i] / s`.
   - So if speed `s` works, every larger speed also works.
   - That gives a classic binary-search predicate.

5. Now define the search space:
   - Lower bound is `1`.
   - Upper bound can be found by doubling until a feasible speed appears, or conclude impossible if even arbitrarily high speed cannot satisfy the prefix structure.

6. Binary search the first feasible speed.
   - This is the standard “leftmost true” pattern over a monotonic boolean function.

## 🧩 Algorithm Walkthrough
1. **Pre-check impossibility from the limit behavior.**  
   Even as `s -> infinity`, each aisle still takes positive time, so the cumulative time after aisle `i` approaches `0` from above, but the route still must complete aisles sequentially. Practically, with this formulation using real division, arbitrarily large speed can make times arbitrarily small, so impossibility comes from deadlines that cannot be met under any positive integer speed found within search bounds. The algorithm therefore relies on bounded search plus feasibility, not a separate combinatorial shortcut.

2. **Define the monotonic predicate `canFinish(s)`.**  
   Pattern: **Binary Search on Answer** with a **Monotonic Predicate**.  
   For a candidate speed `s`, iterate once through the arrays, accumulate `time += distances[i] / s`, and fail immediately if `time > deadlines[i]`.  
   Invariant: after processing index `i`, `time` equals the exact cumulative completion time at speed `s`.

3. **Find a feasible upper bound.**  
   Start with `hi = 1` and repeatedly double it until `canFinish(hi)` is true, or until `hi` exceeds a safe cap such as `1e18`-style logic adapted to the language.  
   Why this works: if any integer speed is feasible, monotonicity guarantees some sufficiently large bound will be feasible.

4. **Binary search for the first feasible speed.**  
   Maintain `[lo, hi]` such that `lo` is infeasible and `hi` is feasible, or use inclusive bounds with the standard leftmost-true update:
   - `mid = lo + (hi - lo) / 2`
   - if `canFinish(mid)`, move `hi = mid`
   - else move `lo = mid + 1`

5. **Return the result or `-1`.**  
   If no feasible upper bound is found, return `-1`; otherwise return `lo`.  
   Correctness follows from the invariant that the feasible region is a suffix of the positive integers.

## 📊 Worked Example
Use `distances = [4, 3, 6]`, `deadlines = [2, 4, 7]`.

| Speed `s` | Aisle | Added Time | Cumulative Time | Deadline | Pass? |
|---|---:|---:|---:|---:|---:|
| 1 | 0 | 4.0 | 4.0 | 2 | No |
| 2 | 0 | 2.0 | 2.0 | 2 | Yes |
| 2 | 1 | 1.5 | 3.5 | 4 | Yes |
| 2 | 2 | 3.0 | 6.5 | 7 | Yes |

Binary search flow:
1. `s = 1` fails, so answer is greater than `1`.
2. `s = 2` succeeds, so `2` is a valid upper bound.
3. Search range collapses immediately to `2`.

Result: `2`.

The important observation is not the arithmetic itself; it is that once `2` works, every `s > 2` also works, which is exactly the monotonic structure binary search needs.

## ⏱ Complexity Analysis

### Time Complexity
`O(n log U)`, where `n` is the number of aisles and `U` is the smallest feasible upper bound explored by binary search. Each feasibility check is a single pass over the arrays. At `10^6` elements this remains practical; at `10^9`, even one pass is the real bottleneck, not the logarithmic search factor.

### Space Complexity
`O(1)` auxiliary space. The algorithm stores only a few scalars: bounds, midpoint, and cumulative time. Space cannot be meaningfully reduced further without changing the input model; the trade-off is numerical care, not memory structure.

## 💡 Key Takeaways
- If the problem asks for the **minimum integer value** that makes a verification procedure pass, check whether it can be reframed as binary search on the answer.
- If “works for `x`” implies “works for any larger `x`,” you almost certainly have a monotonic predicate worth exploiting.
- Use early exit in `canFinish(s)`; once one deadline is missed, continuing the scan only wastes time.
- Be careful with numeric precision in cumulative division; use `double`/`float64` deliberately and compare against deadlines consistently.
- The transferable design insight is to separate **feasibility evaluation** from **parameter search**, which is the core move behind scalable capacity sizing and SLO-driven provisioning.

## 🚀 Variations & Further Practice
- Add per-aisle setup times or mandatory pauses between aisles. The predicate remains monotonic, but the feasibility function now mixes fixed and speed-dependent costs.
- Allow speed changes per aisle with a total energy budget. This stops being simple binary search and turns into optimization with resource allocation constraints.
- Replace exact real division with rounded-up traversal times per aisle. The monotonic pattern survives, but the arithmetic and edge-case behavior become much more discrete.