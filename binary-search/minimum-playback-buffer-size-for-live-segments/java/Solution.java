import java.util.*;

/*
 * Title: Minimum Playback Buffer Size for Live Segments
 * Difficulty: Medium
 * Topic: Binary Search
 *
 * Problem Description:
 * A video platform is preparing a live event made of n consecutive segments.
 * Segment i has size segments[i] megabytes. The player downloads data into a
 * temporary buffer before each segment begins, and the same fixed buffer size
 * must be used for the entire event.
 *
 * If a segment is larger than the buffer, it cannot be played.
 * The platform is allowed to split the event into at most k playback sessions.
 * Each session must contain a contiguous group of segments, and the total size
 * of all segments assigned to a single session cannot exceed the buffer size.
 *
 * Task:
 * Compute the minimum buffer size required so that all segments can be played
 * in order using at most k sessions.
 *
 * In other words:
 * Partition the array into at most k contiguous parts while minimizing the
 * maximum part sum.
 *
 * Constraints:
 * - 1 <= n <= 100000
 * - 1 <= segments[i] <= 1000000000
 * - 1 <= k <= n
 * - The answer fits in a 64-bit signed integer
 *
 * Example 1:
 * segments = [8, 3, 5, 7, 2], k = 3
 * Output: 9
 * Explanation:
 * One optimal partition is [8], [3, 5], [7, 2].
 * The session sums are 8, 8, 9, so the minimum required buffer is 9.
 *
 * Example 2:
 * segments = [4, 4, 4, 4], k = 2
 * Output: 8
 * Explanation:
 * We can split as [4,4] and [4,4].
 * No buffer smaller than 8 can fit all four segments into only two contiguous sessions.
 *
 * Common Interview Approach:
 * Binary search the answer.
 * For a candidate buffer size x, greedily scan from left to right and count
 * how many sessions are needed if each session takes as many consecutive
 * segments as possible without exceeding x.
 * If the required number of sessions is at most k, then x is feasible;
 * otherwise it is too small.
 */

public class Solution {

    /**
     * Computes the minimum possible buffer size required to partition the given
     * segments into at most k contiguous playback sessions.
     *
     * Core idea:
     * 1. The answer must be at least the largest single segment, because every
     *    segment must fit into the buffer by itself.
     * 2. The answer can be at most the sum of all segments, because one session
     *    containing everything is always possible when k >= 1.
     * 3. We binary search this answer range.
     * 4. For each candidate buffer size, we greedily determine how many sessions
     *    are needed.
     * 5. If we can finish using at most k sessions, the candidate is feasible,
     *    so we try a smaller buffer.
     * 6. Otherwise, the candidate is too small, so we try a larger buffer.
     *
     * @param segments the sizes of consecutive live segments
     * @param k the maximum number of contiguous playback sessions allowed
     * @return the minimum buffer size that allows all segments to be played in order
     * @implNote Time complexity: O(n * log(sum of segments))
     * @implNote Space complexity: O(1)
     */
    public long minimumBufferSize(int[] segments, int k) {
        // Lower bound of the answer:
        // The buffer must be at least as large as the biggest segment,
        // otherwise that segment could never fit in any session.
        long left = 0;

        // Upper bound of the answer:
        // If we place all segments into one single session, the required buffer
        // would be the total sum of all segment sizes.
        long right = 0;

        // Compute both bounds in one pass.
        for (int size : segments) {
            left = Math.max(left, size);
            right += size;
        }

        // Binary search for the smallest feasible buffer size.
        //
        // Invariant:
        // - Every value < left is known to be infeasible.
        // - Some value in [left, right] is feasible.
        //
        // We continue until the search space collapses to one value.
        while (left < right) {
            // Use this form to avoid overflow:
            // mid = left + (right - left) / 2
            long mid = left + (right - left) / 2;

            // Check whether this candidate buffer size is enough.
            if (canPlayWithinKSessions(segments, k, mid)) {
                // mid is feasible.
                // Since we want the minimum feasible value, keep searching left half,
                // including mid itself.
                right = mid;
            } else {
                // mid is too small.
                // The answer must be strictly larger than mid.
                left = mid + 1;
            }
        }

        // At the end, left == right and points to the minimum feasible answer.
        return left;
    }

