import java.util.*;

/*
 * Title: Longest Prefix With Unique Running Difference Signatures
 * Difficulty: Hard
 * Topic: Hashing
 *
 * Problem Description:
 * You are given an integer array nums of length n. For any subarray nums[l..r], define its running
 * difference signature as the sequence of adjacent differences:
 * [nums[l+1] - nums[l], nums[l+2] - nums[l+1], ..., nums[r] - nums[r-1]].
 *
 * Two subarrays are considered equivalent if their running difference signatures are exactly the same
 * length and contain the same values in the same order. A subarray of length 1 has an empty signature.
 *
 * Your task is to find the maximum integer L such that every subarray of nums with length L has a unique
 * running difference signature. In other words, among all windows of length L, no two different starting
 * positions produce the same adjacent-difference sequence.
 *
 * Return the largest possible L.
 *
 * This problem is intended to be solved efficiently for large inputs. A brute-force comparison of all
 * subarrays will time out. Since equivalent signatures depend only on adjacent differences, strong hashing
 * or rolling-hash techniques over the difference array are usually required. Be careful with collisions if
 * you use a probabilistic hash.
 *
 * Constraints:
 * - 1 <= n <= 200000
 * - -10^9 <= nums[i] <= 10^9
 * - Subarrays are contiguous
 * - The answer is always between 1 and n
 *
 * Notes about the examples:
 * Under the exact statement above, any length L with only one subarray is trivially valid.
 * Therefore, for any array, L = n is always valid because there is exactly one subarray of length n.
 * So the mathematically correct answer under the stated definition is always n.
 *
 * However, to make the problem meaningful and aligned with the hashing discussion, this implementation
 * also provides a second method:
 *
 * - maxLengthWithAtLeastTwoWindowsUnique(...)
 *
 *   This variant asks for the largest L such that:
 *   1) every subarray of length L has a unique signature, and
 *   2) there are at least two windows of that length.
 *
 *   Equivalently, among lengths 1..n-1, find the largest valid L.
 *
 * This second variant is the interesting one algorithmically, because it reduces to finding the shortest
 * unique substring length in the difference array and then converting back to a window length in nums.
 *
 * Example 1:
 * nums = [5, 8, 6, 9, 7]
 * differences = [3, -2, 3, -2]
 * - Under the exact statement: answer = 5
 * - Under the "at least two windows" variant: answer = 3
 *
 * Example 2:
 * nums = [4, 7, 10, 13, 16]
 * differences = [3, 3, 3, 3]
 * - Under the exact statement: answer = 5
 * - Under the "at least two windows" variant: no length 1..4 is valid, so answer = 0
 *
 * This file demonstrates both interpretations clearly.
 */
public class Solution {

    /**
     * Returns the answer under the exact problem statement.
     *
     * Since a length-n subarray is the entire array, there is exactly one such subarray.
     * Therefore all subarrays of length n are trivially unique, so the maximum valid L is always n.
     *
     * @param nums the input array
     * @return the largest possible L under the exact statement, which is nums.length
     * Time complexity: O(1)
     * Space complexity: O(1)
     */
    public int longestPrefixWithUniqueRunningDifferenceSignatures(int[] nums) {
        return nums.length;
    }

