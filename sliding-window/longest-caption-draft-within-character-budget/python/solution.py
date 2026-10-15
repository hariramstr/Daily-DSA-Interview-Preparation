"""
Title: Longest Caption Draft Within Character Budget

Problem Description:
A social media team is drafting a caption from a sequence of word fragments. The i-th
fragment contributes len(words[i]) characters to the final caption, and fragments must
be used in their original order. You are given an array words of non-empty strings and
an integer maxChars. Find the maximum number of consecutive fragments that can be
included so that the total number of characters in the chosen window is at most
maxChars.

Only the characters inside each fragment count toward the budget. Ignore spaces between
fragments; assume they are inserted automatically and do not consume any characters.
Your task is to return the length of the longest contiguous block of fragments whose
combined character count does not exceed maxChars.

This problem is intended to be solved efficiently for large inputs. A brute-force
solution that checks every subarray will be too slow. Think about maintaining a moving
window and shrinking it only when the current total exceeds the allowed budget.

Constraints:
- 1 <= words.length <= 100000
- 1 <= words[i].length <= 10000
- 1 <= maxChars <= 10^9
- words[i] consists of lowercase English letters
"""

from typing import List


class Solution:
    def longest_caption_draft(self, words: List[str], maxChars: int) -> int:
        """
        Find the maximum number of consecutive word fragments whose total character
        count does not exceed maxChars.

        Args:
            words: A list of non-empty lowercase strings.
            maxChars: The maximum allowed total number of characters in the window.

        Returns:
            The length of the longest contiguous block of fragments whose combined
            character count is at most maxChars.

        Time complexity:
            O(n), where n is the number of words. Each word enters the sliding window
            once and leaves it at most once.

        Space complexity:
            O(1) extra space, excluding the input.
        """
        # This pointer marks the left boundary of our current sliding window.
        # The window will always represent a contiguous block of words:
        # words[left:right + 1]
        left: int = 0

        # This variable stores the sum of character lengths of all words currently
        # inside the window.
        current_total_chars: int = 0

        # This keeps track of the best (largest) valid window length found so far.
        best_length: int = 0

        # We expand the window one word at a time by moving the right pointer from
        # left to right across the array.
        for right in range(len(words)):
            # Add the newly included word's character count to the running total.
            # We use len(words[right]) because only the characters inside each fragment
            # count toward the budget.
            current_total_chars += len(words[right])

            # If the window is now too expensive (too many characters), we must shrink
            # it from the left until it becomes valid again.
            #
            # Why a while loop instead of an if statement?
            # Because adding one large word may cause the total to exceed maxChars by
            # more than the leftmost word can fix. We may need to remove multiple words.
            while current_total_chars > maxChars and left <= right:
                # Remove the leftmost word from the running total because it is no
                # longer part of the window after we move left forward.
                current_total_chars -= len(words[left])

                # Move the left boundary one step to the right, shrinking the window.
                left += 1

            # At this point, the window words[left:right + 1] is guaranteed to be valid:
            # its total character count is <= maxChars.
            #
            # We now compute its length and compare it with the best answer seen so far.
            current_window_length: int = right - left + 1
            if current_window_length > best_length:
                best_length = current_window_length

        # After processing all possible right endpoints, best_length contains the
        # maximum valid number of consecutive fragments.
        return best_length


if __name__ == "__main__":
    solution = Solution()

    # Example 1 from the prompt:
    # The prompt's written explanation contains an inconsistency:
    # ["x", "trend"] has length 1 + 5 = 6, valid length 2
    # ["viral", "now"] has length 5 + 3 = 8, valid length 2
    # No valid length-3 window exists, so the correct answer is 2.
    words1: List[str] = ["go", "viral", "now", "x", "trend"]
    max_chars1: int = 8
    result1: int = solution.longest_caption_draft(words1, max_chars1)
    print(f"Example 1 result: {result1}")  # Expected: 2

    # Example 2:
    # ["a", "bb", "c"] has total 1 + 2 + 1 = 4, so answer is 3.
    words2: List[str] = ["a", "bb", "c", "ddd", "e"]
    max_chars2: int = 4
    result2: int = solution.longest_caption_draft(words2, max_chars2)
    print(f"Example 2 result: {result2}")  # Expected: 3

    # Additional quick checks for clarity.
    words3: List[str] = ["hello"]
    max_chars3: int = 5
    result3: int = solution.longest_caption_draft(words3, max_chars3)
    print(f"Additional example 1 result: {result3}")  # Expected: 1

    words4: List[str] = ["abcd", "ef", "g", "hi"]
    max_chars4: int = 3
    result4: int = solution.longest_caption_draft(words4, max_chars4)
    print(f"Additional example 2 result: {result4}")  # Expected: 2 ("g", "hi")