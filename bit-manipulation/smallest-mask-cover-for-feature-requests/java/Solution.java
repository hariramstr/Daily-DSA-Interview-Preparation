import java.util.*;

/*
 * Title: Smallest Mask Cover for Feature Requests
 * Difficulty: Medium
 * Topic: Bit Manipulation
 *
 * Problem Description:
 * A product team tracks each customer request as a non-negative integer bitmask.
 * In a mask, the i-th bit is 1 if the request needs feature i, and 0 otherwise.
 * Given an array requests, you must choose exactly one request mask x from the array
 * and measure how many extra feature bits need to be turned on so that x can cover
 * every request in the array.
 *
 * A mask y covers a request r if every bit set in r is also set in y.
 * In other words, r is covered when (r & y) == r.
 * Starting from a chosen x, you are allowed to turn on additional bits but never
 * turn bits off. The cost of choosing x is the minimum number of bit positions you
 * must turn on so that the resulting upgraded mask covers all requests.
 *
 * Return the minimum possible cost over all choices of x.
 *
 * Equivalently, if OR is the bitwise OR of all values in requests, then for a chosen x
 * the required cost is the number of 1-bits in (OR ^ x), assuming x is already a subset
 * of OR, which is always true because x comes from the array.
 *
 * Constraints:
 * - 1 <= requests.length <= 100000
 * - 0 <= requests[i] <= 10^9
 * - requests contains at least one value
 *
 * Example 1:
 * Input: requests = [5, 1, 7]
 * Output: 0
 * Explanation:
 * The bitwise OR of all requests is 7 (111 in binary).
 * Since 7 already appears in the array, choosing x = 7 requires turning on 0 extra bits.
 *
 * Example 2:
 * Input: requests = [10, 12, 8]
 * Output: 1
 * Explanation:
 * The OR of all requests is 14 (1110 in binary).
 * Choosing x = 12 (1100) requires turning on only bit 1 to reach 14, so the cost is 1.
 * Choosing 10 or 8 would require turning on 1 or 2 extra bits respectively, and the minimum is 1.
 */

public class Solution {

    /**
     * Computes the minimum number of extra bits that must be turned on when choosing
     * exactly one request mask from the array so that the upgraded mask covers all requests.
     *
     * Core idea:
     * 1. Compute the bitwise OR of all request masks. This OR contains every feature bit
     *    that is required by at least one request.
     * 2. For any chosen mask x from the array, the only bits we ever need to add are the
     *    bits that are present in OR but missing in x.
     * 3. Because x comes from the array, every 1-bit in x is already part of OR, so
     *    x is always a subset of OR in terms of set bits.
     * 4. Therefore, the cost for x is simply the number of set bits in (OR ^ x),
     *    which is the same as the number of set bits in (OR & ~x).
     * 5. We evaluate this cost for every x and return the minimum.
     *
     * @param requests the array of non-negative integer bitmasks representing feature requests
     * @return the minimum possible number of extra bits needed
     * Time complexity: O(n), where n is requests.length
     * Space complexity: O(1), ignoring input storage
     */
    public int minimumCost(int[] requests) {
        // Step 1:
        // Compute the global OR of all request masks.
        //
        // Why does this help?
        // The final upgraded mask must cover every request.
        // That means if any request has a certain bit set, the final mask must also have that bit set.
        // Therefore, the smallest possible mask that can cover all requests is exactly the OR of all requests.
        int globalOr = computeGlobalOr(requests);

        // Step 2:
        // Try each request mask as the starting choice x.
        // For each x, count how many bits are missing compared to globalOr.
        //
        // Since x is from the array, x cannot contain any 1-bit that is not already in globalOr.
        // So the bits we need to turn on are exactly the bits in globalOr that are 0 in x.
        int answer = Integer.MAX_VALUE;

        for (int request : requests) {
            // Step 3:
            // Compute the missing bits.
            //
            // Because request is a subset of globalOr, XOR here marks exactly the bits
            // that differ, which are precisely the bits that must be turned on.
            int missingBitsMask = globalOr ^ request;

            // Step 4:
            // Count how many 1-bits are in missingBitsMask.
            // Each 1-bit corresponds to one feature bit we must turn on.
            int cost = Integer.bitCount(missingBitsMask);

            // Step 5:
            // Keep the minimum cost across all choices.
            if (cost < answer) {
                answer = cost;
            }
        }

        return answer;
    }

    /**
     * Computes the bitwise OR of all values in the array.
     *
     * @param requests the array of request masks
     * @return the bitwise OR of all request masks
     * Time complexity: O(n), where n is requests.length
     * Space complexity: O(1)
     */
    public int computeGlobalOr(int[] requests) {
        int globalOr = 0;

        // We accumulate all required feature bits.
        // If a bit appears in any request, it will be set in globalOr.
        for (int request : requests) {
            globalOr |= request;
        }

        return globalOr;
    }

    /**
     * Computes the cost of choosing a specific request mask as the starting point.
     *
     * The cost is the number of bits that must be turned on so that the chosen mask
     * becomes equal to the global OR mask, which is the smallest mask that covers all requests.
     *
     * @param chosenMask the selected request mask from the array
     * @param globalOr the bitwise OR of all request masks
     * @return the number of extra bits that must be turned on
     * Time complexity: O(1)
     * Space complexity: O(1)
     */
    public int costForMask(int chosenMask, int globalOr) {
        // The differing bits are exactly the missing required bits.
        return Integer.bitCount(globalOr ^ chosenMask);
    }

    /**
     * Demonstrates the solution on the sample inputs from the problem statement
     * and prints the results.
     *
     * @param args command-line arguments (not used)
     * @return nothing
     * Time complexity: O(1) for the fixed demo inputs
     * Space complexity: O(1)
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Sample 1:
        // requests = [5, 1, 7]
        //
        // Binary:
        // 5 = 101
        // 1 = 001
        // 7 = 111
        //
        // global OR = 111 = 7
        // Choosing 7 means no extra bits are needed.
        int[] requests1 = {5, 1, 7};
        int result1 = solution.minimumCost(requests1);
        System.out.println(result1); // Expected: 0

        // Sample 2:
        // requests = [10, 12, 8]
        //
        // Binary:
        // 10 = 1010
        // 12 = 1100
        //  8 = 1000
        //
        // global OR = 1110 = 14
        // cost(10) = bitCount(1110 ^ 1010) = bitCount(0100) = 1
        // cost(12) = bitCount(1110 ^ 1100) = bitCount(0010) = 1
        // cost(8)  = bitCount(1110 ^ 1000) = bitCount(0110) = 2
        // minimum = 1
        int[] requests2 = {10, 12, 8};
        int result2 = solution.minimumCost(requests2);
        System.out.println(result2); // Expected: 1

        // Additional quick checks for beginners:

        // Single element:
        // OR is the element itself, so cost is always 0.
        int[] requests3 = {0};
        System.out.println(solution.minimumCost(requests3)); // Expected: 0

        int[] requests4 = {3};
        System.out.println(solution.minimumCost(requests4)); // Expected: 0

        // Another example:
        // requests = [1, 2, 4]
        // global OR = 7 (111)
        // each individual mask has only one of the three bits, so each needs 2 more bits
        int[] requests5 = {1, 2, 4};
        System.out.println(solution.minimumCost(requests5)); // Expected: 2
    }
}