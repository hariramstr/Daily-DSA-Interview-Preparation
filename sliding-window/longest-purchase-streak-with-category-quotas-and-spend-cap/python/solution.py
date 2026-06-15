"""
Title: Longest Purchase Streak With Category Quotas and Spend Cap

Problem Description:
You are given a chronological list of purchases made by a customer during a promotional
campaign. Each purchase is represented by two arrays of equal length: cost[i] is the
amount spent on the i-th purchase, and category[i] is the category label of that
purchase. You are also given an integer budget, and a dictionary quota where quota[c]
specifies the minimum number of purchases from category c that must appear inside a
valid streak.

A purchase streak is any contiguous subarray of purchases. A streak is considered
eligible if:
1. The total cost of all purchases in the streak is at most budget.
2. For every category c listed in quota, the streak contains at least quota[c]
   purchases whose category is c.

Return the length of the longest eligible streak. If no streak satisfies all quotas
under the budget, return 0.

Constraints:
- 1 <= n == cost.length == category.length <= 2 * 10^5
- 1 <= cost[i] <= 10^9
- 1 <= budget <= 10^15
- 1 <= number of distinct categories in quota <= 2 * 10^5
- category[i] is a lowercase string of length between 1 and 20
- 1 <= quota[c] <= n
- Categories appearing in quota may or may not appear in the purchase list
"""

from typing import Dict, List


class Solution:
    def longest_purchase_streak(
        self,
        cost: List[int],
        category: List[str],
        budget: int,
        quota: Dict[str, int],
    ) -> int:
        """
        Find the maximum length of a contiguous purchase streak whose total cost is
        at most the given budget and that satisfies all category minimum quotas.

        Args:
            cost: List of purchase costs.
            category: List of category labels aligned with cost.
            budget: Maximum allowed total cost for the streak.
            quota: Required minimum counts for specific categories.

        Returns:
            The length of the longest valid contiguous streak. Returns 0 if no valid
            streak exists.

        Time complexity:
            O(n), where n is the number of purchases. Each index enters and leaves
            the sliding window at most once.

        Space complexity:
            O(k), where k is the number of distinct categories tracked in quota.
        """
        n: int = len(cost)

        # Basic safety check. The problem guarantees equal lengths, but keeping this
        # makes the function more robust and beginner-friendly.
        if n != len(category):
            raise ValueError("cost and category must have the same length")

        # If there are no quota requirements, then the problem becomes:
        # "Find the longest subarray with sum <= budget".
        # The same sliding-window logic below already handles that case correctly,
        # because required_categories_count will be 0 and all quotas are considered
        # satisfied immediately.
        required_categories_count: int = len(quota)

        # Quick impossibility pruning:
        # If any required category does not appear enough times in the entire array,
        # then no window can ever satisfy the quota for that category.
        # This is not strictly necessary for correctness, but it can avoid work and
        # also makes the logic easier to reason about.
        total_seen: Dict[str, int] = {}
        for cat in category:
            if cat in quota:
                total_seen[cat] = total_seen.get(cat, 0) + 1

        for cat, needed in quota.items():
            if total_seen.get(cat, 0) < needed:
                return 0

        # Sliding window state:
        # left and right define the current window [left, right].
        left: int = 0
        current_cost: int = 0

        # window_counts stores counts only for categories that matter to quota.
        # We do not need to track categories outside quota because they never affect
        # whether the quota condition is satisfied.
        window_counts: Dict[str, int] = {}

        # satisfied_categories tells us how many quota categories currently meet
        # their required minimum inside the window.
        #
        # Why this is useful:
        # Instead of checking every category in quota each time (which could be too
        # slow), we maintain this number incrementally. Then:
        #   all quotas satisfied <=> satisfied_categories == required_categories_count
        satisfied_categories: int = 0

        # This will store the best valid window length found so far.
        best_length: int = 0

        # Expand the window one purchase at a time using right.
        for right in range(n):
            # Add the new purchase at index right into the window.
            current_cost += cost[right]
            right_cat: str = category[right]

            # If this category is relevant to quota, update its count.
            if right_cat in quota:
                new_count: int = window_counts.get(right_cat, 0) + 1
                window_counts[right_cat] = new_count

                # If we just reached the exact required amount for this category,
                # then one more quota category has become satisfied.
                if new_count == quota[right_cat]:
                    satisfied_categories += 1

            # First, enforce the budget constraint.
            #
            # Because all costs are positive, if current_cost > budget, the only way
            # to fix it is to move left forward and remove items from the window.
            while left <= right and current_cost > budget:
                left_cat: str = category[left]

                # Remove the leftmost purchase cost from the running sum.
                current_cost -= cost[left]

                # If the removed category matters to quota, update its count.
                if left_cat in quota:
                    old_count: int = window_counts[left_cat]

                    # If before removing this item the category was exactly at its
                    # required threshold, then removing it will make that category
                    # no longer satisfied.
                    if old_count == quota[left_cat]:
                        satisfied_categories -= 1

                    window_counts[left_cat] = old_count - 1

                left += 1

            # At this point, the window always satisfies the budget condition.
            # Now we simply check whether all quota requirements are also satisfied.
            #
            # Important reasoning:
            # We do NOT shrink further once the window is valid, because shrinking
            # would only make the window shorter. Since we want the longest valid
            # window ending at 'right', keeping the smallest possible left that still
            # respects budget is best for maximizing length.
            if satisfied_categories == required_categories_count:
                window_length: int = right - left + 1
                if window_length > best_length:
                    best_length = window_length

        return best_length

    def solve(
        self,
        cost: List[int],
        category: List[str],
        budget: int,
        quota: Dict[str, int],
    ) -> int:
        """
        Convenience wrapper around the main algorithm.

        Args:
            cost: List of purchase costs.
            category: List of category labels.
            budget: Maximum allowed total cost.
            quota: Required minimum counts for categories.

        Returns:
            Length of the longest valid purchase streak.

        Time complexity:
            O(n)

        Space complexity:
            O(k)
        """
        return self.longest_purchase_streak(cost, category, budget, quota)


