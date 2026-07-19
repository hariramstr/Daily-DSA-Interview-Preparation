import java.util.*;

/*
Problem Title: Minimum Packet Size for Sequential Upload Windows

Problem Description:
A media company needs to upload a sequence of video clips in the given order. The i-th clip has size clips[i] megabytes. The uploader sends data in fixed-size packets, and every packet can carry at most P megabytes. A single clip may be split across multiple packets, but packets cannot mix data from different upload windows.

You are also given an integer w, the maximum number of upload windows available. In one upload window, the company uploads a contiguous group of clips in order, and the total size of all clips assigned to that window must be fully transmitted using packets of size P. If the total size of a window is S, then that window consumes ceil(S / P) packets. However, each upload window is allowed to use at most one packet. That means the total size of clips placed in any single window must be at most P.

Your task is to find the minimum integer packet size P such that all clips can be partitioned into at most w contiguous upload windows, with each window having total size at most P.

In other words, choose the smallest possible P so the array can be split into at most w contiguous parts, and the sum of each part does not exceed P.

Return that minimum packet size.

Constraints:
- 1 <= clips.length <= 100000
- 1 <= clips[i] <= 1000000000
- 1 <= w <= clips.length
- The answer fits in a 64-bit signed integer.

Example 1:
Input: clips = [8, 3, 5, 7, 2], w = 3
Output: 10
Explanation: One optimal partition is [8], [3,5], [7,2]. The window sums are 8, 8, and 9, so packet size 10 is sufficient. Any packet size smaller than 10 fails because with P = 9, the clips would require more than 3 contiguous windows.

Example 2:
Input: clips = [4, 4, 4, 4], w = 2
Output: 8
Explanation: Split the clips as [4,4] and [4,4]. Each window has total size 8, so P = 8 works. P = 7 is impossible because no window may exceed 7, forcing at least 4 windows.
*/

public class Solution {

    /**
     * Finds the minimum packet size P such that the clips can be split into at most
     * w contiguous upload windows, where the sum of each window is at most P.
     *
     * Core idea:
     * 1. If P is too small, we will need too many windows.
     * 2. If P is large enough, we can fit the clips into at most w windows.
     * 3. This "works / does not work" behavior is monotonic:
     *    - If some P works, then any larger P also works.
     *    - If some P does not work, then any smaller P also does not work.
     * 4. Because of this monotonic property, binary search is the correct tool.
     *
     * Search range:
     * - Lower bound = max(clips[i]), because every single clip must fit in some window.
     * - Upper bound = sum(clips), because one window containing all clips would need that much.
     *
     * @param clips the array of clip sizes in upload order
     * @param w the maximum number of contiguous upload windows allowed
     * @return the minimum integer packet size P that allows a valid partition
     * Time complexity: O(n log S), where n is clips.length and S is the sum of all clip sizes
     * Space complexity: O(1), excluding input storage
     */
    public long minimumPacketSize(int[] clips, int w) {
        // The smallest possible packet size must be at least the largest single clip,
        // because no clip can be split across different windows in terms of window sum limit.
        long left = 0;

        // The largest possible packet size we ever need to consider is the total sum of all clips,
        // which corresponds to putting everything into one single window.
        long right = 0;

        // Compute both:
        // - left as the maximum clip size
        // - right as the total sum
        for (int clip : clips) {
            left = Math.max(left, clip);
            right += clip;
        }

        // Standard binary search on the answer space [left, right].
        // We are looking for the smallest feasible packet size.
        while (left < right) {
            // Midpoint chosen this way to avoid overflow:
            // instead of (left + right) / 2, use left + (right - left) / 2.
            long mid = left + (right - left) / 2;

            // Check whether packet size "mid" is sufficient.
            if (canPartition(clips, w, mid)) {
                // If mid works, try to find an even smaller valid answer.
                right = mid;
            } else {
                // If mid does not work, all smaller values also do not work.
                // So we must search in the larger half.
                left = mid + 1;
            }
        }

        // At the end of binary search, left == right and points to the minimum feasible value.
        return left;
    }

