/*
Title: Minimum Playback Speed for Museum Audio Guides
Difficulty: Medium
Topic: Binary Search

Problem Description:
A museum offers a fixed sequence of audio guide sections that must be listened to in order.
The i-th section has length guides[i] minutes at normal speed.

Visitors may choose a constant playback speed s, where s is a positive integer, and every
section is played at that same speed.

A section that would take x minutes at normal speed takes ceil(x / s) whole minutes to finish
because the museum app only advances to the next section at the start of the next minute.

Given an array guides and an integer limit, return the minimum integer playback speed s such
that the total listening time of all sections is at most limit minutes.

If it is impossible even at arbitrarily large speed, return -1.

Key observation:
- For a fixed speed s, the total time is:
      ceil(guides[0] / s) + ceil(guides[1] / s) + ... + ceil(guides[n-1] / s)
- As speed s increases, each ceil(guides[i] / s) stays the same or decreases.
- Therefore, total time is monotonic non-increasing with respect to speed.
- That monotonic behavior makes binary search the ideal approach.

Important note:
- If limit < guides.Length, the answer is always -1.
  Reason: every section takes at least 1 whole minute, no matter how large the speed is.
*/

using System;

public class Solution
{
    /*
    Time Complexity:
    - Let n = guides.Length
    - Let M = max value in guides
    - Each binary search step computes total time in O(n)
    - Binary search performs O(log M) steps
    - Total: O(n log M)

    Space Complexity:
    - O(1) extra space
    - We only use a few variables besides the input array
    */
    public int MinPlaybackSpeed(int[] guides, int limit)
    {
        // Step 1:
        // Before doing any binary search, check the impossible case.
        //
        // Why this is necessary:
        // Every section takes at least 1 whole minute, even at extremely large speed,
        // because ceil(x / s) is always at least 1 for any positive x and positive s.
        //
        // So if there are n sections, the absolute minimum total time possible is n.
        // If limit is smaller than n, no speed can ever work.
        if (limit < guides.Length)
        {
            return -1;
        }

        // Step 2:
        // Establish the binary search range.
        //
        // We need to search for the minimum integer speed s that works.
        //
        // Lower bound:
        // - Speed must be at least 1.
        //
        // Upper bound:
        // - max(guides) is always sufficient when limit >= guides.Length.
        //   Why?
        //   If s = max(guides), then for every section guides[i] <= s,
        //   so ceil(guides[i] / s) = 1.
        //   Therefore total time becomes exactly guides.Length, which is <= limit
        //   because we already checked limit >= guides.Length.
        //
        // This guarantees that a valid answer exists inside [1, max(guides)].
        int left = 1;
        int right = 0;

        // Find the maximum section length to use as the binary search upper bound.
        foreach (int guide in guides)
        {
            if (guide > right)
            {
                right = guide;
            }
        }

        // Step 3:
        // Perform standard binary search on the answer.
        //
        // In this problem, we are not searching for a value inside the array.
        // Instead, we are searching for the smallest speed that satisfies a condition:
        //     totalTime(speed) <= limit
        //
        // This is a classic "binary search on answer" pattern.
        while (left < right)
        {
            // Compute the middle speed safely.
            // Using this form avoids overflow in general:
            // mid = left + (right - left) / 2
            int mid = left + (right - left) / 2;

            // Step 4:
            // Calculate how many total whole minutes are needed if we use speed = mid.
            //
            // We use long for the running total because:
            // - guides.Length can be up to 100000
            // - each ceil(...) can be large
            // - the sum may exceed the range of int during accumulation
            long totalMinutes = 0;

            foreach (int guide in guides)
            {
                // Compute ceil(guide / mid) using integer arithmetic.
                //
                // Formula:
                //   ceil(a / b) = (a + b - 1) / b
                //
                // Why use this formula?
                // - It avoids floating-point math
                // - It is exact for positive integers
                //
                // Example:
                //   ceil(7 / 4) = (7 + 4 - 1) / 4 = 10 / 4 = 2
                //   ceil(11 / 4) = (11 + 4 - 1) / 4 = 14 / 4 = 3
                totalMinutes += (guide + (long)mid - 1) / mid;

                // Small optimization:
                // If totalMinutes already exceeds limit, we can stop early.
                //
                // Why this is safe:
                // We only care whether totalMinutes <= limit.
                // Once it becomes larger than limit, this speed is definitely too slow.
                if (totalMinutes > limit)
                {
                    break;
                }
            }

            // Step 5:
            // Use the monotonic property to shrink the search range.
            //
            // Case A: totalMinutes <= limit
            // - This speed works.
            // - But we want the MINIMUM working speed.
            // - So we keep mid as a candidate and search the left half, including mid.
            //
            // Case B: totalMinutes > limit
            // - This speed does NOT work.
            // - We need a faster speed.
            // - So search the right half, excluding mid.
            if (totalMinutes <= limit)
            {
                right = mid;
            }
            else
            {
                left = mid + 1;
            }
        }

        // Step 6:
        // When the loop ends, left == right.
        // That value is the smallest speed that satisfies the condition.
        return left;
    }
}

// Demo code
var solution = new Solution();

// Example 1:
// guides = [7, 11, 5], limit = 8
// Expected output: 4
//
// Quick verification:
// speed 4 => ceil(7/4)+ceil(11/4)+ceil(5/4) = 2+3+2 = 7 <= 8
// speed 3 => 3+4+2 = 9 > 8
// So answer is 4.
int[] guides1 = { 7, 11, 5 };
int limit1 = 8;
int result1 = solution.MinPlaybackSpeed(guides1, limit1);
Console.WriteLine(result1);

// Example 2:
// guides = [12, 3, 9, 6], limit = 4
// Expected output: 12
//
// Quick verification:
// There are 4 sections, and limit is 4.
// So each section must take exactly 1 minute.
// That requires speed >= every section length, so speed must be at least max = 12.
// speed 12 => 1 + 1 + 1 + 1 = 4
// Any smaller speed makes the section of length 12 take at least 2 minutes? Let's check:
// speed 11 => ceil(12/11)=2, so total > 4
// Therefore answer is 12.
int[] guides2 = { 12, 3, 9, 6 };
int limit2 = 4;
int result2 = solution.MinPlaybackSpeed(guides2, limit2);
Console.WriteLine(result2);

// Additional demo: impossible case
// guides = [5, 8, 2], limit = 2
// There are 3 sections, so minimum possible total time is 3.
// Since 2 < 3, answer must be -1.
int[] guides3 = { 5, 8, 2 };
int limit3 = 2;
int result3 = solution.MinPlaybackSpeed(guides3, limit3);
Console.WriteLine(result3);