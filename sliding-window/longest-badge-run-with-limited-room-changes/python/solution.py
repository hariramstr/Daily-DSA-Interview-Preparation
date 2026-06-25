"""
Title: Longest Badge Run With Limited Room Changes

Problem Description:
A security team records the sequence of room IDs visited by an employee during a single day.
The sequence is stored in an integer array rooms, where rooms[i] is the room entered at time i.
For auditing, the team wants to find the longest contiguous time interval during which the
employee visited at most k distinct rooms.

Your task is to return the length of the longest contiguous subarray of rooms that contains
no more than k distinct values.

This models a real monitoring scenario where frequent movement across too many different rooms
may indicate unusual behavior, while a long interval with only a few room types may represent
normal work patterns.

A contiguous interval means you may only choose consecutive entries from the log. If k is 0,
then no room can be included, so the answer is 0.

Constraints:
- 1 <= rooms.length <= 200000
- 0 <= rooms[i] <= 1000000000
- 0 <= k <= rooms.length
- The expected solution should run in O(n) time using a sliding window and a frequency map.

Example 1:
Input: rooms = [4, 2, 2, 7, 2, 4, 4, 7], k = 2
Output: 4
Explanation: The longest valid interval is [2, 2, 7, 2], which contains only the distinct
room IDs {2, 7}. Its length is 4.

Example 2:
Input: rooms = [9, 9, 1, 3, 1, 1, 3, 9], k = 3
Output: 8
Explanation: The entire array contains exactly 3 distinct room IDs: {9, 1, 3}. Therefore
the whole log is valid, and the answer is 8.
"""

from typing import Dict, List


class Solution:
    def longest_badge_run(self, rooms: List[int], k: int) -> int:
        """
        Find the length of the longest contiguous subarray containing at most k distinct room IDs.

        Args:
            rooms: List of room IDs visited over time.
            k: Maximum number of distinct room IDs allowed in the chosen contiguous interval.

        Returns:
            The maximum length of a contiguous subarray with at most k distinct values.

        Time complexity:
            O(n), where n is the length of rooms, because each element is added to and removed
            from the sliding window at most once.

        Space complexity:
            O(k) in the typical sliding-window sense for the frequency map of active distinct
            values in the current window, and O(min(n, number of distinct room IDs overall))
            in the worst case.
        """
        # Special case:
        # If k is 0, we are not allowed to include any room at all.
        # That means the longest valid subarray must have length 0.
        if k == 0:
            return 0

        # This dictionary will store the frequency of each room ID currently inside
        # the sliding window.
        #
        # Example:
        # If the current window is [2, 2, 7, 2], then:
        # freq = {2: 3, 7: 1}
        #
        # Why use a dictionary?
        # - We need to know how many times each room appears in the current window.
        # - When shrinking the window from the left, we decrement counts.
        # - When a count becomes 0, that room is no longer in the window, so we remove it.
        freq: Dict[int, int] = {}

        # left marks the beginning of the current sliding window.
        left: int = 0

        # best stores the maximum valid window length found so far.
        best: int = 0

        # We expand the window by moving right from left to right across the array.
        for right, room_id in enumerate(rooms):
            # Step 1: Include the new room at index "right" into the window.
            #
            # If room_id is already in the window, increase its count.
            # Otherwise, start its count at 1.
            freq[room_id] = freq.get(room_id, 0) + 1

            # Step 2: If the window now contains too many distinct room IDs,
            # shrink it from the left until it becomes valid again.
            #
            # The condition len(freq) > k means the current window has more than
            # k distinct values, so it violates the problem requirement.
            while len(freq) > k:
                # Identify the room ID that is leaving the window from the left side.
                left_room_id = rooms[left]

                # Decrease its frequency because it is no longer fully inside the window.
                freq[left_room_id] -= 1

                # If its frequency becomes 0, that means this room ID no longer appears
                # anywhere in the current window.
                #
                # We remove it from the dictionary so that:
                # - len(freq) correctly reflects the number of distinct room IDs
                # - the window can eventually become valid again
                if freq[left_room_id] == 0:
                    del freq[left_room_id]

                # Move the left boundary one step to the right, effectively shrinking
                # the window.
                left += 1

            # Step 3: At this point, the window [left, right] is guaranteed to be valid,
            # meaning it contains at most k distinct room IDs.
            #
            # So we compute its length and update the best answer if this window is larger.
            current_length = right - left + 1
            if current_length > best:
                best = current_length

        # After processing all positions, best contains the maximum valid window length.
        return best


if __name__ == "__main__":
    solution = Solution()

    # Example 1 from the problem statement
    rooms1 = [4, 2, 2, 7, 2, 4, 4, 7]
    k1 = 2
    result1 = solution.longest_badge_run(rooms1, k1)
    print("Example 1:")
    print("rooms =", rooms1)
    print("k =", k1)
    print("Output =", result1)
    print("Expected = 4")
    print()

    # Example 2 from the problem statement
    rooms2 = [9, 9, 1, 3, 1, 1, 3, 9]
    k2 = 3
    result2 = solution.longest_badge_run(rooms2, k2)
    print("Example 2:")
    print("rooms =", rooms2)
    print("k =", k2)
    print("Output =", result2)
    print("Expected = 8")
    print()

    # Additional beginner-friendly checks
    rooms3 = [1, 2, 1, 2, 3]
    k3 = 2
    result3 = solution.longest_badge_run(rooms3, k3)
    print("Additional Test 1:")
    print("rooms =", rooms3)
    print("k =", k3)
    print("Output =", result3)
    print("Expected = 4")
    print()

    rooms4 = [5, 5, 5, 5]
    k4 = 1
    result4 = solution.longest_badge_run(rooms4, k4)
    print("Additional Test 2:")
    print("rooms =", rooms4)
    print("k =", k4)
    print("Output =", result4)
    print("Expected = 4")
    print()

    rooms5 = [1, 2, 3]
    k5 = 0
    result5 = solution.longest_badge_run(rooms5, k5)
    print("Additional Test 3:")
    print("rooms =", rooms5)
    print("k =", k5)
    print("Output =", result5)
    print("Expected = 0")