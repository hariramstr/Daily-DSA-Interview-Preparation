"""
Title: Maximum Rollback Gain in a Version Tree

Problem Description:
You are given a rooted tree representing the history of a large software project.
Node 0 is the initial release, and every other node represents a version created
from exactly one parent version. Each node i has an integer value score[i], which
may be positive or negative, representing the net business impact of keeping that
version and all features introduced exactly at that node.

A rollback operation may be applied to exactly one non-root node x. Rolling back at
x removes the entire subtree rooted at x from the active history. After the rollback,
the remaining tree must still contain the root and all nodes not in x's subtree.

Your task is to compute the maximum possible "stability gain" obtainable by choosing
at most one rollback. The stability gain of rolling back node x is defined as:

(sum of scores in the remaining tree) - (minimum root-to-leaf path sum in the remaining tree)

If you choose not to roll back any node, the original tree is used.

Return the maximum stability gain over all valid choices.

A leaf is any node with no children in the remaining tree after the rollback.
Note that rolling back one subtree can turn an internal node into a leaf.

Constraints:
- 1 <= n <= 200000
- edges.length == n - 1
- 0 <= parent[i] < n for every non-root node
- score[i] is in the range [-10^9, 10^9]
- The input forms a valid rooted tree with root 0
- The answer may not fit in 32-bit signed integer
"""

from typing import List, Tuple


