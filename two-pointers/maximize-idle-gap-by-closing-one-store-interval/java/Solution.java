import java.util.*;

/*
Problem Title: Maximize Idle Gap by Closing One Store Interval

Problem Description:
A retail analytics team tracks the minutes during a day when a store is open. The schedule is represented by a sorted list of non-overlapping open intervals, where each interval is inclusive of its start minute and exclusive of its end minute: [start, end). Management is allowed to permanently cancel at most one open interval for maintenance. After removing zero or one interval, they want to know the largest continuous idle gap anywhere between the remaining open intervals, including the gap before the first interval and after the last interval within the business day.

You are given an integer dayLength representing the total number of minutes in the day, and a 2D integer array intervals where intervals[i] = [start_i, end_i]. The intervals are sorted by start_i, non-overlapping, and all satisfy 0 <= start_i < end_i <= dayLength.

Return the maximum possible idle gap after removing at most one interval.

A gap is any continuous range of minutes with no open interval covering it. If you remove an interval in the middle, the two neighboring gaps merge together with the removed interval's length, forming one larger gap.

Your solution should run efficiently for large inputs. A two-pointer or linear scan approach is expected.

Constraints:
- 1 <= intervals.length <= 200000
- 1 <= dayLength <= 1000000000
- 0 <= start_i < end_i <= dayLength
- intervals is sorted by start_i
- intervals are pairwise non-overlapping

Example 1:
Input: dayLength = 20, intervals = [[2,5],[7,9],[12,15]]
Output: 7
Explanation: Without removing anything, the gaps are [0,2) length 2, [5,7) length 2, [9,12) length 3, and [15,20) length 5. If we remove [7,9), the gap from 5 to 12 becomes length 7, which is the maximum possible.

Example 2:
Input: dayLength = 30, intervals = [[0,4],[6,10],[13,18],[22,30]]
Output: 12
Explanation: If we remove [13,18), the gap before it has length 3 and the gap after it has length 4. Together with the removed interval length 5, they form one idle gap from 10 to 22 of length 12. No other choice gives a larger gap.
*/

public class Solution {

    /**
     * Computes the maximum possible continuous idle gap after removing at most one interval.
     *
     * Core idea:
     * 1. First, compute all existing gaps:
     *    - gap before the first interval
     *    - gaps between consecutive intervals
     *    - gap after the last interval
     * 2. If we remove interval i:
     *    - the gap immediately before it
     *    - the interval's own length
     *    - the gap immediately after it
     *    all merge into one larger continuous idle gap.
     * 3. Therefore, for each interval i, the merged gap is:
     *      leftGap + intervalLength + rightGap
     * 4. Also consider removing nothing, which means the answer could simply be the largest existing gap.
     *
     * @param dayLength the total length of the day in minutes
     * @param intervals sorted, non-overlapping open intervals [start, end)
     * @return the maximum possible idle gap after removing zero or one interval
     * Time complexity: O(n), where n = intervals.length
     * Space complexity: O(n), for storing the gaps array
     */
    public int maximizeIdleGap(int dayLength, int[][] intervals) {
        int n = intervals.length;

        // There are exactly n + 1 gaps around n intervals:
        // gap[0]     = before interval 0
        // gap[1]     = between interval 0 and interval 1
        // ...
        // gap[n - 1] = between interval n - 2 and interval n - 1
        // gap[n]     = after interval n - 1
        int[] gaps = buildGaps(dayLength, intervals);

        // Start with the best answer when we remove nothing.
        // This is simply the largest existing gap.
        int answer = 0;
        for (int gap : gaps) {
            answer = Math.max(answer, gap);
        }

        // Now try removing each interval exactly once.
        // If we remove interval i, then:
        // - gaps[i] is the gap immediately before interval i
        // - intervalLength is the length of interval i
        // - gaps[i + 1] is the gap immediately after interval i
        //
        // These three pieces become one continuous idle segment.
        for (int i = 0; i < n; i++) {
            int intervalLength = intervals[i][1] - intervals[i][0];
            int mergedGap = gaps[i] + intervalLength + gaps[i + 1];
            answer = Math.max(answer, mergedGap);
        }

        return answer;
    }

