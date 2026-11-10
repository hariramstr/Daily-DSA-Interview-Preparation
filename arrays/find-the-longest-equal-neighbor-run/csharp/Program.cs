/*
Title: Find the Longest Equal Neighbor Run
Difficulty: Easy
Topic: Arrays

Problem Description:
You are given an integer array nums representing a sequence of sensor states recorded over time.
A stable run is a contiguous part of the array where every pair of neighboring elements is equal.
In other words, for every valid index i inside that run except the last one, nums[i] == nums[i + 1].

Your task is to return the length of the longest stable run in the array.

A run may contain just one element, so if all values are different, the answer is 1.
If the array is empty, return 0.

This problem is useful in systems that need to measure how long a device stayed in the same state
without changing. You only need to scan the array once and track the current run length and the
best run length seen so far.

Constraints:
- 0 <= nums.length <= 100000
- -1000000000 <= nums[i] <= 1000000000

Example 1:
Input: nums = [4,4,4,2,2,7,7,7,7,1]
Output: 4
Explanation: The longest contiguous block of equal values is [7,7,7,7], which has length 4.

Example 2:
Input: nums = [5,3,8,1]
Output: 1
Explanation: No two neighboring elements are equal, so every stable run has length 1.

Return the maximum length among all contiguous runs of equal values.
*/

using System;

public class Solution
{
    /*
    Time Complexity: O(n)
    - We scan through the array exactly once.
    - Each element is processed a constant number of times.

    Space Complexity: O(1)
    - We only use a few integer variables.
    - No extra data structures are needed.

    Beginner-friendly idea:
    We walk through the array from left to right and count how long the current run
    of equal neighboring values is. Whenever the current value matches the previous value,
    we extend the current run. Otherwise, the run breaks and we start a new run of length 1.
    At every step, we keep track of the best (maximum) run length seen so far.
    */
    public int LongestEqualNeighborRun(int[] nums)
    {
        // Step 1:
        // Handle the special case where the array is empty.
        //
        // Why this is necessary:
        // The problem explicitly says that if the array has no elements,
        // the answer should be 0.
        //
        // Without this check, we would not have any valid starting element
        // for a run, and later logic that assumes at least one element exists
        // would be incorrect.
        if (nums == null || nums.Length == 0)
        {
            return 0;
        }

        // Step 2:
        // Initialize the length of the best run found so far.
        //
        // Why start at 1?
        // If the array is not empty, then at minimum, any single element by itself
        // forms a valid run of length 1.
        //
        // Example:
        // nums = [5, 3, 8, 1]
        // No equal neighbors exist, but each individual element is still a run of length 1.
        int bestRunLength = 1;

        // Step 3:
        // Initialize the length of the current run we are actively tracking.
        //
        // At the beginning, before scanning further elements, the first element
        // starts a run of length 1 by itself.
        int currentRunLength = 1;

        // Step 4:
        // Scan the array from the second element to the end.
        //
        // Why start from index 1?
        // Because for each position i, we want to compare nums[i] with nums[i - 1].
        // The first element at index 0 has no previous neighbor, so comparisons
        // naturally begin at index 1.
        for (int i = 1; i < nums.Length; i++)
        {
            // Step 4a:
            // Check whether the current element is equal to the previous element.
            //
            // This is the key condition in the problem:
            // a stable run continues only when neighboring elements are equal.
            if (nums[i] == nums[i - 1])
            {
                // Step 4b:
                // The run continues, so we extend the current run by 1.
                //
                // Why this works:
                // If nums[i] equals nums[i - 1], then the contiguous block of equal values
                // has grown by one more element.
                //
                // Example:
                // nums = [7, 7, 7, 7]
                // At each equal comparison, currentRunLength becomes 2, then 3, then 4.
                currentRunLength++;
            }
            else
            {
                // Step 4c:
                // The run is broken because the current element differs from the previous one.
                //
                // Why reset to 1?
                // A new run starts at the current element itself.
                // Even though it does not match the previous element, the current element alone
                // is still a valid run of length 1.
                //
                // Example:
                // nums = [4, 4, 4, 2]
                // When we move from 4 to 2, the previous run ends,
                // and the value 2 starts a new run of length 1.
                currentRunLength = 1;
            }

            // Step 4d:
            // Update the best run length if the current run is now larger.
            //
            // Why do this on every iteration?
            // Because the longest run could end at any position in the array.
            // By updating continuously, we never miss the maximum.
            //
            // Data structure choice:
            // We do not need an array, list, or any other collection to store all run lengths.
            // We only need:
            // - currentRunLength: the run we are currently building
            // - bestRunLength: the maximum run seen so far
            //
            // This keeps the solution simple and memory-efficient.
            if (currentRunLength > bestRunLength)
            {
                bestRunLength = currentRunLength;
            }
        }

        // Step 5:
        // Return the maximum stable run length found during the scan.
        //
        // Correctness check with Example 1:
        // nums = [4,4,4,2,2,7,7,7,7,1]
        //
        // Start:
        // best = 1, current = 1
        //
        // i=1: 4 == 4 -> current=2, best=2
        // i=2: 4 == 4 -> current=3, best=3
        // i=3: 2 != 4 -> current=1, best=3
        // i=4: 2 == 2 -> current=2, best=3
        // i=5: 7 != 2 -> current=1, best=3
        // i=6: 7 == 7 -> current=2, best=3
        // i=7: 7 == 7 -> current=3, best=3
        // i=8: 7 == 7 -> current=4, best=4
        // i=9: 1 != 7 -> current=1, best=4
        //
        // Final answer = 4, which matches the expected output.
        //
        // Correctness check with Example 2:
        // nums = [5,3,8,1]
        //
        // Start:
        // best = 1, current = 1
        //
        // i=1: 3 != 5 -> current=1, best=1
        // i=2: 8 != 3 -> current=1, best=1
        // i=3: 1 != 8 -> current=1, best=1
        //
        // Final answer = 1, which matches the expected output.
        return bestRunLength;
    }
}

// Demo code:
// Create a solution instance, prepare sample inputs, call the method,
// and print the results so the program is fully runnable.

var solution = new Solution();

int[] nums1 = { 4, 4, 4, 2, 2, 7, 7, 7, 7, 1 };
int result1 = solution.LongestEqualNeighborRun(nums1);
Console.WriteLine($"Example 1 result: {result1}"); // Expected: 4

int[] nums2 = { 5, 3, 8, 1 };
int result2 = solution.LongestEqualNeighborRun(nums2);
Console.WriteLine($"Example 2 result: {result2}"); // Expected: 1

int[] nums3 = Array.Empty<int>();
int result3 = solution.LongestEqualNeighborRun(nums3);
Console.WriteLine($"Empty array result: {result3}"); // Expected: 0

int[] nums4 = { 9, 9, 9, 9, 9 };
int result4 = solution.LongestEqualNeighborRun(nums4);
Console.WriteLine($"All equal result: {result4}"); // Expected: 5

int[] nums5 = { 1, 2, 2, 3, 3, 3, 2, 2 };
int result5 = solution.LongestEqualNeighborRun(nums5);
Console.WriteLine($"Mixed runs result: {result5}"); // Expected: 3