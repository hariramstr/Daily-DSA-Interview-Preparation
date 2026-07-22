"""
Title: Minimum Rewrite Cost for Nested Template Expansion

Problem Description:
A documentation platform stores a final rendered page as a string `target`. The page was
produced by expanding reusable templates. You are given `m` templates, where template `i`
is a non-empty string `templates[i]` with an associated rewrite cost `cost[i]`.

Starting from an empty page, you may build `target` from left to right using the following
operations:

1. Append any single lowercase letter at cost `appendCost`.
2. Insert one template `templates[i]` at the current end of the page at cost `cost[i]`.
3. If the suffix currently built ends with a string equal to some previously used template,
   you may reuse that same template again immediately for an additional discounted chaining
   cost `chainCost[i]` instead of `cost[i]`.

A template may be used any number of times. Chaining only applies when the most recent
appended block was exactly `templates[i]` and you place the same template again with no gap.
Single-letter appends break the chain.

Return the minimum total cost to build exactly `target`, or `-1` if it is impossible.

Constraints:
- 1 <= target.length <= 10^5
- 1 <= m <= 2 * 10^4
- 1 <= templates[i].length <= 50
- target and every template consist only of lowercase English letters
- 1 <= appendCost, cost[i], chainCost[i] <= 10^9
- Sum of all template lengths does not exceed 2 * 10^5
"""

from typing import Dict, List, Tuple


class AhoNode:
    """Node used by the Aho-Corasick automaton."""

    def __init__(self) -> None:
        self.next: Dict[str, int] = {}
        self.fail: int = 0
        self.out: List[int] = []


