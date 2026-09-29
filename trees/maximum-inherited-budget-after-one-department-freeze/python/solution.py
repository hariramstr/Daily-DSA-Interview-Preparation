"""
Title: Maximum Inherited Budget After One Department Freeze

Problem Description:
A company is organized as a rooted tree with node 0 as the CEO. Each node represents
a department, and edges connect a department to its direct sub-departments. Every
department i has an integer budget value budget[i], which may be positive, zero,
or negative.

For any department u, define its inherited budget as the sum of budget values on the
simple path from the CEO to u, inclusive.

Before the next quarter begins, the company may freeze at most one department x.
Freezing x removes the entire subtree rooted at x from consideration. The CEO's
department cannot be frozen. After the freeze, only departments not in the removed
subtree remain active.

Your task is to compute the maximum inherited budget among all active departments
after applying at most one freeze operation. You may also choose not to freeze any
department.

Return that maximum possible inherited budget.

Important details:
- The tree is rooted at node 0.
- If a subtree is frozen, every node in that subtree becomes inactive.
- The inherited budget of an active node is still computed using the original
  root-to-node path values, since all ancestors outside the frozen subtree remain
  unchanged.
- It is guaranteed that at least one active node always remains because node 0
  cannot be frozen.

Constraints:
- 1 <= n <= 200000
- edges.length == n - 1
- 0 <= parent, child < n
- The input edges form a valid tree rooted at 0
- -10^9 <= budget[i] <= 10^9
- The answer fits in a signed 64-bit integer
"""

from typing import List, Tuple


