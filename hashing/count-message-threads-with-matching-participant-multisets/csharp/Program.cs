/*
Title: Count Message Threads With Matching Participant Multisets
Difficulty: Hard
Topic: Hashing

Problem Description:
You are given chat logs from a messaging platform. Each thread is represented by a list of user IDs in the order messages were sent. A user may appear multiple times in the same thread if they sent multiple messages. Two threads are considered equivalent if they contain exactly the same multiset of participants, meaning every user ID appears the same number of times in both threads, regardless of message order.

Your task is to count how many unordered pairs of threads are equivalent.

Formally, let threads[i] be the list of user IDs in the i-th thread. Threads i and j are equivalent if for every user ID x, the number of occurrences of x in threads[i] equals the number of occurrences of x in threads[j]. Return the number of pairs (i, j) such that 0 <= i < j < n and threads[i] and threads[j] are equivalent.

Because user IDs can be very large and each thread may contain repeated IDs, a naive comparison of every pair will be too slow. You need to design a hashing-based representation that uniquely identifies the participant multiset of each thread.

Constraints:
- 1 <= n <= 100000
- 1 <= total number of user IDs across all threads <= 300000
- 1 <= threads[i].length <= 100000
- Sum of all threads[i].length over all threads is at most 300000
- 1 <= user ID <= 10^9
- Return the answer as a 64-bit integer

Example 1:
Input: threads = [[4,1,4,2],[2,4,4,1],[3,3],[1,2,4,4],[3,3,3]]
Output: 3
Explanation: Threads 0, 1, and 3 all have the same participant multiset: {1:1, 2:1, 4:2}. They contribute 3 unordered pairs. Thread 2 has multiset {3:2}, and thread 4 has multiset {3:3}, so they do not match.

Example 2:
Input: threads = [[8,9],[9,8,8],[7],[8,9],[7],[9,8]]
Output: 4
Explanation: Threads 0, 3, and 5 are equivalent because each contains one 8 and one 9, contributing 3 pairs. Threads 2 and 4 are equivalent, contributing 1 more pair. Thread 1 is different because it contains two 8s and one 9.
*/

using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

