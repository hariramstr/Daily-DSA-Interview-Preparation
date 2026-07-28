import java.util.*;

/*
Problem Title: Minimum Splits to Form Peak-Valley Value Waves

Problem Description:
You are given an integer array nums representing a long stream of measured values.
You want to partition the array into the minimum number of contiguous segments such
that every segment is a valid value wave.

A segment is considered a valid value wave if, after keeping the elements in their
original order, the differences between consecutive elements strictly alternate in sign.

In other words, for a segment a[l..r], if r - l + 1 >= 3, then for every i in
[l + 1, r - 1], (a[i] - a[i - 1]) and (a[i + 1] - a[i]) must be non-zero and one
must be positive while the other is negative.

Segments of length 1 or 2 are always valid, as long as no adjacent equal values appear
inside the segment. Because equal adjacent values break strict alternation, any segment
containing a pair of consecutive equal values is invalid.

Return the minimum number of contiguous segments needed to partition the entire array so
that every element belongs to exactly one valid segment. If it is impossible, return -1.

Constraints:
- 1 <= nums.length <= 200000
- -10^9 <= nums[i] <= 10^9
- The answer must be computed using contiguous segments only

Example 1:
Input: nums = [3, 1, 4, 2, 5]
Output: 1
Explanation: The differences are [-2, +3, -2, +3], which alternate in sign, so the
whole array is one valid wave.

Example 2:
Input: nums = [1, 4, 7, 2, 6, 3]
Output: 2
Explanation: The full array is not valid because the first two differences are both
positive: +3, +3. One optimal partition is [1, 4] and [7, 2, 6, 3]. The first segment
has length 2 and is valid, and the second has differences [-5, +4, -3], which alternate.
Therefore the minimum number of segments is 2.
*/

public class Solution {

    /**
     * Computes the minimum number of contiguous segments needed so that every segment
     * is a valid value wave.
     *
     * Core idea:
     * 1. A segment is invalid immediately if it contains adjacent equal values.
     * 2. For non-equal adjacent values, look at the signs of consecutive differences.
     *    A valid segment must have alternating signs.
     * 3. We greedily extend the current segment as far as possible.
     *    - If the next element keeps the alternating pattern, include it.
     *    - Otherwise, we must start a new segment at the current position.
     * 4. This greedy strategy is optimal because whenever alternation breaks at position i,
     *    no segment ending at i can include both conflicting consecutive differences.
     *    Therefore a cut is forced before nums[i].
     *
     * Important edge case:
     * - If nums[i] == nums[i - 1], then no valid segment can contain both positions.
     *   Since the partition is contiguous and every element must belong to exactly one segment,
     *   this makes the whole task impossible.
     *
     * @param nums the input array of measured values
     * @return the minimum number of valid contiguous wave segments, or -1 if impossible
     * Time complexity: O(n)
     * Space complexity: O(1)
     */
    public int minimumSplits(int[] nums) {
        if (nums == null || nums.length == 0) {
            return -1;
        }

        int n = nums.length;

        // A single element is always a valid segment.
        if (n == 1) {
            return 1;
        }

        // We start with one segment containing nums[0].
        int segments = 1;

        // prevSign represents the sign of the most recent difference inside the current segment.
        // Values:
        //   0  -> current segment has only one element so far, no difference yet
        //   1  -> last difference was positive
        //  -1  -> last difference was negative
        int prevSign = 0;

        // Process each adjacent pair exactly once.
        for (int i = 1; i < n; i++) {
            int sign = compare(nums[i], nums[i - 1]);

            // Equal adjacent values make it impossible.
            // Reason:
            // - A segment of length 2 with equal values is invalid.
            // - A longer segment containing equal adjacent values is also invalid.
            // - Since positions i-1 and i are adjacent in the original array, they must belong
            //   to the same contiguous segment if no cut can separate them between elements.
            if (sign == 0) {
                return -1;
            }

            // If prevSign == 0, the current segment currently has only one element.
            // So adding nums[i] creates a length-2 segment, which is valid because sign != 0.
            if (prevSign == 0) {
                prevSign = sign;
                continue;
            }

            // If the new sign is different from the previous sign, alternation continues.
            if (sign != prevSign) {
                prevSign = sign;
            } else {
                // Alternation breaks here:
                // previous difference and current difference have the same sign.
                //
                // Example:
                // nums = [1, 4, 7]
                // differences = [+3, +3]
                // These two consecutive differences cannot coexist in one valid segment.
                //
                // Therefore, a cut is forced before nums[i].
                // The previous segment ends at i - 1.
                // A new segment starts at i - 1? No:
                // Since nums[i - 1] is already used by the previous segment, the new segment
                // must start at nums[i].
                //
                // After starting a new segment at nums[i], that new segment currently has
                // only one element, so prevSign resets to 0.
                segments++;
                prevSign = 0;
            }
        }

        return segments;
    }

    /**
     * Compares two integers and returns the sign of (a - b) without overflow.
     *
     * @param a the left value
     * @param b the right value
     * @return 1 if a > b, -1 if a < b, 0 if a == b
     * Time complexity: O(1)
     * Space complexity: O(1)
     */
    public int compare(int a, int b) {
        return Integer.compare(a, b);
    }

    /**
     * Utility method to print an array in a beginner-friendly format.
     *
     * @param nums the array to print
     * @return a string representation of the array
     * Time complexity: O(n)
     * Space complexity: O(n)
     */
    public String arrayToString(int[] nums) {
        return Arrays.toString(nums);
    }

    /**
     * Demonstrates the solution on sample and additional test cases.
     *
     * @param args command-line arguments (not used)
     * @return nothing
     * Time complexity: O(total length of demonstrated arrays)
     * Space complexity: O(1) extra, excluding output formatting
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        int[] nums1 = {3, 1, 4, 2, 5};
        int result1 = solution.minimumSplits(nums1);
        System.out.println("Input:  " + solution.arrayToString(nums1));
        System.out.println("Output: " + result1);
        System.out.println("Expected: 1");
        System.out.println();

        int[] nums2 = {1, 4, 7, 2, 6, 3};
        int result2 = solution.minimumSplits(nums2);
        System.out.println("Input:  " + solution.arrayToString(nums2));
        System.out.println("Output: " + result2);
        System.out.println("Expected: 2");
        System.out.println();

        int[] nums3 = {5};
        int result3 = solution.minimumSplits(nums3);
        System.out.println("Input:  " + solution.arrayToString(nums3));
        System.out.println("Output: " + result3);
        System.out.println("Expected: 1");
        System.out.println();

        int[] nums4 = {2, 8};
        int result4 = solution.minimumSplits(nums4);
        System.out.println("Input:  " + solution.arrayToString(nums4));
        System.out.println("Output: " + result4);
        System.out.println("Expected: 1");
        System.out.println();

        int[] nums5 = {1, 2, 3, 4};
        int result5 = solution.minimumSplits(nums5);
        System.out.println("Input:  " + solution.arrayToString(nums5));
        System.out.println("Output: " + result5);
        System.out.println("Expected: 2");
        System.out.println();

        int[] nums6 = {1, 1, 2};
        int result6 = solution.minimumSplits(nums6);
        System.out.println("Input:  " + solution.arrayToString(nums6));
        System.out.println("Output: " + result6);
        System.out.println("Expected: -1");
        System.out.println();

        int[] nums7 = {9, 3, 8, 2, 7, 1, 6};
        int result7 = solution.minimumSplits(nums7);
        System.out.println("Input:  " + solution.arrayToString(nums7));
        System.out.println("Output: " + result7);
        System.out.println("Expected: 1");
    }
}