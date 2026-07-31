"""
Minimum Fatigue to Type a Macro Script

You are building an editor for a programmable keypad. A script is a string `s` of
lowercase English letters that must be produced exactly from left to right.

The editor supports two actions:

1. Type(c):
   Append character `c` to the output. This costs `typeCost[c]` fatigue.

2. Define(l, r):
   If the substring `s[l..r]` has already appeared earlier somewhere completely
   inside `s[0..l-1]`, you may store that substring as a macro at zero cost.

3. Use(l, r):
   Append a previously defined macro equal to `s[l..r]` in one action, costing
   `macroCost` fatigue regardless of the substring length.

A macro can only be used after it has been defined, and a definition is only valid
if an identical substring occurred earlier in the already produced prefix. You may
define and use any number of macros, and different occurrences of the same text count
as the same macro content.

Return the minimum total fatigue needed to produce the entire script.

Formally, when you are about to produce position `i`, you may either type `s[i]`,
or choose any `j >= i` such that substring `s[i..j]` has appeared as a contiguous
substring entirely within `s[0..i-1]`; in that case you may define it if needed and
then use it for cost `macroCost`, advancing to `j + 1`.

Constraints:
- 1 <= s.length <= 2000
- s consists only of lowercase English letters
- typeCost.length == 26
- 1 <= typeCost[k] <= 10^6
- 1 <= macroCost <= 10^6
"""

from typing import List


