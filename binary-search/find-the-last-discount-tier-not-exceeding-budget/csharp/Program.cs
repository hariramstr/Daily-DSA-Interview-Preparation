/*
Title: Find the Last Discount Tier Not Exceeding Budget
Difficulty: Easy
Topic: Binary Search

Problem Description:
An online store offers bulk discounts based on purchase quantity. You are given a sorted integer array `tiers`, where `tiers[i]` represents a quantity threshold at which a new discount tier begins. The array is sorted in strictly increasing order. You are also given an integer `budgetQty`, representing the maximum number of items a customer is willing or able to buy.

Your task is to return the index of the last discount tier whose threshold is less than or equal to `budgetQty`. In other words, find the greatest index `i` such that `tiers[i] <= budgetQty`.

If every threshold in `tiers` is greater than `budgetQty`, return `-1` because the customer does not qualify for any tier. If `budgetQty` is greater than or equal to the largest threshold, return the last index.

This problem should be solved efficiently using binary search rather than scanning the entire array. A linear scan works for small inputs, but interviewers expect an `O(log n)` solution because the thresholds are already sorted.

Constraints:
- `1 <= tiers.length <= 100000`
- `1 <= tiers[i] <= 1000000000`
- `tiers` is strictly increasing
- `1 <= budgetQty <= 1000000000`

Example 1:
Input: tiers = [5, 10, 20, 50], budgetQty = 23
Output: 2
Explanation: The thresholds less than or equal to 23 are 5, 10, and 20. The last such threshold is 20 at index 2.

Example 2:
Input: tiers = [4, 8, 15], budgetQty = 2
Output: -1
Explanation: All thresholds are greater than 2, so no discount tier is available.
*/

using System;

public class Solution
{
    /*
    Time Complexity: O(log n)
    - Each step cuts the remaining search space roughly in half.
    - Because the array is sorted, binary search is the correct efficient choice.

    Space Complexity: O(1)
    - We only use a few integer variables.
    - No extra arrays, lists, or recursion are needed.
    */
    public int FindLastTierNotExceedingBudget(int[] tiers, int budgetQty)
    {
        // We will search within the full valid index range of the array.
        // "left" points to the beginning of the current search range.
        // "right" points to the end of the current search range.
        int left = 0;
        int right = tiers.Length - 1;

        // This variable stores the best answer found so far.
        // We start with -1 because if no tier threshold is <= budgetQty,
        // the correct answer must be -1.
        int answer = -1;

        // Continue searching while there is still a valid range to inspect.
        // When left becomes greater than right, it means the search is finished.
        while (left <= right)
        {
            // Compute the middle index safely.
            // We use this form instead of (left + right) / 2 to avoid integer overflow
            // in general binary search patterns, even though the constraints here are safe.
            int mid = left + (right - left) / 2;

            // Compare the middle threshold with the customer's budget quantity.
            if (tiers[mid] <= budgetQty)
            {
                // Current middle threshold is affordable / not exceeding the budget.
                // That means mid is a VALID candidate answer.
                answer = mid;

                // But we are not done yet:
                // the problem asks for the LAST index whose value is <= budgetQty.
                // So we must continue searching to the RIGHT side
                // to see whether there is an even larger valid index.
                left = mid + 1;
            }
            else
            {
                // Current middle threshold is too large.
                // Therefore mid cannot be the answer,
                // and anything to the RIGHT of mid is also too large
                // because the array is sorted in strictly increasing order.
                // So we discard the right half and continue on the LEFT side.
                right = mid - 1;
            }
        }

        // After the loop ends, "answer" contains:
        // - the last index where tiers[index] <= budgetQty, or
        // - -1 if no such index exists.
        return answer;
    }
}

// Demo code:
// We create sample inputs from the problem statement,
// call the solution method, and print the results.

var solution = new Solution();

// Example 1:
// tiers = [5, 10, 20, 50], budgetQty = 23
// Valid thresholds are 5, 10, 20.
// The last valid threshold is 20 at index 2.
int[] tiers1 = { 5, 10, 20, 50 };
int budgetQty1 = 23;
int result1 = solution.FindLastTierNotExceedingBudget(tiers1, budgetQty1);
Console.WriteLine(result1); // Expected: 2

// Example 2:
// tiers = [4, 8, 15], budgetQty = 2
// All thresholds are greater than 2.
// So the correct answer is -1.
int[] tiers2 = { 4, 8, 15 };
int budgetQty2 = 2;
int result2 = solution.FindLastTierNotExceedingBudget(tiers2, budgetQty2);
Console.WriteLine(result2); // Expected: -1

// Additional demo:
// If budgetQty is at least the largest threshold,
// the answer should be the last index.
int[] tiers3 = { 3, 6, 9, 12 };
int budgetQty3 = 20;
int result3 = solution.FindLastTierNotExceedingBudget(tiers3, budgetQty3);
Console.WriteLine(result3); // Expected: 3

// Additional demo:
// Exact match should return that exact index.
int[] tiers4 = { 7, 14, 21, 28 };
int budgetQty4 = 21;
int result4 = solution.FindLastTierNotExceedingBudget(tiers4, budgetQty4);
Console.WriteLine(result4); // Expected: 2