"""
Title: Longest Upload Burst Within Data Cap

Problem Description:
A mobile app records the size of each file upload a user performs during a session.
You are given an integer array uploads where uploads[i] is the size in megabytes
of the i-th upload, and an integer cap representing the maximum total data allowed
in a continuous burst.

A burst is any contiguous sequence of uploads. Your task is to return the length
of the longest burst whose total uploaded data is less than or equal to cap.
If no single upload fits within the cap, return 0.

This problem models rate-limited network behavior, where the app wants to identify
the longest uninterrupted period of uploads that stayed within a data budget.
Because the burst must be contiguous, reordering uploads is not allowed.

Constraints:
- 1 <= uploads.length <= 200000
- 0 <= uploads[i] <= 100000
- 0 <= cap <= 1000000000
- All values are integers
"""

from typing import List


class Solution:
    def longest_upload_burst(self, uploads: List[int], cap: int) -> int:
        """
        Compute the maximum length of a contiguous subarray whose sum is <= cap.

        This uses the classic sliding window / two-pointer technique, which works
        efficiently here because all upload sizes are non-negative. That property
        is important: when we expand the window, the sum can only stay the same
        or increase, and when we shrink the window, the sum can only stay the same
        or decrease. This makes it possible to adjust the window in linear time.

        Args:
            uploads: A list of non-negative integers representing upload sizes.
            cap: The maximum allowed total size for a contiguous burst.

        Returns:
            The length of the longest contiguous burst whose total size is <= cap.

        Time complexity:
            O(n), where n is the number of uploads. Each element is added to the
            window once and removed from the window at most once.

        Space complexity:
            O(1), because only a few variables are used regardless of input size.
        """
        # This pointer marks the left boundary of our current sliding window.
        # The window will always represent uploads[left:right+1].
        left: int = 0

        # This variable stores the sum of the current window.
        # We update it incrementally instead of recomputing sums repeatedly,
        # which is what makes the algorithm efficient.
        current_sum: int = 0

        # This stores the best (maximum) valid window length found so far.
        max_length: int = 0

        # We move the right boundary from left to right across the array.
        # At each step, we try to include uploads[right] in the current burst.
        for right in range(len(uploads)):
            # Expand the window by adding the new rightmost upload.
            current_sum += uploads[right]

            # If the window sum is now too large, we must shrink the window
            # from the left until the sum becomes valid again.
            #
            # Why a while loop instead of an if statement?
            # Because a single removal may not be enough. For example:
            # uploads = [4, 2, 1, 7], cap = 3
            # If we add 7, we may need to remove several elements before
            # the window sum is <= cap again.
            while left <= right and current_sum > cap:
                current_sum -= uploads[left]
                left += 1

            # At this point, the current window is guaranteed to be valid
            # (sum <= cap), or it may be empty if left moved past right.
            #
            # The current valid window length is:
            # right - left + 1
            #
            # If left > right, this formula gives 0, which is correct for
            # an empty window.
            window_length: int = right - left + 1

            # Update the best answer if this valid window is longer.
            if window_length > max_length:
                max_length = window_length

        # If no upload fits within the cap, max_length will remain 0.
        return max_length


if __name__ == "__main__":
    solution = Solution()

    # Example 1
    uploads1: List[int] = [4, 2, 1, 7, 3, 2]
    cap1: int = 8
    result1: int = solution.longest_upload_burst(uploads1, cap1)
    print("Example 1:")
    print(f"uploads = {uploads1}")
    print(f"cap = {cap1}")
    print(f"Longest upload burst length = {result1}")
    print()

    # Example 2
    # Note: The problem statement's listed output says 4, but its own explanation
    # correctly shows that [1, 2, 1, 1] sums to 5 and is invalid when cap = 4.
    # Therefore, the correct answer is 3.
    uploads2: List[int] = [9, 1, 2, 1, 1]
    cap2: int = 4
    result2: int = solution.longest_upload_burst(uploads2, cap2)
    print("Example 2:")
    print(f"uploads = {uploads2}")
    print(f"cap = {cap2}")
    print(f"Longest upload burst length = {result2}")
    print()

    # Additional quick checks
    uploads3: List[int] = [10, 11, 12]
    cap3: int = 5
    result3: int = solution.longest_upload_burst(uploads3, cap3)
    print("Additional Check 1:")
    print(f"uploads = {uploads3}")
    print(f"cap = {cap3}")
    print(f"Longest upload burst length = {result3}")
    print()

    uploads4: List[int] = [0, 0, 0, 0]
    cap4: int = 0
    result4: int = solution.longest_upload_burst(uploads4, cap4)
    print("Additional Check 2:")
    print(f"uploads = {uploads4}")
    print(f"cap = {cap4}")
    print(f"Longest upload burst length = {result4}")