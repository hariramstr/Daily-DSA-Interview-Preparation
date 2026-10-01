"""
Title: Minimum Processing Power for Alternating GPU Batches

Problem Description:
A machine learning platform must execute a sequence of training batches in the given order.
The i-th batch requires work[i] units of computation. You are provisioning identical GPU
nodes, each with the same processing power P. A single node can process a contiguous group
of batches, and the time needed for that group is the sum of its work values divided by P.

Because of thermal balancing rules, nodes are assigned in alternating modes:
- the 1st used node is in "hot" mode,
- the 2nd in "cool" mode,
- the 3rd in "hot" mode again,
- and so on.

A hot-mode node may be assigned batches whose total work is at most hotLimit, while a
cool-mode node may be assigned batches whose total work is at most coolLimit.

You may split the batch list into any number of contiguous groups, but the mode of each
group is determined by its position among the groups. Every group must respect both:
1. its mode limit
2. the node's processing power deadline:
   groupWork <= P * T

Return the minimum integer processing power P such that all batches can be completed using
at most m nodes.

If it is impossible for any processing power to satisfy the alternating mode limits,
return -1.

Constraints:
- 1 <= n == work.length <= 200000
- 1 <= work[i] <= 10^9
- 1 <= m <= 200000
- 1 <= hotLimit, coolLimit <= 10^18
- 1 <= T <= 10^9
- P must be a positive integer
"""

from typing import List


