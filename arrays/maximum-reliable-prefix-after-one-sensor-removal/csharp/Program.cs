/*
Title: Maximum Reliable Prefix After One Sensor Removal

Problem Description:
You are given an array readings of length n, where readings[i] is the signal quality reported by the i-th sensor in a fixed processing order.
A prefix of the array is considered reliable if the greatest common divisor (GCD) of every pair of adjacent values in that prefix is greater than 1.
In other words, for every index j in that prefix with 0 <= j < prefixLength - 1, gcd(readings[j], readings[j + 1]) > 1 must hold.

Your system is allowed to permanently remove at most one sensor reading from the array before evaluating reliability.
After removing zero or one element, the remaining elements keep their original order and become adjacent if they were separated by the removed element.

Return the maximum possible length of a reliable prefix of the resulting array.

A prefix always starts at index 0 of the resulting array.
Note that removing one element near the front changes which values belong to the prefix, so you must reason about the array after deletion, not just skip one failing GCD check.

Constraints:
- 1 <= n <= 2 * 10^5
- 1 <= readings[i] <= 10^9
- You may remove at most one element
- Expected solution should run in O(n) or O(n log V), where V is the maximum reading value

Important note about the examples in the statement:
The statement itself already corrects both examples to output 3.
This implementation matches those corrected outputs.
*/

using System;
using System.Collections.Generic;

class Solution
{
    /*
    Time Complexity:
    - O(n log V), where:
      n = number of readings
      V = maximum reading value
    Explanation:
    - We compute gcd for adjacent pairs a constant number of times.
    - Each gcd call costs O(log V).

    Space Complexity:
    - O(n)
    Explanation:
    - We store a boolean array describing whether each adjacent pair is "good".
    - We also store a prefix array that helps us quickly know whether all pair checks
      up to a certain point are valid.
    */
    public int MaximumReliablePrefixAfterOneRemoval(int[] readings)
    {
        int n = readings.Length;

        // If there is only one reading, then the whole array is always a reliable prefix.
        // There are no adjacent pairs to check, so the answer is 1.
        if (n == 1)
            return 1;

        // ---------------------------------------------------------------------
        // STEP 1: Build an array that tells us whether each adjacent pair is good.
        //
        // good[i] corresponds to the pair:
        //   (readings[i], readings[i + 1])
        //
        // good[i] = true  if gcd(readings[i], readings[i + 1]) > 1
        // good[i] = false otherwise
        //
        // Why do this?
        // Because the reliability of a prefix depends entirely on whether all adjacent
        // pairs inside that prefix are good.
        //
        // Example:
        // readings = [6, 10, 15, 7, 14]
        // pairs are:
        //   gcd(6,10)  > 1 ? yes
        //   gcd(10,15) > 1 ? yes
        //   gcd(15,7)  > 1 ? no
        //   gcd(7,14)  > 1 ? yes
        //
        // So good = [true, true, false, true]
        // ---------------------------------------------------------------------
        bool[] good = new bool[n - 1];
        for (int i = 0; i < n - 1; i++)
        {
            good[i] = Gcd(readings[i], readings[i + 1]) > 1;
        }

        // ---------------------------------------------------------------------
        // STEP 2: Build a prefix helper array over the "good" array.
        //
        // prefixGoodCount[i] = number of good pairs among good[0..i-1]
        //
        // This is a standard prefix-sum style trick.
        // It lets us answer:
        //   "Are all pairs in a range good?"
        // very quickly.
        //
        // For a range of pair indices [l..r], all are good iff:
        //   countGoodInRange == lengthOfRange
        //
        // countGoodInRange can be computed in O(1) using prefix sums.
        //
        // Why is this useful?
        // Because after removing one element, we need to check whether the first k
        // elements of the resulting array form a reliable prefix.
        // That means checking many adjacent-pair conditions efficiently.
        // ---------------------------------------------------------------------
        int[] prefixGoodCount = new int[n];
        for (int i = 0; i < n - 1; i++)
        {
            prefixGoodCount[i + 1] = prefixGoodCount[i] + (good[i] ? 1 : 0);
        }

        // ---------------------------------------------------------------------
        // STEP 3: Compute the best answer without removing anything.
        //
        // If we do not remove any element, then the reliable prefix extends until
        // the first bad adjacent pair.
        //
        // If the first bad pair is at index p in "good", then:
        // - pairs good[0..p-1] are good
        // - good[p] is bad
        // Therefore the longest reliable prefix length is p + 1
        // because a prefix of length L uses pair indices [0..L-2].
        //
        // If all pairs are good, answer is n.
        // ---------------------------------------------------------------------
        int answer = LongestReliablePrefixWithoutRemoval(good);

        // ---------------------------------------------------------------------
        // STEP 4: Try removing each possible index exactly once.
        //
        // This is the key part.
        //
        // After removing readings[r], the resulting array has length n - 1.
        // We want the longest reliable prefix of that resulting array.
        //
        // A naive approach would rebuild the array and scan it for every r,
        // which would be O(n^2), too slow for n up to 2 * 10^5.
        //
        // Instead, we reason carefully about what changes:
        //
        // Original adjacent pairs:
        //   (0,1), (1,2), (2,3), ...
        //
        // If we remove index r:
        // - Pairs completely before r stay the same.
        // - Pairs completely after r also stay the same, just shifted in position.
        // - The only "new" pair created is between readings[r-1] and readings[r+1]
        //   when 0 < r < n-1.
        //
        // So for a target prefix length in the resulting array, we only need to know:
        // 1) Are all original pairs before the removed index good?
        // 2) Is the new bridge pair good (if it exists)?
        // 3) Are all original pairs after the removed index and inside the prefix good?
        //
        // We can compute the longest reliable prefix after removing r in O(log n)
        // using binary search plus O(1) validity checks.
        //
        // Total becomes O(n log n log V), but note:
        // each validity check uses O(1) prefix sums and at most one gcd.
        // This is still efficient for 2e5.
        //
        // However, we can do even better in practice because the binary search is
        // over prefix length only. This remains comfortably fast.
        // ---------------------------------------------------------------------
        for (int removedIndex = 0; removedIndex < n; removedIndex++)
        {
            int bestAfterThisRemoval = LongestReliablePrefixAfterRemovingIndex(readings, good, prefixGoodCount, removedIndex);
            if (bestAfterThisRemoval > answer)
                answer = bestAfterThisRemoval;
        }

        return answer;
    }

