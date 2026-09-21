/*
Title: Count Team Pairings Within Experience Gap

Problem Description:
You are given an integer array experience where experience[i] is the number of years of experience
of the i-th engineer, and an integer gap. A pair of engineers (i, j) is considered compatible if
i < j and the absolute difference between their experience values is less than or equal to gap.

Return the total number of compatible pairs.

A straightforward O(n^2) solution checks every pair, but that is too slow for large inputs.
Your task is to design an efficient algorithm using sorting and a two-pointers technique.

Because the pair condition depends only on the difference between two values, the array may be
reordered during processing. However, the final answer should count pairs from the original set
of engineers, not based on their positions after sorting.

Constraints:
- 1 <= experience.length <= 200000
- 0 <= experience[i] <= 1000000000
- 0 <= gap <= 1000000000
- The answer may be large, so use a 64-bit integer type where needed.

Example 1:
Input: experience = [1, 3, 4, 7], gap = 3
Output: 4

Explanation:
Compatible pairs are:
- (1, 3) with difference 2
- (1, 4) with difference 3
- (3, 4) with difference 1
- (4, 7) with difference 3
So the total number of compatible pairs is 4.

Example 2:
Input: experience = [5, 5, 5, 8, 10], gap = 0
Output: 3

Explanation:
Only pairs with exactly equal experience are allowed.
Among the three engineers with experience 5, there are 3 choose 2 = 3 valid pairs.
No other pair has difference 0.
*/

import java.util.*;

public class Solution {

    /**
     * Counts the total number of compatible engineer pairs such that:
     * for each pair (i, j), i < j and |experience[i] - experience[j]| <= gap.
     *
     * Core idea:
     * 1. Sort the experience values.
     * 2. Use a sliding window / two-pointers approach.
     * 3. For each right pointer, move the left pointer forward until the window satisfies:
     *    experience[right] - experience[left] <= gap.
     * 4. Then every index from left to right - 1 forms a valid pair with right.
     *
     * Why this works:
     * After sorting, for any right index, if the smallest value in the current window
     * differs from experience[right] by at most gap, then every value between them also
     * differs by at most gap because the array is sorted.
     *
     * @param experience the array of engineers' experience values
     * @param gap the maximum allowed absolute difference for a compatible pair
     * @return the total number of compatible pairs as a long
     * Time complexity: O(n log n) due to sorting, plus O(n) for the two-pointer scan
     * Space complexity: O(log n) due to sorting stack usage in Java's primitive array sort
     */
    public long countCompatiblePairs(int[] experience, int gap) {
        // Defensive handling is not strictly required by the constraints,
        // but this makes the method safer and beginner-friendly.
        if (experience == null || experience.length < 2) {
            return 0L;
        }

        // Sort the array so that compatible values become grouped together.
        // Once sorted, absolute difference for i < j becomes:
        // experience[j] - experience[i]
        // because experience[j] >= experience[i].
        Arrays.sort(experience);

        long totalPairs = 0L;

        // "left" marks the start of the current valid window.
        int left = 0;

        // Expand the window with "right".
        for (int right = 0; right < experience.length; right++) {

            // If the difference between the current largest value (experience[right])
            // and the smallest value in the window (experience[left]) is too large,
            // then the window is invalid.
            //
            // We keep moving "left" forward until the window becomes valid again.
            //
            // Important:
            // Cast to long before subtraction to avoid any risk of integer overflow,
            // even though the given constraints are within int range.
            while ((long) experience[right] - experience[left] > gap) {
                left++;
            }

            // At this point, the window [left, right] is valid:
            // experience[right] - experience[left] <= gap
            //
            // Because the array is sorted, every index k in [left, right - 1]
            // also satisfies:
            // experience[right] - experience[k] <= gap
            //
            // Therefore, the current "right" element forms valid pairs with all
            // previous elements in this window.
            //
            // Number of such elements = right - left
            totalPairs += (right - left);
        }

        return totalPairs;
    }

    /**
     * A helper method that creates a copy of the input array before processing.
     * This is useful in demonstrations when we want to preserve the original array
     * because the main algorithm sorts the array in-place.
     *
     * @param experience the original experience array
     * @param gap the maximum allowed absolute difference
     * @return the total number of compatible pairs as a long
     * Time complexity: O(n log n)
     * Space complexity: O(n) because of the copied array
     */
    public long countCompatiblePairsWithoutModifyingInput(int[] experience, int gap) {
        int[] copy = Arrays.copyOf(experience, experience.length);
        return countCompatiblePairs(copy, gap);
    }

    /**
     * Demonstrates the solution using the sample inputs from the problem statement
     * and prints the results.
     *
     * @param args command-line arguments (not used)
     * @return nothing
     * Time complexity: O(n log n) across the demonstrated test cases
     * Space complexity: O(n) for copied arrays used in demonstration
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Example 1
        int[] experience1 = {1, 3, 4, 7};
        int gap1 = 3;
        long result1 = solution.countCompatiblePairsWithoutModifyingInput(experience1, gap1);
        System.out.println("Example 1:");
        System.out.println("Input: experience = " + Arrays.toString(experience1) + ", gap = " + gap1);
        System.out.println("Output: " + result1);
        System.out.println("Expected: 4");
        System.out.println();

        // Example 2
        int[] experience2 = {5, 5, 5, 8, 10};
        int gap2 = 0;
        long result2 = solution.countCompatiblePairsWithoutModifyingInput(experience2, gap2);
        System.out.println("Example 2:");
        System.out.println("Input: experience = " + Arrays.toString(experience2) + ", gap = " + gap2);
        System.out.println("Output: " + result2);
        System.out.println("Expected: 3");
        System.out.println();

        // Additional quick checks
        int[] experience3 = {2};
        int gap3 = 5;
        long result3 = solution.countCompatiblePairsWithoutModifyingInput(experience3, gap3);
        System.out.println("Additional Test 1:");
        System.out.println("Input: experience = " + Arrays.toString(experience3) + ", gap = " + gap3);
        System.out.println("Output: " + result3);
        System.out.println("Expected: 0");
        System.out.println();

        int[] experience4 = {10, 1, 6, 3};
        int gap4 = 100;
        long result4 = solution.countCompatiblePairsWithoutModifyingInput(experience4, gap4);
        System.out.println("Additional Test 2:");
        System.out.println("Input: experience = " + Arrays.toString(experience4) + ", gap = " + gap4);
        System.out.println("Output: " + result4);
        System.out.println("Expected: 6");
        System.out.println();
    }
}