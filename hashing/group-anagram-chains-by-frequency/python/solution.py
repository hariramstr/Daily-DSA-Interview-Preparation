"""
Title: Group Anagram Chains by Frequency
Difficulty: Medium
Topic: Hashing

Problem Description:
You are given a list of words. Two words are considered **anagram siblings** if one can be
rearranged to form the other (i.e., they contain the same characters with the same frequencies).
Your task is to group all words into anagram families and then return the groups **sorted by
their size in descending order**. If two groups have the same size, sort them lexicographically
by their smallest word in ascending order.

Additionally, for each group, return the words sorted in **lexicographic order**.

Constraints:
- 1 <= words.length <= 10^4
- 1 <= words[i].length <= 20
- All characters in words[i] are lowercase English letters.
- Words in the input may contain duplicates; duplicate words belong to the same group.
"""

from collections import defaultdict
from typing import List, Dict


class Solution:
    def groupAnagrams(self, words: List[str]) -> List[List[str]]:
        """
        Groups words into anagram families, sorts each group lexicographically,
        and returns groups sorted by descending size (ties broken by smallest word ascending).

        Args:
            words (List[str]): A list of lowercase English words (may contain duplicates).

        Returns:
            List[List[str]]: A list of groups, where each group is a sorted list of anagram
                             siblings. Groups are ordered by descending size; ties broken
                             lexicographically by the smallest word in each group.

        Time Complexity:  O(N * K * log K) where N = number of words, K = max word length.
                          Sorting each word costs O(K log K); we do this for all N words.
                          Final sort of groups costs O(G log G) where G <= N.
        Space Complexity: O(N * K) to store all words in the hash map.
        """

        # -----------------------------------------------------------------------
        # STEP 1: Build a hash map that maps a "canonical key" to a list of words.
        #
        # The canonical key for a word is its sorted version.
        # For example:
        #   "eat"  -> sorted -> "aet"
        #   "tea"  -> sorted -> "aet"
        #   "ate"  -> sorted -> "aet"
        # All three map to the same key "aet", so they end up in the same group.
        #
        # We use defaultdict(list) so we don't need to check if a key exists before
        # appending — it automatically initialises missing keys with an empty list.
        # -----------------------------------------------------------------------
        anagram_map: Dict[str, List[str]] = defaultdict(list)

        for word in words:
            # Sort the characters of the word to produce the canonical key.
            # ''.join(sorted(word)) turns ['a','e','t'] back into the string "aet".
            canonical_key: str = "".join(sorted(word))

            # Append the original word (not the sorted version) to its group.
            anagram_map[canonical_key].append(word)

        # -----------------------------------------------------------------------
        # STEP 2: Sort the words inside each group lexicographically.
        #
        # The problem requires each group's words to appear in lexicographic order.
        # We sort each list in-place using Python's built-in sort (Timsort),
        # which is O(M log M) where M is the number of words in that group.
        # -----------------------------------------------------------------------
        groups: List[List[str]] = []
        for group in anagram_map.values():
            group.sort()          # Sort words within the group lexicographically.
            groups.append(group)

        # -----------------------------------------------------------------------
        # STEP 3: Sort the groups themselves.
        #
        # Primary sort key   : group size in DESCENDING order  -> use -len(group)
        # Secondary sort key : smallest word in ASCENDING order -> group[0] is the
        #                       smallest because we already sorted each group above.
        #
        # Python's sort is stable and supports tuple keys, so we pass a lambda
        # that returns (-size, smallest_word).  Sorting by -size descending means
        # larger groups come first; for equal sizes, lexicographically smaller
        # "smallest word" comes first.
        # -----------------------------------------------------------------------
        groups.sort(key=lambda group: (-len(group), group[0]))

        return groups


