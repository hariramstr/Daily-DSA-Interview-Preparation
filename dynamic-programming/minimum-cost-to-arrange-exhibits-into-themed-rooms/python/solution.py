"""
Title: Minimum Cost to Arrange Exhibits into Themed Rooms

Problem Description:
A museum is preparing a long hallway exhibition with n exhibits placed in a fixed
left-to-right order. Each exhibit has an integer theme label given by the array
themes, where themes[i] is the theme of the i-th exhibit. The museum wants to
divide the hallway into exactly k contiguous rooms, and every exhibit must belong
to exactly one room.

The cost of a single room is defined as the number of unordered pairs of exhibits
inside that room that share the same theme label. For example, if a room contains
themes [2, 3, 2, 2], then its cost is 3 because the three equal-theme pairs are
formed by the three exhibits labeled 2. The total arrangement cost is the sum of
the costs of all rooms.

Your task is to return the minimum possible total cost after partitioning the
exhibits into exactly k contiguous rooms.

Constraints:
- 1 <= n <= 1000
- 1 <= k <= min(n, 50)
- 1 <= themes[i] <= 10^5

Examples:
1) themes = [1, 2, 1, 2, 1], k = 2
   Output: 1

2) Corrected Example:
   themes = [4, 4, 4, 5, 5], k = 2
   Output: 4
"""

from typing import Dict, List


