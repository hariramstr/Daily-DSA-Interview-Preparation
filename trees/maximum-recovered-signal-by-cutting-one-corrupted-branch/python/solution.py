"""
Title: Maximum Recovered Signal by Cutting One Corrupted Branch

Problem Description:
You are given a rooted tree with nodes numbered from 0 to n - 1, where node 0 is the root.
Each node i has an integer signal value value[i], which may be positive, zero, or negative.
A monitoring system may disconnect exactly one subtree by cutting the edge between some node x
and its parent. If you cut at node x, then the entire subtree rooted at x is removed from the tree.
The root cannot be cut because it has no parent.

After at most one cut, the recovered signal of the system is defined as the maximum root-to-leaf
path sum in the remaining tree. If no cut is performed, the tree stays unchanged. Your task is to
return the largest possible recovered signal.

Note that after removing a subtree, some internal node may become a leaf if all of its children
were removed. The remaining structure is still a rooted tree containing node 0.

Constraints:
- 2 <= n <= 2 * 10^5
- edges.length == n - 1
- 0 <= parent of each non-root node < n
- -10^9 <= value[i] <= 10^9
- The input tree is connected and acyclic
- The answer fits in a signed 64-bit integer
"""

from typing import List


class Solution:
    def max_recovered_signal(self, value: List[int], edges: List[List[int]]) -> int:
        """
        Compute the maximum possible root-to-leaf path sum after removing at most one subtree.

        Args:
            value: Integer signal value for each node.
            edges: Undirected edges of the tree, rooted at node 0.

        Returns:
            The largest possible recovered signal after at most one cut.

        Time complexity:
            O(n)

        Space complexity:
            O(n)
        """
        n: int = len(value)

        # Build adjacency list for the undirected tree.
        # We root the tree at node 0 later using an iterative DFS.
        graph: List[List[int]] = [[] for _ in range(n)]
        for u, v in edges:
            graph[u].append(v)
            graph[v].append(u)

        # parent[node] = parent of node in rooted tree
        # order will store nodes in DFS visitation order.
        # We later process reversed(order) to do bottom-up DP.
        parent: List[int] = [-1] * n
        order: List[int] = []
        children: List[List[int]] = [[] for _ in range(n)]

        # Iterative DFS avoids recursion depth issues for n up to 2 * 10^5.
        stack: List[int] = [0]
        parent[0] = -2  # temporary marker to indicate "visited root"

        while stack:
            node = stack.pop()
            order.append(node)
            for nei in graph[node]:
                if parent[nei] != -1:
                    continue
                parent[nei] = node
                children[node].append(nei)
                stack.append(nei)

        parent[0] = -1

        # down[node] = maximum path sum from node down to any leaf inside node's subtree,
        # with no cut performed.
        #
        # For a leaf:
        #   down[leaf] = value[leaf]
        #
        # For an internal node:
        #   down[node] = value[node] + max(down[child] for child in children[node])
        #
        # This is the standard downward tree DP.
        down: List[int] = [0] * n

        # best1[node] = largest down[child] among children of node
        # best2[node] = second largest down[child] among children of node
        # best_child[node] = which child gives best1[node]
        #
        # These are useful because when we consider cutting one child subtree,
        # the parent may still continue through another child, or become a leaf
        # if no other child remains.
        neg_inf: int = -10**30
        best1: List[int] = [neg_inf] * n
        best2: List[int] = [neg_inf] * n
        best_child: List[int] = [-1] * n

        for node in reversed(order):
            if not children[node]:
                down[node] = value[node]
                continue

            local_best1 = neg_inf
            local_best2 = neg_inf
            local_child = -1

            for child in children[node]:
                candidate = down[child]
                if candidate > local_best1:
                    local_best2 = local_best1
                    local_best1 = candidate
                    local_child = child
                elif candidate > local_best2:
                    local_best2 = candidate

            best1[node] = local_best1
            best2[node] = local_best2
            best_child[node] = local_child
            down[node] = value[node] + local_best1

        # no_cut_answer is the best root-to-leaf path sum in the original tree.
        no_cut_answer: int = down[0]

        # We now compute, for every node u, a value "up_best[u]":
        #
        # up_best[u] = the best root-to-leaf path sum in the whole tree after removing
        #              the entire subtree rooted at u.
        #
        # This is exactly the answer if we choose to cut at node u.
        #
        # Root cannot be cut, so up_best[0] is not needed.
        #
        # Key reroot-style transition:
        # Suppose we cut child c of parent p.
        # Then the remaining tree's best root-to-leaf path must be one of:
        #
        # 1) A path that does not rely on p -> c at all and already exists outside p's subtree
        #    after some hypothetical removal higher up. That is represented by up_best[p].
        #
        # 2) A path that goes from root down to p and then:
        #      - continues into another child of p (not c), or
        #      - stops at p if p becomes a leaf after removing c and there are no other children.
        #
        # To evaluate option (2), we need the root-to-parent prefix sum.
        prefix_sum: List[int] = [0] * n
        for node in order:
            for child in children[node]:
                prefix_sum[child] = prefix_sum[node] + value[child]

        # root_to[node] = sum of values along path root -> node
        root_to: List[int] = [0] * n
        root_to[0] = value[0]
        for node in order:
            for child in children[node]:
                root_to[child] = root_to[node] + value[child]

        up_best: List[int] = [neg_inf] * n

        # Process top-down from root to children.
        for p in order:
            for c in children[p]:
                # Determine the best downward continuation from p if child c is removed.
                #
                # If c was the best child, we must fall back to the second best child.
                # Otherwise, the best child remains available.
                other_best_down = best2[p] if best_child[p] == c else best1[p]

                # If no other child remains, then after cutting c, node p becomes a leaf.
                # So the best path that goes through p's subtree is simply root_to[p].
                #
                # Otherwise, the best path through p's subtree is:
                #   root_to[p] + other_best_down
                # but note root_to[p] already includes value[p], and other_best_down starts
                # from a child, so this is correct.
                if other_best_down == neg_inf:
                    through_parent_after_cut = root_to[p]
                else:
                    through_parent_after_cut = root_to[p] + other_best_down

                # Also, there may be an even better path entirely outside p's subtree,
                # already represented by up_best[p] if p itself were removed from above.
                #
                # When removing c, everything outside c remains, including all of that.
                up_best[c] = through_parent_after_cut if p == 0 else max(up_best[p], through_parent_after_cut)

        # Final answer is the best among:
        # - no cut
        # - cut at any non-root node
        answer: int = no_cut_answer
        for node in range(1, n):
            if up_best[node] > answer:
                answer = up_best[node]

        return answer

    def maximumRecoveredSignal(self, value: List[int], edges: List[List[int]]) -> int:
        """
        Wrapper method using a camelCase name for convenience.

        Args:
            value: Integer signal value for each node.
            edges: Undirected edges of the tree, rooted at node 0.

        Returns:
            The largest possible recovered signal after at most one cut.

        Time complexity:
            O(n)

        Space complexity:
            O(n)
        """
        return self.max_recovered_signal(value, edges)


if __name__ == "__main__":
    solution = Solution()

    # Example 1
    value1 = [5, 4, -10, 3, 2, 7]
    edges1 = [[0, 1], [0, 2], [1, 3], [1, 4], [2, 5]]
    result1 = solution.maximumRecoveredSignal(value1, edges1)
    print(result1)  # Expected: 12

    # Example 2
    value2 = [8, -5, 6, -2, 4, -1, 3]
    edges2 = [[0, 1], [0, 2], [1, 3], [1, 4], [2, 5], [5, 6]]
    result2 = solution.maximumRecoveredSignal(value2, edges2)
    print(result2)  # Expected: 16