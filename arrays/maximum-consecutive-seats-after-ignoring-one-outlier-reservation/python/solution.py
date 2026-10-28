"""
Title: Maximum Consecutive Seats After Ignoring One Outlier Reservation

Problem Description:
A theater stores its reserved seat numbers for a show in an integer array `seats`.
The numbers are not sorted, may contain duplicates due to repeated sync events,
and may include gaps because many seats were never reserved.

You are allowed to ignore at most one distinct seat number as a data outlier.
Ignoring a seat number means removing all occurrences of that exact value from
consideration.

After optionally ignoring one value, determine the maximum length of a consecutive
block of reserved seat numbers that can remain.

A consecutive block is a set of integers like [12, 13, 14, 15], where every number
from the minimum to the maximum is present at least once. Duplicates do not increase
the block length.

Important observation:
Removing one distinct value cannot create a new consecutive connection across a gap.
It can only delete values, which may shorten existing runs or leave them unchanged.
Therefore, the answer is simply the length of the longest consecutive run among the
distinct seat numbers.

Constraints:
- 1 <= seats.length <= 2 * 10^5
- 1 <= seats[i] <= 10^9
"""

from typing import List, Set


class Solution:
    def longest_consecutive_after_ignoring_one(self, seats: List[int]) -> int:
        """
        Compute the maximum length of a consecutive block of reserved seat numbers
        after ignoring at most one distinct value.

        Because removing one value cannot fill a missing number, it cannot merge two
        separated consecutive runs. Therefore, the best possible answer is exactly
        the longest consecutive run among the distinct seat values.

        Args:
            seats: List of reserved seat numbers. May be unsorted and may contain duplicates.

        Returns:
            The maximum possible length of a consecutive block after removing all
            copies of at most one distinct seat value.

        Time complexity:
            O(n), where n is the length of `seats`, because set construction and
            consecutive-run scanning are both linear on average.

        Space complexity:
            O(n), for storing the distinct seat values in a set.
        """
        # Step 1:
        # Convert the input list into a set.
        #
        # Why do we do this?
        # - Duplicates do not help extend a consecutive block.
        # - The problem explicitly says duplicates should not increase the block length.
        # - A set removes duplicates automatically.
        # - A set also gives us O(1) average-time membership checks, which is perfect
        #   for testing whether x + 1 or x - 1 exists.
        distinct_seats: Set[int] = set(seats)

        # This variable will store the best (largest) consecutive run length found so far.
        longest_run: int = 0

        # Step 2:
        # Iterate through each distinct seat number.
        #
        # Core idea:
        # We only start counting a run from numbers that are the beginning of a run.
        # A number `value` is the start of a run if `value - 1` is NOT present.
        #
        # Example:
        # If the set is {10, 11, 12, 14, 15, 16}
        # - 10 is a start because 9 is not present
        # - 11 is not a start because 10 is present
        # - 12 is not a start because 11 is present
        # - 14 is a start because 13 is not present
        #
        # This avoids recounting the same run many times and keeps the algorithm O(n).
        for value in distinct_seats:
            # Check whether this value is the first number of a consecutive run.
            if value - 1 not in distinct_seats:
                # We found the beginning of a run.
                current_length: int = 1
                current_value: int = value

                # Step 3:
                # Extend the run forward as long as the next consecutive number exists.
                #
                # Example:
                # Start at 10:
                # - 11 exists -> length becomes 2
                # - 12 exists -> length becomes 3
                # - 13 does not exist -> stop
                while current_value + 1 in distinct_seats:
                    current_value += 1
                    current_length += 1

                # Step 4:
                # Update the global best answer if this run is longer than any run seen before.
                if current_length > longest_run:
                    longest_run = current_length

        # Step 5:
        # Return the longest consecutive run found.
        #
        # Why is this the final answer even though we may ignore one value?
        # Because removing one distinct value cannot add any missing seat number.
        # It cannot connect two runs separated by a gap.
        # So the best answer after removing at most one value is never larger than
        # the best answer before removing anything.
        # Since "at most one" includes choosing to remove nothing, the longest existing
        # run is always achievable and therefore optimal.
        return longest_run


if __name__ == "__main__":
    solution = Solution()

    # Sample 1 from the statement.
    seats1: List[int] = [10, 11, 12, 14, 14, 15, 16]
    result1: int = solution.longest_consecutive_after_ignoring_one(seats1)
    print("Input:", seats1)
    print("Output:", result1)
    print("Expected:", 3)
    print()

    # Sample 2 from the statement.
    seats2: List[int] = [3, 4, 5, 7, 8, 9, 20]
    result2: int = solution.longest_consecutive_after_ignoring_one(seats2)
    print("Input:", seats2)
    print("Output:", result2)
    print("Expected:", 3)
    print()

    # Additional sanity checks.

    # All duplicates of one value -> distinct set is {5}, longest run is 1.
    seats3: List[int] = [5, 5, 5, 5]
    result3: int = solution.longest_consecutive_after_ignoring_one(seats3)
    print("Input:", seats3)
    print("Output:", result3)
    print("Expected:", 1)
    print()

    # Fully consecutive distinct values -> longest run is the full length.
    seats4: List[int] = [8, 6, 7, 9, 10, 7, 8]
    result4: int = solution.longest_consecutive_after_ignoring_one(seats4)
    print("Input:", seats4)
    print("Output:", result4)
    print("Expected:", 5)
    print()

    # Multiple separated runs -> answer is the longest one.
    seats5: List[int] = [100, 4, 200, 1, 3, 2]
    result5: int = solution.longest_consecutive_after_ignoring_one(seats5)
    print("Input:", seats5)
    print("Output:", result5)
    print("Expected:", 4)