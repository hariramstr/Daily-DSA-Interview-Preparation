"""
Title: Maximum Matched Crates After One Dock Extension

Problem Description:
A warehouse has two sorted arrays: `crates` and `docks`. `crates[i]` is the size requirement
of the i-th outgoing crate, and `docks[j]` is the capacity of the j-th loading dock.
A crate can be assigned to at most one dock, and a dock can load at most one crate.
A crate can only use a dock whose capacity is at least the crate's size.

Before assignments are made, the warehouse may perform at most one temporary dock extension.
This extension can be applied to exactly one dock, increasing its capacity by an integer value
`boost`, where `0 <= boost <= extraCapacity`. The extension is used on only one dock and only
for this assignment batch.

Return the maximum number of crates that can be matched after optimally choosing whether to use
the extension, which dock to apply it to, and how to pair crates with docks.

Both arrays may contain duplicates, and the chosen dock does not need to remain in its original
relative position after being conceptually boosted; only the final matching count matters.
Your solution should be efficient enough for large inputs.

Constraints:
- 1 <= crates.length, docks.length <= 2 * 10^5
- 1 <= crates[i], docks[j] <= 10^9
- 0 <= extraCapacity <= 10^9
- crates is sorted in nondecreasing order
- docks is sorted in nondecreasing order
"""

from bisect import bisect_left
from typing import List


