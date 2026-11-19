/*
Title: Find the First Broken Sequence Number

Problem Description:
You are given an array `nums` representing event sequence numbers recorded by a service during one batch.
The sequence numbers are supposed to start at `1` and increase by exactly `1` each time with no gaps.
However, due to a logging issue, one positive sequence number in that expected range is missing from the array.

Your task is to return the smallest missing positive sequence number.

The array may be unsorted, and values that are `0`, negative, or larger than the array length may appear
because of corrupted records. Those values should be ignored when determining the answer.

If the array length is `n`, then the answer must be in the range `[1, n + 1]`.

Examples:
1. nums = [3, 4, -1, 1]
   Output: 2
   Explanation:
   - 1 is present
   - 2 is missing
   - 3 is present
   - 4 is present
   So the smallest missing positive is 2.

2. nums = [1, 2, 0]
   Output: 3
   Explanation:
   - 1 is present
   - 2 is present
   - 3 is missing
   So the smallest missing positive is 3.
*/

using System;
using System.Collections.Generic;

public class Solution
{
    /*
    Time Complexity: O(n)
    Space Complexity: O(n)

    Explanation of complexity:
    - We scan the array once to record which positive values matter.
    - We scan from 1 to n once to find the first missing value.
    - HashSet operations are O(1) on average.
    - Therefore total time is linear in the size of the array.
    - We use extra storage in the HashSet, so space is also linear.
    */
    public int FirstMissingPositive(int[] nums)
    {
        // Step 1:
        // Create a HashSet<int> to store the positive numbers that are relevant to the answer.
        //
        // Why use a HashSet?
        // - We need to quickly check whether a number exists in the array.
        // - HashSet gives average O(1) lookup time.
        // - This is simpler and very beginner-friendly compared to in-place index tricks.
        //
        // Why do we not store every number blindly?
        // - The answer for an array of length n must be between 1 and n + 1.
        // - So values less than 1 do not help.
        // - Values greater than n also do not affect the first missing positive in [1, n + 1].
        var seen = new HashSet<int>();

        // Step 2:
        // Go through every number in the input array.
        //
        // Our goal in this loop:
        // - Keep only useful values.
        // - Ignore corrupted or irrelevant values.
        foreach (int num in nums)
        {
            // Step 2a:
            // Check whether the current number is in the useful range [1, nums.Length].
            //
            // Why this exact range?
            // - If n numbers are present, the first missing positive must be among 1..n+1.
            // - To determine whether 1..n are present, we only need to record values in that range.
            // - If all 1..n are present, then the answer is n+1.
            if (num >= 1 && num <= nums.Length)
            {
                // Step 2b:
                // Add the number to the set.
                //
                // If the number appears multiple times, HashSet automatically keeps only one copy.
                // That is perfect for this problem because we only care whether a number exists,
                // not how many times it appears.
                seen.Add(num);
            }
        }

        // Step 3:
        // Now check numbers in increasing order starting from 1.
        //
        // Why increasing order?
        // - The problem asks for the *smallest* missing positive integer.
        // - So we must test 1 first, then 2, then 3, and so on.
        for (int candidate = 1; candidate <= nums.Length; candidate++)
        {
            // Step 3a:
            // If the current candidate is NOT in the set,
            // then this is the first positive integer missing from the array.
            if (!seen.Contains(candidate))
            {
                return candidate;
            }
        }

        // Step 4:
        // If we reach this point, then every number from 1 to nums.Length is present.
        //
        // Example:
        // nums = [1, 2, 0]
        // n = 3
        // Useful values seen are {1, 2}
        // We check:
        // - 1 exists
        // - 2 exists
        // - 3 does not exist -> answer is 3
        //
        // Another example:
        // nums = [1, 2, 3]
        // n = 3
        // We check:
        // - 1 exists
        // - 2 exists
        // - 3 exists
        // Since all 1..3 exist, the answer must be 4.
        return nums.Length + 1;
    }
}

// Demo code

var solution = new Solution();

// Example 1 from the problem description
int[] nums1 = { 3, 4, -1, 1 };
int result1 = solution.FirstMissingPositive(nums1);
Console.WriteLine("Input: [3, 4, -1, 1]");
Console.WriteLine("Output: " + result1);
Console.WriteLine("Expected: 2");
Console.WriteLine();

// Example 2 from the problem description
int[] nums2 = { 1, 2, 0 };
int result2 = solution.FirstMissingPositive(nums2);
Console.WriteLine("Input: [1, 2, 0]");
Console.WriteLine("Output: " + result2);
Console.WriteLine("Expected: 3");
Console.WriteLine();

// Additional demo cases for learning
int[] nums3 = { 1, 2, 3 };
int result3 = solution.FirstMissingPositive(nums3);
Console.WriteLine("Input: [1, 2, 3]");
Console.WriteLine("Output: " + result3);
Console.WriteLine("Expected: 4");
Console.WriteLine();

int[] nums4 = { 7, 8, 9, 11, 12 };
int result4 = solution.FirstMissingPositive(nums4);
Console.WriteLine("Input: [7, 8, 9, 11, 12]");
Console.WriteLine("Output: " + result4);
Console.WriteLine("Expected: 1");
Console.WriteLine();

int[] nums5 = { 2, 2, 1, 4 };
int result5 = solution.FirstMissingPositive(nums5);
Console.WriteLine("Input: [2, 2, 1, 4]");
Console.WriteLine("Output: " + result5);
Console.WriteLine("Expected: 3");