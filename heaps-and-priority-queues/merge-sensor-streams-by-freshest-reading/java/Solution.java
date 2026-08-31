import java.util.*;

/*
Title: Merge Sensor Streams by Freshest Reading
Difficulty: Medium
Topic: Heaps and Priority Queues

Problem Description:
You are given k sensor streams. Each stream is represented as a list of readings sorted by increasing timestamp.
A reading is a pair [timestamp, value]. Different streams may contain readings at the same timestamp, and some
streams may be empty.

Your task is to produce a single merged timeline of readings using the following rule: repeatedly choose the unread
reading with the smallest timestamp among all streams. If multiple unread readings share the same timestamp, choose
the one with the larger value first. If there is still a tie, choose the reading from the smaller stream index first.

Return the merged result as a list of triples [timestamp, value, streamIndex] in the exact order they are selected.

This problem models merging multiple already-sorted event feeds while preserving a deterministic priority rule.
An efficient solution should avoid repeatedly scanning all streams to find the next reading.

Constraints:
- 1 <= k <= 10^4
- 0 <= total number of readings across all streams <= 2 * 10^5
- 0 <= timestamp <= 10^9
- -10^9 <= value <= 10^9
- Each individual stream is sorted by nondecreasing timestamp
- The output size equals the total number of readings

Example 1:
Input: streams = [
  [[1, 5], [4, 2]],
  [[1, 7], [3, 1]],
  [[2, 9]]
]
Output: [[1, 7, 1], [1, 5, 0], [2, 9, 2], [3, 1, 1], [4, 2, 0]]

Example 2:
Input: streams = [
  [],
  [[2, 4], [2, 3], [5, 8]],
  [[2, 4]],
  [[1, 10], [6, 0]]
]
Output: [[1, 10, 3], [2, 4, 1], [2, 4, 2], [2, 3, 1], [5, 8, 1], [6, 0, 3]]
*/

public class Solution {

    /**
     * Small helper object representing the current unread reading from one stream.
     * We store:
     * - streamIndex: which stream this reading belongs to
     * - readingIndex: position inside that stream
     * - timestamp: reading timestamp
     * - value: reading value
     *
     * This object is what we place into the priority queue.
     */
    private static class Node {
        int streamIndex;
        int readingIndex;
        int timestamp;
        int value;

        Node(int streamIndex, int readingIndex, int timestamp, int value) {
            this.streamIndex = streamIndex;
            this.readingIndex = readingIndex;
            this.timestamp = timestamp;
            this.value = value;
        }
    }

    /**
     * Merges k sorted sensor streams into one deterministic timeline using the required priority rules:
     * 1) smaller timestamp first
     * 2) if timestamps tie, larger value first
     * 3) if still tied, smaller stream index first
     *
     * The method uses a priority queue (min-heap behavior via custom comparator) so that we can always
     * extract the next correct reading efficiently without scanning all streams every time.
     *
     * @param streams a list of streams, where each stream is a list of readings [timestamp, value],
     *                and each stream is sorted by nondecreasing timestamp
     * @return a merged list of triples [timestamp, value, streamIndex] in the exact order selected
     * Time complexity: O(N log k), where N is the total number of readings across all streams
     * and k is the number of streams
     * Space complexity: O(k) auxiliary space for the priority queue, excluding the output list
     */
    public List<int[]> mergeSensorStreams(List<List<int[]>> streams) {
        List<int[]> result = new ArrayList<>();

        // Edge case:
        // If the outer list itself is null, we simply return an empty result.
        // The problem constraints imply valid input, but this makes the method safer and beginner-friendly.
        if (streams == null) {
            return result;
        }

        // Priority queue ordering is the heart of the solution.
        //
        // We want the "best next reading" to come out first according to:
        //   1. smallest timestamp
        //   2. if same timestamp, larger value
        //   3. if same timestamp and same value, smaller stream index
        //
        // In Java's PriorityQueue, the element considered "smallest" by the comparator is removed first.
        // So we encode the rules exactly in comparator order:
        //   - timestamp ascending
        //   - value descending
        //   - streamIndex ascending
        PriorityQueue<Node> pq = new PriorityQueue<>((a, b) -> {
            if (a.timestamp != b.timestamp) {
                return Integer.compare(a.timestamp, b.timestamp);
            }
            if (a.value != b.value) {
                return Integer.compare(b.value, a.value);
            }
            return Integer.compare(a.streamIndex, b.streamIndex);
        });

        // Step 1:
        // Put the first unread reading from every non-empty stream into the heap.
        //
        // Why only the first one from each stream?
        // Because each stream is already sorted by timestamp, so the earliest unread candidate from a stream
        // is always enough to represent that stream in the global competition.
        for (int streamIndex = 0; streamIndex < streams.size(); streamIndex++) {
            List<int[]> stream = streams.get(streamIndex);

            // Skip null or empty streams.
            if (stream == null || stream.isEmpty()) {
                continue;
            }

            int[] firstReading = stream.get(0);
            pq.offer(new Node(streamIndex, 0, firstReading[0], firstReading[1]));
        }

        // Step 2:
        // Repeatedly extract the globally best unread reading.
        //
        // Each poll gives us the next reading that must appear in the merged output.
        // After removing one reading from a stream, we then insert the next unread reading from that same stream.
        while (!pq.isEmpty()) {
            Node current = pq.poll();

            // Add the chosen reading to the result in the required format:
            // [timestamp, value, streamIndex]
            result.add(new int[]{current.timestamp, current.value, current.streamIndex});

            // Move forward in the same stream from which we just consumed a reading.
            int nextReadingIndex = current.readingIndex + 1;
            List<int[]> stream = streams.get(current.streamIndex);

            // If that stream still has more readings, push the next one into the heap.
            if (nextReadingIndex < stream.size()) {
                int[] nextReading = stream.get(nextReadingIndex);
                pq.offer(new Node(
                        current.streamIndex,
                        nextReadingIndex,
                        nextReading[0],
                        nextReading[1]
                ));
            }
        }

        return result;
    }

