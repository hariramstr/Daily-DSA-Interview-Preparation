"""
Title: Detect the First Fully Reconciled Invoice Pair

Problem Description:
You are given a list of invoice records in the order they were received by an
accounting system. Each record is represented as a pair [vendorId, amount].
A vendor may issue multiple invoices, and the same amount may appear many times
across different vendors.

Two records form a fully reconciled pair if they belong to the same vendor and
their amounts sum to exactly 0. For example, an invoice of +120 and a later
correction of -120 from the same vendor form a reconciled pair.

Your task is to return the earliest record index j such that record j completes
at least one fully reconciled pair with some earlier record i from the same
vendor. If multiple earlier records could pair with j, any one of them is
acceptable, but the completed pair must be the first one that becomes possible
while scanning from left to right.

Return the pair of indices [i, j]. If no such pair exists, return [-1, -1].
Indices are 0-based.

A record cannot be paired with itself. Multiple identical records may exist,
and each record should be treated as an independent entry. The challenge is to
detect the first completed reconciliation efficiently using hashing rather than
checking all previous records.

Constraints:
- 1 <= records.length <= 200000
- records[i].length == 2
- 1 <= vendorId <= 1000000000
- -1000000000 <= amount <= 1000000000
"""

from typing import Dict, List, Tuple


class Solution:
    def first_fully_reconciled_pair(self, records: List[List[int]]) -> List[int]:
        """
        Find the earliest pair of indices [i, j] such that:
        - i < j
        - records[i] and records[j] have the same vendorId
        - their amounts sum to 0

        The scan is performed from left to right, and the first index j that
        completes any valid pair is returned immediately.

        Args:
            records: A list of [vendorId, amount] invoice records.

        Returns:
            A list [i, j] representing the first completed reconciled pair,
            or [-1, -1] if no such pair exists.

        Time complexity:
            O(n), where n is the number of records, because each record is
            processed once and each hash table operation is O(1) on average.

        Space complexity:
            O(n) in the worst case, if no pair is found early and we store
            many previously seen records.
        """
        # We need to efficiently answer this question for each current record j:
        # "Have we already seen an earlier record i from the SAME vendor whose
        # amount is exactly the negative of the current amount?"
        #
        # A brute-force approach would compare each record with all previous ones,
        # which would take O(n^2) time and would be too slow for up to 200,000 records.
        #
        # Instead, we use hashing so that each lookup is fast.
        #
        # Key idea:
        # For a current record (vendor, amount), the only earlier record that can
        # reconcile with it must be (vendor, -amount).
        #
        # Therefore, while scanning left to right, we store previously seen records
        # in a dictionary:
        #
        #     seen[(vendorId, amount)] = earliest index where this exact pair appeared
        #
        # Why store the earliest index?
        # - The problem says that if multiple earlier records could pair with j,
        #   any one is acceptable.
        # - Storing the earliest occurrence is simple and deterministic.
        # - Most importantly, the earliest j is what matters. Since we return as
        #   soon as we find the first valid j, correctness is preserved.
        seen: Dict[Tuple[int, int], int] = {}

        # Process records in the exact order they were received.
        for j, record in enumerate(records):
            vendor_id, amount = record

            # To reconcile with the current record, we need an earlier record
            # from the same vendor whose amount is the additive inverse.
            #
            # Example:
            # current = [7, -100]
            # needed earlier = [7, 100]
            needed_key: Tuple[int, int] = (vendor_id, -amount)

            # If such a record has already been seen, then the current index j
            # is the FIRST moment (while scanning left to right) that this pair
            # becomes complete, because we are visiting indices in increasing order.
            #
            # Returning immediately guarantees that j is the earliest possible
            # completion index.
            if needed_key in seen:
                return [seen[needed_key], j]

            # If no match exists yet, store the current record for future records.
            #
            # Important detail:
            # We only store the first occurrence of each exact (vendor, amount).
            # This keeps the earlier index stable and avoids overwriting it.
            #
            # Example:
            # records = [[5, 10], [5, 10], [5, -10]]
            # Either index 0 or 1 is acceptable for pairing with index 2.
            # By keeping the first one, we return [0, 2].
            current_key: Tuple[int, int] = (vendor_id, amount)
            if current_key not in seen:
                seen[current_key] = j

        # If we finish scanning all records without finding any valid pair,
        # then no fully reconciled pair exists.
        return [-1, -1]


if __name__ == "__main__":
    solution = Solution()

    # Example 1:
    # Index 0: [7, 100]   -> store (7, 100): 0
    # Index 1: [3, 50]    -> store (3, 50): 1
    # Index 2: [7, -100]  -> need (7, 100), found at 0 -> answer [0, 2]
    records1: List[List[int]] = [[7, 100], [3, 50], [7, -100], [7, 100], [3, -20]]
    result1: List[int] = solution.first_fully_reconciled_pair(records1)
    print("Example 1 result:", result1)  # Expected: [0, 2]

    # Example 2:
    # Index 0: [5, 40]    -> store (5, 40): 0
    # Index 1: [5, 10]    -> store (5, 10): 1
    # Index 2: [8, -40]   -> need (8, 40), not found; store (8, -40): 2
    # Index 3: [5, -10]   -> need (5, 10), found at 1 -> answer [1, 3]
    records2: List[List[int]] = [[5, 40], [5, 10], [8, -40], [5, -10], [8, 40]]
    result2: List[int] = solution.first_fully_reconciled_pair(records2)
    print("Example 2 result:", result2)  # Expected: [1, 3]

    # Additional sample: no pair exists
    records3: List[List[int]] = [[1, 5], [2, -5], [1, 7], [2, 3]]
    result3: List[int] = solution.first_fully_reconciled_pair(records3)
    print("Example 3 result:", result3)  # Expected: [-1, -1]