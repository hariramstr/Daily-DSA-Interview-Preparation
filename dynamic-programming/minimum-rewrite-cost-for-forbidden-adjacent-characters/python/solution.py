"""
Title: Minimum Rewrite Cost for Forbidden Adjacent Characters

Problem Description:
You are given a string s of length n consisting of lowercase English letters. Some pairs
of letters are incompatible and are not allowed to appear next to each other in the final
string. You are also given a non-negative cost matrix changeCost of size 26 x 26, where
changeCost[a][b] is the cost to rewrite character a into character b. Rewriting a character
does not affect any other position, and every position must end as exactly one lowercase letter.

Your task is to compute the minimum total rewrite cost needed to transform s into a new
string t such that for every adjacent pair t[i - 1], t[i], that ordered pair is allowed.
If it is impossible to produce any valid final string, return -1.

The incompatibility rules are provided as a list of forbidden ordered pairs. If (x, y) is
forbidden, then x cannot be immediately followed by y. Note that (x, y) and (y, x) are
different constraints.

This is a global optimization problem: choosing the cheapest letter for one position may
force expensive choices later, so greedy methods do not work in general.

Constraints:
- 1 <= n <= 100000
- s consists only of lowercase English letters
- 0 <= changeCost[i][j] <= 10^9
- 0 <= number of forbidden pairs <= 26 * 26
- The answer may exceed 32-bit integer range
"""

from typing import List


class Solution:
    def _char_to_index(self, ch: str) -> int:
        """
        Convert a lowercase English letter into its 0-based alphabet index.

        Args:
            ch: A single lowercase English letter.

        Returns:
            Integer index in the range [0, 25].

        Time complexity:
            O(1)

        Space complexity:
            O(1)
        """
        return ord(ch) - ord("a")

    def minimum_rewrite_cost(
        self,
        s: str,
        forbiddenPairs: List[List[str]],
        changeCost: List[List[int]],
    ) -> int:
        """
        Compute the minimum total rewrite cost so that no adjacent ordered pair is forbidden.

        This uses dynamic programming over positions and final chosen letters:
        - dp_prev[c] = minimum cost to rewrite the processed prefix so that the last letter is c
        - For each new position, try every possible current letter and every possible previous
          letter that can legally transition into it.

        Because the alphabet size is fixed at 26, the O(n * 26 * 26) dynamic programming
        solution is efficient enough even for n = 100000.

        Args:
            s: Original string consisting of lowercase English letters.
            forbiddenPairs: List of forbidden ordered adjacent pairs, each represented as
                [x, y], meaning x cannot be immediately followed by y.
            changeCost: 26 x 26 matrix where changeCost[a][b] is the cost to rewrite
                letter with index a into letter with index b.

        Returns:
            The minimum total rewrite cost as an integer, or -1 if no valid final string exists.

        Time complexity:
            O(n * 26 * 26), which is effectively O(n) because 26 is constant.

        Space complexity:
            O(26 * 26) for the allowed-transition table plus O(26) for DP arrays, which is O(1)
            with respect to n.
        """
        n: int = len(s)

        # We build a 26 x 26 boolean table named "allowed".
        # allowed[a][b] == True means letter a is allowed to be followed by letter b.
        # We start by assuming every ordered pair is allowed, then mark forbidden ones as False.
        allowed: List[List[bool]] = [[True] * 26 for _ in range(26)]

        # Apply all forbidden constraints.
        # Since constraints are ordered, only the exact direction is blocked.
        for pair in forbiddenPairs:
            first_idx: int = self._char_to_index(pair[0])
            second_idx: int = self._char_to_index(pair[1])
            allowed[first_idx][second_idx] = False

        # Convert the original string into integer indices once.
        # This avoids repeated ord() calls inside the main DP loop.
        source_indices: List[int] = [self._char_to_index(ch) for ch in s]

        # A very large value used to represent "currently impossible".
        # Python integers are unbounded, so this is safe.
        inf: int = 10**30

        # Base case for position 0:
        # If we decide the final first character is c, the cost is simply the rewrite cost
        # from the original first character to c. There is no adjacency constraint yet because
        # the first character has no previous neighbor.
        first_original: int = source_indices[0]
        dp_prev: List[int] = [changeCost[first_original][c] for c in range(26)]

        # Process positions 1 through n - 1.
        for pos in range(1, n):
            original_char_idx: int = source_indices[pos]

            # dp_curr[curr] will store the minimum total cost for the prefix s[0..pos]
            # if the final chosen letter at position pos is "curr".
            dp_curr: List[int] = [inf] * 26

            # We now try every possible final letter for the current position.
            for curr in range(26):
                # Cost to rewrite the current original character into "curr".
                rewrite_cost: int = changeCost[original_char_idx][curr]

                # We need the best previous letter "prev" such that:
                # 1) The prefix ending with prev was achievable
                # 2) The ordered pair (prev, curr) is allowed
                #
                # Then:
                # dp_curr[curr] = min(dp_prev[prev] + rewrite_cost)
                best_total: int = inf

                # Since alphabet size is only 26, checking all previous letters is cheap.
                for prev in range(26):
                    if allowed[prev][curr]:
                        candidate: int = dp_prev[prev] + rewrite_cost
                        if candidate < best_total:
                            best_total = candidate

                dp_curr[curr] = best_total

            # Move current row into previous row for the next iteration.
            dp_prev = dp_curr

        # The answer is the cheapest valid final letter at the last position.
        answer: int = min(dp_prev)

        # If every state is impossible, return -1.
        return -1 if answer >= inf else answer


if __name__ == "__main__":
    solution = Solution()

    # Example 1 setup:
    # We create a 26 x 26 matrix where:
    # - identity changes cost 0
    # - listed special changes have custom costs
    # - all other non-diagonal changes cost 5
    change_cost_example_1: List[List[int]] = [[0 if i == j else 5 for j in range(26)] for i in range(26)]

    def idx(ch: str) -> int:
        return ord(ch) - ord("a")

    change_cost_example_1[idx("a")][idx("c")] = 2
    change_cost_example_1[idx("b")][idx("a")] = 3
    change_cost_example_1[idx("b")][idx("d")] = 1
    change_cost_example_1[idx("c")][idx("a")] = 4

    s1: str = "abca"
    forbidden_pairs_1: List[List[str]] = [["a", "b"], ["b", "c"]]

    result_1: int = solution.minimum_rewrite_cost(s1, forbidden_pairs_1, change_cost_example_1)
    print(result_1)  # Expected: 3

    # Example 2 setup:
    # Every ordered pair is forbidden, so any string of length > 1 is impossible.
    change_cost_example_2: List[List[int]] = [[0 if i == j else 7 for j in range(26)] for i in range(26)]
    forbidden_pairs_2: List[List[str]] = [[chr(ord("a") + i), chr(ord("a") + j)] for i in range(26) for j in range(26)]
    s2: str = "aaa"

    result_2: int = solution.minimum_rewrite_cost(s2, forbidden_pairs_2, change_cost_example_2)
    print(result_2)  # Expected: -1