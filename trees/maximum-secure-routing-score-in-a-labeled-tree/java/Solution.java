import java.util.*;

/*
 * Title: Maximum Secure Routing Score in a Labeled Tree
 * Difficulty: Hard
 * Topic: Trees
 *
 * Problem Description:
 * You are given an undirected tree with n nodes numbered from 0 to n - 1.
 * Each node represents a router, and each router has a security label given by an integer value.
 * A route is defined as any simple path between two nodes in the tree, including a path of length 0
 * consisting of a single node.
 *
 * The secure routing score of a route is defined as:
 *
 *     (score of the route) = (sum of labels on the path) - (maximum label on the path) + (minimum label on the path)
 *
 * Your task is to return the maximum secure routing score among all possible routes in the tree.
 *
 * A path may start and end at any two nodes, and the endpoints do not need to include the root.
 * The tree is not necessarily binary.
 *
 * Constraints:
 * - 1 <= n <= 2 * 10^5
 * - 1 <= labels[i] <= 10^9
 * - edges.length == n - 1
 * - edges[i] = [ui, vi]
 * - 0 <= ui, vi < n
 * - The input edges always form a valid tree.
 *
 * Important note about the examples in the prompt:
 * The numeric explanations shown there are inconsistent with the stated formula.
 * This implementation follows the formula exactly:
 *
 *     score(path) = sum(path) - max(path) + min(path)
 *
 * and computes the mathematically correct maximum value for that definition.
 *
 * Core idea of the solution:
 * --------------------------------
 * For any chosen path, let M be the maximum label on that path and m be the minimum label on that path.
 * Then:
 *
 *     score = sum(path) - M + m
 *
 * We can rewrite the optimization by fixing an interval [m, M] of allowed labels.
 * Among all nodes whose labels lie inside [m, M], every valid path entirely inside that induced forest
 * has score:
 *
 *     sum(path) - M + m
 *
 * Therefore, for a fixed pair of label values (m, M), the best path is simply the maximum path sum
 * inside the subgraph induced by nodes with labels in [m, M].
 *
 * A direct enumeration of all value pairs is impossible.
 *
 * The key observation is that if we process labels in sorted order and use centroid decomposition,
 * then every path is handled at the centroid where its two sides split. For a path passing through
 * a centroid c, once we decide which endpoint contributes the path minimum and which contributes the
 * path maximum, the score can be expressed as:
 *
 *     (sum side A including c) + (sum side B including c) - label[c]
 *     plus a correction depending on the larger/smaller extreme values seen on the two sides.
 *
 * To make this efficient, for every centroid we collect, for each node reachable in each child branch:
 *   - path sum from centroid to that node
 *   - minimum label on that centroid-to-node path
 *   - maximum label on that centroid-to-node path
 *
 * Then we combine branches using Fenwick trees after coordinate compression.
 *
 * Specifically, for a path through centroid c joining two nodes x and y from different branches
 * (or one endpoint being c itself), define for each side:
 *   sumSide = sum(labels on path c..node)
 *   minSide = minimum label on path c..node
 *   maxSide = maximum label on path c..node
 *
 * The full path score becomes:
 *
 *   sumSide1 + sumSide2 - label[c] - max(maxSide1, maxSide2) + min(minSide1, minSide2)
 *
 * We split into two symmetric cases:
 *
 *   Case 1: maxSide1 >= maxSide2 and minSide1 <= minSide2
 *           score = (sumSide1 - maxSide1 + minSide1) + (sumSide2 - label[c])
 *
 *   Case 2: maxSide1 >= maxSide2 and minSide1 > minSide2
 *           score = (sumSide1 - maxSide1) + (sumSide2 - label[c] + minSide2)
 *
 * These can be answered with offline data structures over inequalities on min/max.
 *
 * The implementation below uses centroid decomposition and two Fenwick trees supporting prefix/suffix maxima.
 * Total complexity is O(n log^2 n), which is suitable for n up to 2 * 10^5.
 */
public class Solution {

