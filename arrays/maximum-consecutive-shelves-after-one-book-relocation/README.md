# Maximum Consecutive Shelves After One Book Relocation

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Arrays &nbsp;|&nbsp; **Tags:** Arrays, Sliding Window, Prefix Sum

---

## 🗂 Problem Overview
Given a binary array `shelves`, return the maximum length of consecutive `1`s obtainable after relocating at most one `1` to a `0`. The move preserves the total number of `1`s, so you are not flipping bits arbitrarily; you are reassigning one existing featured book. The non-trivial part is deciding when a zero between two runs can actually be filled: that merge is valid only if some other `1` exists outside the merged segment to relocate.

## 🌍 Engineering Impact
This pattern shows up anywhere one scarce resource can be reassigned once to maximize local continuity: shard rebalancing in distributed caches, slot compaction in storage engines, burst smoothing in streaming pipelines, or contiguous allocation in schedulers. At scale, the wrong approach is usually quadratic neighborhood inspection or repeated simulation of every possible move. That collapses under large arrays, hot paths, or online evaluation. The useful abstraction is local window optimization under a global conservation constraint: total capacity stays fixed, but one relocation can improve locality, throughput, or compression if and only if surplus exists elsewhere.

## 🔍 Problem Statement
You are given an array `shelves` of length `n` where `1 <= n <= 200000` and each value is either `0` or `1`. A `1` represents a featured book, and a `0` represents an empty position. You may perform at most one relocation: remove a `1` from any index and place it into any index currently holding `0`. The total number of `1`s therefore remains unchanged.

Return the maximum possible length of a consecutive block of `1`s after this optional move.

Key edge case: if a zero separates two runs of `1`s, you can fill that gap only when there is at least one extra `1` somewhere outside those runs.

Examples:

- `shelves = [1,1,0,1,1,0,1]` → `5`
- `shelves = [1,0,1,1,0,1]` → `4`

The `n = 200000` constraint rules out trying every relocation explicitly; the solution must be linear.

## 🪜 How to Solve This
1. Read the move carefully → this is not “flip one zero to one.” A new `1` must come from an existing `1`, so total count of `1`s is fixed.

2. That immediately suggests a global fact we should precompute: the total number of `1`s. Any candidate answer can never exceed that count.

3. Next, ask what a useful relocation can do:
   - extend one run by filling an adjacent zero, or
   - merge two runs separated by exactly one zero.

4. That means each zero is a potential bridge. If we know how many consecutive `1`s are directly to its left and right, we can evaluate the best block formed by filling it.

5. Prefix-style run lengths make this cheap: compute consecutive `1`s ending at each index from the left, and starting at each index from the right.

6. For each zero, candidate block = left run + right run, plus possibly one relocated `1`. But that extra `1` is only available if total `1`s exceeds `left + right`.

7. Also handle arrays with no zero or no useful move naturally by taking the maximum over all candidates and existing runs.

The core idea is local merge evaluation constrained by a global inventory count.

## 🧩 Algorithm Walkthrough
1. **Count total featured books.**  
   Compute `totalOnes = sum(shelves)`. This is a hard upper bound on the answer because relocation preserves the number of `1`s. Invariant: no candidate result may exceed `totalOnes`.

2. **Build left consecutive-run lengths.**  
   Create `left[i]` = number of consecutive `1`s ending at index `i`.  
   If `shelves[i] == 1`, then `left[i] = left[i-1] + 1`; otherwise `0`.  
   This captures the run immediately to the left of any zero in O(1) lookup.

3. **Build right consecutive-run lengths.**  
   Create `right[i]` = number of consecutive `1`s starting at index `i`.  
   If `shelves[i] == 1`, then `right[i] = right[i+1] + 1`; otherwise `0`.  
   Invariant: for any zero at `i`, `left[i-1]` and `right[i+1]` describe the two runs that zero could connect.

4. **Evaluate every zero as a relocation target.**  
   For each index `i` where `shelves[i] == 0`, let:
   - `L = i > 0 ? left[i-1] : 0`
   - `R = i + 1 < n ? right[i+1] : 0`
   - `merged = L + R`

   This is the number of existing `1`s adjacent to that gap.

5. **Apply the conservation rule.**  
   If `merged < totalOnes`, there exists at least one extra `1` elsewhere to relocate into this zero, so candidate = `merged + 1`.  
   Otherwise all `1`s are already in those adjacent runs, so candidate = `merged`.  
   This is the critical correctness condition that distinguishes relocation from free flipping.

6. **Track the maximum.**  
   The best answer over all zeros is the result. This is a **Prefix Sum / precomputed run-length** pattern with local gap inspection, functionally similar to a constrained sliding-window merge. It is the right abstraction because each decision depends only on local adjacency plus one global count, enabling linear time.

## 📊 Worked Example
Consider `shelves = [1,1,0,1,1,0,1]`.

| i | shelves[i] | left | right |
|---|------------|------|-------|
| 0 | 1 | 1 | 2 |
| 1 | 1 | 2 | 1 |
| 2 | 0 | 0 | 0 |
| 3 | 1 | 1 | 2 |
| 4 | 1 | 2 | 1 |
| 5 | 0 | 0 | 0 |
| 6 | 1 | 1 | 1 |

`totalOnes = 5`

Evaluate zeros:

1. `i = 2`  
   `L = left[1] = 2`, `R = right[3] = 2`, `merged = 4`  
   Since `merged < totalOnes`, one extra `1` exists elsewhere.  
   Candidate = `4 + 1 = 5`

2. `i = 5`  
   `L = left[4] = 2`, `R = right[6] = 1`, `merged = 3`  
   `merged < totalOnes`, so candidate = `4`

Maximum candidate is `5`.

A valid move is taking the `1` at index `6` and placing it at index `2`, producing a consecutive block of five `1`s.

## ⏱ Complexity Analysis

### Time Complexity
The algorithm runs in `O(n)`: one pass to count `1`s`, one left-to-right pass, one right-to-left pass, and one pass to evaluate zeros. For `10^6` elements this is routine in memory-resident workloads; for `10^9`, the linear scan is still optimal but memory layout and streaming constraints dominate.

### Space Complexity
The implementation uses `O(n)` extra space for the `left` and `right` arrays. That space is owned entirely by run-length precomputation. It can be reduced to `O(1)` with a more intricate sliding-window formulation, but at the cost of clarity and higher implementation risk.

## 💡 Key Takeaways
- If the problem allows one modification but preserves a global count, treat it as constrained reassignment, not unrestricted mutation.
- When a single gap may connect adjacent runs, precomputing left/right run lengths is usually simpler than simulating edits.
- The main trap is adding `+1` unconditionally at a zero; you may only do that if some `1` exists outside the adjacent runs.
- Boundary handling matters: for zeros at index `0` or `n-1`, missing left/right runs must be treated as length `0`.
- The production-grade insight is that local optimization often depends on a global inventory invariant; encode both explicitly instead of overfitting to local structure.

## 🚀 Variations & Further Practice
- Allow up to `k` relocations instead of one. The twist is that local gap filling becomes a budgeted optimization problem, typically pushing toward sliding window or run compression with greedy merging.
- Make the array circular. The harder part is handling runs that wrap from end to start without double-counting available `1`s`.
- Support online updates and repeated queries. The conceptual jump is moving from one-pass array logic to segment trees or interval structures that maintain run metadata incrementally.