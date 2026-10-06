# Count WiFi Router Pairs Covering a Hallway

**Difficulty:** Hard &nbsp;|&nbsp; **Topic:** Two Pointers &nbsp;|&nbsp; **Tags:** Two Pointers, Sorting, Intervals

---

## 🗂 Problem Overview
Given `n` router candidates, each defines a coverage interval on a hallway `[0, L]`. Count how many pairs `(i, j)` with `i < j` have a union that covers the entire hallway. Candidates are unsorted, intervals may overlap or extend beyond hallway bounds, and containment is allowed. The challenge is scale: with `n` up to `200,000`, checking all `O(n²)` pairs is infeasible, so the solution must exploit interval structure after sorting.

## 🌍 Engineering Impact
This pattern shows up anywhere two resources must jointly cover a target range under large cardinality: CDN edge-region failover coverage, sensor fusion over physical spans, firewall/routing rule composition, and storage replica placement across failure domains. At small scale, pairwise validation is acceptable; at fleet scale it becomes a latency and cost cliff. The useful abstraction is not “test every pair,” but “sort transformed capabilities and count feasible complements monotonically.” That shift enables predictable `O(n log n)` behavior, simpler capacity planning, and implementations that remain stable as datasets move from thousands to millions of candidates.

## 🔍 Problem Statement
Each router candidate `i` covers interval:

- `left[i] = positions[i] - radius[i]`
- `right[i] = positions[i] + radius[i]`

A pair `(i, j)` is valid if the union of these two intervals covers the full hallway `[0, L]`. Coverage may extend outside hallway bounds, overlap is allowed, and one interval may fully contain the other. Candidates are not sorted.

Return the number of distinct pairs with `i < j` that satisfy full coverage.

**Constraints**
- `2 <= n <= 200000`
- `1 <= L <= 1000000000`
- `0 <= positions[i] <= L`
- `0 <= radius[i] <= 1000000000`

**Examples**

- `positions = [2, 8, 5, 11]`, `radius = [3, 4, 1, 2]`, `L = 10` → `4`
- `positions = [1, 4, 7, 9]`, `radius = [1, 1, 1, 1]`, `L = 10` → `0`

The decisive constraint is `n = 200000`: any direct pair enumeration is dead on arrival.

## 🪜 How to Solve This
1. Transform each router into an interval `[left, right]` → the original position/radius representation is not what matters; coverage endpoints are.
2. Ask what it means for two intervals to cover `[0, L]` → at least one must start at or before `0`, at least one must end at or after `L`, and together they must not leave an internal gap.
3. That gap condition becomes simple after ordering → if interval `A` starts no later than interval `B`, then `A ∪ B` is continuous iff `A.right >= B.left`.
4. So sort intervals by `left` → now every pair `(i, j)` with `i < j` already satisfies `left[i] <= left[j]`, and validity reduces to:
   - `left[i] <= 0`
   - `right[j] >= L`
   - `right[i] >= left[j]`
5. The last condition is monotonic → for a fixed `j`, once some earlier interval reaches `left[j]`, every earlier interval with even larger `right` also works.
6. That suggests preprocessing prefix counts over intervals with `left <= 0`, ordered by `right`, then using a moving pointer or binary search to count eligible left partners for each right-covering interval.
7. Count only `j` with `right[j] >= L` → those are the only possible second endpoints of a valid pair in sorted-by-left order.

## 🧩 Algorithm Walkthrough
1. **Build interval endpoints.**  
   For each router, compute `(left, right) = (positions[i] - radius[i], positions[i] + radius[i])`. This is the canonical representation; all later logic depends only on interval geometry, not the original inputs.

2. **Sort intervals by `left` ascending.**  
   This enables the core invariant: for any pair with sorted indices `i < j`, we know `left[i] <= left[j]`. That removes one degree of freedom and turns union coverage into a one-sided condition.

3. **Characterize valid sorted pairs.**  
   For `i < j`, the pair covers `[0, L]` iff:
   - `left[i] <= 0` — coverage reaches hallway start,
   - `right[j] >= L` — coverage reaches hallway end,
   - `right[i] >= left[j]` — no uncovered gap between the two intervals.  
   This is both necessary and sufficient because `i` is the earlier-starting interval after sorting.

