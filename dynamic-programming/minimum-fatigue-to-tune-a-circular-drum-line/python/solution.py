"""
Title: Minimum Fatigue to Tune a Circular Drum Line

Problem Description:
A festival stage has n drums arranged in a circle. Drum i currently has pitch current[i],
and it must be retuned to target[i]. You may tune the drums in any order, but because the
drums are mounted on a shared circular frame, tuning one drum affects the effort needed to
tune its neighbors.

If you tune a contiguous block of drums from index l to index r in clockwise order as one
phase, then during that phase you may choose exactly one drum k in that block to tune last.
All other drums in the block must be completely tuned before drum k is finalized. The fatigue
cost contributed by drum k when it is finalized is:

abs(current[k] - target[k]) + leftPenalty + rightPenalty

where leftPenalty is 0 if there is no untuned drum remaining on the clockwise-left side
inside the current block, otherwise it is abs(target[k] - target[left]), where left is the
nearest drum in the block still not finalized before k; similarly, rightPenalty is defined
using the nearest untuned drum on the clockwise-right side inside the current block.

Your task is to compute the minimum total fatigue required to finalize all drums.

Because the drums are arranged in a circle, drum 0 and drum n - 1 are adjacent. You may break
the circle at any position before planning phases, but the resulting total fatigue must respect
the original circular adjacency.

Return the minimum possible total fatigue.

Constraints:
- 1 <= n <= 300
- 0 <= current[i], target[i] <= 10^9
- current.length == target.length == n
"""

from typing import List


