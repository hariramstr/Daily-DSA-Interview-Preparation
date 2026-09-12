import java.util.*;

/*
 * Title: Minimum Server Version to Pass All Client Requirements
 * Difficulty: Medium
 * Topic: Binary Search
 *
 * Problem Description:
 * A company is rolling out a backend API in numbered versions from 1 to m.
 * There are n client applications, and each client i can successfully connect
 * only if the server version is at least requirements[i]. You are also given
 * an integer k, the minimum number of clients that must be supported immediately
 * after launch.
 *
 * Your task is to find the smallest server version v such that at least k clients
 * can connect when version v is deployed.
 *
 * Formally, return the minimum integer v where the count of values in requirements
 * that are less than or equal to v is at least k. If it is impossible because
 * k > n, return -1.
 *
 * The input array is not guaranteed to be sorted. A straightforward solution would
 * sort the requirements first, then use binary search on the answer or directly on
 * the sorted array. The goal is to design an efficient solution that works within
 * typical interview constraints.
 *
 * Constraints:
 * - 1 <= n <= 2 * 10^5
 * - 1 <= requirements[i] <= 10^9
 * - 1 <= m <= 10^9
 * - 1 <= k <= 2 * 10^5
 * - Server versions are positive integers from 1 to m
 *
 * If the minimum required version exceeds m, then no valid deployment inside the
 * allowed range exists, and you should return -1.
 *
 * Example 1:
 * Input: requirements = [5, 2, 8, 4, 4], m = 10, k = 3
 * Output: 4
 * Explanation: At version 4, the supported clients are those requiring 2, 4, and 4,
 * so 3 clients can connect. No smaller version supports at least 3 clients.
 *
 * Example 2:
 * Input: requirements = [7, 9, 12], m = 10, k = 2
 * Output: 9
 * Explanation: At version 9, clients requiring 7 and 9 can connect, so 2 clients
 * are supported. Version 8 supports only 1 client, so 9 is the minimum valid answer.
 */

public class Solution {

    /**
     * Finds the minimum server version that supports at least k clients.
     *
     * Core idea:
     * 1. If k is larger than the number of clients, it is impossible.
     * 2. Sort the requirements array.
     * 3. The smallest version that supports at least k clients is exactly the k-th
     *    smallest requirement (1-indexed), because once the server reaches that value,
     *    at least k requirements are <= that version.
     * 4. If that version is greater than m, then it cannot be deployed within the
     *    allowed version range, so return -1.
     *
     * @param requirements an array where requirements[i] is the minimum server version needed by client i
     * @param m the maximum allowed server version that can be deployed
     * @param k the minimum number of clients that must be supported
     * @return the smallest valid server version, or -1 if no such version exists
     * Time complexity: O(n log n) due to sorting
     * Space complexity: O(log n) auxiliary stack space for sorting in typical implementations, excluding input storage
     */
    public int minimumServerVersion(int[] requirements, int m, int k) {
        // If the input array is null, there are no clients to evaluate.
        // In a strict interview setting this may not be necessary because inputs are usually valid,
        // but adding this check makes the method safer and more beginner-friendly.
        if (requirements == null) {
            return -1;
        }

        int n = requirements.length;

        // If we need to support more clients than actually exist, the task is impossible.
        // Example:
        // requirements length = 3, but k = 5
        // There is no server version that can magically support 5 clients when only 3 exist.
        if (k > n) {
            return -1;
        }

        // Sort the requirements so that:
        // - requirements[0] is the smallest required version
        // - requirements[1] is the second smallest
        // - ...
        // - requirements[k - 1] is the k-th smallest required version
        //
        // Why does this help?
        // Because the minimum version that supports at least k clients must be the k-th smallest
        // requirement. Any smaller version would support fewer than k clients.
        Arrays.sort(requirements);

        // The k-th smallest element in 1-based indexing is at index k - 1 in 0-based indexing.
        int answer = requirements[k - 1];

        // Even if this is the mathematically correct minimum version,
        // it may still be outside the allowed deployment range [1, m].
        // If answer > m, then no valid deployment exists.
        if (answer > m) {
            return -1;
        }

        return answer;
    }

