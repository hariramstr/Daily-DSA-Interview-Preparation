/*
Title: Maximum Net Gain from One Interior Coupon Burst

Problem Description:
You are given an integer array purchases where purchases[i] represents the net profit contributed by the i-th customer order during a day.
Values may be positive, zero, or negative.

The marketing team can run exactly one special coupon burst on a contiguous block of orders, but the burst is only allowed on an interior block:
it cannot include the first order or the last order of the day.

If the team chooses a block purchases[l..r] with 1 <= l <= r <= n - 2, the campaign score is defined as:
    sum(purchases[l..r]) - purchases[l - 1] - purchases[r + 1]

Your task is to return the maximum possible campaign score over all valid interior blocks.

Constraints:
- 3 <= purchases.length <= 200000
- -1000000000 <= purchases[i] <= 1000000000
- The answer fits in a signed 64-bit integer.

Examples:
1) purchases = [4, 2, 7, -3, 5]
   Best interior block is [7]
   Score = 7 - 2 - (-3) = 8

2) purchases = [3, -5, 6, 1, -2, 4]
   Best interior block is [6, 1]
   Score = (6 + 1) - (-5) - (-2) = 10
*/

using System;

public class Solution
{
    /*
    Time Complexity: O(n)
    Space Complexity: O(1)

    Idea:
    We want to maximize:
        sum(purchases[l..r]) - purchases[l - 1] - purchases[r + 1]

    Rearranging by "growing" the right end:
        For a fixed r, this becomes:
            (best value of sum(purchases[l..r]) - purchases[l - 1]) - purchases[r + 1]

    Define a running value:
        bestEndingAtCurrent = maximum value of
            sum(purchases[l..i]) - purchases[l - 1]
        over all valid l for the current i

    Transition:
        To build a block ending at i, we have two choices:
        1) Start a new block at i:
              purchases[i] - purchases[i - 1]
           because the block is [i..i], and its left outside penalty is purchases[i - 1]

        2) Extend the best block ending at i - 1:
              bestEndingAtPrevious + purchases[i]

    So:
        bestEndingAtCurrent = max(
            purchases[i] - purchases[i - 1],
            bestEndingAtPrevious + purchases[i]
        )

    Once we know the best block ending at i, we can finalize its full score by subtracting
    the right outside penalty purchases[i + 1]:
        answer = max(answer, bestEndingAtCurrent - purchases[i + 1])

    Valid i values:
    - The block must be interior, so l >= 1 and r <= n - 2
    - To subtract purchases[r + 1], we need r + 1 to exist, so r <= n - 2
    - Therefore, r can range from 1 to n - 2 inclusive
    */
    public long MaximumNetGain(int[] purchases)
    {
        int n = purchases.Length;

        // The problem guarantees n >= 3, which means at least one interior index exists.
        // Example:
        // n = 3 => only index 1 can be chosen as the block [1..1].
        //
        // We use long because:
        // - individual values can be as large as 1e9 in magnitude
        // - sums over many elements can exceed int range
        long answer = long.MinValue;

        // This variable will store:
        // "the best value of sum(purchases[l..i]) - purchases[l - 1]
        //  among all valid interior blocks that end exactly at index i"
        //
        // We initialize it when i = 1, because index 1 is the first possible interior start/end.
        //
        // For block [1..1]:
        // score before subtracting the right outside element is:
        // purchases[1] - purchases[0]
        long bestEndingHere = (long)purchases[1] - purchases[0];

        // Now complete the full score for the block ending at r = 1
        // by subtracting purchases[r + 1] = purchases[2].
        answer = bestEndingHere - purchases[2];

        // We now process every possible right endpoint r from 2 to n - 2.
        // For each r:
        //   1) compute the best block ending at r
        //   2) subtract the right outside penalty purchases[r + 1]
        //   3) update the global answer
        for (int i = 2; i <= n - 2; i++)
        {
            // Option 1: start a brand-new block at index i.
            //
            // If the block is just [i..i], then:
            // sum(block) = purchases[i]
            // left outside penalty = purchases[i - 1]
            //
            // So its "partial value" before subtracting the right outside element is:
            long startNew = (long)purchases[i] - purchases[i - 1];

            // Option 2: extend the best block that ended at i - 1.
            //
            // If we already had the best value for some block [l..i-1]:
            //     sum(purchases[l..i-1]) - purchases[l - 1]
            // then extending it to include purchases[i] gives:
            //     sum(purchases[l..i]) - purchases[l - 1]
            // which is simply:
            //     bestEndingHere + purchases[i]
            long extendPrevious = bestEndingHere + purchases[i];

            // Choose whichever gives the larger value.
            //
            // This is the Kadane-style dynamic programming step:
            // either begin a new segment here, or continue the previous best segment.
            bestEndingHere = Math.Max(startNew, extendPrevious);

            // Now that we have the best block ending exactly at i,
            // we can compute its full campaign score by subtracting the element
            // immediately to its right, purchases[i + 1].
            long fullScore = bestEndingHere - purchases[i + 1];

            // Update the global maximum answer.
            if (fullScore > answer)
            {
                answer = fullScore;
            }
        }

        return answer;
    }
}

// Demo code

var solution = new Solution();

int[] purchases1 = { 4, 2, 7, -3, 5 };
long result1 = solution.MaximumNetGain(purchases1);
Console.WriteLine(result1); // Expected: 8

int[] purchases2 = { 3, -5, 6, 1, -2, 4 };
long result2 = solution.MaximumNetGain(purchases2);
Console.WriteLine(result2); // Expected: 10

// Additional quick sanity check:
// n = 3, only possible block is the middle element.
int[] purchases3 = { 10, -2, 7 };
// Score = -2 - 10 - 7 = -19
long result3 = solution.MaximumNetGain(purchases3);
Console.WriteLine(result3); // Expected: -19