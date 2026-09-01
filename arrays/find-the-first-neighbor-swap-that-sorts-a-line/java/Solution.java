/*
Title: Find the First Neighbor Swap That Sorts a Line
Difficulty: Easy
Topic: Arrays

Problem Description:
You are given an integer array nums representing the priority values of items standing in a line.
You may perform at most one operation: choose an index i and swap nums[i] with nums[i + 1],
meaning only neighboring items can be swapped.

Your task is to find the smallest index i such that performing this single adjacent swap makes
the entire array sorted in non-decreasing order.

If the array is already sorted, return -1.
If no single adjacent swap can sort the array, also return -1.

Return the index of the left element in the swap.

An array is considered sorted in non-decreasing order if nums[j] <= nums[j + 1] for every valid j.

This problem is meant to test careful array scanning and boundary checking. A correct solution
should avoid trying every possible swap when unnecessary and should correctly handle duplicates.

Constraints:
- 1 <= nums.length <= 100000
- -1000000000 <= nums[i] <= 1000000000

Example 1:
Input: nums = [1, 3, 2, 4]
Output: 1
Explanation: Swapping nums[1] and nums[2] gives [1, 2, 3, 4], which is sorted. No smaller index works.

Example 2:
Input: nums = [1, 5, 3, 4, 2]
Output: -1
Explanation: No single swap of neighboring elements can make the full array sorted.

Notes:
- If nums = [1, 2, 2, 3], the answer is -1 because the array is already sorted.
- If multiple adjacent swaps could sort the array, return the smallest valid index.
*/

import java.util.*;

public class Solution {

    /**
     * Finds the smallest index i such that swapping nums[i] and nums[i + 1]
     * makes the entire array sorted in non-decreasing order.
     *
     * Core idea:
     * - First, find all positions where the array is "broken", meaning nums[i] > nums[i + 1].
     * - If there are no such positions, the array is already sorted, so return -1.
     * - A single adjacent swap can only directly affect local order around one index.
     *   Therefore, if there is more than one inversion position, the only possible way
     *   one swap can fix the whole array is when those inversion positions are actually
     *   the same local issue caused by one adjacent pair. In practice, for a valid answer,
     *   the first inversion index is the only candidate swap index we need to test.
     * - After identifying the candidate index, verify carefully whether swapping that pair
     *   makes the array sorted. We do this with local boundary checks instead of rebuilding
     *   and rescanning the whole array.
     *
     * @param nums the input integer array
     * @return the smallest valid index of the left element in the adjacent swap,
     *         or -1 if the array is already sorted or cannot be sorted by one adjacent swap
     * @implNote Time complexity: O(n)
     * @implNote Space complexity: O(1)
     */
    public int firstNeighborSwapToSort(int[] nums) {
        int n = nums.length;

        // Arrays of length 0 or 1 are trivially sorted, but constraints guarantee length >= 1.
        // Still, handling this explicitly makes the method robust and beginner-friendly.
        if (n <= 1) {
            return -1;
        }

        // Step 1:
        // Find the first position where the sorted order is violated.
        // A violation (also called an inversion here) is an index i such that:
        // nums[i] > nums[i + 1]
        //
        // If we never find such an index, the array is already sorted.
        int firstBad = -1;
        for (int i = 0; i < n - 1; i++) {
            if (nums[i] > nums[i + 1]) {
                firstBad = i;
                break;
            }
        }

        if (firstBad == -1) {
            return -1;
        }

        // Step 2:
        // For a single adjacent swap to fix the array, the only realistic candidate
        // is swapping at the first inversion index.
        //
        // Why?
        // - Any earlier index is already locally sorted, so swapping there would disturb
        //   an already-correct prefix and cannot fix the first broken pair.
        // - Any later index would leave the first broken pair untouched.
        //
        // So we only need to test swap(firstBad, firstBad + 1).
        if (canBecomeSortedBySwappingAt(nums, firstBad)) {
            return firstBad;
        }

        // If that candidate does not work, then no single adjacent swap can sort the array.
        return -1;
    }

