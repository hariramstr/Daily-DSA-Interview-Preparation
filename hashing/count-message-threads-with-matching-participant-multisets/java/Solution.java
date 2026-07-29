import java.util.*;

/*
Problem Title: Count Message Threads With Matching Participant Multisets

Problem Description:
You are given chat logs from a messaging platform. Each thread is represented by a list of user IDs in the order messages were sent. A user may appear multiple times in the same thread if they sent multiple messages. Two threads are considered equivalent if they contain exactly the same multiset of participants, meaning every user ID appears the same number of times in both threads, regardless of message order.

Your task is to count how many unordered pairs of threads are equivalent.

Formally, let threads[i] be the list of user IDs in the i-th thread. Threads i and j are equivalent if for every user ID x, the number of occurrences of x in threads[i] equals the number of occurrences of x in threads[j]. Return the number of pairs (i, j) such that 0 <= i < j < n and threads[i] and threads[j] are equivalent.

Because user IDs can be very large and each thread may contain repeated IDs, a naive comparison of every pair will be too slow. You need to design a hashing-based representation that uniquely identifies the participant multiset of each thread.

Constraints:
- 1 <= n <= 100000
- 1 <= total number of user IDs across all threads <= 300000
- 1 <= threads[i].length <= 100000
- Sum of all threads[i].length over all threads is at most 300000
- 1 <= user ID <= 10^9
- Return the answer as a 64-bit integer

Example 1:
Input: threads = [[4,1,4,2],[2,4,4,1],[3,3],[1,2,4,4],[3,3,3]]
Output: 3
Explanation: Threads 0, 1, and 3 all have the same participant multiset: {1:1, 2:1, 4:2}. They contribute 3 unordered pairs. Thread 2 has multiset {3:2}, and thread 4 has multiset {3:3}, so they do not match.

Example 2:
Input: threads = [[8,9],[9,8,8],[7],[8,9],[7],[9,8]]
Output: 4
Explanation: Threads 0, 3, and 5 are equivalent because each contains one 8 and one 9, contributing 3 pairs. Threads 2 and 4 are equivalent, contributing 1 more pair. Thread 1 is different because it contains two 8s and one 9.
*/

public class Solution {

    /**
     * Counts how many unordered pairs of threads have exactly the same participant multiset.
     *
     * The key idea is:
     * 1. For each thread, count how many times each user ID appears.
     * 2. Convert that frequency map into a canonical representation that is identical
     *    for equivalent multisets and different for non-equivalent multisets.
     * 3. Count how many times each canonical representation appears.
     * 4. If a representation appears k times, it contributes k * (k - 1) / 2 pairs.
     *
     * We use a String-based canonical key:
     * - First count frequencies with a HashMap<Integer, Integer>.
     * - Then sort the distinct user IDs.
     * - Then append "userId#count;" for each distinct user ID.
     *
     * This representation is exact, not probabilistic, so there is no collision risk
     * from hashing tricks beyond normal String/HashMap equality semantics.
     *
     * @param threads a 2D array where threads[i] contains the user IDs appearing in the i-th thread
     * @return the number of unordered equivalent thread pairs as a 64-bit integer
     * Time complexity: O(T + sum over threads of d_i log d_i), where T is the total number of user IDs
     * and d_i is the number of distinct user IDs in thread i
     * Space complexity: O(T) in the worst case for temporary maps/keys across processing
     */
    public long countEquivalentThreads(int[][] threads) {
        // This map stores:
        // canonical multiset representation -> how many threads have this exact multiset
        Map<String, Integer> signatureCount = new HashMap<>();

        // Process each thread independently.
        for (int[] thread : threads) {
            // Build a canonical signature for the current thread.
            String signature = buildSignature(thread);

            // Increase the number of times we have seen this signature.
            signatureCount.put(signature, signatureCount.getOrDefault(signature, 0) + 1);
        }

        // Now compute the number of unordered pairs.
        // If a signature appears c times, the number of pairs is c choose 2 = c * (c - 1) / 2.
        long answer = 0L;
        for (int count : signatureCount.values()) {
            answer += (long) count * (count - 1) / 2;
        }

        return answer;
    }

