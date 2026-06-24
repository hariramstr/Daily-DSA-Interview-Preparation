"""
Title: Longest Promo Window With Limited Duplicate Coupons

Problem Description:
An e-commerce platform records the coupon code used in each order during a marketing campaign.
The analytics team wants to find the longest contiguous block of orders that is still considered
"diverse enough" for reporting. A block is valid if no coupon code appears more than k times
inside that block.

You are given an array coupons where coupons[i] is the coupon code used in the i-th order,
and an integer k. Return the length of the longest contiguous subarray such that every distinct
coupon code appears at most k times within that subarray.

This is a realistic streaming-style problem: orders arrive in sequence, and you need to maintain
a valid window efficiently as you scan from left to right. A brute-force check of every subarray
will be too slow for large inputs.

Constraints:
- 1 <= coupons.length <= 200000
- 1 <= coupons[i] <= 1000000000
- 1 <= k <= coupons.length
- coupons may contain many repeated values

Example 1:
Input: coupons = [4, 1, 4, 2, 4, 1, 2, 2], k = 2
Output: 5
Explanation: One longest valid window is [1, 4, 2, 4, 1], where coupon 1 appears 2 times,
coupon 4 appears 2 times, and coupon 2 appears 1 time. Any longer window would cause either
coupon 4 or coupon 2 to appear more than 2 times.

Example 2:
Input: coupons = [7, 7, 7, 8, 8, 9, 7], k = 1
Output: 3
Explanation: A valid longest window is [7, 8, 9] or [8, 9, 7]. In any valid window, each
coupon code must be unique because k = 1.

Your task is to compute only the maximum length, not the subarray itself.
"""

from typing import Dict, List


class Solution:
    def max_subarray_length(self, coupons: List[int], k: int) -> int:
        """
        Find the length of the longest contiguous subarray in which every distinct
        coupon code appears at most k times.

        This uses the classic sliding window / two-pointer technique:
        - Expand the right side of the window one element at a time.
        - Track frequencies of coupon codes inside the current window.
        - If adding a coupon makes its count exceed k, shrink the window from the left
          until the window becomes valid again.
        - Record the maximum valid window length seen during the scan.

        Args:
            coupons: List of coupon codes in order of appearance.
            k: Maximum allowed frequency for any single coupon code inside a valid window.

        Returns:
            The maximum length of a contiguous valid subarray.

        Time complexity:
            O(n), where n is the length of coupons.
            Each element is added to the window once and removed from the window once.

        Space complexity:
            O(m), where m is the number of distinct coupon codes currently tracked
            in the frequency dictionary. In the worst case, O(n).
        """
        # This dictionary stores how many times each coupon code appears
        # inside the current sliding window.
        #
        # Example:
        # If the current window is [4, 1, 4, 2], then:
        # counts = {4: 2, 1: 1, 2: 1}
        counts: Dict[int, int] = {}

        # left marks the beginning of the current window.
        # right will move from left to right across the array.
        left: int = 0

        # best stores the maximum valid window length found so far.
        best: int = 0

        # We expand the window by moving right one step at a time.
        for right, coupon in enumerate(coupons):
            # Include the new coupon at position 'right' into the window.
            # We increase its frequency count because it is now part of the window.
            counts[coupon] = counts.get(coupon, 0) + 1

            # At this moment, only the count of 'coupon' could have become invalid,
            # because all previous counts were already valid before we added this one.
            #
            # If counts[coupon] > k, the current window violates the rule:
            # "no coupon code appears more than k times".
            #
            # To fix this, we shrink the window from the left until the count of
            # this coupon is back to at most k.
            while counts[coupon] > k:
                # Identify the coupon leaving the window from the left side.
                left_coupon: int = coupons[left]

                # Decrease its count because it is no longer inside the window.
                counts[left_coupon] -= 1

                # Move the left boundary rightward, effectively shrinking the window.
                left += 1

            # After the while-loop finishes, the window [left, right] is valid again.
            # Every coupon code appears at most k times.
            #
            # Compute the current window length:
            # right - left + 1
            current_length: int = right - left + 1

            # Update the best answer if this valid window is the largest so far.
            if current_length > best:
                best = current_length

        # After scanning the entire array, best contains the answer.
        return best


if __name__ == "__main__":
    solution = Solution()

    # Example 1
    coupons1: List[int] = [4, 1, 4, 2, 4, 1, 2, 2]
    k1: int = 2
    result1: int = solution.max_subarray_length(coupons1, k1)
    print("Example 1 Result:", result1)  # Expected: 5

    # Example 2
    coupons2: List[int] = [7, 7, 7, 8, 8, 9, 7]
    k2: int = 1
    result2: int = solution.max_subarray_length(coupons2, k2)
    print("Example 2 Result:", result2)  # Expected: 3

    # Additional quick checks
    coupons3: List[int] = [1, 2, 3, 4]
    k3: int = 1
    result3: int = solution.max_subarray_length(coupons3, k3)
    print("Additional Check 1:", result3)  # Expected: 4

    coupons4: List[int] = [5, 5, 5, 5]
    k4: int = 2
    result4: int = solution.max_subarray_length(coupons4, k4)
    print("Additional Check 2:", result4)  # Expected: 2