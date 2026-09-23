"""
Title: Locate First Warehouse Shelf With Required Capacity

Problem Description:
A warehouse stores bins on shelves arranged from left to right. The shelves are indexed
from 0 to n - 1, and the capacity of each shelf is given in a non-decreasing integer
array `capacities`, where `capacities[i]` is the maximum weight that shelf `i` can
safely hold. Because the array is already sorted, lighter shelves appear before heavier
ones.

Given `capacities` and an integer `requiredWeight`, return the index of the first shelf
whose capacity is greater than or equal to `requiredWeight`. If no shelf can hold that
weight, return `-1`.

The intended solution uses binary search to efficiently find the lower bound.

Example 1:
Input: capacities = [5, 8, 8, 12, 15], requiredWeight = 8
Output: 1

Example 2:
Input: capacities = [3, 4, 6, 9], requiredWeight = 10
Output: -1
"""

from typing import List


class Solution:
    def first_shelf_with_required_capacity(
        self, capacities: List[int], requiredWeight: int
    ) -> int:
        """
        Find the first index in a sorted array whose value is greater than or equal
        to the required weight.

        Args:
            capacities: A non-decreasing list of shelf capacities.
            requiredWeight: The minimum weight a shelf must support.

        Returns:
            The index of the first shelf with capacity >= requiredWeight,
            or -1 if no such shelf exists.

        Time complexity:
            O(log n), because binary search cuts the search range roughly in half
            on each step.

        Space complexity:
            O(1), because only a constant amount of extra memory is used.
        """
        # We will perform a classic "lower bound" binary search.
        #
        # Goal:
        #   Find the LEFTMOST index i such that capacities[i] >= requiredWeight.
        #
        # Why binary search works:
        #   The array is sorted in non-decreasing order.
        #   That means:
        #   - If capacities[mid] is large enough, then mid might be the answer,
        #     but there could still be an earlier valid index on the left.
        #   - If capacities[mid] is too small, then every index to the left of mid
        #     is also too small, so we must search to the right.
        #
        # We keep two pointers:
        #   left  -> start of current search range
        #   right -> end of current search range
        left: int = 0
        right: int = len(capacities) - 1

        # This variable stores the best valid answer found so far.
        # We initialize it to -1 to mean "not found yet".
        answer: int = -1

        # Continue searching while the current range is valid.
        # When left passes right, the search is complete.
        while left <= right:
            # Compute the middle index safely.
            # In Python, (left + right) // 2 is fine, but this form is also a
            # standard interview habit and avoids overflow in some languages.
            mid: int = left + (right - left) // 2

            # Check whether the shelf at mid can hold the required weight.
            if capacities[mid] >= requiredWeight:
                # This shelf is valid.
                #
                # Important:
                #   Since we need the FIRST valid shelf, we do not stop here.
                #   Instead, we record mid as a candidate answer and continue
                #   searching on the LEFT side to see if an earlier valid shelf exists.
                answer = mid
                right = mid - 1
            else:
                # This shelf is too weak.
                #
                # Because the array is sorted, every shelf at index <= mid is also
                # too weak, so none of them can be the answer.
                # Therefore, we discard the left half including mid and search right.
                left = mid + 1

        # If we found at least one valid shelf, answer holds the leftmost one.
        # Otherwise, it remains -1.
        return answer


if __name__ == "__main__":
    solution = Solution()

    # Example 1:
    # capacities = [5, 8, 8, 12, 15], requiredWeight = 8
    # Expected output: 1
    capacities_1: List[int] = [5, 8, 8, 12, 15]
    required_weight_1: int = 8
    result_1: int = solution.first_shelf_with_required_capacity(
        capacities_1, required_weight_1
    )
    print(result_1)

    # Example 2:
    # capacities = [3, 4, 6, 9], requiredWeight = 10
    # Expected output: -1
    capacities_2: List[int] = [3, 4, 6, 9]
    required_weight_2: int = 10
    result_2: int = solution.first_shelf_with_required_capacity(
        capacities_2, required_weight_2
    )
    print(result_2)