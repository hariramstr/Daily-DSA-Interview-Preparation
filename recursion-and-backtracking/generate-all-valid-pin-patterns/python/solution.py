"""
Generate All Valid PIN Patterns
================================

A PIN pattern is a sequence of digits from 1 to 9 where each digit is used at most once.
A PIN pattern is considered valid if its length is between minLen and maxLen (inclusive),
and each consecutive pair of digits in the sequence satisfies the following rule:
if the two digits are not adjacent on a 3x3 grid (horizontally, vertically, or diagonally),
then all digits that lie on the straight line between them must have already been used
in the pattern before that move.

The 3x3 grid is laid out as follows:
    1 2 3
    4 5 6
    7 8 9

For example:
- Moving from 1 to 3 requires that 2 has already been visited.
- Moving from 1 to 9 requires that 5 has already been visited.
- Moving from 1 to 7 requires that 4 has already been visited.

Given two integers minLen and maxLen, return the total number of valid PIN patterns.

Constraints:
    1 <= minLen <= maxLen <= 9
"""

from typing import Dict, Set


class Solution:
    def numberOfPatterns(self, minLen: int, maxLen: int) -> int:
        """
        Count all valid PIN patterns with lengths between minLen and maxLen.

        The approach uses backtracking: we try placing each digit at each position
        in the sequence, checking validity at each step. We use symmetry to reduce
        computation (corners are equivalent, edges are equivalent, center is unique).

        Args:
            minLen (int): Minimum length of valid patterns (inclusive).
            maxLen (int): Maximum length of valid patterns (inclusive).

        Returns:
            int: Total number of valid PIN patterns.

        Time Complexity:  O(9!) in the worst case — we explore all permutations of 9 digits.
        Space Complexity: O(9) for the recursion stack and the visited set.
        """

        # -----------------------------------------------------------------------
        # Step 1: Build the "skip" table.
        #
        # The skip table tells us: when moving from digit `a` to digit `b`,
        # which digit (if any) must have been visited already?
        #
        # If skip[a][b] == 0, it means a and b are adjacent (or diagonal neighbors),
        # so no intermediate digit is required.
        # If skip[a][b] == k (k != 0), then digit k must be visited before we can
        # move from a to b.
        #
        # We only need to specify pairs where there IS a required intermediate digit.
        # All other pairs default to 0 (no requirement).
        # -----------------------------------------------------------------------

        # Initialize a 10x10 table (indices 0-9, we use 1-9) with zeros.
        # skip[i][j] = the digit that must be visited before moving from i to j.
        skip: Dict[int, Dict[int, int]] = {}
        for i in range(10):
            skip[i] = {}
            for j in range(10):
                skip[i][j] = 0  # Default: no intermediate digit required

        # Now fill in the cases where an intermediate digit IS required.
        # These are the "jump" moves on the 3x3 grid:

        # Horizontal jumps (same row, skipping the middle):
        skip[1][3] = skip[3][1] = 2   # 1 <-> 3 requires 2
        skip[4][6] = skip[6][4] = 5   # 4 <-> 6 requires 5
        skip[7][9] = skip[9][7] = 8   # 7 <-> 9 requires 8

        # Vertical jumps (same column, skipping the middle):
        skip[1][7] = skip[7][1] = 4   # 1 <-> 7 requires 4
        skip[2][8] = skip[8][2] = 5   # 2 <-> 8 requires 5
        skip[3][9] = skip[9][3] = 6   # 3 <-> 9 requires 6

        # Diagonal jumps (skipping the center):
        skip[1][9] = skip[9][1] = 5   # 1 <-> 9 requires 5
        skip[3][7] = skip[7][3] = 5   # 3 <-> 7 requires 5

        # -----------------------------------------------------------------------
        # Step 2: Define the backtracking function.
        #
        # We'll use a recursive function that:
        #   - Tracks which digits have been visited (using a set).
        #   - Tracks the current last digit in the pattern.
        #   - Tracks the current pattern length.
        #   - Counts valid patterns whose length falls in [minLen, maxLen].
        # -----------------------------------------------------------------------

        # A set to track which digits are currently in our pattern.
        visited: Set[int] = set()

        def backtrack(last: int, length: int) -> int:
            """
            Recursively count valid patterns starting from 'last' with current 'length'.

            Args:
                last (int): The last digit added to the current pattern.
                length (int): The current length of the pattern.

            Returns:
                int: Number of valid patterns that can be formed from this state.
            """

            # -------------------------------------------------------------------
            # Base case / counting:
            # If the current length is already >= minLen, this pattern itself
            # is valid (as long as length <= maxLen, which we ensure before calling).
            # We count it and continue exploring longer patterns.
            # -------------------------------------------------------------------
            count = 0

            # Count this pattern if its length is within the valid range.
            # (We only call backtrack when length <= maxLen, so we just check >= minLen)
            if length >= minLen:
                count += 1  # This pattern of 'length' digits is valid

            # If we've reached maxLen, we can't extend further — stop here.
            if length == maxLen:
                return count

            # -------------------------------------------------------------------
            # Recursive step:
            # Try adding each digit (1-9) as the next digit in the pattern.
            # A digit can be added if:
            #   1. It hasn't been visited yet.
            #   2. The "skip" requirement is satisfied: either there's no required
            #      intermediate digit, OR the required intermediate digit has
            #      already been visited.
            # -------------------------------------------------------------------
            for next_digit in range(1, 10):
                # Check if next_digit is already used in the current pattern.
                if next_digit in visited:
                    continue  # Skip already-used digits

                # Check the skip requirement:
                # skip[last][next_digit] gives the digit that must be visited
                # before we can move from 'last' to 'next_digit'.
                required = skip[last][next_digit]

                # If required == 0, no intermediate digit is needed (they're neighbors).
                # If required != 0, that digit must already be in 'visited'.
                if required != 0 and required not in visited:
                    continue  # Can't make this move yet

                # -------------------------------------------------------------------
                # Make the move: add next_digit to the pattern.
                # -------------------------------------------------------------------
                visited.add(next_digit)

                # Recurse with next_digit as the new last digit, length + 1.
                count += backtrack(next_digit, length + 1)

                # -------------------------------------------------------------------
                # Undo the move (backtrack): remove next_digit from visited.
                # This restores the state so we can try other digits.
                # -------------------------------------------------------------------
                visited.remove(next_digit)

            return count

        # -----------------------------------------------------------------------
        # Step 3: Use symmetry to reduce computation.
        #
        # The 3x3 grid has symmetry:
        #   - The 4 corner digits (1, 3, 7, 9) are all equivalent by rotation/reflection.
        #   - The 4 edge digits (2, 4, 6, 8) are all equivalent by rotation/reflection.
        #   - The center digit (5) is unique.
        #
        # So instead of starting from all 9 digits, we can:
        #   - Start from digit 1 (a corner) and multiply by 4.
        #   - Start from digit 2 (an edge) and multiply by 4.
        #   - Start from digit 5 (center) and multiply by 1.
        #
        # This reduces the number of backtracking calls by a factor of 3.
        # -----------------------------------------------------------------------

        total = 0

        # Start from a corner digit (1) — represents all 4 corners (1, 3, 7, 9)
        visited.add(1)
        corner_count = backtrack(1, 1)
        visited.remove(1)
        total += corner_count * 4  # Multiply by 4 for all corners

        # Start from an edge digit (2) — represents all 4 edges (2, 4, 6, 8)
        visited.add(2)
        edge_count = backtrack(2, 1)
        visited.remove(2)
        total += edge_count * 4  # Multiply by 4 for all edges

        # Start from the center digit (5) — unique, no multiplication needed
        visited.add(5)
        center_count = backtrack(5, 1)
        visited.remove(5)
        total += center_count * 1  # Only 1 center

        return total