    /**
     * Returns the largest window length L such that:
     * 1) there are at least two windows of length L, and
     * 2) all those windows have pairwise distinct running difference signatures.
     *
     * This is the meaningful algorithmic variant.
     *
     * Key transformation:
     * A window nums[i..i+L-1] has signature equal to the subarray
     * diff[i..i+L-2], where diff[j] = nums[j+1] - nums[j].
     *
     * So uniqueness of all length-L window signatures in nums is exactly the same as uniqueness of all
     * length-(L-1) subarrays in diff.
     *
     * Therefore:
     * - Let k = L - 1.
     * - We need the largest k >= 0 such that all length-k subarrays of diff are distinct,
     *   with at least two windows in nums, meaning L <= n - 1, so k <= n - 2.
     *
     * For k = 0 (which corresponds to L = 1), all signatures are empty. If there are at least two windows,
     * they are all equal, so uniqueness fails whenever n >= 2. Thus only k >= 1 can work in the
     * "at least two windows" variant.
     *
     * Efficient approach:
     * - Build the difference array diff of length m = n - 1.
     * - Use double rolling hash over diff.
     * - Binary search the smallest k such that all length-k subarrays of diff are unique.
     *   Why binary search works:
     *   If all length-k subarrays are unique, then all longer lengths are also unique.
     *   Reason: if two longer subarrays were equal, their first k elements would also be equal.
     * - Once we find the smallest valid k, then every larger k is valid too.
     *   The largest L with at least two windows is then:
     *      L = min(n - 1, n) if valid...
     *   Since we require at least two windows, L <= n - 1, so k <= n - 2.
     *   If the smallest valid k is t, then the largest valid k in range [t, n-2] is n-2.
     *   But uniqueness for larger k is automatic, so if any valid k exists, the largest valid L is n - 1.
     *
     * This reveals an important simplification:
     * - For the "at least two windows" variant, the answer is either:
     *   - n - 1, if the two length-(n-1) windows are distinct, or
     *   - possibly smaller lengths if n-1 fails.
     *
     * More directly:
     * - Length L has at least two windows iff L <= n - 1.
     * - The largest such L is n - 1.
     * - It is valid iff the two windows of length n - 1 have different signatures.
     * - Those signatures are diff[0..n-3] and diff[1..n-2].
     * - If they differ, answer is n - 1.
     * - Otherwise try n - 2, etc.
     *
     * To stay faithful to the intended hashing-based problem, we compute the largest L in [1, n-1]
     * for which all signatures are unique using binary search + rolling hash.
     *
     * @param nums the input array
     * @return the largest valid L among lengths having at least two windows; returns 0 if none exists
     * Time complexity: O(n log n)
     * Space complexity: O(n)
     */
    public int maxLengthWithAtLeastTwoWindowsUnique(int[] nums) {
        int n = nums.length;

        // If n <= 1, there are not at least two windows for any positive length.
        if (n <= 1) {
            return 0;
        }

        // Build the difference array.
        long[] diff = buildDifferenceArray(nums);
        int m = diff.length; // m = n - 1

        // For L = 1, signatures are empty for every window.
        // If n >= 2, there are at least two windows of length 1, and they all collide.
        // So the smallest meaningful signature length in diff is k = 1, corresponding to L = 2.
        if (m == 0) {
            return 0;
        }

        // We want the largest L <= n - 1 such that all length-(L-1) subarrays in diff are unique.
        // Let k = L - 1. Then k ranges from 1 to m - 1 inclusive, because:
        // - L <= n - 1
        // - k = L - 1 <= n - 2 = m - 1
        //
        // If m - 1 < 1, there is no such k.
        if (m - 1 < 1) {
            return 0;
        }

        DoubleRollingHash hash = new DoubleRollingHash(diff);

        // Binary search the smallest k in [1, m-1] such that all length-k subarrays are unique.
        // Because uniqueness is monotone:
        // if length k is unique for all windows, then every larger length is also unique.
        int left = 1;
        int right = m - 1;
        int firstValidK = -1;

        while (left <= right) {
            int mid = left + (right - left) / 2;

            if (allSubarraysOfLengthKAreUnique(hash, m, mid)) {
                firstValidK = mid;
                right = mid - 1;
            } else {
                left = mid + 1;
            }
        }

        // If no k in [1, m-1] works, then no L in [2, n-1] works.
        if (firstValidK == -1) {
            return 0;
        }

        // Since uniqueness is monotone upward in k, every k >= firstValidK is valid.
        // The largest allowed k with at least two windows is m - 1.
        // Convert back: L = k + 1 = m = n - 1.
        //
        // So if any valid length with at least two windows exists, the largest one is always n - 1.
        // We return it directly.
        return n - 1;
    }

    /**
     * Builds the adjacent-difference array.
     *
     * For nums = [a0, a1, a2, ...], returns:
     * [a1-a0, a2-a1, a3-a2, ...]
     *
     * We store differences in long to safely handle subtraction of int values near the limits.
     *
     * @param nums the input array
     * @return the difference array as long[]
     * Time complexity: O(n)
     * Space complexity: O(n)
     */
    public long[] buildDifferenceArray(int[] nums) {
        int n = nums.length;
        long[] diff = new long[Math.max(0, n - 1)];

        for (int i = 0; i + 1 < n; i++) {
            diff[i] = (long) nums[i + 1] - nums[i];
        }

        return diff;
    }

    /**
     * Checks whether all subarrays of a fixed length k in the difference array are unique.
     *
     * We use double rolling hash:
     * - Compute the hash of every length-k subarray.
     * - Insert the pair of hashes into a HashSet.
     * - If a pair repeats, we treat it as a duplicate signature.
     *
     * With two large moduli, collision probability is extremely small and suitable for competitive
     * programming / interview settings.
     *
     * @param hash precomputed rolling hash structure over the difference array
     * @param m the length of the difference array
     * @param k the subarray length in the difference array
     * @return true if every length-k subarray is unique, false otherwise
     * Time complexity: O(m)
     * Space complexity: O(m)
     */
    public boolean allSubarraysOfLengthKAreUnique(DoubleRollingHash hash, int m, int k) {
        // Number of length-k subarrays in an array of length m is (m - k + 1).
        int windows = m - k + 1;

        // Store combined double-hash values.
        HashSet<Long> seen = new HashSet<>(Math.max(16, windows * 2));

        for (int start = 0; start + k <= m; start++) {
            long h1 = hash.getHash1(start, start + k);
            long h2 = hash.getHash2(start, start + k);

            // Combine two 32-bit-ish modular hashes into one long key.
            long key = (h1 << 32) ^ h2;

            if (!seen.add(key)) {
                return false;
            }
        }

        return true;
    }

