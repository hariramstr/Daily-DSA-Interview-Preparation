"""
Title: Pair Lifters for a Moving Truck

Problem Description:
A moving company has a list of furniture items, where weights[i] is the weight of the
i-th item. Each trip with a hand truck can carry either one item or two items, but the
total weight on a single trip cannot exceed limit. Every item must be transported exactly once.

Your task is to return the minimum number of trips required to move all items.

This is not just a counting problem: an optimal solution should decide when to pair a
light item with a heavy item and when an item must go alone. The input array is not
guaranteed to be sorted. A good solution is expected to use sorting together with a
two-pointer strategy.

Constraints:
- 1 <= weights.length <= 100000
- 1 <= weights[i] <= limit <= 1000000000
- Each trip may carry at most 2 items
- All items must be moved

Examples:
1) weights = [4, 2, 3, 5], limit = 6
   Output: 3
   One optimal plan is (4,2), (3), (5)

2) weights = [1, 2, 2, 3, 4], limit = 4
   Output: 3
   One optimal plan is (1,3), (2,2), (4)
"""

from typing import List


class Solution:
    def min_trips(self, weights: List[int], limit: int) -> int:
        """
        Compute the minimum number of trips needed to move all items.

        The algorithm sorts the weights, then uses two pointers:
        - one pointer at the lightest remaining item
        - one pointer at the heaviest remaining item

        On each trip, we always take the heaviest remaining item, because every item
        must be moved and the heaviest item is the hardest one to pair. If the lightest
        remaining item can fit with it, we pair them together. Otherwise, the heaviest
        item must go alone.

        Args:
            weights: A list of item weights.
            limit: The maximum total weight allowed in one trip.

        Returns:
            The minimum number of trips required.

        Time complexity:
            O(n log n), due to sorting the list once.

        Space complexity:
            O(1) extra space beyond the sort operation's internal usage.
        """
        # Step 1: Sort the weights in non-decreasing order.
        #
        # Why sort?
        # Sorting lets us efficiently try the best possible pairing strategy:
        # pair the heaviest remaining item with the lightest remaining item.
        #
        # This is the key greedy idea:
        # - If the lightest item can pair with the heaviest, that is always a good choice,
        #   because using any heavier partner would only make fitting harder.
        # - If the lightest item cannot pair with the heaviest, then the heaviest cannot
        #   pair with any other remaining item either, because all others are at least as heavy.
        #
        # This observation is what makes the two-pointer strategy correct.
        weights.sort()

        # Step 2: Initialize two pointers.
        #
        # left  -> points to the lightest remaining item
        # right -> points to the heaviest remaining item
        #
        # We will move these pointers inward as items get assigned to trips.
        left: int = 0
        right: int = len(weights) - 1

        # Step 3: Count how many trips we use.
        trips: int = 0

        # Step 4: Process items until all have been assigned.
        #
        # The loop condition left <= right means:
        # - If left < right, there are at least two items remaining.
        # - If left == right, there is exactly one item remaining, and it needs one final trip.
        while left <= right:
            # We always use one trip for the heaviest remaining item at index 'right'.
            #
            # Reason:
            # The heaviest item must be transported now or later, but in any case it will
            # consume one trip slot. The only question is whether we can save space by
            # pairing it with the lightest remaining item.
            if weights[left] + weights[right] <= limit:
                # The lightest and heaviest items can share a trip.
                #
                # This is the best possible pairing attempt for the heaviest item:
                # if even the lightest fits, we should use it and save a trip.
                # We move 'left' forward because that lightest item has now been used.
                left += 1

            # Whether paired or not, the heaviest item at 'right' is definitely used now.
            right -= 1

            # We have completed one trip.
            trips += 1

        return trips


if __name__ == "__main__":
    solution = Solution()

    # Example 1
    weights1: List[int] = [4, 2, 3, 5]
    limit1: int = 6
    result1: int = solution.min_trips(weights1, limit1)
    print(f"weights = {weights1}, limit = {limit1} -> minimum trips = {result1}")
    # Expected: 3

    # Example 2
    weights2: List[int] = [1, 2, 2, 3, 4]
    limit2: int = 4
    result2: int = solution.min_trips(weights2, limit2)
    print(f"weights = {weights2}, limit = {limit2} -> minimum trips = {result2}")
    # Expected: 3

    # Additional quick checks
    weights3: List[int] = [3]
    limit3: int = 3
    result3: int = solution.min_trips(weights3, limit3)
    print(f"weights = {weights3}, limit = {limit3} -> minimum trips = {result3}")
    # Expected: 1

    weights4: List[int] = [2, 2, 2, 2]
    limit4: int = 3
    result4: int = solution.min_trips(weights4, limit4)
    print(f"weights = {weights4}, limit = {limit4} -> minimum trips = {result4}")
    # Expected: 4

    weights5: List[int] = [1, 1, 1, 1]
    limit5: int = 2
    result5: int = solution.min_trips(weights5, limit5)
    print(f"weights = {weights5}, limit = {limit5} -> minimum trips = {result5}")
    # Expected: 2