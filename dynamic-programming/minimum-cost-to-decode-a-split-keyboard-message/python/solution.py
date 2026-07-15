"""
Title: Minimum Cost to Decode a Split Keyboard Message

Problem Description:
A mobile device recorded a typed message, but its keyboard firmware was corrupted.
Instead of storing the intended characters directly, the device stored a string `s`
of lowercase letters representing raw key scan groups.

You are also given a dictionary of valid words. Each dictionary word `w` has:
- decodeCost: positive integer
- splitPenalty: positive integer

A word `w` can decode a contiguous substring of `s` if that substring can be
partitioned into one or more non-empty pieces whose concatenation is exactly `w`,
and every piece is either kept in order or reversed before being matched.

If the word is split into k pieces, the total cost for using that word is:
    decodeCost + (k - 1) * splitPenalty

A word may be reused any number of times.

Goal:
Decode the entire string `s` into a sequence of dictionary words with minimum total
cost. If impossible, return -1.

Constraints:
- 1 <= s.length <= 400
- 1 <= dictionary.length <= 120
- Sum of lengths of all dictionary words <= 2500
- 1 <= word.length <= 40
- 1 <= decodeCost, splitPenalty <= 10^6
- All strings contain only lowercase English letters
"""

from typing import List, Dict, Tuple


class Solution:
    def _min_piece_count(self, word: str, target: str) -> int:
        """
        Compute the minimum number of pieces needed so that `word` can decode `target`.

        A valid decoding means:
        - We partition `word` into contiguous non-empty pieces.
        - For each piece, when matched against the corresponding part of `target`,
          that piece is either used as-is or reversed.
        - The concatenation of all matched pieces must equal `target`.

        This method returns the minimum number of pieces among all valid partitions.
        If no valid partition exists, it returns a large sentinel value.

        Args:
            word: Dictionary word being tested.
            target: Substring from the message `s` with the same length as `word`.

        Returns:
            Minimum number of pieces needed, or a large sentinel if impossible.

        Time complexity:
            O(m^3), where m = len(word). Since m <= 40, this is acceptable.

        Space complexity:
            O(m), using a 1D DP array.
        """
        m: int = len(word)
        inf: int = 10**9

        # dp[i] = minimum number of pieces needed to decode target[0:i]
        # using word[0:i] in some valid partitioning.
        #
        # Important observation:
        # Because the pieces must come from a partition of the word in order,
        # and they must cover the target in order as well, if we decide that
        # the last piece has length L, then:
        #   word[i-L:i] must match target[i-L:i]
        # either directly or reversed.
        #
        # This gives a clean prefix DP.
        dp: List[int] = [inf] * (m + 1)
        dp[0] = 0

        # We build the answer for every prefix length i from smaller prefixes.
        for i in range(1, m + 1):
            # Try every possible last piece ending at position i.
            for j in range(i):
                # The candidate piece in the word is word[j:i].
                piece_word: str = word[j:i]

                # The corresponding piece in the target must be target[j:i]
                # because the partition boundaries are aligned by total consumed length.
                piece_target: str = target[j:i]

                # This piece is valid if it matches directly or reversed.
                if piece_word == piece_target or piece_word[::-1] == piece_target:
                    if dp[j] + 1 < dp[i]:
                        dp[i] = dp[j] + 1

        return dp[m]

    def minimum_decode_cost(self, s: str, dictionary: List[List[object]]) -> int:
        """
        Compute the minimum total cost to decode the entire string.

        We use dynamic programming over the message positions:
        - dp[i] = minimum cost to decode the prefix s[0:i]

        Transition:
        For each starting position i, try every dictionary word.
        If the word length is L and i + L <= n, test whether the substring
        s[i:i+L] can be decoded by that word. If yes, let the minimum number
        of pieces be k, then:
            dp[i + L] = min(dp[i + L], dp[i] + decodeCost + (k - 1) * splitPenalty)

        Args:
            s: The recorded message string.
            dictionary: List of [word, decodeCost, splitPenalty].

        Returns:
            Minimum total decoding cost, or -1 if impossible.

        Time complexity:
            Let n = len(s), D = number of dictionary entries, and M = max word length.
            For each position and each word, we may run O(M^3) matching.
            Total: O(n * D * M^3)
            With constraints n <= 400 and M <= 40, this is feasible.

        Space complexity:
            O(n) for the main DP array, plus O(M) temporary space per word check.
        """
        n: int = len(s)
        inf: int = 10**18

        # dp[i] = minimum cost to decode the first i characters of s.
        # So:
        #   dp[0] = 0   (empty prefix costs nothing)
        #   dp[n] = answer if reachable
        dp: List[int] = [inf] * (n + 1)
        dp[0] = 0

        # To avoid repeatedly unpacking and converting types, normalize the dictionary.
        words: List[Tuple[str, int, int]] = []
        for entry in dictionary:
            word: str = str(entry[0])
            decode_cost: int = int(entry[1])
            split_penalty: int = int(entry[2])
            words.append((word, decode_cost, split_penalty))

        # Process every prefix endpoint in increasing order.
        for i in range(n + 1):
            # If this prefix is unreachable, there is no reason to extend it.
            if dp[i] == inf:
                continue

            # Try placing every dictionary word starting at position i.
            for word, decode_cost, split_penalty in words:
                length: int = len(word)
                end: int = i + length

                # The word can only cover a substring of exactly the same length.
                if end > n:
                    continue

                target: str = s[i:end]

                # Compute the minimum number of pieces needed for this word
                # to decode this exact substring.
                piece_count: int = self._min_piece_count(word, target)

                # If impossible, skip this transition.
                if piece_count >= 10**9:
                    continue

                # Cost formula from the statement:
                # decodeCost + (number_of_pieces - 1) * splitPenalty
                use_cost: int = decode_cost + (piece_count - 1) * split_penalty

                # Standard shortest-path / DP relaxation.
                if dp[i] + use_cost < dp[end]:
                    dp[end] = dp[i] + use_cost

        return -1 if dp[n] == inf else dp[n]


if __name__ == "__main__":
    solution = Solution()

    # Sample 1 from the prompt.
    # Under the exact formal rule implemented here, the result is computed by the algorithm.
    s1 = "tabeltcode"
    dictionary1 = [
        ["tablet", 5, 2],
        ["code", 3, 1],
        ["tab", 4, 1],
        ["let", 2, 1],
    ]
    result1 = solution.minimum_decode_cost(s1, dictionary1)
    print(result1)

    # Sample 2 from the prompt.
    s2 = "abdc"
    dictionary2 = [
        ["abcd", 6, 5],
        ["ab", 2, 1],
        ["cd", 2, 1],
    ]
    result2 = solution.minimum_decode_cost(s2, dictionary2)
    print(result2)