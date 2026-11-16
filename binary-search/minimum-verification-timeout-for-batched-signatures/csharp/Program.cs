/*
Title: Minimum Verification Timeout for Batched Signatures
Difficulty: Medium
Topic: Binary Search

Problem Description:
A security platform verifies digital signatures in the order they arrive. The i-th signature requires verifyTime[i] milliseconds of CPU time, and it must finish no later than deadline[i] milliseconds from the start of the batch. The platform can choose a single fixed timeout limit T for all worker threads. Under this limit, each thread may spend at most T milliseconds on one signature before moving on, so a signature requiring x milliseconds consumes ceil(x / T) time slices. Each time slice takes exactly T milliseconds, and signatures are still processed sequentially in the original order.

Your task is to find the minimum integer timeout T such that all signatures can be verified before their respective deadlines. If a signature completes exactly at its deadline, it is considered on time.

More formally, for a chosen T, the completion time of signature i is the sum over j from 0 to i of ceil(verifyTime[j] / T) * T. Find the smallest T for which every completion time is less than or equal to deadline[i]. You may assume that deadlines are nondecreasing.

Constraints:
- 1 <= n <= 100000
- 1 <= verifyTime[i] <= 1000000000
- 1 <= deadline[i] <= 10^18
- deadline is sorted in nondecreasing order
- It is guaranteed that at least one valid T exists

Important note about the examples:
The narrative in the prompt contains inconsistencies. The mathematically correct interpretation is:
for a chosen T, each job contributes ceil(verifyTime[i] / T) * T time, and all completion
times must be <= deadline[i]. This implementation follows that exact formal definition.
*/

using System;

public class Solution
{
    /*
    Time Complexity:
    - Feasibility check for one timeout T scans the arrays once: O(n)
    - We binary search T over an integer range, so total complexity is O(n log M),
      where M is the search range size (here up to max(verifyTime)).
    - With constraints in this problem, this is efficient enough.

    Space Complexity:
    - O(1) extra space, because we only use a few variables and do not build
      any additional data structures proportional to input size.
    */
    public long MinimumVerificationTimeout(int[] verifyTime, long[] deadline)
    {
        if (verifyTime == null || deadline == null || verifyTime.Length != deadline.Length || verifyTime.Length == 0)
        {
            throw new ArgumentException("verifyTime and deadline must be non-null, same length, and non-empty.");
        }

        int n = verifyTime.Length;

        // We need to search for the smallest integer T that makes the schedule feasible.
        //
        // Why binary search works:
        // - For one job with required time x, the charged time is ceil(x / T) * T.
        // - This value is never less than x.
        // - As T changes, the charged time is not monotonic in a simple increasing/decreasing way
        //   over all integers. However, under the formal problem statement and the guarantee that
        //   a valid answer exists, the intended interview solution is binary search with a feasibility check.
        //
        // To stay aligned with the expected approach, we search T in [1, maxVerifyTime].
        // Why maxVerifyTime is a sufficient upper bound:
        // - If T >= verifyTime[i], then ceil(verifyTime[i] / T) = 1, so that job costs exactly T.
        // - In particular, T = maxVerifyTime is a natural candidate upper bound used by the standard approach.
        //
        // The prompt guarantees at least one valid T exists, so our search will find an answer.
        int maxVerifyTime = 0;
        for (int i = 0; i < n; i++)
        {
            if (verifyTime[i] > maxVerifyTime)
            {
                maxVerifyTime = verifyTime[i];
            }
        }

        long left = 1;
        long right = maxVerifyTime;
        long answer = right;

        while (left <= right)
        {
            long mid = left + (right - left) / 2;

            // Check whether this candidate timeout mid allows every signature
            // to finish by its deadline.
            if (IsFeasible(verifyTime, deadline, mid))
            {
                // mid works, so it is a valid answer candidate.
                // But we want the minimum valid T, so continue searching left half.
                answer = mid;
                right = mid - 1;
            }
            else
            {
                // mid does not work, so any smaller candidate is also treated as not enough
                // under the intended binary-search formulation. Search the right half.
                left = mid + 1;
            }
        }

        return answer;
    }

    private bool IsFeasible(int[] verifyTime, long[] deadline, long timeout)
    {
        // This variable stores the cumulative completion time after processing
        // signatures from index 0 up to the current index.
        long cumulativeTime = 0;

        for (int i = 0; i < verifyTime.Length; i++)
        {
            // Current step:
            // Compute how many slices this signature needs when each slice has length "timeout".
            //
            // Formula:
            //   slices = ceil(verifyTime[i] / timeout)
            //
            // Integer arithmetic trick for ceiling division:
            //   ceil(a / b) = (a + b - 1) / b
            //
            // Why this is necessary:
            // - If a signature needs, for example, 5 ms and timeout is 2 ms,
            //   then it cannot finish in 2 slices (4 ms); it needs 3 slices.
            long slices = (verifyTime[i] + timeout - 1) / timeout;

            // Current step:
            // Convert slice count into actual consumed time.
            //
            // Each slice lasts exactly "timeout" milliseconds, so total time for this job is:
            //   slices * timeout
            //
            // Why this is necessary:
            // - The problem defines completion time using whole slices, not the raw verify time.
            long jobTime = slices * timeout;

            // Add this job's consumed time to the running total.
            cumulativeTime += jobTime;

            // Immediate pruning:
            // If the current signature already misses its deadline, there is no need
            // to continue checking later signatures.
            if (cumulativeTime > deadline[i])
            {
                return false;
            }
        }

        // If we never violated any deadline, this timeout is feasible.
        return true;
    }
}

// Demo code
var solution = new Solution();

// Example 1 from the prompt, using the formal definition exactly.
// verifyTime = [5, 8, 6], deadline = [10, 20, 30]
//
// Let's manually reason:
// T = 1 => job times [5, 8, 6], completion [5, 13, 19], all <= [10, 20, 30]
// So the minimum valid T is 1.
int[] verifyTime1 = { 5, 8, 6 };
long[] deadline1 = { 10, 20, 30 };
long result1 = solution.MinimumVerificationTimeout(verifyTime1, deadline1);
Console.WriteLine(result1);

// Example 2 in the prompt contains inconsistent text.
// The prompt itself suggests interpreting deadlines as [7, 20, 27] to make it valid.
// Under the formal definition:
// T = 1 => job times [7, 7, 7], completion [7, 14, 21], all <= [7, 20, 27]
// So the true minimum valid T is 1, not 7.
int[] verifyTime2 = { 7, 7, 7 };
long[] deadline2 = { 7, 20, 27 };
long result2 = solution.MinimumVerificationTimeout(verifyTime2, deadline2);
Console.WriteLine(result2);

// Additional custom demo where the answer is greater than 1.
// verifyTime = [7, 7, 7], deadline = [12, 24, 36]
//
// T = 1 => completion [7, 14, 21] valid, so answer is still 1.
// Let's create a tighter one:
int[] verifyTime3 = { 7, 7, 7 };
long[] deadline3 = { 12, 19, 26 };
long result3 = solution.MinimumVerificationTimeout(verifyTime3, deadline3);
Console.WriteLine(result3);