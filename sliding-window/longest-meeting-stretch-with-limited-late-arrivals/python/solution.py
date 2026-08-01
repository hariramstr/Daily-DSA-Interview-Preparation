"""
Title: Longest Meeting Stretch With Limited Late Arrivals

Problem Description:
A company tracks a day of back-to-back meeting slots using a binary array `arrivals`,
where `arrivals[i] = 1` means the attendee arrived on time for slot `i`, and
`arrivals[i] = 0` means they arrived late for that slot.

Management wants to identify the longest contiguous stretch of meeting slots that can
still be treated as a "reliable attendance block" if they are willing to excuse at
most `k` late arrivals inside that stretch.

Your task is to return the length of the longest contiguous subarray containing at
most `k` zeros.

In other words, find the maximum number of consecutive meeting slots such that no
more than `k` of them are late arrivals. The chosen block must be contiguous, and
you may excuse any late arrivals already inside the block, but you cannot reorder
slots.

This problem should be solved efficiently for large inputs, so solutions that check
every possible subarray will be too slow.

Constraints:
- 1 <= arrivals.length <= 200000
- arrivals[i] is either 0 or 1
- 0 <= k <= arrivals.length

Example 1:
Input: arrivals = [1,1,0,1,0,1,1,1], k = 1
Output: 5
Explanation: The longest valid block is [1,0,1,1,1], which contains exactly one
late arrival.

Example 2:
Input: arrivals = [0,0,1,1,1,0,1,1], k = 2
Output: 7
Explanation: The subarray [0,1,1,1,0,1,1] has length 7 and contains two late
arrivals, which is allowed.
"""

from typing import List


class Solution:
    def longest_reliable_attendance_block(self, arrivals: List[int], k: int) -> int:
        """
        Find the length of the longest contiguous subarray containing at most k zeros.

        This uses the sliding window technique:
        - Expand the right side of the window one element at a time.
        - Count how many zeros are currently inside the window.
        - If the window becomes invalid (more than k zeros), move the left side
          forward until the window becomes valid again.
        - Track the maximum valid window length seen during the process.

        Args:
            arrivals: A binary list where 1 means on time and 0 means late.
            k: Maximum number of late arrivals (zeros) allowed in the chosen block.

        Returns:
            The maximum length of a contiguous subarray containing at most k zeros.

        Time complexity:
            O(n), where n is the length of arrivals, because each pointer moves
            across the array at most once.

        Space complexity:
            O(1), because only a few variables are used regardless of input size.
        """
        # The left boundary of our sliding window.
        # The window will always represent arrivals[left:right+1].
        left: int = 0

        # This variable stores how many late arrivals (zeros) are currently
        # inside the active window.
        zero_count: int = 0

        # This will store the best (maximum) valid window length found so far.
        max_length: int = 0

        # We move the right boundary from the start of the array to the end.
        # At each step, we include arrivals[right] into the current window.
        for right in range(len(arrivals)):
            # If the new element is a zero, that means we added one more
            # late arrival into the current window, so we increase zero_count.
            if arrivals[right] == 0:
                zero_count += 1

            # At this point, the window may have become invalid if it contains
            # more than k zeros.
            #
            # Why do we use a while loop instead of an if statement?
            # Because moving left by one position may still leave too many zeros
            # in the window. We must keep shrinking until the window is valid.
            while zero_count > k:
                # If the element leaving the window from the left side is zero,
                # we must decrease zero_count because that zero is no longer
                # inside the window.
                if arrivals[left] == 0:
                    zero_count -= 1

                # Move the left boundary to the right, effectively shrinking
                # the window from the left side.
                left += 1

            # Once we exit the while loop, the current window is guaranteed
            # to contain at most k zeros, so it is valid.
            #
            # Current window length is:
            # right - left + 1
            current_length: int = right - left + 1

            # Update the best answer if this valid window is larger than any
            # valid window we have seen before.
            if current_length > max_length:
                max_length = current_length

        # After processing the entire array, max_length holds the answer.
        return max_length

    def longestOnes(self, arrivals: List[int], k: int) -> int:
        """
        Compatibility wrapper using a common interview-style method name.

        Args:
            arrivals: A binary list where 1 means on time and 0 means late.
            k: Maximum number of zeros allowed in the chosen contiguous block.

        Returns:
            The maximum valid contiguous block length.

        Time complexity:
            O(n), where n is the length of arrivals.

        Space complexity:
            O(1).
        """
        return self.longest_reliable_attendance_block(arrivals, k)


if __name__ == "__main__":
    solution = Solution()

    # Example 1:
    # arrivals = [1,1,0,1,0,1,1,1], k = 1
    # Expected output: 5
    arrivals_1: List[int] = [1, 1, 0, 1, 0, 1, 1, 1]
    k_1: int = 1
    result_1: int = solution.longest_reliable_attendance_block(arrivals_1, k_1)
    print("Example 1 Result:", result_1)

    # Example 2:
    # arrivals = [0,0,1,1,1,0,1,1], k = 2
    # Expected output: 7
    arrivals_2: List[int] = [0, 0, 1, 1, 1, 0, 1, 1]
    k_2: int = 2
    result_2: int = solution.longest_reliable_attendance_block(arrivals_2, k_2)
    print("Example 2 Result:", result_2)

    # Additional beginner-friendly checks
    arrivals_3: List[int] = [1, 1, 1, 1]
    k_3: int = 0
    result_3: int = solution.longest_reliable_attendance_block(arrivals_3, k_3)
    print("All on-time, no excuses needed:", result_3)

    arrivals_4: List[int] = [0, 0, 0, 0]
    k_4: int = 2
    result_4: int = solution.longest_reliable_attendance_block(arrivals_4, k_4)
    print("All late, excuse up to 2:", result_4)