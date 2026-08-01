# Minimum Heater Radius for Circular Warehouses

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Binary Search &nbsp;|&nbsp; **Tags:** Binary Search, Greedy, Circular Array

---

## 🗂 Problem Overview
Given sorted unique warehouse positions on a circular road of length `L`, choose at most `k` warehouse locations to place heaters. Each heater covers all warehouses within circular distance `R`, where distance is the shorter path around the ring. Return the minimum integer `R` that covers every warehouse. The challenge is the circular wrap-around and the large input size: brute-force placement is infeasible, so the solution must combine binary search on `R` with an efficient feasibility test.

## 🌍 Engineering Impact
This pattern shows up anywhere a system must choose a minimal service radius under placement constraints: edge cache placement on metro fiber rings, cellular small-cell activation on circular transport corridors, maintenance depot coverage on beltways, and token or leader placement on logical ring topologies. At scale, naive search over placements explodes combinatorially, and ignoring circular topology produces systematically wrong boundary behavior. Binary-searching the service threshold while using a monotone feasibility check turns an intractable planning problem into a predictable control loop, which is exactly the kind of transformation that keeps capacity planning, rollout simulation, and topology-aware optimization operationally cheap.

## 🔍 Problem Statement
You are given:

- `warehouses`: a sorted array of `n` unique integers in `[0, L - 1]`
- `L`: the total circumference of the circular road
- `k`: the maximum number of heaters allowed

A heater may be installed only at an existing warehouse position. A heater with radius `R` covers any warehouse whose circular distance from the heater is at most `R`; on a ring, that distance is the minimum of clockwise and counterclockwise travel.

Return the minimum integer radius `R` such that all warehouses are covered using at most `k` heaters.

Constraints:

- `1 <= k <= n <= 2 * 10^5`
- `1 <= L <= 10^9`
- `warehouses` is strictly increasing
- answer fits in 32-bit signed integer

Examples:

- `warehouses = [1, 4, 8, 11], L = 12, k = 2` → `2`
- `warehouses = [2, 6, 9, 14], L = 20, k = 1` → `6`

The key constraint is `n = 2e5`: any approach that tries many placements directly is dead on arrival.

## 🪜 How to Solve This
1. Read the problem → the output is a minimum feasible radius, not a placement itself. That is a strong signal for **binary search on the answer**.

2. Ask whether feasibility is monotonic → if radius `R` works, any larger radius also works. That gives the required monotone predicate.

3. Reduce the circular complication → duplicate the warehouse array as `w[i + n] = w[i] + L`. Now every wrap-around segment on the circle becomes a contiguous interval in a linear array.

4. For a fixed `R`, think greedily → if you start from some warehouse and move clockwise, the best single heater is the one placed as far right as possible while still covering the current leftmost uncovered warehouse. That maximizes how far the same heater can extend coverage.

5. Precompute jumps → from each starting index, determine the first index not covered by one optimal heater. Repeating that jump `k` times tells you how many warehouses can be covered.

6. Because the start point on a circle is arbitrary, test every possible cut implicitly over the doubled array. If some window of `n` warehouses is coverable with `k` jumps, radius `R` is feasible.

Once that structure is visible, the implementation is mostly careful indexing.

## 🧩 Algorithm Walkthrough
1. **Binary Search on Answer**  
   Search `R` over `[0, L / 2]`. No warehouse can be farther than `L/2` in circular distance from a heater, so larger radii are irrelevant. The invariant is standard: `lo` is infeasible, `hi` is feasible, or equivalently maintain the smallest known feasible radius.

2. **Linearize the Circle with Array Doubling**  
   Build `ext = warehouses + [x + L for x in warehouses]`. Any contiguous run of `n` warehouses in `ext` corresponds to choosing a cut point on the ring. This removes modular arithmetic from the core coverage logic.

