"""
Title: Count Pairs of Photos Within Brightness Budget

Problem Description:
You are given an integer array brightness where brightness[i] is the brightness score
of the i-th photo in a gallery, and an integer budget. Two photos can be edited together
if the absolute difference between their brightness scores is less than or equal to budget.

Your task is to return the number of distinct pairs of indices (i, j) such that
0 <= i < j < n and |brightness[i] - brightness[j]| <= budget.

A brute-force solution that checks every pair takes O(n^2) time and is too slow for
large galleries. You should design an algorithm efficient enough for up to 200,000 photos.
A common approach is to sort the brightness values and use two pointers to count how many
valid partners each position can form.

Note that pairs are based on indices, but after sorting, each original photo still
contributes exactly once to the count through its value. Duplicate brightness values are
allowed, and they may create many valid pairs.

Constraints:
- 1 <= brightness.length <= 200000
- 0 <= brightness[i] <= 10^9
- 0 <= budget <= 10^9

Example 1:
Input: brightness = [4, 1, 7, 5], budget = 3
Output: 4
Explanation: Valid pairs are (4,1), (4,7), (4,5), and (7,5). Their brightness
differences are 3, 3, 1, and 2.

Example 2:
Input: brightness = [2, 2, 2, 8, 9], budget = 1
Output: 4
Explanation: Among the three photos with brightness 2, there are 3 valid pairs.
Also, the pair (8,9) is valid. No pair involving a 2 and either 8 or 9 satisfies the budget.
"""

from typing import List


class Solution:
    def count_pairs_within_budget(self, brightness: List[int], budget: int) -> int:
        """
        Count the number of distinct index pairs whose brightness difference
        is less than or equal to the given budget.

        Args:
            brightness: List of photo brightness values.
            budget: Maximum allowed absolute difference for a valid pair.

        Returns:
            The total number of valid pairs.

        Time complexity:
            O(n log n), due to sorting the array once. The two-pointer scan is O(n).

        Space complexity:
            O(n) in Python because sorted(...) creates a new list.
        """
        # We sort the brightness values first.
        #
        # Why sorting helps:
        # - In an unsorted array, checking whether many pairs satisfy
        #   |brightness[i] - brightness[j]| <= budget is difficult to do efficiently.
        # - After sorting, for any fixed left position, all values to the right are
        #   greater than or equal to it.
        # - This means the absolute difference becomes:
        #       sorted_values[right] - sorted_values[left]
        #   because right-side values are never smaller than left-side values.
        # - That removes the need for abs(...) and creates a monotonic structure
        #   that two pointers can exploit efficiently.
        sorted_brightness: List[int] = sorted(brightness)

        # This variable will store the final count of valid pairs.
        total_pairs: int = 0

        # We use two pointers:
        # - left marks the beginning of the current valid window
        # - right expands the window to include as many valid partners as possible
        #
        # The key idea:
        # For each left index, we want to find the largest right boundary such that
        # every index in (left + 1) to (right - 1) forms a valid pair with left.
        #
        # We use a half-open window style:
        # - right is the first index that is NOT valid yet, or it may equal n
        # - therefore, valid partners for left are exactly:
        #       left + 1, left + 2, ..., right - 1
        #
        # Number of such partners:
        #       (right - 1) - (left + 1) + 1 = right - left - 1
        right: int = 0
        n: int = len(sorted_brightness)

        # We iterate left from 0 to n - 1.
        for left in range(n):
            # Important maintenance step:
            # Ensure right never falls behind left + 1.
            #
            # Why this is needed:
            # - Since left moves forward one step at a time, right should always be
            #   at least one position ahead if we want to count pairs (left, j) with j > left.
            # - If right <= left, then there are no candidate partners yet, so we move
            #   right to left + 1.
            if right < left + 1:
                right = left + 1

            # Expand right as far as possible while the pair (left, right) is valid.
            #
            # Because the array is sorted:
            # - If sorted_brightness[right] - sorted_brightness[left] <= budget,
            #   then this right index is a valid partner.
            # - We then try to include even more elements by moving right forward.
            #
            # Why this is efficient:
            # - right only moves forward, never backward.
            # - Across the entire algorithm, right advances at most n times.
            # - Therefore, the total scanning work is linear after sorting.
            while (
                right < n
                and sorted_brightness[right] - sorted_brightness[left] <= budget
            ):
                right += 1

            # At this point:
            # - right is either n, or
            # - right is the first index where the difference became too large
            #
            # So the valid partners for the current left are all indices:
            #   left + 1 through right - 1
            #
            # Count of valid partners:
            valid_partners: int = right - left - 1

            # Add them to the running total.
            total_pairs += valid_partners

        return total_pairs

    def countPairs(self, brightness: List[int], budget: int) -> int:
        """
        Wrapper method matching a compact interview-style naming convention.

        Args:
            brightness: List of photo brightness values.
            budget: Maximum allowed absolute difference for a valid pair.

        Returns:
            The total number of valid pairs.

        Time complexity:
            O(n log n)

        Space complexity:
            O(n)
        """
        return self.count_pairs_within_budget(brightness, budget)


if __name__ == "__main__":
    solution = Solution()

    # Example 1
    brightness1: List[int] = [4, 1, 7, 5]
    budget1: int = 3
    result1: int = solution.count_pairs_within_budget(brightness1, budget1)
    print("Example 1 Result:", result1)  # Expected: 4

    # Example 2
    brightness2: List[int] = [2, 2, 2, 8, 9]
    budget2: int = 1
    result2: int = solution.count_pairs_within_budget(brightness2, budget2)
    print("Example 2 Result:", result2)  # Expected: 4

    # Additional quick checks
    brightness3: List[int] = [1]
    budget3: int = 0
    result3: int = solution.count_pairs_within_budget(brightness3, budget3)
    print("Single Photo Result:", result3)  # Expected: 0

    brightness4: List[int] = [1, 3, 6, 10]
    budget4: int = 2
    result4: int = solution.count_pairs_within_budget(brightness4, budget4)
    print("Additional Check Result:", result4)  # Expected: 1