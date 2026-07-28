import java.util.*;

/*
Problem Title: Maximum Revenue from Menu Bundles with Dish Reuse Fees

Problem Description:
A restaurant is preparing a fixed tasting menu over N evenings. On evening i, the chef must choose exactly one dish from two available options: standard[i] or premium[i]. If the chef serves the same dish type on consecutive evenings, customers feel less variety and a reuse fee is applied for that evening. Specifically, if the chef chooses the same type on both evening i - 1 and evening i, then revenue for evening i is reduced by fee[i].

You are given three integer arrays of length N:
- standard[i]: revenue earned if the standard dish is served on evening i
- premium[i]: revenue earned if the premium dish is served on evening i
- fee[i]: penalty applied only when the dish type on evening i matches the dish type chosen on evening i - 1

Return the maximum total revenue the restaurant can earn over all N evenings.

Notes:
- On the first evening, no reuse fee is ever applied.
- The reuse fee depends only on evening i, not on earlier history.
- Dish type means only standard or premium; the actual revenue values can be different each evening.

Constraints:
- 1 <= N <= 200000
- 0 <= standard[i], premium[i] <= 10^9
- 0 <= fee[i] <= 10^9
- Answer fits in a 64-bit signed integer

Examples:
1)
standard = [5, 6, 4, 7]
premium = [8, 3, 9, 2]
fee = [0, 4, 5, 3]
Correct output: 30

2)
standard = [10, 10, 1, 10]
premium = [1, 1, 20, 1]
fee = [0, 2, 8, 2]
Correct output: 48
*/

public class Solution {

    /**
     * Computes the maximum total revenue using dynamic programming with O(1) extra space.
     *
     * Core idea:
     * For each evening, we only need to know the best total revenue so far under two cases:
     * 1) the previous evening ended with choosing standard
     * 2) the previous evening ended with choosing premium
     *
     * Let:
     * - dpStandard = best total revenue up to current evening if current evening uses standard
     * - dpPremium  = best total revenue up to current evening if current evening uses premium
     *
     * Transition for evening i:
     * - If we choose standard today:
     *   a) yesterday was standard  -> pay fee[i]
     *   b) yesterday was premium   -> no fee
     *   So:
     *   newStandard = max(dpStandard + standard[i] - fee[i], dpPremium + standard[i])
     *
     * - If we choose premium today:
     *   a) yesterday was premium   -> pay fee[i]
     *   b) yesterday was standard  -> no fee
     *   So:
     *   newPremium = max(dpPremium + premium[i] - fee[i], dpStandard + premium[i])
     *
     * Base case:
     * On the first evening, no fee is applied.
     * Therefore:
     * - dpStandard = standard[0]
     * - dpPremium  = premium[0]
     *
     * Final answer:
     * max(dpStandard, dpPremium)
     *
     * @param standard revenue if standard dish is chosen on each evening
     * @param premium revenue if premium dish is chosen on each evening
     * @param fee reuse fee applied on evening i when the same dish type is chosen as on evening i - 1
     * @return maximum total revenue over all evenings
     * Time complexity: O(N)
     * Space complexity: O(1)
     */
    public long maxRevenue(int[] standard, int[] premium, int[] fee) {
        validateInput(standard, premium, fee);

        int n = standard.length;

        // Base case for evening 0:
        // If we end evening 0 with standard, total revenue is simply standard[0].
        long dpStandard = standard[0];

        // If we end evening 0 with premium, total revenue is simply premium[0].
        long dpPremium = premium[0];

        // Process each later evening one by one.
        for (int i = 1; i < n; i++) {
            // If we choose standard on evening i, there are exactly two ways to arrive here:
            //
            // 1) Previous evening also used standard:
            //    Then evening i pays the reuse fee.
            //    Revenue = previous best ending with standard + today's standard revenue - today's fee
            long continueStandard = dpStandard + standard[i] - fee[i];

            // 2) Previous evening used premium:
            //    Then no fee is paid because the dish type changes.
            //    Revenue = previous best ending with premium + today's standard revenue
            long switchToStandard = dpPremium + standard[i];

            // Best total if current evening ends with standard.
            long newStandard = Math.max(continueStandard, switchToStandard);

            // If we choose premium on evening i, again there are exactly two ways:
            //
            // 1) Previous evening also used premium:
            //    Pay the reuse fee.
            long continuePremium = dpPremium + premium[i] - fee[i];

            // 2) Previous evening used standard:
            //    No fee because the type changes.
            long switchToPremium = dpStandard + premium[i];

            // Best total if current evening ends with premium.
            long newPremium = Math.max(continuePremium, switchToPremium);

            // Move the DP window forward:
            // the "current" values become the "previous" values for the next iteration.
            dpStandard = newStandard;
            dpPremium = newPremium;
        }

        // After processing all evenings, the final evening can end with either type.
        return Math.max(dpStandard, dpPremium);
    }

    /**
     * Validates that the input arrays are non-null, non-empty, and have equal lengths.
     *
     * @param standard revenue if standard dish is chosen on each evening
     * @param premium revenue if premium dish is chosen on each evening
     * @param fee reuse fee applied on evening i when the same dish type is chosen as on evening i - 1
     * @return nothing
     * Time complexity: O(1)
     * Space complexity: O(1)
     */
    public void validateInput(int[] standard, int[] premium, int[] fee) {
        if (standard == null || premium == null || fee == null) {
            throw new IllegalArgumentException("Input arrays must not be null.");
        }
        if (standard.length == 0 || premium.length == 0 || fee.length == 0) {
            throw new IllegalArgumentException("Input arrays must not be empty.");
        }
        if (standard.length != premium.length || standard.length != fee.length) {
            throw new IllegalArgumentException("All input arrays must have the same length.");
        }
    }

    /**
     * Demonstrates the algorithm on the sample inputs from the problem statement.
     *
     * @param args command-line arguments (not used)
     * @return nothing
     * Time complexity: O(N) across the demonstrated examples
     * Space complexity: O(1) excluding input storage
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        int[] standard1 = {5, 6, 4, 7};
        int[] premium1 = {8, 3, 9, 2};
        int[] fee1 = {0, 4, 5, 3};
        long result1 = solution.maxRevenue(standard1, premium1, fee1);
        System.out.println("Example 1 result: " + result1);
        System.out.println("Expected: 30");

        int[] standard2 = {10, 10, 1, 10};
        int[] premium2 = {1, 1, 20, 1};
        int[] fee2 = {0, 2, 8, 2};
        long result2 = solution.maxRevenue(standard2, premium2, fee2);
        System.out.println("Example 2 result: " + result2);
        System.out.println("Expected: 48");

        int[] standard3 = {7};
        int[] premium3 = {9};
        int[] fee3 = {0};
        long result3 = solution.maxRevenue(standard3, premium3, fee3);
        System.out.println("Single evening result: " + result3);
        System.out.println("Expected: 9");
    }
}