# Tile the Grid with L-Shaped Trominoes

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Recursion and Backtracking &nbsp;|&nbsp; **Tags:** Backtracking, Recursion, Grid, Combinatorics

---

## 🗂 What Is This Problem? *(For Everyone)*

Imagine you have a rectangular floor and a supply of L-shaped tiles, each covering exactly three squares. Some floor squares are already occupied (blocked). The question is: in how many completely different ways can you lay down the L-shaped tiles so that every free square is covered exactly once, with no tiles overlapping or sticking out over the edge?

---

## 🌍 Why Does This Matter in the Real World? *(For Business Readers)*

Tiling and packing problems appear constantly in manufacturing, logistics, and software. Factories optimise how to cut L-shaped parts from sheet metal to minimise waste. Game developers use similar algorithms to procedurally generate puzzle levels. In chip design, engineers must fit irregular circuit blocks onto a fixed silicon grid — every uncovered or doubly-covered cell is wasted money. Understanding how many valid arrangements exist helps businesses choose the best layout automatically, reducing material costs, speeding up production, and improving product quality without manual trial and error.

---

## 🎯 The Challenge in Plain English *(For Everyone)*

Think of a bathroom floor with some tiles already cemented in place. You have a box of L-shaped tiles — each one covers exactly three squares in a corner shape. Your job is to count every possible way to finish tiling the floor using only those L-shaped pieces, making sure every bare square is covered once and no piece hangs off the edge or sits on a cemented square. Some arrangements may be impossible; in that case the answer is zero.

---

## 🔍 Technical Problem Statement *(For Developers)*

Given a grid of size `rows x cols` and a list of `blocked` cells, tile all remaining empty cells using L-shaped trominoes. Each tromino covers exactly 3 cells in one of four rotations:

- **Rotation 0:** `(r,c)`, `(r+1,c)`, `(r+1,c+1)`
- **Rotation 1:** `(r,c)`, `(r,c+1)`, `(r+1,c+1)`
- **Rotation 2:** `(r,c)`, `(r,c+1)`, `(r+1,c)`
- **Rotation 3:** `(r+1,c)`, `(r+1,c+1)`, `(r,c+1)`

Every non-blocked cell must be covered **exactly once**. Return the total number of distinct valid tilings modulo `10^9 + 7`, or `0` if no valid tiling exists.

**Constraints:** `1 ≤ rows, cols ≤ 6`; `0 ≤ blocked.length ≤ rows × cols`.

| Example | Input | Output | Reason |
|---------|-------|--------|--------|
| 1 | `rows=2, cols=3, blocked=[]` | `2` | Two distinct L-tromino arrangements fill the 2×3 grid |
| 2 | `rows=2, cols=3, blocked=[[0,2]]` | `0` | 5 free cells cannot be divided into groups of 3 |

---

## 🧩 Approach: How We Solve It *(For Developers)*

1. **Pre-check divisibility.** Count the total free cells (grid size minus blocked count). If this number is not divisible by 3, immediately return `0` — no arrangement of 3-cell pieces can ever cover a non-multiple of 3.

2. **Build a boolean grid.** Mark every blocked cell as occupied so the placer can quickly check availability.

3. **Find the first free cell (scan order).** Scan left-to-right, top-to-bottom and locate the first unoccupied cell `(r, c)`. This anchor point guarantees we always try to fill the "earliest" gap, preventing duplicate work and keeping the search space manageable.

4. **Try all four rotations.** For the anchor cell, attempt to place each of the four L-tromino rotations that include `(r, c)`. Before placing, verify all three target cells are inside the grid and currently free.

5. **Place and recurse.** Mark the three cells as occupied, then recurse to find the next free cell. This is the *backtracking* step — we explore one branch of the decision tree fully before trying the next.

6. **Undo (backtrack).** After the recursive call returns, unmark the three cells. This restores the grid so the next rotation or parent call sees a clean state.

