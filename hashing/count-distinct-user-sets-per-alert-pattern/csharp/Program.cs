/*
Title: Count Distinct User Sets per Alert Pattern
Difficulty: Hard
Topic: Hashing

Problem Description:
A monitoring system records security alerts as pairs (userId, alertCode). Multiple records may exist for the same pair
because the same user can trigger the same alert many times. For each alertCode, define its user set as the set of
distinct users who triggered that alert at least once. Two alert codes are considered equivalent if their user sets are
exactly the same.

Your task is to return the number of unordered pairs of distinct alert codes that are equivalent.

For example, if alert A1 was triggered by users {2, 5, 9} and alert B7 was also triggered by users {2, 5, 9}, then
(A1, B7) contributes 1 to the answer. Duplicate records must not change a user set. Alert codes triggered by no users
do not appear in the input and therefore are not considered.

Design an algorithm that works efficiently for large inputs. A naive comparison of every pair of alert codes and every
user in their sets will be too slow. You will likely need hashing to build a canonical representation of each alert's
distinct user set and then count how many alerts share the same representation.

Constraints:
- 1 <= records.length <= 2 * 10^5
- Each record is a pair [userId, alertCode]
- 1 <= userId <= 10^9
- 1 <= alertCode <= 10^9
- The same (userId, alertCode) pair may appear many times
- The answer can be as large as n * (n - 1) / 2, so use 64-bit arithmetic

Example 1:
Input: records = [[1,10],[2,10],[2,10],[1,20],[2,20],[3,30],[4,30]]
Output: 1
Explanation: alert 10 has users {1,2}, alert 20 has users {1,2}, and alert 30 has users {3,4}. Only alerts 10 and 20
have identical user sets.

Example 2:
Input: records = [[5,100],[7,100],[5,200],[7,200],[8,300],[8,300],[9,400],[10,400],[9,500],[10,500]]
Output: 2
Explanation: alert 100 and 200 share the user set {5,7}. Alert 400 and 500 share the user set {9,10}. Alert 300 has
user set {8}. Therefore there are 2 equivalent unordered pairs.
*/

using System;
using System.Collections.Generic;

