/*
 * Odd Bit Pair Swapper
 *
 * Problem Description:
 * You are given an array of non-negative integers `nums`. Your task is to transform each integer
 * in the array by swapping every pair of adjacent bits. Specifically, for each integer, the bit
 * at position 0 swaps with the bit at position 1, the bit at position 2 swaps with the bit at
 * position 3, and so on for all 32 bits.
 *
 * After transforming all integers, return the count of pairs (i, j) where i < j such that the
 * XOR of nums[i] and nums[j] (after transformation) has exactly k bits set to 1.
 *
 * Constraints:
 * - 1 <= nums.length <= 1000
 * - 0 <= nums[i] <= 2^30 - 1
 * - 0 <= k <= 30
 */

import java.util.*;

/**
 * Solution class for the Odd Bit Pair Swapper problem.
 *
 * <p>Core ideas:
 * 1. Swap adjacent bit pairs in each number using bitmask operations.
 * 2. Count pairs whose XOR has exactly k set bits using Integer.bitCount().
 */
public class Solution {

    /**
     * Swaps every pair of adjacent bits in a 32-bit integer.
     *
     * <p>Strategy:
     * - Extract all even-positioned bits (positions 0, 2, 4, ...) using mask 0x55555555
     *   (binary: ...0101 0101 0101 0101).
     * - Extract all odd-positioned bits (positions 1, 3, 5, ...) using mask 0xAAAAAAAA
     *   (binary: ...1010 1010 1010 1010).
     * - Shift even bits left by 1 (move them to odd positions).
     * - Shift odd bits right by 1 (move them to even positions).
     * - OR the two results together to form the swapped number.
     *
     * @param n the integer whose adjacent bit pairs are to be swapped
     * @return the integer after swapping every pair of adjacent bits
     *
     * Time Complexity:  O(1) — fixed number of bitwise operations regardless of input value
     * Space Complexity: O(1) — no extra space used
     */
    public int swapAdjacentBits(int n) {
        // Step 1: Define the mask for even-positioned bits (positions 0, 2, 4, ...)
        // 0x55555555 in binary = 0101 0101 0101 0101 0101 0101 0101 0101
        // This mask selects bits at positions 0, 2, 4, 6, ..., 30
        int evenMask = 0x55555555;

        // Step 2: Define the mask for odd-positioned bits (positions 1, 3, 5, ...)
        // 0xAAAAAAAA in binary = 1010 1010 1010 1010 1010 1010 1010 1010
        // This mask selects bits at positions 1, 3, 5, 7, ..., 31
        int oddMask = 0xAAAAAAAA;

        // Step 3: Extract the even-positioned bits from n
        // Example: n = 2 (binary: 10)
        //   n & evenMask = 10 & 01 = 00 (no even-position bits set)
        int evenBits = n & evenMask;

        // Step 4: Extract the odd-positioned bits from n
        // Example: n = 2 (binary: 10)
        //   n & oddMask = 10 & 10 = 10 (bit at position 1 is set)
        int oddBits = n & oddMask;

        // Step 5: Shift even bits LEFT by 1 to move them to odd positions
        // Example: evenBits = 00, shifted left = 00
        // Another example: n = 1 (binary: 01), evenBits = 01, shifted left = 10
        int evenShiftedLeft = evenBits << 1;

        // Step 6: Shift odd bits RIGHT by 1 to move them to even positions
        // We use unsigned right shift (>>>) to avoid sign extension issues
        // Example: oddBits = 10, shifted right = 01
        int oddShiftedRight = oddBits >>> 1;

        // Step 7: Combine the two shifted parts using OR
        // This gives us the number with all adjacent bit pairs swapped
        // Example: n = 2 (binary: 10) → evenShiftedLeft | oddShiftedRight = 00 | 01 = 01 = 1
        return evenShiftedLeft | oddShiftedRight;
    }

    /**
     * Counts the number of pairs (i, j) with i < j such that the XOR of the
     * transformed nums[i] and nums[j] has exactly k bits set to 1.
     *
     * <p>Algorithm:
     * 1. Transform every element in nums by swapping adjacent bit pairs.
     * 2. Use a nested loop to check every unique pair (i, j) with i < j.
     * 3. Compute XOR of the pair and count set bits using Integer.bitCount().
     * 4. If the popcount equals k, increment the result counter.
     *
     * @param nums the input array of non-negative integers
     * @param k    the exact number of set bits required in the XOR of a valid pair
     * @return the count of valid pairs after transformation
     *
     * Time Complexity:  O(n^2) — we examine every pair; n = nums.length
     * Space Complexity: O(n)   — we store the transformed array of size n
     */
    public int countPairs(int[] nums, int k) {
        int n = nums.length;

        // Step 1: Build the transformed array by swapping adjacent bits in each element
        int[] transformed = new int[n];
        for (int i = 0; i < n; i++) {
            transformed[i] = swapAdjacentBits(nums[i]);
            // Debug-friendly: you can uncomment the line below to trace transformations
            // System.out.println("nums[" + i + "] = " + nums[i] + " → transformed = " + transformed[i]);
        }

        // Step 2: Initialize the pair counter
        int count = 0;

        // Step 3: Iterate over all unique pairs (i, j) with i < j
        for (int i = 0; i < n - 1; i++) {
            for (int j = i + 1; j < n; j++) {

                // Step 4: Compute the XOR of the two transformed values
                // XOR produces a 1 bit wherever the two values differ
                int xorValue = transformed[i] ^ transformed[j];

                // Step 5: Count the number of set bits (1s) in the XOR result
                // Integer.bitCount() returns the number of 1-bits (Hamming weight / popcount)
                int setBits = Integer.bitCount(xorValue);

                // Step 6: If the popcount equals k, this is a valid pair — increment counter
                if (setBits == k) {
                    count++;
                }
            }
        }

        // Step 7: Return the total count of valid pairs
        return count;
    }

