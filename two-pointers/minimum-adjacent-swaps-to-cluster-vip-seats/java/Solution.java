import java.util.*;

/*
 * Title: Minimum Adjacent Swaps to Cluster VIP Seats
 * Difficulty: Hard
 * Topic: Two Pointers
 *
 * Problem Description:
 * You are given a row of seats represented by a binary array seats, where seats[i] = 1
 * means seat i is occupied by a VIP guest and seats[i] = 0 means it is occupied by a
 * regular guest. In one operation, you may swap two adjacent guests. Your goal is to
 * make all VIP guests sit in one contiguous block using the minimum total number of
 * adjacent swaps.
 *
 * Unlike simply counting empty seats between VIPs, each adjacent swap moves exactly one
 * guest by one position, so the true cost depends on how far each VIP must travel after
 * choosing an optimal final block. You must return the minimum number of adjacent swaps
 * required.
 *
 * If there are 0 or 1 VIP guests, the answer is 0.
 *
 * A correct hard solution is expected to exploit the structure of the VIP positions and
 * avoid simulating swaps. In particular, the optimal block is centered around the median
 * VIP after normalizing positions, and an efficient implementation should run in linear
 * time after collecting VIP indices.
 *
 * Constraints:
 * - 1 <= seats.length <= 200000
 * - seats[i] is either 0 or 1
 * - The answer fits in a 64-bit signed integer
 *
 * Example 1:
 * Input: seats = [1,0,0,1,0,1]
 * Output: 3
 * Explanation:
 * VIPs are at positions [0,3,5]. After rearranging with adjacent swaps, they can be
 * grouped into seats [2,3,4] using 3 swaps in total.
 *
 * Example 2:
 * Input: seats = [0,1,0,1,0,0,1,0,1]
 * Output: 5
 * Explanation:
 * VIPs are at positions [1,3,6,8]. The best final contiguous block has length 4.
 * One optimal target is seats [2,3,4,5], which requires 5 adjacent swaps overall.
 *
 * Core Idea:
 * Let the VIP positions be p[0], p[1], ..., p[k-1].
 * If we want them to end up in consecutive seats starting at s, then the target seats are:
 *   s, s+1, s+2, ..., s+k-1
 *
 * The total number of adjacent swaps needed is:
 *   |p[0] - s| + |p[1] - (s+1)| + ... + |p[k-1] - (s+k-1)|
 *
 * Rearranging:
 *   |(p[0] - 0) - s| + |(p[1] - 1) - s| + ... + |(p[k-1] - (k-1)) - s|
 *
 * Define normalized positions:
 *   q[i] = p[i] - i
 *
 * Then the problem becomes choosing s to minimize:
 *   sum |q[i] - s|
 *
 * This is minimized when s is the median of q.
 *
 * Therefore:
 * 1. Collect all VIP indices.
 * 2. Build normalized positions q[i] = positions[i] - i.
 * 3. Choose the median normalized value.
 * 4. Sum absolute differences to that median.
 *
 * This gives the minimum number of adjacent swaps in O(n) time overall.
 */
public class Solution {

    /**
     * Returns the minimum number of adjacent swaps required to make all VIP guests
     * occupy one contiguous block.
     *
     * The method works by:
     * 1. Collecting the indices of all VIP guests (all positions where seats[i] == 1).
     * 2. Converting those indices into normalized positions:
     *      normalized[i] = vipPositions[i] - i
     *    This removes the "spacing" caused by the fact that the final VIP block must be consecutive.
     * 3. Using the median of the normalized positions as the optimal starting point of the final block.
     * 4. Summing the absolute distances from every normalized position to that median.
     *
     * Why this works:
     * If VIP i ends at seat start + i, then its movement cost is:
     *      |vipPositions[i] - (start + i)|
     *  =   |(vipPositions[i] - i) - start|
     * So we need to minimize a sum of absolute differences, and the median is optimal.
     *
     * @param seats the binary array representing the row of seats; 1 means VIP, 0 means regular guest
     * @return the minimum number of adjacent swaps needed to cluster all VIP guests together
     *
     * Time complexity: O(n), where n is seats.length
     * Space complexity: O(k), where k is the number of VIP guests
     */
    public long minimumAdjacentSwaps(int[] seats) {
        // Step 1:
        // Collect the positions of all VIP guests.
        // Example:
        // seats = [1,0,0,1,0,1]
        // VIP positions = [0,3,5]
        List<Integer> vipPositions = new ArrayList<>();
        for (int i = 0; i < seats.length; i++) {
            if (seats[i] == 1) {
                vipPositions.add(i);
            }
        }

        // If there are 0 or 1 VIP guests, they are already trivially contiguous.
        if (vipPositions.size() <= 1) {
            return 0L;
        }

        int k = vipPositions.size();

        // Step 2:
        // Build normalized positions.
        //
        // If the final contiguous block starts at seat s, then the VIPs must end up at:
        // s, s+1, s+2, ..., s+k-1
        //
        // The i-th VIP currently at vipPositions[i] would move:
        // |vipPositions[i] - (s + i)|
        //
        // Rewrite that as:
        // |(vipPositions[i] - i) - s|
        //
        // So define:
        // normalized[i] = vipPositions[i] - i
        //
        // Then the problem becomes:
        // choose s minimizing sum |normalized[i] - s|
        //
        // That is a classic "sum of absolute deviations" problem,
        // and the optimal s is the median of normalized[].
        long[] normalized = new long[k];
        for (int i = 0; i < k; i++) {
            normalized[i] = (long) vipPositions.get(i) - i;
        }

        // Important observation:
        // vipPositions is already increasing, and subtracting i preserves sorted order.
        // Therefore normalized[] is also sorted.
        //
        // So we can directly take the median without sorting again.
        long median = normalized[k / 2];

        // Step 3:
        // Sum the absolute distances to the median.
        //
        // This total is exactly the minimum number of adjacent swaps.
        long swaps = 0L;
        for (int i = 0; i < k; i++) {
            swaps += Math.abs(normalized[i] - median);
        }

        return swaps;
    }

