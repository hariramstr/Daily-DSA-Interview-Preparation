/*
Problem Title: Maximum Consecutive Days After One Schedule Swap

Problem Description:
You are given a binary array days where days[i] is 1 if an employee is scheduled to work on day i, and 0 if they are off.
The company allows at most one swap operation: you may choose one index containing 1 and one index containing 0, and swap
their values. This keeps the total number of working days the same, but may move a workday to a more useful position.

Your task is to return the maximum possible length of a consecutive block of 1s after performing at most one such swap.
You may also choose not to swap if the array is already optimal.

A swap can happen between any two positions in the array, not necessarily adjacent. Note that if there is no 0 in the array,
the answer is simply the full length of the array. If there is no 1 in the array, the answer is 0.

This problem tests whether you can reason about contiguous segments while respecting the global limit on how many 1s exist
in the entire array. An optimal solution should run in linear time.

Constraints:
- 1 <= days.length <= 200000
- days[i] is either 0 or 1

Example 1:
Input: days = [1,1,0,1,1,1,0,1]
Output: 6
Explanation: Swap the last 1 with the 0 at index 2. The array can become [1,1,1,1,1,1,0,0], which contains a consecutive block of 6 working days.

Example 2:
Input: days = [1,0,1,0,1]
Output: 3
Explanation: Swap one of the outer 1s with the middle 0 at index 1 or 3. The best possible consecutive block of 1s is 3.
*/

import java.util.*;

public class Solution {

    /**
     * Computes the maximum possible length of a consecutive block of 1s
     * after performing at most one swap between a 1 and a 0.
     *
     * Core idea:
     * 1. Count the total number of 1s in the entire array.
     * 2. For every index, precompute:
     *    - how many consecutive 1s end immediately to its left
     *    - how many consecutive 1s start immediately to its right
     * 3. For each 0, imagine turning that 0 into a 1 by swapping with some 1 elsewhere.
     *    Then the merged block length is:
     *       leftOnes + 1 + rightOnes
     *    But this cannot exceed the total number of 1s in the whole array,
     *    because a swap does not create new 1s.
     * 4. Also handle edge cases:
     *    - all 1s => answer is array length
     *    - all 0s => answer is 0
     *
     * @param days the binary schedule array where 1 means working day and 0 means off day
     * @return the maximum possible length of consecutive 1s after at most one swap
     * Time complexity: O(n)
     * Space complexity: O(n)
     */
    public int maxConsecutiveDaysAfterOneSwap(int[] days) {
        int n = days.length;

        // Step 1:
        // Count how many total working days (1s) exist in the entire array.
        // This is extremely important because a swap only moves a 1;
        // it never increases the total number of 1s.
        int totalOnes = countOnes(days);

        // If there are no working days at all, no consecutive block of 1s can exist.
        if (totalOnes == 0) {
            return 0;
        }

        // If every day is already a working day, the whole array is one consecutive block.
        if (totalOnes == n) {
            return n;
        }

        // left[i] will store:
        // the number of consecutive 1s ending exactly at index i.
        //
        // Example:
        // days = [1,1,0,1]
        // left = [1,2,0,1]
        int[] left = new int[n];

        // right[i] will store:
        // the number of consecutive 1s starting exactly at index i.
        //
        // Example:
        // days = [1,1,0,1]
        // right = [2,1,0,1]
        int[] right = new int[n];

        buildLeftConsecutiveOnes(days, left);
        buildRightConsecutiveOnes(days, right);

        int answer = 0;

        // Step 2:
        // Consider every position that contains 0.
        // If we swap some 1 from elsewhere into this position,
        // this 0 can become a bridge between the consecutive 1s on its left and right.
        for (int i = 0; i < n; i++) {
            if (days[i] == 0) {
                // Number of consecutive 1s immediately to the left of i.
                int leftOnes = (i > 0) ? left[i - 1] : 0;

                // Number of consecutive 1s immediately to the right of i.
                int rightOnes = (i + 1 < n) ? right[i + 1] : 0;

                // If we place a 1 at index i, we can connect:
                // left block + this new 1 + right block
                int merged = leftOnes + 1 + rightOnes;

                // However, we cannot exceed totalOnes.
                // Why?
                // Because the array still contains exactly totalOnes many 1s after the swap.
                //
                // Example:
                // days = [1,1,0,1,1]
                // leftOnes = 2, rightOnes = 2, merged = 5
                // totalOnes = 4
                // We cannot actually have 5 consecutive 1s because there are only 4 ones total.
                merged = Math.min(merged, totalOnes);

                answer = Math.max(answer, merged);
            }
        }

        // Step 3:
        // In some cases, the best answer may already be an existing block of 1s
        // even without using a swap in a helpful way.
        //
        // Since there is at least one 0 (handled earlier), any existing block length
        // is automatically <= totalOnes, so we can safely compare it directly.
        for (int value : left) {
            answer = Math.max(answer, value);
        }

        return answer;
    }