7. **Accumulate and return.** Each successful recursion that covers all cells contributes `1` to the count. Sum all successful paths modulo `10^9 + 7`.

---

## 📊 Worked Example *(For Developers)*

**Input:** `rows=2, cols=3, blocked=[]` — a clean 2×3 grid, 6 free cells, needs exactly 2 trominoes.

| Step | Action | Grid State (`.` = free, `X` = covered) |
|------|--------|----------------------------------------|
| 1 | Free cells = 6; 6 % 3 == 0 ✓ | `...` / `...` |
| 2 | First free cell: `(0,0)` | — |
| 3 | Try Rotation 1 at `(0,0)` → covers `(0,0),(0,1),(1,1)` | `XX.` / `.X.` |
| 4 | Recurse → first free: `(0,2)` | — |
| 5 | Try Rotation 3 at `(0,1)` → covers `(0,2),(1,1),(1,2)` — `(1,1)` taken, skip | — |
| 6 | Try Rotation 0 at `(0,2)` → covers `(0,2),(1,2),(1,3)` — out of bounds, skip | — |
| 7 | Try Rotation 2 at `(0,2)` → covers `(0,2),(1,2),(1,1)` — `(1,1)` taken, skip | — |
| 8 | Try Rotation 3 at `(0,2)` → covers `(0,2),(1,1),(1,2)` — `(1,1)` taken, skip | — |
| 9 | Backtrack Rotation 1; try Rotation 2 at `(0,0)` → covers `(0,0),(0,1),(1,0)` | `XX.` / `X..` |
| 10 | Recurse → first free: `(0,2)` → Rotation 3 covers `(0,2),(1,1),(1,2)` ✓ | `XXX` / `XXX` |
| 11 | All cells covered → count += 1 | **count = 1** |
| 12 | Continue backtracking finds one more valid arrangement | **count = 2** |

**Final Output: `2`**

---

## ⏱ Performance Analysis *(For Developers + Technical Managers)*

### Time Complexity

**O(4^(N/3))** where N is the number of free cells. At each step we try up to 4 rotations, and we place one tromino (covering 3 cells) per recursive call. With a maximum grid of 6×6 = 36 cells, this yields at most 4^12 ≈ 16 million nodes — very manageable for the given constraints.

### Space Complexity

**O(N)** for the recursion call stack, where N is the number of free cells (at most 36). The grid itself is O(rows × cols), a constant bounded by 36 cells. No additional data structures grow with input size.

---

## 💡 Key Takeaways *(For Everyone)*

- **Early impossibility checks save enormous time** — verifying divisibility by 3 before any search eliminates entire problem instances instantly, a pattern valuable in any constraint-satisfaction business tool.
- **Counting arrangements has direct commercial value** — from factory floor layouts to game level generation, knowing *how many* valid configurations exist helps automate optimal design decisions.
- **Always anchor to the first free cell** — scanning in a fixed order ensures each unique tiling is counted exactly once, preventing duplicates without needing expensive equality checks.
- **Backtracking is "try, explore, undo"** — the power of this pattern is that it exhaustively searches all possibilities while reusing a single grid structure, keeping memory usage constant.
- **Modular arithmetic (`% 10^9 + 7`) prevents integer overflow** — when counts can grow astronomically large, reducing modulo a prime is a standard competitive-programming and systems-engineering technique.

---

## 🚀 Try It Yourself *(For Developers)*

- **Variation 1 — Larger grids:** Extend the solution to `rows, cols ≤ 8` and observe how the runtime scales. Consider adding memoisation with a bitmask representation of the grid state to handle the increased search space efficiently.
- **Variation 2 — Mixed tile sizes:** Modify the problem to allow both L-trominoes (3 cells) and dominoes (2 cells), returning the number of ways to tile the grid using any combination of both shapes.
- **Variation 3 — Count unique shapes only:** Instead of counting all tilings, return the number of *structurally distinct* tilings under rotation and reflection of the entire grid, introducing symmetry-group reduction via Burnside's lemma.

---