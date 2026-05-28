# Weighted Region Score Queries on a 2D Grid

**Difficulty:** Hard &nbsp;|&nbsp; **Topic:** Prefix Sum &nbsp;|&nbsp; **Tags:** Prefix Sum, Binary Search, 2D Array, Sorting, Offline Processing

---

## 🗂 Problem Overview

Given an `m × n` integer grid and up to 10⁵ queries, each query asks for the sum of elements in a rectangular subgrid that exceed a per-query threshold. The output is one integer per query. The non-trivial constraint: thresholds vary per query, so a single 2D prefix sum table is useless — you need a structure that supports threshold-parameterized range sums in sub-linear query time.

---

## 🌍 Engineering Impact

This pattern is the backbone of **threshold-filtered aggregation at query time** — a recurring need in production systems. Geospatial analytics platforms (e.g., terrain heatmaps, satellite imagery scoring) filter grid cells by intensity thresholds per request. Ad-tech ranking pipelines compute score sums over feature regions above a bid floor. Time-series monitoring systems answer "sum of anomaly scores above severity X in region Y" queries. Without layered prefix structures, each query degrades to a full scan — at 10⁵ queries over a 300×300 grid, that's 9×10⁹ operations, which is non-starter territory for any latency-sensitive service.

---

## 🔍 Problem Statement

Given an `m × n` integer matrix `grid` (`1 ≤ m, n ≤ 300`, values in `[-10⁴, 10⁴]`) and a list of queries (`1 ≤ queries.length ≤ 10⁵`), each query `[r1, c1, r2, c2, threshold]` defines a rectangle and a threshold. Return the sum of all `grid[i][j]` in that rectangle where `grid[i][j] > threshold`.

**Key driving constraint:** Thresholds are per-query and arbitrary, ruling out a single static prefix sum. The grid is small (≤ 90,000 cells), but query volume is large (≤ 10⁵), so preprocessing cost amortizes well.

**Examples:**

| Grid | Query | Answer |
|------|-------|--------|
| `[[1,3,5],[2,4,6],[7,8,9]]` | `[0,0,1,2,3]` | `15` (5+4+6) |
| `[[10,-2],[-5,7]]` | `[0,0,1,1,0]` | `17` (10+7) |

Edge cases: negative values, thresholds below all grid values (sum entire region), thresholds above all values (return 0).

---

## 🪜 How to Solve This

1. **Observe the bottleneck** → threshold varies per query, so you can't precompute one prefix sum. You need prefix sums *parameterized by value*.

2. **Reframe the query** → "sum of values > threshold in rectangle" = "sum of all values in rectangle" minus "sum of values ≤ threshold in rectangle." Or equivalently, build cumulative prefix sums only for cells above increasing value thresholds.

3. **Offline insight** → sort all unique cell values. For each unique value level, you can build a 2D prefix sum that captures "sum of cells with value exactly this." Stack these into layers indexed by sorted value.

4. **Binary search to select the layer** → given a query threshold, binary search the sorted unique values to find the first value strictly greater than threshold. The answer is the 2D prefix sum of that layer and all layers above it — which is just the suffix of a cumulative layered prefix sum.

5. **Collapse layers into one structure** → instead of storing K separate prefix sum tables, build a single prefix sum table per unique value in sorted order, accumulating from the largest value downward. Now each query is one binary search + one O(1) 2D range lookup.

---

## 🧩 Algorithm Walkthrough

**Pattern: Layered / Fractional Cascading Prefix Sum**

1. **Collect and sort unique values** from the grid. Let `vals` be the sorted list of `V` unique values. This defines the "layers" of the structure.

2. **Build per-layer 2D prefix sums** — for each unique value `v` in `vals`, create a 2D array `layer[v][i][j]` = 1 if `grid[i][j] == v`, else 0, scaled by the cell value. Compute the standard 2D prefix sum over this layer.

3. **Accumulate layers from largest to smallest** — maintain a running cumulative prefix sum table `cum[i][j]`. Process values in descending order, adding each layer's prefix sum into `cum`. Store a snapshot of `cum` keyed by each value. This gives you: "sum of all cells with value ≥ v in any rectangle," queryable in O(1).

4. **Answer each query** — binary search `vals` for the smallest value strictly greater than `threshold`. If none exists, answer is 0. Otherwise, retrieve the stored cumulative prefix sum snapshot for that value and apply the standard 2D range sum formula: `P[r2][c2] - P[r1-1][c2] - P[r2][c1-1] + P[r1-1][c1-1]`.

