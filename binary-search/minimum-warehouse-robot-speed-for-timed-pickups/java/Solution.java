import java.util.*;

/*
Title: Minimum Warehouse Robot Speed for Timed Pickups

Problem Description:
A warehouse robot must collect items from several aisles in a fixed order. You are given an integer array distances,
where distances[i] is the length of aisle i in meters, and an integer array deadlines, where deadlines[i] is the latest
time in seconds by which the robot must finish aisle i. The robot moves at a constant integer speed s meters per second
for the entire route. The time to finish aisle i is the cumulative time spent traversing aisles 0 through i, which is
the sum of distances[j] / s for all j from 0 to i. The robot is considered on time only if for every i, the cumulative
time is less than or equal to deadlines[i].

Return the minimum integer speed s such that the robot can finish every aisle by its corresponding deadline.
If no positive integer speed can satisfy all deadlines, return -1.

This is a decision-and-search problem: for a candidate speed, you can check whether all cumulative deadlines are met,
and the feasibility is monotonic. If a speed works, any larger speed also works.

Constraints:
- 1 <= distances.length == deadlines.length <= 100000
- 1 <= distances[i] <= 1000000
- 1 <= deadlines[i] <= 1000000000
- Speed s must be a positive integer

Example 1:
Input: distances = [4, 3, 6], deadlines = [2, 4, 7]
Output: 2
Explanation: At speed 2, cumulative times are 2.0, 3.5, and 6.5, all within the deadlines.
Speed 1 fails at the first aisle because 4.0 > 2.

Example 2:
Input: distances = [5, 8, 4], deadlines = [1, 2, 3]
Output: 6
Explanation:
At speed 6, cumulative times are:
- after aisle 0: 5/6 ≈ 0.8333 <= 1
- after aisle 1: 13/6 ≈ 2.1667 > 2, so speed 6 actually fails

Let's compute correctly:
We need:
5/s <= 1      => s >= 5
13/s <= 2     => s >= 6.5, so s >= 7
17/s <= 3     => s >= 5.666..., so s >= 6

Therefore the minimum integer speed is 7.

Important correctness note:
The originally provided example output of -1 is inconsistent with the stated problem definition because with sufficiently
large speed, cumulative times approach 0 and can satisfy these deadlines. Therefore, under the exact mathematical rules
given in this problem, the correct output for Example 2 is 7.

Approach:
Let prefixDistance[i] be the total distance from aisle 0 through aisle i.
For a speed s, aisle i is on time if:
    prefixDistance[i] / s <= deadlines[i]
which is equivalent to:
    prefixDistance[i] <= s * deadlines[i]

Feasibility is monotonic:
- If speed s works, any speed larger than s also works.
So we can binary search the minimum feasible integer speed.
*/
public class Solution {

    /**
     * Finds the minimum positive integer speed that allows the robot to finish every aisle
     * by its corresponding cumulative deadline.
     *
     * The key observation is:
     * For each aisle i, if prefixDistance is the total distance traveled up to i, then
     * prefixDistance / speed <= deadlines[i]
     * Rearranging gives:
     * speed >= prefixDistance / deadlines[i]
     *
     * Since speed must be an integer and must satisfy all aisles, the answer is the maximum
     * over all aisles of:
     * ceil(prefixDistance / deadlines[i])
     *
     * This method computes that directly in linear time.
     *
     * @param distances the distance of each aisle in meters
     * @param deadlines the latest allowed cumulative finishing time for each aisle in seconds
     * @return the minimum positive integer speed that satisfies all deadlines, or -1 if input is invalid
     * Time complexity: O(n)
     * Space complexity: O(1)
     */
    public int minimumSpeed(int[] distances, int[] deadlines) {
        if (distances == null || deadlines == null || distances.length != deadlines.length || distances.length == 0) {
            return -1;
        }

        long prefixDistance = 0L;
        long answer = 1L;

        // We process aisles from left to right because deadlines are defined on cumulative completion times.
        for (int i = 0; i < distances.length; i++) {
            prefixDistance += distances[i];

            // Since deadlines[i] >= 1 by constraints, division is safe.
            // We need the smallest integer speed such that:
            // prefixDistance / speed <= deadlines[i]
            //
            // Rearranged:
            // speed >= prefixDistance / deadlines[i]
            //
            // Because speed must be an integer, we take:
            // requiredSpeed = ceil(prefixDistance / deadlines[i])
            long requiredSpeed = ceilDiv(prefixDistance, deadlines[i]);

            // The final speed must satisfy every aisle, so we keep the maximum requirement seen so far.
            answer = Math.max(answer, requiredSpeed);
        }

        // Under the stated constraints, a valid positive integer answer always exists for valid input.
        // Still, we guard against overflow beyond int range because the method returns int.
        if (answer > Integer.MAX_VALUE) {
            return -1;
        }

        return (int) answer;
    }

