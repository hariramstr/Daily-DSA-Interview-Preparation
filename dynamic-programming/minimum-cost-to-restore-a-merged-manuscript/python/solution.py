"""
Title: Minimum Cost to Restore a Merged Manuscript

Problem Description:
A digital archive stores an original manuscript as a string `target`. During a faulty
backup process, the manuscript was split into reusable text fragments. You are given
an array `fragments`, where `fragments[i]` is a non-empty string and `cost[i]` is the
cost to place that fragment into the restoration plan. You may use any fragment any
number of times.

Your task is to reconstruct `target` exactly from left to right by concatenating chosen
fragments. Every chosen fragment must match the next characters of `target` at the
position where it is placed. The total restoration cost is the sum of the costs of all
fragments used. Return the minimum possible total cost to build the entire `target`,
or `-1` if it is impossible.

Two fragments may have identical text but different costs, and they should be treated
as separate options. Because fragments may be reused unlimited times, a locally cheap
choice is not always globally optimal. This makes the problem a dynamic programming
problem over prefixes of the target string.

Constraints:
- 1 <= target.length <= 5000
- 1 <= fragments.length <= 1000
- 1 <= fragments[i].length <= 50
- target and all fragments[i] consist only of lowercase English letters
- 1 <= cost[i] <= 10^6
"""

from typing import Dict, List


class Solution:
    def _compress_fragments(self, fragments: List[str], cost: List[int]) -> Dict[str, int]:
        """
        Build a dictionary that keeps only the cheapest cost for each distinct fragment text.

        This optimization is safe because if the same fragment text appears multiple times,
        only the minimum cost version is ever useful. Since fragments can be reused unlimited
        times, a more expensive duplicate can never improve the answer.

        Args:
            fragments: List of available fragment strings.
            cost: List of costs corresponding to each fragment.

        Returns:
            A dictionary mapping fragment text -> minimum cost for that exact text.

        Time complexity:
            O(m), where m is the number of fragments.

        Space complexity:
            O(u), where u is the number of unique fragment strings.
        """
        best_cost_by_fragment: Dict[str, int] = {}

        for text, c in zip(fragments, cost):
            if text not in best_cost_by_fragment or c < best_cost_by_fragment[text]:
                best_cost_by_fragment[text] = c

        return best_cost_by_fragment

    def minimum_cost(self, target: str, fragments: List[str], cost: List[int]) -> int:
        """
        Compute the minimum total cost needed to build the target string exactly.

        The algorithm uses dynamic programming over prefixes:
        - dp[i] = minimum cost to build target[:i]
        - Start with dp[0] = 0 because building an empty prefix costs nothing
        - From each reachable position i, try every fragment that matches target at i
        - If a fragment of length L matches, update dp[i + L]

        To make matching efficient and beginner-friendly:
        - First compress duplicate fragment texts to their cheapest cost
        - Group fragments by their starting character, so at position i we only test
          fragments that could possibly match target[i]

        Args:
            target: The manuscript string we want to reconstruct.
            fragments: Available reusable fragment strings.
            cost: Cost for each fragment.

        Returns:
            The minimum total cost to form the entire target, or -1 if impossible.

        Time complexity:
            In the worst case, O(n * k * L), where:
            - n = len(target)
            - k = number of candidate fragments checked per position
            - L = maximum fragment length
            Since fragment length is at most 50, this is practical for the constraints.

        Space complexity:
            O(n + u), where:
            - n = len(target) for the DP array
            - u = number of unique fragments stored after compression
        """
        # Step 1:
        # Reduce duplicate fragment texts to their cheapest cost.
        #
        # Example:
        # fragments = ["ab", "ab", "ra"], cost = [5, 4, 2]
        # We only need to keep:
        # "ab" -> 4
        # "ra" -> 2
        #
        # Why this is correct:
        # If two fragments have identical text, they behave exactly the same during matching.
        # The only difference is cost. Therefore, only the cheapest one matters.
        best_cost_by_fragment: Dict[str, int] = self._compress_fragments(fragments, cost)

        # Step 2:
        # Group fragments by their first character.
        #
        # Why do this?
        # At position i in the target, if target[i] == 'a', then only fragments starting
        # with 'a' can possibly match there. This avoids checking obviously impossible
        # fragments and makes the DP transitions faster.
        #
        # Structure:
        # by_first_char['a'] = [("ab", 4), ("apple", 5), ("a", 1), ...]
        by_first_char: Dict[str, List[tuple[str, int]]] = {}

        for text, c in best_cost_by_fragment.items():
            first_char: str = text[0]
            if first_char not in by_first_char:
                by_first_char[first_char] = []
            by_first_char[first_char].append((text, c))

        # Step 3:
        # Create the DP array.
        #
        # dp[i] means:
        # "the minimum cost required to build the prefix target[:i]"
        #
        # So:
        # dp[0] = 0, because the empty prefix needs no fragments
        # dp[1], dp[2], ..., dp[n] start as infinity (unreachable)
        n: int = len(target)
        inf: int = 10**18
        dp: List[int] = [inf] * (n + 1)
        dp[0] = 0

        # Step 4:
        # Process positions from left to right.
        #
        # If dp[i] is infinity, that means target[:i] cannot be formed, so there is no
        # valid way to continue from that position.
        #
        # Otherwise, we try to place every fragment that matches target starting at i.
        for i in range(n):
            # If this prefix is unreachable, skip it.
            if dp[i] == inf:
                continue

            # Only fragments starting with the current target character can match here.
            current_char: str = target[i]
            if current_char not in by_first_char:
                continue

            # Try each candidate fragment.
            for text, fragment_cost in by_first_char[current_char]:
                length: int = len(text)
                next_index: int = i + length

                # If the fragment would go past the end of the target, it cannot be used here.
                if next_index > n:
                    continue

                # Check whether this fragment exactly matches the next part of the target.
                #
                # Example:
                # target = "applepenapple"
                # i = 0
                # text = "apple"
                # target[0:5] == "apple" -> match
                #
                # If it matches, we can transition:
                # dp[next_index] = min(dp[next_index], dp[i] + fragment_cost)
                if target[i:next_index] == text:
                    new_cost: int = dp[i] + fragment_cost
                    if new_cost < dp[next_index]:
                        dp[next_index] = new_cost

        # Step 5:
        # If dp[n] is still infinity, the full target cannot be formed.
        # Otherwise, dp[n] is the minimum total cost.
        return -1 if dp[n] == inf else dp[n]


if __name__ == "__main__":
    solution = Solution()

    # Sample 1
    target1 = "abracadabra"
    fragments1 = ["ab", "ra", "cad", "a", "bra"]
    cost1 = [4, 2, 5, 1, 3]
    result1 = solution.minimum_cost(target1, fragments1, cost1)
    print("Sample 1 Result:", result1)

    # Sample 2
    target2 = "applepenapple"
    fragments2 = ["apple", "pen", "app", "lepen"]
    cost2 = [5, 2, 3, 10]
    result2 = solution.minimum_cost(target2, fragments2, cost2)
    print("Sample 2 Result:", result2)

    # Additional quick checks
    target3 = "aaaa"
    fragments3 = ["a", "aa", "aaa"]
    cost3 = [3, 4, 10]
    result3 = solution.minimum_cost(target3, fragments3, cost3)
    print("Additional Check 1:", result3)

    target4 = "abc"
    fragments4 = ["a", "bc", "d"]
    cost4 = [1, 2, 3]
    result4 = solution.minimum_cost(target4, fragments4, cost4)
    print("Additional Check 2:", result4)