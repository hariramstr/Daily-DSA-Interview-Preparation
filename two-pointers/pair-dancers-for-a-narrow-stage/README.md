# Pair Dancers for a Narrow Stage

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Two Pointers &nbsp;|&nbsp; **Tags:** Two Pointers, Greedy, Sorting

---

## 🗂 Problem Overview
Given an array `widths` and an integer `stageLimit`, assign every dancer to a stage mark that holds at most two dancers whose combined width does not exceed the limit. Dancers may be reordered arbitrarily, and some may stand alone. Return the minimum number of marks required. The non-trivial part is global optimization: a locally valid pairing is not necessarily optimal, and brute-forcing pair combinations is infeasible at `10^5` elements.

## 🌍 Engineering Impact
This pattern shows up anywhere a system must pack entities into fixed-capacity slots while minimizing resource count: ride-sharing seat allocation, warehouse carton packing under small cardinality limits, CPU task co-location under memory caps, and network batch formation constrained by payload size. At scale, naive matching strategies fragment capacity and inflate infrastructure cost. The sort-plus-greedy approach matters because it converts an exponential pairing space into a predictable `O(n log n)` pipeline. That difference is the boundary between something that works in production request paths or schedulers and something that collapses under high-cardinality workloads.

## 🔍 Problem Statement
You are given:

- `widths`, where `1 <= widths.length <= 100000`
- `widths[i]`, the width of the `i`-th dancer
- `stageLimit`, where `1 <= widths[i] <= stageLimit <= 1000000000`

Each stage mark can hold either:

- one dancer, or
- two dancers whose combined width is `<= stageLimit`

Every dancer must be assigned to exactly one mark. You may reorder dancers before assignment. The objective is to minimize the total number of marks used.

Examples:

- `widths = [2, 3, 4, 5], stageLimit = 6` → `3`
- `widths = [1, 2, 2, 3, 4], stageLimit = 4` → `3`

The key constraint is input size: `10^5` dancers rules out any approach that tries all pairings or repeatedly searches for partners in quadratic time.

## 🪜 How to Solve This
1. Read the problem → notice the objective is not “find valid pairs,” but “minimize containers.” That immediately suggests greedy packing rather than enumeration.

2. Reordering is allowed → sorting is almost always the first move. Once widths are ordered, the pairing decision becomes structured instead of combinatorial.

3. Ask what to do with the widest remaining dancer. They are the hardest to place, so delaying them is risky. If they cannot pair with the narrowest remaining dancer, they cannot pair with anyone.

4. That observation suggests two pointers:
   - left = narrowest unassigned dancer
   - right = widest unassigned dancer

5. Try to pair `widths[left] + widths[right]`.
   - If it fits, use one mark and move both pointers.
   - If it does not fit, the widest dancer must go alone; move only `right`.

6. Repeat until all dancers are assigned. Each iteration consumes exactly one mark, and every decision is forced by the sorted order. That is why the greedy choice is safe rather than heuristic.

## 🧩 Algorithm Walkthrough
1. **Sort the widths in non-decreasing order.**  
   This enables ordered reasoning about who can pair with whom. After sorting, the left side contains the cheapest possible partners and the right side contains the hardest dancers to place.

2. **Initialize two pointers:** `left = 0`, `right = n - 1`, and `marks = 0`.  
   The invariant is that all dancers outside `[left, right]` have already been assigned optimally under the greedy strategy.

3. **Inspect the widest remaining dancer at `right`.**  
   This dancer must be assigned now. Any optimal solution must dedicate one mark to them either alone or with some partner.

4. **Check whether the narrowest remaining dancer can pair with them.**  
   If `widths[left] + widths[right] <= stageLimit`, pair them and increment `left`. This is correct because using the lightest feasible partner preserves larger widths for future pairings; any heavier partner would only reduce future options.

5. **Always decrement `right` and increment `marks`.**  
   Whether paired or alone, the widest dancer is now assigned. The invariant remains: the remaining interval contains only unassigned dancers.

6. **Continue until `left > right`.**  
   Each loop consumes one mark and removes at least one dancer. This is the classic **Two Pointers + Greedy** pattern: sorted order gives a monotonic feasibility test, and the greedy choice is justified by the “if smallest cannot fit with largest, nobody can” property.

## 📊 Worked Example
Example: `widths = [1, 2, 2, 3, 4]`, `stageLimit = 4`

Sorted widths: `[1, 2, 2, 3, 4]`

| Step | left | right | Pair Checked | Decision | Marks |
|------|------|-------|--------------|----------|-------|
| 1 | 0 (`1`) | 4 (`4`) | `1 + 4 = 5` | `4` goes alone | 1 |
| 2 | 0 (`1`) | 3 (`3`) | `1 + 3 = 4` | pair `(1,3)` | 2 |
| 3 | 1 (`2`) | 2 (`2`) | `2 + 2 = 4` | pair `(2,2)` | 3 |

Pointers cross, so all dancers are assigned.

Result: `3` marks.

The trace shows the key greedy fact: once `1` cannot pair with `4`, no other dancer can pair with `4`, because every other remaining dancer is at least as wide as `1`.

## ⏱ Complexity Analysis

### Time Complexity
`O(n log n)` due to sorting, followed by a single `O(n)` two-pointer scan. The sort dominates. At `10^6` elements this is still practical in optimized runtimes; at `10^9`, even linear scanning becomes a systems problem, so the algorithm is asymptotically right but operationally constrained by memory and I/O.

### Space Complexity
`O(1)` auxiliary space if sorting is done in place, excluding the sort implementation’s internal stack or buffer costs. If the language runtime uses non-in-place sorting, effective space may be `O(n)`. Reducing that usually means choosing a lower-level in-place sort with more implementation complexity.

## 💡 Key Takeaways
- If the problem allows reordering and asks to minimize the number of 2-item groups under a capacity limit, think sorted greedy with two pointers immediately.
- When the hardest-to-place item can be tested against the easiest possible partner, you likely have a monotonic condition that makes greedy provable.
- Be careful with the single remaining dancer case: when `left == right`, that dancer still needs one mark.
- Do not increment `left` unless a pair actually fits; otherwise you silently drop an unassigned dancer and undercount marks.
- In production scheduling and packing systems, sorting to expose monotonic structure often turns an intractable matching space into a deterministic, scalable allocator.

## 🚀 Variations & Further Practice
- **Boats to Save People**: identical core pattern; the conceptual twist is recognizing the same abstraction under different domain language.
- **Minimize bins when each bin can hold more than two items**: becomes a harder bin-packing class where this greedy strategy is no longer generally optimal.
- **Online arrivals instead of full batch input**: harder because sorting the complete set is impossible up front, forcing approximation, buffering, or balanced-search data structures.