"""
Title: Maximum Points from Segmenting a Review String

Problem Description:
You are building a text scoring system for customer reviews. A review is represented
as a lowercase string `s`. You are also given a dictionary of approved phrases,
where each phrase has an integer score. Your task is to split the entire string into
a sequence of non-overlapping dictionary phrases so that every character in `s`
belongs to exactly one chosen phrase. Among all valid segmentations, return the
maximum total score. If it is impossible to segment the full string using only the
given phrases, return `-1`.

Each phrase may be used any number of times as long as it matches a substring
exactly. Scores may be positive, zero, or negative, so a valid solution is not
always the one with the fewest segments. This means a greedy choice is not
sufficient; you must consider future decisions when choosing a phrase at a position.

Return only the maximum score, not the actual segmentation.

Constraints:
- 1 <= s.length <= 5000
- 1 <= phrases.length <= 2000
- Sum of lengths of all phrases <= 20000
- 1 <= phrase.length <= 50
- -10^4 <= score <= 10^4
- s and all phrases consist only of lowercase English letters
- Phrase strings in the input are unique
"""

from typing import Dict, List, Optional, Tuple


class TrieNode:
    """Trie node used to store dictionary phrases for efficient prefix matching."""

    def __init__(self) -> None:
        """
        Initialize an empty trie node.

        Args:
            None

        Returns:
            None

        Time complexity:
            O(1)

        Space complexity:
            O(1)
        """
        self.children: Dict[str, "TrieNode"] = {}
        self.score: Optional[int] = None


class Solution:
    def build_trie(self, phrases: List[List[object]]) -> TrieNode:
        """
        Build a trie from the list of phrases and their scores.

        Args:
            phrases: A list where each element is [phrase_string, score].

        Returns:
            The root node of the constructed trie.

        Time complexity:
            O(total_length_of_all_phrases)

        Space complexity:
            O(total_length_of_all_phrases)
        """
        root = TrieNode()

        # We insert every phrase into the trie character by character.
        # This allows us to later walk through the review string starting
        # from any index and quickly discover all dictionary phrases that
        # match that position.
        for phrase, score in phrases:
            node = root

            # Move through the trie, creating child nodes as needed.
            for ch in phrase:
                if ch not in node.children:
                    node.children[ch] = TrieNode()
                node = node.children[ch]

            # Mark the end of a complete phrase and store its score.
            # The problem guarantees phrase strings are unique, so there is
            # no ambiguity about overwriting an existing score.
            node.score = int(score)

        return root

    def max_score_segmentation(self, s: str, phrases: List[List[object]]) -> int:
        """
        Compute the maximum total score for segmenting the entire string.

        This uses bottom-up dynamic programming:
        - dp[i] = maximum score obtainable by segmenting s[i:]
        - If s[i:] cannot be fully segmented, dp[i] stays as negative infinity
        - The answer is dp[0], or -1 if no full segmentation exists

        A trie is used so that from each index i, we only explore phrase matches
        that actually follow the characters in s, instead of checking every phrase
        individually.

        Args:
            s: The review string to segment.
            phrases: A list where each element is [phrase_string, score].

        Returns:
            The maximum total score for a full valid segmentation, or -1 if impossible.

        Time complexity:
            O(n * max_phrase_length), where n = len(s)
            Because from each index we walk at most the maximum phrase length
            through the trie.

        Space complexity:
            O(n + total_length_of_all_phrases)
        """
        # Build the trie once. This is more efficient than repeatedly checking
        # every phrase against every position in the string.
        root = self.build_trie(phrases)

        n = len(s)

        # We use a very small sentinel value to represent "impossible".
        # We do not use None in the DP array because numeric comparisons and
        # additions are simpler with integers.
        neg_inf = -10**18

        # dp[i] means:
        # "What is the best score we can achieve by segmenting the suffix s[i:]?"
        #
        # Base case:
        # dp[n] = 0 because the empty suffix needs no phrases and contributes
        # zero additional score.
        dp: List[int] = [neg_inf] * (n + 1)
        dp[n] = 0

        # We fill the DP table from right to left.
        # Why right to left?
        # Because when computing dp[i], we may need dp[j + 1] or more generally
        # dp[end_index] for phrases that start at i and end before that index.
        # Those future suffix answers must already be known.
        for i in range(n - 1, -1, -1):
            node = root

            # Starting from position i, walk forward through the string while
            # simultaneously walking down the trie.
            #
            # If at some point the current character is not a child in the trie,
            # then no longer phrase can match either, so we stop early.
            for j in range(i, n):
                ch = s[j]

                if ch not in node.children:
                    break

                node = node.children[ch]

                # If node.score is not None, then s[i:j+1] is a complete phrase.
                # That means we can try taking this phrase and then append the
                # best possible segmentation of the remaining suffix s[j+1:].
                if node.score is not None and dp[j + 1] != neg_inf:
                    candidate = node.score + dp[j + 1]

                    # Keep the best score among all valid phrase choices starting at i.
                    if candidate > dp[i]:
                        dp[i] = candidate

        # If dp[0] is still impossible, the whole string cannot be segmented.
        return dp[0] if dp[0] != neg_inf else -1


if __name__ == "__main__":
    solution = Solution()

    # Example 1
    s1 = "applepieapple"
    phrases1: List[List[object]] = [
        ["apple", 5],
        ["pie", 3],
        ["app", 2],
        ["lepie", 4],
    ]
    result1 = solution.max_score_segmentation(s1, phrases1)
    print(result1)  # Expected: 13

    # Example 2
    s2 = "catsandog"
    phrases2: List[List[object]] = [
        ["cat", 4],
        ["cats", 7],
        ["and", 3],
        ["sand", 5],
        ["dog", 6],
    ]
    result2 = solution.max_score_segmentation(s2, phrases2)
    print(result2)  # Expected: -1

    # Additional quick sanity check with negative scores:
    # Full segmentation is still required, so even negative phrases may need to be used.
    s3 = "aaaa"
    phrases3: List[List[object]] = [
        ["a", -1],
        ["aa", 3],
    ]
    result3 = solution.max_score_segmentation(s3, phrases3)
    print(result3)  # One optimal segmentation: "aa" + "aa" => 6