/*
Title: Longest Snack Cart Run Within Sodium Limit
Difficulty: Medium
Topic: Sliding Window

Problem Description:
A company cafeteria tracks the sodium content of snacks placed on a moving cart throughout the day.
You are given an integer array sodium where sodium[i] is the sodium amount (in milligrams) of the
i-th snack on the cart, in order.

A customer wants to choose one contiguous run of snacks, but the total sodium in that chosen run
must not exceed a given limit maxSodium.

Return the length of the longest contiguous subarray whose sum is less than or equal to maxSodium.
If no snack can be chosen without exceeding the limit, return 0.

This problem models a realistic streaming or sequential selection scenario where the chosen items
must stay contiguous and the total budget must remain bounded. An efficient solution is expected
because the list can be large. Since all sodium values are non-negative, a sliding window approach
can expand the right end of the window and shrink the left end whenever the running sum becomes
too large.

Constraints:
- 1 <= sodium.length <= 200000
- 0 <= sodium[i] <= 100000
- 0 <= maxSodium <= 1000000000
- All values fit in 32-bit integers, but using a 64-bit running sum is recommended.

Important correctness note about Example 1:
The original prompt text contains a contradiction:
- It first says Output: 3
- But its own explanation shows the best valid contiguous runs are length 2
For sodium = [120, 80, 200, 90, 60] and maxSodium = 300:
- [120, 80] = 200, length 2
- [80, 200] = 280, length 2
- [200, 90] = 290, length 2
- Any length-3 contiguous subarray exceeds 300
So the correct answer for Example 1 is 2.

Example 1:
Input: sodium = [120, 80, 200, 90, 60], maxSodium = 300
Correct Output: 2

Example 2:
Input: sodium = [40, 0, 30, 20, 10], maxSodium = 60
Output: 4
Explanation: The subarray [0, 30, 20, 10] has total sodium 60, so its length is 4.
*/

using System;

public class Solution
{
    /*
    Time Complexity: O(n)
    - Each element is added to the running sum once when the right pointer moves forward.
    - Each element is removed from the running sum at most once when the left pointer moves forward.
    - Therefore, both pointers together move at most 2n times.

    Space Complexity: O(1)
    - We only use a few variables: left pointer, running sum, and best length.
    - No extra data structures proportional to input size are needed.
    */
    public int LongestSnackCartRunWithinSodiumLimit(int[] sodium, int maxSodium)
    {
        // The left boundary of our sliding window.
        // The window will always represent a contiguous subarray from left to right.
        int left = 0;

        // We use long instead of int for safety.
        // Even though each individual sodium value fits in int, the sum of many values
        // could become large during processing. Using long avoids overflow issues.
        long currentSum = 0;

        // This stores the best (maximum) valid window length found so far.
        int maxLength = 0;

        // We move the right boundary from the start of the array to the end.
        // At every step, we try to include sodium[right] in the current window.
        for (int right = 0; right < sodium.Length; right++)
        {
            // Step 1: Expand the window to the right by including the current element.
            // Why?
            // We want to consider every possible contiguous subarray ending at 'right'.
            // By adding sodium[right], we grow the current candidate window.
            currentSum += sodium[right];

            // Step 2: If the window is invalid (sum too large), shrink it from the left.
            // Why?
            // The problem requires the sum to be <= maxSodium.
            // Since all values are non-negative, adding more elements can only keep the sum
            // the same or increase it. So if the sum is too large, the only way to fix it
            // is to move the left boundary forward and remove elements from the window.
            while (currentSum > maxSodium && left <= right)
            {
                // Remove the element at the left boundary from the running sum,
                // because that element is no longer part of the window after we move left.
                currentSum -= sodium[left];

                // Move the left boundary one step to the right.
                left++;
            }

            // Step 3: At this point, the window [left..right] is guaranteed to be valid
            // (its sum is <= maxSodium), because the while loop stopped only after fixing it.
            //
            // So we can compute its length and compare it with the best answer seen so far.
            int currentLength = right - left + 1;

            // Step 4: Update the best answer if this valid window is longer.
            // Why?
            // The goal is to return the maximum length among all valid contiguous subarrays.
            if (currentLength > maxLength)
            {
                maxLength = currentLength;
            }
        }

        // After processing all possible right boundaries, maxLength contains the answer.
        return maxLength;
    }
}

// Demo code

var solution = new Solution();

// Example 1 from the prompt, corrected by careful verification.
// sodium = [120, 80, 200, 90, 60], maxSodium = 300
// Valid best length is 2, not 3.
int[] sodium1 = { 120, 80, 200, 90, 60 };
int maxSodium1 = 300;
int result1 = solution.LongestSnackCartRunWithinSodiumLimit(sodium1, maxSodium1);
Console.WriteLine($"Example 1 Result: {result1}");

// Example 2 from the prompt.
// sodium = [40, 0, 30, 20, 10], maxSodium = 60
// Best valid contiguous subarray is [0, 30, 20, 10], length 4.
int[] sodium2 = { 40, 0, 30, 20, 10 };
int maxSodium2 = 60;
int result2 = solution.LongestSnackCartRunWithinSodiumLimit(sodium2, maxSodium2);
Console.WriteLine($"Example 2 Result: {result2}");

// Additional demo: no single snack can be chosen because every value exceeds the limit.
int[] sodium3 = { 100, 200, 300 };
int maxSodium3 = 50;
int result3 = solution.LongestSnackCartRunWithinSodiumLimit(sodium3, maxSodium3);
Console.WriteLine($"Additional Example 3 Result: {result3}");

// Additional demo: entire array fits.
int[] sodium4 = { 10, 20, 30 };
int maxSodium4 = 100;
int result4 = solution.LongestSnackCartRunWithinSodiumLimit(sodium4, maxSodium4);
Console.WriteLine($"Additional Example 4 Result: {result4}");