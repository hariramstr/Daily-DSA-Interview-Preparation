"""
Title: Reveal the Next Unopened Support Ticket

Problem Description:
A customer support system stores ticket IDs in the order they were created. Tickets are
numbered with positive integers, and some tickets have already been opened by agents.
You are given two integer arrays: `tickets`, containing all created ticket IDs, and
`opened`, containing the ticket IDs that have already been opened.

Every value in `opened` is guaranteed to appear in `tickets`, and no ticket ID appears
more than once in either array.

Your task is to return the smallest ticket ID from `tickets` that does not appear in
`opened`. If every ticket has already been opened, return `-1`.

Although this problem can be solved in different ways, it is designed to be approachable
with a min-heap or priority queue:
1. Place all ticket IDs into a min-heap.
2. Keep the opened IDs in a set.
3. Repeatedly remove the smallest ticket until you find one that has not been opened.

Constraints:
- 1 <= tickets.length <= 10^5
- 0 <= opened.length <= tickets.length
- 1 <= tickets[i], opened[i] <= 10^9
- All values in `tickets` are distinct.
- All values in `opened` are distinct.
- Every value in `opened` also exists in `tickets`.

Example 1:
Input: tickets = [42, 17, 90, 23], opened = [17, 42]
Output: 23
Explanation: The unopened ticket IDs are 23 and 90. The smallest is 23.

Example 2:
Input: tickets = [8, 3, 11], opened = [3, 8, 11]
Output: -1
Explanation: All tickets have already been opened, so there is no valid answer.
"""

from typing import List
import heapq


class Solution:
    def next_unopened_ticket(self, tickets: List[int], opened: List[int]) -> int:
        """
        Return the smallest ticket ID that exists in tickets but not in opened.

        Args:
            tickets: A list of all created ticket IDs. All values are distinct.
            opened: A list of ticket IDs that have already been opened. All values are
                distinct and each one is guaranteed to appear in tickets.

        Returns:
            The smallest unopened ticket ID, or -1 if every ticket has already been opened.

        Time complexity:
            O(n + m + n log n) in the heap-copy approach used here, where:
            - n = len(tickets)
            - m = len(opened)
            More specifically:
            - Building the set of opened tickets takes O(m)
            - Copying tickets into a heap list takes O(n)
            - Heapifying takes O(n)
            - In the worst case, popping all tickets takes O(n log n)

        Space complexity:
            O(n + m)
            - O(n) for the heap copy of tickets
            - O(m) for the set of opened tickets
        """
        # Step 1: Convert the list of already opened ticket IDs into a set.
        #
        # Why use a set?
        # - We need to repeatedly ask: "Has this ticket already been opened?"
        # - A set gives average O(1) membership checks.
        # - If we used a list instead, each membership check could take O(m),
        #   which would be too slow for large inputs.
        opened_set = set(opened)

        # Step 2: Create a min-heap containing all ticket IDs.
        #
        # Why a min-heap?
        # - The problem asks for the smallest ticket ID that has not been opened.
        # - A min-heap always lets us remove the current smallest element efficiently.
        #
        # Important detail:
        # - We make a copy of `tickets` so we do not modify the original input list.
        # - `heapq.heapify(...)` transforms the list into a valid min-heap in O(n) time.
        min_heap = tickets[:]
        heapq.heapify(min_heap)

        # Step 3: Repeatedly remove the smallest ticket from the heap.
        #
        # At each step:
        # - The popped value is the smallest remaining ticket ID.
        # - If it is NOT in opened_set, then it is the smallest unopened ticket,
        #   so we can return it immediately.
        # - If it IS in opened_set, we skip it and continue.
        #
        # This works because the heap gives ticket IDs in ascending order.
        while min_heap:
            smallest_ticket = heapq.heappop(min_heap)

            # If this ticket has not been opened, we have found the answer.
            if smallest_ticket not in opened_set:
                return smallest_ticket

        # Step 4: If we emptied the heap without finding any unopened ticket,
        # then every ticket was already opened.
        return -1


if __name__ == "__main__":
    # Create an instance of the solution class.
    solution = Solution()

    # Example 1 from the problem statement:
    # tickets = [42, 17, 90, 23]
    # opened = [17, 42]
    #
    # Unopened tickets are [23, 90], and the smallest is 23.
    tickets_1 = [42, 17, 90, 23]
    opened_1 = [17, 42]
    result_1 = solution.next_unopened_ticket(tickets_1, opened_1)
    print(result_1)  # Expected: 23

    # Example 2 from the problem statement:
    # tickets = [8, 3, 11]
    # opened = [3, 8, 11]
    #
    # All tickets are opened, so the answer is -1.
    tickets_2 = [8, 3, 11]
    opened_2 = [3, 8, 11]
    result_2 = solution.next_unopened_ticket(tickets_2, opened_2)
    print(result_2)  # Expected: -1