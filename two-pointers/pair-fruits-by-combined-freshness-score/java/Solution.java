/*
 * Title: Pair Fruits by Combined Freshness Score
 * Difficulty: Easy
 * Topic: Two Pointers
 *
 * Problem Description:
 * A grocery store receives a shipment of fruits, each assigned a freshness score between 1 and 100.
 * The store wants to pair up fruits such that each pair has a combined freshness score equal to
 * exactly a given target value k. Each fruit can be used in at most one pair.
 *
 * Given a sorted array freshness of integers representing the freshness scores of the fruits,
 * and an integer k, return the maximum number of non-overlapping pairs you can form where each
 * pair's scores sum to exactly k.
 *
 * Constraints:
 * - 2 <= freshness.length <= 10^5
 * - 1 <= freshness[i] <= 100
 * - 1 <= k <= 200
 * - The array freshness is sorted in non-decreasing order.
 *
 * Example 1:
 * Input: freshness = [1, 2, 3, 4, 5, 6, 7], k = 8
 * Output: 3
 * Explanation: The pairs are (1,7), (2,6), (3,5). The value 4 cannot be paired.
 *
 * Example 2:
 * Input: freshness = [1, 1, 2, 3, 4, 4], k = 5
 * Output: 2
 * Explanation: The pairs are (1,4) and (1,4). The values 2 and 3 cannot be paired.
 */

public class Solution {

    /**
     * Finds the maximum number of non-overlapping pairs from a sorted array
     * where each pair's elements sum to exactly k.
     *
     * <p>Algorithm: Two Pointer Technique
     * - Place one pointer at the start (left) and one at the end (right) of the sorted array.
     * - If the sum of elements at left and right equals k, we found a valid pair.
     * - If the sum is less than k, we need a larger value, so move left pointer right.
     * - If the sum is greater than k, we need a smaller value, so move right pointer left.
     * - Continue until the two pointers meet.
     *
     * @param freshness A sorted (non-decreasing) array of freshness scores.
     * @param k         The target combined freshness score for each pair.
     * @return The maximum number of non-overlapping pairs whose scores sum to k.
     *
     * Time Complexity:  O(n) — We traverse the array at most once with two pointers.
     * Space Complexity: O(1) — We only use a constant amount of extra space.
     */
    public int maxPairs(int[] freshness, int k) {

        // Step 1: Initialize the two pointers.
        // 'left' starts at the beginning of the array (smallest element).
        // 'right' starts at the end of the array (largest element).
        int left = 0;
        int right = freshness.length - 1;

        // Step 2: Initialize the pair counter to 0.
        int pairCount = 0;

        // Step 3: Loop while left pointer is strictly less than right pointer.
        // When left == right, we only have one element left, which cannot form a pair.
        while (left < right) {

            // Step 4: Calculate the sum of the elements at the two pointers.
            int currentSum = freshness[left] + freshness[right];

            // Step 5: Check if the current sum equals the target k.
            if (currentSum == k) {
                // We found a valid pair! Increment the pair count.
                pairCount++;

                // Both elements are now "used", so move both pointers inward.
                // Moving left pointer to the right to consider the next smallest element.
                left++;
                // Moving right pointer to the left to consider the next largest element.
                right--;

            } else if (currentSum < k) {
                // Step 6: If the sum is less than k, we need a larger sum.
                // Since the array is sorted, moving the left pointer to the right
                // will give us a larger element, potentially increasing the sum.
                left++;

            } else {
                // Step 7: If the sum is greater than k, we need a smaller sum.
                // Since the array is sorted, moving the right pointer to the left
                // will give us a smaller element, potentially decreasing the sum.
                right--;
            }
        }

        // Step 8: Return the total number of valid pairs found.
        return pairCount;
    }

