"""
Title: Minimum Energy to Decode a Beacon Stream

Problem Description:
A remote beacon sends a message as a string `s` consisting only of lowercase English letters.
Your decoder reads the message from left to right and must split it into valid signal blocks.
You are given a dictionary `patterns`, where each pattern is a valid block that may be used
any number of times. Decoding a block has an energy cost equal to `block.length * block.length`.

However, if two consecutive chosen blocks start with the same letter, the second block receives
a discount of `d` energy units. The energy cost of a block can never go below 0 after applying
the discount.

Your task is to compute the minimum total energy required to decode the entire string exactly.
If it is impossible to split the whole string into valid patterns, return `-1`.

Formally, if the chosen sequence of blocks is `b1, b2, ..., bk`, then they must concatenate
to exactly `s`. The cost of `b1` is `len(b1)^2`. For each `i > 1`, the cost of `bi` is
max(0, len(bi)^2 - d) if `bi` and `b(i-1)` start with the same character; otherwise it is
len(bi)^2.

Constraints:
- 1 <= s.length <= 5000
- 1 <= patterns.length <= 2000
- 1 <= patterns[i].length <= 50
- 0 <= d <= 2500
- s and all patterns[i] contain only lowercase English letters
- The sum of lengths of all patterns does not exceed 20000
"""

from typing import Dict, List, Tuple


class TrieNode:
    """Simple trie node used to match patterns starting at a given position."""

    def __init__(self) -> None:
        """
        Initialize an empty trie node.

        Time complexity:
            O(1)

        Space complexity:
            O(1)
        """
        self.children: Dict[str, "TrieNode"] = {}
        self.word_lengths: List[int] = []


