"""
Title: Count Docking Slot Pairs Within a Time Limit
Difficulty: Medium
Topic: Two Pointers

Problem Description:
A shipping terminal records the available docking duration of each open slot during the next hour.
You are given an integer array durations where durations[i] is the number of minutes that slot i
will remain available, and an integer limit. Two different slots can be assigned to a dual-berth
vessel only if their combined available time is less than or equal to limit.

Your task is to return the number of distinct pairs of slots (i, j) such that i < j and
durations[i] + durations[j] <= limit.

The input array is not guaranteed to be sorted. A brute-force O(n^2) approach may be too slow
for large terminals, so you should design an efficient solution using sorting and a two-pointer
strategy.

A pair is counted by indices, not by values. This means if the same duration appears multiple
times, different index combinations are considered different valid pairs.

Constraints:
- 1 <= durations.length <= 200000
- 0 <= durations[i] <= 1000000000
- 0 <= limit <= 2000000000
- The answer can be large, so use a 64-bit integer type if needed.

Example 1:
Input: durations = [4, 1, 3, 2], limit = 5
Output: 4
Explanation: After sorting, durations become [1, 2, 3, 4]. Valid pairs are (1,2), (1,3), (1,4),
and (2,3), whose sums are 3, 4, 5, and 5.

Example 2:
Input: durations = [6, 2, 2, 5, 1], limit = 7
Output: 6
Explanation: Valid index pairs correspond to value pairs (1,2), (1,2), (1,5), (1,6), (2,2),
and (2,5). Pairs involving 6 with 2 or 5 exceed the limit, and 5 with 2 also exceeds it.
"""

from typing import List


class Solution:
    def count_docking_slot_pairs(self, durations: List[int], limit: int) -> int:
        """
        Count the number of distinct index pairs whose values sum to at most the limit.

        The method sorts the input values and then uses a two-pointer strategy:
        one pointer starts at the smallest value and the other at the largest value.
        Based on the current sum, we either count many pairs at once or move the
        right pointer inward.

        Args:
            durations: A list of non-negative integers representing slot availability durations.
            limit: The maximum allowed combined duration for a valid pair.

        Returns:
            The number of distinct pairs (i, j) with i < j such that
            durations[i] + durations[j] <= limit.

        Time complexity:
            O(n log n), due to sorting. The two-pointer scan itself is O(n).

        Space complexity:
            O(n) in Python because sorted() creates a new list.
        """
        # We sort the durations first because the two-pointer technique depends on order.
        # Once the list is sorted:
        # - The left pointer refers to a small value.
        # - The right pointer refers to a large value.
        # This ordering lets us make strong conclusions about many pairs at once.
        sorted_durations: List[int] = sorted(durations)

        # This variable stores the total number of valid pairs found so far.
        # Python integers automatically support large values, so this safely handles
        # the large answer range required by the problem.
        pair_count: int = 0

        # The left pointer starts at the beginning of the sorted list.
        # It represents the smaller element in the candidate pair.
        left: int = 0

        # The right pointer starts at the end of the sorted list.
        # It represents the larger element in the candidate pair.
        right: int = len(sorted_durations) - 1

        # We continue while left is strictly less than right because a pair must use
        # two different indices. When left == right, there is only one element left,
        # so no pair can be formed.
        while left < right:
            # Compute the sum of the current smallest available candidate on the left
            # and the current largest available candidate on the right.
            current_sum: int = sorted_durations[left] + sorted_durations[right]

            # Case 1:
            # If the current sum is within the limit, then we can count not just one pair,
            # but many pairs at once.
            #
            # Why?
            # Because the array is sorted. If:
            #   sorted_durations[left] + sorted_durations[right] <= limit
            # then for every index k where left < k <= right:
            #   sorted_durations[left] + sorted_durations[k] <= limit
            #
            # This is true because sorted_durations[k] <= sorted_durations[right].
            # So replacing the right value with any smaller value keeps the sum valid.
            #
            # Therefore, the element at 'left' forms valid pairs with every element from
            # left + 1 through right. The number of such pairs is:
            #   right - left
            if current_sum <= limit:
                pair_count += right - left

                # After counting all pairs that start with 'left', we move left forward.
                # We do this because every valid pair involving the current left element
                # has already been counted exactly once.
                left += 1
            else:
                # Case 2:
                # If the current sum is too large, then the value at 'right' is too large
                # to pair with the current 'left'. Since the list is sorted, it is also too
                # large to pair with any value to the right of 'left' that is >= current left.
                #
                # To reduce the sum, we must move the right pointer leftward to a smaller value.
                right -= 1

        # Once the pointers cross, all valid pairs have been counted.
        return pair_count

    def countPairs(self, durations: List[int], limit: int) -> int:
        """
        Provide an alternate method name that calls the main implementation.

        Args:
            durations: A list of slot availability durations.
            limit: The maximum allowed sum for a valid pair.

        Returns:
            The number of valid distinct pairs.

        Time complexity:
            O(n log n)

        Space complexity:
            O(n)
        """
        return self.count_docking_slot_pairs(durations, limit)


if __name__ == "__main__":
    solution = Solution()

    # Example 1:
    # Original input: [4, 1, 3, 2], limit = 5
    # Sorted: [1, 2, 3, 4]
    # Valid pairs by values:
    # 1+2=3, 1+3=4, 1+4=5, 2+3=5
    # Total = 4
    durations_1: List[int] = [4, 1, 3, 2]
    limit_1: int = 5
    result_1: int = solution.count_docking_slot_pairs(durations_1, limit_1)
    print("Example 1 Result:", result_1)  # Expected: 4

    # Example 2:
    # Original input: [6, 2, 2, 5, 1], limit = 7
    # Sorted: [1, 2, 2, 5, 6]
    # Valid pairs by values:
    # 1+2=3, 1+2=3, 1+5=6, 1+6=7, 2+2=4, 2+5=7
    # Total = 6
    durations_2: List[int] = [6, 2, 2, 5, 1]
    limit_2: int = 7
    result_2: int = solution.count_docking_slot_pairs(durations_2, limit_2)
    print("Example 2 Result:", result_2)  # Expected: 6

    # Additional quick checks for beginner-friendly demonstration.
    durations_3: List[int] = [1]
    limit_3: int = 10
    result_3: int = solution.count_docking_slot_pairs(durations_3, limit_3)
    print("Single Element Result:", result_3)  # Expected: 0

    durations_4: List[int] = [0, 0, 0]
    limit_4: int = 0
    result_4: int = solution.count_docking_slot_pairs(durations_4, limit_4)
    print("All Zeroes Result:", result_4)  # Expected: 3