    /**
     * Finds the minimum positive integer speed using binary search.
     *
     * This method is included because the problem is explicitly a decision-and-search problem.
     * It first determines an upper bound that is guaranteed to work, then binary searches
     * the smallest feasible speed.
     *
     * Feasibility condition for a candidate speed s:
     * For every aisle i:
     *     cumulativeDistanceUpToI / s <= deadlines[i]
     *
     * Since larger speeds only reduce travel times, feasibility is monotonic.
     *
     * @param distances the distance of each aisle in meters
     * @param deadlines the latest allowed cumulative finishing time for each aisle in seconds
     * @return the minimum positive integer speed that satisfies all deadlines, or -1 if input is invalid
     * Time complexity: O(n log A), where A is the answer range
     * Space complexity: O(1)
     */
    public int minimumSpeedBinarySearch(int[] distances, int[] deadlines) {
        if (distances == null || deadlines == null || distances.length != deadlines.length || distances.length == 0) {
            return -1;
        }

        long low = 1L;
        long high = 1L;

        // Step 1: Expand the upper bound until it becomes feasible.
        // This guarantees that the answer lies in [low, high].
        while (!canFinish(distances, deadlines, high)) {
            high <<= 1;

            // Safety guard: if the upper bound grows beyond int range, we stop.
            // Under normal valid constraints, the direct formula answer fits in int for many practical cases,
            // but this guard keeps the method robust.
            if (high > Integer.MAX_VALUE) {
                high = Integer.MAX_VALUE;
                if (!canFinish(distances, deadlines, high)) {
                    return -1;
                }
                break;
            }
        }

        // Step 2: Standard binary search for the first feasible speed.
        while (low < high) {
            long mid = low + (high - low) / 2;

            // If mid works, try to find an even smaller working speed on the left side.
            if (canFinish(distances, deadlines, mid)) {
                high = mid;
            } else {
                // If mid fails, every speed <= mid also fails, so move right.
                low = mid + 1;
            }
        }

        return (int) low;
    }

    /**
     * Checks whether a given speed allows the robot to satisfy every cumulative deadline.
     *
     * Instead of using floating-point arithmetic, this method uses integer arithmetic to avoid
     * precision issues:
     *     prefixDistance / speed <= deadline
     * is equivalent to:
     *     prefixDistance <= speed * deadline
     *
     * This is exact and safe when computed with long.
     *
     * @param distances the distance of each aisle in meters
     * @param deadlines the latest allowed cumulative finishing time for each aisle in seconds
     * @param speed the candidate integer speed in meters per second
     * @return true if the robot can finish every aisle by its deadline at this speed; false otherwise
     * Time complexity: O(n)
     * Space complexity: O(1)
     */
    public boolean canFinish(int[] distances, int[] deadlines, long speed) {
        if (speed <= 0) {
            return false;
        }

        long prefixDistance = 0L;

        // We walk through the aisles in order and verify each cumulative deadline.
        for (int i = 0; i < distances.length; i++) {
            prefixDistance += distances[i];

            // If cumulative distance traveled so far is greater than speed * allowed time,
            // then cumulativeTime = prefixDistance / speed would exceed deadlines[i].
            if (prefixDistance > speed * (long) deadlines[i]) {
                return false;
            }
        }

        // If no aisle violated its deadline, this speed is feasible.
        return true;
    }

    /**
     * Computes ceil(a / b) for positive integers using integer arithmetic.
     *
     * Formula:
     *     ceil(a / b) = (a + b - 1) / b
     *
     * @param a the numerator
     * @param b the denominator
     * @return the ceiling of a divided by b
     * Time complexity: O(1)
     * Space complexity: O(1)
     */
    public long ceilDiv(long a, long b) {
        return (a + b - 1) / b;
    }

    /**
     * Demonstrates the solution on sample inputs and prints the results.
     *
     * Note:
     * The second sample in the prompt contains an inconsistency. Under the exact problem rules,
     * the correct minimum speed is 7, not -1. This main method prints the mathematically correct result.
     *
     * @param args command-line arguments (not used)
     * @return nothing
     * Time complexity: O(1) for the demonstration itself, excluding method internals
     * Space complexity: O(1)
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        int[] distances1 = {4, 3, 6};
        int[] deadlines1 = {2, 4, 7};

        int result1Direct = solution.minimumSpeed(distances1, deadlines1);
        int result1Binary = solution.minimumSpeedBinarySearch(distances1, deadlines1);

        System.out.println("Example 1:");
        System.out.println("Distances: " + Arrays.toString(distances1));
        System.out.println("Deadlines: " + Arrays.toString(deadlines1));
        System.out.println("Minimum speed (direct): " + result1Direct);
        System.out.println("Minimum speed (binary search): " + result1Binary);
        System.out.println("Expected: 2");
        System.out.println();

        int[] distances2 = {5, 8, 4};
        int[] deadlines2 = {1, 2, 3};

        int result2Direct = solution.minimumSpeed(distances2, deadlines2);
        int result2Binary = solution.minimumSpeedBinarySearch(distances2, deadlines2);

        System.out.println("Example 2:");
        System.out.println("Distances: " + Arrays.toString(distances2));
        System.out.println("Deadlines: " + Arrays.toString(deadlines2));
        System.out.println("Minimum speed (direct): " + result2Direct);
        System.out.println("Minimum speed (binary search): " + result2Binary);
        System.out.println("Mathematically correct expected value under the stated rules: 7");
        System.out.println();

        // Additional quick sanity check.
        int[] distances3 = {1, 1, 1, 1};
        int[] deadlines3 = {1, 2, 3, 4};

        System.out.println("Additional Test:");
        System.out.println("Distances: " + Arrays.toString(distances3));
        System.out.println("Deadlines: " + Arrays.toString(deadlines3));
        System.out.println("Minimum speed (direct): " + solution.minimumSpeed(distances3, deadlines3));
        System.out.println("Minimum speed (binary search): " + solution.minimumSpeedBinarySearch(distances3, deadlines3));
    }
}