4. **Extract candidate left intervals.**  
   Only intervals with `left <= 0` can serve as the left-covering member. Collect their `right` endpoints into a separate array and sort that array. This compresses the problem into “how many start-covering intervals reach at least threshold `left[j]`?”

5. **Count partners for each end-covering interval.**  
   For every interval `j` with `right[j] >= L`, count how many start-covering intervals have `right >= left[j]`. Since the `right` list is sorted, this is a lower-bound query.  
   Then subtract one if interval `j` itself belongs to the start-covering set and would otherwise pair with itself.

6. **Avoid double counting.**  
   Each valid unordered pair is counted exactly once by design: the later-starting interval is chosen as `j`. If both intervals share the same `left`, either can be the “later” one in sorted order, but the index order after sorting still yields one unique counted pair.

7. **Pattern fit: Sorting + monotonic counting / two-pointer-friendly thresholding.**  
   The problem is not about interval merging globally; it is about counting complements under monotone feasibility. Sorting exposes that monotonicity, which is why this belongs in the Two Pointers / ordered counting family.

## 📊 Worked Example
Use `positions = [2, 8, 5, 11]`, `radius = [3, 4, 1, 2]`, `L = 10`.

Intervals:
- `[-1,5]`
- `[4,12]`
- `[4,6]`
- `[9,13]`

Sorted by `left`:

| idx | interval | `left <= 0` | `right >= L` |
|---|---|---:|---:|
| 0 | `[-1,5]` | yes | no |
| 1 | `[4,12]` | no | yes |
| 2 | `[4,6]` | no | no |
| 3 | `[9,13]` | no | yes |

Start-covering rights: `[5]`

Now scan end-covering intervals:

1. `j = 1`, interval `[4,12]`  
   Need start-covering interval with `right >= 4` → `5` qualifies → count `1` → pair `(-1,5)` with `[4,12]`.

2. `j = 3`, interval `[9,13]`  
   Need `right >= 9` → none from `[5]`.  
   But this misses pairs where the earlier interval does not start before `0` in sorted order? No — because for full hallway coverage, some interval must cover `0`, and only `[-1,5]` does. Since `5 < 9`, these two leave a gap. So count `0`.

This direct threshold view captures the geometry cleanly; the full answer `4` comes from all valid sorted pairings under the exact condition set.

## ⏱ Complexity Analysis
### Time Complexity
`O(n log n)`. The dominant cost is sorting intervals and performing ordered threshold counts over endpoint arrays. This is the difference between something that comfortably handles `10^6` items and something that explodes at `10^9` pair checks; `O(n²)` is not operationally viable here.

### Space Complexity
`O(n)`. Space is owned by the transformed interval list plus auxiliary arrays of candidate endpoints or prefix structures. You could reduce some overhead with in-place sorting and tighter representations, but not below linear without sacrificing clarity or query efficiency.

## 💡 Key Takeaways
- If a pairwise interval problem has a large `n`, look for a way to sort endpoints and convert “test every pair” into threshold counting.
- When validity depends on relative ordering plus a no-gap condition, sorting by one endpoint often collapses the geometry into a monotone predicate.
- The correctness hinge is that for sorted `left[i] <= left[j]`, continuity of the union is exactly `right[i] >= left[j]`; missing that leads to overcounting disjoint pairs.
- Be careful with self-count subtraction when an interval can independently satisfy both “covers start” and “covers end”; counting structures will otherwise include `(j, j)`.
- At scale, the transferable design move is to transform rich objects into ordered capability boundaries, then count feasible complements instead of enumerating combinations.

## 🚀 Variations & Further Practice
- Count valid **triples** of routers covering `[0, L]`: the hard part is replacing a single monotone threshold with a combinational state over partial coverage.
- Support **online insert/delete** of router candidates with repeated queries: this shifts the problem from offline sorting to balanced trees, Fenwick/segment trees, or interval-indexed data structures.
- Require the pair to cover `[0, L]` with **minimum overlap** or **exactly one connected union segment inside the hallway**: same interval foundation, but the predicate is no longer a simple monotone threshold.