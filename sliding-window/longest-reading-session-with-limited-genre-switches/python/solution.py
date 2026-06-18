"""
Title: Longest Reading Session With Limited Genre Switches

Problem Description:
You are given an array genres where genres[i] is the genre ID of the i-th article a user reads
in chronological order. A reading session is defined as any contiguous segment of this array.
Product analysts want to find the longest session that feels focused, so they allow at most k
genre switches inside the session.

A genre switch occurs between two adjacent articles when their genre IDs are different.
For example, in the session [2, 2, 5, 5, 3], there are 2 genre switches:
one from 2 to 5 and one from 5 to 3.

Return the length of the longest contiguous reading session that contains at most k genre switches.

This is not the same as limiting the number of distinct genres. A session may contain many
articles of the same genre grouped together, and only changes between neighboring articles
count toward the switch total.

Constraints:
- 1 <= genres.length <= 200000
- 1 <= genres[i] <= 1000000000
- 0 <= k < genres.length

Examples:
1) genres = [1, 1, 2, 2, 2, 3, 3], k = 1
   Output: 5

2) genres = [4, 7, 7, 4, 4, 9, 9, 9, 4], k = 2
   Output: 7

Expected approach:
- O(n) sliding window
"""


from typing import List


class Solution:
    def longest_reading_session(self, genres: List[int], k: int) -> int:
        """
        Find the maximum length of a contiguous subarray with at most k genre switches.

        A genre switch is counted only between neighboring elements inside the current window.
        So for a window [left, ..., right], the number of switches equals the count of indices i
        such that left < i <= right and genres[i] != genres[i - 1].

        Args:
            genres: List of genre IDs in chronological reading order.
            k: Maximum allowed number of genre switches.

        Returns:
            The length of the longest valid contiguous reading session.

        Time complexity:
            O(n), where n is the length of genres.
            Each pointer (left and right) moves at most n times.

        Space complexity:
            O(1), ignoring input storage.
        """
        n: int = len(genres)

        # A single article always forms a valid session with 0 switches.
        # Since constraints guarantee n >= 1, we can safely initialize the best answer to 1.
        best_length: int = 1

        # "left" is the start index of our sliding window.
        # "right" will expand the window one step at a time.
        left: int = 0

        # This variable stores the number of genre switches currently inside the window [left, right].
        #
        # Important detail:
        # A switch is associated with a boundary between adjacent positions:
        # boundary i means the pair (i - 1, i), for i in [1, n - 1].
        #
        # For a window [left, right], the switch count is the number of boundaries i such that:
        #   left < i <= right
        # and genres[i] != genres[i - 1].
        #
        # We maintain this count incrementally as the window grows/shrinks.
        switches_in_window: int = 0

        # We start "right" from 0 and expand to the end.
        for right in range(n):
            # When we extend the window to include genres[right], a NEW adjacent boundary appears
            # only if right > 0. That new boundary is between positions (right - 1) and right.
            #
            # If the genres differ across that boundary, then we have introduced one additional
            # genre switch into the current window.
            if right > 0 and genres[right] != genres[right - 1]:
                switches_in_window += 1

            # If the window now has too many switches, we must move "left" rightward until the
            # window becomes valid again.
            #
            # Why does moving left help?
            # Because removing elements from the left can remove the leftmost boundary from the window.
            #
            # Specifically, when we increment left from L to L + 1, the boundary that may leave
            # the window is the one between L and L + 1.
            #
            # If genres[L] != genres[L + 1], that boundary was contributing exactly one switch
            # to the old window [L, right], but it is NOT part of the new window [L + 1, right].
            # So we subtract 1 in that case.
            while switches_in_window > k:
                # Before moving left forward, check whether the boundary (left, left + 1)
                # is a switch. If yes, that switch will disappear from the window after left moves.
                if left + 1 <= right and genres[left] != genres[left + 1]:
                    switches_in_window -= 1

                left += 1

            # At this point, the window [left, right] is guaranteed valid:
            # it contains at most k genre switches.
            current_length: int = right - left + 1

            # Update the best answer if this valid window is longer than any seen before.
            if current_length > best_length:
                best_length = current_length

        return best_length

    def longestReadingSession(self, genres: List[int], k: int) -> int:
        """
        Compatibility wrapper using camelCase naming.

        Args:
            genres: List of genre IDs in chronological reading order.
            k: Maximum allowed number of genre switches.

        Returns:
            The length of the longest valid contiguous reading session.

        Time complexity:
            O(n), where n is the length of genres.

        Space complexity:
            O(1), ignoring input storage.
        """
        return self.longest_reading_session(genres, k)


if __name__ == "__main__":
    solution = Solution()

    # Example 1
    genres1: List[int] = [1, 1, 2, 2, 2, 3, 3]
    k1: int = 1
    result1: int = solution.longest_reading_session(genres1, k1)
    print("Example 1:")
    print(f"genres = {genres1}, k = {k1}")
    print(f"Output: {result1}")
    print("Expected: 5")
    print()

    # Example 2
    genres2: List[int] = [4, 7, 7, 4, 4, 9, 9, 9, 4]
    k2: int = 2
    result2: int = solution.longest_reading_session(genres2, k2)
    print("Example 2:")
    print(f"genres = {genres2}, k = {k2}")
    print(f"Output: {result2}")
    print("Expected: 7")
    print()

    # Additional quick checks for beginner understanding

    # If k = 0, we are looking for the longest block of equal values.
    genres3: List[int] = [5, 5, 5, 2, 2, 8, 8, 8, 8, 1]
    k3: int = 0
    result3: int = solution.longest_reading_session(genres3, k3)
    print("Additional Check 1:")
    print(f"genres = {genres3}, k = {k3}")
    print(f"Output: {result3}")
    print("Expected: 4")
    print()

    # Single element array always has answer 1.
    genres4: List[int] = [42]
    k4: int = 0
    result4: int = solution.longest_reading_session(genres4, k4)
    print("Additional Check 2:")
    print(f"genres = {genres4}, k = {k4}")
    print(f"Output: {result4}")
    print("Expected: 1")