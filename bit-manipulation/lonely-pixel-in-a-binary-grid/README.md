# Lonely Pixel in a Binary Grid

**Difficulty:** Easy &nbsp;|&nbsp; **Topic:** Bit Manipulation &nbsp;|&nbsp; **Tags:** Bit Manipulation, Array, Counting

---

## 🗂 Problem Overview

Given a binary grid encoded as an array of integers (each integer is one row in binary), count every pixel `(row, col)` that is `1` and is the **only** `1` in its column across all rows. The encoding is MSB-first, so column 0 is the highest-order bit. The non-trivial constraint is that column membership is implicit in the bit positions of each integer — there is no explicit 2D matrix to iterate; you must extract column information through bitwise operations.

---

## 🌍 Engineering Impact

This pattern — encoding a dense boolean matrix as packed integers and querying column-wise properties — appears directly in:

- **Columnar databases** (e.g., Apache Parquet, ClickHouse): column bitmaps for null tracking and predicate pushdown use exactly this representation; uniqueness checks over packed bitmaps drive filter selectivity.
- **Network ACL engines**: firewall rule bitmasks are scanned column-wise to detect non-overlapping (unambiguous) rules.
- **Feature flag systems at scale**: per-feature bit vectors across user cohorts; identifying flags set for exactly one cohort avoids fan-out in rollout logic.

Without the packed representation, memory bandwidth becomes the bottleneck at millions of rows.

---

## 🔍 Problem Statement

**Input:** `rows: List[int]` — `n` integers, each encoding one row of a binary grid using its `w` least-significant bits (MSB = column 0). Parameter `w` defines grid width.

**Output:** `int` — count of lonely pixels: positions `(r, c)` where `rows[r]` has bit `c` set **and** no other row has bit `c` set.

**Constraints:** `1 ≤ n ≤ 1000`, `1 ≤ w ≤ 30`, all values fit within `w` bits.

**Examples:**

```
rows = [5, 3, 4], w = 3   →  Output: 1
  101 / 011 / 100
  col0: rows 0,2 → sum=2  (not lonely)
  col1: row 1 only → sum=1 (lonely)
  col2: rows 0,2 → sum=2  (not lonely)

rows = [8, 4, 2, 1], w = 4  →  Output: 4
  Identity-like pattern; each column has exactly one 1.
```

**Edge case driver:** `w` up to 30 means naive 2D iteration is fine here, but the bit-extraction pattern must be exact — off-by-one in shift direction silently miscounts every column.

---

## 🪜 How to Solve This

1. **Read the problem** → the word "column" immediately signals a need to aggregate across rows, not within them. This is a column-reduction problem.

2. **Recognize the encoding** → rows are integers, columns are bit positions. To ask "how many rows have a 1 in column `c`?" is equivalent to asking "how many integers have bit `c` set?" — a standard bitwise AND + shift check.

3. **Two-pass structure becomes obvious** → first pass: for each column `c`, count how many rows have that bit set (build a column-sum array of length `w`). Second pass: for each `(row, col)` where the bit is set, check if `col_sum[col] == 1`.

4. **Why not one pass?** → you cannot determine loneliness until you've seen all rows, so the column sum must be fully materialized before the second pass. This is a classic reduce-then-filter pattern.

5. **Bit direction** → MSB is column 0, so column `c` corresponds to bit position `(w - 1 - c)`. Getting this wrong is the only real trap.

The solution is `O(n·w)` time and `O(w)` space — straightforward once the encoding is mapped correctly.

---

## 🧩 Algorithm Walkthrough

**Pattern: Frequency Array + Bit Extraction**

This is a two-pass frequency-count pattern applied to an implicit 2D structure encoded in integer bit fields.

**Step 1 — Build column frequency array**
Allocate `col_count[0..w-1]` initialized to zero. For each row integer `r` and each column index `c` in `[0, w)`, extract bit `c` as `(r >> (w - 1 - c)) & 1`. If it equals 1, increment `col_count[c]`. This pass materializes the vertical projection of the grid.

**Invariant after Step 1:** `col_count[c]` holds the exact number of `1`s in column `c` across all rows.

**Step 2 — Count lonely pixels**
Iterate again over every `(row, col)`. For each position where the bit is set, check `col_count[col] == 1`. If true, increment the result counter.

