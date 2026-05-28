# Spiral Matrix Layer Rotation with Queries

**Difficulty:** Hard &nbsp;|&nbsp; **Topic:** Arrays &nbsp;|&nbsp; **Tags:** Arrays, Matrix, Simulation

---

## 🗂 Problem Overview

Given an `n × m` matrix and a list of queries `[layer, k, direction]`, rotate each specified concentric rectangular layer by `k` positions clockwise (`L`) or counter-clockwise (`R`), applying queries sequentially. The non-trivial constraint is that `k` can reach `10^9` and queries can number `10^4`, so naive element-by-element rotation per query is unacceptable — the solution must collapse repeated queries on the same layer into a single modular offset before touching the matrix.

---

## 🌍 Engineering Impact

This pattern surfaces wherever a fixed-topology structure requires deferred, batched mutations: GPU texture buffer rotations in graphics pipelines, ring-buffer management in high-throughput message queues (Kafka partition reassignment), and sliding-window aggregations in stream processors. In distributed systems, layer-scoped rotation mirrors shard rebalancing — rotating ownership rings in consistent hashing. Without offset accumulation and modular reduction, each query becomes an O(perimeter) write, and at `10^4` queries over a 300×300 matrix the constant-factor blowup stalls latency-sensitive pipelines.

---

## 🔍 Problem Statement

**Input:** An `n × m` integer matrix (`1 ≤ n, m ≤ 300`) and a query list of length up to `10^4`. Each query `[layer, k, direction]` identifies a concentric rectangular ring (layer 0 = outermost), a rotation magnitude `k` (`1 ≤ k ≤ 10^9`), and a direction (`L` = clockwise, `R` = counter-clockwise).

**Output:** The matrix after all queries are applied in order.

**Edge cases:** `k` larger than the layer's perimeter (requires modulo); multiple queries on the same layer (offsets must accumulate before writing); non-square matrices where layer count is `floor(min(n, m) / 2)`; a 1-element layer where any rotation is a no-op.

**Driving constraint:** `k ≤ 10^9` forces modular arithmetic — the algorithmic choice is offset accumulation, not repeated rotation.

**Example 1:**
```
Input:  [[1,2,3],[4,5,6],[7,8,9]], queries = [[0,1,'L']]
Output: [[2,3,6],[1,5,9],[4,7,8]]
```

**Example 2:**
```
Input:  [[1,2,3,4],[5,6,7,8],[9,10,11,12],[13,14,15,16]]
        queries = [[0,2,'R'],[1,1,'L']]
Output: [[13,5,1,2],[9,11,6,3],[14,7,10,4],[15,16,12,8]]
```

---

## 🪜 How to Solve This

1. **Read the problem → notice layers are independent rings.** No query on layer `i` affects layer `j`. This is the key structural insight: process each layer in isolation.

2. **Multiple queries on the same layer → don't rotate repeatedly.** Accumulate a net signed offset per layer. `L` adds `+k`, `R` adds `-k`. A single `mod perimeter` at the end gives the minimal effective rotation.

3. **`k` up to `10^9` → modular reduction is mandatory.** Without it, rotating a 4-element layer `10^9` times is nonsensical. After accumulation, `net_offset % perimeter` collapses this to at most one full traversal.

4. **Rotation is just index remapping on a 1D extraction.** Extract the ring into a flat array in clockwise order, apply the offset as a cyclic shift, then write back. This separates the 2D coordinate complexity from the rotation logic entirely.

5. **Write-back requires the same clockwise traversal order.** The extraction and write-back must use identical traversal — top row left→right, right column top→bottom, bottom row right→left, left column bottom→top — or the ring reassembles incorrectly.

---

## 🧩 Algorithm Walkthrough

**Pattern: Simulation with Offset Accumulation**

1. **Accumulate net offsets.** Iterate through all queries, building a map `layer → net_offset`. `L` increments, `R` decrements. This is O(Q) and defers all matrix writes to a single pass.

2. **Extract each affected layer.** For layer `i`, the ring spans rows `[i, n-1-i]` and columns `[i, m-1-i]`. Traverse clockwise: top edge left→right, right edge top→bottom (excluding corners), bottom edge right→left, left edge bottom→top (excluding corners). This produces a 1D array of length `perimeter`.

3. **Compute effective shift.** `shift = net_offset % perimeter`. If `shift < 0`, add `perimeter` to normalize to a positive index. This invariant ensures `shift ∈ [0, perimeter)` regardless of query count or magnitude.

