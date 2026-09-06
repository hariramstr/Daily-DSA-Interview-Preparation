/*
Title: Merge Release Streams by Highest Current Severity
Difficulty: Medium
Topic: Heaps and Priority Queues

Problem Description:
A company receives incident reports from multiple release streams. Each stream is already sorted in non-increasing order by severity score, where a larger score means a more critical issue. You are given a list of integer arrays `streams`, where `streams[i]` contains the severity scores for stream `i`, already sorted from highest to lowest. Your task is to merge all streams into one global processing order.

At every step, you may only take the next unprocessed report from any stream. Return the merged list of all severity scores in non-increasing order. If two available reports have the same severity, choose the one from the smaller stream index first. If there is still a tie, choose the one that appears earlier within its stream.

Design an algorithm that is efficient when the number of streams is large and each stream may have different length. A solution that repeatedly scans every stream for the next best item will be too slow.

Constraints:
- 1 <= streams.length <= 10^5
- 0 <= streams[i].length <= 10^5
- 0 <= severity <= 10^9
- Each streams[i] is sorted in non-increasing order
- The total number of reports across all streams does not exceed 2 * 10^5

Example 1:
Input: streams = [[9,7,3],[10,6],[8,8,1]]
Output: [10,9,8,8,7,6,3,1]

Example 2:
Input: streams = [[5,5,2],[],[5,4],[6]]
Output: [6,5,5,5,4,2]
*/

using System;
using System.Collections.Generic;
using System.Linq;

public class Solution
{
    private readonly record struct HeapItem(int Severity, int StreamIndex, int ElementIndex);

    private sealed class HeapItemComparer : IComparer<HeapItem>
    {
        public int Compare(HeapItem x, HeapItem y)
        {
            // We want the "best" candidate to come out first from PriorityQueue.
            // In .NET PriorityQueue, the smallest priority is dequeued first.
            // So we define "smaller" as:
            // 1) Higher severity first  => larger severity should compare as smaller
            // 2) Smaller stream index first
            // 3) Smaller element index first
            if (x.Severity != y.Severity)
            {
                return y.Severity.CompareTo(x.Severity);
            }

            if (x.StreamIndex != y.StreamIndex)
            {
                return x.StreamIndex.CompareTo(y.StreamIndex);
            }

            return x.ElementIndex.CompareTo(y.ElementIndex);
        }
    }

    /*
    Time Complexity:
    - Let k be the number of streams.
    - Let n be the total number of reports across all streams.
    - We push each report into the heap exactly once and pop each report exactly once.
    - Each heap operation costs O(log k), because the heap contains at most one current candidate per stream.
    - Total time complexity: O(n log k)

    Space Complexity:
    - The heap stores at most one item from each stream at a time, so O(k)
    - The output list stores all n merged values, so O(n)
    - Total auxiliary heap space: O(k), total including output: O(n)
    */
    public int[] MergeReleaseStreams(int[][] streams)
    {
        // This list will store the final merged order of all severity scores.
        // We know every report must appear exactly once in the answer.
        var merged = new List<int>();

        // This priority queue is the key data structure that makes the solution efficient.
        //
        // Why use a priority queue (heap)?
        // --------------------------------
        // At any moment, the only reports we are allowed to choose from are the "current front"
        // of each stream: the next unprocessed item in that stream.
        //
        // If we scanned all streams every time to find the best next report, that would be too slow.
        // Instead, we keep exactly those current front items in a heap, so we can always extract
        // the best available report quickly.
        //
        // The heap will contain at most one item per stream:
        // - the next unprocessed report from that stream
        //
        // Each heap item stores:
        // - Severity: the actual severity score
        // - StreamIndex: which stream this report came from
        // - ElementIndex: where it is inside that stream
        //
        // We use the same HeapItem as both the element and the priority.
        // The custom comparer ensures the queue follows the exact problem rules.
        var pq = new PriorityQueue<HeapItem, HeapItem>(new HeapItemComparer());

        // Step 1: Initialize the heap with the first report from every non-empty stream.
        //
        // Why is this correct?
        // --------------------
        // At the beginning, the only available report from each stream is its first element,
        // because we may only take the next unprocessed report from a stream.
        //
        // So the global next answer must be one of these first elements.
        for (int streamIndex = 0; streamIndex < streams.Length; streamIndex++)
        {
            if (streams[streamIndex].Length > 0)
            {
                var first = new HeapItem(
                    Severity: streams[streamIndex][0],
                    StreamIndex: streamIndex,
                    ElementIndex: 0
                );

                pq.Enqueue(first, first);
            }
        }

        // Step 2: Repeatedly take the best currently available report.
        //
        // Each loop iteration does exactly one thing:
        // - remove the highest-priority available report
        // - append its severity to the answer
        // - advance that same stream by one step
        //
        // This is the standard "k-way merge" pattern.
        while (pq.Count > 0)
        {
            // Remove the best available candidate according to:
            // 1) highest severity
            // 2) smaller stream index if severities tie
            // 3) smaller element index if still tied
            //
            // Note:
            // In practice, because the heap contains at most one current item per stream,
            // the third tie-breaker almost never changes anything across different streams.
            // But we include it to exactly match the requested ordering rule.
            HeapItem current = pq.Dequeue();

            // Add the chosen severity to the final merged result.
            merged.Add(current.Severity);

            // We have now consumed one report from current.StreamIndex.
            // So the next available report from that same stream becomes eligible.
            int nextElementIndex = current.ElementIndex + 1;

            // If that stream still has more reports, push the next one into the heap.
            //
            // Why only this next one?
            // -----------------------
            // Because the problem says we may only take the next unprocessed report from a stream.
            // We are NOT allowed to skip ahead deeper into the stream.
            //
            // Since each stream is already sorted in non-increasing order, once we consume one item,
            // the next item becomes the new front candidate for that stream.
            if (nextElementIndex < streams[current.StreamIndex].Length)
            {
                var next = new HeapItem(
                    Severity: streams[current.StreamIndex][nextElementIndex],
                    StreamIndex: current.StreamIndex,
                    ElementIndex: nextElementIndex
                );

                pq.Enqueue(next, next);
            }
        }

        return merged.ToArray();
    }
}

// Demo code

var solution = new Solution();

// Example 1
int[][] streams1 =
{
    new[] { 9, 7, 3 },
    new[] { 10, 6 },
    new[] { 8, 8, 1 }
};

int[] result1 = solution.MergeReleaseStreams(streams1);
Console.WriteLine("Example 1 Output: [" + string.Join(",", result1) + "]");

// Example 2
int[][] streams2 =
{
    new[] { 5, 5, 2 },
    Array.Empty<int>(),
    new[] { 5, 4 },
    new[] { 6 }
};

int[] result2 = solution.MergeReleaseStreams(streams2);
Console.WriteLine("Example 2 Output: [" + string.Join(",", result2) + "]");

// Additional quick demo
int[][] streams3 =
{
    new[] { 12, 11, 1 },
    new[] { 12, 10 },
    new[] { 12 },
    Array.Empty<int>(),
    new[] { 9, 9, 8 }
};

int[] result3 = solution.MergeReleaseStreams(streams3);
Console.WriteLine("Example 3 Output: [" + string.Join(",", result3) + "]");