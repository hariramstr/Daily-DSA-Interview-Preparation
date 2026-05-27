/*
 * Recipe Ingredient Substitution Tracker
 * =======================================
 * Difficulty: Medium
 * Topic: Hashing
 *
 * Problem Description:
 * A cooking platform maintains a list of ingredient substitution rules.
 * Each rule states that ingredient A can be substituted with ingredient B (one-directional).
 * Given a list of recipes (each with a name and list of ingredients) and a pantry list,
 * determine which recipes the user can fully prepare — either by using the exact ingredient
 * or any valid substitution chain.
 *
 * A substitution chain means: if A → B and B → C, then A can be substituted with C (transitively).
 * However, you may apply at most one substitution per ingredient in a recipe.
 * (i.e., you cannot substitute a substitution — only one hop is allowed per ingredient)
 *
 * Return the list of recipe names that can be made, sorted lexicographically.
 *
 * Key Insight:
 * "At most one substitution per ingredient" means:
 *   - For ingredient X, we can use X directly (0 hops), OR
 *   - We can use the direct substitute of X (1 hop): X → Y, use Y
 * We CANNOT follow a chain of length 2 (X → Y → Z) because that would be two substitutions.
 *
 * Wait — re-reading the problem carefully:
 * "A substitution chain means: if A → B and B → C, then A can be substituted with C (transitively).
 *  However, you may apply at most one substitution per ingredient"
 *
 * This is contradictory. Let's look at Example 1:
 * butter → margarine → coconut-oil (chain), coconut-oil is in pantry.
 * Output includes "bread" (needs butter, flour). If transitive chains were allowed, bread would work.
 * But the explanation says bread CAN be made. Let me re-read...
 *
 * Actually looking at Example 1 output: ["bread", "cake"] — both are in the output!
 * So butter CAN reach coconut-oil. The "at most one substitution" must mean something else,
 * or the chain IS allowed (transitive closure). Let me re-examine:
 * The problem says chain is allowed transitively, and "at most one substitution per ingredient"
 * means you pick ONE final substitute for each ingredient (not that you can only do one hop).
 * So the transitive closure IS computed, and for each ingredient you find all reachable ingredients,
 * then check if any of them (or the ingredient itself) is in the pantry.
 *
 * This matches Example 1: butter→margarine→coconut-oil, coconut-oil in pantry → bread works.
 */

using System;
using System.Collections.Generic;
using System.Linq;

