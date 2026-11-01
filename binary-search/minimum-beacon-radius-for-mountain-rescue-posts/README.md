# Minimum Beacon Radius for Mountain Rescue Posts

**Difficulty:** Hard &nbsp;|&nbsp; **Topic:** Binary Search &nbsp;|&nbsp; **Tags:** Binary Search, Sorting, Greedy

---

## 🗂 Problem Overview
Given unsorted integer coordinates for rescue posts and shelters on a 1D line, compute the smallest integer radius `R` such that every shelter lies within distance `R` of at least one post. Positions may be negative and may repeat. The challenge is scale: with up to `2 * 10^5` posts and shelters, checking every shelter against every post is too slow, so the solution must exploit ordering and monotonicity.

## 🌍 Engineering Impact
This pattern shows up anywhere a system must find the minimum service radius, latency budget, or replication distance that satisfies full coverage. Examples include CDN edge placement covering user regions, cellular tower planning, warehouse-to-customer service guarantees, geofencing alerts, and shard replica placement in distributed storage. At scale, brute-force pairwise checks collapse under `O(nm)` behavior and destroy tail latency. The combination of sorting, greedy coverage checks, and binary search on the answer turns a global optimization question into a monotone decision problem, which is exactly the kind of reframing that keeps large scheduling, placement, and routing systems tractable.

## 🔍 Problem Statement
You are given two integer arrays:

- `posts[i]`: position of the `i`-th rescue post
- `shelters[j]`: position of the `j`-th shelter that must be covered

A post with radius `R` covers any shelter whose absolute distance from that post is at most `R`. Return the minimum integer `R` such that every shelter is covered by at least one post.

Constraints:

- `1 <= posts.length, shelters.length <= 2 * 10^5`
- `-10^9 <= posts[i], shelters[j] <= 10^9`
- Coordinates may be negative
- Coordinates may repeat
- Answer fits in 32-bit signed integer

Examples:

- `posts = [1, 8, 15]`, `shelters = [2, 6, 10, 14]` → `2`
- `posts = [-5, 0, 9]`, `shelters = [-8, -4, 3, 11]` → `4`

The key constraint is input size: `O(nm)` is infeasible, so the algorithm must be near-linear after sorting or `O((n+m) log M)` with a monotone feasibility check.

## 🪜 How to Solve This
1. Read the problem → the output is not a matching or assignment, just the smallest radius that makes full coverage possible.

2. Notice the decision version is easier:  
   “If radius = `R`, can all shelters be covered?”  
   That yes/no question is monotone: if `R` works, any larger radius also works.

3. Monotone predicate → think **binary search on the answer**, not binary search inside the arrays first.

4. To check a candidate `R` efficiently, sort both arrays. Once sorted, coverage intervals from posts are ordered, so you can sweep through shelters from left to right.

5. Use a greedy two-pointer scan: for each post, its coverage interval is `[post - R, post + R]`. Advance the shelter pointer while shelters fall inside that interval. Never move backward.

6. Why this works: once a shelter is covered, revisiting it is pointless; once a post cannot cover the current shelter, earlier posts cannot help either because everything is sorted.

7. Binary search the minimum feasible `R`. The search space is bounded and small enough in bit-width, so the total runtime stays efficient even for very large inputs.

## 🧩 Algorithm Walkthrough
1. **Sort both arrays**  
   Sort `posts` and `shelters` in non-decreasing order. This enables ordered reasoning about coverage and makes a linear feasibility scan possible.  
   **Invariant:** all unprocessed shelters lie to the right of the current shelter pointer.

2. **Define the monotone predicate `canCover(R)`**  
   For a fixed radius `R`, determine whether every shelter is covered. This is the core **Binary Search on Answer** pattern: convert optimization into repeated feasibility checks.  
   **Why correct:** coverage only expands as `R` increases, so feasibility is monotone.

