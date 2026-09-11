/*
Title: Minimum Router Delay for Sequential Packet Waves

Problem Description:
You are given a network router that must transmit packet waves in the given order. The i-th wave contains packets[i] packets, and the router can process at most d packets per second while a wave is active. Because of protocol overhead, each wave takes ceil(packets[i] / d) whole seconds to finish. The router cannot start the next wave until the current one is completed.

You are also given an integer maxTime, the maximum total number of seconds allowed to transmit all waves. Your task is to find the minimum integer router delay capacity d such that all waves can be transmitted within maxTime seconds.

If d is too small, some waves take too long. If d is larger, every wave finishes no slower than before. This monotonic behavior makes the problem suitable for binary search on the answer.

Return the minimum positive integer d that allows the total transmission time to be at most maxTime.

Constraints:
- 1 <= packets.length <= 100000
- 1 <= packets[i] <= 10^9
- packets.length <= maxTime <= 10^14
- The answer always exists.

Example 1:
Input: packets = [8, 4, 10], maxTime = 8
Output: 4
Explanation: With d = 4, the total time is ceil(8/4) + ceil(4/4) + ceil(10/4) = 2 + 1 + 3 = 6, which fits in 8.
With d = 3, the total time is ceil(8/3) + ceil(4/3) + ceil(10/3) = 3 + 2 + 4 = 9, which is too slow.

Example 2:
Input: packets = [30, 11, 23, 4, 20], maxTime = 10
Output: 11
Explanation: At d = 11, the total time is 3 + 1 + 3 + 1 + 2 = 10.
Any smaller delay capacity causes the total time to exceed maxTime.
*/

using System;

public class Solution
{
    /*
    Time Complexity:
    - O(n * log M)
    - n = number of waves in the packets array
    - M = maximum packet count in any single wave
    Why:
    - For each binary search guess of d, we scan the entire array once to compute total time.
    - Binary search performs about log2(M) guesses.

    Space Complexity:
    - O(1) extra space
    Why:
    - We only use a few variables and do not allocate extra data structures proportional to input size.
    */
    public int MinRouterDelay(int[] packets, long maxTime)
    {
        // Step 1:
        // We need to search for the minimum valid router capacity d.
        // Since d must be a positive integer, the smallest possible value is 1.
        int left = 1;

        // Step 2:
        // We need a safe upper bound for binary search.
        // If d equals the largest wave size, then every wave finishes in exactly 1 second,
        // because ceil(packets[i] / d) will be 1 for every packets[i] <= d.
        // Since the constraints guarantee packets.length <= maxTime, this upper bound is always valid.
        int right = 0;
        foreach (int wave in packets)
        {
            if (wave > right)
            {
                right = wave;
            }
        }

        // Step 3:
        // Binary search on the answer.
        // The key observation is monotonicity:
        // - If a certain d is fast enough, then any larger d is also fast enough.
        // - If a certain d is too slow, then any smaller d is also too slow.
        //
        // This "false, false, false, true, true, true" pattern is exactly what binary search needs.
        while (left < right)
        {
            // Step 3a:
            // Compute the middle candidate safely.
            // We use this form to avoid overflow, even though int would still be safe here.
            int mid = left + (right - left) / 2;

            // Step 3b:
            // Check whether this candidate capacity mid can finish all waves within maxTime.
            if (CanFinishWithinTime(packets, mid, maxTime))
            {
                // If mid works, it might be the answer,
                // but there could still be a smaller valid capacity.
                // So we keep searching on the left half, including mid itself.
                right = mid;
            }
            else
            {
                // If mid does NOT work, then mid is too small.
                // Therefore every value <= mid is also too small.
                // We must search strictly to the right.
                left = mid + 1;
            }
        }

        // Step 4:
        // When left == right, binary search has narrowed the range to the smallest valid d.
        return left;
    }

    private bool CanFinishWithinTime(int[] packets, int d, long maxTime)
    {
        // This variable stores the total number of whole seconds needed
        // to transmit all waves using router capacity d.
        long totalTime = 0;

        // We scan each wave one by one because the waves must be processed sequentially.
        // There is no overlap, so total time is simply the sum of each wave's time.
        foreach (int wave in packets)
        {
            // For one wave of size "wave", the time needed is ceil(wave / d).
            //
            // Instead of using floating-point math, we use a standard integer formula:
            // ceil(a / b) = (a + b - 1) / b
            //
            // This is important because:
            // 1. Integer arithmetic is exact here.
            // 2. It avoids precision issues from floating-point operations.
            // 3. It is faster and common in algorithm problems.
            totalTime += (wave + (long)d - 1) / d;

            // Early stopping optimization:
            // If totalTime already exceeds maxTime, there is no need to continue.
            // This makes the check faster in many cases, especially when d is too small.
            if (totalTime > maxTime)
            {
                return false;
            }
        }

        // If we finish the loop and totalTime never exceeded maxTime,
        // then this capacity d is sufficient.
        return totalTime <= maxTime;
    }
}

// Demo code:
// Creates sample inputs, calls the solution, and prints the results.

var solution = new Solution();

// Example 1
int[] packets1 = { 8, 4, 10 };
long maxTime1 = 8;
int result1 = solution.MinRouterDelay(packets1, maxTime1);
Console.WriteLine($"Example 1 Result: {result1}");

// Manual verification for Example 1:
// d = 4 => ceil(8/4) + ceil(4/4) + ceil(10/4) = 2 + 1 + 3 = 6 <= 8, valid
// d = 3 => ceil(8/3) + ceil(4/3) + ceil(10/3) = 3 + 2 + 4 = 9 > 8, invalid
// Therefore the minimum valid answer is 4.

// Example 2
int[] packets2 = { 30, 11, 23, 4, 20 };
long maxTime2 = 10;
int result2 = solution.MinRouterDelay(packets2, maxTime2);
Console.WriteLine($"Example 2 Result: {result2}");

// Manual verification for Example 2:
// d = 11 => ceil(30/11) + ceil(11/11) + ceil(23/11) + ceil(4/11) + ceil(20/11)
//        => 3 + 1 + 3 + 1 + 2 = 10 <= 10, valid
// d = 10 => 3 + 2 + 3 + 1 + 2 = 11 > 10, invalid
// Therefore the minimum valid answer is 11.