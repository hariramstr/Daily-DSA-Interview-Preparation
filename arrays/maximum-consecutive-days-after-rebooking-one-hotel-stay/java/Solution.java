import java.util.*;

/*
Problem Title: Maximum Consecutive Days After Rebooking One Hotel Stay

Problem Description:
You are given a strictly increasing array bookedDays, where each value represents a calendar day on which a traveler already has a hotel reservation. The traveler wants the longest possible streak of consecutive booked days. To improve the itinerary, they are allowed to rebook exactly one existing reservation to any other day number, as long as the final set of booked days still contains no duplicates.

Return the maximum number of consecutive calendar days that can appear in the final set after moving at most one reservation.

A rebooking means choosing one value from bookedDays, removing it, and inserting a different positive integer day. You may also choose not to move any reservation if the array is already optimal. The relative order of the array does not matter after the move; only the set of booked days matters.

This is an arrays problem focused on analyzing gaps between consecutive values. A correct solution should run efficiently for large inputs.

Constraints:
- 1 <= bookedDays.length <= 200000
- 1 <= bookedDays[i] <= 1000000000
- bookedDays is strictly increasing

Example 1:
Input: bookedDays = [2, 4, 5, 7]
Output: 4
Explanation: Rebook day 7 to day 3. The final set becomes [2, 3, 4, 5], which contains a longest consecutive streak of length 4.

Example 2:
Input: bookedDays = [10, 11, 14, 15, 16, 20]
Output: 4
Explanation: The best move is to rebook day 20 to day 13, producing [10, 11, 13, 14, 15, 16]. The longest consecutive streak is then [13, 14, 15, 16] with length 4. It is not possible to form a streak of length 5 with only one rebooking.
*/

public class Solution {

    /**
     * Computes the maximum possible length of a consecutive-day streak after moving at most one booked day.
     *
     * Core idea:
     * 1. Split the sorted array into maximal consecutive blocks.
     * 2. Without any move, the answer is simply the largest block length.
     * 3. With one move, there are only two meaningful ways to improve:
     *    - Extend one existing block by 1, if there exists some "spare" reservation outside that block
     *      that can be moved next to it.
     *    - Bridge two blocks separated by exactly one missing day, if there exists some spare reservation
     *      outside those two blocks that can be moved into that missing day.
     *
     * Why "spare" matters:
     * If we move a reservation from inside the block(s) we are trying to build, we may break that block,
     * so we need at least one reservation outside the target structure to donate.
     *
     * @param bookedDays strictly increasing array of booked calendar days
     * @return maximum achievable consecutive streak length after at most one rebooking
     * Time complexity: O(n), where n is bookedDays.length
     * Space complexity: O(n), for storing consecutive block boundaries and lengths
     */
    public int maximumConsecutiveDaysAfterOneRebooking(int[] bookedDays) {
        int n = bookedDays.length;

        // With only one reservation, the longest possible consecutive streak is always 1.
        // We may move it somewhere else, but we still only have one booked day.
        if (n == 1) {
            return 1;
        }

        // ---------------------------------------------------------------------
        // STEP 1: Decompose the array into maximal consecutive blocks.
        //
        // Example:
        // [10, 11, 14, 15, 16, 20]
        // blocks are:
        //   [10, 11] length 2
        //   [14, 15, 16] length 3
        //   [20] length 1
        //
        // We store:
        //   starts[k] = first day of block k
        //   ends[k]   = last day of block k
        //   lens[k]   = length of block k
        // ---------------------------------------------------------------------
        int[] starts = new int[n];
        int[] ends = new int[n];
        int[] lens = new int[n];
        int blockCount = 0;

        int start = bookedDays[0];
        int prev = bookedDays[0];

        for (int i = 1; i < n; i++) {
            if (bookedDays[i] == prev + 1) {
                // Still inside the same consecutive block.
                prev = bookedDays[i];
            } else {
                // Current block ends at "prev".
                starts[blockCount] = start;
                ends[blockCount] = prev;
                lens[blockCount] = prev - start + 1;
                blockCount++;

                // Start a new block.
                start = bookedDays[i];
                prev = bookedDays[i];
            }
        }

        // Store the final block.
        starts[blockCount] = start;
        ends[blockCount] = prev;
        lens[blockCount] = prev - start + 1;
        blockCount++;

        // ---------------------------------------------------------------------
        // STEP 2: Compute the best answer without any move.
        // This is simply the maximum block length already present.
        // ---------------------------------------------------------------------
        int answer = 1;
        for (int i = 0; i < blockCount; i++) {
            answer = Math.max(answer, lens[i]);
        }

        // ---------------------------------------------------------------------
        // STEP 3: Precompute prefix/suffix maxima of block lengths.
        //
        // These arrays let us quickly answer:
        // "Is there some block outside a certain range that contains at least one reservation?"
        //
        // In practice, since every block has positive length, if there exists any block outside
        // the target range, then there exists at least one reservation outside it.
        //
        // We use these arrays to test whether a "donor" reservation exists outside:
        //   - one chosen block
        //   - or two neighboring blocks we want to bridge
        // ---------------------------------------------------------------------
        int[] prefixMax = new int[blockCount];
        int[] suffixMax = new int[blockCount];

        prefixMax[0] = lens[0];
        for (int i = 1; i < blockCount; i++) {
            prefixMax[i] = Math.max(prefixMax[i - 1], lens[i]);
        }

        suffixMax[blockCount - 1] = lens[blockCount - 1];
        for (int i = blockCount - 2; i >= 0; i--) {
            suffixMax[i] = Math.max(suffixMax[i + 1], lens[i]);
        }

        // ---------------------------------------------------------------------
        // STEP 4: Try extending a single block by 1.
        //
        // Suppose a block is [L..R] with length len.
        // If there exists any reservation outside this block, we can move that outside reservation
        // to either L-1 or R+1 (provided that day is positive and not already occupied).
        //
        // Since the block is maximal, at least one side is free:
        //   - If L > 1, then L-1 is a valid positive day and is not in the block.
        //   - Or R+1 is not in the block because otherwise the block would extend.
        //
        // Therefore, any block can be extended by 1 as long as there is at least one reservation
        // outside it to donate.
        //
        // The only exception is when the entire array is a single block; then there is no outside
        // reservation, so no extension is possible.
        // ---------------------------------------------------------------------
        for (int i = 0; i < blockCount; i++) {
            boolean hasOutsideReservation = (i > 0) || (i + 1 < blockCount);
            if (hasOutsideReservation) {
                answer = Math.max(answer, lens[i] + 1);
            }
        }

        // ---------------------------------------------------------------------
        // STEP 5: Try bridging two neighboring blocks separated by exactly one missing day.
        //
        // Example:
        //   block A = [2, 2]
        //   block B = [4, 5]
        // gap is exactly one day: missing day 3
        //
        // If we have some reservation outside A and B, we can move it to day 3 and create:
        //   [2, 3, 4, 5] => length 4
        //
        // Important:
        // We must have a donor reservation outside these two blocks.
        // If the whole array consists only of these two blocks, then moving one reservation from
        // inside them into the gap would break one side, so we cannot achieve full merge length.
        //
        // In that case, the best we can do is already covered by "extend one block by 1".
        // ---------------------------------------------------------------------
        for (int i = 0; i + 1 < blockCount; i++) {
            // Gap size between block i and block i+1:
            // If ends[i] = 5 and starts[i+1] = 7, then gap = 1 (missing day 6).
            long gap = (long) starts[i + 1] - ends[i] - 1L;

            if (gap == 1L) {
                boolean hasOutsideLeft = i - 1 >= 0;
                boolean hasOutsideRight = i + 2 < blockCount;
                boolean hasOutsideReservation = hasOutsideLeft || hasOutsideRight;

                if (hasOutsideReservation) {
                    answer = Math.max(answer, lens[i] + 1 + lens[i + 1]);
                }
            }
        }

        // The answer can never exceed n, because we still have exactly n reservations after rebooking.
        return Math.min(answer, n);
    }

