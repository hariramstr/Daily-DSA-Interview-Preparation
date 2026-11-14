import java.util.*;

/*
 * Title: Find the Closest Available Meeting Room in an Office Tree
 * Difficulty: Medium
 * Topic: Trees
 *
 * Problem Description:
 * You are given the layout of an office campus as a rooted tree with nodes numbered from 0 to n - 1.
 * Each node represents either a hallway junction or a room, and edges represent direct walking connections.
 * Node 0 is the main entrance. Every node has a boolean value available[i] indicating whether that location
 * is currently an available meeting room. A query asks: starting from employee position u, which available
 * meeting room can be reached with the fewest number of edges?
 *
 * If multiple available meeting rooms are tied for the same minimum distance, return the one with the
 * smallest node index. If no available meeting room exists anywhere in the tree, return -1 for that query.
 *
 * You must answer many independent queries on the same tree efficiently.
 *
 * Implement a function that receives:
 * - n: number of nodes
 * - edges: n - 1 undirected edges of the tree
 * - available: array of length n
 * - queries: array of starting nodes
 *
 * Return an array where the ith value is the answer for queries[i].
 *
 * Constraints:
 * - 1 <= n <= 2 * 10^5
 * - edges.length == n - 1
 * - 0 <= u, v < n
 * - available.length == n
 * - 1 <= queries.length <= 2 * 10^5
 * - The input graph is guaranteed to be a tree
 *
 * Intended efficient idea:
 * Precompute, for every node, the nearest available room in the entire tree.
 * Then each query can be answered in O(1).
 *
 * This solution uses a classic rerooting-style tree DP:
 *
 * 1) First DFS / tree DP from children upward:
 *    For each node u, compute the best available room reachable inside u's subtree.
 *    We store:
 *      - downDist[u]: minimum distance from u to an available room in its subtree
 *      - downNode[u]: the room index that achieves that minimum distance
 *
 * 2) Second DFS / rerooting from parent downward:
 *    For each node u, compute the best available room that comes from "outside" u's subtree.
 *    We store:
 *      - upDist[u]: minimum distance from u to an available room not using u's own subtree contribution
 *      - upNode[u]: the room index that achieves that minimum distance
 *
 *    To do this efficiently for all children of a node, we precompute prefix/suffix best values
 *    over the children contributions, so each child can exclude itself in O(1).
 *
 * 3) Final answer for each node:
 *    Compare the best downward answer and the best upward answer.
 *    The better one is the nearest available room for that node.
 *    If distances tie, choose the smaller node index.
 *
 * Total complexity:
 * - Preprocessing: O(n)
 * - Answering all queries: O(q)
 * - Total: O(n + q)
 */

public class Solution {

    /**
     * A large sentinel value representing "infinite distance" / unreachable.
     */
    private static final int INF = 1_000_000_000;

    /**
     * Adjacency list for the tree.
     */
    private List<Integer>[] graph;

    /**
     * parent[u] = parent of node u in the rooted tree (rooted at 0).
     */
    private int[] parent;

    /**
     * order[] stores nodes in BFS/DFS-like parent-before-child order.
     * We use it to process bottom-up and top-down iteratively without recursion,
     * which avoids stack overflow for large trees.
     */
    private int[] order;

    /**
     * children[u] = list of children of u in the rooted tree.
     */
    private List<Integer>[] children;

    /**
     * downDist[u] = minimum distance from u to an available room inside u's subtree.
     * downNode[u] = room index achieving downDist[u].
     */
    private int[] downDist;
    private int[] downNode;

    /**
     * upDist[u] = minimum distance from u to an available room that comes from outside u's subtree.
     * upNode[u] = room index achieving upDist[u].
     */
    private int[] upDist;
    private int[] upNode;

    /**
     * finalAnswer[u] = nearest available room anywhere in the tree for starting node u.
     */
    private int[] finalAnswer;

