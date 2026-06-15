# Minimum Beacon Radius for City Coverage

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Binary Search &nbsp;|&nbsp; **Tags:** Binary Search, Sorting, Arrays

---

## 🗂 Problem Overview
Given unsorted positions of houses and beacon towers on a 1D line, compute the minimum integer radius `R` such that every house lies within distance `R` of at least one beacon. All beacons share the same radius. The key challenge is scale: with up to 200,000 houses and 200,000 beacons, any pairwise comparison strategy is too slow, so the solution must exploit ordering and fast nearest-neighbor lookup.

## 🌍 Engineering Impact
This pattern shows up anywhere a large set of query points must be matched to the nearest service point on an ordered axis. Examples include CDN edge selection by latency buckets, nearest warehouse or cell-tower assignment, compiler symbol lookup in sorted tables, and time-based event correlation in streaming systems. At scale, naive scan-per-query logic collapses under `O(nm)` behavior, causing latency spikes and poor cost efficiency. Sorting once and answering each query with logarithmic lookup turns an intractable batch into a predictable pipeline, and the same design principle generalizes to index-backed serving systems.

## 🔍 Problem Statement
You are given two integer arrays:

- `houses`, where `houses[i]` is the position of a house
- `beacons`, where `beacons[j]` is the position of a beacon tower

Each beacon broadcasts symmetrically with the same radius `R`. A house is covered if there exists at least one beacon with distance at most `R`. Return the minimum integer `R` that covers every house.

Constraints:

- `1 <= houses.length <= 200000`
- `1 <= beacons.length <= 200000`
- `-10^9 <= houses[i], beacons[j] <= 10^9`
- Arrays may be unsorted
- Positions may be negative
- Duplicates are allowed
- The result fits in a 32-bit signed integer

Examples:

- `houses = [1, 5, 9], beacons = [2, 8]` → `3`
- `houses = [-4, 0, 7, 15], beacons = [-2, 10]` → `5`

The constraint that drives the algorithmic choice is input size: `200000 x 200000` pairwise distance checks are not viable.

## 🪜 How to Solve This
1. Read the problem → the output is a single global radius, but coverage is determined house by house.

2. Reframe it → for each house, the only number that matters is the distance to its nearest beacon. If a house’s nearest beacon is `d`, then any valid global radius must be at least `d`.

3. That means the final answer is:
   **maximum over all houses of (distance to nearest beacon)**.

4. Now ask how to get the nearest beacon efficiently. The beacons are on a line, so once sorted, the nearest beacon to a house must be either:
   - the first beacon not less than the house, or
   - the beacon immediately before it.

5. That observation suggests binary search on the sorted beacon array for each house. No need to scan all beacons.

6. Complexity falls to sorting plus one logarithmic lookup per house, which is exactly the shape you want for large inputs.

7. Equivalent binary-search-on-answer approaches also work, but direct nearest-neighbor lookup is simpler and avoids an extra search layer.

## 🧩 Algorithm Walkthrough
1. **Sort the beacon positions**  
   This enables ordered nearest-neighbor lookup. The core pattern is **Binary Search on a sorted array**: once beacons are sorted, every house can locate its insertion point in `O(log m)`.

2. **Iterate through each house**  
   For a given house position `h`, binary search finds the first beacon index `i` such that `beacons[i] >= h`. This partitions the beacon array into candidates on the left and right.

3. **Evaluate the only relevant candidates**  
   The nearest beacon must be either:
   - `beacons[i]` if `i` exists, or
   - `beacons[i - 1]` if `i > 0`  
   Any beacon farther left than `i - 1` or farther right than `i` is strictly worse because the array is sorted.

4. **Compute the nearest distance for that house**  
   Let `leftDist` and `rightDist` be the distances to those candidates, using infinity-like sentinels when one side is missing. The nearest distance is `min(leftDist, rightDist)`.

5. **Maintain the global answer**  
   Update `answer = max(answer, nearestDistance)`.  
   Invariant: after processing the first `k` houses, `answer` is the minimum radius needed to cover exactly those `k` houses.

6. **Return the final maximum**  
   This is correct because a single shared radius must satisfy the worst-covered house. The algorithm is optimal enough for the constraints and avoids the unnecessary complexity of binary-searching the answer space.

## 📊 Worked Example
Example: `houses = [-4, 0, 7, 15]`, `beacons = [-2, 10]`

Sorted beacons: `[-2, 10]`

| House `h` | Lower-bound index `i` | Left beacon | Right beacon | Nearest distance | Running answer |
|---|---:|---:|---:|---:|---:|
| -4 | 0 | none | -2 | 2 | 2 |
| 0  | 1 | -2 | 10 | min(2, 10) = 2 | 2 |
| 7  | 1 | -2 | 10 | min(9, 3) = 3 | 3 |
| 15 | 2 | 10 | none | 5 | 5 |

Trace:

1. House `-4` can only use beacon `-2` → distance `2`.
2. House `0` sits between `-2` and `10` → nearest is `-2`, distance `2`.
3. House `7` is closer to `10` than `-2` → distance `3`.
4. House `15` can only use beacon `10` → distance `5`.

Maximum nearest distance is `5`, so the minimum valid radius is `5`.

## ⏱ Complexity Analysis
### Time Complexity
`O(m log m + n log m)`, where `n = houses.length` and `m = beacons.length`. Sorting beacons dominates once, then each house performs one binary search. At `10^6` scale this is still practical; at `10^9`, even linear passes become the bottleneck, so this approach is only realistic when data is externally indexed or partitioned.

### Space Complexity
`O(1)` auxiliary space beyond the sort implementation, or `O(log m)` if the language runtime uses recursive/introspective sort stack space. The main storage is the input arrays themselves. If mutation is disallowed, copying the beacon array adds `O(m)` space in exchange for preserving caller-owned data.

## 💡 Key Takeaways
- If the problem asks for the nearest element on a 1D axis and the input is large, sorting plus binary search should be one of the first candidate strategies.
- If the final answer is a global threshold that must satisfy every item, check whether it reduces to a max/min over per-item local optima.
- Be careful with lower-bound edge cases: when insertion index is `0` or `beacons.length`, only one neighbor exists.
- Use absolute-distance logic carefully around negative coordinates and duplicates; duplicates are valid and should not trigger special-case bugs.
- The transferable design insight is to pay a one-time indexing cost up front so repeated nearest-neighbor queries become cheap, predictable, and scalable.

## 🚀 Variations & Further Practice
- **Binary search on the answer instead of direct nearest lookup**: check whether a candidate radius covers all houses using a linear sweep; harder because correctness depends on a monotonic feasibility function.
- **2D nearest facility coverage**: houses and beacons become points on a plane; harder because simple sorting no longer gives nearest neighbors, pushing you toward spatial indexes like k-d trees or Voronoi-style reasoning.
- **Dynamic beacon updates with online house queries**: beacons can be inserted or removed between queries; harder because static sorting is no longer enough and you need balanced search trees or interval-aware indexing.