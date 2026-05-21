"""
Campus Network Connectivity Check
==================================

Problem Description:
A university campus has `n` buildings numbered from `0` to `n - 1`. The IT department
has laid down direct network cables between certain pairs of buildings. You are given
an integer `n` and a list of connections `cables`, where `cables[i] = [a, b]` means
there is a direct network cable between building `a` and building `b`. Network
connectivity is bidirectional — if building `a` can reach building `b`, then building
`b` can also reach building `a`.

Your task is to determine how many isolated network clusters exist on campus. A cluster
is a group of one or more buildings that are all reachable from each other (directly or
indirectly). Buildings with no cables connected to them form their own cluster of size 1.

Return the number of connected clusters in the campus network.

Constraints:
- 1 <= n <= 1000
- 0 <= cables.length <= 5000
- cables[i].length == 2
- 0 <= cables[i][0], cables[i][1] < n
- cables[i][0] != cables[i][1]
- There are no duplicate cables.
"""

from typing import List


class Solution:
    def countClusters(self, n: int, cables: List[List[int]]) -> int:
        """
        Count the number of connected clusters (connected components) in the campus network.

        This solution uses the Union-Find (Disjoint Set Union) data structure, which is
        perfect for grouping elements into clusters and counting distinct groups efficiently.

        Args:
            n (int): Number of buildings on campus (labeled 0 to n-1).
            cables (List[List[int]]): List of direct cable connections between buildings.

        Returns:
            int: The total number of isolated network clusters.

        Time Complexity:  O(n + m * alpha(n)) where m = number of cables and
                          alpha is the inverse Ackermann function (nearly constant).
                          Effectively O(n + m) for practical purposes.
        Space Complexity: O(n) for the parent and rank arrays used in Union-Find.
        """

        # -----------------------------------------------------------------------
        # STEP 1: Initialize Union-Find (Disjoint Set Union) data structures
        # -----------------------------------------------------------------------
        # 'parent[i]' stores the representative (root) of the cluster that building i
        # belongs to. Initially, every building is its own cluster, so each building
        # is its own parent (parent[i] = i).
        parent: List[int] = list(range(n))

        # 'rank[i]' is used for "union by rank" optimization. It tracks the approximate
        # depth of the tree rooted at i. We always attach the shallower tree under the
        # deeper one to keep trees flat and operations fast.
        rank: List[int] = [0] * n

        # -----------------------------------------------------------------------
        # STEP 2: Define the 'find' helper with path compression
        # -----------------------------------------------------------------------
        # 'find(x)' returns the root (representative) of the cluster containing x.
        # Path compression: after finding the root, we point every node along the
        # path directly to the root. This flattens the tree for future lookups.
        def find(x: int) -> int:
            """
            Find the root representative of the cluster containing building x.
            Uses path compression for efficiency.

            Args:
                x (int): Building index to find the root for.

            Returns:
                int: The root representative of x's cluster.
            """
            # If x is not its own parent, it's not the root yet.
            # Recursively find the root and compress the path along the way.
            if parent[x] != x:
                # Path compression: set parent[x] directly to the root
                parent[x] = find(parent[x])
            # Return the root of this cluster
            return parent[x]

        # -----------------------------------------------------------------------
        # STEP 3: Define the 'union' helper with union by rank
        # -----------------------------------------------------------------------
        # 'union(a, b)' merges the clusters containing buildings a and b.
        # Union by rank: attach the root of the smaller-rank tree under the
        # root of the larger-rank tree to keep the overall tree height minimal.
        def union(a: int, b: int) -> None:
            """
            Merge the clusters containing buildings a and b.
            Uses union by rank to keep the tree balanced.

            Args:
                a (int): First building index.
                b (int): Second building index.
            """
            # Find the roots of both buildings' clusters
            root_a: int = find(a)
            root_b: int = find(b)

            # If they already share the same root, they're in the same cluster.
            # No merge needed — skip to avoid creating cycles.
            if root_a == root_b:
                return

            # Union by rank: attach the smaller tree under the larger tree.
            # This keeps the tree as flat as possible for efficient future finds.
            if rank[root_a] < rank[root_b]:
                # root_a's tree is shallower, attach it under root_b
                parent[root_a] = root_b
            elif rank[root_a] > rank[root_b]:
                # root_b's tree is shallower, attach it under root_a
                parent[root_b] = root_a
            else:
                # Both trees have equal rank; arbitrarily attach root_b under root_a
                # and increment root_a's rank since the tree just got one level deeper
                parent[root_b] = root_a
                rank[root_a] += 1

        # -----------------------------------------------------------------------
        # STEP 4: Process all cable connections
        # -----------------------------------------------------------------------
        # For each cable [a, b], we union the two buildings into the same cluster.
        # After processing all cables, buildings connected directly or indirectly
        # will share the same root in the Union-Find structure.
        for cable in cables:
            a: int = cable[0]
            b: int = cable[1]
            # Merge the clusters of building a and building b
            union(a, b)

        # -----------------------------------------------------------------------
        # STEP 5: Count the number of distinct clusters
        # -----------------------------------------------------------------------
        # A building is a "cluster root" if it is its own parent (parent[i] == i).
        # Each unique root represents one distinct connected cluster.
        # We count how many buildings are their own root after all unions are done.
        cluster_count: int = 0
        for i in range(n):
            # find(i) returns the root of building i's cluster.
            # If find(i) == i, then building i is the representative of its cluster.
            # We count each representative exactly once.
            if find(i) == i:
                cluster_count += 1

        # -----------------------------------------------------------------------
        # STEP 6: Return the total number of clusters
        # -----------------------------------------------------------------------
        return cluster_count


