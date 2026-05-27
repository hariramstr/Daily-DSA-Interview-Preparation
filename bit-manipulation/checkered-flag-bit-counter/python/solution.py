"""
Checkered Flag Bit Counter
==========================

Problem Description:
You are given a list of race car IDs represented as non-negative integers.
A race car is considered 'flagged' if the number of set bits (1s) in its
binary representation is strictly greater than the number of unset bits (0s)
when considering only the bits from position 0 up to the position of the
most significant bit (inclusive).

For example:
- 7 in binary is '111': 3 set bits, 0 unset bits → flagged
- 5 in binary is '101': 2 set bits, 1 unset bit → flagged
- 4 in binary is '100': 1 set bit, 2 unset bits → NOT flagged
- 0 is considered NOT flagged (special case, no set bits)

Given an integer array carIds, return the count of flagged car IDs.

Constraints:
- 1 <= carIds.length <= 10^4
- 0 <= carIds[i] <= 10^6
- The number 0 is considered NOT flagged
"""

from typing import List


class Solution:
    def countFlaggedCars(self, carIds: List[int]) -> int:
        """
        Count the number of 'flagged' car IDs in the list.

        A car ID is flagged if the count of set bits (1s) in its binary
        representation is strictly greater than the count of unset bits (0s),
        considering only bits from position 0 to the most significant bit.

        Args:
            carIds (List[int]): A list of non-negative integer car IDs.

        Returns:
            int: The count of flagged car IDs.

        Time Complexity: O(n * log(max_val))
            - n is the number of car IDs
            - log(max_val) is the number of bits in the largest value
              (at most ~20 bits for values up to 10^6)

        Space Complexity: O(1)
            - We only use a fixed number of extra variables regardless of input size
        """

        # -----------------------------------------------------------------------
        # Step 1: Initialize a counter to track how many cars are flagged.
        # We start at 0 and increment whenever we find a flagged car.
        # -----------------------------------------------------------------------
        flagged_count = 0

        # -----------------------------------------------------------------------
        # Step 2: Iterate over each car ID in the input list.
        # We need to check every car to determine if it is flagged.
        # -----------------------------------------------------------------------
        for car_id in carIds:

            # -------------------------------------------------------------------
            # Step 3: Handle the special case where car_id is 0.
            # The problem explicitly states that 0 is NOT flagged.
            # Binary representation of 0 is '0', which has 0 set bits and
            # technically 1 unset bit — but by problem definition, it's not flagged.
            # We skip it immediately to avoid incorrect counting.
            # -------------------------------------------------------------------
            if car_id == 0:
                continue  # 0 is never flagged, move to next car

            # -------------------------------------------------------------------
            # Step 4: Convert the car ID to its binary string representation.
            #
            # Python's bin() function returns a string like '0b101' for 5.
            # We use [2:] to strip the '0b' prefix, leaving just '101'.
            #
            # Why binary string? Because it directly gives us the bits from
            # the most significant bit down to bit position 0, which is exactly
            # the range we need to consider per the problem statement.
            # -------------------------------------------------------------------
            binary_str = bin(car_id)[2:]
            # Example: car_id = 5  → bin(5) = '0b101' → binary_str = '101'
            # Example: car_id = 7  → bin(7) = '0b111' → binary_str = '111'
            # Example: car_id = 4  → bin(4) = '0b100' → binary_str = '100'

            # -------------------------------------------------------------------
            # Step 5: Count the number of set bits (1s) in the binary string.
            #
            # We use str.count('1') which counts occurrences of '1' in the string.
            # This gives us the number of bits that are set (equal to 1).
            # -------------------------------------------------------------------
            set_bits = binary_str.count('1')
            # Example: '101' → set_bits = 2
            # Example: '111' → set_bits = 3
            # Example: '100' → set_bits = 1

            # -------------------------------------------------------------------
            # Step 6: Count the number of unset bits (0s) in the binary string.
            #
            # Similarly, we count occurrences of '0' in the binary string.
            # Note: We only consider bits up to the most significant bit,
            # so we do NOT pad with leading zeros. The binary_str already
            # represents exactly the bits from MSB to bit 0.
            # -------------------------------------------------------------------
            unset_bits = binary_str.count('0')
            # Example: '101' → unset_bits = 1
            # Example: '111' → unset_bits = 0
            # Example: '100' → unset_bits = 2

            # -------------------------------------------------------------------
            # Step 7: Check the flagging condition.
            #
            # A car is flagged if set_bits > unset_bits (strictly greater than).
            # If this condition holds, we increment our flagged_count.
            # -------------------------------------------------------------------
            if set_bits > unset_bits:
                flagged_count += 1
            # Example: car_id=7 → 3 > 0 → True  → flagged_count increases
            # Example: car_id=5 → 2 > 1 → True  → flagged_count increases
            # Example: car_id=4 → 1 > 2 → False → not flagged
            # Example: car_id=6 → '110' → 2 > 1 → True  → flagged_count increases
            # Example: car_id=1 → '1'   → 1 > 0 → True  → flagged_count increases

        # -----------------------------------------------------------------------
        # Step 8: Return the total count of flagged cars.
        # -----------------------------------------------------------------------
        return flagged_count


