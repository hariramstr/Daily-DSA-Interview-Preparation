import java.util.*;

/*
 * Title: Maximum Starter Batch for Subscription Trials
 * Difficulty: Medium
 * Topic: Binary Search
 *
 * Problem Description:
 * A product team is preparing trial kits for a new subscription launch. There are n warehouses,
 * and the i-th warehouse can provide kits[i] starter kits. Every customer trial batch must contain
 * exactly the same number of kits, and a single batch can only be assembled using kits from one warehouse.
 * However, one warehouse may be split into multiple batches as long as the total number of kits used
 * from that warehouse does not exceed its inventory.
 *
 * Given an integer array kits where kits[i] is the number of available starter kits in warehouse i,
 * and an integer m representing the number of customer batches that must be created, return the maximum
 * possible number of kits in each batch.
 *
 * If it is impossible to create m non-empty batches, return 0.
 *
 * This problem should be solved efficiently for large inputs. A brute-force search over all possible
 * batch sizes will be too slow. Think about how the answer changes as the candidate batch size increases,
 * and use that monotonic behavior to design a binary search solution.
 *
 * Constraints:
 * - 1 <= kits.length <= 100000
 * - 1 <= kits[i] <= 1000000000
 * - 1 <= m <= 1000000000000
 * - The answer fits in a 32-bit signed integer
 *
 * Example 1:
 * Input: kits = [9, 7, 5], m = 5
 * Output: 3
 * Explanation:
 * Using batch size 3, the warehouses can produce:
 * 9 / 3 = 3 batches
 * 7 / 3 = 2 batches
 * 5 / 3 = 1 batch
 * Total = 6 batches, which is enough.
 *
 * With batch size 4:
 * 9 / 4 = 2 batches
 * 7 / 4 = 1 batch
 * 5 / 4 = 1 batch
 * Total = 4 batches, which is not enough.
 * So the maximum valid size is 3.
 *
 * Example 2:
 * Input: kits = [2, 4, 6], m = 7
 * Output: 1
 * Explanation:
 * With batch size 1, total batches = 2 + 4 + 6 = 12, enough.
 * With batch size 2, total batches = 1 + 2 + 3 = 6, not enough.
 * Therefore, the largest possible batch size is 1.
 */

public class Solution {

    /**
     * Computes the maximum possible number of kits in each batch such that at least {@code m}
     * equal-sized non-empty batches can be formed.
     *
     * The key observation is monotonicity:
     * - If a batch size x is possible, then every smaller positive batch size is also possible.
     * - If a batch size x is not possible, then every larger batch size is also not possible.
     *
     * This monotonic behavior allows us to binary search on the answer.
     *
     * @param kits array where kits[i] is the number of starter kits available in warehouse i
     * @param m the required number of customer batches
     * @return the maximum valid batch size; returns 0 if it is impossible to create m non-empty batches
     * Time complexity: O(n log M), where n is kits.length and M is the maximum possible batch size
     * Space complexity: O(1), excluding input storage
     */
    public int maximumBatchSize(int[] kits, long m) {
        // ------------------------------------------------------------
        // Step 1: Compute the total number of kits across all warehouses.
        // ------------------------------------------------------------
        // Why do we do this?
        // If the total number of kits is less than m, then even making batches of size 1
        // would not be enough to create m non-empty batches.
        // In that case, the answer must be 0 immediately.
        long totalKits = 0L;

        // Also track the maximum warehouse inventory.
        // This gives a natural upper bound for the answer because a single batch cannot
        // be larger than the largest warehouse inventory.
        int maxKitsInSingleWarehouse = 0;

        for (int kitCount : kits) {
            totalKits += kitCount;
            if (kitCount > maxKitsInSingleWarehouse) {
                maxKitsInSingleWarehouse = kitCount;
            }
        }

        // If we do not even have enough total kits to make m batches of size 1,
        // then creating m non-empty batches is impossible.
        if (totalKits < m) {
            return 0;
        }

        // ------------------------------------------------------------
        // Step 2: Set up binary search boundaries.
        // ------------------------------------------------------------
        // Lowest possible valid batch size is 1.
        // Highest possible valid batch size is maxKitsInSingleWarehouse.
        //
        // We will search for the largest size that is feasible.
        int left = 1;
        int right = maxKitsInSingleWarehouse;
        int answer = 0;

        // ------------------------------------------------------------
        // Step 3: Standard binary search on the answer space.
        // ------------------------------------------------------------
        // Invariant:
        // - Every feasible size can potentially improve the answer.
        // - We keep searching to the right after finding a feasible size,
        //   because we want the maximum possible size.
        while (left <= right) {
            // Use this form to avoid overflow:
            int mid = left + (right - left) / 2;

            // Check whether we can form at least m batches of size mid.
            if (canMakeAtLeastMBatches(kits, m, mid)) {
                // mid is feasible, so it is a candidate answer.
                answer = mid;

                // Since we want the maximum feasible size, search larger values.
                left = mid + 1;
            } else {
                // mid is not feasible, so any larger value is also not feasible.
                // Search smaller values.
                right = mid - 1;
            }
        }

        // After binary search finishes, answer stores the largest feasible batch size.
        return answer;
    }

