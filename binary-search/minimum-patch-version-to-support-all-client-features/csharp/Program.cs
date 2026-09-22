/*
Title: Minimum Patch Version to Support All Client Features
Difficulty: Medium
Topic: Binary Search

Problem Description:
A software platform publishes patch versions in increasing order from 1 to n. For each patch version i, you are given an integer compatibility score patches[i], where a larger score means that version supports a wider set of client-side features. The array is guaranteed to be non-decreasing, because later patches never remove previously supported features.

You are also given several client requirements. Each requirement is a target compatibility score target. For every target, return the smallest patch version index (1-indexed) whose compatibility score is greater than or equal to target. If no patch version can satisfy the target, return -1 for that query.

Your task is to implement a function that answers all queries efficiently.

This is not a simple linear scan problem: the number of patch versions and queries can both be large, so an efficient binary search solution is expected. Since the compatibility scores are sorted in non-decreasing order, you should search for the leftmost version that satisfies each target.

Constraints:
- 1 <= n <= 200000
- 1 <= q <= 200000
- 0 <= patches[i] <= 1000000000
- patches is non-decreasing
- 0 <= target <= 1000000000

Example 1:
Input: patches = [2, 4, 4, 7, 10], queries = [4, 5, 10, 11]
Output: [2, 4, 5, -1]
Explanation:
- Target 4 is first satisfied by version 2.
- Target 5 is first satisfied by version 4 (score 7).
- Target 10 is first satisfied by version 5.
- No version reaches 11.

Example 2:
Input: patches = [0, 0, 3, 3, 8], queries = [0, 1, 3, 6]
Output: [1, 3, 3, 5]
Explanation:
- Target 0 is already satisfied by the first version.
- Target 1 is first satisfied by version 3.
- Target 3 is also first satisfied by version 3.
- Target 6 is first satisfied by version 5.

Return an array of answers in the same order as the queries.
*/

using System;
using System.Linq;

public class Solution
{
    /*
    Time Complexity:
    - For each query, we perform a binary search on the sorted patches array.
    - A single binary search takes O(log n).
    - If there are q queries, total time is O(q log n).

    Space Complexity:
    - We create an answers array of size q.
    - Aside from that, we use only a few extra variables.
    - Total extra space is O(q).
    */
    public int[] MinimumPatchVersions(int[] patches, int[] queries)
    {
        // This array will store the final answer for each query in the same order
        // as the input queries array.
        // We must preserve order because the problem explicitly asks for answers
        // corresponding to each query position.
        int[] answers = new int[queries.Length];

        // We process each query independently.
        // Since patches is already sorted in non-decreasing order, we can use
        // binary search for every target value.
        for (int i = 0; i < queries.Length; i++)
        {
            int target = queries[i];

            // We want the LEFTMOST index where patches[index] >= target.
            // This is a classic "lower bound" binary search.
            //
            // Search space:
            // left  = first possible index in the array
            // right = last possible index in the array
            int left = 0;
            int right = patches.Length - 1;

            // This variable stores the best answer found so far.
            // We initialize it to -1, meaning "not found yet".
            //
            // If we discover any index whose value is >= target, we save it here.
            // But we do NOT stop immediately, because there might be an earlier
            // valid index to the left, and the problem asks for the smallest
            // patch version index.
            int foundIndex = -1;

            // Continue searching while the current search range is valid.
            while (left <= right)
            {
                // Compute the middle index carefully.
                // This form avoids overflow in languages where left + right
                // could exceed integer limits.
                int mid = left + (right - left) / 2;

                // If the middle patch score is large enough, then mid is a valid
                // candidate answer.
                if (patches[mid] >= target)
                {
                    // Save this index as a possible answer.
                    foundIndex = mid;

                    // Why move right boundary to mid - 1?
                    // Because we are not satisfied with just any valid index.
                    // We specifically need the FIRST valid index.
                    // So after finding a valid position, we continue searching
                    // on the left half to see whether an even smaller index also works.
                    right = mid - 1;
                }
                else
                {
                    // If patches[mid] < target, then mid is too small and cannot
                    // satisfy the query.
                    //
                    // Because the array is sorted in non-decreasing order,
                    // every index to the left of mid is also <= patches[mid],
                    // so none of them can satisfy the target either.
                    //
                    // Therefore, we safely discard the entire left half including mid,
                    // and continue searching only in the right half.
                    left = mid + 1;
                }
            }

            // Convert from 0-based array index to 1-based patch version number.
            // If foundIndex stayed -1, then no patch satisfies the target.
            answers[i] = foundIndex == -1 ? -1 : foundIndex + 1;
        }

        return answers;
    }
}

// Demo code

var solution = new Solution();

// Example 1:
// patches = [2, 4, 4, 7, 10], queries = [4, 5, 10, 11]
// Expected output: [2, 4, 5, -1]
int[] patches1 = { 2, 4, 4, 7, 10 };
int[] queries1 = { 4, 5, 10, 11 };
int[] result1 = solution.MinimumPatchVersions(patches1, queries1);
Console.WriteLine("Example 1 Output: [" + string.Join(", ", result1) + "]");

// Example 2:
// patches = [0, 0, 3, 3, 8], queries = [0, 1, 3, 6]
// Expected output: [1, 3, 3, 5]
int[] patches2 = { 0, 0, 3, 3, 8 };
int[] queries2 = { 0, 1, 3, 6 };
int[] result2 = solution.MinimumPatchVersions(patches2, queries2);
Console.WriteLine("Example 2 Output: [" + string.Join(", ", result2) + "]");

// Additional small demo
int[] patches3 = { 1, 2, 2, 2, 9 };
int[] queries3 = { 0, 2, 3, 9, 10 };
int[] result3 = solution.MinimumPatchVersions(patches3, queries3);
Console.WriteLine("Additional Demo Output: [" + string.Join(", ", result3) + "]");