"""
Title: Find the First Missing Checkpoint Number

Problem Description:
A delivery company labels route checkpoints with positive integer IDs starting from 1.
After syncing data from a driver's device, you receive an unsorted array `checkpoints`
containing the IDs that were recorded during the trip. Some IDs may appear more than
once because of duplicate scans, and some values may be invalid, such as `0` or
negative numbers.

Your task is to return the smallest positive checkpoint ID that does not appear in the
array. In other words, find the first missing positive integer in the recorded data.

This problem is useful for validating whether the earliest expected checkpoint was
skipped or never uploaded. Only positive IDs matter. Duplicates do not change the
answer, and invalid values should be ignored.

You should design a solution that works efficiently for typical interview constraints.

Constraints:
- `1 <= checkpoints.length <= 10^5`
- `-10^5 <= checkpoints[i] <= 10^5`
- The array may contain duplicates
- The array is not guaranteed to be sorted

Example 1:
Input: checkpoints = [3, 4, -1, 1]
Output: 2
Explanation: Positive IDs 1, 3, and 4 are present, but 2 is missing, so the answer is 2.

Example 2:
Input: checkpoints = [1, 2, 2, 5]
Output: 3
Explanation: IDs 1 and 2 are present. The smallest missing positive checkpoint ID is 3.
"""

from typing import List


class Solution:
    def first_missing_positive(self, checkpoints: List[int]) -> int:
        """
        Find the smallest missing positive integer from an unsorted list.

        This method uses the in-place cyclic positioning idea:
        if a value x is in the valid range [1, n], then it ideally belongs
        at index x - 1. We repeatedly swap values into their correct positions.
        After that rearrangement, the first index i where checkpoints[i] != i + 1
        reveals the missing positive integer.

        Args:
            checkpoints: A list of integers that may contain positives, duplicates,
                zeros, and negative values.

        Returns:
            The smallest positive integer that does not appear in the list.

        Time complexity:
            O(n), because each element is moved at most a constant number of times.

        Space complexity:
            O(1), because the rearrangement is done in place without extra data
            structures proportional to input size.
        """
        # Store the length once so we do not repeatedly call len(checkpoints).
        # This also makes the code easier to read.
        n: int = len(checkpoints)

        # ---------------------------------------------------------------------
        # STEP 1: Place each valid positive number in its "correct" index.
        #
        # Key idea:
        # - If the array length is n, then the answer must be in the range [1, n + 1].
        # - Therefore, only values from 1 to n are useful for direct placement.
        # - A value x should ideally be placed at index x - 1.
        #
        # Example:
        # - Value 1 belongs at index 0
        # - Value 2 belongs at index 1
        # - Value 3 belongs at index 2
        #
        # We use a while loop instead of a single swap per index because after
        # swapping, the new value that arrives at the current index may also need
        # to be moved to its correct place.
        #
        # We must also avoid infinite loops caused by duplicates. For example,
        # if checkpoints[i] == checkpoints[correct_index], swapping would do nothing,
        # so we stop in that case.
        # ---------------------------------------------------------------------
        for i in range(n):
            # Continue swapping while the current value is:
            # 1. Positive
            # 2. Within the useful range [1, n]
            # 3. Not already in its correct position
            #
            # The target index for value v is v - 1.
            while (
                1 <= checkpoints[i] <= n
                and checkpoints[i] != checkpoints[checkpoints[i] - 1]
            ):
                # Compute the index where the current value should go.
                correct_index: int = checkpoints[i] - 1

                # Swap the current value into its correct position.
                #
                # Why swap?
                # Because we want to rearrange the array so that if value x exists,
                # it ends up at index x - 1.
                #
                # This is efficient because it uses the input array itself as the
                # placement structure, avoiding extra memory like a set or boolean array.
                checkpoints[i], checkpoints[correct_index] = (
                    checkpoints[correct_index],
                    checkpoints[i],
                )

        # ---------------------------------------------------------------------
        # STEP 2: Scan the array from left to right.
        #
        # After the placement phase:
        # - If value 1 exists, it should be at index 0
        # - If value 2 exists, it should be at index 1
        # - If value 3 exists, it should be at index 2
        # and so on...
        #
        # Therefore, the first index i where checkpoints[i] != i + 1 tells us
        # that the value i + 1 is missing.
        # ---------------------------------------------------------------------
        for i in range(n):
            expected_value: int = i + 1

            # If the current index does not contain the expected value,
            # then expected_value is the smallest missing positive integer.
            if checkpoints[i] != expected_value:
                return expected_value

        # ---------------------------------------------------------------------
        # STEP 3: If every position 0..n-1 contains the correct values 1..n,
        # then the array contains all positive integers from 1 to n.
        #
        # In that case, the smallest missing positive must be n + 1.
        #
        # Example:
        # checkpoints = [1, 2, 3]
        # All values 1..3 are present, so the answer is 4.
        # ---------------------------------------------------------------------
        return n + 1


if __name__ == "__main__":
    solution = Solution()

    # Example 1:
    # Input: [3, 4, -1, 1]
    # Expected output: 2
    #
    # Quick trace:
    # - Valid positives are 1, 3, 4
    # - After placement, index 0 should hold 1, index 1 should hold 2, etc.
    # - The first missing positive is 2.
    checkpoints_1: List[int] = [3, 4, -1, 1]
    result_1: int = solution.first_missing_positive(checkpoints_1[:])
    print(f"Input: {checkpoints_1}")
    print(f"Output: {result_1}")
    print("Expected: 2")
    print()

    # Example 2:
    # Input: [1, 2, 2, 5]
    # Expected output: 3
    #
    # Quick trace:
    # - 1 is present
    # - 2 is present (duplicate does not matter)
    # - 3 is missing
    checkpoints_2: List[int] = [1, 2, 2, 5]
    result_2: int = solution.first_missing_positive(checkpoints_2[:])
    print(f"Input: {checkpoints_2}")
    print(f"Output: {result_2}")
    print("Expected: 3")
    print()

    # Additional beginner-friendly test cases:
    additional_tests: List[List[int]] = [
        [1],            # Missing positive should be 2
        [2],            # Missing positive should be 1
        [1, 2, 3],      # Missing positive should be 4
        [7, 8, 9, 11],  # Missing positive should be 1
        [0, -1, -2],    # Missing positive should be 1
        [2, 1],         # Missing positive should be 3
    ]

    for test in additional_tests:
        print(f"Input: {test}")
        print(f"First missing positive: {solution.first_missing_positive(test[:])}")
        print()