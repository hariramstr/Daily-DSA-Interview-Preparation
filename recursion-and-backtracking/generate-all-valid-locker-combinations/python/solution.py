"""
Title: Generate All Valid Locker Combinations
Difficulty: Easy
Topic: Recursion and Backtracking

Problem Description:
You are managing a secure storage facility where each locker is protected by a
numeric combination lock. A valid combination is a sequence of exactly n digits where:

1. Each digit is between 1 and k (inclusive).
2. No two adjacent digits in the combination are the same.

Given two integers n (the length of the combination) and k (the maximum digit value),
return all valid combinations in lexicographically ascending order.

Example 1:
    Input: n = 2, k = 3
    Output: ["12", "13", "21", "23", "31", "32"]
    Explanation: All 2-digit combinations using digits 1-3 where no two adjacent
                 digits are the same.

Example 2:
    Input: n = 1, k = 4
    Output: ["1", "2", "3", "4"]
    Explanation: Single-digit combinations have no adjacent pairs, so all digits
                 from 1 to k are valid.

Constraints:
    - 1 <= n <= 6
    - 1 <= k <= 9
    - The total number of valid combinations will not exceed 10,000.
"""

from typing import List


class Solution:
    """
    A class to solve the Generate All Valid Locker Combinations problem
    using recursive backtracking.
    """

    def generate_combinations(self, n: int, k: int) -> List[str]:
        """
        Generate all valid locker combinations of length n using digits 1 to k,
        where no two adjacent digits are the same.

        This method uses recursive backtracking to build each combination
        digit by digit. At each step, we try all digits from 1 to k, but
        skip any digit that equals the last digit we placed (to avoid
        adjacent duplicates). We collect results in lexicographic order
        naturally because we iterate digits from 1 to k in ascending order.

        Args:
            n (int): The exact length of each combination (1 <= n <= 6).
            k (int): The maximum digit value allowed (1 <= k <= 9).

        Returns:
            List[str]: A list of all valid combinations as strings, sorted
                       in lexicographically ascending order.

        Time Complexity:
            O(k * (k-1)^(n-1)) — For the first position we have k choices,
            and for each subsequent position we have (k-1) choices (any digit
            except the previous one). Each combination takes O(n) to convert
            to a string, so total is O(n * k * (k-1)^(n-1)).

        Space Complexity:
            O(n) for the recursion call stack depth (we go n levels deep)
            plus O(n * k * (k-1)^(n-1)) to store all results.
        """

        # ----------------------------------------------------------------
        # STEP 1: Initialize the results list and the current path.
        #
        # 'results' will accumulate every valid combination we find.
        # 'current_path' is a list of digit characters we are building
        # up one position at a time. Using a list (instead of a string)
        # lets us efficiently append and pop during backtracking.
        # ----------------------------------------------------------------
        results: List[str] = []
        current_path: List[str] = []

        # ----------------------------------------------------------------
        # STEP 2: Define the inner recursive backtracking function.
        #
        # We define it as a nested function so it can directly access
        # 'results' and 'current_path' from the enclosing scope without
        # needing to pass them as arguments every time.
        # ----------------------------------------------------------------
        def backtrack() -> None:
            """
            Recursive helper that builds combinations digit by digit.

            At each call, 'current_path' holds the digits chosen so far.
            We either record the path (if it has reached length n) or
            extend it by trying each digit from 1 to k, skipping the
            digit that was used in the immediately preceding position.
            """

            # ------------------------------------------------------------
            # BASE CASE: If the current path has exactly n digits, we have
            # built a complete valid combination.
            #
            # We join the list of digit characters into a single string
            # and append it to 'results'. Then we return immediately to
            # stop going deeper — there is nothing more to add.
            # ------------------------------------------------------------
            if len(current_path) == n:
                # ''.join(current_path) converts ['1', '2'] -> "12"
                results.append("".join(current_path))
                return  # Backtrack: let the caller try the next digit

            # ------------------------------------------------------------
            # RECURSIVE CASE: We still need more digits.
            #
            # Determine what the last digit placed was (if any).
            # If current_path is empty, there is no previous digit, so
            # we set last_digit to None to signal "no restriction yet".
            # ------------------------------------------------------------
            last_digit: str | None = current_path[-1] if current_path else None

            # ------------------------------------------------------------
            # Try every digit from 1 to k in ascending order.
            #
            # Iterating from 1 upward naturally produces results in
            # lexicographic order, so no sorting is needed afterward.
            # ------------------------------------------------------------
            for digit in range(1, k + 1):
                # Convert the integer digit to a string character so it
                # can be stored in current_path and later joined.
                digit_str: str = str(digit)

                # --------------------------------------------------------
                # PRUNING: Skip this digit if it equals the last digit.
                #
                # This is the key constraint — no two adjacent digits may
                # be the same. By skipping here, we avoid ever placing an
                # invalid digit, which is more efficient than generating
                # the full combination and then checking validity.
                # --------------------------------------------------------
                if digit_str == last_digit:
                    # This digit would create an adjacent duplicate; skip it.
                    continue

                # --------------------------------------------------------
                # CHOOSE: Add the current digit to the path.
                #
                # We append to the list, which is O(1) amortized.
                # --------------------------------------------------------
                current_path.append(digit_str)

                # --------------------------------------------------------
                # EXPLORE: Recurse to fill the next position.
                #
                # The recursive call will see the updated current_path
                # and continue building from the next position.
                # --------------------------------------------------------
                backtrack()

                # --------------------------------------------------------
                # UN-CHOOSE (Backtrack): Remove the digit we just added.
                #
                # After the recursive call returns, we pop the last element
                # so that current_path is restored to its state before we
                # chose this digit. This allows the loop to try the next
                # digit in the same position cleanly.
                # --------------------------------------------------------
                current_path.pop()

        # ----------------------------------------------------------------
        # STEP 3: Kick off the recursion with an empty path.
        #
        # The first call to backtrack() starts with current_path = [],
        # meaning no digits have been chosen yet.
        # ----------------------------------------------------------------
        backtrack()

        # ----------------------------------------------------------------
        # STEP 4: Return the fully populated results list.
        #
        # Because we iterated digits 1..k in ascending order at every
        # level of recursion, the combinations are already in
        # lexicographically ascending order.
        # ----------------------------------------------------------------
        return results


