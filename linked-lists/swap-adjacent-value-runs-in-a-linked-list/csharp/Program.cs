/*
Title: Swap Adjacent Value Runs in a Linked List
Difficulty: Medium
Topic: Linked Lists

Problem Description:
You are given the head of a singly linked list representing a stream of event codes.
Consecutive nodes with the same value form a run. Your task is to rearrange the list
by swapping every two adjacent runs, while preserving the internal order of nodes inside each run.

A run is a maximal contiguous block of nodes with equal values. For example, in the list

4 -> 4 -> 1 -> 1 -> 1 -> 3 -> 2 -> 2

the runs are:
[4,4], [1,1,1], [3], and [2,2]

After swapping adjacent runs, the result becomes:
[1,1,1] -> [4,4] -> [2,2] -> [3]

If the list contains an odd number of runs, the final run stays in its original position.
You must relink existing nodes and should not create a new list of copied values.
The goal is to return the head of the modified list.

Constraints:
- The number of nodes in the list is in the range [0, 2 * 10^5].
- Node values are in the range [-10^9, 10^9].
- The list is singly linked.
- Your solution should run in O(n) time.
- Extra space should be O(1), excluding recursion stack and input storage.
*/

using System;
using System.Collections.Generic;

public class ListNode
{
    public int val;
    public ListNode? next;

    public ListNode(int val = 0, ListNode? next = null)
    {
        this.val = val;
        this.next = next;
    }
}

public class Solution
{
    /*
    Time Complexity: O(n)
    - We scan through the linked list once.
    - Each node is visited a constant number of times while identifying runs and reconnecting pointers.

    Space Complexity: O(1)
    - We only use a fixed number of pointer variables.
    - We do not allocate another linked list of copied nodes.
    */
    public ListNode? SwapAdjacentRuns(ListNode? head)
    {
        // If the list is empty, there is nothing to rearrange.
        // Returning null is correct because an empty list stays empty.
        if (head == null)
        {
            return null;
        }

        // We use a dummy node before the real head.
        // Why this is helpful:
        // - Swapping the very first two runs may change the head of the list.
        // - A dummy node gives us a stable "node before the current work area".
        // - This avoids special-case code for the first pair of runs.
        ListNode dummy = new ListNode(0, head);

        // prevTail always points to the node that should connect to the next processed section.
        // At the beginning, that is the dummy node.
        ListNode prevTail = dummy;

        // current points to the first node of the next run pair we want to process.
        ListNode? current = head;

        // We repeatedly process the list run by run.
        while (current != null)
        {
            // -----------------------------
            // STEP 1: Identify the first run
            // -----------------------------
            // The first run starts at "current".
            ListNode firstStart = current;
            ListNode firstEnd = current;

            // Move firstEnd forward while the next node has the same value.
            // This finds the maximal contiguous block of equal values.
            while (firstEnd.next != null && firstEnd.next.val == firstStart.val)
            {
                firstEnd = firstEnd.next;
            }

            // After this loop:
            // - firstStart is the first node of run #1
            // - firstEnd is the last node of run #1
            // - firstEnd.next is the first node after run #1 (possibly null)

            // If there is no second run, then we have an odd number of runs
            // and this final run should remain in place.
            if (firstEnd.next == null)
            {
                // Connect the already processed part to this final untouched run.
                prevTail.next = firstStart;

                // Since this is the last run, we are done.
                break;
            }

            // ------------------------------
            // STEP 2: Identify the second run
            // ------------------------------
            // The second run starts immediately after the first run.
            ListNode secondStart = firstEnd.next;
            ListNode secondEnd = secondStart;

            // Again, extend to the end of this run by following equal values.
            while (secondEnd.next != null && secondEnd.next.val == secondStart.val)
            {
                secondEnd = secondEnd.next;
            }

            // Save the node after the second run.
            // This is important because once we start rewiring pointers,
            // we still need to know where the rest of the list begins.
            ListNode? nextPairStart = secondEnd.next;

            // -----------------------------------------
            // STEP 3: Swap the two runs by relinking them
            // -----------------------------------------
            // Before swap:
            // prevTail -> [first run] -> [second run] -> nextPairStart
            //
            // After swap:
            // prevTail -> [second run] -> [first run] -> nextPairStart
            //
            // Important:
            // We are NOT reversing nodes inside a run.
            // We are only moving whole run blocks.

            // Connect the already processed prefix to the second run,
            // because after swapping, the second run comes first.
            prevTail.next = secondStart;

            // The end of the second run should now point to the start of the first run.
            secondEnd.next = firstStart;

            // The end of the first run should point to whatever comes after the second run.
            firstEnd.next = nextPairStart;

            // ---------------------------------------------------------
            // STEP 4: Advance pointers to continue processing the list
            // ---------------------------------------------------------
            // After the swap, the first run is now the second block in this swapped pair.
            // Therefore its end becomes the tail of the fully processed portion.
            prevTail = firstEnd;

            // Continue from the first node after the swapped pair.
            current = nextPairStart;
        }

        // The real head may have changed after swapping the first two runs,
        // so we return dummy.next instead of the original head.
        return dummy.next;
    }
}

static ListNode? BuildList(int[] values)
{
    ListNode dummy = new ListNode(0);
    ListNode tail = dummy;

    foreach (int value in values)
    {
        tail.next = new ListNode(value);
        tail = tail.next;
    }

    return dummy.next;
}

static string ListToString(ListNode? head)
{
    if (head == null)
    {
        return "[]";
    }

    List<int> values = new List<int>();
    ListNode? current = head;

    while (current != null)
    {
        values.Add(current.val);
        current = current.next;
    }

    return "[" + string.Join(",", values) + "]";
}

// Demo code
Solution solution = new Solution();

// Example 1:
// Input:  [4,4,1,1,1,3,2,2]
// Runs:   [4,4], [1,1,1], [3], [2,2]
// Output: [1,1,1,4,4,2,2,3]
ListNode? example1 = BuildList(new[] { 4, 4, 1, 1, 1, 3, 2, 2 });
ListNode? result1 = solution.SwapAdjacentRuns(example1);
Console.WriteLine("Example 1 Output: " + ListToString(result1));

// Example 2:
// Input:  [7,7,5,6,6,6,9]
// Runs:   [7,7], [5], [6,6,6], [9]
// Output: [5,7,7,9,6,6,6]
ListNode? example2 = BuildList(new[] { 7, 7, 5, 6, 6, 6, 9 });
ListNode? result2 = solution.SwapAdjacentRuns(example2);
Console.WriteLine("Example 2 Output: " + ListToString(result2));

// Additional demo: odd number of runs
// Input:  [1,1,2,3,3,4,4,4,5]
// Runs:   [1,1], [2], [3,3], [4,4,4], [5]
// Output: [2,1,1,4,4,4,3,3,5]
ListNode? example3 = BuildList(new[] { 1, 1, 2, 3, 3, 4, 4, 4, 5 });
ListNode? result3 = solution.SwapAdjacentRuns(example3);
Console.WriteLine("Example 3 Output: " + ListToString(result3));

// Additional demo: single run only
ListNode? example4 = BuildList(new[] { 8, 8, 8 });
ListNode? result4 = solution.SwapAdjacentRuns(example4);
Console.WriteLine("Example 4 Output: " + ListToString(result4));

// Additional demo: empty list
ListNode? example5 = BuildList(Array.Empty<int>());
ListNode? result5 = solution.SwapAdjacentRuns(example5);
Console.WriteLine("Example 5 Output: " + ListToString(result5));