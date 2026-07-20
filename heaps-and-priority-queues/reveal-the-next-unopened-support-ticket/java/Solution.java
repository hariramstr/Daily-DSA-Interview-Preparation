import java.util.*;

/*
 * Title: Reveal the Next Unopened Support Ticket
 * Difficulty: Easy
 * Topic: Heaps and Priority Queues
 *
 * Problem Description:
 * A customer support system stores ticket IDs in the order they were created.
 * Tickets are numbered with positive integers, and some tickets have already
 * been opened by agents. You are given two integer arrays: tickets, containing
 * all created ticket IDs, and opened, containing the ticket IDs that have
 * already been opened.
 *
 * Every value in opened is guaranteed to appear in tickets, and no ticket ID
 * appears more than once in either array.
 *
 * Your task is to return the smallest ticket ID from tickets that does not
 * appear in opened. If every ticket has already been opened, return -1.
 *
 * A heap-based approach:
 * 1. Place all ticket IDs into a min-heap (priority queue).
 * 2. Put all opened ticket IDs into a set for fast lookup.
 * 3. Repeatedly remove the smallest ticket from the heap.
 * 4. The first ticket not found in the opened set is the answer.
 * 5. If the heap becomes empty, return -1.
 *
 * Constraints:
 * - 1 <= tickets.length <= 10^5
 * - 0 <= opened.length <= tickets.length
 * - 1 <= tickets[i], opened[i] <= 10^9
 * - All values in tickets are distinct.
 * - All values in opened are distinct.
 * - Every value in opened also exists in tickets.
 *
 * Example 1:
 * Input: tickets = [42, 17, 90, 23], opened = [17, 42]
 * Output: 23
 * Explanation:
 * The unopened ticket IDs are 23 and 90. The smallest is 23.
 *
 * Example 2:
 * Input: tickets = [8, 3, 11], opened = [3, 8, 11]
 * Output: -1
 * Explanation:
 * All tickets have already been opened, so there is no valid answer.
 */

public class Solution {

    /**
     * Finds the smallest ticket ID that has not been opened yet by using
     * a min-heap (priority queue) and a hash set.
     *
     * Step-by-step idea:
     * 1. Store all opened ticket IDs in a HashSet so we can quickly check
     *    whether a ticket has already been opened.
     * 2. Store all ticket IDs in a PriorityQueue, which always gives us
     *    the smallest remaining ticket ID first.
     * 3. Repeatedly remove the smallest ticket from the heap.
     * 4. If that ticket is not in the opened set, it is the smallest
     *    unopened ticket, so return it immediately.
     * 5. If all tickets are removed and every one of them was opened,
     *    return -1.
     *
     * @param tickets the array containing all created ticket IDs
     * @param opened the array containing ticket IDs that have already been opened
     * @return the smallest unopened ticket ID, or -1 if every ticket has been opened
     *
     * Time complexity: O(n log n + m), where n = tickets.length and m = opened.length.
     * Building the heap takes O(n log n) through repeated insertions, building the set
     * takes O(m), and polling from the heap in the worst case takes O(n log n) overall.
     *
     * Space complexity: O(n + m), for the priority queue and hash set.
     */
    public int revealNextUnopenedTicket(int[] tickets, int[] opened) {
        // Create a hash set to store all opened ticket IDs.
        // Why a set?
        // Because checking whether a value exists in a HashSet is very fast on average: O(1).
        Set<Integer> openedSet = new HashSet<>();

        // Add every opened ticket ID into the set.
        for (int ticketId : opened) {
            openedSet.add(ticketId);
        }

        // Create a min-heap (PriorityQueue in Java).
        // A PriorityQueue with integers naturally keeps the smallest value at the top.
        PriorityQueue<Integer> minHeap = new PriorityQueue<>();

        // Insert every ticket ID into the min-heap.
        // After this loop, the heap can always give us the smallest ticket ID remaining.
        for (int ticketId : tickets) {
            minHeap.offer(ticketId);
        }

        // Keep removing the smallest ticket until the heap becomes empty.
        while (!minHeap.isEmpty()) {
            // Get and remove the smallest ticket ID currently in the heap.
            int smallestTicket = minHeap.poll();

            // Check whether this smallest ticket has already been opened.
            // If it has NOT been opened, then this is exactly the answer we want:
            // the smallest ticket ID from tickets that does not appear in opened.
            if (!openedSet.contains(smallestTicket)) {
                return smallestTicket;
            }

            // If it was already opened, we simply continue to the next smallest ticket.
        }

        // If we reach this point, every ticket in the heap was found in the opened set.
        // That means all tickets have already been opened.
        return -1;
    }

    /**
     * A second public method that solves the same problem.
     * This method simply delegates to the heap-based implementation above.
     * It is included to keep the solution beginner-friendly and to expose
     * the algorithm clearly as a public method inside the Solution class.
     *
     * @param tickets the array containing all created ticket IDs
     * @param opened the array containing ticket IDs that have already been opened
     * @return the smallest unopened ticket ID, or -1 if every ticket has been opened
     *
     * Time complexity: O(n log n + m), where n = tickets.length and m = opened.length.
     * Space complexity: O(n + m).
     */
    public int findSmallestUnopenedTicket(int[] tickets, int[] opened) {
        return revealNextUnopenedTicket(tickets, opened);
    }

    /**
     * Demonstrates the solution using the sample inputs from the problem statement.
     *
     * It also prints the expected outputs so it is easy to verify correctness.
     *
     * @param args command-line arguments (not used)
     * @return nothing
     *
     * Time complexity: O(1) for the demonstration itself, excluding the called algorithm.
     * Space complexity: O(1), excluding the called algorithm.
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Example 1:
        // tickets = [42, 17, 90, 23]
        // opened  = [17, 42]
        //
        // All tickets sorted would be: 17, 23, 42, 90
        // Opened tickets are 17 and 42
        // The smallest ticket not opened is 23
        int[] tickets1 = {42, 17, 90, 23};
        int[] opened1 = {17, 42};
        int result1 = solution.revealNextUnopenedTicket(tickets1, opened1);
        System.out.println("Example 1 Result: " + result1);
        System.out.println("Expected: 23");

        // Example 2:
        // tickets = [8, 3, 11]
        // opened  = [3, 8, 11]
        //
        // All tickets have already been opened, so the answer is -1
        int[] tickets2 = {8, 3, 11};
        int[] opened2 = {3, 8, 11};
        int result2 = solution.revealNextUnopenedTicket(tickets2, opened2);
        System.out.println("Example 2 Result: " + result2);
        System.out.println("Expected: -1");

        // Additional quick demonstration:
        // tickets = [100, 5, 70, 1]
        // opened  = [5]
        //
        // Sorted tickets: 1, 5, 70, 100
        // Since 1 is not opened, answer should be 1
        int[] tickets3 = {100, 5, 70, 1};
        int[] opened3 = {5};
        int result3 = solution.findSmallestUnopenedTicket(tickets3, opened3);
        System.out.println("Additional Example Result: " + result3);
        System.out.println("Expected: 1");
    }
}