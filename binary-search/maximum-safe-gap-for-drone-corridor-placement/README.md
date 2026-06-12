# Maximum Safe Gap for Drone Corridor Placement

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Binary Search &nbsp;|&nbsp; **Tags:** Binary Search, Greedy, Array

---

## 🗂 Problem Overview
Given a sorted array of approved mounting positions and an integer `k`, place exactly `k` beacons so that the minimum distance between consecutive chosen positions is as large as possible. Return that maximum achievable minimum gap. The challenge is not placement generation but optimization under large input bounds: `n` can reach `100000`, so enumerating subsets or trying all placements directly is computationally infeasible.

## 🌍 Engineering Impact
This pattern shows up anywhere a system must maximize minimum separation under discrete placement constraints: warehouse robot charging pads, CDN edge placement along network topology slices, sensor deployment on pipelines, shard leader placement across racks, or maintenance windows spaced across event timelines. At scale, brute force collapses because the search space is combinatorial, while the decision boundary is monotonic. Recognizing that distinction lets you convert an optimization problem into repeated feasibility checks, which is a common design move in schedulers, capacity planners, and placement engines where exact enumeration is operationally impossible.

## 🔍 Problem Statement
You are given a strictly increasing sorted array `positions` of length `n`, where `positions[i]` is a valid mounting point on a straight corridor, and an integer `k` with `2 <= k <= n`. You may place at most one beacon at each position and must place exactly `k` beacons.

The objective is to maximize the minimum distance between any two consecutive placed beacons. Return that largest feasible distance.

Constraints:
- `2 <= n <= 100000`
- `2 <= k <= n`
- `0 <= positions[i] <= 1000000000`
- `positions` is sorted in strictly increasing order

Examples:
- `positions = [1, 2, 8, 12, 17], k = 3` → `7`
- `positions = [3, 6, 14, 20, 25, 31], k = 4` → `6`

The key constraint is input size: exponential subset search is impossible, so the solution must exploit structure in the answer space.

## 🪜 How to Solve This
1. Read the objective carefully → we are maximizing a minimum value, not constructing all valid placements.
2. Ask whether the answer space is ordered → if a minimum gap `d` is feasible, then any smaller gap is also feasible. That monotonicity is the signal for binary search on the answer.
3. Reduce optimization to decision → “Can I place `k` beacons such that each chosen position is at least `d` away from the previous one?”
4. Solve that decision problem greedily → always place the next beacon at the earliest position that satisfies the gap. Delaying placement never helps you fit more beacons later.
5. Combine both ideas → binary search over candidate distances from `1` to `positions[n-1] - positions[0]`, using the greedy check as the feasibility oracle.
6. Keep the best feasible distance and discard infeasible larger ones.
7. The result is efficient because each feasibility check is linear, and the number of candidate distances is logarithmic in the coordinate range.

## 🧩 Algorithm Walkthrough
1. **Define the search space.**  
   The minimum gap must lie between `1` and `positions[n-1] - positions[0]`. This is the answer domain for **Binary Search on Answer**. The invariant is: feasible values lie on the left side of some boundary, infeasible values on the right.

2. **Implement a feasibility check for a candidate gap `d`.**  
   Start by placing the first beacon at `positions[0]`. Then scan left to right and place the next beacon whenever `positions[i] - lastPlaced >= d`. This is the **Greedy placement** step.

3. **Why greedy is correct.**  
   Choosing the earliest valid position leaves maximal remaining space for future placements. Any later choice cannot increase the number of beacons you can still place. The invariant is: after placing `t` beacons, greedy has used the leftmost possible valid ending position among all ways to place `t` beacons.

4. **Count placements.**  
   If the greedy scan places at least `k` beacons, then distance `d` is feasible. Otherwise it is not.

5. **Binary search the boundary.**  
   If `d` is feasible, record it and search larger distances. If not, search smaller distances. Use `mid = low + (high - low) / 2` to avoid overflow in fixed-width integer languages.

6. **Return the largest feasible value.**  
   At termination, the stored answer is the maximum safe gap. This works because feasibility is monotonic and the greedy oracle is correct.

## 📊 Worked Example
Use `positions = [1, 2, 8, 12, 17]`, `k = 3`.

| low | high | mid | Greedy placement trace | placed | feasible |
|---|---:|---:|---|---:|---|
| 1 | 16 | 8 | 1 → 12 | 2 | No |
| 1 | 7 | 4 | 1 → 8 → 12 → 17 | 4 | Yes |
| 5 | 7 | 6 | 1 → 8 → 17 | 3 | Yes |
| 7 | 7 | 7 | 1 → 8 → 17 | 3 | Yes |

Trace details:
1. Try gap `8`: after placing at `1`, neither `2` nor `8` works; `12` works, but no third beacon fits.
2. Try gap `4`: greedy can place more than `k`, so `4` is feasible.
3. Try gap `6`: `1`, `8`, `17` works.
4. Try gap `7`: still feasible. Search ends, answer is `7`.

## ⏱ Complexity Analysis
### Time Complexity
`O(n log R)`, where `R = positions[n-1] - positions[0]`. Each binary-search step runs one linear greedy scan over `n` positions, and there are `log R` candidate distances to test. This scales well even when coordinates approach `10^9`; the logarithmic factor stays small while the linear scan dominates.

### Space Complexity
`O(1)` auxiliary space. The algorithm uses a handful of scalar variables for binary search bounds, the last placed position, and the placement count. No extra arrays or heaps are required. Space cannot be meaningfully reduced further without changing the input representation.

## 💡 Key Takeaways
- If the problem asks to **maximize a minimum** or **minimize a maximum**, check whether candidate answers form a monotonic feasible/infeasible boundary; that is the classic trigger for binary search on the answer.
- If the input is already sorted and feasibility depends on local spacing, expect a greedy left-to-right validator rather than dynamic programming or backtracking.
- Be precise about the binary-search bounds: search distances, not indices, and return the largest feasible value rather than the last midpoint tested.
- The feasibility check should count `>= k` placements as success; requiring exactly `k` inside the check is an unnecessary trap and can reject valid answers.
- The transferable design insight is to separate **optimization** from **decision**: build a cheap monotonic feasibility oracle, then search the objective space instead of enumerating configurations.

## 🚀 Variations & Further Practice
- **Aggressive Cows / Magnetic Force Between Two Balls** — same core pattern, but framed as stall or basket placement; useful for recognizing the abstraction independent of domain wording.
- **Minimize the maximum load / capacity problems** — binary search on answer still applies, but the feasibility check changes from spacing-greedy to partitioning or accumulation logic.
- **Beacon placement with blocked intervals or weighted positions** — harder because simple greedy may no longer be sufficient; feasibility can require interval reasoning, DP, or more complex state.