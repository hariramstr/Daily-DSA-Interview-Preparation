/*
Title: Minimum Processing Rate for Deadline Batches
Difficulty: Medium
Topic: Binary Search

Problem Description:
A data platform receives analytics batches, where batch i contains batches[i] records that must be processed by a single worker.
The worker uses one fixed processing rate r records per hour for the entire day.
In one hour, the worker may process records from only one batch, and if a batch has fewer than r remaining records,
the worker still spends the full hour finishing that batch. The batches can be processed in any order.

Given an integer array batches and an integer h, return the minimum integer processing rate r such that all batches
can be completed within h hours.

This is a realistic capacity-planning problem: choosing a rate that is too low misses the deadline, while choosing a rate
that is too high may waste resources. Your task is to find the smallest feasible rate.

Constraints:
- 1 <= batches.length <= 100000
- 1 <= batches[i] <= 1000000000
- batches.length <= h <= 1000000000
- The answer always exists.

Notes:
- The time needed for one batch of size x at rate r is ceil(x / r) hours.
- Since the order of processing does not change the total hours, you only need to determine whether a candidate rate is feasible.
- An O(n log M) solution is expected, where M is the maximum batch size.

Example 1:
Input: batches = [12, 7, 25, 9], h = 10
Output: 7
Explanation:
At rate 7, the required hours are ceil(12/7) + ceil(7/7) + ceil(25/7) + ceil(9/7) = 2 + 1 + 4 + 2 = 9, which fits within 10 hours.
At rate 6, the required hours are 2 + 2 + 5 + 2 = 11, which is too slow.
So the minimum valid rate is 7.

Example 2:
Input: batches = [30, 11, 23, 4, 20], h = 6
Output: 23
Explanation:
At rate 23, the total time is 2 + 1 + 1 + 1 + 1 = 6 hours.
At rate 22, the total time becomes 2 + 1 + 2 + 1 + 1 = 7 hours, which exceeds the limit.
*/

using System;

public class Solution
{
    /*
    Time Complexity:
    - O(n log M)
      where:
      n = number of batches
      M = maximum batch size
    Explanation:
    - We binary search the answer (the processing rate) between 1 and max batch size.
    - For each candidate rate, we scan all batches once to compute required hours.
    - That gives O(log M) checks, each costing O(n).

    Space Complexity:
    - O(1)
    Explanation:
    - We only use a few variables regardless of input size.
    - No extra arrays or complex data structures are needed.
    */
    public int MinProcessingRate(int[] batches, int h)
    {
        // Step 1: Determine the search range for binary search.
        //
        // Why do we need a search range?
        // Because we are looking for the smallest integer rate r that works.
        // Binary search only works when we know:
        //   - the minimum possible answer
        //   - the maximum possible answer
        //
        // Minimum possible rate:
        //   1 record per hour
        //   We cannot process at rate 0, because then no work would be done.
        //
        // Maximum possible rate:
        //   max(batches)
        //   Why is this enough?
        //   Because if the worker can process the largest batch in one hour,
        //   then every other batch also takes at most one hour.
        //   Since h is guaranteed to be at least batches.Length, a valid answer exists.
        int left = 1;
        int right = 0;

        // Step 2: Find the largest batch size.
        //
        // We use this as the upper bound of the binary search.
        foreach (int batch in batches)
        {
            if (batch > right)
            {
                right = batch;
            }
        }

        // Step 3: Perform binary search on the answer.
        //
        // Key observation:
        // If a rate r is fast enough to finish within h hours,
        // then any larger rate will also be fast enough.
        //
        // This creates a monotonic condition:
        //   rates too small  -> not feasible
        //   rates large enough -> feasible
        //
        // That exact pattern is what binary search is designed for.
        while (left < right)
        {
            // Step 3a: Pick the middle candidate rate.
            //
            // We use this formula instead of (left + right) / 2
            // to avoid overflow in general binary search patterns.
            int mid = left + (right - left) / 2;

            // Step 3b: Check whether this candidate rate is feasible.
            //
            // If feasible:
            //   mid might be the answer, but maybe there is a smaller valid rate.
            //   So we keep searching the left half, including mid.
            //
            // If not feasible:
            //   mid is too slow, so all smaller rates are also too slow.
            //   We must search the right half.
            if (CanFinishWithinHours(batches, h, mid))
            {
                right = mid;
            }
            else
            {
                left = mid + 1;
            }
        }

        // Step 4: When left == right, binary search has converged.
        //
        // This value is the smallest feasible processing rate.
        return left;
    }

    private bool CanFinishWithinHours(int[] batches, int h, int rate)
    {
        // Step 1: Track the total number of hours needed at this rate.
        //
        // We use long instead of int because:
        // - batches.Length can be up to 100000
        // - h can be up to 1000000000
        // - the sum of required hours could exceed int during intermediate calculations
        //
        // Using long prevents overflow and keeps the logic correct.
        long totalHours = 0;

        // Step 2: For each batch, compute how many hours it needs at the given rate.
        //
        // Formula:
        //   ceil(batch / rate)
        //
        // Instead of using floating-point math, we use integer arithmetic:
        //   ceil(a / b) = (a + b - 1) / b
        //
        // Why avoid floating point?
        // - Integer math is exact here
        // - It is faster
        // - It avoids precision issues
        foreach (int batch in batches)
        {
            totalHours += (batch + (long)rate - 1) / rate;

            // Step 3: Early exit optimization.
            //
            // If we already exceed h hours, there is no need to continue.
            // The candidate rate is definitely not feasible.
            //
            // This does not change correctness.
            // It only improves performance on large inputs.
            if (totalHours > h)
            {
                return false;
            }
        }

        // Step 4: If total required hours is within the limit, the rate works.
        return totalHours <= h;
    }
}

// Demo code

var solution = new Solution();

// Example 1
int[] batches1 = { 12, 7, 25, 9 };
int h1 = 10;
int result1 = solution.MinProcessingRate(batches1, h1);
Console.WriteLine("Example 1 Result: " + result1);

// Example 2
int[] batches2 = { 30, 11, 23, 4, 20 };
int h2 = 6;
int result2 = solution.MinProcessingRate(batches2, h2);
Console.WriteLine("Example 2 Result: " + result2);

// Additional quick demo
int[] batches3 = { 3, 6, 7, 11 };
int h3 = 8;
int result3 = solution.MinProcessingRate(batches3, h3);
Console.WriteLine("Additional Demo Result: " + result3);