    /**
     * Determines whether the clips can be partitioned into at most w contiguous windows
     * such that the sum of each window does not exceed the given packet size limit.
     *
     * Greedy strategy:
     * - Scan clips from left to right.
     * - Keep adding clips to the current window while the sum stays <= limit.
     * - As soon as adding the next clip would exceed limit, start a new window.
     *
     * Why greedy is correct here:
     * - To minimize the number of windows used for a fixed limit, we should pack each
     *   window as much as possible before starting the next one.
     * - Any earlier split would only increase or keep the same number of windows, never reduce it.
     *
     * @param clips the array of clip sizes in order
     * @param w the maximum number of windows allowed
     * @param limit the candidate packet size P being tested
     * @return true if the clips can be split into at most w contiguous windows with each window sum <= limit; false otherwise
     * Time complexity: O(n), where n is clips.length
     * Space complexity: O(1)
     */
    public boolean canPartition(int[] clips, int w, long limit) {
        // We start with one window already "open".
        int windowsUsed = 1;

        // Running sum of the current window.
        long currentWindowSum = 0;

        // Process clips in order because partitions must be contiguous.
        for (int clip : clips) {
            // Safety check:
            // If a single clip is larger than the limit, then it is impossible to place it anywhere.
            if (clip > limit) {
                return false;
            }

            // Try to add this clip to the current window.
            if (currentWindowSum + clip <= limit) {
                // It fits, so keep extending the current window.
                currentWindowSum += clip;
            } else {
                // It does not fit, so we must start a new window beginning with this clip.
                windowsUsed++;
                currentWindowSum = clip;

                // Early exit:
                // If we already exceeded the allowed number of windows, limit is not feasible.
                if (windowsUsed > w) {
                    return false;
                }
            }
        }

        // If we finished processing all clips without exceeding w windows, the limit works.
        return true;
    }

    /**
     * Utility method to print an array in a beginner-friendly format.
     *
     * @param arr the integer array to print
     * @return a string representation of the array
     * Time complexity: O(n)
     * Space complexity: O(n) due to string construction
     */
    public String arrayToString(int[] arr) {
        return Arrays.toString(arr);
    }

    /**
     * Demonstrates the solution using the sample inputs from the problem statement
     * and a few additional sanity checks.
     *
     * Expected sample outputs:
     * - clips = [8, 3, 5, 7, 2], w = 3 => 10
     * - clips = [4, 4, 4, 4], w = 2 => 8
     *
     * @param args command-line arguments (not used)
     * @return nothing
     * Time complexity: O(k * n log S) for k demo test cases
     * Space complexity: O(1), excluding input arrays
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Sample test case 1
        int[] clips1 = {8, 3, 5, 7, 2};
        int w1 = 3;
        long result1 = solution.minimumPacketSize(clips1, w1);
        System.out.println("Sample 1:");
        System.out.println("clips = " + solution.arrayToString(clips1) + ", w = " + w1);
        System.out.println("Minimum packet size = " + result1);
        System.out.println("Expected = 10");
        System.out.println();

        // Sample test case 2
        int[] clips2 = {4, 4, 4, 4};
        int w2 = 2;
        long result2 = solution.minimumPacketSize(clips2, w2);
        System.out.println("Sample 2:");
        System.out.println("clips = " + solution.arrayToString(clips2) + ", w = " + w2);
        System.out.println("Minimum packet size = " + result2);
        System.out.println("Expected = 8");
        System.out.println();

        // Additional sanity check:
        // If only one window is allowed, answer must be the total sum.
        int[] clips3 = {2, 6, 1, 9};
        int w3 = 1;
        long result3 = solution.minimumPacketSize(clips3, w3);
        System.out.println("Additional Test 1:");
        System.out.println("clips = " + solution.arrayToString(clips3) + ", w = " + w3);
        System.out.println("Minimum packet size = " + result3);
        System.out.println("Expected = 18");
        System.out.println();

        // Additional sanity check:
        // If number of windows equals number of clips, answer must be the maximum element.
        int[] clips4 = {5, 1, 7, 3};
        int w4 = 4;
        long result4 = solution.minimumPacketSize(clips4, w4);
        System.out.println("Additional Test 2:");
        System.out.println("clips = " + solution.arrayToString(clips4) + ", w = " + w4);
        System.out.println("Minimum packet size = " + result4);
        System.out.println("Expected = 7");
    }
}