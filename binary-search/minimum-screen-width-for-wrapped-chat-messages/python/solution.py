"""
Title: Minimum Screen Width for Wrapped Chat Messages

Problem Description:
You are building a chat application that displays messages in a fixed-width text area.
Each message is a string containing lowercase letters and single spaces between words,
with no leading or trailing spaces. A message is shown using word wrapping: each line
can contain one or more whole words separated by a single space, and a word cannot be
split across lines. The width of a line is the total number of characters in that line,
including spaces between words.

Given an array messages and an integer maxLines, find the minimum screen width W such
that every message can be displayed using at most maxLines lines. All messages must fit
under the same width W.

If a message contains a word of length L, then any valid width must satisfy W >= L.
Your task is to return the smallest such width.

This problem should be solved efficiently. A common approach is to binary search the
answer W and check whether all messages can be wrapped within maxLines lines for that
width.

Constraints:
- 1 <= messages.length <= 100000
- 1 <= messages[i].length <= 100000
- The total length of all messages combined does not exceed 200000
- 1 <= maxLines <= 100000
- messages[i] contains only lowercase English letters and single spaces
- Each word length is at least 1
"""

from typing import List, Tuple


class Solution:
    def _preprocess_messages(self, messages: List[str]) -> Tuple[List[List[int]], int, int]:
        """
        Convert each message into a list of word lengths and compute binary-search bounds.

        Args:
            messages: List of message strings.

        Returns:
            A tuple containing:
            - A list where each message is represented by a list of its word lengths.
            - The minimum possible valid width (largest single word length across all messages).
            - The maximum needed width (largest full message length across all messages).

        Time complexity:
            O(total_characters)

        Space complexity:
            O(total_words)
        """
        # We store only word lengths instead of the original words because wrapping decisions
        # depend only on lengths and spaces between words. This reduces repeated work during
        # binary search checks.
        messages_word_lengths: List[List[int]] = []

        # Lower bound for binary search:
        # Any valid width must be at least as large as the longest word in all messages.
        min_width: int = 0

        # Upper bound for binary search:
        # A width equal to the longest entire message length always works, because that message
        # can then fit on one line, and every shorter message also fits on one line.
        max_width: int = 0

        for message in messages:
            # Splitting by spaces is safe because the problem guarantees:
            # - single spaces between words
            # - no leading/trailing spaces
            words: List[str] = message.split()
            lengths: List[int] = [len(word) for word in words]
            messages_word_lengths.append(lengths)

            # Update the lower bound using the longest word in this message.
            local_longest_word: int = max(lengths)
            if local_longest_word > min_width:
                min_width = local_longest_word

            # Update the upper bound using the full message length.
            # This is the width needed to place the entire message on one line.
            message_length: int = len(message)
            if message_length > max_width:
                max_width = message_length

        return messages_word_lengths, min_width, max_width

    def _can_fit_message(self, word_lengths: List[int], width: int, max_lines: int) -> bool:
        """
        Check whether one message can be wrapped within max_lines using the given width.

        Args:
            word_lengths: Lengths of words in a single message.
            width: Candidate screen width.
            max_lines: Maximum allowed number of lines for the message.

        Returns:
            True if the message fits within max_lines lines, otherwise False.

        Time complexity:
            O(number_of_words_in_message)

        Space complexity:
            O(1)
        """
        # We greedily place as many words as possible on the current line.
        # This greedy strategy is optimal for minimizing the number of lines:
        # if a word fits on the current line, moving it to the next line can never reduce
        # the total number of lines used.
        lines_used: int = 1
        current_line_length: int = 0

        for word_length in word_lengths:
            # Safety check:
            # If even a single word is longer than the width, the message cannot fit.
            # In practice, our binary search lower bound prevents this, but keeping this
            # check makes the helper method fully correct and self-contained.
            if word_length > width:
                return False

            if current_line_length == 0:
                # The current line is empty, so we place the word directly.
                current_line_length = word_length
            else:
                # If we add another word to a non-empty line, we must also add one space.
                needed_length: int = current_line_length + 1 + word_length

                if needed_length <= width:
                    # The word fits on the current line, so extend the line.
                    current_line_length = needed_length
                else:
                    # The word does not fit, so we start a new line with this word.
                    lines_used += 1

                    # Early exit:
                    # As soon as we exceed max_lines, there is no need to continue.
                    if lines_used > max_lines:
                        return False

                    current_line_length = word_length

        return True

    def _can_fit_all_messages(
        self,
        messages_word_lengths: List[List[int]],
        width: int,
        max_lines: int,
    ) -> bool:
        """
        Check whether all messages fit within max_lines lines under the same width.

        Args:
            messages_word_lengths: Preprocessed messages as lists of word lengths.
            width: Candidate screen width.
            max_lines: Maximum allowed number of lines per message.

        Returns:
            True if every message fits, otherwise False.

        Time complexity:
            O(total_number_of_words_across_all_messages)

        Space complexity:
            O(1)
        """
        # We test each message independently because the line limit applies to each message,
        # not to the total across all messages.
        for word_lengths in messages_word_lengths:
            if not self._can_fit_message(word_lengths, width, max_lines):
                return False
        return True

    def minimumScreenWidth(self, messages: List[str], maxLines: int) -> int:
        """
        Find the minimum width so every message can be wrapped into at most maxLines lines.

        Args:
            messages: List of message strings.
            maxLines: Maximum allowed number of lines per message.

        Returns:
            The smallest valid screen width.

        Time complexity:
            O(total_characters + total_words * log(max_message_length))

        Space complexity:
            O(total_words)
        """
        # Step 1: Preprocess messages into word lengths.
        # This avoids splitting strings repeatedly during binary search checks.
        messages_word_lengths, left, right = self._preprocess_messages(messages)

        # Step 2: Binary search on the answer.
        # Why binary search works:
        # - If a width W is valid, then any larger width is also valid.
        # - If a width W is invalid, then any smaller width is also invalid.
        # This monotonic property is exactly what binary search needs.
        while left < right:
            # Midpoint candidate width.
            mid: int = (left + right) // 2

            # Check whether this width is sufficient for all messages.
            if self._can_fit_all_messages(messages_word_lengths, mid, maxLines):
                # mid works, so the answer is mid or smaller.
                right = mid
            else:
                # mid does not work, so we must search larger widths.
                left = mid + 1

        # At loop end, left == right and points to the smallest valid width.
        return left


if __name__ == "__main__":
    solution = Solution()

    # Example 1
    messages_1: List[str] = ["chat app design", "binary search works"]
    max_lines_1: int = 2
    result_1: int = solution.minimumScreenWidth(messages_1, max_lines_1)
    print("Example 1 result:", result_1)  # Expected: 12

    # Example 2
    messages_2: List[str] = ["a bb ccc", "dddd e"]
    max_lines_2: int = 3
    result_2: int = solution.minimumScreenWidth(messages_2, max_lines_2)
    print("Example 2 result:", result_2)  # Expected: 4

    # Additional quick sanity checks
    messages_3: List[str] = ["hello"]
    max_lines_3: int = 1
    result_3: int = solution.minimumScreenWidth(messages_3, max_lines_3)
    print("Additional test 1 result:", result_3)  # Expected: 5

    messages_4: List[str] = ["a a a a", "bb bb"]
    max_lines_4: int = 2
    result_4: int = solution.minimumScreenWidth(messages_4, max_lines_4)
    print("Additional test 2 result:", result_4)  # Expected: 3