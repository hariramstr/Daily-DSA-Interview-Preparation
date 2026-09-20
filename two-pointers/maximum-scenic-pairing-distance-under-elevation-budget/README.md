# Maximum Scenic Pairing Distance Under Elevation Budget

**Difficulty:** Hard &nbsp;|&nbsp; **Topic:** Two Pointers &nbsp;|&nbsp; **Tags:** Two Pointers, Sorting, Greedy, Data Structures

---

## 🗂 Problem Overview
Given an unsorted array `elevations` and an integer `budget`, choose indices `i < j` such that `|elevations[i] - elevations[j]| <= budget` and maximize `(j - i) * min(elevations[i], elevations[j])`. Return that maximum scenic value, or `0` if no valid pair exists. The difficulty is that the score depends on both index distance and height, while validity depends on value proximity, so neither a plain two-pointer scan on indices nor brute-force pair enumeration is sufficient.

## 🌍 Engineering Impact
This pattern shows up when a system must optimize a composite objective under a local compatibility constraint. Examples include ad-slot pairing under latency skew budgets, cross-region replica selection under version drift limits, search-result diversification with relevance-distance trade-offs, and stream joins where events must be “close enough” in value but still maximize downstream utility. At scale, naive all-pairs evaluation collapses under quadratic cost. The right approach combines ordering, window maintenance, and a query structure so candidate sets stay small and answerable online. That enables predictable latency, bounded memory growth, and designs that still work when inputs reach hundreds of thousands or millions of records.

## 🔍 Problem Statement
You are given `elevations`, an array of length `n` (`2 <= n <= 200000`), where `elevations[i]` is the height of viewpoint `i`, and an integer `budget` (`0 <= budget <= 10^9`). A pair `(i, j)` is valid when `i < j` and `|elevations[i] - elevations[j]| <= budget`.

Its scenic value is:

`scenicValue(i, j) = (j - i) * min(elevations[i], elevations[j])`

Return the maximum scenic value over all valid pairs. If no valid pair exists, return `0`.

Examples:

- `elevations = [8, 1, 6, 2, 5, 7], budget = 2` → `35`
- `elevations = [3, 10, 4, 9, 2], budget = 0` → `0`

The decisive constraint is `n = 200000`: `O(n^2)` is not viable. The array is unsorted, and the budget applies to heights, not positions, so the algorithm must reconcile value-range filtering with index-distance maximization.

## 🪜 How to Solve This
1. Read the formula → the score is `distance * lower height`. That looks like a container-style objective, but the validity rule is on **height difference**, not on width or monotonic ordering.

2. Notice what the budget really says → for a viewpoint of height `h`, only partners with height in `[h - budget, h + budget]` matter. That suggests sorting by height so valid partners become a sliding value window.

3. After sorting by height, ask what each item needs → among previously seen viewpoints in the active height window, we want the one that maximizes `(index distance) * min height`.

4. Split by which height is smaller. If we process heights in ascending order, then for any earlier active item, its height is `<=` current height, so `min(...)` becomes the earlier height. The candidate score is `earlierHeight * (currentIndex - earlierIndex)`.

5. Rewrite that as `(earlierHeight * currentIndex) - (earlierHeight * earlierIndex)`. For fixed current index, maximizing this is equivalent to querying the best line `y = m*x + b` with `m = earlierHeight`, `b = -earlierHeight * earlierIndex`.

6. That points to a sliding window over sorted heights plus a dynamic max-query structure over lines: a Li Chao tree or equivalent. Add lines as heights enter the budget window, remove them lazily as they leave, and query at each current index.

## 🧩 Algorithm Walkthrough
1. **Sort viewpoints by elevation** as pairs `(height, index)`.  
   This is the enabling transformation: the budget constraint `|h_i - h_j| <= budget` becomes a contiguous window in sorted-by-height order.

2. **Sweep the sorted array from left to right** with two pointers.  
   Let `r` be the current viewpoint and `l` the first viewpoint whose height is still within `budget` of `height[r]`. All items in `[l, r-1]` are valid earlier partners by height.

