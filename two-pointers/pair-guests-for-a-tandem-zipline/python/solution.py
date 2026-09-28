"""
Title: Pair Guests for a Tandem Zipline

Problem Description:
An adventure park runs a tandem zipline where each ride must carry either one guest alone
or two guests together. For safety, the combined weight of any paired guests cannot exceed
a given limit. You are given an integer array weights where weights[i] is the weight of the
i-th guest, and an integer limit representing the maximum total weight allowed on one tandem ride.

Return the minimum number of rides needed to send all guests down the zipline.

Each ride can take at most two guests. A guest may ride alone, and every guest must be assigned
to exactly one ride. Your goal is to minimize the total number of rides.

This problem is designed to reward an efficient pairing strategy rather than brute force search.
A common approach is to sort the guest weights and use two pointers to try pairing the lightest
remaining guest with the heaviest remaining guest whenever possible.

Constraints:
- 1 <= weights.length <= 100000
- 1 <= weights[i] <= 1000000000
- 1 <= limit <= 1000000000
- It is guaranteed that every individual guest can ride alone, so weights[i] <= limit for all i

Example 1:
Input: weights = [70, 50, 80, 50], limit = 100
Output: 3
Explanation: Pair the two guests weighing 50 and 50 together. The guests weighing 70 and 80
must ride alone. So the minimum number of rides is 3.

Example 2:
Input: weights = [40, 60, 55, 45, 80], limit = 100
Output: 3
Explanation: One optimal assignment is (40, 60), (45, 55), and (80). No solution can use fewer
than 3 rides because there are 5 guests and each ride holds at most 2 guests.
"""

from typing import List


class Solution:
    def min_rides(self, weights: List[int], limit: int) -> int:
        """
        Compute the minimum number of rides needed to transport all guests.

        The method sorts the guest weights, then uses a two-pointer strategy:
        - One pointer starts at the lightest remaining guest.
        - One pointer starts at the heaviest remaining guest.
        - We always place the heaviest remaining guest on the next ride.
        - If the lightest remaining guest can join them without exceeding the limit,
          we pair them together.
        - Otherwise, the heaviest guest rides alone.

        Args:
            weights: A list of guest weights.
            limit: The maximum total weight allowed on one ride.

        Returns:
            The minimum number of rides required.

        Time complexity:
            O(n log n), due to sorting the list.

        Space complexity:
            O(n) in Python because sorted(weights) creates a new list.
        """
        # Sort the weights so we can efficiently try the best possible pairing.
        #
        # Why sorting helps:
        # - The heaviest guest is the hardest to place because they have the least room
        #   left for a partner.
        # - If the lightest guest cannot pair with the heaviest guest, then no one can,
        #   because every other remaining guest is heavier than the lightest.
        # - This observation makes the greedy strategy correct and efficient.
        sorted_weights: List[int] = sorted(weights)

        # left points to the lightest guest not yet assigned to a ride.
        left: int = 0

        # right points to the heaviest guest not yet assigned to a ride.
        right: int = len(sorted_weights) - 1

        # This will count how many rides we use in total.
        rides: int = 0

        # Continue until all guests have been assigned.
        #
        # The condition left <= right means:
        # - If left < right, there are at least two guests remaining.
        # - If left == right, exactly one guest remains and they need one final ride.
        while left <= right:
            # We are definitely going to use one ride for the heaviest remaining guest.
            #
            # Why?
            # - Every remaining solution must place this heaviest guest somewhere.
            # - We decide that "somewhere" is the current ride.
            rides += 1

            # Check whether the lightest remaining guest can share the ride
            # with the heaviest remaining guest.
            #
            # If their combined weight is within the limit, pairing them is optimal:
            # - It uses one ride for two people.
            # - It preserves heavier middle guests for later, where they may still
            #   need to ride alone anyway.
            if sorted_weights[left] + sorted_weights[right] <= limit:
                # The lightest guest is successfully paired with the heaviest guest,
                # so move the left pointer inward to mark that guest as assigned.
                left += 1

            # Whether paired or not, the heaviest guest has now been assigned,
            # so move the right pointer inward.
            right -= 1

        # After the loop finishes, every guest has been assigned to exactly one ride.
        return rides

    def num_rescue_boats(self, weights: List[int], limit: int) -> int:
        """
        Alias method that solves the same problem.

        This wrapper is included to provide an alternative familiar method name
        for the same two-pointer solution.

        Args:
            weights: A list of guest weights.
            limit: The maximum total weight allowed on one ride.

        Returns:
            The minimum number of rides required.

        Time complexity:
            O(n log n), due to sorting.

        Space complexity:
            O(n) in Python because sorting with sorted() creates a new list.
        """
        return self.min_rides(weights, limit)


if __name__ == "__main__":
    solution = Solution()

    # Example 1 from the problem statement.
    weights1: List[int] = [70, 50, 80, 50]
    limit1: int = 100
    result1: int = solution.min_rides(weights1, limit1)
    print("Example 1:")
    print(f"weights = {weights1}, limit = {limit1}")
    print(f"Minimum rides needed = {result1}")
    print("Expected = 3")
    print()

    # Example 2 from the problem statement.
    weights2: List[int] = [40, 60, 55, 45, 80]
    limit2: int = 100
    result2: int = solution.min_rides(weights2, limit2)
    print("Example 2:")
    print(f"weights = {weights2}, limit = {limit2}")
    print(f"Minimum rides needed = {result2}")
    print("Expected = 3")
    print()

    # Additional beginner-friendly test cases.
    extra_tests: List[tuple[List[int], int]] = [
        ([50], 100),                 # One guest, one ride
        ([30, 70], 100),             # Perfect pair
        ([60, 60, 60], 100),         # No pair possible
        ([20, 30, 50, 70, 80], 100)  # Mixed case
    ]

    print("Additional Tests:")
    for test_weights, test_limit in extra_tests:
        rides_needed: int = solution.min_rides(test_weights, test_limit)
        print(f"weights = {test_weights}, limit = {test_limit} -> rides = {rides_needed}")