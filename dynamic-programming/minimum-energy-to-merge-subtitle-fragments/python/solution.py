"""
Title: Minimum Energy to Merge Subtitle Fragments

Problem Description:
You are given a list of subtitle fragments extracted from a long video. The fragments
must be merged in their original order into a single final transcript. Merging fragments
is expensive because each merge requires re-rendering and timing adjustments.

Suppose the length of fragment i is lengths[i]. You may only merge adjacent groups of
fragments. When you merge one contiguous group of fragments into a single larger group,
the energy cost of that operation is equal to the total number of characters in that group.
After the merge, that group behaves like a single fragment and can be merged again later.

Your task is to compute the minimum total energy needed to merge all fragments into
exactly one fragment.

Example:
If the fragments have lengths [4, 2, 7], one option is:
- Merge 4 and 2 first: cost = 6, new fragments = [6, 7]
- Merge 6 and 7 next: cost = 13
Total = 19

A different merge order may cost more, so we must find the minimum possible total.

This is a classic interval dynamic programming problem:
- We consider every subarray / interval of fragments.
- For each interval, we try every possible split point.
- We use prefix sums so the total length of any interval can be computed quickly.
"""

from typing import List


class Solution:
    def min_merge_energy(self, lengths: List[int]) -> int:
        """
        Compute the minimum total energy required to merge all subtitle fragments
        into exactly one fragment.

        Args:
            lengths: A list where lengths[i] is the length of the i-th fragment.

        Returns:
            The minimum total energy needed to merge all fragments into one.

        Time Complexity:
            O(n^3)
            - There are O(n^2) intervals.
            - For each interval, we try O(n) split points.

        Space Complexity:
            O(n^2)
            - We store a DP table for all intervals.
        """
        # Number of fragments.
        n: int = len(lengths)

        # If there is only one fragment, no merge is needed, so the cost is 0.
        if n <= 1:
            return 0

        # ---------------------------------------------------------------------
        # STEP 1: Build prefix sums.
        #
        # Why?
        # We frequently need the total length of a contiguous interval [i..j].
        # The merge cost for combining all fragments in that interval is exactly
        # the sum of their lengths.
        #
        # Prefix sums let us compute:
        #   sum(lengths[i..j]) = prefix[j + 1] - prefix[i]
        #
        # in O(1) time instead of O(n).
        # ---------------------------------------------------------------------
        prefix: List[int] = [0] * (n + 1)
        for i in range(n):
            prefix[i + 1] = prefix[i] + lengths[i]

        # ---------------------------------------------------------------------
        # STEP 2: Create the DP table.
        #
        # dp[i][j] will store the minimum cost to merge the subarray
        # lengths[i..j] into one single fragment.
        #
        # Base case:
        #   dp[i][i] = 0
        # because a single fragment is already "merged".
        #
        # We initialize all values to 0 first. We will fill only the upper-right
        # triangle of the table where i <= j.
        # ---------------------------------------------------------------------
        dp: List[List[int]] = [[0] * n for _ in range(n)]

        # ---------------------------------------------------------------------
        # STEP 3: Fill the DP table by increasing interval length.
        #
        # Why increasing length?
        # To compute dp[i][j], we need smaller intervals such as dp[i][k]
        # and dp[k+1][j]. Therefore, those smaller intervals must already be
        # computed before we process the larger interval.
        #
        # interval_len is the number of fragments in the current interval.
        # We start from 2 because intervals of length 1 are base cases.
        # ---------------------------------------------------------------------
        for interval_len in range(2, n + 1):
            # Try every starting index i for this interval length.
            for i in range(0, n - interval_len + 1):
                # Compute the ending index j.
                j: int = i + interval_len - 1

                # Compute the total length of fragments from i to j.
                # This is the cost paid when the two merged halves are finally
                # combined into one fragment.
                interval_sum: int = prefix[j + 1] - prefix[i]

                # Start with a very large number so any real answer is smaller.
                best_cost: int = float("inf")

                # -----------------------------------------------------------------
                # STEP 4: Try every possible split point.
                #
                # If we split the interval [i..j] at k, then:
                # - Left side is [i..k]
                # - Right side is [k+1..j]
                #
                # First, we optimally merge the left side: cost = dp[i][k]
                # Then, we optimally merge the right side: cost = dp[k+1][j]
                # Finally, we merge those two resulting groups together:
                # cost = sum(lengths[i..j]) = interval_sum
                #
                # So:
                #   dp[i][j] = min(
                #       dp[i][k] + dp[k+1][j] + interval_sum
                #   ) for all k in [i, j-1]
                # -----------------------------------------------------------------
                for k in range(i, j):
                    current_cost: int = dp[i][k] + dp[k + 1][j] + interval_sum
                    if current_cost < best_cost:
                        best_cost = current_cost

                # Store the best possible cost for this interval.
                dp[i][j] = best_cost

        # The answer for the whole array is the cost to merge [0..n-1].
        return dp[0][n - 1]

    def minimumEnergy(self, lengths: List[int]) -> int:
        """
        Wrapper method matching a common interview-style naming convention.

        Args:
            lengths: A list where lengths[i] is the length of the i-th fragment.

        Returns:
            The minimum total energy needed to merge all fragments into one.

        Time Complexity:
            O(n^3)

        Space Complexity:
            O(n^2)
        """
        return self.min_merge_energy(lengths)


if __name__ == "__main__":
    solution = Solution()

    # Example 1:
    # lengths = [4, 2, 7]
    # Optimal:
    # - Merge [4,2] => cost 6
    # - Merge [6,7] => cost 13
    # Total = 19
    sample_1: List[int] = [4, 2, 7]
    result_1: int = solution.minimumEnergy(sample_1)
    print(f"Input: {sample_1}")
    print(f"Minimum total energy: {result_1}")
    print("Expected: 19")
    print()

    # Example 2:
    # lengths = [3, 5, 1, 2]
    # One optimal strategy:
    # - Merge [1,2] => cost 3, array becomes conceptually [3,5,3]
    # - Merge [5,3] => cost 8
    # - Merge [3,8] => cost 11
    # Total = 22
    sample_2: List[int] = [3, 5, 1, 2]
    result_2: int = solution.minimumEnergy(sample_2)
    print(f"Input: {sample_2}")
    print(f"Minimum total energy: {result_2}")
    print("Expected: 22")
    print()

    # Additional small sanity checks for beginners:
    sample_3: List[int] = [10]
    result_3: int = solution.minimumEnergy(sample_3)
    print(f"Input: {sample_3}")
    print(f"Minimum total energy: {result_3}")
    print("Expected: 0")
    print()

    sample_4: List[int] = [1, 2]
    result_4: int = solution.minimumEnergy(sample_4)
    print(f"Input: {sample_4}")
    print(f"Minimum total energy: {result_4}")
    print("Expected: 3")