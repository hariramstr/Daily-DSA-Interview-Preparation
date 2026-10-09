/*
Title: Count Compatible Mentor-Mentee Matches

Problem Description:
You are given two integer arrays, mentors and mentees, representing skill ratings of available mentors
and incoming mentees. A mentor can be paired with at most one mentee, and a mentee can be paired with
at most one mentor. A pair is considered compatible if:
1. mentor skill >= mentee required skill
2. mentor skill - mentee required skill <= maxGap

Your task is to return the maximum number of compatible mentor-mentee pairs that can be formed.

You may reorder the arrays in any way when forming pairs. The goal is to maximize the number of valid
one-to-one matches.

Examples:
1)
mentors = [6, 3, 8, 10]
mentees = [2, 5, 7, 9]
maxGap = 2
Output: 3

One optimal pairing:
(3,2), (6,5), (10,9)

2)
mentors = [4, 4, 6]
mentees = [3, 4, 5, 6]
maxGap = 0
Output: 2

Only equal skills are allowed when maxGap = 0, so valid pairs are:
(4,4) and (6,6)
*/

using System;

public class Solution
{
    /*
    Time Complexity:
    - Sorting mentors: O(m log m)
    - Sorting mentees: O(n log n)
    - Two-pointer scan: O(m + n)
    - Total: O(m log m + n log n)

    Space Complexity:
    - If sorting is considered in-place for the arrays passed in, the extra algorithmic space is O(1)
      excluding the internal stack/implementation details of the sort.
    - Practically, we use only a few integer variables in addition to the input arrays.
    */
    public int MaxCompatiblePairs(int[] mentors, int[] mentees, int maxGap)
    {
        // Step 1:
        // Sort both arrays in non-decreasing order.
        //
        // Why do we sort?
        // Because the problem asks for the maximum number of one-to-one valid matches,
        // and sorting allows us to process both groups from smallest skill upward.
        //
        // This is the key idea behind the two-pointer technique:
        // once both arrays are sorted, we can make locally optimal decisions that are also globally optimal.
        //
        // Intuition:
        // - Smaller mentees are easier to satisfy, so we try to match them first.
        // - Smaller mentors should be used as early as possible if they can help,
        //   because saving a small mentor for later usually does not help with larger mentees.
        Array.Sort(mentors);
        Array.Sort(mentees);

        // Pointer i will walk through mentors.
        int i = 0;

        // Pointer j will walk through mentees.
        int j = 0;

        // This will count how many valid pairs we successfully form.
        int pairs = 0;

        // Step 2:
        // Scan through both sorted arrays using two pointers.
        //
        // We continue while both pointers are still inside their arrays.
        // If either side is exhausted, no more pairs can be formed.
        while (i < mentors.Length && j < mentees.Length)
        {
            int mentor = mentors[i];
            int mentee = mentees[j];

            // For a pair to be valid, we need:
            // mentor >= mentee
            // mentor - mentee <= maxGap
            //
            // Equivalently, mentor must lie in the interval:
            // [mentee, mentee + maxGap]

            // Case 1:
            // The current mentor is TOO SMALL for the current mentee.
            //
            // That means mentor < mentee.
            // This mentor cannot help this mentee, and because mentees are sorted,
            // this mentor also cannot help any later mentee (later mentees require equal or greater skill).
            //
            // Therefore, this mentor is useless for all remaining mentees,
            // so the only sensible move is to discard this mentor and advance i.
            if (mentor < mentee)
            {
                i++;
            }
            // Case 2:
            // The current mentor is TOO LARGE relative to the allowed gap.
            //
            // That means mentor > mentee + maxGap.
            // This mentor cannot pair with this mentee because the skill difference is too big.
            //
            // Since mentors are sorted, this mentor and all later mentors are >= this mentor,
            // so they will also be too large for this same mentee.
            //
            // Therefore, this mentee can never be matched with any remaining mentor.
            // The only sensible move is to discard this mentee and advance j.
            else if (mentor > mentee + maxGap)
            {
                j++;
            }
            else
            {
                // Case 3:
                // The current mentor is within the valid range:
                // mentee <= mentor <= mentee + maxGap
                //
                // So we can form a valid pair.
                //
                // Why is it safe to pair them immediately?
                // Because:
                // - This is the smallest remaining mentor that can satisfy this mentee.
                // - Using a larger mentor instead would only make it harder to match future mentees.
                // - Greedily using the smallest valid mentor preserves as many options as possible.
                //
                // So we count the pair and move both pointers forward,
                // because each mentor and each mentee can be used at most once.
                pairs++;
                i++;
                j++;
            }
        }

        // Step 3:
        // Return the total number of valid pairs found.
        return pairs;
    }
}

// Demo code:
// Create sample inputs, call the solution, and print results.

var solution = new Solution();

// Example 1
int[] mentors1 = { 6, 3, 8, 10 };
int[] mentees1 = { 2, 5, 7, 9 };
int maxGap1 = 2;
int result1 = solution.MaxCompatiblePairs(mentors1, mentees1, maxGap1);
Console.WriteLine($"Example 1 Result: {result1}"); // Expected: 3

// Example 2
int[] mentors2 = { 4, 4, 6 };
int[] mentees2 = { 3, 4, 5, 6 };
int maxGap2 = 0;
int result2 = solution.MaxCompatiblePairs(mentors2, mentees2, maxGap2);
Console.WriteLine($"Example 2 Result: {result2}"); // Expected: 2

// Additional demo
int[] mentors3 = { 5, 7, 9 };
int[] mentees3 = { 4, 5, 8, 10 };
int maxGap3 = 1;
int result3 = solution.MaxCompatiblePairs(mentors3, mentees3, maxGap3);
Console.WriteLine($"Additional Demo Result: {result3}");