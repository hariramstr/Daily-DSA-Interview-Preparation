"""
Title: Maximum Reliability from Staged Model Rollouts

Problem Description:
A machine learning platform plans to deploy models over the next n days. On day i, the platform may either skip deployment or launch exactly one candidate model for that day. Each candidate model belongs to one of m architecture families, represented by an integer family[i], and provides an immediate reliability gain gain[i].

However, repeatedly deploying models from the same family too close together causes hidden coupling risk. To control this, the platform defines a cooldown window k: if you deploy a model from family x on day i, then deploying another model from the same family on any of the next k days is not allowed. Deploying a different family is always allowed, and skipping a day resets nothing.

Your task is to compute the maximum total reliability gain achievable over all n days.

Formally, choose a subset of days to deploy such that for any two chosen days a < b with family[a] == family[b], we must have b - a > k. The score is the sum of gain[i] over all chosen days. Return the maximum possible score.

Constraints:
- 1 <= n <= 200000
- 1 <= m <= 200000
- 1 <= family[i] <= m
- 1 <= gain[i] <= 10^9
- 0 <= k <= n

Notes:
- The answer always fits in a 64-bit signed integer.
- Efficient dynamic programming with per-family best-prefix tracking is expected.
"""

from typing import Dict, List


class Solution:
    def maximum_reliability(self, family: List[int], gain: List[int], k: int) -> int:
        """
        Compute the maximum total reliability gain under same-family cooldown rules.

        The key dynamic programming idea:
        - Let dp[i] be the best answer using only days [0..i].
        - For day i, we can either:
          1) Skip day i, so answer stays dp[i - 1]
          2) Take day i, and then we may also take any valid set from earlier days,
             as long as the last chosen day of the same family is more than k days earlier.

        A direct DP over all previous days would be too slow. Instead, for each family,
        we maintain the best value of:
            dp[t] - prefix_sum_of_recent_same_family_conflicts
        more concretely implemented as:
            best_before_family[f] = maximum dp[j]
            over all j that are valid "prefix endpoints" before a future use of family f.

        A simpler and correct formulation is:
        - When processing day i of family f, the best total if we take day i is:
              gain[i] + best_allowed_prefix_for_family[f]
        - Here best_allowed_prefix_for_family[f] means the maximum dp[p]
          where p <= i - k - 1 relative to the previous same-family restriction.
        - We can update this lazily by sweeping days from left to right and, for each family,
          feeding in dp values from indices that have become old enough.

        More specifically:
        - For day i, a previous deployment of the same family must be at day <= i - k - 1.
        - Therefore, when considering taking day i, any solution ending at or before day i-k-1
          is always safe as a prefix, regardless of what families were used there.
        - Since different families never conflict, the only issue is whether the prefix already
          contains the same family too recently. But if the whole prefix ends by i-k-1, then
          every chosen day in that prefix is at most i-k-1, so any same-family choice is also safe.

        This means:
            take_i = gain[i] + dp[i - k - 1]   if i - k - 1 >= 0
                     gain[i]                    otherwise

        However, that would incorrectly forbid choosing day i together with recent days of
        different families. We need a stronger DP:
        - We want the best total among all valid subsets of days < i, with the only extra rule
          that family[i] has not been used in the last k days.
        - So we track, for each family f:
              best_excluding_recent[f]
          = best total achievable so far where the most recent chosen day of family f
            is more than k days ago (or f has never been chosen).

        Transition:
        - skip day i: global_best stays the same
        - take day i of family f:
              candidate = best_excluding_recent[f] + gain[i]

        Then after processing day i, this new total becomes a state for all families.
        The challenge is updating best_excluding_recent efficiently as days age out of the
        cooldown window.

        Efficient structure:
        - Let dp_take[i] = best total of a valid plan that definitely takes day i.
        - Let global_best_up_to[i] = max valid total using days [0..i].
        - For each family f, maintain:
              best_ready[f] = best total among plans whose last chosen day of family f
                              is either nonexistent or already older than k days
                              relative to the current day.
        - To make plans become "ready" after k days, we use a queue-like delayed activation:
          when we take day i with family f and value dp_take[i], this value should start
          contributing to best_ready[f] only for future days j where j - i > k,
          i.e. starting at day i + k + 1.
        - But note: a plan ending at day i is immediately valid as a prefix for all OTHER families.
          The family-specific restriction only matters when we try to take the same family again.

        We can solve this with:
        - global_best = best valid total so far
        - for each family f:
              blocked_best[f] = best total among plans whose most recent chosen day of family f
                                is within the last k days and thus cannot be followed by f yet
              ready_best[f] = best total among plans that are currently allowed to be followed by f
        - Delayed events move dp_take values from blocked to ready after k days.

        There is an even cleaner formulation:
        - Let best_for_family[f] be the best total of a valid plan whose last chosen day
          of family f is the most recent chosen day in that plan.
        - Let global_best be the best over all plans.
        - To take day i of family f, we need the best plan that does NOT use family f
          in the last k days. We can obtain this by maintaining, for each family,
          the best plan value available before the cooldown window.

        Final practical implementation:
        - Maintain dp_prefix[i] = best answer using days [0..i].
        - For each family f, maintain best_prev[f]:
              the best value of dp_prefix[t] where t is before the latest forbidden window
              for future occurrences of family f.
        - As we process day i, before using family[i], we update best_prev[family[i]]
          with dp_prefix[i - k - 1] if that index exists.
        - Also, because the family may have multiple occurrences, we keep taking the max.

        Then:
            take = gain[i] + best_prev[family[i]]
        where best_prev[f] starts at 0, representing taking the first occurrence of that family
        with no previous chosen days.

        This works because best_prev[f] represents the best total from any prefix that ends
        early enough to safely append family f now.

        Args:
            family: List of family identifiers for each day.
            gain: List of reliability gains for each day.
            k: Cooldown window for repeating the same family.

        Returns:
            Maximum total reliability gain.

        Time complexity:
            O(n)

        Space complexity:
            O(n + u), where u is the number of distinct families.
        """
        n: int = len(family)

        # dp_prefix[i] will store the best answer considering days 0..i.
        # This is the standard "prefix DP" array:
        # - either we skip day i and keep the previous best
        # - or we take day i and add its gain to the best safe prefix for its family
        dp_prefix: List[int] = [0] * n

        # best_prev[f] means:
        # "the best dp_prefix value from a day index that is old enough so that
        #  we may safely deploy family f today."
        #
        # Why is this enough?
        # Because if a prefix ends at day t <= i-k-1, then every chosen day in that prefix
        # is also <= i-k-1, so any previous deployment of family f is far enough away.
        #
        # Initial value 0 means:
        # "choose no previous days at all", which is always a valid prefix.
        best_prev: Dict[int, int] = {}

        for i in range(n):
            current_family: int = family[i]
            current_gain: int = gain[i]

            # Before computing transitions for day i, we need to make sure that
            # best_prev[current_family] includes any prefix that has just become old enough.
            #
            # The latest prefix endpoint that is guaranteed safe for reusing the same family
            # today is day i-k-1.
            #
            # Example:
            # - if k = 2 and today is i = 5
            # - then same family may have been used on day 2 or earlier, because 5 - 2 = 3 > 2
            # - therefore any valid plan entirely within prefix ending at day 2 is safe to extend
            #   with current_family on day 5
            safe_prefix_index: int = i - k - 1
            if safe_prefix_index >= 0:
                safe_value: int = dp_prefix[safe_prefix_index]
                previous_best: int = best_prev.get(current_family, 0)
                if safe_value > previous_best:
                    best_prev[current_family] = safe_value

            # Option 1: skip today.
            skip_value: int = dp_prefix[i - 1] if i > 0 else 0

            # Option 2: take today.
            #
            # We add today's gain to the best safe prefix for this family.
            # If this family has never had any safe prefix recorded yet, default is 0,
            # meaning we start a new plan with just today.
            take_value: int = best_prev.get(current_family, 0) + current_gain

            # The best answer up to today is the better of skipping or taking.
            dp_prefix[i] = max(skip_value, take_value)

        return dp_prefix[-1] if n > 0 else 0


if __name__ == "__main__":
    solution = Solution()

    family1: List[int] = [1, 2, 1, 3, 2, 1]
    gain1: List[int] = [5, 4, 7, 3, 6, 10]
    k1: int = 2
    result1: int = solution.maximum_reliability(family1, gain1, k1)
    print(result1)

    family2: List[int] = [4, 4, 4, 2, 2]
    gain2: List[int] = [8, 1, 9, 5, 7]
    k2: int = 1
    result2: int = solution.maximum_reliability(family2, gain2, k2)
    print(result2)