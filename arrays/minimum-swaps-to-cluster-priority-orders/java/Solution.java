import java.util.*;

/*
 * Title: Minimum Swaps to Cluster Priority Orders
 * Difficulty: Medium
 * Topic: Arrays
 *
 * Problem Description:
 * A warehouse tracks outgoing orders in an array where each value is either 0 or 1.
 * A value of 1 represents a priority order, and 0 represents a regular order.
 * To speed up loading, the warehouse wants all priority orders to appear together
 * in one contiguous block somewhere in the array. The block does not need to be at
 * the beginning or the end; it can be placed anywhere as long as all 1s are grouped together.
 *
 * In one operation, you may swap the values at any two different indices in the array.
 * Return the minimum number of swaps required to make all priority orders contiguous.
 *
 * You are not asked to return the final arrangement, only the minimum number of swaps.
 *
 * This problem is about choosing the best window of length equal to the total number
 * of priority orders. Inside that window, every 0 represents a regular order that must
 * be swapped out with a priority order from outside the window.
 *
 * Constraints:
 * - 1 <= orders.length <= 100000
 * - orders[i] is either 0 or 1
 * - The answer fits in a 32-bit integer
 *
 * Example 1:
 * Input: orders = [1,0,1,0,1]
 * Output: 1
 * Explanation:
 * There are 3 priority orders, so we examine windows of length 3.
 * The best window is [1,0,1], which contains only one 0.
 * Swap that 0 with the 1 outside the window to get all 1s together.
 *
 * Example 2:
 * Input: orders = [0,0,1,0,1,1,0]
 * Output: 1
 * Explanation:
 * There are 3 priority orders.
 * We only consider windows of length 3.
 * The best length-3 window contains two 1s and one 0, so only one swap is needed.
 *
 * Important observation:
 * If the array contains 0 or 1 priority order, the answer is 0 because all priority
 * orders are already contiguous.
 */

public class Solution {

    /**
     * Computes the minimum number of swaps needed to group all 1s into one contiguous block.
     *
     * Core idea:
     * 1. Count how many 1s exist in the entire array. Let that count be totalOnes.
     * 2. If totalOnes is 0 or 1, the answer is immediately 0.
     * 3. Any valid final contiguous block of all 1s must have length exactly totalOnes.
     * 4. For every window of length totalOnes:
     *    - Count how many 0s are inside that window.
     *    - Each 0 inside must be swapped with a 1 outside.
     *    - Therefore, the number of swaps needed for that window equals the number of 0s in it.
     * 5. The minimum such value across all windows is the answer.
     *
     * We implement this efficiently using a sliding window:
     * - Build the first window.
     * - Then move the window one step at a time.
     * - Update the number of 0s by removing the left element and adding the new right element.
     *
     * @param orders the binary array where 1 means priority order and 0 means regular order
     * @return the minimum number of swaps required to make all 1s contiguous
     * Time complexity: O(n), where n is the length of the array
     * Space complexity: O(1), ignoring input storage
     */
    public int minSwaps(int[] orders) {
        // Step 1:
        // Count the total number of priority orders (1s) in the array.
        // This count tells us the exact size of the contiguous block we want to create.
        int totalOnes = countOnes(orders);

        // Step 2:
        // If there are no 1s, or only one 1, then all priority orders are already contiguous.
        // Examples:
        // - [0,0,0] -> no 1s -> answer is 0
        // - [0,1,0,0] -> one 1 -> answer is 0
        if (totalOnes <= 1) {
            return 0;
        }

        // Step 3:
        // We now examine every window of length totalOnes.
        // For each such window, we count how many 0s are inside it.
        // Those 0s are exactly the positions that must be replaced by 1s from outside the window.

        // Build the first window: indices [0 .. totalOnes - 1]
        int zerosInWindow = 0;
        for (int i = 0; i < totalOnes; i++) {
            if (orders[i] == 0) {
                zerosInWindow++;
            }
        }

        // The first window is our initial best candidate.
        int minSwapsNeeded = zerosInWindow;

        // Step 4:
        // Slide the window across the array.
        // If the window length is totalOnes, and the right end moves to index right,
        // then the left end is right - totalOnes.
        for (int right = totalOnes; right < orders.length; right++) {
            int left = right - totalOnes;

            // Remove the effect of the element leaving the window.
            // If the outgoing element was 0, then the zero count decreases by 1.
            if (orders[left] == 0) {
                zerosInWindow--;
            }

            // Add the effect of the new element entering the window.
            // If the incoming element is 0, then the zero count increases by 1.
            if (orders[right] == 0) {
                zerosInWindow++;
            }

            // Update the best answer seen so far.
            minSwapsNeeded = Math.min(minSwapsNeeded, zerosInWindow);
        }

        // Step 5:
        // The smallest number of 0s found in any valid window is the minimum number of swaps.
        return minSwapsNeeded;
    }

