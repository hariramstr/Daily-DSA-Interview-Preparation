import java.util.*;

/*
 * Title: Maximum Recharge Gain from Flipping One Low-Battery Segment
 * Difficulty: Medium
 * Topic: Arrays
 *
 * Problem Description:
 * A mobile device records its battery trend over a day as a binary array `status`,
 * where `status[i] = 1` means the battery level was healthy during minute `i`,
 * and `status[i] = 0` means the device was in a low-battery state during minute `i`.
 * You are allowed to apply exactly one recovery patch to a single contiguous segment
 * of the array. The patch flips every value in that chosen segment: each `0` becomes `1`,
 * and each `1` becomes `0`.
 *
 * Your task is to return the maximum possible number of healthy minutes after applying
 * the patch exactly once.
 *
 * Because the patch must be used exactly once, you must choose a non-empty subarray to flip,
 * even if doing so decreases the final number of healthy minutes.
 *
 * Design an efficient algorithm for large inputs.
 *
 * Constraints:
 * - 1 <= status.length <= 2 * 10^5
 * - status[i] is either 0 or 1
 *
 * Example 1:
 * Input: status = [1,0,0,1,0,1]
 * Output: 5
 * Explanation:
 * Initially there are 3 healthy minutes.
 * Flipping the segment [0,0,1,0] (indices 1 to 4) changes the array to [1,1,1,0,1,1],
 * which has 5 healthy minutes. No single flip can produce more.
 *
 * Example 2:
 * Input: status = [1,1,1,1]
 * Output: 3
 * Explanation:
 * Since you must flip exactly one non-empty segment, at least one `1` must become `0`.
 * The best choice is to flip any segment of length 1, leaving 3 healthy minutes.
 *
 * Key Insight:
 * For each position:
 * - If status[i] == 0, flipping it changes it to 1, so the total number of healthy minutes gains +1.
 * - If status[i] == 1, flipping it changes it to 0, so the total number of healthy minutes gains -1.
 *
 * Therefore, convert the array into a "gain array":
 * - 0 -> +1
 * - 1 -> -1
 *
 * Then the problem becomes:
 * - Count the original number of 1s.
 * - Find the contiguous subarray with the maximum sum in the gain array.
 * - Add that maximum gain to the original count of 1s.
 *
 * Because the flip must be used exactly once and must be non-empty, we must find the maximum
 * sum over a non-empty subarray. This is exactly Kadane's algorithm in its non-empty form.
 */

public class Solution {

