import java.util.*;

/*
Problem Title: Count Servers With Pairwise-Unique Capability Masks

Problem Description:
You are given an integer array masks where masks[i] represents the enabled capabilities
of the i-th server as a bitmask. Two servers are considered compatible for a special
deployment if they do not share any enabled capability bit. In other words, for servers
i and j, they are compatible if (masks[i] & masks[j]) == 0.

Return the number of unordered pairs of distinct servers that are compatible.

This problem is designed for situations where each server has a small fixed set of
possible capability bits, but the number of servers can be large. A brute-force O(n^2)
comparison over all pairs may be too slow. You should take advantage of the bitmask
structure to count valid pairs efficiently.

The answer can be large, so return it as a 64-bit integer.

Constraints:
- 1 <= masks.length <= 200000
- 0 <= masks[i] < 2^20
- Capability bits are numbered from 0 to 19
- Multiple servers may have the same mask value

Example 1:
Input: masks = [1, 2, 3, 4]
Output: 4

Explanation:
The compatible pairs are:
- (1, 2) because 001 & 010 = 0
- (1, 4) because 001 & 100 = 0
- (2, 4) because 010 & 100 = 0
- (3, 4) because 011 & 100 = 0

Example 2:
Input: masks = [0, 1, 1, 2, 6]
Output: 6

Explanation:
A mask of 0 is compatible with every other mask. The valid unordered pairs are:
- (0, 1) using the first 1
- (0, 1) using the second 1
- (0, 2)
- (0, 6)
- (1, 2) using the first 1
- (1, 2) using the second 1
No other pair has bitwise AND equal to 0.
*/

public class Solution {

    /**
     * Counts the number of unordered pairs of distinct servers whose masks are bitwise disjoint.
     *
     * Core idea:
     * 1. Count how many times each exact mask appears.
     * 2. Build a "subset-sum over masks" table where subsetCount[s] tells us:
     *    how many input masks are subsets of s.
     * 3. For a server with mask m, any compatible partner must use only bits that are NOT in m.
     *    Therefore, the partner mask must be a subset of (~m) within the 20-bit universe.
     * 4. So the number of compatible servers for mask m is subsetCount[complementOf(m)].
     * 5. Summing this over all servers counts every unordered pair twice, so divide by 2.
     *
     * Why this works:
     * - If a partner mask x is a subset of complement(m), then x has no bit in common with m,
     *   which means (m & x) == 0.
     * - Conversely, if (m & x) == 0, then every set bit of x must lie in complement(m),
     *   so x is a subset of complement(m).
     *
     * @param masks the array of server capability bitmasks
     * @return the number of unordered compatible pairs as a 64-bit integer
     * Time complexity: O(20 * 2^20 + n)
     * Space complexity: O(2^20)
     */
    public long countCompatiblePairs(int[] masks) {
        final int BITS = 20;
        final int SIZE = 1 << BITS;
        final int FULL_MASK = SIZE - 1;

        // freq[mask] = how many servers have exactly this mask.
        int[] freq = new int[SIZE];
        for (int mask : masks) {
            freq[mask]++;
        }

        // subsetCount starts as the exact frequencies.
        // After SOS DP, subsetCount[s] will equal:
        // sum of freq[sub] for every sub that is a subset of s.
        int[] subsetCount = Arrays.copyOf(freq, SIZE);

        // SOS DP (Sum Over Subsets Dynamic Programming)
        //
        // Detailed intuition:
        // We want, for every mask s, the total number of input masks that are subsets of s.
        //
        // Initially:
        //   subsetCount[s] = freq[s]
        //
        // Then for each bit, we "merge in" counts from masks that differ by that bit.
        // If bit b is set in s, then any subset of s either:
        //   - does not use bit b, or
        //   - may use bit b
        //
        // By doing:
        //   subsetCount[s] += subsetCount[s without bit b]
        //
        // across all bits, we accumulate counts from all subsets.
        for (int bit = 0; bit < BITS; bit++) {
            for (int mask = 0; mask < SIZE; mask++) {
                if ((mask & (1 << bit)) != 0) {
                    subsetCount[mask] += subsetCount[mask ^ (1 << bit)];
                }
            }
        }

        long orderedPairs = 0L;

        // For each server mask m:
        // - compatible partners are exactly the masks that are subsets of complement(m)
        // - subsetCount[complement(m)] gives how many such servers exist in the whole array
        //
        // Important note:
        // - If m != 0, then m is NOT a subset of complement(m), so the server does not count itself.
        // - If m == 0, then complement(m) is all 20 bits, and 0 is a subset of that,
        //   so each zero-mask server counts itself once. This is okay because:
        //     * all zero-zero pairs are valid,
        //     * summing over all servers counts every valid unordered pair exactly twice,
        //       including zero-zero pairs, and self-counts from zero masks contribute correctly
        //       to that double-counting framework.
        //
        // More concretely for zero masks:
        //   If there are z zero-mask servers, each one sees z zero-mask servers including itself.
        //   Total contribution among zeros is z * z in ordered counting.
        //   After dividing by 2? That seems suspicious if interpreted alone.
        //   But remember orderedPairs counts all ordered compatible pairs (i, j), including i == j
        //   only when mask[i] == 0.
        //   To avoid any ambiguity and guarantee correctness for distinct-server pairs only,
        //   we will explicitly subtract self-pairs for zero masks below.
        int zeroCount = freq[0];

        for (int mask : masks) {
            int complement = FULL_MASK ^ mask;
            orderedPairs += subsetCount[complement];
        }

        // Remove invalid self-pairs.
        //
        // The only time a server is counted as compatible with itself is when mask == 0,
        // because 0 & 0 == 0.
        // But the problem asks for pairs of DISTINCT servers, so these self-pairs must be removed.
        orderedPairs -= zeroCount;

        // Every valid unordered pair {i, j} with i != j is counted twice in orderedPairs:
        // once as (i, j) and once as (j, i).
        return orderedPairs / 2L;
    }

