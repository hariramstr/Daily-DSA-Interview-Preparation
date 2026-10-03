# Minimum Reading Light Radius for Library Tables

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Binary Search &nbsp;|&nbsp; **Tags:** Binary Search, Sorting, Arrays

---

## 🗂 Problem Overview
You are given positions of reading lights and study tables on a 1D number line and must compute the smallest integer radius `r` such that every table lies within distance `r` of at least one light. The output is a single integer: the minimum feasible shared radius. The non-trivial part is scale: with up to `2 * 10^5` lights and tables, comparing every table against every light is too slow, so the solution must exploit ordering and nearest-neighbor search.

## 🌍 Engineering Impact
This pattern shows up anywhere a system must guarantee coverage from fixed service points: CDN edge placement against user regions, warehouse-to-delivery-zone reachability, cellular tower coverage, geofencing, and shard-to-key-range assignment. At small scale, brute force works; at production scale, it collapses under quadratic behavior and cache-unfriendly scans. Sorting plus binary search turns a global coverage question into repeated local nearest-neighbor queries. That shift matters architecturally: it enables predictable latency, supports large batch evaluations, and composes cleanly with offline preprocessing when reference points change less frequently than queries.

## 🔍 Problem Statement
A hallway is modeled as a number line. `lights[i]` gives the position of a reading light, and `tables[j]` gives the position of a study table. Every light uses the same illumination radius `r`, and a table at position `t` is covered if there exists some light at position `l` such that `|l - t| <= r`.

Return the minimum integer `r` that covers all tables.

Constraints:
- `1 <= lights.length, tables.length <= 2 * 10^5`
- `-10^9 <= lights[i], tables[i] <= 10^9`
- Positions may repeat
- The answer fits in a 32-bit signed integer

Examples:

- `lights = [2, 10], tables = [1, 5, 11]` → `3`
- `lights = [-4, 0, 8], tables = [-7, -1, 3, 10]` → `3`

The key constraint is input size. An `O(L * T)` scan is unacceptable; the algorithm must be near `O((L + T) log L)` or `O((L + T) log (L + T))`.

## 🪜 How to Solve This
1. Read the problem → this is a **minimum feasible value** question. That is usually a binary-search signal.
2. Ask what makes a radius feasible → if radius `r` covers every table, then any larger radius also works. That monotonic property makes binary search valid.
3. Now ask how to test one candidate radius efficiently → sort the light positions, then for each table find the nearest light using binary search.
4. But there is an even simpler observation → the minimum required radius is just the **maximum**, over all tables, of the distance to that table’s nearest light.
5. That means we do not actually need outer binary search on `r`. We can directly compute each table’s nearest-light distance after sorting lights.
6. For each table, binary-search the insertion point among lights. Only two candidates can be closest: the light just left of the insertion point and the one at the insertion point.
7. Take the smaller of those two distances for the current table, then take the maximum across all tables.

This is the standard “sorted reference set + nearest-neighbor by binary search” pattern.

## 🧩 Algorithm Walkthrough
1. **Sort the `lights` array.**  
   This enables logarithmic nearest-neighbor lookup. The invariant after sorting is that relative order is fixed, so for any table, the closest light must be adjacent to its insertion position.

2. **Initialize `answer = 0`.**  
   This tracks the minimum radius required so far. The invariant is: after processing the first `k` tables, `answer` equals the maximum nearest-light distance among those `k` tables.

3. **For each table position `t`, binary-search the first light `>= t`.**  
   This is the standard lower-bound operation. It partitions lights into those left of `t` and those at or right of `t`.

4. **Evaluate the only two relevant candidates.**  
   If lower bound returns index `i`, then the nearest light can only be:
   - `lights[i]` if `i < lights.length`
   - `lights[i - 1]` if `i > 0`  
   Any other light is farther because the array is sorted.

5. **Compute `nearestDist = min(leftDist, rightDist)`.**  
   This is correct because nearest-neighbor in 1D under absolute distance is determined by adjacent sorted positions around the query point.

6. **Update `answer = max(answer, nearestDist)`.**  
   The global radius must cover the worst-covered table. This preserves the invariant that `answer` is sufficient for all processed tables.

7. **Return `answer`.**  
   This is the direct solution.  
   Pattern: **Binary Search on Sorted Array** for nearest-neighbor queries. It is the right abstraction because the problem reduces from global coverage to repeated local distance minimization against a static sorted set.

## 📊 Worked Example
Use `lights = [2, 10]`, `tables = [1, 5, 11]`.

Sorted lights: `[2, 10]`

| Table `t` | Lower-bound index | Left candidate | Right candidate | Nearest distance | Running answer |
|---|---:|---:|---:|---:|---:|
| 1  | 0 | none | 2  | `|2-1| = 1` | 1 |
| 5  | 1 | 2 | 10 | `min(3, 5) = 3` | 3 |
| 11 | 2 | 10 | none | `|11-10| = 1` | 3 |

Trace:
1. Table `1` is left of all lights, so only light `2` matters.
2. Table `5` falls between `2` and `10`; the closer light is `2`, distance `3`.
3. Table `11` is right of all lights, so only light `10` matters.

The maximum nearest-light distance is `3`, so the minimum valid radius is `3`.

## ⏱ Complexity Analysis
### Time Complexity
Sorting lights costs `O(L log L)`. Then each of `T` tables performs a binary search over lights in `O(log L)`, for total `O(L log L + T log L)`. This remains practical at `10^6` scale, while quadratic comparison becomes unusable long before that; at `10^9`, only heavily indexed or distributed variants are realistic.

### Space Complexity
If sorting in place is allowed, auxiliary space is `O(1)` beyond the input arrays, excluding sort implementation details. If the language runtime uses non-in-place sorting, space is typically `O(L)`. You can reduce memory by reusing the input array, at the cost of mutating caller-owned data.

## 💡 Key Takeaways
- If the problem asks for a minimum shared threshold that makes all items valid, check for monotonic feasibility and binary-search signals.
- If one set is static and the other issues many proximity queries, sort the static set and do nearest-neighbor lookup instead of pairwise comparison.
- After lower-bound search, only the immediate left and right lights can be closest; checking more positions is wasted work.
- Be careful at array boundaries: when the insertion point is `0` or `lights.length`, only one candidate exists.
- At production scale, the winning move is often reframing a global coverage requirement as repeated local queries over a preprocessed index.

## 🚀 Variations & Further Practice
- **Circular hallway / ring topology:** nearest light is no longer determined by simple linear neighbors; wrap-around distance changes the search logic.
- **2D table and light positions:** the 1D sorted-array trick no longer works directly; you need spatial indexing such as KD-trees, sweep lines, or Voronoi-style reasoning.
- **Dynamic updates to lights:** if lights are inserted and removed between queries, static sorting is insufficient; use balanced BSTs, interval trees, or ordered sets to preserve fast nearest-neighbor queries.