"""
Title: Minimum Escalations to Meet Boat Departure Deadlines

Problem Description:
A harbor dispatch system tracks cargo crates waiting to be loaded onto boats.
You are given an integer array weights where weights[i] is the weight of the i-th crate.
Each boat can carry at most two crates, and the combined weight of the two crates on the
same boat cannot exceed limit. A crate may also be loaded alone.

Normally, every crate must be assigned to some boat. However, the harbor has a costly
manual escalation process that allows a crate to be split and shipped separately outside
the normal loading system. You may escalate any number of crates, but each escalated crate
counts as 1 escalation and is removed before boat assignment. Your goal is to minimize the
number of escalations so that all remaining crates can be assigned to boats under the
two-crate and weight-limit rules using any number of boats.

Return the minimum number of escalations required.

This is not the same as minimizing the number of boats. You may use as many boats as needed
after removing some crates, but every non-escalated crate must fit on a boat either alone
or paired with another crate. Since a single crate can ride alone, the only crates that
force escalation are those whose individual weight already exceeds limit. Still, the input
size is large enough that your solution should be efficient and should make use of sorting
and a two-pointer style argument rather than simulation.

Constraints:
- 1 <= weights.length <= 200000
- 1 <= weights[i] <= 10^9
- 1 <= limit <= 10^9
"""

from typing import List


class Solution:
    def minimum_escalations(self, weights: List[int], limit: int) -> int:
        """
        Compute the minimum number of crates that must be escalated.

        The key observation is:
        - Any crate with weight <= limit can always be placed on a boat alone.
        - Therefore, such a crate never forces escalation.
        - A crate must be escalated if and only if its individual weight is greater than limit.

        We sort the array and use a two-pointer style scan to emphasize the structure of the
        feasible set: all crates from the left up to some boundary are valid, and all crates
        beyond that boundary are too heavy and must be escalated.

        Args:
            weights: List of crate weights.
            limit: Maximum weight a boat can carry.

        Returns:
            The minimum number of escalations required.

        Time complexity:
            O(n log n) due to sorting.

        Space complexity:
            O(1) extra space beyond sorting requirements. Python's sort may use internal space.
        """
        # Step 1: Sort the weights.
        #
        # Why sort?
        # The problem statement asks for an efficient solution and specifically mentions
        # sorting and a two-pointer style argument. After sorting:
        # - All lighter crates appear first.
        # - All heavier crates appear later.
        # This makes it easy to find the exact point where crates stop being loadable alone.
        weights.sort()

        # Step 2: Use two pointers to identify the boundary between:
        # - crates that can fit on a boat alone (weight <= limit)
        # - crates that cannot fit on any boat at all (weight > limit)
        #
        # We will move `left` from the start and `right` from the end.
        # In practice, the answer is simply the count of values > limit.
        # The two-pointer framing helps show the monotonic structure after sorting.
        left: int = 0
        right: int = len(weights) - 1

        # This variable will count how many crates are too heavy and therefore must be escalated.
        escalations: int = 0

        # Step 3: Process from the heaviest side inward.
        #
        # Why from the right?
        # Because if the array is sorted, the heaviest crates are at the end.
        # Any crate heavier than `limit` is immediately known to be impossible to place,
        # even alone, so it must be escalated.
        #
        # Once we reach a crate with weight <= limit, then every crate to its left is also
        # <= limit because the array is sorted. At that point we can stop immediately.
        while left <= right:
            # If the current heaviest remaining crate exceeds the boat limit,
            # it cannot be assigned to any boat under the rules.
            if weights[right] > limit:
                escalations += 1
                right -= 1
            else:
                # Since weights[right] <= limit and the array is sorted,
                # every crate from index 0 through right is also <= limit.
                #
                # That means all remaining crates can each ride alone if needed.
                # Therefore, no more escalations are necessary.
                break

        return escalations

    def minimum_escalations_count_only(self, weights: List[int], limit: int) -> int:
        """
        Alternative implementation that directly counts crates heavier than the limit.

        This method is included for educational completeness. It expresses the same logic
        in the most direct possible way after sorting.

        Args:
            weights: List of crate weights.
            limit: Maximum weight a boat can carry.

        Returns:
            The minimum number of escalations required.

        Time complexity:
            O(n log n) due to sorting.

        Space complexity:
            O(1) extra space beyond sorting requirements. Python's sort may use internal space.
        """
        weights.sort()

        # Start from the heaviest crate and count how many are individually too heavy.
        count: int = 0
        index: int = len(weights) - 1

        while index >= 0 and weights[index] > limit:
            count += 1
            index -= 1

        return count


if __name__ == "__main__":
    solution = Solution()

    # Example 1:
    # weights = [3, 5, 2, 8, 1], limit = 5
    # Sorted: [1, 2, 3, 5, 8]
    # Only 8 is greater than 5, so exactly 1 escalation is needed.
    weights1: List[int] = [3, 5, 2, 8, 1]
    limit1: int = 5
    result1: int = solution.minimum_escalations(weights1[:], limit1)
    print("Example 1 Result:", result1)  # Expected: 1

    # Example 2:
    # weights = [9, 4, 7, 6, 2, 11], limit = 7
    # Sorted: [2, 4, 6, 7, 9, 11]
    # Crates 9 and 11 are greater than 7, so 2 escalations are needed.
    weights2: List[int] = [9, 4, 7, 6, 2, 11]
    limit2: int = 7
    result2: int = solution.minimum_escalations(weights2[:], limit2)
    print("Example 2 Result:", result2)  # Expected: 2

    # Additional sanity checks:
    weights3: List[int] = [1, 2, 3]
    limit3: int = 10
    result3: int = solution.minimum_escalations(weights3[:], limit3)
    print("All fit alone Result:", result3)  # Expected: 0

    weights4: List[int] = [12, 13, 14]
    limit4: int = 11
    result4: int = solution.minimum_escalations(weights4[:], limit4)
    print("None fit Result:", result4)  # Expected: 3