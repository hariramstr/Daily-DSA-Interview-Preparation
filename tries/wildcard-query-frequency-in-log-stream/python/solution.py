```python
"""
Title: Wildcard Query Frequency in Log Stream
Difficulty: Hard
Topic: Tries

Problem Description:
You are given a stream of log entries, where each log entry is a lowercase alphabetic string.
You need to build a data structure that supports two operations efficiently:

1. insert(word): Insert a word into the log stream.
2. query(pattern): Given a pattern string consisting of lowercase letters and the wildcard
   character '?', return the total number of previously inserted words that match the pattern.
   A '?' can match exactly one lowercase letter. The pattern must match the entire word.

Additionally, after each query, you must also return the lexicographically smallest word
that matched the pattern, or an empty string if no word matched.

Implement the LogStream class:
- LogStream(): Initializes the object.
- void insert(String word): Inserts the word into the data structure.
- int[] query(String pattern): Returns a pair [count, index] where count is the number of
  matching words and index is the 0-based insertion index of the lexicographically smallest
  matching word, or -1 if no match exists.

Constraints:
- 1 <= word.length, pattern.length <= 20
- All characters in word are lowercase English letters.
- pattern consists of lowercase English letters and '?'.
- At most 5 * 10^4 calls will be made to insert and query combined.
- Words inserted are not necessarily unique.
"""

from typing import List, Dict, Optional, Tuple
import sys
sys.setrecursionlimit(100000)


class TrieNode:
    """
    A node in the Trie data structure.
    
    Each node stores:
    - children: a dictionary mapping characters to child TrieNode objects
    - is_end: whether this node marks the end of an inserted word
    - count: how many words end at this node (handles duplicate insertions)
    - insertion_indices: list of insertion indices for words ending here
    - min_word: the lexicographically smallest word that passes through this node
               (used for optimization, though we'll compute during query)
    """
    def __init__(self):
        # Dictionary from character -> TrieNode child
        self.children: Dict[str, 'TrieNode'] = {}
        # True if a word ends at this node
        self.is_end: bool = False
        # Count of words ending at this node (for duplicates)
        self.count: int = 0
        # List of (word, insertion_index) for words ending at this node
        self.words_here: List[Tuple[str, int]] = []


class LogStream:
    """
    LogStream data structure supporting insert and wildcard query operations.
    
    Uses a Trie for efficient prefix-based traversal combined with DFS for
    wildcard pattern matching.
    """
    
    def __init__(self):
        """
        Initialize the LogStream with an empty Trie and insertion counter.
        
        Time complexity: O(1)
        Space complexity: O(1)
        """
        # Root of the Trie - doesn't represent any character itself
        self.root = TrieNode()
        # Global insertion counter - increments with each insert call
        self.insertion_counter: int = 0
        # We also keep a flat list of all inserted words with their indices
        # This allows us to do brute-force matching as a fallback or verification
        # For the Trie approach, we store words at end nodes
        
    def insert(self, word: str) -> None:
        """
        Insert a word into the Trie data structure.
        
        Traverses the Trie character by character, creating new nodes as needed.
        At the terminal node, records the word and its insertion index.
        
        Args:
            word: The word to insert (lowercase alphabetic string)
            
        Returns:
            None
            
        Time complexity: O(L) where L is the length of the word
        Space complexity: O(L) in the worst case for new nodes
        """
        # Start at the root of the Trie
        current_node = self.root
        
        # Traverse each character in the word
        for char in word:
            # If this character doesn't have a child node yet, create one
            if char not in current_node.children:
                current_node.children[char] = TrieNode()
            # Move to the child node for this character
            current_node = current_node.children[char]
        
        # We've reached the end of the word
        # Mark this node as an end-of-word node
        current_node.is_end = True
        # Increment the count (handles duplicate words)
        current_node.count += 1
        # Record this word along with its insertion index
        # insertion_counter is the 0-based index of this insertion
        current_node.words_here.append((word, self.insertion_counter))
        
        # Increment the global insertion counter for the next insert
        self.insertion_counter += 1
    
    def query(self, pattern: str) -> List[int]:
        """
        Query the Trie for all words matching the given wildcard pattern.
        
        Uses DFS (Depth-First Search) through the Trie to find all words
        that match the pattern. '?' matches exactly one character.
        
        Args:
            pattern: A string of lowercase letters and '?' wildcards
            
        Returns:
            [count, index] where count is the number of matching words and
            index is the 0-based insertion index of the lexicographically
            smallest matching word, or -1 if no match exists.
            
        Time complexity: O(26^W * L) in worst case where W is number of wildcards
                        and L is pattern length. In practice much faster.
        Space complexity: O(L) for the recursion stack
        """
        # We'll collect all matching (word, insertion_index) pairs
        # using DFS through the Trie
        matching_results: List[Tuple[str, int]] = []
        
        # Start DFS from the root, matching against the full pattern
        self._dfs(self.root, pattern, 0, matching_results)
        
        # If no matches found, return [0, -1]
        if not matching_results:
            return [0, -1]
        
        # Count total matches (sum of all occurrences)
        total_count = len(matching_results)
        
        # Find the lexicographically smallest word among matches
        # and return its insertion index
        # Sort by word first (lexicographic), then by insertion index for ties
        # We want the lex smallest word; if same word inserted multiple times,
        # return the earliest insertion index of that word
        
        # Find the minimum word lexicographically
        min_word = min(matching_results, key=lambda x: (x[0], x[1]))
        min_index = min_word[1]
        
        return [total_count, min_index]
    
    def _dfs(
        self,
        node: TrieNode,
        pattern: str,
        pos: int,
        results: List[Tuple[str, int]]
    ) -> None:
        """
        Depth-First Search through the Trie to find all words matching the pattern.
        
        At each position in the pattern:
        - If it's a regular character, follow only that character's child
        - If it's '?', follow ALL children (wildcard matches any single char)
        - If we've consumed the entire pattern and we're at an end node, collect results
        
        Args:
            node: Current Trie node being visited
            pattern: The full pattern string
            pos: Current position in the pattern we're matching
            results: List to collect matching (word, index) tuples
            
        Returns:
            None (modifies results in place)
            
        Time complexity: O(26^W) where W is number of wildcards in pattern
        Space complexity: O(L) for recursion depth
        """
        # Base case: we've matched all characters in the pattern
        if pos == len(pattern):
            # Check if this node marks the end of a valid word
            if node.is_end:
                # Collect all (word, insertion_index) pairs stored at this node
                for word_entry in node.words_here:
                    results.append(word_entry)
            # Whether or not it's an end node, we stop here
            return
        
        # Get the current character in the pattern
        current_char = pattern[pos]
        
        if current_char == '?':
            # Wildcard: must match exactly ONE character
            # So we explore ALL children of the current node
            for child_char, child_node in node.children.items():
                # Recurse into each child, advancing position by 1
                self._dfs(child_node, pattern, pos + 1, results)
        else:
            # Regular character: only follow the matching child
            if current_char in node.children:
                # The child exists, recurse into it
                self._dfs(node.children[current_char], pattern, pos + 1, results)
            # If the character doesn't exist as a child, this path fails silently


# ─────────────────────────────────────────────────────────────────────────────
# Solution class wrapper (as required by the problem statement)
# ─────────────────────────────────────────────────────────────────────────────

class Solution:
    """
    Wrapper class that demonstrates the LogStream usage.
    
    In competitive programming / LeetCode style, the LogStream class itself
    IS the solution. This Solution class provides a simulate() method to
    run the command-based interface shown in the examples.
    """
    
    def simulate(
        self,
        commands: List[str],
        arguments: List[List]
    ) -> List[Optional[List[int]]]:
        """
        Simulate the sequence of LogStream operations.
        
        Args:
            commands: List of operation names like ["LogStream","insert","query",...]
            arguments: List of argument lists corresponding to each command
            
        Returns:
            List of results: None for constructor/insert, [count, index] for query
            
        Time complexity: O(N * 26^W * L) where N is number of operations,
                        W is max wildcards, L is max word length
        Space complexity: O(N * L) for storing all words in the Trie
        """
        # Output list to collect results
        output: List[Optional[List[int]]] = []
        
        # We'll create the LogStream instance when we see the constructor command
        log_stream: Optional[LogStream] = None
        
        # Process each command one by one
        for i, command in enumerate(commands):
            args = arguments[i]
            
            if command == "LogStream":
                # Constructor: create a new LogStream instance
                log_stream = LogStream()
                # Constructor returns null in the output
                output.append(None)
                
            elif command == "insert":
                # Insert operation: insert the word into the log stream
                # args[0] is the word to insert
                word = args[0]
                log_stream.insert(word)
                # Insert returns null
                output.append(None)
                
            elif command == "query":
                # Query operation: find matching words for the pattern
                # args[0] is the pattern
                pattern = args[0]
                result = log_stream.query(pattern)
                output.append(result)
        
        return output


# ─────────────────────────────────────────────────────────────────────────────
# Main block: trace through examples to verify correctness
# ─────────────────────────────────────────────────────────────────────────────

if __name__ == "__main__":
    """
    Verify the solution against the provided examples.
    
    Example 1:
    Commands: ["LogStream","insert","insert","insert","query","query"]
    Args:     [[],["apple"],["apply"],["apt"],["ap?le"],["a??"]]
    Expected: [null, null, null, null, [2, 0], [1, 2]]
    
    Trace:
    - insert("apple") -> index 0
    - insert("apply") -> index 1
    - insert("apt")   -> index 2
    - query("ap?le"):
        Pattern "ap?le" has length 5.
        "apple" (len 5): a=a, p=p, ?=p✓, l=l, e=e → MATCH (index 0)
        "apply" (len 5): a=a, p=p, ?=p✓, l=l, y≠e → NO MATCH
        Wait, let me re-check: "ap?le" vs "apply"
          a=a✓, p=p✓, ?=p✓, l=l✓, e≠y → NO MATCH
        "apt" (len 3): length mismatch → NO MATCH
        So only "apple" matches → count=1?
        
        Wait, the expected output is [2, 0]. Let me re-read.
        "ap?le" matches "apple" AND "apply"?
        "ap?le" vs "apple": a=a, p=p, ?=p, l=l, e=e → YES
        "ap?le" vs "apply": a=a, p=p, ?=p, l=l, e≠y → NO
        
        Hmm, but expected says count=2. Let me re-read the problem...
        Oh wait: "ap?le" - the ? is at position 2 (0-indexed).
        "apple": a,p,p,l,e - position 2 is 'p', ? matches 'p' ✓
        "apply": a,p,p,l,y - position 2 is 'p', ? matches 'p', but position 4: e≠y ✗
        
        So "apply" should NOT match "ap?le". But expected says [2, 0]...
        
        Wait, maybe I'm misreading. Let me check again:
        "ap?le" has 5 chars: a, p, ?, l, e
        "apple" has 5 chars: a, p, p, l, e → matches (? matches p) ✓
        "apply" has 5 chars: a, p, p, l, y → position 4: e≠y ✗
        
        Hmm, the expected output says [2, 0] for "ap?le". That seems wrong based on
        my analysis. Unless "apply" is a,p,p,l,y and the pattern is a,p,?,l,e...
        
        Actually wait - maybe the example has a typo or I need to re-read.
        Let me re-read: "ap?le" matches "apple" (index 0) and "apply" (index 1).
        
        Oh! Maybe the problem statement example has an error, OR maybe I need to
        look at this differently. Let me check if "ap?le" could match "apply":
        a=a, p=p, ?=p, l=l, e=y? No, e≠y.
        
        I'll trust my implementation which correctly does character-by-character matching.
        The example in the problem might have a typo. Let me just run both examples
        and show what my solution produces.
    """
    
    solution = Solution()
    
    print("=" * 60)
    print("Example 1:")
    print("=" * 60)
    
    # Example 1
    commands1 = ["LogStream", "insert", "insert", "insert", "query", "query"]
    arguments1 = [[], ["apple"], ["apply"], ["apt"], ["ap?le"], ["a??"]]
    expected1 = [None, None, None, None, [2, 0], [1, 2]]
    
    result1 = solution.simulate(commands1, arguments1)
    
    print(f"Commands:  {commands1}")
    print(f"Arguments: {arguments1}")
    print(f"Output:    {result1}")
    print(f"Expected:  {expected1}")
    
    # Manual verification for Example 1
    print("\nManual trace for Example 1:")
    ls1 = LogStream()
    ls1.insert("apple")  # index 0
    print("  Inserted 'apple' at index 0")
    ls1.insert("apply")  # index 1
    print("  Inserted 'apply' at index 1")
    ls1.insert("apt")    # index 2
    print("  Inserted 'apt' at index 2")
    
    #