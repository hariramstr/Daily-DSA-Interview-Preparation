/*
Title: Maximum Revenue from Menu Bundles with Dish Reuse Fees

Problem Description:
A restaurant is preparing a fixed tasting menu over N evenings. On evening i, the chef must choose exactly one dish from two available options: standard[i] or premium[i]. If the chef serves the same dish type on consecutive evenings, customers feel less variety and a reuse fee is applied for that evening. Specifically, if the chef chooses the same type on both evening i - 1 and evening i, then revenue for evening i is reduced by fee[i].

You are given three integer arrays of length N:
- standard[i]: revenue earned if the standard dish is served on evening i
- premium[i]: revenue earned if the premium dish is served on evening i
- fee[i]: penalty applied only when the dish type on evening i matches the dish type chosen on evening i - 1

Return the maximum total revenue the restaurant can earn over all N evenings.

Notes:
- On the first evening, no reuse fee is ever applied.
- The reuse fee depends only on evening i, not on earlier history.
- Dish type means only standard or premium; the actual revenue values can be different each evening.

Examples:
1)
standard = [5, 6, 4, 7]
premium  = [8, 3, 9, 2]
fee      = [0, 4, 5, 3]
Correct output: 30
One optimal plan is premium, standard, premium, standard:
8 + 6 + 9 + 7 = 30
No consecutive evenings use the same type, so no fee is paid.

2)
standard = [10, 10, 1, 10]
premium  = [1, 1, 20, 1]
fee      = [0, 2, 8, 2]
Correct output: 48
One optimal plan is standard, standard, premium, standard:
10 + (10 - 2) + 20 + 10 = 48
The fee on evening 4 does not apply because evening 3 used premium.

Goal:
Design an efficient dynamic programming solution for large N.
*/

using System;

public class Solution
{
    /*
    Time Complexity:
    O(N)
    We process each evening exactly once, and each step does only constant work.

    Space Complexity:
    O(1)
    We do not need a full DP array. We only keep the best totals for the previous evening:
    - best total if previous evening ended with standard
    - best total if previous evening ended with premium

    Beginner-friendly idea:
    At each evening, the only thing that matters from the past is:
    "What is the best total revenue so far if yesterday's dish type was standard?"
    and
    "What is the best total revenue so far if yesterday's dish type was premium?"

    Why is that enough?
    Because today's fee depends only on whether today's type matches yesterday's type.
    So we never need the full history of all earlier choices.
    */
    public long MaxRevenue(int[] standard, int[] premium, int[] fee)
    {
        if (standard == null || premium == null || fee == null)
            throw new ArgumentNullException("Input arrays must not be null.");

        if (standard.Length != premium.Length || standard.Length != fee.Length)
            throw new ArgumentException("All input arrays must have the same length.");

        int n = standard.Length;
        if (n == 0)
            return 0;

        // -----------------------------
        // Base case: evening 0
        // -----------------------------
        // On the first evening, there is no previous evening.
        // Therefore, no reuse fee can ever apply on evening 0.
        //
        // dpStandard = best total revenue after processing evening 0
        //              if we choose STANDARD on evening 0
        //
        // dpPremium  = best total revenue after processing evening 0
        //              if we choose PREMIUM on evening 0
        //
        // Since there is only one evening so far, these are simply the revenues
        // of the chosen dishes on evening 0.
        long dpStandard = standard[0];
        long dpPremium = premium[0];

        // -----------------------------
        // Process evenings 1 to n - 1
        // -----------------------------
        for (int i = 1; i < n; i++)
        {
            // We now compute the best totals for evening i.
            //
            // There are two states we want to compute:
            // 1) nextStandard = best total if we choose STANDARD on evening i
            // 2) nextPremium  = best total if we choose PREMIUM on evening i
            //
            // To compute each one, we consider the two possible dish types from evening i - 1.

            // ==========================================================
            // Compute nextStandard
            // ==========================================================
            //
            // Option A: Yesterday was STANDARD, and today is also STANDARD.
            // Then we earn standard[i], but because the type is reused,
            // we must subtract fee[i].
            //
            // Total = dpStandard + standard[i] - fee[i]
            long comeFromStandardToStandard = dpStandard + standard[i] - fee[i];

            //
            // Option B: Yesterday was PREMIUM, and today is STANDARD.
            // The type changes, so there is NO fee today.
            //
            // Total = dpPremium + standard[i]
            long comeFromPremiumToStandard = dpPremium + standard[i];

            //
            // We want the best possible total among these two ways.
            long nextStandard = Math.Max(comeFromStandardToStandard, comeFromPremiumToStandard);

            // ==========================================================
            // Compute nextPremium
            // ==========================================================
            //
            // Option A: Yesterday was PREMIUM, and today is also PREMIUM.
            // Same type on consecutive evenings => pay fee[i].
            //
            // Total = dpPremium + premium[i] - fee[i]
            long comeFromPremiumToPremium = dpPremium + premium[i] - fee[i];

            //
            // Option B: Yesterday was STANDARD, and today is PREMIUM.
            // Type changes => no fee today.
            //
            // Total = dpStandard + premium[i]
            long comeFromStandardToPremium = dpStandard + premium[i];

            //
            // Again, keep the better of the two possibilities.
            long nextPremium = Math.Max(comeFromPremiumToPremium, comeFromStandardToPremium);

            // ==========================================================
            // Move to the next iteration
            // ==========================================================
            //
            // The values we just computed become the "previous evening" states
            // for the next loop iteration.
            dpStandard = nextStandard;
            dpPremium = nextPremium;
        }

        // After processing all evenings, the final answer is the better of:
        // - best total ending with standard
        // - best total ending with premium
        return Math.Max(dpStandard, dpPremium);
    }
}

// Demo code
var solution = new Solution();

// Example 1
int[] standard1 = { 5, 6, 4, 7 };
int[] premium1 = { 8, 3, 9, 2 };
int[] fee1 = { 0, 4, 5, 3 };
long result1 = solution.MaxRevenue(standard1, premium1, fee1);
Console.WriteLine(result1); // Expected: 30

// Example 2
int[] standard2 = { 10, 10, 1, 10 };
int[] premium2 = { 1, 1, 20, 1 };
int[] fee2 = { 0, 2, 8, 2 };
long result2 = solution.MaxRevenue(standard2, premium2, fee2);
Console.WriteLine(result2); // Expected: 48

// Additional quick sanity check
int[] standard3 = { 7 };
int[] premium3 = { 9 };
int[] fee3 = { 0 };
long result3 = solution.MaxRevenue(standard3, premium3, fee3);
Console.WriteLine(result3); // Expected: 9