import java.util.*;

/*
 * Maximum Idle Gap After Removing One Meeting
 *
 * Problem Description:
 * You are given an integer dayLength representing the total length of a workday,
 * and two arrays start and end of equal length n, where the i-th meeting occupies
 * the half-open interval [start[i], end[i]). The meetings are non-overlapping and
 * sorted by start time. You may cancel at most one meeting entirely. After the
 * cancellation, the remaining meetings keep their original times and order.
 *
 * Your task is to return the maximum length of a continuous idle interval during
 * the day. Idle time may appear before the first meeting, between two consecutive
 * remaining meetings, or after the last meeting. If you choose not to remove any
 * meeting, that is also allowed.
 *
 * In other words, removing one meeting can merge the free time on its left, the
 * meeting's own duration, and the free time on its right into one larger idle block.
 * Compute the largest possible idle block obtainable.
 *
 * Constraints:
 * - 1 <= n <= 2 * 10^5
 * - 1 <= dayLength <= 10^9
 * - 0 <= start[i] < end[i] <= dayLength
 * - end[i] <= start[i + 1] for all valid i
 * - start.length == end.length
 *
 * Example 1:
 * Input: dayLength = 20, start = [2, 6, 12], end = [4, 9, 15]
 * Output: 11
 * Explanation:
 * Existing idle intervals are:
 * - [0,2)   => 2
 * - [4,6)   => 2
 * - [9,12)  => 3
 * - [15,20) => 5
 *
 * If we remove meeting [12,15), then the free time immediately before it is [9,12)
 * and the free time immediately after it is [15,20). Together with the removed
 * meeting duration [12,15), they merge into one continuous idle interval [9,20),
 * whose length is 11. This is optimal.
 *
 * Example 2:
 * Input: dayLength = 10, start = [1, 3, 7], end = [2, 5, 8]
 * Output: 5
 * Explanation:
 * Existing idle intervals are lengths 1, 1, 2, and 2.
 * If we remove meeting [3,5), then the interval from 2 to 7 becomes fully idle,
 * with length 5. This is the maximum possible answer.
 */

public class Solution {

    /**
     * Computes the maximum continuous idle interval obtainable by removing at most one meeting.
     *
     * Core idea:
     * 1. First, compute the best idle interval without removing anything.
     * 2. Then, for each meeting i, imagine removing it.
     *    The newly merged idle block stretches:
     *    - from the end of the previous remaining meeting (or 0 if i is the first meeting)
     *    - to the start of the next remaining meeting (or dayLength if i is the last meeting)
     * 3. The length of that merged block is simply:
     *    rightBoundary - leftBoundary
     * 4. Take the maximum over all choices, including removing no meeting.
     *
     * Because meetings are already sorted and non-overlapping, this can be done in one linear scan.
     *
     * @param dayLength the total length of the day
     * @param start the sorted start times of meetings
     * @param end the corresponding end times of meetings
     * @return the maximum possible length of a continuous idle interval after removing at most one meeting
     *
     * Time complexity: O(n), where n is the number of meetings.
     * Space complexity: O(1), ignoring input storage.
     */
    public int maximumIdleGapAfterRemovingOneMeeting(int dayLength, int[] start, int[] end) {
        validateInput(dayLength, start, end);

        int n = start.length;

        // This variable stores the best answer found so far.
        // We begin by considering the case where we remove NO meeting.
        int best = 0;

        // ------------------------------------------------------------
        // STEP 1: Compute the maximum idle gap without removing anything.
        // ------------------------------------------------------------
        //
        // There are exactly n + 1 idle gaps in the original schedule:
        //   gap before first meeting: [0, start[0])
        //   gaps between meetings:    [end[i], start[i+1])
        //   gap after last meeting:   [end[n-1], dayLength)
        //
        // We scan all of them and keep the largest.
        //
        best = Math.max(best, start[0] - 0);

        for (int i = 0; i < n - 1; i++) {
            best = Math.max(best, start[i + 1] - end[i]);
        }

        best = Math.max(best, dayLength - end[n - 1]);

        // ------------------------------------------------------------
        // STEP 2: Try removing each meeting exactly once.
        // ------------------------------------------------------------
        //
        // Suppose we remove meeting i = [start[i], end[i]).
        //
        // Then the continuous idle block created around it begins at:
        //   - 0, if i is the first meeting
        //   - end[i - 1], otherwise
        //
        // and ends at:
        //   - dayLength, if i is the last meeting
        //   - start[i + 1], otherwise
        //
        // Why?
        // Because after removing meeting i, the nearest meeting on the left is i-1,
        // and the nearest meeting on the right is i+1. Everything between those two
        // boundaries becomes free continuously.
        //
        // So the merged idle length is:
        //   rightBoundary - leftBoundary
        //
        for (int i = 0; i < n; i++) {
            int leftBoundary = (i == 0) ? 0 : end[i - 1];
            int rightBoundary = (i == n - 1) ? dayLength : start[i + 1];

            int mergedIdleLength = rightBoundary - leftBoundary;
            best = Math.max(best, mergedIdleLength);
        }

        return best;
    }