# =============================================================================
# MAIN: Demonstrate and verify the solution with the provided examples
# =============================================================================
if __name__ == "__main__":
    # Create an instance of the Solution class
    sol = Solution()

    # ---------------------------------------------------------
    # Example 1:
    # n = 6, cables = [[0,1],[1,2],[3,4]]
    # Expected Output: 3
    # Explanation:
    #   - Cable [0,1] → buildings 0 and 1 are in the same cluster
    #   - Cable [1,2] → building 2 joins the cluster {0,1} → cluster {0,1,2}
    #   - Cable [3,4] → buildings 3 and 4 form cluster {3,4}
    #   - Building 5 has no cables → isolated cluster {5}
    #   - Total clusters: {0,1,2}, {3,4}, {5} → 3 clusters
    # ---------------------------------------------------------
    n1 = 6
    cables1 = [[0, 1], [1, 2], [3, 4]]
    result1 = sol.countClusters(n1, cables1)
    print(f"Example 1:")
    print(f"  Input:    n={n1}, cables={cables1}")
    print(f"  Output:   {result1}")
    print(f"  Expected: 3")
    print(f"  Correct:  {result1 == 3}")
    print()

    # ---------------------------------------------------------
    # Example 2:
    # n = 4, cables = [[0,1],[1,2],[2,3]]
    # Expected Output: 1
    # Explanation:
    #   - Cable [0,1] → {0,1}
    #   - Cable [1,2] → {0,1,2}
    #   - Cable [2,3] → {0,1,2,3}
    #   - All buildings are in one cluster → 1 cluster
    # ---------------------------------------------------------
    n2 = 4
    cables2 = [[0, 1], [1, 2], [2, 3]]
    result2 = sol.countClusters(n2, cables2)
    print(f"Example 2:")
    print(f"  Input:    n={n2}, cables={cables2}")
    print(f"  Output:   {result2}")
    print(f"  Expected: 1")
    print(f"  Correct:  {result2 == 1}")
    print()

    # ---------------------------------------------------------
    # Additional Edge Case: No cables at all
    # n = 3, cables = []
    # Expected Output: 3 (each building is its own isolated cluster)
    # ---------------------------------------------------------
    n3 = 3
    cables3: List[List[int]] = []
    result3 = sol.countClusters(n3, cables3)
    print(f"Edge Case (no cables):")
    print(f"  Input:    n={n3}, cables={cables3}")
    print(f"  Output:   {result3}")
    print(f"  Expected: 3")
    print(f"  Correct:  {result3 == 3}")
    print()

    # ---------------------------------------------------------
    # Additional Edge Case: Single building
    # n = 1, cables = []
    # Expected Output: 1
    # ---------------------------------------------------------
    n4 = 1
    cables4: List[List[int]] = []
    result4 = sol.countClusters(n4, cables4)
    print(f"Edge Case (single building):")
    print(f"  Input:    n={n4}, cables={cables4}")
    print(f"  Output:   {result4}")
    print(f"  Expected: 1")
    print(f"  Correct:  {result4 == 1}")
    print()

    # ---------------------------------------------------------
    # Additional Edge Case: All buildings fully connected
    # n = 5, cables = [[0,1],[0,2],[0,3],[0,4]]
    # Expected Output: 1
    # ---------------------------------------------------------
    n5 = 5
    cables5 = [[0, 1], [0, 2], [0, 3], [0, 4]]
    result5 = sol.countClusters(n5, cables5)
    print(f"Edge Case (star topology):")
    print(f"  Input:    n={n5}, cables={cables5}")
    print(f"  Output:   {result5}")
    print(f"  Expected: 1")
    print(f"  Correct:  {result5 == 1}")