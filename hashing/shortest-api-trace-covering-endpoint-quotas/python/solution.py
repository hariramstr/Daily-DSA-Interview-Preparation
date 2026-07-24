"""
Title: Shortest API Trace Covering Endpoint Quotas

Problem Description:
You are given an API trace represented by an array `trace`, where `trace[i]` is the
endpoint name called at time `i`. You are also given a list of required endpoint quotas
as pairs `(endpoint, count)`, meaning a valid incident window must contain that endpoint
at least `count` times.

Return the length of the shortest contiguous subarray of `trace` that satisfies all
required quotas. If no such subarray exists, return `-1`.

Unlike a simple coverage problem, the trace can be very large, endpoint names are
arbitrary strings, and the quota list may contain repeated endpoint requirements that
should be combined. Your solution should be efficient enough for production-scale logs.

Formally, if `need[x]` is the required number of occurrences of endpoint `x`, then a
window `[l, r]` is valid if for every required endpoint `x`, the number of indices `i`
in `[l, r]` with `trace[i] == x` is at least `need[x]`.

Constraints:
- 1 <= trace.length <= 2 * 10^5
- 1 <= quotas.length <= 2 * 10^5
- trace[i]` and endpoint names in `quotas` are non-empty strings of lowercase English
  letters, digits, `_`, or `/`
- The sum of all endpoint name lengths across input is at most `10^6`
- Quotas may contain duplicate endpoint names; they should be added together

Examples:
1)
trace = ["/login","/feed","/cart","/login","/feed","/pay"]
quotas = [["/login",2],["/feed",1],["/pay",1]]
Output: 6

2)
trace = ["a","x","b","a","c","b","a"]
quotas = [["a",2],["b",1]]
Output: 4
"""

from typing import Dict, List


