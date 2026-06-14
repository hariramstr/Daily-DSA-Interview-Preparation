import java.util.*;

/*
 * Title: Shortest Error Burst Covering All Failure Codes
 * Difficulty: Hard
 * Topic: Sliding Window
 *
 * Problem Description:
 * You are given a chronological stream of application error reports represented by an integer array reports,
 * where reports[i] is the failure code produced at time i. You are also given an integer array required
 * containing the set of distinct failure codes that an incident investigation must observe at least once.
 * In addition, some failure codes may appear many times in the stream, and the same required code may be
 * scattered far apart.
 *
 * Your task is to find the length of the shortest contiguous subarray of reports that contains every code
 * in required at least once and also contains at least k total occurrences of critical codes, where critical
 * is another integer array of distinct codes. A report whose code belongs to critical contributes 1 toward
 * this count even if that code is not in required. If no such subarray exists, return -1.
 *
 * Formally, find the minimum length of a window reports[l..r] such that:
 * 1. Every value in required appears at least once in reports[l..r].
 * 2. The number of indices j in [l, r] with reports[j] in critical is at least k.
 *
 * The arrays required and critical may overlap partially, completely, or not at all. All codes are positive integers.
 *
 * Constraints:
 * - 1 <= reports.length <= 200000
 * - 1 <= required.length <= 200000
 * - 1 <= critical.length <= 200000
 * - 1 <= reports[i], required[i], critical[i] <= 10^9
 * - required contains distinct values
 * - critical contains distinct values
 * - 0 <= k <= reports.length
 *
 * Example 1:
 * Input: reports = [7,4,9,4,2,8,9,2,5], required = [4,2,5], critical = [9,8,5], k = 2
 * Output: 6
 *
 * Example 2:
 * Input: reports = [1,3,1,6,7,3,6], required = [1,6], critical = [3], k = 2
 * Output: 5
 *
 * A solution is expected to run in near-linear time. Carefully handle duplicate report values, overlap
 * between required and critical sets, and the case k = 0.
 */

public class Solution {

    /**
     * Finds the minimum length of a contiguous subarray that:
     * 1) contains every required code at least once, and
     * 2) contains at least k total occurrences of critical codes.
     *
     * The algorithm uses a classic sliding window:
     * - Expand the right boundary to include more reports.
     * - Track:
     *   a) how many distinct required codes are currently satisfied in the window
     *   b) how many critical occurrences are currently inside the window
     * - Once the window is valid, shrink the left boundary as much as possible
     *   while preserving validity, updating the best answer.
     *
     * @param reports the chronological stream of failure codes
     * @param required the distinct failure codes that must each appear at least once in the window
     * @param critical the distinct failure codes whose occurrences count toward the critical total
     * @param k the minimum number of critical occurrences required inside the window
     * @return the length of the shortest valid subarray, or -1 if no such subarray exists
     *
     * Time complexity: O(n + r + c), where n = reports.length, r = required.length, c = critical.length
     * Space complexity: O(r + c)
     */
    public int shortestErrorBurst(int[] reports, int[] required, int[] critical, int k) {
        int n = reports.length;

        // Store required codes in a set for O(1)-average membership checks.
        Set<Integer> requiredSet = new HashSet<>();
        for (int code : required) {
            requiredSet.add(code);
        }

        // Store critical codes in a set for O(1)-average membership checks.
        Set<Integer> criticalSet = new HashSet<>();
        for (int code : critical) {
            criticalSet.add(code);
        }

        // Quick impossibility check:
        // If k is larger than the total number of critical occurrences in the entire array,
        // then no window can ever satisfy the critical-count condition.
        if (k > 0) {
            int totalCriticalInAllReports = 0;
            for (int code : reports) {
                if (criticalSet.contains(code)) {
                    totalCriticalInAllReports++;
                }
            }
            if (totalCriticalInAllReports < k) {
                return -1;
            }
        }

        // Another quick impossibility check:
        // If any required code never appears in the full reports array, then no valid window exists.
        Set<Integer> seenRequiredInReports = new HashSet<>();
        for (int code : reports) {
            if (requiredSet.contains(code)) {
                seenRequiredInReports.add(code);
            }
        }
        if (seenRequiredInReports.size() < requiredSet.size()) {
            return -1;
        }

        // Frequency map for required codes currently inside the sliding window.
        Map<Integer, Integer> windowRequiredCount = new HashMap<>();

        // Number of distinct required codes currently present at least once in the window.
        int satisfiedRequiredKinds = 0;

        // Total number of critical occurrences currently inside the window.
        int criticalOccurrences = 0;

        // Left boundary of the sliding window.
        int left = 0;

        // Best answer found so far. Start with a very large value.
        int best = Integer.MAX_VALUE;

        // Expand the right boundary one step at a time.
        for (int right = 0; right < n; right++) {
            int current = reports[right];

            // Step 1: Add reports[right] into the window.

            // If this value is one of the required codes, update its frequency.
            if (requiredSet.contains(current)) {
                int newCount = windowRequiredCount.getOrDefault(current, 0) + 1;
                windowRequiredCount.put(current, newCount);

                // If this required code just appeared for the first time in the window,
                // then one more required kind is now satisfied.
                if (newCount == 1) {
                    satisfiedRequiredKinds++;
                }
            }

            // If this value is critical, it contributes one occurrence.
            if (criticalSet.contains(current)) {
                criticalOccurrences++;
            }

            // Step 2: While the current window is valid, try to shrink it from the left.
            //
            // A window is valid if:
            // - all required distinct codes are present
            // - critical occurrence count is at least k
            while (satisfiedRequiredKinds == requiredSet.size() && criticalOccurrences >= k) {
                // Update the best answer with the current valid window length.
                best = Math.min(best, right - left + 1);

                // Now attempt to remove reports[left] and see whether the window can remain valid.
                int leftValue = reports[left];

                // If the left value is required, decrement its count.
                if (requiredSet.contains(leftValue)) {
                    int oldCount = windowRequiredCount.get(leftValue);
                    int newCount = oldCount - 1;

                    if (newCount == 0) {
                        // Removing this value means this required code is no longer present,
                        // so the window will stop satisfying all required codes.
                        windowRequiredCount.remove(leftValue);
                        satisfiedRequiredKinds--;
                    } else {
                        windowRequiredCount.put(leftValue, newCount);
                    }
                }

                // If the left value is critical, removing it decreases the critical occurrence count.
                if (criticalSet.contains(leftValue)) {
                    criticalOccurrences--;
                }

                // Move left boundary rightward by one.
                left++;
            }
        }

        return best == Integer.MAX_VALUE ? -1 : best;
    }

