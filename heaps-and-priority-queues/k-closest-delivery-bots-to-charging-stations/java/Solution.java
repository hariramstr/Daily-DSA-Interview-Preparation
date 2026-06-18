import java.util.*;

/*
 * Title: K Closest Delivery Bots to Charging Stations
 * Difficulty: Medium
 * Topic: Heaps and Priority Queues
 *
 * Problem Description:
 * A robotics warehouse tracks the current positions of delivery bots and needs to quickly identify
 * which bots should be recalled for charging. You are given two arrays: bots, where each element is
 * [id, x, y] representing a unique bot ID and its 2D position, and stations, where each element is
 * [x, y] representing a charging station.
 *
 * For every bot, define its charging distance as the Manhattan distance to its nearest charging station:
 * |x1 - x2| + |y1 - y2|.
 *
 * Your task is to return the IDs of the k bots with the smallest charging distances.
 * If two bots have the same charging distance, the bot with the smaller ID should come first.
 * The returned list must be sorted by increasing charging distance, and then by increasing ID.
 *
 * A straightforward solution may compare every bot against every station, but the interview challenge
 * is to combine efficient nearest-station evaluation with a heap or priority queue to keep only the
 * best k candidates seen so far.
 *
 * Constraints:
 * - 1 <= bots.length <= 10^5
 * - 1 <= stations.length <= 10^5
 * - 1 <= k <= bots.length
 * - 0 <= id <= 10^9
 * - -10^6 <= x, y <= 10^6
 * - All bot IDs are unique.
 *
 * Important correctness note:
 * To efficiently compute the nearest Manhattan distance from each bot to any station, this solution
 * uses the standard 4-transform technique:
 *
 * For any point (x, y), Manhattan distance to a station (sx, sy) can be expressed through:
 * max over 4 expressions involving:
 *   (sx + sy), (sx - sy), (-sx + sy), (-sx - sy)
 *
 * Then for a bot (x, y), the minimum Manhattan distance to any station is:
 * min_s |x - sx| + |y - sy|
 *   = max(
 *       (x + y) - max(sx + sy) ???  -> not directly enough alone
 *     )
 *
 * The correct compact form is:
 * min_s |x - sx| + |y - sy|
 *   = max(
 *       (x + y) - max_s(sx + sy) ??? no
 *     )
 *
 * That direct reduction is NOT valid for minimum-to-set queries.
 *
 * Therefore, to guarantee correctness for all inputs, this implementation computes each bot's nearest
 * station distance by scanning all stations. This is straightforward and correct, and then uses a heap
 * to keep only the best k bots. While this nearest-station step is O(B * S), the heap maintenance is
 * optimal for the "top k" part and the implementation is fully correct and beginner-friendly.
 *
 * Since the prompt explicitly prioritizes correctness, this solution chooses correctness first.
 */
public class Solution {

    /**
     * Small helper class to store a bot candidate together with its nearest charging distance.
     */
    private static class BotInfo {
        int id;
        long distance;

        BotInfo(int id, long distance) {
            this.id = id;
            this.distance = distance;
        }
    }

    /**
     * Returns the IDs of the k bots with the smallest Manhattan distance to their nearest charging station.
     * The final returned IDs are ordered by:
     * 1) increasing nearest distance
     * 2) increasing bot ID when distances tie
     *
     * Time complexity:
     * - Nearest-station computation: O(bots.length * stations.length)
     * - Heap maintenance: O(bots.length * log k)
     * - Final sorting of k selected bots: O(k log k)
     * Overall: O(bots.length * stations.length + bots.length * log k + k log k)
     *
     * Space complexity:
     * - O(k) for the heap
     * - O(k) for the final selected list
     *
     * @param bots     array where each element is [id, x, y]
     * @param stations array where each element is [x, y]
     * @param k        number of closest bots to return
     * @return an int array of bot IDs sorted by increasing nearest charging distance, then increasing ID
     */
    public int[] kClosestBots(int[][] bots, int[][] stations, int k) {
        /*
         * We use a max-heap of size at most k.
         *
         * Why a max-heap?
         * Because we want to keep the best k bots seen so far.
         * The "worst" among those best k should be easy to remove when a better bot appears.
         *
         * "Worse" means:
         * - larger distance is worse
         * - if distance ties, larger ID is worse
         *
         * So the heap top should be the currently worst candidate among the kept k.
         */
        PriorityQueue<BotInfo> maxHeap = new PriorityQueue<>((a, b) -> {
            if (a.distance != b.distance) {
                return Long.compare(b.distance, a.distance);
            }
            return Integer.compare(b.id, a.id);
        });

        /*
         * Process every bot one by one.
         */
        for (int[] bot : bots) {
            int id = bot[0];
            int x = bot[1];
            int y = bot[2];

            /*
             * Compute the nearest Manhattan distance from this bot to any charging station.
             *
             * We do a full scan of all stations to guarantee correctness.
             */
            long nearestDistance = nearestStationDistance(x, y, stations);

            BotInfo current = new BotInfo(id, nearestDistance);

            /*
             * If we still have fewer than k bots in the heap, simply add this one.
             */
            if (maxHeap.size() < k) {
                maxHeap.offer(current);
            } else {
                /*
                 * Compare this bot with the current worst among the selected k bots.
                 * If current is better, remove the worst and insert current.
                 */
                BotInfo worstKept = maxHeap.peek();

                if (isBetter(current, worstKept)) {
                    maxHeap.poll();
                    maxHeap.offer(current);
                }
            }
        }

        /*
         * Extract the selected bots from the heap into a list.
         */
        List<BotInfo> selected = new ArrayList<>();
        while (!maxHeap.isEmpty()) {
            selected.add(maxHeap.poll());
        }

        /*
         * The heap does NOT guarantee the final required order.
         * So we must sort the selected bots by:
         * 1) increasing distance
         * 2) increasing ID
         *
         * This is essential for correctness.
         *
         * Example 2 from the prompt contains a wording inconsistency:
         * It first lists [3,12,9], but then correctly explains the final ordered result
         * should be [3,9,12] because bots 9 and 12 both have distance 4 and 9 < 12.
         *
         * We follow the stated sorting rule, so the correct output is [3,9,12].
         */
        selected.sort((a, b) -> {
            if (a.distance != b.distance) {
                return Long.compare(a.distance, b.distance);
            }
            return Integer.compare(a.id, b.id);
        });

        /*
         * Build the final answer array containing only IDs.
         */
        int[] result = new int[selected.size()];
        for (int i = 0; i < selected.size(); i++) {
            result[i] = selected.get(i).id;
        }

        return result;
    }

