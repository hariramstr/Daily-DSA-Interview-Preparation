"""
Title: Prefix Replacement Suggestions

Problem Description:
You are building a text normalization tool for a search system. A dictionary of
approved root words is given, along with a list of query words typed by users.
For each query word, you must find the shortest dictionary root that is a prefix
of that query. If such a root exists, replace the query word with that root;
otherwise keep the original word unchanged.

In addition to returning the transformed list, you must also report how many
query words were actually replaced.

A Trie-based solution is especially suitable here because it allows efficient
prefix traversal and early stopping as soon as a terminal root is found, which
guarantees the shortest matching root is selected.
"""

from __future__ import annotations

from dataclasses import dataclass, field
from typing import Dict, List, Tuple


@dataclass
class TrieNode:
    """
    Node used inside the Trie.

    Attributes:
        children: Mapping from character to the next TrieNode.
        is_end: True if this node marks the end of a complete root word.
    """

    children: Dict[str, "TrieNode"] = field(default_factory=dict)
    is_end: bool = False


class Solution:
    def __init__(self) -> None:
        """
        Initialize the solution with an empty Trie root node.

        Args:
            None

        Returns:
            None

        Time complexity:
            O(1)

        Space complexity:
            O(1)
        """
        self.trie_root = TrieNode()

    def insert_root(self, root_word: str) -> None:
        """
        Insert one root word into the Trie.

        Args:
            root_word: A dictionary root to store in the Trie.

        Returns:
            None

        Time complexity:
            O(len(root_word))

        Space complexity:
            O(len(root_word)) in the worst case if all characters create new nodes
        """
        # We always begin insertion from the Trie root.
        current = self.trie_root

        # We walk through each character of the root word one by one.
        # If the path for a character does not exist yet, we create it.
        # If it already exists, we simply reuse the existing node.
        for char in root_word:
            if char not in current.children:
                current.children[char] = TrieNode()
            current = current.children[char]

        # After processing all characters, we mark the final node as a word ending.
        # This tells future searches that a complete root exists at this point.
        current.is_end = True

    def build_trie(self, roots: List[str]) -> None:
        """
        Build the Trie from the list of root words.

        Duplicate roots do not change the result, and the Trie naturally handles
        them because inserting the same path again simply marks the same end node.

        Args:
            roots: List of approved root words.

        Returns:
            None

        Time complexity:
            O(total characters in roots)

        Space complexity:
            O(total unique characters stored in the Trie)
        """
        # Insert every root into the Trie.
        # Even if duplicates appear, the Trie structure remains correct.
        for root_word in roots:
            self.insert_root(root_word)

    def find_shortest_prefix(self, query_word: str) -> str:
        """
        Find the shortest dictionary root that is a prefix of the given query word.

        We traverse the Trie character by character following the query word.
        The moment we reach a Trie node marked as the end of a root, we stop
        immediately and return that prefix. This early stop guarantees that the
        returned root is the shortest matching one.

        If traversal fails because a character path does not exist, then no root
        matches the query word, so we return the original word unchanged.

        Args:
            query_word: The user query word to normalize.

        Returns:
            The shortest matching root if one exists; otherwise the original word.

        Time complexity:
            O(len(query_word))

        Space complexity:
            O(len(query_word)) for building the returned prefix string incrementally
        """
        # Start from the Trie root for every new query word.
        current = self.trie_root

        # We collect characters as we traverse so that if we find a root,
        # we can return the exact prefix immediately.
        prefix_chars: List[str] = []

        # Process the query word from left to right.
        for char in query_word:
            # If the next character is not present in the Trie,
            # then no dictionary root can continue from here.
            # That means there is no valid prefix root for this query.
            if char not in current.children:
                return query_word

            # Move to the next Trie node and record the character
            # as part of the current prefix.
            current = current.children[char]
            prefix_chars.append(char)

            # The key idea:
            # As soon as we hit a node that marks the end of a root word,
            # we return immediately. Because we are scanning from left to right,
            # this is the shortest possible matching root.
            if current.is_end:
                return "".join(prefix_chars)

        # If we consumed the entire query word without finding an end marker,
        # then no stored root is a prefix of this query.
        return query_word

    def replace_words(
        self, roots: List[str], queries: List[str]
    ) -> Tuple[List[str], int]:
        """
        Replace each query word with the shortest matching root prefix, if any,
        and count how many words were actually replaced.

        Args:
            roots: List of approved root words.
            queries: List of query words to transform.

        Returns:
            A tuple containing:
            - transformed list of query words
            - number of words that were replaced

        Time complexity:
            O(total characters in roots + total characters in queries)

        Space complexity:
            O(total unique characters in roots + total characters in output)
        """
        # Build the Trie once from all roots.
        # This preprocessing makes each query lookup efficient.
        self.build_trie(roots)

        # This list will store the final transformed words in order.
        transformed: List[str] = []

        # This counter tracks how many query words changed.
        replaced_count = 0

        # Process each query independently.
        for query_word in queries:
            # Find the shortest matching root or return the original word.
            replacement = self.find_shortest_prefix(query_word)

            # Add the result to the output list.
            transformed.append(replacement)

            # Count a replacement only when the returned word differs
            # from the original query word.
            if replacement != query_word:
                replaced_count += 1

        return transformed, replaced_count


if __name__ == "__main__":
    # Example 1
    roots1 = ["cat", "bat", "rat"]
    queries1 = ["cattle", "battery", "rattle", "dog"]

    solution1 = Solution()
    transformed1, replaced_count1 = solution1.replace_words(roots1, queries1)

    print("Example 1")
    print("Roots:", roots1)
    print("Queries:", queries1)
    print("Transformed:", transformed1)
    print("Replaced Count:", replaced_count1)
    print()

    # Expected:
    # Transformed: ["cat", "bat", "rat", "dog"]
    # Replaced Count: 3

    # Example 2
    roots2 = ["a", "ab", "abc", "bcd"]
    queries2 = ["abcde", "abacus", "bcdx", "zzz"]

    solution2 = Solution()
    transformed2, replaced_count2 = solution2.replace_words(roots2, queries2)

    print("Example 2")
    print("Roots:", roots2)
    print("Queries:", queries2)
    print("Transformed:", transformed2)
    print("Replaced Count:", replaced_count2)

    # Expected:
    # Transformed: ["a", "a", "bcd", "zzz"]
    # Replaced Count: 3