class Solution:
    def max_inherited_budget_after_one_freeze(
        self, n: int, edges: List[List[int]], budget: List[int]
    ) -> int:
        """
        Compute the maximum inherited budget among active departments after freezing
        at most one non-root subtree.

        The key observation is:
        - Every node has a fixed root-to-node path sum.
        - Freezing a subtree rooted at x removes exactly the nodes inside that subtree.
        - Therefore, after freezing x, the answer becomes:
              maximum path sum among nodes NOT in subtree(x)
        - So the problem reduces to:
              For every possible subtree, what is the maximum value outside it?

        We solve this efficiently by:
        1. Building the rooted tree.
        2. Running an iterative DFS to compute:
           - Euler tour entry/exit times for each node
           - Root-to-node path sum for each node
        3. In Euler order, every subtree becomes one contiguous segment.
        4. For each node x, the nodes outside subtree(x) are exactly:
           - the prefix before subtree(x)
           - the suffix after subtree(x)
        5. Precompute prefix maximums and suffix maximums over the Euler-order path sums.
        6. For each possible frozen node x != 0, combine:
           - best prefix before subtree(x)
           - best suffix after subtree(x)
        7. Also consider "no freeze".

        Args:
            n: Number of nodes in the tree.
            edges: Directed edges [parent, child] describing the rooted tree.
            budget: Budget value for each node.

        Returns:
            The maximum possible inherited budget after applying at most one freeze.

        Time complexity:
            O(n)

        Space complexity:
            O(n)
        """
        # ------------------------------------------------------------
        # Step 1: Build adjacency list for the rooted tree.
        #
        # Because the input already represents a rooted tree with edges
        # directed from parent to child, we can directly store children.
        #
        # children[u] will contain all direct sub-departments of u.
        # ------------------------------------------------------------
        children: List[List[int]] = [[] for _ in range(n)]
        for parent, child in edges:
            children[parent].append(child)

        # ------------------------------------------------------------
        # Step 2: Iterative DFS to compute:
        # - path_sum[u]: inherited budget of node u
        # - tin[u]: Euler entry index of u
        # - tout[u]: Euler exit index of u
        # - euler_values: path sums listed in Euler entry order
        #
        # Why iterative DFS instead of recursive DFS?
        # The tree can have up to 200000 nodes, and Python recursion
        # depth would be unsafe. Iterative DFS avoids recursion limits.
        #
        # Euler tour property used here:
        # In entry-order traversal, all nodes of subtree(u) occupy one
        # contiguous segment [tin[u], tout[u]].
        # ------------------------------------------------------------
        path_sum: List[int] = [0] * n
        tin: List[int] = [0] * n
        tout: List[int] = [0] * n
        euler_values: List[int] = []

        # Stack entries:
        # (node, state)
        # state = 0 means "enter node"
        # state = 1 means "exit node"
        stack: List[Tuple[int, int]] = [(0, 0)]
        path_sum[0] = budget[0]

        while stack:
            node, state = stack.pop()

            if state == 0:
                # ----------------------------------------------------
                # Entering the node:
                # - assign Euler entry time
                # - record its path sum in Euler order
                # - schedule exit event
                # - schedule children entry events
                # ----------------------------------------------------
                tin[node] = len(euler_values)
                euler_values.append(path_sum[node])

                stack.append((node, 1))

                # Reverse iteration is used so that the left-to-right
                # child order in DFS matches the original adjacency order.
                # This is not required for correctness, but it keeps the
                # traversal deterministic and easier to reason about.
                for child in reversed(children[node]):
                    path_sum[child] = path_sum[node] + budget[child]
                    stack.append((child, 0))
            else:
                # ----------------------------------------------------
                # Exiting the node:
                # At this moment, all descendants have already been
                # processed, so the subtree segment is complete.
                # ----------------------------------------------------
                tout[node] = len(euler_values) - 1

        # ------------------------------------------------------------
        # Step 3: Precompute prefix and suffix maximums over Euler order.
        #
        # Let arr = euler_values.
        #
        # prefix_max[i] = max(arr[0], arr[1], ..., arr[i])
        # suffix_max[i] = max(arr[i], arr[i+1], ..., arr[n-1])
        #
        # Then for subtree(x) = segment [L, R]:
        # best outside subtree(x) is:
        #   max(
        #       prefix_max[L-1] if L > 0,
        #       suffix_max[R+1] if R+1 < n
        #   )
        #
        # This works because nodes outside the subtree are exactly all
        # Euler positions before L and after R.
        # ------------------------------------------------------------
        prefix_max: List[int] = [0] * n
        suffix_max: List[int] = [0] * n

        prefix_max[0] = euler_values[0]
        for i in range(1, n):
            prefix_max[i] = max(prefix_max[i - 1], euler_values[i])

        suffix_max[n - 1] = euler_values[n - 1]
        for i in range(n - 2, -1, -1):
            suffix_max[i] = max(suffix_max[i + 1], euler_values[i])

        # ------------------------------------------------------------
        # Step 4: Start with the "no freeze" answer.
        #
        # If we do not freeze anything, the answer is simply the maximum
        # inherited budget among all nodes.
        # ------------------------------------------------------------
        answer: int = prefix_max[n - 1]

        # ------------------------------------------------------------
        # Step 5: Try freezing every non-root node.
        #
        # We cannot freeze node 0 (the CEO), as stated in the problem.
        #
        # For each node x:
        # - subtree(x) corresponds to Euler segment [tin[x], tout[x]]
        # - the best remaining node must lie outside that segment
        #
        # We combine the best prefix before the segment and the best
        # suffix after the segment.
        #
        # There is always at least one remaining node because x != 0,
        # so the root remains active.
        # ------------------------------------------------------------
        for node in range(1, n):
            left_best: int = -10**30
            right_best: int = -10**30

            left_index: int = tin[node] - 1
            if left_index >= 0:
                left_best = prefix_max[left_index]

            right_index: int = tout[node] + 1
            if right_index < n:
                right_best = suffix_max[right_index]

            best_after_freeze: int = max(left_best, right_best)
            answer = max(answer, best_after_freeze)

        return answer


if __name__ == "__main__":
    solution = Solution()

    # Example 1
    n1 = 5
    edges1 = [[0, 1], [0, 2], [1, 3], [1, 4]]
    budget1 = [4, -2, 3, 5, -1]
    result1 = solution.max_inherited_budget_after_one_freeze(n1, edges1, budget1)
    print(result1)  # Expected: 7

    # Example 2
    n2 = 6
    edges2 = [[0, 1], [0, 2], [1, 3], [2, 4], [2, 5]]
    budget2 = [5, 4, -10, 8, 20, 1]
    result2 = solution.max_inherited_budget_after_one_freeze(n2, edges2, budget2)
    print(result2)  # Expected: 17