"""
Title: Longest Prefix Chain with One-Character Mutations

Problem Description:
You are given an array of distinct lowercase strings words. A string a can transition
to string b if and only if all of the following are true:
1. b is exactly one character longer than a
2. b starts with a prefix that differs from a in at most one position among the first |a| characters
3. the extra character in b may appear only at the end

In other words, you may extend a by appending one new character to the end, and while
comparing the original positions, you are allowed to mutate at most one existing character.

Your task is to compute the length of the longest possible chain of words where each next
word is reachable from the previous one by the rule above.

Constraints:
- 1 <= words.length <= 2 * 10^5
- 1 <= words[i].length <= 30
-  words[i] are distinct lowercase strings
- sum of all word lengths <= 2 * 10^6

Key observation:
For a word b of length L, a predecessor a of length L - 1 is valid if:
- a == b[:-1]  (zero mismatches), or
- a differs from b[:-1] in exactly one position

So for each target prefix p = b[:-1], we need the best chain value among:
- the exact word p
- any length-(L-1) word matching p with one position replaced by any character

Efficient hashing idea:
For each length group, maintain:
- exact_best[word] = best chain ending at that exact word
- pattern_best[masked_pattern] = best chain among words that fit that pattern,
  where one position is replaced by '*'

Then for a target prefix p, the best predecessor is:
max(
    exact_best.get(p, 0),
    max(pattern_best[p with one position masked] for each position)
)

This works because:
- exact match covers zero-mutation transitions
- masked patterns cover one-mutation transitions
"""

from typing import Dict, List
from collections import defaultdict