    /**
     * Convenience overload that accepts a 4D int array representation:
     * streams[streamIndex][readingIndex] = [timestamp, value]
     *
     * This is useful for easy testing in main and for interview-style examples.
     *
     * @param streams a 4D array where each stream contains readings [timestamp, value]
     * @return a merged list of triples [timestamp, value, streamIndex]
     * Time complexity: O(N log k), where N is the total number of readings and k is the number of streams
     * Space complexity: O(N + k) including conversion storage and heap usage, excluding output formatting overhead
     */
    public List<int[]> mergeSensorStreams(int[][][] streams) {
        List<List<int[]>> listStreams = new ArrayList<>();

        if (streams == null) {
            return new ArrayList<>();
        }

        for (int[][] stream : streams) {
            List<int[]> currentStream = new ArrayList<>();
            if (stream != null) {
                for (int[] reading : stream) {
                    if (reading != null && reading.length >= 2) {
                        currentStream.add(new int[]{reading[0], reading[1]});
                    }
                }
            }
            listStreams.add(currentStream);
        }

        return mergeSensorStreams(listStreams);
    }

    /**
     * Converts a list of int arrays into a readable string such as:
     * [[1, 7, 1], [1, 5, 0], [2, 9, 2]]
     *
     * @param list the list of integer arrays to print
     * @return a human-readable string representation
     * Time complexity: O(M * L), where M is the number of arrays and L is the average array length
     * Space complexity: O(M * L) for the produced string
     */
    public String formatListOfArrays(List<int[]> list) {
        StringBuilder sb = new StringBuilder();
        sb.append("[");

        for (int i = 0; i < list.size(); i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(Arrays.toString(list.get(i)));
        }

        sb.append("]");
        return sb.toString();
    }

    /**
     * Demonstrates the solution on the sample inputs from the problem statement
     * and prints the outputs.
     *
     * @param args command-line arguments (not used)
     * @return nothing
     * Time complexity: O(N log k) across the demonstrated examples
     * Space complexity: O(N + k) due to example storage and output
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Example 1
        int[][][] streams1 = {
                { {1, 5}, {4, 2} },
                { {1, 7}, {3, 1} },
                { {2, 9} }
        };

        List<int[]> result1 = solution.mergeSensorStreams(streams1);
        System.out.println("Example 1 Output:");
        System.out.println(solution.formatListOfArrays(result1));
        System.out.println("Expected:");
        System.out.println("[[1, 7, 1], [1, 5, 0], [2, 9, 2], [3, 1, 1], [4, 2, 0]]");
        System.out.println();

        // Example 2
        int[][][] streams2 = {
                { },
                { {2, 4}, {2, 3}, {5, 8} },
                { {2, 4} },
                { {1, 10}, {6, 0} }
        };

        List<int[]> result2 = solution.mergeSensorStreams(streams2);
        System.out.println("Example 2 Output:");
        System.out.println(solution.formatListOfArrays(result2));
        System.out.println("Expected:");
        System.out.println("[[1, 10, 3], [2, 4, 1], [2, 4, 2], [2, 3, 1], [5, 8, 1], [6, 0, 3]]");
    }
}