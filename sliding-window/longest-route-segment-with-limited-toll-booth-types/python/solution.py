"""
Title: Longest Route Segment With Limited Toll Booth Types

Problem Description:
A navigation company records the sequence of toll booths a truck passes during a long
highway trip. Each toll booth is labeled by an integer type representing the toll
operator that manages it. For billing simplification, the company wants to analyze
the longest contiguous segment of the trip that uses at most k distinct toll booth
types.

Given an integer array booths where booths[i] is the type of the i-th toll booth
encountered, and an integer k, return the length of the longest contiguous subarray
containing at most k distinct values.

A segment is contiguous if it consists of consecutive toll booths in the original trip
log. If k is 0, then no toll booth type can be included, so the answer is 0.

The expected solution should be efficient for large inputs, ideally O(n).

Examples:
1.
Input: booths = [4, 7, 4, 4, 9, 7, 9, 9], k = 2
Output: 4

2.
Input: booths = [5, 5, 1, 2, 1, 2, 3], k = 3
Output: 6
"""

from typing import Dict, List


class Solution:
    def longest_segment_with_at_most_k_distinct(self, booths: List[int], k: int) -> int:
        """
        Return the length of the longest contiguous subarray containing at most k distinct values.

        Args:
            booths: List of integers representing toll booth types in trip order.
            k: Maximum number of distinct booth types allowed in the segment.

        Returns:
            The maximum length of a contiguous segment with at most k distinct values.

        Time Complexity:
            O(n), where n is the length of booths, because each element is added to and
            removed from the sliding window at most once.

        Space Complexity:
            O(k) on average for the frequency map of values currently inside the window.
            In the worst case, this can be O(n) if k is large.
        """
        # If k is 0, we are not allowed to include any booth type at all.
        # Therefore, no non-empty subarray can be valid, and the answer must be 0.
        if k == 0:
            return 0

        # This dictionary stores how many times each booth type appears
        # inside the current sliding window.
        #
        # Example:
        # If the current window is [4, 7, 4, 4], then:
        # counts = {4: 3, 7: 1}
        #
        # Why use a dictionary?
        # - We need to quickly update counts as the window expands and shrinks.
        # - We need to know when a booth type completely leaves the window
        #   so we can reduce the number of distinct types.
        counts: Dict[int, int] = {}

        # left marks the beginning of the current window.
        # right will move from left to right through the array.
        left: int = 0

        # best stores the maximum valid window length found so far.
        best: int = 0

        # Iterate over the array with right as the end of the current window.
        for right, booth_type in enumerate(booths):
            # Step 1: Expand the window by including booths[right].
            #
            # We increase the frequency of this booth type in the current window.
            # If it was not present before, it becomes a new distinct type.
            counts[booth_type] = counts.get(booth_type, 0) + 1

            # Step 2: If the window has become invalid (more than k distinct types),
            # shrink it from the left until it becomes valid again.
            #
            # Why a while loop instead of an if?
            # Because removing just one element may not be enough.
            # We must keep shrinking until the number of distinct types is <= k.
            while len(counts) > k:
                # Identify the booth type that is currently at the left edge
                # of the window, because that is the one we are about to remove.
                left_booth_type: int = booths[left]

                # Decrease its count because it is leaving the window.
                counts[left_booth_type] -= 1

                # If its count becomes zero, that means this booth type no longer
                # exists anywhere in the current window.
                #
                # At that moment, we remove it from the dictionary entirely.
                # This is important because len(counts) is how we track the number
                # of distinct booth types currently inside the window.
                if counts[left_booth_type] == 0:
                    del counts[left_booth_type]

                # Move the left edge one step to the right, completing the shrink step.
                left += 1

            # Step 3: At this point, the window [left, right] is guaranteed to be valid.
            # It contains at most k distinct booth types.
            #
            # Compute its length and update the best answer if this window is larger.
            current_length: int = right - left + 1
            if current_length > best:
                best = current_length

        # After scanning the full array, best contains the length of the longest
        # valid contiguous segment.
        return best


if __name__ == "__main__":
    solution = Solution()

    # Example 1:
    # booths = [4, 7, 4, 4, 9, 7, 9, 9], k = 2
    # Expected output: 4
    booths1: List[int] = [4, 7, 4, 4, 9, 7, 9, 9]
    k1: int = 2
    result1: int = solution.longest_segment_with_at_most_k_distinct(booths1, k1)
    print("Example 1 Result:", result1)

    # Example 2:
    # booths = [5, 5, 1, 2, 1, 2, 3], k = 3
    # Expected output: 6
    booths2: List[int] = [5, 5, 1, 2, 1, 2, 3]
    k2: int = 3
    result2: int = solution.longest_segment_with_at_most_k_distinct(booths2, k2)
    print("Example 2 Result:", result2)

    # Additional edge case:
    # If k = 0, answer must be 0.
    booths3: List[int] = [1, 2, 3]
    k3: int = 0
    result3: int = solution.longest_segment_with_at_most_k_distinct(booths3, k3)
    print("Edge Case Result (k=0):", result3)