    /**
     * finalDist[u] = distance from u to finalAnswer[u].
     */
    private int[] finalDist;

    /**
     * Solves the problem for all queries.
     *
     * @param n number of nodes in the tree
     * @param edges undirected edges of the tree, where each edge is [u, v]
     * @param available available[i] is true if node i is an available meeting room
     * @param queries starting nodes for which we must return the nearest available room
     * @return an array answers where answers[i] is the nearest available room for queries[i],
     *         or -1 if no available room exists
     *
     * Time complexity: O(n + q), where q = queries.length
     * Space complexity: O(n)
     */
    public int[] closestAvailableMeetingRoom(int n, int[][] edges, boolean[] available, int[] queries) {
        buildGraph(n, edges);
        buildRootedTree(n);
        computeDownDP(n, available);
        computeUpDP(n, available);
        buildFinalAnswers(n);

        int[] result = new int[queries.length];
        for (int i = 0; i < queries.length; i++) {
            result[i] = finalAnswer[queries[i]];
        }
        return result;
    }

    /**
     * Builds the undirected adjacency list of the tree.
     *
     * @param n number of nodes
     * @param edges undirected edges
     * @return nothing; initializes internal graph field
     *
     * Time complexity: O(n)
     * Space complexity: O(n)
     */
    public void buildGraph(int n, int[][] edges) {
        graph = new ArrayList[n];
        for (int i = 0; i < n; i++) {
            graph[i] = new ArrayList<>();
        }
        for (int[] edge : edges) {
            int u = edge[0];
            int v = edge[1];
            graph[u].add(v);
            graph[v].add(u);
        }
    }

    /**
     * Roots the tree at node 0 and builds:
     * - parent array
     * - traversal order with parent before children
     * - children lists
     *
     * We do this iteratively to avoid recursion depth issues on large trees.
     *
     * @param n number of nodes
     * @return nothing; initializes parent, order, and children fields
     *
     * Time complexity: O(n)
     * Space complexity: O(n)
     */
    public void buildRootedTree(int n) {
        parent = new int[n];
        Arrays.fill(parent, -1);

        children = new ArrayList[n];
        for (int i = 0; i < n; i++) {
            children[i] = new ArrayList<>();
        }

        order = new int[n];
        int idx = 0;

        ArrayDeque<Integer> queue = new ArrayDeque<>();
        queue.add(0);
        parent[0] = -2; // temporary marker meaning "root visited"

        while (!queue.isEmpty()) {
            int u = queue.poll();
            order[idx++] = u;

            for (int v : graph[u]) {
                if (parent[v] != -1) {
                    continue;
                }
                parent[v] = u;
                children[u].add(v);
                queue.add(v);
            }
        }

        parent[0] = -1; // restore root parent convention
    }

    /**
     * Computes the downward DP values:
     * - downDist[u]
     * - downNode[u]
     *
     * Meaning:
     * Among all available rooms in u's subtree, find the closest one to u.
     * If there is a tie in distance, choose the smaller node index.
     *
     * We process nodes in reverse rooted order so children are solved before parents.
     *
     * @param n number of nodes
     * @param available availability array
     * @return nothing; initializes downDist and downNode fields
     *
     * Time complexity: O(n)
     * Space complexity: O(n)
     */
    public void computeDownDP(int n, boolean[] available) {
        downDist = new int[n];
        downNode = new int[n];
        Arrays.fill(downDist, INF);
        Arrays.fill(downNode, -1);

        for (int i = n - 1; i >= 0; i--) {
            int u = order[i];

            // Candidate 1:
            // If u itself is an available room, then distance is 0 and room is u.
            if (available[u]) {
                relaxSelf(downDist, downNode, u, 0, u);
            }

            // Candidate 2:
            // Any available room in a child's subtree can be reached from u
            // by first going one edge down to that child.
            for (int child : children[u]) {
                if (downNode[child] != -1) {
                    relaxSelf(downDist, downNode, u, downDist[child] + 1, downNode[child]);
                }
            }
        }
    }

