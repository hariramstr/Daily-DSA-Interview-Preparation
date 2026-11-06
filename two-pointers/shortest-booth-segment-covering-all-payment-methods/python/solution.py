"""
Title: Shortest Booth Segment Covering All Payment Methods

Problem Description:
At a large outdoor festival, booths are arranged in a single row. Each booth supports
exactly one payment method, represented by a string in the array methods, where
methods[i] is the payment method accepted by the i-th booth. Festival organizers want
to place a help desk so that visitors can walk to a contiguous segment of booths that
includes every distinct payment method used anywhere in the festival at least once.

Your task is to return the length of the shortest contiguous segment of booths that
contains all distinct payment methods present in the entire array. If the array is
empty, return 0.

This is a practical optimization problem: the answer is not asking for all such
segments, only the minimum possible length. The payment methods are case-sensitive,
so "Card" and "card" are different methods.

Constraints:
- 0 <= methods.length <= 100000
- 1 <= methods[i].length <= 20
- methods[i] consists of English letters, digits, underscores, or hyphens
- The expected solution should run in O(n) time using a two-pointers / sliding window approach

Example 1:
Input: methods = ["cash", "card", "voucher", "cash", "qr", "card", "qr"]
Output: 4
Explanation: The distinct payment methods are {"cash", "card", "voucher", "qr"}.
One shortest valid segment is ["voucher", "cash", "qr", "card"], which has length 4.

Example 2:
Input: methods = ["applepay", "applepay", "cash"]
Output: 2
Explanation: The distinct payment methods are {"applepay", "cash"}.
The shortest segment covering both is ["applepay", "cash"].
"""

from typing import Dict, List


class Solution:
    def shortest_segment_covering_all_methods(self, methods: List[str]) -> int:
        """
        Find the length of the shortest contiguous segment that contains every
        distinct payment method present in the full input array.

        Args:
            methods: A list of strings where each string represents the payment
                method accepted by one booth.

        Returns:
            The minimum length of a contiguous subarray that contains all distinct
            payment methods at least once. Returns 0 if the input list is empty.

        Time complexity:
            O(n), where n is the number of booths. Each pointer moves across the
            array at most once.

        Space complexity:
            O(k), where k is the number of distinct payment methods.
        """
        # If there are no booths, there is no segment to consider.
        # The problem explicitly says to return 0 for an empty array.
        if not methods:
            return 0

        # Step 1: Determine how many unique payment methods exist in the entire festival.
        #
        # Why do we need this?
        # Because our sliding window is only valid when it contains ALL distinct methods.
        # So first we must know exactly how many different methods must be covered.
        #
        # Example:
        # methods = ["cash", "card", "voucher", "cash", "qr", "card", "qr"]
        # distinct methods = {"cash", "card", "voucher", "qr"}
        # total distinct count = 4
        total_distinct: int = len(set(methods))

        # Step 2: Prepare the sliding window state.
        #
        # left:
        #   The left boundary of the current window.
        #
        # window_count:
        #   A dictionary counting how many times each payment method appears
        #   inside the current window methods[left:right+1].
        #
        # formed_distinct:
        #   How many distinct payment methods are currently represented in the window.
        #   This increases only when a method count goes from 0 to 1.
        #
        # min_length:
        #   Best (smallest) valid window length found so far.
        left: int = 0
        window_count: Dict[str, int] = {}
        formed_distinct: int = 0
        min_length: int = len(methods) + 1

        # Step 3: Expand the window using the right pointer.
        #
        # We iterate right from 0 to n-1, adding one booth at a time into the window.
        # After each addition, if the window becomes valid (contains all distinct methods),
        # we try to shrink it from the left to make it as short as possible while still valid.
        for right, method in enumerate(methods):
            # Add the current right-side method into the window count.
            #
            # If this method was not already present in the window, its count becomes 1,
            # which means the number of distinct methods represented in the window increases.
            window_count[method] = window_count.get(method, 0) + 1

            if window_count[method] == 1:
                formed_distinct += 1

            # Step 4: If the current window contains all required distinct methods,
            # try to shrink it from the left.
            #
            # Why shrink?
            # Because once a window is valid, any larger version of it is not better
            # than a smaller valid version. So we move left forward as much as possible
            # while preserving validity.
            while formed_distinct == total_distinct:
                # Current window is methods[left:right+1]
                current_length: int = right - left + 1

                # Update the best answer if this valid window is smaller.
                if current_length < min_length:
                    min_length = current_length

                # We now attempt to remove methods[left] from the window
                # and see whether the window remains valid.
                left_method: str = methods[left]
                window_count[left_method] -= 1

                # If the count drops to 0, that method is no longer represented
                # in the window, so the window stops being valid.
                if window_count[left_method] == 0:
                    formed_distinct -= 1

                # Move the left boundary rightward to continue shrinking.
                left += 1

        # Since the array is non-empty, and the full array always contains all distinct methods,
        # we are guaranteed to find at least one valid window. Still, returning 0 as a safe
        # fallback keeps the method robust.
        return min_length if min_length != len(methods) + 1 else 0


if __name__ == "__main__":
    solution = Solution()

    sample_methods_1: List[str] = ["cash", "card", "voucher", "cash", "qr", "card", "qr"]
    result_1: int = solution.shortest_segment_covering_all_methods(sample_methods_1)
    print("Sample Input 1:", sample_methods_1)
    print("Output 1:", result_1)
    print("Expected 1: 4")
    print()

    sample_methods_2: List[str] = ["applepay", "applepay", "cash"]
    result_2: int = solution.shortest_segment_covering_all_methods(sample_methods_2)
    print("Sample Input 2:", sample_methods_2)
    print("Output 2:", result_2)
    print("Expected 2: 2")
    print()

    sample_methods_3: List[str] = []
    result_3: int = solution.shortest_segment_covering_all_methods(sample_methods_3)
    print("Sample Input 3:", sample_methods_3)
    print("Output 3:", result_3)
    print("Expected 3: 0")
    print()

    sample_methods_4: List[str] = ["Card", "card", "Card"]
    result_4: int = solution.shortest_segment_covering_all_methods(sample_methods_4)
    print("Sample Input 4:", sample_methods_4)
    print("Output 4:", result_4)
    print("Expected 4: 2")