    /**
     * Counts the total number of 1s in the array.
     *
     * @param days the binary schedule array
     * @return the total number of entries equal to 1
     * Time complexity: O(n)
     * Space complexity: O(1)
     */
    public int countOnes(int[] days) {
        int count = 0;

        for (int day : days) {
            if (day == 1) {
                count++;
            }
        }

        return count;
    }

    /**
     * Builds an array where left[i] is the number of consecutive 1s
     * ending exactly at index i.
     *
     * Example:
     * days = [1,1,0,1,1]
     * left = [1,2,0,1,2]
     *
     * @param days the binary schedule array
     * @param left output array to fill with consecutive-left counts
     * @return nothing; the result is written into the provided left array
     * Time complexity: O(n)
     * Space complexity: O(1) extra, excluding the output array
     */
    public void buildLeftConsecutiveOnes(int[] days, int[] left) {
        int n = days.length;

        // We scan from left to right.
        // If current value is 1:
        //   left[i] = left[i - 1] + 1
        // Otherwise:
        //   left[i] = 0
        for (int i = 0; i < n; i++) {
            if (days[i] == 1) {
                if (i == 0) {
                    left[i] = 1;
                } else {
                    left[i] = left[i - 1] + 1;
                }
            } else {
                left[i] = 0;
            }
        }
    }

    /**
     * Builds an array where right[i] is the number of consecutive 1s
     * starting exactly at index i.
     *
     * Example:
     * days = [1,1,0,1,1]
     * right = [2,1,0,2,1]
     *
     * @param days the binary schedule array
     * @param right output array to fill with consecutive-right counts
     * @return nothing; the result is written into the provided right array
     * Time complexity: O(n)
     * Space complexity: O(1) extra, excluding the output array
     */
    public void buildRightConsecutiveOnes(int[] days, int[] right) {
        int n = days.length;

        // We scan from right to left.
        // If current value is 1:
        //   right[i] = right[i + 1] + 1
        // Otherwise:
        //   right[i] = 0
        for (int i = n - 1; i >= 0; i--) {
            if (days[i] == 1) {
                if (i == n - 1) {
                    right[i] = 1;
                } else {
                    right[i] = right[i + 1] + 1;
                }
            } else {
                right[i] = 0;
            }
        }
    }

    /**
     * Converts an int array into a readable string representation.
     * This helper is used only for demonstration in main.
     *
     * @param arr the array to convert
     * @return a string such as [1, 0, 1]
     * Time complexity: O(n)
     * Space complexity: O(n)
     */
    public String arrayToString(int[] arr) {
        return Arrays.toString(arr);
    }

    /**
     * Demonstrates the solution on the sample inputs and a few additional edge cases.
     *
     * @param args command-line arguments (not used)
     * @return nothing
     * Time complexity: O(k * n) for k demonstration test cases
     * Space complexity: O(n) per test case due to helper arrays in the algorithm
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        int[] sample1 = {1, 1, 0, 1, 1, 1, 0, 1};
        int[] sample2 = {1, 0, 1, 0, 1};

        int result1 = solution.maxConsecutiveDaysAfterOneSwap(sample1);
        int result2 = solution.maxConsecutiveDaysAfterOneSwap(sample2);

        System.out.println("Sample 1 input:  " + solution.arrayToString(sample1));
        System.out.println("Sample 1 output: " + result1);
        System.out.println("Expected: 6");
        System.out.println();

        System.out.println("Sample 2 input:  " + solution.arrayToString(sample2));
        System.out.println("Sample 2 output: " + result2);
        System.out.println("Expected: 3");
        System.out.println();

        int[] allOnes = {1, 1, 1, 1};
        int[] allZeros = {0, 0, 0, 0};
        int[] singleOne = {1};
        int[] singleZero = {0};
        int[] mixed = {1, 1, 0, 0, 1, 1, 1};

        System.out.println("All ones input:  " + solution.arrayToString(allOnes));
        System.out.println("Output: " + solution.maxConsecutiveDaysAfterOneSwap(allOnes));
        System.out.println();

        System.out.println("All zeros input: " + solution.arrayToString(allZeros));
        System.out.println("Output: " + solution.maxConsecutiveDaysAfterOneSwap(allZeros));
        System.out.println();

        System.out.println("Single one input: " + solution.arrayToString(singleOne));
        System.out.println("Output: " + solution.maxConsecutiveDaysAfterOneSwap(singleOne));
        System.out.println();

        System.out.println("Single zero input: " + solution.arrayToString(singleZero));
        System.out.println("Output: " + solution.maxConsecutiveDaysAfterOneSwap(singleZero));
        System.out.println();

        System.out.println("Mixed input:      " + solution.arrayToString(mixed));
        System.out.println("Output: " + solution.maxConsecutiveDaysAfterOneSwap(mixed));
    }
}