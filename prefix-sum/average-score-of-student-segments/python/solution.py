"""
Average Score of Student Segments
==================================

Problem Description:
A teacher has recorded the scores of n students in a linear array scores.
She wants to evaluate the performance of students over multiple contiguous
segments of the class roster. For each query, she provides a range [left, right]
(0-indexed, inclusive) and wants to know the average score (as a floating point
number) of students in that segment.

Given an integer array scores and a 2D integer array queries where
queries[i] = [left, right], return an array of floating point numbers where
the i-th element is the average score of students from index left to index
right (inclusive).

Constraints:
- 1 <= scores.length <= 10^5
- 0 <= scores[i] <= 100
- 1 <= queries.length <= 10^4
- 0 <= queries[i][0] <= queries[i][1] < scores.length

Example 1:
Input: scores = [80, 90, 70, 60, 85], queries = [[0, 2], [1, 4]]
Output: [80.0, 76.25]
Explanation: For query [0,2]: (80+90+70)/3 = 80.0
             For query [1,4]: (90+70+60+85)/4 = 76.25

Example 2:
Input: scores = [50, 100, 40, 90, 20], queries = [[0, 4], [2, 3]]
Output: [60.0, 65.0]
Explanation: For query [0,4]: (50+100+40+90+20)/5 = 60.0
             For query [2,3]: (40+90)/2 = 65.0
"""

from typing import List


class Solution:
    def averageScoreOfSegments(
        self, scores: List[int], queries: List[List[int]]
    ) -> List[float]:
        """
        Compute the average score for each query segment using a prefix sum array.

        The key insight is that computing the sum of a subarray naively for each
        query would be O(n) per query, leading to O(n * q) total time. Instead,
        we precompute a prefix sum array so that any subarray sum can be retrieved
        in O(1) time.

        Prefix Sum Concept:
        - prefix[0] = 0  (sentinel/base value, no elements summed yet)
        - prefix[i] = scores[0] + scores[1] + ... + scores[i-1]
        - Sum of scores[left..right] = prefix[right+1] - prefix[left]

        Args:
            scores  : List of integer scores for n students.
            queries : List of [left, right] pairs (0-indexed, inclusive).

        Returns:
            List of floats where result[i] is the average score for queries[i].

        Time Complexity:  O(n + q)  — O(n) to build prefix sum, O(1) per query
        Space Complexity: O(n)      — for the prefix sum array
        """

        # ------------------------------------------------------------------ #
        # STEP 1: Determine the number of students.
        # We need this to size our prefix sum array correctly.
        # ------------------------------------------------------------------ #
        n: int = len(scores)

        # ------------------------------------------------------------------ #
        # STEP 2: Build the prefix sum array.
        #
        # We create an array of length (n + 1) initialised to 0.
        # The extra element at index 0 acts as a sentinel so that the formula
        # works uniformly even when left == 0.
        #
        # After this loop:
        #   prefix[0] = 0
        #   prefix[1] = scores[0]
        #   prefix[2] = scores[0] + scores[1]
        #   ...
        #   prefix[n] = scores[0] + scores[1] + ... + scores[n-1]
        #
        # Why this layout?
        #   Sum of scores[left..right] = prefix[right+1] - prefix[left]
        #   This avoids any special-casing for left == 0.
        # ------------------------------------------------------------------ #
        prefix: List[int] = [0] * (n + 1)

        for i in range(n):
            # Each prefix entry is the running total up to (but not including)
            # index i in the original scores array.
            prefix[i + 1] = prefix[i] + scores[i]

        # ------------------------------------------------------------------ #
        # STEP 3: Answer each query in O(1) using the prefix sum array.
        #
        # For a query [left, right]:
        #   - segment_sum   = prefix[right + 1] - prefix[left]
        #   - segment_count = right - left + 1   (number of elements)
        #   - average       = segment_sum / segment_count
        #
        # We cast to float explicitly (Python's / operator already returns float,
        # but the explicit float() call makes the intent crystal-clear for readers).
        # ------------------------------------------------------------------ #
        results: List[float] = []

        for query in queries:
            left: int = query[0]
            right: int = query[1]

            # Compute the sum of the segment [left, right] using the prefix array.
            # Example: scores=[80,90,70,60,85], left=1, right=4
            #   prefix = [0, 80, 170, 240, 300, 385]
            #   segment_sum = prefix[5] - prefix[1] = 385 - 80 = 305
            segment_sum: int = prefix[right + 1] - prefix[left]

            # Number of students in this segment (inclusive on both ends).
            # Example continued: right - left + 1 = 4 - 1 + 1 = 4
            segment_count: int = right - left + 1

            # Compute the average as a floating-point number.
            # Example continued: 305 / 4 = 76.25  ✓
            average: float = segment_sum / segment_count

            results.append(average)

        # ------------------------------------------------------------------ #
        # STEP 4: Return the list of averages, one per query.
        # ------------------------------------------------------------------ #
        return results


