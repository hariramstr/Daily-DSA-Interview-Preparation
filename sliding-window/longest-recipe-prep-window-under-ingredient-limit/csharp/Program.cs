/*
Title: Longest Recipe Prep Window Under Ingredient Limit
Difficulty: Medium
Topic: Sliding Window

Problem Description:
A meal-planning app stores a chef's recipe schedule as an array of strings `recipes`,
where `recipes[i]` is the main ingredient category used by the `i`-th recipe prepared
that day. The chef wants to analyze the longest contiguous stretch of recipes that can
be cooked without making the pantry too diverse.

Given `recipes` and an integer `k`, return the length of the longest contiguous subarray
that contains at most `k` distinct ingredient categories.

In other words, you need to find the largest window [l, r] such that among
recipes[l], recipes[l+1], ..., recipes[r], there are no more than k different
category names.

This problem should be solved efficiently for large inputs, so solutions that check
every possible subarray will be too slow.

Constraints:
- 1 <= recipes.length <= 100000
- 1 <= recipes[i].length <= 20
- recipes[i] consists of lowercase English letters
- 1 <= k <= recipes.length

Example 1:
Input: recipes = ["dairy","grain","dairy","spice","grain","grain"], k = 2
Output: 3

Example 2:
Input: recipes = ["meat","meat","veg","veg","sauce","veg","veg"], k = 2
Output: 4
*/

using System;
using System.Collections.Generic;

public class Solution
{
    /*
    Time Complexity:
    - O(n), where n is the number of recipes.
    - Each recipe is added to the window once by the right pointer,
      and removed from the window at most once by the left pointer.

    Space Complexity:
    - O(k) in the typical sliding-window sense, or more precisely O(m),
      where m is the number of distinct recipe categories currently tracked
      in the dictionary. In the worst case this can be O(n) if many distinct
      strings appear over time, but at any valid window state it is at most k.
    */
    public int LengthOfLongestRecipeWindow(string[] recipes, int k)
    {
        // This dictionary stores:
        // key   -> ingredient category name
        // value -> how many times that category appears inside the current window
        //
        // Why do we need counts instead of just a set?
        // Because when we move the left side of the window forward, we need to know
        // whether removing one recipe completely removes that category from the window
        // or whether that category still appears elsewhere inside the window.
        var counts = new Dictionary<string, int>();

        // left marks the beginning of the current sliding window.
        int left = 0;

        // best stores the maximum valid window length we have seen so far.
        int best = 0;

        // We expand the window by moving right from 0 to recipes.Length - 1.
        for (int right = 0; right < recipes.Length; right++)
        {
            // Step 1: include recipes[right] into the current window.
            //
            // We are expanding the window to the right by one element.
            // That means this recipe category now belongs to the current subarray.
            string currentRecipe = recipes[right];

            if (!counts.ContainsKey(currentRecipe))
            {
                counts[currentRecipe] = 0;
            }

            counts[currentRecipe]++;

            // Step 2: if the window has become invalid, shrink it from the left.
            //
            // The rule says the window may contain AT MOST k distinct categories.
            // If counts.Count > k, then we currently have too many distinct categories,
            // so we must move left forward until the window becomes valid again.
            while (counts.Count > k)
            {
                // Identify the recipe category that is leaving the window.
                string leftRecipe = recipes[left];

                // Decrease its frequency because it is no longer inside the window.
                counts[leftRecipe]--;

                // If its frequency becomes zero, that means this category no longer
                // exists anywhere in the current window, so we remove it entirely
                // from the dictionary. This is important because counts.Count is how
                // we track the number of distinct categories.
                if (counts[leftRecipe] == 0)
                {
                    counts.Remove(leftRecipe);
                }

                // Move the left boundary forward by one position.
                left++;
            }

            // Step 3: at this point, the window [left, right] is guaranteed valid.
            //
            // Why?
            // Because the while-loop above keeps shrinking until the number of
            // distinct categories is no more than k.
            //
            // So now we can safely measure its length and compare it with the best
            // answer found so far.
            int currentWindowLength = right - left + 1;

            if (currentWindowLength > best)
            {
                best = currentWindowLength;
            }
        }

        // After scanning the entire array, best contains the maximum valid length.
        return best;
    }
}

// Demo code

var solution = new Solution();

// Example 1
string[] recipes1 = { "dairy", "grain", "dairy", "spice", "grain", "grain" };
int k1 = 2;
int result1 = solution.LengthOfLongestRecipeWindow(recipes1, k1);
Console.WriteLine(result1); // Expected: 3

// Example 2
string[] recipes2 = { "meat", "meat", "veg", "veg", "sauce", "veg", "veg" };
int k2 = 2;
int result2 = solution.LengthOfLongestRecipeWindow(recipes2, k2);
Console.WriteLine(result2); // Expected: 4

// Additional quick check
string[] recipes3 = { "fruit" };
int k3 = 1;
int result3 = solution.LengthOfLongestRecipeWindow(recipes3, k3);
Console.WriteLine(result3); // Expected: 1