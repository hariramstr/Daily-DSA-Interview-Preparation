import java.util.*;

/*
 * Title: Minimum Heater Radius for Circular Warehouses
 * Difficulty: Medium
 * Topic: Binary Search
 *
 * Problem Description:
 * A logistics company stores goods in warehouses placed around a circular ring road of total length L.
 * The positions of the warehouses are given as integers in the range [0, L - 1], measured clockwise
 * from a fixed origin. The company wants to install heaters at some existing warehouse locations.
 * Each heater warms all warehouses within clockwise or counterclockwise road distance at most R,
 * where distance on the ring is the shorter of the two circular paths.
 *
 * You are given a sorted array warehouses of unique warehouse positions, an integer L, and an integer k
 * representing the maximum number of heaters that may be installed. Return the minimum integer radius R
 * such that all warehouses can be covered by at most k heaters.
 *
 * A heater may only be placed at one of the given warehouse positions. Coverage wraps around the circle,
 * so a heater near position 0 may also cover warehouses near position L - 1.
 *
 * Constraints:
 * - 1 <= k <= n <= 2 * 10^5
 * - 1 <= L <= 10^9
 * - 0 <= warehouses[i] < L
 * - warehouses is sorted in strictly increasing order
 * - All answers fit in a 32-bit signed integer
 *
 * Example 1:
 * Input: warehouses = [1, 4, 8, 11], L = 12, k = 2
 * Output: 2
 *
 * Example 2:
 * Input: warehouses = [2, 6, 9, 14], L = 20, k = 1
 * Output: 6
 */

public class Solution {

    /**
     * Computes the minimum integer heater radius needed to cover all warehouses on a circle
     * using at most k heaters, where each heater must be placed at an existing warehouse.
     *
     * Core idea:
     * 1. Binary search the answer R.
     * 2. For a fixed R, check whether coverage is possible with at most k heaters.
     * 3. The feasibility check transforms the circular problem into a linear doubled-array problem.
     *
     * @param warehouses sorted array of unique warehouse positions on the circle
     * @param L total length of the circular road
     * @param k maximum number of heaters allowed
     * @return the minimum integer radius R that allows covering all warehouses
     *
     * Time complexity: O(n log L + n log n log k) in practice dominated by O(n log L log k)
     * because each feasibility check is near-linear with binary lifting.
     * Space complexity: O(n log k) for jump tables and helper arrays.
     */
    public int minimumHeaterRadius(int[] warehouses, int L, int k) {
        int n = warehouses.length;

        if (n == 0) {
            return 0;
        }

        if (k >= n) {
            return 0;
        }

        int low = 0;
        int high = L / 2;

        while (low < high) {
            int mid = low + (high - low) / 2;
            if (canCoverAll(warehouses, L, k, mid)) {
                high = mid;
            } else {
                low = mid + 1;
            }
        }

        return low;
    }

    /**
     * Checks whether all warehouses can be covered using at most k heaters for a fixed radius R.
     *
     * Detailed strategy:
     * - On a circle, choosing a "start" point is tricky because coverage may wrap around.
     * - To handle this cleanly, duplicate the warehouse positions by appending each position + L.
     *   This creates a linear view of two consecutive copies of the circle.
     * - For each possible starting warehouse i in the first copy, we want to know whether the interval
     *   of n consecutive warehouses [i, i + n - 1] can be covered by at most k heaters.
     * - For the linear version, if we start covering from warehouse i, the optimal greedy move is:
     *   place a heater at the farthest warehouse position <= warehouses[i] + R.
     *   That heater then covers up to position heaterPosition + R, so all warehouses with position
     *   <= warehouses[i] + 2R become covered.
     * - This greedy step defines a "next uncovered index" jump from i to next[i].
     * - Then we ask whether after at most k such jumps we move beyond i + n - 1.
     * - We answer this efficiently for all starts using binary lifting.
     *
     * @param warehouses sorted array of unique warehouse positions on the circle
     * @param L total length of the circular road
     * @param k maximum number of heaters allowed
     * @param R candidate heater radius
     * @return true if all warehouses can be covered with radius R using at most k heaters; false otherwise
     *
     * Time complexity: O(n log n + n log k)
     * Space complexity: O(n log k)
     */
    public boolean canCoverAll(int[] warehouses, int L, int k, int R) {
        int n = warehouses.length;
        int m = 2 * n;

        long[] extended = new long[m];

        for (int i = 0; i < n; i++) {
            extended[i] = warehouses[i];
            extended[i + n] = (long) warehouses[i] + L;
        }

        int[] next = buildNextArray(extended, R);

        int maxPow = 1;
        while ((1 << maxPow) <= k) {
            maxPow++;
        }

        int[][] jump = buildJumpTable(next, maxPow);

        for (int start = 0; start < n; start++) {
            int endExclusiveTarget = start + n;
            int position = advanceByKHeaters(start, k, jump);

            if (position >= endExclusiveTarget) {
                return true;
            }
        }

        return false;
    }

