# Minimum Router Signal to Reach All Offices

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Binary Search &nbsp;|&nbsp; **Tags:** Binary Search, Greedy, Array

---

## 🗂 Problem Overview
Given sorted office positions on a line and a limit of exactly `k` routers, find the minimum integer signal strength `r` such that all offices can be covered using at most `k` router placements. A router centered at any real position covers an interval of length `2r`. The non-trivial part is that placement is continuous, but feasibility for a fixed `r` must still be decided efficiently under `n <= 100000`, which rules out combinatorial search.

## 🌍 Engineering Impact
This pattern shows up whenever you must minimize a uniform resource budget while checking whether a deployment plan is feasible: CDN edge placement radius, cellular or Wi-Fi coverage planning, shard ownership ranges in distributed storage, and batch window sizing in streaming systems. At scale, brute-force placement or dynamic programming over coordinates collapses under large input domains (`10^9` positions here). The binary-search-on-answer pattern turns an optimization problem into repeated linear feasibility checks, which is often the difference between an operationally viable planner and one that times out or requires expensive approximation infrastructure.

## 🔍 Problem Statement
You are given a sorted integer array `offices`, where `offices[i]` is the kilometer marker of the `i`-th office along a highway, and an integer `k` representing the number of routers available. Every router must use the same integer signal strength `r`. A router placed at real-valued position `x` covers all offices in the inclusive interval `[x - r, x + r]`.

The task is to return the minimum integer `r` such that all offices can be covered using `k` or fewer routers.

Constraints:
- `1 <= offices.length <= 100000`
- `1 <= k <= offices.length`
- `0 <= offices[i] <= 1000000000`
- `offices` is sorted in non-decreasing order

Examples:
- `offices = [1, 2, 8, 12, 17], k = 2` → `4`
- `offices = [0, 4, 9, 15], k = 3` → `2`

The key algorithmic driver is the large coordinate range combined with sorted input: search the answer space, not the placement space.

## 🪜 How to Solve This
1. Read the problem → this is not asking for actual router positions first; it asks for the *smallest radius* that makes coverage possible.

2. Minimum feasible value usually suggests **binary search on the answer**. If radius `r` works, then any larger radius also works. That monotonicity is the entire opening.

3. Now define the feasibility check: for a fixed `r`, can we cover all offices with at most `k` routers?

4. Because offices are sorted and coverage is an interval on a line, the greedy move is forced: start from the leftmost uncovered office, place a router as far right as possible while still covering it. That means centering at `office + r`, which extends coverage to `office + 2r`.

5. Then skip every office within that covered range and repeat. This minimizes router count for that `r`, because each router covers the maximum possible suffix starting from the current uncovered office.

6. Once feasibility is `O(n)`, binary search over `r` from `0` to `offices[last] - offices[0]` gives the minimum feasible integer radius efficiently.

## 🧩 Algorithm Walkthrough
1. **Pattern selection: Binary Search on Answer + Greedy Feasibility Check.**  
   The optimization target is a scalar integer `r`. Feasibility is monotonic: if radius `r` covers all offices with `<= k` routers, then any `r' > r` also works. That makes binary search the right abstraction.

2. **Set search bounds.**  
   Use `low = 0` and `high = offices[n - 1] - offices[0]`.  
   `0` is the smallest possible integer radius. The full span is a safe upper bound because one router with that radius can always cover everything if `k >= 1`.

3. **Define `canCover(r)`.**  
   Scan left to right. Let `i` be the first uncovered office. Place a router centered at `offices[i] + r`. Its rightmost coverage is `offices[i] + 2*r`. Consume all offices with position `<= offices[i] + 2*r`, then increment the router count.

4. **Why the greedy placement is correct.**  
   For the leftmost uncovered office, any valid router must cover it. Among all such placements, centering at `offices[i] + r` pushes the interval farthest right, maximizing how many future offices are covered. This preserves the invariant: after each router, we have covered the longest possible prefix using that many routers.

5. **Use the feasibility result in binary search.**  
   If `canCover(mid)` is true, record it as a candidate and search left for a smaller radius. Otherwise search right.

6. **Terminate at the first feasible radius.**  
   Standard lower-bound binary search returns the minimum integer `r` satisfying feasibility.

## 📊 Worked Example
Example: `offices = [1, 2, 8, 12, 17]`, `k = 2`

Check `r = 4`:

| Step | First Uncovered Office | Router Center | Coverage Interval | Offices Covered | Routers Used |
|---|---:|---:|---|---|---:|
| 1 | 1 | 5 | [1, 9] | 1, 2, 8 | 1 |
| 2 | 12 | 16 | [12, 20] | 12, 17 | 2 |

All offices are covered with `2` routers, so `r = 4` is feasible.

Check `r = 3`:

- Start at office `1`, place router at `4`, cover `[1, 7]` → covers `1, 2`
- Next uncovered is `8`, place router at `11`, cover `[8, 14]` → covers `8, 12`
- Next uncovered is `17`, need a third router

So `r = 3` is not feasible with `k = 2`. Since `4` works and `3` does not, the minimum answer is `4`.

## ⏱ Complexity Analysis
### Time Complexity
The greedy feasibility check is `O(n)` because each office is visited once per check. Binary search over the radius range adds a factor of `O(log D)`, where `D = offices[n-1] - offices[0]`, so total time is `O(n log D)`. With `D <= 10^9`, `log D` is about 30, which is practical even at million-scale input sizes.

### Space Complexity
The algorithm uses `O(1)` extra space beyond the input array. No auxiliary structures are required; the scan maintains only indices, bounds, and a router counter. Space cannot be meaningfully reduced further unless you change the input representation itself.

## 💡 Key Takeaways
- If the problem asks for the minimum numeric value that makes a condition true, check whether the condition is monotonic and binary-search the answer.
- When points lie on a line and coverage is interval-based, a left-to-right greedy sweep is often the optimal feasibility check.
- The router should be placed at `leftmost_uncovered + r`, not at the office itself; that is what maximizes rightward coverage.
- Be careful with inclusive boundaries: offices at exactly `office[i] + 2*r` are covered and must be consumed in the same sweep.
- In production planning systems, separating optimization from feasibility often turns an intractable search over placements into a stable, scalable decision procedure.

## 🚀 Variations & Further Practice
- Add a constraint that routers may only be placed at office locations; the feasibility check changes because the optimal center is no longer continuous.
- Minimize the number of routers for a fixed radius instead of minimizing radius for fixed `k`; same greedy core, but no outer binary search.
- Extend from 1D offices on a line to 2D office coordinates with circular coverage; the monotone structure weakens and the problem becomes substantially harder.