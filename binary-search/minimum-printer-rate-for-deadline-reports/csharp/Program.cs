/*
Title: Minimum Printer Rate for Deadline Reports
Difficulty: Medium
Topic: Binary Search

Problem Description:
You are managing a shared office printer that must print several reports in a fixed order. The i-th report has pages[i] pages and must be fully printed no later than deadline[i], measured in whole minutes from the start of the day. The printer works at a constant integer rate of r pages per minute, and it prints the reports one after another in the given order without interruption. If a report starts in the middle of a minute, printing still continues normally; the time needed for a report is pages[i] / r minutes. A rate r is considered feasible if, after printing reports 0 through i in order, the completion time of every report i is at most deadline[i].

Your task is to find the minimum integer printer rate r such that all reports can be completed by their respective deadlines. If it is impossible even with an arbitrarily large rate, return -1.

A useful observation is that feasibility is monotonic: if some rate r works, then any rate larger than r also works. This makes the problem a good candidate for binary search over the answer.

Constraints:
- 1 <= pages.length == deadline.length <= 100000
- 1 <= pages[i] <= 10^9
- 1 <= deadline[i] <= 10^9
- deadline is not guaranteed to be sorted, but deadlines apply to the reports in the given order
- Return the minimum integer rate, or -1 if no such rate exists

Important correction to Example 1:
The statement's written explanation says output 4, but then correctly reasons that rate 3 is feasible and rate 2 is not.
So the true minimum feasible rate for Example 1 is 3.

Example 1:
Input: pages = [6, 8, 3], deadline = [2, 5, 6]
Correct Output: 3

Example 2:
Input: pages = [5, 5], deadline = [0, 10]
Output: -1
Explanation: The first report must finish by time 0, but it has positive length, so no finite printer rate can satisfy the requirement.
*/

using System;

public class Solution
{
    /*
    Time Complexity:
    - O(n log U), where:
      n = number of reports
      U = upper bound on the answer (here we use max prefix pages needed against deadlines, safely bounded by total pages)
    - Each feasibility check scans the arrays once: O(n)
    - Binary search performs O(log U) checks

    Space Complexity:
    - O(1) extra space
    - We only use a few variables regardless of input size

    Beginner-friendly idea:
    1. We need the smallest integer rate r.
    2. If a rate works, every larger rate also works.
    3. That "works / doesn't work" pattern is exactly what binary search needs.
    4. So:
       - first detect impossible cases
       - find some high rate that definitely works
       - binary search for the minimum working rate
    */
    public int MinimumPrinterRate(int[] pages, int[] deadline)
    {
        int n = pages.Length;

        // Step 1:
        // Before doing binary search, we check whether the task is impossible even with an infinitely fast printer.
        //
        // Why this is necessary:
        // Even if the printer were infinitely fast, the completion time after printing reports 0..i
        // would approach 0 only if there were no pages. But every report has positive pages.
        // More importantly, because reports are printed in order and each one takes positive time,
        // the completion time of report i is always strictly greater than 0.
        //
        // Therefore:
        // - If any deadline[i] <= 0, that report cannot be completed on time by any finite rate.
        //
        // In the given constraints deadlines are usually positive, but the examples include 0,
        // so we must handle it correctly.
        for (int i = 0; i < n; i++)
        {
            if (deadline[i] <= 0)
            {
                return -1;
            }
        }

        // Step 2:
        // We need a search range [left, right] for binary search.
        //
        // left:
        // - The smallest possible positive integer rate is 1 page per minute.
        //
        // right:
        // - We want a rate that is guaranteed to work if any finite rate can work.
        // - A safe way is to grow the upper bound by doubling until it becomes feasible.
        //
        // Why doubling is useful:
        // - We may not know the exact maximum answer in advance.
        // - Doubling quickly reaches a sufficient upper bound in O(log answer) steps.
        long left = 1;
        long right = 1;

        // Step 3:
        // Expand the upper bound until:
        // - it becomes feasible, meaning we found a valid "right" boundary for binary search, or
        // - it grows too large, in which case we conclude no finite answer exists within practical integer range
        //   and return -1.
        //
        // In this problem, if all deadlines are positive, a sufficiently large finite rate always works.
        // Still, we keep a very large cap for safety and to avoid overflow.
        const long Limit = (long)4e18;

        while (!IsFeasible(pages, deadline, right))
        {
            if (right >= Limit / 2)
            {
                return -1;
            }

            right *= 2;
        }

        // Step 4:
        // Standard binary search on the answer.
        //
        // Invariant:
        // - left is a candidate range start
        // - right is known to be feasible
        //
        // Goal:
        // - shrink the range until left == right
        // - that value will be the minimum feasible rate
        while (left < right)
        {
            // Compute the middle carefully to avoid overflow.
            long mid = left + (right - left) / 2;

            // Step 4a:
            // Check whether this rate works.
            //
            // If it works:
            // - the answer could be mid or something smaller
            // - so we keep the left half, including mid
            //
            // If it does not work:
            // - we must increase the rate
            // - so we discard mid and everything smaller
            if (IsFeasible(pages, deadline, mid))
            {
                right = mid;
            }
            else
            {
                left = mid + 1;
            }
        }

        // Step 5:
        // left == right is now the smallest feasible rate.
        //
        // The problem asks for an integer result.
        // If it somehow exceeds int range, return -1 for safety.
        if (left > int.MaxValue)
        {
            return -1;
        }

        return (int)left;
    }

