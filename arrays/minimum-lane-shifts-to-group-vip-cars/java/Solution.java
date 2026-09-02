import java.util.*;

/*
 * Title: Minimum Lane Shifts to Group VIP Cars
 * Difficulty: Medium
 * Topic: Arrays
 *
 * Problem Description:
 * You are given an array lanes where each element is either 0 or 1. A value of 1
 * represents a VIP car, and a value of 0 represents a regular car. The cars are
 * parked in a single row, and you want all VIP cars to end up occupying consecutive
 * positions somewhere in the row.
 *
 * In one operation, you may choose a VIP car and shift it left or right by one
 * position, swapping it with the adjacent car. The cost of each adjacent swap is 1.
 * Your task is to return the minimum total number of adjacent swaps required to make
 * all VIP cars contiguous.
 *
 * The relative order of VIP cars does not matter beyond what is implied by adjacent
 * swaps, and you may choose any final block of consecutive positions for them. If the
 * array contains 0 or 1 VIP car, the answer is 0.
 *
 * This problem asks you to compute the minimum movement cost efficiently for large
 * inputs. A brute-force attempt over all possible target blocks will be too slow, so
 * you need to exploit the structure of VIP positions in the array.
 *
 * Constraints:
 * - 1 <= lanes.length <= 100000
 * - lanes[i] is either 0 or 1
 * - The answer fits in a 64-bit integer
 *
 * Example 1:
 * Input: lanes = [1,0,0,1,0,1]
 * Output: 3
 * Explanation: The VIP cars are at indices 0, 3, and 5. One optimal result is to move
 * them to indices 2, 3, and 4. That takes 2 swaps for the first VIP car and 1 swap for
 * the last VIP car, for a total of 3.
 *
 * Example 2:
 * Input: lanes = [0,1,0,1,0,0,1,0]
 * Output: 4
 * Explanation: The VIP cars are at indices 1, 3, and 6. An optimal final block is
 * indices 2, 3, and 4. The first VIP car moves 1 step right, the second stays, and the
 * third moves 2 steps left, so the total cost is 4.
 */

public class Solution {

    /**
     * Computes the minimum number of adjacent swaps required to make all VIP cars
     * (represented by 1s) occupy consecutive positions.
     *
     * Core idea:
     * If the VIP cars are currently at positions p[0], p[1], ..., p[k-1], and we want
     * them to end up in some consecutive block:
     *
     *     x, x+1, x+2, ..., x+k-1
     *
     * then the total movement cost is:
     *
     *     |p[0] - x| + |p[1] - (x+1)| + ... + |p[k-1] - (x+k-1)|
     *
     * Rearranging:
     *
     *     |(p[0] - 0) - x| + |(p[1] - 1) - x| + ... + |(p[k-1] - (k-1)) - x|
     *
     * So we define:
     *
     *     adjusted[i] = p[i] - i
     *
     * Then we need to minimize:
     *
     *     sum |adjusted[i] - x|
     *
     * A standard fact is that the sum of absolute deviations is minimized at the median.
     * Therefore, choosing x as the median of adjusted[] gives the minimum total swaps.
     *
     * Because p[] is increasing and i is increasing, adjusted[] is also non-decreasing,
     * so we can directly take the middle element as the median without sorting.
     *
     * @param lanes the input array where 1 means VIP car and 0 means regular car
     * @return the minimum total number of adjacent swaps needed to group all VIP cars
     *         into one contiguous block
     * Time complexity: O(n), where n is lanes.length
     * Space complexity: O(k), where k is the number of VIP cars
     */
    public long minAdjacentSwapsToGroupVIPCars(int[] lanes) {
        // Step 1:
        // Collect the indices of all VIP cars (all positions where lanes[i] == 1).
        // We only care about where the VIP cars currently are.
        List<Integer> positions = new ArrayList<>();
        for (int i = 0; i < lanes.length; i++) {
            if (lanes[i] == 1) {
                positions.add(i);
            }
        }

        // Step 2:
        // If there are 0 or 1 VIP cars, they are already trivially contiguous.
        int vipCount = positions.size();
        if (vipCount <= 1) {
            return 0L;
        }

        // Step 3:
        // Build the adjusted positions array:
        // adjusted[i] = positions[i] - i
        //
        // Why subtract i?
        // Because in the final contiguous arrangement, the VIP cars should occupy:
        // x, x+1, x+2, ..., x+(vipCount-1)
        //
        // Matching the i-th VIP car to x+i gives movement:
        // |positions[i] - (x+i)| = |(positions[i] - i) - x|
        //
        // So the problem becomes:
        // choose x minimizing sum of absolute differences from adjusted[].
        long[] adjusted = new long[vipCount];
        for (int i = 0; i < vipCount; i++) {
            adjusted[i] = (long) positions.get(i) - i;
        }

        // Step 4:
        // The minimum sum of absolute deviations is achieved at the median.
        // Since adjusted[] is already sorted/non-decreasing, the median is simply
        // the middle element.
        long median = adjusted[vipCount / 2];

        // Step 5:
        // Compute the total cost to move every adjusted[i] to the median.
        // This equals the minimum number of adjacent swaps.
        long totalSwaps = 0L;
        for (int i = 0; i < vipCount; i++) {
            totalSwaps += Math.abs(adjusted[i] - median);
        }

        return totalSwaps;
    }