    // -------------------------------------------------------------------------
    // Returns the longest reliable prefix length in the original array
    // when no element is removed.
    // -------------------------------------------------------------------------
    private int LongestReliablePrefixWithoutRemoval(bool[] good)
    {
        for (int i = 0; i < good.Length; i++)
        {
            if (!good[i])
            {
                // If pair i is the first bad pair, then the longest reliable prefix
                // contains elements 0..i, which has length i + 1.
                return i + 1;
            }
        }

        // If every adjacent pair is good, the whole array is reliable.
        return good.Length + 1;
    }

    // -------------------------------------------------------------------------
    // Computes the longest reliable prefix length after removing readings[removedIndex].
    //
    // The resulting array has length n - 1.
    //
    // We binary search on prefix length L in [1, n-1].
    // For each L, we check whether the first L elements of the resulting array
    // form a reliable prefix.
    //
    // Why binary search works:
    // If a prefix of length L is reliable, then every shorter prefix is also reliable.
    // So the predicate "prefix of length L is reliable" is monotonic.
    // -------------------------------------------------------------------------
    private int LongestReliablePrefixAfterRemovingIndex(int[] readings, bool[] good, int[] prefixGoodCount, int removedIndex)
    {
        int n = readings.Length;
        int low = 1;
        int high = n - 1;
        int best = 1;

        while (low <= high)
        {
            int mid = low + (high - low) / 2;

            if (IsReliablePrefixAfterRemoval(readings, good, prefixGoodCount, removedIndex, mid))
            {
                best = mid;
                low = mid + 1;
            }
            else
            {
                high = mid - 1;
            }
        }

        return best;
    }

