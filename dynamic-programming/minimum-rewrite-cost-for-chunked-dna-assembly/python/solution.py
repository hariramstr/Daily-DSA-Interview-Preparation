"""
Minimum Rewrite Cost for Chunked DNA Assembly

Problem Description:
A bioinformatics pipeline is assembling a target DNA string from a catalog of reusable
fragments. You are given a target string `target` of length `n` and a list of fragments
`parts`, where each fragment `parts[i]` has an associated non-negative rewrite cost
`cost[i]`. You may build the target from left to right by repeatedly choosing any
fragment and placing it over the next uncovered portion of the target. A fragment can
only be placed if its characters exactly match the corresponding substring of `target`.
Fragments may be reused any number of times.

However, each time you use a fragment whose length is different from the length of the
fragment used immediately before it, the assembly machine must be recalibrated. This
adds an extra penalty `switchCost` to that placement, except for the very first
fragment, which never pays a switch penalty.

Return the minimum total cost needed to assemble the entire target exactly. If it is
impossible, return `-1`.

Formally, if you place fragments with indices f1, f2, ..., fk, such that their
concatenation equals `target`, then the total cost is:

cost[f1] + cost[f2] + ... + cost[fk]
+ switchCost * (# of i from 2..k where len(parts[fi]) != len(parts[f(i-1)]))

Constraints:
- 1 <= n <= 10^4
- 1 <= parts.length <= 2 * 10^4
- 1 <= parts[i].length <= 50
- sum(parts[i].length) <= 2 * 10^5
- 0 <= cost[i] <= 10^9
- 0 <= switchCost <= 10^9
- `target` and all `parts[i]` consist only of uppercase letters `A`, `C`, `G`, `T`
"""

from typing import Dict, List, Tuple


