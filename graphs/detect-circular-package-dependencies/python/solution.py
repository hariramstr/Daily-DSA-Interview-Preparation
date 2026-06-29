"""
Title: Detect Circular Package Dependencies

Problem Description:
You are given a set of software packages and a list of dependency relationships between them.
Each dependency [a, b] means package a requires package b to be installed first.

A deployment is considered invalid if there exists any cycle in the dependency graph,
because at least one package would indirectly depend on itself.

Write a function that determines whether the dependency configuration is valid.
Return true if all packages can be installed in some order, and false if there is at least
one circular dependency.

The packages are labeled from 0 to n - 1. Not every package must appear in the dependency list.
A package with no incoming or outgoing edges is still considered part of the system.

This problem models a directed graph. You need to detect whether the graph contains a cycle.
A valid installation order exists if and only if the graph is acyclic.

Constraints:
- 1 <= n <= 100000
- 0 <= dependencies.length <= 200000
- dependencies[i].length == 2
- 0 <= a, b < n
- a != b
- There may be duplicate dependency pairs

Examples:
1) n = 4, dependencies = [[1,0],[2,1],[3,2]]
   Output: true

2) n = 4, dependencies = [[1,0],[2,1],[0,2],[3,1]]
   Output: false
"""

from collections import deque
from typing import Deque, List


class Solution:
    def can_install_all_packages(self, n: int, dependencies: List[List[int]]) -> bool:
        """
        Determine whether all packages can be installed without circular dependencies.

        This method uses Kahn's Algorithm for topological sorting.
        The key idea is:
        - Build a directed graph
        - Count how many prerequisites (incoming edges) each package has
        - Repeatedly install packages that currently have no remaining prerequisites
        - If we can process all packages, the graph has no cycle
        - If some packages remain unprocessed, they are part of a cycle

        Args:
            n: Total number of packages labeled from 0 to n - 1.
            dependencies: List of [a, b] pairs meaning package a depends on package b.

        Returns:
            True if all packages can be installed in some valid order, otherwise False.

        Time complexity:
            O(n + m), where n is the number of packages and m is the number of dependencies.

        Space complexity:
            O(n + m) for the adjacency list, indegree array, and queue.
        """
        # We will represent the dependency graph using an adjacency list.
        #
        # Important direction choice:
        # The input says [a, b] means "a depends on b".
        # That means b must come before a.
        #
        # For topological sorting, it is most convenient to create an edge:
        #     b -> a
        #
        # Why?
        # Because once package b is installed, it helps unlock package a.
        # This makes indegree[a] represent "how many prerequisites package a still needs".
        #
        # Example:
        # dependencies = [[2, 1]]
        # means 2 depends on 1
        # graph edge becomes 1 -> 2
        # indegree[2] += 1
        graph: List[List[int]] = [[] for _ in range(n)]

        # indegree[i] will store how many prerequisites package i currently has.
        # If indegree[i] == 0, then package i can be installed immediately.
        indegree: List[int] = [0] * n

        # Build the graph and indegree array from the dependency list.
        for package, prerequisite in dependencies:
            graph[prerequisite].append(package)
            indegree[package] += 1

        # Create a queue containing all packages that currently have no prerequisites.
        #
        # These are the packages we can install right away.
        # We use deque because:
        # - appending to the right is O(1)
        # - popping from the left is O(1)
        queue: Deque[int] = deque()

        # Every package with indegree 0 is initially available.
        for package in range(n):
            if indegree[package] == 0:
                queue.append(package)

        # This counter tracks how many packages we successfully process/install.
        #
        # At the end:
        # - if processed_count == n, every package was installable
        # - otherwise, some packages were blocked by a cycle
        processed_count: int = 0

        # Process packages in topological order.
        while queue:
            # Take one currently installable package from the queue.
            current_package: int = queue.popleft()

            # We have now "installed" this package.
            processed_count += 1

            # Every neighbor is a package that depends on current_package.
            # Since current_package is now installed, each dependent package
            # has one fewer remaining prerequisite.
            for dependent_package in graph[current_package]:
                indegree[dependent_package] -= 1

                # If a dependent package now has no remaining prerequisites,
                # it becomes installable, so we add it to the queue.
                if indegree[dependent_package] == 0:
                    queue.append(dependent_package)

        # If we processed all packages, there was no cycle.
        #
        # Why does this work?
        # In a cycle, every package in the cycle always has at least one
        # remaining prerequisite from inside the cycle. Therefore, none of them
        # can ever reach indegree 0, so they never enter the queue.
        #
        # So:
        # - processed_count == n  -> acyclic graph -> valid installation
        # - processed_count < n   -> cycle exists   -> invalid installation
        return processed_count == n


if __name__ == "__main__":
    solution = Solution()

    # Example 1:
    # 1 depends on 0
    # 2 depends on 1
    # 3 depends on 2
    #
    # This forms a simple chain:
    # 0 -> 1 -> 2 -> 3
    #
    # Valid installation order exists, such as [0, 1, 2, 3].
    n1: int = 4
    dependencies1: List[List[int]] = [[1, 0], [2, 1], [3, 2]]
    result1: bool = solution.can_install_all_packages(n1, dependencies1)
    print("Example 1:")
    print(f"n = {n1}, dependencies = {dependencies1}")
    print(f"Can install all packages? {result1}")
    print("Expected: True")
    print()

    # Example 2:
    # 1 depends on 0
    # 2 depends on 1
    # 0 depends on 2
    # 3 depends on 1
    #
    # The first three create a cycle:
    # 0 -> 1 -> 2 -> 0   (after converting dependency meaning into graph edges)
    #
    # Because of this cycle, no valid installation order exists for all packages.
    n2: int = 4
    dependencies2: List[List[int]] = [[1, 0], [2, 1], [0, 2], [3, 1]]
    result2: bool = solution.can_install_all_packages(n2, dependencies2)
    print("Example 2:")
    print(f"n = {n2}, dependencies = {dependencies2}")
    print(f"Can install all packages? {result2}")
    print("Expected: False")
    print()

    # Additional simple test:
    # No dependencies at all means every package is independently installable.
    n3: int = 5
    dependencies3: List[List[int]] = []
    result3: bool = solution.can_install_all_packages(n3, dependencies3)
    print("Additional Test 1:")
    print(f"n = {n3}, dependencies = {dependencies3}")
    print(f"Can install all packages? {result3}")
    print("Expected: True")
    print()

    # Additional test with duplicate dependencies.
    # Duplicates are allowed by the problem statement.
    # This still has no cycle.
    n4: int = 3
    dependencies4: List[List[int]] = [[1, 0], [1, 0], [2, 1]]
    result4: bool = solution.can_install_all_packages(n4, dependencies4)
    print("Additional Test 2:")
    print(f"n = {n4}, dependencies = {dependencies4}")
    print(f"Can install all packages? {result4}")
    print("Expected: True")