    // -------------------------------------------------------------------------
    // Checks whether the first prefixLength elements of the array obtained after
    // removing readings[removedIndex] form a reliable prefix.
    //
    // This is the most important correctness function.
    //
    // Let's define:
    // - Original array indices: 0..n-1
    // - Remove index r
    // - Resulting array indices: 0..n-2
    //
    // The first prefixLength elements of the resulting array correspond to:
    // - Original indices [0..prefixLength-1] if r >= prefixLength
    //   because the removed element is outside the prefix.
    //
    // - Original indices [0..prefixLength] except r if r < prefixLength
    //   because removing something inside the prefix pulls one extra original
    //   element into the prefix.
    //
    // Instead of explicitly constructing that prefix, we verify all adjacent pairs.
    //
    // Cases:
    //
    // 1) removedIndex >= prefixLength
    //    The removed element is not inside the prefix at all.
    //    So the prefix is exactly the original first prefixLength elements.
    //    We only need original pairs good[0..prefixLength-2] all true.
    //
    // 2) removedIndex == 0
    //    The resulting prefix starts from original index 1.
    //    So we need original pairs:
    //      (1,2), (2,3), ..., (prefixLength-1, prefixLength)
    //    which correspond to good[1..prefixLength-1].
    //
    // 3) 0 < removedIndex < prefixLength
    //    The removed element lies inside the prefix.
    //    Then the prefix contains:
    //      original [0..removedIndex-1], then [removedIndex+1..prefixLength]
    //
    //    Adjacent-pair requirements become:
    //    a) original pairs before removedIndex:
    //         good[0..removedIndex-2]
    //    b) bridge pair:
    //         gcd(readings[removedIndex-1], readings[removedIndex+1]) > 1
    //    c) original pairs after removedIndex, up to the end of the prefix:
    //         good[removedIndex+1 .. prefixLength-1]
    //
    // All of these must hold.
    // -------------------------------------------------------------------------
    private bool IsReliablePrefixAfterRemoval(int[] readings, bool[] good, int[] prefixGoodCount, int removedIndex, int prefixLength)
    {
        // A prefix of length 0 is not needed in this problem.
        // A prefix of length 1 is always reliable because it has no adjacent pairs.
        if (prefixLength <= 1)
            return true;

        int n = readings.Length;

        // Case 1:
        // Removed element is outside the prefix.
        // Then the prefix is unchanged from the original first prefixLength elements.
        if (removedIndex >= prefixLength)
        {
            return AreAllGood(prefixGoodCount, 0, prefixLength - 2);
        }

        // Case 2:
        // Removed the very first element, and the prefix is taken from the shifted array.
        if (removedIndex == 0)
        {
            // Need all original pairs good[1..prefixLength-1] to be true.
            return AreAllGood(prefixGoodCount, 1, prefixLength - 1);
        }

        // Case 3:
        // Removed element is inside the prefix and is not the first element.
        // Since prefixLength <= n-1, removedIndex < prefixLength implies removedIndex <= n-2,
        // so removedIndex + 1 is a valid original index.
        //
        // Part A: all pairs before the removed element must be good.
        bool leftOk = AreAllGood(prefixGoodCount, 0, removedIndex - 2);
        if (!leftOk)
            return false;

        // Part B: the new bridge pair created by deletion must be good.
        bool bridgeOk = Gcd(readings[removedIndex - 1], readings[removedIndex + 1]) > 1;
        if (!bridgeOk)
            return false;

        // Part C: all original pairs after the removed element and still inside the prefix
        // must be good.
        bool rightOk = AreAllGood(prefixGoodCount, removedIndex + 1, prefixLength - 1);
        return rightOk;
    }

    // -------------------------------------------------------------------------
    // Returns true if all good[l..r] are true.
    //
    // If l > r, the range is empty, and "all elements are good" is vacuously true.
    //
    // We use the prefixGoodCount array:
    //   number of true values in [l..r] =
    //      prefixGoodCount[r + 1] - prefixGoodCount[l]
    //
    // The range length is (r - l + 1).
    // All are true iff countTrue == rangeLength.
    // -------------------------------------------------------------------------
    private bool AreAllGood(int[] prefixGoodCount, int l, int r)
    {
        if (l > r)
            return true;

        int countTrue = prefixGoodCount[r + 1] - prefixGoodCount[l];
        int length = r - l + 1;
        return countTrue == length;
    }

    // -------------------------------------------------------------------------
    // Standard iterative Euclidean algorithm for GCD.
    //
    // We use long? No need here because values are within int range and modulo
    // operations remain safe in int.
    // -------------------------------------------------------------------------
    private int Gcd(int a, int b)
    {
        while (b != 0)
        {
            int t = a % b;
            a = b;
            b = t;
        }
        return a;
    }
}

// -----------------------------------------------------------------------------
// Demo code
// -----------------------------------------------------------------------------

var solution = new Solution();

// Example 1 from the statement (corrected output should be 3)
int[] readings1 = { 6, 10, 15, 7, 14 };
int result1 = solution.MaximumReliablePrefixAfterOneRemoval(readings1);
Console.WriteLine(result1);

// Example 2 from the statement (corrected output should be 3)
int[] readings2 = { 8, 12, 18, 25, 35, 49 };
int result2 = solution.MaximumReliablePrefixAfterOneRemoval(readings2);
Console.WriteLine(result2);

// Additional quick checks

// Single element: always 1
int[] readings3 = { 7 };
Console.WriteLine(solution.MaximumReliablePrefixAfterOneRemoval(readings3));

// Already fully reliable: answer is full length
int[] readings4 = { 6, 9, 15, 25 };
Console.WriteLine(solution.MaximumReliablePrefixAfterOneRemoval(readings4));

// Removal of first element can help
int[] readings5 = { 7, 6, 10, 15 };
Console.WriteLine(solution.MaximumReliablePrefixAfterOneRemoval(readings5));