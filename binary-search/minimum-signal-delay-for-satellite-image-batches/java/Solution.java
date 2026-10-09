import java.util.*;

/*
Problem Title: Minimum Signal Delay for Satellite Image Batches

Problem Description:
A ground station receives satellite image batches in a fixed order. The i-th batch has size batches[i] megabytes.
You must transmit all batches within h minutes, and the transmitter can be configured to send data at a constant
integer speed of s megabytes per minute.

During each minute, the transmitter works on only one batch. If the current batch has fewer than s megabytes
remaining, the unused portion of that minute is wasted and you may continue with the next batch only in the
following minute. In other words, sending a batch of size x at speed s takes ceil(x / s) whole minutes.

Your task is to return the minimum integer transmission speed s such that all batches can be sent within h minutes.

If it is impossible even when the speed is very large, return -1. This happens when h is smaller than the number
of batches, because every non-empty batch needs at least one full minute.

Constraints:
- 1 <= batches.length <= 100000
- 1 <= batches[i] <= 1000000000
- 1 <= h <= 1000000000
- The answer must fit in a 32-bit signed integer.

Example 1:
Input: batches = [30, 11, 23, 4, 20], h = 6
Output: 23

Explanation:
At speed 23, the required time is:
ceil(30/23) + ceil(11/23) + ceil(23/23) + ceil(4/23) + ceil(20/23)
= 2 + 1 + 1 + 1 + 1
= 6

Any smaller speed requires more than 6 minutes.

Example 2:
Input: batches = [8, 5, 8], h = 2
Output: -1

Explanation:
There are 3 non-empty batches, and each batch takes at least 1 minute regardless of speed.
So finishing in 2 minutes is impossible.

Key Insight:
The condition is monotonic:
- If a speed s is enough to finish within h minutes,
- then any speed larger than s is also enough.

That allows us to binary search for the minimum valid speed.
*/

public class Solution {

    /**
     * Finds the minimum integer transmission speed needed to send all batches within h minutes.
     *
     * The method first checks the impossible case:
     * if h is smaller than the number of batches, then the answer is -1 because each non-empty
     * batch needs at least one whole minute.
     *
     * Otherwise, it performs binary search on the speed range:
     * - minimum possible speed = 1
     * - maximum possible speed = max(batches)
     *
     * Why is max(batches) a valid upper bound?
     * Because at that speed, every batch finishes in at most 1 minute, so total time becomes
     * exactly the number of batches, which is the smallest possible total time.
     *
     * @param batches the array where batches[i] is the size of the i-th image batch in megabytes
     * @param h the maximum number of minutes allowed to transmit all batches
     * @return the minimum integer speed required, or -1 if it is impossible
     * Time complexity: O(n log M), where n is the number of batches and M is the maximum batch size
     * Space complexity: O(1), excluding input storage
     */
    public int minTransmissionSpeed(int[] batches, int h) {
        // If there are more batches than available minutes, it is impossible.
        // Each batch is non-empty and therefore needs at least 1 full minute.
        if (h < batches.length) {
            return -1;
        }

        // Find the largest batch size.
        // This gives us the right boundary for binary search.
        int maxBatch = 0;
        for (int batch : batches) {
            maxBatch = Math.max(maxBatch, batch);
        }

        // Binary search boundaries:
        // left  = smallest possible speed
        // right = definitely sufficient speed
        int left = 1;
        int right = maxBatch;

        // We want the minimum valid speed.
        // Standard "first true" binary search pattern:
        // - if mid works, try smaller speeds on the left side
        // - if mid does not work, move right
        while (left < right) {
            // Safe midpoint calculation to avoid overflow.
            int mid = left + (right - left) / 2;

            // Check whether this speed is enough.
            if (canFinish(batches, h, mid)) {
                // mid is sufficient, so the answer could be mid or something smaller.
                right = mid;
            } else {
                // mid is too slow, so we must search larger speeds.
                left = mid + 1;
            }
        }

        // At loop end, left == right and points to the minimum sufficient speed.
        return left;
    }

    /**
     * Checks whether all batches can be transmitted within h minutes at the given speed.
     *
     * For each batch of size x, the required minutes are ceil(x / speed).
     * To compute ceil(x / speed) using integer arithmetic, we use:
     * (x + speed - 1) / speed
     *
     * We use a long variable for total time because the sum can exceed the int range.
     * For example, with many large batches and a small speed, the total minutes can be very large.
     *
     * We also stop early if total time already exceeds h, because in that case the speed is
     * definitely not sufficient and there is no need to continue summing.
     *
     * @param batches the array of batch sizes
     * @param h the allowed total number of minutes
     * @param speed the candidate transmission speed in megabytes per minute
     * @return true if all batches can be transmitted within h minutes at this speed, otherwise false
     * Time complexity: O(n), where n is the number of batches
     * Space complexity: O(1)
     */
    public boolean canFinish(int[] batches, int h, int speed) {
        long totalMinutes = 0L;

        // Process every batch independently.
        for (int batch : batches) {
            // Compute ceil(batch / speed) using integer math.
            // Example:
            // batch = 30, speed = 23
            // (30 + 23 - 1) / 23 = 52 / 23 = 2
            totalMinutes += (batch + (long) speed - 1) / speed;

            // Early exit:
            // if we already exceeded h, this speed is not enough.
            if (totalMinutes > h) {
                return false;
            }
        }

        // If total required time is within h, the speed works.
        return totalMinutes <= h;
    }

    /**
     * Demonstrates the solution using the sample inputs from the problem statement.
     *
     * It prints:
     * - the input batches
     * - the allowed time h
     * - the computed minimum transmission speed
     *
     * Expected outputs:
     * Example 1 -> 23
     * Example 2 -> -1
     *
     * @param args command-line arguments (not used)
     * @return nothing
     * Time complexity: O(n log M) across the demonstrated test cases
     * Space complexity: O(1), excluding input arrays
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        int[] batches1 = {30, 11, 23, 4, 20};
        int h1 = 6;
        int result1 = solution.minTransmissionSpeed(batches1, h1);
        System.out.println("Example 1:");
        System.out.println("batches = " + Arrays.toString(batches1));
        System.out.println("h = " + h1);
        System.out.println("Minimum transmission speed = " + result1);
        System.out.println("Expected = 23");
        System.out.println();

        int[] batches2 = {8, 5, 8};
        int h2 = 2;
        int result2 = solution.minTransmissionSpeed(batches2, h2);
        System.out.println("Example 2:");
        System.out.println("batches = " + Arrays.toString(batches2));
        System.out.println("h = " + h2);
        System.out.println("Minimum transmission speed = " + result2);
        System.out.println("Expected = -1");
        System.out.println();

        // Additional quick sanity check.
        int[] batches3 = {3, 6, 7, 11};
        int h3 = 8;
        int result3 = solution.minTransmissionSpeed(batches3, h3);
        System.out.println("Additional Test:");
        System.out.println("batches = " + Arrays.toString(batches3));
        System.out.println("h = " + h3);
        System.out.println("Minimum transmission speed = " + result3);
    }
}