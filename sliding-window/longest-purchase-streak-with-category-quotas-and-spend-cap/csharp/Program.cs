/*
Title: Longest Purchase Streak With Category Quotas and Spend Cap
Difficulty: Hard
Topic: Sliding Window

Problem Description:
You are given a chronological list of purchases made by a customer during a promotional campaign.
Each purchase is represented by two arrays of equal length:
- cost[i] is the amount spent on the i-th purchase
- category[i] is the category label of that purchase

You are also given:
- an integer budget
- a dictionary quota where quota[c] specifies the minimum number of purchases from category c
  that must appear inside a valid streak

A purchase streak is any contiguous subarray of purchases.

A streak is considered eligible if:
1. The total cost of all purchases in the streak is at most budget.
2. For every category c listed in quota, the streak contains at least quota[c] purchases whose category is c.

Return the length of the longest eligible streak.
If no streak satisfies all quotas under the budget, return 0.

Constraints:
- 1 <= n == cost.length == category.length <= 2 * 10^5
- 1 <= cost[i] <= 10^9
- 1 <= budget <= 10^15
- 1 <= number of distinct categories in quota <= 2 * 10^5
- category[i] is a lowercase string of length between 1 and 20
- 1 <= quota[c] <= n
- Categories appearing in quota may or may not appear in the purchase list

Important note about the examples:
The written explanations in the prompt contain inconsistencies.
For example 1, the window [0..3] has categories:
[grocery, book, grocery, toy]
This satisfies grocery >= 2 and book >= 1, and its total cost is 10, so length 4 is valid.
No longer valid window exists under the budget, so the correct answer is 4.

For example 2:
cost = [2, 2, 2, 2, 2]
category = ["a", "b", "a", "c", "b"]
budget = 6
quota = {"a": 1, "b": 2}
Any valid window must contain both b's, so it must span indices [1..4] or [0..4].
Their costs are 8 and 10 respectively, both above budget.
Therefore the correct answer is 0.
*/

using System;
using System.Collections.Generic;

