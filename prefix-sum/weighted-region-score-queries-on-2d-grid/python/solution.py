```python
"""
Title: Weighted Region Score Queries on a 2D Grid
Difficulty: Hard
Topic: Prefix Sum

Problem Description:
You are given an m x n integer matrix `grid` representing a map of terrain values,
and a list of queries. Each query is of the form [r1, c1, r2, c2, threshold],
where (r1, c1) is the top-left corner and (r2, c2) is the bottom-right corner
of a rectangular region.

For each query, compute the weighted region score: the sum of all elements in
the subgrid grid[r1..r2][c1..c2] that are strictly greater than threshold.

Because threshold values differ per query, a single static 2D prefix sum is not
sufficient. We pre-process the grid by building layered prefix sums indexed by
sorted unique cell values, then use binary search to find the right layer per query.
"""

import bisect
from typing import List


class Solution:
    def weighted_region_score_queries(
        self, grid: List[List[int]], queries: List[List[int]]
    ) -> List[int]:
        """
        Compute weighted region scores for each query using layered prefix sums.

        For each query [r1, c1, r2, c2, threshold], sum all grid values in the
        rectangle that are strictly greater than threshold.

        Args:
            grid: 2D list of integers representing terrain values.
            queries: List of [r1, c1, r2, c2, threshold] query descriptors.

        Returns:
            List of integers, one per query, each being the weighted region score.

        Time Complexity:
            Pre-processing: O(m * n * log(m * n)) — sorting unique values and
            building one prefix-sum layer per unique value.
            Actually O(U * m * n) where U = number of unique values (at most m*n).
            Each query: O(log U) for binary search + O(1) for prefix-sum lookup.
            Total: O(m*n*U + Q*log(U)) where U <= m*n, Q = len(queries).

        Space Complexity:
            O(U * m * n) for storing all prefix-sum layers.
        """

        # ------------------------------------------------------------------ #
        # STEP 1: Gather basic dimensions
        # ------------------------------------------------------------------ #
        m = len(grid)
        n = len(grid[0])

        # ------------------------------------------------------------------ #
        # STEP 2: Collect all unique cell values and sort them.
        #
        # Why? We want to build one 2D prefix-sum layer for each unique value v,
        # where that layer stores the sum of all grid cells whose value is
        # EXACTLY v (or equivalently, we'll accumulate them so a layer for v
        # stores the sum of cells with value <= v — see below).
        #
        # Sorting lets us later use binary search to find, for a given threshold,
        # the "first layer whose value > threshold" in O(log U) time.
        # ------------------------------------------------------------------ #
        unique_vals = sorted(set(grid[i][j] for i in range(m) for j in range(n)))
        # unique_vals is now a sorted list, e.g. [-5, -2, 7, 10] for Example 2.

        U = len(unique_vals)

        # Map each unique value to its index in unique_vals for quick lookup.
        val_to_idx = {v: idx for idx, v in enumerate(unique_vals)}

        # ------------------------------------------------------------------ #
        # STEP 3: Build "exact-value" 2D prefix sums for each unique value.
        #
        # prefix[k][i][j] = sum of all cells (r, c) with r <= i, c <= j
        #                    whose value equals unique_vals[k].
        #
        # We use 1-indexed prefix arrays (size (m+1) x (n+1)) to simplify the
        # rectangle-sum formula:
        #   rect_sum(r1,c1,r2,c2) = P[r2+1][c2+1] - P[r1][c2+1]
        #                           - P[r2+1][c1] + P[r1][c1]
        #
        # This avoids boundary checks for row/col = 0.
        # ------------------------------------------------------------------ #

        # Initialize U layers, each of size (m+1) x (n+1), filled with 0.
        # prefix[k] is the 2D prefix sum for cells equal to unique_vals[k].
        prefix = [[[0] * (n + 1) for _ in range(m + 1)] for _ in range(U)]

        # Fill in the raw "indicator * value" contribution for each cell.
        for i in range(m):
            for j in range(n):
                v = grid[i][j]
                k = val_to_idx[v]
                # In the 1-indexed prefix array, cell (i,j) maps to position (i+1, j+1).
                prefix[k][i + 1][j + 1] = v  # contribute the value itself

        # Now convert each layer into a proper 2D prefix sum.
        # Standard 2D prefix sum construction:
        #   P[i][j] += P[i-1][j] + P[i][j-1] - P[i-1][j-1]
        for k in range(U):
            for i in range(1, m + 1):
                for j in range(1, n + 1):
                    prefix[k][i][j] += (
                        prefix[k][i - 1][j]
                        + prefix[k][i][j - 1]
                        - prefix[k][i - 1][j - 1]
                    )
            # After this, prefix[k][i][j] = sum of all cells (r,c) with
            # r < i, c < j (0-indexed r,c) whose value == unique_vals[k].

        # ------------------------------------------------------------------ #
        # STEP 4: Build cumulative prefix sums across layers.
        #
        # Instead of querying each layer separately and summing, we accumulate:
        #   cum_prefix[k][i][j] = sum over all layers 0..k of prefix[k][i][j]
        #                       = sum of cells (r,c) with r<i, c<j whose value
        #                         is in {unique_vals[0], ..., unique_vals[k]}.
        #
        # This lets us answer "sum of cells with value <= unique_vals[k] in rect"
        # in O(1) using a single rectangle query on cum_prefix[k].
        #
        # Why cumulative? For a query with threshold T, we want cells with
        # value > T, i.e., value >= unique_vals[first_idx_above_T].
        # That equals (total sum in rect) - (sum of cells with value <= T in rect).
        # Using cum_prefix, "sum of cells with value <= T" = cum_prefix[idx_T] rect query.
        # ------------------------------------------------------------------ #

        # cum_prefix[k] = prefix[0] + prefix[1] + ... + prefix[k] (element-wise)
        cum_prefix = [[[0] * (n + 1) for _ in range(m + 1)] for _ in range(U)]

        # Layer 0 is just prefix[0] itself.
        for i in range(m + 1):
            for j in range(n + 1):
                cum_prefix[0][i][j] = prefix[0][i][j]

        # Each subsequent layer adds the previous cumulative layer.
        for k in range(1, U):
            for i in range(m + 1):
                for j in range(n + 1):
                    cum_prefix[k][i][j] = cum_prefix[k - 1][i][j] + prefix[k][i][j]

        # After this:
        # cum_prefix[k][i][j] = sum of all cells (r,c) with r<i, c<j (0-indexed)
        #                        whose value is <= unique_vals[k].

        # ------------------------------------------------------------------ #
        # STEP 5: Helper function to query a rectangle sum from a prefix layer.
        #
        # Given a 2D prefix array P (1-indexed), the sum of the rectangle
        # with 0-indexed corners (r1,c1) to (r2,c2) inclusive is:
        #   P[r2+1][c2+1] - P[r1][c2+1] - P[r2+1][c1] + P[r1][c1]
        # ------------------------------------------------------------------ #
        def rect_query(P: List[List[int]], r1: int, c1: int, r2: int, c2: int) -> int:
            """Return sum of rectangle (r1,c1)..(r2,c2) using 1-indexed prefix P."""
            return P[r2 + 1][c2 + 1] - P[r1][c2 + 1] - P[r2 + 1][c1] + P[r1][c1]

        # ------------------------------------------------------------------ #
        # STEP 6: Answer each query.
        #
        # For query [r1, c1, r2, c2, threshold]:
        #   We want sum of cells in rect with value > threshold.
        #
        # Strategy:
        #   total_sum = sum of ALL cells in rect (use cum_prefix[U-1])
        #   leq_sum   = sum of cells in rect with value <= threshold
        #             = cum_prefix[k_thresh] rect query, where k_thresh is the
        #               index of the largest unique value <= threshold.
        #   answer    = total_sum - leq_sum
        #
        # To find k_thresh: use bisect_right on unique_vals to find the
        # insertion point of threshold, then subtract 1.
        # If threshold < unique_vals[0], all values are > threshold → leq_sum = 0.
        # ------------------------------------------------------------------ #
        results = []

        for query in queries:
            r1, c1, r2, c2, threshold = query

            # Total sum of all cells in the rectangle (use the last cumulative layer).
            total_sum = rect_query(cum_prefix[U - 1], r1, c1, r2, c2)

            # Find the index of the largest unique value that is <= threshold.
            # bisect_right(unique_vals, threshold) gives the insertion point after
            # all values <= threshold. Subtract 1 to get the last such index.
            k_thresh = bisect.bisect_right(unique_vals, threshold) - 1

            if k_thresh < 0:
                # All unique values are > threshold, so leq_sum = 0.
                leq_sum = 0
            else:
                # Sum of cells with value <= unique_vals[k_thresh] = <= threshold.
                leq_sum = rect_query(cum_prefix[k_thresh], r1, c1, r2, c2)

            results.append(total_sum - leq_sum)

        return results


# ---------------------------------------------------------------------------- #
# VERIFICATION / TRACE
#
# Example 1:
#   grid = [[1,3,5],[2,4,6],[7,8,9]]
#   unique_vals = [1,2,3,4,5,6,7,8,9]
#
#   Query [0,0,1,2,3]: rect = rows 0-1, cols 0-2 → values {1,3,5,2,4,6}
#     total_sum = 1+3+5+2+4+6 = 21
#     threshold=3 → k_thresh = bisect_right([1..9],3)-1 = 3-1 = 2 (unique_vals[2]=3)
#     leq_sum = sum of cells with value<=3 in rect = 1+3+2 = 6
#     answer = 21 - 6 = 15 ✓
#
#   Query [1,1,2,2,5]: rect = rows 1-2, cols 1-2 → values {4,6,8,9}
#     total_sum = 4+6+8+9 = 27
#     threshold=5 → k_thresh = bisect_right([1..9],5)-1 = 5-1 = 4 (unique_vals[4]=5)
#     leq_sum = sum of cells with value<=5 in rect = 4 (only 4 is <=5 in {4,6,8,9})
#     answer = 27 - 4 = 23 ✓
#
# Example 2:
#   grid = [[10,-2],[-5,7]]
#   unique_vals = [-5,-2,7,10]
#
#   Query [0,0,1,1,0]: rect = all cells → values {10,-2,-5,7}
#     total_sum = 10+(-2)+(-5)+7 = 10
#     threshold=0 → bisect_right([-5,-2,7,10],0)=2, k_thresh=1 (unique_vals[1]=-2)
#     leq_sum = sum of cells with value<=-2 = -2+(-5) = -7
#     answer = 10 - (-7) = 17 ✓
#
#   Query [0,0,0,1,9]: rect = row 0, cols 0-1 → values {10,-2}
#     total_sum = 10+(-2) = 8
#     threshold=9 → bisect_right([-5,-2,7,10],9)=3, k_thresh=2 (unique_vals[2]=7)
#     leq_sum = sum of cells with value<=7 in rect = -2 (only -2 is <=7 in {10,-2})
#     answer = 8 - (-2) = 10 ✓
# ---------------------------------------------------------------------------- #


if __name__ == "__main__":
    sol = Solution()

    # ---- Example 1 ----
    grid1 = [
        [1, 3, 5],
        [2, 4, 6],
        [7, 8, 9],
    ]
    queries1 = [[0, 0, 1, 2, 3], [1, 1, 2, 2, 5]]
    result1 = sol.weighted_region_score_queries(grid1, queries1)
    print("Example 1:")
    print(f"  Input grid: {grid1}")
    print(f"  Queries:    {queries1}")
    print(f"  Output:     {result1}")
    print(f"  Expected:   [15, 23]")
    print()

    # ---- Example 2 ----
    grid2 = [
        [10, -2],
        [-5, 7],
    ]
    queries2 = [[0, 0, 1, 1, 0], [0, 0, 0, 1, 9]]
    result2 = sol.weighted_region_score_queries(grid2, queries2)
    print("Example 2:")
    print(f"  Input grid: {grid2}")
    print(f"  Queries:    {queries2}")
    print(f"  Output:     {result2}")
    print(f"  Expected:   [17, 10]")
    print()

    # ---- Edge case: threshold below all values ----
    grid3 = [[5, 10], [15, 20]]
    queries3 = [[0, 0, 1, 1, -100]]
    result3 = sol.weighted_region_score_queries(grid3, queries3)
    print("Edge case (threshold below all values):")
    print(f"  Input grid: {grid3}")
    print(f"  Queries:    {queries3}")
    print(f"  Output:     {result3}")
    print(f"  Expected:   [50]")
    print()

    # ---- Edge case: threshold above all values ----
    queries4 = [[0, 0, 1, 1, 100]]
    result4 = sol.weighted_region_score_queries(grid3, queries4)
    print("Edge case (threshold above all values):")
    print(f"  Input grid: