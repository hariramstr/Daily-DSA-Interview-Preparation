"""
Title: Minimum Cost to Encode a Message with Reusable Dictionary Blocks

Problem Description:
You are building a compression system for a chat platform. A message string `target`
must be encoded from left to right using a set of reusable dictionary blocks.
Each dictionary block is a lowercase string `words[i]` with an associated
non-negative encoding cost `costs[i]`. You may use any block any number of times.

Starting at position `p` in `target`, you may place block `words[i]` only if it
exactly matches the substring of `target` beginning at `p`. If it matches, you pay
`costs[i]` and advance by `words[i].length`. Your goal is to encode the entire
target string with minimum total cost. If it is impossible to cover the full string
exactly, return `-1`.

This is not a greedy problem: a cheaper block at the current position may force a
more expensive completion later. You must determine the globally minimum cost.

Return the minimum encoding cost.

Constraints:
- 1 <= target.length <= 10^5
- 1 <= words.length <= 10^5
- 1 <= sum(words[i].length) <= 2 * 10^5
- 0 <= costs[i] <= 10^9
- target and all words[i] consist only of lowercase English letters
- Duplicate dictionary strings may appear with different costs
"""

from collections import deque
from typing import Deque, Dict, List, Optional, Tuple


class AhoCorasickNode:
    """Single node in the Aho-Corasick automaton."""

    __slots__ = ("next", "fail", "depth", "best_cost")

    def __init__(self) -> None:
        """
        Initialize an automaton node.

        Args:
            None

        Returns:
            None

        Time complexity:
            O(1)

        Space complexity:
            O(1)
        """
        # Transition table:
        # maps character index 0..25 to next node index.
        self.next: Dict[int, int] = {}

        # Failure link:
        # when current path cannot continue with a character,
        # we jump to this node and try again.
        self.fail: int = 0

        # Depth of this node in the trie = length of the string represented here.
        self.depth: int = 0

        # If one or more dictionary words end at this node,
        # store the minimum cost among those words.
        # Otherwise keep None.
        self.best_cost: Optional[int] = None


