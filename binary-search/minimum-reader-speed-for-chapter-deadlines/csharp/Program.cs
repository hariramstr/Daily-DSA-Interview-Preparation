/*
Title: Minimum Reader Speed for Chapter Deadlines
Difficulty: Medium
Topic: Binary Search

Problem Description:
You are given an array chapters where chapters[i] is the number of pages in the i-th chapter of an online course,
and an integer h representing the total number of hours available before all chapter deadlines pass.

A student must read the chapters in the given order, but within any single hour they can work on only one chapter.
If the student's reading speed is s pages per hour, then finishing a chapter with p pages takes ceil(p / s) whole hours.
The student may start the next chapter only after the current one is fully finished.

Return the minimum integer reading speed s such that the student can finish all chapters within h hours.

It is guaranteed that an answer exists.

This is a search problem over the answer space: a higher reading speed never increases the total time needed,
so the feasibility of a speed is monotonic.

Constraints:
- 1 <= chapters.length <= 100000
- 1 <= chapters[i] <= 1000000000
- chapters.length <= h <= 1000000000
- The answer fits in a 32-bit signed integer

Examples:
1)
Input: chapters = [120, 90, 150], h = 8
Correct minimum speed:
- speed 50 => ceil(120/50) + ceil(90/50) + ceil(150/50) = 3 + 2 + 3 = 8, valid
- speed 49 => 3 + 2 + 4 = 9, invalid
So the minimum valid speed is 50.

2)
Input: chapters = [300, 200, 400, 100], h = 10
Output: 100
Explanation:
At speed 100, total time = 3 + 2 + 4 + 1 = 10, valid.
Any smaller speed makes the total exceed 10.
*/

using System;

public class Solution
{
    /*
    Time Complexity:
    - O(n * log M)
      where n is the number of chapters and M is the maximum number of pages in any chapter.
      We binary search the answer from 1 to max(chapters), and for each candidate speed
      we scan all chapters once to compute required hours.

    Space Complexity:
    - O(1)
      We only use a few extra variables and do not allocate any additional data structures
      proportional to the input size.
    */
    public int MinReadingSpeed(int[] chapters, int h)
    {
        // Step 1:
        // We need to define the search range for the answer.
        //
        // Why this works:
        // - The minimum possible reading speed is 1 page per hour.
        // - The maximum necessary reading speed is the size of the largest chapter.
        //   At that speed, every chapter takes at most 1 hour, because:
        //   ceil(chapterPages / maxChapterPages) is always 1 for any non-zero chapter.
        //
        // Since the problem guarantees that an answer exists and h >= number of chapters,
        // searching in [1, maxChapter] is always enough.
        int left = 1;
        int right = 0;

        // We scan once to find the largest chapter size.
        // This gives us the upper bound of the binary search.
        foreach (int pages in chapters)
        {
            if (pages > right)
            {
                right = pages;
            }
        }

        // Step 2:
        // Perform binary search on the answer space.
        //
        // Important idea:
        // If a speed 's' is enough to finish within h hours, then any speed larger than 's'
        // is also enough. That means the feasibility function is monotonic:
        //
        // speed too small -> invalid
        // speed large enough -> valid
        //
        // This exact pattern is what binary search is designed for.
        while (left < right)
        {
            // We compute the middle speed carefully.
            // Using left + (right - left) / 2 avoids overflow in general.
            int mid = left + (right - left) / 2;

            // Step 3:
            // Check whether this candidate speed 'mid' is sufficient.
            //
            // We need the total number of hours required to read all chapters
            // if the student reads at 'mid' pages per hour.
            //
            // We use long for the running total because:
            // - chapters.length can be up to 100000
            // - each chapter can contribute many hours
            // - the sum could exceed int during intermediate computation
            long requiredHours = 0;

            foreach (int pages in chapters)
            {
                // For one chapter with 'pages' pages and speed 'mid',
                // the number of hours needed is ceil(pages / mid).
                //
                // Instead of using floating-point math, we use the integer formula:
                // ceil(a / b) = (a + b - 1) / b
                //
                // Why this is better:
                // - avoids precision issues
                // - faster and cleaner for integer problems
                requiredHours += (pages + (long)mid - 1) / mid;

                // Small optimization:
                // If we already exceeded h, there is no need to continue summing.
                // The speed is definitely not sufficient.
                if (requiredHours > h)
                {
                    break;
                }
            }

            // Step 4:
            // Use the feasibility result to shrink the search space.
            //
            // Case A: requiredHours <= h
            // This means speed 'mid' works.
            // But we are looking for the MINIMUM valid speed,
            // so we keep 'mid' in the search range and continue leftward.
            //
            // Case B: requiredHours > h
            // This means speed 'mid' is too slow.
            // So we must search strictly to the right of 'mid'.
            if (requiredHours <= h)
            {
                right = mid;
            }
            else
            {
                left = mid + 1;
            }
        }

        // Step 5:
        // When left == right, binary search has converged to the smallest valid speed.
        return left;
    }
}

// Demo code

var solution = new Solution();

// Example 1
int[] chapters1 = { 120, 90, 150 };
int h1 = 8;
int result1 = solution.MinReadingSpeed(chapters1, h1);
Console.WriteLine($"Example 1 Result: {result1}");

// Example 2
int[] chapters2 = { 300, 200, 400, 100 };
int h2 = 10;
int result2 = solution.MinReadingSpeed(chapters2, h2);
Console.WriteLine($"Example 2 Result: {result2}");

// Additional quick checks
int[] chapters3 = { 30 };
int h3 = 5;
int result3 = solution.MinReadingSpeed(chapters3, h3);
Console.WriteLine($"Additional Check 1 Result: {result3}");

int[] chapters4 = { 1, 1, 1, 1 };
int h4 = 4;
int result4 = solution.MinReadingSpeed(chapters4, h4);
Console.WriteLine($"Additional Check 2 Result: {result4}");