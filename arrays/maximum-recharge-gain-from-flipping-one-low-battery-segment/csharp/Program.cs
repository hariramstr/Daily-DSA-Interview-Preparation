/*
Title: Maximum Recharge Gain from Flipping One Low-Battery Segment

Problem Description:
A mobile device records its battery trend over a day as a binary array `status`, where
`status[i] = 1` means the battery level was healthy during minute `i`, and
`status[i] = 0` means the device was in a low-battery state during minute `i`.

You are allowed to apply exactly one recovery patch to a single contiguous segment of the array.
The patch flips every value in that chosen segment:
- each `0` becomes `1`
- each `1` becomes `0`

Your task is to return the maximum possible number of healthy minutes after applying the patch exactly once.

Important:
Because the patch must be used exactly once, you must choose a non-empty subarray to flip,
even if doing so decreases the final number of healthy minutes.

Efficient solution idea:
- Count how many healthy minutes already exist (how many 1s are in the array).
- If we flip a position:
  - flipping 0 -> 1 gives a gain of +1 healthy minute
  - flipping 1 -> 0 gives a gain of -1 healthy minute
- So we transform the array into a "gain array":
  - 0 becomes +1
  - 1 becomes -1
- Then we need the contiguous segment with the maximum total gain.
- That is exactly the classic maximum subarray problem, solved in O(n) using Kadane's algorithm.
- Final answer = original count of 1s + best gain

Example 1:
status = [1,0,0,1,0,1]
Original healthy count = 3
Gain array = [-1,+1,+1,-1,+1,-1]
Maximum subarray gain = 2
Answer = 3 + 2 = 5

Example 2:
status = [1,1,1,1]
Original healthy count = 4
Gain array = [-1,-1,-1,-1]
Because we must flip exactly one non-empty segment, the best gain is -1
Answer = 4 + (-1) = 3
*/

using System;

public class Solution
{
    /*
    Time Complexity: O(n)
    - We scan through the array once.
    - Every element is processed in constant time.

    Space Complexity: O(1)
    - We only use a few integer variables.
    - No extra array or data structure proportional to input size is created.
    */
    public int MaxHealthyMinutesAfterOneFlip(int[] status)
    {
        // This variable stores how many healthy minutes already exist before any flip.
        // Since healthy minutes are represented by 1s, we count the number of 1s.
        int originalHealthyCount = 0;

        // We will use a Kadane-style running sum to find the maximum gain subarray.
        //
        // currentGain:
        //   The best gain of a subarray that MUST end at the current index.
        //
        // bestGain:
        //   The best gain of ANY subarray seen so far.
        //
        // Important detail:
        // Because the problem says we must flip exactly one NON-EMPTY segment,
        // we are NOT allowed to choose "flip nothing".
        // That means bestGain must come from at least one element.
        //
        // To enforce that correctly, we initialize using the first element rather than 0.
        // This avoids the common mistake of allowing an empty subarray with gain 0.
        int firstGain = status[0] == 0 ? 1 : -1;
        int currentGain = firstGain;
        int bestGain = firstGain;

        // Count the first element if it is healthy.
        if (status[0] == 1)
        {
            originalHealthyCount++;
        }

        // Process the rest of the array from left to right.
        for (int i = 1; i < status.Length; i++)
        {
            // Step 1: Count original healthy minutes.
            // This is necessary because our final answer is:
            // original number of 1s + best gain from one flip.
            if (status[i] == 1)
            {
                originalHealthyCount++;
            }

            // Step 2: Convert the current binary value into its "gain if flipped".
            //
            // Why this works:
            // - If status[i] is 0, flipping it turns it into 1, so we gain +1 healthy minute.
            // - If status[i] is 1, flipping it turns it into 0, so we lose -1 healthy minute.
            //
            // This transformation turns the original problem into:
            // "Find the contiguous subarray with the maximum sum."
            int gain = status[i] == 0 ? 1 : -1;

            // Step 3: Update the best subarray sum ending at this index.
            //
            // At index i, there are only two possibilities for the best subarray ending here:
            //
            // Option A: Start a brand-new subarray at i
            //   gain
            //
            // Option B: Extend the previous best subarray ending at i-1
            //   currentGain + gain
            //
            // We choose whichever is larger.
            //
            // Why this is valid:
            // If the previous running gain is hurting us, it is better to discard it
            // and start fresh from the current position.
            currentGain = Math.Max(gain, currentGain + gain);

            // Step 4: Update the global best gain seen anywhere so far.
            //
            // This keeps track of the best segment to flip among all segments examined.
            bestGain = Math.Max(bestGain, currentGain);
        }

        // Include the first element in the original healthy count if needed.
        // We already handled it before the loop, so nothing extra is needed here.

        // Final result:
        // Start with the original number of healthy minutes,
        // then add the best possible net gain from flipping exactly one segment.
        return originalHealthyCount + bestGain;
    }
}

// Demo code
var solution = new Solution();

// Example 1:
// status = [1,0,0,1,0,1]
// Original healthy count = 3
// Best gain = 2
// Expected answer = 5
int[] status1 = { 1, 0, 0, 1, 0, 1 };
int result1 = solution.MaxHealthyMinutesAfterOneFlip(status1);
Console.WriteLine($"Input: [{string.Join(",", status1)}]");
Console.WriteLine($"Output: {result1}");
Console.WriteLine("Expected: 5");
Console.WriteLine();

// Example 2:
// status = [1,1,1,1]
// Original healthy count = 4
// Best gain = -1 because we must flip exactly one non-empty segment
// Expected answer = 3
int[] status2 = { 1, 1, 1, 1 };
int result2 = solution.MaxHealthyMinutesAfterOneFlip(status2);
Console.WriteLine($"Input: [{string.Join(",", status2)}]");
Console.WriteLine($"Output: {result2}");
Console.WriteLine("Expected: 3");
Console.WriteLine();

// Additional quick checks

// All zeros: flipping the whole array is best.
int[] status3 = { 0, 0, 0, 0 };
int result3 = solution.MaxHealthyMinutesAfterOneFlip(status3);
Console.WriteLine($"Input: [{string.Join(",", status3)}]");
Console.WriteLine($"Output: {result3}");
Console.WriteLine("Expected: 4");
Console.WriteLine();

// Single element 0: must flip it, becomes 1.
int[] status4 = { 0 };
int result4 = solution.MaxHealthyMinutesAfterOneFlip(status4);
Console.WriteLine($"Input: [{string.Join(",", status4)}]");
Console.WriteLine($"Output: {result4}");
Console.WriteLine("Expected: 1");
Console.WriteLine();

// Single element 1: must flip it, becomes 0.
int[] status5 = { 1 };
int result5 = solution.MaxHealthyMinutesAfterOneFlip(status5);
Console.WriteLine($"Input: [{string.Join(",", status5)}]");
Console.WriteLine($"Output: {result5}");
Console.WriteLine("Expected: 0");