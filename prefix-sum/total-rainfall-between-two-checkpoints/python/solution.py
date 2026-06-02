"""
Title: Total Rainfall Between Two Checkpoints
Difficulty: Easy
Topic: Prefix Sum

Problem Description:
A weather station has recorded daily rainfall measurements along a straight highway.
There are n checkpoints numbered from 0 to n-1, and rainfall[i] represents the amount
of rainfall (in millimeters) recorded at checkpoint i on a given day.

You are given an integer array rainfall of length n and a 2D array queries where
queries[j] = [left, right]. For each query, you need to find the total rainfall
recorded between checkpoint left and checkpoint right, inclusive.

Return an integer array answer where answer[j] is the total rainfall for the j-th query.

Constraints:
- 1 <= n <= 10^5
- 0 <= rainfall[i] <= 1000
- 1 <= queries.length <= 10^5
- 0 <= left <= right < n

Example 1:
- Input: rainfall = [3, 1, 4, 1, 5, 9, 2, 6], queries = [[1, 4], [0, 6], [3, 3]]
- Output: [11, 25, 1]
- Explanation: Query [1,4]: 1+4+1+5 = 11. Query [0,6]: 3+1+4+1+5+9+2 = 25. Query [3,3]: 1 = 1.

Example 2:
- Input: rainfall = [0, 2, 7, 3, 5], queries = [[0, 4], [2, 3]]
- Output: [17, 10]
- Explanation: Query [0,4]: 0+2+7+3+5 = 17. Query [2,3]: 7+3 = 10.
"""

from typing import List


class Solution:
    def totalRainfallBetweenCheckpoints(
        self, rainfall: List[int], queries: List[List[int]]
    ) -> List[int]:
        """
        Compute the total rainfall between two checkpoints for each query using prefix sums.

        The key insight is that if we precompute a prefix sum array where prefix[i] holds
        the cumulative sum of rainfall from index 0 up to (but not including) index i,
        then the sum of any subarray [left, right] can be computed in O(1) as:
            prefix[right + 1] - prefix[left]

        This avoids recomputing sums from scratch for every query (which would be O(n)
        per query), bringing the per-query cost down to O(1).

        Args:
            rainfall (List[int]): Array of daily rainfall measurements at each checkpoint.
            queries (List[List[int]]): List of [left, right] range queries (inclusive).

        Returns:
            List[int]: Total rainfall for each query.

        Time Complexity:  O(n + q), where n = len(rainfall) and q = len(queries).
                          Building the prefix sum array takes O(n), and answering
                          each of the q queries takes O(1), so total is O(n + q).
        Space Complexity: O(n) for the prefix sum array (output array O(q) is required).
        """

        # ----------------------------------------------------------------
        # STEP 1: Determine the length of the rainfall array.
        # We need this to size our prefix sum array correctly.
        # ----------------------------------------------------------------
        n: int = len(rainfall)

        # ----------------------------------------------------------------
        # STEP 2: Build the prefix sum array.
        #
        # We create an array `prefix` of length n + 1, initialized to all zeros.
        # The extra element at index 0 acts as a sentinel (base case) so that
        # range queries starting at index 0 work without special-casing.
        #
        # Definition:
        #   prefix[i] = rainfall[0] + rainfall[1] + ... + rainfall[i-1]
        #             = sum of the first i elements (0-indexed up to i-1)
        #
        # So:
        #   prefix[0] = 0                          (empty sum, base case)
        #   prefix[1] = rainfall[0]
        #   prefix[2] = rainfall[0] + rainfall[1]
        #   ...
        #   prefix[n] = rainfall[0] + ... + rainfall[n-1]
        #
        # Why this layout?
        #   The sum of rainfall[left..right] (inclusive) equals:
        #       prefix[right + 1] - prefix[left]
        #   This single subtraction replaces iterating over the subarray.
        # ----------------------------------------------------------------
        prefix: List[int] = [0] * (n + 1)

        for i in range(n):
            # Each prefix entry is the previous prefix entry plus the current rainfall.
            # prefix[i+1] accumulates the total from index 0 through index i.
            prefix[i + 1] = prefix[i] + rainfall[i]

        # ----------------------------------------------------------------
        # STEP 3: Answer each query in O(1) using the prefix sum array.
        #
        # For a query [left, right]:
        #   total = prefix[right + 1] - prefix[left]
        #
        # Intuition:
        #   prefix[right + 1] = sum of rainfall[0..right]
        #   prefix[left]       = sum of rainfall[0..left-1]
        #   Subtracting removes the portion before `left`, leaving rainfall[left..right].
        #
        # Example trace for Example 1:
        #   rainfall = [3, 1, 4, 1, 5, 9, 2, 6]
        #   prefix   = [0, 3, 4, 8, 9,14,23,25,31]
        #
        #   Query [1, 4]: prefix[5] - prefix[1] = 14 - 3 = 11  ✓
        #   Query [0, 6]: prefix[7] - prefix[0] = 25 - 0 = 25  ✓
        #   Query [3, 3]: prefix[4] - prefix[3] =  9 - 8 =  1  ✓
        #
        # Example trace for Example 2:
        #   rainfall = [0, 2, 7, 3, 5]
        #   prefix   = [0, 0, 2, 9,12,17]
        #
        #   Query [0, 4]: prefix[5] - prefix[0] = 17 - 0 = 17  ✓
        #   Query [2, 3]: prefix[4] - prefix[2] = 12 - 2 = 10  ✓
        # ----------------------------------------------------------------
        answer: List[int] = []

        for query in queries:
            left: int = query[0]
            right: int = query[1]

            # Compute the range sum using the O(1) prefix-sum formula.
            range_sum: int = prefix[right + 1] - prefix[left]

            # Append the result for this query to our answer list.
            answer.append(range_sum)

        # ----------------------------------------------------------------
        # STEP 4: Return the list of answers, one per query.
        # ----------------------------------------------------------------
        return answer