    /**
     * Convenience wrapper matching a shorter interview-style method name.
     *
     * @param bookedDays strictly increasing array of booked calendar days
     * @return maximum achievable consecutive streak length after at most one rebooking
     * Time complexity: O(n)
     * Space complexity: O(n)
     */
    public int maxConsecutiveDays(int[] bookedDays) {
        return maximumConsecutiveDaysAfterOneRebooking(bookedDays);
    }

    /**
     * Utility method to print an input array and the computed answer.
     *
     * @param bookedDays strictly increasing array of booked calendar days
     * @return computed maximum consecutive streak length
     * Time complexity: O(n)
     * Space complexity: O(n)
     */
    public int demonstrate(int[] bookedDays) {
        int result = maximumConsecutiveDaysAfterOneRebooking(bookedDays);
        System.out.println("bookedDays = " + Arrays.toString(bookedDays));
        System.out.println("Maximum consecutive streak after at most one rebooking = " + result);
        System.out.println();
        return result;
    }

    /**
     * Demonstrates the algorithm on sample inputs and a few additional sanity checks.
     *
     * @param args command-line arguments (unused)
     * @return nothing
     * Time complexity: O(total input size of demonstrated examples)
     * Space complexity: O(max input size among demonstrated examples)
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Sample 1 from the prompt:
        // [2, 4, 5, 7]
        // Move 7 -> 3, result [2, 3, 4, 5], longest streak = 4
        int[] sample1 = {2, 4, 5, 7};
        solution.demonstrate(sample1);

        // Sample 2 from the prompt:
        // [10, 11, 14, 15, 16, 20]
        // Best move: 20 -> 13, result [10, 11, 13, 14, 15, 16], longest streak = 4
        int[] sample2 = {10, 11, 14, 15, 16, 20};
        solution.demonstrate(sample2);

        // Additional checks:

        // Already fully consecutive; no move needed.
        int[] test1 = {5, 6, 7, 8};
        solution.demonstrate(test1); // Expected 4

        // Single element.
        int[] test2 = {100};
        solution.demonstrate(test2); // Expected 1

        // Two blocks with one missing day, but no outside donor.
        // [1] and [3,4] only. Cannot fully make [1,2,3,4] because donor must come from inside.
        // Best is move 1 -> 2 to get [2,3,4], or move 4 -> 2 to get [1,2,3], so answer = 3.
        int[] test3 = {1, 3, 4};
        solution.demonstrate(test3); // Expected 3

        // Multiple blocks, can bridge using an outside donor.
        int[] test4 = {1, 3, 4, 6};
        solution.demonstrate(test4); // Expected 3

        // Can extend a large block by one using an outside singleton.
        int[] test5 = {10, 11, 12, 20};
        solution.demonstrate(test5); // Expected 4
    }
}