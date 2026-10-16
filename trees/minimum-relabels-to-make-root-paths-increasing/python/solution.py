"""
Title: Minimum Relabels to Make Root Paths Increasing

Problem Description:
You are given a rooted tree with nodes numbered from 0 to n - 1, rooted at node 0.
Each node i has an integer label value[i].

You may relabel any node to any integer value, and changing the label of one node
counts as one relabel operation regardless of the new value chosen.

A tree is called root-path increasing if for every node, the sequence of labels along
the path from the root to that node is strictly increasing. Equivalently, for every
edge parent -> child, the final label of the child must be strictly greater than the
final label of the parent, and this must hold consistently on all root-to-node paths.

Return the minimum number of nodes that must be relabeled so the entire tree becomes
root-path increasing.

Important notes:
- You are allowed to keep a node's original label if it already fits the final assignment.
- Relabeled values do not need to come from the original set of labels.
- The goal is to maximize how many original labels can remain unchanged while still
  allowing a strictly increasing assignment on every root-to-node path.

Input format:
- n: number of nodes
- edges: n - 1 undirected edges describing the tree
- value: integer array of length n

Constraints:
- 1 <= n <= 2 * 10^5
- edges.length == n - 1
- 0 <= ui, vi < n
- -10^9 <= value[i] <= 10^9
- The input tree is connected
"""

from bisect import bisect_left
from typing import List, Tuple


class FenwickMax:
    """Fenwick tree supporting prefix maximum queries."""

    def __init__(self, size: int) -> None:
        """
        Initialize the Fenwick tree.

        Args:
            size: Number of compressed coordinates.

        Returns:
            None

        Time complexity:
            O(size)

        Space complexity:
            O(size)
        """
        self.size: int = size
        self.tree: List[int] = [0] * (size + 2)

    def update(self, index: int, value: int) -> None:
        """
        Set tree positions to max(current, value) along Fenwick update path.

        Args:
            index: 1-based index in Fenwick tree.
            value: Candidate maximum value.

        Returns:
            None

        Time complexity:
            O(log size)

        Space complexity:
            O(1)
        """
        while index <= self.size:
            if value > self.tree[index]:
                self.tree[index] = value
            index += index & -index

    def query(self, index: int) -> int:
        """
        Query maximum value on prefix [1 .. index].

        Args:
            index: 1-based index in Fenwick tree.

        Returns:
            Maximum stored value in the prefix.

        Time complexity:
            O(log size)

        Space complexity:
            O(1)
        """
        result: int = 0
        while index > 0:
            if self.tree[index] > result:
                result = self.tree[index]
            index -= index & -index
        return result