    /**
     * Checks whether it is possible to create at least {@code m} batches, each of size {@code batchSize}.
     *
     * For each warehouse with kits[i] kits, the number of full batches it can contribute is:
     * kits[i] / batchSize
     *
     * We sum these values across all warehouses and compare the total to m.
     *
     * An important optimization:
     * As soon as the running total reaches or exceeds m, we can return true early.
     * This avoids unnecessary work and also helps prevent large intermediate sums from growing more than needed.
     *
     * @param kits array of warehouse inventories
     * @param m required number of batches
     * @param batchSize candidate size for each batch
     * @return true if at least m batches can be formed, otherwise false
     * Time complexity: O(n), where n is kits.length
     * Space complexity: O(1)
     */
    public boolean canMakeAtLeastMBatches(int[] kits, long m, int batchSize) {
        // Running count of how many batches can be formed in total.
        long batches = 0L;

        // Visit each warehouse independently.
        for (int kitCount : kits) {
            // Integer division tells us how many full batches of size batchSize
            // this warehouse can provide.
            batches += kitCount / batchSize;

            // Early exit:
            // Once we already have enough batches, there is no need to continue.
            if (batches >= m) {
                return true;
            }
        }

        // If we finish the loop and still have fewer than m batches, it is not feasible.
        return false;
    }

    /**
     * Demonstrates the solution using the sample inputs from the problem statement.
     *
     * This method prints:
     * - the input arrays
     * - the required number of batches
     * - the computed maximum batch size
     *
     * It also includes a couple of extra quick demonstrations for clarity.
     *
     * @param args command-line arguments (not used)
     * @return nothing
     * Time complexity: O(total work of the demonstrated calls)
     * Space complexity: O(1), excluding input arrays used for demonstration
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // ------------------------------------------------------------
        // Sample Example 1
        // kits = [9, 7, 5], m = 5
        // Expected output: 3
        // Reason:
        // size 3 => 9/3 + 7/3 + 5/3 = 3 + 2 + 1 = 6 >= 5
        // size 4 => 9/4 + 7/4 + 5/4 = 2 + 1 + 1 = 4 < 5
        // Therefore answer = 3
        // ------------------------------------------------------------
        int[] kits1 = {9, 7, 5};
        long m1 = 5L;
        int result1 = solution.maximumBatchSize(kits1, m1);
        System.out.println("Example 1:");
        System.out.println("kits = " + Arrays.toString(kits1) + ", m = " + m1);
        System.out.println("Maximum batch size = " + result1);
        System.out.println("Expected = 3");
        System.out.println();

        // ------------------------------------------------------------
        // Sample Example 2
        // kits = [2, 4, 6], m = 7
        // Expected output: 1
        // Reason:
        // size 1 => 2 + 4 + 6 = 12 >= 7
        // size 2 => 1 + 2 + 3 = 6 < 7
        // Therefore answer = 1
        // ------------------------------------------------------------
        int[] kits2 = {2, 4, 6};
        long m2 = 7L;
        int result2 = solution.maximumBatchSize(kits2, m2);
        System.out.println("Example 2:");
        System.out.println("kits = " + Arrays.toString(kits2) + ", m = " + m2);
        System.out.println("Maximum batch size = " + result2);
        System.out.println("Expected = 1");
        System.out.println();

        // ------------------------------------------------------------
        // Extra demonstration:
        // Impossible case
        // total kits = 1 + 1 + 1 = 3, but m = 5
        // Even size 1 cannot make 5 batches, so answer = 0
        // ------------------------------------------------------------
        int[] kits3 = {1, 1, 1};
        long m3 = 5L;
        int result3 = solution.maximumBatchSize(kits3, m3);
        System.out.println("Extra Example 3:");
        System.out.println("kits = " + Arrays.toString(kits3) + ", m = " + m3);
        System.out.println("Maximum batch size = " + result3);
        System.out.println("Expected = 0");
        System.out.println();

        // ------------------------------------------------------------
        // Extra demonstration:
        // kits = [8, 8], m = 4
        // size 4 => 2 + 2 = 4, feasible
        // size 5 => 1 + 1 = 2, not feasible
        // answer = 4
        // ------------------------------------------------------------
        int[] kits4 = {8, 8};
        long m4 = 4L;
        int result4 = solution.maximumBatchSize(kits4, m4);
        System.out.println("Extra Example 4:");
        System.out.println("kits = " + Arrays.toString(kits4) + ", m = " + m4);
        System.out.println("Maximum batch size = " + result4);
        System.out.println("Expected = 4");
    }
}