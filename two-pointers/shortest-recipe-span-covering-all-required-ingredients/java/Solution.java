import java.util.*;

/*
 * Title: Shortest Recipe Span Covering All Required Ingredients
 * Difficulty: Medium
 * Topic: Two Pointers
 *
 * Problem Description:
 * A cooking platform stores each recipe as an ordered list of ingredient tokens,
 * where the same ingredient may appear multiple times if it is used in different steps.
 * You are given an array ingredients representing this token stream for one recipe,
 * and another array required representing the ingredients that must all appear in a
 * highlighted excerpt. Your task is to return the length of the shortest contiguous
 * span of ingredients that contains every ingredient in required at least once.
 * If no such span exists, return -1.
 *
 * This problem models finding the smallest meaningful excerpt of a recipe that still
 * includes all mandatory ingredients. The order inside the span does not matter,
 * but the span must be contiguous in the original recipe. Duplicate values in
 * ingredients are allowed. Assume required contains distinct ingredient names.
 *
 * Constraints:
 * - 1 <= ingredients.length <= 200000
 * - 1 <= required.length <= 200000
 * - 1 <= required.length <= ingredients.length
 * - ingredients[i] and required[i] are non-empty strings of lowercase English letters
 * - required contains distinct strings
 *
 * Example 1:
 * Input: ingredients = ["salt","oil","garlic","onion","salt","tomato","basil"],
 *        required = ["garlic","salt","tomato"]
 * Output: 4
 * Explanation: The shortest valid span is ["garlic","onion","salt","tomato"], which has length 4.
 *
 * Example 2:
 * Input: ingredients = ["flour","egg","milk","butter","egg"],
 *        required = ["egg","yeast"]
 * Output: -1
 * Explanation: "yeast" never appears, so no contiguous span can satisfy the requirement.
 */

public class Solution {

    /**
     * Finds the length of the shortest contiguous subarray of ingredients that contains
     * every required ingredient at least once.
     *
     * The method uses the classic sliding window / two pointers technique:
     * 1. Expand the right pointer to include more ingredients.
     * 2. Track how many required ingredients are currently satisfied inside the window.
     * 3. Once all required ingredients are present, try shrinking from the left
     *    to make the window as short as possible while still valid.
     *
     * @param ingredients the full ordered ingredient token stream of the recipe
     * @param required the distinct ingredient names that must all appear in the chosen span
     * @return the length of the shortest valid contiguous span, or -1 if no such span exists
     *
     * Time complexity: O(n + m), where n = ingredients.length and m = required.length.
     * Each pointer moves at most n times, and building the required set takes O(m).
     *
     * Space complexity: O(m), because we store only required ingredients in hash-based structures.
     */
    public int shortestRecipeSpan(String[] ingredients, String[] required) {
        // Defensive handling for unexpected null inputs.
        // The problem constraints guarantee valid arrays, but this makes the method safer.
        if (ingredients == null || required == null || ingredients.length == 0 || required.length == 0) {
            return -1;
        }

        // Store all required ingredients in a set for O(1) average membership checks.
        Set<String> requiredSet = new HashSet<>();
        for (String item : required) {
            requiredSet.add(item);
        }

        // This map stores the count of each required ingredient currently inside the window.
        // We do NOT need to track non-required ingredients, because they do not affect validity.
        Map<String, Integer> windowCounts = new HashMap<>();

        // Number of distinct required ingredients currently present in the window
        // with count >= 1.
        int formed = 0;

        // Total number of distinct required ingredients that must be formed.
        int need = requiredSet.size();

        // Left boundary of the sliding window.
        int left = 0;

        // Best answer found so far. Start with a very large number.
        int minLength = Integer.MAX_VALUE;

        // Move the right boundary from left to right across the array.
        for (int right = 0; right < ingredients.length; right++) {
            String current = ingredients[right];

            // Only required ingredients matter for satisfying the condition.
            if (requiredSet.contains(current)) {
                // Add the current ingredient to the window count.
                int newCount = windowCounts.getOrDefault(current, 0) + 1;
                windowCounts.put(current, newCount);

                // If this ingredient count becomes 1, then this required ingredient
                // is now satisfied in the current window.
                if (newCount == 1) {
                    formed++;
                }
            }

            // If formed == need, the current window contains every required ingredient.
            // Now try to shrink it from the left to get the smallest valid window
            // ending at the current right position.
            while (formed == need) {
                // Current window length is right - left + 1 because both ends are inclusive.
                int currentLength = right - left + 1;
                if (currentLength < minLength) {
                    minLength = currentLength;
                }

                String leftItem = ingredients[left];

                // We are about to remove ingredients[left] from the window by moving left forward.
                // If it is a required ingredient, update its count.
                if (requiredSet.contains(leftItem)) {
                    int updatedCount = windowCounts.get(leftItem) - 1;
                    windowCounts.put(leftItem, updatedCount);

                    // If the count drops to 0, then the window is no longer valid,
                    // because one required ingredient disappeared completely.
                    if (updatedCount == 0) {
                        formed--;
                    }
                }

                // Actually shrink the window from the left.
                left++;
            }
        }

        // If minLength was never updated, no valid window exists.
        return minLength == Integer.MAX_VALUE ? -1 : minLength;
    }

    /**
     * A helper method that prints an input case and the computed answer.
     *
     * @param ingredients the recipe ingredient stream
     * @param required the required ingredients that must all appear
     * @return the computed shortest span length
     *
     * Time complexity: O(n + m), delegated to shortestRecipeSpan.
     * Space complexity: O(m), delegated to shortestRecipeSpan.
     */
    public int demonstrateCase(String[] ingredients, String[] required) {
        int result = shortestRecipeSpan(ingredients, required);
        System.out.println("Ingredients: " + Arrays.toString(ingredients));
        System.out.println("Required: " + Arrays.toString(required));
        System.out.println("Shortest span length: " + result);
        System.out.println();
        return result;
    }

    /**
     * Demonstrates the solution on the sample inputs from the problem statement.
     *
     * Expected outputs:
     * - Example 1 -> 4
     * - Example 2 -> -1
     *
     * @param args command-line arguments (not used)
     * @return nothing
     *
     * Time complexity: O(total input size of demonstrated examples).
     * Space complexity: O(size of required arrays in each demonstration).
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        String[] ingredients1 = {"salt", "oil", "garlic", "onion", "salt", "tomato", "basil"};
        String[] required1 = {"garlic", "salt", "tomato"};
        solution.demonstrateCase(ingredients1, required1);

        String[] ingredients2 = {"flour", "egg", "milk", "butter", "egg"};
        String[] required2 = {"egg", "yeast"};
        solution.demonstrateCase(ingredients2, required2);

        // Additional small sanity check:
        // The shortest span containing "a", "b", and "c" is ["b","c","a"] with length 3.
        String[] ingredients3 = {"x", "a", "b", "c", "a", "y"};
        String[] required3 = {"a", "b", "c"};
        solution.demonstrateCase(ingredients3, required3);
    }
}