import java.util.*;

/*
 * Title: Longest Delivery Streak With Limited Cold Packages
 * Difficulty: Medium
 * Topic: Sliding Window
 *
 * Problem Description:
 * A courier company records deliveries in the order they are loaded onto a truck.
 * Each delivery is labeled with a category:
 * - 'N' for normal packages
 * - 'C' for cold-chain packages that require expensive refrigerated handling
 *
 * The company wants to find the longest consecutive stretch of deliveries that can be
 * assigned to a standard truck, as long as that stretch contains at most k cold-chain packages.
 *
 * Given a string deliveries where each character is either 'N' or 'C', and an integer k,
 * return the length of the longest contiguous substring that contains at most k occurrences of 'C'.
 *
 * This is a realistic scheduling problem: the deliveries must remain in their original order,
 * and you may only choose one contiguous block. A block with more than k cold-chain packages
 * is invalid because it would exceed the truck's refrigeration allowance.
 *
 * Constraints:
 * - 1 <= deliveries.length <= 200000
 * - deliveries[i] is either 'N' or 'C'
 * - 0 <= k <= deliveries.length
 *
 * Example 1:
 * Input: deliveries = "NCCNNNCN", k = 2
 * Output: 6
 * Explanation: The longest valid block is "CNNNCN" or "NCCNNN" depending on how the window is chosen.
 * Each contains at most 2 cold-chain packages, so the answer is 6.
 *
 * Example 2:
 * Input: deliveries = "CCCCNNC", k = 1
 * Output: 3
 * Explanation: Any valid block can contain at most one 'C'. One longest choice is "CNN" after taking
 * a contiguous segment with exactly one cold-chain package. No longer valid segment exists.
 */

public class Solution {

    /**
     * Finds the length of the longest contiguous substring that contains at most k occurrences of 'C'.
     *
     * This method uses the classic sliding window technique:
     * 1. Expand the right side of the window one character at a time.
     * 2. Count how many cold-chain packages ('C') are currently inside the window.
     * 3. If the window becomes invalid (more than k 'C' characters), move the left side forward
     *    until the window becomes valid again.
     * 4. Track the maximum valid window length seen during the process.
     *
     * @param deliveries the string of deliveries, where each character is either 'N' or 'C'
     * @param k the maximum number of cold-chain packages allowed in the chosen contiguous block
     * @return the length of the longest contiguous substring containing at most k 'C' characters
     *
     * Time complexity: O(n), where n is deliveries.length, because each character is processed
     * at most twice (once by the right pointer and once by the left pointer).
     * Space complexity: O(1), because only a few integer variables are used.
     */
    public int longestDeliveryStreak(String deliveries, int k) {
        // Defensive handling for null or empty input.
        // The problem guarantees length >= 1, but this makes the method safer and beginner-friendly.
        if (deliveries == null || deliveries.isEmpty()) {
            return 0;
        }

        // left marks the beginning of the current sliding window.
        int left = 0;

        // coldCount stores how many 'C' characters are currently inside the window [left, right].
        int coldCount = 0;

        // best stores the maximum valid window length found so far.
        int best = 0;

        // Move right from the start of the string to the end.
        // At each step, we try to include deliveries.charAt(right) into the current window.
        for (int right = 0; right < deliveries.length(); right++) {

            // Step 1: Expand the window by including the character at index right.
            char current = deliveries.charAt(right);

            // If the newly included character is a cold-chain package, increase the count.
            if (current == 'C') {
                coldCount++;
            }

            // Step 2: If the window is invalid (too many 'C' characters),
            // shrink it from the left until it becomes valid again.
            //
            // Why a while loop instead of if?
            // Because removing just one character may still leave us with more than k 'C' characters.
            while (coldCount > k) {
                char leftChar = deliveries.charAt(left);

                // If the character leaving the window is 'C', reduce the cold count.
                if (leftChar == 'C') {
                    coldCount--;
                }

                // Move the left boundary one step to the right.
                left++;
            }

            // Step 3: At this point, the window [left, right] is guaranteed to be valid.
            // So we compute its length.
            int currentLength = right - left + 1;

            // Step 4: Update the best answer if this valid window is larger than any previous one.
            if (currentLength > best) {
                best = currentLength;
            }
        }

        // After scanning the entire string, best contains the longest valid window length.
        return best;
    }

    /**
     * A helper method that runs one demonstration test case and prints the result.
     *
     * @param deliveries the input deliveries string
     * @param k the maximum allowed number of 'C' characters
     * @return the computed longest valid streak length
     *
     * Time complexity: O(n), where n is deliveries.length, because it delegates to the sliding window method.
     * Space complexity: O(1), excluding the input string.
     */
    public int demonstrateCase(String deliveries, int k) {
        int result = longestDeliveryStreak(deliveries, k);
        System.out.println("deliveries = " + deliveries + ", k = " + k + " -> " + result);
        return result;
    }

    /**
     * Demonstrates the solution using the sample inputs from the problem statement
     * and a few additional beginner-friendly test cases.
     *
     * @param args command-line arguments (not used)
     * @return nothing
     *
     * Time complexity: O(total input size of demonstrated test cases)
     * Space complexity: O(1), excluding input storage
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Sample Example 1
        // deliveries = "NCCNNNCN", k = 2
        // Expected output: 6
        //
        // Quick correctness check:
        // One valid longest window is "CCNNNC" from index 1 to 6, length 6, with exactly 2 'C's.
        // Another valid length-6 window is "CNNNCN" from index 2 to 7, also with 2 'C's.
        // So the correct answer is 6.
        solution.demonstrateCase("NCCNNNCN", 2);

        // Sample Example 2
        // deliveries = "CCCCNNC", k = 1
        // Expected output: 3
        //
        // Quick correctness check:
        // A valid longest window is "CNN" from index 3 to 5, length 3, with exactly 1 'C'.
        // No length-4 window can have at most 1 'C' in this string.
        // So the correct answer is 3.
        solution.demonstrateCase("CCCCNNC", 1);

        // Additional test cases for clarity:

        // No cold packages allowed.
        // Longest block must contain only 'N'.
        solution.demonstrateCase("NNNCNNN", 0); // Expected: 3

        // All packages are normal, so the entire string is valid.
        solution.demonstrateCase("NNNNN", 2); // Expected: 5

        // k is large enough to allow all cold packages in the string.
        solution.demonstrateCase("CNCCNC", 10); // Expected: 6

        // Single character cases.
        solution.demonstrateCase("C", 0); // Expected: 0
        solution.demonstrateCase("C", 1); // Expected: 1
        solution.demonstrateCase("N", 0); // Expected: 1
    }
}