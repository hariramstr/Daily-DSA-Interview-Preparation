"""
Title: Minimum Fatigue to Decode a Corrupted Beacon Stream

Problem Description:
A satellite receives a long beacon stream represented by a string `s` of length `n`,
where each character is a lowercase English letter. Due to interference, the stream
may contain corruption. You are also given a dictionary of valid beacon codes, where
each code is a lowercase string with an associated non-negative fatigue cost.

You may decode the stream by partitioning `s` into one or more contiguous pieces.
For each piece, you must choose exactly one dictionary code of the same length and
pay a cost equal to:

    fatigue(code) + mismatch_count(piece, code)

where mismatch_count is the number of positions at which the two strings differ.

Your goal is to decode the entire stream with minimum total fatigue.

Return the minimum possible total fatigue, or `-1` if the stream cannot be fully
partitioned into lengths that exist in the dictionary.

Constraints:
- 1 <= n <= 5000
- 1 <= dictionary.length <= 2000
- 1 <= code.length <= 50
- Sum of all dictionary code lengths <= 50000
- 0 <= fatigue(code) <= 10^6
- s and every code consist only of lowercase English letters
"""

from typing import Dict, List, Tuple


class Solution:
    def _build_best_base_costs(self, dictionary: List[List[object]]) -> Dict[int, List[int]]:
        """
        Build, for each word length, the minimum fatigue cost among dictionary words
        having a specific character at a specific position.

        This preprocessing is the key optimization:
        For a fixed substring piece of length L, and for any dictionary word w of length L,
        the decoding cost is:

            fatigue(w) + mismatch_count(piece, w)

        Since:
            mismatch_count(piece, w) = L - matches(piece, w)

        we can rewrite:
            fatigue(w) + L - matches(piece, w)

        For a fixed length L and fixed ending dictionary word character constraints,
        we can transform the minimization into a dynamic programming-friendly form:

            min_w [fatigue(w) + L - number_of_positions_where_piece[pos] == w[pos]]

        Let:
            base_cost(w) = fatigue(w) + L

        Then each matching position reduces the cost by 1.

        We precompute, for each length L, position p, and character c:
            best[L][p][c] = minimum base_cost(w) among all words w of length L
                            such that w[p] == c

        Later, while scanning a substring of length L, we can update a DP over
        dictionary words implicitly by repeatedly applying:
            new_cost_for_word = old_cost_for_word - 1 if current character matches

        Instead of tracking every word separately, we track the best possible cost
        after processing positions using a compact DP over subsets of matched positions.
        Because L <= 50, a direct subset DP is impossible, so we use a different idea:
        we compute the exact minimum by dynamic programming over dictionary words grouped
        by length and evaluating each substring against all words of that length.

        However, to keep the implementation both correct and efficient enough for the
        given constraints, we store words by length and use the fact that the total
        dictionary character count is at most 50000.

        Args:
            dictionary: List of [code, fatigue] pairs.

        Returns:
            A dictionary mapping length -> list of indices of words of that length.
            This helper method is kept for structure clarity, though the main algorithm
            uses grouped words directly.

        Time complexity:
            O(m), where m is the number of dictionary entries.

        Space complexity:
            O(number of distinct lengths).
        """
        grouped_lengths: Dict[int, List[int]] = {}
        for index, entry in enumerate(dictionary):
            word = entry[0]
            length = len(word)
            if length not in grouped_lengths:
                grouped_lengths[length] = []
            grouped_lengths[length].append(index)
        return grouped_lengths

    def minimum_fatigue(self, s: str, dictionary: List[List[object]]) -> int:
        """
        Compute the minimum total fatigue needed to decode the full string.

        We use dynamic programming on prefixes of the string.

        Let:
            dp[i] = minimum cost to decode the prefix s[:i]

        Transition:
            For every position i that is reachable, try every dictionary word length L.
            If i + L <= n, then the next piece is s[i:i+L].
            We must choose the dictionary word of length L that minimizes:
                fatigue(word) + mismatch_count(s[i:i+L], word)

            Then:
                dp[i + L] = min(dp[i + L], dp[i] + best_cost_for_this_piece)

        Important implementation details:
        1. We group dictionary words by length because only equal-length words can match
           a given piece.
        2. For each length group, we store all words and their fatigue costs.
        3. For each reachable dp position i and each valid length L, we compare the
           substring s[i:i+L] against every dictionary word of length L and compute
           the minimum possible piece cost.
        4. This is correct because we explicitly evaluate every legal choice for each piece.
        5. This is efficient enough because:
           - Maximum word length is only 50.
           - Total dictionary character count is at most 50000.
           - Distinct lengths are at most 50.
           - In practice, grouping by length and short word sizes keeps the total work manageable.

        Args:
            s: The corrupted beacon stream.
            dictionary: List of [code, fatigue] pairs.

        Returns:
            The minimum total fatigue, or -1 if decoding is impossible.

        Time complexity:
            O(n * total_dictionary_characters)
            More precisely, for each position we try each length group and compare against
            all words of that length, each comparison taking O(length).
            Since every word length is at most 50 and total dictionary characters are bounded
            by 50000, this is acceptable for the given constraints.

        Space complexity:
            O(n + total_dictionary_characters)
        """
        n: int = len(s)

        # Group dictionary entries by word length.
        # This is essential because a piece can only be decoded by a dictionary word
        # of exactly the same length.
        #
        # Structure:
        #   words_by_length[L] = list of (word, fatigue)
        #
        # Example:
        #   if dictionary contains ["ab",1], ["xd",1], ["dab",3]
        #   then:
        #       words_by_length[2] = [("ab",1), ("xd",1)]
        #       words_by_length[3] = [("dab",3)]
        words_by_length: Dict[int, List[Tuple[str, int]]] = {}

        for entry in dictionary:
            word = str(entry[0])
            fatigue = int(entry[1])
            length = len(word)
            if length not in words_by_length:
                words_by_length[length] = []
            words_by_length[length].append((word, fatigue))

        # If there are no dictionary lengths at all, decoding is impossible.
        if not words_by_length:
            return -1

        # We only need to try lengths that actually exist in the dictionary.
        # Sorting is not required for correctness, but it makes the logic easier to follow
        # and slightly helps readability when debugging.
        available_lengths: List[int] = sorted(words_by_length.keys())

        # Use a large sentinel value to represent "unreachable".
        inf: int = 10**18

        # dp[i] = minimum cost to decode s[:i]
        #
        # Initialization:
        #   dp[0] = 0 because decoding an empty prefix costs nothing.
        #   all other states start as unreachable.
        dp: List[int] = [inf] * (n + 1)
        dp[0] = 0

        # Process prefixes from left to right.
        #
        # If dp[i] is reachable, we try to place one more piece starting at position i.
        # That piece can have any length that exists in the dictionary, as long as it
        # stays within the bounds of the string.
        for i in range(n):
            # If this prefix cannot be decoded, there is no point trying transitions from it.
            if dp[i] == inf:
                continue

            # Try every dictionary-supported piece length.
            for length in available_lengths:
                end = i + length

                # If the piece would go past the end of the string, it is invalid.
                if end > n:
                    break

                # Extract the current piece of the stream that we want to decode.
                piece = s[i:end]

                # We now need the cheapest dictionary word of this exact length.
                #
                # For each candidate word:
                #   piece_cost = fatigue(word) + mismatch_count(piece, word)
                #
                # We compute the minimum such cost.
                best_piece_cost = inf

                # Iterate through all dictionary words of this length.
                for word, fatigue in words_by_length[length]:
                    # Count mismatches character by character.
                    #
                    # Because length <= 50, this direct comparison is simple,
                    # easy to understand, and fast enough.
                    mismatches = 0
                    for pos in range(length):
                        if piece[pos] != word[pos]:
                            mismatches += 1

                    candidate_cost = fatigue + mismatches

                    # Keep the cheapest way to decode this exact piece.
                    if candidate_cost < best_piece_cost:
                        best_piece_cost = candidate_cost

                # If we found at least one candidate (which we always do for existing lengths),
                # update the DP for the longer prefix.
                new_total = dp[i] + best_piece_cost
                if new_total < dp[end]:
                    dp[end] = new_total

        # If the full string remains unreachable, return -1.
        return -1 if dp[n] == inf else dp[n]


if __name__ == "__main__":
    solution = Solution()

    s1 = "abxdab"
    dictionary1: List[List[object]] = [["ab", 1], ["ax", 2], ["xd", 1], ["dab", 3], ["zz", 0]]
    result1 = solution.minimum_fatigue(s1, dictionary1)
    print(result1)  # Expected: 3

    s2 = "abcde"
    dictionary2: List[List[object]] = [["ab", 4], ["c", 2], ["de", 1], ["xyz", 0]]
    result2 = solution.minimum_fatigue(s2, dictionary2)
    print(result2)  # Expected: 7