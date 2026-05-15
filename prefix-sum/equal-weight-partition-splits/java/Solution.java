```java
/*
 * Title: Equal Weight Partition Splits
 * Difficulty: Medium
 * Topic: Prefix Sum
 *
 * Problem Description:
 * You are given an array of positive integers `weights` representing the weights of items
 * arranged in a line. You want to split the array into exactly three non-empty contiguous
 * subarrays such that the sum of weights in each subarray is equal.
 *
 * Return the number of ways you can make such a split.
 *
 * A split is defined by choosing two cut points i and j where 0 < i < j < n (0-based indexing),
 * such that:
 *   - The first subarray is weights[0..i-1]
 *   - The second subarray is weights[i..j-1]
 *   - The third subarray is weights[j..n-1]
 *
 * All three subarrays must have the same sum.
 *
 * Constraints:
 *   - 3 <= weights.length <= 10^5
 *   - 1 <= weights[i] <= 10^4
 *
 * Example 1:
 *   Input: weights = [1, 2, 3, 0, 3]
 *   Output: 2
 *   Explanation: Total sum = 9, each part must sum to 3.
 *     Split 1: [1,2] | [3] | [0,3]
 *     Split 2: [1,2] | [3,0] | [3]
 *
 * Example 2:
 *   Input: weights = [1, 1, 1, 1, 1, 1]
 *   Output: 4
 *   Explanation: Total sum = 6, each part must sum to 2.
 *
 * Example 3:
 *   Input: weights = [1, 2, 4]
 *   Output: 0
 *   Explanation: Total sum = 7, not divisible by 3, so no valid split exists.
 */

import java.util.*;

/**
 * Solution class for the Equal Weight Partition Splits problem.
 *
 * <p>Key Insight:
 * If the total sum is S, each of the three parts must sum to S/3.
 * We use prefix sums to efficiently find valid split points.
 *
 * <p>Algorithm Overview:
 * 1. Compute the total sum. If not divisible by 3, return 0.
 * 2. Each part must equal target = totalSum / 3.
 * 3. Scan through the array tracking the running prefix sum.
 *    - When prefix sum equals target (end of first part), count valid first-cut positions.
 *    - When prefix sum equals 2*target (end of second part), add the count of valid
 *      first-cut positions to the answer (each first-cut pairs with this second-cut).
 */
public class Solution {

    /**
     * Counts the number of ways to split the weights array into exactly three
     * non-empty contiguous subarrays with equal sums.
     *
     * <p>Algorithm:
     * We use a single pass with prefix sums. The key observation is:
     * - If total sum S is not divisible by 3, answer is 0.
     * - Otherwise, target = S/3. We need prefix[i] = target and prefix[j] = 2*target.
     * - For each valid j (where prefix sum = 2*target), we add the number of valid i
     *   positions seen so far (where prefix sum = target).
     *
     * @param weights array of positive integers representing item weights
     * @return the number of valid three-way equal-sum splits
     *
     * Time Complexity: O(n) — single pass through the array
     * Space Complexity: O(1) — only a few integer variables used
     */
    public int countEqualWeightSplits(int[] weights) {
        int n = weights.length;

        // ---------------------------------------------------------------
        // Step 1: Compute the total sum of all weights.
        // ---------------------------------------------------------------
        int totalSum = 0;
        for (int w : weights) {
            totalSum += w;
        }

        // ---------------------------------------------------------------
        // Step 2: Check divisibility.
        // If totalSum is not divisible by 3, it's impossible to split into
        // three equal parts, so return 0 immediately.
        // ---------------------------------------------------------------
        if (totalSum % 3 != 0) {
            return 0;
        }

        // Each of the three parts must sum to exactly this target value.
        int target = totalSum / 3;

        // ---------------------------------------------------------------
        // Step 3: Single-pass scan using prefix sums.
        //
        // We maintain:
        //   prefixSum  — running sum from index 0 to current index
        //   firstCuts  — number of valid positions where the first part ends
        //                (i.e., positions i where prefix[0..i-1] == target)
        //   result     — total number of valid (i, j) split pairs
        //
        // Logic:
        //   - When prefixSum == target:
        //       We've found a potential end of the first subarray.
        //       Increment firstCuts. (We do NOT add to result yet, because
        //       we need a valid second cut j > i.)
        //
        //   - When prefixSum == 2 * target:
        //       We've found a potential end of the second subarray (start of third).
        //       The third subarray is weights[currentIndex..n-1], which sums to
        //       totalSum - 2*target = target. ✓
        //       Every previously counted firstCut position can pair with this j,
        //       so add firstCuts to result.
        //
        // IMPORTANT: We must process the "prefixSum == 2*target" check BEFORE
        // "prefixSum == target" at the same value? Actually they can't be equal
        // simultaneously (target == 2*target only if target == 0, but weights > 0).
        // Also, we must ensure the third subarray is non-empty: we should NOT count
        // j == n (i.e., the last element must be in the third part).
        // We iterate i from 0 to n-2 (inclusive) to ensure j < n.
        // ---------------------------------------------------------------

        int prefixSum = 0;
        int firstCuts = 0; // number of valid positions for the first cut
        int result = 0;    // total valid splits

        // We iterate from index 0 to n-2 (not n-1) because:
        //   - The second cut j must satisfy j < n, meaning the third subarray
        //     must have at least one element (weights[j..n-1] is non-empty).
        //   - If we allowed j = n, the third subarray would be empty.
        // By stopping at n-2, when prefixSum == 2*target at index k (0-based),
        // the third subarray starts at k+1 and goes to n-1, which is non-empty
        // as long as k <= n-2.
        for (int idx = 0; idx < n - 1; idx++) {
            // Add current element to running prefix sum
            prefixSum += weights[idx];

            // -------------------------------------------------------
            // Check if we've reached the end of a potential second part.
            // prefixSum == 2 * target means:
            //   - weights[0..idx] sums to 2*target
            //   - weights[idx+1..n-1] sums to target (the third part)
            //   - The second cut is at position j = idx + 1
            //   - We need at least one element in the third part: idx+1 <= n-1,
            //     which is guaranteed since idx <= n-2.
            //   - Each of the 'firstCuts' valid first-cut positions can pair
            //     with this second-cut position.
            // -------------------------------------------------------
            if (prefixSum == 2 * target) {
                result += firstCuts;
            }

            // -------------------------------------------------------
            // Check if we've reached the end of a potential first part.
            // prefixSum == target means:
            //   - weights[0..idx] sums to target (the first part)
            //   - The first cut is at position i = idx + 1
            //   - We need at least one element in the second part (i < j),
            //     which will be ensured when we find a valid second cut later.
            // Increment firstCuts to record this valid first-cut position.
            //
            // NOTE: We check 2*target BEFORE target in the loop body.
            // This is crucial! If we checked target first and then 2*target,
            // and if target == 2*target (impossible here since weights > 0),
            // we'd double-count. But more importantly, when prefixSum == 2*target,
            // we must use the firstCuts count BEFORE potentially incrementing it
            // for the same index. Since target != 2*target (weights are positive,
            // so target > 0), both conditions can't be true simultaneously,
            // so order doesn't matter for correctness here. But it's good practice.
            // -------------------------------------------------------
            if (prefixSum == target) {
                firstCuts++;
            }
        }

        return result;
    }

    /**
     * Main method to demonstrate the solution with sample inputs from the problem.
     *
     * @param args command-line arguments (not used)
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // ---------------------------------------------------------------
        // Example 1: weights = [1, 2, 3, 0, 3]
        // Total sum = 9, target = 3
        // Valid splits:
        //   [1,2] | [3] | [0,3]   -> i=2, j=3
        //   [1,2] | [3,0] | [3]   -> i=2, j=4
        // Expected output: 2
        // ---------------------------------------------------------------
        int[] weights1 = {1, 2, 3, 0, 3};
        int result1 = solution.countEqualWeightSplits(weights1);
        System.out.println("Example 1:");
        System.out.println("  Input:    " + Arrays.toString(weights1));
        System.out.println("  Output:   " + result1);
        System.out.println("  Expected: 2");
        System.out.println("  Pass: " + (result1 == 2));
        System.out.println();

        // ---------------------------------------------------------------
        // Example 2: weights = [1, 1, 1, 1, 1, 1]
        // Total sum = 6, target = 2
        // Prefix sums: [1, 2, 3, 4, 5]  (we check indices 0..4, i.e., n-2=4)
        //
        // Trace:
        //   idx=0: prefixSum=1. Neither target(2) nor 2*target(4). firstCuts=0, result=0
        //   idx=1: prefixSum=2. == target(2) -> firstCuts=1. result=0
        //   idx=2: prefixSum=3. Neither. firstCuts=1, result=0
        //   idx=3: prefixSum=4. == 2*target(4) -> result += firstCuts(1) = 1.
        //          Then check target: 4 != 2. firstCuts=1, result=1
        //   idx=4: prefixSum=5. Neither. firstCuts=1, result=1
        //
        // Hmm, that gives 1, but expected is 4. Let me re-trace carefully.
        //
        // Wait — with [1,1,1,1,1,1], target=2:
        // Valid (i,j) pairs (1-based cut points, meaning first part is [0..i-1]):
        //   i=2, j=4: [1,1] | [1,1] | [1,1] ✓
        //   i=2, j=5: [1,1] | [1,1,1] | [1] — second part sums to 3 ✗
        //
        // Hmm wait. Let me recount. n=6, indices 0..5.
        // Cut i means first part = weights[0..i-1], second = weights[i..j-1], third = weights[j..5].
        // For all three to sum to 2:
        //   First part sums to 2: prefix[i] = 2, so i=2 (prefix[0..1]=2)
        //   Second part sums to 2: prefix[i..j-1] = 2, so prefix[j] - prefix[i] = 2
        //     prefix[j] = 4, so j=4 (prefix[0..3]=4)
        //   Third part: prefix[5]-prefix[4] = 6-4 = 2 ✓
        //
        // So only one valid split? But expected is 4...
        //
        // Oh wait, there are zeros! No, all are 1s. Let me re-examine the problem.
        // The problem says [1,1,1,1,1,1] has 4 valid splits. Let me list them:
        //   [1,1] | [1,1] | [1,1]  -> i=2, j=4 ✓
        // That's the only one where each part sums to 2... unless I'm misreading.
        //
        // Actually wait — could there be multiple i values? prefix sum = 2 at index 1 (0-based),
        // meaning weights[0]+weights[1]=2, so i=2 (cut after index 1).
        // prefix sum = 4 at index 3, so j=4 (cut after index 3).
        // Only one valid split. Expected should be 1, not 4?
        //
        // Let me re-read the problem... "Output: 4" for [1,1,1,1,1,1].
        // Hmm, maybe I'm misunderstanding the cut point definition.
        //
        // Re-reading: "0 < i < j < n" with weights[0..i-1], weights[i..j-1], weights[j..n-1].
        // For n=6: i in {1,2,3,4}, j in {i+1,...,5}.
        //
        // For [1,1,1,1,1,1], target=2:
        //   First part weights[0..i-1] sums to 2: i=2 (weights[0]+weights[1]=2)
        //   Second part weights[i..j-1] sums to 2: weights[2..j-1]=2, so j-1=3, j=4
        //   Third part weights[4..5]=2 ✓
        // Only one split: i=2, j=4. Output should be 1.
        //
        // But problem says 4. There must be something wrong with the problem statement's
        // example, OR I'm misreading. Let me re-examine...
        //
        // Actually, maybe the problem allows zeros and the example [1,1,1,1,1,1] is
        // just wrong in the problem, or maybe I need to re-examine.
        //
        // Given that Example 1 clearly shows [1,2] | [3,0] | [3] as valid (with a zero
        // in the middle part), the algorithm is correct for that case.
        //
        // For [1,1,1,1,1,1]: I believe the correct answer is 1, not 4.
        // The problem statement's Example 2 might have a typo.
        // My algorithm gives 1 for this input, which seems mathematically correct.
        //
        // Let me verify Example 1 trace:
        // weights=[1,2,3,0,3], n=5, totalSum=9, target=3
        // idx=0: prefixSum=1. 1!=6, 1!=3. firstCuts=0, result=0
        // idx=1: prefixSum=3. 3!=6. Check 2*target=6: no. Check target=3: yes! firstCuts