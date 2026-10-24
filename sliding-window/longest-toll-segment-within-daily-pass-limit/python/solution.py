"""
Title: Longest Toll Segment Within Daily Pass Limit

Problem Description:
A driver is traveling along a highway divided into consecutive road segments.
For each segment i, tolls[i] is the toll cost required to drive through that segment.

The driver owns a special daily pass that can fully waive the toll on at most k
segments within one continuous part of the trip.

For any contiguous subarray tolls[l..r], the driver may select up to k elements
in that subarray and treat their cost as 0. The remaining segment costs must sum
to at most budget.

Return the maximum possible length of such a contiguous subarray.

Constraints:
- 1 <= tolls.length <= 200000
- 0 <= tolls[i] <= 1000000000
- 0 <= budget <= 100000000000000
- 0 <= k <= tolls.length

Examples:
1)
Input: tolls = [7, 2, 5, 10, 3], budget = 10, k = 1
Output: 3

2)
Input: tolls = [4, 8, 1, 9, 2, 6], budget = 11, k = 2
Output: 5
"""

from typing import List
import heapq


class DualHeapTopK:
    """
    Maintain the sum of the largest k values in a dynamic multiset with lazy deletion.

    This helper data structure supports:
    - add(value): insert a value
    - remove(value): remove one occurrence of a value
    - top_k_sum(): get the sum of the largest k active values

    Internally, it uses two heaps:
    1. large:
       A min-heap containing exactly the current largest k active values
       (or fewer if total active count < k).
       Because it is a min-heap, the smallest among the "top k" values is at the root.
       This makes rebalancing efficient.

    2. small:
       A max-heap implemented by storing negative values.
       It contains all remaining active values that are not currently in large.

    We also use lazy deletion:
    - When removing a value, we do not immediately search inside a heap.
    - Instead, we record that value in a delayed-deletion dictionary.
    - Whenever a heap top is accessed, we repeatedly discard invalid elements.

    This is a standard technique for efficient sliding-window order-statistics problems.
    """

    def __init__(self, k: int) -> None:
        """
        Initialize the data structure.

        Args:
            k: Number of largest elements whose sum we want to maintain.

        Returns:
            None

        Time complexity:
            O(1)

        Space complexity:
            O(1) excluding stored elements
        """
        self.k: int = k

        # Min-heap of the current largest k elements.
        self.large: List[int] = []

        # Max-heap (via negatives) of the remaining elements.
        self.small: List[int] = []

        # Lazy deletion maps:
        # delayed_large[x] = how many copies of x should be removed from large
        # delayed_small[x] = how many copies of x should be removed from small
        self.delayed_large: dict[int, int] = {}
        self.delayed_small: dict[int, int] = {}

        # Number of valid active elements currently assigned to each heap.
        self.large_size: int = 0
        self.small_size: int = 0

        # Sum of valid elements currently in large.
        self.large_sum: int = 0

    def _prune_large(self) -> None:
        """
        Remove invalid top elements from the large heap.

        Args:
            None

        Returns:
            None

        Time complexity:
            Amortized O(log n)

        Space complexity:
            O(1)
        """
        while self.large:
            value = self.large[0]
            count = self.delayed_large.get(value, 0)
            if count == 0:
                break
            heapq.heappop(self.large)
            if count == 1:
                del self.delayed_large[value]
            else:
                self.delayed_large[value] = count - 1

    def _prune_small(self) -> None:
        """
        Remove invalid top elements from the small heap.

        Args:
            None

        Returns:
            None

        Time complexity:
            Amortized O(log n)

        Space complexity:
            O(1)
        """
        while self.small:
            value = -self.small[0]
            count = self.delayed_small.get(value, 0)
            if count == 0:
                break
            heapq.heappop(self.small)
            if count == 1:
                del self.delayed_small[value]
            else:
                self.delayed_small[value] = count - 1

    def _rebalance(self) -> None:
        """
        Restore the invariant that large contains exactly min(k, total_count) valid elements.

        Why this matters:
        - large must always represent the largest k active values
        - then large_sum is exactly the total amount that can be waived optimally

        Rebalancing rules:
        1. If large has too many elements, move its smallest element to small.
        2. If large has too few elements, move the largest element from small to large.
        3. Prune heap tops whenever needed so comparisons use valid elements.

        Args:
            None

        Returns:
            None

        Time complexity:
            Amortized O(log n)

        Space complexity:
            O(1)
        """
        target_large_size = min(self.k, self.large_size + self.small_size)

        # If large has more than needed, move the smallest of the top-k set into small.
        while self.large_size > target_large_size:
            self._prune_large()
            value = heapq.heappop(self.large)
            self.large_size -= 1
            self.large_sum -= value

            heapq.heappush(self.small, -value)
            self.small_size += 1

        # If large has fewer than needed, promote the largest element from small.
        while self.large_size < target_large_size:
            self._prune_small()
            value = -heapq.heappop(self.small)
            self.small_size -= 1

            heapq.heappush(self.large, value)
            self.large_size += 1
            self.large_sum += value

        # After moving elements, heap tops may expose delayed items, so prune again.
        self._prune_large()
        self._prune_small()

        # Additional ordering fix:
        # Every element in large should be >= every element in small.
        # If not, swap the boundary elements.
        while self.large_size > 0 and self.small_size > 0:
            self._prune_large()
            self._prune_small()
            if not self.large or not self.small:
                break

            smallest_large = self.large[0]
            largest_small = -self.small[0]

            if largest_small <= smallest_large:
                break

            heapq.heappop(self.large)
            heapq.heappop(self.small)

            self.large_sum -= smallest_large
            self.large_sum += largest_small

            heapq.heappush(self.large, largest_small)
            heapq.heappush(self.small, -smallest_large)

    def add(self, value: int) -> None:
        """
        Add one value into the structure.

        Strategy:
        - If large has room, place the value there.
        - Otherwise compare against the smallest value in large:
          * if the new value belongs among the top k, insert into large and demote one
          * else insert into small
        - Then rebalance to restore invariants.

        Args:
            value: Value to insert

        Returns:
            None

        Time complexity:
            Amortized O(log n)

        Space complexity:
            O(1) auxiliary, not counting stored elements
        """
        if self.k == 0:
            heapq.heappush(self.small, -value)
            self.small_size += 1
            return

        self._prune_large()

        if self.large_size < self.k:
            heapq.heappush(self.large, value)
            self.large_size += 1
            self.large_sum += value
        else:
            if self.large and value >= self.large[0]:
                heapq.heappush(self.large, value)
                self.large_size += 1
                self.large_sum += value
            else:
                heapq.heappush(self.small, -value)
                self.small_size += 1

        self._rebalance()

    def remove(self, value: int) -> None:
        """
        Remove one occurrence of value from the structure.

        Important detail:
        Because heaps do not support efficient arbitrary deletion, we use lazy deletion.
        We decide which heap the value logically belongs to by comparing it with the
        current smallest element in large.

        If value >= min(large), it must belong to large's side of the partition.
        Otherwise it belongs to small.

        This works because large always stores the largest k values.

        Args:
            value: Value to remove

        Returns:
            None

        Time complexity:
            Amortized O(log n)

        Space complexity:
            O(1)
        """
        if self.k == 0:
            self.delayed_small[value] = self.delayed_small.get(value, 0) + 1
            self.small_size -= 1
            self._prune_small()
            return

        self._prune_large()

        if self.large and value >= self.large[0]:
            self.delayed_large[value] = self.delayed_large.get(value, 0) + 1
            self.large_size -= 1
            self.large_sum -= value
            if self.large and self.large[0] == value:
                self._prune_large()
        else:
            self.delayed_small[value] = self.delayed_small.get(value, 0) + 1
            self.small_size -= 1
            if self.small and -self.small[0] == value:
                self._prune_small()

        self._rebalance()

    def top_k_sum(self) -> int:
        """
        Return the sum of the largest k active values.

        Args:
            None

        Returns:
            Sum of the largest k values currently stored

        Time complexity:
            O(1)

        Space complexity:
            O(1)
        """
        return self.large_sum


