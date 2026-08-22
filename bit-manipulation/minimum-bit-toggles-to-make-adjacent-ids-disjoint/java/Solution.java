import java.util.*;

/*
Problem Title: Minimum Bit Toggles to Make Adjacent IDs Disjoint

Problem Description:
You are given an array nums of length n, where each nums[i] is a non-negative integer representing
a device ID encoded as a bitmask. Two neighboring device IDs are considered conflicting if they
share at least one common set bit, meaning (nums[i] & nums[i+1]) != 0.

In one operation, you may toggle off exactly one set bit from any single element in the array.
In other words, if bit b is currently 1 in nums[i], you may change nums[i] to nums[i] ^ (1 << b).
You are not allowed to toggle a 0 bit on, and you may perform any number of operations.

Return the minimum number of bit-toggle operations required so that every adjacent pair becomes
disjoint, i.e. for every i from 0 to n - 2, (nums[i] & nums[i+1]) == 0.

Your goal is to minimize the total number of toggled bits across the entire array.

Constraints:
- 1 <= n <= 100000
- 0 <= nums[i] < 2^20
- The answer always fits in a 32-bit signed integer.

Example 1:
Input: nums = [3, 6, 5]
Output: 2

Example 2:
Input: nums = [7, 7]
Output: 3

Key Insight:
Because we are only allowed to turn 1-bits off, each bit position behaves independently.
For a fixed bit b, look at the indices where nums[i] contains that bit.
For every adjacent pair of such indices (i, i+1 in the array), at least one endpoint must remove
that bit. Therefore, for each bit independently, we need the minimum number of chosen vertices in
a path graph so that every edge is covered. That is exactly the minimum vertex cover on a path,
which equals the size of a maximum matching on a path, and can be computed greedily by counting
adjacent pairs among positions containing that bit.

Equivalently:
For each bit b:
- Build the subsequence of indices where bit b is set.
- Consecutive indices in that subsequence that differ by 1 form edges in a path.
- The minimum removals for that bit is the minimum vertex cover of those path components.
- For a run of length L consecutive array positions having bit b set, the answer contribution is floor((L + 1) / 2),
  which is also ceil((L - 1) / 2), i.e. the maximum matching size of a path with L vertices.

Since there are only 20 bits, we can scan the array once per bit.
*/
public class Solution {

    private static final int MAX_BITS = 20;

    /**
     * Computes the minimum number of single-bit toggle-off operations needed so that
     * every adjacent pair of numbers becomes bitwise disjoint.
     *
     * Core idea:
     * Each bit position can be solved independently because removing one bit from one number
     * only affects conflicts involving that same bit. So we sum the optimal cost over all bits.
     *
     * For one fixed bit:
     * - Consider all array positions where this bit is currently 1.
     * - If two neighboring array positions both have this bit, then that adjacent pair conflicts
     *   on this bit, so at least one of those two positions must remove the bit.
     * - Over a consecutive run of positions where the bit is present, this becomes a path graph.
     * - The minimum number of removals needed on a path is the minimum vertex cover of that path,
     *   which equals floor((runLength + 1) / 2).
     *
     * We can compute this greedily while scanning:
     * - Track the length of the current consecutive run for each bit.
     * - When the run ends, add its contribution and reset.
     *
     * @param nums the input array of non-negative integers representing bitmasks
     * @return the minimum total number of bit-toggle-off operations required
     * Time complexity: O(n * 20), which is O(n) because 20 is a constant.
     * Space complexity: O(1) extra space, excluding the input array.
     */
    public int minBitToggles(int[] nums) {
        int n = nums.length;
        int answer = 0;

        /*
         * We process each bit independently.
         *
         * Why is this valid?
         * Because the total cost is simply the number of removed bits.
         * Removing bit 3 from nums[i] has no interaction with whether bit 7 should be removed.
         * The adjacency condition (nums[i] & nums[i+1]) == 0 means:
         * for every bit, that bit cannot remain set in both adjacent numbers simultaneously.
         *
         * So the global optimum is the sum of per-bit optima.
         */
        for (int bit = 0; bit < MAX_BITS; bit++) {
            int runLength = 0;

            /*
             * We scan the array and look only at whether the current bit is set.
             *
             * Example for one bit:
             * presence pattern: 1 1 0 1 1 1 0
             * runs are lengths 2 and 3
             * contribution = floor((2+1)/2) + floor((3+1)/2) = 1 + 2 = 3
             */
            for (int i = 0; i < n; i++) {
                boolean hasBit = ((nums[i] >> bit) & 1) == 1;

                if (hasBit) {
                    // Extend the current consecutive run of positions containing this bit.
                    runLength++;
                } else {
                    // The run ended here, so finalize its contribution.
                    answer += contributionOfRun(runLength);
                    runLength = 0;
                }
            }

            // Finalize the last run if the array ended while we were inside one.
            answer += contributionOfRun(runLength);
        }

        return answer;
    }

