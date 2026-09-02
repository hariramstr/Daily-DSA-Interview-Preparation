/*
Title: Minimum Processor Count for Deadline-Sorted Builds
Difficulty: Hard
Topic: Binary Search

Problem Description:
You are given a list of software build jobs that must be executed in the given order. The i-th job takes buildTimes[i] minutes and must finish no later than deadlines[i] minutes from time 0. You may provision k identical processors, where each processor can run at most one job at a time, and preemption is not allowed. Jobs are assigned online in the fixed order: when considering a job, you may choose any processor, but the relative order of jobs in the input cannot be changed.

Your task is to return the minimum number of processors needed so that all jobs can be completed before or at their respective deadlines.

A schedule is valid if every job starts after the previous job assigned to the same processor finishes, and each job's completion time is at most its deadline. If no number of processors from 1 to n can satisfy the deadlines, return -1.

This problem is designed around a monotonic feasibility condition: if k processors are enough, then any number greater than k is also enough. An efficient solution should combine binary search on the answer with a fast feasibility check using an appropriate data structure.

Constraints:
- 1 <= n == buildTimes.length == deadlines.length <= 200000
- 1 <= buildTimes[i] <= 1000000000
- 1 <= deadlines[i] <= 1000000000000000000
- Jobs must be considered in the given order
- Processor count can be between 1 and n

Example 1:
Input: buildTimes = [3, 2, 4, 1], deadlines = [4, 5, 8, 6]
Output: 2

Example 2:
Input: buildTimes = [5, 5, 5], deadlines = [4, 10, 15]
Output: -1
*/

using System;
using System.Collections.Generic;

public class Solution
{
    /*
    Time Complexity:
    - Binary search tries O(log n) different processor counts.
    - Each feasibility check processes all n jobs.
    - For each job, we perform one "extract minimum" and one "insert" into a min-heap of size k,
      each costing O(log k), which is at most O(log n).
    - Total: O(n log n log n) in the worst case, which is fast enough for n <= 200000.

    Space Complexity:
    - O(k) for the min-heap during a feasibility check, at most O(n).
    */
    public int MinimumProcessorCount(long[] buildTimes, long[] deadlines)
    {
        int n = buildTimes.Length;

        // First, handle the impossible case early.
        // If any single job takes longer than its own deadline, then even if that job had
        // a completely dedicated processor starting at time 0, it would still miss.
        // In that case, no answer from 1..n can work, so we return -1 immediately.
        for (int i = 0; i < n; i++)
        {
            if (buildTimes[i] > deadlines[i])
            {
                return -1;
            }
        }

        // Because feasibility is monotonic:
        // - if k processors are enough, then k+1, k+2, ... are also enough
        // we can binary search for the smallest feasible k.
        int left = 1;
        int right = n;
        int answer = -1;

        while (left <= right)
        {
            int mid = left + (right - left) / 2;

            // Check whether it is possible to schedule all jobs using exactly "mid" processors.
            if (CanSchedule(buildTimes, deadlines, mid))
            {
                // mid works, so record it as a candidate answer
                // and try to find an even smaller feasible processor count.
                answer = mid;
                right = mid - 1;
            }
            else
            {
                // mid does not work, so we need more processors.
                left = mid + 1;
            }
        }

        return answer;
    }

    private bool CanSchedule(long[] buildTimes, long[] deadlines, int k)
    {
        // Core greedy idea:
        //
        // At any moment while processing jobs in the required input order,
        // each processor has a current "available time" = when it becomes free.
        //
        // For the next job, to maximize our chance of meeting its deadline,
        // we should place it on the processor that becomes free the earliest.
        //
        // Why is this greedy choice correct?
        // Because the job's completion time becomes:
        //     earliestAvailableTime + buildTime
        // and choosing any later-available processor would only make the completion time
        // the same or worse. So if the earliest-free processor cannot meet the deadline,
        // no other processor can do better.
        //
        // Therefore, for each job:
        // 1. take the minimum available time among all processors
        // 2. schedule the job there
        // 3. compute the new finish time
        // 4. if finish time > deadline, fail immediately
        // 5. push the updated finish time back into the heap
        //
        // To support "get earliest available processor" efficiently,
        // we use a min-heap of processor available times.

        var heap = new MinHeap();

        // Initially, all k processors are free at time 0.
        // So the heap starts with k zeros.
        for (int i = 0; i < k; i++)
        {
            heap.Push(0L);
        }

        for (int i = 0; i < buildTimes.Length; i++)
        {
            // Step 1: get the processor that becomes available the earliest.
            long earliestAvailable = heap.Pop();

            // Step 2: assign the current job to that processor.
            // Since the processor is free at earliestAvailable, the job starts then
            // and finishes after buildTimes[i] minutes.
            long finishTime = earliestAvailable + buildTimes[i];

            // Step 3: verify the deadline.
            // If this job finishes after its deadline, then this processor count k is not enough.
            // Also, because we chose the earliest-free processor, no alternative processor
            // could produce an earlier finish time for this same job.
            if (finishTime > deadlines[i])
            {
                return false;
            }

            // Step 4: the processor is now next available at finishTime.
            // Put that updated availability back into the heap.
            heap.Push(finishTime);
        }

        // If every job met its deadline, then k processors are sufficient.
        return true;
    }

    private class MinHeap
    {
        private readonly List<long> _data = new();

        public int Count => _data.Count;

        public void Push(long value)
        {
            _data.Add(value);
            SiftUp(_data.Count - 1);
        }

        public long Pop()
        {
            long root = _data[0];
            int lastIndex = _data.Count - 1;
            _data[0] = _data[lastIndex];
            _data.RemoveAt(lastIndex);

            if (_data.Count > 0)
            {
                SiftDown(0);
            }

            return root;
        }

        private void SiftUp(int index)
        {
            while (index > 0)
            {
                int parent = (index - 1) / 2;

                if (_data[parent] <= _data[index])
                {
                    break;
                }

                (_data[parent], _data[index]) = (_data[index], _data[parent]);
                index = parent;
            }
        }

        private void SiftDown(int index)
        {
            int count = _data.Count;

            while (true)
            {
                int left = index * 2 + 1;
                int right = index * 2 + 2;
                int smallest = index;

                if (left < count && _data[left] < _data[smallest])
                {
                    smallest = left;
                }

                if (right < count && _data[right] < _data[smallest])
                {
                    smallest = right;
                }

                if (smallest == index)
                {
                    break;
                }

                (_data[index], _data[smallest]) = (_data[smallest], _data[index]);
                index = smallest;
            }
        }
    }
}

// Demo code

var solution = new Solution();

// Example 1
long[] buildTimes1 = { 3, 2, 4, 1 };
long[] deadlines1 = { 4, 5, 8, 6 };
int result1 = solution.MinimumProcessorCount(buildTimes1, deadlines1);
Console.WriteLine(result1); // Expected: 2

// Example 2
long[] buildTimes2 = { 5, 5, 5 };
long[] deadlines2 = { 4, 10, 15 };
int result2 = solution.MinimumProcessorCount(buildTimes2, deadlines2);
Console.WriteLine(result2); // Expected: -1

// Additional quick sanity check
long[] buildTimes3 = { 2, 2, 2, 2 };
long[] deadlines3 = { 2, 2, 4, 4 };
int result3 = solution.MinimumProcessorCount(buildTimes3, deadlines3);
Console.WriteLine(result3); // Expected: 2