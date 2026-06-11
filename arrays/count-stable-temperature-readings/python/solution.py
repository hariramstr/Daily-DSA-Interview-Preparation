"""
Title: Count Stable Temperature Readings

Problem Description:
You are given an integer array readings where readings[i] represents the temperature
recorded on day i. A reading is called stable if its value is equal to the average
of its immediate neighbors.

For an index i to be stable, it must satisfy:
    1 <= i <= n - 2
    readings[i] * 2 == readings[i - 1] + readings[i + 1]

Your task is to return the number of stable readings in the array.

A stable reading is not necessarily the same as its neighbors, but it must lie
exactly halfway between the previous and next values. Since only immediate neighbors
matter, each valid index can be checked independently.

If the array has fewer than 3 elements, the answer is 0 because no element has
two neighbors.

Constraints:
- 1 <= readings.length <= 100000
- -100000 <= readings[i] <= 100000
- Expected time complexity: O(n)
- Expected extra space complexity: O(1)
"""

from typing import List


class Solution:
    def count_stable_readings(self, readings: List[int]) -> int:
        """
        Count how many readings are stable.

        A reading at index i is stable if it has both a left and right neighbor
        and its value is exactly the average of those two neighbors.

        Args:
            readings: A list of integers representing temperature readings.

        Returns:
            The number of stable indices in the array.

        Time Complexity:
            O(n), because we scan through the array once.

        Space Complexity:
            O(1), because we use only a few extra variables regardless of input size.
        """
        # If the array has fewer than 3 elements, no index can have both
        # an immediate left neighbor and an immediate right neighbor.
        # That means it is impossible for any reading to be stable.
        if len(readings) < 3:
            return 0

        # This variable will store the total number of stable readings found.
        stable_count: int = 0

        # We only check indices from 1 to len(readings) - 2 inclusive.
        #
        # Why?
        # - Index 0 has no left neighbor.
        # - Index len(readings) - 1 has no right neighbor.
        # So only the "middle" elements are valid candidates.
        for i in range(1, len(readings) - 1):
            # Read the three relevant values:
            # - left neighbor
            # - current reading
            # - right neighbor
            #
            # Storing them in named variables makes the code easier to read,
            # especially for beginners, and helps clearly express the condition.
            left: int = readings[i - 1]
            current: int = readings[i]
            right: int = readings[i + 1]

            # A reading is stable if:
            # current == (left + right) / 2
            #
            # Instead of using division, we rewrite it as:
            # current * 2 == left + right
            #
            # Why is this better?
            # 1. It avoids floating-point arithmetic.
            # 2. It is exact for integers.
            # 3. It is simple and efficient.
            if current * 2 == left + right:
                # If the condition is true, we found one stable reading.
                stable_count += 1

        # After checking every valid index, return the total count.
        return stable_count


if __name__ == "__main__":
    # Create an instance of the solution class so we can call the method.
    solution = Solution()

    # Example 1 from the problem statement.
    readings1: List[int] = [4, 6, 8, 7, 6]
    result1: int = solution.count_stable_readings(readings1)
    print(f"Input: {readings1}")
    print(f"Stable readings count: {result1}")
    print("Expected: 2")
    print()

    # Example 2 from the problem statement.
    readings2: List[int] = [5, 5, 5, 5]
    result2: int = solution.count_stable_readings(readings2)
    print(f"Input: {readings2}")
    print(f"Stable readings count: {result2}")
    print("Expected: 2")
    print()

    # Additional small test: fewer than 3 elements, so answer must be 0.
    readings3: List[int] = [10, 20]
    result3: int = solution.count_stable_readings(readings3)
    print(f"Input: {readings3}")
    print(f"Stable readings count: {result3}")
    print("Expected: 0")
    print()

    # Additional test with negative values.
    readings4: List[int] = [-4, -2, 0, 2, 4]
    result4: int = solution.count_stable_readings(readings4)
    print(f"Input: {readings4}")
    print(f"Stable readings count: {result4}")
    print("Expected: 3")