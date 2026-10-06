/*
Title: Minimum Playback Rate for Training Videos
Difficulty: Medium
Topic: Binary Search

Problem Description:
A company needs to finish a sequence of employee training videos within a fixed number of hours before a compliance deadline.
You are given an integer array videos where videos[i] is the length of the i-th video in minutes, and an integer h
representing the total number of hours available.

During each hour, the player runs at a constant integer playback rate r (in minutes of video watched per hour).
If a video has length x, it takes ceil(x / r) hours to finish because a partially watched final hour still counts
as a full hour block for scheduling purposes. Videos must be completed one by one in the given order, but the playback
rate is the same for all videos.

Return the minimum integer playback rate r such that all videos can be finished within h hours.
If it is impossible even with an arbitrarily large rate because h is smaller than the number of videos, return -1.

This problem is designed to be solved efficiently. A brute-force scan of all possible rates may be too slow when video
lengths are large, so you should exploit the monotonic relationship between playback rate and total required hours.

Constraints:
- 1 <= videos.length <= 100000
- 1 <= videos[i] <= 1000000000
- 1 <= h <= 1000000000

Example 1:
Input: videos = [90, 120, 75], h = 6
Output: 60
Explanation:
At rate 45, required hours = ceil(90/45) + ceil(120/45) + ceil(75/45) = 2 + 3 + 2 = 7, so 45 is too slow.
At rate 60, required hours = 2 + 2 + 2 = 6, so 60 works.
At rate 59, required hours = 2 + 3 + 2 = 7, so 59 does not work.
Therefore, the minimum valid rate is 60.

Example 2:
Input: videos = [30, 11, 23, 4, 20], h = 5
Output: 30
Explanation:
There are 5 videos and only 5 hours, so each video must fit into a single hour.
That means the playback rate must be at least the length of the longest video, which is 30.

Key Insight:
- If a playback rate r is fast enough to finish within h hours, then any rate larger than r is also fast enough.
- If a playback rate r is too slow, then any rate smaller than r is also too slow.
- This "false, false, false, true, true, true" pattern is exactly what binary search is designed for.
*/

using System;

public class Solution
{
    /*
    Time Complexity:
    - O(n log m)
      where:
      n = number of videos
      m = maximum video length
    Explanation:
    - Each binary search step checks whether a candidate playback rate works.
    - That check scans all videos once, which costs O(n).
    - Binary search over the rate range [1, maxVideoLength] takes O(log m) steps.

    Space Complexity:
    - O(1) extra space
    Explanation:
    - We only use a few variables regardless of input size.
    - No extra arrays, lists, or recursion are used.
    */
    public int MinPlaybackRate(int[] videos, int h)
    {
        // Step 1:
        // First, handle the impossible case.
        //
        // Why is this necessary?
        // Each video takes at least 1 hour, even if the playback rate is extremely large,
        // because ceil(x / r) is always at least 1 for any positive video length x.
        //
        // So if we have more videos than available hours, there is no possible rate
        // that can finish all videos in time.
        if (h < videos.Length)
        {
            return -1;
        }

        // Step 2:
        // Determine the search boundaries for binary search.
        //
        // Left boundary (minimum possible rate):
        // - The smallest valid integer playback rate is 1 minute per hour.
        //
        // Right boundary (maximum rate we ever need to consider):
        // - We never need a rate larger than the longest video length.
        // - Why?
        //   If rate >= longest video length, then every video finishes in exactly 1 hour,
        //   because ceil(videoLength / rate) becomes 1.
        // - Any rate larger than that gives the same "1 hour per video" result,
        //   so searching beyond max video length is unnecessary.
        int left = 1;
        int right = 0;

        // We scan the array once to find the maximum video length.
        // This gives us the upper bound for binary search.
        foreach (int video in videos)
        {
            if (video > right)
            {
                right = video;
            }
        }

        // Step 3:
        // Binary search for the minimum feasible playback rate.
        //
        // Our goal is NOT just to find any working rate.
        // We want the SMALLEST working rate.
        //
        // Binary search invariant:
        // - If a rate works, search the left half to see if a smaller one also works.
        // - If a rate does not work, search the right half for a faster rate.
        while (left < right)
        {
            // Compute the middle rate safely.
            // Using left + (right - left) / 2 avoids overflow in languages where that matters.
            int mid = left + (right - left) / 2;

            // Step 4:
            // Check how many total hours are needed if we use playback rate = mid.
            //
            // We use long for totalHours because:
            // - There can be up to 100,000 videos
            // - Each can contribute many hours
            // - The sum may exceed the range of int during intermediate calculation
            long totalHours = 0;

            // We process videos one by one because the problem states they are completed sequentially.
            // However, for the total hour calculation, only the sum matters.
            foreach (int video in videos)
            {
                // Step 4a:
                // Compute ceil(video / mid) using integer arithmetic.
                //
                // Formula:
                // ceil(a / b) = (a + b - 1) / b
                //
                // Why use this formula?
                // - It avoids floating-point math
                // - It is exact and efficient
                //
                // Example:
                // video = 75, mid = 60
                // ceil(75 / 60) = (75 + 60 - 1) / 60 = 134 / 60 = 2
                totalHours += (video + (long)mid - 1) / mid;

                // Step 4b:
                // Small optimization:
                // If totalHours already exceeds h, we can stop early.
                //
                // Why is this valid?
                // - We only care whether the total is <= h or > h.
                // - Once it is already greater than h, this rate definitely fails.
                if (totalHours > h)
                {
                    break;
                }
            }

            // Step 5:
            // Decide which half of the search space to keep.
            if (totalHours <= h)
            {
                // The current rate works.
                //
                // But we are looking for the MINIMUM working rate,
                // so we keep mid as a candidate and continue searching left.
                right = mid;
            }
            else
            {
                // The current rate is too slow.
                //
                // Therefore, all rates <= mid are also too slow,
                // so we must search strictly to the right.
                left = mid + 1;
            }
        }

        // Step 6:
        // When left == right, binary search has converged.
        // That value is the smallest playback rate that works.
        return left;
    }
}

// Demo code:
// Create sample inputs, call the solution, and print results.

var solution = new Solution();

// Example 1:
// videos = [90, 120, 75], h = 6
// Expected output: 60
int[] videos1 = { 90, 120, 75 };
int h1 = 6;
int result1 = solution.MinPlaybackRate(videos1, h1);
Console.WriteLine(result1);

// Example 2:
// videos = [30, 11, 23, 4, 20], h = 5
// Expected output: 30
int[] videos2 = { 30, 11, 23, 4, 20 };
int h2 = 5;
int result2 = solution.MinPlaybackRate(videos2, h2);
Console.WriteLine(result2);

// Additional impossible-case demo:
// There are 3 videos but only 2 hours, so answer must be -1.
int[] videos3 = { 10, 20, 30 };
int h3 = 2;
int result3 = solution.MinPlaybackRate(videos3, h3);
Console.WriteLine(result3);