3. **Compute One-Heater Coverage via Greedy + Two Pointers**  
   For each index `i` in `ext`, treat `ext[i]` as the leftmost uncovered warehouse. The best heater center is the farthest warehouse `j` such that `ext[j] - ext[i] <= R`; placing farther right cannot hurt because it preserves left coverage and maximizes right reach. That heater then covers through position `ext[j] + R`. Using another advancing pointer, compute `next[i]`: the first index beyond that covered range.  
   Pattern: **Greedy + Two Pointers**. Invariant: `next[i]` is the earliest uncovered warehouse after one optimal heater starting from `i`.

4. **Lift to k Heaters with Jump Pointers**  
   Build binary lifting tables over `next`: `up[b][i]` = result of applying `2^b` heaters starting from `i`. This turns “where do we land after `k` heaters?” into `O(log k)` per start.

5. **Check Feasibility Across All Circular Cuts**  
   For each start `s` in `[0, n - 1]`, jump `k` times from `s`. If the resulting index is at least `s + n`, then that choice of cut covers all original warehouses. If any start works, radius `R` is feasible.  
   Correctness hinges on two facts: greedy is optimal for covering a line from left to right, and doubling enumerates all possible circular start points.

## 📊 Worked Example
Use `warehouses = [1, 4, 8, 11]`, `L = 12`, `k = 2`, and test `R = 2`.

Extended array: `[1, 4, 8, 11, 13, 16, 20, 23]`

| `i` | leftmost uncovered | farthest center within `R` | covered through | `next[i]` |
|---|---:|---:|---:|---:|
| 0 | 1  | 1  | 3  | 1 |
| 1 | 4  | 4  | 6  | 2 |
| 2 | 8  | 8  | 10 | 3 |
| 3 | 11 | 13 | 15 | 5 |

Now test circular starts:

1. Start at index `3` (warehouse `11`)  
   - Heater 1: `next[3] = 5` → covers `11, 1`  
   - From index `5` (warehouse `4` in wrapped view), Heater 2 reaches past index `7`  
   - Result index `>= 3 + 4`, so all 4 warehouses are covered.

2. Therefore `R = 2` is feasible with `k = 2`.  
   Trying `R = 1` fails for every start, so the minimum radius is `2`.

## ⏱ Complexity Analysis
### Time Complexity
For each candidate radius, the feasibility check is `O(n log k)`: `O(n)` to compute one-step jumps with two pointers, plus `O(n log k)` for binary lifting and evaluating all starts. Binary search over `R` adds a factor of `O(log L)`, yielding `O(n log k log L)`. This is practical at `2e5`; anything quadratic is not.

### Space Complexity
`O(n log k)` for the jump table over the doubled-array indices, plus `O(n)` for the extended positions and one-step transitions. You can reduce constants with iterative jump application, but you trade away predictable query speed during each feasibility check.

## 💡 Key Takeaways
- If the problem asks for the **minimum feasible threshold** and feasibility only gets easier as the threshold grows, reach for binary search on the answer.
- If the data lives on a ring but coverage is local and contiguous, duplicating the array is often the cleanest way to convert circular logic into linear intervals.
- The greedy placement must choose the **rightmost valid heater center** for the current leftmost uncovered warehouse; choosing the first valid center is suboptimal.
- Be careful with wrap-around windows: success means covering a span of exactly `n` warehouses in the doubled array, not merely reaching the physical coordinate `start + L`.
- In production planning systems, the winning move is often to turn combinatorial placement into a monotone decision problem with a fast verifier.

## 🚀 Variations & Further Practice
- Allow heaters to be placed at **any point on the ring**, not only warehouse positions. The feasibility check changes because the optimal center is no longer snapped to discrete coordinates.
- Minimize the **number of heaters** for a fixed radius `R`. Same greedy core, but the optimization direction flips and binary search is no longer necessary.
- Extend to **weighted warehouses** where each heater has capacity or each warehouse has demand. Coverage becomes coupled with assignment, pushing the problem toward flow or DP rather than pure interval greediness.