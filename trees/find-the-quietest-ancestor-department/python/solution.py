"""
Title: Find the Quietest Ancestor Department

Problem Description:
A company models its organization as a rooted tree of departments. Department 0 is the
root headquarters. Each department has a non-negative noise score representing how
disruptive its daily operations are. For every department, you must find the ancestor
department on the path from the root to that department (including the department itself)
that has the smallest noise score. If multiple departments on that path have the same
minimum noise score, return the one that is closest to the root.

You are given an integer n, an array parent of length n where parent[0] = -1 and
parent[i] is the parent of department i, and an array noise of length n where noise[i]
is the noise score of department i.

Return an array answer of length n where answer[i] is the index of the quietest ancestor
department for department i.

The input always forms a valid rooted tree.

Constraints:
- 1 <= n <= 100000
- parent.length == n
- noise.length == n
- parent[0] == -1
- 0 <= parent[i] < n for i > 0
- 0 <= noise[i] <= 1000000000
- The parent array represents a valid tree with root 0
"""

from typing import List


class Solution:
    def quietest_ancestor_department(self, n: int, parent: List[int], noise: List[int]) -> List[int]:
        """
        Find the quietest ancestor for every department in a rooted tree.

        For each node, we only need to know the quietest department on the path from
        the root to its parent. Then we compare that department with the current node.
        This allows us to solve the problem in linear time using a tree traversal.

        Args:
            n: Number of departments (nodes) in the tree.
            parent: parent[i] is the parent of node i, with parent[0] == -1.
            noise: noise[i] is the noise score of node i.

        Returns:
            A list answer where answer[i] is the index of the quietest ancestor
            department on the path from the root to i, inclusive.

        Time complexity:
            O(n), because each node and each tree edge is processed once.

        Space complexity:
            O(n), for the children adjacency list, answer array, and traversal stack.
        """
        # Build the tree as an adjacency list of children.
        #
        # Why do we do this?
        # The input gives us a parent array, which is enough to know each node's parent,
        # but for a top-down traversal from the root we need to quickly find all children
        # of a node. An adjacency list is the standard and efficient structure for this.
        children: List[List[int]] = [[] for _ in range(n)]
        for node in range(1, n):
            children[parent[node]].append(node)

        # This array will store the final answer for every node.
        # answer[i] = index of the quietest ancestor on the root-to-i path.
        answer: List[int] = [0] * n

        # The root has only itself on its path, so its quietest ancestor is itself.
        answer[0] = 0

        # We use an explicit stack for iterative DFS instead of recursive DFS.
        #
        # Why iterative?
        # The tree can have up to 100000 nodes. A recursive DFS may exceed Python's
        # recursion limit on a deep tree (for example, a chain). Using a stack avoids
        # recursion depth issues and is safer for large inputs.
        stack: List[int] = [0]

        # Standard depth-first traversal.
        while stack:
            current: int = stack.pop()

            # Process every child of the current node.
            for child in children[current]:
                # The best candidate from the parent's path is already known:
                # answer[current] is the quietest node on the path root -> current.
                best_from_parent_path: int = answer[current]

                # Now we must decide whether the child itself is quieter than that best
                # ancestor, or whether the previous best remains the answer.
                #
                # Important tie-breaking rule:
                # If noise[child] == noise[best_from_parent_path], we must keep the one
                # closer to the root. Since best_from_parent_path is an ancestor and the
                # child is deeper, the ancestor must win the tie.
                #
                # Therefore:
                # - If noise[child] < noise[best_from_parent_path], child becomes best.
                # - Otherwise (greater OR equal), keep the ancestor.
                if noise[child] < noise[best_from_parent_path]:
                    answer[child] = child
                else:
                    answer[child] = best_from_parent_path

                # Add the child to the stack so we can continue propagating the correct
                # quietest ancestor information down its subtree.
                stack.append(child)

        return answer

    def solve(self, n: int, parent: List[int], noise: List[int]) -> List[int]:
        """
        Wrapper method that calls the main algorithm.

        Args:
            n: Number of departments.
            parent: Parent array describing the rooted tree.
            noise: Noise score for each department.

        Returns:
            The quietest ancestor index for each department.

        Time complexity:
            O(n)

        Space complexity:
            O(n)
        """
        return self.quietest_ancestor_department(n, parent, noise)


if __name__ == "__main__":
    solution = Solution()

    # Example 1
    n1: int = 7
    parent1: List[int] = [-1, 0, 0, 1, 1, 2, 2]
    noise1: List[int] = [5, 3, 8, 4, 2, 8, 1]
    result1: List[int] = solution.solve(n1, parent1, noise1)
    print("Example 1 Output:", result1)
    print("Expected:         [0, 1, 0, 1, 4, 0, 6]")

    # Example 2
    n2: int = 5
    parent2: List[int] = [-1, 0, 1, 2, 3]
    noise2: List[int] = [4, 4, 6, 2, 2]
    result2: List[int] = solution.solve(n2, parent2, noise2)
    print("Example 2 Output:", result2)
    print("Expected:         [0, 0, 0, 3, 3]")