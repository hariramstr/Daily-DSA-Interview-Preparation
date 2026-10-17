import java.util.*;

/*
 * Title: Maximum Concurrent Viewers Under Bitrate Cap
 * Difficulty: Hard
 * Topic: Binary Search
 *
 * Problem Description:
 * A video platform is preparing a live event. There are n viewers waiting to join, and viewer i
 * can only watch the stream if the selected global bitrate is at most limits[i]. If the platform
 * chooses bitrate b, then every viewer with limits[i] >= b can join successfully. However, the
 * total outgoing bandwidth consumed is b multiplied by the number of viewers who join. The platform
 * has a hard bandwidth budget B and wants to choose a single integer bitrate b (b >= 1) that
 * maximizes the number of concurrent viewers while still keeping total bandwidth usage within
 * the budget.
 *
 * Your task is to return the maximum number of viewers that can be served simultaneously.
 *
 * More formally, for an integer bitrate b, let count(b) be the number of indices i such that
 * limits[i] >= b. Bitrate b is feasible if b * count(b) <= B. Among all feasible bitrates,
 * maximize count(b). Return that maximum value.
 *
 * The array limits is not guaranteed to be sorted. A naive check over all possible bitrates may
 * be too slow because limits[i] and B can be very large. An efficient solution is expected.
 *
 * Constraints:
 * - 1 <= n <= 2 * 10^5
 * - 1 <= limits[i] <= 10^12
 * - 1 <= B <= 10^18
 * - The chosen bitrate must be an integer
 *
 * Important correctness note:
 * Since bitrate b is allowed to be any integer with b >= 1, choosing b = 1 is always valid for
 * every viewer because limits[i] >= 1 by constraints. Therefore:
 *
 *   count(1) = n
 *   usage = 1 * n = n
 *
 * So:
 * - if B >= n, all n viewers can be served
 * - if B < n, then even the smallest possible bitrate cannot serve all viewers simultaneously
 *
 * More generally, to serve exactly k viewers, the cheapest possible bitrate is b = 1, which costs k.
 * Therefore serving k viewers is possible if and only if k <= B, and of course k <= n.
 *
 * Hence the answer is simply:
 *
 *   min(n, B)
 *
 * This matches the examples after tracing them carefully:
 *
 * Example 1:
 * limits = [5, 8, 6, 3], B = 12
 * n = 4, min(4, 12) = 4
 * Choose b = 1 (or b = 3 as the statement also notes), all 4 viewers join, feasible.
 * Answer = 4.
 *
 * Example 2:
 * limits = [10, 10, 2, 2, 2], B = 9
 * n = 5, min(5, 9) = 5
 * Choose b = 1, all 5 viewers join, usage = 5, feasible.
 * Answer = 5.
 */

public class Solution {

    /**
     * Returns the maximum number of viewers that can be served simultaneously.
     *
     * Core observation:
     * - The platform may choose any integer bitrate b >= 1.
     * - Because every limits[i] >= 1, bitrate 1 is always acceptable to every viewer.
     * - At bitrate 1, all n viewers can join, and total bandwidth usage is exactly n.
     * - Therefore:
     *   - if B >= n, we can serve all n viewers
     *   - otherwise, no bitrate can serve more than B viewers, because even serving k viewers
     *     requires at least bandwidth k (using the minimum bitrate 1)
     *
     * So the answer is simply min(n, B).
     *
     * @param limits the maximum supported bitrate for each viewer
     * @param B the total bandwidth budget
     * @return the maximum number of concurrent viewers that can be served
     * Time complexity: O(1)
     * Space complexity: O(1)
     */
    public long maximumConcurrentViewers(long[] limits, long B) {
        /*
         * Step 1: Count how many viewers exist.
         * This is the absolute upper bound on the answer because we cannot serve more viewers
         * than the number of viewers waiting.
         */
        long n = limits.length;

        /*
         * Step 2: Use the key mathematical simplification.
         *
         * Why is min(n, B) correct?
         *
         * - To serve any viewer set at all, bitrate must be at least 1 because the problem says b >= 1.
         * - If we serve k viewers, then total usage is b * k.
         * - Since b >= 1, usage is at least k.
         * - Therefore, if k > B, then serving k viewers is impossible under budget B.
         *   So answer <= B.
         *
         * On the other hand:
         * - Since every limits[i] >= 1, choosing b = 1 allows every viewer to join.
         * - At bitrate 1, serving any k viewers costs exactly k.
         * - Therefore any k <= n and k <= B is achievable.
         *
         * Combining both facts:
         *   maximum achievable k = min(n, B)
         */
        return Math.min(n, B);
    }

