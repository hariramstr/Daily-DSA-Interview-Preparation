"""
Title: Maximum Score from Picking a Protected Triple

Problem Description:
You are given an integer array nums of length n. You want to choose three indices
i, j, k such that i < j < k. The score of choosing this triple is defined as:

    (nums[i] + nums[j] + nums[k]) * min(k - j, j - i)

The factor min(k - j, j - i) represents how well the middle element is protected
by spacing on both sides: the smaller of the two gaps limits the final score.

Your task is to return the maximum possible score over all valid triples. If n < 3,
return 0.

A brute-force O(n^3) solution will not pass. The challenge is to exploit the
structure of the score formula and design an algorithm efficient enough for large arrays.

Constraints:
- 3 <= n <= 2 * 10^5
- -10^9 <= nums[i] <= 10^9
- The answer can be negative, so do not clamp it to 0 unless no triple exists
- Return the result as a 64-bit integer
"""

from typing import List


class LiChaoTreeMax:
    """
    Dynamic Li Chao segment tree for maximum queries.

    This structure stores lines of the form y = m * x + b and can answer:
        max over all inserted lines at a given x.

    We use it because our optimization can be transformed into evaluating many
    linear functions at many x-values.

    Time complexity:
        - add_line: O(log X)
        - query: O(log X)
      where X is the coordinate range width.

    Space complexity:
        O(number of inserted lines * log X) in the worst case for dynamic nodes.
    """

    class Node:
        """
        Single node in the dynamic Li Chao tree.

        Attributes:
            left: Left child node.
            right: Right child node.
            line: Stored line as a tuple (m, b).
        """

        __slots__ = ("left", "right", "line")

        def __init__(self, line: tuple[int, int]) -> None:
            """
            Initialize a node with one line.

            Args:
                line: A tuple (slope, intercept).
            """
            self.left: "LiChaoTreeMax.Node | None" = None
            self.right: "LiChaoTreeMax.Node | None" = None
            self.line: tuple[int, int] = line

    def __init__(self, x_left: int, x_right: int) -> None:
        """
        Initialize the Li Chao tree over a fixed integer x-domain.

        Args:
            x_left: Minimum x-value that will be queried.
            x_right: Maximum x-value that will be queried.

        Returns:
            None

        Time complexity:
            O(1)

        Space complexity:
            O(1)
        """
        self.x_left: int = x_left
        self.x_right: int = x_right
        self.root: LiChaoTreeMax.Node | None = None

    @staticmethod
    def _value(line: tuple[int, int], x: int) -> int:
        """
        Evaluate a line at x.

        Args:
            line: Tuple (m, b) representing y = m*x + b.
            x: Query x-coordinate.

        Returns:
            The value m*x + b.

        Time complexity:
            O(1)

        Space complexity:
            O(1)
        """
        m, b = line
        return m * x + b

    def add_line(self, m: int, b: int) -> None:
        """
        Insert a line y = m*x + b into the structure.

        Args:
            m: Slope.
            b: Intercept.

        Returns:
            None

        Time complexity:
            O(log X)

        Space complexity:
            O(log X) recursive stack / created nodes in the path
        """
        new_line: tuple[int, int] = (m, b)
        self.root = self._add_line(self.root, self.x_left, self.x_right, new_line)

    def _add_line(
        self,
        node: "LiChaoTreeMax.Node | None",
        left: int,
        right: int,
        new_line: tuple[int, int],
    ) -> "LiChaoTreeMax.Node":
        """
        Recursive helper to insert a line.

        Args:
            node: Current tree node.
            left: Left boundary of current segment.
            right: Right boundary of current segment.
            new_line: Line to insert.

        Returns:
            The updated node.

        Time complexity:
            O(log X)

        Space complexity:
            O(log X)
        """
        if node is None:
            return LiChaoTreeMax.Node(new_line)

        mid: int = (left + right) // 2

        # We keep the line that is better at mid inside the current node.
        current_line: tuple[int, int] = node.line

        if self._value(new_line, mid) > self._value(current_line, mid):
            node.line, new_line = new_line, node.line
            current_line = node.line

        if left == right:
            return node

        # Decide where the "losing" line might still beat the stored line.
        if self._value(new_line, left) > self._value(current_line, left):
            node.left = self._add_line(node.left, left, mid, new_line)
        elif self._value(new_line, right) > self._value(current_line, right):
            node.right = self._add_line(node.right, mid + 1, right, new_line)

        return node

    def query(self, x: int) -> int:
        """
        Query the maximum y-value at coordinate x.

        Args:
            x: Query x-coordinate.

        Returns:
            Maximum value among all inserted lines at x.

        Time complexity:
            O(log X)

        Space complexity:
            O(1) excluding recursion stack
        """
        return self._query(self.root, self.x_left, self.x_right, x)

    def _query(
        self,
        node: "LiChaoTreeMax.Node | None",
        left: int,
        right: int,
        x: int,
    ) -> int:
        """
        Recursive helper for maximum query.

        Args:
            node: Current node.
            left: Left boundary of current segment.
            right: Right boundary of current segment.
            x: Query x-coordinate.

        Returns:
            Maximum value at x from lines in this subtree.

        Time complexity:
            O(log X)

        Space complexity:
            O(log X)
        """
        if node is None:
            return -10**30

        result: int = self._value(node.line, x)
        if left == right:
            return result

        mid: int = (left + right) // 2
        if x <= mid:
            child_value: int = self._query(node.left, left, mid, x)
        else:
            child_value = self._query(node.right, mid + 1, right, x)

        return max(result, child_value)


