/*
Maximum Idle Gap After Removing One Meeting

Problem Description:
You are given an integer dayLength representing the total length of a workday, and two arrays start and end of equal length n, where the i-th meeting occupies the half-open interval [start[i], end[i]). The meetings are non-overlapping and sorted by start time. You may cancel at most one meeting entirely. After the cancellation, the remaining meetings keep their original times and order.

Your task is to return the maximum length of a continuous idle interval during the day. Idle time may appear before the first meeting, between two consecutive remaining meetings, or after the last meeting. If you choose not to remove any meeting, that is also allowed.

In other words, removing one meeting can merge the free time on its left, the meeting's own duration, and the free time on its right into one larger idle block. Compute the largest possible idle block obtainable.

Constraints:
- 1 <= n <= 2 * 10^5
- 1 <= dayLength <= 10^9
- 0 <= start[i] < end[i] <= dayLength
- end[i] <= start[i + 1] for all valid i
- start.length == end.length

Examples:
1)
dayLength = 20
start = [2, 6, 12]
end   = [4, 9, 15]

Idle intervals without cancellation:
[0,2)   -> 2
[4,6)   -> 2
[9,12)  -> 3
[15,20) -> 5

If we remove [12,15), the free time from 9 to 20 becomes continuous because there is no meeting after 9.
That merged idle block has length 11.
So the correct answer is 11.

2)
dayLength = 10
start = [1, 3, 7]
end   = [2, 5, 8]

Idle intervals without cancellation:
[0,1)  -> 1
[2,3)  -> 1
[5,7)  -> 2
[8,10) -> 2

If we remove [3,5), then the free time from 2 to 7 becomes continuous.
That merged idle block has length 5.
So the correct answer is 5.
*/

using System;

public class Solution
{
    /*
    Time Complexity: O(n)
    Space Complexity: O(1)

    Why O(n)?
    - We scan once to compute the best existing idle gap without removing anything.
    - We scan once more to evaluate the result of removing each meeting.
    - Each step does only constant-time work.

    Why O(1) extra space?
    - We only store a few integer variables.
    - We do not build any additional arrays.
    */
    public int MaximumIdleGapAfterRemovingOneMeeting(int dayLength, int[] start, int[] end)
    {
        int n = start.Length;

        // ------------------------------------------------------------
        // STEP 1: Compute the best idle gap if we remove NO meeting.
        // ------------------------------------------------------------
        //
        // This is necessary because the problem says we may cancel
        // "at most one" meeting, which means skipping cancellation
        // is allowed.
        //
        // So even before considering removals, we should know the
        // largest currently existing free interval.
        //
        // The free intervals are:
        // - before the first meeting: [0, start[0])
        // - between meetings i-1 and i: [end[i-1], start[i])
        // - after the last meeting: [end[n-1], dayLength)
        //
        int answer = 0;

        // Free time before the first meeting.
        answer = Math.Max(answer, start[0]);

        // Free time between consecutive meetings.
        for (int i = 1; i < n; i++)
        {
            int gapBetweenMeetings = start[i] - end[i - 1];
            answer = Math.Max(answer, gapBetweenMeetings);
        }

        // Free time after the last meeting.
        answer = Math.Max(answer, dayLength - end[n - 1]);

        // ------------------------------------------------------------
        // STEP 2: Try removing each meeting exactly once.
        // ------------------------------------------------------------
        //
        // Key idea:
        // If we remove meeting i, then the free time immediately to
        // its left, the meeting duration itself, and the free time
        // immediately to its right all merge into one continuous idle
        // interval.
        //
        // We can compute that merged interval directly by finding:
        // - the nearest boundary on the left that still blocks free time
        // - the nearest boundary on the right that still blocks free time
        //
        // After removing meeting i:
        // - the merged idle block starts at:
        //     end[i - 1] if i > 0
        //     0          if i == 0
        //
        // - the merged idle block ends at:
        //     start[i + 1] if i < n - 1
        //     dayLength    if i == n - 1
        //
        // Therefore merged length is:
        //     rightBoundary - leftBoundary
        //
        // This formula automatically includes:
        // - left free gap
        // - removed meeting duration
        // - right free gap
        //
        // Example 1, removing [12,15):
        // leftBoundary  = end[1] = 9
        // rightBoundary = dayLength = 20
        // merged length = 20 - 9 = 11
        //
        for (int i = 0; i < n; i++)
        {
            // Determine where the merged free interval begins.
            // If there is a previous meeting, free time cannot start
            // before that previous meeting ends.
            // If there is no previous meeting, the day starts at 0.
            int leftBoundary = (i == 0) ? 0 : end[i - 1];

            // Determine where the merged free interval ends.
            // If there is a next meeting, free time must stop when
            // that next meeting begins.
            // If there is no next meeting, the day ends at dayLength.
            int rightBoundary = (i == n - 1) ? dayLength : start[i + 1];

            // This is the full continuous idle interval created by
            // deleting meeting i.
            int mergedIdleLength = rightBoundary - leftBoundary;

            // Update the global best answer.
            answer = Math.Max(answer, mergedIdleLength);
        }

        // After considering:
        // 1) no removal
        // 2) removing each possible meeting
        // the answer now contains the maximum achievable idle gap.
        return answer;
    }
}

// ------------------------------------------------------------
// Demo code
// ------------------------------------------------------------

var solution = new Solution();

// Example 1
int dayLength1 = 20;
int[] start1 = { 2, 6, 12 };
int[] end1 = { 4, 9, 15 };
int result1 = solution.MaximumIdleGapAfterRemovingOneMeeting(dayLength1, start1, end1);
Console.WriteLine(result1); // Expected: 11

// Example 2
int dayLength2 = 10;
int[] start2 = { 1, 3, 7 };
int[] end2 = { 2, 5, 8 };
int result2 = solution.MaximumIdleGapAfterRemovingOneMeeting(dayLength2, start2, end2);
Console.WriteLine(result2); // Expected: 5

// Additional quick sanity check:
// One meeting covering the middle of the day.
// Remove it, and the whole day becomes idle.
int dayLength3 = 8;
int[] start3 = { 2 };
int[] end3 = { 6 };
int result3 = solution.MaximumIdleGapAfterRemovingOneMeeting(dayLength3, start3, end3);
Console.WriteLine(result3); // Expected: 8