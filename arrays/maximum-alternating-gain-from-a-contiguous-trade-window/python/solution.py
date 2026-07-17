"""
Title: Maximum Alternating Gain from a Contiguous Trade Window

Problem Description:
You are given an integer array profits where profits[i] represents the net profit or loss
of the i-th trade executed during a day. A risk analyst wants to choose exactly one
contiguous window of trades and evaluate it with an alternating sign rule: the first
chosen trade contributes normally, the second is subtracted, the third is added, the
fourth is subtracted, and so on.

For a chosen subarray profits[l..r], its score is:

    profits[l] - profits[l + 1] + profits[l + 2] - profits[l + 3] + ...

Return the maximum possible alternating score over all non-empty contiguous subarrays.

The chosen window must remain contiguous, but you may start and end anywhere in the array.
Values may be positive, zero, or negative. Because subtraction can turn a negative value
into a gain, the best answer is not necessarily the same as the maximum subarray sum.

Constraints:
- 1 <= profits.length <= 200000
- -1000000000 <= profits[i] <= 1000000000
- The answer fits in a signed 64-bit integer.
"""

from typing import List


class Solution:
    def max_alternating_gain(self, profits: List[int]) -> int:
        """
        Compute the maximum alternating score over all non-empty contiguous subarrays.

        We use dynamic programming with two states:
        - plus_end: best alternating score of a subarray ending at the current index
          where the current element is taken with a '+' sign.
        - minus_end: best alternating score of a subarray ending at the current index
          where the current element is taken with a '-' sign.

        Because every valid subarray must start with '+', a '-' state can only be formed
        by extending a previous '+' state.

        Args:
            profits: List of trade profits/losses.

        Returns:
            The maximum alternating score among all non-empty contiguous subarrays.

        Time complexity:
            O(n), where n is the length of profits.

        Space complexity:
            O(1), because we only keep the previous DP states.
        """
        # The array is guaranteed to be non-empty by the constraints.
        # We initialize the DP using the first element.
        #
        # If a subarray ends at index 0, the only possible non-empty subarray is [profits[0]],
        # and its score is simply profits[0]. Since the first element of any chosen subarray
        # always has a '+' sign, this initializes the "plus" state.
        plus_end: int = profits[0]

        # A subarray cannot end at index 0 with the current element having a '-' sign,
        # because a valid subarray must begin with '+'. So this state is impossible at first.
        #
        # We use a very small number to represent "invalid / impossible".
        negative_infinity: int = -10**30
        minus_end: int = negative_infinity

        # The answer must consider every non-empty subarray seen so far.
        # Initially, the best answer is the single-element subarray [profits[0]].
        answer: int = profits[0]

        # Process the array from left to right, updating the best subarray scores
        # that end exactly at the current position.
        for i in range(1, len(profits)):
            value = profits[i]

            # Save previous states before overwriting them.
            previous_plus_end = plus_end
            previous_minus_end = minus_end

            # Compute the new "plus" state:
            #
            # Case 1: Start a brand-new subarray at this index.
            #         Then the score is simply +value.
            #
            # Case 2: Extend a previous subarray that ended with a '-' sign.
            #         If the previous element in the chosen subarray had '-', then the current
            #         one must have '+', so the new score becomes previous_minus_end + value.
            #
            # We take the better of these two choices.
            plus_end = max(value, previous_minus_end + value)

            # Compute the new "minus" state:
            #
            # A current '-' sign is only possible if we extend a previous subarray whose
            # current end had a '+' sign. We cannot start a subarray with '-'.
            #
            # So the only valid transition is:
            #     previous_plus_end - value
            minus_end = previous_plus_end - value

            # Update the global answer.
            #
            # Any valid subarray ending here must be represented by either plus_end or minus_end.
            # We compare both with the best answer seen so far.
            answer = max(answer, plus_end, minus_end)

        return answer


if __name__ == "__main__":
    solution = Solution()

    sample_inputs: List[List[int]] = [
        [4, 2, 5, 3],
        [5, -1, -3, 4],
    ]

    for profits in sample_inputs:
        result = solution.max_alternating_gain(profits)
        print(f"profits = {profits}")
        print(f"maximum alternating gain = {result}")
        print()