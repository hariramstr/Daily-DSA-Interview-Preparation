"""
Title: Pair Contestants for a Canoe Ride

Problem Description:
You are organizing a team-building event where contestants will ride in two-person canoes.
Each canoe can carry at most 2 people, and the combined weight of the two people in the
same canoe cannot exceed a given limit. Some contestants may need to ride alone if no
valid partner is available.

Given an integer array weights where weights[i] is the weight of the i-th contestant,
and an integer limit representing the maximum allowed total weight in one canoe, return
the minimum number of canoes needed to carry everyone.

A common efficient strategy:
1. Sort the weights.
2. Use two pointers:
   - one pointer at the lightest remaining contestant
   - one pointer at the heaviest remaining contestant
3. If the lightest and heaviest can share a canoe, place them together.
4. Otherwise, the heaviest contestant must ride alone.

Constraints:
- 1 <= weights.length <= 50000
- 1 <= weights[i] <= limit <= 30000
- Each canoe can carry at most 2 contestants

Example 1:
Input: weights = [70, 50, 80, 50], limit = 100
Output: 3
Explanation: One optimal arrangement is (50, 50), (70), (80), so 3 canoes are required.

Example 2:
Input: weights = [40, 60, 55, 45], limit = 100
Output: 2
Explanation: One optimal arrangement is (40, 60) and (45, 55), so only 2 canoes are needed.

Goal:
Return the smallest possible number of canoes that can transport all contestants.
"""

from typing import List


class Solution:
    def numRescueBoats(self, weights: List[int], limit: int) -> int:
        """
        Compute the minimum number of canoes needed to carry all contestants.

        The method sorts the list of weights and then uses a two-pointer strategy:
        - The left pointer starts at the lightest remaining contestant.
        - The right pointer starts at the heaviest remaining contestant.
        - We always place the heaviest remaining contestant into a canoe.
        - If the lightest remaining contestant can fit with the heaviest without
          exceeding the limit, they share the canoe.
        - Otherwise, the heaviest contestant rides alone.

        This greedy choice is optimal because the heaviest contestant is the hardest
        person to pair. If they cannot pair with the lightest person, then they cannot
        pair with anyone else either, so sending them alone is necessary.

        Args:
            weights: A list of contestant weights.
            limit: The maximum total weight allowed in one canoe.

        Returns:
            The minimum number of canoes required.

        Time complexity:
            O(n log n), due to sorting the weights list.

        Space complexity:
            O(n) in Python because sorted() creates a new list.
        """
        # We sort the weights so that we can efficiently try to pair
        # the lightest remaining contestant with the heaviest remaining contestant.
        #
        # Why sorting helps:
        # - The heaviest contestant is the most difficult to place because they have
        #   the fewest possible partners.
        # - The lightest contestant is the best candidate to pair with the heaviest.
        # - If even the lightest cannot pair with the heaviest, then nobody can.
        sorted_weights: List[int] = sorted(weights)

        # 'left' points to the lightest contestant not yet assigned to a canoe.
        left: int = 0

        # 'right' points to the heaviest contestant not yet assigned to a canoe.
        right: int = len(sorted_weights) - 1

        # This will count how many canoes we use.
        boats: int = 0

        # Continue until all contestants have been assigned.
        #
        # The condition left <= right means:
        # - If left < right, there are at least two contestants remaining.
        # - If left == right, exactly one contestant remains and they need one canoe.
        while left <= right:
            # We are definitely going to use one canoe for the heaviest remaining contestant.
            # So we count one canoe now.
            boats += 1

            # Check whether the lightest and heaviest remaining contestants can share.
            #
            # If their combined weight is within the limit:
            # - We place both in the same canoe.
            # - Move 'left' forward because the lightest contestant is now assigned.
            #
            # Whether they pair or not, the heaviest contestant at 'right' will always
            # be assigned in this iteration, so 'right' will move backward afterward.
            if sorted_weights[left] + sorted_weights[right] <= limit:
                left += 1

            # The heaviest contestant is always assigned in this step:
            # - either paired with the lightest
            # - or alone if pairing was impossible
            right -= 1

        return boats


if __name__ == "__main__":
    solution = Solution()

    # Example 1
    weights1: List[int] = [70, 50, 80, 50]
    limit1: int = 100
    result1: int = solution.numRescueBoats(weights1, limit1)
    print(f"Example 1: weights = {weights1}, limit = {limit1}")
    print(f"Minimum canoes needed: {result1}")
    print("Expected: 3")
    print()

    # Example 2
    weights2: List[int] = [40, 60, 55, 45]
    limit2: int = 100
    result2: int = solution.numRescueBoats(weights2, limit2)
    print(f"Example 2: weights = {weights2}, limit = {limit2}")
    print(f"Minimum canoes needed: {result2}")
    print("Expected: 2")
    print()

    # Additional beginner-friendly test cases
    weights3: List[int] = [30]
    limit3: int = 50
    result3: int = solution.numRescueBoats(weights3, limit3)
    print(f"Additional Test 1: weights = {weights3}, limit = {limit3}")
    print(f"Minimum canoes needed: {result3}")
    print("Expected: 1")
    print()

    weights4: List[int] = [20, 20, 20, 20]
    limit4: int = 40
    result4: int = solution.numRescueBoats(weights4, limit4)
    print(f"Additional Test 2: weights = {weights4}, limit = {limit4}")
    print(f"Minimum canoes needed: {result4}")
    print("Expected: 2")
    print()

    weights5: List[int] = [90, 10, 80, 20, 70, 30]
    limit5: int = 100
    result5: int = solution.numRescueBoats(weights5, limit5)
    print(f"Additional Test 3: weights = {weights5}, limit = {limit5}")
    print(f"Minimum canoes needed: {result5}")
    print("Expected: 3")