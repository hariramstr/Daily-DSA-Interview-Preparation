# Tile a Board with Dominoes and Trominoes

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Recursion and Backtracking &nbsp;|&nbsp; **Tags:** Backtracking, Recursion, Grid, Combinatorics

---

## 🗂 Problem Overview

Given an `m × n` grid and two tile types — a domino (2 cells, horizontal or vertical) and an L-shaped tromino (3 cells, 4 rotations) — enumerate every distinct complete tiling of the board. Each valid tiling covers every cell exactly once. The output is a count (and optionally the labeled grids) of all such tilings. The non-trivial constraint: tromino rotations multiply the branching factor significantly, making naive enumeration expensive without disciplined pruning.

---

## 🌍 Engineering Impact

This pattern — exhaustive placement with backtracking over a constrained 2D space — appears directly in physical layout engines (PCB auto-routing, VLSI cell placement), warehouse slot-packing optimizers, and game-map procedural generation. At scale, the backtracking frame becomes a branch-and-bound search; without systematic pruning and canonical ordering, state-space explosion renders these systems unusable. The same "fill the next empty cell, try all shapes, undo on failure" discipline underlies constraint propagation in SAT solvers and Kubernetes bin-packing schedulers.

---

## 🔍 Problem Statement

**Input:** Two integers `m` and `n` (1 ≤ m, n ≤ 4).

**Output:** The count of all distinct complete tilings of the `m × n` board.

**Tile types:**
- **Domino:** covers 2 adjacent cells — horizontal `(r,c),(r,c+1)` or vertical `(r,c),(r+1,c)`.
- **Tromino:** covers 3 cells in any of 4 L-shaped rotations.

**Examples:**

| Input | Output | Notes |
|-------|--------|-------|
| m=2, n=2 | 2 | Two horizontal or two vertical dominoes |
| m=2, n=3 | 3 | Three configurations using dominoes only |

**Edge cases:** Boards where `m × n` is not divisible by 2 or 3 simultaneously may still have valid tilings if tromino and domino counts combine correctly. Boards with `m × n` not expressible as `2a + 3b` (a,b ≥ 0) have zero tilings — detect this early.

**Driving constraint:** The 4 × 4 ceiling keeps total cells at 16, making full backtracking enumeration tractable, but the branching factor (up to 6 tile placements per step) demands canonical cell-ordering to avoid redundant state visits.

---

## 🪜 How to Solve This

1. **Observe the structure** → Every valid tiling must cover every cell. If you always place the next tile at the first uncovered cell (scanning left-to-right, top-to-bottom), you impose a canonical ordering that eliminates duplicate solution paths.

2. **Canonical ordering is the key insight** → Without it, the same physical tiling can be constructed in multiple orders, inflating your count. Anchoring each tile placement to the top-left-most uncovered cell means each tiling is constructed in exactly one way.

3. **Enumerate tile shapes at the anchor** → At the current anchor cell, try every tile shape and rotation that *includes* that cell as its top-left-most cell. This is a finite, small set — at most 6 candidates (2 domino orientations + 4 tromino rotations).

4. **Bounds-check and overlap-check** → For each candidate placement, verify all target cells are within the grid and currently uncovered.

5. **Recurse and backtrack** → Mark cells, recurse to the next uncovered cell, then unmark. Count solutions at the base case (no uncovered cells remain).

6. **Early exit on impossibility** → If remaining uncovered cells can't be expressed as `2a + 3b`, prune immediately.

---

## 🧩 Algorithm Walkthrough

**Pattern: Backtracking with Canonical Cell Ordering**

This is a classic exact-cover problem solved via backtracking. The canonical ordering invariant is what makes it correct and efficient.

1. **Find the anchor:** Scan the grid row-by-row, left-to-right. The first `0` (uncovered) cell is the anchor `(r, c)`. If none exists, increment the solution count and return — the board is fully tiled.

2. **Generate candidate placements:** Predefine all tile shapes as sets of relative offsets from an anchor. For dominoes: `{(0,0),(0,1)}` and `{(0,0),(1,0)}`. For trominoes: 4 L-shaped offset sets, each including `(0,0)` as the minimum cell. Only shapes where `(0,0)` is the lexicographic minimum offset are valid anchors — this enforces the canonical ordering invariant.

