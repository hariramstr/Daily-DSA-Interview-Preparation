import java.util.*;

/*
 * Title: Detect Circular Package Dependencies
 * Difficulty: Medium
 * Topic: Graphs
 *
 * Problem Description:
 * You are given a set of software packages and a list of dependency relationships between them.
 * Each dependency [a, b] means package a requires package b to be installed first.
 * A deployment is considered invalid if there exists any cycle in the dependency graph,
 * because at least one package would indirectly depend on itself.
 *
 * Write a function that determines whether the dependency configuration is valid.
 * Return true if all packages can be installed in some order, and false if there is at least
 * one circular dependency.
 *
 * The packages are labeled from 0 to n - 1. Not every package must appear in the dependency list.
 * A package with no incoming or outgoing edges is still considered part of the system.
 *
 * This problem models a directed graph. You need to detect whether the graph contains a cycle.
 * A valid installation order exists if and only if the graph is acyclic.
 *
 * Constraints:
 * - 1 <= n <= 100000
 * - 0 <= dependencies.length <= 200000
 * - dependencies[i].length == 2
 * - 0 <= a, b < n
 * - a != b
 * - There may be duplicate dependency pairs
 *
 * Example 1:
 * Input: n = 4, dependencies = [[1,0],[2,1],[3,2]]
 * Output: true
 * Explanation: One valid installation order is [0,1,2,3]. No package depends on itself through a chain.
 *
 * Example 2:
 * Input: n = 4, dependencies = [[1,0],[2,1],[0,2],[3,1]]
 * Output: false
 * Explanation: Package 0 depends on 2, 2 depends on 1, and 1 depends on 0 through the chain,
 * forming a cycle. Therefore, no valid installation order exists.
 */

public class Solution {

    /**
     * Determines whether all packages can be installed using Kahn's Algorithm
     * (topological sorting with indegree counting).
     *
     * Important graph interpretation:
     * If dependency [a, b] means "a requires b first", then the directed edge should be:
     * b -> a
     * because b must come before a in a valid installation order.
     *
     * The algorithm works as follows:
     * 1. Build the graph using adjacency lists.
     * 2. Compute indegree for every package.
     * 3. Put every package with indegree 0 into a queue.
     * 4. Repeatedly remove a package from the queue and "install" it.
     * 5. For each package that depends on it, reduce indegree by 1.
     * 6. If another package's indegree becomes 0, add it to the queue.
     * 7. At the end, if we processed all packages, there was no cycle.
     *    Otherwise, some packages were stuck in a cycle.
     *
     * @param n the total number of packages labeled from 0 to n - 1
     * @param dependencies an array where each pair [a, b] means package a depends on package b
     * @return true if the dependency graph is acyclic and all packages can be installed; false otherwise
     * Time complexity: O(n + m), where m is the number of dependency pairs
     * Space complexity: O(n + m)
     */
    public boolean canInstallAllPackages(int n, int[][] dependencies) {
        // Create an adjacency list for the graph.
        // graph.get(x) will contain all packages that become available only after x is installed.
        List<List<Integer>> graph = new ArrayList<>(n);

        // Initialize an empty list for every package, even if it has no edges.
        for (int i = 0; i < n; i++) {
            graph.add(new ArrayList<>());
        }

        // indegree[i] = number of prerequisites package i still needs before it can be installed.
        int[] indegree = new int[n];

        // Build the graph.
        // For dependency [a, b], package a depends on b, so we add edge b -> a.
        // That means once b is installed, it helps unlock a.
        for (int[] dependency : dependencies) {
            int a = dependency[0];
            int b = dependency[1];

            graph.get(b).add(a);
            indegree[a]++;
        }

        // Queue for packages that currently have no unmet prerequisites.
        Deque<Integer> queue = new ArrayDeque<>();

        // Initially, every package with indegree 0 can be installed immediately.
        for (int pkg = 0; pkg < n; pkg++) {
            if (indegree[pkg] == 0) {
                queue.offer(pkg);
            }
        }

        // Count how many packages we successfully process/install.
        int installedCount = 0;

        // Process packages in topological order.
        while (!queue.isEmpty()) {
            // Take one package that is currently installable.
            int current = queue.poll();
            installedCount++;

            // Visit every package that depends on the current package.
            for (int next : graph.get(current)) {
                // Since current is now installed, next has one fewer unmet prerequisite.
                indegree[next]--;

                // If next now has no remaining prerequisites, it becomes installable.
                if (indegree[next] == 0) {
                    queue.offer(next);
                }
            }
        }

        // If we installed all packages, there was no cycle.
        // If not, the remaining packages are part of or blocked by a cycle.
        return installedCount == n;
    }

    /**
     * Determines whether all packages can be installed using depth-first search
     * with explicit state tracking to detect cycles.
     *
     * State meanings:
     * 0 = unvisited
     * 1 = currently visiting (this node is in the active DFS path)
     * 2 = fully processed
     *
     * Cycle detection rule:
     * If during DFS we reach a node that is already in state 1,
     * then we found a back edge, which means there is a cycle.
     *
     * This method is included as an alternative graph-cycle detection approach.
     * For very large graphs in Java, the BFS/topological sort solution is often safer
     * because recursive DFS may risk stack overflow. Still, this implementation is correct.
     *
     * @param n the total number of packages labeled from 0 to n - 1
     * @param dependencies an array where each pair [a, b] means package a depends on package b
     * @return true if the dependency graph has no cycle; false otherwise
     * Time complexity: O(n + m), where m is the number of dependency pairs
     * Space complexity: O(n + m)
     */
    public boolean canInstallAllPackagesDfs(int n, int[][] dependencies) {
        // Build graph with edge b -> a for dependency [a, b].
        List<List<Integer>> graph = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            graph.add(new ArrayList<>());
        }

