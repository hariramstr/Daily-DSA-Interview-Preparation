# Maximum Safe Gap for Festival Stage Placement

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Binary Search &nbsp;|&nbsp; **Tags:** Binary Search, Greedy, Array

---

## 🗂 Problem Overview
Given `n` distinct approved positions on a walkway and an integer `k`, place exactly `k` stages so that the minimum distance between any two consecutive chosen positions is as large as possible. Return that maximum achievable minimum gap. Positions are unsorted, can be large, and `n` is up to `10^5`, so brute-force subset selection is infeasible. The key observation is monotonicity: if gap `d` is feasible, every smaller gap is also feasible.

## 🌍 Engineering Impact
This pattern shows up whenever you optimize a worst-case separation under discrete placement constraints. Examples include placing replicas across failure domains, scheduling jobs with cooldown windows, allocating wireless channels with interference spacing, and choosing shard boundaries or cache warmup checkpoints across ordered keyspaces. At scale, exhaustive search collapses immediately because the candidate configuration space is combinatorial. Recognizing a monotonic feasibility predicate lets you replace search over arrangements with search over the answer space. That changes the architecture from “enumerate possibilities” to “validate a threshold,” which is often the only tractable path in large distributed or latency-sensitive systems.

## 🔍 Problem Statement
You are given an array `positions` of `n` distinct integers, where `positions[i]` is an approved stage location, and an integer `k` with `2 <= k <= n`. You must place exactly `k` stages, using only these approved spots, with at most one stage per spot.

The score of a placement is the minimum gap between consecutive chosen positions after sorting them. Return the maximum possible score.

Constraints:
- `2 <= n <= 10^5`
- `0 <= positions[i] <= 10^9`
- All `positions[i]` are distinct

Examples:
- `positions = [2, 11, 6, 17, 25], k = 3` → `9`
- `positions = [1, 3, 4, 8, 13, 17], k = 4` → `4`

The decisive constraint is `n = 10^5`: this rules out combinatorial placement search and points toward an `O(n log M)` strategy, where `M` is the position range.

## 🪜 How to Solve This
1. Sort the positions → distance constraints only make sense on ordered coordinates. Once sorted, every valid placement is just a subsequence.
2. Reframe the optimization goal → instead of directly maximizing the minimum gap, ask: “Can I place `k` stages so every adjacent gap is at least `d`?”
3. Notice monotonicity → if gap `d` works, then any smaller gap also works. If `d` fails, any larger gap must fail. That is the exact signal for binary search on the answer.
4. Build a feasibility check → greedily place the first stage at the leftmost position, then always place the next stage at the earliest position at least `d` away from the last one.
5. Why greedy works → choosing the earliest feasible spot preserves maximum room for remaining placements. Delaying a placement never helps fit more stages.
6. Binary search the largest feasible `d` over `[1, maxPosition - minPosition]`.
7. Return the best feasible value found.

This is the standard “binary search on answer + greedy validator” pattern.

## 🧩 Algorithm Walkthrough
1. **Sort the array**  
   Let `positions` be sorted ascending. This converts the problem into selecting an ordered subsequence. The invariant is that all future distance checks are local and monotone in index order.

2. **Define the decision problem**  
   For a candidate minimum gap `d`, ask whether it is possible to place `k` stages such that each chosen position is at least `d` away from the previous chosen one. This is the monotonic predicate needed for **Binary Search on Answer**.

3. **Implement feasibility with a greedy scan**  
   Place the first stage at `positions[0]`. Then scan left to right and place another stage whenever `positions[i] - lastPlaced >= d`. Count placements until either `k` stages are placed or the array ends.  
   Why correct: among all valid choices for the next stage, the earliest feasible one leaves the most remaining space. The invariant is that after placing `t` stages greedily, no other strategy can have placed `t` stages earlier while preserving more room for the rest.

4. **Binary search the gap**  
   Search `d` in the integer range `[1, positions[n-1] - positions[0]]`. For midpoint `mid`, run the greedy feasibility check.  
   - If feasible, record `mid` and search higher.
   - If infeasible, search lower.

5. **Return the maximum feasible gap**  
   Binary search terminates with the largest `d` for which the predicate holds. This is correct because the feasible set is a prefix of the search space.

## 📊 Worked Example
Example: `positions = [2, 11, 6, 17, 25], k = 3`

Sorted positions: `[2, 6, 11, 17, 25]`

| Binary Search `d` | Greedy Placement Trace | Stages Placed | Feasible |
|---|---|---:|---|
| 12 | `2 -> 17` | 2 | No |
| 6  | `2 -> 11 -> 17` | 3 | Yes |
| 9  | `2 -> 11 -> 25` | 3 | Yes |
| 10 | `2 -> 17` | 2 | No |

Trace details:
1. Try `d = 12`: after placing at `2`, the next valid spot is `17`; no third placement exists.
2. Try `d = 6`: `2`, then `11`, then `17` works.
3. Try `d = 9`: `2`, then `11`, then `25` works.
4. Try `d = 10`: `2`, then `17`; `25` is only `8` away, so it fails.

Largest feasible gap is `9`.

## ⏱ Complexity Analysis
### Time Complexity
Sorting costs `O(n log n)`. Each binary-search step runs a linear greedy scan in `O(n)`, and there are `O(log M)` steps where `M = max(positions) - min(positions)`. Total complexity is `O(n log n + n log M)`. This is practical at `10^5` scale; at `10^6`, it is still viable in optimized environments. Anything quadratic is dead on arrival, and combinatorial search is irrelevant long before `10^9`-range coordinates matter.

### Space Complexity
Space is `O(1)` auxiliary beyond the sort, or `O(log n)` / `O(n)` depending on the sorting implementation. The greedy feasibility check is constant-space. You could avoid extra allocation entirely with in-place sorting, trading off mutation of the input array.

## 💡 Key Takeaways
- If the problem asks for the **maximum minimum** value and feasibility for a threshold is monotone, think **binary search on the answer**.
- If candidate solutions are chosen from sorted positions and only local spacing matters, a **greedy left-to-right validator** is usually the right feasibility check.
- Be careful with binary-search bounds: search over distances, not indices, and return the largest feasible value, not the last midpoint tested.
- The feasibility check must use `>= d`, not `> d`; that off-by-one changes valid placements and silently underestimates the answer.
- In production systems, converting optimization into threshold validation is often the difference between an intractable planner and a scalable control loop.

## 🚀 Variations & Further Practice
- **Aggressive Cows / Magnetic Force Between Two Balls**: same core pattern; the challenge is recognizing that the story changes but the monotonic predicate does not.
- **Place facilities on a line with forbidden intervals or weighted demand**: feasibility is no longer a simple greedy scan, forcing a stronger validator or dynamic programming.
- **Maximize minimum distance in 2D or on graphs**: the answer-space search may remain, but feasibility becomes substantially harder because ordering and one-dimensional greediness disappear.