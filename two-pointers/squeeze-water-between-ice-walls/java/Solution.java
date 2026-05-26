/*
 * Title: Squeeze Water Between Ice Walls
 * Difficulty: Easy
 * Topic: Two Pointers
 *
 * Problem Description:
 * You are given an array `heights` representing the heights of ice walls standing upright in a row.
 * A bucket of water is placed between any two walls at positions `i` and `j` (where `i < j`).
 * The amount of water the bucket can hold is determined by the shorter of the two walls multiplied
 * by the number of gaps between them: min(heights[i], heights[j]) * (j - i).
 *
 * Your task is to find the maximum amount of water that can be held between any two walls.
 *
 * Note: This problem is about choosing the optimal pair of walls, not filling every gap between them.
 *
 * Constraints:
 * - 2 <= heights.length <= 10^5
 * - 1 <= heights[i] <= 10^4
 *
 * Example 1:
 * Input: heights = [2, 7, 4, 1, 6, 3]
 * Output: 18
 * Explanation: Using walls at index 1 (height 7) and index 4 (height 6),
 *              water = min(7, 6) * (4 - 1) = 6 * 3 = 18.
 *
 * Example 2:
 * Input: heights = [3, 1, 2, 4, 5]
 * Output: 12
 * Explanation: Using walls at index 0 (height 3) and index 4 (height 5),
 *              water = min(3, 5) * (4 - 0) = 3 * 4 = 12.
 *
 * Approach: Two-pointer technique starting from both ends of the array.
 * Move the pointer pointing to the shorter wall inward at each step
 * to maximize the potential water held.
 */

public class Solution {

    /**
     * Finds the maximum amount of water that can be held between any two walls
     * using the two-pointer approach.
     *
     * <p>The key insight is:
     * - Water held = min(heights[left], heights[right]) * (right - left)
     * - We start with the widest possible container (left=0, right=n-1)
     * - At each step, we move the pointer with the shorter wall inward,
     *   because keeping the shorter wall can never give us a better result
     *   (the width decreases, and the height is still limited by the shorter wall).
     * - Moving the taller wall inward can only decrease or maintain the water amount,
     *   so we always move the shorter wall to try to find a taller replacement.
     *
     * @param heights an array of integers representing wall heights
     * @return the maximum amount of water that can be held between any two walls
     *
     * Time Complexity:  O(n) — we traverse the array once with two pointers
     * Space Complexity: O(1) — only a constant number of variables are used
     */
    public int maxWater(int[] heights) {
        // Step 1: Initialize two pointers — one at the leftmost wall, one at the rightmost wall.
        // This gives us the maximum possible width to start with.
        int left = 0;
        int right = heights.length - 1;

        // Step 2: Initialize the variable to track the maximum water found so far.
        int maxWater = 0;

        // Step 3: Loop until the two pointers meet.
        // When left >= right, there are no more pairs to evaluate.
        while (left < right) {
            // Step 4: Calculate the height of water for the current pair of walls.
            // The water level is limited by the SHORTER of the two walls.
            int currentHeight = Math.min(heights[left], heights[right]);

            // Step 5: Calculate the width between the two walls.
            // Width = distance between the two pointer positions.
            int currentWidth = right - left;

            // Step 6: Calculate the water held for this pair of walls.
            int currentWater = currentHeight * currentWidth;

            // Step 7: Update maxWater if the current water amount is greater.
            maxWater = Math.max(maxWater, currentWater);

            // Step 8: Move the pointer pointing to the SHORTER wall inward.
            // Reasoning: The water is limited by the shorter wall.
            //   - If we move the taller wall inward, the width decreases AND
            //     the height can only stay the same or decrease (still limited by shorter wall).
            //     So the water amount cannot increase.
            //   - If we move the shorter wall inward, the width decreases BUT
            //     we might find a taller wall that increases the height enough to compensate.
            //   Therefore, moving the shorter wall gives us the best chance of improvement.
            if (heights[left] <= heights[right]) {
                // Left wall is shorter (or equal) — move left pointer to the right
                left++;
            } else {
                // Right wall is shorter — move right pointer to the left
                right--;
            }
        }

        // Step 9: Return the maximum water found across all evaluated pairs.
        return maxWater;
    }