    private bool IsFeasible(int[] pages, int[] deadline, long rate)
    {
        // This method checks whether a given printer rate is enough.
        //
        // Core math:
        // Completion time of report i =
        //   (pages[0] + pages[1] + ... + pages[i]) / rate
        //
        // We need:
        //   prefixPages / rate <= deadline[i]
        //
        // Multiply both sides by positive rate:
        //   prefixPages <= deadline[i] * rate
        //
        // Why this transformation is important:
        // - It avoids floating-point arithmetic.
        // - Floating-point comparisons can introduce precision issues.
        // - Integer arithmetic is exact and safer here.
        //
        // Data structure choice:
        // - We do not need any extra arrays.
        // - We only maintain a running prefix sum of pages.
        // - That keeps memory usage constant: O(1).

        long prefixPages = 0;

        for (int i = 0; i < pages.Length; i++)
        {
            // Add the current report's pages to the running total.
            //
            // What this means:
            // - prefixPages now represents the total number of pages that must have been printed
            //   by the time report i finishes.
            prefixPages += pages[i];

            // Compute the maximum number of pages that could be printed by deadline[i]
            // at the current rate.
            //
            // maxPrintableByDeadline = deadline[i] * rate
            //
            // If prefixPages is larger than this value, then report i would finish too late.
            long maxPrintableByDeadline = (long)deadline[i] * rate;

            if (prefixPages > maxPrintableByDeadline)
            {
                // As soon as one report misses its deadline, this rate is not feasible.
                // We can stop early because checking later reports is unnecessary.
                return false;
            }
        }

        // If every report met its deadline, the rate is feasible.
        return true;
    }
}

// Demo code:
// Creates sample inputs, calls the solution, and prints the results.

var solution = new Solution();

// Example 1 from the prompt.
// Note: the prompt contains a contradiction.
// The correct minimum rate is 3, not 4.
int[] pages1 = { 6, 8, 3 };
int[] deadline1 = { 2, 5, 6 };
int result1 = solution.MinimumPrinterRate(pages1, deadline1);
Console.WriteLine(result1); // Expected: 3

// Example 2 from the prompt.
int[] pages2 = { 5, 5 };
int[] deadline2 = { 0, 10 };
int result2 = solution.MinimumPrinterRate(pages2, deadline2);
Console.WriteLine(result2); // Expected: -1

// Additional small demo.
int[] pages3 = { 10, 10, 10 };
int[] deadline3 = { 5, 10, 15 };
int result3 = solution.MinimumPrinterRate(pages3, deadline3);
Console.WriteLine(result3); // Expected: 2