import java.util.*;

/*
Problem Title: Maximum Backup Interval Under Restore Deadline

Problem Description:
A storage team wants to reduce the number of full backups taken for a large database.
The database changes over time, and the amount of changed data on day i is given by changes[i].

If the team chooses a backup interval of k days, then every backup taken on day s must include
the total changed data from days s through min(s + k - 1, n - 1). Restoring from any single
backup is only allowed if the size of that backup does not exceed a system restore limit L.

The team always partitions the timeline into consecutive backup blocks of length at most k:
the first backup covers days 0 to k - 1, the next covers the following k days, and so on.
A backup interval k is considered feasible if every such backup block has total changed data
at most L.

Your task is to return the maximum feasible backup interval k.

In other words, among all positive integers k, find the largest k such that for every block
formed by splitting the array into consecutive chunks of size k (the last chunk may be shorter),
the sum of each chunk is at most L.

This problem is designed to reward an efficient solution. A brute-force check over all k values
and all ranges will be too slow for the largest inputs. You should exploit the monotonic nature
of feasibility with respect to k and combine binary search with fast range-sum validation.

Constraints:
- 1 <= n == changes.length <= 200000
- 0 <= changes[i] <= 1000000000
- 0 <= L <= 1000000000000000000
- Return 0 if no positive backup interval is feasible

Example 1:
Input: changes = [2, 1, 3, 2, 2], L = 6
Output: 3
Explanation:
- k = 2 gives blocks [2,1], [3,2], [2] with sums 3, 5, 2, all <= 6.
- k = 3 gives blocks [2,1,3], [2,2] with sums 6 and 4, also feasible.
- k = 4 gives blocks [2,1,3,2], [2] with sums 8 and 2, so it is not feasible.
The maximum feasible interval is 3.

Example 2:
Input: changes = [7, 1, 2], L = 6
Output: 0
Explanation:
Even k = 1 is not feasible because the first backup would contain 7 units of changed data,
which exceeds the restore limit.
*/

public class Solution {

    /**
     * Finds the maximum feasible backup interval k.
     *
     * Core idea:
     * 1. Build prefix sums so any block sum can be computed in O(1).
     * 2. Observe monotonicity:
     *    - If some k is feasible, then every smaller positive interval is also feasible.
     *      Why? Because all values are non-negative, so splitting a valid block into smaller
     *      consecutive pieces cannot increase any piece sum beyond the original block sum.
     *    - Therefore, feasibility over k looks like:
     *      feasible, feasible, feasible, ..., feasible, not feasible, not feasible, ...
     * 3. Use binary search on k from 1 to n to find the largest feasible k.
     *
     * Important correctness note:
     * The examples imply the intended answer for [2,1,3,2,2], L=6 is 3, not 2.
     *
     * @param changes array where changes[i] is the changed data on day i
     * @param L maximum allowed size of any single backup block
     * @return the largest feasible positive interval k, or 0 if no positive k is feasible
     * Time complexity: O(n log n)
     * Space complexity: O(n)
     */
    public int maximumBackupInterval(int[] changes, long L) {
        int n = changes.length;

        // Step 1:
        // Build prefix sums.
        //
        // prefix[i] will store the sum of the first i elements:
        // prefix[0] = 0
        // prefix[1] = changes[0]
        // prefix[2] = changes[0] + changes[1]
        // ...
        //
        // Then the sum of changes from index left to right inclusive is:
        // prefix[right + 1] - prefix[left]
        long[] prefix = buildPrefixSums(changes);

        // Step 2:
        // Before binary search, quickly check whether k = 1 is feasible.
        //
        // If even single-day backups are too large, then no positive interval can work.
        // In that case, the required answer is 0.
        if (!isFeasible(changes, prefix, 1, L)) {
            return 0;
        }

        // Step 3:
        // Binary search for the largest feasible k.
        //
        // Search space is [1, n].
        // We maintain:
        // - all values <= answer are feasible
        // - values > answer may be infeasible
        int left = 1;
        int right = n;
        int answer = 1;

        while (left <= right) {
            // Standard midpoint calculation that avoids overflow.
            int mid = left + (right - left) / 2;

            // Check whether interval length mid is feasible.
            if (isFeasible(changes, prefix, mid, L)) {
                // mid works, so record it and try to go larger.
                answer = mid;
                left = mid + 1;
            } else {
                // mid does not work, so all larger values also do not work.
                right = mid - 1;
            }
        }

        return answer;
    }