    /**
     * A brute-force solution for verification purposes.
     * Checks every possible pair of walls and returns the maximum water.
     *
     * @param heights an array of integers representing wall heights
     * @return the maximum amount of water that can be held between any two walls
     *
     * Time Complexity:  O(n^2) — checks every pair of walls
     * Space Complexity: O(1) — only a constant number of variables are used
     */
    public int maxWaterBruteForce(int[] heights) {
        int maxWater = 0;

        // Check every possible pair (i, j) where i < j
        for (int i = 0; i < heights.length - 1; i++) {
            for (int j = i + 1; j < heights.length; j++) {
                // Water = min height * width
                int water = Math.min(heights[i], heights[j]) * (j - i);
                maxWater = Math.max(maxWater, water);
            }
        }

        return maxWater;
    }

    /**
     * Main method to demonstrate the solution with sample inputs and verify correctness.
     *
     * @param args command-line arguments (not used)
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // -----------------------------------------------------------------------
        // Example 1: heights = [2, 7, 4, 1, 6, 3]
        // Expected Output: 18
        // Explanation: Walls at index 1 (height=7) and index 4 (height=6)
        //              water = min(7, 6) * (4 - 1) = 6 * 3 = 18
        // -----------------------------------------------------------------------
        System.out.println("=== Example 1 ===");
        int[] heights1 = {2, 7, 4, 1, 6, 3};
        System.out.print("Input heights: ");
        printArray(heights1);

        // Trace through the two-pointer algorithm for Example 1:
        // Initial: left=0 (h=2), right=5 (h=3), width=5, water=min(2,3)*5=10, max=10
        //   heights[left]=2 <= heights[right]=3, so move left → left=1
        // Step 2: left=1 (h=7), right=5 (h=3), width=4, water=min(7,3)*4=12, max=12
        //   heights[left]=7 > heights[right]=3, so move right → right=4
        // Step 3: left=1 (h=7), right=4 (h=6), width=3, water=min(7,6)*3=18, max=18
        //   heights[left]=7 > heights[right]=6, so move right → right=3
        // Step 4: left=1 (h=7), right=3 (h=1), width=2, water=min(7,1)*2=2, max=18
        //   heights[left]=7 > heights[right]=1, so move right → right=2
        // Step 5: left=1 (h=7), right=2 (h=4), width=1, water=min(7,4)*1=4, max=18
        //   heights[left]=7 > heights[right]=4, so move right → right=1
        // Now left=1 >= right=1, loop ends. Result = 18 ✓

        int result1 = solution.maxWater(heights1);
        int bruteForce1 = solution.maxWaterBruteForce(heights1);
        System.out.println("Two-Pointer Result: " + result1);
        System.out.println("Brute-Force Result: " + bruteForce1);
        System.out.println("Expected:           18");
        System.out.println("Correct: " + (result1 == 18 && bruteForce1 == 18));
        System.out.println();

        // -----------------------------------------------------------------------
        // Example 2: heights = [3, 1, 2, 4, 5]
        // Expected Output: 12
        // Explanation: Walls at index 0 (height=3) and index 4 (height=5)
        //              water = min(3, 5) * (4 - 0) = 3 * 4 = 12
        // -----------------------------------------------------------------------
        System.out.println("=== Example 2 ===");
        int[] heights2 = {3, 1, 2, 4, 5};
        System.out.print("Input heights: ");
        printArray(heights2);

        // Trace through the two-pointer algorithm for Example 2:
        // Initial: left=0 (h=3), right=4 (h=5), width=4, water=min(3,5)*4=12, max=12
        //   heights[left]=3 <= heights[right]=5, so move left → left=1
        // Step 2: left=1 (h=1), right=4 (h=5), width=3, water=min(1,5)*3=3, max=12
        //   heights[left]=1 <= heights[right]=5, so move left → left=2
        // Step 3: left=2 (h=2), right=4 (h=5), width=2, water=min(2,5)*2=4, max=12
        //   heights[left]=2 <= heights[right]=5, so move left → left=3
        // Step 4: left=3 (h=4), right=4 (h=5), width=1, water=min(4,5)*1=4, max=12
        //   heights[left]=4 <= heights[right]=5, so move left → left=4
        // Now left=4 >= right=4, loop ends. Result = 12 ✓

        int result2 = solution.maxWater(heights2);
        int bruteForce2 = solution.maxWaterBruteForce(heights2);
        System.out.println("Two-Pointer Result: " + result2);
        System.out.println("Brute-Force Result: " + bruteForce2);
        System.out.println("Expected:           12");
        System.out.println("Correct: " + (result2 == 12 && bruteForce2 == 12));
        System.out.println();

        // -----------------------------------------------------------------------
        // Additional Test Case: Uniform heights
        // heights = [5, 5, 5, 5, 5]
        // Best pair: index 0 and index 4 → min(5,5) * 4 = 20
        // -----------------------------------------------------------------------
        System.out.println("=== Additional Test: Uniform Heights ===");
        int[] heights3 = {5, 5, 5, 5, 5};
        System.out.print("Input heights: ");
        printArray(heights3);
        int result3 = solution.maxWater(heights3);
        int bruteForce3 = solution.maxWaterBruteForce(heights3);
        System.out.println("Two-Pointer Result: " + result3);
        System.out.println("Brute-Force Result: " + bruteForce3);
        System.out.println("Expected:           20");
        System.out.println("Correct: " + (result3 == 20 && bruteForce3 == 20));
        System.out.println();

        // -----------------------------------------------------------------------
        // Additional Test Case: Two walls only
        // heights = [4, 6]
        // Only one pair: min(4,6) * (1-0) = 4 * 1 = 4
        // -----------------------------------------------------------------------
        System.out.println("=== Additional Test: Two Walls Only ===");
        int[] heights4 = {4, 6};
        System.out.print("Input heights: ");
        printArray(heights4);
        int result4 = solution.maxWater(heights4);
        int bruteForce4 = solution.maxWaterBruteForce(heights4);
        System.out.println("Two-Pointer Result: " + result4);
        System.out.println("Brute-Force Result: " + bruteForce4);
        System.out.println("Expected:           4");
        System.out.println("Correct: " + (result4 == 4 && bruteForce4 == 4));
        System.out.println();

        // -----------------------------------------------------------------------
        // Additional Test Case: Increasing heights
        // heights = [1, 2, 3, 4, 5, 6]
        // Best pair: index 0 (h=1) and index 5 (h=6) → min(1,6)*5 = 5
        //            OR index 1 (h=2) and index 5 (h=6) → min(2,6)*4 = 8
        //            OR index 2 (h=3) and index 5 (h=6) → min(3,6)*3 = 9
        //            OR index 3 (h=4) and index 5 (h=6) → min(4,6)*2 = 8
        //            OR index 4 (h=5) and index 5 (h=6) → min(5,6)*1 = 5
        //            Best = 9
        // -----------------------------------------------------------------------
        System.out.println("=== Additional Test: Increasing Heights ===");
        int[] heights5 = {1, 2, 3, 4, 5, 6};
        System.out.print("Input heights: ");
        printArray(heights5);
        int result5 = solution.maxWater(heights5);
        int bruteForce5 = solution.maxWaterBruteForce(heights5);
        System.out.println("Two-Pointer Result: " + result5);
        System.out.println("Brute-Force Result: " + bruteForce5);
        System.out.println("Expected:           9");
        System.out.println("Correct: " + (result5 == 9 && bruteForce5 == 9));
    }

    /**
     * Helper method to print an integer array in a readable format.
     *
     * @param arr the array to print
     */
    private static void printArray(int[] arr) {
        System.out.print("[");
        for (int i = 0; i < arr.length; i++) {
            System.out.print(arr[i]);
            if (i < arr.length - 1) {
                System.out.print(", ");
            }
        }
        System.out.println("]");
    }
}