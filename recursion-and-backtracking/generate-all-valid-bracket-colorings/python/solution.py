```python
"""
Title: Generate All Valid Bracket Colorings
Difficulty: Easy
Topic: Recursion and Backtracking

Problem Description:
You are given a positive integer `n` representing the number of pairs of brackets.
Your task is to generate all valid combinations of `n` pairs of brackets, where each
bracket (both opening and closing) is also assigned one of two colors: Red or Blue.

A bracket sequence is considered valid if:
1. Every opening bracket has a corresponding closing bracket.
2. The brackets are properly nested.

For each valid bracket sequence, every individual bracket character (both `(` and `)`)
must be assigned a color. Two colorings of the same bracket sequence are considered
distinct if any bracket differs in color.

Return a list of all distinct results as strings. Each bracket should be represented as
`R(`, `B(`, `R)`, or `B)` for red-open, blue-open, red-close, and blue-close respectively.
The output can be in any order.

Constraints:
- 1 <= n <= 4

Example 1:
- Input: n = 1
- Output: ["R(R)", "R(B)", "B(R)", "B(B)"]
- Explanation: The only valid bracket sequence is (). Each of the two brackets can
  independently be Red or Blue, giving 4 combinations.

Example 2:
- Input: n = 2
- Output: A list of 32 strings. For the sequence (()) entries include "R(R(R)R)",
  "R(R(R)B)", etc. For the sequence ()() entries include "R(R)R(R)", etc.
  There are 2 valid sequences for n=2, each with 2^4 = 16 colorings, giving 32 total.
"""

from typing import List


class Solution:
    def generate_colored_brackets(self, n: int) -> List[str]:
        """
        Generate all valid bracket sequences of n pairs, with each bracket
        independently colored Red or Blue.

        Strategy:
        - Use backtracking to build valid bracket sequences character by character.
        - At each position, we decide:
            1. Whether to place an opening bracket '(' or closing bracket ')'
            2. What color (Red or Blue) to assign to that bracket
        - We track how many opening brackets we've placed (open_count) and how many
          closing brackets we've placed (close_count) to ensure validity.

        Args:
            n (int): Number of bracket pairs (1 <= n <= 4)

        Returns:
            List[str]: All distinct valid colored bracket sequences

        Time Complexity: O(C(n) * 2^(2n)) where C(n) is the nth Catalan number
                         (number of valid bracket sequences). Each sequence has
                         2n brackets, each independently colored 2 ways = 2^(2n) colorings.
                         For n=4: C(4)=14, 2^8=256, so ~3584 results.

        Space Complexity: O(n) for the recursion stack depth (at most 2n deep),
                          plus O(C(n) * 2^(2n)) for storing all results.
        """

        # This list will collect all valid colored bracket strings
        results: List[str] = []

        # Colors available for each bracket
        colors = ["R", "B"]

        def backtrack(
            current: str,   # The bracket string built so far
            open_count: int,  # Number of '(' placed so far
            close_count: int  # Number of ')' placed so far
        ) -> None:
            """
            Recursive backtracking function to build all valid colored bracket sequences.

            At each recursive call, we try to add the next bracket to `current`.
            We can add:
              - A colored '(' if we haven't used all n opening brackets yet
              - A colored ')' if we have more opening brackets than closing brackets
                (ensures proper nesting)

            Base case: when open_count == n and close_count == n, we have a complete
            valid sequence of n pairs, so we add it to results.
            """

            # ---------------------------------------------------------------
            # BASE CASE: We've placed exactly n opening and n closing brackets.
            # This means we have a complete, valid bracket sequence.
            # Add the current string to our results list.
            # ---------------------------------------------------------------
            if open_count == n and close_count == n:
                results.append(current)
                return  # Stop recursing; nothing more to add

            # ---------------------------------------------------------------
            # CHOICE 1: Place an opening bracket '('
            # We can place '(' as long as we haven't used all n opening brackets.
            # For each placement, we try both colors: Red and Blue.
            # ---------------------------------------------------------------
            if open_count < n:
                # Try each color for the opening bracket
                for color in colors:
                    # Build the colored bracket token, e.g., "R(" or "B("
                    token = color + "("

                    # Recurse: add this token to current, increment open_count
                    backtrack(
                        current + token,   # New string with this token appended
                        open_count + 1,    # We've used one more opening bracket
                        close_count        # Closing count unchanged
                    )
                    # Note: No explicit "undo" needed because we pass a new string
                    # (current + token) rather than mutating current in place.
                    # This is a functional/immutable approach to backtracking.

            # ---------------------------------------------------------------
            # CHOICE 2: Place a closing bracket ')'
            # We can place ')' only if close_count < open_count.
            # This ensures we never have more closing brackets than opening ones,
            # which would make the sequence invalid (e.g., ")(" is invalid).
            # For each placement, we try both colors: Red and Blue.
            # ---------------------------------------------------------------
            if close_count < open_count:
                # Try each color for the closing bracket
                for color in colors:
                    # Build the colored bracket token, e.g., "R)" or "B)"
                    token = color + ")"

                    # Recurse: add this token to current, increment close_count
                    backtrack(
                        current + token,   # New string with this token appended
                        open_count,        # Opening count unchanged
                        close_count + 1    # We've used one more closing bracket
                    )

        # ---------------------------------------------------------------
        # KICK OFF THE RECURSION
        # Start with an empty string, 0 opening brackets, 0 closing brackets.
        # ---------------------------------------------------------------
        backtrack("", 0, 0)

        return results

    def count_expected(self, n: int) -> int:
        """
        Helper method to compute the expected number of results for verification.

        The number of valid bracket sequences of n pairs is the nth Catalan number C(n).
        Each sequence has 2n brackets, each independently colored 2 ways.
        So total results = C(n) * 2^(2n).

        Catalan numbers: C(1)=1, C(2)=2, C(3)=5, C(4)=14

        Args:
            n (int): Number of bracket pairs

        Returns:
            int: Expected number of colored bracket strings
        """
        # Catalan numbers for n = 1, 2, 3, 4
        catalan = {1: 1, 2: 2, 3: 5, 4: 14}

        # Each of the 2n brackets can be colored 2 ways independently
        colorings_per_sequence = 2 ** (2 * n)

        return catalan[n] * colorings_per_sequence


# -----------------------------------------------------------------------
# MAIN BLOCK: Demonstrate and verify the solution
# -----------------------------------------------------------------------
if __name__ == "__main__":
    solution = Solution()

    # -------------------------------------------------------------------
    # Example 1: n = 1
    # Expected output: ["R(R)", "R(B)", "B(R)", "B(B)"] (in any order)
    # Explanation: Only one valid sequence "()", 2^2 = 4 colorings
    # -------------------------------------------------------------------
    print("=" * 60)
    print("Example 1: n = 1")
    print("=" * 60)

    n1 = 1
    result1 = solution.generate_colored_brackets(n1)

    print(f"Input: n = {n1}")
    print(f"Output ({len(result1)} items): {sorted(result1)}")
    print(f"Expected count: {solution.count_expected(n1)}")
    print(f"Count matches: {len(result1) == solution.count_expected(n1)}")

    # Verify specific expected values from the problem
    expected1 = {"R(R)", "R(B)", "B(R)", "B(B)"}
    result1_set = set(result1)
    print(f"Contains all expected strings: {expected1.issubset(result1_set)}")
    print(f"No extra strings: {result1_set == expected1}")
    print()

    # -------------------------------------------------------------------
    # Example 2: n = 2
    # Expected: 32 strings total
    # 2 valid sequences: "(())" and "()()"
    # Each has 4 brackets, 2^4 = 16 colorings per sequence
    # Total: 2 * 16 = 32
    # -------------------------------------------------------------------
    print("=" * 60)
    print("Example 2: n = 2")
    print("=" * 60)

    n2 = 2
    result2 = solution.generate_colored_brackets(n2)

    print(f"Input: n = {n2}")
    print(f"Total count: {len(result2)} (expected: {solution.count_expected(n2)})")
    print(f"Count matches: {len(result2) == solution.count_expected(n2)}")

    # Show a few sample entries to verify format
    print("\nSample entries (first 8):")
    for item in sorted(result2)[:8]:
        print(f"  {item}")

    # Verify specific entries mentioned in the problem description
    check_entries = ["R(R(R)R)", "R(R(R)B)", "R(R)R(R)"]
    print("\nVerifying specific entries from problem description:")
    for entry in check_entries:
        present = entry in result2
        print(f"  '{entry}' present: {present}")
    print()

    # -------------------------------------------------------------------
    # Example 3: n = 3
    # Expected: C(3) * 2^6 = 5 * 64 = 320 strings
    # -------------------------------------------------------------------
    print("=" * 60)
    print("Example 3: n = 3")
    print("=" * 60)

    n3 = 3
    result3 = solution.generate_colored_brackets(n3)

    print(f"Input: n = {n3}")
    print(f"Total count: {len(result3)} (expected: {solution.count_expected(n3)})")
    print(f"Count matches: {len(result3) == solution.count_expected(n3)}")

    # Show a few sample entries
    print("\nSample entries (first 5):")
    for item in sorted(result3)[:5]:
        print(f"  {item}")
    print()

    # -------------------------------------------------------------------
    # Example 4: n = 4
    # Expected: C(4) * 2^8 = 14 * 256 = 3584 strings
    # -------------------------------------------------------------------
    print("=" * 60)
    print("Example 4: n = 4")
    print("=" * 60)

    n4 = 4
    result4 = solution.generate_colored_brackets(n4)

    print(f"Input: n = {n4}")
    print(f"Total count: {len(result4)} (expected: {solution.count_expected(n4)})")
    print(f"Count matches: {len(result4) == solution.count_expected(n4)}")

    # Show a few sample entries
    print("\nSample entries (first 5):")
    for item in sorted(result4)[:5]:
        print(f"  {item}")
    print()

    # -------------------------------------------------------------------
    # Verify all results have no duplicates
    # -------------------------------------------------------------------
    print("=" * 60)
    print("Uniqueness Verification")
    print("=" * 60)
    for n_val, result in [(1, result1), (2, result2), (3, result3), (4, result4)]:
        unique_count = len(set(result))
        total_count = len(result)
        print(f"n={n_val}: total={total_count}, unique={unique_count}, "
              f"all unique: {unique_count == total_count}")
```