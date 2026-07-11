"""
Title: Longest Support Queue With Limited VIP Skips

Problem Description:
A customer support system records the arrival order of tickets in an array `tickets`,
where `tickets[i]` is `0` for a regular ticket and `1` for a VIP ticket.

The support team wants to analyze contiguous portions of the queue that can be handled
by one standard agent.

A standard agent can process any number of regular tickets, but can tolerate at most
`k` VIP tickets in the assigned contiguous segment by forwarding those VIP tickets to
a specialist.

Your task is to find the length of the longest contiguous subarray of `tickets` that
contains at most `k` VIP tickets.

Return the maximum possible length of such a segment.

This models a realistic queue-monitoring problem where a team wants the longest
uninterrupted block of work that stays within a forwarding limit.

Constraints:
- 1 <= tickets.length <= 100000
- tickets[i] is either 0 or 1
- 0 <= k <= tickets.length

Example 1:
Input: tickets = [0,1,0,0,1,0,0,0], k = 1
Output: 5

Example 2:
Input: tickets = [1,0,1,0,0,1,0], k = 2
Output: 6
"""

from typing import List


class Solution:
    def longest_support_queue(self, tickets: List[int], k: int) -> int:
        """
        Find the length of the longest contiguous subarray containing at most k VIP tickets.

        Args:
            tickets: A list of integers where 0 represents a regular ticket and 1 represents a VIP ticket.
            k: The maximum number of VIP tickets allowed in the chosen contiguous segment.

        Returns:
            The maximum length of a contiguous subarray with at most k VIP tickets.

        Time complexity:
            O(n), where n is the length of tickets, because each pointer moves at most n times.

        Space complexity:
            O(1), because only a few variables are used regardless of input size.
        """
        # We use the classic "sliding window" technique.
        #
        # Core idea:
        # - Maintain a window [left, right] that always represents a contiguous segment.
        # - Expand the window by moving `right` one step at a time.
        # - Count how many VIP tickets (1s) are inside the current window.
        # - If the number of VIP tickets becomes greater than k, the window is invalid.
        # - To restore validity, move `left` forward until the window has at most k VIP tickets again.
        #
        # Why sliding window works well here:
        # - We need the longest contiguous segment.
        # - The condition "at most k VIP tickets" can be updated incrementally as the window grows/shrinks.
        # - This avoids checking all subarrays, which would be far too slow.

        # `left` marks the start of the current window.
        left: int = 0

        # `vip_count` stores how many 1s are currently inside the window [left, right].
        vip_count: int = 0

        # `max_length` stores the best valid window length found so far.
        max_length: int = 0

        # Move `right` across the array one position at a time.
        for right in range(len(tickets)):
            # Step 1: Include tickets[right] into the current window.
            #
            # If the new ticket is a VIP ticket (1), increase the VIP counter.
            if tickets[right] == 1:
                vip_count += 1

            # Step 2: If the window is invalid (too many VIP tickets),
            # shrink it from the left until it becomes valid again.
            #
            # We use a while loop, not an if statement, because the window may need
            # to shrink by multiple positions before the VIP count drops back to <= k.
            while vip_count > k:
                # Before moving `left`, check whether the ticket leaving the window
                # is a VIP ticket. If yes, reduce the VIP counter.
                if tickets[left] == 1:
                    vip_count -= 1

                # Move the left boundary rightward to shrink the window.
                left += 1

            # Step 3: At this point, the window [left, right] is guaranteed valid:
            # it contains at most k VIP tickets.
            #
            # So we compute its length and update the best answer if needed.
            current_length: int = right - left + 1
            if current_length > max_length:
                max_length = current_length

        # After scanning the entire array, `max_length` is the answer.
        return max_length

    def longestSubarray(self, tickets: List[int], k: int) -> int:
        """
        Wrapper method using a common interview-style name.

        Args:
            tickets: A list of integers where 0 represents a regular ticket and 1 represents a VIP ticket.
            k: The maximum number of VIP tickets allowed in the chosen contiguous segment.

        Returns:
            The maximum length of a contiguous subarray with at most k VIP tickets.

        Time complexity:
            O(n), where n is the length of tickets.

        Space complexity:
            O(1).
        """
        return self.longest_support_queue(tickets, k)


if __name__ == "__main__":
    solution = Solution()

    # Example 1
    tickets1: List[int] = [0, 1, 0, 0, 1, 0, 0, 0]
    k1: int = 1
    result1: int = solution.longest_support_queue(tickets1, k1)
    print("Example 1 Result:", result1)  # Expected: 5

    # Example 2
    tickets2: List[int] = [1, 0, 1, 0, 0, 1, 0]
    k2: int = 2
    result2: int = solution.longest_support_queue(tickets2, k2)
    print("Example 2 Result:", result2)  # Expected: 6

    # Additional beginner-friendly checks
    tickets3: List[int] = [0, 0, 0, 0]
    k3: int = 0
    result3: int = solution.longest_support_queue(tickets3, k3)
    print("All regular tickets:", result3)  # Expected: 4

    tickets4: List[int] = [1, 1, 1, 1]
    k4: int = 2
    result4: int = solution.longest_support_queue(tickets4, k4)
    print("All VIP tickets with k=2:", result4)  # Expected: 2

    tickets5: List[int] = [1]
    k5: int = 0
    result5: int = solution.longest_support_queue(tickets5, k5)
    print("Single VIP with k=0:", result5)  # Expected: 0