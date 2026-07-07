/*
Title: Count Accounts Sharing an Email Domain Set
Difficulty: Medium
Topic: Hashing

Problem Description:
You are given a list of user accounts, where each account is represented by a user name and a list of email addresses associated with that user.
Two accounts are considered equivalent if the set of email domains used by their addresses is exactly the same, regardless of how many emails
they have from each domain, the order of the emails, or repeated addresses.

For example:
- ["a@x.com", "b@y.com", "c@x.com"] -> domain set {"x.com", "y.com"}
- ["m@y.com", "n@x.com"] -> domain set {"x.com", "y.com"}

These two accounts are equivalent because their unique domain sets are identical.

Task:
Return the number of unordered pairs of accounts that are equivalent in this sense.

Important details:
- Only the domain part after '@' matters.
- Account names must be ignored.
- Repeated emails or repeated domains inside the same account should not affect the result.
- Because the input can be large, we should normalize each account into a canonical representation
  of its unique domain set, then count how many accounts share the same normalized representation.

Constraints:
- 1 <= accounts.length <= 100000
- 1 <= emails[i].length <= 100000 across all accounts combined
- Each account contains at least 1 email
- Each email string length is between 3 and 100
- Domains consist of lowercase English letters, digits, dots, and hyphens
- The total number of emails across all accounts does not exceed 200000

Example 1:
Input:
[
  ["Alice", ["a@x.com", "b@y.com", "c@x.com"]],
  ["Bob",   ["u@y.com", "v@x.com"]],
  ["Cara",  ["k@z.com"]],
  ["Dan",   ["p@z.com", "q@z.com"]]
]
Output: 2

Explanation:
- Alice -> {"x.com", "y.com"}
- Bob   -> {"x.com", "y.com"}
- Cara  -> {"z.com"}
- Dan   -> {"z.com"}

Equivalent unordered pairs:
- (Alice, Bob)
- (Cara, Dan)

So the answer is 2.

Example 2:
Input:
[
  ["Rita", ["r@a.io", "s@b.io"]],
  ["Sam",  ["t@b.io", "u@a.io", "v@c.io"]],
  ["Tina", ["w@a.io", "x@b.io"]],
  ["Uma",  ["y@c.io"]]
]
Output: 1

Explanation:
- Rita -> {"a.io", "b.io"}
- Sam  -> {"a.io", "b.io", "c.io"}
- Tina -> {"a.io", "b.io"}
- Uma  -> {"c.io"}

Equivalent unordered pair:
- (Rita, Tina)

So the answer is 1.
*/

using System;
using System.Collections.Generic;
using System.Linq;

