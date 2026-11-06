/*
Title: Longest Pantry Window With At Most K Expired Items
Difficulty: Easy
Topic: Sliding Window

Problem Description:
A grocery store tracks a row of pantry items in the order they were scanned during an inspection.
Each item is marked as either fresh or expired.

You are given an array items where:
- items[i] = 0 means the i-th item is fresh
- items[i] = 1 means the i-th item is expired

You are also given an integer k, representing the maximum number of expired items
the manager is willing to temporarily tolerate inside one continuous inspection segment.

Your task is to return the length of the longest contiguous segment of items
that contains at most k expired items.

In other words, find the largest window of consecutive entries where the number of 1s
is less than or equal to k.

Constraints:
- 1 <= items.length <= 100000
- items[i] is either 0 or 1
- 0 <= k <= items.length

Example 1:
Input: items = [0,1,0,0,1,0,0], k = 1
Output: 4

Explanation:
A longest valid segment is from index 2 to index 5:
[0,0,1,0]
This window has length 4 and contains only 1 expired item.

Example 2:
Input: items = [1,1,0,0,0,1,0], k = 2
Output: 6

Explanation:
The segment from index 1 to index 6:
[1,0,0,0,1,0]
has length 6 and contains exactly 2 expired items, which is valid.
*/

using System;

public class Solution
{
    /*
    Time Complexity: O(n)
    - Each element is processed at most twice:
      1) once when the right pointer expands the window
      2) once when the left pointer shrinks the window
    - Therefore the total work is linear in the size of the array.

    Space Complexity: O(1)
    - We only use a few integer variables.
    - No extra data structures proportional to input size are needed.
    */
    public int LongestPantryWindow(int[] items, int k)
    {
        // The left boundary of our sliding window.
        // The window will always represent a contiguous segment from left to right.
        int left = 0;

        // This variable stores how many expired items (value 1) are currently
        // inside the active window [left..right].
        int expiredCount = 0;

        // This variable stores the best (maximum) valid window length found so far.
        int maxLength = 0;

        // We move the right boundary from the beginning of the array to the end.
        // At every step, we try to include items[right] into the current window.
        for (int right = 0; right < items.Length; right++)
        {
            // STEP 1: Expand the window by including the current right element.
            // If the new element is expired (1), then the number of expired items
            // inside the window increases by one.
            if (items[right] == 1)
            {
                expiredCount++;
            }

            // STEP 2: If the window became invalid, shrink it from the left.
            //
            // A window is invalid when it contains more than k expired items.
            // Since the problem allows AT MOST k expired items, any count above k
            // must be corrected before we consider this window for the answer.
            //
            // We use a while loop instead of an if statement because removing just
            // one element from the left may still leave the window invalid.
            while (expiredCount > k)
            {
                // Before moving left forward, check whether the element leaving
                // the window is an expired item.
                //
                // If it is expired, then removing it decreases expiredCount.
                if (items[left] == 1)
                {
                    expiredCount--;
                }

                // Move the left boundary one step to the right.
                // This shrinks the window and helps restore validity.
                left++;
            }

            // STEP 3: At this point, the window [left..right] is guaranteed valid.
            // Why? Because the while loop above stops only when expiredCount <= k.
            //
            // So now we can safely compute the current valid window length.
            int currentLength = right - left + 1;

            // STEP 4: Update the best answer if the current valid window is larger
            // than any valid window we have seen before.
            if (currentLength > maxLength)
            {
                maxLength = currentLength;
            }
        }

        // After scanning the entire array, maxLength contains the length of the
        // longest contiguous segment with at most k expired items.
        return maxLength;
    }
}

// Demo code

var solution = new Solution();

// Example 1
int[] items1 = { 0, 1, 0, 0, 1, 0, 0 };
int k1 = 1;
int result1 = solution.LongestPantryWindow(items1, k1);
Console.WriteLine("Example 1 Result: " + result1); // Expected: 4

// Example 2
int[] items2 = { 1, 1, 0, 0, 0, 1, 0 };
int k2 = 2;
int result2 = solution.LongestPantryWindow(items2, k2);
Console.WriteLine("Example 2 Result: " + result2); // Expected: 6

// Additional demo
int[] items3 = { 0, 0, 0, 0 };
int k3 = 0;
int result3 = solution.LongestPantryWindow(items3, k3);
Console.WriteLine("Additional Example 3 Result: " + result3); // Expected: 4

int[] items4 = { 1, 1, 1, 1 };
int k4 = 2;
int result4 = solution.LongestPantryWindow(items4, k4);
Console.WriteLine("Additional Example 4 Result: " + result4); // Expected: 2