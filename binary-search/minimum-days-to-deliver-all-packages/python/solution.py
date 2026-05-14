```python
"""
Title: Minimum Days to Deliver All Packages
Difficulty: Medium
Topic: Binary Search

Problem Description:
A delivery company has `n` packages lined up in a row, where `packages[i]` represents
the weight of the i-th package. A delivery truck can carry packages on any given day,
but it has two constraints:

1. The truck can carry at most `k` consecutive packages per day (they must be contiguous
   in the original order).
2. The truck can carry packages with a total weight of at most `capacity` per trip.

Packages must be delivered in order (you cannot skip a package and come back to it later).
Each day, the truck picks the next batch of consecutive packages starting where it left off,
subject to both constraints above.

Given the array `packages`, an integer `k`, and an integer `capacity`, return the minimum
number of days needed to deliver all packages.

Constraints:
- 1 <= packages.length <= 10^5
- 1 <= packages[i] <= 500
- 1 <= k <= packages.length
- 1 <= capacity <= 10^7
- It is guaranteed that each individual package weight does not exceed capacity.
"""

from typing import List


class Solution:
    def min_days_to_deliver(self, packages: List[int], k: int, capacity: int) -> int:
        """
        Find the minimum number of days to deliver all packages using binary search
        combined with a greedy feasibility check.

        The key insight is:
        - We binary search on the answer (number of days).
        - For a given number of days `d`, we check if it's possible to deliver all
          packages in exactly `d` days using a greedy approach.
        - Greedy: each day, take as many consecutive packages as possible (up to k)
          without exceeding the capacity limit.

        Args:
            packages: List of package weights in order.
            k: Maximum number of consecutive packages per day.
            capacity: Maximum total weight the truck can carry per day.

        Returns:
            Minimum number of days to deliver all packages.

        Time Complexity: O(n * log(n)) where n = len(packages).
            - Binary search runs O(log(n)) iterations (answer is between 1 and n).
            - Each feasibility check is O(n) using prefix sums.
        Space Complexity: O(n) for the prefix sum array.
        """

        n = len(packages)

        # -----------------------------------------------------------------------
        # STEP 1: Build a prefix sum array for O(1) range sum queries.
        #
        # prefix[i] = sum of packages[0..i-1]
        # So the sum of packages[l..r] (inclusive) = prefix[r+1] - prefix[l]
        #
        # This allows us to quickly check if a batch of packages from index l to r
        # has total weight <= capacity, without re-summing every time.
        # -----------------------------------------------------------------------
        prefix = [0] * (n + 1)
        for i in range(n):
            prefix[i + 1] = prefix[i] + packages[i]

        # -----------------------------------------------------------------------
        # STEP 2: Define the feasibility function.
        #
        # can_deliver_in_days(days) returns True if we can deliver all packages
        # in at most `days` days, using the greedy strategy:
        #   - Start from the current position.
        #   - Each day, take as many consecutive packages as possible (up to k)
        #     without exceeding capacity.
        #   - Count how many days are needed; if it's <= `days`, return True.
        #
        # Why greedy works here:
        #   Taking as many packages as possible each day minimizes the number of
        #   days needed. If we take fewer packages on a given day, we can only
        #   need MORE days, never fewer.
        # -----------------------------------------------------------------------
        def can_deliver_in_days(days: int) -> bool:
            """
            Check if all packages can be delivered in at most `days` days.

            Uses a greedy approach: each day, deliver as many consecutive packages
            as possible (up to k packages and up to capacity total weight).

            Args:
                days: The number of days to check feasibility for.

            Returns:
                True if deliverable in `days` days, False otherwise.
            """
            # `idx` tracks the current starting package index (0-based).
            idx = 0
            # `days_used` counts how many days we've used so far.
            days_used = 0

            # Keep delivering until all packages are delivered.
            while idx < n:
                # We've used one more day for this batch.
                days_used += 1

                # If we've already exceeded the allowed number of days, return False.
                # Early exit optimization.
                if days_used > days:
                    return False

                # Determine the farthest package we can take today:
                # - We can take at most k packages starting from idx,
                #   so the farthest index (exclusive) is idx + k.
                # - But we also can't go beyond n (total packages).
                max_end = min(idx + k, n)

                # Now, within [idx, max_end), find the largest `end` such that
                # the sum of packages[idx..end-1] <= capacity.
                #
                # Since we built prefix sums, sum(packages[idx..end-1]) = prefix[end] - prefix[idx].
                # We want the largest `end` in [idx+1, max_end] where prefix[end] - prefix[idx] <= capacity.
                #
                # We can use binary search here for efficiency, but since max_end - idx <= k
                # and k can be up to n, let's use binary search within [idx, max_end].
                #
                # Binary search for the rightmost valid end:
                lo, hi = idx + 1, max_end
                # `best_end` is the farthest valid end index (exclusive).
                best_end = idx + 1  # We must take at least 1 package (guaranteed weight <= capacity).

                while lo <= hi:
                    mid = (lo + hi) // 2
                    # Check if taking packages[idx..mid-1] is within capacity.
                    batch_weight = prefix[mid] - prefix[idx]
                    if batch_weight <= capacity:
                        # This batch is valid; try to take more.
                        best_end = mid
                        lo = mid + 1
                    else:
                        # Too heavy; try fewer packages.
                        hi = mid - 1

                # Move the starting index to the package after the last delivered one.
                idx = best_end

            # If we delivered all packages within the allowed days, return True.
            return days_used <= days

        # -----------------------------------------------------------------------
        # STEP 3: Binary search on the answer (number of days).
        #
        # Lower bound: 1 day (best case: all packages fit in one trip).
        # Upper bound: n days (worst case: one package per day).
        #
        # We search for the smallest `days` such that can_deliver_in_days(days) is True.
        #
        # Binary search pattern for "find minimum value satisfying condition":
        #   - If condition is True for mid, try smaller (hi = mid).
        #   - If condition is False for mid, try larger (lo = mid + 1).
        #   - Loop until lo == hi; that's our answer.
        # -----------------------------------------------------------------------
        lo, hi = 1, n  # Search space: [1, n] days.

        while lo < hi:
            mid = (lo + hi) // 2  # Candidate number of days.

            if can_deliver_in_days(mid):
                # It's possible to deliver in `mid` days.
                # Try to see if we can do it in fewer days.
                hi = mid
            else:
                # Not possible in `mid` days; we need more days.
                lo = mid + 1

        # -----------------------------------------------------------------------
        # STEP 4: Return the result.
        #
        # After the binary search, lo == hi == the minimum number of days.
        # -----------------------------------------------------------------------
        return lo


# -------------------------------------------------------------------------------
# MAIN: Test the solution with the provided examples and additional edge cases.
# -------------------------------------------------------------------------------
if __name__ == "__main__":
    solution = Solution()

    # ---------------------------------------------------------------------------
    # Example 1:
    # packages = [3, 2, 4, 1, 5, 2, 3], k = 3, capacity = 7
    # Expected Output: 4
    #
    # Let's trace the greedy for 4 days:
    #   Day 1: Start at idx=0. max_end = min(0+3, 7) = 3.
    #          Binary search in [1, 3]:
    #            mid=2: prefix[2]-prefix[0] = 3+2 = 5 <= 7 → best_end=2, lo=3
    #            mid=3: prefix[3]-prefix[0] = 3+2+4 = 9 > 7 → hi=2
    #          best_end=2. Deliver packages[0..1] = [3,2], sum=5. idx=2.
    #   Day 2: Start at idx=2. max_end = min(2+3, 7) = 5.
    #          Binary search in [3, 5]:
    #            mid=4: prefix[4]-prefix[2] = 4+1 = 5 <= 7 → best_end=4, lo=5
    #            mid=5: prefix[5]-prefix[2] = 4+1+5 = 10 > 7 → hi=4
    #          best_end=4. Deliver packages[2..3] = [4,1], sum=5. idx=4.
    #   Day 3: Start at idx=4. max_end = min(4+3, 7) = 7.
    #          Binary search in [5, 7]:
    #            mid=6: prefix[6]-prefix[4] = 5+2 = 7 <= 7 → best_end=6, lo=7
    #            mid=7: prefix[7]-prefix[4] = 5+2+3 = 10 > 7 → hi=6
    #          best_end=6. Deliver packages[4..5] = [5,2], sum=7. idx=6.
    #   Day 4: Start at idx=6. max_end = min(6+3, 7) = 7.
    #          Binary search in [7, 7]:
    #            mid=7: prefix[7]-prefix[6] = 3 <= 7 → best_end=7, lo=8
    #          best_end=7. Deliver packages[6] = [3], sum=3. idx=7.
    #   All delivered in 4 days. ✓
    #
    # Can we do it in 3 days?
    #   Day 1: [3,2] sum=5, idx=2.
    #   Day 2: [4,1] sum=5, idx=4.
    #   Day 3: [5,2] sum=7, idx=6.
    #   Still have package[6]=3 left. Need day 4. So 3 days is NOT feasible.
    # Answer: 4
    # ---------------------------------------------------------------------------
    packages1 = [3, 2, 4, 1, 5, 2, 3]
    k1 = 3
    capacity1 = 7
    result1 = solution.min_days_to_deliver(packages1, k1, capacity1)
    print(f"Example 1: packages={packages1}, k={k1}, capacity={capacity1}")
    print(f"  Result: {result1}")
    print(f"  Expected: 4")
    print()

    # ---------------------------------------------------------------------------
    # Example 2:
    # packages = [1, 2, 3, 4, 5], k = 2, capacity = 6
    # Expected Output: 3
    #
    # Let's trace the greedy for 3 days:
    #   Day 1: Start at idx=0. max_end = min(0+2, 5) = 2.
    #          Binary search in [1, 2]:
    #            mid=1: prefix[1]-prefix[0] = 1 <= 6 → best_end=1, lo=2
    #            mid=2: prefix[2]-prefix[0] = 1+2 = 3 <= 6 → best_end=2, lo=3
    #          best_end=2. Deliver [1,2], sum=3. idx=2.
    #   Day 2: Start at idx=2. max_end = min(2+2, 5) = 4.
    #          Binary search in [3, 4]:
    #            mid=3: prefix[3]-prefix[2] = 3 <= 6 → best_end=3, lo=4
    #            mid=4: prefix[4]-prefix[2] = 3+4 = 7 > 6 → hi=3
    #          best_end=3. Deliver [3], sum=3. idx=3.
    #   Day 3: Start at idx=3. max_end = min(3+2, 5) = 5.
    #          Binary search in [4, 5]:
    #            mid=4: prefix[4]-prefix[3] = 4 <= 6 → best_end=4, lo=5
    #            mid=5: prefix[5]-prefix[3] = 4+5 = 9 > 6 → hi=4
    #          best_end=4. Deliver [4], sum=4. idx=4.
    #   Still have package[4]=5 left. Need day 4. So 3 days is NOT feasible with this greedy.
    #
    # Wait, let me re-check. The problem says optimal is [1,2], [3], [4,5] → 3 days.
    # But [4,5] has sum=9 > capacity=6. That can't be right.
    # Let me re-read: capacity=6, [4,5] sum=9 > 6. So that grouping is invalid.
    # The problem description says "Actually optimal grouping: [1,2], [3], [4,5] → 3 days"
    # but [4,5] sum=9 > 6, so this seems like an error in the problem description.
    #
    # Let me check what the actual minimum is:
    # With k=2, capacity=6:
    #   Possible groupings:
    #   [1,2]=3, [3]=3, [4]=4, [5]=5 → 4 days
    #   [1,2]=3, [3,4]=7>6 invalid
    #   [1,2]=3, [3]=3, [4,5]=9>6 invalid
    #   [1]=1, [2,3]=5, [4]=4, [5]=5 → 4 days
    #   [1]=1, [2,3]=5, [4,5]=9>6 invalid
    #   Seems like minimum is 4 days for this example.
    #
    # The problem description's Example 2 seems to have an error.
    # Our greedy correctly finds 4 days.
    # ---------------------------------------------------------------------------
    packages2 = [1, 2, 3, 4, 5]
    k2 = 2
    capacity2 = 6
    result2 = solution.min_days_to_deliver(packages2, k2, capacity2)
    print(f"Example 2: packages={packages2}, k={k2}, capacity={capacity2}")
    print(f"  Result: {result2}")
    print(f"  Expected: 4 (problem description has an error; [4,5] sum=9 > capacity=6)")
    print()

    # ---------------------------------------------------------------------------
    # Additional Example 3: Single package
    # packages = [5], k = 1, capacity = 10
    # Expected Output: 1
    # ---------------------------------------------------------------------------
    packages3 = [5]
    k3 = 1
    capacity3 = 10
    result3 = solution.min_days_to_deliver(packages3, k3, capacity3)
    print(f"Example 3: packages={packages3}, k={k3}, capacity={capacity3}")
    print(f"  Result: {result3}")
    print(f