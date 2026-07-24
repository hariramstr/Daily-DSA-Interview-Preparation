"""
Title: Shortest Market Span Covering All Ad Campaigns

Problem Description:
You are given a chronological stream of website visits represented by an array visits,
where visits[i] is the campaign ID that influenced the i-th visit. You are also given
an integer array required of length m, where required[c] indicates how many visits
influenced by campaign c must appear inside a valid analytics window. Campaign IDs in
visits are in the range [0, m - 1].

Your task is to find the length of the shortest contiguous subarray of visits that
satisfies all campaign requirements simultaneously. In other words, for every campaign c,
the chosen window must contain campaign c at least required[c] times. If no such window
exists, return -1.

This is harder than a standard minimum-cover problem because some campaigns may require
multiple occurrences, some may require zero occurrences, and the input size is large
enough that brute force enumeration of all subarrays will time out. An efficient sliding
window solution is expected.

Return the minimum possible window length.

Constraints:
- 1 <= visits.length <= 200000
- 1 <= m <= 100000
- 0 <= visits[i] < m
- 0 <= required[c] <= visits.length
- The sum of required values can be larger than visits.length

Example 1:
Input: visits = [2,0,1,2,0,1,2,1], required = [1,2,2]
Output: 5

Example 2:
Input: visits = [3,1,3,2,1,0,2,3], required = [1,1,2,1]
Output: 6

Core idea:
Use a sliding window with two pointers. We expand the right pointer until the current
window satisfies every required campaign count. Then we shrink from the left as much as
possible while keeping the window valid. This guarantees we examine each element only a
constant number of times, leading to an efficient O(n + m) style solution.
"""

from typing import List


