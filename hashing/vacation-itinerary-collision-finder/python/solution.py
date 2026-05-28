```python
"""
Title: Vacation Itinerary Collision Finder
Difficulty: Medium
Topic: Hashing

Problem Description:
A travel company manages bookings for multiple tourists. Each tourist has an itinerary
represented as an ordered list of city codes (strings). Two tourists are said to have a
"collision" if they visit at least k consecutive cities in the exact same order at any
point in their respective itineraries.

Given a list of itineraries (each a list of city codes) and an integer k, return a list
of all unique pairs [i, j] (where i < j) such that tourist i and tourist j have a
collision. Return the pairs sorted in ascending order by i, then by j.

Constraints:
- 2 <= itineraries.length <= 200
- 1 <= itineraries[i].length <= 500
- 1 <= k <= 50
- Each city code is a non-empty string of uppercase letters with length between 1 and 5.
- The same city may appear multiple times in an itinerary.
"""

from typing import List, Dict, Set, Tuple


class Solution:
    def find_collisions(self, itineraries: List[List[str]], k: int) -> List[List[int]]:
        """
        Find all pairs of tourists who share at least k consecutive cities in the same order.

        The approach uses a hash map to store all k-length subsequences (windows) from each
        itinerary, then checks for overlaps between pairs of tourists.

        Args:
            itineraries: A list of itineraries, where each itinerary is a list of city codes.
            k: The minimum number of consecutive cities that must be shared for a collision.

        Returns:
            A sorted list of [i, j] pairs (i < j) where tourists i and j have a collision.

        Time Complexity:
            O(n^2 * m * k) in the worst case, where n is the number of itineraries,
            m is the maximum length of an itinerary, and k is the window size.
            More precisely, building the hash map is O(n * m * k) and checking pairs
            is O(n^2 * m) since set intersection is O(min(len(s1), len(s2))).

        Space Complexity:
            O(n * m * k) to store all the k-length window tuples for each itinerary.
        """

        # -----------------------------------------------------------------------
        # STEP 1: Handle edge case where k is larger than any itinerary.
        # If k is 0 or negative, every pair would collide trivially, but the
        # constraints say k >= 1, so we mainly guard against k being too large.
        # -----------------------------------------------------------------------
        n = len(itineraries)  # Total number of tourists/itineraries

        # -----------------------------------------------------------------------
        # STEP 2: Build a dictionary mapping each tourist index to the SET of all
        # k-length consecutive subsequences (windows) in their itinerary.
        #
        # Why a set? Because we only care about WHETHER a window exists in both
        # itineraries, not how many times. Using a set gives O(1) average lookup.
        #
        # Why convert to tuple? Lists are not hashable in Python, so we can't put
        # them in a set. Tuples are immutable and hashable.
        #
        # Example: itinerary = ["A","B","C","D"], k=2
        #   Windows: ("A","B"), ("B","C"), ("C","D")
        # -----------------------------------------------------------------------
        # windows_per_tourist[i] = set of all k-length window tuples for tourist i
        windows_per_tourist: List[Set[Tuple[str, ...]]] = []

        for i in range(n):
            itinerary = itineraries[i]
            window_set: Set[Tuple[str, ...]] = set()

            # Slide a window of size k across the itinerary.
            # The window starts at index 0 and ends at index len(itinerary) - k.
            # If the itinerary has fewer than k cities, no windows are generated,
            # meaning this tourist can never have a collision (window_set stays empty).
            for start in range(len(itinerary) - k + 1):
                # Extract the k-length window starting at 'start'
                # Convert to tuple so it's hashable and can be stored in a set
                window = tuple(itinerary[start: start + k])
                window_set.add(window)

            windows_per_tourist.append(window_set)

        # -----------------------------------------------------------------------
        # STEP 3: Check every unique pair (i, j) where i < j.
        # For each pair, check if the intersection of their window sets is non-empty.
        # If there's at least one common k-length window, they have a collision.
        #
        # Why iterate i < j? The problem asks for unique pairs with i < j,
        # so we avoid duplicates by only checking each pair once.
        # -----------------------------------------------------------------------
        result: List[List[int]] = []

        for i in range(n):
            for j in range(i + 1, n):
                # Check if tourist i and tourist j share any k-length window.
                # Set intersection (&) returns elements common to both sets.
                # If the intersection is non-empty, there's a collision.
                shared_windows = windows_per_tourist[i] & windows_per_tourist[j]

                if shared_windows:
                    # They share at least one k-length consecutive sequence → collision!
                    result.append([i, j])

        # -----------------------------------------------------------------------
        # STEP 4: Return the result.
        # The pairs are already in ascending order by i then j because of how
        # we iterate (outer loop i from 0..n-1, inner loop j from i+1..n-1).
        # -----------------------------------------------------------------------
        return result


# -------------------------------------------------------------------------------
# VERIFICATION / TRACING THROUGH EXAMPLES:
#
# Example 1:
#   itineraries = [["NYC","LAX","CHI","MIA"],["SEA","NYC","LAX","CHI"],["BOS","MIA","DFW"]]
#   k = 3
#
#   Tourist 0 windows (k=3):
#     ("NYC","LAX","CHI"), ("LAX","CHI","MIA")
#
#   Tourist 1 windows (k=3):
#     ("SEA","NYC","LAX"), ("NYC","LAX","CHI"), ("LAX","CHI","CHI") -- wait let me redo
#     itinerary = ["SEA","NYC","LAX","CHI"]
#     start=0: ("SEA","NYC","LAX")
#     start=1: ("NYC","LAX","CHI")
#
#   Tourist 2 windows (k=3):
#     itinerary = ["BOS","MIA","DFW"]
#     start=0: ("BOS","MIA","DFW")
#
#   Pair (0,1): intersection = {("NYC","LAX","CHI")} → non-empty → collision ✓
#   Pair (0,2): intersection = {} → no collision ✓
#   Pair (1,2): intersection = {} → no collision ✓
#
#   Result: [[0, 1]] ✓ Matches expected output!
#
# Example 2:
#   itineraries = [["A","B","C"],["B","C","D"],["A","B","C","D"]]
#   k = 2
#
#   Tourist 0 windows (k=2):
#     ("A","B"), ("B","C")
#
#   Tourist 1 windows (k=2):
#     ("B","C"), ("C","D")
#
#   Tourist 2 windows (k=2):
#     ("A","B"), ("B","C"), ("C","D")
#
#   Pair (0,1): intersection = {("B","C")} → collision ✓
#   Pair (0,2): intersection = {("A","B"), ("B","C")} → collision ✓
#   Pair (1,2): intersection = {("B","C"), ("C","D")} → collision ✓
#
#   Result: [[0,1],[0,2],[1,2]] ✓ Matches expected output!
# -------------------------------------------------------------------------------


if __name__ == "__main__":
    # Create a Solution instance
    sol = Solution()

    # ------------------------------------------------------------------
    # Example 1
    # ------------------------------------------------------------------
    print("=" * 60)
    print("Example 1:")
    itineraries_1 = [
        ["NYC", "LAX", "CHI", "MIA"],
        ["SEA", "NYC", "LAX", "CHI"],
        ["BOS", "MIA", "DFW"]
    ]
    k_1 = 3
    result_1 = sol.find_collisions(itineraries_1, k_1)
    print(f"  Input itineraries: {itineraries_1}")
    print(f"  k = {k_1}")
    print(f"  Output: {result_1}")
    print(f"  Expected: [[0, 1]]")
    print(f"  Correct: {result_1 == [[0, 1]]}")

    # ------------------------------------------------------------------
    # Example 2
    # ------------------------------------------------------------------
    print("=" * 60)
    print("Example 2:")
    itineraries_2 = [
        ["A", "B", "C"],
        ["B", "C", "D"],
        ["A", "B", "C", "D"]
    ]
    k_2 = 2
    result_2 = sol.find_collisions(itineraries_2, k_2)
    print(f"  Input itineraries: {itineraries_2}")
    print(f"  k = {k_2}")
    print(f"  Output: {result_2}")
    print(f"  Expected: [[0, 1], [0, 2], [1, 2]]")
    print(f"  Correct: {result_2 == [[0, 1], [0, 2], [1, 2]]}")

    # ------------------------------------------------------------------
    # Additional Edge Case: k larger than all itineraries → no collisions
    # ------------------------------------------------------------------
    print("=" * 60)
    print("Edge Case: k larger than all itineraries")
    itineraries_3 = [
        ["A", "B"],
        ["A", "B"]
    ]
    k_3 = 5  # Both itineraries have length 2, so no window of size 5 exists
    result_3 = sol.find_collisions(itineraries_3, k_3)
    print(f"  Input itineraries: {itineraries_3}")
    print(f"  k = {k_3}")
    print(f"  Output: {result_3}")
    print(f"  Expected: []")
    print(f"  Correct: {result_3 == []}")

    # ------------------------------------------------------------------
    # Additional Edge Case: k = 1, every pair sharing any city collides
    # ------------------------------------------------------------------
    print("=" * 60)
    print("Edge Case: k = 1")
    itineraries_4 = [
        ["A", "B"],
        ["B", "C"],
        ["D", "E"]
    ]
    k_4 = 1
    result_4 = sol.find_collisions(itineraries_4, k_4)
    print(f"  Input itineraries: {itineraries_4}")
    print(f"  k = {k_4}")
    print(f"  Output: {result_4}")
    # Tourist 0 has {A, B}, Tourist 1 has {B, C} → share B → collision
    # Tourist 0 has {A, B}, Tourist 2 has {D, E} → no shared city → no collision
    # Tourist 1 has {B, C}, Tourist 2 has {D, E} → no shared city → no collision
    print(f"  Expected: [[0, 1]]")
    print(f"  Correct: {result_4 == [[0, 1]]}")

    # ------------------------------------------------------------------
    # Additional Edge Case: Identical itineraries
    # ------------------------------------------------------------------
    print("=" * 60)
    print("Edge Case: Identical itineraries")
    itineraries_5 = [
        ["X", "Y", "Z"],
        ["X", "Y", "Z"],
        ["X", "Y", "Z"]
    ]
    k_5 = 3
    result_5 = sol.find_collisions(itineraries_5, k_5)
    print(f"  Input itineraries: {itineraries_5}")
    print(f"  k = {k_5}")
    print(f"  Output: {result_5}")
    print(f"  Expected: [[0, 1], [0, 2], [1, 2]]")
    print(f"  Correct: {result_5 == [[0, 1], [0, 2], [1, 2]]}")

    print("=" * 60)
    print("All tests completed!")
```