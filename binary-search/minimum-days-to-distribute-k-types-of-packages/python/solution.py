```python
"""
Title: Minimum Days to Distribute K Types of Packages
Difficulty: Hard
Topic: Binary Search

Problem Description:
A warehouse has `n` workers and `m` package types. Each package type `i` has a weight
`weights[i]` and a count `counts[i]` representing how many packages of that type exist.
Each worker can carry packages on a given day, but the total weight they carry must not
exceed a given capacity `C`. On each day, every worker makes exactly one trip, and workers
can only carry packages of the same type in a single trip.

You are given an integer `k` — you must ensure that at least `k` distinct package types
are fully distributed (all packages of those types delivered) in the minimum number of days.
You want to find the minimum number of days required to fully distribute at least `k`
distinct package types.

Formally, binary search on the number of days `d`. For a given `d` days, determine the
maximum number of distinct package types that can be fully distributed using `n` workers
each carrying up to `C` weight per day.

Constraints:
- 1 <= n <= 10^4
- 1 <= m <= 10^5
- 1 <= k <= m
- 1 <= weights[i] <= C <= 10^6
- 1 <= counts[i] <= 10^9
"""

import math
from typing import List


class Solution:
    def min_days(
        self,
        n: int,
        C: int,
        weights: List[int],
        counts: List[int],
        k: int
    ) -> int:
        """
        Find the minimum number of days to fully distribute at least k distinct
        package types using n workers each with capacity C.

        Core Idea:
        - Binary search on the answer `d` (number of days).
        - For a fixed number of days `d`, compute for each package type i how many
          days it takes to fully distribute all counts[i] packages:
              packages_per_worker_per_day = floor(C / weights[i])
              total_packages_per_day = n * packages_per_worker_per_day
              days_needed_for_type_i = ceil(counts[i] / total_packages_per_day)
        - Count how many types can be fully distributed within `d` days (i.e.,
          days_needed_for_type_i <= d).
        - If that count >= k, then `d` days is feasible.
        - Binary search for the smallest feasible `d`.

        Args:
            n: Number of workers.
            C: Maximum weight capacity per worker per trip.
            weights: List of weights for each package type.
            counts: List of counts for each package type.
            k: Minimum number of distinct package types to fully distribute.

        Returns:
            The minimum number of days required.

        Time Complexity: O(m * log(max_days)) where max_days = ceil(max(counts) / n)
                         and m is the number of package types.
        Space Complexity: O(1) extra space (ignoring input storage).
        """

        # -----------------------------------------------------------------------
        # Step 1: Precompute the number of days needed for each package type.
        #
        # For each type i:
        #   - A single worker can carry floor(C / weights[i]) packages per trip.
        #   - With n workers, total packages moved per day = n * floor(C / weights[i]).
        #   - Days needed = ceil(counts[i] / (n * floor(C / weights[i]))).
        #
        # We store these values so we don't recompute them during binary search.
        # -----------------------------------------------------------------------
        m = len(weights)
        days_needed: List[int] = []  # days_needed[i] = days to fully distribute type i

        for i in range(m):
            # How many packages can one worker carry in one trip for type i?
            pkgs_per_worker = C // weights[i]  # floor division

            if pkgs_per_worker == 0:
                # This shouldn't happen per constraints (weights[i] <= C),
                # but guard against it: if a worker can't carry even one package,
                # this type can never be distributed.
                # We'll set a very large sentinel value.
                days_needed.append(10**18)
                continue

            # Total packages all n workers can move in one day for type i
            pkgs_per_day = n * pkgs_per_worker

            # Days needed to clear all counts[i] packages of type i
            # Use ceiling division: ceil(a / b) = (a + b - 1) // b
            d_i = math.ceil(counts[i] / pkgs_per_day)
            days_needed.append(d_i)

        # -----------------------------------------------------------------------
        # Step 2: Define the feasibility check function.
        #
        # Given `d` days, count how many package types can be fully distributed.
        # A type i is distributable in `d` days iff days_needed[i] <= d.
        # We return True if the count of such types >= k.
        # -----------------------------------------------------------------------
        def can_distribute_k_types(d: int) -> bool:
            """
            Check if at least k package types can be fully distributed in d days.

            For each type, we already know how many days it needs. We simply count
            how many types need <= d days.
            """
            # Count types that can be fully distributed within d days
            distributable_count = 0
            for d_i in days_needed:
                if d_i <= d:
                    distributable_count += 1
                    # Early exit: once we've found k types, no need to continue
                    if distributable_count >= k:
                        return True
            return False

        # -----------------------------------------------------------------------
        # Step 3: Set up binary search bounds.
        #
        # Lower bound (lo): 1 day (minimum possible answer).
        # Upper bound (hi): The maximum days_needed across all types.
        #   - In the worst case, we need to distribute the hardest type,
        #     so hi = max(days_needed) is a safe upper bound.
        #   - Actually, since we only need k types, hi = the k-th smallest
        #     days_needed value. But using max(days_needed) is simpler and correct.
        #
        # We binary search for the smallest d such that can_distribute_k_types(d)
        # is True.
        # -----------------------------------------------------------------------
        lo = 1
        hi = max(days_needed)  # Safe upper bound: worst case for any single type

        # Edge case: if even with hi days we can't distribute k types,
        # that means there aren't k valid types — but per constraints k <= m,
        # so this shouldn't happen (assuming all weights[i] <= C).

        # -----------------------------------------------------------------------
        # Step 4: Binary search for the minimum feasible number of days.
        #
        # Invariant:
        #   - can_distribute_k_types(hi) is always True (hi is a valid answer).
        #   - can_distribute_k_types(lo - 1) is always False.
        #
        # We narrow the range until lo == hi, at which point lo is our answer.
        # -----------------------------------------------------------------------
        while lo < hi:
            mid = (lo + hi) // 2  # Candidate number of days

            if can_distribute_k_types(mid):
                # mid days is feasible — try fewer days (search left half)
                hi = mid
            else:
                # mid days is not enough — need more days (search right half)
                lo = mid + 1

        # -----------------------------------------------------------------------
        # Step 5: Return the answer.
        #
        # After the loop, lo == hi and this is the minimum number of days.
        # -----------------------------------------------------------------------
        return lo


# -------------------------------------------------------------------------------
# Main block: Test the solution with the provided examples.
# -------------------------------------------------------------------------------
if __name__ == "__main__":
    sol = Solution()

    # ---------------------------------------------------------------------------
    # Example 1:
    # n=3, C=10, weights=[4,6,3], counts=[12,6,9], k=2
    #
    # Trace:
    # Type 0: pkgs_per_worker = 10//4 = 2, pkgs_per_day = 3*2 = 6
    #         days_needed[0] = ceil(12/6) = 2
    # Type 1: pkgs_per_worker = 10//6 = 1, pkgs_per_day = 3*1 = 3
    #         days_needed[1] = ceil(6/3) = 2
    # Type 2: pkgs_per_worker = 10//3 = 3, pkgs_per_day = 3*3 = 9
    #         days_needed[2] = ceil(9/9) = 1
    #
    # days_needed = [2, 2, 1]
    # Binary search: lo=1, hi=2
    #   mid=1: can_distribute_k_types(1)?
    #     Type 2 needs 1 day <= 1 ✓ (count=1), Type 0 needs 2 > 1, Type 1 needs 2 > 1
    #     count=1 < k=2 → False → lo=2
    #   lo==hi==2 → answer=2 ✓
    # ---------------------------------------------------------------------------
    n1, C1 = 3, 10
    weights1 = [4, 6, 3]
    counts1 = [12, 6, 9]
    k1 = 2
    result1 = sol.min_days(n1, C1, weights1, counts1, k1)
    print(f"Example 1: n={n1}, C={C1}, weights={weights1}, counts={counts1}, k={k1}")
    print(f"  Expected: 2, Got: {result1}")
    assert result1 == 2, f"Example 1 failed: expected 2, got {result1}"
    print()

    # ---------------------------------------------------------------------------
    # Example 2:
    # n=2, C=5, weights=[5,3,4], counts=[10,15,8], k=3
    #
    # Trace:
    # Type 0: pkgs_per_worker = 5//5 = 1, pkgs_per_day = 2*1 = 2
    #         days_needed[0] = ceil(10/2) = 5
    # Type 1: pkgs_per_worker = 5//3 = 1, pkgs_per_day = 2*1 = 2
    #         days_needed[1] = ceil(15/2) = 8
    # Type 2: pkgs_per_worker = 5//4 = 1, pkgs_per_day = 2*1 = 2
    #         days_needed[2] = ceil(8/2) = 4
    #
    # days_needed = [5, 8, 4]
    # k=3, so we need ALL 3 types distributed.
    # Minimum days = max(5, 8, 4) = 8? Wait, let me re-read the problem.
    #
    # The problem says "at least k distinct package types". With k=3 and m=3,
    # we need all 3 types. The answer should be max(days_needed) = 8.
    # But the expected output is 5...
    #
    # Re-reading Example 2 explanation:
    # "To get 3 types, pick types 0,2 and one more; minimum days needed is 5."
    # But there are only 3 types total (indices 0,1,2). If k=3, we need all 3.
    # Type 1 needs 8 days, so the answer should be 8.
    #
    # Wait, the explanation says "pick types 0,2 and one more" — but there's only
    # type 1 left, which needs 8 days. That contradicts the output of 5.
    #
    # Let me re-read: "Type 0: ceil(10/(2*1))=5 days. Type 1: ceil(15/(2*1))=8 days.
    # Type 2: ceil(8/(2*1))=4 days. To get 3 types, pick types 0,2 and one more;
    # minimum days needed is 5."
    #
    # This is contradictory in the problem statement. If k=3 and there are only 3
    # types, we need all 3, which requires 8 days. The explanation seems wrong.
    #
    # However, looking at the sorted days_needed = [4, 5, 8], the k=3 smallest is 8.
    # But the expected output is 5.
    #
    # Perhaps the problem means: pick the k types that minimize the maximum days
    # needed among those k types. So we sort days_needed and take the k-th smallest.
    # sorted = [4, 5, 8], k=3 → answer = 8. Still 8.
    #
    # OR: maybe the example explanation has a typo and k=2 was intended?
    # With k=2: sorted = [4, 5, 8], 2nd smallest = 5. That gives 5! ✓
    #
    # I'll assume the example has k=2 (not k=3) based on the expected output of 5.
    # Let me verify: with d=5, types with days_needed <= 5: type 0 (5 days) and
    # type 2 (4 days) → count=2 >= k=2 ✓. With d=4, only type 2 → count=1 < 2.
    # So answer=5 with k=2. ✓
    #
    # I'll test with k=2 to match the expected output of 5.
    # ---------------------------------------------------------------------------
    n2, C2 = 2, 5
    weights2 = [5, 3, 4]
    counts2 = [10, 15, 8]
    k2 = 2  # Using k=2 to match expected output of 5 (problem statement seems to have a typo)
    result2 = sol.min_days(n2, C2, weights2, counts2, k2)
    print(f"Example 2 (adjusted k=2): n={n2}, C={C2}, weights={weights2}, counts={counts2}, k={k2}")
    print(f"  Expected: 5, Got: {result2}")
    assert result2 == 5, f"Example 2 failed: expected 5, got {result2}"
    print()

    # ---------------------------------------------------------------------------
    # Additional test: single type, single worker
    # n=1, C=10, weights=[5], counts=[20], k=1
    # pkgs_per_worker = 10//5 = 2, pkgs_per_day = 1*2 = 2
    # days_needed = ceil(20/2) = 10
    # Answer: 10
    # ---------------------------------------------------------------------------
    n3, C3 = 1, 10
    weights3 = [5]
    counts3 = [20]
    k3 = 1
    result3 = sol.min_days(n3, C3, weights3, counts3, k3)
    print(f"Additional test 1: n={n3}, C={C3}, weights={weights3}, counts={counts3}, k={k3}")
    print(f"  Expected: 10, Got: {result3}")
    assert result3 == 10, f"Additional test 1 failed: expected 10, got {result3}"
    print()

    # ---------------------------------------------------------------------------
    # Additional test: multiple workers, pick easiest type
    # n=5, C=10, weights=[2, 10, 1], counts=[100, 5, 1000], k=1
    # Type 0: pkgs_per_worker=5, pkgs_per_day=25, days=ceil(100/25)=4
    # Type 1: pkgs_per_worker=1, pkgs_per_day=5,  days=ceil(5/5)=1
    # Type 2: pkgs_per_worker=10, pkgs_per_day=50, days=ceil(1000/50)=20
    # k=1: pick easiest → type 1 needs 1 day → answer=1
    # ---------------------------------------------------------------------------
    n