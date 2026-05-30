# Generate All Valid PIN Patterns

**Difficulty:** Easy &nbsp;|&nbsp; **Topic:** Recursion and Backtracking &nbsp;|&nbsp; **Tags:** Backtracking, Recursion, Grid, Counting

---

## 🗂 Problem Overview

Given a 3×3 grid of digits 1–9, count all valid PIN sequences whose length falls within `[minLen, maxLen]`. A sequence is valid if every move between consecutive digits either connects adjacent cells (horizontally, vertically, or diagonally) or has all intermediate collinear digits already used. The non-triviality is the "skip" rule: the legality of a move is state-dependent, making it impossible to precompute valid transitions without tracking visit history.

---

## 🌍 Engineering Impact

The "contextually gated traversal" pattern appears in gesture-unlock systems (Android's pattern lock is a direct analogue), circuit board routing where trace crossings are forbidden unless prior nodes are committed, and game-tree search engines where move legality depends on board state. At scale, the key architectural insight is that state-dependent edge validity collapses the problem from a static graph traversal into a stateful DFS — the same shift that separates a naïve rule engine from a correct one in workflow orchestration systems like Temporal or Apache Airflow.

---

## 🔍 Problem Statement

**Input:** Two integers `minLen` and `maxLen`, where `1 <= minLen <= maxLen <= 9`.

**Output:** Total count of distinct valid PIN sequences whose length is between `minLen` and `maxLen` inclusive.

**Validity rule:** A move from digit `a` to digit `b` is legal if and only if every digit that lies on the straight line segment between `a` and `b` on the grid has already been visited.

**Examples:**

| minLen | maxLen | Output | Notes |
|--------|--------|--------|-------|
| 1 | 1 | 9 | All single digits valid |
| 1 | 2 | 65 | 9 + 56 two-digit patterns |

**Edge cases:** `minLen == maxLen == 9` enumerates only full-board Hamiltonian paths. The skip constraint means move legality cannot be determined from endpoints alone — you must carry visited state through every recursive call.

---

## 🪜 How to Solve This

1. **Read the constraint** → move legality depends on which digits are already visited. This is inherently stateful, which rules out pure combinatorics or a static adjacency matrix.

2. **Stateful traversal** → think DFS/backtracking. You need to explore all sequences, prune illegal moves, and count sequences that hit the target length range.

3. **Encode the skip rule** → precompute a `skip[a][b]` table: for each pair `(a, b)` that are not direct neighbors, store the digit that must be visited first. This is a fixed 9×9 lookup — compute it once, use it in O(1) per move check.

4. **Exploit grid symmetry** → digits 1, 3, 7, 9 are symmetric corners; 2, 4, 6, 8 are symmetric edges; 5 is the center. Run DFS from one representative of each group and multiply by group size (4, 4, 1). This cuts the work by ~9×.

5. **Accumulate counts** → at each DFS depth, if current length is within `[minLen, maxLen]`, increment the counter. Backtrack by unmarking the visited digit.

The reasoning chain: state-dependent edges → backtracking → precomputed skip table → symmetry reduction → single pass count accumulation.

---

## 🧩 Algorithm Walkthrough

**Pattern: Backtracking with precomputed state-dependent edge table**

1. **Precompute `skip[i][j]`** for all digit pairs. If `i` and `j` are on the same row/column/diagonal with a digit strictly between them, record that intermediate digit. For all directly adjacent or knight's-move pairs, `skip[i][j] = 0` (no prerequisite). This table is 9×9 and fixed — build it once.

2. **DFS function** `dfs(current, visited, length)`:
   - For each candidate digit `next` from 1–9:
     - Skip if `visited[next]` is true.
     - Check `mid = skip[current][next]`: if `mid != 0` and `visited[mid]` is false, this move is illegal — skip it.
     - Otherwise, mark `visited[next]`, recurse with `length + 1`, then unmark (backtrack).
   - If `length >= minLen`, increment the global count before recursing further.

3. **Symmetry grouping**: Call DFS starting from digit `1` (corner), multiply result by 4. Call from digit `2` (edge), multiply by 4. Call from digit `5` (center), multiply by 1. Sum the three contributions.

4. **Invariant maintained**: at every recursive frame, `visited` accurately reflects exactly the digits in the current partial sequence — the backtrack step is the guarantee.

5. **Termination**: maximum recursion depth is 9 (all digits used), and the visited bitmask strictly grows each level, so no cycle is possible.

---

## 📊 Worked Example

Starting from digit `1`, tracing two-step sequences to illustrate skip enforcement:

| Step | Current | Trying Next | `skip[cur][next]` | `visited[skip]`? | Legal? | Action |
|------|---------|-------------|-------------------|------------------|--------|--------|
| 1 | 1 | 2 | 0 | — | ✅ | Visit 2, depth=2, count++ |
| 1 | 1 | 3 | 2 | false | ❌ | Skip (2 not yet visited) |
| 1 | 1 | 5 | 0 | — | ✅ | Visit 5, depth=2, count++ |
| 1 | 1 | 7 | 4 | false | ❌ | Skip (4 not yet visited) |
| 1 | 1 | 9 | 5 | false | ❌ | Skip (5 not yet visited) |

From digit `1` at depth 1, only digits with no skip requirement or whose skip digit is already visited are reachable. This trace shows exactly why the skip table is the critical data structure — without it, you'd need to recompute collinearity inline on every move.

---

## ⏱ Complexity Analysis

### Time Complexity

**O(9!)** in the absolute worst case — bounded by the number of permutations of 9 digits. In practice, the skip constraint prunes a significant fraction of branches, and symmetry reduction cuts the constant by ~9×. At 9! = 362,880 nodes, this is instantaneous regardless of input.

### Space Complexity

**O(9)** for the recursion stack (maximum depth equals grid size) plus O(81) for the precomputed skip table. The visited array is O(9). No additional heap allocation is needed — the space is effectively constant and cannot be meaningfully reduced further.

---

## 💡 Key Takeaways

- **Pattern signal — state-dependent edge legality:** When whether you can traverse an edge depends on what you've already visited, backtracking with an explicit visited set is the correct abstraction — static graph algorithms won't apply.
- **Pattern signal — counting valid sequences under constraints:** If the problem asks for a count (not the sequences themselves) with path-level constraints, DFS + counter is almost always cleaner than trying to derive a closed-form formula.
- **Implementation gotcha — skip table indexing:** The skip table must be indexed by the actual digit values (1–9), not zero-based indices, or you'll introduce off-by-one errors in every lookup. Pick one convention and enforce it at construction time.
- **Implementation gotcha — counting at the right depth:** Increment the counter *before* deciding whether to recurse further, not after. Counting after the recursive call causes you to miss sequences exactly at `minLen` when `minLen == maxLen`.
- **Architectural insight:** Precomputing a small, fixed constraint table (here, 81 entries) and separating it from the traversal logic is the production-grade pattern — it makes the move-validation rule independently testable, swappable, and auditable, which matters when the "grid" is a business rule graph rather than a literal keypad.

---

## 🚀 Variations & Further Practice

- **Android Pattern Lock with minimum length 4:** The real Android lock enforces `minLen = 4` and disallows single-point reuse — the conceptual twist is adding a minimum-length gate and understanding how that changes the symmetry argument when partial paths must be pruned earlier.
- **Weighted PIN patterns:** Assign a cost to each move and find the count of valid patterns whose total cost falls within a range. This extends the problem from pure counting to constrained enumeration, requiring you to carry accumulated cost as additional DFS state and potentially apply branch-and-bound pruning.
- **Generalized N×N grid patterns:** Scale the grid beyond 3×3 — the skip rule generalizes, but the precomputation of intermediate cells becomes a line-rasterization problem (Bresenham's algorithm), and symmetry groups grow in complexity, making the reduction non-trivial to implement correctly.