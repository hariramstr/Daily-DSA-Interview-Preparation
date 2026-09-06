"""
Title: Maximum Viable Cache TTL

Problem Description:
You are designing a cache for a backend service. There are n requests arriving in chronological order,
and request i arrives at time times[i] and would cost costs[i] units to recompute if it is not already
in cache. The cache uses a single global TTL (time-to-live) value T. If two consecutive requests for
the same key happen within T seconds of each other, then every request after the first within that TTL
window is served from cache and contributes 0 recomputation cost. Otherwise, the request is a cache miss
and its full recomputation cost is paid.

You are given three arrays of equal length: keys, times, and costs, where keys[i] is the resource
requested by the i-th request. The arrays are sorted by nondecreasing times. You are also given a budget B,
representing the maximum total recomputation cost the system can afford.

Return the maximum integer TTL T such that the total recomputation cost is at most B. If even T = 0
satisfies the budget, you may return 0 or a larger valid TTL if possible. If no TTL can make the total
cost at most B, return -1.

A request is considered a cache hit only if there exists an earlier request for the same key whose cached
value has not expired yet. More formally, for a fixed key, if the previous request for that key happened
at time p and the current request happens at time c, then the current request is a hit iff c - p <= T;
otherwise it is a miss and refreshes the cached value.

Constraints:
- 1 <= n <= 200000
- 1 <= keys[i] <= 10^9
- 0 <= times[i] <= 10^18
- times is sorted in nondecreasing order
- 1 <= costs[i] <= 10^9
- 0 <= B <= 10^18

Important interpretation note:
The total recomputation cost is monotone non-increasing as T grows. Therefore, if some TTL is valid,
then every larger TTL is also valid. In the literal mathematical sense, the maximum valid integer TTL
would then be unbounded whenever any valid TTL exists.

To make the problem well-defined, we search only among the finite set of "induced" TTL thresholds where
the answer can change:
- T = 0
- every gap between consecutive requests of the same key

The returned answer is the largest induced TTL whose total recomputation cost is at most B.
If no induced TTL is feasible, return -1.
"""

from bisect import bisect_left
from typing import Dict, List, Tuple


