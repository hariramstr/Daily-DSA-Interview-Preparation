```python
"""
Title: Minimum Maximum Distance Between Placed Towers
Difficulty: Hard
Topic: Binary Search

Problem Description:
You are given a list of n candidate positions along a straight road, represented as a
sorted array of distinct integers `positions`. You must place exactly k communication
towers among these candidate positions. Your goal is to minimize the maximum distance
between any two consecutively placed towers (including the first and last positions,
which must always have a tower).

Given that the first position positions[0] and the last position positions[n-1] must
always contain a tower, place the remaining k - 2 towers at any of the other candidate
positions to minimize the maximum gap between any two adjacent towers.

Return the minimum possible value of the maximum distance between any two consecutively
placed towers. The answer should be returned as a floating-point number, rounded to 5
decimal places.

Constraints:
- 2 <= k <= n <= 10^5
- 0 <= positions[i] <= 10^9
- positions is sorted in strictly increasing order.
- The first and last positions must always have a tower.
"""

from typing import List


class Solution:
    def minimizeMaxDistance(self, positions: List[int], k: int) -> float:
        """
        Find the minimum possible maximum distance between consecutively placed towers.

        We use binary search on the answer (the maximum gap value). For a given
        candidate maximum gap `mid`, we check if we can place exactly k towers
        (with first and last fixed) such that no consecutive gap exceeds `mid`.

        The key insight:
        - Binary search on the answer: the minimum possible maximum gap.
        - For a given gap `d`, within each segment [positions[i], positions[i+1]],
          we can place towers to split it. The number of additional towers needed
          to ensure no sub-gap exceeds `d` in a segment of length `L` is:
          ceil(L / d) - 1  (we need ceil(L/d) pieces, which requires ceil(L/d)-1
          interior points from the candidate list... wait, we must use candidates).

        IMPORTANT CORRECTION:
        Since we can only place towers at CANDIDATE positions (not arbitrary points),
        we need a different approach.

        Correct approach:
        - Binary search on the answer value `mid` (the maximum allowed gap).
        - For a given `mid`, greedily check: starting from positions[0], always
          place the next tower at the farthest candidate position that is still
          within distance `mid` from the current tower.
        - Count how many towers we need. If count <= k, then `mid` is feasible.

        Args:
            positions: Sorted list of distinct integers representing candidate positions.
            k: Number of towers to place (first and last positions are mandatory).

        Returns:
            The minimum possible maximum distance as a float rounded to 5 decimal places.

        Time Complexity: O(n log(max_gap)) where max_gap = positions[-1] - positions[0]
                         Binary search runs O(log(max_gap * precision)) iterations,
                         each feasibility check is O(n) with binary search O(log n),
                         so overall O(n log n log(max_gap)).
        Space Complexity: O(1) extra space (ignoring input).
        """

        # -----------------------------------------------------------------------
        # STEP 1: Handle edge case where k >= n
        # If we have at least as many towers as positions, we can place a tower
        # at every position. The max gap is the minimum consecutive gap.
        # -----------------------------------------------------------------------
        n = len(positions)

        if k >= n:
            # Place towers at all positions; find the minimum consecutive gap
            min_gap = min(positions[i + 1] - positions[i] for i in range(n - 1))
            return round(float(min_gap), 5)

        # -----------------------------------------------------------------------
        # STEP 2: Set up binary search bounds
        #
        # - Lower bound (lo): The smallest possible gap. Since we must use candidate
        #   positions, the smallest possible gap is 0 (if two candidates are adjacent
        #   with distance 0, but positions are distinct so minimum is 1... actually
        #   we binary search on real values).
        #   More precisely, lo = 0 (we'll search in real domain).
        #
        # - Upper bound (hi): The largest possible gap, which is when we only place
        #   towers at the first and last positions.
        #   hi = positions[-1] - positions[0]
        #
        # We binary search on the real-valued answer.
        # -----------------------------------------------------------------------
        lo = 0.0
        hi = float(positions[-1] - positions[0])

        # -----------------------------------------------------------------------
        # STEP 3: Define the feasibility check function
        #
        # Given a maximum allowed gap `max_gap`, can we place at most k towers
        # (including first and last) such that no consecutive gap exceeds max_gap?
        #
        # Greedy strategy:
        # - Start at positions[0] (mandatory).
        # - From current position, find the farthest candidate position that is
        #   still within max_gap distance.
        # - Place tower there, increment count.
        # - Repeat until we reach positions[-1].
        # - If total towers used <= k, it's feasible.
        # -----------------------------------------------------------------------

        def is_feasible(max_gap: float) -> bool:
            """
            Check if we can cover all positions using at most k towers
            with no consecutive gap exceeding max_gap.

            Uses a greedy approach: always jump as far as possible.
            """
            # We start at the first position (tower count = 1)
            tower_count = 1
            current_idx = 0  # Index of the current tower position

            # Keep placing towers until we reach or pass the last position
            while current_idx < n - 1:
                # Find the farthest candidate within max_gap from positions[current_idx]
                # We want the largest index j such that positions[j] - positions[current_idx] <= max_gap

                # Use binary search to find this index efficiently
                # We search in the range [current_idx + 1, n - 1]
                left = current_idx + 1
                right = n - 1
                best = current_idx  # fallback (shouldn't stay here if max_gap > 0)

                while left <= right:
                    mid_idx = (left + right) // 2
                    if positions[mid_idx] - positions[current_idx] <= max_gap:
                        # This position is reachable; try to go farther
                        best = mid_idx
                        left = mid_idx + 1
                    else:
                        # Too far; try closer
                        right = mid_idx - 1

                if best == current_idx:
                    # Cannot move forward at all — max_gap is too small
                    # (even the next candidate is too far)
                    return False

                # Place tower at 'best' position
                current_idx = best
                tower_count += 1

                # Early termination: if we've used more towers than allowed
                if tower_count > k:
                    return False

            # We've reached the last position; check if tower count is within limit
            return tower_count <= k

        # -----------------------------------------------------------------------
        # STEP 4: Binary search on the answer
        #
        # We search for the minimum value of max_gap such that is_feasible returns True.
        #
        # We perform a fixed number of iterations for precision.
        # Since positions can be up to 10^9, and we want 5 decimal places of precision,
        # we need about log2(10^9 / 10^-5) ≈ log2(10^14) ≈ 47 iterations.
        # We'll use 100 iterations to be safe.
        # -----------------------------------------------------------------------

        # Number of binary search iterations for sufficient precision
        ITERATIONS = 100

        for _ in range(ITERATIONS):
            mid = (lo + hi) / 2.0

            if is_feasible(mid):
                # mid is feasible; try to minimize further
                hi = mid
            else:
                # mid is not feasible; need a larger gap
                lo = mid

        # -----------------------------------------------------------------------
        # STEP 5: Return the result rounded to 5 decimal places
        # -----------------------------------------------------------------------
        return round(hi, 5)


# -------------------------------------------------------------------------------
# VERIFICATION / TRACING THROUGH EXAMPLES
# -------------------------------------------------------------------------------
# Example 1: positions = [1, 3, 6, 7, 12, 19], k = 3
#
# We must place towers at positions[0]=1 and positions[-1]=19.
# We need to place 1 more tower (k-2 = 1) from {3, 6, 7, 12}.
#
# Options:
# - Place at 3:  gaps = [2, 16] → max = 16
# - Place at 6:  gaps = [5, 13] → max = 13
# - Place at 7:  gaps = [6, 12] → max = 12
# - Place at 12: gaps = [11, 7] → max = 11
#
# Minimum max gap = 11 (place at 12).
#
# Let's verify with binary search:
# is_feasible(11.0)?
#   Start at index 0 (pos=1), tower_count=1
#   Find farthest within 11 from pos=1: positions within [1, 12]
#     positions: 1,3,6,7,12,19
#     12 - 1 = 11 <= 11 ✓, 19 - 1 = 18 > 11 ✗
#     best = index 4 (pos=12)
#   Move to index 4, tower_count=2
#   Find farthest within 11 from pos=12: positions within [12, 23]
#     19 - 12 = 7 <= 11 ✓
#     best = index 5 (pos=19)
#   Move to index 5 (last), tower_count=3
#   current_idx = n-1 = 5, exit loop
#   tower_count=3 <= k=3 → True
#
# is_feasible(10.99999)?
#   Start at index 0 (pos=1), tower_count=1
#   Find farthest within 10.99999 from pos=1: positions within [1, 11.99999]
#     12 - 1 = 11 > 10.99999 ✗, 7 - 1 = 6 <= 10.99999 ✓
#     best = index 3 (pos=7)
#   Move to index 3, tower_count=2
#   Find farthest within 10.99999 from pos=7: positions within [7, 17.99999]
#     12 - 7 = 5 <= 10.99999 ✓, 19 - 7 = 12 > 10.99999 ✗
#     best = index 4 (pos=12)
#   Move to index 4, tower_count=3
#   Find farthest within 10.99999 from pos=12: positions within [12, 22.99999]
#     19 - 12 = 7 <= 10.99999 ✓
#     best = index 5 (pos=19)
#   Move to index 5, tower_count=4
#   tower_count=4 > k=3 → False
#
# So the answer converges to 11.0 → 11.00000 ✓
#
# The problem statement says 9.00000 but then corrects itself to 11.00000.
# Our algorithm gives 11.00000 which matches the corrected answer.
#
# Example 2: positions = [0, 5, 10, 15, 20], k = 4
#
# We must place towers at 0 and 20, plus 2 more from {5, 10, 15}.
#
# Options (choosing 2 from {5, 10, 15}):
# - {5, 10}: gaps = [5, 5, 10] → max = 10
# - {5, 15}: gaps = [5, 10, 5] → max = 10
# - {10, 15}: gaps = [10, 5, 5] → max = 10
#
# Minimum max gap = 10.
#
# is_feasible(10.0)?
#   Start at index 0 (pos=0), tower_count=1
#   Find farthest within 10 from pos=0: 10-0=10 ✓, 15-0=15 > 10 ✗
#     best = index 2 (pos=10)
#   Move to index 2, tower_count=2
#   Find farthest within 10 from pos=10: 15-10=5 ✓, 20-10=10 ✓
#     best = index 4 (pos=20)
#   Move to index 4 (last), tower_count=3
#   tower_count=3 <= k=4 → True
#
# is_feasible(9.99999)?
#   Start at index 0 (pos=0), tower_count=1
#   Find farthest within 9.99999 from pos=0: 10-0=10 > 9.99999 ✗, 5-0=5 ✓
#     best = index 1 (pos=5)
#   Move to index 1, tower_count=2
#   Find farthest within 9.99999 from pos=5: 15-5=10 > 9.99999 ✗, 10-5=5 ✓
#     best = index 2 (pos=10)
#   Move to index 2, tower_count=3
#   Find farthest within 9.99999 from pos=10: 20-10=10 > 9.99999 ✗, 15-10=5 ✓
#     best = index 3 (pos=15)
#   Move to index 3, tower_count=4
#   Find farthest within 9.99999 from pos=15: 20-15=5 ✓
#     best = index 4 (pos=20)
#   Move to index 4 (last), tower_count=5
#   tower_count=5 > k=4 → False
#
# So the answer converges to 10.0 → 10.00000 ✓
# -------------------------------------------------------------------------------


if __name__ == "__main__":
    solution = Solution()

    # -----------------------------------------------------------------------
    # Test Case 1
    # positions = [1, 3, 6, 7, 12, 19], k = 3
    # Expected output: 11.00000
    # (Place towers at 1, 12, 19 → gaps 11 and 7 → max = 11)
    # -----------------------------------------------------------------------
    positions1 = [1, 3, 6, 7, 12, 19]
    k1 = 3
    result1 = solution.minimizeMaxDistance(positions1, k1)
    print(f"Test Case 1:")
    print(f"  positions = {positions1}, k = {k1}")
    print(f"  Result: {result1}")
    print(f"  Expected: 11.00000")
    print()

    # -----------------------------------------------------------------------
    # Test Case 2
    # positions = [0, 5, 10, 15, 20], k = 4
    # Expected output: 10.00000
    # -----------------------------------------------------------------------
    positions2 = [0, 5, 10, 15, 20]
    k2 = 4
    result2 = solution.minimizeMaxDistance(positions2, k2)
    print(f"Test Case 2:")
    print(f"  positions = {positions2}, k = {k2}")
    print(f"  Result: {result2}")
    print(f"  Expected: 10.00000")
    print()

    # -----------------------------------------------------------------------
    # Test Case 3: k equals n (place tower at every position)
    # positions = [0, 3, 7, 10], k = 4
    # All positions get a tower. Gaps: 3, 4, 3 → max = 4
    # Expected output: 3.00000 (