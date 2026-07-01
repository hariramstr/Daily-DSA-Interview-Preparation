/*
Title: Maximum Audience Gain from One Schedule Insertion
Difficulty: Medium
Topic: Arrays

Problem Description:
You are given an array `viewers` where `viewers[i]` represents the expected audience size of the `i`-th show in a streaming platform's daily lineup. The platform may insert exactly one promotional show into the schedule. The inserted show has an audience value `promo`, and it can be placed at any position: before the first show, between any two consecutive shows, or after the last show.

After insertion, the platform evaluates the best contiguous block of shows to feature on its homepage. The score of a block is the sum of its audience values. Your task is to return the maximum possible homepage score after inserting the promotional show exactly once.

In other words, choose one insertion position for `promo`, then compute the maximum subarray sum of the new array, and maximize that value over all possible insertion positions.

A valid solution should be more efficient than trying every insertion explicitly.

Constraints:
- `1 <= viewers.length <= 2 * 10^5`
- `-10^4 <= viewers[i] <= 10^4`
- `-10^4 <= promo <= 10^4`
- The answer fits in a 32-bit signed integer.

Example 1:
Input: viewers = [4, -2, 3, -1], promo = 5
Output: 10
Explanation: Insert `5` between `4` and `-2`, producing [4, 5, -2, 3, -1].
The best contiguous block is [4, 5, -2, 3] with sum 10.

Example 2:
Input: viewers = [-3, -2, -4], promo = 6
Output: 6
Explanation: All original shows have negative audience values, so the best choice is to insert 6 anywhere
and feature only that single show. The maximum possible score is 6.
*/

using System;

public class Solution
{
    /*
    Time Complexity: O(n)
    Space Complexity: O(n)

    Beginner-friendly idea:
    We want the maximum subarray sum after inserting `promo` exactly once.

    A maximum subarray in the new array can be of two types:
    1. It does NOT include the inserted promo.
       - Then its value is just the original maximum subarray sum of `viewers`.
    2. It DOES include the inserted promo.
       - Then the chosen subarray looks like:
         [some suffix ending at the left side] + promo + [some prefix starting at the right side]

       If we insert promo between indices i and i+1, then:
       - the best part we can take from the left is the maximum subarray sum that ENDS at i
         (or 0 if taking nothing from the left is better)
       - the best part we can take from the right is the maximum subarray sum that STARTS at i+1
         (or 0 if taking nothing from the right is better)

       So for each insertion gap:
         candidate = max(0, bestEndingAtLeft) + promo + max(0, bestStartingAtRight)

    We compute:
    - endHere[i]   = maximum subarray sum ending exactly at i
    - startHere[i] = maximum subarray sum starting exactly at i
    - originalBest = maximum subarray sum in the original array

    Then we test all n+1 insertion positions efficiently.
    */
    public int MaxAudienceGainAfterInsertion(int[] viewers, int promo)
    {
        int n = viewers.Length;

        // This array will store:
        // endHere[i] = the maximum sum of any contiguous subarray that MUST end at index i.
        //
        // Why do we need this?
        // If we insert promo somewhere after index i, and we want the final chosen block
        // to include some shows from the left side, then the left contribution must be a
        // subarray that ends exactly at i (because it must connect directly to the inserted promo).
        int[] endHere = new int[n];

        // This array will store:
        // startHere[i] = the maximum sum of any contiguous subarray that MUST start at index i.
        //
        // Why do we need this?
        // If we insert promo before index i, and we want the final chosen block to include
        // some shows from the right side, then the right contribution must be a subarray
        // that starts exactly at i (so it connects directly to promo).
        int[] startHere = new int[n];

        // -----------------------------
        // Step 1: Compute endHere using Kadane-style DP from left to right.
        // -----------------------------
        //
        // Recurrence:
        // endHere[i] = max(viewers[i], endHere[i - 1] + viewers[i])
        //
        // Explanation:
        // A best subarray ending at i has only two possibilities:
        // - start fresh at i
        // - extend the best subarray that ended at i-1
        endHere[0] = viewers[0];

        // We also compute the original maximum subarray sum while building endHere.
        // This covers the case where the best final subarray does not include promo at all.
        int originalBest = endHere[0];

        for (int i = 1; i < n; i++)
        {
            endHere[i] = Math.Max(viewers[i], endHere[i - 1] + viewers[i]);
            originalBest = Math.Max(originalBest, endHere[i]);
        }

        // -----------------------------
        // Step 2: Compute startHere from right to left.
        // -----------------------------
        //
        // Recurrence:
        // startHere[i] = max(viewers[i], viewers[i] + startHere[i + 1])
        //
        // Explanation:
        // A best subarray starting at i has only two possibilities:
        // - take only viewers[i]
        // - extend into the best subarray that starts at i+1
        startHere[n - 1] = viewers[n - 1];

        for (int i = n - 2; i >= 0; i--)
        {
            startHere[i] = Math.Max(viewers[i], viewers[i] + startHere[i + 1]);
        }

        // -----------------------------
        // Step 3: Try every insertion position.
        // -----------------------------
        //
        // There are n+1 possible gaps:
        // gap 0: before index 0
        // gap 1: between 0 and 1
        // ...
        // gap n-1: between n-2 and n-1
        // gap n: after index n-1
        //
        // For a gap:
        // - left contribution comes from the best subarray ending at the element just before the gap
        // - right contribution comes from the best subarray starting at the element just after the gap
        //
        // IMPORTANT:
        // We use max(0, contribution) because we are allowed to ignore one side completely.
        // If the best left or right connected subarray is negative, including it would only hurt.
        int answer = originalBest;

        for (int gap = 0; gap <= n; gap++)
        {
            int leftContribution = 0;
            int rightContribution = 0;

            // If there is at least one element on the left side of the gap,
            // we may attach the best subarray ending at that left boundary.
            if (gap > 0)
            {
                leftContribution = Math.Max(0, endHere[gap - 1]);
            }

            // If there is at least one element on the right side of the gap,
            // we may attach the best subarray starting at that right boundary.
            if (gap < n)
            {
                rightContribution = Math.Max(0, startHere[gap]);
            }

            int candidateIncludingPromo = leftContribution + promo + rightContribution;

            answer = Math.Max(answer, candidateIncludingPromo);
        }

        return answer;
    }
}

// Demo code

var solution = new Solution();

// Example 1
int[] viewers1 = { 4, -2, 3, -1 };
int promo1 = 5;
int result1 = solution.MaxAudienceGainAfterInsertion(viewers1, promo1);
Console.WriteLine(result1); // Expected: 10

// Example 2
int[] viewers2 = { -3, -2, -4 };
int promo2 = 6;
int result2 = solution.MaxAudienceGainAfterInsertion(viewers2, promo2);
Console.WriteLine(result2); // Expected: 6

// Additional quick checks
int[] viewers3 = { 1, 2, 3 };
int promo3 = -10;
int result3 = solution.MaxAudienceGainAfterInsertion(viewers3, promo3);
Console.WriteLine(result3); // Best may ignore promo in chosen subarray, expected: 6

int[] viewers4 = { -1, 4, -2, 5 };
int promo4 = 3;
int result4 = solution.MaxAudienceGainAfterInsertion(viewers4, promo4);
Console.WriteLine(result4); // One strong possibility is 4 + 3 - 2 + 5 = 10