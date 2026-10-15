/*
 * Title: Find the Second Largest Daily Sale
 * Difficulty: Easy
 * Topic: Arrays
 *
 * Problem Description:
 * You are given an integer array sales where sales[i] represents the total sales made on day i.
 * Your task is to return the second largest distinct value in the array. A value is considered
 * distinct only if it is different from the largest value. If there is no second largest distinct
 * value, return -1.
 *
 * This problem is about scanning an array efficiently while keeping track of the two best distinct
 * values seen so far. Duplicate values should not be counted multiple times. For example, if the
 * largest value appears several times, you still need the next smaller distinct value.
 *
 * Write a function that returns the second largest distinct daily sale.
 *
 * Constraints:
 * - 1 <= sales.length <= 10^5
 * - -10^9 <= sales[i] <= 10^9
 * - The solution should run in O(n) time.
 * - Extra space should be O(1), not counting the input array.
 *
 * Example 1:
 * Input: sales = [12, 7, 19, 19, 5, 14]
 * Output: 14
 * Explanation: The largest distinct sale is 19, and the second largest distinct sale is 14.
 *
 * Example 2:
 * Input: sales = [8, 8, 8]
 * Output: -1
 * Explanation: There is only one distinct value in the array, so a second largest distinct value
 * does not exist.
 */

import java.util.*;

public class Solution {

    /**
     * Finds the second largest distinct value in the given sales array.
     *
     * The method scans the array exactly once while maintaining:
     * 1. The largest distinct value seen so far
     * 2. The second largest distinct value seen so far
     *
     * Duplicate values are ignored when determining distinct positions.
     *
     * @param sales the array of daily sales values
     * @return the second largest distinct value, or -1 if it does not exist
     * Time complexity: O(n), where n is the length of the array
     * Space complexity: O(1), because only a constant amount of extra space is used
     */
    public int findSecondLargestDistinct(int[] sales) {
        // We use Integer objects instead of primitive int so that we can clearly represent
        // the "not assigned yet" state with null.
        //
        // largest will store the largest distinct value found so far.
        // secondLargest will store the second largest distinct value found so far.
        Integer largest = null;
        Integer secondLargest = null;

        // Traverse every value in the array exactly once.
        for (int sale : sales) {

            // Case 1:
            // If largest has not been assigned yet, then this is the first value we are seeing.
            // So it automatically becomes the largest.
            if (largest == null) {
                largest = sale;
            }
            // Case 2:
            // If the current sale is greater than the current largest, then:
            // - the old largest must move down to secondLargest
            // - the current sale becomes the new largest
            //
            // This preserves the top two distinct values in correct order.
            else if (sale > largest) {
                secondLargest = largest;
                largest = sale;
            }
            // Case 3:
            // If the current sale is equal to largest, we ignore it.
            //
            // Why?
            // Because the problem asks for the second largest DISTINCT value.
            // Repeated copies of the largest value should not count as second largest.
            else if (sale == largest) {
                continue;
            }
            // Case 4:
            // Now we know sale < largest.
            // We should check whether it can become the second largest distinct value.
            //
            // It should become secondLargest if:
            // - secondLargest has not been assigned yet, OR
            // - sale is greater than the current secondLargest
            //
            // This ensures secondLargest is always the best distinct value below largest.
            else if (secondLargest == null || sale > secondLargest) {
                secondLargest = sale;
            }
            // Case 5:
            // Otherwise, sale is not useful for our answer:
            // - it is smaller than or equal to secondLargest
            // - or it is just another duplicate of a smaller value
            //
            // In either situation, we do nothing.
        }

        // If secondLargest was never assigned, then no second distinct value exists.
        // According to the problem statement, we return -1 in that case.
        return secondLargest == null ? -1 : secondLargest;
    }

    /**
     * Helper method to print an array in a readable format and show the computed answer.
     *
     * @param sales the input sales array to test
     * @return the computed second largest distinct value
     * Time complexity: O(n), due to calling the main algorithm and converting the array to string
     * Space complexity: O(1) extra algorithmic space, ignoring output formatting internals
     */
    public int demonstrate(int[] sales) {
        int result = findSecondLargestDistinct(sales);
        System.out.println("Input: " + Arrays.toString(sales));
        System.out.println("Second largest distinct sale: " + result);
        System.out.println();
        return result;
    }

    /**
     * Main method to demonstrate the solution on sample and additional test cases.
     *
     * @param args command-line arguments (not used)
     * @return nothing
     * Time complexity: O(k * n) overall for the demonstrated test cases, where each case is processed linearly
     * Space complexity: O(1) extra algorithmic space per test case
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Example 1 from the problem statement:
        // sales = [12, 7, 19, 19, 5, 14]
        // Distinct values are {12, 7, 19, 5, 14}
        // Largest distinct = 19
        // Second largest distinct = 14
        solution.demonstrate(new int[]{12, 7, 19, 19, 5, 14});

        // Example 2 from the problem statement:
        // sales = [8, 8, 8]
        // Only one distinct value exists: 8
        // Therefore, no second largest distinct value exists
        // Expected answer = -1
        solution.demonstrate(new int[]{8, 8, 8});

        // Additional test: array of length 1
        // Only one value exists, so answer must be -1
        solution.demonstrate(new int[]{42});

        // Additional test: negative numbers
        // Distinct values are {-10, -3, -20, -7}
        // Largest = -3
        // Second largest = -7
        solution.demonstrate(new int[]{-10, -3, -20, -7, -3});

        // Additional test: already descending
        // Largest = 100
        // Second largest = 90
        solution.demonstrate(new int[]{100, 90, 80, 70});

        // Additional test: repeated largest and repeated second largest
        // Distinct values are {5, 4, 3}
        // Largest = 5
        // Second largest = 4
        solution.demonstrate(new int[]{5, 5, 4, 4, 3, 3});
    }
}