    /**
     * A helper method that runs one demonstration case and prints the result.
     *
     * @param reports the stream of failure codes
     * @param required the required distinct codes
     * @param critical the critical distinct codes
     * @param k the minimum number of critical occurrences
     * @return the computed shortest valid window length
     *
     * Time complexity: O(n + r + c)
     * Space complexity: O(r + c)
     */
    public int runDemo(int[] reports, int[] required, int[] critical, int k) {
        int result = shortestErrorBurst(reports, required, critical, k);
        System.out.println(result);
        return result;
    }

    /**
     * Main method demonstrating the solution on the sample inputs from the problem statement.
     *
     * @param args command-line arguments (not used)
     * @return nothing
     *
     * Time complexity: O(total input size of demonstrated examples)
     * Space complexity: O(size of sets/maps used per example)
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Example 1
        int[] reports1 = {7, 4, 9, 4, 2, 8, 9, 2, 5};
        int[] required1 = {4, 2, 5};
        int[] critical1 = {9, 8, 5};
        int k1 = 2;
        System.out.println("Example 1 Output:");
        solution.runDemo(reports1, required1, critical1, k1); // Expected: 6

        // Example 2
        int[] reports2 = {1, 3, 1, 6, 7, 3, 6};
        int[] required2 = {1, 6};
        int[] critical2 = {3};
        int k2 = 2;
        System.out.println("Example 2 Output:");
        solution.runDemo(reports2, required2, critical2, k2); // Expected: 5

        // Additional quick checks

        // k = 0 case: only need all required codes.
        int[] reports3 = {5, 1, 2, 3, 1};
        int[] required3 = {1, 3};
        int[] critical3 = {9};
        int k3 = 0;
        System.out.println("Additional Example 3 Output:");
        solution.runDemo(reports3, required3, critical3, k3); // Expected: 3 -> [1,2,3]

        // Impossible case: missing required code.
        int[] reports4 = {1, 2, 3};
        int[] required4 = {1, 4};
        int[] critical4 = {2};
        int k4 = 1;
        System.out.println("Additional Example 4 Output:");
        solution.runDemo(reports4, required4, critical4, k4); // Expected: -1
    }
}