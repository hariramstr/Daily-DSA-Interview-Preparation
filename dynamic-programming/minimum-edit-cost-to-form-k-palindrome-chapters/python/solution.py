"""
Title: Minimum Edit Cost to Form K Palindrome Chapters

Problem Description:
You are given a lowercase string s representing a draft manuscript and an integer k.
The editor wants to divide the manuscript into exactly k non-empty contiguous chapters.
After the split, each chapter may be edited independently so that the final text of
every chapter is a palindrome.

In one edit, you may change any single character in a chapter to any other lowercase
English letter. The cost of a chapter is the minimum number of character changes
required to turn that chapter into a palindrome. Your task is to return the minimum
total edit cost over all ways to split s into exactly k chapters.

A palindrome reads the same forward and backward. For example, the chapter "abca"
can be turned into a palindrome with 1 edit, because changing either 'b' to 'c' or
'c' to 'b' makes it symmetric. The split must preserve order, and every character
of s must belong to exactly one chapter.

Return the minimum possible total number of edits.

Constraints:
- 1 <= s.length <= 200
- 1 <= k <= s.length
- s consists only of lowercase English letters

Example 1:
Input: s = "abcac", k = 2
Output: 1

Example 2:
Input: s = "leetcode", k = 3
Output: 2
"""

from typing import List


class Solution:
    def _build_palindrome_cost(self, s: str) -> List[List[int]]:
        """
        Precompute the minimum edit cost needed to convert every substring into a palindrome.

        For any substring s[i:j+1], the minimum number of edits needed is equal to the
        number of mismatched mirrored character pairs inside that substring.

        We compute this with interval dynamic programming:
        cost[i][j] = cost[i+1][j-1] + (1 if s[i] != s[j] else 0)

        Args:
            s: The input lowercase string.

        Returns:
            A 2D list where cost[i][j] is the minimum number of character changes
            needed to make substring s[i:j+1] a palindrome.

        Time complexity:
            O(n^2)

        Space complexity:
            O(n^2)
        """
        n: int = len(s)

        # Create an n x n table initialized with 0.
        # cost[i][j] will only be meaningful when i <= j.
        cost: List[List[int]] = [[0] * n for _ in range(n)]

        # We fill the table by increasing substring length.
        # Length 1 substrings are already palindromes, so cost is 0.
        # Length 2+ substrings depend on smaller inner substrings.
        for length in range(2, n + 1):
            for left in range(0, n - length + 1):
                right: int = left + length - 1

                # If the two boundary characters are different, we need one edit
                # to make this mirrored pair match.
                mismatch_cost: int = 1 if s[left] != s[right] else 0

                # For length 2, there is no "inner" substring to look up.
                # Example:
                #   substring "ab" -> cost is simply 1 if a != b else 0
                if length == 2:
                    cost[left][right] = mismatch_cost
                else:
                    # For longer substrings, the total cost is:
                    #   cost of fixing the inside + cost of fixing the outer pair
                    cost[left][right] = cost[left + 1][right - 1] + mismatch_cost

        return cost

    def palindromePartition(self, s: str, k: int) -> int:
        """
        Compute the minimum total edit cost to split the string into exactly k
        non-empty contiguous parts, where each part can be edited into a palindrome.

        This solution uses two dynamic programming stages:

        1. Precompute palindrome conversion cost for every substring.
        2. Use partition DP:
           dp[i][parts] = minimum cost to split the first i characters of s
                          into exactly 'parts' palindrome chapters.

        Transition:
            dp[i][parts] = min(
                dp[start][parts - 1] + cost[start][i - 1]
                for all valid start positions
            )

        Here, the last chapter is s[start:i], and the previous part uses the first
        'start' characters.

        Args:
            s: The input lowercase string.
            k: The exact number of chapters to split into.

        Returns:
            The minimum possible total number of edits.

        Time complexity:
            O(n^2 + k * n^2) = O(k * n^2)

        Space complexity:
            O(n^2 + n * k)
        """
        n: int = len(s)

        # Step 1:
        # Precompute the cost to convert every substring into a palindrome.
        # This is essential because during partition DP we will repeatedly ask:
        # "What is the cost of making substring s[l:r+1] a palindrome?"
        # Without preprocessing, answering that repeatedly would be too slow.
        cost: List[List[int]] = self._build_palindrome_cost(s)

        # We use a large number to represent "impossible" or "not yet computed".
        inf: int = 10**9

        # Step 2:
        # Create DP table:
        # dp[i][parts] = minimum cost to split the first i characters (s[0:i])
        # into exactly 'parts' non-empty chapters.
        #
        # Dimensions:
        #   i ranges from 0 to n
        #   parts ranges from 0 to k
        #
        # Why use first i characters instead of substring indices directly?
        # Because prefix-based DP makes partition transitions very natural:
        # choose where the last chapter starts.
        dp: List[List[int]] = [[inf] * (k + 1) for _ in range(n + 1)]

        # Base case:
        # Splitting an empty prefix into 0 chapters costs 0.
        dp[0][0] = 0

        # Fill the DP table chapter count by chapter count.
        for parts in range(1, k + 1):
            # To split the first i characters into 'parts' non-empty chapters,
            # we must have at least i >= parts.
            #
            # Example:
            #   i = 3, parts = 4 is impossible because 4 non-empty chapters
            #   cannot fit into 3 characters.
            for i in range(parts, n + 1):
                # We try every possible starting position for the last chapter.
                #
                # Let the last chapter be s[start:i].
                # Then:
                #   - the previous prefix is s[0:start]
                #   - it must be split into exactly parts - 1 chapters
                #
                # Since each chapter must be non-empty:
                #   start must be at least parts - 1
                #   because the first 'start' characters must hold parts - 1 chapters
                #
                # Also start < i so that the last chapter is non-empty.
                for start in range(parts - 1, i):
                    # If the previous state is impossible, skip it.
                    if dp[start][parts - 1] == inf:
                        continue

                    # Cost of making the last chapter s[start:i] into a palindrome.
                    # Since our cost table uses inclusive indices, this is:
                    #   cost[start][i - 1]
                    last_chapter_cost: int = cost[start][i - 1]

                    # Candidate total cost:
                    #   best cost for previous prefix + cost of last chapter
                    candidate: int = dp[start][parts - 1] + last_chapter_cost

                    # Keep the minimum over all possible last chapter starts.
                    if candidate < dp[i][parts]:
                        dp[i][parts] = candidate

        # The answer is the minimum cost to split the entire string (first n chars)
        # into exactly k chapters.
        return dp[n][k]


if __name__ == "__main__":
    solution = Solution()

    # Sample 1
    s1: str = "abcac"
    k1: int = 2
    result1: int = solution.palindromePartition(s1, k1)
    print(f's = "{s1}", k = {k1} -> {result1}')  # Expected: 1

    # Sample 2
    s2: str = "leetcode"
    k2: int = 3
    result2: int = solution.palindromePartition(s2, k2)
    print(f's = "{s2}", k = {k2} -> {result2}')  # Expected: 2

    # Additional quick checks
    s3: str = "a"
    k3: int = 1
    result3: int = solution.palindromePartition(s3, k3)
    print(f's = "{s3}", k = {k3} -> {result3}')  # Expected: 0

    s4: str = "abc"
    k4: int = 3
    result4: int = solution.palindromePartition(s4, k4)
    print(f's = "{s4}", k = {k4} -> {result4}')  # Expected: 0