class Solution
{
    // Time Complexity:
    // Let n be the number of input records.
    // 1) We first sort all records by (alertCode, userId): O(n log n)
    // 2) We then scan the sorted array once to build a canonical signature for each alert: O(n)
    // 3) We count how many alerts share the same signature using a dictionary: O(number of alerts)
    //
    // Overall: O(n log n)
    //
    // Space Complexity:
    // - We store a copy of the records for sorting: O(n)
    // - We store one signature per distinct alert code: O(number of alerts)
    // - We store counts of equal signatures: O(number of distinct user sets)
    //
    // Overall: O(n)
    public long CountEquivalentAlertPairs(int[][] records)
    {
        // Defensive check.
        // The problem guarantees at least one record, but writing safe code is still a good habit.
        if (records == null || records.Length == 0)
        {
            return 0L;
        }

        // STEP 1:
        // Convert the jagged input into a strongly-typed list of tuples and sort it.
        //
        // Why sort?
        // We need a canonical representation of each alert's DISTINCT user set.
        // If we process all records for the same alert together, and within that alert see users in sorted order,
        // then we can:
        //   - ignore duplicates easily
        //   - build the exact same signature for alerts that have the same user set
        //
        // Sorting by:
        //   1) alertCode
        //   2) userId
        //
        // ensures that:
        //   - all rows for the same alert are consecutive
        //   - duplicate (userId, alertCode) rows are adjacent
        var pairs = new (int UserId, int AlertCode)[records.Length];
        for (int i = 0; i < records.Length; i++)
        {
            pairs[i] = (records[i][0], records[i][1]);
        }

        Array.Sort(pairs, (a, b) =>
        {
            int byAlert = a.AlertCode.CompareTo(b.AlertCode);
            if (byAlert != 0) return byAlert;
            return a.UserId.CompareTo(b.UserId);
        });

        // STEP 2:
        // For each alert, build a canonical signature of its DISTINCT user set.
        //
        // Important idea:
        // Two alerts are equivalent if and only if the sorted list of distinct users is identical.
        //
        // We could store the whole sorted user list as a string like "1#2#5#9",
        // but string building can be memory-heavy for large inputs.
        //
        // Instead, we compute a robust hash-based signature from the sorted distinct users.
        // To make collisions astronomically unlikely, we use TWO independent 64-bit rolling hashes
        // plus the count of distinct users.
        //
        // Because the users are processed in sorted order and duplicates are skipped,
        // equivalent user sets produce exactly the same signature.
        //
        // Signature fields:
        //   - H1: first 64-bit hash
        //   - H2: second 64-bit hash
        //   - Count: number of distinct users in the set
        //
        // Why include Count?
        // It adds another layer of safety and helps distinguish some edge cases even more clearly.
        var signatureCount = new Dictionary<SetSignature, long>();

        // Constants for hashing.
        // These are fixed odd 64-bit values used to mix numbers well.
        const ulong Seed1 = 1469598103934665603UL;
        const ulong Seed2 = 1099511628211UL;
        const ulong Mul1 = 11400714819323198485UL;
        const ulong Mul2 = 14029467366897019727UL;
        const ulong MixConst1 = 0x9E3779B185EBCA87UL;
        const ulong MixConst2 = 0xC2B2AE3D27D4EB4FUL;

        int index = 0;

        // We scan the sorted array group by group.
        // Each group corresponds to one alertCode.
        while (index < pairs.Length)
        {
            int currentAlert = pairs[index].AlertCode;

            // Initialize the rolling hash state for this alert's user set.
            ulong h1 = Seed1;
            ulong h2 = Seed2;
            int distinctUserCount = 0;

            // We also track the last user we accepted for this alert,
            // so duplicate rows like (2,10), (2,10), (2,10) only contribute once.
            int lastUser = -1;
            bool hasLastUser = false;

            // Process all rows belonging to the current alert.
            while (index < pairs.Length && pairs[index].AlertCode == currentAlert)
            {
                int user = pairs[index].UserId;

                // If this user is the same as the previous user in the same alert group,
                // then this is a duplicate record and must NOT change the set.
                if (!hasLastUser || user != lastUser)
                {
                    // This is a new distinct user for the current alert.
                    distinctUserCount++;

                    // Mix the user id into both hashes.
                    //
                    // We use unchecked arithmetic intentionally.
                    // In hashing, overflow is normal and expected.
                    unchecked
                    {
                        ulong x = (ulong)(uint)user;

                        // First hash:
                        //   - xor with a mixed version of x
                        //   - multiply by a large odd constant
                        //   - add another constant
                        h1 ^= x + MixConst1 + (h1 << 6) + (h1 >> 2);
                        h1 *= Mul1;
                        h1 += 0x27D4EB2F165667C5UL;

                        // Second hash:
                        //   - use a different mixing pattern and constants
                        h2 += x + MixConst2 + (h2 << 7) + (h2 >> 3);
                        h2 ^= Mul2;
                        h2 *= 0x165667B19E3779F9UL;
                    }

                    lastUser = user;
                    hasLastUser = true;
                }

                index++;
            }

            // Finalize the signature for this alert.
            //
            // Finalization helps make the hash depend not only on the sequence of users,
            // but also more strongly on the total number of distinct users.
            unchecked
            {
                h1 ^= (ulong)distinctUserCount * 0x9E3779B97F4A7C15UL;
                h2 ^= (ulong)distinctUserCount * 0xC2B2AE3D27D4EB4FUL;

                h1 ^= h1 >> 33;
                h1 *= 0xff51afd7ed558ccdUL;
                h1 ^= h1 >> 33;

                h2 ^= h2 >> 29;
                h2 *= 0xbf58476d1ce4e5b9UL;
                h2 ^= h2 >> 32;
            }

            var signature = new SetSignature(h1, h2, distinctUserCount);

            // Count how many alerts have this exact signature.
            if (signatureCount.TryGetValue(signature, out long existing))
            {
                signatureCount[signature] = existing + 1;
            }
            else
            {
                signatureCount[signature] = 1;
            }
        }

        // STEP 3:
        // If a particular user-set signature appears k times,
        // then there are k choose 2 unordered pairs of alerts with that same set.
        //
        // Formula:
        //   k * (k - 1) / 2
        //
        // We sum this over all signatures.
        long answer = 0L;

        foreach (var entry in signatureCount)
        {
            long k = entry.Value;
            answer += k * (k - 1) / 2;
        }

        return answer;
    }

    // This readonly struct is the dictionary key representing one alert's distinct user set.
    // Two alerts with the same (H1, H2, Count) are treated as having the same canonical representation.
    private readonly record struct SetSignature(ulong H1, ulong H2, int Count);
}

// ------------------------------------------------------------
// Demo code
// ------------------------------------------------------------

var solution = new Solution();

// Example 1:
// alert 10 -> users {1,2}
// alert 20 -> users {1,2}
// alert 30 -> users {3,4}
// Equivalent unordered pairs: only (10,20) => 1
int[][] records1 =
{
    new[] { 1, 10 },
    new[] { 2, 10 },
    new[] { 2, 10 },
    new[] { 1, 20 },
    new[] { 2, 20 },
    new[] { 3, 30 },
    new[] { 4, 30 }
};

long result1 = solution.CountEquivalentAlertPairs(records1);
Console.WriteLine(result1); // Expected: 1

// Example 2:
// alert 100 -> {5,7}
// alert 200 -> {5,7}
// alert 300 -> {8}
// alert 400 -> {9,10}
// alert 500 -> {9,10}
// Equivalent unordered pairs: (100,200) and (400,500) => 2
int[][] records2 =
{
    new[] { 5, 100 },
    new[] { 7, 100 },
    new[] { 5, 200 },
    new[] { 7, 200 },
    new[] { 8, 300 },
    new[] { 8, 300 },
    new[] { 9, 400 },
    new[] { 10, 400 },
    new[] { 9, 500 },
    new[] { 10, 500 }
};

long result2 = solution.CountEquivalentAlertPairs(records2);
Console.WriteLine(result2); // Expected: 2

// Additional small sanity check:
// alert 1 -> {42}
// alert 2 -> {42}
// alert 3 -> {42}
// Number of unordered equivalent pairs = 3 choose 2 = 3
int[][] records3 =
{
    new[] { 42, 1 },
    new[] { 42, 1 },
    new[] { 42, 2 },
    new[] { 42, 3 }
};

long result3 = solution.CountEquivalentAlertPairs(records3);
Console.WriteLine(result3); // Expected: 3