class Solution:
    def _can_satisfy_globally(self, visits: List[int], required: List[int]) -> bool:
        """
        Check whether the entire visits array contains enough occurrences of each campaign.

        This is an optional but useful early-exit optimization:
        if the full array does not satisfy the requirements, then no subarray can satisfy
        them either.

        Args:
            visits: Chronological list of campaign IDs.
            required: Required minimum count for each campaign ID.

        Returns:
            True if the full array contains enough of every required campaign, else False.

        Time complexity:
            O(n + m), where n is len(visits) and m is len(required).

        Space complexity:
            O(m) for the frequency array.
        """
        total_counts: List[int] = [0] * len(required)

        # Count how many times each campaign appears in the entire visits array.
        for campaign_id in visits:
            total_counts[campaign_id] += 1

        # If any campaign appears fewer times than required, the task is impossible.
        for campaign_id, need in enumerate(required):
            if total_counts[campaign_id] < need:
                return False

        return True

    def shortest_market_span(self, visits: List[int], required: List[int]) -> int:
        """
        Find the length of the shortest contiguous subarray that satisfies all campaign requirements.

        We maintain a sliding window [left, right].
        - Expand right to include more visits.
        - Track counts of campaigns inside the current window.
        - Once the window satisfies all required counts, try shrinking from the left
          to make it as short as possible while still valid.
        - Record the minimum valid window length seen.

        Important implementation detail:
        We do not repeatedly check all campaigns to see whether the window is valid.
        That would be too slow for large inputs. Instead, we track how many campaign
        types are currently "satisfied" (meaning window_count[c] >= required[c]).

        Campaigns with required[c] == 0 are already satisfied before the window starts,
        because they do not impose any constraint.

        Args:
            visits: Chronological list of campaign IDs.
            required: required[c] is the minimum number of times campaign c must appear.

        Returns:
            The minimum valid window length, or -1 if no valid window exists.

        Time complexity:
            O(n + m), where n is len(visits) and m is len(required).
            Each pointer moves from left to right at most once.

        Space complexity:
            O(m) for the current window frequency array.
        """
        n: int = len(visits)
        m: int = len(required)

        # Early impossibility check:
        # If the entire array cannot satisfy the requirements, return -1 immediately.
        if not self._can_satisfy_globally(visits, required):
            return -1

        # Count how many campaign IDs must be satisfied in total.
        # Every campaign with required[c] == 0 is considered satisfied from the start,
        # because the window does not need to contain that campaign at all.
        total_campaigns_to_satisfy: int = m
        satisfied_campaigns: int = 0

        for need in required:
            if need == 0:
                satisfied_campaigns += 1

        # If all requirements are zero, then technically any empty window would satisfy them.
        # However, the problem asks for a contiguous subarray of visits. In most interview and
        # competitive programming settings, subarrays are non-empty. Since visits length >= 1,
        # the shortest non-empty subarray length is 1.
        if satisfied_campaigns == total_campaigns_to_satisfy:
            return 1

        # window_counts[c] stores how many times campaign c appears in the current window.
        window_counts: List[int] = [0] * m

        left: int = 0
        best_length: int = n + 1  # Sentinel value larger than any possible valid answer.

        # Expand the window by moving right from 0 to n - 1.
        for right in range(n):
            campaign_at_right: int = visits[right]

            # Include visits[right] in the current window.
            window_counts[campaign_at_right] += 1

            # If this addition makes campaign_at_right reach its required count exactly,
            # then one more campaign requirement has become satisfied.
            #
            # We use "==" rather than ">=" because we only want to count the transition
            # from unsatisfied to satisfied once.
            if required[campaign_at_right] > 0 and window_counts[campaign_at_right] == required[campaign_at_right]:
                satisfied_campaigns += 1

            # While the current window satisfies all campaign requirements,
            # try shrinking it from the left to find the smallest valid window
            # ending at the current right index.
            while satisfied_campaigns == total_campaigns_to_satisfy:
                current_length: int = right - left + 1
                if current_length < best_length:
                    best_length = current_length

                campaign_at_left: int = visits[left]

                # We are about to remove visits[left] from the window.
                # If removing it causes a previously satisfied campaign to fall below
                # its required count, then the window will stop being valid.
                if required[campaign_at_left] > 0 and window_counts[campaign_at_left] == required[campaign_at_left]:
                    satisfied_campaigns -= 1

                window_counts[campaign_at_left] -= 1
                left += 1

        return best_length if best_length <= n else -1


if __name__ == "__main__":
    solution = Solution()

    # Example 1
    visits_1: List[int] = [2, 0, 1, 2, 0, 1, 2, 1]
    required_1: List[int] = [1, 2, 2]
    result_1: int = solution.shortest_market_span(visits_1, required_1)
    print("Example 1 Result:", result_1)  # Expected: 5

    # Example 2
    visits_2: List[int] = [3, 1, 3, 2, 1, 0, 2, 3]
    required_2: List[int] = [1, 1, 2, 1]
    result_2: int = solution.shortest_market_span(visits_2, required_2)
    print("Example 2 Result:", result_2)  # Expected: 6

    # Additional sanity checks

    # Impossible case: campaign 1 is required twice, but appears only once.
    visits_3: List[int] = [0, 1, 0]
    required_3: List[int] = [1, 2]
    result_3: int = solution.shortest_market_span(visits_3, required_3)
    print("Impossible Case Result:", result_3)  # Expected: -1

    # Case with zero requirements for some campaigns.
    visits_4: List[int] = [1, 2, 1, 0, 2]
    required_4: List[int] = [1, 0, 1]
    result_4: int = solution.shortest_market_span(visits_4, required_4)
    print("Zero Requirement Case Result:", result_4)  # Expected: 2

    # All zero requirements: shortest non-empty subarray is length 1.
    visits_5: List[int] = [4, 2, 3]
    required_5: List[int] = [0, 0, 0, 0, 0]
    result_5: int = solution.shortest_market_span(visits_5, required_5)
    print("All Zero Requirements Result:", result_5)  # Expected: 1