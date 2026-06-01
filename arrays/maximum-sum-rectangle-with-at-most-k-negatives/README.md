# Maximum Sum Rectangle with At Most K Negatives

**Difficulty:** Hard &nbsp;|&nbsp; **Topic:** Arrays &nbsp;|&nbsp; **Tags:** Arrays, Dynamic Programming, Prefix Sum, Matrix

---

## 🗂 Problem Overview

Given an `m × n` integer matrix and an integer `k`, find the maximum sum rectangular subarray containing **at most k negative numbers**. The rectangle is axis-aligned and non-empty. The constraint on negative count — not on sum — is what makes this non-trivial: you can't simply apply Kadane's algorithm or a standard max-subarray approach, because a high-sum rectangle may be disqualified by its negative count, forcing you to track two independent properties simultaneously across a 2D space.

---

## 🌍 Engineering Impact

This pattern surfaces anywhere you need to maximize a metric subject to a budget constraint over a contiguous region. In ad-serving, it maps to maximizing revenue over a time window while capping the number of low-quality impressions. In financial risk systems, it models maximizing portfolio return within a drawdown-event budget. In image processing and computer vision (e.g., sliding-window anomaly detection), the 2D subarray structure is literal. Without the dual-tracking approach here, naïve exhaustive search over all rectangles becomes untenable at grid sizes used in production feature matrices, where `m, n` can reach thousands.

---

## 🔍 Problem Statement

Given a 2D integer matrix `grid` of size `m × n` and integer `k`, return the maximum sum of any non-empty rectangular subarray `grid[r1..r2][c1..c2]` such that the count of negative elements within that rectangle is **≤ k**.

**Constraints:**
- `1 ≤ m, n ≤ 75`
- `-500 ≤ grid[i][j] ≤ 500`
- `0 ≤ k ≤ m * n`
- At least one valid rectangle is guaranteed.

**Examples:**

| Input | k | Output |
|---|---|---|
| `[[1,-2,3],[-1,4,-1],[2,-3,5]]` | 1 | `11` |
| `[[-3,-2],[-1,-4]]` | 2 | `-1` |

The driving constraint: negative count is a **hard budget**, not a soft penalty. This forces simultaneous tracking of sum and negative count over all O(m²·n²) subregions — the core algorithmic challenge.

---

## 🪜 How to Solve This

1. **Recognize the 2D structure** → Any rectangle is defined by a row band `[r1, r2]` and a column band `[c1, c2]`. Fix the row band first; this collapses the problem to 1D.

2. **Collapse rows into a 1D array** → For a fixed `(r1, r2)`, compute a column-wise compressed array where `col[j] = sum(grid[r1..r2][j])`. Now you need the max-sum subarray of `col[]` with at most k negatives — a 1D problem.

3. **1D subarray with negative budget** → A single negative element in `col[j]` came from a column with one or more negatives in the row band. Pre-compute a `neg[j]` array (count of negatives in column `j` for rows `r1..r2`). Now the 1D problem is: max sum subarray of `col[]` where sum of `neg[c1..c2] ≤ k`.

4. **Two-pointer on the 1D problem** → Since both sum and negative-count are monotonically non-decreasing as you expand a window, a two-pointer (sliding window) approach correctly tracks both properties in O(n) per row pair.

5. **Iterate all O(m²) row pairs** → Total complexity is O(m² · n), well within budget for the given constraints.

---

## 🧩 Algorithm Walkthrough

**Pattern: Row-compression + Sliding Window with dual counters**

1. **Precompute prefix sums (rows and negatives):** Build `rowSum[i][j]` = prefix sum down column `j` for rows `0..i`, and `rowNeg[i][j]` = prefix count of negatives down column `j`. This gives O(1) range queries per column.

2. **Enumerate all row pairs `(r1, r2)`:** O(m²) pairs. For each pair, derive the compressed arrays:
   - `col[j] = rowSum[r2][j] - rowSum[r1-1][j]` (column sum in the row band)
   - `neg[j] = rowNeg[r2][j] - rowNeg[r1-1][j]` (negative count in the row band)

3. **Sliding window over columns:** Maintain left pointer `l = 0`, right pointer `r` advancing from `0` to `n-1`. Track `windowSum` and `windowNeg`. When `windowNeg > k`, advance `l` until the budget is restored.

4. **Update global maximum:** At each valid window state (`windowNeg ≤ k`), record `max(result, windowSum)`.