# =============================================================================
# Main block: Test the solution with the provided examples and additional cases.
# =============================================================================

if __name__ == "__main__":
    solution = Solution()

    # -------------------------------------------------------------------------
    # Example 1: minLen = 1, maxLen = 1
    # Expected Output: 9
    # Explanation: All single-digit patterns (1 through 9) are valid.
    # -------------------------------------------------------------------------
    result1 = solution.numberOfPatterns(1, 1)
    print(f"Example 1: minLen=1, maxLen=1")
    print(f"  Result:   {result1}")
    print(f"  Expected: 9")
    print(f"  Correct:  {result1 == 9}")
    print()

    # -------------------------------------------------------------------------
    # Example 2: minLen = 1, maxLen = 2
    # Expected Output: 65
    # Explanation: 9 single-digit + 56 valid two-digit patterns = 65
    # -------------------------------------------------------------------------
    result2 = solution.numberOfPatterns(1, 2)
    print(f"Example 2: minLen=1, maxLen=2")
    print(f"  Result:   {result2}")
    print(f"  Expected: 65")
    print(f"  Correct:  {result2 == 65}")
    print()

    # -------------------------------------------------------------------------
    # Additional test: minLen = 2, maxLen = 2
    # Expected: 56 (just the two-digit patterns)
    # -------------------------------------------------------------------------
    result3 = solution.numberOfPatterns(2, 2)
    print(f"Additional: minLen=2, maxLen=2")
    print(f"  Result:   {result3}")
    print(f"  Expected: 56")
    print(f"  Correct:  {result3 == 56}")
    print()

    # -------------------------------------------------------------------------
    # Additional test: minLen = 4, maxLen = 4
    # Expected: 1624 (well-known result for Android unlock patterns of length 4)
    # -------------------------------------------------------------------------
    result4 = solution.numberOfPatterns(4, 4)
    print(f"Additional: minLen=4, maxLen=4")
    print(f"  Result:   {result4}")
    print(f"  Expected: 1624")
    print(f"  Correct:  {result4 == 1624}")
    print()

    # -------------------------------------------------------------------------
    # Additional test: minLen = 1, maxLen = 9
    # Expected: 389112 (total valid patterns of all lengths)
    # -------------------------------------------------------------------------
    result5 = solution.numberOfPatterns(1, 9)
    print(f"Additional: minLen=1, maxLen=9")
    print(f"  Result:   {result5}")
    print(f"  Expected: 389112")
    print(f"  Correct:  {result5 == 389112}")
    print()

    # -------------------------------------------------------------------------
    # Additional test: minLen = 9, maxLen = 9
    # Expected: 140 (only full 9-digit patterns)
    # -------------------------------------------------------------------------
    result6 = solution.numberOfPatterns(9, 9)
    print(f"Additional: minLen=9, maxLen=9")
    print(f"  Result:   {result6}")
    print(f"  Expected: 140")
    print(f"  Correct:  {result6 == 140}")