class Solution:
    def minimumFatigue(self, current: List[int], target: List[int]) -> int:
        """
        Compute the minimum total fatigue for the circular drum line.

        The unavoidable part is the direct retuning cost:
            sum(abs(current[i] - target[i]))

        The optimization concerns only the extra structural penalties caused by the order
        in which drums are finalized.

        We solve the circular problem by considering every possible drum as the globally
        last finalized drum. If drum s is last on the circle, then every other drum lies
        on the open path from s+1 to s-1, and their extra penalties become a linear interval
        DP problem. Finally, when s itself is finalized last, both of its circle neighbors
        are still present, so it pays one final extra penalty involving those two boundary
        drums.

        Args:
            current: Current pitches of the drums.
            target: Desired target pitches of the drums.

        Returns:
            The minimum possible total fatigue.

        Time complexity:
            O(n^3)

        Space complexity:
            O(n^2)
        """
        n: int = len(current)

        # Base cost is unavoidable no matter what order we choose.
        # Every drum must eventually be changed from current[i] to target[i].
        base_cost: int = sum(abs(c - t) for c, t in zip(current, target))

        # Edge case:
        # If there is only one drum, there are no neighbors and therefore no extra penalty.
        if n == 1:
            return base_cost

        # Duplicate the target array so that any circular segment of length <= n
        # can be viewed as a normal contiguous interval in this doubled array.
        doubled_target: List[int] = target + target

        # We will try every possible drum `s` as the globally last finalized drum.
        # For a fixed `s`, the remaining drums form the linear interval:
        #   [s + 1, s + n - 1] in the doubled array
        #
        # We need a DP for the following linear problem:
        # Given a line of drums, repeatedly finalize one of the two ends of the
        # still-unfinished interval. Why only ends?
        #
        # Because in the reverse process of "building up" the set of unfinished drums,
        # when a drum is finalized while some interval remains, the nearest unfinished
        # drums that matter are exactly the current boundaries around it. This is the
        # classic interval-DP structure. Equivalently, in the forward elimination view,
        # the last drum chosen inside any interval splits the interval into left and right
        # subproblems, and the optimal substructure can be compressed into a boundary DP.
        #
        # dp[l][r][0 or 1] meaning:
        #   We have already reduced the original line segment to the still-unfinished
        #   interval [l, r]. Among this interval, the next drum to finalize from outside
        #   perspective is either:
        #     state 0: the left boundary drum l is the "active" boundary representative
        #     state 1: the right boundary drum r is the "active" boundary representative
        #
        # A simpler and standard interpretation used here:
        #   cost_left[l][r]  = minimum extra penalty needed to finalize all drums in [l, r]
        #                      except that the very last finalized drum of this interval
        #                      is l.
        #   cost_right[l][r] = same, but the very last finalized drum is r.
        #
        # Transition:
        #   If l is last in [l, r], then before finalizing l, the interval [l+1, r]
        #   must already have been processed down to one last drum, which is either l+1
        #   or r. At the moment l is finalized, its only remaining unfinished neighbor
        #   inside [l, r] is that last drum from [l+1, r], so it pays exactly one penalty:
        #       abs(target[l] - target[last_of_subinterval])
        #
        #   Therefore:
        #     cost_left[l][r] =
        #         min(
        #             cost_left[l+1][r]  + abs(t[l] - t[l+1]),
        #             cost_right[l+1][r] + abs(t[l] - t[r])
        #         )
        #
        #   Symmetrically:
        #     cost_right[l][r] =
        #         min(
        #             cost_left[l][r-1]  + abs(t[r] - t[l]),
        #             cost_right[l][r-1] + abs(t[r] - t[r-1])
        #         )
        #
        # Base:
        #   For a single drum interval [i, i], there is no extra penalty inside that
        #   interval because it has no unfinished neighbor within the interval.
        #
        # After solving the open path [L, R] = [s+1, s+n-1]:
        #   If the last remaining drum of that path is L, then when we finally finalize s
        #   on the original circle, its two still-relevant neighbors are:
        #       left side boundary = that last path drum L
        #       right side boundary = same if path length is 1, otherwise the other circle
        #                             side is also represented by the same remaining path drum
        #
        # However, on the circle, when s is globally last, immediately before finalizing s
        # all other drums are already finalized, so there are actually NO untuned drums left.
        # The structural penalties are paid during recursive interval finalizations, not after.
        #
        # The correct circular reduction is:
        #   choose s as a fixed "root" that is never charged against both sides simultaneously,
        #   and solve the open path of the other n-1 drums where each finalized drum pays
        #   relative to the remaining path. This exactly captures the circular problem because
        #   one drum can be treated as the break point with zero wrap penalty.
        #
        # Thus for each break s, the extra cost is simply the minimum cost to reduce the path
        # [s+1, s+n-1] to one drum, regardless of which endpoint is last, and then that final
        # path drum can be finalized with no additional path neighbor.
        #
        # For a path interval [L, R], the minimum extra structural cost is:
        #   min(cost_left[L][R], cost_right[L][R])
        #
        # We compute this for every length up to n-1 in the doubled array, then test all breaks.

        size: int = 2 * n

        # Allocate DP tables.
        # left_dp[l][r]  = minimum extra cost for interval [l, r] if l is finalized last.
        # right_dp[l][r] = minimum extra cost for interval [l, r] if r is finalized last.
        left_dp: List[List[int]] = [[0] * size for _ in range(size)]
        right_dp: List[List[int]] = [[0] * size for _ in range(size)]

        # Build intervals by increasing length.
        # Length 1 intervals already have value 0.
        for length in range(2, n):
            for l in range(0, size - length + 1):
                r: int = l + length - 1

                # Transition for making l the last drum in [l, r].
                # Two possibilities for the last drum of subinterval [l+1, r]:
                #   - it is l+1
                #   - it is r
                #
                # In each case, when l is finally finalized, it pays one penalty to that
                # remaining neighbor inside the interval.
                option1_left: int = left_dp[l + 1][r] + abs(doubled_target[l] - doubled_target[l + 1])
                option2_left: int = right_dp[l + 1][r] + abs(doubled_target[l] - doubled_target[r])
                left_dp[l][r] = min(option1_left, option2_left)

                # Symmetric transition for making r the last drum in [l, r].
                option1_right: int = left_dp[l][r - 1] + abs(doubled_target[r] - doubled_target[l])
                option2_right: int = right_dp[l][r - 1] + abs(doubled_target[r] - doubled_target[r - 1])
                right_dp[l][r] = min(option1_right, option2_right)

        # Evaluate every possible break point on the circle.
        # If we break before drum s, the remaining open path is the next n-1 drums:
        #   [s+1, s+n-1] in doubled indexing.
        #
        # For path length 0 (n == 1) we already returned.
        # For path length 1 (n == 2), the extra cost is 0 because a single drum on a path
        # has no internal neighbor penalty.
        best_extra: int = 10**30

        for s in range(n):
            l = s + 1
            r = s + n - 1

            if l == r:
                extra = 0
            else:
                extra = min(left_dp[l][r], right_dp[l][r])

            if extra < best_extra:
                best_extra = extra

        return base_cost + best_extra


if __name__ == "__main__":
    solution = Solution()

    current1 = [5, 1, 4]
    target1 = [3, 2, 6]
    result1 = solution.minimumFatigue(current1, target1)
    print(result1)

    current2 = [8, 8, 8, 8]
    target2 = [1, 10, 1, 10]
    result2 = solution.minimumFatigue(current2, target2)
    print(result2)