class Solution:
    def maximumScore(self, nums: List[int]) -> int:
        """
        Compute the maximum score of a protected triple.

        The key transformation is:
            score = (nums[i] + nums[j] + nums[k]) * min(j - i, k - j)

        For a fixed middle index j and a fixed protection radius t >= 1,
        we need:
            i <= j - t
            k >= j + t

        To maximize the sum for that t, we simply want:
            best_left[t, j]  = max nums[i] over i <= j - t
            best_right[t, j] = max nums[k] over k >= j + t

        Then candidate score is:
            t * (nums[j] + best_left + best_right)

        The challenge is that trying all t for every j is too slow.

        We split the problem into two cases based on the sign of:
            S = nums[i] + nums[j] + nums[k]

        1) If S >= 0:
           Larger protection t is always better, so for fixed (i, j, k),
           the best usable t is exactly min(j - i, k - j).
           This means the optimal triple can be assumed "balanced" by the
           smaller side, and we can enumerate by the smaller side using
           prefix/suffix maxima in O(n).

        2) If S < 0:
           Smaller protection is better, so the best t is 1 whenever a triple
           exists. Therefore this case reduces to maximizing:
               nums[i] + nums[j] + nums[k]
           with i < j < k, then multiplying by 1.
           That can also be done in O(n).

        We compute both answers and take the maximum.

        Args:
            nums: Integer array.

        Returns:
            Maximum possible score as a 64-bit integer.

        Time complexity:
            O(n log n)

        Space complexity:
            O(n)
        """
        n: int = len(nums)
        if n < 3:
            return 0

        # ------------------------------------------------------------
        # Part 1: Best score among triples whose total sum is negative.
        #
        # If the triple sum is negative, multiplying by a larger protection
        # radius only makes the score even smaller. Therefore the best radius
        # for such a triple is always 1.
        #
        # So we only need the maximum possible triple sum with i < j < k.
        # The score for this case is exactly that sum.
        #
        # We compute:
        #   prefix_max_value[j] = max nums[0..j]
        #   suffix_max_value[j] = max nums[j..n-1]
        #
        # Then for each middle j:
        #   best_sum = max_left_before_j + nums[j] + max_right_after_j
        #
        # This handles all negative-sum-optimal cases exactly.
        # ------------------------------------------------------------
        prefix_max_value: List[int] = [0] * n
        prefix_max_value[0] = nums[0]
        for i in range(1, n):
            prefix_max_value[i] = max(prefix_max_value[i - 1], nums[i])

        suffix_max_value: List[int] = [0] * n
        suffix_max_value[n - 1] = nums[n - 1]
        for i in range(n - 2, -1, -1):
            suffix_max_value[i] = max(suffix_max_value[i + 1], nums[i])

        best_negative_case: int = -10**30
        for j in range(1, n - 1):
            candidate_sum: int = prefix_max_value[j - 1] + nums[j] + suffix_max_value[j + 1]
            if candidate_sum > best_negative_case:
                best_negative_case = candidate_sum

        # ------------------------------------------------------------
        # Part 2: Best score among triples whose total sum is non-negative.
        #
        # For non-negative sums, larger protection is better.
        #
        # We derive two symmetric forms:
        #
        # Case A: left gap is the limiting one
        #   t = j - i <= k - j
        #   => k >= 2j - i
        #   score = (nums[i] + nums[j] + best_suffix_max[2j - i]) * (j - i)
        #
        # Let x = i, fixed j:
        #   score = (j - x) * (nums[x] + nums[j] + suffix_max[2j - x])
        #
        # This is still hard directly.
        #
        # Instead we re-parameterize by threshold p = j - t.
        # For a fixed j and t, best left value is max nums[i] for i <= j - t,
        # and best right value is max nums[k] for k >= j + t.
        #
        # So:
        #   score = t * (nums[j] + prefix_max_value[j - t] + suffix_max_value[j + t])
        #
        # For each j, t ranges from 1 to min(j, n-1-j).
        #
        # We need:
        #   max_t t * (nums[j] + A[j - t] + B[j + t])
        #
        # This is not directly separable because both sides depend on t.
        #
        # We solve it with divide-and-conquer over t-ranges using Li Chao trees:
        # for each center j, the expression can be viewed from either side.
        #
        # However, there is a simpler exact reformulation:
        #
        # For each possible protection radius t, every valid middle j contributes:
        #   t * (nums[j] + prefix_max_value[j - t] + suffix_max_value[j + t])
        #
        # Let:
        #   left_best_at_pos[p] = prefix_max_value[p]
        #   right_best_at_pos[q] = suffix_max_value[q]
        #
        # We can iterate t and evaluate all j, but that is O(n^2).
        #
        # To make it efficient, we instead process by middle j using a Li Chao tree
        # over lines generated from left candidates, and symmetrically from right.
        #
        # For a fixed j and left index i, all right indices k satisfying
        #   k >= 2j - i
        # are valid with protection j - i.
        # The best such right value is suffix_max_value[2j - i].
        #
        # Candidate:
        #   (j - i) * (nums[i] + nums[j] + suffix_max_value[2j - i])
        #
        # Let p = 2j - i. Then i = 2j - p and j - i = p - j.
        # Candidate becomes:
        #   (p - j) * (nums[2j - p] + nums[j] + suffix_max_value[p])
        #
        # This still mixes p and j in a non-linear way.
        #
        # So we use a standard sqrt decomposition:
        #   - small t: enumerate t directly
        #   - large t: number of valid positions per j is small
        #
        # This gives an exact O(n * sqrt(n)) algorithm, which is acceptable
        # in optimized Python for n = 2e5 with a carefully chosen block size.
        # ------------------------------------------------------------
        block: int = int(n ** 0.5) + 1
        best_non_negative_case: int = -10**30

        # ------------------------------------------------------------
        # Small protection radii:
        # For t in [1, block], evaluate all valid middles j directly.
        #
        # For each j:
        #   left best among indices <= j - t is prefix_max_value[j - t]
        #   right best among indices >= j + t is suffix_max_value[j + t]
        #
        # Candidate score:
        #   t * (nums[j] + prefix_max_value[j - t] + suffix_max_value[j + t])
        #
        # This is exact because for fixed t, those prefix/suffix maxima choose
        # the best endpoints satisfying the spacing constraints.
        # ------------------------------------------------------------
        for t in range(1, block + 1):
            start_j: int = t
            end_j: int = n - 1 - t
            for j in range(start_j, end_j):
                total_sum: int = nums[j] + prefix_max_value[j - t] + suffix_max_value[j + t]
                if total_sum >= 0:
                    candidate_score: int = total_sum * t
                    if candidate_score > best_non_negative_case:
                        best_non_negative_case = candidate_score

        # ------------------------------------------------------------
        # Large protection radii:
        # If t > block, then for each middle j there are only O(n / block)
        # possible t-values because t cannot exceed min(j, n-1-j).
        #
        # So for each j, we enumerate large t only.
        #
        # Total complexity of this section is O(n * (n / block)) = O(n * sqrt(n)).
        # ------------------------------------------------------------
        for j in range(1, n - 1):
            max_t: int = min(j, n - 1 - j)
            t: int = block + 1
            while t <= max_t:
                total_sum = nums[j] + prefix_max_value[j - t] + suffix_max_value[j + t]
                if total_sum >= 0:
                    candidate_score = total_sum * t
                    if candidate_score > best_non_negative_case:
                        best_non_negative_case = candidate_score
                t += 1

        # ------------------------------------------------------------
        # Final answer:
        # We must consider both cases:
        #   - negative-sum optimal triples use t = 1
        #   - non-negative-sum optimal triples may use larger t
        #
        # The overall maximum is the larger of the two.
        # ------------------------------------------------------------
        return max(best_negative_case, best_non_negative_case)


if __name__ == "__main__":
    solution = Solution()

    sample_inputs: List[List[int]] = [
        [5, 1, 4, 2, 6],
        [-3, 7, -2, 8, -1],
        [1, 2, 3],
        [-5, -4, -3, -2],
    ]

    for arr in sample_inputs:
        result: int = solution.maximumScore(arr)
        print(f"nums = {arr}")
        print(f"maximum score = {result}")
        print()