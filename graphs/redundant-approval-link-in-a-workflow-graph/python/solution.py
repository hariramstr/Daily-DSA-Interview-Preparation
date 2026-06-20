"""
Title: Redundant Approval Link in a Workflow Graph

Problem Description:
A company models an approval workflow as a directed graph with nodes labeled from 1 to n.
Each directed edge [u, v] means task u must be completed immediately before task v can proceed.

The workflow was originally intended to form a valid rooted structure:
- Exactly one task is the root
- Every other task has exactly one direct prerequisite
- All tasks are reachable from the root by following directed edges

However, due to a configuration mistake, one extra directed edge was added.

Your task is to return the single edge that should be removed so the graph becomes a valid
rooted workflow again. If multiple edges could be removed, return the one that appears last
in the input order among the valid choices.

A valid rooted workflow must satisfy all of the following:
1. Exactly one node has indegree 0.
2. Every other node has indegree 1.
3. The graph contains no directed cycle.
4. All nodes belong to one connected workflow when viewed from the root through edge directions.

Input is given as an array edges of length n, where each element is a pair [u, v].
You may assume the final correct graph uses the same n nodes and exactly n - 1 edges.

Constraints:
- 2 <= n <= 100000
- edges.length == n
- 1 <= u, v <= n
- u != v
- It is guaranteed that removing exactly one edge can restore a valid rooted workflow.
"""

from typing import List


class UnionFind:
    """Disjoint Set Union structure for efficient cycle detection.

    This data structure supports:
    - Finding the representative (root) of a set
    - Merging two sets

    It is used here on the underlying parent relationships to detect whether
    adding an edge would connect two nodes already in the same component,
    which indicates a cycle in this specific rooted-tree reconstruction process.
    """

    def __init__(self, size: int) -> None:
        """Initialize DSU with each node in its own set.

        Args:
            size: Maximum node label. Nodes are assumed to be in [1, size].

        Returns:
            None

        Time complexity:
            O(size)

        Space complexity:
            O(size)
        """
        self.parent: List[int] = list(range(size + 1))
        self.rank: List[int] = [0] * (size + 1)

    def find(self, x: int) -> int:
        """Find the representative of the set containing x.

        Path compression is used so future queries become faster.

        Args:
            x: Node whose set representative is requested.

        Returns:
            The root representative of x's set.

        Time complexity:
            Amortized O(alpha(n))

        Space complexity:
            O(1) auxiliary, ignoring recursion stack because this implementation is iterative
        """
        while self.parent[x] != x:
            self.parent[x] = self.parent[self.parent[x]]
            x = self.parent[x]
        return x

    def union(self, a: int, b: int) -> bool:
        """Merge the sets containing a and b.

        If a and b are already in the same set, the merge fails and returns False.
        In this problem, that means adding the corresponding edge would create a cycle.

        Args:
            a: First node.
            b: Second node.

        Returns:
            True if a merge happened successfully, False if they were already connected.

        Time complexity:
            Amortized O(alpha(n))

        Space complexity:
            O(1)
        """
        root_a: int = self.find(a)
        root_b: int = self.find(b)

        if root_a == root_b:
            return False

        if self.rank[root_a] < self.rank[root_b]:
            self.parent[root_a] = root_b
        elif self.rank[root_a] > self.rank[root_b]:
            self.parent[root_b] = root_a
        else:
            self.parent[root_b] = root_a
            self.rank[root_a] += 1

        return True


