"""
Title: Minimum Latency to Decode a Layered Signal Tape

Problem Description:
A telemetry system stores a long signal tape as a string s of length n, where each
character is an uppercase letter representing a frequency band.

To decode the tape, a hardware decoder may process any contiguous segment [l, r]
in one pass if the first and last characters of that segment are the same.
During that pass, the decoder resolves both matching endpoints together, and the
inside of the segment may be decoded before, after, or split across additional
passes. The latency of one pass is equal to the length of the segment being
processed, i.e. r - l + 1.

We must compute the minimum total latency required to fully decode the entire tape.

Equivalent recursive interpretation:
- Every character must be matched exactly once.
- A single character may be decoded alone with cost 1.
- If s[l] == s[r], then we may decode those two endpoints together by paying
  (r - l + 1), while the inside interval (l + 1, r - 1) is decoded recursively.
- Different independent parts of the string may also be split and solved separately.

This naturally leads to interval dynamic programming.

Constraints:
- 1 <= n <= 400
- s consists only of uppercase English letters
- The answer fits in a 32-bit signed integer
"""

from typing import List


class Solution:
    def min_latency(self, s: str) -> int:
        """
        Compute the minimum total latency to decode the entire tape.

        We use interval dynamic programming:
        dp[l][r] = minimum cost to fully decode substring s[l:r+1].

        Transition:
        1. Split the interval into two independent parts:
           dp[l][r] = min(dp[l][k] + dp[k+1][r]) for l <= k < r

        2. If the two ends match, we may decode them together in one pass:
           cost = (r - l + 1) + dp[l+1][r-1]
           This corresponds exactly to resolving the endpoints together and
           recursively decoding the inside.

        Base:
        dp[i][i] = 1

        Args:
            s: The signal tape string.

        Returns:
            Minimum total latency.

        Time complexity:
            O(n^3)

        Space complexity:
            O(n^2)
        """
        n: int = len(s)

        # Edge case: although constraints guarantee n >= 1, handling n == 0
        # makes the method more robust and easier to reuse.
        if n == 0:
            return 0

        # Create a 2D DP table.
        #
        # dp[l][r] will store the minimum cost to decode substring s[l..r].
        # We initialize everything to 0 first, then fill valid intervals.
        dp: List[List[int]] = [[0] * n for _ in range(n)]

        # Base case:
        # A single character can always be decoded alone.
        # The segment length is 1, so the cost is exactly 1.
        for i in range(n):
            dp[i][i] = 1

        # We now process intervals in increasing order of length.
        #
        # This is crucial for interval DP:
        # when computing dp[l][r], we may need values like:
        # - dp[l][k]
        # - dp[k+1][r]
        # - dp[l+1][r-1]
        #
        # All of those correspond to strictly smaller intervals, so they must
        # already be computed before dp[l][r].
        for length in range(2, n + 1):
            # For each interval length, slide the window across the string.
            for l in range(0, n - length + 1):
                r: int = l + length - 1

                # Start with a very large value.
                # We will minimize over all valid ways to decode s[l..r].
                best: int = 10**18

                # Option 1: split the interval into two independent subproblems.
                #
                # Why is this valid?
                # Because the decoding process may be organized so that one part
                # is fully decoded independently of the other part. Since total
                # cost adds across passes, the combined cost is simply the sum.
                #
                # We try every possible split point k:
                # [l..k] and [k+1..r]
                for k in range(l, r):
                    candidate: int = dp[l][k] + dp[k + 1][r]
                    if candidate < best:
                        best = candidate

                # Option 2: decode the two endpoints together, but only if they match.
                #
                # If s[l] == s[r], then one valid pass is the whole segment [l, r].
                # That pass costs its length: (r - l + 1).
                #
                # After resolving the endpoints, the inside substring s[l+1..r-1]
                # still needs to be decoded recursively.
                #
                # Special case:
                # If length == 2, then the inside is empty, so its cost is 0.
                if s[l] == s[r]:
                    inside_cost: int = dp[l + 1][r - 1] if l + 1 <= r - 1 else 0
                    candidate = (r - l + 1) + inside_cost
                    if candidate < best:
                        best = candidate

                # Store the best answer for this interval.
                dp[l][r] = best

        # The answer for the whole string is the interval covering everything.
        return dp[0][n - 1]

    def minimumLatency(self, s: str) -> int:
        """
        Wrapper method using a camelCase name for convenience.

        Args:
            s: The signal tape string.

        Returns:
            Minimum total latency.

        Time complexity:
            O(n^3)

        Space complexity:
            O(n^2)
        """
        return self.min_latency(s)


if __name__ == "__main__":
    solution = Solution()

    # Sample inputs from the problem statement.
    samples: List[str] = [
        "ABCA",
        "ABBA",
    ]

    for tape in samples:
        result = solution.minimumLatency(tape)
        print(f"s = {tape!r} -> minimum latency = {result}")

    # Expected:
    # "ABCA" -> 6
    #   Outer A...A costs 4, then B costs 1, C costs 1 => total 6
    #
    # "ABBA" -> 6
    #   Outer A...A costs 4, inner B...B costs 2 => total 6