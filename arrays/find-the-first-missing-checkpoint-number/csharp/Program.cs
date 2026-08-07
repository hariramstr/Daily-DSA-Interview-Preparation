/*
Title: Find the First Missing Checkpoint Number
Difficulty: Easy
Topic: Arrays

Problem Description:
A delivery company labels route checkpoints with positive integer IDs starting from 1. After syncing data from a driver's device, you receive an unsorted array checkpoints containing the IDs that were recorded during the trip. Some IDs may appear more than once because of duplicate scans, and some values may be invalid, such as 0 or negative numbers.

Your task is to return the smallest positive checkpoint ID that does not appear in the array. In other words, find the first missing positive integer in the recorded data.

This problem is useful for validating whether the earliest expected checkpoint was skipped or never uploaded. Only positive IDs matter. Duplicates do not change the answer, and invalid values should be ignored.

You should design a solution that works efficiently for typical interview constraints.

Constraints:
- 1 <= checkpoints.length <= 10^5
- -10^5 <= checkpoints[i] <= 10^5
- The array may contain duplicates
- The array is not guaranteed to be sorted

Example 1:
Input: checkpoints = [3, 4, -1, 1]
Output: 2
Explanation: Positive IDs 1, 3, and 4 are present, but 2 is missing, so the answer is 2.

Example 2:
Input: checkpoints = [1, 2, 2, 5]
Output: 3
Explanation: IDs 1 and 2 are present. The smallest missing positive checkpoint ID is 3.
*/

using System;

public class Solution
{
    /*
    Time Complexity: O(n)
    Space Complexity: O(1) extra space

    Explanation of the complexity:
    - We rearrange numbers in-place so that, whenever possible, value x is moved to index x - 1.
    - Each number is moved at most a small constant number of times, so the total work stays linear.
    - We do not use any extra array, hash set, or dictionary, so extra space is constant.
    */
    public int FirstMissingPositive(int[] checkpoints)
    {
        int n = checkpoints.Length;

        // STEP 1:
        // We want to place each valid positive number x into its "correct" position:
        // index x - 1.
        //
        // Why does this help?
        // Because if the array contains:
        // - 1, it should end up at index 0
        // - 2, it should end up at index 1
        // - 3, it should end up at index 2
        // and so on.
        //
        // After this rearrangement:
        // - if index 0 does not contain 1, then 1 is missing
        // - else if index 1 does not contain 2, then 2 is missing
        // - else if index 2 does not contain 3, then 3 is missing
        //
        // This is a classic in-place indexing trick that avoids extra memory.
        //
        // Important detail:
        // We only care about values in the range [1, n].
        // Why?
        // - Any value <= 0 is invalid for this problem.
        // - Any value > n cannot affect the answer among 1..n.
        //   If all numbers 1..n are present, then the answer is n + 1.
        for (int i = 0; i < n; i++)
        {
            // We use a while loop instead of a single if statement because after one swap,
            // the new value that arrives at checkpoints[i] might also need to be moved.
            //
            // Example:
            // checkpoints = [3, 4, -1, 1]
            // i = 0
            // checkpoints[0] = 3 -> should go to index 2
            // after swapping, a different value lands at index 0
            // that new value may also need to move immediately.
            while (
                checkpoints[i] >= 1 && checkpoints[i] <= n &&                  // The value is useful only if it is in [1, n].
                checkpoints[checkpoints[i] - 1] != checkpoints[i]              // Avoid infinite loops with duplicates.
            )
            {
                // The current value wants to go to its target index.
                // If current value is x, target index is x - 1.
                int correctIndex = checkpoints[i] - 1;

                // Swap checkpoints[i] with checkpoints[correctIndex].
                //
                // Why swap?
                // Because we are trying to place the current number into its correct slot
                // without using extra memory.
                int temp = checkpoints[i];
                checkpoints[i] = checkpoints[correctIndex];
                checkpoints[correctIndex] = temp;
            }
        }

        // STEP 2:
        // Now that we have tried to place every valid number into its correct position,
        // we scan from left to right.
        //
        // At index i, the correct value should be i + 1.
        // The first place where this is not true tells us the smallest missing positive.
        //
        // Why is the first mismatch the answer?
        // Because we scan in increasing order:
        // - if index 0 has 1, then 1 exists
        // - if index 1 has 2, then 2 exists
        // - the first missing expected value is therefore the smallest missing positive
        for (int i = 0; i < n; i++)
        {
            if (checkpoints[i] != i + 1)
            {
                return i + 1;
            }
        }

        // STEP 3:
        // If every position 0..n-1 contains exactly 1..n,
        // then all positive integers from 1 through n are present.
        //
        // Therefore, the smallest missing positive must be n + 1.
        return n + 1;
    }
}

// Demo code

var solution = new Solution();

// Example 1 from the problem statement:
// Input: [3, 4, -1, 1]
// Expected output: 2
//
// Quick trace:
// - Rearranged in-place to something like [1, -1, 3, 4]
// - Index 0 has 1 -> good
// - Index 1 should have 2, but has -1
// - So the first missing positive is 2
int[] checkpoints1 = { 3, 4, -1, 1 };
int result1 = solution.FirstMissingPositive((int[])checkpoints1.Clone());
Console.WriteLine($"Input: [{string.Join(", ", checkpoints1)}]");
Console.WriteLine($"First missing checkpoint ID: {result1}");
Console.WriteLine("Expected: 2");
Console.WriteLine();

// Example 2 from the problem statement:
// Input: [1, 2, 2, 5]
// Expected output: 3
//
// Quick trace:
// - 1 is already in the correct place
// - 2 is already in the correct place
// - duplicate 2 does not help place 3
// - 5 is outside the useful range for n = 4
// - Index 2 should contain 3, but it does not
// - So the answer is 3
int[] checkpoints2 = { 1, 2, 2, 5 };
int result2 = solution.FirstMissingPositive((int[])checkpoints2.Clone());
Console.WriteLine($"Input: [{string.Join(", ", checkpoints2)}]");
Console.WriteLine($"First missing checkpoint ID: {result2}");
Console.WriteLine("Expected: 3");
Console.WriteLine();

// Additional demo cases for learning:

int[] checkpoints3 = { 1, 2, 3 };
int result3 = solution.FirstMissingPositive((int[])checkpoints3.Clone());
Console.WriteLine($"Input: [{string.Join(", ", checkpoints3)}]");
Console.WriteLine($"First missing checkpoint ID: {result3}");
Console.WriteLine("Expected: 4");
Console.WriteLine();

int[] checkpoints4 = { 7, 8, 9, 11, 12 };
int result4 = solution.FirstMissingPositive((int[])checkpoints4.Clone());
Console.WriteLine($"Input: [{string.Join(", ", checkpoints4)}]");
Console.WriteLine($"First missing checkpoint ID: {result4}");
Console.WriteLine("Expected: 1");
Console.WriteLine();

int[] checkpoints5 = { 2, 1, 0 };
int result5 = solution.FirstMissingPositive((int[])checkpoints5.Clone());
Console.WriteLine($"Input: [{string.Join(", ", checkpoints5)}]");
Console.WriteLine($"First missing checkpoint ID: {result5}");
Console.WriteLine("Expected: 3");