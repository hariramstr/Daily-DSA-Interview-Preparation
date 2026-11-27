"""
Title: Pair Dishes for a Shared Oven Slot

Problem Description:
A catering team has a list of dish preparation times, where times[i] is the number
of minutes needed to bake the i-th dish. The team wants to load exactly two dishes
into a shared oven slot. For safety and scheduling reasons, the combined baking time
of the two chosen dishes must be at least minTime and at most maxTime.

Your task is to return the number of distinct pairs of dishes (i, j) such that
0 <= i < j < n and minTime <= times[i] + times[j] <= maxTime.

Two dishes are considered distinct if they come from different indices, even if
their baking times are equal. You may reorder the array internally if needed, but
the answer must count pairs from the original set of dishes.

Design an algorithm that is efficient for large inputs. A brute-force O(n^2)
solution will be too slow under the given constraints.

Constraints:
- 2 <= times.length <= 2 * 10^5
- 1 <= times[i] <= 10^9
- 1 <= minTime <= maxTime <= 2 * 10^9
- The answer may be large, so use a 64-bit integer type where necessary.

Key Idea:
Count how many pairs have sum <= maxTime, then subtract how many pairs have
sum <= minTime - 1. The difference is exactly the number of pairs whose sums
fall inside [minTime, maxTime].

This is efficient after sorting, using a classic two-pointer technique.
"""

from typing import List


class Solution:
    def count_pairs_with_sum_at_most(self, times: List[int], limit: int) -> int:
        """
        Count how many index pairs (i, j), with i < j, have times[i] + times[j] <= limit.

        Args:
            times: Sorted list of dish preparation times.
            limit: Maximum allowed pair sum for this counting pass.

        Returns:
            Number of valid pairs whose sum is at most limit.

        Time complexity:
            O(n), where n is the length of times.

        Space complexity:
            O(1) extra space, not counting the input list.
        """
        # We will use the two-pointer technique on the sorted array.
        #
        # left  -> starts at the smallest value
        # right -> starts at the largest value
        #
        # Because the array is sorted:
        # - If times[left] + times[right] <= limit, then not only is (left, right) valid,
        #   but also every pair (left, k) for k in [left+1, right] is valid.
        #   Why? Because times[k] <= times[right] for all k <= right, so replacing right
        #   with any smaller index cannot increase the sum.
        #
        # This allows us to count many pairs at once instead of checking each pair individually.
        left: int = 0
        right: int = len(times) - 1
        pair_count: int = 0

        # Continue until the two pointers cross.
        while left < right:
            current_sum: int = times[left] + times[right]

            if current_sum <= limit:
                # Since times[left] + times[right] is within the limit,
                # every pair formed by fixing 'left' and choosing any index from
                # left+1 through right is also valid.
                #
                # Number of such pairs:
                #   right - left
                #
                # Example:
                #   times = [1, 2, 3, 5], left = 0, right = 3
                #   if 1 + 5 <= limit, then:
                #   (0,1), (0,2), (0,3) are all valid -> 3 pairs
                pair_count += right - left

                # Move left forward to count pairs starting from the next smallest value.
                left += 1
            else:
                # If the sum is too large, then pairing times[right] with times[left]
                # already fails. Since times[left] is the smallest available on the left side,
                # pairing times[right] with any larger left index would only keep the sum large
                # or make it larger.
                #
                # Therefore, to reduce the sum, we must move 'right' leftward.
                right -= 1

        return pair_count

    def count_valid_pairs(self, times: List[int], minTime: int, maxTime: int) -> int:
        """
        Return the number of distinct pairs of dishes whose total baking time is
        between minTime and maxTime inclusive.

        Args:
            times: List of dish preparation times.
            minTime: Minimum allowed sum of a chosen pair.
            maxTime: Maximum allowed sum of a chosen pair.

        Returns:
            Number of distinct index pairs (i, j) with i < j and
            minTime <= times[i] + times[j] <= maxTime.

        Time complexity:
            O(n log n) due to sorting, plus O(n) for the two counting passes.

        Space complexity:
            O(n) if counting the sorted copy created by sorted(...),
            or O(1) extra if sorting in place were used.
        """
        # Step 1: Sort the times.
        #
        # Why sort?
        # The two-pointer counting strategy relies on the array being in non-decreasing order.
        # Once sorted, we can reason about how sums change when moving pointers.
        sorted_times: List[int] = sorted(times)

        # Step 2: Count all pairs with sum <= maxTime.
        #
        # This gives us every pair that is not too large.
        pairs_at_most_max: int = self.count_pairs_with_sum_at_most(sorted_times, maxTime)

        # Step 3: Count all pairs with sum <= minTime - 1.
        #
        # These are the pairs that are strictly too small to be included.
        pairs_below_min: int = self.count_pairs_with_sum_at_most(sorted_times, minTime - 1)

        # Step 4: Subtract.
        #
        # Pairs in [minTime, maxTime] =
        #   (pairs with sum <= maxTime) - (pairs with sum <= minTime - 1)
        #
        # This is a standard inclusive-range counting trick.
        valid_pairs: int = pairs_at_most_max - pairs_below_min
        return valid_pairs


if __name__ == "__main__":
    solution = Solution()

    # Example 1
    times1: List[int] = [2, 5, 3, 7, 4]
    min_time1: int = 7
    max_time1: int = 9
    result1: int = solution.count_valid_pairs(times1, min_time1, max_time1)
    print("Example 1 Result:", result1)  # Expected: 5

    # Example 2
    times2: List[int] = [1, 1, 2, 2, 3]
    min_time2: int = 3
    max_time2: int = 4
    result2: int = solution.count_valid_pairs(times2, min_time2, max_time2)
    print("Example 2 Result:", result2)  # Expected: 8

    # Additional quick sanity check
    times3: List[int] = [4, 4, 4]
    min_time3: int = 8
    max_time3: int = 8
    result3: int = solution.count_valid_pairs(times3, min_time3, max_time3)
    print("Additional Test Result:", result3)  # Expected: 3