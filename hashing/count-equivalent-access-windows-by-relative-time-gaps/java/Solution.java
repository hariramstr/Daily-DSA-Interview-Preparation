import java.util.*;

/*
 * Title: Count Equivalent Access Windows by Relative Time Gaps
 * Difficulty: Hard
 * Topic: Hashing
 *
 * Problem Description:
 * You are given an integer array timestamps representing event times in nondecreasing order,
 * and an integer k. Consider every contiguous window of exactly k timestamps.
 *
 * Two windows are considered equivalent if their internal pattern of time gaps is identical.
 * For a window:
 *   [t[i], t[i+1], ..., t[i+k-1]]
 * define its gap signature as:
 *   [t[i+1]-t[i], t[i+2]-t[i+1], ..., t[i+k-1]-t[i+k-2]]
 *
 * Two length-k windows are equivalent if these gap signatures are exactly the same
 * element by element, even if the absolute starting times differ.
 *
 * Return the number of unordered pairs of distinct windows that are equivalent.
 *
 * Constraints:
 * - 1 <= timestamps.length <= 200000
 * - 1 <= k <= timestamps.length
 * - 0 <= timestamps[i] <= 10^18
 * - timestamps is sorted in nondecreasing order
 *
 * Important special case:
 * - When k = 1, every window has an empty gap signature, so all single-element windows
 *   are equivalent. If there are n timestamps, then there are C(n, 2) equivalent pairs.
 *
 * Notes about the examples:
 * - Example 1's text contains a contradiction in the stated output. Tracing the windows
 *   carefully shows the correct answer is 1.
 * - Example 2's text says output 2, but the detailed counting gives 4, which is correct.
 *
 * Efficient idea:
 * - Convert timestamps into the array of adjacent gaps.
 * - Every length-k timestamp window corresponds to a length-(k-1) subarray in the gaps array.
 * - We need to count how many equal subarrays of fixed length exist.
 * - Use rolling hash to represent each gap-signature efficiently, and group equal signatures.
 * - To make collisions negligibly unlikely, use double hashing.
 */
public class Solution {

