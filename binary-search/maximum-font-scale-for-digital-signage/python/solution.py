"""
Title: Maximum Font Scale for Digital Signage

Problem Description:
A mall uses a digital signage system to display a single message on one line.
For each candidate font scale s, the width of a character c is given by
widths[c][s], where widths are nondecreasing as the scale increases.

The display panel has a fixed pixel width W, and the entire message must fit
on the screen at the same scale.

You are given:
- a string message consisting of lowercase English letters,
- an integer W,
- an array scales of available font scales sorted in strictly increasing order,
- a 2D integer array charWidths where charWidths[i][j] is the width of
  character ('a' + i) at scales[j].

Return the largest scale value from scales such that the total rendered width
of message is at most W. If the message does not fit even at the smallest
scale, return -1.

The key observation is monotonicity:
- If the message fits at some scale, it also fits at every smaller scale.
- If the message does not fit at some scale, it will not fit at any larger scale.

This makes binary search the correct and efficient approach.
"""

from typing import List


class Solution:
    def _build_frequency(self, message: str) -> List[int]:
        """
        Build a frequency table for lowercase English letters in the message.

        Instead of re-reading every character of the message for every scale check,
        we count how many times each letter appears once. Then, for any candidate
        scale, the total width can be computed as:

            sum(freq[letter] * width_of_letter_at_that_scale)

        This is much faster when the message is long.

        Args:
            message: The text that must fit on the display.

        Returns:
            A list of length 26 where index 0 stores the count of 'a',
            index 1 stores the count of 'b', ..., index 25 stores the count of 'z'.

        Time complexity:
            O(len(message))

        Space complexity:
            O(1)
            The returned array always has fixed size 26.
        """
        freq: List[int] = [0] * 26

        # Count each character in the message.
        # Because the problem guarantees lowercase English letters,
        # converting a character to an index is safe:
        # 'a' -> 0, 'b' -> 1, ..., 'z' -> 25.
        for ch in message:
            freq[ord(ch) - ord('a')] += 1

        return freq

    def _fits(self, freq: List[int], width_limit: int, scale_index: int, char_widths: List[List[int]]) -> bool:
        """
        Check whether the message fits within the display width at one scale index.

        We use the precomputed frequency table so that we only iterate over the
        26 letters, not over the entire message. This is especially important
        because the message length can be as large as 100,000.

        Args:
            freq: Frequency of each lowercase letter in the message.
            width_limit: Maximum allowed total width W.
            scale_index: Index into the scales array / charWidths columns.
            char_widths: char_widths[i][j] gives width of letter i at scale j.

        Returns:
            True if the total rendered width is at most width_limit, else False.

        Time complexity:
            O(26), which is O(1) in practice

        Space complexity:
            O(1)
        """
        total_width: int = 0

        # Compute the total width contributed by each letter.
        # We also stop early if the running total already exceeds the limit.
        # Early stopping is a useful optimization because once we exceed W,
        # the exact final total no longer matters for the binary search decision.
        for letter_index in range(26):
            count = freq[letter_index]

            # If this letter does not appear in the message, it contributes nothing.
            if count == 0:
                continue

            total_width += count * char_widths[letter_index][scale_index]

            # Early exit: the message already does not fit.
            if total_width > width_limit:
                return False

        # If we finish the loop without exceeding the limit, the message fits.
        return True

    def maximum_font_scale(
        self,
        message: str,
        W: int,
        scales: List[int],
        charWidths: List[List[int]],
    ) -> int:
        """
        Return the largest scale value such that the message fits within width W.

        The algorithm works in two main phases:
        1. Precompute character frequencies for the message.
        2. Use binary search over the sorted scales array.

        Why binary search works:
        - For any scale index j, define "works(j)" as whether the message fits.
        - Because character widths are nondecreasing with scale, total message
          width is also nondecreasing with scale.
        - Therefore, works(j) is monotonic:
            True, True, True, ..., False, False, False
          or possibly all False / all True.
        - Binary search can find the last True efficiently.

        Args:
            message: The message to render.
            W: Maximum allowed total width.
            scales: Sorted available scale values in strictly increasing order.
            charWidths: 26 x len(scales) width table.

        Returns:
            The largest scale value that fits, or -1 if none fit.

        Time complexity:
            O(len(message) + 26 * log(len(scales)))
            Since 26 is constant, this is effectively:
            O(len(message) + log(len(scales)))

        Space complexity:
            O(1) auxiliary space beyond the 26-element frequency array
        """
        # Step 1: Count how many times each letter appears in the message.
        # This lets us evaluate any scale in constant time with respect to
        # the alphabet size, instead of linear time in message length.
        freq: List[int] = self._build_frequency(message)

        # Step 2: Quick rejection.
        # If the message does not fit even at the smallest scale, then there is
        # no valid answer at all, so we return -1 immediately.
        if not self._fits(freq, W, 0, charWidths):
            return -1

        # Step 3: Binary search for the largest valid scale index.
        #
        # We know index 0 fits from the check above.
        # We want the rightmost index such that _fits(...) is True.
        #
        # Search space:
        #   left  = smallest candidate index that may still be the answer
        #   right = largest candidate index that may still be the answer
        left: int = 0
        right: int = len(scales) - 1
        best_index: int = 0

        while left <= right:
            # Standard midpoint calculation.
            mid: int = left + (right - left) // 2

            # Check whether the message fits at this scale.
            if self._fits(freq, W, mid, charWidths):
                # This scale works, so it is a valid candidate answer.
                # Because we want the maximum scale, we record it and then
                # continue searching to the right for a possibly larger one.
                best_index = mid
                left = mid + 1
            else:
                # This scale does not work, so any larger scale also cannot work
                # due to monotonicity. Therefore, we discard the right half.
                right = mid - 1

        # Convert the best valid index back to the actual scale value.
        return scales[best_index]


if __name__ == "__main__":
    solution = Solution()

    # Example 1
    # message = "cab"
    # W = 10
    # scales = [1, 2, 3]
    # Relevant widths:
    # 'a' -> [1, 2, 4]
    # 'b' -> [2, 3, 5]
    # 'c' -> [3, 4, 6]
    #
    # Totals:
    # scale 1: 3 + 1 + 2 = 6  -> fits
    # scale 2: 4 + 2 + 3 = 9  -> fits
    # scale 3: 6 + 4 + 5 = 15 -> does not fit
    # Expected answer: 2
    scales1: List[int] = [1, 2, 3]
    char_widths1: List[List[int]] = [[1, 1, 1] for _ in range(26)]
    char_widths1[0] = [1, 2, 4]  # a
    char_widths1[1] = [2, 3, 5]  # b
    char_widths1[2] = [3, 4, 6]  # c

    result1 = solution.maximum_font_scale(
        message="cab",
        W=10,
        scales=scales1,
        charWidths=char_widths1,
    )
    print(result1)  # Expected: 2

    # Example 2
    # message = "zzzz"
    # W = 7
    # scales = [2, 4]
    # 'z' -> [2, 3]
    #
    # At scale 2, total width = 4 * 2 = 8, which already exceeds 7.
    # Therefore, no scale works.
    # Expected answer: -1
    scales2: List[int] = [2, 4]
    char_widths2: List[List[int]] = [[1, 1] for _ in range(26)]
    char_widths2[25] = [2, 3]  # z

    result2 = solution.maximum_font_scale(
        message="zzzz",
        W=7,
        scales=scales2,
        charWidths=char_widths2,
    )
    print(result2)  # Expected: -1