"""
Title: Minimum Cost to Assemble a Playlist with Genre Switch Fees

Problem Description:
A music app wants to build a playlist of exactly k songs from a catalog shown in a fixed order.
For each song i, you are given its duration duration[i], its enjoyment score enjoy[i], and its
genre genre[i]. You may either skip a song or include it in the playlist, but the relative order
of chosen songs must remain the same as in the catalog.

The total cost of a playlist is defined as follows:
1. For every selected song, you pay its duration.
2. For every pair of consecutive selected songs with different genres, you pay an additional
   switch fee s.
3. You receive a discount equal to the sum of enjoyment scores of all selected songs.

So the final playlist cost is:
(sum of durations) + (number of genre changes between consecutive chosen songs) * s
- (sum of enjoyment scores)

Your task is to return the minimum possible total cost of selecting exactly k songs.
If it is impossible to choose exactly k songs, return -1.

Constraints:
- 1 <= n <= 200
- 1 <= k <= n
- 1 <= duration[i] <= 10^4
- 0 <= enjoy[i] <= 10^4
- 1 <= genre[i] <= 50
- 0 <= s <= 10^4
"""

from typing import List


class Solution:
    def min_playlist_cost(
        self,
        duration: List[int],
        enjoy: List[int],
        genre: List[int],
        k: int,
        s: int,
    ) -> int:
        """
        Compute the minimum total cost of selecting exactly k songs in original order.

        The key observation is that each chosen song contributes a base value:
            duration[i] - enjoy[i]
        and each time the genre changes between two consecutive chosen songs,
        we add the switch fee s.

        We use dynamic programming where:
        - dp_prev[c][g] = minimum cost after processing some prefix of songs,
          choosing exactly c songs, and the last chosen song has genre g.
        - We also maintain best_prev[c] = minimum over all genres of dp_prev[c][g].
        - And best_genre_prev[c][g] = dp_prev[c][g] for direct same-genre extension.

        Transition when considering a new song with genre cur_g and base cost base:
        - Skip it: existing states remain unchanged.
        - Take it as the first chosen song:
              new cost = base
        - Extend a previous playlist of length c-1:
              * if previous last genre is cur_g: no switch fee
              * otherwise: add switch fee s

        To make "different genre" transitions efficient, for each count c-1 we use:
            min(
                dp_prev[c-1][cur_g],              # same genre, no fee
                min over all genres h != cur_g of dp_prev[c-1][h] + s
            )
        This can be computed from the global best and whether the best genre equals cur_g.

        Args:
            duration: List of song durations.
            enjoy: List of song enjoyment scores.
            genre: List of song genres.
            k: Exact number of songs to choose.
            s: Genre switch fee.

        Returns:
            The minimum possible total cost, or -1 if impossible.

        Time complexity:
            O(n * k * G), where G is the number of possible genres (here at most 50).

        Space complexity:
            O(k * G)
        """
        n: int = len(duration)
        if k > n:
            return -1

        max_genre: int = max(genre) if genre else 0
        inf: int = 10**18

        # dp[c][g] means:
        # minimum cost to choose exactly c songs so far,
        # with the last chosen song having genre g.
        #
        # We use 1-based indexing for genres because the problem states genre >= 1.
        dp_prev: List[List[int]] = [[inf] * (max_genre + 1) for _ in range(k + 1)]

        # Process songs one by one in the given fixed order.
        for i in range(n):
            cur_genre: int = genre[i]
            base_cost: int = duration[i] - enjoy[i]

            # Start from a copy of previous states to naturally support "skip this song".
            # If we do not choose the current song, all old states remain valid.
            dp_next: List[List[int]] = [row[:] for row in dp_prev]

            # Case 1: choose current song as the very first selected song.
            # Then there is no previous song, so no switch fee applies.
            if base_cost < dp_next[1][cur_genre]:
                dp_next[1][cur_genre] = base_cost

            # For efficient transitions, precompute for each chosen count c:
            # - the smallest value among all ending genres
            # - the second smallest value among all ending genres
            # - which genre achieved the smallest value
            #
            # Why do we need both smallest and second smallest?
            # Because when we want the best previous state with a genre DIFFERENT
            # from cur_genre, if the global minimum already ends with cur_genre,
            # we must use the second minimum instead.
            best1_val: List[int] = [inf] * (k + 1)
            best1_genre: List[int] = [-1] * (k + 1)
            best2_val: List[int] = [inf] * (k + 1)

            for chosen in range(1, k + 1):
                for g in range(1, max_genre + 1):
                    val: int = dp_prev[chosen][g]
                    if val < best1_val[chosen]:
                        best2_val[chosen] = best1_val[chosen]
                        best1_val[chosen] = val
                        best1_genre[chosen] = g
                    elif val < best2_val[chosen]:
                        best2_val[chosen] = val

            # Case 2: choose current song as the c-th selected song, where c >= 2.
            # We extend a valid playlist of length c-1.
            upper_count: int = min(k, i + 1)
            for chosen in range(2, upper_count + 1):
                prev_count: int = chosen - 1

                # Option A: previous chosen song has the same genre as current song.
                # Then no switch fee is added.
                same_genre_cost: int = dp_prev[prev_count][cur_genre]

                # Option B: previous chosen song has a different genre.
                # Then we add the switch fee s.
                if best1_genre[prev_count] != cur_genre:
                    different_genre_best: int = best1_val[prev_count]
                else:
                    different_genre_best = best2_val[prev_count]

                best_extend: int = same_genre_cost
                if different_genre_best < inf:
                    best_extend = min(best_extend, different_genre_best + s)

                # If there exists any valid previous playlist of length prev_count,
                # we can append the current song and update the state.
                if best_extend < inf:
                    candidate: int = best_extend + base_cost
                    if candidate < dp_next[chosen][cur_genre]:
                        dp_next[chosen][cur_genre] = candidate

            # Move to the next song.
            dp_prev = dp_next

        # Final answer: among all playlists of exactly k songs,
        # take the minimum cost regardless of ending genre.
        answer: int = min(dp_prev[k]) if k >= 0 else inf
        return -1 if answer >= inf else answer


if __name__ == "__main__":
    solution = Solution()

    duration1 = [4, 6, 3, 5]
    enjoy1 = [2, 5, 1, 4]
    genre1 = [1, 2, 2, 1]
    k1 = 2
    s1 = 3
    result1 = solution.min_playlist_cost(duration1, enjoy1, genre1, k1, s1)
    print(result1)

    duration2 = [7, 2, 6, 3, 4]
    enjoy2 = [1, 3, 2, 5, 1]
    genre2 = [1, 1, 3, 3, 2]
    k2 = 3
    s2 = 4
    result2 = solution.min_playlist_cost(duration2, enjoy2, genre2, k2, s2)
    print(result2)