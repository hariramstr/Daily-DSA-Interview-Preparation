"""
Title: Count Binary IDs With Even Set Bits

Problem Description:
A warehouse system stores item IDs as non-negative integers. For a quick integrity
check, an ID is called valid if its binary representation contains an even number
of 1 bits. Given an integer array ids, return how many IDs are valid.

For example, the number 10 is binary 1010, which contains two 1 bits, so it is
valid. The number 7 is binary 111, which contains three 1 bits, so it is not valid.

Your task is to scan the array and count how many values have even bit parity.
A straightforward solution can examine each number independently and count its
set bits using bit manipulation operations such as shifting or repeatedly clearing
the lowest set bit.

Constraints:
- 1 <= ids.length <= 100000
- 0 <= ids[i] <= 10^9
- The answer fits in a 32-bit integer

Example 1:
Input: ids = [0, 1, 2, 3, 4]
Output: 2
Explanation: 0 has 0 set bits (even), 1 has 1, 2 has 1, 3 has 2 (even), and 4 has 1.
So only 0 and 3 are valid.

Example 2:
Input: ids = [5, 6, 7, 8, 15]
Output: 3
Explanation: 5 is 101 (2 set bits), 6 is 110 (2 set bits), 7 is 111 (3 set bits),
8 is 1000 (1 set bit), and 15 is 1111 (4 set bits). The valid IDs are 5, 6, and 15.
"""

from typing import List


class Solution:
    def has_even_set_bits(self, value: int) -> bool:
        """
        Determine whether a non-negative integer has an even number of set bits.

        Args:
            value: A non-negative integer whose binary representation will be checked.

        Returns:
            True if the number of 1 bits is even, otherwise False.

        Time complexity:
            O(k), where k is the number of set bits in value.

        Space complexity:
            O(1)
        """
        # We will count how many 1 bits appear in the binary representation.
        # Instead of checking every bit position one by one, we use a classic
        # bit manipulation trick:
        #
        #   x & (x - 1)
        #
        # This operation removes the lowest set bit from x.
        #
        # Example:
        #   x = 12 -> binary 1100
        #   x - 1 = 11 -> binary 1011
        #   x & (x - 1) = 1000
        #
        # Notice that one 1 bit disappeared.
        #
        # Why use this approach?
        # - It is efficient.
        # - It loops only once per set bit.
        # - It is a standard and beginner-friendly bit manipulation technique.
        set_bit_count: int = 0

        # We make a local copy so the original input value remains unchanged.
        current: int = value

        # Continue until all set bits have been removed.
        while current > 0:
            # Each iteration removes exactly one 1 bit.
            current &= current - 1

            # Since one set bit was removed, increase the count by 1.
            set_bit_count += 1

        # A number is valid if the total number of set bits is even.
        # Using modulo 2 checks parity:
        # - count % 2 == 0 -> even
        # - count % 2 == 1 -> odd
        return set_bit_count % 2 == 0

    def count_valid_ids(self, ids: List[int]) -> int:
        """
        Count how many IDs have an even number of set bits.

        Args:
            ids: A list of non-negative integers representing item IDs.

        Returns:
            The number of IDs whose binary representation contains an even number of 1 bits.

        Time complexity:
            O(n * k), where n is the number of IDs and k is the average number of set bits
            processed per ID. In the worst case for fixed-size integers, this is efficient.

        Space complexity:
            O(1), excluding the input list.
        """
        # This variable will store the final answer.
        valid_count: int = 0

        # We scan through every ID exactly once.
        # This is the natural choice because the problem asks us to evaluate each number
        # independently and count how many satisfy the condition.
        for item_id in ids:
            # For each ID, determine whether it has even bit parity.
            if self.has_even_set_bits(item_id):
                # If it is valid, add it to the answer.
                valid_count += 1

        # After processing the full list, return the total number of valid IDs.
        return valid_count


if __name__ == "__main__":
    # Create an instance of the solution class so we can call its methods.
    solution = Solution()

    # Sample input from Example 1:
    # 0 -> binary 0 -> 0 set bits -> even -> valid
    # 1 -> binary 1 -> 1 set bit  -> odd  -> invalid
    # 2 -> binary 10 -> 1 set bit -> odd  -> invalid
    # 3 -> binary 11 -> 2 set bits -> even -> valid
    # 4 -> binary 100 -> 1 set bit -> odd -> invalid
    # Expected result: 2
    ids_example_1: List[int] = [0, 1, 2, 3, 4]
    result_1: int = solution.count_valid_ids(ids_example_1)
    print("Example 1 Input:", ids_example_1)
    print("Example 1 Output:", result_1)
    print("Expected Output: 2")
    print()

    # Sample input from Example 2:
    # 5  -> binary 101  -> 2 set bits -> even -> valid
    # 6  -> binary 110  -> 2 set bits -> even -> valid
    # 7  -> binary 111  -> 3 set bits -> odd  -> invalid
    # 8  -> binary 1000 -> 1 set bit  -> odd  -> invalid
    # 15 -> binary 1111 -> 4 set bits -> even -> valid
    # Expected result: 3
    ids_example_2: List[int] = [5, 6, 7, 8, 15]
    result_2: int = solution.count_valid_ids(ids_example_2)
    print("Example 2 Input:", ids_example_2)
    print("Example 2 Output:", result_2)
    print("Expected Output: 3")
    print()

    # Additional small custom test to help beginners see more behavior.
    custom_ids: List[int] = [10, 7, 12, 0]
    # 10 -> 1010 -> 2 set bits -> valid
    # 7  -> 111  -> 3 set bits -> invalid
    # 12 -> 1100 -> 2 set bits -> valid
    # 0  -> 0    -> 0 set bits -> valid
    # Expected result: 3
    custom_result: int = solution.count_valid_ids(custom_ids)
    print("Custom Input:", custom_ids)
    print("Custom Output:", custom_result)
    print("Expected Output: 3")