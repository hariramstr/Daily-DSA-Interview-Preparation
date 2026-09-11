import java.util.*;

/*
Problem Title: Minimum Router Delay for Sequential Packet Waves

Problem Description:
You are given a network router that must transmit packet waves in the given order. The i-th wave contains packets[i] packets, and the router can process at most d packets per second while a wave is active. Because of protocol overhead, each wave takes ceil(packets[i] / d) whole seconds to finish. The router cannot start the next wave until the current one is completed.

You are also given an integer maxTime, the maximum total number of seconds allowed to transmit all waves. Your task is to find the minimum integer router delay capacity d such that all waves can be transmitted within maxTime seconds.

If d is too small, some waves take too long. If d is larger, every wave finishes no slower than before. This monotonic behavior makes the problem suitable for binary search on the answer.

Return the minimum positive integer d that allows the total transmission time to be at most maxTime.

Constraints:
- 1 <= packets.length <= 100000
- 1 <= packets[i] <= 10^9
- packets.length <= maxTime <= 10^14
- The answer always exists.

Example 1:
Input: packets = [8, 4, 10], maxTime = 8
Output: 4
Explanation: With d = 4, the total time is ceil(8/4) + ceil(4/4) + ceil(10/4) = 2 + 1 + 3 = 6, which fits in 8.
With d = 3, the total time is 3 + 2 + 4 = 9, which is too slow.

Example 2:
Input: packets = [30, 11, 23, 4, 20], maxTime = 10
Output: 11
Explanation: At d = 11, the total time is 3 + 1 + 3 + 1 + 2 = 10.
Any smaller delay capacity causes the total time to exceed maxTime.
*/

public class Solution {

    /**
     * Finds the minimum positive integer router delay capacity d such that the total
     * transmission time of all packet waves is at most maxTime.
     *
     * The key idea is binary search on the answer:
     * - If a certain capacity d is sufficient, then any larger capacity is also sufficient.
     * - If a certain capacity d is not sufficient, then any smaller capacity is also not sufficient.
     * This monotonic property allows us to search efficiently.
     *
     * @param packets the array where packets[i] is the number of packets in the i-th wave
     * @param maxTime the maximum allowed total transmission time
     * @return the minimum router delay capacity d that allows all waves to finish within maxTime
     * Time complexity: O(n log M), where n is packets.length and M is the maximum value in packets
     * Space complexity: O(1), excluding input storage
     */
    public int minimumRouterDelay(int[] packets, long maxTime) {
        // The minimum possible capacity is 1 packet per second.
        long left = 1;

        // The maximum necessary capacity is the largest wave size.
        // Why is this enough?
        // Because if d >= max(packets), then each wave takes exactly 1 second.
        // Since the problem guarantees that an answer exists and maxTime >= packets.length,
        // searching up to max(packets) is sufficient.
        long right = getMaxPacketCount(packets);

        // This variable will store the best valid answer found so far.
        long answer = right;

        // Standard binary search over the capacity range [left, right].
        while (left <= right) {
            // Compute middle carefully to avoid overflow.
            long mid = left + (right - left) / 2;

            // Check whether this candidate capacity is enough.
            if (canFinishWithinTime(packets, maxTime, mid)) {
                // If mid works, record it as a possible answer.
                answer = mid;

                // But we still want the minimum valid capacity,
                // so continue searching on the left half.
                right = mid - 1;
            } else {
                // If mid does not work, we need a larger capacity.
                left = mid + 1;
            }
        }

        return (int) answer;
    }

