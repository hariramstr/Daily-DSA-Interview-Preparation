"""
Title: Longest Restock Interval with One Emergency Purchase

Problem Description:
A store records its daily inventory change for a single product in an integer array
`changes`, where a positive value means items were added and a negative value means
items were sold.

For any chosen contiguous subarray, define its running balance as the prefix sums of
that subarray. The interval is considered stable if, after optionally adding one
emergency purchase of any size on exactly one day inside the interval, every running
balance in that interval becomes at least 0.

Equivalently:
- We may choose at most one index inside the subarray.
- We may increase that day's value by any non-negative amount.
- After that single adjustment, every prefix sum of the chosen subarray must be >= 0.

Return the maximum possible length of a stable contiguous interval.

Key observation:
For a subarray changes[l..r], let its internal prefix sums be:
    s_k = changes[l] + changes[l+1] + ... + changes[k]

If we apply the emergency purchase at position p:
- Prefixes before p are unchanged.
- Prefixes from p onward all increase by the same amount.

Therefore, one purchase can repair all bad prefixes only if all negative prefixes
start at or after some position p. That means the subarray's minimum prefix value
must occur only in a suffix of the prefix sequence, and all earlier prefixes must
already be non-negative.

A much simpler equivalent condition using global prefix sums:
Let global prefix sums be P[0] = 0, P[i] = sum(changes[0:i]).
For subarray [l..r], its internal prefixes are P[t] - P[l] for t in [l+1..r+1].

The subarray is stable with at most one purchase iff:
1) The first internal prefix is non-negative after the purchase point restriction.
   More precisely, all prefixes before the first minimum prefix position must already
   be non-negative.
2) This is equivalent to:
   - Let m be the earliest position in [l+1..r+1] where P attains its minimum over
     that range.
   - Then min(P[l+1..m-1]) >= P[l].

This leads to an O(n log n) solution using:
- Prefix sums
- Previous strictly smaller element on prefix sums
- Earliest minimum position for every right endpoint using a monotonic stack
- A segment tree for range maximum queries
"""

from typing import List


class SegmentTreeMax:
    """Segment tree supporting point updates with max and range maximum queries."""

    def __init__(self, size: int) -> None:
        """
        Initialize an empty max segment tree.

        Args:
            size: Number of positions to store.

        Returns:
            None

        Time complexity:
            O(size)

        Space complexity:
            O(size)
        """
        self.n: int = 1
        while self.n < size:
            self.n <<= 1
        self.data: List[int] = [-1] * (2 * self.n)

    def update(self, index: int, value: int) -> None:
        """
        Set tree[index] = max(tree[index], value).

        Args:
            index: Position to update.
            value: Candidate value.

        Returns:
            None

        Time complexity:
            O(log n)

        Space complexity:
            O(1) extra
        """
        i: int = index + self.n
        if value <= self.data[i]:
            return
        self.data[i] = value
        i >>= 1
        while i:
            left: int = self.data[i << 1]
            right: int = self.data[i << 1 | 1]
            self.data[i] = left if left >= right else right
            i >>= 1

    def query(self, left: int, right: int) -> int:
        """
        Return max value in inclusive range [left, right].

        Args:
            left: Left boundary.
            right: Right boundary.

        Returns:
            Maximum value in the range, or -1 if the range is empty.

        Time complexity:
            O(log n)

        Space complexity:
            O(1) extra
        """
        if left > right:
            return -1

        left += self.n
        right += self.n
        result: int = -1

        while left <= right:
            if left & 1:
                if self.data[left] > result:
                    result = self.data[left]
                left += 1
            if not (right & 1):
                if self.data[right] > result:
                    result = self.data[right]
                right -= 1
            left >>= 1
            right >>= 1

        return result


