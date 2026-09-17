/*
Maximum Publish Delay Before Missing Ad Slots

Problem Description:
A news platform has already reserved several ad slots during the day. The i-th slot opens at time slotStart[i]
and remains available until time slotEnd[i], inclusive. Before each slot can be used, the publishing system
must finish generating a page variant, which takes exactly renderTime minutes. The system processes all
reserved slots in the given order, and it can start working on the first page only after waiting for some
initial delay D minutes.

Once the first page starts after delay D, the system renders page 0, then page 1, then page 2, and so on,
back-to-back with no idle time between pages. A slot is successfully used if the render for its page finishes
at some time t such that slotStart[i] <= t <= slotEnd[i].

Your task is to find the maximum integer delay D such that every reserved slot can still be used successfully.
If even D = 0 is impossible, return -1.

Formally, page i finishes at time D + (i + 1) * renderTime. This finish time must lie inside the i-th interval.

Constraints:
- 1 <= n <= 200000
- slotStart.length == slotEnd.length == n
- 1 <= slotStart[i] <= slotEnd[i] <= 10^18
- 1 <= renderTime <= 10^18
- The intervals are given in the required processing order and are not necessarily sorted by start or end value.

Example 1:
Input: slotStart = [5, 11, 17], slotEnd = [9, 15, 21], renderTime = 3
Output: 6

Explanation:
Page finish times are D+3, D+6, D+9.
For D = 6, finishes are 9, 12, 15, which all lie in their respective windows.
For D = 7, the first finish time becomes 10, which misses the first slot. So the maximum valid delay is 6.

Example 2:
Input: slotStart = [4, 8, 10], slotEnd = [5, 9, 11], renderTime = 3
Output: -1

Explanation:
With D = 0, finish times are 3, 6, 9.
The first page already misses its allowed slot [4, 5], so no nonnegative delay can satisfy all slots.
*/

using System;
using System.Numerics;

