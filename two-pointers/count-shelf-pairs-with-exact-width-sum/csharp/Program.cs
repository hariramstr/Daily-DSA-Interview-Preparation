/*
Title: Count Shelf Pairs With Exact Width Sum
Difficulty: Medium
Topic: Two Pointers

Problem Description:
You are given an integer array widths representing the widths of wooden shelves currently stored in a warehouse.
The array is not guaranteed to be sorted. You are also given an integer targetWidth.
A pair of shelves (i, j) is considered valid if i < j and widths[i] + widths[j] == targetWidth.

Your task is to return the total number of valid index pairs.

Because the warehouse may contain many shelves with the same width, duplicate values must be handled correctly.
For example, if four shelves have width 2 and targetWidth is 4, then they form 6 distinct pairs because every
choice of two different indices counts.

Design an efficient solution using sorting and the two-pointer technique. A brute-force O(n^2) solution will be
too slow for the largest inputs.

Constraints:
- 1 <= widths.length <= 2 * 10^5
- -10^9 <= widths[i] <= 10^9
- -2 * 10^9 <= targetWidth <= 2 * 10^9
- The answer fits in a 64-bit signed integer.

Example 1:
Input: widths = [1, 5, 3, 3, 2, 4], targetWidth = 6
Output: 3
Explanation: The valid pairs are formed by values (1,5), (2,4), and the two shelves with width 3.

Example 2:
Input: widths = [2, 2, 2, 2, 3, 1], targetWidth = 4
Output: 6
Explanation: Only shelves with width 2 can pair with each other. There are 4 such shelves, so the number of
index pairs is 4 choose 2 = 6.
*/

using System;

public class Solution
{
    /*
    Time Complexity:
    - Sorting the array takes O(n log n)
    - The two-pointer scan takes O(n)
    - Total: O(n log n)

    Space Complexity:
    - If we sort the input array in place, the extra algorithmic space is O(1)
      (ignoring the internal implementation details of the sorting routine)
    */
    public long CountPairsWithExactWidthSum(int[] widths, int targetWidth)
    {
        // Step 1:
        // Sort the array so that we can use the two-pointer technique.
        //
        // Why sorting is necessary:
        // The two-pointer method depends on the array being in sorted order.
        // Once sorted:
        // - If the current sum is too small, we know we must move the left pointer right
        //   to increase the sum.
        // - If the current sum is too large, we know we must move the right pointer left
        //   to decrease the sum.
        //
        // Without sorting, pointer movement would not have any reliable meaning.
        Array.Sort(widths);

        // This variable stores the final number of valid index pairs.
        // We use long because the number of pairs can be large.
        long pairCount = 0;

        // Step 2:
        // Initialize two pointers:
        // - left starts at the beginning of the sorted array
        // - right starts at the end of the sorted array
        //
        // We will move these pointers inward until they cross.
        int left = 0;
        int right = widths.Length - 1;

        // Step 3:
        // Continue while there is still a valid range to inspect.
        while (left < right)
        {
            // Compute the current sum using long to be extra safe,
            // even though int would still fit within the given constraints.
            long currentSum = (long)widths[left] + widths[right];

            // Case A:
            // If the current sum is smaller than the target,
            // we need a larger sum.
            //
            // Because the array is sorted, moving the left pointer to the right
            // gives us a value that is the same or larger, which may increase the sum.
            if (currentSum < targetWidth)
            {
                left++;
            }
            // Case B:
            // If the current sum is larger than the target,
            // we need a smaller sum.
            //
            // Because the array is sorted, moving the right pointer to the left
            // gives us a value that is the same or smaller, which may decrease the sum.
            else if (currentSum > targetWidth)
            {
                right--;
            }
            else
            {
                // Case C:
                // We found values at positions left and right whose sum equals targetWidth.
                //
                // Now we must count how many duplicate values exist on the left side
                // and how many duplicate values exist on the right side.
                //
                // This is the key part that makes the algorithm correct for duplicates.

                // If both pointer values are the same, then every value between left and right
                // is identical.
                //
                // Example:
                // sorted widths = [2, 2, 2, 2]
                // target = 4
                //
                // If left points to the first 2 and right points to the last 2,
                // then every pair among these 4 elements is valid.
                if (widths[left] == widths[right])
                {
                    // Number of equal elements in this block.
                    long count = right - left + 1;

                    // Number of ways to choose any 2 distinct indices from 'count' elements:
                    // count * (count - 1) / 2
                    //
                    // This correctly counts all index pairs in one step.
                    pairCount += count * (count - 1) / 2;

                    // We are done, because all elements between left and right
                    // have been fully counted.
                    break;
                }

                // Otherwise, the left value and right value are different.
                // We count how many duplicates of widths[left] appear consecutively,
                // and how many duplicates of widths[right] appear consecutively.
                //
                // Example:
                // sorted widths = [1, 1, 1, 5, 5]
                // target = 6
                //
                // If left value is 1 appearing 3 times, and right value is 5 appearing 2 times,
                // then we get 3 * 2 = 6 valid pairs from these groups.

                int leftValue = widths[left];
                int rightValue = widths[right];

                long leftCount = 0;
                long rightCount = 0;

                // Count how many times leftValue repeats starting from the left pointer.
                //
                // Why this is necessary:
                // If the left value appears multiple times, each occurrence can pair
                // with each matching occurrence on the right side.
                while (left <= right && widths[left] == leftValue)
                {
                    leftCount++;
                    left++;
                }

                // Count how many times rightValue repeats starting from the right pointer.
                //
                // Why this is necessary:
                // Just like on the left side, duplicates on the right side create
                // multiple distinct index pairs.
                while (right >= left && widths[right] == rightValue)
                {
                    rightCount++;
                    right--;
                }

                // Every occurrence of leftValue can pair with every occurrence of rightValue.
                //
                // So the number of new valid pairs is:
                // leftCount * rightCount
                pairCount += leftCount * rightCount;
            }
        }

        // Return the total number of valid index pairs.
        return pairCount;
    }
}

// Demo code:
// Create sample inputs, call the solution, and print the results.

var solution = new Solution();

// Example 1:
// widths = [1, 5, 3, 3, 2, 4], targetWidth = 6
// Valid pairs are:
// (1,5), (2,4), and (3,3) => total 3
int[] widths1 = { 1, 5, 3, 3, 2, 4 };
int targetWidth1 = 6;
long result1 = solution.CountPairsWithExactWidthSum(widths1, targetWidth1);
Console.WriteLine(result1); // Expected: 3

// Example 2:
// widths = [2, 2, 2, 2, 3, 1], targetWidth = 4
// Only 2 + 2 works, and there are 4 twos.
// Number of pairs = 4 choose 2 = 6
int[] widths2 = { 2, 2, 2, 2, 3, 1 };
int targetWidth2 = 4;
long result2 = solution.CountPairsWithExactWidthSum(widths2, targetWidth2);
Console.WriteLine(result2); // Expected: 6

// Additional demo:
int[] widths3 = { -1, 7, 2, 4, 3, 3, 5, 1 };
int targetWidth3 = 6;
long result3 = solution.CountPairsWithExactWidthSum(widths3, targetWidth3);
Console.WriteLine(result3); // Pairs include (1,5), (2,4), (3,3) => Expected: 4