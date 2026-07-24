/*
Minimum Cost to Plan Study Sessions with Topic Fatigue

Problem Description:
You are preparing a study plan for an upcoming exam. There are n chapters to study in a fixed order, where chapter i has a study time time[i] and belongs to a topic topic[i]. You must partition the chapters into contiguous study sessions. Each session is completed in one sitting.

The cost of a single session is defined as:

session_cost = max(time in the session) + fatigue_penalty

The fatigue_penalty for a session is the number of times the topic changes between consecutive chapters inside that session. For example, if the topics in a session are [2, 2, 5, 5, 3], then the fatigue_penalty is 2 because the topic changes at 2->5 and 5->3.

Your task is to compute the minimum total cost to finish all chapters by choosing where to split the sessions.

Since the chapters must be studied in order, every session must be a contiguous subarray, and every chapter must belong to exactly one session.

Return the minimum possible total cost.

Constraints:
- 1 <= n <= 1000
- 1 <= time[i] <= 10^6
- 1 <= topic[i] <= 10^6
- You may create as many sessions as you want, including one chapter per session or one session containing all chapters.
*/

using System;

public class Solution
{
    /*
    Time Complexity: O(n^2)
    Space Complexity: O(n)

    Explanation of complexity:
    - We use dynamic programming over prefixes.
    - For each ending position i, we try every possible starting position j of the last session.
    - While moving j backward, we maintain:
      1) the maximum study time inside the current session
      2) the number of topic changes inside the current session
    - That gives an O(n) inner loop for each of the n positions, so total O(n^2).
    */
    public long MinTotalCost(int[] time, int[] topic)
    {
        int n = time.Length;

        // dp[i] will store the minimum total cost needed to study the first i chapters.
        // Important indexing detail:
        // - dp[0] = 0 means studying zero chapters costs nothing.
        // - dp[1] means first chapter only
        // - dp[n] means all chapters
        //
        // We use long because:
        // - time[i] can be up to 1,000,000
        // - n can be up to 1000
        // - total cost can exceed int range in some combinations
        long[] dp = new long[n + 1];

        // Initialize all states to a very large number so we can safely minimize later.
        for (int i = 0; i <= n; i++)
        {
            dp[i] = long.MaxValue / 4;
        }

        // Base case:
        // No chapters studied => cost 0.
        dp[0] = 0;

        // We now compute dp[1], dp[2], ..., dp[n].
        // Each dp[i] considers all ways to choose the final session ending at chapter i - 1.
        for (int i = 1; i <= n; i++)
        {
            // We will expand the last session backward.
            // Suppose the last session is chapters [j..i-1] in 0-based indexing.
            //
            // As j moves from i-1 down to 0:
            // - maxTime tracks the maximum time in that session
            // - topicChanges tracks how many adjacent topic changes occur inside that session
            int maxTime = 0;
            int topicChanges = 0;

            // Try every possible starting point j for the last session.
            for (int j = i - 1; j >= 0; j--)
            {
                // Step 1: include chapter j into the current session [j..i-1].
                // We update the session maximum study time.
                //
                // Why necessary?
                // The session cost depends on the maximum time among all chapters in the session.
                if (time[j] > maxTime)
                {
                    maxTime = time[j];
                }

                // Step 2: update the fatigue penalty.
                //
                // When we extend the session backward from [j+1..i-1] to [j..i-1],
                // the only new adjacent pair introduced is (j, j+1).
                //
                // If topic[j] != topic[j+1], then the number of topic changes increases by 1.
                //
                // Why this works:
                // We are building the session incrementally from right to left.
                // Every time we add one new chapter at the front, only one new boundary appears.
                if (j < i - 1 && topic[j] != topic[j + 1])
                {
                    topicChanges++;
                }

                // Step 3: compute the cost if chapters [j..i-1] form the last session.
                //
                // Total cost =
                //   best cost for first j chapters
                //   + cost of session [j..i-1]
                //
                // Session cost = maxTime + topicChanges
                long candidate = dp[j] + maxTime + topicChanges;

                // Step 4: minimize dp[i].
                //
                // Why necessary?
                // There may be many valid ways to split the first i chapters.
                // We want the cheapest one.
                if (candidate < dp[i])
                {
                    dp[i] = candidate;
                }
            }
        }

        // dp[n] is the minimum cost to study all chapters.
        return dp[n];
    }
}

// Demo code

var solution = new Solution();

// Example 1
int[] time1 = { 3, 1, 4, 2 };
int[] topic1 = { 1, 1, 2, 2 };
long result1 = solution.MinTotalCost(time1, topic1);
Console.WriteLine(result1);

// Example 2
int[] time2 = { 5, 2, 6, 1, 3 };
int[] topic2 = { 7, 7, 7, 8, 7 };
long result2 = solution.MinTotalCost(time2, topic2);
Console.WriteLine(result2);

// Additional quick sanity checks
int[] time3 = { 10 };
int[] topic3 = { 42 };
Console.WriteLine(solution.MinTotalCost(time3, topic3));

int[] time4 = { 1, 2, 3 };
int[] topic4 = { 1, 2, 3 };
Console.WriteLine(solution.MinTotalCost(time4, topic4));