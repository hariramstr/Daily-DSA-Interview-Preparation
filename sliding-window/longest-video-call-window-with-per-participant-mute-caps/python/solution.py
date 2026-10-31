"""
Title: Longest Video Call Window With Per-Participant Mute Caps

Problem Description:
You are given a video call activity log represented by an array participants, where
participants[i] is the participant ID speaking during second i of a recorded meeting.
You are also given an integer array muteCap of length m, where muteCap[id] indicates
the maximum number of times participant id is allowed to appear inside a valid
contiguous window. Participant IDs are in the range [0, m - 1].

A contiguous time window is considered stable if, for every participant id, the
number of occurrences of id inside the window is at most muteCap[id]. Your task is
to return the length of the longest stable window.

This is harder than a standard frequency-limited sliding window because muteCap may
be different for every participant, some caps may be zero, and the input size is
large enough that any O(n * m) or repeated full-map validation will time out. You
need to design an algorithm that updates validity as the window expands and shrinks.

Return only the maximum possible length of a stable contiguous subarray.

Constraints:
- 1 <= participants.length <= 200000
- 1 <= m <= 200000
- 0 <= participants[i] < m
- 0 <= muteCap[id] <= participants.length
- The sum of all values does not need to match the array length
"""

from typing import List


class Solution:
    def longest_stable_window(self, participants: List[int], muteCap: List[int]) -> int:
        """
        Find the length of the longest contiguous window such that each participant's
        frequency inside the window does not exceed that participant's allowed cap.

        Args:
            participants: List of participant IDs appearing second-by-second.
            muteCap: List where muteCap[id] is the maximum allowed count of that ID.

        Returns:
            The maximum length of a valid contiguous subarray.

        Time complexity:
            O(n), where n is len(participants).
            Each element is added to the window once and removed from the window once.

        Space complexity:
            O(m), where m is len(muteCap), for the frequency array.
        """
        # We use the classic sliding window / two-pointer technique.
        #
        # Core idea:
        # - Expand the right pointer one step at a time.
        # - Track how many times each participant appears in the current window.
        # - If adding a participant makes the window invalid, move the left pointer
        #   forward until the window becomes valid again.
        #
        # Why this works efficiently:
        # - Only one participant's count changes when we expand the window.
        # - Therefore, only that participant can newly violate its cap.
        # - While shrinking, we only need to keep shrinking until that violation is fixed.
        #
        # This avoids re-checking all participant IDs after every move, which would
        # be too slow for large inputs.

        n: int = len(participants)

        # Frequency array:
        # freq[x] = how many times participant x currently appears in the window.
        #
        # We use a list instead of a dictionary because participant IDs are guaranteed
        # to be in the range [0, m - 1], so direct indexing is fast and simple.
        freq: List[int] = [0] * len(muteCap)

        # Left boundary of the sliding window.
        left: int = 0

        # Best valid window length found so far.
        best: int = 0

        # Iterate with 'right' as the inclusive right boundary of the window.
        for right in range(n):
            current_id: int = participants[right]

            # Step 1: include participants[right] in the current window.
            freq[current_id] += 1

            # Step 2: if this participant now exceeds its allowed cap, the window is invalid.
            #
            # Important observation:
            # Before adding current_id, the window was valid.
            # After adding current_id, only current_id's count increased.
            # So only current_id can be the reason the window became invalid.
            #
            # Therefore, we only need to shrink until freq[current_id] <= muteCap[current_id].
            while freq[current_id] > muteCap[current_id]:
                left_id: int = participants[left]

                # Remove the leftmost participant from the window.
                freq[left_id] -= 1

                # Move the left boundary rightward.
                left += 1

            # Step 3: now the window [left, right] is valid again.
            # Update the best answer.
            window_length: int = right - left + 1
            if window_length > best:
                best = window_length

        return best

    def longestStableWindow(self, participants: List[int], muteCap: List[int]) -> int:
        """
        Compatibility wrapper using camelCase naming.

        Args:
            participants: List of participant IDs appearing second-by-second.
            muteCap: List where muteCap[id] is the maximum allowed count of that ID.

        Returns:
            The maximum length of a valid contiguous subarray.

        Time complexity:
            O(n), where n is len(participants).

        Space complexity:
            O(m), where m is len(muteCap).
        """
        return self.longest_stable_window(participants, muteCap)


if __name__ == "__main__":
    solution = Solution()

    # Example 1
    participants_1: List[int] = [1, 2, 1, 3, 2, 1, 2]
    mute_cap_1: List[int] = [0, 2, 2, 1]
    result_1: int = solution.longest_stable_window(participants_1, mute_cap_1)
    print(result_1)  # Expected: 5

    # Example 2
    participants_2: List[int] = [4, 4, 2, 4, 1, 2, 1, 1, 3]
    mute_cap_2: List[int] = [0, 1, 2, 2, 1]
    result_2: int = solution.longest_stable_window(participants_2, mute_cap_2)
    print(result_2)  # Expected: 4

    # Additional quick checks
    participants_3: List[int] = [0, 0, 0]
    mute_cap_3: List[int] = [0]
    result_3: int = solution.longest_stable_window(participants_3, mute_cap_3)
    print(result_3)  # Expected: 0

    participants_4: List[int] = [0, 1, 2, 3]
    mute_cap_4: List[int] = [1, 1, 1, 1]
    result_4: int = solution.longest_stable_window(participants_4, mute_cap_4)
    print(result_4)  # Expected: 4