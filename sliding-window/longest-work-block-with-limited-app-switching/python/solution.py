"""
Title: Longest Work Block With Limited App Switching

Problem Description:
You are given an array apps where apps[i] is the name of the application a user was
focused on during the i-th minute of a work session. A contiguous block of minutes is
called efficient if the user switched between at most k distinct applications inside
that block. Your task is to return the length of the longest efficient block.

More formally, find the maximum length of a contiguous subarray of apps that contains
no more than k distinct strings.

This problem models productivity analysis in desktop telemetry systems, where frequent
switching across too many tools in a short period may indicate fragmented work. You
must process the session efficiently because the log can be large.

Return 0 if the input array is empty. You may assume k is non-negative. If k = 0, no
non-empty block is valid, so the answer is 0.

Constraints:
- 0 <= apps.length <= 200000
- 0 <= k <= apps.length
- 1 <= apps[i].length <= 20
- apps[i] consists of lowercase English letters, digits, underscores, or hyphens

Example 1:
Input: apps = ["mail","docs","mail","chat","docs","docs"], k = 2
Output: 3
Explanation: One longest valid block is ["docs","mail","chat"]? No, that has 3 distinct
apps, so it is invalid. The longest valid blocks include ["mail","docs","mail"] and
["chat","docs","docs"], both of length 3.

Example 2:
Input: apps = ["ide","ide","browser","terminal","browser","terminal","music"], k = 3
Output: 6
Explanation: The subarray ["ide","browser","terminal","browser","terminal","music"] has
4 distinct apps, so it is invalid. A longest valid block is
["ide","ide","browser","terminal","browser","terminal"], which contains exactly 3
distinct apps and has length 6.
"""

from typing import Dict, List


class Solution:
    def longest_efficient_block(self, apps: List[str], k: int) -> int:
        """
        Return the length of the longest contiguous subarray containing at most k distinct apps.

        Args:
            apps: A list of application names, where apps[i] is the app used at minute i.
            k: The maximum number of distinct application names allowed in a valid block.

        Returns:
            The maximum length of a contiguous block with at most k distinct strings.

        Time complexity:
            O(n), where n is the length of apps, because each pointer moves at most n times.

        Space complexity:
            O(k) in the typical sliding-window sense for the active window's frequency map,
            and O(min(n, number of distinct apps overall)) in the worst case due to the map.
        """
        # Handle edge cases first.
        # If the input list is empty, there is no block at all, so the answer must be 0.
        if not apps:
            return 0

        # If k is 0, then we are not allowed to have any distinct app in the window.
        # That means no non-empty subarray can ever be valid, so return 0 immediately.
        if k == 0:
            return 0

        # This dictionary stores how many times each app appears inside the current window.
        # Key   -> app name (string)
        # Value -> count of that app in the current window
        #
        # We use a frequency map because:
        # 1. We need to know how many distinct apps are currently in the window.
        # 2. When shrinking the window from the left, we must know when an app count
        #    drops to zero so we can remove it from the map and reduce the distinct count.
        counts: Dict[str, int] = {}

        # left marks the beginning of the current sliding window.
        left: int = 0

        # best stores the maximum valid window length found so far.
        best: int = 0

        # Expand the window by moving right from left to right across the array.
        # At each step, we include apps[right] into the current window.
        for right, app in enumerate(apps):
            # Add the current app to the frequency map.
            # If it is not already present, get(..., 0) starts its count at 0.
            counts[app] = counts.get(app, 0) + 1

            # After adding the new app, the window might now contain too many distinct apps.
            # If len(counts) > k, the window is invalid and must be shrunk from the left
            # until it becomes valid again.
            while len(counts) > k:
                # Identify the app that is leaving the window from the left side.
                left_app: str = apps[left]

                # Decrease its frequency because it is no longer inside the window.
                counts[left_app] -= 1

                # If its count becomes zero, that app is no longer present in the window.
                # We remove it completely from the dictionary so len(counts) correctly
                # reflects the number of distinct apps currently inside the window.
                if counts[left_app] == 0:
                    del counts[left_app]

                # Move the left boundary one step to the right, shrinking the window.
                left += 1

            # At this point, the window apps[left:right+1] is guaranteed to be valid:
            # it contains at most k distinct apps.
            #
            # Compute its length. Since both ends are inclusive, length is:
            # right - left + 1
            current_length: int = right - left + 1

            # Update the best answer if this valid window is longer than any previous one.
            if current_length > best:
                best = current_length

        # After scanning the entire array, best holds the length of the longest valid block.
        return best


if __name__ == "__main__":
    solution = Solution()

    # Example 1
    apps1: List[str] = ["mail", "docs", "mail", "chat", "docs", "docs"]
    k1: int = 2
    result1: int = solution.longest_efficient_block(apps1, k1)
    print("Example 1 result:", result1)  # Expected: 3

    # Example 2
    apps2: List[str] = ["ide", "ide", "browser", "terminal", "browser", "terminal", "music"]
    k2: int = 3
    result2: int = solution.longest_efficient_block(apps2, k2)
    print("Example 2 result:", result2)  # Expected: 6

    # Additional edge cases for clarity
    apps3: List[str] = []
    k3: int = 2
    result3: int = solution.longest_efficient_block(apps3, k3)
    print("Empty input result:", result3)  # Expected: 0

    apps4: List[str] = ["mail", "docs", "chat"]
    k4: int = 0
    result4: int = solution.longest_efficient_block(apps4, k4)
    print("k = 0 result:", result4)  # Expected: 0

    apps5: List[str] = ["editor", "editor", "editor"]
    k5: int = 1
    result5: int = solution.longest_efficient_block(apps5, k5)
    print("Single distinct app result:", result5)  # Expected: 3