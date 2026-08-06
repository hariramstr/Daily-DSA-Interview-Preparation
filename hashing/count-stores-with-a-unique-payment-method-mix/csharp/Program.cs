/*
Title: Count Stores With a Unique Payment Method Mix
Difficulty: Medium
Topic: Hashing

Problem Description:
A retail analytics system records, for each store, the payment methods used during a day.
Each store is represented by a list of method names such as "cash", "card", "wallet", or "gift".
The same method may appear multiple times for a store because many customers can use it,
but for this task only the set of distinct methods matters.

Two stores are considered to have the same payment method mix if the set of distinct method
names used at both stores is identical, regardless of order or repetition.
For example, ["cash", "card", "cash"] and ["card", "cash"] represent the same mix.

Given a 2D array methodsUsed where methodsUsed[i] is the list of payment methods recorded
for store i, return the number of stores whose payment method mix is unique across all stores.
In other words, count how many stores belong to a distinct-method set that appears exactly once.

You should design an efficient solution using hashing. A common approach is to normalize each
store's method list into a canonical representation of its distinct methods, then count how many
times each normalized signature appears.

Constraints:
- 1 <= methodsUsed.length <= 100000
- 0 <= methodsUsed[i].length <= 100
- 1 <= total number of method entries across all stores <= 200000
- Each method name consists of lowercase English letters and has length from 1 to 20

Example 1:
Input: methodsUsed = [["cash","card","cash"],["wallet"],["card","cash"],["gift","wallet"],["wallet","gift"],["bank"]]
Output: 2
Explanation: The normalized mixes are {cash,card}, {wallet}, {cash,card}, {gift,wallet}, {gift,wallet}, and {bank}.
Only {wallet} and {bank} appear exactly once, so the answer is 2.

Example 2:
Input: methodsUsed = [["cash"],[],["card","wallet"],["wallet","card","wallet"],[]]
Output: 1
Explanation: The normalized mixes are {cash}, {}, {card,wallet}, {card,wallet}, and {}.
Only {cash} appears exactly once, so the answer is 1.
*/

using System;
using System.Collections.Generic;

public class Solution
{
    /*
    Time Complexity:
    Let N be the number of stores.
    Let K be the total number of method entries across all stores.
    Let Mi be the number of entries in store i, and Di be the number of distinct methods in store i.

    For each store:
    - We build a HashSet to remove duplicates: O(Mi)
    - We copy distinct methods into a list and sort them: O(Di log Di)
    - We build a canonical string signature from the sorted distinct methods

    Total:
    O( sum(Mi) + sum(Di log Di) ) which is efficient for the given constraints.

    Space Complexity:
    - HashSet/list used per store for normalization
    - Dictionary storing frequency of each normalized signature

    Overall auxiliary space is O(U + S), where:
    - U is the total size of unique signatures stored in the dictionary
    - S is the temporary space for one store's distinct methods
    */
    public int CountStoresWithUniquePaymentMethodMix(string[][] methodsUsed)
    {
        // This dictionary maps:
        //   normalized signature of a store's distinct payment methods
        // to
        //   how many stores have exactly that same signature.
        //
        // Why do we need this?
        // Because the problem asks us to count stores whose distinct-method set appears exactly once.
        // So first we must know how many times each distinct set occurs.
        var signatureCount = new Dictionary<string, int>();

        // We also keep the normalized signature for each store in order.
        // Why store them?
        // After counting frequencies, we need to revisit each store and check
        // whether its signature appeared exactly once.
        var storeSignatures = new string[methodsUsed.Length];

        // Process every store one by one.
        for (int i = 0; i < methodsUsed.Length; i++)
        {
            // STEP 1: Remove duplicates inside the current store.
            //
            // Example:
            // ["cash", "card", "cash"] should become {"cash", "card"}
            //
            // A HashSet is the perfect data structure here because:
            // - it automatically keeps only distinct values
            // - insertion is average O(1)
            var distinctMethods = new HashSet<string>();

            foreach (var method in methodsUsed[i])
            {
                distinctMethods.Add(method);
            }

            // STEP 2: Convert the distinct set into a list so we can sort it.
            //
            // Why sort?
            // Because sets do not have a stable order.
            // For example:
            // {"cash", "card"} and {"card", "cash"} are the same set,
            // but if we directly join them without sorting, we might produce different strings.
            //
            // Sorting guarantees a canonical order:
            // both become ["card", "cash"].
            var normalizedList = new List<string>(distinctMethods);
            normalizedList.Sort(StringComparer.Ordinal);

            // STEP 3: Build a canonical string signature.
            //
            // We need one consistent representation for the set of distinct methods.
            // Joining the sorted method names with a separator gives us that.
            //
            // Example:
            // ["card", "cash"] -> "card|cash"
            // ["wallet"]       -> "wallet"
            // []               -> ""   (empty signature for empty set)
            //
            // Why is this safe?
            // Method names contain only lowercase English letters, so a separator like '|'
            // cannot appear inside a method name and therefore cannot create ambiguity.
            string signature = string.Join("|", normalizedList);

            // Save the signature for this store so we can use it later.
            storeSignatures[i] = signature;

            // STEP 4: Count how many times this signature appears.
            //
            // If we have seen it before, increment its count.
            // Otherwise, start its count at 1.
            if (signatureCount.ContainsKey(signature))
            {
                signatureCount[signature]++;
            }
            else
            {
                signatureCount[signature] = 1;
            }
        }

        // STEP 5: Count stores whose signature frequency is exactly 1.
        //
        // This directly matches the problem statement:
        // a store is counted if its distinct-method mix appears exactly once across all stores.
        int uniqueStoreCount = 0;

        for (int i = 0; i < storeSignatures.Length; i++)
        {
            if (signatureCount[storeSignatures[i]] == 1)
            {
                uniqueStoreCount++;
            }
        }

        return uniqueStoreCount;
    }
}

// Demo code

var solution = new Solution();

// Example 1
string[][] methodsUsed1 =
{
    new[] { "cash", "card", "cash" },
    new[] { "wallet" },
    new[] { "card", "cash" },
    new[] { "gift", "wallet" },
    new[] { "wallet", "gift" },
    new[] { "bank" }
};

int result1 = solution.CountStoresWithUniquePaymentMethodMix(methodsUsed1);
Console.WriteLine(result1); // Expected: 2

// Example 2
string[][] methodsUsed2 =
{
    new[] { "cash" },
    Array.Empty<string>(),
    new[] { "card", "wallet" },
    new[] { "wallet", "card", "wallet" },
    Array.Empty<string>()
};

int result2 = solution.CountStoresWithUniquePaymentMethodMix(methodsUsed2);
Console.WriteLine(result2); // Expected: 1

// Additional quick demo
string[][] methodsUsed3 =
{
    new[] { "cash", "cash" },
    new[] { "cash" },
    new[] { "card" },
    new[] { "wallet" },
    new[] { "wallet", "wallet" }
};

int result3 = solution.CountStoresWithUniquePaymentMethodMix(methodsUsed3);
Console.WriteLine(result3); // Expected: 1 (only {"card"} is unique)