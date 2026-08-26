"""
Title: Count Equivalent Playlist Rotations
Difficulty: Medium
Topic: Hashing

Problem Description:
A music platform stores many playlists, where each playlist is represented as an array of
song IDs. Two playlists are considered equivalent if one can be obtained from the other by
a circular rotation. For example, [4, 7, 9, 4] and [9, 4, 4, 7] are equivalent because
rotating the first playlist by 2 positions gives the second. However, [4, 7, 9] and
[7, 9, 8] are not equivalent.

You are given a list of playlists, where each playlist may have a different length.
Count how many unordered pairs of playlists are equivalent under circular rotation.
Playlists of different lengths can never be equivalent.

Your task is to return the total number of equivalent pairs.

A straightforward pairwise comparison of all playlists is too slow for large inputs, so
you should design a solution that groups equivalent playlists efficiently using hashing or
a canonical representation.

Constraints:
- 1 <= playlists.length <= 100000
- 1 <= total number of song IDs across all playlists <= 200000
- 1 <= playlists[i].length <= 200000
- 0 <= song IDs <= 1000000000
- The sum of all playlist lengths does not exceed 200000

Example 1:
Input: playlists = [[1,2,3],[2,3,1],[3,1,2],[1,3,2],[5],[5]]
Output: 4
Explanation: The first three playlists are all rotations of each other, contributing
3 pairs. The two single-song playlists contribute 1 more pair. [1,3,2] is not equivalent
to the others.

Example 2:
Input: playlists = [[8,8,1],[8,1,8],[1,8,8],[2,2],[2,2],[2],[3,4,3]]
Output: 4
Explanation: The first three playlists form 3 equivalent pairs. The two playlists [2,2]
and [2,2] form 1 pair. The playlist [2] has different length, and [3,4,3] does not
match any other playlist.

Notes:
- Rotation preserves order cyclically; reversing a playlist does not count.
- Duplicate song IDs are allowed and must be handled correctly.
- An efficient approach is to compute a canonical signature for each playlist, such as
  its lexicographically smallest rotation, then count equal signatures with a hash map.
"""

from typing import Dict, List, Tuple