    /**
     * Builds the greedy "next uncovered index" array for the doubled linear warehouse positions.
     *
     * For each index i:
     * 1. The first uncovered warehouse is at extended[i].
     * 2. To maximize coverage with one heater, place it at the farthest warehouse whose position
     *    is <= extended[i] + R. This is the best legal heater location that still covers extended[i].
     * 3. If that heater is placed at position p, then it covers all warehouses with position <= p + R.
     * 4. Therefore next[i] is the first index whose position is > p + R.
     *
     * We compute this with two moving pointers, so the whole array is built in linear time.
     *
     * @param extended doubled warehouse positions array of length 2n
     * @param R candidate heater radius
     * @return next array where next[i] is the first uncovered index after one optimal heater from i
     *
     * Time complexity: O(n)
     * Space complexity: O(n)
     */
    public int[] buildNextArray(long[] extended, int R) {
        int m = extended.length;
        int[] next = new int[m + 1];

        int heaterIndexPointer = 0;
        int coveredIndexPointer = 0;

        for (int i = 0; i < m; i++) {
            if (heaterIndexPointer < i) {
                heaterIndexPointer = i;
            }

            long maxHeaterPosition = extended[i] + R;

            while (heaterIndexPointer + 1 < m && extended[heaterIndexPointer + 1] <= maxHeaterPosition) {
                heaterIndexPointer++;
            }

            if (coveredIndexPointer < heaterIndexPointer) {
                coveredIndexPointer = heaterIndexPointer;
            }

            long maxCoveredPosition = extended[heaterIndexPointer] + R;

            while (coveredIndexPointer + 1 < m && extended[coveredIndexPointer + 1] <= maxCoveredPosition) {
                coveredIndexPointer++;
            }

            next[i] = coveredIndexPointer + 1;
        }

        next[m] = m;
        return next;
    }

    /**
     * Builds a binary lifting jump table.
     *
     * jump[p][i] means:
     * after using exactly 2^p heaters starting from first uncovered index i,
     * we end at jump[p][i], which is the next uncovered index after those heaters.
     *
     * This allows us to simulate k greedy heater placements in O(log k) time.
     *
     * @param next base jump array for one heater
     * @param maxPow number of binary lifting levels
     * @return binary lifting table
     *
     * Time complexity: O(n log k)
     * Space complexity: O(n log k)
     */
    public int[][] buildJumpTable(int[] next, int maxPow) {
        int mPlusOne = next.length;
        int[][] jump = new int[maxPow][mPlusOne];

        for (int i = 0; i < mPlusOne; i++) {
            jump[0][i] = next[i];
        }

        for (int p = 1; p < maxPow; p++) {
            for (int i = 0; i < mPlusOne; i++) {
                jump[p][i] = jump[p - 1][jump[p - 1][i]];
            }
        }

        return jump;
    }

    /**
     * Advances from a starting index using exactly k greedy heater placements,
     * using the binary lifting jump table.
     *
     * @param start starting first-uncovered warehouse index
     * @param k number of heaters to use
     * @param jump binary lifting table
     * @return the next uncovered index after using k heaters
     *
     * Time complexity: O(log k)
     * Space complexity: O(1) extra space beyond the jump table
     */
    public int advanceByKHeaters(int start, int k, int[][] jump) {
        int position = start;
        int bit = 0;
        int heaters = k;

        while (heaters > 0) {
            if ((heaters & 1) != 0) {
                position = jump[bit][position];
            }
            heaters >>= 1;
            bit++;
        }

        return position;
    }

    /**
     * Demonstrates the solution on the sample inputs from the problem statement.
     *
     * @param args command-line arguments, unused
     *
     * Time complexity: O(1) for the fixed demonstrations, excluding the algorithm calls
     * Space complexity: O(1) extra space, excluding the algorithm calls
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        int[] warehouses1 = {1, 4, 8, 11};
        int L1 = 12;
        int k1 = 2;
        int result1 = solution.minimumHeaterRadius(warehouses1, L1, k1);
        System.out.println("Example 1 result: " + result1);

        int[] warehouses2 = {2, 6, 9, 14};
        int L2 = 20;
        int k2 = 1;
        int result2 = solution.minimumHeaterRadius(warehouses2, L2, k2);
        System.out.println("Example 2 result: " + result2);
    }
}