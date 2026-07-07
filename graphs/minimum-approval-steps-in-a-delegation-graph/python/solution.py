"""
Title: Minimum Approval Steps in a Delegation Graph

Problem Description:
In a company workflow system, each employee may delegate approval authority to several
other employees. You are given a directed graph with n employees labeled from 0 to n - 1,
where each directed edge [u, v] means employee u can forward an approval request directly
to employee v in one step. You are also given a list of employees who are initially
authorized to start the request, and a list of target employees whose signatures are
considered acceptable final approvals.

Your task is to compute the minimum number of delegation steps needed for any starting
employee to reach any target employee. If no target can be reached from any starting
employee, return -1.

This is not the same as finding a path from one fixed source to one fixed destination.
Multiple starting points and multiple acceptable ending points are allowed, and the
optimal answer may begin from any start node and end at any target node.

Return the fewest number of edges in such a path.

Constraints:
- 1 <= n <= 2 * 10^5
- 0 <= edges.length <= 3 * 10^5
- edges[i] = [u, v]
- 0 <= u, v < n
- 1 <= starts.length <= n
- 1 <= targets.length <= n
- Values in starts are distinct
- Values in targets are distinct

Examples:
1)
Input:
n = 7
edges = [[0,1],[1,3],[2,3],[3,4],[4,6],[2,5]]
starts = [0,2]
targets = [6,5]

Output: 1

Explanation:
Start employee 2 can delegate directly to employee 5, which is already an acceptable
final approver. So the minimum number of steps is 1.

2)
Input:
n = 6
edges = [[0,1],[1,2],[2,3],[4,5]]
starts = [0,4]
targets = [3]

Output: 3

Explanation:
The shortest valid chain is 0 -> 1 -> 2 -> 3, which takes 3 delegation steps.
The other start employee 4 cannot reach the target.
"""

from collections import deque
from typing import Deque, List, Set


class Solution:
    def minimum_approval_steps(
        self,
        n: int,
        edges: List[List[int]],
        starts: List[int],
        targets: List[int],
    ) -> int:
        """
        Compute the minimum number of directed edges needed to go from any start node
        to any target node in a directed graph.

        Args:
            n: Number of nodes in the graph, labeled from 0 to n - 1.
            edges: Directed edges where [u, v] means u can reach v in one step.
            starts: List of valid starting nodes.
            targets: List of acceptable ending nodes.

        Returns:
            The minimum number of steps from any start to any target, or -1 if no
            such path exists.

        Time complexity:
            O(n + m), where m is the number of edges.

        Space complexity:
            O(n + m), for the adjacency list, visited structure, and BFS queue.
        """
        # Convert targets into a set so we can check "is this node a target?"
        # in average O(1) time.
        #
        # Why use a set?
        # - We will perform this membership check many times during BFS.
        # - A list would make each check O(k), where k is number of targets.
        # - A set keeps the algorithm efficient for large inputs.
        target_set: Set[int] = set(targets)

        # Important edge case:
        # If any starting node is already a target node, then we need 0 steps,
        # because we are already at an acceptable final approver.
        #
        # Example:
        # starts = [2, 4], targets = [4, 7]
        # Then start node 4 is itself a target, so answer is 0.
        for start in starts:
            if start in target_set:
                return 0

        # Build the adjacency list for the directed graph.
        #
        # adjacency[u] will contain all nodes v such that there is an edge u -> v.
        #
        # Why adjacency list?
        # - It is the standard efficient representation for sparse graphs.
        # - Constraints allow up to 3 * 10^5 edges, so adjacency matrix would be
        #   far too large.
        adjacency: List[List[int]] = [[] for _ in range(n)]
        for u, v in edges:
            adjacency[u].append(v)

        # We will run a MULTI-SOURCE BFS.
        #
        # Key idea:
        # Instead of running BFS separately from each start node, we put all start
        # nodes into the queue initially with distance 0.
        #
        # Why is this correct?
        # - BFS explores nodes in increasing order of distance.
        # - Starting from all starts at once means the first time we reach any node,
        #   we have found the shortest distance from the closest start.
        # - Therefore, the first target we pop/reach gives the global minimum steps
        #   from any start to any target.
        #
        # This is much more efficient than doing one BFS per start.
        queue: Deque[int] = deque()
        distance: List[int] = [-1] * n

        # Initialize BFS with all start nodes.
        #
        # distance[start] = 0 because no edges have been used yet.
        # We also mark them as visited by setting their distance.
        for start in starts:
            queue.append(start)
            distance[start] = 0

        # Standard BFS loop.
        while queue:
            # Pop the next node to process.
            current: int = queue.popleft()

            # Current node's shortest distance from the nearest start.
            current_distance: int = distance[current]

            # Explore all outgoing neighbors.
            for neighbor in adjacency[current]:
                # If neighbor has already been visited, skip it.
                #
                # Why?
                # In BFS, the first time we visit a node is guaranteed to be via
                # the shortest path (because BFS expands layer by layer).
                # So revisiting cannot improve the answer.
                if distance[neighbor] != -1:
                    continue

                # Record the shortest distance to this neighbor.
                distance[neighbor] = current_distance + 1

                # If this neighbor is a target, we can immediately return.
                #
                # Why is early return correct?
                # Because BFS guarantees that nodes are discovered in nondecreasing
                # order of distance from the set of start nodes.
                # So the first target discovered has the minimum possible number
                # of steps among all valid start-target paths.
                if neighbor in target_set:
                    return distance[neighbor]

                # Otherwise, continue BFS from this neighbor later.
                queue.append(neighbor)

        # If BFS finishes without finding any target, then no target is reachable
        # from any start node.
        return -1

    def minApprovalSteps(
        self,
        n: int,
        edges: List[List[int]],
        starts: List[int],
        targets: List[int],
    ) -> int:
        """
        Wrapper method using an alternative camelCase name.

        Args:
            n: Number of nodes in the graph.
            edges: Directed edges of the graph.
            starts: Starting nodes.
            targets: Target nodes.

        Returns:
            Minimum number of steps from any start to any target, or -1 if impossible.

        Time complexity:
            O(n + m), where m is the number of edges.

        Space complexity:
            O(n + m).
        """
        return self.minimum_approval_steps(n, edges, starts, targets)


