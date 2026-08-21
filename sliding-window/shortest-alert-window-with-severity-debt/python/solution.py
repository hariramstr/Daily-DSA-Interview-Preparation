"""
Title: Shortest Alert Window With Severity Debt

Problem Description:
You are given an array `alerts` of length `n`, where each element is a pair
`[serviceId, severity]`. The monitoring team wants to isolate the shortest
contiguous time window that is "actionable".

A window is actionable if it satisfies both conditions:
1. It contains alerts from at least `m` distinct services.
2. Let `peak` be the maximum severity inside the window. For every distinct
   service that appears in the window, consider only that service's highest
   severity within the same window. The total severity debt of the window is
   the sum of `(peak - highestSeverityOfThatService)` over all distinct
   services in the window. The window is valid only if this total debt is at
   most `budget`.

Return the length of the shortest actionable window. If no such window exists,
return `-1`.

Constraints:
- `1 <= n <= 2 * 10^5`
- `1 <= serviceId <= 2 * 10^5`
- `1 <= severity <= 10^9`
- `1 <= m <= n`
- `0 <= budget <= 10^14`
"""

from __future__ import annotations

import heapq
from collections import defaultdict
from typing import DefaultDict, Dict, List, Tuple


class FenwickCount:
    """Fenwick tree for prefix sums of counts."""

    def __init__(self, size: int) -> None:
        """
        Initialize a Fenwick tree that stores integer counts.

        Args:
            size: Number of indices supported, using 1-based indexing internally.

        Returns:
            None

        Time complexity:
            O(size)

        Space complexity:
            O(size)
        """
        self.size: int = size
        self.tree: List[int] = [0] * (size + 1)

    def add(self, index: int, delta: int) -> None:
        """
        Add delta to one position.

        Args:
            index: 1-based compressed index.
            delta: Value to add.

        Returns:
            None

        Time complexity:
            O(log size)

        Space complexity:
            O(1)
        """
        while index <= self.size:
            self.tree[index] += delta
            index += index & -index

    def sum_prefix(self, index: int) -> int:
        """
        Compute prefix sum from 1 to index.

        Args:
            index: 1-based compressed index.

        Returns:
            Prefix sum.

        Time complexity:
            O(log size)

        Space complexity:
            O(1)
        """
        result: int = 0
        while index > 0:
            result += self.tree[index]
            index -= index & -index
        return result

    def range_sum(self, left: int, right: int) -> int:
        """
        Compute sum on [left, right].

        Args:
            left: Left 1-based index.
            right: Right 1-based index.

        Returns:
            Sum on the interval.

        Time complexity:
            O(log size)

        Space complexity:
            O(1)
        """
        if left > right:
            return 0
        return self.sum_prefix(right) - self.sum_prefix(left - 1)


class FenwickSum:
    """Fenwick tree for prefix sums of large integer values."""

    def __init__(self, size: int) -> None:
        """
        Initialize a Fenwick tree that stores integer sums.

        Args:
            size: Number of indices supported, using 1-based indexing internally.

        Returns:
            None

        Time complexity:
            O(size)

        Space complexity:
            O(size)
        """
        self.size: int = size
        self.tree: List[int] = [0] * (size + 1)

    def add(self, index: int, delta: int) -> None:
        """
        Add delta to one position.

        Args:
            index: 1-based compressed index.
            delta: Value to add.

        Returns:
            None

        Time complexity:
            O(log size)

        Space complexity:
            O(1)
        """
        while index <= self.size:
            self.tree[index] += delta
            index += index & -index

    def sum_prefix(self, index: int) -> int:
        """
        Compute prefix sum from 1 to index.

        Args:
            index: 1-based compressed index.

        Returns:
            Prefix sum.

        Time complexity:
            O(log size)

        Space complexity:
            O(1)
        """
        result: int = 0
        while index > 0:
            result += self.tree[index]
            index -= index & -index
        return result

    def range_sum(self, left: int, right: int) -> int:
        """
        Compute sum on [left, right].

        Args:
            left: Left 1-based index.
            right: Right 1-based index.

        Returns:
            Sum on the interval.

        Time complexity:
            O(log size)

        Space complexity:
            O(1)
        """
        if left > right:
            return 0
        return self.sum_prefix(right) - self.sum_prefix(left - 1)