    /**
     * Returns the minimum number of removals needed for one bit over a consecutive run
     * of positions where that bit is set.
     *
     * Suppose a bit appears in a consecutive run of length L:
     * positions: p, p+1, ..., p+L-1
     *
     * Then every adjacent pair inside that run creates a conflict edge:
     * (p,p+1), (p+1,p+2), ..., (p+L-2,p+L-1)
     *
     * This is exactly a path graph with L vertices and L-1 edges.
     * We need the minimum number of vertices to choose so that every edge has at least one endpoint chosen.
     * That is the minimum vertex cover on a path, whose size is floor(L / 2) when counting edges?
     *
     * Careful:
     * For a path with L vertices:
     * - L=1 => 0 edges => 0 removals
     * - L=2 => 1 edge  => 1 removal
     * - L=3 => 2 edges => 1 removal
     * - L=4 => 3 edges => 2 removals
     * - L=5 => 4 edges => 2 removals
     *
     * Formula: floor(L / 2)
     *
     * Let's verify:
     * L=1 -> 0
     * L=2 -> 1
     * L=3 -> 1
     * L=4 -> 2
     * L=5 -> 2
     *
     * Correct.
     *
     * @param runLength the number of consecutive array positions containing a particular bit
     * @return the minimum removals needed for that bit within this run
     * Time complexity: O(1)
     * Space complexity: O(1)
     */
    public int contributionOfRun(int runLength) {
        return runLength / 2;
    }

    /**
     * A second implementation using dynamic programming over kept masks.
     *
     * This method is included for educational completeness because the prompt mentions
     * "dynamic programming over the bits kept in each position".
     *
     * Important note:
     * This DP is only practical for very small inputs because each number may have many submasks.
     * It is NOT the method used for the final efficient solution under the given constraints.
     *
     * DP idea:
     * - For each position i, choose a submask keep of nums[i] (the bits we decide to keep).
     * - Cost at position i is popcount(nums[i]) - popcount(keep), i.e. removed bits.
     * - Validity requires (prevKeep & keep) == 0 for adjacent positions.
     * - Minimize total cost.
     *
     * @param nums the input array
     * @return the minimum total removals, computed by a brute-force style DP suitable only for tiny arrays
     * Time complexity: Exponential in the number of set bits per element; not suitable for large constraints.
     * Space complexity: Exponential in the number of submasks stored for DP states.
     */
    public int minBitTogglesDpEducational(int[] nums) {
        Map<Integer, Integer> dp = new HashMap<>();
        dp.put(0, 0);

        for (int value : nums) {
            List<Integer> submasks = generateSubmasks(value);
            Map<Integer, Integer> next = new HashMap<>();

            for (int keep : submasks) {
                int cost = Integer.bitCount(value) - Integer.bitCount(keep);

                for (Map.Entry<Integer, Integer> entry : dp.entrySet()) {
                    int prevKeep = entry.getKey();
                    int prevCost = entry.getValue();

                    if ((prevKeep & keep) == 0) {
                        int newCost = prevCost + cost;
                        next.merge(keep, newCost, Math::min);
                    }
                }
            }

            dp = next;
        }

        int answer = Integer.MAX_VALUE;
        for (int cost : dp.values()) {
            answer = Math.min(answer, cost);
        }
        return answer;
    }

    /**
     * Generates all submasks of a given mask.
     *
     * Example:
     * mask = 6 (110)
     * submasks are: 6 (110), 4 (100), 2 (010), 0 (000)
     *
     * @param mask the original bitmask
     * @return a list containing all submasks of mask
     * Time complexity: O(2^k), where k is the number of set bits in mask
     * Space complexity: O(2^k)
     */
    public List<Integer> generateSubmasks(int mask) {
        List<Integer> result = new ArrayList<>();
        int sub = mask;

        while (true) {
            result.add(sub);
            if (sub == 0) {
                break;
            }
            sub = (sub - 1) & mask;
        }

        return result;
    }

    /**
     * Demonstrates the solution on sample inputs from the problem statement and a few extra cases.
     *
     * @param args command-line arguments, not used
     * @return nothing
     * Time complexity: O(total input size * 20) for the demonstrated examples
     * Space complexity: O(1) extra space excluding arrays used for examples
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        int[] nums1 = {3, 6, 5};
        int result1 = solution.minBitToggles(nums1);
        System.out.println("Input: [3, 6, 5]");
        System.out.println("Output: " + result1);
        System.out.println("Expected: 2");
        System.out.println();

        int[] nums2 = {7, 7};
        int result2 = solution.minBitToggles(nums2);
        System.out.println("Input: [7, 7]");
        System.out.println("Output: " + result2);
        System.out.println("Expected: 3");
        System.out.println();

        int[] nums3 = {0};
        int result3 = solution.minBitToggles(nums3);
        System.out.println("Input: [0]");
        System.out.println("Output: " + result3);
        System.out.println("Expected: 0");
        System.out.println();

        int[] nums4 = {1, 1, 1};
        int result4 = solution.minBitToggles(nums4);
        System.out.println("Input: [1, 1, 1]");
        System.out.println("Output: " + result4);
        System.out.println("Expected: 1");
        System.out.println();

        int[] nums5 = {3, 3, 3};
        int result5 = solution.minBitToggles(nums5);
        System.out.println("Input: [3, 3, 3]");
        System.out.println("Output: " + result5);
        System.out.println("Expected: 2");
        System.out.println();

        /*
         * Quick verification of the two official examples:
         *
         * Example 1: [3, 6, 5]
         * Binary:
         * 3 = 011
         * 6 = 110
         * 5 = 101
         *
         * Bit 0 presence: positions 0 and 2, not adjacent => cost 0
         * Bit 1 presence: positions 0 and 1, adjacent => cost 1
         * Bit 2 presence: positions 1 and 2, adjacent => cost 1
         * Total = 2
         *
         * Example 2: [7, 7]
         * For each of bits 0,1,2 we have a run of length 2 => cost 1 each
         * Total = 3
         *
         * So the implementation matches the required outputs.
         */
    }
}