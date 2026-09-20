"""
Title: Locate the First Train Arrival Not Earlier Than Target

Problem Description:
You are given a sorted array `arrivals` where `arrivals[i]` represents the scheduled
arrival time of the `i`-th train in minutes after midnight. The array is sorted in
non-decreasing order, so multiple trains may share the same arrival time. You are also
given an integer `target`, representing the earliest time a passenger is willing to board.

Your task is to return the index of the first train whose arrival time is greater than
or equal to `target`. If no such train exists, return `-1`.

This problem models a common lookup operation in booking and scheduling systems:
finding the earliest available option that satisfies a minimum requirement. A linear
scan works, but the input is already sorted, so an efficient binary search solution
is expected.

Constraints:
- 1 <= arrivals.length <= 10^5
- 0 <= arrivals[i] <= 1439
- arrivals is sorted in non-decreasing order
- 0 <= target <= 1439

Example 1:
Input: arrivals = [120, 180, 180, 240, 315], target = 181
Output: 3
Explanation: The first arrival not earlier than 181 is 240, which is at index 3.

Example 2:
Input: arrivals = [60, 90, 150, 150, 210], target = 150
Output: 2
Explanation: There are trains at time 150, and the first such train appears at index 2.

If every train arrives before `target`, the answer should be `-1`.
"""

from typing import List


class Solution:
    def first_not_earlier(self, arrivals: List[int], target: int) -> int:
        """
        Find the index of the first value in a sorted list that is greater than
        or equal to the target.

        Args:
            arrivals: A sorted list of train arrival times in non-decreasing order.
            target: The earliest acceptable arrival time.

        Returns:
            The index of the first arrival time >= target, or -1 if no such
            arrival exists.

        Time complexity:
            O(log n), because binary search cuts the search range roughly in half
            on each step.

        Space complexity:
            O(1), because only a few variables are used regardless of input size.
        """
        # We will use binary search because the input list is already sorted.
        # This is much faster than checking every element one by one.
        #
        # Goal:
        #   Find the LEFTMOST index i such that arrivals[i] >= target.
        #
        # Why "leftmost" matters:
        #   If multiple trains have the same valid arrival time, we must return
        #   the first one, not just any one of them.
        #
        # Example:
        #   arrivals = [60, 90, 150, 150, 210], target = 150
        #   Both index 2 and 3 satisfy arrivals[i] >= 150,
        #   but the correct answer is 2.

        # `left` marks the beginning of the current search range.
        left: int = 0

        # `right` marks the end of the current search range.
        right: int = len(arrivals) - 1

        # `answer` stores the best valid index found so far.
        # We start with -1 to mean "not found yet".
        answer: int = -1

        # Continue searching while there is still a valid range to inspect.
        while left <= right:
            # Compute the middle index safely.
            # This avoids overflow in some languages, and is also a common pattern.
            mid: int = left + (right - left) // 2

            # Read the middle value once so the code is easier to understand.
            current_arrival: int = arrivals[mid]

            # Case 1:
            # If the middle arrival is large enough, then it is a VALID candidate.
            if current_arrival >= target:
                # Record this index as a possible answer.
                answer = mid

                # But we are not done yet.
                # There might be another valid train even earlier in the list.
                #
                # Since the array is sorted:
                # - everything to the right of `mid` is >= current_arrival or equal,
                #   so those indices are not earlier than `mid`
                # - if a better answer exists, it must be on the LEFT side
                #
                # Therefore, move `right` leftward to continue searching for the
                # first valid index.
                right = mid - 1
            else:
                # Case 2:
                # If the middle arrival is smaller than target, then this train
                # is too early and cannot be the answer.
                #
                # Because the array is sorted, every value at indices <= mid is
                # also too small or not better than this one for our condition.
                #
                # So we discard the left half including `mid` and search only
                # in the right half.
                left = mid + 1

        # After the loop ends:
        # - `answer` is the leftmost index with arrivals[index] >= target
        # - or it remains -1 if no such index exists
        return answer

    def search_insert_style(self, arrivals: List[int], target: int) -> int:
        """
        Alternative implementation using the classic lower-bound pattern.

        Args:
            arrivals: A sorted list of train arrival times in non-decreasing order.
            target: The earliest acceptable arrival time.

        Returns:
            The index of the first arrival time >= target, or -1 if no such
            arrival exists.

        Time complexity:
            O(log n)

        Space complexity:
            O(1)
        """
        # This method uses a slightly different binary search style.
        # Instead of storing an answer during the search, it narrows the range
        # until `left` becomes the insertion position for `target`.
        #
        # That insertion position is exactly the first index where a value is
        # greater than or equal to target.

        left: int = 0
        right: int = len(arrivals)

        # Notice that `right` is len(arrivals), not len(arrivals) - 1.
        # This means we are searching in the half-open interval [left, right).
        # This style is common for lower-bound searches.
        while left < right:
            mid: int = left + (right - left) // 2

            # If arrivals[mid] is too small, the answer must be after mid.
            if arrivals[mid] < target:
                left = mid + 1
            else:
                # arrivals[mid] is valid, so the first valid index could be mid
                # or somewhere before it.
                right = mid

        # At this point, `left` is the first index where arrivals[left] >= target,
        # if such an index exists.
        if left == len(arrivals):
            return -1

        return left


if __name__ == "__main__":
    solution = Solution()

    # Example 1 from the problem statement
    arrivals_1: List[int] = [120, 180, 180, 240, 315]
    target_1: int = 181
    result_1: int = solution.first_not_earlier(arrivals_1, target_1)
    print("Example 1:")
    print("Arrivals:", arrivals_1)
    print("Target:", target_1)
    print("Output:", result_1)
    print("Expected:", 3)
    print()

    # Example 2 from the problem statement
    arrivals_2: List[int] = [60, 90, 150, 150, 210]
    target_2: int = 150
    result_2: int = solution.first_not_earlier(arrivals_2, target_2)
    print("Example 2:")
    print("Arrivals:", arrivals_2)
    print("Target:", target_2)
    print("Output:", result_2)
    print("Expected:", 2)
    print()

    # Additional example: no train is late enough
    arrivals_3: List[int] = [30, 45, 50]
    target_3: int = 60
    result_3: int = solution.first_not_earlier(arrivals_3, target_3)
    print("Example 3:")
    print("Arrivals:", arrivals_3)
    print("Target:", target_3)
    print("Output:", result_3)
    print("Expected:", -1)
    print()

    # Additional example: first element already satisfies the condition
    arrivals_4: List[int] = [100, 120, 140]
    target_4: int = 90
    result_4: int = solution.first_not_earlier(arrivals_4, target_4)
    print("Example 4:")
    print("Arrivals:", arrivals_4)
    print("Target:", target_4)
    print("Output:", result_4)
    print("Expected:", 0)
    print()

    # Verify the alternative method produces the same results
    print("Verification using alternative lower-bound method:")
    print(solution.search_insert_style(arrivals_1, target_1))  # Expected 3
    print(solution.search_insert_style(arrivals_2, target_2))  # Expected 2
    print(solution.search_insert_style(arrivals_3, target_3))  # Expected -1
    print(solution.search_insert_style(arrivals_4, target_4))  # Expected 0