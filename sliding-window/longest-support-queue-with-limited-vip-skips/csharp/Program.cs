/*
Title: Longest Support Queue With Limited VIP Skips
Difficulty: Medium
Topic: Sliding Window

Problem Description:
A customer support system records the arrival order of tickets in an array `tickets`, where
`tickets[i]` is:
- 0 for a regular ticket
- 1 for a VIP ticket

The support team wants to analyze contiguous portions of the queue that can be handled by one
standard agent.

A standard agent can process any number of regular tickets, but can tolerate at most `k` VIP
tickets in the assigned contiguous segment by forwarding those VIP tickets to a specialist.

Task:
Find the length of the longest contiguous subarray of `tickets` that contains at most `k` VIP tickets.

Constraints:
- 1 <= tickets.length <= 100000
- tickets[i] is either 0 or 1
- 0 <= k <= tickets.length

Example 1:
Input: tickets = [0,1,0,0,1,0,0,0], k = 1
Output: 5

Example 2:
Input: tickets = [1,0,1,0,0,1,0], k = 2
Output: 6
*/

using System;

public class Solution
{
    /*
    Time Complexity: O(n)
    - Each element is visited at most twice:
      1) once when the right pointer expands the window
      2) once when the left pointer shrinks the window
    - Because both pointers only move forward, the total work is linear.

    Space Complexity: O(1)
    - We only store a few integer variables.
    - No extra arrays, lists, or hash maps are needed.
    */
    public int LongestSupportQueue(int[] tickets, int k)
    {
        // This variable marks the left boundary of our current sliding window.
        // The window always represents a contiguous segment of the array.
        int left = 0;

        // This counts how many VIP tickets (value 1) are currently inside the window.
        // We must keep this count <= k for the window to remain valid.
        int vipCount = 0;

        // This stores the best (maximum) valid window length found so far.
        int maxLength = 0;

        // We expand the window one element at a time by moving the right boundary.
        // For every position 'right', we include tickets[right] into the current window.
        for (int right = 0; right < tickets.Length; right++)
        {
            // Step 1: Add the new rightmost element into the window.
            // If it is a VIP ticket, increase the VIP count.
            // Why necessary:
            // - The window [left..right] must reflect the exact contents currently being considered.
            // - Since the problem limits how many VIP tickets are allowed, we must track them precisely.
            if (tickets[right] == 1)
            {
                vipCount++;
            }

            // Step 2: If the window has too many VIP tickets, it is invalid.
            // We must shrink it from the left until it becomes valid again.
            //
            // Why a while loop instead of if?
            // - Because adding one new element could make the window exceed the limit by more than
            //   one step of shrinking, depending on what is at the left side.
            // - We continue shrinking until vipCount <= k.
            while (vipCount > k)
            {
                // Before moving left forward, check whether the element leaving the window
                // is a VIP ticket. If yes, reduce vipCount because that VIP is no longer inside.
                if (tickets[left] == 1)
                {
                    vipCount--;
                }

                // Move the left boundary rightward by one position.
                // This shortens the window and helps restore validity.
                left++;
            }

            // Step 3: At this point, the window [left..right] is guaranteed valid:
            // it contains at most k VIP tickets.
            //
            // So we compute its length.
            int currentLength = right - left + 1;

            // Step 4: Update the best answer if this valid window is longer than any previous one.
            // Why necessary:
            // - The problem asks for the maximum length among all valid contiguous segments.
            if (currentLength > maxLength)
            {
                maxLength = currentLength;
            }
        }

        // After scanning the full array, maxLength contains the answer.
        return maxLength;
    }
}

// Demo code

var solution = new Solution();

// Example 1
int[] tickets1 = { 0, 1, 0, 0, 1, 0, 0, 0 };
int k1 = 1;
int result1 = solution.LongestSupportQueue(tickets1, k1);
Console.WriteLine($"Example 1 Result: {result1}"); // Expected: 5

// Example 2
int[] tickets2 = { 1, 0, 1, 0, 0, 1, 0 };
int k2 = 2;
int result2 = solution.LongestSupportQueue(tickets2, k2);
Console.WriteLine($"Example 2 Result: {result2}"); // Expected: 6

// Additional demo cases

// All regular tickets: entire array is valid
int[] tickets3 = { 0, 0, 0, 0 };
int k3 = 0;
int result3 = solution.LongestSupportQueue(tickets3, k3);
Console.WriteLine($"Additional Demo 1 Result: {result3}"); // Expected: 4

// No VIPs allowed, must find longest run of zeros
int[] tickets4 = { 1, 0, 0, 1, 0, 0, 0, 1 };
int k4 = 0;
int result4 = solution.LongestSupportQueue(tickets4, k4);
Console.WriteLine($"Additional Demo 2 Result: {result4}"); // Expected: 3

// k large enough to allow entire array
int[] tickets5 = { 1, 1, 0, 1, 0 };
int k5 = 5;
int result5 = solution.LongestSupportQueue(tickets5, k5);
Console.WriteLine($"Additional Demo 3 Result: {result5}"); // Expected: 5