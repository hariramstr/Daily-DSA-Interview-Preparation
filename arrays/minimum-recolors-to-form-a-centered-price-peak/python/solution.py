"""
Title: Minimum Recolors to Form a Centered Price Peak

Problem Description:
You are given an integer array prices representing daily prices of a product. A day i is
called a centered peak of radius r if there are at least r elements on both sides of i,
and the subarray from i - r to i + r forms a strict mountain centered at i.

In other words, for every d from 1 to r:
- prices[i - d] < prices[i - d + 1]
- prices[i + d - 1] > prices[i + d]

The value at index i is the unique highest value in that window.

You may recolor (change) any element to any integer value in one operation. Return the
minimum number of elements that must be recolored so that the array contains at least one
centered peak of exact radius k.

Only the elements inside the chosen window of length 2k + 1 matter. You may choose any
valid center i such that the full window fits inside the array. A position already
satisfying the required strict relation does not need to be changed.

Your task is to find the minimum number of changes over all possible windows of length
2k + 1.

Constraints:
- 1 <= prices.length <= 200000
- 0 <= prices[i] <= 1000000000
- 1 <= k
- 2 * k + 1 <= prices.length
"""

from typing import List


class Solution:
    def minimum_recolors(self, prices: List[int], k: int) -> int:
        """
        Compute the minimum number of elements to recolor so that some window of length
        2k + 1 becomes a strict mountain centered at its middle.

        Key idea:
        A window [L .. R] with center C = L + k is valid if:
        - every adjacent pair on the left half is strictly increasing
        - every adjacent pair on the right half is strictly decreasing

        Because we may change any element to any integer, each "bad" adjacent comparison
        must be fixed by changing at least one endpoint. This becomes a minimum vertex
        cover problem on a path graph:
        - vertices = positions in the chosen window
        - edges = adjacent comparisons that currently fail
        - changing a position "covers" all bad comparisons touching it

        On a path, the minimum vertex cover can be computed greedily / by DP, but doing
        that for every window independently would be too slow.

        We exploit the special structure:
        - The left half and right half are two disjoint paths that only meet at the center.
        - Therefore, for a fixed window:
            answer = min(
                left_cost_if_center_unchanged + right_cost_if_center_unchanged,
                1 + left_cost_after_forcing_center_changed + right_cost_after_forcing_center_changed
            )

        We precompute, for every possible center:
        - the minimum cover size on the left path with the center excluded
        - the minimum cover size on the left path after removing the center edge by changing center
        - similarly for the right path

        These values can be derived from simple prefix/suffix DP on path graphs.

        Args:
            prices: Array of daily prices.
            k: Exact radius of the centered peak.

        Returns:
            Minimum number of recolors needed.

        Time complexity:
            O(n)

        Space complexity:
            O(n)
        """
        n: int = len(prices)

        # ---------------------------------------------------------------------
        # Step 1: Build arrays describing which adjacent comparisons are "bad".
        #
        # For a centered mountain:
        # - On the left side of the center, values must strictly increase as we move right.
        #   So for adjacent pair (j, j+1), the required relation is prices[j] < prices[j+1].
        #   If this is NOT true, then this edge is bad for a left-half path.
        #
        # - On the right side of the center, values must strictly decrease as we move right.
        #   So for adjacent pair (j, j+1), the required relation is prices[j] > prices[j+1].
        #   If this is NOT true, then this edge is bad for a right-half path.
        #
        # We store these as 0/1 arrays over the n-1 adjacent edges.
        # ---------------------------------------------------------------------
        left_bad: List[int] = [0] * (n - 1)
        right_bad: List[int] = [0] * (n - 1)

        for j in range(n - 1):
            if prices[j] >= prices[j + 1]:
                left_bad[j] = 1
            if prices[j] <= prices[j + 1]:
                right_bad[j] = 1

        # ---------------------------------------------------------------------
        # Step 2: Prefix DP for minimum vertex cover on a path.
        #
        # We need a reusable DP for many subpaths of edges.
        #
        # Consider a path with vertices 0..m and edges 0..m-1, where edge e connects
        # vertex e and e+1. Some edges are "required" (bad comparisons that must be fixed).
        #
        # Standard DP over vertices:
        #   dp0[i] = minimum cost for vertices [0..i] with vertex i NOT chosen
        #   dp1[i] = minimum cost for vertices [0..i] with vertex i chosen
        #
        # Transition for edge (i-1, i):
        # - If that edge is required, then at least one endpoint must be chosen.
        # - If not required, no constraint.
        #
        # We build this DP for:
        #   A) left_bad as a prefix DP from left to right
        #   B) right_bad as a prefix DP from left to right
        #
        # Then we also build suffix-style DP by reversing the same logic, which lets us
        # query right-side path segments efficiently.
        # ---------------------------------------------------------------------
        pref_left0: List[int] = [0] * n
        pref_left1: List[int] = [0] * n
        pref_right0: List[int] = [0] * n
        pref_right1: List[int] = [0] * n

        # Base case for a single vertex path:
        # - not chosen => cost 0
        # - chosen => cost 1
        pref_left0[0] = 0
        pref_left1[0] = 1
        pref_right0[0] = 0
        pref_right1[0] = 1

        for i in range(1, n):
            # ---------------- Left-bad path prefix DP ----------------
            if left_bad[i - 1] == 1:
                # Edge (i-1, i) must be covered.
                # If i is not chosen, then i-1 must be chosen.
                pref_left0[i] = pref_left1[i - 1]
                # If i is chosen, previous vertex may be chosen or not.
                pref_left1[i] = min(pref_left0[i - 1], pref_left1[i - 1]) + 1
            else:
                # Edge does not matter; no coverage constraint.
                pref_left0[i] = min(pref_left0[i - 1], pref_left1[i - 1])
                pref_left1[i] = min(pref_left0[i - 1], pref_left1[i - 1]) + 1

            # ---------------- Right-bad path prefix DP ----------------
            if right_bad[i - 1] == 1:
                pref_right0[i] = pref_right1[i - 1]
                pref_right1[i] = min(pref_right0[i - 1], pref_right1[i - 1]) + 1
            else:
                pref_right0[i] = min(pref_right0[i - 1], pref_right1[i - 1])
                pref_right1[i] = min(pref_right0[i - 1], pref_right1[i - 1]) + 1

        # ---------------------------------------------------------------------
        # Step 3: Suffix DP for minimum vertex cover on a path segment [i..n-1].
        #
        # Similar meaning:
        #   suf0[i] = minimum cost for vertices [i..n-1] with vertex i NOT chosen
        #   suf1[i] = minimum cost for vertices [i..n-1] with vertex i chosen
        #
        # This is useful for right-side windows and for extracting subpath costs.
        # ---------------------------------------------------------------------
        suf_left0: List[int] = [0] * n
        suf_left1: List[int] = [0] * n
        suf_right0: List[int] = [0] * n
        suf_right1: List[int] = [0] * n

        suf_left0[n - 1] = 0
        suf_left1[n - 1] = 1
        suf_right0[n - 1] = 0
        suf_right1[n - 1] = 1

        for i in range(n - 2, -1, -1):
            # Edge (i, i+1) is the first edge in this suffix path.
            if left_bad[i] == 1:
                # If i is not chosen, i+1 must be chosen.
                suf_left0[i] = suf_left1[i + 1]
                # If i is chosen, i+1 may be chosen or not.
                suf_left1[i] = 1 + min(suf_left0[i + 1], suf_left1[i + 1])
            else:
                suf_left0[i] = min(suf_left0[i + 1], suf_left1[i + 1])
                suf_left1[i] = 1 + min(suf_left0[i + 1], suf_left1[i + 1])

            if right_bad[i] == 1:
                suf_right0[i] = suf_right1[i + 1]
                suf_right1[i] = 1 + min(suf_right0[i + 1], suf_right1[i + 1])
            else:
                suf_right0[i] = min(suf_right0[i + 1], suf_right1[i + 1])
                suf_right1[i] = 1 + min(suf_right0[i + 1], suf_right1[i + 1])

        # ---------------------------------------------------------------------
        # Helper functions:
        #
        # We need costs for path segments of exactly k edges on either side of a center.
        #
        # Left side of center c:
        #   vertices [c-k .. c], edges [c-k .. c-1] using left_bad
        #
        # Right side of center c:
        #   vertices [c .. c+k], edges [c .. c+k-1] using right_bad
        #
        # For each side we need two values:
        # 1) cost when center is NOT changed
        #    => center vertex is forced "not chosen"
        # 2) cost when center IS changed
        #    => center vertex is forced "chosen", but since we already count the center
        #       globally once, the side contribution should exclude that +1.
        #
        # For a path segment, these values can be extracted by combining prefix/suffix DPs.
        #
        # Because each side segment touches the center at one endpoint, the formulas become
        # especially simple:
        #
        # Left segment [L..C]:
        #   cost_keep_center = minimum cover on this path with vertex C not chosen
        #   cost_change_center = minimum cover on this path with vertex C chosen, minus 1
        #
        # Right segment [C..R]:
        #   cost_keep_center = minimum cover on this path with vertex C not chosen
        #   cost_change_center = minimum cover on this path with vertex C chosen, minus 1
        #
        # We compute these using local DP over exactly k edges by rolling arrays.
        # Since k can be large and we need all centers, we instead derive them with
        # sliding endpoint recurrences below.
        # ---------------------------------------------------------------------

        # ---------------------------------------------------------------------
        # Step 4: Compute left-side costs for every center.
        #
        # We process centers from left to right and maintain DP for the current window
        # path of length k ending at the center.
        #
        # For a fixed center c, the left path is vertices [c-k .. c].
        # We want:
        #   left_keep[c]   = min cost with vertex c not chosen
        #   left_change[c] = min cost with vertex c chosen, excluding the cost of c itself
        #
        # To keep the implementation beginner-friendly and still linear, we use a compact
        # DP over the k edges for each center via recurrence reuse:
        #
        # Since the path endpoint moves by one each time, we can build these values from
        # prefix DP on the full array:
        #
        # For path [L..C], if we run the standard prefix DP from L to C, the endpoint states
        # depend only on edges in [L..C-1]. To extract them in O(1), we use the fact that
        # the DP is linear and can be represented by 2x2 min-plus transitions. However,
        # implementing a segment tree of min-plus matrices would add complexity.
        #
        # There is a simpler observation for this problem:
        # On a path, minimum vertex cover size equals maximum matching size.
        # For a path of required edges, the greedy left-to-right matching is optimal.
        # Therefore, the minimum number of changed positions needed on one side equals the
        # maximum number of pairwise non-adjacent bad edges in that side path.
        #
        # Additionally:
        # - If center is forced unchanged, the last edge touching center cannot be covered
        #   by center, so the path effectively ends with endpoint unavailable.
        # - If center is forced changed, the last edge is automatically covered if bad, and
        #   we solve the remaining shorter prefix.
        #
        # This leads to a very simple DP on bad-edge arrays:
        #
        # For an edge array b over a path:
        #   match[i] = maximum matching size using edges up to i
        #   match[i] = max(match[i-1], match[i-2] + b[i])
        #
        # Because only bad edges matter, this exactly counts the minimum cover size.
        #
        # We now precompute this for left_bad and right_bad.
        # ---------------------------------------------------------------------
        match_left: List[int] = [0] * (n - 1)
        match_right: List[int] = [0] * (n - 1)

        for i in range(n - 1):
            take_left = left_bad[i]
            take_right = right_bad[i]

            if i >= 2:
                take_left += match_left[i - 2]
                take_right += match_right[i - 2]

            skip_left = match_left[i - 1] if i >= 1 else 0
            skip_right = match_right[i - 1] if i >= 1 else 0

            match_left[i] = max(skip_left, take_left)
            match_right[i] = max(skip_right, take_right)

        # Suffix version of the same matching DP.
        suf_match_left: List[int] = [0] * (n - 1)
        suf_match_right: List[int] = [0] * (n - 1)

        for i in range(n - 2, -1, -1):
            take_left = left_bad[i]
            take_right = right_bad[i]

            if i + 2 <= n - 2:
                take_left += suf_match_left[i + 2]
                take_right += suf_match_right[i + 2]

            skip_left = suf_match_left[i + 1] if i + 1 <= n - 2 else 0
            skip_right = suf_match_right[i + 1] if i + 1 <= n - 2 else 0

            suf_match_left[i] = max(skip_left, take_left)
            suf_match_right[i] = max(skip_right, take_right)

        def segment_matching(prefix_match: List[int], bad: List[int], l: int, r: int) -> int:
            """
            Return maximum matching size on the subarray of edges bad[l..r], where edges
            form a contiguous path.

            This helper uses a tiny local DP when the segment is queried. Since each center
            is queried once and total queried length would be too large if done naively,
            we only use this helper on O(1)-sized derived segments below.

            Args:
                prefix_match: Unused in the final local computation, kept for signature clarity.
                bad: Edge-bad array.
                l: Left edge index.
                r: Right edge index.

            Returns:
                Maximum matching size on that edge path.

            Time complexity:
                O(r - l + 1)

            Space complexity:
                O(1)
            """
            if l > r:
                return 0

            prev2 = 0
            prev1 = 0
            for idx in range(l, r + 1):
                cur = max(prev1, prev2 + bad[idx])
                prev2, prev1 = prev1, cur
            return prev1

        # ---------------------------------------------------------------------
        # Step 5: Evaluate every possible center.
        #
        # For center c:
        # Left edges are [c-k .. c-1] with left_bad
        # Right edges are [c .. c+k-1] with right_bad
        #
        # We need:
        # A) center unchanged
        #    - left side: minimum cover on left path with endpoint c unavailable
        #      This is exactly the minimum cover on edges [c-k .. c-2], plus possibly
        #      edge c-1 if it is bad and then vertex c-1 must be chosen.
        #      Equivalently, it is the path DP with endpoint fixed not chosen.
        #
        # B) center changed
        #    - center itself contributes +1 globally
        #    - any bad edge touching center is automatically covered by center
        #    - remaining work is on:
        #         left edges [c-k .. c-2]
        #         right edges [c+1 .. c+k-1]
        #
        # To keep correctness crystal-clear, we compute the two side costs with small
        # endpoint-aware DP over exactly k edges. Since k can be large, that would be too
        # slow overall, so instead we use the standard path DP but only over the current
        # window edges. This remains O(nk) and is not acceptable.
        #
        # Therefore, we use a simpler and fully correct characterization:
        #
        # For a path, minimum vertex cover with one endpoint forbidden/forced can be solved
        # by the same DP. Since the path length is exactly k and windows slide by one, we
        # can maintain these endpoint-aware DPs incrementally using deques of 2x2 min-plus
        # transition matrices.
        # ---------------------------------------------------------------------

        INF