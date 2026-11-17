/*
Title: Minimum Escalations to Meet Boat Departure Deadlines

Problem Description:
A harbor dispatch system tracks cargo crates waiting to be loaded onto boats. You are given an integer array weights where weights[i] is the weight of the i-th crate. Each boat can carry at most two crates, and the combined weight of the two crates on the same boat cannot exceed limit. A crate may also be loaded alone.

Normally, every crate must be assigned to some boat. However, the harbor has a costly manual escalation process that allows a crate to be split and shipped separately outside the normal loading system. You may escalate any number of crates, but each escalated crate counts as 1 escalation and is removed before boat assignment. Your goal is to minimize the number of escalations so that all remaining crates can be assigned to boats under the two-crate and weight-limit rules using any number of boats.

Return the minimum number of escalations required.

This is not the same as minimizing the number of boats. You may use as many boats as needed after removing some crates, but every non-escalated crate must fit on a boat either alone or paired with another crate. Since a single crate can ride alone, the only crates that force escalation are those whose individual weight already exceeds limit. Still, the input size is large enough that your solution should be efficient and should make use of sorting and a two-pointer style argument rather than simulation.

Constraints:
- 1 <= weights.length <= 200000
- 1 <= weights[i] <= 10^9
- 1 <= limit <= 10^9

Examples:
1) weights = [3, 5, 2, 8, 1], limit = 5
   Output: 1
   Explanation:
   - Weight 8 is greater than limit 5, so it cannot go on any boat, even alone.
   - Therefore it must be escalated.
   - Remaining weights [1, 2, 3, 5] can all be assigned to boats:
     (1,3), (2), (5)

2) weights = [9, 4, 7, 6, 2, 11], limit = 7
   Output: 2
   Explanation:
   - Weights 9 and 11 are both greater than limit 7.
   - Each of those must be escalated.
   - Remaining weights [2, 4, 6, 7] can all be assigned to boats:
     (2,4), (6), (7)
*/

using System;

public class Solution
{
    /*
    Time Complexity: O(n log n)
    - We sort the array once, which costs O(n log n).
    - Then we use a two-pointer scan / boundary search idea in O(n).

    Space Complexity: O(log n)
    - Array.Sort on primitive arrays uses an in-place introspective sort style approach.
    - Aside from sorting stack usage, we only use a few variables.

    Beginner-friendly key idea:
    - A crate only needs escalation if it cannot fit on a boat by itself.
    - Since a boat may carry one crate alone, any crate with weight <= limit is always valid.
    - Therefore, the answer is simply the number of crates with weight > limit.
    - We still sort and use a pointer/boundary argument to efficiently count how many values exceed limit.
    */
    public int MinimumEscalations(int[] weights, int limit)
    {
        // Step 1: Sort the weights in non-decreasing order.
        //
        // Why do we sort?
        // - After sorting, all "small enough" crates will appear first,
        //   and all "too heavy" crates will appear at the end.
        // - That means the crates requiring escalation form one contiguous suffix.
        // - Once we find the first crate that is greater than limit,
        //   every crate after it must also be greater than limit.
        //
        // This makes counting very easy and efficient.
        Array.Sort(weights);

        // Step 2: Use a two-pointer / boundary-style scan from the right side.
        //
        // Why from the right?
        // - The heaviest crates are at the end after sorting.
        // - Those are exactly the crates most likely to violate the limit.
        // - We move left while crates are too heavy.
        //
        // Interpretation of 'right':
        // - right starts at the last valid index.
        // - While weights[right] > limit, that crate cannot fit even alone,
        //   so it must be escalated.
        int right = weights.Length - 1;

        // This variable will count how many crates must be escalated.
        int escalations = 0;

        // Step 3: Walk left across the suffix of overweight crates.
        //
        // Each iteration checks one crate:
        // - If its weight is greater than limit, it cannot be assigned to any boat.
        // - A boat can carry at most two crates, but also allows one crate alone.
        // - So if a single crate already exceeds limit, there is no legal placement for it.
        // - Therefore escalation is mandatory for that crate.
        //
        // We stop as soon as we reach a crate with weight <= limit,
        // because all earlier crates are even lighter and therefore also fit alone.
        while (right >= 0 && weights[right] > limit)
        {
            escalations++;
            right--;
        }

        // Step 4: Return the total number of mandatory escalations.
        //
        // Why is this correct?
        // - Every counted crate is > limit, so it cannot fit even alone.
        //   Thus it must be escalated.
        // - Every uncounted crate is <= limit, so it can always be placed alone on a boat.
        //   Since the problem allows any number of boats, no further escalation is needed.
        //
        // Therefore this count is both necessary and sufficient, which means it is minimal.
        return escalations;
    }
}

// Demo code

var solution = new Solution();

// Example 1
int[] weights1 = { 3, 5, 2, 8, 1 };
int limit1 = 5;
int result1 = solution.MinimumEscalations(weights1, limit1);
Console.WriteLine(result1); // Expected: 1

// Example 2
int[] weights2 = { 9, 4, 7, 6, 2, 11 };
int limit2 = 7;
int result2 = solution.MinimumEscalations(weights2, limit2);
Console.WriteLine(result2); // Expected: 2

// Additional quick checks
int[] weights3 = { 1, 2, 3, 4 };
int limit3 = 10;
Console.WriteLine(solution.MinimumEscalations(weights3, limit3)); // Expected: 0

int[] weights4 = { 12, 13, 14 };
int limit4 = 11;
Console.WriteLine(solution.MinimumEscalations(weights4, limit4)); // Expected: 3