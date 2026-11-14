"""
Title: Minimum Cost to Archive Photos with Album Switching Fee

Problem Description:
A cloud photo service needs to archive a sequence of photos in the exact order they
were taken. Each photo belongs to one of m visual categories, numbered from 1 to m.

You must place every photo into one of two archive albums, Album A or Album B,
while preserving the original order inside each album.

When a photo of category x is appended to an album, the service pays a storage
adjustment cost based on the previous category already stored in that same album.
If the album is empty, the cost is 0. Otherwise, if the previous category in that
album was y, the cost is switchCost[y][x].

Your task is to assign every photo to either Album A or Album B so that the total
cost is minimized.

More formally:
- You are given an array photos of length n, where photos[i] is the category of the
  i-th photo.
- You are also given an m x m matrix switchCost.
- Process photos from left to right.
- For each photo, choose Album A or Album B.
- The relative order of photos assigned to the same album must remain the same as in
  the original sequence.
- Return the minimum possible total adjustment cost.

Key DP idea:
Instead of tracking the full contents of both albums, we only need to know the most
recent photo placed into each album. Even better, after processing photos[0..i], the
most recently processed photo photos[i] must be the last photo in one of the two albums.
So we can define a DP state by:
- i: the index of the most recently processed photo
- j: the index of the last photo in the *other* album, or -1 if that album is empty

This reduces the state count to O(n^2), and each transition is O(1), giving an
overall O(n^2) solution.
"""

from typing import List


