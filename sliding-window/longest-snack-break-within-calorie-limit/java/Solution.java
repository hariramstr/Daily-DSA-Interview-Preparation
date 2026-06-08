/*
 * Title: Longest Snack Break Within Calorie Limit
 * Difficulty: Easy
 * Topic: Sliding Window
 *
 * Problem Description:
 * You are tracking the snacks eaten by an employee during a workday. Each snack is
 * represented by its calorie count in an integer array `calories`, where `calories[i]`
 * is the calorie count of the i-th snack consumed in order.
 *
 * An employee is allowed a continuous snack break, which is defined as a contiguous
 * subarray of snacks. The break is considered 'within budget' if the total calories
 * of all snacks in that break does not exceed a given limit `maxCalories`.
 *
 * Return the maximum number of snacks the employee can eat in a single contiguous
 * snack break without exceeding the calorie limit.
 *
 * If no single snack fits within the limit, return 0.
 *
 * Constraints:
 * - 1 <= calories.length <= 10^5
 * - 1 <= calories[i] <= 1000
 * - 1 <= maxCalories <= 10^7
 *
 * Example 1:
 * Input: calories = [100, 200, 150, 50, 300, 80], maxCalories = 400
 * Output: 3
 * Explanation: The subarray [200, 150, 50] has a total of 400 calories and contains
 * 3 snacks, which is the longest valid contiguous break.
 *
 * Example 2:
 * Input: calories = [500, 600, 700], maxCalories = 400
 * Output: 0
 * Explanation: Every individual snack exceeds the calorie limit, so no valid break exists.
 */

public class Solution {

    /**
     * Finds the maximum number of snacks that can be eaten in a single contiguous
     * snack break without exceeding the calorie limit.
     *
     * <p>This method uses the Sliding Window technique:
     * - We maintain a window [left, right] representing the current contiguous subarray.
     * - We expand the window by moving 'right' forward and adding calories.
     * - When the window's total calories exceed maxCalories, we shrink it from the left.
     * - At each step, we track the maximum window size seen so far.
     *
     * @param calories    an integer array where calories[i] is the calorie count of the i-th snack
     * @param maxCalories the maximum total calories allowed in a single contiguous snack break
     * @return the maximum number of snacks in a valid contiguous break, or 0 if none exists
     *
     * Time Complexity:  O(n) — each element is added and removed from the window at most once,
     *                   so the total work is proportional to the length of the array.
     * Space Complexity: O(1) — we only use a constant number of integer variables.
     */
    public int maxSnacks(int[] calories, int maxCalories) {

        // 'left' is the start index of our sliding window
        int left = 0;

        // 'currentSum' tracks the total calories in the current window [left, right]
        int currentSum = 0;

        // 'maxSnacks' stores the best (largest) valid window size found so far
        int maxSnacksCount = 0;

        // Step 1: Iterate through the array with 'right' as the expanding end of the window.
        // We try to include each snack one by one into our current break.
        for (int right = 0; right < calories.length; right++) {

            // Step 2: Expand the window by including the snack at index 'right'.
            // Add its calorie count to the running total.
            currentSum += calories[right];

            // Step 3: Check if the current window exceeds the calorie limit.
            // If it does, we need to shrink the window from the left side until
            // the total is within the limit again.
            while (currentSum > maxCalories) {
                // Remove the calorie contribution of the snack at 'left'
                // because we are sliding the left boundary forward.
                currentSum -= calories[left];

                // Move the left boundary one step to the right,
                // effectively removing the leftmost snack from the window.
                left++;
            }

            // Step 4: At this point, the window [left, right] is valid
            // (i.e., currentSum <= maxCalories).
            // Calculate the number of snacks in this window: (right - left + 1).
            // Update maxSnacksCount if this window is larger than any we've seen before.
            int windowSize = right - left + 1;
            if (windowSize > maxSnacksCount) {
                maxSnacksCount = windowSize;
            }
        }

        // Step 5: Return the maximum valid window size found.
        // If no single snack was within the limit, maxSnacksCount remains 0.
        return maxSnacksCount;
    }

