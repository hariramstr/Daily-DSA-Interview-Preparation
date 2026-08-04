/*
Title: Count Equivalent Ingredient Lists by Frequency
Difficulty: Medium
Topic: Hashing

Problem Description:
A meal delivery company stores each recipe as a list of ingredient names. Two recipes are considered equivalent if they use exactly the same multiset of ingredients, ignoring the order of the ingredients but not their frequencies. For example, ["egg", "milk", "egg", "flour"] is equivalent to ["flour", "egg", "milk", "egg"], but it is not equivalent to ["egg", "milk", "flour"] because one copy of "egg" is missing.

You are given an array recipes where recipes[i] is a non-empty array of lowercase strings representing the ingredients of the i-th recipe. Return the number of unordered pairs of indices (i, j) such that 0 <= i < j < recipes.length and recipes[i] is equivalent to recipes[j].

Your solution should be efficient for large inputs. A brute-force comparison of every pair of recipes will be too slow. Think about how to build a canonical signature for each recipe so that equivalent recipes produce the same key and non-equivalent recipes produce different keys.

Constraints:
- 1 <= recipes.length <= 100000
- 1 <= recipes[i].length <= 20
- 1 <= ingredients[i][k].length <= 20
- Each ingredient consists only of lowercase English letters
- The total number of ingredient strings across all recipes does not exceed 300000

Example 1:
Input: recipes = [["egg","milk","egg"],["milk","egg","egg"],["egg","milk"],["flour"],["flour"]]
Output: 2
Explanation: Recipes 0 and 1 are equivalent. Recipes 3 and 4 are equivalent. No other pair matches, so the answer is 2.

Example 2:
Input: recipes = [["tomato","basil","tomato"],["basil","tomato","tomato"],["tomato","basil"],["cheese","tomato"],["tomato","cheese"]]
Output: 2
Explanation: The first two recipes form one equivalent pair. The last two recipes form another equivalent pair because order does not matter. The third recipe is different because the frequency of "tomato" is smaller.
*/

using System;
using System.Collections.Generic;
using System.Text;

public class Solution
{
    /*
    Time Complexity:
    Let R be the number of recipes.
    Let T be the total number of ingredient strings across all recipes.
    Let K be the maximum number of ingredients in a single recipe (K <= 20).

    For each recipe:
    - We count ingredient frequencies in O(recipe length).
    - We sort the distinct ingredient names inside that recipe.
      Since each recipe has at most 20 ingredients, this is very small.
    Overall complexity is effectively O(T * log K) in the worst case,
    which is efficient here because K <= 20.

    Space Complexity:
    - O(U) for the global dictionary of signatures, where U is the number of distinct recipe signatures.
    - O(K) temporary space per recipe for the frequency map and sorted keys.
    */
    public long CountEquivalentRecipes(string[][] recipes)
    {
        // This dictionary stores:
        // key   = canonical signature of a recipe
        // value = how many times we have already seen that exact signature
        //
        // Why do we need this?
        // If the current recipe has a signature we have seen c times before,
        // then it forms exactly c new unordered pairs with those previous recipes.
        //
        // Example:
        // signatures seen so far for "egg x2, milk x1" = 2
        // current recipe has same signature
        // then current recipe pairs with both previous ones, adding 2 pairs
        var signatureCount = new Dictionary<string, long>();

        // We use long because the number of pairs can be large.
        // In the worst case, if all recipes are equivalent, the answer is:
        // R * (R - 1) / 2
        // With R up to 100000, this exceeds int.
        long pairs = 0;

        // Process each recipe one by one.
        foreach (var recipe in recipes)
        {
            // Step 1: Count how many times each ingredient appears in this recipe.
            //
            // Why is this necessary?
            // Because equivalence depends on the multiset of ingredients:
            // - order does NOT matter
            // - frequency DOES matter
            //
            // So ["egg","milk","egg"] should become something like:
            // egg -> 2
            // milk -> 1
            //
            // A dictionary is a natural choice for frequency counting.
            var frequency = new Dictionary<string, int>();

            foreach (var ingredient in recipe)
            {
                if (frequency.TryGetValue(ingredient, out int currentCount))
                {
                    frequency[ingredient] = currentCount + 1;
                }
                else
                {
                    frequency[ingredient] = 1;
                }
            }

            // Step 2: Build a canonical signature string for this recipe.
            //
            // Why do we need a canonical signature?
            // Two equivalent recipes may list ingredients in different orders.
            // We need a single normalized representation so that:
            // - equivalent recipes produce exactly the same key
            // - non-equivalent recipes produce different keys
            //
            // To do that:
            // 1. Take all distinct ingredient names
            // 2. Sort them alphabetically
            // 3. Append "ingredient#count" for each one in sorted order
            //
            // Example:
            // ["milk","egg","egg"] -> frequency map:
            //   milk -> 1
            //   egg  -> 2
            //
            // sorted keys: ["egg", "milk"]
            // signature: "egg#2|milk#1|"
            //
            // This will match the signature from ["egg","milk","egg"] exactly.
            var ingredients = new List<string>(frequency.Keys);
            ingredients.Sort(StringComparer.Ordinal);

            // StringBuilder is used because we are constructing a string piece by piece.
            // This is more efficient and clearer than repeated string concatenation.
            var signatureBuilder = new StringBuilder();

            foreach (var ingredient in ingredients)
            {
                signatureBuilder.Append(ingredient);
                signatureBuilder.Append('#');
                signatureBuilder.Append(frequency[ingredient]);
                signatureBuilder.Append('|');
            }

            string signature = signatureBuilder.ToString();

            // Step 3: Use the signature to count new pairs.
            //
            // If we have already seen this signature before, then every previous recipe
            // with the same signature forms a valid pair with the current recipe.
            //
            // Suppose signatureCount[signature] = c
            // Then current recipe adds c new pairs.
            if (signatureCount.TryGetValue(signature, out long seen))
            {
                pairs += seen;
                signatureCount[signature] = seen + 1;
            }
            else
            {
                signatureCount[signature] = 1;
            }
        }

        // After processing all recipes, 'pairs' contains the total number
        // of unordered equivalent pairs.
        return pairs;
    }
}

// Demo code

var solution = new Solution();

// Example 1:
// Recipes 0 and 1 are equivalent: ["egg","milk","egg"] and ["milk","egg","egg"]
// Recipes 3 and 4 are equivalent: ["flour"] and ["flour"]
// Total = 2
string[][] recipes1 =
{
    new[] { "egg", "milk", "egg" },
    new[] { "milk", "egg", "egg" },
    new[] { "egg", "milk" },
    new[] { "flour" },
    new[] { "flour" }
};

long result1 = solution.CountEquivalentRecipes(recipes1);
Console.WriteLine(result1);

// Example 2:
// Recipes 0 and 1 are equivalent: ["tomato","basil","tomato"] and ["basil","tomato","tomato"]
// Recipes 3 and 4 are equivalent: ["cheese","tomato"] and ["tomato","cheese"]
// Recipe 2 is different because tomato appears only once there.
// Total = 2
string[][] recipes2 =
{
    new[] { "tomato", "basil", "tomato" },
    new[] { "basil", "tomato", "tomato" },
    new[] { "tomato", "basil" },
    new[] { "cheese", "tomato" },
    new[] { "tomato", "cheese" }
};

long result2 = solution.CountEquivalentRecipes(recipes2);
Console.WriteLine(result2);