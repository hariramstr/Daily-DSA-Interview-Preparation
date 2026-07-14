"""
Title: Maximum Consecutive Sensor IDs After One Repair

Problem Description:
A monitoring system stores a sorted array of distinct sensor IDs that should ideally form one
uninterrupted consecutive sequence. However, due to a database error, exactly one recorded ID
may be incorrect and can be changed to any integer value you choose. Your task is to determine
the maximum possible length of a consecutive run of distinct integers that can appear in the
array after repairing at most one element.

A consecutive run means a set of values like x, x+1, x+2, ..., y, with no gaps. The repaired
array does not need to remain sorted in memory, but the values should still be considered as a
set of distinct IDs after the change. If the array already contains the best possible run, you
may choose not to modify anything.

Return the maximum length of any consecutive run obtainable after at most one repair.

Constraints:
- 1 <= nums.length <= 100000
- -1000000000 <= nums[i] <= 1000000000
- nums is sorted in strictly increasing order

Example 1:
Input: nums = [10, 11, 13, 14]
Output: 5
Explanation: Change 14 to 12. The array values can become [10, 11, 12, 13, 14], which forms a
consecutive run of length 5.

Example 2:
Input: nums = [3, 4, 7, 8, 9]
Output: 4
Explanation: One optimal repair is to change 3 to 6, giving values [4, 6, 7, 8, 9]. The longest
consecutive run is then [6, 7, 8, 9], with length 4. It is impossible to obtain a run of length 5
with only one change.
"""

from typing import List


class Solution:
    def max_consecutive_after_one_repair(self, nums: List[int]) -> int:
        """
        Compute the maximum possible length of a consecutive run after changing
        at most one array element to any integer value.

        The input array is sorted and contains distinct integers.

        Args:
            nums: Sorted list of distinct integers.

        Returns:
            The maximum length of a consecutive run obtainable after at most one repair.

        Time complexity:
            O(n), where n is the length of nums.

        Space complexity:
            O(n), for helper arrays storing lengths of consecutive blocks.
        """
        n: int = len(nums)

        # If there is only one element, the longest consecutive run is obviously 1.
        # We may "change" it, but there is still only one value in the array.
        if n == 1:
            return 1

        # ---------------------------------------------------------------------
        # STEP 1: Build helper arrays describing consecutive blocks.
        #
        # left[i]  = length of the consecutive block ending at index i
        # right[i] = length of the consecutive block starting at index i
        #
        # Example:
        # nums = [10, 11, 13, 14]
        # left  = [1, 2, 1, 2]
        # right = [2, 1, 2, 1]
        #
        # Why this helps:
        # If we consider changing nums[i], then:
        # - the block immediately to the left of i can be measured using left[i-1]
        # - the block immediately to the right of i can be measured using right[i+1]
        #
        # This lets us quickly evaluate how large a run can become if nums[i] is
        # changed to bridge or extend nearby blocks.
        # ---------------------------------------------------------------------
        left: List[int] = [1] * n
        for i in range(1, n):
            # If current value continues the previous value by exactly +1,
            # then the consecutive block ending here extends the previous block.
            if nums[i] == nums[i - 1] + 1:
                left[i] = left[i - 1] + 1

        right: List[int] = [1] * n
        for i in range(n - 2, -1, -1):
            # If next value is exactly current + 1, then the consecutive block
            # starting here extends the block starting at i + 1.
            if nums[i + 1] == nums[i] + 1:
                right[i] = right[i + 1] + 1

        # ---------------------------------------------------------------------
        # STEP 2: Baseline answer without any modification.
        #
        # If the array already contains a long consecutive block, we are allowed
        # to perform "at most one" repair, so doing nothing is valid.
        # ---------------------------------------------------------------------
        answer: int = max(left)

        # ---------------------------------------------------------------------
        # STEP 3: Try changing each element nums[i].
        #
        # Since exactly one element may be wrong, we imagine removing nums[i]
        # from the set and replacing it with some carefully chosen value.
        #
        # Around index i there can be:
        # - a consecutive block on the left
        # - a consecutive block on the right
        #
        # Let:
        #   left_len  = size of block ending at i-1
        #   right_len = size of block starting at i+1
        #
        # We can always place the repaired value:
        # - just after the left block, making left_len + 1
        # - or just before the right block, making right_len + 1
        #
        # Sometimes we can do even better:
        # If the left block ends at value A and the right block starts at value B,
        # then one changed value can fully bridge them only when B - A == 2.
        # In that case, the missing value is exactly A + 1, and the total run is:
        #   left_len + 1 + right_len
        #
        # Important subtlety:
        # We are changing nums[i], so the left and right blocks must be computed
        # from neighbors excluding nums[i] itself. That is exactly why we use
        # left[i-1] and right[i+1].
        # ---------------------------------------------------------------------
        for i in range(n):
            left_len: int = left[i - 1] if i > 0 else 0
            right_len: int = right[i + 1] if i + 1 < n else 0

            # -------------------------------------------------------------
            # Option A: Extend only the left-side block by placing the new
            # value immediately after it.
            #
            # This is always possible because we can choose any integer.
            # We must still keep all values distinct, but placing the new value
            # adjacent to one side is valid as long as it does not collide with
            # an existing value inside that side's block. Since it is exactly
            # one step outside the block, it does not collide with that block.
            # If it collides with the opposite side, then that simply means the
            # two sides are close enough that a bridge case may apply; the max
            # logic below will still handle the best valid outcome.
            # -------------------------------------------------------------
            answer = max(answer, left_len + 1)

            # -------------------------------------------------------------
            # Option B: Extend only the right-side block by placing the new
            # value immediately before it.
            # -------------------------------------------------------------
            answer = max(answer, right_len + 1)

            # -------------------------------------------------------------
            # Option C: Fully bridge left and right blocks.
            #
            # Let:
            #   left_end = nums[i - 1]
            #   right_start = nums[i + 1]
            #
            # After removing nums[i], the only way one new value can connect
            # both sides into one single consecutive run is if there is exactly
            # one missing integer between them:
            #
            #   right_start - left_end == 2
            #
            # Then we can set nums[i] = left_end + 1 and obtain:
            #   [left block] + [new value] + [right block]
            # -------------------------------------------------------------
            if i > 0 and i + 1 < n and nums[i + 1] - nums[i - 1] == 2:
                answer = max(answer, left_len + 1 + right_len)

        # The answer can never exceed n because the array contains exactly n
        # distinct values after the repair.
        return min(answer, n)


if __name__ == "__main__":
    solution = Solution()

    sample_inputs: List[List[int]] = [
        [10, 11, 13, 14],
        [3, 4, 7, 8, 9],
        [5],
        [1, 2, 3, 4],
        [1, 3, 5],
        [1, 2, 4, 5, 6],
    ]

    for arr in sample_inputs:
        result = solution.max_consecutive_after_one_repair(arr)
        print(f"nums = {arr} -> {result}")