class Solution:
    def min_cost(self, themes: List[int], k: int) -> int:
        """
        Compute the minimum total cost to split the array into exactly k contiguous rooms.

        The algorithm uses dynamic programming with divide-and-conquer optimization.
        A helper sliding-window cost structure allows us to evaluate the cost of any
        segment efficiently while recursively computing each DP layer.

        Args:
            themes: List of exhibit theme labels.
            k: Exact number of contiguous rooms.

        Returns:
            The minimum possible total cost.

        Time complexity:
            O(k * n * log n) amortized for the divide-and-conquer DP transitions,
            with O(1) amortized window updates. This is efficient for n <= 1000.

        Space complexity:
            O(n) for DP arrays, plus O(n) for the frequency structure in the current
            window, so overall O(n).
        """
        n: int = len(themes)

        # ------------------------------------------------------------
        # We use 1-based indexing for the main DP logic because partition
        # formulas become much cleaner:
        #
        # dp[g][i] = minimum cost to split the first i exhibits into g rooms
        #
        # Transition:
        # dp[g][i] = min over p from g-1 to i-1 of:
        #            dp[g-1][p] + cost(p+1, i)
        #
        # Here:
        # - p is the end index of the previous group partition
        # - the last room is exhibits [p+1 .. i]
        #
        # To make indexing easy, we create a 1-based copy of themes.
        # ------------------------------------------------------------
        arr: List[int] = [0] + themes

        # ------------------------------------------------------------
        # INF is a very large number used to initialize DP states that
        # have not yet been computed.
        # ------------------------------------------------------------
        inf: int = 10**18

        # ------------------------------------------------------------
        # prev_dp represents dp[g-1][i]
        # curr_dp represents dp[g][i]
        #
        # Base case:
        # dp[0][0] = 0   -> zero exhibits split into zero rooms costs 0
        # dp[0][i] = INF for i > 0 -> impossible
        # ------------------------------------------------------------
        prev_dp: List[int] = [inf] * (n + 1)
        prev_dp[0] = 0

        # ------------------------------------------------------------
        # The next section maintains the cost of a current segment [left, right]
        # using a sliding window.
        #
        # Why this works:
        # The room cost is the number of equal-value unordered pairs.
        # If a value x currently appears c times in the window, then it contributes:
        #   c choose 2 = c * (c - 1) // 2
        #
        # Instead of recomputing this from scratch for every segment, we update
        # incrementally:
        #
        # - When adding a value x with current count c:
        #   the new element forms exactly c new equal pairs with the existing x's.
        #   So cost += c, then count becomes c+1.
        #
        # - When removing a value x with current count c:
        #   that removed element was participating in exactly c-1 pairs.
        #   So cost -= (c-1), then count becomes c-1.
        #
        # This gives O(1) amortized updates for moving the window boundaries.
        # ------------------------------------------------------------
        freq: Dict[int, int] = {}
        left: int = 1
        right: int = 0
        current_cost: int = 0

        def add_right(index: int) -> None:
            """
            Add arr[index] to the right end of the current window.

            Args:
                index: 1-based index to add.

            Returns:
                None

            Time complexity:
                O(1) average

            Space complexity:
                O(1) auxiliary
            """
            nonlocal current_cost
            value: int = arr[index]
            count: int = freq.get(value, 0)

            # The new value forms one new equal pair with each existing identical value.
            current_cost += count
            freq[value] = count + 1

        def remove_right(index: int) -> None:
            """
            Remove arr[index] from the right end of the current window.

            Args:
                index: 1-based index to remove.

            Returns:
                None

            Time complexity:
                O(1) average

            Space complexity:
                O(1) auxiliary
            """
            nonlocal current_cost
            value: int = arr[index]
            count: int = freq[value]

            # Before removal, this element participates in (count - 1) equal pairs.
            current_cost -= count - 1
            if count == 1:
                del freq[value]
            else:
                freq[value] = count - 1

        def add_left(index: int) -> None:
            """
            Add arr[index] to the left end of the current window.

            Args:
                index: 1-based index to add.

            Returns:
                None

            Time complexity:
                O(1) average

            Space complexity:
                O(1) auxiliary
            """
            nonlocal current_cost
            value: int = arr[index]
            count: int = freq.get(value, 0)

            # Same logic as adding on the right:
            # the new item pairs with all existing identical items.
            current_cost += count
            freq[value] = count + 1

        def remove_left(index: int) -> None:
            """
            Remove arr[index] from the left end of the current window.

            Args:
                index: 1-based index to remove.

            Returns:
                None

            Time complexity:
                O(1) average

            Space complexity:
                O(1) auxiliary
            """
            nonlocal current_cost
            value: int = arr[index]
            count: int = freq[value]

            # Same logic as removing on the right.
            current_cost -= count - 1
            if count == 1:
                del freq[value]
            else:
                freq[value] = count - 1

        def move_window(new_left: int, new_right: int) -> int:
            """
            Move the current sliding window to [new_left, new_right] and return its cost.

            This function is the key utility that lets divide-and-conquer DP query
            segment costs efficiently without rebuilding frequency counts from scratch.

            Args:
                new_left: Desired left boundary (1-based, inclusive).
                new_right: Desired right boundary (1-based, inclusive).

            Returns:
                Cost of the segment arr[new_left..new_right].

            Time complexity:
                O(number of boundary moves), amortized efficient across recursion

            Space complexity:
                O(1) auxiliary
            """
            nonlocal left, right

            # Expand or shrink the left boundary until it matches new_left.
            while left > new_left:
                left -= 1
                add_left(left)
            while left < new_left:
                remove_left(left)
                left += 1

            # Expand or shrink the right boundary until it matches new_right.
            while right < new_right:
                right += 1
                add_right(right)
            while right > new_right:
                remove_right(right)
                right -= 1

            return current_cost

        def compute_layer(
            group_count: int,
            l: int,
            r: int,
            opt_l: int,
            opt_r: int,
            curr_dp: List[int],
        ) -> None:
            """
            Recursively compute curr_dp[mid] for a DP layer using divide-and-conquer optimization.

            The optimization relies on the monotonicity of the optimal split point,
            which holds for this cost structure. This reduces the search range for
            each midpoint and makes the overall DP much faster.

            Args:
                group_count: Current number of rooms being formed.
                l: Left boundary of i-values to compute.
                r: Right boundary of i-values to compute.
                opt_l: Lower bound for the optimal split point.
                opt_r: Upper bound for the optimal split point.
                curr_dp: DP array for the current layer.

            Returns:
                None

            Time complexity:
                O((r - l + 1) * log n) style recurrence for the layer overall,
                with O(1) amortized cost updates per candidate movement

            Space complexity:
                O(log n) recursion depth
            """
            if l > r:
                return

            # ------------------------------------------------------------
            # We compute the midpoint first, then search for the best split
            # point p in the restricted range [opt_l, min(opt_r, mid-1)].
            #
            # Why p <= mid - 1:
            # The last room is [p+1 .. mid], and rooms cannot be empty.
            # So p must be strictly before mid.
            #
            # Why p >= group_count - 1:
            # To split the first p exhibits into group_count - 1 non-empty rooms,
            # we need at least group_count - 1 exhibits.
            # ------------------------------------------------------------
            mid: int = (l + r) // 2
            best_cost: int = inf
            best_split: int = -1

            start_p: int = opt_l
            end_p: int = min(opt_r, mid - 1)

            # ------------------------------------------------------------
            # Try every valid split point p for this midpoint.
            #
            # Transition:
            # curr_dp[mid] = min(prev_dp[p] + cost(p+1, mid))
            #
            # We obtain cost(p+1, mid) by moving the maintained window.
            # ------------------------------------------------------------
            for p in range(start_p, end_p + 1):
                segment_cost: int = move_window(p + 1, mid)
                candidate: int = prev_dp[p] + segment_cost

                if candidate < best_cost:
                    best_cost = candidate
                    best_split = p

            curr_dp[mid] = best_cost

            # ------------------------------------------------------------
            # Divide-and-conquer recursion:
            #
            # If best split for mid is best_split, then:
            # - left half [l .. mid-1] only needs to search [opt_l .. best_split]
            # - right half [mid+1 .. r] only needs to search [best_split .. opt_r]
            #
            # This is the classic monotone queue / divide-and-conquer DP optimization.
            # ------------------------------------------------------------
            compute_layer(group_count, l, mid - 1, opt_l, best_split, curr_dp)
            compute_layer(group_count, mid + 1, r, best_split, opt_r, curr_dp)

        # ------------------------------------------------------------
        # Build the DP one group at a time.
        #
        # For each group_count:
        # - We compute dp[group_count][i] for i from group_count to n
        #   because at least one exhibit must go into each room.
        #
        # Before each layer, we reset the sliding window because the recursive
        # evaluation order will move it around many times.
        # ------------------------------------------------------------
        for group_count in range(1, k + 1):
            curr_dp: List[int] = [inf] * (n + 1)

            # Reset the maintained cost window to empty.
            freq.clear()
            left = 1
            right = 0
            current_cost = 0

            # Only i >= group_count are valid states.
            compute_layer(
                group_count=group_count,
                l=group_count,
                r=n,
                opt_l=group_count - 1,
                opt_r=n - 1,
                curr_dp=curr_dp,
            )

            prev_dp = curr_dp

        return prev_dp[n]


if __name__ == "__main__":
    solution = Solution()

    # Example 1
    themes1: List[int] = [1, 2, 1, 2, 1]
    k1: int = 2
    result1: int = solution.min_cost(themes1, k1)
    print(f"themes = {themes1}, k = {k1} -> minimum cost = {result1}")

    # Corrected Example 2
    themes2: List[int] = [4, 4, 4, 5, 5]
    k2: int = 2
    result2: int = solution.min_cost(themes2, k2)
    print(f"themes = {themes2}, k = {k2} -> minimum cost = {result2}")

    # Additional small sanity checks
    themes3: List[int] = [2, 3, 2, 2]
    k3: int = 1
    result3: int = solution.min_cost(themes3, k3)
    print(f"themes = {themes3}, k = {k3} -> minimum cost = {result3}")

    themes4: List[int] = [1, 1, 1, 1]
    k4: int = 4
    result4: int = solution.min_cost(themes4, k4)
    print(f"themes = {themes4}, k = {k4} -> minimum cost = {result4}")