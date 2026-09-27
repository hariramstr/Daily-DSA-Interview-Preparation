import java.util.*;

/*
 * Title: Count Shelf Pairs With Exact Width Sum
 * Difficulty: Medium
 * Topic: Two Pointers
 *
 * Problem Description:
 * You are given an integer array widths representing the widths of wooden shelves currently stored
 * in a warehouse. The array is not guaranteed to be sorted. You are also given an integer targetWidth.
 * A pair of shelves (i, j) is considered valid if i < j and widths[i] + widths[j] == targetWidth.
 *
 * Your task is to return the total number of valid index pairs.
 *
 * Because the warehouse may contain many shelves with the same width, duplicate values must be handled
 * correctly. For example, if four shelves have width 2 and targetWidth is 4, then they form 6 distinct
 * pairs because every choice of two different indices counts.
 *
 * Design an efficient solution using sorting and the two-pointer technique. A brute-force O(n^2)
 * solution will be too slow for the largest inputs.
 *
 * Constraints:
 * - 1 <= widths.length <= 2 * 10^5
 * - -10^9 <= widths[i] <= 10^9
 * - -2 * 10^9 <= targetWidth <= 2 * 10^9
 * - The answer fits in a 64-bit signed integer.
 *
 * Example 1:
 * Input: widths = [1, 5, 3, 3, 2, 4], targetWidth = 6
 * Output: 3
 * Explanation: The valid pairs are formed by values (1,5), (2,4), and the two shelves with width 3.
 *
 * Example 2:
 * Input: widths = [2, 2, 2, 2, 3, 1], targetWidth = 4
 * Output: 6
 * Explanation: Only shelves with width 2 can pair with each other. There are 4 such shelves,
 * so the number of index pairs is 4 choose 2 = 6.
 */

public class Solution {

    /**
     * Counts how many index pairs (i, j) satisfy i < j and widths[i] + widths[j] == targetWidth.
     *
     * The algorithm works as follows:
     * 1. Sort the array so equal values become adjacent and two-pointer traversal becomes possible.
     * 2. Use one pointer at the left end and one pointer at the right end.
     * 3. If the current sum is too small, move the left pointer rightward.
     * 4. If the current sum is too large, move the right pointer leftward.
     * 5. If the current sum matches the target:
     *    - If the values at both pointers are different, count how many duplicates exist on both sides.
     *      Every left duplicate can pair with every right duplicate.
     *    - If the values at both pointers are the same, then every element in that entire range can pair
     *      with every other element in that range. If there are k such elements, the number of pairs is
     *      k * (k - 1) / 2.
     *
     * This correctly handles duplicates and avoids O(n^2) brute force checking.
     *
     * @param widths the array of shelf widths
     * @param targetWidth the required sum for a valid pair
     * @return the total number of valid index pairs as a long
     * Time complexity: O(n log n) due to sorting, plus O(n) for the two-pointer scan
     * Space complexity: O(1) extra space excluding the sorting implementation details used by Java
     */
    public long countShelfPairs(int[] widths, int targetWidth) {
        // Defensive handling for very small arrays:
        // if there are fewer than 2 shelves, no pair can exist.
        if (widths == null || widths.length < 2) {
            return 0L;
        }

        // Sort the array first.
        // This is the key step that enables the two-pointer technique.
        Arrays.sort(widths);

        // Left pointer starts at the smallest value.
        int left = 0;

        // Right pointer starts at the largest value.
        int right = widths.length - 1;

        // We store the answer in a long because the number of pairs can be large.
        long totalPairs = 0L;

        // Continue while there is still at least one pair of distinct indices to examine.
        while (left < right) {
            // Use long for the sum to avoid any risk of integer overflow
            // when adding two values near the int limits.
            long currentSum = (long) widths[left] + widths[right];

            if (currentSum < targetWidth) {
                // Current sum is too small.
                // Because the array is sorted, increasing the left pointer is the only way
                // to possibly make the sum larger.
                left++;
            } else if (currentSum > targetWidth) {
                // Current sum is too large.
                // Because the array is sorted, decreasing the right pointer is the only way
                // to possibly make the sum smaller.
                right--;
            } else {
                // We found values whose sum equals the target.
                // Now we must count duplicates carefully.

                if (widths[left] == widths[right]) {
                    // Special case:
                    // The values at both ends are the same.
                    //
                    // Since widths[left] + widths[right] == targetWidth and both values are equal,
                    // every element from left to right has the same value and can pair with every
                    // other element in that range.
                    //
                    // Example:
                    // [2, 2, 2, 2], target = 4
                    // Number of pairs = 4 choose 2 = 6
                    long count = right - left + 1L;
                    totalPairs += count * (count - 1L) / 2L;

                    // We have counted all possible pairs in this equal-value block,
                    // so the process is complete.
                    break;
                } else {
                    // General case:
                    // widths[left] and widths[right] are different values,
                    // but together they sum to the target.
                    //
                    // We count how many duplicates of widths[left] appear consecutively from the left,
                    // and how many duplicates of widths[right] appear consecutively from the right.
                    //
                    // Then the number of valid pairs contributed by these groups is:
                    // leftCount * rightCount

                    int leftValue = widths[left];
                    int rightValue = widths[right];

                    long leftCount = 0L;
                    long rightCount = 0L;

                    // Count duplicates on the left side.
                    while (left <= right && widths[left] == leftValue) {
                        leftCount++;
                        left++;
                    }

                    // Count duplicates on the right side.
                    while (left <= right && widths[right] == rightValue) {
                        rightCount++;
                        right--;
                    }

                    // Every occurrence of leftValue can pair with every occurrence of rightValue.
                    totalPairs += leftCount * rightCount;
                }
            }
        }

        return totalPairs;
    }

