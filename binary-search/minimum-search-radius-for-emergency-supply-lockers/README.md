# Minimum Search Radius for Emergency Supply Lockers

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Binary Search &nbsp;|&nbsp; **Tags:** Binary Search, Sorting, Greedy

---

## 🗂 Problem Overview
Given unsorted integer positions for neighborhoods (`homes`) and supply lockers (`lockers`) on a line, compute the smallest integer radius `R` such that every home lies within distance `R` of at least one locker. The output is a single integer: the minimum feasible coverage radius. The challenge is scale: with up to `2 * 10^5` homes and lockers, comparing every home against every locker is prohibitively expensive, so the solution must exploit ordering and monotonicity.

## 🌍 Engineering Impact
This pattern shows up anywhere a system must provision the minimum service radius that guarantees full coverage over ordered coordinates: CDN edge placement, warehouse-to-customer delivery zones, cellular tower planning, cache node reachability, and geofenced alerting. At scale, brute-force nearest-facility checks collapse under quadratic behavior and destroy latency budgets. Sorting plus monotonic validation turns an intractable all-pairs problem into a predictable, cache-friendly scan. Architecturally, this enables capacity planning, SLA verification, and “smallest sufficient threshold” decisions without overprovisioning infrastructure or burning compute on repeated nearest-neighbor searches.

## 🔍 Problem Statement
You are given two integer arrays:

- `homes`, where `homes[i]` is the position of a neighborhood
- `lockers`, where `lockers[j]` is the position of an emergency supply locker

A home is covered if there exists some locker within distance `R`. Return the minimum integer `R` such that all homes are covered.

Constraints:

- `1 <= homes.length, lockers.length <= 2 * 10^5`
- `0 <= homes[i], lockers[j] <= 10^9`
- Arrays may be unsorted
- Duplicate positions are allowed
- The answer fits in a 32-bit signed integer

Examples:

- `homes = [2, 10, 14], lockers = [4, 12]` → `2`
- `homes = [1, 5, 9, 15], lockers = [6]` → `9`

The key constraint is input size. An `O(n * m)` search over all home-locker pairs is too slow, so the algorithm must leverage sorting and the monotonic property of the answer.

## 🪜 How to Solve This
1. Read the problem → we are not asked for the nearest locker per home directly; we are asked for the **smallest radius that makes all homes valid**. That is a classic “search on answer” signal.

2. Ask whether feasibility is monotonic → if radius `R` covers every home, then any larger radius also covers every home. Once true, always true. That immediately suggests binary search over `R`.

3. To test one candidate radius efficiently, sort both arrays. After sorting, coverage intervals around lockers are ordered, and homes are ordered too.

4. Now think greedily: scan homes from left to right while advancing through lockers only when needed. For a fixed `R`, each locker covers `[locker - R, locker + R]`. If the current home is left of that interval, coverage fails. If it is inside, move to the next home. If it is right of the interval, move to the next locker.

5. This feasibility check is linear after sorting, so binary search becomes practical: `O((n + m) log answer_range)` after `O(n log n + m log m)` preprocessing.

## 🧩 Algorithm Walkthrough
1. **Sort both arrays**  
   Sort `homes` and `lockers` in ascending order. This enables a single left-to-right pass. The invariant after sorting: any uncovered home to the left can never be helped by a later locker if the current locker already fails to reach it.

2. **Define the search space for `R`**  
   The minimum possible radius is `0`. A safe upper bound is `max(|home - locker|)` in the worst case, but `10^9` is sufficient under constraints. We binary search this integer range. Pattern: **Binary Search on Answer**.

3. **Implement a feasibility check `canCover(R)`**  
   Use two pointers: `i` for homes, `j` for lockers. Pattern: **Two Pointers + Greedy Scan**.  
   For each locker, its coverage interval is `[lockers[j] - R, lockers[j] + R]`.

4. **Advance pointers greedily**  
   - If `homes[i] < lockers[j] - R`, then the current home lies left of the current locker’s reach, and because lockers are sorted, no later locker can cover it with this `R`. Return `false`.  
   - If `homes[i] > lockers[j] + R`, this locker cannot cover the current home; advance `j`.  
   - Otherwise, the home is covered; advance `i`.

5. **Maintain the invariant**  
   At every step, all homes before `i` are already proven covered, and all lockers before `j` are exhausted or irrelevant. No backtracking is needed because ordering makes local decisions globally safe.

6. **Binary search for the minimum valid radius**  
   If `canCover(mid)` is true, search left to find a smaller radius. Otherwise search right. The first feasible `R` is the answer.

## 📊 Worked Example
Example: `homes = [2, 10, 14]`, `lockers = [4, 12]`

After sorting: already sorted.

Check `R = 2`:

| Step | `i` | `j` | Home | Locker | Coverage Interval | Action |
|---|---:|---:|---:|---:|---|---|
| 1 | 0 | 0 | 2 | 4 | `[2, 6]` | `2` is covered → `i++` |
| 2 | 1 | 0 | 10 | 4 | `[2, 6]` | `10 > 6` → `j++` |
| 3 | 1 | 1 | 10 | 12 | `[10, 14]` | `10` is covered → `i++` |
| 4 | 2 | 1 | 14 | 12 | `[10, 14]` | `14` is covered → `i++` |

Now `i == homes.length`, so `R = 2` works.

Check `R = 1`:

- Locker `4` covers `[3, 5]`
- First home is `2`, which is left of `3`
- Immediate failure

So the minimum valid radius is `2`.

## ⏱ Complexity Analysis
### Time Complexity
Sorting dominates preprocessing: `O(n log n + m log m)`. Each feasibility check is a linear `O(n + m)` scan, and binary search performs `O(log U)` checks where `U` is the radius range, at most about 31 for 32-bit integers. At million-scale inputs this remains practical; at billion-scale element counts, sorting cost and memory bandwidth become the limiting factors.

### Space Complexity
`O(1)` auxiliary space beyond the sort implementation, assuming in-place sorting semantics from the language runtime. In practice, many standard library sorts use `O(log n)` stack space or temporary buffers. Space can only be reduced marginally; the main trade-off is algorithmic simplicity versus custom external-memory sorting for extreme datasets.

## 💡 Key Takeaways
- If the problem asks for the **minimum threshold** that makes a global condition true, check for a monotonic feasibility function and consider binary search on the answer.
- If inputs are positions on a line and coverage or matching is local, sorting often converts an all-pairs problem into a linear greedy scan.
- Be careful with the failure case `home < locker - R`: once that happens for the current locker, later lockers cannot rescue that home.
- Binary search boundaries and midpoint updates must be written to return the **first feasible** radius, not just any feasible radius.
- At production scale, the win is not just asymptotic; ordered scans are far more predictable for cache behavior, latency, and operational cost than repeated nearest-neighbor brute force.

## 🚀 Variations & Further Practice
- **Nearest facility with dynamic updates**: lockers are inserted or removed online; the harder twist is replacing one-time sorting with balanced trees or interval indexes.
- **2D coverage radius**: homes and lockers are points on a plane; the conceptual jump is that linear ordering disappears, so the greedy scan no longer works directly.
- **Weighted or capacity-constrained lockers**: each locker can cover only `k` homes or has asymmetric reach; feasibility becomes more complex than simple interval coverage.