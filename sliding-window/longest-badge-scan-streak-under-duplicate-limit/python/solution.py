"""
Title: Longest Badge Scan Streak Under Duplicate Limit

Problem Description:
A security team is analyzing a hallway badge scanner that records employee badge IDs
in the order they were scanned. Because people may walk back and forth, the same
badge ID can appear multiple times in a row or later in the log. The team wants to
find the longest contiguous portion of the scan log that is still considered "clean"
under a simple rule: within that portion, no badge ID may appear more than k times.

Given an array scans where scans[i] is the badge ID seen at time i, and an integer k,
return the length of the longest contiguous subarray such that every distinct badge ID
in that subarray appears at most k times.

This is an interview-style sliding window problem. A good solution should expand the
right side of the window and shrink the left side only when some badge ID appears too
many times.

Constraints:
- 1 <= scans.length <= 100000
- 1 <= scans[i] <= 1000000000
- 1 <= k <= scans.length
- The answer fits in a 32-bit integer

Example 1:
Input: scans = [5, 7, 5, 7, 5, 8], k = 2
Output: 5
Explanation: The longest valid window is [7, 5, 7, 5, 8]. In this subarray,
badge 7 appears 2 times, badge 5 appears 2 times, and badge 8 appears 1 time.

Example 2:
Input: scans = [3, 3, 3, 2, 2, 1], k = 1
Output: 2
Explanation: With k = 1, all badge IDs inside the chosen window must be unique.
The longest valid windows have length 2, such as [3, 2] or [2, 1].
"""

from typing import Dict, List


class Solution:
    def max_subarray_length(self, scans: List[int], k: int) -> int:
        """
        Find the length of the longest contiguous subarray where every distinct
        value appears at most k times.

        Args:
            scans: A list of badge IDs in scan order.
            k: The maximum allowed frequency for any badge ID inside the window.

        Returns:
            The maximum length of a valid contiguous subarray.

        Time complexity:
            O(n), where n is the length of scans.
            Each element is added to the window once and removed at most once.

        Space complexity:
            O(m), where m is the number of distinct badge IDs currently tracked
            in the frequency dictionary. In the worst case, O(n).
        """
        # This dictionary stores how many times each badge ID appears
        # inside the current sliding window.
        #
        # Key   -> badge ID
        # Value -> count of that badge ID in the current window
        counts: Dict[int, int] = {}

        # left marks the beginning of the current window.
        # We will expand the window by moving right forward,
        # and shrink the window by moving left forward when needed.
        left: int = 0

        # best stores the maximum valid window length found so far.
        best: int = 0

        # We move right from left to right across the array.
        # At each step, we include scans[right] into the window.
        for right, badge_id in enumerate(scans):
            # Add the new badge ID into the frequency map.
            # If it is not already present, start from 0.
            counts[badge_id] = counts.get(badge_id, 0) + 1

            # After adding scans[right], the window may become invalid.
            # The only badge count that could have just exceeded k is badge_id,
            # because all other counts were unchanged in this step.
            #
            # So while this specific badge ID appears too many times,
            # we shrink the window from the left until it becomes valid again.
            while counts[badge_id] > k:
                # Identify the badge ID that is leaving the window.
                left_badge_id: int = scans[left]

                # Decrease its count because we are moving left forward.
                counts[left_badge_id] -= 1

                # Move the left boundary rightward by one position.
                left += 1

            # At this point, the window scans[left:right+1] is valid:
            # every badge ID appears at most k times.
            current_length: int = right - left + 1

            # Update the best answer if this valid window is larger.
            if current_length > best:
                best = current_length

        return best


if __name__ == "__main__":
    solution = Solution()

    # Example 1
    scans1: List[int] = [5, 7, 5, 7, 5, 8]
    k1: int = 2
    result1: int = solution.max_subarray_length(scans1, k1)
    print(f"Input: scans = {scans1}, k = {k1}")
    print(f"Output: {result1}")
    print("Expected: 5")
    print()

    # Example 2
    scans2: List[int] = [3, 3, 3, 2, 2, 1]
    k2: int = 1
    result2: int = solution.max_subarray_length(scans2, k2)
    print(f"Input: scans = {scans2}, k = {k2}")
    print(f"Output: {result2}")
    print("Expected: 2")
    print()

    # Additional sample
    scans3: List[int] = [1, 2, 1, 2, 1, 2, 3]
    k3: int = 2
    result3: int = solution.max_subarray_length(scans3, k3)
    print(f"Input: scans = {scans3}, k = {k3}")
    print(f"Output: {result3}")
    print("Expected: 5")