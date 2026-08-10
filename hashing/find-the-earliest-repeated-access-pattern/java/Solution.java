import java.util.*;

/*
Problem Title: Find the Earliest Repeated Access Pattern

Problem Description:
A security system records each employee's building access events as an array of lowercase strings,
where each string is a room code visited in order during one day. You are also given an integer
windowSize. For every contiguous block of exactly windowSize room codes, define its access pattern
as the ordered sequence of those room codes. Your task is to return the starting index of the
earliest window whose exact access pattern appears again later in the array. If multiple windows
repeat, choose the one with the smallest starting index. If no length-windowSize pattern appears
at least twice, return -1.

Two windows are considered the same only if they have the same length and every position contains
the same room code. Overlapping windows are allowed. For example, with windowSize = 3, the windows
starting at indices 1 and 3 may match even if they overlap.

Design an efficient solution using hashing so that large inputs can be processed quickly. A naive
comparison of every pair of windows will be too slow.

Constraints:
- 1 <= accessLog.length <= 100000
- 1 <= windowSize <= accessLog.length
- accessLog[i] consists of lowercase English letters
- The total number of characters across all room codes is at most 200000

Example 1:
Input: accessLog = ["lab","hall","vault","lab","hall","vault","exit"], windowSize = 3
Output: 0
Explanation: The window ["lab","hall","vault"] appears starting at index 0 and again at index 3.
Since index 0 is the earliest repeated pattern, return 0.

Example 2:
Input: accessLog = ["a","b","a","b","c"], windowSize = 2
Output: 0
Explanation: The window ["a","b"] appears at indices 0 and 2. Other windows do not repeat. The
earliest repeated starting index is 0.
*/

public class Solution {

    private static final long MOD1 = 1_000_000_007L;
    private static final long MOD2 = 1_000_000_009L;
    private static final long BASE1 = 911_382_323L;
    private static final long BASE2 = 972_663_749L;

