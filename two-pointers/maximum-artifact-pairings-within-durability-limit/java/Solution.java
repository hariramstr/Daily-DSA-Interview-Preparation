import java.util.*;

/*
Problem Title: Maximum Artifact Pairings Within Durability Limit

Problem Description:
A museum is preparing fragile artifacts for transport. You are given an integer array durability
where durability[i] is the durability score of the i-th artifact, and an integer limit.
A shipping crate may contain either one artifact or exactly two artifacts. If two artifacts
with durabilities a and b are packed together, the pair is valid only if |a - b| <= limit.
Each artifact can be used in at most one crate.

Your task is to maximize the number of 2-artifact crates that can be formed.

Unlike simpler pairing problems, the artifacts are not pre-sorted, durability scores may repeat
many times, and the answer must be computed efficiently for very large inputs. Returning the
maximum number of valid pairs is enough; you do not need to output the actual pairing.

A pair uses two distinct indices. Single-artifact crates are allowed for leftovers, but they do
not contribute to the answer. The challenge is to decide which artifacts to match so that local
greedy choices do not reduce the total number of valid pairs.

Constraints:
- 1 <= durability.length <= 2 * 10^5
- 0 <= durability[i] <= 10^9
- 0 <= limit <= 10^9

Example 1:
Input: durability = [8, 1, 5, 6, 2, 9], limit = 1
Output: 2
Explanation: After sorting, one optimal pairing is (1,2) and (5,6). Artifact 8 and 9 cannot both
be paired because using one of them blocks a better total arrangement.

Example 2:
Input: durability = [4, 4, 4, 7, 8, 9, 9], limit = 0
Output: 2
Explanation: Only equal durabilities can be paired. We can form one pair from the three artifacts
with durability 4 and one pair from the two artifacts with durability 9. The artifact with
durability 7 and the artifact with durability 8 remain alone.
*/

public class Solution {

    /**
     * Computes the maximum number of valid 2-artifact crates.
     *
     * Core idea:
     * 1. Sort the durability values.
     * 2. In sorted order, if two neighboring unused artifacts can be paired, pairing them is always safe.
     *    Why? Because in a sorted array, if artifact i can pair with artifact i+1, then using them together
     *    never reduces the number of total pairs compared with skipping one of them for a later artifact.
     *    This is a standard greedy exchange argument for maximum matching on a path-like compatibility structure.
     * 3. Therefore, we scan from left to right and greedily pair adjacent artifacts whenever their difference
     *    is within the limit.
     *
     * Example:
     * Sorted [1, 2, 5, 6, 8, 9], limit = 1
     * - 1 and 2 differ by 1 -> pair them
     * - 5 and 6 differ by 1 -> pair them
     * - 8 and 9 differ by 1 -> pair them
     * So the true maximum is 3.
     *
     * @param durability the durability scores of all artifacts
     * @param limit the maximum allowed absolute difference for a valid pair
     * @return the maximum number of valid pairs
     * Time complexity: O(n log n) due to sorting, followed by O(n) scanning.
     * Space complexity: O(1) extra space beyond the sort implementation details.
     */
    public int maxArtifactPairings(int[] durability, int limit) {
        // Defensive handling for very small arrays:
        // if there are fewer than 2 artifacts, no pair can be formed.
        if (durability == null || durability.length < 2) {
            return 0;
        }

        // Sort the array so that close durability values become neighbors.
        // This is the key transformation that makes the greedy strategy correct and efficient.
        Arrays.sort(durability);

        // This will count how many valid 2-artifact crates we can form.
        int pairs = 0;

        // We scan with index i.
        // At each step, we consider artifact i and artifact i + 1.
        // If they can be paired, we use both and jump by 2.
        // Otherwise, artifact i cannot be profitably paired with any earlier item
        // (because earlier items are already processed), so we skip it and move by 1.
        int i = 0;

        while (i < durability.length - 1) {
            // Since the array is sorted, durability[i + 1] >= durability[i],
            // so absolute difference is simply durability[i + 1] - durability[i].
            if ((long) durability[i + 1] - durability[i] <= limit) {
                // These two adjacent artifacts form a valid pair.
                pairs++;

                // Move past both used artifacts.
                i += 2;
            } else {
                // They cannot be paired.
                // Because the array is sorted, artifact i also cannot pair with any later artifact
                // if it already fails with the closest possible later candidate i+1.
                // Any later artifact would differ by even more.
                // So artifact i must remain single, and we move on.
                i++;
            }
        }

        return pairs;
    }

    /**
     * Convenience wrapper that preserves the caller's original array by sorting a copy.
     *
     * This is useful in demonstrations or interview settings where mutating the input may be undesirable.
     *
     * @param durability the durability scores of all artifacts
     * @param limit the maximum allowed absolute difference for a valid pair
     * @return the maximum number of valid pairs
     * Time complexity: O(n log n)
     * Space complexity: O(n) because a copy of the array is created
     */
    public int maxArtifactPairingsWithoutMutatingInput(int[] durability, int limit) {
        if (durability == null) {
            return 0;
        }

        int[] copy = Arrays.copyOf(durability, durability.length);
        return maxArtifactPairings(copy, limit);
    }

    /**
     * Demonstrates the solution on the sample inputs and a few extra checks.
     *
     * @param args command-line arguments (not used)
     * @return nothing
     * Time complexity: O(n log n) per demonstration call
     * Space complexity: depends on whether the copied-input wrapper is used; here O(n) per call
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        int[] durability1 = {8, 1, 5, 6, 2, 9};
        int limit1 = 1;
        int result1 = solution.maxArtifactPairingsWithoutMutatingInput(durability1, limit1);
        System.out.println("Example 1 result: " + result1);

        int[] durability2 = {4, 4, 4, 7, 8, 9, 9};
        int limit2 = 0;
        int result2 = solution.maxArtifactPairingsWithoutMutatingInput(durability2, limit2);
        System.out.println("Example 2 result: " + result2);

        int[] durability3 = {1};
        int limit3 = 10;
        int result3 = solution.maxArtifactPairingsWithoutMutatingInput(durability3, limit3);
        System.out.println("Single artifact result: " + result3);

        int[] durability4 = {1, 10, 20, 30};
        int limit4 = 0;
        int result4 = solution.maxArtifactPairingsWithoutMutatingInput(durability4, limit4);
        System.out.println("No valid pairs result: " + result4);

        int[] durability5 = {3, 3, 3, 3, 3, 3};
        int limit5 = 0;
        int result5 = solution.maxArtifactPairingsWithoutMutatingInput(durability5, limit5);
        System.out.println("All equal result: " + result5);
    }
}