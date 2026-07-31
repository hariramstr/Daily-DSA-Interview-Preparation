"""
Title: Maximum Product Score from a Fixed-Length Window

Problem Description:
You are given an integer array nums and an integer k. A contiguous window of length k
is called valid if it contains no zero. The product score of a valid window is the
product of all elements inside that window. Your task is to return the maximum product
score among all valid windows of length exactly k. If no valid window exists, return 0.

This problem is designed for large inputs, so recomputing the product from scratch for
every window will be too slow. You need to process the array efficiently while handling
positive numbers, negative numbers, and zeros. Because negative values can flip the sign
of the product, the maximum answer is not always produced by the window with the largest
absolute values. Windows containing even one zero are invalid and must be skipped entirely.

Return the maximum product as a 64-bit integer. You may assume the final answer fits in
a signed 64-bit range.

Constraints:
- 1 <= nums.length <= 100000
- -10 <= nums[i] <= 10
- 1 <= k <= nums.length
- The maximum valid product fits in a signed 64-bit range

Examples:
1) nums = [2, -3, 4, -1, 5], k = 3
   Windows:
   [2, -3, 4]  -> -24
   [-3, 4, -1] -> 12
   [4, -1, 5]  -> -20
   Answer: 12

2) nums = [0, -2, -3, 4, 0, 5], k = 2
   Windows:
   [0, -2]  -> invalid
   [-2, -3] -> 6
   [-3, 4]  -> -12
   [4, 0]   -> invalid
   [0, 5]   -> invalid
   Answer: 6
"""

from typing import List


class Solution:
    def max_product_score(self, nums: List[int], k: int) -> int:
        """
        Return the maximum product among all valid contiguous windows of length k.

        A window is valid only if it contains no zero. The algorithm uses a sliding
        window and maintains:
        - the count of zeros currently inside the window
        - the exact product of all non-zero values currently inside the window

        This allows us to update the window in O(1) time per step instead of
        recomputing the full product for every window.

        Args:
            nums: List of integers.
            k: Exact window length.

        Returns:
            The maximum product among all valid windows of length k, or 0 if no
            valid window exists.

        Time complexity:
            O(n), where n is len(nums), because each element enters and leaves the
            sliding window exactly once.

        Space complexity:
            O(1), because only a few variables are used regardless of input size.
        """
        n: int = len(nums)

        # This variable stores the product of all NON-ZERO elements currently
        # inside the sliding window.
        #
        # Important detail:
        # - If the window contains zeros, then the window is invalid anyway.
        # - We still keep the product of non-zero elements so that when zeros
        #   leave the window, we can immediately know the product of the now-valid
        #   window without rebuilding it from scratch.
        product: int = 1

        # Number of zeros currently inside the window.
        # A window is valid exactly when zero_count == 0.
        zero_count: int = 0

        # This will store the best valid product found so far.
        # We use None initially to mean "no valid window has been seen yet".
        best: int | None = None

        # We expand the window one element at a time using 'right'.
        for right in range(n):
            incoming: int = nums[right]

            # Step 1: Add the new element on the right side of the window.
            #
            # If the incoming value is zero:
            # - the window becomes invalid (or remains invalid)
            # - we increase zero_count
            # - we do NOT multiply product by zero, because product is intended
            #   to track only non-zero values
            #
            # If the incoming value is non-zero:
            # - multiply it into the running non-zero product
            if incoming == 0:
                zero_count += 1
            else:
                product *= incoming

            # Step 2: If the window size exceeds k, remove the leftmost element.
            #
            # Current window is nums[left..right], where left is implicitly
            # right - k + 1 after adjustment.
            if right >= k:
                outgoing: int = nums[right - k]

                # Removing the outgoing element must mirror how we added it.
                #
                # If it was zero:
                # - decrease zero_count
                # - product was never multiplied by zero, so nothing to divide
                #
                # If it was non-zero:
                # - divide it out of the running product
                #
                # Exact division is safe here because:
                # - product always contains the multiplication of all current
                #   non-zero elements in the window
                # - outgoing is guaranteed to be one of those factors
                if outgoing == 0:
                    zero_count -= 1
                else:
                    product //= outgoing

            # Step 3: Once we have formed a full window of size exactly k,
            # evaluate it if it is valid.
            #
            # The first full window ends at index k - 1.
            if right >= k - 1:
                # A valid window contains no zero.
                if zero_count == 0:
                    # Since there are no zeros in the window, 'product' is
                    # exactly the product of the entire window.
                    if best is None or product > best:
                        best = product

        # If no valid window was ever found, return 0 as required.
        return 0 if best is None else best


if __name__ == "__main__":
    solution = Solution()

    # Example 1
    nums1: List[int] = [2, -3, 4, -1, 5]
    k1: int = 3
    result1: int = solution.max_product_score(nums1, k1)
    print(f"nums = {nums1}, k = {k1} -> {result1}")  # Expected: 12

    # Example 2
    nums2: List[int] = [0, -2, -3, 4, 0, 5]
    k2: int = 2
    result2: int = solution.max_product_score(nums2, k2)
    print(f"nums = {nums2}, k = {k2} -> {result2}")  # Expected: 6

    # Additional quick checks
    nums3: List[int] = [1, 2, 3, 4]
    k3: int = 2
    result3: int = solution.max_product_score(nums3, k3)
    print(f"nums = {nums3}, k = {k3} -> {result3}")  # Expected: 12

    nums4: List[int] = [0, 0, 0]
    k4: int = 1
    result4: int = solution.max_product_score(nums4, k4)
    print(f"nums = {nums4}, k = {k4} -> {result4}")  # Expected: 0