    /**
     * Finds the starting index of the earliest length-windowSize access pattern that appears
     * at least twice in the access log.
     *
     * The algorithm works in three main stages:
     * 1. Convert each room code string into a stable numeric token hash.
     * 2. Build rolling hashes over the array of token hashes so every window of length windowSize
     *    can be represented in O(1) time after preprocessing.
     * 3. Scan windows from left to right, remember the first index where each window hash appeared,
     *    and when the same window hash appears again, verify the actual strings to guarantee
     *    correctness even in the extremely unlikely event of a hash collision.
     *
     * Because we scan from left to right and store only the first occurrence of each pattern,
     * the smallest repeated starting index is naturally tracked.
     *
     * @param accessLog the ordered array of room codes visited during the day
     * @param windowSize the exact size of each contiguous window to compare
     * @return the smallest starting index whose length-windowSize pattern appears again later;
     *         returns -1 if no such repeated pattern exists
     *
     * Time complexity: O(n + totalCharacters) on average, where n is accessLog.length.
     * In the rare case of hash collisions, extra verification comparisons may occur.
     * Space complexity: O(n + totalCharacters) for token hashes, prefix arrays, and maps.
     */
    public int earliestRepeatedAccessPattern(String[] accessLog, int windowSize) {
        int n = accessLog.length;

        // If the window size is larger than the array length, no valid window exists.
        // The problem constraints guarantee windowSize <= n, but this check keeps the method robust.
        if (windowSize > n || windowSize <= 0) {
            return -1;
        }

        // Number of windows of size windowSize in an array of length n.
        int totalWindows = n - windowSize + 1;

        // If there is only one window, it cannot repeat later.
        if (totalWindows <= 1) {
            return -1;
        }

        // Step 1:
        // Convert each room code string into two numeric hashes.
        // We use double hashing for the strings themselves so that equal strings almost certainly
        // produce equal token pairs, while different strings almost certainly produce different pairs.
        // Then we combine the two values into one long token for rolling over the array.
        long[] tokenValues = buildTokenValues(accessLog);

        // Step 2:
        // Build prefix hashes and powers for the token array.
        // This allows us to compute the hash of any contiguous subarray (window) in O(1).
        long[] prefix1 = new long[n + 1];
        long[] prefix2 = new long[n + 1];
        long[] power1 = new long[n + 1];
        long[] power2 = new long[n + 1];

        power1[0] = 1L;
        power2[0] = 1L;

        for (int i = 0; i < n; i++) {
            power1[i + 1] = (power1[i] * BASE1) % MOD1;
            power2[i + 1] = (power2[i] * BASE2) % MOD2;

            // We shift token values by +1 before inserting into the rolling hash sequence.
            // This avoids issues if a token value were 0.
            long value1 = tokenValues[i] % MOD1;
            long value2 = tokenValues[i] % MOD2;

            prefix1[i + 1] = (prefix1[i] * BASE1 + value1 + 1) % MOD1;
            prefix2[i + 1] = (prefix2[i] * BASE2 + value2 + 1) % MOD2;
        }

        // Step 3:
        // For each window, compute its double rolling hash.
        // We store the first index where each hash pair appears.
        //
        // Important detail:
        // - If a hash appears again, we verify the actual window contents using string comparison.
        //   This guarantees correctness even if a collision happens.
        // - We keep the earliest repeated starting index found so far.
        Map<Long, List<Integer>> seen = new HashMap<>();
        int answer = Integer.MAX_VALUE;

        for (int start = 0; start < totalWindows; start++) {
            int endExclusive = start + windowSize;

            long windowHash1 = subHash(prefix1, power1, start, endExclusive, MOD1);
            long windowHash2 = subHash(prefix2, power2, start, endExclusive, MOD2);

            long combinedWindowHash = combine(windowHash1, windowHash2);

            List<Integer> previousStarts = seen.get(combinedWindowHash);

            if (previousStarts != null) {
                // This hash has been seen before. Because collisions are theoretically possible,
                // we must compare actual strings for every earlier window with the same hash.
                //
                // We stored earlier starts in increasing order because we scan left to right.
                // Therefore, the first verified match we find is the earliest matching start for
                // this current window.
                for (int earlierStart : previousStarts) {
                    if (windowsEqual(accessLog, earlierStart, start, windowSize)) {
                        answer = Math.min(answer, earlierStart);

                        // Since the list is in increasing order, once we find a real match,
                        // this is the earliest matching start for the current window.
                        // No need to check later entries in this list.
                        break;
                    }
                }
            }

            // Record the current window start for future windows.
            // We always append, preserving increasing order of start indices.
            if (previousStarts == null) {
                previousStarts = new ArrayList<>();
                seen.put(combinedWindowHash, previousStarts);
            }
            previousStarts.add(start);
        }

        return answer == Integer.MAX_VALUE ? -1 : answer;
    }

    /**
     * Builds a numeric token value for each room code string.
     *
     * Each string is hashed independently using two modular polynomial hashes over characters.
     * The two hash values are then combined into one long token. These token values are later
     * used as the elements of the array-level rolling hash.
     *
     * This design is efficient because:
     * - The total number of characters across all strings is bounded.
     * - Comparing windows of strings directly every time would be too slow.
     * - Converting strings to compact numeric tokens makes rolling hash practical.
     *
     * @param accessLog the array of room code strings
     * @return an array of numeric token values, one per room code
     *
     * Time complexity: O(totalCharacters), where totalCharacters is the sum of all string lengths.
     * Space complexity: O(n), where n is accessLog.length.
     */
    public long[] buildTokenValues(String[] accessLog) {
        int n = accessLog.length;
        long[] tokens = new long[n];

        for (int i = 0; i < n; i++) {
            long h1 = 0L;
            long h2 = 0L;
            String s = accessLog[i];

            // Hash the string character by character.
            // Since the strings contain lowercase English letters, we map:
            // 'a' -> 1, 'b' -> 2, ..., 'z' -> 26
            for (int j = 0; j < s.length(); j++) {
                int value = s.charAt(j) - 'a' + 1;
                h1 = (h1 * 31 + value) % MOD1;
                h2 = (h2 * 37 + value) % MOD2;
            }

            tokens[i] = combine(h1, h2);
        }

        return tokens;
    }

