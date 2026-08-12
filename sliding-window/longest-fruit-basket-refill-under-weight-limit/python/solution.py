"""
Title: Longest Fruit Basket Refill Under Weight Limit

Problem Description:
A grocery store packs fruits onto a conveyor belt in a fixed order. The weight of each
fruit is given in an integer array `weights`, where `weights[i]` is the weight of the
`i`th fruit. A worker wants to refill a basket using one contiguous group of fruits
from the belt. The basket can hold at most `maxWeight` total weight.

Your task is to return the length of the longest contiguous subarray whose sum is less
than or equal to `maxWeight`.

Because the fruits must be taken in order and without skipping, this is a contiguous
window problem. If multiple windows have the same maximum length, you only need to
return the length, not the actual window.

You may assume all fruit weights are positive integers, which makes it possible to grow
and shrink a sliding window efficiently.

Constraints:
- 1 <= weights.length <= 100000
- 1 <= weights[i] <= 10000
- 1 <= maxWeight <= 1000000000

Example 1:
Input: weights = [2, 1, 3, 2, 1], maxWeight = 5
Output: 2

Example 2:
Input: weights = [1, 1, 1, 1, 2], maxWeight = 4
Output: 4
"""

from typing import List


class Solution:
    def longest_fruit_basket(self, weights: List[int], maxWeight: int) -> int:
        """
        Find the length of the longest contiguous subarray whose sum is
        less than or equal to maxWeight.

        Args:
            weights: A list of positive integers representing fruit weights.
            maxWeight: The maximum total weight the basket can hold.

        Returns:
            The maximum length of a contiguous subarray with sum <= maxWeight.

        Time complexity:
            O(n), where n is the length of weights, because each element is added
            to the window once and removed from the window at most once.

        Space complexity:
            O(1), because only a few variables are used regardless of input size.
        """
        # `left` marks the starting index of the current sliding window.
        # We will expand the window by moving `right` from left to right.
        left: int = 0

        # `current_sum` stores the total weight of the fruits currently inside
        # the window [left, right].
        current_sum: int = 0

        # `best_length` stores the maximum valid window length found so far.
        best_length: int = 0

        # We iterate `right` through every index in the array.
        # At each step, we include weights[right] into the current window.
        for right in range(len(weights)):
            # Expand the window by adding the new fruit at index `right`.
            current_sum += weights[right]

            # If the total weight is too large, the current window is invalid.
            # Because all weights are positive integers, the only way to make
            # the sum smaller is to remove items from the left side.
            #
            # This positivity is exactly why sliding window works efficiently here:
            # - Expanding the window always increases or keeps the sum larger
            # - Shrinking the window always decreases the sum
            #
            # So once the sum exceeds maxWeight, we repeatedly move `left`
            # forward until the window becomes valid again.
            while current_sum > maxWeight:
                current_sum -= weights[left]
                left += 1

            # At this point, the window [left, right] is guaranteed to be valid,
            # meaning its sum is <= maxWeight.
            #
            # We compute its length:
            # length = ending index - starting index + 1
            current_length: int = right - left + 1

            # If this valid window is longer than any previously seen valid window,
            # update our answer.
            if current_length > best_length:
                best_length = current_length

        # After checking all possible windows in one pass, return the best length found.
        return best_length


if __name__ == "__main__":
    solution = Solution()

    # Example 1
    weights1: List[int] = [2, 1, 3, 2, 1]
    max_weight1: int = 5
    result1: int = solution.longest_fruit_basket(weights1, max_weight1)
    print(f"Example 1 result: {result1}")  # Expected: 2

    # Example 2
    weights2: List[int] = [1, 1, 1, 1, 2]
    max_weight2: int = 4
    result2: int = solution.longest_fruit_basket(weights2, max_weight2)
    print(f"Example 2 result: {result2}")  # Expected: 4

    # Additional simple test
    weights3: List[int] = [5]
    max_weight3: int = 5
    result3: int = solution.longest_fruit_basket(weights3, max_weight3)
    print(f"Additional test result: {result3}")  # Expected: 1