if __name__ == "__main__":
    solution = Solution()

    # Example 1 from the prompt:
    # The prompt's written explanation is inconsistent in places, but after checking
    # all valid windows carefully, the correct answer is 4.
    cost1 = [4, 2, 3, 1, 2, 5, 1]
    category1 = ["grocery", "book", "grocery", "toy", "book", "grocery", "toy"]
    budget1 = 10
    quota1 = {"grocery": 2, "book": 1}
    result1 = solution.solve(cost1, category1, budget1, quota1)
    print("Example 1 result:", result1)  # Expected: 4

    # Example 2 from the prompt:
    # After checking all windows, no contiguous subarray can satisfy both:
    # - total cost <= 6
    # - at least one "a" and at least two "b"
    # So the correct answer is 0.
    cost2 = [2, 2, 2, 2, 2]
    category2 = ["a", "b", "a", "c", "b"]
    budget2 = 6
    quota2 = {"a": 1, "b": 2}
    result2 = solution.solve(cost2, category2, budget2, quota2)
    print("Example 2 result:", result2)  # Expected: 0

    # Additional small sanity checks.

    # Entire array is valid.
    cost3 = [1, 2, 1]
    category3 = ["x", "y", "x"]
    budget3 = 4
    quota3 = {"x": 2}
    result3 = solution.solve(cost3, category3, budget3, quota3)
    print("Additional test 1 result:", result3)  # Expected: 3

    # Impossible because required category never appears enough times.
    cost4 = [1, 1, 1, 1]
    category4 = ["a", "b", "a", "c"]
    budget4 = 10
    quota4 = {"b": 2}
    result4 = solution.solve(cost4, category4, budget4, quota4)
    print("Additional test 2 result:", result4)  # Expected: 0

    # No quota categories beyond one requirement; longest budget-valid window that
    # includes enough of that category should be found.
    cost5 = [3, 1, 1, 1, 3]
    category5 = ["m", "n", "m", "n", "m"]
    budget5 = 5
    quota5 = {"m": 1}
    result5 = solution.solve(cost5, category5, budget5, quota5)
    print("Additional test 3 result:", result5)  # Expected: 3