class Solution:
    def findRedundantDirectedConnection(self, edges: List[List[int]]) -> List[int]:
        """Return the single directed edge that should be removed.

        Core idea:
        In a rooted directed tree with one extra edge, only two structural problems are possible:
        1. A node has two parents.
        2. A directed cycle exists.
        3. Both can happen at the same time.

        We handle these cases using a classic two-step strategy:
        - First, scan for a node with indegree 2 (two parents).
        - Then, use Union-Find to detect cycles while optionally skipping one candidate edge.

        Args:
            edges: List of directed edges [u, v].

        Returns:
            The redundant edge that should be removed.

        Time complexity:
            O(n * alpha(n)), effectively linear for practical purposes

        Space complexity:
            O(n)
        """
        n: int = len(edges)

        # parent_of[v] stores the parent currently assigned to node v while scanning edges.
        # In a valid rooted tree, every node except the root must have exactly one parent.
        # So if we ever see a second parent for the same child, we have found a conflict.
        parent_of: List[int] = [0] * (n + 1)

        # These two variables store the two competing edges when a node has two parents.
        #
        # candidate1 = the earlier edge that first assigned a parent to the child
        # candidate2 = the later edge that tries to assign a second parent to the same child
        #
        # Example:
        # edges = [[1,3], [2,3]]
        # candidate1 = [1,3]
        # candidate2 = [2,3]
        candidate1: List[int] = []
        candidate2: List[int] = []

        # ---------------------------------------------------------------------
        # STEP 1: Detect whether any node has two parents.
        #
        # Why do we do this first?
        # Because if a node has indegree 2, then one of those two incoming edges
        # must be removed. The only question is WHICH one.
        #
        # We do not decide immediately. Instead, we remember both candidates and
        # let cycle detection tell us which one is actually invalid.
        # ---------------------------------------------------------------------
        for u, v in edges:
            if parent_of[v] == 0:
                # First time we assign a parent to v.
                parent_of[v] = u
            else:
                # v already had a parent, so now v has two parents.
                # Save both conflicting edges.
                candidate1 = [parent_of[v], v]
                candidate2 = [u, v]

                # Important detail:
                # We do NOT overwrite parent_of[v] here because candidate1 is the
                # original parent assignment. We simply record the conflict.
                #
                # Later, during Union-Find, we will simulate "removing" candidate2
                # by skipping it. If that resolves everything, candidate2 is the answer.
                # Otherwise, candidate1 is the answer.
                break

        # ---------------------------------------------------------------------
        # STEP 2: Run Union-Find to detect cycles.
        #
        # There are two major scenarios:
        #
        # A) No node has two parents:
        #    Then the graph's only issue must be a cycle.
        #    The edge that closes the cycle is the answer.
        #
        # B) A node has two parents:
        #    We temporarily skip candidate2 (the later conflicting edge).
        #    - If the remaining edges form a valid tree (no cycle), then candidate2
        #      is the extra edge and should be removed.
        #    - If a cycle still exists even after skipping candidate2, then candidate1
        #      must be removed instead.
        #
        # This logic is the standard correct solution for LeetCode 685 / redundant
        # directed connection and works for all valid inputs under the problem guarantee.
        # ---------------------------------------------------------------------
        uf: UnionFind = UnionFind(n)

        for u, v in edges:
            # If we found a two-parent conflict, skip candidate2 during this pass.
            # This simulates removing candidate2 from the graph.
            if candidate2 and u == candidate2[0] and v == candidate2[1]:
                continue

            # Try to connect u and v in Union-Find.
            # If union returns False, u and v were already connected, so this edge
            # creates a cycle in the current simulated graph.
            if not uf.union(u, v):
                # Case 1: No two-parent conflict was found.
                # Then this edge is simply the redundant edge that closes the cycle.
                if not candidate1:
                    return [u, v]

                # Case 2: There WAS a two-parent conflict, and even after skipping
                # candidate2, a cycle still exists.
                #
                # That means candidate2 was not the true problem. The earlier edge
                # candidate1 must be part of the invalid structure and should be removed.
                return candidate1

        # If we finish the Union-Find pass without finding a cycle, then:
        # - either there was a two-parent conflict and skipping candidate2 fixed it,
        # - or the input guarantee ensures one removable edge exists.
        #
        # Therefore candidate2 is the correct answer in this branch.
        return candidate2


if __name__ == "__main__":
    solution = Solution()

    sample_inputs: List[List[List[int]]] = [
        [[1, 2], [1, 3], [2, 3]],
        [[1, 2], [2, 3], [3, 4], [4, 1], [1, 5]],
    ]

    for index, edges in enumerate(sample_inputs, start=1):
        result = solution.findRedundantDirectedConnection(edges)
        print(f"Example {index}: edges = {edges}")
        print(f"Redundant edge to remove: {result}")
        print()