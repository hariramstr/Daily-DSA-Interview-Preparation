"""
Shortest Unique Prefix for Each Word
=====================================

Problem Description:
Given a list of distinct lowercase words, find the shortest prefix for each word
such that the prefix uniquely identifies that word among all words in the list.
In other words, no other word in the list starts with the same prefix.

Return an array of strings where the i-th element is the shortest unique prefix
for the i-th word in the input list.

Constraints:
- 1 <= words.length <= 1000
- 1 <= words[i].length <= 100
- All words consist of lowercase English letters only.
- All words in the list are distinct.
- It is guaranteed that no word is a prefix of another word in the list.

Example 1:
    Input: words = ["dog", "cat", "car", "card", "done"]
    Output: ["dog", "cat", "car", "card", "do"]

Example 2:
    Input: words = ["apple", "banana", "cherry"]
    Output: ["a", "b", "c"]
"""

from typing import List, Dict, Optional


class TrieNode:
    """
    Represents a single node in the Trie data structure.
    
    Each node stores:
    - children: a dictionary mapping characters to child TrieNodes
    - count: how many words pass through this node (used to detect uniqueness)
    """
    
    def __init__(self):
        # Dictionary to hold child nodes, keyed by character
        # e.g., children['a'] points to the TrieNode for 'a'
        self.children: Dict[str, 'TrieNode'] = {}
        
        # Count of how many words pass through this node
        # When count == 1, this node is on the path of exactly one word
        # meaning the prefix up to this node is unique!
        self.count: int = 0


class Solution:
    """
    Solution class implementing the Shortest Unique Prefix algorithm using a Trie.
    
    Why use a Trie?
    - A Trie (prefix tree) naturally organizes words by their shared prefixes
    - By counting how many words pass through each node, we can efficiently
      determine where a prefix becomes unique (count drops to 1)
    - This avoids O(n^2) brute-force comparisons between all word pairs
    """
    
    def __init__(self):
        # The root of our Trie — it doesn't represent any character itself
        self.root: TrieNode = TrieNode()
    
    def _insert(self, word: str) -> None:
        """
        Insert a word into the Trie, incrementing the count at each node.
        
        Args:
            word: The word to insert into the Trie.
        
        Returns:
            None
        
        Time complexity: O(L) where L is the length of the word
        Space complexity: O(L) in the worst case (new nodes created)
        """
        # Start at the root of the Trie
        current_node: TrieNode = self.root
        
        # Traverse each character in the word
        for char in word:
            # If this character doesn't have a child node yet, create one
            if char not in current_node.children:
                current_node.children[char] = TrieNode()
            
            # Move to the child node for this character
            current_node = current_node.children[char]
            
            # Increment the count: this node is now on the path of one more word
            # This count is the KEY to finding unique prefixes later!
            current_node.count += 1
    
    def _find_shortest_prefix(self, word: str) -> str:
        """
        Find the shortest unique prefix for a given word by traversing the Trie.
        
        The prefix is unique when we reach a node with count == 1,
        meaning only this word passes through that node.
        
        Args:
            word: The word for which to find the shortest unique prefix.
        
        Returns:
            The shortest prefix string that uniquely identifies this word.
        
        Time complexity: O(L) where L is the length of the word
        Space complexity: O(L) for building the prefix string
        """
        # Start at the root of the Trie
        current_node: TrieNode = self.root
        
        # Build the prefix character by character
        prefix: str = ""
        
        # Traverse each character in the word
        for char in word:
            # Move to the child node for this character
            current_node = current_node.children[char]
            
            # Add this character to our growing prefix
            prefix += char
            
            # KEY INSIGHT: If count == 1, only ONE word passes through this node
            # That means our current prefix is already unique — we can stop here!
            if current_node.count == 1:
                return prefix
        
        # If we reach here, the entire word is needed as the prefix
        # (This shouldn't happen given the constraint that no word is a prefix
        # of another, but it's good defensive programming)
        return prefix
    
    def shortestUniquePrefixes(self, words: List[str]) -> List[str]:
        """
        Find the shortest unique prefix for each word in the input list.
        
        Algorithm Overview:
        1. Build a Trie from all words, tracking how many words pass through each node
        2. For each word, traverse the Trie to find the first node with count == 1
           (that's where the prefix becomes unique)
        
        Args:
            words: A list of distinct lowercase words.
        
        Returns:
            A list of strings where the i-th element is the shortest unique prefix
            for the i-th word in the input list.
        
        Time complexity: O(N * L) where N is the number of words and L is the
                         average word length. We insert each word once and query each once.
        Space complexity: O(N * L) for the Trie in the worst case (no shared prefixes)
        
        Example trace for ["dog", "cat", "car", "card", "done"]:
        
        After inserting all words, the Trie looks like:
        root
        ├── d (count=2: "dog", "done")
        │   ├── o (count=2: "dog", "done")
        │   │   ├── g (count=1: "dog") ← unique!
        │   │   └── n (count=1: "done") ← unique!
        └── c (count=3: "cat", "car", "card")
            └── a (count=3: "cat", "car", "card")
                ├── t (count=1: "cat") ← unique!
                └── r (count=2: "car", "card")
                    └── d (count=1: "card") ← unique!
        
        For "dog": d(2) → o(2) → g(1) ← stop! prefix = "dog"
        For "cat": c(3) → a(3) → t(1) ← stop! prefix = "cat"
        For "car": c(3) → a(3) → r(2) → ... wait, r has count=2
                   We need to go further: but "car" ends here.
                   Actually "car" itself is unique at 'r' node... 
                   Let me re-check: "car" and "card" both pass through c-a-r,
                   so r has count=2. We continue: next char of "car" would be end.
                   Since the problem guarantees no word is prefix of another,
                   "car" must be the full prefix needed → "car"
                   
                   Wait — but "car" IS a prefix of "card"! The problem says
                   "It is guaranteed that no word is a prefix of another word."
                   But the example has "car" and "card"... Let me re-read.
                   
                   Hmm, the example contradicts the constraint. The example output
                   shows "car" needs prefix "car" and "card" needs "card".
                   We'll handle this correctly: the loop ends when we exhaust
                   the word's characters, returning the full word as prefix.
        """
        # ----------------------------------------------------------------
        # STEP 1: Reset the Trie (in case this method is called multiple times)
        # ----------------------------------------------------------------
        self.root = TrieNode()
        
        # ----------------------------------------------------------------
        # STEP 2: Insert all words into the Trie
        # ----------------------------------------------------------------
        # By inserting ALL words first, each node's count reflects how many
        # words share that prefix. This is crucial for finding unique prefixes.
        for word in words:
            self._insert(word)
        
        # ----------------------------------------------------------------
        # STEP 3: For each word, find its shortest unique prefix
        # ----------------------------------------------------------------
        # We query the Trie for each word to find where its path becomes unique
        result: List[str] = []
        
        for word in words:
            # Find and store the shortest unique prefix for this word
            unique_prefix: str = self._find_shortest_prefix(word)
            result.append(unique_prefix)
        
        return result


