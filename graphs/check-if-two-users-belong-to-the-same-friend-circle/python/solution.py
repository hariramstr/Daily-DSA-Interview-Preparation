"""
Title: Check if Two Users Belong to the Same Friend Circle

Problem Description:
You are given an undirected social graph with n users labeled from 0 to n - 1.
Each friendship is represented by a pair [u, v], meaning user u and user v know
each other directly. Friendships are mutual, so the graph is undirected.

A friend circle is a connected group of users where every user can reach every
other user through one or more friendship links.

Your task is to determine, for a single query, whether two given users source and
target belong to the same friend circle. Return true if there is a path between
them, otherwise return false.

This problem is intended to test basic graph traversal using either Breadth-First
Search (BFS) or Depth-First Search (DFS). We will build an adjacency list from
the friendship pairs and explore from source until we either reach target or
finish visiting the connected component.

Constraints:
- 1 <= n <= 10^4
- 0 <= friendships.length <= 2 * 10^4
- friendships[i].length == 2
- 0 <= u, v < n
- u != v
- There are no duplicate friendship pairs.
- 0 <= source, target < n

Example 1:
Input: n = 5, friendships = [[0,1],[1,2],[3,4]], source = 0, target = 2
Output: true

Example 2:
Input: n = 6, friendships = [[0,1],[2,3],[3,4]], source = 1, target = 5
Output: false
"""

from collections import deque
from typing import Deque, List


class Solution:
    def valid_path(self, n: int, friendships: List[List[int]], source: int, target: int) -> bool:
        """
        Determine whether two users belong to the same connected component
        in an undirected graph using Breadth-First Search (BFS).

        Args:
            n: Total number of users labeled from 0 to n - 1.
            friendships: List of undirected edges where each pair [u, v]
                represents a mutual friendship.
            source: The user where the search starts.
            target: The user we want to determine reachability to.

        Returns:
            True if there is a path from source to target, otherwise False.

        Time Complexity:
            O(n + m), where n is the number of users and m is the number of friendships.
            We may build the adjacency list in O(m) time and traverse each user/edge
            at most once in BFS.

        Space Complexity:
            O(n + m) for the adjacency list, visited structure, and BFS queue.
        """
        # If source and target are the same user, then they are trivially in the same
        # friend circle because a node is always reachable from itself with a path of length 0.
        if source == target:
            return True

        # Build an adjacency list representation of the graph.
        #
        # Why adjacency list?
        # - The graph can have up to 10^4 nodes and 2 * 10^4 edges.
        # - An adjacency list is memory-efficient for sparse graphs.
        # - It allows us to quickly find all direct friends (neighbors) of a user.
        #
        # We create a list of empty lists, one for each user.
        # Example:
        #   n = 5
        #   adjacency = [[], [], [], [], []]
        adjacency: List[List[int]] = [[] for _ in range(n)]

        # Since the graph is undirected, each friendship [u, v] must be added in both directions:
        # - v is a neighbor of u
        # - u is a neighbor of v
        for u, v in friendships:
            adjacency[u].append(v)
            adjacency[v].append(u)

        # We need to avoid visiting the same user multiple times.
        #
        # Why use a visited list?
        # - Prevents infinite loops in graphs with cycles.
        # - Ensures each node is processed at most once.
        # - Makes the BFS efficient.
        visited: List[bool] = [False] * n
        visited[source] = True

        # BFS uses a queue because it explores the graph level by level.
        #
        # Why BFS works here:
        # - We only need to know whether a path exists, not necessarily the shortest path.
        # - BFS is simple, beginner-friendly, and guarantees we explore all reachable
        #   users from source.
        queue: Deque[int] = deque([source])

        # Continue exploring while there are still users to process.
        while queue:
            # Remove the user at the front of the queue.
            current_user: int = queue.popleft()

            # Look at every direct friend of the current user.
            for neighbor in adjacency[current_user]:
                # If we find the target user, we can stop immediately.
                # This is an optimization: no need to continue searching once the answer is known.
                if neighbor == target:
                    return True

                # If this neighbor has not been visited yet, mark it visited
                # and add it to the queue so we can explore its friends later.
                if not visited[neighbor]:
                    visited[neighbor] = True
                    queue.append(neighbor)

        # If BFS finishes and we never reached target, then target is not in the
        # same connected component as source.
        return False


if __name__ == "__main__":
    solution = Solution()

    # Example 1:
    # Graph:
    # 0 -- 1 -- 2
    # 3 -- 4
    #
    # source = 0, target = 2
    # Path exists: 0 -> 1 -> 2
    n1: int = 5
    friendships1: List[List[int]] = [[0, 1], [1, 2], [3, 4]]
    source1: int = 0
    target1: int = 2
    result1: bool = solution.valid_path(n1, friendships1, source1, target1)
    print(result1)  # Expected: True

    # Example 2:
    # Graph:
    # 0 -- 1
    # 2 -- 3 -- 4
    # 5 is isolated
    #
    # source = 1, target = 5
    # No path exists from 1 to 5
    n2: int = 6
    friendships2: List[List[int]] = [[0, 1], [2, 3], [3, 4]]
    source2: int = 1
    target2: int = 5
    result2: bool = solution.valid_path(n2, friendships2, source2, target2)
    print(result2)  # Expected: False

    # Additional beginner-friendly test:
    # source and target are the same user, so the answer should always be True.
    n3: int = 3
    friendships3: List[List[int]] = [[0, 1]]
    source3: int = 2
    target3: int = 2
    result3: bool = solution.valid_path(n3, friendships3, source3, target3)
    print(result3)  # Expected: True