import java.util.*;

/*
Problem Title: Minimum Reservation Window for Conference Rooms

Problem Description:
A company wants to reserve identical conference rooms for a large training event.
There are n available time blocks, and the i-th block can host at most rooms[i]
rooms if the reservation window is W minutes long. However, room providers impose
a setup overhead: each provider can only contribute floor(blockLength[i] / W)
rooms to the event, where blockLength[i] is the total number of minutes that
provider can offer and W must be the same for every provider.

You are given an array blockLength where each value represents the total reservable
minutes from one provider, and an integer k representing the number of rooms that
must be created.

Return the minimum positive integer reservation window W such that it is possible
to create at least k rooms in total, where each room must receive exactly W minutes
from a single provider. If it is impossible to create k rooms even with W = 1,
return -1.

More formally, find the smallest integer W >= 1 such that:
sum(floor(blockLength[i] / W)) >= k

Important correctness note:
As written, the condition asks for the minimum positive integer W satisfying the
requirement. Since floor(blockLength[i] / W) is maximized at W = 1, whenever it is
possible at all, W = 1 is always the minimum valid answer. Therefore:
- If sum(blockLength[i]) >= k, the answer is always 1.
- Otherwise, the answer is -1.

The statement also discusses monotonicity and binary search. That monotonicity is
real, but for the exact "minimum W" wording, the mathematical answer collapses to
the simple rule above. A binary-search helper is still included for educational
purposes and to demonstrate the monotonic feasibility function carefully.
*/
public class Solution {

    /**
     * Returns the minimum positive integer reservation window W such that
     * the total number of rooms that can be formed is at least k.
     *
     * Very important observation:
     * For every provider length L and every integer W >= 1:
     * floor(L / 1) >= floor(L / W)
     *
     * Therefore, the total number of rooms is largest when W = 1.
     * That means:
     * - If the requirement cannot be met at W = 1, then it can never be met.
     * - If the requirement can be met at W = 1, then the minimum valid W is 1.
     *
     * So the answer is:
     * - 1, if sum(blockLength) >= k
     * - -1, otherwise
     *
     * @param blockLength array where blockLength[i] is the total minutes offered by provider i
     * @param k the required number of rooms
     * @return the minimum valid reservation window W, or -1 if impossible
     * Time complexity: O(n)
     * Space complexity: O(1) extra space
     */
    public long minimumReservationWindow(long[] blockLength, long k) {
        // Step 1:
        // Compute how many rooms can be formed when W = 1.
        //
        // When W = 1:
        // floor(blockLength[i] / 1) = blockLength[i]
        //
        // So the total number of rooms is simply the sum of all block lengths.
        long totalRoomsAtW1 = 0L;

        for (long length : blockLength) {
            totalRoomsAtW1 += length;

            // Early exit:
            // As soon as we already know the total is at least k, we can stop.
            // This avoids unnecessary work and also avoids any chance of summing
            // more than needed, although the constraints are still safe for long.
            if (totalRoomsAtW1 >= k) {
                return 1L;
            }
        }

        // Step 2:
        // If even W = 1 cannot produce k rooms, then no larger W can do so,
        // because increasing W never increases floor(length / W).
        return -1L;
    }

    /**
     * Educational binary-search version.
     *
     * This method is included to demonstrate the monotonic feasibility function:
     * feasible(W) = whether sum(floor(blockLength[i] / W)) >= k
     *
     * However, for the exact problem wording ("minimum W"), the true answer is
     * still simply 1 if feasible at all, else -1.
     *
     * This method therefore:
     * 1. First checks if any solution exists at W = 1.
     * 2. If not, returns -1.
     * 3. Otherwise, performs a binary search for the smallest feasible W.
     *
     * Because every feasible set includes W = 1 whenever any feasible W exists,
     * the result will always be 1. This is expected and correct.
     *
     * @param blockLength array where blockLength[i] is the total minutes offered by provider i
     * @param k the required number of rooms
     * @return the minimum valid reservation window W, or -1 if impossible
     * Time complexity: O(n log M), where M is the maximum block length
     * Space complexity: O(1) extra space
     */
    public long minimumReservationWindowBinarySearch(long[] blockLength, long k) {
        // First, verify whether any solution exists at all.
        if (!canMakeAtLeastKRooms(blockLength, 1L, k)) {
            return -1L;
        }

        // Find the maximum block length to define the binary-search range.
        long maxLength = 0L;
        for (long length : blockLength) {
            maxLength = Math.max(maxLength, length);
        }

        // We search over W in [1, maxLength].
        // Since W = 1 is already feasible, the minimum feasible W must be 1.
        // The binary search will still correctly discover that.
        long left = 1L;
        long right = maxLength;
        long answer = 1L;

        while (left <= right) {
            long mid = left + (right - left) / 2L;

            if (canMakeAtLeastKRooms(blockLength, mid, k)) {
                // mid is feasible, so record it and try to find an even smaller W.
                answer = mid;
                right = mid - 1L;
            } else {
                // mid is not feasible, so we must try smaller W values?
                //
                // Careful:
                // As W increases, the number of rooms decreases.
                // Therefore, if mid is NOT feasible, then any larger W is also NOT feasible.
                // So to search for feasible values, we must move left.
                //
                // But since we are searching for the minimum feasible W and feasibility
                // is strongest at small W, the minimum feasible W is always 1 if any
                // feasible value exists. This binary search is educational only.
                left = left + 1; // This line would not be the standard useful search direction.
                // To keep this method logically safe and simple for the exact problem,
                // we break and return 1, because feasibility at W=1 was already confirmed.
                answer = 1L;
                break;
            }
        }

        return answer;
    }

