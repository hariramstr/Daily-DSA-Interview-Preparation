/*
Title: Minimum Warehouse Robot Speed for Timed Pickups
Difficulty: Medium
Topic: Binary Search

Problem Description:
A warehouse robot must collect items from several aisles in a fixed order. You are given an integer array distances, where distances[i] is the length of aisle i in meters, and an integer array deadlines, where deadlines[i] is the latest time in seconds by which the robot must finish aisle i. The robot moves at a constant integer speed s meters per second for the entire route. The time to finish aisle i is the cumulative time spent traversing aisles 0 through i, which is the sum of distances[j] / s for all j from 0 to i. The robot is considered on time only if for every i, the cumulative time is less than or equal to deadlines[i].

Return the minimum integer speed s such that the robot can finish every aisle by its corresponding deadline. If no positive integer speed can satisfy all deadlines, return -1.

This is a decision-and-search problem: for a candidate speed, you can check whether all cumulative deadlines are met, and the feasibility is monotonic. If a speed works, any larger speed also works.

Constraints:
- 1 <= distances.length == deadlines.length <= 100000
- 1 <= distances[i] <= 1000000
- 1 <= deadlines[i] <= 1000000000
- Speed s must be a positive integer

Example 1:
Input: distances = [4, 3, 6], deadlines = [2, 4, 7]
Output: 2
Explanation: At speed 2, cumulative times are 2.0, 3.5, and 6.5, all within the deadlines. Speed 1 fails at the first aisle because 4.0 > 2.

Example 2:
Input: distances = [5, 8, 4], deadlines = [1, 2, 3]
Output: -1
Explanation: Even with arbitrarily large speed, the robot needs positive time to finish each aisle in sequence, so it cannot complete 3 aisles by time 3 if the earlier cumulative deadlines are this tight. No integer speed satisfies all constraints.
*/

using System;

public class Solution
{
    /*
    Time Complexity:
    - O(n log U), where:
      n = number of aisles
      U = searched speed range
    - Each feasibility check scans the arrays once: O(n)
    - Binary search performs O(log U) checks

    Space Complexity:
    - O(1) extra space
    - We only use a few variables regardless of input size

    Beginner-friendly idea:
    1. We do not try every speed one by one because the answer could be very large.
    2. Instead, we use binary search on the speed.
    3. For any chosen speed s, we can test whether all cumulative deadlines are satisfied.
    4. If speed s works, then any larger speed also works.
       That monotonic behavior is exactly what binary search needs.
    */
    public int MinRobotSpeed(int[] distances, int[] deadlines)
    {
        int n = distances.Length;

        // Step 1:
        // Perform a very important impossibility check before binary search.
        //
        // Why this is necessary:
        // Even if speed becomes infinitely large, each aisle still takes a positive amount of time.
        // Therefore, after finishing aisle i, the cumulative time must be strictly greater than 0,
        // and in the limit of infinite speed it approaches 0 from above for each aisle segment.
        //
        // However, the problem's own example 2 highlights a stronger practical impossibility idea:
        // to finish i + 1 aisles in sequence, the deadline must at least allow enough "ordering room".
        //
        // A clean and mathematically correct way to detect impossibility is:
        // if no finite speed can satisfy the deadlines, then even our exponential search for an upper
        // bound will fail and we return -1.
        //
        // We still keep this quick sanity check:
        // deadlines must be positive because cumulative times are positive.
        // Given constraints deadlines[i] >= 1, this is always true, but the check documents the logic.
        for (int i = 0; i < n; i++)
        {
            if (deadlines[i] <= 0)
            {
                return -1;
            }
        }

        // Step 2:
        // Find a speed that definitely works, if such a speed exists.
        //
        // Why we do this:
        // Binary search needs a search interval [left, right] where:
        // - left is a speed that may fail
        // - right is a speed that definitely works
        //
        // Since we do not know the answer in advance, we start with right = 1
        // and repeatedly double it until it works.
        //
        // This is called "exponential search" for an upper bound.
        long left = 1;
        long right = 1;

        while (right <= int.MaxValue && !CanFinish(distances, deadlines, right))
        {
            right *= 2;
        }

        // Step 3:
        // If even the largest practical 32-bit integer speed does not work, return -1.
        //
        // Why this is correct:
        // The method requires an integer speed.
        // If no speed up to int.MaxValue works, then no positive int result exists in this implementation.
        //
        // Also, because distances and deadlines are bounded, if a feasible integer speed exists,
        // it will be found before this point in normal cases.
        if (right > int.MaxValue)
        {
            right = int.MaxValue;
            if (!CanFinish(distances, deadlines, right))
            {
                return -1;
            }
        }

        // Step 4:
        // Standard binary search for the minimum working speed.
        //
        // Invariant:
        // - There exists at least one working speed in [left, right]
        // - We want the smallest such speed
        while (left < right)
        {
            // Use this form to avoid overflow:
            // mid = left + (right - left) / 2
            long mid = left + (right - left) / 2;

            // Step 4a:
            // Test whether this candidate speed is sufficient.
            if (CanFinish(distances, deadlines, mid))
            {
                // If mid works, the answer could be mid or something smaller.
                // So we keep the left half, including mid.
                right = mid;
            }
            else
            {
                // If mid fails, then every speed <= mid also fails
                // because slower speed means larger travel times.
                // So the answer must be in the right half.
                left = mid + 1;
            }
        }

        // At the end of binary search, left == right and points to the minimum feasible speed.
        return (int)left;
    }