    /**
     * Builds prefix sums for the input array.
     *
     * @param changes input array of daily changed data
     * @return prefix sum array where prefix[i] is the sum of the first i elements
     * Time complexity: O(n)
     * Space complexity: O(n)
     */
    public long[] buildPrefixSums(int[] changes) {
        int n = changes.length;
        long[] prefix = new long[n + 1];

        for (int i = 0; i < n; i++) {
            prefix[i + 1] = prefix[i] + changes[i];
        }

        return prefix;
    }

    /**
     * Checks whether a given backup interval k is feasible.
     *
     * A k is feasible if, after splitting the array into consecutive blocks of size k
     * (with the final block possibly shorter), every block sum is at most L.
     *
     * We use prefix sums so each block sum is computed in O(1).
     *
     * Example:
     * changes = [2,1,3,2,2], k = 3
     * blocks are:
     * - indices [0..2] => sum = 6
     * - indices [3..4] => sum = 4
     * both <= 6, so feasible for L = 6
     *
     * @param changes input array of daily changed data
     * @param prefix prefix sum array for changes
     * @param k candidate backup interval
     * @param L maximum allowed block sum
     * @return true if every block of size k has sum <= L, otherwise false
     * Time complexity: O(n / k + 1), which is O(n) in the worst case
     * Space complexity: O(1) extra space beyond the prefix array
     */
    public boolean isFeasible(int[] changes, long[] prefix, int k, long L) {
        int n = changes.length;

        // We iterate over the array in jumps of size k.
        // Each iteration examines one backup block.
        for (int start = 0; start < n; start += k) {
            // The block ends at either start + k - 1 or the last index, whichever comes first.
            int endExclusive = Math.min(start + k, n);

            // Compute block sum using prefix sums.
            long blockSum = prefix[endExclusive] - prefix[start];

            // If any block exceeds the restore limit, k is not feasible.
            if (blockSum > L) {
                return false;
            }
        }

        // If we checked all blocks and none exceeded L, then k is feasible.
        return true;
    }

    /**
     * Runs a demonstration of the algorithm on sample inputs.
     *
     * @param args command-line arguments (not used)
     * @return nothing
     * Time complexity: O(1) for the fixed demo cases, excluding the algorithm calls
     * Space complexity: O(1) extra space, excluding the algorithm calls
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        int[] changes1 = {2, 1, 3, 2, 2};
        long L1 = 6;
        int result1 = solution.maximumBackupInterval(changes1, L1);
        System.out.println("Example 1 result: " + result1);
        System.out.println("Expected: 3");

        int[] changes2 = {7, 1, 2};
        long L2 = 6;
        int result2 = solution.maximumBackupInterval(changes2, L2);
        System.out.println("Example 2 result: " + result2);
        System.out.println("Expected: 0");

        int[] changes3 = {1, 1, 1, 1, 1};
        long L3 = 5;
        int result3 = solution.maximumBackupInterval(changes3, L3);
        System.out.println("Additional test 1 result: " + result3);
        System.out.println("Expected: 5");

        int[] changes4 = {5, 0, 0, 0};
        long L4 = 5;
        int result4 = solution.maximumBackupInterval(changes4, L4);
        System.out.println("Additional test 2 result: " + result4);
        System.out.println("Expected: 4");

        int[] changes5 = {3, 3, 3, 3};
        long L5 = 6;
        int result5 = solution.maximumBackupInterval(changes5, L5);
        System.out.println("Additional test 3 result: " + result5);
        System.out.println("Expected: 2");
    }
}