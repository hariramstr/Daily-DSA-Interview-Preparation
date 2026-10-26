"""
Title: Maximum Score from Choosing a Stable Product Trio

Problem Description:
You are given an integer array ratings where ratings[i] is the quality rating of the i-th
product in a catalog. A merchandising team wants to highlight exactly three products in
increasing index order: left, middle, and right such that left < middle < right.

The middle product is considered stable only if its rating is strictly greater than the
rating of the left product and strictly less than the rating of the right product.
In other words, the chosen ratings must form a strictly increasing trio when read
from left to right.

The score of a valid trio is defined as:
    ratings[left] + ratings[middle] + ratings[right]

Your task is to return the maximum possible score among all valid trios.
If no valid trio exists, return -1.

Constraints:
- 3 <= ratings.length <= 2 * 10^5
- 1 <= ratings[i] <= 10^9

Goal:
Design an efficient solution for large arrays without checking all O(n^3) trios.
"""

from bisect import bisect_left
from typing import List


class FenwickMax:
    """Fenwick Tree (Binary Indexed Tree) for prefix maximum queries."""

    def __init__(self, size: int) -> None:
        """
        Initialize the Fenwick tree.

        Args:
            size: Number of positions in the tree.

        Returns:
            None

        Time complexity:
            O(size)

        Space complexity:
            O(size)
        """
        self.size: int = size
        self.tree: List[int] = [0] * (size + 1)

    def update(self, index: int, value: int) -> None:
        """
        Update one position with a new value, keeping the maximum.

        Args:
            index: 1-based index in the Fenwick tree.
            value: Value to merge into the structure.

        Returns:
            None

        Time complexity:
            O(log n)

        Space complexity:
            O(1)
        """
        while index <= self.size:
            if value > self.tree[index]:
                self.tree[index] = value
            index += index & -index

    def query(self, index: int) -> int:
        """
        Query the maximum value in the prefix [1..index].

        Args:
            index: 1-based right boundary of the prefix.

        Returns:
            The maximum value stored in that prefix, or 0 if none exists.

        Time complexity:
            O(log n)

        Space complexity:
            O(1)
        """
        result: int = 0
        while index > 0:
            if self.tree[index] > result:
                result = self.tree[index]
            index -= index & -index
        return result


