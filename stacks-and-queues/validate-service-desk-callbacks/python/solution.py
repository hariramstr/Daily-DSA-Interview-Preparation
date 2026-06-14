"""
Title: Validate Service Desk Callbacks

Problem Description:
A customer support system receives calls identified by unique integer IDs. During the day,
every incoming call is appended to a waiting line in the order it arrives. At certain times,
the system records a callback log showing the order in which calls were actually handled.
Because the support desk always serves the oldest waiting call first, except that some calls
may still remain unhandled by the end of the day, the callback log should be a valid prefix
of the queue's natural processing order.

You are given two integer arrays: arrivals and handled. The array arrivals lists call IDs
in the exact order they entered the waiting line. The array handled lists call IDs in the
exact order the support desk claims to have handled them. Determine whether handled could
be a valid callback log.

A callback log is valid if each handled call appears at the front of the queue at the moment
it is processed, and every handled call must have appeared earlier in arrivals. Calls are
unique, and a handled call cannot appear more than once.

Return true if handled is valid, otherwise return false.

Constraints:
- 1 <= arrivals.length <= 100000
- 0 <= handled.length <= arrivals.length
- 1 <= arrivals[i], handled[i] <= 1000000000
- All values in arrivals are distinct
- All values in handled are distinct

Example 1:
Input: arrivals = [10, 20, 30, 40], handled = [10, 20]
Output: true

Example 2:
Input: arrivals = [10, 20, 30, 40], handled = [10, 30]
Output: false
"""

from typing import List


class Solution:
    def validate_callbacks(self, arrivals: List[int], handled: List[int]) -> bool:
        """
        Determine whether the handled list is a valid FIFO processing prefix of arrivals.

        Args:
            arrivals: List of unique call IDs in the exact order they entered the queue.
            handled: List of unique call IDs in the exact order they were supposedly handled.

        Returns:
            True if handled is a valid callback log, otherwise False.

        Time complexity:
            O(k), where k = len(handled), because we only compare the handled prefix
            against the corresponding prefix of arrivals.

        Space complexity:
            O(1), ignoring input storage, because we use only a few variables.
        """
        # In a FIFO queue, the only valid order of handling is exactly the same as the
        # arrival order, starting from the front.
        #
        # Since some calls may remain unhandled, the handled list does NOT need to contain
        # every arrival. However, it MUST match the beginning (prefix) of arrivals.
        #
        # That means:
        #   arrivals = [10, 20, 30, 40]
        #   handled  = [10, 20]      -> valid, because it matches the first two arrivals
        #   handled  = [10, 30]      -> invalid, because 20 would still be at the front
        #
        # So the task becomes:
        #   "Is handled exactly equal to the first len(handled) elements of arrivals?"

        # If handled is longer than arrivals, it cannot possibly be valid because the system
        # cannot handle more unique calls than actually arrived.
        if len(handled) > len(arrivals):
            return False

        # Walk through each handled call and compare it with the call that should be at the
        # front of the queue at that moment.
        #
        # Why this works:
        # - Before any handling happens, the front of the queue is arrivals[0].
        # - After one valid handling, the front becomes arrivals[1].
        # - After two valid handlings, the front becomes arrivals[2].
        # - And so on.
        #
        # Therefore, the i-th handled call must equal arrivals[i].
        for index in range(len(handled)):
            # If the claimed handled call does not match the expected front call,
            # then the callback log breaks FIFO order and is invalid.
            if handled[index] != arrivals[index]:
                return False

        # If every handled call matched the corresponding arrival at the front of the queue,
        # then the handled list is a valid prefix of FIFO processing.
        return True

    def is_valid_callback_log(self, arrivals: List[int], handled: List[int]) -> bool:
        """
        Wrapper method that validates the callback log.

        Args:
            arrivals: List of unique call IDs in arrival order.
            handled: List of unique call IDs in claimed handling order.

        Returns:
            True if the callback log is valid, otherwise False.

        Time complexity:
            O(k), where k = len(handled).

        Space complexity:
            O(1).
        """
        # This wrapper exists to provide a clear, beginner-friendly method name
        # while still keeping the core logic in a dedicated helper method.
        return self.validate_callbacks(arrivals, handled)


if __name__ == "__main__":
    solution = Solution()

    # Example 1
    arrivals_1 = [10, 20, 30, 40]
    handled_1 = [10, 20]
    result_1 = solution.is_valid_callback_log(arrivals_1, handled_1)
    print(result_1)  # Expected: True

    # Example 2
    arrivals_2 = [10, 20, 30, 40]
    handled_2 = [10, 30]
    result_2 = solution.is_valid_callback_log(arrivals_2, handled_2)
    print(result_2)  # Expected: False

    # Additional quick checks
    arrivals_3 = [1, 2, 3]
    handled_3 = []
    print(solution.is_valid_callback_log(arrivals_3, handled_3))  # Expected: True

    arrivals_4 = [5, 6, 7]
    handled_4 = [5, 6, 7]
    print(solution.is_valid_callback_log(arrivals_4, handled_4))  # Expected: True

    arrivals_5 = [5, 6, 7]
    handled_5 = [6]
    print(solution.is_valid_callback_log(arrivals_5, handled_5))  # Expected: False