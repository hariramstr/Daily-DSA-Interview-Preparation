"""
Title: Maximum Uniform Poster Height for Campus Boards

Problem Description:
A university is preparing posters for an event and wants every poster placed on campus
boards to have the same height. You are given an array boards, where boards[i] is the
height of the i-th available board material strip. A strip can be cut into smaller
poster pieces, but pieces cannot be joined together. Every poster must have exactly
the same integer height h, and each cut piece used as a poster must come from a single
strip. You are also given an integer k, the minimum number of posters the university
needs.

Return the maximum possible integer poster height h such that it is possible to cut
at least k posters from the given strips. If it is impossible to make even k posters
of height 1, return 0.

For a chosen height h, a strip of height x can contribute floor(x / h) posters.
Your task is to find the largest valid h efficiently.

This problem is designed for a binary search on the answer: if a height h is feasible,
then any smaller positive height is also feasible. Use that monotonic property to
search the answer space.

Constraints:
- 1 <= boards.length <= 200000
- 1 <= boards[i] <= 1000000000
- 1 <= k <= 1000000000
- The answer must be an integer.

Example 1:
Input: boards = [8, 5, 8], k = 5
Output: 4
Explanation:
- Height 3 produces floor(8/3) + floor(5/3) + floor(8/3) = 2 + 1 + 2 = 5 posters.
- Height 4 produces floor(8/4) + floor(5/4) + floor(8/4) = 2 + 1 + 2 = 5 posters.
- Height 5 produces floor(8/5) + floor(5/5) + floor(8/5) = 1 + 1 + 1 = 3 posters.
Therefore, the maximum valid height is 4.

Example 2:
Input: boards = [2, 3], k = 10
Output: 0
Explanation:
Even with height 1, the total number of posters is 2 + 3 = 5, which is less than 10,
so the requirement cannot be met.
"""

from typing import List


class Solution:
    def _can_make_at_least_k(self, boards: List[int], k: int, height: int) -> bool:
        """
        Check whether a given poster height is feasible.

        A height is feasible if, after cutting every strip independently into pieces
        of exactly `height`, the total number of pieces is at least `k`.

        Args:
            boards: List of strip heights.
            k: Minimum number of posters required.
            height: Candidate uniform poster height to test.

        Returns:
            True if at least k posters can be produced, otherwise False.

        Time complexity:
            O(n), where n is the number of strips.

        Space complexity:
            O(1), ignoring input storage.
        """
        # This variable accumulates how many posters we can make in total
        # using the current candidate height.
        total_posters: int = 0

        # We inspect every strip exactly once.
        for strip_height in boards:
            # A strip of size `strip_height` can produce:
            # floor(strip_height / height) posters of exact height `height`.
            total_posters += strip_height // height

            # Important optimization:
            # As soon as we already know we can make at least k posters,
            # we can stop early and return True.
            #
            # Why is this safe?
            # Because the question is only whether the total is >= k.
            # Once we reach that threshold, additional strips do not change
            # the answer from True to False.
            if total_posters >= k:
                return True

        # If we finish the loop and still have fewer than k posters,
        # then this height is not feasible.
        return False

    def maximum_uniform_poster_height(self, boards: List[int], k: int) -> int:
        """
        Find the maximum integer poster height that allows making at least k posters.

        This uses binary search on the answer:
        - If a height h works, then every smaller positive height also works.
        - If a height h does not work, then every larger height also does not work.

        Args:
            boards: List of strip heights.
            k: Minimum number of posters required.

        Returns:
            The largest feasible integer poster height, or 0 if even height 1
            cannot produce at least k posters.

        Time complexity:
            O(n log M), where:
            - n is the number of strips
            - M is the maximum strip height

        Space complexity:
            O(1), ignoring input storage.
        """
        # Defensive handling:
        # If the list is empty (not expected by constraints, but safe to handle),
        # then no posters can be made.
        if not boards:
            return 0

        # Quick impossibility check:
        # If height = 1 still cannot produce at least k posters,
        # then no larger height can possibly work.
        #
        # Why?
        # Because increasing the required height can only reduce or keep the same
        # the number of obtainable pieces from each strip.
        if sum(boards) < k:
            return 0

        # Binary search boundaries:
        #
        # The smallest meaningful positive height is 1.
        # The largest possible height cannot exceed the tallest strip,
        # because no poster can be taller than the strip it comes from.
        left: int = 1
        right: int = max(boards)

        # This variable stores the best feasible answer found so far.
        # We start with 0 in case no positive height works,
        # though the earlier impossibility check already handles that case.
        best_height: int = 0

        # Standard binary search over the answer space.
        while left <= right:
            # Middle candidate height.
            # We use integer division because heights must be integers.
            mid: int = (left + right) // 2

            # Check whether this candidate height is feasible.
            if self._can_make_at_least_k(boards, k, mid):
                # If `mid` works, then it is a valid answer.
                # But we want the MAXIMUM valid height, so we record it
                # and continue searching to the right for possibly larger heights.
                best_height = mid
                left = mid + 1
            else:
                # If `mid` does not work, then any height larger than `mid`
                # also cannot work due to the monotonic property.
                # Therefore, we discard the right half including `mid`
                # and continue searching smaller heights.
                right = mid - 1

        # After the loop, `best_height` contains the largest feasible height found.
        return best_height


if __name__ == "__main__":
    solution = Solution()

    # Example 1
    boards_1: List[int] = [8, 5, 8]
    k_1: int = 5
    result_1: int = solution.maximum_uniform_poster_height(boards_1, k_1)
    print("Example 1:")
    print(f"boards = {boards_1}, k = {k_1}")
    print(f"Maximum uniform poster height = {result_1}")
    print("Expected = 4")
    print()

    # Example 2
    boards_2: List[int] = [2, 3]
    k_2: int = 10
    result_2: int = solution.maximum_uniform_poster_height(boards_2, k_2)
    print("Example 2:")
    print(f"boards = {boards_2}, k = {k_2}")
    print(f"Maximum uniform poster height = {result_2}")
    print("Expected = 0")
    print()

    # Additional beginner-friendly test cases
    boards_3: List[int] = [5, 5, 5]
    k_3: int = 3
    result_3: int = solution.maximum_uniform_poster_height(boards_3, k_3)
    print("Additional Test 1:")
    print(f"boards = {boards_3}, k = {k_3}")
    print(f"Maximum uniform poster height = {result_3}")
    print("Expected = 5")
    print()

    boards_4: List[int] = [9]
    k_4: int = 2
    result_4: int = solution.maximum_uniform_poster_height(boards_4, k_4)
    print("Additional Test 2:")
    print(f"boards = {boards_4}, k = {k_4}")
    print(f"Maximum uniform poster height = {result_4}")
    print("Expected = 4")
    print()

    boards_5: List[int] = [1, 2, 3, 4, 5]
    k_5: int = 7
    result_5: int = solution.maximum_uniform_poster_height(boards_5, k_5)
    print("Additional Test 3:")
    print(f"boards = {boards_5}, k = {k_5}")
    print(f"Maximum uniform poster height = {result_5}")
    print("Expected = 2")