    /**
     * Counts how many elements in the array are equal to 1.
     *
     * @param orders the binary array to inspect
     * @return the total number of 1s in the array
     * Time complexity: O(n), where n is the length of the array
     * Space complexity: O(1)
     */
    public int countOnes(int[] orders) {
        int count = 0;

        // Visit every element once and count the number of 1s.
        for (int value : orders) {
            if (value == 1) {
                count++;
            }
        }

        return count;
    }

    /**
     * Converts an integer array to a readable string representation.
     * This helper is used only for demonstration in main.
     *
     * @param array the array to convert to text
     * @return a string such as [1, 0, 1, 0, 1]
     * Time complexity: O(n), where n is the length of the array
     * Space complexity: O(n), due to string construction
     */
    public String arrayToString(int[] array) {
        return Arrays.toString(array);
    }

    /**
     * Demonstrates the solution on the sample inputs and a few additional edge cases.
     *
     * Verified sample traces:
     *
     * Example 1:
     * orders = [1,0,1,0,1]
     * totalOnes = 3
     * windows of length 3:
     * - [1,0,1] -> 1 zero
     * - [0,1,0] -> 2 zeros
     * - [1,0,1] -> 1 zero
     * minimum = 1
     *
     * Example 2:
     * orders = [0,0,1,0,1,1,0]
     * totalOnes = 3
     * windows of length 3:
     * - [0,0,1] -> 2 zeros
     * - [0,1,0] -> 2 zeros
     * - [1,0,1] -> 1 zero
     * - [0,1,1] -> 1 zero
     * - [1,1,0] -> 1 zero
     * minimum = 1
     *
     * @param args command-line arguments (not used)
     * @return nothing
     * Time complexity: O(k * n) across all demonstrations, where k is the number of test cases
     * Space complexity: O(1), excluding output text
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        int[] sample1 = {1, 0, 1, 0, 1};
        int[] sample2 = {0, 0, 1, 0, 1, 1, 0};

        System.out.println("Sample Input 1: " + solution.arrayToString(sample1));
        System.out.println("Minimum swaps: " + solution.minSwaps(sample1));
        System.out.println("Expected: 1");
        System.out.println();

        System.out.println("Sample Input 2: " + solution.arrayToString(sample2));
        System.out.println("Minimum swaps: " + solution.minSwaps(sample2));
        System.out.println("Expected: 1");
        System.out.println();

        // Additional beginner-friendly checks

        int[] edge1 = {0, 0, 0, 0};
        System.out.println("Edge Case 1: " + solution.arrayToString(edge1));
        System.out.println("Minimum swaps: " + solution.minSwaps(edge1));
        System.out.println("Expected: 0");
        System.out.println();

        int[] edge2 = {1};
        System.out.println("Edge Case 2: " + solution.arrayToString(edge2));
        System.out.println("Minimum swaps: " + solution.minSwaps(edge2));
        System.out.println("Expected: 0");
        System.out.println();

        int[] edge3 = {1, 1, 1, 1};
        System.out.println("Edge Case 3: " + solution.arrayToString(edge3));
        System.out.println("Minimum swaps: " + solution.minSwaps(edge3));
        System.out.println("Expected: 0");
        System.out.println();

        int[] edge4 = {1, 0, 0, 1, 0, 1};
        System.out.println("Edge Case 4: " + solution.arrayToString(edge4));
        System.out.println("Minimum swaps: " + solution.minSwaps(edge4));
        System.out.println("Expected: 1");
    }
}