    /**
     * Computes the maximum possible number of healthy minutes after flipping exactly one
     * non-empty contiguous segment.
     *
     * The method works in two conceptual phases:
     * 1. Count how many healthy minutes already exist in the original array.
     * 2. Compute the best possible gain from flipping one contiguous segment.
     *
     * Gain rules for each element:
     * - status[i] == 0  => flipping changes 0 to 1, so gain = +1
     * - status[i] == 1  => flipping changes 1 to 0, so gain = -1
     *
     * Then we find the maximum sum non-empty subarray in this gain representation using
     * Kadane's algorithm. That maximum sum is the best improvement (or smallest damage if
     * all choices are bad).
     *
     * @param status the binary array representing battery health over time; 1 means healthy, 0 means low-battery
     * @return the maximum possible number of healthy minutes after exactly one non-empty segment flip
     * Time complexity: O(n), where n is the length of the array
     * Space complexity: O(1), ignoring input storage
     */
    public int maxHealthyMinutesAfterOneFlip(int[] status) {
        // Step 1:
        // Count how many minutes are already healthy in the original array.
        // This gives us the baseline answer before any flip is applied.
        int originalHealthyCount = 0;
        for (int value : status) {
            if (value == 1) {
                originalHealthyCount++;
            }
        }

        // Step 2:
        // We now run Kadane's algorithm on the implicit gain array.
        //
        // Instead of building a separate gain array, we compute each gain on the fly:
        // - If current value is 0, gain = +1
        // - If current value is 1, gain = -1
        //
        // Important:
        // The flip must be used exactly once and the chosen segment must be non-empty.
        // Therefore, we must find the maximum sum of a NON-EMPTY subarray.
        //
        // To enforce non-empty behavior correctly, initialize using the first element's gain.
        int firstGain = (status[0] == 0) ? 1 : -1;

        // currentMaxEndingHere:
        // The maximum gain of a non-empty subarray that MUST end at the current index.
        int currentMaxEndingHere = firstGain;

        // bestMaxSoFar:
        // The best gain of any non-empty subarray seen so far.
        int bestMaxSoFar = firstGain;

        // Step 3:
        // Process the rest of the array.
        for (int i = 1; i < status.length; i++) {
            int gain = (status[i] == 0) ? 1 : -1;

            // At each position, we have exactly two choices:
            //
            // Choice A: Start a brand-new subarray at index i.
            //           Then the best sum ending here is just "gain".
            //
            // Choice B: Extend the previous best subarray that ended at i - 1.
            //           Then the sum becomes currentMaxEndingHere + gain.
            //
            // We choose whichever is larger.
            currentMaxEndingHere = Math.max(gain, currentMaxEndingHere + gain);

            // Update the global best answer if the current ending subarray is better.
            bestMaxSoFar = Math.max(bestMaxSoFar, currentMaxEndingHere);
        }

        // Step 4:
        // The final answer is:
        // original number of healthy minutes + best gain from one required flip.
        return originalHealthyCount + bestMaxSoFar;
    }

    /**
     * A beginner-friendly helper method that prints the input array and the computed result.
     *
     * @param status the binary array to test
     * @return the computed maximum possible healthy minutes after exactly one flip
     * Time complexity: O(n), because it delegates to the main algorithm and also prints the array
     * Space complexity: O(1), ignoring output formatting internals
     */
    public int demonstrateCase(int[] status) {
        int result = maxHealthyMinutesAfterOneFlip(status);
        System.out.println("Input:  " + Arrays.toString(status));
        System.out.println("Output: " + result);
        System.out.println();
        return result;
    }

    /**
     * Main method to demonstrate the solution on the sample inputs from the problem statement
     * and a few additional test cases.
     *
     * @param args command-line arguments (not used)
     * @return nothing
     * Time complexity: O(total number of elements across demonstrated test cases)
     * Space complexity: O(1), ignoring output formatting internals
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Sample 1 from the problem statement:
        // status = [1,0,0,1,0,1]
        // Original healthy count = 3
        // Gain array = [-1,+1,+1,-1,+1,-1]
        // Best non-empty subarray sum = 2
        // Final answer = 3 + 2 = 5
        int[] sample1 = {1, 0, 0, 1, 0, 1};
        solution.demonstrateCase(sample1);

        // Sample 2 from the problem statement:
        // status = [1,1,1,1]
        // Original healthy count = 4
        // Gain array = [-1,-1,-1,-1]
        // Best non-empty subarray sum = -1
        // Final answer = 4 + (-1) = 3
        int[] sample2 = {1, 1, 1, 1};
        solution.demonstrateCase(sample2);

        // Additional test:
        // All zeros. Flipping the whole array is best.
        // status = [0,0,0]
        // Original healthy count = 0
        // Gain array = [+1,+1,+1]
        // Best sum = 3
        // Final answer = 3
        int[] test1 = {0, 0, 0};
        solution.demonstrateCase(test1);

        // Additional test:
        // Single element 0
        // Must flip it, so 0 -> 1
        // Answer = 1
        int[] test2 = {0};
        solution.demonstrateCase(test2);

        // Additional test:
        // Single element 1
        // Must flip it, so 1 -> 0
        // Answer = 0
        int[] test3 = {1};
        solution.demonstrateCase(test3);

        // Additional mixed test
        int[] test4 = {1, 0, 1, 0, 0, 1, 1, 0};
        solution.demonstrateCase(test4);
    }
}