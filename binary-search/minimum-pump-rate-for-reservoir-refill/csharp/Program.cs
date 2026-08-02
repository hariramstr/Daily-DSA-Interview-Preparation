/*
Title: Minimum Pump Rate for Reservoir Refill
Difficulty: Medium
Topic: Binary Search

Problem Description:
A city utility team must refill several reservoirs over a fixed number of nights. You are given an integer array volumes where volumes[i] is the amount of water needed for the i-th reservoir, and an integer h representing the total number of nights available.

In one night, the team chooses exactly one reservoir and pumps water into it at a constant rate of k units per night. If a reservoir needs less than k units, the remaining pumping capacity for that night is wasted and cannot be used on another reservoir. A reservoir may require multiple nights to finish, and the number of nights needed for a reservoir with volume v is ceil(v / k).

Return the minimum integer pump rate k such that all reservoirs can be completely refilled within h nights.

This is guaranteed to have a valid answer.

Constraints:
- 1 <= volumes.length <= 100000
- 1 <= volumes[i] <= 1000000000
- volumes.length <= h <= 1000000000

Example 1:
Input: volumes = [8, 5, 10, 7], h = 8
Output: 5
Explanation:
At rate 5, the required nights are:
ceil(8/5)=2, ceil(5/5)=1, ceil(10/5)=2, ceil(7/5)=2
Total = 7 nights, which fits within 8.

At rate 4, the required nights are:
ceil(8/4)=2, ceil(5/4)=2, ceil(10/4)=3, ceil(7/4)=2
Total = 9 nights, which is too many.
So the minimum valid rate is 5.

Example 2:
Input: volumes = [30, 11, 23, 4, 20], h = 6
Output: 23
Explanation:
At rate 23, the required nights are:
ceil(30/23)=2, ceil(11/23)=1, ceil(23/23)=1, ceil(4/23)=1, ceil(20/23)=1
Total = 6 nights, which fits exactly.

At any smaller rate, the total nights exceed 6.
So the answer is 23.

Key Insight:
If a pump rate k is sufficient, then any larger pump rate is also sufficient.
That means the answer space is monotonic, which makes binary search the correct and efficient approach.
*/

using System;

public class Solution
{
    /*
    Time Complexity:
    - O(n * log M)
    - n = number of reservoirs
    - M = maximum volume in the input
    Why:
    - Each binary search step checks all reservoirs once to compute total nights.
    - Binary search over the range [1, maxVolume] takes log M steps.

    Space Complexity:
    - O(1)
    Why:
    - We only use a few extra variables.
    - No additional data structures proportional to input size are created.
    */
    public int MinPumpRate(int[] volumes, int h)
    {
        // Step 1:
        // We need to binary search the answer, so we must define the search range.
        //
        // The smallest possible pump rate is 1:
        // - If we pump 1 unit per night, that is the slowest meaningful positive rate.
        //
        // The largest possible pump rate is the maximum reservoir volume:
        // - If k equals the largest volume, then even the biggest reservoir can be completed in one night.
        // - Any rate larger than that is unnecessary because it would not reduce any reservoir below 1 night.
        int left = 1;
        int right = 0;

        // Step 2:
        // Find the maximum volume to establish the upper bound of binary search.
        //
        // We scan the array once because we need the largest reservoir size.
        // This is a simple and efficient choice, and it avoids guessing an unnecessarily large upper bound.
        foreach (int volume in volumes)
        {
            if (volume > right)
            {
                right = volume;
            }
        }

        // Step 3:
        // Perform binary search on the pump rate.
        //
        // Our goal is to find the smallest k such that total required nights <= h.
        //
        // Because the condition is monotonic:
        // - If a certain k works, then any larger k also works.
        // - If a certain k does not work, then any smaller k also does not work.
        //
        // This is exactly the pattern binary search is designed for.
        while (left < right)
        {
            // Step 3a:
            // Compute the middle pump rate.
            //
            // We use this form:
            // left + (right - left) / 2
            // instead of (left + right) / 2
            // to avoid potential integer overflow in general.
            int mid = left + (right - left) / 2;

            // Step 3b:
            // Determine how many nights are needed if the pump rate is 'mid'.
            //
            // We use long for the total because:
            // - There can be up to 100000 reservoirs.
            // - Each reservoir may require many nights.
            // - The sum can exceed the range of int.
            long requiredNights = 0;

            // Step 3c:
            // For each reservoir, compute ceil(volume / mid).
            //
            // Instead of using floating-point math, we use integer arithmetic:
            // ceil(a / b) = (a + b - 1) / b
            //
            // This is important because:
            // - It is exact
            // - It is faster
            // - It avoids floating-point precision issues
            foreach (int volume in volumes)
            {
                requiredNights += (volume + mid - 1L) / mid;
            }

            // Step 3d:
            // Decide which half of the search space to keep.
            //
            // If requiredNights <= h:
            // - This rate is fast enough.
            // - But we want the MINIMUM valid rate.
            // - So we keep searching the left half, including mid itself.
            //
            // If requiredNights > h:
            // - This rate is too slow.
            // - We must search the right half for a larger rate.
            if (requiredNights <= h)
            {
                right = mid;
            }
            else
            {
                left = mid + 1;
            }
        }

        // Step 4:
        // When the loop ends, left == right.
        // That value is the smallest valid pump rate.
        return left;
    }
}

// Demo code:
// Create sample inputs, call the solution, and print the results.

var solution = new Solution();

// Example 1
int[] volumes1 = { 8, 5, 10, 7 };
int h1 = 8;
int result1 = solution.MinPumpRate(volumes1, h1);
Console.WriteLine($"Example 1 Result: {result1}"); // Expected: 5

// Example 2
int[] volumes2 = { 30, 11, 23, 4, 20 };
int h2 = 6;
int result2 = solution.MinPumpRate(volumes2, h2);
Console.WriteLine($"Example 2 Result: {result2}"); // Expected: 23

// Additional quick sanity check
int[] volumes3 = { 1, 1, 1, 1 };
int h3 = 4;
int result3 = solution.MinPumpRate(volumes3, h3);
Console.WriteLine($"Additional Example Result: {result3}"); // Expected: 1