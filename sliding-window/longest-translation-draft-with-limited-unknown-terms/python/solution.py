"""
Title: Longest Translation Draft With Limited Unknown Terms

Problem Description:
A localization team is reviewing a draft translation represented by an array of lowercase words.
Some words are already approved and appear in a dictionary, while other words are currently
unknown and require manual review. The team wants to find the longest contiguous section of the
draft that can be sent to a reviewer, under the rule that the section may contain at most k
unknown words.

You are given a string array words, a string array approved containing all approved dictionary
words, and an integer k. Return the length of the longest contiguous subarray of words that
contains at most k words not present in approved.

A word is considered unknown if it does not exist in the approved list. The approved list may
contain duplicates, but they should be treated as a set. Your solution should be efficient enough
for large inputs.

Constraints:
- 1 <= words.length <= 200000
- 1 <= approved.length <= 200000
- 0 <= k <= words.length
- 1 <= words[i].length, approved[i].length <= 20
- All words consist only of lowercase English letters

Example 1:
Input:
    words = ["hola","mundo","api","seguro","beta","listo"]
    approved = ["hola","mundo","seguro","listo"]
    k = 1
Output:
    4
Explanation:
    The longest valid window is ["hola","mundo","api","seguro"].
    It contains only one unknown word: "api".

Example 2:
Input:
    words = ["save","draft","tmp","publish","tmp","done"]
    approved = ["save","draft","publish","done"]
    k = 0
Output:
    2
Explanation:
    With no unknown words allowed, the longest valid windows are ["save","draft"],
    ["publish"], and ["done"]. The maximum length is 2.
"""

from typing import List, Set


class Solution:
    def longest_translation_draft(self, words: List[str], approved: List[str], k: int) -> int:
        """
        Find the length of the longest contiguous subarray containing at most k unknown words.

        Args:
            words: The draft translation as a list of lowercase words.
            approved: The list of approved dictionary words. Duplicates are treated as one word.
            k: The maximum number of unknown words allowed inside the chosen contiguous subarray.

        Returns:
            The maximum length of a contiguous subarray with at most k unknown words.

        Time Complexity:
            O(n + m), where:
            - n is the length of words
            - m is the length of approved
            Each word is processed at most a constant number of times.

        Space Complexity:
            O(m), for storing the approved words in a set.
        """
        # Convert the approved list into a set.
        #
        # Why do this?
        # - We need to repeatedly check whether a word is approved.
        # - Membership testing in a Python set is average-case O(1).
        # - This is much faster than checking in a list, which would be O(m) each time.
        # - Since approved may contain duplicates, converting to a set also naturally removes them.
        approved_set: Set[str] = set(approved)

        # We will use the classic sliding window / two-pointer technique.
        #
        # The idea:
        # - Maintain a window words[left:right+1].
        # - Expand the right pointer one step at a time.
        # - Track how many unknown words are currently inside the window.
        # - If the window becomes invalid (unknown_count > k), move left forward
        #   until the window becomes valid again.
        #
        # This works efficiently because:
        # - Each element enters the window once when right moves.
        # - Each element leaves the window at most once when left moves.
        # - Therefore total work is linear.
        left: int = 0
        unknown_count: int = 0
        best_length: int = 0

        # Iterate through the array using right as the end of the current window.
        for right in range(len(words)):
            # Step 1: Include words[right] into the current window.
            #
            # If this word is not in the approved set, it is an unknown word,
            # so we increase the count of unknown words in the window.
            if words[right] not in approved_set:
                unknown_count += 1

            # Step 2: If the window has too many unknown words, shrink it from the left.
            #
            # We keep moving left until the window satisfies the rule:
            # unknown_count <= k
            #
            # Why a while loop instead of if?
            # - Because adding one new word at right could make the window exceed the limit,
            #   and we may need to remove multiple words from the left to restore validity.
            while unknown_count > k:
                # Before moving left forward, check whether the word leaving the window
                # is unknown. If it is, we must decrease unknown_count because that word
                # will no longer be part of the window.
                if words[left] not in approved_set:
                    unknown_count -= 1

                # Move the left boundary one step to the right.
                left += 1

            # Step 3: At this point, the window words[left:right+1] is valid.
            #
            # Compute its length and update the best answer seen so far.
            current_length: int = right - left + 1
            if current_length > best_length:
                best_length = current_length

        # After processing all possible right endpoints, best_length stores
        # the maximum valid window size.
        return best_length


if __name__ == "__main__":
    solution = Solution()

    # Example 1
    words1: List[str] = ["hola", "mundo", "api", "seguro", "beta", "listo"]
    approved1: List[str] = ["hola", "mundo", "seguro", "listo"]
    k1: int = 1
    result1: int = solution.longest_translation_draft(words1, approved1, k1)
    print(result1)  # Expected: 4

    # Example 2
    words2: List[str] = ["save", "draft", "tmp", "publish", "tmp", "done"]
    approved2: List[str] = ["save", "draft", "publish", "done"]
    k2: int = 0
    result2: int = solution.longest_translation_draft(words2, approved2, k2)
    print(result2)  # Expected: 2