    /**
     * Computes the upward / rerooting DP values:
     * - upDist[u]
     * - upNode[u]
     *
     * Meaning:
     * Among all available rooms that can be reached from u without using the "downward answer"
     * from u's own subtree branch, what is the best one coming from parent side / siblings / ancestors?
     *
     * More concretely for a child v of u:
     * The best answer for v from outside v's subtree can come from:
     * 1) u itself, if u is available
     * 2) u's own up-answer (something above u)
     * 3) any sibling subtree of v
     *
     * To exclude v's own subtree efficiently, we compute prefix/suffix best child contributions at u.
     *
     * @param n number of nodes
     * @param available availability array
     * @return nothing; initializes upDist and upNode fields
     *
     * Time complexity: O(n)
     * Space complexity: O(n)
     */
    public void computeUpDP(int n, boolean[] available) {
        upDist = new int[n];
        upNode = new int[n];
        Arrays.fill(upDist, INF);
        Arrays.fill(upNode, -1);

        // Root has no parent side, so its up-answer is "none".
        upDist[0] = INF;
        upNode[0] = -1;

        // Process nodes in parent-before-child order.
        for (int i = 0; i < n; i++) {
            int u = order[i];
            List<Integer> childList = children[u];
            int m = childList.size();

            // For each child c of u, the contribution from that child's subtree to u is:
            // distance = downDist[c] + 1, node = downNode[c]
            //
            // We need prefix/suffix best arrays so that for child at position j,
            // we can quickly get the best contribution among all siblings except itself.
            int[] prefDist = new int[m];
            int[] prefNode = new int[m];
            int[] suffDist = new int[m];
            int[] suffNode = new int[m];

            for (int j = 0; j < m; j++) {
                int c = childList.get(j);
                int candDist = (downNode[c] == -1) ? INF : downDist[c] + 1;
                int candNode = downNode[c];

                if (j == 0) {
                    prefDist[j] = candDist;
                    prefNode[j] = candNode;
                } else {
                    int bestDist = prefDist[j - 1];
                    int bestNode = prefNode[j - 1];
                    if (isBetter(candDist, candNode, bestDist, bestNode)) {
                        bestDist = candDist;
                        bestNode = candNode;
                    }
                    prefDist[j] = bestDist;
                    prefNode[j] = bestNode;
                }
            }

            for (int j = m - 1; j >= 0; j--) {
                int c = childList.get(j);
                int candDist = (downNode[c] == -1) ? INF : downDist[c] + 1;
                int candNode = downNode[c];

                if (j == m - 1) {
                    suffDist[j] = candDist;
                    suffNode[j] = candNode;
                } else {
                    int bestDist = suffDist[j + 1];
                    int bestNode = suffNode[j + 1];
                    if (isBetter(candDist, candNode, bestDist, bestNode)) {
                        bestDist = candDist;
                        bestNode = candNode;
                    }
                    suffDist[j] = bestDist;
                    suffNode[j] = bestNode;
                }
            }

            // Now compute up-answer for each child.
            for (int j = 0; j < m; j++) {
                int v = childList.get(j);

                // We build the best candidate at node u that does NOT come from v's subtree.
                int bestAtUDist = INF;
                int bestAtUNode = -1;

                // Candidate A: u itself
                if (available[u]) {
                    if (isBetter(0, u, bestAtUDist, bestAtUNode)) {
                        bestAtUDist = 0;
                        bestAtUNode = u;
                    }
                }

                // Candidate B: something from above u
                if (upNode[u] != -1) {
                    if (isBetter(upDist[u], upNode[u], bestAtUDist, bestAtUNode)) {
                        bestAtUDist = upDist[u];
                        bestAtUNode = upNode[u];
                    }
                }

                // Candidate C: best sibling subtree on the left
                if (j - 1 >= 0) {
                    if (isBetter(prefDist[j - 1], prefNode[j - 1], bestAtUDist, bestAtUNode)) {
                        bestAtUDist = prefDist[j - 1];
                        bestAtUNode = prefNode[j - 1];
                    }
                }

                // Candidate D: best sibling subtree on the right
                if (j + 1 < m) {
                    if (isBetter(suffDist[j + 1], suffNode[j + 1], bestAtUDist, bestAtUNode)) {
                        bestAtUDist = suffDist[j + 1];
                        bestAtUNode = suffNode[j + 1];
                    }
                }

                // If the best candidate at u exists, then from child v it is one more edge away.
                if (bestAtUNode != -1) {
                    upDist[v] = bestAtUDist + 1;
                    upNode[v] = bestAtUNode;
                } else {
                    upDist[v] = INF;
                    upNode[v] = -1;
                }
            }
        }
    }

