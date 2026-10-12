import java.util.*;

/*
 * Title: Find the First Version Supporting a Feature
 * Difficulty: Easy
 * Topic: Binary Search
 *
 * Problem Description:
 * A software platform releases product versions in increasing order, and each version either
 * supports a new feature or does not. You are given a sorted binary array support, where
 * support[i] is either 0 or 1. A value of 0 means version i does not support the feature yet,
 * and a value of 1 means version i does support it. The array is guaranteed to be sorted in
 * non-decreasing order, so once the feature appears in some version, every later version also
 * supports it.
 *
 * Your task is to return the index of the first version that supports the feature.
 * If no version supports it, return -1.
 *
 * This is a classic search task where a linear scan works, but the intended solution should use
 * binary search to achieve logarithmic time. Be careful with edge cases such as when the first
 * element is already 1, when all elements are 0, or when the array contains only one version.
 *
 * Constraints:
 * - 1 <= support.length <= 100000
 * - support[i] is either 0 or 1
 * - support is sorted in non-decreasing order
 *
 * Example 1:
 * Input: support = [0,0,0,1,1,1]
 * Output: 3
 * Explanation: The first version that supports the feature is at index 3.
 *
 * Example 2:
 * Input: support = [0,0,0,0]
 * Output: -1
 * Explanation: No version supports the feature, so return -1.
 */

public class Solution {

    /**
     * Finds the index of the first version that supports the feature using binary search.
     *
     * The array is sorted and contains only 0s followed by 1s, so we search for the leftmost 1.
     *
     * @param support the sorted binary array where 0 means unsupported and 1 means supported
     * @return the index of the first 1, or -1 if the array contains no 1
     *
     * Time Complexity: O(log n), because the search range is halved in each step.
     * Space Complexity: O(1), because only a constant amount of extra space is used.
     */
    public int firstSupportingVersion(int[] support) {
        // Defensive check:
        // The problem guarantees at least one element, but checking for null or empty input
        // makes the method safer and easier for beginners to reuse in other contexts.
        if (support == null || support.length == 0) {
            return -1;
        }

        // 'left' points to the beginning of the current search range.
        int left = 0;

        // 'right' points to the end of the current search range.
        int right = support.length - 1;

        // This variable will store the best answer found so far.
        // We initialize it to -1, which means "no supporting version found yet".
        int answer = -1;

        // Continue searching while the current range is valid.
        // A valid range means left has not crossed right.
        while (left <= right) {
            // Compute the middle index carefully.
            // We use this form instead of (left + right) / 2 to avoid integer overflow,
            // even though overflow is not likely here due to constraints.
            int mid = left + (right - left) / 2;

            // Case 1:
            // If support[mid] == 1, then mid is a version that supports the feature.
            // However, it may not be the FIRST such version.
            // So:
            //   1) record mid as a possible answer
            //   2) continue searching on the LEFT side to see if there is an earlier 1
            if (support[mid] == 1) {
                answer = mid;
                right = mid - 1;
            } else {
                // Case 2:
                // If support[mid] == 0, then mid definitely does NOT support the feature.
                // Since the array is sorted, every index at or before mid is also not the first 1.
                // Therefore, we must search on the RIGHT side.
                left = mid + 1;
            }
        }

        // If we found at least one 1, 'answer' holds the leftmost one.
        // Otherwise, it remains -1.
        return answer;
    }

    /**
     * A simple linear scan version for learning and comparison.
     * This is not the intended optimal solution, but it helps beginners understand the problem.
     *
     * @param support the sorted binary array where 0 means unsupported and 1 means supported
     * @return the index of the first 1, or -1 if the array contains no 1
     *
     * Time Complexity: O(n), because in the worst case we may inspect every element.
     * Space Complexity: O(1), because only a constant amount of extra space is used.
     */
    public int firstSupportingVersionLinear(int[] support) {
        if (support == null || support.length == 0) {
            return -1;
        }

        for (int i = 0; i < support.length; i++) {
            if (support[i] == 1) {
                return i;
            }
        }

        return -1;
    }

    /**
     * Prints an array and the result of the binary search solution.
     *
     * @param support the input array to test
     * @return no return value
     *
     * Time Complexity: O(n) for printing the array, plus O(log n) for the search.
     * Space Complexity: O(1) extra space, excluding output-related internals.
     */
    public static void runDemo(int[] support) {
        Solution solution = new Solution();
        System.out.println("Input:  " + Arrays.toString(support));
        System.out.println("Output: " + solution.firstSupportingVersion(support));
        System.out.println();
    }

    /**
     * Demonstrates the solution using the sample inputs and a few additional edge cases.
     *
     * @param args command-line arguments (not used)
     * @return no return value
     *
     * Time Complexity: Depends on the number and size of demo test cases.
     * Space Complexity: O(1) extra space, excluding output-related internals.
     */
    public static void main(String[] args) {
        // Sample Example 1:
        // support = [0,0,0,1,1,1]
        // The first 1 appears at index 3.
        runDemo(new int[]{0, 0, 0, 1, 1, 1});

        // Sample Example 2:
        // support = [0,0,0,0]
        // There is no 1, so the answer is -1.
        runDemo(new int[]{0, 0, 0, 0});

        // Additional edge case:
        // The first element is already 1, so the answer should be 0.
        runDemo(new int[]{1, 1, 1, 1});

        // Additional edge case:
        // Single element array containing 0, so answer is -1.
        runDemo(new int[]{0});

        // Additional edge case:
        // Single element array containing 1, so answer is 0.
        runDemo(new int[]{1});

        // Additional edge case:
        // Transition happens in the middle.
        runDemo(new int[]{0, 0, 1, 1, 1});
    }
}