    /**
     * Builds the array of all idle gaps around the given intervals.
     *
     * Example:
     * intervals = [[2,5],[7,9],[12,15]], dayLength = 20
     * gaps will be:
     * - gaps[0] = 2   => [0,2)
     * - gaps[1] = 2   => [5,7)
     * - gaps[2] = 3   => [9,12)
     * - gaps[3] = 5   => [15,20)
     *
     * @param dayLength the total length of the day
     * @param intervals sorted, non-overlapping open intervals
     * @return an array of length intervals.length + 1 containing all idle gaps
     * Time complexity: O(n)
     * Space complexity: O(n)
     */
    public int[] buildGaps(int dayLength, int[][] intervals) {
        int n = intervals.length;
        int[] gaps = new int[n + 1];

        // Gap before the first interval.
        gaps[0] = intervals[0][0];

        // Gaps between consecutive intervals.
        for (int i = 1; i < n; i++) {
            gaps[i] = intervals[i][0] - intervals[i - 1][1];
        }

        // Gap after the last interval.
        gaps[n] = dayLength - intervals[n - 1][1];

        return gaps;
    }

    /**
     * A helper method that prints intervals in a readable format.
     *
     * @param intervals the interval array to print
     * @return a string representation of the intervals
     * Time complexity: O(n)
     * Space complexity: O(n), due to StringBuilder output construction
     */
    public String intervalsToString(int[][] intervals) {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < intervals.length; i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append("[").append(intervals[i][0]).append(",").append(intervals[i][1]).append("]");
        }
        sb.append("]");
        return sb.toString();
    }

    /**
     * Demonstrates the solution on the sample inputs from the problem statement.
     *
     * @param args command-line arguments (not used)
     * @return nothing
     * Time complexity: O(1) for the fixed demo cases, excluding algorithm calls
     * Space complexity: O(1), excluding algorithm internal usage
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        int dayLength1 = 20;
        int[][] intervals1 = {
            {2, 5},
            {7, 9},
            {12, 15}
        };
        int result1 = solution.maximizeIdleGap(dayLength1, intervals1);
        System.out.println("Example 1");
        System.out.println("dayLength = " + dayLength1);
        System.out.println("intervals = " + solution.intervalsToString(intervals1));
        System.out.println("Output = " + result1);
        System.out.println("Expected = 7");
        System.out.println();

        int dayLength2 = 30;
        int[][] intervals2 = {
            {0, 4},
            {6, 10},
            {13, 18},
            {22, 30}
        };
        int result2 = solution.maximizeIdleGap(dayLength2, intervals2);
        System.out.println("Example 2");
        System.out.println("dayLength = " + dayLength2);
        System.out.println("intervals = " + solution.intervalsToString(intervals2));
        System.out.println("Output = " + result2);
        System.out.println("Expected = 12");
        System.out.println();

        int dayLength3 = 10;
        int[][] intervals3 = {
            {2, 4}
        };
        int result3 = solution.maximizeIdleGap(dayLength3, intervals3);
        System.out.println("Additional Example 3");
        System.out.println("dayLength = " + dayLength3);
        System.out.println("intervals = " + solution.intervalsToString(intervals3));
        System.out.println("Output = " + result3);
        System.out.println("Expected = 10");
        System.out.println();

        int dayLength4 = 15;
        int[][] intervals4 = {
            {0, 3},
            {5, 6},
            {8, 10}
        };
        int result4 = solution.maximizeIdleGap(dayLength4, intervals4);
        System.out.println("Additional Example 4");
        System.out.println("dayLength = " + dayLength4);
        System.out.println("intervals = " + solution.intervalsToString(intervals4));
        System.out.println("Output = " + result4);
        System.out.println();
    }
}