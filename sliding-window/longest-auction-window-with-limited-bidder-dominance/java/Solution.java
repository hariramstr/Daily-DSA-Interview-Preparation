import java.util.*;

/*
 * Title: Longest Auction Window With Limited Bidder Dominance
 * Difficulty: Hard
 * Topic: Sliding Window
 *
 * Problem Description:
 * You are given an array bids where bids[i] is the bidder ID that placed the i-th bid
 * in a live auction feed, and an integer limit. A contiguous window of bids is called
 * fair if no single bidder appears more than limit times more than all other bidders
 * combined inside that window.
 *
 * Formally, for a window of length L, if the maximum frequency of any bidder in the
 * window is maxFreq, then the window is fair when:
 *
 *     maxFreq <= (L - maxFreq) + limit
 *
 * Rearranging:
 *
 *     2 * maxFreq <= L + limit
 *
 * Your task is to return the length of the longest fair contiguous window.
 *
 * This models a fraud-detection rule for auction systems: a bidder may be active,
 * but cannot dominate the stream too heavily relative to everyone else unless the
 * configured tolerance limit allows it.
 *
 * Constraints:
 * - 1 <= bids.length <= 200000
 * - 1 <= bids[i] <= 10^9
 * - 0 <= limit <= bids.length
 * - The answer must be computed for a contiguous subarray
 *
 * Example 1:
 * Input: bids = [4, 7, 4, 4, 8, 7, 4, 9], limit = 1
 * Output: 5
 *
 * Example 2:
 * Input: bids = [5, 5, 5, 2, 3, 5, 2, 2, 2], limit = 0
 * Output: 6
 */

public class Solution {

    /**
     * Computes the length of the longest fair contiguous window.
     *
     * A window is fair if:
     *     2 * maxFreq <= windowLength + limit
     * where maxFreq is the highest bidder frequency inside the current window.
     *
     * Core idea:
     * We use a sliding window with two pointers [left, right].
     * For each right endpoint, we add bids[right] into the window.
     * Then we repeatedly shrink from the left while the window is unfair.
     *
     * The main challenge is maintaining the current maximum frequency efficiently.
     * Because bidder IDs can be large, we use a HashMap<Integer, Integer> for counts.
     * To also query the current maximum frequency quickly, we maintain:
     * - countByBidder: bidder -> current frequency in the window
     * - freqCount: frequency -> how many bidders currently have that exact frequency
     * - currentMaxFreq: the largest frequency currently present in the window
     *
     * When a bidder's count changes from oldFreq to newFreq:
     * - decrement freqCount[oldFreq]
     * - increment freqCount[newFreq]
     * - update currentMaxFreq if needed
     *
     * When shrinking the window, currentMaxFreq may become invalid if no bidder still
     * has that frequency. In that case, we decrease currentMaxFreq until it points to
     * a frequency that is still present.
     *
     * This guarantees correctness because the fairness condition depends on the exact
     * current maximum frequency, not an outdated upper bound.
     *
     * @param bids the array of bidder IDs in chronological order
     * @param limit the allowed dominance tolerance
     * @return the maximum length of a fair contiguous subarray
     * Time complexity: O(n), because each element enters and leaves the window at most once,
     * and currentMaxFreq only moves downward as needed across the whole process.
     * Space complexity: O(n), in the worst case when all bidder IDs are distinct.
     */
    public int longestFairWindow(int[] bids, int limit) {
        int n = bids.length;

        // Maps each bidder ID to its current frequency inside the sliding window.
        Map<Integer, Integer> countByBidder = new HashMap<>();

        // Maps a frequency value f to the number of bidders that currently appear exactly f times.
        // This lets us know whether the current maximum frequency still exists after removals.
        Map<Integer, Integer> freqCount = new HashMap<>();

        int left = 0;
        int best = 0;

        // Exact maximum frequency currently present in the window.
        int currentMaxFreq = 0;

        // Expand the window one element at a time.
        for (int right = 0; right < n; right++) {
            int bidder = bids[right];

            // Step 1: Add bids[right] into the window.
            int oldFreq = countByBidder.getOrDefault(bidder, 0);
            int newFreq = oldFreq + 1;
            countByBidder.put(bidder, newFreq);

            // Update the "how many bidders have frequency f" structure.
            if (oldFreq > 0) {
                decrementFrequencyBucket(freqCount, oldFreq);
            }
            incrementFrequencyBucket(freqCount, newFreq);

            // If this bidder now has a larger frequency than any bidder before,
            // update the exact current maximum frequency.
            if (newFreq > currentMaxFreq) {
                currentMaxFreq = newFreq;
            }

            // Step 2: While the window is unfair, shrink it from the left.
            //
            // Fairness condition:
            //     2 * currentMaxFreq <= windowLength + limit
            //
            // If this is false, some bidder dominates too much, so we must move left forward.
            while (!isFairWindow(right - left + 1, currentMaxFreq, limit)) {
                int leftBidder = bids[left];
                int leftOldFreq = countByBidder.get(leftBidder);
                int leftNewFreq = leftOldFreq - 1;

                // Remove one occurrence of bids[left] from the window.
                decrementFrequencyBucket(freqCount, leftOldFreq);

                if (leftNewFreq == 0) {
                    countByBidder.remove(leftBidder);
                } else {
                    countByBidder.put(leftBidder, leftNewFreq);
                    incrementFrequencyBucket(freqCount, leftNewFreq);
                }

                left++;

                // After shrinking, currentMaxFreq might no longer exist.
                // Example:
                //   if currentMaxFreq was 4, but after removal no bidder has frequency 4,
                //   then we must decrease it until we find an existing frequency.
                while (currentMaxFreq > 0 && freqCount.getOrDefault(currentMaxFreq, 0) == 0) {
                    currentMaxFreq--;
                }
            }

            // Step 3: The current window is fair, so update the answer.
            best = Math.max(best, right - left + 1);
        }

        return best;
    }

