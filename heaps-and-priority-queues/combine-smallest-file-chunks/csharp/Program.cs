/*
Title: Combine Smallest File Chunks

Problem Description:
You are given an array `chunks` where `chunks[i]` is the size of the `i-th` file chunk waiting to be merged into one final archive.
In one operation, you must take the two smallest available chunks, merge them, and pay a cost equal to the sum of their sizes.
The merged chunk is then added back to the pool of available chunks.
Continue until only one chunk remains.

Return the total cost of all merge operations.

This models a common systems task: repeatedly combining small pieces of data where each intermediate merge also creates a new piece
that may be merged again later. To minimize the total cost, you should always merge the smallest available chunks first.

Example 1:
Input: chunks = [4, 3, 2, 6]
Output: 29

Explanation:
- Merge 2 and 3 -> cost 5, chunks become [4, 5, 6]
- Merge 4 and 5 -> cost 9, chunks become [6, 9]
- Merge 6 and 9 -> cost 15
Total cost = 5 + 9 + 15 = 29

Example 2:
Input: chunks = [10]
Output: 0

Important Note:
A single initial sort is not enough, because every merge creates a new chunk that must be inserted back into the remaining values
in the correct order efficiently. That is why a min-heap / priority queue is the right data structure.
*/

using System;
using System.Collections.Generic;

public class Solution
{
    /*
    Time Complexity:
    - Building the priority queue with all n chunks takes O(n log n) in this implementation
      because we enqueue each item one by one.
    - Each merge operation performs:
        2 dequeues + 1 enqueue
      and there are exactly (n - 1) merges.
      Each heap operation costs O(log n).
    - Total: O(n log n)

    Space Complexity:
    - The priority queue stores up to n chunk sizes.
    - Total: O(n)
    */
    public long MinimumMergeCost(int[] chunks)
    {
        // If there is only one chunk, no merge is needed.
        // That means the total cost is zero because we never perform any operation.
        if (chunks == null || chunks.Length <= 1)
        {
            return 0L;
        }

        // We use PriorityQueue<long, long> as a min-heap.
        //
        // Why a priority queue?
        // The problem repeatedly asks us to take the two smallest available chunks.
        // A min-heap is specifically designed for this:
        // - Insert a value efficiently
        // - Remove the smallest value efficiently
        //
        // We store the chunk size as both:
        // - the element
        // - the priority
        //
        // Using long is important because:
        // - each chunk can be as large as 1,000,000,000
        // - repeated merges can make values much larger
        // - the final total cost may exceed the range of int
        var minHeap = new PriorityQueue<long, long>();

        // Put every chunk into the min-heap.
        // After this loop, the heap contains all currently available chunks.
        foreach (int chunk in chunks)
        {
            long size = chunk;
            minHeap.Enqueue(size, size);
        }

        // This variable accumulates the total cost of all merge operations.
        long totalCost = 0L;

        // We continue merging until only one chunk remains.
        //
        // Why stop at one?
        // Because the goal is to combine everything into one final archive.
        // If more than one chunk remains, we are not done yet.
        while (minHeap.Count > 1)
        {
            // Step 1: Remove the smallest available chunk.
            //
            // This is necessary because the greedy strategy for minimizing total cost
            // is to always merge the two smallest chunks first.
            long firstSmallest = minHeap.Dequeue();

            // Step 2: Remove the second smallest available chunk.
            //
            // Together with the first smallest, these are the two chunks we must merge now.
            long secondSmallest = minHeap.Dequeue();

            // Step 3: Compute the cost of merging these two chunks.
            //
            // The problem states that the cost of one merge is the sum of the two chunk sizes.
            long mergedSize = firstSmallest + secondSmallest;

            // Step 4: Add this merge cost to the running total.
            //
            // Every merge contributes to the final answer, so we accumulate it here.
            totalCost += mergedSize;

            // Step 5: Put the merged chunk back into the heap.
            //
            // Why do we reinsert it?
            // Because the newly created chunk may need to be merged again later.
            // It becomes part of the pool of available chunks.
            minHeap.Enqueue(mergedSize, mergedSize);
        }

        // When the loop ends, exactly one chunk remains.
        // At that point, totalCost contains the minimum possible total merge cost.
        return totalCost;
    }
}

// Demo code

var solution = new Solution();

// Example 1
int[] chunks1 = { 4, 3, 2, 6 };
long result1 = solution.MinimumMergeCost(chunks1);
Console.WriteLine($"Input: [{string.Join(", ", chunks1)}]");
Console.WriteLine($"Output: {result1}");
Console.WriteLine("Expected: 29");
Console.WriteLine();

// Example 2
int[] chunks2 = { 10 };
long result2 = solution.MinimumMergeCost(chunks2);
Console.WriteLine($"Input: [{string.Join(", ", chunks2)}]");
Console.WriteLine($"Output: {result2}");
Console.WriteLine("Expected: 0");
Console.WriteLine();

// Additional demo
int[] chunks3 = { 1, 2, 3, 4, 5 };
long result3 = solution.MinimumMergeCost(chunks3);
Console.WriteLine($"Input: [{string.Join(", ", chunks3)}]");
Console.WriteLine($"Output: {result3}");