import java.util.*;

/*
 * Title: Minimum Gap to Place Festival Stages
 * Difficulty: Hard
 * Topic: Binary Search
 *
 * Problem Description:
 * You are organizing a large outdoor festival along a straight road. There are n approved
 * installation points, given as a sorted or unsorted array positions, where positions[i]
 * is the coordinate of the i-th point. You must place exactly k stages at distinct
 * installation points.
 *
 * For safety and crowd control, the festival authority defines the gap of a placement as
 * the minimum distance between any two chosen stages. A placement is considered valid if
 * every pair of neighboring chosen stages is at least that gap apart. Your task is to
 * compute the largest possible gap that can be guaranteed while still placing all k stages.
 *
 * Return the maximum integer value g such that it is possible to choose exactly k
 * installation points and the distance between every two consecutive chosen points is at
 * least g.
 *
 * This problem is designed to reward an efficient solution. A brute-force search over all
 * subsets of size k is far too slow. The key observation is that if a gap g is feasible,
 * then every smaller gap is also feasible, which makes the answer searchable with binary search.
 *
 * Constraints:
 * - 2 <= n <= 2 * 10^5
 * - 2 <= k <= n
 * - 0 <= positions[i] <= 10^9
 * - All installation points are distinct integers.
 *
 * Example 1:
 * Input: positions = [1, 2, 8, 12, 17], k = 3
 * Output: 7
 * Explanation: Choose stages at positions 1, 8, and 17. The pairwise neighboring gaps are
 * 7 and 9, so the minimum gap is 7. No arrangement of 3 stages can achieve a larger minimum gap.
 *
 * Example 2:
 * Input: positions = [4, 15, 7, 20, 1, 11], k = 4
 * Output: 4
 * Explanation: After sorting, positions become [1, 4, 7, 11, 15, 20]. One optimal choice is
 * 1, 7, 11, and 15, whose neighboring gaps are 6, 4, and 4. Therefore the maximum achievable
 * minimum gap is 4.
 */

public class Solution {

    /**
     * Computes the largest possible minimum gap between any two consecutive chosen stage positions.
     *
     * The algorithm works in two major phases:
     * 1. Sort the installation points so we can reason about distances from left to right.
     * 2. Binary search the answer g (the minimum required gap).
     *
     * Why binary search works:
     * - If we can place k stages with gap g, then we can also place them with any smaller gap.
     * - This creates a monotonic true/false pattern over possible gap values.
     *
     * @param positions array of distinct installation point coordinates; may be sorted or unsorted
     * @param k exact number of stages that must be placed
     * @return the maximum integer gap g such that k stages can be placed with every neighboring pair at least g apart
     *
     * Time complexity: O(n log n + n log R), where R = max(positions) - min(positions)
     * Space complexity: O(1) extra space beyond the sorting implementation details used by Java
     */
    public int maximumGap(int[] positions, int k) {
        // Step 1: Sort the positions.
        // This is essential because the greedy feasibility check relies on scanning
        // from left to right and always taking the earliest valid next position.
        Arrays.sort(positions);

        // Step 2: Define the binary search range for the answer.
        //
        // The smallest possible minimum gap is 0 in a general mathematical sense,
        // but since all positions are distinct integers and k >= 2, the practical
        // answer will be at least 1. Still, using 0 is safe and simple.
        int left = 0;

        // The largest possible minimum gap cannot exceed the distance between the
        // smallest and largest installation points.
        int right = positions[positions.length - 1] - positions[0];

        // We will store the best feasible gap found so far.
        int answer = 0;

        // Standard binary search on the answer space.
        while (left <= right) {
            // Midpoint gap candidate.
            // Using this form avoids overflow, although overflow is not a real issue here
            // because coordinates are within 1e9. Still, this is the standard safe pattern.
            int mid = left + (right - left) / 2;

            // Check whether it is possible to place exactly k stages such that
            // every neighboring chosen pair is at least 'mid' apart.
            if (canPlaceStages(positions, k, mid)) {
                // If feasible, this gap is a valid candidate.
                answer = mid;

                // Since we want the maximum feasible gap, try larger values.
                left = mid + 1;
            } else {
                // If not feasible, this gap is too large.
                // We must search smaller values.
                right = mid - 1;
            }
        }

        return answer;
    }

    /**
     * Determines whether it is possible to place at least k stages such that the distance
     * between every two consecutive chosen positions is at least minGap.
     *
     * Greedy strategy:
     * - Always place the first stage at the leftmost position.
     * - Then keep placing each next stage at the earliest position that is at least minGap
     *   away from the last chosen one.
     *
     * Why this greedy strategy is correct:
     * - Choosing the earliest valid next position leaves as much room as possible for the
     *   remaining stages on the right.
     * - Therefore, if this greedy process cannot place k stages, no other placement can.
     *
     * @param positions sorted array of distinct installation point coordinates
     * @param k exact number of stages required
     * @param minGap candidate minimum gap to test
     * @return true if at least k stages can be placed with neighboring distance at least minGap; false otherwise
     *
     * Time complexity: O(n)
     * Space complexity: O(1)
     */
    public boolean canPlaceStages(int[] positions, int k, int minGap) {
        // We always place the first stage at the first (leftmost) position.
        int placed = 1;
        int lastPlacedPosition = positions[0];

        // Scan through the remaining positions from left to right.
        for (int i = 1; i < positions.length; i++) {
            // If the current position is far enough from the last chosen position,
            // we can place another stage here.
            if (positions[i] - lastPlacedPosition >= minGap) {
                placed++;
                lastPlacedPosition = positions[i];

                // As soon as we have placed k stages, the candidate gap is feasible.
                if (placed >= k) {
                    return true;
                }
            }
        }

        // If we finish scanning and still placed fewer than k stages,
        // then this minimum gap is not feasible.
        return false;
    }

    /**
     * Demonstrates the solution on the sample inputs from the problem statement.
     *
     * @param args command-line arguments (not used)
     * @return nothing
     *
     * Time complexity: O(n log n + n log R) per demonstration call
     * Space complexity: O(1) extra space beyond sorting implementation details
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Example 1
        int[] positions1 = {1, 2, 8, 12, 17};
        int k1 = 3;
        int result1 = solution.maximumGap(positions1, k1);
        System.out.println("Example 1 Output: " + result1); // Expected: 7

        // Example 2
        int[] positions2 = {4, 15, 7, 20, 1, 11};
        int k2 = 4;
        int result2 = solution.maximumGap(positions2, k2);
        System.out.println("Example 2 Output: " + result2); // Expected: 4

        // Additional quick sanity check
        int[] positions3 = {0, 5, 10, 15};
        int k3 = 2;
        int result3 = solution.maximumGap(positions3, k3);
        System.out.println("Additional Example Output: " + result3); // Expected: 15
    }
}