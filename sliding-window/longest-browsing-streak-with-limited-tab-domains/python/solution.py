"""
Title: Longest Browsing Streak With Limited Tab Domains

Problem Description:
You are given an array `domains` where `domains[i]` is the website domain opened in the
browser at minute `i`. A user wants to study their browsing habits and find the longest
contiguous time interval during which they were focused on only a small set of websites.

Define a browsing streak as any contiguous subarray of `domains`. Given an integer `k`,
return the length of the longest browsing streak that contains visits to at most `k`
distinct domains.

For example, if the streak is `["docs.com", "mail.com", "docs.com"]` and `k = 2`, the
streak is valid because it contains only 2 distinct domains. However,
`["docs.com", "mail.com", "video.com"]` is invalid when `k = 2` because it contains
3 distinct domains.

Your task is to compute the maximum possible length of a valid streak.

Constraints:
- 1 <= domains.length <= 200000
- 1 <= domains[i].length <= 30
- domains[i] consists of lowercase English letters, digits, dots, and hyphens
- 1 <= k <= domains.length
"""

from typing import Dict, List


class Solution:
    def longest_browsing_streak(self, domains: List[str], k: int) -> int:
        """
        Find the length of the longest contiguous subarray containing at most k distinct domains.

        Args:
            domains: A list of domain strings representing the domain opened at each minute.
            k: The maximum number of distinct domains allowed in a valid browsing streak.

        Returns:
            The maximum length of a contiguous browsing streak with at most k distinct domains.

        Time complexity:
            O(n), where n is the length of domains.
            Each domain is added to the window once and removed from the window at most once.

        Space complexity:
            O(k) in the typical sliding-window sense, or more precisely O(min(n, number of distinct domains)),
            due to the frequency dictionary storing counts of domains currently in the window.
        """
        # This dictionary stores how many times each domain appears inside the current window.
        # Key   -> domain name
        # Value -> count of that domain in the current window [left, right]
        #
        # We use a dictionary because:
        # 1. We need fast updates when expanding or shrinking the window.
        # 2. We need to know when a domain count becomes zero so we can remove it.
        # 3. The number of distinct domains in the current window is simply len(counts).
        counts: Dict[str, int] = {}

        # `left` is the starting index of our sliding window.
        # The window will always represent domains[left:right+1].
        left: int = 0

        # This variable stores the best (maximum) valid window length found so far.
        best_length: int = 0

        # We move `right` from left to right across the array, expanding the window one step at a time.
        for right, domain in enumerate(domains):
            # Step 1: Include the new domain at index `right` into the current window.
            # If the domain is not already in the dictionary, start its count at 0 first.
            counts[domain] = counts.get(domain, 0) + 1

            # Step 2: If the window has become invalid (more than k distinct domains),
            # we must shrink it from the left until it becomes valid again.
            #
            # Why a while loop instead of an if?
            # Because removing just one element may still leave more than k distinct domains.
            # We keep shrinking until the condition is satisfied.
            while len(counts) > k:
                # Identify the domain that is leaving the window from the left side.
                left_domain: str = domains[left]

                # Decrease its frequency because it is no longer inside the window.
                counts[left_domain] -= 1

                # If its count becomes zero, that means this domain no longer exists
                # anywhere in the current window, so we remove it from the dictionary.
                #
                # This is important because len(counts) is how we track the number of
                # distinct domains currently in the window.
                if counts[left_domain] == 0:
                    del counts[left_domain]

                # Move the left boundary one step to the right.
                left += 1

            # Step 3: At this point, the window is guaranteed to be valid:
            # it contains at most k distinct domains.
            #
            # So we compute its length and update the best answer if needed.
            current_length: int = right - left + 1
            if current_length > best_length:
                best_length = current_length

        # After scanning the entire array, best_length holds the answer.
        return best_length


if __name__ == "__main__":
    solution = Solution()

    # Example 1
    domains1: List[str] = [
        "docs.com",
        "mail.com",
        "docs.com",
        "video.com",
        "mail.com",
        "mail.com",
    ]
    k1: int = 2
    result1: int = solution.longest_browsing_streak(domains1, k1)
    print("Example 1 result:", result1)  # Expected: 3

    # Example 2
    # The corrected interpretation gives answer 4.
    domains2: List[str] = [
        "news.com",
        "news.com",
        "shop.com",
        "music.com",
        "shop.com",
        "shop.com",
        "news.com",
    ]
    k2: int = 2
    result2: int = solution.longest_browsing_streak(domains2, k2)
    print("Example 2 result:", result2)  # Expected: 4

    # Additional quick checks
    domains3: List[str] = ["a.com"]
    k3: int = 1
    result3: int = solution.longest_browsing_streak(domains3, k3)
    print("Additional test 1 result:", result3)  # Expected: 1

    domains4: List[str] = ["a.com", "b.com", "a.com", "a.com", "c.com"]
    k4: int = 2
    result4: int = solution.longest_browsing_streak(domains4, k4)
    print("Additional test 2 result:", result4)  # Expected: 4