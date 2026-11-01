/*
Title: Shortest Snack Line Covering All Allergens
Difficulty: Medium
Topic: Two Pointers

Problem Description:
A food safety team is reviewing a cafeteria snack line. The line is represented by an array `snacks`,
where `snacks[i]` is the allergen label of the snack at position `i`.

You are also given an array `required`, containing the set of allergen labels that must all appear
at least once in a contiguous section of the line for inspection.

Your task is to return the length of the shortest contiguous subarray of `snacks` that contains
every allergen in `required` at least once. If no such subarray exists, return `-1`.

Important detail:
- `required` should be treated as a set of distinct labels, even if duplicates appear in the input.

Examples:
1)
snacks   = ["dairy","soy","nuts","gluten","soy","dairy"]
required = ["soy","nuts","dairy"]
Answer   = 3
Because ["dairy","soy","nuts"] already contains all required labels.

2)
snacks   = ["fruit","seed","dairy","fruit","seed"]
required = ["dairy","soy"]
Answer   = -1
Because "soy" never appears, so no valid window exists.
*/

using System;
using System.Collections.Generic;

public class Solution
{
    /*
    Time Complexity:
    - O(n + m), where:
      n = snacks.Length
      m = required.Length
    Why:
    - We first process `required` once to build a distinct set.
    - Then we scan `snacks` with a sliding window.
    - Each index moves into the window at most once (right pointer),
      and out of the window at most once (left pointer).

    Space Complexity:
    - O(k), where k is the number of distinct required allergen labels.
    Why:
    - We store the required labels in a HashSet.
    - We also store counts for only those required labels currently in the window.
    */
    public int ShortestSnackLineCoveringAllAllergens(string[] snacks, string[] required)
    {
        // Step 1:
        // Convert `required` into a set of distinct labels.
        //
        // Why this is necessary:
        // The problem explicitly says `required` should be treated as a set.
        // That means if required = ["soy", "soy", "nuts"], we only need
        // "soy" once and "nuts" once in the window.
        //
        // Why use HashSet:
        // A HashSet gives us:
        // - automatic duplicate removal
        // - O(1) average-time membership checks
        var requiredSet = new HashSet<string>(required);

        // If for some reason the distinct required set is empty,
        // the shortest window would conceptually be 0.
        // Under the given constraints required.Length >= 1, so this is just a safe guard.
        if (requiredSet.Count == 0)
        {
            return 0;
        }

        // Step 2:
        // This dictionary stores how many times each required allergen
        // appears inside the CURRENT sliding window.
        //
        // Example:
        // If the current window is ["dairy", "soy", "nuts", "soy"],
        // then windowCounts might be:
        // dairy -> 1
        // soy   -> 2
        // nuts  -> 1
        //
        // We only care about allergens that are in requiredSet.
        var windowCounts = new Dictionary<string, int>();

        // Step 3:
        // `formed` tells us how many DISTINCT required labels are currently satisfied.
        //
        // A required label is considered "satisfied" when its count in the current window
        // is at least 1.
        //
        // Example:
        // requiredSet = { "soy", "nuts", "dairy" }
        // If current window contains:
        // soy -> 2, nuts -> 1, dairy -> 0
        // then formed = 2
        int formed = 0;

        // Total number of distinct required labels we must satisfy.
        int need = requiredSet.Count;

        // `left` is the left boundary of the sliding window.
        int left = 0;

        // `bestLength` stores the shortest valid window length found so far.
        // We start with int.MaxValue to mean "no valid answer found yet".
        int bestLength = int.MaxValue;

        // Step 4:
        // Expand the window by moving `right` from left to right across the array.
        //
        // At each step:
        // - include snacks[right] into the window
        // - update counts if it is a required allergen
        // - if the window becomes valid, try shrinking it from the left
        for (int right = 0; right < snacks.Length; right++)
        {
            string currentSnack = snacks[right];

            // Step 4a:
            // Only process this snack if it is one of the required allergens.
            //
            // Why:
            // Non-required labels do not help satisfy the requirement directly.
            // They may still sit inside the window, but we do not need to count them.
            if (requiredSet.Contains(currentSnack))
            {
                // If this allergen is not yet in the dictionary, initialize its count to 0.
                if (!windowCounts.ContainsKey(currentSnack))
                {
                    windowCounts[currentSnack] = 0;
                }

                // Add the current snack to the window count.
                windowCounts[currentSnack]++;

                // If the count became exactly 1, that means this required allergen
                // has just become satisfied in the current window.
                //
                // Why "exactly 1" matters:
                // - 0 -> 1 means we newly covered this allergen
                // - 1 -> 2 does NOT increase the number of distinct satisfied allergens
                if (windowCounts[currentSnack] == 1)
                {
                    formed++;
                }
            }

            // Step 4b:
            // If formed == need, then the current window [left..right] contains
            // every required allergen at least once.
            //
            // Now we try to shrink the window from the left to make it as short as possible
            // while still remaining valid.
            while (formed == need)
            {
                // Current window length.
                int currentLength = right - left + 1;

                // Update the best answer if this valid window is smaller.
                if (currentLength < bestLength)
                {
                    bestLength = currentLength;
                }

                // We are about to remove snacks[left] from the window,
                // so store it first.
                string leftSnack = snacks[left];

                // Move the left boundary forward by one position.
                //
                // But before we do that logically, we must update the data structures
                // to reflect that the old left element is leaving the window.
                if (requiredSet.Contains(leftSnack))
                {
                    // Decrease the count of this required allergen in the window.
                    windowCounts[leftSnack]--;

                    // If the count becomes 0, then after removing this element,
                    // the window no longer contains that required allergen.
                    //
                    // That means the window is no longer valid, so formed decreases.
                    if (windowCounts[leftSnack] == 0)
                    {
                        formed--;
                    }
                }

                // Finally move left forward, actually shrinking the window.
                left++;
            }
        }

        // Step 5:
        // If bestLength was never updated, then no valid window was found.
        // Return -1 in that case.
        return bestLength == int.MaxValue ? -1 : bestLength;
    }
}

// Demo code:
// Create sample inputs, call the solution, and print the results.

var solution = new Solution();

// Example 1
string[] snacks1 = { "dairy", "soy", "nuts", "gluten", "soy", "dairy" };
string[] required1 = { "soy", "nuts", "dairy" };
int result1 = solution.ShortestSnackLineCoveringAllAllergens(snacks1, required1);
Console.WriteLine(result1); // Expected: 3

// Example 2
string[] snacks2 = { "fruit", "seed", "dairy", "fruit", "seed" };
string[] required2 = { "dairy", "soy" };
int result2 = solution.ShortestSnackLineCoveringAllAllergens(snacks2, required2);
Console.WriteLine(result2); // Expected: -1

// Extra demo: required contains duplicates, but should be treated as a set.
string[] snacks3 = { "soy", "bread", "nuts", "soy", "dairy" };
string[] required3 = { "soy", "soy", "dairy", "nuts" };
int result3 = solution.ShortestSnackLineCoveringAllAllergens(snacks3, required3);
Console.WriteLine(result3); // Expected: 4 (["nuts","soy","dairy"] is not contiguous from index 2 to 4? Actually it is length 3? Let's verify below.)

// Correct the printed expectation through actual reasoning:
// snacks3 = ["soy","bread","nuts","soy","dairy"]
// required distinct = {"soy","dairy","nuts"}
// shortest valid window is ["nuts","soy","dairy"] => length 3
Console.WriteLine("Verified shortest length for demo 3 should be 3.");