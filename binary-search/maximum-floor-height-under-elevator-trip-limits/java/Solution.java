import java.util.*;

/*
Problem Title: Maximum Floor Height Under Elevator Trip Limits

Problem Description:
A logistics company is configuring a freight elevator in a warehouse tower. There are n delivery batches,
and batch i contains boxes[i] identical boxes. The elevator can carry at most cap boxes per trip, where
cap is a positive integer chosen once for all batches. A single batch may be split across multiple trips,
but boxes from different batches cannot be mixed in the same trip because each batch must remain sealed
and tracked separately. Therefore, batch i requires ceil(boxes[i] / cap) trips.

The elevator is only allowed to make at most maxTrips total trips during the shift. Your task is to compute
the largest integer capacity cap such that all batches can still be transported within maxTrips trips.

If it is impossible even when cap is arbitrarily large, return -1. Note that making cap larger never
increases the number of trips, so the answer is monotonic and should be solved efficiently.

Formally, find the maximum integer cap >= 1 satisfying:
ceil(boxes[0] / cap) + ceil(boxes[1] / cap) + ... + ceil(boxes[n-1] / cap) <= maxTrips.

Constraints:
- 1 <= n <= 200000
- 1 <= boxes[i] <= 10^12
- 1 <= maxTrips <= 10^18
- The answer must fit in a signed 64-bit integer

Important clarification:
The statement says to return the "largest" capacity, but capacities larger than max(boxes) do not change
the number of trips anymore. To keep the problem well-defined and finite, we search only in the range
[1, max(boxes)]. Under that interpretation, if a feasible capacity exists, the answer is the largest
feasible value in that range.

Example 1:
Input: boxes = [8, 5, 13], maxTrips = 8
Output: 13

Explanation:
- cap = 4 gives trips: ceil(8/4)=2, ceil(5/4)=2, ceil(13/4)=4, total = 8, so 4 is feasible.
- But the problem asks for the largest feasible cap in [1, max(boxes)].
- max(boxes) = 13
- cap = 13 gives trips: ceil(8/13)=1, ceil(5/13)=1, ceil(13/13)=1, total = 3 <= 8
- Therefore the correct answer is 13.

Example 2:
Input: boxes = [4, 4, 4], maxTrips = 2
Output: -1

Explanation:
- Even with cap = 4, each batch still needs 1 trip.
- Total minimum possible trips = 3
- Since 3 > 2, it is impossible.
*/

public class Solution {

    /**
     * Computes the largest feasible elevator capacity cap in the bounded range [1, max(boxes)]
     * such that the total number of trips needed is at most maxTrips.
     *
     * Core idea:
     * 1. For a fixed capacity cap, the trips needed for one batch is ceil(boxes[i] / cap).
     * 2. As cap increases, the total number of trips never increases.
     * 3. Therefore feasibility is monotonic:
     *    - if some cap is feasible, then every larger cap (up to max(boxes)) is also feasible
     *    - if some cap is not feasible, then every smaller cap is also not feasible
     * 4. This monotonic property allows binary search.
     *
     * We also handle the impossible case first:
     * - Even with the largest meaningful capacity max(boxes), each non-empty batch still needs at least 1 trip.
     * - So the absolute minimum total trips is exactly boxes.length.
     * - If boxes.length > maxTrips, the answer is impossible and we return -1.
     *
     * @param boxes array where boxes[i] is the number of boxes in batch i
     * @param maxTrips maximum total trips allowed
     * @return the largest integer capacity in [1, max(boxes)] that is feasible, or -1 if impossible
     * Time complexity: O(n log M), where n is the number of batches and M is max(boxes)
     * Space complexity: O(1) extra space
     */
    public long maximumCapacity(long[] boxes, long maxTrips) {
        // Step 1:
        // If there are more batches than allowed trips, then it is impossible.
        // Reason:
        // Each batch requires at least one trip no matter how large the capacity is,
        // because batches cannot be mixed and each non-empty batch must be transported.
        if (boxes.length > maxTrips) {
            return -1L;
        }

        // Step 2:
        // Find the maximum batch size.
        // This gives us the upper bound of the meaningful search range.
        // Any capacity larger than max(boxes) behaves exactly like max(boxes):
        // every batch would take exactly one trip.
        long maxBox = 0L;
        for (long box : boxes) {
            maxBox = Math.max(maxBox, box);
        }

        // Step 3:
        // Binary search on capacity in the inclusive range [1, maxBox].
        //
        // We want the LARGEST feasible capacity.
        // Standard pattern:
        // - if mid is feasible, record it and move right
        // - otherwise move left
        long left = 1L;
        long right = maxBox;
        long answer = -1L;

        while (left <= right) {
            // Safe midpoint calculation to avoid overflow.
            long mid = left + (right - left) / 2;

            // Check whether this capacity is feasible.
            if (canTransportWithinTrips(boxes, maxTrips, mid)) {
                // mid works, so it is a candidate answer.
                answer = mid;

                // Since we want the largest feasible capacity,
                // try searching for a larger one.
                left = mid + 1;
            } else {
                // mid does not work, so all smaller capacities also do not work?
                // Actually because larger capacity means fewer or equal trips,
                // if mid is NOT feasible, then smaller capacities are also NOT feasible,
                // and larger capacities may become feasible.
                // Therefore we must move rightward.
                left = mid + 1;
            }
        }

        // The above loop structure needs careful monotonic interpretation:
        // Since larger capacity is always easier (never worse),
        // the feasible region is a suffix, not a prefix.
        // That means if mid is feasible, everything to the right is also feasible,
        // so the largest feasible in [1, maxBox] is simply maxBox whenever any feasible exists.
        //
        // To keep the implementation aligned with the problem's bounded interpretation,
        // once feasibility exists at all, the answer is maxBox.
        //
        // However, we still preserve a direct and correct result:
        // if maxBox itself is feasible, answer should be maxBox; otherwise impossible.
        //
        // Because of the monotonic direction, the simplest correct bounded answer is:
        // - if maxBox is feasible => maxBox
        // - else => -1
        //
        // We return that directly for correctness.
        return canTransportWithinTrips(boxes, maxTrips, maxBox) ? maxBox : -1L;
    }

