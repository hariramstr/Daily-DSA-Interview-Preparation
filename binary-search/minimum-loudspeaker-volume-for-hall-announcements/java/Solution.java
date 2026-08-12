import java.util.*;

/*
 * Title: Minimum Loudspeaker Volume for Hall Announcements
 * Difficulty: Medium
 * Topic: Binary Search
 *
 * Problem Description:
 * A convention center has a long hallway with event booths placed at known integer positions
 * along a straight line. You need to install loudspeakers at some of these booth positions so
 * that every booth can hear announcements. If a loudspeaker is set to volume radius R, it covers
 * every booth whose position is within distance R from that loudspeaker. You may install at most
 * k loudspeakers, and each loudspeaker must be placed at one of the given booth positions.
 *
 * Return the minimum integer radius R needed so that all booths are covered.
 *
 * This problem is designed for an efficient solution using binary search on the answer.
 * For a fixed radius R, determine whether it is possible to cover all booth positions using
 * at most k loudspeakers. The booth positions are not guaranteed to be sorted and may contain duplicates.
 *
 * Constraints:
 * - 1 <= n == positions.length <= 2 * 10^5
 * - 1 <= k <= n
 * - 0 <= positions[i] <= 10^9
 * - The answer fits in a 32-bit signed integer.
 *
 * Important correctness note about the examples:
 * The narrative in the prompt contains inconsistent placement explanations.
 * The algorithm implemented below solves the stated problem exactly:
 * loudspeakers must be placed only at given booth positions, and every booth must be within
 * distance R of at least one chosen loudspeaker.
 *
 * For example:
 * 1) positions = [1, 2, 8, 12, 17], k = 2
 *    The true minimum radius is 5:
 *    - With R = 4, no choice of 2 booth positions covers all booths.
 *    - With R = 5, placing speakers at 2 and 12 covers all booths.
 *
 * 2) positions = [4, 4, 4, 10, 15, 21], k = 3
 *    The true minimum radius is 5:
 *    - With R = 3, no valid placement at booth positions covers 21 together with 15 using one speaker.
 *    - With R = 5, placing speakers at 4, 10, and 21 covers all booths.
 *
 * Therefore, the sample outputs written in the prompt are not consistent with the formal rules.
 * This solution follows the formal rules and returns the mathematically correct answers.
 */

public class Solution {

    /**
     * Computes the minimum integer radius needed so that all booth positions are covered
     * using at most k loudspeakers placed only at given booth positions.
     *
     * The algorithm:
     * 1. Sort the booth positions.
     * 2. Binary search the answer R.
     * 3. For each candidate R, greedily check whether all booths can be covered with at most k speakers.
     *
     * @param positions the booth positions; may be unsorted and may contain duplicates
     * @param k the maximum number of loudspeakers allowed
     * @return the smallest integer radius that allows full coverage
     * Time complexity: O(n log n + n log M), where M is the coordinate range
     * Space complexity: O(n) due to sorting copy / internal arrays
     */
    public int minimumRadius(int[] positions, int k) {
        int[] sorted = positions.clone();
        Arrays.sort(sorted);

        int low = 0;
        int high = sorted[sorted.length - 1] - sorted[0];

        while (low < high) {
            int mid = low + (high - low) / 2;

            if (canCoverAll(sorted, k, mid)) {
                high = mid;
            } else {
                low = mid + 1;
            }
        }

        return low;
    }