        for (int[] dependency : dependencies) {
            int a = dependency[0];
            int b = dependency[1];
            graph.get(b).add(a);
        }

        // state[i]:
        // 0 = not visited yet
        // 1 = currently exploring this node in the current DFS path
        // 2 = completely explored, no cycle found through it
        int[] state = new int[n];

        // We must start DFS from every node because the graph may be disconnected.
        for (int pkg = 0; pkg < n; pkg++) {
            if (state[pkg] == 0) {
                if (hasCycle(pkg, graph, state)) {
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * Recursively explores the graph to detect whether a cycle exists starting from the given node.
     *
     * Detailed logic:
     * - If we see a node in state 1, we returned to a node already in the current DFS path,
     *   so a cycle exists.
     * - If we see a node in state 2, it was already fully checked before, so no need to repeat work.
     * - Otherwise, mark the node as state 1, recursively explore neighbors,
     *   then mark it as state 2 when done.
     *
     * @param node the current package being explored
     * @param graph adjacency list representing the dependency graph
     * @param state visitation state array
     * @return true if a cycle is found reachable from this node; false otherwise
     * Time complexity: O(n + m) across all DFS calls combined
     * Space complexity: O(n + m), plus recursion stack up to O(n) in the worst case
     */
    public boolean hasCycle(int node, List<List<Integer>> graph, int[] state) {
        // If this node is currently being visited, we found a back edge.
        // That means a cycle exists.
        if (state[node] == 1) {
            return true;
        }

        // If this node was already fully processed earlier,
        // then we already know no cycle exists through it.
        if (state[node] == 2) {
            return false;
        }

        // Mark this node as currently in the recursion path.
        state[node] = 1;

        // Explore all outgoing edges from this node.
        for (int neighbor : graph.get(node)) {
            if (hasCycle(neighbor, graph, state)) {
                return true;
            }
        }

        // All descendants were processed safely, so mark this node as fully done.
        state[node] = 2;
        return false;
    }

    /**
     * Helper method to print a dependency list in a readable format.
     *
     * @param dependencies the dependency pairs to print
     * @return a string representation of the dependency array
     * Time complexity: O(m)
     * Space complexity: O(m) for the generated string
     */
    public String dependenciesToString(int[][] dependencies) {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < dependencies.length; i++) {
            sb.append(Arrays.toString(dependencies[i]));
            if (i < dependencies.length - 1) {
                sb.append(",");
            }
        }
        sb.append("]");
        return sb.toString();
    }

    /**
     * Demonstrates the solution using the sample inputs from the problem statement
     * and prints the results.
     *
     * Verified expected outputs:
     * Example 1:
     * n = 4, dependencies = [[1,0],[2,1],[3,2]]
     * Graph edges: 0->1, 1->2, 2->3
     * This is acyclic, so output is true.
     *
     * Example 2:
     * n = 4, dependencies = [[1,0],[2,1],[0,2],[3,1]]
     * Graph edges: 0->1, 1->2, 2->0, 1->3
     * There is a cycle 0->1->2->0, so output is false.
     *
     * @param args command-line arguments (not used)
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        int n1 = 4;
        int[][] dependencies1 = {
            {1, 0},
            {2, 1},
            {3, 2}
        };

        int n2 = 4;
        int[][] dependencies2 = {
            {1, 0},
            {2, 1},
            {0, 2},
            {3, 1}
        };

        boolean result1 = solution.canInstallAllPackages(n1, dependencies1);
        boolean result2 = solution.canInstallAllPackages(n2, dependencies2);

        System.out.println("Example 1:");
        System.out.println("n = " + n1 + ", dependencies = " + solution.dependenciesToString(dependencies1));
        System.out.println("Output: " + result1);
        System.out.println("Expected: true");
        System.out.println();

        System.out.println("Example 2:");
        System.out.println("n = " + n2 + ", dependencies = " + solution.dependenciesToString(dependencies2));
        System.out.println("Output: " + result2);
        System.out.println("Expected: false");
        System.out.println();

        // Additional quick checks for beginner understanding.

        int n3 = 3;
        int[][] dependencies3 = {};
        System.out.println("Additional Example 3:");
        System.out.println("n = " + n3 + ", dependencies = " + solution.dependenciesToString(dependencies3));
        System.out.println("Output: " + solution.canInstallAllPackages(n3, dependencies3));
        System.out.println("Expected: true");
        System.out.println();

        int n4 = 2;
        int[][] dependencies4 = {
            {1, 0},
            {1, 0}
        };
        System.out.println("Additional Example 4 (duplicate dependencies):");
        System.out.println("n = " + n4 + ", dependencies = " + solution.dependenciesToString(dependencies4));
        System.out.println("Output: " + solution.canInstallAllPackages(n4, dependencies4));
        System.out.println("Expected: true");
    }
}