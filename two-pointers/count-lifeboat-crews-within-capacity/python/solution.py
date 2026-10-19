"""
Title: Count Lifeboat Crews Within Capacity

Problem Description:
You are given an integer array weights where weights[i] is the weight of the i-th rescue worker,
and an integer limit representing the maximum total weight a small lifeboat can safely carry.
A valid crew consists of exactly two different workers whose combined weight is less than or
equal to limit.

Your task is to return the total number of distinct valid crews that can be formed.
Two crews are considered different if they use a different pair of indices, even if the workers
have the same weight.

For example, if three workers all weigh 40 and the limit is 80, then all three index pairs are
valid crews.

The input array is not guaranteed to be sorted. An efficient solution is expected; a quadratic
scan over all pairs may time out for large inputs. Think carefully about how sorting and a
two-pointer strategy can help count many valid pairs at once.

Constraints:
- 2 <= weights.length <= 200000
- 1 <= weights[i] <= 10^9
- 1 <= limit <= 2 * 10^9
- Return the answer as a 64-bit integer if your language requires it
"""

from typing import List


class Solution:
    def count_valid_crews(self, weights: List[int], limit: int) -> int:
        """
        Count the number of distinct pairs of workers whose combined weight
        is less than or equal to the given limit.

        Args:
            weights: List of worker weights.
            limit: Maximum allowed combined weight for a crew of two.

        Returns:
            The total number of distinct valid pairs.

        Time complexity:
            O(n log n), because we sort the array once and then scan it with
            two pointers in linear time.

        Space complexity:
            O(n) in Python in the practical sense because sorted(weights)
            creates a new list. The two-pointer scan itself uses O(1) extra space.
        """
        # We sort the weights first.
        #
        # Why sorting helps:
        # - Once the array is sorted, we can reason about many pairs at once.
        # - If the lightest remaining worker paired with the heaviest remaining worker
        #   is valid, then that same lightest worker can also pair with EVERY worker
        #   between them, because all those workers are lighter than or equal to the
        #   current heaviest worker.
        # - This allows us to count multiple valid pairs in one step instead of checking
        #   each pair individually.
        sorted_weights: List[int] = sorted(weights)

        # Left pointer starts at the lightest worker.
        left: int = 0

        # Right pointer starts at the heaviest worker.
        right: int = len(sorted_weights) - 1

        # This variable stores the total number of valid crews found.
        #
        # In Python, int automatically handles large values, so it is safe even when
        # the number of pairs is large. In other languages, a 64-bit integer would be needed.
        valid_pairs: int = 0

        # We continue while left is strictly less than right because:
        # - A crew must contain exactly two DIFFERENT workers.
        # - Therefore, we cannot pair a worker with themselves.
        while left < right:
            # Compute the sum of the current lightest and heaviest remaining workers.
            current_sum: int = sorted_weights[left] + sorted_weights[right]

            # Case 1:
            # If this pair is valid, then we can count MANY pairs at once.
            if current_sum <= limit:
                # Why can we add (right - left)?
                #
                # Because the array is sorted:
                # sorted_weights[left] <= sorted_weights[left + 1] <= ... <= sorted_weights[right]
                #
                # We already know:
                # sorted_weights[left] + sorted_weights[right] <= limit
                #
                # Therefore, for every index k where left < k <= right:
                # sorted_weights[left] + sorted_weights[k] <= limit
                #
                # So the worker at index "left" can form a valid crew with every worker
                # from left + 1 through right.
                #
                # Number of such workers:
                # right - left
                valid_pairs += right - left

                # After counting all pairs that use sorted_weights[left], we move left forward.
                #
                # Why?
                # - We have already counted every valid pair involving this left worker.
                # - There is no need to consider this worker again.
                left += 1
            else:
                # Case 2:
                # If the lightest + heaviest is TOO HEAVY, then the heaviest worker cannot
                # pair with the current lightest worker.
                #
                # Because the array is sorted, any worker between left and right is at least
                # as heavy as sorted_weights[left]. That means:
                # sorted_weights[i] + sorted_weights[right] >= sorted_weights[left] + sorted_weights[right]
                # for all i >= left
                #
                # So if the lightest possible partner already makes the sum exceed the limit,
                # then the heaviest worker cannot form a valid pair with ANY worker in the
                # current range.
                #
                # Therefore, we must move the right pointer leftward to try a lighter worker.
                right -= 1

        # When the loop finishes, all valid pairs have been counted.
        return valid_pairs

    def countPairs(self, weights: List[int], limit: int) -> int:
        """
        Compatibility wrapper method that calls the main counting algorithm.

        Args:
            weights: List of worker weights.
            limit: Maximum allowed combined weight for a crew of two.

        Returns:
            The total number of distinct valid pairs.

        Time complexity:
            O(n log n)

        Space complexity:
            O(n) due to sorting into a new list.
        """
        return self.count_valid_crews(weights, limit)


if __name__ == "__main__":
    solution = Solution()

    # Example 1
    # Input: [70, 50, 80, 40], limit = 120
    # Sorted: [40, 50, 70, 80]
    # Valid pairs:
    # (40,50), (40,70), (40,80), (50,70)
    # Total = 4
    weights1: List[int] = [70, 50, 80, 40]
    limit1: int = 120
    result1: int = solution.count_valid_crews(weights1, limit1)
    print("Example 1 Result:", result1)  # Expected: 4

    # Example 2
    # Input: [30, 30, 60, 90, 50], limit = 90
    # Sorted: [30, 30, 50, 60, 90]
    # Valid pairs:
    # (30,30), (30,60), (30,60 using the other 30), (30,50), (30,50 using the other 30)
    # Total = 5
    weights2: List[int] = [30, 30, 60, 90, 50]
    limit2: int = 90
    result2: int = solution.count_valid_crews(weights2, limit2)
    print("Example 2 Result:", result2)  # Expected: 5

    # Additional quick sanity check
    # Three workers of weight 40, limit 80
    # All three index pairs are valid: C(3,2) = 3
    weights3: List[int] = [40, 40, 40]
    limit3: int = 80
    result3: int = solution.count_valid_crews(weights3, limit3)
    print("Additional Example Result:", result3)  # Expected: 3