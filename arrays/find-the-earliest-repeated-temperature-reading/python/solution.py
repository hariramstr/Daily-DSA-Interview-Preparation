"""
Title: Find the Earliest Repeated Temperature Reading
Difficulty: Easy
Topic: Arrays

Problem Description:
A weather station stores hourly temperature readings in an integer array `temps`,
where `temps[i]` is the recorded temperature at hour `i`.

Your task is to find the first temperature value that appears more than once when
scanning the array from left to right. Return the temperature whose second
occurrence happens earliest.

This means:
- We are NOT looking for the smallest repeated value.
- We are NOT looking for the most frequent value.
- We ARE looking for the first value that becomes a duplicate during a left-to-right scan.

Example:
For temps = [18, 21, 19, 21, 18]
- 18 is seen first
- 21 is seen first
- 19 is seen first
- 21 is seen again -> this is the first duplicate encountered
So the answer is 21.

If no temperature is repeated, return -1.

Constraints:
- 1 <= temps.length <= 100000
- -1000000 <= temps[i] <= 1000000
- The solution should run efficiently for large arrays.
"""

from typing import List, Set


class Solution:
    def first_repeated_temperature(self, temps: List[int]) -> int:
        """
        Find the earliest repeated temperature reading while scanning left to right.

        The method returns the first value whose second occurrence is encountered
        during the scan. If no value repeats, it returns -1.

        Args:
            temps: A list of integer temperature readings.

        Returns:
            The first temperature value that repeats during a left-to-right scan,
            or -1 if no repeated value exists.

        Time Complexity:
            O(n), where n is the length of temps, because each element is processed once.

        Space Complexity:
            O(n) in the worst case, if all values are unique and stored in the set.
        """
        # We use a set to keep track of temperatures we have already seen.
        #
        # Why a set?
        # - A set gives average O(1) lookup time.
        # - For each temperature, we need to quickly answer:
        #   "Have I seen this value before?"
        # - This makes the overall solution efficient even for large inputs.
        seen: Set[int] = set()

        # We scan the array from left to right exactly once.
        #
        # This order is extremely important because the problem asks for the
        # temperature whose SECOND occurrence happens earliest.
        #
        # That means the first time we encounter a value that is already in `seen`,
        # we can immediately return it. At that exact moment, we have found the
        # earliest duplicate by arrival order.
        for temp in temps:
            # Check whether the current temperature has already appeared earlier.
            if temp in seen:
                # If yes, then this is the first duplicate encountered during the scan.
                # Because we are scanning from left to right, this duplicate's second
                # occurrence is the earliest possible one among all repeated values.
                return temp

            # If the temperature has not been seen before, add it to the set
            # so future occurrences can be detected as duplicates.
            seen.add(temp)

        # If we finish the entire scan without finding any duplicate,
        # then no temperature repeats.
        return -1


if __name__ == "__main__":
    solution = Solution()

    # Example 1 from the problem statement:
    # temps = [18, 21, 19, 21, 18]
    #
    # Step-by-step trace:
    # - See 18 -> not seen before, add it
    # - See 21 -> not seen before, add it
    # - See 19 -> not seen before, add it
    # - See 21 -> already seen, return 21
    #
    # Expected output: 21
    temps1 = [18, 21, 19, 21, 18]
    result1 = solution.first_repeated_temperature(temps1)
    print(f"Input: {temps1}")
    print(f"Output: {result1}")
    print("Expected: 21")
    print()

    # Example 2 from the problem statement:
    # temps = [5, 7, 8, 9]
    #
    # Step-by-step trace:
    # - See 5 -> add
    # - See 7 -> add
    # - See 8 -> add
    # - See 9 -> add
    # End of array, no duplicates found
    #
    # Expected output: -1
    temps2 = [5, 7, 8, 9]
    result2 = solution.first_repeated_temperature(temps2)
    print(f"Input: {temps2}")
    print(f"Output: {result2}")
    print("Expected: -1")
    print()

    # Additional beginner-friendly test:
    # temps = [3, 1, 4, 1, 5, 3]
    #
    # Step-by-step:
    # - 3 -> add
    # - 1 -> add
    # - 4 -> add
    # - 1 -> duplicate found first, return 1
    #
    # Even though 3 also repeats later, 1 repeats earlier.
    temps3 = [3, 1, 4, 1, 5, 3]
    result3 = solution.first_repeated_temperature(temps3)
    print(f"Input: {temps3}")
    print(f"Output: {result3}")
    print("Expected: 1")