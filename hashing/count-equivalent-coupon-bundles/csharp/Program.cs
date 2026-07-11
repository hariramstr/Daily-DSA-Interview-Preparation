/*
Title: Count Equivalent Coupon Bundles
Difficulty: Medium
Topic: Hashing

Problem Description:
An e-commerce platform stores promotional bundles as arrays of coupon codes. Two bundles are considered equivalent if they contain exactly the same coupon codes with the same frequencies, regardless of order. For example, ["SAVE10", "FREESHIP", "SAVE10"] is equivalent to ["SAVE10", "SAVE10", "FREESHIP"], but not to ["SAVE10", "FREESHIP"] or ["SAVE10", "FREESHIP", "BONUS"].

You are given a list of bundles, where each bundle is an array of strings. Return the number of unordered pairs of indices (i, j) such that i < j and bundles[i] is equivalent to bundles[j].

Because coupon codes are strings and bundle order does not matter, a direct comparison of every pair is too slow for large inputs. You should design an efficient solution using hashing or canonical representations.

Constraints:
- 1 <= bundles.length <= 100000
- 1 <= bundles[i].length <= 20
- 1 <= couponCodes[i].length <= 20
- Each coupon code consists of uppercase English letters and digits.
- The total number of coupon codes across all bundles does not exceed 200000.

Example 1:
Input: bundles = [["SAVE10","FREESHIP"],["FREESHIP","SAVE10"],["BONUS"],["SAVE10","SAVE10","FREESHIP"],["FREESHIP","SAVE10","SAVE10"]]
Output: 2
Explanation: Bundles 0 and 1 are equivalent. Bundles 3 and 4 are equivalent. No other pair matches.

Example 2:
Input: bundles = [["A","B","A"],["A","A","B"],["B","A"],["C"],["C"],["C","C"]]
Output: 2
Explanation: One matching pair comes from the first two bundles. Another matching pair comes from the two single-element ["C"] bundles.
*/

using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

public class Solution
{
    /*
    Time Complexity:
    Let T be the total number of coupon codes across all bundles.
    Let k be the size of one bundle.
    For each bundle, we sort its coupon codes to create a canonical representation.
    Sorting one bundle costs O(k log k), and because each bundle length is at most 20,
    this is very small in practice.
    Overall complexity is O(sum over all bundles of k log k)), which is efficient
    under the given constraints.

    Space Complexity:
    O(U), where U is the number of distinct canonical bundle representations stored
    in the dictionary. We also use temporary space for sorting each bundle.
    */
    public long CountEquivalentBundles(string[][] bundles)
    {
        // This dictionary maps:
        //   canonical bundle representation -> how many times we have seen it so far
        //
        // Why do we need this?
        // If the current bundle has already appeared x times before, then it forms
        // exactly x new equivalent pairs with those previous bundles.
        //
        // Example:
        //   If "A|B|B" has appeared 3 times already, and we see it again now,
        //   then this new bundle forms 3 new pairs:
        //   (new with first), (new with second), (new with third)
        var frequencyByCanonicalForm = new Dictionary<string, long>();

        // We use long because the number of pairs can be large.
        // For example, if many bundles are identical, the number of pairs can be
        // close to n * (n - 1) / 2, which may exceed int.
        long pairCount = 0;

        // Process each bundle one by one.
        foreach (var bundle in bundles)
        {
            // STEP 1: Create a copy of the current bundle.
            //
            // Why copy?
            // We do not want to modify the original input array in case the caller
            // expects it to remain unchanged.
            var sortedCoupons = bundle.ToArray();

            // STEP 2: Sort the coupon codes.
            //
            // Why sort?
            // Two bundles are equivalent if they contain the same strings with the same
            // frequencies, regardless of order.
            //
            // Sorting transforms all equivalent bundles into the same ordered sequence.
            //
            // Example:
            //   ["SAVE10", "FREESHIP"] -> ["FREESHIP", "SAVE10"]
            //   ["FREESHIP", "SAVE10"] -> ["FREESHIP", "SAVE10"]
            //
            // After sorting, both become identical.
            Array.Sort(sortedCoupons, StringComparer.Ordinal);

            // STEP 3: Build a canonical string key from the sorted bundle.
            //
            // Why do we need a canonical key?
            // We need something we can store in a hash table (Dictionary).
            // A string is a convenient immutable key type in C#.
            //
            // Important detail:
            // We must separate coupon codes safely so that different bundles do not
            // accidentally produce the same key.
            //
            // For example, without separators:
            //   ["AB", "C"]  -> "ABC"
            //   ["A", "BC"]  -> "ABC"
            // These are different bundles but would collide.
            //
            // To avoid that, we include each string's length before the string itself.
            // This makes the representation unambiguous.
            //
            // Example:
            //   ["A", "BC"]  -> "1#A|2#BC|"
            //   ["AB", "C"]  -> "2#AB|1#C|"
            //
            // These are clearly different.
            string canonicalKey = BuildCanonicalKey(sortedCoupons);

            // STEP 4: Check how many identical canonical bundles we have seen before.
            //
            // If we have seen this exact canonical form before 'seenCount' times,
            // then the current bundle forms 'seenCount' new equivalent pairs.
            //
            // Why?
            // Because every previous identical bundle can pair with the current one.
            if (frequencyByCanonicalForm.TryGetValue(canonicalKey, out long seenCount))
            {
                pairCount += seenCount;
                frequencyByCanonicalForm[canonicalKey] = seenCount + 1;
            }
            else
            {
                // First time we see this bundle shape.
                frequencyByCanonicalForm[canonicalKey] = 1;
            }
        }

        // After processing all bundles, pairCount contains the number of unordered
        // pairs (i, j) with i < j that are equivalent.
        return pairCount;
    }

    private string BuildCanonicalKey(string[] sortedCoupons)
    {
        // We use StringBuilder because repeatedly concatenating strings can be slower
        // and create many temporary string objects.
        var sb = new StringBuilder();

        // Append each coupon in sorted order with a safe encoding:
        // length + '#' + coupon + '|'
        //
        // This guarantees that the final string uniquely represents the multiset
        // of coupon codes in the bundle.
        foreach (var coupon in sortedCoupons)
        {
            sb.Append(coupon.Length);
            sb.Append('#');
            sb.Append(coupon);
            sb.Append('|');
        }

        return sb.ToString();
    }
}

// Demo code

var solution = new Solution();

// Example 1
string[][] bundles1 =
{
    new[] { "SAVE10", "FREESHIP" },
    new[] { "FREESHIP", "SAVE10" },
    new[] { "BONUS" },
    new[] { "SAVE10", "SAVE10", "FREESHIP" },
    new[] { "FREESHIP", "SAVE10", "SAVE10" }
};

long result1 = solution.CountEquivalentBundles(bundles1);
Console.WriteLine(result1); // Expected: 2

// Example 2
string[][] bundles2 =
{
    new[] { "A", "B", "A" },
    new[] { "A", "A", "B" },
    new[] { "B", "A" },
    new[] { "C" },
    new[] { "C" },
    new[] { "C", "C" }
};

long result2 = solution.CountEquivalentBundles(bundles2);
Console.WriteLine(result2); // Expected: 2

// Additional quick sanity check
string[][] bundles3 =
{
    new[] { "X" },
    new[] { "Y" },
    new[] { "X" },
    new[] { "X", "X" },
    new[] { "X", "X" },
    new[] { "Y" }
};

long result3 = solution.CountEquivalentBundles(bundles3);
Console.WriteLine(result3); // Expected: 3