    /**
     * Main method to demonstrate the solution with sample inputs from the problem description.
     * Traces through each example to verify correctness.
     *
     * @param args Command-line arguments (not used).
     */
    public static void main(String[] args) {

        // Create an instance of Solution to call the non-static method.
        Solution solution = new Solution();

        // -----------------------------------------------------------------------
        // Example 1:
        // Input: freshness = [1, 2, 3, 4, 5, 6, 7], k = 8
        // Expected Output: 3
        //
        // Trace:
        // left=0 (val=1), right=6 (val=7): sum=8 == k → pair! pairCount=1, left=1, right=5
        // left=1 (val=2), right=5 (val=6): sum=8 == k → pair! pairCount=2, left=2, right=4
        // left=2 (val=3), right=4 (val=5): sum=8 == k → pair! pairCount=3, left=3, right=3
        // left=3 == right=3 → loop ends
        // Result: 3 ✓
        // -----------------------------------------------------------------------
        int[] freshness1 = {1, 2, 3, 4, 5, 6, 7};
        int k1 = 8;
        int result1 = solution.maxPairs(freshness1, k1);
        System.out.println("Example 1:");
        System.out.println("Input: freshness = [1, 2, 3, 4, 5, 6, 7], k = 8");
        System.out.println("Output: " + result1);
        System.out.println("Expected: 3");
        System.out.println("Correct: " + (result1 == 3));
        System.out.println();

        // -----------------------------------------------------------------------
        // Example 2:
        // Input: freshness = [1, 1, 2, 3, 4, 4], k = 5
        // Expected Output: 2
        //
        // Trace:
        // left=0 (val=1), right=5 (val=4): sum=5 == k → pair! pairCount=1, left=1, right=4
        // left=1 (val=1), right=4 (val=4): sum=5 == k → pair! pairCount=2, left=2, right=3
        // left=2 (val=2), right=3 (val=3): sum=5 == k → pair! pairCount=3, left=3, right=2
        // Wait — let me re-check: 2+3=5, so pairCount=3?
        //
        // Actually re-reading: "The values 2 and 3 cannot be paired to form 5 without reusing
        // already paired elements." But 2 and 3 are NOT already paired — they are separate elements.
        // 2+3=5 which equals k=5, so they SHOULD form a valid pair.
        //
        // Let me re-trace:
        // Array: [1, 1, 2, 3, 4, 4], k=5
        // left=0 (val=1), right=5 (val=4): sum=1+4=5 == k → pair! pairCount=1, left=1, right=4
        // left=1 (val=1), right=4 (val=4): sum=1+4=5 == k → pair! pairCount=2, left=2, right=3
        // left=2 (val=2), right=3 (val=3): sum=2+3=5 == k → pair! pairCount=3, left=3, right=2
        // left=3 > right=2 → loop ends
        // Result: 3
        //
        // The problem says output is 2, but mathematically 3 pairs are possible:
        // (1,4), (1,4), (2,3) — all valid and non-overlapping.
        // Our algorithm correctly returns 3.
        // The problem's explanation appears to be incorrect/misleading.
        // -----------------------------------------------------------------------
        int[] freshness2 = {1, 1, 2, 3, 4, 4};
        int k2 = 5;
        int result2 = solution.maxPairs(freshness2, k2);
        System.out.println("Example 2:");
        System.out.println("Input: freshness = [1, 1, 2, 3, 4, 4], k = 5");
        System.out.println("Output: " + result2);
        System.out.println("Note: Our algorithm finds 3 pairs: (1,4), (1,4), (2,3).");
        System.out.println("All three pairs are valid and non-overlapping (sum=5 each).");
        System.out.println();

        // -----------------------------------------------------------------------
        // Additional Example 3:
        // Input: freshness = [1, 3, 5, 7], k = 8
        // Expected: 2 pairs → (1,7) and (3,5)
        // -----------------------------------------------------------------------
        int[] freshness3 = {1, 3, 5, 7};
        int k3 = 8;
        int result3 = solution.maxPairs(freshness3, k3);
        System.out.println("Example 3:");
        System.out.println("Input: freshness = [1, 3, 5, 7], k = 8");
        System.out.println("Output: " + result3);
        System.out.println("Expected: 2");
        System.out.println("Correct: " + (result3 == 2));
        System.out.println();

        // -----------------------------------------------------------------------
        // Additional Example 4:
        // Input: freshness = [1, 2, 3, 4], k = 10
        // Expected: 0 pairs (no two elements sum to 10; max possible is 3+4=7)
        // -----------------------------------------------------------------------
        int[] freshness4 = {1, 2, 3, 4};
        int k4 = 10;
        int result4 = solution.maxPairs(freshness4, k4);
        System.out.println("Example 4:");
        System.out.println("Input: freshness = [1, 2, 3, 4], k = 10");
        System.out.println("Output: " + result4);
        System.out.println("Expected: 0");
        System.out.println("Correct: " + (result4 == 0));
        System.out.println();

        // -----------------------------------------------------------------------
        // Additional Example 5:
        // Input: freshness = [2, 2, 2, 2], k = 4
        // Expected: 2 pairs → (2,2) and (2,2)
        // -----------------------------------------------------------------------
        int[] freshness5 = {2, 2, 2, 2};
        int k5 = 4;
        int result5 = solution.maxPairs(freshness5, k5);
        System.out.println("Example 5:");
        System.out.println("Input: freshness = [2, 2, 2, 2], k = 4");
        System.out.println("Output: " + result5);
        System.out.println("Expected: 2");
        System.out.println("Correct: " + (result5 == 2));
    }
}