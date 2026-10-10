"""
Title: Count Repair Teams Within Arrival Spread

Problem Description:
A facilities company is scheduling emergency repair teams for a large campus.
You are given an integer array arrivalTimes where arrivalTimes[i] is the arrival
time, in minutes, of the i-th team. You are also given an integer maxSpread.

Two teams are considered compatible if the absolute difference between their
arrival times is less than or equal to maxSpread.

Your task is to return the total number of distinct pairs of teams (i, j) such
that i < j and the two teams are compatible.

Because the input may be unsorted and can be large, an efficient solution is
required. A brute-force O(n^2) approach will be too slow for the largest cases.
The intended solution uses sorting together with a two-pointer scan to count,
for each right endpoint, how many earlier teams can pair with it while staying
within the allowed spread.

Constraints:
- 1 <= arrivalTimes.length <= 200000
- 0 <= arrivalTimes[i] <= 1000000000
- 0 <= maxSpread <= 1000000000
- The answer may exceed 32-bit integer range, so use a 64-bit integer type
  where needed.

Example 1:
Input: arrivalTimes = [12, 5, 9, 14], maxSpread = 4
Output: 4
Explanation:
After sorting, arrival times are [5, 9, 12, 14].
Compatible pairs are (5,9), (9,12), (9,14), and (12,14).

Example 2:
Input: arrivalTimes = [3, 3, 3, 10], maxSpread = 0
Output: 3
Explanation:
Only teams with exactly the same arrival time can pair.
The three teams arriving at time 3 form 3 distinct pairs.
"""

from typing import List


class Solution:
    def count_compatible_pairs(self, arrivalTimes: List[int], maxSpread: int) -> int:
        """
        Count the number of distinct compatible team pairs.

        A pair of teams is compatible if the absolute difference between their
        arrival times is less than or equal to maxSpread.

        Args:
            arrivalTimes: A list of team arrival times.
            maxSpread: The maximum allowed difference between two arrival times.

        Returns:
            The total number of distinct pairs (i, j) with i < j that are compatible.

        Time complexity:
            O(n log n), due to sorting the array. The two-pointer scan itself is O(n).

        Space complexity:
            O(n) in Python because sorted() creates a new list.
        """
        # Step 1: Sort the arrival times.
        #
        # Why sort?
        # The compatibility rule depends only on the difference between values.
        # Once the array is sorted, for any fixed right endpoint, all valid left
        # endpoints form one continuous block. That structure lets us count pairs
        # efficiently with two pointers instead of checking every pair.
        sorted_times: List[int] = sorted(arrivalTimes)

        # This variable will store the final answer.
        #
        # In Python, integers automatically grow as needed, so we do not need a
        # special 64-bit type. This still satisfies the requirement that the
        # answer may exceed 32-bit range.
        total_pairs: int = 0

        # The left pointer marks the smallest index that can still form a valid
        # pair with the current right pointer.
        #
        # Important invariant:
        # For each right index, after we move left forward enough,
        # sorted_times[right] - sorted_times[left] <= maxSpread
        # and for any index smaller than left, the difference would be too large.
        left: int = 0

        # Step 2: Sweep the array with the right pointer.
        #
        # For each position "right", we want to know how many earlier positions
        # can pair with it. Because the array is sorted, we only need to move
        # left forward when the spread becomes too large.
        for right in range(len(sorted_times)):
            # Step 3: Shrink the window from the left while it is invalid.
            #
            # Since sorted_times is sorted in non-decreasing order:
            # sorted_times[right] >= sorted_times[left]
            #
            # Therefore, the absolute difference simplifies to:
            # sorted_times[right] - sorted_times[left]
            #
            # If that difference is greater than maxSpread, then left is too far
            # away in value and cannot pair with right. We move left forward until
            # the window becomes valid again.
            while sorted_times[right] - sorted_times[left] > maxSpread:
                left += 1

            # Step 4: Count how many valid left endpoints exist for this right.
            #
            # After the while-loop finishes, every index from left to right - 1
            # forms a valid pair with right.
            #
            # Number of such indices:
            # right - left
            #
            # Example:
            # If left = 2 and right = 5, then valid partners are indices
            # 2, 3, 4 -> exactly 3 values -> 5 - 2 = 3
            total_pairs += right - left

        return total_pairs


if __name__ == "__main__":
    solution = Solution()

    # Example 1
    arrival_times_1: List[int] = [12, 5, 9, 14]
    max_spread_1: int = 4
    result_1: int = solution.count_compatible_pairs(arrival_times_1, max_spread_1)
    print(result_1)  # Expected: 4

    # Example 2
    arrival_times_2: List[int] = [3, 3, 3, 10]
    max_spread_2: int = 0
    result_2: int = solution.count_compatible_pairs(arrival_times_2, max_spread_2)
    print(result_2)  # Expected: 3

    # Additional quick sanity checks
    arrival_times_3: List[int] = [1]
    max_spread_3: int = 10
    result_3: int = solution.count_compatible_pairs(arrival_times_3, max_spread_3)
    print(result_3)  # Expected: 0

    arrival_times_4: List[int] = [1, 2, 3, 4, 5]
    max_spread_4: int = 2
    result_4: int = solution.count_compatible_pairs(arrival_times_4, max_spread_4)
    print(result_4)  # Expected: 7