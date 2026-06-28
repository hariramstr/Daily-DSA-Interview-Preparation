"""
Title: Detect Reused Transaction Memo Patterns

Problem Description:
A payments platform stores the free-text memo attached to each transaction as an array
of lowercase words. Two memos are considered to have the same pattern if the sequence
of word repetitions is identical, even if the actual words are different.

For example:
["rent", "paid", "rent", "late"] and ["coffee", "today", "coffee", "again"]
have the same pattern because the 1st and 3rd words are equal in both memos, while
the 2nd and 4th words are different from the others.

Given a list of memos, return all indices of memos that belong to a pattern group
of size at least 2. The returned indices must be sorted in increasing order.

Two memos can only be grouped together if they have:
1. The same length
2. The same repetition structure

Constraints:
- 1 <= memos.length <= 100000
- 1 <= memos[i].length <= 100
- 1 <= memos[i][j].length <= 20
- memos[i][j] contains only lowercase English letters
- The sum of all words across all memos does not exceed 300000

Examples:
1.
Input:
memos = [
    ["rent", "paid", "rent", "late"],
    ["coffee", "today", "coffee", "again"],
    ["taxi", "home", "taxi", "home"],
    ["x", "y", "x", "z"]
]
Output: [0, 1, 3]

2.
Input:
memos = [["a", "b"], ["c", "c"], ["dog", "cat"], ["hi"], ["m", "n", "m"]]
Output: [0, 2]
"""

from typing import Dict, List, Tuple


class Solution:
    def _build_pattern_signature(self, memo: List[str]) -> Tuple[int, ...]:
        """
        Convert one memo into a canonical repetition-pattern signature.

        The idea:
        - Scan the words from left to right.
        - Assign each new word the next available integer ID.
        - Reuse the same ID whenever that word appears again.
        - The resulting sequence of IDs uniquely represents the repetition structure.

        Example:
        ["rent", "paid", "rent", "late"] -> (0, 1, 0, 2)
        ["coffee", "today", "coffee", "again"] -> (0, 1, 0, 2)

        Args:
            memo: A single memo represented as a list of lowercase words.

        Returns:
            A tuple of integers representing the normalized pattern signature.

        Time complexity:
            O(k), where k is the number of words in the memo.

        Space complexity:
            O(k), for the mapping and the produced signature.
        """
        # This dictionary maps each distinct word in the current memo
        # to the integer ID of its first appearance.
        #
        # Example while processing ["a", "b", "a"]:
        # after "a" -> {"a": 0}
        # after "b" -> {"a": 0, "b": 1}
        # after second "a" -> unchanged
        word_to_id: Dict[str, int] = {}

        # We build the pattern step by step in a list because appending to a list
        # is efficient. At the end, we convert it to a tuple so it becomes hashable
        # and can be used as a dictionary key.
        pattern: List[int] = []

        # next_id stores the integer label to assign to the next new word we see.
        next_id = 0

        # Process each word in order because the pattern depends on position.
        for word in memo:
            # If this word has not appeared before in this memo,
            # assign it the next unused ID.
            if word not in word_to_id:
                word_to_id[word] = next_id
                next_id += 1

            # Append the ID corresponding to this word.
            # If the word appeared before, we reuse the same ID.
            pattern.append(word_to_id[word])

        # Convert to tuple so it can be used as a stable hashable signature.
        return tuple(pattern)

    def find_reused_memo_pattern_indices(self, memos: List[List[str]]) -> List[int]:
        """
        Return all indices of memos that belong to a pattern group of size at least 2.

        Strategy:
        1. Normalize each memo into a canonical signature based on repetition structure.
        2. Group memo indices by that signature using a hash map.
        3. Collect indices from groups whose size is at least 2.
        4. Return the collected indices in increasing order.

        Args:
            memos: A list of memos, where each memo is a list of lowercase words.

        Returns:
            A sorted list of indices of memos that share a pattern with at least
            one other memo.

        Time complexity:
            O(T), where T is the total number of words across all memos,
            excluding the small overhead of tuple creation and hashing.

        Space complexity:
            O(T), in the worst case for stored signatures and grouped indices.
        """
        # This dictionary groups memo indices by their normalized pattern signature.
        #
        # Key:
        #   A tuple like (0, 1, 0, 2)
        #
        # Value:
        #   A list of all memo indices that produce that signature
        #
        # Example:
        # {
        #   (0, 1, 0, 2): [0, 1, 3],
        #   (0, 1, 0, 1): [2]
        # }
        groups: Dict[Tuple[int, ...], List[int]] = {}

        # Enumerate gives us both:
        # - index: where the memo appears in the input
        # - memo: the actual list of words
        for index, memo in enumerate(memos):
            # Build the canonical signature for this memo.
            signature = self._build_pattern_signature(memo)

            # If this signature has not been seen before, create a new list for it.
            if signature not in groups:
                groups[signature] = []

            # Add the current memo's index to its signature group.
            groups[signature].append(index)

        # This list will store the final answer.
        result: List[int] = []

        # Now inspect every group.
        for indices in groups.values():
            # We only want groups with at least 2 memos,
            # because the problem asks for memos that belong to a repeated pattern group.
            if len(indices) >= 2:
                # The indices inside each group are already in increasing order
                # because we processed memos from left to right.
                result.extend(indices)

        # Different groups may have indices interleaved in theory only if we were
        # collecting them in some arbitrary order. Since dictionary iteration order
        # depends on insertion order of signatures, not index order, we sort at the end
        # to guarantee the required increasing order.
        result.sort()

        return result


if __name__ == "__main__":
    solution = Solution()

    # Example 1 from the problem statement
    memos1 = [
        ["rent", "paid", "rent", "late"],
        ["coffee", "today", "coffee", "again"],
        ["taxi", "home", "taxi", "home"],
        ["x", "y", "x", "z"],
    ]
    result1 = solution.find_reused_memo_pattern_indices(memos1)
    print("Example 1 Output:", result1)  # Expected: [0, 1, 3]

    # Example 2 from the problem statement
    memos2 = [
        ["a", "b"],
        ["c", "c"],
        ["dog", "cat"],
        ["hi"],
        ["m", "n", "m"],
    ]
    result2 = solution.find_reused_memo_pattern_indices(memos2)
    print("Example 2 Output:", result2)  # Expected: [0, 2]

    # Additional quick sanity check
    memos3 = [
        ["one"],
        ["two"],
        ["a", "a"],
        ["b", "c"],
        ["x"],
    ]
    result3 = solution.find_reused_memo_pattern_indices(memos3)
    print("Additional Example Output:", result3)  # Expected: [0, 1, 4]