    /**
     * Demonstrates the solution on the sample inputs and prints both interpretations:
     * 1) exact statement answer
     * 2) "at least two windows" variant answer
     *
     * @param args command-line arguments (unused)
     * @return nothing
     * Time complexity: O(n log n) for the demonstrated calls
     * Space complexity: O(n)
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        int[] nums1 = {5, 8, 6, 9, 7};
        int[] nums2 = {4, 7, 10, 13, 16};

        // Exact statement:
        // Since length n always has exactly one window, the answer is always n.
        System.out.println("Exact statement answers:");
        System.out.println(solution.longestPrefixWithUniqueRunningDifferenceSignatures(nums1)); // 5
        System.out.println(solution.longestPrefixWithUniqueRunningDifferenceSignatures(nums2)); // 5

        // Meaningful variant with at least two windows:
        System.out.println("At least two windows variant answers:");
        System.out.println(solution.maxLengthWithAtLeastTwoWindowsUnique(nums1)); // 4
        System.out.println(solution.maxLengthWithAtLeastTwoWindowsUnique(nums2)); // 0

        // Additional small sanity checks.
        int[] nums3 = {1};
        int[] nums4 = {1, 2};
        int[] nums5 = {1, 3, 2};

        System.out.println("More exact statement answers:");
        System.out.println(solution.longestPrefixWithUniqueRunningDifferenceSignatures(nums3)); // 1
        System.out.println(solution.longestPrefixWithUniqueRunningDifferenceSignatures(nums4)); // 2
        System.out.println(solution.longestPrefixWithUniqueRunningDifferenceSignatures(nums5)); // 3

        System.out.println("More at least two windows variant answers:");
        System.out.println(solution.maxLengthWithAtLeastTwoWindowsUnique(nums3)); // 0
        System.out.println(solution.maxLengthWithAtLeastTwoWindowsUnique(nums4)); // 0
        System.out.println(solution.maxLengthWithAtLeastTwoWindowsUnique(nums5)); // 2
    }

    /**
     * Double rolling hash over a long array.
     *
     * We hash the difference array rather than the original nums array because the signature of a window
     * depends only on adjacent differences.
     */
    static class DoubleRollingHash {
        private static final long MOD1 = 1_000_000_007L;
        private static final long MOD2 = 1_000_000_009L;

        // Large bases smaller than the moduli.
        private static final long BASE1 = 911_382_323L;
        private static final long BASE2 = 972_663_749L;

        private final long[] prefix1;
        private final long[] prefix2;
        private final long[] power1;
        private final long[] power2;

        /**
         * Precomputes prefix hashes and powers for the given array.
         *
         * Because the difference values can be negative and large, we first normalize each value into
         * the modular range before adding it into the polynomial hash.
         *
         * Hash convention for subarray [l, r):
         * hash = a[l] * base^(len-1) + a[l+1] * base^(len-2) + ... + a[r-1]
         *
         * @param arr the array to hash
         * @return nothing
         * Time complexity: O(n)
         * Space complexity: O(n)
         */
        public DoubleRollingHash(long[] arr) {
            int n = arr.length;
            prefix1 = new long[n + 1];
            prefix2 = new long[n + 1];
            power1 = new long[n + 1];
            power2 = new long[n + 1];

            power1[0] = 1L;
            power2[0] = 1L;

            for (int i = 0; i < n; i++) {
                power1[i + 1] = (power1[i] * BASE1) % MOD1;
                power2[i + 1] = (power2[i] * BASE2) % MOD2;

                long value1 = normalize(arr[i], MOD1);
                long value2 = normalize(arr[i], MOD2);

                prefix1[i + 1] = (prefix1[i] * BASE1 + value1) % MOD1;
                prefix2[i + 1] = (prefix2[i] * BASE2 + value2) % MOD2;
            }
        }

        /**
         * Returns the first modular hash of arr[left..right).
         *
         * @param left inclusive start index
         * @param right exclusive end index
         * @return hash modulo MOD1
         * Time complexity: O(1)
         * Space complexity: O(1)
         */
        public long getHash1(int left, int right) {
            long result = prefix1[right] - (prefix1[left] * power1[right - left]) % MOD1;
            if (result < 0) {
                result += MOD1;
            }
            return result;
        }

        /**
         * Returns the second modular hash of arr[left..right).
         *
         * @param left inclusive start index
         * @param right exclusive end index
         * @return hash modulo MOD2
         * Time complexity: O(1)
         * Space complexity: O(1)
         */
        public long getHash2(int left, int right) {
            long result = prefix2[right] - (prefix2[left] * power2[right - left]) % MOD2;
            if (result < 0) {
                result += MOD2;
            }
            return result;
        }

        /**
         * Normalizes a possibly negative long value into the range [0, mod).
         *
         * @param value the value to normalize
         * @param mod the modulus
         * @return normalized value
         * Time complexity: O(1)
         * Space complexity: O(1)
         */
        private long normalize(long value, long mod) {
            long result = value % mod;
            if (