import java.util.*;

/*
 * Title: Longest Recipe Video Segment With Limited Ingredient Repeats
 * Difficulty: Medium
 * Topic: Sliding Window
 *
 * Problem Description:
 * You are given an array ingredients where ingredients[i] is the ingredient name mentioned
 * at second i of a cooking video. The editor wants to extract one contiguous segment of the
 * video such that no ingredient is mentioned more than k times inside that segment.
 * Your task is to return the length of the longest valid segment.
 *
 * A segment is valid if, for every distinct ingredient appearing in it, its frequency within
 * the segment is at most k. The segment must be contiguous, so you may only choose a
 * continuous block of timestamps.
 *
 * This problem models a common interview pattern: maintaining counts inside a moving window
 * while expanding and shrinking the boundaries efficiently.
 *
 * Return an integer representing the maximum number of timestamps in any valid segment.
 *
 * Constraints:
 * - 1 <= ingredients.length <= 200000
 * - 1 <= ingredients[i].length <= 20
 * - ingredients[i] consists of lowercase English letters
 * - 1 <= k <= ingredients.length
 *
 * Example 1:
 * Input: ingredients = ["salt","pepper","salt","oil","salt","pepper"], k = 2
 * Output: 4
 * Explanation: One longest valid segment is ["pepper","salt","oil","salt"].
 *
 * Example 2:
 * Input: ingredients = ["egg","egg","milk","egg","milk","milk","flour"], k = 2
 * Output: 5
 * Explanation: A longest valid segment is ["egg","milk","egg","milk","flour"], which has
 * "egg" twice, "milk" twice, and "flour" once.
 */

public class Solution {

    /**
     * Finds the length of the longest contiguous segment such that no ingredient appears
     * more than k times inside that segment.
     *
     * The algorithm uses the classic sliding window technique:
     * 1. Expand the right boundary one element at a time.
     * 2. Track ingredient frequencies inside the current window using a hash map.
     * 3. If the newly added ingredient causes its count to exceed k, shrink the left boundary
     *    until the window becomes valid again.
     * 4. After each valid state, update the best window length found so far.
     *
     * Why this works:
     * - At any moment, the window [left, right] represents a contiguous segment.
     * - We only violate the rule when adding the new right-side ingredient makes its count > k.
     * - Shrinking from the left removes elements until that violation disappears.
     * - Because each index enters and leaves the window at most once, the total work is linear.
     *
     * @param ingredients the array where ingredients[i] is the ingredient mentioned at second i
     * @param k the maximum allowed frequency of any ingredient inside the chosen segment
     * @return the maximum length of a valid contiguous segment
     *
     * Time complexity: O(n), where n is ingredients.length, because each pointer moves at most n times.
     * Space complexity: O(m), where m is the number of distinct ingredients currently tracked in the map.
     */
    public int longestValidSegment(String[] ingredients, int k) {
        // Frequency map:
        // key   -> ingredient name
        // value -> how many times that ingredient appears in the current window
        Map<String, Integer> frequency = new HashMap<>();

        // Left boundary of the sliding window.
        int left = 0;

        // Best answer found so far.
        int maxLength = 0;

        // Move the right boundary from left to right across the entire array.
        for (int right = 0; right < ingredients.length; right++) {
            String currentIngredient = ingredients[right];

            // Step 1: Include the new ingredient at position 'right' into the window.
            frequency.put(currentIngredient, frequency.getOrDefault(currentIngredient, 0) + 1);

            // Step 2: If this ingredient now appears more than k times,
            // the window is invalid and must be shrunk from the left.
            //
            // Important observation:
            // Before adding ingredients[right], the window was valid.
            // Therefore, only the count of currentIngredient could have become invalid.
            while (frequency.get(currentIngredient) > k) {
                String leftIngredient = ingredients[left];

                // Remove one occurrence of the ingredient at the left boundary.
                frequency.put(leftIngredient, frequency.get(leftIngredient) - 1);

                // Optional cleanup:
                // If a count becomes zero, remove it from the map to keep the map tidy.
                if (frequency.get(leftIngredient) == 0) {
                    frequency.remove(leftIngredient);
                }

                // Move the left boundary rightward, effectively shrinking the window.
                left++;
            }

            // Step 3: At this point, the window [left, right] is valid.
            // Compute its length and update the best answer if needed.
            int currentWindowLength = right - left + 1;
            maxLength = Math.max(maxLength, currentWindowLength);
        }

        return maxLength;
    }

    /**
     * Helper method to print an example in a beginner-friendly format.
     *
     * @param ingredients the input ingredient array
     * @param k the maximum allowed frequency per ingredient
     * @return the computed longest valid segment length
     *
     * Time complexity: O(n), delegated to longestValidSegment.
     * Space complexity: O(m), delegated to longestValidSegment.
     */
    public int runExample(String[] ingredients, int k) {
        int result = longestValidSegment(ingredients, k);
        System.out.println("Ingredients: " + Arrays.toString(ingredients));
        System.out.println("k = " + k);
        System.out.println("Longest valid segment length = " + result);
        System.out.println();
        return result;
    }

    /**
     * Demonstrates the solution using the sample inputs from the problem statement
     * and a few additional sanity checks.
     *
     * @param args command-line arguments (not used)
     * @return nothing
     *
     * Time complexity: O(total input size of demonstrated examples).
     * Space complexity: O(m) for the largest example processed.
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Sample Example 1
        // ["salt","pepper","salt","oil","salt","pepper"], k = 2
        // Expected output: 4
        String[] ingredients1 = {"salt", "pepper", "salt", "oil", "salt", "pepper"};
        int result1 = solution.runExample(ingredients1, 2);
        System.out.println("Expected: 4, Actual: " + result1);
        System.out.println();

        // Sample Example 2
        // ["egg","egg","milk","egg","milk","milk","flour"], k = 2
        // Expected output: 5
        String[] ingredients2 = {"egg", "egg", "milk", "egg", "milk", "milk", "flour"};
        int result2 = solution.runExample(ingredients2, 2);
        System.out.println("Expected: 5, Actual: " + result2);
        System.out.println();

        // Additional sanity check 1:
        // All unique ingredients, so the whole array is valid when k >= 1.
        String[] ingredients3 = {"tomato", "onion", "garlic", "basil"};
        int result3 = solution.runExample(ingredients3, 1);
        System.out.println("Expected: 4, Actual: " + result3);
        System.out.println();

        // Additional sanity check 2:
        // Same ingredient repeated many times, k limits the answer directly.
        String[] ingredients4 = {"sugar", "sugar", "sugar", "sugar"};
        int result4 = solution.runExample(ingredients4, 2);
        System.out.println("Expected: 2, Actual: " + result4);
        System.out.println();

        // Additional sanity check 3:
        // k equals array length, so the whole array is always valid.
        String[] ingredients5 = {"a", "b", "a", "c", "b", "a"};
        int result5 = solution.runExample(ingredients5, ingredients5.length);
        System.out.println("Expected: 6, Actual: " + result5);
    }
}