    /**
     * Validates the input according to the problem constraints.
     * This method is helpful for demonstration and beginner-friendly robustness.
     *
     * @param dayLength the total length of the day
     * @param start the meeting start times
     * @param end the meeting end times
     * @return nothing; throws an exception if input is invalid
     *
     * Time complexity: O(n), because we may scan the arrays once.
     * Space complexity: O(1).
     */
    public void validateInput(int dayLength, int[] start, int[] end) {
        if (start == null || end == null) {
            throw new IllegalArgumentException("start and end arrays must not be null.");
        }

        if (start.length != end.length) {
            throw new IllegalArgumentException("start and end arrays must have the same length.");
        }

        if (start.length == 0) {
            throw new IllegalArgumentException("There must be at least one meeting.");
        }

        if (dayLength < 1) {
            throw new IllegalArgumentException("dayLength must be at least 1.");
        }

        for (int i = 0; i < start.length; i++) {
            if (start[i] < 0 || end[i] < 0 || start[i] >= end[i] || end[i] > dayLength) {
                throw new IllegalArgumentException("Invalid meeting interval at index " + i + ".");
            }

            if (i > 0 && end[i - 1] > start[i]) {
                throw new IllegalArgumentException("Meetings must be sorted and non-overlapping.");
            }
        }
    }

    /**
     * Helper method to run one demonstration test case and print the result.
     *
     * @param dayLength the total length of the day
     * @param start the meeting start times
     * @param end the meeting end times
     * @return the computed answer for convenience in demonstrations
     *
     * Time complexity: O(n), delegated to the main algorithm.
     * Space complexity: O(1), ignoring input storage.
     */
    public int runDemo(int dayLength, int[] start, int[] end) {
        int answer = maximumIdleGapAfterRemovingOneMeeting(dayLength, start, end);
        System.out.println("dayLength = " + dayLength);
        System.out.println("start = " + Arrays.toString(start));
        System.out.println("end   = " + Arrays.toString(end));
        System.out.println("Maximum idle gap after removing at most one meeting = " + answer);
        System.out.println();
        return answer;
    }

    /**
     * Demonstrates the solution on the sample inputs from the problem statement.
     *
     * Verified examples:
     * Example 1:
     * - dayLength = 20
     * - start = [2, 6, 12]
     * - end = [4, 9, 15]
     * Correct answer = 11
     *
     * Example 2:
     * - dayLength = 10
     * - start = [1, 3, 7]
     * - end = [2, 5, 8]
     * Correct answer = 5
     *
     * @param args command-line arguments (unused)
     * @return nothing
     *
     * Time complexity: O(total number of meetings across demo cases).
     * Space complexity: O(1), ignoring input arrays.
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        int dayLength1 = 20;
        int[] start1 = {2, 6, 12};
        int[] end1 = {4, 9, 15};
        int result1 = solution.runDemo(dayLength1, start1, end1);
        System.out.println("Expected: 11");
        System.out.println("Actual:   " + result1);
        System.out.println();

        int dayLength2 = 10;
        int[] start2 = {1, 3, 7};
        int[] end2 = {2, 5, 8};
        int result2 = solution.runDemo(dayLength2, start2, end2);
        System.out.println("Expected: 5");
        System.out.println("Actual:   " + result2);
        System.out.println();

        // Additional quick sanity check:
        // One meeting covering the middle of the day.
        // dayLength = 8, meeting [2,6)
        // Without removal: max idle = max(2,2) = 2
        // Remove it: whole day [0,8) becomes idle => 8
        int dayLength3 = 8;
        int[] start3 = {2};
        int[] end3 = {6};
        int result3 = solution.runDemo(dayLength3, start3, end3);
        System.out.println("Expected: 8");
        System.out.println("Actual:   " + result3);
    }
}