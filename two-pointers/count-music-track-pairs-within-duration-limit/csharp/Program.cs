/*
Title: Count Music Track Pairs Within Duration Limit
Difficulty: Medium
Topic: Two Pointers

Problem Description:
You are given an integer array durations where durations[i] is the length of the i-th music track in seconds,
and an integer limit. A pair of distinct tracks (i, j) is considered playable in one short session if
i < j and durations[i] + durations[j] <= limit.

Return the total number of playable pairs.

The order of tracks in the input array does not matter for pairing, and each pair should be counted at most once.
You are not asked to list the pairs, only to count them efficiently.

A brute-force solution that checks every pair takes O(n^2) time and may be too slow for large inputs.
Think about how sorting the array can help you use two pointers to count many valid pairs at once.

Constraints:
- 1 <= durations.length <= 2 * 10^5
- 1 <= durations[i] <= 10^9
- 1 <= limit <= 2 * 10^9
- The answer may be large, so use a 64-bit integer type if needed.

Example 1:
Input: durations = [120, 90, 150, 60], limit = 210
Output: 4
Explanation: Valid pairs are (120,90), (120,60), (90,60), and (150,60). The pair (150,90) exceeds the limit.

Example 2:
Input: durations = [40, 40, 40, 100], limit = 80
Output: 3
Explanation: Any pair formed by two 40-second tracks is valid. There are 3 such pairs, and no pair involving 100 fits within the limit.
*/

using System;

public class Solution
{
    /*
    Time Complexity:
    - Sorting the array takes O(n log n)
    - The two-pointer scan takes O(n)
    - Total: O(n log n)

    Space Complexity:
    - If we sort the input array in place, the extra space used by the algorithm itself is O(1)
      (ignoring any internal implementation details of the sorting routine)
    */
    public long CountPlayablePairs(int[] durations, int limit)
    {
        // Step 1:
        // Sort the durations array in non-decreasing order.
        //
        // Why do we sort?
        // Sorting creates a very useful structure:
        // - Small values are on the left
        // - Large values are on the right
        //
        // Once sorted, we can use two pointers to efficiently decide:
        // - If the smallest remaining track and the largest remaining track fit together,
        //   then the smallest track will also fit with every track between them.
        // This is the key idea that lets us count many valid pairs at once instead of checking
        // every possible pair individually.
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
        // Use a 64-bit integer (long) for the answer.
        //
        // Why long instead of int?
        // Because the number of pairs can be very large.
        // For n = 200,000, the maximum number of pairs is n * (n - 1) / 2,
        // which is about 19,999,900,000 and does not fit in a 32-bit int.
        long count = 0;

        // Step 4:
        // Continue while left is strictly less than right.
        //
        // Why left < right?
        // Because a pair must use two distinct tracks.
        // If left == right, that would mean trying to pair a track with itself, which is not allowed.
        while (left < right)
        {
            // Step 5:
            // Compute the sum of the durations at the two pointers.
            //
            // We cast to long before adding to be extra safe, even though the constraints here
            // still fit within int when adding two durations. This is a good habit when dealing
            // with potentially large numeric values.
            long currentSum = (long)durations[left] + durations[right];

            // Step 6:
            // Check whether the current smallest + current largest track fits within the limit.
            if (currentSum <= limit)
            {
                // If durations[left] + durations[right] <= limit, then something very powerful is true:
                //
                // Since the array is sorted:
                // durations[left] <= durations[left + 1] <= ... <= durations[right]
                //
                // We already know durations[left] + durations[right] is valid.
                // Therefore durations[left] + durations[k] is also valid for every k in [left + 1, right],
                // because durations[k] <= durations[right].
                //
                // That means the track at index left can form a valid pair with ALL tracks from
                // left + 1 through right.
                //
                // Number of such pairs:
                // right - left
                //
                // Example:
                // If left = 0 and right = 3, then index 0 can pair with 1, 2, and 3 => 3 pairs.
                count += right - left;

                // After counting all pairs that use durations[left], we move left forward.
                //
                // Why move left?
                // Because we have already counted every valid pair involving this left element
                // with indices up to right. There is nothing more to gain by keeping it.
                left++;
            }
            else
            {
                // If durations[left] + durations[right] > limit, then this pair is too large.
                //
                // Since durations[right] is the largest remaining value, pairing it with durations[left]
                // already exceeds the limit. That means pairing durations[right] with any element to the right
                // of left would only be equal or larger, so those would also fail.
                //
                // Therefore, durations[right] cannot form a valid pair with the current left,
                // and it also cannot form a valid pair with any larger element.
                //
                // So the only sensible move is to decrease right, making the sum smaller.
                right--;
            }
        }

        // Step 7:
        // Return the total number of valid pairs found.
        return count;
    }
}

// Demo code:
// Create sample inputs, call the solution, and print the results.

var solution = new Solution();

// Example 1:
// durations = [120, 90, 150, 60], limit = 210
// After sorting: [60, 90, 120, 150]
//
// Trace:
// left=0 (60), right=3 (150), sum=210 <= 210
// -> count += 3 because 60 pairs with 90, 120, 150
// count = 3, left becomes 1
//
// left=1 (90), right=3 (150), sum=240 > 210
// -> right becomes 2
//
// left=1 (90), right=2 (120), sum=210 <= 210
// -> count += 1 because 90 pairs with 120
// count = 4, left becomes 2
//
// Stop because left == right
//
// Final answer = 4, which matches the problem statement.
int[] durations1 = { 120, 90, 150, 60 };
int limit1 = 210;
long result1 = solution.CountPlayablePairs(durations1, limit1);
Console.WriteLine($"Example 1 Result: {result1}");

// Example 2:
// durations = [40, 40, 40, 100], limit = 80
// After sorting: [40, 40, 40, 100]
//
// Trace:
// left=0 (40), right=3 (100), sum=140 > 80
// -> right becomes 2
//
// left=0 (40), right=2 (40), sum=80 <= 80
// -> count += 2 because index 0 pairs with indices 1 and 2
// count = 2, left becomes 1
//
// left=1 (40), right=2 (40), sum=80 <= 80
// -> count += 1 because index 1 pairs with index 2
// count = 3, left becomes 2
//
// Stop because left == right
//
// Final answer = 3, which matches the problem statement.
int[] durations2 = { 40, 40, 40, 100 };
int limit2 = 80;
long result2 = solution.CountPlayablePairs(durations2, limit2);
Console.WriteLine($"Example 2 Result: {result2}");

// Additional quick demo:
int[] durations3 = { 10, 20, 30, 40, 50 };
int limit3 = 60;
long result3 = solution.CountPlayablePairs(durations3, limit3);
Console.WriteLine($"Additional Demo Result: {result3}");