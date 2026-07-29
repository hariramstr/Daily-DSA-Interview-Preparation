import java.util.*;

/*
Problem Title: Minimum Cost to Assemble a Playlist with Genre Switch Fees

Problem Description:
A music app wants to build a playlist of exactly k songs from a catalog shown in a fixed order.
For each song i, you are given its duration duration[i], its enjoyment score enjoy[i], and its genre genre[i].
You may either skip a song or include it in the playlist, but the relative order of chosen songs must remain
the same as in the catalog.

The total cost of a playlist is defined as follows:
1. For every selected song, you pay its duration.
2. For every pair of consecutive selected songs with different genres, you pay an additional switch fee s.
3. You receive a discount equal to the sum of enjoyment scores of all selected songs.

So the final playlist cost is:
(sum of durations) + (number of genre changes between consecutive chosen songs) * s - (sum of enjoyment scores)

Your task is to return the minimum possible total cost of selecting exactly k songs.
If it is impossible to choose exactly k songs, return -1.

Constraints:
- 1 <= n <= 200
- 1 <= k <= n
- 1 <= duration[i] <= 10^4
- 0 <= enjoy[i] <= 10^4
- 1 <= genre[i] <= 50
- 0 <= s <= 10^4

Important note about the examples:
The narrative examples in the prompt contain arithmetic inconsistencies.
This implementation follows the formula exactly as stated in the problem:
total cost = sum(duration) + switches * s - sum(enjoy)

For example 1:
duration = [4, 6, 3, 5]
enjoy    = [2, 5, 1, 4]
genre    = [1, 2, 2, 1]
k = 2, s = 3

Net base costs duration - enjoy are [2, 1, 2, 1].
All pairs:
(1,2): 2 + 1 + 3 = 6
(1,3): 2 + 2 + 3 = 7
(1,4): 2 + 1 + 0 = 3
(2,3): 1 + 2 + 0 = 3
(2,4): 1 + 1 + 3 = 5
(3,4): 2 + 1 + 3 = 6
Minimum = 3

For example 2:
duration = [7, 2, 6, 3, 4]
enjoy    = [1, 3, 2, 5, 1]
genre    = [1, 1, 3, 3, 2]
k = 3, s = 4

One optimal choice is songs (2,3,4):
base = (2-3) + (6-2) + (3-5) = -1 + 4 - 2 = 1
switches = 1 (genre 1 -> 3, then 3 -> 3 no switch)
total = 1 + 4 = 5
Minimum = 5

Therefore, a correct implementation must return 3 for example 1 and 5 for example 2.
*/

public class Solution {

    /**
     * A large value used to represent "impossible" states in dynamic programming.
     * We use long to safely handle sums and avoid integer overflow.
     */
    private static final long INF = Long.MAX_VALUE / 4;

    /**
     * Computes the minimum possible total cost of selecting exactly k songs while preserving order.
     *
     * Core idea:
     * We process songs from left to right. For each possible number of chosen songs, we track the
     * minimum cost of ending with each possible last genre.
     *
     * Let:
     * dp[c][g] = minimum cost after processing some prefix of songs, having chosen exactly c songs,
     *            and the last chosen song has genre g.
     *
     * Transition when considering a new song i with:
     * - base contribution = duration[i] - enjoy[i]
     * - genre = currentGenre
     *
     * If this song becomes the first chosen song:
     *   new cost = base contribution
     *
     * If this song is appended after a previously chosen song whose last genre is prevGenre:
     *   new cost = previous cost + base contribution + (prevGenre == currentGenre ? 0 : s)
     *
     * Since genre values are in [1, 50], we can keep DP over a small genre dimension.
     *
     * @param duration array where duration[i] is the duration cost of song i
     * @param enjoy array where enjoy[i] is the enjoyment discount of song i
     * @param genre array where genre[i] is the genre of song i
     * @param k exact number of songs that must be selected
     * @param s switch fee paid whenever two consecutive selected songs have different genres
     * @return the minimum total cost to select exactly k songs; returns -1 if impossible
     *
     * Time complexity: O(n * k * G * G), where G = 50. Since G is small and fixed, this is effectively O(n * k).
     * Space complexity: O(k * G)
     */
    public long minimumPlaylistCost(int[] duration, int[] enjoy, int[] genre, int k, int s) {
        if (duration == null || enjoy == null || genre == null) {
            return -1;
        }

        int n = duration.length;
        if (enjoy.length != n || genre.length != n || k < 0 || k > n) {
            return -1;
        }

        // Maximum genre value from constraints.
        int maxGenre = 50;

        // dp[c][g] = minimum cost for choosing exactly c songs, ending with genre g.
        long[][] dp = new long[k + 1][maxGenre + 1];

        // Initialize all states as impossible.
        for (int c = 0; c <= k; c++) {
            Arrays.fill(dp[c], INF);
        }

        // We process songs one by one.
        for (int i = 0; i < n; i++) {
            int currentGenre = genre[i];
            long baseCost = (long) duration[i] - enjoy[i];

            // next starts as a copy of dp because we are allowed to skip the current song.
            long[][] next = new long[k + 1][maxGenre + 1];
            for (int c = 0; c <= k; c++) {
                System.arraycopy(dp[c], 0, next[c], 0, maxGenre + 1);
            }

            // Case 1: choose current song as the first selected song.
            // Then we have chosen exactly 1 song, and the last genre is currentGenre.
            next[1][currentGenre] = Math.min(next[1][currentGenre], baseCost);

            // Case 2: append current song after an existing valid playlist of size chosen.
            // We iterate backwards on chosen count to avoid reusing the same song multiple times
            // within the same iteration if we were updating in place. Here we use "next", so either
            // direction would work, but backward iteration still clearly reflects 0/1 choice logic.
            for (int chosen = 1; chosen < k; chosen++) {
                for (int prevGenre = 1; prevGenre <= maxGenre; prevGenre++) {
                    long previousCost = dp[chosen][prevGenre];
                    if (previousCost == INF) {
                        continue;
                    }

                    long switchCost = (prevGenre == currentGenre) ? 0L : s;
                    long candidate = previousCost + baseCost + switchCost;

                    if (candidate < next[chosen + 1][currentGenre]) {
                        next[chosen + 1][currentGenre] = candidate;
                    }
                }
            }

            // Move to the next song.
            dp = next;
        }

        // The answer is the best cost among all possible ending genres after choosing exactly k songs.
        long answer = INF;
        for (int g = 1; g <= 50; g++) {
            answer = Math.min(answer, dp[k][g]);
        }

        return answer == INF ? -1 : answer;
    }