    /**
     * Alternative solution using binary search on the answer.
     *
     * This method is included because the problem topic is Binary Search.
     * It searches for the smallest version v in the range [1, m] such that
     * at least k clients have requirements <= v.
     *
     * Steps:
     * 1. If k > n, return -1.
     * 2. Binary search over possible server versions from 1 to m.
     * 3. For each mid version, count how many clients can connect.
     * 4. If at least k clients can connect, mid is a valid answer, so try smaller.
     * 5. Otherwise, try larger.
     *
     * Note:
     * This implementation sorts first and then uses upper bound to count how many
     * requirements are <= mid efficiently.
     *
     * @param requirements an array where requirements[i] is the minimum server version needed by client i
     * @param m the maximum allowed server version that can be deployed
     * @param k the minimum number of clients that must be supported
     * @return the smallest valid server version, or -1 if no such version exists
     * Time complexity: O(n log n + log m * log n)
     * Space complexity: O(log n) auxiliary stack space for sorting in typical implementations, excluding input storage
     */
    public int minimumServerVersionBinarySearch(int[] requirements, int m, int k) {
        if (requirements == null) {
            return -1;
        }

        int n = requirements.length;

        if (k > n) {
            return -1;
        }

        Arrays.sort(requirements);

        // Quick impossibility check:
        // If even the k-th smallest requirement is greater than m,
        // then no version in [1, m] can support k clients.
        if (requirements[k - 1] > m) {
            return -1;
        }

        int left = 1;
        int right = m;
        int answer = -1;

        // Standard "find first true" binary search:
        // Predicate(version) = number of requirements <= version is at least k
        while (left <= right) {
            int mid = left + (right - left) / 2;

            // Count how many clients can connect at version = mid.
            // Since the array is sorted, we can find the first index with value > mid.
            // That index is exactly the count of values <= mid.
            int supportedClients = countLessThanOrEqual(requirements, mid);

            if (supportedClients >= k) {
                // mid works, so record it and try to find an even smaller valid version.
                answer = mid;
                right = mid - 1;
            } else {
                // mid does not support enough clients, so we must increase the version.
                left = mid + 1;
            }
        }

        return answer;
    }

    /**
     * Counts how many values in a sorted array are less than or equal to target.
     *
     * This is the classic "upper bound" operation:
     * it returns the index of the first element greater than target,
     * which is also the count of elements <= target.
     *
     * Example:
     * sorted = [2, 4, 4, 5, 8], target = 4
     * first element > 4 is 5 at index 3
     * so there are 3 elements <= 4
     *
     * @param sortedRequirements the sorted requirements array
     * @param target the server version being tested
     * @return the number of elements in sortedRequirements that are <= target
     * Time complexity: O(log n)
     * Space complexity: O(1)
     */
    public int countLessThanOrEqual(int[] sortedRequirements, int target) {
        int left = 0;
        int right = sortedRequirements.length;

        // We search in the half-open interval [left, right).
        // Goal: find the first index where sortedRequirements[index] > target.
        while (left < right) {
            int mid = left + (right - left) / 2;

            if (sortedRequirements[mid] <= target) {
                // This value is allowed, so the first "greater than target" position
                // must be to the right of mid.
                left = mid + 1;
            } else {
                // This value is already too large, so the answer is at mid or to the left.
                right = mid;
            }
        }

        // left is now the first index with value > target,
        // which equals the count of values <= target.
        return left;
    }

    /**
     * Demonstrates the solution on the sample inputs from the problem statement.
     *
     * It prints:
     * - The result of the direct sorted approach
     * - The result of the binary search approach
     *
     * This also acts as a simple correctness check for the provided examples.
     *
     * @param args command-line arguments (not used)
     * @return nothing
     * Time complexity: O(n log n) per demonstration call
     * Space complexity: O(log n) auxiliary stack space for sorting in typical implementations
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Example 1:
        // requirements = [5, 2, 8, 4, 4], m = 10, k = 3
        // Sorted requirements = [2, 4, 4, 5, 8]
        // The 3rd smallest requirement is 4, so answer should be 4.
        int[] requirements1 = {5, 2, 8, 4, 4};
        int m1 = 10;
        int k1 = 3;

        System.out.println(solution.minimumServerVersion(requirements1.clone(), m1, k1));
        System.out.println(solution.minimumServerVersionBinarySearch(requirements1.clone(), m1, k1));

        // Example 2:
        // requirements = [7, 9, 12], m = 10, k = 2
        // Sorted requirements = [7, 9, 12]
        // The 2nd smallest requirement is 9, and 9 <= 10, so answer should be 9.
        int[] requirements2 = {7, 9, 12};
        int m2 = 10;
        int k2 = 2;

        System.out.println(solution.minimumServerVersion(requirements2.clone(), m2, k2));
        System.out.println(solution.minimumServerVersionBinarySearch(requirements2.clone(), m2, k2));

        // Additional demonstration:
        // Impossible because k > n
        int[] requirements3 = {3, 6, 9};
        int m3 = 10;
        int k3 = 5;

        System.out.println(solution.minimumServerVersion(requirements3.clone(), m3, k3));
        System.out.println(solution.minimumServerVersionBinarySearch(requirements3.clone(), m3, k3));

        // Additional demonstration:
        // k-th smallest requirement exceeds m, so no valid deployment exists.
        int[] requirements4 = {11, 12, 13};
        int m4 = 10;
        int k4 = 1;

        System.out.println(solution.minimumServerVersion(requirements4.clone(), m4, k4));
        System.out.println(solution.minimumServerVersionBinarySearch(requirements4.clone(), m4, k4));
    }
}