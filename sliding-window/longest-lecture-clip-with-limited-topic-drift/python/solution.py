"""
Title: Longest Lecture Clip With Limited Topic Drift

Problem Description:
You are given an array `topics` representing the topic label of each consecutive minute
in a recorded lecture. The lecture platform wants to extract the longest contiguous clip
that still feels focused. A clip is considered focused if it contains at most `k` topic
transitions, where a transition happens between two adjacent minutes `i - 1` and `i`
when `topics[i] != topics[i - 1]`.

Return the length of the longest contiguous subarray of `topics` that contains at most
`k` transitions.

This is not the same as limiting the number of distinct topic labels. For example, the
clip `[2, 2, 3, 3, 2]` has only 2 distinct labels, but it has 2 transitions:
`2 -> 3` and `3 -> 2`.

Your task is to design an efficient algorithm that scans the lecture once or nearly once,
since the input can be large.

Constraints:
- `1 <= topics.length <= 2 * 10^5`
- `1 <= topics[i] <= 10^9`
- `0 <= k < topics.length`

Example 1:
Input: `topics = [4, 4, 1, 1, 1, 3, 3, 4], k = 2`
Output: `7`

Example 2:
Input: `topics = [5, 6, 5, 6, 5], k = 1`
Output: `2`
"""

from typing import List


class Solution:
    def longest_focused_clip(self, topics: List[int], k: int) -> int:
        """
        Return the length of the longest contiguous subarray with at most k transitions.

        A transition is counted between adjacent positions inside the current window
        whenever their topic labels are different.

        Args:
            topics: List of topic labels for each minute of the lecture.
            k: Maximum allowed number of topic transitions inside the chosen subarray.

        Returns:
            The maximum valid subarray length.

        Time complexity:
            O(n), where n is the length of topics. Each pointer moves at most n times.

        Space complexity:
            O(1), excluding the input array.
        """
        # We use the classic sliding window pattern:
        #
        # - `left` marks the start of the current window.
        # - `right` expands the window one element at a time.
        # - `transitions` stores how many adjacent changes currently exist inside
        #   the window topics[left:right+1].
        #
        # Key observation:
        # A transition only exists between adjacent elements.
        # So when we expand the window to include `topics[right]`, the ONLY new
        # possible transition introduced is between `topics[right - 1]` and
        # `topics[right]`.
        #
        # Similarly, when we shrink the window from the left by moving `left`
        # forward, the ONLY transition that can disappear is the one between
        # `topics[left]` and `topics[left + 1]`.
        #
        # This lets us maintain the transition count in O(1) time per pointer move.

        n: int = len(topics)

        # Window start index.
        left: int = 0

        # Number of topic changes currently inside the window.
        transitions: int = 0

        # Best answer found so far.
        best: int = 0

        # Move `right` from left to right, expanding the window.
        for right in range(n):
            # If right > 0, then the newly added element topics[right] creates
            # one new adjacent pair with topics[right - 1].
            #
            # If those two values differ, then we have introduced exactly one
            # additional transition into the current window.
            if right > 0 and topics[right] != topics[right - 1]:
                transitions += 1

            # If the window now has too many transitions, we must shrink it
            # from the left until it becomes valid again.
            #
            # Why this works:
            # - The window only becomes invalid because transitions exceeded k.
            # - Moving `left` rightward removes elements from the front.
            # - Each move can remove at most one transition: the pair
            #   (topics[left], topics[left + 1]).
            while transitions > k:
                # Before incrementing `left`, check whether the adjacent pair
                # crossing out of the window contributes a transition.
                #
                # The pair is (topics[left], topics[left + 1]).
                # If they are different, then removing topics[left] from the
                # window also removes that transition from the count.
                if left < right and topics[left] != topics[left + 1]:
                    transitions -= 1

                # Actually shrink the window.
                left += 1

            # At this point, the window topics[left:right+1] is valid:
            # it contains at most k transitions.
            #
            # Compute its length and update the best answer.
            current_length: int = right - left + 1
            if current_length > best:
                best = current_length

        return best


if __name__ == "__main__":
    solution = Solution()

    # Example 1:
    # topics = [4, 4, 1, 1, 1, 3, 3, 4], k = 2
    # Longest valid subarray is [4, 4, 1, 1, 1, 3, 3] with transitions:
    # 4 -> 1, 1 -> 3  => total 2 transitions, length 7
    topics1: List[int] = [4, 4, 1, 1, 1, 3, 3, 4]
    k1: int = 2
    result1: int = solution.longest_focused_clip(topics1, k1)
    print(result1)  # Expected: 7

    # Example 2:
    # topics = [5, 6, 5, 6, 5], k = 1
    # Every adjacent pair changes, so any length-3 subarray has 2 transitions.
    # Therefore the longest valid length is 2.
    topics2: List[int] = [5, 6, 5, 6, 5]
    k2: int = 1
    result2: int = solution.longest_focused_clip(topics2, k2)
    print(result2)  # Expected: 2

    # Additional quick checks for beginners:
    topics3: List[int] = [7]
    k3: int = 0
    print(solution.longest_focused_clip(topics3, k3))  # Expected: 1

    topics4: List[int] = [1, 1, 1, 1]
    k4: int = 0
    print(solution.longest_focused_clip(topics4, k4))  # Expected: 4

    topics5: List[int] = [1, 2, 2, 3, 3, 3, 2]
    k5: int = 2
    print(solution.longest_focused_clip(topics5, k5))  # Expected: 6