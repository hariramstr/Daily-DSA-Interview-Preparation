"""
Title: Minimum Refills to Keep a Garden Strip Green

Problem Description:
A city park has a long straight garden strip divided into n sections. You are given
an integer array waterNeed where waterNeed[i] is the amount of water required by
section i. A maintenance cart starts with a watering tank of capacity cap units,
and it moves from left to right, watering sections in order.

Watering section i reduces the current water in the tank by waterNeed[i]. If the
cart does not have enough water to fully water the next section, it must return to
the refill station, instantly refill the tank back to cap, and then continue.
Returning for a refill can happen only before watering a section, never in the
middle of one. Your task is to compute the minimum number of refills needed to
water all sections in order.

However, there is one optimization: exactly once during the entire process, the
cart may skip refilling even if the tank is insufficient, but only if the deficit
for that section is at most bonus units. In that case, think of it as a temporary
emergency reserve that can be used once and only once. After using this reserve,
the tank becomes 0 after watering that section, and future watering still follows
the normal refill rules.

Return the minimum number of refills required.

Constraints:
- 1 <= n <= 200000
- 1 <= waterNeed[i] <= cap <= 1000000000
- 0 <= bonus <= 1000000000
- The emergency reserve can be used at most once
"""

from typing import List


class Solution:
    def minimum_refills(self, waterNeed: List[int], cap: int, bonus: int) -> int:
        """
        Compute the minimum number of refills needed to water all sections in order,
        when one one-time emergency reserve may be used to avoid a refill if the
        shortage for that section is at most `bonus`.

        The key idea is dynamic programming with only two states while scanning
        from left to right:
        1. Best situation if we have NOT used the reserve yet.
        2. Best situation if we HAVE already used the reserve.

        For each state, we track:
        - the minimum number of refills so far
        - among all ways achieving that minimum, the maximum remaining water

        Why maximum remaining water for equal refill count?
        Because having more water can only help in future sections. So for the same
        number of refills, a state with more remaining water dominates one with less.

        Args:
            waterNeed: List of water requirements for each section.
            cap: Tank capacity after each refill.
            bonus: Maximum allowed deficit for the one-time reserve use.

        Returns:
            The minimum number of refills required.

        Time complexity:
            O(n), where n is the number of sections.

        Space complexity:
            O(1), because we keep only a constant number of DP states.
        """
        # We represent an unreachable state by using a very large refill count.
        # Any real answer will be much smaller than this.
        inf: int = 10**18

        # dp0_refills / dp0_water:
        #   Best state after processing the current prefix when the reserve
        #   has NOT been used yet.
        #
        # dp1_refills / dp1_water:
        #   Best state after processing the current prefix when the reserve
        #   HAS already been used.
        #
        # Initially:
        # - We have watered nothing.
        # - We start with a full tank.
        # - No refill has happened.
        # - Reserve has not been used.
        dp0_refills: int = 0
        dp0_water: int = cap

        # The "reserve already used" state is impossible at the very beginning.
        dp1_refills: int = inf
        dp1_water: int = -1

        # Process each section from left to right exactly once.
        for need in waterNeed:
            # These variables will store the next-step DP values after watering
            # the current section.
            next0_refills: int = inf
            next0_water: int = -1
            next1_refills: int = inf
            next1_water: int = -1

            # ---------------------------------------------------------------
            # Transition from state 0: reserve not used yet
            # ---------------------------------------------------------------
            if dp0_refills < inf:
                # Case A: We have enough water already.
                # Then we simply water this section and reduce remaining water.
                if dp0_water >= need:
                    cand_refills: int = dp0_refills
                    cand_water: int = dp0_water - need

                    # Update next state "reserve still unused".
                    # We prefer:
                    # 1) fewer refills
                    # 2) if tied, more remaining water
                    if (
                        cand_refills < next0_refills
                        or (
                            cand_refills == next0_refills
                            and cand_water > next0_water
                        )
                    ):
                        next0_refills = cand_refills
                        next0_water = cand_water
                else:
                    # Case B: Not enough water.
                    # Normal action: refill before this section.
                    # After refill, tank becomes full (cap), then we water `need`,
                    # so remaining water is cap - need.
                    cand_refills = dp0_refills + 1
                    cand_water = cap - need

                    if (
                        cand_refills < next0_refills
                        or (
                            cand_refills == next0_refills
                            and cand_water > next0_water
                        )
                    ):
                        next0_refills = cand_refills
                        next0_water = cand_water

                    # Case C: Use the one-time reserve right now instead of refilling.
                    # This is allowed only if the deficit is at most `bonus`.
                    # Deficit = need - current_water.
                    #
                    # After using the reserve:
                    # - this section is completed
                    # - reserve becomes used
                    # - tank becomes exactly 0
                    deficit: int = need - dp0_water
                    if deficit <= bonus:
                        cand_refills = dp0_refills
                        cand_water = 0

                        if (
                            cand_refills < next1_refills
                            or (
                                cand_refills == next1_refills
                                and cand_water > next1_water
                            )
                        ):
                            next1_refills = cand_refills
                            next1_water = cand_water

            # ---------------------------------------------------------------
            # Transition from state 1: reserve already used
            # ---------------------------------------------------------------
            if dp1_refills < inf:
                # Since reserve is already used, only normal watering/refill rules apply.
                if dp1_water >= need:
                    # Enough water: just water the section.
                    cand_refills = dp1_refills
                    cand_water = dp1_water - need
                else:
                    # Not enough water: must refill before this section.
                    cand_refills = dp1_refills + 1
                    cand_water = cap - need

                # Update next state "reserve already used".
                if (
                    cand_refills < next1_refills
                    or (
                        cand_refills == next1_refills
                        and cand_water > next1_water
                    )
                ):
                    next1_refills = cand_refills
                    next1_water = cand_water

            # Move to the next section by replacing current DP states with the
            # newly computed ones.
            dp0_refills, dp0_water = next0_refills, next0_water
            dp1_refills, dp1_water = next1_refills, next1_water

        # Final answer is the better of:
        # - finishing without ever using the reserve
        # - finishing after using the reserve at some point
        return min(dp0_refills, dp1_refills)


if __name__ == "__main__":
    solution = Solution()

    # Example 1
    water_need_1: List[int] = [2, 4, 3, 5, 2]
    cap_1: int = 6
    bonus_1: int = 2
    result_1: int = solution.minimum_refills(water_need_1, cap_1, bonus_1)
    print(result_1)  # Expected: 2

    # Example 2
    water_need_2: List[int] = [5, 1, 4, 4]
    cap_2: int = 5
    bonus_2: int = 0
    result_2: int = solution.minimum_refills(water_need_2, cap_2, bonus_2)
    print(result_2)  # Expected: 2

    # Additional quick checks
    extra_1: List[int] = [3]
    print(solution.minimum_refills(extra_1, 5, 10))  # Expected: 0

    extra_2: List[int] = [4, 4]
    print(solution.minimum_refills(extra_2, 5, 3))  # Expected: 0
