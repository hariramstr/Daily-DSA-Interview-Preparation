"""
Title: Maximum Audience Gain from One Schedule Insertion

Problem Description:
You are given an array `viewers` where `viewers[i]` represents the expected audience
size of the `i`-th show in a streaming platform's daily lineup. The platform may
insert exactly one promotional show into the schedule. The inserted show has an
audience value `promo`, and it can be placed at any position: before the first show,
between any two consecutive shows, or after the last show.

After insertion, the platform evaluates the best contiguous block of shows to feature
on its homepage. The score of a block is the sum of its audience values. Your task is
to return the maximum possible homepage score after inserting the promotional show
exactly once.

In other words, choose one insertion position for `promo`, then compute the maximum
subarray sum of the new array, and maximize that value over all possible insertion
positions.

A valid solution should be more efficient than trying every insertion explicitly.

Constraints:
- 1 <= viewers.length <= 2 * 10^5
- -10^4 <= viewers[i] <= 10^4
- -10^4 <= promo <= 10^4
- The answer fits in a 32-bit signed integer.

Example 1:
Input: viewers = [4, -2, 3, -1], promo = 5
Output: 10
Explanation: Insert `5` between `4` and `-2`, producing [4, 5, -2, 3, -1].
The best contiguous block is [4, 5, -2, 3] with sum 10.

Example 2:
Input: viewers = [-3, -2, -4], promo = 6
Output: 6
Explanation: All original shows have negative audience values, so the best choice is
to insert 6 anywhere and feature only that single show. The maximum possible score is 6.
"""

from typing import List


class Solution:
    def max_audience_gain(self, viewers: List[int], promo: int) -> int:
        """
        Compute the maximum possible maximum-subarray sum after inserting `promo`
        exactly once into any position of the array.

        Args:
            viewers: List of audience values for the original schedule.
            promo: Audience value of the promotional show to insert exactly once.

        Returns:
            The largest possible contiguous-block sum after the insertion.

        Time complexity:
            O(n), where n is the length of `viewers`.

        Space complexity:
            O(n), for prefix/suffix helper arrays.
        """
        n: int = len(viewers)

        # left_best_end[i] will store:
        # "the maximum sum of a contiguous subarray that MUST end at index i"
        #
        # This is the standard Kadane transition for ending-at-i:
        # either:
        #   1) start fresh at viewers[i]
        # or
        #   2) extend the best subarray ending at i-1
        #
        # Why do we need this?
        # If we insert promo between positions i and i+1, then any best block that
        # includes promo can also include:
        #   - some suffix of the left side ending exactly at i
        #   - some prefix of the right side starting exactly at i+1
        #
        # So we specifically need "best ending at i", not just "best anywhere so far".
        left_best_end: List[int] = [0] * n
        left_best_end[0] = viewers[0]

        for i in range(1, n):
            # Either extend the previous ending subarray or start new at current element.
            left_best_end[i] = max(viewers[i], left_best_end[i - 1] + viewers[i])

        # right_best_start[i] will store:
        # "the maximum sum of a contiguous subarray that MUST start at index i"
        #
        # This is the mirror-image Kadane transition:
        # either:
        #   1) start fresh at viewers[i]
        # or
        #   2) extend into the best subarray starting at i+1
        #
        # Why do we need this?
        # For an insertion between i and i+1, if the chosen block includes promo and
        # continues into the right side, it must take a prefix that starts exactly at i+1.
        right_best_start: List[int] = [0] * n
        right_best_start[n - 1] = viewers[n - 1]

        for i in range(n - 2, -1, -1):
            # Either extend the next starting subarray or start new at current element.
            right_best_start[i] = max(viewers[i], viewers[i] + right_best_start[i + 1])

        # original_best will store the maximum subarray sum in the original array.
        #
        # Why do we need this?
        # After insertion, the best subarray in the new array might choose to ignore
        # the inserted promo entirely. Since insertion does not change the relative
        # order or values of original elements, the original best subarray is still
        # available in the new array.
        #
        # Therefore, the final answer must be at least original_best.
        original_best: int = viewers[0]
        current: int = viewers[0]

        for i in range(1, n):
            current = max(viewers[i], current + viewers[i])
            original_best = max(original_best, current)

        # Start with the case where the best block ignores promo.
        answer: int = original_best

        # Now consider all possible insertion positions.
        #
        # There are n + 1 positions:
        #   0: before viewers[0]
        #   1: between viewers[0] and viewers[1]
        #   ...
        #   n-1: between viewers[n-2] and viewers[n-1]
        #   n: after viewers[n-1]
        #
        # For each insertion position, we compute the best subarray that MUST include promo.
        #
        # Key idea:
        # Any contiguous block containing promo looks like:
        #   [optional left part] + [promo] + [optional right part]
        #
        # The optional left part, if taken, must end immediately before promo.
        # The optional right part, if taken, must start immediately after promo.
        #
        # Since taking a negative contribution would only hurt the sum, we only add
        # a side if its best contribution is positive.
        for pos in range(n + 1):
            # Start with the promo alone.
            candidate: int = promo

            # If promo is inserted after index pos-1, then the left adjacent side ends at pos-1.
            # Example:
            #   pos = 0  -> inserted before first element, so there is no left side.
            #   pos = 2  -> inserted between index 1 and 2, so left side can end at 1.
            if pos > 0:
                # Only add the left contribution if it is positive.
                # A non-positive left side would reduce or not improve the total.
                candidate += max(0, left_best_end[pos - 1])

            # If promo is inserted before index pos, then the right adjacent side starts at pos.
            # Example:
            #   pos = n  -> inserted after last element, so there is no right side.
            #   pos = 2  -> inserted between index 1 and 2, so right side can start at 2.
            if pos < n:
                # Only add the right contribution if it is positive.
                candidate += max(0, right_best_start[pos])

            # Update the global answer.
            answer = max(answer, candidate)

        return answer

    def solve(self, viewers: List[int], promo: int) -> int:
        """
        Wrapper method for the main algorithm.

        Args:
            viewers: List of audience values for the original schedule.
            promo: Audience value of the promotional show.

        Returns:
            The maximum possible homepage score after one insertion.

        Time complexity:
            O(n)

        Space complexity:
            O(n)
        """
        return self.max_audience_gain(viewers, promo)