class Solution:
    def _build_lcp(self, s: str) -> List[List[int]]:
        """
        Build the LCP (Longest Common Prefix) table for all suffix pairs.

        lcp[i][j] = length of the longest common prefix of s[i:] and s[j:].

        This allows us to quickly answer:
        "How many characters match if we compare the substring starting at i
        with the substring starting at j?"

        Args:
            s: Input string.

        Returns:
            A 2D list containing LCP values.

        Time complexity:
            O(n^2)

        Space complexity:
            O(n^2)
        """
        n: int = len(s)
        lcp: List[List[int]] = [[0] * (n + 1) for _ in range(n + 1)]

        # We fill from bottom-right toward top-left.
        # Why?
        # Because the recurrence uses lcp[i + 1][j + 1].
        #
        # If s[i] == s[j], then the common prefix length is:
        #   1 + lcp[i + 1][j + 1]
        # Otherwise it is 0.
        for i in range(n - 1, -1, -1):
            row_i = lcp[i]
            row_ip1 = lcp[i + 1]
            for j in range(n - 1, -1, -1):
                if s[i] == s[j]:
                    row_i[j] = 1 + row_ip1[j + 1]

        return lcp

    def minimum_fatigue(self, s: str, typeCost: List[int], macroCost: int) -> int:
        """
        Compute the minimum fatigue needed to produce the entire string.

        Core idea:
        Let dp[i] be the minimum fatigue needed to produce the prefix s[0:i].
        Then from position i, we can:
        1. Type s[i] individually.
        2. Use one macro for any substring s[i:j+1] that already appeared fully
           inside the earlier prefix s[0:i-1].

        The difficult part is efficiently checking which substring lengths are valid
        for macro use at each starting position i.

        We precompute an LCP table so that for any earlier start p < i, we know how
        many characters match between s[p:] and s[i:].
        However, the earlier occurrence must lie completely inside s[0:i-1], so if
        it starts at p, its usable length is at most i - p.
        Therefore, the maximum valid macro length at position i is:

            best_len[i] = max over p in [0, i-1] of min(lcp[p][i], i - p)

        Then from dp[i], we may jump to any dp[i + L] for 1 <= L <= best_len[i]
        with cost macroCost.

        Args:
            s: The target script string.
            typeCost: Cost to type each lowercase letter.
            macroCost: Cost to use any valid macro once.

        Returns:
            The minimum total fatigue.

        Time complexity:
            O(n^2)

        Space complexity:
            O(n^2)
        """
        n: int = len(s)

        # Step 1:
        # Precompute LCP values for all suffix pairs.
        #
        # This is the main string-matching helper structure used by the DP.
        lcp: List[List[int]] = self._build_lcp(s)

        # Step 2:
        # For every position i, compute the maximum macro length that can be used
        # starting at i.
        #
        # Explanation:
        # We are about to produce s[i].
        # A macro s[i:i+L] is valid if the exact same text appeared earlier
        # completely inside s[0:i-1].
        #
        # Suppose that earlier occurrence starts at p, where p < i.
        # Then:
        # - The match length between s[p:] and s[i:] is lcp[p][i].
        # - But the earlier occurrence must end before i, so its length cannot
        #   exceed i - p.
        #
        # Therefore, the usable length contributed by start p is:
        #   min(lcp[p][i], i - p)
        #
        # Taking the maximum over all earlier p gives the longest valid macro
        # we can use at position i.
        #
        # Important observation:
        # If the longest valid macro length is X, then every shorter positive
        # length 1..X is also valid, because a prefix of a previously seen
        # substring is also previously seen.
        best_len: List[int] = [0] * n
        for i in range(n):
            best: int = 0
            for p in range(i):
                candidate: int = lcp[p][i]
                earlier_space: int = i - p
                if candidate > earlier_space:
                    candidate = earlier_space
                if candidate > best:
                    best = candidate
            best_len[i] = best

        # Step 3:
        # Dynamic programming over prefix length.
        #
        # dp[k] = minimum fatigue to produce exactly the first k characters,
        #         i.e. s[0:k].
        #
        # Initial state:
        # dp[0] = 0 because producing an empty prefix costs nothing.
        inf: int = 10**30
        dp: List[int] = [inf] * (n + 1)
        dp[0] = 0

        # We process positions from left to right.
        for i in range(n):
            current_cost: int = dp[i]

            # Option A: Type the next character s[i].
            char_index: int = ord(s[i]) - ord('a')
            typed_cost: int = current_cost + typeCost[char_index]
            if typed_cost < dp[i + 1]:
                dp[i + 1] = typed_cost

            # Option B: Use a macro starting at i.
            #
            # If best_len[i] = X, then any length L in [1, X] is valid.
            # Using any such macro costs exactly macroCost.
            #
            # A naive update would loop over all L from 1 to X and do:
            #   dp[i + L] = min(dp[i + L], dp[i] + macroCost)
            #
            # Since n <= 2000, this O(n^2) total work is perfectly acceptable.
            max_macro_length: int = best_len[i]
            if max_macro_length > 0:
                macro_total: int = current_cost + macroCost
                end_limit: int = i + max_macro_length
                for nxt in range(i + 1, end_limit + 1):
                    if macro_total < dp[nxt]:
                        dp[nxt] = macro_total

        return dp[n]

    def minFatigue(self, s: str, typeCost: List[int], macroCost: int) -> int:
        """
        Public wrapper matching a typical interview / platform style method name.

        Args:
            s: The target script string.
            typeCost: Cost to type each lowercase letter.
            macroCost: Cost to use any valid macro once.

        Returns:
            Minimum total fatigue.

        Time complexity:
            O(n^2)

        Space complexity:
            O(n^2)
        """
        return self.minimum_fatigue(s, typeCost, macroCost)


if __name__ == "__main__":
    solution = Solution()

    # Example 1
    s1 = "ababa"
    type_cost1 = [
        1, 1, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100,
        100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100
    ]
    macro_cost1 = 2
    result1 = solution.minFatigue(s1, type_cost1, macro_cost1)
    print(result1)  # Expected: 5

    # Example 2
    #
    # Note:
    # The narrative in the prompt is inconsistent, but the formal rules define
    # the actual problem. We trust the formal rules and compute the true optimum.
    s2 = "aaaaaa"
    type_cost2 = [
        3, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100,
        100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100
    ]
    macro_cost2 = 4
    result2 = solution.minFatigue(s2, type_cost2, macro_cost2)
    print(result2)

    # Additional small sanity checks
    s3 = "abc"
    type_cost3 = [1] * 26
    macro_cost3 = 10
    result3 = solution.minFatigue(s3, type_cost3, macro_cost3)
    print(result3)  # Expected: 3

    s4 = "aaaa"
    type_cost4 = [2] + [100] * 25
    macro_cost4 = 1
    result4 = solution.minFatigue(s4, type_cost4, macro_cost4)
    print(result4)  # One optimal answer: type "aa" (4), macro "aa" (1) => 5