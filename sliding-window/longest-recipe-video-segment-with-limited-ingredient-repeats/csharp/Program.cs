/*
Title: Longest Recipe Video Segment With Limited Ingredient Repeats
Difficulty: Medium
Topic: Sliding Window

Problem Description:
You are given an array ingredients where ingredients[i] is the ingredient name mentioned at second i of a cooking video.
The editor wants to extract one contiguous segment of the video such that no ingredient is mentioned more than k times
inside that segment. Your task is to return the length of the longest valid segment.

A segment is valid if, for every distinct ingredient appearing in it, its frequency within the segment is at most k.
The segment must be contiguous, so you may only choose a continuous block of timestamps.

This problem models a common interview pattern: maintaining counts inside a moving window while expanding and shrinking
the boundaries efficiently.

Return an integer representing the maximum number of timestamps in any valid segment.

Constraints:
- 1 <= ingredients.length <= 200000
- 1 <= ingredients[i].length <= 20
- ingredients[i] consists of lowercase English letters
- 1 <= k <= ingredients.length

Example 1:
Input: ingredients = ["salt","pepper","salt","oil","salt","pepper"], k = 2
Output: 4
Explanation: One longest valid segment is ["pepper","salt","oil","salt"]. In this segment, "salt" appears 2 times,
and every other ingredient appears at most 1 time. Any longer segment would contain "salt" 3 times, which is not allowed.

Example 2:
Input: ingredients = ["egg","egg","milk","egg","milk","milk","flour"], k = 2
Output: 5
Explanation:
A valid longest segment is ["milk","egg","milk","flour"]? Length 4 only.
The actual best valid segment is ["egg","milk","egg","milk","flour"], which has "egg" twice, "milk" twice,
and "flour" once, so the answer is 5.
*/

using System;
using System.Collections.Generic;

public class Solution
{
    /*
    Time Complexity: O(n)
    - Each ingredient enters the sliding window once when the right pointer expands.
    - Each ingredient leaves the sliding window at most once when the left pointer shrinks.
    - Therefore, the total amount of work across the whole array is linear.

    Space Complexity: O(m)
    - We store frequencies in a hash map (Dictionary).
    - m is the number of distinct ingredient names currently tracked, at most the number of distinct values in the array.
    */
    public int LongestValidSegment(string[] ingredients, int k)
    {
        // This dictionary stores how many times each ingredient appears
        // inside the CURRENT sliding window [left..right].
        //
        // Why a Dictionary?
        // - Ingredient names are strings, not small integer indexes.
        // - We need fast insert/update/lookup by ingredient name.
        // - Dictionary gives average O(1) time for these operations.
        var frequency = new Dictionary<string, int>();

        // left marks the beginning of the current window.
        int left = 0;

        // best stores the maximum valid window length we have seen so far.
        int best = 0;

        // We expand the window one position at a time using right.
        for (int right = 0; right < ingredients.Length; right++)
        {
            string currentIngredient = ingredients[right];

            // Step 1: Include the new ingredient at position right into the window.
            //
            // We are growing the window from the right side, so we must update
            // the count of this ingredient in our frequency map.
            if (!frequency.ContainsKey(currentIngredient))
            {
                frequency[currentIngredient] = 0;
            }

            frequency[currentIngredient]++;

            // Step 2: If adding this ingredient caused its count to exceed k,
            // then the window is no longer valid.
            //
            // Important observation:
            // Before adding ingredients[right], the window was valid.
            // After adding it, only the count of currentIngredient could have become invalid.
            // No other ingredient's count increased.
            //
            // Therefore, we only need to shrink while currentIngredient has count > k.
            while (frequency[currentIngredient] > k)
            {
                string leftIngredient = ingredients[left];

                // Remove the ingredient at the left edge from the window,
                // because we are moving the left boundary one step to the right.
                frequency[leftIngredient]--;

                // Optional cleanup:
                // If a count becomes zero, removing it keeps the dictionary cleaner.
                // This is not required for correctness, but it is a nice maintenance step.
                if (frequency[leftIngredient] == 0)
                {
                    frequency.Remove(leftIngredient);
                }

                left++;
            }

            // Step 3: At this point, the window [left..right] is valid again.
            //
            // Why is it valid?
            // - We kept shrinking until the only possible violating ingredient
            //   (currentIngredient) no longer exceeds k.
            // - Since all other ingredient counts were already valid before,
            //   the whole window is now valid.
            int currentWindowLength = right - left + 1;

            // Step 4: Update the best answer if this valid window is larger
            // than any valid window we have seen before.
            if (currentWindowLength > best)
            {
                best = currentWindowLength;
            }
        }

        // After scanning the entire array, best contains the length
        // of the longest contiguous valid segment.
        return best;
    }
}

// Demo code

var solution = new Solution();

// Example 1
string[] ingredients1 = { "salt", "pepper", "salt", "oil", "salt", "pepper" };
int k1 = 2;
int result1 = solution.LongestValidSegment(ingredients1, k1);
Console.WriteLine("Example 1 Result: " + result1); // Expected: 4

// Example 2
string[] ingredients2 = { "egg", "egg", "milk", "egg", "milk", "milk", "flour" };
int k2 = 2;
int result2 = solution.LongestValidSegment(ingredients2, k2);
Console.WriteLine("Example 2 Result: " + result2); // Expected: 5

// Additional quick sanity checks
string[] ingredients3 = { "a" };
int k3 = 1;
int result3 = solution.LongestValidSegment(ingredients3, k3);
Console.WriteLine("Additional Test 1 Result: " + result3); // Expected: 1

string[] ingredients4 = { "x", "x", "x", "x" };
int k4 = 2;
int result4 = solution.LongestValidSegment(ingredients4, k4);
Console.WriteLine("Additional Test 2 Result: " + result4); // Expected: 2

string[] ingredients5 = { "onion", "garlic", "tomato", "onion", "garlic", "tomato" };
int k5 = 1;
int result5 = solution.LongestValidSegment(ingredients5, k5);
Console.WriteLine("Additional Test 3 Result: " + result5); // Expected: 3