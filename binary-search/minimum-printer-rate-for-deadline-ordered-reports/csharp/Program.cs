/*
Title: Minimum Printer Rate for Deadline-Ordered Reports
Difficulty: Medium
Topic: Binary Search

Problem Description:
A company has one high-speed printer that must print a list of reports in the given order. The i-th report has pages[i] pages, and all reports must be fully printed within h hours total. The printer works at a constant integer rate of r pages per hour. During each hour, it can print up to r pages from the current report only. If a report finishes before the hour ends, the remaining time in that hour is wasted and the next report cannot start until the next hour. In other words, each report with x pages requires ceil(x / r) whole hours.

Your task is to find the minimum integer printing rate r such that all reports can be completed within h hours.

This problem is designed to be solved efficiently. A brute-force scan over all possible rates may be too slow when page counts are large. Think about how the total required hours changes as the printing rate increases.

Return the smallest integer r that satisfies the deadline.

Constraints:
- 1 <= pages.length <= 100000
- 1 <= pages[i] <= 1000000000
- pages.length <= h <= 1000000000000
- Reports must be printed in the given order
- The answer is guaranteed to exist

Example 1:
Input: pages = [300, 200, 400, 100], h = 8
Output: 150
Explanation: At rate 150, the hours needed are ceil(300/150)=2, ceil(200/150)=2, ceil(400/150)=3, ceil(100/150)=1, for a total of 8 hours. Any smaller rate requires more than 8 hours.

Example 2:
Input: pages = [30, 11, 23, 4, 20], h = 6
Output: 23
Explanation: With rate 23, the total time is 2 + 1 + 1 + 1 + 1 = 6 hours. At rate 22, the total becomes 2 + 1 + 2 + 1 + 1 = 7 hours, so 23 is the minimum valid rate.
*/

using System;

public class Solution
{
    /*
    Time Complexity:
    - O(n * log M)
    - n = number of reports
    - M = maximum page count among all reports
    Why:
    - For each binary search guess of the printer rate, we scan the entire pages array once
      to compute how many hours that rate would require.
    - Binary search performs about log M guesses.

    Space Complexity:
    - O(1) extra space
    Why:
    - We only use a few variables regardless of input size.
    */
    public int MinPrintingRate(int[] pages, long h)
    {
        // Step 1:
        // We need to search for the minimum integer rate r that allows all reports
        // to finish within h hours.
        //
        // A very important observation makes binary search possible:
        // - If a certain rate r is fast enough, then any larger rate is also fast enough.
        // - If a certain rate r is too slow, then any smaller rate is also too slow.
        //
        // This creates a monotonic "false, false, false, ..., true, true, true" pattern,
        // which is exactly the kind of pattern binary search is designed for.

        // Step 2:
        // Establish the search boundaries.
        //
        // Lowest possible rate:
        // - At least 1 page per hour, because the rate must be a positive integer.
        int left = 1;

        // Highest possible rate:
        // - We can safely use the maximum pages in any single report.
        // Why is this enough?
        // - If the rate equals the largest report size, then every report finishes in 1 hour.
        // - Since h >= pages.Length by constraint, this is always sufficient.
        int right = 0;
        foreach (int p in pages)
        {
            if (p > right)
            {
                right = p;
            }
        }

        // Step 3:
        // Binary search for the smallest valid rate.
        //
        // Invariant we maintain:
        // - The answer is always somewhere in the range [left, right].
        while (left < right)
        {
            // Step 3a:
            // Pick the middle rate.
            //
            // We use this overflow-safe formula instead of (left + right) / 2.
            int mid = left + (right - left) / 2;

            // Step 3b:
            // Compute how many total hours are needed if the printer runs at rate = mid.
            //
            // We use long because:
            // - h can be as large as 1,000,000,000,000
            // - the sum of required hours can exceed int range
            long neededHours = CalculateRequiredHours(pages, mid, h);

            // Step 3c:
            // Decide which half of the search space to keep.
            //
            // If neededHours <= h:
            // - This rate is fast enough.
            // - But we still want the MINIMUM valid rate.
            // - So we keep searching the left half, including mid itself.
            if (neededHours <= h)
            {
                right = mid;
            }
            else
            {
                // If neededHours > h:
                // - This rate is too slow.
                // - Therefore every rate <= mid is also too slow.
                // - We must search strictly to the right of mid.
                left = mid + 1;
            }
        }

        // Step 4:
        // When left == right, binary search has narrowed the answer down to one value.
        // That value is the smallest valid printing rate.
        return left;
    }

    private long CalculateRequiredHours(int[] pages, int rate, long limit)
    {
        // This helper method computes the total number of hours needed to print all reports
        // at a given integer rate.
        //
        // Why a helper method?
        // - It keeps the main binary search logic clean and easy to read.
        // - It separates "searching for the answer" from "checking whether a candidate works".

        long totalHours = 0;

        foreach (int reportPages in pages)
        {
            // Each report must be printed in whole hours only.
            // If a report finishes early in an hour, the remaining time is wasted.
            //
            // Therefore, a report with reportPages pages needs:
            // ceil(reportPages / rate) hours
            //
            // In integer arithmetic, we can compute ceil(a / b) as:
            // (a + b - 1) / b
            //
            // We cast to long before addition to be extra safe.
            totalHours += ((long)reportPages + rate - 1) / rate;

            // Small optimization:
            // If we already exceeded the allowed limit h, we can stop early.
            // There is no need to continue summing because this rate is already invalid.
            if (totalHours > limit)
            {
                return totalHours;
            }
        }

        return totalHours;
    }
}

// Demo code:
// Create sample inputs, call the solution, and print the results.

var solution = new Solution();

// Example 1
int[] pages1 = { 300, 200, 400, 100 };
long h1 = 8;
int result1 = solution.MinPrintingRate(pages1, h1);
Console.WriteLine(result1); // Expected: 150

// Example 2
int[] pages2 = { 30, 11, 23, 4, 20 };
long h2 = 6;
int result2 = solution.MinPrintingRate(pages2, h2);
Console.WriteLine(result2); // Expected: 23