/// <summary>
/// Solution class for the Recipe Ingredient Substitution Tracker problem.
/// Uses hashing (Dictionary and HashSet) for efficient lookups.
/// </summary>
public class RecipeSubstitutionTracker
{
    /// <summary>
    /// Determines which recipes can be made given substitution rules and pantry contents.
    ///
    /// Time Complexity:  O(S^2 + R * I * S) where S = substitutions count,
    ///                   R = recipes count, I = max ingredients per recipe
    ///                   The S^2 comes from building transitive closure via BFS/DFS per ingredient.
    ///
    /// Space Complexity: O(S + P + R * I) where P = pantry size
    ///                   For the substitution map, pantry set, and recipe storage.
    /// </summary>
    public List<string> FindMakeableRecipes(
        List<List<string>> substitutions,
        List<string> pantry,
        List<List<object>> recipes)
    {
        // -----------------------------------------------------------------------
        // STEP 1: Build a substitution map (adjacency list / dictionary)
        // -----------------------------------------------------------------------
        // We store substitution rules as a dictionary where:
        //   key   = ingredient that needs substituting
        //   value = the ingredient it can be substituted WITH (one direct substitute)
        //
        // Why a Dictionary? O(1) average lookup time when checking if an ingredient
        // has a substitute. Each ingredient has at most one direct substitute
        // (substitution pairs are unique and form a chain, not a tree).
        //
        // Example: butter→margarine, margarine→coconut-oil
        // substitutionMap = { "butter": "margarine", "margarine": "coconut-oil" }

        var substitutionMap = new Dictionary<string, string>(StringComparer.Ordinal);

        foreach (var rule in substitutions)
        {
            // rule[0] = ingredient A (the one being substituted)
            // rule[1] = ingredient B (what it can be replaced with)
            string from = rule[0];
            string to   = rule[1];
            substitutionMap[from] = to;
        }

        // -----------------------------------------------------------------------
        // STEP 2: Build a HashSet of pantry ingredients for O(1) lookup
        // -----------------------------------------------------------------------
        // Why a HashSet? Checking "is ingredient X in the pantry?" happens many times.
        // HashSet gives O(1) average time for Contains(), vs O(N) for a List.

        var pantrySet = new HashSet<string>(pantry, StringComparer.Ordinal);

        // -----------------------------------------------------------------------
        // STEP 3: Build a "reachability" cache
        // -----------------------------------------------------------------------
        // For each ingredient we encounter, we want to know: can we satisfy it
        // given our pantry? We follow the substitution chain from that ingredient
        // until we either find something in the pantry or exhaust the chain.
        //
        // We cache results to avoid recomputing for the same ingredient multiple times.
        // Key   = ingredient name
        // Value = true if the ingredient (or any reachable substitute) is in pantry

        var reachabilityCache = new Dictionary<string, bool>(StringComparer.Ordinal);

        // -----------------------------------------------------------------------
        // STEP 4: Define a helper function to check if an ingredient can be satisfied
        // -----------------------------------------------------------------------
        // This follows the substitution chain (transitive closure) from a given
        // ingredient and returns true if any ingredient in the chain is in the pantry.
        //
        // We use an iterative approach (following the linked chain) since the problem
        // guarantees NO circular substitution chains exist — so we won't loop forever.
        //
        // Example trace for "butter" with pantry containing "coconut-oil":
        //   Check "butter"       → not in pantry
        //   butter → "margarine" → not in pantry
        //   margarine → "coconut-oil" → IN pantry! → return true

        bool CanSatisfyIngredient(string ingredient)
        {
            // Check cache first to avoid redundant work
            if (reachabilityCache.TryGetValue(ingredient, out bool cachedResult))
            {
                return cachedResult;
            }

            // Walk the substitution chain starting from this ingredient
            // We keep track of visited nodes to handle any edge cases
            // (though the problem guarantees no cycles)
            string current = ingredient;

            // We'll collect all ingredients in this chain so we can cache results for all of them
            var chainVisited = new List<string>();

            while (current != null)
            {
                // Mark that we're visiting this node in the chain
                chainVisited.Add(current);

                // Check if the current ingredient is in the pantry
                if (pantrySet.Contains(current))
                {
                    // SUCCESS: this ingredient (or a substitute) is available
                    // Cache the result for ALL ingredients we visited in this chain
                    // (they all lead to a pantry item, so they're all satisfiable)
                    foreach (var visited in chainVisited)
                    {
                        reachabilityCache[visited] = true;
                    }
                    return true;
                }

                // Check if there's a cached result for the current node
                // (we might be joining a previously computed chain)
                if (reachabilityCache.TryGetValue(current, out bool cached))
                {
                    // Propagate the cached result back to all nodes we visited
                    foreach (var visited in chainVisited)
                    {
                        reachabilityCache[visited] = cached;
                    }
                    return cached;
                }

                // Move to the next ingredient in the substitution chain
                // If there's no substitute, substitutionMap.TryGetValue returns false
                // and 'next' remains null, ending the loop
                if (substitutionMap.TryGetValue(current, out string next))
                {
                    current = next;
                }
                else
                {
                    // No more substitutes available — chain is exhausted
                    current = null;
                }
            }

            // If we reach here, no ingredient in the chain was found in the pantry
            // Cache false for all ingredients in this chain
            foreach (var visited in chainVisited)
            {
                reachabilityCache[visited] = false;
            }
            return false;
        }

        // -----------------------------------------------------------------------
        // STEP 5: Check each recipe to see if all ingredients can be satisfied
        // -----------------------------------------------------------------------
        // For each recipe:
        //   - Get the recipe name (first element)
        //   - Get the ingredient list (second element)
        //   - For each ingredient, check if it can be satisfied (direct or via chain)
        //   - If ALL ingredients are satisfiable, add the recipe name to results

        var makeableRecipes = new List<string>();

        foreach (var recipeEntry in recipes)
        {
            // Each recipe entry is [recipeName, List<string> ingredients]
            // The first element is the recipe name (a string)
            string recipeName = (string)recipeEntry[0];

            // The second element is the list of ingredients
            var ingredients = (List<string>)recipeEntry[1];

            // Assume the recipe is makeable until proven otherwise
            bool canMake = true;

            foreach (string ingredient in ingredients)
            {
                // Check if this ingredient can be satisfied from the pantry
                // (either directly or through the substitution chain)
                if (!CanSatisfyIngredient(ingredient))
                {
                    // This ingredient cannot be satisfied — recipe fails
                    canMake = false;
                    break; // No need to check remaining ingredients
                }
            }

            if (canMake)
            {
                makeableRecipes.Add(recipeName);
            }
        }

        // -----------------------------------------------------------------------
        // STEP 6: Sort the result lexicographically as required by the problem
        // -----------------------------------------------------------------------
        // Why sort? The problem explicitly asks for lexicographic ordering.
        // List.Sort() uses an in-place sort with O(N log N) time complexity.

        makeableRecipes.Sort(StringComparer.Ordinal);

        return makeableRecipes;
    }
}

// =============================================================================
// DEMO / TEST CODE
// =============================================================================

