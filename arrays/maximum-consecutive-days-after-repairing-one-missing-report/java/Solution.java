import java.util.*;

/*
 * Title: Maximum Consecutive Days After Repairing One Missing Report
 * Difficulty: Medium
 * Topic: Arrays
 *
 * Problem Description:
 * A monitoring system stores the day numbers on which a service successfully uploaded
 * its daily health report. The array days contains distinct positive integers representing
 * those successful upload days, but the values are not guaranteed to be sorted.
 *
 * You are allowed to repair at most one missing report by choosing exactly one day number
 * that is not already in the array and treating it as if the report had been uploaded
 * successfully.
 *
 * Your task is to return the maximum possible length of a consecutive streak of successful
 * days after performing at most one repair. A consecutive streak is a set of day numbers
 * with no gaps, such as [10, 11, 12, 13], whose length is 4.
 *
 * For example, if the recorded days are [3, 5, 6], you may repair day 4 to obtain the
 * consecutive streak [3, 4, 5, 6], so the answer is 4. If the array already contains a
 * long consecutive block, you may choose not to repair anything.
 *
 * Design an algorithm that works efficiently for large inputs.
 *
 * Constraints:
 * - 1 <= days.length <= 2 * 10^5
 * - 1 <= days[i] <= 10^9
 * - All values in days are distinct
 *
 * Example 1:
 * Input: days = [8, 4, 5, 10, 6]
 * Output: 5
 * Explanation: The existing streak [4, 5, 6] has length 3. By repairing day 7,
 * you form [4, 5, 6, 7, 8], so the best streak length is 5.
 *
 * Example 2:
 * Input: days = [1, 2, 4, 7, 8]
 * Output: 4
 * Explanation: You can repair day 3 to form [1, 2, 3, 4], which has length 4.
 * Repairing day 6 forms [6, 7, 8], length 3. The best possible answer is 4.
 *
 * Note:
 * The answer should be the maximum streak length obtainable after adding at most one
 * missing day anywhere on the number line.
 */

public class Solution {

    /**
     * Computes the maximum possible length of a consecutive streak after adding
     * at most one missing day.
     *
     * Core idea:
     * 1. Sort the distinct day values.
     * 2. Compress the sorted values into maximal consecutive segments.
     *    Example: [1,2,4,7,8] -> segments [1..2], [4..4], [7..8]
     * 3. The best answer can come from:
     *    - using an existing segment as-is
     *    - extending one segment by 1 by adding a missing day next to it
     *    - bridging two neighboring segments if the gap between them is exactly 2
     *      (because then there is exactly one missing day between them)
     *
     * Why this is correct:
     * - With only one repair, you can fill only one missing number.
     * - Therefore, you can only:
     *   a) extend a segment by one on either side, or
     *   b) merge two segments separated by exactly one missing value.
     * - If the gap is larger than 2, one repair cannot connect both sides.
     *
     * @param days an array of distinct positive integers representing successful upload days
     * @return the maximum consecutive streak length obtainable after at most one repair
     * Time complexity: O(n log n), due to sorting
     * Space complexity: O(n), for the sorted copy and segment storage
     */
    public int maxConsecutiveDaysAfterOneRepair(int[] days) {
        int n = days.length;

        // If there is only one recorded day, we can always add one neighboring missing day
        // and obtain a streak of length 2.
        if (n == 1) {
            return 2;
        }

        // We sort a copy so that we can analyze consecutive structure from left to right.
        int[] sorted = Arrays.copyOf(days, n);
        Arrays.sort(sorted);

        // Each segment is represented by its start and end value.
        // Because all values are distinct, after sorting we can scan once and build
        // maximal consecutive blocks.
        List<int[]> segments = buildSegments(sorted);

        int answer = 1;

        // First, consider every existing segment by itself.
        // Since we are allowed "at most one" repair, we may also choose not to repair.
        for (int[] segment : segments) {
            int length = segment[1] - segment[0] + 1;
            answer = Math.max(answer, length);

            // Also consider extending this segment by exactly one day.
            // This is always possible because:
            // - all days are positive, so extending to the right by +1 is valid
            // - that day cannot already be inside the segment because it is just outside it
            //
            // Example: [4,5,6] can become [4,5,6,7] or [3,4,5,6].
            answer = Math.max(answer, length + 1);
        }

        // Now consider merging neighboring segments.
        // Let left = [a..b], right = [c..d].
        // If c - b == 2, then exactly one day (b+1) is missing between them.
        // By repairing that one day, we merge them into [a..d].
        for (int i = 0; i + 1 < segments.size(); i++) {
            int[] left = segments.get(i);
            int[] right = segments.get(i + 1);

            int leftEnd = left[1];
            int rightStart = right[0];

            // Gap size in terms of missing numbers between the two segments:
            // missingCount = rightStart - leftEnd - 1
            // We can merge only when missingCount == 1, i.e. rightStart - leftEnd == 2.
            if (rightStart - leftEnd == 2) {
                int mergedLength = right[1] - left[0] + 1;
                answer = Math.max(answer, mergedLength);
            }
        }

        return answer;
    }

