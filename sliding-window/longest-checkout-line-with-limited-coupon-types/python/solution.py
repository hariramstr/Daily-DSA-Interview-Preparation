"""
Title: Longest Checkout Line With Limited Coupon Types

Problem Description:
A supermarket records the coupon type used by each customer in the order they join
a checkout line. You are given an integer array coupons where coupons[i] is the
coupon type used by the i-th customer, and an integer k. Your task is to find the
length of the longest contiguous block of customers such that the block contains at
most k distinct coupon types.

This problem models a cashier lane that can efficiently process only a limited
variety of coupon rules at once. A valid block may contain repeated coupon types
any number of times, but the total number of different coupon types appearing
inside the block must not exceed k.

Return the maximum possible length of such a contiguous block. If k is 0, no
customer can be included, so the answer is 0.

Constraints:
- 1 <= coupons.length <= 200000
- 1 <= coupons[i] <= 1000000000
- 0 <= k <= coupons.length

Example 1:
Input: coupons = [4, 2, 2, 5, 5, 2, 4, 4], k = 2
Output: 5
Explanation: The longest valid block is [2, 2, 5, 5, 2], which contains only
coupon types 2 and 5.

Example 2:
Input: coupons = [1, 3, 1, 3, 2, 2, 2, 4], k = 3
Output: 7
Explanation: The block [1, 3, 1, 3, 2, 2, 2] contains exactly 3 distinct coupon
types: 1, 3, and 2. No longer contiguous block satisfies the limit.
"""

from typing import Dict, List


class Solution:
    def length_of_longest_block(self, coupons: List[int], k: int) -> int:
        """
        Find the length of the longest contiguous subarray containing at most
        k distinct coupon types.

        Args:
            coupons: A list of integers where each value represents a coupon type.
            k: The maximum number of distinct coupon types allowed in the block.

        Returns:
            The maximum length of a contiguous block with at most k distinct values.

        Time Complexity:
            O(n), where n is the length of coupons.
            Each element is added to the window once and removed at most once.

        Space Complexity:
            O(k) on average for the frequency map of coupon types currently
            inside the sliding window. In the worst case, this can be O(n)
            if k is large.
        """
        # If k is 0, the problem explicitly says no customer can be included.
        # That means the answer must be 0 regardless of the coupon list.
        if k == 0:
            return 0

        # This dictionary stores how many times each coupon type appears
        # inside the current sliding window.
        #
        # Example:
        # If the current window is [2, 2, 5, 5, 2], then:
        # counts = {2: 3, 5: 2}
        #
        # Why use a dictionary?
        # - We need to quickly update counts when expanding or shrinking the window.
        # - We need to know how many distinct coupon types are currently inside.
        # - Dictionary operations are average O(1), which is ideal for large input.
        counts: Dict[int, int] = {}

        # 'left' is the starting index of the current window.
        # 'right' will move from left to right through the array.
        left: int = 0

        # This stores the best (maximum) valid window length found so far.
        max_length: int = 0

        # We expand the window one customer at a time by moving 'right'.
        for right, coupon_type in enumerate(coupons):
            # Add the current coupon type into the window.
            # If it is not already present, start its count at 0 first.
            counts[coupon_type] = counts.get(coupon_type, 0) + 1

            # At this point, the window is coupons[left:right+1].
            # It may now contain too many distinct coupon types.
            #
            # If the number of distinct keys in 'counts' is greater than k,
            # the window is invalid and must be shrunk from the left side.
            while len(counts) > k:
                # Identify the coupon type that is leaving the window.
                left_coupon_type: int = coupons[left]

                # Decrease its frequency because we are moving 'left' forward.
                counts[left_coupon_type] -= 1

                # If its count becomes 0, that coupon type no longer exists
                # in the current window, so we remove it from the dictionary.
                #
                # This is very important because the number of distinct coupon
                # types is exactly the number of keys in the dictionary.
                if counts[left_coupon_type] == 0:
                    del counts[left_coupon_type]

                # Move the left boundary one step to the right.
                left += 1

            # After the while-loop finishes, the window is guaranteed valid:
            # it contains at most k distinct coupon types.
            #
            # So we can safely compute its length and compare it with the best
            # answer found so far.
            current_length: int = right - left + 1
            if current_length > max_length:
                max_length = current_length

        # After processing all positions, max_length contains the answer.
        return max_length

    def total_fruit(self, coupons: List[int], k: int) -> int:
        """
        Wrapper method that solves the same problem using the main algorithm.

        Args:
            coupons: A list of coupon types.
            k: Maximum number of distinct coupon types allowed.

        Returns:
            The length of the longest valid contiguous block.

        Time Complexity:
            O(n), where n is the length of coupons.

        Space Complexity:
            O(k) on average, due to the frequency dictionary.
        """
        return self.length_of_longest_block(coupons, k)


if __name__ == "__main__":
    solution = Solution()

    # Example 1
    coupons1: List[int] = [4, 2, 2, 5, 5, 2, 4, 4]
    k1: int = 2
    result1: int = solution.length_of_longest_block(coupons1, k1)
    print("Example 1 Result:", result1)  # Expected: 5

    # Example 2
    coupons2: List[int] = [1, 3, 1, 3, 2, 2, 2, 4]
    k2: int = 3
    result2: int = solution.length_of_longest_block(coupons2, k2)
    print("Example 2 Result:", result2)  # Expected: 7

    # Additional edge case: k = 0
    coupons3: List[int] = [1, 2, 3]
    k3: int = 0
    result3: int = solution.length_of_longest_block(coupons3, k3)
    print("Edge Case Result:", result3)  # Expected: 0