# ----------------------------------------------------------------------
# Main block: demonstrate the solution with the provided examples and
# print results so the output can be verified at a glance.
# ----------------------------------------------------------------------
if __name__ == "__main__":
    solution = Solution()

    # ------------------------------------------------------------------
    # Example 1
    # ------------------------------------------------------------------
    rainfall_1: List[int] = [3, 1, 4, 1, 5, 9, 2, 6]
    queries_1: List[List[int]] = [[1, 4], [0, 6], [3, 3]]
    expected_1: List[int] = [11, 25, 1]

    result_1 = solution.totalRainfallBetweenCheckpoints(rainfall_1, queries_1)

    print("Example 1")
    print(f"  rainfall : {rainfall_1}")
    print(f"  queries  : {queries_1}")
    print(f"  result   : {result_1}")
    print(f"  expected : {expected_1}")
    print(f"  correct  : {result_1 == expected_1}")
    print()

    # ------------------------------------------------------------------
    # Example 2
    # ------------------------------------------------------------------
    rainfall_2: List[int] = [0, 2, 7, 3, 5]
    queries_2: List[List[int]] = [[0, 4], [2, 3]]
    expected_2: List[int] = [17, 10]

    result_2 = solution.totalRainfallBetweenCheckpoints(rainfall_2, queries_2)

    print("Example 2")
    print(f"  rainfall : {rainfall_2}")
    print(f"  queries  : {queries_2}")
    print(f"  result   : {result_2}")
    print(f"  expected : {expected_2}")
    print(f"  correct  : {result_2 == expected_2}")
    print()

    # ------------------------------------------------------------------
    # Edge case: single checkpoint, single query
    # ------------------------------------------------------------------
    rainfall_3: List[int] = [42]
    queries_3: List[List[int]] = [[0, 0]]
    expected_3: List[int] = [42]

    result_3 = solution.totalRainfallBetweenCheckpoints(rainfall_3, queries_3)

    print("Edge Case: single checkpoint")
    print(f"  rainfall : {rainfall_3}")
    print(f"  queries  : {queries_3}")
    print(f"  result   : {result_3}")
    print(f"  expected : {expected_3}")
    print(f"  correct  : {result_3 == expected_3}")