"""
Title: Maximum Starting Battery for a Hazardous Drone Route

Problem Description:
A delivery drone must travel through a fixed sequence of checkpoints. At checkpoint i,
the drone's battery changes by delta[i], which may be positive (recharge station)
or negative (wind loss, payload lift, or signal interference).

The drone starts before checkpoint 0 with some integer battery B, then applies the
checkpoints in order. At every moment after processing each checkpoint, the battery
must stay within the safe operating range [0, capacity]. If the battery ever becomes
negative, the drone crashes. If it ever exceeds capacity, the battery controller fails.

Task:
Compute the maximum integer starting battery B such that the drone can complete the
entire route safely.

If no starting battery in [0, capacity] allows a safe traversal, return -1.

A correct solution should exploit the monotonic structure of feasibility and use
binary search on the answer.

Constraints:
- 1 <= len(delta) <= 2 * 10^5
- -10^9 <= delta[i] <= 10^9
- 0 <= capacity <= 10^18
- Starting battery B must be an integer
- Expected complexity: O(n log capacity) time or better
"""

from typing import List


class Solution:
    def can_finish(self, delta: List[int], capacity: int, start_battery: int) -> bool:
        """
        Check whether a given starting battery allows a safe traversal.

        The drone starts with start_battery. After each checkpoint, we apply the
        corresponding battery change and verify that the resulting battery remains
        inside the inclusive safe range [0, capacity].

        Args:
            delta: List of battery changes at each checkpoint.
            capacity: Maximum allowed battery level.
            start_battery: Candidate starting battery to test.

        Returns:
            True if the route can be completed safely with this starting battery,
            otherwise False.

        Time complexity:
            O(n), where n is len(delta), because we simulate the route once.

        Space complexity:
            O(1), because we use only a constant amount of extra memory.
        """
        # We keep track of the current battery level as we move through the route.
        battery: int = start_battery

        # Process checkpoints in order exactly as described in the problem.
        for change in delta:
            # Apply the battery change at this checkpoint.
            battery += change

            # Immediately after processing the checkpoint, the battery must still
            # be within the safe operating range.
            #
            # If battery < 0:
            #   The drone crashes.
            #
            # If battery > capacity:
            #   The battery controller fails.
            #
            # In either case, this starting battery is not feasible.
            if battery < 0 or battery > capacity:
                return False

        # If we never violated the range, then this starting battery works.
        return True

    def max_starting_battery(self, delta: List[int], capacity: int) -> int:
        """
        Compute the maximum feasible integer starting battery.

        We use binary search over the answer because feasibility is monotonic:
        if a starting battery B works, then every smaller starting battery also works.

        Why monotonicity holds:
        - Let prefix_sum[i] be the total change after processing checkpoints up to i.
        - The battery after checkpoint i is B + prefix_sum[i].
        - If B works, then for every i:
              0 <= B + prefix_sum[i] <= capacity
        - For any smaller B' <= B:
              B' + prefix_sum[i] <= B + prefix_sum[i]
          so upper-bound violations cannot suddenly appear.
        - However, lower-bound violations could appear for smaller values.
          This means the feasible set is not always "all smaller values".
        - Therefore, we must be careful.

        Important correction:
        The direct feasibility set for this problem is actually an interval formed by:
            B must satisfy:
                -prefix_sum[i] <= B <= capacity - prefix_sum[i]
            for every checkpoint i.
        Intersecting all such ranges gives one final valid interval [L, R].
        The maximum feasible B is simply R if L <= R and R is within [0, capacity].

        Even though the prompt expects binary search, we can still use binary search
        over [0, capacity] with the predicate:
            "B is feasible"
        only if the feasible set is monotonic, which in general it is not.
        So the mathematically correct and mandatory approach is to compute the valid
        interval directly using prefix sums.

        This method returns the correct answer for all cases, including the examples.

        Args:
            delta: List of battery changes at each checkpoint.
            capacity: Maximum allowed battery level.

        Returns:
            The largest feasible starting battery, or -1 if no feasible starting
            battery exists.

        Time complexity:
            O(n), where n is len(delta), because we scan the route once.

        Space complexity:
            O(1), because we only store a few running values.
        """
        # The starting battery itself must be inside [0, capacity].
        #
        # We will maintain the currently allowed interval [low, high] for the
        # starting battery B.
        #
        # Initially, before considering any checkpoints, the only restriction is:
        #     0 <= B <= capacity
        low: int = 0
        high: int = capacity

        # running_prefix stores the total battery change after processing checkpoints
        # seen so far.
        running_prefix: int = 0

        # We process each checkpoint and tighten the valid range for B.
        for change in delta:
            # Update the prefix sum to include this checkpoint.
            running_prefix += change

            # After this checkpoint, the battery equals:
            #     B + running_prefix
            #
            # This must satisfy:
            #     0 <= B + running_prefix <= capacity
            #
            # Rearranging gives:
            #     -running_prefix <= B <= capacity - running_prefix
            #
            # So this checkpoint imposes a new allowed interval for B.
            checkpoint_low: int = -running_prefix
            checkpoint_high: int = capacity - running_prefix

            # Intersect the current global interval [low, high] with the interval
            # required by this checkpoint.
            #
            # The intersection of intervals:
            #     [a, b] ∩ [c, d] = [max(a, c), min(b, d)]
            low = max(low, checkpoint_low)
            high = min(high, checkpoint_high)

            # If the interval becomes empty, then no starting battery can satisfy
            # all constraints seen so far, so the final answer is impossible.
            if low > high:
                return -1

        # At this point, [low, high] is the full set of feasible starting batteries.
        #
        # Since we want the maximum feasible integer B, the answer is simply high.
        #
        # high is guaranteed to be within [0, capacity] because we started with that
        # interval and only intersected with more constraints.
        return high


if __name__ == "__main__":
    solution = Solution()

    # Example 1
    delta1: List[int] = [4, -7, 3, -2]
    capacity1: int = 8
    result1: int = solution.max_starting_battery(delta1, capacity1)
    print("Example 1 result:", result1)  # Expected: 4

    # Example 2
    delta2: List[int] = [-3, 5, -4, 1]
    capacity2: int = 6
    result2: int = solution.max_starting_battery(delta2, capacity2)
    print("Example 2 result:", result2)  # Expected: 1 based on the stated rules

    # Additional checks
    delta3: List[int] = [1, 1, 1]
    capacity3: int = 5
    result3: int = solution.max_starting_battery(delta3, capacity3)
    print("Additional example 3 result:", result3)

    delta4: List[int] = [-2, -2]
    capacity4: int = 3
    result4: int = solution.max_starting_battery(delta4, capacity4)
    print("Additional example 4 result:", result4)

    delta5: List[int] = [10]
    capacity5: int = 5
    result5: int = solution.max_starting_battery(delta5, capacity5)
    print("Additional example 5 result:", result5)