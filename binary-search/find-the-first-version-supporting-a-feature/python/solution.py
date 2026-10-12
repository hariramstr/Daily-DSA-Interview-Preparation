"""
Title: Find the First Version Supporting a Feature

Problem Description:
A software platform releases product versions in increasing order, and each version
either supports a new feature or does not. You are given a sorted binary array
`support`, where `support[i]` is either `0` or `1`. A value of `0` means version `i`
does not support the feature yet, and a value of `1` means version `i` does support
it. The array is guaranteed to be sorted in non-decreasing order, so once the feature
appears in some version, every later version also supports it.

Your task is to return the index of the first version that supports the feature.
If no version supports it, return `-1`.

This is a classic search task where a linear scan works, but the intended solution
should use binary search to achieve logarithmic time. Be careful with edge cases such
as when the first element is already `1`, when all elements are `0`, or when the array
contains only one version.

Constraints:
- 1 <= support.length <= 100000
- support[i] is either 0 or 1
- support is sorted in non-decreasing order

Example 1:
Input: support = [0,0,0,1,1,1]
Output: 3
Explanation: The first version that supports the feature is at index 3.

Example 2:
Input: support = [0,0,0,0]
Output: -1
Explanation: No version supports the feature, so return -1.
"""

from typing import List


class Solution:
    def first_supported_version(self, support: List[int]) -> int:
        """
        Find the index of the first version that supports the feature.

        This method uses binary search on a sorted binary array. Because all 0s come
        before all 1s, we can efficiently search for the first occurrence of 1.

        Args:
            support: A sorted list of integers containing only 0 and 1.

        Returns:
            The index of the first 1 in the list, or -1 if no 1 exists.

        Time complexity:
            O(log n), because binary search halves the search space each step.

        Space complexity:
            O(1), because only a constant amount of extra memory is used.
        """
        # We will search within the inclusive range [left, right].
        # "left" starts at the beginning of the array.
        left: int = 0

        # "right" starts at the end of the array.
        right: int = len(support) - 1

        # This variable will store the best answer found so far.
        # We initialize it to -1, which means "no supporting version found yet".
        #
        # Why do we need this variable?
        # Because during binary search, when we find a 1, that index could be the
        # first supporting version, but there might still be another 1 earlier in
        # the array. So we save the current index as a possible answer and continue
        # searching on the left side.
        answer: int = -1

        # Continue searching while the current search range is valid.
        # The condition left <= right means there is still at least one element
        # left to inspect.
        while left <= right:
            # Compute the middle index of the current search range.
            #
            # We use this form instead of (left + right) // 2 as a standard safe
            # binary search habit. In Python, integer overflow is not a practical
            # issue, but this style is widely used and good to learn.
            mid: int = left + (right - left) // 2

            # Check the value at the middle index.
            if support[mid] == 1:
                # If support[mid] is 1, then this version supports the feature.
                #
                # Because the array is sorted, this means:
                # - mid is a valid candidate answer
                # - but there may be an earlier 1 to the LEFT
                #
                # So we record mid as the current best answer.
                answer = mid

                # Now move the right boundary to mid - 1 to continue searching
                # the left half for an even earlier 1.
                right = mid - 1
            else:
                # If support[mid] is 0, then this version does NOT support the feature.
                #
                # Because the array is sorted in non-decreasing order, every index
                # at or before mid must also be 0 or not the first 1 we want.
                #
                # Therefore, the first 1 (if it exists) must be to the RIGHT of mid.
                left = mid + 1

        # After the loop ends, "answer" is either:
        # - the index of the first 1 found
        # - or -1 if no 1 exists in the array
        return answer


if __name__ == "__main__":
    solution = Solution()

    # Example 1 from the problem statement:
    # support = [0, 0, 0, 1, 1, 1]
    # Expected output: 3
    sample_1: List[int] = [0, 0, 0, 1, 1, 1]
    result_1: int = solution.first_supported_version(sample_1)
    print(f"Input: {sample_1}")
    print(f"First supported version index: {result_1}")
    print()

    # Example 2 from the problem statement:
    # support = [0, 0, 0, 0]
    # Expected output: -1
    sample_2: List[int] = [0, 0, 0, 0]
    result_2: int = solution.first_supported_version(sample_2)
    print(f"Input: {sample_2}")
    print(f"First supported version index: {result_2}")
    print()

    # Additional beginner-friendly edge case tests:

    # Edge case: first element is already 1
    sample_3: List[int] = [1, 1, 1, 1]
    result_3: int = solution.first_supported_version(sample_3)
    print(f"Input: {sample_3}")
    print(f"First supported version index: {result_3}")
    print()

    # Edge case: single element that is 0
    sample_4: List[int] = [0]
    result_4: int = solution.first_supported_version(sample_4)
    print(f"Input: {sample_4}")
    print(f"First supported version index: {result_4}")
    print()

    # Edge case: single element that is 1
    sample_5: List[int] = [1]
    result_5: int = solution.first_supported_version(sample_5)
    print(f"Input: {sample_5}")
    print(f"First supported version index: {result_5}")