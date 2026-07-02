import java.util.*;

/*
 * Title: Longest Route Segment With Limited Toll Booth Types
 * Difficulty: Medium
 * Topic: Sliding Window
 *
 * Problem Description:
 * A navigation company records the sequence of toll booths a truck passes during a long highway trip.
 * Each toll booth is labeled by an integer type representing the toll operator that manages it.
 * For billing simplification, the company wants to analyze the longest contiguous segment of the trip
 * that uses at most k distinct toll booth types.
 *
 * Given an integer array booths where booths[i] is the type of the i-th toll booth encountered,
 * and an integer k, return the length of the longest contiguous subarray containing at most k distinct values.
 *
 * A segment is contiguous if it consists of consecutive toll booths in the original trip log.
 * If k is 0, then no toll booth type can be included, so the answer is 0.
 *
 * Your solution should be efficient enough for large trip logs, so an O(n) or O(n log n) approach is expected.
 *
 * Constraints:
 * - 1 <= booths.length <= 200000
 * - 1 <= booths[i] <= 1000000000
 * - 0 <= k <= booths.length
 *
 * Example 1:
 * Input: booths = [4, 7, 4, 4, 9, 7, 9, 9], k = 2
 * Output: 4
 * Explanation: The longest valid segment is [4, 4, 9, 7] or [9, 7, 9, 9], each containing only 2 distinct toll booth types.
 *
 * Example 2:
 * Input: booths = [5, 5, 1, 2, 1, 2, 3], k = 3
 * Output: 6
 * Explanation: The segment [5, 1, 2, 1, 2, 3] contains exactly 3 distinct types and has length 6.
 */

public class Solution {

    /**
     * Computes the length of the longest contiguous subarray that contains at most k distinct values.
     *
     * This method uses the classic sliding window technique:
     * 1. Expand the right side of the window one element at a time.
     * 2. Track frequencies of values inside the current window using a HashMap.
     * 3. If the window becomes invalid (more than k distinct values), move the left side forward
     *    until the window becomes valid again.
     * 4. Record the maximum valid window length seen during the process.
     *
     * @param booths the array of toll booth types encountered during the trip
     * @param k the maximum number of distinct toll booth types allowed in a valid segment
     * @return the length of the longest contiguous segment containing at most k distinct values
     *
     * Time complexity: O(n), where n is booths.length, because each element is added to and removed
     * from the sliding window at most once.
     * Space complexity: O(k) on average for the frequency map of values currently in the window,
     * and in the worst case O(n) if k is large enough to allow many distinct values.
     */
    public int longestSegmentWithAtMostKDistinct(int[] booths, int k) {
        // If k is 0, we are not allowed to include any toll booth type at all.
        // Therefore, the longest valid segment must have length 0.
        if (k == 0) {
            return 0;
        }

        // This map stores:
        // key   -> toll booth type
        // value -> how many times that type appears inside the current window [left, right]
        Map<Integer, Integer> frequencyMap = new HashMap<>();

        // left marks the beginning of the current sliding window.
        int left = 0;

        // bestLength stores the maximum valid window length found so far.
        int bestLength = 0;

        // We expand the window by moving right from 0 to booths.length - 1.
        for (int right = 0; right < booths.length; right++) {
            int currentType = booths[right];

            // Add the new rightmost element into the frequency map.
            frequencyMap.put(currentType, frequencyMap.getOrDefault(currentType, 0) + 1);

            // If we now have more than k distinct values, the window is invalid.
            // We must shrink it from the left until it becomes valid again.
            while (frequencyMap.size() > k) {
                int leftType = booths[left];

                // Decrease the count of the value that is leaving the window.
                frequencyMap.put(leftType, frequencyMap.get(leftType) - 1);

                // If its count becomes 0, it no longer exists in the current window,
                // so we remove it from the map entirely.
                if (frequencyMap.get(leftType) == 0) {
                    frequencyMap.remove(leftType);
                }

                // Move the left boundary one step to the right.
                left++;
            }

            // At this point, the window [left, right] is guaranteed to contain at most k distinct values.
            // So it is a valid candidate for the answer.
            int currentWindowLength = right - left + 1;

            // Update the best answer if this valid window is longer than any previous one.
            if (currentWindowLength > bestLength) {
                bestLength = currentWindowLength;
            }
        }

        return bestLength;
    }

    /**
     * A convenience wrapper method using the exact problem wording.
     *
     * @param booths the array of toll booth types
     * @param k the maximum number of distinct toll booth types allowed
     * @return the length of the longest contiguous subarray with at most k distinct values
     *
     * Time complexity: O(n)
     * Space complexity: O(k) average, O(n) worst case
     */
    public int lengthOfLongestContiguousSegment(int[] booths, int k) {
        return longestSegmentWithAtMostKDistinct(booths, k);
    }

    /**
     * Prints an integer array in a readable format.
     *
     * @param arr the array to print
     *
     * Time complexity: O(n)
     * Space complexity: O(1) extra space excluding output construction handled by library methods
     */
    public static void printArray(int[] arr) {
        System.out.println(Arrays.toString(arr));
    }

    /**
     * Demonstrates the solution on the sample inputs from the problem statement
     * and a few additional edge cases.
     *
     * @param args command-line arguments (not used)
     *
     * Time complexity: O(total number of elements across demonstrated test cases)
     * Space complexity: depends on the largest test case processed by the algorithm
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Sample Example 1
        int[] booths1 = {4, 7, 4, 4, 9, 7, 9, 9};
        int k1 = 2;
        int result1 = solution.lengthOfLongestContiguousSegment(booths1, k1);
        System.out.println("Example 1:");
        System.out.print("booths = ");
        printArray(booths1);
        System.out.println("k = " + k1);
        System.out.println("Output = " + result1);
        System.out.println("Expected = 4");
        System.out.println();

        // Sample Example 2
        int[] booths2 = {5, 5, 1, 2, 1, 2, 3};
        int k2 = 3;
        int result2 = solution.lengthOfLongestContiguousSegment(booths2, k2);
        System.out.println("Example 2:");
        System.out.print("booths = ");
        printArray(booths2);
        System.out.println("k = " + k2);
        System.out.println("Output = " + result2);
        System.out.println("Expected = 6");
        System.out.println();

        // Edge case: k = 0
        int[] booths3 = {1, 2, 3};
        int k3 = 0;
        int result3 = solution.lengthOfLongestContiguousSegment(booths3, k3);
        System.out.println("Edge Case 1:");
        System.out.print("booths = ");
        printArray(booths3);
        System.out.println("k = " + k3);
        System.out.println("Output = " + result3);
        System.out.println("Expected = 0");
        System.out.println();

        // Edge case: all same values
        int[] booths4 = {8, 8, 8, 8, 8};
        int k4 = 1;
        int result4 = solution.lengthOfLongestContiguousSegment(booths4, k4);
        System.out.println("Edge Case 2:");
        System.out.print("booths = ");
        printArray(booths4);
        System.out.println("k = " + k4);
        System.out.println("Output = " + result4);
        System.out.println("Expected = 5");
        System.out.println();

        // Edge case: k larger than number of distinct values
        int[] booths5 = {2, 3, 2, 4, 5};
        int k5 = 10;
        int result5 = solution.lengthOfLongestContiguousSegment(booths5, k5);
        System.out.println("Edge Case 3:");
        System.out.print("booths = ");
        printArray(booths5);
        System.out.println("k = " + k5);
        System.out.println("Output = " + result5);
        System.out.println("Expected = 5");
    }
}