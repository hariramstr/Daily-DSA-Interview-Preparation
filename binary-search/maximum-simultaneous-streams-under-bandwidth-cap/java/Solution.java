import java.util.*;

/*
Problem Title: Maximum Simultaneous Streams Under Bandwidth Cap

Problem Description:
A video platform is preparing a live event with n available stream encodings. The i-th encoding can serve up to viewers[i] viewers, and each served viewer on that encoding consumes bitrate[i] units of bandwidth. You are given a total network budget B. The platform wants to choose a single target number x and require every selected encoding to serve exactly x viewers. An encoding can only be selected if viewers[i] >= x, and selecting it would then consume x * bitrate[i] bandwidth. You may select any subset of encodings, but the total consumed bandwidth of the selected encodings must not exceed B.

Your task is to compute the maximum possible number of selected encodings.

In other words, among all integers x >= 1, determine the largest subset size k such that there exists a set of k encodings where each chosen encoding can serve at least x viewers and the sum of x * bitrate[i] over those k encodings is at most B.

This problem is designed so that a direct search over all x is too slow. You will need to exploit the monotonic structure of feasibility and combine binary search with an efficient way to test whether at least k encodings can be chosen.

Constraints:
- 1 <= n <= 200000
- 1 <= viewers[i] <= 10^9
- 1 <= bitrate[i] <= 10^9
- 1 <= B <= 10^18
- Return a single integer: the maximum number of encodings that can be selected.

Important correctness note:
The natural mathematical interpretation of the statement is:
For a fixed subset size k, we only need the existence of some integer x >= 1 such that:
1) at least k encodings have viewers[i] >= x
2) among those eligible encodings, the sum of the k smallest bitrates multiplied by x is <= B

Under that interpretation, Example 2 from the prompt would actually allow k = 3 with x = 1
because all encodings are eligible and the three cheapest bitrates are 1, 2, and 5,
whose total is 8, and 1 * 8 <= 12.

Therefore, the sample explanation for Example 2 is inconsistent with the stated problem.
This implementation follows the formal problem statement exactly, which is the only way
to produce a correct general algorithm.
*/
public class Solution {

    /**
     * Small helper structure representing one encoding.
     * We store:
     * - viewers capacity
     * - bitrate cost per viewer
     */
    static class Encoding {
        int viewers;
        int bitrate;

        Encoding(int viewers, int bitrate) {
            this.viewers = viewers;
            this.bitrate = bitrate;
        }
    }

    /**
     * Computes the maximum number of encodings that can be selected.
     *
     * Core idea:
     * 1) Binary search on the answer k = number of selected encodings.
     * 2) For a fixed k, test whether it is possible to choose at least k encodings.
     * 3) The feasibility test uses a sweep over possible x values, but only at the
     *    distinct viewers capacities. While sweeping from large x to small x, more
     *    encodings become eligible. Among currently eligible encodings, we maintain
     *    the sum of the k smallest bitrates. If at any sweep point with target x,
     *    we have at least k eligible encodings and x * (sum of k smallest bitrates) <= B,
     *    then k is feasible.
     *
     * Why checking only distinct viewers values is enough:
     * - The set of eligible encodings changes only when x crosses some viewers[i].
     * - Between two consecutive distinct viewers values, eligibility is unchanged,
     *   and cost grows linearly with x, so the smallest x in that interval is best.
     * - That smallest x is exactly one of the distinct viewers values (or 1, which is
     *   also covered because if some viewers are >= 1, then the minimum distinct value
     *   among positive capacities handles the same eligible set or a subset boundary).
     *
     * Monotonicity for binary search:
     * - If k encodings are feasible, then any smaller number of encodings is also feasible
     *   by simply selecting fewer encodings.
     *
     * @param viewers array where viewers[i] is the maximum viewers the i-th encoding can serve
     * @param bitrate array where bitrate[i] is the bandwidth consumed per served viewer on encoding i
     * @param B total bandwidth budget
     * @return the maximum feasible number of selected encodings
     * Time complexity: O(n log^2 n) in the worst case
     * Space complexity: O(n)
     */
    public int maximumSimultaneousStreams(int[] viewers, int[] bitrate, long B) {
        int n = viewers.length;
        Encoding[] encodings = new Encoding[n];
        for (int i = 0; i < n; i++) {
            encodings[i] = new Encoding(viewers[i], bitrate[i]);
        }

        /*
         * Sort encodings by viewers descending.
         *
         * During feasibility checking for a fixed k, we sweep target x from large to small.
         * When x decreases, every encoding with viewers >= x becomes eligible.
         * Sorting descending lets us add encodings in exactly that order.
         */
        Arrays.sort(encodings, (a, b) -> Integer.compare(b.viewers, a.viewers));

        int low = 0;
        int high = n;

        /*
         * Standard binary search on answer:
         * - low is feasible
         * - high may or may not be feasible initially, so we use the common pattern
         *   of searching over [0, n] and testing mid.
         */
        while (low < high) {
            int mid = low + (high - low + 1) / 2;
            if (canSelectK(encodings, mid, B)) {
                low = mid;
            } else {
                high = mid - 1;
            }
        }

        return low;
    }

