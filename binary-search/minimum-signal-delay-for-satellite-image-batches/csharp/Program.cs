/*
Title: Minimum Signal Delay for Satellite Image Batches
Difficulty: Medium
Topic: Binary Search

Problem Description:
A ground station receives satellite image batches in a fixed order. The i-th batch has size batches[i] megabytes.
You must transmit all batches within h minutes, and the transmitter can be configured to send data at a constant
integer speed of s megabytes per minute.

During each minute, the transmitter works on only one batch. If the current batch has fewer than s megabytes remaining,
the unused portion of that minute is wasted and you may continue with the next batch only in the following minute.
In other words, sending a batch of size x at speed s takes ceil(x / s) whole minutes.

Your task is to return the minimum integer transmission speed s such that all batches can be sent within h minutes.

If it is impossible even when the speed is very large, return -1. This happens when h is smaller than the number of batches,
because every non-empty batch needs at least one full minute.

Constraints:
- 1 <= batches.length <= 100000
- 1 <= batches[i] <= 1000000000
- 1 <= h <= 1000000000
- The answer must fit in a 32-bit signed integer.

Examples:
1) batches = [30, 11, 23, 4, 20], h = 6
   Output: 23

2) batches = [8, 5, 8], h = 2
   Output: -1
*/

using System;

public class Solution
{
    /*
    Time Complexity:
    - O(n * log M)
    - n = number of batches
    - M = maximum batch size
    Why:
    - We binary search the answer (the speed), which takes log M steps.
    - For each candidate speed, we scan all batches once to compute total required minutes.

    Space Complexity:
    - O(1)
    Why:
    - We only use a few variables regardless of input size.
    */
    public int MinTransmissionSpeed(int[] batches, int h)
    {
        // Step 1:
        // First, handle the impossible case immediately.
        //
        // Why this is necessary:
        // Every batch is non-empty (size >= 1), and because the transmitter can work on only one batch per minute,
        // each batch needs at least 1 full minute no matter how large the speed is.
        //
        // So if we have more batches than available minutes, there is no possible speed that can finish on time.
        if (h < batches.Length)
        {
            return -1;
        }

        // Step 2:
        // Establish the binary search range for the answer.
        //
        // We are searching for the minimum integer speed s.
        //
        // Lowest possible speed:
        // - 1 MB/minute, because speed must be a positive integer.
        //
        // Highest necessary speed:
        // - max(batches), because at that speed, every batch finishes in exactly 1 minute.
        //   Any speed larger than the largest batch size does not reduce any batch below 1 minute,
        //   so searching above max(batches) is unnecessary.
        int left = 1;
        int right = 0;

        // Find the maximum batch size to define the upper bound of binary search.
        foreach (int batch in batches)
        {
            if (batch > right)
            {
                right = batch;
            }
        }

        // Step 3:
        // Perform binary search on the speed.
        //
        // Why binary search works:
        // The condition is monotonic:
        // - If a speed s is sufficient to finish within h minutes,
        //   then any larger speed is also sufficient.
        // - If a speed s is not sufficient,
        //   then any smaller speed is also not sufficient.
        //
        // This monotonic true/false pattern is exactly what binary search needs.
        while (left < right)
        {
            // Compute the middle speed safely.
            // Using this form avoids overflow compared to (left + right) / 2.
            int mid = left + (right - left) / 2;

            // Step 4:
            // Check whether this candidate speed "mid" is enough.
            //
            // We calculate the total minutes needed if every batch is sent at speed mid.
            long requiredMinutes = 0;

            foreach (int batch in batches)
            {
                // Each batch takes ceil(batch / mid) minutes.
                //
                // Instead of using floating-point math, we use integer arithmetic:
                // ceil(a / b) = (a + b - 1) / b
                //
                // Why this is better:
                // - It is exact for integers.
                // - It avoids floating-point precision issues.
                requiredMinutes += (batch + (long)mid - 1) / mid;

                // Small optimization:
                // If we already exceed h, we can stop early because this speed is definitely not enough.
                if (requiredMinutes > h)
                {
                    break;
                }
            }

            // Step 5:
            // Use the result of the feasibility check to shrink the search space.
            if (requiredMinutes <= h)
            {
                // Current speed is sufficient.
                //
                // But we want the MINIMUM sufficient speed,
                // so we keep searching on the left half, including mid itself.
                right = mid;
            }
            else
            {
                // Current speed is too slow.
                //
                // Therefore, all speeds <= mid are also too slow,
                // so we must search strictly to the right.
                left = mid + 1;
            }
        }

        // Step 6:
        // When left == right, binary search has found the smallest sufficient speed.
        return left;
    }
}

// Demo code:
// Create sample inputs, call the solution, and print results.

var solution = new Solution();

// Example 1:
// batches = [30, 11, 23, 4, 20], h = 6
// Expected output: 23
//
// Quick verification:
// speed 23 -> ceil(30/23)=2, ceil(11/23)=1, ceil(23/23)=1, ceil(4/23)=1, ceil(20/23)=1
// total = 2+1+1+1+1 = 6, so 23 works.
// Any smaller speed would require more than 6 minutes.
int[] batches1 = { 30, 11, 23, 4, 20 };
int h1 = 6;
int result1 = solution.MinTransmissionSpeed(batches1, h1);
Console.WriteLine(result1);

// Example 2:
// batches = [8, 5, 8], h = 2
// Expected output: -1
//
// Quick verification:
// There are 3 batches, and each needs at least 1 minute.
// Minimum possible total time is 3 minutes, which is already greater than h = 2.
int[] batches2 = { 8, 5, 8 };
int h2 = 2;
int result2 = solution.MinTransmissionSpeed(batches2, h2);
Console.WriteLine(result2);

// Additional demo:
int[] batches3 = { 3, 6, 7, 11 };
int h3 = 8;
int result3 = solution.MinTransmissionSpeed(batches3, h3);
Console.WriteLine(result3);