4. **Apply cyclic rotation.** The rotated array is `elements[shift:] + elements[:shift]`. This is the standard left-rotation by `shift` positions — element at index `i` in the original maps to index `(i - shift + perimeter) % perimeter` in the result.

5. **Write back.** Traverse the same clockwise path, placing elements from the rotated array sequentially. The 2D coordinates are unchanged; only the values are reassigned.

6. **Correctness invariant:** Each cell in a layer is written exactly once per affected layer, and the net offset is idempotent — applying it once is equivalent to applying all individual queries.

---

## 📊 Worked Example

**Input:** `matrix = [[1,2,3],[4,5,6],[7,8,9]]`, `queries = [[0,1,'L']]`

Layer 0 clockwise extraction:

| Step | Position | Value |
|------|----------|-------|
| Top row L→R | (0,0),(0,1),(0,2) | 1, 2, 3 |
| Right col T→B | (1,2),(2,2) | 6, 9 |
| Bottom row R→L | (2,1),(2,0) | 8, 7 |
| Left col B→T | (1,0) | 4 |

Extracted: `[1, 2, 3, 6, 9, 8, 7, 4]`, perimeter = 8

Net offset: `+1` (L), shift = `1 % 8 = 1`

Rotated: `[2, 3, 6, 9, 8, 7, 4, 1]`

Write-back maps `2→(0,0)`, `3→(0,1)`, `6→(0,2)`, `9→(1,2)`, `8→(2,2)`, `7→(2,1)`, `4→(2,0)`, `1→(1,0)`.

**Output:** `[[2,3,6],[1,5,9],[4,7,8]]` ✓

---

## ⏱ Complexity Analysis

### Time Complexity

**O(Q + n·m)** — query accumulation is O(Q); each matrix cell belongs to exactly one layer and is extracted and written back at most once, giving O(n·m) for all write-backs. At a 300×300 matrix with `10^4` queries this is ~100K operations, well within real-time budgets. The modular reduction keeps `k = 10^9` irrelevant to runtime.

### Space Complexity

**O(min(n, m) · max(n, m)) = O(n·m)** in the worst case for the extracted layer buffer, though at any moment only one layer's perimeter (at most `2(n+m)`) is held. The layer-offset map is O(number of distinct layers) = O(min(n, m)). The dominant allocation is the input matrix itself; auxiliary space cannot be reduced below O(perimeter) without in-place rotation, which adds implementation complexity for marginal gain.

---

## 💡 Key Takeaways

- **Pattern signal — independent partitions with repeated mutations:** When the problem decomposes into non-overlapping substructures each receiving multiple updates, offset accumulation + single-pass application is almost always the right frame.
- **Pattern signal — large `k` with cyclic structure:** Any time `k` can exceed the structure's size and the operation is cyclic, `k % size` is the first reduction to reach for; its absence is a correctness bug, not just a performance issue.
- **Gotcha — direction sign convention:** `L` and `R` are described relative to clockwise traversal order, not screen coordinates. Mixing these up produces a result that passes visual inspection on square matrices but fails on non-square ones.
- **Gotcha — perimeter calculation for non-square matrices:** The perimeter of layer `i` is `2*(n - 2i) + 2*(m - 2i) - 4`. Off-by-one on the subtraction double-counts corners and corrupts the modulo base, causing silent wrap-around errors.
- **Architectural insight:** Separating *intent accumulation* (query folding) from *effect application* (matrix mutation) is the same principle behind write-ahead logs and command sourcing — it makes the operation auditable, replayable, and trivially parallelizable per layer.

---

## 🚀 Variations & Further Practice

- **Diagonal layer rotation:** Replace rectangular rings with diagonal shells (elements equidistant from the matrix diagonal). The traversal order becomes non-contiguous, breaking the simple clockwise extraction and requiring explicit coordinate enumeration — the offset accumulation logic is identical, but the coordinate mapping is significantly harder.
- **Online queries with point reads:** Add a query type `[layer, position]` that returns the current value at a clockwise position without materializing the matrix. This forces lazy evaluation — you must compute the effective offset on-demand and map the position through accumulated rotations in O(1) per read, turning the problem into an interval-offset data structure design.
- **3D concentric shells:** Extend to an `n × m × p` tensor where layers are concentric cuboid shells. Traversal order becomes ambiguous (face ordering matters), and the perimeter becomes a surface area — the same modular reduction applies, but defining a canonical traversal order across six faces is the core design challenge.