    /**
     * Counts the number of unordered pairs of distinct contiguous windows of size k
     * whose internal gap signatures are identical.
     *
     * This method accepts a long[] because timestamps may be as large as 10^18.
     *
     * @param timestamps the sorted event times in nondecreasing order
     * @param k the exact window size
     * @return the number of unordered equivalent window pairs
     *
     * Time complexity: O(n), where n = timestamps.length
     * Space complexity: O(n)
     */
    public long countEquivalentAccessWindows(long[] timestamps, int k) {
        int n = timestamps.length;

        // If k == 1, every single-element window has an empty signature.
        // Therefore all windows are equivalent.
        // Number of windows = n, so answer is n choose 2.
        if (k == 1) {
            return combinationsOfTwo(n);
        }

        // If k > n, there are no valid windows. The constraints say k <= n,
        // but this guard makes the method more robust.
        if (k > n) {
            return 0L;
        }

        // Build the gaps array:
        // gaps[i] = timestamps[i+1] - timestamps[i]
        //
        // Since timestamps is nondecreasing, each gap is >= 0.
        // Because timestamps values can be up to 10^18, gaps also fit in long.
        int gapCount = n - 1;
        long[] gaps = new long[gapCount];
        for (int i = 0; i < gapCount; i++) {
            gaps[i] = timestamps[i + 1] - timestamps[i];
        }

        // A window of k timestamps has exactly (k - 1) gaps.
        int signatureLength = k - 1;

        // Number of timestamp windows of size k is n - k + 1.
        // Equivalently, number of gap subarrays of length signatureLength is:
        // gapCount - signatureLength + 1 = (n - 1) - (k - 1) + 1 = n - k + 1
        int windowCount = n - k + 1;

        // We will use double rolling hash:
        // hash1 under MOD1, hash2 under MOD2.
        //
        // Why double hash?
        // A single hash can theoretically collide. Double hashing makes collision
        // probability extremely small for practical purposes.
        final long MOD1 = 1_000_000_007L;
        final long MOD2 = 1_000_000_009L;
        final long BASE1 = 911_382_323L;
        final long BASE2 = 972_663_749L;

        // Precompute powers for the rolling hash window length.
        long[] pow1 = new long[gapCount + 1];
        long[] pow2 = new long[gapCount + 1];
        pow1[0] = 1L;
        pow2[0] = 1L;
        for (int i = 1; i <= gapCount; i++) {
            pow1[i] = multiplyMod(pow1[i - 1], BASE1, MOD1);
            pow2[i] = multiplyMod(pow2[i - 1], BASE2, MOD2);
        }

        // Build prefix hashes over the gaps array.
        //
        // We cannot directly store raw gap values into the polynomial hash without
        // reducing them modulo MOD, because gaps may be very large.
        //
        // Standard prefix hash recurrence:
        // prefix[i+1] = prefix[i] * BASE + value
        //
        // Then subarray hash [l..r] can be extracted in O(1).
        long[] prefix1 = new long[gapCount + 1];
        long[] prefix2 = new long[gapCount + 1];
        for (int i = 0; i < gapCount; i++) {
            long value1 = normalizeGapForHash(gaps[i], MOD1);
            long value2 = normalizeGapForHash(gaps[i], MOD2);

            prefix1[i + 1] = (multiplyMod(prefix1[i], BASE1, MOD1) + value1) % MOD1;
            prefix2[i + 1] = (multiplyMod(prefix2[i], BASE2, MOD2) + value2) % MOD2;
        }

        // Count how many times each signature hash appears.
        //
        // Each length-k timestamp window corresponds to one length-(k-1) gap subarray.
        // If a signature appears c times, it contributes c choose 2 unordered pairs.
        Map<HashPair, Long> frequency = new HashMap<>(windowCount * 2);

        for (int start = 0; start < windowCount; start++) {
            int endExclusive = start + signatureLength;

            long h1 = subarrayHash(prefix1, pow1, start, endExclusive, MOD1);
            long h2 = subarrayHash(prefix2, pow2, start, endExclusive, MOD2);

            HashPair key = new HashPair(h1, h2);
            frequency.put(key, frequency.getOrDefault(key, 0L) + 1L);
        }

        long answer = 0L;

        // Sum c choose 2 over all equal-signature groups.
        for (long count : frequency.values()) {
            answer += combinationsOfTwo(count);
        }

        return answer;
    }

    /**
     * Convenience overload that accepts int[] input.
     * This is useful for small demonstrations, but internally the algorithm
     * still works with long values.
     *
     * @param timestamps the sorted event times in nondecreasing order
     * @param k the exact window size
     * @return the number of unordered equivalent window pairs
     *
     * Time complexity: O(n), where n = timestamps.length
     * Space complexity: O(n)
     */
    public long countEquivalentAccessWindows(int[] timestamps, int k) {
        long[] converted = new long[timestamps.length];
        for (int i = 0; i < timestamps.length; i++) {
            converted[i] = timestamps[i];
        }
        return countEquivalentAccessWindows(converted, k);
    }

    /**
     * Computes "n choose 2", i.e. the number of unordered pairs that can be formed
     * from n items.
     *
     * @param n the number of items
     * @return n * (n - 1) / 2
     *
     * Time complexity: O(1)
     * Space complexity: O(1)
     */
    public long combinationsOfTwo(long n) {
        return n * (n - 1L) / 2L;
    }

    /**
     * Extracts the rolling hash of the subarray [left, rightExclusive).
     *
     * If prefix is built as:
     *   prefix[i+1] = prefix[i] * BASE + value[i]
     * then:
     *   hash(left, right) = prefix[right] - prefix[left] * BASE^(right-left)
     *
     * We take modulo carefully to keep the result nonnegative.
     *
     * @param prefix prefix hash array
     * @param powers precomputed powers of the base
     * @param left inclusive start index
     * @param rightExclusive exclusive end index
     * @param mod modulus used by this hash
     * @return the hash value of the requested subarray
     *
     * Time complexity: O(1)
     * Space complexity: O(1)
     */
    public long subarrayHash(long[] prefix, long[] powers, int left, int rightExclusive, long mod) {
        long remove = multiplyMod(prefix[left], powers[rightExclusive - left], mod);
        long result = prefix[rightExclusive] - remove;
        result %= mod;
        if (result < 0) {
            result += mod;
        }
        return result;
    }

