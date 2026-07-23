"""
Title: Maximum Score from Picking a Centered Product Trio

Problem Description:
You are given an integer array ratings where ratings[i] is the popularity score of the
i-th product in a catalog. A valid centered product trio is formed by choosing three
indices (l, c, r) such that l < c < r, ratings[l] < ratings[c], and ratings[r] < ratings[c].
In other words, the center product must have a strictly higher rating than one product
on its left and one product on its right. The score of such a trio is
ratings[l] + ratings[c] + ratings[r].

Return the maximum possible score among all valid centered product trios.
If no valid trio exists, return -1.

Constraints:
- 3 <= ratings.length <= 100000
- 1 <= ratings[i] <= 1000000000
- All values fit in 64-bit signed integers when summed

Examples:
1)
Input: ratings = [4, 9, 2, 7, 3]
Output: 16
Explanation:
Valid choices include:
- (0, 1, 2) -> 4 + 9 + 2 = 15
- (0, 1, 4) -> 4 + 9 + 3 = 16
- (2, 3, 4) -> 2 + 7 + 3 = 12
Maximum is 16.

2)
Input: ratings = [1, 2, 3, 4]
Output: -1
Explanation:
No element has a smaller value on both its left and right, so no valid trio exists.
"""

from typing import List


class FenwickTreeMax:
    """Fenwick Tree (Binary Indexed Tree) supporting prefix maximum queries."""

    def __init__(self, size: int) -> None:
        """
        Initialize the Fenwick tree.

        Args:
            size: Number of positions managed by the tree.

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
        Set tree positions so that prefix maximum queries can see this value.

        Args:
            index: 1-based compressed index to update.
            value: Value to merge into the structure using max.

        Returns:
            None

        Time complexity:
            O(log size)

        Space complexity:
            O(1)
        """
        while index <= self.size:
            if value > self.tree[index]:
                self.tree[index] = value
            index += index & -index

    def query(self, index: int) -> int:
        """
        Return the maximum value stored in the prefix [1..index].

        Args:
            index: 1-based compressed index for the prefix end.

        Returns:
            The maximum value in that prefix, or 0 if nothing was stored there.

        Time complexity:
            O(log size)

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
        Compute the maximum score of a valid centered product trio.

        A valid trio chooses indices (l, c, r) such that:
        - l < c < r
        - ratings[l] < ratings[c]
        - ratings[r] < ratings[c]

        To maximize the score for a fixed center c, we want:
        - the largest value on the left that is still < ratings[c]
        - the largest value on the right that is still < ratings[c]

        We compute those efficiently using coordinate compression plus two
        Fenwick trees for prefix maximum queries.

        Args:
            ratings: List of product popularity scores.

        Returns:
            The maximum trio score, or -1 if no valid trio exists.

        Time complexity:
            O(n log n)

        Space complexity:
            O(n)
        """
        n: int = len(ratings)

        # Coordinate compression:
        # The values can be as large as 1e9, which is too large to use directly
        # as Fenwick tree indices.
        #
        # Compression maps each distinct rating value to a small rank in [1..m].
        # The relative ordering is preserved, which is exactly what we need for
        # "strictly smaller than" comparisons.
        sorted_unique: List[int] = sorted(set(ratings))
        rank_map = {value: index + 1 for index, value in enumerate(sorted_unique)}
        compressed: List[int] = [rank_map[value] for value in ratings]
        m: int = len(sorted_unique)

        # left_best_smaller[i] will store:
        # the largest rating value that appears somewhere to the LEFT of i
        # and is strictly smaller than ratings[i].
        #
        # If no such value exists, it stays 0.
        left_best_smaller: List[int] = [0] * n

        # This Fenwick tree stores actual rating values, indexed by compressed rank.
        # Querying prefix (rank - 1) gives the largest value among all previously
        # seen elements whose value is strictly smaller than the current value.
        left_tree = FenwickTreeMax(m)

        # Left-to-right pass:
        # For each position i, we first ask:
        # "Among values smaller than ratings[i] that we have already seen,
        #  what is the largest one?"
        #
        # Then we insert ratings[i] itself into the structure for future positions.
        for i in range(n):
            current_rank: int = compressed[i]
            current_value: int = ratings[i]

            # Query only ranks strictly smaller than current_rank.
            # This enforces the condition ratings[l] < ratings[c].
            left_best_smaller[i] = left_tree.query(current_rank - 1)

            # Add current value so later indices can use it as a left candidate.
            left_tree.update(current_rank, current_value)

        # right_best_smaller[i] will store:
        # the largest rating value that appears somewhere to the RIGHT of i
        # and is strictly smaller than ratings[i].
        #
        # If no such value exists, it stays 0.
        right_best_smaller: List[int] = [0] * n

        # Same idea, but now we scan from right to left so the Fenwick tree
        # represents elements that are to the RIGHT of the current index.
        right_tree = FenwickTreeMax(m)

        # Right-to-left pass:
        # For each position i, we ask:
        # "Among values smaller than ratings[i] that appear to the right,
        #  what is the largest one?"
        #
        # Then we insert ratings[i] for positions further left.
        for i in range(n - 1, -1, -1):
            current_rank = compressed[i]
            current_value = ratings[i]

            # Again, query only strictly smaller values.
            right_best_smaller[i] = right_tree.query(current_rank - 1)

            # Add current value for future queries from the left side.
            right_tree.update(current_rank, current_value)

        # Now evaluate every index as the center of the trio.
        #
        # A center is valid only if:
        # - there exists at least one smaller value on the left
        # - there exists at least one smaller value on the right
        #
        # Because ratings are positive integers (>= 1), using 0 as "not found"
        # is safe and unambiguous.
        best_score: int = -1

        for i in range(n):
            left_value: int = left_best_smaller[i]
            right_value: int = right_best_smaller[i]

            # If either side has no valid smaller value, this index cannot be
            # the center of a valid trio.
            if left_value == 0 or right_value == 0:
                continue

            # For a fixed center, choosing the largest valid left and right values
            # clearly maximizes the sum.
            current_score: int = left_value + ratings[i] + right_value

            if current_score > best_score:
                best_score = current_score

        return best_score


if __name__ == "__main__":
    solution = Solution()

    sample_inputs: List[List[int]] = [
        [4, 9, 2, 7, 3],
        [1, 2, 3, 4],
        [5, 1, 6, 2, 7, 3],
        [3, 8, 5, 7, 2],
    ]

    for ratings in sample_inputs:
        result = solution.maximumScore(ratings)
        print(f"ratings = {ratings}")
        print(f"maximum score = {result}")
        print("-" * 40)