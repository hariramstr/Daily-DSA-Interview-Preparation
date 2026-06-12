"""
Title: Longest Even-Parity Access Window

Problem Description:
A security system records a sequence of access events. Each event is labeled with an
integer from 0 to 19 representing one of 20 badge categories. You are given an array
events where events[i] is the category of the i-th event.

A contiguous window of events is called balanced if, for every badge category, the
number of times it appears inside that window is even.

Your task is to return the length of the longest balanced contiguous window.

Because there are only 20 possible categories, an efficient solution should take
advantage of bit manipulation. One useful observation is that while scanning the array
from left to right, you can represent the parity (even or odd count) of each category
seen so far as a bitmask of length 20. If the same parity mask appears at two different
positions, then the subarray between those positions has even frequency for every
category.

Return 0 if no non-empty balanced window exists.

Constraints:
- 1 <= events.length <= 200000
- 0 <= events[i] < 20
- Expected time complexity: O(n)
- Expected extra space: O(min(n, 2^20))

Examples:
1) events = [3, 5, 3, 5, 7, 7]
   Output: 6
   Explanation: The full array is balanced because 3, 5, and 7 each appear twice.

2) events = [1, 2, 1, 4, 2, 4, 4]
   Output: 6
   Explanation: The subarray [1, 2, 1, 4, 2, 4] is balanced because 1, 2, and 4 each
   appear twice. The full array is not balanced because 4 appears three times.
"""

from typing import Dict, List


class Solution:
    def longest_balanced_window(self, events: List[int]) -> int:
        """
        Return the length of the longest contiguous subarray in which every category
        appears an even number of times.

        The key idea is to track the parity (even/odd) of counts for all 20 categories
        using a 20-bit integer mask:
        - Bit k = 0 means category k has appeared an even number of times so far.
        - Bit k = 1 means category k has appeared an odd number of times so far.

        If the same mask appears at two different prefix positions, then the subarray
        between those positions has even counts for every category, because the parity
        changes cancel out.

        Args:
            events: List of event categories, where each value is in the range [0, 19].

        Returns:
            The length of the longest non-empty balanced contiguous window.
            Returns 0 if no such non-empty window exists.

        Time complexity:
            O(n), where n is the length of events.

        Space complexity:
            O(min(n, 2^20)), for storing the earliest index of each seen parity mask.
        """
        # This dictionary stores the FIRST index where each parity mask was seen.
        #
        # Why store the first index only?
        # Because for a fixed current index, using the earliest occurrence of the same
        # mask gives the longest possible subarray ending at the current position.
        #
        # The mask represents parity of counts in the prefix up to the current point.
        # We define a "virtual" prefix before the array starts at index -1.
        #
        # At that moment, no events have been seen, so every category count is 0,
        # which is even. Therefore the parity mask is 0.
        #
        # Storing {0: -1} is extremely important:
        # - It allows a balanced subarray starting at index 0 to be counted correctly.
        # Example:
        #   If after processing index 5 the mask becomes 0 again, then the subarray
        #   from 0 to 5 is balanced, and its length is 5 - (-1) = 6.
        first_seen: Dict[int, int] = {0: -1}

        # Current parity mask of the prefix scanned so far.
        #
        # Initially 0 because all category counts are even (all zero).
        mask: int = 0

        # Best answer found so far.
        longest: int = 0

        # Scan through the array once from left to right.
        for index, category in enumerate(events):
            # Toggle the bit corresponding to the current category.
            #
            # Why XOR?
            # Each time we see a category, its count parity flips:
            # - even -> odd
            # - odd  -> even
            #
            # XOR with (1 << category) flips exactly that one bit.
            mask ^= 1 << category

            # If this exact mask has been seen before, then the subarray between the
            # previous occurrence + 1 and the current index is balanced.
            #
            # Why?
            # Let:
            #   prefix_mask_at_prev = M
            #   prefix_mask_at_current = M
            #
            # Then the parity difference between these two prefixes is zero for every
            # category, meaning each category appears an even number of times in the
            # subarray between them.
            if mask in first_seen:
                # Compute the length of the balanced subarray.
                current_length: int = index - first_seen[mask]

                # Update the best answer if this subarray is longer.
                if current_length > longest:
                    longest = current_length
            else:
                # We only store the first time a mask appears.
                #
                # Why not overwrite it later?
                # Because the earliest index gives the longest future subarray when the
                # same mask appears again.
                first_seen[mask] = index

        # If no non-empty balanced subarray exists, longest remains 0, which matches
        # the required output.
        return longest


if __name__ == "__main__":
    solution = Solution()

    # Example 1:
    # events = [3, 5, 3, 5, 7, 7]
    # Counts in the full array:
    # - 3 appears 2 times
    # - 5 appears 2 times
    # - 7 appears 2 times
    # All are even, so the answer should be 6.
    events1: List[int] = [3, 5, 3, 5, 7, 7]
    result1: int = solution.longest_balanced_window(events1)
    print("Example 1:")
    print("Input:", events1)
    print("Output:", result1)
    print("Expected:", 6)
    print()

    # Example 2:
    # events = [1, 2, 1, 4, 2, 4, 4]
    # The subarray [1, 2, 1, 4, 2, 4] has:
    # - 1 appears 2 times
    # - 2 appears 2 times
    # - 4 appears 2 times
    # So it is balanced and has length 6.
    # The full array is not balanced because 4 appears 3 times.
    events2: List[int] = [1, 2, 1, 4, 2, 4, 4]
    result2: int = solution.longest_balanced_window(events2)
    print("Example 2:")
    print("Input:", events2)
    print("Output:", result2)
    print("Expected:", 6)
    print()

    # Additional quick sanity checks for beginners:
    extra_tests: List[List[int]] = [
        [0],            # No non-empty balanced subarray -> 0
        [0, 0],         # Entire array balanced -> 2
        [1, 2, 3],      # No repeated parity mask except initial? No non-empty balanced -> 0
        [1, 1, 2, 2],   # Entire array balanced -> 4
        [4, 4, 4, 4],   # Entire array balanced -> 4
    ]

    print("Additional Tests:")
    for test in extra_tests:
        print(f"Input: {test} -> Output: {solution.longest_balanced_window(test)}")