    /**
     * Determines whether all segments can be partitioned into at most k contiguous
     * sessions such that no session sum exceeds the given buffer size.
     *
     * Greedy strategy:
     * - Start a session and keep adding consecutive segments while the total
     *   remains <= bufferSize.
     * - As soon as adding the next segment would exceed bufferSize, start a new session.
     * - This greedy method minimizes the number of sessions needed for this fixed
     *   buffer size, because each session is packed as much as possible from left to right.
     *
     * Why this works:
     * - For a fixed maximum allowed session sum, taking fewer items than possible
     *   in the current session can never help reduce the total number of sessions.
     * - Therefore, the greedy count is the minimum number of sessions needed for
     *   that candidate buffer size.
     *
     * @param segments the sizes of consecutive live segments
     * @param k the maximum number of sessions allowed
     * @param bufferSize the candidate buffer size being tested
     * @return true if all segments can be played using at most k sessions; false otherwise
     * @implNote Time complexity: O(n)
     * @implNote Space complexity: O(1)
     */
    public boolean canPlayWithinKSessions(int[] segments, int k, long bufferSize) {
        // We need at least one session if there is at least one segment.
        int sessionsUsed = 1;

        // Running sum of the current session.
        long currentSessionSum = 0;

        // Process segments from left to right, preserving order.
        for (int size : segments) {
            // Safety check:
            // If a single segment is larger than the candidate buffer size,
            // then this candidate is immediately impossible.
            if (size > bufferSize) {
                return false;
            }

            // If the current segment fits into the current session, add it.
            if (currentSessionSum + size <= bufferSize) {
                currentSessionSum += size;
            } else {
                // Otherwise, we must start a new session beginning with this segment.
                sessionsUsed++;
                currentSessionSum = size;

                // Early stopping optimization:
                // If we already exceeded k sessions, no need to continue scanning.
                if (sessionsUsed > k) {
                    return false;
                }
            }
        }

        // If we finished with at most k sessions, the candidate buffer size works.
        return true;
    }

    /**
     * Helper method to print an integer array in a beginner-friendly format.
     *
     * @param arr the array to convert to string
     * @return a string representation such as [1, 2, 3]
     * @implNote Time complexity: O(n)
     * @implNote Space complexity: O(n) for the generated string
     */
    public String arrayToString(int[] arr) {
        return Arrays.toString(arr);
    }

    /**
     * Demonstrates the solution on sample test cases from the problem statement
     * and a few additional sanity checks.
     *
     * Expected outputs:
     * - Example 1: 9
     * - Example 2: 8
     *
     * @param args command-line arguments (not used)
     * @implNote Time complexity: O(total input size across demonstrations * log(sum))
     * @implNote Space complexity: O(1) excluding output
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Example 1 from the problem statement.
        int[] segments1 = {8, 3, 5, 7, 2};
        int k1 = 3;
        long result1 = solution.minimumBufferSize(segments1, k1);
        System.out.println("Example 1");
        System.out.println("segments = " + solution.arrayToString(segments1) + ", k = " + k1);
        System.out.println("Minimum buffer size = " + result1);
        System.out.println("Expected = 9");
        System.out.println();

        // Example 2 from the problem statement.
        int[] segments2 = {4, 4, 4, 4};
        int k2 = 2;
        long result2 = solution.minimumBufferSize(segments2, k2);
        System.out.println("Example 2");
        System.out.println("segments = " + solution.arrayToString(segments2) + ", k = " + k2);
        System.out.println("Minimum buffer size = " + result2);
        System.out.println("Expected = 8");
        System.out.println();

        // Additional sanity check:
        // If k equals n, each segment can be its own session,
        // so the answer should be the maximum element.
        int[] segments3 = {1, 10, 3, 2};
        int k3 = 4;
        long result3 = solution.minimumBufferSize(segments3, k3);
        System.out.println("Additional Test 1");
        System.out.println("segments = " + solution.arrayToString(segments3) + ", k = " + k3);
        System.out.println("Minimum buffer size = " + result3);
        System.out.println("Expected = 10");
        System.out.println();

        // Additional sanity check:
        // If k is 1, everything must be in one session,
        // so the answer should be the total sum.
        int[] segments4 = {2, 6, 4, 5};
        int k4 = 1;
        long result4 = solution.minimumBufferSize(segments4, k4);
        System.out.println("Additional Test 2");
        System.out.println("segments = " + solution.arrayToString(segments4) + ", k = " + k4);
        System.out.println("Minimum buffer size = " + result4);
        System.out.println("Expected = 17");
    }
}