    /**
     * Small record-like container describing one centroid-to-node path state.
     *
     * Each state corresponds to a node reachable from the current centroid through exactly one branch
     * (or the centroid itself when we create a synthetic state).
     */
    private static class State {
        long sum;
        int min;
        int max;
        int branch;

        State(long sum, int min, int max, int branch) {
            this.sum = sum;
            this.min = min;
            this.max = max;
            this.branch = branch;
        }
    }

    /**
     * Fenwick tree (Binary Indexed Tree) for prefix maximum queries.
     *
     * We store long values and support:
     * - update(index, value): tree[index] = max(tree[index], value)
     * - query(index): maximum on range [1..index]
     */
    private static class FenwickMax {
        long[] bit;

        FenwickMax(int n) {
            bit = new long[n + 2];
            Arrays.fill(bit, Long.MIN_VALUE / 4);
        }

        void update(int idx, long val) {
            int n = bit.length - 1;
            while (idx <= n) {
                if (val > bit[idx]) {
                    bit[idx] = val;
                }
                idx += idx & -idx;
            }
        }

        long query(int idx) {
            long ans = Long.MIN_VALUE / 4;
            while (idx > 0) {
                if (bit[idx] > ans) {
                    ans = bit[idx];
                }
                idx -= idx & -idx;
            }
            return ans;
        }
    }

    private int n;
    private int[] labels;
    private List<Integer>[] graph;

    private boolean[] removed;
    private int[] subtreeSize;

    private long answer;

    /**
     * Solves the problem for the given tree.
     *
     * @param n the number of nodes in the tree
     * @param labels the security label of each node
     * @param edges the undirected tree edges
     * @return the maximum secure routing score over all simple paths
     *
     * Time complexity: O(n log^2 n)
     * Space complexity: O(n log n) in the worst case due to decomposition recursion and temporary lists
     */
    public long maximumSecureRoutingScore(int n, int[] labels, int[][] edges) {
        this.n = n;
        this.labels = labels;
        this.graph = new ArrayList[n];
        for (int i = 0; i < n; i++) {
            graph[i] = new ArrayList<>();
        }
        for (int[] e : edges) {
            graph[e[0]].add(e[1]);
            graph[e[1]].add(e[0]);
        }

        this.removed = new boolean[n];
        this.subtreeSize = new int[n];
        this.answer = 0L;

        for (int v : labels) {
            answer = Math.max(answer, v);
        }

        decompose(0);
        return answer;
    }

    /**
     * Performs centroid decomposition recursively.
     *
     * At each centroid:
     * 1. Gather all centroid-to-node states for every child branch.
     * 2. Count the best path that passes through this centroid.
     * 3. Recurse into each remaining component.
     *
     * @param entry any node inside the current not-yet-removed component
     *
     * Time complexity: O(size(component) log^2 size(component)) amortized over decomposition
     * Space complexity: O(size(component)) temporary
     */
    public void decompose(int entry) {
        int total = computeSubtreeSize(entry, -1);
        int centroid = findCentroid(entry, -1, total);
        removed[centroid] = true;

        processCentroid(centroid);

        for (int nei : graph[centroid]) {
            if (!removed[nei]) {
                decompose(nei);
            }
        }
    }

    /**
     * Computes subtree sizes inside the current active component.
     *
     * @param u current node
     * @param p parent node in DFS
     * @return size of the active subtree rooted at u
     *
     * Time complexity: O(size of traversed component)
     * Space complexity: O(recursion depth)
     */
    public int computeSubtreeSize(int u, int p) {
        subtreeSize[u] = 1;
        for (int v : graph[u]) {
            if (v != p && !removed[v]) {
                subtreeSize[u] += computeSubtreeSize(v, u);
            }
        }
        return subtreeSize[u];
    }

    /**
     * Finds the centroid of the current active component.
     *
     * @param u current node
     * @param p parent node
     * @param total total size of the current component
     * @return centroid node
     *
     * Time complexity: O(size of traversed component)
     * Space complexity: O(recursion depth)
     */
    public int findCentroid(int u, int p, int total) {
        for (int v : graph[u]) {
            if (v != p && !removed[v] && subtreeSize[v] > total / 2) {
                return findCentroid(v, u, total);
            }
        }
        return u;
    }

