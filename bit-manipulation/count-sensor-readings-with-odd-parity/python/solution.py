"""
Title: Count Sensor Readings With Odd Parity

Difficulty: Easy
Topic: Bit Manipulation

Problem Description:
You are given an integer array readings, where each value represents a compact binary
reading produced by a sensor. A reading is considered odd-parity if its binary
representation contains an odd number of 1 bits. Your task is to return how many
readings in the array are odd-parity.

For example, the number 5 has binary form 101, which contains two 1 bits, so it is
not odd-parity. The number 7 has binary form 111, which contains three 1 bits, so it
is odd-parity.

Write a function that counts how many numbers in the array satisfy this condition.
The expected solution should use bit manipulation rather than converting numbers to
strings.

Constraints:
- 1 <= readings.length <= 100000
- 0 <= readings[i] <= 10^9
- An O(n * number_of_bits) solution is acceptable

Example 1:
Input: readings = [1, 2, 3, 4]
Output: 3
Explanation:
- 1 -> 1 has 1 set bit, odd
- 2 -> 10 has 1 set bit, odd
- 3 -> 11 has 2 set bits, even
- 4 -> 100 has 1 set bit, odd
So the answer is 3.

Example 2:
Input: readings = [0, 5, 7, 8, 10]
Output: 2
Explanation:
- 0 -> 0 set bits, even
- 5 -> 101 has 2 set bits, even
- 7 -> 111 has 3 set bits, odd
- 8 -> 1000 has 1 set bit, odd
- 10 -> 1010 has 2 set bits, even
There are 2 odd-parity readings in total.
"""

from typing import List


class Solution:
    def has_odd_parity(self, value: int) -> bool:
        """
        Determine whether a number has an odd number of set bits (1s) in binary.

        Args:
            value: A non-negative integer sensor reading.

        Returns:
            True if the number of 1 bits is odd, otherwise False.

        Time complexity:
            O(number_of_set_bits) using Brian Kernighan's bit trick.

        Space complexity:
            O(1)
        """
        # We will track parity instead of storing the full count.
        # Why?
        # Because the problem only asks whether the number of set bits is odd or even.
        # That means we do not actually need the exact count like 1, 2, 3, 4, ...
        # We only need to know whether the count flips between:
        # - even parity
        # - odd parity
        #
        # We start with parity = 0, meaning "even so far".
        # Every time we find one set bit, parity flips:
        # 0 -> 1
        # 1 -> 0
        #
        # This is a very natural fit for XOR with 1:
        # parity ^= 1
        parity: int = 0

        # We use Brian Kernighan's algorithm:
        #   value &= value - 1
        #
        # This operation removes the lowest set bit from the number.
        #
        # Example:
        # value = 12 -> binary 1100
        # value - 1 = 11 -> binary 1011
        # 1100 & 1011 = 1000
        #
        # One set bit has been removed.
        #
        # This is efficient because the loop runs only once per set bit,
        # not once per total bit position.
        while value > 0:
            # Remove one set bit.
            value &= value - 1

            # Since we removed exactly one 1 bit, we flip parity.
            parity ^= 1

        # If parity is 1, the number had an odd count of set bits.
        return parity == 1

    def count_odd_parity_readings(self, readings: List[int]) -> int:
        """
        Count how many readings have an odd number of set bits in binary.

        Args:
            readings: A list of non-negative integers representing sensor readings.

        Returns:
            The number of readings whose binary representation contains an odd
            number of 1 bits.

        Time complexity:
            O(n * number_of_set_bits_per_value) in practice, or
            O(n * number_of_bits) in the worst case.

        Space complexity:
            O(1) extra space, excluding input storage.
        """
        # This variable will store the final answer:
        # how many readings are odd-parity.
        odd_count: int = 0

        # We process each reading one by one.
        #
        # Why a simple loop?
        # - The task is independent for each number.
        # - No sorting or extra data structure is needed.
        # - This keeps the solution easy to understand and memory efficient.
        for reading in readings:
            # For each reading, determine whether it has odd parity.
            if self.has_odd_parity(reading):
                # If yes, increase our answer by 1.
                odd_count += 1

        # After checking all readings, return the total.
        return odd_count


if __name__ == "__main__":
    solution = Solution()

    # Example 1:
    # 1  -> 1    -> 1 set bit  -> odd
    # 2  -> 10   -> 1 set bit  -> odd
    # 3  -> 11   -> 2 set bits -> even
    # 4  -> 100  -> 1 set bit  -> odd
    # Total odd-parity readings = 3
    readings1: List[int] = [1, 2, 3, 4]
    result1: int = solution.count_odd_parity_readings(readings1)
    print(f"Input: {readings1}")
    print(f"Output: {result1}")
    print("Expected: 3")
    print()

    # Example 2:
    # 0  -> 0     -> 0 set bits -> even
    # 5  -> 101   -> 2 set bits -> even
    # 7  -> 111   -> 3 set bits -> odd
    # 8  -> 1000  -> 1 set bit  -> odd
    # 10 -> 1010  -> 2 set bits -> even
    # Total odd-parity readings = 2
    readings2: List[int] = [0, 5, 7, 8, 10]
    result2: int = solution.count_odd_parity_readings(readings2)
    print(f"Input: {readings2}")
    print(f"Output: {result2}")
    print("Expected: 2")
    print()

    # Additional quick sanity checks for beginners:
    extra_readings: List[int] = [0, 1, 5, 7, 15]
    # 0  -> even
    # 1  -> odd
    # 5  -> even (101 has 2 ones)
    # 7  -> odd  (111 has 3 ones)
    # 15 -> even (1111 has 4 ones)
    # Total = 2
    extra_result: int = solution.count_odd_parity_readings(extra_readings)
    print(f"Input: {extra_readings}")
    print(f"Output: {extra_result}")
    print("Expected: 2")