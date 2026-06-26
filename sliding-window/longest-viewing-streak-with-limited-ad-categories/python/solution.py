"""
Title: Longest Viewing Streak With Limited Ad Categories

Problem Description:
A video platform records the category of the ad shown before each video in a user's
session. You are given an array ads where ads[i] is the category ID of the ad shown
at minute i, and an integer k. Your task is to find the length of the longest
contiguous viewing streak such that the number of distinct ad categories appearing
in that streak is at most k.

A viewing streak is any contiguous subarray of ads. Distinct categories are counted
by category ID, so repeated appearances of the same category only count once toward
the limit. Return the maximum possible length of such a streak.

This problem models a real analytics scenario where the platform wants to identify
the longest period of time during which ad variety stayed within an acceptable
threshold.

Constraints:
- 1 <= ads.length <= 200000
- 1 <= ads[i] <= 1000000000
- 1 <= k <= ads.length

Example 1:
Input: ads = [4, 2, 2, 5, 5, 2, 4], k = 2
Output: 5
Explanation: The longest valid streak is [2, 2, 5, 5, 2], which contains only
2 distinct categories: 2 and 5.

Example 2:
Input: ads = [1, 3, 1, 3, 2, 2, 4, 2], k = 3
Output: 6
Explanation: The subarray [1, 3, 1, 3, 2, 2, 4] is invalid because it contains
4 distinct categories: 1, 3, 2, and 4. Valid longest streaks include
[1, 3, 1, 3, 2, 2] and [3, 1, 3, 2, 2, 4], each of length 6.
"""

from typing import Dict, List


class Solution:
    def longest_viewing_streak(self, ads: List[int], k: int) -> int:
        """
        Find the length of the longest contiguous subarray containing at most k
        distinct ad categories.

        Args:
            ads: A list of integers where each value represents an ad category ID.
            k: The maximum number of distinct categories allowed in the streak.

        Returns:
            The maximum length of a contiguous subarray with at most k distinct values.

        Time Complexity:
            O(n), where n is the length of ads, because each element is added to and
            removed from the sliding window at most once.

        Space Complexity:
            O(k) on average for the frequency map of categories currently inside the
            window. In the worst case, this is O(n) if k can be as large as n.
        """
        # This dictionary will store how many times each ad category appears
        # inside the current sliding window.
        #
        # Example:
        # If the current window is [2, 2, 5, 5, 2], then:
        # counts = {2: 3, 5: 2}
        #
        # We use a dictionary because:
        # - We need fast updates when expanding or shrinking the window.
        # - We need to know when a category count drops to zero so that category
        #   is no longer considered part of the current window.
        counts: Dict[int, int] = {}

        # left marks the beginning of the current window.
        # right will move from left to right through the array.
        left: int = 0

        # best stores the maximum valid window length found so far.
        best: int = 0

        # We expand the window one element at a time by moving right.
        for right, category in enumerate(ads):
            # Add the current category into the window.
            # If it is not already present, start its count at 0 first.
            counts[category] = counts.get(category, 0) + 1

            # At this point, the window is ads[left:right+1].
            # It may now contain too many distinct categories.
            #
            # If the number of distinct keys in counts is greater than k,
            # the window is invalid and must be shrunk from the left side
            # until it becomes valid again.
            while len(counts) > k:
                # Identify the category that is leaving the window.
                left_category: int = ads[left]

                # Decrease its frequency because we are moving left forward.
                counts[left_category] -= 1

                # If its count becomes zero, that means this category no longer
                # exists anywhere in the current window, so we remove it from
                # the dictionary entirely.
                #
                # This is very important because the number of distinct categories
                # is represented by the number of keys in the dictionary.
                if counts[left_category] == 0:
                    del counts[left_category]

                # Move the left boundary to the right, effectively shrinking
                # the window by one element.
                left += 1

            # Now the window is guaranteed to be valid:
            # it contains at most k distinct categories.
            #
            # Compute its length and update the best answer if this window
            # is longer than any valid window seen before.
            current_length: int = right - left + 1
            if current_length > best:
                best = current_length

        # After processing all positions, best contains the answer.
        return best


if __name__ == "__main__":
    solution = Solution()

    # Example 1
    ads1: List[int] = [4, 2, 2, 5, 5, 2, 4]
    k1: int = 2
    result1: int = solution.longest_viewing_streak(ads1, k1)
    print("Example 1 Result:", result1)  # Expected: 5

    # Example 2
    ads2: List[int] = [1, 3, 1, 3, 2, 2, 4, 2]
    k2: int = 3
    result2: int = solution.longest_viewing_streak(ads2, k2)
    print("Example 2 Result:", result2)  # Expected: 6

    # Additional quick checks
    ads3: List[int] = [7]
    k3: int = 1
    result3: int = solution.longest_viewing_streak(ads3, k3)
    print("Additional Check 1 Result:", result3)  # Expected: 1

    ads4: List[int] = [1, 2, 3, 4, 5]
    k4: int = 2
    result4: int = solution.longest_viewing_streak(ads4, k4)
    print("Additional Check 2 Result:", result4)  # Expected: 2