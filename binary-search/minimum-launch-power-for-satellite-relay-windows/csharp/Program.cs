/*
Title: Minimum Launch Power for Satellite Relay Windows

Problem Description:
A space operations team needs to transmit data to a sequence of orbital relay windows.
The i-th relay window opens at time windows[i], and sending to that window requires
power[i] units of launch energy.

A single transmitter can only handle windows in chronological order. If the transmitter
is configured with a launch power limit X, then it may send to any relay window whose
required power is at most X. However, skipped windows are lost forever, and the team
must still successfully transmit to at least k relay windows in order.

Your task is to find the minimum integer launch power limit X such that it is possible
to transmit to at least k relay windows.

The arrays windows and power are both length n. The windows array is strictly increasing,
but the actual times only determine the order of processing; you may not reorder windows.
Since a higher power limit always allows transmitting to every window that was possible
under a lower limit, the answer is monotonic and can be found efficiently.

Return the minimum integer X that allows at least k successful transmissions.

Constraints:
- 1 <= n <= 200000
- 1 <= k <= n
- 1 <= windows[i] <= 10^9
- windows is strictly increasing
- 1 <= power[i] <= 10^9

Example 1:
Input: windows = [2, 5, 9, 12], power = [7, 3, 6, 4], k = 3
Output: 6

Explanation:
With X = 5, the team can transmit only to windows with required power 3 and 4,
so only 2 transmissions are possible.
With X = 6, the team can transmit to windows requiring 3, 6, and 4, reaching 3 transmissions.
Therefore the minimum valid power limit is 6.

Example 2:
Input: windows = [1, 4, 8, 10, 15], power = [9, 2, 5, 8, 1], k = 4
Output: 8

Explanation:
With X = 7, only windows with required power 2, 5, and 1 can be used, for a total of 3 transmissions.
With X = 8, windows requiring 2, 5, 8, and 1 become available, so 4 transmissions are possible.
Thus the answer is 8.
*/

using System;

public class Solution
{
    /*
    Time Complexity:
    - O(n log M), where:
      n = number of relay windows
      M = range of possible power values (from min power to max power, effectively up to 1e9)
    - For each binary search step, we scan the power array once to count how many windows
      are usable under the current launch power limit.

    Space Complexity:
    - O(1) extra space
    - We only use a few integer variables and do not allocate any extra data structures
      proportional to input size.
    */
    public int MinimumLaunchPower(int[] windows, int[] power, int k)
    {
        // Even though the "windows" array is part of the input, the problem statement tells us
        // that it is already strictly increasing and only matters for preserving order.
        // Since we are not allowed to reorder anything, and since every window is processed
        // in the given order anyway, we do not actually need to use the numeric values inside
        // "windows" for the algorithm.
        //
        // The real decision is:
        // "For a given power limit X, how many entries in power[] are <= X?"
        //
        // Why is that enough?
        // Because the transmitter can process windows in chronological order and may skip any
        // window it cannot handle. Therefore, every window with required power <= X is usable,
        // and every window with required power > X is unusable. The maximum number of successful
        // transmissions is simply the count of usable windows.

        int n = power.Length;

        // We need a search range for binary search.
        // The minimum possible valid answer cannot be smaller than the smallest required power
        // if k >= 1, but using 1 as the lower bound is also safe because constraints say power[i] >= 1.
        //
        // The maximum possible valid answer definitely does not need to exceed the largest required
        // power in the array, because once X reaches that value, every window becomes usable.
        int left = 1;
        int right = 1;

        // Find the maximum power requirement to establish the upper bound of binary search.
        // This is necessary so that our search space definitely contains the answer.
        for (int i = 0; i < n; i++)
        {
            if (power[i] > right)
            {
                right = power[i];
            }
        }

        // This variable will store the best (smallest) valid power limit found so far.
        // We initialize it to the upper bound because that value is always sufficient:
        // if X equals the maximum required power, then all windows are usable, so at least k
        // transmissions are certainly possible since k <= n.
        int answer = right;

        // Standard binary search on the answer space.
        //
        // Why binary search works:
        // - If a power limit X is sufficient to allow at least k transmissions,
        //   then any larger power limit is also sufficient.
        // - If a power limit X is not sufficient,
        //   then any smaller power limit is also not sufficient.
        //
        // This "false, false, false, ..., true, true, true" pattern is exactly what binary
        // search on the answer is designed for.
        while (left <= right)
        {
            // Compute the middle value carefully.
            // This avoids overflow compared to (left + right) / 2,
            // although with current constraints int would still be safe.
            int mid = left + (right - left) / 2;

            // Check whether this candidate launch power limit is enough.
            if (CanTransmitAtLeastK(power, k, mid))
            {
                // If mid is sufficient, it is a valid answer.
                // But we are looking for the MINIMUM valid answer,
                // so we record it and continue searching on the left half
                // to see whether an even smaller power limit also works.
                answer = mid;
                right = mid - 1;
            }
            else
            {
                // If mid is not sufficient, then every value smaller than mid
                // is also not sufficient due to monotonicity.
                // So we must search in the right half for a larger power limit.
                left = mid + 1;
            }
        }

        return answer;
    }

    private bool CanTransmitAtLeastK(int[] power, int k, int limit)
    {
        // This helper method answers the question:
        // "If the transmitter is configured with launch power limit = limit,
        //  can we successfully transmit to at least k relay windows?"
        //
        // Since windows must be handled in order and skipping is allowed,
        // every window with required power <= limit can be used.
        // So we simply count how many such windows exist.

        int successfulTransmissions = 0;

        // Scan through the windows in their given chronological order.
        // We do not need any advanced data structure here because:
        // 1. The order is fixed.
        // 2. For a fixed limit, each window is independently either usable or not usable.
        // 3. We only need the count, not the actual chosen subsequence.
        for (int i = 0; i < power.Length; i++)
        {
            // Current step:
            // Check whether the current relay window can be handled under this power limit.
            //
            // Why this step is necessary:
            // We must determine whether this window contributes to the total number of
            // successful transmissions possible with the current candidate limit.
            if (power[i] <= limit)
            {
                successfulTransmissions++;

                // Small optimization:
                // As soon as we reach k successful transmissions, we can stop early.
                //
                // Why this is correct:
                // The question is only whether at least k transmissions are possible.
                // Once we already know the answer is "yes", scanning the rest of the array
                // would not change that result.
                if (successfulTransmissions >= k)
                {
                    return true;
                }
            }
        }

        // If we finished scanning all windows and still did not reach k,
        // then this power limit is not sufficient.
        return false;
    }
}

// Demo code

var solution = new Solution();

// Example 1
int[] windows1 = { 2, 5, 9, 12 };
int[] power1 = { 7, 3, 6, 4 };
int k1 = 3;
int result1 = solution.MinimumLaunchPower(windows1, power1, k1);
Console.WriteLine(result1); // Expected: 6

// Example 2
int[] windows2 = { 1, 4, 8, 10, 15 };
int[] power2 = { 9, 2, 5, 8, 1 };
int k2 = 4;
int result2 = solution.MinimumLaunchPower(windows2, power2, k2);
Console.WriteLine(result2); // Expected: 8