    /**
     * Computes the rolling hash of the subarray [left, right) using prefix hashes and powers.
     *
     * Formula:
     * hash(left, right) = prefix[right] - prefix[left] * base^(right-left)
     *
     * We add the modulus before taking % modulus to keep the result non-negative.
     *
     * @param prefix prefix hash array
     * @param power precomputed powers of the base
     * @param left inclusive start index
     * @param right exclusive end index
     * @param mod modulus used for this hash
     * @return the hash value of the subarray [left, right)
     *
     * Time complexity: O(1)
     * Space complexity: O(1)
     */
    public long subHash(long[] prefix, long[] power, int left, int right, long mod) {
        long result = (prefix[right] - (prefix[left] * power[right - left]) % mod + mod) % mod;
        return result;
    }

    /**
     * Combines two 32-bit-safe hash values into one long key for use in hash maps.
     *
     * Since both values are less than about 1e9+9, shifting the first by 32 bits and XOR-ing
     * with the second gives a compact combined key.
     *
     * @param a the first hash value
     * @param b the second hash value
     * @return a combined long key
     *
     * Time complexity: O(1)
     * Space complexity: O(1)
     */
    public long combine(long a, long b) {
        return (a << 32) ^ b;
    }

    /**
     * Verifies whether two windows of the same length contain exactly the same strings
     * in the same order.
     *
     * This method is used only as a safety check after hash values match. It guarantees
     * correctness even if a hash collision occurs.
     *
     * @param accessLog the original array of room codes
     * @param start1 the starting index of the first window
     * @param start2 the starting index of the second window
     * @param windowSize the length of both windows
     * @return true if the two windows are exactly equal; false otherwise
     *
     * Time complexity: O(windowSize) in the worst case
     * Space complexity: O(1)
     */
    public boolean windowsEqual(String[] accessLog, int start1, int start2, int windowSize) {
        for (int offset = 0; offset < windowSize; offset++) {
            if (!accessLog[start1 + offset].equals(accessLog[start2 + offset])) {
                return false;
            }
        }
        return true;
    }

    /**
     * Demonstrates the solution using the sample inputs from the problem statement.
     *
     * @param args command-line arguments (not used)
     *
     * @return nothing
     *
     * Time complexity: O(1) for the fixed demo inputs, excluding the called method costs.
     * Space complexity: O(1), excluding the called method costs.
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        String[] accessLog1 = {"lab", "hall", "vault", "lab", "hall", "vault", "exit"};
        int windowSize1 = 3;
        int result1 = solution.earliestRepeatedAccessPattern(accessLog1, windowSize1);
        System.out.println(result1); // Expected: 0

        String[] accessLog2 = {"a", "b", "a", "b", "c"};
        int windowSize2 = 2;
        int result2 = solution.earliestRepeatedAccessPattern(accessLog2, windowSize2);
        System.out.println(result2); // Expected: 0

        // Additional quick checks for clarity.
        String[] accessLog3 = {"x", "y", "z"};
        int windowSize3 = 2;
        int result3 = solution.earliestRepeatedAccessPattern(accessLog3, windowSize3);
        System.out.println(result3); // Expected: -1

        String[] accessLog4 = {"a", "a", "a", "a"};
        int windowSize4 = 2;
        int result4 = solution.earliestRepeatedAccessPattern(accessLog4, windowSize4);
        System.out.println(result4); // Expected: 0
    }
}