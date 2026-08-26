import java.util.*;

/*
Problem Title: Count Equivalent Playlist Rotations

Problem Description:
A music platform stores many playlists, where each playlist is represented as an array of song IDs.
Two playlists are considered equivalent if one can be obtained from the other by a circular rotation.
For example, [4, 7, 9, 4] and [9, 4, 4, 7] are equivalent because rotating the first playlist by
2 positions gives the second. However, [4, 7, 9] and [7, 9, 8] are not equivalent.

You are given a list of playlists, where each playlist may have a different length. Count how many
unordered pairs of playlists are equivalent under circular rotation. Playlists of different lengths
can never be equivalent.

Your task is to return the total number of equivalent pairs.

A straightforward pairwise comparison of all playlists is too slow for large inputs, so you should
design a solution that groups equivalent playlists efficiently using hashing or a canonical representation.

Constraints:
- 1 <= playlists.length <= 100000
- 1 <= total number of song IDs across all playlists <= 200000
- 1 <= playlists[i].length <= 200000
- 0 <= song IDs <= 1000000000
- The sum of all playlist lengths does not exceed 200000

Example 1:
Input: playlists = [[1,2,3],[2,3,1],[3,1,2],[1,3,2],[5],[5]]
Output: 4
Explanation: The first three playlists are all rotations of each other, contributing 3 pairs.
The two single-song playlists contribute 1 more pair. [1,3,2] is not equivalent to the others.

Example 2:
Input: playlists = [[8,8,1],[8,1,8],[1,8,8],[2,2],[2,2],[2],[3,4,3]]
Output: 4
Explanation: The first three playlists form 3 equivalent pairs. The two playlists [2,2] and [2,2]
form 1 pair. The playlist [2] has different length, and [3,4,3] does not match any other playlist.

Notes:
- Rotation preserves order cyclically; reversing a playlist does not count.
- Duplicate song IDs are allowed and must be handled correctly.
- An efficient approach is to compute a canonical signature for each playlist, such as its
  lexicographically smallest rotation, then count equal signatures with a hash map.
*/

public class Solution {

    /**
     * Counts how many unordered pairs of playlists are equivalent under circular rotation.
     *
     * The key idea is:
     * 1. Convert every playlist into a canonical form.
     * 2. Two playlists are equivalent if and only if their canonical forms are identical.
     * 3. Count how many times each canonical form appears.
     * 4. For a group of size k, the number of unordered pairs is k * (k - 1) / 2.
     *
     * To build the canonical form efficiently, we compute the lexicographically smallest rotation
     * of each playlist using Booth's algorithm in linear time.
     *
     * @param playlists the input list of playlists, where each playlist is an array of song IDs
     * @return the total number of unordered equivalent pairs
     * Time complexity: O(total number of song IDs across all playlists)
     * Space complexity: O(total number of song IDs across all playlists) for stored signatures
     */
    public long countEquivalentPairs(int[][] playlists) {
        Map<PlaylistSignature, Integer> frequency = new HashMap<>();
        long pairs = 0L;

        for (int[] playlist : playlists) {
            PlaylistSignature signature = canonicalSignature(playlist);

            int seenSoFar = frequency.getOrDefault(signature, 0);

            // If we have already seen this canonical playlist "seenSoFar" times,
            // then the current playlist forms exactly "seenSoFar" new unordered pairs
            // with all previously seen equivalent playlists.
            pairs += seenSoFar;

            frequency.put(signature, seenSoFar + 1);
        }

        return pairs;
    }

    /**
     * Computes a canonical signature for a playlist.
     *
     * The canonical signature is defined as the lexicographically smallest circular rotation
     * of the playlist. This guarantees:
     * - All playlists in the same rotation-equivalence class map to exactly the same signature.
     * - Playlists from different classes map to different signatures.
     *
     * Example:
     * playlist = [2, 3, 1]
     * rotations:
     *   [2, 3, 1]
     *   [3, 1, 2]
     *   [1, 2, 3]  <-- smallest
     * canonical signature = [1, 2, 3]
     *
     * @param playlist the playlist to normalize
     * @return a signature object representing the lexicographically smallest rotation
     * Time complexity: O(n), where n is the playlist length
     * Space complexity: O(n)
     */
    public PlaylistSignature canonicalSignature(int[] playlist) {
        int n = playlist.length;

        // For a single-element playlist, the only rotation is itself.
        if (n == 1) {
            return new PlaylistSignature(new int[]{playlist[0]});
        }

        // Find the starting index of the lexicographically smallest rotation.
        int start = smallestRotationIndex(playlist);

        // Build the normalized array by reading the playlist starting from "start"
        // and wrapping around using modulo arithmetic.
        int[] normalized = new int[n];
        for (int i = 0; i < n; i++) {
            normalized[i] = playlist[(start + i) % n];
        }

        return new PlaylistSignature(normalized);
    }

