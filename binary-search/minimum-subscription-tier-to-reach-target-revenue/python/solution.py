"""
Title: Minimum Subscription Tier to Reach Target Revenue

Problem Description:
A software company offers a product with several subscription tiers. Each customer has
a maximum tier they are willing to buy, represented by an integer in the array
`limits`, where `limits[i]` is the highest price tier customer `i` will accept.

If the company sets a global tier price `p`, then:
- every customer with `limits[i] >= p` subscribes
- every customer with `limits[i] < p` does not

So the total revenue is:
    revenue(p) = p * (number of customers willing to pay at least p)

Given the array `limits` and an integer `targetRevenue`, return the minimum integer
tier price `p` such that the total revenue is at least `targetRevenue`.

If no such tier exists, return -1.

Constraints:
- 1 <= limits.length <= 2 * 10^5
- 1 <= limits[i] <= 10^9
- 1 <= targetRevenue <= 10^18

Important note:
The revenue function is not globally monotonic over all prices, because increasing
price can reduce the number of customers. However, after sorting the customer limits,
we can reason in segments where the number of paying customers stays constant.
That allows an efficient binary-search-based solution over the sorted thresholds.
"""

from bisect import bisect_left
from typing import List


class Solution:
    def minimum_tier_price(self, limits: List[int], target_revenue: int) -> int:
        """
        Find the minimum integer subscription tier price that achieves at least
        the target revenue.

        The key idea:
        1. Sort the customer limits.
        2. For any price p, the number of customers who can pay is the count of
           values >= p.
        3. If exactly k customers can pay, then p must lie in a specific interval
           determined by the sorted limits, and revenue in that interval is simply
           p * k, which increases as p increases.
        4. For each possible customer count k, we can compute the smallest p in that
           interval that makes p * k >= target_revenue, and check whether that p is
           valid for the interval.
        5. Among all valid candidates, choose the minimum p.

        Args:
            limits: List of maximum acceptable tier prices for each customer.
            target_revenue: Required minimum total revenue.

        Returns:
            The minimum integer price p such that revenue(p) >= target_revenue,
            or -1 if no such price exists.

        Time complexity:
            O(n log n), where n is the number of customers.
            - Sorting costs O(n log n)
            - The scan over all customer counts costs O(n)

        Space complexity:
            O(n) due to storing the sorted list.
        """
        # Step 1:
        # Sort the limits in non-decreasing order.
        #
        # Why sort?
        # Because once sorted, we can describe exactly how many customers are willing
        # to pay a given price by looking at where that price would fit in the array.
        #
        # Example:
        # limits = [3, 8, 6, 6, 10]
        # sorted = [3, 6, 6, 8, 10]
        #
        # If p = 5, then customers with limits >= 5 are [6, 6, 8, 10], so count = 4.
        sorted_limits: List[int] = sorted(limits)
        n: int = len(sorted_limits)

        # Step 2:
        # Quick impossibility check.
        #
        # The maximum possible revenue must occur at one of the customer limit values,
        # because between two consecutive distinct limits, the number of paying
        # customers stays constant, so revenue increases with price and is best at the
        # right endpoint of that interval.
        #
        # We can compute the best possible revenue by checking:
        # sorted_limits[i] * (n - i)
        # because at price sorted_limits[i], exactly the suffix from i onward can pay.
        max_revenue: int = 0
        for i, price_limit in enumerate(sorted_limits):
            paying_customers: int = n - i
            revenue_here: int = price_limit * paying_customers
            if revenue_here > max_revenue:
                max_revenue = revenue_here

        # If even the best possible revenue is too small, there is no answer.
        if max_revenue < target_revenue:
            return -1

        # Step 3:
        # Search all possible "customer count segments".
        #
        # Suppose exactly k customers are willing to pay.
        # In the sorted array, those are the last k customers.
        #
        # Let i = n - k.
        # Then:
        # - The smallest limit among those k customers is sorted_limits[i]
        # - So price p must satisfy p <= sorted_limits[i], otherwise one of those
        #   k customers would drop out.
        #
        # Also, to ensure that no extra customer joins from the left side:
        # - If i > 0, then p must be > sorted_limits[i - 1]
        # - Since p is an integer, that means:
        #       p >= sorted_limits[i - 1] + 1
        # - If i == 0, then there is no left side, so the lower bound is 1.
        #
        # Therefore, for each k, valid integer prices form:
        #   [low, high]
        # where:
        #   low = 1 if i == 0 else sorted_limits[i - 1] + 1
        #   high = sorted_limits[i]
        #
        # In this interval, the number of paying customers is exactly k, so:
        #   revenue = p * k
        #
        # To reach target_revenue, we need:
        #   p * k >= target_revenue
        #   p >= ceil(target_revenue / k)
        #
        # So the smallest candidate in this interval is:
        #   candidate = max(low, ceil(target_revenue / k))
        #
        # If candidate <= high, then it is valid.
        #
        # We evaluate all k and keep the smallest valid candidate.
        answer: int = 10**30

        for k in range(1, n + 1):
            # i is the first index of the suffix containing the k customers
            # who would pay in this segment.
            i: int = n - k

            # Compute the valid price interval [low, high] for exactly k paying customers.
            if i == 0:
                # All customers are paying.
                # Since prices are positive integers, the smallest possible price is 1.
                low: int = 1
            else:
                # To exclude the customer at index i - 1, the price must be strictly
                # greater than sorted_limits[i - 1]. Because price is integer:
                low = sorted_limits[i - 1] + 1

            # To keep the customer at index i included, price cannot exceed
            # the smallest limit in the paying suffix.
            high: int = sorted_limits[i]

            # If low > high, this segment contains no valid integer price.
            # That can happen when there are repeated values and no integer exists
            # that gives exactly this customer count.
            if low > high:
                continue

            # Compute ceil(target_revenue / k) using integer arithmetic.
            #
            # This avoids floating-point issues and is safe for very large numbers.
            required_price: int = (target_revenue + k - 1) // k

            # The smallest price in this segment that can reach the target is the
            # larger of:
            # - the segment's lower bound
            # - the price required by revenue
            candidate: int = max(low, required_price)

            # If candidate stays inside the segment, then exactly k customers pay
            # and revenue is at least target_revenue.
            if candidate <= high and candidate < answer:
                answer = candidate

        return answer if answer != 10**30 else -1

    def minimum_tier_price_binary_search(self, limits: List[int], target_revenue: int) -> int:
        """
        Alternative implementation using binary search over the sorted unique limits
        and interval logic.

        This method is included for educational completeness, but the main method
        `minimum_tier_price` is already optimal and simpler to follow.

        Args:
            limits: List of maximum acceptable tier prices for each customer.
            target_revenue: Required minimum total revenue.

        Returns:
            The minimum integer price p such that revenue(p) >= target_revenue,
            or -1 if no such price exists.

        Time complexity:
            O(n log n)

        Space complexity:
            O(n)
        """
        sorted_limits: List[int] = sorted(limits)
        n: int = len(sorted_limits)

        max_revenue: int = 0
        for i, value in enumerate(sorted_limits):
            max_revenue = max(max_revenue, value * (n - i))
        if max_revenue < target_revenue:
            return -1

        answer: int = 10**30

        for i in range(n):
            count: int = n - i
            low: int = 1 if i == 0 else sorted_limits[i - 1] + 1
            high: int = sorted_limits[i]
            if low > high:
                continue

            required_price: int = (target_revenue + count - 1) // count
            candidate: int = max(low, required_price)
            if candidate <= high:
                answer = min(answer, candidate)

        return answer if answer != 10**30 else -1


if __name__ == "__main__":
    solution = Solution()

    # Example 1
    limits_1: List[int] = [3, 8, 6, 6, 10]
    target_1: int = 18
    result_1: int = solution.minimum_tier_price(limits_1, target_1)
    print("Example 1 Result:", result_1)  # Expected: 5

    # Example 2
    limits_2: List[int] = [2, 2, 2]
    target_2: int = 10
    result_2: int = solution.minimum_tier_price(limits_2, target_2)
    print("Example 2 Result:", result_2)  # Expected: -1

    # Additional sample tests
    limits_3: List[int] = [5]
    target_3: int = 3
    result_3: int = solution.minimum_tier_price(limits_3, target_3)
    print("Additional Test 1 Result:", result_3)  # Expected: 3

    limits_4: List[int] = [1, 4, 4, 4]
    target_4: int = 8
    result_4: int = solution.minimum_tier_price(limits_4, target_4)
    print("Additional Test 2 Result:", result_4)  # Expected: 2

    limits_5: List[int] = [10, 10, 10]
    target_5: int = 1
    result_5: int = solution.minimum_tier_price(limits_5, target_5)
    print("Additional Test 3 Result:", result_5)  # Expected: 1