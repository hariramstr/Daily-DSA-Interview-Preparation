"""
Title: Longest Podcast Queue Within Download Cap

Problem Description:
You are building an offline listening feature for a podcast app. The app shows episodes
in a fixed order, and a user wants to download a contiguous block of episodes to listen
to later. Each episode has a download size in megabytes. Because the user has limited
storage, the total size of the chosen block cannot exceed a given capacity `cap`.

Given an integer array `sizes` where `sizes[i]` is the size of the `i`-th episode,
return the length of the longest contiguous subarray whose total sum is less than or
equal to `cap`. If multiple valid subarrays have the same maximum length, returning
the length alone is sufficient.

This problem models a common streaming or caching constraint and is intended to be
solved efficiently. A brute-force solution that checks every possible subarray will
be too slow for large inputs. Think about how to expand and shrink a window while
maintaining the current total size.

Constraints:
- 1 <= sizes.length <= 200000
- 1 <= sizes[i] <= 100000
- 1 <= cap <= 10^15
- The answer is always at least 1 if some single episode fits within `cap`;
  otherwise return 0.

Example 1:
Input: sizes = [40, 20, 30, 50, 10, 10], cap = 70
Output: 3
Explanation: The longest valid contiguous block is [50, 10, 10], whose total size is 70.

Example 2:
Input: sizes = [90, 30, 20, 40], cap = 60
Output: 2
Explanation: The best block is [20, 40] with total size 60. Episode 90 alone does not
fit, so it cannot be part of any valid block.
"""

from typing import List


class Solution:
    def longest_podcast_queue(self, sizes: List[int], cap: int) -> int:
        """
        Find the length of the longest contiguous subarray whose sum is <= cap.

        This uses the sliding window / two-pointer technique. Because all episode sizes
        are positive integers, once the current window sum becomes too large, moving the
        left pointer to the right will strictly decrease the sum. That property makes
        sliding window the correct and efficient approach.

        Args:
            sizes: A list of positive integers where sizes[i] is the size of the i-th episode.
            cap: The maximum allowed total size for the chosen contiguous block.

        Returns:
            The maximum length of a contiguous subarray with sum less than or equal to cap.
            Returns 0 if no single episode fits within cap.

        Time complexity:
            O(n), where n is the number of episodes. Each element is added to the window
            once and removed from the window at most once.

        Space complexity:
            O(1), ignoring the input storage, because only a few variables are used.
        """
        # `left` marks the start index of the current sliding window.
        # We will expand the window by moving `right` from left to right across the array.
        left: int = 0

        # `current_sum` stores the total size of all episodes currently inside the window
        # from index `left` to index `right`, inclusive.
        current_sum: int = 0

        # `best_length` stores the maximum valid window length found so far.
        best_length: int = 0

        # We iterate `right` through every episode index.
        # At each step, we include sizes[right] into the current window.
        for right in range(len(sizes)):
            current_sum += sizes[right]

            # If the window sum is too large, we must shrink the window from the left
            # until the sum becomes valid again (<= cap).
            #
            # Why this works:
            # - All values are positive.
            # - Adding more elements can only increase the sum.
            # - Removing elements from the left decreases the sum.
            # Therefore, once the sum exceeds cap, the only way to restore validity
            # while keeping the window contiguous is to move `left` forward.
            while current_sum > cap and left <= right:
                current_sum -= sizes[left]
                left += 1

            # After the shrinking loop finishes, one of two things is true:
            # 1. The window is valid: current_sum <= cap
            # 2. The window is empty or effectively invalid because all included elements
            #    were removed. In that case, the computed length will not improve best_length.
            #
            # If the current window is valid, compute its length and update the answer.
            if current_sum <= cap:
                current_length: int = right - left + 1
                if current_length > best_length:
                    best_length = current_length

        return best_length

    def solve(self, sizes: List[int], cap: int) -> int:
        """
        Wrapper method that calls the main algorithm.

        Args:
            sizes: A list of positive episode sizes.
            cap: Maximum allowed total size.

        Returns:
            The length of the longest valid contiguous block.

        Time complexity:
            O(n), where n is the length of sizes.

        Space complexity:
            O(1).
        """
        return self.longest_podcast_queue(sizes, cap)


if __name__ == "__main__":
    solution = Solution()

    # Example 1:
    # sizes = [40, 20, 30, 50, 10, 10], cap = 70
    # Valid longest block is [50, 10, 10] with length 3.
    sizes1: List[int] = [40, 20, 30, 50, 10, 10]
    cap1: int = 70
    result1: int = solution.solve(sizes1, cap1)
    print("Example 1 Result:", result1)  # Expected: 3

    # Example 2:
    # sizes = [90, 30, 20, 40], cap = 60
    # Best valid block is [20, 40] with length 2.
    sizes2: List[int] = [90, 30, 20, 40]
    cap2: int = 60
    result2: int = solution.solve(sizes2, cap2)
    print("Example 2 Result:", result2)  # Expected: 2

    # Additional quick checks for beginner-friendly demonstration.
    sizes3: List[int] = [5, 5, 5, 5]
    cap3: int = 10
    result3: int = solution.solve(sizes3, cap3)
    print("Additional Example 3 Result:", result3)  # Expected: 2

    sizes4: List[int] = [100]
    cap4: int = 50
    result4: int = solution.solve(sizes4, cap4)
    print("Additional Example 4 Result:", result4)  # Expected: 0