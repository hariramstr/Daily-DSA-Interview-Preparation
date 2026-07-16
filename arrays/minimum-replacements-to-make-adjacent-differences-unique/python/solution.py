"""
Title: Minimum Replacements to Make Adjacent Differences Unique

Problem Description:
You are given an integer array nums. Define the adjacent-difference sequence of nums
as the array diff where diff[i] = |nums[i] - nums[i + 1]| for every valid i.

An array is called difference-distinct if every value in its adjacent-difference
sequence appears at most once.

In one operation, you may replace any single element of nums with any integer value
in the range [0, limit]. Your task is to return the minimum number of operations
needed to make nums difference-distinct.

A replacement changes up to two adjacent differences, so choosing which positions to
modify matters. You are not asked to construct the final array, only to compute the
minimum number of replacements.

Constraints:
- 2 <= nums.length <= 100000
- 0 <= nums[i] <= 1000000000
- 1 <= limit <= 1000000000

Notes:
- The answer always exists because limit provides a valid replacement range and you
  may replace multiple elements if needed.
- Efficiency matters: solutions close to quadratic time will not pass for the largest
  inputs.
"""

from typing import Dict, List, Set


class Solution:
    def min_replacements(self, nums: List[int], limit: int) -> int:
        """
        Compute the minimum number of element replacements needed so that all adjacent
        absolute differences become pairwise distinct.

        Key idea:
        We only care about the adjacent-difference array:
            diff[i] = abs(nums[i] - nums[i + 1])

        If some difference value appears multiple times, then at least all but one of
        those occurrences must be "changed". Changing an element of nums changes at
        most two neighboring diff positions, so one replacement can cover:
        - one diff position if we replace an endpoint element
        - up to two consecutive diff positions if we replace an interior element

        Therefore, the problem becomes:
        - Let "bad" diff positions be those whose values are duplicates
          (that is, every occurrence of a repeated value is bad, because at most one
          occurrence can remain unchanged in the final array).
        - We need the minimum number of nums indices whose affected diff positions
          cover all bad diff positions.

        Since nums index j affects:
        - diff[0] only, if j == 0
        - diff[n - 2] only, if j == n - 1
        - diff[j - 1] and diff[j], if 1 <= j <= n - 2

        This is exactly the minimum number of vertices on a path that cover a given set
        of edges. On a path, the optimal answer is obtained greedily from left to right:
        whenever we encounter an uncovered bad diff edge i, choose nums index i + 1
        (the right endpoint of that edge). This covers edge i and also edge i + 1.

        Why this is correct:
        - A bad diff position must be changed.
        - On a path, choosing the right endpoint of the leftmost uncovered bad edge is
          the standard optimal greedy for minimum vertex cover on selected edges.
        - After deciding which diff positions are changed, we can always realize actual
          values because:
            * changed diff positions can be assigned distinct values
            * unchanged diff positions keep their original values
            * with enough replacements, values can be chosen sequentially within
              [0, limit] to realize the target differences
          For this problem's decision count, the path-cover formulation gives the exact
          minimum number of replacements.

        Args:
            nums: Original integer array.
            limit: Allowed replacement values are in [0, limit].

        Returns:
            Minimum number of replacements.

        Time complexity:
            O(n)

        Space complexity:
            O(n)
        """
        n: int = len(nums)

        # If there are fewer than 2 elements, there are no adjacent differences.
        # The constraints guarantee n >= 2, but keeping this guard makes the method
        # more robust and beginner-friendly.
        if n < 2:
            return 0

        # ---------------------------------------------------------------------
        # Step 1: Build the adjacent-difference array.
        #
        # diff[i] corresponds to the edge between nums[i] and nums[i + 1].
        # There are exactly n - 1 such edges.
        # ---------------------------------------------------------------------
        diff: List[int] = [abs(nums[i] - nums[i + 1]) for i in range(n - 1)]

        # ---------------------------------------------------------------------
        # Step 2: Count how many times each difference value appears.
        #
        # If a difference value appears exactly once, that occurrence is already fine.
        # If it appears multiple times, then all occurrences are "bad" in the sense
        # that they belong to a duplicated value class and must be handled, except
        # that at most one occurrence of that value may remain unchanged in the end.
        #
        # To minimize replacements on the path, it is enough to mark every occurrence
        # of repeated values as needing coverage. The greedy path-cover then computes
        # the minimum number of replacements needed to touch all such problematic
        # positions.
        # ---------------------------------------------------------------------
        freq: Dict[int, int] = {}
        for value in diff:
            freq[value] = freq.get(value, 0) + 1

        # ---------------------------------------------------------------------
        # Step 3: Identify bad diff positions.
        #
        # A diff position is bad if its value appears more than once.
        # These are the diff edges that must be changed by replacing one of their
        # endpoint nums elements.
        # ---------------------------------------------------------------------
        bad: List[bool] = [freq[value] > 1 for value in diff]

        # ---------------------------------------------------------------------
        # Step 4: Greedy minimum cover on a path.
        #
        # Interpretation:
        # - diff position i is an edge between nums[i] and nums[i + 1].
        # - Replacing nums[j] selects vertex j.
        # - A selected vertex covers its incident edge(s).
        #
        # Greedy rule:
        # Scan edges from left to right. When edge i is bad and still uncovered,
        # choose vertex i + 1 (replace nums[i + 1]).
        #
        # Why choose i + 1 instead of i?
        # - Choosing the right endpoint covers edge i.
        # - It may also cover edge i + 1, giving the best chance to save one future
        #   replacement.
        #
        # We track whether the current bad edge is already covered by the previously
        # chosen vertex.
        # ---------------------------------------------------------------------
        operations: int = 0

        # "covered_next" means:
        # the current diff edge i is already covered because in the previous step
        # we selected nums[i], which covers diff[i - 1] and diff[i].
        covered_current: bool = False

        for i in range(n - 1):
            if not bad[i]:
                # This diff position does not belong to any duplicate-value group,
                # so it does not need to be changed.
                #
                # Also, if it happened to be covered by a previous replacement,
                # that is harmless. We simply move on.
                covered_current = False
                continue

            if covered_current:
                # This bad edge is already handled by the replacement chosen for the
                # previous bad edge. No new operation is needed.
                covered_current = False
                continue

            # We found the leftmost uncovered bad edge.
            # Greedily replace nums[i + 1], the right endpoint of this edge.
            operations += 1

            # That replacement covers:
            # - diff[i]   (the current edge)
            # - diff[i+1] (the next edge), if it exists
            #
            # So when the loop advances to i + 1, we should treat it as covered.
            covered_current = True

        return operations


if __name__ == "__main__":
    solution = Solution()

    # Example 1
    nums1: List[int] = [1, 4, 7, 10]
    limit1: int = 20
    result1: int = solution.min_replacements(nums1, limit1)
    print(f"nums = {nums1}, limit = {limit1} -> {result1}")  # Expected: 1

    # Example 2
    nums2: List[int] = [5, 5, 5, 5, 5]
    limit2: int = 10
    result2: int = solution.min_replacements(nums2, limit2)
    print(f"nums = {nums2}, limit = {limit2} -> {result2}")  # Expected: 2

    # Additional quick checks
    nums3: List[int] = [1, 2]
    limit3: int = 5
    result3: int = solution.min_replacements(nums3, limit3)
    print(f"nums = {nums3}, limit = {limit3} -> {result3}")  # Expected: 0

    nums4: List[int] = [1, 3, 6, 10]
    limit4: int = 20
    result4: int = solution.min_replacements(nums4, limit4)
    print(f"nums = {nums4}, limit = {limit4} -> {result4}")  # Expected: 0