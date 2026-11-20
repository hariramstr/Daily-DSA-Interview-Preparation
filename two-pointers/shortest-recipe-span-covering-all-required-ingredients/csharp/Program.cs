/*
Title: Shortest Recipe Span Covering All Required Ingredients
Difficulty: Medium
Topic: Two Pointers

Problem Description:
A cooking platform stores each recipe as an ordered list of ingredient tokens, where the same ingredient may appear multiple times if it is used in different steps. You are given an array ingredients representing this token stream for one recipe, and another array required representing the ingredients that must all appear in a highlighted excerpt. Your task is to return the length of the shortest contiguous span of ingredients that contains every ingredient in required at least once. If no such span exists, return -1.

This problem models finding the smallest meaningful excerpt of a recipe that still includes all mandatory ingredients. The order inside the span does not matter, but the span must be contiguous in the original recipe. Duplicate values in ingredients are allowed. Assume required contains distinct ingredient names.

You should design an efficient solution using the two pointers / sliding window technique. A brute-force approach that checks all subarrays will be too slow for large inputs.

Constraints:
- 1 <= ingredients.length <= 200000
- 1 <= required.length <= 200000
- 1 <= required.length <= ingredients.length
- ingredients[i] and required[i] are non-empty strings of lowercase English letters
- required contains distinct strings
*/

using System;
using System.Collections.Generic;

public class Solution
{
    /*
    Time Complexity:
    - O(n + m), where:
      n = ingredients.Length
      m = required.Length
    - We build a set of required ingredients in O(m).
    - Then we move the right pointer across the ingredients array once.
    - The left pointer also only moves forward, at most n times total.
    - Because each pointer moves at most n times, the sliding window work is O(n).

    Space Complexity:
    - O(m)
    - We store:
      1) A HashSet containing all required ingredients
      2) A Dictionary counting how many times each required ingredient appears in the current window
    - Both structures are proportional to the number of required ingredients.
    */
    public int ShortestRecipeSpan(string[] ingredients, string[] required)
    {
        // Step 1:
        // Put all required ingredient names into a HashSet.
        //
        // Why do we use a HashSet?
        // - We need very fast membership checks: "Is this ingredient one of the required ones?"
        // - HashSet provides average O(1) lookup time.
        //
        // This is important because we will inspect every ingredient in the recipe stream,
        // and we do not want to scan the required array every time.
        var requiredSet = new HashSet<string>(required);

        // Step 2:
        // Create a dictionary to store how many times each required ingredient
        // appears inside the current sliding window [left..right].
        //
        // Why do we need counts instead of just a boolean?
        // - Because when we move the left pointer forward, we may remove an ingredient.
        // - If that ingredient still appears elsewhere in the window, the window is still valid.
        // - So we must know the exact count currently inside the window.
        var windowCounts = new Dictionary<string, int>();

        // Step 3:
        // "formed" tells us how many distinct required ingredients are currently satisfied
        // inside the window.
        //
        // A required ingredient is considered satisfied when its count in the window is at least 1.
        //
        // Since required contains distinct values, the total number we need to satisfy is:
        int need = requiredSet.Count;
        int formed = 0;

        // Step 4:
        // Initialize the left pointer of the sliding window.
        int left = 0;

        // Step 5:
        // Track the best (smallest) valid window length found so far.
        // We start with int.MaxValue to mean "no answer found yet".
        int bestLength = int.MaxValue;

        // Step 6:
        // Expand the window by moving the right pointer from left to right.
        //
        // The idea of sliding window:
        // - First, expand right until the window contains all required ingredients.
        // - Then, shrink from the left as much as possible while still keeping the window valid.
        // - Repeat this process for every right position.
        for (int right = 0; right < ingredients.Length; right++)
        {
            string currentIngredient = ingredients[right];

            // Step 6a:
            // Only process this ingredient if it is one of the required ones.
            //
            // Why ignore non-required ingredients in the counting structure?
            // - They do not help satisfy the requirement directly.
            // - They may still be inside the window, but we do not need to count them.
            if (requiredSet.Contains(currentIngredient))
            {
                // If this required ingredient is not yet in the dictionary, initialize its count to 0.
                if (!windowCounts.ContainsKey(currentIngredient))
                {
                    windowCounts[currentIngredient] = 0;
                }

                // Add the current ingredient into the window count.
                windowCounts[currentIngredient]++;

                // If the count became 1, that means this ingredient was previously missing
                // and is now newly satisfied in the current window.
                //
                // Example:
                // - Before adding "garlic", count was 0 (or absent)
                // - After adding, count is 1
                // - So we have satisfied one more required ingredient
                if (windowCounts[currentIngredient] == 1)
                {
                    formed++;
                }
            }

            // Step 7:
            // If formed == need, then the current window [left..right] contains
            // every required ingredient at least once.
            //
            // Now we try to shrink the window from the left to make it as short as possible
            // while still remaining valid.
            while (formed == need)
            {
                // Compute the current window length.
                int currentLength = right - left + 1;

                // Update the best answer if this valid window is smaller.
                if (currentLength < bestLength)
                {
                    bestLength = currentLength;
                }

                // We are about to move the left pointer forward,
                // so we must remove ingredients[left] from the window.
                string leftIngredient = ingredients[left];

                // Only required ingredients affect validity tracking.
                if (requiredSet.Contains(leftIngredient))
                {
                    // Decrease its count because it is leaving the window.
                    windowCounts[leftIngredient]--;

                    // If the count becomes 0, then this required ingredient is no longer present
                    // in the window at all, so the window stops being valid.
                    //
                    // That means we have one fewer satisfied required ingredient.
                    if (windowCounts[leftIngredient] == 0)
                    {
                        formed--;
                    }
                }

                // Finally move the left boundary forward by one position.
                left++;
            }
        }

        // Step 8:
        // If bestLength was never updated, then no valid window was found.
        // Return -1 in that case.
        //
        // Otherwise return the smallest valid window length discovered.
        return bestLength == int.MaxValue ? -1 : bestLength;
    }
}

// Demo code

var solution = new Solution();

// Example 1
string[] ingredients1 = { "salt", "oil", "garlic", "onion", "salt", "tomato", "basil" };
string[] required1 = { "garlic", "salt", "tomato" };
int result1 = solution.ShortestRecipeSpan(ingredients1, required1);
Console.WriteLine(result1); // Expected: 4

// Example 2
string[] ingredients2 = { "flour", "egg", "milk", "butter", "egg" };
string[] required2 = { "egg", "yeast" };
int result2 = solution.ShortestRecipeSpan(ingredients2, required2);
Console.WriteLine(result2); // Expected: -1

// Additional quick demo
string[] ingredients3 = { "a", "b", "c", "a", "b" };
string[] required3 = { "a", "c" };
int result3 = solution.ShortestRecipeSpan(ingredients3, required3);
Console.WriteLine(result3); // Expected: 2 ("c","a")