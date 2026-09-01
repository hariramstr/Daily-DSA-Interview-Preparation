"""
Title: Minimum Daily Build Quota for Staged Releases

Problem Description:
A software team must publish n features in the given order. Feature i requires builds[i]
units of build effort. The team works over multiple days, but releases are staged:
each day they may work on a contiguous suffix of the remaining effort for the current
feature and then continue with later features, as long as the total effort completed
that day does not exceed a fixed daily quota Q.

A feature may be split across days, but a new rule applies: if a day starts working on
feature i, then every earlier feature must already be fully completed. In other words,
progress is always made from left to right, and partial work is allowed only on the
current frontier feature.

You are also given:
- d: the maximum number of days allowed
- k: the maximum number of features that may be split across more than one day

Task:
Find the minimum integer daily quota Q such that all features can be completed within
at most d days while splitting at most k features.

Key observations:
- Work always proceeds from left to right.
- A feature is "split" if it is worked on in more than one day.
- For a fixed quota Q, feasibility depends on:
  1) how many days are needed at minimum
  2) among schedules using that minimum number of days, whether the number of split
     features can be kept at most k

This is solved with binary search on Q and a linear-time feasibility check.
"""

from typing import List


class Solution:
    def _feasible(self, builds: List[int], d: int, k: int, quota: int) -> bool:
        """
        Check whether a given daily quota is feasible.

        The core idea:
        For a fixed quota Q, the minimum possible number of days is forced by total work:
            min_days = ceil(sum(builds) / Q)
        because work is divisible across days in order.

        However, not every boundary between consecutive days must split a feature.
        A day boundary avoids creating a split exactly when it lands on a feature boundary
        (that is, after finishing some feature completely).

        If we use exactly T days, then there are T - 1 day boundaries.
        Let "good boundaries" be boundaries that can be placed at cumulative sums that are
        multiples of Q:
            prefix_sum[i] = builds[0] + ... + builds[i]
        A boundary after feature i can be used without splitting if prefix_sum[i] is a
        multiple of Q, because then one day can end exactly there.

        Suppose T = ceil(total / Q). Then:
        - The first T - 1 boundaries are forced at cumulative work amounts:
              Q, 2Q, 3Q, ..., (T - 1)Q
        - Each such forced boundary avoids a split iff that cumulative amount equals some
          feature prefix sum.
        - Therefore:
              minimum_splits_among_min_day_schedules
              = (T - 1) - count_of_forced_boundaries_that_match_feature_boundaries

        Why is considering only the minimum number of days enough?
        Because using more days can only increase the number of day boundaries, and every
        extra boundary can never reduce the total number of split features below what is
        achievable with the minimum number of days. So if the minimum-day schedule already
        needs more than k splits, adding days does not help.

        Args:
            builds: List of feature efforts.
            d: Maximum allowed number of days.
            k: Maximum allowed number of split features.
            quota: Candidate daily quota Q.

        Returns:
            True if quota is feasible, otherwise False.

        Time complexity:
            O(n)

        Space complexity:
            O(1)
        """
        total_work: int = sum(builds)

        # If even one day cannot process the largest single uninterrupted chunk when
        # splitting is not enough, that is still okay here because features are divisible.
        # The only impossible case for divisibility would be quota <= 0, which never occurs.
        #
        # The minimum number of days required is purely determined by total work.
        min_days: int = (total_work + quota - 1) // quota

        # If the minimum possible number of days already exceeds the allowed limit,
        # then this quota is impossible.
        if min_days > d:
            return False

        # There are min_days - 1 internal day boundaries.
        # We want to know how many of the forced boundaries at Q, 2Q, ..., (min_days-1)Q
        # coincide with feature boundaries.
        #
        # We scan prefix sums from left to right and count how many forced multiples of Q
        # appear as exact prefix sums.
        good_boundaries: int = 0
        next_forced_boundary: int = quota

        # We only care about internal boundaries, not the final total_work boundary.
        last_internal_boundary: int = (min_days - 1) * quota

        prefix_sum: int = 0
        for effort in builds:
            prefix_sum += effort

            # If this prefix sum has already passed all internal forced boundaries,
            # we can stop early.
            if next_forced_boundary > last_internal_boundary:
                break

            # Because prefix sums are strictly increasing (all builds[i] >= 1),
            # each forced boundary can match at most one prefix sum.
            if prefix_sum == next_forced_boundary:
                good_boundaries += 1
                next_forced_boundary += quota

            # If prefix_sum > next_forced_boundary, that means the forced boundary lies
            # inside the current feature, so it necessarily causes a split. We do not
            # advance next_forced_boundary here because future prefix sums might match
            # later forced boundaries, but this particular one is already known to be bad.
            #
            # To keep the logic simple and linear, we can advance through all forced
            # boundaries that are strictly less than the current prefix sum, because none
            # of them can ever be matched by a future larger prefix sum.
            while next_forced_boundary <= last_internal_boundary and next_forced_boundary < prefix_sum:
                next_forced_boundary += quota

            # After skipping smaller forced boundaries, check once more whether the current
            # prefix sum matches the next forced boundary.
            if next_forced_boundary <= last_internal_boundary and prefix_sum == next_forced_boundary:
                good_boundaries += 1
                next_forced_boundary += quota

        total_internal_boundaries: int = min_days - 1
        min_required_splits: int = total_internal_boundaries - good_boundaries

        return min_required_splits <= k

    def minimum_daily_quota(self, builds: List[int], d: int, k: int) -> int:
        """
        Compute the minimum feasible daily quota using binary search.

        Binary search details:
        - If quota Q is feasible, then any larger quota is also feasible.
        - Therefore the answer is the smallest feasible Q.

        Lower bound:
        - At least ceil(total_work / d), otherwise even using all d days is not enough.
        - Also at least 1, but builds are positive so ceil(total_work / d) already covers it.

        Upper bound:
        - total_work, because finishing everything in one day is always possible with that
          quota, and 1 day <= d is guaranteed only if d >= 1, which it is.

        Args:
            builds: List of feature efforts.
            d: Maximum allowed number of days.
            k: Maximum allowed number of split features.

        Returns:
            The minimum integer daily quota.

        Time complexity:
            O(n log(sum(builds)))

        Space complexity:
            O(1)
        """
        total_work: int = sum(builds)

        # Best possible theoretical lower bound from total capacity across d days.
        left: int = (total_work + d - 1) // d

        # Safe upper bound: do all work in one day.
        right: int = total_work

        # Standard binary search for the first feasible quota.
        while left < right:
            mid: int = (left + right) // 2

            if self._feasible(builds, d, k, mid):
                right = mid
            else:
                left = mid + 1

        return left


if __name__ == "__main__":
    solution = Solution()

    # Example 1
    builds1: List[int] = [7, 2, 5, 10, 8]
    d1: int = 3
    k1: int = 1
    result1: int = solution.minimum_daily_quota(builds1, d1, k1)
    print(result1)  # Expected: 14

    # Example 2
    builds2: List[int] = [9, 9, 9]
    d2: int = 2
    k2: int = 0
    result2: int = solution.minimum_daily_quota(builds2, d2, k2)
    print(result2)  # Expected: 18