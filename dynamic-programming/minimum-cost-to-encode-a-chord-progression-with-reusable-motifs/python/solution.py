"""
Title: Minimum Cost to Encode a Chord Progression with Reusable Motifs

Problem Description:
A music production tool stores a long chord progression as a string `progression`
of uppercase letters, where each character represents one chord symbol after
normalization. To reduce storage, the tool may encode the progression using a
library of reusable motifs. Each motif `motifs[i]` is a non-empty string and has
an associated non-negative cost `cost[i]`. You may use any motif any number of
times. When a motif is used, it must match the next characters of the progression
exactly and advances the encoding position by its length. You may also encode a
single chord directly without using a motif, paying a fixed cost `singleCost` for
that one character.

Your task is to compute the minimum total cost required to encode the entire
progression from left to right. If multiple motifs overlap in possible usage, you
may choose whichever sequence yields the smallest total cost. Motifs may have
repeated strings with different costs; you should treat them as separate options,
though an optimal solution will naturally prefer cheaper uses. Return the minimum
possible cost.

This is not a compression ratio problem: every character of `progression` must be
covered exactly once, either by a matching motif or by paying `singleCost` for
that character.

Constraints:
- 1 <= progression.length <= 10^5
- 1 <= motifs.length <= 10^4
- 1 <= sum(motifs[i].length) <= 2 * 10^5
- 1 <= motifs[i].length <= 10^4
- 0 <= cost[i], singleCost <= 10^9
- progression and all motifs[i] consist only of uppercase English letters.
"""

from collections import deque
from typing import Deque, Dict, List, Optional


class AhoCorasickNode:
    """Node used in the Aho-Corasick automaton."""

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
        self.output: List[int] = []


