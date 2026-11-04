from typing import List


"""
Title: Maximum Gain from Reversing One Checkout Segment

Problem Description:
A retail analytics team records the value of each item scanned at a self-checkout lane
as an integer array `items`, where `items[i]` is the profit contribution of the i-th
scanned item. Positive values increase profit, while negative values represent discounts,
refunds, or promotional losses.

The team is allowed to reverse at most one contiguous segment of the scan order to improve
the total value of a chosen contiguous checkout run.

Return the maximum possible sum of any contiguous subarray after performing at most one
reversal on `items`. You may choose not to reverse anything if that already gives the best
result.

A reversal changes only the order of elements inside one chosen segment; the values
themselves do not change. After the optional reversal, evaluate the best contiguous
subarray in the modified array.

Constraints:
- 1 <= items.length <= 2 * 10^5
- -10^9 <= items[i] <= 10^9
- The answer fits in a signed 64-bit integer.
"""


class Solution:
    def max_subarray_after_one_reverse(self, items: List[int]) -> int:
        """
        Compute the maximum possible contiguous subarray sum after reversing
        at most one contiguous segment.

        Args:
            items: List of integers representing profit contributions.

        Returns:
            The maximum achievable contiguous subarray sum.

        Time complexity:
            O(n)

        Space complexity:
            O(n)
        """
        n: int = len(items)

        # ------------------------------------------------------------
        # Step 1: Handle the "no reversal" case with standard Kadane.
        #
        # This is important because the problem says "at most one reversal".
        # So if reversing does not help, we must still return the best normal
        # maximum subarray sum.
        # ------------------------------------------------------------
        best_no_reverse: int = items[0]
        current: int = items[0]
        for i in range(1, n):
            current = max(items[i], current + items[i])
            best_no_reverse = max(best_no_reverse, current)

        # ------------------------------------------------------------
        # Key idea for the reversal case:
        #
        # Suppose in the final array we choose a best subarray that intersects
        # the reversed segment.
        #
        # Let the reversed segment be [l..r].
        # Let the chosen final subarray correspond to:
        #   left part before reversal segment,
        #   then some middle part that comes from inside [l..r] after reversal,
        #   then right part after reversal segment.
        #
        # After careful transformation, every useful solution can be expressed as:
        #
        #   best suffix ending at l-1
        #   + some subarray sum inside [l..r] but taken in reversed order
        #   + best prefix starting at r+1
        #
        # Because reversing does not change the sum of the chosen middle elements,
        # only their order, the middle contribution is simply the sum of a
        # contiguous subarray inside the original [l..r].
        #
        # The only new power of reversal is that it lets us connect:
        #   a suffix ending just before one boundary
        # with
        #   a prefix starting just after the other boundary
        # while choosing the middle interval appropriately.
        #
        # A very useful equivalent formulation is:
        #
        # We need the best value of:
        #   suffix_best_end_at_i + prefix_best_start_at_j
        # for i < j,
        # where reversal can make these two pieces adjacent in the final array.
        #
        # Additionally, the chosen subarray may use only one side or be fully
        # inside the reversed segment, but those are already covered by the
        # no-reversal Kadane answer.
        #
        # So the main extra gain comes from joining:
        #   a best suffix ending at i
        # and
        #   a best prefix starting at j
        # with i < j.
        #
        # We now precompute:
        #   end_here[i]   = maximum subarray sum ending exactly at i
        #   start_here[i] = maximum subarray sum starting exactly at i
        #
        # Then we need:
        #   max over i < j of end_here[i] + start_here[j]
        #
        # This can be done in O(n) by scanning and maintaining the best
        # end_here seen so far.
        #
        # Why this is correct:
        # - Reversal can bring a block ending at i and a block starting at j
        #   next to each other, as long as i < j.
        # - The reversed middle segment is exactly what flips the order of the
        #   gap between them.
        # - The sum of the chosen left block and right block is unchanged.
        # - Any optimal reversed-answer subarray can be represented this way.
        # ------------------------------------------------------------

        # ------------------------------------------------------------
        # Step 2: Compute maximum subarray sum ending exactly at each index.
        #
        # end_here[i] means:
        # among all subarrays that must end at i, what is the largest sum?
        #
        # Standard recurrence:
        #   end_here[i] = max(items[i], end_here[i-1] + items[i])
        # ------------------------------------------------------------
        end_here: List[int] = [0] * n
        end_here[0] = items[0]
        for i in range(1, n):
            end_here[i] = max(items[i], end_here[i - 1] + items[i])

        # ------------------------------------------------------------
        # Step 3: Compute maximum subarray sum starting exactly at each index.
        #
        # start_here[i] means:
        # among all subarrays that must start at i, what is the largest sum?
        #
        # Reverse-direction Kadane recurrence:
        #   start_here[i] = max(items[i], items[i] + start_here[i+1])
        # ------------------------------------------------------------
        start_here: List[int] = [0] * n
        start_here[n - 1] = items[n - 1]
        for i in range(n - 2, -1, -1):
            start_here[i] = max(items[i], items[i] + start_here[i + 1])

        # ------------------------------------------------------------
        # Step 4: Try all ways to connect a left-ending block and a right-starting
        # block using one reversal.
        #
        # We maintain:
        #   best_left = maximum value of end_here[0..j-1]
        #
        # Then candidate for split at j is:
        #   best_left + start_here[j]
        #
        # This corresponds to choosing some i < j and connecting:
        #   best subarray ending at i
        # with
        #   best subarray starting at j
        #
        # after reversing a suitable segment.
        # ------------------------------------------------------------
        answer: int = best_no_reverse
        best_left: int = end_here[0]

        for j in range(1, n):
            answer = max(answer, best_left + start_here[j])
            best_left = max(best_left, end_here[j])

        return answer


if __name__ == "__main__":
    solution = Solution()

    sample_inputs: List[List[int]] = [
        [4, -10, 3, 5],
        [-2, 8, -1, -3, 7],
        [1],
        [-5, -2, -7],
        [1, 2, 3, 4],
        [3, -1, 2, -1, 4],
    ]

    for items in sample_inputs:
        result = solution.max_subarray_after_one_reverse(items)
        print(f"items = {items}")
        print(f"maximum sum after at most one reversal = {result}")
        print("-" * 60)