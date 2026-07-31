"""
Title: Count Documents Sharing the Same Keyword Fingerprint
Difficulty: Hard
Topic: Hashing

Problem Description:
You are given a collection of documents. Each document is represented by a list of
keywords, where keywords may repeat within the same document because a term can
appear multiple times.

Define the fingerprint of a document as the multiset of keyword frequencies,
ignoring the actual keyword names.

Example:
["red", "red", "blue", "green", "green"]
has keyword counts:
- red -> 2
- blue -> 1
- green -> 2

So its fingerprint is the sorted list of counts: [1, 2, 2]

Another document:
["cat", "cat", "dog", "fox", "fox"]
has counts:
- cat -> 2
- dog -> 1
- fox -> 2

Its fingerprint is also [1, 2, 2], so the two documents match.

Task:
Count how many unordered pairs of documents share the same fingerprint.

Two documents are matching if:
1. We count occurrences of each distinct keyword in each document
2. We take the list of those counts
3. We sort that list
4. The sorted lists are identical

The order of keywords inside a document does not matter, and the actual keyword
strings do not matter once frequencies are computed.

Constraints:
- 1 <= documents.length <= 10^5
- 1 <= total number of keywords across all documents <= 3 * 10^5
- 1 <= keyword.length <= 20
- Keywords contain only lowercase English letters
- Each document contains at least 1 keyword
"""

from collections import Counter, defaultdict
from typing import DefaultDict, Dict, List, Tuple


class Solution:
    def _build_fingerprint(self, document: List[str]) -> Tuple[int, ...]:
        """
        Build a canonical fingerprint for one document.

        The fingerprint is defined as the sorted tuple of keyword frequencies.
        We ignore the actual keyword names and only keep how many times each
        distinct keyword appears.

        Args:
            document: A list of keywords representing one document.

        Returns:
            A hashable tuple containing the sorted keyword frequencies.

        Time complexity:
            O(k + d log d)
            where:
            - k is the number of keywords in the document
            - d is the number of distinct keywords in the document

        Space complexity:
            O(d)
            for storing keyword counts and the resulting frequency list.
        """
        # Step 1:
        # Count how many times each keyword appears in this document.
        #
        # Example:
        # document = ["red", "red", "blue", "green", "green"]
        # Counter(document) becomes:
        # {
        #   "red": 2,
        #   "blue": 1,
        #   "green": 2
        # }
        #
        # We use Counter because it is the most direct and readable way in Python
        # to count repeated items in a list.
        keyword_counts: Counter[str] = Counter(document)

        # Step 2:
        # Extract only the frequency values, because the problem explicitly says
        # that keyword names should be ignored once frequencies are computed.
        #
        # From:
        # {"red": 2, "blue": 1, "green": 2}
        # we take:
        # [2, 1, 2]
        frequency_values: List[int] = list(keyword_counts.values())

        # Step 3:
        # Sort the frequencies so that documents with the same multiset of counts
        # produce exactly the same canonical representation.
        #
        # Why sorting?
        # Because [2, 1, 2] and [1, 2, 2] represent the same multiset, but lists
        # in different orders would not compare equal unless we normalize them.
        #
        # After sorting:
        # [2, 1, 2] -> [1, 2, 2]
        frequency_values.sort()

        # Step 4:
        # Convert the sorted list to a tuple.
        #
        # Why tuple instead of list?
        # Because tuples are immutable and hashable, so they can be used as keys
        # in dictionaries. We need that in order to count how many documents share
        # the same fingerprint.
        return tuple(frequency_values)

    def count_matching_pairs(self, documents: List[List[str]]) -> int:
        """
        Count how many unordered pairs of documents share the same fingerprint.

        For each document:
        1. Count keyword frequencies
        2. Sort those frequencies
        3. Use the sorted tuple as a canonical fingerprint
        4. Count how many documents have each fingerprint
        5. For each fingerprint with frequency c, add c * (c - 1) // 2 pairs

        Args:
            documents: A list of documents, where each document is a list of keywords.

        Returns:
            The total number of unordered matching document pairs.

        Time complexity:
            O(T + sum(d_i log d_i))
            where:
            - T is the total number of keywords across all documents
            - d_i is the number of distinct keywords in document i

            This is efficient enough because the total number of keywords is bounded.

        Space complexity:
            O(T) in the worst case across temporary counting structures and the
            fingerprint frequency map, though practically bounded by the total
            number of distinct keywords and number of unique fingerprints.
        """
        # This dictionary will map:
        # fingerprint -> number of documents that have this fingerprint
        #
        # Example:
        # (1, 2, 2) -> 3
        # (1, 1, 2) -> 1
        # (1,)      -> 1
        #
        # We use defaultdict(int) so that unseen fingerprints start at 0
        # automatically, which keeps the code simple and beginner-friendly.
        fingerprint_count: DefaultDict[Tuple[int, ...], int] = defaultdict(int)

        # Process each document one by one.
        for document in documents:
            # Build the canonical fingerprint for the current document.
            fingerprint: Tuple[int, ...] = self._build_fingerprint(document)

            # Record that we have seen one more document with this fingerprint.
            fingerprint_count[fingerprint] += 1

        # Now compute the number of unordered pairs.
        #
        # If a fingerprint appears c times, then the number of ways to choose
        # 2 documents from those c documents is:
        #
        #   C(c, 2) = c * (c - 1) // 2
        #
        # This counts unordered pairs exactly once.
        total_pairs: int = 0

        for count in fingerprint_count.values():
            total_pairs += count * (count - 1) // 2

        return total_pairs


