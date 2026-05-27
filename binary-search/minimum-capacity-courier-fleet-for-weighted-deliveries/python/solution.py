```python
"""
Title: Minimum Capacity Courier Fleet for Weighted Deliveries
Difficulty: Hard
Topic: Binary Search

Problem Description:
A logistics company needs to deliver `n` packages arranged in a fixed route order.
Each package has a weight given by `weights[i]`. You have a fleet of `k` couriers,
and each courier must take a contiguous segment of packages from the route
(i.e., the packages cannot be reordered). All `k` couriers operate simultaneously,
and each courier's load is the sum of weights of the packages assigned to them.

However, there is an additional constraint: each courier can only carry packages if
the number of packages assigned to them does not exceed a given limit `maxPkgs`.
In other words, every contiguous segment assigned to a courier must contain at most
`maxPkgs` packages.

Your goal is to minimize the maximum load carried by any single courier, while ensuring:
1. All packages are delivered.
2. Each courier receives a contiguous, non-empty segment.
3. No courier carries more than `maxPkgs` packages.

Return the minimum possible value of the maximum load among all couriers. If it is
impossible to partition the packages into exactly `k` non-empty contiguous segments
each with at most `maxPkgs` packages, return -1.

Constraints:
- 1 <= n <= 10^5
- 1 <= k <= n
- 1 <= maxPkgs <= n
- 1 <= weights[i] <= 10^4
"""

from typing import List


class Solution:
    def minimumCapacity(self, weights: List[int], k: int, maxPkgs: int) -> int:
        """
        Find the minimum possible maximum load for k couriers with a package count limit.

        This uses binary search on the answer (the capacity/max load), combined with
        a greedy feasibility check. For a given capacity `mid`, we greedily assign
        as many packages as possible to each courier (without exceeding `mid` weight
        or `maxPkgs` packages), and check if we can cover all packages with at most k couriers.

        Args:
            weights: List of package weights in route order.
            k: Number of couriers available.
            maxPkgs: Maximum number of packages any single courier can carry.

        Returns:
            The minimum possible maximum load, or -1 if impossible.

        Time Complexity: O(n * log(sum(weights))) — binary search over the range of
                         possible capacities, with O(n) greedy check each iteration.
        Space Complexity: O(1) — only a constant amount of extra space is used.
        """

        n = len(weights)

        # -----------------------------------------------------------------------
        # STEP 1: FEASIBILITY PRE-CHECK
        # -----------------------------------------------------------------------
        # Before doing binary search, check if a valid partition is even possible.
        #
        # For a valid partition into exactly k non-empty contiguous segments where
        # each segment has at most maxPkgs packages:
        #
        # Condition A: k * maxPkgs >= n
        #   Each of the k couriers can carry at most maxPkgs packages.
        #   So the total packages we can handle is k * maxPkgs.
        #   If this is less than n, we can't cover all packages → return -1.
        #
        # Condition B: k <= n
        #   We need at least k packages to form k non-empty segments.
        #   (This is guaranteed by the constraint 1 <= k <= n, but good to note.)
        #
        # Condition C: maxPkgs >= 1 (guaranteed by constraints)
        #   Each courier must get at least 1 package (non-empty segment).

        if k * maxPkgs < n:
            # Not enough capacity across all couriers to cover all n packages
            return -1

        if k > n:
            # Can't form k non-empty segments from fewer than k packages
            return -1

        # -----------------------------------------------------------------------
        # STEP 2: DEFINE THE BINARY SEARCH RANGE
        # -----------------------------------------------------------------------
        # We binary search on the "capacity" — the maximum load any courier carries.
        #
        # Lower bound (lo): The minimum possible capacity must be at least the
        #   maximum single package weight (since every package must be assigned
        #   to some courier, and a courier with just that package has load = weight).
        #   Also, we need to think about the minimum window: since we have k couriers
        #   and n packages, some courier must take at least ceil(n/k) packages.
        #   But the tightest lower bound is max(weights).
        #
        # Upper bound (hi): The maximum possible capacity is the sum of all weights
        #   (one courier takes everything — though this might violate maxPkgs,
        #   it's a safe upper bound for the binary search range).

        lo = max(weights)  # At minimum, capacity must cover the heaviest single package
        hi = sum(weights)  # At maximum, one courier takes all packages

        # -----------------------------------------------------------------------
        # STEP 3: BINARY SEARCH
        # -----------------------------------------------------------------------
        # We search for the smallest capacity `mid` such that it's feasible to
        # partition all packages into AT MOST k segments, each with:
        #   - total weight <= mid
        #   - number of packages <= maxPkgs
        #
        # Key insight: If capacity `mid` is feasible, then any capacity > mid is
        # also feasible (monotone property). So we can binary search.
        #
        # We want the MINIMUM feasible capacity, so we use the classic binary search
        # pattern: when feasible, try smaller (hi = mid); when not feasible, go larger (lo = mid + 1).

        while lo < hi:
            mid = (lo + hi) // 2  # Candidate capacity to test

            # Check if we can partition packages into at most k segments
            # where each segment has weight <= mid AND count <= maxPkgs
            if self._is_feasible(weights, k, maxPkgs, mid):
                # mid is feasible — try to find something smaller
                hi = mid
            else:
                # mid is not feasible — we need more capacity
                lo = mid + 1

        # At this point, lo == hi == the minimum feasible capacity
        return lo

    def _is_feasible(self, weights: List[int], k: int, maxPkgs: int, capacity: int) -> bool:
        """
        Greedy check: can we partition `weights` into at most `k` contiguous segments
        where each segment has total weight <= capacity AND count <= maxPkgs?

        Strategy: Greedily assign as many packages as possible to the current courier
        without exceeding either the weight limit (capacity) or the package count limit
        (maxPkgs). When we can't add more, start a new courier.

        Args:
            weights: List of package weights.
            k: Maximum number of couriers (segments) allowed.
            maxPkgs: Maximum packages per courier.
            capacity: The maximum load (weight sum) per courier to test.

        Returns:
            True if the partition is possible with at most k couriers, False otherwise.

        Time Complexity: O(n) — single pass through weights.
        Space Complexity: O(1) — only counters used.
        """

        # Number of couriers used so far (start with 1, since we always need at least one)
        couriers_used = 1

        # Current courier's accumulated weight
        current_load = 0

        # Current courier's package count
        current_pkg_count = 0

        for weight in weights:
            # ---------------------------------------------------------------
            # Check if adding this package to the current courier is valid:
            # 1. current_load + weight <= capacity (weight constraint)
            # 2. current_pkg_count + 1 <= maxPkgs (package count constraint)
            # ---------------------------------------------------------------
            if (current_load + weight <= capacity and
                    current_pkg_count + 1 <= maxPkgs):
                # Safe to add this package to the current courier
                current_load += weight
                current_pkg_count += 1
            else:
                # Cannot add this package to the current courier.
                # Start a new courier for this package.
                couriers_used += 1

                # If we've exceeded k couriers, this capacity is not feasible
                if couriers_used > k:
                    return False

                # Also check: if a single package exceeds capacity, it's impossible
                # (This handles the edge case where weight > capacity)
                if weight > capacity:
                    return False

                # Assign this package to the new courier
                current_load = weight
                current_pkg_count = 1

        # We successfully partitioned all packages into `couriers_used` segments
        # Check if we used at most k couriers
        return couriers_used <= k


# -------------------------------------------------------------------------------
# MAIN: Test with provided examples and additional edge cases
# -------------------------------------------------------------------------------

if __name__ == "__main__":
    solution = Solution()

    # -----------------------------------------------------------------------
    # Example 1 (from problem description):
    # weights = [3, 2, 8, 5, 1, 7, 4], k = 3, maxPkgs = 4
    # Expected Output: 13
    #
    # Let's trace through:
    # - n=7, k=3, maxPkgs=4
    # - k * maxPkgs = 12 >= 7 = n → feasible check passes
    # - lo = max(weights) = 8, hi = sum(weights) = 30
    #
    # Binary search:
    #   mid=19: feasible? Greedy: [3,2,8,5]=18 (4 pkgs), [1,7,4]=12 (3 pkgs) → 2 couriers ≤ 3 → YES
    #   hi=19, mid=13: feasible? [3,2,8]=13 (3 pkgs), [5,1,7]=13 (3 pkgs), [4]=4 (1 pkg) → 3 couriers ≤ 3 → YES
    #   hi=13, mid=10: feasible? [3,2,8]=13 > 10, try [3,2]=5, [8]=8, [5,1]=6, [7]=7, [4]=4 → 5 couriers > 3 → NO
    #   lo=11, mid=12: feasible? [3,2,8]=13>12, [3,2]=5(2pkgs), [8]=8(1pkg), [5,1]=6(2pkgs), [7]=7(1pkg), [4]=4(1pkg) → 5 couriers > 3 → NO
    #   lo=13, lo==hi=13 → answer = 13
    # -----------------------------------------------------------------------
    weights1 = [3, 2, 8, 5, 1, 7, 4]
    k1 = 3
    maxPkgs1 = 4
    result1 = solution.minimumCapacity(weights1, k1, maxPkgs1)
    print(f"Example 1:")
    print(f"  Input: weights={weights1}, k={k1}, maxPkgs={maxPkgs1}")
    print(f"  Output: {result1}")
    print(f"  Expected: 13")
    print(f"  {'PASS' if result1 == 13 else 'FAIL'}")
    print()

    # -----------------------------------------------------------------------
    # Example 2 (from problem description):
    # weights = [10, 10, 10, 10, 10], k = 3, maxPkgs = 1
    # Expected Output: -1
    #
    # Trace:
    # - n=5, k=3, maxPkgs=1
    # - k * maxPkgs = 3 * 1 = 3 < 5 = n → return -1 immediately
    # -----------------------------------------------------------------------
    weights2 = [10, 10, 10, 10, 10]
    k2 = 3
    maxPkgs2 = 1
    result2 = solution.minimumCapacity(weights2, k2, maxPkgs2)
    print(f"Example 2:")
    print(f"  Input: weights={weights2}, k={k2}, maxPkgs={maxPkgs2}")
    print(f"  Output: {result2}")
    print(f"  Expected: -1")
    print(f"  {'PASS' if result2 == -1 else 'FAIL'}")
    print()

    # -----------------------------------------------------------------------
    # Additional Test Case 3: Single package, single courier
    # weights = [5], k = 1, maxPkgs = 1
    # Expected: 5 (only one way to partition)
    # -----------------------------------------------------------------------
    weights3 = [5]
    k3 = 1
    maxPkgs3 = 1
    result3 = solution.minimumCapacity(weights3, k3, maxPkgs3)
    print(f"Example 3 (single package):")
    print(f"  Input: weights={weights3}, k={k3}, maxPkgs={maxPkgs3}")
    print(f"  Output: {result3}")
    print(f"  Expected: 5")
    print(f"  {'PASS' if result3 == 5 else 'FAIL'}")
    print()

    # -----------------------------------------------------------------------
    # Additional Test Case 4: k equals n (each courier gets exactly 1 package)
    # weights = [3, 1, 4, 1, 5], k = 5, maxPkgs = 1
    # Expected: 5 (each courier gets one package; max is 5)
    # -----------------------------------------------------------------------
    weights4 = [3, 1, 4, 1, 5]
    k4 = 5
    maxPkgs4 = 1
    result4 = solution.minimumCapacity(weights4, k4, maxPkgs4)
    print(f"Example 4 (k == n):")
    print(f"  Input: weights={weights4}, k={k4}, maxPkgs={maxPkgs4}")
    print(f"  Output: {result4}")
    print(f"  Expected: 5")
    print(f"  {'PASS' if result4 == 5 else 'FAIL'}")
    print()

    # -----------------------------------------------------------------------
    # Additional Test Case 5: k=1 (one courier takes all packages)
    # weights = [1, 2, 3, 4, 5], k = 1, maxPkgs = 5
    # Expected: 15 (sum of all weights)
    # -----------------------------------------------------------------------
    weights5 = [1, 2, 3, 4, 5]
    k5 = 1
    maxPkgs5 = 5
    result5 = solution.minimumCapacity(weights5, k5, maxPkgs5)
    print(f"Example 5 (k=1, one courier):")
    print(f"  Input: weights={weights5}, k={k5}, maxPkgs={maxPkgs5}")
    print(f"  Output: {result5}")
    print(f"  Expected: 15")
    print(f"  {'PASS' if result5 == 15 else 'FAIL'}")
    print()

    # -----------------------------------------------------------------------
    # Additional Test Case 6: Impossible due to k > n
    # weights = [1, 2], k = 5, maxPkgs = 2
    # Expected: -1 (can't form 5 non-empty segments from 2 packages)
    # -----------------------------------------------------------------------
    weights6 = [1, 2]
    k6 = 5
    maxPkgs6 = 2
    result6 = solution.minimumCapacity(weights6, k6, maxPkgs6)
    print(f"Example 6 (k > n, impossible):")
    print(f"  Input: weights={weights6}, k={k6}, maxPkgs={maxPkgs6}")
    print(f"  Output: {result6}")
    print(f"  Expected: -1")
    print(f"  {'PASS' if result6 == -1 else 'FAIL'}")
    print()

    # -----------------------------------------------------------------------
    # Additional Test Case 7: Uniform weights
    # weights = [5, 5, 5, 5, 5, 5], k = 2, maxP