class Solution:
    def _compress(self, values: List[int]) -> List[int]:
        """
        Coordinate-compress a list of integers.

        Args:
            values: Original values.

        Returns:
            A sorted list of unique values.

        Time complexity:
            O(n log n)

        Space complexity:
            O(n)
        """
        return sorted(set(values))

    def _lower_bound(self, arr: List[int], target: int) -> int:
        """
        Return first index i such that arr[i] >= target.

        Args:
            arr: Sorted list.
            target: Value to search for.

        Returns:
            Lower-bound index.

        Time complexity:
            O(log n)

        Space complexity:
            O(1)
        """
        lo: int = 0
        hi: int = len(arr)
        while lo < hi:
            mid: int = (lo + hi) // 2
            if arr[mid] < target:
                lo = mid + 1
            else:
                hi = mid
        return lo

    def maxStableInterval(self, changes: List[int]) -> int:
        """
        Compute the maximum length of a contiguous interval that can be made to have
        all non-negative running balances using at most one emergency purchase.

        Args:
            changes: Daily inventory changes.

        Returns:
            Maximum stable interval length.

        Time complexity:
            O(n log n)

        Space complexity:
            O(n)
        """
        n: int = len(changes)

        # ------------------------------------------------------------
        # Step 1: Build global prefix sums.
        #
        # We use the standard prefix sum definition:
        #   prefix[0] = 0
        #   prefix[i] = changes[0] + ... + changes[i-1]
        #
        # Then for a subarray [l..r], its internal prefix sums are:
        #   prefix[l+1] - prefix[l],
        #   prefix[l+2] - prefix[l],
        #   ...
        #   prefix[r+1] - prefix[l]
        #
        # This lets us reason about any subarray using only the global prefix array.
        # ------------------------------------------------------------
        prefix: List[int] = [0] * (n + 1)
        for i in range(n):
            prefix[i + 1] = prefix[i] + changes[i]

        # ------------------------------------------------------------
        # Step 2: For every position t in the prefix array, compute prev_smaller[t]:
        #
        # prev_smaller[t] = nearest index j < t such that prefix[j] < prefix[t]
        #
        # Why do we need this?
        # For a fixed right endpoint in prefix-space, we will identify the earliest
        # position m where the minimum prefix value is reached in the chosen range.
        # Then valid left boundaries l must satisfy:
        #   there is NO index in (l, m) with prefix value < prefix[m]
        #
        # That condition is exactly:
        #   prev_smaller[m] <= l
        #
        # We compute prev_smaller using a monotonic increasing stack of indices by
        # prefix value. While the top has value >= current, it cannot be the previous
        # strictly smaller value for the current position, so we pop it.
        # ------------------------------------------------------------
        prev_smaller: List[int] = [-1] * (n + 1)
        stack: List[int] = []
        for i in range(n + 1):
            while stack and prefix[stack[-1]] >= prefix[i]:
                stack.pop()
            prev_smaller[i] = stack[-1] if stack else -1
            stack.append(i)

        # ------------------------------------------------------------
        # Step 3: For every right endpoint j in prefix-space (meaning subarray right
        # endpoint r = j - 1 in the original array), find:
        #
        #   min_pos[j] = earliest position m in [1..j] where prefix[m] is minimal
        #                over prefix[1..j]
        #
        # Why earliest?
        # If the minimum appears multiple times, the first time it appears is the
        # critical split point:
        # - Prefixes before that point cannot be repaired by a purchase placed later.
        # - Prefixes from that point onward all can be lifted together by one purchase.
        #
        # We can compute this incrementally:
        # - Keep a stack of indices with strictly increasing prefix values.
        # - For each j, the bottom of that stack is the earliest global minimum among
        #   positions seen so far in 1..j.
        #
        # Since we only need the earliest minimum in prefix[1..j], we process j from
        # left to right and maintain the stack over positions 1..j.
        # ------------------------------------------------------------
        min_pos: List[int] = [0] * (n + 1)
        min_stack: List[int] = []
        for j in range(1, n + 1):
            while min_stack and prefix[min_stack[-1]] > prefix[j]:
                min_stack.pop()
            min_stack.append(j)
            min_pos[j] = min_stack[0]

        # ------------------------------------------------------------
        # Step 4: Coordinate compression for prefix values.
        #
        # We need to answer queries of the form:
        #   among all candidate left boundaries l in some index range,
        #   find one with prefix[l] <= threshold and maximize l
        #
        # A convenient way:
        # - Compress all prefix values
        # - Process left boundaries in descending order of prefix value
        # - Use a segment tree indexed by l to mark which left boundaries are currently
        #   "active" (those with prefix[l] <= current threshold)
        # - Query the maximum active index inside an allowed index interval
        #
        # This turns the value condition into an offline sweep.
        # ------------------------------------------------------------
        coords: List[int] = self._compress(prefix)

        # ------------------------------------------------------------
        # Step 5: Prepare left boundaries sorted by prefix value.
        #
        # Each left boundary is an index l in [0..n-1], because a non-empty subarray
        # [l..r] corresponds to prefix right endpoint j = r + 1 > l.
        #
        # We sort left indices by prefix[l] ascending. During the sweep for a threshold
        # T, we activate all l with prefix[l] <= T.
        # ------------------------------------------------------------
        left_indices: List[int] = list(range(n))
        left_indices.sort(key=lambda idx: prefix[idx])

        # ------------------------------------------------------------
        # Step 6: Build queries for each prefix right endpoint j.
        #
        # For a fixed j:
        #   m = min_pos[j] = earliest minimum position in prefix[1..j]
        #
        # A left boundary l is valid iff:
        #   0 <= l < j
        #   prev_smaller[m] <= l
        #   prefix[l] <= prefix[m]
        #
        # Explanation:
        # - prefix[m] is the minimum internal prefix value candidate.
        # - If there were an even smaller prefix between l+1 and m-1, then m would not
        #   be the first minimum in that subarray; such a negative prefix before the
        #   repair point would be impossible to fix later.
        # - prev_smaller[m] is the last place before m where prefix is strictly smaller
        #   than prefix[m]. So requiring prev_smaller[m] <= l ensures no such smaller
        #   value lies inside (l, m).
        # - prefix[l] <= prefix[m] ensures the minimum internal prefix value
        #   prefix[m] - prefix[l] is <= 0 and can be repaired by adding enough amount
        #   at position m-1 (or earlier at the first minimum day inside the subarray).
        #   If prefix[l] > prefix[m], then the minimum internal prefix is negative,
        #   which is fine because one purchase can lift it. The real restriction is
        #   that all earlier prefixes before m are already non-negative:
        #       min(prefix[l+1..m-1]) >= prefix[l]
        #   and because m is the first minimum, this is equivalent to choosing l so
        #   that no prefix < prefix[m] occurs before m and prefix[m] is the first dip.
        #
        # The offline query we actually need is:
        #   among l in [prev_smaller[m], j-1], find the largest l with prefix[l] <= prefix[m]
        #
        # Note:
        # If prev_smaller[m] = -1, then l can start from 0.
        # ------------------------------------------------------------
        queries: List[tuple[int, int, int]] = []
        for j in range(1, n + 1):
            m: int = min_pos[j]
            threshold: int = prefix[m]
            left_limit: int = max(0, prev_smaller[m])
            right_limit: int = j - 1
            queries.append((threshold, left_limit, right_limit, j))

        # Sort queries by threshold ascending so we can activate left boundaries with
        # prefix[l] <= threshold in one forward sweep.
        queries.sort(key=lambda item: item[0])

        # ------------------------------------------------------------
        # Step 7: Offline sweep with a segment tree.
        #
        # Segment tree meaning:
        # - Position l stores value l if left boundary l is active
        # - Otherwise it stores -1
        #
        # Because the stored value equals the index itself, a range maximum query over
        # [L, R] directly returns the largest active left boundary in that range.
        #
        # That is exactly what we want, because for a fixed right endpoint j, the best
        # subarray length is maximized by choosing the largest valid l:
        #   length = j - l
        # ------------------------------------------------------------
        seg: SegmentTreeMax = SegmentTreeMax(n)
        ptr: int = 0
        answer: int = 1

        for threshold, left_limit, right_limit, j in queries:
            while ptr < n and prefix[left_indices[ptr]] <= threshold:
                l_idx: int = left_indices[ptr]
                seg.update(l_idx, l_idx)
                ptr += 1

            best_l: int = seg.query(left_limit, right_limit)
            if best_l != -1:
                current_length: int = j - best_l
                if current_length > answer:
                    answer = current_length

        return answer


if __name__ == "__main__":
    solution = Solution()

    sample_1: List[int] = [4, -6, 3, 2]
    result_1: int = solution.maxStableInterval(sample_1)
    print("Input:", sample_1)
    print("Output:", result_1)
    print("Expected:", 4)
    print()

    sample_2: List[int] = [-3, 5, -2, 1, -4]
    result_2: int = solution.maxStableInterval(sample_2)
    print("Input:", sample_2)
    print("Output:", result_2)
    print("Expected:", 4)
    print()

    extra_samples: List[List[int]] = [
        [1],
        [-1],
        [5, -2, -2],
        [-5, 10],
        [2, -3, 1],
        [3, -10, 4, 4],
    ]

    for arr in extra_samples:
        print("Input:", arr)
        print("Output:", solution.maxStableInterval(arr))
        print()