class Solution:
    def longest_toll_segment(self, tolls: List[int], budget: int, k: int) -> int:
        """
        Find the maximum length of a contiguous subarray such that after waiving
        at most k tolls inside that subarray, the remaining paid sum is <= budget.

        Key idea:
        For a fixed window, the best way to minimize what we pay is to waive the
        most expensive up to k segments in that window.

        So for any window:
            paid_sum = total_window_sum - sum_of_largest_k_values_in_window

        We need the longest window with:
            total_window_sum - top_k_sum <= budget

        We use a sliding window:
        - Expand the right end one segment at a time.
        - Maintain:
            1. total sum of the current window
            2. sum of the largest k values in the current window
        - If the paid amount exceeds budget, shrink from the left until valid again.
        - Track the maximum valid window length.

        Why this is efficient:
        - Each element enters the window once and leaves once.
        - Each add/remove operation on the helper structure is O(log n).
        - Total complexity is O(n log n), which is suitable for n up to 200000.

        Args:
            tolls: List of toll costs for consecutive road segments
            budget: Maximum total amount that can actually be paid
            k: Maximum number of segments whose toll can be waived

        Returns:
            The maximum length of a valid contiguous subarray

        Time complexity:
            O(n log n)

        Space complexity:
            O(n)
        """
        # This helper structure will always know the sum of the largest k values
        # in the current sliding window.
        top_k = DualHeapTopK(k)

        # left marks the beginning of the current window.
        left: int = 0

        # total_sum stores the sum of all tolls currently inside the window [left..right].
        total_sum: int = 0

        # best stores the maximum valid window length found so far.
        best: int = 0

        # We expand the window by moving right from 0 to len(tolls)-1.
        for right, value in enumerate(tolls):
            # Step 1: include the new rightmost value into the window.
            total_sum += value
            top_k.add(value)

            # Step 2: compute how much would actually be paid if we waive the
            # largest up to k tolls in the current window.
            #
            # This is the minimum possible paid amount for this window, because
            # waiving the most expensive segments is always optimal.
            paid_sum = total_sum - top_k.top_k_sum()

            # Step 3: if the window is too expensive, shrink it from the left
            # until it becomes affordable again.
            #
            # This is the standard sliding-window pattern:
            # while invalid -> remove leftmost element and advance left.
            while paid_sum > budget and left <= right:
                remove_value = tolls[left]
                total_sum -= remove_value
                top_k.remove(remove_value)
                left += 1

                paid_sum = total_sum - top_k.top_k_sum()

            # Step 4: now the window [left..right] is valid.
            # Update the best answer with its length.
            current_length = right - left + 1
            if current_length > best:
                best = current_length

        return best


if __name__ == "__main__":
    solution = Solution()

    # Example 1
    tolls1 = [7, 2, 5, 10, 3]
    budget1 = 10
    k1 = 1
    result1 = solution.longest_toll_segment(tolls1, budget1, k1)
    print("Example 1 Result:", result1)  # Expected: 3

    # Example 2
    tolls2 = [4, 8, 1, 9, 2, 6]
    budget2 = 11
    k2 = 2
    result2 = solution.longest_toll_segment(tolls2, budget2, k2)
    print("Example 2 Result:", result2)  # Expected: 5

    # Additional quick checks
    print("Additional Check 1:", solution.longest_toll_segment([1, 2, 3], 0, 3))   # Expected: 3
    print("Additional Check 2:", solution.longest_toll_segment([5, 5, 5], 5, 0))   # Expected: 1
    print("Additional Check 3:", solution.longest_toll_segment([0, 0, 0], 0, 0))   # Expected: 3