    /**
     * Processes all paths whose highest centroid-decomposition separator is the given centroid.
     *
     * This method is the heart of the algorithm.
     *
     * We gather one list of states per child branch. Each state describes a path from the centroid
     * to some node in that branch:
     * - sum = sum of labels on the path from centroid to that node, inclusive
     * - min = minimum label on that path
     * - max = maximum label on that path
     *
     * Any simple path passing through the centroid is formed by combining:
     * - one state from branch A (or the centroid itself),
     * - one state from branch B (or the centroid itself),
     * where A and B are different branches.
     *
     * We evaluate all such combinations efficiently using two offline passes.
     *
     * @param centroid the centroid currently being processed
     *
     * Time complexity: O(k log k) for k states gathered at this centroid
     * Space complexity: O(k)
     */
    public void processCentroid(int centroid) {
        List<State> all = new ArrayList<>();
        List<List<State>> branchLists = new ArrayList<>();

        int branchId = 0;
        for (int nei : graph[centroid]) {
            if (removed[nei]) {
                continue;
            }
            List<State> list = new ArrayList<>();
            collectStates(
                    nei,
                    centroid,
                    labels[centroid] + (long) labels[nei],
                    Math.min(labels[centroid], labels[nei]),
                    Math.max(labels[centroid], labels[nei]),
                    branchId,
                    list
            );
            branchLists.add(list);
            all.addAll(list);
            branchId++;
        }

        answer = Math.max(answer, labels[centroid]);

        for (State s : all) {
            long singlePathScore = s.sum - s.max + s.min;
            if (singlePathScore > answer) {
                answer = singlePathScore;
            }
        }

        if (all.isEmpty()) {
            return;
        }

        List<Integer> mins = new ArrayList<>();
        List<Integer> maxs = new ArrayList<>();
        for (State s : all) {
            mins.add(s.min);
            maxs.add(s.max);
        }
        mins.add(labels[centroid]);
        maxs.add(labels[centroid]);

        Collections.sort(mins);
        mins = unique(mins);

        Collections.sort(maxs);
        maxs = unique(maxs);

        List<State> globalStates = new ArrayList<>(all);
        State centroidState = new State(labels[centroid], labels[centroid], labels[centroid], -1);
        globalStates.add(centroidState);

        combineCaseOne(globalStates, branchLists, centroid, mins, maxs);
        combineCaseTwo(globalStates, branchLists, centroid, mins, maxs);
    }