public class Solution
{
    /*
    Time Complexity:
    Let E be the total number of emails across all accounts.
    Let D be the total number of unique domains collected across all accounts.
    For each account, we:
    1. Extract domains from its emails.
    2. Deduplicate them with a HashSet.
    3. Sort the unique domains so that equivalent sets get the exact same canonical order.
    4. Join them into one string key.

    Overall, the work is:
    - O(E) for scanning emails and extracting domains
    - plus sorting the unique domains inside each account

    More precisely:
    O(E + sum over accounts of (k_i log k_i)))
    where k_i is the number of unique domains in account i.

    Space Complexity:
    O(D + A)
    where:
    - D is the total temporary storage for unique domains while processing accounts
    - A is the number of distinct normalized domain-set keys stored in the dictionary

    This is efficient enough for the given constraints.
    */
    public long CountEquivalentAccountPairs(List<Account> accounts)
    {
        // This dictionary maps:
        //   canonical domain-set representation -> how many accounts have that exact domain set
        //
        // Why do we need this?
        // Because once every account is normalized into a standard form,
        // the problem becomes:
        // "How many times does each normalized form appear?"
        //
        // If a normalized form appears c times, then the number of unordered pairs
        // among those c accounts is:
        //   c * (c - 1) / 2
        //
        // We will compute that after counting frequencies.
        var frequency = new Dictionary<string, long>();

        // Process each account one by one.
        foreach (var account in accounts)
        {
            // Step 1: Build the set of unique domains for the current account.
            //
            // We use HashSet<string> because:
            // - We only care about unique domains, not duplicate emails.
            // - If the same domain appears multiple times in one account,
            //   it should be counted only once.
            //
            // Example:
            // ["a@x.com", "b@y.com", "c@x.com"]
            // should become {"x.com", "y.com"}.
            var uniqueDomains = new HashSet<string>();

            foreach (var email in account.Emails)
            {
                // Step 2: Extract the domain part after '@'.
                //
                // The problem guarantees exactly one '@' and valid non-empty parts,
                // so taking the substring after '@' is safe.
                int atIndex = email.IndexOf('@');
                string domain = email[(atIndex + 1)..];

                // Add to the set.
                // If the domain is already present, HashSet ignores duplicates automatically.
                uniqueDomains.Add(domain);
            }

            // Step 3: Convert the set into a canonical representation.
            //
            // Why is this necessary?
            // Because sets do not have order, but strings do.
            // Two equivalent accounts may list domains in different orders:
            //
            // Account A -> {"x.com", "y.com"}
            // Account B -> {"y.com", "x.com"}
            //
            // If we simply joined them without sorting, we might get:
            // "x.com|y.com" vs "y.com|x.com"
            // which would incorrectly look different.
            //
            // So we sort the domains first.
            var sortedDomains = uniqueDomains.ToList();
            sortedDomains.Sort(StringComparer.Ordinal);

            // Step 4: Join the sorted domains into one string key.
            //
            // This key is the normalized identity of the domain set.
            //
            // Example:
            // {"x.com", "y.com"} -> ["x.com", "y.com"] -> "x.com|y.com"
            //
            // Any equivalent account will produce the exact same key.
            string key = string.Join("|", sortedDomains);

            // Step 5: Count how many accounts produce this same normalized key.
            if (!frequency.TryAdd(key, 1))
            {
                frequency[key]++;
            }
        }

        // Step 6: Convert frequencies into number of unordered pairs.
        //
        // If a key appears c times, the number of unordered pairs is:
        // c choose 2 = c * (c - 1) / 2
        //
        // Example:
        // If 3 accounts share the same domain set, the pairs are:
        // (1,2), (1,3), (2,3) => 3 pairs
        long result = 0;

        foreach (var count in frequency.Values)
        {
            result += count * (count - 1) / 2;
        }

        return result;
    }
}

public class Account
{
    public string Name { get; }
    public List<string> Emails { get; }

    public Account(string name, List<string> emails)
    {
        Name = name;
        Emails = emails;
    }
}

// Demo code

var solution = new Solution();

// Example 1
var accounts1 = new List<Account>
{
    new Account("Alice", new List<string> { "a@x.com", "b@y.com", "c@x.com" }),
    new Account("Bob",   new List<string> { "u@y.com", "v@x.com" }),
    new Account("Cara",  new List<string> { "k@z.com" }),
    new Account("Dan",   new List<string> { "p@z.com", "q@z.com" })
};

long result1 = solution.CountEquivalentAccountPairs(accounts1);
Console.WriteLine(result1); // Expected: 2

// Example 2
var accounts2 = new List<Account>
{
    new Account("Rita", new List<string> { "r@a.io", "s@b.io" }),
    new Account("Sam",  new List<string> { "t@b.io", "u@a.io", "v@c.io" }),
    new Account("Tina", new List<string> { "w@a.io", "x@b.io" }),
    new Account("Uma",  new List<string> { "y@c.io" })
};

long result2 = solution.CountEquivalentAccountPairs(accounts2);
Console.WriteLine(result2); // Expected: 1

// Additional small sanity check:
// Two accounts with repeated domains should still match by unique domain set.
var accounts3 = new List<Account>
{
    new Account("User1", new List<string> { "a@test.com", "b@test.com", "c@site.org" }),
    new Account("User2", new List<string> { "x@site.org", "y@test.com" }),
    new Account("User3", new List<string> { "z@other.net" })
};

long result3 = solution.CountEquivalentAccountPairs(accounts3);
Console.WriteLine(result3); // Expected: 1