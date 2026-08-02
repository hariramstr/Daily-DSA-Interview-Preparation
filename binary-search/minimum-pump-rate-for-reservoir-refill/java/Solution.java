import java.util.*;

/*
Problem Title: Minimum Pump Rate for Reservoir Refill

Problem Description:
A city utility team must refill several reservoirs over a fixed number of nights.
You are given an integer array volumes where volumes[i] is the amount of water needed
for the i-th reservoir, and an integer h representing the total number of nights available.

In one night, the team chooses exactly one reservoir and pumps water into it at a constant
rate of k units per night. If a reservoir needs less than k units, the remaining pumping
capacity for that night is wasted and cannot be used on another reservoir.

A reservoir may require multiple nights to finish, and the number of nights needed for a
reservoir with volume v is ceil(v / k).

Return the minimum integer pump rate k such that all reservoirs can be completely refilled
within h nights.

This is guaranteed to have a valid answer.

Constraints:
- 1 <= volumes.length <= 100000
- 1 <= volumes[i] <= 1000000000
- volumes.length <= h <= 1000000000

Example 1:
Input: volumes = [8, 5, 10, 7], h = 8
Output: 5

Explanation:
At rate 5, the required nights are:
ceil(8/5)=2, ceil(5/5)=1, ceil(10/5)=2, ceil(7/5)=2
Total = 7 nights, which fits within 8.

At rate 4, the total becomes:
ceil(8/4)=2, ceil(5/4)=2, ceil(10/4)=3, ceil(7/4)=2
Total = 9 nights, so 4 is too slow.

Example 2:
Input: volumes = [30, 11, 23, 4, 20], h = 6
Output: 23

Explanation:
With k = 23, the total nights are:
ceil(30/23)=2, ceil(11/23)=1, ceil(23/23)=1, ceil(4/23)=1, ceil(20/23)=1
Total = 6 nights.

Any smaller rate would require more than 6 nights.

Key Insight:
If a pump rate k is sufficient, then any larger pump rate is also sufficient.
This monotonic behavior allows us to use binary search on the answer.
*/

public class Solution {

    /**
     * Finds the minimum integer pump rate needed to refill all reservoirs within h nights.
     *
     * We binary search the answer because:
     * - If a rate k works, then every rate > k also works.
     * - If a rate k does not work, then every rate < k also does not work.
     *
     * @param volumes the water volume required for each reservoir
     * @param h the maximum number of nights allowed
     * @return the minimum pump rate k such that all reservoirs can be completed within h nights
     * Time Complexity: O(n log m), where n is volumes.length and m is the maximum value in volumes
     * Space Complexity: O(1)
     */
    public int minPumpRate(int[] volumes, int h) {
        // The smallest possible pump rate is 1 unit per night.
        int left = 1;

        // The largest necessary pump rate is the maximum reservoir volume.
        // Why?
        // Because if k equals the largest volume, every reservoir can be completed in exactly 1 night.
        int right = getMaxVolume(volumes);

        // We will shrink the search space until left == right.
        // That final value will be the minimum valid pump rate.
        while (left < right) {
            // Compute the middle carefully to avoid overflow.
            int mid = left + (right - left) / 2;

            // Check whether this candidate rate is fast enough.
            if (canFinish(volumes, h, mid)) {
                // If mid works, it might be the answer,
                // but maybe there is an even smaller valid rate.
                // So we keep searching on the left half, including mid.
                right = mid;
            } else {
                // If mid does not work, then every smaller rate also does not work.
                // So we must search strictly to the right of mid.
                left = mid + 1;
            }
        }

        // At this point, left == right and points to the minimum valid rate.
        return left;
    }

    /**
     * Determines whether all reservoirs can be refilled within h nights at a given pump rate.
     *
     * For each reservoir with volume v, the number of nights needed is ceil(v / rate).
     * We compute that using integer arithmetic:
     * ceil(v / rate) = (v + rate - 1) / rate
     *
     * @param volumes the water volume required for each reservoir
     * @param h the maximum number of nights allowed
     * @param rate the candidate pump rate to test
     * @return true if all reservoirs can be completed within h nights, otherwise false
     * Time Complexity: O(n), where n is volumes.length
     * Space Complexity: O(1)
     */
    public boolean canFinish(int[] volumes, int h, int rate) {
        // Use long because the total number of nights can exceed int during accumulation.
        long totalNights = 0;

        // Process each reservoir independently.
        for (int volume : volumes) {
            // Compute ceil(volume / rate) without floating-point math.
            totalNights += (volume + (long) rate - 1) / rate;

            // Small optimization:
            // If we already exceeded h, there is no need to continue.
            if (totalNights > h) {
                return false;
            }
        }

        // If total required nights is within the limit, this rate works.
        return totalNights <= h;
    }

    /**
     * Returns the maximum reservoir volume in the array.
     *
     * This value is used as the upper bound for binary search.
     *
     * @param volumes the water volume required for each reservoir
     * @return the maximum value in volumes
     * Time Complexity: O(n), where n is volumes.length
     * Space Complexity: O(1)
     */
    public int getMaxVolume(int[] volumes) {
        int max = 0;

        for (int volume : volumes) {
            if (volume > max) {
                max = volume;
            }
        }

        return max;
    }

    /**
     * Demonstrates the solution using the sample inputs from the problem statement.
     *
     * Also prints a few extra checks so the behavior is easy to understand.
     *
     * @param args command-line arguments (not used)
     * @return nothing
     * Time Complexity: O(n log m) per demonstration call
     * Space Complexity: O(1)
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        int[] volumes1 = {8, 5, 10, 7};
        int h1 = 8;
        int result1 = solution.minPumpRate(volumes1, h1);
        System.out.println("Example 1:");
        System.out.println("volumes = " + Arrays.toString(volumes1) + ", h = " + h1);
        System.out.println("Minimum pump rate = " + result1);
        System.out.println("Expected = 5");
        System.out.println();

        int[] volumes2 = {30, 11, 23, 4, 20};
        int h2 = 6;
        int result2 = solution.minPumpRate(volumes2, h2);
        System.out.println("Example 2:");
        System.out.println("volumes = " + Arrays.toString(volumes2) + ", h = " + h2);
        System.out.println("Minimum pump rate = " + result2);
        System.out.println("Expected = 23");
        System.out.println();

        // Extra demonstration:
        // If h equals the number of reservoirs, then each reservoir must usually be finished in one night,
        // so the answer often becomes the maximum volume.
        int[] volumes3 = {3, 6, 7, 11};
        int h3 = 4;
        int result3 = solution.minPumpRate(volumes3, h3);
        System.out.println("Extra Example:");
        System.out.println("volumes = " + Arrays.toString(volumes3) + ", h = " + h3);
        System.out.println("Minimum pump rate = " + result3);
    }
}