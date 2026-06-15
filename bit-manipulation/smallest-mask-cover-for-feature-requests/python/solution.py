"""
Title: Smallest Mask Cover for Feature Requests

Problem Description:
A product team tracks each customer request as a non-negative integer bitmask. In a mask,
the i-th bit is 1 if the request needs feature i, and 0 otherwise. Given an array
requests, you must choose exactly one request mask x from the array and measure how many
extra feature bits need to be turned on so that x can cover every request in the array.

A mask y covers a request r if every bit set in r is also set in y. In other words,
r is covered when (r & y) == r. Starting from a chosen x, you are allowed to turn on
additional bits but never turn bits off. The cost of choosing x is the minimum number
of bit positions you must turn on so that the resulting upgraded mask covers all requests.

Return the minimum possible cost over all choices of x.

Equivalently, if OR is the bitwise OR of all values in requests, then for a chosen x
the required cost is the number of 1-bits in (OR ^ x), assuming x is already a subset
of OR, which is always true because x comes from the array.

Constraints:
- 1 <= requests.length <= 100000
- 0 <= requests[i] <= 10^9
- requests contains at least one value

Example 1:
Input: requests = [5, 1, 7]
Output: 0
Explanation:
The bitwise OR of all requests is 7 (111 in binary). Since 7 already appears in the array,
choosing x = 7 requires turning on 0 extra bits.

Example 2:
Input: requests = [10, 12, 8]
Output: 1
Explanation:
The OR of all requests is 14 (1110 in binary). Choosing x = 12 (1100) requires turning on
only bit 1 to reach 14, so the cost is 1. Choosing 10 or 8 would require turning on
1 or 2 extra bits respectively, and the minimum is 1.
"""

from typing import List


class Solution:
    def min_extra_bits_to_cover_all(self, requests: List[int]) -> int:
        """
        Compute the minimum number of extra bits that must be turned on in one chosen
        request mask so that it can cover every request in the array.

        The key observation is:
        - To cover every request, the final upgraded mask must contain every bit that
          appears in any request.
        - Therefore, the final target mask must be the bitwise OR of all requests.
        - If we choose a starting mask x from the array, the number of extra bits needed
          is exactly the number of 1-bits present in (global_or ^ x). Because x comes
          from the array, every 1-bit in x is also present in global_or, so this XOR
          effectively counts the bits that are missing from x.

        Args:
            requests: A list of non-negative integer bitmasks.

        Returns:
            The minimum possible number of extra bits that need to be turned on.

        Time complexity:
            O(n), where n is the number of requests.
            We make one pass to compute the global OR and one pass to find the minimum cost.

        Space complexity:
            O(1), excluding the input list.
        """
        # ------------------------------------------------------------
        # Step 1: Compute the bitwise OR of all request masks.
        #
        # Why?
        # The OR accumulates every feature bit that appears in at least one request.
        # Any mask that covers all requests must contain all of these bits.
        #
        # Example:
        # requests = [5, 1, 7]
        # 5 = 101
        # 1 = 001
        # 7 = 111
        # OR = 111 = 7
        #
        # That means the final upgraded mask must be 7, because that is the smallest
        # mask containing every required bit from the entire array.
        # ------------------------------------------------------------
        global_or: int = 0
        for mask in requests:
            global_or |= mask

        # ------------------------------------------------------------
        # Step 2: Try each request mask as the starting choice.
        #
        # For a chosen mask x:
        # - We are allowed to turn on bits.
        # - We are not allowed to turn off bits.
        # - Since x is one of the requests, every bit set in x must also be set in
        #   global_or. So x is always a subset of global_or.
        #
        # Therefore, the exact number of missing bits is the number of 1-bits in:
        #     global_or ^ x
        #
        # Because x is a subset of global_or, XOR here is equivalent to identifying
        # the bits that are 1 in global_or but 0 in x.
        #
        # We compute this cost for every candidate and keep the minimum.
        # ------------------------------------------------------------
        min_cost: int = float("inf")

        for mask in requests:
            # --------------------------------------------------------
            # Step 2a: Find which bits are missing from this mask.
            #
            # Example 2:
            # global_or = 14 = 1110
            # mask      = 12 = 1100
            # XOR       =  2 = 0010
            #
            # This means only one bit is missing, so cost = 1.
            # --------------------------------------------------------
            missing_bits_mask: int = global_or ^ mask

            # --------------------------------------------------------
            # Step 2b: Count how many missing bits there are.
            #
            # Python's int.bit_count() returns the number of 1-bits in
            # the integer's binary representation.
            #
            # This is exactly the number of extra feature bits we must
            # turn on to upgrade the chosen mask into global_or.
            # --------------------------------------------------------
            cost: int = missing_bits_mask.bit_count()

            # --------------------------------------------------------
            # Step 2c: Update the best answer seen so far.
            #
            # We want the minimum cost over all possible starting masks.
            # --------------------------------------------------------
            if cost < min_cost:
                min_cost = cost

        # ------------------------------------------------------------
        # Step 3: Return the minimum cost found.
        #
        # Correctness check on the provided examples:
        #
        # Example 1:
        # requests = [5, 1, 7]
        # global_or = 7
        # cost(5) = bit_count(7 ^ 5) = bit_count(2) = 1
        # cost(1) = bit_count(7 ^ 1) = bit_count(6) = 2
        # cost(7) = bit_count(7 ^ 7) = bit_count(0) = 0
        # answer = 0
        #
        # Example 2:
        # requests = [10, 12, 8]
        # global_or = 14
        # cost(10) = bit_count(14 ^ 10) = bit_count(4) = 1
        # cost(12) = bit_count(14 ^ 12) = bit_count(2) = 1
        # cost(8)  = bit_count(14 ^ 8)  = bit_count(6) = 2
        # answer = 1
        #
        # Both match the problem statement.
        # ------------------------------------------------------------
        return min_cost

    def solve(self, requests: List[int]) -> int:
        """
        Wrapper method that calls the main algorithm.

        Args:
            requests: A list of non-negative integer bitmasks.

        Returns:
            The minimum number of extra bits needed.

        Time complexity:
            O(n), where n is the number of requests.

        Space complexity:
            O(1), excluding the input list.
        """
        return self.min_extra_bits_to_cover_all(requests)


if __name__ == "__main__":
    # ------------------------------------------------------------
    # Beginner-friendly demonstration of the solution.
    #
    # We create sample inputs from the problem statement, run the
    # algorithm, and print the results.
    # ------------------------------------------------------------
    solution = Solution()

    # Example 1
    requests1: List[int] = [5, 1, 7]
    result1: int = solution.solve(requests1)
    print("Example 1:")
    print("Input:", requests1)
    print("Output:", result1)
    print("Expected:", 0)
    print()

    # Example 2
    requests2: List[int] = [10, 12, 8]
    result2: int = solution.solve(requests2)
    print("Example 2:")
    print("Input:", requests2)
    print("Output:", result2)
    print("Expected:", 1)
    print()

    # Additional quick sanity checks
    requests3: List[int] = [0]
    result3: int = solution.solve(requests3)
    print("Additional Test 1:")
    print("Input:", requests3)
    print("Output:", result3)
    print("Expected:", 0)
    print()

    requests4: List[int] = [1, 2, 4]
    result4: int = solution.solve(requests4)
    print("Additional Test 2:")
    print("Input:", requests4)
    print("Output:", result4)
    print("Expected:", 2)