**Why this is correct:** A pixel is lonely iff its column sum is exactly 1. The two-pass approach guarantees the full column sum is known before any loneliness decision is made — no streaming approximation needed.

**Why not bitwise column OR/XOR tricks?** XOR of all rows gives columns with an odd number of `1`s, not exactly one — fails for `col_count = 3`. The explicit frequency array is both simpler and correct.

**Total operations:** `2 × n × w` bit extractions — linear in the grid area.

---

## 📊 Worked Example

**Input:** `rows = [5, 3, 4]`, `w = 3`

Binary: `5 = 101`, `3 = 011`, `4 = 100`

**Pass 1 — Build `col_count`:**

| Row \ Col | c=0 (bit 2) | c=1 (bit 1) | c=2 (bit 0) |
|-----------|-------------|-------------|-------------|
| 5 (101)   | 1           | 0           | 1           |
| 3 (011)   | 0           | 1           | 1           |
| 4 (100)   | 1           | 0           | 0           |
| **sum**   | **2**       | **1**       | **2**       |

**Pass 2 — Check loneliness:**

| (row, col) | bit set? | col_count | lonely? |
|------------|----------|-----------|---------|
| (0, 0)     | yes      | 2         | no      |
| (0, 2)     | yes      | 2         | no      |
| (1, 1)     | yes      | 1         | **yes** |
| (1, 2)     | yes      | 2         | no      |
| (2, 0)     | yes      | 2         | no      |

**Result: 1** ✓

---

## ⏱ Complexity Analysis

### Time Complexity

**O(n · w)** — two passes each touching every `(row, col)` pair exactly once. With `n = 1000` and `w = 30`, this is 60,000 operations — trivially fast. At `n = 10^6` (if constraints were relaxed), it remains 30M operations, well within a single-core budget. At `10^9` rows, you'd need columnar streaming with SIMD popcount.

### Space Complexity

**O(w)** — the `col_count` array of length `w ≤ 30` is the only auxiliary structure. This is effectively O(1) given the fixed 30-bit cap. No reduction trade-off exists here; the frequency array is necessary for correctness.

---

## 💡 Key Takeaways

- **Pattern signal #1:** When a problem asks you to identify elements that are "unique in their column/group," the canonical approach is always a two-pass frequency reduction — first aggregate, then filter. Seeing "lonely," "unique," or "only one" in a grid context should trigger this immediately.
- **Pattern signal #2:** Integer-encoded rows with bit-position semantics are a compressed 2D boolean matrix. Any column-wise query on such a structure maps directly to bitwise operations across the integer array — recognize this encoding before reaching for a 2D array expansion.
- **Implementation gotcha #1:** MSB-first column indexing means column `c` is bit `(w - 1 - c)`, not bit `c`. Using `c` directly silently reverses the column order — the count may still be correct for symmetric inputs, masking the bug in tests.
- **Implementation gotcha #2:** If `w` is not validated against the actual bit-width of the integers, columns beyond the actual data width will always read 0, producing a correct but misleading `col_count` entry of 0 (no lonely pixels in phantom columns) rather than an error.
- **Architectural insight:** The two-pass reduce-then-filter structure is the foundation of columnar query execution (scan → aggregate → filter). Inlining the loneliness check into pass 1 is tempting but incorrect — it's the same mistake as trying to compute a running median without seeing all values. Materializing the intermediate state (column sums) before making decisions is the correct separation of concerns at any scale.

---

## 🚀 Variations & Further Practice

- **Lonely Pixel II (LeetCode 534):** A pixel is lonely only if its column sum equals exactly `B` **and** among the rows containing a `1` in that column, exactly `B` of them have the same row sum as the current row. This adds a row-sum correlation constraint, requiring you to join two frequency dimensions rather than one — the two-pass structure extends to a three-pass or hash-join approach.
- **Column uniqueness in a streaming grid:** Same problem but rows arrive as a stream and you must report lonely pixels as each row is finalized (no second pass over historical rows). This forces a different data structure — a column state machine tracking `{zero, one, many}` — and introduces the challenge of retracting a previously-reported lonely pixel when a later row invalidates it.
- **Packed bitmap intersection queries:** Generalize to "find columns where exactly `k` rows are set" across a large corpus of packed integers. At scale this becomes a `popcount`-over-column problem amenable to SIMD vectorization and Roaring Bitmap representations — the same logical problem, but the implementation strategy shifts entirely to hardware-level bit parallelism.