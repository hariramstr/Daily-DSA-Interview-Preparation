"""
Title: Count Distinct Palindrome Pairs in a Word Stream
Difficulty: Hard
Topic: Hashing

Problem Description:
You are given an array of lowercase strings `words`, where each string represents a token
observed in a live text stream. Two different indices `i` and `j` form a valid palindrome
pair if `i != j` and the concatenation `words[i] + words[j]` is a palindrome.

Your task is to count how many distinct ordered index pairs `(i, j)` are valid.

Important details:
- Order matters, so `(i, j)` and `(j, i)` are different pairs.
- Repeated words are allowed, and each index is treated separately.
- Empty strings may appear.
- `(i, i)` is never allowed.

Constraints:
- 1 <= words.length <= 10^5
- 0 <= words[i].length <= 100
- words[i] consists only of lowercase English letters
- The sum of all word lengths does not exceed 3 * 10^5

Examples:
1.
Input: ["bat", "tab", "cat"]
Output: 2

2.
Input: ["", "aba", "xy", "yx", "a"]
Output: 8

Efficient approach idea:
Use hashing and palindrome-compatible split checks instead of brute force over all pairs.
"""

from collections import defaultdict
from typing import DefaultDict, Dict, List, Set, Tuple


class Solution:
    def _is_palindrome(self, text: str) -> bool:
        """
        Check whether a string is a palindrome.

        Args:
            text: The string to test.

        Returns:
            True if the string reads the same forward and backward, otherwise False.

        Time complexity:
            O(len(text))

        Space complexity:
            O(1)
        """
        return text == text[::-1]

    def count_palindrome_pairs(self, words: List[str]) -> int:
        """
        Count the number of distinct ordered index pairs (i, j) such that:
        - i != j
        - words[i] + words[j] is a palindrome

        The algorithm uses a hash map from word -> list of indices where that word appears.
        For each word, we try every split position:
        - If the suffix is a palindrome, then we need a word equal to reverse(prefix)
          to append on the right.
        - If the prefix is a palindrome, then we need a word equal to reverse(suffix)
          to place on the left.

        We store results in a set of ordered index pairs to guarantee correctness and
        avoid double counting when the same pair is discovered through multiple split paths.

        Args:
            words: List of lowercase strings.

        Returns:
            Total number of valid ordered pairs.

        Time complexity:
            O(sum(len(word)^2) + number_of_reported_pairs)
            Since each word length is at most 100, this is efficient in practice.

        Space complexity:
            O(n + number_of_reported_pairs)
            For the hash map and the set of discovered pairs.
        """
        # This dictionary maps each exact word to all indices where it appears.
        # We store a list because duplicates matter:
        # if "ab" appears at indices 2 and 7, both are distinct possible partners.
        word_to_indices: DefaultDict[str, List[int]] = defaultdict(list)

        # Build the lookup table.
        for index, word in enumerate(words):
            word_to_indices[word].append(index)

        # We use a set of tuples (i, j) to ensure each ordered pair is counted once.
        # This is important because the same valid pair can be discovered from
        # different split positions, especially with empty strings or repeated patterns.
        pairs: Set[Tuple[int, int]] = set()

        # Process each word independently as the "left" or "right" side depending
        # on which palindrome condition is satisfied.
        for i, word in enumerate(words):
            length: int = len(word)

            # We try every split position from 0 to length inclusive.
            # Example: word = "abc"
            # split = 0 -> prefix="",   suffix="abc"
            # split = 1 -> prefix="a",  suffix="bc"
            # split = 2 -> prefix="ab", suffix="c"
            # split = 3 -> prefix="abc",suffix=""
            for split in range(length + 1):
                prefix: str = word[:split]
                suffix: str = word[split:]

                # Case 1:
                # If suffix is a palindrome, then:
                # word + candidate will be a palindrome if candidate == reverse(prefix)
                #
                # Why?
                # word = prefix + suffix
                # If suffix is already palindromic, then appending reverse(prefix)
                # creates:
                # prefix + suffix + reverse(prefix)
                # which mirrors around the palindromic suffix.
                #
                # This discovers ordered pairs of the form (i, j).
                if self._is_palindrome(suffix):
                    needed_right: str = prefix[::-1]

                    if needed_right in word_to_indices:
                        for j in word_to_indices[needed_right]:
                            # We must not pair a word with itself at the same index.
                            if i != j:
                                pairs.add((i, j))

                # Case 2:
                # If prefix is a palindrome, then:
                # candidate + word will be a palindrome if candidate == reverse(suffix)
                #
                # Why?
                # candidate + word
                # = reverse(suffix) + prefix + suffix
                # and if prefix is palindromic, this mirrors correctly.
                #
                # This discovers ordered pairs of the form (j, i).
                #
                # Important subtlety:
                # We skip split == 0 here to avoid duplicate discovery with Case 1.
                # At split == 0, prefix="" is always a palindrome, and this branch
                # would often rediscover pairs already found elsewhere.
                # Skipping split == 0 is a standard trick that preserves correctness.
                if split > 0 and self._is_palindrome(prefix):
                    needed_left: str = suffix[::-1]

                    if needed_left in word_to_indices:
                        for j in word_to_indices[needed_left]:
                            if i != j:
                                pairs.add((j, i))

        return len(pairs)


if __name__ == "__main__":
    solution = Solution()

    sample_words_1: List[str] = ["bat", "tab", "cat"]
    result_1: int = solution.count_palindrome_pairs(sample_words_1)
    print("Input:", sample_words_1)
    print("Output:", result_1)
    print("Expected:", 2)
    print()

    sample_words_2: List[str] = ["", "aba", "xy", "yx", "a"]
    result_2: int = solution.count_palindrome_pairs(sample_words_2)
    print("Input:", sample_words_2)
    print("Output:", result_2)
    print("Expected:", 6)
    print()

    # Additional checks to demonstrate duplicate handling and empty strings.
    sample_words_3: List[str] = ["a", "", "a"]
    result_3: int = solution.count_palindrome_pairs(sample_words_3)
    print("Input:", sample_words_3)
    print("Output:", result_3)
    print("Expected:", 6)
    print()

    sample_words_4: List[str] = ["ab", "ba", "ab"]
    result_4: int = solution.count_palindrome_pairs(sample_words_4)
    print("Input:", sample_words_4)
    print("Output:", result_4)
    print("Expected:", 4)