class Solution:
    def maximumScore(self, ratings: List[int]) -> int:
        """
        Compute the maximum score of a strictly increasing trio.

        We need indices left < middle < right such that:
            ratings[left] < ratings[middle] < ratings[right]

        For each possible middle position, we want:
        1. The largest value on the left that is strictly smaller than ratings[middle]
        2. The largest value on the right that is strictly greater than ratings[middle]

        Then the best trio using this middle is:
            best_left + ratings[middle] + best_right

        To do this efficiently:
        - We coordinate-compress the values because ratings can be as large as 1e9.
        - We use a Fenwick tree from left to right to get the best smaller value on the left.
        - We precompute, for every compressed rank, the maximum original value that appears
          to its right or at higher ranks. This lets us quickly find the best greater value
          on the right for each middle.

        Args:
            ratings: List of product ratings.

        Returns:
            The maximum score of any valid strictly increasing trio, or -1 if none exists.

        Time complexity:
            O(n log n)

        Space complexity:
            O(n)
        """
        n: int = len(ratings)

        # Step 1: Coordinate compression
        # --------------------------------
        # The values can be very large (up to 1e9), so we compress them into ranks:
        # sorted_unique = sorted distinct values
        # each rating gets a rank in [1..m]
        #
        # Why do this?
        # Fenwick trees work naturally on dense integer indices.
        sorted_unique: List[int] = sorted(set(ratings))
        m: int = len(sorted_unique)

        # Convert each rating into a 1-based rank.
        ranks: List[int] = [bisect_left(sorted_unique, value) + 1 for value in ratings]

        # Step 2: Precompute the best strictly greater value on the right for each index
        # ------------------------------------------------------------------------------
        # We want, for each position i used as the middle:
        #   best_right[i] = maximum rating[j] for j > i and rating[j] > rating[i]
        #
        # A direct approach would be too slow if repeated for every i.
        #
        # Instead:
        # - We scan from right to left.
        # - We maintain, for each compressed rank, the maximum original value seen so far
        #   with exactly that rank on the right side.
        # - Then we build a suffix maximum array over ranks so that:
        #       suffix_best_greater[r] = maximum original value among ranks >= r
        #
        # For current rank r, the best strictly greater value is:
        #       suffix_best_greater[r + 1]
        #
        # Because ranks > r correspond exactly to values > current value.
        best_right: List[int] = [0] * n

        # exact_max_by_rank[rank] = maximum original rating seen to the right with this exact rank
        exact_max_by_rank: List[int] = [0] * (m + 2)

        # suffix_best_greater[rank] = maximum original rating among exact_max_by_rank[rank..m]
        suffix_best_greater: List[int] = [0] * (m + 3)

        for i in range(n - 1, -1, -1):
            current_rank: int = ranks[i]

            # Query the best value on the right with strictly greater rank.
            # Since suffix_best_greater stores max over [rank..m],
            # asking for current_rank + 1 gives us values strictly greater than current.
            best_right[i] = suffix_best_greater[current_rank + 1]

            # Now insert the current value into the right-side structures for future positions.
            current_value: int = ratings[i]
            if current_value > exact_max_by_rank[current_rank]:
                exact_max_by_rank[current_rank] = current_value

                # Rebuild suffix maxima only as far as needed.
                # We update from current_rank down to 1 until values stop changing.
                #
                # This local propagation works because only one exact rank changed.
                # suffix_best_greater[r] = max(exact_max_by_rank[r], suffix_best_greater[r + 1])
                #
                # In the worst case, repeated full propagation would be too slow.
                # So instead of rebuilding all suffix values every time, we only propagate
                # while the new value improves the suffix.
                new_value: int = exact_max_by_rank[current_rank]
                r: int = current_rank
                while r >= 1:
                    updated: int = max(exact_max_by_rank[r], suffix_best_greater[r + 1])
                    if updated == suffix_best_greater[r]:
                        break
                    suffix_best_greater[r] = updated
                    r -= 1

        # Step 3: Scan from left to right to find the best smaller value on the left
        # --------------------------------------------------------------------------
        # We use a Fenwick tree where:
        # - index = compressed rank
        # - stored value = maximum original rating seen so far with that rank
        #
        # For current middle with rank r:
        # - all strictly smaller values have ranks in [1..r-1]
        # - so best_left = fenwick.query(r - 1)
        #
        # Then if both best_left and best_right[i] exist, we can form a valid trio.
        fenwick: FenwickMax = FenwickMax(m)
        answer: int = -1

        for i in range(n):
            current_value = ratings[i]
            current_rank = ranks[i]

            # Query the largest value on the left that is strictly smaller.
            best_left: int = fenwick.query(current_rank - 1)

            # best_right[i] was already precomputed.
            right_value: int = best_right[i]

            # A valid trio exists only if both sides are non-zero.
            # Since ratings are positive integers, zero safely means "not found".
            if best_left > 0 and right_value > 0:
                candidate_score: int = best_left + current_value + right_value
                if candidate_score > answer:
                    answer = candidate_score

            # After processing this index as a possible middle,
            # insert it into the left-side structure for future indices.
            fenwick.update(current_rank, current_value)

        return answer


if __name__ == "__main__":
    solution = Solution()

    sample_inputs: List[List[int]] = [
        [4, 2, 6, 3, 8],
        [9, 7, 5, 4],
    ]

    for ratings in sample_inputs:
        result = solution.maximumScore(ratings)
        print(f"ratings = {ratings}")
        print(f"maximum score = {result}")
        print()

    # Expected:
    # [4, 2, 6, 3, 8] -> 18
    # [9, 7, 5, 4] -> -1