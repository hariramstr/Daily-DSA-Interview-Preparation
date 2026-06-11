/*
Title: Remove Duplicate Stops from a Sorted Route
Difficulty: Easy
Topic: Linked Lists

Problem Description:
A city transit system stores the stops of a bus route in a singly linked list. The stop IDs are sorted in non-decreasing order because nearby route planning software groups identical stop IDs together. Sometimes duplicate stop entries appear due to data import issues, and you need to clean the route.

Given the head of a singly linked list where each node contains an integer stop ID, remove duplicate nodes so that each stop ID appears only once in the final list. Since the list is already sorted, all duplicates of the same value will appear next to each other. You must modify the linked list in place by updating next pointers and return the head of the cleaned list.

If the list is empty, return null. If the list has only one node, it should be returned unchanged.

Your goal is to keep the first occurrence of each stop ID and remove any immediately repeated copies that follow it.

Constraints:
- The number of nodes in the list is in the range [0, 300].
- -1000 <= Node.val <= 1000
- The linked list is sorted in non-decreasing order.
- Use O(1) extra space, excluding the input list.

Example 1:
Input: head = [4,4,7,7,7,9,12,12]
Output: [4,7,9,12]
Explanation: Repeated stop IDs are adjacent, so each group is reduced to a single node.

Example 2:
Input: head = [1,2,2,3,5,5,8]
Output: [1,2,3,5,8]
Explanation: The first copy of each stop ID is kept, and later duplicates are skipped.

This problem tests careful pointer manipulation on linked lists and the ability to take advantage of sorted input.
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
    - We visit each node at most once while scanning through the linked list.

    Space Complexity: O(1)
    - We do not allocate extra data structures that grow with input size.
    - We only use a small fixed number of pointers/variables.
    */
    public ListNode? DeleteDuplicates(ListNode? head)
    {
        // Step 1:
        // Handle the simplest edge case first: an empty list.
        // If head is null, there are no nodes to process, so the cleaned list is also null.
        // Returning immediately keeps the code safe and avoids null reference issues later.
        if (head == null)
        {
            return null;
        }

        // Step 2:
        // Create a pointer named "current" that will walk through the linked list.
        // We start at the head because we want to preserve the first occurrence of each value.
        // Since the list is sorted, any duplicates of current.val must appear directly after current.
        ListNode current = head;

        // Step 3:
        // Continue processing while there is a next node to compare against.
        // We need current.next to exist because the duplicate check compares:
        // current.val with current.next.val
        while (current.next != null)
        {
            // Step 4:
            // Check whether the current node and the next node have the same value.
            // Because the list is sorted, equal values appear in a consecutive block.
            // That means if current.val == current.next.val, then current.next is a duplicate
            // of the value we already decided to keep at "current".
            if (current.val == current.next.val)
            {
                // Step 5:
                // Remove the duplicate node by changing the "next" pointer.
                // Instead of pointing to the duplicate node, current.next will skip over it
                // and point directly to the node after the duplicate.
                //
                // Example:
                // current -> [7] -> [7] -> [7] -> [9]
                // After one skip:
                // current -> [7] -------> [7] -> [9]
                //
                // Why this works:
                // In a singly linked list, removing a node is done by bypassing it.
                // We do not need to move current forward yet, because there may be more duplicates
                // immediately after the one we just skipped.
                current.next = current.next.next;
            }
            else
            {
                // Step 6:
                // If the values are different, then current.next is not a duplicate.
                // This means the current value's duplicate block (if any) is finished,
                // and we can safely move forward to the next distinct value.
                current = current.next;
            }
        }

        // Step 7:
        // Return the original head pointer.
        // The list has been modified in place, so head now points to the cleaned route.
        return head;
    }
}

static ListNode? BuildList(int[] values)
{
    if (values.Length == 0)
    {
        return null;
    }

    ListNode head = new ListNode(values[0]);
    ListNode current = head;

    for (int i = 1; i < values.Length; i++)
    {
        current.next = new ListNode(values[i]);
        current = current.next;
    }

    return head;
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

var solution = new Solution();

// Demo 1: Example 1
var route1 = BuildList(new[] { 4, 4, 7, 7, 7, 9, 12, 12 });
var cleaned1 = solution.DeleteDuplicates(route1);
Console.WriteLine("Example 1 Output: " + ListToString(cleaned1)); // Expected: [4,7,9,12]

// Demo 2: Example 2
var route2 = BuildList(new[] { 1, 2, 2, 3, 5, 5, 8 });
var cleaned2 = solution.DeleteDuplicates(route2);
Console.WriteLine("Example 2 Output: " + ListToString(cleaned2)); // Expected: [1,2,3,5,8]

// Demo 3: Empty list
var route3 = BuildList(Array.Empty<int>());
var cleaned3 = solution.DeleteDuplicates(route3);
Console.WriteLine("Empty List Output: " + ListToString(cleaned3)); // Expected: []

// Demo 4: Single node
var route4 = BuildList(new[] { 42 });
var cleaned4 = solution.DeleteDuplicates(route4);
Console.WriteLine("Single Node Output: " + ListToString(cleaned4)); // Expected: [42]

// Demo 5: All duplicates
var route5 = BuildList(new[] { 6, 6, 6, 6 });
var cleaned5 = solution.DeleteDuplicates(route5);
Console.WriteLine("All Duplicates Output: " + ListToString(cleaned5)); // Expected: [6]

// Demo 6: No duplicates
var route6 = BuildList(new[] { 1, 3, 5, 7 });
var cleaned6 = solution.DeleteDuplicates(route6);
Console.WriteLine("No Duplicates Output: " + ListToString(cleaned6)); // Expected: [1,3,5,7]