3. **Maintain a dynamic set of candidate lines** for active earlier viewpoints.  
   For an active viewpoint `(h, idx)`, define a line:
   `f(x) = h * x - h * idx`  
   Querying this line at `x = currentIndex` yields `h * (currentIndex - idx)`, exactly the scenic value when `h` is the smaller height. Since we process in nondecreasing height order, that invariant holds.

4. **Handle window expiration lazily.**  
   When `height[r] - height[l] > budget`, viewpoints at `l` are no longer valid for future items. Instead of deleting eagerly from every node, tag each inserted line with an id and keep an `active[id]` flag. Li Chao nodes store candidate ids; on query, discard inactive ones lazily.

5. **Query before inserting the current viewpoint.**  
   This prevents pairing an item with itself and ensures only earlier indices contribute. The query at `x = index[r]` returns the best valid scenic value ending at `r`.

6. **Insert the current viewpoint’s line** and continue.  
   The invariant after each step: the structure contains exactly the active earlier viewpoints whose heights differ from future candidates by at most `budget`.

This is a **Two Pointers + Sorting + Dynamic Line Query** solution. Two pointers isolate the valid value-range window; the line structure converts “best earlier partner” from linear scan to logarithmic query.

## 📊 Worked Example
Use `elevations = [8, 1, 6, 2, 5, 7]`, `budget = 2`.

Sorted `(height, index)`:
`[(1,1), (2,3), (5,4), (6,2), (7,5), (8,0)]`

| r | current `(h, idx)` | active window by height | best query at `x=idx` | answer |
|---|---|---|---|---|
| 0 | (1,1) | none | none | 0 |
| 1 | (2,3) | (1,1) | `1*(3-1)=2` | 2 |
| 2 | (5,4) | none after shrinking | none | 2 |
| 3 | (6,2) | (5,4) | `5*(2-4)=-10` invalid for max, ignore | 2 |
| 4 | (7,5) | (5,4),(6,2) | max of `5*(5-4)=5`, `6*(5-2)=18` | 18 |
| 5 | (8,0) | (6,2),(7,5) after shrinking | negative distances only | 18 |

That trace only captures “earlier in height order” partners. The pair `(8,0)` with `(7,5)` is discovered when processing `(8,0)` is impossible because index order is reversed. To cover both index directions, run the same sweep twice: once on original indices, once on mirrored indices `n-1-i`, and take the maximum. The mirrored pass finds scenic value `7*(5-0)=35`.

## ⏱ Complexity Analysis
### Time Complexity
`O(n log n + n log C)`, where `C` is the index domain size used by the line-query structure. Sorting costs `O(n log n)`, and each viewpoint is inserted and queried once, with window movement amortized `O(n)`. At million-scale inputs this remains practical; quadratic work does not. At billion-scale, even sorting becomes the dominant architectural bottleneck.

### Space Complexity
`O(n)` for the sorted viewpoint list, active flags for lazy deletion, and the dynamic Li Chao tree nodes. You can reduce some constant factors with coordinate compression and iterative storage, but not the overall asymptotic footprint without giving up logarithmic max-query performance.

## 💡 Key Takeaways
- If a pairwise constraint becomes contiguous after sorting by one attribute, that is a strong signal for combining sorting with a sliding two-pointer window.
- If the objective over candidates can be algebraically rewritten as `m*x + b`, consider a line-query structure instead of scanning the active set.
- Query the structure before inserting the current element; otherwise self-pairing silently corrupts the result.
- One directional sweep is insufficient here because height order and index order are independent; you must account for both index orientations.
- In production systems, the transferable idea is to separate **eligibility maintenance** from **best-candidate selection**: use one mechanism to keep the valid window small and another to answer the optimization query fast.

## 🚀 Variations & Further Practice
- Require the pair to satisfy both `|height[i] - height[j]| <= budget` and `j - i <= maxGap`; now the active set is constrained by both value range and index range, forcing dual-window maintenance.
- Replace `min(elevations[i], elevations[j])` with `max(...)` or `elevations[i] + elevations[j]`; the algebra changes, and the same line formulation may no longer apply directly.
- Support online updates to elevations with interleaved queries; the static sort-plus-sweep breaks, pushing the design toward balanced trees, segment trees, or offline batching strategies.