# --------------------------------------------------------------------------- #
# Manual verification against the provided examples
# --------------------------------------------------------------------------- #
# Example 1:
#   scores  = [80, 90, 70, 60, 85]
#   prefix  = [0, 80, 170, 240, 300, 385]
#
#   Query [0, 2]:
#     segment_sum   = prefix[3] - prefix[0] = 240 - 0 = 240
#     segment_count = 2 - 0 + 1 = 3
#     average       = 240 / 3 = 80.0  ✓
#
#   Query [1, 4]:
#     segment_sum   = prefix[5] - prefix[1] = 385 - 80 = 305
#     segment_count = 4 - 1 + 1 = 4
#     average       = 305 / 4 = 76.25  ✓
#
# Example 2:
#   scores  = [50, 100, 40, 90, 20]
#   prefix  = [0, 50, 150, 190, 280, 300]
#
#   Query [0, 4]:
#     segment_sum   = prefix[5] - prefix[0] = 300 - 0 = 300
#     segment_count = 4 - 0 + 1 = 5
#     average       = 300 / 5 = 60.0  ✓
#
#   Query [2, 3]:
#     segment_sum   = prefix[4] - prefix[2] = 280 - 150 = 130
#     segment_count = 3 - 2 + 1 = 2
#     average       = 130 / 2 = 65.0  ✓
# --------------------------------------------------------------------------- #


if __name__ == "__main__":
    solution = Solution()

    # ---------------------------------------------------------------------- #
    # Example 1
    # ---------------------------------------------------------------------- #
    scores_1: List[int] = [80, 90, 70, 60, 85]
    queries_1: List[List[int]] = [[0, 2], [1, 4]]
    result_1: List[float] = solution.averageScoreOfSegments(scores_1, queries_1)
    print("Example 1")
    print(f"  scores  : {scores_1}")
    print(f"  queries : {queries_1}")
    print(f"  output  : {result_1}")          # Expected: [80.0, 76.25]
    print()

    # ---------------------------------------------------------------------- #
    # Example 2
    # ---------------------------------------------------------------------- #
    scores_2: List[int] = [50, 100, 40, 90, 20]
    queries_2: List[List[int]] = [[0, 4], [2, 3]]
    result_2: List[float] = solution.averageScoreOfSegments(scores_2, queries_2)
    print("Example 2")
    print(f"  scores  : {scores_2}")
    print(f"  queries : {queries_2}")
    print(f"  output  : {result_2}")          # Expected: [60.0, 65.0]
    print()

    # ---------------------------------------------------------------------- #
    # Edge case: single-element segment
    # ---------------------------------------------------------------------- #
    scores_3: List[int] = [42]
    queries_3: List[List[int]] = [[0, 0]]
    result_3: List[float] = solution.averageScoreOfSegments(scores_3, queries_3)
    print("Edge case — single element")
    print(f"  scores  : {scores_3}")
    print(f"  queries : {queries_3}")
    print(f"  output  : {result_3}")          # Expected: [42.0]
    print()

    # ---------------------------------------------------------------------- #
    # Edge case: entire array as one query
    # ---------------------------------------------------------------------- #
    scores_4: List[int] = [10, 20, 30, 40, 50]
    queries_4: List[List[int]] = [[0, 4]]
    result_4: List[float] = solution.averageScoreOfSegments(scores_4, queries_4)
    print("Edge case — full array query")
    print(f"  scores  : {scores_4}")
    print(f"  queries : {queries_4}")
    print(f"  output  : {result_4}")          # Expected: [30.0]