"""
Title: First Store Open at or After Query Time

Problem Description:
You are given a sorted integer array `openTimes` where `openTimes[i]` represents
the minute of the day when the `i`-th store opens. The array is sorted in
non-decreasing order, and multiple stores may open at the same time. You are
also given an integer `queryTime`, representing the time a customer arrives.

Your task is to return the index of the first store whose opening time is
greater than or equal to `queryTime`. If no such store exists, return `-1`.

This problem models a common search task in scheduling and availability systems:
given a sorted list of event times, quickly find the earliest event that
satisfies a threshold condition. A linear scan works, but the expected interview
solution uses binary search to achieve logarithmic time complexity.

Implement a function that finds the leftmost valid index efficiently.

Constraints:
- 1 <= openTimes.length <= 10^5
- 0 <= openTimes[i] <= 10^9
- openTimes is sorted in non-decreasing order
- 0 <= queryTime <= 10^9

Example 1:
Input: openTimes = [120, 180, 240, 300], queryTime = 200
Output: 2
Explanation: The first opening time that is at least 200 is 240, which is at index 2.

Example 2:
Input: openTimes = [60, 60, 90, 150], queryTime = 60
Output: 0
Explanation: Multiple stores open at 60, and the first such index is 0.

If `queryTime` is larger than every value in `openTimes`, the answer should be `-1`.
"""

from typing import List


class Solution:
    def first_store_open_at_or_after(self, openTimes: List[int], queryTime: int) -> int:
        """
        Find the index of the first store whose opening time is greater than or equal to queryTime.

        Args:
            openTimes: A sorted list of store opening times in non-decreasing order.
            queryTime: The customer's arrival time.

        Returns:
            The leftmost index i such that openTimes[i] >= queryTime.
            Returns -1 if no such index exists.

        Time complexity:
            O(log n), where n is the length of openTimes, because binary search
            cuts the search space roughly in half each step.

        Space complexity:
            O(1), because only a constant amount of extra memory is used.
        """
        # We will use binary search because the input array is already sorted.
        # That sorted property is the key reason binary search works:
        # - If openTimes[mid] is large enough, then mid could be the answer,
        #   but there might be an earlier valid index on the left side.
        # - If openTimes[mid] is too small, then every index on the left side
        #   including mid is also too small or not better, so we must search right.
        #
        # Our goal is not just to find ANY valid index.
        # We specifically want the FIRST index where:
        #     openTimes[index] >= queryTime
        #
        # This is commonly called a "lower bound" search.

        left: int = 0
        right: int = len(openTimes) - 1

        # This variable will store the best answer found so far.
        # We start with -1 to mean "no valid store found yet".
        answer: int = -1

        # Continue searching while the current search interval is valid.
        while left <= right:
            # Compute the middle index safely.
            # In Python, overflow is not an issue, but this formula is still
            # the standard binary search pattern and is good practice.
            mid: int = left + (right - left) // 2

            # Check whether the store at index mid opens at or after queryTime.
            if openTimes[mid] >= queryTime:
                # This index is valid, so it is a candidate answer.
                answer = mid

                # However, we want the FIRST such index, not just any valid one.
                # So even though mid works, there may be another valid index
                # further to the left.
                #
                # Therefore, we continue searching in the left half.
                right = mid - 1
            else:
                # openTimes[mid] < queryTime
                #
                # This means the store at mid opens too early.
                # Since the array is sorted, every index to the left of mid
                # also has a value <= openTimes[mid], so none of them can satisfy
                # openTimes[i] >= queryTime.
                #
                # Therefore, we discard the left half and search only to the right.
                left = mid + 1

        # After the loop ends, answer is either:
        # - the leftmost valid index found, or
        # - -1 if no opening time was >= queryTime
        return answer

    def search(self, openTimes: List[int], queryTime: int) -> int:
        """
        Wrapper method that calls the main binary search implementation.

        Args:
            openTimes: A sorted list of store opening times in non-decreasing order.
            queryTime: The customer's arrival time.

        Returns:
            The index of the first store opening at or after queryTime, or -1 if none exists.

        Time complexity:
            O(log n), because it delegates to binary search.

        Space complexity:
            O(1), because it uses constant extra space.
        """
        return self.first_store_open_at_or_after(openTimes, queryTime)


if __name__ == "__main__":
    solution = Solution()

    # Example 1:
    # openTimes = [120, 180, 240, 300], queryTime = 200
    # The first value >= 200 is 240 at index 2.
    open_times_1: List[int] = [120, 180, 240, 300]
    query_time_1: int = 200
    result_1: int = solution.search(open_times_1, query_time_1)
    print("Example 1 Result:", result_1)  # Expected: 2

    # Example 2:
    # openTimes = [60, 60, 90, 150], queryTime = 60
    # The first value >= 60 is 60 at index 0.
    open_times_2: List[int] = [60, 60, 90, 150]
    query_time_2: int = 60
    result_2: int = solution.search(open_times_2, query_time_2)
    print("Example 2 Result:", result_2)  # Expected: 0

    # Additional example:
    # queryTime is larger than every opening time, so answer should be -1.
    open_times_3: List[int] = [30, 90, 120]
    query_time_3: int = 200
    result_3: int = solution.search(open_times_3, query_time_3)
    print("Example 3 Result:", result_3)  # Expected: -1