    /**
     * Main method to demonstrate the solution with sample inputs from the problem description.
     * Traces through each example and prints the result.
     *
     * @param args command-line arguments (not used)
     */
    public static void main(String[] args) {
        // Create an instance of Solution to call the non-static method
        Solution solution = new Solution();

        // -----------------------------------------------------------------------
        // Example 1:
        // calories = [100, 200, 150, 50, 300, 80], maxCalories = 400
        // Expected Output: 3
        //
        // Trace through the sliding window:
        // right=0: window=[100],           sum=100,  valid, size=1, max=1
        // right=1: window=[100,200],        sum=300,  valid, size=2, max=2
        // right=2: window=[100,200,150],    sum=450,  EXCEEDS 400
        //          shrink: remove 100 -> sum=350, left=1
        //          window=[200,150],         sum=350,  valid, size=2, max=2
        // right=3: window=[200,150,50],     sum=400,  valid, size=3, max=3
        // right=4: window=[200,150,50,300], sum=700,  EXCEEDS 400
        //          shrink: remove 200 -> sum=500, left=2
        //          shrink: remove 150 -> sum=350, left=3
        //          window=[50,300],          sum=350,  valid, size=2, max=3
        // right=5: window=[50,300,80],      sum=430,  EXCEEDS 400
        //          shrink: remove 50 -> sum=380, left=4
        //          window=[300,80],          sum=380,  valid, size=2, max=3
        // Final answer: 3 ✓
        // -----------------------------------------------------------------------
        int[] calories1 = {100, 200, 150, 50, 300, 80};
        int maxCalories1 = 400;
        int result1 = solution.maxSnacks(calories1, maxCalories1);
        System.out.println("Example 1:");
        System.out.println("  Input:  calories = [100, 200, 150, 50, 300, 80], maxCalories = 400");
        System.out.println("  Output: " + result1);
        System.out.println("  Expected: 3");
        System.out.println("  Correct: " + (result1 == 3));
        System.out.println();

        // -----------------------------------------------------------------------
        // Example 2:
        // calories = [500, 600, 700], maxCalories = 400
        // Expected Output: 0
        //
        // Trace through the sliding window:
        // right=0: window=[500], sum=500, EXCEEDS 400
        //          shrink: remove 500 -> sum=0, left=1
        //          window=[], size=0, max=0
        // right=1: window=[600], sum=600, EXCEEDS 400
        //          shrink: remove 600 -> sum=0, left=2
        //          window=[], size=0, max=0
        // right=2: window=[700], sum=700, EXCEEDS 400
        //          shrink: remove 700 -> sum=0, left=3
        //          window=[], size=0, max=0
        // Final answer: 0 ✓
        // -----------------------------------------------------------------------
        int[] calories2 = {500, 600, 700};
        int maxCalories2 = 400;
        int result2 = solution.maxSnacks(calories2, maxCalories2);
        System.out.println("Example 2:");
        System.out.println("  Input:  calories = [500, 600, 700], maxCalories = 400");
        System.out.println("  Output: " + result2);
        System.out.println("  Expected: 0");
        System.out.println("  Correct: " + (result2 == 0));
        System.out.println();

        // -----------------------------------------------------------------------
        // Additional Example 3: All snacks fit within the limit
        // calories = [50, 100, 75], maxCalories = 500
        // Expected Output: 3 (entire array fits)
        // -----------------------------------------------------------------------
        int[] calories3 = {50, 100, 75};
        int maxCalories3 = 500;
        int result3 = solution.maxSnacks(calories3, maxCalories3);
        System.out.println("Example 3 (all fit):");
        System.out.println("  Input:  calories = [50, 100, 75], maxCalories = 500");
        System.out.println("  Output: " + result3);
        System.out.println("  Expected: 3");
        System.out.println("  Correct: " + (result3 == 3));
        System.out.println();

        // -----------------------------------------------------------------------
        // Additional Example 4: Single element exactly at the limit
        // calories = [400], maxCalories = 400
        // Expected Output: 1
        // -----------------------------------------------------------------------
        int[] calories4 = {400};
        int maxCalories4 = 400;
        int result4 = solution.maxSnacks(calories4, maxCalories4);
        System.out.println("Example 4 (single element at limit):");
        System.out.println("  Input:  calories = [400], maxCalories = 400");
        System.out.println("  Output: " + result4);
        System.out.println("  Expected: 1");
        System.out.println("  Correct: " + (result4 == 1));
    }
}