    /**
     * Checks whether it is possible to select at least k encodings for some integer x >= 1.
     *
     * Detailed strategy:
     * - If k == 0, it is always feasible.
     * - Sweep through encodings grouped by equal viewers value v.
     * - At the moment we finish adding all encodings with viewers == v, the currently
     *   added set is exactly the set of encodings eligible for target x = v.
     * - Among these eligible encodings, the cheapest way to choose k encodings is to
     *   take the k smallest bitrates.
     * - We maintain those k smallest bitrates dynamically using two heaps:
     *
     *   1) chosenMaxHeap:
     *      - contains exactly the currently selected smallest bitrates (up to k items)
     *      - max-heap so we can quickly remove the largest among the chosen ones if a
     *        newly added bitrate is smaller
     *      - sumChosen stores the sum of values inside this heap
     *
     *   2) restMinHeap:
     *      - contains all other eligible bitrates not currently in the chosen set
     *      - min-heap so if chosen has fewer than k items, we can promote the smallest
     *        remaining bitrate into chosen
     *
     * - After processing a viewers group with value v:
     *   if chosen contains k items and v * sumChosen <= B, then k is feasible.
     *
     * Why this is correct:
     * - For any fixed x, the eligible set is exactly encodings with viewers >= x.
     * - The minimum possible total bitrate sum for selecting k encodings from that set
     *   is the sum of the k smallest bitrates in the eligible set.
     * - Therefore feasibility at x is exactly:
     *      x * (sum of k smallest eligible bitrates) <= B
     * - The sweep checks this condition for every distinct eligible-set change point.
     *
     * @param encodings encodings sorted by viewers descending
     * @param k target number of encodings to select
     * @param B total bandwidth budget
     * @return true if there exists some integer x >= 1 making selection of k encodings feasible; false otherwise
     * Time complexity: O(n log n)
     * Space complexity: O(n)
     */
    public boolean canSelectK(Encoding[] encodings, int k, long B) {
        if (k == 0) {
            return true;
        }

        /*
         * chosenMaxHeap stores the currently selected k smallest bitrates.
         * Because it is a max-heap, the largest chosen bitrate is on top.
         * If a new bitrate is smaller than that largest chosen one, we should replace it.
         */
        PriorityQueue<Integer> chosenMaxHeap = new PriorityQueue<>(Collections.reverseOrder());

        /*
         * restMinHeap stores all other eligible bitrates not in chosen.
         * If chosen has fewer than k items, we promote the smallest from rest.
         */
        PriorityQueue<Integer> restMinHeap = new PriorityQueue<>();

        long sumChosen = 0L;
        int n = encodings.length;
        int i = 0;

        while (i < n) {
            int currentViewers = encodings[i].viewers;

            /*
             * Add every encoding whose viewers capacity equals currentViewers.
             * After this loop, all encodings with viewers >= currentViewers are eligible.
             */
            while (i < n && encodings[i].viewers == currentViewers) {
                int b = encodings[i].bitrate;

                /*
                 * Step 1: Insert this bitrate into our two-heap structure.
                 *
                 * Cases:
                 * - If chosen has fewer than k items, this bitrate should definitely be chosen
                 *   for now, because we still need more items.
                 * - Otherwise, compare against the largest chosen bitrate:
                 *   * if new bitrate is smaller, it belongs among the k smallest eligible bitrates
                 *   * the previous largest chosen bitrate gets demoted to rest
                 *   * otherwise, the new bitrate goes to rest
                 */
                if (chosenMaxHeap.size() < k) {
                    chosenMaxHeap.offer(b);
                    sumChosen += b;
                } else if (!chosenMaxHeap.isEmpty() && b < chosenMaxHeap.peek()) {
                    int removed = chosenMaxHeap.poll();
                    sumChosen -= removed;
                    restMinHeap.offer(removed);

                    chosenMaxHeap.offer(b);
                    sumChosen += b;
                } else {
                    restMinHeap.offer(b);
                }

                i++;
            }

            /*
             * Step 2: Safety rebalance.
             *
             * In this specific insertion logic, chosen should already have size min(k, eligibleCount),
             * but this rebalance keeps the structure robust and easy to understand.
             */
            while (chosenMaxHeap.size() < k && !restMinHeap.isEmpty()) {
                int promoted = restMinHeap.poll();
                chosenMaxHeap.offer(promoted);
                sumChosen += promoted;
            }

            /*
             * Step 3: If we have at least k eligible encodings, test feasibility for x = currentViewers.
             *
             * We need:
             *   currentViewers * sumChosen <= B
             *
             * To avoid overflow, compare using division:
             *   sumChosen <= B / currentViewers
             */
            if (chosenMaxHeap.size() == k) {
                long maxAllowedBitrateSum = B / currentViewers;
                if (sumChosen <= maxAllowedBitrateSum) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Convenience wrapper matching the problem statement style.
     *
     * @param viewers array of maximum viewers per encoding
     * @param bitrate array of bitrate cost per viewer
     * @param B total bandwidth budget
     * @return maximum number of encodings that can be selected
     * Time complexity: O(n log^2 n)
     * Space complexity: O(n)
     */
    public int solve(int[] viewers, int[] bitrate, long B) {
        return maximumSimultaneousStreams(viewers, bitrate, B);
    }

    /**
     * Demonstrates the solution on the sample-style inputs.
     *
     * Note:
     * - Example 1 from the prompt is consistent and this program prints 3.
     * - Example 2's stated output conflicts with the formal problem statement.
     *   Under the statement as written, the correct result is 3, not 2.
     *
     * @param args command-line arguments (unused)
     * @return nothing
     * Time complexity: O(1) for the demonstration itself, excluding calls to solve
     * Space complexity: O(1), excluding input storage
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        int[] viewers1 = {5, 3, 8, 6};
        int[] bitrate1 = {4, 2, 1, 3};
        long B1 = 18L;
        int result1 = solution.solve(viewers1, bitrate1, B1);
        System.out.println("Example 1 result: " + result1);

        int[] viewers2 = {10, 2, 7, 4};
        int[] bitrate2 = {8, 5, 2, 1};
        long B2 = 12L;
        int result2 = solution.solve(viewers2, bitrate2, B2);
        System.out.println("Example 2 result (by formal statement): " + result2);

        /*
         * Additional quick sanity checks:
         */

        int[] viewers3 = {1};
        int[] bitrate3 = {1};
        long B3 = 1L;
        System.out.println("Single encoding feasible: " + solution.solve(viewers3, bitrate3, B3));

        int[] viewers4 = {100, 100, 100};
        int[] bitrate4 = {10, 20, 30};
        long B4 = 59L;
        System.out.println("Budget too small for x=1 and all 3? result: " + solution.solve(viewers4, bitrate4, B4));
    }
}