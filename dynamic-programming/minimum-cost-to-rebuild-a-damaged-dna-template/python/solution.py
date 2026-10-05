"""
Title: Minimum Cost to Rebuild a Damaged DNA Template

Problem Description:
A genetics lab stores a reference DNA template as a string `target` consisting only of
the characters 'A', 'C', 'G', and 'T'. After a storage failure, the lab can no longer
access the original template directly, but it still has a collection of DNA fragments.
Each fragment `fragments[i]` can be used any number of times, and using it once adds a
fixed assembly cost `cost[i]`. A fragment may be placed only if it exactly matches the
corresponding substring of `target` at the chosen position. The final reconstructed
sequence must equal `target` exactly, with no extra characters and no mismatches.

Your task is to compute the minimum total cost required to reconstruct the entire
`target`. If it is impossible, return -1.

Fragments may overlap in the input set, and different fragments may have the same string
but different costs. Because fragments can be reused, choosing a locally cheapest fragment
does not always lead to a globally optimal answer. An efficient dynamic programming
solution is required.

Constraints:
- 1 <= target.length <= 10^5
- 1 <= fragments.length <= 10^4
- 1 <= fragments[i].length <= 200
- sum(fragments[i].length) <= 2 * 10^5
- 1 <= cost[i] <= 10^9
- `target` and every fragment contain only 'A', 'C', 'G', and 'T'

Examples:
1) target = "ACGTAC", fragments = ["AC", "CGT", "GT", "AC"], cost = [4, 5, 3, 2]
   Output: 7
   One valid minimum partition is:
   - "AC" at position 0 with cost 2
   - "GT" at position 2 with cost 3
   - "AC" at position 4 with cost 2
   Total = 7

2) target = "AAGT", fragments = ["AA", "AG", "GT"], cost = [3, 4, 2]
   Output: 5
   Use:
   - "AA" at position 0 with cost 3
   - "GT" at position 2 with cost 2
   Total = 5
"""

from typing import Dict, List, Tuple


class AhoCorasickNode:
    """Node used inside the Aho-Corasick automaton."""

    def __init__(self) -> None:
        """
        Initialize one trie/automaton node.

        Args:
            None

        Returns:
            None

        Time complexity:
            O(1)

        Space complexity:
            O(1)
        """
        self.next: Dict[str, int] = {}
        self.fail: int = 0
        self.outputs: List[Tuple[int, int]] = []