    /**
     * A simple brute-force checker for small inputs.
     * This is useful for demonstration and validation, though it is too slow for large constraints.
     *
     * @param masks the array of server capability bitmasks
     * @return the number of unordered compatible pairs
     * Time complexity: O(n^2)
     * Space complexity: O(1)
     */
    public long countCompatiblePairsBruteForce(int[] masks) {
        long count = 0L;

        for (int i = 0; i < masks.length; i++) {
            for (int j = i + 1; j < masks.length; j++) {
                if ((masks[i] & masks[j]) == 0) {
                    count++;
                }
            }
        }

        return count;
    }

    /**
     * Demonstrates the solution on sample inputs from the problem statement
     * and compares the optimized result with a brute-force result for confidence.
     *
     * @param args command-line arguments (not used)
     * @return nothing
     * Time complexity: O(20 * 2^20 + n) for each optimized call shown here
     * Space complexity: O(2^20)
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        int[] masks1 = {1, 2, 3, 4};
        long result1 = solution.countCompatiblePairs(masks1);
        long brute1 = solution.countCompatiblePairsBruteForce(masks1);
        System.out.println("Example 1 optimized: " + result1);
        System.out.println("Example 1 brute force: " + brute1);
        System.out.println("Expected: 4");
        System.out.println();

        int[] masks2 = {0, 1, 1, 2, 6};
        long result2 = solution.countCompatiblePairs(masks2);
        long brute2 = solution.countCompatiblePairsBruteForce(masks2);
        System.out.println("Example 2 optimized: " + result2);
        System.out.println("Example 2 brute force: " + brute2);
        System.out.println("Expected: 6");
        System.out.println();

        int[] extra = {0, 0, 0};
        long result3 = solution.countCompatiblePairs(extra);
        long brute3 = solution.countCompatiblePairsBruteForce(extra);
        System.out.println("Extra test optimized: " + result3);
        System.out.println("Extra test brute force: " + brute3);
        System.out.println("Expected: 3");
    }
}