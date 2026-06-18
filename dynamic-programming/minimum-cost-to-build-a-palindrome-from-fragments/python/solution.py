"""
Title: Minimum Cost to Build a Palindrome from Fragments

Problem Description:
You are given a string `s` representing a raw message and an integer array `cost`
of the same length, where `cost[i]` is the cost to keep character `s[i]` in the
final message. You may delete any characters you want, and deleting a character
has no cost. Your goal is to build a palindrome subsequence from `s` such that
the sum of costs of the kept characters is as small as possible. Among all
non-empty palindrome subsequences that can be formed, return the minimum possible
total keep-cost.

A subsequence is formed by deleting zero or more characters without changing the
order of the remaining characters. A palindrome reads the same forward and backward.

Constraints:
- 1 <= s.length <= 1000
- s.length == cost.length
- 1 <= cost[i] <= 10^6
- s contains only lowercase English letters

Important correctness note:
Because any single character is itself a non-empty palindrome subsequence, the
minimum possible keep-cost is simply the minimum value in `cost`.

Even though the problem statement mentions dynamic programming and substrings,
the mathematically correct answer is the cheapest single character, since every
longer palindrome has positive additional keep-cost and therefore cannot be
cheaper than the minimum single-character choice.

For the provided examples:
- s = "abca", cost = [4, 2, 7, 3] -> answer is 2 (keep only 'b')
- s = "racecar", cost = [8, 6, 5, 1, 5, 6, 8] -> answer is 1 (keep only 'e')

The first example text in the prompt contains an internal contradiction:
it claims output 3, but also lists "b" with cost 2. The correct answer is 2.
"""

from typing import List


class Solution:
    def min_cost_palindrome_subsequence(self, s: str, cost: List[int]) -> int:
        """
        Return the minimum total keep-cost of any non-empty palindromic subsequence.

        Since every single character is a palindrome of length 1, we can always form
        a valid non-empty palindromic subsequence by keeping exactly one character.
        Therefore, the optimal answer is the minimum keep-cost among all positions.

        Args:
            s: The input string.
            cost: cost[i] is the cost to keep s[i].

        Returns:
            The minimum possible total keep-cost of a non-empty palindromic subsequence.

        Time complexity:
            O(n), where n is the length of the string.

        Space complexity:
            O(1) auxiliary space.
        """
        # Defensive check:
        # The constraints guarantee that the string is non-empty and that
        # len(s) == len(cost). Still, validating the input makes the method
        # safer and easier to understand for beginners.
        if not s or not cost or len(s) != len(cost):
            raise ValueError("Input must satisfy: non-empty s, non-empty cost, and len(s) == len(cost).")

        # Core observation:
        # - Any one character by itself is a palindrome.
        # - Keeping more than one character adds positive cost because all costs >= 1.
        # - Therefore, no longer palindrome can beat the cheapest single character.
        #
        # So we simply scan the cost array and return its minimum value.
        #
        # Example:
        # s = "abca", cost = [4, 2, 7, 3]
        # Single-character palindromes:
        #   "a" from index 0 -> 4
        #   "b" from index 1 -> 2
        #   "c" from index 2 -> 7
        #   "a" from index 3 -> 3
        # Minimum is 2.
        return min(cost)


if __name__ == "__main__":
    solution = Solution()

    # Sample 1:
    # The prompt text is contradictory here.
    # Correct answer is 2, because keeping only 'b' is a valid palindrome subsequence.
    s1 = "abca"
    cost1 = [4, 2, 7, 3]
    result1 = solution.min_cost_palindrome_subsequence(s1, cost1)
    print(f"Input: s = {s1}, cost = {cost1}")
    print(f"Output: {result1}")
    print("Expected (correct by definition): 2")
    print()

    # Sample 2:
    s2 = "racecar"
    cost2 = [8, 6, 5, 1, 5, 6, 8]
    result2 = solution.min_cost_palindrome_subsequence(s2, cost2)
    print(f"Input: s = {s2}, cost = {cost2}")
    print(f"Output: {result2}")
    print("Expected: 1")
    print()

    # Additional quick sanity checks:
    s3 = "z"
    cost3 = [10]
    print(f"Input: s = {s3}, cost = {cost3}")
    print(f"Output: {solution.min_cost_palindrome_subsequence(s3, cost3)}")
    print("Expected: 10")
    print()

    s4 = "aaaa"
    cost4 = [9, 4, 6, 2]
    print(f"Input: s = {s4}, cost = {cost4}")
    print(f"Output: {solution.min_cost_palindrome_subsequence(s4, cost4)}")
    print("Expected: 2")