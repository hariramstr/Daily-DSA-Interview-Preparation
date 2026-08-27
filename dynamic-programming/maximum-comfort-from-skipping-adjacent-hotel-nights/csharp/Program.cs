/*
Title: Maximum Comfort from Skipping Adjacent Hotel Nights

Problem Description:
You are planning a road trip with a list of hotel options, one for each night of the trip.
The i-th hotel gives you a comfort score represented by comfort[i].

Because moving luggage and checking in on back-to-back nights is too exhausting,
you are not allowed to stay in hotels on two adjacent nights.

You may choose any set of nights to book, as long as no two chosen nights are consecutive.

Return the maximum total comfort score you can get.

This is a classic dynamic programming problem:
for each night, you can either:
1. Skip that hotel and keep the best score from previous nights, or
2. Book it and add its comfort score to the best result that ends at least one night earlier.

Constraints:
- 1 <= comfort.length <= 100
- 0 <= comfort[i] <= 1000

Example 1:
Input: comfort = [6, 7, 1, 30, 8, 2, 4]
Output: 41
Explanation:
One optimal choice is nights with comfort 7, 30, and 4.
Their total is 41.
You cannot take 6 and 7 together because those nights are adjacent.

Example 2:
Input: comfort = [5, 1, 1, 5]
Output: 10
Explanation:
Choose the first and last nights.
The total comfort is 5 + 5 = 10.
*/

using System;

public class Solution
{
    /*
    Time Complexity: O(n)
    - We visit each hotel night exactly once.

    Space Complexity: O(1)
    - We do not build a full DP array.
    - We only keep track of the last two dynamic programming states.

    Beginner-friendly idea:
    Let dp[i] mean:
    "the maximum comfort we can get from nights 0..i"

    Transition:
    For each night i, we have two choices:
    1. Skip night i:
       Then our total stays the same as dp[i - 1]
    2. Take night i:
       Then we cannot take night i - 1, so total becomes comfort[i] + dp[i - 2]

    Therefore:
    dp[i] = max(dp[i - 1], comfort[i] + dp[i - 2])

    We optimize space by storing only:
    - prevTwo = dp[i - 2]
    - prevOne = dp[i - 1]
    */
    public int MaxComfort(int[] comfort)
    {
        // Since the constraints guarantee at least one element,
        // we do not need to handle an empty array for the problem itself.
        // Still, adding a defensive check makes the method safer and easier to reuse.
        if (comfort == null || comfort.Length == 0)
        {
            return 0;
        }

        // If there is only one night, the answer is simply that night's comfort.
        // There are no adjacency conflicts because there is only one choice.
        if (comfort.Length == 1)
        {
            return comfort[0];
        }

        // prevTwo will represent the best answer up to index i - 2.
        // At the start, before processing index 1, this corresponds to dp[0].
        // For dp[0], the best we can do is take the first hotel,
        // because comfort values are non-negative and there is only one night considered.
        int prevTwo = comfort[0];

        // prevOne will represent the best answer up to index i - 1.
        // For index 1, we compare:
        // - taking night 0
        // - taking night 1
        // We cannot take both because they are adjacent.
        int prevOne = Math.Max(comfort[0], comfort[1]);

        // Now we process from the third night onward (index 2).
        // At each step, we compute the best answer for the current prefix of nights.
        for (int i = 2; i < comfort.Length; i++)
        {
            // Option 1: Skip the current night.
            // If we skip it, the best total remains whatever was best up to the previous night.
            int skipCurrent = prevOne;

            // Option 2: Take the current night.
            // If we take it, we must skip the immediately previous night.
            // So we add the current comfort score to the best answer from two nights ago.
            int takeCurrent = comfort[i] + prevTwo;

            // The best answer for the current night range is the better of:
            // - skipping this night
            // - taking this night
            int current = Math.Max(skipCurrent, takeCurrent);

            // Move the DP window forward:
            // - The old prevOne becomes the new prevTwo
            // - The newly computed current becomes the new prevOne
            prevTwo = prevOne;
            prevOne = current;
        }

        // After processing all nights, prevOne holds the best answer
        // for the entire array.
        return prevOne;
    }
}

// Demo code:
// We create sample inputs from the problem statement,
// call the solution method,
// and print the results.

var solution = new Solution();

int[] comfort1 = { 6, 7, 1, 30, 8, 2, 4 };
int result1 = solution.MaxComfort(comfort1);
Console.WriteLine("Example 1 Result: " + result1); // Expected: 41

int[] comfort2 = { 5, 1, 1, 5 };
int result2 = solution.MaxComfort(comfort2);
Console.WriteLine("Example 2 Result: " + result2); // Expected: 10

// Additional quick sanity checks for learning/debugging:
int[] comfort3 = { 10 };
int result3 = solution.MaxComfort(comfort3);
Console.WriteLine("Single Night Result: " + result3); // Expected: 10

int[] comfort4 = { 2, 9 };
int result4 = solution.MaxComfort(comfort4);
Console.WriteLine("Two Nights Result: " + result4); // Expected: 9