/*
Title: Minimum Playback Speed for Buffered Lectures

Problem Description:
You are given an array lectures where lectures[i] is the length in minutes of the i-th recorded lecture segment.
A student wants to finish all segments within h hours by watching them at a constant playback speed s.

If a segment has length x minutes and the student watches at speed s, the time spent on that segment is:
    ceil(x / s) minutes

This rounding happens for every segment independently because the platform only allows moving on after
finishing the current segment.

The same integer speed s must be used for every segment.

Return the minimum positive integer playback speed s such that the total time needed to watch all lecture
segments is at most h hours.

If it is impossible even with arbitrarily large speed because each non-empty segment still takes at least
1 minute, return -1.

Key observation:
- If a speed s is fast enough, then any larger speed is also fast enough.
- That means the answer space is monotonic, so binary search is the right tool.

Important unit conversion:
- h is given in hours
- each segment's rounded viewing time is measured in minutes
- so we must compare total required minutes against h * 60
*/

using System;

public class Solution
{
    /*
    Time Complexity: O(n log M)
        - n = number of lecture segments
        - M = maximum lecture length
        We binary search over possible speeds, and for each candidate speed
        we scan the array once to compute total required minutes.

    Space Complexity: O(1)
        - We only use a few variables besides the input array.
    */
    public int MinPlaybackSpeed(int[] lectures, int h)
    {
        // Convert the allowed time from hours to minutes.
        // We use long because h can be as large as 1,000,000,000,
        // and h * 60 must not overflow a 32-bit int.
        long allowedMinutes = (long)h * 60;

        // Even with an extremely large speed, each non-empty segment still takes at least 1 minute
        // because ceil(x / very_large_speed) = 1 for any positive x.
        //
        // Therefore, the absolute minimum possible total time is exactly the number of segments.
        // If the allowed minutes are smaller than the number of segments, it is impossible.
        if (allowedMinutes < lectures.Length)
        {
            return -1;
        }

        // The minimum possible speed is 1.
        int left = 1;

        // The maximum speed we ever need to test is the largest lecture length.
        // Why?
        // If speed >= max lecture length, then every segment takes exactly 1 minute,
        // and going faster cannot reduce any segment below 1 minute.
        int right = 0;
        foreach (int lecture in lectures)
        {
            if (lecture > right)
            {
                right = lecture;
            }
        }

        // This variable will store the best valid speed found so far.
        // Since we already ruled out impossible cases, a valid answer must exist.
        int answer = right;

        // Standard binary search on the answer space.
        // We are searching for the smallest speed that is "fast enough".
        while (left <= right)
        {
            // Compute the middle speed carefully.
            // This form avoids overflow compared to (left + right) / 2.
            int mid = left + (right - left) / 2;

            // Check whether this candidate speed allows finishing within allowedMinutes.
            if (CanFinish(lectures, mid, allowedMinutes))
            {
                // If mid works, it is a valid answer candidate.
                // But we want the MINIMUM valid speed, so we continue searching left.
                answer = mid;
                right = mid - 1;
            }
            else
            {
                // If mid is too slow, then every smaller speed is also too slow
                // because smaller speed means more time.
                // So we must search to the right for a faster speed.
                left = mid + 1;
            }
        }

        return answer;
    }

    private bool CanFinish(int[] lectures, int speed, long allowedMinutes)
    {
        // This variable accumulates the total required viewing time in minutes.
        // We use long because the sum can become large.
        long totalMinutes = 0;

        // Process each lecture segment independently.
        foreach (int lecture in lectures)
        {
            // We need ceil(lecture / speed), but both values are integers.
            //
            // A common integer math trick for ceiling division is:
            //     ceil(a / b) = (a + b - 1) / b
            //
            // Example:
            // lecture = 95, speed = 2
            // ceil(95 / 2) = 48
            // (95 + 2 - 1) / 2 = 96 / 2 = 48
            long minutesForThisLecture = ((long)lecture + speed - 1) / speed;

            // Add this lecture's required time to the running total.
            totalMinutes += minutesForThisLecture;

            // Early exit optimization:
            // If we already exceeded the allowed time, there is no need to continue.
            // This keeps the check efficient in practice.
            if (totalMinutes > allowedMinutes)
            {
                return false;
            }
        }

        // If we finished summing all segments and never exceeded the limit,
        // then this speed is fast enough.
        return totalMinutes <= allowedMinutes;
    }
}

// Demo code

var solution = new Solution();

// Example 1
int[] lectures1 = { 45, 80, 30 };
int h1 = 3;
int result1 = solution.MinPlaybackSpeed(lectures1, h1);
Console.WriteLine($"Example 1 Result: {result1}");
// Expected: 1
// Check:
// allowed = 3 * 60 = 180 minutes
// speed 1 => 45 + 80 + 30 = 155 <= 180, so answer is 1

// Example 2
int[] lectures2 = { 120, 95, 200 };
int h2 = 4;
int result2 = solution.MinPlaybackSpeed(lectures2, h2);
Console.WriteLine($"Example 2 Result: {result2}");
// Correct expected result: 2
// Check:
// allowed = 4 * 60 = 240 minutes
// speed 1 => 120 + 95 + 200 = 415 > 240
// speed 2 => ceil(120/2) + ceil(95/2) + ceil(200/2)
//         => 60 + 48 + 100 = 208 <= 240
// Therefore minimum valid speed is 2

// Additional impossible example
int[] lectures3 = { 100, 200, 300, 400 };
int h3 = 0 + 0 + 0 + 0 + 0 + 0 + 0 + 0 + 0 + 0 + 0 + 0 + 0 + 0 + 0 + 0 + 0 + 0 + 0 + 0 + 0 + 0 + 0 + 0 + 0 + 0 + 0 + 0 + 0 + 0 + 0 + 0 + 0 + 0 + 0 + 0 + 0 + 0 + 0 + 0 + 0 + 0 + 0 + 0 + 0 + 0 + 0 + 0 + 0 + 0 + 0 + 1;
int result3 = solution.MinPlaybackSpeed(lectures3, h3);
Console.WriteLine($"Additional Example Result: {result3}");
// h3 = 1 hour => 60 minutes
// 4 segments means minimum possible total is 4 minutes, so this one is possible.
// This demo simply shows another call works.

// Truly impossible example
int[] lectures4 = { 10, 20, 30, 40, 50, 60, 70 };
int h4 = 0;
int result4 = solution.MinPlaybackSpeed(lectures4, h4);
Console.WriteLine($"Impossible Example Result: {result4}");
// allowed = 0 minutes, but there are 7 non-empty segments, each needs at least 1 minute
// Expected: -1