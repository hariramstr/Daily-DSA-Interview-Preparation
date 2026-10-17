"""
Title: Longest Sleep Tracker Streak With Limited Wake Spikes

Problem Description:
A wearable device records a user's sleep state once per minute. The data is stored in
an integer array states where each value is either 0 or 1. A value of 1 means the user
was asleep during that minute, and 0 means they were awake. Because short wake-ups are
common and can be ignored by the app, the product team wants to measure the longest
continuous sleep streak if you are allowed to treat at most k awake minutes as sleep.

Your task is to return the length of the longest contiguous subarray that can be
considered a sleep streak after converting at most k values of 0 into 1. In other words,
find the maximum number of consecutive minutes that contain no more than k awake samples.

This is a realistic streaming-analysis problem: the chosen interval must remain contiguous
in the original timeline, and you may only ignore up to k wake spikes inside that interval.

Constraints:
- 1 <= states.length <= 200000
- states[i] is either 0 or 1
- 0 <= k <= states.length

Example 1:
Input: states = [1,1,0,1,0,1,1,1], k = 1
Output: 5

Example 2:
Input: states = [0,0,1,1,1,0,1,1,0], k = 2
Output: 7
"""

from typing import List


class Solution:
    def longest_sleep_streak(self, states: List[int], k: int) -> int:
        """
        Find the maximum length of a contiguous subarray containing at most k zeros.

        We interpret zeros as awake minutes and ones as asleep minutes. Since we are
        allowed to treat at most k awake minutes as sleep, the problem becomes:
        "What is the longest contiguous window that contains no more than k zeros?"

        Args:
            states: A list of integers where 1 means asleep and 0 means awake.
            k: The maximum number of awake minutes allowed inside the chosen window.

        Returns:
            The length of the longest valid contiguous sleep streak.

        Time complexity:
            O(n), where n is the length of states, because each pointer moves at most n times.

        Space complexity:
            O(1), because we use only a few extra variables.
        """
        # We use the classic "sliding window" technique.
        #
        # Idea:
        # - Maintain a window [left, right] that always represents a contiguous segment.
        # - Count how many zeros (awake minutes) are inside this window.
        # - If the number of zeros becomes greater than k, the window is no longer valid.
        #   In that case, move the left pointer to shrink the window until it becomes valid again.
        # - At every step, record the largest valid window length seen so far.
        #
        # Why sliding window is a good fit:
        # - We need a contiguous subarray.
        # - The condition "at most k zeros" can be maintained incrementally.
        # - This avoids checking every possible subarray, which would be far too slow.

        left: int = 0

        # This variable stores how many awake minutes (0 values) are currently inside
        # the active window from index left to index right.
        zero_count: int = 0

        # This will store the best answer found so far.
        max_length: int = 0

        # Expand the window by moving the right pointer one step at a time.
        for right in range(len(states)):
            # If the new element entering the window is 0, then the number of awake
            # minutes inside the window increases by 1.
            if states[right] == 0:
                zero_count += 1

            # If the window now contains too many zeros, it is invalid.
            # We must shrink it from the left until the number of zeros is at most k again.
            while zero_count > k:
                # Before moving left forward, check whether the element leaving the window
                # is a zero. If it is, then the zero count inside the window decreases.
                if states[left] == 0:
                    zero_count -= 1

                # Move the left boundary rightward to make the window smaller.
                left += 1

            # At this point, the window [left, right] is guaranteed to be valid:
            # it contains at most k zeros.
            #
            # So we compute its length and compare it with the best answer so far.
            current_length: int = right - left + 1
            if current_length > max_length:
                max_length = current_length

        # After processing all positions, max_length is the longest valid window.
        return max_length

    def longestOnes(self, states: List[int], k: int) -> int:
        """
        Compatibility wrapper using a common interview-style method name.

        Args:
            states: A list of integers where 1 means asleep and 0 means awake.
            k: The maximum number of awake minutes allowed inside the chosen window.

        Returns:
            The length of the longest valid contiguous sleep streak.

        Time complexity:
            O(n), where n is the length of states.

        Space complexity:
            O(1).
        """
        return self.longest_sleep_streak(states, k)


if __name__ == "__main__":
    solution = Solution()

    # Example 1
    states1: List[int] = [1, 1, 0, 1, 0, 1, 1, 1]
    k1: int = 1
    result1: int = solution.longest_sleep_streak(states1, k1)
    print("Example 1 Result:", result1)  # Expected: 5

    # Example 2
    states2: List[int] = [0, 0, 1, 1, 1, 0, 1, 1, 0]
    k2: int = 2
    result2: int = solution.longest_sleep_streak(states2, k2)
    print("Example 2 Result:", result2)  # Expected: 7

    # Additional beginner-friendly checks
    states3: List[int] = [1, 1, 1, 1]
    k3: int = 0
    result3: int = solution.longest_sleep_streak(states3, k3)
    print("All asleep, no conversions needed:", result3)  # Expected: 4

    states4: List[int] = [0, 0, 0, 0]
    k4: int = 2
    result4: int = solution.longest_sleep_streak(states4, k4)
    print("All awake, convert at most 2:", result4)  # Expected: 2

    states5: List[int] = [1, 0, 1, 0, 1, 0, 1]
    k5: int = 2
    result5: int = solution.longest_sleep_streak(states5, k5)
    print("Alternating pattern with 2 conversions:", result5)  # Expected: 5