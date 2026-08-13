"""
Title: Shortest Browser Session Covering Required Domains

Problem Description:
A security team analyzes a user's browsing history as an array visits, where visits[i]
is the domain opened at minute i. For an investigation, the team is given a requirement
map need describing how many times each important domain must appear inside a single
contiguous session. Your task is to find the length of the shortest contiguous subarray
of visits that satisfies all domain requirements.

A window is valid if for every domain d in need, the window contains at least need[d]
occurrences of d. Domains not listed in need may appear any number of times and do not
affect validity. If no valid session exists, return -1.

This is not just a basic coverage problem: the input size is large, domain names may
repeat heavily, and the solution must scale close to linear time. An O(n^2) solution
will time out.

Return the minimum possible length of a valid contiguous session.
"""

from typing import Dict, List


class Solution:
    def min_session_length(self, visits: List[str], need: Dict[str, int]) -> int:
        """
        Find the length of the shortest contiguous subarray that satisfies all domain requirements.

        Args:
            visits: List of visited domains in chronological order.
            need: Mapping from required domain to the minimum count needed inside one window.

        Returns:
            The minimum valid window length, or -1 if no valid window exists.

        Time complexity:
            O(n + m), where n is len(visits) and m is len(need), because each pointer
            moves across the array at most once and dictionary operations are average O(1).

        Space complexity:
            O(m), for storing counts of only the required domains.
        """
        # If there are no requirements, the shortest valid window would conceptually be 0.
        # The problem constraints say need.size >= 1, but this guard makes the method robust.
        if not need:
            return 0

        # ------------------------------------------------------------
        # Step 1: Quick impossibility check using total frequencies.
        # ------------------------------------------------------------
        # Why do this?
        # If some required domain does not appear enough times in the entire visits array,
        # then no contiguous subarray can ever satisfy the requirement.
        #
        # This early check can save work and also makes the logic easier to reason about.
        total_counts: Dict[str, int] = {}
        for domain in visits:
            if domain in need:
                total_counts[domain] = total_counts.get(domain, 0) + 1

        for domain, required_count in need.items():
            if total_counts.get(domain, 0) < required_count:
                return -1

        # ------------------------------------------------------------
        # Step 2: Sliding window setup.
        # ------------------------------------------------------------
        # We maintain a window [left, right].
        #
        # window_counts stores how many times each required domain appears in the current window.
        # We only track domains that matter (those in need), because all other domains do not
        # affect whether the window is valid.
        window_counts: Dict[str, int] = {}

        # formed tells us how many distinct required domains currently meet their required count.
        # Example:
        # need = {"news.com": 2, "mail.com": 1}
        # If window has {"news.com": 2, "mail.com": 0}, then formed = 1.
        formed = 0

        # required_distinct is the number of different domains that must be satisfied.
        required_distinct = len(need)

        # left is the start of the sliding window.
        left = 0

        # best_length stores the shortest valid window found so far.
        # Start with infinity so any real valid window will be smaller.
        best_length = float("inf")

        # ------------------------------------------------------------
        # Step 3: Expand the window by moving right.
        # ------------------------------------------------------------
        # For each right index, we include visits[right] into the current window.
        for right, domain in enumerate(visits):
            # Only required domains matter for counting.
            if domain in need:
                # Add this domain to the current window count.
                window_counts[domain] = window_counts.get(domain, 0) + 1

                # If this addition makes the count exactly reach the required amount,
                # then one more required domain is now satisfied.
                #
                # We use "==" and not ">=" because formed should increase only once
                # when crossing the threshold from "not enough" to "enough".
                if window_counts[domain] == need[domain]:
                    formed += 1

            # --------------------------------------------------------
            # Step 4: While the current window is valid, shrink it.
            # --------------------------------------------------------
            # A window is valid when every required domain is satisfied,
            # which means formed == required_distinct.
            #
            # Once valid, we try to move left forward as much as possible
            # without breaking validity, because that is how we find the
            # shortest valid window ending at the current right index.
            while formed == required_distinct:
                # Current window length is right - left + 1.
                current_length = right - left + 1
                if current_length < best_length:
                    best_length = current_length

                # We are about to remove visits[left] from the window.
                left_domain = visits[left]

                if left_domain in need:
                    # If the current count is exactly the required count,
                    # then removing one occurrence will make this domain no longer satisfied.
                    # Therefore formed must decrease.
                    if window_counts[left_domain] == need[left_domain]:
                        formed -= 1

                    # Actually remove the leftmost domain from the window count.
                    window_counts[left_domain] -= 1

                # Move left boundary forward to shrink the window.
                left += 1

        # If best_length was never updated, no valid window existed.
        return -1 if best_length == float("inf") else int(best_length)


if __name__ == "__main__":
    solution = Solution()

    # Example 1
    visits1 = [
        "news.com",
        "mail.com",
        "shop.com",
        "news.com",
        "video.com",
        "mail.com",
        "news.com",
    ]
    need1 = {"news.com": 2, "mail.com": 1}
    result1 = solution.min_session_length(visits1, need1)
    print(result1)  # Expected: 4

    # Example 2
    visits2 = ["a.com", "b.com", "a.com", "c.com", "b.com"]
    need2 = {"a.com": 2, "b.com": 2, "d.com": 1}
    result2 = solution.min_session_length(visits2, need2)
    print(result2)  # Expected: -1

    # Additional quick checks
    visits3 = ["x.com", "y.com", "x.com", "z.com", "y.com", "x.com"]
    need3 = {"x.com": 2, "y.com": 1}
    result3 = solution.min_session_length(visits3, need3)
    print(result3)  # One valid shortest window is ["y.com", "x.com", "z.com", "y.com", "x.com"] -> 5, but shorter exists ["x.com","z.com","y.com","x.com"] -> 4

    visits4 = ["only.com"]
    need4 = {"only.com": 1}
    result4 = solution.min_session_length(visits4, need4)
    print(result4)  # Expected: 1