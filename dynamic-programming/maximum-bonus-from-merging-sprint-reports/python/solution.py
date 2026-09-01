"""
Title: Maximum Bonus from Merging Sprint Reports

Problem Description:
A product team tracks daily engineering output as an array of integers `reports`,
where `reports[i]` is the score recorded on day `i`. To prepare a quarterly review,
the manager wants to compress the timeline into several consecutive sprint summaries.

If you choose a subarray from index `l` to `r` as one sprint summary, its bonus is:

    (sum of reports[l..r]) * (length of the sprint)

You must partition the entire array into one or more contiguous, non-empty sprint
summaries. Every day must belong to exactly one summary, and summaries cannot overlap
or be reordered.

Return the maximum total bonus obtainable.

In other words, split the array into contiguous blocks, compute sum(block) * size(block)
for each block, and maximize the sum of these values.

This is a dynamic programming problem because the best partition ending at a position
depends on the best partitions of all earlier prefixes.

Constraints:
- 1 <= reports.length <= 2000
- -10^4 <= reports[i] <= 10^4
- The answer fits in a signed 64-bit integer.

Examples:
1) reports = [3, -1, 2]
   Whole array bonus = (3 + -1 + 2) * 3 = 4 * 3 = 12
   This is better than splitting, so answer is 12.

2) reports = [4, -5, 6, 1]
   Whole array bonus = (4 + -5 + 6 + 1) * 4 = 6 * 4 = 24
   This is the best, so answer is 24.
"""

from typing import List


class Solution:
    def max_bonus(self, reports: List[int]) -> int:
        """
        Compute the maximum total bonus by partitioning the array into contiguous blocks.

        We use dynamic programming on prefixes:
        - Let dp[i] be the maximum bonus obtainable using the first i elements
          (that is, reports[0:i]).
        - To compute dp[i], we try every possible last block ending at index i - 1.
          If that last block starts at index j, then:
              previous best = dp[j]
              last block sum = sum(reports[j:i])
              last block length = i - j
              candidate = dp[j] + sum(reports[j:i]) * (i - j)
          We take the maximum candidate over all j in [0, i - 1].

        Prefix sums allow us to compute any subarray sum in O(1), making the total
        time complexity O(n^2), which is acceptable for n <= 2000.

        Args:
            reports: List of daily engineering output scores.

        Returns:
            The maximum total bonus obtainable as an integer.

        Time complexity:
            O(n^2)

        Space complexity:
            O(n)
        """
        n: int = len(reports)

        # prefix[i] will store the sum of the first i elements:
        # prefix[0] = 0
        # prefix[1] = reports[0]
        # prefix[2] = reports[0] + reports[1]
        # ...
        #
        # This lets us compute the sum of any subarray reports[l:r+1] quickly:
        # sum(reports[l:r+1]) = prefix[r+1] - prefix[l]
        #
        # We use size n + 1 because prefix[0] represents an empty prefix.
        prefix: List[int] = [0] * (n + 1)
        for i in range(n):
            prefix[i + 1] = prefix[i] + reports[i]

        # dp[i] = maximum bonus for the first i elements, i.e. reports[0:i]
        #
        # dp[0] = 0 because an empty array contributes no bonus.
        #
        # We initialize all other values to a very small number because we are
        # maximizing, and values can be negative due to negative report scores.
        negative_infinity: int = -(10**30)
        dp: List[int] = [negative_infinity] * (n + 1)
        dp[0] = 0

        # We build the answer for larger and larger prefixes.
        #
        # For each i, we want to determine the best way to partition reports[0:i].
        # The final block of that partition must end at i - 1.
        for i in range(1, n + 1):
            # Try every possible starting index j for the last block.
            #
            # That means the last block is reports[j:i], where:
            # - j ranges from 0 to i - 1
            # - block length is i - j
            # - block sum is prefix[i] - prefix[j]
            #
            # Then:
            # candidate total = best partition of reports[0:j] + bonus of reports[j:i]
            for j in range(i):
                block_sum: int = prefix[i] - prefix[j]
                block_length: int = i - j
                block_bonus: int = block_sum * block_length

                # Combine the best result for the prefix before this block
                # with the bonus contributed by this chosen last block.
                candidate: int = dp[j] + block_bonus

                # Keep the best possible value for dp[i].
                if candidate > dp[i]:
                    dp[i] = candidate

        # dp[n] represents the best partition for the entire array.
        return dp[n]

    def solve(self, reports: List[int]) -> int:
        """
        Wrapper method that calls the main dynamic programming solution.

        Args:
            reports: List of daily engineering output scores.

        Returns:
            The maximum total bonus obtainable.

        Time complexity:
            O(n^2)

        Space complexity:
            O(n)
        """
        return self.max_bonus(reports)


if __name__ == "__main__":
    solution = Solution()

    # Example 1 from the prompt.
    # reports = [3, -1, 2]
    # Whole array sum = 4, length = 3, bonus = 12
    # Best answer should be 12.
    reports1: List[int] = [3, -1, 2]
    result1: int = solution.solve(reports1)
    print(f"reports = {reports1}")
    print(f"Maximum bonus = {result1}")
    print("Expected = 12")
    print()

    # Example 2 from the prompt.
    # reports = [4, -5, 6, 1]
    # Whole array sum = 6, length = 4, bonus = 24
    # Best answer should be 24.
    reports2: List[int] = [4, -5, 6, 1]
    result2: int = solution.solve(reports2)
    print(f"reports = {reports2}")
    print(f"Maximum bonus = {result2}")
    print("Expected = 24")
    print()

    # Additional small sanity checks for beginners to observe behavior.
    extra_tests: List[List[int]] = [
        [5],
        [-3],
        [1, 2, 3],
        [-1, -2, -3],
        [2, -1, 2, -1, 2],
    ]

    for test in extra_tests:
        print(f"reports = {test}")
        print(f"Maximum bonus = {solution.solve(test)}")
        print()