/*
Title: Minimum WiFi Router Radius for Linear Offices
Difficulty: Medium
Topic: Binary Search

Problem Description:
A company has rented a long hallway with offices placed along a straight line. The positions of the offices are given in a sorted integer array offices, where offices[i] is the coordinate of the i-th office. The company can install WiFi routers only at locations listed in another sorted integer array routers, where routers[j] is the coordinate of a possible router location. Every installed router uses the same signal radius r, and it covers every office whose distance from that router is at most r.

You may use any number of the available router locations, including all of them. Your task is to compute the minimum integer radius r such that every office is covered by at least one router.

Return that minimum radius.

A solution is expected to use binary search efficiently rather than checking every radius one by one. For a candidate radius, you can determine whether all offices are coverable by checking the nearest router position for each office.

Constraints:
- 1 <= offices.length, routers.length <= 2 * 10^5
- 0 <= offices[i], routers[j] <= 10^9
- offices is sorted in non-decreasing order
- routers is sorted in non-decreasing order
- The answer fits in a 32-bit signed integer

Example 1:
Input: offices = [1, 5, 9], routers = [2, 8]
Output: 3
Explanation: With radius 3, router 2 covers office 1 and 5, and router 8 covers office 9. Radius 2 is not enough because office 5 would be too far from both routers.

Example 2:
Input: offices = [2, 4, 6, 14], routers = [1, 7, 15]
Output: 2
Explanation: Office 2 is covered by router 1, offices 4 and 6 are covered by router 7, and office 14 is covered by router 15. Radius 1 fails because office 4 is not within distance 1 of any router.
*/

using System;

public class Solution
{
    /*
    Time Complexity:
    - Outer binary search over the answer range: O(log M), where M is the search range of radius values.
    - For each candidate radius, we scan through offices and routers once using two pointers: O(n + m).
    - Total: O((n + m) * log M)

    Space Complexity:
    - O(1) extra space, because we only use a few variables and do not allocate extra data structures
      proportional to input size.

    Beginner-friendly idea:
    - We are searching for the smallest radius that works.
    - If a radius works, then any larger radius also works.
    - That "works / does not work" pattern is exactly what binary search is good at.
    */
    public int FindMinimumRadius(int[] offices, int[] routers)
    {
        // Step 1:
        // We define the binary search boundaries for the answer.
        //
        // The minimum possible radius is 0.
        // The maximum possible radius can safely be 1,000,000,000 because coordinates are in [0, 1e9].
        // In the absolute worst case, an office and the nearest router could be that far apart.
        int left = 0;
        int right = 1_000_000_000;

        // Step 2:
        // Standard binary search on the answer.
        //
        // Invariant:
        // - We are looking for the smallest radius that can cover all offices.
        // - If mid works, we try smaller values by moving right to mid.
        // - If mid does not work, we must try larger values by moving left to mid + 1.
        while (left < right)
        {
            // This form avoids overflow and is the standard safe way to compute the middle.
            int mid = left + (right - left) / 2;

            // Step 3:
            // Check whether radius = mid is enough to cover every office.
            if (CanCoverAllOffices(offices, routers, mid))
            {
                // If this radius works, it might still be larger than necessary.
                // So we keep searching on the left half, including mid itself.
                right = mid;
            }
            else
            {
                // If this radius fails, every smaller radius also fails.
                // Therefore, the answer must be strictly larger than mid.
                left = mid + 1;
            }
        }

        // When left == right, binary search has converged to the smallest valid radius.
        return left;
    }

    private bool CanCoverAllOffices(int[] offices, int[] routers, int radius)
    {
        // This method answers:
        // "If every router has this radius, can all offices be covered?"
        //
        // Key observation:
        // Because both arrays are already sorted, we do NOT need to search from scratch
        // for each office. We can move through both arrays from left to right using pointers.
        //
        // Why this works:
        // - For a router at position x with radius r, its coverage interval is [x - r, x + r].
        // - Since routers are sorted, these intervals are encountered in sorted order as well.
        // - Since offices are sorted, we can greedily try to cover offices from left to right.

        int officeIndex = 0;
        int routerIndex = 0;

        // Continue while we still have offices to cover and routers to consider.
        while (officeIndex < offices.Length && routerIndex < routers.Length)
        {
            // Compute the current router's coverage interval.
            //
            // We use long here for safety in arithmetic, even though the final answer fits in int.
            // This avoids any accidental overflow when doing position +/- radius.
            long coverageStart = (long)routers[routerIndex] - radius;
            long coverageEnd = (long)routers[routerIndex] + radius;

            // Step A:
            // If the current office lies to the LEFT of this router's coverage interval,
            // then this office cannot be covered by the current router.
            //
            // More importantly, because routers are sorted, all future routers are at the same
            // position or farther to the right. Their coverage intervals will also start no earlier
            // than this one in a useful way for this office. So if this office is already too far left,
            // there is no future router that can rescue it.
            //
            // Therefore, we can immediately conclude failure.
            if (offices[officeIndex] < coverageStart)
            {
                return false;
            }

            // Step B:
            // If the current office lies to the RIGHT of this router's coverage interval,
            // then this router cannot cover that office.
            //
            // Since this router is too far left, we move to the next router and try again.
            if (offices[officeIndex] > coverageEnd)
            {
                routerIndex++;
                continue;
            }

            // Step C:
            // Otherwise, the current office is inside this router's coverage interval.
            // Since offices are sorted, this router may also cover several consecutive offices.
            //
            // We advance officeIndex as long as offices remain within [coverageStart, coverageEnd].
            while (officeIndex < offices.Length && offices[officeIndex] <= coverageEnd)
            {
                officeIndex++;
            }

            // After this loop:
            // - We have consumed every office covered by the current router.
            // - The next uncovered office, if any, lies to the right of coverageEnd.
            // - So we move to the next router.
            routerIndex++;
        }

        // If we covered all offices, officeIndex reached the end.
        // Otherwise, some offices remain uncovered.
        return officeIndex == offices.Length;
    }
}

// Demo code

var solution = new Solution();

// Example 1:
// offices = [1, 5, 9], routers = [2, 8]
// Expected output: 3
int[] offices1 = { 1, 5, 9 };
int[] routers1 = { 2, 8 };
int result1 = solution.FindMinimumRadius(offices1, routers1);
Console.WriteLine(result1);

// Example 2:
// offices = [2, 4, 6, 14], routers = [1, 7, 15]
// Expected output: 2
int[] offices2 = { 2, 4, 6, 14 };
int[] routers2 = { 1, 7, 15 };
int result2 = solution.FindMinimumRadius(offices2, routers2);
Console.WriteLine(result2);