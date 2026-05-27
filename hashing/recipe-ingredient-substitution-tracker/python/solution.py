```python
"""
Recipe Ingredient Substitution Tracker
=======================================
Difficulty: Medium
Topic: Hashing

Problem Description:
A cooking platform maintains a list of ingredient substitution rules. Each rule states
that ingredient A can be substituted with ingredient B (one-directional). Given a list
of recipes, where each recipe is represented as a list of ingredient names, and a pantry
list of ingredients the user currently has, determine which recipes the user can fully
prepare — either by using the exact ingredient or any valid substitution chain.

A substitution chain means: if A → B and B → C, then A can be substituted with C
(transitively). However, you may apply at most one substitution per ingredient in a
recipe (i.e., you cannot substitute a substitution).

Return the list of recipe names that can be made, sorted lexicographically.

Constraints:
- 1 <= substitutions.length <= 500
- 1 <= recipes.length <= 200
- Each recipe has 1 to 20 ingredients
- 1 <= pantry.length <= 300
- All ingredient names are lowercase strings of length 1 to 20
- Substitution pairs are unique
- No circular substitution chains exist

Example 1:
Input:
  substitutions = [["butter", "margarine"], ["margarine", "coconut-oil"], ["milk", "oat-milk"]]
  pantry = ["coconut-oil", "oat-milk", "flour", "sugar"]
  recipes = [["cake", ["butter", "milk", "flour", "sugar"]],
             ["bread", ["butter", "flour"]],
             ["cookies", ["butter", "eggs"]]]
Output: ["bread", "cake"]

Wait — re-reading the problem carefully:
"at most one substitution per ingredient" means only ONE hop is allowed.
butter → margarine (one hop) — margarine NOT in pantry
butter cannot go to coconut-oil (that would be two hops)
So butter is NOT satisfiable.

But the expected output is ["bread", "cake"]... Let me re-read.

Actually looking at the example explanation more carefully, it says:
"butter → margarine → coconut-oil (chain of length 2, but we allow one substitution
per ingredient — wait, butter can reach coconut-oil via chain, but only one substitution
is allowed per ingredient."

The explanation itself seems confused. But the OUTPUT is ["bread", "cake"].

For bread: ["butter", "flour"] — flour is in pantry. butter needs substitution.
For cake: ["butter", "milk", "flour", "sugar"] — flour, sugar in pantry. milk→oat-milk works.

For both bread and cake to be in output, butter must be satisfiable.
butter→margarine→coconut-oil: coconut-oil IS in pantry.

So it seems the problem DOES allow the full chain (transitive closure), despite saying
"at most one substitution". The example output contradicts the "one substitution" restriction.

Given that the OUTPUT ["bread", "cake"] is the ground truth, we must support full
transitive chains. The "at most one substitution" note in the problem description
appears to be an error in the problem statement.

Let's implement transitive closure: if A can reach any ingredient in the pantry
through the substitution chain, then A is satisfiable.

Verification:
Example 1:
- butter → margarine → coconut-oil (coconut-oil in pantry) ✓
- milk → oat-milk (oat-milk in pantry) ✓
- flour in pantry ✓
- sugar in pantry ✓
- eggs: no substitution, not in pantry ✗
- cake: butter✓, milk✓, flour✓, sugar✓ → cake ✓
- bread: butter✓, flour✓ → bread ✓
- cookies: butter✓, eggs✗ → cookies ✗
Output: ["bread", "cake"] ✓

Example 2:
- eggs → flax-egg (in pantry) ✓
- butter → vegan-butter (in pantry) ✓
- flour in pantry ✓
- sugar in pantry ✓
- milk: no substitution, not in pantry ✗
- muffins: eggs✓, butter✓, flour✓, sugar✓ → muffins ✓
- pancakes: eggs✓, milk✗ → pancakes ✗
Output: ["muffins"] ✓
"""

from typing import List, Dict, Set, Tuple


class Solution:
    def find_makeable_recipes(
        self,
        substitutions: List[List[str]],
        pantry: List[str],
        recipes: List[List],
    ) -> List[str]:
        """
        Determine which recipes can be fully prepared given a pantry and substitution rules.

        The algorithm:
        1. Build a substitution graph (directed: A → B means A can be replaced by B).
        2. Compute the transitive closure: for each ingredient, find ALL ingredients
           reachable via the substitution chain.
        3. For each recipe ingredient, check if the ingredient itself OR any reachable
           substitute is in the pantry.
        4. A recipe is makeable only if ALL its ingredients are satisfiable.
        5. Return makeable recipe names sorted lexicographically.

        Args:
            substitutions: List of [A, B] pairs meaning A can be substituted with B.
            pantry: List of ingredient names the user currently has.
            recipes: List of [recipe_name, [ingredient1, ingredient2, ...]] pairs.

        Returns:
            Sorted list of recipe names that can be fully prepared.

        Time Complexity:
            O(S^2 + R * I * S) where S = number of substitutions (nodes in graph),
            R = number of recipes, I = max ingredients per recipe.
            Building transitive closure: O(S^2) using BFS/DFS per node.
            Checking recipes: O(R * I * S) in worst case.

        Space Complexity:
            O(S^2) for storing the transitive closure sets,
            O(S) for the adjacency list,
            O(P) for the pantry set where P = pantry size.
        """

        # -----------------------------------------------------------------------
        # STEP 1: Build a directed adjacency list from substitution rules.
        # Key = ingredient that CAN be substituted (source)
        # Value = ingredient it maps TO (destination)
        # We use a dict mapping each ingredient to its direct substitute.
        # Note: "Substitution pairs are unique" and the structure is a simple chain
        # (no circular chains), so each ingredient has at most one direct substitute.
        # -----------------------------------------------------------------------
        direct_sub: Dict[str, str] = {}
        for rule in substitutions:
            # rule[0] can be substituted with rule[1]
            source, dest = rule[0], rule[1]
            direct_sub[source] = dest

        # -----------------------------------------------------------------------
        # STEP 2: Convert pantry list to a set for O(1) lookup.
        # Using a set dramatically speeds up the "is this ingredient available?"
        # check from O(P) per lookup to O(1).
        # -----------------------------------------------------------------------
        pantry_set: Set[str] = set(pantry)

        # -----------------------------------------------------------------------
        # STEP 3: Build transitive closure.
        # For each ingredient that appears as a source in substitutions, follow
        # the chain until we reach an ingredient with no further substitution.
        # We cache results to avoid recomputation (memoization).
        #
        # reachable[ingredient] = set of ALL ingredients reachable from ingredient
        # via the substitution chain (not including the ingredient itself).
        #
        # Example: butter→margarine→coconut-oil
        #   reachable["butter"] = {"margarine", "coconut-oil"}
        #   reachable["margarine"] = {"coconut-oil"}
        # -----------------------------------------------------------------------
        reachable: Dict[str, Set[str]] = {}

        def get_reachable(ingredient: str) -> Set[str]:
            """
            Recursively compute all ingredients reachable from 'ingredient'
            via the substitution chain. Uses memoization to avoid redundant work.

            Args:
                ingredient: The starting ingredient name.

            Returns:
                Set of all ingredients reachable from this ingredient (excluding itself).
            """
            # If already computed, return cached result immediately
            if ingredient in reachable:
                return reachable[ingredient]

            # If this ingredient has no direct substitution, it reaches nothing
            if ingredient not in direct_sub:
                reachable[ingredient] = set()
                return reachable[ingredient]

            # Get the direct substitute
            next_ingredient = direct_sub[ingredient]

            # Recursively get everything reachable from the next ingredient
            # This gives us the transitive closure
            further_reachable = get_reachable(next_ingredient)

            # This ingredient can reach: its direct substitute + everything
            # reachable from that direct substitute
            reachable[ingredient] = {next_ingredient} | further_reachable

            return reachable[ingredient]

        # Pre-compute reachable sets for all source ingredients in substitutions
        # This ensures our memoization cache is fully populated before recipe checking
        for rule in substitutions:
            source = rule[0]
            get_reachable(source)

        # -----------------------------------------------------------------------
        # STEP 4: Define a helper to check if a single ingredient is satisfiable.
        # An ingredient is satisfiable if:
        #   (a) It is directly in the pantry, OR
        #   (b) Any ingredient reachable from it (via substitution chain) is in pantry
        # -----------------------------------------------------------------------
        def can_satisfy(ingredient: str) -> bool:
            """
            Check if an ingredient can be satisfied using the pantry
            (directly or via substitution chain).

            Args:
                ingredient: The ingredient name to check.

            Returns:
                True if the ingredient or any valid substitute is in the pantry.
            """
            # Direct check: ingredient itself is in pantry
            if ingredient in pantry_set:
                return True

            # Substitution check: any reachable substitute is in pantry
            # get_reachable returns a set; we check intersection with pantry_set
            reachable_ingredients = get_reachable(ingredient)
            for sub in reachable_ingredients:
                if sub in pantry_set:
                    return True

            # Neither the ingredient nor any substitute is available
            return False

        # -----------------------------------------------------------------------
        # STEP 5: Check each recipe.
        # A recipe is makeable if and only if EVERY ingredient in it is satisfiable.
        # We collect the names of all makeable recipes.
        # -----------------------------------------------------------------------
        makeable_recipes: List[str] = []

        for recipe_entry in recipes:
            # Each entry is [recipe_name, [ingredient1, ingredient2, ...]]
            recipe_name: str = recipe_entry[0]
            ingredients: List[str] = recipe_entry[1]

            # Check if ALL ingredients in this recipe can be satisfied
            # Using all() with a generator for short-circuit evaluation:
            # stops as soon as one ingredient fails
            if all(can_satisfy(ing) for ing in ingredients):
                makeable_recipes.append(recipe_name)

        # -----------------------------------------------------------------------
        # STEP 6: Sort the result lexicographically as required by the problem.
        # -----------------------------------------------------------------------
        makeable_recipes.sort()

        return makeable_recipes


# =============================================================================
# MAIN: Demonstrate the solution with the provided examples
# =============================================================================
if __name__ == "__main__":
    solution = Solution()

    # -------------------------------------------------------------------------
    # Example 1
    # Expected Output: ["bread", "cake"]
    #
    # Trace:
    # - Substitution chains:
    #     butter → margarine → coconut-oil
    #     milk → oat-milk
    # - Pantry: {coconut-oil, oat-milk, flour, sugar}
    # - Reachable sets:
    #     butter: {margarine, coconut-oil}
    #     margarine: {coconut-oil}
    #     milk: {oat-milk}
    # - Ingredient satisfiability:
    #     butter: not in pantry, but coconut-oil ∈ reachable(butter) ∩ pantry → ✓
    #     milk: not in pantry, but oat-milk ∈ reachable(milk) ∩ pantry → ✓
    #     flour: in pantry → ✓
    #     sugar: in pantry → ✓
    #     eggs: not in pantry, reachable(eggs) = {} → ✗
    # - Recipes:
    #     cake [butter✓, milk✓, flour✓, sugar✓] → makeable ✓
    #     bread [butter✓, flour✓] → makeable ✓
    #     cookies [butter✓, eggs✗] → NOT makeable ✗
    # - Sorted: ["bread", "cake"]
    # -------------------------------------------------------------------------
    print("=" * 60)
    print("Example 1")
    print("=" * 60)

    substitutions1 = [
        ["butter", "margarine"],
        ["margarine", "coconut-oil"],
        ["milk", "oat-milk"],
    ]
    pantry1 = ["coconut-oil", "oat-milk", "flour", "sugar"]
    recipes1 = [
        ["cake", ["butter", "milk", "flour", "sugar"]],
        ["bread", ["butter", "flour"]],
        ["cookies", ["butter", "eggs"]],
    ]

    result1 = solution.find_makeable_recipes(substitutions1, pantry1, recipes1)
    print(f"Input substitutions : {substitutions1}")
    print(f"Input pantry        : {pantry1}")
    print(f"Input recipes       : {recipes1}")
    print(f"Output              : {result1}")
    print(f"Expected            : ['bread', 'cake']")
    print(f"PASS" if result1 == ["bread", "cake"] else f"FAIL")
    print()

    # -------------------------------------------------------------------------
    # Example 2
    # Expected Output: ["muffins"]
    #
    # Trace:
    # - Substitution chains:
    #     eggs → flax-egg
    #     butter → vegan-butter
    # - Pantry: {flax-egg, vegan-butter, flour, sugar}
    # - Reachable sets:
    #     eggs: {flax-egg}
    #     butter: {vegan-butter}
    # - Ingredient satisfiability:
    #     eggs: not in pantry, flax-egg ∈ reachable(eggs) ∩ pantry → ✓
    #     butter: not in pantry, vegan-butter ∈ reachable(butter) ∩ pantry → ✓
    #     flour: in pantry → ✓
    #     sugar: in pantry → ✓
    #     milk: not in pantry, reachable(milk) = {} → ✗
    # - Recipes:
    #     muffins [eggs✓, butter✓, flour✓, sugar✓] → makeable ✓
    #     pancakes [eggs✓, milk✗] → NOT makeable ✗
    # - Sorted: ["muffins"]
    # -------------------------------------------------------------------------
    print("=" * 60)
    print("Example 2")
    print("=" * 60)

    substitutions2 = [
        ["eggs", "flax-egg"],
        ["butter", "vegan-butter"],
    ]
    pantry2 = ["flax-egg", "vegan-butter", "flour", "sugar"]
    recipes2 = [
        ["muffins", ["eggs", "butter", "flour", "sugar"]],
        ["pancakes", ["eggs", "milk"]],
    ]

    result2 = solution.find_makeable_recipes(substitutions2, pantry2, recipes2)
    print(f"Input substitutions : {substitutions2}")
    print(f"Input pantry        : {pantry2}")
    print(f"Input recipes       : {recipes2}")
    print(f"Output              : {result2}")
    print(f"Expected            : ['muffins']")
    print(f"PASS" if result2 == ["muffins"] else f"FAIL")
    print()

    # -------------------------------------------------------------------------
    # Additional Edge Case: Empty pantry — no recipes can be made
    # -------------------------------------------------------------------------
    print("=" *