class Solution:
    def max_matched_crates(self, crates: List[int], docks: List[int], extraCapacity: int) -> int:
        """
        Compute the maximum number of crate-dock matches after optionally boosting one dock.

        The key idea is:
        1. Binary search the answer `k` = number of crates we want to match.
        2. Check whether matching `k` crates is possible:
           - Either without using any boost at all, or
           - By using the one allowed boost on exactly one dock.
        3. Because arrays are sorted, the "best" set of crates to try matching for a fixed `k`
           is the smallest `k` crates. If we cannot match those, we cannot match any other `k`
           crates, since any other choice would only be harder.

        Args:
            crates: Sorted list of crate size requirements.
            docks: Sorted list of dock capacities.
            extraCapacity: Maximum extra capacity that can be added to one dock.

        Returns:
            The maximum number of matched crate-dock pairs.

        Time complexity:
            O((n + m) * log(min(n, m))) in practice, where n = len(crates), m = len(docks)

        Space complexity:
            O(n + m) due to helper prefix/suffix arrays used inside feasibility checks.
        """
        n: int = len(crates)
        m: int = len(docks)
        upper: int = min(n, m)

        # Standard binary search on the answer.
        # If we can match k crates, then we can also match any smaller number of crates.
        left: int = 0
        right: int = upper

        while left < right:
            # We bias upward so the loop converges correctly for "maximum feasible".
            mid: int = (left + right + 1) // 2

            if self._can_match_k(crates, docks, extraCapacity, mid):
                left = mid
            else:
                right = mid - 1

        return left

    def _can_match_k(self, crates: List[int], docks: List[int], extraCapacity: int, k: int) -> bool:
        """
        Check whether it is possible to match exactly `k` crates using at most one boosted dock.

        Important observation:
        - For a fixed `k`, it is always optimal to consider the smallest `k` crates.
          If even those cannot be matched, then any larger set of `k` crates also cannot be matched.
        - To maximize the chance of success, we should compare them against the largest `k` docks,
          because smaller docks outside that suffix would never help more than larger ones.

        We therefore reduce the problem to:
        - A = crates[0:k]                (smallest k crates)
        - B = docks[m-k:m]               (largest k docks)

        Then we ask:
        Can we pair all k items from A with all k items from B, where one chosen dock in B may
        be increased by at most extraCapacity?

        The check is done in three stages:
        1. If A can already be matched to B directly, return True.
        2. Precompute which prefixes can be matched directly.
        3. Precompute which suffixes can be matched directly.
        4. Try every possible crate index `i` as the one that will use the boosted dock.
           If there exists some dock in B with capacity + extraCapacity >= A[i], and the crates
           before `i` and after `i` can be matched directly to the remaining docks, return True.

        Args:
            crates: Sorted list of crate size requirements.
            docks: Sorted list of dock capacities.
            extraCapacity: Maximum extra capacity for one dock.
            k: Number of crates we want to match.

        Returns:
            True if matching k crates is possible, otherwise False.

        Time complexity:
            O(k)

        Space complexity:
            O(k)
        """
        if k == 0:
            return True

        m: int = len(docks)

        # We only need the smallest k crates.
        # Any other choice of k crates would have requirements >= these,
        # so if these cannot be matched, no harder choice can be matched either.
        a: List[int] = crates[:k]

        # We only need the largest k docks.
        # If a solution exists using any k docks, replacing them with larger docks cannot hurt.
        b: List[int] = docks[m - k:]

        # ------------------------------------------------------------
        # Step 1: Quick check - can we already match all k without boost?
        # ------------------------------------------------------------
        # Since both arrays are sorted, direct pairwise comparison works:
        # a[i] must be <= b[i] for all i.
        direct_possible: bool = True
        for i in range(k):
            if a[i] > b[i]:
                direct_possible = False
                break

        if direct_possible:
            return True

        # ------------------------------------------------------------
        # Step 2: prefix_ok[i]
        # ------------------------------------------------------------
        # prefix_ok[i] means:
        #   The first i crates a[0..i-1] can be matched directly to the first i docks b[0..i-1].
        #
        # Because arrays are sorted, this is true exactly when:
        #   a[t] <= b[t] for every t in [0, i-1]
        #
        # We build this incrementally so later we can "remove" one crate/dock position and
        # quickly know whether the left side still matches.
        prefix_ok: List[bool] = [False] * (k + 1)
        prefix_ok[0] = True

        for i in range(1, k + 1):
            prefix_ok[i] = prefix_ok[i - 1] and (a[i - 1] <= b[i - 1])

        # ------------------------------------------------------------
        # Step 3: suffix_ok[i]
        # ------------------------------------------------------------
        # suffix_ok[i] means:
        #   The crates a[i..k-1] can be matched directly to the docks b[i..k-1].
        #
        # Again, because arrays are sorted, this is true exactly when:
        #   a[t] <= b[t] for every t in [i, k-1]
        #
        # We build it from right to left.
        suffix_ok: List[bool] = [False] * (k + 1)
        suffix_ok[k] = True

        for i in range(k - 1, -1, -1):
            suffix_ok[i] = suffix_ok[i + 1] and (a[i] <= b[i])

        # ------------------------------------------------------------
        # Step 4: Try using the boost for exactly one crate position i.
        # ------------------------------------------------------------
        # Suppose crate a[i] is the one loaded by the boosted dock.
        #
        # Then:
        # - Crates a[0..i-1] must be matched directly to some i docks on the left.
        #   The natural and optimal choice is b[0..i-1].
        #   This is possible iff prefix_ok[i] is True.
        #
        # - Crates a[i+1..k-1] must be matched directly to the remaining docks on the right
        #   after removing one dock for the boosted crate.
        #
        #   Since one dock is consumed for a[i], the remaining right-side crates
        #   a[i+1], a[i+2], ..., a[k-1]
        #   should align with
        #   b[i],   b[i+1], ..., b[k-2]
        #
        #   In other words, for every t > i we need:
        #       a[t] <= b[t-1]
        #
        # We precompute this condition using a shifted suffix array.
        shifted_suffix_ok: List[bool] = [False] * (k + 1)
        shifted_suffix_ok[k] = True

        # For i = k-1, there are no crates after i, so it is trivially True.
        # For smaller i, we need a[i+1] <= b[i], a[i+2] <= b[i+1], ...
        for i in range(k - 2, -1, -1):
            shifted_suffix_ok[i] = shifted_suffix_ok[i + 1] and (a[i + 1] <= b[i])

        # Now we need to know whether there exists some dock in b that can serve a[i]
        # after boosting, i.e. some b[j] such that:
        #   b[j] + extraCapacity >= a[i]
        #
        # Since b is sorted, the best candidate is the smallest dock that reaches a[i]
        # after boost. Equivalently, we need some j with:
        #   b[j] >= a[i] - extraCapacity
        #
        # But because the left i docks are already reserved for the first i crates,
        # the boosted dock must come from positions j >= i.
        #
        # Therefore, for each i:
        #   1. prefix_ok[i] must hold
        #   2. shifted_suffix_ok[i] must hold
        #   3. There must exist j in [i, k-1] with b[j] >= a[i] - extraCapacity
        #
        # Since b is sorted, we can find the first such j using bisect_left.
        for i in range(k):
            if not prefix_ok[i]:
                continue

            if not shifted_suffix_ok[i]:
                continue

            needed_capacity: int = a[i] - extraCapacity

            # Find the first dock in b with capacity >= needed_capacity.
            # If that position is at least i and still inside the array,
            # then there exists an available dock among b[i..k-1] that can be boosted enough.
            pos: int = bisect_left(b, needed_capacity)

            if pos < i:
                pos = i

            if pos < k:
                return True

        return False


if __name__ == "__main__":
    solution = Solution()

    crates1 = [2, 4, 7, 9]
    docks1 = [3, 5, 8]
    extra1 = 2
    result1 = solution.max_matched_crates(crates1, docks1, extra1)
    print(result1)  # Expected: 3

    crates2 = [3, 6, 6, 10]
    docks2 = [2, 6, 8, 8]
    extra2 = 3
    result2 = solution.max_matched_crates(crates2, docks2, extra2)
    print(result2)  # Expected: 4