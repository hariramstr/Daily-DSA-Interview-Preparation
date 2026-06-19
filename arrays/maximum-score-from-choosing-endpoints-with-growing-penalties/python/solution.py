"""
Title: Maximum Score from Choosing Endpoints with Growing Penalties

Problem Description:
You are given an integer array nums of length n. You must remove every element from
the array, one at a time. At each step, you may remove either the leftmost remaining
element or the rightmost remaining element. If this is the t-th removal (1-indexed),
removing a value x adds x * t to your score.

However, there is an additional penalty for switching sides. If your previous removal
was from the left and your current removal is from the right, or vice versa, then you
pay a fixed penalty p for that step. No penalty is paid on the first removal because
there is no previous side.

Return the maximum total score you can obtain after removing all elements.

The challenge is to choose both the order of removals and when to switch sides.
A greedy approach is not sufficient, because taking a large value early may reduce
the multiplier available for other values, and unnecessary side switches may erase
the gain.

Constraints:
- 1 <= n <= 2000
- -10^9 <= nums[i] <= 10^9
- 0 <= p <= 10^9
- The answer fits in a signed 64-bit integer.
"""

from typing import List


class Solution:
    def maximum_score(self, nums: List[int], p: int) -> int:
        """
        Compute the maximum total score obtainable by repeatedly removing either
        the leftmost or rightmost remaining element, with time-based multipliers
        and a fixed penalty for switching sides.

        Args:
            nums: The input array of integers.
            p: The fixed penalty paid whenever the chosen side differs from the
               side used in the previous removal.

        Returns:
            The maximum achievable total score.

        Time complexity:
            O(n^2), where n is the length of nums.

        Space complexity:
            O(n^2) for the dynamic programming table.
        """
        n: int = len(nums)

        # We use a dynamic programming approach over subarrays.
        #
        # Key observation:
        # After some number of removals, the remaining elements always form a
        # contiguous subarray nums[l..r].
        #
        # If the remaining subarray is nums[l..r], then:
        # - exactly n - (r - l + 1) elements have already been removed
        # - therefore the next removal happens at time:
        #       t = n - (r - l + 1) + 1 = n - (r - l)
        #
        # We define two DP states for every remaining subarray [l][r]:
        #
        # dp_left[l][r]:
        #   Maximum score obtainable from the state where nums[l..r] remains,
        #   and the PREVIOUS removal (the one just before reaching this state)
        #   was from the LEFT side.
        #
        # dp_right[l][r]:
        #   Maximum score obtainable from the state where nums[l..r] remains,
        #   and the PREVIOUS removal was from the RIGHT side.
        #
        # Why do we need to remember the previous side?
        # Because the next choice may incur a switching penalty p.
        #
        # Transition idea:
        # From state [l][r], we choose the next removal:
        #
        # 1) Remove left element nums[l]
        #    Gain = nums[l] * t
        #    Penalty = p if previous side was RIGHT, else 0
        #    New remaining subarray = [l+1][r]
        #    New previous side becomes LEFT
        #
        # 2) Remove right element nums[r]
        #    Gain = nums[r] * t
        #    Penalty = p if previous side was LEFT, else 0
        #    New remaining subarray = [l][r-1]
        #    New previous side becomes RIGHT
        #
        # Base case:
        # When l > r, no elements remain, so score is 0.
        #
        # Since Python lists are 0-indexed and we want O(1) access, we build
        # 2D tables of size n x n. We fill them by increasing subarray length.
        #
        # Important detail about the first move:
        # There is no previous side before the first removal, so no penalty
        # should be charged. To handle this cleanly, we do not directly start
        # from a DP state with a previous side. Instead, we manually try:
        # - first move from left
        # - first move from right
        # and then continue using the DP states, where the previous side is now
        # well-defined.

        dp_left: List[List[int]] = [[0] * n for _ in range(n)]
        dp_right: List[List[int]] = [[0] * n for _ in range(n)]

        # We fill subproblems from smaller remaining subarrays to larger ones.
        #
        # For a subarray of length 'length':
        #   remaining count = length
        #   next time multiplier t = n - length + 1
        #
        # We need values for [l+1][r] and [l][r-1], which correspond to
        # smaller subarrays, so iterating length from 1 to n works correctly.
        for length in range(1, n + 1):
            t: int = n - length + 1

            for l in range(0, n - length + 1):
                r: int = l + length - 1

                # Compute dp_left[l][r]:
                # Previous removal was from LEFT.
                #
                # Option A: remove left now
                #   - no switching penalty, because previous side was also LEFT
                #   - gain nums[l] * t
                #   - continue with remaining [l+1][r], previous side = LEFT
                if l == r:
                    # If only one element remains, removing it ends the process.
                    # There is no future state to add.
                    take_left_after_left: int = nums[l] * t
                    take_right_after_left: int = nums[r] * t - p
                else:
                    take_left_after_left = nums[l] * t + dp_left[l + 1][r]
                    take_right_after_left = nums[r] * t - p + dp_right[l][r - 1]

                dp_left[l][r] = max(take_left_after_left, take_right_after_left)

                # Compute dp_right[l][r]:
                # Previous removal was from RIGHT.
                #
                # Option A: remove left now
                #   - switching penalty applies, because previous side was RIGHT
                #   - gain nums[l] * t - p
                #   - continue with remaining [l+1][r], previous side = LEFT
                #
                # Option B: remove right now
                #   - no switching penalty
                #   - gain nums[r] * t
                #   - continue with remaining [l][r-1], previous side = RIGHT
                if l == r:
                    take_left_after_right: int = nums[l] * t - p
                    take_right_after_right: int = nums[r] * t
                else:
                    take_left_after_right = nums[l] * t - p + dp_left[l + 1][r]
                    take_right_after_right = nums[r] * t + dp_right[l][r - 1]

                dp_right[l][r] = max(take_left_after_right, take_right_after_right)

        # Handle the first move separately so that no penalty is charged.
        #
        # First move at time t = 1:
        #
        # 1) Remove left first:
        #    score = nums[0] * 1
        #    remaining = [1..n-1]
        #    previous side becomes LEFT
        #
        # 2) Remove right first:
        #    score = nums[n-1] * 1
        #    remaining = [0..n-2]
        #    previous side becomes RIGHT
        #
        # Then we use the DP tables to finish optimally.
        if n == 1:
            return nums[0]

        first_left: int = nums[0] + dp_left[1][n - 1]
        first_right: int = nums[n - 1] + dp_right[0][n - 2]

        return max(first_left, first_right)


if __name__ == "__main__":
    solution = Solution()

    # Example 1
    nums1: List[int] = [4, 2, 9]
    p1: int = 3
    result1: int = solution.maximum_score(nums1, p1)
    print(f"nums = {nums1}, p = {p1} -> maximum score = {result1}")
    # Expected: 35

    # Example 2
    nums2: List[int] = [8, -5, 7, 3]
    p2: int = 4
    result2: int = solution.maximum_score(nums2, p2)
    print(f"nums = {nums2}, p = {p2} -> maximum score = {result2}")

    # Additional small sanity checks
    nums3: List[int] = [5]
    p3: int = 10
    result3: int = solution.maximum_score(nums3, p3)
    print(f"nums = {nums3}, p = {p3} -> maximum score = {result3}")
    # Expected: 5

    nums4: List[int] = [1, 2]
    p4: int = 100
    result4: int = solution.maximum_score(nums4, p4)
    print(f"nums = {nums4}, p = {p4} -> maximum score = {result4}")