    /**
     * Computes the Manhattan distance from a bot position (x, y) to its nearest charging station.
     *
     * Time complexity:
     * - O(stations.length)
     *
     * Space complexity:
     * - O(1)
     *
     * @param x        bot x-coordinate
     * @param y        bot y-coordinate
     * @param stations array of charging stations, each as [x, y]
     * @return the minimum Manhattan distance from the bot to any station
     */
    public long nearestStationDistance(int x, int y, int[][] stations) {
        long best = Long.MAX_VALUE;

        /*
         * Check every station and keep the smallest Manhattan distance found.
         */
        for (int[] station : stations) {
            long sx = station[0];
            long sy = station[1];

            long distance = Math.abs((long) x - sx) + Math.abs((long) y - sy);

            if (distance < best) {
                best = distance;
            }
        }

        return best;
    }

    /**
     * Returns true if bot a should rank ahead of bot b.
     *
     * Ranking rule:
     * - smaller distance is better
     * - if distance ties, smaller ID is better
     *
     * Time complexity:
     * - O(1)
     *
     * Space complexity:
     * - O(1)
     *
     * @param a first bot candidate
     * @param b second bot candidate
     * @return true if a is better than b according to the problem ordering
     */
    public boolean isBetter(BotInfo a, BotInfo b) {
        if (a.distance != b.distance) {
            return a.distance < b.distance;
        }
        return a.id < b.id;
    }

    /**
     * Utility method to print an int array in a readable format.
     *
     * Time complexity:
     * - O(n)
     *
     * Space complexity:
     * - O(1) auxiliary, ignoring output construction done by Arrays.toString
     *
     * @param arr the array to print
     * @return string representation of the array
     */
    public String arrayToString(int[] arr) {
        return Arrays.toString(arr);
    }

    /**
     * Demonstrates the solution on the sample inputs from the problem statement.
     *
     * Time complexity:
     * - Depends on the sample sizes used here; for each call it follows kClosestBots complexity
     *
     * Space complexity:
     * - Depends on the sample sizes used here; for each call it follows kClosestBots complexity
     *
     * @param args command-line arguments (not used)
     * @return nothing
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        /*
         * Example 1
         *
         * bots = [[101,1,2],[205,3,5],[150,-1,4]]
         * stations = [[0,0],[2,3]]
         * k = 2
         *
         * Distances:
         * - Bot 101 at (1,2):
         *   to (0,0) = 3
         *   to (2,3) = 2
         *   nearest = 2
         *
         * - Bot 205 at (3,5):
         *   to (0,0) = 8
         *   to (2,3) = 3
         *   nearest = 3
         *
         * - Bot 150 at (-1,4):
         *   to (0,0) = 5
         *   to (2,3) = 4
         *   nearest = 4
         *
         * Therefore the correct closest 2 bots are [101, 205].
         *
         * Note:
         * The prompt's Example 1 explanation claims bot 150 has nearest distance 2,
         * which is incorrect. The actual nearest distance is 4.
         * Since correctness is mandatory, we print the mathematically correct result.
         */
        int[][] bots1 = {
            {101, 1, 2},
            {205, 3, 5},
            {150, -1, 4}
        };
        int[][] stations1 = {
            {0, 0},
            {2, 3}
        };
        int k1 = 2;

        int[] result1 = solution.kClosestBots(bots1, stations1, k1);
        System.out.println("Example 1 result: " + solution.arrayToString(result1));

        /*
         * Example 2
         *
         * bots = [[7,10,10],[3,1,1],[9,4,0],[12,2,2]]
         * stations = [[0,0]]
         * k = 3
         *
         * Distances:
         * - Bot 7  -> 20
         * - Bot 3  -> 2
         * - Bot 9  -> 4
         * - Bot 12 -> 4
         *
         * Closest 3 bots sorted by distance then ID:
         * [3, 9, 12]
         */
        int[][] bots2 = {
            {7, 10, 10},
            {3, 1, 1},
            {9, 4, 0},
            {12, 2, 2}
        };
        int[][] stations2 = {
            {0, 0}
        };
        int k2 = 3;

        int[] result2 = solution.kClosestBots(bots2, stations2, k2);
        System.out.println("Example 2 result: " + solution.arrayToString(result2));

        /*
         * Additional quick sanity test:
         * If k equals the number of bots, all bots should be returned in sorted order.
         */
        int[][] bots3 = {
            {50, 0, 5},
            {10, 0, 1},
            {30, 0, 3}
        };
        int[][] stations3 = {
            {0, 0}
        };
        int k3 = 3;

        int[] result3 = solution.kClosestBots(bots3, stations3, k3);
        System.out.println("Additional test result: " + solution.arrayToString(result3));
    }
}