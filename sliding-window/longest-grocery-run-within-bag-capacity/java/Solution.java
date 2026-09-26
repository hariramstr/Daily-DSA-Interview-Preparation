import java.util.*;

/*
Problem Title: Longest Grocery Run Within Bag Capacity

Problem Description:
You are given an array weights where weights[i] represents the weight of the i-th grocery item
picked up in order while walking through a store. You also have an integer capacity representing
the maximum total weight that can fit in your shopping bag at one time.

Your task is to find the length of the longest contiguous sequence of items you can pick such that
the sum of their weights is less than or equal to capacity. In other words, choose a subarray with
the largest possible length whose total weight does not exceed the bag limit.

This is a realistic sliding window problem because all item weights are non-negative, so if a
window becomes too heavy, you can move its left boundary forward until it becomes valid again.

Return the maximum number of consecutive items that can fit in the bag.

Constraints:
- 1 <= weights.length <= 100000
- 0 <= weights[i] <= 10000
- 0 <= capacity <= 1000000000

Example 1:
Input: weights = [2, 1, 3, 2, 1], capacity = 5
Output: 2
Explanation: Valid contiguous runs include [2,1], [3,2], and [2,1]. Each has total weight 5 or less
and length 2. No length-3 subarray fits within the capacity.

Example 2:
Input: weights = [1, 1, 1, 1, 2], capacity = 4
Output: 4
Explanation: The subarray [1,1,1,1] has total weight 4, so the answer is 4. The full array has
total weight 6, which exceeds the capacity.
*/

public class Solution {

    /**
     * Finds the maximum length of a contiguous subarray whose sum is less than or equal to
     * the given bag capacity.
     *
     * This method uses the classic sliding window technique:
     * - Expand the window by moving the right pointer.
     * - Keep track of the running sum of the current window.
     * - If the sum becomes too large, shrink the window from the left until it becomes valid again.
     * - Record the largest valid window length seen so far.
     *
     * This works efficiently because all weights are non-negative. That property guarantees that:
     * - Expanding the window can only keep the sum the same or increase it.
     * - Shrinking the window can only keep the sum the same or decrease it.
     *
     * @param weights the array of grocery item weights in the order they are picked
     * @param capacity the maximum allowed total weight of any chosen contiguous sequence
     * @return the length of the longest contiguous sequence whose total weight is at most capacity
     *
     * Time complexity: O(n), because each element is added to the window once and removed at most once.
     * Space complexity: O(1), because only a few variables are used regardless of input size.
     */
    public int longestGroceryRun(int[] weights, int capacity) {
        // Left boundary of the current sliding window.
        int left = 0;

        // This stores the best (maximum) valid window length found so far.
        int maxLength = 0;

        // We use long for safety, even though int would usually be enough under these constraints.
        // Using long avoids any accidental overflow if constraints change or if sums get large.
        long currentSum = 0;

        // Move the right boundary from left to right across the array.
        for (int right = 0; right < weights.length; right++) {
            // Step 1: Include the new item at index 'right' into the current window.
            currentSum += weights[right];

            // Step 2: If the window is too heavy, shrink it from the left side.
            // Because all values are non-negative, removing items from the left is the correct way
            // to reduce the sum until the window becomes valid again.
            while (currentSum > capacity && left <= right) {
                currentSum -= weights[left];
                left++;
            }

            // Step 3: At this point, the window [left, right] is valid:
            // currentSum <= capacity
            // So we compute its length.
            int currentLength = right - left + 1;

            // Step 4: Update the best answer if this valid window is longer than any previous one.
            if (currentLength > maxLength) {
                maxLength = currentLength;
            }
        }

        // After scanning the entire array, maxLength holds the answer.
        return maxLength;
    }

    /**
     * Helper method to print an example run in a beginner-friendly format.
     *
     * @param weights the array of grocery item weights
     * @param capacity the bag capacity
     * @return the computed longest valid contiguous run length
     *
     * Time complexity: O(n), because it calls the sliding window method once.
     * Space complexity: O(1), excluding the space used by the input array itself.
     */
    public int demonstrateExample(int[] weights, int capacity) {
        int result = longestGroceryRun(weights, capacity);
        System.out.println("weights = " + Arrays.toString(weights));
        System.out.println("capacity = " + capacity);
        System.out.println("Longest valid contiguous run length = " + result);
        System.out.println();
        return result;
    }

    /**
     * Main method to demonstrate the solution on the sample inputs from the problem statement.
     *
     * It also prints the expected outputs so it is easy to verify correctness.
     *
     * @param args command-line arguments (not used)
     *
     * Time complexity: O(n) per demonstrated test case.
     * Space complexity: O(1), excluding input storage.
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Example 1 from the problem statement:
        // weights = [2, 1, 3, 2, 1], capacity = 5
        // Expected output: 2
        int[] weights1 = {2, 1, 3, 2, 1};
        int capacity1 = 5;
        int result1 = solution.demonstrateExample(weights1, capacity1);
        System.out.println("Expected: 2");
        System.out.println("Actual:   " + result1);
        System.out.println();

        // Example 2 from the problem statement:
        // weights = [1, 1, 1, 1, 2], capacity = 4
        // Expected output: 4
        int[] weights2 = {1, 1, 1, 1, 2};
        int capacity2 = 4;
        int result2 = solution.demonstrateExample(weights2, capacity2);
        System.out.println("Expected: 4");
        System.out.println("Actual:   " + result2);
        System.out.println();

        // Additional quick checks for clarity.

        // Single item fits exactly.
        int[] weights3 = {5};
        int capacity3 = 5;
        int result3 = solution.demonstrateExample(weights3, capacity3);
        System.out.println("Expected: 1");
        System.out.println("Actual:   " + result3);
        System.out.println();

        // No positive-weight item fits when capacity is 0, but zero-weight items would fit.
        int[] weights4 = {1, 2, 3};
        int capacity4 = 0;
        int result4 = solution.demonstrateExample(weights4, capacity4);
        System.out.println("Expected: 0");
        System.out.println("Actual:   " + result4);
        System.out.println();

        // Zero weights can create long valid windows even with zero capacity.
        int[] weights5 = {0, 0, 0, 1, 0};
        int capacity5 = 0;
        int result5 = solution.demonstrateExample(weights5, capacity5);
        System.out.println("Expected: 3");
        System.out.println("Actual:   " + result5);
    }
}