import java.util.*;

/*
Problem Title: Maximum Starting Battery for a Hazardous Drone Route

Problem Description:
A delivery drone must travel through a fixed sequence of checkpoints. At checkpoint i, the drone's battery changes by delta[i], which may be positive (recharge station) or negative (wind loss, payload lift, or signal interference). The drone starts before checkpoint 0 with some integer battery B, then applies the checkpoints in order. At every moment after processing each checkpoint, the battery must stay within the safe operating range [0, capacity]. If the battery ever becomes negative, the drone crashes. If it ever exceeds capacity, the battery controller fails.

Your task is to compute the maximum integer starting battery B such that the drone can complete the entire route safely.

If no starting battery in [0, capacity] allows a safe traversal, return -1.

This is not asking whether one particular B works. You must find the largest feasible starting value. A correct solution is expected to exploit the monotonic structure of feasibility and use binary search on the answer.

Constraints:
- 1 <= delta.length <= 2 * 10^5
- -10^9 <= delta[i] <= 10^9
- 0 <= capacity <= 10^18
- Starting battery B must be an integer
- The answer must be computed in O(n log capacity) time or better

Example 1:
Input: delta = [4, -7, 3, -2], capacity = 8
Output: 4
Explanation: Starting with B = 4 gives battery levels 8, 1, 4, 2, all valid. Starting with B = 5 immediately reaches 9 after the first checkpoint, which exceeds capacity.

Example 2:
Input: delta = [-3, 5, -4, 1], capacity = 6
Output: 1
Explanation:
For a starting battery B to be valid, every prefix sum condition must hold:
B + prefixSum(i) must stay in [0, capacity] for all checkpoints i.

For delta = [-3, 5, -4, 1], the prefix sums are:
-3, 2, -2, -1

So we need:
B - 3 >= 0   => B >= 3
B + 2 <= 6   => B <= 4
B - 2 >= 0   => B >= 2
B - 1 >= 0   => B >= 1

Combining all constraints gives 3 <= B <= 4, so the maximum valid start is 4.
Indeed:
B = 4 -> battery levels: 1, 6, 2, 3, all valid.
Therefore the mathematically correct answer for this example is 4.

We implement the correct algorithm based on the monotonic feasibility of starting battery values.
*/

public class Solution {

    /**
     * Computes the maximum integer starting battery such that after applying every checkpoint
     * delta in order, the battery always remains within [0, capacity].
     *
     * The feasibility of a starting battery B is monotonic:
     * if some B is feasible, then every smaller B is not always feasible in general,
     * and every larger B is not always feasible either. However, for this specific problem,
     * feasibility over B forms a contiguous interval because every checkpoint imposes a linear
     * bound of the form:
     *   0 <= B + prefixSum <= capacity
     * which becomes:
     *   -prefixSum <= B <= capacity - prefixSum
     *
     * The intersection of all such intervals is again an interval. Therefore, the set of feasible
     * starting values is contiguous, and we can binary search for the maximum feasible B.
     *
     * @param delta the battery changes at each checkpoint
     * @param capacity the maximum allowed battery capacity
     * @return the largest feasible starting battery, or -1 if no starting battery works
     * Time complexity: O(n log(capacity + 1))
     * Space complexity: O(1)
     */
    public long maximumStartingBattery(int[] delta, long capacity) {
        // Before running binary search, it is useful to know whether there exists
        // at least one feasible starting battery at all.
        //
        // If even B = 0..capacity all fail, we must return -1.
        // Because feasibility forms an interval, checking whether any feasible value exists
        // can be done by testing the smallest and largest possible candidates through binary search,
        // but the simplest approach is:
        // - binary search for the maximum feasible B
        // - if none is feasible, return -1
        //
        // To detect "none is feasible", we first test whether B = 0 is feasible or whether
        // any feasible value exists by searching the whole range and tracking the best answer.
        long left = 0L;
        long right = capacity;
        long answer = -1L;

        // Standard "find maximum feasible value" binary search.
        while (left <= right) {
            // Use overflow-safe midpoint computation.
            long mid = left + ((right - left) >>> 1);

            if (isFeasible(delta, capacity, mid)) {
                // mid works, so it is a candidate answer.
                answer = mid;

                // Since we want the maximum feasible starting battery,
                // try to move to the right half to find a larger valid value.
                left = mid + 1;
            } else {
                // mid does not work, so any answer must be smaller.
                right = mid - 1;
            }
        }

        return answer;
    }

    /**
     * Checks whether a given starting battery can safely traverse the entire route.
     *
     * We simulate the route checkpoint by checkpoint:
     * - Start with battery = startBattery
     * - Add delta[i]
     * - After each addition, verify battery is still in [0, capacity]
     *
     * Because values can be large, we use long for the running battery.
     * The input delta values are int, but cumulative sums can exceed int range.
     *
     * @param delta the battery changes at each checkpoint
     * @param capacity the maximum allowed battery capacity
     * @param startBattery the starting battery to test
     * @return true if the route is safe for this starting battery, false otherwise
     * Time complexity: O(n)
     * Space complexity: O(1)
     */
    public boolean isFeasible(int[] delta, long capacity, long startBattery) {
        // The starting battery itself must be within [0, capacity].
        // In our binary search this is always true, but keeping this check makes
        // the method robust and self-contained.
        if (startBattery < 0 || startBattery > capacity) {
            return false;
        }

        long battery = startBattery;

        // Process each checkpoint in order.
        for (int change : delta) {
            // Apply the battery change at this checkpoint.
            battery += change;

            // Immediately after processing the checkpoint, the battery must remain safe.
            if (battery < 0 || battery > capacity) {
                return false;
            }
        }

        // If we never violated the bounds, this starting battery is feasible.
        return true;
    }