class Solution:
    def minimumProcessingPower(
        self,
        work: List[int],
        m: int,
        hotLimit: int,
        coolLimit: int,
        T: int,
    ) -> int:
        """
        Compute the minimum integer processing power P that allows the batch list to be
        partitioned into at most m contiguous groups, where group modes alternate hot/cool
        starting with hot, and each group respects both its mode limit and the time limit.

        Args:
            work: List of batch workloads in fixed order.
            m: Maximum number of nodes/groups allowed.
            hotLimit: Maximum total work allowed on hot-positioned groups.
            coolLimit: Maximum total work allowed on cool-positioned groups.
            T: Maximum allowed processing time per group.

        Returns:
            The minimum positive integer processing power P, or -1 if impossible for any P.

        Time complexity:
            O(n log U), where U is the binary-search range of P.

        Space complexity:
            O(1) auxiliary space, excluding the input list.
        """
        n: int = len(work)

        # ------------------------------------------------------------
        # Step 1: Quick impossibility checks that do not depend on P.
        # ------------------------------------------------------------
        #
        # Even if P were infinitely large, the time constraint would disappear,
        # but the alternating hot/cool mode limits would still remain.
        #
        # Since the very first group must be hot, the first batch must belong to a hot group.
        # Therefore, if work[0] > hotLimit, there is no possible solution at all.
        #
        # More generally, if any single batch exceeds BOTH hotLimit and coolLimit, then that
        # batch cannot fit into any group regardless of position, so the answer is impossible.
        #
        # Also, if m <= 0 it would be invalid by constraints, so we do not need to handle it.
        if work[0] > hotLimit:
            return -1

        max_mode_limit: int = max(hotLimit, coolLimit)
        for value in work:
            if value > max_mode_limit:
                return -1

        # ------------------------------------------------------------
        # Step 2: Define a feasibility checker for a fixed P.
        # ------------------------------------------------------------
        #
        # For a chosen processing power P:
        # - any group must satisfy groupWork <= P * T due to time
        # - hot groups additionally satisfy groupWork <= hotLimit
        # - cool groups additionally satisfy groupWork <= coolLimit
        #
        # So the effective capacity of:
        # - a hot group is hotCap = min(hotLimit, P * T)
        # - a cool group is coolCap = min(coolLimit, P * T)
        #
        # We must determine whether the array can be partitioned into at most m contiguous
        # non-empty groups, with capacities alternating:
        #   hotCap, coolCap, hotCap, coolCap, ...
        #
        # Important subtlety:
        # A simple greedy "always take as many as possible in the current group" is NOT always
        # correct here, because taking too much in an early hot group can force the next cool
        # group to start at a bad position and fail, even though a smaller first hot group
        # would have worked.
        #
        # Example:
        #   work = [8, 5, 6, 4, 7], hotCap = 12, coolCap = 11, m = 3
        # Greedy max-fill hot gives [8], cool gives [5,6], hot gives [4,7] -> works.
        # But in other cases, max-fill can fail while a smaller split succeeds.
        #
        # To solve this correctly in O(n) per feasibility check, we use a dynamic interval
        # propagation idea:
        #
        # Let reachable range after exactly k groups be an interval [L_k, R_k] of indices
        # meaning:
        #   after forming exactly k valid alternating groups, we may have consumed any prefix
        #   length in that interval.
        #
        # Why is it an interval?
        # Because from any starting consumed index s, the next group can end at any index from
        # s+1 up to the furthest index allowed by the current capacity. Taking the union over
        # a contiguous range of s values still produces a contiguous range.
        #
        # Transition:
        #   If current reachable consumed-prefix interval is [L, R], and current group capacity
        #   is cap, then for each start s in [L, R], the next consumed prefix can be any e in
        #   [s+1, furthest(s, cap)].
        #
        # The union over all s in [L, R] becomes:
        #   [L + 1, max_{s in [L, R]} furthest(s, cap)]
        #
        # Since prefix sums are increasing (all work[i] >= 1), furthest(s, cap) is monotone
        # non-decreasing in s, so the maximum is attained at s = R.
        #
        # Therefore the next interval is:
        #   [L + 1, furthest(R, cap)]
        #
        # This gives an O(1) transition if we can compute furthest(R, cap) quickly.
        #
        # We compute furthest consumed prefix after starting at index R using a two-pointer
        # scan during each feasibility check. Because capacities alternate between only two
        # values, and R only moves forward, total work remains O(n).
        def can_finish(power: int) -> bool:
            """
            Check whether a given processing power is sufficient.

            Args:
                power: Candidate processing power P.

            Returns:
                True if all batches can be processed using at most m alternating groups,
                otherwise False.

            Time complexity:
                O(n)

            Space complexity:
                O(1)
            """
            time_cap: int = power * T
            hot_cap: int = min(hotLimit, time_cap)
            cool_cap: int = min(coolLimit, time_cap)

            # If the first batch cannot fit into the first (hot) group under this power,
            # then this P is immediately infeasible.
            if work[0] > hot_cap:
                return False

            # --------------------------------------------------------
            # Reachable interval after 0 groups:
            # We have consumed exactly 0 batches, so interval is [0, 0].
            #
            # Here "consumed prefix length" means:
            # - 0 means no batches processed yet
            # - n means all batches processed
            # --------------------------------------------------------
            left: int = 0
            right: int = 0

            # --------------------------------------------------------
            # Two pointers for computing furthest(right, hot_cap/cool_cap).
            #
            # For a fixed start index s (which equals current "right"), we want the largest
            # consumed prefix j such that sum(work[s:j]) <= cap.
            #
            # We maintain:
            # - hot_end: furthest consumed prefix reachable from current right using hot_cap
            # - hot_sum: sum(work[right:hot_end])
            # - cool_end / cool_sum similarly for cool_cap
            #
            # As right increases over iterations, we remove work[right] from these sums if
            # it is currently included, then extend the end pointers as far as allowed.
            #
            # Since each end pointer only moves forward from 0 to n, total cost is linear.
            # --------------------------------------------------------
            hot_end: int = 0
            hot_sum: int = 0
            cool_end: int = 0
            cool_sum: int = 0

            # We can use at most m groups. After each group count k, if n belongs to the
            # reachable interval [left, right], then all work can be finished using exactly
            # k groups, which is allowed because k <= m.
            for groups_used in range(1, m + 1):
                current_cap: int = hot_cap if groups_used % 2 == 1 else cool_cap

                # ----------------------------------------------------
                # Advance the appropriate two-pointer state so that it
                # represents the furthest reachable end from start=right.
                # ----------------------------------------------------
                if groups_used % 2 == 1:
                    # Move the hot window's start from its previous start to the new "right".
                    # If hot_end > right, then work[right] is currently inside the window
                    # [right, hot_end), so removing it shifts the start by one.
                    while hot_end < right:
                        hot_end += 1
                    # The above while should rarely iterate because right is monotone and
                    # hot_end is also monotone, but we still keep it correct.
                    #
                    # Rebuild hot_sum consistency by removing elements from the old start
                    # up to the new start. Since we do not explicitly store the old start,
                    # we instead maintain the invariant below using a separate loop.
                    #
                    # To do this cleanly, we need a persistent notion of the current start
                    # for each pointer. We derive it from previous right values by updating
                    # sums when right changes after each transition. To keep the code simple
                    # and correct, we will instead use prefix sums + binary search? That
                    # would be O(log n) per group. But m can be 2e5 and binary search outer
                    # loop also exists. We need O(n) per feasibility.
                    #
                    # Therefore, we switch to a simpler and still linear approach below:
                    # use prefix sums and a direct greedy DP over number of groups is not enough.
                    # However, the interval transition only needs furthest(R, cap), and R is
                    # monotone. We can compute it with local pointers plus explicit starts.
                    pass

            return False

        # The above nested function needs a cleaner implementation with explicit starts.
        # We redefine it properly below.

        def can_finish(power: int) -> bool:
            """
            Check whether a given processing power is sufficient.

            Args:
                power: Candidate processing power P.

            Returns:
                True if all batches can be processed using at most m alternating groups,
                otherwise False.

            Time complexity:
                O(n)

            Space complexity:
                O(1)
            """
            time_cap: int = power * T
            hot_cap: int = min(hotLimit, time_cap)
            cool_cap: int = min(coolLimit, time_cap)

            if work[0] > hot_cap:
                return False

            # Reachable consumed-prefix interval after 0 groups.
            left: int = 0
            right: int = 0

            # Explicit start positions for the two sliding windows.
            hot_start: int = 0
            hot_end: int = 0
            hot_sum: int = 0

            cool_start: int = 0
            cool_end: int = 0
            cool_sum: int = 0

            for groups_used in range(1, m + 1):
                if groups_used % 2 == 1:
                    cap = hot_cap

                    # Shift hot window start forward until it matches current "right".
                    while hot_start < right:
                        if hot_start < hot_end:
                            hot_sum -= work[hot_start]
                        hot_start += 1
                        if hot_end < hot_start:
                            hot_end = hot_start

                    # Extend hot window end as far as capacity allows.
                    while hot_end < n and hot_sum + work[hot_end] <= cap:
                        hot_sum += work[hot_end]
                        hot_end += 1

                    next_left: int = left + 1
                    next_right: int = hot_end
                else:
                    cap = cool_cap

                    # Shift cool window start forward until it matches current "right".
                    while cool_start < right:
                        if cool_start < cool_end:
                            cool_sum -= work[cool_start]
                        cool_start += 1
                        if cool_end < cool_start:
                            cool_end = cool_start

                    # Extend cool window end as far as capacity allows.
                    while cool_end < n and cool_sum + work[cool_end] <= cap:
                        cool_sum += work[cool_end]
                        cool_end += 1

                    next_left = left + 1
                    next_right = cool_end

                # If next_left > next_right, then there is no valid way to form exactly
                # this many groups from any previously reachable prefix length.
                if next_left > next_right:
                    return False

                left, right = next_left, next_right

                # If consuming all n batches is reachable after at most groups_used groups,
                # then this power is feasible.
                if left <= n <= right:
                    return True

                # If even the maximum reachable consumed prefix is still less than the number
                # of groups used, that is okay; it just means each group is non-empty.
                # No extra action needed.

            return False

        # ------------------------------------------------------------
        # Step 3: If even extremely large power cannot work, return -1.
        # ------------------------------------------------------------
        #
        # Once P*T is at least max(hotLimit, coolLimit), increasing P further does not help,
        # because the effective capacities become exactly hotLimit and coolLimit.
        #
        # So it is enough to test a power large enough to saturate the time cap beyond both
        # mode limits.
        #
        # We need P such that P*T >= max_mode_limit.
        # The smallest such integer is ceil(max_mode_limit / T).
        upper_saturating_power: int = (max_mode_limit + T - 1) // T
        upper_saturating_power = max(1, upper_saturating_power)

        if not can_finish(upper_saturating_power):
            return -1

        # ------------------------------------------------------------
        # Step 4: Binary search the minimum feasible power.
        # ------------------------------------------------------------
        #
        # Feasibility is monotone:
        # If power P works, then any larger power also works, because P*T only increases,
        # so every effective capacity min(modeLimit, P*T) stays the same or increases.
        #
        # Therefore we binary search on P in [1, upper_saturating_power].
        left_power: int = 1
        right_power: int = upper_saturating_power

        while left_power < right_power:
            mid_power: int = (left_power + right_power) // 2

            if can_finish(mid_power):
                right_power = mid_power
            else:
                left_power = mid_power + 1

        return left_power


if __name__ == "__main__":
    solution = Solution()

    # Example 1
    work1 = [8, 5, 6, 4, 7]
    m1 = 3
    hot_limit1 = 13
    cool_limit1 = 11
    t1 = 2
    result1 = solution.minimumProcessingPower(work1, m1, hot_limit1, cool_limit1, t1)
    print(result1)  # Expected: 6

    # Example 2
    work2 = [9, 9, 9]
    m2 = 2
    hot_limit2 = 8
    cool_limit2 = 20
    t2 = 3
    result2 = solution.minimumProcessingPower(work2, m2, hot_limit2, cool_limit2, t2)
    print(result2)  # Expected: -1