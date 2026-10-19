# Minimum Heater Power for Cold Storage Crates

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Binary Search &nbsp;|&nbsp; **Tags:** Binary Search, Sorting, Arrays

---

## 🗂 Problem Overview
Given unsorted crate and heater positions on a number line, compute the smallest integer heater power `r` such that every crate lies within distance `r` of at least one heater. The output is a single minimum radius. The problem is non-trivial because both arrays can be large, coordinates can be negative, and a naive nearest-heater scan per crate becomes too expensive. The key is to exploit ordering so nearest-neighbor lookup is sublinear.

## 🌍 Engineering Impact
This pattern shows up anywhere a large set of demand points must be covered by the nearest supply point under a uniform service radius: CDN edge coverage, warehouse robot docking zones, cellular tower planning, geofenced alerting, and shard-to-node placement validation. At scale, the difference between linear nearest-neighbor scans and indexed lookup is the difference between a planning job that finishes in seconds and one that degrades quadratically under growth. Sorting plus binary search gives a simple, cache-friendly static index. It enables predictable latency, straightforward correctness reasoning, and easy adaptation to offline capacity or coverage analysis pipelines.

## 🔍 Problem Statement
You are given two integer arrays:

- `crates`, where `crates[i]` is the position of the `i`-th crate
- `heaters`, where `heaters[j]` is the position of the `j`-th heater

A heater with power `r` covers every crate within distance `r` from its position. A crate at position `x` is covered if there exists some heater `h` such that `|x - h| <= r`.

Return the smallest integer `r` that covers all crates.

Constraints:

- `1 <= crates.length, heaters.length <= 2 * 10^5`
- `-10^9 <= crates[i], heaters[j] <= 10^9`
- Arrays may be unsorted
- Positions may contain duplicates

Examples:

- `crates = [2, 8, 14], heaters = [4, 10]` → `4`
- `crates = [1, 5, 9, 15], heaters = [6]` → `9`

The decisive constraint is input size: with up to `2 * 10^5` elements, checking every crate against every heater is not viable. The solution needs indexed nearest-heater lookup, which naturally points to sorting and binary search.

## 🪜 How to Solve This
1. Read the problem → the output is a **minimum global radius**, but coverage is determined **locally per crate** by its nearest heater.

2. For any fixed crate, only the closest heater matters. A farther heater can never reduce the required radius for that crate.

3. That reframes the problem: for each crate, compute its distance to the nearest heater; then take the maximum of those distances across all crates.

4. Now ask: how do we find the nearest heater efficiently?  
   → If heaters are sorted, the nearest candidate must be either:
   - the first heater `>= crate`  
   - or the heater immediately before it

5. That is exactly a binary search problem: find the insertion point of each crate in the sorted heater array.

6. For each crate, compare distance to the left neighbor and right neighbor heater, handling missing sides with infinity-like logic.

7. Track the maximum nearest-heater distance seen so far. That maximum is the smallest radius that covers every crate.

The core insight is simple: **min over heaters per crate, then max over crates globally**. Sorting turns nearest-neighbor lookup from linear scan into logarithmic search.

## 🧩 Algorithm Walkthrough
1. **Sort the heater positions**  
   This is the enabling preprocessing step. Once sorted, every crate’s nearest heater must lie adjacent to its insertion point.  
   **Invariant:** heater positions are in non-decreasing order.

2. **Iterate through each crate**  
   Process crates independently; no crate affects another crate’s nearest-heater distance.  
   **Why correct:** the required global radius is the maximum of independent local requirements.

3. **Run binary search on the heater array**  
   Find the first heater position `>= crate`. This is the standard lower-bound operation.  
   **Pattern:** Binary Search on a sorted static index.  
   **Invariant:** after search, all heaters before the index are `< crate`, and the heater at the index, if present, is `>= crate`.

4. **Evaluate the two nearest candidates**  
   The right candidate is the lower-bound heater. The left candidate is the heater immediately before it. Compute both distances when they exist.  
   **Why correct:** in a sorted array, any heater farther left than the immediate predecessor or farther right than the lower bound is strictly worse.

5. **Take the smaller of the two distances**  
   This is the minimum power needed to cover the current crate.  
   **Invariant:** after this step, we know the exact local requirement for that crate.

6. **Update the global answer with the maximum local requirement**  
   `answer = max(answer, nearestDistance)`  
   **Why correct:** a single global heater power must satisfy every crate, so it must be at least the largest local minimum.

7. **Return the final maximum**  
   This is the smallest feasible integer radius. Any smaller value fails for at least one crate.

## 📊 Worked Example
Example: `crates = [2, 8, 14]`, `heaters = [4, 10]`

Sorted heaters: `[4, 10]`

| Crate | Lower-bound index | Left heater | Right heater | Nearest distance | Running answer |
|------:|------------------:|------------:|-------------:|-----------------:|---------------:|
| 2     | 0                 | —           | 4            | `|2-4| = 2`      | 2              |
| 8     | 1                 | 4           | 10           | `min(4, 2) = 2`  | 2              |
| 14    | 2                 | 10          | —            | `|14-10| = 4`    | 4              |

Trace:

1. Crate `2` has no left heater; nearest is `4`, distance `2`.
2. Crate `8` falls between `4` and `10`; nearest is `10`, distance `2`.
3. Crate `14` has no right heater; nearest is `10`, distance `4`.

The maximum nearest distance is `4`, so the minimum required heater power is `4`.

## ⏱ Complexity Analysis
### Time Complexity
Sorting heaters costs `O(m log m)`. Then each of `n` crates performs a binary search over `m` heaters, costing `O(log m)` each, for total `O(m log m + n log m)`. At million-scale inputs this remains practical; at billion-scale, even sorting alone becomes a distributed systems problem rather than an algorithmic one.

### Space Complexity
Auxiliary space is `O(1)` beyond the sort, or `O(log m)` if the language runtime uses recursive sorting stack space. The dominant storage is the input arrays themselves. You can sort in place to avoid extra arrays, trading away preservation of original order.

## 💡 Key Takeaways
- If the problem asks for the nearest element in another large static set, sorting plus binary search should be one of the first candidate patterns.
- If the final answer is a single global threshold, check whether it can be expressed as `max(min(...))` over independent items.
- Be careful with lower-bound edge cases: insertion index `0` means no left heater, and index `m` means no right heater.
- Use wide integer arithmetic for distance calculations if your language can overflow on subtraction near `±10^9`.
- In production systems, a sorted immutable index is often the simplest high-leverage structure for repeated nearest-neighbor queries when full spatial indexing would be overkill.

## 🚀 Variations & Further Practice
- **Binary search the answer radius instead of direct nearest lookup:** check whether all crates are coverable for a candidate `r`; the twist is converting local nearest-neighbor logic into a monotonic feasibility test.
- **2D coverage with heaters on a plane:** nearest-neighbor lookup is no longer solved by one-dimensional ordering; the twist is needing spatial indexes such as KD-trees or sweep-line structures.
- **Heaters with individual power limits or activation costs:** the twist is optimization under heterogeneous constraints, which pushes the problem toward greedy interval covering or dynamic programming.