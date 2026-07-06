"""
Title: Longest Transcript Stretch With Limited Speaker Interruptions

Problem Description:
You are given a conversation transcript represented by an array `speakers`, where
`speakers[i]` is the ID of the person speaking at second `i`.

A continuous segment of the transcript is considered smooth if the number of speaker
interruptions inside that segment is at most `k`.

An interruption occurs whenever two adjacent seconds in the segment are spoken by
different people.

Example:
In the segment [2, 2, 5, 5, 5, 2], there are 2 interruptions:
- between the second and third elements: 2 -> 5
- between the fifth and sixth elements: 5 -> 2

Task:
Return the length of the longest smooth contiguous segment.

Constraints:
- 1 <= speakers.length <= 200000
- 1 <= speakers[i] <= 1000000000
- 0 <= k < speakers.length

Examples:
1)
Input: speakers = [1, 1, 2, 2, 2, 3, 3], k = 1
Output: 5

2)
Input: speakers = [4, 7, 7, 4, 4, 4, 9], k = 2
Output: 6
"""

from typing import List


class Solution:
    def longest_smooth_segment(self, speakers: List[int], k: int) -> int:
        """
        Find the length of the longest contiguous segment with at most k interruptions.

        An interruption is counted whenever two neighboring elements inside the current
        segment are different.

        Args:
            speakers: List of speaker IDs, where speakers[i] is the speaker at second i.
            k: Maximum number of allowed interruptions inside the chosen segment.

        Returns:
            The maximum length of a contiguous segment whose number of interruptions
            is at most k.

        Time complexity:
            O(n), where n is the length of speakers.
            Each pointer (left and right) moves at most n times.

        Space complexity:
            O(1), because only a few integer variables are used.
        """
        # The sliding window will represent the current candidate segment [left, right].
        # We expand the window by moving `right` one step at a time.
        #
        # Key observation:
        # The number of interruptions inside a window [left, right] depends only on
        # adjacent pairs within that window:
        #   (left, left+1), (left+1, left+2), ..., (right-1, right)
        #
        # So when we extend the window to include a new element at index `right`,
        # the only new adjacent pair introduced is (right-1, right).
        # If speakers[right-1] != speakers[right], interruptions increase by 1.
        #
        # Similarly, when we shrink the window from the left by moving `left` forward,
        # the adjacent pair (left, left+1) leaves the window.
        # If speakers[left] != speakers[left+1], interruptions decrease by 1.
        #
        # This makes sliding window perfect here:
        # - expand right
        # - if interruptions exceed k, move left until valid again
        # - track the largest valid window length

        n: int = len(speakers)

        # Left boundary of the current sliding window.
        left: int = 0

        # Number of interruptions currently inside the window [left, right].
        interruptions: int = 0

        # Best answer found so far.
        best_length: int = 1

        # Iterate over every possible right boundary.
        for right in range(n):
            # If right > 0, then adding speakers[right] creates exactly one new
            # adjacent pair: (right - 1, right).
            #
            # If those two speakers are different, that means we introduced one
            # additional interruption into the current window.
            if right > 0 and speakers[right] != speakers[right - 1]:
                interruptions += 1

            # If the current window has too many interruptions, it is invalid.
            # We must move `left` to the right until the window becomes valid again.
            #
            # Why this works:
            # - The window only becomes invalid when interruptions > k.
            # - Moving left forward removes one adjacent pair at a time:
            #   specifically the pair (left, left+1).
            # - If that removed pair was a speaker change, interruptions decreases.
            while interruptions > k and left < right:
                # Before incrementing left, check whether the pair that is about
                # to leave the window contributes an interruption.
                #
                # The pair (left, left+1) is inside the current window as long as
                # left < right. Once we move left forward, that pair is no longer
                # part of the window.
                if speakers[left] != speakers[left + 1]:
                    interruptions -= 1

                left += 1

            # At this point, the window [left, right] is guaranteed valid:
            # interruptions <= k
            current_length: int = right - left + 1

            # Update the best answer if this valid window is larger.
            if current_length > best_length:
                best_length = current_length

        return best_length

    def longestSmoothSegment(self, speakers: List[int], k: int) -> int:
        """
        Compatibility wrapper using camelCase naming.

        Args:
            speakers: List of speaker IDs.
            k: Maximum allowed interruptions.

        Returns:
            Length of the longest valid contiguous segment.

        Time complexity:
            O(n)

        Space complexity:
            O(1)
        """
        return self.longest_smooth_segment(speakers, k)


if __name__ == "__main__":
    solution = Solution()

    # Example 1
    speakers1: List[int] = [1, 1, 2, 2, 2, 3, 3]
    k1: int = 1
    result1: int = solution.longest_smooth_segment(speakers1, k1)
    print("Example 1:")
    print("speakers =", speakers1)
    print("k =", k1)
    print("Output =", result1)
    print("Expected = 5")
    print()

    # Example 2
    speakers2: List[int] = [4, 7, 7, 4, 4, 4, 9]
    k2: int = 2
    result2: int = solution.longest_smooth_segment(speakers2, k2)
    print("Example 2:")
    print("speakers =", speakers2)
    print("k =", k2)
    print("Output =", result2)
    print("Expected = 6")
    print()

    # Additional simple checks
    speakers3: List[int] = [5]
    k3: int = 0
    result3: int = solution.longest_smooth_segment(speakers3, k3)
    print("Additional Test 1:")
    print("speakers =", speakers3)
    print("k =", k3)
    print("Output =", result3)
    print("Expected = 1")
    print()

    speakers4: List[int] = [1, 2, 3, 4]
    k4: int = 0
    result4: int = solution.longest_smooth_segment(speakers4, k4)
    print("Additional Test 2:")
    print("speakers =", speakers4)
    print("k =", k4)
    print("Output =", result4)
    print("Expected = 1")
    print()

    speakers5: List[int] = [2, 2, 5, 5, 5, 2]
    k5: int = 2
    result5: int = solution.longest_smooth_segment(speakers5, k5)
    print("Additional Test 3:")
    print("speakers =", speakers5)
    print("k =", k5)
    print("Output =", result5)
    print("Expected = 6")