    /**
     * Checks whether a given capacity cap allows all batches to be transported
     * within at most maxTrips total trips.
     *
     * For each batch:
     * tripsNeeded = ceil(boxes[i] / cap)
     *
     * Integer arithmetic trick for ceiling division:
     * ceil(a / b) = (a + b - 1) / b, for positive integers a and b
     *
     * We also stop early if the running total already exceeds maxTrips,
     * which improves performance in failing cases.
     *
     * @param boxes array of batch sizes
     * @param maxTrips maximum allowed total trips
     * @param cap candidate elevator capacity
     * @return true if total required trips <= maxTrips, otherwise false
     * Time complexity: O(n)
     * Space complexity: O(1) extra space
     */
    public boolean canTransportWithinTrips(long[] boxes, long maxTrips, long cap) {
        long totalTrips = 0L;

        for (long box : boxes) {
            // Compute ceil(box / cap) using integer arithmetic.
            long tripsForThisBatch = (box + cap - 1) / cap;

            totalTrips += tripsForThisBatch;

            // Early exit:
            // As soon as we exceed maxTrips, we already know cap is not feasible.
            if (totalTrips > maxTrips) {
                return false;
            }
        }

        return true;
    }

    /**
     * Convenience overload that accepts a List of Long values.
     *
     * @param boxes list of batch sizes
     * @param maxTrips maximum allowed total trips
     * @return the largest feasible capacity in [1, max(boxes)], or -1 if impossible
     * Time complexity: O(n log M), where n is the number of batches and M is max(boxes)
     * Space complexity: O(n) due to conversion from List to array
     */
    public long maximumCapacity(List<Long> boxes, long maxTrips) {
        long[] array = new long[boxes.size()];
        for (int i = 0; i < boxes.size(); i++) {
            array[i] = boxes.get(i);
        }
        return maximumCapacity(array, maxTrips);
    }

    /**
     * Demonstrates the solution on the sample inputs from the problem statement.
     *
     * @param args command-line arguments (not used)
     * @return nothing
     * Time complexity: O(n log M) per demonstration call
     * Space complexity: O(1) extra space besides input storage
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Example 1
        long[] boxes1 = {8L, 5L, 13L};
        long maxTrips1 = 8L;
        long result1 = solution.maximumCapacity(boxes1, maxTrips1);
        System.out.println("Example 1 Result: " + result1);
        // Expected: 13

        // Example 2
        long[] boxes2 = {4L, 4L, 4L};
        long maxTrips2 = 2L;
        long result2 = solution.maximumCapacity(boxes2, maxTrips2);
        System.out.println("Example 2 Result: " + result2);
        // Expected: -1

        // Additional quick checks
        long[] boxes3 = {10L};
        long maxTrips3 = 1L;
        long result3 = solution.maximumCapacity(boxes3, maxTrips3);
        System.out.println("Additional Example 3 Result: " + result3);
        // Expected: 10

        long[] boxes4 = {1L, 1L, 1L, 1L};
        long maxTrips4 = 4L;
        long result4 = solution.maximumCapacity(boxes4, maxTrips4);
        System.out.println("Additional Example 4 Result: " + result4);
        // Expected: 1
    }
}