import java.util.*;

/*
Problem Title: Minimum Query Limit for Ranked Search Pages

Problem Description:
A search platform stores n documents in a fixed ranking order from 1 to n. The relevance score of document i is given by scores[i]. A user only sees the first L documents, where L is called the query limit. You are also given m analyst requirements. Each requirement is a pair [k, t], meaning that within the first L documents, there must be at least k documents whose relevance score is greater than or equal to t.

Your task is to find the minimum query limit L such that all requirements are satisfied at the same time. If no such L exists, return -1.

The ranking order cannot be changed. A document contributes to a requirement only if its position is at most L. Note that different requirements may use different score thresholds, and the same document may help satisfy multiple requirements.

This problem is designed for an efficient solution. A naive scan for every possible L will be too slow. You should exploit the monotonic nature of the answer: if a certain prefix length L satisfies all requirements, then any larger prefix length also satisfies all requirements.

Constraints:
- 1 <= n <= 200000
- 1 <= m <= 200000
- 1 <= scores[i] <= 10^9
- 1 <= k <= n
- 1 <= t <= 10^9
- Requirements are independent, but all must hold simultaneously

Example 1:
Input:
scores = [5, 1, 7, 3, 9, 6]
requirements = [[2, 6], [3, 5]]
Output:
5

Explanation:
For L = 4, the first four scores are [5, 1, 7, 3]. There is only one score >= 6, so the first requirement fails.
For L = 5, the prefix is [5, 1, 7, 3, 9]. There are two scores >= 6 (7 and 9), and three scores >= 5 (5, 7, 9), so both requirements are satisfied.
Therefore, the minimum valid query limit is 5.

Example 2:
Input:
scores = [2, 2, 2, 2]
requirements = [[1, 3]]
Output:
-1

Explanation:
No prefix contains any score >= 3, so the requirement can never be satisfied.

Goal:
Return the smallest integer L in the range [1, n] that satisfies every requirement, or -1 if none exists.
*/

public class Solution {

    /**
     * Small helper structure representing one analyst requirement:
     * "Need at least k documents with score >= threshold".
     */
    private static class Requirement {
        int k;
        int threshold;

        Requirement(int k, int threshold) {
            this.k = k;
            this.threshold = threshold;
        }
    }

    /**
     * Small helper structure representing one document score together with its
     * 1-based position in the fixed ranking order.
     */
    private static class ScoreWithIndex {
        int score;
        int index;

        ScoreWithIndex(int score, int index) {
            this.score = score;
            this.index = index;
        }
    }

    /**
     * Segment tree that supports:
     * 1) point update: set a position to 1 when a document becomes "active"
     * 2) query: find the index of the k-th active position
     *
     * In this problem, after sorting documents by score descending, we activate
     * all documents whose score is >= current threshold. Then:
     * - the k-th active position in ranking order tells us the minimum prefix length
     *   needed to include at least k such documents.
     */
    private static class SegmentTree {
        private final int n;
        private final int[] tree;

        SegmentTree(int n) {
            this.n = n;
            this.tree = new int[4 * n];
        }

        /**
         * Marks one position as active by setting its value to 1.
         *
         * @param position 1-based position in the ranking
         */
        void activate(int position) {
            update(1, 1, n, position);
        }

        private void update(int node, int left, int right, int position) {
            if (left == right) {
                tree[node] = 1;
                return;
            }

            int mid = left + (right - left) / 2;
            if (position <= mid) {
                update(node * 2, left, mid, position);
            } else {
                update(node * 2 + 1, mid + 1, right, position);
            }

            tree[node] = tree[node * 2] + tree[node * 2 + 1];
        }

        /**
         * Returns how many active positions currently exist.
         *
         * @return total number of active positions
         */
        int totalActive() {
            return tree[1];
        }

        /**
         * Finds the 1-based ranking position of the k-th active document.
         *
         * Important:
         * - This method assumes 1 <= k <= totalActive().
         *
         * @param k the order statistic among active positions
         * @return the ranking index of the k-th active position
         */
        int kthActivePosition(int k) {
            return kth(1, 1, n, k);
        }

