/*
Title: Minimum Launch Window for Satellite Image Batches
Difficulty: Medium
Topic: Binary Search

Problem Description:
A space imaging company must upload satellite photos to a ground station. The photos must be transmitted in the given order, and each photo batch has a size stored in the array batches, where batches[i] is the number of megabytes in the i-th batch. The company has exactly d launch windows left before weather conditions become too unstable. In one launch window, the ground station can transmit any consecutive sequence of batches, as long as the total size sent in that window does not exceed the chosen window capacity.

Your task is to find the minimum integer launch window capacity needed so that all batches can be transmitted within at most d launch windows.

Every batch must be sent completely within a single window. Batches cannot be split across windows, and the order of batches cannot be changed.

Return the smallest possible capacity that makes the schedule feasible.

This problem is designed to be solved efficiently using binary search on the answer. A candidate capacity can be checked greedily by simulating how many launch windows are required if each window can carry at most that much data.

Constraints:
- 1 <= batches.length <= 100000
- 1 <= batches[i] <= 1000000000
- 1 <= d <= batches.length
- The answer fits in a 64-bit signed integer

Example 1:
Input: batches = [12, 7, 15, 6, 9], d = 3
Output: 21
Explanation: With capacity 21, one valid schedule is [12, 7], [15, 6], [9]. Capacity 20 is not enough because it would require 4 windows.

Example 2:
Input: batches = [5, 5, 5, 5, 5, 5], d = 2
Output: 15
Explanation: A capacity of 15 allows [5, 5, 5] and [5, 5, 5]. Any smaller capacity would need more than 2 windows.
*/

using System;

public class Solution
{
    /*
    Time Complexity:
    - O(n * log(S)), where:
      n = number of batches
      S = range of possible capacities, from max(batches) to sum(batches)
    - For each binary search guess, we scan the array once to check feasibility.

    Space Complexity:
    - O(1) extra space
    - We only use a few variables and do not allocate extra data structures proportional to input size.
    */
    public long MinimumLaunchWindowCapacity(int[] batches, int d)
    {
        // Step 1:
        // Establish the search boundaries for binary search.
        //
        // Why do we need boundaries?
        // Binary search works on a numeric range. We need a smallest possible answer
        // and a largest possible answer.
        //
        // Lower bound:
        // The capacity can never be smaller than the largest single batch,
        // because a batch cannot be split across windows.
        //
        // Upper bound:
        // The capacity can always be the sum of all batches,
        // which means we send everything in one window.
        long left = 0;
        long right = 0;

        foreach (int batch in batches)
        {
            // We update the lower bound to be the maximum batch size seen so far.
            // This guarantees every individual batch can fit.
            left = Math.Max(left, batch);

            // We add every batch to the upper bound.
            // This represents the capacity needed if we send all batches in one window.
            right += batch;
        }

        // Step 2:
        // Perform binary search on the answer.
        //
        // Key idea:
        // If a capacity X is enough to finish within d windows,
        // then any capacity larger than X is also enough.
        //
        // This "feasible / not feasible" pattern is exactly what binary search needs.
        while (left < right)
        {
            // Compute the middle capacity safely.
            // We use this form to avoid overflow in general:
            // mid = left + (right - left) / 2
            long mid = left + (right - left) / 2;

            // Step 3:
            // Check whether this candidate capacity is feasible.
            //
            // If feasible:
            //   We try to find an even smaller valid capacity, so move right to mid.
            //
            // If not feasible:
            //   We need a larger capacity, so move left to mid + 1.
            if (CanShipWithinDays(batches, d, mid))
            {
                right = mid;
            }
            else
            {
                left = mid + 1;
            }
        }

        // At the end of binary search, left == right,
        // and that value is the minimum feasible capacity.
        return left;
    }

    private bool CanShipWithinDays(int[] batches, int d, long capacity)
    {
        // Step 1:
        // Start with one launch window already in use.
        //
        // Why start at 1 instead of 0?
        // Because as soon as we begin placing batches, we are using the first window.
        int windowsUsed = 1;

        // This variable tracks the total size currently placed in the active window.
        long currentLoad = 0;

        // Step 2:
        // Process batches in the given order.
        //
        // Why in order?
        // The problem explicitly says the order cannot be changed.
        foreach (int batch in batches)
        {
            // Step 3:
            // Try to place the current batch into the current window.
            //
            // If adding this batch would exceed the allowed capacity,
            // then we must start a new window.
            if (currentLoad + batch > capacity)
            {
                // Open a new launch window.
                windowsUsed++;

                // Put the current batch into the new window.
                // This is safe because our binary search lower bound guarantees
                // capacity >= max(batch), so every single batch fits by itself.
                currentLoad = batch;

                // Small optimization:
                // If we already used more than d windows, this capacity is not feasible.
                if (windowsUsed > d)
                {
                    return false;
                }
            }
            else
            {
                // Otherwise, the batch fits in the current window,
                // so we simply add it to the running load.
                currentLoad += batch;
            }
        }

        // If we finished processing all batches using at most d windows,
        // then this capacity works.
        return true;
    }
}

// Demo code

var solution = new Solution();

// Example 1
int[] batches1 = { 12, 7, 15, 6, 9 };
int d1 = 3;
long result1 = solution.MinimumLaunchWindowCapacity(batches1, d1);
Console.WriteLine(result1); // Expected: 21

// Example 2
int[] batches2 = { 5, 5, 5, 5, 5, 5 };
int d2 = 2;
long result2 = solution.MinimumLaunchWindowCapacity(batches2, d2);
Console.WriteLine(result2); // Expected: 15

// Additional quick sanity check
int[] batches3 = { 10 };
int d3 = 1;
long result3 = solution.MinimumLaunchWindowCapacity(batches3, d3);
Console.WriteLine(result3); // Expected: 10