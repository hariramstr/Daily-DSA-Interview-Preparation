"""
Title: Pair Kayaks for a Calm River

Problem Description:
A river tour company has a list of tourists waiting to use two-person kayaks.
Each tourist has a weight, and every kayak can hold at most two people as long
as their combined weight does not exceed a given limit. You want to use as few
kayaks as possible while sending everyone on the river.

Given an integer array `weights` where `weights[i]` is the weight of the `i`th
tourist, and an integer `limit` representing the maximum total weight a kayak
can safely carry, return the minimum number of kayaks needed to carry all
tourists.

Each kayak may carry either one tourist or two tourists. A tourist cannot be
split across kayaks, and every tourist must be assigned to exactly one kayak.

This problem is intended to be solved efficiently using sorting and two
pointers. After sorting the weights, try pairing the lightest remaining tourist
with the heaviest remaining tourist whenever possible. If they cannot share a
kayak, the heaviest tourist must go alone.

Constraints:
- 1 <= weights.length <= 50000
- 1 <= weights[i] <= limit <= 100000
- It is always possible to place each tourist in some kayak individually.

Example 1:
Input: weights = [70, 50, 80, 50], limit = 100
Output: 3
Explanation: One optimal arrangement is (50, 50), (70), (80). So 3 kayaks are needed.

Example 2:
Input: weights = [40, 60, 55, 45, 30], limit = 90
Output: 3
Explanation: One optimal arrangement is (30, 60), (40, 45), (55). This uses the fewest possible kayaks.
"""

from typing import List


class Solution:
    def numRescueBoats(self, weights: List[int], limit: int) -> int:
        """
        Compute the minimum number of kayaks needed to carry all tourists.

        The strategy is:
        1. Sort the weights.
        2. Use two pointers:
           - One pointer starts at the lightest remaining tourist.
           - One pointer starts at the heaviest remaining tourist.
        3. Always place the heaviest remaining tourist into a kayak.
           - If the lightest remaining tourist can fit with them, pair them.
           - Otherwise, the heaviest tourist goes alone.
        4. Count how many kayaks are used.

        Args:
            weights: A list of tourist weights.
            limit: The maximum total weight one kayak can carry.

        Returns:
            The minimum number of kayaks required.

        Time complexity:
            O(n log n), because sorting dominates the runtime.

        Space complexity:
            O(1) extra space beyond the sorting operation
            (or O(n) depending on Python's internal sort implementation details).
        """
        # First, sort the list of weights in non-decreasing order.
        # Why do we sort?
        # Because sorting lets us efficiently compare the lightest and heaviest
        # remaining tourists. This is the key idea behind the two-pointer method.
        #
        # After sorting:
        # - The lightest tourist is at the beginning.
        # - The heaviest tourist is at the end.
        #
        # This structure makes it easy to decide whether the heaviest person
        # can share a kayak with the lightest person.
        weights.sort()

        # `left` points to the lightest tourist not yet assigned to a kayak.
        left: int = 0

        # `right` points to the heaviest tourist not yet assigned to a kayak.
        right: int = len(weights) - 1

        # This will count the total number of kayaks used.
        kayaks_used: int = 0

        # Continue until all tourists have been assigned.
        # The condition `left <= right` means there is at least one unassigned tourist.
        while left <= right:
            # We are definitely going to use one kayak for the heaviest remaining tourist.
            # Why?
            # Because every iteration removes the heaviest person from consideration.
            # They must go either:
            # - alone, or
            # - paired with the lightest remaining person.
            kayaks_used += 1

            # Check whether the lightest and heaviest remaining tourists can share a kayak.
            #
            # If their combined weight is within the limit, pairing them is optimal.
            # Why is this greedy choice correct?
            # - The heaviest person is the hardest to place.
            # - If even the lightest person cannot fit with the heaviest, then nobody can.
            # - If the lightest person can fit, pairing them saves space and reduces
            #   the number of kayaks needed.
            if weights[left] + weights[right] <= limit:
                # They can share a kayak.
                # Move `left` forward because the lightest tourist is now assigned.
                left += 1

            # Whether paired or alone, the heaviest tourist at `right` is now assigned.
            # So we always move `right` backward.
            right -= 1

        # After the loop finishes, every tourist has been assigned to a kayak.
        # Return the total number of kayaks used.
        return kayaks_used

    def min_kayaks(self, weights: List[int], limit: int) -> int:
        """
        Wrapper method that returns the minimum number of kayaks.

        This method exists to provide a beginner-friendly alternate name while
        delegating to the main algorithm method.

        Args:
            weights: A list of tourist weights.
            limit: The maximum total weight one kayak can carry.

        Returns:
            The minimum number of kayaks required.

        Time complexity:
            O(n log n), because it calls the sorting-based main method.

        Space complexity:
            O(1) extra space beyond the sorting operation
            (or O(n) depending on Python's internal sort implementation details).
        """
        return self.numRescueBoats(weights, limit)


if __name__ == "__main__":
    # Create an instance of the solution class.
    solution = Solution()

    # Example 1 from the problem statement.
    weights1: List[int] = [70, 50, 80, 50]
    limit1: int = 100
    result1: int = solution.numRescueBoats(weights1[:], limit1)
    print("Example 1:")
    print(f"weights = {weights1}, limit = {limit1}")
    print(f"Minimum kayaks needed = {result1}")
    print("Expected = 3")
    print()

    # Example 2 from the problem statement.
    weights2: List[int] = [40, 60, 55, 45, 30]
    limit2: int = 90
    result2: int = solution.numRescueBoats(weights2[:], limit2)
    print("Example 2:")
    print(f"weights = {weights2}, limit = {limit2}")
    print(f"Minimum kayaks needed = {result2}")
    print("Expected = 3")
    print()

    # Additional small test cases for beginner-friendly verification.
    extra_tests: List[tuple[List[int], int]] = [
        ([50], 100),             # One tourist, one kayak
        ([30, 30], 60),          # Perfect pair
        ([30, 40, 50], 70),      # One pair and one alone
        ([90, 80, 70, 60], 100)  # Likely all alone except if possible
    ]

    print("Additional Tests:")
    for test_weights, test_limit in extra_tests:
        result: int = solution.min_kayaks(test_weights[:], test_limit)
        print(f"weights = {test_weights}, limit = {test_limit} -> {result}")