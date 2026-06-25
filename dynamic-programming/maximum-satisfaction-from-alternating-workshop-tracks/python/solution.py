"""
Title: Maximum Satisfaction from Alternating Workshop Tracks

Problem Description:
A conference offers a sequence of workshops over N time slots. For each slot i, you may
attend either the Engineering track or the Design track. If you attend Engineering in
slot i, you gain engineering[i] satisfaction points. If you attend Design, you gain
design[i] satisfaction points.

However, the organizers want attendees to avoid staying in the same track for too long.
You are given an integer K, and you may not attend more than K consecutive workshops
from the same track. In other words, any valid schedule must ensure that every maximal
run of Engineering choices has length at most K, and every maximal run of Design choices
also has length at most K.

Your task is to compute the maximum total satisfaction possible across all N slots.

This is a dynamic programming problem because the best choice at each time slot depends
not only on the current slot's values, but also on how many consecutive times you have
already chosen the current track.

Constraints:
- 1 <= N <= 100000
- 1 <= K <= N
- engineering.length == design.length == N
- 0 <= engineering[i], design[i] <= 10000
- It is guaranteed that at least one valid schedule exists.

Example 1:
Input:
engineering = [8, 3, 5, 7]
design = [4, 6, 2, 9]
K = 2
Output:
28

Example 2:
Input:
engineering = [10, 10, 1, 10, 10]
design = [1, 1, 20, 1, 1]
K = 2
Output:
60
"""

from collections import deque
from typing import Deque, List


