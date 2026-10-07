"""
Title: Find the Earliest Stable Brightness Window

Problem Description:
You are given an array `brightness` where `brightness[i]` is the measured screen
brightness on minute `i`, and an integer `k`. A monitoring system wants to find
the earliest contiguous window of exactly `k` minutes where the brightness never
changes too sharply.

A window of length `k` is called stable if the difference between the maximum and
minimum values inside that window is less than or equal to `limit`.

Return the starting index of the earliest stable window of length `k`. If no such
window exists, return `-1`.

This problem models a simple quality check over time-series array data. Since the
difficulty is easy, you may assume the constraints are small enough for a
straightforward solution, although cleaner solutions are encouraged.

Constraints:
- 1 <= brightness.length <= 200
- 1 <= brightness[i] <= 10^4
- 1 <= k <= brightness.length
- 0 <= limit <= 10^4

Example 1:
Input: brightness = [7, 9, 8, 8, 10, 13], k = 3, limit = 2
Output: 0
Explanation:
The window [7, 9, 8] has max = 9 and min = 7, so the difference is 2, which is
allowed. Since this is the earliest valid window, return 0.

Example 2:
Input: brightness = [4, 10, 3, 12, 8], k = 2, limit = 1
Output: -1
Explanation:
Every window of length 2 has a max-min difference greater than 1, so there is no
stable window.

Task:
Scan the array and determine the first starting position that satisfies the
stability rule for a window of exactly `k` consecutive elements.
"""

from typing import List


class Solution:
    def earliest_stable_window(self, brightness: List[int], k: int, limit: int) -> int:
        """
        Find the earliest starting index of a window of length k whose
        maximum value minus minimum value is at most limit.

        Args:
            brightness: List of measured brightness values.
            k: Exact size of the contiguous window to check.
            limit: Maximum allowed difference between max and min in a window.

        Returns:
            The starting index of the earliest stable window, or -1 if none exists.

        Time complexity:
            O((n - k + 1) * k), where n is the length of brightness.
            For each possible window, we compute min and max over up to k elements.

        Space complexity:
            O(1) extra space, ignoring the input.
        """
        # We will use a simple brute-force sliding window approach.
        #
        # Why this approach?
        # - The constraints are small: brightness.length <= 200.
        # - That means checking every window directly is completely acceptable.
        # - This keeps the code easy to understand for beginners.
        #
        # Main idea:
        # 1. Try every possible starting index for a window of length k.
        # 2. For each window, compute:
        #       - the minimum value inside the window
        #       - the maximum value inside the window
        # 3. If max - min <= limit, then this window is stable.
        # 4. Because we scan from left to right, the first valid one is the earliest.
        # 5. If we finish scanning and find none, return -1.

        n: int = len(brightness)

        # The last valid starting index for a window of size k is n - k.
        # Example:
        #   n = 6, k = 3
        #   valid starts are 0, 1, 2, 3
        # So we iterate from 0 through n - k inclusive.
        for start in range(n - k + 1):
            # For the current window, we need to inspect exactly k elements:
            # brightness[start], brightness[start + 1], ..., brightness[start + k - 1]
            #
            # We initialize both current_min and current_max using the first element
            # of the window. This is a common pattern because:
            # - it avoids using artificial large/small sentinel values
            # - it works naturally even when k == 1
            current_min: int = brightness[start]
            current_max: int = brightness[start]

            # Now scan the rest of the elements in this window.
            # We start from start + 1 because start itself was already used
            # to initialize current_min and current_max.
            for i in range(start + 1, start + k):
                value: int = brightness[i]

                # Update the running minimum if we found a smaller value.
                if value < current_min:
                    current_min = value

                # Update the running maximum if we found a larger value.
                if value > current_max:
                    current_max = value

            # After scanning the full window, we know its min and max.
            # The stability rule says:
            #   max - min <= limit
            if current_max - current_min <= limit:
                # Because we are scanning windows from left to right,
                # this is automatically the earliest valid window.
                return start

        # If no window satisfied the condition, return -1.
        return -1


if __name__ == "__main__":
    # Create an instance of the solution class.
    solution = Solution()

    # Example 1 from the problem statement.
    brightness1: List[int] = [7, 9, 8, 8, 10, 13]
    k1: int = 3
    limit1: int = 2
    result1: int = solution.earliest_stable_window(brightness1, k1, limit1)
    print("Example 1 Result:", result1)  # Expected: 0

    # Example 2 from the problem statement.
    brightness2: List[int] = [4, 10, 3, 12, 8]
    k2: int = 2
    limit2: int = 1
    result2: int = solution.earliest_stable_window(brightness2, k2, limit2)
    print("Example 2 Result:", result2)  # Expected: -1

    # Additional beginner-friendly test cases.

    # A case where k = 1.
    # Any single-element window has max - min = 0, so it is always stable
    # as long as limit >= 0, which is guaranteed by constraints.
    brightness3: List[int] = [5, 100, 20]
    k3: int = 1
    limit3: int = 0
    result3: int = solution.earliest_stable_window(brightness3, k3, limit3)
    print("Additional Test 1 Result:", result3)  # Expected: 0

    # A case where the earliest window is not valid, but a later one is.
    brightness4: List[int] = [1, 10, 11, 12]
    k4: int = 2
    limit4: int = 1
    result4: int = solution.earliest_stable_window(brightness4, k4, limit4)
    print("Additional Test 2 Result:", result4)  # Expected: 1