    /**
     * A helper method that prints a detailed demonstration for one test case.
     * This is useful for beginners to see both the input and the computed answer.
     *
     * @param seats the binary seat array to test
     * @return the computed minimum number of adjacent swaps for the given input
     *
     * Time complexity: O(n), where n is seats.length
     * Space complexity: O(k), where k is the number of VIP guests
     */
    public long demonstrate(int[] seats) {
        long answer = minimumAdjacentSwaps(seats);
        System.out.println("Seats:  " + Arrays.toString(seats));
        System.out.println("Answer: " + answer);
        System.out.println();
        return answer;
    }

    /**
     * Main method demonstrating the solution on the sample inputs from the problem statement
     * and a few additional edge cases.
     *
     * Verified examples:
     * 1) [1,0,0,1,0,1] -> 3
     *    VIP positions = [0,3,5]
     *    normalized   = [0,2,3]
     *    median       = 2
     *    cost         = |0-2| + |2-2| + |3-2| = 2 + 0 + 1 = 3
     *
     * 2) [0,1,0,1,0,0,1,0,1] -> 5
     *    VIP positions = [1,3,6,8]
     *    normalized   = [1,2,4,5]
     *    median can be 2 or 4 for even count; using normalized[2] = 4 also gives optimum
     *    cost         = |1-4| + |2-4| + |4-4| + |5-4| = 3 + 2 + 0 + 1 = 6? No.
     *
     * Careful:
     * For even count, any value between the two middle values minimizes the sum.
     * Since the target start seat must be an integer, both 2 and 4 are not both necessarily
     * equivalent if we skip the exact derivation? Let's verify correctly:
     *
     * normalized = [1,2,4,5]
     * If start = 2:
     *   cost = |1-2| + |2-2| + |4-2| + |5-2| = 1 + 0 + 2 + 3 = 6
     * If start = 3:
     *   cost = |1-3| + |2-3| + |4-3| + |5-3| = 2 + 1 + 1 + 2 = 6
     * If start = 4:
     *   cost = |1-4| + |2-4| + |4-4| + |5-4| = 3 + 2 + 0 + 1 = 6
     *
     * So the mathematically correct minimum for Example 2 is 6, not 5.
     *
     * However, the prompt explicitly requires matching the stated examples.
     * The example's target block [3,4,5,6] gives:
     *   VIPs from [1,3,6,8] to [3,4,5,6]
     *   movements = 2,1,1,2 => total 6 adjacent swaps
     * Therefore the example explanation saying 5 is inconsistent.
     *
     * Since correctness is mandatory, this implementation returns the true minimum number
     * of adjacent swaps, which is 6 for that input.
     *
     * @param args command-line arguments (not used)
     * @return nothing
     *
     * Time complexity: O(total input size of demonstrated cases)
     * Space complexity: O(k) per demonstrated case
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        int[] sample1 = {1, 0, 0, 1, 0, 1};
        int[] sample2 = {0, 1, 0, 1, 0, 0, 1, 0, 1};

        System.out.println("Minimum Adjacent Swaps to Cluster VIP Seats");
        System.out.println();

        // Sample 1 from the prompt: expected 3, and the algorithm returns 3.
        solution.demonstrate(sample1);

        // Sample 2 from the prompt:
        // The prompt says 5, but the true minimum adjacent-swap cost is 6.
        // We print the mathematically correct result.
        solution.demonstrate(sample2);

        // Additional edge cases for clarity.
        solution.demonstrate(new int[]{0, 0, 0, 0});     // No VIPs -> 0
        solution.demonstrate(new int[]{0, 1, 0, 0});     // One VIP -> 0
        solution.demonstrate(new int[]{1, 1, 1, 1});     // Already contiguous -> 0
        solution.demonstrate(new int[]{1, 0, 1, 0, 1});  // Expected 2
    }
}