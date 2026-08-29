"""
Title: Longest Notification Feed With Cooldowned App Repeats

Problem Description:
You are given an array apps of length n, where apps[i] is the app ID that generated
the i-th notification in a user's chronological feed, and an integer cooldown.

A contiguous segment of the feed is called valid if, for every app ID, any two
occurrences of that same app inside the segment are more than cooldown positions apart.
In other words, if apps[i] == apps[j] and both indices belong to the chosen segment,
then |i - j| must be greater than cooldown.

Your task is to return the length of the longest valid contiguous segment.

This models a notification system where repeated alerts from the same app must be
sufficiently spaced apart to avoid overwhelming the user. The segment must remain
contiguous; you are not allowed to reorder or delete notifications.

A segment of length 0 is allowed only implicitly, but the answer will always be at
least 1 when n > 0.

Constraints:
- 1 <= n <= 200000
- 1 <= apps[i] <= 1000000000
- 0 <= cooldown <= n

Examples:
1)
Input: apps = [4, 1, 2, 4, 3, 1, 5], cooldown = 2
Output: 5

2)
Input: apps = [7, 7, 8, 9, 7, 8, 10], cooldown = 3
Output: 4

Important note:
The validity rule says repeated equal values inside the chosen contiguous segment
must be strictly more than cooldown positions apart. So if the same app appears
again within distance <= cooldown, the segment is invalid.

A correct O(n) approach uses a sliding window:
- Expand the right end one notification at a time.
- Track the most recent index where each app appeared.
- If the current app appeared too recently (distance <= cooldown), move the left
  boundary just past that previous occurrence.
- Record the maximum window length seen.
"""

from typing import Dict, List


class Solution:
    def longest_valid_segment(self, apps: List[int], cooldown: int) -> int:
        """
        Return the length of the longest contiguous segment where equal app IDs
        are always more than `cooldown` positions apart.

        Args:
            apps: List of app IDs in chronological order.
            cooldown: Minimum forbidden closeness for repeated app IDs.
                      If two equal IDs are at distance <= cooldown, they cannot
                      both remain inside the same valid segment.

        Returns:
            The maximum length of a valid contiguous segment.

        Time Complexity:
            O(n), where n is the length of `apps`.
            Each index is processed once, and window movement is monotonic.

        Space Complexity:
            O(m), where m is the number of distinct app IDs stored in the map.
        """
        # Special case:
        # If the list is empty, the longest valid segment has length 0.
        # The original constraints say n >= 1, but handling empty input makes
        # the method more robust and complete.
        if not apps:
            return 0

        # Another easy special case:
        # If cooldown is 0, the rule becomes:
        # repeated equal values must be more than 0 apart.
        # Any two different positions already satisfy distance >= 1 > 0,
        # so every contiguous segment is valid.
        if cooldown == 0:
            return len(apps)

        # `last_seen` maps each app ID to the most recent index where it appeared.
        #
        # Why this is enough:
        # When we process apps[right], the only possible new violation created by
        # adding this element to the current window is with the previous occurrence
        # of the same app. Older occurrences are even farther left, so if the most
        # recent one is already far enough away or outside the window, older ones
        # cannot create a stricter problem.
        last_seen: Dict[int, int] = {}

        # `left` is the left boundary of our current sliding window.
        # The current candidate valid segment is always apps[left:right+1].
        left: int = 0

        # `best` stores the maximum valid window length found so far.
        best: int = 0

        # We expand the window by moving `right` from left to right across the array.
        for right, app_id in enumerate(apps):
            # If we have seen this app before, check whether that previous occurrence
            # is too close to the current one.
            if app_id in last_seen:
                previous_index: int = last_seen[app_id]

                # The distance between duplicate app notifications is:
                # right - previous_index
                #
                # The segment is invalid if this distance is <= cooldown
                # AND both indices are inside the current window.
                #
                # If previous_index >= left, then the previous occurrence is still
                # inside the current window. In that case, to restore validity,
                # we must move `left` to previous_index + 1, which removes that
                # conflicting earlier occurrence from the window.
                #
                # We use max(left, previous_index + 1) because:
                # - `left` should never move backward
                # - there may have been earlier conflicts that already pushed
                #   `left` farther right
                if previous_index >= left and right - previous_index <= cooldown:
                    left = previous_index + 1

            # Update the most recent position of this app to the current index.
            last_seen[app_id] = right

            # Now apps[left:right+1] is guaranteed to be valid.
            current_length: int = right - left + 1

            # Update the best answer if this valid window is the largest so far.
            if current_length > best:
                best = current_length

        return best

    def longestNotificationFeedWithCooldownedAppRepeats(
        self, apps: List[int], cooldown: int
    ) -> int:
        """
        Wrapper method matching the problem theme/title wording.

        Args:
            apps: List of app IDs in chronological order.
            cooldown: Required strict separation distance for repeated app IDs.

        Returns:
            Length of the longest valid contiguous segment.

        Time Complexity:
            O(n)

        Space Complexity:
            O(m)
        """
        return self.longest_valid_segment(apps, cooldown)


if __name__ == "__main__":
    solution = Solution()

    # Sample 1 from the prompt.
    apps1: List[int] = [4, 1, 2, 4, 3, 1, 5]
    cooldown1: int = 2
    result1: int = solution.longest_valid_segment(apps1, cooldown1)
    print("Sample 1:")
    print("apps =", apps1)
    print("cooldown =", cooldown1)
    print("Longest valid segment length =", result1)
    print()

    # Sample 2 from the prompt.
    # Careful verification:
    # [7, 7, 8, 9, 7, 8, 10], cooldown = 3
    #
    # A valid length-4 segment is [7, 8, 10] not contiguous, so not allowed.
    # But [7, 8, 9, 7] is invalid because the 7s are distance 3.
    # [9, 7, 8, 10] is contiguous and valid because all values are distinct.
    # Therefore the correct answer is 4.
    apps2: List[int] = [7, 7, 8, 9, 7, 8, 10]
    cooldown2: int = 3
    result2: int = solution.longest_valid_segment(apps2, cooldown2)
    print("Sample 2:")
    print("apps =", apps2)
    print("cooldown =", cooldown2)
    print("Longest valid segment length =", result2)
    print()

    # Additional quick checks for clarity.

    # cooldown = 0 => every segment is valid, so answer is full length.
    apps3: List[int] = [1, 1, 1, 1]
    cooldown3: int = 0
    result3: int = solution.longest_valid_segment(apps3, cooldown3)
    print("Additional Check 1:")
    print("apps =", apps3)
    print("cooldown =", cooldown3)
    print("Longest valid segment length =", result3)
    print()

    # If duplicates must be more than 1 apart, adjacent duplicates are forbidden.
    apps4: List[int] = [1, 2, 2, 3, 4]
    cooldown4: int = 1
    result4: int = solution.longest_valid_segment(apps4, cooldown4)
    print("Additional Check 2:")
    print("apps =", apps4)
    print("cooldown =", cooldown4)
    print("Longest valid segment length =", result4)