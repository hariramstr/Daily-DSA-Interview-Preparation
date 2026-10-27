"""
Title: Pair Gondolas for Minimum Height Imbalance

Problem Description:
A mountain resort is loading visitors into 2-person gondolas. Each gondola must contain
exactly two people, and the resort wants the ride experience to feel as balanced as
possible. You are given an integer array heights where heights[i] is the height of the
i-th visitor. The number of visitors is always even.

Your task is to pair up all visitors so that every visitor is used exactly once, and the
maximum height difference inside any single gondola is as small as possible. Return that
minimum possible maximum difference.

For example, if one pairing creates gondolas with height differences [1, 4, 2], then the
quality of that pairing is 4 because the worst gondola has a difference of 4. Among all
valid pairings, you must minimize this worst-case value.

A common interview insight is that after sorting the array, the best strategy is not
arbitrary matching. The challenge is to reason about which visitors should be paired
together so the largest gap is minimized globally.

Constraints:
- 2 <= heights.length <= 200000
- heights.length is even
- 1 <= heights[i] <= 1000000000

Example 1:
Input: heights = [150, 160, 155, 168]
Output: 8
Explanation: Sort the heights to [150, 155, 160, 168].
If we pair adjacent visitors, we get differences 5 and 8, so the maximum is 8.
Other pairings such as (150, 168) and (155, 160) give differences 18 and 5, so the
maximum becomes 18. Thus the minimum possible maximum difference is 8.

Example 2:
Input: heights = [172, 180, 169, 171, 175, 178]
Output: 3
Explanation: After sorting we get [169, 171, 172, 175, 178, 180].
Pairing adjacent visitors gives (169,171), (172,175), and (178,180), with differences
2, 3, and 2. The maximum difference is 3. No other complete pairing can make the worst
gondola have a smaller difference, so the answer is 3.
"""

from typing import List


class Solution:
    def min_max_height_difference(self, heights: List[int]) -> int:
        """
        Compute the minimum possible value of the maximum height difference among all
        2-person gondolas.

        The key idea is:
        1. Sort the heights.
        2. Pair adjacent visitors in the sorted order.
        3. The answer is the largest difference among those adjacent pairs.

        Why this works:
        In a sorted array, pairing people who are next to each other avoids creating
        unnecessarily large gaps. Any crossing or long-distance pairing can only make
        some pair difference larger, so adjacent pairing minimizes the worst-case gap.

        Args:
            heights: A list of visitor heights. The length is guaranteed to be even.

        Returns:
            The minimum possible maximum difference within any gondola.

        Time complexity:
            O(n log n), due to sorting.

        Space complexity:
            O(n), because sorted() creates a new list.
        """
        # Step 1: Sort the heights.
        #
        # Why sort?
        # Sorting puts heights in increasing order, which makes nearby values easy to
        # compare. Once sorted, the best strategy is to pair neighbors:
        #
        #   sorted_heights[0] with sorted_heights[1]
        #   sorted_heights[2] with sorted_heights[3]
        #   ...
        #
        # This is the standard greedy insight for minimizing the maximum pair difference.
        # Intuitively, if we skip over a nearby person and pair with someone farther away,
        # that pair's difference cannot get smaller.
        sorted_heights: List[int] = sorted(heights)

        # Step 2: Track the largest difference among all adjacent pairs.
        #
        # Since every gondola must contain exactly two people, and we are pairing adjacent
        # people after sorting, we examine the array two elements at a time.
        #
        # Example:
        #   [150, 155, 160, 168]
        #   pairs -> (150,155), (160,168)
        #   diffs -> 5, 8
        #   answer -> max(5, 8) = 8
        max_difference: int = 0

        # Step 3: Walk through the sorted list in steps of 2.
        #
        # We use index positions 0, 2, 4, ...
        # For each such index i, the pair is:
        #   sorted_heights[i] and sorted_heights[i + 1]
        #
        # Because the number of visitors is guaranteed to be even, i + 1 is always valid.
        for i in range(0, len(sorted_heights), 2):
            # Compute the difference inside the current gondola.
            current_difference: int = sorted_heights[i + 1] - sorted_heights[i]

            # Update the running maximum.
            #
            # We care about the "worst" gondola in the chosen pairing, so we keep the
            # largest difference seen so far.
            if current_difference > max_difference:
                max_difference = current_difference

        # Step 4: Return the smallest possible worst-case difference.
        #
        # Because adjacent pairing is optimal after sorting, this maximum difference is
        # exactly the minimum possible answer.
        return max_difference

    def solve(self, heights: List[int]) -> int:
        """
        Wrapper method that calls the main algorithm.

        This method exists to provide a simple, interview-friendly entry point.

        Args:
            heights: A list of visitor heights.

        Returns:
            The minimum possible maximum height difference.

        Time complexity:
            O(n log n), due to sorting in the main method.

        Space complexity:
            O(n), due to the sorted copy of the list.
        """
        return self.min_max_height_difference(heights)


if __name__ == "__main__":
    solution = Solution()

    # Sample input 1 from the problem statement.
    heights1: List[int] = [150, 160, 155, 168]
    result1: int = solution.solve(heights1)
    print("Input:", heights1)
    print("Output:", result1)
    print("Expected:", 8)
    print()

    # Manual trace for Example 1:
    # Sorted: [150, 155, 160, 168]
    # Pairs: (150,155) -> 5, (160,168) -> 8
    # Maximum difference = 8
    #
    # This matches the expected output.

    # Sample input 2 from the problem statement.
    heights2: List[int] = [172, 180, 169, 171, 175, 178]
    result2: int = solution.solve(heights2)
    print("Input:", heights2)
    print("Output:", result2)
    print("Expected:", 3)
    print()

    # Manual trace for Example 2:
    # Sorted: [169, 171, 172, 175, 178, 180]
    # Pairs: (169,171) -> 2, (172,175) -> 3, (178,180) -> 2
    # Maximum difference = 3
    #
    # This matches the expected output.

    # Additional quick sanity checks.
    extra_tests: List[List[int]] = [
        [1, 2],
        [1, 10, 2, 9],
        [5, 5, 5, 5],
        [1, 3, 4, 9, 10, 11],
    ]

    for test in extra_tests:
        print("Input:", test)
        print("Output:", solution.solve(test))
        print()