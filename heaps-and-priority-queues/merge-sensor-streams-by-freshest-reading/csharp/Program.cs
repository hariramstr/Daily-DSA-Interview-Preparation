/*
Title: Merge Sensor Streams by Freshest Reading
Difficulty: Medium
Topic: Heaps and Priority Queues

Problem Description:
You are given k sensor streams. Each stream is represented as a list of readings sorted by increasing timestamp.
A reading is a pair [timestamp, value]. Different streams may contain readings at the same timestamp, and some
streams may be empty.

Your task is to produce a single merged timeline of readings using the following rule:
- Repeatedly choose the unread reading with the smallest timestamp among all streams.
- If multiple unread readings share the same timestamp, choose the one with the larger value first.
- If there is still a tie, choose the reading from the smaller stream index first.

Return the merged result as a list of triples [timestamp, value, streamIndex] in the exact order they are selected.

This problem models merging multiple already-sorted event feeds while preserving a deterministic priority rule.
An efficient solution should avoid repeatedly scanning all streams to find the next reading.

Constraints:
- 1 <= k <= 10^4
- 0 <= total number of readings across all streams <= 2 * 10^5
- 0 <= timestamp <= 10^9
- -10^9 <= value <= 10^9
- Each individual stream is sorted by nondecreasing timestamp
- The output size equals the total number of readings

Example 1:
Input: streams = [
  [[1, 5], [4, 2]],
  [[1, 7], [3, 1]],
  [[2, 9]]
]
Output: [[1, 7, 1], [1, 5, 0], [2, 9, 2], [3, 1, 1], [4, 2, 0]]

Example 2:
Input: streams = [
  [],
  [[2, 4], [2, 3], [5, 8]],
  [[2, 4]],
  [[1, 10], [6, 0]]
]
Output: [[1, 10, 3], [2, 4, 1], [2, 4, 2], [2, 3, 1], [5, 8, 1], [6, 0, 3]]
*/

using System;
using System.Collections.Generic;

public class Solution
{
    // This small record stores exactly one "current unread reading" from a stream.
    // We keep:
    // - Timestamp: used as the primary ordering key (smaller first)
    // - Value: used as the secondary ordering key when timestamps tie (larger first)
    // - StreamIndex: used as the final deterministic tie-breaker (smaller first)
    // - ReadingIndex: tells us where this reading came from inside its stream, so we can
    //   advance to the next reading from the same stream after we remove this one.
    private readonly record struct HeapEntry(int Timestamp, int Value, int StreamIndex, int ReadingIndex);

    // This comparer defines the exact priority rule required by the problem.
    // Important note:
    // PriorityQueue in .NET removes the SMALLEST priority according to the comparer.
    // So we design the comparison so that:
    // 1) smaller timestamp comes first
    // 2) for equal timestamp, larger value comes first
    // 3) for equal timestamp and value, smaller stream index comes first
    private sealed class HeapEntryComparer : IComparer<HeapEntry>
    {
        public int Compare(HeapEntry a, HeapEntry b)
        {
            int byTimestamp = a.Timestamp.CompareTo(b.Timestamp);
            if (byTimestamp != 0) return byTimestamp;

            int byValueDescending = b.Value.CompareTo(a.Value);
            if (byValueDescending != 0) return byValueDescending;

            return a.StreamIndex.CompareTo(b.StreamIndex);
        }
    }