    private bool CanFinish(int[] distances, int[] deadlines, long speed)
    {
        // This variable stores the cumulative distance traveled so far.
        //
        // Why cumulative distance instead of cumulative time directly?
        // Because cumulative time after aisle i is:
        //   (distances[0] + distances[1] + ... + distances[i]) / speed
        //
        // So we can keep a running sum of distances and compare:
        //   cumulativeDistance / speed <= deadlines[i]
        //
        // To avoid floating-point precision issues, we rewrite it as:
        //   cumulativeDistance <= deadlines[i] * speed
        //
        // This integer comparison is exact and safer than using doubles.
        long cumulativeDistance = 0;

        for (int i = 0; i < distances.Length; i++)
        {
            // Add the current aisle length to the total distance completed so far.
            cumulativeDistance += distances[i];

            // Compute the maximum distance the robot could have covered by this deadline
            // if it moves at the chosen constant speed.
            //
            // maxAllowedDistanceByDeadline = deadlines[i] * speed
            //
            // If cumulativeDistance is larger than that, then by the time aisle i should be done,
            // the robot would still be late.
            long maxAllowedDistanceByDeadline = (long)deadlines[i] * speed;

            if (cumulativeDistance > maxAllowedDistanceByDeadline)
            {
                // The chosen speed is not enough.
                return false;
            }
        }

        // If we never violated any deadline, then this speed works.
        return true;
    }
}

// Demo code:
// Create sample inputs, call the solution, and print results.

var solution = new Solution();

// Example 1
int[] distances1 = { 4, 3, 6 };
int[] deadlines1 = { 2, 4, 7 };
int result1 = solution.MinRobotSpeed(distances1, deadlines1);
Console.WriteLine(result1); // Expected: 2

// Example 2
int[] distances2 = { 5, 8, 4 };
int[] deadlines2 = { 1, 2, 3 };
int result2 = solution.MinRobotSpeed(distances2, deadlines2);
Console.WriteLine(result2); // Based on the stated formula, expected by correct math: 6

// Additional quick sanity demo
int[] distances3 = { 10 };
int[] deadlines3 = { 3 };
int result3 = solution.MinRobotSpeed(distances3, deadlines3);
Console.WriteLine(result3); // Expected: 4