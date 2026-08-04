"""
Title: Count Equivalent Ingredient Lists by Frequency

Problem Description:
A meal delivery company stores each recipe as a list of ingredient names. Two recipes
are considered equivalent if they use exactly the same multiset of ingredients,
ignoring the order of the ingredients but not their frequencies. For example,
["egg", "milk", "egg", "flour"] is equivalent to
["flour", "egg", "milk", "egg"], but it is not equivalent to
["egg", "milk", "flour"] because one copy of "egg" is missing.

You are given an array recipes where recipes[i] is a non-empty array of lowercase
strings representing the ingredients of the i-th recipe. Return the number of
unordered pairs of indices (i, j) such that 0 <= i < j < recipes.length and
recipes[i] is equivalent to recipes[j].

The solution must be efficient for large inputs. A brute-force comparison of every
pair of recipes would be too slow. Instead, we build a canonical signature for each
recipe so that equivalent recipes produce the same key and non-equivalent recipes
produce different keys.

Constraints:
- 1 <= recipes.length <= 100000
- 1 <= recipes[i].length <= 20
- 1 <= ingredients[i][k].length <= 20
- Each ingredient consists only of lowercase English letters
- The total number of ingredient strings across all recipes does not exceed 300000

Example 1:
Input:
recipes = [
    ["egg", "milk", "egg"],
    ["milk", "egg", "egg"],
    ["egg", "milk"],
    ["flour"],
    ["flour"]
]
Output: 2

Explanation:
- Recipes 0 and 1 are equivalent.
- Recipes 3 and 4 are equivalent.
So the total number of equivalent unordered pairs is 2.

Example 2:
Input:
recipes = [
    ["tomato", "basil", "tomato"],
    ["basil", "tomato", "tomato"],
    ["tomato", "basil"],
    ["cheese", "tomato"],
    ["tomato", "cheese"]
]
Output: 2

Explanation:
- Recipes 0 and 1 are equivalent.
- Recipes 3 and 4 are equivalent.
So the total number of equivalent unordered pairs is 2.
"""

from collections import Counter, defaultdict
from typing import DefaultDict, Dict, List, Tuple


class Solution:
    def _build_signature(self, recipe: List[str]) -> Tuple[Tuple[str, int], ...]:
        """
        Build a canonical, hashable signature for one recipe.

        The signature must uniquely represent the multiset of ingredients:
        - Order should not matter
        - Frequency must matter

        We achieve this by:
        1. Counting how many times each ingredient appears
        2. Sorting the (ingredient, count) pairs by ingredient name
        3. Converting the sorted pairs into a tuple so it can be used as a dictionary key

        Args:
            recipe: A list of ingredient names for one recipe.

        Returns:
            A tuple of (ingredient, count) pairs in sorted order.

        Time complexity:
            O(k + u log u), where:
            - k is the number of ingredients in the recipe
            - u is the number of distinct ingredients in the recipe

        Space complexity:
            O(u), for the frequency map and resulting signature.
        """
        # Count how many times each ingredient appears in this recipe.
        # Example:
        # ["egg", "milk", "egg"] -> {"egg": 2, "milk": 1}
        frequency: Counter[str] = Counter(recipe)

        # Sort the items so that recipes with the same ingredient counts but different
        # original order produce exactly the same canonical representation.
        #
        # Without sorting:
        # {"egg": 2, "milk": 1} and {"milk": 1, "egg": 2}
        # might not always produce the same iteration order in a general sense.
        #
        # With sorting:
        # both become (("egg", 2), ("milk", 1))
        signature: Tuple[Tuple[str, int], ...] = tuple(sorted(frequency.items()))

        return signature

    def count_equivalent_recipes(self, recipes: List[List[str]]) -> int:
        """
        Count the number of unordered pairs of equivalent recipes.

        Two recipes are equivalent if they contain exactly the same ingredients
        with exactly the same frequencies, regardless of order.

        The efficient strategy is:
        - Convert each recipe into a canonical signature
        - Count how many times each signature appears
        - For each signature appearing c times, add c * (c - 1) // 2 pairs

        An even more efficient streaming variation is used here:
        - As we process each recipe, if its signature has already appeared x times,
          then the current recipe forms exactly x new equivalent pairs.
        - Then we increment the count for that signature.

        Args:
            recipes: A list of recipes, where each recipe is a list of ingredient names.

        Returns:
            The number of unordered equivalent pairs.

        Time complexity:
            O(T + sum(u_i log u_i)), where:
            - T is the total number of ingredient strings across all recipes
            - u_i is the number of distinct ingredients in recipe i

            Since each recipe has at most 20 ingredients, this is efficient in practice.

        Space complexity:
            O(m), where m is the number of distinct recipe signatures.
        """
        # This dictionary stores how many times we have already seen each canonical recipe
        # signature.
        #
        # Key:
        #   A tuple like (("egg", 2), ("milk", 1))
        #
        # Value:
        #   The number of previous recipes with exactly that signature
        signature_count: DefaultDict[Tuple[Tuple[str, int], ...], int] = defaultdict(int)

        # This will accumulate the total number of valid unordered pairs.
        total_pairs: int = 0

        # Process each recipe one by one.
        for recipe in recipes:
            # Convert the current recipe into its canonical signature.
            signature: Tuple[Tuple[str, int], ...] = self._build_signature(recipe)

            # If we have already seen this signature x times, then the current recipe
            # forms x new pairs:
            #
            # previous matching recipes: indices a, b, c, ...
            # current recipe index: i
            #
            # New pairs are:
            # (a, i), (b, i), (c, i), ...
            #
            # So we add exactly the number of previous occurrences.
            total_pairs += signature_count[signature]

            # Record that we have now seen one more recipe with this signature.
            signature_count[signature] += 1

        return total_pairs


if __name__ == "__main__":
    solution = Solution()

    # Example 1 from the problem statement
    recipes1: List[List[str]] = [
        ["egg", "milk", "egg"],
        ["milk", "egg", "egg"],
        ["egg", "milk"],
        ["flour"],
        ["flour"],
    ]
    result1: int = solution.count_equivalent_recipes(recipes1)
    print("Example 1 Output:", result1)  # Expected: 2

    # Example 2 from the problem statement
    recipes2: List[List[str]] = [
        ["tomato", "basil", "tomato"],
        ["basil", "tomato", "tomato"],
        ["tomato", "basil"],
        ["cheese", "tomato"],
        ["tomato", "cheese"],
    ]
    result2: int = solution.count_equivalent_recipes(recipes2)
    print("Example 2 Output:", result2)  # Expected: 2

    # Additional small sanity checks for beginners

    # Same ingredients, same counts, different order -> equivalent
    recipes3: List[List[str]] = [
        ["a", "b", "a"],
        ["b", "a", "a"],
    ]
    result3: int = solution.count_equivalent_recipes(recipes3)
    print("Sanity Check 1 Output:", result3)  # Expected: 1

    # Same ingredients, different counts -> not equivalent
    recipes4: List[List[str]] = [
        ["a", "b", "a"],
        ["a", "b"],
    ]
    result4: int = solution.count_equivalent_recipes(recipes4)
    print("Sanity Check 2 Output:", result4)  # Expected: 0

    # Multiple identical single-ingredient recipes
    recipes5: List[List[str]] = [
        ["flour"],
        ["flour"],
        ["flour"],
    ]
    result5: int = solution.count_equivalent_recipes(recipes5)
    print("Sanity Check 3 Output:", result5)  # Expected: 3