    /**
     * Finds the starting index of the lexicographically smallest rotation of an array
     * using Booth's algorithm.
     *
     * This algorithm works in linear time and correctly handles duplicate values.
     *
     * High-level idea:
     * - Imagine the array doubled: a + a
     * - Compare candidate starting positions i and j
     * - Skip ranges of positions that cannot possibly be the smallest rotation
     * - Continue until only the best candidate remains
     *
     * Why this is correct:
     * - If two candidate rotations differ at offset k, then the one with the larger value
     *   at that first differing position cannot be the smallest.
     * - Moreover, several nearby candidates can be eliminated at once, which is what gives
     *   the linear-time performance.
     *
     * @param arr the input playlist
     * @return the index where the lexicographically smallest rotation starts
     * Time complexity: O(n)
     * Space complexity: O(1) extra space
     */
    public int smallestRotationIndex(int[] arr) {
        int n = arr.length;

        int i = 0; // first candidate start
        int j = 1; // second candidate start
        int k = 0; // current offset while comparing rotations starting at i and j

        while (i < n && j < n && k < n) {
            int a = arr[(i + k) % n];
            int b = arr[(j + k) % n];

            if (a == b) {
                // The compared elements are equal, so continue comparing the next offset.
                k++;
            } else if (a > b) {
                // Rotation at i is lexicographically larger than rotation at j.
                // Therefore, i cannot be the answer, and neither can any position
                // between i and i + k inclusive.
                i = i + k + 1;

                // If both candidates collide, move j forward so they remain distinct.
                if (i == j) {
                    i++;
                }

                // Reset offset because we are comparing new candidate positions.
                k = 0;
            } else {
                // Rotation at j is lexicographically larger than rotation at i.
                // Therefore, j cannot be the answer, and neither can any position
                // between j and j + k inclusive.
                j = j + k + 1;

                // If both candidates collide, move j forward so they remain distinct.
                if (i == j) {
                    j++;
                }

                // Reset offset because we are comparing new candidate positions.
                k = 0;
            }
        }

        return Math.min(i, j) % n;
    }

    /**
     * Demonstrates the solution on the sample inputs from the problem statement.
     *
     * @param args command-line arguments (not used)
     * @return nothing
     * Time complexity: O(total input size of the demo data)
     * Space complexity: O(total input size of the demo data)
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        int[][] playlists1 = {
                {1, 2, 3},
                {2, 3, 1},
                {3, 1, 2},
                {1, 3, 2},
                {5},
                {5}
        };

        int[][] playlists2 = {
                {8, 8, 1},
                {8, 1, 8},
                {1, 8, 8},
                {2, 2},
                {2, 2},
                {2},
                {3, 4, 3}
        };

        long result1 = solution.countEquivalentPairs(playlists1);
        long result2 = solution.countEquivalentPairs(playlists2);

        System.out.println(result1); // Expected: 4
        System.out.println(result2); // Expected: 4
    }

    /**
     * Immutable wrapper used as a hash-map key for canonical playlists.
     *
     * We store the normalized rotation array and define equals/hashCode based on
     * the full sequence of values. This allows Java's HashMap to group equivalent
     * playlists correctly.
     */
    public static final class PlaylistSignature {
        private final int[] values;
        private final int hash;

        /**
         * Creates a signature from a normalized playlist array.
         *
         * @param values the canonical rotation values
         * @return nothing
         * Time complexity: O(n)
         * Space complexity: O(1) extra space beyond the stored array reference
         */
        public PlaylistSignature(int[] values) {
            this.values = values;
            this.hash = Arrays.hashCode(values);
        }

        /**
         * Compares this signature with another object for equality.
         *
         * @param obj the other object
         * @return true if both signatures contain exactly the same sequence, otherwise false
         * Time complexity: O(n) in the worst case
         * Space complexity: O(1)
         */
        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof PlaylistSignature)) {
                return false;
            }
            PlaylistSignature other = (PlaylistSignature) obj;
            return Arrays.equals(this.values, other.values);
        }

        /**
         * Returns the precomputed hash code of this signature.
         *
         * @param none no parameters
         * @return the hash code
         * Time complexity: O(1)
         * Space complexity: O(1)
         */
        @Override
        public int hashCode() {
            return hash;
        }
    }
}