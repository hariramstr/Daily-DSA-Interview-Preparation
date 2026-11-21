"""
Maximum Toll Collected by Closing One Ancestral Gate

Problem Description:
You are given a rooted tree with n checkpoints numbered from 0 to n - 1, where node 0
is the capital. Each checkpoint i has a non-negative toll value toll[i]. A tourist route
is any simple path from the capital to a leaf, and the revenue of a route is the sum of
toll values on the nodes along that path.

The city may permanently close exactly one checkpoint x, with the restriction that x
cannot be the capital. Closing x removes x and every node in the subtree rooted at x,
so every route passing through x becomes unavailable. After the closure, the city
collects revenue only from the remaining capital-to-leaf routes. Your task is to choose
the checkpoint to close so that the total remaining revenue across all surviving
capital-to-leaf routes is maximized.

More formally, let S be the sum of revenues of all root-to-leaf paths in the original
tree. If subtree(x) is removed, every root-to-leaf path that enters x disappears
entirely. Return the maximum possible total revenue of the remaining routes.

The answer can be large, so use 64-bit integers.

Key Insight:
If we know the total sum of all root-to-leaf path revenues, then for any node x
(except the root), closing x removes exactly the set of root-to-leaf paths that pass
through x. Therefore:

remaining_revenue_if_close_x = total_revenue - removed_revenue_of_subtree_x

So the problem becomes:
1. Compute total_revenue over all root-to-leaf paths.
2. For every node x != 0, compute the total revenue of all root-to-leaf paths that pass
   through x.
3. Minimize the removed revenue, or equivalently maximize the remaining revenue.

A very useful decomposition:
For a node u, every leaf in u's subtree contributes one root-to-leaf path passing
through u. Let:
- leaf_count[u] = number of leaves in subtree(u)
- prefix_sum[u] = sum of tolls from root to u

Then the total removed revenue for closing u is:
removed[u] = sum over leaves v in subtree(u) of prefix_sum[v]

This can be computed with a subtree DP:
- If u is a leaf, removed[u] = prefix_sum[u]
- Otherwise, removed[u] = sum(removed[child] for child in children[u])

This works because the leaves in subtree(u) are exactly the union of leaves in the
children subtrees.

We implement everything iteratively to safely handle n up to 2 * 10^5 without risking
Python recursion depth issues.
"""

from typing import List


class Solution:
    def maximum_remaining_revenue(self, parent: List[int], toll: List[int]) -> int:
        """
        Compute the maximum total revenue remaining after closing exactly one non-root node.

        Args:
            parent: parent[i] is the parent of node i, with parent[0] == -1.
            toll: toll[i] is the non-negative toll value at node i.

        Returns:
            The maximum possible sum of revenues of all surviving root-to-leaf paths
            after removing exactly one non-root subtree.

        Time Complexity:
            O(n), because every node and every edge is processed a constant number of times.

        Space Complexity:
            O(n), for the adjacency list and DP/helper arrays.
        """
        n: int = len(parent)

        # Build the children adjacency list for the rooted tree.
        # children[u] will contain all direct children of node u.
        children: List[List[int]] = [[] for _ in range(n)]
        for node in range(1, n):
            children[parent[node]].append(node)

        # We need a traversal order from root downward to compute prefix sums,
        # and then a reverse order to compute subtree-based DP values.
        #
        # Using an explicit stack avoids recursion depth problems on deep trees.
        order: List[int] = []
        prefix_sum: List[int] = [0] * n

        stack: List[int] = [0]
        prefix_sum[0] = toll[0]

        while stack:
            u = stack.pop()
            order.append(u)

            # Propagate root-to-current path sum to each child.
            for v in children[u]:
                prefix_sum[v] = prefix_sum[u] + toll[v]
                stack.append(v)

        # removed[u] will store the total revenue of all root-to-leaf paths
        # that pass through node u, which is exactly the total revenue removed
        # if we close node u.
        removed: List[int] = [0] * n

        # Process nodes bottom-up so children are already solved before their parent.
        for u in reversed(order):
            if not children[u]:
                # Leaf case:
                # The only root-to-leaf path in this subtree is the path ending at u,
                # whose revenue is prefix_sum[u].
                removed[u] = prefix_sum[u]
            else:
                # Internal node case:
                # All leaves in subtree(u) live inside the child subtrees, so the total
                # removed revenue is the sum of removed revenues of all children.
                total_removed_here = 0
                for v in children[u]:
                    total_removed_here += removed[v]
                removed[u] = total_removed_here

        # The total original revenue is the removed value of the root,
        # because every root-to-leaf path passes through the root.
        total_revenue: int = removed[0]

        # We must close exactly one node that is NOT the root.
        # So we try every node 1..n-1 and keep the best remaining revenue.
        best_remaining: int = 0
        for u in range(1, n):
            remaining = total_revenue - removed[u]
            if remaining > best_remaining:
                best_remaining = remaining

        return best_remaining

    def solve(self, parent: List[int], toll: List[int]) -> int:
        """
        Convenience wrapper around the main algorithm.

        Args:
            parent: Parent array describing the rooted tree.
            toll: Toll values on nodes.

        Returns:
            Maximum remaining revenue after closing one non-root node.

        Time Complexity:
            O(n)

        Space Complexity:
            O(n)
        """
        return self.maximum_remaining_revenue(parent, toll)


if __name__ == "__main__":
    solution = Solution()

    # Example 1
    parent1 = [-1, 0, 0, 1, 1, 2]
    toll1 = [5, 2, 4, 3, 1, 6]
    result1 = solution.solve(parent1, toll1)
    print(result1)  # Expected: 25

    # Example 2
    # Note:
    # The problem statement's written explanation says "Output: 31" but then concludes
    # "Therefore the maximum remaining revenue is 42."
    # The correct answer is 42, because closing node 6 removes only the path 0->2->6 = 11,
    # leaving 53 - 11 = 42.
    parent2 = [-1, 0, 0, 1, 3, 3, 2]
    toll2 = [1, 10, 2, 5, 4, 6, 8]
    result2 = solution.solve(parent2, toll2)
    print(result2)  # Expected: 42