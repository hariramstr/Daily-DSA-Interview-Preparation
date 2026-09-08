/*
Title: Longest Study Window With Limited Difficult Problems
Difficulty: Medium
Topic: Sliding Window

Problem Description:
You are given an array problems where problems[i] is the difficulty rating of the i-th practice problem in the order a student solved them.
You are also given two integers threshold and k. A problem is considered difficult if its difficulty rating is greater than or equal to threshold.

Find the length of the longest contiguous window of solved problems that contains at most k difficult problems.

In other words, you may choose any consecutive segment of the array, but inside that segment the number of values greater than or equal to threshold must not exceed k.
Return the maximum possible length of such a segment.

This models a realistic interview-style scenario where you want to identify the longest sustained practice streak that does not contain too many high-difficulty interruptions.

Constraints:
- 1 <= problems.length <= 200000
- 0 <= problems[i] <= 1000000000
- 0 <= k <= problems.length
- 0 <= threshold <= 1000000000

Example 1:
Input: problems = [2, 7, 3, 9, 4, 8, 1], threshold = 7, k = 2
Output: 5
Explanation: Difficult problems are those with value >= 7, so the difficult entries are 7, 9, and 8.
The longest valid contiguous window with at most 2 difficult problems is [7, 3, 9, 4, 8] or [2, 7, 3, 9, 4], both of length 5.

Example 2:
Input: problems = [10, 1, 1, 10, 1, 10, 1, 1], threshold = 10, k = 1
Output: 4
Explanation: Any valid window may contain at most one value >= 10.
One optimal window is [1, 1, 10, 1], which has length 4.
*/

using System;

public class Solution
{
    /*
    Time Complexity: O(n)
    - Each element is processed by the right pointer once.
    - Each element is removed from the left side at most once.
    - Therefore, the total amount of work is linear in the size of the array.

    Space Complexity: O(1)
    - We only use a few integer variables.
    - No extra data structures proportional to input size are needed.
    */
    public int LongestStudyWindow(int[] problems, int threshold, int k)
    {
        // This variable marks the left boundary of our current sliding window.
        // The window always represents a contiguous segment: problems[left..right].
        int left = 0;

        // This variable stores how many "difficult" problems are currently inside the window.
        // A problem is difficult if its value is >= threshold.
        int difficultCount = 0;

        // This variable stores the best (maximum) valid window length found so far.
        int maxLength = 0;

        // We expand the window one element at a time by moving the right boundary.
        // For every position 'right', we try to include problems[right] into the current window.
        for (int right = 0; right < problems.Length; right++)
        {
            // Step 1:
            // Check whether the newly added problem at index 'right' is difficult.
            // If it is, we increase difficultCount because the current window now contains one more difficult problem.
            if (problems[right] >= threshold)
            {
                difficultCount++;
            }

            // Step 2:
            // If the window now contains too many difficult problems, it is invalid.
            // The problem requires "at most k difficult problems", so while difficultCount > k,
            // we must shrink the window from the left until it becomes valid again.
            while (difficultCount > k)
            {
                // Before moving 'left' forward, check whether the element leaving the window
                // is a difficult problem. If yes, removing it decreases difficultCount.
                if (problems[left] >= threshold)
                {
                    difficultCount--;
                }

                // Move the left boundary to the right, effectively removing problems[left]
                // from the current window.
                left++;
            }

            // Step 3:
            // At this point, the window [left..right] is guaranteed to be valid,
            // because difficultCount <= k.
            //
            // So we compute its length:
            // length = right - left + 1
            //
            // Then we compare it with the best answer seen so far.
            int currentLength = right - left + 1;
            if (currentLength > maxLength)
            {
                maxLength = currentLength;
            }
        }

        // After scanning the entire array, maxLength contains the longest valid window length.
        return maxLength;
    }
}

// Demo code

var solution = new Solution();

// Example 1
int[] problems1 = { 2, 7, 3, 9, 4, 8, 1 };
int threshold1 = 7;
int k1 = 2;
int result1 = solution.LongestStudyWindow(problems1, threshold1, k1);
Console.WriteLine(result1); // Expected: 5

// Example 2
int[] problems2 = { 10, 1, 1, 10, 1, 10, 1, 1 };
int threshold2 = 10;
int k2 = 1;
int result2 = solution.LongestStudyWindow(problems2, threshold2, k2);
Console.WriteLine(result2); // Expected: 4

// Additional demo
int[] problems3 = { 1, 2, 3, 4, 5 };
int threshold3 = 10;
int k3 = 0;
int result3 = solution.LongestStudyWindow(problems3, threshold3, k3);
Console.WriteLine(result3); // Expected: 5

// Additional demo
int[] problems4 = { 10, 10, 10 };
int threshold4 = 10;
int k4 = 0;
int result4 = solution.LongestStudyWindow(problems4, threshold4, k4);
Console.WriteLine(result4); // Expected: 0