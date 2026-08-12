import java.util.*;

/*
Problem Title: Longest Fruit Basket Refill Under Weight Limit

Problem Description:
A grocery store packs fruits onto a conveyor belt in a fixed order. The weight of each fruit
is given in an integer array weights, where weights[i] is the weight of the ith fruit.
A worker wants to refill a basket using one contiguous group of fruits from the belt.
The basket can hold at most maxWeight total weight.

Your task is to return the length of the longest contiguous subarray whose sum is less than
or equal to maxWeight.

Because the fruits must be taken in order and without skipping, this is a contiguous window
problem. If multiple windows have the same maximum length, you only need to return the length,
not the actual window.

You may assume all fruit weights are positive integers, which makes it possible to grow and
shrink a sliding window efficiently.

Constraints:
- 1 <= weights.length <= 100000
- 1 <= weights[i] <= 10000
- 1 <= maxWeight <= 1000000000

Example 1:
Input: weights = [2, 1, 3, 2, 1], maxWeight = 5
Output: 2
Explanation: The longest valid contiguous groups include [2, 1], [3, 2], and [2, 1].
Any group of length 3 exceeds the basket limit.

Example 2:
Input: weights = [1, 1, 1, 1, 2], maxWeight = 4
Output: 4
Explanation: The subarray [1, 1, 1, 1] has total weight 4, so it fits exactly.
No longer contiguous group stays within the limit.
*/

public class Solution {

    /**
     * Finds the length of the longest contiguous subarray whose sum is less than or equal to maxWeight.
     *
     * This method uses the classic sliding window technique:
     * - Expand the window by moving the right pointer.
     * - Keep track of the current window sum.
     * - If the sum becomes too large, shrink the window from the left until it becomes valid again.
     * - Track the maximum valid window length seen so far.
     *
     * This works efficiently because all weights are positive integers. That means:
     * - Expanding the window can only increase the sum.
     * - Shrinking the window can only decrease the sum.
     * Therefore, we never need to move pointers backward.
     *
     * @param weights the array of positive fruit weights in conveyor-belt order
     * @param maxWeight the maximum total weight the basket can hold
     * @return the maximum length of a contiguous subarray with sum less than or equal to maxWeight
     *
     * Time complexity: O(n), where n is the length of weights, because each index is visited at most twice
     *                  (once by the right pointer and once by the left pointer).
     * Space complexity: O(1), because only a few extra variables are used.
     */
    public int longestFruitBasket(int[] weights, int maxWeight) {
        // Left boundary of the current sliding window.
        int left = 0;

        // This stores the sum of all elements currently inside the window [left, right].
        long currentSum = 0;

        // This stores the best (maximum) valid window length found so far.
        int maxLength = 0;

        // Move the right boundary one step at a time across the array.
        for (int right = 0; right < weights.length; right++) {
            // Step 1: Expand the window by including weights[right].
            currentSum += weights[right];

            // Step 2: If the window is invalid (sum too large), shrink it from the left.
            // Because all numbers are positive, removing elements from the left will reduce the sum.
            while (currentSum > maxWeight) {
                currentSum -= weights[left];
                left++;
            }

            // Step 3: At this point, the window [left, right] is guaranteed to be valid.
            // Compute its length.
            int currentLength = right - left + 1;

            // Step 4: Update the best answer if this valid window is longer.
            if (currentLength > maxLength) {
                maxLength = currentLength;
            }
        }

        // Return the longest valid contiguous window length.
        return maxLength;
    }

    /**
     * A small helper method to print an array in a beginner-friendly format.
     *
     * @param arr the integer array to print
     * @return a string representation of the array
     *
     * Time complexity: O(n), where n is the length of arr
     * Space complexity: O(n), due to string construction
     */
    public String arrayToString(int[] arr) {
        return Arrays.toString(arr);
    }

    /**
     * Demonstrates the solution using the sample inputs from the problem statement
     * and prints the results.
     *
     * It also includes expected outputs so the reader can visually verify correctness.
     *
     * @param args command-line arguments (not used)
     * @return nothing
     *
     * Time complexity: O(n) overall for the demonstrated test cases
     * Space complexity: O(1) extra space excluding output formatting
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Sample Example 1
        int[] weights1 = {2, 1, 3, 2, 1};
        int maxWeight1 = 5;
        int result1 = solution.longestFruitBasket(weights1, maxWeight1);

        System.out.println("Example 1");
        System.out.println("weights = " + solution.arrayToString(weights1));
        System.out.println("maxWeight = " + maxWeight1);
        System.out.println("Output = " + result1);
        System.out.println("Expected = 2");
        System.out.println();

        // Sample Example 2
        int[] weights2 = {1, 1, 1, 1, 2};
        int maxWeight2 = 4;
        int result2 = solution.longestFruitBasket(weights2, maxWeight2);

        System.out.println("Example 2");
        System.out.println("weights = " + solution.arrayToString(weights2));
        System.out.println("maxWeight = " + maxWeight2);
        System.out.println("Output = " + result2);
        System.out.println("Expected = 4");
        System.out.println();

        // Additional quick sanity checks
        int[] weights3 = {5};
        int maxWeight3 = 5;
        int result3 = solution.longestFruitBasket(weights3, maxWeight3);

        System.out.println("Additional Test 1");
        System.out.println("weights = " + solution.arrayToString(weights3));
        System.out.println("maxWeight = " + maxWeight3);
        System.out.println("Output = " + result3);
        System.out.println("Expected = 1");
        System.out.println();

        int[] weights4 = {6};
        int maxWeight4 = 5;
        int result4 = solution.longestFruitBasket(weights4, maxWeight4);

        System.out.println("Additional Test 2");
        System.out.println("weights = " + solution.arrayToString(weights4));
        System.out.println("maxWeight = " + maxWeight4);
        System.out.println("Output = " + result4);
        System.out.println("Expected = 0");
    }
}