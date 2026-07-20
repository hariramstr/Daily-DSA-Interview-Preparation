/*
Title: Reveal the Next Unopened Support Ticket

Problem Description:
A customer support system stores ticket IDs in the order they were created. Tickets are numbered with positive integers, and some tickets have already been opened by agents. You are given two integer arrays: tickets, containing all created ticket IDs, and opened, containing the ticket IDs that have already been opened. Every value in opened is guaranteed to appear in tickets, and no ticket ID appears more than once in either array.

Your task is to return the smallest ticket ID from tickets that does not appear in opened. If every ticket has already been opened, return -1.

A natural approach is:
1. Put all ticket IDs into a min-heap / priority queue so we can always access the smallest ticket first.
2. Put all opened ticket IDs into a hash set so we can check quickly whether a ticket has already been opened.
3. Repeatedly remove the smallest ticket from the heap until we find one that is not in the opened set.
4. If the heap becomes empty, then every ticket was already opened, so return -1.

Constraints:
- 1 <= tickets.length <= 10^5
- 0 <= opened.length <= tickets.length
- 1 <= tickets[i], opened[i] <= 10^9
- All values in tickets are distinct.
- All values in opened are distinct.
- Every value in opened also exists in tickets.

Example 1:
Input: tickets = [42, 17, 90, 23], opened = [17, 42]
Output: 23
Explanation: The unopened ticket IDs are 23 and 90. The smallest is 23.

Example 2:
Input: tickets = [8, 3, 11], opened = [3, 8, 11]
Output: -1
Explanation: All tickets have already been opened, so there is no valid answer.
*/

using System;
using System.Collections.Generic;

public class Solution
{
    /*
    Time Complexity:
    - Building the hash set from opened: O(m), where m = opened.Length
    - Inserting all tickets into the priority queue: O(n log n), where n = tickets.Length
    - Removing tickets from the priority queue until we find the answer: in the worst case O(n log n)
    - Overall: O(n log n + m)

    Space Complexity:
    - Hash set for opened tickets: O(m)
    - Priority queue containing all tickets: O(n)
    - Overall: O(n + m)
    */
    public int RevealNextUnopenedTicket(int[] tickets, int[] opened)
    {
        // Step 1:
        // Create a hash set containing all ticket IDs that have already been opened.
        //
        // Why do we use a HashSet?
        // - We need to repeatedly ask: "Has this ticket already been opened?"
        // - A HashSet gives average O(1) lookup time.
        // - This is much faster than searching through the opened array every time.
        //
        // Example:
        // opened = [17, 42]
        // openedSet will contain {17, 42}
        var openedSet = new HashSet<int>(opened);

        // Step 2:
        // Create a priority queue (min-heap behavior) for all ticket IDs.
        //
        // Why a priority queue?
        // - We want the smallest ticket ID that has NOT been opened.
        // - A min-heap lets us repeatedly remove the smallest available ticket efficiently.
        //
        // In .NET's PriorityQueue<TElement, TPriority>:
        // - TElement is the stored value
        // - TPriority determines ordering
        //
        // Here, we store the ticket ID as both the element and the priority.
        // That means the smallest ticket ID will come out first.
        var minHeap = new PriorityQueue<int, int>();

        // Step 3:
        // Insert every ticket ID into the min-heap.
        //
        // After this loop, the heap contains all tickets,
        // and calling Dequeue() will always return the smallest remaining ticket.
        foreach (int ticket in tickets)
        {
            minHeap.Enqueue(ticket, ticket);
        }

        // Step 4:
        // Repeatedly remove the smallest ticket from the heap.
        //
        // For each smallest ticket:
        // - If it is NOT in openedSet, then it is unopened.
        // - Because we are processing tickets in ascending order,
        //   the first unopened one we find must be the smallest unopened ticket.
        //
        // This is the key correctness idea:
        // - The heap gives tickets in sorted order from smallest to largest.
        // - The first ticket not found in openedSet is therefore the answer.
        while (minHeap.Count > 0)
        {
            // Remove the current smallest ticket ID.
            int smallestTicket = minHeap.Dequeue();

            // Check whether this ticket has already been opened.
            if (!openedSet.Contains(smallestTicket))
            {
                // This ticket is not opened.
                // Since it is the smallest remaining ticket in the heap,
                // it is the smallest unopened ticket overall.
                return smallestTicket;
            }

            // If we reach here, the ticket was already opened,
            // so we continue checking the next smallest ticket.
        }

        // Step 5:
        // If we emptied the heap without finding any unopened ticket,
        // then every ticket in tickets appeared in opened.
        // Therefore, the correct answer is -1.
        return -1;
    }
}

// Demo code:
// Create sample inputs, call the solution, and print results.

// Example 1:
// tickets = [42, 17, 90, 23], opened = [17, 42]
// Sorted tickets would be [17, 23, 42, 90]
// 17 is opened, 23 is not opened => answer should be 23
var solution = new Solution();

int[] tickets1 = { 42, 17, 90, 23 };
int[] opened1 = { 17, 42 };
int result1 = solution.RevealNextUnopenedTicket(tickets1, opened1);
Console.WriteLine(result1); // Expected: 23

// Example 2:
// tickets = [8, 3, 11], opened = [3, 8, 11]
// All tickets are opened => answer should be -1
int[] tickets2 = { 8, 3, 11 };
int[] opened2 = { 3, 8, 11 };
int result2 = solution.RevealNextUnopenedTicket(tickets2, opened2);
Console.WriteLine(result2); // Expected: -1

// Additional quick demo:
// tickets = [100, 5, 20], opened = []
// Smallest unopened ticket is 5
int[] tickets3 = { 100, 5, 20 };
int[] opened3 = Array.Empty<int>();
int result3 = solution.RevealNextUnopenedTicket(tickets3, opened3);
Console.WriteLine(result3); // Expected: 5