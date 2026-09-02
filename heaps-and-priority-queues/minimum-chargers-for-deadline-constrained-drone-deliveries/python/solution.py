from bisect import bisect_left
from typing import List, Tuple


"""
Title: Minimum Chargers for Deadline-Constrained Drone Deliveries

Problem Description:
A company operates a fleet of identical drones from a single depot. Each delivery request
is described by three integers: start[i], end[i], and charge[i]. The drone assigned to
request i must occupy one charging dock continuously from time start[i] until time end[i]
(inclusive of start, exclusive of end), and the dock must provide at least charge[i] units
of charging capacity while that request is active. A charging dock can serve at most one
drone at a time, but its installed capacity is fixed for the entire day and may be reused
by multiple non-overlapping requests.

You may install any number of docks. The cost of a dock equals its capacity. Your goal is
to schedule all requests and choose dock capacities so that every request is assigned to
some dock whose capacity is at least the request's required charge, while minimizing the
total installation cost across all docks.

Return the minimum possible total cost.

Constraints:
- 1 <= n <= 200000
- 1 <= start[i] < end[i] <= 10^9
- 1 <= charge[i] <= 10^9
- All values are integers.
"""


class FenwickMin:
    """Fenwick tree supporting prefix minimum queries on reversed indices."""

    def __init__(self, size: int) -> None:
        """
        Initialize the Fenwick tree.

        Args:
            size: Number of positions.

        Returns:
            None

        Time complexity:
            O(size)

        Space complexity:
            O(size)
        """
        self.n: int = size
        self.inf: int = 10**30
        self.tree: List[int] = [self.inf] * (size + 1)

    def update(self, index: int, value: int) -> None:
        """
        Apply tree[index] = min(tree[index], value).

        Args:
            index: 1-based Fenwick index.
            value: Value to merge by minimum.

        Returns:
            None

        Time complexity:
            O(log n)

        Space complexity:
            O(1)
        """
        while index <= self.n:
            if value < self.tree[index]:
                self.tree[index] = value
            index += index & -index

    def query(self, index: int) -> int:
        """
        Return minimum value in prefix [1..index].

        Args:
            index: 1-based Fenwick index.

        Returns:
            Minimum value in the prefix, or inf if no value exists.

        Time complexity:
            O(log n)

        Space complexity:
            O(1)
        """
        result: int = self.inf
        while index > 0:
            if self.tree[index] < result:
                result = self.tree[index]
            index -= index & -index
        return result