public class Solution
{
    /*
    Time Complexity:
    - O(n) to compute the valid range of D directly.
    - If you conceptually view it as "check + binary search", each check is O(n) and binary search is O(log M),
      where M is the search range. However, this implementation computes the same answer more directly in O(n).

    Space Complexity:
    - O(1) extra space, ignoring the input arrays.

    Beginner-friendly idea:
    For each page i, the finish time is:
        finishTime = D + (i + 1) * renderTime

    This finish time must satisfy:
        slotStart[i] <= D + (i + 1) * renderTime <= slotEnd[i]

    Rearranging for D:
        slotStart[i] - (i + 1) * renderTime <= D <= slotEnd[i] - (i + 1) * renderTime

    So every page i gives us an allowed interval for D.
    We need one single nonnegative integer D that lies inside ALL of those intervals.
    Therefore:
    - The global lower bound is the maximum of all per-page lower bounds.
    - The global upper bound is the minimum of all per-page upper bounds.
    - Also D must be >= 0.
    - The maximum valid D is simply the global upper bound, as long as it is still >= the final lower bound.

    This is mathematically equivalent to what a binary-search solution would discover.
    */
    public long MaximumDelay(long[] slotStart, long[] slotEnd, long renderTime)
    {
        // We will maintain the intersection of all valid ranges for D.
        //
        // Initially:
        // - D cannot be negative, because the problem asks for an initial delay.
        //   So the starting lower bound is 0.
        // - The upper bound starts as "very large", and we will shrink it as we process intervals.
        //
        // We use BigInteger internally for safety because:
        // - (i + 1) * renderTime can exceed long when both are large.
        // - The constraints allow values up to 1e18 and n up to 2e5, so the product can be much larger than 64-bit.
        BigInteger globalLowerBound = BigInteger.Zero;
        BigInteger globalUpperBound = BigInteger.Parse(long.MaxValue.ToString()) * BigInteger.Parse(long.MaxValue.ToString());

        // Process each slot in the required order.
        for (int i = 0; i < slotStart.Length; i++)
        {
            // Step 1:
            // Compute how much fixed rendering time has elapsed by the moment page i finishes,
            // excluding the initial delay D.
            //
            // Page 0 finishes after 1 * renderTime
            // Page 1 finishes after 2 * renderTime
            // ...
            // Page i finishes after (i + 1) * renderTime
            //
            // This value is important because the finish time is:
            //     D + offset
            BigInteger offset = (BigInteger)(i + 1) * renderTime;

            // Step 2:
            // Convert the slot constraint into a valid interval for D.
            //
            // We need:
            //     slotStart[i] <= D + offset <= slotEnd[i]
            //
            // Subtract offset from all parts:
            //     slotStart[i] - offset <= D <= slotEnd[i] - offset
            //
            // So for this page alone:
            // - D must be at least localLowerBound
            // - D must be at most localUpperBound
            BigInteger localLowerBound = (BigInteger)slotStart[i] - offset;
            BigInteger localUpperBound = (BigInteger)slotEnd[i] - offset;

            // Step 3:
            // Intersect this page's valid D-range with the global valid D-range.
            //
            // Why intersection?
            // Because the same single D must satisfy every page.
            //
            // If one page says D must be >= 5 and another says D must be >= 8,
            // then together they require D >= 8.
            //
            // If one page says D must be <= 20 and another says D must be <= 14,
            // then together they require D <= 14.
            if (localLowerBound > globalLowerBound)
            {
                globalLowerBound = localLowerBound;
            }

            if (localUpperBound < globalUpperBound)
            {
                globalUpperBound = localUpperBound;
            }

            // Step 4:
            // Early failure check.
            //
            // If at any point the lower bound becomes greater than the upper bound,
            // the intersection is empty. That means no D can satisfy all pages seen so far,
            // and therefore no final answer exists.
            if (globalLowerBound > globalUpperBound)
            {
                return -1;
            }
        }

        // Step 5:
        // After processing all pages, the valid D values are exactly:
        //     [globalLowerBound, globalUpperBound]
        //
        // We also already enforced D >= 0 by starting globalLowerBound at 0.
        //
        // Since the question asks for the MAXIMUM integer delay,
        // the best answer is the largest value still inside the valid range:
        //     D = globalUpperBound
        //
        // If the final range is empty, return -1.
        if (globalUpperBound < globalLowerBound)
        {
            return -1;
        }

        // The problem's return type expectation is long.
        // If the answer somehow exceeds long, it would not fit the requested output type.
        // Under normal competitive-programming expectations, valid answers are intended to fit.
        if (globalUpperBound > long.MaxValue)
        {
            throw new OverflowException("The computed answer exceeds Int64 range.");
        }

        return (long)globalUpperBound;
    }

    /*
    Optional helper that matches the "check candidate delay" idea from the prompt.
    This is not required by the direct O(n) solution above, but it is educational.

    Time Complexity: O(n)
    Space Complexity: O(1)

    It verifies whether a specific delay D works for every slot.
    */
    public bool CanUseAllSlots(long[] slotStart, long[] slotEnd, long renderTime, long delay)
    {
        for (int i = 0; i < slotStart.Length; i++)
        {
            BigInteger finishTime = (BigInteger)delay + (BigInteger)(i + 1) * renderTime;

            if (finishTime < slotStart[i] || finishTime > slotEnd[i])
            {
                return false;
            }
        }

        return true;
    }
}

// Demo code

var solution = new Solution();

// Example 1
long[] slotStart1 = { 5, 11, 17 };
long[] slotEnd1 = { 9, 15, 21 };
long renderTime1 = 3;
long result1 = solution.MaximumDelay(slotStart1, slotEnd1, renderTime1);
Console.WriteLine(result1); // Expected: 6

// Example 2
long[] slotStart2 = { 4, 8, 10 };
long[] slotEnd2 = { 5, 9, 11 };
long renderTime2 = 3;
long result2 = solution.MaximumDelay(slotStart2, slotEnd2, renderTime2);
Console.WriteLine(result2); // Expected: -1

// Additional quick sanity check
long[] slotStart3 = { 3, 6, 9 };
long[] slotEnd3 = { 100, 100, 100 };
long renderTime3 = 3;
long result3 = solution.MaximumDelay(slotStart3, slotEnd3, renderTime3);
Console.WriteLine(result3); // Expected: 91