5. **Invariant maintained:** at each snapshot, `cum[i][j]` holds the prefix sum of all cells whose value is ≥ the current layer's value — ensuring the binary search correctly maps threshold → snapshot.

---

## 📊 Worked Example

Using `grid = [[1,3,5],[2,4,6],[7,8,9]]`, query `[0,0,1,2,3]` (threshold=3):

**Sorted unique values:** `[1, 2, 3, 4, 5, 6, 7, 8, 9]`

**Accumulation (descending), tracking rectangle sum for rows 0–1, cols 0–2:**

| Processing value | Cells added | Cumulative rect sum [0,0→1,2] |
|-----------------|-------------|-------------------------------|
| 9 | (2,2)=9 | 9 |
| 8 | (2,1)=8 | 17 |
| 7 | (2,0)=7 | 24 |
| 6 | (1,2)=6 | 30 → snapshot stored at v=6 |
| 5 | (0,2)=5 | 35 |
| **4** | **(1,1)=4** | **39 → snapshot stored at v=4** |
| 3 | (0,1)=3 | 42 |

**Query:** threshold=3 → binary search finds first value > 3 → `v=4`. Retrieve snapshot at v=4 → rect sum = **15** (5+4+6). ✓

---

## ⏱ Complexity Analysis

### Time Complexity

**Preprocessing:** O(V · m · n) where V ≤ m·n = 90,000 — building and accumulating V layered prefix sum tables, each of size m×n. **Per query:** O(log V) for binary search + O(1) for the range lookup. Total: O(V·m·n + Q·log V). At Q=10⁵ and V·m·n ≤ 8.1×10⁹ in the worst case, the preprocessing dominates — in practice V is far smaller than m·n, keeping this tractable.

### Space Complexity

**O(V · m · n)** for storing all cumulative prefix sum snapshots — the dominant structure. With V ≤ 90,000 and m·n ≤ 90,000, worst case is ~8.1×10⁹ integers, which is impractical. In practice, compress by storing only the snapshot array (not a full copy per layer) using a map from value → 2D array, and note that V is typically much smaller than m·n. Trade-off: reducing snapshots via interpolation increases per-query cost.

---

## 💡 Key Takeaways

- **Pattern signal #1:** When queries ask for aggregates over a range *filtered by a value threshold*, and thresholds vary per query, that's the fingerprint of a layered/parameterized prefix sum — not a single static table.
- **Pattern signal #2:** If preprocessing cost is bounded by grid size but query volume is large (Q >> m·n), offline preprocessing with binary-search dispatch is almost always the right frame.
- **Gotcha #1:** The 2D prefix sum boundary conditions (off-by-one on `r1-1` and `c1-1`) silently corrupt results when `r1=0` or `c1=0` — always clamp to 0 or use 1-indexed tables with a sentinel row/column of zeros.
- **Gotcha #2:** The threshold filter is *strictly greater than* — binary search must find the first value `> threshold`, not `>= threshold`. Using `upper_bound` vs `lower_bound` incorrectly here produces wrong answers on exact-match thresholds.
- **Architectural insight:** This layered snapshot pattern generalizes to any system needing multi-dimensional filtered aggregation at read time — precompute cumulative structures indexed by the filter dimension, then dispatch reads via binary search. It trades write-time memory for O(log N) read latency, a favorable exchange in read-heavy systems.

---

## 🚀 Variations & Further Practice

- **3D threshold queries:** Extend the grid to three dimensions (e.g., voxel grids in medical imaging or 3D game maps) — the layered prefix sum scales to 3D, but snapshot memory becomes O(V · m · n · p), forcing you to evaluate compressed sparse representations or wavelet trees instead.
- **Online queries with grid updates:** If `grid[i][j]` can be mutated between queries, static snapshots are invalidated — the conceptual twist is replacing the layered prefix sum with a 2D Binary Indexed Tree (Fenwick Tree) per value layer, or a merge-sort tree, accepting O(log² N) per update and query.
- **Top-K weighted region queries:** Instead of summing values above a threshold, return the K largest values in a rectangle — this requires order-statistic structures (persistent segment trees or wavelet trees) over the grid, where the "layer" concept evolves into a fully persistent data structure indexed by value rank.