"""
Title: Minimum Cost to Bundle Songs into Albums

Problem Description:
A music platform wants to package a sequence of songs into albums for physical release.
The songs must remain in their original order, and every song must belong to exactly one album.
For each song i, you are given its duration durations[i]. You are also given an integer
maxSongs, meaning an album can contain at most maxSongs consecutive songs.

The production cost of one album is defined as:
(max duration among songs in that album) * (number of songs in that album)

Your task is to split the full song list into one or more albums so that the total production
cost is minimized.

Return the minimum possible total cost.

This is an optimization problem over contiguous partitions of the array. A greedy choice does
not always work, because putting a long song into one album may increase that album's maximum
duration, but it could still reduce the total cost if it avoids creating another expensive
album later.

Constraints:
- 1 <= durations.length <= 2000
- 1 <= durations[i] <= 10^6
- 1 <= maxSongs <= durations.length
- Each album must contain at least 1 song and at most maxSongs songs
"""

from typing import List


class Solution:
    def min_album_cost(self, durations: List[int], maxSongs: int) -> int:
        """
        Compute the minimum total production cost to partition songs into valid albums.

        We use dynamic programming:
        - Let dp[i] be the minimum cost to package the first i songs.
        - To compute dp[i], we try every possible valid album ending at song i - 1
          with length from 1 to maxSongs (as long as it stays within bounds).
        - For each candidate album, we maintain the maximum duration in that album
          and compute:
              dp[start] + (max_duration_in_album * album_length)

        Args:
            durations: List of song durations in original order.
            maxSongs: Maximum number of consecutive songs allowed in one album.

        Returns:
            The minimum possible total production cost.

        Time complexity:
            O(n * maxSongs), where n is len(durations).

        Space complexity:
            O(n) for the dynamic programming array.
        """
        n: int = len(durations)

        # dp[i] means:
        # "the minimum cost needed to package the first i songs"
        #
        # Important indexing note:
        # - songs are stored in durations[0] to durations[n - 1]
        # - dp[0] = 0 means packaging zero songs costs nothing
        # - answer will be dp[n]
        #
        # We initialize all states to infinity because we want to minimize cost,
        # and infinity is a standard "worst possible" starting value for min-DP.
        dp: List[int] = [float("inf")] * (n + 1)
        dp[0] = 0

        # We build the answer from left to right.
        # For each i, we determine the best way to end the packaging of the first i songs.
        for i in range(1, n + 1):
            # This variable will track the maximum duration inside the current candidate album.
            #
            # We will consider albums that end at index i - 1.
            # We extend backward one song at a time:
            # - length = 1 means album is [i-1, i-1]
            # - length = 2 means album is [i-2, i-1]
            # - ...
            # up to maxSongs songs or until we run out of songs.
            current_max: int = 0

            # Try every valid album length that ends at song i - 1.
            # The album start index in 0-based song indexing is:
            #   start_index = i - length
            #
            # In dp terms:
            # - first "start_index" songs are already optimally packaged with cost dp[start_index]
            # - then songs from start_index to i - 1 form the last album
            for length in range(1, min(maxSongs, i) + 1):
                start_index: int = i - length

                # Update the maximum duration for the current candidate album.
                #
                # Since we are extending the album backward by one song at a time,
                # we can update the maximum incrementally instead of recomputing it
                # from scratch. This is a key optimization that keeps the inner work O(1)
                # per candidate length.
                current_max = max(current_max, durations[start_index])

                # Cost of the current last album:
                #   (maximum duration in this album) * (number of songs in this album)
                album_cost: int = current_max * length

                # Total cost if we choose this album as the final album for first i songs:
                # - dp[start_index] covers songs before this album
                # - album_cost covers this album
                candidate_total: int = dp[start_index] + album_cost

                # Keep the minimum among all valid choices.
                if candidate_total < dp[i]:
                    dp[i] = candidate_total

        return dp[n]


if __name__ == "__main__":
    solution = Solution()

    # Example 1
    durations1: List[int] = [3, 1, 4, 2]
    max_songs1: int = 2
    result1: int = solution.min_album_cost(durations1, max_songs1)
    print("Example 1 result:", result1)  # Expected: 10

    # Example 2
    durations2: List[int] = [5, 2, 2, 6, 3]
    max_songs2: int = 3
    result2: int = solution.min_album_cost(durations2, max_songs2)
    print("Example 2 result:", result2)  # Expected: 18

    # Additional quick checks
    durations3: List[int] = [7]
    max_songs3: int = 1
    result3: int = solution.min_album_cost(durations3, max_songs3)
    print("Single song result:", result3)  # Expected: 7

    durations4: List[int] = [2, 2, 2]
    max_songs4: int = 3
    result4: int = solution.min_album_cost(durations4, max_songs4)
    print("All equal durations result:", result4)  # Expected: 6

    durations5: List[int] = [1, 10, 1]
    max_songs5: int = 2
    result5: int = solution.min_album_cost(durations5, max_songs5)
    print("Mixed durations result:", result5)  # Expected: 12