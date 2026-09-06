import java.util.*;

/*
Title: Merge Release Streams by Highest Current Severity
Difficulty: Medium
Topic: Heaps and Priority Queues

Problem Description:
A company receives incident reports from multiple release streams. Each stream is already sorted in non-increasing order by severity score, where a larger score means a more critical issue. You are given a list of integer arrays streams, where streams[i] contains the severity scores for stream i, already sorted from highest to lowest. Your task is to merge all streams into one global processing order.

At every step, you may only take the next unprocessed report from any stream. Return the merged list of all severity scores in non-increasing order. If two available reports have the same severity, choose the one from the smaller stream index first. If there is still a tie, choose the one that appears earlier within its stream.

Design an algorithm that is efficient when the number of streams is large and each stream may have different length. A solution that repeatedly scans every stream for the next best item will be too slow.

Constraints:
- 1 <= streams.length <= 10^5
- 0 <= streams[i].length <= 10^5
- 0 <= severity <= 10^9
- Each streams[i] is sorted in non-increasing order
- The total number of reports across all streams does not exceed 2 * 10^5

Example 1:
Input: streams = [[9,7,3],[10,6],[8,8,1]]
Output: [10,9,8,8,7,6,3,1]
Explanation: Start with the first available value from each stream: 9, 10, and 8. Pick 10, then compare 9, 6, and 8. Continue until all reports are merged.

Example 2:
Input: streams = [[5,5,2],[],[5,4],[6]]
Output: [6,5,5,5,4,2]
Explanation: After taking 6 from stream 3, the next candidates are 5 from stream 0 and 5 from stream 2. Since the severities are equal, choose the smaller stream index first, so stream 0's 5 comes before stream 2's 5. The second 5 from stream 0 is then considered normally.

Your function should return the merged array of severity scores.
*/
public class Solution {

    /**
     * Small helper object representing the "current available" report from one stream.
     *
     * We store:
     * - severity: the value currently available to be chosen
     * - streamIndex: which stream this value came from
     * - elementIndex: the position inside that stream
     *
     * Why store elementIndex too?
     * - After we remove this node from the heap, we need to know where the next value
     *   in the same stream is located.
     * - It also makes the tie-breaking rule explicit and easy to reason about.
     */
    private static class Node {
        int severity;
        int streamIndex;
        int elementIndex;

        Node(int severity, int streamIndex, int elementIndex) {
            this.severity = severity;
            this.streamIndex = streamIndex;
            this.elementIndex = elementIndex;
        }
    }

    /**
     * Merges all release streams into one global order using a priority queue (heap).
     *
     * Core idea:
     * - Each stream is already sorted from largest to smallest.
     * - So at any moment, the only candidate we may take from a stream is its next
     *   unprocessed element.
     * - That means we only need to compare one current element per stream.
     * - A max-heap lets us efficiently retrieve the best currently available report.
     *
     * Tie-breaking:
     * 1. Higher severity first
     * 2. If severity is equal, smaller stream index first
     * 3. If still equal, earlier position within the same stream first
     *
     * Note:
     * The third tie-breaker is naturally respected because for any single stream,
     * only one "current" element can be in the heap at a time. Still, we include
     * elementIndex in the comparator to make the ordering rule fully explicit.
     *
     * @param streams an array of streams, where each stream is sorted in non-increasing order
     * @return a merged array containing all severity scores in the required order
     * Time complexity: O(T log K), where T is the total number of reports and K is the number of streams
     * Space complexity: O(K) for the heap, excluding the output array
     */
    public int[] mergeReleaseStreams(int[][] streams) {
        // Defensive handling:
        // If the outer array itself is null, we return an empty result.
        if (streams == null) {
            return new int[0];
        }

        // First, compute the total number of reports across all streams.
        // We need this so we can allocate the exact output array size once.
        int totalReports = 0;
        for (int i = 0; i < streams.length; i++) {
            if (streams[i] != null) {
                totalReports += streams[i].length;
            }
        }

        // This priority queue acts like a max-heap according to our custom ordering.
        //
        // Comparator explanation:
        // - Larger severity should come first, so compare b.severity vs a.severity.
        // - If severities tie, smaller stream index should come first.
        // - If still tied, smaller element index should come first.
        //
        // In Java's PriorityQueue, the "smallest" according to the comparator is removed first.
        // So we define the comparator such that the "best" candidate becomes the smallest
        // in comparator terms.
        PriorityQueue<Node> pq = new PriorityQueue<>((a, b) -> {
            if (a.severity != b.severity) {
                return Integer.compare(b.severity, a.severity);
            }
            if (a.streamIndex != b.streamIndex) {
                return Integer.compare(a.streamIndex, b.streamIndex);
            }
            return Integer.compare(a.elementIndex, b.elementIndex);
        });

        // Initialize the heap with the first element from every non-empty stream.
        //
        // Why only the first element?
        // Because from each stream, only the next unprocessed element is currently available.
        // Since the streams are already sorted, later elements in the same stream cannot be
        // chosen before earlier ones.
        for (int streamIndex = 0; streamIndex < streams.length; streamIndex++) {
            if (streams[streamIndex] != null && streams[streamIndex].length > 0) {
                pq.offer(new Node(streams[streamIndex][0], streamIndex, 0));
            }
        }

        // Prepare the result array.
        int[] merged = new int[totalReports];
        int writeIndex = 0;

        // Repeatedly extract the best currently available report.
        while (!pq.isEmpty()) {
            // Step 1:
            // Remove the highest-priority current report among all streams.
            Node current = pq.poll();

            // Step 2:
            // Write its severity into the output.
            merged[writeIndex++] = current.severity;

            // Step 3:
            // Advance only within the same stream from which we just took an element.
            // If that stream still has more elements, its next element now becomes available,
            // so we push it into the heap.
            int nextElementIndex = current.elementIndex + 1;
            int streamIndex = current.streamIndex;

            if (streams[streamIndex] != null && nextElementIndex < streams[streamIndex].length) {
                pq.offer(new Node(streams[streamIndex][nextElementIndex], streamIndex, nextElementIndex));
            }
        }

        return merged;
    }

