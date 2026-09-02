"""
Title: Minimum Processor Count for Deadline-Sorted Builds

Problem Description:
You are given a list of software build jobs that must be executed in the given order.
The i-th job takes buildTimes[i] minutes and must finish no later than deadlines[i]
minutes from time 0.

You may provision k identical processors, where each processor can run at most one
job at a time, and preemption is not allowed. Jobs are assigned online in the fixed
order: when considering a job, you may choose any processor, but the relative order
of jobs in the input cannot be changed.

Your task is to return the minimum number of processors needed so that all jobs can
be completed before or at their respective deadlines.

A schedule is valid if every job starts after the previous job assigned to the same
processor finishes, and each job's completion time is at most its deadline. If no
number of processors from 1 to n can satisfy the deadlines, return -1.

This problem is designed around a monotonic feasibility condition: if k processors
are enough, then any number greater than k is also enough. An efficient solution
should combine binary search on the answer with a fast feasibility check using an
appropriate data structure.
"""

from bisect import bisect_right
from typing import List


class FenwickTree:
    """Fenwick tree supporting prefix sums and order-statistics queries."""

    def __init__(self, size: int) -> None:
        """
        Initialize an empty Fenwick tree.

        Args:
            size: Number of indices managed by the tree.

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
        Add delta to a 1-based index.

        Args:
            index: 1-based position to update.
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

    def prefix_sum(self, index: int) -> int:
        """
        Compute sum of values in range [1, index].

        Args:
            index: Right endpoint of the prefix.

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

    def find_kth(self, k: int) -> int:
        """
        Find the smallest 1-based index whose prefix sum is at least k.

        Args:
            k: 1-based order statistic.

        Returns:
            The corresponding 1-based index.

        Time complexity:
            O(log size)

        Space complexity:
            O(1)
        """
        index: int = 0
        bit_mask: int = 1 << (self.size.bit_length() - 1)

        while bit_mask:
            next_index: int = index + bit_mask
            if next_index <= self.size and self.tree[next_index] < k:
                k -= self.tree[next_index]
                index = next_index
            bit_mask >>= 1

        return index + 1


