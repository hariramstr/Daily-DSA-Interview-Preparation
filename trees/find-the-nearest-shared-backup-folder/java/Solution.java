import java.util.*;

/*
 * Title: Find the Nearest Shared Backup Folder
 * Difficulty: Medium
 * Topic: Trees
 *
 * Problem Description:
 * A company stores project backups in a rooted tree of folders. Each folder has a unique integer ID
 * from 0 to n - 1, and folder 0 is always the root. You are given the tree as a parent array where
 * parent[i] is the parent of folder i, and parent[0] = -1.
 *
 * For several restore operations, two backup snapshots are selected from folders a and b. The restore
 * system needs to locate the nearest folder that is an ancestor of both a and b. This folder is the
 * deepest folder that lies on both paths from a and b to the root.
 *
 * Your task is to answer all queries and return an array where the i-th answer is the nearest shared
 * backup folder for queries[i] = [a, b].
 *
 * A folder is considered an ancestor of itself.
 *
 * Constraints:
 * - 1 <= n <= 2 * 10^5
 * - parent.length == n
 * - parent[0] == -1
 * - For every i > 0, 0 <= parent[i] < n
 * - The input parent array always forms a valid tree rooted at 0
 * - 1 <= queries.length <= 2 * 10^5
 * - queries[i].length == 2
 * - 0 <= a, b < n
 *
 * Example 1:
 * Input:
 * parent = [-1,0,0,1,1,2,2]
 * queries = [[3,4],[3,5],[2,6],[0,6]]
 * Output:
 * [1,0,2,0]
 *
 * Example 2:
 * Input:
 * parent = [-1,0,1,2,3,1,5]
 * queries = [[4,6],[2,4],[6,6],[3,5]]
 * Output:
 * [1,2,6,1]
 *
 * Efficient Approach:
 * This is a classic Lowest Common Ancestor (LCA) problem on a rooted tree.
 *
 * We preprocess:
 * 1. depth[node] = distance from root
 * 2. up[k][node] = the 2^k-th ancestor of node
 *
 * Then for each query (a, b):
 * 1. If depths differ, lift the deeper node upward until both are at the same depth.
 * 2. If they become equal, that node is the answer.
 * 3. Otherwise, lift both upward from the largest jump down to the smallest jump,
 *    but only when their ancestors differ.
 * 4. Their parents then become the LCA.
 *
 * This allows answering each query in O(log n) time after O(n log n) preprocessing.
 */
public class Solution {

    /**
     * Answers all nearest shared backup folder queries using Lowest Common Ancestor (LCA)
     * with binary lifting.
     *
     * @param parent the parent array representing the rooted tree; parent[0] = -1
     * @param queries each query contains two folder IDs [a, b]
     * @return an array where each element is the nearest shared backup folder for the corresponding query
     *
     * Time complexity:
     * - Preprocessing: O(n log n)
     * - Each query: O(log n)
     * - Total: O((n + q) log n)
     *
     * Space complexity:
     * - O(n log n) for the binary lifting table and adjacency representation
     */
    public int[] nearestSharedBackupFolder(int[] parent, int[][] queries) {
        int n = parent.length;

        // Compute how many levels are needed in the binary lifting table.
        // If n <= 200000, log2(n) is below 18, but we compute it generally.
        int maxLog = 1;
        while ((1 << maxLog) <= n) {
            maxLog++;
        }

        // Build children adjacency list from the parent array.
        // Since the input is a tree rooted at 0, every node except root has exactly one parent.
        List<List<Integer>> children = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            children.add(new ArrayList<>());
        }
        for (int node = 1; node < n; node++) {
            children.get(parent[node]).add(node);
        }

        // up[k][v] = 2^k-th ancestor of node v, or -1 if it does not exist.
        int[][] up = new int[maxLog][n];

        // depth[v] = number of edges from root to v.
        int[] depth = new int[n];

        // Initialize all ancestors as -1.
        for (int k = 0; k < maxLog; k++) {
            Arrays.fill(up[k], -1);
        }

        // We perform an iterative BFS from the root to fill:
        // 1) depth of each node
        // 2) immediate parent up[0][node]
        //
        // We use BFS instead of recursion to avoid stack overflow on deep trees.
        ArrayDeque<Integer> queue = new ArrayDeque<>();
        queue.offer(0);
        depth[0] = 0;
        up[0][0] = -1;

        while (!queue.isEmpty()) {
            int current = queue.poll();

            for (int child : children.get(current)) {
                depth[child] = depth[current] + 1;
                up[0][child] = current;
                queue.offer(child);
            }
        }

        // Build the rest of the binary lifting table.
        //
        // If up[k - 1][v] is the 2^(k-1)-th ancestor of v,
        // then the 2^k-th ancestor is:
        // up[k][v] = up[k - 1][ up[k - 1][v] ]
        //
        // Example:
        // - up[0][v] = parent
        // - up[1][v] = grandparent
        // - up[2][v] = 4th ancestor
        // and so on.
        for (int k = 1; k < maxLog; k++) {
            for (int v = 0; v < n; v++) {
                int midAncestor = up[k - 1][v];
                if (midAncestor != -1) {
                    up[k][v] = up[k - 1][midAncestor];
                }
            }
        }