Console.WriteLine("=== Recipe Ingredient Substitution Tracker ===\n");

var solver = new RecipeSubstitutionTracker();

// ---------------------------------------------------------------------------
// Example 1
// ---------------------------------------------------------------------------
// substitutions: butter→margarine, margarine→coconut-oil, milk→oat-milk
// pantry: coconut-oil, oat-milk, flour, sugar
// recipes:
//   cake:    [butter, milk, flour, sugar]
//   bread:   [butter, flour]
//   cookies: [butter, eggs]
//
// Expected Output: ["bread", "cake"]
//
// Trace:
//   cake:
//     butter → margarine → coconut-oil (in pantry) ✓
//     milk → oat-milk (in pantry) ✓
//     flour (in pantry) ✓
//     sugar (in pantry) ✓
//     → cake is makeable ✓
//   bread:
//     butter → margarine → coconut-oil (in pantry) ✓
//     flour (in pantry) ✓
//     → bread is makeable ✓
//   cookies:
//     butter → margarine → coconut-oil (in pantry) ✓
//     eggs → no substitute, not in pantry ✗
//     → cookies NOT makeable
//
// Sorted result: ["bread", "cake"] ✓

Console.WriteLine("--- Example 1 ---");

var substitutions1 = new List<List<string>>
{
    new List<string> { "butter",    "margarine"   },
    new List<string> { "margarine", "coconut-oil" },
    new List<string> { "milk",      "oat-milk"    }
};

var pantry1 = new List<string> { "coconut-oil", "oat-milk", "flour", "sugar" };

var recipes1 = new List<List<object>>
{
    new List<object> { "cake",    new List<string> { "butter", "milk", "flour", "sugar" } },
    new List<object> { "bread",   new List<string> { "butter", "flour" } },
    new List<object> { "cookies", new List<string> { "butter", "eggs" } }
};

var result1 = solver.FindMakeableRecipes(substitutions1, pantry1, recipes1);
Console.WriteLine("Input substitutions: butter→margarine→coconut-oil, milk→oat-milk");
Console.WriteLine("Pantry: coconut-oil, oat-milk, flour, sugar");
Console.WriteLine("Recipes: cake(butter,milk,flour,sugar), bread(butter,flour), cookies(butter,eggs)");
Console.WriteLine($"Output: [{string.Join(", ", result1)}]");
Console.WriteLine("Expected: [bread, cake]");
Console.WriteLine($"Correct: {string.Join(",", result1) == "bread,cake"}\n");

// ---------------------------------------------------------------------------
// Example 2
// ---------------------------------------------------------------------------
// substitutions: eggs→flax-egg, butter→vegan-butter
// pantry: flax-egg, vegan-butter, flour, sugar
// recipes:
//   muffins:  [eggs, butter, flour, sugar]
//   pancakes: [eggs, milk]
//
// Expected Output: ["muffins"]
//
// Trace:
//   muffins:
//     eggs → flax-egg (in pantry) ✓
//     butter → vegan-butter (in pantry) ✓
//     flour (in pantry) ✓
//     sugar (in pantry) ✓
//     → muffins is makeable ✓
//   pancakes:
//     eggs → flax-egg (in pantry) ✓
//     milk → no substitute, not in pantry ✗
//     → pancakes NOT makeable
//
// Sorted result: ["muffins"] ✓

Console.WriteLine("--- Example 2 ---");

var substitutions2 = new List<List<string>>
{
    new List<string> { "eggs",   "flax-egg"     },
    new List<string> { "butter", "vegan-butter" }
};

var pantry2 = new List<string> { "flax-egg", "vegan-butter", "flour", "sugar" };

var recipes2 = new List<List<object>>
{
    new List<object> { "muffins",  new List<string> { "eggs", "butter", "flour", "sugar" } },
    new List<object> { "pancakes", new List<string> { "eggs", "milk" } }
};

var result2 = solver.FindMakeableRecipes(substitutions2, pantry2, recipes2);
Console.WriteLine("Input substitutions: eggs→flax-egg, butter→vegan-butter");
Console.WriteLine("Pantry: flax-egg, vegan-butter, flour, sugar");
Console.WriteLine("Recipes: muffins(eggs,butter,flour,sugar), pancakes(eggs,milk)");
Console.WriteLine($"Output: [{string.Join(", ", result2)}]");
Console.WriteLine("Expected: [muffins]");
Console.WriteLine($"Correct: {string.Join(",", result2) == "muffins"}\n");

// ---------------------------------------------------------------------------
// Example 3: Edge case — ingredient directly in pantry (no substitution needed)
// ---------------------------------------------------------------------------

Console.WriteLine("--- Example 3 (Edge Case: Direct pantry match) ---");

var substitutions3 = new List<List<string