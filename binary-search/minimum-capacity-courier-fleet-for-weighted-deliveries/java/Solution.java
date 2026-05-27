```java
/*
 * Title: Minimum Capacity Courier Fleet for Weighted Deliveries
 *
 * Problem Description:
 * A logistics company needs to deliver n packages arranged in a fixed route order.
 * Each package has a weight given by weights[i]. You have a fleet of k couriers,
 * and each courier must take a contiguous segment of packages from the route
 * (i.e., the packages cannot be reordered). All k couriers operate simultaneously,
 * and each courier's load is the sum of weights of the packages assigned to them.
 *
 * However, there is an additional constraint: each courier can only carry packages
 * if the number of packages assigned to them does not exceed a given limit maxPkgs.
 * In other words, every contiguous segment assigned to a courier must contain at
 * most maxPkgs packages.
 *
 * Your goal is to minimize the maximum load carried by any single courier, while ensuring:
 * 1. All packages are delivered.
 * 2. Each courier receives a contiguous, non-empty segment.
 * 3. No courier carries more than maxPkgs packages.
 *
 * Return the minimum possible value of the maximum load among all couriers.
 * If it is impossible to partition the packages into exactly k non-empty contiguous
 * segments each with at most maxPkgs packages, return -1.
 *
 * Constraints:
 * - 1 <= n <= 10^5
 * - 1 <= k <= n
 * - 1 <= maxPkgs <= n
 * - 1 <= weights[i] <= 10^4
 */

import java.util.*;

/**
 * Solution class for the Minimum Capacity Courier Fleet problem.
 *
 * <p>Approach: Binary Search on the answer.
 * We binary search on the "maximum load" capacity. For a given candidate capacity,
 * we check if it's feasible to split the packages into exactly k contiguous segments
 * where each segment has sum <= capacity AND count <= maxPkgs.
 *
 * <p>The feasibility check uses a greedy approach combined with dynamic programming
 * to determine if we can partition into exactly k segments.
 */
public class Solution {

    /**
     * Finds the minimum possible maximum load for the courier fleet.
     *
     * <p>Algorithm Overview:
     * 1. First, check basic feasibility (impossible cases).
     * 2. Binary search on the answer (the maximum load capacity).
     * 3. For each candidate capacity, check if exactly k valid segments are achievable.
     *
     * @param weights  array of package weights in route order
     * @param k        number of couriers (must partition into exactly k segments)
     * @param maxPkgs  maximum number of packages any single courier can carry
     * @return the minimum possible maximum load, or -1 if impossible
     *
     * Time Complexity: O(n * log(sum(weights))) where n = weights.length
     *   - Binary search runs O(log(totalSum)) iterations
     *   - Each feasibility check runs in O(n)
     * Space Complexity: O(n) for prefix sums
     */
    public int minCapacity(int[] weights, int k, int maxPkgs) {
        int n = weights.length;

        // -----------------------------------------------------------------------
        // Step 1: Check basic impossibility conditions
        // -----------------------------------------------------------------------

        // Condition A: We need exactly k non-empty segments from n packages.
        // Each segment must have at least 1 package, so we need n >= k.
        // Also, each segment can have at most maxPkgs packages, so k * maxPkgs >= n.
        // If k > n: impossible (can't have k non-empty segments from n < k packages)
        if (k > n) {
            return -1;
        }

        // If k * maxPkgs < n: impossible (even if all couriers carry maxPkgs packages,
        // we can't cover all n packages)
        if ((long) k * maxPkgs < n) {
            return -1;
        }

        // -----------------------------------------------------------------------
        // Step 2: Build prefix sums for efficient range sum queries
        // prefix[i] = sum of weights[0..i-1]
        // So sum of weights[l..r] = prefix[r+1] - prefix[l]
        // -----------------------------------------------------------------------
        long[] prefix = new long[n + 1];
        for (int i = 0; i < n; i++) {
            prefix[i + 1] = prefix[i] + weights[i];
        }
        long totalSum = prefix[n];

        // -----------------------------------------------------------------------
        // Step 3: Determine binary search bounds
        // Lower bound: the maximum single package weight (a courier must carry at
        //              least one package, so capacity >= max(weights[i]))
        //              Also, we need at least ceil(n/k) packages per courier on average,
        //              but the minimum capacity is at least max(weights[i]).
        // Upper bound: the total sum of all packages (one courier takes everything)
        // -----------------------------------------------------------------------
        long lo = 0;
        for (int w : weights) {
            lo = Math.max(lo, w); // minimum possible capacity is max single weight
        }
        long hi = totalSum; // maximum possible capacity

        // -----------------------------------------------------------------------
        // Step 4: Binary search on the answer
        // We want the smallest capacity such that isFeasible(capacity, ...) is true
        // -----------------------------------------------------------------------
        long result = -1;

        while (lo <= hi) {
            long mid = lo + (hi - lo) / 2;

            // Check if we can partition into exactly k valid segments with this capacity
            if (isFeasible(weights, prefix, n, k, maxPkgs, mid)) {
                // This capacity works; try to find a smaller one
                result = mid;
                hi = mid - 1;
            } else {
                // This capacity doesn't work; need a larger one
                lo = mid + 1;
            }
        }

        return (int) result;
    }

    /**
     * Checks whether it is feasible to partition the packages into exactly k
     * contiguous segments such that each segment has:
     * - sum of weights <= capacity
     * - number of packages <= maxPkgs
     *
     * <p>Approach: We use a greedy check to find the minimum and maximum number
     * of valid segments we can create. If k falls within [minSegments, maxSegments],
     * then it's feasible.
     *
     * <p>More precisely:
     * - minSegments = minimum number of segments needed (greedy: make each segment
     *   as large as possible)
     * - maxSegments = maximum number of segments possible (greedy: make each segment
     *   as small as possible, i.e., 1 package each, but limited by capacity)
     *
     * If minSegments <= k <= maxSegments, return true.
     *
     * @param weights   array of package weights
     * @param prefix    prefix sum array where prefix[i] = sum of weights[0..i-1]
     * @param n         number of packages
     * @param k         required number of segments
     * @param maxPkgs   maximum packages per segment
     * @param capacity  the candidate maximum load capacity
     * @return true if exactly k segments are achievable with this capacity
     *
     * Time Complexity: O(n) - two linear passes
     * Space Complexity: O(1) extra space
     */
    private boolean isFeasible(int[] weights, long[] prefix, int n, int k, int maxPkgs, long capacity) {

        // -----------------------------------------------------------------------
        // First, verify that every individual package weight <= capacity.
        // If any single package exceeds capacity, it's impossible regardless of k.
        // -----------------------------------------------------------------------
        // (We already set lo = max(weights), so if capacity >= lo, this is satisfied.
        //  But let's be safe and check explicitly.)
        for (int w : weights) {
            if (w > capacity) {
                return false;
            }
        }

        // -----------------------------------------------------------------------
        // Compute minSegments: minimum number of segments needed.
        // Greedy: extend each segment as far as possible (up to maxPkgs packages
        // and sum <= capacity). Count how many segments we need.
        // -----------------------------------------------------------------------
        int minSegments = 0;
        int i = 0;
        while (i < n) {
            minSegments++;
            // Try to extend this segment as far as possible
            int count = 0;
            long segSum = 0;
            while (i < n && count < maxPkgs && segSum + weights[i] <= capacity) {
                segSum += weights[i];
                count++;
                i++;
            }
            // If we couldn't add even one package (shouldn't happen since we checked
            // individual weights above), return false
            if (count == 0) {
                return false;
            }
        }

        // -----------------------------------------------------------------------
        // Compute maxSegments: maximum number of segments possible.
        // Greedy: make each segment as small as possible (1 package each),
        // but each package must fit within capacity (already verified above).
        // So maxSegments = n (each package is its own segment), but we need
        // to check that each single package fits within capacity.
        // Since we verified all weights[i] <= capacity, maxSegments = n.
        // -----------------------------------------------------------------------
        // However, we also need to ensure that we can actually split into k segments
        // where each segment is non-empty. Since maxSegments = n and minSegments is
        // computed above, we just check minSegments <= k <= n.
        // But wait: we also need k <= n (already checked in the main method).
        int maxSegments = n; // each package in its own segment (all fit since weights[i] <= capacity)

        // -----------------------------------------------------------------------
        // The answer is feasible if k is between minSegments and maxSegments.
        // - If k < minSegments: we can't cover all packages with only k segments
        //   (each segment would need to exceed capacity or maxPkgs).
        // - If k > maxSegments: we can't split into more than n segments.
        // -----------------------------------------------------------------------
        return minSegments <= k && k <= maxSegments;
    }

    /**
     * Main method to demonstrate the solution with sample inputs.
     *
     * @param args command-line arguments (not used)
     */
    public static void main(String[] args) {
        Solution sol = new Solution();

        // -----------------------------------------------------------------------
        // Example 1:
        // weights = [3, 2, 8, 5, 1, 7, 4], k = 3, maxPkgs = 4
        // Expected Output: 13
        //
        // Explanation:
        // One optimal partition: [3,2,8] | [5,1,7] | [4]
        // Loads: 13, 13, 4 → Maximum = 13
        //
        // Let's verify with our algorithm:
        // - lo = max(3,2,8,5,1,7,4) = 8
        // - hi = 3+2+8+5+1+7+4 = 30
        // - Binary search will find capacity = 13
        //   isFeasible(13, maxPkgs=4):
        //     minSegments: start i=0
        //       seg1: add 3(sum=3,cnt=1), add 2(sum=5,cnt=2), add 8(sum=13,cnt=3),
        //             can't add 5 (sum=18>13), so seg1=[3,2,8], i=3
        //       seg2: add 5(sum=5,cnt=1), add 1(sum=6,cnt=2), add 7(sum=13,cnt=3),
        //             can't add 4 (sum=17>13), so seg2=[5,1,7], i=6
        //       seg3: add 4(sum=4,cnt=1), i=7, done
        //       minSegments = 3
        //     maxSegments = 7
        //     3 <= 3 <= 7 → feasible!
        //   isFeasible(12, maxPkgs=4):
        //     minSegments: start i=0
        //       seg1: add 3(sum=3,cnt=1), add 2(sum=5,cnt=2), add 8(sum=13>12 NO)
        //             Wait: 5+8=13>12, so can't add 8.
        //             seg1=[3,2], i=2
        //       seg2: add 8(sum=8,cnt=1), add 5(sum=13>12 NO)
        //             seg2=[8], i=3
        //       seg3: add 5(sum=5,cnt=1), add 1(sum=6,cnt=2), add 7(sum=13>12 NO)
        //             seg3=[5,1], i=5
        //       seg4: add 7(sum=7,cnt=1), add 4(sum=11,cnt=2), i=7, done
        //       minSegments = 4
        //     4 <= 3? NO → not feasible
        //   So minimum feasible capacity = 13 ✓
        // -----------------------------------------------------------------------
        int[] weights1 = {3, 2, 8, 5, 1, 7, 4};
        int k1 = 3;
        int maxPkgs1 = 4;
        int result1 = sol.minCapacity(weights1, k1, maxPkgs1);
        System.out.println("Example 1:");
        System.out.println("  weights = [3, 2, 8, 5, 1, 7, 4], k = 3, maxPkgs = 4");
        System.out.println("  Expected: 13");
        System.out.println("  Got:      " + result1);
        System.out.println();

        // -----------------------------------------------------------------------
        // Example 2:
        // weights = [10, 10, 10, 10, 10], k = 3, maxPkgs = 1
        // Expected Output: -1
        //
        // Explanation:
        // With maxPkgs = 1, each courier carries exactly 1 package.
        // 3 couriers can cover at most 3 packages, but there are 5.
        // k * maxPkgs = 3 * 1 = 3 < 5 = n → impossible → return -1
        // -----------------------------------------------------------------------
        int[] weights2 = {10, 10, 10, 10, 10};
        int k2 = 3;
        int maxPkgs2 = 1;
        int result2 = sol.minCapacity(weights2, k2, maxPkgs2);
        System.out.println("Example 2:");
        System.out.println("  weights = [10, 10, 10, 10, 10], k = 3, maxPkgs = 1");
        System.out.println("  Expected: -1");
        System.out.println("  Got:      " + result2);
        System.out.println();

        // -----------------------------------------------------------------------
        // Additional Example 3:
        // weights = [1, 2, 3, 4, 5], k = 2, maxPkgs = 3
        // n=5, k=2, maxPkgs=3
        // k*maxPkgs = 6 >= 5, k=2 <= n=5, so feasible check needed.
        //
        // lo = 5, hi = 15
        // Binary search:
        //   mid=10: isFeasible(10, maxPkgs=3)
        //     minSegments: seg1: 1+2+3=6<=10,cnt=3; can't add 4 (cnt=3=maxPkgs)
        //                  i=3, seg2: 4+5=9<=10,cnt=2; i=5, done
        //                  minSegments=2
        //     2<=2<=5 → feasible, result=10, hi=9
        //   mid=7: isFeasible(7, maxPkgs=3)
        //     minSegments: seg1: 1+2+3=6<=7,cnt=3; can't add 4 (cnt=3=maxPkgs)
        //                