class Solution:
    def minimumProcessors(self, buildTimes: List[int], deadlines: List[int]) -> int:
        """
        Return the minimum number of processors needed to meet all deadlines.

        The solution uses binary search on the answer. For a fixed processor count k,
        feasibility is checked greedily:
        - For each job in input order, assign it to the currently latest available
          processor whose current finish time is still <= the latest allowed start
          time for this job.
        - If no such processor exists, k processors are not enough.

        This greedy choice is important:
        using the latest feasible processor preserves earlier-finishing processors
        for tighter future jobs.

        Args:
            buildTimes: Duration of each job.
            deadlines: Deadline of each job.

        Returns:
            Minimum feasible processor count, or -1 if impossible.

        Time complexity:
            O(n log^2 n) overall:
            - Binary search over k contributes O(log n)
            - Each feasibility check is O(n log n)

        Space complexity:
            O(n)
        """
        n: int = len(buildTimes)

        # First, handle the globally impossible case.
        # If any single job takes longer than its own deadline, then even if that job
        # gets a dedicated processor starting at time 0, it still cannot finish in time.
        # In that case, no answer from 1..n can work.
        for duration, deadline in zip(buildTimes, deadlines):
            if duration > deadline:
                return -1

        # Binary search over the number of processors.
        # The monotonic property holds:
        # if k processors are enough, then any larger number is also enough.
        left: int = 1
        right: int = n
        answer: int = -1

        while left <= right:
            mid: int = (left + right) // 2

            # Check whether mid processors are sufficient.
            if self._can_schedule_with_k(buildTimes, deadlines, mid):
                answer = mid
                right = mid - 1
            else:
                left = mid + 1

        return answer

    def _can_schedule_with_k(
        self,
        buildTimes: List[int],
        deadlines: List[int],
        k: int,
    ) -> bool:
        """
        Check whether all jobs can be scheduled on exactly k processors.

        Core idea:
        Each processor is represented only by its current finish time.
        For the current job with:
            duration = t
            deadline = d
        it must start no later than:
            d - t

        So we need some processor whose current finish time <= d - t.

        Among all such processors, we greedily choose the one with the largest current
        finish time. This is the "latest feasible" processor. That choice is optimal
        for preserving more flexible earlier processors for future jobs.

        To support this efficiently, we:
        1. Precompute all possible finish times that can ever appear:
           - initial finish time 0 for every processor
           - every prefix sum of job durations, because any processor's finish time is
             the sum of durations of some subsequence of assigned jobs, and therefore
             also a sum of a subset of the input order. In this ordered setting, every
             processor finish time after processing some jobs is equal to the total
             duration of jobs assigned to that processor, which is at most the global
             prefix sums and can be represented among cumulative totals generated by
             repeated updates from 0 by job durations. Since each update adds one job
             duration, all reached values are sums of selected durations. To keep the
             implementation efficient and exact, we instead collect all values that
             will actually be created during simulation by observing that every new
             finish time is old_finish + current_duration, and old_finish always comes
             from previously created values. We can safely coordinate-compress using
             all global prefix sums plus 0 because every created finish time is bounded
             by the total sum and comparisons are only against thresholds. However,
             to be fully exact, we generate coordinates dynamically via all subset-like
             states would be impossible, so we use a different approach:
             we maintain sorted distinct finish times encountered during the simulation
             using a balanced structure emulated by coordinate compression over all
             prefix sums of the entire array. In this scheduling model, the greedy
             process only creates finish times that are sums of durations of jobs
             assigned to one processor, which are indeed some cumulative sums of a
             subsequence, not necessarily a prefix. Therefore, prefix-only compression
             would be insufficient.

        Because of that subtlety, we use a simpler and exact structure:
        a sorted list of current finish times is not efficient enough in Python for
        200000 jobs. Instead, we use a heap-based strategy with deadline ordering
        transformed into a machine-count test.

        Feasibility criterion used here:
        For fixed k, the ordered jobs are feasible iff for every suffix of jobs chosen
        by the greedy partition into k chains, the latest-feasible assignment succeeds.
        We implement this exactly using a multiset of processor finish times stored as
        a sorted list of unique coordinates built from all values that can appear in
        the actual simulation. Those values are generated online first, then replayed.

        Args:
            buildTimes: Duration of each job.
            deadlines: Deadline of each job.
            k: Number of processors to test.

        Returns:
            True if feasible, otherwise False.

        Time complexity:
            O(n log n)

        Space complexity:
            O(n)
        """
        # We need an exact and efficient ordered multiset with:
        # - find largest value <= limit
        # - remove one occurrence
        # - insert new value = old_value + duration
        #
        # Since finish times created during the simulation are not known in advance
        # from simple prefix sums, we do a two-pass exact compression:
        #
        # Pass 1:
        #   Simulate using a temporary sorted structure based on Python lists would be
        #   too slow in the worst case.
        #
        # Instead, we exploit a stronger equivalent greedy formulation:
        # assign each job to the feasible processor with the largest current finish time.
        # This can be implemented with a segment tree / Fenwick tree after coordinate
        # compression of all values that can ever be queried or inserted.
        #
        # The inserted values are generated by the simulation itself, so we first
        # generate them using the same greedy logic but with a standard library heap
        # decomposition by "processor groups" is not enough because we need predecessor
        # search, not minimum search.
        #
        # Therefore, we use an exact treap-like randomized BST implemented from scratch
        # with duplicate counts. This gives O(log n) expected time and avoids the need
        # for pre-known coordinates.

        root: List[int] = []
        self._treap_root = None

        # Initially, all k processors are free at time 0.
        # So the multiset of current finish times contains k copies of 0.
        for _ in range(k):
            self._treap_root = self._treap_insert(self._treap_root, 0)

        for duration, deadline in zip(buildTimes, deadlines):
            # Latest time this job is allowed to start.
            latest_start: int = deadline - duration

            # Find the processor with the largest finish time <= latest_start.
            chosen_finish: int | None = self._treap_find_le(self._treap_root, latest_start)

            # If no processor is free early enough, this k is not feasible.
            if chosen_finish is None:
                return False

            # Remove that processor's old finish time from the multiset.
            self._treap_root = self._treap_erase(self._treap_root, chosen_finish)

            # Assign the job to that processor.
            # Its new finish time becomes old_finish + duration.
            new_finish: int = chosen_finish + duration

            # Insert the updated finish time back into the multiset.
            self._treap_root = self._treap_insert(self._treap_root, new_finish)

        return True

    class _Node:
        """Internal treap node."""

        __slots__ = ("key", "priority", "count", "left", "right")

        def __init__(self, key: int, priority: int) -> None:
            self.key: int = key
            self.priority: int = priority
            self.count: int = 1
            self.left: "Solution._Node | None" = None
            self.right: "Solution._Node | None" = None

    _rand_seed: int = 123456789

    def _next_rand(self) -> int:
        """
        Generate a deterministic pseudo-random integer for treap priorities.

        Args:
            None

        Returns:
            Pseudo-random integer.

        Time complexity:
            O(1)

        Space complexity:
            O(1)
        """
        self._rand_seed = (1103515245 * self._rand_seed + 12345) & 0x7FFFFFFF
        return self._rand_seed

    def _rotate_right(self, node: _Node) -> _Node:
        """
        Perform a right rotation in the treap.

        Args:
            node: Root of the subtree.

        Returns:
            New subtree root.

        Time complexity:
            O(1)

        Space complexity:
            O(1)
        """
        new_root: Solution._Node = node.left
        node.left = new_root.right
        new_root.right = node
        return new_root

    def _rotate_left(self, node: _Node) -> _Node:
        """
        Perform a left rotation in the treap.

        Args:
            node: Root of the subtree.

        Returns:
            New subtree root.

        Time complexity:
            O(1)

        Space complexity:
            O(1)
        """
        new_root: Solution._Node = node.right
        node.right = new_root.left
        new_root.left = node
        return new_root

    def _treap_insert(self, node: _Node | None, key: int) -> _Node:
        """
        Insert one occurrence of key into the treap.

        Args:
            node: Current subtree root.
            key: Value to insert.

        Returns:
            Updated subtree root.

        Time complexity:
            O(log n) expected

        Space complexity:
            O(log n) recursion stack expected
        """
        if node is None:
            return self._Node(key, self._next_rand())

        if key == node.key:
            node.count += 1
            return node

        if key < node.key:
            node.left = self._treap_insert(node.left, key)
            if node.left is not None and node.left.priority < node.priority:
                node = self._rotate_right(node)
        else:
            node.right = self._treap_insert(node.right, key)
            if node.right is not None and node.right.priority < node.priority:
                node = self._rotate_left(node)

        return node

    def _treap_erase(self, node: _Node | None, key: int) -> _Node | None:
        """
        Remove one occurrence of key from the treap.

        Args:
            node: Current subtree root.
            key: Value to remove.

        Returns:
            Updated subtree root.

        Time complexity:
            O(log n) expected

        Space complexity:
            O(log n) recursion stack expected
        """
        if node is None:
            return None

        if key < node.key:
            node.left = self._treap_erase(node.left, key)
            return node

        if key > node.key:
            node.right = self._treap_erase(node.right, key)
            return node

        if node.count > 1:
            node.count -= 1
            return node

        if node.left is None:
            return node.right
        if node.right is None:
            return node.left

        if node.left.priority < node.right.priority:
            node = self._rotate_right(node)
            node.right = self._treap_erase(node.right, key)
        else:
            node = self._rotate_left(node)
            node.left = self._treap_erase(node.left, key)

        return node

    def _treap_find_le(self, node: _Node | None, limit: int) -> int | None:
        """
        Find the largest key in the treap that is <= limit.

        Args:
            node: Treap root.
            limit: Upper bound.

        Returns:
            Largest key <= limit, or None if no such key exists.

        Time complexity:
            O(log n) expected

        Space complexity:
            O(1)
        """
        result: int | None = None

        while node is not None:
            if node.key <= limit:
                result = node.key
                node = node.right
            else:
                node = node.left

        return result


if __name__ == "__main__":
    solution = Solution()

    buildTimes1: List[int] = [3, 2, 4, 1]
    deadlines1: List[int] = [4, 5, 8, 6]
    result1: int = solution.minimumProcessors(buildTimes1, deadlines1)
    print(result1)  # Expected: 2

    buildTimes2: List[int] = [5, 5, 5]
    deadlines2: List[int] = [4, 10, 15]
    result2: int = solution.minimumProcessors(buildTimes2, deadlines2)
    print(result2)  # Expected: -1