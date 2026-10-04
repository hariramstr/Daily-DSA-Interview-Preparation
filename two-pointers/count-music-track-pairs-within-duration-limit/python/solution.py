"""
Title: Count Music Track Pairs Within Duration Limit

Problem Description:
You are given an integer array durations where durations[i] is the length of the i-th
music track in seconds, and an integer limit. A pair of distinct tracks (i, j) is
considered playable in one short session if i < j and durations[i] + durations[j] <= limit.

Return the total number of playable pairs.

The order of tracks in the input array does not matter for pairing, and each pair should
be counted at most once. You are not asked to list the pairs, only to count them efficiently.

A brute-force solution that checks every pair takes O(n^2) time and may be too slow for
large inputs. Sorting the array allows us to use a two-pointer strategy to count many
valid pairs at once.

Constraints:
- 1 <= durations.length <= 2 * 10^5
- 1 <= durations[i] <= 10^9
- 1 <= limit <= 2 * 10^9
- The answer may be large, so use a 64-bit integer type if needed.

Example 1:
Input: durations = [120, 90, 150, 60], limit = 210
Output: 4
Explanation:
Valid pairs are:
- (120, 90) = 210
- (120, 60) = 180
- (90, 60) = 150
- (150, 60) = 210
The pair (150, 90) = 240 exceeds the limit.

Example 2:
Input: durations = [40, 40, 40, 100], limit = 80
Output: 3
Explanation:
Any pair formed by two 40-second tracks is valid. There are 3 such pairs:
- first 40 with second 40
- first 40 with third 40
- second 40 with third 40
No pair involving 100 fits within the limit.
"""

from typing import List


class Solution:
    def count_playable_pairs(self, durations: List[int], limit: int) -> int:
        """
        Count the number of distinct track pairs whose total duration does not exceed the limit.

        Args:
            durations: A list of positive integers representing track durations in seconds.
            limit: The maximum allowed sum for a playable pair.

        Returns:
            The total number of valid pairs (i, j) such that i < j and
            durations[i] + durations[j] <= limit.

        Time complexity:
            O(n log n), due to sorting the array once. The two-pointer scan is O(n).

        Space complexity:
            O(n) in Python because sorted(durations) creates a new list.
            The pointer scan itself uses O(1) extra space.
        """
        # Step 1: Sort the durations.
        #
        # Why sort?
        # ----------
        # Sorting is the key idea that makes the two-pointer technique possible.
        # Once the values are in non-decreasing order:
        # - Small values are on the left
        # - Large values are on the right
        #
        # This structure lets us reason about many pairs at once instead of checking
        # every possible pair individually.
        #
        # Example:
        # durations = [120, 90, 150, 60]
        # sorted -> [60, 90, 120, 150]
        #
        # After sorting, if the smallest remaining value plus the largest remaining value
        # is valid, then that smallest value will also form valid pairs with every value
        # between them, because those values are <= the largest one.
        sorted_durations: List[int] = sorted(durations)

        # Step 2: Initialize two pointers.
        #
        # left  -> starts at the smallest duration
        # right -> starts at the largest duration
        #
        # We will move these pointers inward based on whether the current pair fits.
        left: int = 0
        right: int = len(sorted_durations) - 1

        # Step 3: This variable stores the total number of valid pairs found.
        #
        # In Python, int automatically handles large values, so it is safe even if
        # the number of pairs becomes very large.
        pair_count: int = 0

        # Step 4: Process while there are at least two different indices left.
        #
        # We need left < right because a pair must use two distinct tracks.
        while left < right:
            # Compute the sum of the current smallest and current largest remaining tracks.
            current_sum: int = sorted_durations[left] + sorted_durations[right]

            # Case A: The pair fits within the limit.
            if current_sum <= limit:
                # This is the most important optimization in the algorithm.
                #
                # Since the array is sorted:
                # sorted_durations[left] <= sorted_durations[left + 1] <= ... <= sorted_durations[right]
                #
                # If sorted_durations[left] + sorted_durations[right] <= limit,
                # then sorted_durations[left] paired with ANY index from left+1 to right
                # will also be valid, because all those values are <= sorted_durations[right].
                #
                # That means we can count all these pairs at once:
                # (left, left+1), (left, left+2), ..., (left, right)
                #
                # Number of such pairs:
                # right - left
                #
                # This avoids checking each one individually and is what makes the
                # algorithm linear after sorting.
                pair_count += right - left

                # After counting all pairs involving sorted_durations[left],
                # we move left forward.
                #
                # Why?
                # Because we have already counted every valid pair that starts with
                # this left index. There is no need to revisit it.
                left += 1
            else:
                # Case B: The pair is too large.
                #
                # If sorted_durations[left] + sorted_durations[right] > limit,
                # then the track at 'right' is too large to pair with the current
                # smallest remaining track.
                #
                # Because sorted_durations[left] is the smallest available value,
                # pairing sorted_durations[right] with any other value between left and right
                # would only keep the sum the same or make it larger.
                #
                # Therefore, no pair using this 'right' value can work with the current
                # left boundary, so we must reduce the larger value by moving 'right' leftward.
                right -= 1

        # Step 5: Return the total number of valid pairs found.
        return pair_count


if __name__ == "__main__":
    solution = Solution()

    # Sample input 1 from the problem statement.
    durations_1: List[int] = [120, 90, 150, 60]
    limit_1: int = 210
    result_1: int = solution.count_playable_pairs(durations_1, limit_1)
    print("Example 1:")
    print(f"durations = {durations_1}, limit = {limit_1}")
    print(f"Output: {result_1}")
    print("Expected: 4")
    print()

    # Manual trace for Example 1 to verify correctness:
    # Sorted: [60, 90, 120, 150]
    # left=0, right=3 -> 60+150=210 <= 210, count += 3 -> pairs:
    #   (60,90), (60,120), (60,150)
    # left=1, right=3 -> 90+150=240 > 210, move right
    # left=1, right=2 -> 90+120=210 <= 210, count += 1 -> pair:
    #   (90,120)
    # Total = 4

    # Sample input 2 from the problem statement.
    durations_2: List[int] = [40, 40, 40, 100]
    limit_2: int = 80
    result_2: int = solution.count_playable_pairs(durations_2, limit_2)
    print("Example 2:")
    print(f"durations = {durations_2}, limit = {limit_2}")
    print(f"Output: {result_2}")
    print("Expected: 3")
    print()

    # Manual trace for Example 2 to verify correctness:
    # Sorted: [40, 40, 40, 100]
    # left=0, right=3 -> 40+100=140 > 80, move right
    # left=0, right=2 -> 40+40=80 <= 80, count += 2 -> pairs:
    #   (index 0 with index 1), (index 0 with index 2)
    # left=1, right=2 -> 40+40=80 <= 80, count += 1 -> pair:
    #   (index 1 with index 2)
    # Total = 3

    # Additional quick sanity check.
    durations_3: List[int] = [10, 20, 30, 40, 50]
    limit_3: int = 60
    result_3: int = solution.count_playable_pairs(durations_3, limit_3)
    print("Additional Test:")
    print(f"durations = {durations_3}, limit = {limit_3}")
    print(f"Output: {result_3}")