    /**
     * Builds maximal consecutive segments from a sorted array of distinct integers.
     *
     * Example:
     * sorted = [1,2,4,7,8]
     * segments = [ [1,2], [4,4], [7,8] ]
     *
     * @param sorted a sorted array of distinct integers
     * @return a list of segments, where each segment is an int[2] = {start, end}
     * Time complexity: O(n)
     * Space complexity: O(k), where k is the number of segments, and k <= n
     */
    public List<int[]> buildSegments(int[] sorted) {
        List<int[]> segments = new ArrayList<>();

        // Start the first segment at the first value.
        int start = sorted[0];
        int end = sorted[0];

        // Scan from the second value onward.
        for (int i = 1; i < sorted.length; i++) {
            // If current value continues the consecutive run, extend the current segment.
            if (sorted[i] == end + 1) {
                end = sorted[i];
            } else {
                // Otherwise, the current segment ends here.
                segments.add(new int[]{start, end});

                // Start a new segment at the current value.
                start = sorted[i];
                end = sorted[i];
            }
        }

        // Do not forget to add the final segment after the loop.
        segments.add(new int[]{start, end});

        return segments;
    }

    /**
     * Utility method to print an array in a beginner-friendly format.
     *
     * @param arr the array to convert to string
     * @return a string representation of the array
     * Time complexity: O(n)
     * Space complexity: O(1) excluding the returned string
     */
    public String arrayToString(int[] arr) {
        return Arrays.toString(arr);
    }

    /**
     * Demonstrates the solution on sample and additional test cases.
     *
     * @param args command-line arguments (not used)
     * @return nothing
     * Time complexity: O(total input size * log n) across demonstrated cases
     * Space complexity: O(n) per test case due to sorting/segment storage
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Sample 1 from the prompt.
        int[] days1 = {8, 4, 5, 10, 6};
        int result1 = solution.maxConsecutiveDaysAfterOneRepair(days1);
        System.out.println("Input:  " + solution.arrayToString(days1));
        System.out.println("Output: " + result1);
        System.out.println("Expected: 5");
        System.out.println();

        // Sample 2 from the prompt.
        int[] days2 = {1, 2, 4, 7, 8};
        int result2 = solution.maxConsecutiveDaysAfterOneRepair(days2);
        System.out.println("Input:  " + solution.arrayToString(days2));
        System.out.println("Output: " + result2);
        System.out.println("Expected: 4");
        System.out.println();

        // Example mentioned in the description.
        int[] days3 = {3, 5, 6};
        int result3 = solution.maxConsecutiveDaysAfterOneRepair(days3);
        System.out.println("Input:  " + solution.arrayToString(days3));
        System.out.println("Output: " + result3);
        System.out.println("Expected: 4");
        System.out.println();

        // Additional checks.
        int[] days4 = {100};
        int result4 = solution.maxConsecutiveDaysAfterOneRepair(days4);
        System.out.println("Input:  " + solution.arrayToString(days4));
        System.out.println("Output: " + result4);
        System.out.println("Expected: 2");
        System.out.println();

        int[] days5 = {1, 2, 3, 4};
        int result5 = solution.maxConsecutiveDaysAfterOneRepair(days5);
        System.out.println("Input:  " + solution.arrayToString(days5));
        System.out.println("Output: " + result5);
        System.out.println("Expected: 5");
        System.out.println();

        int[] days6 = {1, 4, 5, 6, 9};
        int result6 = solution.maxConsecutiveDaysAfterOneRepair(days6);
        System.out.println("Input:  " + solution.arrayToString(days6));
        System.out.println("Output: " + result6);
        System.out.println("Expected: 4");
    }
}