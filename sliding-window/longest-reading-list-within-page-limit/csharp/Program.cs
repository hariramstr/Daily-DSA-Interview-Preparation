/*
Title: Longest Reading List Within Page Limit
Difficulty: Easy
Topic: Sliding Window

Problem Description:
You are given an array `pages` where `pages[i]` is the number of pages in the `i`-th article
of an online reading list. A user wants to read a consecutive group of articles in one session,
but they can read at most `maxPages` pages total.

Your task is to return the maximum number of consecutive articles the user can read without the
sum of pages in that group exceeding `maxPages`.

In other words, find the length of the longest contiguous subarray whose sum is less than or
equal to `maxPages`.

This problem models a common interview pattern where all values are non-negative, which makes
a sliding window approach efficient. As you expand the right end of the window, keep track of
the total pages. If the total becomes too large, move the left end forward until the window is
valid again. The answer is the largest valid window size seen during the scan.

Constraints:
- 1 <= pages.length <= 100000
- 0 <= pages[i] <= 10000
- 0 <= maxPages <= 1000000000
- All page counts are non-negative integers.

Example 1:
Input: pages = [4, 2, 1, 7, 3, 2], maxPages = 8
Output: 3
Explanation: The longest valid consecutive group is [4, 2, 1] with total 7.
Any window of length 4 exceeds 8.

Example 2:
Input: pages = [1, 1, 1, 1, 1], maxPages = 3
Output: 3
Explanation: Any 3 consecutive articles fit within the limit, but 4 articles would total 4.
*/

using System;

public class Solution
{
    /*
    Time Complexity: O(n)
    - Each article is added to the window once by moving the right pointer forward.
    - Each article is removed from the window at most once by moving the left pointer forward.
    - Because both pointers only move from left to right, the total work is linear.

    Space Complexity: O(1)
    - We only use a few extra variables: left pointer, running sum, and answer.
    - No extra arrays, lists, or other data structures are needed.
    */
    public int LongestReadingListWithinPageLimit(int[] pages, int maxPages)
    {
        // This pointer marks the beginning of our current sliding window.
        // The window will always represent a consecutive group of articles.
        int left = 0;

        // This variable stores the total number of pages inside the current window.
        // We use long for extra safety, even though the given constraints fit in int.
        // Using long is a good habit when repeatedly adding values.
        long currentSum = 0;

        // This will store the best (largest) valid window length we have seen so far.
        int bestLength = 0;

        // We move the right pointer from left to right across the array.
        // At each step, we try to include pages[right] in the current reading session.
        for (int right = 0; right < pages.Length; right++)
        {
            // Step 1: Expand the window by including the article at index "right".
            // Why?
            // We want to consider every possible ending position of a consecutive group.
            // By adding this article, the window now covers indices [left..right].
            currentSum += pages[right];

            // Step 2: If the total pages is too large, the window is invalid.
            // Because all page counts are non-negative, adding more articles can never
            // reduce the sum. So the only way to make the window valid again is to
            // move the left side forward and remove articles from the start.
            while (currentSum > maxPages && left <= right)
            {
                // Remove the leftmost article from the running total.
                // This shrinks the window from the left side.
                currentSum -= pages[left];

                // Move the left boundary one step to the right.
                // After this, the window becomes [left + 1 .. right].
                left++;
            }

            // Step 3: At this point, the window is guaranteed to be valid:
            // currentSum <= maxPages
            //
            // So we can measure its length and compare it with the best answer seen so far.
            int currentLength = right - left + 1;

            // If this valid window is larger than any previous valid window,
            // update the answer.
            if (currentLength > bestLength)
            {
                bestLength = currentLength;
            }
        }

        // After scanning the entire array, bestLength contains the maximum number
        // of consecutive articles that fit within the page limit.
        return bestLength;
    }
}

// Demo code
var solution = new Solution();

// Example 1
int[] pages1 = { 4, 2, 1, 7, 3, 2 };
int maxPages1 = 8;
int result1 = solution.LongestReadingListWithinPageLimit(pages1, maxPages1);
Console.WriteLine($"Example 1 Result: {result1}");

// Example 2
int[] pages2 = { 1, 1, 1, 1, 1 };
int maxPages2 = 3;
int result2 = solution.LongestReadingListWithinPageLimit(pages2, maxPages2);
Console.WriteLine($"Example 2 Result: {result2}");

// Additional demo
int[] pages3 = { 0, 0, 5, 0, 2, 1 };
int maxPages3 = 5;
int result3 = solution.LongestReadingListWithinPageLimit(pages3, maxPages3);
Console.WriteLine($"Additional Demo Result: {result3}");