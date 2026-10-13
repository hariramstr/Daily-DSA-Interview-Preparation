"""
Title: Minimum Launch Day for Staggered Marketing Campaigns

Problem Description:
A company is preparing n marketing campaigns. The i-th campaign becomes ready on day
readyDay[i]. Once a campaign is ready, it stays ready forever. The company wants to
form exactly m launch groups, and each launch group must contain k consecutive campaigns
in the original lineup. A campaign can belong to at most one group.

Return the minimum day on which it is possible to form at least m such launch groups.
If it is impossible even after all campaigns are ready, return -1.

Consecutive means adjacent by index, not by ready day.

This problem is efficiently solved using binary search on the answer:
- For a candidate day D, check whether enough valid consecutive groups can be formed
  using only campaigns with readyDay[i] <= D.
- The feasibility condition is monotonic:
  if a day works, any later day also works.
"""

from typing import List


class Solution:
    def min_launch_day(self, readyDay: List[int], m: int, k: int) -> int:
        """
        Find the minimum day needed to form at least m non-overlapping groups,
        where each group contains k consecutive ready campaigns.

        Args:
            readyDay: List of days when each campaign becomes ready.
            m: Number of groups required.
            k: Number of consecutive campaigns required per group.

        Returns:
            The minimum feasible day, or -1 if it is impossible.

        Time Complexity:
            O(n log R), where n is the number of campaigns and
            R = max(readyDay) - min(readyDay) + 1

        Space Complexity:
            O(1), ignoring input storage
        """
        # ------------------------------------------------------------
        # Step 1: Quick impossibility check
        # ------------------------------------------------------------
        # If we need m groups and each group needs exactly k campaigns,
        # then in total we need at least m * k campaigns.
        #
        # Because campaigns cannot be reused across groups, if the total
        # number of campaigns is smaller than m * k, then it is impossible
        # no matter how many days we wait.
        #
        # We do this check first because it lets us return immediately
        # without doing any extra work.
        # ------------------------------------------------------------
        n: int = len(readyDay)
        if m * k > n:
            return -1

        # ------------------------------------------------------------
        # Step 2: Define the search range for binary search
        # ------------------------------------------------------------
        # The answer must be some day between:
        # - the earliest ready day in the array
        # - the latest ready day in the array
        #
        # Why?
        # - Before the minimum ready day, no campaign is ready.
        # - After the maximum ready day, all campaigns that will ever be
        #   ready are already ready.
        #
        # Since the feasibility of a day is monotonic, binary search is
        # the correct tool here.
        # ------------------------------------------------------------
        left: int = min(readyDay)
        right: int = max(readyDay)

        # ------------------------------------------------------------
        # Step 3: Binary search for the minimum feasible day
        # ------------------------------------------------------------
        # We repeatedly test the middle day:
        # - If it is possible to form at least m groups by that day,
        #   then we try to find an even smaller feasible day.
        # - Otherwise, we must wait longer, so we search the right half.
        #
        # At the end, left will point to the smallest feasible day.
        # ------------------------------------------------------------
        while left < right:
            mid: int = (left + right) // 2

            # If mid is feasible, keep searching on the left side
            # to find a smaller valid day.
            if self._can_form_groups(readyDay, m, k, mid):
                right = mid
            else:
                # If mid is not feasible, all smaller days are also not feasible,
                # so we move the left boundary to mid + 1.
                left = mid + 1

        # After binary search finishes, left == right and represents
        # the minimum feasible day.
        return left

    def _can_form_groups(self, readyDay: List[int], m: int, k: int, day: int) -> bool:
        """
        Check whether it is possible to form at least m non-overlapping groups
        of size k using only campaigns that are ready on or before the given day.

        Args:
            readyDay: List of days when each campaign becomes ready.
            m: Number of groups required.
            k: Number of consecutive campaigns required per group.
            day: Candidate day being tested.

        Returns:
            True if at least m groups can be formed by 'day', otherwise False.

        Time Complexity:
            O(n), where n is the number of campaigns

        Space Complexity:
            O(1)
        """
        # ------------------------------------------------------------
        # Core idea of the feasibility check
        # ------------------------------------------------------------
        # We scan the campaigns from left to right and count how many
        # consecutive campaigns are ready by the given day.
        #
        # If readyDay[i] <= day, then campaign i is usable.
        # Otherwise, it breaks the current consecutive streak.
        #
        # Every time the streak reaches length k, we can form one group.
        # Then we reset the streak to 0 because:
        # - groups cannot overlap
        # - the k campaigns we just used are consumed
        #
        # This greedy approach is correct because whenever we have k
        # consecutive ready campaigns, using them immediately cannot hurt.
        # Delaying their use would not create more groups than taking them now.
        # ------------------------------------------------------------
        groups_formed: int = 0
        consecutive_ready: int = 0

        for current_ready_day in readyDay:
            # --------------------------------------------------------
            # If this campaign is ready by the candidate day,
            # it extends the current consecutive usable segment.
            # --------------------------------------------------------
            if current_ready_day <= day:
                consecutive_ready += 1

                # ----------------------------------------------------
                # Once we collect exactly k consecutive ready campaigns,
                # we can form one complete group.
                #
                # We then:
                # - increase the number of groups formed
                # - reset consecutive_ready to 0 so that the next group
                #   does not overlap with the current one
                # ----------------------------------------------------
                if consecutive_ready == k:
                    groups_formed += 1
                    consecutive_ready = 0

                    # ------------------------------------------------
                    # Early exit optimization:
                    # As soon as we have formed at least m groups,
                    # there is no need to continue scanning.
                    # ------------------------------------------------
                    if groups_formed >= m:
                        return True
            else:
                # ----------------------------------------------------
                # This campaign is not ready by the candidate day.
                # Therefore, any consecutive segment is broken here.
                # We must reset the streak counter.
                # ----------------------------------------------------
                consecutive_ready = 0

        # If we finish scanning and still have fewer than m groups,
        # then this day is not feasible.
        return False


if __name__ == "__main__":
    solution = Solution()

    # Example 1
    # Note:
    # The mathematically correct answer for this input is 6.
    # By day 5, ready status is [True, True, True, False, True, False],
    # which allows only one non-overlapping consecutive pair: [0,1] or [1,2].
    # By day 6, ready status is [True, True, True, True, True, False],
    # which allows two non-overlapping consecutive pairs: [0,1] and [2,3].
    ready_day_1: List[int] = [5, 2, 4, 6, 3, 7]
    m_1: int = 2
    k_1: int = 2
    result_1: int = solution.min_launch_day(ready_day_1, m_1, k_1)
    print("Example 1 Output:", result_1)  # Expected: 6

    # Example 2
    # Since k = 1, each ready campaign can form its own group.
    # By day 3, campaigns at indexes 0, 2, and 4 are ready.
    ready_day_2: List[int] = [1, 10, 3, 10, 2]
    m_2: int = 3
    k_2: int = 1
    result_2: int = solution.min_launch_day(ready_day_2, m_2, k_2)
    print("Example 2 Output:", result_2)  # Expected: 3

    # Additional impossible case
    ready_day_3: List[int] = [1, 2, 3]
    m_3: int = 2
    k_3: int = 2
    result_3: int = solution.min_launch_day(ready_day_3, m_3, k_3)
    print("Impossible Case Output:", result_3)  # Expected: -1