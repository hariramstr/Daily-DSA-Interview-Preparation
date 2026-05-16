"""
Auto-Complete Sentence Builder
==============================

Problem Description:
You are building a simple auto-complete feature for a text editor. Given a list of
previously typed sentences and a partial input string, return all sentences from the
list that start with the given partial input, sorted in the order they were originally
inserted.

A sentence is considered a match if it begins with the exact characters of the partial
input (case-sensitive). If no sentences match, return an empty list.

You must implement your solution using a Trie data structure to efficiently store and
search the sentences.

Constraints:
- 1 <= sentences.length <= 200
- 1 <= sentences[i].length <= 100
- 1 <= partial.length <= 50
- All characters in sentences and partial are printable ASCII characters
- Sentences may contain spaces
- Duplicate sentences in the input list should each be returned individually
"""

from typing import List, Dict, Optional


class TrieNode:
    """
    Represents a single node in the Trie.

    Each node stores:
    - children: a dictionary mapping characters to child TrieNode objects
    - sentences: a list of full sentences that pass through this node
                 (in insertion order). We store the full sentence at every
                 node along its path so that once we navigate to the node
                 matching the last character of `partial`, we already have
                 all matching sentences ready to return.
    """

    def __init__(self) -> None:
        # Maps each character to the corresponding child TrieNode
        self.children: Dict[str, "TrieNode"] = {}
        # Stores full sentences that pass through (or end at) this node
        # Keeping them in insertion order is natural since we insert
        # sentences one by one from left to right in the input list.
        self.sentences: List[str] = []


class Solution:
    """
    Implements the Auto-Complete Sentence Builder using a Trie.

    Design rationale:
    -----------------
    A Trie (prefix tree) is the ideal data structure here because:
    1. It groups sentences by their shared prefixes naturally.
    2. Searching for all sentences that start with a given prefix takes
       O(P) time where P is the length of the prefix — independent of
       the number of sentences stored.
    3. We store the list of matching sentences directly at each node,
       so retrieval after navigation is O(1) (just return the list).
    """

    def __init__(self) -> None:
        # The root node represents the empty string (before any character)
        self.root: TrieNode = TrieNode()

    def _insert(self, sentence: str) -> None:
        """
        Insert a sentence into the Trie.

        For every character in the sentence we:
          1. Move to (or create) the corresponding child node.
          2. Append the full sentence to that node's `sentences` list.

        This means every node along the path of a sentence holds a
        reference to that sentence, making prefix-based retrieval instant.

        Args:
            sentence: The full sentence string to insert.

        Returns:
            None

        Time complexity:  O(L) where L = len(sentence)
        Space complexity: O(L) for the new nodes created (worst case)
        """
        # Start traversal from the root
        current_node: TrieNode = self.root

        for char in sentence:
            # ----------------------------------------------------------------
            # Step 1: If the current character doesn't have a child node yet,
            #         create one. This lazily builds the trie structure only
            #         for characters that actually appear in our sentences.
            # ----------------------------------------------------------------
            if char not in current_node.children:
                current_node.children[char] = TrieNode()

            # ----------------------------------------------------------------
            # Step 2: Move down to the child node for this character.
            # ----------------------------------------------------------------
            current_node = current_node.children[char]

            # ----------------------------------------------------------------
            # Step 3: Record the full sentence at this node.
            #
            # Why store the sentence at EVERY node along the path?
            # Because when we later search for a prefix like "hel", we
            # navigate to the node for 'l' (the last char of "hel") and
            # immediately read off all sentences stored there — no further
            # traversal needed.
            #
            # Duplicates are allowed per the problem constraints, so we
            # always append without checking for duplicates.
            # ----------------------------------------------------------------
            current_node.sentences.append(sentence)

    def _search_prefix(self, partial: str) -> List[str]:
        """
        Navigate the Trie along the characters of `partial` and return
        the list of sentences stored at the final node.

        Args:
            partial: The prefix string to search for.

        Returns:
            A list of sentences (in insertion order) that start with
            `partial`, or an empty list if no match is found.

        Time complexity:  O(P) where P = len(partial)
        Space complexity: O(1) extra (we just return a reference to the
                          existing list stored in the node)
        """
        # Begin at the root of the Trie
        current_node: TrieNode = self.root

        for char in partial:
            # ----------------------------------------------------------------
            # If at any point the current character is not a child of the
            # current node, no sentence in our Trie starts with this prefix.
            # Return an empty list immediately.
            # ----------------------------------------------------------------
            if char not in current_node.children:
                return []

            # Move to the child node corresponding to this character
            current_node = current_node.children[char]

        # ----------------------------------------------------------------
        # After consuming all characters of `partial`, `current_node` is
        # the node that represents the end of the prefix.  Every sentence
        # stored in `current_node.sentences` starts with `partial` because
        # we stored sentences at every node along their path during insert.
        #
        # Return a copy of the list so the internal Trie state is not
        # accidentally mutated by the caller.
        # ----------------------------------------------------------------
        return list(current_node.sentences)

    def auto_complete(self, sentences: List[str], partial: str) -> List[str]:
        """
        Given a list of sentences and a partial input string, return all
        sentences that start with `partial` in their original insertion order.

        This method:
          1. Builds a fresh Trie by inserting all sentences.
          2. Searches the Trie for the given prefix.
          3. Returns the matching sentences.

        Args:
            sentences: A list of previously typed sentences.
            partial:   The prefix string typed by the user.

        Returns:
            A list of sentences from `sentences` that begin with `partial`,
            in the order they appeared in the input list.

        Time complexity:
            - Building the Trie: O(N * L) where N = number of sentences
              and L = average sentence length.
            - Searching the Trie: O(P) where P = len(partial).
            - Overall: O(N * L + P)  ≈  O(N * L) for typical inputs.

        Space complexity:
            O(N * L) for storing all characters and sentence references
            in the Trie nodes.
        """
        # ----------------------------------------------------------------
        # Step 1: Reset the Trie root so this method is idempotent and can
        #         be called multiple times on the same Solution instance
        #         without leftover data from previous calls.
        # ----------------------------------------------------------------
        self.root = TrieNode()

        # ----------------------------------------------------------------
        # Step 2: Insert every sentence into the Trie.
        #
        # We iterate in the original order so that `sentences` lists inside
        # each TrieNode naturally maintain insertion order — Python lists
        # preserve append order.
        # ----------------------------------------------------------------
        for sentence in sentences:
            self._insert(sentence)

        # ----------------------------------------------------------------
        # Step 3: Search the Trie for the prefix `partial` and return the
        #         result directly.  The _search_prefix method handles the
        #         "no match" case by returning [].
        # ----------------------------------------------------------------
        result: List[str] = self._search_prefix(partial)
        return result


