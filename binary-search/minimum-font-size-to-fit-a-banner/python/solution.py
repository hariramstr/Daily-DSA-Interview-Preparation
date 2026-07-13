"""
Title: Minimum Font Size to Fit a Banner

Problem Description:
You are building a layout engine for digital signage. A banner must display a fixed
message on a screen with width W and height H. The message is split into words, and
words must remain in order. For a chosen integer font size s, each character occupies
exactly s units of width and each line occupies exactly s units of height.

A word with length L therefore needs L * s width. Adjacent words on the same line must
have exactly one space between them, and a space also consumes s width. You may wrap to
the next line only between words; a word cannot be split across lines.

Return the minimum integer font size s such that the full message can be displayed
inside the banner using at most H total height and at most W width per line.
If no positive font size can fit the message, return -1.

This is a decision-search problem: for a candidate font size, determine whether the
message can be laid out within the banner. The feasibility is monotonic, which allows
an efficient binary search over the answer space.
"""

from typing import List


class Solution:
    def can_fit(self, words: List[str], w: int, h: int, size: int) -> bool:
        """
        Check whether all words can be displayed inside the banner using the given font size.

        The layout rules are:
        - Each character uses `size` width.
        - Each space between adjacent words on the same line also uses `size` width.
        - Each line uses `size` height.
        - Words must stay in order.
        - A word cannot be split across lines.

        Args:
            words: List of words in the message, in fixed order.
            w: Maximum width of the banner.
            h: Maximum height of the banner.
            size: Candidate integer font size.

        Returns:
            True if the message can be laid out within the banner at this font size,
            otherwise False.

        Time complexity:
            O(n), where n is the number of words.

        Space complexity:
            O(1), excluding the input storage.
        """
        # First, determine how many full lines can exist vertically.
        # Since each line consumes exactly `size` height, the number of lines available is:
        #   max_lines = h // size
        #
        # If this is 0, then even a single line cannot fit vertically, so the answer is
        # immediately False.
        max_lines: int = h // size
        if max_lines == 0:
            return False

        # We will greedily place words from left to right.
        #
        # Why greedy works:
        # - For a fixed font size, if we always place the next word on the current line
        #   whenever it fits, we minimize the number of lines used.
        # - Any other strategy that wraps earlier cannot use fewer lines.
        #
        # `used_lines` tracks how many lines we have started so far.
        # We start with the first line already in use.
        used_lines: int = 1

        # `current_line_width` stores the width already occupied on the current line.
        current_line_width: int = 0

        for word in words:
            # Compute the width of this word at the current font size.
            word_width: int = len(word) * size

            # If a single word is wider than the entire banner width, then it can never fit,
            # because words are not allowed to split across lines.
            if word_width > w:
                return False

            if current_line_width == 0:
                # The current line is empty, so we place the word directly with no leading space.
                current_line_width = word_width
            else:
                # If the line already has content, then adding another word requires:
                # - one space of width `size`
                # - plus the word's width
                needed_width: int = current_line_width + size + word_width

                if needed_width <= w:
                    # The word fits on the current line, so we extend the line.
                    current_line_width = needed_width
                else:
                    # The word does not fit on the current line, so we must wrap to a new line.
                    used_lines += 1

                    # If we have already exceeded the number of lines available vertically,
                    # then this font size is not feasible.
                    if used_lines > max_lines:
                        return False

                    # Start the new line with this word.
                    current_line_width = word_width

        # If we processed all words without violating width or height constraints,
        # then the layout is feasible.
        return True

    def minimumFontSize(self, words: List[str], W: int, H: int) -> int:
        """
        Return the minimum positive integer font size that allows the full message to fit.

        Important note:
        Under the problem's monotonicity statement, feasibility is monotonic with respect
        to font size, which naturally supports binary search. In actual geometry, larger
        font sizes make fitting harder, so the feasible set is of the form [1..max_fit].
        Therefore, if any positive size fits, the minimum fitting size is always 1.
        The examples confirm this:
        - Example 1: size 1 fits, so the minimum is 1.
        - Example 2: size 1 fits, so the minimum is 1.

        To remain fully correct with the stated "minimum" requirement, we first test size 1.
        - If size 1 does not fit, then no larger size can fit, so return -1.
        - If size 1 fits, then the minimum positive fitting size is 1.

        Args:
            words: List of words in the message.
            W: Banner width.
            H: Banner height.

        Returns:
            The minimum positive integer font size that fits, or -1 if impossible.

        Time complexity:
            O(n), where n is the number of words.

        Space complexity:
            O(1), excluding the input storage.
        """
        # Because font size must be a positive integer, the smallest possible candidate is 1.
        #
        # If size 1 fits:
        #   then it is automatically the minimum positive fitting size.
        #
        # If size 1 does not fit:
        #   then any larger size will only increase word widths and line heights,
        #   so no larger size can fit either.
        if self.can_fit(words, W, H, 1):
            return 1
        return -1


if __name__ == "__main__":
    solution = Solution()

    # Sample 1 from the prompt.
    # Careful verification:
    # - At size 1:
    #   "team sync" uses (4 + 1 + 4) = 9 width <= 20
    #   "works" uses 5 width <= 20
    #   total lines = 2, total height = 2 <= 8
    # So the true minimum fitting size is 1.
    words1: List[str] = ["team", "sync", "works"]
    W1: int = 20
    H1: int = 8
    print(solution.minimumFontSize(words1, W1, H1))  # Correct result: 1

    # Sample 2 from the prompt.
    # At size 1, it clearly fits, so the minimum is 1.
    words2: List[str] = ["a", "bb", "ccc"]
    W2: int = 6
    H2: int = 6
    print(solution.minimumFontSize(words2, W2, H2))  # Correct result: 1

    # Additional sample where nothing fits:
    # Even at size 1, "hello" needs width 5 > 4.
    words3: List[str] = ["hello"]
    W3: int = 4
    H3: int = 10
    print(solution.minimumFontSize(words3, W3, H3))  # Correct result: -1