/*
 * Zigzag Array Reconstruction
 * ============================
 * Given an integer array nums, rearrange its elements so that the resulting
 * array follows a zigzag pattern. A zigzag pattern means every element at an
 * even index is less than or equal to its neighbors, and every element at an
 * odd index is greater than or equal to its neighbors. Formally:
 *
 *   nums[0] <= nums[1] >= nums[2] <= nums[3] >= nums[4] ...
 *
 * Return the rearranged array. If multiple valid answers exist, return any of them.
 *
 * Note: You may not sort the array before rearranging; instead, perform the
 * rearrangement in a single pass by swapping adjacent elements where necessary.
 *
 * Constraints:
 *   - 1 <= nums.length <= 10^4
 *   - 0 <= nums[i] <= 10^5
 *   - It is guaranteed that a valid zigzag arrangement always exists for the given input.
 *
 * Example 1:
 *   Input:  nums = [4, 3, 7, 8, 6, 2, 1]
 *   Output: [3, 7, 4, 8, 2, 6, 1]
 *   Explanation: 3 <= 7 >= 4 <= 8 >= 2 <= 6 >= 1
 *
 * Example 2:
 *   Input:  nums = [1, 2, 3]
 *   Output: [1, 3, 2]
 *   Explanation: 1 <= 3 >= 2
 */

import java.util.Arrays;

/**
 * Solution class for the Zigzag Array Reconstruction problem.
 *
 * <p>Core Idea:
 * We iterate through the array and at each index i, we check whether the
 * current relationship between nums[i] and nums[i+1] satisfies the required
 * zigzag condition:
 *   - At even index i: we need nums[i] <= nums[i+1]
 *   - At odd  index i: we need nums[i] >= nums[i+1]
 *
 * If the condition is violated, we simply swap nums[i] and nums[i+1].
 * This greedy single-pass approach works because swapping two adjacent
 * elements to fix a local violation never breaks a previously fixed condition.
 */
public class Solution {

    /**
     * Rearranges the given array in-place into a zigzag pattern using a single pass.
     *
     * <p>Algorithm walkthrough:
     * <ol>
     *   <li>Iterate i from 0 to nums.length - 2 (we compare pairs).</li>
     *   <li>Determine whether the current index i is even or odd.</li>
     *   <li>At even i: we want nums[i] <= nums[i+1]. If nums[i] > nums[i+1], swap.</li>
     *   <li>At odd  i: we want nums[i] >= nums[i+1]. If nums[i] < nums[i+1], swap.</li>
     *   <li>Return the modified array.</li>
     * </ol>
     *
     * <p>Why does swapping never break a previously satisfied condition?
     * When we swap nums[i] and nums[i+1], the element that moves to position i
     * was already compared with nums[i-1] in the previous iteration (if i > 0).
     * Because we only swap when the condition is violated, the swapped element
     * at position i will still satisfy the condition with nums[i-1]:
     *   - If i is even (we want nums[i] <= nums[i+1]):
     *       Previous step (i-1 is odd) ensured nums[i-1] >= nums[i].
     *       After swap, nums[i] becomes the old nums[i+1] which was < old nums[i],
     *       so nums[i-1] >= old nums[i] > new nums[i] — condition still holds.
     *   - Similarly for odd i.
     *
     * @param nums the input integer array to be rearranged
     * @return the same array rearranged in zigzag order (in-place modification)
     *
     * @implNote Time Complexity:  O(n) — single pass through the array
     *           Space Complexity: O(1) — only a temporary variable for swapping
     */
    public int[] zigzag(int[] nums) {

        // ---------------------------------------------------------------
        // Step 1: Handle edge case — if the array has 0 or 1 element,
        //         it is trivially in zigzag order; return immediately.
        // ---------------------------------------------------------------
        if (nums == null || nums.length <= 1) {
            return nums;
        }

        // ---------------------------------------------------------------
        // Step 2: Single-pass loop over all adjacent pairs (i, i+1).
        //         We stop at index nums.length - 2 because we look ahead
        //         one position (nums[i+1]).
        // ---------------------------------------------------------------
        for (int i = 0; i < nums.length - 1; i++) {

            // -----------------------------------------------------------
            // Step 3: Determine the required relationship at index i.
            //
            //   Even index i → "valley": nums[i] <= nums[i+1]
            //   Odd  index i → "peak":   nums[i] >= nums[i+1]
            //
            //   We use (i % 2 == 0) to detect even indices.
            // -----------------------------------------------------------
            boolean isEvenIndex = (i % 2 == 0);

            if (isEvenIndex) {
                // -------------------------------------------------------
                // Step 4a: At an even index we need nums[i] <= nums[i+1].
                //          If nums[i] > nums[i+1], the condition is violated
                //          → swap the two elements to fix it.
                // -------------------------------------------------------
                if (nums[i] > nums[i + 1]) {
                    // Swap nums[i] and nums[i+1]
                    int temp = nums[i];
                    nums[i]     = nums[i + 1];
                    nums[i + 1] = temp;
                }
                // If nums[i] <= nums[i+1] already, no action needed.

            } else {
                // -------------------------------------------------------
                // Step 4b: At an odd index we need nums[i] >= nums[i+1].
                //          If nums[i] < nums[i+1], the condition is violated
                //          → swap the two elements to fix it.
                // -------------------------------------------------------
                if (nums[i] < nums[i + 1]) {
                    // Swap nums[i] and nums[i+1]
                    int temp = nums[i];
                    nums[i]     = nums[i + 1];
                    nums[i + 1] = temp;
                }
                // If nums[i] >= nums[i+1] already, no action needed.
            }
        }

        // ---------------------------------------------------------------
        // Step 5: Return the in-place modified array.
        // ---------------------------------------------------------------
        return nums;
    }

