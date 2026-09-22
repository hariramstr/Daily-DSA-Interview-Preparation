"""
Title: Count Ferry Passenger Pairs Under Seat Limit

Problem Description:
A ferry operator wants to offer a discounted ticket to every pair of passengers who can
share one bench. You are given an integer array weights where weights[i] is the weight
of the i-th passenger, and an integer limit representing the maximum total weight that
a single bench can safely support. Each discounted pair must consist of two different
passengers, and a pair is considered valid if the sum of their weights is less than or
equal to limit.

Return the total number of distinct valid pairs (i, j) such that 0 <= i < j < n and
weights[i] + weights[j] <= limit.

The solution should be efficient for large inputs. A brute-force O(n^2) approach is too
slow when the passenger list is long, so we use sorting and a two-pointer strategy.

Constraints:
- 2 <= weights.length <= 200000
- 1 <= weights[i] <= 1000000000
- 1 <= limit <= 2000000000
- The answer can be large, so use a 64-bit integer type if needed
"""

from typing import List


class Solution:
    def count_valid_pairs(self, weights: List[int], limit: int) -> int:
        """
        Count the number of distinct passenger pairs whose combined weight
        does not exceed the bench limit.

        Args:
            weights: List of passenger weights.
            limit: Maximum allowed combined weight for one bench.

        Returns:
            The total number of distinct valid pairs.

        Time Complexity:
            O(n log n), due to sorting the array once.

        Space Complexity:
            O(n) in Python in the practical sense because sorted() creates a new list.
            If sorting in place were used, the extra auxiliary space beyond sorting
            internals would be O(1).
        """
        # We sort the weights first.
        #
        # Why sorting helps:
        # After sorting, if the lightest remaining passenger at index `left`
        # can pair with the heaviest remaining passenger at index `right`,
        # then that same lightest passenger can also pair with every passenger
        # between `left + 1` and `right`.
        #
        # This is the key observation that lets us count many pairs at once
        # instead of checking every possible pair individually.
        sorted_weights: List[int] = sorted(weights)

        # `left` starts at the lightest passenger.
        left: int = 0

        # `right` starts at the heaviest passenger.
        right: int = len(sorted_weights) - 1

        # This will store the total number of valid pairs.
        total_pairs: int = 0

        # We continue while there are at least two different passengers left to consider.
        while left < right:
            # Compute the sum of the current lightest and heaviest remaining passengers.
            current_sum: int = sorted_weights[left] + sorted_weights[right]

            # Case 1:
            # If this pair is valid, then:
            # sorted_weights[left] + sorted_weights[right] <= limit
            #
            # Because the array is sorted, every passenger between `left + 1` and `right`
            # has weight <= sorted_weights[right].
            #
            # Therefore:
            # sorted_weights[left] + sorted_weights[k] <= limit
            # for every k in [left + 1, right]
            #
            # That means the passenger at `left` forms a valid pair with ALL passengers
            # from `left + 1` through `right`.
            #
            # Number of such pairs = right - left
            if current_sum <= limit:
                total_pairs += right - left

                # We have now counted every valid pair that uses the passenger at `left`.
                # So we move `left` one step to the right and continue.
                left += 1
            else:
                # Case 2:
                # If the lightest + heaviest is already too heavy, then the heaviest
                # passenger cannot pair with the lightest one.
                #
                # Since every other passenger from `left` to `right - 1` is at least as
                # heavy as the lightest? Actually, because the list is sorted ascending,
                # every passenger from `left` to `right - 1` is >= sorted_weights[left].
                #
                # So if:
                # sorted_weights[left] + sorted_weights[right] > limit
                #
                # then for any k where left <= k < right:
                # sorted_weights[k] + sorted_weights[right] >=
                # sorted_weights[left] + sorted_weights[right] > limit
                #
                # Therefore, the passenger at `right` cannot form a valid pair with
                # anyone in the current range.
                #
                # So we must reduce `right` to try a lighter heaviest passenger.
                right -= 1

        # After the loop ends, all valid pairs have been counted.
        return total_pairs


if __name__ == "__main__":
    solution = Solution()

    # Example 1
    # weights = [2, 3, 4, 5], limit = 7
    #
    # All pairs:
    # (2,3)=5 valid
    # (2,4)=6 valid
    # (2,5)=7 valid
    # (3,4)=7 valid
    # (3,5)=8 invalid
    # (4,5)=9 invalid
    #
    # Expected answer: 4
    weights1: List[int] = [2, 3, 4, 5]
    limit1: int = 7
    result1: int = solution.count_valid_pairs(weights1, limit1)
    print("Example 1 Result:", result1)  # Expected: 4

    # Example 2
    # weights = [1, 1, 2, 2, 3], limit = 3
    #
    # Valid pairs:
    # (1,1)=2 -> 1 pair
    # each 1 with each 2 -> 2 * 2 = 4 pairs
    # each 1 with 3 -> 1+3=4 invalid
    # (2,2)=4 invalid
    # (2,3)=5 invalid
    #
    # Total valid pairs = 1 + 4 = 5
    #
    # Note:
    # The problem statement's example explanation says 6, but that explanation is
    # inconsistent with the stated condition weights[i] + weights[j] <= limit when
    # limit = 3. Under the actual rule, the correct answer is 5.
    weights2: List[int] = [1, 1, 2, 2, 3]
    limit2: int = 3
    result2: int = solution.count_valid_pairs(weights2, limit2)
    print("Example 2 Result:", result2)  # Correct under the stated rule: 5

    # Additional quick sanity check
    weights3: List[int] = [1, 2, 3, 4, 5]
    limit3: int = 5
    result3: int = solution.count_valid_pairs(weights3, limit3)
    print("Additional Example Result:", result3)  # Valid pairs: (1,2), (1,3), (1,4), (2,3) => 4