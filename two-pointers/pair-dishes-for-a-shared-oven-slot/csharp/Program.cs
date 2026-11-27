/*
Title: Pair Dishes for a Shared Oven Slot

Problem Description:
A catering team has a list of dish preparation times, where times[i] is the number of minutes needed to bake the i-th dish.
The team wants to load exactly two dishes into a shared oven slot. For safety and scheduling reasons, the combined baking
time of the two chosen dishes must be at least minTime and at most maxTime.

Your task is to return the number of distinct pairs of dishes (i, j) such that:
0 <= i < j < n
and
minTime <= times[i] + times[j] <= maxTime

Two dishes are considered distinct if they come from different indices, even if their baking times are equal.
You may reorder the array internally if needed, but the answer must count pairs from the original set of dishes.

Design an algorithm that is efficient for large inputs. A brute-force O(n^2) solution will be too slow under the given constraints.

Constraints:
- 2 <= times.length <= 2 * 10^5
- 1 <= times[i] <= 10^9
- 1 <= minTime <= maxTime <= 2 * 10^9
- The answer may be large, so use a 64-bit integer type where necessary.

Key idea:
Count how many pairs have sum <= maxTime.
Count how many pairs have sum <= minTime - 1.
Subtract the second count from the first count.
That leaves exactly the number of pairs whose sum is in [minTime, maxTime].
*/

using System;

public class Solution
{
    /*
    Time Complexity:
    - Sorting the array takes O(n log n)
    - Each two-pointer counting pass takes O(n)
    - We do two counting passes, so total is still O(n log n)

    Space Complexity:
    - O(n) if we clone the input array to avoid modifying the caller's array
    - The two-pointer logic itself uses O(1) extra space
    */
    public long CountValidPairs(int[] times, int minTime, int maxTime)
    {
        // We clone the input array so our sorting does not modify the original array supplied by the caller.
        // This is not strictly required by the problem, but it is often a good practice in demo/interview code
        // when the method should behave politely and avoid side effects unless mutation is intended.
        int[] sorted = (int[])times.Clone();

        // Sorting is the foundation of the efficient solution.
        // Why do we sort?
        // Because once the numbers are in non-decreasing order, we can use the two-pointer technique.
        // Two pointers let us count many valid pairs at once instead of checking every pair individually.
        Array.Sort(sorted);

        // We want:
        // number of pairs with sum in [minTime, maxTime]
        //
        // This is equal to:
        // number of pairs with sum <= maxTime
        // minus
        // number of pairs with sum <= minTime - 1
        //
        // This transformation is extremely useful because "count pairs with sum <= threshold"
        // can be solved efficiently with two pointers on a sorted array.
        long atMostMax = CountPairsWithSumAtMost(sorted, maxTime);
        long belowMin = CountPairsWithSumAtMost(sorted, minTime - 1L);

        return atMostMax - belowMin;
    }

    private long CountPairsWithSumAtMost(int[] sorted, long threshold)
    {
        // This method counts how many index pairs (i, j), with i < j, satisfy:
        // sorted[i] + sorted[j] <= threshold
        //
        // The array must already be sorted in non-decreasing order.

        int left = 0;
        int right = sorted.Length - 1;
        long count = 0;

        // We move the two pointers toward each other.
        // The invariant is:
        // - left starts at the smallest value
        // - right starts at the largest value
        //
        // At each step, we inspect the sum of the current outer pair.
        while (left < right)
        {
            long sum = (long)sorted[left] + sorted[right];

            if (sum <= threshold)
            {
                // Current step:
                // We found that sorted[left] + sorted[right] is within the threshold.
                //
                // Why is this powerful?
                // Because the array is sorted.
                // If sorted[left] + sorted[right] <= threshold,
                // then sorted[left] + sorted[k] <= threshold for every k between left+1 and right.
                //
                // Reason:
                // sorted[k] <= sorted[right] for all k <= right
                // so replacing sorted[right] with any smaller or equal value cannot increase the sum.
                //
                // Therefore, all pairs:
                // (left, left+1), (left, left+2), ..., (left, right)
                // are valid.
                //
                // How many such pairs are there?
                // right - left
                //
                // This is the key optimization:
                // instead of counting one pair, we count a whole block of pairs at once.
                count += right - left;

                // After counting all pairs that start with this 'left',
                // we move left forward to consider the next starting element.
                //
                // Why not move right here?
                // Because we have already counted every valid pair involving the current left
                // and any index up to right.
                left++;
            }
            else
            {
                // Current step:
                // The sum is too large, so the pair (left, right) is invalid.
                //
                // Why do we move 'right' leftward?
                // Because the array is sorted, and sorted[right] is currently the largest candidate.
                // If the sum is too large, keeping the same left and choosing an even larger right is impossible.
                // The only way to reduce the sum is to decrease the right pointer.
                //
                // We do NOT move left here, because increasing left would only make the sum larger or equal,
                // which would not help when the current sum is already too big.
                right--;
            }
        }

        return count;
    }
}

// ------------------------------------------------------------
// Demo code
// ------------------------------------------------------------

var solution = new Solution();

// Example 1
int[] times1 = { 2, 5, 3, 7, 4 };
int minTime1 = 7;
int maxTime1 = 9;
long result1 = solution.CountValidPairs(times1, minTime1, maxTime1);
Console.WriteLine("Example 1 Result: " + result1);

// Example 2
int[] times2 = { 1, 1, 2, 2, 3 };
int minTime2 = 3;
int maxTime2 = 4;
long result2 = solution.CountValidPairs(times2, minTime2, maxTime2);
Console.WriteLine("Example 2 Result: " + result2);

// Additional quick sanity checks
int[] times3 = { 10, 20, 30, 40 };
Console.WriteLine("Extra Test 1 Result: " + solution.CountValidPairs(times3, 50, 60)); // Expected 2: (10,40), (20,30)

int[] times4 = { 5, 5, 5, 5 };
Console.WriteLine("Extra Test 2 Result: " + solution.CountValidPairs(times4, 10, 10)); // Expected 6

int[] times5 = { 1, 1000000000 };
Console.WriteLine("Extra Test 3 Result: " + solution.CountValidPairs(times5, 1, 1000000001)); // Expected 1