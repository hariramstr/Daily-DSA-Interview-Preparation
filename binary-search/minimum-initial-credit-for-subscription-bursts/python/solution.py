"""
Title: Minimum Initial Credit for Subscription Bursts

Problem Description:
A streaming platform processes a fixed sequence of billing events over several days.
You are given an integer array transactions where transactions[i] represents the net
credit change on day i: a positive value adds credit to the account, and a negative
value consumes credit.

The platform may optionally activate at most k emergency top-ups. Each top-up can be
inserted immediately before any day and adds exactly x credits to the current balance.
Multiple top-ups cannot be used on the same day, and unused top-ups are allowed.

Your task is to compute the minimum initial credit S such that, by choosing when to use
at most k top-ups, the account balance never becomes negative at any point during the
sequence.

In other words, starting with balance S, process the days from left to right. Before
processing day i, if you still have available top-ups, you may add x once. After that,
apply transactions[i]. The balance must remain at least 0 after every day.

Return the smallest possible S.

This is a decision/optimization problem intended to be solved with binary search on the
answer. A candidate value S is feasible if there exists some strategy using at most k
top-ups that keeps the running balance nonnegative for the entire array.
"""

from typing import List


class Solution:
    def _can_keep_nonnegative(self, transactions: List[int], k: int, x: int, initial: int) -> bool:
        """
        Check whether a given initial credit is sufficient.

        We simulate the days from left to right. Before each day, if the current balance
        is too small to survive that day's transaction, the only useful action is to use
        a top-up immediately before that day (if available). Because each top-up gives a
        fixed amount x and can only be used before a day, a greedy strategy is optimal:
        use a top-up exactly when it is necessary, and never earlier.

        Args:
            transactions: List of daily net credit changes.
            k: Maximum number of top-ups allowed.
            x: Credit added by one top-up.
            initial: Candidate initial credit to test.

        Returns:
            True if there exists a strategy using at most k top-ups that keeps the
            balance nonnegative after every day, otherwise False.

        Time complexity:
            O(n), where n is len(transactions)

        Space complexity:
            O(1)
        """
        # This variable stores the current account balance as we process the days.
        balance: int = initial

        # This variable counts how many top-ups we still have available.
        remaining_topups: int = k

        # Process each day in order.
        for change in transactions:
            # We need the balance AFTER applying today's transaction to stay >= 0.
            #
            # If today's transaction is negative, then we need:
            #     balance + change >= 0
            # which is equivalent to:
            #     balance >= -change
            #
            # If this is not true, then the only possible rescue is to use a top-up
            # immediately before this day. Since multiple top-ups on the same day are
            # forbidden, we can use at most one here.
            #
            # Important greedy idea:
            # - If we can already survive the day, using a top-up now is never better
            #   than saving it for later, because the top-up amount is fixed and future
            #   days may need it more.
            # - If we cannot survive the day, then using a top-up now is mandatory.
            if balance + change < 0:
                if remaining_topups == 0:
                    # No top-ups left, so this initial credit fails.
                    return False

                # Use one top-up immediately before this day.
                balance += x
                remaining_topups -= 1

                # Even after using the top-up, if the balance would still go negative,
                # then this initial credit is impossible.
                if balance + change < 0:
                    return False

            # Apply today's transaction.
            balance += change

        # If we finished all days without going negative, the initial credit works.
        return True

    def minimum_initial_credit(self, transactions: List[int], k: int, x: int) -> int:
        """
        Compute the minimum initial credit needed so that the balance never becomes
        negative, using at most k top-ups of size x.

        The answer is monotonic:
        - If some initial credit S works, then any larger initial credit also works.
        This monotonicity allows binary search on S.

        We first find a high bound that is guaranteed to work by doubling until the
        feasibility check succeeds. Then we binary search the smallest feasible value.

        Args:
            transactions: List of daily net credit changes.
            k: Maximum number of top-ups allowed.
            x: Credit added by one top-up.

        Returns:
            The minimum initial credit as an integer.

        Time complexity:
            O(n log A), where n is len(transactions) and A is the answer range

        Space complexity:
            O(1)
        """
        # Lower bound:
        # Initial credit cannot be negative, so 0 is always a valid lower search bound.
        left: int = 0

        # Upper bound:
        # We do not know the answer in advance, so we start with 1 and keep doubling
        # until we find a value that is feasible.
        #
        # This is a standard technique when the answer range is not explicitly known.
        right: int = 1

        while not self._can_keep_nonnegative(transactions, k, x, right):
            right *= 2

        # Standard binary search for the first feasible value.
        #
        # Invariant:
        # - left is a candidate lower bound
        # - right is known to be feasible
        while left < right:
            mid: int = (left + right) // 2

            if self._can_keep_nonnegative(transactions, k, x, mid):
                # mid works, so the answer is <= mid
                right = mid
            else:
                # mid does not work, so the answer must be > mid
                left = mid + 1

        return left


if __name__ == "__main__":
    solution = Solution()

    # Example 1
    transactions_1: List[int] = [-4, 3, -6, 2]
    k_1: int = 1
    x_1: int = 5
    result_1: int = solution.minimum_initial_credit(transactions_1, k_1, x_1)
    print("Example 1 result:", result_1)  # Expected: 2

    # Example 2
    transactions_2: List[int] = [-8, -2, 5, -7]
    k_2: int = 2
    x_2: int = 4
    result_2: int = solution.minimum_initial_credit(transactions_2, k_2, x_2)
    print("Example 2 result:", result_2)  # Expected: 6

    # Additional small sanity checks
    transactions_3: List[int] = [5, -3, -2]
    k_3: int = 0
    x_3: int = 10
    result_3: int = solution.minimum_initial_credit(transactions_3, k_3, x_3)
    print("Sanity check 1 result:", result_3)  # Expected: 0

    transactions_4: List[int] = [-10]
    k_4: int = 1
    x_4: int = 7
    result_4: int = solution.minimum_initial_credit(transactions_4, k_4, x_4)
    print("Sanity check 2 result:", result_4)  # Expected: 3