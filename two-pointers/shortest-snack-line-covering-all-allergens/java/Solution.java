import java.util.*;

/*
 * Title: Shortest Snack Line Covering All Allergens
 * Difficulty: Medium
 * Topic: Two Pointers
 *
 * Problem Description:
 * A food safety team is reviewing a cafeteria snack line. The line is represented by an array
 * snacks, where snacks[i] is the allergen label of the snack at position i. Labels are strings
 * such as "nuts", "dairy", or "soy". You are also given an array required, containing the set
 * of allergen labels that must all appear at least once in a contiguous section of the line for
 * inspection.
 *
 * Your task is to return the length of the shortest contiguous subarray of snacks that contains
 * every allergen in required at least once. If no such subarray exists, return -1.
 *
 * This is a realistic streaming/window problem: the snack line is ordered, and the inspectors
 * want the smallest continuous region they need to isolate. Duplicate allergen labels may appear
 * many times in snacks, but required should be treated as a set of distinct labels even if
 * duplicates are present in the input.
 *
 * Constraints:
 * - 1 <= snacks.length <= 100000
 * - 1 <= required.length <= 100000
 * - snacks[i] and required[i] consist of lowercase English letters
 * - Total number of characters across all strings is at most 300000
 *
 * Example 1:
 * Input: snacks = ["dairy","soy","nuts","gluten","soy","dairy"], required = ["soy","nuts","dairy"]
 * Output: 3
 * Explanation: The subarray ["dairy","soy","nuts"] contains all required allergens and has
 * length 3, which is the minimum possible.
 *
 * Example 2:
 * Input: snacks = ["fruit","seed","dairy","fruit","seed"], required = ["dairy","soy"]
 * Output: -1
 * Explanation: No contiguous section contains both dairy and soy, so the answer is -1.
 */

public class Solution {

    /**
     * Finds the length of the shortest contiguous subarray of snacks that contains
     * every distinct allergen listed in required at least once.
     *
     * The method uses the classic sliding window / two-pointers technique:
     * 1. Convert required into a set so duplicates are ignored.
     * 2. Expand the right pointer to include more snacks.
     * 3. Track counts of required allergens currently inside the window.
     * 4. Once the window contains all required allergens, move the left pointer
     *    forward as much as possible while still keeping the window valid.
     * 5. Record the minimum valid window length seen.
     *
     * @param snacks the ordered snack line, where each element is an allergen label
     * @param required the allergen labels that must all appear in the chosen subarray;
     *                 duplicates are treated as one distinct requirement
     * @return the length of the shortest valid contiguous subarray, or -1 if no such subarray exists
     *
     * Time complexity: O(n + m), where n = snacks.length and m = required.length.
     * Each pointer moves at most n times, and building the required set takes O(m).
     *
     * Space complexity: O(k), where k is the number of distinct required allergens.
     * We store the required set and frequency map only for required labels.
     */
    public int shortestSnackLine(String[] snacks, String[] required) {
        // Defensive handling for completeness.
        // Based on constraints, arrays are non-empty, but this makes the method safer.
        if (snacks == null || required == null || snacks.length == 0 || required.length == 0) {
            return -1;
        }

        // Step 1:
        // Convert the required array into a set.
        // This is important because the problem explicitly says required should be treated
        // as a set of distinct labels, even if duplicates appear in the input.
        Set<String> requiredSet = new HashSet<>();
        for (String allergen : required) {
            requiredSet.add(allergen);
        }

        // Total number of distinct allergens we must cover in the window.
        int need = requiredSet.size();

        // Step 2:
        // This map stores how many times each required allergen appears inside the current window.
        // We only track labels that are actually required.
        Map<String, Integer> windowCount = new HashMap<>();

        // Step 3:
        // formed = how many distinct required allergens are currently satisfied in the window.
        // A required allergen is "satisfied" when its count in the window becomes at least 1.
        int formed = 0;

        // left pointer of the sliding window.
        int left = 0;

        // This will store the smallest valid window length found.
        int minLength = Integer.MAX_VALUE;

        // Step 4:
        // Expand the window by moving the right pointer from left to right.
        for (int right = 0; right < snacks.length; right++) {
            String current = snacks[right];

            // Only process this snack if it is one of the required allergens.
            if (requiredSet.contains(current)) {
                // Increase its frequency inside the current window.
                int newCount = windowCount.getOrDefault(current, 0) + 1;
                windowCount.put(current, newCount);

                // If this allergen just appeared for the first time in the window,
                // then one more required allergen is now satisfied.
                if (newCount == 1) {
                    formed++;
                }
            }

            // Step 5:
            // If the current window contains all required allergens,
            // try to shrink it from the left to make it as short as possible.
            while (formed == need && left <= right) {
                // The current window [left, right] is valid.
                // Update the best answer.
                int currentLength = right - left + 1;
                if (currentLength < minLength) {
                    minLength = currentLength;
                }

                // We now attempt to remove snacks[left] from the window
                // and see whether the window remains valid.
                String leftSnack = snacks[left];

                // Only required allergens affect validity.
                if (requiredSet.contains(leftSnack)) {
                    int updatedCount = windowCount.get(leftSnack) - 1;
                    windowCount.put(leftSnack, updatedCount);

                    // If the count drops to 0, then this allergen is no longer covered,
                    // so the window becomes invalid after moving left forward.
                    if (updatedCount == 0) {
                        formed--;
                    }
                }

                // Move the left boundary forward to shrink the window.
                left++;
            }
        }

        // If minLength was never updated, no valid window exists.
        return minLength == Integer.MAX_VALUE ? -1 : minLength;
    }