# ============================================================
# MAIN BLOCK — Demonstrates and verifies the solution
# ============================================================
if __name__ == "__main__":
    # Create an instance of our Solution class
    sol = Solution()

    # ----------------------------------------------------------
    # Example 1: n=2, k=3
    # Expected output: ["12", "13", "21", "23", "31", "32"]
    # ----------------------------------------------------------
    n1, k1 = 2, 3
    result1 = sol.generate_combinations(n1, k1)
    expected1 = ["12", "13", "21", "23", "31", "32"]

    print("=" * 55)
    print(f"Example 1  |  n={n1}, k={k1}")
    print(f"Result  : {result1}")
    print(f"Expected: {expected1}")
    print(f"PASS" if result1 == expected1 else f"FAIL")

    # ----------------------------------------------------------
    # Example 2: n=1, k=4
    # Expected output: ["1", "2", "3", "4"]
    # ----------------------------------------------------------
    n2, k2 = 1, 4
    result2 = sol.generate_combinations(n2, k2)
    expected2 = ["1", "2", "3", "4"]

    print("=" * 55)
    print(f"Example 2  |  n={n2}, k={k2}")
    print(f"Result  : {result2}")
    print(f"Expected: {expected2}")
    print(f"PASS" if result2 == expected2 else f"FAIL")

    # ----------------------------------------------------------
    # Extra test: n=3, k=2
    # Digits: 1, 2  — no two adjacent the same, length 3
    # Valid combos: 121, 212
    # ----------------------------------------------------------
    n3, k3 = 3, 2
    result3 = sol.generate_combinations(n3, k3)
    expected3 = ["121", "212"]

    print("=" * 55)
    print(f"Extra Test |  n={n3}, k={k3}")
    print(f"Result  : {result3}")
    print(f"Expected: {expected3}")
    print(f"PASS" if result3 == expected3 else f"FAIL")

    # ----------------------------------------------------------
    # Extra test: n=1, k=1
    # Only one digit available, length 1 — result: ["1"]
    # ----------------------------------------------------------
    n4, k4 = 1, 1
    result4 = sol.generate_combinations(n4, k4)
    expected4 = ["1"]

    print("=" * 55)
    print(f"Edge Case  |  n={n4}, k={k4}")
    print(f"Result  : {result4}")
    print(f"Expected: {expected4}")
    print(f"PASS" if result4 == expected4 else f"FAIL")

    # ----------------------------------------------------------
    # Extra test: n=2, k=1
    # Only digit 1, but adjacent digits can't repeat — no valid combo
    # ----------------------------------------------------------
    n5, k5 = 2, 1
    result5 = sol.generate_combinations(n5, k5)
    expected5: List[str] = []

    print("=" * 55)
    print(f"Edge Case  |  n={n5}, k={k5}")
    print(f"Result  : {result5}")
    print(f"Expected: {expected5}")
    print(f"PASS" if result5 == expected5 else f"FAIL")
    print("=" * 55)