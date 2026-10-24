import java.util.*;

/*
 * Title: Find the Earliest Fully Stocked Shelf
 * Difficulty: Easy
 * Topic: Arrays
 *
 * Problem Description:
 * A store receives a delivery log for a single shelf. The shelf has n slots, and each slot should
 * eventually contain exactly one product. You are given an array deliveries of length n, where
 * deliveries[i] is the slot number filled on minute i + 1. Each slot number from 1 to n appears
 * exactly once.
 *
 * Return the earliest minute when the shelf becomes fully stocked from left to right without any gaps,
 * meaning slots 1 through k are all filled for some k, and k is as large as possible at that minute.
 * In this problem, you must return the earliest minute when the filled slots form the prefix 1 through m,
 * where m is the maximum slot number filled so far and all smaller slot numbers are also already filled.
 *
 * Equivalently, after processing minute t, look at the largest slot number seen so far. If every slot
 * from 1 up to that number has been filled, then the shelf is fully stocked up to that point. Return
 * the first minute when this happens. If it only happens after the final delivery, return n.
 *
 * Constraints:
 * - 1 <= n <= 100000
 * - deliveries.length == n
 * - 1 <= deliveries[i] <= n
 * - All values in deliveries are distinct
 *
 * Example 1:
 * Input: deliveries = [2,1,3,5,4]
 * Output: 2
 * Explanation:
 * After minute 1, only slot 2 is filled, so there is a gap at slot 1.
 * After minute 2, slots 1 and 2 are both filled, and the maximum filled slot is 2,
 * so the shelf is fully stocked from 1 to 2.
 *
 * Example 2:
 * Input: deliveries = [3,1,2,4]
 * Output: 3
 * Explanation:
 * After minute 1, slot 3 is filled, but slots 1 and 2 are missing.
 * After minute 2, slots 1 and 3 are filled, but slot 2 is still missing.
 * After minute 3, slots 1, 2, and 3 are filled, which matches the maximum filled slot,
 * so the answer is 3.
 */

public class Solution {

    /**
     * Finds the earliest minute when all filled shelf slots form a perfect prefix from 1 to the
     * maximum slot number seen so far.
     *
     * Core idea:
     * - As we process deliveries minute by minute, we track the largest slot number filled so far.
     * - At minute i (1-based), exactly i slots have been filled because one new distinct slot is filled each minute.
     * - If the largest slot number seen so far is equal to i, then the filled slots must be exactly
     *   the set {1, 2, ..., i}. Why?
     *   Because:
     *   1) We have filled exactly i distinct slots.
     *   2) The maximum among them is i.
     *   3) The only way to have i distinct numbers all within the range [1, i] is to have every number
     *      from 1 through i present.
     * - Therefore, the first minute where maxSeen == minuteNumber is the answer.
     *
     * @param deliveries an array where deliveries[i] is the slot filled at minute i + 1
     * @return the earliest minute when the filled slots form a complete prefix from 1 to the current maximum
     * Time complexity: O(n), because we scan the array once.
     * Space complexity: O(1), because we use only a few variables.
     */
    public int earliestFullyStockedMinute(int[] deliveries) {
        // This variable stores the largest slot number we have seen so far.
        int maxSeen = 0;

        // We process each delivery in order.
        // Index i corresponds to minute i + 1.
        for (int i = 0; i < deliveries.length; i++) {
            // Update the largest slot number encountered up to this minute.
            maxSeen = Math.max(maxSeen, deliveries[i]);

            // The current minute number is i + 1 because i is zero-based.
            int minute = i + 1;

            // Very important observation:
            // If maxSeen == minute, then after 'minute' deliveries:
            // - we have exactly 'minute' distinct filled slots
            // - all of them are between 1 and maxSeen (= minute)
            // Therefore, they must be exactly 1 through minute with no gaps.
            //
            // This is the earliest minute we are asked to return, so we return immediately.
            if (maxSeen == minute) {
                return minute;
            }
        }

        // According to the problem statement, if it does not happen earlier,
        // it will definitely happen by the final delivery.
        // At minute n, all slots 1..n are filled, so the condition is true.
        return deliveries.length;
    }

    /**
     * Helper method to print an integer array in a beginner-friendly format.
     *
     * @param arr the array to print
     * @return a string representation of the array
     * Time complexity: O(n), where n is the array length.
     * Space complexity: O(n), due to string construction.
     */
    public String arrayToString(int[] arr) {
        return Arrays.toString(arr);
    }

    /**
     * Runs a single demonstration test case:
     * prints the input array, computes the answer, and prints the result.
     *
     * @param deliveries the delivery order to test
     * @return the computed earliest fully stocked minute
     * Time complexity: O(n), because it calls the main algorithm once.
     * Space complexity: O(1), excluding output formatting.
     */
    public int runDemo(int[] deliveries) {
        System.out.println("Deliveries: " + arrayToString(deliveries));
        int result = earliestFullyStockedMinute(deliveries);
        System.out.println("Earliest fully stocked minute: " + result);
        System.out.println();
        return result;
    }

    /**
     * Main method to demonstrate the solution on sample inputs from the problem statement
     * and a few additional cases.
     *
     * @param args command-line arguments (not used)
     * @return nothing
     * Time complexity: O(total input size of demonstrated test cases).
     * Space complexity: O(1), excluding output formatting.
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Sample 1 from the problem statement:
        // deliveries = [2,1,3,5,4]
        // Minute 1: maxSeen = 2, minute = 1 -> not equal
        // Minute 2: maxSeen = 2, minute = 2 -> equal, answer = 2
        int[] sample1 = {2, 1, 3, 5, 4};
        int result1 = solution.runDemo(sample1);
        System.out.println("Expected: 2, Actual: " + result1);
        System.out.println();

        // Sample 2 from the problem statement:
        // deliveries = [3,1,2,4]
        // Minute 1: maxSeen = 3, minute = 1 -> not equal
        // Minute 2: maxSeen = 3, minute = 2 -> not equal
        // Minute 3: maxSeen = 3, minute = 3 -> equal, answer = 3
        int[] sample2 = {3, 1, 2, 4};
        int result2 = solution.runDemo(sample2);
        System.out.println("Expected: 3, Actual: " + result2);
        System.out.println();

        // Additional example:
        // deliveries = [1,2,3]
        // Minute 1 already forms prefix [1], so answer = 1
        int[] extra1 = {1, 2, 3};
        int result3 = solution.runDemo(extra1);
        System.out.println("Expected: 1, Actual: " + result3);
        System.out.println();

        // Additional example:
        // deliveries = [4,1,2,3]
        // Minute 1: maxSeen = 4
        // Minute 2: maxSeen = 4
        // Minute 3: maxSeen = 4
        // Minute 4: maxSeen = 4, minute = 4 -> answer = 4
        int[] extra2 = {4, 1, 2, 3};
        int result4 = solution.runDemo(extra2);
        System.out.println("Expected: 4, Actual: " + result4);
    }
}