"""
Title: Longest Store Queue Under Customer Limit

Difficulty: Easy
Topic: Sliding Window

Problem Description:
A supermarket records the number of customers joining a checkout line each minute.
You are given an integer array customers where customers[i] is the number of new
customers who joined during the i-th minute, and an integer limit representing the
maximum total number of customers the manager is willing to handle in one continuous
observation period.

Your task is to find the length of the longest contiguous block of minutes such that
the sum of customers in that block is less than or equal to limit.

In other words, among all subarrays of customers whose total sum does not exceed
limit, return the maximum possible subarray length.

This problem models a common real-world monitoring task where a team wants to identify
the longest time span during which demand stayed within a manageable threshold. Since
all customer counts are non-negative, an efficient sliding window solution can expand
and shrink a window while maintaining the current sum.

Constraints:
- 1 <= customers.length <= 100000
- 0 <= customers[i] <= 10000
- 0 <= limit <= 1000000000

Example 1:
Input: customers = [2, 1, 3, 2, 1], limit = 5
Output: 2
Explanation: Valid windows include [2,1], [3,2], and [2,1]. Any window of length 3
has sum greater than 5, so the answer is 2.

Example 2:
Input: customers = [1, 0, 1, 1, 0, 1], limit = 3
Output: 5
Explanation: The window [1,0,1,1,0] has sum 3 and length 5, which is the longest
valid contiguous block.

Return only the maximum length. If no minute can be included because every single
value is greater than limit, return 0.
"""

from typing import List


class Solution:
    def longest_store_queue(self, customers: List[int], limit: int) -> int:
        """
        Find the maximum length of a contiguous subarray whose sum is at most limit.

        Args:
            customers: A list of non-negative integers where each value represents
                the number of customers joining during that minute.
            limit: The maximum allowed total number of customers in a valid window.

        Returns:
            The length of the longest contiguous subarray with sum less than or
            equal to limit.

        Time Complexity:
            O(n), where n is the length of customers, because each index is visited
            at most twice: once by the right pointer and once by the left pointer.

        Space Complexity:
            O(1), because only a few extra variables are used.
        """
        # The sliding window technique works especially well here because all values
        # in the array are non-negative.
        #
        # Why does non-negative matter?
        # - When we expand the window to the right, the sum can only stay the same
        #   or increase.
        # - If the sum becomes too large, moving the left side to the right will
        #   reduce or keep the sum the same.
        #
        # This predictable behavior allows us to maintain a valid window efficiently
        # without checking every possible subarray.

        # Left boundary of the current window.
        left: int = 0

        # Running sum of the current window customers[left:right+1].
        current_sum: int = 0

        # Best answer found so far.
        max_length: int = 0

        # Move the right boundary one step at a time to expand the window.
        for right in range(len(customers)):
            # Include the new rightmost element in the current window sum.
            current_sum += customers[right]

            # If the window sum is too large, it is invalid.
            # We must shrink the window from the left until the sum becomes valid.
            #
            # We use a while loop instead of an if statement because removing just
            # one element may not be enough. The sum might still exceed the limit,
            # so we continue shrinking until current_sum <= limit.
            while current_sum > limit and left <= right:
                # Remove the element at the left boundary from the running sum
                # because that minute is no longer part of the window.
                current_sum -= customers[left]

                # Move the left boundary rightward to complete the shrink step.
                left += 1

            # At this point, the window from left to right is guaranteed to be valid
            # because its sum is <= limit.
            #
            # So we can safely compute its length and compare it with the best answer.
            window_length: int = right - left + 1

            # Update the maximum length if this valid window is longer.
            if window_length > max_length:
                max_length = window_length

        # If every single element was greater than limit, then every window would
        # eventually shrink to length 0, and max_length would remain 0.
        return max_length


if __name__ == "__main__":
    solution = Solution()

    # Example 1
    customers1: List[int] = [2, 1, 3, 2, 1]
    limit1: int = 5
    result1: int = solution.longest_store_queue(customers1, limit1)
    print(result1)  # Expected: 2

    # Example 2
    customers2: List[int] = [1, 0, 1, 1, 0, 1]
    limit2: int = 3
    result2: int = solution.longest_store_queue(customers2, limit2)
    print(result2)  # Expected: 5

    # Additional sample: no single minute can be included
    customers3: List[int] = [6, 7, 8]
    limit3: int = 5
    result3: int = solution.longest_store_queue(customers3, limit3)
    print(result3)  # Expected: 0