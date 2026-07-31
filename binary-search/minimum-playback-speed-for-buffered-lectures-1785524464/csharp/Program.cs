/*
Title: Minimum Playback Speed for Buffered Lectures
Difficulty: Medium
Topic: Binary Search

Problem Description:
You are given a list of lecture video lengths in minutes, where lectures must be watched in the given order.
A student has exactly H hours before an exam and wants to finish all lectures on time.
The player supports variable playback speed, but the same speed must be used for every lecture.

If the playback speed is s, a lecture with length x minutes takes ceil(x / s) minutes to finish,
because even a partially watched final minute still consumes a full minute block in the study planner.
For example, at speed 3, a 7-minute lecture takes ceil(7 / 3) = 3 minutes.

Return the minimum positive integer playback speed s such that the total time needed to watch all lectures
is at most H * 60 minutes. If it is impossible even with arbitrarily large integer speed under this rounding rule,
return -1.

This problem is designed to test whether you can recognize a monotonic condition and search over the answer space efficiently.

Constraints:
- 1 <= lectures.length <= 100000
- 1 <= lectures[i] <= 10^9
- 1 <= H <= 10^9
- All values are integers.

Notes:
- The student cannot split a lecture across different speeds.
- Because of the ceiling rule, each lecture requires at least 1 minute, no matter how large the speed is.
- Therefore, if lectures.length > H * 60, the answer is immediately -1.

Example 1:
Input: lectures = [30, 11, 23, 4, 20], H = 1
Correct Output: 2
Explanation:
Available time = 1 * 60 = 60 minutes.
At speed 1: 30 + 11 + 23 + 4 + 20 = 88 minutes -> too slow.
At speed 2: ceil(30/2) + ceil(11/2) + ceil(23/2) + ceil(4/2) + ceil(20/2)
          = 15 + 6 + 12 + 2 + 10
          = 45 minutes -> fits.
Since speed 1 fails and speed 2 works, the minimum valid speed is 2.

Example 2:
Input: lectures = [100, 200, 300], H = 0
Output: -1
Explanation:
Available time = 0 minutes, so it is impossible to watch any positive-length lecture.

*/

using System;

public class Solution
{
    /*
    Time Complexity:
    - O(n log M)
      where:
      n = number of lectures
      M = maximum lecture length
    Explanation:
    - For each candidate speed tested by binary search, we scan all lectures once to compute total required time.
    - Binary search checks O(log M) speeds.

    Space Complexity:
    - O(1) extra space
    Explanation:
    - We only use a few variables and do not allocate extra data structures proportional to input size.
    */
    public int MinPlaybackSpeed(int[] lectures, long H)
    {
        // Step 1:
        // Convert the available time from hours to minutes.
        //
        // Why:
        // The lecture lengths are given in minutes, and the formula ceil(x / s) also produces minutes.
        // So we must compare everything in the same unit.
        //
        // We use long instead of int because H can be large, and H * 60 should be safe from overflow.
        long availableMinutes = H * 60L;

        // Step 2:
        // Handle impossible cases immediately.
        //
        // Why:
        // Even with an extremely large playback speed, each lecture still costs at least 1 minute
        // because of the ceiling rule.
        //
        // So the absolute minimum total time is exactly the number of lectures.
        // If we do not even have that many minutes available, the answer is impossible.
        if (availableMinutes <= 0 || lectures.Length > availableMinutes)
        {
            return -1;
        }

        // Step 3:
        // Find the maximum lecture length.
        //
        // Why:
        // This gives us a safe upper bound for binary search.
        //
        // Reasoning:
        // If speed = maxLectureLength, then every lecture takes at most 1 minute,
        // because each lecture length x satisfies x <= maxLectureLength, so ceil(x / maxLectureLength) <= 1.
        //
        // Since we already checked that lectures.Length <= availableMinutes,
        // this upper bound is guaranteed to be feasible.
        int maxLectureLength = 0;
        foreach (int lecture in lectures)
        {
            if (lecture > maxLectureLength)
            {
                maxLectureLength = lecture;
            }
        }

        // Step 4:
        // Set up binary search over the answer.
        //
        // Why binary search works:
        // The condition "Can we finish within availableMinutes at speed s?" is monotonic.
        //
        // - If a speed s works, then any larger speed also works,
        //   because increasing speed can only reduce or keep the same total required time.
        // - If a speed s does not work, then any smaller speed also does not work.
        //
        // This monotonic true/false pattern is exactly what binary search needs.
        int left = 1;
        int right = maxLectureLength;

        // We will shrink the search space until left == right,
        // and that value will be the minimum feasible speed.
        while (left < right)
        {
            // Step 5:
            // Pick the middle speed.
            //
            // We compute it this way to avoid overflow:
            // left + (right - left) / 2
            int mid = left + (right - left) / 2;

            // Step 6:
            // Check whether this speed is feasible.
            //
            // If feasible:
            //   mid might be the answer, but maybe there is an even smaller valid speed.
            //   So we continue searching on the left half, including mid.
            //
            // If not feasible:
            //   mid is too slow, so every speed <= mid is also too slow.
            //   We must search strictly to the right.
            if (CanFinish(lectures, mid, availableMinutes))
            {
                right = mid;
            }
            else
            {
                left = mid + 1;
            }
        }

        // Step 7:
        // At this point, left == right and points to the smallest feasible speed.
        return left;
    }

    private bool CanFinish(int[] lectures, int speed, long availableMinutes)
    {
        // This variable stores the total number of minutes required
        // to watch all lectures at the given playback speed.
        //
        // We use long because the sum can become large.
        long totalRequiredMinutes = 0;

        // Step through every lecture in order.
        //
        // Why:
        // The total time is the sum of the rounded-up time for each lecture.
        // The order does not affect the sum, but the problem states lectures are watched in order.
        foreach (int lecture in lectures)
        {
            // Compute ceil(lecture / speed) using integer arithmetic.
            //
            // Formula:
            // ceil(a / b) = (a + b - 1) / b   for positive integers
            //
            // Why use this formula:
            // It avoids floating-point math and is exact.
            long minutesForThisLecture = (lecture + (long)speed - 1) / speed;

            // Add this lecture's required time to the running total.
            totalRequiredMinutes += minutesForThisLecture;

            // Early exit optimization:
            //
            // If we already exceeded the available time, there is no need
            // to process the remaining lectures.
            //
            // Why this is helpful:
            // It can save time on large inputs when a speed is clearly too slow.
            if (totalRequiredMinutes > availableMinutes)
            {
                return false;
            }
        }

        // If we finished summing all lectures without exceeding the limit,
        // then this speed is feasible.
        return totalRequiredMinutes <= availableMinutes;
    }
}

// Demo code

var solution = new Solution();

// Example 1
int[] lectures1 = { 30, 11, 23, 4, 20 };
long h1 = 1;
int result1 = solution.MinPlaybackSpeed(lectures1, h1);
Console.WriteLine(result1); // Expected: 2

// Example 2
int[] lectures2 = { 100, 200, 300 };
long h2 = 0;
int result2 = solution.MinPlaybackSpeed(lectures2, h2);
Console.WriteLine(result2); // Expected: -1

// Additional quick checks
int[] lectures3 = { 60 };
long h3 = 1;
int result3 = solution.MinPlaybackSpeed(lectures3, h3);
Console.WriteLine(result3); // Expected: 1

int[] lectures4 = { 100, 100, 100 };
long h4 = 1; // 60 minutes available, speed 5 => 20+20+20 = 60
int result4 = solution.MinPlaybackSpeed(lectures4, h4);
Console.WriteLine(result4); // Expected: 5