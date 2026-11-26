"""
Title: Locate Customer Tier by Spend Threshold

Problem Description:
A company stores its loyalty program tiers as a sorted array of minimum spend thresholds.
Each value represents the smallest total yearly spend needed to qualify for that tier.

Given a sorted integer array thresholds in strictly increasing order and an integer spend,
return the index of the highest threshold that is less than or equal to spend.
If spend is smaller than the first threshold, return -1.

The solution must run in O(log n) time, so binary search is the appropriate approach.

Example 1:
thresholds = [0, 100, 300, 700], spend = 450
Output: 2

Example 2:
thresholds = [50, 120, 200], spend = 20
Output: -1
"""

from typing import List


class Solution:
    def locate_customer_tier(self, thresholds: List[int], spend: int) -> int:
        """
        Find the index of the largest threshold that is less than or equal to spend.

        Args:
            thresholds: A sorted list of strictly increasing minimum spend thresholds.
            spend: The customer's total yearly spend.

        Returns:
            The index of the rightmost threshold value that is <= spend.
            Returns -1 if spend is smaller than the first threshold.

        Time Complexity:
            O(log n), because binary search cuts the search space roughly in half each step.

        Space Complexity:
            O(1), because only a constant amount of extra memory is used.
        """
        # We will use binary search, but not for an exact match.
        # Instead, we want the "rightmost valid index":
        # the largest index i such that thresholds[i] <= spend.
        #
        # Why binary search works:
        # - thresholds is sorted in strictly increasing order.
        # - If thresholds[mid] <= spend, then mid is a valid candidate,
        #   and maybe there is an even better answer to the RIGHT.
        # - If thresholds[mid] > spend, then mid is too large,
        #   and every value to the RIGHT is also too large,
        #   so we must move LEFT.

        # 'left' and 'right' define the current search range, inclusive.
        left: int = 0
        right: int = len(thresholds) - 1

        # This variable stores the best valid answer found so far.
        # We start with -1 because if no threshold is <= spend,
        # then the correct result should be -1.
        answer: int = -1

        # Continue searching while there is still a valid range to inspect.
        while left <= right:
            # Compute the middle index safely.
            # In Python, overflow is not an issue, but this form is standard
            # and commonly used in binary search implementations.
            mid: int = left + (right - left) // 2

            # Check the threshold at the middle position.
            if thresholds[mid] <= spend:
                # This threshold is valid because it does not exceed spend.
                # That means mid is a possible answer.
                answer = mid

                # But we are not done yet.
                # We need the HIGHEST index whose threshold is still <= spend.
                # So we move to the right half to see if there is a larger valid threshold.
                left = mid + 1
            else:
                # This threshold is too large.
                # Since the array is sorted, everything to the right is also too large.
                # Therefore, the answer must be in the left half.
                right = mid - 1

        # When the loop ends, 'answer' holds the rightmost valid index found,
        # or -1 if no threshold was small enough.
        return answer


if __name__ == "__main__":
    solution = Solution()

    # Example 1 from the problem statement:
    # thresholds[2] = 300 is the largest threshold <= 450.
    thresholds_1: List[int] = [0, 100, 300, 700]
    spend_1: int = 450
    result_1: int = solution.locate_customer_tier(thresholds_1, spend_1)
    print(f"thresholds = {thresholds_1}, spend = {spend_1} -> {result_1}")

    # Example 2 from the problem statement:
    # spend is below the first threshold, so the answer is -1.
    thresholds_2: List[int] = [50, 120, 200]
    spend_2: int = 20
    result_2: int = solution.locate_customer_tier(thresholds_2, spend_2)
    print(f"thresholds = {thresholds_2}, spend = {spend_2} -> {result_2}")

    # Additional beginner-friendly checks for important edge cases:

    # Exact match with a threshold.
    thresholds_3: List[int] = [0, 100, 300, 700]
    spend_3: int = 300
    result_3: int = solution.locate_customer_tier(thresholds_3, spend_3)
    print(f"thresholds = {thresholds_3}, spend = {spend_3} -> {result_3}")

    # Spend larger than every threshold.
    thresholds_4: List[int] = [0, 100, 300, 700]
    spend_4: int = 1000
    result_4: int = solution.locate_customer_tier(thresholds_4, spend_4)
    print(f"thresholds = {thresholds_4}, spend = {spend_4} -> {result_4}")

    # Spend equal to the first threshold.
    thresholds_5: List[int] = [50, 120, 200]
    spend_5: int = 50
    result_5: int = solution.locate_customer_tier(thresholds_5, spend_5)
    print(f"thresholds = {thresholds_5}, spend = {spend_5} -> {result_5}")