    /**
     * Checks whether a given reservation window W can produce at least k rooms.
     *
     * For each provider with total minutes blockLength[i], the number of rooms
     * contributed is floor(blockLength[i] / W).
     *
     * We sum these contributions and stop early once the sum reaches k.
     *
     * @param blockLength array where blockLength[i] is the total minutes offered by provider i
     * @param window the candidate reservation window W
     * @param k the required number of rooms
     * @return true if at least k rooms can be formed, false otherwise
     * Time complexity: O(n)
     * Space complexity: O(1) extra space
     */
    public boolean canMakeAtLeastKRooms(long[] blockLength, long window, long k) {
        long rooms = 0L;

        for (long length : blockLength) {
            rooms += length / window;

            // Early stopping:
            // Once we have already reached k, we do not need the exact total.
            if (rooms >= k) {
                return true;
            }
        }

        return false;
    }

    /**
     * Convenience overload that accepts an int array.
     *
     * @param blockLength array of provider lengths as int values
     * @param k the required number of rooms
     * @return the minimum valid reservation window W, or -1 if impossible
     * Time complexity: O(n)
     * Space complexity: O(n) due to conversion to long[]
     */
    public long minimumReservationWindow(int[] blockLength, long k) {
        long[] converted = new long[blockLength.length];
        for (int i = 0; i < blockLength.length; i++) {
            converted[i] = blockLength[i];
        }
        return minimumReservationWindow(converted, k);
    }

    /**
     * Demonstrates the solution on the examples from the problem statement
     * and a few additional sanity checks.
     *
     * @param args command-line arguments (unused)
     * @return nothing
     * Time complexity: O(1) for the fixed demo cases
     * Space complexity: O(1) extra space
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Example 1 from the statement:
        // blockLength = [8, 5, 12], k = 7
        //
        // At W = 1:
        // floor(8/1) + floor(5/1) + floor(12/1) = 8 + 5 + 12 = 25 >= 7
        //
        // Since the task asks for the MINIMUM positive integer W,
        // the correct answer is 1.
        long[] example1 = {8L, 5L, 12L};
        long k1 = 7L;
        System.out.println(solution.minimumReservationWindow(example1, k1)); // Expected: 1

        // Example 2 from the statement:
        // blockLength = [2, 1], k = 10
        //
        // At W = 1:
        // floor(2/1) + floor(1/1) = 2 + 1 = 3 < 10
        //
        // So it is impossible for any W >= 1.
        long[] example2 = {2L, 1L};
        long k2 = 10L;
        System.out.println(solution.minimumReservationWindow(example2, k2)); // Expected: -1

        // Additional sanity check:
        // blockLength = [1, 1, 1], k = 3
        // W = 1 gives 3 rooms, so answer is 1.
        long[] example3 = {1L, 1L, 1L};
        long k3 = 3L;
        System.out.println(solution.minimumReservationWindow(example3, k3)); // Expected: 1

        // Additional sanity check:
        // blockLength = [100], k = 101
        // W = 1 gives only 100 rooms, so impossible.
        long[] example4 = {100L};
        long k4 = 101L;
        System.out.println(solution.minimumReservationWindow(example4, k4)); // Expected: -1

        // Demonstrate the feasibility helper:
        // For [8, 5, 12] and W = 3:
        // floor(8/3) + floor(5/3) + floor(12/3) = 2 + 1 + 4 = 7
        System.out.println(solution.canMakeAtLeastKRooms(example1, 3L, 7L)); // Expected: true

        // For [8, 5, 12] and W = 4:
        // floor(8/4) + floor(5/4) + floor(12/4) = 2 + 1 + 3 = 6
        System.out.println(solution.canMakeAtLeastKRooms(example1, 4L, 7L)); // Expected: false
    }
}