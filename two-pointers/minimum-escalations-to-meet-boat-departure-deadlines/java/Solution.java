import java.util.*;

/*
Title: Minimum Escalations to Meet Boat Departure Deadlines

Problem Description:
A harbor dispatch system tracks cargo crates waiting to be loaded onto boats. You are given an integer array
weights where weights[i] is the weight of the i-th crate. Each boat can carry at most two crates, and the
combined weight of the two crates on the same boat cannot exceed limit. A crate may also be loaded alone.

Normally, every crate must be assigned to some boat. However, the harbor has a costly manual escalation process
that allows a crate to be split and shipped separately outside the normal loading system. You may escalate any
number of crates, but each escalated crate counts as 1 escalation and is removed before boat assignment.
Your goal is to minimize the number of escalations so that all remaining crates can be assigned to boats under
the two-crate and weight-limit rules using any number of boats.

Return the minimum number of escalations required.

This is not the same as minimizing the number of boats. You may use as many boats as needed after removing some
crates, but every non-escalated crate must fit on a boat either alone or paired with another crate. Since a
single crate can ride alone, the only crates that force escalation are those whose individual weight already
exceeds limit. Still, the input size is large enough that your solution should be efficient and should make use
of sorting and a two-pointer style argument rather than simulation.

Constraints:
- 1 <= weights.length <= 200000
- 1 <= weights[i] <= 10^9
- 1 <= limit <= 10^9

Example 1:
Input: weights = [3, 5, 2, 8, 1], limit = 5
Output: 1
Explanation: The crate with weight 8 cannot be placed on any boat, even alone, so it must be escalated.
The remaining crates [1, 2, 3, 5] can all be assigned to boats, for example (1,3), (2), (5).

Example 2:
Input: weights = [9, 4, 7, 6, 2, 11], limit = 7
Output: 2
Explanation: Crates with weights 9 and 11 exceed the boat limit individually, so both must be escalated.
The remaining crates [2, 4, 6, 7] can be loaded as (2,4), (6), (7).
*/

public class Solution {

    /**
     * Computes the minimum number of escalations required so that every remaining crate
     * can be assigned to some boat.
     *
     * Core observation:
     * - A boat may carry one crate alone.
     * - Therefore, any crate with weight <= limit is always loadable, even if it cannot pair with anyone.
     * - The only impossible crates are those with weight > limit.
     * - So the answer is simply the number of such crates.
     *
     * We still sort and use a two-pointer style scan to align with the requested technique and to
     * make the reasoning explicit:
     * - After sorting, all valid crates (<= limit) appear first.
     * - All invalid crates (> limit) appear at the end.
     * - A right-to-left pointer can count exactly how many crates exceed the limit.
     *
     * @param weights the array of crate weights
     * @param limit the maximum weight a boat can carry, whether carrying one crate or two combined
     * @return the minimum number of escalations required
     * Time complexity: O(n log n) due to sorting
     * Space complexity: O(log n) due to sorting recursion/implementation details
     */
    public int minimumEscalations(int[] weights, int limit) {
        // Defensive handling is not strictly necessary under the given constraints,
        // but this keeps the method beginner-friendly and robust.
        if (weights == null || weights.length == 0) {
            return 0;
        }

        // Step 1: Sort the array.
        //
        // Why sort if the answer is "count weights > limit"?
        // Because the problem explicitly asks for a sorting + two-pointer style argument,
        // and sorting makes the structure of the data very clear:
        //
        // After sorting:
        //   [ all weights <= limit ][ all weights > limit ]
        //
        // Once we find the boundary, counting impossible crates becomes easy.
        Arrays.sort(weights);

        // Step 2: Use a pointer starting from the heaviest crate.
        //
        // Every crate heavier than 'limit' cannot fit on any boat:
        // - not with another crate, because even alone it already exceeds the limit
        // - not alone, for the same reason
        //
        // Therefore, each such crate must be escalated.
        int right = weights.length - 1;
        int escalations = 0;

        // Step 3: Move from the end toward the beginning while crates are too heavy.
        //
        // Because the array is sorted:
        // - if weights[right] > limit, then this crate is impossible and must be escalated
        // - continue moving left until we reach a crate that is <= limit
        //
        // At that point, every crate to the left is also <= limit and therefore loadable alone if needed.
        while (right >= 0 && weights[right] > limit) {
            escalations++;
            right--;
        }

        // Step 4: Return the count of impossible crates.
        return escalations;
    }

    /**
     * Alternative implementation using a direct linear count without sorting.
     *
     * This method is included for educational comparison:
     * - It is actually more efficient than sorting for this exact problem.
     * - However, the main requested solution uses sorting and a two-pointer style argument.
     *
     * @param weights the array of crate weights
     * @param limit the maximum allowed boat load
     * @return the minimum number of escalations required
     * Time complexity: O(n)
     * Space complexity: O(1)
     */
    public int minimumEscalationsLinear(int[] weights, int limit) {
        if (weights == null || weights.length == 0) {
            return 0;
        }

        int escalations = 0;

        // Every crate heavier than the limit is individually impossible to place on any boat.
        for (int weight : weights) {
            if (weight > limit) {
                escalations++;
            }
        }

        return escalations;
    }

    /**
     * Demonstrates the algorithm on the sample inputs from the problem statement.
     *
     * @param args command-line arguments, not used
     * @return nothing
     * Time complexity: O(1) for the fixed demo size, excluding the called method costs
     * Space complexity: O(1), excluding the called method costs
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Example 1
        int[] weights1 = {3, 5, 2, 8, 1};
        int limit1 = 5;
        int result1 = solution.minimumEscalations(weights1.clone(), limit1);
        System.out.println("Example 1 Result: " + result1);
        // Expected: 1
        //
        // Trace:
        // weights = [3, 5, 2, 8, 1], limit = 5
        // Sorted:  [1, 2, 3, 5, 8]
        // Only 8 > 5, so exactly 1 escalation is required.
        //
        // Remaining crates [1, 2, 3, 5] are all individually <= 5,
        // so each can be placed on a boat, alone if necessary.
        // Example valid loading: (1,3), (2), (5)

        // Example 2
        int[] weights2 = {9, 4, 7, 6, 2, 11};
        int limit2 = 7;
        int result2 = solution.minimumEscalations(weights2.clone(), limit2);
        System.out.println("Example 2 Result: " + result2);
        // Expected: 2
        //
        // Trace:
        // weights = [9, 4, 7, 6, 2, 11], limit = 7
        // Sorted:  [2, 4, 6, 7, 9, 11]
        // Crates 9 and 11 are > 7, so both must be escalated.
        //
        // Remaining crates [2, 4, 6, 7] are all individually <= 7,
        // so they can all be assigned to boats.
        // Example valid loading: (2,4), (6), (7)

        // Additional quick checks
        int[] weights3 = {1, 2, 3, 4};
        int limit3 = 10;
        int result3 = solution.minimumEscalations(weights3.clone(), limit3);
        System.out.println("Additional Check 1 Result: " + result3);
        // Expected: 0

        int[] weights4 = {12, 13, 14};
        int limit4 = 11;
        int result4 = solution.minimumEscalations(weights4.clone(), limit4);
        System.out.println("Additional Check 2 Result: " + result4);
        // Expected: 3

        int[] weights5 = {7};
        int limit5 = 7;
        int result5 = solution.minimumEscalations(weights5.clone(), limit5);
        System.out.println("Additional Check 3 Result: " + result5);
        // Expected: 0
    }
}