class Solution:
    def min_total_cost(self, requests: List[List[int]]) -> int:
        """
        Compute the minimum total installed dock capacity.

        The key idea is a dynamic programming reduction:
        - Sort requests by end time.
        - Let dp[i] be the minimum total cost to cover requests up to i in this order.
        - If request i starts a new dock chain, cost is dp[i-1] + charge[i].
        - Or request i can be appended to a previously ended compatible chain whose
          dock capacity is at least charge[i]. In that case, no new cost is added,
          and we want the best previous state.

        This becomes:
            dp[i] = min(
                dp[i-1] + charge[i],
                min(dp[j-1] + max_charge_of_chain_started_at_request_k ... ) ...)
        A direct formulation is difficult, so we transform it into:
            choose previous request j with end[j] <= start[i] and charge[j] >= charge[i]
            then dp[i] can inherit the same dock started at j, giving:
                dp[i] = min(dp[i], best_value_for_j)
            where best_value_for_j = dp[j-1] + charge[j]
        because request j must have started a dock of capacity at least charge[j], and
        if charge[j] >= charge[i], request i can reuse that same dock later.

        Therefore, for each request i, we need the minimum value among all previous
        requests j such that:
            end[j] <= start[i]
            charge[j] >= charge[i]
        of:
            dp[j-1] + charge[j]

        We process requests in increasing end time. As requests become eligible by end
        time, we insert their value into a Fenwick tree keyed by charge, allowing us to
        query the minimum among charges >= current charge.

        Args:
            requests: List of [start, end, charge].

        Returns:
            Minimum possible total installation cost.

        Time complexity:
            O(n log n)

        Space complexity:
            O(n)
        """
        n: int = len(requests)
        if n == 0:
            return 0

        # Sort requests by end time.
        # This order is natural for interval scheduling style DP because when we process
        # a request, all requests that end earlier are potential predecessors.
        intervals: List[Tuple[int, int, int]] = sorted(
            (s, e, c) for s, e, c in requests
        )

        # We will actually run the DP in order of increasing end time.
        intervals.sort(key=lambda x: x[1])

        # Extract all charge values and coordinate-compress them.
        # Compression is necessary because charge values can be as large as 1e9, but we
        # only need relative ordering for "charge >= current_charge" queries.
        charges_sorted: List[int] = sorted({c for _, _, c in intervals})
        m: int = len(charges_sorted)

        # dp[i] means minimum total cost to cover the first i requests in end-time order.
        # We use 1-based indexing for convenience:
        #   dp[0] = 0
        #   request i in code below corresponds to intervals[i-1]
        dp: List[int] = [0] * (n + 1)

        # Fenwick tree stores minimum values for eligible previous requests.
        # We need query over charges >= current_charge.
        # Fenwick naturally supports prefix queries, so we reverse the compressed index:
        #   rev_index = m - pos
        # Then "charges >= current_charge" becomes a prefix in reversed order.
        fenwick: FenwickMin = FenwickMin(m)

        # To know which previous requests are eligible for reuse when processing current
        # request i, we need all requests j with end[j] <= start[i].
        # Since intervals are sorted by end time, we can advance a pointer and insert
        # those requests into the Fenwick tree exactly once.
        add_ptr: int = 1  # 1-based request index to add into Fenwick when eligible

        # Precompute end times for pointer advancement.
        ends: List[int] = [0] + [e for _, e, _ in intervals]

        for i in range(1, n + 1):
            start_i, end_i, charge_i = intervals[i - 1]

            # First, make all earlier requests whose end time is <= current start time
            # available for reuse. These are exactly the requests that do not overlap
            # with the current request.
            while add_ptr <= n and ends[add_ptr] <= start_i:
                _, _, charge_j = intervals[add_ptr - 1]

                # If request j starts a dock chain (or continues one), then the total
                # cost of the solution up to j can be viewed as:
                #   dp[j-1] + cost_of_dock_used_by_j + other completed chains
                #
                # For future compatible requests with charge <= charge_j, that same dock
                # can be reused at no extra cost. The relevant value to carry forward is:
                #   dp[j-1] + charge_j
                #
                # Why dp[j-1] and not dp[j]?
                # Because request j itself is the first request on that particular dock
                # in this representation. Future requests can continue that dock chain.
                candidate_value: int = dp[add_ptr - 1] + charge_j

                pos_j: int = bisect_left(charges_sorted, charge_j)
                rev_j: int = m - pos_j
                fenwick.update(rev_j, candidate_value)

                add_ptr += 1

            # Option 1:
            # Start a completely new dock for request i.
            # Then we must pay charge_i in addition to the optimal cost for the first
            # i-1 requests.
            best: int = dp[i - 1] + charge_i

            # Option 2:
            # Reuse a previously started dock from some compatible request j with:
            #   end[j] <= start[i] and charge[j] >= charge_i
            # Among all such j, we want minimum dp[j-1] + charge[j].
            pos_i: int = bisect_left(charges_sorted, charge_i)
            rev_i: int = m - pos_i
            reusable_best: int = fenwick.query(rev_i)
            if reusable_best < best:
                best = reusable_best

            dp[i] = best

        return dp[n]


if __name__ == "__main__":
    solution = Solution()

    requests1: List[List[int]] = [[1, 4, 5], [2, 6, 3], [4, 7, 5]]
    result1: int = solution.min_total_cost(requests1)
    print(result1)  # Expected: 8

    requests2: List[List[int]] = [[1, 5, 8], [2, 3, 2], [3, 6, 6], [5, 8, 2]]
    result2: int = solution.min_total_cost(requests2)
    print(result2)  # Expected: 10