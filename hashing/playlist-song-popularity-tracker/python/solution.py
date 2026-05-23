"""
Playlist Song Popularity Tracker
=================================

Problem Description:
You are building a music streaming app. Users submit a log of song plays,
where each entry is a string representing the song title. Your task is to
determine which songs were played more than once and return them sorted by
their play count in descending order. If two songs have the same play count,
sort them alphabetically (ascending).

Given a list of strings `plays` where each element is the title of a song
that was played, return a list of song titles that appear more than once,
sorted as described above.

Constraints:
- 1 <= plays.length <= 10^4
- 1 <= plays[i].length <= 50
- plays[i] consists of lowercase English letters and spaces.
- Song titles are case-sensitive.

Example 1:
    Input: plays = ["shape of you", "blinding lights", "shape of you",
                    "blinding lights", "shape of you", "levitating"]
    Output: ["shape of you", "blinding lights"]
    Explanation: "shape of you" played 3 times, "blinding lights" played 2 times.
                 "levitating" played only once so it is excluded.

Example 2:
    Input: plays = ["song a", "song b", "song c", "song b", "song a",
                    "song d", "song d"]
    Output: ["song a", "song b", "song d"]
    Explanation: All three songs played exactly 2 times, sorted alphabetically.
"""

from collections import Counter
from typing import Dict, List


class Solution:
    def get_popular_songs(self, plays: List[str]) -> List[str]:
        """
        Determine which songs were played more than once, sorted by play count
        descending; ties broken alphabetically ascending.

        Args:
            plays (List[str]): A list of song title strings representing the
                               play log (may contain duplicates).

        Returns:
            List[str]: Song titles that appear more than once, sorted by
                       descending play count, then ascending alphabetically
                       for ties.

        Time Complexity:
            O(n + k log k), where n = len(plays) and k = number of unique
            songs played more than once.
            - Building the frequency map: O(n)
            - Filtering songs with count > 1: O(unique songs) ⊆ O(n)
            - Sorting k songs: O(k log k)

        Space Complexity:
            O(u), where u = number of unique songs in `plays`.
            - The Counter (hash map) stores one entry per unique song title.
        """

        # ---------------------------------------------------------------
        # STEP 1: Count how many times each song was played
        # ---------------------------------------------------------------
        # We use collections.Counter, which is a specialised dictionary
        # (hash map) that maps each unique element to its frequency.
        #
        # Why a hash map?  Lookup and insertion are O(1) on average, so
        # counting all n plays takes O(n) total — much better than sorting
        # the raw list first (O(n log n)).
        #
        # After this line, play_counts looks like:
        #   {"shape of you": 3, "blinding lights": 2, "levitating": 1}
        # for Example 1.
        play_counts: Dict[str, int] = Counter(plays)

        # ---------------------------------------------------------------
        # STEP 2: Keep only songs that were played MORE than once
        # ---------------------------------------------------------------
        # We iterate over every (song, count) pair in the Counter and
        # discard any song whose count is exactly 1 (played only once).
        #
        # List comprehension is used here for clarity and conciseness.
        # The result is a plain list of song title strings.
        #
        # For Example 1 this produces: ["shape of you", "blinding lights"]
        # For Example 2 this produces: ["song a", "song b", "song d"]
        # (order at this stage is arbitrary — we sort in the next step)
        popular_songs: List[str] = [
            song for song, count in play_counts.items() if count > 1
        ]

        # ---------------------------------------------------------------
        # STEP 3: Sort the filtered songs with a two-key sort
        # ---------------------------------------------------------------
        # We need:
        #   Primary key   → play count, DESCENDING  (higher count first)
        #   Secondary key → song title, ASCENDING   (alphabetical for ties)
        #
        # Python's built-in sort is stable and accepts a `key` function
        # that returns a tuple.  Tuples are compared element-by-element,
        # so (primary, secondary) gives us exactly the ordering we want.
        #
        # To sort play count in DESCENDING order we negate it:
        #   -play_counts[song]  →  a larger count becomes a more-negative
        #                          number, which sorts earlier (smaller).
        #
        # The song title itself is left as-is for ASCENDING alphabetical
        # order (Python compares strings lexicographically by default).
        #
        # Trace for Example 1:
        #   "shape of you"    → key = (-3, "shape of you")
        #   "blinding lights" → key = (-2, "blinding lights")
        #   Sorted: [(-3, ...), (-2, ...)]  →  ["shape of you", "blinding lights"] ✓
        #
        # Trace for Example 2:
        #   "song a" → key = (-2, "song a")
        #   "song b" → key = (-2, "song b")
        #   "song d" → key = (-2, "song d")
        #   All counts equal, so secondary (alphabetical) key decides:
        #   "song a" < "song b" < "song d"  →  ["song a", "song b", "song d"] ✓
        popular_songs.sort(key=lambda song: (-play_counts[song], song))

        # ---------------------------------------------------------------
        # STEP 4: Return the sorted result
        # ---------------------------------------------------------------
        return popular_songs


# -------------------------------------------------------------------
# Entry point — demonstrates the solution with the provided examples
# -------------------------------------------------------------------
if __name__ == "__main__":
    solver = Solution()

    # ------ Example 1 ------
    plays_1: List[str] = [
        "shape of you",
        "blinding lights",
        "shape of you",
        "blinding lights",
        "shape of you",
        "levitating",
    ]
    result_1 = solver.get_popular_songs(plays_1)
    print("Example 1")
    print(f"  Input : {plays_1}")
    print(f"  Output: {result_1}")
    print(f"  Expected: ['shape of you', 'blinding lights']")
    print(f"  Correct : {result_1 == ['shape of you', 'blinding lights']}")
    print()

    # ------ Example 2 ------
    plays_2: List[str] = [
        "song a",
        "song b",
        "song c",
        "song b",
        "song a",
        "song d",
        "song d",
    ]
    result_2 = solver.get_popular_songs(plays_2)
    print("Example 2")
    print(f"  Input : {plays_2}")
    print(f"  Output: {result_2}")
    print(f"  Expected: ['song a', 'song b', 'song d']")
    print(f"  Correct : {result_2 == ['song a', 'song b', 'song d']}")
    print()

    # ------ Edge case: all songs played only once ------
    plays_3: List[str] = ["alpha", "beta", "gamma"]
    result_3 = solver.get_popular_songs(plays_3)
    print("Edge Case — all songs played once")
    print(f"  Input : {plays_3}")
    print(f"  Output: {result_3}")
    print(f"  Expected: []")
    print(f"  Correct : {result_3 == []}")
    print()

    # ------ Edge case: single song repeated many times ------
    plays_4: List[str] = ["hit song"] * 100
    result_4 = solver.get_popular_songs(plays_4)
    print("Edge Case — single song repeated 100 times")
    print(f"  Input : ['hit song'] * 100")
    print(f"  Output: {result_4}")
    print(f"  Expected: ['hit song']")
    print(f"  Correct : {result_4 == ['hit song']}")