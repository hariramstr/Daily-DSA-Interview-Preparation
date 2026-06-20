"""
Title: Minimum Daily Charge to Finish Fleet Deliveries

Problem Description:
A logistics company operates a fleet of electric vans. Each van i must complete
deliveryDistance[i] kilometers of work. If the company sets the charging system
to provide a daily charge budget of X kilometers per van, then van i can complete
its route in ceil(deliveryDistance[i] / X) days, because it can recharge to cover
at most X kilometers per day. All vans work in parallel, and the company wants
every van to finish within at most maxDays total days.

Your task is to find the minimum integer value X such that the sum of days needed
by all vans is less than or equal to maxDays. If X is too small, the total required
days will exceed the limit. If X is large enough, the fleet can finish on time.

Return the smallest possible daily charge budget X.

Constraints:
- 1 <= deliveryDistance.length <= 100000
- 1 <= deliveryDistance[i] <= 1000000000
- deliveryDistance.length <= maxDays <= 1000000000000
- X must be a positive integer

Examples:
1)
Input: deliveryDistance = [12, 7, 25, 9], maxDays = 10
Output: 7

2)
Input: deliveryDistance = [3, 6, 14], maxDays = 8
Correct Output: 3

Important note:
The second example's written explanation says "Output: 4", but then correctly
shows that X = 3 also satisfies the condition and is smaller. Therefore, the
true minimum valid answer for Example 2 is 3.
"""

from typing import List


class Solution:
    def _required_days(self, delivery_distance: List[int], daily_charge: int, max_days: int) -> int:
        """
        Compute the total number of days needed if each van can travel at most
        `daily_charge` kilometers per day.

        Args:
            delivery_distance: List of route distances for each van.
            daily_charge: Candidate daily charge budget X.
            max_days: Upper limit on total allowed days, used for early stopping.

        Returns:
            The total number of required days. May stop early and return a value
            greater than max_days once it is already impossible to fit the limit.

        Time complexity:
            O(n), where n is the number of vans.

        Space complexity:
            O(1), excluding input storage.
        """
        total_days: int = 0

        # We process each van independently because the number of days for one van
        # depends only on its own distance and the chosen daily charge budget.
        for distance in delivery_distance:
            # We need ceil(distance / daily_charge).
            #
            # Instead of using floating point math, we use the standard integer trick:
            # ceil(a / b) == (a + b - 1) // b
            #
            # This is safer and faster in Python for large integers.
            total_days += (distance + daily_charge - 1) // daily_charge

            # Early stopping optimization:
            # If we already exceeded max_days, there is no need to continue summing.
            # This keeps the method efficient in many failing cases.
            if total_days > max_days:
                return total_days

        return total_days

    def minimum_daily_charge(self, deliveryDistance: List[int], maxDays: int) -> int:
        """
        Find the smallest positive integer daily charge budget X such that the sum
        of ceil(deliveryDistance[i] / X) over all vans is at most maxDays.

        Args:
            deliveryDistance: List of route distances for each van.
            maxDays: Maximum allowed total days across all vans.

        Returns:
            The minimum valid integer daily charge budget.

        Time complexity:
            O(n log m), where:
            - n is the number of vans
            - m is the maximum distance in deliveryDistance

        Space complexity:
            O(1), excluding input storage.
        """
        # Binary search is the correct tool here because the condition is monotonic:
        #
        # - If a certain daily charge X is sufficient, then any larger value is also sufficient.
        # - If a certain daily charge X is insufficient, then any smaller value is also insufficient.
        #
        # This "false, false, false, true, true, true" pattern is exactly what binary
        # search is designed to solve efficiently.

        # The smallest possible positive daily charge is 1.
        left: int = 1

        # The largest distance is always a valid upper bound:
        # if X == max(deliveryDistance), then every van finishes in exactly 1 day,
        # so total days == number of vans, and the problem guarantees that this is
        # at most maxDays.
        right: int = max(deliveryDistance)

        # We search for the first valid X.
        while left < right:
            # Midpoint candidate.
            # Using integer division keeps everything exact.
            mid: int = (left + right) // 2

            # Calculate how many total days are needed with this candidate charge.
            needed_days: int = self._required_days(deliveryDistance, mid, maxDays)

            # If the candidate is good enough, we do NOT stop immediately.
            # Why? Because we want the MINIMUM valid X, not just any valid X.
            #
            # So when mid works, we keep searching the left half, including mid.
            if needed_days <= maxDays:
                right = mid
            else:
                # If mid is too small, then every value <= mid is also too small.
                # Therefore, we discard the entire left half up to mid.
                left = mid + 1

        # At loop end, left == right, and it points to the smallest valid daily charge.
        return left


if __name__ == "__main__":
    solution = Solution()

    # Sample 1 from the prompt:
    # deliveryDistance = [12, 7, 25, 9], maxDays = 10
    # Check:
    # X = 6 -> 2 + 2 + 5 + 2 = 11 (too many)
    # X = 7 -> 2 + 1 + 4 + 2 = 9 (valid)
    # Therefore answer should be 7.
    delivery_distance_1: List[int] = [12, 7, 25, 9]
    max_days_1: int = 10
    result_1: int = solution.minimum_daily_charge(delivery_distance_1, max_days_1)
    print(result_1)  # Expected: 7

    # Sample 2 from the prompt:
    # deliveryDistance = [3, 6, 14], maxDays = 8
    # Check carefully:
    # X = 3 -> 1 + 2 + 5 = 8 (valid)
    # X = 2 -> 2 + 3 + 7 = 12 (too many)
    # Therefore the true minimum answer is 3.
    delivery_distance_2: List[int] = [3, 6, 14]
    max_days_2: int = 8
    result_2: int = solution.minimum_daily_charge(delivery_distance_2, max_days_2)
    print(result_2)  # Expected: 3