    /*
    Time Complexity:
    Let N be the total number of readings across all streams, and k be the number of streams.
    - Each reading is inserted into the heap once and removed once.
    - Each heap operation costs O(log k), because the heap contains at most one active unread reading per stream.
    Therefore, total time complexity is O(N log k).

    Space Complexity:
    - The heap stores at most one current reading from each non-empty stream: O(k)
    - The output stores all merged readings: O(N)
    Therefore, auxiliary heap space is O(k), and total space including output is O(N).
    */
    public List<int[]> MergeSensorStreams(IList<IList<int[]>> streams)
    {
        // This list will hold the final merged order exactly as the problem asks:
        // each item is [timestamp, value, streamIndex].
        var result = new List<int[]>();

        // We use a priority queue (min-heap behavior with our custom comparer) because:
        // - At every step, we need the "best next reading" among all streams.
        // - Scanning all streams every time would be too slow.
        // - A heap lets us get the next best reading efficiently.
        //
        // The queue element and the queue priority are both HeapEntry.
        // We store the same object as both element and priority for convenience.
        var pq = new PriorityQueue<HeapEntry, HeapEntry>(new HeapEntryComparer());

        // STEP 1: Initialize the heap with the first unread reading from every non-empty stream.
        //
        // Why this works:
        // Each stream is already sorted by timestamp, so the first unread reading of a stream
        // is always the only candidate from that stream that can possibly be chosen next.
        // We never need to put all readings from a stream into the heap at once.
        for (int streamIndex = 0; streamIndex < streams.Count; streamIndex++)
        {
            var stream = streams[streamIndex];

            // If a stream is empty, it contributes nothing to the merge.
            if (stream.Count == 0)
            {
                continue;
            }

            // Take the first reading from this stream.
            int timestamp = stream[0][0];
            int value = stream[0][1];

            var entry = new HeapEntry(timestamp, value, streamIndex, 0);

            // Add it to the heap so it can compete with the first readings of other streams.
            pq.Enqueue(entry, entry);
        }

        // STEP 2: Repeatedly remove the highest-priority unread reading from the heap.
        //
        // This loop continues until there are no more unread readings left in any stream.
        while (pq.Count > 0)
        {
            // Remove the reading that should appear next in the merged output.
            //
            // Because of our comparer, this is guaranteed to be:
            // - smallest timestamp
            // - if tied, larger value
            // - if still tied, smaller stream index
            HeapEntry current = pq.Dequeue();

            // Add the chosen reading to the final answer in the required triple format.
            result.Add(new[] { current.Timestamp, current.Value, current.StreamIndex });

            // STEP 3: Advance only the stream that provided the reading we just used.
            //
            // Why only that stream?
            // Because every other stream still has the same unread front reading as before.
            // The chosen stream now needs to expose its next unread reading, if one exists.
            int nextReadingIndex = current.ReadingIndex + 1;
            var sourceStream = streams[current.StreamIndex];

            // If there is another reading in this same stream, push it into the heap.
            if (nextReadingIndex < sourceStream.Count)
            {
                int nextTimestamp = sourceStream[nextReadingIndex][0];
                int nextValue = sourceStream[nextReadingIndex][1];

                var nextEntry = new HeapEntry(nextTimestamp, nextValue, current.StreamIndex, nextReadingIndex);

                // This new reading now becomes the active unread front for that stream.
                pq.Enqueue(nextEntry, nextEntry);
            }
        }

        // When the heap is empty, all streams have been fully consumed,
        // and the merged result is complete.
        return result;
    }
}

static IList<IList<int[]>> BuildStreams(params int[][][] streams)
{
    var result = new List<IList<int[]>>();
    foreach (var stream in streams)
    {
        var list = new List<int[]>();
        foreach (var reading in stream)
        {
            list.Add(new[] { reading[0], reading[1] });
        }
        result.Add(list);
    }
    return result;
}

static string FormatResult(IList<int[]> merged)
{
    var parts = new List<string>();
    foreach (var item in merged)
    {
        parts.Add($"[{item[0]}, {item[1]}, {item[2]}]");
    }
    return "[" + string.Join(", ", parts) + "]";
}

// Demo code
var solution = new Solution();

// Example 1
var streams1 = BuildStreams(
    new[]
    {
        new[] { 1, 5 },
        new[] { 4, 2 }
    },
    new[]
    {
        new[] { 1, 7 },
        new[] { 3, 1 }
    },
    new[]
    {
        new[] { 2, 9 }
    }
);

var merged1 = solution.MergeSensorStreams(streams1);
Console.WriteLine("Example 1 Output:");
Console.WriteLine(FormatResult(merged1));
Console.WriteLine("Expected:");
Console.WriteLine("[[1, 7, 1], [1, 5, 0], [2, 9, 2], [3, 1, 1], [4, 2, 0]]");
Console.WriteLine();

// Example 2
var streams2 = BuildStreams(
    Array.Empty<int[]>(),
    new[]
    {
        new[] { 2, 4 },
        new[] { 2, 3 },
        new[] { 5, 8 }
    },
    new[]
    {
        new[] { 2, 4 }
    },
    new[]
    {
        new[] { 1, 10 },
        new[] { 6, 0 }
    }
);

var merged2 = solution.MergeSensorStreams(streams2);
Console.WriteLine("Example 2 Output:");
Console.WriteLine(FormatResult(merged2));
Console.WriteLine("Expected:");
Console.WriteLine("[[1, 10, 3], [2, 4, 1], [2, 4, 2], [2, 3, 1], [5, 8, 1], [6, 0, 3]]");
Console.WriteLine();

// Additional quick custom demo
var streams3 = BuildStreams(
    new[]
    {
        new[] { 1, 2 },
        new[] { 1, 1 },
        new[] { 3, 5 }
    },
    new[]
    {
        new[] { 1, 2 },
        new[] { 2, 9 }
    }
);

var merged3 = solution.MergeSensorStreams(streams3);
Console.WriteLine("Custom Demo Output:");
Console.WriteLine(FormatResult(merged3));