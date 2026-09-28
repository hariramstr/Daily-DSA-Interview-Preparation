"""
Title: Maximum Path Score With One Blocked Subtree Bypass

Problem Description:
You are given a rooted tree with n nodes numbered from 0 to n - 1, rooted at node 0.
Each node i has an integer score value[i], which may be positive, zero, or negative.
You are also given a list blocked containing some nodes that are considered unavailable.
A valid root-to-leaf route normally cannot pass through any blocked node.

However, the system allows exactly one special bypass operation on a route: at most once,
when your route reaches a blocked node b, you may ignore node b and jump directly to one
of its children c, continuing the route from c. This bypass skips only node b itself.
It does not remove the blockage of any other blocked nodes, and it cannot be used more
than once. If a blocked node has no children, the bypass cannot help there. The score of
a route is the sum of value[x] over all visited nodes; a skipped blocked node contributes
nothing because it is not visited.

Return the maximum possible score of any root-to-leaf route using at most one bypass.
If no valid root-to-leaf route exists, return null.

A leaf is a node with no children. The route must start at the root. If the root is
blocked, you may use the bypass immediately to jump to one of its children.

Constraints:
- 1 <= n <= 200000
- -10^9 <= value[i] <= 10^9
- edges.length == n - 1
- edges describes a valid tree
- 0 <= blocked.length <= n
- All nodes in blocked are distinct
"""

from typing import List, Optional, Set
import sys


class Solution:
    def max_path_score_with_one_bypass(
        self,
        value: List[int],
        edges: List[List[int]],
        blocked: List[int],
    ) -> Optional[int]:
        """
        Compute the maximum root-to-leaf score in a rooted tree when at most one blocked
        node may be bypassed by skipping that blocked node and jumping directly to one
        of its children.

        Args:
            value: Score of each node.
            edges: Undirected edges of the tree.
            blocked: Distinct blocked nodes.

        Returns:
            The maximum possible score as an integer, or None if no valid route exists.

        Time complexity:
            O(n)

        Space complexity:
            O(n)
        """
        n: int = len(value)

        # We will use a very negative sentinel to represent "impossible".
        # This is safer than using None in arithmetic-heavy DP transitions.
        neg_inf: int = -10**30

        # Convert blocked list to a set for O(1) membership checks.
        blocked_set: Set[int] = set(blocked)

        # Build an undirected adjacency list first.
        # Since the input is a tree, there are exactly n - 1 edges.
        graph: List[List[int]] = [[] for _ in range(n)]
        for u, v in edges:
            graph[u].append(v)
            graph[v].append(u)

        # We need the tree rooted at node 0.
        # To avoid recursion depth issues on very deep trees (n can be 200000),
        # we build parent/children arrays iteratively using a stack.
        parent: List[int] = [-1] * n
        children: List[List[int]] = [[] for _ in range(n)]
        order: List[int] = []

        stack: List[int] = [0]
        parent[0] = -2  # Special marker meaning "root already visited"

        while stack:
            node = stack.pop()
            order.append(node)

            # Explore neighbors and orient edges away from the root.
            for nei in graph[node]:
                if parent[nei] != -1:
                    continue
                parent[nei] = node
                children[node].append(nei)
                stack.append(nei)

        # DP definition:
        #
        # dp0[u]:
        #   Maximum score of a valid route that starts at node u, ends at a leaf in u's
        #   subtree, and assumes:
        #   - node u itself must be VISITED now
        #   - the bypass has NOT been used yet before reaching u
        #
        # dp1[u]:
        #   Maximum score of a valid route that starts at node u, ends at a leaf in u's
        #   subtree, and assumes:
        #   - node u itself must be VISITED now
        #   - the bypass HAS ALREADY been used before reaching u
        #
        # Important:
        # These states only describe routes where u is actually visited.
        # If u is blocked, then visiting u is illegal, so dp0[u] and dp1[u] are impossible.
        #
        # The bypass action is handled when considering a blocked child v of a visited node u:
        # from u, if bypass is still unused, we may skip v and jump directly to one of v's
        # children w. That contributes dp1[w], because after skipping v the bypass is now used.
        #
        # This design cleanly models the rule "when your route reaches a blocked node b,
        # you may ignore b and jump directly to one of its children".
        dp0: List[int] = [neg_inf] * n
        dp1: List[int] = [neg_inf] * n

        # Process nodes in reverse DFS/BFS order so children are computed before parents.
        for u in reversed(order):
            is_blocked: bool = u in blocked_set
            child_list: List[int] = children[u]

            # If u is blocked, we cannot "visit" it in either state.
            # Therefore both dp0[u] and dp1[u] remain impossible.
            if is_blocked:
                continue

            # If u is a leaf and not blocked, then a route can end here immediately.
            # Since the route must be root-to-leaf, stopping at a non-leaf is not allowed.
            if not child_list:
                dp0[u] = value[u]
                dp1[u] = value[u]
                continue

            # We now compute the best continuation from u to one of its descendants.
            #
            # best_after_u_without_bypass:
            #   Best additional score from choosing one child path when bypass is still unused.
            #
            # best_after_u_with_bypass_used:
            #   Best additional score from choosing one child path when bypass is already used.
            #
            # Since the route must go to exactly one leaf, we take a maximum over children.
            best_after_u_without_bypass: int = neg_inf
            best_after_u_with_bypass_used: int = neg_inf

            for v in child_list:
                # Case A: child v is not blocked.
                # Then we can simply visit v normally.
                #
                # - If bypass is still unused at u, we continue with dp0[v].
                # - If bypass is already used at u, we continue with dp1[v].
                if v not in blocked_set:
                    if dp0[v] > best_after_u_without_bypass:
                        best_after_u_without_bypass = dp0[v]
                    if dp1[v] > best_after_u_with_bypass_used:
                        best_after_u_with_bypass_used = dp1[v]
                else:
                    # Case B: child v is blocked.
                    #
                    # We cannot visit v directly.
                    #
                    # If bypass is already used, then this child contributes nothing valid.
                    #
                    # If bypass is still unused, we may use it here:
                    # skip blocked node v and jump directly to one of its children w.
                    #
                    # That means we need the best dp1[w] among children of v,
                    # because after skipping v the bypass becomes used.
                    #
                    # If v has no children, bypassing v cannot help.
                    for w in children[v]:
                        if dp1[w] > best_after_u_without_bypass:
                            best_after_u_without_bypass = dp1[w]

            # If there exists at least one valid continuation, add value[u].
            if best_after_u_without_bypass != neg_inf:
                dp0[u] = value[u] + best_after_u_without_bypass

            if best_after_u_with_bypass_used != neg_inf:
                dp1[u] = value[u] + best_after_u_with_bypass_used

        # Final answer handling:
        #
        # Normally, if root 0 is not blocked, the answer is dp0[0] because we start at root
        # with bypass unused.
        #
        # If root 0 is blocked, we cannot visit it. The only possible start is to use the
        # bypass immediately and jump from root to one of its children c. Root contributes
        # nothing because it is skipped.
        #
        # Then the remainder must be a valid route from c with bypass already used, i.e. dp1[c].
        if 0 not in blocked_set:
            return None if dp0[0] == neg_inf else dp0[0]

        # Root is blocked: try immediate bypass to one of its children.
        answer: int = neg_inf
        for c in children[0]:
            if dp1[c] > answer:
                answer = dp1[c]

        return None if answer == neg_inf else answer

    def solve(
        self,
        value: List[int],
        edges: List[List[int]],
        blocked: List[int],
    ) -> Optional[int]:
        """
        Wrapper method for the main algorithm.

        Args:
            value: Score of each node.
            edges: Undirected edges of the tree.
            blocked: Distinct blocked nodes.

        Returns:
            The maximum possible score as an integer, or None if no valid route exists.

        Time complexity:
            O(n)

        Space complexity:
            O(n)
        """
        return self.max_path_score_with_one_bypass(value, edges, blocked)


