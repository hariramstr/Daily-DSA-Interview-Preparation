/*
Title: Longest Translation Draft With Terminology Budget
Difficulty: Hard
Topic: Sliding Window

Problem Description:
A localization team is reviewing a draft translation represented by an array `terms`,
where `terms[i]` is the terminology ID used in the `i`th sentence.

The team wants to select one contiguous block of sentences. A block is valid if it can
be made terminology-consistent using at most `k` rewrites.

For any chosen contiguous block:
- Let `f` be the highest frequency of any single terminology ID inside that block.
- The cleanup cost is `window_length - f`.

That means every sentence not using the dominant terminology would need to be rewritten.

We must return the maximum length of a contiguous block whose cleanup cost is at most `k`.

Constraints:
- 1 <= terms.length <= 2 * 10^5
- 1 <= terms[i] <= 10^9
- 0 <= k <= terms.length

Examples:
1)
terms = [4, 7, 7, 4, 7, 9, 7], k = 2
Output: 6

2)
terms = [1, 2, 3, 2, 2, 3, 3, 3, 2], k = 3
Output: 7
*/

using System;
using System.Collections.Generic;

public class Solution
{
    /*
    Time Complexity:
    O(n), where n is the number of elements in `terms`.

    Why O(n)?
    - The right pointer moves from left to right exactly once.
    - The left pointer also moves from left to right at most once per element.
    - Dictionary updates are O(1) average time.

    Space Complexity:
    O(m), where m is the number of distinct terminology IDs currently tracked.
    In the worst case, this can be O(n) if all values are different.
    */
    public int LongestValidBlock(int[] terms, int k)
    {
        // This dictionary stores the frequency of each terminology ID
        // inside the current sliding window.
        //
        // Key   -> terminology ID
        // Value -> how many times that terminology appears in the current window
        //
        // We use Dictionary<int, int> because:
        // - terminology IDs can be as large as 1e9
        // - they are not guaranteed to be small or continuous
        // - an array-based frequency table would be wasteful or impossible
        var frequency = new Dictionary<int, int>();

        // `left` is the starting index of the current window.
        int left = 0;

        // `maxFrequencyInWindow` stores the largest frequency of any single value
        // we have seen while expanding the current window.
        //
        // Important subtle point:
        // This value is allowed to be "stale" when the left side moves.
        // That means it may sometimes be larger than the true maximum frequency
        // in the current exact window.
        //
        // Why is that still okay?
        // Because this is a standard and correct optimization for this problem.
        // It never causes us to miss the correct answer, and it keeps the algorithm O(n).
        int maxFrequencyInWindow = 0;

        // `bestLength` stores the maximum valid window length found so far.
        int bestLength = 0;

        // We expand the window by moving `right` from 0 to the end of the array.
        for (int right = 0; right < terms.Length; right++)
        {
            // Step 1: Include the new element at index `right` into the window.
            int currentTerm = terms[right];

            // If this terminology ID has not been seen in the current dictionary yet,
            // initialize its count to 0 before incrementing.
            if (!frequency.ContainsKey(currentTerm))
            {
                frequency[currentTerm] = 0;
            }

            // Increase the count because `terms[right]` is now inside the window.
            frequency[currentTerm]++;

            // Step 2: Update the maximum frequency seen in the window.
            //
            // This tracks the highest count of any single terminology ID.
            // It is the key quantity because:
            // cleanup cost = window length - max frequency
            //
            // If one terminology appears many times, fewer rewrites are needed.
            maxFrequencyInWindow = Math.Max(maxFrequencyInWindow, frequency[currentTerm]);

            // Step 3: Check whether the current window is invalid.
            //
            // Current window length is:
            // right - left + 1
            //
            // Cleanup cost is:
            // window length - maxFrequencyInWindow
            //
            // If that cost is greater than k, we must shrink the window from the left.
            while ((right - left + 1) - maxFrequencyInWindow > k)
            {
                // The element at `left` is leaving the window.
                int leftTerm = terms[left];

                // Decrease its frequency because it is no longer inside the window.
                frequency[leftTerm]--;

                // Move the left boundary one step to the right.
                left++;

                // Notice:
                // We do NOT recompute `maxFrequencyInWindow` here.
                //
                // Recomputing it exactly would require scanning all dictionary values,
                // which could make the algorithm too slow in the worst case.
                //
                // Keeping it stale is safe for correctness in this classic sliding
                // window pattern. It may delay shrinking slightly, but it does not
                // prevent finding the true maximum valid answer.
            }

            // Step 4: At this point, the window is considered valid under the
            // sliding-window invariant used by this algorithm.
            //
            // So we can safely update the best answer.
            int currentWindowLength = right - left + 1;
            bestLength = Math.Max(bestLength, currentWindowLength);
        }

        // After processing all possible right endpoints, `bestLength` is the answer.
        return bestLength;
    }
}

// Demo code:
// We create the sample inputs from the problem statement,
// call the solution method, and print the results.

var solution = new Solution();

int[] terms1 = { 4, 7, 7, 4, 7, 9, 7 };
int k1 = 2;
int result1 = solution.LongestValidBlock(terms1, k1);
Console.WriteLine(result1); // Expected: 6

int[] terms2 = { 1, 2, 3, 2, 2, 3, 3, 3, 2 };
int k2 = 3;
int result2 = solution.LongestValidBlock(terms2, k2);
Console.WriteLine(result2); // Expected: 7