"""
Title: Shortest Recipe Span Covering All Required Ingredients

Problem Description:
A cooking platform stores each recipe as an ordered list of ingredient tokens, where
the same ingredient may appear multiple times if it is used in different steps.
You are given an array ingredients representing this token stream for one recipe,
and another array required representing the ingredients that must all appear in a
highlighted excerpt. Your task is to return the length of the shortest contiguous
span of ingredients that contains every ingredient in required at least once.
If no such span exists, return -1.

This problem models finding the smallest meaningful excerpt of a recipe that still
includes all mandatory ingredients. The order inside the span does not matter, but
the span must be contiguous in the original recipe. Duplicate values in ingredients
are allowed. Assume required contains distinct ingredient names.

You should design an efficient solution using the two pointers / sliding window
technique. A brute-force approach that checks all subarrays will be too slow for
large inputs.

Constraints:
- 1 <= ingredients.length <= 200000
- 1 <= required.length <= 200000
- 1 <= required.length <= ingredients.length
- ingredients[i] and required[i] are non-empty strings of lowercase English letters
- required contains distinct strings

Example 1:
Input:
ingredients = ["salt","oil","garlic","onion","salt","tomato","basil"]
required = ["garlic","salt","tomato"]
Output: 4

Explanation:
The shortest valid span is ["garlic","onion","salt","tomato"], which has length 4.

Example 2:
Input:
ingredients = ["flour","egg","milk","butter","egg"]
required = ["egg","yeast"]
Output: -1

Explanation:
"yeast" never appears, so no contiguous span can satisfy the requirement.
"""

from typing import Dict, List, Set


class Solution:
    def shortest_recipe_span(self, ingredients: List[str], required: List[str]) -> int:
        """
        Find the length of the shortest contiguous subarray that contains every
        required ingredient at least once.

        Args:
            ingredients: Ordered list of ingredient tokens from the recipe.
            required: Distinct ingredient names that must all appear in the span.

        Returns:
            The minimum length of a contiguous span containing all required
            ingredients at least once, or -1 if no such span exists.

        Time complexity:
            O(n), where n is the length of ingredients.
            Each pointer (left and right) moves at most n times.

        Space complexity:
            O(k), where k is the number of required ingredients.
            We store the required set and the frequency map for the current window.
        """
        # Convert the required list into a set for O(1) average-time membership checks.
        # Why use a set?
        # - We only care whether an ingredient is required or not.
        # - The problem states required contains distinct strings, so a set is a natural fit.
        required_set: Set[str] = set(required)

        # This is the total number of distinct required ingredients we must cover.
        # Since required contains distinct values, this is simply len(required).
        total_needed: int = len(required_set)

        # window_count keeps track of how many times each required ingredient appears
        # inside the current sliding window [left, right].
        #
        # Important detail:
        # - We only store counts for ingredients that are actually required.
        # - Non-required ingredients do not help satisfy the condition, so we do not
        #   need to track them in this dictionary.
        window_count: Dict[str, int] = {}

        # formed tells us how many distinct required ingredients are currently satisfied
        # in the window.
        #
        # A required ingredient is considered "satisfied" when its count in the window
        # becomes at least 1.
        #
        # Example:
        # required = ["garlic", "salt", "tomato"]
        # If the current window contains:
        # - garlic: 2
        # - salt: 1
        # - tomato: 0
        # then formed = 2, because garlic and salt are present, but tomato is missing.
        formed: int = 0

        # left is the start index of the sliding window.
        left: int = 0

        # best_length stores the smallest valid window length found so far.
        # We initialize it to infinity so that any real valid window will be smaller.
        best_length: int = float("inf")

        # Expand the window by moving right from left to right across the array.
        for right, ingredient in enumerate(ingredients):
            # Step 1: Include ingredients[right] into the current window.
            #
            # We only care if this ingredient is one of the required ones.
            if ingredient in required_set:
                # Increase its count in the current window.
                window_count[ingredient] = window_count.get(ingredient, 0) + 1

                # If this is the first time this required ingredient appears in the window,
                # then we have satisfied one more required ingredient.
                if window_count[ingredient] == 1:
                    formed += 1

            # Step 2: If the current window already contains all required ingredients,
            # try to shrink it from the left to make it as short as possible.
            #
            # This is the key sliding window idea:
            # - Expand right until the window becomes valid.
            # - Then contract left while it stays valid.
            # - Update the answer during contraction.
            while formed == total_needed:
                # The current window [left, right] is valid because it contains
                # every required ingredient at least once.
                current_length: int = right - left + 1

                # Update the best answer if this valid window is smaller.
                if current_length < best_length:
                    best_length = current_length

                # We now attempt to remove ingredients[left] from the window
                # and see whether the window can remain valid.
                left_ingredient: str = ingredients[left]

                if left_ingredient in required_set:
                    # Decrease the count because this ingredient is leaving the window.
                    window_count[left_ingredient] -= 1

                    # If its count becomes 0, then the window is no longer valid
                    # for that required ingredient, so formed decreases.
                    if window_count[left_ingredient] == 0:
                        formed -= 1

                # Move the left boundary one step to the right.
                left += 1

        # If best_length was never updated, then no valid window exists.
        if best_length == float("inf"):
            return -1

        return best_length

    def solve(self, ingredients: List[str], required: List[str]) -> int:
        """
        Wrapper method that calls the main sliding window solution.

        Args:
            ingredients: Ordered list of ingredient tokens from the recipe.
            required: Distinct ingredient names that must all appear in the span.

        Returns:
            The minimum valid span length, or -1 if impossible.

        Time complexity:
            O(n), where n is the length of ingredients.

        Space complexity:
            O(k), where k is the number of required ingredients.
        """
        return self.shortest_recipe_span(ingredients, required)


if __name__ == "__main__":
    solution = Solution()

    # Example 1
    ingredients_1: List[str] = ["salt", "oil", "garlic", "onion", "salt", "tomato", "basil"]
    required_1: List[str] = ["garlic", "salt", "tomato"]
    result_1: int = solution.solve(ingredients_1, required_1)
    print("Example 1 Result:", result_1)  # Expected: 4

    # Example 2
    ingredients_2: List[str] = ["flour", "egg", "milk", "butter", "egg"]
    required_2: List[str] = ["egg", "yeast"]
    result_2: int = solution.solve(ingredients_2, required_2)
    print("Example 2 Result:", result_2)  # Expected: -1

    # Additional quick checks
    ingredients_3: List[str] = ["a", "b", "c"]
    required_3: List[str] = ["a", "b", "c"]
    result_3: int = solution.solve(ingredients_3, required_3)
    print("Example 3 Result:", result_3)  # Expected: 3

    ingredients_4: List[str] = ["x", "a", "y", "b", "z", "c", "a", "b"]
    required_4: List[str] = ["a", "b", "c"]
    result_4: int = solution.solve(ingredients_4, required_4)
    print("Example 4 Result:", result_4)  # Expected: 4