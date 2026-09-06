/*
Title: Count Pairs of Receipts With the Same Tax Breakdown

Problem Description:
A retail platform stores each receipt as a list of purchased line items. Every line item is represented by a pair [category, amount], where category is a lowercase string such as "food" or "electronics", and amount is a positive integer in cents. Two receipts are considered tax-equivalent if, after summing amounts by category, they produce exactly the same category-to-total mapping. The order of line items does not matter, and repeated categories within the same receipt should be merged by addition before comparison.

Your task is to return the number of unordered pairs of receipts that are tax-equivalent.

For example, the receipts [["food", 200], ["book", 500], ["food", 300]] and [["book", 500], ["food", 500]] are tax-equivalent because both reduce to {"food": 500, "book": 500}. However, receipts with the same set of categories but different totals are not equivalent.

Design an efficient solution using hashing. A common approach is to convert each receipt into a canonical signature that uniquely represents its aggregated category totals, then count how many times each signature appears.

Constraints:
- 1 <= receipts.length <= 100000
- 1 <= total number of line items across all receipts <= 200000
- 1 <= category.length <= 20
- category consists of lowercase English letters
- 1 <= amount <= 100000
- The answer may not fit in 32-bit integer; use 64-bit arithmetic

Example 1:
Input: receipts = [
  [["food",200],["book",500],["food",300]],
  [["book",500],["food",500]],
  [["food",200],["book",400]],
  [["book",500],["food",500],["toy",100]]
]
Output: 1

Example 2:
Input: receipts = [
  [["a",10],["b",5],["a",5]],
  [["b",5],["a",15]],
  [["a",15]],
  [["c",7]],
  [["c",7]],
  [["b",5],["a",15]]
]
Output: 4
*/

using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

public class Solution
{
    /*
    Time Complexity:
    Let T be the total number of line items across all receipts.
    Let U be the total number of distinct categories after aggregation across all receipts.
    For each receipt, we:
    1. Aggregate repeated categories using a dictionary.
    2. Sort the distinct categories of that receipt to build a canonical signature.
    Overall complexity is O(T + sum(k_i log k_i)), where k_i is the number of distinct categories in receipt i.
    This is efficient for the given constraints.

    Space Complexity:
    O(U + R), where:
    - U is the temporary space used for per-receipt aggregation dictionaries/signatures,
    - R is the number of distinct receipt signatures stored in the global frequency map.
    */
    public long CountEquivalentReceiptPairs(List<List<(string category, int amount)>> receipts)
    {
        // This dictionary is the core hashing structure for the whole problem.
        // Key   = canonical signature of a fully aggregated receipt
        // Value = how many previous receipts have exactly this same signature
        //
        // Why do we need this?
        // Because if the current receipt has already appeared 'f' times before,
        // then it forms exactly 'f' new unordered equivalent pairs with those earlier receipts.
        var signatureFrequency = new Dictionary<string, long>();

        // We store the final answer in a 64-bit integer because the number of pairs
        // can be large. For example, if many receipts are identical, the pair count
        // can exceed the range of a 32-bit integer.
        long pairCount = 0;

        // Process each receipt one by one.
        foreach (var receipt in receipts)
        {
            // STEP 1: Aggregate amounts by category inside the current receipt.
            //
            // Example:
            // [["food",200],["book",500],["food",300]]
            // becomes:
            // {"food": 500, "book": 500}
            //
            // Why is this necessary?
            // Because the problem says repeated categories within the same receipt
            // should be merged by addition before comparison.
            //
            // We use Dictionary<string, long> so that:
            // - lookup/update by category is fast on average: O(1)
            // - totals are safely stored using long
            var totalsByCategory = new Dictionary<string, long>();

            foreach (var (category, amount) in receipt)
            {
                // If the category has already been seen in this receipt,
                // add the amount to the existing total.
                if (totalsByCategory.ContainsKey(category))
                {
                    totalsByCategory[category] += amount;
                }
                else
                {
                    // Otherwise, this is the first time we see this category
                    // in the current receipt, so initialize its total.
                    totalsByCategory[category] = amount;
                }
            }

            // STEP 2: Convert the aggregated dictionary into a canonical signature.
            //
            // Why do we need a canonical signature?
            // Because dictionaries do not guarantee a meaningful order,
            // and the original line item order should not matter.
            //
            // So two logically equivalent receipts must produce the exact same string.
            //
            // To guarantee that, we:
            // 1. Sort categories alphabetically
            // 2. Append each as "category:total"
            // 3. Separate entries with a delimiter
            //
            // Example:
            // {"food":500, "book":500}
            // sorted by category => [("book",500), ("food",500)]
            // signature => "book:500|food:500|"
            //
            // This ensures:
            // - same aggregated mapping => same signature
            // - different mapping => different signature
            var sortedEntries = totalsByCategory.OrderBy(entry => entry.Key);

            var signatureBuilder = new StringBuilder();

            foreach (var entry in sortedEntries)
            {
                // Append category
                signatureBuilder.Append(entry.Key);

                // Append a separator between category and total.
                // Using explicit separators avoids ambiguity.
                signatureBuilder.Append(':');

                // Append the total amount for that category.
                signatureBuilder.Append(entry.Value);

                // Append an entry separator so adjacent entries do not blend together.
                signatureBuilder.Append('|');
            }

            string signature = signatureBuilder.ToString();

            // STEP 3: Use the signature frequency map to count new pairs.
            //
            // If this exact signature has already appeared before, then every previous
            // occurrence forms one new unordered pair with the current receipt.
            //
            // Example:
            // If frequency["book:500|food:500|"] == 2,
            // then the current receipt creates 2 new pairs.
            if (signatureFrequency.TryGetValue(signature, out long existingCount))
            {
                pairCount += existingCount;
                signatureFrequency[signature] = existingCount + 1;
            }
            else
            {
                // First time we see this signature.
                signatureFrequency[signature] = 1;
            }
        }

        // After processing all receipts, pairCount contains the total number
        // of unordered equivalent receipt pairs.
        return pairCount;
    }
}

// Demo code

var solution = new Solution();

// Example 1
var receipts1 = new List<List<(string category, int amount)>>
{
    new()
    {
        ("food", 200),
        ("book", 500),
        ("food", 300)
    },
    new()
    {
        ("book", 500),
        ("food", 500)
    },
    new()
    {
        ("food", 200),
        ("book", 400)
    },
    new()
    {
        ("book", 500),
        ("food", 500),
        ("toy", 100)
    }
};

long result1 = solution.CountEquivalentReceiptPairs(receipts1);
Console.WriteLine(result1); // Expected: 1

// Example 2
var receipts2 = new List<List<(string category, int amount)>>
{
    new()
    {
        ("a", 10),
        ("b", 5),
        ("a", 5)
    },
    new()
    {
        ("b", 5),
        ("a", 15)
    },
    new()
    {
        ("a", 15)
    },
    new()
    {
        ("c", 7)
    },
    new()
    {
        ("c", 7)
    },
    new()
    {
        ("b", 5),
        ("a", 15)
    }
};

long result2 = solution.CountEquivalentReceiptPairs(receipts2);
Console.WriteLine(result2); // Expected: 4