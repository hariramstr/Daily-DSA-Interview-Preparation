import java.util.*;

/*
Problem Title: Minimum Buffer Size for Batched Video Uploads

Problem Description:
A media platform needs to upload a sequence of video clips in the given order. The size of the i-th clip is clips[i] megabytes. The platform has a temporary upload buffer with capacity B megabytes. Clips must be grouped into consecutive batches, and the total size of each batch cannot exceed B. Once a batch is formed, it is uploaded together. You are also given an integer maxBatches, the maximum number of batches the platform is allowed to use.

Your task is to find the minimum buffer size B such that all clips can be uploaded in order using at most maxBatches batches.

This is not a reordering problem: clips must stay in their original order, and every clip must belong to exactly one batch. A buffer size is considered feasible if it is possible to partition the array into at most maxBatches contiguous groups where each group sum is at most B.

Return the smallest feasible buffer size.

Constraints:
- 1 <= clips.length <= 100000
- 1 <= clips[i] <= 1000000000
- 1 <= maxBatches <= clips.length
- The answer fits in a 64-bit signed integer.

Example 1:
Input: clips = [8, 3, 5, 7, 2], maxBatches = 3
Output: 10
Explanation: One optimal partition is [8], [3,5], [7,2]. The largest batch size is 10. No buffer smaller than 10 can fit the clips in at most 3 batches.

Example 2:
Input: clips = [4, 4, 4, 4], maxBatches = 2
Output: 8
Explanation: We can split the clips as [4,4] and [4,4]. A buffer of 7 is impossible because each batch can hold at most one clip pair of size 8, so at least 3 batches would be needed.

Typical approach:
- A candidate buffer size can be checked in linear time.
- Then binary search is used on the answer range from max(clips) to sum(clips).
*/

public class Solution {

    /**
     * Finds the minimum feasible buffer size needed to upload all clips in order
     * using at most maxBatches contiguous batches.
     *
     * Core idea:
     * 1. The answer cannot be smaller than the largest single clip, because every clip
     *    must fit into some batch.
     * 2. The answer cannot be larger than the sum of all clips, because one single batch
     *    containing everything is always possible if allowed.
     * 3. For any candidate buffer size B, we can greedily scan from left to right and
     *    count how many batches are required:
     *      - Keep adding clips to the current batch while the sum stays <= B.
     *      - If adding the next clip would exceed B, start a new batch.
     * 4. If a candidate B can finish within at most maxBatches batches, then B is feasible.
     *    Otherwise it is not feasible.
     * 5. Because feasibility is monotonic:
     *      - If B works, any larger value also works.
     *      - If B does not work, any smaller value also does not work.
     *    This monotonic property allows binary search.
     *
     * @param clips the array of clip sizes in upload order
     * @param maxBatches the maximum number of contiguous batches allowed
     * @return the smallest feasible buffer size as a long
     * Time complexity: O(n log S), where n is clips.length and S is the search range size
     *                  between max(clips) and sum(clips)
     * Space complexity: O(1), ignoring input storage
     */
    public long minimumBufferSize(int[] clips, int maxBatches) {
        // The smallest possible answer must be at least the largest clip,
        // because no batch can be smaller than an individual clip it contains.
        long left = 0L;

        // The largest possible answer is the total sum of all clips,
        // meaning we upload everything in one batch.
        long right = 0L;

        // Compute the binary search boundaries carefully using long,
        // because sums can exceed the int range.
        for (int clip : clips) {
            left = Math.max(left, clip);
            right += clip;
        }

        // We now binary search for the minimum feasible buffer size.
        // Invariant:
        // - The answer is always somewhere in [left, right].
        while (left < right) {
            // Use this form to avoid overflow:
            // mid = left + (right - left) / 2
            long mid = left + (right - left) / 2;

            // Check whether this candidate buffer size is enough.
            if (canUploadWithinBatches(clips, maxBatches, mid)) {
                // If mid is feasible, then the true answer could be mid
                // or something even smaller.
                right = mid;
            } else {
                // If mid is not feasible, then we must increase the buffer size.
                left = mid + 1;
            }
        }

        // When left == right, we have found the smallest feasible value.
        return left;
    }