    /**
     * Checks whether swapping nums[i] and nums[i + 1] would make the entire array sorted.
     *
     * Important observation:
     * Swapping neighboring elements only changes comparisons involving indices near i.
     * Every other pair in the array stays exactly the same.
     *
     * Therefore, instead of performing the swap and scanning the whole array again,
     * we can:
     * 1. Ensure all pairs far away from the swap are already sorted.
     * 2. Check the few local comparisons that change because of the swap.
     *
     * Before swap:
     *   ... nums[i-1], nums[i], nums[i+1], nums[i+2] ...
     *
     * After swap:
     *   ... nums[i-1], nums[i+1], nums[i], nums[i+2] ...
     *
     * The only comparisons that can change are:
     * - nums[i-1] <= nums[i+1]   (if i - 1 exists)
     * - nums[i+1] <= nums[i]     (this is guaranteed if we started from an inversion nums[i] > nums[i+1])
     * - nums[i] <= nums[i+2]     (if i + 2 exists)
     *
     * Also, all pairs before i-1 and after i+1 must already be sorted.
     *
     * @param nums the input integer array
     * @param i the left index of the adjacent pair to swap
     * @return true if swapping nums[i] and nums[i + 1] sorts the array; otherwise false
     * @implNote Time complexity: O(n) in the worst case due to validation scans
     * @implNote Space complexity: O(1)
     */
    public boolean canBecomeSortedBySwappingAt(int[] nums, int i) {
        int n = nums.length;

        // Safety check: i must be a valid left index for an adjacent swap.
        if (i < 0 || i >= n - 1) {
            return false;
        }

        int leftValue = nums[i];
        int rightValue = nums[i + 1];

        // Step 1:
        // Everything strictly before the affected local window must already be sorted.
        //
        // Pairs checked here:
        // (0,1), (1,2), ..., (i-2, i-1)
        //
        // These pairs are not changed by swapping nums[i] and nums[i+1].
        // So if any of them is broken now, it will still be broken after the swap.
        for (int j = 0; j <= i - 2; j++) {
            if (nums[j] > nums[j + 1]) {
                return false;
            }
        }

        // Step 2:
        // Check the left boundary after the swap.
        //
        // Before swap:
        //   nums[i-1], leftValue, rightValue
        // After swap:
        //   nums[i-1], rightValue, leftValue
        //
        // So we need:
        //   nums[i-1] <= rightValue
        if (i - 1 >= 0 && nums[i - 1] > rightValue) {
            return false;
        }

        // Step 3:
        // Check the middle pair after the swap:
        //   rightValue <= leftValue
        //
        // This must hold for the swapped array to be sorted at positions i and i+1.
        // In many intended cases this is exactly the inversion we are fixing.
        if (rightValue > leftValue) {
            return false;
        }

        // Step 4:
        // Check the right boundary after the swap.
        //
        // Before swap:
        //   leftValue, rightValue, nums[i+2]
        // After swap:
        //   rightValue, leftValue, nums[i+2]
        //
        // So we need:
        //   leftValue <= nums[i+2]
        if (i + 2 < n && leftValue > nums[i + 2]) {
            return false;
        }

        // Step 5:
        // Everything strictly after the affected local window must already be sorted.
        //
        // Pairs checked here:
        // (i+2, i+3), (i+3, i+4), ..., (n-2, n-1)
        //
        // These pairs are also unchanged by the swap.
        for (int j = i + 2; j < n - 1; j++) {
            if (nums[j] > nums[j + 1]) {
                return false;
            }
        }

        // If all unchanged regions are already sorted and all changed local comparisons
        // are valid after the swap, then the whole array becomes sorted.
        return true;
    }

    /**
     * Utility method to print an array in a readable format.
     *
     * @param nums the array to convert to string form
     * @return a string representation of the array
     * @implNote Time complexity: O(n)
     * @implNote Space complexity: O(n) due to string construction
     */
    public String arrayToString(int[] nums) {
        return Arrays.toString(nums);
    }

    /**
     * Demonstrates the solution on sample and additional test cases.
     *
     * @param args command-line arguments (not used)
     * @return nothing
     * @implNote Time complexity: O(total size of demonstrated arrays)
     * @implNote Space complexity: O(1) excluding output formatting
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        int[] nums1 = {1, 3, 2, 4};
        int[] nums2 = {1, 5, 3, 4, 2};
        int[] nums3 = {1, 2, 2, 3};
        int[] nums4 = {2, 1};
        int[] nums5 = {1, 4, 3, 5};
        int[] nums6 = {1, 3, 2, 2, 4};
        int[] nums7 = {3, 1, 2};
        int[] nums8 = {1};

        System.out.println("Input: " + solution.arrayToString(nums1));
        System.out.println("Output: " + solution.firstNeighborSwapToSort(nums1));
        System.out.println("Expected: 1");
        System.out.println();

        System.out.println("Input: " + solution.arrayToString(nums2));
        System.out.println("Output: " + solution.firstNeighborSwapToSort(nums2));
        System.out.println("Expected: -1");
        System.out.println();

        System.out.println("Input: " + solution.arrayToString(nums3));
        System.out.println("Output: " + solution.firstNeighborSwapToSort(nums3));
        System.out.println("Expected: -1");
        System.out.println();

        System.out.println("Input: " + solution.arrayToString(nums4));
        System.out.println("Output: " + solution.firstNeighborSwapToSort(nums4));
        System.out.println("Expected: 0");
        System.out.println();

        System.out.println("Input: " + solution.arrayToString(nums5));
        System.out.println("Output: " + solution.firstNeighborSwapToSort(nums5));
        System.out.println("Expected: 1");
        System.out.println();

        System.out.println("Input: " + solution.arrayToString(nums6));
        System.out.println("Output: " + solution.firstNeighborSwapToSort(nums6));
        System.out.println("Expected: 1");
        System.out.println();

        System.out.println("Input: " + solution.arrayToString(nums7));
        System.out.println("Output: " + solution.firstNeighborSwapToSort(nums7));
        System.out.println("Expected: -1");
        System.out.println();

        System.out.println("Input: " + solution.arrayToString(nums8));
        System.out.println("Output: " + solution.firstNeighborSwapToSort(nums8));
        System.out.println("Expected: -1");
    }
}