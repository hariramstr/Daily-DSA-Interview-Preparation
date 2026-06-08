```python
"""
Title: Minimum Capacity Shipping Containers Over D Days
Difficulty: Medium
Topic: Binary Search

Problem Description:
A shipping company needs to transport a sequence of cargo containers across a route.
Each container has a weight, and containers must be loaded onto ships in the order
they appear (you cannot reorder them). Each day, a single ship makes one trip and
can carry at most `capacity` total weight. You must ship all containers within
exactly `D` days.

Given an integer array `weights` where `weights[i]` is the weight of the i-th
container, and an integer `D` representing the number of days available, return
the minimum ship capacity needed to ship all containers within `D` days.

Note: Each container must be placed on a ship, and the order of containers cannot
be changed. A ship cannot carry more than its capacity on any single day.

Constraints:
- 1 <= D <= weights.length <= 50000
- 1 <= weights[i] <= 500

Example 1:
- Input: weights = [3, 2, 2, 4, 1, 4], D = 3
- Output: 6
- Explanation: With capacity 6, Day 1 ships [3,2], Day 2 ships [2,4], Day 3 ships [1,4].

Example 2:
- Input: weights = [1, 2, 3, 4, 5], D = 2
- Output: 9
- Explanation: With capacity 9, Day 1 ships [1,2,3], Day 2 ships [4,5]. Total days = 2.
"""

from typing import List


class Solution:
    def shipWithinDays(self, weights: List[int], D: int) -> int:
        """
        Find the minimum ship capacity to ship all containers within D days.

        Key Insight:
        - The answer (minimum capacity) lies in a range:
          * Lower bound: max(weights) — the ship must at least carry the heaviest
            single container (otherwise that container can never be shipped).
          * Upper bound: sum(weights) — if the ship can carry everything at once,
            it can always finish in 1 day (which satisfies any D >= 1).
        - We binary search over this range of possible capacities.
        - For each candidate capacity, we check: "Can we ship everything in <= D days?"
        - If yes, we try a smaller capacity (move right boundary left).
        - If no, we need a larger capacity (move left boundary right).

        Args:
            weights (List[int]): List of container weights in order.
            D (int): Number of days available to ship all containers.

        Returns:
            int: The minimum ship capacity needed to ship all containers within D days.

        Time Complexity: O(N * log(sum(weights) - max(weights)))
            - Binary search runs O(log(sum - max)) iterations.
            - Each iteration calls can_ship() which is O(N).
            - In the worst case, sum ~ 500 * 50000 = 25,000,000 and max ~ 500,
              so log(25,000,000) ≈ 25 iterations.

        Space Complexity: O(1)
            - We only use a constant amount of extra variables.
        """

        # -----------------------------------------------------------------------
        # STEP 1: Define the binary search boundaries.
        # -----------------------------------------------------------------------
        # The minimum possible capacity is the weight of the heaviest container.
        # Reason: If the ship's capacity were less than the heaviest container,
        # that container could never be loaded, making shipping impossible.
        left = max(weights)

        # The maximum possible capacity is the sum of all container weights.
        # Reason: With this capacity, the ship can carry everything in one trip
        # (one day), which trivially satisfies any D >= 1 constraint.
        right = sum(weights)

        # -----------------------------------------------------------------------
        # STEP 2: Binary search for the minimum valid capacity.
        # -----------------------------------------------------------------------
        # We want the SMALLEST capacity such that can_ship(capacity, D) is True.
        # This is a classic "find the leftmost True in a sorted True/False array"
        # binary search pattern.

        while left < right:
            # Calculate the midpoint to avoid integer overflow (though Python
            # handles big integers natively, this is good practice).
            mid = (left + right) // 2

            # -----------------------------------------------------------------------
            # STEP 3: Check if the current candidate capacity (mid) is sufficient.
            # -----------------------------------------------------------------------
            if self._can_ship(weights, D, mid):
                # If we CAN ship everything within D days using capacity `mid`,
                # then `mid` is a valid answer — but there might be a smaller
                # valid capacity. So we move the right boundary to `mid`
                # (not mid - 1, because mid itself might be the answer).
                right = mid
            else:
                # If we CANNOT ship everything within D days using capacity `mid`,
                # then `mid` is too small. We need a larger capacity.
                # Move the left boundary to mid + 1.
                left = mid + 1

        # -----------------------------------------------------------------------
        # STEP 4: Return the result.
        # -----------------------------------------------------------------------
        # When left == right, we've converged on the minimum valid capacity.
        return left

    def _can_ship(self, weights: List[int], D: int, capacity: int) -> bool:
        """
        Determine if all containers can be shipped within D days given a capacity.

        This is a greedy simulation:
        - We load containers onto the current day's ship one by one.
        - If adding the next container would exceed the capacity, we start a new day.
        - At the end, we check if the total days used is <= D.

        Args:
            weights (List[int]): List of container weights in order.
            D (int): Maximum number of days allowed.
            capacity (int): The ship's weight capacity per day.

        Returns:
            bool: True if all containers can be shipped within D days, False otherwise.

        Time Complexity: O(N) — we iterate through all containers once.
        Space Complexity: O(1) — only a few integer variables used.
        """

        # Track how many days (trips) we've used so far.
        # We start with 1 because the first day begins immediately.
        days_used = 1

        # Track the current load on today's ship.
        current_load = 0

        # -----------------------------------------------------------------------
        # Greedy simulation: iterate through each container in order.
        # -----------------------------------------------------------------------
        for weight in weights:
            # Check if adding this container to today's ship would exceed capacity.
            if current_load + weight > capacity:
                # It would exceed capacity, so we must start a new day (new trip).
                days_used += 1
                # Reset the current load for the new day, starting with this container.
                current_load = weight
            else:
                # It fits! Add this container to today's ship.
                current_load += weight

        # If the total days used is within the allowed D days, return True.
        return days_used <= D


# =============================================================================
# Main block: Test the solution with the provided examples and additional cases.
# =============================================================================
if __name__ == "__main__":
    solution = Solution()

    # -------------------------------------------------------------------------
    # Example 1:
    # weights = [3, 2, 2, 4, 1, 4], D = 3
    # Expected Output: 6
    #
    # Trace through binary search:
    # - left = max([3,2,2,4,1,4]) = 4
    # - right = sum([3,2,2,4,1,4]) = 16
    # - mid = 10 → can_ship? Day1:[3,2,2], Day2:[4,1,4] → 2 days ≤ 3 → True → right=10
    # - mid = 7  → can_ship? Day1:[3,2,2], Day2:[4,1], Day3:[4] → 3 days ≤ 3 → True → right=7
    # - mid = 5  → can_ship? Day1:[3,2], Day2:[2], Day3:[4,1], Day4:[4] → 4 days > 3 → False → left=6
    # - mid = 6  → can_ship? Day1:[3,2], Day2:[2,4], Day3:[1,4] → 3 days ≤ 3 → True → right=6
    # - left == right == 6 → return 6 ✓
    # -------------------------------------------------------------------------
    weights1 = [3, 2, 2, 4, 1, 4]
    D1 = 3
    result1 = solution.shipWithinDays(weights1, D1)
    print(f"Example 1:")
    print(f"  Input:    weights = {weights1}, D = {D1}")
    print(f"  Output:   {result1}")
    print(f"  Expected: 6")
    print(f"  Pass: {result1 == 6}")
    print()

    # -------------------------------------------------------------------------
    # Example 2:
    # weights = [1, 2, 3, 4, 5], D = 2
    # Expected Output: 9
    #
    # Trace through binary search:
    # - left = max([1,2,3,4,5]) = 5
    # - right = sum([1,2,3,4,5]) = 15
    # - mid = 10 → can_ship? Day1:[1,2,3,4], Day2:[5] → 2 days ≤ 2 → True → right=10
    # - mid = 7  → can_ship? Day1:[1,2,3], Day2:[4], Day3:[5] → 3 days > 2 → False → left=8
    # - mid = 9  → can_ship? Day1:[1,2,3], Day2:[4,5] → 2 days ≤ 2 → True → right=9
    # - mid = 8  → can_ship? Day1:[1,2,3], Day2:[4], Day3:[5] → 3 days > 2 → False → left=9
    # - left == right == 9 → return 9 ✓
    # -------------------------------------------------------------------------
    weights2 = [1, 2, 3, 4, 5]
    D2 = 2
    result2 = solution.shipWithinDays(weights2, D2)
    print(f"Example 2:")
    print(f"  Input:    weights = {weights2}, D = {D2}")
    print(f"  Output:   {result2}")
    print(f"  Expected: 9")
    print(f"  Pass: {result2 == 9}")
    print()

    # -------------------------------------------------------------------------
    # Additional Test Case 3:
    # weights = [1, 2, 3, 1, 1], D = 4
    # Expected Output: 3
    # Explanation: Day1:[1,2], Day2:[3], Day3:[1,1] → 3 days ≤ 4 → capacity 3 works
    # Can we do capacity 2? Day1:[1], Day2:[2], Day3:[1,1], Day4:[1] → 4 days ≤ 4 → True!
    # Wait, max(weights)=3, so left starts at 3. Let's verify:
    # capacity=3: Day1:[1,2], Day2:[3], Day3:[1,1] → 3 days ≤ 4 → True
    # But we need to check if capacity=3 is the minimum (left starts at max=3).
    # Since left=3 is the minimum possible, the answer is 3.
    # -------------------------------------------------------------------------
    weights3 = [1, 2, 3, 1, 1]
    D3 = 4
    result3 = solution.shipWithinDays(weights3, D3)
    print(f"Additional Test 3:")
    print(f"  Input:    weights = {weights3}, D = {D3}")
    print(f"  Output:   {result3}")
    print(f"  Expected: 3")
    print(f"  Pass: {result3 == 3}")
    print()

    # -------------------------------------------------------------------------
    # Additional Test Case 4: Single container, single day
    # weights = [7], D = 1
    # Expected Output: 7
    # The ship must carry the only container, so capacity = 7.
    # -------------------------------------------------------------------------
    weights4 = [7]
    D4 = 1
    result4 = solution.shipWithinDays(weights4, D4)
    print(f"Additional Test 4 (single container):")
    print(f"  Input:    weights = {weights4}, D = {D4}")
    print(f"  Output:   {result4}")
    print(f"  Expected: 7")
    print(f"  Pass: {result4 == 7}")
    print()

    # -------------------------------------------------------------------------
    # Additional Test Case 5: D equals number of containers (one per day)
    # weights = [5, 3, 8, 2], D = 4
    # Expected Output: 8 (max weight, since each container gets its own day)
    # -------------------------------------------------------------------------
    weights5 = [5, 3, 8, 2]
    D5 = 4
    result5 = solution.shipWithinDays(weights5, D5)
    print(f"Additional Test 5 (D = len(weights)):")
    print(f"  Input:    weights = {weights5}, D = {D5}")
    print(f"  Output:   {result5}")
    print(f"  Expected: 8")
    print(f"  Pass: {result5 == 8}")
    print()

    # -------------------------------------------------------------------------
    # Additional Test Case 6: D = 1 (must ship everything in one day)
    # weights = [1, 2, 3, 4, 5], D = 1
    # Expected Output: 15 (sum of all weights)
    # -------------------------------------------------------------------------
    weights6 = [1, 2, 3, 4, 5]
    D6 = 1
    result6 = solution.shipWithinDays(weights6, D6)
    print(f"Additional Test 6 (D = 1, ship all in one day):")
    print(f"  Input:    weights = {weights6}, D = {D6}")
    print(f"  Output:   {result6}")
    print(f"  Expected: 15")
    print(f"  Pass: {result6 == 15}")
```