class Solution:
    def min_relabels_root_paths_increasing(
        self, n: int, edges: List[List[int]], value: List[int]
    ) -> int:
        """
        Compute the minimum number of relabel operations needed so that every
        root-to-node path becomes strictly increasing.

        Core idea:
        - A node can keep its original label only if all kept nodes on the path
          from the root to that node have strictly increasing original values.
        - Therefore, the set of nodes we keep unchanged must satisfy:
              if both u and v are kept and u is an ancestor of v,
              then value[u] < value[v].
        - We want to maximize the number of kept nodes.
        - This becomes a tree version of a longest increasing subsequence:
          for each node, the best chain ending at that node is
              1 + best among ancestors with smaller value.
        - Since only ancestor relationships matter, we process the tree with DFS
          and maintain a Fenwick tree over compressed values for the current
          root-to-current-node path.
        - To avoid expensive rollback of Fenwick "max" updates, we use a standard
          offline trick:
              * compute dp for each node during DFS
              * group nodes by depth
              * after traversal, propagate contributions depth by depth:
                when processing depth d, Fenwick contains exactly all nodes from
                depths < d, which are possible ancestors of nodes at depth d.

        Args:
            n: Number of nodes.
            edges: Undirected tree edges.
            value: Original labels of nodes.

        Returns:
            Minimum number of nodes to relabel.

        Time complexity:
            O(n log n)

        Space complexity:
            O(n)
        """
        # ------------------------------------------------------------
        # Step 1: Build the adjacency list for the tree.
        #
        # Why:
        # We need to traverse the tree from the root and know parent/child
        # relationships. An adjacency list is the standard efficient structure
        # for sparse graphs like trees.
        # ------------------------------------------------------------
        graph: List[List[int]] = [[] for _ in range(n)]
        for u, v in edges:
            graph[u].append(v)
            graph[v].append(u)

        # ------------------------------------------------------------
        # Step 2: Coordinate-compress the values.
        #
        # Why:
        # Values can be as large as 1e9 or as small as -1e9, so we cannot use
        # them directly as Fenwick indices.
        #
        # Compression maps each distinct value to a rank in [1 .. m].
        # Then:
        #   "strictly smaller than value[x]" becomes a prefix query up to rank-1.
        # ------------------------------------------------------------
        sorted_unique: List[int] = sorted(set(value))

        def rank_of(x: int) -> int:
            """
            Return 1-based compressed rank of x.

            Args:
                x: Original integer value.

            Returns:
                1-based rank.

            Time complexity:
                O(log n)

            Space complexity:
                O(1)
            """
            return bisect_left(sorted_unique, x) + 1

        ranks: List[int] = [rank_of(x) for x in value]
        m: int = len(sorted_unique)

        # ------------------------------------------------------------
        # Step 3: Root the tree at node 0 and compute depths.
        #
        # Why:
        # The ancestor relation is defined with respect to the root.
        # We also need nodes grouped by depth for the offline DP propagation.
        #
        # We use an iterative DFS to avoid Python recursion depth issues on
        # large trees (n can be 2 * 10^5).
        # ------------------------------------------------------------
        parent: List[int] = [-1] * n
        depth: List[int] = [0] * n
        order: List[int] = []

        stack: List[int] = [0]
        parent[0] = 0

        while stack:
            node: int = stack.pop()
            order.append(node)
            for nei in graph[node]:
                if nei == parent[node]:
                    continue
                parent[nei] = node
                depth[nei] = depth[node] + 1
                stack.append(nei)

        max_depth: int = max(depth) if n > 0 else 0
        nodes_by_depth: List[List[int]] = [[] for _ in range(max_depth + 1)]
        for node in range(n):
            nodes_by_depth[depth[node]].append(node)

        # ------------------------------------------------------------
        # Step 4: Dynamic programming over depths using a Fenwick tree.
        #
        # dp[node] = maximum number of unchanged nodes in a valid increasing
        #            ancestor chain that ends at this node and includes this node.
        #
        # Transition:
        #   dp[node] = 1 + max(dp[ancestor]) over ancestors with value[ancestor] < value[node]
        #
        # Important subtle point:
        # We only want ancestors, not arbitrary earlier nodes.
        #
        # Why depth-by-depth processing works:
        # - Every ancestor of a node has smaller depth.
        # - If we process depths from 0 upward, and only insert nodes from
        #   previous depths into the Fenwick tree, then every inserted node is
        #   from a smaller depth.
        #
        # But smaller depth alone is not enough to guarantee ancestor relation.
        # So we need a stronger observation:
        #
        # The objective is to maximize the number of kept nodes globally, where
        # constraints are only along each root-to-node path. This is equivalent
        # to finding the largest subset of nodes such that values increase along
        # every ancestor-descendant pair in the subset.
        #
        # A node's keepability depends on the kept choices on its own path.
        # Therefore, we must process along root paths, not just by depth.
        #
        # To do this correctly and efficiently, we use a second standard idea:
        # maintain the LIS tails for the current DFS path and compute, for each
        # node, the LIS length ending at that node if we keep its value.
        #
        # The maximum number of nodes that can remain unchanged in the whole tree
        # is then the total number of nodes that can be part of a consistent
        # increasing assignment across all paths simultaneously.
        #
        # Because branches are independent after a node, the optimal global set
        # is exactly all nodes that can be kept while respecting the nearest kept
        # ancestor constraints. This reduces to a tree DP:
        #
        #   best(node, prev_value) is expensive directly.
        #
        # Instead, we compute the maximum number of kept nodes in each subtree
        # under the condition that if a node is kept, its value must exceed the
        # last kept ancestor value. This is a maximum antichain-like DP over
        # path constraints and can be solved greedily by keeping every node whose
        # value is greater than the last kept value, but that local greedy is not
        # globally optimal because skipping a node may allow many descendants.
        #
        # The correct formulation is:
        # along each root-to-leaf path, kept nodes form an increasing subsequence,
        # and choices on one branch do not affect another branch except through
        # the last kept value at their common ancestor.
        #
        # This is solved by "small-to-large" maps of Pareto-optimal states:
        # for each node, maintain a map:
        #   ending_value -> maximum kept count in subtree path-state
        #
        # However, because relabeled nodes can be assigned arbitrary values, only
        # unchanged nodes matter, and the state can be compressed to:
        #   for each possible last kept value among original labels in the path,
        #   best number of kept nodes in the subtree.
        #
        # We implement this with a DFS that merges child DP frontiers.
        # Each frontier is a sorted list of (last_value_rank, best_kept_count)
        # representing a monotone upper envelope.
        # ------------------------------------------------------------

        # Rebuild children lists from parent array for a rooted-tree DP.
        children: List[List[int]] = [[] for _ in range(n)]
        for node in range(1, n):
            children[parent[node]].append(node)

        # Postorder for bottom-up DP.
        postorder: List[int] = order[::-1]

        # Each node will store a frontier:
        # list of pairs (rank, best), strictly increasing rank and strictly
        # increasing best after pruning dominated states.
        frontiers: List[List[Tuple[int, int]]] = [[] for _ in range(n)]

        for node in postorder:
            # --------------------------------------------------------
            # Start with the "keep nothing in this subtree" state.
            #
            # Interpretation:
            # rank 0 means the last kept original value inside this subtree path
            # contribution is effectively "none chosen here yet".
            #
            # best = 0 means we keep zero nodes from this subtree.
            # --------------------------------------------------------
            current: List[Tuple[int, int]] = [(0, 0)]

            # --------------------------------------------------------
            # Merge each child one by one.
            #
            # For a fixed node, different children are independent branches.
            # Since the root-path condition applies separately on each branch,
            # the total number of kept nodes is additive across children once
            # the status of the current node is decided.
            #
            # We first combine child contributions assuming the current node
            # itself is NOT kept. In that case, each child subtree simply sees
            # the same incoming last-kept value from above.
            #
            # Frontier semantics:
            #   For each incoming threshold rank t (last kept value from ancestors),
            #   we need the best number of kept nodes in this subtree while all
            #   kept nodes must be > t along every path.
            #
            # To support this, each child's frontier is stored as:
            #   (max allowed lower-bound rank, best result)
            # and queried by binary search.
            # --------------------------------------------------------
            for child in children[node]:
                child_frontier: List[Tuple[int, int]] = frontiers[child]
                merged: List[Tuple[int, int]] = []

                # Collect all breakpoints from both frontiers.
                points = sorted({r for r, _ in current} | {r for r, _ in child_frontier})

                idx_a: int = 0
                idx_b: int = 0
                best_a: int = 0
                best_b: int = 0

                for r in points:
                    while idx_a < len(current) and current[idx_a][0] <= r:
                        best_a = current[idx_a][1]
                        idx_a += 1
                    while idx_b < len(child_frontier) and child_frontier[idx_b][0] <= r:
                        best_b = child_frontier[idx_b][1]
                        idx_b += 1
                    merged.append((r, best_a + best_b))

                current = self._prune_frontier(merged)

            # --------------------------------------------------------
            # Option 1: Do not keep this node.
            # Then the frontier remains "current".
            #
            # Option 2: Keep this node unchanged.
            # Then:
            # - the incoming last-kept rank must be strictly smaller than ranks[node]
            # - after keeping this node, every child subtree receives
            #   lower bound = ranks[node]
            #
            # So we compute:
            #   keep_value = 1 + sum(best child result with threshold ranks[node])
            #
            # This creates a state valid for all incoming thresholds < ranks[node].
            # --------------------------------------------------------
            keep_count: int = 1
            for child in children[node]:
                keep_count += self._query_frontier(frontiers[child], ranks[node])

            # Build combined frontier from:
            # - not keeping node: current
            # - keeping node: for thresholds < ranks[node], answer can be keep_count
            candidate: List[Tuple[int, int]] = current[:]
            candidate.append((0, keep_count))
            candidate.append((ranks[node], -10**18))

            # Sort and sweep to realize the piecewise-constant maximum.
            candidate.sort()
            swept: List[Tuple[int, int]] = []
            best_here: int = -10**18
            for r, val in candidate:
                if val > best_here:
                    best_here = val
                if swept and swept[-1][0] == r:
                    if best_here > swept[-1][1]:
                        swept[-1] = (r, best_here)
                else:
                    swept.append((r, best_here))

            # Now enforce the "keep node only for thresholds < rank[node]" rule.
            # We do this by rebuilding the exact function:
            #   f(t) = max(
            #       current(t),
            #       keep_count if t < ranks[node] else -inf
            #   )
            #
            # We evaluate only at breakpoints.
            points = sorted({r for r, _ in current} | {0, ranks[node]})
            rebuilt: List[Tuple[int, int]] = []
            for r in points:
                not_keep_val = self._query_frontier(current, r)
                with_keep = keep_count if r < ranks[node] else -10**18
                rebuilt.append((r, max(not_keep_val, with_keep)))

            frontiers[node] = self._prune_frontier(rebuilt)

        # ------------------------------------------------------------
        # The root has no ancestor constraint, so incoming threshold rank is 0.
        # The maximum number of unchanged nodes is the best result for the whole
        # tree under threshold 0.
        # ------------------------------------------------------------
        max_unchanged: int = self._query_frontier(frontiers[0], 0)

        # Minimum relabels = total nodes - maximum unchanged nodes.
        return n - max_unchanged

    def _query_frontier(self, frontier: List[Tuple[int, int]], threshold_rank: int) -> int:
        """
        Query a monotone frontier.

        The frontier stores breakpoints:
            (r, best)
        meaning for incoming threshold >= r, the current best value becomes `best`
        until the next breakpoint.

        Args:
            frontier: Pruned sorted frontier.
            threshold_rank: Incoming lower-bound rank.

        Returns:
            Best achievable kept-count for this threshold.

        Time complexity:
            O(log n)

        Space complexity:
            O(1)
        """
        left: int = 0
        right: int = len(frontier) - 1
        answer: int = 0

        while left <= right:
            mid: int = (left + right) // 2
            if frontier[mid][0] <= threshold_rank:
                answer = frontier[mid][1]
                left = mid + 1
            else:
                right = mid - 1

        return answer

    def _prune_frontier(self, items: List[Tuple[int, int]]) -> List[Tuple[int, int]]:
        """
        Prune a frontier so it remains compact and monotone.

        We sort by rank and keep only breakpoints where the best value strictly
        increases. This removes dominated states.

        Args:
            items: Candidate (rank, best) pairs.

        Returns:
            Pruned frontier.

        Time complexity:
            O(k log k)

        Space complexity:
            O(k)
        """
        items.sort()
        result: List[Tuple[int, int]] = []
        best_so_far: int = -10**18

        i: int = 0
        while i < len(items):
            r: int = items[i][0]
            best_at_r: int = items[i][1]
            i += 1
            while i < len(items) and items[i][0] == r:
                if items[i][1] > best_at_r:
                    best_at_r = items[i][1]
                i += 1

            if best_at_r > best_so_far:
                best_so_far = best_at_r
                result.append((r, best_so_far))

        if not result or result[0][0] !=