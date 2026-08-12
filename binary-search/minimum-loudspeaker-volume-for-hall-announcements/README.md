# Minimum Loudspeaker Volume for Hall Announcements

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Binary Search &nbsp;|&nbsp; **Tags:** Binary Search, Greedy, Sorting

---

## 🗂 Problem Overview
Given booth positions on a line and a limit of `k` loudspeakers, place speakers only at existing booth positions so every booth lies within distance `R` of some speaker. Return the smallest integer `R` that makes full coverage possible. The challenge is not placement alone but minimizing the radius under large input constraints: up to `2 * 10^5` positions, unsorted input, and duplicates, which rules out brute-force search over placements.

## 🌍 Engineering Impact
This pattern shows up anywhere you minimize a capability threshold while validating feasibility under placement or capacity constraints. Examples include CDN edge placement radius, warehouse service zones, cellular small-cell coverage, sensor deployment, and batch window sizing in streaming systems. At scale, brute-forcing configurations explodes combinatorially; what matters is recognizing a monotonic decision boundary: “is threshold `X` sufficient?” Binary search over that threshold, paired with a linear feasibility check, turns an intractable optimization problem into something operationally predictable, fast, and easy to reason about under production load.

## 🔍 Problem Statement
You are given an integer array `positions` of length `n`, where each value is a booth position on a 1D hallway, and an integer `k` representing the maximum number of loudspeakers you may install. A loudspeaker placed at booth position `p` with radius `R` covers every booth in `[p - R, p + R]`. Speakers may only be installed at positions from the input array; positions are not sorted and may contain duplicates.

Return the minimum integer radius `R` such that all booths are covered using at most `k` loudspeakers.

Constraints:
- `1 <= n <= 2 * 10^5`
- `1 <= k <= n`
- `0 <= positions[i] <= 10^9`

Examples:
- `positions = [1, 2, 8, 12, 17], k = 2` → `4`
- `positions = [4, 4, 4, 10, 15, 21], k = 3` → `3`

The key constraint is `n = 2 * 10^5`: any approach that tries many placements explicitly is too slow.

## 🪜 How to Solve This
1. Sort the booth positions → coverage on a line becomes structured. Without sorting, “what is the next uncovered booth?” is undefined.

2. Notice the optimization target is a single integer radius `R` → that suggests binary search on the answer, not direct search over placements.

3. Ask the decision version: if radius is fixed to `R`, can we cover all booths with at most `k` speakers? This is much easier than directly minimizing `R`.

4. Observe monotonicity → if radius `R` works, any larger radius also works. That is the exact condition binary search needs.

5. For a fixed `R`, cover booths greedily from left to right:
   - take the leftmost uncovered booth,
   - place a speaker as far right as possible while still covering that booth,
   - that choice maximizes how far this speaker can extend coverage to the right.

6. Repeat until all booths are covered or you exceed `k` speakers.

This gives a clean decomposition: sort once, then binary search over `R`, using a linear greedy feasibility check.

## 🧩 Algorithm Walkthrough
1. **Sort `positions`.**  
   This converts the problem into ordered interval coverage on a line. Duplicates naturally collapse into adjacent equal values and require no special-case logic.

2. **Define the search range for `R`.**  
   The minimum possible radius is `0`. A safe upper bound is `positions[n - 1] - positions[0]`, which is enough for one speaker to potentially span the entire hallway if placement constraints allow.

3. **Use the pattern: Binary Search on Answer.**  
   For a candidate radius `R`, run a feasibility check `canCover(R)`. Because feasibility is monotonic, binary search finds the smallest working radius.

4. **Feasibility check with a Greedy sweep.**  
   Start at the leftmost uncovered booth `positions[i]`.  
   Find the rightmost booth position `<= positions[i] + R`; place the speaker there. This is the farthest valid placement that still covers the current booth.  
   That speaker then covers up to `speakerPos + R`. Advance `i` past every booth within that bound.

5. **Maintain the invariant.**  
   After each speaker placement, all booths strictly before index `i` are covered, and the number of speakers used is minimal for that prefix. Any earlier placement would not extend coverage farther right, so the greedy choice is optimal locally and therefore globally on a line.

6. **Binary search termination.**  
   If `canCover(mid)` succeeds, search left for a smaller radius; otherwise search right. The final `lo` is the minimum valid radius.

## 📊 Worked Example
Use `positions = [4, 4, 4, 10, 15, 21]`, `k = 3`.

Sorted positions are the same. Test `R = 3`.

| Step | Leftmost uncovered | Farthest valid speaker position | Covers through | Speakers used |
|---|---:|---:|---:|---:|
| 1 | 4 | 4 | 7 | 1 |
| 2 | 10 | 10 | 13 | 2 |
| 3 | 15 | 15 | 18 | 3 |
| 4 | 21 | 21 | 24 | 4 |

`R = 3` appears to need 4 speakers if we force coverage from each leftmost point independently. But the correct greedy move is to place for the leftmost uncovered booth at the farthest booth within `+R`, then skip all covered booths. Re-evaluating carefully:

- Speaker at `4` covers `4,4,4`
- Speaker at `15` covers `12..18`, so it covers `15`
- Speaker at `21` covers `18..24`, so it covers `21`

`10` remains uncovered, so `R = 3` still needs 4 speakers for this exact input. That means the example’s stated output is inconsistent; under the stated rules, the minimum radius is `5`, using speakers at `4`, `10`, and `21`.

## ⏱ Complexity Analysis
### Time Complexity
Sorting costs `O(n log n)`. Each feasibility check is `O(n)` with a single left-to-right sweep, and binary search performs `O(log D)` checks where `D = max(positions) - min(positions)`. Total complexity is `O(n log n + n log D)`. This scales comfortably for `2 * 10^5` elements; it would still be practical at low millions, but not for repeated full recomputation at billion-scale inputs.

### Space Complexity
The algorithm uses `O(1)` auxiliary space beyond the sort, or `O(log n)` stack space depending on the sorting implementation. The dominant storage is the input array itself. Space cannot be meaningfully reduced unless input mutation is disallowed, in which case a copied sorted array costs `O(n)`.

## 💡 Key Takeaways
- If the problem asks for the **minimum integer threshold** and feasibility becomes easier once the threshold is fixed, think **binary search on the answer**.
- If coverage happens on a **sorted 1D line** and local choices affect only the remaining suffix, a **left-to-right greedy sweep** is usually the right feasibility primitive.
- The speaker must be placed at an **existing booth position**, not at an arbitrary coordinate; ignoring that changes the answer.
- Be careful with the greedy step: place the speaker at the **rightmost booth that still covers the current uncovered booth**, then skip everything within that speaker’s right reach.
- In production systems, this is the standard move for turning expensive optimization into a monotonic decision problem plus a cheap validator.

## 🚀 Variations & Further Practice
- Allow speakers to be placed at **any real-valued coordinate**, not just booth positions. The feasibility check simplifies, but the proof and edge handling change.
- Extend from a line to **2D booth coordinates** with circular coverage. The monotonic threshold remains, but feasibility is no longer a simple greedy sweep.
- Add **per-speaker costs or heterogeneous radii**. Now you are balancing budgeted placement against coverage, which typically shifts the problem toward DP or more complex optimization.