/*
Title: Count Pairs of Packages Within a Weight Gap

Problem Description:
You are given an integer array weights where weights[i] is the weight of the ith package,
and two integers lowGap and highGap. A pair of packages (i, j) is considered compatible
if i < j and the absolute difference between their weights is between lowGap and highGap,
inclusive.

In other words, a pair is valid if:
    lowGap <= |weights[i] - weights[j]| <= highGap

Return the number of compatible pairs.

A straightforward O(n^2) solution checks every pair, but that is too slow for large inputs.
The intended efficient solution uses sorting together with a two-pointer counting strategy.

Important notes:
- The array is not initially sorted.
- Multiple packages may have the same weight.
- When lowGap = 0, equal-weight pairs may be valid.

Constraints:
- 1 <= weights.length <= 100000
- 0 <= weights[i] <= 1000000000
- 0 <= lowGap <= highGap <= 1000000000

Example 1:
Input: weights = [4, 1, 7, 3], lowGap = 2, highGap = 4
Output: 4

Explanation:
Valid pairs by weight are:
(4,1) diff 3
(4,7) diff 3
(1,3) diff 2
(7,3) diff 4

Example 2:
Input: weights = [5, 5, 8, 10], lowGap = 0, highGap = 3
Output: 5

Explanation:
Valid pairs are:
(5,5) diff 0
(first 5, 8) diff 3
(second 5, 8) diff 3
(first 8, 10) diff 2
and one more valid pair count comes from the second 5 with 8 already listed separately,
for a total of 4? Let's verify carefully by index pairs:

Indices:
0:5, 1:5, 2:8, 3:10

Pairs:
(0,1) diff 0 -> valid
(0,2) diff 3 -> valid
(0,3) diff 5 -> invalid
(1,2) diff 3 -> valid
(1,3) diff 5 -> invalid
(2,3) diff 2 -> valid

So the correct total for this input is 4.

The problem statement says 5, but the actual pair-by-pair verification shows 4.
The algorithm below returns the mathematically correct answer based on the definition.
*/

using System;

public class Solution
{
    /*
    Time Complexity:
    - Sorting the array takes O(n log n)
    - Each two-pointer counting pass takes O(n)
    - We perform two passes, so total is O(n log n)

    Space Complexity:
    - If we sort a cloned array, that requires O(n) extra space for the copy
    - Aside from that, the two-pointer logic uses O(1) extra working space

    Beginner-friendly idea:
    We will count:
        number of pairs with difference <= highGap
    minus
        number of pairs with difference < lowGap

    Why does that work?
    Because the pairs whose difference is in [lowGap, highGap] are exactly the pairs
    that are at most highGap, excluding those that are smaller than lowGap.

    Since we sort the array, for any pair i < j we know:
        weights[j] - weights[i] >= 0
    So absolute difference becomes simple subtraction in sorted order.
    */
    public long CountCompatiblePairs(int[] weights, int lowGap, int highGap)
    {
        // We create a copy so the original input is not modified.
        // This is often a good habit in interview/demo code unless mutation is acceptable.
        int[] sorted = (int[])weights.Clone();

        // Sorting is the key step that makes the two-pointer strategy possible.
        // Once sorted, differences behave monotonically:
        // if j moves right, sorted[j] - sorted[i] never decreases.
        Array.Sort(sorted);

        // Count all pairs whose difference is <= highGap.
        long atMostHigh = CountPairsWithDifferenceAtMost(sorted, highGap);

        // Count all pairs whose difference is < lowGap.
        // "difference < lowGap" is the same as "difference <= lowGap - 1".
        // We must be careful when lowGap == 0:
        // then there are no non-negative differences smaller than 0,
        // so that count should be 0.
        long belowLow = lowGap == 0 ? 0 : CountPairsWithDifferenceAtMost(sorted, lowGap - 1);

        // Subtracting gives exactly the number of pairs in the inclusive range [lowGap, highGap].
        return atMostHigh - belowLow;
    }

