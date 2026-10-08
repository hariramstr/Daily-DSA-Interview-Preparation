"""
Title: Minimum Cost to Assemble a Target Robot Command Stream

Problem Description:
A robotics team stores reusable command macros in a library. Each macro is a non-empty
string made of lowercase English letters, and executing that macro contributes a fixed cost.
You are given a target command stream target, along with arrays macros and cost, where
macros[i] can be used any number of times and appending macros[i] adds cost[i] to the total cost.

Your task is to build target exactly by concatenating chosen macros in order. You may reuse
the same macro many times, but every chosen macro must match the next characters of target
exactly at the position where it is placed. Return the minimum total cost needed to assemble
the entire target string, or -1 if it is impossible.

This is not a shortest-length problem: a more expensive long macro may be worse than several
cheaper short macros, and duplicate macro strings may appear with different costs. The answer
should use the cheapest possible combination.

Constraints:
- 1 <= target.length <= 10^5
- 1 <= macros.length <= 10^5
- 1 <= macros[i].length <= 10^5
- Sum of all macros[i].length <= 2 * 10^5
- 1 <= cost[i] <= 10^9
- target and every macros[i] consist only of lowercase English letters

Examples:
1)
Input: target = "ababa", macros = ["ab", "aba", "ba", "a"], cost = [4, 5, 2, 10]
Output: 7

2)
Input: target = "robot", macros = ["ro", "bot", "obo", "t"], cost = [3, 4, 10, 1]
Output: -1
"""

from collections import deque
from typing import Deque, Dict, List, Tuple


class AhoCorasickNode:
    """
    Node used in the Aho-Corasick automaton.

    Each node represents a prefix of one or more macro strings.
    """

    def __init__(self) -> None:
        """
        Initialize an empty trie/automaton node.

        Time complexity:
            O(1)

        Space complexity:
            O(1)
        """
        self.next: Dict[str, int] = {}
        self.fail: int = 0
        self.outputs: List[Tuple[int, int]] = []