class Solution:
    def _build_trie(self, patterns: List[str]) -> TrieNode:
        """
        Build a trie containing all valid patterns.

        We store the length of a pattern at the terminal node. Even though duplicate patterns
        do not change the answer, storing lengths in a list keeps the implementation simple
        and correct.

        Args:
            patterns: List of valid block strings.

        Returns:
            Root node of the trie.

        Time complexity:
            O(total length of all patterns)

        Space complexity:
            O(total length of all patterns)
        """
        root = TrieNode()

        for pattern in patterns:
            node = root
            for ch in pattern:
                if ch not in node.children:
                    node.children[ch] = TrieNode()
                node = node.children[ch]
            node.word_lengths.append(len(pattern))

        return root

    def minimum_energy(self, s: str, patterns: List[str], d: int) -> int:
        """
        Compute the minimum total energy needed to split the string exactly into valid patterns.

        Dynamic programming idea:
        - We process the string from left to right.
        - At every position i, we keep track of the minimum cost to decode s[:i] where the
          previous chosen block starts with each possible letter 'a'..'z'.
        - This "previous starting letter" is exactly the information needed to decide whether
          the next block receives the discount.
        - From position i, we use the trie to find every pattern that matches s starting at i.
          For each matching pattern, we transition to position j = i + len(pattern), updating
          the state corresponding to the starting letter of the newly chosen block.

        State definition:
            dp[i][c] = minimum energy to decode prefix s[:i], where the last chosen block
                       starts with letter index c (0 for 'a', ..., 25 for 'z').

        Special handling:
            The first block has no previous block, so no discount applies. We handle this by
            starting transitions from position 0 separately.

        Args:
            s: The beacon stream string to decode.
            patterns: List of valid patterns that may be reused.
            d: Discount applied when two consecutive blocks start with the same letter.

        Returns:
            Minimum total energy, or -1 if exact decoding is impossible.

        Time complexity:
            O(n * L * 26), where:
            - n = len(s)
            - L = maximum pattern length (<= 50)
            The trie limits matching from each position to at most L characters.

        Space complexity:
            O(n * 26 + total pattern length)
        """
        n = len(s)
        root = self._build_trie(patterns)

        # A very large number used as "infinity" for unreachable states.
        inf = 10**18

        # dp[i][c]:
        #   minimum cost to decode s[:i]
        #   and the last chosen block starts with character index c.
        #
        # Why 26 columns?
        #   Because only the starting letter of the previous block matters for the discount.
        #   There are only 26 lowercase English letters, so this keeps the state compact.
        dp: List[List[int]] = [[inf] * 26 for _ in range(n + 1)]

        # ---------------------------------------------------------------------
        # Step 1: Handle the very first block separately.
        #
        # There is no previous block before the first one, so no discount can apply.
        # We match every pattern that starts at position 0 and initialize dp[end][start_char].
        # ---------------------------------------------------------------------
        node = root
        for end in range(n):
            ch = s[end]
            if ch not in node.children:
                break
            node = node.children[ch]

            # If one or more patterns end here, each gives us a valid first block.
            if node.word_lengths:
                block_len = end + 1
                start_idx = ord(s[0]) - ord("a")
                base_cost = block_len * block_len

                # Multiple identical patterns ending here do not improve the cost,
                # but iterating over stored lengths keeps the logic straightforward.
                for _ in node.word_lengths:
                    if base_cost < dp[end + 1][start_idx]:
                        dp[end + 1][start_idx] = base_cost

        # ---------------------------------------------------------------------
        # Step 2: For every reachable position i, try to place the next block.
        #
        # We only proceed from position i if at least one previous-state letter is reachable.
        # Then we walk the trie along s[i:], which efficiently enumerates all patterns that
        # match starting at i.
        # ---------------------------------------------------------------------
        for i in range(1, n):
            # Quick skip: if no state at position i is reachable, there is nothing to extend.
            if min(dp[i]) == inf:
                continue

            node = root

            # Walk forward from position i while the trie has matching edges.
            # Because pattern lengths are at most 50, this loop is naturally short.
            for end in range(i, n):
                ch = s[end]
                if ch not in node.children:
                    break
                node = node.children[ch]

                # If no pattern ends here, continue walking to see if a longer pattern matches.
                if not node.word_lengths:
                    continue

                block_len = end - i + 1
                start_idx = ord(s[i]) - ord("a")
                normal_cost = block_len * block_len
                discounted_cost = max(0, normal_cost - d)

                # -----------------------------------------------------------------
                # Transition logic:
                # We are choosing a new block s[i:end+1].
                # Its own starting letter is start_idx.
                #
                # For every possible previous starting letter prev_idx:
                #   - If prev_idx == start_idx, discount applies.
                #   - Otherwise, normal cost applies.
                #
                # Then we update dp[end+1][start_idx] because after choosing this block,
                # the "last chosen block starts with ..." information becomes start_idx.
                # -----------------------------------------------------------------
                best_new_cost = dp[end + 1][start_idx]

                for prev_idx in range(26):
                    prev_cost = dp[i][prev_idx]
                    if prev_cost == inf:
                        continue

                    add_cost = discounted_cost if prev_idx == start_idx else normal_cost
                    candidate = prev_cost + add_cost

                    if candidate < best_new_cost:
                        best_new_cost = candidate

                dp[end + 1][start_idx] = best_new_cost

        # ---------------------------------------------------------------------
        # Step 3: The answer is the best reachable state after consuming all n chars.
        # If all states are still infinity, exact decoding is impossible.
        # ---------------------------------------------------------------------
        answer = min(dp[n])
        return -1 if answer == inf else answer


if __name__ == "__main__":
    solution = Solution()

    # Example 1
    s1 = "ababa"
    patterns1 = ["a", "ab", "ba"]
    d1 = 2
    result1 = solution.minimum_energy(s1, patterns1, d1)
    print(result1)  # Expected: 5

    # Example 2
    s2 = "cable"
    patterns2 = ["ca", "ble", "cab"]
    d2 = 3
    result2 = solution.minimum_energy(s2, patterns2, d2)
    print(result2)  # Expected: -1