        private int kth(int node, int left, int right, int k) {
            if (left == right) {
                return left;
            }

            int leftCount = tree[node * 2];
            int mid = left + (right - left) / 2;

            if (k <= leftCount) {
                return kth(node * 2, left, mid, k);
            } else {
                return kth(node * 2 + 1, mid + 1, right, k - leftCount);
            }
        }
    }

    /**
     * Computes the minimum query limit L such that all requirements are satisfied.
     *
     * Core idea:
     * - For one requirement [k, t], we need the earliest prefix that contains
     *   at least k scores >= t.
     * - If we can compute that earliest prefix length for every requirement,
     *   then the final answer is simply the maximum among them, because all
     *   requirements must hold simultaneously.
     * - If any requirement is impossible, the answer is -1.
     *
     * Efficient strategy:
     * - Sort documents by score descending.
     * - Sort requirements by threshold descending.
     * - Sweep thresholds from high to low:
     *   activate every document whose score is >= current threshold.
     * - Among active documents, the earliest prefix satisfying requirement [k, t]
     *   is the position of the k-th active document in ranking order.
     * - Use a segment tree to maintain active positions and answer k-th order
     *   statistic queries efficiently.
     *
     * @param scores the relevance scores of documents in fixed ranking order; scores[i] is the score of document at position i + 1
     * @param requirements each element is [k, t], meaning within the first L documents there must be at least k documents with score >= t
     * @return the minimum valid query limit L, or -1 if no such prefix exists
     *
     * Time complexity:
     * O((n + m) log n), because we sort and then perform segment tree updates/queries.
     *
     * Space complexity:
     * O(n + m), for sorted arrays, helper objects, and the segment tree.
     */
    public int minimumQueryLimit(int[] scores, int[][] requirements) {
        int n = scores.length;
        int m = requirements.length;

        // Edge case:
        // If there are no requirements, then technically every prefix works.
        // Since the problem asks for L in [1, n], the minimum would be 1.
        if (m == 0) {
            return 1;
        }

        // Convert scores into objects that remember both:
        // - the score value
        // - the original 1-based ranking position
        ScoreWithIndex[] documents = new ScoreWithIndex[n];
        for (int i = 0; i < n; i++) {
            documents[i] = new ScoreWithIndex(scores[i], i + 1);
        }

        // Sort documents by score descending.
        // During the sweep, when we process threshold t, we activate all documents
        // with score >= t.
        Arrays.sort(documents, (a, b) -> Integer.compare(b.score, a.score));

        // Convert requirements into objects and keep original order if needed.
        Requirement[] reqs = new Requirement[m];
        for (int i = 0; i < m; i++) {
            reqs[i] = new Requirement(requirements[i][0], requirements[i][1]);
        }

        // Sort requirements by threshold descending.
        // This allows a single sweep over documents.
        Arrays.sort(reqs, (a, b) -> Integer.compare(b.threshold, a.threshold));

        SegmentTree segmentTree = new SegmentTree(n);

        int docPointer = 0;
        int answer = 1;

        // Process each requirement in descending threshold order.
        for (Requirement req : reqs) {
            int neededCount = req.k;
            int threshold = req.threshold;

            // Activate every document whose score is >= current threshold.
            // Because both documents and requirements are processed in descending order,
            // each document is activated at most once overall.
            while (docPointer < n && documents[docPointer].score >= threshold) {
                segmentTree.activate(documents[docPointer].index);
                docPointer++;
            }

            // If fewer than k documents in the entire array have score >= threshold,
            // then this requirement can never be satisfied by any prefix.
            if (segmentTree.totalActive() < neededCount) {
                return -1;
            }

            // The earliest prefix that satisfies this requirement is exactly the
            // ranking position of the k-th active document.
            int earliestPrefixForThisRequirement = segmentTree.kthActivePosition(neededCount);

            // Since all requirements must hold simultaneously, the final prefix
            // must be at least as large as the largest individual requirement.
            answer = Math.max(answer, earliestPrefixForThisRequirement);
        }

        return answer;
    }

