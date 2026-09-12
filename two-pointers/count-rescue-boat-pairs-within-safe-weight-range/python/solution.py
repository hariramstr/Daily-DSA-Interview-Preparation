"""
Title: Count Rescue Boat Pairs Within Safe Weight Range

Problem Description:
You are given an integer array weights where weights[i] is the weight of the i-th
passenger waiting for evacuation. A rescue boat can carry exactly two passengers,
and for safety reasons the combined weight of the pair must be between lowLimit
and highLimit, inclusive.

Your task is to count how many distinct pairs of passengers (i, j) with i < j can
be assigned to the same boat.

Two pairs are considered distinct if they use different passenger indices, even if
the weights are equal. Each passenger may appear in many counted pairs because you
are only asked to count all valid possible pairings, not to build a final
non-overlapping assignment.

Design an algorithm faster than O(n^2). A typical solution sorts the array and uses
a two-pointer strategy combined with range counting.

Constraints:
- 2 <= weights.length <= 2 * 10^5
- 1 <= weights[i] <= 10^9
- 1 <= lowLimit <= highLimit <= 2 * 10^9

Example 1:
Input: weights = [2, 3, 5, 6, 8], lowLimit = 7, highLimit = 10
Output: 5
Explanation: Valid pairs are (2,5), (2,6), (2,8), (3,5), and (3,6). Their sums are
7, 8, 10, 8, and 9.

Example 2:
Input: weights = [1, 1, 4, 4, 7], lowLimit = 5, highLimit = 8
Output: 7
Explanation: Valid pairs are:
- Four pairs formed by choosing one 1 and one 4
- Two pairs formed by choosing one 1 and 7
- One pair formed by the two 4s
Total = 7
"""

from typing import List


class Solution:
    def count_pairs_with_sum_at_most(self, weights: List[int], limit: int) -> int:
        """
        Count how many index pairs have sum <= limit.

        This helper is the core two-pointer routine. After sorting the array,
        we place one pointer at the lightest passenger and one pointer at the
        heaviest passenger.

        If weights[left] + weights[right] <= limit, then:
        - The current left passenger can pair with every passenger from
          left + 1 through right.
        - Why? Because the array is sorted, so every value between those
          positions is <= weights[right], meaning all those sums are also
          <= limit.
        - Therefore, we can add (right - left) pairs in one step.

        Otherwise, if the sum is too large, we must reduce it by moving the
        right pointer leftward.

        Args:
            weights: Sorted list of passenger weights.
            limit: Maximum allowed pair sum for this helper count.

        Returns:
            The number of pairs (i, j) with i < j and weights[i] + weights[j] <= limit.

        Time complexity:
            O(n), where n is the number of passengers.

        Space complexity:
            O(1) extra space, not counting the input list.
        """
        # This variable accumulates the total number of valid pairs found.
        pair_count: int = 0

        # Start with the lightest passenger.
        left: int = 0

        # Start with the heaviest passenger.
        right: int = len(weights) - 1

        # Continue until the two pointers cross.
        # We only consider pairs where left < right because a passenger
        # cannot pair with themselves.
        while left < right:
            current_sum: int = weights[left] + weights[right]

            # Case 1:
            # The lightest remaining passenger and the heaviest remaining
            # passenger already fit within the limit.
            if current_sum <= limit:
                # Because the array is sorted:
                # weights[left] + weights[right] <= limit
                # implies
                # weights[left] + weights[k] <= limit for every k in [left+1, right]
                #
                # So the passenger at 'left' can form valid pairs with all
                # passengers from left+1 up to right.
                pair_count += right - left

                # We have counted every pair that starts with this 'left'
                # passenger, so we move left forward to count pairs for the
                # next passenger.
                left += 1
            else:
                # Case 2:
                # The sum is too large, so the heaviest passenger at 'right'
                # cannot pair with the current lightest passenger.
                #
                # Since weights[right] is too heavy with weights[left], it will
                # also be too heavy with any passenger heavier than weights[left].
                # Therefore, no valid pair ending at 'right' can be formed with
                # the current or any later left pointer in this configuration.
                #
                # So we move 'right' leftward to try a lighter passenger.
                right -= 1

        return pair_count

    def countRescueBoatPairs(
        self,
        weights: List[int],
        lowLimit: int,
        highLimit: int,
    ) -> int:
        """
        Count distinct passenger pairs whose combined weight is within
        [lowLimit, highLimit], inclusive.

        The key idea is to transform the range-counting problem into two
        simpler prefix-counting problems:

            count(lowLimit <= sum <= highLimit)
            = count(sum <= highLimit) - count(sum <= lowLimit - 1)

        We sort the weights once, then use the two-pointer helper twice:
        - once to count all pairs with sum <= highLimit
        - once to count all pairs with sum <= lowLimit - 1

        Their difference gives exactly the number of pairs whose sums fall
        inside the desired inclusive range.

        Args:
            weights: List of passenger weights.
            lowLimit: Minimum allowed combined weight.
            highLimit: Maximum allowed combined weight.

        Returns:
            The number of distinct index pairs (i, j), i < j, whose sum is
            between lowLimit and highLimit inclusive.

        Time complexity:
            O(n log n), due to sorting. The two-pointer scans are O(n).

        Space complexity:
            O(n) if counting the sorted copy created by sorted(...).
            The two-pointer logic itself uses O(1) extra space.
        """
        # We sort the weights because the two-pointer strategy relies on
        # monotonic order:
        # - moving the left pointer increases the smaller addend
        # - moving the right pointer decreases the larger addend
        #
        # This structure lets us count many pairs at once instead of checking
        # every pair individually, which would be too slow for n up to 200,000.
        sorted_weights: List[int] = sorted(weights)

        # Count all pairs whose sum is at most the upper bound.
        pairs_up_to_high: int = self.count_pairs_with_sum_at_most(
            sorted_weights,
            highLimit,
        )

        # Count all pairs whose sum is strictly below the lower bound.
        # Since we want sums >= lowLimit, we subtract pairs with sum <= lowLimit - 1.
        pairs_below_low: int = self.count_pairs_with_sum_at_most(
            sorted_weights,
            lowLimit - 1,
        )

        # The difference leaves exactly the pairs in the inclusive range.
        return pairs_up_to_high - pairs_below_low


if __name__ == "__main__":
    solution = Solution()

    # Example 1
    weights1: List[int] = [2, 3, 5, 6, 8]
    low_limit1: int = 7
    high_limit1: int = 10
    result1: int = solution.countRescueBoatPairs(weights1, low_limit1, high_limit1)
    print("Example 1 Result:", result1)  # Expected: 5

    # Example 2
    # The correct total is 7:
    # - 1 with 4: 2 * 2 = 4 pairs
    # - 1 with 7: 2 * 1 = 2 pairs
    # - 4 with 4: 1 pair
    weights2: List[int] = [1, 1, 4, 4, 7]
    low_limit2: int = 5
    high_limit2: int = 8
    result2: int = solution.countRescueBoatPairs(weights2, low_limit2, high_limit2)
    print("Example 2 Result:", result2)  # Expected: 7

    # Additional quick sanity check
    weights3: List[int] = [3, 3, 3]
    low_limit3: int = 6
    high_limit3: int = 6
    result3: int = solution.countRescueBoatPairs(weights3, low_limit3, high_limit3)
    print("Additional Test Result:", result3)  # Expected: 3