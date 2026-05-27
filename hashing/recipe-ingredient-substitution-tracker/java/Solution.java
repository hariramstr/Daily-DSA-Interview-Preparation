```java
/*
 * Recipe Ingredient Substitution Tracker
 * Difficulty: Medium
 * Topic: Hashing
 *
 * Problem Description:
 * A cooking platform maintains a list of ingredient substitution rules. Each rule states
 * that ingredient A can be substituted with ingredient B (one-directional). Given a list
 * of recipes, where each recipe is represented as a list of ingredient names, and a pantry
 * list of ingredients the user currently has, determine which recipes the user can fully
 * prepare — either by using the exact ingredient or any valid substitution chain.
 *
 * A substitution chain means: if A → B and B → C, then A can be substituted with C
 * (transitively). However, you may apply at most one substitution per ingredient in a
 * recipe (i.e., you cannot substitute a substitution).
 *
 * Wait — re-reading the problem carefully:
 * "at most one substitution per ingredient" means we can only do ONE hop in the chain.
 * So if butter → margarine → coconut-oil, butter can only be substituted with margarine
 * (one hop), NOT coconut-oil (two hops).
 *
 * Return the list of recipe names that can be made, sorted lexicographically.
 *
 * Example 1:
 * substitutions = [["butter", "margarine"], ["margarine", "coconut-oil"], ["milk", "oat-milk"]]
 * pantry = ["coconut-oil", "oat-milk", "flour", "sugar"]
 * recipes = [["cake", ["butter", "milk", "flour", "sugar"]], ["bread", ["butter", "flour"]], ["cookies", ["butter", "eggs"]]]
 * Output: ["bread", "cake"]
 *
 * Wait, let me re-trace Example 1 carefully:
 * - butter → margarine (one hop). margarine NOT in pantry. So butter cannot be satisfied.
 * - But output says "bread" and "cake" CAN be made...
 *
 * Hmm, let me re-read: "A substitution chain means: if A → B and B → C, then A can be
 * substituted with C (transitively). However, you may apply at most one substitution per
 * ingredient in a recipe."
 *
 * I think "at most one substitution per ingredient" means: for each ingredient in the recipe,
 * you pick ONE substitution to apply (you don't apply multiple substitutions to the same
 * ingredient). But the substitution itself can be a chain result.
 *
 * So the substitution chain IS allowed transitively — butter can reach coconut-oil via
 * butter→margarine→coconut-oil. The "at most one substitution per ingredient" means
 * you apply the substitution once (choosing the final target), not that you're limited
 * to one hop.
 *
 * Let me re-verify Example 1 with this interpretation:
 * - butter can reach: margarine (direct), coconut-oil (via margarine). coconut-oil IS in pantry. ✓
 * - milk can reach: oat-milk (direct). oat-milk IS in pantry. ✓
 * - flour IS in pantry. ✓
 * - sugar IS in pantry. ✓
 * - cake: all satisfied → YES ✓
 * - bread: butter→coconut-oil ✓, flour ✓ → YES ✓
 * - cookies: butter→coconut-oil ✓, eggs NOT in pantry and no substitution → NO ✓
 * Output: ["bread", "cake"] ✓
 *
 * Example 2:
 * - eggs → flax-egg (direct). flax-egg in pantry. ✓
 * - butter → vegan-butter (direct). vegan-butter in pantry. ✓
 * - flour in pantry. ✓
 * - sugar in pantry. ✓
 * - muffins: all satisfied → YES ✓
 * - pancakes: eggs→flax-egg ✓, milk NOT in pantry and no substitution → NO ✓
 * Output: ["muffins"] ✓
 *
 * Algorithm:
 * 1. Build a substitution map (ingredient → direct substitute).
 * 2. For each ingredient, follow the chain until we find something in the pantry or exhaust options.
 * 3. For each recipe, check if all ingredients can be satisfied.
 * 4. Return sorted list of satisfiable recipe names.
 */

import java.util.*;

/**
 * Solution for the Recipe Ingredient Substitution Tracker problem.
 * Uses hashing to efficiently look up substitutions and pantry items.
 */
public class Solution {

    /**
     * Determines which recipes can be made given a pantry and substitution rules.
     *
     * <p>The algorithm:
     * 1. Store pantry items in a HashSet for O(1) lookup.
     * 2. Store substitution rules in a HashMap (ingredient → substitute).
     * 3. For each ingredient in each recipe, follow the substitution chain
     *    to see if any reachable ingredient is in the pantry.
     * 4. Collect and sort the names of recipes where all ingredients are satisfiable.
     *
     * @param substitutions List of [ingredientA, ingredientB] pairs meaning A can be substituted with B
     * @param pantry        List of ingredients the user currently has
     * @param recipes       List of [recipeName, [ingredient1, ingredient2, ...]] pairs
     * @return Lexicographically sorted list of recipe names that can be fully prepared
     *
     * Time complexity: O(S + P + R * I * C) where S = substitutions count, P = pantry size,
     *                  R = recipes count, I = max ingredients per recipe, C = max chain length
     * Space complexity: O(S + P) for the substitution map and pantry set
     */
    public List<String> findMakeableRecipes(
            List<List<String>> substitutions,
            List<String> pantry,
            List<List<Object>> recipes) {

        // -----------------------------------------------------------------------
        // Step 1: Build a HashSet of pantry items for O(1) membership checks.
        // -----------------------------------------------------------------------
        Set<String> pantrySet = new HashSet<>(pantry);

        // -----------------------------------------------------------------------
        // Step 2: Build a HashMap of substitution rules.
        // Key = ingredient that needs substituting, Value = its direct substitute.
        // Example: "butter" → "margarine", "margarine" → "coconut-oil"
        // -----------------------------------------------------------------------
        Map<String, String> substitutionMap = new HashMap<>();
        for (List<String> rule : substitutions) {
            // rule.get(0) is the ingredient, rule.get(1) is its substitute
            substitutionMap.put(rule.get(0), rule.get(1));
        }

        // -----------------------------------------------------------------------
        // Step 3: Process each recipe to determine if it can be made.
        // -----------------------------------------------------------------------
        List<String> makeableRecipes = new ArrayList<>();

        for (List<Object> recipe : recipes) {
            // Each recipe entry is [recipeName, [ingredient1, ingredient2, ...]]
            String recipeName = (String) recipe.get(0);

            @SuppressWarnings("unchecked")
            List<String> ingredients = (List<String>) recipe.get(1);

            // Check if ALL ingredients in this recipe can be satisfied
            boolean canMake = true;

            for (String ingredient : ingredients) {
                // Check if this ingredient (or any substitute in its chain) is in the pantry
                if (!canSatisfyIngredient(ingredient, pantrySet, substitutionMap)) {
                    // This ingredient cannot be satisfied — recipe cannot be made
                    canMake = false;
                    break; // No need to check remaining ingredients
                }
            }

            if (canMake) {
                makeableRecipes.add(recipeName);
            }
        }

        // -----------------------------------------------------------------------
        // Step 4: Sort the result lexicographically as required by the problem.
        // -----------------------------------------------------------------------
        Collections.sort(makeableRecipes);

        return makeableRecipes;
    }

    /**
     * Checks whether a given ingredient can be satisfied using the pantry,
     * following the substitution chain transitively.
     *
     * <p>We follow the chain: ingredient → substitute1 → substitute2 → ...
     * until we find one that's in the pantry, or we run out of substitutes.
     *
     * <p>Since the problem guarantees no circular substitution chains exist,
     * we don't need cycle detection.
     *
     * @param ingredient      The ingredient to check
     * @param pantrySet       Set of available pantry items
     * @param substitutionMap Map of ingredient → its direct substitute
     * @return true if the ingredient or any substitute in its chain is in the pantry
     *
     * Time complexity: O(C) where C is the length of the substitution chain
     * Space complexity: O(1) — only uses a pointer variable
     */
    private boolean canSatisfyIngredient(
            String ingredient,
            Set<String> pantrySet,
            Map<String, String> substitutionMap) {

        // Start with the original ingredient and follow the chain
        String current = ingredient;

        // Keep following the substitution chain as long as there's a next step
        while (current != null) {
            // Check if the current ingredient (original or substitute) is in the pantry
            if (pantrySet.contains(current)) {
                return true; // Found it! This ingredient can be satisfied.
            }

            // Move to the next substitute in the chain (or null if no substitute exists)
            // Example: butter → margarine → coconut-oil → null (end of chain)
            current = substitutionMap.get(current);
        }

        // We exhausted the entire chain without finding anything in the pantry
        return false;
    }

    /**
     * Overloaded convenience method that accepts substitutions as a 2D array,
     * pantry as a String array, and recipes in a simpler format.
     *
     * @param substitutions 2D array of substitution pairs
     * @param pantry        Array of pantry ingredients
     * @param recipeNames   Array of recipe names
     * @param recipeIngredients 2D array where recipeIngredients[i] are the ingredients for recipeNames[i]
     * @return Lexicographically sorted list of makeable recipe names
     *
     * Time complexity: O(S + P + R * I * C)
     * Space complexity: O(S + P)
     */
    public List<String> findMakeableRecipes(
            String[][] substitutions,
            String[] pantry,
            String[] recipeNames,
            String[][] recipeIngredients) {

        // Convert substitutions array to List<List<String>>
        List<List<String>> subList = new ArrayList<>();
        for (String[] pair : substitutions) {
            subList.add(Arrays.asList(pair));
        }

        // Convert pantry array to List<String>
        List<String> pantryList = Arrays.asList(pantry);

        // Convert recipes to List<List<Object>>
        List<List<Object>> recipeList = new ArrayList<>();
        for (int i = 0; i < recipeNames.length; i++) {
            List<Object> recipe = new ArrayList<>();
            recipe.add(recipeNames[i]);
            recipe.add(Arrays.asList(recipeIngredients[i]));
            recipeList.add(recipe);
        }

        return findMakeableRecipes(subList, pantryList, recipeList);
    }

    /**
     * Main method demonstrating the solution with the provided examples.
     *
     * @param args Command-line arguments (not used)
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // ===================================================================
        // Example 1:
        // substitutions = [["butter", "margarine"], ["margarine", "coconut-oil"], ["milk", "oat-milk"]]
        // pantry = ["coconut-oil", "oat-milk", "flour", "sugar"]
        // recipes = [["cake", ["butter", "milk", "flour", "sugar"]],
        //            ["bread", ["butter", "flour"]],
        //            ["cookies", ["butter", "eggs"]]]
        // Expected Output: ["bread", "cake"]
        //
        // Trace:
        // - butter chain: butter(not in pantry) → margarine(not in pantry) → coconut-oil(IN pantry) ✓
        // - milk chain: milk(not in pantry) → oat-milk(IN pantry) ✓
        // - flour: flour(IN pantry) ✓
        // - sugar: sugar(IN pantry) ✓
        // - eggs: eggs(not in pantry) → no substitute → FAIL ✗
        //
        // cake: butter✓, milk✓, flour✓, sugar✓ → CAN MAKE
        // bread: butter✓, flour✓ → CAN MAKE
        // cookies: butter✓, eggs✗ → CANNOT MAKE
        // Sorted: ["bread", "cake"] ✓
        // ===================================================================
        System.out.println("=== Example 1 ===");

        String[][] substitutions1 = {
            {"butter", "margarine"},
            {"margarine", "coconut-oil"},
            {"milk", "oat-milk"}
        };
        String[] pantry1 = {"coconut-oil", "oat-milk", "flour", "sugar"};
        String[] recipeNames1 = {"cake", "bread", "cookies"};
        String[][] recipeIngredients1 = {
            {"butter", "milk", "flour", "sugar"},
            {"butter", "flour"},
            {"butter", "eggs"}
        };

        List<String> result1 = solution.findMakeableRecipes(
            substitutions1, pantry1, recipeNames1, recipeIngredients1);

        System.out.println("Input substitutions: butter→margarine, margarine→coconut-oil, milk→oat-milk");
        System.out.println("Pantry: [coconut-oil, oat-milk, flour, sugar]");
        System.out.println("Recipes: cake(butter,milk,flour,sugar), bread(butter,flour), cookies(butter,eggs)");
        System.out.println("Output: " + result1);
        System.out.println("Expected: [bread, cake]");
        System.out.println("Correct: " + result1.equals(Arrays.asList("bread", "cake")));
        System.out.println();

        // ===================================================================
        // Example 2:
        // substitutions = [["eggs", "flax-egg"], ["butter", "vegan-butter"]]
        // pantry = ["flax-egg", "vegan-butter", "flour", "sugar"]
        // recipes = [["muffins", ["eggs", "butter", "flour", "sugar"]],
        //            ["pancakes", ["eggs", "milk"]]]
        // Expected Output: ["muffins"]
        //
        // Trace:
        // - eggs chain: eggs(not in pantry) → flax-egg(IN pantry) ✓
        // - butter chain: butter(not in pantry) → vegan-butter(IN pantry) ✓
        // - flour: flour(IN pantry) ✓
        // - sugar: sugar(IN pantry) ✓
        // - milk: milk(not in pantry) → no substitute → FAIL ✗
        //
        // muffins: eggs✓, butter✓, flour✓, sugar✓ → CAN MAKE
        // pancakes: eggs✓, milk✗ → CANNOT MAKE
        // Sorted: ["muffins"] ✓
        // ===================================================================
        System.out.println("=== Example 2 ===");

        String[][] substitutions2 = {
            {"eggs", "flax-egg"},
            {"butter", "vegan-butter"}
        };
        String[] pantry2 = {"flax-egg", "vegan-butter", "flour", "sugar"};
        String[] recipeNames2 = {"muffins", "pancakes"};
        String[][] recipeIngredients2 = {
            {"eggs", "butter", "flour", "sugar"},
            {"eggs", "milk"}
        };

        List<String> result2 = solution.findM