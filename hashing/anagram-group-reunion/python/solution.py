```python
"""
Title: Anagram Group Reunion
Difficulty: Medium
Topic: Hashing

Problem Description:
You are given a list of n strings called words and a separate list of q query strings
called queries. For each query string, you must find all strings in words that are
anagrams of the query string and return them in the order they appear in words.
Two strings are considered anagrams if they contain the same characters with the same
frequencies, regardless of order.

However, there is an additional twist: after answering each query, the matched words
are removed from words permanently, so they cannot be matched again by future queries.

Return a list of lists where the i-th list contains all words from words (at the time
of the i-th query, after previous removals) that are anagrams of queries[i], in their
original relative order.

Constraints:
- 1 <= n <= 10^4
- 1 <= q <= 10^3
- 1 <= words[i].length, queries[i].length <= 20
- All strings consist of lowercase English letters only.

Example 1:
- Input: words = ["eat", "tea", "tan", "ate", "nat", "bat"], queries = ["ate", "tan"]
- Output: [["eat", "tea", "ate"], ["tan", "nat"]]

Example 2:
- Input: words = ["abc", "bca", "cab", "xyz", "zyx"], queries = ["abc", "xyz", "abc"]
- Output: [["abc", "bca", "cab"], ["xyz", "zyx"], []]
"""

from typing import List, Dict, Tuple


class Solution:
    """
    Solution class for the Anagram Group Reunion problem.
    
    Core Idea:
    -----------
    Two strings are anagrams if and only if their sorted versions are identical.
    For example: "eat", "tea", "ate" all sort to "aet".
    
    We use a dictionary that maps each "sorted signature" to a list of (index, word)
    pairs. For each query, we compute its sorted signature, look up all matching words,
    collect them in original order, then remove them from the dictionary so future
    queries cannot find them again.
    """

    def anagram_group_reunion(
        self, words: List[str], queries: List[str]
    ) -> List[List[str]]:
        """
        Find anagrams of each query in the remaining words list, removing matches
        after each query.

        Args:
            words (List[str]): The initial list of words to search through.
            queries (List[str]): The list of query strings to find anagrams for.

        Returns:
            List[List[str]]: A list of lists where the i-th list contains all words
                             that are anagrams of queries[i] (after prior removals),
                             in their original relative order.

        Time Complexity:
            O(n * L * log(L) + q * L * log(L))
            where n = number of words, q = number of queries, L = max string length.
            - Building the dictionary: O(n * L * log(L)) for sorting each word.
            - Processing each query: O(L * log(L)) to sort the query, then O(k) to
              collect k matching words. Total across all queries: O(q * L * log(L)).

        Space Complexity:
            O(n * L) for storing all words in the dictionary.
        """

        # -----------------------------------------------------------------------
        # STEP 1: Build an "anagram signature" dictionary.
        #
        # The key insight: two strings are anagrams iff sorting their characters
        # gives the same result. We call this sorted result the "signature".
        #
        # We build a dictionary:
        #   anagram_map: Dict[str, List[Tuple[int, str]]]
        #   key   -> sorted signature (e.g., "aet")
        #   value -> list of (original_index, word) tuples
        #
        # We store the original index so we can later reconstruct the words in
        # their original relative order (though since we iterate left-to-right
        # when building, the list is already in order).
        # -----------------------------------------------------------------------
        anagram_map: Dict[str, List[Tuple[int, str]]] = {}

        for idx, word in enumerate(words):
            # Sort the characters of the word to get its unique anagram signature.
            # "eat" -> sorted -> ['a', 'e', 't'] -> joined -> "aet"
            signature: str = "".join(sorted(word))

            # If this signature hasn't been seen before, initialize an empty list.
            if signature not in anagram_map:
                anagram_map[signature] = []

            # Append a tuple of (original_index, word) to preserve ordering info.
            # We store the index as well, though since we build left-to-right,
            # the list is already in original order. The index is kept for clarity.
            anagram_map[signature].append((idx, word))

        # -----------------------------------------------------------------------
        # STEP 2: Process each query one by one.
        #
        # For each query:
        #   a) Compute the query's anagram signature (same sorting trick).
        #   b) Look up the signature in anagram_map.
        #   c) If found, collect all matching words in order.
        #   d) Remove the entry from anagram_map so future queries can't reuse them.
        #   e) If not found (or already removed), return an empty list for this query.
        # -----------------------------------------------------------------------
        results: List[List[str]] = []

        for query in queries:
            # Compute the anagram signature for the current query string.
            query_signature: str = "".join(sorted(query))

            # Check if there are any remaining words with this signature.
            if query_signature in anagram_map:
                # Retrieve all (index, word) pairs that match this signature.
                matched_pairs: List[Tuple[int, str]] = anagram_map[query_signature]

                # Extract just the word strings, in the order they appear
                # (which is already the original relative order since we built
                # the list by iterating left-to-right over words).
                matched_words: List[str] = [word for (_, word) in matched_pairs]

                # Append the matched words to our results for this query.
                results.append(matched_words)

                # CRITICAL: Remove this signature from the dictionary permanently.
                # This ensures that future queries cannot match these same words again.
                # This is the "removal" mechanic described in the problem.
                del anagram_map[query_signature]

            else:
                # No matching words found (either never existed or already removed).
                # Append an empty list to maintain the correct index in results.
                results.append([])

        # Return the final list of lists, one per query.
        return results


# -------------------------------------------------------------------------------
# Main block: Demonstrate the solution with the provided examples and verify
# that the output matches the expected results.
# -------------------------------------------------------------------------------
if __name__ == "__main__":
    solution = Solution()

    # ---------------------------------------------------------------------------
    # Example 1:
    # Input:  words = ["eat", "tea", "tan", "ate", "nat", "bat"]
    #         queries = ["ate", "tan"]
    # Expected Output: [["eat", "tea", "ate"], ["tan", "nat"]]
    #
    # Trace:
    #   Build anagram_map:
    #     "eat" -> signature "aet" -> {("aet": [(0,"eat")])}
    #     "tea" -> signature "aet" -> {("aet": [(0,"eat"),(1,"tea")])}
    #     "tan" -> signature "ant" -> {("ant": [(2,"tan")])}
    #     "ate" -> signature "aet" -> {("aet": [(0,"eat"),(1,"tea"),(3,"ate")])}
    #     "nat" -> signature "ant" -> {("ant": [(2,"tan"),(4,"nat")])}
    #     "bat" -> signature "abt" -> {("abt": [(5,"bat")])}
    #
    #   Query "ate": signature "aet" -> matches ["eat","tea","ate"] -> remove "aet"
    #   Query "tan": signature "ant" -> matches ["tan","nat"] -> remove "ant"
    #
    #   Result: [["eat","tea","ate"], ["tan","nat"]] ✓
    # ---------------------------------------------------------------------------
    words1 = ["eat", "tea", "tan", "ate", "nat", "bat"]
    queries1 = ["ate", "tan"]
    result1 = solution.anagram_group_reunion(words1, queries1)
    print("Example 1:")
    print(f"  Input words:   {words1}")
    print(f"  Input queries: {queries1}")
    print(f"  Output:        {result1}")
    print(f"  Expected:      [['eat', 'tea', 'ate'], ['tan', 'nat']]")
    print(f"  Correct:       {result1 == [['eat', 'tea', 'ate'], ['tan', 'nat']]}")
    print()

    # ---------------------------------------------------------------------------
    # Example 2:
    # Input:  words = ["abc", "bca", "cab", "xyz", "zyx"]
    #         queries = ["abc", "xyz", "abc"]
    # Expected Output: [["abc", "bca", "cab"], ["xyz", "zyx"], []]
    #
    # Trace:
    #   Build anagram_map:
    #     "abc" -> signature "abc" -> {("abc": [(0,"abc")])}
    #     "bca" -> signature "abc" -> {("abc": [(0,"abc"),(1,"bca")])}
    #     "cab" -> signature "abc" -> {("abc": [(0,"abc"),(1,"bca"),(2,"cab")])}
    #     "xyz" -> signature "xyz" -> {("xyz": [(3,"xyz")])}
    #     "zyx" -> signature "xyz" -> {("xyz": [(3,"xyz"),(4,"zyx")])}
    #
    #   Query "abc": signature "abc" -> matches ["abc","bca","cab"] -> remove "abc"
    #   Query "xyz": signature "xyz" -> matches ["xyz","zyx"] -> remove "xyz"
    #   Query "abc": signature "abc" -> NOT in map (already removed) -> []
    #
    #   Result: [["abc","bca","cab"], ["xyz","zyx"], []] ✓
    # ---------------------------------------------------------------------------
    words2 = ["abc", "bca", "cab", "xyz", "zyx"]
    queries2 = ["abc", "xyz", "abc"]
    result2 = solution.anagram_group_reunion(words2, queries2)
    print("Example 2:")
    print(f"  Input words:   {words2}")
    print(f"  Input queries: {queries2}")
    print(f"  Output:        {result2}")
    print(f"  Expected:      [['abc', 'bca', 'cab'], ['xyz', 'zyx'], []]")
    print(f"  Correct:       {result2 == [['abc', 'bca', 'cab'], ['xyz', 'zyx'], []]}")
    print()

    # ---------------------------------------------------------------------------
    # Additional edge case: query that never existed in words
    # ---------------------------------------------------------------------------
    words3 = ["hello", "world"]
    queries3 = ["python"]
    result3 = solution.anagram_group_reunion(words3, queries3)
    print("Edge Case - Query not in words:")
    print(f"  Input words:   {words3}")
    print(f"  Input queries: {queries3}")
    print(f"  Output:        {result3}")
    print(f"  Expected:      [[]]")
    print(f"  Correct:       {result3 == [[]]}")
    print()

    # ---------------------------------------------------------------------------
    # Additional edge case: single word, single query that matches
    # ---------------------------------------------------------------------------
    words4 = ["listen"]
    queries4 = ["silent"]
    result4 = solution.anagram_group_reunion(words4, queries4)
    print("Edge Case - Single word anagram match:")
    print(f"  Input words:   {words4}")
    print(f"  Input queries: {queries4}")
    print(f"  Output:        {result4}")
    print(f"  Expected:      [['listen']]")
    print(f"  Correct:       {result4 == [['listen']]}")
```