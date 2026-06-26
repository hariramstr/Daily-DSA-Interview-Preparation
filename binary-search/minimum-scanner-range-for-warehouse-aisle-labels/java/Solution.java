import java.util.*;

/*
 * Title: Minimum Scanner Range for Warehouse Aisle Labels
 * Difficulty: Medium
 * Topic: Binary Search
 *
 * Problem Description:
 * A warehouse is organized as a long straight line of aisles, numbered by their distance from the entrance.
 * Some aisles contain fixed barcode scanners, and some aisles contain labels that must be readable by at least one scanner.
 *
 * You are given two integer arrays: scanners and labels. scanners[i] is the position of the i-th scanner,
 * and labels[j] is the position of the j-th label. A scanner with range r can read every label whose position
 * is within distance r from that scanner. Your task is to find the minimum integer range r such that every
 * label is readable by at least one scanner.
 *
 * Positions may be unsorted and may include duplicates. A scanner and a label can appear at the same position.
 * You must return the smallest possible range that covers all labels.
 *
 * A common interview approach is to sort the positions and use binary search on the answer.
 * For a candidate range r, determine whether all labels can be covered efficiently.
 *
 * Constraints:
 * - 1 <= scanners.length, labels.length <= 2 * 10^5
 * - 0 <= scanners[i], labels[j] <= 10^9
 * - The answer fits in a 32-bit signed integer.
 *
 * Example 1:
 * Input: scanners = [2, 10], labels = [1, 5, 11]
 * Output: 3
 * Explanation: With range 3, scanner 2 covers labels at 1 and 5, and scanner 10 covers label 11.
 * Range 2 is not enough because label 5 would be uncovered.
 *
 * Example 2:
 * Input: scanners = [15, 4, 20], labels = [3, 8, 14, 21]
 * Output: 4
 * Explanation: After sorting, the nearest scanner distances for the labels are 1, 4, 1, and 1.
 * Therefore the minimum range that covers every label is 4.
 */

public class Solution {

    /**
     * Finds the minimum integer scanner range needed so that every label is covered by at least one scanner.
     *
     * Core idea:
     * 1. Sort scanner positions and label positions.
     * 2. Binary search the answer r.
     * 3. For each candidate r, greedily verify whether all labels are covered.
     *
     * Why binary search works:
     * - If a range r is sufficient to cover all labels, then any larger range is also sufficient.
     * - This monotonic property makes binary search valid.
     *
     * @param scanners positions of fixed scanners
     * @param labels positions of labels that must be readable
     * @return the smallest integer range that allows all labels to be covered
     * Time complexity: O((n log n) + (m log m) + (n + m) log U), where U is the search range of answers
     * Space complexity: O(1) extra space beyond sorting implementation details
     */
    public int minimumScannerRange(int[] scanners, int[] labels) {
        // Sort both arrays so we can process positions from left to right.
        // This is essential for the efficient linear feasibility check.
        Arrays.sort(scanners);
        Arrays.sort(labels);

        // The minimum possible answer is 0:
        // if every label already sits on some scanner position, no range is needed.
        int left = 0;

        // A safe upper bound is the maximum possible distance between any label and any scanner.
        // Since positions are within [0, 1e9], 1e9 is always sufficient.
        int right = 1_000_000_000;

        // Standard binary search for the first feasible value.
        while (left < right) {
            // Midpoint chosen this way to avoid overflow.
            int mid = left + (right - left) / 2;

            // If range mid can cover all labels, try to find a smaller valid range.
            if (canCoverAllLabels(scanners, labels, mid)) {
                right = mid;
            } else {
                // Otherwise, we must increase the range.
                left = mid + 1;
            }
        }

        // At loop end, left == right and points to the smallest feasible range.
        return left;
    }

