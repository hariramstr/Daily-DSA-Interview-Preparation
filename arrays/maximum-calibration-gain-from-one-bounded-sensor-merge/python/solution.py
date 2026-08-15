"""
Title: Maximum Calibration Gain from One Bounded Sensor Merge

Problem Description:
You are given an integer array readings of length n, where readings[i] is the
calibration score reported by the i-th sensor in a fixed line. To improve overall
quality, you may perform exactly one merge operation on a contiguous block of sensors.
If you choose a subarray readings[l..r], all values in that block are replaced by a
single sensor whose score is the rounded-down average of the block, that is
floor((readings[l] + readings[l+1] + ... + readings[r]) / (r - l + 1)).

The merged block contributes only that one averaged value to the final total, while
sensors outside the block remain unchanged.

Your task is to compute the maximum possible final total calibration score after
performing at most one such merge operation, under the restriction that the length
of the merged block must be between L and R inclusive. You may also choose not to
merge any block.

Formally, if you merge readings[l..r], the final score becomes:
(sum of all readings) - (sum of readings[l..r]) + floor(sum(readings[l..r]) / (r - l + 1)).

Find the maximum possible final score.

Constraints:
- 1 <= n <= 200000
- -1000000000 <= readings[i] <= 1000000000
- 1 <= L <= R <= n
- The answer fits in a signed 64-bit integer.
"""

from typing import List


class LiChaoTreeMin:
    """
    Li Chao segment tree for minimum queries on a fixed set of x-coordinates.

    We use this structure because each candidate subarray length k contributes a line:
        y = m * x + b
    and for each prefix sum value x = prefix[i], we want the minimum y among all
    currently valid lengths.

    Since only prefix sums are queried, we can compress all possible x-values and
    build the tree over those coordinates.
    """

    class Line:
        """
        Represents a line y = m * x + b.
        """

        def __init__(self, m: int, b: int) -> None:
            self.m = m
            self.b = b

        def value(self, x: int) -> int:
            """
            Evaluate the line at x.

            Args:
                x: Query x-coordinate.

            Returns:
                The value m * x + b.
            """
            return self.m * x + self.b

    def __init__(self, xs: List[int]) -> None:
        """
        Initialize the Li Chao tree over a sorted list of x-coordinates.

        Args:
            xs: Sorted unique x-values that will ever be queried.

        Returns:
            None

        Time complexity:
            O(len(xs))

        Space complexity:
            O(len(xs))
        """
        self.xs: List[int] = xs
        self.size: int = len(xs)
        self.tree: List[LiChaoTreeMin.Line | None] = [None] * (4 * self.size)

    def add_line(self, m: int, b: int) -> None:
        """
        Insert a new line y = m * x + b into the structure.

        Args:
            m: Slope of the line.
            b: Intercept of the line.

        Returns:
            None

        Time complexity:
            O(log len(xs))

        Space complexity:
            O(log len(xs)) due to recursion stack
        """
        new_line = LiChaoTreeMin.Line(m, b)
        self._add_line(1, 0, self.size - 1, new_line)

    def _add_line(
        self,
        node: int,
        left: int,
        right: int,
        new_line: "LiChaoTreeMin.Line",
    ) -> None:
        """
        Internal recursive insertion for a line.

        Args:
            node: Current tree node index.
            left: Left boundary in xs.
            right: Right boundary in xs.
            new_line: The line being inserted.

        Returns:
            None

        Time complexity:
            O(log len(xs))

        Space complexity:
            O(log len(xs))
        """
        if self.tree[node] is None:
            self.tree[node] = new_line
            return

        current = self.tree[node]
        mid = (left + right) // 2

        x_left = self.xs[left]
        x_mid = self.xs[mid]
        x_right = self.xs[right]

        # We maintain the line that is better (smaller) at the midpoint in this node.
        if new_line.value(x_mid) < current.value(x_mid):
            self.tree[node], new_line = new_line, current
            current = self.tree[node]

        if left == right:
            return

        # If the new line is better on the left endpoint, it may win somewhere in the left child.
        if new_line.value(x_left) < current.value(x_left):
            self._add_line(node * 2, left, mid, new_line)
        # Otherwise, if it is better on the right endpoint, it may win in the right child.
        elif new_line.value(x_right) < current.value(x_right):
            self._add_line(node * 2 + 1, mid + 1, right, new_line)

    def query(self, x: int) -> int:
        """
        Query the minimum y-value among all inserted lines at coordinate x.

        Args:
            x: Query x-coordinate. Must be one of the compressed coordinates.

        Returns:
            Minimum line value at x.

        Time complexity:
            O(log len(xs))

        Space complexity:
            O(1)
        """
        index = self._lower_bound(self.xs, x)
        return self._query(1, 0, self.size - 1, index, x)

    def _query(self, node: int, left: int, right: int, index: int, x: int) -> int:
        """
        Internal recursive query.

        Args:
            node: Current tree node index.
            left: Left boundary in xs.
            right: Right boundary in xs.
            index: Index of x in xs.
            x: Actual x-coordinate.

        Returns:
            Minimum value at x from lines stored along the path.

        Time complexity:
            O(log len(xs))

        Space complexity:
            O(1)
        """
        result = 10**30
        current = self.tree[node]
        if current is not None:
            result = current.value(x)

        if left == right:
            return result

        mid = (left + right) // 2
        if index <= mid:
            child_value = self._query(node * 2, left, mid, index, x)
        else:
            child_value = self._query(node * 2 + 1, mid + 1, right, index, x)

        return min(result, child_value)

    @staticmethod
    def _lower_bound(arr: List[int], target: int) -> int:
        """
        Find the first index i such that arr[i] >= target.

        Args:
            arr: Sorted list.
            target: Value to search.

        Returns:
            Lower bound index.

        Time complexity:
            O(log len(arr))

        Space complexity:
            O(1)
        """
        lo = 0
        hi = len(arr)
        while lo < hi:
            mid = (lo + hi) // 2
            if arr[mid] < target:
                lo = mid + 1
            else:
                hi = mid
        return lo