    /**
     * Convenience method that returns the merged result as a List<Integer>.
     * This can be easier for demonstration or interview discussion.
     *
     * @param streams an array of streams, where each stream is sorted in non-increasing order
     * @return a list containing all merged severity scores in the required order
     * Time complexity: O(T log K), where T is the total number of reports and K is the number of streams
     * Space complexity: O(T + K), because this method creates a list copy of the output
     */
    public List<Integer> mergeReleaseStreamsAsList(int[][] streams) {
        int[] merged = mergeReleaseStreams(streams);
        List<Integer> result = new ArrayList<>(merged.length);
        for (int value : merged) {
            result.add(value);
        }
        return result;
    }

    /**
     * Converts an int array into a readable string like [1, 2, 3].
     *
     * @param array the input array
     * @return a human-readable string representation of the array
     * Time complexity: O(n), where n is the array length
     * Space complexity: O(n) for the produced string content
     */
    public static String arrayToString(int[] array) {
        return Arrays.toString(array);
    }

    /**
     * Program entry point.
     *
     * Demonstrates the algorithm on the sample inputs from the problem statement
     * and prints the results.
     *
     * Verified outputs:
     * Example 1 -> [10, 9, 8, 8, 7, 6, 3, 1]
     * Example 2 -> [6, 5, 5, 5, 4, 2]
     *
     * @param args command-line arguments (not used)
     * @return nothing
     * Time complexity: O(T log K) across the demonstrated examples
     * Space complexity: O(K) auxiliary heap space per merge, excluding outputs
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Example 1
        int[][] streams1 = {
            {9, 7, 3},
            {10, 6},
            {8, 8, 1}
        };
        int[] result1 = solution.mergeReleaseStreams(streams1);
        System.out.println("Example 1 Input:  " + Arrays.deepToString(streams1));
        System.out.println("Example 1 Output: " + arrayToString(result1));
        System.out.println("Expected:         [10, 9, 8, 8, 7, 6, 3, 1]");
        System.out.println();

        // Example 2
        int[][] streams2 = {
            {5, 5, 2},
            {},
            {5, 4},
            {6}
        };
        int[] result2 = solution.mergeReleaseStreams(streams2);
        System.out.println("Example 2 Input:  " + Arrays.deepToString(streams2));
        System.out.println("Example 2 Output: " + arrayToString(result2));
        System.out.println("Expected:         [6, 5, 5, 5, 4, 2]");
        System.out.println();

        // Additional small demonstration
        int[][] streams3 = {
            {},
            {7, 7, 5},
            {7, 6},
            {7}
        };
        int[] result3 = solution.mergeReleaseStreams(streams3);
        System.out.println("Additional Input:  " + Arrays.deepToString(streams3));
        System.out.println("Additional Output: " + arrayToString(result3));
    }
}