public class Solution
{
    /*
    Time Complexity:
    Let T be the total number of user IDs across all threads.
    Let k_i be the number of distinct user IDs in thread i.

    For each thread:
    1. We count frequencies in O(length of thread).
    2. We sort the distinct user IDs so that order does not matter, in O(k_i log k_i).
    3. We build a canonical string key in O(k_i).

    Total:
    O(T + sum(k_i log k_i))

    Since the total number of IDs across all threads is at most 300000, this is efficient enough.

    Space Complexity:
    O(T) in the worst case across all temporary frequency maps and stored keys/counts.
    */
    public long CountEquivalentThreadPairs(IList<IList<int>> threads)
    {
        // This dictionary maps:
        //   canonical multiset representation -> how many threads have exactly this multiset
        //
        // Why do we need this?
        // Because once we can convert each thread into a unique "signature" that represents
        // its participant multiset, the problem becomes:
        //   "How many equal signatures are there?"
        //
        // If a signature appears c times, then it contributes:
        //   c choose 2 = c * (c - 1) / 2
        //
        // Instead of computing combinations at the end, we can count pairs incrementally:
        // - when we see a signature that has already appeared x times,
        //   the new thread forms exactly x new pairs with those previous threads.
        var signatureCount = new Dictionary<string, long>();

        // This will store the final answer.
        long pairs = 0;

        // Process every thread one by one.
        foreach (var thread in threads)
        {
            // Step 1: Count how many times each user ID appears in the current thread.
            //
            // Why is this necessary?
            // Because equivalence is based on a multiset, not a set.
            // For example:
            //   [8, 9]      -> {8:1, 9:1}
            //   [8, 8, 9]   -> {8:2, 9:1}
            // These are NOT equivalent, so we must preserve counts.
            //
            // We use Dictionary<int, int> because:
            // - user IDs can be large (up to 1e9), so array indexing is not practical
            // - dictionary gives average O(1) insertion/update
            var frequency = new Dictionary<int, int>();

            foreach (var userId in thread)
            {
                if (frequency.TryGetValue(userId, out int currentCount))
                {
                    frequency[userId] = currentCount + 1;
                }
                else
                {
                    frequency[userId] = 1;
                }
            }

            // Step 2: Convert the frequency map into a canonical representation.
            //
            // Why do we need a canonical representation?
            // Because the original message order must be ignored.
            //
            // Example:
            //   [4,1,4,2] and [2,4,4,1]
            // both produce the same frequency map:
            //   {1:1, 2:1, 4:2}
            //
            // But dictionaries do not guarantee a stable order when enumerating entries.
            // So if we directly serialize the dictionary without sorting, equivalent threads
            // might produce different strings depending on enumeration order.
            //
            // Therefore, we sort by user ID first.
            var sortedEntries = frequency.OrderBy(entry => entry.Key);

            // Step 3: Build a string key from the sorted (userId, count) pairs.
            //
            // We need a representation that is:
            // - identical for equivalent multisets
            // - different for different multisets
            //
            // Example:
            //   {1:1, 2:1, 4:2} -> "1#1|2#1|4#2|"
            //
            // We include separators (# and |) so that values cannot accidentally merge.
            // For example, without separators:
            //   (1,11)(11,1) could become ambiguous.
            //
            // With separators, the representation is unambiguous.
            var sb = new StringBuilder();

            foreach (var entry in sortedEntries)
            {
                sb.Append(entry.Key);
                sb.Append('#');
                sb.Append(entry.Value);
                sb.Append('|');
            }

            string signature = sb.ToString();

            // Step 4: Use the signature to count pairs.
            //
            // If this signature has already been seen x times,
            // then the current thread forms x new unordered pairs:
            // - with each previous thread having the same signature.
            //
            // Example:
            // Suppose signature "1#1|2#1|4#2|" has been seen 2 times already.
            // The current thread is the 3rd one with this signature.
            // It forms 2 new pairs:
            //   (current, first)
            //   (current, second)
            //
            // This incremental counting avoids a second pass.
            if (signatureCount.TryGetValue(signature, out long seenSoFar))
            {
                pairs += seenSoFar;
                signatureCount[signature] = seenSoFar + 1;
            }
            else
            {
                signatureCount[signature] = 1;
            }
        }

        // After processing all threads, 'pairs' contains the total number of unordered
        // equivalent thread pairs.
        return pairs;
    }
}

// Demo code

var solution = new Solution();

// Example 1:
// Threads 0, 1, and 3 all have multiset {1:1, 2:1, 4:2}
// Number of pairs among 3 equivalent threads = 3 choose 2 = 3
var threads1 = new List<IList<int>>
{
    new List<int> { 4, 1, 4, 2 },
    new List<int> { 2, 4, 4, 1 },
    new List<int> { 3, 3 },
    new List<int> { 1, 2, 4, 4 },
    new List<int> { 3, 3, 3 }
};

long result1 = solution.CountEquivalentThreadPairs(threads1);
Console.WriteLine(result1); // Expected: 3

// Example 2:
// Threads 0, 3, and 5 are equivalent: [8,9], [8,9], [9,8] -> multiset {8:1, 9:1}
// They contribute 3 pairs.
// Threads 2 and 4 are both [7] -> multiset {7:1}
// They contribute 1 pair.
// Total = 4
var threads2 = new List<IList<int>>
{
    new List<int> { 8, 9 },
    new List<int> { 9, 8, 8 },
    new List<int> { 7 },
    new List<int> { 8, 9 },
    new List<int> { 7 },
    new List<int> { 9, 8 }
};

long result2 = solution.CountEquivalentThreadPairs(threads2);
Console.WriteLine(result2); // Expected: 4

// Additional small sanity check:
// [1,1,2], [2,1,1], [1,2], [2,2]
// First two are equivalent, so answer should be 1.
var threads3 = new List<IList<int>>
{
    new List<int> { 1, 1, 2 },
    new List<int> { 2, 1, 1 },
    new List<int> { 1, 2 },
    new List<int> { 2, 2 }
};

long result3 = solution.CountEquivalentThreadPairs(threads3);
Console.WriteLine(result3); // Expected: 1