"""
Title: Minimum Cost to Stitch a Banner from Overlapping Slogans

Problem Description:
You are given a target banner string `target` that a marketing system must assemble exactly
from left to right. You also receive `n` slogan fragments. The `i`-th fragment is a string
`fragments[i]` with an associated usage cost `cost[i]`. You may use any fragment any number
of times.

When placing a fragment, it must match a contiguous substring of `target` starting at some
position `p`. However, unlike simple concatenation, the new fragment is allowed to overlap
the already-built banner by up to `k` characters. More formally, if you have already built
the prefix `target[0...p-1]`, then you may place a fragment of length `L` so that its first
`t` characters overlap the suffix `target[p-t...p-1]` for some
`0 <= t <= min(k, L-1, p)`, and its full content still matches `target[p-t...p-t+L-1]`.
After placing it, the built prefix extends to `max(p, p-t+L)`. Each placement adds `cost[i]`.

Return the minimum total cost to build the entire `target`, or `-1` if it is impossible.

This is a weighted string-construction problem where the best solution may require
intentionally using overlaps to reduce total cost. Greedy choices are not sufficient because
a cheaper fragment now may block a more efficient overlap later.
"""

from typing import Dict, List, Tuple


class Solution:
    def _compress_fragments(self, fragments: List[str], costs: List[int]) -> List[Tuple[str, int]]:
        """
        Keep only the cheapest cost for each distinct fragment string.

        Args:
            fragments: List of fragment strings.
            costs: Matching list of usage costs.

        Returns:
            A list of (fragment, minimum_cost_for_that_fragment).

        Time complexity:
            O(n)

        Space complexity:
            O(n)
        """
        best_cost_by_fragment: Dict[str, int] = {}

        # If the same fragment text appears multiple times, only the cheapest one matters.
        # Since we can reuse fragments unlimited times, a more expensive duplicate is never useful.
        for fragment, cost in zip(fragments, costs):
            if fragment not in best_cost_by_fragment or cost < best_cost_by_fragment[fragment]:
                best_cost_by_fragment[fragment] = cost

        return list(best_cost_by_fragment.items())

    def minimum_cost(self, target: str, fragments: List[str], costs: List[int], k: int) -> int:
        """
        Compute the minimum total cost to build the target exactly using reusable fragments
        with allowed overlap up to k characters.

        Args:
            target: The banner string to build.
            fragments: Available fragment strings.
            costs: Usage cost for each fragment.
            k: Maximum allowed overlap length.

        Returns:
            The minimum total cost, or -1 if impossible.

        Time complexity:
            O(U * m * (n + m)) in the implemented form, where:
            - U is the number of unique fragments after compression
            - m is the maximum fragment length (<= 50)
            - n is len(target) (<= 5000)
            In practice this is efficient because m and k are both small (<= 50).

        Space complexity:
            O(n + U * m)
        """
        n: int = len(target)
        if n == 0:
            return 0

        # Step 1:
        # Remove duplicate fragment strings by keeping only the cheapest cost for each text.
        # This reduces unnecessary repeated work later.
        unique_fragments: List[Tuple[str, int]] = self._compress_fragments(fragments, costs)

        # Step 2:
        # For every possible "start index" s in target, we want to know which fragment placements
        # can start exactly at s and how far they extend.
        #
        # Why "start index"?
        # A placement with overlap t from current built prefix length p actually starts at s = p - t.
        # The fragment must match target[s : s + L].
        # If it matches, then after placing it, the built prefix becomes s + L.
        #
        # So each valid placement can be represented simply as:
        #   start = s
        #   end = s + L
        #   cost = fragment_cost
        #
        # Then from a current built prefix p, we are allowed to use any placement whose start s
        # lies in the range [max(0, p-k), p], because that means overlap t = p - s is between
        # 0 and k. Also t must be <= L-1, but if end > p then automatically L > t.
        #
        # We store placements grouped by start index:
        #   placements_by_start[s] = list of (end, cost)
        placements_by_start: List[List[Tuple[int, int]]] = [[] for _ in range(n)]

        # Step 3:
        # Enumerate every unique fragment and every possible target position where it could match.
        #
        # Because fragment length <= 50, direct character comparison is acceptable.
        for fragment, fragment_cost in unique_fragments:
            length: int = len(fragment)
            if length > n:
                continue

            # Try matching this fragment at every possible start position.
            for start in range(0, n - length + 1):
                # Check whether fragment == target[start : start + length].
                # We do manual comparison to avoid creating many substring objects.
                matches: bool = True
                for offset in range(length):
                    if target[start + offset] != fragment[offset]:
                        matches = False
                        break

                if matches:
                    end: int = start + length
                    placements_by_start[start].append((end, fragment_cost))

        # Step 4:
        # Dynamic programming over built prefix length.
        #
        # dp[p] = minimum cost to have built exactly target[0:p]
        #
        # Initial state:
        #   dp[0] = 0 because building an empty prefix costs nothing.
        #
        # Transition:
        #   From dp[p], we may choose any placement starting at s where s is in [p-k, p].
        #   Such a placement overlaps by t = p - s characters.
        #   If the placement ends at e > p, then it extends the built prefix to e.
        #
        # Important detail:
        #   A placement that ends at e <= p does not help because it does not extend the built prefix.
        #   Reusing such a placement would only add cost without progress, so we ignore it.
        INF: int = 10**30
        dp: List[int] = [INF] * (n + 1)
        dp[0] = 0

        # We process p from left to right.
        # Since every useful transition strictly increases the built prefix length,
        # this order is valid and complete.
        for built_prefix in range(n + 1):
            if dp[built_prefix] == INF:
                continue

            # The next fragment may start anywhere from built_prefix - k up to built_prefix.
            # Starting earlier means using overlap.
            left_start: int = max(0, built_prefix - k)
            right_start: int = min(built_prefix, n - 1)

            for start in range(left_start, right_start + 1):
                # overlap = built_prefix - start
                # This is automatically in [0, k] because of the chosen range.
                #
                # Now inspect every fragment placement that matches target starting at `start`.
                for end, fragment_cost in placements_by_start[start]:
                    # The placement must actually extend the current built prefix.
                    # If end <= built_prefix, it contributes no new characters.
                    if end <= built_prefix:
                        continue

                    new_cost: int = dp[built_prefix] + fragment_cost
                    if new_cost < dp[end]:
                        dp[end] = new_cost

        return -1 if dp[n] == INF else dp[n]


if __name__ == "__main__":
    solution = Solution()

    # Example 1
    target1 = "ababa"
    fragments1 = ["aba", "baba", "ab"]
    costs1 = [4, 5, 2]
    k1 = 2
    result1 = solution.minimum_cost(target1, fragments1, costs1, k1)
    print(result1)  # Expected: 6

    # Example 2
    target2 = "aaaaab"
    fragments2 = ["aaa", "aaab", "b"]
    costs2 = [3, 4, 10]
    k2 = 2
    result2 = solution.minimum_cost(target2, fragments2, costs2, k2)
    print(result2)  # Expected: 7