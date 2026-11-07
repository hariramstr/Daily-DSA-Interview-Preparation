"""
Title: Longest Recipe Video Segment With Limited Step Repeats

Problem Description:
You are given an array steps where steps[i] is the step ID shown at second i of a
cooking tutorial video. The same step may appear multiple times in a row or reappear
later because the editor repeats important actions such as chopping, mixing, or plating.
You are also given an integer k. A video segment is considered watchable if, within that
contiguous segment, no single step ID appears more than k times.

Your task is to return the length of the longest watchable contiguous segment.

In other words, find the maximum size of a subarray of steps such that the frequency of
every distinct value inside that subarray is at most k.

This problem models a realistic streaming feature: the app wants to recommend the longest
clip that does not over-repeat any one instruction beyond a user-defined tolerance.

Constraints:
- 1 <= steps.length <= 200000
- 1 <= steps[i] <= 1000000000
- 1 <= k <= steps.length
- The answer must be computed in O(n) or O(n log n) time for full credit.

Example 1:
Input: steps = [4, 2, 4, 3, 2, 4, 1], k = 2
Output: 5

Example 2:
Input: steps = [7, 7, 7, 8, 8, 9, 7], k = 1
Output: 3
"""

from typing import Dict, List


class Solution:
    def max_watchable_segment(self, steps: List[int], k: int) -> int:
        """
        Find the maximum length of a contiguous subarray where no value appears more than k times.

        This uses the sliding window technique:
        - Expand the right side of the window one element at a time.
        - Track frequencies of values inside the current window.
        - If adding a new value causes its frequency to exceed k, move the left side
          forward until the window becomes valid again.
        - Record the largest valid window length seen.

        Args:
            steps: List of step IDs shown in the video over time.
            k: Maximum allowed frequency for any single step ID inside a valid segment.

        Returns:
            The length of the longest valid contiguous segment.

        Time complexity:
            O(n), where n is the length of steps, because each index is visited at most
            twice: once by the right pointer and once by the left pointer.

        Space complexity:
            O(m), where m is the number of distinct step IDs currently tracked in the
            frequency dictionary. In the worst case, this can be O(n).
        """
        # This dictionary stores how many times each step ID appears inside the
        # current sliding window [left, right].
        #
        # Example:
        # If the current window is [4, 2, 4, 3], then:
        # counts = {4: 2, 2: 1, 3: 1}
        counts: Dict[int, int] = {}

        # The left boundary of our sliding window.
        # We will move this forward whenever the window becomes invalid.
        left: int = 0

        # This will store the best (maximum) valid window length found so far.
        best_length: int = 0

        # We expand the window by moving "right" from left to right across the array.
        for right, step_id in enumerate(steps):
            # Add the new step at index "right" into the window.
            # If the step ID has not been seen in the current window before,
            # start its count at 0 and then increment.
            counts[step_id] = counts.get(step_id, 0) + 1

            # At this point, only the count of "step_id" could have become invalid,
            # because it is the only value we just changed.
            #
            # If its count is now greater than k, the window is invalid.
            # We must shrink from the left until this specific count is back to <= k.
            while counts[step_id] > k:
                # Identify the value that is leaving the window from the left side.
                left_step_id: int = steps[left]

                # Decrease its frequency because it is no longer inside the window.
                counts[left_step_id] -= 1

                # Move the left boundary one step to the right.
                left += 1

            # Now the window [left, right] is valid:
            # every step ID appears at most k times.
            current_length: int = right - left + 1

            # Update the best answer if this valid window is larger than any previous one.
            if current_length > best_length:
                best_length = current_length

        # After processing all positions, best_length is the answer.
        return best_length

    def maxSubarrayLength(self, steps: List[int], k: int) -> int:
        """
        Compatibility wrapper using a common interview-style method name.

        Args:
            steps: List of step IDs shown in the video over time.
            k: Maximum allowed frequency for any single step ID inside a valid segment.

        Returns:
            The length of the longest valid contiguous segment.

        Time complexity:
            O(n)

        Space complexity:
            O(n) in the worst case
        """
        return self.max_watchable_segment(steps, k)


if __name__ == "__main__":
    solution = Solution()

    # Example 1
    steps1: List[int] = [4, 2, 4, 3, 2, 4, 1]
    k1: int = 2
    result1: int = solution.max_watchable_segment(steps1, k1)
    print("Example 1 Result:", result1)  # Expected: 5

    # Example 2
    steps2: List[int] = [7, 7, 7, 8, 8, 9, 7]
    k2: int = 1
    result2: int = solution.max_watchable_segment(steps2, k2)
    print("Example 2 Result:", result2)  # Expected: 3

    # Additional quick checks
    steps3: List[int] = [1, 2, 3, 4]
    k3: int = 1
    result3: int = solution.max_watchable_segment(steps3, k3)
    print("Additional Check 1:", result3)  # Expected: 4

    steps4: List[int] = [5, 5, 5, 5]
    k4: int = 2
    result4: int = solution.max_watchable_segment(steps4, k4)
    print("Additional Check 2:", result4)  # Expected: 2