3. **Run a greedy two-pointer sweep inside `canCover(R)`**  
   Maintain pointer `j` to the first uncovered shelter. Iterate posts from left to right. For each post at `p`, compute interval `[p - R, p + R]`. If `shelters[j] < p - R`, that shelter is too far left for this post and all later posts, so `R` fails immediately. Otherwise, advance `j` while `shelters[j] <= p + R`.  
   **Pattern:** **Greedy + Two Pointers**.  
   **Invariant:** shelters before `j` are already covered; shelters at or after `j` are not yet covered.

4. **Check completion**  
   If `j == shelters.length`, all shelters are covered. Otherwise, some suffix remains uncovered.  
   **Why correct:** the sweep processes posts in the only order that can extend coverage rightward without missing earlier gaps.

5. **Binary search the minimum feasible radius**  
   Search `R` over `[0, maxDistance]`, where `maxDistance` can safely be `2 * 10^9`. For each midpoint, call `canCover(mid)`. If feasible, shrink right bound; otherwise, grow left bound.  
   **Invariant:** answer always remains inside the current search interval.

6. **Return the left bound**  
   When the interval closes, it is the smallest feasible integer radius.

## 📊 Worked Example
Use `posts = [1, 8, 15]`, `shelters = [2, 6, 10, 14]`.

After sorting, arrays are unchanged. Test `R = 2`.

| Post | Coverage Interval | Shelter Pointer Before | Shelters Covered | Shelter Pointer After |
|------|-------------------|------------------------|------------------|-----------------------|
| 1    | `[-1, 3]`         | `j = 0` (`2`)          | `2`              | `j = 1` (`6`)         |
| 8    | `[6, 10]`         | `j = 1` (`6`)          | `6, 10`          | `j = 3` (`14`)        |
| 15   | `[13, 17]`        | `j = 3` (`14`)         | `14`             | `j = 4` (done)        |

All shelters are covered, so `R = 2` is feasible.

Now test `R = 1`:

- Post `1` covers `[0, 2]` → covers shelter `2`
- Post `8` covers `[7, 9]` → current shelter is `6`, which is left of `7`
- Since shelters are sorted, no later post can cover `6`

So `R = 1` fails, proving the minimum radius is `2`.

## ⏱ Complexity Analysis
### Time Complexity
Sorting dominates setup: `O(p log p + s log s)`, where `p = posts.length` and `s = shelters.length`. Each feasibility check is `O(p + s)`, and binary search adds a factor of `log M`, where `M` is the coordinate range. In practice this is `O((p log p + s log s) + (p + s) log M)`, which remains viable at million-scale but not at billion-scale in memory-bound environments.

### Space Complexity
`O(1)` auxiliary space beyond the sort implementation if sorting can be done in place; otherwise `O(p + s)` depending on language/runtime sort behavior. The sweep itself is constant-space. Reducing space further usually means accepting implementation-specific in-place sorting trade-offs.

## 💡 Key Takeaways
- If the problem asks for the **minimum value** that makes a global condition true, check whether the condition is monotone and apply binary search on the answer.
- If entities live on a **1D ordered domain** and coverage is interval-based, sorting plus a greedy two-pointer sweep is usually the right abstraction.
- Be careful with the failure condition `shelters[j] < post - R`: that means the current shelter is already unreachable by this and all future posts.
- Use integer-safe midpoint calculation and wide enough arithmetic for `post ± R`, especially near `±10^9`.
- The transferable design insight is to turn expensive optimization into a cheap monotone feasibility test, then search the control parameter instead of exploring assignments directly.

## 🚀 Variations & Further Practice
- **2D beacon coverage:** posts and shelters are points on a plane; feasibility now depends on geometric nearest-neighbor structure rather than a simple ordered sweep.
- **Variable-radius posts with cost budget:** each post can be upgraded independently; minimize total cost while covering all shelters, which shifts the problem toward DP or weighted interval covering.
- **Online shelter arrivals:** shelters stream in over time and queries ask for current minimum feasible radius, requiring dynamic ordered sets or segment-tree-based nearest coverage maintenance.