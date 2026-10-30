"""
Title: Shortest Sensor Window Covering Required Device Types
Difficulty: Hard
Topic: Sliding Window

Problem Description:
You are given a stream of sensor events collected in chronological order. Each event belongs
to a device type represented by a string in the array events, where events[i] is the type
of the i-th event. You are also given a map required where required[t] is the minimum number
of events of device type t that must appear inside a valid contiguous window.

Your task is to return the length of the shortest contiguous subarray of events that satisfies
all required device-type quotas. A window [l, r] is valid if for every device type t in required,
the number of occurrences of t inside events[l..r] is at least required[t]. If no such window
exists, return -1.

This problem is harder than a standard minimum-cover window because the stream can be very large,
device type names are arbitrary strings, and multiple quotas may need to be met simultaneously.
An efficient solution is expected.

Constraints:
- 1 <= events.length <= 200000
- 1 <= events[i].length <= 20
- events[i] consists of lowercase English letters, digits, or underscores
- 1 <= required.size() <= 50000
- 1 <= required[t] <= 100000
- The sum of all required counts may be greater than events.length
- Only device types present in required matter for validity; other event types may appear anywhere
  in the window

Example 1:
Input: events = ["cam","temp","cam","door","temp","cam","door"], required = {"cam":2,"door":1}
Output: 4

Explanation:
A valid window must contain at least two "cam" events and one "door" event.
The shortest valid contiguous window is events[2..5] = ["cam","door","temp","cam"], length 4.
No length-3 contiguous window contains two "cam" and one "door".

Example 2:
Input: events = ["a","b","c","a","b","a","d"], required = {"a":2,"b":2,"d":1}
Output: 6

Explanation:
The shortest valid window is events[1..6] = ["b","c","a","b","a","d"], which contains
two "a", two "b", and one "d". No shorter window satisfies all quotas.
"""

from typing import Dict, List


class Solution:
    def min_window_length(self, events: List[str], required: Dict[str, int]) -> int:
        """
        Find the length of the shortest contiguous subarray whose counts satisfy all required quotas.

        Args:
            events: Chronological list of sensor event device types.
            required: Mapping from device type to the minimum count needed in a valid window.

        Returns:
            The minimum valid window length, or -1 if no valid window exists.

        Time Complexity:
            O(n), where n is len(events), because each pointer in the sliding window
            moves from left to right at most once.

        Space Complexity:
            O(k), where k is the number of distinct required device types, for the
            frequency map used inside the current window.
        """
        # If there are no required types, the problem constraints say required.size() >= 1,
        # but this guard makes the method more robust and self-contained.
        if not required:
            return 0

        # ---------------------------------------------------------------------
        # Early impossibility check:
        #
        # Before running the sliding window, we can count how many times each
        # required device type appears in the entire events array.
        #
        # Why do this?
        # - If the full array itself does not contain enough of some required type,
        #   then no subarray can possibly satisfy the requirement.
        # - This avoids doing unnecessary sliding-window work in impossible cases.
        #
        # We only count event types that matter (those present in "required"),
        # because all other types are irrelevant to validity.
        # ---------------------------------------------------------------------
        total_counts: Dict[str, int] = {}
        for event in events:
            if event in required:
                total_counts[event] = total_counts.get(event, 0) + 1

        for device_type, needed_count in required.items():
            if total_counts.get(device_type, 0) < needed_count:
                return -1

        # ---------------------------------------------------------------------
        # Sliding window setup:
        #
        # left  -> left boundary of the current window
        # right -> right boundary of the current window (advanced by the loop)
        #
        # window_counts keeps track of how many times each required device type
        # appears in the current window.
        #
        # formed tracks how many required device types currently meet their quota.
        # Example:
        #   required = {"cam": 2, "door": 1}
        #   If current window has {"cam": 2, "door": 0}, then formed = 1
        #   If current window has {"cam": 2, "door": 1}, then formed = 2
        #
        # When formed == len(required), the current window is valid.
        # ---------------------------------------------------------------------
        left: int = 0
        window_counts: Dict[str, int] = {}
        formed: int = 0
        need_types: int = len(required)

        # Start with an infinitely large answer so that any real valid window
        # will be smaller.
        best_length: int = float("inf")

        # ---------------------------------------------------------------------
        # Expand the window by moving "right" one step at a time.
        # For each new event:
        #   1. Add it to the current window if it is relevant.
        #   2. If adding it causes that device type to exactly meet its required
        #      quota, increase "formed".
        #   3. While the window is valid, try shrinking from the left to make
        #      it as short as possible.
        # ---------------------------------------------------------------------
        for right, event in enumerate(events):
            # Only required device types affect validity, so we only track those.
            if event in required:
                window_counts[event] = window_counts.get(event, 0) + 1

                # We increase "formed" only when the count becomes exactly equal
                # to the required quota.
                #
                # Why exactly equal?
                # - Suppose required["cam"] = 2.
                # - When count goes from 1 -> 2, the quota becomes satisfied,
                #   so formed should increase by 1.
                # - When count goes from 2 -> 3, the quota was already satisfied,
                #   so formed should NOT increase again.
                if window_counts[event] == required[event]:
                    formed += 1

            # -----------------------------------------------------------------
            # If all required device types are satisfied, the window is valid.
            # Now we try to shrink it from the left while keeping it valid.
            #
            # This is the key idea of the minimum-window sliding window pattern:
            # - Expand until valid
            # - Shrink while still valid
            # - Record the smallest valid window seen
            # -----------------------------------------------------------------
            while formed == need_types:
                current_length: int = right - left + 1
                if current_length < best_length:
                    best_length = current_length

                left_event: str = events[left]

                # If the event leaving the window is relevant, update its count.
                if left_event in required:
                    window_counts[left_event] -= 1

                    # If removing this event causes the count to drop below the
                    # required quota, then the window is no longer valid.
                    #
                    # Example:
                    #   required["door"] = 1
                    #   current count is 1
                    #   after decrement it becomes 0
                    #   so we lost satisfaction for "door"
                    if window_counts[left_event] < required[left_event]:
                        formed -= 1

                # Move the left boundary rightward to continue shrinking.
                left += 1

        # If best_length was never updated, then no valid window was found.
        return -1 if best_length == float("inf") else best_length


if __name__ == "__main__":
    solution = Solution()

    # Example 1
    events1: List[str] = ["cam", "temp", "cam", "door", "temp", "cam", "door"]
    required1: Dict[str, int] = {"cam": 2, "door": 1}
    result1: int = solution.min_window_length(events1, required1)
    print("Example 1 Result:", result1)  # Expected: 4

    # Example 2
    events2: List[str] = ["a", "b", "c", "a", "b", "a", "d"]
    required2: Dict[str, int] = {"a": 2, "b": 2, "d": 1}
    result2: int = solution.min_window_length(events2, required2)
    print("Example 2 Result:", result2)  # Expected: 6

    # Additional quick checks
    events3: List[str] = ["x", "y", "z"]
    required3: Dict[str, int] = {"x": 1, "z": 1}
    result3: int = solution.min_window_length(events3, required3)
    print("Additional Check 1:", result3)  # Expected: 3

    events4: List[str] = ["x", "y", "x", "y"]
    required4: Dict[str, int] = {"x": 3}
    result4: int = solution.min_window_length(events4, required4)
    print("Additional Check 2:", result4)  # Expected: -1