def main() -> None:
    """
    Main function to demonstrate the Shortest Unique Prefix solution.
    Tests with the provided examples and additional edge cases.
    """
    solution = Solution()
    
    # ----------------------------------------------------------------
    # Example 1: Mixed words with shared prefixes
    # ----------------------------------------------------------------
    print("=" * 60)
    print("Example 1:")
    words1: List[str] = ["dog", "cat", "car", "card", "done"]
    result1: List[str] = solution.shortestUniquePrefixes(words1)
    print(f"  Input:    {words1}")
    print(f"  Output:   {result1}")
    print(f"  Expected: ['dog', 'cat', 'car', 'card', 'do']")
    
    # Verify correctness
    expected1 = ["dog", "cat", "car", "card", "do"]
    print(f"  Correct:  {result1 == expected1}")
    
    # ----------------------------------------------------------------
    # Example 2: All words start with different letters
    # ----------------------------------------------------------------
    print("\nExample 2:")
    words2: List[str] = ["apple", "banana", "cherry"]
    result2: List[str] = solution.shortestUniquePrefixes(words2)
    print(f"  Input:    {words2}")
    print(f"  Output:   {result2}")
    print(f"  Expected: ['a', 'b', 'c']")
    
    # Verify correctness
    expected2 = ["a", "b", "c"]
    print(f"  Correct:  {result2 == expected2}")
    
    # ----------------------------------------------------------------
    # Example 3: Single word — the word itself is the prefix
    # ----------------------------------------------------------------
    print("\nExample 3 (Single word):")
    words3: List[str] = ["hello"]
    result3: List[str] = solution.shortestUniquePrefixes(words3)
    print(f"  Input:    {words3}")
    print(f"  Output:   {result3}")
    print(f"  Expected: ['h']")
    
    expected3 = ["h"]
    print(f"  Correct:  {result3 == expected3}")
    
    # ----------------------------------------------------------------
    # Example 4: Words with longer shared prefixes
    # ----------------------------------------------------------------
    print("\nExample 4 (Longer shared prefixes):")
    words4: List[str] = ["interview", "interact", "interface", "zoom"]
    result4: List[str] = solution.shortestUniquePrefixes(words4)
    print(f"  Input:    {words4}")
    print(f"  Output:   {result4}")
    # "interview" → "interv" (unique after 'v')
    # "interact"  → "intera" (unique after 'a')
    # "interface" → "interf" (unique after 'f')
    # "zoom"      → "z"      (unique at 'z')
    print(f"  Expected: ['interv', 'intera', 'interf', 'z']")
    
    expected4 = ["interv", "intera", "interf", "z"]
    print(f"  Correct:  {result4 == expected4}")
    
    # ----------------------------------------------------------------
    # Example 5: Two words differing only at the last character
    # ----------------------------------------------------------------
    print("\nExample 5 (Words differing at last char):")
    words5: List[str] = ["abc", "abd"]
    result5: List[str] = solution.shortestUniquePrefixes(words5)
    print(f"  Input:    {words5}")
    print(f"  Output:   {result5}")
    print(f"  Expected: ['abc', 'abd']")
    
    expected5 = ["abc", "abd"]
    print(f"  Correct:  {result5 == expected5}")
    
    print("\n" + "=" * 60)
    print("All tests completed!")


if __name__ == "__main__":
    main()