    /**
     * Checks whether all clips can be uploaded in order using at most maxBatches batches
     * when the buffer capacity is candidateBuffer.
     *
     * Greedy reasoning:
     * - To minimize the number of batches for a fixed buffer size, we should pack each batch
     *   as much as possible before starting a new one.
     * - Therefore, scanning left to right and only starting a new batch when necessary
     *   gives the minimum number of batches required for that candidate buffer.
     *
     * Detailed process:
     * - Start with one batch.
     * - Keep a running sum of the current batch.
     * - For each clip:
     *     * If adding it stays within candidateBuffer, add it to the current batch.
     *     * Otherwise, start a new batch with this clip.
     * - If the number of batches ever exceeds maxBatches, the candidate is not feasible.
     *
     * @param clips the array of clip sizes in upload order
     * @param maxBatches the maximum number of batches allowed
     * @param candidateBuffer the buffer size being tested
     * @return true if candidateBuffer is feasible, false otherwise
     * Time complexity: O(n), where n is clips.length
     * Space complexity: O(1)
     */
    public boolean canUploadWithinBatches(int[] clips, int maxBatches, long candidateBuffer) {
        // We begin with one batch ready to receive clips.
        int batchesUsed = 1;

        // This stores the total size currently placed in the active batch.
        long currentBatchSum = 0L;

        // Process clips in the original order, because reordering is not allowed.
        for (int clip : clips) {
            // Safety check:
            // If a single clip is larger than the candidate buffer,
            // then this candidate is immediately impossible.
            if (clip > candidateBuffer) {
                return false;
            }

            // If the current clip fits into the current batch, keep packing.
            if (currentBatchSum + clip <= candidateBuffer) {
                currentBatchSum += clip;
            } else {
                // Otherwise, we must start a new batch.
                batchesUsed++;
                currentBatchSum = clip;

                // Early exit:
                // If we already exceeded the allowed number of batches,
                // there is no need to continue scanning.
                if (batchesUsed > maxBatches) {
                    return false;
                }
            }
        }

        // If we finished without exceeding maxBatches, the candidate works.
        return true;
    }

    /**
     * Demonstrates the solution using the sample inputs from the problem statement.
     *
     * Expected outputs:
     * - Example 1: 10
     * - Example 2: 8
     *
     * Also includes a few extra demonstrations for clarity.
     *
     * @param args command-line arguments (not used)
     * @return nothing
     * Time complexity: O(total input size used in demonstrations * log S)
     * Space complexity: O(1), ignoring input arrays
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Example 1
        int[] clips1 = {8, 3, 5, 7, 2};
        int maxBatches1 = 3;
        long result1 = solution.minimumBufferSize(clips1, maxBatches1);
        System.out.println("Example 1 Result: " + result1);
        // Verified:
        // With buffer 10, one valid partition is [8], [3,5], [7,2].
        // Largest batch sum = 10, so answer is 10.

        // Example 2
        int[] clips2 = {4, 4, 4, 4};
        int maxBatches2 = 2;
        long result2 = solution.minimumBufferSize(clips2, maxBatches2);
        System.out.println("Example 2 Result: " + result2);
        // Verified:
        // With buffer 8, partition [4,4], [4,4] works.
        // Buffer 7 fails because each batch can hold only one 4 or at most one clip pair is impossible,
        // causing more than 2 batches. So answer is 8.

        // Extra example: each clip must be its own batch when maxBatches == clips.length
        int[] clips3 = {5, 1, 9, 2};
        int maxBatches3 = 4;
        long result3 = solution.minimumBufferSize(clips3, maxBatches3);
        System.out.println("Extra Example 1 Result: " + result3);
        // Expected: 9 (largest single clip)

        // Extra example: only one batch allowed, so answer is total sum
        int[] clips4 = {5, 1, 9, 2};
        int maxBatches4 = 1;
        long result4 = solution.minimumBufferSize(clips4, maxBatches4);
        System.out.println("Extra Example 2 Result: " + result4);
        // Expected: 17
    }
}