3. **Validate placement:** For each candidate, check every target cell is in-bounds and uncovered.

4. **Place and recurse:** Assign a new label to all cells in the tile. Recurse. On return, reset those cells to `0`.

5. **Invariant maintained:** Because the anchor is always the first uncovered cell, and we only place tiles whose top-left cell is the anchor, no tiling is ever constructed twice.

6. **Pruning:** After placing a tile, check if the remaining uncovered cell count can still be partitioned into groups of 2 and 3. If not, skip recursion.

---

## 📊 Worked Example

**Input:** m=2, n=2. Trace scanning left-to-right, top-to-bottom.

| Step | Anchor | Tile Tried | Placement | Board State | Action |
|------|--------|------------|-----------|-------------|--------|
| 1 | (0,0) | H-domino | (0,0),(0,1) | `[[1,1],[0,0]]` | Recurse |
| 2 | (1,0) | H-domino | (1,0),(1,1) | `[[1,1],[2,2]]` | Recurse |
| 3 | — | — | — | Full board | **Count = 1** |
| 4 | (0,0) | V-domino | (0,0),(1,0) | `[[1,0],[1,0]]` | Recurse |
| 5 | (0,1) | V-domino | (0,1),(1,1) | `[[1,2],[1,2]]` | Recurse |
| 6 | — | — | — | Full board | **Count = 2** |

No tromino fits a 2×2 board (3 cells can't tile 4 evenly with trominoes alone). Total: **2 tilings**.

---

## ⏱ Complexity Analysis

### Time Complexity

**O(k^(m·n))** where `k ≤ 6` is the max branching factor per anchor cell and `m·n ≤ 16`. In practice, the canonical ordering prunes the tree aggressively — most branches fail within 2–3 steps. At the given constraints this is effectively constant-bounded; the approach does not scale to large grids.

### Space Complexity

**O(m·n)** for the recursion stack depth (at most `m·n / 2` frames, one per tile placed) plus the board grid itself. No memoization structure is needed. Stack depth peaks at 8 frames for a 4×4 board — negligible.

---

## 💡 Key Takeaways

- **Pattern signal — "enumerate all complete coverings of a grid":** This is exact-cover. Reach for backtracking with canonical cell ordering before considering anything more complex.
- **Pattern signal — small fixed bounds with combinatorial structure:** When constraints cap the input size (here, 4×4), exhaustive backtracking is often the intended and correct approach — don't over-engineer toward DP prematurely.
- **Gotcha — tromino anchor definition:** Only include a tromino rotation as a candidate at anchor `(r,c)` if `(r,c)` is the lexicographically smallest cell in that shape. Missing this produces duplicate counts that are hard to debug.
- **Gotcha — early feasibility check:** Forgetting to prune when remaining cells can't be covered by any combination of 2s and 3s wastes significant time on 3×3 and 4×3 boards where many partial states are dead ends.
- **Architectural insight:** The canonical-ordering trick — always acting on the minimum uncovered element — is the same principle behind topological sort, BFS level ordering, and priority-queue-driven schedulers. It converts an unordered search into a deterministic traversal, eliminating redundant paths at the cost of a single invariant to maintain.

---

## 🚀 Variations & Further Practice

- **Exact Cover with Pentominoes (12 shapes, 8×8 board):** The same backtracking frame applies, but the branching factor and shape library explode. The practical solution requires Algorithm X (Knuth's Dancing Links) — a disciplined exact-cover solver that makes backtracking over sparse constraint matrices tractable.
- **Count tilings modulo a prime (LeetCode 790 — Domino and Tromino Tiling):** Restricts to 2×n boards and asks only for the count mod 10^9+7. The twist: the recurrence structure becomes apparent, enabling an O(n) DP solution — a reminder that backtracking gives you the general answer but algebraic structure can collapse complexity by orders of magnitude.
- **Tiling with weighted tiles (optimization variant):** Assign costs to tile placements and find the minimum-cost complete tiling. Backtracking becomes branch-and-bound; the feasibility pruning from the counting problem becomes a lower-bound pruning step, directly connecting combinatorial enumeration to combinatorial optimization.