    /**
     * Combines downward and upward DP answers to produce the final nearest available room
     * for every node in the tree.
     *
     * For each node u:
     * - down-answer covers available rooms in u's subtree
     * - up-answer covers available rooms outside u's subtree
     * The better of the two is the global answer for u.
     *
     * @param n number of nodes
     * @return nothing; initializes finalAnswer and finalDist fields
     *
     * Time complexity: O(n)
     * Space complexity: O(n)
     */
    public void buildFinalAnswers(int n) {
        finalAnswer = new int[n];
        finalDist = new int[n];

        for (int u = 0; u < n; u++) {
            int bestDist = INF;
            int bestNode = -1;

            if (isBetter(downDist[u], downNode[u], bestDist, bestNode)) {
                bestDist = downDist[u];
                bestNode = downNode[u];
            }

            if (isBetter(upDist[u], upNode[u], bestDist, bestNode)) {
                bestDist = upDist[u];
                bestNode = upNode[u];
            }

            finalDist[u] = bestDist;
            finalAnswer[u] = bestNode;
        }
    }

    /**
     * Relaxes the current best answer stored at arrays[targetIndex] using a new candidate.
     *
     * This helper is used in the downward DP.
     *
     * @param distArray array storing best distances
     * @param nodeArray array storing corresponding best node indices
     * @param targetIndex index whose answer we want to improve
     * @param candDist candidate distance
     * @param candNode candidate node
     * @return nothing; updates distArray[targetIndex] and nodeArray[targetIndex] if candidate is better
     *
     * Time complexity: O(1)
     * Space complexity: O(1)
     */
    public void relaxSelf(int[] distArray, int[] nodeArray, int targetIndex, int candDist, int candNode) {
        if (isBetter(candDist, candNode, distArray[targetIndex], nodeArray[targetIndex])) {
            distArray[targetIndex] = candDist;
            nodeArray[targetIndex] = candNode;
        }
    }

    /**
     * Returns true if candidate (candDist, candNode) is better than current (bestDist, bestNode).
     *
     * Comparison rule:
     * 1) Smaller distance is better
     * 2) If distances tie, smaller node index is better
     * 3) Node -1 means "no valid answer"
     *
     * @param candDist candidate distance
     * @param candNode candidate node
     * @param bestDist current best distance
     * @param bestNode current best node
     * @return true if candidate should replace current best, otherwise false
     *
     * Time complexity: O(1)
     * Space complexity: O(1)
     */
    public boolean isBetter(int candDist, int candNode, int bestDist, int bestNode) {
        if (candNode == -1) {
            return false;
        }
        if (bestNode == -1) {
            return true;
        }
        if (candDist != bestDist) {
            return candDist < bestDist;
        }
        return candNode < bestNode;
    }

    /**
     * Utility method to print an int array in a beginner-friendly format.
     *
     * @param arr array to print
     * @return string representation of the array
     *
     * Time complexity: O(arr.length)
    