class Solution:
    def max_satisfaction(self, engineering: List[int], design: List[int], k: int) -> int:
        """
        Compute the maximum total satisfaction while never taking more than k
        consecutive workshops from the same track.

        Args:
            engineering: Satisfaction values for choosing Engineering at each slot.
            design: Satisfaction values for choosing Design at each slot.
            k: Maximum allowed length of any consecutive run of the same track.

        Returns:
            The maximum total satisfaction achievable.

        Time complexity:
            O(n), where n is the number of slots.

        Space complexity:
            O(k), due to the sliding-window deques used to maintain candidate states.
        """
        n: int = len(engineering)

        # We will use dynamic programming, but we must do it efficiently because
        # N can be as large as 100000. A naive DP that stores all run lengths for
        # every position would be O(N * K), which can be too slow when K is large.
        #
        # Instead, we derive a more efficient recurrence:
        #
        # Let:
        #   dp_e[i] = best total satisfaction for slots [0..i] if slot i is Engineering
        #   dp_d[i] = best total satisfaction for slots [0..i] if slot i is Design
        #
        # If slot i ends with an Engineering run of length t (1 <= t <= k), then:
        #   - slots [i-t+1 .. i] are all Engineering
        #   - slot i-t is either Design, or does not exist if the run starts at 0
        #
        # Therefore:
        #   dp_e[i] = max over t in [1..k]:
        #       sum(engineering[i-t+1 .. i]) + dp_d[i-t]
        #   where dp_d[-1] is treated as 0 (meaning we started directly with Engineering)
        #
        # Similarly:
        #   dp_d[i] = max over t in [1..k]:
        #       sum(design[i-t+1 .. i]) + dp_e[i-t]
        #   where dp_e[-1] is treated as 0
        #
        # To evaluate these quickly, we use prefix sums:
        #   pref_e[x] = engineering[0] + ... + engineering[x-1]
        #   pref_d[x] = design[0] + ... + design[x-1]
        #
        # Then:
        #   sum(engineering[l .. r]) = pref_e[r+1] - pref_e[l]
        #
        # So:
        #   dp_e[i] = pref_e[i+1] + max over j in [i-k, i-1]:
        #       (dp_d[j] - pref_e[j+1])
        #   plus also the possibility j = -1:
        #       0 - pref_e[0] = 0
        #
        # Here j = i - t is the index just before the final Engineering run.
        #
        # This means for each i, dp_e[i] only needs the maximum value of:
        #   dp_d[j] - pref_e[j+1]
        # over a sliding window of valid j values.
        #
        # We can maintain that maximum with a monotonic deque.
        #
        # We do the symmetric thing for dp_d[i].

        # Build prefix sums so that we can query any contiguous segment sum in O(1).
        pref_e: List[int] = [0] * (n + 1)
        pref_d: List[int] = [0] * (n + 1)

        for i in range(n):
            pref_e[i + 1] = pref_e[i] + engineering[i]
            pref_d[i + 1] = pref_d[i] + design[i]

        # These arrays store the best total if the current slot ends in Engineering
        # or Design respectively.
        dp_e: List[int] = [0] * n
        dp_d: List[int] = [0] * n

        # Each deque stores candidate indices j for the recurrence.
        #
        # For eng_candidates:
        #   candidate value for index j is:
        #       dp_d[j] - pref_e[j+1]
        #   and we also include the virtual index j = -1 with value 0.
        #
        # For des_candidates:
        #   candidate value for index j is:
        #       dp_e[j] - pref_d[j+1]
        #   and we also include the virtual index j = -1 with value 0.
        #
        # We store actual indices in the deque. We represent the virtual index -1
        # explicitly as integer -1.
        eng_candidates: Deque[int] = deque([-1])
        des_candidates: Deque[int] = deque([-1])

        def eng_value(index: int) -> int:
            """
            Return the candidate value used to compute dp_e for a given previous split index.

            Args:
                index: Previous split index j, or -1 for the virtual starting state.

            Returns:
                The value dp_d[j] - pref_e[j+1], or 0 for j = -1.

            Time complexity:
                O(1)

            Space complexity:
                O(1)
            """
            if index == -1:
                return 0
            return dp_d[index] - pref_e[index + 1]

        def des_value(index: int) -> int:
            """
            Return the candidate value used to compute dp_d for a given previous split index.

            Args:
                index: Previous split index j, or -1 for the virtual starting state.

            Returns:
                The value dp_e[j] - pref_d[j+1], or 0 for j = -1.

            Time complexity:
                O(1)

            Space complexity:
                O(1)
            """
            if index == -1:
                return 0
            return dp_e[index] - pref_d[index + 1]

        for i in range(n):
            # ------------------------------------------------------------
            # Step 1: Remove indices that are no longer valid for slot i.
            # ------------------------------------------------------------
            # For dp_e[i], valid split indices j satisfy:
            #   i - k <= j <= i - 1
            # and also j can be -1 if the run starts from the beginning.
            #
            # Any real index j < i - k is too old and would imply a run longer than k.
            while eng_candidates and eng_candidates[0] != -1 and eng_candidates[0] < i - k:
                eng_candidates.popleft()

            while des_candidates and des_candidates[0] != -1 and des_candidates[0] < i - k:
                des_candidates.popleft()

            # The front of each deque always stores the best candidate value among
            # currently valid indices because we maintain the deque in decreasing
            # order of candidate value.
            best_for_e: int = eng_value(eng_candidates[0])
            best_for_d: int = des_value(des_candidates[0])

            # ------------------------------------------------------------
            # Step 2: Compute DP values for the current slot i.
            # ------------------------------------------------------------
            # Using the recurrence:
            #   dp_e[i] = pref_e[i+1] + max_j(dp_d[j] - pref_e[j+1])
            #   dp_d[i] = pref_d[i+1] + max_j(dp_e[j] - pref_d[j+1])
            dp_e[i] = pref_e[i + 1] + best_for_e
            dp_d[i] = pref_d[i + 1] + best_for_d

            # ------------------------------------------------------------
            # Step 3: Insert current index i as a future candidate.
            # ------------------------------------------------------------
            # Now that dp_e[i] and dp_d[i] are known, index i can serve as a split
            # point for future positions.
            #
            # We maintain each deque as monotonic decreasing by candidate value:
            #   - Before appending i, remove all indices from the back whose value
            #     is <= the new value, because they can never be better than i for
            #     any future position while i remains in range.
            current_eng_value: int = eng_value(i)
            while eng_candidates and eng_value(eng_candidates[-1]) <= current_eng_value:
                eng_candidates.pop()
            eng_candidates.append(i)

            current_des_value: int = des_value(i)
            while des_candidates and des_value(des_candidates[-1]) <= current_des_value:
                des_candidates.pop()
            des_candidates.append(i)

        # The final answer can end in either Engineering or Design on the last slot.
        return max(dp_e[-1], dp_d[-1])


if __name__ == "__main__":
    solution = Solution()

    engineering_1: List[int] = [8, 3, 5, 7]
    design_1: List[int] = [4, 6, 2, 9]
    k_1: int = 2
    result_1: int = solution.max_satisfaction(engineering_1, design_1, k_1)
    print(result_1)  # Expected: 28

    engineering_2: List[int] = [10, 10, 1, 10, 10]
    design_2: List[int] = [1, 1, 20, 1, 1]
    k_2: int = 2
    result_2: int = solution.max_satisfaction(engineering_2, design_2, k_2)
    print(result_2)  # Expected: 60