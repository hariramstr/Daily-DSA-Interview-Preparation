"""
Title: Minimum Query Limit for Ranked Search Pages

Problem Description:
A search platform stores n documents in a fixed ranking order from 1 to n.
The relevance score of document i is given by scores[i].

A user only sees the first L documents, where L is called the query limit.

You are also given m analyst requirements. Each requirement is a pair [k, t],
meaning that within the first L documents, there must be at least k documents
whose relevance score is greater than or equal to t.

Your task is to find the minimum query limit L such that all requirements are
satisfied at the same time. If no such L exists, return -1.

The ranking order cannot be changed. A document contributes to a requirement
only if its position is at most L. Note that different requirements may use
different score thresholds, and the same document may help satisfy multiple
requirements.

This problem is designed for an efficient solution. A naive scan for every
possible L will be too slow. You should exploit the monotonic nature of the
answer: if a certain prefix length L satisfies all requirements, then any
larger prefix length also satisfies all requirements.

Constraints:
- 1 <= n <= 200000
- 1 <= m <= 200000
- 1 <= scores[i] <= 10^9
- 1 <= k <= n
- 1 <= t <= 10^9
- Requirements are independent, but all must hold simultaneously
"""

from bisect import bisect_left
from typing import Dict, List, Tuple


class FenwickTree:
    """Fenwick Tree / Binary Indexed Tree for prefix sums."""

    def __init__(self, size: int) -> None:
        """
        Initialize the Fenwick Tree.

        Args:
            size: Number of positions in the tree.

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
        Add a value to one position.

        Args:
            index: 1-based index to update.
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
        Compute the sum from position 1 to index.

        Args:
            index: 1-based ending index.

        Returns:
            Prefix sum up to index.

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


class Solution:
    def minimum_query_limit(self, scores: List[int], requirements: List[List[int]]) -> int:
        """
        Find the minimum prefix length L such that all requirements are satisfied.

        The key idea is:
        1. For each distinct threshold t, only the largest required k matters.
           Why? If we need at least 5 documents with score >= t, then any weaker
           requirement like at least 3 documents with score >= t is automatically
           satisfied once the stronger one is satisfied.
        2. For a fixed threshold t, the earliest prefix that satisfies the
           requirement is the position of the k-th document whose score >= t.
        3. Therefore, the final answer is simply the maximum of those earliest
           positions over all distinct thresholds.
        4. To compute these positions efficiently for many thresholds, we process
           thresholds in descending order and activate document positions whose
           scores are high enough. A Fenwick Tree lets us find the position of the
           k-th active document efficiently.

        Args:
            scores: Relevance scores in fixed ranking order.
            requirements: List of [k, t] constraints.

        Returns:
            The smallest valid query limit L, or -1 if impossible.

        Time complexity:
            O((n + u) log n), where u is the number of distinct thresholds in
            the requirements. Since u <= m, this is O((n + m) log n).

        Space complexity:
            O(n + u)
        """
        # ------------------------------------------------------------
        # Step 1: Basic sizes.
        # ------------------------------------------------------------
        n: int = len(scores)

        # If there are no requirements, then every prefix length works.
        # The problem asks for L in [1, n], so the smallest valid one is 1.
        if not requirements:
            return 1 if n > 0 else -1

        # ------------------------------------------------------------
        # Step 2: Compress requirements by threshold.
        #
        # For the same threshold t, only the largest k matters.
        # Example:
        #   requirements = [[2, 6], [5, 6], [3, 6]]
        # Then satisfying "at least 5 docs >= 6" automatically satisfies
        # the other two. So we keep only:
        #   threshold 6 -> required count 5
        #
        # This reduces redundant work and makes the later logic cleaner.
        # ------------------------------------------------------------
        max_k_for_threshold: Dict[int, int] = {}
        for k, t in requirements:
            if t not in max_k_for_threshold or k > max_k_for_threshold[t]:
                max_k_for_threshold[t] = k

        # ------------------------------------------------------------
        # Step 3: Prepare documents as (score, position) pairs.
        #
        # Positions are 1-based because Fenwick Trees are naturally easier
        # to implement with 1-based indexing.
        #
        # We sort documents by score descending so that while processing a
        # threshold t, we can "activate" every document with score >= t.
        # ------------------------------------------------------------
        documents: List[Tuple[int, int]] = [(score, idx + 1) for idx, score in enumerate(scores)]
        documents.sort(reverse=True)

        # ------------------------------------------------------------
        # Step 4: Sort distinct thresholds descending.
        #
        # We process from highest threshold to lowest threshold.
        # As thresholds decrease, more documents become eligible
        # (because score >= smaller threshold is easier to satisfy).
        #
        # This allows a single sweep through the sorted documents.
        # ------------------------------------------------------------
        sorted_thresholds: List[int] = sorted(max_k_for_threshold.keys(), reverse=True)

        # ------------------------------------------------------------
        # Step 5: Fenwick Tree stores which positions are currently active.
        #
        # Active means: this document's score is >= current threshold.
        #
        # If a position is active, we store 1 there.
        # Then:
        #   prefix_sum(x) = number of active documents in positions [1..x]
        #
        # This lets us find the position of the k-th active document:
        # the smallest position pos such that prefix_sum(pos) >= k.
        # ------------------------------------------------------------
        fenwick: FenwickTree = FenwickTree(n)

        # Pointer into the sorted documents list.
        # We will move it forward as we lower the threshold.
        doc_index: int = 0

        # This variable will store the final answer.
        # For each threshold, we compute the earliest prefix that satisfies it.
        # The overall prefix must satisfy ALL thresholds, so it must be at least
        # the maximum of those individual earliest prefixes.
        answer: int = 0

        # ------------------------------------------------------------
        # Step 6: Process each threshold.
        # ------------------------------------------------------------
        for threshold in sorted_thresholds:
            # --------------------------------------------------------
            # Activate every document whose score is >= current threshold.
            #
            # Because both documents and thresholds are processed in
            # descending order, each document is activated exactly once.
            # --------------------------------------------------------
            while doc_index < n and documents[doc_index][0] >= threshold:
                position: int = documents[doc_index][1]
                fenwick.add(position, 1)
                doc_index += 1

            required_count: int = max_k_for_threshold[threshold]

            # --------------------------------------------------------
            # Check if we even have enough active documents in total.
            #
            # If total active documents < required_count, then no prefix
            # can ever satisfy this threshold requirement, because even the
            # full array does not contain enough qualifying documents.
            # --------------------------------------------------------
            total_active: int = fenwick.prefix_sum(n)
            if total_active < required_count:
                return -1

            # --------------------------------------------------------
            # Find the earliest position where the number of active
            # documents reaches required_count.
            #
            # This is the position of the required_count-th active document.
            #
            # We use binary search over positions [1..n].
            # Condition:
            #   prefix_sum(mid) >= required_count
            # means the k-th active document is at or before mid.
            # --------------------------------------------------------
            left: int = 1
            right: int = n
            kth_position: int = n

            while left <= right:
                mid: int = (left + right) // 2
                if fenwick.prefix_sum(mid) >= required_count:
                    kth_position = mid
                    right = mid - 1
                else:
                    left = mid + 1

            # --------------------------------------------------------
            # The final prefix must satisfy every threshold requirement,
            # so it must be at least as large as this threshold's earliest
            # satisfying position.
            # --------------------------------------------------------
            if kth_position > answer:
                answer = kth_position

        # ------------------------------------------------------------
        # Step 7: Return the maximum earliest position across all
        # thresholds. That is the minimum L satisfying all requirements.
        #
        # Example 1 trace:
        # scores = [5, 1, 7, 3, 9, 6]
        # requirements = [[2, 6], [3, 5]]
        #
        # threshold 6:
        #   qualifying positions = [3, 5, 6]
        #   2nd qualifying position = 5
        #
        # threshold 5:
        #   qualifying positions = [1, 3, 5, 6]
        #   3rd qualifying position = 5
        #
        # answer = max(5, 5) = 5
        #
        # Example 2 trace:
        # scores = [2, 2, 2, 2]
        # requirements = [[1, 3]]
        #
        # threshold 3:
        #   qualifying positions = []
        #   total active = 0 < 1
        #   impossible -> return -1
        # ------------------------------------------------------------
        return answer


if __name__ == "__main__":
    solution = Solution()

    scores1: List[int] = [5, 1, 7, 3, 9, 6]
    requirements1: List[List[int]] = [[2, 6], [3, 5]]
    result1: int = solution.minimum_query_limit(scores1, requirements1)
    print(result1)  # Expected: 5

    scores2: List[int] = [2, 2, 2, 2]
    requirements2: List[List[int]] = [[1, 3]]
    result2: int = solution.minimum_query_limit(scores2, requirements2)
    print(result2)  # Expected: -1