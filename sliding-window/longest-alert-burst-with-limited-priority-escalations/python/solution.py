"""
Title: Longest Alert Burst With Limited Priority Escalations

Problem Description:
A monitoring system records a stream of alert priorities over time as a string alerts,
where each character is either 'L' (low priority) or 'H' (high priority). The operations
team wants to identify the longest contiguous burst of alerts that can be treated as a
mostly low-priority incident window.

You are allowed to escalate at most k high-priority alerts inside a chosen contiguous
window, meaning those 'H' alerts can be treated as if they were 'L' for reporting
purposes. Return the length of the longest contiguous substring that can be made entirely
low priority after at most k such escalations.

In other words, find the maximum window length containing at most k occurrences of 'H'.

This problem models real incident analysis, where a team may tolerate a small number of
severe alerts inside an otherwise routine burst. An efficient solution is expected because
the alert stream can be very large.

Constraints:
- 1 <= alerts.length <= 200000
- alerts[i] is either 'L' or 'H'
- 0 <= k <= alerts.length

Example 1:
Input: alerts = "LLHLLHLLL", k = 1
Output: 5
Explanation: The window "LHLLL" contains exactly one 'H', so it can be fully treated as
low priority after one escalation. No longer valid window exists.

Example 2:
Input: alerts = "HHLLLHLH", k = 2
Output: 6
Explanation: One optimal window is "HLLLHL", which contains two 'H' characters. After
escalating both, the entire window is considered low priority, giving length 6.
"""

from typing import List


class Solution:
    def longest_alert_burst(self, alerts: str, k: int) -> int:
        """
        Find the maximum length of a contiguous substring containing at most k 'H' characters.

        Args:
            alerts: A string of 'L' and 'H' characters representing alert priorities.
            k: Maximum number of high-priority alerts that may be escalated.

        Returns:
            The length of the longest valid contiguous window.

        Time complexity:
            O(n), where n is the length of alerts, because each character is processed
            at most twice: once by the right pointer and once by the left pointer.

        Space complexity:
            O(1), because only a few integer variables are used regardless of input size.
        """
        # We use the classic "sliding window" technique.
        #
        # Main idea:
        # - We maintain a window alerts[left:right+1].
        # - This window should always contain at most k 'H' characters.
        # - If adding a new character makes the window invalid (too many 'H'),
        #   we move the left side forward until the window becomes valid again.
        #
        # Why sliding window is a good choice:
        # - We need the longest contiguous substring.
        # - The condition "at most k 'H'" can be updated incrementally as the window grows/shrinks.
        # - This avoids checking every substring, which would be far too slow.

        # Left boundary of the current window.
        left: int = 0

        # Count how many 'H' characters are currently inside the window.
        high_count: int = 0

        # Best answer found so far.
        max_length: int = 0

        # Expand the window one character at a time using the right pointer.
        for right in range(len(alerts)):
            # Step 1: Include alerts[right] into the current window.
            #
            # If the new character is 'H', then the number of high-priority alerts
            # inside the window increases by 1.
            if alerts[right] == "H":
                high_count += 1

            # Step 2: If the window is invalid, shrink it from the left.
            #
            # The window is invalid when it contains more than k 'H' characters,
            # because we are only allowed to escalate at most k of them.
            #
            # We keep moving left forward until the window becomes valid again.
            while high_count > k:
                # Before removing alerts[left] from the window, check whether it is 'H'.
                # If it is, then removing it decreases the number of high-priority alerts
                # currently inside the window.
                if alerts[left] == "H":
                    high_count -= 1

                # Move the left boundary to the right, effectively removing one character
                # from the current window.
                left += 1

            # Step 3: At this point, the window alerts[left:right+1] is guaranteed valid.
            #
            # So we compute its length and compare it with the best answer seen so far.
            current_length: int = right - left + 1
            if current_length > max_length:
                max_length = current_length

        # After processing all positions, max_length stores the longest valid window.
        return max_length

    def longestAlertBurst(self, alerts: str, k: int) -> int:
        """
        Wrapper method using camelCase naming for compatibility with common interview styles.

        Args:
            alerts: A string of 'L' and 'H' characters representing alert priorities.
            k: Maximum number of high-priority alerts that may be escalated.

        Returns:
            The length of the longest valid contiguous window.

        Time complexity:
            O(n), where n is the length of alerts.

        Space complexity:
            O(1).
        """
        return self.longest_alert_burst(alerts, k)


if __name__ == "__main__":
    solution = Solution()

    # Example 1
    alerts_1: str = "LLHLLHLLL"
    k_1: int = 1
    result_1: int = solution.longest_alert_burst(alerts_1, k_1)
    print(f"alerts = {alerts_1}, k = {k_1} -> {result_1}")

    # Example 2
    alerts_2: str = "HHLLLHLH"
    k_2: int = 2
    result_2: int = solution.longest_alert_burst(alerts_2, k_2)
    print(f"alerts = {alerts_2}, k = {k_2} -> {result_2}")

    # Additional simple checks
    alerts_3: str = "LLLLL"
    k_3: int = 0
    result_3: int = solution.longest_alert_burst(alerts_3, k_3)
    print(f"alerts = {alerts_3}, k = {k_3} -> {result_3}")

    alerts_4: str = "HHHH"
    k_4: int = 2
    result_4: int = solution.longest_alert_burst(alerts_4, k_4)
    print(f"alerts = {alerts_4}, k = {k_4} -> {result_4}")