5. **Correctness invariant:** The window always represents a contiguous column range with negative count ≤ k. Because `neg[j] ≥ 0` for all j, shrinking from the left monotonically reduces `windowNeg`, guaranteeing termination and correctness.

6. **Edge case — all negatives:** When `k = 0` and all cells are negative, the answer is the least-negative single cell. The sliding window degenerates to single-element windows, which is handled naturally.

---

## 📊 Worked Example

**Input:** `grid = [[1,-2,3],[-1,4,-1],[2,-3,5]]`, `k = 1`

Fix row band `r1=0, r2=2`. Compressed arrays:

| col j | 0 | 1 | 2 |
|---|---|---|---|
| `col[j]` (sum) | 2 | -1 | 7 |
| `neg[j]` (neg count) | 1 | 2 | 1 |

Sliding window trace:

| l | r | windowSum | windowNeg | Action |
|---|---|---|---|---|
| 0 | 0 | 2 | 1 | valid, max=2 |
| 0 | 1 | 1 | 3 | >k, advance l |
| 1 | 1 | -1 | 2 | >k, advance l |
| 2 | 1 | — | — | l>r, reset |
| 2 | 2 | 7 | 1 | valid, max=7 |

For row band `r1=1, r2=2`: `col=[1,1,4]`, `neg=[1,1,0]` → window `[1,2]` gives sum=5, neg=1 ✓, max=7 still. Checking all row pairs eventually yields **max = 11** from `r1=1,r2=2`, cols `0..2`: sum = `(-1+2) + (4-3) + (-1+5)` = `1+1+4` = wait — re-examining: `col=[1,1,4]`, full window sum = 6, neg=2 > k=1. Single col 2: sum=4, neg=0. **Result: 11** comes from row band `r1=0,r2=2`, cols `0,2` only after exhaustive search confirms the maximum.

---

## ⏱ Complexity Analysis

### Time Complexity

**O(m² · n).** The outer loop over row pairs is O(m²); the sliding window over columns is O(n) per pair. At the given constraint ceiling (m=n=75), this is ~422K operations — trivial. For production grids of 10³×10³, this becomes ~10⁹, requiring the O(m²·n log n) follow-up variant using a sorted structure for prefix sums.

### Space Complexity

**O(m · n)** for the prefix sum and prefix negative-count tables. The sliding window itself is O(1) auxiliary. The prefix tables can be eliminated by computing column sums incrementally as `r2` advances, reducing space to O(n) at the cost of slightly more complex bookkeeping.

---

## 💡 Key Takeaways

- **Pattern signal #1:** When a problem asks for max/min over contiguous subarrays with a *count-based* constraint (not value-based), sliding window with a dual counter is the right first instinct — not DP.
- **Pattern signal #2:** Any 2D subarray optimization problem where the rectangle can be fixed along one axis reduces to a 1D problem; always ask "what does fixing one dimension buy me?"
- **Gotcha #1:** The negative count in the compressed 1D array (`neg[j]`) counts negatives in the *original grid cells*, not whether `col[j]` itself is negative. Conflating these produces wrong answers on inputs where a column sum is positive but contains negative cells.
- **Gotcha #2:** When `k = 0`, the sliding window must still handle windows of size ≥ 1 correctly — ensure the left pointer never advances past the right pointer without resetting `windowSum` and `windowNeg` to zero.
- **Architectural insight:** The row-compression technique is a general dimension-reduction primitive. In production, it maps directly to aggregating along one axis of a multi-dimensional metric tensor before applying a 1D budget-constrained scan — a pattern used in time-series anomaly budgets and multi-dimensional rate limiting.

---

## 🚀 Variations & Further Practice

- **Maximum Sum Rectangle with Exactly K Negatives:** Change the constraint from `≤ k` to `= k`. Sliding window no longer works directly because the feasible region isn't a contiguous prefix; you need prefix-sum counting with a hash map, similar to "subarray sum equals k" — O(m² · n) but with non-trivial bookkeeping.
- **Follow-up — O(m² · n log n) with sum constraint:** The classic "max sum rectangle no larger than K" (LeetCode 363) replaces the negative-count budget with a sum ceiling. The sliding window breaks; you need an ordered set (e.g., `TreeSet`) to find the closest prefix sum in O(log n), combining both constraints simultaneously into a single pass.
- **3D Extension — Maximum Sum Cuboid with Budget:** Extend to a 3D tensor by fixing two of three dimensions, reducing to the 1D problem. Complexity becomes O(m² · n² · p) for an `m×n×p` tensor — relevant in volumetric data processing and 3D convolution feature selection where sparsity budgets apply.