class Solution:
    def longestPrefixChain(self, words: List[str]) -> int:
        """
        Compute the maximum chain length under the "append one character and allow
        at most one mutation in previous positions" rule.

        Args:
            words: List of distinct lowercase strings.

        Returns:
            The length of the longest valid chain.

        Time complexity:
            O(total_characters * max_word_length)
            Since each word of length L creates/checks O(L) masked patterns,
            and max length is at most 30, this is effectively linear in input size.

        Space complexity:
            O(total_characters * max_word_length) in the worst case for stored patterns,
            though bounded well by the small maximum word length.
        """
        # ---------------------------------------------------------------------
        # Step 1: Group words by their length.
        #
        # Why?
        # A valid transition only goes from length k to length k + 1.
        # Therefore, dynamic programming should naturally process words in
        # increasing order of length.
        #
        # Example:
        # "ab" can only transition to words of length 3.
        # So when computing DP for length 3 words, we only need information
        # from length 2 words.
        # ---------------------------------------------------------------------
        words_by_length: Dict[int, List[str]] = defaultdict(list)
        max_len = 0

        for word in words:
            words_by_length[len(word)].append(word)
            if len(word) > max_len:
                max_len = len(word)

        # ---------------------------------------------------------------------
        # Step 2: Dynamic programming storage.
        #
        # dp[word] = longest valid chain ending at this exact word.
        #
        # We also maintain, for the PREVIOUS length only:
        # - prev_exact_best: maps exact word -> best chain ending there
        # - prev_pattern_best: maps masked pattern -> best chain among all words
        #   of previous length that fit that pattern
        #
        # A masked pattern means replacing one character by '*'.
        # Example:
        # word = "abc"
        # masked patterns:
        #   "*bc", "a*c", "ab*"
        #
        # If target prefix is "axc", then masking index 1 gives "a*c".
        # Any previous word that also gives "a*c" differs in at most one position.
        # ---------------------------------------------------------------------
        dp: Dict[str, int] = {}
        answer = 0

        prev_exact_best: Dict[str, int] = {}
        prev_pattern_best: Dict[str, int] = {}

        # ---------------------------------------------------------------------
        # Step 3: Process lengths from smallest to largest.
        #
        # For length 1 words:
        # They cannot have predecessors because length must increase by exactly 1,
        # so each starts a chain of length 1.
        #
        # For length L > 1:
        # Let prefix = word[:-1].
        # We want the best predecessor among length L-1 words that differ from
        # prefix in at most one position.
        #
        # That means:
        # predecessor_best = max(
        #     exact match on prefix,
        #     any word matching one of prefix's masked patterns
        # )
        #
        # Then:
        # dp[word] = predecessor_best + 1
        # If no predecessor exists, dp[word] = 1
        # ---------------------------------------------------------------------
        for length in range(1, max_len + 1):
            current_words = words_by_length.get(length, [])
            if not current_words:
                # No words of this length, so the next length cannot use any
                # predecessors from this missing length.
                prev_exact_best = {}
                prev_pattern_best = {}
                continue

            current_exact_best: Dict[str, int] = {}
            current_pattern_best: Dict[str, int] = {}

            for word in current_words:
                # -------------------------------------------------------------
                # Base chain length is always at least 1:
                # the word can stand alone as a chain of length 1.
                # -------------------------------------------------------------
                best_chain = 1

                if length > 1:
                    # ---------------------------------------------------------
                    # The predecessor must compare against the first length - 1
                    # characters of the current word.
                    #
                    # Example:
                    # current word = "abca"
                    # predecessor length = 3
                    # target prefix = "abc"
                    #
                    # Valid predecessor examples:
                    # - "abc"  (0 mismatches)
                    # - "acc"  (1 mismatch at index 1)
                    # Invalid:
                    # - "ab"   (wrong length)
                    # - "abda" (wrong length)
                    # ---------------------------------------------------------
                    prefix = word[:-1]

                    # ---------------------------------------------------------
                    # Case A: zero mismatches.
                    # If the exact prefix exists as a previous word, it is a
                    # valid predecessor.
                    # ---------------------------------------------------------
                    if prefix in prev_exact_best:
                        best_chain = max(best_chain, prev_exact_best[prefix] + 1)

                    # ---------------------------------------------------------
                    # Case B: exactly one mismatch.
                    #
                    # We generate all masked versions of the prefix. For each
                    # masked pattern, if some previous word matched that pattern,
                    # then that previous word differs from prefix in at most one
                    # position at the masked index.
                    #
                    # Example:
                    # prefix = "abc"
                    # patterns:
                    #   "*bc", "a*c", "ab*"
                    #
                    # If previous word "acc" exists, then masking index 1:
                    #   "a*c"
                    # matches prefix's "a*c"
                    # so "acc" is a valid predecessor.
                    # ---------------------------------------------------------
                    for i in range(length - 1):
                        pattern = prefix[:i] + "*" + prefix[i + 1 :]
                        if pattern in prev_pattern_best:
                            best_chain = max(best_chain, prev_pattern_best[pattern] + 1)

                # Save DP result for this exact word.
                dp[word] = best_chain

                # Update global answer.
                if best_chain > answer:
                    answer = best_chain

                # -------------------------------------------------------------
                # Build exact and masked summary structures for this length.
                #
                # current_exact_best[word]:
                #   best chain ending at this exact word
                #
                # current_pattern_best[pattern]:
                #   best chain among all words of this length that fit pattern
                #
                # Even though words are distinct, using max() is the safest and
                # most general way to maintain "best chain for this key".
                # -------------------------------------------------------------
                current_exact_best[word] = max(current_exact_best.get(word, 0), best_chain)

                for i in range(length):
                    pattern = word[:i] + "*" + word[i + 1 :]
                    existing = current_pattern_best.get(pattern, 0)
                    if best_chain > existing:
                        current_pattern_best[pattern] = best_chain

            # -----------------------------------------------------------------
            # Move current length summaries into "previous length" storage.
            #
            # On the next iteration, words of length L+1 will use these as their
            # predecessor lookup tables.
            # -----------------------------------------------------------------
            prev_exact_best = current_exact_best
            prev_pattern_best = current_pattern_best

        return answer

    def longestStrChain(self, words: List[str]) -> int:
        """
        Compatibility wrapper using a common interview-style method name.

        Args:
            words: List of distinct lowercase strings.

        Returns:
            The length of the longest valid chain.

        Time complexity:
            Same as longestPrefixChain: O(total_characters * max_word_length)

        Space complexity:
            Same as longestPrefixChain.
        """
        return self.longestPrefixChain(words)


if __name__ == "__main__":
    solution = Solution()

    # Example 1 from the prompt.
    words1 = ["a", "ab", "ac", "abc", "acc", "abca", "acca"]
    result1 = solution.longestPrefixChain(words1)
    print(result1)  # Expected: 5

    # Example 2 from the prompt.
    # Note:
    # The textual explanation in the prompt is inconsistent, but the algorithm
    # follows the formal rule exactly.
    words2 = ["cat", "bat", "bate", "bath", "batch", "catch", "cater"]
    result2 = solution.longestPrefixChain(words2)
    print(result2)

    # Additional small sanity checks.
    words3 = ["a"]
    print(solution.longestPrefixChain(words3))  # Expected: 1

    words4 = ["a", "b", "ba", "bb", "bba", "bbb", "bbba"]
    print(solution.longestPrefixChain(words4))  # One possible expected result: 4