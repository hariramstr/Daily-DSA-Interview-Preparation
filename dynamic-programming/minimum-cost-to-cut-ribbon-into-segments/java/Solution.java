```java
/*
 * Title: Minimum Cost to Cut a Ribbon into Segments
 *
 * Problem Description:
 * You have a ribbon of length n and an array cuts where cuts[i] represents a position
 * along the ribbon where a cut can be made. Each cut at position cuts[i] has an associated
 * cost costs[i]. You want to divide the ribbon into exactly k segments by making exactly
 * k-1 cuts.
 *
 * The cost of making a cut depends only on the specific cut chosen (not on the order in
 * which cuts are made). Your goal is to select exactly k-1 cuts from the available options
 * such that the total cost is minimized.
 *
 * Return the minimum total cost to divide the ribbon into exactly k segments.
 * If it is impossible to make exactly k-1 cuts (i.e., there are fewer than k-1 available
 * cut positions), return -1.
 *
 * Constraints:
 * - 2 <= k <= 100
 * - 1 <= cuts.length <= 300
 * - cuts.length == costs.length
 * - All positions in cuts are distinct.
 * - 1 <= costs[i] <= 10^4
 *
 * Example 1:
 * Input: cuts = [2, 5, 7, 9], costs = [3, 8, 2, 6], k = 3
 * Output: 5
 * Explanation: Make 2 cuts to get 3 segments. Choose cut at position 2 (cost 3) and
 * cut at position 7 (cost 2). Total cost = 3 + 2 = 5.
 *
 * Example 2:
 * Input: cuts = [1, 4, 6], costs = [10, 5, 7], k = 4
 * Output: 22
 * Explanation: We need 3 cuts to create 4 segments, and we have exactly 3 cuts available.
 * We must use all of them. Total cost = 10 + 5 + 7 = 22.
 */

import java.util.Arrays;

/**
 * Solution class for the "Minimum Cost to Cut a Ribbon into Segments" problem.
 *
 * <p>Approach: This is a classic "select k-1 items with minimum total cost" problem.
 * Since we simply need to choose exactly k-1 cuts from the available cuts array
 * (order doesn't matter, each cut has a fixed cost), the optimal strategy is:
 * 1. Check if we have at least k-1 cuts available. If not, return -1.
 * 2. Sort the costs in ascending order.
 * 3. Pick the k-1 smallest costs and sum them up.
 *
 * This greedy approach works because:
 * - Each cut is independent (cost doesn't depend on order or neighbors).
 * - We want to minimize total cost, so we always prefer cheaper cuts.
 * - We need exactly k-1 cuts, so we pick the k-1 cheapest ones.
 */
public class Solution {

    /**
     * Finds the minimum cost to divide a ribbon into exactly k segments
     * by selecting exactly k-1 cuts from the available cut positions.
     *
     * <p>Algorithm:
     * 1. If the number of available cuts is less than k-1, return -1 (impossible).
     * 2. Sort the costs array in ascending order.
     * 3. Sum the first k-1 elements (the k-1 smallest costs).
     * 4. Return the sum.
     *
     * @param cuts  array of cut positions along the ribbon (not directly used in greedy,
     *              but represents where cuts can be made)
     * @param costs array of costs associated with each cut position (costs[i] is the
     *              cost of making the cut at position cuts[i])
     * @param k     the desired number of segments (we need exactly k-1 cuts)
     * @return the minimum total cost to create exactly k segments, or -1 if impossible
     *
     * Time Complexity: O(m log m) where m = costs.length, due to sorting
     * Space Complexity: O(m) for the sorted copy of costs array
     */
    public int minCostToCutRibbon(int[] cuts, int[] costs, int k) {
        // Step 1: Determine how many cuts we need to make.
        // To divide a ribbon into k segments, we need exactly k-1 cuts.
        int cutsNeeded = k - 1;

        // Step 2: Check feasibility.
        // If we have fewer available cut positions than cuts needed, it's impossible.
        // Example: if k=5 but we only have 3 cut positions, we can't make 4 cuts.
        if (costs.length < cutsNeeded) {
            // Not enough cut positions available — return -1 to indicate impossibility.
            return -1;
        }

        // Step 3: Handle the edge case where k=1.
        // If k=1, we need 0 cuts, so the cost is 0 (no cuts needed).
        if (cutsNeeded == 0) {
            return 0;
        }

        // Step 4: Create a copy of the costs array to sort without modifying the original.
        // We sort costs in ascending order so we can easily pick the smallest ones.
        int[] sortedCosts = Arrays.copyOf(costs, costs.length);
        Arrays.sort(sortedCosts); // O(m log m) sorting

        // After sorting, sortedCosts looks like: [smallest, ..., largest]
        // For Example 1: costs = [3, 8, 2, 6] → sorted = [2, 3, 6, 8]
        // For Example 2: costs = [10, 5, 7] → sorted = [5, 7, 10]

        // Step 5: Sum the k-1 smallest costs.
        // Since the array is sorted in ascending order, the first k-1 elements
        // are the k-1 cheapest cuts — exactly what we want to minimize total cost.
        int totalCost = 0;
        for (int i = 0; i < cutsNeeded; i++) {
            // Add the i-th smallest cost to our running total.
            // For Example 1 (k=3, cutsNeeded=2): add sortedCosts[0]=2 and sortedCosts[1]=3 → total=5
            // For Example 2 (k=4, cutsNeeded=3): add sortedCosts[0]=5, [1]=7, [2]=10 → total=22
            totalCost += sortedCosts[i];
        }

        // Step 6: Return the minimum total cost.
        return totalCost;
    }

    /**
     * Alternative implementation using a more explicit step-by-step approach
     * with detailed intermediate output for educational purposes.
     *
     * @param cuts  array of cut positions along the ribbon
     * @param costs array of costs for each cut position
     * @param k     desired number of segments
     * @return minimum total cost, or -1 if impossible
     *
     * Time Complexity: O(m log m) where m = costs.length
     * Space Complexity: O(m) for the sorted costs array
     */
    public int minCostToCutRibbonVerbose(int[] cuts, int[] costs, int k) {
        int m = costs.length; // Total number of available cut positions
        int cutsNeeded = k - 1; // Number of cuts required to make k segments

        System.out.println("  [Verbose] Total available cuts: " + m);
        System.out.println("  [Verbose] Cuts needed (k-1): " + cutsNeeded);

        // Feasibility check: need at least k-1 cuts available
        if (m < cutsNeeded) {
            System.out.println("  [Verbose] Not enough cuts available! Returning -1.");
            return -1;
        }

        // Special case: no cuts needed
        if (cutsNeeded == 0) {
            System.out.println("  [Verbose] No cuts needed (k=1). Cost = 0.");
            return 0;
        }

        // Sort costs to find the cheapest options
        int[] sortedCosts = Arrays.copyOf(costs, m);
        Arrays.sort(sortedCosts);
        System.out.println("  [Verbose] Sorted costs: " + Arrays.toString(sortedCosts));

        // Pick the k-1 cheapest cuts
        int totalCost = 0;
        System.out.print("  [Verbose] Selecting cheapest " + cutsNeeded + " cuts: ");
        for (int i = 0; i < cutsNeeded; i++) {
            System.out.print(sortedCosts[i] + (i < cutsNeeded - 1 ? " + " : ""));
            totalCost += sortedCosts[i];
        }
        System.out.println(" = " + totalCost);

        return totalCost;
    }

    /**
     * Main method to demonstrate the solution with sample inputs.
     * Traces through each example from the problem description to verify correctness.
     *
     * @param args command-line arguments (not used)
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        System.out.println("=== Minimum Cost to Cut a Ribbon into Segments ===");
        System.out.println();

        // -----------------------------------------------------------------------
        // Example 1:
        // cuts = [2, 5, 7, 9], costs = [3, 8, 2, 6], k = 3
        // Expected Output: 5
        // Explanation:
        //   We need k-1 = 2 cuts.
        //   Available costs: [3, 8, 2, 6]
        //   Sorted costs: [2, 3, 6, 8]
        //   Pick 2 cheapest: 2 + 3 = 5
        //   These correspond to cut at position 7 (cost 2) and cut at position 2 (cost 3).
        // -----------------------------------------------------------------------
        System.out.println("--- Example 1 ---");
        int[] cuts1 = {2, 5, 7, 9};
        int[] costs1 = {3, 8, 2, 6};
        int k1 = 3;
        System.out.println("Input: cuts = " + Arrays.toString(cuts1)
                + ", costs = " + Arrays.toString(costs1) + ", k = " + k1);
        int result1 = solution.minCostToCutRibbon(cuts1, costs1, k1);
        System.out.println("Output: " + result1);
        System.out.println("Expected: 5");
        System.out.println("Correct: " + (result1 == 5));
        System.out.println();

        // Verbose trace for Example 1
        System.out.println("--- Example 1 (Verbose Trace) ---");
        solution.minCostToCutRibbonVerbose(cuts1, costs1, k1);
        System.out.println();

        // -----------------------------------------------------------------------
        // Example 2:
        // cuts = [1, 4, 6], costs = [10, 5, 7], k = 4
        // Expected Output: 22
        // Explanation:
        //   We need k-1 = 3 cuts.
        //   Available cuts: 3 (exactly enough).
        //   Sorted costs: [5, 7, 10]
        //   Pick all 3: 5 + 7 + 10 = 22
        // -----------------------------------------------------------------------
        System.out.println("--- Example 2 ---");
        int[] cuts2 = {1, 4, 6};
        int[] costs2 = {10, 5, 7};
        int k2 = 4;
        System.out.println("Input: cuts = " + Arrays.toString(cuts2)
                + ", costs = " + Arrays.toString(costs2) + ", k = " + k2);
        int result2 = solution.minCostToCutRibbon(cuts2, costs2, k2);
        System.out.println("Output: " + result2);
        System.out.println("Expected: 22");
        System.out.println("Correct: " + (result2 == 22));
        System.out.println();

        // Verbose trace for Example 2
        System.out.println("--- Example 2 (Verbose Trace) ---");
        solution.minCostToCutRibbonVerbose(cuts2, costs2, k2);
        System.out.println();

        // -----------------------------------------------------------------------
        // Example 3: Impossible case
        // cuts = [1, 3], costs = [5, 8], k = 5
        // Expected Output: -1
        // Explanation:
        //   We need k-1 = 4 cuts, but only 2 are available.
        //   Return -1.
        // -----------------------------------------------------------------------
        System.out.println("--- Example 3 (Impossible Case) ---");
        int[] cuts3 = {1, 3};
        int[] costs3 = {5, 8};
        int k3 = 5;
        System.out.println("Input: cuts = " + Arrays.toString(cuts3)
                + ", costs = " + Arrays.toString(costs3) + ", k = " + k3);
        int result3 = solution.minCostToCutRibbon(cuts3, costs3, k3);
        System.out.println("Output: " + result3);
        System.out.println("Expected: -1");
        System.out.println("Correct: " + (result3 == -1));
        System.out.println();

        // Verbose trace for Example 3
        System.out.println("--- Example 3 (Verbose Trace) ---");
        solution.minCostToCutRibbonVerbose(cuts3, costs3, k3);
        System.out.println();

        // -----------------------------------------------------------------------
        // Example 4: k = 2 (only one cut needed)
        // cuts = [3, 7, 10], costs = [100, 1, 50], k = 2
        // Expected Output: 1
        // Explanation:
        //   We need k-1 = 1 cut.
        //   Sorted costs: [1, 50, 100]
        //   Pick cheapest 1: cost = 1 (cut at position 7)
        // -----------------------------------------------------------------------
        System.out.println("--- Example 4 (Single Cut) ---");
        int[] cuts4 = {3, 7, 10};
        int[] costs4 = {100, 1, 50};
        int k4 = 2;
        System.out.println("Input: cuts = " + Arrays.toString(cuts4)
                + ", costs = " + Arrays.toString(costs4) + ", k = " + k4);
        int result4 = solution.minCostToCutRibbon(cuts4, costs4, k4);
        System.out.println("Output: " + result4);
        System.out.println("Expected: 1");
        System.out.println("Correct: " + (result4 == 1));
        System.out.println();

        // Verbose trace for Example 4
        System.out.println("--- Example 4 (Verbose Trace) ---");
        solution.minCostToCutRibbonVerbose(cuts4, costs4, k4);
        System.out.println();

        // -----------------------------------------------------------------------
        // Example 5: Large costs, many cuts
        // cuts = [1,2,3,4,5], costs = [10,20,5,15,8], k = 3
        // Expected Output: 13
        // Explanation:
        //   We need k-1 = 2 cuts.
        //   Sorted costs: [5, 8, 10, 15, 20]
        //   Pick 2 cheapest: 5 + 8 = 13
        // -----------------------------------------------------------------------
        System.out.println("--- Example 5 (Multiple Cuts Available) ---");
        int[] cuts5 = {1, 2, 3, 4, 5};
        int[] costs5 