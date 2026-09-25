/*
Title: Minimum Initial Credit for Subscription Bursts
Difficulty: Hard
Topic: Binary Search

Problem Description:
A streaming platform processes a fixed sequence of billing events over several days. You are given an integer array transactions where transactions[i] represents the net credit change on day i: a positive value adds credit to the account, and a negative value consumes credit. The platform may optionally activate at most k emergency top-ups. Each top-up can be inserted immediately before any day and adds exactly x credits to the current balance. Multiple top-ups cannot be used on the same day, and unused top-ups are allowed.

Your task is to compute the minimum initial credit S such that, by choosing when to use at most k top-ups, the account balance never becomes negative at any point during the sequence.

In other words, starting with balance S, process the days from left to right. Before processing day i, if you still have available top-ups, you may add x once. After that, apply transactions[i]. The balance must remain at least 0 after every day.

Return the smallest possible S.

This is a decision/optimization problem intended to be solved with binary search on the answer. A candidate value S is feasible if there exists some strategy using at most k top-ups that keeps the running balance nonnegative for the entire array.

Constraints:
- 1 <= transactions.length <= 200000
- -1000000000 <= transactions[i] <= 1000000000
- 0 <= k <= 200000
- 1 <= x <= 1000000000
- The answer fits in a signed 64-bit integer.

Example 1:
Input: transactions = [-4, 3, -6, 2], k = 1, x = 5
Output: 2
Explanation: Start with S = 2. Day 1: balance becomes -2 unless we top up first, so use the top-up and get 7, then after -4 the balance is 3. Day 2: +3 => 6. Day 3: -6 => 0. Day 4: +2 => 2. It is impossible with S = 1.

Example 2:
Input: transactions = [-8, -2, 5, -7], k = 2, x = 4
Output: 6
Explanation: With S = 6, use a top-up before day 1: 10 -> 2 after processing -8. Use the second top-up before day 2: 6 -> 4 after processing -2. Then day 3 gives 9, and day 4 leaves 2. Any initial credit smaller than 6 will fail no matter where the two top-ups are placed.
*/

using System;

public class Solution
{
    /*
    Time Complexity:
    - Feasibility check: O(n), because we scan the array once.
    - Binary search over the answer: O(log R), where R is the numeric search range.
    - Total: O(n log R)

    Space Complexity:
    - O(1) extra space, because we only use a few variables.

    Beginner-friendly idea:
    1. We do not directly guess the exact minimum starting credit.
    2. Instead, we ask a yes/no question:
       "If the starting credit were S, could we survive all days using at most k top-ups?"
    3. If a certain S works, then any larger starting credit also works.
       That monotonic behavior is exactly what makes binary search possible.
    */
    public long MinimumInitialCredit(int[] transactions, int k, int x)
    {
        // We binary search the smallest feasible starting credit S.
        //
        // First, define a lower bound:
        // Starting credit cannot be negative, so 0 is always a valid lower bound.
        long left = 0;

        // Next, define an upper bound that is guaranteed to work.
        //
        // A very safe upper bound is:
        // sum of absolute values of all negative transactions.
        //
        // Why does this always work?
        // If we start with at least the total amount of all losses, then even if we ignore
        // all positive days and never use any top-up, we can still absorb every negative day.
        //
        // This is not necessarily tight, but it is guaranteed and fits in long.
        long right = 0;
        foreach (int value in transactions)
        {
            if (value < 0)
            {
                right += -(long)value;
            }
        }

        // Binary search for the minimum feasible S.
        while (left < right)
        {
            // Standard midpoint calculation that avoids overflow.
            long mid = left + (right - left) / 2;

            // If mid is enough to survive the whole sequence,
            // then maybe we can do even better with a smaller starting credit.
            if (CanSurvive(transactions, k, x, mid))
            {
                right = mid;
            }
            else
            {
                // If mid is not enough, we must increase the starting credit.
                left = mid + 1;
            }
        }

        // At the end, left == right and points to the smallest feasible answer.
        return left;
    }

    private bool CanSurvive(int[] transactions, int k, int x, long startCredit)
    {
        // This method answers:
        // "Is it possible to process all days without the balance ever going negative,
        //  starting from startCredit and using at most k top-ups?"
        //
        // The key greedy idea:
        // Before each day, if processing that day without a top-up would make the balance negative,
        // then we MUST use a top-up right now if possible.
        //
        // Why is this greedy choice correct?
        // - We are only allowed to insert a top-up immediately before a day.
        // - If the current day would fail without a top-up, delaying the top-up to a later day
        //   does not help, because we would already have gone negative now.
        // - Therefore, whenever a top-up is necessary to survive the current day, using it now
        //   is forced, not optional.
        //
        // This means the simulation is deterministic:
        // use a top-up exactly when needed, and never earlier.
        //
        // Why "never earlier"?
        // - Using a top-up earlier than necessary cannot create any new advantage.
        // - The balance evolution is linear, and a top-up simply adds x once.
        // - Saving a top-up until the first moment it becomes necessary is always at least as good.
        //
        // So this greedy simulation correctly decides feasibility.

        long balance = startCredit;
        int usedTopUps = 0;

        for (int i = 0; i < transactions.Length; i++)
        {
            long today = transactions[i];

            // Step 1:
            // Check whether we can survive today's transaction without using a top-up.
            //
            // If balance + today >= 0, then today's event is safe as-is.
            // If balance + today < 0, then after today's transaction we would go negative,
            // which is forbidden. In that case, we must try to use a top-up before today.
            if (balance + today < 0)
            {
                // If we have already used all allowed top-ups, then this starting credit fails.
                if (usedTopUps == k)
                {
                    return false;
                }

                // Use one top-up immediately before today.
                //
                // This increases the current balance by exactly x.
                balance += x;
                usedTopUps++;

                // After using the top-up, we still must verify that today's transaction is survivable.
                //
                // If even after adding x we still go negative, then there is no legal move left:
                // - We cannot use multiple top-ups on the same day.
                // - Therefore this candidate starting credit is impossible.
                if (balance + today < 0)
                {
                    return false;
                }
            }

            // Step 2:
            // Apply today's transaction.
            //
            // At this point we know the result will be nonnegative,
            // because either:
            // - it was already safe without a top-up, or
            // - we used a top-up and verified safety afterward.
            balance += today;
        }

        // If we finished all days without violating the rules, then startCredit is feasible.
        return true;
    }
}

// Demo code

var solution = new Solution();

// Example 1
int[] transactions1 = { -4, 3, -6, 2 };
int k1 = 1;
int x1 = 5;
long result1 = solution.MinimumInitialCredit(transactions1, k1, x1);
Console.WriteLine(result1); // Expected: 2

// Example 2
int[] transactions2 = { -8, -2, 5, -7 };
int k2 = 2;
int x2 = 4;
long result2 = solution.MinimumInitialCredit(transactions2, k2, x2);
Console.WriteLine(result2); // Expected: 6