    /**
     * Checks whether a given scanner range is sufficient to cover every label.
     *
     * Detailed greedy strategy:
     * - Both arrays are sorted.
     * - We walk through labels from left to right.
     * - For each scanner, its coverage interval is [scanner - range, scanner + range].
     * - We advance the label pointer while labels fall inside the current scanner's coverage.
     * - If after processing all scanners every label has been consumed, then the range works.
     *
     * Why this is correct:
     * - Because arrays are sorted, once a label is to the left of a scanner's coverage, no future scanner
     *   can move backward in position order to help with earlier labels in a better way than this greedy scan.
     * - Greedily covering as many current labels as possible with each scanner is optimal in this linear setting.
     *
     * @param scanners sorted or unsorted scanner positions
     * @param labels sorted or unsorted label positions
     * @param range candidate scanner range to test
     * @return true if every label is covered by at least one scanner; false otherwise
     * Time complexity: O(n + m), where n is scanners.length and m is labels.length
     * Space complexity: O(1)
     */
    public boolean canCoverAllLabels(int[] scanners, int[] labels, int range) {
        // Pointer to the first label not yet confirmed as covered.
        int labelIndex = 0;
        int totalLabels = labels.length;

        // Process scanners from left to right.
        for (int scannerPos : scanners) {
            // Compute this scanner's coverage interval carefully using long arithmetic.
            // Even though values fit in int, using long avoids accidental overflow when subtracting/adding.
            long leftReach = (long) scannerPos - range;
            long rightReach = (long) scannerPos + range;

            // Step 1:
            // If there are labels strictly to the left of this scanner's coverage interval,
            // then those labels cannot be covered by this scanner.
            //
            // Could they be covered by a future scanner?
            // No, because future scanners are at the same or larger positions, so their left reach
            // will not help with an already-too-far-left uncovered label in a better way.
            //
            // Therefore, if the current first uncovered label is left of leftReach, the candidate range fails.
            if (labelIndex < totalLabels && labels[labelIndex] < leftReach) {
                return false;
            }

            // Step 2:
            // Consume every label that lies inside this scanner's coverage interval.
            // Since labels are sorted, once a label is beyond rightReach, later labels will also be beyond it.
            while (labelIndex < totalLabels && labels[labelIndex] <= rightReach) {
                labelIndex++;
            }

            // Small optimization:
            // If all labels are already covered, we can stop early.
            if (labelIndex == totalLabels) {
                return true;
            }
        }

        // After all scanners are processed, success happens only if every label was covered.
        return labelIndex == totalLabels;
    }

    /**
     * Alternative direct solution without binary search.
     *
     * This method computes, for each label, the distance to its nearest scanner,
     * then returns the maximum of those nearest distances.
     *
     * It is included for educational value and verification.
     *
     * @param scanners positions of fixed scanners
     * @param labels positions of labels that must be readable
     * @return the smallest integer range that allows all labels to be covered
     * Time complexity: O((n log n) + (m log m))
     * Space complexity: O(1) extra space beyond sorting implementation details
     */
    public int minimumScannerRangeDirect(int[] scanners, int[] labels) {
        Arrays.sort(scanners);
        Arrays.sort(labels);

        int scannerIndex = 0;
        int answer = 0;

        for (int labelPos : labels) {
            // Move scannerIndex forward while the next scanner is at least as close to the current label
            // as the current scanner. This keeps scannerIndex near the nearest scanner for this label.
            while (scannerIndex + 1 < scanners.length &&
                    Math.abs((long) scanners[scannerIndex + 1] - labelPos) <= Math.abs((long) scanners[scannerIndex] - labelPos)) {
                scannerIndex++;
            }

            int nearestDistance = (int) Math.abs((long) scanners[scannerIndex] - labelPos);
            answer = Math.max(answer, nearestDistance);
        }

        return answer;
    }

    /**
     * Demonstrates the solution on the sample inputs from the problem statement.
     *
     * @param args command-line arguments (not used)
     * @return nothing
     * Time complexity: O(1) for the fixed demo inputs, excluding algorithm calls
     * Space complexity: O(1), excluding input arrays
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        int[] scanners1 = {2, 10};
        int[] labels1 = {1, 5, 11};
        int result1 = solution.minimumScannerRange(scanners1.clone(), labels1.clone());
        System.out.println("Example 1 Result: " + result1);
        // Expected: 3

        int[] scanners2 = {15, 4, 20};
        int[] labels2 = {3, 8, 14, 21};
        int result2 = solution.minimumScannerRange(scanners2.clone(), labels2.clone());
        System.out.println("Example 2 Result: " + result2);
        // Expected: 4

        // Optional verification using the direct method.
        int verify1 = solution.minimumScannerRangeDirect(scanners1.clone(), labels1.clone());
        int verify2 = solution.minimumScannerRangeDirect(scanners2.clone(), labels2.clone());

        System.out.println("Example 1 Direct Verification: " + verify1);
        System.out.println("Example 2 Direct Verification: " + verify2);
    }
}