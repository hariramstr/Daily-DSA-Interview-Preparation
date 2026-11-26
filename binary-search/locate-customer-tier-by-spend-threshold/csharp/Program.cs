/*
Title: Locate Customer Tier by Spend Threshold
Difficulty: Easy
Topic: Binary Search

Problem Description:
A company stores its loyalty program tiers as a sorted array of minimum spend thresholds.
Each value represents the smallest total yearly spend needed to qualify for that tier.

Example:
If the thresholds are [0, 100, 300, 700]:
- A customer who spent 450 qualifies for the tier starting at 300
- A customer who spent 90 qualifies for the tier starting at 0

Given a sorted integer array thresholds in strictly increasing order and an integer spend,
return the index of the highest threshold that is less than or equal to spend.
If spend is smaller than the first threshold, return -1.

The solution must run in O(log n) time, so binary search is the expected approach.

Constraints:
- 1 <= thresholds.length <= 100000
- 0 <= thresholds[i] <= 1000000000
- thresholds is sorted in strictly increasing order
- 0 <= spend <= 1000000000

Example 1:
Input: thresholds = [0, 100, 300, 700], spend = 450
Output: 2
Explanation: thresholds[2] = 300 is the largest threshold not exceeding 450.
The next threshold, 700, is too high.

Example 2:
Input: thresholds = [50, 120, 200], spend = 20
Output: -1
Explanation: The customer has not reached the minimum spend for the first tier,
so no valid threshold index exists.
*/

using System;

public class Solution
{
    /*
    Time Complexity: O(log n)
    - Each step cuts the search range roughly in half, which is the key advantage of binary search.

    Space Complexity: O(1)
    - We only use a few integer variables and do not allocate extra data structures based on input size.
    */
    public int LocateCustomerTier(int[] thresholds, int spend)
    {
        // We will perform binary search on the sorted array.
        // Because the array is strictly increasing, all values to the left of a position
        // are smaller, and all values to the right are larger.
        //
        // Our goal is NOT to find an exact match only.
        // Instead, we want the index of the RIGHTMOST value that is <= spend.
        //
        // This is a very common binary search variation:
        // "Find the last valid position."

        // 'left' marks the beginning of the current search range.
        int left = 0;

        // 'right' marks the end of the current search range.
        int right = thresholds.Length - 1;

        // 'answer' stores the best valid index found so far.
        // We start with -1 because if no threshold is <= spend,
        // then the correct result must be -1.
        int answer = -1;

        // Continue searching while there is still a valid range to inspect.
        while (left <= right)
        {
            // Compute the middle index safely.
            // We use this form instead of (left + right) / 2 to avoid overflow in general.
            int mid = left + (right - left) / 2;

            // If thresholds[mid] is <= spend, then this index is a valid candidate.
            if (thresholds[mid] <= spend)
            {
                // Record this index as the current best answer.
                // Why?
                // Because it satisfies the requirement: threshold <= spend.
                answer = mid;

                // But we are not done yet.
                // We want the HIGHEST index with this property, not just any valid index.
                //
                // Since the array is sorted in increasing order, there might be another
                // threshold to the RIGHT that is also <= spend and would be a better answer.
                //
                // So we move 'left' to search the right half.
                left = mid + 1;
            }
            else
            {
                // If thresholds[mid] > spend, then this threshold is too large.
                //
                // Because the array is sorted, every value to the RIGHT of mid
                // will also be too large.
                //
                // Therefore, the answer cannot be at mid or to its right.
                // We must search the LEFT half.
                right = mid - 1;
            }
        }

        // When the loop ends, 'answer' contains the index of the rightmost threshold
        // that is <= spend, or -1 if no such threshold exists.
        return answer;
    }
}

// Demo code

var solution = new Solution();

// Example 1:
// thresholds = [0, 100, 300, 700], spend = 450
// Expected output: 2
int[] thresholds1 = { 0, 100, 300, 700 };
int spend1 = 450;
int result1 = solution.LocateCustomerTier(thresholds1, spend1);
Console.WriteLine(result1);

// Example 2:
// thresholds = [50, 120, 200], spend = 20
// Expected output: -1
int[] thresholds2 = { 50, 120, 200 };
int spend2 = 20;
int result2 = solution.LocateCustomerTier(thresholds2, spend2);
Console.WriteLine(result2);

// Additional beginner-friendly checks:

// Exact match case:
// spend = 300 should return index 2
int[] thresholds3 = { 0, 100, 300, 700 };
int spend3 = 300;
int result3 = solution.LocateCustomerTier(thresholds3, spend3);
Console.WriteLine(result3);

// Larger than every threshold:
// spend = 1000 should return index 3
int[] thresholds4 = { 0, 100, 300, 700 };
int spend4 = 1000;
int result4 = solution.LocateCustomerTier(thresholds4, spend4);
Console.WriteLine(result4);

// Between first and second threshold:
// spend = 90 should return index 0
int[] thresholds5 = { 0, 100, 300, 700 };
int spend5 = 90;
int result5 = solution.LocateCustomerTier(thresholds5, spend5);
Console.WriteLine(result5);