import java.util.*;

/*
Problem Title: Count Equivalent Ingredient Lists by Frequency

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

public class Solution {

    /**
     * Counts how many unordered pairs of recipes are equivalent.
     *
     * The key idea is:
     * 1. Convert each recipe into a canonical signature that represents the multiset
     *    of ingredients.
     * 2. Two recipes are equivalent if and only if their signatures are identical.
     * 3. Count how many times each signature appears.
     * 4. If a signature appears k times, then it contributes k * (k - 1) / 2 pairs.
     *
     * We build the signature by:
     * - Counting frequency of each ingredient in the recipe.
     * - Sorting ingredient names so the order becomes deterministic.
     * - Concatenating ingredient name and frequency into a single string key.
     *
     * @param recipes a 2D array where recipes[i] contains the ingredient list of the i-th recipe
     * @return the number of unordered equivalent recipe pairs
     * Time complexity: O(T + sum over recipes of m log m), where T is the total number of ingredient strings
     * and m is the number of distinct ingredients in a recipe. Since each recipe length is at most 20,
     * this is efficient for the given constraints.
     * Space complexity: O(T) in the worst case for storing signatures and frequency maps
     */
    public long countEquivalentRecipes(String[][] recipes) {
        // This map stores:
        // key   = canonical signature of a recipe
        // value = how many recipes seen so far have exactly this signature
        Map<String, Integer> signatureCount = new HashMap<>();

        // This will accumulate the total number of equivalent pairs.
        // We use long because the number of pairs can be large:
        // for example, if all 100000 recipes are equivalent,
        // the answer is 100000 * 99999 / 2, which does not fit in int.
        long pairs = 0L;

        // Process each recipe one by one.
        for (String[] recipe : recipes) {
            // Build a canonical representation for the current recipe.
            String signature = buildSignature(recipe);

            // If we have already seen this signature 'seen' times,
            // then the current recipe forms exactly 'seen' new pairs:
            // one pair with each previously seen equivalent recipe.
            int seen = signatureCount.getOrDefault(signature, 0);
            pairs += seen;

            // Record that we have now seen one more recipe with this signature.
            signatureCount.put(signature, seen + 1);
        }

        return pairs;
    }

    /**
     * Builds a canonical signature for one recipe.
     *
     * Two recipes should produce the same signature if and only if they contain
     * exactly the same ingredients with exactly the same frequencies.
     *
     * Example:
     * ["egg", "milk", "egg"] -> "egg#2|milk#1|"
     * ["milk", "egg", "egg"] -> "egg#2|milk#1|"
     *
     * Why this works:
     * - We count frequencies, so duplicates are preserved.
     * - We sort ingredient names, so original order does not matter.
     * - We include separators, so different combinations cannot accidentally collide.
     *
     * @param recipe the ingredient list for a single recipe
     * @return a canonical string signature representing the ingredient multiset
     * Time complexity: O(n + d log d), where n is recipe length and d is number of distinct ingredients
     * Space complexity: O(d), where d is number of distinct ingredients
     */
    public String buildSignature(String[] recipe) {
        // First, count how many times each ingredient appears in this recipe.
        Map<String, Integer> frequency = new HashMap<>();
        for (String ingredient : recipe) {
            frequency.put(ingredient, frequency.getOrDefault(ingredient, 0) + 1);
        }

        // To make the signature independent of original order,
        // we sort the distinct ingredient names.
        List<String> ingredients = new ArrayList<>(frequency.keySet());
        Collections.sort(ingredients);

        // Build the final signature in a deterministic format.
        // We use separators to avoid ambiguity.
        StringBuilder signature = new StringBuilder();
        for (String ingredient : ingredients) {
            signature.append(ingredient)
                     .append('#')
                     .append(frequency.get(ingredient))
                     .append('|');
        }

        return signature.toString();
    }

    /**
     * Utility method to print a 2D string array in a readable format.
     *
     * @param recipes the 2D array of recipes to print
     * @return a readable string representation of the recipes
     * Time complexity: O(T), where T is the total number of strings printed
     * Space complexity: O(T) for the constructed output string
     */
    public String recipesToString(String[][] recipes) {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < recipes.length; i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(Arrays.toString(recipes[i]));
        }
        sb.append("]");
        return sb.toString();
    }

    /**
     * Demonstrates the solution on the sample inputs from the problem statement.
     *
     * This method also prints the expected outputs so it is easy to verify correctness.
     *
     * @param args command-line arguments (not used)
     * @return nothing
     * Time complexity: O(T + sum over recipes of m log m) for the demonstrated inputs
     * Space complexity: O(T) for maps and printed strings
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Example 1
        String[][] recipes1 = {
            {"egg", "milk", "egg"},
            {"milk", "egg", "egg"},
            {"egg", "milk"},
            {"flour"},
            {"flour"}
        };

        long result1 = solution.countEquivalentRecipes(recipes1);
        System.out.println("Example 1 Input: " + solution.recipesToString(recipes1));
        System.out.println("Example 1 Output: " + result1);
        System.out.println("Example 1 Expected: 2");
        System.out.println();

        // Example 2
        String[][] recipes2 = {
            {"tomato", "basil", "tomato"},
            {"basil", "tomato", "tomato"},
            {"tomato", "basil"},
            {"cheese", "tomato"},
            {"tomato", "cheese"}
        };

        long result2 = solution.countEquivalentRecipes(recipes2);
        System.out.println("Example 2 Input: " + solution.recipesToString(recipes2));
        System.out.println("Example 2 Output: " + result2);
        System.out.println("Example 2 Expected: 2");
        System.out.println();

        // Additional quick sanity check
        String[][] recipes3 = {
            {"salt"},
            {"pepper"},
            {"salt"},
            {"salt", "pepper"},
            {"pepper", "salt"},
            {"salt", "salt"}
        };

        long result3 = solution.countEquivalentRecipes(recipes3);
        System.out.println("Additional Test Input: " + solution.recipesToString(recipes3));
        System.out.println("Additional Test Output: " + result3);
        System.out.println("Additional Test Expected: 2");
    }
}