/*
Title: Maximum Tips from Non-Consecutive Deliveries
Difficulty: Easy
Topic: Dynamic Programming

Problem Description:
A courier works through a straight list of delivery requests for the day. The i-th request offers a tip given by tips[i]. However, accepting two consecutive requests is not allowed because each accepted delivery requires a cooldown period before the next nearby pickup. You may choose any set of requests as long as no two chosen requests are adjacent in the list.

Your task is to return the maximum total tip the courier can earn.

This is a classic decision process where, at each request, you either skip it or accept it and then skip the previous adjacent choice. An efficient solution should use dynamic programming to build the best answer for prefixes of the list.

Constraints:
- 1 <= tips.length <= 100000
- 0 <= tips[i] <= 10000
- The answer fits in a 32-bit signed integer

Example 1:
Input: tips = [5, 1, 2, 10, 6]
Output: 15
Explanation: The best choice is to accept requests with tips 5 and 10, for a total of 15. Choosing 5, 2, and 6 gives 13, which is smaller.

Example 2:
Input: tips = [4, 7, 3, 9]
Output: 16
Explanation: Accept the 2nd and 4th requests for tips 7 + 9 = 16. You cannot take 7 and 3 together because they are consecutive.

Return only the maximum total tip. If all tips are 0, the answer is 0.
*/

using System;
using System.Linq;

public class Solution
{
    /*
    Time Complexity: O(n)
    - We scan through the array exactly once.
    - Each position is processed in constant time.

    Space Complexity: O(1)
    - We do not need a full DP array.
    - We only keep track of the best answers for the previous two states.
    */
    public int MaxTotalTip(int[] tips)
    {
        // The input guarantees at least one element, but this guard makes the method safer
        // and easier to reuse in other contexts.
        if (tips == null || tips.Length == 0)
        {
            return 0;
        }

        // Dynamic Programming Idea:
        //
        // Let dp[i] mean:
        // "the maximum total tip we can earn from the first i + 1 requests (index 0 to i),
        // while never taking two adjacent requests."
        //
        // For each request at index i, we have exactly two choices:
        //
        // 1) Skip the current request:
        //    Then the best total remains whatever was best up to i - 1.
        //    That value is dp[i - 1].
        //
        // 2) Take the current request:
        //    Then we are NOT allowed to take request i - 1.
        //    So the best total becomes:
        //    tips[i] + dp[i - 2]
        //    (or just tips[i] if i - 2 does not exist)
        //
        // Therefore the recurrence is:
        // dp[i] = max(dp[i - 1], tips[i] + dp[i - 2])
        //
        // Instead of storing the entire dp array, we only need the previous two values:
        // - prevOne = dp[i - 1]
        // - prevTwo = dp[i - 2]
        //
        // This reduces space from O(n) to O(1).

        // prevTwo represents the best answer up to index i - 2.
        // At the beginning, before processing anything, this is 0.
        int prevTwo = 0;

        // prevOne represents the best answer up to index i - 1.
        // Before processing the first element, this is also 0.
        int prevOne = 0;

        // Process each delivery request from left to right.
        for (int i = 0; i < tips.Length; i++)
        {
            // Option 1: skip the current request.
            // If we skip it, the best total does not change from the previous best.
            int skipCurrent = prevOne;

            // Option 2: take the current request.
            // If we take it, we must add its tip to the best answer from two positions back,
            // because the immediately previous request cannot also be taken.
            int takeCurrent = tips[i] + prevTwo;

            // The best answer ending at this position is whichever choice gives more total tip.
            int currentBest = Math.Max(skipCurrent, takeCurrent);

            // Move the rolling DP window forward:
            //
            // Before moving:
            // - prevTwo = dp[i - 2]
            // - prevOne = dp[i - 1]
            //
            // After computing currentBest = dp[i], we update:
            // - prevTwo should become old prevOne
            // - prevOne should become currentBest
            prevTwo = prevOne;
            prevOne = currentBest;
        }

        // After the loop, prevOne holds the best answer for the entire array.
        return prevOne;
    }
}

// Demo code

var solution = new Solution();

// Example 1:
// tips = [5, 1, 2, 10, 6]
// Best choice is 5 + 10 = 15
int[] tips1 = { 5, 1, 2, 10, 6 };
int result1 = solution.MaxTotalTip(tips1);
Console.WriteLine(result1);

// Example 2:
// tips = [4, 7, 3, 9]
// Best choice is 7 + 9 = 16
int[] tips2 = { 4, 7, 3, 9 };
int result2 = solution.MaxTotalTip(tips2);
Console.WriteLine(result2);

// Additional demo:
// all zeros should return 0
int[] tips3 = { 0, 0, 0, 0 };
int result3 = solution.MaxTotalTip(tips3);
Console.WriteLine(result3);

// Additional demo:
// single request
int[] tips4 = { 8 };
int result4 = solution.MaxTotalTip(tips4);
Console.WriteLine(result4);

// Additional demo:
// choose non-consecutive larger values
int[] tips5 = { 2, 1, 4, 9, 2 };
int result5 = solution.MaxTotalTip(tips5);
Console.WriteLine(result5);