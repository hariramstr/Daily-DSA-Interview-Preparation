/*
Title: Reverse Nodes in Even-Length ID Groups

Problem Description:
You are given the head of a singly linked list representing a stream of record IDs.
Starting from the head, split the list into consecutive groups whose intended sizes
are 1, 2, 3, 4, and so on. The last group may contain fewer nodes than its intended
size if the list runs out.

Your task is to reverse the nodes inside every group whose actual length is even,
while leaving odd-length groups unchanged. The groups must remain in the same overall
order, and only the node links should be modified. You may not create a second list
of all values and rebuild the answer from scratch.

For example, if the list is grouped as [a], [b, c], [d, e, f], [g, h, i, j], then
the second and fourth groups should be reversed because their lengths are 2 and 4,
both even. If the final group has fewer nodes than expected, use its actual size
when deciding whether to reverse it.

Return the head of the modified linked list.

Constraints:
- The number of nodes in the list is in the range [1, 100000].
- Node values are integers in the range [-1000000000, 1000000000].
- The list is singly linked.
- Aim for O(n) time complexity.
- Extra space should be O(1), excluding recursion stack and input storage.
*/

using System;
using System.Collections.Generic;
using System.Text;

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
    - Every node is visited a constant number of times.
    - We scan each group to determine its actual size.
    - If the group length is even, we reverse that group in-place.
    - Overall, the total work across all groups is linear in the number of nodes.

    Space Complexity: O(1)
    - We only use a few pointer variables.
    - No extra array/list is used to rebuild the linked list.
    - Reversal is done by changing links in-place.
    */
    public ListNode? ReverseEvenLengthGroups(ListNode? head)
    {
        // If the list is empty or has only one node, there is nothing meaningful to change.
        // Returning early keeps the code simple and avoids unnecessary pointer work.
        if (head == null || head.next == null)
        {
            return head;
        }

        // "dummy" is a very common linked-list technique.
        // Why use it?
        // - It gives us a stable node before the real head.
        // - This makes reconnection logic easier, especially if a group near the front changes.
        // - Even though the first group here has length 1 and will never be reversed,
        //   using a dummy node still keeps the code cleaner and more beginner-friendly.
        ListNode dummy = new ListNode(0, head);

        // "prevGroupTail" will always point to the node immediately BEFORE the current group.
        // This is extremely useful because:
        // - If we reverse the current group, we need to connect the previous part of the list
        //   to the new head of the reversed group.
        // - If we do not reverse the group, we still need to move this pointer forward to the
        //   end of the current group.
        ListNode prevGroupTail = dummy;

        // The intended group sizes are 1, 2, 3, 4, ...
        int intendedGroupSize = 1;

        // Continue as long as there are still nodes left to process after prevGroupTail.
        while (prevGroupTail.next != null)
        {
            // The first node of the current group starts right after prevGroupTail.
            ListNode groupStart = prevGroupTail.next;

            // We now need to determine the ACTUAL size of this group.
            // Why actual size instead of intended size?
            // Because the list may end early, and the final group can be smaller than intended.
            int actualGroupSize = 0;

            // "groupEnd" will walk through the nodes of the current group.
            // After this loop:
            // - actualGroupSize tells us how many nodes are really in the group
            // - groupEnd points to the last node of the current group
            ListNode? groupEnd = groupStart;
            while (groupEnd != null && actualGroupSize < intendedGroupSize)
            {
                actualGroupSize++;
                if (actualGroupSize == intendedGroupSize || groupEnd.next == null)
                {
                    break;
                }
                groupEnd = groupEnd.next;
            }

            // The node after the current group.
            // We save this before any reversal because once we start changing links,
            // we still need to know where the remainder of the list begins.
            ListNode? nextGroupStart = groupEnd!.next;

            // If the actual group size is even, we reverse this group in-place.
            if (actualGroupSize % 2 == 0)
            {
                // To reverse a linked-list segment safely, a very convenient trick is:
                // start "prev" at the node AFTER the segment.
                //
                // Why does that help?
                // Suppose the segment is:
                //   a -> b -> c -> nextGroupStart
                //
                // If we reverse it using:
                //   prev = nextGroupStart
                // then:
                //   c.next becomes nextGroupStart
                //   b.next becomes c
                //   a.next becomes b
                //
                // This automatically reconnects the reversed segment to the rest of the list.
                ListNode? prev = nextGroupStart;
                ListNode? curr = groupStart;

                // Reverse exactly "actualGroupSize" nodes.
                // We do not go until null because this is only a segment, not necessarily the whole list.
                for (int i = 0; i < actualGroupSize; i++)
                {
                    // Save the next node before changing the current node's next pointer.
                    // If we do not save it first, we would lose access to the rest of the segment.
                    ListNode? tempNext = curr!.next;

                    // Reverse the pointer:
                    // current node now points backward (or to nextGroupStart for the first processed node).
                    curr.next = prev;

                    // Advance both pointers for the next iteration of reversal.
                    prev = curr;
                    curr = tempNext;
                }

                // After reversal:
                // - "prev" is the new head of the reversed group
                // - "groupStart" became the tail of the reversed group
                //
                // We now connect the previous part of the list to the new head.
                prevGroupTail.next = prev;

                // Since groupStart is now the tail of the reversed group,
                // it becomes the "previous group tail" for the next iteration.
                prevGroupTail = groupStart;
            }
            else
            {
                // If the group length is odd, we leave it exactly as it is.
                //
                // But we still must move prevGroupTail to the end of this group,
                // otherwise the next iteration would start from the wrong place.
                prevGroupTail = groupEnd;
            }

            // Move to the next intended group size: 1, 2, 3, 4, ...
            intendedGroupSize++;
        }

        // The real head may or may not have changed.
        // Returning dummy.next always gives the correct final head.
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
    StringBuilder sb = new StringBuilder();
    sb.Append('[');

    ListNode? current = head;
    while (current != null)
    {
        sb.Append(current.val);
        if (current.next != null)
        {
            sb.Append(',');
        }
        current = current.next;
    }

    sb.Append(']');
    return sb.ToString();
}

