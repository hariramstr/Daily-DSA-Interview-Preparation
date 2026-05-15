```python
"""
Title: Contact List Prefix Search with Wildcards
Difficulty: Medium
Topic: Tries

Problem Description:
You are building a smart contact search feature for a messaging app. Users can search
their contact list using a query string that may contain lowercase letters and the
wildcard character `?`, where `?` can match exactly one lowercase letter.

Given a list of contact names and a list of search queries, return for each query
the number of contacts whose names start with the given query pattern (prefix match
with wildcard support).

A contact name matches a query if:
1. The query is a prefix of the contact name (or equal in length to the name), AND
2. Every non-wildcard character in the query matches the corresponding character in
   the contact name exactly, AND
3. Every `?` in the query matches exactly one character in the contact name.

Constraints:
- 1 <= contacts.length <= 10^4
- 1 <= contacts[i].length <= 20
- 1 <= queries.length <= 10^4
- 1 <= queries[i].length <= 20
- All contact names and query characters are lowercase English letters or `?` (queries only)
- Contact names contain only lowercase English letters
"""

from typing import List, Dict, Optional


# ---------------------------------------------------------------------------
# Trie Node Definition
# ---------------------------------------------------------------------------

class TrieNode:
    """
    Represents a single node in the Trie.

    Each node stores:
      - children: a dictionary mapping a character (a-z) to the next TrieNode
      - count: how many contact names pass through (or end at) this node.
               This lets us answer "how many contacts share this prefix?" in O(1)
               once we've navigated to the right node.
    """

    def __init__(self) -> None:
        # children maps each possible next character to its TrieNode
        self.children: Dict[str, "TrieNode"] = {}
        # count tracks how many contact names were inserted through this node
        self.count: int = 0


# ---------------------------------------------------------------------------
# Trie Definition
# ---------------------------------------------------------------------------

class Trie:
    """
    A Trie (prefix tree) that stores contact names and supports
    wildcard-aware prefix queries.
    """

    def __init__(self) -> None:
        # The root node represents the empty string (before any character)
        self.root = TrieNode()

    def insert(self, word: str) -> None:
        """
        Insert a contact name into the Trie.

        As we walk down the Trie one character at a time, we increment the
        `count` of every node we visit. This means each node's count equals
        the number of words that share the prefix leading to that node.

        Args:
            word: A contact name consisting of lowercase English letters.

        Time complexity:  O(L) where L = len(word)
        Space complexity: O(L) in the worst case (new nodes created)
        """
        # Start at the root
        node = self.root

        for ch in word:
            # If this character hasn't been seen at this level, create a new node
            if ch not in node.children:
                node.children[ch] = TrieNode()

            # Move down to the child node for this character
            node = node.children[ch]

            # Increment count: this node is on the path of one more contact name
            node.count += 1

    def search_with_wildcards(self, query: str) -> int:
        """
        Count how many inserted contact names start with the given query pattern.

        The query may contain '?' which matches exactly one lowercase letter.
        We use a recursive DFS (depth-first search) through the Trie.

        Key insight:
          - For a normal character, we follow exactly one child edge.
          - For '?', we must follow ALL 26 possible child edges and sum the results.
          - When we exhaust all characters in the query, the answer is the `count`
            of the current node (all contacts that pass through here share this prefix).

        Args:
            query: A search pattern with lowercase letters and/or '?' wildcards.

        Returns:
            The number of contacts whose names start with the query pattern.

        Time complexity:  O(Q * 26^W) where Q = len(query), W = number of '?' in query.
                          In the worst case (all wildcards, depth 20) this is 26^20,
                          but in practice the Trie prunes branches that don't exist,
                          so real performance is much better.
        Space complexity: O(Q) for the recursion stack depth.
        """

        def dfs(node: TrieNode, depth: int) -> int:
            """
            Recursively count matching contacts starting from `node` at position
            `depth` in the query string.

            Args:
                node:  Current Trie node we are visiting.
                depth: Current index into the query string.

            Returns:
                Number of contacts reachable from this node that match the
                remaining query[depth:].
            """
            # BASE CASE: We've matched all characters in the query.
            # Every contact name that passes through `node` is a valid match
            # because they all share the prefix we've matched so far.
            # `node.count` was set during insertion to exactly this number.
            if depth == len(query):
                return node.count

            ch = query[depth]  # The current query character

            if ch == '?':
                # WILDCARD: '?' can match any single lowercase letter.
                # We must explore every child of the current node and sum results.
                total = 0
                for child_node in node.children.values():
                    # Recurse deeper with the next query character
                    total += dfs(child_node, depth + 1)
                return total
            else:
                # EXACT CHARACTER: Only follow the edge for this specific character.
                if ch not in node.children:
                    # No contact has this character at this position → 0 matches
                    return 0
                # Follow the single matching edge and continue
                return dfs(node.children[ch], depth + 1)

        # Start the DFS from the root at query position 0
        return dfs(self.root, 0)


# ---------------------------------------------------------------------------
# Solution Class
# ---------------------------------------------------------------------------

class Solution:
    """
    Solves the Contact List Prefix Search with Wildcards problem using a Trie.
    """

    def search_contacts(
        self,
        contacts: List[str],
        queries: List[str]
    ) -> List[int]:
        """
        For each query, count how many contacts match the prefix pattern.

        Algorithm overview:
          1. Build a Trie by inserting all contact names.
             - Each node stores a `count` = number of contacts passing through it.
          2. For each query, perform a DFS on the Trie:
             - Exact characters navigate a single edge.
             - '?' navigates all 26 possible edges and sums results.
          3. When the query is fully consumed, return the node's `count`.

        Why a Trie?
          - Inserting N contacts of average length L costs O(N*L).
          - Querying with wildcards is efficient because the Trie prunes
            non-existent branches automatically.
          - The `count` field at each node gives O(1) answers once we reach
            the end of a query pattern.

        Args:
            contacts: List of contact names (lowercase letters only).
            queries:  List of search patterns (lowercase letters + '?').

        Returns:
            A list of integers where result[i] is the number of contacts
            matching queries[i].

        Time complexity:
            Build:  O(N * L_c)  where N = len(contacts), L_c = avg contact length
            Query:  O(Q * 26^W) where Q = len(queries), W = wildcards per query
            Overall: O(N*L_c + Q*26^W)

        Space complexity:
            O(N * L_c) for the Trie nodes (at most N*L_c nodes total)
        """

        # ----------------------------------------------------------------
        # STEP 1: Build the Trie from all contact names
        # ----------------------------------------------------------------
        trie = Trie()

        for contact in contacts:
            # Insert each contact name; this populates the `count` fields
            # at every node along the path for that name.
            trie.insert(contact)

        # ----------------------------------------------------------------
        # STEP 2: Answer each query using the Trie
        # ----------------------------------------------------------------
        results: List[int] = []

        for query in queries:
            # Use the wildcard-aware DFS search on the Trie
            match_count = trie.search_with_wildcards(query)
            results.append(match_count)

        return results


# ---------------------------------------------------------------------------
# Main: Trace through examples to verify correctness
# ---------------------------------------------------------------------------

if __name__ == "__main__":
    solution = Solution()

    # ------------------------------------------------------------------
    # Example 1 (from problem description, corrected explanation)
    # contacts = ["alice", "alfred", "bob", "alicia", "alba"]
    # queries  = ["al", "al?", "b?b"]
    #
    # Trie structure (relevant paths):
    #   root → a(4) → l(4) → i(3) → c(1) → e(1)  ← "alice"
    #                                       a(1) → (1)  ← "alicia"
    #                          f(1) → r(1) → e(1) → d(1)  ← "alfred"
    #                          b(1) → a(1)  ← "alba"
    #        → b(1) → o(1) → b(1)  ← "bob"
    #
    # Query "al":
    #   Navigate root→a→l, depth==2==len("al"), return node_l.count = 4
    #   ✓ Expected: 4
    #
    # Query "al?":
    #   Navigate root→a→l, then '?' at depth 2:
    #     child 'i' (count=3): depth==3==len("al?"), return 3  [alice, alicia + wait]
    #       Actually node after 'i' has count=2 (alice, alicia)
    #     child 'f' (count=1): return 1  [alfred]
    #     child 'b' (count=1): return 1  [alba]
    #   Total = 2 + 1 + 1 = 4
    #   ✓ Expected: 4
    #
    # Query "b?b":
    #   Navigate root→b, then '?' at depth 1:
    #     child 'o': navigate to 'b' at depth 2→3==len("b?b"), return node.count=1
    #   Total = 1
    #   ✓ Expected: 1
    # ------------------------------------------------------------------

    print("=" * 60)
    print("Example 1")
    print("=" * 60)
    contacts1 = ["alice", "alfred", "bob", "alicia", "alba"]
    queries1 = ["al", "al?", "b?b"]
    result1 = solution.search_contacts(contacts1, queries1)
    print(f"Contacts : {contacts1}")
    print(f"Queries  : {queries1}")
    print(f"Output   : {result1}")
    print(f"Expected : [4, 4, 1]")
    assert result1 == [4, 4, 1], f"FAILED: got {result1}"
    print("PASSED ✓")

    # ------------------------------------------------------------------
    # Example 2 (from problem description)
    # contacts = ["sam", "samuel", "sandy", "sandra"]
    # queries  = ["sa?", "san??", "s?m"]
    #
    # Trie structure:
    #   root → s(4) → a(4) → m(2) → u(1) → e(1) → l(1)  ← "samuel"
    #                                               ← "sam" (ends here, count=2 at 'm')
    #                          n(2) → d(2) → y(1)  ← "sandy"
    #                                        r(1) → a(1)  ← "sandra"
    #
    # Query "sa?":
    #   Navigate root→s→a, then '?' at depth 2:
    #     child 'm' (count=2): depth==3==len("sa?"), return 2  [sam, samuel]
    #     child 'n' (count=2): depth==3==len("sa?"), return 2  [sandy, sandra]
    #   Total = 2 + 2 = 4
    #   ✓ Expected: 4
    #
    # Query "san??":
    #   Navigate root→s→a→n, then '?' at depth 3:
    #     child 'd' (count=2): then '?' at depth 4:
    #       child 'y' (count=1): depth==5==len("san??"), return 1  [sandy]
    #       child 'r' (count=1): depth==5==len("san??"), return 1  [sandra]
    #       subtotal = 2
    #   Total = 2
    #   ✓ Expected: 2
    #
    # Query "s?m":
    #   Navigate root→s, then '?' at depth 1:
    #     child 'a' (count=4): navigate to 'm' at depth 2→3==len("s?m"):
    #       node_m.count = 2  [sam, samuel]
    #   Total = 2
    #   ✓ Expected: 2
    # ------------------------------------------------------------------

    print()
    print("=" * 60)
    print("Example 2")
    print("=" * 60)
    contacts2 = ["sam", "samuel", "sandy", "sandra"]
    queries2 = ["sa?", "san??", "s?m"]
    result2 = solution.search_contacts(contacts2, queries2)
    print(f"Contacts : {contacts2}")
    print(f"Queries  : {queries2}")
    print(f"Output   : {result2}")
    print(f"Expected : [4, 2, 2]")
    assert result2 == [4, 2, 2], f"FAILED: got {result2}"
    print("PASSED ✓")

    # ------------------------------------------------------------------
    # Additional edge case: query longer than all contacts → 0
    # ------------------------------------------------------------------
    print()
    print("=" * 60)
    print("Edge Case: Query longer than contacts")
    print("=" * 60)
    contacts3 = ["hi", "hey"]
    queries3 = ["hello"]
    result3 = solution.search_contacts(contacts3, queries3)
    print(f"Contacts : {contacts3}")
    print(f"Queries  : {queries3}")
    print(f"Output   : {result3}")
    print(f"Expected : [0]")
    assert result3 == [0], f"FAILED: got {result3}"
    print("PASSED ✓")

    # ------------------------------------------------------------------
    # Additional edge case: single character wildcard
    # ------------------------------------------------------------------
    print()
    print("=" * 60)
    print("Edge Case: Single wildcard matches all")
    print("=" * 60)
    contacts4 = ["a", "b", "c", "ab"]
    queries4 = ["?"]
    result4 = solution.search_contacts(contacts4, queries4)
    print(f"Contacts : {contacts4}")
    print(f"Queries  : {queries4}")
    print(f"Output   : {result4}")
    print(f"Expected : [4]")
    assert result4 == [4], f"FAILED: got {result4}"
    print("PASSED ✓")

    print()
    print("All tests passed! ✓")
```