class Solution:
    def maximum_rollback_gain(self, parent: List[int], score: List[int]) -> int:
        """
        Compute the maximum stability gain after removing at most one non-root subtree.

        The gain for a chosen rollback node x is:
            (sum of scores outside subtree(x)) - (minimum root-to-leaf path sum
             in the remaining tree)

        We must also consider the option of performing no rollback.

        Args:
            parent: Parent array of the rooted tree, with parent[0] == -1.
            score: Integer score for each node.

        Returns:
            The maximum possible stability gain as an integer.

        Time complexity:
            O(n log n)

        Space complexity:
            O(n)
        """
        n: int = len(parent)

        # ------------------------------------------------------------
        # Step 1: Build the children adjacency list.
        #
        # We store the tree in forward direction because most of our
        # computations are naturally "from parent to children".
        # ------------------------------------------------------------
        children: List[List[int]] = [[] for _ in range(n)]
        for node in range(1, n):
            children[parent[node]].append(node)

        # ------------------------------------------------------------
        # Step 2: Iterative DFS to compute:
        #   - tin / tout: Euler tour entry/exit times
        #   - order: nodes in DFS order
        #   - prefix_sum[node]: sum of scores from root to node
        #   - subtree_sum[node]: sum of scores inside node's subtree
        #
        # We avoid recursion because n can be as large as 200000, and
        # Python recursion depth would be unsafe.
        # ------------------------------------------------------------
        tin: List[int] = [0] * n
        tout: List[int] = [0] * n
        prefix_sum: List[int] = [0] * n
        subtree_sum: List[int] = [0] * n
        order: List[int] = []

        total_sum: int = 0
        timer: int = 0

        # Stack entries:
        #   (node, state)
        # state == 0 -> entering node
        # state == 1 -> exiting node
        stack: List[Tuple[int, int]] = [(0, 0)]
        prefix_sum[0] = score[0]

        while stack:
            node, state = stack.pop()

            if state == 0:
                tin[node] = timer
                timer += 1
                order.append(node)

                # We push exit state first, then children enter states.
                # Children are pushed in reverse order so traversal order
                # is stable, though correctness does not depend on that.
                stack.append((node, 1))
                for child in reversed(children[node]):
                    prefix_sum[child] = prefix_sum[node] + score[child]
                    stack.append((child, 0))
            else:
                # Post-order accumulation of subtree sums:
                # subtree_sum[node] = score[node] + sum(subtree_sum[child])
                current_subtree_sum: int = score[node]
                for child in children[node]:
                    current_subtree_sum += subtree_sum[child]
                subtree_sum[node] = current_subtree_sum
                tout[node] = timer - 1

        total_sum = subtree_sum[0]

        # ------------------------------------------------------------
        # Step 3: Identify original leaves and compute the original
        # minimum root-to-leaf path sum.
        #
        # A node is an original leaf if it has no children in the
        # original tree.
        # ------------------------------------------------------------
        is_leaf: List[bool] = [len(children[node]) == 0 for node in range(n)]

        INF: int = 10**30
        original_min_leaf_path: int = INF
        for node in range(n):
            if is_leaf[node]:
                if prefix_sum[node] < original_min_leaf_path:
                    original_min_leaf_path = prefix_sum[node]

        # If n == 1, there is no valid rollback because root cannot be removed.
        # The answer is simply total_sum - root_path_sum, and root is a leaf.
        if n == 1:
            return total_sum - prefix_sum[0]

        # ------------------------------------------------------------
        # Step 4: Build arrays over Euler order to support:
        #   "What is the minimum prefix_sum among original leaves that
        #    are OUTSIDE subtree(x)?"
        #
        # Since every subtree corresponds to one contiguous Euler interval
        # [tin[x], tout[x]], we can answer "outside interval" minimum by
        # using prefix minima and suffix minima over Euler positions.
        #
        # leaf_value_at_pos[pos] = prefix_sum[node] if node is a leaf
        #                         = INF otherwise
        # ------------------------------------------------------------
        euler_to_node: List[int] = order
        leaf_value_at_pos: List[int] = [INF] * n
        for pos, node in enumerate(euler_to_node):
            if is_leaf[node]:
                leaf_value_at_pos[pos] = prefix_sum[node]

        prefix_min_leaf: List[int] = [INF] * n
        suffix_min_leaf: List[int] = [INF] * n

        running_min: int = INF
        for i in range(n):
            if leaf_value_at_pos[i] < running_min:
                running_min = leaf_value_at_pos[i]
            prefix_min_leaf[i] = running_min

        running_min = INF
        for i in range(n - 1, -1, -1):
            if leaf_value_at_pos[i] < running_min:
                running_min = leaf_value_at_pos[i]
            suffix_min_leaf[i] = running_min

        # ------------------------------------------------------------
        # Step 5: Compute best "new leaf" contribution for every node.
        #
        # Important observation:
        # After removing subtree(x), the only leaves that can appear newly
        # are ancestors of x. More specifically, an ancestor a becomes a
        # leaf exactly when the removed subtree was its ONLY remaining child
        # branch, meaning x lies inside the subtree of one of a's children,
        # and all original leaves under a are inside subtree(x).
        #
        # A simpler and very powerful reformulation:
        #
        # For any node a (except root constraints handled naturally),
        # if we remove one of its child subtrees c and there are no original
        # leaves left under a outside c, then a becomes a leaf.
        #
        # Let best_new_leaf[x] be the minimum prefix_sum[a] among ancestors a
        # that become leaves when subtree(x) is removed.
        #
        # We can compute this by propagating along root-to-node paths.
        #
        # For a node a and one of its children c:
        #   all original leaves under a but outside subtree(c)
        #   = total leaves under a excluding leaves under c
        #
        # If that set is empty, then removing any subtree inside c removes
        # all original leaves below a, so a becomes a leaf.
        #
        # Therefore every node x inside subtree(c) can use candidate
        # prefix_sum[a].
        #
        # We propagate a "best candidate from ancestors" down the tree.
        #
        # To know whether a child c is the unique leaf-containing child of a,
        # we compute leaf_count_subtree[node].
        # ------------------------------------------------------------
        leaf_count_subtree: List[int] = [0] * n
        for node in reversed(order):
            if is_leaf[node]:
                leaf_count_subtree[node] = 1
            else:
                count: int = 0
                for child in children[node]:
                    count += leaf_count_subtree[child]
                leaf_count_subtree[node] = count

        best_new_leaf: List[int] = [INF] * n

        # Iterative DFS for propagation.
        # inherited_best = best prefix_sum[a] from ancestors a that would
        # become a leaf if we remove the current node's subtree.
        stack2: List[Tuple[int, int]] = [(0, INF)]

        while stack2:
            node, inherited_best = stack2.pop()
            best_new_leaf[node] = inherited_best

            total_leaf_count_here: int = leaf_count_subtree[node]

            for child in children[node]:
                next_best: int = inherited_best

                # If child contains all leaves of node, then removing any
                # subtree inside child removes every original leaf below node.
                # That means node itself becomes a leaf.
                if leaf_count_subtree[child] == total_leaf_count_here:
                    if prefix_sum[node] < next_best:
                        next_best = prefix_sum[node]

                stack2.append((child, next_best))

        # ------------------------------------------------------------
        # Step 6: Evaluate the answer.
        #
        # Case A: no rollback
        #   gain = total_sum - original_min_leaf_path
        #
        # Case B: rollback subtree(x), x != 0
        #
        # Remaining total:
        #   total_sum - subtree_sum[x]
        #
        # Remaining minimum root-to-leaf path sum is the minimum of:
        #   1) original leaves outside subtree(x)
        #   2) newly created leaves among ancestors of x
        #
        # We compute (1) using Euler prefix/suffix minima.
        # We compute (2) using best_new_leaf[x].
        # ------------------------------------------------------------
        answer: int = total_sum - original_min_leaf_path

        for x in range(1, n):
            outside_min: int = INF

            left_end: int = tin[x] - 1
            if left_end >= 0:
                outside_min = prefix_min_leaf[left_end]

            right_start: int = tout[x] + 1
            if right_start < n:
                if suffix_min_leaf[right_start] < outside_min:
                    outside_min = suffix_min_leaf[right_start]

            remaining_min_leaf_path: int = outside_min
            if best_new_leaf[x] < remaining_min_leaf_path:
                remaining_min_leaf_path = best_new_leaf[x]

            # The remaining tree always contains root, so there is always
            # at least one leaf. Therefore remaining_min_leaf_path must be valid.
            gain: int = (total_sum - subtree_sum[x]) - remaining_min_leaf_path
            if gain > answer:
                answer = gain

        return answer


if __name__ == "__main__":
    solution = Solution()

    # Example 1
    parent1 = [-1, 0, 0, 1, 1, 2]
    score1 = [5, -2, 4, -3, 6, -1]
    result1 = solution.maximum_rollback_gain(parent1, score1)
    print(result1)

    # Example 2
    parent2 = [-1, 0, 0, 1, 2, 2, 4]
    score2 = [7, -5, 3, 10, -4, 6, -8]
    result2 = solution.maximum_rollback_gain(parent2, score2)
    print(result2)