    /**
     * Builds a canonical representation of the participant multiset of one thread.
     *
     * Detailed process:
     * 1. Count frequencies of each user ID.
     * 2. Extract distinct user IDs.
     * 3. Sort the distinct user IDs so order of messages does not matter.
     * 4. Serialize the sorted (userId, count) pairs into a unique String.
     *
     * Example:
     * thread = [4, 1, 4, 2]
     * frequency map = {4=2, 1=1, 2=1}
     * sorted keys = [1, 2, 4]
     * signature = "1#1;2#1;4#2;"
     *
     * Any thread with the same multiset will produce exactly the same signature.
     *
     * @param thread the list of user IDs in one thread
     * @return a canonical String signature representing the multiset of user IDs
     * Time complexity: O(m + d log d), where m is thread length and d is number of distinct user IDs
     * Space complexity: O(d)
     */
    public String buildSignature(int[] thread) {
        // Step 1: Count how many times each user ID appears in this thread.
        Map<Integer, Integer> frequency = new HashMap<>();
        for (int userId : thread) {
            frequency.put(userId, frequency.getOrDefault(userId, 0) + 1);
        }

        // Step 2: Extract all distinct user IDs into a list so we can sort them.
        List<Integer> users = new ArrayList<>(frequency.keySet());

        // Step 3: Sort distinct user IDs.
        // This is the crucial step that removes dependence on original message order.
        Collections.sort(users);

        // Step 4: Serialize the sorted (userId, count) pairs.
        // We use separators to avoid ambiguity.
        StringBuilder sb = new StringBuilder();
        for (int userId : users) {
            sb.append(userId).append('#').append(frequency.get(userId)).append(';');
        }

        return sb.toString();
    }

    /**
     * Convenience overload that accepts a List of Lists instead of a primitive 2D array.
     *
     * This is useful for demonstrations or interview-style inputs where data may naturally
     * be represented as nested lists.
     *
     * @param threads a list where each inner list contains the user IDs of one thread
     * @return the number of unordered equivalent thread pairs as a 64-bit integer
     * Time complexity: O(T + sum over threads of d_i log d_i), after conversion
     * Space complexity: O(T) for the converted array plus processing structures
     */
    public long countEquivalentThreads(List<List<Integer>> threads) {
        int[][] array = to2DArray(threads);
        return countEquivalentThreads(array);
    }

    /**
     * Converts a List<List<Integer>> into an int[][].
     *
     * @param threads nested list representation of threads
     * @return equivalent primitive 2D array
     * Time complexity: O(T), where T is the total number of user IDs
     * Space complexity: O(T)
     */
    public int[][] to2DArray(List<List<Integer>> threads) {
        int[][] result = new int[threads.size()][];
        for (int i = 0; i < threads.size(); i++) {
            List<Integer> current = threads.get(i);
            result[i] = new int[current.size()];
            for (int j = 0; j < current.size(); j++) {
                result[i][j] = current.get(j);
            }
        }
        return result;
    }

    /**
     * Demonstrates the solution on the sample inputs from the problem statement.
     *
     * Expected outputs:
     * Example 1 -> 3
     * Example 2 -> 4
     *
     * @param args command-line arguments (not used)
     * @return nothing
     * Time complexity: O(T + sum over threads of d_i log d_i) for the demonstrated inputs
     * Space complexity: O(T)
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Example 1:
        // Threads 0, 1, and 3 all represent the same multiset {1:1, 2:1, 4:2}
        // Number of unordered pairs among these three threads = 3 choose 2 = 3
        // Thread 2 is {3:2}
        // Thread 4 is {3:3}
        // So total answer = 3
        int[][] threads1 = {
            {4, 1, 4, 2},
            {2, 4, 4, 1},
            {3, 3},
            {1, 2, 4, 4},
            {3, 3, 3}
        };
        long result1 = solution.countEquivalentThreads(threads1);
        System.out.println(result1); // Expected: 3

        // Example 2:
        // Threads 0, 3, and 5 are all {8:1, 9:1} -> 3 pairs
        // Threads 2 and 4 are both {7:1} -> 1 pair
        // Thread 1 is {8:2, 9:1} -> no match with others
        // Total = 4
        int[][] threads2 = {
            {8, 9},
            {9, 8, 8},
            {7},
            {8, 9},
            {7},
            {9, 8}
        };
        long result2 = solution.countEquivalentThreads(threads2);
        System.out.println(result2); // Expected: 4
    }
}