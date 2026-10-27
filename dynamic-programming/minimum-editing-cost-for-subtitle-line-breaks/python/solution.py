"""
Title: Minimum Editing Cost for Subtitle Line Breaks

Problem Description:
A video platform stores a spoken transcript as an array of words. To generate subtitles,
the transcript must be split into lines while preserving the original word order.
You may place a line break between any two adjacent words, but each subtitle line must
contain at most `maxWidth` characters, including single spaces between consecutive words
on the same line.

The cost of a line is defined as the square of its unused space:
    (maxWidth - usedCharacters)^2

However, the final line is displayed without padding penalties, so its cost is 0
as long as it fits within `maxWidth`.

Your task is to return the minimum total editing cost needed to split the transcript
into valid subtitle lines.

Formally, if words `i...j` are placed on one line, then the line length is the sum of
their word lengths plus `(j - i)` spaces. This line is valid only if that total is
less than or equal to `maxWidth`.

Return the minimum possible sum of line costs over all lines.

Constraints:
- 1 <= words.length <= 500
- 1 <= words[i].length <= maxWidth <= 1000
- words[i] contains only English letters, digits, or punctuation without spaces.
"""

from typing import List


class Solution:
    def minimum_editing_cost(self, words: List[str], maxWidth: int) -> int:
        """
        Compute the minimum total subtitle line-breaking cost.

        This method uses dynamic programming. For each starting word index `i`,
        we try placing words `i..j` on the current line for every valid `j`.
        If `j` is the last word, that line contributes cost 0.
        Otherwise, the line contributes the square of its unused spaces, plus
        the best cost for formatting the remaining suffix starting at `j + 1`.

        Args:
            words: List of transcript words in original order.
            maxWidth: Maximum allowed width of each subtitle line.

        Returns:
            The minimum possible total editing cost.

        Time complexity:
            O(n^2), where n is the number of words. For each starting index,
            we expand the ending index until the line becomes too long.

        Space complexity:
            O(n), for the dynamic programming array.
        """
        n: int = len(words)

        # Store the length of each word once so we do not repeatedly call len(...)
        # inside nested loops. This keeps the code cleaner and avoids redundant work.
        word_lengths: List[int] = [len(word) for word in words]

        # dp[i] will store the minimum total cost needed to format the suffix
        # starting from word index i.
        #
        # Meaning:
        #   - We still need to place words[i], words[i+1], ..., words[n-1].
        #   - dp[i] is the best possible cost for that remaining part.
        #
        # We also define dp[n] = 0:
        #   - If there are no words left to place, the cost is naturally 0.
        #
        # We initialize all entries to 0 first, then fill from right to left.
        dp: List[int] = [0] * (n + 1)

        # We process indices from the end toward the beginning.
        #
        # Why right to left?
        # Because when computing dp[i], we may need dp[j + 1] for some j >= i.
        # Those values correspond to suffixes that start later, so they must
        # already be known.
        for i in range(n - 1, -1, -1):
            # Start with a very large number so any valid arrangement will be smaller.
            best_cost: int = float("inf")

            # `current_line_length` will track the total used characters if we place
            # words[i..j] on the same line.
            #
            # We build this incrementally:
            #   - Add the next word length
            #   - Add one space before it if it is not the first word on the line
            current_line_length: int = 0

            # Try every possible ending index j for the current line, starting at i.
            for j in range(i, n):
                # If j == i, this is the first word on the line, so no leading space.
                # Otherwise, we need exactly one space before words[j].
                if j == i:
                    current_line_length = word_lengths[j]
                else:
                    current_line_length += 1 + word_lengths[j]

                # If the line is too long, then any larger j will only make it longer.
                # So we can stop immediately.
                if current_line_length > maxWidth:
                    break

                # If j is the last word, then this is the final line.
                # The problem states the last line has cost 0 as long as it fits.
                if j == n - 1:
                    line_cost: int = 0
                else:
                    # Otherwise, the line cost is the square of unused spaces.
                    unused_spaces: int = maxWidth - current_line_length
                    line_cost = unused_spaces * unused_spaces

                # Total cost for choosing words[i..j] on this line:
                #   cost of this line + best cost for remaining words
                total_cost: int = line_cost + dp[j + 1]

                # Keep the minimum over all valid choices of j.
                if total_cost < best_cost:
                    best_cost = total_cost

            # After trying all valid line endings from i, store the best answer.
            dp[i] = best_cost

        # The answer for the full transcript starts at word index 0.
        return dp[0]

    def minCost(self, words: List[str], maxWidth: int) -> int:
        """
        Wrapper method matching a shorter common interview-style naming pattern.

        Args:
            words: List of transcript words in original order.
            maxWidth: Maximum allowed width of each subtitle line.

        Returns:
            The minimum possible total editing cost.

        Time complexity:
            O(n^2), where n is the number of words.

        Space complexity:
            O(n).
        """
        return self.minimum_editing_cost(words, maxWidth)


if __name__ == "__main__":
    solution = Solution()

    # Sample input 1 from the prompt.
    words1: List[str] = ["AI", "helps", "teams", "ship", "faster"]
    max_width1: int = 10
    result1: int = solution.minimum_editing_cost(words1, max_width1)
    print("Example 1 result:", result1)

    # Sample input 2 from the prompt.
    words2: List[str] = ["code", "review", "needs", "clear", "notes"]
    max_width2: int = 12
    result2: int = solution.minimum_editing_cost(words2, max_width2)
    print("Example 2 result:", result2)

    # Additional quick sanity checks.
    words3: List[str] = ["single"]
    max_width3: int = 10
    result3: int = solution.minimum_editing_cost(words3, max_width3)
    print("Single-line result:", result3)

    words4: List[str] = ["a", "bb", "ccc", "dd"]
    max_width4: int = 6
    result4: int = solution.minimum_editing_cost(words4, max_width4)
    print("Additional test result:", result4)