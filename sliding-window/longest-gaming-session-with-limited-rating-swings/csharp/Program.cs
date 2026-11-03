/*
Title: Longest Gaming Session With Limited Rating Swings
Difficulty: Medium
Topic: Sliding Window

Problem Description:
You are given an array ratings where ratings[i] is the skill rating of the opponent faced in the i-th consecutive match of a gaming session.
A session segment is considered stable if the difference between the highest and lowest opponent ratings inside that contiguous segment is at most limit.

Your task is to return the length of the longest stable contiguous segment of matches.

In other words, find the maximum size of a subarray ratings[l..r] such that:
max(ratings[l..r]) - min(ratings[l..r]) <= limit

This models a real matchmaking analysis problem: a player is said to be in a consistent queue environment only when the opponent difficulty does not swing too much over a stretch of consecutive games.

You should design an algorithm efficient enough for large inputs. A brute-force approach that checks every subarray will be too slow.

Constraints:
- 1 <= ratings.length <= 200000
- 1 <= ratings[i] <= 1000000000
- 0 <= limit <= 1000000000

Example 1:
Input: ratings = [1200, 1215, 1190, 1220, 1265, 1230], limit = 40
Output: 4

Example 2:
Input: ratings = [1500, 1500, 1500, 1510, 1498, 1499], limit = 12
Output: 6
*/

using System;
using System.Collections.Generic;

public class Solution
{
    /*
    Time Complexity:
    O(n), where n is the number of ratings.
    Each element is added to and removed from each deque at most one time.

    Space Complexity:
    O(n) in the worst case for the deques.
    */
    public int LongestStableSegment(int[] ratings, int limit)
    {
        // We will use the classic "sliding window" technique.
        //
        // The idea:
        // - We maintain a window [left..right] that represents a current contiguous segment.
        // - We expand the window by moving 'right' one step at a time.
        // - If the window becomes invalid (max - min > limit), we shrink it from the left.
        //
        // The challenge:
        // - For every window, we need to know its current minimum and maximum efficiently.
        // - Recomputing min and max by scanning the whole window every time would be too slow.
        //
        // To solve that efficiently, we use two deques:
        // 1) maxDeque: stores indices of elements in decreasing order of values.
        //    - The front always points to the maximum value in the current window.
        //
        // 2) minDeque: stores indices of elements in increasing order of values.
        //    - The front always points to the minimum value in the current window.
        //
        // Why store indices instead of values?
        // - Because when the left side of the window moves forward, we need to know
        //   whether the oldest max/min element has fallen out of the window.
        // - Indices let us check that directly.

        int n = ratings.Length;

        // These linked lists are used as deques:
        // - AddLast / RemoveLast for maintaining monotonic order
        // - First / RemoveFirst for reading/removing the current window's min/max
        LinkedList<int> maxDeque = new LinkedList<int>();
        LinkedList<int> minDeque = new LinkedList<int>();

        int left = 0;
        int bestLength = 0;

        // Move the right boundary of the window from left to right across the array.
        for (int right = 0; right < n; right++)
        {
            // ------------------------------------------------------------
            // STEP 1: Insert ratings[right] into maxDeque
            // ------------------------------------------------------------
            //
            // maxDeque must remain in decreasing order of values.
            // That means:
            // - The front is the largest value's index.
            // - Smaller values at the back are useless if the new value is larger,
            //   because they can never become the maximum while the new value remains in the window.
            //
            // So we remove from the back while the new value is greater than the values there.
            while (maxDeque.Count > 0 && ratings[maxDeque.Last!.Value] < ratings[right])
            {
                maxDeque.RemoveLast();
            }

            // Now append the current index.
            maxDeque.AddLast(right);

            // ------------------------------------------------------------
            // STEP 2: Insert ratings[right] into minDeque
            // ------------------------------------------------------------
            //
            // minDeque must remain in increasing order of values.
            // That means:
            // - The front is the smallest value's index.
            // - Larger values at the back are useless if the new value is smaller,
            //   because they can never become the minimum while the new value remains in the window.
            //
            // So we remove from the back while the new value is smaller than the values there.
            while (minDeque.Count > 0 && ratings[minDeque.Last!.Value] > ratings[right])
            {
                minDeque.RemoveLast();
            }

            // Now append the current index.
            minDeque.AddLast(right);

            // ------------------------------------------------------------
            // STEP 3: Shrink the window while it is invalid
            // ------------------------------------------------------------
            //
            // The window is invalid if:
            // currentMax - currentMin > limit
            //
            // currentMax is at ratings[maxDeque.First.Value]
            // currentMin is at ratings[minDeque.First.Value]
            //
            // If invalid, we move 'left' forward until the window becomes valid again.
            while (ratings[maxDeque.First!.Value] - ratings[minDeque.First!.Value] > limit)
            {
                // If the index at the front of maxDeque is exactly the element
                // leaving the window, remove it from the deque.
                if (maxDeque.First!.Value == left)
                {
                    maxDeque.RemoveFirst();
                }

                // If the index at the front of minDeque is exactly the element
                // leaving the window, remove it from the deque.
                if (minDeque.First!.Value == left)
                {
                    minDeque.RemoveFirst();
                }

                // Move the left boundary rightward to shrink the window.
                left++;
            }

            // ------------------------------------------------------------
            // STEP 4: Update the best answer
            // ------------------------------------------------------------
            //
            // At this point, the window [left..right] is guaranteed to be valid.
            // So its length is a candidate answer.
            int currentLength = right - left + 1;
            if (currentLength > bestLength)
            {
                bestLength = currentLength;
            }
        }

        return bestLength;
    }
}

// Demo code

var solution = new Solution();

// Example 1
int[] ratings1 = { 1200, 1215, 1190, 1220, 1265, 1230 };
int limit1 = 40;
int result1 = solution.LongestStableSegment(ratings1, limit1);
Console.WriteLine(result1); // Expected: 4

// Example 2
int[] ratings2 = { 1500, 1500, 1500, 1510, 1498, 1499 };
int limit2 = 12;
int result2 = solution.LongestStableSegment(ratings2, limit2);
Console.WriteLine(result2); // Expected: 6

// Additional quick demo
int[] ratings3 = { 8, 2, 4, 7 };
int limit3 = 4;
int result3 = solution.LongestStableSegment(ratings3, limit3);
Console.WriteLine(result3); // Expected: 2