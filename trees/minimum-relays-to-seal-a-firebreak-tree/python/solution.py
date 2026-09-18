"""
Title: Minimum Relays to Seal a Firebreak Tree

Problem Description:
You are given an undirected tree with n zones numbered from 0 to n - 1. A wildfire can
start at any leaf zone and spreads one edge per minute toward the interior of the tree.
You may install emergency relay beacons on some zones. A beacon protects its own zone,
its parent, and all of its direct children. If the fire reaches a protected zone, it is
stopped immediately and cannot pass through that zone.

Your task is to compute the minimum number of beacons needed so that, regardless of which
leaf the fire starts from, the fire can never reach the designated command center root r.

The tree is rooted at r only for defining parent-child relationships used by beacon
coverage. A beacon on node u covers: u, parent(u) if it exists, and every child of u.
You may place a beacon on any node. The fire may start from any leaf, including leaves
very deep in the tree. You must guarantee that every path from any leaf to the root
contains at least one protected node.

Return the minimum number of beacons required.

Constraints:
- 1 <= n <= 2 * 10^5
- 0 <= r < n
- edges.length == n - 1
- edges[i] = [ui, vi]
- The input graph is a valid tree.
- An O(n) or O(n log n) solution is expected.
"""

from typing import List, Tuple


class Solution:
    def min_beacons(self, n: int, edges: List[List[int]], r: int) -> int:
        """
        Compute the minimum number of beacons needed so every leaf-to-root path
        contains at least one protected node.

        The key observation is:
        - A path is blocked if it contains any protected node.
        - A node becomes protected if there is a beacon on:
            1) itself,
            2) its parent,
            3) one of its children.
        - Therefore, for each subtree, we only need to know whether the first
          protected node on every leaf-to-current-node path has already appeared,
          and whether the current node is protected by a child beacon.

        We solve this with tree DP using three states for each node u:
        - dp0[u]: minimum beacons in subtree of u such that every leaf-to-u path
                  is already blocked strictly below u, and u itself is NOT protected.
                  This means the parent side still cannot rely on u being protected.
        - dp1[u]: minimum beacons in subtree of u such that every leaf-to-u path
                  is blocked and u IS protected by at least one child beacon.
        - dp2[u]: minimum beacons in subtree of u if we place a beacon at u.
                  Then u is protected, its children are protected, and all paths
                  through this subtree are blocked.

        Final answer:
        - For the root, it is enough that every leaf-to-root path is blocked.
        - So the answer is min(dp1[root], dp2[root], dp0[root] only if root is leaf).
          In general, dp0[root] is valid only when there are no leaf paths needing
          root itself to be protected; our transitions naturally handle this.

        Args:
            n: Number of nodes.
            edges: Undirected tree edges.
            r: Root node.

        Returns:
            Minimum number of beacons required.

        Time complexity:
            O(n)

        Space complexity:
            O(n)
        """
        if n == 1:
            # Single-node tree:
            # The only node is both root and leaf.
            # Fire starts at the root immediately, so the root must be protected.
            # A beacon at the root protects itself.
            return 1

        # Build adjacency list for the undirected tree.
        graph: List[List[int]] = [[] for _ in range(n)]
        for u, v in edges:
            graph[u].append(v)
            graph[v].append(u)

        # We root the tree at r using an iterative DFS to avoid recursion depth issues
        # on very deep trees (n can be up to 2 * 10^5).
        parent: List[int] = [-1] * n
        order: List[int] = []
        stack: List[int] = [r]
        parent[r] = r

        while stack:
            u = stack.pop()
            order.append(u)
            for v in graph[u]:
                if parent[v] == -1:
                    parent[v] = u
                    stack.append(v)

        # dp0, dp1, dp2 as described in the docstring.
        #
        # We use a large INF for impossible states.
        INF = 10**18
        dp0: List[int] = [INF] * n
        dp1: List[int] = [INF] * n
        dp2: List[int] = [INF] * n

        # Process nodes in reverse DFS order so children are computed before parent.
        for u in reversed(order):
            children: List[int] = [v for v in graph[u] if v != parent[u]]

            # Case 1: u is a leaf in the rooted tree.
            #
            # For a leaf:
            # - If we do NOT place a beacon at u, then the path from this leaf up to u
            #   is not blocked below u (because there is nothing below). So dp0 is
            #   impossible for a leaf: the leaf-to-u path would contain no protected node.
            # - dp1 is also impossible because a leaf has no child, so it cannot be
            #   protected by a child beacon.
            # - dp2 is possible by placing a beacon at u.
            if not children:
                dp0[u] = INF
                dp1[u] = INF
                dp2[u] = 1
                continue

            # ------------------------------------------------------------
            # Compute dp2[u]: place a beacon at u
            # ------------------------------------------------------------
            #
            # If we place a beacon at u:
            # - u is protected.
            # - every child of u is protected.
            # - therefore every leaf-to-u path is definitely blocked at u or below.
            #
            # For each child v, because v is protected by u's beacon, the subtree rooted
            # at v only needs to ensure every leaf-to-v path is blocked by the time it
            # reaches v. The child itself does not need to be protected by its own subtree.
            #
            # Thus child v may be in:
            # - dp0[v]: all paths blocked below v, but v itself not protected
            # - dp1[v]: all paths blocked and v protected by a child
            # - dp2[v]: beacon at v
            #
            # Any of these are acceptable because u's beacon already protects v.
            total_with_beacon = 1
            for v in children:
                total_with_beacon += min(dp0[v], dp1[v], dp2[v])
            dp2[u] = total_with_beacon

            # ------------------------------------------------------------
            # Compute dp1[u]: u is protected by at least one child beacon
            # ------------------------------------------------------------
            #
            # To make u protected by a child:
            # - At least one child must be in state dp2 (beacon on that child).
            #
            # For every child v:
            # - Since u itself has no beacon in this state, child v is NOT automatically
            #   protected by parent.
            # - The subtree of v must still block every leaf-to-v path.
            # - So child v can be in dp1[v] or dp2[v].
            #   * dp1[v]: v protected by one of its children
            #   * dp2[v]: beacon at v
            # - dp0[v] is NOT enough here, because then some leaf-to-v path would only
            #   be blocked below v while v itself is unprotected, and when extending to u,
            #   that is still okay path-wise; however the issue is that if the first
            #   protected node is strictly below v, the path is already blocked. So dp0[v]
            #   actually is acceptable for path blocking.
            #
            # Let's reason carefully:
            # - dp0[v] means every leaf-to-v path is already blocked below v, and v is
            #   not protected.
            # - That is perfectly fine for the parent, because the fire never reaches v.
            # Therefore for path blocking, child can be in dp0, dp1, or dp2.
            #
            # The only extra requirement for dp1[u] is: at least one child must be dp2
            # so that u becomes protected.
            base_sum = 0
            extra_to_force_one_child_beacon = INF
            possible = True

            for v in children:
                best_any = min(dp0[v], dp1[v], dp2[v])
                if best_any >= INF:
                    possible = False
                    break
                base_sum += best_any

                # If this child is chosen to carry a beacon, the extra cost is:
                # dp2[v] - best_any
                if dp2[v] < INF:
                    extra_to_force_one_child_beacon = min(
                        extra_to_force_one_child_beacon,
                        dp2[v] - best_any,
                    )

            if possible and extra_to_force_one_child_beacon < INF:
                dp1[u] = base_sum + extra_to_force_one_child_beacon
            else:
                dp1[u] = INF

            # ------------------------------------------------------------
            # Compute dp0[u]: all leaf-to-u paths blocked below u, and u unprotected
            # ------------------------------------------------------------
            #
            # In this state:
            # - u has no beacon.
            # - no child has a beacon, otherwise u would be protected.
            # - every leaf-to-u path must already be blocked strictly below u.
            #
            # Therefore each child v must be in a state where all leaf-to-v paths are
            # blocked, and v itself does NOT need a beacon at v that would protect u.
            #
            # Allowed child states:
            # - dp0[v]: okay, blocked below v, v unprotected
            # - dp1[v]: okay, blocked and v protected by its child
            # - dp2[v]: NOT allowed, because beacon at v would protect u
            #
            # So for each child we choose min(dp0[v], dp1[v]).
            total_without_protecting_u = 0
            possible = True
            for v in children:
                best_no_child_beacon = min(dp0[v], dp1[v])
                if best_no_child_beacon >= INF:
                    possible = False
                    break
                total_without_protecting_u += best_no_child_beacon

            dp0[u] = total_without_protecting_u if possible else INF

        # For the root:
        # We need every leaf-to-root path blocked.
        #
        # Root state dp0[root] means all such paths are blocked below root and root itself
        # is unprotected. That is still valid, because the fire is stopped before reaching
        # root on every path.
        #
        # Root state dp1[root] means root is protected by a child beacon.
        # Root state dp2[root] means root has a beacon.
        #
        # So all three states are valid for the final answer.
        return min(dp0[r], dp1[r], dp2[r])


if __name__ == "__main__":
    solution = Solution()

    # Example 1
    n1 = 7
    edges1 = [[0, 1], [0, 2], [1, 3], [1, 4], [2, 5], [2, 6]]
    r1 = 0
    result1 = solution.min_beacons(n1, edges1, r1)
    print(result1)  # Expected: 2

    # Example 2
    n2 = 8
    edges2 = [[3, 0], [3, 1], [3, 4], [4, 2], [4, 5], [5, 6], [5, 7]]
    r2 = 3
    result2 = solution.min_beacons(n2, edges2, r2)
    print(result2)  # Expected: 2

    # Additional small sanity checks

    # Single node
    n3 = 1
    edges3: List[List[int]] = []
    r3 = 0
    result3 = solution.min_beacons(n3, edges3, r3)
    print(result3)  # Expected: 1

    # Chain: 0-1-2 with root 0
    # One beacon at 1 protects 0,1,2, so answer is 1.
    n4 = 3
    edges4 = [[0, 1], [1, 2]]
    r4 = 0
    result4 = solution.min_beacons(n4, edges4, r4)
    print(result4)  # Expected: 1