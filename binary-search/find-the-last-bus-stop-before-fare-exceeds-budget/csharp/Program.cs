/*
Title: Find the Last Bus Stop Before Fare Exceeds Budget
Difficulty: Easy
Topic: Binary Search

Problem Description:
You are given a non-decreasing array fares where fares[i] is the total bus fare needed
to travel from the starting terminal to stop i. Because the route gets longer, the total
fare never decreases as the stop index increases. You are also given an integer budget
representing the maximum amount a passenger can spend.

Return the largest index i such that fares[i] <= budget. If even the first stop is too
expensive, return -1.

This problem is a classic binary search pattern:
find the rightmost index whose value satisfies a condition.

Important detail:
If there are duplicate fares and that fare is within the budget, we must return the
LAST index among those duplicates.

Example 1:
fares = [2, 4, 4, 7, 9], budget = 4
Output: 2

Reason:
- fares[1] = 4 is affordable
- fares[2] = 4 is also affordable
- fares[3] = 7 is too expensive
So the last reachable stop is index 2.

Example 2:
fares = [5, 8, 12], budget = 3
Output: -1

Reason:
- fares[0] = 5 is already greater than 3
So no stop is affordable.
*/

using System;

public class Solution
{
    // Time Complexity: O(log n)
    // Space Complexity: O(1)
    public int FindLastAffordableStop(int[] fares, int budget)
    {
        // We will use binary search because the fares array is sorted in non-decreasing order.
        // That sorted property is the key reason binary search works here efficiently.
        //
        // Goal:
        // Find the RIGHTMOST index i such that fares[i] <= budget.
        //
        // Why "rightmost"?
        // Because there may be duplicates, and if multiple stops have the same affordable fare,
        // the problem asks us to return the last such stop.
        //
        // Example:
        // fares = [2, 4, 4, 7, 9], budget = 4
        // Both index 1 and index 2 are valid, but we must return 2.

        int left = 0;
        int right = fares.Length - 1;

        // This variable stores the best valid answer found so far.
        // We initialize it to -1 because if no fare is affordable,
        // the required return value is -1.
        int answer = -1;

        // Continue searching while the current search range is valid.
        while (left <= right)
        {
            // Compute the middle index safely.
            // Using left + (right - left) / 2 avoids overflow in languages where
            // left + right could exceed integer limits. It is a standard best practice.
            int mid = left + (right - left) / 2;

            // Case 1:
            // If the fare at mid is within budget, then mid is a valid candidate.
            if (fares[mid] <= budget)
            {
                // Record mid as the current best answer.
                // Why?
                // Because fares[mid] satisfies the condition fares[mid] <= budget.
                answer = mid;

                // But we are not done yet.
                // We need the LARGEST such index, not just any valid index.
                //
                // Since the array is sorted, there may be more affordable stops to the RIGHT.
                // So we move left boundary to mid + 1 and continue searching there.
                left = mid + 1;
            }
            else
            {
                // Case 2:
                // fares[mid] > budget, so this stop is too expensive.
                //
                // Because the array is sorted, every index to the RIGHT of mid
                // will also be >= fares[mid], and therefore also too expensive.
                //
                // So the answer, if it exists, must be somewhere to the LEFT.
                right = mid - 1;
            }
        }

        // When the loop ends, answer contains the rightmost affordable index found,
        // or -1 if no fare was affordable.
        return answer;
    }
}

// Demo code:
// Create sample inputs, call the solution, and print the results.

var solution = new Solution();

// Example 1:
// fares = [2, 4, 4, 7, 9], budget = 4
// Expected output: 2
int[] fares1 = { 2, 4, 4, 7, 9 };
int budget1 = 4;
int result1 = solution.FindLastAffordableStop(fares1, budget1);
Console.WriteLine(result1);

// Example 2:
// fares = [5, 8, 12], budget = 3
// Expected output: -1
int[] fares2 = { 5, 8, 12 };
int budget2 = 3;
int result2 = solution.FindLastAffordableStop(fares2, budget2);
Console.WriteLine(result2);

// Additional demo examples for learning:

// All stops affordable, so answer should be the last index.
int[] fares3 = { 1, 2, 3, 4, 5 };
int budget3 = 10;
int result3 = solution.FindLastAffordableStop(fares3, budget3);
Console.WriteLine(result3);

// Duplicate affordable values at the end of the valid range.
int[] fares4 = { 3, 3, 3, 6, 8 };
int budget4 = 3;
int result4 = solution.FindLastAffordableStop(fares4, budget4);
Console.WriteLine(result4);

// Exact match at the final element.
int[] fares5 = { 2, 5, 7, 11 };
int budget5 = 11;
int result5 = solution.FindLastAffordableStop(fares5, budget5);
Console.WriteLine(result5);