    /**
     * Checks whether all sorted booth positions can be covered using at most k loudspeakers,
     * each with radius r, where each loudspeaker must be placed at one of the booth positions.
     *
     * Greedy idea:
     * - Start from the leftmost uncovered booth.
     * - Among booth positions that can still cover this leftmost uncovered booth, place the speaker
     *   as far to the right as possible. This is optimal because it maximizes how far right this
     *   speaker can cover while still covering the current leftmost uncovered booth.
     * - Then skip every booth covered by that speaker and repeat.
     *
     * Why this greedy works:
     * - Suppose the current leftmost uncovered booth is at x.
     * - Any valid speaker covering x must be placed at some booth position p with p <= x + r
     *   and also p >= x - r. Since x is the leftmost uncovered booth in sorted order, choosing the
     *   rightmost possible such booth position gives the largest right coverage endpoint p + r.
     * - Therefore, this choice never hurts and is optimal for the current step.
     *
     * @param sortedPositions booth positions sorted in nondecreasing order
     * @param k maximum number of loudspeakers allowed
     * @param r candidate radius to test
     * @return true if full coverage is possible with at most k loudspeakers, otherwise false
     * Time complexity: O(n)
     * Space complexity: O(1) extra space
     */
    public boolean canCoverAll(int[] sortedPositions, int k, int r) {
        int n = sortedPositions.length;

        // i points to the first booth that is not yet covered.
        int i = 0;

        // Count how many loudspeakers we have used so far.
        int used = 0;

        // Continue until every booth is covered or we exceed k speakers.
        while (i < n) {
            used++;

            // If we already used too many speakers, this radius is not feasible.
            if (used > k) {
                return false;
            }

            // Step 1:
            // Let x be the leftmost uncovered booth.
            int x = sortedPositions[i];

            // Step 2:
            // We want to place a speaker at the RIGHTMOST booth position that can still cover x.
            // A speaker at position p covers x if |p - x| <= r.
            // Since p is a booth position and sorted, and because all remaining positions are >= x,
            // it is enough to move right while sortedPositions[j] <= x + r.
            //
            // After this loop, j will be the index of the rightmost booth position where we can place
            // the speaker while still covering x.
            int j = i;
            while (j + 1 < n && sortedPositions[j + 1] <= x + r) {
                j++;
            }

            // Place the speaker at sortedPositions[j].
            int speakerPosition = sortedPositions[j];

            // Step 3:
            // This speaker covers every booth up to speakerPosition + r.
            int coverRight = speakerPosition + r;

            // Step 4:
            // Advance i past every booth that is covered by this speaker.
            while (i < n && sortedPositions[i] <= coverRight) {
                i++;
            }
        }

        return true;
    }

    /**
     * Runs a demonstration of the solution on several inputs, including the prompt's examples.
     *
     * Note:
     * The prompt's written sample outputs are inconsistent with the formal problem rules.
     * This main method prints the mathematically correct results produced by the algorithm.
     *
     * @param args command-line arguments (not used)
     * @return nothing
     * Time complexity: O(total input size log total input size) for the demonstrated examples
     * Space complexity: O(total input size) for copied/sorted arrays in the demonstrations
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        int[] positions1 = {1, 2, 8, 12, 17};
        int k1 = 2;
        int answer1 = solution.minimumRadius(positions1, k1);
        System.out.println("Example 1:");
        System.out.println("positions = " + Arrays.toString(positions1) + ", k = " + k1);
        System.out.println("Minimum radius = " + answer1);
        System.out.println();

        int[] positions2 = {4, 4, 4, 10, 15, 21};
        int k2 = 3;
        int answer2 = solution.minimumRadius(positions2, k2);
        System.out.println("Example 2:");
        System.out.println("positions = " + Arrays.toString(positions2) + ", k = " + k2);
        System.out.println("Minimum radius = " + answer2);
        System.out.println();

        int[] positions3 = {5};
        int k3 = 1;
        int answer3 = solution.minimumRadius(positions3, k3);
        System.out.println("Single booth:");
        System.out.println("positions = " + Arrays.toString(positions3) + ", k = " + k3);
        System.out.println("Minimum radius = " + answer3);
        System.out.println();

        int[] positions4 = {1, 100, 200, 300};
        int k4 = 4;
        int answer4 = solution.minimumRadius(positions4, k4);
        System.out.println("One speaker per booth allowed:");
        System.out.println("positions = " + Arrays.toString(positions4) + ", k = " + k4);
        System.out.println("Minimum radius = " + answer4);
        System.out.println();

        int[] positions5 = {1, 100, 200, 300};
        int k5 = 1;
        int answer5 = solution.minimumRadius(positions5, k5);
        System.out.println("Only one speaker allowed:");
        System.out.println("positions = " + Arrays.toString(positions5) + ", k = " + k5);
        System.out.println("Minimum radius = " + answer5);
    }
}