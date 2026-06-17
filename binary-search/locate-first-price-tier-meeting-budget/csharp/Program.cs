/*
Title: Locate First Price Tier Meeting Budget
Difficulty: Easy
Topic: Binary Search

Problem Description:
You are given a sorted array `tiers` where `tiers[i]` represents the minimum order amount required to unlock the `i`-th discount tier in an online store. The array is sorted in non-decreasing order, and duplicate values may appear because multiple tier labels can start at the same minimum amount. You are also given an integer `budget`.

Your task is to return the smallest index `i` such that `tiers[i] >= budget`. In other words, find the first discount tier whose required minimum order amount is at least the shopper's budget target. If no such index exists, return `-1`.

This problem should be solved efficiently. A linear scan works, but the intended solution uses binary search because the array is already sorted. Be careful with edge cases such as an empty array, all values being smaller than `budget`, or the answer appearing multiple times due to duplicates.

Constraints:
- `0 <= tiers.length <= 100000`
- `0 <= tiers[i] <= 1000000000`
- `tiers` is sorted in non-decreasing order
- `0 <= budget <= 1000000000`

Example 1:
Input: tiers = [20, 35, 50, 50, 80], budget = 50
Output: 2
Explanation: The first index with value at least 50 is index 2.

Example 2:
Input: tiers = [10, 15, 15, 30], budget = 16
Output: 3
Explanation: Values at indices 0, 1, and 2 are smaller than 16. The first valid tier is 30 at index 3.
*/

using System;

public class Solution
{
    /*
    Time Complexity: O(log n)
    Space Complexity: O(1)

    We use binary search because the array is already sorted in non-decreasing order.
    That sorted property lets us eliminate half of the remaining search space on each step,
    which is much faster than checking every element one by one.
    */
    public int FindFirstTierAtLeastBudget(int[] tiers, int budget)
    {
        // Step 1:
        // Handle the simplest edge case first: if the array is empty,
        // there is no index we can return.
        // This check is necessary because binary search needs valid boundaries,
        // and an empty array has no valid positions at all.
        if (tiers == null || tiers.Length == 0)
        {
            return -1;
        }

        // Step 2:
        // Set up the two pointers that define our current search range.
        //
        // left  = the first index we are still considering
        // right = the last index we are still considering
        //
        // At the beginning, the whole array is a candidate range.
        int left = 0;
        int right = tiers.Length - 1;

        // Step 3:
        // We will store the best answer found so far in this variable.
        //
        // Why do we need this?
        // Because when we find a value tiers[mid] >= budget, that index is a valid answer,
        // but it might not be the FIRST such index.
        // So we save it, then continue searching to the left to see if an earlier valid index exists.
        //
        // We start with -1 to mean "no valid index found yet".
        int answer = -1;

        // Step 4:
        // Continue searching while the current range is valid.
        //
        // The condition left <= right means there is still at least one element to inspect.
        while (left <= right)
        {
            // Step 4a:
            // Compute the middle index of the current search range.
            //
            // We use:
            // left + (right - left) / 2
            // instead of:
            // (left + right) / 2
            //
            // This is a common safe pattern that avoids integer overflow in some languages
            // when left and right are very large.
            int mid = left + (right - left) / 2;

            // Step 4b:
            // Compare the middle value with the target budget.
            //
            // There are two cases:
            //
            // Case 1: tiers[mid] >= budget
            // This means mid is a valid candidate answer.
            // But we are looking for the FIRST such index, so we should continue searching
            // on the LEFT side to see if there is an earlier valid index.
            if (tiers[mid] >= budget)
            {
                // Save the current index as the best valid answer seen so far.
                answer = mid;

                // Move the right boundary to just before mid.
                // This keeps searching only the left half.
                right = mid - 1;
            }
            else
            {
                // Case 2: tiers[mid] < budget
                //
                // This means mid cannot be the answer, and neither can any index to the left of mid,
                // because the array is sorted and all those values are <= tiers[mid].
                //
                // So we discard the left half including mid, and search only the right half.
                left = mid + 1;
            }
        }

        // Step 5:
        // When the loop ends, answer contains:
        // - the first index where tiers[index] >= budget, if one exists
        // - or -1 if no such index was found
        return answer;
    }
}

// Demo code:
// Create a solution object, run the examples from the problem statement,
// and print the results so the program is fully runnable.

var solution = new Solution();

// Example 1:
// tiers = [20, 35, 50, 50, 80], budget = 50
// Expected output: 2
int[] tiers1 = { 20, 35, 50, 50, 80 };
int budget1 = 50;
int result1 = solution.FindFirstTierAtLeastBudget(tiers1, budget1);
Console.WriteLine(result1);

// Example 2:
// tiers = [10, 15, 15, 30], budget = 16
// Expected output: 3
int[] tiers2 = { 10, 15, 15, 30 };
int budget2 = 16;
int result2 = solution.FindFirstTierAtLeastBudget(tiers2, budget2);
Console.WriteLine(result2);

// Additional edge case demos:

// Empty array -> expected -1
int[] tiers3 = Array.Empty<int>();
int budget3 = 25;
int result3 = solution.FindFirstTierAtLeastBudget(tiers3, budget3);
Console.WriteLine(result3);

// All values smaller than budget -> expected -1
int[] tiers4 = { 5, 10, 15 };
int budget4 = 20;
int result4 = solution.FindFirstTierAtLeastBudget(tiers4, budget4);
Console.WriteLine(result4);

// Budget smaller than all values -> expected 0
int[] tiers5 = { 40, 50, 60 };
int budget5 = 10;
int result5 = solution.FindFirstTierAtLeastBudget(tiers5, budget5);
Console.WriteLine(result5);

// Duplicates where answer appears multiple times -> expected 1
int[] tiers6 = { 10, 20, 20, 20, 30 };
int budget6 = 20;
int result6 = solution.FindFirstTierAtLeastBudget(tiers6, budget6);
Console.WriteLine(result6);