class Solution:
    def _build_automaton(self, motifs: List[str], costs: List[int]) -> tuple[List[AhoCorasickNode], List[int]]:
        """
        Build an Aho-Corasick automaton for all motifs.

        Each terminal node stores motif indices that end at that node.
        We also return a list of motif lengths so later we can compute where
        each matched motif started in the progression.

        Args:
            motifs: List of motif strings.
            costs: List of motif costs. Included for interface symmetry; actual
                automaton construction only needs motifs.

        Returns:
            A tuple:
            - list of automaton nodes
            - list of motif lengths by motif index

        Time complexity:
            O(sum(len(motif)) * alphabet-related dictionary operations)

        Space complexity:
            O(sum(len(motif)))
        """
        nodes: List[AhoCorasickNode] = [AhoCorasickNode()]
        lengths: List[int] = [0] * len(motifs)

        # Step 1:
        # Insert every motif into the trie.
        #
        # Why a trie?
        # - Many motifs may share prefixes.
        # - A trie stores those shared prefixes once.
        # - This is the standard first step for Aho-Corasick, which lets us
        #   match all motifs against the progression in one left-to-right scan.
        for idx, motif in enumerate(motifs):
            current: int = 0
            lengths[idx] = len(motif)

            for ch in motif:
                if ch not in nodes[current].next:
                    nodes[current].next[ch] = len(nodes)
                    nodes.append(AhoCorasickNode())
                current = nodes[current].next[ch]

            # Mark that motif `idx` ends at this node.
            nodes[current].output.append(idx)

        # Step 2:
        # Build failure links using BFS.
        #
        # A failure link tells us where to continue in the automaton when the
        # current character does not continue the current trie path.
        #
        # This is what makes Aho-Corasick efficient: we never restart matching
        # from scratch for each position.
        queue: Deque[int] = deque()

        # All direct children of the root have failure link 0 (the root).
        for ch, nxt in nodes[0].next.items():
            nodes[nxt].fail = 0
            queue.append(nxt)

        # BFS over trie nodes to compute failure links level by level.
        while queue:
            node_index: int = queue.popleft()

            for ch, child_index in nodes[node_index].next.items():
                # Follow failure links from the current node until we find
                # a node that has the same transition, or we reach the root.
                fail_state: int = nodes[node_index].fail
                while fail_state != 0 and ch not in nodes[fail_state].next:
                    fail_state = nodes[fail_state].fail

                if ch in nodes[fail_state].next:
                    nodes[child_index].fail = nodes[fail_state].next[ch]
                else:
                    nodes[child_index].fail = 0

                # Important:
                # If the failure-linked node is also terminal for some motifs,
                # then those motifs also end here.
                #
                # Example:
                # If we matched "...ABA" and there is also a motif "BA",
                # then the node for "ABA" should also report "BA" through
                # failure links.
                nodes[child_index].output.extend(nodes[nodes[child_index].fail].output)
                queue.append(child_index)

        return nodes, lengths

    def minimum_cost(self, progression: str, motifs: List[str], cost: List[int], singleCost: int) -> int:
        """
        Compute the minimum cost to encode the progression.

        The algorithm combines:
        1. Dynamic programming over positions in the progression.
        2. Aho-Corasick multi-pattern matching to find all motif matches ending
           at each position in one scan.

        DP definition:
        - dp[i] = minimum cost to encode progression[0:i]
        - Answer is dp[n]

        Transition:
        - Encode one character directly:
              dp[i + 1] = min(dp[i + 1], dp[i] + singleCost)
        - For every motif that ends at position i:
              let its length be L and cost be C
              then it covers progression[i-L+1 : i+1]
              so:
              dp[i + 1] = min(dp[i + 1], dp[i + 1 - L] + C)

        Args:
            progression: The target chord progression string.
            motifs: Available reusable motifs.
            cost: Cost for each motif.
            singleCost: Cost to encode one character directly.

        Returns:
            Minimum total encoding cost.

        Time complexity:
            O(n + total_motif_length + total_number_of_matches)
            where n = len(progression)

        Space complexity:
            O(n + total_motif_length)
        """
        n: int = len(progression)

        # Build the Aho-Corasick automaton once.
        # This lets us discover all motif matches while scanning the progression
        # only one time from left to right.
        nodes, lengths = self._build_automaton(motifs, cost)

        # dp[i] means:
        # minimum cost to encode the first i characters of progression.
        #
        # Example:
        # - dp[0] = 0 because encoding an empty prefix costs nothing.
        # - dp[3] = minimum cost to encode progression[0:3]
        #
        # We initialize everything to a very large number because we are taking
        # minimums.
        inf: int = 10**30
        dp: List[int] = [inf] * (n + 1)
        dp[0] = 0

        # `state` is the current node in the automaton while scanning the string.
        state: int = 0

        # Process the progression one character at a time.
        for i, ch in enumerate(progression):
            # ---------------------------------------------------------------
            # Part 1: Advance the Aho-Corasick automaton with progression[i].
            # ---------------------------------------------------------------
            #
            # If the current state does not have an edge for this character,
            # follow failure links until we either find one or return to root.
            while state != 0 and ch not in nodes[state].next:
                state = nodes[state].fail

            if ch in nodes[state].next:
                state = nodes[state].next[ch]
            else:
                state = 0

            # ---------------------------------------------------------------
            # Part 2: Default transition - encode this one character directly.
            # ---------------------------------------------------------------
            #
            # Since every character must be covered exactly once, direct encoding
            # is always a valid fallback.
            #
            # If we have already encoded the first i characters with cost dp[i],
            # then encoding progression[i] directly gives a candidate for dp[i+1].
            if dp[i] + singleCost < dp[i + 1]:
                dp[i + 1] = dp[i] + singleCost

            # ---------------------------------------------------------------
            # Part 3: Motif transitions - every motif ending here is another
            # valid way to reach dp[i+1].
            # ---------------------------------------------------------------
            #
            # Because of the automaton's output list (including inherited outputs
            # from failure links), nodes[state].output contains every motif index
            # whose string ends exactly at position i.
            #
            # For each such motif:
            # - length = L
            # - it covers progression[i-L+1 : i+1]
            # - so the previous DP state is dp[i+1-L]
            for motif_index in nodes[state].output:
                motif_len: int = lengths[motif_index]
                motif_cost: int = cost[motif_index]
                start_prefix_len: int = i + 1 - motif_len

                candidate: int = dp[start_prefix_len] + motif_cost
                if candidate < dp[i + 1]:
                    dp[i + 1] = candidate

        return dp[n]


if __name__ == "__main__":
    solution = Solution()

    progression1 = "ABABAC"
    motifs1 = ["ABA", "BA", "AC"]
    cost1 = [4, 2, 3]
    single_cost1 = 2
    result1 = solution.minimum_cost(progression1, motifs1, cost1, single_cost1)
    print(result1)  # Expected: 7

    progression2 = "AAAAAA"
    motifs2 = ["A", "AA", "AAA"]
    cost2 = [5, 3, 4]
    single_cost2 = 2
    result2 = solution.minimum_cost(progression2, motifs2, cost2, single_cost2)
    print(result2)  # Expected: 8