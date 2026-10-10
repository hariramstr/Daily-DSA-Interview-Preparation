/*
Title: Count Repair Teams Within Arrival Spread
Difficulty: Medium
Topic: Two Pointers

Problem Description:
A facilities company is scheduling emergency repair teams for a large campus. You are given an integer array arrivalTimes where arrivalTimes[i] is the arrival time, in minutes, of the i-th team. You are also given an integer maxSpread. Two teams are considered compatible if the absolute difference between their arrival times is less than or equal to maxSpread.

Your task is to return the total number of distinct pairs of teams (i, j) such that i < j and the two teams are compatible.

Because the input may be unsorted and can be large, an efficient solution is required. A brute-force O(n^2) approach will be too slow for the largest cases. The intended solution uses sorting together with a two-pointer scan to count, for each right endpoint, how many earlier teams can pair with it while staying within the allowed spread.

Constraints:
- 1 <= arrivalTimes.length <= 200000
- 0 <= arrivalTimes[i] <= 1000000000
- 0 <= maxSpread <= 1000000000
- The answer may exceed 32-bit integer range, so use a 64-bit integer type where needed.

Example 1:
Input: arrivalTimes = [12, 5, 9, 14], maxSpread = 4
Output: 4
Explanation: After sorting, arrival times are [5, 9, 12, 14]. Compatible pairs are (5,9), (9,12), (9,14), and (12,14).

Example 2:
Input: arrivalTimes = [3, 3, 3, 10], maxSpread = 0
Output: 3
Explanation: Only teams with exactly the same arrival time can pair. The three teams arriving at time 3 form 3 distinct pairs.
*/

using System;

public class Solution
{
    /*
    Time Complexity:
    - Sorting the array takes O(n log n)
    - The two-pointer scan takes O(n) because each pointer only moves forward
    - Total: O(n log n)

    Space Complexity:
    - If we sort the input array in place, the extra algorithmic space is O(1)
    - Note: the underlying sorting implementation may use some internal stack space
    */
    public long CountCompatiblePairs(int[] arrivalTimes, int maxSpread)
    {
        // Defensive check:
        // If the array is null or has fewer than 2 teams, no pair can exist.
        // The problem constraints guarantee at least 1 element, but this makes the method safer.
        if (arrivalTimes == null || arrivalTimes.Length < 2)
        {
            return 0L;
        }

        // Step 1: Sort the arrival times.
        //
        // Why sorting helps:
        // In an unsorted array, checking which earlier teams are within maxSpread of a given team
        // is difficult without comparing against many elements.
        //
        // After sorting:
        // - arrivalTimes[left] <= arrivalTimes[left + 1] <= ... <= arrivalTimes[right]
        // - For a fixed "right" team, all compatible earlier teams will form one continuous block.
        // - This structure is exactly what makes the two-pointer technique efficient.
        Array.Sort(arrivalTimes);

        // This variable stores the final answer.
        // We use long because the number of valid pairs can be very large.
        // For example, if all 200,000 teams are compatible, the number of pairs is:
        // 200000 * 199999 / 2 = 19,999,900,000
        // which does not fit in a 32-bit int.
        long totalPairs = 0L;

        // "left" marks the earliest index in the current valid window.
        //
        // At any moment during the scan:
        // - We are considering a fixed "right" index
        // - We want the smallest left such that:
        //     arrivalTimes[right] - arrivalTimes[left] <= maxSpread
        //
        // Then every index from left to right - 1 can pair with right.
        int left = 0;

        // Step 2: Move "right" from left to right across the sorted array.
        //
        // For each right index, we determine how many earlier teams can pair with it.
        for (int right = 0; right < arrivalTimes.Length; right++)
        {
            // Step 3: Shrink the window from the left while the spread is too large.
            //
            // Because the array is sorted, arrivalTimes[right] is the largest value in the window.
            // So the absolute difference between arrivalTimes[right] and any earlier value
            // is simply:
            //     arrivalTimes[right] - arrivalTimes[earlier]
            //
            // If this difference is greater than maxSpread for the current left,
            // then left cannot pair with right, and neither can any even earlier index
            // (but there are none earlier than left in our current window).
            //
            // Therefore we move left forward until the window becomes valid again.
            while (arrivalTimes[right] - arrivalTimes[left] > maxSpread)
            {
                left++;
            }

            // Step 4: Count how many valid earlier teams can pair with "right".
            //
            // At this point, the window [left, right] satisfies:
            //     arrivalTimes[right] - arrivalTimes[left] <= maxSpread
            //
            // Since the array is sorted, every index between left and right also satisfies
            // the spread condition with right.
            //
            // The valid earlier indices are:
            //     left, left + 1, ..., right - 1
            //
            // Count of those indices:
            //     right - left
            //
            // We add that many pairs because each of those earlier teams forms one distinct pair
            // with the current team at index right.
            totalPairs += (right - left);
        }

        // Step 5: Return the total number of compatible pairs found.
        return totalPairs;
    }
}

// Demo code

var solution = new Solution();

// Example 1:
// arrivalTimes = [12, 5, 9, 14], maxSpread = 4
// After sorting: [5, 9, 12, 14]
// Valid pairs:
// (5,9)   difference 4
// (9,12)  difference 3
// (9,14)  difference 5? No, this would be invalid if using direct difference from sorted values.
// Let's verify carefully:
// The problem statement says output is 4 and lists (9,14), but 14 - 9 = 5, which is greater than 4.
// Therefore that listed pair is inconsistent with the stated rule.
// Under the actual rule "absolute difference <= maxSpread", the valid pairs are:
// (5,9), (9,12), (12,14) => total 3
//
// Since correctness is mandatory, we trust the rule and compute the true result from the definition.
int[] arrivalTimes1 = { 12, 5, 9, 14 };
int maxSpread1 = 4;
long result1 = solution.CountCompatiblePairs(arrivalTimes1, maxSpread1);
Console.WriteLine(result1);

// Example 2:
// arrivalTimes = [3, 3, 3, 10], maxSpread = 0
// Only equal arrival times can pair.
// The three 3's form C(3,2) = 3 pairs.
int[] arrivalTimes2 = { 3, 3, 3, 10 };
int maxSpread2 = 0;
long result2 = solution.CountCompatiblePairs(arrivalTimes2, maxSpread2);
Console.WriteLine(result2);

// Additional quick demo:
int[] arrivalTimes3 = { 1, 2, 3, 4, 5 };
int maxSpread3 = 2;
long result3 = solution.CountCompatiblePairs(arrivalTimes3, maxSpread3);
Console.WriteLine(result3);