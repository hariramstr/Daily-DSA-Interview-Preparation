/*
Title: Longest Delivery Streak With Limited Cold Packages
Difficulty: Medium
Topic: Sliding Window

Problem Description:
A courier company records deliveries in the order they are loaded onto a truck. Each delivery is labeled with a category:
- 'N' for normal packages
- 'C' for cold-chain packages that require expensive refrigerated handling

The company wants to find the longest consecutive stretch of deliveries that can be assigned to a standard truck,
as long as that stretch contains at most k cold-chain packages.

Given a string deliveries where each character is either 'N' or 'C', and an integer k, return the length of the
longest contiguous substring that contains at most k occurrences of 'C'.

This is a realistic scheduling problem: the deliveries must remain in their original order, and you may only choose
one contiguous block. A block with more than k cold-chain packages is invalid because it would exceed the truck's
refrigeration allowance.

Constraints:
- 1 <= deliveries.length <= 200000
- deliveries[i] is either 'N' or 'C'
- 0 <= k <= deliveries.length

Example 1:
Input: deliveries = "NCCNNNCN", k = 2
Output: 6
Explanation: The longest valid block is "CNNNCN" or "NCCNNN" depending on how the window is chosen.
Each contains at most 2 cold-chain packages, so the answer is 6.

Example 2:
Input: deliveries = "CCCCNNC", k = 1
Output: 3
Explanation: Any valid block can contain at most one 'C'. One longest choice is "CNN" after taking a contiguous
segment with exactly one cold-chain package. No longer valid segment exists.
*/

using System;

public class Solution
{
    /*
    Time Complexity: O(n)
    - Each character is processed by the right pointer once.
    - Each character is removed from the window by the left pointer at most once.
    - Therefore, the total work is linear in the length of the string.

    Space Complexity: O(1)
    - We only store a few integer variables.
    - No extra data structures proportional to input size are used.
    */
    public int LongestDeliveryStreak(string deliveries, int k)
    {
        // This pointer marks the beginning of our current sliding window.
        // The window always represents a contiguous block of deliveries.
        int left = 0;

        // This variable counts how many cold-chain packages ('C') are currently
        // inside the window from index left to index right.
        // We need this because the rule says the chosen block may contain
        // at most k cold-chain packages.
        int coldCount = 0;

        // This stores the best (maximum) valid window length we have seen so far.
        int maxLength = 0;

        // We expand the window one character at a time using the right pointer.
        // At every step, the window is deliveries[left..right].
        for (int right = 0; right < deliveries.Length; right++)
        {
            // Step 1: Include the new character at position "right" into the window.
            // If it is a cold-chain package, increase our count.
            // This is necessary because the validity of the window depends on
            // how many 'C' characters it contains.
            if (deliveries[right] == 'C')
            {
                coldCount++;
            }

            // Step 2: If the window has become invalid (too many 'C' packages),
            // we must shrink it from the left until it becomes valid again.
            //
            // Why a while loop instead of an if?
            // Because adding one new character could make the window exceed the limit,
            // and we may need to remove multiple characters from the left before the
            // number of cold packages is back to at most k.
            while (coldCount > k)
            {
                // Before moving the left pointer forward, check whether the character
                // leaving the window is a cold-chain package.
                // If it is, we must decrease coldCount because that 'C' is no longer
                // part of the current window.
                if (deliveries[left] == 'C')
                {
                    coldCount--;
                }

                // Move the left boundary rightward by one position.
                // This shrinks the window and helps restore validity.
                left++;
            }

            // Step 3: At this point, the window is guaranteed to be valid:
            // it contains at most k cold-chain packages.
            //
            // So we compute its current length.
            // Since both left and right are inclusive indices,
            // length = right - left + 1.
            int currentLength = right - left + 1;

            // Step 4: Update the best answer if this valid window is larger
            // than any valid window we have seen before.
            if (currentLength > maxLength)
            {
                maxLength = currentLength;
            }
        }

        // After scanning the entire string, maxLength holds the length of the
        // longest contiguous substring containing at most k occurrences of 'C'.
        return maxLength;
    }
}

// Demo code
var solution = new Solution();

// Example 1
string deliveries1 = "NCCNNNCN";
int k1 = 2;
int result1 = solution.LongestDeliveryStreak(deliveries1, k1);
Console.WriteLine($"Deliveries: {deliveries1}, k = {k1}, Longest valid streak = {result1}");

// Example 2
string deliveries2 = "CCCCNNC";
int k2 = 1;
int result2 = solution.LongestDeliveryStreak(deliveries2, k2);
Console.WriteLine($"Deliveries: {deliveries2}, k = {k2}, Longest valid streak = {result2}");

// Additional demo cases
string deliveries3 = "NNNNN";
int k3 = 0;
int result3 = solution.LongestDeliveryStreak(deliveries3, k3);
Console.WriteLine($"Deliveries: {deliveries3}, k = {k3}, Longest valid streak = {result3}");

string deliveries4 = "CNCNCNCN";
int k4 = 2;
int result4 = solution.LongestDeliveryStreak(deliveries4, k4);
Console.WriteLine($"Deliveries: {deliveries4}, k = {k4}, Longest valid streak = {result4}");

string deliveries5 = "C";
int k5 = 0;
int result5 = solution.LongestDeliveryStreak(deliveries5, k5);
Console.WriteLine($"Deliveries: {deliveries5}, k = {k5}, Longest valid streak = {result5}");