class WindowServiceState:
    """
    Maintains the current highest severity for every service inside the window,
    and supports querying:
    - number of distinct services
    - current global peak severity among service maxima
    - sum of all service maxima

    This lets us compute:
        debt = distinct_count * peak - sum_of_service_maxima
    """

    def __init__(self, all_severities: List[int]) -> None:
        """
        Prepare compressed-coordinate data structures.

        Args:
            all_severities: All severities from the full input, used for compression.

        Returns:
            None

        Time complexity:
            O(k log k), where k is number of severities

        Space complexity:
            O(k)
        """
        unique_values: List[int] = sorted(set(all_severities))
        self.values: List[int] = unique_values
        self.index_of: Dict[int, int] = {value: i + 1 for i, value in enumerate(unique_values)}
        size: int = len(unique_values)

        # count_tree stores how many services currently have maximum severity == values[idx]
        self.count_tree: FenwickCount = FenwickCount(size)

        # sum_tree stores the sum of those maxima values, grouped by compressed severity
        self.sum_tree: FenwickSum = FenwickSum(size)

        # For each service, we keep a frequency map of severities currently present in the window.
        # We also keep a max-heap (implemented as negatives) so we can lazily retrieve the current
        # highest severity for that service.
        self.service_freq: DefaultDict[int, DefaultDict[int, int]] = defaultdict(lambda: defaultdict(int))
        self.service_heap: DefaultDict[int, List[int]] = defaultdict(list)

        # Current highest severity per service.
        self.service_max: Dict[int, int] = {}

        # Number of distinct services currently present in the window.
        self.distinct_count: int = 0

        # Sum of current per-service maxima.
        self.sum_of_maxima: int = 0

    def _bit_add_max_value(self, severity: int, delta_count: int) -> None:
        """
        Update Fenwick trees for one service-maximum value.

        Args:
            severity: Severity value being inserted or removed as a service maximum.
            delta_count: +1 to add one service with this maximum, -1 to remove one.

        Returns:
            None

        Time complexity:
            O(log k)

        Space complexity:
            O(1)
        """
        idx: int = self.index_of[severity]
        self.count_tree.add(idx, delta_count)
        self.sum_tree.add(idx, delta_count * severity)

    def _clean_service_heap(self, service_id: int) -> None:
        """
        Remove stale heap entries for one service.

        Because we use lazy deletion, the heap may still contain severities whose
        frequency in the current window is now zero. We repeatedly pop until the
        top is truly present.

        Args:
            service_id: Service whose heap should be cleaned.

        Returns:
            None

        Time complexity:
            Amortized O(log t), where t is number of alerts for this service

        Space complexity:
            O(1)
        """
        heap: List[int] = self.service_heap[service_id]
        freq: DefaultDict[int, int] = self.service_freq[service_id]
        while heap and freq[-heap[0]] == 0:
            heapq.heappop(heap)

    def add_alert(self, service_id: int, severity: int) -> None:
        """
        Add one alert to the right side of the sliding window.

        Args:
            service_id: Service identifier.
            severity: Severity of the alert.

        Returns:
            None

        Time complexity:
            Amortized O(log n)

        Space complexity:
            O(1) extra beyond maintained structures
        """
        # If this service already exists in the window, it currently contributes one
        # maximum value to the global structures. We may need to replace that maximum
        # if the new alert increases the service's highest severity.
        old_max: int | None = self.service_max.get(service_id)

        # Record the new severity inside the service-local multiset.
        self.service_freq[service_id][severity] += 1
        heapq.heappush(self.service_heap[service_id], -severity)

        # Clean stale values and read the new current maximum for this service.
        self._clean_service_heap(service_id)
        new_max: int = -self.service_heap[service_id][0]

        if old_max is None:
            # This service was not present before, so it becomes a new distinct service.
            self.service_max[service_id] = new_max
            self.distinct_count += 1
            self.sum_of_maxima += new_max
            self._bit_add_max_value(new_max, 1)
        elif new_max != old_max:
            # The service was already present, but its maximum changed.
            # Remove the old contribution and add the new one.
            self.service_max[service_id] = new_max
            self.sum_of_maxima += new_max - old_max
            self._bit_add_max_value(old_max, -1)
            self._bit_add_max_value(new_max, 1)

    def remove_alert(self, service_id: int, severity: int) -> None:
        """
        Remove one alert from the left side of the sliding window.

        Args:
            service_id: Service identifier.
            severity: Severity of the alert being removed.

        Returns:
            None

        Time complexity:
            Amortized O(log n)

        Space complexity:
            O(1) extra beyond maintained structures
        """
        old_max: int = self.service_max[service_id]

        # Remove one occurrence from the service-local multiset.
        self.service_freq[service_id][severity] -= 1

        # Clean stale heap entries to discover the new current maximum, if any.
        self._clean_service_heap(service_id)

        if not self.service_heap[service_id]:
            # The service disappears completely from the window.
            del self.service_max[service_id]
            self.distinct_count -= 1
            self.sum_of_maxima -= old_max
            self._bit_add_max_value(old_max, -1)
        else:
            new_max: int = -self.service_heap[service_id][0]
            if new_max != old_max:
                # The service remains, but its maximum dropped.
                self.service_max[service_id] = new_max
                self.sum_of_maxima += new_max - old_max
                self._bit_add_max_value(old_max, -1)
                self._bit_add_max_value(new_max, 1)

    def current_peak(self) -> int:
        """
        Return the current global peak severity among all service maxima.

        Since every alert belongs to some service, the maximum alert severity in
        the window is exactly the maximum among per-service maxima.

        Returns:
            Current peak severity, or 0 if the window is empty.

        Time complexity:
            O(log k)

        Space complexity:
            O(1)
        """
        if self.distinct_count == 0:
            return 0

        # Binary lifting on Fenwick prefix sums:
        # find the largest index with prefix count < total_count.
        target: int = self.distinct_count
        idx: int = 0
        bit_mask: int = 1 << (self.count_tree.size.bit_length() - 1)
        current_sum: int = 0

        while bit_mask:
            next_idx: int = idx + bit_mask
            if next_idx <= self.count_tree.size and current_sum + self.count_tree.tree[next_idx] < target:
                idx = next_idx
                current_sum += self.count_tree.tree[next_idx]
            bit_mask >>= 1

        return self.values[idx]

    def current_debt(self) -> int:
        """
        Compute the current severity debt of the window.

        If there are d distinct services and the peak severity is P, then:
            debt = sum(P - max_of_service)
                 = d * P - sum_of_service_maxima

        Returns:
            Current debt.

        Time complexity:
            O(log k) due to peak query

        Space complexity:
            O(1)
        """
        if self.distinct_count == 0:
            return 0
        peak: int = self.current_peak()
        return self.distinct_count * peak - self.sum_of_maxima


