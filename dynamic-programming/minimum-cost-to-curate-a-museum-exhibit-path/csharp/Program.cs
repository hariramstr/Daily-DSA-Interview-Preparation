/*
Title: Minimum Cost to Curate a Museum Exhibit Path
Difficulty: Medium
Topic: Dynamic Programming

Problem Description:
A museum is preparing a guided exhibit path through a linear hallway of display rooms. The hallway has n rooms numbered from 0 to n - 1, and the guide must start in room 0 and finish in room n - 1. From room i, the guide may move to room i + 1 or skip ahead to room i + 2, as long as the destination room exists.

Each room i has a curation cost cost[i], representing the effort required to prepare that room for the tour if the guide enters it. However, some rooms are marked as premium rooms. If two premium rooms are visited consecutively in the tour path, an additional transition fee penalty is charged because the guide team must perform a special reset between them.

You are given:
- an integer array cost where cost[i] is the curation cost of room i,
- a binary array premium where premium[i] is 1 if room i is premium, otherwise 0,
- an integer penalty.

Return the minimum total cost to create a valid tour path from room 0 to room n - 1. The total cost includes the cost of every visited room, plus penalty for every consecutive pair of visited rooms in the chosen path where both rooms are premium.

A pair of visited rooms is consecutive if they are adjacent in the chosen path, even if their indices differ by 2 due to a skip.

Constraints:
- 2 <= n <= 100000
- cost.length == premium.length == n
- 0 <= cost[i] <= 100000
- premium[i] is either 0 or 1
- 0 <= penalty <= 100000

Example 1:
Input: cost = [4, 2, 7, 3, 5], premium = [1, 0, 1, 1, 0], penalty = 6
Output: 14

Example 2:
Input: cost = [1, 9, 2, 8, 1, 3], premium = [1, 1, 0, 1, 1, 1], penalty = 4
Output: 11
*/

using System;

public class Solution
{
    /*
    Time Complexity: O(n)
    Space Complexity: O(n)

    Why O(n)?
    - For each room i, we compute the minimum cost to reach it.
    - Each room only checks at most two previous rooms: i - 1 and i - 2.
    - So the total work grows linearly with the number of rooms.

    Why O(n) space?
    - We store a DP array where dp[i] means:
      "the minimum total cost required to reach room i as part of a valid path."
    - This array has one entry per room.
    */
    public long MinCostExhibitPath(int[] cost, int[] premium, int penalty)
    {
        // Step 1:
        // Read the number of rooms.
        // We will use this many times, so storing it in a variable makes the code cleaner.
        int n = cost.Length;

        // Step 2:
        // Create a DP array.
        //
        // Meaning of dp[i]:
        // dp[i] = minimum total cost to start at room 0 and arrive at room i,
        //         including:
        //         - the cost of every visited room on that chosen path
        //         - every premium-to-premium transition penalty that occurs
        //
        // We use long instead of int for safety.
        // Even though individual values fit in int, the total sum across many rooms
        // can become large. Using long avoids overflow issues.
        long[] dp = new long[n];

        // Step 3:
        // Base case for room 0.
        //
        // The guide must start in room 0, so room 0 is always visited.
        // Therefore, the minimum cost to be at room 0 is simply cost[0].
        //
        // There is no previous room yet, so no transition penalty can apply here.
        dp[0] = cost[0];

        // Step 4:
        // Handle room 1 separately.
        //
        // Since movement is only +1 or +2:
        // - To reach room 1, the only possible previous room is room 0.
        //
        // So the path is forced: 0 -> 1
        //
        // Total cost:
        // dp[0] + cost[1] + possible penalty between rooms 0 and 1
        if (n >= 2)
        {
            dp[1] = dp[0] + cost[1] + TransitionPenalty(0, 1, premium, penalty);
        }

        // Step 5:
        // Fill the DP table from room 2 up to room n - 1.
        //
        // For each room i, there are exactly two ways to arrive:
        // 1) from room i - 1
        // 2) from room i - 2
        //
        // We compute the total cost of both choices and keep the smaller one.
        //
        // This is the core dynamic programming idea:
        // the best path to i must come from the best path to one of its valid predecessors.
        for (int i = 2; i < n; i++)
        {
            // Option A: come from room i - 1
            //
            // If we already know the minimum cost to reach i - 1,
            // then to move into room i we must:
            // - pay the cost of room i
            // - possibly pay the premium transition penalty if both
            //   room i - 1 and room i are premium
            long fromPrevious = dp[i - 1] + cost[i] + TransitionPenalty(i - 1, i, premium, penalty);

            // Option B: come from room i - 2
            //
            // This represents skipping one room.
            // Even though the indices differ by 2, these two rooms become consecutive
            // in the chosen path, so the premium transition penalty still applies
            // if both are premium.
            long fromTwoBack = dp[i - 2] + cost[i] + TransitionPenalty(i - 2, i, premium, penalty);

            // Choose the cheaper of the two valid ways to arrive at room i.
            //
            // This works because:
            // - any valid path to i must end with either (i - 1) -> i or (i - 2) -> i
            // - dp[i - 1] and dp[i - 2] already store the minimum cost to reach those rooms
            // - so taking the minimum of these two complete options gives the true optimum for i
            dp[i] = Math.Min(fromPrevious, fromTwoBack);
        }

        // Step 6:
        // The problem asks for the minimum total cost to start at room 0
        // and finish at room n - 1.
        //
        // By definition, dp[n - 1] stores exactly that answer.
        return dp[n - 1];
    }

    private static int TransitionPenalty(int from, int to, int[] premium, int penalty)
    {
        // This helper method computes the extra fee for moving from one visited room
        // to the next visited room in the chosen path.
        //
        // The rule is:
        // - if both rooms are premium, add the given penalty
        // - otherwise add 0
        //
        // We keep this logic in a helper method because:
        // 1) it avoids repeating the same condition in multiple places
        // 2) it makes the DP transitions easier to read
        // 3) it clearly expresses the business rule of the problem
        return (premium[from] == 1 && premium[to] == 1) ? penalty : 0;
    }
}

// Demo code:
// Create sample inputs, call the solution, and print the results.

var solution = new Solution();

// Example 1
int[] cost1 = { 4, 2, 7, 3, 5 };
int[] premium1 = { 1, 0, 1, 1, 0 };
int penalty1 = 6;

long result1 = solution.MinCostExhibitPath(cost1, premium1, penalty1);
Console.WriteLine(result1); // Expected: 14

// Example 2
int[] cost2 = { 1, 9, 2, 8, 1, 3 };
int[] premium2 = { 1, 1, 0, 1, 1, 1 };
int penalty2 = 4;

long result2 = solution.MinCostExhibitPath(cost2, premium2, penalty2);
Console.WriteLine(result2); // Expected: 11