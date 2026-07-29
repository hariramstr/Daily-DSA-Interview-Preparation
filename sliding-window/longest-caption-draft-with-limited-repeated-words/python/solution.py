"""
Title: Longest Caption Draft With Limited Repeated Words

Problem Description:
A social media team is drafting a caption represented as an array of lowercase words
`words`, where `words[i]` is the i-th word in order.

To keep the caption varied, the team wants to select one contiguous block of words
such that no single distinct word appears more than `k` times inside that block.

Return the length of the longest contiguous subarray of `words` that satisfies this rule.

In other words, among all windows `words[l...r]`, find the maximum size of a window
where the frequency of every word in that window is at most `k`.

This problem should be solved efficiently for large inputs, so an approach that checks
every possible subarray will not pass. A sliding window with frequency tracking is expected.

Constraints:
- 1 <= words.length <= 200000
- 1 <= words[i].length <= 20
- words[i] contains only lowercase English letters
- 1 <= k <= words.length

Example 1:
Input: words = ["sale","new","sale","trend","sale","new"], k = 2
Output: 4

Example 2:
Input: words = ["a","b","a","c","b","b","d"], k = 1
Output: 3
"""

from collections import defaultdict
from typing import DefaultDict, List


class Solution:
    def longest_caption_draft(self, words: List[str], k: int) -> int:
        """
        Find the length of the longest contiguous subarray where every distinct word
        appears at most k times.

        Args:
            words: List of lowercase words representing the caption draft.
            k: Maximum allowed frequency for any single word inside the chosen window.

        Returns:
            The maximum valid window length.

        Time Complexity:
            O(n), where n is the number of words.
            Each word is added to the window once and removed from the window at most once.

        Space Complexity:
            O(m), where m is the number of distinct words currently tracked in the frequency map.
            In the worst case, this can be O(n).
        """
        # This dictionary stores how many times each word appears in the current sliding window.
        # Example:
        # if the current window is ["sale", "new", "sale"],
        # then counts will be {"sale": 2, "new": 1}
        counts: DefaultDict[str, int] = defaultdict(int)

        # `left` is the starting index of the current window.
        # We will expand the window by moving `right`,
        # and shrink it from the left whenever the window becomes invalid.
        left: int = 0

        # This will store the best (maximum) valid window length found so far.
        best: int = 0

        # We move `right` from left to right across the array.
        # At each step, we include words[right] into the current window.
        for right, word in enumerate(words):
            # Add the new word on the right side of the window.
            counts[word] += 1

            # After adding this word, the only possible rule violation is that
            # this specific word's count may now be greater than k.
            #
            # Why only this word?
            # Because before adding it, the window was valid.
            # Adding one word can only increase the count of that one word.
            #
            # So if counts[word] > k, we must shrink the window from the left
            # until this word's count is back to at most k.
            while counts[word] > k:
                # The word leaving the window is words[left].
                left_word: str = words[left]

                # Decrease its frequency because it is no longer inside the window.
                counts[left_word] -= 1

                # Move the left boundary one step to the right,
                # making the window smaller.
                left += 1

            # At this point, the window words[left:right+1] is valid:
            # every word appears at most k times.
            current_length: int = right - left + 1

            # Update the best answer if this valid window is larger than any seen before.
            if current_length > best:
                best = current_length

        # After scanning the whole array, `best` is the answer.
        return best


if __name__ == "__main__":
    solution = Solution()

    # Example 1
    words1: List[str] = ["sale", "new", "sale", "trend", "sale", "new"]
    k1: int = 2
    result1: int = solution.longest_caption_draft(words1, k1)
    print("Example 1 Result:", result1)  # Expected: 4

    # Example 2
    words2: List[str] = ["a", "b", "a", "c", "b", "b", "d"]
    k2: int = 1
    result2: int = solution.longest_caption_draft(words2, k2)
    print("Example 2 Result:", result2)  # Expected: 3

    # Additional quick checks
    words3: List[str] = ["x"]
    k3: int = 1
    result3: int = solution.longest_caption_draft(words3, k3)
    print("Additional Check 1 Result:", result3)  # Expected: 1

    words4: List[str] = ["a", "a", "a", "a"]
    k4: int = 2
    result4: int = solution.longest_caption_draft(words4, k4)
    print("Additional Check 2 Result:", result4)  # Expected: 2