    /**
     * Convenience wrapper that returns the answer as an int when it fits typical interview-style output.
     *
     * @param duration array where duration[i] is the duration cost of song i
     * @param enjoy array where enjoy[i] is the enjoyment discount of song i
     * @param genre array where genre[i] is the genre of song i
     * @param k exact number of songs that must be selected
     * @param s switch fee paid whenever two consecutive selected songs have different genres
     * @return the minimum total cost as an int, or -1 if impossible
     *
     * Time complexity: O(n * k * G * G), where G = 50
     * Space complexity: O(k * G)
     */
    public int minimumCost(int[] duration, int[] enjoy, int[] genre, int k, int s) {
        long result = minimumPlaylistCost(duration, enjoy, genre, k, s);
        if (result == -1) {
            return -1;
        }
        return (int) result;
    }

    /**
     * Brute-force verifier for small inputs.
     * This is useful for demonstration and sanity checking in the main method.
     *
     * It enumerates all subsets of size k, preserves original order automatically,
     * and computes the exact cost directly from the definition.
     *
     * @param duration array of song durations
     * @param enjoy array of song enjoyment values
     * @param genre array of song genres
     * @param k exact number of songs to choose
     * @param s genre switch fee
     * @return exact minimum cost by brute force, or -1 if impossible
     *
     * Time complexity: O(2^n * n), suitable only for small n
     * Space complexity: O(1) excluding recursion/iteration overhead
     */
    public long bruteForceMinimumCost(int[] duration, int[] enjoy, int[] genre, int k, int s) {
        int n = duration.length;
        if (k > n) {
            return -1;
        }

        long best = INF;

        int totalMasks = 1 << n;
        for (int mask = 0; mask < totalMasks; mask++) {
            if (Integer.bitCount(mask) != k) {
                continue;
            }

            long cost = 0;
            int lastGenre = -1;

            for (int i = 0; i < n; i++) {
                if ((mask & (1 << i)) == 0) {
                    continue;
                }

                cost += duration[i];
                cost -= enjoy[i];

                if (lastGenre != -1 && lastGenre != genre[i]) {
                    cost += s;
                }

                lastGenre = genre[i];
            }

            best = Math.min(best, cost);
        }

        return best == INF ? -1 : best;
    }

    /**
     * Demonstrates the solution on the examples and a few additional checks.
     *
     * @param args command-line arguments (not used)
     *
     * Time complexity: depends on the sample sizes used here
     * Space complexity: depends on the sample sizes used here
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Example 1 from the prompt.
        // Important: the prompt's stated output is inconsistent with the formula.
        // Using the formula exactly, the correct minimum is 3.
        int[] duration1 = {4, 6, 3, 5};
        int[] enjoy1 = {2, 5, 1, 4};
        int[] genre1 = {1, 2, 2, 1};
        int k1 = 2;
        int s1 = 3;

        long result1 = solution.minimumPlaylistCost(duration1, enjoy1, genre1, k1, s1);
        long brute1 = solution.bruteForceMinimumCost(duration1, enjoy1, genre1, k1, s1);

        System.out.println("Example 1 DP result: " + result1);
        System.out.println("Example 1 brute-force check: " + brute1);
        System.out.println("Expected by formula: 3");

        // Example 2 from the prompt.
        // Again, the prompt's stated output is inconsistent with the formula.
        // Using the formula exactly, the correct minimum is 5.
        int[] duration2 = {7, 2, 6, 3, 4};
        int[] enjoy2 = {1, 3, 2, 5, 1};
        int[] genre2 = {1, 1, 3, 3, 2};
        int k2 = 3;
        int s2 = 4;

        long result2 = solution.minimumPlaylistCost(duration2, enjoy2, genre2, k2, s2);
        long brute2 = solution.bruteForceMinimumCost(duration2, enjoy2, genre2, k2, s2);

        System.out.println("Example 2 DP result: " + result2);
        System.out.println("Example 2 brute-force check: " + brute2);
        System.out.println("Expected by formula: 5");

        // Additional small demonstration.
        int[] duration3 = {5, 1, 4};
        int[] enjoy3 = {1, 1, 1};
        int[] genre3 = {2, 2, 3};
        int k3 = 2;
        int s3 = 10;

        long result3 = solution.minimumPlaylistCost(duration3, enjoy3, genre3, k3, s3);
        long brute3 = solution.bruteForceMinimumCost(duration3, enjoy3, genre3, k3, s3);

        System.out.println("Additional example DP result: " + result3);
        System.out.println("Additional example brute-force check: " + brute3);
    }
}