    /**
     * Main method demonstrating the solution with the provided examples.
     *
     * @param args command-line arguments (not used)
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // -----------------------------------------------------------------------
        // Example 1
        // Input:  nums = [2, 1, 3], k = 1
        // Expected transformed array: [1, 2, 3]
        //   2  (binary 10)  → swap adjacent bits → 01 = 1
        //   1  (binary 01)  → swap adjacent bits → 10 = 2
        //   3  (binary 11)  → swap adjacent bits → 11 = 3
        // XOR pairs:
        //   1 ^ 2 = 3  → bitCount(3)  = 2  (not k=1)
        //   1 ^ 3 = 2  → bitCount(2)  = 1  ✓
        //   2 ^ 3 = 1  → bitCount(1)  = 1  ✓
        // Expected output: 2
        // -----------------------------------------------------------------------
        int[] nums1 = {2, 1, 3};
        int k1 = 1;
        int result1 = solution.countPairs(nums1, k1);
        System.out.println("=== Example 1 ===");
        System.out.println("Input nums: " + Arrays.toString(nums1) + ", k = " + k1);

        // Print individual transformations for clarity
        System.out.print("Transformed: [");
        for (int i = 0; i < nums1.length; i++) {
            System.out.print(solution.swapAdjacentBits(nums1[i]));
            if (i < nums1.length - 1) System.out.print(", ");
        }
        System.out.println("]");

        System.out.println("Output (pair count): " + result1);
        System.out.println("Expected:            2");
        System.out.println("Correct: " + (result1 == 2));
        System.out.println();

        // -----------------------------------------------------------------------
        // Example 2
        // Input:  nums = [5, 10, 0], k = 2
        // Expected transformed array: [10, 5, 0]
        //   5  (binary 0101) → swap adjacent bits → 1010 = 10
        //   10 (binary 1010) → swap adjacent bits → 0101 = 5
        //   0  (binary 0000) → swap adjacent bits → 0000 = 0
        // XOR pairs:
        //   10 ^ 5  = 15 → bitCount(15) = 4  (not k=2)
        //   10 ^ 0  = 10 → bitCount(10) = 2  ✓
        //   5  ^ 0  = 5  → bitCount(5)  = 2  ✓
        // Expected output: 2
        // -----------------------------------------------------------------------
        int[] nums2 = {5, 10, 0};
        int k2 = 2;
        int result2 = solution.countPairs(nums2, k2);
        System.out.println("=== Example 2 ===");
        System.out.println("Input nums: " + Arrays.toString(nums2) + ", k = " + k2);

        System.out.print("Transformed: [");
        for (int i = 0; i < nums2.length; i++) {
            System.out.print(solution.swapAdjacentBits(nums2[i]));
            if (i < nums2.length - 1) System.out.print(", ");
        }
        System.out.println("]");

        System.out.println("Output (pair count): " + result2);
        System.out.println("Expected:            2");
        System.out.println("Correct: " + (result2 == 2));
        System.out.println();

        // -----------------------------------------------------------------------
        // Additional edge case: single element array → no pairs possible
        // -----------------------------------------------------------------------
        int[] nums3 = {7};
        int k3 = 1;
        int result3 = solution.countPairs(nums3, k3);
        System.out.println("=== Edge Case: Single Element ===");
        System.out.println("Input nums: " + Arrays.toString(nums3) + ", k = " + k3);
        System.out.println("Output (pair count): " + result3);
        System.out.println("Expected:            0");
        System.out.println("Correct: " + (result3 == 0));
        System.out.println();

        // -----------------------------------------------------------------------
        // Additional edge case: k = 0 means XOR must be 0, i.e., equal elements
        // nums = [4, 4, 4], k = 0
        // transformed: 4 (100) → 1000 = 8; all three become 8
        // pairs: (0,1), (0,2), (1,2) → XOR = 0 for all → bitCount = 0 = k
        // Expected: 3
        // -----------------------------------------------------------------------
        int[] nums4 = {4, 4, 4};
        int k4 = 0;
        int result4 = solution.countPairs(nums4, k4);
        System.out.println("=== Edge Case: k=0, all equal elements ===");
        System.out.println("Input nums: " + Arrays.toString(nums4) + ", k = " + k4);

        System.out.print("Transformed: [");
        for (int i = 0; i < nums4.length; i++) {
            System.out.print(solution.swapAdjacentBits(nums4[i]));
            if (i < nums4.length - 1) System.out.print(", ");
        }
        System.out.println("]");

        System.out.println("Output (pair count): " + result4);
        System.out.println("Expected:            3");
        System.out.println("Correct: " + (result4 == 3));
    }
}