    /**
     * Checks whether all packet waves can be transmitted within maxTime using
     * the given router delay capacity d.
     *
     * For each wave of size p, the required time is:
     * ceil(p / d)
     *
     * We compute ceiling division using integer arithmetic:
     * ceil(p / d) = (p + d - 1) / d
     *
     * We also stop early if the accumulated time already exceeds maxTime,
     * which improves performance in many cases.
     *
     * @param packets the array of packet wave sizes
     * @param maxTime the maximum allowed total time
     * @param d the candidate router delay capacity
     * @return true if all waves can finish within maxTime using capacity d; false otherwise
     * Time complexity: O(n), where n is packets.length
     * Space complexity: O(1)
     */
    public boolean canFinishWithinTime(int[] packets, long maxTime, long d) {
        // Use long because:
        // - maxTime can be as large as 10^14
        // - the accumulated total time can also be large
        long totalTime = 0;

        // Process each wave in order.
        for (int packetCount : packets) {
            // Compute ceil(packetCount / d) using integer math only.
            long timeForThisWave = (packetCount + d - 1) / d;

            // Add this wave's time to the running total.
            totalTime += timeForThisWave;

            // Important optimization:
            // As soon as totalTime exceeds maxTime, we already know d is not sufficient.
            if (totalTime > maxTime) {
                return false;
            }
        }

        // If we never exceeded maxTime, then d is sufficient.
        return true;
    }

    /**
     * Returns the maximum packet count among all waves.
     *
     * This value is used as the upper bound for binary search because a capacity
     * equal to the largest wave size guarantees each wave finishes in 1 second.
     *
     * @param packets the array of packet wave sizes
     * @return the maximum value in packets
     * Time complexity: O(n), where n is packets.length
     * Space complexity: O(1)
     */
    public long getMaxPacketCount(int[] packets) {
        long max = 0;

        for (int packetCount : packets) {
            if (packetCount > max) {
                max = packetCount;
            }
        }

        return max;
    }

    /**
     * Demonstrates the solution using the sample inputs from the problem statement.
     *
     * It also prints a few verification details so a beginner can see that the
     * computed answers match the expected outputs.
     *
     * @param args command-line arguments (not used)
     * @return nothing
     * Time complexity: O(n log M) per demonstration call
     * Space complexity: O(1), excluding input storage
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Example 1
        int[] packets1 = {8, 4, 10};
        long maxTime1 = 8;
        int result1 = solution.minimumRouterDelay(packets1, maxTime1);
        System.out.println("Example 1:");
        System.out.println("packets = " + Arrays.toString(packets1));
        System.out.println("maxTime = " + maxTime1);
        System.out.println("Minimum router delay capacity = " + result1);
        System.out.println("Expected = 4");
        System.out.println();

        // Quick trace verification for Example 1:
        // d = 4 => ceil(8/4) + ceil(4/4) + ceil(10/4) = 2 + 1 + 3 = 6 <= 8
        // d = 3 => ceil(8/3) + ceil(4/3) + ceil(10/3) = 3 + 2 + 4 = 9 > 8
        System.out.println("Verification for Example 1:");
        System.out.println("Can finish with d = 4? " + solution.canFinishWithinTime(packets1, maxTime1, 4));
        System.out.println("Can finish with d = 3? " + solution.canFinishWithinTime(packets1, maxTime1, 3));
        System.out.println();

        // Example 2
        int[] packets2 = {30, 11, 23, 4, 20};
        long maxTime2 = 10;
        int result2 = solution.minimumRouterDelay(packets2, maxTime2);
        System.out.println("Example 2:");
        System.out.println("packets = " + Arrays.toString(packets2));
        System.out.println("maxTime = " + maxTime2);
        System.out.println("Minimum router delay capacity = " + result2);
        System.out.println("Expected = 11");
        System.out.println();

        // Quick trace verification for Example 2:
        // d = 11 => ceil(30/11) + ceil(11/11) + ceil(23/11) + ceil(4/11) + ceil(20/11)
        //        = 3 + 1 + 3 + 1 + 2 = 10 <= 10
        // Any smaller valid candidate should fail; for example d = 10:
        //        = 3 + 2 + 3 + 1 + 2 = 11 > 10
        System.out.println("Verification for Example 2:");
        System.out.println("Can finish with d = 11? " + solution.canFinishWithinTime(packets2, maxTime2, 11));
        System.out.println("Can finish with d = 10? " + solution.canFinishWithinTime(packets2, maxTime2, 10));
    }
}