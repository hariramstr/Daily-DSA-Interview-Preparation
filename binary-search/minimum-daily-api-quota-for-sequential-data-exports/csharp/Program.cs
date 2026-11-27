/*
Title: Minimum Daily API Quota for Sequential Data Exports
Difficulty: Medium
Topic: Binary Search

Problem Description:
A company needs to export analytics data for n clients in the given order. The i-th client requires requests[i] API calls to finish its export. Because of compliance rules, exports cannot be split across days out of order: each day you process a contiguous block of clients, and once you move to the next day, you cannot go back to an earlier client. You are also not allowed to split one client's export across multiple days, so the daily quota must be at least as large as the largest single value in requests.

Given an integer array requests and an integer d, return the minimum daily API quota needed to finish all exports within at most d days.

This is a decision-and-search problem: for any proposed daily quota Q, you can check whether all exports can be completed in at most d days while preserving order. Your task is to find the smallest feasible Q.

Constraints:
- 1 <= requests.length <= 100000
- 1 <= requests[i] <= 1000000000
- 1 <= d <= requests.length

Example 1:
Input: requests = [7, 2, 5, 10, 8], d = 2
Output: 18
Explanation: With quota 18, one valid split is [7, 2, 5] and [10, 8]. A quota of 17 is not enough because preserving order would require 3 days.

Example 2:
Input: requests = [4, 4, 4, 4, 4], d = 3
Output: 8
Explanation: Using quota 8, the exports can be grouped as [4, 4], [4, 4], [4]. Any quota below 8 would force at least 4 days.
*/

using System;

public class Solution
{
    /*
        Time Complexity:
        - O(n * log(S))
        - n = number of clients
        - S = range of possible answers, from max(requests) to sum(requests)
        Explanation:
        We use binary search over the answer space. For each candidate daily quota,
        we scan the array once to check if that quota is feasible.

        Space Complexity:
        - O(1)
        Explanation:
        We only use a few variables and do not allocate extra data structures
        proportional to the input size.
    */
    public long MinimumDailyQuota(int[] requests, int d)
    {
        // Step 1:
        // We need to determine the search boundaries for binary search.
        //
        // Why do we need boundaries?
        // Binary search works on a sorted search space. Here, the "search space"
        // is all possible daily quota values. We need a minimum possible quota
        // and a maximum possible quota.
        //
        // Lower bound:
        // The quota must be at least the largest single request value,
        // because one client's export cannot be split across multiple days.
        //
        // Upper bound:
        // In the worst case, we do everything in one day, so the quota could be
        // the sum of all requests.
        long left = 0;
        long right = 0;

        // We scan through the input once to compute:
        // - left  = max(requests)
        // - right = sum(requests)
        //
        // We use long instead of int because:
        // requests[i] can be as large as 1,000,000,000
        // and there can be up to 100,000 elements,
        // so the total sum can exceed the int range.
        foreach (int request in requests)
        {
            if (request > left)
            {
                left = request;
            }

            right += request;
        }

        // Step 2:
        // Perform binary search on the answer.
        //
        // Invariant:
        // - Any quota smaller than the true answer is not feasible.
        // - The true answer is somewhere in [left, right].
        //
        // Goal:
        // Find the smallest feasible quota.
        while (left < right)
        {
            // Step 2a:
            // Pick the middle quota candidate.
            //
            // We use this form to avoid overflow:
            // mid = left + (right - left) / 2
            long mid = left + (right - left) / 2;

            // Step 2b:
            // Check whether this candidate quota is enough to finish
            // all exports within at most d days.
            //
            // If it is feasible:
            //   We try to find an even smaller feasible quota,
            //   so we move the right boundary down to mid.
            //
            // If it is not feasible:
            //   We must increase the quota,
            //   so we move the left boundary up to mid + 1.
            if (CanFinishWithinDays(requests, d, mid))
            {
                right = mid;
            }
            else
            {
                left = mid + 1;
            }
        }

        // Step 3:
        // When left == right, binary search has converged.
        // That value is the smallest feasible daily quota.
        return left;
    }

    private bool CanFinishWithinDays(int[] requests, int d, long quota)
    {
        // This helper answers the decision problem:
        // "If the daily quota is 'quota', can we finish all exports in at most d days?"
        //
        // This is the key idea behind binary search on the answer:
        // - If a quota works, then any larger quota also works.
        // - If a quota does not work, then any smaller quota also does not work.
        //
        // That monotonic behavior is exactly what binary search needs.

        // Step 1:
        // Start with day 1.
        //
        // Why start at 1 instead of 0?
        // Because if there is at least one client, we will need at least one day.
        int daysUsed = 1;

        // Step 2:
        // Track how many API calls have already been assigned to the current day.
        long currentDayLoad = 0;

        // Step 3:
        // Process clients in the given order.
        //
        // We must preserve order, so we cannot rearrange requests.
        // We greedily place each client into the current day if it fits.
        // Otherwise, we start a new day.
        //
        // Why is greedy correct here?
        // Because for a fixed quota, packing as much as possible into the current day
        // never hurts. Starting a new day earlier would only waste capacity and could
        // increase the number of days used.
        foreach (int request in requests)
        {
            // If adding this client's export to the current day would exceed the quota,
            // then this client must start a new day.
            if (currentDayLoad + request > quota)
            {
                daysUsed++;
                currentDayLoad = request;

                // Important optimization:
                // If we already exceeded the allowed number of days,
                // we can stop early and return false.
                if (daysUsed > d)
                {
                    return false;
                }
            }
            else
            {
                // Otherwise, this client's export fits in the current day,
                // so we add it to the current day's load.
                currentDayLoad += request;
            }
        }

        // If we processed all clients without exceeding d days,
        // then this quota is feasible.
        return true;
    }
}

// Demo code:
// Create sample inputs, call the solution, and print the results.

var solution = new Solution();

// Example 1:
// requests = [7, 2, 5, 10, 8], d = 2
// Expected output: 18
int[] requests1 = { 7, 2, 5, 10, 8 };
int d1 = 2;
long result1 = solution.MinimumDailyQuota(requests1, d1);
Console.WriteLine($"Example 1 Result: {result1}");

// Example 2:
// requests = [4, 4, 4, 4, 4], d = 3
// Expected output: 8
int[] requests2 = { 4, 4, 4, 4, 4 };
int d2 = 3;
long result2 = solution.MinimumDailyQuota(requests2, d2);
Console.WriteLine($"Example 2 Result: {result2}");

// Additional quick sanity check:
// If d equals the number of clients, the answer should be the maximum single request,
// because we can process one client per day if needed.
int[] requests3 = { 3, 1, 9, 2 };
int d3 = 4;
long result3 = solution.MinimumDailyQuota(requests3, d3);
Console.WriteLine($"Additional Check Result: {result3}");