    /**
     * Normalizes a gap value so it can be safely inserted into a modular rolling hash.
     *
     * We add 1 after modulo reduction so that a gap value of 0 still contributes
     * a nontrivial symbol in the polynomial hash.
     *
     * @param gap the original gap value
     * @param mod the modulus of the hash
     * @return a normalized value in the range [1, mod]
     *
     * Time complexity: O(1)
     * Space complexity: O(1)
     */
    public long normalizeGapForHash(long gap, long mod) {
        return (gap % mod) + 1L;
    }

    /**
     * Multiplies two numbers under a modulus.
     *
     * In this problem, the chosen moduli are around 1e9 and the values are also
     * below the modulus, so the product fits safely in signed 64-bit long:
     * about 1e18, which is below Long.MAX_VALUE.
     *
     * @param a first factor
     * @param b second factor
     * @param mod modulus
     * @return (a * b) % mod
     *
     * Time complexity: O(1)
     * Space complexity: O(1)
     */
    public long multiplyMod(long a, long b, long mod) {
        return (a * b) % mod;
    }

    /**
     * Demonstrates the solution on the examples and a few extra checks.
     *
     * @param args command-line arguments (unused)
     * @return nothing
     *
     * Time complexity: O(total input size of the demonstrations)
     * Space complexity: O(total input size of the demonstrations)
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Example 1:
        // timestamps = [2, 5, 9, 12, 15, 19], k = 3
        // Windows:
        // [2,5,9]    -> gaps [3,4]
        // [5,9,12]   -> gaps [4,3]
        // [9,12,15]  -> gaps [3,3]
        // [12,15,19] -> gaps [3,4]
        // Matching unordered pairs = 1
        long[] timestamps1 = {2L, 5L, 9L, 12L, 15L, 19L};
        int k1 = 3;
        System.out.println(solution.countEquivalentAccessWindows(timestamps1, k1)); // Expected: 1

        // Example 2:
        // timestamps = [7, 7, 10, 13, 13, 16], k = 2
        // Windows:
        // [7,7]   -> [0]
        // [7,10]  -> [3]
        // [10,13] -> [3]
        // [13,13] -> [0]
        // [13,16] -> [3]
        // Signature [0] appears 2 times -> 1 pair
        // Signature [3] appears 3 times -> 3 pairs
        // Total = 4
        long[] timestamps2 = {7L, 7L, 10L, 13L, 13L, 16L};
        int k2 = 2;
        System.out.println(solution.countEquivalentAccessWindows(timestamps2, k2)); // Expected: 4

        // Special case: k = 1
        // Every single-element window has empty signature.
        // For 4 timestamps, answer = C(4,2) = 6
        long[] timestamps3 = {100L, 200L, 200L, 500L};
        int k3 = 1;
        System.out.println(solution.countEquivalentAccessWindows(timestamps3, k3)); // Expected: 6

        // Extra check:
        // timestamps = [1,3,6,8,11]
        // k = 3
        // gaps = [2,3,2,3]
        // signatures of length 2:
        // [2,3], [3,2], [2,3]
        // answer = 1
        long[] timestamps4 = {1L, 3L, 6L, 8L, 11L};
        int k4 = 3;
        System.out.println(solution.countEquivalentAccessWindows(timestamps4, k4)); // Expected: 1
    }

    /**
     * Small immutable key class for storing a pair of hash values in a HashMap.
     */
    public static final class HashPair {
        private final long first;
        private final long second;

        /**
         * Creates a pair of hash values.
         *
         * @param first first hash
         * @param second second hash
         */
        public HashPair(long first, long second) {
            this.first = first;
            this.second = second;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof HashPair)) {
                return false;
            }
            HashPair other = (HashPair) obj;
            return this.first == other.first && this.second == other.second;
        }

        @Override
        public int hashCode() {
            return Objects.hash(first, second);
        }
    }
}