import java.util.*;

/*
 * Title: Maximum Feasible Backup Snapshot Size
 * Difficulty: Hard
 * Topic: Binary Search
 *
 * Problem Description:
 * A company stores daily database backups in a fixed order. The i-th backup has size backups[i] gigabytes.
 * To reduce restore complexity, the operations team wants to group the backups into exactly k contiguous
 * restore bundles. Every backup must belong to exactly one bundle, and bundles must preserve the original order.
 *
 * For a chosen snapshot size limit S, a bundle is considered valid only if the total size of backups inside
 * that bundle is at least S. Since large bundles are harder to manage, the team wants to know the largest
 * snapshot size limit S such that it is still possible to partition the array into exactly k contiguous valid bundles.
 *
 * Return the maximum possible value of S.
 *
 * In other words, split the array into exactly k non-empty contiguous parts, maximize the minimum part sum,
 * and return that optimal minimum sum.
 *
 * This problem is intended to be solved efficiently for large inputs. A brute-force search over all partitions
 * will time out. You should exploit the monotonic nature of feasibility: if a value S is achievable, then every
 * smaller value is also achievable.
 *
 * Constraints:
 * - 1 <= k <= backups.length <= 200000
 * - 1 <= backups[i] <= 1000000000
 * - The answer fits in a 64-bit signed integer
 *
 * Example 1:
 * Input: backups = [7,2,5,10,8], k = 2
 * Output: 14
 * Explanation: One optimal partition is [7,2,5] and [10,8], whose sums are 14 and 18.
 * The minimum bundle sum is 14. It is impossible to make both bundle sums at least 15.
 *
 * Example 2:
 * Input: backups = [4,4,4,4,4,4,4], k = 3
 * Output: 8
 * Explanation: We can partition as [4,4], [4,4], [4,4,4], giving bundle sums 8, 8, and 12.
 * So 8 is feasible. A limit of 9 is not feasible because at least one of the three contiguous bundles
 * would have sum less than 9.
 */

public class Solution {

    /**
     * Computes the maximum possible minimum bundle sum when splitting the array into exactly k
     * non-empty contiguous parts.
     *
     * Core idea:
     * 1. Binary search the answer S.
     * 2. For a candidate S, check whether we can form at least k contiguous groups such that each group
     *    has sum >= S.
     * 3. If we can form at least k such groups, then S is feasible.
     *    Why "at least k" is enough:
     *    - All numbers are positive.
     *    - If we can greedily cut the array into more than k valid groups, we can always merge adjacent groups
     *      until exactly k groups remain.
     *    - Merging only increases group sums, so every merged group still has sum >= S.
     *
     * @param backups the array of backup sizes; each value is positive
     * @param k the exact number of contiguous bundles required
     * @return the largest value S such that the array can be partitioned into exactly k contiguous non-empty parts,
     *         each having sum at least S
     * Time complexity: O(n log(sum(backups)))
     * Space complexity: O(1)
     */
    public long maximumFeasibleSnapshotSize(int[] backups, int k) {
        long totalSum = 0L;

        // Compute total sum.
        // This gives us a safe upper bound for the answer:
        // if we need exactly k groups, then the minimum group sum can never exceed totalSum / k.
        for (int value : backups) {
            totalSum += value;
        }

        // Lower bound:
        // 1 is always a valid lower bound because all values are positive.
        long left = 1L;

        // Upper bound:
        // The minimum among k group sums cannot be larger than average totalSum / k.
        long right = totalSum / k;

        long answer = 1L;

        // Standard binary search on the answer space.
        // We search for the largest feasible S.
        while (left <= right) {
            long mid = left + (right - left) / 2;

            // If mid is feasible, try larger values.
            if (canPartitionIntoAtLeastKGroups(backups, k, mid)) {
                answer = mid;
                left = mid + 1;
            } else {
                // Otherwise mid is too large, try smaller values.
                right = mid - 1;
            }
        }

        return answer;
    }

    /**
     * Checks whether it is possible to split the array into at least k contiguous groups
     * such that every group's sum is at least targetMinSum.
     *
     * Greedy strategy:
     * - Scan from left to right.
     * - Keep adding elements to the current group.
     * - As soon as the current group sum reaches or exceeds targetMinSum, immediately cut the group.
     *
     * Why this greedy approach is correct:
     * - Cutting as early as possible leaves as much remaining sum as possible for future groups.
     * - Therefore, this strategy maximizes the number of groups we can form.
     * - So if even this greedy approach cannot make k groups, then no other partition can.
     *
     * Example for targetMinSum = 14 on [7,2,5,10,8]:
     * - 7 + 2 + 5 = 14 -> first group
     * - 10 + 8 = 18 -> second group
     * - formed 2 groups, so feasible for k = 2
     *
     * @param backups the array of positive backup sizes
     * @param k the required number of groups
     * @param targetMinSum the candidate minimum sum each group must satisfy
     * @return true if at least k valid contiguous groups can be formed; false otherwise
     * Time complexity: O(n)
     * Space complexity: O(1)
     */
    public boolean canPartitionIntoAtLeastKGroups(int[] backups, int k, long targetMinSum) {
        long currentSum = 0L;
        int groupsFormed = 0;

        // Traverse the array once.
        for (int value : backups) {
            currentSum += value;

            // The moment the current running sum reaches the target,
            // we greedily finalize one group.
            if (currentSum >= targetMinSum) {
                groupsFormed++;

                // Reset for the next group.
                currentSum = 0L;

                // Small optimization:
                // if we already formed at least k groups, we can stop early.
                if (groupsFormed >= k) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Convenience wrapper that accepts a List<Integer> instead of an int[].
     *
     * @param backupsList list of backup sizes
     * @param k the exact number of contiguous bundles required
     * @return the largest feasible minimum bundle sum
     * Time complexity: O(n log(sum(backups)))
     * Space complexity: O(n) due to array conversion
     */
    public long maximumFeasibleSnapshotSize(List<Integer> backupsList, int k) {
        int[] backups = new int[backupsList.size()];
        for (int i = 0; i < backupsList.size(); i++) {
            backups[i] = backupsList.get(i);
        }
        return maximumFeasibleSnapshotSize(backups, k);
    }

    /**
     * Demonstrates the solution on the sample test cases from the problem statement.
     *
     * Expected outputs:
     * - Example 1: 14
     * - Example 2: 8
     *
     * @param args command-line arguments (not used)
     * @return nothing
     * Time complexity: O(n log(sum(backups))) across the demonstrated examples
     * Space complexity: O(1) excluding input storage
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        int[] backups1 = {7, 2, 5, 10, 8};
        int k1 = 2;
        long result1 = solution.maximumFeasibleSnapshotSize(backups1, k1);
        System.out.println("Example 1 Result: " + result1);

        int[] backups2 = {4, 4, 4, 4, 4, 4, 4};
        int k2 = 3;
        long result2 = solution.maximumFeasibleSnapshotSize(backups2, k2);
        System.out.println("Example 2 Result: " + result2);

        // Additional quick sanity checks for beginners:
        // 1) If k == n, every element must be its own group, so answer is min element.
        int[] backups3 = {9, 1, 7, 3};
        int k3 = 4;
        long result3 = solution.maximumFeasibleSnapshotSize(backups3, k3);
        System.out.println("Sanity Check 1 Result: " + result3);

        // 2) If k == 1, the whole array is one group, so answer is total sum.
        int[] backups4 = {5, 6, 7};
        int k4 = 1;
        long result4 = solution.maximumFeasibleSnapshotSize(backups4, k4);
        System.out.println("Sanity Check 2 Result: " + result4);
    }
}