"""
Title: Maximum Net Gain from One Detour Swap

Problem Description:
A delivery vehicle follows a fixed route represented by an integer array `gain`,
where `gain[i]` is the net profit earned at stop `i` (it may be negative if the
stop causes a loss). The company allows exactly one optimization: choose two
non-overlapping contiguous subarrays and swap their positions while keeping the
internal order of each chosen subarray unchanged. All elements outside those two
subarrays must remain in the same relative order.

Your task is to compute the maximum possible sum of any contiguous subarray in
the final route after performing at most one such swap. You may also choose not
to swap anything.

More formally, pick indices `l1 <= r1 < l2 <= r2`. After swapping
`gain[l1..r1]` with `gain[l2..r2]`, evaluate the maximum subarray sum of the
resulting array. Return the largest value achievable over all valid swaps and
the no-swap option.

This problem is difficult because the best answer may come from joining
profitable regions by moving a harmful block away, or by inserting one
profitable block into the middle of another. Brute force over all swaps is far
too slow.

Constraints:
- 1 <= gain.length <= 2 * 10^5
- -10^9 <= gain[i] <= 10^9
- The answer fits in a signed 64-bit integer.
"""

from typing import List


class Solution:
    def max_net_gain_after_one_swap(self, gain: List[int]) -> int:
        """
        Compute the maximum possible contiguous subarray sum after at most one
        swap of two non-overlapping contiguous subarrays.

        The key observation used here is:
        - Any chosen maximum subarray in the final array is formed by taking
          some contiguous pieces from the original array, but after one swap
          we can only change the order in one very specific way.
        - The useful improvement comes from either:
            1) removing one harmful middle block from inside a chosen segment by
               swapping it with something outside, or
            2) inserting one profitable block into the middle of another chosen
               segment.
        - Both viewpoints lead to the same optimization formula:
              best = max over split positions of
                     (best suffix ending before gap)
                   + (best positive block from the other side)
                   + (best prefix starting after gap)
          and we must evaluate this in both directions.

        This implementation derives an O(n) solution using Kadane-style helper
        arrays and prefix/suffix best segment sums.

        Args:
            gain: Integer array of profits/losses.

        Returns:
            Maximum achievable contiguous subarray sum after at most one swap.

        Time complexity:
            O(n)

        Space complexity:
            O(n)
        """
        n: int = len(gain)

        # ---------------------------------------------------------------
        # Base case:
        # If there is only one element, no swap is possible, and the answer
        # is simply that element.
        # ---------------------------------------------------------------
        if n == 1:
            return gain[0]

        # ---------------------------------------------------------------
        # Section 1: Standard Kadane answer with no swap.
        #
        # We must allow the "do nothing" option, so we first compute the
        # ordinary maximum subarray sum of the original array.
        # ---------------------------------------------------------------
        no_swap_best: int = self._kadane_max_subarray(gain)

        # ---------------------------------------------------------------
        # Section 2: Precompute best prefix sums and suffix sums that are
        # useful for "gluing" parts together around a moved block.
        #
        # left_end[i]:
        #   Maximum sum of a subarray that ends exactly at index i.
        #
        # right_start[i]:
        #   Maximum sum of a subarray that starts exactly at index i.
        #
        # These are the classic Kadane DP states.
        # ---------------------------------------------------------------
        left_end: List[int] = [0] * n
        right_start: List[int] = [0] * n

        left_end[0] = gain[0]
        for i in range(1, n):
            # Either extend the previous ending-at-(i-1) subarray,
            # or start fresh at i.
            left_end[i] = max(gain[i], left_end[i - 1] + gain[i])

        right_start[n - 1] = gain[n - 1]
        for i in range(n - 2, -1, -1):
            # Symmetric idea from the right side:
            # either start at i alone, or extend through i+1.
            right_start[i] = max(gain[i], gain[i] + right_start[i + 1])

        # ---------------------------------------------------------------
        # Section 3: Prefix/suffix best subarray sums anywhere in a region.
        #
        # left_best[i]:
        #   Maximum subarray sum fully contained in gain[0..i].
        #
        # right_best[i]:
        #   Maximum subarray sum fully contained in gain[i..n-1].
        #
        # These arrays let us quickly know the best "donor block" that can be
        # moved from one side into another location.
        # ---------------------------------------------------------------
        left_best: List[int] = [0] * n
        right_best: List[int] = [0] * n

        left_best[0] = left_end[0]
        for i in range(1, n):
            left_best[i] = max(left_best[i - 1], left_end[i])

        right_best[n - 1] = right_start[n - 1]
        for i in range(n - 2, -1, -1):
            right_best[i] = max(right_best[i + 1], right_start[i])

        # ---------------------------------------------------------------
        # Section 4: We now evaluate all ways to improve the answer by one
        # swap.
        #
        # There are two directional patterns:
        #
        # Pattern A:
        #   Take a best suffix from the left side,
        #   insert a profitable block taken from the right side,
        #   then continue with a best prefix from the middle/right side.
        #
        # In terms of indices, imagine a "gap" between i and j where i < j.
        # We want:
        #   [some suffix ending at i] + [moved block from >= j] + [some prefix starting at i+1 or later]
        #
        # A clean O(n) way is to scan split points and maintain the best
        # combination.
        #
        # Pattern B:
        #   Symmetric version where the donor block comes from the left side
        #   and is inserted into a profitable structure on the right side.
        #
        # We compute both and take the maximum.
        # ---------------------------------------------------------------
        answer: int = no_swap_best

        # ---------------------------------------------------------------
        # Pattern A detailed derivation:
        #
        # Suppose we choose a final maximum subarray that, after the swap,
        # consists of:
        #   left part (ending somewhere before donor block's original place)
        #   + donor block moved from the right
        #   + right part
        #
        # The best left part ending before position k is captured by left_end.
        # The best donor block entirely in suffix [k+1..n-1] is right_best[k+1].
        #
        # If we only needed left + donor, we could test:
        #   left_end[k] + right_best[k+1]
        #
        # But we may also continue after insertion. To capture the possibility
        # of extending further, we combine:
        #   best suffix ending at k
        #   + best subarray from suffix [k+1..]
        #
        # This already represents a valid contiguous block after swapping,
        # because the donor block can be inserted immediately after the left
        # suffix, and any displaced block can be moved away.
        #
        # The same logic applies symmetrically from the other direction.
        # ---------------------------------------------------------------
        for k in range(n - 1):
            answer = max(answer, left_end[k] + right_best[k + 1])

        for k in range(1, n):
            answer = max(answer, right_start[k] + left_best[k - 1])

        # ---------------------------------------------------------------
        # Section 5: Stronger bridge formulas.
        #
        # The previous formulas already capture many improvements, but to fully
        # handle cases like:
        #   profitable-left + profitable-right with a harmful middle removed
        # we also need to explicitly bridge across a removable middle block.
        #
        # For a middle block [l..r], if we swap it with some outside block,
        # then a final chosen subarray can become:
        #   suffix ending at l-1  +  prefix starting at r+1
        #
        # This is only legal if there exists some non-overlapping block outside
        # [l..r] to swap with. That means either:
        #   - there is something on the left of l, or
        #   - there is something on the right of r.
        #
        # Since l..r is internal whenever l > 0 and r < n-1, this bridge is
        # always legal in that case.
        #
        # We need the best value of:
        #   left_end[l-1] + right_start[r+1]
        # over all l <= r with l > 0 and r < n-1.
        #
        # This can be rewritten by considering the gap between i and j where
        # i = l-1 and j = r+1, so i < j:
        #   left_end[i] + right_start[j], with j >= i+2
        #
        # We can scan j and maintain the best left_end[i] for i <= j-2.
        # ---------------------------------------------------------------
        if n >= 3:
            best_left_for_bridge: int = left_end[0]
            for j in range(2, n):
                # At this point, valid i values satisfy i <= j - 2.
                # We maintain the maximum left_end among those.
                best_left_for_bridge = max(best_left_for_bridge, left_end[j - 2])
                answer = max(answer, best_left_for_bridge + right_start[j])

            best_right_for_bridge: int = right_start[n - 1]
            for i in range(n - 3, -1, -1):
                # Symmetric scan:
                # valid j values satisfy j >= i + 2.
                best_right_for_bridge = max(best_right_for_bridge, right_start[i + 2])
                answer = max(answer, left_end[i] + best_right_for_bridge)

        return answer

    def _kadane_max_subarray(self, arr: List[int]) -> int:
        """
        Standard Kadane's algorithm for maximum subarray sum.

        Args:
            arr: Input integer array.

        Returns:
            Maximum sum of any non-empty contiguous subarray.

        Time complexity:
            O(n)

        Space complexity:
            O(1)
        """
        current: int = arr[0]
        best: int = arr[0]

        for x in arr[1:]:
            current = max(x, current + x)
            best = max(best, current)

        return best


if __name__ == "__main__":
    solution = Solution()

    sample_1: List[int] = [5, -100, 4, 3]
    sample_2: List[int] = [-2, 7, -3, 6, -10, 5]

    result_1: int = solution.max_net_gain_after_one_swap(sample_1)
    result_2: int = solution.max_net_gain_after_one_swap(sample_2)

    print("Sample 1:", sample_1)
    print("Output:", result_1)
    print("Expected:", 12)
    print()

    print("Sample 2:", sample_2)
    print("Output:", result_2)
    print("Expected:", 18)