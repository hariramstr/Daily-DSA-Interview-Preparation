"""
Title: Minimum Cooldown to Launch K Rockets

Problem Description:
A spaceport has n launch pads arranged along a straight line. The position of the i-th
pad is given by pads[i], and the array is sorted in non-decreasing order. You want to
schedule exactly k rocket launches, choosing k distinct pads. For safety reasons, any
two chosen pads must be at least d units apart, where d is a global cooldown distance
applied to the entire schedule.

Your task is to compute the largest possible value of d such that it is still possible
to choose k pads satisfying the distance requirement. In other words, maximize the
minimum pairwise distance between consecutive selected launch pads.

Return the maximum feasible cooldown distance.

This problem is intended to be solved efficiently for large inputs. A brute-force search
over all subsets of pads will not pass. Think carefully about how the feasibility of a
candidate distance changes as the distance increases, and how that property can be
exploited.

Constraints:
- 2 <= n <= 200000
- 2 <= k <= n
- 0 <= pads[i] <= 10^18
- pads is sorted in non-decreasing order
- Multiple pads may share the same position, but only one rocket can be launched from
  each pad index
"""

from typing import List


class Solution:
    def can_place_k_pads(self, pads: List[int], k: int, distance: int) -> bool:
        """
        Check whether it is possible to choose at least k pads such that every newly
        chosen pad is at least `distance` units away from the previously chosen pad.

        This uses a greedy strategy:
        - Always choose the earliest possible pad.
        - Then keep choosing the next earliest pad that is far enough away.

        Why greedy works:
        - Choosing an earlier valid pad leaves as much room as possible for future choices.
        - Therefore, if this greedy process cannot place k pads, no other strategy can.

        Args:
            pads: Sorted list of pad positions.
            k: Number of pads that must be chosen.
            distance: Candidate minimum required distance between consecutive chosen pads.

        Returns:
            True if at least k pads can be chosen with the given minimum distance,
            otherwise False.

        Time complexity:
            O(n), where n is the number of pads.

        Space complexity:
            O(1), ignoring input storage.
        """
        # We always choose the first pad initially.
        # This is the earliest possible choice and is optimal for a greedy feasibility check.
        chosen_count: int = 1
        last_chosen_position: int = pads[0]

        # Scan through the remaining pads from left to right.
        # Whenever we find a pad that is at least `distance` away from the last chosen pad,
        # we choose it immediately.
        for i in range(1, len(pads)):
            # If the current pad is far enough from the last chosen one,
            # it can safely be added to our schedule.
            if pads[i] - last_chosen_position >= distance:
                chosen_count += 1
                last_chosen_position = pads[i]

                # Early exit:
                # As soon as we have chosen k pads, we know this distance is feasible.
                if chosen_count >= k:
                    return True

        # If we finish scanning and still have fewer than k chosen pads,
        # then this candidate distance is not feasible.
        return False

    def maximum_cooldown(self, pads: List[int], k: int) -> int:
        """
        Compute the largest possible minimum distance between consecutive selected pads
        when choosing exactly k distinct pads.

        Core idea:
        - If a distance `d` is feasible, then every smaller distance is also feasible.
        - If a distance `d` is not feasible, then every larger distance is also not feasible.
        - This monotonic behavior allows binary search on the answer.

        Binary search range:
        - Minimum possible answer: 0
        - Maximum possible answer: pads[-1] - pads[0]

        Args:
            pads: Sorted list of pad positions.
            k: Number of pads to choose.

        Returns:
            The maximum feasible cooldown distance.

        Time complexity:
            O(n log R), where:
            - n is the number of pads
            - R is pads[-1] - pads[0]

        Space complexity:
            O(1), ignoring input storage.
        """
        # The smallest possible cooldown distance is 0.
        # This matters when duplicate positions exist and we may need to choose multiple
        # pads located at the same coordinate.
        left: int = 0

        # The largest possible cooldown distance cannot exceed the full spread
        # between the leftmost and rightmost pad.
        right: int = pads[-1] - pads[0]

        # `answer` will store the best feasible distance found so far.
        answer: int = 0

        # Standard binary search on the answer space.
        # We search for the maximum feasible distance.
        while left <= right:
            # Midpoint candidate distance.
            mid: int = (left + right) // 2

            # Check whether this candidate distance is feasible.
            if self.can_place_k_pads(pads, k, mid):
                # If feasible, record it as a valid answer.
                answer = mid

                # Since we want the largest feasible distance,
                # try searching to the right for a bigger valid value.
                left = mid + 1
            else:
                # If not feasible, all larger distances are also impossible.
                # So we must search the smaller half.
                right = mid - 1

        # After binary search completes, `answer` is the largest feasible cooldown.
        return answer


if __name__ == "__main__":
    solution = Solution()

    # Example 1 from the problem statement
    pads1: List[int] = [1, 2, 8, 12, 17]
    k1: int = 3
    result1: int = solution.maximum_cooldown(pads1, k1)
    print("Example 1 Result:", result1)  # Expected: 7

    # Example 2 from the problem statement
    pads2: List[int] = [0, 0, 4, 9, 13, 18]
    k2: int = 4
    result2: int = solution.maximum_cooldown(pads2, k2)
    print("Example 2 Result:", result2)  # Expected: 4

    # Additional quick sanity checks
    pads3: List[int] = [0, 0, 0, 0]
    k3: int = 2
    result3: int = solution.maximum_cooldown(pads3, k3)
    print("Additional Test 1 Result:", result3)  # Expected: 0

    pads4: List[int] = [1, 3, 6, 10, 15]
    k4: int = 5
    result4: int = solution.maximum_cooldown(pads4, k4)
    print("Additional Test 2 Result:", result4)  # Expected: 2