if __name__ == "__main__":
    sys.setrecursionlimit(1_000_000)

    solution = Solution()

    # Example 1 from the statement.
    # Careful verification:
    # value = [5, 4, -2, 7, 3, 6]
    # edges = [[0,1],[0,2],[1,3],[1,4],[2,5]]
    # blocked = [1]
    #
    # Valid without bypass:
    # 0 -> 2 -> 5 = 5 + (-2) + 6 = 9
    #
    # Using bypass at blocked node 1:
    # 0 -> (skip 1) -> 3 = 5 + 7 = 12
    # 0 -> (skip 1) -> 4 = 5 + 3 = 8
    #
    # Maximum is 12.
    value1 = [5, 4, -2, 7, 3, 6]
    edges1 = [[0, 1], [0, 2], [1, 3], [1, 4], [2, 5]]
    blocked1 = [1]
    result1 = solution.solve(value1, edges1, blocked1)
    print(result1)  # Expected: 12

    # Example 2 from the statement.
    # value = [2, -5, 10, 1, 4]
    # edges = [[0,1],[1,2],[2,3],[2,4]]
    # blocked = [1,2]
    #
    # Root 0 is not blocked, but child 1 is blocked.
    # We may bypass 1 and jump to 2, but 2 is also blocked and bypass is already used.
    # Therefore no valid root-to-leaf route exists.
    value2 = [2, -5, 10, 1, 4]
    edges2 = [[0, 1], [1, 2], [2, 3], [2, 4]]
    blocked2 = [1, 2]
    result2 = solution.solve(value2, edges2, blocked2)
    print(result2)  # Expected: None

    # Additional quick sanity check:
    # Root blocked, immediate bypass possible.
    # Tree: 0 -> 1, 0 -> 2
    # values: [100, 5, 7], blocked = [0]
    # We skip root and choose best child leaf: max(5, 7) = 7
    value3 = [100, 5, 7]
    edges3 = [[0, 1], [0, 2]]
    blocked3 = [0]
    result3 = solution.solve(value3, edges3, blocked3)
    print(result3)  # Expected: 7