class Solution:
    def __init__(self) -> None:
        """
        Prepare reusable containers for the automaton.

        Args:
            None

        Returns:
            None

        Time complexity:
            O(1)

        Space complexity:
            O(1)
        """
        self.nodes: List[AhoCorasickNode] = []

    def _build_automaton(self, words: List[str], costs: List[int]) -> None:
        """
        Build an Aho-Corasick automaton from the dictionary words.

        Important design choice:
        We only store the minimum cost for each exact dictionary string.
        If the same word appears multiple times with different costs, only the
        cheapest one matters, because using a more expensive duplicate is never
        beneficial.

        Args:
            words: Dictionary strings.
            costs: Cost for each dictionary string.

        Returns:
            None

        Time complexity:
            O(sum(len(word)) + number_of_nodes * alphabet_factor_in_practice)
            More precisely, O(total_dictionary_characters + number_of_edges)

        Space complexity:
            O(total_dictionary_characters)
        """
        # Start with the root node at index 0.
        self.nodes = [AhoCorasickNode()]

        # ------------------------------------------------------------------
        # Step 1: Insert every word into the trie.
        #
        # Each path from the root represents a prefix.
        # When we finish inserting a word, we mark the ending node with the
        # minimum cost for that exact word.
        # ------------------------------------------------------------------
        for word, cost in zip(words, costs):
            current = 0
            for ch in word:
                idx = ord(ch) - ord("a")
                if idx not in self.nodes[current].next:
                    new_node = AhoCorasickNode()
                    new_node.depth = self.nodes[current].depth + 1
                    self.nodes.append(new_node)
                    self.nodes[current].next[idx] = len(self.nodes) - 1
                current = self.nodes[current].next[idx]

            # Keep only the cheapest cost for duplicate words.
            if self.nodes[current].best_cost is None:
                self.nodes[current].best_cost = cost
            else:
                self.nodes[current].best_cost = min(self.nodes[current].best_cost, cost)

        # ------------------------------------------------------------------
        # Step 2: Build failure links using BFS.
        #
        # The failure link of a node points to the longest proper suffix of the
        # current trie-string that is also a trie prefix.
        #
        # This allows us to process the target in linear time while still
        # discovering all dictionary matches that end at each position.
        # ------------------------------------------------------------------
        queue: Deque[int] = deque()

        # Root's immediate children fail back to root.
        for child in self.nodes[0].next.values():
            self.nodes[child].fail = 0
            queue.append(child)

        # BFS over trie levels.
        while queue:
            node_index = queue.popleft()
            node = self.nodes[node_index]

            for ch_idx, child_index in node.next.items():
                # Follow failure links from the current node until we find
                # a node that has the same transition, or we reach the root.
                fail_state = node.fail
                while fail_state != 0 and ch_idx not in self.nodes[fail_state].next:
                    fail_state = self.nodes[fail_state].fail

                if ch_idx in self.nodes[fail_state].next:
                    self.nodes[child_index].fail = self.nodes[fail_state].next[ch_idx]
                else:
                    self.nodes[child_index].fail = 0

                queue.append(child_index)

    def minimum_cost(self, target: str, words: List[str], costs: List[int]) -> int:
        """
        Compute the minimum total cost to cover the entire target exactly.

        Core idea:
        1. Build an Aho-Corasick automaton for all dictionary words.
        2. Scan the target from left to right.
        3. At each target position, find every dictionary word that ends here.
        4. Use dynamic programming:
           dp[i] = minimum cost to cover target[0:i]
           If a word of length L and cost C ends at position i-1, then:
               dp[i] = min(dp[i], dp[i-L] + C)

        Why this works:
        - Every valid encoding is a sequence of dictionary words placed
          consecutively without overlap or gaps.
        - If a word ends at position i-1, then the prefix before that word must
          already be optimally encoded as dp[i-L].
        - Taking the minimum over all possible ending words gives the optimal
          dp[i].

        Args:
            target: The message to encode.
            words: Available reusable dictionary blocks.
            costs: Cost of each dictionary block.

        Returns:
            Minimum encoding cost, or -1 if exact full coverage is impossible.

        Time complexity:
            O(len(target) + total_dictionary_characters + total_number_of_matches * average_fail_chain_work)
            In practice this is efficient for the given constraints.
            More concretely:
            - Building automaton: O(total_dictionary_characters)
            - Scanning target: O(len(target) + number_of_reported_matches + failure traversals)

        Space complexity:
            O(total_dictionary_characters + len(target))
        """
        # Build the automaton once from the dictionary.
        self._build_automaton(words, costs)

        n = len(target)

        # ------------------------------------------------------------------
        # Dynamic programming array:
        # dp[i] = minimum cost to encode the first i characters of target.
        #
        # dp[0] = 0 because encoding an empty prefix costs nothing.
        # Initialize all other states to a very large value meaning "unreachable".
        # ------------------------------------------------------------------
        inf = 10**30
        dp: List[int] = [inf] * (n + 1)
        dp[0] = 0

        # Current automaton state while scanning the target.
        state = 0

        # ------------------------------------------------------------------
        # Process target characters one by one.
        #
        # Suppose we are at target index i (0-based), meaning we just consumed
        # target[i]. Then every dictionary word that ends here can potentially
        # update dp[i + 1].
        # ------------------------------------------------------------------
        for i, ch in enumerate(target):
            ch_idx = ord(ch) - ord("a")

            # --------------------------------------------------------------
            # Move through the automaton with the current character.
            #
            # If there is no direct transition, repeatedly follow failure
            # links until we either find one or return to the root.
            # --------------------------------------------------------------
            while state != 0 and ch_idx not in self.nodes[state].next:
                state = self.nodes[state].fail

            if ch_idx in self.nodes[state].next:
                state = self.nodes[state].next[ch_idx]
            else:
                state = 0

            # --------------------------------------------------------------
            # Every word ending at this position corresponds either to:
            # - the current state itself, if it is a terminal node
            # - or some terminal node reachable by following failure links
            #
            # We walk that chain and apply DP transitions.
            #
            # If a terminal node has depth L and cost C, then it matches
            # target[i-L+1 : i+1], so:
            #   dp[i+1] = min(dp[i+1], dp[i+1-L] + C)
            # --------------------------------------------------------------
            check_state = state
            while True:
                terminal_cost = self.nodes[check_state].best_cost
                if terminal_cost is not None:
                    word_len = self.nodes[check_state].depth
                    start_prefix_len = (i + 1) - word_len

                    # Only update if the prefix before this word is reachable.
                    if dp[start_prefix_len] != inf:
                        candidate = dp[start_prefix_len] + terminal_cost
                        if candidate < dp[i + 1]:
                            dp[i + 1] = candidate

                if check_state == 0:
                    break
                check_state = self.nodes[check_state].fail

        return -1 if dp[n] == inf else dp[n]


if __name__ == "__main__":
    solution = Solution()

    # Example 1
    target1 = "ababa"
    words1 = ["ab", "aba", "ba", "a"]
    costs1 = [4, 5, 2, 10]
    result1 = solution.minimum_cost(target1, words1, costs1)
    print(result1)  # Expected: 9

    # Example 2
    target2 = "codecode"
    words2 = ["co", "code", "de", "odec"]
    costs2 = [3, 8, 4, 5]
    result2 = solution.minimum_cost(target2, words2, costs2)
    print(result2)  # Expected: 14