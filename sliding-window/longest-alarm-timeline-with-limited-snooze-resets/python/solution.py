"""
Title: Longest Alarm Timeline With Limited Snooze Resets

Problem Description:
A productivity app records a user's wake-up behavior over several days. For each day,
the app stores an integer in an array `alarms`, where `alarms[i]` is the alarm label
used on day `i`. Equal values mean the same exact alarm sound was used again.

The app considers a contiguous block of days to be a valid timeline if no alarm label
appears more than `limit` times inside that block. However, the app is allowed to apply
up to `k` snooze resets inside the chosen block. A snooze reset can be assigned to any
single day in the block and makes that day exempt from the frequency rule, meaning its
alarm label does not count toward the `limit` for that label. Each day can use at most
one reset.

Return the length of the longest contiguous subarray that can be made valid using at most
`k` snooze resets.

In other words, for a chosen window, if an alarm label appears `f` times, then at least
`max(0, f - limit)` of those occurrences must be covered by snooze resets. The total
number of required resets across all labels in the window must be at most `k`.

Design an algorithm efficient enough for large inputs.

Constraints:
- 1 <= alarms.length <= 200000
- 1 <= alarms[i] <= 10^9
- 0 <= k <= alarms.length
- 1 <= limit <= alarms.length
"""

from collections import defaultdict
from typing import DefaultDict, List


class Solution:
    def longest_alarm_timeline(self, alarms: List[int], limit: int, k: int) -> int:
        """
        Find the maximum length of a contiguous subarray that can be made valid
        using at most k snooze resets.

        A window is valid if the total number of "extra" occurrences beyond `limit`
        across all values is at most `k`.

        Args:
            alarms: List of alarm labels.
            limit: Maximum allowed counted occurrences of any label in the window.
            k: Maximum number of snooze resets available.

        Returns:
            The length of the longest valid contiguous subarray.

        Time complexity:
            O(n), where n is the length of alarms.
            Each element is added to the window once and removed once.

        Space complexity:
            O(m), where m is the number of distinct alarm labels in the current window
            (or overall array in the worst case).
        """
        # Frequency map for the current sliding window.
        # count[value] tells us how many times `value` currently appears
        # between indices left and right, inclusive.
        count: DefaultDict[int, int] = defaultdict(int)

        # `needed_resets` stores the exact number of snooze resets required
        # to make the current window valid.
        #
        # For one label with frequency f:
        # required resets for that label = max(0, f - limit)
        #
        # Summed across all labels:
        # needed_resets = sum(max(0, freq[label] - limit))
        #
        # Instead of recomputing this sum from scratch for every window,
        # we update it incrementally whenever we expand or shrink the window.
        needed_resets = 0

        # Standard sliding window left boundary.
        left = 0

        # Best answer found so far.
        best = 0

        # Expand the window by moving `right` from left to right across the array.
        for right, value in enumerate(alarms):
            # Add the new rightmost value into the window.
            count[value] += 1

            # If after adding this value its frequency becomes greater than `limit`,
            # then this new occurrence creates exactly one additional "extra" item
            # that must be covered by a snooze reset.
            #
            # Example with limit = 2:
            # frequency changes 2 -> 3, so required resets for this label changes 0 -> 1
            # frequency changes 3 -> 4, so required resets changes 1 -> 2
            #
            # Therefore, every time count[value] > limit after insertion,
            # we increase needed_resets by 1.
            if count[value] > limit:
                needed_resets += 1

            # If the current window requires too many resets, it is invalid.
            # We must shrink it from the left until it becomes valid again.
            while needed_resets > k:
                left_value = alarms[left]

                # Before removing alarms[left], if its current frequency is greater than `limit`,
                # then removing one occurrence will reduce the number of required resets by 1.
                #
                # Example with limit = 2:
                # frequency changes 4 -> 3, required resets 2 -> 1
                # frequency changes 3 -> 2, required resets 1 -> 0
                #
                # So whenever count[left_value] > limit before decrementing,
                # needed_resets decreases by 1.
                if count[left_value] > limit:
                    needed_resets -= 1

                # Actually remove the leftmost element from the window.
                count[left_value] -= 1

                # Move the left boundary rightward.
                left += 1

            # At this point, the window [left, right] is valid:
            # needed_resets <= k
            current_length = right - left + 1
            if current_length > best:
                best = current_length

        return best


if __name__ == "__main__":
    solution = Solution()

    # Example 1
    alarms_1 = [5, 1, 5, 2, 5, 1, 1]
    limit_1 = 2
    k_1 = 1
    result_1 = solution.longest_alarm_timeline(alarms_1, limit_1, k_1)
    print("Example 1 result:", result_1)  # Expected: 5

    # Example 2
    alarms_2 = [4, 4, 4, 3, 3, 4, 3, 3]
    limit_2 = 1
    k_2 = 3
    result_2 = solution.longest_alarm_timeline(alarms_2, limit_2, k_2)
    print("Example 2 result:", result_2)  # Expected: 5

    # Additional quick checks
    print("Additional check 1:", solution.longest_alarm_timeline([1, 1, 1, 1], 2, 0))  # Expected: 2
    print("Additional check 2:", solution.longest_alarm_timeline([1, 1, 1, 1], 2, 2))  # Expected: 4
    print("Additional check 3:", solution.longest_alarm_timeline([1, 2, 3, 4], 1, 0))  # Expected: 4