    /**
     * Computes the answer using a direct mathematical interval intersection approach.
     *
     * This method is not required by the prompt, but it is educational and useful for verification.
     * Every checkpoint with prefix sum P imposes:
     *   0 <= B + P <= capacity
     * which means:
     *   -P <= B <= capacity - P
     *
     * Intersecting these constraints over all checkpoints yields the full feasible interval.
     * The maximum feasible B is simply the upper end of that interval if the interval is non-empty.
     *
     * @param delta the battery changes at each checkpoint
     * @param capacity the maximum allowed battery capacity
     * @return the largest feasible starting battery, or -1 if no starting battery works
     * Time complexity: O(n)
     * Space complexity: O(1)
     */
    public long maximumStartingBatteryDirect(int[] delta, long capacity) {
        // Initially, before considering any checkpoint, B itself must be in [0, capacity].
        long lowerBound = 0L;
        long upperBound = capacity;

        long prefixSum = 0L;

        for (int change : delta) {
            prefixSum += change;

            // From 0 <= B + prefixSum, we get B >= -prefixSum.
            lowerBound = Math.max(lowerBound, -prefixSum);

            // From B + prefixSum <= capacity, we get B <= capacity - prefixSum.
            upperBound = Math.min(upperBound, capacity - prefixSum);

            // If the interval becomes empty, no feasible starting battery exists.
            if (lowerBound > upperBound) {
                return -1L;
            }
        }

        // We want the maximum feasible starting battery.
        return upperBound;
    }

    /**
     * Helper method to print an array in a readable form.
     *
     * @param arr the integer array to print
     * @return a string representation of the array
     * Time complexity: O(n)
     * Space complexity: O(n)
     */
    public String arrayToString(int[] arr) {
        return Arrays.toString(arr);
    }

    /**
     * Demonstrates the solution on sample and additional test cases.
     *
     * @param args command-line arguments (unused)
     * @return nothing
     * Time complexity: O(total input size * log(capacity + 1)) for demonstrated calls
     * Space complexity: O(1) excluding output formatting
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Sample 1 from the prompt.
        int[] delta1 = {4, -7, 3, -2};
        long capacity1 = 8L;
        long result1 = solution.maximumStartingBattery(delta1, capacity1);
        System.out.println("Sample 1:");
        System.out.println("delta = " + solution.arrayToString(delta1) + ", capacity = " + capacity1);
        System.out.println("Maximum starting battery = " + result1);
        System.out.println("Expected = 4");
        System.out.println();

        // Sample 2:
        // The prompt states output 3, but that is incorrect.
        // Let's verify the mathematically correct answer.
        int[] delta2 = {-3, 5, -4, 1};
        long capacity2 = 6L;
        long result2 = solution.maximumStartingBattery(delta2, capacity2);
        System.out.println("Sample 2:");
        System.out.println("delta = " + solution.arrayToString(delta2) + ", capacity = " + capacity2);
        System.out.println("Maximum starting battery = " + result2);
        System.out.println("Correct mathematical answer = 4");
        System.out.println();

        // Additional test: no feasible starting battery.
        int[] delta3 = {5};
        long capacity3 = 3L;
        long result3 = solution.maximumStartingBattery(delta3, capacity3);
        System.out.println("Additional Test 1:");
        System.out.println("delta = " + solution.arrayToString(delta3) + ", capacity = " + capacity3);
        System.out.println("Maximum starting battery = " + result3);
        System.out.println("Expected = -1");
        System.out.println();

        // Additional test: all starts from 0..capacity are feasible only if all prefix sums stay within [0, capacity]
        // after shifting by chosen B. Here the maximum should be capacity.
        int[] delta4 = {0, 0, 0};
        long capacity4 = 10L;
        long result4 = solution.maximumStartingBattery(delta4, capacity4);
        System.out.println("Additional Test 2:");
        System.out.println("delta = " + solution.arrayToString(delta4) + ", capacity = " + capacity4);
        System.out.println("Maximum starting battery = " + result4);
        System.out.println("Expected = 10");
        System.out.println();

        // Cross-check binary search result with direct interval method.
        long direct1 = solution.maximumStartingBatteryDirect(delta1, capacity1);
        long direct2 = solution.maximumStartingBatteryDirect(delta2, capacity2);
        long direct3 = solution.maximumStartingBatteryDirect(delta3, capacity3);
        long direct4 = solution.maximumStartingBatteryDirect(delta4, capacity4);

        System.out.println("Cross-check with direct interval method:");
        System.out.println("Test 1 direct = " + direct1);
        System.out.println("Test 2 direct = " + direct2);
        System.out.println("Test 3 direct = " + direct3);
        System.out.println("Test 4 direct = " + direct4);
    }
}