if __name__ == "__main__":
    # Create an instance of the solution class.
    solution = Solution()

    # Example 1 from the problem statement.
    #
    # Graph edges:
    # 0 -> 1
    # 1 -> 3
    # 2 -> 3
    # 3 -> 4
    # 4 -> 6
    # 2 -> 5
    #
    # Starts: [0, 2]
    # Targets: [6, 5]
    #
    # Shortest valid path:
    # 2 -> 5
    # This uses exactly 1 edge, so expected answer is 1.
    n1: int = 7
    edges1: List[List[int]] = [[0, 1], [1, 3], [2, 3], [3, 4], [4, 6], [2, 5]]
    starts1: List[int] = [0, 2]
    targets1: List[int] = [6, 5]
    result1: int = solution.minimum_approval_steps(n1, edges1, starts1, targets1)
    print(result1)  # Expected: 1

    # Example 2 from the problem statement.
    #
    # Graph edges:
    # 0 -> 1 -> 2 -> 3
    # 4 -> 5
    #
    # Starts: [0, 4]
    # Targets: [3]
    #
    # Start 0 can reach 3 in 3 steps:
    # 0 -> 1 -> 2 -> 3
    #
    # Start 4 cannot reach 3.
    # So expected answer is 3.
    n2: int = 6
    edges2: List[List[int]] = [[0, 1], [1, 2], [2, 3], [4, 5]]
    starts2: List[int] = [0, 4]
    targets2: List[int] = [3]
    result2: int = solution.minimum_approval_steps(n2, edges2, starts2, targets2)
    print(result2)  # Expected: 3

    # Additional quick sanity check:
    # If a start is already a target, answer should be 0.
    n3: int = 3
    edges3: List[List[int]] = [[0, 1], [1, 2]]
    starts3: List[int] = [2]
    targets3: List[int] = [2]
    result3: int = solution.minimum_approval_steps(n3, edges3, starts3, targets3)
    print(result3)  # Expected: 0

    # Additional unreachable case:
    # No path from any start to any target.
    n4: int = 5
    edges4: List[List[int]] = [[0, 1], [1, 2], [3, 4]]
    starts4: List[int] = [0]
    targets4: List[int] = [4]
    result4: int = solution.minimum_approval_steps(n4, edges4, starts4, targets4)
    print(result4)  # Expected: -1