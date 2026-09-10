import java.util.*;

/*
 * Maximum Feasible Toll Pass Duration
 *
 * Problem Description:
 * A logistics company operates on a straight highway with toll gates placed at strictly increasing
 * mile markers. A driver starts at mile 0 and must finish at mile L. The company wants to sell
 * a reusable toll pass that lasts for exactly D miles: once activated, it covers every toll gate
 * whose mile marker lies in the interval [x, x + D] for some chosen activation point x.
 * The driver may buy at most K such passes during the trip, and passes may overlap.
 *
 * You are given an array gates where gates[i] is the mile marker of the i-th toll gate, sorted in
 * strictly increasing order, along with integers L and K. Determine the maximum integer duration D
 * such that it is possible to choose at most K activation intervals of length D and cover every toll
 * gate on the route. The activation points do not need to be toll locations; they may be any real
 * values, but the answer D must be an integer.
 *
 * Your task is to return the largest feasible D.
 *
 * A solution faster than checking every possible D is expected. In particular, the feasibility of a
 * fixed D is monotonic, so an efficient binary-search-based approach combined with a greedy coverage
 * check should be considered.
 *
 * Constraints:
 * - 1 <= gates.length <= 2 * 10^5
 * - 1 <= K <= gates.length
 * - 1 <= gates[i] <= L <= 10^18
 * - gates is sorted in strictly increasing order
 * - The answer is an integer in the range [0, L]
 *
 * Important note about correctness:
 * The natural monotonic interpretation of this problem is:
 * "Can all gate positions be covered by at most K intervals, each of length D?"
 * Under that interpretation, if a duration D is feasible, then any larger duration is also feasible.
 * Therefore the largest feasible D in [0, L] is always L, because one interval of length L can cover
 * every gate on the route and K >= 1.
 *
 * Since the prompt explicitly asks for a binary-search-based solution and also asks to verify examples,
 * the examples in the statement are inconsistent with the stated rules. This implementation follows the
 * actual stated rules exactly and therefore returns the mathematically correct answer for those rules.
 */

public class Solution {

    /**
     * Returns the largest feasible integer duration D.
     *
     * Under the exact problem statement, feasibility means:
     * all toll gates can be covered by at most K intervals of length D.
     *
     * Because intervals may start at any real position, and because K >= 1, choosing one interval
     * [0, L] of length L covers every gate whose position is between 0 and L inclusive. Since every
     * gate satisfies 1 <= gates[i] <= L, duration L is always feasible. Also D cannot exceed L by the
     * problem's answer range. Therefore the maximum feasible duration is always exactly L.
     *
     * We still provide a binary-search-based implementation to match the requested topic and to
     * demonstrate the monotonic feasibility structure.
     *
     * @param gates sorted array of strictly increasing toll gate mile markers
     * @param L total route length
     * @param K maximum number of passes allowed
     * @return the largest feasible integer duration D
     * Time complexity: O(n log L), where n = gates.length
     * Space complexity: O(1) extra space
     */
    public long maximumFeasibleDuration(long[] gates, long L, int K) {
        long low = 0;
        long high = L;
        long answer = 0;

        while (low <= high) {
            long mid = low + ((high - low) >>> 1);

            if (isFeasible(gates, mid, K)) {
                answer = mid;
                low = mid + 1;
            } else {
                high = mid - 1;
            }
        }

        return answer;
    }

    /**
     * Checks whether all gates can be covered using at most K intervals of length D.
     *
     * Greedy idea:
     * - Start from the first uncovered gate.
     * - Place an interval beginning exactly at that gate position.
     *   This is optimal for covering as many subsequent gates as possible, because shifting the
     *   interval left would not help cover more gates to the right, and shifting it right would fail
     *   to cover the current gate.
     * - That interval covers every gate with position <= start + D.
     * - Repeat until all gates are covered or we exceed K intervals.
     *
     * This greedy check is standard and correct for covering sorted points with a minimum number of
     * fixed-length intervals.
     *
     * @param gates sorted array of strictly increasing toll gate mile markers
     * @param D candidate pass duration
     * @param K maximum number of passes allowed
     * @return true if all gates can be covered by at most K intervals of length D; false otherwise
     * Time complexity: O(n), where n = gates.length
     * Space complexity: O(1) extra space
     */
    public boolean isFeasible(long[] gates, long D, int K) {
        int usedPasses = 0;
        int i = 0;
        int n = gates.length;

        while (i < n) {
            usedPasses++;

            if (usedPasses > K) {
                return false;
            }

            long start = gates[i];
            long coveredUntil = start + D;

            i++;

            while (i < n && gates[i] <= coveredUntil) {
                i++;
            }
        }

        return true;
    }

    /**
     * Convenience overload for int[] input.
     *
     * @param gates sorted array of strictly increasing toll gate mile markers
     * @param L total route length
     * @param K maximum number of passes allowed
     * @return the largest feasible integer duration D
     * Time complexity: O(n log L)
     * Space complexity: O(n) due to conversion to long[]
     */
    public long maximumFeasibleDuration(int[] gates, long L, int K) {
        long[] converted = new long[gates.length];
        for (int i = 0; i < gates.length; i++) {
            converted[i] = gates[i];
        }
        return maximumFeasibleDuration(converted, L, K);
    }

    /**
     * Demonstrates the solution on sample-style inputs.
     *
     * Note:
     * The outputs printed here follow the exact mathematical interpretation of the written problem
     * statement, not the inconsistent sample outputs in the prompt.
     *
     * @param args command-line arguments (unused)
     * @return nothing
     * Time complexity: O(log L + n log L) across the demonstration calls
     * Space complexity: O(1) extra space excluding input arrays
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        int[] gates1 = {2, 5, 6, 11, 14};
        long L1 = 20;
        int K1 = 2;
        System.out.println(solution.maximumFeasibleDuration(gates1, L1, K1));

        int[] gates2 = {1, 4, 8, 9, 15};
        long L2 = 20;
        int K2 = 3;
        System.out.println(solution.maximumFeasibleDuration(gates2, L2, K2));

        int[] gates3 = {3};
        long L3 = 10;
        int K3 = 1;
        System.out.println(solution.maximumFeasibleDuration(gates3, L3, K3));
    }
}