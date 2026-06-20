/*
Title: Process Servers by Highest Failure Count

Problem Description:
You are given a stream of server failure reports from a monitoring system. Each report contains
the name of a server that just failed. After processing all reports, the operations team wants
to repair servers in priority order.

A server with a higher total number of failures should be repaired earlier.
If two servers have the same number of failures, the server with the lexicographically smaller
name should come first.

Your task is to return the names of the top k servers in the exact order they should be repaired.

This problem is designed for an efficient solution using a heap or priority queue.
A straightforward full sort may work for small inputs, but the intended approach should scale
well when the number of distinct servers is large and k is much smaller than that number.

Return a list of up to k server names. If there are fewer than k distinct servers, return all
of them in the required order.

Constraints:
- 1 <= reports.length <= 200000
- 1 <= reports[i].length <= 30
- reports[i] consists of lowercase English letters, digits, and hyphens
- 1 <= k <= 200000
- Server names are case-sensitive only if your language treats strings that way; assume all input
  names are already normalized

Example 1:
Input: reports = ["db-1","api-2","db-1","cache-7","api-2","db-1"], k = 2
Output: ["db-1","api-2"]

Example 2:
Input: reports = ["node-b","node-a","node-b","node-a","node-c"], k = 3
Output: ["node-a","node-b","node-c"]
*/

using System;
using System.Collections.Generic;
using System.Linq;

public class Solution
{
    private sealed class HeapPriorityComparer : IComparer<(int Count, string Name)>
    {
        public int Compare((int Count, string Name) x, (int Count, string Name) y)
        {
            int countComparison = x.Count.CompareTo(y.Count);
            if (countComparison != 0)
            {
                return countComparison;
            }

            return string.CompareOrdinal(y.Name, x.Name);
        }
    }

    /*
    Time Complexity:
    - Counting frequencies: O(n), where n is the number of reports
    - Maintaining a heap of size at most k for m distinct servers: O(m log k)
    - Extracting and reversing results: O(k log k) in total due to heap removals
    Overall: O(n + m log k)

    Space Complexity:
    - Frequency dictionary: O(m)
    - Heap: O(k)
    - Result list: O(k)
    Overall: O(m + k)
    */
    public IList<string> TopKServers(string[] reports, int k)
    {
        // This dictionary will store:
        // key   = server name
        // value = how many times that server appears in the failure report stream
        //
        // Why do we need this?
        // Because the final repair priority depends on the TOTAL number of failures per server,
        // not on the order in which reports arrived.
        var frequency = new Dictionary<string, int>();

        // Step 1: Count how many failures each server has.
        //
        // We scan every report exactly once.
        // If the server has been seen before, increase its count.
        // Otherwise, start its count at 1.
        foreach (var server in reports)
        {
            if (frequency.ContainsKey(server))
            {
                frequency[server]++;
            }
            else
            {
                frequency[server] = 1;
            }
        }

        // If k is larger than the number of distinct servers, we only need all distinct servers.
        // This prevents unnecessary heap growth and keeps the logic precise.
        k = Math.Min(k, frequency.Count);

        // We use a PriorityQueue as a min-heap that keeps only the best k servers seen so far.
        //
        // Important idea:
        // We want the final answer ordered by:
        //   1) higher failure count first
        //   2) if tied, lexicographically smaller name first
        //
        // To efficiently keep only the top k items, we store the "worst" item among the current
        // top k at the root of the min-heap.
        //
        // That means the heap root should be:
        //   - smaller count = worse
        //   - if counts tie, lexicographically larger name = worse
        //
        // Why is lexicographically larger worse on ties?
        // Because the required output says lexicographically smaller should come earlier,
        // so among equal counts, the larger name should be removed first if we exceed k.
        //
        // We achieve that by using a custom comparer for the priority tuple:
        // priority = (count ascending, name descending)
        //
        // With this setup:
        // - the root is always the current worst candidate among the kept servers
        // - when heap size exceeds k, we remove that worst candidate
        var heap = new PriorityQueue<string, (int Count, string Name)>(new HeapPriorityComparer());

        // Step 2: Process each distinct server and maintain only the best k in the heap.
        foreach (var entry in frequency)
        {
            string serverName = entry.Key;
            int count = entry.Value;

            // Add the current server into the heap.
            //
            // The element is the server name itself.
            // The priority contains the information needed to compare repair priority.
            heap.Enqueue(serverName, (count, serverName));

            // If the heap now contains more than k servers,
            // remove exactly one server: the worst one currently in the heap.
            //
            // This is the key optimization:
            // instead of sorting all distinct servers, we keep only the top k candidates.
            if (heap.Count > k)
            {
                heap.Dequeue();
            }
        }

        // Step 3: Extract servers from the heap.
        //
        // Because this is a min-heap of the "worst among the kept items",
        // removing from it gives servers from worst to best within the top k.
        //
        // But the required final answer must be from best to worst.
        // So we collect them first, then reverse the list.
        var result = new List<string>();

        while (heap.Count > 0)
        {
            result.Add(heap.Dequeue());
        }

        // Reverse because extraction order is currently:
        //   worst -> best
        // and we need:
        //   best -> worst
        result.Reverse();

        return result;
    }
}

// Demo code

var solution = new Solution();

// Example 1
string[] reports1 = { "db-1", "api-2", "db-1", "cache-7", "api-2", "db-1" };
int k1 = 2;
var answer1 = solution.TopKServers(reports1, k1);
Console.WriteLine("Example 1 Output: [" + string.Join(",", answer1.Select(x => $"\"{x}\"")) + "]");

// Example 2
string[] reports2 = { "node-b", "node-a", "node-b", "node-a", "node-c" };
int k2 = 3;
var answer2 = solution.TopKServers(reports2, k2);
Console.WriteLine("Example 2 Output: [" + string.Join(",", answer2.Select(x => $"\"{x}\"")) + "]");

// Additional demo: fewer distinct servers than k
string[] reports3 = { "web-1", "web-1", "db-2" };
int k3 = 5;
var answer3 = solution.TopKServers(reports3, k3);
Console.WriteLine("Additional Demo Output: [" + string.Join(",", answer3.Select(x => $"\"{x}\"")) + "]");