"""
Title: Longest Watering Streak Within Tank Capacity

Problem Description:
A gardener records how many liters of water are used on each consecutive day in an array
`usage`, where `usage[i]` is the water needed on day `i`. The watering system has a tank
that can supply at most `capacity` liters before it must be refilled. We want to find the
longest consecutive stretch of days that can be fully covered using a single tank fill.

Return the length of the longest contiguous subarray whose total water usage is less than
or equal to `capacity`.

This is a classic contiguous-range optimization problem: we are not allowed to skip days,
reorder them, or split the interval. If no day can be covered because every single day's
usage is greater than `capacity`, return `0`.

Constraints:
- 1 <= usage.length <= 100000
- 0 <= usage[i] <= 10000
- 0 <= capacity <= 10^9

Examples:
1) usage = [2, 1, 3, 2, 1], capacity = 5
   Output: 2

2) usage = [1, 1, 1, 2, 1], capacity = 4
   Correct Output: 3
"""

from typing import List


class Solution:
    def longest_watering_streak(self, usage: List[int], capacity: int) -> int:
        """
        Find the length of the longest contiguous subarray whose sum is at most capacity.

        This method uses the sliding window technique, which works efficiently here
        because all values in `usage` are non-negative. That non-negative property is
        important: when we expand the window, the sum can only stay the same or increase,
        and when we shrink the window, the sum can only stay the same or decrease.
        That makes it possible to adjust the window in linear time.

        Args:
            usage: A list where usage[i] is the water used on day i.
            capacity: The maximum total water the tank can provide in one fill.

        Returns:
            The maximum length of a contiguous subarray with sum <= capacity.

        Time complexity:
            O(n), where n is the length of usage.
            Each element is added to the window once and removed from the window once.

        Space complexity:
            O(1), because only a few variables are used regardless of input size.
        """
        # `left` marks the start index of the current sliding window.
        left: int = 0

        # `current_sum` stores the total water usage inside the current window
        # from index `left` to index `right`.
        current_sum: int = 0

        # `best_length` stores the maximum valid window length found so far.
        best_length: int = 0

        # We move `right` from left to right across the array, expanding the window
        # one day at a time.
        for right in range(len(usage)):
            # Add the new day's usage into the current window sum because the window
            # now includes `usage[right]`.
            current_sum += usage[right]

            # If the window sum is too large, it is no longer valid.
            # Because we need the sum to be <= capacity, we repeatedly move `left`
            # forward and subtract those values until the window becomes valid again.
            #
            # We use a `while` loop instead of `if` because removing just one element
            # may still leave the sum above capacity.
            while current_sum > capacity and left <= right:
                current_sum -= usage[left]
                left += 1

            # At this point, the window from `left` to `right` is guaranteed to be valid
            # (or empty in edge cases), meaning its sum is <= capacity.
            #
            # So we compute its length and compare it with the best answer seen so far.
            current_length: int = right - left + 1
            if current_length > best_length:
                best_length = current_length

        # If every single day had usage > capacity, then every valid window length would
        # end up being 0, and `best_length` correctly remains 0.
        return best_length


if __name__ == "__main__":
    solution = Solution()

    # Example 1
    usage1: List[int] = [2, 1, 3, 2, 1]
    capacity1: int = 5
    result1: int = solution.longest_watering_streak(usage1, capacity1)
    print(f"usage = {usage1}, capacity = {capacity1} -> {result1}")  # Expected: 2

    # Example 2
    # Important note:
    # The original explanation text contains contradictions, but the correct answer is 3.
    # Valid length-3 windows include [1,1,2] and [1,2,1], both summing to 4.
    usage2: List[int] = [1, 1, 1, 2, 1]
    capacity2: int = 4
    result2: int = solution.longest_watering_streak(usage2, capacity2)
    print(f"usage = {usage2}, capacity = {capacity2} -> {result2}")  # Expected: 3

    # Additional quick checks
    usage3: List[int] = [6, 7, 8]
    capacity3: int = 5
    result3: int = solution.longest_watering_streak(usage3, capacity3)
    print(f"usage = {usage3}, capacity = {capacity3} -> {result3}")  # Expected: 0

    usage4: List[int] = [0, 0, 0, 0]
    capacity4: int = 0
    result4: int = solution.longest_watering_streak(usage4, capacity4)
    print(f"usage = {usage4}, capacity = {capacity4} -> {result4}")  # Expected: 4

    usage5: List[int] = [1, 2, 3, 4]
    capacity5: int = 6
    result5: int = solution.longest_watering_streak(usage5, capacity5)
    print(f"usage = {usage5}, capacity = {capacity5} -> {result5}")  # Expected: 3