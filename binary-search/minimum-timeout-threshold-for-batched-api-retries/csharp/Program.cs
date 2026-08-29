/*
Title: Minimum Timeout Threshold for Batched API Retries
Difficulty: Medium
Topic: Binary Search

Problem Description:
A backend service needs to process a sequence of API calls in the given order. The i-th call takes requestTimes[i] milliseconds if executed normally. To avoid excessive retry overhead, the service groups consecutive calls into batches. For any batch, the total time of all calls inside that batch must not exceed a chosen timeout threshold T. If adding the next call would make the batch total exceed T, the current batch is closed and a new batch starts with that call.

You are given an array requestTimes and an integer maxBatches. Find the minimum integer timeout threshold T such that all API calls can be processed using at most maxBatches batches.

You must preserve the original order of calls, and every call must belong to exactly one batch. The threshold T must be at least as large as the longest single request time, otherwise that call could never fit into any batch.

Return the smallest possible T.

Constraints:
- 1 <= requestTimes.length <= 100000
- 1 <= requestTimes[i] <= 1000000000
- 1 <= maxBatches <= requestTimes.length
- The answer fits in a 64-bit signed integer.

Example 1:
Input: requestTimes = [7, 2, 5, 10, 8], maxBatches = 2
Output: 18
Explanation: With T = 18, the calls can be grouped as [7, 2, 5] and [10, 8]. Using T = 17 would require 3 batches: [7, 2, 5], [10], [8]. So 18 is the minimum feasible threshold.

Example 2:
Input: requestTimes = [4, 4, 4, 4], maxBatches = 3
Output: 8
Explanation: With T = 8, one valid grouping is [4, 4], [4], [4], which uses 3 batches. With T = 7, each batch can contain only one call, so 4 batches are required. Therefore the minimum threshold is 8.
*/

using System;

public class Solution
{
    /*
    Time Complexity:
    - O(n * log S)
      where n is the number of API calls and S is the search range of possible answers
      (from max(requestTimes) to sum(requestTimes)).
      For each binary search step, we scan the array once to check feasibility.

    Space Complexity:
    - O(1)
      We only use a few extra variables and do not allocate additional data structures
      proportional to the input size.
    */
    public long MinimumTimeoutThreshold(int[] requestTimes, int maxBatches)
    {
        // We need to search for the smallest threshold T that allows processing
        // all requests in at most maxBatches batches.
        //
        // This is a classic "binary search on answer" problem because:
        // - If some threshold T works, then any larger threshold also works.
        // - If some threshold T does not work, then any smaller threshold also does not work.
        //
        // That monotonic behavior is exactly what binary search needs.

        // "left" is the smallest possible valid threshold candidate.
        // It must be at least the largest single request time, because every request
        // must fit into some batch by itself if necessary.
        long left = 0;

        // "right" is the largest threshold we would ever need to consider.
        // If we choose the sum of all request times, then all requests can fit
        // into one single batch, so this is always a valid upper bound.
        long right = 0;

        // We compute both bounds in one pass:
        // - left becomes the maximum element
        // - right becomes the total sum
        foreach (int time in requestTimes)
        {
            if (time > left)
            {
                left = time;
            }

            right += time;
        }

        // Standard binary search over the answer space [left, right].
        // Our goal is to find the minimum feasible threshold.
        while (left < right)
        {
            // We use this overflow-safe midpoint formula.
            long mid = left + (right - left) / 2;

            // Check whether this candidate threshold "mid" is sufficient.
            if (CanProcessWithinBatches(requestTimes, maxBatches, mid))
            {
                // If "mid" works, then it is a valid answer candidate.
                // But we want the smallest valid threshold, so we continue
                // searching on the left half, including mid itself.
                right = mid;
            }
            else
            {
                // If "mid" does not work, then every smaller threshold also fails.
                // So we must search strictly to the right of mid.
                left = mid + 1;
            }
        }

        // When the loop ends, left == right, and that value is the smallest
        // threshold that passed the feasibility check.
        return left;
    }

    private bool CanProcessWithinBatches(int[] requestTimes, int maxBatches, long threshold)
    {
        // This helper method answers:
        // "If the timeout threshold is exactly 'threshold', can we split the requests
        //  into at most maxBatches consecutive batches?"
        //
        // We use a greedy strategy:
        // - Keep adding requests to the current batch while the sum stays <= threshold.
        // - As soon as adding the next request would exceed threshold, we close the current batch
        //   and start a new batch with that request.
        //
        // Why is greedy correct here?
        // Because for a fixed threshold, packing each batch as much as possible
        // minimizes the number of batches needed. Starting a new batch earlier
        // would never help reduce the total number of batches.

        // We start with one batch because if there is at least one request,
        // we need at least one batch to hold it.
        int batchesUsed = 1;

        // This stores the running total time of the current batch.
        long currentBatchSum = 0;

        // Process requests in the original order, because the problem requires
        // that order must be preserved.
        foreach (int time in requestTimes)
        {
            // If adding this request to the current batch stays within the threshold,
            // we simply include it in the current batch.
            if (currentBatchSum + time <= threshold)
            {
                currentBatchSum += time;
            }
            else
            {
                // Otherwise, we must close the current batch and start a new one.
                batchesUsed++;
                currentBatchSum = time;

                // Important optimization:
                // If we already exceeded the allowed number of batches,
                // there is no need to continue scanning the rest of the array.
                if (batchesUsed > maxBatches)
                {
                    return false;
                }
            }
        }

        // If we finished processing all requests and used no more than maxBatches,
        // then this threshold is feasible.
        return true;
    }
}

// Demo code

var solution = new Solution();

// Example 1
int[] requestTimes1 = { 7, 2, 5, 10, 8 };
int maxBatches1 = 2;
long result1 = solution.MinimumTimeoutThreshold(requestTimes1, maxBatches1);
Console.WriteLine(result1); // Expected: 18

// Example 2
int[] requestTimes2 = { 4, 4, 4, 4 };
int maxBatches2 = 3;
long result2 = solution.MinimumTimeoutThreshold(requestTimes2, maxBatches2);
Console.WriteLine(result2); // Expected: 8

// Additional demo
int[] requestTimes3 = { 1, 2, 3, 4, 5 };
int maxBatches3 = 2;
long result3 = solution.MinimumTimeoutThreshold(requestTimes3, maxBatches3);
Console.WriteLine(result3); // Expected: 9