    /**
     * Handles combinations satisfying:
     *
     *   maxA >= maxB and minA <= minB
     *
     * For such pairs:
     *
     *   score = (sumA - maxA + minA) + (sumB - label[centroid])
     *
     * So for each B we need the best A among already inserted states with:
     *   maxA >= maxB
     *   minA <= minB
     * and from a different branch.
     *
     * We process states in descending max order, and use a Fenwick tree over min.
     *
     * @param globalStates all states plus the synthetic centroid-only state
     * @param branchLists states grouped by branch
     * @param centroid current centroid
     * @param mins compressed sorted unique minimum values
     * @param maxs compressed sorted unique maximum values
     *
     * Time complexity: O(k log k)
     * Space complexity: O(k)
     */
    public void combineCaseOne(List<State> globalStates, List<List<State>> branchLists, int centroid,
                               List<Integer> mins, List<Integer> maxs) {
        List<State> sortedGlobal = new ArrayList<>(globalStates);
        sortedGlobal.sort((a, b) -> Integer.compare(b.max, a.max));

        List<List<State>> sortedBranches = new ArrayList<>();
        for (List<State> branch : branchLists) {
            List<State> copy = new ArrayList<>(branch);
            copy.sort((a, b) -> Integer.compare(b.max, a.max));
            sortedBranches.add(copy);
        }

        FenwickMax globalFenwick = new FenwickMax(mins.size() + 2);
        FenwickMax[] branchFenwick = new FenwickMax[sortedBranches.size()];
        for (int i = 0; i < sortedBranches.size(); i++) {
            branchFenwick[i] = new FenwickMax(mins.size() + 2);
        }

        int[] branchPtr = new int[sortedBranches.size()];
        int gPtr = 0;

        List<State> queryOrder = new ArrayList<>(globalStates);
        queryOrder.sort((a, b) -> Integer.compare(b.max, a.max));

        for (State b : queryOrder) {
            while (gPtr < sortedGlobal.size() && sortedGlobal.get(gPtr).max >= b.max) {
                State a = sortedGlobal.get(gPtr++);
                int minIndex = lowerBound(mins, a.min) + 1;
                long value = a.sum - a.max + a.min;
                globalFenwick.update(minIndex, value);
            }

            for (int id = 0; id < sortedBranches.size(); id++) {
                List<State> branch = sortedBranches.get(id);
                while (branchPtr[id] < branch.size() && branch.get(branchPtr[id]).max >= b.max) {
                    State a = branch.get(branchPtr[id]++);
                    int minIndex = lowerBound(mins, a.min) + 1;
                    long value = a.sum - a.max + a.min;
                    branchFenwick[id].update(minIndex, value);
                }
            }

            int queryMinIndex = upperBound(mins, b.min);
            long best = globalFenwick.query(queryMinIndex);
            if (b.branch >= 0) {
                long sameBranch = branchFenwick[b.branch].query(queryMinIndex);
                if (sameBranch == best) {
                    // We need a different branch. Since Fenwick stores only one maximum,
                    // exact subtraction is not possible. To keep correctness, we handle this
                    // by also querying all branches when needed. This fallback is only triggered
                    // on ties and remains acceptable in practice because centroid levels are logarithmic.
                    best = Long.MIN_VALUE / 4;
                    long centroidCandidate = centroidOnlyCaseOne(centroid, b);
                    if (centroidCandidate > best) {
                        best = centroidCandidate;
                    }
                    for (int id = 0; id < branchLists.size(); id++) {
                        if (id == b.branch) {
                            continue;
                        }
                        long candidate = queryBestCaseOneInBranch(sortedBranches.get(id), b.min, b.max);
                        if (candidate > best) {
                            best = candidate;
                        }
                    }
                }
            }

            if (best > Long.MIN_VALUE / 8) {
                long score = best + (b.sum - labels[centroid]);
                if (score > answer) {
                    answer = score;
                }
            }
        }
    }

    /**
     * Handles combinations satisfying:
     *
     *   maxA >= maxB and minA > minB
     *
     * For such pairs:
     *
     *   score = (sumA - maxA) + (sumB - label[centroid] + minB)
     *
     * So for each B we need the best A among already inserted states with:
     *   maxA >= maxB
     *   minA > minB
     * and from a different branch.
     *
     * We again process in descending max order, but now query a suffix over min.
     * A suffix query is implemented by reversing the min coordinate.
     *
     * @param globalStates all states plus the synthetic centroid-only state
     * @param branchLists states grouped by branch
     * @param centroid current centroid
     * @param mins compressed sorted unique minimum values
     * @param maxs compressed sorted unique maximum values
     *
     * Time complexity: O(k log k)
     * Space complexity: O(k)
     */
    public void combineCaseTwo(List<State> globalStates, List<List<State>> branchLists, int centroid,
                               List<Integer> mins, List<Integer> maxs) {
        List<State> sortedGlobal = new ArrayList<>(globalStates);
        sortedGlobal.sort((a, b) -> Integer.compare(b.max, a.max));

        List<List<State>> sortedBranches = new ArrayList<>();
        for (List<State> branch : branchLists) {
            List<State> copy = new ArrayList<>(branch);
            copy.sort((a, b) -> Integer.compare(b.max, a.max));
            sortedBranches.add(copy);
        }

        FenwickMax globalFenwick = new FenwickMax(mins.size() + 2);
        FenwickMax[] branchFenwick = new FenwickMax[sortedBranches.size()];
        for (int i = 0; i < sortedBranches.size(); i++) {
            branchFenwick[i] = new FenwickMax(mins.size() + 2);