# ---------------------------------------------------------------------------
# Main block: demonstrate the solution with the examples from the problem.
# ---------------------------------------------------------------------------
if __name__ == "__main__":
    solution = Solution()

    # ------------------------------------------------------------------
    # Example 1
    # Input : ["eat", "tea", "tan", "ate", "nat", "bat"]
    # Expected Output: [["ate", "eat", "tea"], ["nat", "tan"], ["bat"]]
    #
    # Trace:
    #   "eat" -> key "aet"  -> {"aet": ["eat"]}
    #   "tea" -> key "aet"  -> {"aet": ["eat", "tea"]}
    #   "tan" -> key "ant"  -> {"aet": [...], "ant": ["tan"]}
    #   "ate" -> key "aet"  -> {"aet": ["eat", "tea", "ate"]}
    #   "nat" -> key "ant"  -> {"ant": ["tan", "nat"]}
    #   "bat" -> key "abt"  -> {"abt": ["bat"]}
    #
    # After sorting each group:
    #   "aet" -> ["ate", "eat", "tea"]   size 3, smallest "ate"
    #   "ant" -> ["nat", "tan"]          size 2, smallest "nat"
    #   "abt" -> ["bat"]                 size 1, smallest "bat"
    #
    # Sort groups by (-size, smallest):
    #   (-3, "ate"), (-2, "nat"), (-1, "bat")
    #   -> [["ate","eat","tea"], ["nat","tan"], ["bat"]]  ✓
    # ------------------------------------------------------------------
    words1 = ["eat", "tea", "tan", "ate", "nat", "bat"]
    result1 = solution.groupAnagrams(words1)
    print("Example 1:")
    print(f"  Input : {words1}")
    print(f"  Output: {result1}")
    print(f"  Expected: [['ate', 'eat', 'tea'], ['nat', 'tan'], ['bat']]")
    print()

    # ------------------------------------------------------------------
    # Example 2
    # Input : ["abc", "bca", "xyz", "zyx", "cab", "yzx"]
    # Expected Output: [["abc", "bca", "cab"], ["xyz", "yzx", "zyx"]]
    #
    # Trace:
    #   "abc" -> key "abc" -> {"abc": ["abc"]}
    #   "bca" -> key "abc" -> {"abc": ["abc", "bca"]}
    #   "xyz" -> key "xyz" -> {"xyz": ["xyz"]}
    #   "zyx" -> key "xyz" -> {"xyz": ["xyz", "zyx"]}
    #   "cab" -> key "abc" -> {"abc": ["abc", "bca", "cab"]}
    #   "yzx" -> key "xyz" -> {"xyz": ["xyz", "zyx", "yzx"]}
    #
    # After sorting each group:
    #   "abc" -> ["abc", "bca", "cab"]   size 3, smallest "abc"
    #   "xyz" -> ["xyz", "yzx", "zyx"]   size 3, smallest "xyz"
    #
    # Sort groups by (-size, smallest):
    #   Both size 3; "abc" < "xyz" -> "abc" group first
    #   -> [["abc","bca","cab"], ["xyz","yzx","zyx"]]  ✓
    # ------------------------------------------------------------------
    words2 = ["abc", "bca", "xyz", "zyx", "cab", "yzx"]
    result2 = solution.groupAnagrams(words2)
    print("Example 2:")
    print(f"  Input : {words2}")
    print(f"  Output: {result2}")
    print(f"  Expected: [['abc', 'bca', 'cab'], ['xyz', 'yzx', 'zyx']]")
    print()

    # ------------------------------------------------------------------
    # Extra edge-case: single word
    # ------------------------------------------------------------------
    words3 = ["hello"]
    result3 = solution.groupAnagrams(words3)
    print("Edge case (single word):")
    print(f"  Input : {words3}")
    print(f"  Output: {result3}")
    print(f"  Expected: [['hello']]")
    print()

    # ------------------------------------------------------------------
    # Extra edge-case: duplicate words
    # ------------------------------------------------------------------
    words4 = ["ab", "ba", "ab"]
    result4 = solution.groupAnagrams(words4)
    print("Edge case (duplicates):")
    print(f"  Input : {words4}")
    print(f"  Output: {result4}")
    print(f"  Expected: [['ab', 'ab', 'ba']]")