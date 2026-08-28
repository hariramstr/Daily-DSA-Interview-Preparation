"""
Title: Find Insertion Slot for a Sorted Event Timeline

Problem Description:
You are given a sorted array `times` representing event start times in minutes from the
beginning of the day. The array is sorted in non-decreasing order, and duplicate values
may exist because multiple events can start at the same minute. You are also given an
integer `target`, representing the start time of a new event.

Return the index where `target` should be inserted so that the array remains sorted after
insertion. If `target` already exists, return the leftmost index where it appears. In
other words, you must find the first position `i` such that `times[i] >= target`. If no
such position exists, return `times.length`.

Your solution should run in O(log n) time, which makes binary search the intended
approach. A linear scan will work for small inputs but will not satisfy the expected
performance for large arrays.

Constraints:
- 0 <= times.length <= 100000
- 0 <= times[i] <= 1440
- times is sorted in non-decreasing order
- 0 <= target <= 1440

Example 1:
Input: times = [15, 30, 30, 45, 90], target = 30
Output: 1
Explanation: The value 30 already exists, and the leftmost valid insertion position is
index 1.

Example 2:
Input: times = [10, 20, 40, 80], target = 35
Output: 2
Explanation: Inserting 35 at index 2 gives [10, 20, 35, 40, 80], which is still sorted.
"""

from typing import List


class Solution:
    def search_insert(self, times: List[int], target: int) -> int:
        """
        Find the leftmost index where target can be inserted in a sorted list.

        This method uses binary search to locate the first position where the value
        is greater than or equal to target. That position is exactly where target
        should be inserted to keep the list sorted.

        Args:
            times: A sorted list of event start times in non-decreasing order.
            target: The event start time to insert.

        Returns:
            The leftmost valid insertion index for target.

        Time complexity:
            O(log n), because the search range is cut roughly in half each step.

        Space complexity:
            O(1), because only a constant amount of extra memory is used.
        """
        # We use a "search space" defined by two pointers:
        # - left: the beginning of the current candidate range
        # - right: the end of the current candidate range
        #
        # Important idea:
        # We are not just looking for whether target exists.
        # We are specifically looking for the FIRST index where:
        #     times[index] >= target
        #
        # This is commonly called a "lower bound" binary search.
        left: int = 0
        right: int = len(times) - 1

        # We continue searching while there is still a valid range to inspect.
        while left <= right:
            # Compute the middle index of the current search range.
            #
            # Using this formula avoids overflow in some languages:
            #     mid = left + (right - left) // 2
            #
            # In Python, integer overflow is not a practical issue here, but this
            # is still the standard and safest binary search pattern.
            mid: int = left + (right - left) // 2

            # If the middle value is less than target, then mid cannot be the answer,
            # and neither can anything to its left.
            #
            # Why?
            # Because we need the FIRST index where value >= target.
            # If times[mid] < target, then every index up to mid is too small.
            #
            # So we move left to mid + 1 and continue searching only on the right side.
            if times[mid] < target:
                left = mid + 1
            else:
                # Otherwise, times[mid] >= target.
                #
                # This means mid is a VALID candidate answer.
                # But it may not be the LEFTMOST such index.
                #
                # So instead of returning immediately, we continue searching on the
                # left half to see if there is an earlier valid position.
                right = mid - 1

        # When the loop ends, left is exactly the first index where target can be inserted.
        #
        # Why is left correct here?
        # - All indices before left are confirmed to have values < target.
        # - All indices from left onward are potential positions where value >= target,
        #   or left may be len(times) if target is larger than every element.
        #
        # This works for all cases:
        # - target exists -> returns its leftmost occurrence
        # - target does not exist -> returns correct insertion point
        # - target is larger than all elements -> returns len(times)
        # - empty list -> returns 0
        return left


if __name__ == "__main__":
    # Create an instance of the solution class.
    solution = Solution()

    # Example 1 from the problem statement:
    # times = [15, 30, 30, 45, 90], target = 30
    # Expected output: 1
    #
    # Quick trace:
    # - The first index where value >= 30 is index 1.
    example_times_1: List[int] = [15, 30, 30, 45, 90]
    example_target_1: int = 30
    result_1: int = solution.search_insert(example_times_1, example_target_1)
    print("Example 1:")
    print(f"times = {example_times_1}, target = {example_target_1}")
    print(f"Output: {result_1}")
    print("Expected: 1")
    print()

    # Example 2 from the problem statement:
    # times = [10, 20, 40, 80], target = 35
    # Expected output: 2
    #
    # Quick trace:
    # - 35 should go between 20 and 40.
    # - The first index where value >= 35 is index 2.
    example_times_2: List[int] = [10, 20, 40, 80]
    example_target_2: int = 35
    result_2: int = solution.search_insert(example_times_2, example_target_2)
    print("Example 2:")
    print(f"times = {example_times_2}, target = {example_target_2}")
    print(f"Output: {result_2}")
    print("Expected: 2")
    print()

    # Additional beginner-friendly test cases to show edge behavior.

    # Case 3: Empty list
    # Inserting into an empty list always gives index 0.
    example_times_3: List[int] = []
    example_target_3: int = 50
    result_3: int = solution.search_insert(example_times_3, example_target_3)
    print("Example 3:")
    print(f"times = {example_times_3}, target = {example_target_3}")
    print(f"Output: {result_3}")
    print("Expected: 0")
    print()

    # Case 4: Insert at the end
    # Since 100 is larger than every element, the answer is len(times) = 4.
    example_times_4: List[int] = [10, 20, 40, 80]
    example_target_4: int = 100
    result_4: int = solution.search_insert(example_times_4, example_target_4)
    print("Example 4:")
    print(f"times = {example_times_4}, target = {example_target_4}")
    print(f"Output: {result_4}")
    print("Expected: 4")
    print()

    # Case 5: Insert at the beginning
    # Since 5 is smaller than every element, the answer is index 0.
    example_times_5: List[int] = [10, 20, 40, 80]
    example_target_5: int = 5
    result_5: int = solution.search_insert(example_times_5, example_target_5)
    print("Example 5:")
    print(f"times = {example_times_5}, target = {example_target_5}")
    print(f"Output: {result_5}")
    print("Expected: 0")