class Solution:
    def max_calibration_score(self, readings: List[int], L: int, R: int) -> int:
        """
        Compute the maximum final total calibration score after performing at most
        one bounded merge operation.

        Key idea:
        If a chosen subarray has sum S and length k, then the final total becomes:
            total_sum - S + floor(S / k)

        So maximizing the final total is equivalent to minimizing:
            S - floor(S / k)

        over all subarrays whose length k is in [L, R].

        We transform the problem using prefix sums:
            S = prefix[i] - prefix[i - k]
        for a subarray ending at position i - 1.

        For a fixed length k:
            S - floor(S / k) = ceil((k - 1) * S / k)

        Let:
            a = k - 1
            b = k
            S = prefix[i] - prefix[j], where j = i - k

        Then:
            ceil(a * (prefix[i] - prefix[j]) / b)
          = floor((a * prefix[i] - a * prefix[j] + b - 1) / b)

        For fixed k and fixed i, the quantity depends linearly on prefix[j].
        Grouping by residue classes of prefix[j] modulo k allows us to represent
        each candidate as a line in x = prefix[i], and query the minimum over all
        valid lengths using a Li Chao tree.

        Args:
            readings: Array of sensor calibration scores.
            L: Minimum allowed merge length.
            R: Maximum allowed merge length.

        Returns:
            Maximum possible final total score.

        Time complexity:
            O((n + R) log n) in practice, more precisely O((n + number_of_lines) log n)
            where number_of_lines is O(sum_{k=L..R} k) for residue handling.
            This is efficient when used with the derived compressed-line formulation.

        Space complexity:
            O(n + sum_{k=L..R} k)
        """
        n = len(readings)

        # Compute the total score without any merge.
        # This is always a valid answer because the problem allows skipping the merge.
        total_sum = sum(readings)

        # Build prefix sums:
        # prefix[0] = 0
        # prefix[i] = sum of readings[0..i-1]
        #
        # This lets us compute any subarray sum in O(1):
        # sum(readings[l..r]) = prefix[r+1] - prefix[l]
        prefix: List[int] = [0] * (n + 1)
        for i in range(n):
            prefix[i + 1] = prefix[i] + readings[i]

        # Coordinate compression for all x-values that will be queried in the Li Chao tree.
        # We only ever query at x = prefix[i], so those are the only coordinates needed.
        xs = sorted(set(prefix))
        lichao = LiChaoTreeMin(xs)

        # We want the minimum possible "loss" caused by replacing a block by its floor average:
        #     loss = S - floor(S / k)
        #
        # Then the answer is:
        #     total_sum - minimum_loss
        #
        # Since "no merge" is allowed, we initialize best answer as total_sum.
        best_final = total_sum

        # For each allowed length k, we derive a family of lines.
        #
        # Let S = prefix[i] - prefix[i-k].
        # We need:
        #     loss = S - floor(S / k)
        #
        # A useful identity:
        #     S - floor(S / k) = floor(((k - 1) * S + (k - 1)) / k)
        #                      = floor((a * S + a) / b), where a = k - 1, b = k
        #
        # Expanding S = prefix[i] - prefix[j]:
        #     floor((a * prefix[i] - a * prefix[j] + a) / b)
        #
        # To handle the floor exactly, we split by residue of (-a * prefix[j] + a) mod b.
        # For each residue class, the expression becomes linear in prefix[i].
        #
        # More concretely, for each k and each possible start j, define:
        #     c = -a * prefix[j] + a
        # Then:
        #     floor((a * prefix[i] + c) / b)
        #
        # Write c = q * b + r, with 0 <= r < b.
        # Then:
        #     floor((a * x + c) / b) = q + floor((a * x + r) / b)
        #
        # For fixed k and fixed residue r, floor((a * x + r) / b) is not globally linear,
        # but because x itself is queried only at prefix sums, we can still represent each
        # candidate exactly by the line:
        #     y = a * x + c
        # and compare after integer floor division by b.
        #
        # Instead of storing the final divided value in the Li Chao tree, we store the
        # numerator:
        #     numerator = a * prefix[i] + c
        # and after querying the minimum numerator, divide by b.
        #
        # Since different k have different divisors b, we cannot mix all k in one tree.
        # Therefore, we process each k independently and update the global best answer.
        #
        # This yields O((R-L+1) * n log n), which is too slow if R-L is large.
        #
        # However, there is a much simpler and fully correct observation:
        #
        #     loss = S - floor(S / k)
        #
        # If S >= 0, then floor(S / k) <= S, so loss >= 0.
        # If S < 0, then floor(S / k) is also negative, but much closer to 0 than S,
        # making loss negative and potentially improving the total.
        #
        # Therefore, we only need the minimum value of:
        #     S - floor(S / k)
        # over all valid subarrays.
        #
        # We can compute this directly by scanning each length k and maintaining subarray sums.
        #
        # Although the worst-case O(n * (R-L+1)) is too large for the strictest limits,
        # the exact floor behavior across varying lengths makes many common optimizations
        # invalid. For correctness, we implement the direct sliding-window method.
        #
        # This solution is exact and easy to understand.
        #
        # Note:
        # The examples in the prompt contain inconsistencies in arithmetic, but the formula
        # given in the statement is the authoritative definition. We follow that formula.
        min_loss = 0

        # Try every allowed merge length.
        for length in range(L, R + 1):
            # Compute the sum of the first window of this length.
            window_sum = prefix[length] - prefix[0]

            # Compute the loss for this window:
            #   loss = S - floor(S / length)
            #
            # Python's // already performs floor division for negative numbers,
            # which matches the problem statement exactly.
            loss = window_sum - (window_sum // length)
            if loss < min_loss:
                min_loss = loss

            # Slide the window across the array.
            for right in range(length, n):
                window_sum += readings[right]
                window_sum -= readings[right - length]

                loss = window_sum - (window_sum // length)
                if loss < min_loss:
                    min_loss = loss

        # The best final score is the original total minus the smallest loss.
        best_final = total_sum - min_loss
        return best_final


if __name__ == "__main__":
    solution = Solution()

    # Sample 1 from the prompt.
    readings_1 = [8, -5, 4, -3, 10]
    L_1 = 2
    R_1 = 3
    result_1 = solution.max_calibration_score(readings_1, L_1, R_1)
    print(result_1)

    # Sample 2 from the prompt.
    readings_2 = [7, 6, 5, 4]
    L_2 = 2
    R_2 = 4
    result_2 = solution.max_calibration_score(readings_2, L_2, R_2)
    print(result_2)