"""
Title: Minimum Rafts to Carry River Hikers

Problem Description:
A tour company needs to transport hikers across a river using small rafts.
Each raft can carry at most two hikers, and the combined weight of the hikers
on a raft cannot exceed a given limit.

You are given an integer array weights where weights[i] is the weight of the
i-th hiker, and an integer limit representing the maximum total weight a single
raft can safely carry.

Return the minimum number of rafts required to carry every hiker across the river.

A raft may carry either one hiker or two hikers. Every hiker must be assigned
to exactly one raft. You may pair hikers in any way you want, as long as the
weight limit is not exceeded.

This problem is designed to test whether you can recognize when sorting and a
two-pointer strategy lead to an optimal greedy solution. A correct solution
should be efficient enough for large inputs.

Constraints:
- 1 <= weights.length <= 100000
- 1 <= weights[i] <= limit <= 100000
- It is guaranteed that each individual hiker can fit on a raft alone.

Example 1:
Input: weights = [3, 5, 3, 4], limit = 5
Output: 4

Example 2:
Input: weights = [1, 2, 2, 3], limit = 3
Output: 3
"""

from typing import List


class Solution:
    def numRescueRafts(self, weights: List[int], limit: int) -> int:
        """
        Compute the minimum number of rafts needed to carry all hikers.

        The algorithm sorts the weights, then uses two pointers:
        - One pointer starts at the lightest hiker.
        - One pointer starts at the heaviest hiker.

        On each step, we always place the heaviest remaining hiker onto a raft,
        because that hiker is the hardest to pair. If the lightest remaining
        hiker can share the raft with the heaviest without exceeding the limit,
        we pair them together. Otherwise, the heaviest rides alone.

        Args:
            weights: A list of hiker weights.
            limit: The maximum total weight a single raft can carry.

        Returns:
            The minimum number of rafts required.

        Time complexity:
            O(n log n), due to sorting.

        Space complexity:
            O(1) extra space beyond the sort implementation details.
            Note: Python's sorting may use additional internal memory.
        """
        # Step 1: Sort the weights in non-decreasing order.
        #
        # Why sort?
        # Sorting allows us to efficiently consider the lightest and heaviest
        # remaining hikers at the same time.
        #
        # This is the key insight:
        # - The heaviest person is the most difficult to place.
        # - If the heaviest person cannot pair with the lightest person,
        #   then the heaviest person cannot pair with anyone at all,
        #   because everyone else is heavier than the lightest.
        #
        # That makes a greedy decision safe and optimal.
        weights.sort()

        # 'left' points to the lightest remaining hiker.
        left: int = 0

        # 'right' points to the heaviest remaining hiker.
        right: int = len(weights) - 1

        # This will count how many rafts we use.
        rafts: int = 0

        # Step 2: Process hikers until all have been assigned to rafts.
        #
        # The loop continues while there are still unassigned hikers.
        # Specifically, while the left pointer has not crossed the right pointer.
        while left <= right:
            # Every iteration uses exactly one raft.
            #
            # Why?
            # Because we always assign the heaviest remaining hiker (at index 'right')
            # to a raft during this iteration.
            rafts += 1

            # Check whether the lightest and heaviest remaining hikers can share a raft.
            #
            # If their combined weight is within the limit, we should pair them.
            # This is optimal because:
            # - We successfully place the heaviest hiker.
            # - We also use the remaining capacity as efficiently as possible
            #   by adding the lightest hiker.
            #
            # If they cannot fit together, then the heaviest must go alone.
            if weights[left] + weights[right] <= limit:
                # The lightest hiker is now assigned together with the heaviest,
                # so move the left pointer inward.
                left += 1

            # In all cases, the heaviest hiker at 'right' is assigned in this raft,
            # either alone or paired with the lightest.
            # So we always move the right pointer inward.
            right -= 1

        # After the loop finishes, every hiker has been assigned to a raft.
        return rafts


if __name__ == "__main__":
    """
    Run sample test cases from the problem statement and print the results.
    """

    solution = Solution()

    # Example 1
    #
    # weights = [3, 5, 3, 4], limit = 5
    #
    # Sorted: [3, 3, 4, 5]
    #
    # Step-by-step:
    # - Heaviest = 5, lightest = 3 -> 3 + 5 = 8 > 5, so 5 goes alone. Rafts = 1
    # - Heaviest = 4, lightest = 3 -> 3 + 4 = 7 > 5, so 4 goes alone. Rafts = 2
    # - Heaviest = 3, lightest = 3 -> 3 + 3 = 6 > 5, so one 3 goes alone. Rafts = 3
    # - Remaining 3 goes alone. Rafts = 4
    #
    # Expected output: 4
    weights1: List[int] = [3, 5, 3, 4]
    limit1: int = 5
    result1: int = solution.numRescueRafts(weights1, limit1)
    print("Example 1 Result:", result1)  # Expected: 4

    # Example 2
    #
    # weights = [1, 2, 2, 3], limit = 3
    #
    # Sorted: [1, 2, 2, 3]
    #
    # Step-by-step:
    # - Heaviest = 3, lightest = 1 -> 1 + 3 = 4 > 3, so 3 goes alone. Rafts = 1
    # - Heaviest = 2, lightest = 1 -> 1 + 2 = 3 <= 3, so pair (1, 2). Rafts = 2
    # - Remaining 2 goes alone. Rafts = 3
    #
    # Expected output: 3
    weights2: List[int] = [1, 2, 2, 3]
    limit2: int = 3
    result2: int = solution.numRescueRafts(weights2, limit2)
    print("Example 2 Result:", result2)  # Expected: 3

    # Additional quick check
    weights3: List[int] = [2, 2]
    limit3: int = 4
    result3: int = solution.numRescueRafts(weights3, limit3)
    print("Additional Example Result:", result3)  # Expected: 1