    /**
     * Convenience wrapper using the exact problem wording.
     *
     * @param lanes the input array where 1 means VIP car and 0 means regular car
     * @return the minimum total adjacent swap cost to make all VIP cars contiguous
     * Time complexity: O(n), where n is lanes.length
     * Space complexity: O(k), where k is the number of VIP cars
     */
    public long minimumLaneShifts(int[] lanes) {
        return minAdjacentSwapsToGroupVIPCars(lanes);
    }

    /**
     * Converts an int array to a readable string representation.
     *
     * @param arr the array to convert
     * @return a string such as [1, 0, 1]
     * Time complexity: O(n), where n is arr.length
     * Space complexity: O(n) for the produced string
     */
    public String arrayToString(int[] arr) {
        return Arrays.toString(arr);
    }

    /**
     * Demonstrates the solution on the sample inputs and a few extra cases.
     *
     * @param args command-line arguments (not used)
     * @return nothing
     * Time complexity: O(1) for the fixed demo inputs
     * Space complexity: O(1) excluding method-local demo arrays
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        int[] lanes1 = {1, 0, 0, 1, 0, 1};
        long result1 = solution.minimumLaneShifts(lanes1);
        System.out.println("Input:  " + solution.arrayToString(lanes1));
        System.out.println("Output: " + result1);
        System.out.println("Expected: 3");
        System.out.println();

        int[] lanes2 = {0, 1, 0, 1, 0, 0, 1, 0};
        long result2 = solution.minimumLaneShifts(lanes2);
        System.out.println("Input:  " + solution.arrayToString(lanes2));
        System.out.println("Output: " + result2);
        System.out.println("Expected: 4");
        System.out.println();

        int[] lanes3 = {0, 0, 0, 0};
        long result3 = solution.minimumLaneShifts(lanes3);
        System.out.println("Input:  " + solution.arrayToString(lanes3));
        System.out.println("Output: " + result3);
        System.out.println("Expected: 0");
        System.out.println();

        int[] lanes4 = {0, 1, 0, 0, 0};
        long result4 = solution.minimumLaneShifts(lanes4);
        System.out.println("Input:  " + solution.arrayToString(lanes4));
        System.out.println("Output: " + result4);
        System.out.println("Expected: 0");
        System.out.println();

        int[] lanes5 = {1, 0, 1, 0, 1, 0, 1};
        long result5 = solution.minimumLaneShifts(lanes5);
        System.out.println("Input:  " + solution.arrayToString(lanes5));
        System.out.println("Output: " + result5);
        System.out.println("Expected: 4");
    }
}