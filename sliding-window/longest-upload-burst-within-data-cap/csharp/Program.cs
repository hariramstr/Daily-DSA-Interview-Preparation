/*
Title: Longest Upload Burst Within Data Cap
Difficulty: Medium
Topic: Sliding Window

Problem Description:
A mobile app records the size of each file upload a user performs during a session.
You are given an integer array uploads where uploads[i] is the size in megabytes of
the i-th upload, and an integer cap representing the maximum total data allowed in
a continuous burst.

A burst is any contiguous sequence of uploads. Your task is to return the length of
the longest burst whose total uploaded data is less than or equal to cap. If no
single upload fits within the cap, return 0.

This problem models rate-limited network behavior, where the app wants to identify
the longest uninterrupted period of uploads that stayed within a data budget.
Because the burst must be contiguous, reordering uploads is not allowed.

Write a function that efficiently computes the answer for large input sizes.

Constraints:
- 1 <= uploads.length <= 200000
- 0 <= uploads[i] <= 100000
- 0 <= cap <= 1000000000
- All values are integers

Example 1:
Input: uploads = [4, 2, 1, 7, 3, 2], cap = 8
Output: 3
Explanation: The longest valid burst is [4, 2, 1] with total 7. Other valid bursts
such as [3, 2] have smaller length.

Example 2:
Input: uploads = [9, 1, 2, 1, 1], cap = 4
Correct Output: 3
Explanation:
- [9] is invalid because 9 > 4
- [1, 2, 1] has total 4, valid
- [2, 1, 1] has total 4, valid
- [1, 2, 1, 1] has total 5, invalid
So the longest valid burst length is 3.

Approach Summary:
Because all upload sizes are non-negative, we can use the classic sliding window technique.
We expand the right side of the window to include more uploads, and whenever the total
sum becomes too large, we shrink the left side until the window becomes valid again.
This guarantees an O(n) solution.
*/

using System;

public class Solution
{
    /*
    Time Complexity: O(n)
    - Each element is added to the window once when the right pointer moves forward.
    - Each element is removed from the window at most once when the left pointer moves forward.
    - Therefore, the total amount of work is linear in the number of uploads.

    Space Complexity: O(1)
    - We only use a few variables: pointers, running sum, and answer.
    - No extra data structures proportional to input size are needed.
    */
    public int LongestUploadBurstWithinCap(int[] uploads, int cap)
    {
        // This pointer marks the beginning of the current sliding window.
        // The window always represents a contiguous burst from left to right.
        int left = 0;

        // This variable stores the sum of all upload sizes currently inside the window.
        // We use long to be extra safe, even though int would also fit under the given constraints.
        long currentSum = 0;

        // This stores the best (maximum) valid window length we have seen so far.
        int bestLength = 0;

        // We move the right pointer from the start of the array to the end.
        // At each step, we try to extend the current burst by including uploads[right].
        for (int right = 0; right < uploads.Length; right++)
        {
            // Step 1: Expand the window to the right by including the current upload.
            // Why this is necessary:
            // We want to consider every possible contiguous burst, and expanding the
            // right boundary is how the sliding window explores them efficiently.
            currentSum += uploads[right];

            // Step 2: If the window is invalid (sum exceeds cap), shrink it from the left.
            // Why this is necessary:
            // The problem requires the total uploaded data in the burst to be <= cap.
            // Since all numbers are non-negative, once the sum is too large, the only
            // way to make it smaller is to remove elements from the left side.
            //
            // Why a while loop instead of an if statement:
            // It is possible that removing just one element is not enough.
            // We keep shrinking until the window becomes valid again.
            while (currentSum > cap && left <= right)
            {
                // Remove the leftmost upload from the running sum because it is no longer
                // part of the current window after we move the left boundary forward.
                currentSum -= uploads[left];

                // Advance the left pointer, effectively shrinking the burst.
                left++;
            }

            // Step 3: At this point, the window [left..right] is guaranteed to be valid,
            // meaning its total sum is <= cap.
            //
            // We can now compute its length and compare it with the best answer so far.
            int currentLength = right - left + 1;

            // Update the best answer if this valid burst is longer than any previously seen.
            if (currentLength > bestLength)
            {
                bestLength = currentLength;
            }
        }

        // If no single upload fits within the cap, bestLength will remain 0.
        // Otherwise, it will contain the length of the longest valid contiguous burst.
        return bestLength;
    }
}

// Demo code

var solution = new Solution();

// Example 1
int[] uploads1 = { 4, 2, 1, 7, 3, 2 };
int cap1 = 8;
int result1 = solution.LongestUploadBurstWithinCap(uploads1, cap1);
Console.WriteLine($"Example 1 Result: {result1}"); // Expected: 3

// Example 2
int[] uploads2 = { 9, 1, 2, 1, 1 };
int cap2 = 4;
int result2 = solution.LongestUploadBurstWithinCap(uploads2, cap2);
Console.WriteLine($"Example 2 Result: {result2}"); // Expected: 3

// Additional demo: no upload fits
int[] uploads3 = { 5, 6, 7 };
int cap3 = 4;
int result3 = solution.LongestUploadBurstWithinCap(uploads3, cap3);
Console.WriteLine($"Additional Example Result: {result3}"); // Expected: 0

// Additional demo: all uploads fit
int[] uploads4 = { 1, 1, 1, 1 };
int cap4 = 10;
int result4 = solution.LongestUploadBurstWithinCap(uploads4, cap4);
Console.WriteLine($"All Fit Example Result: {result4}"); // Expected: 4