    /**
     * Verifies that the given array satisfies the zigzag condition.
     *
     * <p>This helper is used in main() to validate our results.
     *
     * @param nums the array to verify
     * @return {@code true} if the array is in valid zigzag order, {@code false} otherwise
     *
     * @implNote Time Complexity:  O(n)
     *           Space Complexity: O(1)
     */
    public boolean isValidZigzag(int[] nums) {
        if (nums == null || nums.length <= 1) {
            return true; // trivially valid
        }

        for (int i = 0; i < nums.length - 1; i++) {
            if (i % 2 == 0) {
                // Even index: must be a valley — nums[i] <= nums[i+1]
                if (nums[i] > nums[i + 1]) {
                    return false;
                }
            } else {
                // Odd index: must be a peak — nums[i] >= nums[i+1]
                if (nums[i] < nums[i + 1]) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Demonstrates the zigzag rearrangement with several test cases,
     * prints the results, and validates correctness.
     *
     * @param args command-line arguments (not used)
     */
    public static void main(String[] args) {

        Solution solution = new Solution();

        // ==============================================================
        // Test Case 1 — from the problem description
        // Input:    [4, 3, 7, 8, 6, 2, 1]
        // Expected: any valid zigzag, e.g. [3, 7, 4, 8, 2, 6, 1]
        //
        // Let's trace through manually:
        //   i=0 (even): need nums[0] <= nums[1] → 4 <= 3? NO → swap → [3, 4, 7, 8, 6, 2, 1]
        //   i=1 (odd):  need nums[1] >= nums[2] → 4 >= 7? NO → swap → [3, 7, 4, 8, 6, 2, 1]
        //   i=2 (even): need nums[2] <= nums[3] → 4 <= 8? YES → no swap
        //   i=3 (odd):  need nums[3] >= nums[4] → 8 >= 6? YES → no swap
        //   i=4 (even): need nums[4] <= nums[5] → 6 <= 2? NO → swap → [3, 7, 4, 8, 2, 6, 1]
        //   i=5 (odd):  need nums[5] >= nums[6] → 6 >= 1? YES → no swap
        // Result: [3, 7, 4, 8, 2, 6, 1]  ✓
        // ==============================================================
        int[] test1 = {4, 3, 7, 8, 6, 2, 1};
        System.out.println("=== Test Case 1 ===");
        System.out.println("Input:  " + Arrays.toString(test1));
        int[] result1 = solution.zigzag(test1);
        System.out.println("Output: " + Arrays.toString(result1));
        System.out.println("Valid zigzag? " + solution.isValidZigzag(result1));
        System.out.println();

        // ==============================================================
        // Test Case 2 — from the problem description
        // Input:    [1, 2, 3]
        // Expected: any valid zigzag, e.g. [1, 3, 2]
        //
        // Trace:
        //   i=0 (even): need nums[0] <= nums[1] → 1 <= 2? YES → no swap
        //   i=1 (odd):  need nums[1] >= nums[2] → 2 >= 3? NO → swap → [1, 3, 2]
        // Result: [1, 3, 2]  ✓
        // ==============================================================
        int[] test2 = {1, 2, 3};
        System.out.println("=== Test Case 2 ===");
        System.out.println("Input:  " + Arrays.toString(test2));
        int[] result2 = solution.zigzag(test2);
        System.out.println("Output: " + Arrays.toString(result2));
        System.out.println("Valid zigzag? " + solution.isValidZigzag(result2));
        System.out.println();

        // ==============================================================
        // Test Case 3 — single element (edge case)
        // ==============================================================
        int[] test3 = {42};
        System.out.println("=== Test Case 3 (single element) ===");
        System.out.println("Input:  " + Arrays.toString(test3));
        int[] result3 = solution.zigzag(test3);
        System.out.println("Output: " + Arrays.toString(result3));
        System.out.println("Valid zigzag? " + solution.isValidZigzag(result3));
        System.out.println();

        // ==============================================================
        // Test Case 4 — two elements
        // Input:    [5, 3]
        // Trace:
        //   i=0 (even): need nums[0] <= nums[1] → 5 <= 3? NO → swap → [3, 5]
        // Result: [3, 5]  ✓
        // ==============================================================
        int[] test4 = {5, 3};
        System.out.println("=== Test Case 4 (two elements) ===");
        System.out.println("Input:  " + Arrays.toString(test4));
        int[] result4 = solution.zigzag(test4);
        System.out.println("Output: " + Arrays.toString(result4));
        System.out.println("Valid zigzag? " + solution.isValidZigzag(result4));
        System.out.println();

        // ==============================================================
        // Test Case 5 — already in zigzag order
        // Input:    [1, 5, 2, 6, 3]
        // Trace:
        //   i=0 (even): 1 <= 5? YES
        //   i=1 (odd):  5 >= 2? YES
        //   i=2 (even): 2 <= 6? YES
        //   i=3 (odd):  6 >= 3? YES
        // No swaps needed. Result: [1, 5, 2, 6, 3]  ✓
        // ==============================================================
        int[] test5 = {1, 5, 2, 6, 3};
        System.out.println("=== Test Case 5 (already zigzag) ===");
        System.out.println("Input:  " + Arrays.toString(test5));
        int[] result5 = solution.zigzag(test5);
        System.out.println("Output: " + Arrays.toString(result5));
        System.out.println("Valid zigzag? " + solution.isValidZigzag(result5));
        System.out.println();

        // ==============================================================
        // Test Case 6 — all equal elements
        // Input:    [3, 3, 3, 3]
        // All comparisons are equal, so no swaps needed.
        // Result: [3, 3, 3, 3]  ✓ (3<=3>=3<=3)
        // ==============================================================
        int[] test6 = {3, 3, 3, 3};
        System.out.println("=== Test Case 6 (all equal) ===");
        System.out.println("Input:  " + Arrays.toString(test6));
        int[] result6 = solution.zigzag(test6);
        System.out.println("Output: " + Arrays.toString(result6));
        System.out.println("Valid zigzag? " + solution.isValidZigzag(result6));
        System.out.println();

        // ==============================================================
        // Test Case 7 — descending order
        // Input:    [5, 4, 3, 2, 1]
        // Trace:
        //   i=0 (even): 5 <= 4? NO → swap → [4, 5, 3, 2, 1]
        //   i=1 (odd):  5 >= 3? YES
        //   i=2 (even): 3 <= 2? NO → swap → [4, 5, 2, 3, 1]
        //   i=3 (odd):  3 >= 1? YES
        // Result: [4, 5, 2, 3, 1]  → 4<=5>=