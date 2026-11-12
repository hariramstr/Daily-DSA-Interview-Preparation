import java.util.*;

/*
Problem Title: Find the Quietest Ancestor Department

Problem Description:
A company models its organization as a rooted tree of departments. Department 0 is the root headquarters.
Each department has a non-negative noise score representing how disruptive its daily operations are.
For every department, you must find the ancestor department on the path from the root to that department
(including the department itself) that has the smallest noise score. If multiple departments on that path
have the same minimum noise score, return the one that is closest to the root.

You are given an integer n, an array parent of length n where parent[0] = -1 and parent[i] is the parent
of department i, and an array noise of length n where noise[i] is the noise score of department i.

Return an array answer of length n where answer[i] is the index of the quietest ancestor department
for department i.

The input always forms a valid rooted tree.

Constraints:
- 1 <= n <= 100000
- parent.length == n
- noise.length == n
- parent[0] == -1
- 0 <= parent[i] < n for i > 0
- 0 <= noise[i] <= 1000000000
- The parent array represents a valid tree with root 0

Example 1:
Input: n = 7, parent = [-1,0,0,1,1,2,2], noise = [5,3,8,4,2,8,1]
Output: [0,1,0,1,4,0,6]

Example 2:
Input: n = 5, parent = [-1,0,1,2,3], noise = [4,4,6,2,2]
Output: [0,0,0,3,3]

Key Idea:
We do a single traversal from the root to all nodes.
While moving down the tree, we carry the best (quietest) ancestor seen so far.
For each child:
- If the child's noise is smaller than the current best noise, the child becomes the new best.
- If the child's noise is equal to the current best noise, we keep the earlier one (closer to the root),
  which is exactly the current best.
This gives an O(n) solution.
*/

public class Solution {

    /**
     * Finds, for every department, the ancestor on the root-to-that-department path
     * with the minimum noise score. If multiple ancestors have the same minimum noise,
     * the one closest to the root is chosen.
     *
     * Time complexity: O(n), because each node and each tree edge is processed once.
     * Space complexity: O(n), for the adjacency list, answer array, and traversal stack.
     *
     * @param n the number of departments
     * @param parent parent[i] is the parent of department i, with parent[0] = -1
     * @param noise noise[i] is the noise score of department i
     * @return an array where answer[i] is the index of the quietest ancestor department for department i
     */
    public int[] findQuietestAncestorDepartment(int n, int[] parent, int[] noise) {
        // Build the tree as an adjacency list.
        // children[u] will contain all direct child departments of department u.
        List<List<Integer>> children = buildChildrenList(n, parent);

        // This array will store the final answer for every department.
        int[] answer = new int[n];

        // We use an iterative DFS instead of recursive DFS.
        // Why?
        // Because n can be as large as 100000, and deep recursion could cause stack overflow in Java.
        //
        // Each stack entry stores:
        // - node: the current department
        // - bestAncestor: the quietest ancestor index seen on the path from root to this node
        Deque<int[]> stack = new ArrayDeque<>();

        // Start from the root department 0.
        // On the path to itself, the best ancestor is obviously 0.
        stack.push(new int[]{0, 0});

        // Standard DFS traversal.
        while (!stack.isEmpty()) {
            int[] current = stack.pop();
            int node = current[0];
            int bestAncestor = current[1];

            // The best ancestor carried into this node is exactly the answer for this node.
            answer[node] = bestAncestor;

            // Visit all children of the current node.
            for (int child : children.get(node)) {
                // By default, the best ancestor for the child is the same as the current best.
                int childBestAncestor = bestAncestor;

                // Compare the child's noise with the current best ancestor's noise.
                //
                // If child's noise is strictly smaller, then the child becomes the new best.
                // If child's noise is equal, we DO NOT replace the current best, because ties
                // must be broken in favor of the department closer to the root.
                if (noise[child] < noise[bestAncestor]) {
                    childBestAncestor = child;
                }

                // Push the child with its correct best ancestor information.
                stack.push(new int[]{child, childBestAncestor});
            }
        }

        return answer;
    }

    /**
     * Builds the children adjacency list from the parent array.
     *
     * Time complexity: O(n), because each node is added once to its parent's child list.
     * Space complexity: O(n), for storing all child relationships.
     *
     * @param n the number of departments
     * @param parent the parent array describing the rooted tree
     * @return adjacency list where result.get(u) contains all children of u
     */
    public List<List<Integer>> buildChildrenList(int n, int[] parent) {
        List<List<Integer>> children = new ArrayList<>(n);

        // Create an empty list for every node.
        for (int i = 0; i < n; i++) {
            children.add(new ArrayList<>());
        }

        // For every node except the root, add it to its parent's child list.
        for (int i = 1; i < n; i++) {
            children.get(parent[i]).add(i);
        }

        return children;
    }

    /**
     * Converts an int array to a readable string representation.
     *
     * Time complexity: O(n), where n is the array length.
     * Space complexity: O(n), for the produced string.
     *
     * @param arr the array to convert
     * @return a string like [a, b, c]
     */
    public String arrayToString(int[] arr) {
        return Arrays.toString(arr);
    }

    /**
     * Demonstrates the solution using the sample inputs from the problem statement.
     *
     * Time complexity: O(n) per demonstration call to the main algorithm.
     * Space complexity: O(n) per demonstration call.
     *
     * @param args command-line arguments (not used)
     * @return nothing
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Example 1
        int n1 = 7;
        int[] parent1 = {-1, 0, 0, 1, 1, 2, 2};
        int[] noise1 = {5, 3, 8, 4, 2, 8, 1};
        int[] result1 = solution.findQuietestAncestorDepartment(n1, parent1, noise1);

        System.out.println("Example 1:");
        System.out.println("Input n = " + n1);
        System.out.println("parent = " + solution.arrayToString(parent1));
        System.out.println("noise  = " + solution.arrayToString(noise1));
        System.out.println("Output = " + solution.arrayToString(result1));
        System.out.println("Expected = [0, 1, 0, 1, 4, 0, 6]");
        System.out.println();

        // Example 2
        int n2 = 5;
        int[] parent2 = {-1, 0, 1, 2, 3};
        int[] noise2 = {4, 4, 6, 2, 2};
        int[] result2 = solution.findQuietestAncestorDepartment(n2, parent2, noise2);

        System.out.println("Example 2:");
        System.out.println("Input n = " + n2);
        System.out.println("parent = " + solution.arrayToString(parent2));
        System.out.println("noise  = " + solution.arrayToString(noise2));
        System.out.println("Output = " + solution.arrayToString(result2));
        System.out.println("Expected = [0, 0, 0, 3, 3]");
        System.out.println();

        // Additional small sanity test
        int n3 = 1;
        int[] parent3 = {-1};
        int[] noise3 = {10};
        int[] result3 = solution.findQuietestAncestorDepartment(n3, parent3, noise3);

        System.out.println("Additional Test:");
        System.out.println("Input n = " + n3);
        System.out.println("parent = " + solution.arrayToString(parent3));
        System.out.println("noise  = " + solution.arrayToString(noise3));
        System.out.println("Output = " + solution.arrayToString(result3));
        System.out.println("Expected = [0]");
    }
}