class Solution:
    def _build_automaton(self, word_cost: Dict[str, int]) -> List[AhoCorasickNode]:
        """
        Build an Aho-Corasick automaton from unique fragments with minimum costs.

        Each terminal node stores pairs of:
        - fragment length
        - minimum cost for that exact fragment

        Args:
            word_cost: Mapping from fragment string to its minimum usable cost.

        Returns:
            A list of automaton nodes representing the trie + failure links.

        Time complexity:
            O(total_fragment_length * alphabet_factor), effectively O(total_fragment_length)
            for this small alphabet.

        Space complexity:
            O(total_fragment_length)
        """
        nodes: List[AhoCorasickNode] = [AhoCorasickNode()]

        # Insert every unique fragment into the trie.
        # If multiple identical fragments exist in input, we already compressed them
        # into the cheapest cost only, because using a more expensive identical fragment
        # is never beneficial.
        for word, c in word_cost.items():
            current: int = 0
            for ch in word:
                if ch not in nodes[current].next:
                    nodes[current].next[ch] = len(nodes)
                    nodes.append(AhoCorasickNode())
                current = nodes[current].next[ch]
            nodes[current].outputs.append((len(word), c))

        # Build failure links using BFS.
        # Failure link of a node points to the longest proper suffix that is also a trie node.
        from collections import deque

        queue = deque()

        # Root's direct children have failure link to root.
        for ch, nxt in nodes[0].next.items():
            nodes[nxt].fail = 0
            queue.append(nxt)

        # Standard BFS over trie nodes.
        while queue:
            v = queue.popleft()

            # Important detail:
            # If the failure node is terminal for some patterns, then this node should
            # also report those patterns when matched as suffixes.
            fail_node = nodes[v].fail
            if nodes[fail_node].outputs:
                nodes[v].outputs.extend(nodes[fail_node].outputs)

            for ch, nxt in nodes[v].next.items():
                # Follow failure links until we find a node that has this transition,
                # or we fall back to root.
                f = nodes[v].fail
                while f != 0 and ch not in nodes[f].next:
                    f = nodes[f].fail

                if ch in nodes[f].next:
                    nodes[nxt].fail = nodes[f].next[ch]
                else:
                    nodes[nxt].fail = 0

                queue.append(nxt)

        return nodes

    def minimumCost(self, target: str, fragments: List[str], cost: List[int]) -> int:
        """
        Compute the minimum total cost to reconstruct the target exactly.

        The algorithm works in three major phases:
        1. Compress duplicate fragments by keeping only the minimum cost for each string.
        2. Build an Aho-Corasick automaton to find all fragment matches in the target
           efficiently in one left-to-right scan.
        3. Run dynamic programming where dp[i] is the minimum cost to build target[:i].

        Args:
            target: The DNA template that must be reconstructed exactly.
            fragments: Available DNA fragments, each reusable unlimited times.
            cost: cost[i] is the cost to use fragments[i] once.

        Returns:
            The minimum total cost to form the entire target, or -1 if impossible.

        Time complexity:
            O(sum(len(fragment)) + len(target) + number_of_matches)
            In this problem, fragment lengths are at most 200 and total fragment length
            is bounded, so this is efficient enough.

        Space complexity:
            O(sum(len(fragment)) + len(target))
        """
        # ------------------------------------------------------------
        # Step 1: Deduplicate identical fragments by keeping only the
        # cheapest cost for each exact fragment string.
        #
        # Why this is correct:
        # If the same fragment text appears multiple times with different costs,
        # we would never choose the more expensive one because both produce the
        # exact same coverage on the target.
        # ------------------------------------------------------------
        word_cost: Dict[str, int] = {}
        for frag, c in zip(fragments, cost):
            if frag not in word_cost or c < word_cost[frag]:
                word_cost[frag] = c

        # ------------------------------------------------------------
        # Step 2: Build the Aho-Corasick automaton.
        #
        # Why use this data structure?
        # We need to know, for every position in target, which fragments end there.
        # Checking every fragment at every position would be far too slow.
        #
        # Aho-Corasick lets us scan the target once and report all matching fragments
        # ending at each position in near-linear time.
        # ------------------------------------------------------------
        nodes = self._build_automaton(word_cost)

        n: int = len(target)
        inf: int = 10**30

        # ------------------------------------------------------------
        # Step 3: Dynamic programming.
        #
        # dp[i] = minimum cost to build the prefix target[:i]
        #
        # Interpretation:
        # - dp[0] = 0 because empty prefix costs nothing.
        # - If a fragment of length L and cost C ends at position i-1,
        #   then it covers target[i-L : i], so:
        #       dp[i] = min(dp[i], dp[i-L] + C)
        #
        # This is a classic "minimum cost exact segmentation" DP.
        # ------------------------------------------------------------
        dp: List[int] = [inf] * (n + 1)
        dp[0] = 0

        # Current automaton state while scanning target left to right.
        state: int = 0

        # ------------------------------------------------------------
        # Scan the target one character at a time.
        #
        # At each character:
        # 1. Move through the automaton using trie edges / failure links.
        # 2. Every output at the current state corresponds to a fragment that
        #    ends at the current index.
        # 3. Use those matches to update the DP value for the current prefix end.
        # ------------------------------------------------------------
        for i, ch in enumerate(target):
            # Follow failure links until we can transition with ch,
            # or until we reach the root.
            while state != 0 and ch not in nodes[state].next:
                state = nodes[state].fail

            # If transition exists, take it; otherwise remain at root.
            if ch in nodes[state].next:
                state = nodes[state].next[ch]
            else:
                state = 0

            # Prefix length after consuming target[i].
            end_pos = i + 1

            # Every output here is a fragment that ends exactly at index i.
            # For each such fragment, try to extend a valid previous prefix.
            for length, c in nodes[state].outputs:
                start_pos = end_pos - length
                if start_pos >= 0 and dp[start_pos] != inf:
                    candidate = dp[start_pos] + c
                    if candidate < dp[end_pos]:
                        dp[end_pos] = candidate

        return -1 if dp[n] == inf else dp[n]


if __name__ == "__main__":
    solution = Solution()

    target_1 = "ACGTAC"
    fragments_1 = ["AC", "CGT", "GT", "AC"]
    cost_1 = [4, 5, 3, 2]
    result_1 = solution.minimumCost(target_1, fragments_1, cost_1)
    print(result_1)  # Expected: 7

    target_2 = "AAGT"
    fragments_2 = ["AA", "AG", "GT"]
    cost_2 = [3, 4, 2]
    result_2 = solution.minimumCost(target_2, fragments_2, cost_2)
    print(result_2)  # Expected: 5