class Solution:
    def _build_min_cost_by_string(
        self, parts: List[str], cost: List[int]
    ) -> Dict[str, int]:
        """
        Build a dictionary that stores the minimum cost for each distinct fragment string.

        If the same fragment text appears multiple times with different costs, only the
        cheapest one matters because fragments are reusable and there is never a reason
        to choose a more expensive copy of the exact same string.

        Args:
            parts: List of fragment strings.
            cost: List of costs aligned with `parts`.

        Returns:
            A dictionary mapping fragment string -> minimum cost among duplicates.

        Time complexity:
            O(m), where m is the number of fragments.

        Space complexity:
            O(u), where u is the number of distinct fragment strings.
        """
        min_cost_by_string: Dict[str, int] = {}

        # We scan every fragment once and keep only the cheapest cost for each exact text.
        for fragment, c in zip(parts, cost):
            if fragment not in min_cost_by_string or c < min_cost_by_string[fragment]:
                min_cost_by_string[fragment] = c

        return min_cost_by_string

    def _group_by_length(
        self, min_cost_by_string: Dict[str, int]
    ) -> Dict[int, Dict[str, int]]:
        """
        Group unique fragments by their length.

        This is useful because the switching rule depends only on fragment length.
        Also, when checking which fragments can start at a target position, we only need
        to test lengths that actually exist.

        Args:
            min_cost_by_string: Mapping fragment string -> minimum cost.

        Returns:
            A dictionary:
                length -> {fragment_string -> minimum_cost}

        Time complexity:
            O(u), where u is the number of distinct fragment strings.

        Space complexity:
            O(u)
        """
        by_length: Dict[int, Dict[str, int]] = {}

        for fragment, c in min_cost_by_string.items():
            length = len(fragment)
            if length not in by_length:
                by_length[length] = {}
            by_length[length][fragment] = c

        return by_length

    def minimumRewriteCost(
        self, target: str, parts: List[str], cost: List[int], switchCost: int
    ) -> int:
        """
        Compute the minimum total cost to assemble the target exactly.

        Dynamic programming idea:
        - Let dp[i][L] mean:
            minimum cost to build target[0:i] exactly, where the last fragment used
            has length L.
        - From position i, if a fragment of length new_len matches target[i:i+new_len],
          then we can transition from every previous last length prev_len:
              dp[i + new_len][new_len] =
                  min(
                      dp[i + new_len][new_len],
                      dp[i][prev_len] + fragment_cost + penalty
                  )
          where penalty is:
              0 if prev_len == new_len
              switchCost otherwise
        - For the first fragment, there is no switching penalty.

        Important optimization:
        - Fragment lengths are at most 50, so the number of possible lengths is tiny.
        - At each target position, we only check those lengths that exist in the input.
        - Matching a fragment is done by slicing target[i:i+length] and checking whether
          that exact string exists in the dictionary for that length.

        Args:
            target: The DNA string we must assemble exactly.
            parts: Available reusable fragment strings.
            cost: Cost for each fragment.
            switchCost: Extra penalty paid when consecutive fragment lengths differ.

        Returns:
            The minimum total cost, or -1 if assembly is impossible.

        Time complexity:
            O(n * K^2 + n * K * L)
            where:
            - n is len(target)
            - K is the number of distinct fragment lengths (at most 50)
            - L is the max fragment length (at most 50)
            In practice this is efficient because K and L are both small constants.

        Space complexity:
            O(n * K + u)
            where u is the number of distinct fragment strings.
        """
        n: int = len(target)

        # Step 1:
        # Compress duplicate fragment strings by keeping only the cheapest cost.
        # This reduces unnecessary work and simplifies later lookups.
        min_cost_by_string: Dict[str, int] = self._build_min_cost_by_string(parts, cost)

        # Step 2:
        # Group fragments by length because:
        # - matching depends on exact substring text
        # - switching penalty depends only on length
        by_length: Dict[int, Dict[str, int]] = self._group_by_length(min_cost_by_string)

        # If there are no usable fragments at all, assembly is impossible.
        if not by_length:
            return -1

        # Distinct lengths that actually exist in the input.
        # Sorting is not strictly required for correctness, but it makes the logic
        # deterministic and easier to follow.
        lengths: List[int] = sorted(by_length.keys())
        k: int = len(lengths)

        # Map each fragment length to a compact index so we can store DP states in lists
        # instead of dictionaries. Lists are faster and simpler for repeated updates.
        length_to_index: Dict[int, int] = {length: idx for idx, length in enumerate(lengths)}

        # We use a very large number as "infinity" for unreachable states.
        INF: int = 10**30

        # dp[i][j]:
        # minimum cost to build target prefix ending at position i,
        # with the last used fragment having length lengths[j].
        #
        # There are n + 1 positions from 0 to n inclusive.
        # Position 0 means "nothing has been built yet".
        dp: List[List[int]] = [[INF] * k for _ in range(n + 1)]

        # Step 3:
        # Initialize transitions for the very first fragment.
        #
        # The first fragment is special because it never pays switchCost.
        # So for every fragment length that matches at the beginning of target,
        # we can directly set dp[length][that_length].
        for length in lengths:
            if length <= n:
                candidate_text = target[0:length]
                fragment_cost = by_length[length].get(candidate_text)
                if fragment_cost is not None:
                    j = length_to_index[length]
                    dp[length][j] = min(dp[length][j], fragment_cost)

        # Step 4:
        # Process every reachable position from left to right.
        #
        # At each position i, we ask:
        # "If we have already built target[0:i], what fragments can we place next?"
        #
        # For every matching next fragment, we try extending all currently reachable
        # "last length" states at position i.
        for i in range(1, n + 1):
            # Quick check: if no state at position i is reachable, skip it.
            # This avoids unnecessary work.
            reachable = False
            for value in dp[i]:
                if value < INF:
                    reachable = True
                    break
            if not reachable:
                continue

            # Try every available fragment length as the next piece.
            for new_length in lengths:
                end = i + new_length
                if end > n:
                    continue

                # Extract the exact substring that the next fragment would need to match.
                candidate_text = target[i:end]

                # Check whether we actually have a fragment with this exact text and length.
                fragment_cost = by_length[new_length].get(candidate_text)
                if fragment_cost is None:
                    continue

                new_idx = length_to_index[new_length]

                # Transition from every possible previous last length.
                for prev_idx, prev_cost in enumerate(dp[i]):
                    if prev_cost >= INF:
                        continue

                    prev_length = lengths[prev_idx]

                    # Pay switchCost only if the fragment length changes.
                    extra = 0 if prev_length == new_length else switchCost
                    total = prev_cost + fragment_cost + extra

                    if total < dp[end][new_idx]:
                        dp[end][new_idx] = total

        # Step 5:
        # The answer is the cheapest reachable state at position n,
        # regardless of what the final fragment length was.
        answer = min(dp[n])

        return -1 if answer >= INF else answer


if __name__ == "__main__":
    solution = Solution()

    # Example 1
    target1 = "ACGTAC"
    parts1 = ["AC", "GT", "ACG", "TAC"]
    cost1 = [3, 2, 5, 4]
    switch_cost1 = 6
    result1 = solution.minimumRewriteCost(target1, parts1, cost1, switch_cost1)
    print(result1)  # Expected: 8

    # Example 2
    target2 = "AACGT"
    parts2 = ["AA", "A", "CG", "GT"]
    cost2 = [4, 2, 3, 3]
    switch_cost2 = 5
    result2 = solution.minimumRewriteCost(target2, parts2, cost2, switch_cost2)
    print(result2)  # Expected: -1