    /**
     * A small helper method to print an array of strings in a readable format.
     *
     * @param array the string array to print
     * @return a human-readable string representation of the array
     *
     * Time complexity: O(n), where n is the array length.
     * Space complexity: O(n) for the StringBuilder output.
     */
    public String arrayToString(String[] array) {
        if (array == null) {
            return "null";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("[");

        for (int i = 0; i < array.length; i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append("\"").append(array[i]).append("\"");
        }

        sb.append("]");
        return sb.toString();
    }

    /**
     * Demonstrates the solution using the sample inputs from the problem statement
     * and prints the results.
     *
     * @param args command-line arguments (not used)
     * @return nothing
     *
     * Time complexity: O(1) for the fixed demonstration code, excluding the called method.
     * Space complexity: O(1), excluding the called method.
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Example 1
        String[] snacks1 = {"dairy", "soy", "nuts", "gluten", "soy", "dairy"};
        String[] required1 = {"soy", "nuts", "dairy"};
        int result1 = solution.shortestSnackLine(snacks1, required1);

        System.out.println("Example 1");
        System.out.println("snacks   = " + solution.arrayToString(snacks1));
        System.out.println("required = " + solution.arrayToString(required1));
        System.out.println("Output   = " + result1);
        System.out.println("Expected = 3");
        System.out.println();

        // Example 2
        String[] snacks2 = {"fruit", "seed", "dairy", "fruit", "seed"};
        String[] required2 = {"dairy", "soy"};
        int result2 = solution.shortestSnackLine(snacks2, required2);

        System.out.println("Example 2");
        System.out.println("snacks   = " + solution.arrayToString(snacks2));
        System.out.println("required = " + solution.arrayToString(required2));
        System.out.println("Output   = " + result2);
        System.out.println("Expected = -1");
        System.out.println();

        // Additional demonstration: required contains duplicates, but must be treated as a set.
        String[] snacks3 = {"soy", "dairy", "soy", "nuts"};
        String[] required3 = {"soy", "soy", "nuts"};
        int result3 = solution.shortestSnackLine(snacks3, required3);

        System.out.println("Additional Example");
        System.out.println("snacks   = " + solution.arrayToString(snacks3));
        System.out.println("required = " + solution.arrayToString(required3));
        System.out.println("Output   = " + result3);
        System.out.println("Expected = 2");
    }
}