class Solution:
    def booth_min_rotation_index(self, arr: List[int]) -> int:
        """
        Compute the starting index of the lexicographically smallest rotation.

        This uses Booth's algorithm, which finds the minimum rotation in linear time.
        The algorithm works correctly even when duplicate values exist.

        Args:
            arr: The playlist represented as a list of song IDs.

        Returns:
            The starting index (0-based) of the lexicographically smallest rotation.

        Time complexity:
            O(n), where n is the length of arr.

        Space complexity:
            O(n), due to building arr + arr for circular comparison.
        """
        n: int = len(arr)

        # A playlist of length 1 has only one possible rotation.
        if n == 1:
            return 0

        # To simulate circular rotations using normal indexing, we concatenate the array
        # with itself. Then any rotation of length n appears as a contiguous subarray.
        doubled: List[int] = arr + arr

        # i and j are candidate starting positions for the minimum rotation.
        # k is the offset while comparing the two candidates.
        i: int = 0
        j: int = 1
        k: int = 0

        # We only need candidate starts within the first n positions.
        while i < n and j < n and k < n:
            a: int = doubled[i + k]
            b: int = doubled[j + k]

            if a == b:
                # If elements match, continue comparing the next offset.
                k += 1
                continue

            if a > b:
                # Rotation starting at i is worse than rotation starting at j.
                # Therefore, all starts from i to i + k are invalid candidates.
                i = i + k + 1
                if i <= j:
                    i = j + 1
            else:
                # Rotation starting at j is worse than rotation starting at i.
                # Therefore, all starts from j to j + k are invalid candidates.
                j = j + k + 1
                if j <= i:
                    j = i + 1

            # Reset comparison offset after eliminating candidates.
            k = 0

        # The smaller surviving candidate is the answer.
        return min(i, j)

    def canonical_rotation(self, arr: List[int]) -> Tuple[int, ...]:
        """
        Build a canonical representation for a playlist under circular rotation.

        The canonical form is the lexicographically smallest rotation of the playlist.
        Any two playlists that are rotations of each other will produce exactly the same
        canonical tuple, which makes grouping easy with a hash map.

        Args:
            arr: The playlist represented as a list of song IDs.

        Returns:
            A tuple representing the lexicographically smallest rotation.

        Time complexity:
            O(n), where n is the length of arr.

        Space complexity:
            O(n), for the returned tuple.
        """
        # Find where the smallest rotation begins.
        start: int = self.booth_min_rotation_index(arr)
        n: int = len(arr)

        # Construct the canonical rotation explicitly.
        # We use modulo indexing so the rotation wraps around correctly.
        canonical: Tuple[int, ...] = tuple(arr[(start + offset) % n] for offset in range(n))
        return canonical

    def countEquivalentPairs(self, playlists: List[List[int]]) -> int:
        """
        Count unordered pairs of playlists that are equivalent under circular rotation.

        The key idea:
        1. Convert each playlist into a canonical signature.
        2. Use a hash map to count how many times each signature appears.
        3. If a signature appears c times, it contributes c * (c - 1) // 2 pairs.

        Args:
            playlists: A list of playlists, where each playlist is a list of song IDs.

        Returns:
            The total number of unordered equivalent pairs.

        Time complexity:
            O(S), where S is the total number of song IDs across all playlists.
            This is because each playlist is processed in linear time in its own length,
            and the sum of lengths is bounded.

        Space complexity:
            O(S), for storing canonical signatures in the hash map.
        """
        # This dictionary maps:
        #   canonical playlist signature -> number of times we have seen it
        #
        # We use tuples as keys because:
        # - tuples are immutable
        # - tuples are hashable
        # - tuples preserve exact order and values
        signature_count: Dict[Tuple[int, ...], int] = {}

        # We will accumulate the answer incrementally.
        #
        # Why incremental counting is nice:
        # Suppose we have already seen a canonical signature x exactly c times.
        # When we see one more playlist with the same signature, it forms exactly c
        # new pairs with the previous c playlists.
        #
        # This avoids a second pass over the dictionary to compute combinations.
        total_pairs: int = 0

        # Process each playlist one by one.
        for playlist in playlists:
            # Convert the playlist into its canonical rotation.
            # All equivalent rotations collapse to the same tuple.
            signature: Tuple[int, ...] = self.canonical_rotation(playlist)

            # If we have seen this signature before count times, then the current
            # playlist forms 'count' new unordered pairs with those previous playlists.
            previous_count: int = signature_count.get(signature, 0)
            total_pairs += previous_count

            # Record that we have now seen one more playlist with this signature.
            signature_count[signature] = previous_count + 1

        return total_pairs


if __name__ == "__main__":
    solution = Solution()

    # Example 1:
    # Rotationally equivalent group:
    # [1,2,3], [2,3,1], [3,1,2] -> 3 pairs
    # [5], [5] -> 1 pair
    # Total = 4
    playlists1: List[List[int]] = [
        [1, 2, 3],
        [2, 3, 1],
        [3, 1, 2],
        [1, 3, 2],
        [5],
        [5],
    ]
    result1: int = solution.countEquivalentPairs(playlists1)
    print(result1)  # Expected: 4

    # Example 2:
    # [8,8,1], [8,1,8], [1,8,8] -> 3 pairs
    # [2,2], [2,2] -> 1 pair
    # [2] is different length from [2,2]
    # [3,4,3] matches none
    # Total = 4
    playlists2: List[List[int]] = [
        [8, 8, 1],
        [8, 1, 8],
        [1, 8, 8],
        [2, 2],
        [2, 2],
        [2],
        [3, 4, 3],
    ]
    result2: int = solution.countEquivalentPairs(playlists2)
    print(result2)  # Expected: 4

    # Additional quick sanity checks.
    playlists3: List[List[int]] = [
        [4, 7, 9, 4],
        [9, 4, 4, 7],   # rotation-equivalent to first
        [4, 4, 7, 9],   # also equivalent
        [7, 9, 4, 5],   # not equivalent
    ]
    result3: int = solution.countEquivalentPairs(playlists3)
    print(result3)  # Expected: 3

    playlists4: List[List[int]] = [
        [1],
        [1],
        [1],
        [2],
    ]
    result4: int = solution.countEquivalentPairs(playlists4)
    print(result4)  # Expected: 3