if __name__ == "__main__":
    solution = Solution()

    # Example 1 from the prompt:
    # Fingerprints:
    # 0 -> [1,2,2]
    # 1 -> [1,2,2]
    # 2 -> [1,1,2]
    # 3 -> [1,2,2]
    # 4 -> [1]
    #
    # Matching pairs among documents 0, 1, 3:
    # (0,1), (0,3), (1,3) => 3 pairs
    documents1: List[List[str]] = [
        ["red", "red", "blue", "green", "green"],
        ["cat", "cat", "dog", "fox", "fox"],
        ["a", "b", "b", "c"],
        ["m", "m", "n", "n", "p"],
        ["z"],
    ]
    result1: int = solution.count_matching_pairs(documents1)
    print(result1)  # Expected: 3

    # Important note:
    # The second example text in the prompt is internally inconsistent.
    # Based on the formal definition, the correct count for the provided input is 1:
    #
    # ["aa","bb","aa","cc","cc","cc"] -> [1,2,3]
    # ["x","y","y","z","z","z"]       -> [1,2,3]
    # ["p","p","q","q","r","r"]       -> [2,2,2]
    # ["k"]                           -> [1]
    # ["u","v","w"]                   -> [1,1,1]
    #
    # Only the first two documents match, so the answer is 1.
    documents2: List[List[str]] = [
        ["aa", "bb", "aa", "cc", "cc", "cc"],
        ["x", "y", "y", "z", "z", "z"],
        ["p", "p", "q", "q", "r", "r"],
        ["k"],
        ["u", "v", "w"],
    ]
    result2: int = solution.count_matching_pairs(documents2)
    print(result2)  # Correct by formal definition: 1

    # Additional sanity check:
    # Two single-keyword documents should match because both have fingerprint [1].
    documents3: List[List[str]] = [
        ["apple"],
        ["banana"],
        ["cat", "cat"],
        ["dog", "dog"],
        ["x", "y"],
    ]
    # Fingerprints:
    # ["apple"]   -> [1]
    # ["banana"]  -> [1]
    # ["cat","cat"] -> [2]
    # ["dog","dog"] -> [2]
    # ["x","y"]   -> [1,1]
    #
    # Pairs:
    # [1] group => 1 pair
    # [2] group => 1 pair
    # total => 2
    result3: int = solution.count_matching_pairs(documents3)
    print(result3)  # Expected: 2