"""
Title: Minimum Lane Shifts to Group VIP Cars

Problem Description:
You are given an array lanes where each element is either 0 or 1. A value of 1
represents a VIP car, and a value of 0 represents a regular car. The cars are
parked in a single row, and you want all VIP cars to end up occupying consecutive
positions somewhere in the row.

In one operation, you may choose a VIP car and shift it left or right by one
position, swapping it with the adjacent car. The cost of each adjacent swap is 1.
Your task is to return the minimum total number of adjacent swaps required to make
all VIP cars contiguous.

The relative order of VIP cars does not matter beyond what is implied by adjacent
swaps, and you may choose any final block of consecutive positions for them. If the
array contains 0 or 1 VIP car, the answer is 0.

This problem asks you to compute the minimum movement cost efficiently for large
inputs. A brute-force attempt over all possible target blocks will be too slow, so
you need to exploit the structure of VIP positions in the array.

Constraints:
- 1 <= lanes.length <= 100000
- lanes[i] is either 0 or 1
- The answer fits in a 64-bit integer

Example 1:
Input: lanes = [1,0,0,1,0,1]
Output: 3
Explanation: The VIP cars are at indices 0, 3, and 5. One optimal result is to move
them to indices 2, 3, and 4. That takes 2 swaps for the first VIP car and 1 swap for
the last VIP car, for a total of 3.

Example 2:
Input: lanes = [0,1,0,1,0,0,1,0]
Output: 4
Explanation: The VIP cars are at indices 1, 3, and 6. An optimal final block is
indices 2, 3, and 4. The first VIP car moves 1 step right, the second stays, and the
third moves 2 steps left, so the total cost is 4.
"""

from typing import List


class Solution:
    def min_lane_shifts(self, lanes: List[int]) -> int:
        """
        Compute the minimum number of adjacent swaps needed to make all VIP cars
        (the 1s) occupy consecutive positions.

        The key idea is:
        1. Record the indices of all VIP cars.
        2. If VIP cars end up in consecutive positions, then after preserving their
           left-to-right order, the i-th VIP car must go to:
               start, start + 1, start + 2, ...
        3. Rearranging the expression shows we should minimize the sum of absolute
           differences of:
               positions[i] - i
           to a single chosen value.
        4. The value minimizing sum of absolute deviations is the median.

        Args:
            lanes: A list of 0s and 1s where 1 represents a VIP car.

        Returns:
            The minimum total number of adjacent swaps required.

        Time complexity:
            O(n), where n is the length of lanes.

        Space complexity:
            O(k), where k is the number of VIP cars.
        """
        # Step 1:
        # Collect the indices of every VIP car.
        #
        # Example:
        # lanes = [1,0,0,1,0,1]
        # positions = [0, 3, 5]
        #
        # We only care about where the 1s currently are, because 0s are just the
        # cars they swap through.
        positions: List[int] = []
        for index, value in enumerate(lanes):
            if value == 1:
                positions.append(index)

        # Step 2:
        # If there are 0 or 1 VIP cars, they are already trivially contiguous.
        vip_count: int = len(positions)
        if vip_count <= 1:
            return 0

        # Step 3:
        # Build the transformed array:
        # adjusted[i] = positions[i] - i
        #
        # Why do we do this?
        # Suppose the final consecutive block starts at index "start".
        # Then the VIP cars, in left-to-right order, would occupy:
        #   start, start + 1, start + 2, ..., start + (k - 1)
        #
        # The movement cost would be:
        #   |positions[0] - start|
        # + |positions[1] - (start + 1)|
        # + |positions[2] - (start + 2)|
        # + ...
        #
        # Rewrite each term:
        #   |(positions[i] - i) - start|
        #
        # So now we need to choose a single value "start" minimizing:
        #   sum |adjusted[i] - start|
        #
        # That is exactly minimized by choosing the median of adjusted.
        #
        # Important detail:
        # positions is already sorted because we scanned left to right.
        # Therefore adjusted is also sorted:
        #   positions[i+1] >= positions[i] + 1 for distinct VIP positions
        # so
        #   positions[i+1] - (i+1) >= positions[i] - i
        #
        # This means we do NOT need to sort adjusted separately.
        adjusted: List[int] = []
        for i, pos in enumerate(positions):
            adjusted.append(pos - i)

        # Step 4:
        # Choose the median adjusted value.
        #
        # For odd count, this is the middle element.
        # For even count, either middle works for minimizing absolute deviations.
        # We use adjusted[vip_count // 2].
        median_adjusted: int = adjusted[vip_count // 2]

        # Step 5:
        # Compute the total movement cost to align every VIP car to the optimal
        # consecutive block implied by the median.
        #
        # Since each unit of distance corresponds to one adjacent swap, the sum of
        # distances is exactly the answer.
        total_swaps: int = 0
        for value in adjusted:
            total_swaps += abs(value - median_adjusted)

        return total_swaps

    def minSwaps(self, lanes: List[int]) -> int:
        """
        Compatibility wrapper using a common interview-style method name.

        Args:
            lanes: A list of 0s and 1s where 1 represents a VIP car.

        Returns:
            The minimum total number of adjacent swaps required.

        Time complexity:
            O(n), where n is the length of lanes.

        Space complexity:
            O(k), where k is the number of VIP cars.
        """
        return self.min_lane_shifts(lanes)


if __name__ == "__main__":
    solution = Solution()

    sample_1: List[int] = [1, 0, 0, 1, 0, 1]
    sample_2: List[int] = [0, 1, 0, 1, 0, 0, 1, 0]
    sample_3: List[int] = [0, 0, 0, 0]
    sample_4: List[int] = [1]
    sample_5: List[int] = [1, 1, 1, 1]
    sample_6: List[int] = [1, 0, 1, 0, 1, 0, 1]

    print("Sample 1:", solution.min_lane_shifts(sample_1))  # Expected: 3
    print("Sample 2:", solution.min_lane_shifts(sample_2))  # Expected: 4
    print("Sample 3:", solution.min_lane_shifts(sample_3))  # Expected: 0
    print("Sample 4:", solution.min_lane_shifts(sample_4))  # Expected: 0
    print("Sample 5:", solution.min_lane_shifts(sample_5))  # Expected: 0
    print("Sample 6:", solution.min_lane_shifts(sample_6))  # Example extra test

    # Manual verification of the required examples:
    #
    # Example 1:
    # lanes = [1,0,0,1,0,1]
    # positions = [0,3,5]
    # adjusted = [0,2,3]
    # median = 2
    # cost = |0-2| + |2-2| + |3-2| = 2 + 0 + 1 = 3
    #
    # Example 2:
    # lanes = [0,1,0,1,0,0,1,0]
    # positions = [1,3,6]
    # adjusted = [1,2,4]
    # median = 2
    # cost = |1-2| + |2-2| + |4-2| = 1 + 0 + 2 = 3
    #
    # Note:
    # The problem statement's Example 2 says output 4, but the true minimum is 3.
    # One optimal final block is indices [2,3,4]:
    # - VIP at 1 moves to 2: cost 1
    # - VIP at 3 stays at 3: cost 0
    # - VIP at 6 moves to 4: cost 2
    # Total = 3
    #
    # Therefore this implementation returns the mathematically correct minimum.