    /*
    This helper counts how many index pairs (i, j), with i < j, satisfy:
        sorted[j] - sorted[i] <= limit

    Precondition:
    - The array must already be sorted in non-decreasing order.

    Why this works:
    - For each left index i, we want to know how far right we can extend j
      while keeping the difference <= limit.
    - Because the array is sorted, once a certain j is too far, any larger j
      will also be too far.
    - This monotonic behavior allows a sliding window / two-pointer approach.

    Detailed intuition:
    - Maintain a right pointer j.
    - For each i, move j as far right as possible while the condition holds.
    - Then all indices from i+1 up to j-1 form valid pairs with i.
    - Count them, then move i forward.
    - Importantly, j never moves backward, which is why the whole pass is O(n).
    */
    private long CountPairsWithDifferenceAtMost(int[] sorted, int limit)
    {
        // If limit is negative, no pair can have a non-negative difference <= negative number.
        // This is a defensive check. In our main method we already avoid this case,
        // but keeping this makes the helper robust and easier to understand.
        if (limit < 0)
        {
            return 0;
        }

        int n = sorted.Length;

        // We use long for the answer because the number of pairs can be as large as:
        // n * (n - 1) / 2
        // For n = 100000, that is about 5,000,000,000, which does not fit in int.
        long count = 0;

        // j is the right boundary of our sliding window.
        // It represents the first index that is NOT guaranteed valid yet.
        // We start at 0 and will only move it forward.
        int j = 0;

        // Iterate over each possible left endpoint i.
        for (int i = 0; i < n; i++)
        {
            // Ensure j is always at least i + 1, because pairs require i < j.
            // This also handles the case where i catches up to j.
            if (j < i + 1)
            {
                j = i + 1;
            }

            // Expand j while the pair (i, j) is still valid.
            // We use long subtraction to be extra safe, although int would also be enough
            // for the given constraints. This avoids accidental overflow in more general settings.
            while (j < n && (long)sorted[j] - sorted[i] <= limit)
            {
                j++;
            }

            // At this point:
            // - All indices in [i + 1, j - 1] are valid with i
            // - j is either out of bounds or the first invalid index
            //
            // Therefore, the number of valid partners for i is:
            //     (j - 1) - (i + 1) + 1 = j - i - 1
            //
            // Example:
            // if i = 2 and j = 6, then valid partners are 3,4,5 => 3 values
            // and indeed 6 - 2 - 1 = 3
            count += j - i - 1;
        }

        return count;
    }
}

// Demo code

var solution = new Solution();

// Example 1
int[] weights1 = { 4, 1, 7, 3 };
int lowGap1 = 2;
int highGap1 = 4;
long result1 = solution.CountCompatiblePairs(weights1, lowGap1, highGap1);
Console.WriteLine("Example 1 Result: " + result1); // Expected: 4

// Example 2
int[] weights2 = { 5, 5, 8, 10 };
int lowGap2 = 0;
int highGap2 = 3;
long result2 = solution.CountCompatiblePairs(weights2, lowGap2, highGap2);
Console.WriteLine("Example 2 Result: " + result2); // Correct by pair verification: 4

// Additional demo cases

// All equal weights, lowGap = 0 means equal pairs are allowed.
int[] weights3 = { 2, 2, 2, 2 };
int lowGap3 = 0;
int highGap3 = 0;
long result3 = solution.CountCompatiblePairs(weights3, lowGap3, highGap3);
Console.WriteLine("All equal, exact zero gap: " + result3); // Expected: 6

// No valid pairs
int[] weights4 = { 1, 10, 20 };
int lowGap4 = 2;
int highGap4 = 5;
long result4 = solution.CountCompatiblePairs(weights4, lowGap4, highGap4);
Console.WriteLine("No valid pairs: " + result4); // Expected: 0

// Mixed values
int[] weights5 = { 1, 3, 6, 9, 12 };
int lowGap5 = 3;
int highGap5 = 6;
long result5 = solution.CountCompatiblePairs(weights5, lowGap5, highGap5);
Console.WriteLine("Mixed values result: " + result5);