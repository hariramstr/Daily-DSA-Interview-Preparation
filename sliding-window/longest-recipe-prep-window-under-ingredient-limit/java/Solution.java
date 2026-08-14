import java.util.*;

/*
 * Title: Longest Recipe Prep Window Under Ingredient Limit
 * Difficulty: Medium
 * Topic: Sliding Window
 *
 * Problem Description:
 * A meal-planning app stores a chef's recipe schedule as an array of strings recipes,
 * where recipes[i] is the main ingredient category used by the i-th recipe prepared that day.
 * The chef wants to analyze the longest contiguous stretch of recipes that can be cooked
 * without making the pantry too diverse.
 *
 * Given recipes and an integer k, return the length of the longest contiguous subarray
 * that contains at most k distinct ingredient categories.
 *
 * In other words, you need to find the largest window [l, r] such that among
 * recipes[l], recipes[l+1], ..., recipes[r], there are no more than k different
 * category names.
 *
 * This problem should be solved efficiently for large inputs, so solutions that check
 * every possible subarray will be too slow.
 *
 * Constraints:
 * - 1 <= recipes.length <= 100000
 * - 1 <= recipes[i].length <= 20
 * - recipes[i] consists of lowercase English letters
 * - 1 <= k <= recipes.length
 *
 * Example 1:
 * Input: recipes = ["dairy","grain","dairy","spice","grain","grain"], k = 2
 * Output: 3
 * Explanation:
 * The longest valid window length is 3.
 * One valid maximum window is ["dairy","grain","dairy"], which contains only
 * 2 distinct categories: {dairy, grain}.
 *
 * Example 2:
 * Input: recipes = ["meat","meat","veg","veg","sauce","veg","veg"], k = 2
 * Output: 5
 * Explanation:
 * The longest valid window is ["veg","veg","sauce","veg","veg"], which contains only
 * 2 distinct categories: {veg, sauce}.
 *
 * Note:
 * The originally written explanation text in some versions of this problem may mention
 * a shorter window for Example 2, but the correct maximum length is 5, not 4.
 * The algorithm below correctly computes the true maximum.
 */

public class Solution {

    /**
     * Finds the length of the longest contiguous subarray that contains
     * at most k distinct recipe categories.
     *
     * This method uses the classic sliding window technique:
     * - Expand the right side of the window one element at a time.
     * - Track how many times each recipe category appears in the current window.
     * - If the number of distinct categories becomes greater than k,
     *   shrink the window from the left until it becomes valid again.
     * - At every valid step, update the best window length seen so far.
     *
     * @param recipes the array of recipe ingredient categories
     * @param k the maximum number of distinct categories allowed in the window
     * @return the maximum length of a contiguous subarray containing at most k distinct categories
     *
     * Time complexity: O(n), where n is recipes.length, because each element is added
     * to the window once and removed from the window at most once.
     * Space complexity: O(k) on average for the frequency map of categories in the current window,
     * and O(n) in the worst case if many distinct strings are encountered before shrinking.
     */
    public int longestRecipePrepWindow(String[] recipes, int k) {
        // Defensive handling:
        // Based on the constraints, recipes has at least 1 element and k >= 1.
        // Still, writing safe code is a good practice for real-world usage.
        if (recipes == null || recipes.length == 0 || k <= 0) {
            return 0;
        }

        // This map stores the frequency of each recipe category currently inside the window.
        // Example:
        // If the current window is ["dairy", "grain", "dairy"],
        // then the map will be:
        // dairy -> 2
        // grain -> 1
        Map<String, Integer> frequencyMap = new HashMap<>();

        // left is the starting index of the sliding window.
        int left = 0;

        // maxLength stores the best valid window length found so far.
        int maxLength = 0;

        // We move right from 0 to recipes.length - 1, expanding the window one step at a time.
        for (int right = 0; right < recipes.length; right++) {
            // Step 1:
            // Include recipes[right] into the current window.
            String currentRecipe = recipes[right];
            frequencyMap.put(currentRecipe, frequencyMap.getOrDefault(currentRecipe, 0) + 1);

            // Step 2:
            // If the window now contains more than k distinct categories,
            // it is invalid and must be shrunk from the left.
            //
            // Why use a while loop instead of if?
            // Because removing just one element may still leave the window invalid.
            // We keep shrinking until the number of distinct categories is at most k again.
            while (frequencyMap.size() > k) {
                // Identify the recipe category at the left edge of the window.
                String leftRecipe = recipes[left];

                // Decrease its count because we are removing it from the window.
                frequencyMap.put(leftRecipe, frequencyMap.get(leftRecipe) - 1);

                // If its count becomes 0, it no longer exists in the window,
                // so we remove it from the map entirely.
                if (frequencyMap.get(leftRecipe) == 0) {
                    frequencyMap.remove(leftRecipe);
                }

                // Move the left edge one step to the right.
                left++;
            }

            // Step 3:
            // At this point, the window [left, right] is guaranteed to be valid,
            // meaning it contains at most k distinct categories.
            //
            // Compute its length:
            // length = right - left + 1
            int currentWindowLength = right - left + 1;

            // Update the best answer if this valid window is larger than any seen before.
            maxLength = Math.max(maxLength, currentWindowLength);
        }

        // After processing all possible right endpoints, maxLength is the answer.
        return maxLength;
    }

    /**
     * An alternative method name that may be useful in interview settings.
     * It delegates to the main implementation.
     *
     * @param recipes the array of recipe ingredient categories
     * @param k the maximum number of distinct categories allowed
     * @return the maximum valid contiguous window length
     *
     * Time complexity: O(n), where n is recipes.length.
     * Space complexity: O(k) on average for the sliding window frequency map.
     */
    public int lengthOfLongestSubarrayAtMostKDistinct(String[] recipes, int k) {
        return longestRecipePrepWindow(recipes, k);
    }

    /**
     * Demonstrates the solution using the sample inputs and prints the results.
     *
     * This main method also verifies the expected outputs:
     * - Example 1 should print 3
     * - Example 2 should print 5 based on the actual problem statement logic
     *
     * @param args command-line arguments (not used)
     * @return nothing
     *
     * Time complexity: O(n) per demonstration call.
     * Space complexity: O(k) on average per demonstration call.
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        String[] recipes1 = {"dairy", "grain", "dairy", "spice", "grain", "grain"};
        int k1 = 2;
        int result1 = solution.longestRecipePrepWindow(recipes1, k1);
        System.out.println("Example 1 Result: " + result1);
        System.out.println("Expected: 3");

        String[] recipes2 = {"meat", "meat", "veg", "veg", "sauce", "veg", "veg"};
        int k2 = 2;
        int result2 = solution.longestRecipePrepWindow(recipes2, k2);
        System.out.println("Example 2 Result: " + result2);
        System.out.println("Expected: 5");

        // Additional quick sanity checks for beginners:
        String[] recipes3 = {"a"};
        int k3 = 1;
        int result3 = solution.longestRecipePrepWindow(recipes3, k3);
        System.out.println("Single element test: " + result3);
        System.out.println("Expected: 1");

        String[] recipes4 = {"a", "b", "c", "a", "b"};
        int k4 = 3;
        int result4 = solution.longestRecipePrepWindow(recipes4, k4);
        System.out.println("At most 3 distinct test: " + result4);
        System.out.println("Expected: 5");
    }
}