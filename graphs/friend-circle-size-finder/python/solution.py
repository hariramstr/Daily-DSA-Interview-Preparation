```python
"""
Title: Friend Circle Size Finder
Difficulty: Easy
Topic: Graphs

Problem Description:
You are given a social network of `n` users, numbered from `0` to `n - 1`. You are also
given a list of `connections`, where `connections[i] = [a, b]` means user `a` and user `b`
are direct friends. Friendship is bidirectional and transitive — if A is friends with B and
B is friends with C, then A, B, and C all belong to the same friend circle.

A **friend circle** is a group of users who are directly or indirectly connected to each other.

Return a list of integers representing the **sizes of all friend circles**, sorted in
**descending order**.

Constraints:
- 1 <= n <= 1000
- 0 <= connections.length <= 5000
- connections[i].length == 2
- 0 <= connections[i][0], connections[i][1] < n
- connections[i][0] != connections[i][1]
- There are no duplicate connections.

Example 1:
- Input: n = 6, connections = [[0,1],[1,2],[3,4]]
- Output: [3, 2, 1]
- Explanation: Users {0,1,2} form a circle of size 3, users {3,4} form a circle of size 2,
  and user {5} is alone with size 1.

Example 2:
- Input: n = 4, connections = [[0,1],[2,3],[1,3]]
- Output: [4]
- Explanation: All four users are connected transitively, forming one circle of size 4.
"""

from typing import List


class Solution:
    """
    Solution using Union-Find (Disjoint Set Union) data structure.

    Union-Find is ideal for this problem because:
    1. It efficiently groups connected components together.
    2. Finding which "group" (friend circle) a user belongs to is O(α(n)) ≈ O(1).
    3. Merging two groups (when a friendship is found) is also nearly O(1).

    Alternative approach: BFS/DFS to find connected components — also valid,
    but Union-Find is more elegant and efficient for this type of problem.
    """

    def find_friend_circle_sizes(self, n: int, connections: List[List[int]]) -> List[int]:
        """
        Find the sizes of all friend circles in the social network.

        Uses Union-Find (Disjoint Set Union) with path compression and
        union by rank for optimal performance.

        Args:
            n (int): Total number of users (numbered 0 to n-1).
            connections (List[List[int]]): List of [a, b] pairs indicating
                                           direct friendships.

        Returns:
            List[int]: Sizes of all friend circles sorted in descending order.

        Time Complexity: O(n + m * α(n)) where m = number of connections and
                         α(n) is the inverse Ackermann function (practically O(1)).
                         Overall: O(n + m).
        Space Complexity: O(n) for the parent and rank arrays.
        """

        # -----------------------------------------------------------------------
        # STEP 1: Initialize Union-Find data structures
        # -----------------------------------------------------------------------
        # `parent[i]` stores the "representative" (root) of the group that user i
        # belongs to. Initially, each user is their own representative (their own
        # group of size 1).
        parent: List[int] = list(range(n))  # parent[i] = i initially

        # `rank[i]` is used to keep the tree shallow during union operations.
        # When merging two trees, we attach the shorter tree under the taller one.
        # This prevents the tree from becoming a long chain (which would be slow).
        rank: List[int] = [0] * n  # all ranks start at 0

        # -----------------------------------------------------------------------
        # STEP 2: Define the `find` helper with path compression
        # -----------------------------------------------------------------------
        def find(x: int) -> int:
            """
            Find the root (representative) of the group containing user x.

            Path Compression Optimization:
            While traversing up to the root, we directly connect every node
            to the root. This flattens the tree, making future `find` calls
            much faster (nearly O(1) amortized).

            Args:
                x (int): The user whose root we want to find.

            Returns:
                int: The root representative of user x's group.
            """
            # If x is not its own parent, it's not the root yet.
            # Recursively find the root and apply path compression.
            if parent[x] != x:
                # Path compression: point x directly to the root
                parent[x] = find(parent[x])
            return parent[x]

        # -----------------------------------------------------------------------
        # STEP 3: Define the `union` helper with union by rank
        # -----------------------------------------------------------------------
        def union(x: int, y: int) -> None:
            """
            Merge the groups containing users x and y.

            Union by Rank Optimization:
            We always attach the tree with lower rank under the tree with
            higher rank. This keeps the overall tree height small, which
            speeds up future `find` operations.

            Args:
                x (int): First user.
                y (int): Second user.
            """
            # Find the roots of both users' groups
            root_x = find(x)
            root_y = find(y)

            # If they already share the same root, they're in the same group.
            # No merging needed.
            if root_x == root_y:
                return

            # Merge the smaller-rank tree under the larger-rank tree.
            # This keeps the tree balanced and shallow.
            if rank[root_x] < rank[root_y]:
                # root_x's tree is shorter, attach it under root_y
                parent[root_x] = root_y
            elif rank[root_x] > rank[root_y]:
                # root_y's tree is shorter, attach it under root_x
                parent[root_y] = root_x
            else:
                # Both trees have the same rank.
                # Arbitrarily attach root_y under root_x and increase root_x's rank.
                parent[root_y] = root_x
                rank[root_x] += 1

        # -----------------------------------------------------------------------
        # STEP 4: Process all connections
        # -----------------------------------------------------------------------
        # For each friendship [a, b], we union the two users' groups.
        # After processing all connections, users in the same friend circle
        # will share the same root in the Union-Find structure.
        for a, b in connections:
            union(a, b)

        # -----------------------------------------------------------------------
        # STEP 5: Count the size of each friend circle
        # -----------------------------------------------------------------------
        # We use a dictionary to count how many users belong to each root.
        # The key is the root representative, and the value is the count of
        # users in that group.
        from collections import defaultdict
        circle_sizes: dict = defaultdict(int)

        for user in range(n):
            # Find the root of this user's group
            root = find(user)
            # Increment the count for this root's group
            circle_sizes[root] += 1

        # -----------------------------------------------------------------------
        # STEP 6: Extract sizes and sort in descending order
        # -----------------------------------------------------------------------
        # We only care about the sizes (values), not which root they belong to.
        # Sort in descending order as required by the problem.
        result: List[int] = sorted(circle_sizes.values(), reverse=True)

        return result


# =============================================================================
# Main block: Test the solution with the provided examples
# =============================================================================
if __name__ == "__main__":
    # Create an instance of our Solution class
    solution = Solution()

    # -------------------------------------------------------------------------
    # Example 1:
    # n = 6, connections = [[0,1],[1,2],[3,4]]
    # Expected Output: [3, 2, 1]
    #
    # Trace:
    # - union(0, 1): Groups {0,1}, {2}, {3}, {4}, {5}
    # - union(1, 2): Groups {0,1,2}, {3}, {4}, {5}
    # - union(3, 4): Groups {0,1,2}, {3,4}, {5}
    # - Sizes: 3, 2, 1 → sorted descending: [3, 2, 1] ✓
    # -------------------------------------------------------------------------
    n1 = 6
    connections1 = [[0, 1], [1, 2], [3, 4]]
    result1 = solution.find_friend_circle_sizes(n1, connections1)
    print(f"Example 1:")
    print(f"  Input:    n={n1}, connections={connections1}")
    print(f"  Output:   {result1}")
    print(f"  Expected: [3, 2, 1]")
    print(f"  Correct:  {result1 == [3, 2, 1]}")
    print()

    # -------------------------------------------------------------------------
    # Example 2:
    # n = 4, connections = [[0,1],[2,3],[1,3]]
    # Expected Output: [4]
    #
    # Trace:
    # - union(0, 1): Groups {0,1}, {2}, {3}
    # - union(2, 3): Groups {0,1}, {2,3}
    # - union(1, 3): find(1)=root of {0,1}, find(3)=root of {2,3}
    #                Merges into {0,1,2,3}
    # - Sizes: 4 → sorted descending: [4] ✓
    # -------------------------------------------------------------------------
    n2 = 4
    connections2 = [[0, 1], [2, 3], [1, 3]]
    result2 = solution.find_friend_circle_sizes(n2, connections2)
    print(f"Example 2:")
    print(f"  Input:    n={n2}, connections={connections2}")
    print(f"  Output:   {result2}")
    print(f"  Expected: [4]")
    print(f"  Correct:  {result2 == [4]}")
    print()

    # -------------------------------------------------------------------------
    # Additional Edge Case: No connections at all
    # n = 3, connections = []
    # Expected Output: [1, 1, 1]
    # Each user is their own friend circle of size 1.
    # -------------------------------------------------------------------------
    n3 = 3
    connections3: List[List[int]] = []
    result3 = solution.find_friend_circle_sizes(n3, connections3)
    print(f"Edge Case (no connections):")
    print(f"  Input:    n={n3}, connections={connections3}")
    print(f"  Output:   {result3}")
    print(f"  Expected: [1, 1, 1]")
    print(f"  Correct:  {result3 == [1, 1, 1]}")
    print()

    # -------------------------------------------------------------------------
    # Additional Edge Case: Single user
    # n = 1, connections = []
    # Expected Output: [1]
    # -------------------------------------------------------------------------
    n4 = 1
    connections4: List[List[int]] = []
    result4 = solution.find_friend_circle_sizes(n4, connections4)
    print(f"Edge Case (single user):")
    print(f"  Input:    n={n4}, connections={connections4}")
    print(f"  Output:   {result4}")
    print(f"  Expected: [1]")
    print(f"  Correct:  {result4 == [1]}")
    print()

    # -------------------------------------------------------------------------
    # Additional Edge Case: All users connected in a chain
    # n = 5, connections = [[0,1],[1,2],[2,3],[3,4]]
    # Expected Output: [5]
    # -------------------------------------------------------------------------
    n5 = 5
    connections5 = [[0, 1], [1, 2], [2, 3], [3, 4]]
    result5 = solution.find_friend_circle_sizes(n5, connections5)
    print(f"Edge Case (chain of all users):")
    print(f"  Input:    n={n5}, connections={connections5}")
    print(f"  Output:   {result5}")
    print(f"  Expected: [5]")
    print(f"  Correct:  {result5 == [5]}")
```