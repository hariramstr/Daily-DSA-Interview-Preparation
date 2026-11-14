import java.util.*;

/*
 * Title: Minimum Cost to Archive Photos with Album Switching Fee
 * Difficulty: Medium
 * Topic: Dynamic Programming
 *
 * Problem Description:
 * A cloud photo service needs to archive a sequence of photos in the exact order they were taken.
 * Each photo belongs to one of m visual categories, numbered from 1 to m.
 * You must place every photo into one of two archive albums, Album A or Album B, while preserving
 * the original order inside each album.
 *
 * When a photo of category x is appended to an album, the service pays a storage adjustment cost
 * based on the previous category already stored in that same album. If the album is empty, the cost is 0.
 * Otherwise, if the previous category in that album was y, the cost is switchCost[y][x].
 *
 * Your task is to assign every photo to either Album A or Album B so that the total cost is minimized.
 *
 * More formally, you are given an array photos of length n, where photos[i] is the category of the i-th photo,
 * and an m x m matrix switchCost. Process photos from left to right. For each photo, choose Album A or Album B.
 * The relative order of photos assigned to the same album must remain the same as in the original sequence.
 * Return the minimum possible total adjustment cost.
 *
 * Constraints:
 * - 1 <= n <= 2000
 * - 1 <= m <= 50
 * - 1 <= photos[i] <= m
 * - 0 <= switchCost[a][b] <= 10^6 for all 1 <= a, b <= m
 * - The answer fits in a 64-bit signed integer
 *
 * Key Dynamic Programming Insight:
 * We process photos from left to right.
 * After processing some prefix, the only information that matters for future cost is:
 * 1) the category of the last photo placed into one album
 * 2) the category of the last photo placed into the other album
 *
 * However, we can compress the state even further:
 * Let the current photo (the most recently processed one) always be considered the last photo
 * of one album. Then we only need to remember the last photo index of the other album.
 *
 * DP state:
 * dp[j] after processing photo i means:
 * - photo i is the last photo in one album
 * - photo j is the last photo in the other album
 * - j = -1 (represented by index n in implementation) means the other album is empty
 *
 * Transition for next photo k = i + 1:
 * 1) Put photo k into the same album that currently ends with photo i:
 *    new state keeps j unchanged
 *    cost += switchCost[photos[i]][photos[k]]
 *
 * 2) Put photo k into the other album that currently ends with photo j:
 *    then the "current-photo album" still ends at i, and the other album now ends at k.
 *    To preserve our state convention that the newest processed photo is the distinguished one,
 *    we store the new state as dpNext[i].
 *    Added cost is:
 *      - 0 if j = -1 (other album was empty)
 *      - switchCost[photos[j]][photos[k]] otherwise
 *
 * This yields O(n^2) states and O(1) work per transition, which is efficient for n <= 2000.
 */
public class Solution {

