import java.util.*;

/*
Title: First Store Open at or After Query Time

Problem Description:
You are given a sorted integer array openTimes where openTimes[i] represents the minute
of the day when the i-th store opens. The array is sorted in non-decreasing order, and
multiple stores may open at the same time. You are also given an integer queryTime,
representing the time a customer arrives.

Your task is to return the index of the first store whose opening time is greater than
or equal to queryTime. If no such store exists, return -1.

This problem models a common search task in scheduling and availability systems: given
a sorted list of event times, quickly find the earliest event that satisfies a threshold
condition. A linear scan works, but the expected interview solution uses binary search
to achieve logarithmic time complexity.

Implement a function that finds the leftmost valid index efficiently.

Constraints:
- 1 <= openTimes.length <= 10^5
- 0 <= openTimes[i] <= 10^9
- openTimes is sorted in non-decreasing order
- 0 <= queryTime <= 10^9

Example 1:
Input: openTimes = [120, 180, 240, 300], queryTime = 200
Output: 2
Explanation: The first opening time that is at least 200 is 240, which is at index 2.

Example 2:
Input: openTimes = [60, 60, 90, 150], queryTime = 60
Output: 0
Explanation: Multiple stores open at 60, and the first such index is 0.

If queryTime is larger than every value in openTimes, the answer should be -1.
*/

public class Solution {

    /**
     * Finds the index of the first store whose opening time is greater than or equal to
     * the given query time.
     *
     * This method uses binary search to efficiently locate the leftmost index that satisfies:
     * openTimes[index] >= queryTime
     *
     * If no such index exists, the method returns -1.
     *
     * @param openTimes the sorted array of store opening times in non-decreasing order
     * @param queryTime the customer arrival time to compare against store opening times
     * @return the index of the first store with opening time >= queryTime; otherwise -1
     *
     * Time Complexity: O(log n), where n is the length of openTimes
     * Space Complexity: O(1), because only a constant amount of extra space is used
     */
    public int firstStoreOpenAtOrAfter(int[] openTimes, int queryTime) {
        // We will perform a classic "lower bound" binary search.
        //
        // Goal:
        // Find the LEFTMOST index i such that:
        // openTimes[i] >= queryTime
        //
        // Why leftmost?
        // Because there may be duplicates, and the problem specifically asks for
        // the first store that opens at or after the query time.

        // 'left' starts at the beginning of the array.
        int left = 0;

        // 'right' starts at the end of the array.
        int right = openTimes.length - 1;

        // This variable will store the best valid answer found so far.
        // We initialize it to -1, meaning "not found yet".
        int answer = -1;

        // Continue searching while the search range is valid.
        while (left <= right) {
            // Compute the middle index safely.
            // This avoids potential integer overflow compared to (left + right) / 2.
            int mid = left + (right - left) / 2;

            // If the middle opening time is large enough, then mid is a valid candidate.
            if (openTimes[mid] >= queryTime) {
                // Record this index as a possible answer.
                answer = mid;

                // But we are not done yet.
                // Since we want the FIRST such index, we must continue searching
                // on the LEFT side to see if there is an earlier valid store.
                right = mid - 1;
            } else {
                // If openTimes[mid] < queryTime, then mid is too early and cannot be the answer.
                // Also, every index to the left of mid is <= openTimes[mid] because the array is sorted,
                // so none of them can satisfy the condition either.
                //
                // Therefore, we must search only in the RIGHT half.
                left = mid + 1;
            }
        }

        // If we found at least one valid index, 'answer' contains the leftmost one.
        // Otherwise, it remains -1.
        return answer;
    }

    /**
     * A helper method that prints an array in a beginner-friendly format.
     *
     * @param arr the integer array to print
     * @return a string representation of the array
     *
     * Time Complexity: O(n), where n is the length of the array
     * Space Complexity: O(n), due to the string construction
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
     * Time Complexity: O(log n) per demonstrated search call
     * Space Complexity: O(1) extra space per search call
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Example 1:
        // openTimes = [120, 180, 240, 300], queryTime = 200
        // The first value >= 200 is 240 at index 2.
        int[] openTimes1 = {120, 180, 240, 300};
        int queryTime1 = 200;
        int result1 = solution.firstStoreOpenAtOrAfter(openTimes1, queryTime1);
        System.out.println("Example 1:");
        System.out.println("openTimes = " + solution.arrayToString(openTimes1));
        System.out.println("queryTime = " + queryTime1);
        System.out.println("Output = " + result1);
        System.out.println("Expected = 2");
        System.out.println();

        // Example 2:
        // openTimes = [60, 60, 90, 150], queryTime = 60
        // Multiple stores open at 60, and the first such index is 0.
        int[] openTimes2 = {60, 60, 90, 150};
        int queryTime2 = 60;
        int result2 = solution.firstStoreOpenAtOrAfter(openTimes2, queryTime2);
        System.out.println("Example 2:");
        System.out.println("openTimes = " + solution.arrayToString(openTimes2));
        System.out.println("queryTime = " + queryTime2);
        System.out.println("Output = " + result2);
        System.out.println("Expected = 0");
        System.out.println();

        // Additional test:
        // queryTime is larger than every opening time, so answer should be -1.
        int[] openTimes3 = {100, 200, 300};
        int queryTime3 = 400;
        int result3 = solution.firstStoreOpenAtOrAfter(openTimes3, queryTime3);
        System.out.println("Additional Test 1:");
        System.out.println("openTimes = " + solution.arrayToString(openTimes3));
        System.out.println("queryTime = " + queryTime3);
        System.out.println("Output = " + result3);
        System.out.println("Expected = -1");
        System.out.println();

        // Additional test:
        // queryTime exactly matches an element in the middle.
        int[] openTimes4 = {30, 80, 120, 120, 200};
        int queryTime4 = 120;
        int result4 = solution.firstStoreOpenAtOrAfter(openTimes4, queryTime4);
        System.out.println("Additional Test 2:");
        System.out.println("openTimes = " + solution.arrayToString(openTimes4));
        System.out.println("queryTime = " + queryTime4);
        System.out.println("Output = " + result4);
        System.out.println("Expected = 2");
        System.out.println();

        // Additional test:
        // queryTime is smaller than all values, so the first valid index is 0.
        int[] openTimes5 = {50, 100, 150};
        int queryTime5 = 10;
        int result5 = solution.firstStoreOpenAtOrAfter(openTimes5, queryTime5);
        System.out.println("Additional Test 3:");
        System.out.println("openTimes = " + solution.arrayToString(openTimes5));
        System.out.println("queryTime = " + queryTime5);
        System.out.println("Output = " + result5);
        System.out.println("Expected = 0");
    }
}