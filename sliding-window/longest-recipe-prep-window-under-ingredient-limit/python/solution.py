"""
Title: Longest Recipe Prep Window Under Ingredient Limit

Problem Description:
A meal-planning app stores a chef's recipe schedule as an array of strings `recipes`,
where `recipes[i]` is the main ingredient category used by the `i`-th recipe prepared
that day. The chef wants to analyze the longest contiguous stretch of recipes that can
be cooked without making the pantry too diverse.

Given `recipes` and an integer `k`, return the length of the longest contiguous subarray
that contains at most `k` distinct ingredient categories.

In other words, you need to find the largest window [l, r] such that among
recipes[l], recipes[l+1], ..., recipes[r], there are no more than `k` different
category names.

This problem should be solved efficiently for large inputs, so solutions that check
every possible subarray will be too slow.

Constraints:
- 1 <= recipes.length <= 100000
- 1 <= recipes[i].length <= 20
- recipes[i] consists of lowercase English letters
- 1 <= k <= recipes.length

Example 1:
Input: recipes = ["dairy","grain","dairy","spice","grain","grain"], k = 2
Output: 3

Example 2:
Input: recipes = ["meat","meat","veg","veg","sauce","veg","veg"], k = 2
Output: 4
"""

from typing import Dict, List


class Solution:
    def longest_recipe_window(self, recipes: List[str], k: int) -> int:
        """
        Find the length of the longest contiguous subarray containing at most
        k distinct recipe categories.

        Args:
            recipes: A list of recipe ingredient category names.
            k: The maximum number of distinct categories allowed in a valid window.

        Returns:
            The maximum length of a contiguous subarray with at most k distinct categories.

        Time complexity:
            O(n), where n is the number of recipes, because each pointer moves
            across the array at most once.

        Space complexity:
            O(k) in the typical sliding-window sense, or more precisely O(m),
            where m is the number of distinct categories currently tracked in
            the window. In the worst case this can be O(n).
        """
        # This dictionary stores the frequency of each recipe category
        # currently inside the sliding window.
        #
        # Example:
        # If the current window is ["grain", "dairy", "grain"],
        # then counts will be:
        # {
        #     "grain": 2,
        #     "dairy": 1
        # }
        #
        # Why use a dictionary?
        # - We need to quickly update counts as the window expands and shrinks.
        # - We need to know how many distinct categories are currently inside
        #   the window.
        # - Dictionary operations (get, set, delete) are average O(1).
        counts: Dict[str, int] = {}

        # `left` is the left boundary of our sliding window.
        # The right boundary will be controlled by the loop variable `right`.
        left: int = 0

        # This will store the best (maximum) valid window length found so far.
        max_length: int = 0

        # We expand the window one recipe at a time by moving `right`
        # from the start of the array to the end.
        for right, category in enumerate(recipes):
            # Add the current category to the window by increasing its count.
            # If it is not already present, start its count at 0 first.
            counts[category] = counts.get(category, 0) + 1

            # At this point, the window is recipes[left:right+1].
            #
            # However, after adding the new category, the window may now contain
            # more than k distinct categories, which makes it invalid.
            #
            # While the window is invalid, we must shrink it from the left side
            # until it becomes valid again.
            while len(counts) > k:
                # Identify the category that is leaving the window.
                left_category: str = recipes[left]

                # Decrease its frequency because we are moving the left boundary
                # one step to the right.
                counts[left_category] -= 1

                # If the frequency becomes 0, that means this category is no longer
                # present anywhere in the current window.
                #
                # We remove it from the dictionary entirely so that:
                # - len(counts) correctly reflects the number of distinct categories
                # - the window can become valid again once distinct count <= k
                if counts[left_category] == 0:
                    del counts[left_category]

                # Move the left boundary rightward to complete the shrink step.
                left += 1

            # Once we exit the while-loop, the current window is guaranteed valid:
            # it contains at most k distinct categories.
            #
            # Compute its length:
            # - right is inclusive
            # - left is inclusive
            # so length = right - left + 1
            current_length: int = right - left + 1

            # Update the best answer if this valid window is larger than any
            # previously seen valid window.
            if current_length > max_length:
                max_length = current_length

        # After processing all windows, return the largest valid length found.
        return max_length


if __name__ == "__main__":
    solution = Solution()

    recipes1: List[str] = ["dairy", "grain", "dairy", "spice", "grain", "grain"]
    k1: int = 2
    result1: int = solution.longest_recipe_window(recipes1, k1)
    print(result1)  # Expected: 3

    recipes2: List[str] = ["meat", "meat", "veg", "veg", "sauce", "veg", "veg"]
    k2: int = 2
    result2: int = solution.longest_recipe_window(recipes2, k2)
    print(result2)  # Expected: 4