    /**
     * Helper method to print an array in a beginner-friendly format.
     *
     * @param arr the array to print
     * @return a string representation of the array
     * Time complexity: O(n)
     * Space complexity: O(n) for the produced string
     */
    public String arrayToString(int[] arr) {
        return Arrays.toString(arr);
    }

    /**
     * Demonstrates the solution using the sample inputs from the problem statement
     * and prints the results.
     *
     * @param args command-line arguments (not used)
     * @return nothing
     * Time complexity: O(n log n) per demonstration call because it invokes the main algorithm
     * Space complexity: O(1) extra space excluding sorting internals
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Example 1
        int[] widths1 = {1, 5, 3, 3, 2, 4};
        int target1 = 6;
        long result1 = solution.countShelfPairs(widths1.clone(), target1);
        System.out.println("Example 1:");
        System.out.println("widths = " + solution.arrayToString(widths1));
        System.out.println("targetWidth = " + target1);
        System.out.println("Output = " + result1);
        System.out.println("Expected = 3");
        System.out.println();

        // Example 2
        int[] widths2 = {2, 2, 2, 2, 3, 1};
        int target2 = 4;
        long result2 = solution.countShelfPairs(widths2.clone(), target2);
        System.out.println("Example 2:");
        System.out.println("widths = " + solution.arrayToString(widths2));
        System.out.println("targetWidth = " + target2);
        System.out.println("Output = " + result2);
        System.out.println("Expected = 6");
        System.out.println();

        // Additional quick checks
        int[] widths3 = {0, 0, 0};
        int target3 = 0;
        long result3 = solution.countShelfPairs(widths3.clone(), target3);
        System.out.println("Additional Test 1:");
        System.out.println("widths = " + solution.arrayToString(widths3));
        System.out.println("targetWidth = " + target3);
        System.out.println("Output = " + result3);
        System.out.println("Expected = 3");
        System.out.println();

        int[] widths4 = {-1, 7, 3, 3, 4, 2, 5};
        int target4 = 6;
        long result4 = solution.countShelfPairs(widths4.clone(), target4);
        System.out.println("Additional Test 2:");
        System.out.println("widths = " + solution.arrayToString(widths4));
        System.out.println("targetWidth = " + target4);
        System.out.println("Output = " + result4);
        System.out.println("Expected = 3");
    }
}