# ---------------------------------------------------------------------------
# Verification trace
# ---------------------------------------------------------------------------
# Example 1:
#   sentences = ["hello world", "hello there", "help me", "goodbye"]
#   partial   = "hel"
#
#   Trie after insertions (showing only the 'h' -> 'e' -> 'l' path):
#     root -> 'h' -> 'e' -> 'l' (sentences: ["hello world","hello there","help me"])
#                              -> 'l' -> 'o' -> ...
#                              -> 'p' -> ...
#
#   _search_prefix("hel"):
#     root -> children['h'] -> children['e'] -> children['l']
#     node.sentences = ["hello world", "hello there", "help me"]  ✓
#
# Example 2:
#   sentences = ["apple pie", "apple juice", "banana split", "apple"]
#   partial   = "apple"
#
#   After navigating root->'a'->'p'->'p'->'l'->'e':
#     node.sentences = ["apple pie", "apple juice", "apple"]
#     ("banana split" never passes through the 'a' subtree)  ✓
# ---------------------------------------------------------------------------


if __name__ == "__main__":
    solution = Solution()

    # ------------------------------------------------------------------
    # Example 1
    # ------------------------------------------------------------------
    sentences_1: List[str] = ["hello world", "hello there", "help me", "goodbye"]
    partial_1: str = "hel"
    result_1: List[str] = solution.auto_complete(sentences_1, partial_1)
    print("Example 1")
    print(f"  Input sentences : {sentences_1}")
    print(f"  Partial input   : '{partial_1}'")
    print(f"  Output          : {result_1}")
    print(f"  Expected        : ['hello world', 'hello there', 'help me']")
    print()

    # ------------------------------------------------------------------
    # Example 2
    # ------------------------------------------------------------------
    sentences_2: List[str] = ["apple pie", "apple juice", "banana split", "apple"]
    partial_2: str = "apple"
    result_2: List[str] = solution.auto_complete(sentences_2, partial_2)
    print("Example 2")
    print(f"  Input sentences : {sentences_2}")
    print(f"  Partial input   : '{partial_2}'")
    print(f"  Output          : {result_2}")
    print(f"  Expected        : ['apple pie', 'apple juice', 'apple']")
    print()

    # ------------------------------------------------------------------
    # Edge case: no match
    # ------------------------------------------------------------------
    sentences_3: List[str] = ["cat", "car", "card"]
    partial_3: str = "dog"
    result_3: List[str] = solution.auto_complete(sentences_3, partial_3)
    print("Edge case: no match")
    print(f"  Input sentences : {sentences_3}")
    print(f"  Partial input   : '{partial_3}'")
    print(f"  Output          : {result_3}")
    print(f"  Expected        : []")
    print()

    # ------------------------------------------------------------------
    # Edge case: duplicate sentences
    # ------------------------------------------------------------------
    sentences_4: List[str] = ["hi there", "hi there", "hi mom"]
    partial_4: str = "hi"
    result_4: List[str] = solution.auto_complete(sentences_4, partial_4)
    print("Edge case: duplicates")
    print(f"  Input sentences : {sentences_4}")
    print(f"  Partial input   : '{partial_4}'")
    print(f"  Output          : {result_4}")
    print(f"  Expected        : ['hi there', 'hi there', 'hi mom']")
    print()

    # ------------------------------------------------------------------
    # Edge case: partial equals a full sentence exactly
    # ------------------------------------------------------------------
    sentences_5: List[str] = ["go", "going", "gone"]
    partial_5: str = "go"
    result_5: List[str] = solution.auto_complete(sentences_5, partial_5)
    print("Edge case: partial equals a full sentence")
    print(f"  Input sentences : {sentences_5}")
    print(f"  Partial input   : '{partial_5}'")
    print(f"  Output          : {result_5}")
    print(f"  Expected        : ['go', 'going', 'gone']")
    print()

    # ------------------------------------------------------------------
    # Edge case: sentences with spaces
    # ------------------------------------------------------------------
    sentences_6: List[str] = ["good morning", "good night", "good day", "bad day"]
    partial_6: str = "good "
    result_6: List[str] = solution.auto_complete(sentences_6, partial_6)
    print("Edge case: prefix includes a space")
    print(f"  Input sentences : {sentences_6}")
    print(f"  Partial input   : '{partial_6}'")
    print(f"  Output          : {result_6}")
    print(f"  Expected        : ['good morning', 'good night', 'good day']")