    /**
     * Computes the minimum total adjustment cost to assign all photos into two albums.
     *
     * The method uses dynamic programming over prefixes of the photo sequence.
     * We process photos from left to right and maintain a compressed state:
     * after processing photo i, we remember the last photo index of the "other" album.
     *
     * Important implementation detail:
     * - photos values are 1-based categories from the problem statement.
     * - switchCost is a standard Java 0-based matrix.
     * - Therefore, whenever we access switchCost for category c, we use c - 1.
     *
     * @param photos the categories of the photos in original order; each value is in [1, m]
     * @param switchCost the directional cost matrix where switchCost[a-1][b-1] is the cost of appending category b after category a
     * @return the minimum possible total adjustment cost as a long
     * Time complexity: O(n^2), because there are O(n) layers and each layer scans O(n) states.
     * Space complexity: O(n), because we only keep the current and next DP arrays.
     */
    public long minimumArchiveCost(int[] photos, int[][] switchCost) {
        int n = photos.length;

        // If there are no photos or only one photo, no album ever pays a transition cost.
        if (n <= 1) {
            return 0L;
        }

        // We use a large sentinel value for "infinity".
        // Long.MAX_VALUE itself is risky because adding to it can overflow,
        // so we choose a safely smaller large number.
        long INF = Long.MAX_VALUE / 4;

        // Special index representing "the other album is empty".
        // Since valid photo indices are 0..n-1, index n is available as a sentinel.
        int EMPTY = n;

        // dp[j] means:
        // After processing current photo i,
        // - one album ends with photo i
        // - the other album ends with photo j
        // - if j == EMPTY, the other album is empty
        long[] dp = new long[n + 1];
        Arrays.fill(dp, INF);

        // Base case:
        // After processing only photo 0, we can place it into one album,
        // and the other album is empty. Cost is 0.
        dp[EMPTY] = 0L;

        // Process next photos one by one.
        // At the start of each iteration, dp describes states after processing photo i.
        for (int i = 0; i < n - 1; i++) {
            long[] next = new long[n + 1];
            Arrays.fill(next, INF);

            // Category of the current distinguished last photo.
            int currentCategory = photos[i];

            // We try every possible "other album last index" j.
            for (int j = 0; j <= n; j++) {
                long currentCost = dp[j];

                // Skip unreachable states.
                if (currentCost >= INF) {
                    continue;
                }

                // The next photo to place.
                int k = i + 1;
                int nextCategory = photos[k];

                // ------------------------------------------------------------
                // Transition 1:
                // Put photo k into the same album that currently ends with photo i.
                //
                // Then:
                // - that album now ends with k
                // - the other album still ends with j
                //
                // Additional cost:
                // Since this album already ends with photo i, appending photo k costs
                // switchCost[photos[i]][photos[k]].
                // ------------------------------------------------------------
                long addToCurrentAlbum = switchCost[currentCategory - 1][nextCategory - 1];
                long candidate1 = currentCost + addToCurrentAlbum;
                if (candidate1 < next[j]) {
                    next[j] = candidate1;
                }

                // ------------------------------------------------------------
                // Transition 2:
                // Put photo k into the other album that currently ends with j.
                //
                // If j == EMPTY, that album is empty, so appending costs 0.
                // Otherwise, appending costs switchCost[photos[j]][photos[k]].
                //
                // After placing k there:
                // - one album ends with previous distinguished photo i
                // - the other album ends with k
                //
                // But our DP convention for the next layer is:
                // "the newly processed photo k is the distinguished album end"
                // and we store the other album's end index.
                //
                // Therefore the new state is next[i].
                // ------------------------------------------------------------
                long addToOtherAlbum = 0L;
                if (j != EMPTY) {
                    addToOtherAlbum = switchCost[photos[j] - 1][nextCategory - 1];
                }

                long candidate2 = currentCost + addToOtherAlbum;
                if (candidate2 < next[i]) {
                    next[i] = candidate2;
                }
            }

            // Move to the next layer.
            dp = next;
        }

        // After processing all photos, the answer is the minimum over all possible
        // last indices of the other album.
        long answer = INF;
        for (long value : dp) {
            answer = Math.min(answer, value);
        }
        return answer;
    }

    /**
     * Convenience wrapper that accepts a List of photo categories.
     *
     * This is useful for beginners who may prefer working with Java collections
     * before converting to primitive arrays.
     *
     * @param photosList the photo categories as a list
     * @param switchCost the directional cost matrix
     * @return the minimum possible total adjustment cost
     * Time complexity: O(n^2), same as the array-based method.
     * Space complexity: O(n), same as the array-based method, plus O(n) for list-to-array conversion.
     */
    public long minimumArchiveCost(List<Integer> photosList, int[][] switchCost) {
        int[] photos = new int[photosList.size()];
        for (int i = 0; i < photosList.size(); i++) {
            photos[i] = photosList.get(i);
        }
        return minimumArchiveCost(photos, switchCost);
    }

    /**
     * Demonstrates the solution on the sample inputs from the problem statement.
     *
     * Expected outputs:
     * Example 1 -> 0
     * Example 2 -> 1
     *
     * @param args command-line arguments (not used)
     * @return nothing
     * Time complexity: O(1) for the demonstration itself, excluding the called algorithm runs.
     * Space complexity: O(1), excluding the called algorithm's internal memory usage.
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Example 1
        int[] photos1 = {1, 2, 1};
        int[][] switchCost1 = {
            {0, 5},
            {4, 0}
        };
        long result1 = solution.minimumArchiveCost(photos1, switchCost1);
        System.out.println(result1); // Expected: 0

        // Example 2
        int[] photos2 = {1, 3, 2, 3};
        int[][] switchCost2 = {
            {0, 7, 2},
            {6, 0, 5},
            {4, 1, 0}
        };
        long result2 = solution.minimumArchiveCost(photos2, switchCost2);
        System.out.println(result2); // Expected: 1
    }
}