public class Solution
{
    /*
    Time Complexity:
    O(n + q)
    where:
    - n = number of purchases
    - q = number of categories in quota

    Why?
    - We first do a quick feasibility scan over the purchases: O(n)
    - Then we run a classic sliding window:
      each index moves from left to right at most once, so total pointer movement is O(n)
    - Dictionary operations are average O(1)

    Space Complexity:
    O(q)
    because we only need to store counts for categories that appear in the quota dictionary.
    */
    public int LongestEligibleStreak(long[] cost, string[] category, long budget, Dictionary<string, int> quota)
    {
        // -----------------------------
        // Step 0: Basic input validation
        // -----------------------------
        // The problem guarantees valid input sizes, but adding these checks makes the method safer
        // and easier to understand for learners.
        if (cost == null || category == null || quota == null)
        {
            return 0;
        }

        if (cost.Length != category.Length || cost.Length == 0)
        {
            return 0;
        }

        int n = cost.Length;

        // ------------------------------------------------------------
        // Step 1: Quick impossibility check based on total availability
        // ------------------------------------------------------------
        // Before we even start the sliding window, we can detect some impossible cases.
        //
        // Example:
        // If quota requires:
        //   "grocery" -> 3
        // but the entire purchase list only contains 2 groceries,
        // then no contiguous subarray can ever satisfy the quota.
        //
        // This early check is not strictly required for correctness,
        // but it is useful and makes the logic clearer.
        var totalSeen = new Dictionary<string, int>(quota.Count);
        foreach (var key in quota.Keys)
        {
            totalSeen[key] = 0;
        }

        for (int i = 0; i < n; i++)
        {
            string cat = category[i];
            if (quota.ContainsKey(cat))
            {
                totalSeen[cat]++;
            }
        }

        foreach (var kvp in quota)
        {
            string cat = kvp.Key;
            int required = kvp.Value;

            if (!totalSeen.TryGetValue(cat, out int present) || present < required)
            {
                return 0;
            }
        }

        // -------------------------------------------------------------------
        // Step 2: Prepare the sliding window bookkeeping for quota categories
        // -------------------------------------------------------------------
        // We only care about categories that appear in the quota dictionary.
        // Categories not mentioned in quota still contribute to cost and window length,
        // but they do not affect whether the quota condition is satisfied.
        //
        // windowCount[cat] = how many times category 'cat' appears in the current window
        var windowCount = new Dictionary<string, int>(quota.Count);
        foreach (var key in quota.Keys)
        {
            windowCount[key] = 0;
        }

        // --------------------------------------------------------------
        // Step 3: Track how many quota categories are currently satisfied
        // --------------------------------------------------------------
        // Suppose quota is:
        //   grocery -> 2
        //   book    -> 1
        //
        // Then totalRequiredCategories = 2
        //
        // We maintain:
        //   satisfiedCategories = number of quota categories whose current count
        //                         has reached or exceeded the required amount.
        //
        // This is much faster than checking every category on every step.
        int totalRequiredCategories = quota.Count;
        int satisfiedCategories = 0;

        // ---------------------------------------------------------
        // Step 4: Standard sliding window variables
        // ---------------------------------------------------------
        int left = 0;
        long currentCost = 0;
        int bestLength = 0;

        // ----------------------------------------------------------------------
        // Step 5: Expand the window by moving 'right' from left to right once
        // ----------------------------------------------------------------------
        // The current window is always [left .. right].
        for (int right = 0; right < n; right++)
        {
            // ---------------------------------------------------------
            // Add the new purchase at index 'right' into the window
            // ---------------------------------------------------------
            currentCost += cost[right];

            string rightCategory = category[right];

            // If this category matters for quota checking, update its count.
            if (quota.ContainsKey(rightCategory))
            {
                int before = windowCount[rightCategory];
                int after = before + 1;
                windowCount[rightCategory] = after;

                // We only increase satisfiedCategories at the exact moment
                // the count reaches the required quota.
                //
                // Example:
                // quota["grocery"] = 2
                // count goes 0 -> 1 : still not satisfied
                // count goes 1 -> 2 : now satisfied, increment by 1
                // count goes 2 -> 3 : already satisfied, do not increment again
                if (before < quota[rightCategory] && after >= quota[rightCategory])
                {
                    satisfiedCategories++;
                }
            }

            // -----------------------------------------------------------------
            // Step 6: Shrink from the left while the budget condition is broken
            // -----------------------------------------------------------------
            // The budget condition is:
            //   currentCost <= budget
            //
            // If currentCost > budget, the current window is too expensive.
            // Because all costs are positive, the only way to reduce total cost
            // is to move 'left' forward and remove purchases from the window.
            while (left <= right && currentCost > budget)
            {
                string leftCategory = category[left];

                // Remove the purchase at index 'left' from the cost sum first.
                currentCost -= cost[left];

                // If the removed category is part of the quota, update its count.
                if (quota.ContainsKey(leftCategory))
                {
                    int before = windowCount[leftCategory];
                    int after = before - 1;
                    windowCount[leftCategory] = after;

                    // If this category was previously satisfying its quota,
                    // but after removal it drops below the required amount,
                    // then the number of satisfied categories decreases by 1.
                    //
                    // Example:
                    // quota["book"] = 1
                    // count goes 1 -> 0 : no longer satisfied
                    if (before >= quota[leftCategory] && after < quota[leftCategory])
                    {
                        satisfiedCategories--;
                    }
                }

                left++;
            }

            // -------------------------------------------------------------------
            // Step 7: After budget repair, check whether all quotas are satisfied
            // -------------------------------------------------------------------
            // At this point:
            //   currentCost <= budget
            //
            // So the only remaining condition is whether every quota category
            // is satisfied inside the current window.
            //
            // If satisfiedCategories == totalRequiredCategories,
            // then this window is fully valid.
            if (satisfiedCategories == totalRequiredCategories)
            {
                int currentLength = right - left + 1;
                if (currentLength > bestLength)
                {
                    bestLength = currentLength;
                }
            }
        }

        // ---------------------------------------------------------
        // Step 8: Return the best valid window length we found
        // ---------------------------------------------------------
        return bestLength;
    }
}

// ---------------------------------------------------------
// Demo code
// ---------------------------------------------------------

var solution = new Solution();

// Example 1
long[] cost1 = { 4, 2, 3, 1, 2, 5, 1 };
string[] category1 = { "grocery", "book", "grocery", "toy", "book", "grocery", "toy" };
long budget1 = 10;
var quota1 = new Dictionary<string, int>
{
    ["grocery"] = 2,
    ["book"] = 1
};

int result1 = solution.LongestEligibleStreak(cost1, category1, budget1, quota1);
Console.WriteLine(result1); // Correct result: 4

// Example 2
long[] cost2 = { 2, 2, 2, 2, 2 };
string[] category2 = { "a", "b", "a", "c", "b" };
long budget2 = 6;
var quota2 = new Dictionary<string, int>
{
    ["a"] = 1,
    ["b"] = 2
};

int result2 = solution.LongestEligibleStreak(cost2, category2, budget2, quota2);
Console.WriteLine(result2); // Correct result: 0

// Additional demo
long[] cost3 = { 1, 3, 2, 1, 1, 2 };
string[] category3 = { "x", "y", "x", "z", "y", "x" };
long budget3 = 7;
var quota3 = new Dictionary<string, int>
{
    ["x"] = 2,
    ["y"] = 1
};

int result3 = solution.LongestEligibleStreak(cost3, category3, budget3, quota3);
Console.WriteLine(result3);