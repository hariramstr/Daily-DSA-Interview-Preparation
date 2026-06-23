import java.util.*;

/*
 * Title: Combine Smallest File Chunks
 * Difficulty: Easy
 * Topic: Heaps and Priority Queues
 *
 * Problem Description:
 * You are given an array chunks where chunks[i] is the size of the i-th file chunk
 * waiting to be merged into one final archive. In one operation, you must take the
 * two smallest available chunks, merge them, and pay a cost equal to the sum of
 * their sizes. The merged chunk is then added back to the pool of available chunks.
 * Continue until only one chunk remains.
 *
 * Return the total cost of all merge operations.
 *
 * This models a common systems task: repeatedly combining small pieces of data where
 * each intermediate merge also creates a new piece that may be merged again later.
 * To minimize the total cost, you should always merge the smallest available chunks first.
 *
 * Constraints:
 * - 1 <= chunks.length <= 100000
 * - 1 <= chunks[i] <= 1000000000
 * - The answer may not fit in a 32-bit integer, so use 64-bit arithmetic where needed.
 *
 * Example 1:
 * Input: chunks = [4, 3, 2, 6]
 * Output: 29
 * Explanation:
 * - Merge 2 and 3 -> cost 5, chunks become [4, 5, 6]
 * - Merge 4 and 5 -> cost 9, chunks become [6, 9]
 * - Merge 6 and 9 -> cost 15
 * Total cost = 5 + 9 + 15 = 29
 *
 * Example 2:
 * Input: chunks = [10]
 * Output: 0
 * Explanation:
 * There is only one chunk, so no merge is needed.
 */

public class Solution {

    /**
     * Computes the minimum total cost required to merge all file chunks into one chunk.
     *
     * The optimal strategy is greedy:
     * always merge the two smallest currently available chunks first.
     * A min-heap (PriorityQueue) allows us to repeatedly extract the two smallest
     * values efficiently and insert the merged result back.
     *
     * @param chunks an array where each element represents the size of a file chunk
     * @return the minimum total merge cost as a long
     * Time complexity: O(n log n), where n is the number of chunks
     * Space complexity: O(n), due to the priority queue
     */
    public long minimumMergeCost(int[] chunks) {
        // If there is only one chunk, no merge is needed, so the cost is 0.
        if (chunks == null || chunks.length <= 1) {
            return 0L;
        }

        // Create a min-heap.
        // Java's PriorityQueue is a min-heap by default, meaning the smallest
        // element is always removed first with poll().
        PriorityQueue<Long> minHeap = new PriorityQueue<>();

        // Insert all chunk sizes into the min-heap.
        // We convert each int to long before storing it to ensure all arithmetic
        // remains safe even when sums become larger than Integer.MAX_VALUE.
        for (int chunk : chunks) {
            minHeap.offer((long) chunk);
        }

        // This variable accumulates the total cost of all merge operations.
        long totalCost = 0L;

        // Keep merging until only one chunk remains.
        // Why? Because each operation reduces the number of chunks by exactly one:
        // remove two chunks, add one merged chunk back.
        while (minHeap.size() > 1) {
            // Step 1: Take the smallest chunk currently available.
            long firstSmallest = minHeap.poll();

            // Step 2: Take the second smallest chunk currently available.
            long secondSmallest = minHeap.poll();

            // Step 3: Merge them.
            // The cost of this merge is the sum of the two chunk sizes.
            long mergedSize = firstSmallest + secondSmallest;

            // Step 4: Add this merge cost to the running total.
            totalCost += mergedSize;

            // Step 5: Put the merged chunk back into the heap,
            // because it may need to be merged again later.
            minHeap.offer(mergedSize);
        }

        // When the loop ends, exactly one chunk remains, and totalCost contains
        // the minimum total cost of all merges.
        return totalCost;
    }

    /**
     * A helper method that demonstrates the merge process in a beginner-friendly way.
     * This method prints each step of the algorithm for a given input.
     *
     * @param chunks an array of chunk sizes to merge
     * @return the final minimum total merge cost as a long
     * Time complexity: O(n log n), where n is the number of chunks
     * Space complexity: O(n), due to the priority queue
     */
    public long minimumMergeCostWithTrace(int[] chunks) {
        if (chunks == null || chunks.length == 0) {
            System.out.println("Input: null or empty array");
            System.out.println("No chunks to merge. Total cost = 0");
            return 0L;
        }

        System.out.println("Input chunks: " + Arrays.toString(chunks));

        if (chunks.length == 1) {
            System.out.println("Only one chunk exists, so no merge is needed.");
            System.out.println("Total cost = 0");
            return 0L;
        }

        PriorityQueue<Long> minHeap = new PriorityQueue<>();
        for (int chunk : chunks) {
            minHeap.offer((long) chunk);
        }

        long totalCost = 0L;
        int step = 1;

        while (minHeap.size() > 1) {
            long firstSmallest = minHeap.poll();
            long secondSmallest = minHeap.poll();
            long mergedSize = firstSmallest + secondSmallest;
            totalCost += mergedSize;
            minHeap.offer(mergedSize);

            System.out.println(
                "Step " + step + ": merge " + firstSmallest + " and " + secondSmallest
                + " -> cost " + mergedSize + ", running total = " + totalCost
            );
            step++;
        }

        System.out.println("Final minimum total cost = " + totalCost);
        return totalCost;
    }

    /**
     * Runs sample demonstrations for the problem statement examples.
     *
     * @param args command-line arguments (not used)
     * @return nothing
     * Time complexity: O(1) for the fixed demonstrations here, excluding method internals
     * Space complexity: O(1) for the fixed demonstrations here, excluding method internals
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Example 1 from the problem statement:
        // chunks = [4, 3, 2, 6]
        // Expected output: 29
        int[] chunks1 = {4, 3, 2, 6};
        System.out.println("Example 1:");
        long result1 = solution.minimumMergeCostWithTrace(chunks1);
        System.out.println("Returned result: " + result1);
        System.out.println("Expected result: 29");
        System.out.println();

        // Example 2 from the problem statement:
        // chunks = [10]
        // Expected output: 0
        int[] chunks2 = {10};
        System.out.println("Example 2:");
        long result2 = solution.minimumMergeCostWithTrace(chunks2);
        System.out.println("Returned result: " + result2);
        System.out.println("Expected result: 0");
        System.out.println();

        // Additional quick demonstration using the main algorithm directly.
        int[] chunks3 = {1, 2, 3, 4, 5};
        System.out.println("Additional Example:");
        System.out.println("Input chunks: " + Arrays.toString(chunks3));
        System.out.println("Minimum merge cost: " + solution.minimumMergeCost(chunks3));
    }
}