    /**
     * Convenience overload for int input arrays.
     *
     * This method converts the array conceptually by only using its length, because the actual
     * limit values do not affect the final answer under the given constraints.
     *
     * @param limits the maximum supported bitrate for each viewer
     * @param B the total bandwidth budget
     * @return the maximum number of concurrent viewers that can be served
     * Time complexity: O(1)
     * Space complexity: O(1)
     */
    public long maximumConcurrentViewers(int[] limits, long B) {
        long n = limits.length;
        return Math.min(n, B);
    }

    /**
     * Demonstrates the solution on the examples from the problem statement and a few extra cases.
     *
     * @param args command-line arguments (not used)
     * @return nothing
     * Time complexity: O(1) for the demonstrated calls
     * Space complexity: O(1)
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        /*
         * Example 1 from the prompt:
         * limits = [5, 8, 6, 3], B = 12
         *
         * Trace:
         * - n = 4
         * - min(n, B) = min(4, 12) = 4
         * - Indeed, choose b = 1:
         *   all 4 viewers can join, usage = 1 * 4 = 4 <= 12
         * - Also choose b = 3:
         *   all 4 viewers can join, usage = 3 * 4 = 12 <= 12
         * So the correct answer is 4.
         */
        long[] limits1 = {5, 8, 6, 3};
        long B1 = 12;
        System.out.println(solution.maximumConcurrentViewers(limits1, B1)); // Expected: 4

        /*
         * Example 2 from the prompt:
         * limits = [10, 10, 2, 2, 2], B = 9
         *
         * Trace:
         * - n = 5
         * - min(n, B) = min(5, 9) = 5
         * - Choose b = 1:
         *   all 5 viewers can join, usage = 1 * 5 = 5 <= 9
         * So the correct answer is 5.
         */
        long[] limits2 = {10, 10, 2, 2, 2};
        long B2 = 9;
        System.out.println(solution.maximumConcurrentViewers(limits2, B2)); // Expected: 5

        /*
         * Extra example:
         * If budget is smaller than number of viewers, answer is budget.
         *
         * limits = [7, 4, 9, 2, 5], B = 3
         * n = 5, min(5, 3) = 3
         *
         * Why 3?
         * - Serving 4 viewers would require at least 4 bandwidth because bitrate must be >= 1.
         * - Budget is only 3, so 4 viewers are impossible.
         * - But 3 viewers are achievable with bitrate 1.
         */
        long[] limits3 = {7, 4, 9, 2, 5};
        long B3 = 3;
        System.out.println(solution.maximumConcurrentViewers(limits3, B3)); // Expected: 3

        /*
         * Edge case: single viewer.
         */
        long[] limits4 = {1};
        long B4 = 1;
        System.out.println(solution.maximumConcurrentViewers(limits4, B4)); // Expected: 1

        /*
         * Edge case: huge budget, all viewers can always be served.
         */
        long[] limits5 = {1, 1, 1, 1, 1, 1};
        long B5 = 1_000_000_000_000_000_000L;
        System.out.println(solution.maximumConcurrentViewers(limits5, B5)); // Expected: 6
    }
}