/*
Title: Minimum Download Speed for Expiring Mirror Links

Problem Description:
You are given a list of file downloads that must be completed using a single downloader.
The downloader processes the files in the given order, and it can work on only one file at a time.

File i has:
- sizes[i] megabytes
- expires[i] minutes

If you choose a constant download speed of S megabytes per minute, then file i requires:
ceil(sizes[i] / S)
whole minutes to finish.

A file is considered successful only if the cumulative time spent downloading files 0 through i
is less than or equal to expires[i].

Your task is to find the minimum integer download speed S such that all files can be completed
before their mirror links expire. If no speed can make the schedule feasible, return -1.

Important details:
- Files must be processed in the given order.
- Only one file can be downloaded at a time.
- Even partial minutes count as a full minute for each individual file.
- If a speed S works, then any larger speed also works.
  This monotonic property allows binary search on the answer.

Constraints:
- 1 <= n == sizes.length == expires.length <= 2 * 10^5
- 1 <= sizes[i] <= 10^12
- 1 <= expires[i] <= 10^18
*/

using System;
using System.Linq;

public class Solution
{
    /*
    Time Complexity:
    - Feasibility check for one speed: O(n)
    - Binary search over speed range: O(log M), where M = max(sizes)
    - Total: O(n log M)

    Space Complexity:
    - O(1) extra space, ignoring input storage

    Beginner-friendly idea:
    1. We need the smallest speed S that lets every file finish by its deadline.
    2. For a fixed speed S, we can simulate the downloads in order and check whether all deadlines are met.
    3. If speed S works, then any speed > S also works.
       That means the answer space is monotonic, so binary search is the perfect tool.
    */
    public long MinimumDownloadSpeed(long[] sizes, long[] expires)
    {
        int n = sizes.Length;

        // Step 1:
        // Before doing any binary search, we perform a very important impossibility check.
        //
        // Why is this necessary?
        // Even if the speed were "infinite", each file would still take at least 1 whole minute,
        // because the problem says each file's time is ceil(size / S), and for any positive size,
        // that value can never go below 1.
        //
        // Therefore, the absolute best possible cumulative completion times are:
        // file 0 finishes at time 1
        // file 1 finishes at time 2
        // file 2 finishes at time 3
        // ...
        // file i finishes at time i + 1
        //
        // If expires[i] < i + 1 for any file, then no speed can ever make the schedule feasible.
        for (int i = 0; i < n; i++)
        {
            if (expires[i] < i + 1L)
            {
                return -1;
            }
        }

        // Step 2:
        // Establish the binary search range for the answer.
        //
        // Lower bound:
        // The speed must be at least 1, because speed is an integer download rate.
        long left = 1;

        // Upper bound:
        // A safe upper bound is max(sizes).
        //
        // Why is this enough?
        // If S >= sizes[i], then ceil(sizes[i] / S) = 1 for that file.
        // So if S is at least the largest file size, then every file takes exactly 1 minute.
        // We already checked above whether that best-case schedule is possible.
        // Therefore, if a solution exists at all, one exists within [1, max(sizes)].
        long right = 0;
        for (int i = 0; i < n; i++)
        {
            if (sizes[i] > right)
            {
                right = sizes[i];
            }
        }

        long answer = -1;

        // Step 3:
        // Standard binary search on the speed.
        //
        // Invariant:
        // - If a speed is feasible, all larger speeds are also feasible.
        // - If a speed is not feasible, all smaller speeds are also not feasible.
        //
        // So we search for the first feasible speed.
        while (left <= right)
        {
            long mid = left + (right - left) / 2;

            // Check whether this candidate speed works.
            if (IsFeasible(sizes, expires, mid))
            {
                // mid works, so it is a valid candidate answer.
                answer = mid;

                // But we want the minimum feasible speed,
                // so we continue searching on the left half.
                right = mid - 1;
            }
            else
            {
                // mid does not work, so we need a larger speed.
                left = mid + 1;
            }
        }

        return answer;
    }

    private bool IsFeasible(long[] sizes, long[] expires, long speed)
    {
        // This variable stores the cumulative time spent downloading files so far.
        //
        // We use long because:
        // - n can be as large as 2 * 10^5
        // - expires[i] can be as large as 10^18
        // - cumulative time can also become very large
        long cumulativeTime = 0;

        // We process files strictly in the given order, because the problem does not allow reordering.
        for (int i = 0; i < sizes.Length; i++)
        {
            // Compute the time needed for the current file at the chosen speed.
            //
            // We need ceil(sizes[i] / speed), but we want to avoid floating-point arithmetic
            // because integer math is exact and safer for large values.
            //
            // Standard integer formula:
            // ceil(a / b) = (a + b - 1) / b
            //
            // This works for positive integers a and b.
            long timeForCurrentFile = (sizes[i] + speed - 1) / speed;

            // Add this file's time to the total elapsed time.
            cumulativeTime += timeForCurrentFile;

            // Immediately check whether the current file misses its expiration time.
            //
            // Why check here instead of after the loop?
            // Because the condition must hold for every prefix:
            // files 0..i must finish by expires[i].
            //
            // Also, early exit is efficient:
            // as soon as one deadline is missed, this speed is impossible.
            if (cumulativeTime > expires[i])
            {
                return false;
            }
        }

        // If we finished the loop without missing any deadline,
        // then this speed is feasible.
        return true;
    }
}

// Demo code:
// Creates sample inputs from the problem statement,
// calls the solution, and prints the results.

var solution = new Solution();

// Example 1
long[] sizes1 = { 8, 5, 10 };
long[] expires1 = { 3, 5, 9 };
long result1 = solution.MinimumDownloadSpeed(sizes1, expires1);
Console.WriteLine(result1); // Expected: 3

// Example 2
long[] sizes2 = { 4, 4, 4 };
long[] expires2 = { 1, 2, 2 };
long result2 = solution.MinimumDownloadSpeed(sizes2, expires2);
Console.WriteLine(result2); // Expected: -1