class Solution:
    def _compress_templates(
        self,
        templates: List[str],
        cost: List[int],
        chain_cost: List[int],
    ) -> Tuple[List[str], List[int], List[int]]:
        """
        Merge duplicate template strings by keeping the cheapest normal and chain costs.

        Args:
            templates: Original template strings.
            cost: Normal insertion costs.
            chain_cost: Chaining costs.

        Returns:
            A tuple of:
            - unique template strings
            - minimum normal cost for each unique string
            - minimum chain cost for each unique string

        Time complexity:
            O(total_template_characters)

        Space complexity:
            O(number_of_unique_templates)
        """
        best: Dict[str, List[int]] = {}

        for s, c, cc in zip(templates, cost, chain_cost):
            if s not in best:
                best[s] = [c, cc]
            else:
                if c < best[s][0]:
                    best[s][0] = c
                if cc < best[s][1]:
                    best[s][1] = cc

        unique_templates: List[str] = []
        unique_cost: List[int] = []
        unique_chain: List[int] = []

        for s, pair in best.items():
            unique_templates.append(s)
            unique_cost.append(pair[0])
            unique_chain.append(pair[1])

        return unique_templates, unique_cost, unique_chain

    def _build_aho(self, templates: List[str]) -> List[AhoNode]:
        """
        Build an Aho-Corasick automaton for all template strings.

        Args:
            templates: Unique template strings.

        Returns:
            List of automaton nodes.

        Time complexity:
            O(total_template_characters * alphabet_factor_for_dict_operations)

        Space complexity:
            O(total_template_characters)
        """
        nodes: List[AhoNode] = [AhoNode()]

        # Insert every template into the trie.
        for idx, s in enumerate(templates):
            cur = 0
            for ch in s:
                if ch not in nodes[cur].next:
                    nodes[cur].next[ch] = len(nodes)
                    nodes.append(AhoNode())
                cur = nodes[cur].next[ch]
            nodes[cur].out.append(idx)

        # Standard BFS construction of failure links.
        from collections import deque

        queue = deque()

        for ch, nxt in nodes[0].next.items():
            nodes[nxt].fail = 0
            queue.append(nxt)

        while queue:
            v = queue.popleft()

            for ch, nxt in nodes[v].next.items():
                f = nodes[v].fail
                while f != 0 and ch not in nodes[f].next:
                    f = nodes[f].fail
                if ch in nodes[f].next:
                    nodes[nxt].fail = nodes[f].next[ch]
                else:
                    nodes[nxt].fail = 0

                # Any pattern ending at the failure state also ends here.
                nodes[nxt].out.extend(nodes[nodes[nxt].fail].out)
                queue.append(nxt)

        return nodes

    def minimum_rewrite_cost(
        self,
        target: str,
        templates: List[str],
        cost: List[int],
        chain_cost: List[int],
        append_cost: int,
    ) -> int:
        """
        Compute the minimum cost to build the target string exactly.

        Core idea:
        We perform dynamic programming over prefix lengths.
        Let dp[i] be the minimum cost to build target[:i], regardless of what the last block was.
        Additionally, for each template t and each position i where target[:i] ends with t,
        we conceptually have a state:
            chain_state(i, t) = minimum cost to build target[:i] with the last block exactly t
        This state matters because only then can we place the same template again using the
        discounted chain cost.

        A direct DP over all (position, template) states would be too large if implemented
        naively. The key observation is:
        - A chain state for template t can only move to the next position by adding the same
          template t again.
        - Therefore, for each template, chain transitions form a simple forward recurrence
          along positions where that template matches.

        We process positions from left to right.
        At every position i:
        1. We can always append one character, so dp[i + 1] can improve from dp[i].
        2. For every template that matches starting at i:
           - Start a fresh use of that template from dp[i] with normal cost.
           - Or, if a chain state for the same template already exists at position i,
             extend it using chain cost.
           The resulting value updates dp[i + len(template)] and also becomes the new
           chain state at that ending position.

        To efficiently know which templates match at each starting position, we use
        Aho-Corasick over the target string and convert end matches into start-position lists.

        Args:
            target: Final string to build.
            templates: Template strings.
            cost: Normal insertion costs for templates.
            chain_cost: Discounted chaining costs for templates.
            append_cost: Cost to append one single character.

        Returns:
            Minimum total cost to build target exactly.

        Time complexity:
            O(n + total_template_characters + total_number_of_matches)
            where n = len(target)

        Space complexity:
            O(n + total_template_characters + total_number_of_matches + number_of_templates)
        """
        n = len(target)

        # The problem statement says appending any single lowercase letter is always allowed.
        # Therefore, building the target is always possible by appending every character one by one.
        # So the answer can never be -1 under the given rules.
        # We still keep the DP generic and return -1 only if something unexpected happens.
        if n == 0:
            return 0

        # Step 1: Merge duplicate template strings.
        # Why?
        # If the same string appears multiple times with different costs, only the cheapest
        # normal cost and cheapest chain cost matter for optimality.
        uniq_templates, uniq_cost, uniq_chain = self._compress_templates(
            templates, cost, chain_cost
        )
        m = len(uniq_templates)

        # Precompute template lengths because we use them constantly.
        lengths: List[int] = [len(s) for s in uniq_templates]

        # Step 2: Build Aho-Corasick automaton to find every occurrence of every template
        # inside the target in one linear scan.
        nodes = self._build_aho(uniq_templates)

        # Step 3: For each start position, store which template indices match there.
        # matches_start[i] = list of template indices t such that target[i:i+len(t)] == template[t]
        matches_start: List[List[int]] = [[] for _ in range(n)]

        state = 0
        for end_pos, ch in enumerate(target):
            while state != 0 and ch not in nodes[state].next:
                state = nodes[state].fail
            if ch in nodes[state].next:
                state = nodes[state].next[ch]
            else:
                state = 0

            # Every template in nodes[state].out ends at end_pos.
            # Convert that to a start position.
            for template_idx in nodes[state].out:
                start_pos = end_pos - lengths[template_idx] + 1
                if start_pos >= 0:
                    matches_start[start_pos].append(template_idx)

        # Step 4: Dynamic programming arrays.
        INF = 10**30

        # dp[i] = minimum cost to build exactly target[:i], regardless of last block type.
        dp: List[int] = [INF] * (n + 1)
        dp[0] = 0

        # chain_best_end[i] stores a dictionary:
        #   template_idx -> minimum cost to build target[:i] with the last block exactly
        #   uniq_templates[template_idx]
        #
        # Why sparse dictionaries?
        # Most positions will only be reachable by a small number of "last template" states,
        # because such a state only exists if that exact template ends at that exact position.
        chain_best_end: List[Dict[int, int]] = [{} for _ in range(n + 1)]

        # Step 5: Main left-to-right DP.
        for i in range(n):
            # 5a. Single-character append transition.
            # This always exists and also breaks any chain, which is fine because dp only
            # stores the best cost regardless of chain status.
            if dp[i] < INF:
                append_value = dp[i] + append_cost
                if append_value < dp[i + 1]:
                    dp[i + 1] = append_value

            # 5b. Template transitions for every template matching at position i.
            #
            # For each matching template t of length L:
            # - Fresh use: dp[i] + cost[t]
            # - Chained use: chain_best_end[i].get(t) + chain_cost[t], if such a chain state exists
            #
            # The best of these creates a state at position i + L where the last block is t.
            # That state also contributes to dp[i + L].
            if matches_start[i]:
                current_chain_states = chain_best_end[i]

                for t_idx in matches_start[i]:
                    end_pos = i + lengths[t_idx]
                    best_value = INF

                    # Start template t fresh from the generic best cost at position i.
                    if dp[i] < INF:
                        fresh_value = dp[i] + uniq_cost[t_idx]
                        if fresh_value < best_value:
                            best_value = fresh_value

                    # Continue a chain of the same template if the previous block was exactly t.
                    prev_chain_value = current_chain_states.get(t_idx)
                    if prev_chain_value is not None:
                        chained_value = prev_chain_value + uniq_chain[t_idx]
                        if chained_value < best_value:
                            best_value = chained_value

                    # If we found any valid way to place this template, record it.
                    if best_value < INF:
                        # Update the chain state at the ending position.
                        existing = chain_best_end[end_pos].get(t_idx)
                        if existing is None or best_value < existing:
                            chain_best_end[end_pos][t_idx] = best_value

                        # Also update the generic best cost for that prefix.
                        if best_value < dp[end_pos]:
                            dp[end_pos] = best_value

        return dp[n] if dp[n] < INF else -1


if __name__ == "__main__":
    solution = Solution()

    # Example 1
    target1 = "ababab"
    templates1 = ["ab", "aba"]
    cost1 = [5, 8]
    chain1 = [2, 6]
    append_cost1 = 4
    result1 = solution.minimum_rewrite_cost(
        target1, templates1, cost1, chain1, append_cost1
    )
    print(result1)  # Expected: 9

    # Example 2
    target2 = "codecodex"
    templates2 = ["code", "x"]
    cost2 = [7, 10]
    chain2 = [3, 1]
    append_cost2 = 2
    result2 = solution.minimum_rewrite_cost(
        target2, templates2, cost2, chain2, append_cost2
    )
    print(result2)  # Expected: 12