if __name__ == "__main__":
    solution = Solution()

    # Example 1 from the problem statement.
    viewers_1: List[int] = [4, -2, 3, -1]
    promo_1: int = 5
    result_1: int = solution.solve(viewers_1, promo_1)
    print("Example 1 Result:", result_1)  # Expected: 10

    # Quick manual verification for Example 1:
    # Insert 5 between 4 and -2:
    # [4, 5, -2, 3, -1]
    # Best contiguous block is [4, 5, -2, 3] = 10
    #
    # Our formula at position 1 gives:
    # left_best_end[0] = 4
    # right_best_start[1] = max subarray starting at -2 => 1 (from [-2, 3])
    # candidate = 5 + max(0, 4) + max(0, 1) = 10

    # Example 2 from the problem statement.
    viewers_2: List[int] = [-3, -2, -4]
    promo_2: int = 6
    result_2: int = solution.solve(viewers_2, promo_2)
    print("Example 2 Result:", result_2)  # Expected: 6

    # Quick manual verification for Example 2:
    # All original values are negative, so any extension around promo would hurt.
    # Best is promo alone => 6

    # Additional small sanity checks.
    viewers_3: List[int] = [1, 2, 3]
    promo_3: int = 4
    result_3: int = solution.solve(viewers_3, promo_3)
    print("Additional Test 1 Result:", result_3)  # Expected: 10

    viewers_4: List[int] = [5, -100, 6]
    promo_4: int = 7
    result_4: int = solution.solve(viewers_4, promo_4)
    print("Additional Test 2 Result:", result_4)  # Expected: 13