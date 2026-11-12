"""
Title: Find the Last Discount Tier Not Exceeding Budget

Problem Description:
An online store offers bulk discounts based on purchase quantity. You are given a
sorted integer array `tiers`, where `tiers[i]` represents a quantity threshold at
which a new discount tier begins. The array is sorted in strictly increasing order.
You are also given an integer `budgetQty`, representing the maximum number of items
a customer is willing or able to buy.

Your task is to return the index of the last discount tier whose threshold is less
than or equal to `budgetQty`. In other words, find the greatest index `i` such that
`tiers[i] <= budgetQty`.

If every threshold in `tiers` is greater than `budgetQty`, return `-1` because the
customer does not qualify for any tier. If `budgetQty` is greater than or equal to
the largest threshold, return the last index.

This problem should be solved efficiently using binary search rather than scanning
the entire array. A linear scan works for small inputs, but interviewers expect an
`O(log n)` solution because the thresholds are already sorted.

Constraints:
- `1 <= tiers.length <= 100000`
- `1 <= tiers[i] <= 1000000000`
- `tiers` is strictly increasing
- `1 <= budgetQty <= 1000000000`

Example 1:
Input: tiers = [5, 10, 20, 50], budgetQty = 23
Output: 2
Explanation: The thresholds less than or equal to 23 are 5, 10, and 20. The last
such threshold is 20 at index 2.

Example 2:
Input: tiers = [4, 8, 15], budgetQty = 2
Output: -1
Explanation: All thresholds are greater than 2, so no discount tier is available.
"""

from typing import List


class Solution:
    def find_last_discount_tier(self, tiers: List[int], budgetQty: int) -> int:
        """
        Find the index of the last threshold that does not exceed the given budget quantity.

        Args:
            tiers: A strictly increasing sorted list of discount tier thresholds.
            budgetQty: The maximum quantity the customer can buy.

        Returns:
            The greatest index i such that tiers[i] <= budgetQty.
            Returns -1 if no such index exists.

        Time complexity:
            O(log n), where n is the number of tiers, because binary search halves
            the search space on each step.

        Space complexity:
            O(1), because only a constant amount of extra memory is used.
        """
        # We will perform a classic binary search on the sorted array.
        #
        # Important idea:
        # We are NOT looking for an exact match only.
        # We are looking for the "rightmost" value that is <= budgetQty.
        #
        # Because the array is sorted in increasing order:
        # - If tiers[mid] <= budgetQty, then mid is a valid candidate.
        #   But there might be another valid index farther to the right,
        #   so we continue searching in the right half.
        # - If tiers[mid] > budgetQty, then mid is too large and every element
        #   to the right is also too large, so we search in the left half.

        # `left` marks the beginning of the current search range.
        left: int = 0

        # `right` marks the end of the current search range.
        right: int = len(tiers) - 1

        # `answer` stores the best valid index found so far.
        # We start with -1 because if no threshold is <= budgetQty,
        # that is exactly the value we must return.
        answer: int = -1

        # Continue searching while the current range is valid.
        # The range is valid when left has not crossed right.
        while left <= right:
            # Compute the middle index safely.
            # In Python, overflow is not a practical issue for integers,
            # but this formula is still the standard binary search pattern.
            mid: int = left + (right - left) // 2

            # Case 1:
            # The middle threshold is affordable within the customer's budget.
            if tiers[mid] <= budgetQty:
                # This means `mid` is a valid answer candidate.
                # We store it because it satisfies the condition.
                answer = mid

                # But we are specifically asked for the LAST such index,
                # so we must check whether there is another valid threshold
                # farther to the right.
                left = mid + 1

            else:
                # Case 2:
                # The middle threshold is too large.
                # Since the array is sorted, every element to the right of `mid`
                # is also too large, so none of them can be valid.
                #
                # Therefore, we discard the right half and continue searching
                # only in the left half.
                right = mid - 1

        # When the loop ends, `answer` contains:
        # - the rightmost index whose value is <= budgetQty, or
        # - -1 if no such index exists.
        return answer

    def search(self, tiers: List[int], budgetQty: int) -> int:
        """
        Wrapper method that calls the main binary search solution.

        Args:
            tiers: A strictly increasing sorted list of discount tier thresholds.
            budgetQty: The maximum quantity the customer can buy.

        Returns:
            The greatest index i such that tiers[i] <= budgetQty.
            Returns -1 if no such index exists.

        Time complexity:
            O(log n)

        Space complexity:
            O(1)
        """
        return self.find_last_discount_tier(tiers, budgetQty)


if __name__ == "__main__":
    solution = Solution()

    # Example 1 from the problem statement:
    # tiers = [5, 10, 20, 50], budgetQty = 23
    # Thresholds <= 23 are 5, 10, 20
    # The last valid threshold is 20 at index 2
    tiers1: List[int] = [5, 10, 20, 50]
    budget1: int = 23
    result1: int = solution.search(tiers1, budget1)
    print("Example 1 Result:", result1)  # Expected: 2

    # Example 2 from the problem statement:
    # tiers = [4, 8, 15], budgetQty = 2
    # No threshold is <= 2
    # Therefore the answer is -1
    tiers2: List[int] = [4, 8, 15]
    budget2: int = 2
    result2: int = solution.search(tiers2, budget2)
    print("Example 2 Result:", result2)  # Expected: -1

    # Additional quick checks for completeness:

    # If budget is exactly equal to a threshold, that threshold's index should be returned.
    tiers3: List[int] = [3, 6, 9, 12]
    budget3: int = 9
    result3: int = solution.search(tiers3, budget3)
    print("Exact Match Result:", result3)  # Expected: 2

    # If budget is larger than all thresholds, return the last index.
    tiers4: List[int] = [2, 5, 7]
    budget4: int = 100
    result4: int = solution.search(tiers4, budget4)
    print("Larger Than All Result:", result4)  # Expected: 2