class Solution:
    def _build_candidate_ttls(self, keys: List[int], times: List[int]) -> List[int]:
        """
        Build the sorted list of TTL values where the cache behavior can change.

        The total cost only changes when T crosses a gap between two consecutive requests
        of the same key. Therefore, instead of searching over all integers, we only need
        to consider:
        - 0
        - every such consecutive same-key gap

        Args:
            keys: Request keys in chronological order.
            times: Request times in nondecreasing order.

        Returns:
            A sorted list of unique candidate TTL values.

        Time complexity:
            O(n log n) in the worst case due to sorting unique gaps.

        Space complexity:
            O(n) for storing previous times and candidate gaps.
        """
        # This dictionary remembers the most recent time we have seen for each key
        # while scanning the requests from left to right.
        last_time: Dict[int, int] = {}

        # We use a set so that duplicate gaps are stored only once.
        # Example: if many keys have the same gap value 5, we only need one candidate TTL = 5.
        candidates = {0}

        # Scan requests in chronological order.
        for key, current_time in zip(keys, times):
            # If we have seen this key before, then the gap between the previous request
            # and the current request is a threshold where the hit/miss behavior for this
            # request changes.
            if key in last_time:
                gap = current_time - last_time[key]
                candidates.add(gap)

            # Update the most recent time for this key.
            last_time[key] = current_time

        # Convert to a sorted list so we can binary search on candidate TTL values.
        return sorted(candidates)

    def _cost_with_ttl(
        self,
        keys: List[int],
        times: List[int],
        costs: List[int],
        ttl: int,
        budget: int,
    ) -> int:
        """
        Compute total recomputation cost for a fixed TTL.

        We simulate the request stream from left to right. For each key, we only need
        the time of its most recent request:
        - If the current request is within ttl of the previous request for the same key,
          it is a cache hit and costs 0.
        - Otherwise, it is a miss and we pay its recomputation cost.

        Early stopping is used: if the running total already exceeds budget, we can stop
        immediately because the caller only needs to know whether the cost is <= budget.

        Args:
            keys: Request keys in chronological order.
            times: Request times in nondecreasing order.
            costs: Recomputation costs for each request.
            ttl: TTL value being tested.
            budget: Budget threshold used for early exit.

        Returns:
            The total recomputation cost, or any value greater than budget if early exit occurs.

        Time complexity:
            O(n)

        Space complexity:
            O(u), where u is the number of distinct keys.
        """
        # last_time maps each key to the time of its most recent request.
        # This is exactly the information needed by the problem statement,
        # because a request only compares against the previous request for the same key.
        last_time: Dict[int, int] = {}

        total_cost = 0

        # Process each request in chronological order.
        for key, current_time, current_cost in zip(keys, times, costs):
            # Case 1: first time we see this key -> definitely a miss.
            if key not in last_time:
                total_cost += current_cost
            else:
                # Compute the gap from the previous request for this same key.
                gap = current_time - last_time[key]

                # If the gap is larger than ttl, the previous cached value has expired,
                # so this request is a miss and we must pay the recomputation cost.
                # Otherwise it is a hit and contributes 0.
                if gap > ttl:
                    total_cost += current_cost

            # Regardless of hit or miss, the current request becomes the new "previous request"
            # for this key for future requests.
            last_time[key] = current_time

            # Early exit optimization:
            # once we exceed the budget, there is no need to continue.
            if total_cost > budget:
                return total_cost

        return total_cost

    def _is_feasible(
        self,
        keys: List[int],
        times: List[int],
        costs: List[int],
        ttl: int,
        budget: int,
    ) -> bool:
        """
        Check whether a given TTL keeps total recomputation cost within budget.

        Args:
            keys: Request keys in chronological order.
            times: Request times in nondecreasing order.
            costs: Recomputation costs for each request.
            ttl: TTL value being tested.
            budget: Maximum allowed total recomputation cost.

        Returns:
            True if total cost <= budget, otherwise False.

        Time complexity:
            O(n)

        Space complexity:
            O(u), where u is the number of distinct keys.
        """
        return self._cost_with_ttl(keys, times, costs, ttl, budget) <= budget

    def maximum_viable_cache_ttl(
        self,
        keys: List[int],
        times: List[int],
        costs: List[int],
        budget: int,
    ) -> int:
        """
        Return the largest induced TTL whose total recomputation cost is at most budget.

        Key idea:
        - As TTL increases, requests can only change from miss to hit, never the reverse.
          Therefore total cost is monotone non-increasing.
        - The cost only changes when TTL reaches a gap between consecutive requests of the same key.
        - So we:
            1) Build all candidate TTL thresholds.
            2) Binary search the largest candidate whose cost is <= budget.

        Args:
            keys: Request keys in chronological order.
            times: Request times in nondecreasing order.
            costs: Recomputation costs for each request.
            budget: Maximum allowed total recomputation cost.

        Returns:
            The largest induced TTL satisfying the budget, or -1 if none exists.

        Time complexity:
            O(n log n + n log m), where m is the number of candidate TTLs.
            Since m <= n, this is O(n log n).

        Space complexity:
            O(n)
        """
        # Defensive handling for malformed input lengths.
        # The problem guarantees equal lengths, but this keeps the method robust.
        if not (len(keys) == len(times) == len(costs)):
            raise ValueError("keys, times, and costs must have the same length")

        n = len(keys)
        if n == 0:
            # Not needed by constraints, but included for completeness.
            return -1

        # Step 1:
        # Build the finite set of TTL values where the answer can change.
        candidates = self._build_candidate_ttls(keys, times)

        # Step 2:
        # Because cost is monotone non-increasing in TTL, feasibility is monotone:
        # once a candidate TTL is feasible, every larger candidate is also feasible.
        #
        # Therefore we can binary search for the first feasible candidate,
        # then the answer is simply the largest candidate (because all larger ones are feasible).
        #
        # However, to keep the logic explicit and beginner-friendly, we instead binary search
        # for the largest feasible candidate directly.
        left = 0
        right = len(candidates) - 1
        answer = -1

        while left <= right:
            mid = (left + right) // 2
            ttl = candidates[mid]

            # Test whether this TTL keeps the total cost within budget.
            if self._is_feasible(keys, times, costs, ttl, budget):
                # This TTL works, so record it.
                answer = ttl

                # Since larger TTLs can only reduce or preserve cost,
                # we try to find an even larger feasible candidate.
                left = mid + 1
            else:
                # This TTL is too small to meet the budget.
                # We need a larger TTL to potentially convert more misses into hits.
                left = mid + 1

        # The above loop always moves rightward because feasibility is monotone from
        # "possibly false" to "true" as TTL increases. To correctly find the largest feasible
        # candidate, we should instead locate the first feasible candidate and then return
        # the last candidate. The current direct-search structure would skip necessary state.
        #
        # So we now perform the correct monotone binary search cleanly below.

        # Find the first candidate TTL that is feasible.
        left = 0
        right = len(candidates) - 1
        first_feasible_index = -1

        while left <= right:
            mid = (left + right) // 2
            ttl = candidates[mid]

            if self._is_feasible(keys, times, costs, ttl, budget):
                first_feasible_index = mid
                right = mid - 1
            else:
                left = mid + 1

        # If no candidate TTL is feasible, return -1.
        if first_feasible_index == -1:
            return -1

        # Since every candidate after the first feasible one is also feasible,
        # the largest feasible induced TTL is simply the last candidate.
        return candidates[-1]


if __name__ == "__main__":
    solution = Solution()

    # Example 1
    keys1 = [1, 2, 1, 1, 2]
    times1 = [1, 2, 4, 8, 10]
    costs1 = [5, 7, 5, 5, 7]
    budget1 = 17
    result1 = solution.maximum_viable_cache_ttl(keys1, times1, costs1, budget1)
    print(result1)  # Expected: 8

    # Example 2
    keys2 = [3, 3, 3, 4]
    times2 = [5, 9, 15, 20]
    costs2 = [4, 4, 4, 10]
    budget2 = 13
    result2 = solution.maximum_viable_cache_ttl(keys2, times2, costs2, budget2)
    print(result2)  # Expected: -1

    # Additional sanity check:
    # If budget were 14 in example 2, TTL 6 becomes feasible and is the largest induced TTL.
    budget3 = 14
    result3 = solution.maximum_viable_cache_ttl(keys2, times2, costs2, budget3)
    print(result3)  # Expected: 6