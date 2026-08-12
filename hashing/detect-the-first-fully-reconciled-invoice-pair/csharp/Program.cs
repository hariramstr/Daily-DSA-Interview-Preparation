/*
Title: Detect the First Fully Reconciled Invoice Pair

Problem Description:
You are given a list of invoice records in the order they were received by an accounting system.
Each record is represented as a pair [vendorId, amount].

Two records form a fully reconciled pair if:
- they belong to the same vendor, and
- their amounts sum to exactly 0

Your task is to return the earliest record index j such that record j completes at least one
fully reconciled pair with some earlier record i from the same vendor.

Return the pair of indices [i, j].
If no such pair exists, return [-1, -1].

Important details:
- Indices are 0-based
- A record cannot be paired with itself
- Multiple identical records may exist
- Each record is an independent entry
- We must detect the first completed reconciliation efficiently using hashing

Examples:
1)
records = [[7,100],[3,50],[7,-100],[7,100],[3,-20]]
Output: [0,2]

2)
records = [[5,40],[5,10],[8,-40],[5,-10],[8,40]]
Output: [1,3]
*/

using System;
using System.Collections.Generic;

public class Solution
{
    /*
    Time Complexity: O(n)
    - We scan the records exactly once from left to right.
    - Each dictionary lookup and insertion is O(1) on average.

    Space Complexity: O(n)
    - In the worst case, we may store information for many previously seen records.

    Beginner-friendly idea:
    We want the FIRST index j that completes a valid pair.
    That means we should scan from left to right.
    For each current record [vendorId, amount], we ask:
    "Have we already seen the same vendor with amount = -amount?"
    If yes, then the current record completes a pair immediately, and because we scan left to right,
    this is the earliest possible j. We return at once.

    To do this efficiently, we store previously seen records in a hash-based structure:
    - First key: vendorId
    - Second key: amount
    - Value: earliest index where that exact (vendorId, amount) appeared

    Why earliest index?
    If multiple earlier records could pair with the same j, the problem says any one is acceptable.
    Storing the earliest one is simple and deterministic.
    */
    public int[] FirstFullyReconciledPair(int[][] records)
    {
        // This dictionary groups information by vendor.
        // Key   -> vendorId
        // Value -> another dictionary that maps amount to the earliest index where that amount
        //          was seen for this vendor.
        //
        // Example:
        // seen[7][100] = 0 means:
        // "For vendor 7, amount 100 was first seen at index 0."
        var seen = new Dictionary<int, Dictionary<int, int>>();

        // We scan records from left to right because the problem asks for the earliest index j
        // that completes a pair.
        for (int j = 0; j < records.Length; j++)
        {
            // Read the current record.
            int vendorId = records[j][0];
            int amount = records[j][1];

            // A valid earlier partner must:
            // 1) have the same vendorId
            // 2) have amount = -currentAmount
            //
            // Example:
            // current amount = -100
            // needed earlier amount = 100
            int neededAmount = -amount;

            // Step 1:
            // Check whether we have seen this vendor before.
            //
            // Why necessary?
            // If the vendor has never appeared before, then no earlier record from the same vendor
            // can possibly exist, so no pair can be completed at this index.
            if (seen.TryGetValue(vendorId, out var amountToIndex))
            {
                // Step 2:
                // For this same vendor, check whether the opposite amount was seen earlier.
                //
                // Why necessary?
                // Two records reconcile only if their amounts sum to 0.
                // That means earlierAmount + currentAmount == 0
                // so earlierAmount must equal -currentAmount.
                if (amountToIndex.TryGetValue(neededAmount, out int i))
                {
                    // We found an earlier record i from the same vendor whose amount is the exact
                    // opposite of the current amount.
                    //
                    // Because we are scanning j from left to right, this is the FIRST time any pair
                    // has become complete. Therefore returning immediately is correct.
                    return new[] { i, j };
                }
            }
            else
            {
                // If this vendor has not been seen before, create a new inner dictionary for it.
                //
                // Why do we create it now?
                // We are about to store the current record so future records from the same vendor
                // can look it up quickly.
                amountToIndex = new Dictionary<int, int>();
                seen[vendorId] = amountToIndex;
            }

            // Step 3:
            // Store the current record for future matches.
            //
            // Important detail:
            // We only store the earliest index for each exact (vendorId, amount).
            //
            // Why earliest?
            // - It is enough to detect future pairs.
            // - If multiple earlier records have the same amount, any one is acceptable.
            // - Keeping the earliest one gives deterministic output.
            if (!amountToIndex.ContainsKey(amount))
            {
                amountToIndex[amount] = j;
            }

            // Then continue scanning the next record.
        }

        // If we finish the entire scan without finding any completed pair,
        // then no valid reconciliation exists.
        return new[] { -1, -1 };
    }
}

// -------------------------
// Demo code
// -------------------------

var solution = new Solution();

// Example 1:
// records = [[7,100],[3,50],[7,-100],[7,100],[3,-20]]
// Expected output: [0,2]
int[][] records1 =
{
    new[] { 7, 100 },
    new[] { 3, 50 },
    new[] { 7, -100 },
    new[] { 7, 100 },
    new[] { 3, -20 }
};

int[] result1 = solution.FirstFullyReconciledPair(records1);
Console.WriteLine($"Example 1 Result: [{result1[0]},{result1[1]}]");

// Example 2:
// records = [[5,40],[5,10],[8,-40],[5,-10],[8,40]]
// Expected output: [1,3]
int[][] records2 =
{
    new[] { 5, 40 },
    new[] { 5, 10 },
    new[] { 8, -40 },
    new[] { 5, -10 },
    new[] { 8, 40 }
};

int[] result2 = solution.FirstFullyReconciledPair(records2);
Console.WriteLine($"Example 2 Result: [{result2[0]},{result2[1]}]");

// Additional demo: no pair exists
int[][] records3 =
{
    new[] { 1, 25 },
    new[] { 2, -25 },
    new[] { 1, 30 },
    new[] { 2, 40 }
};

int[] result3 = solution.FirstFullyReconciledPair(records3);
Console.WriteLine($"Example 3 Result: [{result3[0]},{result3[1]}]");