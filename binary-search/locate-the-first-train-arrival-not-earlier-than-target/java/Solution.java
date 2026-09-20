/*
Title: Locate the First Train Arrival Not Earlier Than Target
Difficulty: Easy
Topic: Binary Search

Problem Description:
You are given a sorted array arrivals where arrivals[i] represents the scheduled arrival time
of the i-th train in minutes after midnight. The array is sorted in non-decreasing order,
so multiple trains may share the same arrival time. You are also given an integer target,
representing the earliest time a passenger is willing to board.

Your task is to return the index of the first train whose arrival time is greater than or
equal to target. If no such train exists, return -1.

This problem models a common lookup operation in booking and scheduling systems: finding
the earliest available option that satisfies a minimum requirement. A linear scan works,
but the input is already sorted, so an efficient binary search solution is expected.

Constraints:
- 1 <= arrivals.length <= 10^5
- 0 <= arrivals[i] <= 1439
- arrivals is sorted in non-decreasing order
- 0 <= target <= 1439

Example 1:
Input: arrivals = [120, 180, 180, 240, 315], target = 181
Output: 3
Explanation: The first arrival not earlier than 181 is 240, which is at index 3.

Example 2:
Input: arrivals = [60, 90, 150, 150, 210], target = 150
Output: 2
Explanation: There are trains at time 150, and the first such train appears at index 2.

If every train arrives before target, the answer should be -1.
*/

import java.util.*;

public class Solution {

    /**
     * Finds the index of the first train arrival time that is greater than or equal to the target.
     *
     * This method uses binary search because the input array is already sorted in non-decreasing order.
     * The goal is not just to find any value >= target, but specifically the first such index.
     *
     * @param arrivals the sorted array of train arrival times in minutes after midnight
     * @param target the earliest acceptable boarding time
     * @return the index of the first arrival time >= target; returns -1 if no such arrival exists
     *
     * Time Complexity: O(log n), where n is the length of the arrivals array
     * Space Complexity: O(1), because only a constant amount of extra space is used
     */
    public int firstArrivalNotEarlierThan(int[] arrivals, int target) {
        // We will search within the full array boundaries.
        int left = 0;
        int right = arrivals.length - 1;

        // This variable stores the best answer found so far.
        // We initialize it to -1, meaning "not found yet".
        int answer = -1;

        // Continue searching while the current search range is valid.
        while (left <= right) {
            // Compute the middle index carefully.
            // This form avoids potential integer overflow that could happen with (left + right) / 2.
            int mid = left + (right - left) / 2;

            // Case 1:
            // If arrivals[mid] is large enough, then mid is a valid candidate.
            // But there might be an even earlier valid index on the left side,
            // so we record mid and continue searching to the left.
            if (arrivals[mid] >= target) {
                answer = mid;
                right = mid - 1;
            } else {
                // Case 2:
                // If arrivals[mid] is still smaller than target,
                // then mid cannot be the answer, and neither can anything to its left,
                // because the array is sorted.
                // So we move to the right half.
                left = mid + 1;
            }
        }

        // If we found at least one valid index, answer holds the first one.
        // Otherwise, it remains -1.
        return answer;
    }

    /**
     * A simple linear scan version for learning and verification purposes.
     * This is not the optimal approach for large inputs, but it is easy to understand.
     *
     * @param arrivals the sorted array of train arrival times in minutes after midnight
     * @param target the earliest acceptable boarding time
     * @return the index of the first arrival time >= target; returns -1 if no such arrival exists
     *
     * Time Complexity: O(n), where n is the length of the arrivals array
     * Space Complexity: O(1), because only a constant amount of extra space is used
     */
    public int firstArrivalNotEarlierThanLinear(int[] arrivals, int target) {
        // Check each arrival from left to right.
        // The first one that satisfies arrivals[i] >= target is the answer.
        for (int i = 0; i < arrivals.length; i++) {
            if (arrivals[i] >= target) {
                return i;
            }
        }

        // If we finish the loop, no arrival met the requirement.
        return -1;
    }

    /**
     * Prints an integer array in a beginner-friendly format.
     *
     * @param arr the array to print
     * @return a string representation of the array
     *
     * Time Complexity: O(n), where n is the length of the array
     * Space Complexity: O(n), due to the string being built for output
     */
    public String arrayToString(int[] arr) {
        return Arrays.toString(arr);
    }

    /**
     * Demonstrates the solution using the sample inputs from the problem statement
     * and a few additional test cases.
     *
     * @param args command-line arguments (not used)
     * @return nothing
     *
     * Time Complexity: O(log n) per binary-search demonstration call
     * Space Complexity: O(1) extra space per binary-search call
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Example 1 from the problem statement:
        // arrivals = [120, 180, 180, 240, 315], target = 181
        // Expected output: 3
        int[] arrivals1 = {120, 180, 180, 240, 315};
        int target1 = 181;
        int result1 = solution.firstArrivalNotEarlierThan(arrivals1, target1);

        System.out.println("Example 1:");
        System.out.println("arrivals = " + solution.arrayToString(arrivals1));
        System.out.println("target = " + target1);
        System.out.println("Output = " + result1);
        System.out.println("Expected = 3");
        System.out.println();

        // Example 2 from the problem statement:
        // arrivals = [60, 90, 150, 150, 210], target = 150
        // Expected output: 2
        int[] arrivals2 = {60, 90, 150, 150, 210};
        int target2 = 150;
        int result2 = solution.firstArrivalNotEarlierThan(arrivals2, target2);

        System.out.println("Example 2:");
        System.out.println("arrivals = " + solution.arrayToString(arrivals2));
        System.out.println("target = " + target2);
        System.out.println("Output = " + result2);
        System.out.println("Expected = 2");
        System.out.println();

        // Additional test:
        // Every train arrives before target, so answer should be -1.
        int[] arrivals3 = {100, 200, 300};
        int target3 = 400;
        int result3 = solution.firstArrivalNotEarlierThan(arrivals3, target3);

        System.out.println("Additional Test 1:");
        System.out.println("arrivals = " + solution.arrayToString(arrivals3));
        System.out.println("target = " + target3);
        System.out.println("Output = " + result3);
        System.out.println("Expected = -1");
        System.out.println();

        // Additional test:
        // Target is smaller than or equal to the first element, so answer should be 0.
        int[] arrivals4 = {50, 80, 120};
        int target4 = 30;
        int result4 = solution.firstArrivalNotEarlierThan(arrivals4, target4);

        System.out.println("Additional Test 2:");
        System.out.println("arrivals = " + solution.arrayToString(arrivals4));
        System.out.println("target = " + target4);
        System.out.println("Output = " + result4);
        System.out.println("Expected = 0");
        System.out.println();

        // Additional test:
        // Multiple equal values; we must return the first matching index.
        int[] arrivals5 = {150, 150, 150, 200};
        int target5 = 150;
        int result5 = solution.firstArrivalNotEarlierThan(arrivals5, target5);

        System.out.println("Additional Test 3:");
        System.out.println("arrivals = " + solution.arrayToString(arrivals5));
        System.out.println("target = " + target5);
        System.out.println("Output = " + result5);
        System.out.println("Expected = 0");
    }
}