    /**
     * Checks whether a window is fair using the transformed inequality:
     *     2 * maxFreq <= windowLength + limit
     *
     * @param windowLength the number of elements in the current window
     * @param maxFreq the maximum bidder frequency in the current window
     * @param limit the allowed dominance tolerance
     * @return true if the window is fair, false otherwise
     * Time complexity: O(1)
     * Space complexity: O(1)
     */
    public boolean isFairWindow(int windowLength, int maxFreq, int limit) {
        return 2L * maxFreq <= (long) windowLength + limit;
    }

    /**
     * Increases the number of bidders that currently have the given frequency.
     *
     * @param freqCount map from frequency to number of bidders with that frequency
     * @param frequency the frequency bucket to increment
     * @return nothing
     * Time complexity: O(1) average
     * Space complexity: O(1)
     */
    public void incrementFrequencyBucket(Map<Integer, Integer> freqCount, int frequency) {
        freqCount.put(frequency, freqCount.getOrDefault(frequency, 0) + 1);
    }

    /**
     * Decreases the number of bidders that currently have the given frequency.
     * If the bucket becomes zero, it is removed from the map to keep the structure clean.
     *
     * @param freqCount map from frequency to number of bidders with that frequency
     * @param frequency the frequency bucket to decrement
     * @return nothing
     * Time complexity: O(1) average
     * Space complexity: O(1)
     */
    public void decrementFrequencyBucket(Map<Integer, Integer> freqCount, int frequency) {
        int updated = freqCount.getOrDefault(frequency, 0) - 1;
        if (updated <= 0) {
            freqCount.remove(frequency);
        } else {
            freqCount.put(frequency, updated);
        }
    }

    /**
     * Demonstrates the solution on the sample test cases from the problem statement.
     *
     * @param args command-line arguments, not used
     * @return nothing
     * Time complexity: O(n) for each demonstrated test case
     * Space complexity: O(n) for each demonstrated test case
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        int[] bids1 = {4, 7, 4, 4, 8, 7, 4, 9};
        int limit1 = 1;
        int result1 = solution.longestFairWindow(bids1, limit1);
        System.out.println("Example 1 result: " + result1);
        System.out.println("Expected: 5");

        int[] bids2 = {5, 5, 5, 2, 3, 5, 2, 2, 2};
        int limit2 = 0;
        int result2 = solution.longestFairWindow(bids2, limit2);
        System.out.println("Example 2 result: " + result2);
        System.out.println("Expected: 6");

        int[] bids3 = {1};
        int limit3 = 0;
        int result3 = solution.longestFairWindow(bids3, limit3);
        System.out.println("Additional test 1 result: " + result3);

        int[] bids4 = {1, 1, 2, 2, 3, 3};
        int limit4 = 0;
        int result4 = solution.longestFairWindow(bids4, limit4);
        System.out.println("Additional test 2 result: " + result4);
    }
}