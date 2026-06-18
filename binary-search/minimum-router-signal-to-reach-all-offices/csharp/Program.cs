/*
Title: Minimum Router Signal to Reach All Offices
Difficulty: Medium
Topic: Binary Search

Problem Description:
A company has opened offices along a straight highway. The positions of the offices are given in a sorted integer array `offices`, where `offices[i]` is the kilometer marker of the `i`-th office. You may install exactly `k` Wi-Fi routers. Every router uses the same signal strength `r`, and a router placed at position `x` covers all offices whose positions lie in the inclusive range [x - r, x + r].

You may place routers at any real-valued position, not necessarily at an office location. Your task is to compute the minimum integer signal strength `r` needed so that all offices are covered using at most `k` routers.

This is an optimization problem: for a fixed signal strength, you must determine whether it is possible to cover all offices with `k` or fewer routers, and then find the smallest feasible value.

Return the minimum integer `r`.

Constraints:
- 1 <= offices.length <= 100000
- 1 <= k <= offices.length
- 0 <= offices[i] <= 1000000000
- offices is sorted in non-decreasing order

Example 1:
Input: offices = [1, 2, 8, 12, 17], k = 2
Output: 4

Example 2:
Input: offices = [0, 4, 9, 15], k = 3
Output: 2
*/

using System;

public class Solution
{
    /*
    Time Complexity:
    - The outer binary search tries possible signal strengths from 0 up to (maxOffice - minOffice),
      which takes O(log(maxCoordinateRange)).
    - For each candidate signal strength, we run a greedy feasibility check over the offices array once,
      which takes O(n).
    - Total: O(n * log(maxCoordinateRange))

    Space Complexity:
    - O(1) extra space, because we only use a few variables and do not create additional large data structures.
    */
    public int MinimumSignalStrength(int[] offices, int k)
    {
        // If there is only one office, signal strength 0 is enough:
        // place one router exactly at that office.
        if (offices.Length <= 1)
        {
            return 0;
        }

        // Binary search boundaries:
        // - The minimum possible integer signal strength is 0.
        // - The maximum possible useful signal strength is the distance between the
        //   first and last office. With that much radius, one router could cover everything.
        int left = 0;
        int right = offices[offices.Length - 1] - offices[0];

        // We will shrink the search space until left == right.
        // At the end, that value will be the smallest feasible signal strength.
        while (left < right)
        {
            // Use the standard overflow-safe midpoint formula.
            int mid = left + (right - left) / 2;

            // Check whether signal strength "mid" is enough to cover all offices
            // using at most k routers.
            if (CanCoverAllOffices(offices, k, mid))
            {
                // If "mid" works, then maybe an even smaller signal strength also works.
                // So we keep searching on the left half, including mid itself.
                right = mid;
            }
            else
            {
                // If "mid" does NOT work, then every smaller value also cannot work.
                // So we must search strictly to the right.
                left = mid + 1;
            }
        }

        // When the loop ends, left == right and points to the minimum feasible answer.
        return left;
    }

    private bool CanCoverAllOffices(int[] offices, int k, int r)
    {
        // This method answers:
        // "If every router has signal strength r, can we cover all offices with at most k routers?"

        // Greedy strategy:
        // Always start from the leftmost office that is not yet covered.
        // If that office is at position p, the best place to put the next router is at p + r.
        // Why?
        // - A router at p + r still covers office p, because p is exactly r units to the left.
        // - This placement pushes the router as far right as possible while still covering p.
        // - Therefore, it maximizes how many future offices this router can cover.
        //
        // Coverage interval of that router becomes:
        // [ (p + r) - r, (p + r) + r ] = [ p, p + 2r ]
        //
        // So after placing the router, every office with position <= p + 2r is covered.

        int routersUsed = 0;
        int i = 0;
        int n = offices.Length;

        // Process offices from left to right.
        while (i < n)
        {
            // We are about to place one router to cover the current leftmost uncovered office.
            routersUsed++;

            // If we already used more than k routers, this signal strength is not enough.
            if (routersUsed > k)
            {
                return false;
            }

            // The current office is the leftmost uncovered one.
            // Let its position be p.
            long p = offices[i];

            // Best router center is p + r.
            // Then the farthest office this router can cover on the right is p + 2r.
            // We use long arithmetic here to be extra safe, even though int would still fit
            // under the given constraints. This is a good habit when adding large values.
            long farthestCovered = p + 2L * r;

            // Skip every office covered by this router.
            // Because the offices array is sorted, once we find an office beyond farthestCovered,
            // we know all later offices are also beyond it.
            while (i < n && offices[i] <= farthestCovered)
            {
                i++;
            }
        }

        // If we finished scanning all offices without exceeding k routers,
        // then this signal strength is feasible.
        return true;
    }
}

// Demo code

var solution = new Solution();

// Example 1
int[] offices1 = { 1, 2, 8, 12, 17 };
int k1 = 2;
int result1 = solution.MinimumSignalStrength(offices1, k1);
Console.WriteLine($"Example 1 Result: {result1}"); // Expected: 4

// Example 2
int[] offices2 = { 0, 4, 9, 15 };
int k2 = 3;
int result2 = solution.MinimumSignalStrength(offices2, k2);
Console.WriteLine($"Example 2 Result: {result2}"); // Expected: 2

// Additional quick checks
int[] offices3 = { 5 };
int k3 = 1;
int result3 = solution.MinimumSignalStrength(offices3, k3);
Console.WriteLine($"Single office Result: {result3}"); // Expected: 0

int[] offices4 = { 1, 3, 6, 10 };
int k4 = 4;
int result4 = solution.MinimumSignalStrength(offices4, k4);
Console.WriteLine($"One router per office Result: {result4}"); // Expected: 0