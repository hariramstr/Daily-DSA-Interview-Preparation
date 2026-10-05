"""
Title: Maximum Score from Choosing a Centered Triple

Problem Description:
You are given an integer array nums representing signal strengths recorded over time.
A valid centered triple is a choice of three indices (i, j, k) such that:

    i < j < k
    nums[i] < nums[j]
    nums[k] < nums[j]

In other words, the middle element must be strictly greater than one element on its
left and one element on its right. The score of such a triple is:

    nums[i] + nums[j] + nums[k]

Return the maximum possible score among all valid centered triples. If no valid
centered triple exists, return -1.

This problem asks you to efficiently evaluate each position as the center of a
peak-like triple. A brute-force solution that tries all triples is too slow for
large inputs. You need to determine, for each index j, the best smaller value on
the left and the best smaller value on the right that can pair with nums[j] to
maximize the total score.

Constraints:
- 3 <= nums.length <= 200000
- 1 <= nums[i] <= 1000000000
- All values fit in 32-bit signed integers, but the answer may require 64-bit
  arithmetic in some languages.

Example 1:
Input: nums = [4, 9, 6, 3, 8]
Output: 19
Explanation:
Choose indices (0, 1, 2) with values (4, 9, 6). They satisfy 4 < 9 and 6 < 9,
so the score is 4 + 9 + 6 = 19. Another valid choice is (0, 1, 3) with score 16,
which is smaller.

Example 2:
Input: nums = [5, 5, 5, 5]
Output: -1
Explanation:
No index can be the center of a valid triple because the inequalities must be strict.
Equal values do not qualify.
"""

from bisect import bisect_left
from typing import List


class FenwickMax:
    """Fenwick Tree (Binary Indexed Tree) supporting prefix maximum queries."""

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
        Update one position with a value, keeping maximums in Fenwick structure.

        Args:
            index: 1-based index to update.
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
        Query the maximum value in prefix [1..index].

        Args:
            index: 1-based right boundary of prefix.

        Returns:
            Maximum value in the prefix, or 0 if none exists.

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
    def maxScore(self, nums: List[int]) -> int:
        """
        Compute the maximum score of a valid centered triple.

        The key idea is:
        - For each position j, we want the largest value on the left that is
          strictly smaller than nums[j].
        - We also want the largest value on the right that is strictly smaller
          than nums[j].
        - If both exist, then nums[j] can serve as the center, and the score is:
              best_left_smaller + nums[j] + best_right_smaller
        - We maximize this over all j.

        To do this efficiently for large arrays, we use:
        - Coordinate compression on values
        - Fenwick trees for prefix maximum queries

        Args:
            nums: List of signal strengths.

        Returns:
            Maximum score of any valid centered triple, or -1 if none exists.

        Time complexity:
            O(n log n)

        Space complexity:
            O(n)
        """
        n: int = len(nums)

        # ------------------------------------------------------------
        # Step 1: Coordinate compression
        # ------------------------------------------------------------
        # Why do we need this?
        # The values in nums can be as large as 1,000,000,000, which is too large
        # to use directly as Fenwick tree indices.
        #
        # Coordinate compression maps each distinct value to a small rank:
        # for example, values [3, 8, 9] become ranks [1, 2, 3].
        #
        # This allows us to ask:
        # "What is the maximum value among all previously seen values with rank
        # strictly less than the current rank?"
        #
        # Since strict inequality is required (nums[i] < nums[j]), querying ranks
        # less than the current rank naturally enforces that rule.
        sorted_unique: List[int] = sorted(set(nums))

        # ------------------------------------------------------------
        # Step 2: Compute best smaller value on the left for every index
        # ------------------------------------------------------------
        # left_best[j] will store the largest value nums[i] such that:
        #   i < j and nums[i] < nums[j]
        #
        # If no such value exists, left_best[j] remains 0.
        #
        # Why is "largest smaller value" the correct choice?
        # Because nums[j] is fixed when j is the center, so to maximize:
        #   nums[i] + nums[j] + nums[k]
        # we should choose the largest valid nums[i] on the left and the largest
        # valid nums[k] on the right independently.
        left_best: List[int] = [0] * n
        left_tree = FenwickMax(len(sorted_unique))

        # We scan from left to right.
        # Before processing nums[j], the Fenwick tree contains information about
        # values seen at indices < j.
        for j, value in enumerate(nums):
            # Find the compressed rank of the current value.
            # bisect_left gives a 0-based position in sorted_unique.
            # We convert it to 1-based rank for Fenwick tree usage.
            rank: int = bisect_left(sorted_unique, value) + 1

            # Query all ranks strictly smaller than current rank.
            # This gives the largest value seen so far that is < value.
            left_best[j] = left_tree.query(rank - 1)

            # Now insert the current value into the Fenwick tree so that future
            # positions can use it as a candidate left element.
            left_tree.update(rank, value)

        # ------------------------------------------------------------
        # Step 3: Compute best smaller value on the right for every index
        # ------------------------------------------------------------
        # right_best[j] will store the largest value nums[k] such that:
        #   k > j and nums[k] < nums[j]
        #
        # Again, if no such value exists, right_best[j] remains 0.
        #
        # We use the same idea as the left side, but scan from right to left.
        right_best: List[int] = [0] * n
        right_tree = FenwickMax(len(sorted_unique))

        for j in range(n - 1, -1, -1):
            value = nums[j]
            rank = bisect_left(sorted_unique, value) + 1

            # Query values strictly smaller than nums[j] among elements to the right.
            right_best[j] = right_tree.query(rank - 1)

            # Insert current value so positions further left can use it.
            right_tree.update(rank, value)

        # ------------------------------------------------------------
        # Step 4: Evaluate every index as the center of the triple
        # ------------------------------------------------------------
        # A valid center j needs:
        #   left_best[j] > 0   and   right_best[j] > 0
        #
        # Since all nums[i] are positive (constraint says nums[i] >= 1),
        # using 0 as "not found" is safe.
        #
        # For each valid center, compute:
        #   left_best[j] + nums[j] + right_best[j]
        # and keep the maximum.
        answer: int = -1

        for j in range(n):
            if left_best[j] > 0 and right_best[j] > 0:
                score: int = left_best[j] + nums[j] + right_best[j]
                if score > answer:
                    answer = score

        return answer


if __name__ == "__main__":
    solution = Solution()

    # Example 1 from the problem statement
    nums1: List[int] = [4, 9, 6, 3, 8]
    result1: int = solution.maxScore(nums1)
    print(f"Input: {nums1}")
    print(f"Output: {result1}")
    print("Expected: 19")
    print()

    # Example 2 from the problem statement
    nums2: List[int] = [5, 5, 5, 5]
    result2: int = solution.maxScore(nums2)
    print(f"Input: {nums2}")
    print(f"Output: {result2}")
    print("Expected: -1")
    print()

    # Additional quick checks
    nums3: List[int] = [1, 3, 2]
    result3: int = solution.maxScore(nums3)
    print(f"Input: {nums3}")
    print(f"Output: {result3}")
    print("Expected: 6")
    print()

    nums4: List[int] = [9, 8, 7, 6]
    result4: int = solution.maxScore(nums4)
    print(f"Input: {nums4}")
    print(f"Output: {result4}")
    print("Expected: -1")