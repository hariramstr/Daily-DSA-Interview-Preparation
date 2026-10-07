"""
Title: Count Valid Song Duos Under Stage Time

Problem Description:
You are organizing a live showcase and have a list of song durations in seconds.
A duo performance is formed by choosing exactly two different songs.
Due to stage scheduling limits, only duos whose combined duration is less than
or equal to a given limit can be performed.

Given an integer array durations where durations[i] is the length of the i-th song,
and an integer stageLimit, return the number of distinct index pairs (i, j) such that
0 <= i < j < n and durations[i] + durations[j] <= stageLimit.

Two songs with the same duration are still considered different if they come from
different indices. Your solution should be efficient enough for large inputs, so a
brute-force O(n^2) approach may not pass.

A common efficient strategy is to sort the durations and use two pointers to count
how many pairs can be formed for each position without checking every pair individually.

Constraints:
- 2 <= durations.length <= 200000
- 1 <= durations[i] <= 1000000000
- 1 <= stageLimit <= 2000000000
- The answer fits in a 64-bit signed integer

Example 1:
Input: durations = [120, 90, 150, 60, 80], stageLimit = 210
Output: 7

Example 2:
Input: durations = [200, 40, 40, 170, 30], stageLimit = 210
Output: 5
"""

from typing import List


class Solution:
    def count_valid_duos(self, durations: List[int], stageLimit: int) -> int:
        """
        Count the number of distinct index pairs whose sum is less than or equal to stageLimit.

        Args:
            durations: List of song durations.
            stageLimit: Maximum allowed combined duration for a duo.

        Returns:
            The number of valid pairs (i, j) with i < j and durations[i] + durations[j] <= stageLimit.

        Time complexity:
            O(n log n), due to sorting the array once. The two-pointer scan is O(n).

        Space complexity:
            O(n) in Python because sorted() creates a new list.
        """
        # We sort the durations first.
        #
        # Why sort?
        # ----------
        # Sorting allows us to use the two-pointer technique efficiently.
        # Once the array is sorted:
        # - Smaller values are on the left
        # - Larger values are on the right
        #
        # This ordering gives us a very powerful property:
        # If durations[left] + durations[right] <= stageLimit,
        # then durations[left] paired with ANY element between left+1 and right
        # will also be valid, because all of those elements are <= durations[right].
        #
        # That means we can count many pairs at once instead of checking each pair individually.
        sorted_durations: List[int] = sorted(durations)

        # Initialize two pointers:
        # - left starts at the smallest duration
        # - right starts at the largest duration
        left: int = 0
        right: int = len(sorted_durations) - 1

        # This variable stores the total number of valid pairs found.
        total_pairs: int = 0

        # Continue until the two pointers meet.
        #
        # We only consider pairs where left < right because:
        # - A song cannot be paired with itself
        # - We only count each pair once
        while left < right:
            current_sum: int = sorted_durations[left] + sorted_durations[right]

            # Case 1:
            # The smallest remaining value + the largest remaining value is valid.
            if current_sum <= stageLimit:
                # Since the array is sorted, if sorted_durations[left] + sorted_durations[right]
                # is within the limit, then:
                #
                # sorted_durations[left] + sorted_durations[left+1] <= stageLimit
                # sorted_durations[left] + sorted_durations[left+2] <= stageLimit
                # ...
                # sorted_durations[left] + sorted_durations[right] <= stageLimit
                #
                # In other words, sorted_durations[left] can form a valid pair with
                # every element from left+1 through right.
                #
                # Number of such pairs:
                # right - left
                total_pairs += right - left

                # After counting all pairs involving the current left element,
                # we move left forward to consider the next smallest value.
                left += 1
            else:
                # Case 2:
                # The sum is too large.
                #
                # Because sorted_durations[right] is currently the largest value,
                # pairing it with sorted_durations[left] already exceeds the limit.
                # Therefore, pairing sorted_durations[right] with any value to the right of left
                # would also exceed the limit or be even larger.
                #
                # So the only way to possibly make the sum smaller is to move right leftward
                # to a smaller value.
                right -= 1

        # After the loop finishes, total_pairs contains the complete count.
        return total_pairs


if __name__ == "__main__":
    solution = Solution()

    # Example 1
    durations1: List[int] = [120, 90, 150, 60, 80]
    stage_limit1: int = 210
    result1: int = solution.count_valid_duos(durations1, stage_limit1)
    print("Example 1 Result:", result1)  # Expected: 7

    # Example 2
    durations2: List[int] = [200, 40, 40, 170, 30]
    stage_limit2: int = 210
    result2: int = solution.count_valid_duos(durations2, stage_limit2)
    print("Example 2 Result:", result2)  # Expected: 5

    # Additional quick sanity checks
    durations3: List[int] = [1, 1]
    stage_limit3: int = 2
    result3: int = solution.count_valid_duos(durations3, stage_limit3)
    print("Sanity Check 1 Result:", result3)  # Expected: 1

    durations4: List[int] = [5, 10, 15]
    stage_limit4: int = 10
    result4: int = solution.count_valid_duos(durations4, stage_limit4)
    print("Sanity Check 2 Result:", result4)  # Expected: 0