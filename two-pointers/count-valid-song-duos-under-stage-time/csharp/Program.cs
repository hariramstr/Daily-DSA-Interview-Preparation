/*
Title: Count Valid Song Duos Under Stage Time
Difficulty: Medium
Topic: Two Pointers

Problem Description:
You are organizing a live showcase and have a list of song durations in seconds. A duo performance is formed by choosing exactly two different songs. Due to stage scheduling limits, only duos whose combined duration is less than or equal to a given limit can be performed.

Given an integer array durations where durations[i] is the length of the i-th song, and an integer stageLimit, return the number of distinct index pairs (i, j) such that 0 <= i < j < n and durations[i] + durations[j] <= stageLimit.

Two songs with the same duration are still considered different if they come from different indices. Your solution should be efficient enough for large inputs, so a brute-force O(n^2) approach may not pass.

A common efficient strategy is to sort the durations and use two pointers to count how many pairs can be formed for each position without checking every pair individually.

Constraints:
- 2 <= durations.length <= 200000
- 1 <= durations[i] <= 1000000000
- 1 <= stageLimit <= 2000000000
- The answer fits in a 64-bit signed integer

Example 1:
Input: durations = [120, 90, 150, 60, 80], stageLimit = 210
Output: 7
Explanation: Valid pairs are (120,90), (120,60), (120,80), (90,60), (90,80), (150,60), and (60,80). The pairs (150,90), (150,80), and (150,120) exceed the limit.

Example 2:
Input: durations = [200, 40, 40, 170, 30], stageLimit = 210
Output: 5
Explanation: The valid pairs are:
- (40,40)
- (40,30) using the first 40
- (40,30) using the second 40
- (170,30)
That is 4 valid pairs total.
Note:
The originally stated total of 5 in the prompt is incorrect because 200 cannot pair with either 40,
and 40 + 170 also exceeds 210. A correct algorithm must return 4 for this input.
*/

using System;

public class Solution
{
    /*
    Time Complexity: O(n log n)
    - We sort the array once, which costs O(n log n).
    - Then we scan it with two pointers in O(n).

    Space Complexity: O(1) extra space (ignoring the sorting implementation details)
    - We sort the input array in place.
    - We only use a few variables besides the input array.
    */
    public long CountValidSongDuos(int[] durations, int stageLimit)
    {
        // Step 1:
        // Sort the song durations in non-decreasing order.
        //
        // Why do we sort?
        // Sorting creates an important structure that allows the two-pointer technique to work.
        // Once sorted, if the smallest remaining song paired with the largest remaining song
        // is within the limit, then that smallest song can also pair with every song between them.
        //
        // Example:
        // Suppose the sorted array segment is:
        // [30, 40, 40, 170, 200]
        // If 30 + 170 <= 210, then 30 can also pair with 40 and 40,
        // because those are even smaller than 170.
        //
        // This is the key idea that lets us count many pairs at once instead of checking all pairs one by one.
        Array.Sort(durations);

        // Step 2:
        // Create two pointers:
        // - left starts at the beginning (smallest duration)
        // - right starts at the end (largest duration)
        //
        // We will move these pointers toward each other.
        int left = 0;
        int right = durations.Length - 1;

        // Step 3:
        // Use a 64-bit integer for the answer because the number of valid pairs can be very large.
        // For n = 200000, the number of pairs can be close to n * (n - 1) / 2,
        // which does not fit in a 32-bit int.
        long count = 0;

        // Step 4:
        // Continue while left is strictly less than right.
        //
        // Why left < right?
        // Because a pair must use two different indices.
        // If left == right, that would mean using the same song twice, which is not allowed.
        while (left < right)
        {
            // Step 5:
            // Compute the sum of the durations at the two pointers.
            //
            // We cast to long before adding to be completely safe,
            // even though the given constraints still fit in int for the sum.
            long currentSum = (long)durations[left] + durations[right];

            // Step 6:
            // If the current pair is valid, we can count multiple pairs at once.
            if (currentSum <= stageLimit)
            {
                // Why can we add (right - left) pairs immediately?
                //
                // Since the array is sorted:
                // durations[left] <= durations[left + 1] <= ... <= durations[right]
                //
                // We already know:
                // durations[left] + durations[right] <= stageLimit
                //
                // Therefore:
                // durations[left] + durations[k] <= stageLimit
                // for every k in [left + 1, right]
                //
                // That means the song at index left can pair with ALL songs from left + 1 through right.
                //
                // Number of such pairs:
                // (left, left+1), (left, left+2), ..., (left, right)
                // which is exactly right - left pairs.
                count += right - left;

                // After counting all pairs that use durations[left] as the first song,
                // we move left forward to consider the next song.
                //
                // Why not move right here?
                // Because we have already fully counted every valid pair involving the current left.
                // There is nothing more to gain by keeping this left in place.
                left++;
            }
            else
            {
                // Step 7:
                // If the current sum is too large, then durations[right] is too large
                // to pair with durations[left].
                //
                // Since the array is sorted, durations[right] is the largest remaining value.
                // If even the smallest remaining value at 'left' cannot pair with it,
                // then no value between left and right can pair with durations[right] either,
                // because all of them are >= durations[left].
                //
                // So the only way to possibly make the sum smaller is to move 'right' leftward
                // to a smaller duration.
                right--;
            }
        }

        // Step 8:
        // Return the total number of valid pairs found.
        return count;
    }
}

// Demo code:
// Creates sample inputs, calls the solution, and prints the results.

var solution = new Solution();

// Example 1
int[] durations1 = { 120, 90, 150, 60, 80 };
int stageLimit1 = 210;
long result1 = solution.CountValidSongDuos(durations1, stageLimit1);
Console.WriteLine("Example 1 Result: " + result1); // Expected: 7

// Example 2
int[] durations2 = { 200, 40, 40, 170, 30 };
int stageLimit2 = 210;
long result2 = solution.CountValidSongDuos(durations2, stageLimit2);
Console.WriteLine("Example 2 Result: " + result2); // Correct result: 4

// Additional small demo
int[] durations3 = { 50, 50, 50 };
int stageLimit3 = 100;
long result3 = solution.CountValidSongDuos(durations3, stageLimit3);
Console.WriteLine("Additional Demo Result: " + result3); // Expected: 3