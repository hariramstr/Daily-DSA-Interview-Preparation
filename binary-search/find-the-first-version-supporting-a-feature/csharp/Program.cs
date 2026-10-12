/*
Title: Find the First Version Supporting a Feature
Difficulty: Easy
Topic: Binary Search

Problem Description:
A software platform releases product versions in increasing order, and each version either supports a new feature or does not.
You are given a sorted binary array support, where support[i] is either 0 or 1.

- 0 means version i does not support the feature yet
- 1 means version i does support the feature

The array is sorted in non-decreasing order. That means:
all 0s come first, and once we see the first 1, every element after it is also 1.

Task:
Return the index of the first version that supports the feature.
If no version supports it, return -1.

Examples:
1) support = [0,0,0,1,1,1]
   Output: 3

2) support = [0,0,0,0]
   Output: -1

Important edge cases:
- The first element is already 1
- All elements are 0
- The array has only one element

Goal:
Use binary search to solve the problem in logarithmic time.
*/

using System;

public class Solution
{
    /*
    Time Complexity: O(log n)
    Space Complexity: O(1)

    We use binary search because the array is sorted:
    - all 0s are on the left
    - all 1s are on the right

    That sorted structure allows us to repeatedly cut the search space in half.
    */
    public int FirstSupportingVersion(int[] support)
    {
        // We will search within the full array at the beginning.
        // left points to the start of the current search range.
        // right points to the end of the current search range.
        int left = 0;
        int right = support.Length - 1;

        // This variable will store the best answer found so far.
        // We start with -1, which means "no supporting version found yet".
        // If we ever find a 1, we will update this variable with that index.
        int answer = -1;

        // Continue searching while the current range is valid.
        // A valid range means left has not crossed right.
        while (left <= right)
        {
            // Find the middle index of the current search range.
            // We use this form instead of (left + right) / 2
            // to avoid potential integer overflow in general.
            int mid = left + (right - left) / 2;

            // Case 1:
            // If support[mid] is 1, then mid is a version that supports the feature.
            // That means:
            // - mid could be the first supporting version
            // - but there might be an earlier 1 to the left
            //
            // So we:
            // 1) record mid as a possible answer
            // 2) continue searching the LEFT half to find an even earlier 1
            if (support[mid] == 1)
            {
                answer = mid;
                right = mid - 1;
            }
            else
            {
                // Case 2:
                // If support[mid] is 0, then this version does NOT support the feature.
                // Because the array is sorted, every index at or before mid is also not
                // the first supporting version.
                //
                // Therefore, we must search only the RIGHT half.
                left = mid + 1;
            }
        }

        // If we found at least one 1, answer holds the first such index.
        // Otherwise, answer is still -1, meaning no version supports the feature.
        return answer;
    }
}

// Demo code:
// Create a solution object, run the required examples, and print the results.

var solution = new Solution();

// Example 1:
// support = [0,0,0,1,1,1]
// The first 1 appears at index 3.
int[] support1 = { 0, 0, 0, 1, 1, 1 };
int result1 = solution.FirstSupportingVersion(support1);
Console.WriteLine(result1); // Expected: 3

// Example 2:
// support = [0,0,0,0]
// There is no 1, so the answer should be -1.
int[] support2 = { 0, 0, 0, 0 };
int result2 = solution.FirstSupportingVersion(support2);
Console.WriteLine(result2); // Expected: -1

// Additional edge case demo:
// First element is already 1.
int[] support3 = { 1, 1, 1, 1 };
int result3 = solution.FirstSupportingVersion(support3);
Console.WriteLine(result3); // Expected: 0

// Additional edge case demo:
// Single element array containing 0.
int[] support4 = { 0 };
int result4 = solution.FirstSupportingVersion(support4);
Console.WriteLine(result4); // Expected: -1

// Additional edge case demo:
// Single element array containing 1.
int[] support5 = { 1 };
int result5 = solution.FirstSupportingVersion(support5);
Console.WriteLine(result5); // Expected: 0