        // Answer each query independently using the preprocessed data.
        int[] answer = new int[queries.length];
        for (int i = 0; i < queries.length; i++) {
            int a = queries[i][0];
            int b = queries[i][1];
            answer[i] = lca(a, b, depth, up);
        }

        return answer;
    }

    /**
     * Computes the Lowest Common Ancestor (LCA) of two nodes in a rooted tree.
     *
     * @param a the first node
     * @param b the second node
     * @param depth depth array where depth[v] is the distance from root to v
     * @param up binary lifting table where up[k][v] is the 2^k-th ancestor of v
     * @return the lowest common ancestor of a and b
     *
     * Time complexity:
     * - O(log n)
     *
     * Space complexity:
     * - O(1) extra space beyond the input preprocessing tables
     */
    public int lca(int a, int b, int[] depth, int[][] up) {
        // Step 1:
        // Ensure that 'a' is the deeper node (or equal depth).
        // This makes the "lift deeper node upward" step simpler.
        if (depth[a] < depth[b]) {
            int temp = a;
            a = b;
            b = temp;
        }

        // Step 2:
        // Lift 'a' upward until both nodes are at the same depth.
        //
        // Suppose depth[a] = 10 and depth[b] = 6.
        // We need to move 'a' up by 4 levels.
        // Binary lifting allows us to do this in O(log n) jumps.
        int depthDifference = depth[a] - depth[b];
        a = liftNode(a, depthDifference, up);

        // If after equalizing depths both nodes are the same,
        // then one node was an ancestor of the other,
        // and that node is the LCA.
        if (a == b) {
            return a;
        }

        // Step 3:
        // Lift both nodes upward together.
        //
        // We try the largest jump first.
        // If the 2^k-th ancestors of a and b are different,
        // then the LCA must be above those ancestors,
        // so we can safely lift both nodes to those ancestors.
        //
        // We do NOT lift when the ancestors are the same,
        // because that would jump over the LCA.
        for (int k = up.length - 1; k >= 0; k--) {
            if (up[k][a] != up[k][b]) {
                a = up[k][a];
                b = up[k][b];
            }
        }

        // Step 4:
        // Now a and b are distinct, but their parents are the same.
        // Therefore, that shared parent is the LCA.
        return up[0][a];
    }

    /**
     * Lifts a node upward by a given number of levels using binary lifting.
     *
     * @param node the starting node
     * @param distance how many levels to move upward
     * @param up binary lifting table where up[k][v] is the 2^k-th ancestor of v
     * @return the ancestor reached after moving up 'distance' levels
     *
     * Time complexity:
     * - O(log n)
     *
     * Space complexity:
     * - O(1)
     */
    public int liftNode(int node, int distance, int[][] up) {
        // We inspect each bit of 'distance'.
        //
        // If bit k is set in distance, we move the node up by 2^k levels.
        //
        // Example:
        // distance = 13 = binary 1101
        // Then we lift by 1 + 4 + 8 levels.
        int bit = 0;
        while (distance > 0) {
            if ((distance & 1) == 1) {
                node = up[bit][node];
            }
            distance >>= 1;
            bit++;
        }
        return node;
    }

    /**
     * Utility method to print an integer array in a readable format.
     *
     * @param arr the array to print
     * @return a string representation such as [1, 2, 3]
     *
     * Time complexity:
     * - O(n)
     *
     * Space complexity:
     * - O(n) for the StringBuilder content
     */
    public String arrayToString(int[] arr) {
        return Arrays.toString(arr);
    }

    /**
     * Demonstrates the solution on the sample test cases from the problem statement.
     *
     * @param args command-line arguments (not used)
     *
     * Time complexity:
     * - Depends on the sample sizes; for each demonstration it is O((n + q) log n)
     *
     * Space complexity:
     * - Depends on the sample sizes; O(n log n)
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Example 1
        int[] parent1 = {-1, 0, 0, 1, 1, 2, 2};
        int[][] queries1 = {
            {3, 4},
            {3, 5},
            {2, 6},
            {0, 6}
        };
        int[] result1 = solution.nearestSharedBackupFolder(parent1, queries1);
        System.out.println("Example 1 Output: " + solution.arrayToString(result1));
        System.out.println("Expected: [1, 0, 2, 0]");

        // Example 2
        int[] parent2 = {-1, 0, 1, 2, 3, 1, 5};
        int[][] queries2 = {
            {4, 6},
            {2, 4},
            {6, 6},
            {3, 5}
        };
        int[] result2 = solution.nearestSharedBackupFolder(parent2, queries2);
        System.out.println("Example 2 Output: " + solution.arrayToString(result2));
        System.out.println("Expected: [1, 2, 6, 1]");
    }
}