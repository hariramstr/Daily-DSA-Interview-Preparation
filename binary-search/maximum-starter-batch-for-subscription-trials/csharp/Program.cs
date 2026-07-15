/*
Title: Maximum Starter Batch for Subscription Trials
Difficulty: Medium
Topic: Binary Search

Problem Description:
A product team is preparing trial kits for a new subscription launch. There are n warehouses, and the i-th warehouse can provide kits[i] starter kits. Every customer trial batch must contain exactly the same number of kits, and a single batch can only be assembled using kits from one warehouse. However, one warehouse may be split into multiple batches as long as the total number of kits used from that warehouse does not exceed its inventory.

Given an integer array kits where kits[i] is the number of available starter kits in warehouse i, and an integer m representing the number of customer batches that must be created, return the maximum possible number of kits in each batch.

If it is impossible to create m non-empty batches, return 0.

This problem should be solved efficiently for large inputs. A brute-force search over all possible batch sizes will be too slow. Think about how the answer changes as the candidate batch size increases, and use that monotonic behavior to design a binary search solution.

Constraints:
- 1 <= kits.length <= 100000
- 1 <= kits[i] <= 1000000000
- 1 <= m <= 1000000000000
- The answer fits in a 32-bit signed integer

Example 1:
Input: kits = [9, 7, 5], m = 5
Output: 3
Explanation: Using batch size 3, the warehouses can produce 3 + 2 + 1 = 6 batches, which is enough.
Batch size 4 would produce only 2 + 1 + 1 = 4 batches, which is not enough.
So the maximum valid size is 3.

Example 2:
Input: kits = [2, 4, 6], m = 7
Output: 1
Explanation: With batch size 1, we can create 12 batches in total.
With batch size 2, we can create only 1 + 2 + 3 = 6 batches, which is fewer than 7.
Therefore, the largest possible batch size is 1.
*/

using System;

public class Solution
{
    /*
    Time Complexity:
    - O(n * log M)
    - n = number of warehouses
    - M = maximum possible batch size we binary search over
    Explanation:
    For each binary search step, we scan the array once to count how many batches can be formed.
    The binary search range is from 1 to max(kits), so the number of steps is log M.

    Space Complexity:
    - O(1)
    Explanation:
    We only use a few extra variables and do not allocate extra data structures proportional to input size.
    */
    public int MaximumBatchSize(int[] kits, long m)
    {
        // Step 1:
        // Before doing any binary search, we should check whether creating m non-empty batches
        // is even possible in the best-case scenario.
        //
        // Why this matters:
        // - The smallest valid non-empty batch size is 1.
        // - If even batch size 1 cannot produce m batches, then no larger batch size can work.
        //
        // To check this, we sum all kits across all warehouses.
        // Since m can be as large as 1e12, we must use long for totals to avoid overflow.
        long totalKits = 0;

        // We will also compute the maximum warehouse inventory.
        // Why?
        // - No batch size can be larger than the largest single warehouse inventory,
        //   because one batch must come entirely from one warehouse.
        int maxKitsInAnyWarehouse = 0;

        foreach (int count in kits)
        {
            totalKits += count;

            if (count > maxKitsInAnyWarehouse)
            {
                maxKitsInAnyWarehouse = count;
            }
        }

        // If total kits are fewer than the number of required batches,
        // then even making batches of size 1 is impossible.
        if (totalKits < m)
        {
            return 0;
        }

        // Step 2:
        // Set up binary search boundaries.
        //
        // left  = smallest candidate batch size we want to test
        // right = largest candidate batch size that could possibly work
        //
        // We start from 1 because batches must be non-empty.
        int left = 1;
        int right = maxKitsInAnyWarehouse;

        // This variable stores the best valid answer found so far.
        // We update it whenever we find a batch size that can produce at least m batches.
        int answer = 0;

        // Step 3:
        // Standard binary search on the answer space.
        //
        // Key monotonic property:
        // - If a batch size x is feasible, then every smaller batch size is also feasible.
        // - If a batch size x is not feasible, then every larger batch size is also not feasible.
        //
        // This "true...true...false...false" pattern is exactly what binary search needs.
        while (left <= right)
        {
            // Compute the middle candidate carefully.
            // Using left + (right - left) / 2 avoids overflow in general.
            int mid = left + (right - left) / 2;

            // Step 4:
            // Check how many batches of size "mid" we can form in total.
            //
            // For one warehouse with "count" kits:
            // - It can contribute count / mid full batches.
            // - We use integer division because partial batches are not allowed.
            //
            // Example:
            // - If count = 9 and mid = 3, then 9 / 3 = 3 batches.
            // - If count = 7 and mid = 3, then 7 / 3 = 2 batches.
            long batchesFormed = 0;

            foreach (int count in kits)
            {
                batchesFormed += count / mid;

                // Small optimization:
                // As soon as we already know we can form at least m batches,
                // we can stop counting early.
                //
                // Why this is safe:
                // - For feasibility, we only care whether batchesFormed >= m.
                // - Any extra counting beyond m does not change the decision.
                if (batchesFormed >= m)
                {
                    break;
                }
            }

            // Step 5:
            // Decide how to move the binary search.
            if (batchesFormed >= m)
            {
                // Current batch size "mid" is feasible.
                //
                // That means:
                // - "mid" is a valid answer candidate.
                // - But maybe we can do even better with a larger batch size.
                //
                // So:
                // - Save mid as the best answer so far.
                // - Search the right half for a larger feasible value.
                answer = mid;
                left = mid + 1;
            }
            else
            {
                // Current batch size "mid" is not feasible.
                //
                // That means:
                // - mid is too large
                // - Any size larger than mid will also be too large
                //
                // So we search the left half.
                right = mid - 1;
            }
        }

        // Step 6:
        // After binary search finishes, "answer" holds the largest feasible batch size.
        return answer;
    }
}

// Demo code

var solution = new Solution();

// Example 1:
// kits = [9, 7, 5], m = 5
// Batch size 3 => 9/3 + 7/3 + 5/3 = 3 + 2 + 1 = 6 >= 5, valid
// Batch size 4 => 9/4 + 7/4 + 5/4 = 2 + 1 + 1 = 4 < 5, invalid
// Expected answer: 3
int[] kits1 = { 9, 7, 5 };
long m1 = 5;
int result1 = solution.MaximumBatchSize(kits1, m1);
Console.WriteLine(result1);

// Example 2:
// kits = [2, 4, 6], m = 7
// Batch size 1 => 2 + 4 + 6 = 12 >= 7, valid
// Batch size 2 => 1 + 2 + 3 = 6 < 7, invalid
// Expected answer: 1
int[] kits2 = { 2, 4, 6 };
long m2 = 7;
int result2 = solution.MaximumBatchSize(kits2, m2);
Console.WriteLine(result2);

// Additional demo:
// Impossible case:
// total kits = 1 + 1 = 2, but m = 3 batches required
// Even batch size 1 cannot make 3 batches, so answer should be 0
int[] kits3 = { 1, 1 };
long m3 = 3;
int result3 = solution.MaximumBatchSize(kits3, m3);
Console.WriteLine(result3);