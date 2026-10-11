import java.util.*;

/*
 * Title: Minimum Time Gap to Place K Exam Sessions
 * Difficulty: Medium
 * Topic: Binary Search
 *
 * Problem Description:
 * A university has already reserved several classrooms at specific start times during a day.
 * The administration wants to schedule exactly k proctored exam sessions using these reserved
 * start times, with at most one exam session per reserved time. To reduce staffing conflicts
 * and hallway congestion, they want the chosen exam sessions to be as far apart as possible.
 *
 * You are given an integer array times, where times[i] is the start time of the i-th reserved
 * classroom slot in minutes from the beginning of the day, and an integer k. Return the largest
 * possible value d such that it is possible to choose exactly k distinct start times from times
 * where the difference between every pair of consecutive chosen start times is at least d minutes.
 *
 * If multiple selections are possible, you only need to return the maximum achievable minimum gap d.
 *
 * This problem should be solved efficiently for large inputs. A common approach is to sort the
 * times and use binary search on the answer. For a candidate gap d, greedily place exam sessions
 * from left to right and check whether k sessions can be scheduled.
 *
 * Constraints:
 * - 2 <= times.length <= 100000
 * - 0 <= times[i] <= 1000000000
 * - All values in times are distinct
 * - 2 <= k <= times.length
 *
 * Example 1:
 * Input: times = [30, 0, 90, 60, 120], k = 3
 * Output: 60
 * Explanation: After sorting, times = [0, 30, 60, 90, 120].
 * Choosing exam sessions at 0, 60, and 120 gives gaps of 60 and 60.
 * It is impossible to choose 3 sessions with a minimum gap greater than 60, so the answer is 60.
 *
 * Example 2:
 * Input: times = [5, 18, 1, 9, 14], k = 4
 * Output: 4
 * Explanation: After sorting, times = [1, 5, 9, 14, 18].
 * One valid choice is 1, 5, 9, and 14, whose consecutive gaps are 4, 4, and 5.
 * A minimum gap of 5 is not possible for 4 sessions, so the answer is 4.
 */

public class Solution {

    /**
     * Computes the largest possible minimum gap between consecutive chosen exam session times.
     *
     * The idea is:
     * 1. Sort the available times.
     * 2. Binary search on the answer d (the minimum allowed gap).
     * 3. For each candidate d, greedily try to place k sessions from left to right.
     * 4. If placement is possible, try a larger d; otherwise, try a smaller d.
     *
     * @param times the array of reserved classroom start times; all values are distinct
     * @param k the exact number of exam sessions to schedule
     * @return the maximum possible minimum gap between consecutive chosen session times
     * Time complexity: O(n log n + n log R), where n = times.length and R = max(times) - min(times)
     * Space complexity: O(1) extra space beyond the sort implementation details
     */
    public int maximumMinimumGap(int[] times, int k) {
        // Step 1:
        // Sort the times so that we can reason about gaps in increasing order.
        // Once sorted, if we greedily place sessions from left to right,
        // we can efficiently test whether a candidate minimum gap is feasible.
        Arrays.sort(times);

        // The smallest possible minimum gap is 0.
        // The largest possible minimum gap cannot exceed the difference
        // between the largest and smallest reserved times.
        int low = 0;
        int high = times[times.length - 1] - times[0];

        // This variable stores the best feasible answer found so far.
        int answer = 0;

        // Standard binary search on the answer space.
        while (low <= high) {
            // Compute middle carefully to avoid overflow.
            int mid = low + (high - low) / 2;

            // Check whether it is possible to choose k times such that
            // every consecutive chosen pair differs by at least mid.
            if (canPlaceSessions(times, k, mid)) {
                // If feasible, mid is a valid answer.
                // We record it and try to do even better by increasing the gap.
                answer = mid;
                low = mid + 1;
            } else {
                // If not feasible, the gap is too large.
                // We must search smaller values.
                high = mid - 1;
            }
        }

        return answer;
    }

    /**
     * Checks whether it is possible to choose exactly k session times from the sorted array
     * such that the gap between every pair of consecutive chosen times is at least minGap.
     *
     * Greedy strategy:
     * - Always choose the earliest possible time first.
     * - Then keep choosing the next earliest time that is at least minGap away from the last chosen one.
     *
     * Why greedy works:
     * - Choosing an earlier valid time leaves as much room as possible for future choices.
     * - Therefore, if greedy cannot place k sessions, no other strategy can.
     *
     * @param sortedTimes the sorted array of reserved start times
     * @param k the number of sessions we need to place
     * @param minGap the candidate minimum gap to test
     * @return true if we can place at least k sessions with gap at least minGap; false otherwise
     * Time complexity: O(n), where n = sortedTimes.length
     * Space complexity: O(1)
     */
    public boolean canPlaceSessions(int[] sortedTimes, int k, int minGap) {
        // We always place the first session at the earliest available time.
        int placed = 1;
        int lastChosenTime = sortedTimes[0];

        // Scan through the remaining times from left to right.
        for (int i = 1; i < sortedTimes.length; i++) {
            // If the current time is far enough from the last chosen time,
            // we can place another session here.
            if (sortedTimes[i] - lastChosenTime >= minGap) {
                placed++;
                lastChosenTime = sortedTimes[i];

                // As soon as we have placed k sessions, we can stop early.
                if (placed >= k) {
                    return true;
                }
            }
        }

        // If we finish scanning and still have fewer than k sessions,
        // then this minimum gap is not feasible.
        return false;
    }

    /**
     * Demonstrates the solution on the sample inputs from the problem statement.
     *
     * @param args command-line arguments (not used)
     * @return nothing
     * Time complexity: O(n log n + n log R) per demonstration call
     * Space complexity: O(1) extra space beyond sorting details
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Example 1:
        // Input: times = [30, 0, 90, 60, 120], k = 3
        // Sorted: [0, 30, 60, 90, 120]
        // Best choice: 0, 60, 120 -> minimum gap = 60
        int[] times1 = {30, 0, 90, 60, 120};
        int k1 = 3;
        int result1 = solution.maximumMinimumGap(times1, k1);
        System.out.println("Example 1 Output: " + result1); // Expected: 60

        // Example 2:
        // Input: times = [5, 18, 1, 9, 14], k = 4
        // Sorted: [1, 5, 9, 14, 18]
        // One valid choice: 1, 5, 9, 14 -> gaps 4, 4, 5 -> minimum gap = 4
        int[] times2 = {5, 18, 1, 9, 14};
        int k2 = 4;
        int result2 = solution.maximumMinimumGap(times2, k2);
        System.out.println("Example 2 Output: " + result2); // Expected: 4

        // Additional quick demonstration:
        int[] times3 = {1, 2, 4, 8, 9};
        int k3 = 3;
        int result3 = solution.maximumMinimumGap(times3, k3);
        System.out.println("Additional Example Output: " + result3);
    }
}