    /**
     * Alternative implementation that explicitly uses binary search on L and checks
     * feasibility for a given prefix length.
     *
     * This method is included because the problem statement highlights the monotonic
     * nature of the answer. However, this direct binary-search-check approach is not
     * the most efficient one for the largest constraints if implemented naively.
     *
     * For educational purposes, this method:
     * - binary searches the answer
     * - uses an offline threshold sweep inside the checker
     *
     * It is still significantly more complex than the direct "maximum earliest prefix"
     * solution above, so the main recommended method is minimumQueryLimit().
     *
     * @param scores the relevance scores of documents in fixed ranking order
     * @param requirements each element is [k, t]
     * @return the minimum valid query limit L, or -1 if impossible
     *
     * Time complexity:
     * Higher than minimumQueryLimit(); not the preferred method for this problem.
     *
     * Space complexity:
     * O(n + m)
     */
    public int minimumQueryLimitBinarySearchStyle(int[] scores, int[][] requirements) {
        int n = scores.length;

        if (requirements.length == 0) {
            return 1;
        }

        if (!canSatisfy(scores, requirements, n)) {
            return -1;
        }

        int left = 1;
        int right = n;
        int answer = n;

        while (left <= right) {
            int mid = left + (right - left) / 2;

            if (canSatisfy(scores, requirements, mid)) {
                answer = mid;
                right = mid - 1;
            } else {
                left = mid + 1;
            }
        }

        return answer;
    }

    /**
     * Checks whether the prefix of length L satisfies all requirements.
     *
     * This helper is used by the educational binary-search-style method.
     * It works by:
     * - taking the first L scores
     * - sorting them descending
     * - sorting requirements by threshold descending
     * - counting how many prefix scores are >= each threshold
     *
     * @param scores the full score array
     * @param requirements the analyst requirements
     * @param L the prefix length being tested
     * @return true if the first L documents satisfy every requirement; false otherwise
     *
     * Time complexity:
     * O(L log L + m log m) per call.
     *
     * Space complexity:
     * O(L + m)
     */
    public boolean canSatisfy(int[] scores, int[][] requirements, int L) {
        int[] prefix = Arrays.copyOf(scores, L);
        Arrays.sort(prefix);

        int[][] reqCopy = new int[requirements.length][2];
        for (int i = 0; i < requirements.length; i++) {
            reqCopy[i][0] = requirements[i][0];
            reqCopy[i][1] = requirements[i][1];
        }

        Arrays.sort(reqCopy, (a, b) -> Integer.compare(b[1], a[1]));

        int pointer = L - 1;
        int countAtLeastThreshold = 0;

        for (int[] req : reqCopy) {
            int needed = req[0];
            int threshold = req[1];

            while (pointer >= 0 && prefix[pointer] >= threshold) {
                countAtLeastThreshold++;
                pointer--;
            }

            if (countAtLeastThreshold < needed) {
                return false;
            }
        }

        return true;
    }

    /**
     * Demonstrates the solution on the sample inputs from the problem statement.
     *
     * Expected outputs:
     * Example 1 -> 5
     * Example 2 -> -1
     *
     * @param args command-line arguments; not used
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        int[] scores1 = {5, 1, 7, 3, 9, 6};
        int[][] requirements1 = {
            {2, 6},
            {3, 5}
        };
        int result1 = solution.minimumQueryLimit(scores1, requirements1);
        System.out.println(result1); // Expected: 5

        int[] scores2 = {2, 2, 2, 2};
        int[][] requirements2 = {
            {1, 3}
        };
        int result2 = solution.minimumQueryLimit(scores2, requirements2);
        System.out.println(result2); // Expected: -1

        // Optional extra demonstration using the binary-search-style educational method.
        System.out.println(solution.minimumQueryLimitBinarySearchStyle(scores1, requirements1)); // Expected: 5
        System.out.println(solution.minimumQueryLimitBinarySearchStyle(scores2, requirements2)); // Expected: -1
    }
}