class Solution:
    def shortestAlertWindow(self, alerts: List[List[int]], m: int, budget: int) -> int:
        """
        Find the shortest contiguous actionable window.

        A window is actionable if:
        1. It contains at least m distinct services.
        2. Its severity debt is at most budget.

        Args:
            alerts: List of [serviceId, severity].
            m: Minimum number of distinct services required.
            budget: Maximum allowed severity debt.

        Returns:
            Length of the shortest valid window, or -1 if none exists.

        Time complexity:
            O(n log n)

        Space complexity:
            O(n)
        """
        n: int = len(alerts)
        if m > n:
            return -1

        # We compress all severity values once so that Fenwick trees can index them.
        all_severities: List[int] = [severity for _, severity in alerts]
        state: WindowServiceState = WindowServiceState(all_severities)

        left: int = 0
        answer: int = n + 1

        # Standard sliding window:
        # - expand right one step at a time
        # - after each expansion, shrink left while the window remains valid
        #
        # Why this works:
        # For a fixed right endpoint, once the window is valid, moving left forward
        # can only make the window shorter. We keep shrinking until it would become
        # invalid, ensuring we record the shortest valid window ending at this right.
        for right in range(n):
            service_id, severity = alerts[right]

            # Add the new alert into all maintained structures.
            state.add_alert(service_id, severity)

            # While the current window satisfies both conditions, record its length
            # and try to shrink it from the left.
            while state.distinct_count >= m and state.current_debt() <= budget:
                answer = min(answer, right - left + 1)

                left_service, left_severity = alerts[left]
                state.remove_alert(left_service, left_severity)
                left += 1

        return answer if answer <= n else -1


if __name__ == "__main__":
    solution = Solution()

    alerts1: List[List[int]] = [[1, 4], [2, 2], [1, 6], [3, 5]]
    m1: int = 3
    budget1: int = 5
    result1: int = solution.shortestAlertWindow(alerts1, m1, budget1)
    print(result1)  # Expected: 4

    alerts2: List[List[int]] = [[1, 8], [2, 7], [2, 3], [3, 8], [1, 5]]
    m2: int = 3
    budget2: int = 1
    result2: int = solution.shortestAlertWindow(alerts2, m2, budget2)
    print(result2)  # Expected: 4