class Solution:
    def minimumCost(self, target: str, macros: List[str], cost: List[int]) -> int:
        """
        Compute the minimum total cost to assemble the target exactly.

        The algorithm has two major parts:
        1. Build an Aho-Corasick automaton from all unique macros, keeping only the
           cheapest cost for duplicate macro strings.
        2. Scan the target once to find every macro occurrence efficiently, and use
           dynamic programming to relax transitions:
               dp[end_position] = min(dp[start_position] + macro_cost)

        Args:
            target: The target string that must be assembled exactly.
            macros: List of reusable macro strings.
            cost: Cost of using each macro.

        Returns:
            The minimum total cost to build the entire target, or -1 if impossible.

        Time complexity:
            O(sum(len(macro)) + len(target) + number_of_matches)
            where number_of_matches is the total number of macro occurrences found in target.

        Space complexity:
            O(sum(len(macro)) + len(target))
        """
        # ------------------------------------------------------------
        # Step 1: Deduplicate identical macro strings by keeping only
        # the minimum cost for each exact string.
        #
        # Why?
        # If the same macro text appears multiple times with different costs,
        # we would never want to use a more expensive copy of the exact same
        # string, because it creates the same transition in the target but
        # costs more. So we compress duplicates early.
        # ------------------------------------------------------------
        cheapest: Dict[str, int] = {}
        for macro, c in zip(macros, cost):
            if macro not in cheapest or c < cheapest[macro]:
                cheapest[macro] = c

        # ------------------------------------------------------------
        # Step 2: Build the Aho-Corasick automaton.
        #
        # This data structure allows us to search for all macro matches in
        # the target in a single left-to-right pass.
        #
        # A trie alone can match prefixes, but Aho-Corasick adds failure links
        # so that when a character does not continue the current trie path,
        # we can jump to the longest valid suffix state and continue matching
        # without rescanning characters.
        # ------------------------------------------------------------
        nodes: List[AhoCorasickNode] = [AhoCorasickNode()]  # node 0 is the root

        for macro, c in cheapest.items():
            current: int = 0

            # Insert the macro into the trie character by character.
            for ch in macro:
                if ch not in nodes[current].next:
                    nodes[current].next[ch] = len(nodes)
                    nodes.append(AhoCorasickNode())
                current = nodes[current].next[ch]

            # Store an output at the terminal node:
            # (length_of_macro, cost_of_macro)
            #
            # Later, when the automaton reaches this node while scanning target,
            # it means a macro ends at the current target position.
            nodes[current].outputs.append((len(macro), c))

        # ------------------------------------------------------------
        # Step 3: Build failure links using BFS.
        #
        # Failure link of a node points to the node representing the longest
        # proper suffix of the current node's string that is also a trie prefix.
        #
        # We also propagate outputs along failure links:
        # if a node fails to another node that is terminal for some macro(s),
        # then those macro(s) also end at the current position.
        # ------------------------------------------------------------
        queue: Deque[int] = deque()

        # Initialize root's direct children.
        for ch, nxt in nodes[0].next.items():
            nodes[nxt].fail = 0
            queue.append(nxt)

        while queue:
            node_index = queue.popleft()
            node = nodes[node_index]

            for ch, child_index in node.next.items():
                # Start from the failure link of the current node.
                fail_state = node.fail

                # Follow failure links until we either find a transition by ch
                # or reach the root.
                while fail_state != 0 and ch not in nodes[fail_state].next:
                    fail_state = nodes[fail_state].fail

                # If the root or some failure ancestor has a transition by ch,
                # use it. Otherwise the failure link becomes root (0).
                if ch in nodes[fail_state].next:
                    nodes[child_index].fail = nodes[fail_state].next[ch]
                else:
                    nodes[child_index].fail = 0

                # Important:
                # Any pattern that ends at the failure state also ends here.
                # So we append those outputs to the child node's outputs.
                nodes[child_index].outputs.extend(nodes[nodes[child_index].fail].outputs)

                queue.append(child_index)

        # ------------------------------------------------------------
        # Step 4: Dynamic programming array.
        #
        # dp[i] = minimum cost to build target[:i]
        #
        # So:
        # - dp[0] = 0 because empty prefix costs nothing.
        # - dp[n] is the answer for the whole target.
        #
        # Transition:
        # If a macro of length L and cost C ends at position i (0-based),
        # then it covers target[i-L+1 : i+1].
        # Let end = i + 1 and start = end - L.
        # If dp[start] is reachable, then:
        #     dp[end] = min(dp[end], dp[start] + C)
        # ------------------------------------------------------------
        n: int = len(target)
        inf: int = 10**30
        dp: List[int] = [inf] * (n + 1)
        dp[0] = 0

        # ------------------------------------------------------------
        # Step 5: Scan the target with the automaton.
        #
        # At each character:
        # - Move through trie transitions if possible.
        # - Otherwise follow failure links until a valid transition is found
        #   or we return to root.
        # - Every output at the resulting state corresponds to a macro ending
        #   at the current position.
        #
        # For each such macro occurrence, perform a DP relaxation.
        # ------------------------------------------------------------
        state: int = 0

        for i, ch in enumerate(target):
            # While the current state does not have a transition by ch,
            # follow failure links to find the next best suffix state.
            while state != 0 and ch not in nodes[state].next:
                state = nodes[state].fail

            # If a transition exists, take it; otherwise remain at root.
            if ch in nodes[state].next:
                state = nodes[state].next[ch]
            else:
                state = 0

            # Every output at this state means a macro ends at index i.
            if nodes[state].outputs:
                end_pos = i + 1

                for length, macro_cost in nodes[state].outputs:
                    start_pos = end_pos - length

                    # Only relax if the prefix before this macro is reachable.
                    if dp[start_pos] != inf:
                        candidate = dp[start_pos] + macro_cost
                        if candidate < dp[end_pos]:
                            dp[end_pos] = candidate

        # If dp[n] was never updated, the target cannot be assembled exactly.
        return dp[n] if dp[n] != inf else -1


if __name__ == "__main__":
    solution = Solution()

    # Example 1
    target1 = "ababa"
    macros1 = ["ab", "aba", "ba", "a"]
    cost1 = [4, 5, 2, 10]
    result1 = solution.minimumCost(target1, macros1, cost1)
    print(result1)  # Expected: 7

    # Example 2
    target2 = "robot"
    macros2 = ["ro", "bot", "obo", "t"]
    cost2 = [3, 4, 10, 1]
    result2 = solution.minimumCost(target2, macros2, cost2)
    print(result2)  # Expected: 8