class Solution:
    def _build_need(self, quotas: List[List[object]]) -> Dict[str, int]:
        """
        Build the combined required frequency map from the quota list.

        Duplicate endpoint names in the quota input must be merged by summing their
        required counts. For example:
        [["a", 1], ["b", 2], ["a", 3]] -> {"a": 4, "b": 2}

        Args:
            quotas: A list where each element is [endpoint, count].

        Returns:
            A dictionary mapping each endpoint to its total required count.

        Time complexity:
            O(len(quotas))

        Space complexity:
            O(k), where k is the number of distinct endpoints in quotas.
        """
        need: Dict[str, int] = {}

        # We iterate through every quota pair and accumulate counts.
        # This is necessary because the problem explicitly says duplicate endpoint
        # requirements may appear and should be combined.
        for endpoint, count in quotas:
            endpoint_str = str(endpoint)
            count_int = int(count)
            need[endpoint_str] = need.get(endpoint_str, 0) + count_int

        return need

    def shortest_api_trace_covering_quotas(
        self, trace: List[str], quotas: List[List[object]]
    ) -> int:
        """
        Return the length of the shortest contiguous subarray that satisfies all quotas.

        This uses a classic sliding window / two-pointer technique with hashing-based
        frequency counting:
        1. Build the required frequency map `need`.
        2. Expand the right pointer to include more endpoints.
        3. Track current frequencies in the window using `window_count`.
        4. Once all requirements are satisfied, shrink from the left to find the
           smallest valid window ending at the current right pointer.
        5. Record the minimum length seen.

        Args:
            trace: List of endpoint names in chronological order.
            quotas: List of [endpoint, count] requirements.

        Returns:
            The length of the shortest valid contiguous subarray, or -1 if impossible.

        Time complexity:
            O(n + q), where n = len(trace), q = len(quotas).
            Each trace element is added to the window once and removed once.

        Space complexity:
            O(k), where k is the number of distinct required endpoints.
        """
        # Step 1: Combine duplicate quota entries into one required-frequency map.
        need: Dict[str, int] = self._build_need(quotas)

        # If there are no requirements after processing, the shortest window would
        # conceptually be length 0. However, based on the problem constraints,
        # quotas.length >= 1, so this is mostly defensive programming.
        if not need:
            return 0

        # Step 2: Quick impossibility check.
        # Before running the sliding window, count how many times each required endpoint
        # appears in the entire trace. If the full trace does not even contain enough of
        # some required endpoint, then no window can ever satisfy the quotas.
        total_available: Dict[str, int] = {}
        for endpoint in trace:
            if endpoint in need:
                total_available[endpoint] = total_available.get(endpoint, 0) + 1

        for endpoint, required_count in need.items():
            if total_available.get(endpoint, 0) < required_count:
                return -1

        # Step 3: Prepare the sliding window state.
        #
        # `window_count` stores frequencies of required endpoints currently inside the
        # active window [left, right].
        window_count: Dict[str, int] = {}

        # `required_types` is the number of distinct endpoint names we must satisfy.
        required_types: int = len(need)

        # `formed_types` counts how many distinct endpoint names currently meet their
        # required quota inside the window.
        #
        # Example:
        # need = {"a": 2, "b": 1}
        # If window_count = {"a": 2, "b": 0}, then formed_types = 1
        # because only "a" is satisfied.
        formed_types: int = 0

        # Left boundary of the sliding window.
        left: int = 0

        # Best answer found so far. Start with infinity so any valid window is smaller.
        best_length: int = float("inf")

        # Step 4: Expand the window by moving `right` from left to right across the trace.
        for right, endpoint in enumerate(trace):
            # We only care about endpoints that appear in `need`.
            # Endpoints not required by the quotas do not affect validity directly.
            if endpoint in need:
                # Add the current endpoint to the window frequency map.
                window_count[endpoint] = window_count.get(endpoint, 0) + 1

                # If this addition makes the count exactly equal to the required count,
                # then this endpoint type has just become satisfied.
                #
                # We use equality (==), not >=, because we only want to increment
                # `formed_types` once per endpoint type when it transitions from
                # "not satisfied" to "satisfied".
                if window_count[endpoint] == need[endpoint]:
                    formed_types += 1

            # Step 5: If all required endpoint types are satisfied, try to shrink.
            #
            # While the current window is valid, move `left` rightward as much as
            # possible without breaking validity. This guarantees we find the shortest
            # valid window ending at the current `right`.
            while formed_types == required_types and left <= right:
                # Current window [left, right] is valid, so update the best answer.
                current_length: int = right - left + 1
                if current_length < best_length:
                    best_length = current_length

                # We are about to remove trace[left] from the window and move left forward.
                left_endpoint: str = trace[left]

                if left_endpoint in need:
                    # Remove one occurrence of this endpoint from the window.
                    window_count[left_endpoint] -= 1

                    # If the count drops below the required amount, this endpoint type
                    # is no longer satisfied, so the whole window becomes invalid.
                    if window_count[left_endpoint] < need[left_endpoint]:
                        formed_types -= 1

                # Actually shrink the window from the left.
                left += 1

        # If best_length was never updated, no valid window exists.
        return -1 if best_length == float("inf") else best_length


if __name__ == "__main__":
    solution = Solution()

    # Example 1
    trace1: List[str] = ["/login", "/feed", "/cart", "/login", "/feed", "/pay"]
    quotas1: List[List[object]] = [["/login", 2], ["/feed", 1], ["/pay", 1]]
    result1: int = solution.shortest_api_trace_covering_quotas(trace1, quotas1)
    print(result1)  # Expected: 6

    # Example 2
    trace2: List[str] = ["a", "x", "b", "a", "c", "b", "a"]
    quotas2: List[List[object]] = [["a", 2], ["b", 1]]
    result2: int = solution.shortest_api_trace_covering_quotas(trace2, quotas2)
    print(result2)  # Expected: 4

    # Additional sanity checks
    trace3: List[str] = ["a", "b", "c"]
    quotas3: List[List[object]] = [["a", 1], ["c", 1]]
    result3: int = solution.shortest_api_trace_covering_quotas(trace3, quotas3)
    print(result3)  # Expected: 3

    trace4: List[str] = ["a", "b", "a", "b", "a"]
    quotas4: List[List[object]] = [["a", 2], ["b", 2]]
    result4: int = solution.shortest_api_trace_covering_quotas(trace4, quotas4)
    print(result4)  # Expected: 4

    trace5: List[str] = ["a", "b"]
    quotas5: List[List[object]] = [["a", 1], ["c", 1]]
    result5: int = solution.shortest_api_trace_covering_quotas(trace5, quotas5)
    print(result5)  # Expected: -1