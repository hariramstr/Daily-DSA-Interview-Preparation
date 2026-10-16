"""
Title: Find the Last Bus Stop Before Fare Exceeds Budget

Problem Description:
You are given a non-decreasing array fares where fares[i] is the total bus fare
needed to travel from the starting terminal to stop i. Because the route gets
longer, the total fare never decreases as the stop index increases. You are also
given an integer budget representing the maximum amount a passenger can spend.

Return the largest index i such that fares[i] <= budget. If even the first stop
is too expensive, return -1.

This problem models a common interview pattern: finding the rightmost value in a
sorted array that satisfies a condition. Be careful with duplicate fares. If
several stops have the same fare and that fare is within the budget, you should
return the last such stop.

Constraints:
- 1 <= fares.length <= 100000
- 0 <= fares[i] <= 1000000000
- fares is sorted in non-decreasing order
- 0 <= budget <= 1000000000

Example 1:
Input: fares = [2, 4, 4, 7, 9], budget = 4
Output: 2
Explanation: Stops 1 and 2 both cost 4, which is within budget. The last
reachable stop is index 2.

Example 2:
Input: fares = [5, 8, 12], budget = 3
Output: -1
Explanation: The passenger cannot afford even the first stop, so the answer is -1.
"""

from typing import List


class Solution:
    def find_last_affordable_stop(self, fares: List[int], budget: int) -> int:
        """
        Find the largest index whose fare is less than or equal to the budget.

        Args:
            fares: A non-decreasing list of total fares to each stop.
            budget: The maximum fare the passenger can afford.

        Returns:
            The largest index i such that fares[i] <= budget, or -1 if no such
            index exists.

        Time Complexity:
            O(log n), where n is the number of stops, because binary search
            halves the search space each step.

        Space Complexity:
            O(1), because only a constant amount of extra memory is used.
        """
        # We will use binary search because the fares array is sorted in
        # non-decreasing order. This sorted property is exactly what allows us
        # to search efficiently instead of checking every stop one by one.
        #
        # Our goal is NOT just to find any fare that is <= budget.
        # Our goal is to find the RIGHTMOST such index.
        #
        # Example:
        # fares = [2, 4, 4, 7, 9], budget = 4
        # Valid indices are 0, 1, and 2.
        # We must return 2, the largest valid index.
        #
        # To do that, whenever we find a fare that is affordable, we record it
        # as a possible answer and continue searching to the RIGHT to see if
        # there is another affordable stop later in the array.

        # 'left' marks the beginning of the current search range.
        left: int = 0

        # 'right' marks the end of the current search range.
        right: int = len(fares) - 1

        # 'answer' stores the best valid index found so far.
        # We start with -1 because if no fare is affordable, that is the
        # required return value.
        answer: int = -1

        # Continue searching while there is still a valid range to inspect.
        while left <= right:
            # Compute the middle index safely.
            # In Python, overflow is not a practical issue for integers, but
            # this form is standard and widely used in binary search.
            mid: int = left + (right - left) // 2

            # If the fare at 'mid' is affordable, then:
            # 1. 'mid' is a valid candidate answer.
            # 2. There may still be a later index that is also affordable.
            # So we save 'mid' and move the search to the right half.
            if fares[mid] <= budget:
                answer = mid
                left = mid + 1
            else:
                # If fares[mid] is too expensive, then 'mid' cannot be part of
                # the answer, and neither can anything to its right because the
                # array is sorted and all later fares are >= fares[mid].
                #
                # Therefore, we must search only the left half.
                right = mid - 1

        # When the loop ends, 'answer' is the largest index found with
        # fares[index] <= budget, or -1 if none existed.
        return answer


if __name__ == "__main__":
    solution = Solution()

    fares_1: List[int] = [2, 4, 4, 7, 9]
    budget_1: int = 4
    result_1: int = solution.find_last_affordable_stop(fares_1, budget_1)
    print(f"fares = {fares_1}, budget = {budget_1} -> {result_1}")

    fares_2: List[int] = [5, 8, 12]
    budget_2: int = 3
    result_2: int = solution.find_last_affordable_stop(fares_2, budget_2)
    print(f"fares = {fares_2}, budget = {budget_2} -> {result_2}")