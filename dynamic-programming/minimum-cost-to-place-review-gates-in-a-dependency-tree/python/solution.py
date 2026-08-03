"""
Title: Minimum Cost to Place Review Gates in a Dependency Tree

Problem Description:
You are given a software dependency tree with n modules numbered from 0 to n - 1,
rooted at module 0. Each module has a non-negative installation risk cost risk[i].
A review gate can be placed on any module i, paying risk[i]. A gate on module i
protects module i itself, its parent, and all of its direct children.

Your task is to place review gates so that every module in the tree is protected
by at least one gate, while minimizing the total cost.

Formally, you are given an array risk of length n and an edge list edges of length
n - 1 describing an undirected tree rooted at 0. If a gate is placed at node i,
then node i, every neighbor of i at distance 1 upward or downward along the rooted
tree, are considered protected. Every node must end up protected.

Return the minimum possible total cost.

Constraints:
- 1 <= n <= 100000
- 0 <= risk[i] <= 1000000000
- edges.length == n - 1
- edges represents a valid tree
- The answer fits in a 64-bit signed integer
"""

from typing import List, Tuple
import sys


class Solution:
    def min_review_gate_cost(self, risk: List[int], edges: List[List[int]]) -> int:
        """
        Compute the minimum total cost needed so every node in the rooted tree is protected.

        We use tree dynamic programming with three states for each node u:

        1. dp_has_gate[u]:
           Minimum cost for the subtree of u when we place a gate at u.
           Since u has a gate, u is protected, its parent is protected by u,
           and all direct children are protected by u.

        2. dp_covered_by_parent[u]:
           Minimum cost for the subtree of u when u does NOT have a gate,
           but u is already protected by its parent's gate.
           In this state, u itself needs no further protection from children,
           but every child subtree still must be handled.

        3. dp_covered_by_child[u]:
           Minimum cost for the subtree of u when u does NOT have a gate,
           and u is NOT protected by parent, so u must be protected by at least
           one of its children having a gate.

        The root has no parent, so the final answer is:
            min(dp_has_gate[root], dp_covered_by_child[root])

        Args:
            risk: Cost of placing a gate at each node.
            edges: Undirected edges of the tree.

        Returns:
            Minimum total cost to protect all nodes.

        Time complexity:
            O(n)

        Space complexity:
            O(n)
        """
        n: int = len(risk)

        # Special case:
        # If there is only one node, then the only way to protect it is to place
        # a gate on that node itself.
        if n == 1:
            return risk[0]

        # Build adjacency list for the undirected tree.
        # We use a list of lists because it is memory-efficient and fast for trees.
        graph: List[List[int]] = [[] for _ in range(n)]
        for u, v in edges:
            graph[u].append(v)
            graph[v].append(u)

        # We need a parent array and a traversal order so that we can process nodes
        # in postorder (children before parent). Recursive DFS could hit recursion
        # limits for n = 100000, so we use an iterative DFS.
        parent: List[int] = [-1] * n
        order: List[int] = []
        stack: List[int] = [0]
        parent[0] = -2  # Mark root as visited with a special parent marker.

        while stack:
            node: int = stack.pop()
            order.append(node)
            for nei in graph[node]:
                if parent[nei] == -1:
                    parent[nei] = node
                    stack.append(nei)

        # Restore the root parent to a normal sentinel value.
        parent[0] = -1

        # Large "infinity" value for impossible states.
        # The answer fits in signed 64-bit, but Python ints are unbounded anyway.
        inf: int = 10**30

        # DP arrays:
        # has_gate[u]         -> place gate at u
        # covered_by_parent[u] -> u has no gate, but parent protects u
        # covered_by_child[u]  -> u has no gate, parent does not protect u,
        #                         so at least one child must have a gate
        has_gate: List[int] = [0] * n
        covered_by_parent: List[int] = [0] * n
        covered_by_child: List[int] = [0] * n

        # Process nodes in reverse DFS order so children are computed before parent.
        for u in reversed(order):
            children: List[int] = []
            for v in graph[u]:
                if v != parent[u]:
                    children.append(v)

            # ------------------------------------------------------------
            # State 1: has_gate[u]
            # ------------------------------------------------------------
            # If we place a gate at u:
            # - u is protected by itself
            # - every child is protected by u (their parent)
            #
            # Therefore, each child v can be in either:
            # - has_gate[v]          : child also has a gate
            # - covered_by_parent[v] : child relies on u's gate for protection
            #
            # We choose the cheaper option independently for each child.
            total_has_gate: int = risk[u]
            for v in children:
                total_has_gate += min(has_gate[v], covered_by_parent[v])
            has_gate[u] = total_has_gate

            # ------------------------------------------------------------
            # State 2: covered_by_parent[u]
            # ------------------------------------------------------------
            # Here:
            # - u does NOT have a gate
            # - u IS protected by its parent
            #
            # Since u has no gate, it does not protect its children.
            # So each child v must be protected either:
            # - by having its own gate: has_gate[v]
            # - by one of its own children: covered_by_child[v]
            #
            # Child v cannot use covered_by_parent[v] here, because u has no gate.
            total_covered_by_parent: int = 0
            for v in children:
                total_covered_by_parent += min(has_gate[v], covered_by_child[v])
            covered_by_parent[u] = total_covered_by_parent

            # ------------------------------------------------------------
            # State 3: covered_by_child[u]
            # ------------------------------------------------------------
            # Here:
            # - u does NOT have a gate
            # - parent does NOT protect u
            # - so u must be protected by at least one child having a gate
            #
            # For each child v, the allowed states are still:
            # - has_gate[v]
            # - covered_by_child[v]
            #
            # But across all children, at least one child must specifically be in
            # has_gate[v], otherwise u would remain unprotected.
            #
            # Standard trick:
            # 1. First take the cheapest allowed state for every child.
            # 2. If at least one chosen cheapest state is has_gate[v], we are done.
            # 3. Otherwise, force one child to switch from covered_by_child[v] to
            #    has_gate[v], paying the minimum extra cost.
            if not children:
                # Leaf case:
                # A leaf cannot be protected by a child because it has no children.
                # So this state is impossible.
                covered_by_child[u] = inf
            else:
                base_cost: int = 0
                has_selected_gate_child: bool = False
                min_extra_to_force_gate: int = inf

                for v in children:
                    best_without_constraint: int = min(has_gate[v], covered_by_child[v])
                    base_cost += best_without_constraint

                    # If has_gate[v] is already no worse than covered_by_child[v],
                    # then one optimal choice can include a gate at child v.
                    if has_gate[v] <= covered_by_child[v]:
                        has_selected_gate_child = True

                    # If we need to force child v to have a gate, this is the extra
                    # amount we must pay compared with the unconstrained best choice.
                    extra: int = has_gate[v] - best_without_constraint
                    if extra < min_extra_to_force_gate:
                        min_extra_to_force_gate = extra

                if has_selected_gate_child:
                    covered_by_child[u] = base_cost
                else:
                    covered_by_child[u] = base_cost + min_extra_to_force_gate

        # Root handling:
        # The root has no parent, so it cannot be in covered_by_parent[root].
        # Therefore the valid final states are:
        # - has_gate[root]
        # - covered_by_child[root]
        #
        # We take the minimum of these two.
        return min(has_gate[0], covered_by_child[0])

    def minimumCost(self, risk: List[int], edges: List[List[int]]) -> int:
        """
        Wrapper method using a shorter interview-style name.

        Args:
            risk: Cost of placing a gate at each node.
            edges: Undirected edges of the tree.

        Returns:
            Minimum total cost to protect all nodes.

        Time complexity:
            O(n)

        Space complexity:
            O(n)
        """
        return self.min_review_gate_cost(risk, edges)


if __name__ == "__main__":
    """
    Run sample test cases from the problem statement.
    """
    sys.setrecursionlimit(1_000_000)

    solver = Solution()

    # Example 1
    risk1: List[int] = [5, 2, 4, 6]
    edges1: List[List[int]] = [[0, 1], [1, 2], [1, 3]]
    result1: int = solver.minimumCost(risk1, edges1)
    print(result1)  # Expected: 2

    # Example 2
    risk2: List[int] = [7, 3, 8, 2, 5, 1]
    edges2: List[List[int]] = [[0, 1], [0, 2], [1, 3], [1, 4], [2, 5]]
    result2: int = solver.minimumCost(risk2, edges2)
    print(result2)  # Expected: 4