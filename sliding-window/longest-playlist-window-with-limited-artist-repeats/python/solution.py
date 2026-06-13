"""
Title: Longest Playlist Window With Limited Artist Repeats

Problem Description:
A music streaming service stores a listening session as an array `artists`, where
`artists[i]` is the artist ID of the `i`-th song played in order. To keep a
generated playlist feeling varied, the service wants to find the longest
contiguous block of songs such that no single artist appears more than `k` times
inside that block.

Your task is to return the length of the longest contiguous subarray of `artists`
satisfying this rule. In other words, among all windows `[l..r]`, find the
maximum window size where every distinct artist appears at most `k` times.

This is a realistic streaming constraint problem: duplicates are allowed, the
same artist may appear many times overall, and only contiguous segments count.
If `k = 0`, then no song can be included, so the answer is `0`.

Constraints:
- 1 <= artists.length <= 200000
- 1 <= artists[i] <= 1000000000
- 0 <= k <= artists.length

Example 1:
Input: artists = [4, 1, 4, 2, 4, 1, 3], k = 2
Output: 5
Explanation: The longest valid window is [1, 4, 2, 4, 1]. In this window,
artist 4 appears 2 times, artist 1 appears 2 times, and artist 2 appears 1 time.

Example 2:
Input: artists = [7, 7, 7, 2, 2, 3, 7], k = 1
Output: 3
Explanation: One longest valid window is [7, 2, 3]. Any longer window would
cause some artist to appear more than once.
"""

from typing import Dict, List


class Solution:
    def longest_playlist_window(self, artists: List[int], k: int) -> int:
        """
        Find the length of the longest contiguous subarray where every artist
        appears at most k times.

        Args:
            artists: List of artist IDs in listening order.
            k: Maximum allowed frequency of any single artist inside a valid window.

        Returns:
            The maximum length of a contiguous valid window.

        Time complexity:
            O(n), where n is the length of artists.
            Each element is added to the window once and removed at most once.

        Space complexity:
            O(m), where m is the number of distinct artists currently tracked
            in the frequency dictionary. In the worst case, O(n).
        """
        # Special case:
        # If k is 0, then no artist is allowed to appear even once.
        # That means no non-empty window can ever be valid, so the answer is 0.
        if k == 0:
            return 0

        # This dictionary stores how many times each artist appears
        # in the current sliding window [left, right].
        #
        # Example:
        # If the current window is [4, 1, 4, 2], then counts would be:
        # {4: 2, 1: 1, 2: 1}
        counts: Dict[int, int] = {}

        # `left` marks the start of the current window.
        # We will expand the window by moving `right` from left to right,
        # and shrink from the left whenever the window becomes invalid.
        left: int = 0

        # This stores the best valid window length found so far.
        best: int = 0

        # Iterate over every possible right boundary of the window.
        for right, artist in enumerate(artists):
            # Step 1: Include the new artist at position `right` into the window.
            #
            # We increase that artist's frequency in the dictionary.
            # If the artist was not seen before in the current window,
            # start its count at 0 and then add 1.
            counts[artist] = counts.get(artist, 0) + 1

            # Step 2: If adding this artist caused its count to exceed k,
            # then the current window is invalid.
            #
            # Important observation:
            # Before adding `artists[right]`, the window was valid.
            # After adding it, only this specific artist's count could have
            # become too large. No other artist's count changed.
            #
            # Therefore, we only need to shrink the window until
            # counts[artist] <= k again.
            while counts[artist] > k:
                # Identify the artist at the current left edge.
                left_artist: int = artists[left]

                # Remove that leftmost artist from the window by decreasing its count.
                counts[left_artist] -= 1

                # Move the left boundary one step to the right,
                # effectively shrinking the window.
                left += 1

            # Step 3: At this point, the window [left, right] is valid again.
            # Every artist appears at most k times.
            #
            # Compute its length and update the best answer if needed.
            current_length: int = right - left + 1
            if current_length > best:
                best = current_length

        # After processing all positions, `best` holds the maximum valid length.
        return best


if __name__ == "__main__":
    solution = Solution()

    # Example 1
    artists1: List[int] = [4, 1, 4, 2, 4, 1, 3]
    k1: int = 2
    result1: int = solution.longest_playlist_window(artists1, k1)
    print("Example 1:")
    print("artists =", artists1)
    print("k =", k1)
    print("Output =", result1)
    print("Expected = 5")
    print()

    # Example 2
    artists2: List[int] = [7, 7, 7, 2, 2, 3, 7]
    k2: int = 1
    result2: int = solution.longest_playlist_window(artists2, k2)
    print("Example 2:")
    print("artists =", artists2)
    print("k =", k2)
    print("Output =", result2)
    print("Expected = 3")
    print()

    # Additional edge case: k = 0
    artists3: List[int] = [1, 2, 3]
    k3: int = 0
    result3: int = solution.longest_playlist_window(artists3, k3)
    print("Edge Case:")
    print("artists =", artists3)
    print("k =", k3)
    print("Output =", result3)
    print("Expected = 0")