var solution = new Solution();

// Example 1
// Input: [5,8,3,9,1,4]
// Groups: [5], [8,3], [9,1,4]
// Group lengths: 1 (odd), 2 (even), 3 (odd)
// Expected output: [5,3,8,9,1,4]
var head1 = BuildList(new[] { 5, 8, 3, 9, 1, 4 });
var result1 = solution.ReverseEvenLengthGroups(head1);
Console.WriteLine(ListToString(result1));

// Example 2
// Input: [1,2,3,4,5,6,7,8,9]
// Groups: [1], [2,3], [4,5,6], [7,8,9]
// Intended sizes: 1,2,3,4 but last actual size is 3
// Even groups: only [2,3]
// Expected output: [1,3,2,4,5,6,7,8,9] if grouped by actual nodes as shown,
// but the problem statement's output says [1,3,2,4,5,6,9,8,7] while also describing
// the last group as actual length 3 and unchanged. The description and output conflict.
// According to the stated rule "reverse only even actual-length groups", the correct result is:
// [1,3,2,4,5,6,7,8,9]
var head2 = BuildList(new[] { 1, 2, 3, 4, 5, 6, 7, 8, 9 });
var result2 = solution.ReverseEvenLengthGroups(head2);
Console.WriteLine(ListToString(result2));

// Additional demo matching the classic behavior where a final group can be even and reversed.
// Input: [1,1,0,6]
// Groups: [1], [1,0], [6]
// Reverse only [1,0] => [0,1]
// Output: [1,0,1,6]
var head3 = BuildList(new[] { 1, 1, 0, 6 });
var result3 = solution.ReverseEvenLengthGroups(head3);
Console.WriteLine(ListToString(result3));