class Solution:
    def min_archive_cost(self, photos: List[int], switchCost: List[List[int]]) -> int:
        """
        Compute the minimum total adjustment cost to assign photos to two albums.

        We process photos from left to right. After processing up to index i, one album
        must end with photo i (because photo i is the most recently processed photo),
        and the other album ends with some earlier photo j, or is empty.

        Let dp[j] represent the minimum cost after processing photos[0..i-1], where:
        - one album ends with photo i-1
        - the other album ends with photo j
        - j == -1 means the other album is empty

        To implement this efficiently in Python arrays, we store j == -1 at position 0,
        and j >= 0 at position j + 1.

        Args:
            photos: List of photo categories, each in the range 1..m.
            switchCost: m x m matrix where switchCost[a-1][b-1] is the cost of appending
                category b after category a in the same album.

        Returns:
            The minimum possible total adjustment cost.

        Time complexity:
            O(n^2), where n is the number of photos.

        Space complexity:
            O(n), using rolling DP arrays.
        """
        n: int = len(photos)

        # If there are no photos or only one photo, no album ever receives a second photo,
        # so the total cost is automatically 0.
        if n <= 1:
            return 0

        # Convert categories from 1-based to 0-based so they match Python list indexing.
        # This makes switchCost access straightforward:
        # cost from category a to category b becomes switchCost[a][b].
        categories: List[int] = [value - 1 for value in photos]

        # We use a very large number as "infinity" for impossible/unreached states.
        inf: int = 10**30

        # Base case after processing only photo 0:
        #
        # One album contains photo 0.
        # The other album is empty.
        #
        # Cost is 0 because placing the first photo into an empty album costs nothing.
        #
        # dp_prev[k] stores the state after processing up to some i.
        # Index mapping:
        #   k == 0  -> other album is empty (j = -1)
        #   k >= 1  -> other album ends at photo index j = k - 1
        #
        # After processing photo 0, the only valid state is:
        #   current album ends at photo 0
        #   other album is empty
        dp_prev: List[int] = [inf] * (n + 1)
        dp_prev[0] = 0

        # We now process photos[1], photos[2], ..., photos[n-1].
        for i in range(1, n):
            # dp_curr will represent states after processing photos[0..i].
            # In every such state:
            # - one album ends at photo i
            # - the other album ends at some j in [-1, 0, 1, ..., i-1]
            dp_curr: List[int] = [inf] * (n + 1)

            # ------------------------------------------------------------
            # Transition type 1:
            # Put photo i into the SAME album that already ended with photo i-1.
            #
            # Before this move:
            #   - one album ends at i-1
            #   - the other album ends at j
            #
            # After this move:
            #   - that same album now ends at i
            #   - the other album still ends at j
            #
            # Additional cost:
            #   switchCost[photos[i-1]][photos[i]]
            #
            # This transition is valid for every previous state j.
            # ------------------------------------------------------------
            same_album_cost: int = switchCost[categories[i - 1]][categories[i]]

            for other_pos in range(i):
                # other_pos ranges over valid encoded positions for j:
                #   0 means j = -1
                #   1 means j = 0
                #   ...
                #   i-1 means j = i-2
                previous_cost: int = dp_prev[other_pos]
                if previous_cost == inf:
                    continue

                new_cost: int = previous_cost + same_album_cost
                if new_cost < dp_curr[other_pos]:
                    dp_curr[other_pos] = new_cost

            # ------------------------------------------------------------
            # Transition type 2:
            # Put photo i into the OTHER album.
            #
            # Before this move:
            #   - one album ends at i-1
            #   - the other album ends at j
            #
            # After this move:
            #   - the album that previously ended at j now receives photo i,
            #     so it ends at i
            #   - the first album still ends at i-1
            #
            # Therefore the new "other album end index" becomes i-1.
            #
            # Additional cost:
            #   - 0 if the other album was empty (j = -1)
            #   - switchCost[photos[j]][photos[i]] otherwise
            #
            # Among all previous j, we take the minimum resulting cost and store it
            # in dp_curr[i], because encoded position i corresponds to j = i-1.
            # ------------------------------------------------------------
            best_switch_to_other: int = inf

            for other_pos in range(i):
                previous_cost = dp_prev[other_pos]
                if previous_cost == inf:
                    continue

                if other_pos == 0:
                    # The other album is empty, so placing photo i there costs 0.
                    add_cost: int = 0
                else:
                    # Decode j from the array position.
                    j: int = other_pos - 1
                    add_cost = switchCost[categories[j]][categories[i]]

                candidate: int = previous_cost + add_cost
                if candidate < best_switch_to_other:
                    best_switch_to_other = candidate

            # Encoded position i corresponds to j = i - 1.
            dp_curr[i] = best_switch_to_other

            # Move to the next iteration.
            dp_prev = dp_curr

        # After processing all photos, the answer is the minimum cost among all valid
        # states for the final index n-1.
        #
        # Valid encoded positions are:
        #   0 -> other album empty
        #   1 -> other album ends at 0
        #   ...
        #   n-1 -> other album ends at n-2
        #
        # Position n is unused in the final iteration, but harmless to include in min().
        return min(dp_prev)

    def solve(self, photos: List[int], switchCost: List[List[int]]) -> int:
        """
        Wrapper method matching a typical interview/platform style API.

        Args:
            photos: List of photo categories.
            switchCost: Cost matrix for category transitions within the same album.

        Returns:
            Minimum total adjustment cost.

        Time complexity:
            O(n^2)

        Space complexity:
            O(n)
        """
        return self.min_archive_cost(photos, switchCost)


if __name__ == "__main__":
    solution = Solution()

    # Example 1
    photos1: List[int] = [1, 2, 1]
    switch_cost1: List[List[int]] = [
        [0, 5],
        [4, 0],
    ]
    result1: int = solution.solve(photos1, switch_cost1)
    print("Example 1 Result:", result1)  # Expected: 0

    # Example 2
    photos2: List[int] = [1, 3, 2, 3]
    switch_cost2: List[List[int]] = [
        [0, 7, 2],
        [6, 0, 5],
        [4, 1, 0],
    ]
    result2: int = solution.solve(photos2, switch_cost2)
    print("Example 2 Result:", result2)  # Expected: 1

    # Additional small sanity checks
    photos3: List[int] = [1]
    switch_cost3: List[List[int]] = [
        [0],
    ]
    result3: int = solution.solve(photos3, switch_cost3)
    print("Single Photo Result:", result3)  # Expected: 0

    photos4: List[int] = [1, 2]
    switch_cost4: List[List[int]] = [
        [0, 10],
        [3, 0],
    ]
    result4: int = solution.solve(photos4, switch_cost4)
    print("Two Photos Result:", result4)  # Expected: 0, put each in a different album