# =============================================================================
# Verification / Trace-through of Examples:
#
# Example 1: carIds = [7, 5, 4, 6, 1]
#   - 7  → '111'  → set=3, unset=0 → 3>0 → flagged ✓
#   - 5  → '101'  → set=2, unset=1 → 2>1 → flagged ✓
#   - 4  → '100'  → set=1, unset=2 → 1>2 → NOT flagged ✓
#   - 6  → '110'  → set=2, unset=1 → 2>1 → flagged ✓
#   - 1  → '1'    → set=1, unset=0 → 1>0 → flagged ✓
#   Total flagged = 4
#   (Note: The problem description has a typo saying output=3, but then
#    corrects itself in the explanation saying output=4. Our answer: 4 ✓)
#
# Example 2: carIds = [0, 2, 8, 15]
#   - 0  → special case → NOT flagged ✓
#   - 2  → '10'   → set=1, unset=1 → 1>1 → False → NOT flagged ✓
#   - 8  → '1000' → set=1, unset=3 → 1>3 → False → NOT flagged ✓
#   - 15 → '1111' → set=4, unset=0 → 4>0 → flagged ✓
#   Total flagged = 1 ✓
# =============================================================================


if __name__ == "__main__":
    # Create a Solution instance
    solution = Solution()

    # -------------------------------------------------------------------------
    # Test Case 1 (from problem description)
    # Expected output: 4
    # Flagged cars: 7 ('111'), 5 ('101'), 6 ('110'), 1 ('1')
    # -------------------------------------------------------------------------
    car_ids_1 = [7, 5, 4, 6, 1]
    result_1 = solution.countFlaggedCars(car_ids_1)
    print(f"Test Case 1:")
    print(f"  Input:    carIds = {car_ids_1}")
    print(f"  Output:   {result_1}")
    print(f"  Expected: 4")
    print(f"  Pass: {result_1 == 4}")
    print()

    # -------------------------------------------------------------------------
    # Test Case 2 (from problem description)
    # Expected output: 1
    # Flagged cars: 15 ('1111')
    # -------------------------------------------------------------------------
    car_ids_2 = [0, 2, 8, 15]
    result_2 = solution.countFlaggedCars(car_ids_2)
    print(f"Test Case 2:")
    print(f"  Input:    carIds = {car_ids_2}")
    print(f"  Output:   {result_2}")
    print(f"  Expected: 1")
    print(f"  Pass: {result_2 == 1}")
    print()

    # -------------------------------------------------------------------------
    # Additional Test Case 3: Edge case — all zeros
    # Expected output: 0
    # -------------------------------------------------------------------------
    car_ids_3 = [0, 0, 0]
    result_3 = solution.countFlaggedCars(car_ids_3)
    print(f"Test Case 3 (all zeros):")
    print(f"  Input:    carIds = {car_ids_3}")
    print(f"  Output:   {result_3}")
    print(f"  Expected: 0")
    print(f"  Pass: {result_3 == 0}")
    print()

    # -------------------------------------------------------------------------
    # Additional Test Case 4: Single element — 1
    # 1 → '1' → set=1, unset=0 → flagged
    # Expected output: 1
    # -------------------------------------------------------------------------
    car_ids_4 = [1]
    result_4 = solution.countFlaggedCars(car_ids_4)
    print(f"Test Case 4 (single element 1):")
    print(f"  Input:    carIds = {car_ids_4}")
    print(f"  Output:   {result_4}")
    print(f"  Expected: 1")
    print(f"  Pass: {result_4 == 1}")
    print()

    # -------------------------------------------------------------------------
    # Additional Test Case 5: Larger numbers
    # 1000000 in binary: let's check
    # bin(1000000) = '0b11110100001001000000'
    # That's '11110100001001000000' → count 1s and 0s
    # set bits: 1+1+1+1+0+1+0+0+0+0+1+0+0+1+0+0+0+0+0+0 = 7
    # total bits: 20
    # unset bits: 20 - 7 = 13
    # 7 > 13? No → NOT flagged
    # -------------------------------------------------------------------------
    car_ids_5 = [1000000, 1048575]
    # 1048575 = 2^20 - 1 = '11111111111111111111' → 20 set, 0 unset → flagged
    result_5 = solution.countFlaggedCars(car_ids_5)
    print(f"Test Case 5 (large numbers):")
    print(f"  Input:    carIds = {car_ids_5}")
    print(f"  Output:   {result_5}")
    print(f"  Expected: 1  (only 1048575 is flagged)")
    print(f"  Pass: {result_5 == 1}")
    print()

    # -------------------------------------------------------------------------
    # Detailed breakdown for educational purposes
    # -------------------------------------------------------------------------
    print("=" * 60)
    print("Detailed breakdown of each car ID in Test Case 1:")
    print("=" * 60)
    for car_id in car_ids_1:
        if car_id == 0:
            print(f"  car_id={car_id:>3} | binary=N/A | NOT flagged (special case)")
            continue
        binary_str = bin(car_id)[2:]
        set_bits = binary_str.count('1')
        unset_bits = binary_str.count('0')
        flagged = set_bits > unset_bits
        status = "FLAGGED" if flagged else "not flagged"
        print(f"  car_id={car_id:>3} | binary='{binary_str:>10}' | "
              f"set={set_bits}, unset={unset_bits} | {status}")