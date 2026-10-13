"""
Title: Maximum Ribbon Cut Gain from One Interior Slice

Problem Description:
You are given an integer array values where values[i] represents the profit contribution
of the i-th ribbon segment in a production line. You must choose exactly one contiguous
subarray to keep, but before finalizing it, you are allowed to make one special operation:
remove exactly one interior segment from that chosen subarray. The removed segment cannot
be the first or last element of the chosen subarray. After the removal, the remaining left
and right parts are stitched together, and the score of the choice is the sum of all
remaining kept segments. Your task is to return the maximum possible score.

In other words, choose indices l < k < r and maximize the sum of values[l..r] except
values[k]. The chosen kept structure must still have at least one segment on both sides
of the removed interior segment.

This is not the same as deleting any element from the full array. The removed element must
lie strictly inside the selected range, so the final kept result is formed by a non-empty
prefix part and a non-empty suffix part of one larger contiguous block.

Constraints:
- 3 <= values.length <= 200000
- -1000000000 <= values[i] <= 1000000000
- The answer fits in a signed 64-bit integer.
"""

from typing import List


class Solution:
    def maximum_ribbon_cut_gain(self, values: List[int]) -> int:
        """
        Compute the maximum possible score after choosing one contiguous subarray
        and removing exactly one interior element from it.

        The final kept structure must contain:
        - at least one element on the left of the removed element
        - at least one element on the right of the removed element

        A very useful reformulation is:
        for each possible removed index k, we want:
            best non-empty subarray ending at k - 1
            +
            best non-empty subarray starting at k + 1

        Then the answer is the maximum of that sum over all valid k.

        Args:
            values: List of integer segment profits.

        Returns:
            The maximum achievable score as an integer.

        Time complexity:
            O(n), where n is the length of values.

        Space complexity:
            O(n), for the prefix and suffix helper arrays.
        """
        n: int = len(values)

        # ---------------------------------------------------------------------
        # Step 1: Build an array "best_end_here".
        #
        # best_end_here[i] means:
        #   the maximum sum of any NON-EMPTY contiguous subarray that ends exactly at index i.
        #
        # This is the classic Kadane transition for "best subarray ending here":
        #   best_end_here[i] = max(values[i], best_end_here[i - 1] + values[i])
        #
        # Why do we need this?
        # If we decide to remove index k, then the left kept part must be a non-empty
        # contiguous subarray that ends at k - 1. Among all such choices, we want the best one.
        # That exact quantity is best_end_here[k - 1].
        # ---------------------------------------------------------------------
        best_end_here: List[int] = [0] * n
        best_end_here[0] = values[0]

        for i in range(1, n):
            # Either:
            # 1) start a new subarray at i
            # 2) extend the best subarray that ended at i - 1
            best_end_here[i] = max(values[i], best_end_here[i - 1] + values[i])

        # ---------------------------------------------------------------------
        # Step 2: Build an array "best_start_here".
        #
        # best_start_here[i] means:
        #   the maximum sum of any NON-EMPTY contiguous subarray that starts exactly at index i.
        #
        # This is the symmetric version of Kadane from right to left:
        #   best_start_here[i] = max(values[i], values[i] + best_start_here[i + 1])
        #
        # Why do we need this?
        # If we remove index k, then the right kept part must be a non-empty contiguous
        # subarray that starts at k + 1. Among all such choices, we want the best one.
        # That exact quantity is best_start_here[k + 1].
        # ---------------------------------------------------------------------
        best_start_here: List[int] = [0] * n
        best_start_here[n - 1] = values[n - 1]

        for i in range(n - 2, -1, -1):
            # Either:
            # 1) start a new subarray at i
            # 2) extend the best subarray that starts at i + 1
            best_start_here[i] = max(values[i], values[i] + best_start_here[i + 1])

        # ---------------------------------------------------------------------
        # Step 3: Try every possible removed index k.
        #
        # Since the removed element must be strictly interior to the chosen original subarray,
        # there must be at least one kept element on both sides.
        #
        # Therefore:
        #   k can only be in [1, n - 2]
        #
        # For each such k:
        #   score = best_end_here[k - 1] + best_start_here[k + 1]
        #
        # Why is this correct?
        # - The left kept part must end immediately before the removed index in the original
        #   chosen block after trimming away any unnecessary worse prefix.
        # - The right kept part must start immediately after the removed index in the original
        #   chosen block after trimming away any unnecessary worse suffix.
        #
        # More concretely, any valid choice l < k < r gives:
        #   sum(values[l..k-1]) + sum(values[k+1..r])
        #
        # The best possible left contribution for this k is exactly the best non-empty subarray
        # ending at k - 1.
        # The best possible right contribution for this k is exactly the best non-empty subarray
        # starting at k + 1.
        #
        # So taking the maximum over all k gives the global optimum.
        # ---------------------------------------------------------------------
        answer: int = -(10 ** 30)

        for k in range(1, n - 1):
            current_score: int = best_end_here[k - 1] + best_start_here[k + 1]
            answer = max(answer, current_score)

        return answer

    def solve(self, values: List[int]) -> int:
        """
        Convenience wrapper around the main algorithm.

        Args:
            values: List of integer segment profits.

        Returns:
            The maximum achievable score.

        Time complexity:
            O(n)

        Space complexity:
            O(n)
        """
        return self.maximum_ribbon_cut_gain(values)


if __name__ == "__main__":
    solution = Solution()

    # Example 1:
    # values = [4, -7, 5, 6]
    # Best choice:
    # - choose the whole array
    # - remove interior element -7
    # Remaining score = 4 + 5 + 6 = 15
    sample_1: List[int] = [4, -7, 5, 6]
    result_1: int = solution.solve(sample_1)
    print(f"Input: {sample_1}")
    print(f"Output: {result_1}")
    print("Expected: 15")
    print()

    # Example 2:
    # values = [3, -2, 4, -1, 2]
    # Best choice:
    # - choose the whole array
    # - remove interior element -2
    # Remaining score = 3 + 4 + (-1) + 2 = 8
    sample_2: List[int] = [3, -2, 4, -1, 2]
    result_2: int = solution.solve(sample_2)
    print(f"Input: {sample_2}")
    print(f"Output: {result_2}")
    print("Expected: 8")
    print()

    # Additional quick checks for beginner-friendly demonstration.

    # Minimum length case:
    # Only one possible removed index (the middle one).
    sample_3: List[int] = [1, -5, 2]
    result_3: int = solution.solve(sample_3)
    print(f"Input: {sample_3}")
    print(f"Output: {result_3}")
    print("Expected: 3")
    print()

    # All negative values:
    # We still must keep one non-empty part on each side of the removed element.
    sample_4: List[int] = [-5, -2, -3, -4]
    result_4: int = solution.solve(sample_4)
    print(f"Input: {sample_4}")
    print(f"Output: {result_4}")
    print("One optimal choice removes -3 from [-2, -3, -4]? Not valid because left must be before removed.")
    print("Actual algorithm computes the best valid score automatically.")
    print()

    # Mixed values:
    sample_5: List[int] = [10, -100, 20, 30, -5, 40]
    result_5: int = solution.solve(sample_5)
    print(f"Input: {sample_5}")
    print(f"Output: {result_5}")