/*
 * Closest Patient Appointments
 * =============================
 * Difficulty: Easy
 * Topic: Heaps and Priority Queues
 *
 * Problem Description:
 * A hospital scheduling system receives a list of patient appointment times
 * (in minutes from midnight) and a query time `t`. Your task is to find the
 * `k` appointment times closest to the query time `t`.
 *
 * If two appointments are equally close to `t`, prefer the one with the
 * smaller time value. Return the result as a list sorted in ascending order.
 *
 * Problem Statement:
 * Given an integer array `appointments` representing scheduled appointment times,
 * an integer `t` representing a query time, and an integer `k`, return the `k`
 * appointment times closest to `t`, sorted in ascending order.
 *
 * Constraints:
 * - 1 <= appointments.length <= 10^4
 * - 0 <= appointments[i] <= 1440 (minutes in a day)
 * - 0 <= t <= 1440
 * - 1 <= k <= appointments.length
 * - All appointment times are distinct.
 *
 * Example 1:
 * Input: appointments = [30, 120, 200, 450, 800], t = 150, k = 2
 * Output: [120, 200]
 * Explanation: Distances from 150 are [120, 30, 50, 300, 650].
 *              The two smallest distances are 30 (time 120) and 50 (time 200).
 *
 * Example 2:
 * Input: appointments = [60, 180, 300, 420], t = 240, k = 3
 * Output: [60, 180, 300]
 * Explanation: Distances from 240 are [180, 60, 60, 180].
 *              The three closest: 60 (time 180), 60 (time 300),
 *              180 (time 60 — prefer smaller, so 60 over 420).
 *              Result sorted: [60, 180, 300].
 */

import java.util.*;

/**
 * Solution class for the "Closest Patient Appointments" problem.
 *
 * <p>Strategy: Use a max-heap (PriorityQueue) of size k to maintain the k closest
 * appointment times seen so far. The heap orders elements by:
 *   1. Larger distance first (so we can easily evict the "worst" candidate)
 *   2. On tie in distance, larger time value first (so smaller time is preferred)
 *
 * After processing all appointments, the heap contains exactly the k closest ones.
 * We then collect them into a list and sort ascending.
 */
public class Solution {

    /**
     * Finds the k appointment times closest to the query time t.
     *
     * <p>Algorithm overview:
     * <ol>
     *   <li>Build a max-heap keyed on (distance DESC, time DESC) with capacity k.</li>
     *   <li>For each appointment, compute its distance to t.</li>
     *   <li>If the heap has fewer than k elements, push the appointment.</li>
     *   <li>Otherwise, compare the current appointment with the heap's top (the
     *       "worst" of the current k best). If the current appointment is strictly
     *       better, replace the top.</li>
     *   <li>Collect the heap contents, sort ascending, and return.</li>
     * </ol>
     *
     * @param appointments array of appointment times in minutes from midnight
     * @param t            the query time in minutes from midnight
     * @param k            the number of closest appointments to return
     * @return a list of k appointment times closest to t, sorted in ascending order
     *
     * Time Complexity:  O(n log k) — each of the n appointments may trigger a
     *                   heap push/pop, each costing O(log k).
     * Space Complexity: O(k) — the heap holds at most k elements at any time.
     */
    public List<Integer> findClosestAppointments(int[] appointments, int t, int k) {

        // -----------------------------------------------------------------------
        // Step 1: Define a max-heap comparator.
        //
        // We want the heap's ROOT to be the "worst" among the current k best,
        // so that we can quickly decide whether a new appointment should replace it.
        //
        // Ordering rule (for the root to be the worst):
        //   - Primary  : larger distance  → comes first (max by distance)
        //   - Secondary: larger time value → comes first (max by time, so that
        //                when distances are equal the smaller time is kept)
        //
        // Java's PriorityQueue is a MIN-heap by default, so we invert the natural
        // order with the comparator to get a MAX-heap.
        // -----------------------------------------------------------------------
        PriorityQueue<Integer> maxHeap = new PriorityQueue<>(k, (a, b) -> {
            int distA = Math.abs(a - t);
            int distB = Math.abs(b - t);

            // If distances differ, the one with the LARGER distance is "worse"
            // and should sit at the root → return positive when distA > distB.
            if (distA != distB) {
                return distB - distA; // descending by distance
            }

            // Distances are equal: the one with the LARGER time value is "worse"
            // (we prefer smaller time on ties) → return positive when a > b.
            return b - a; // descending by time value
        });

        // -----------------------------------------------------------------------
        // Step 2: Iterate over every appointment and maintain the heap of size k.
        // -----------------------------------------------------------------------
        for (int appt : appointments) {

            // Compute how far this appointment is from the query time t.
            int dist = Math.abs(appt - t);

            if (maxHeap.size() < k) {
                // The heap is not yet full — always add the current appointment.
                maxHeap.offer(appt);

            } else {
                // The heap is full. Peek at the root, which is the "worst" of
                // the current k best candidates.
                int worstAppt = maxHeap.peek();
                int worstDist = Math.abs(worstAppt - t);

                // Decide whether the current appointment is better than the worst.
                // "Better" means:
                //   (a) smaller distance, OR
                //   (b) same distance but smaller time value.
                boolean currentIsBetter;
                if (dist != worstDist) {
                    currentIsBetter = dist < worstDist;
                } else {
                    // Equal distance → prefer the smaller time value.
                    currentIsBetter = appt < worstAppt;
                }

                if (currentIsBetter) {
                    // Evict the worst candidate and insert the current appointment.
                    maxHeap.poll();
                    maxHeap.offer(appt);
                }
                // If the current appointment is NOT better, we simply skip it.
            }
        }

        // -----------------------------------------------------------------------
        // Step 3: Drain the heap into a list.
        //
        // The heap now contains exactly k appointment times (the closest ones).
        // The heap order is NOT ascending, so we must sort the result.
        // -----------------------------------------------------------------------
        List<Integer> result = new ArrayList<>(maxHeap);

        // Sort the k appointments in ascending order as required by the problem.
        Collections.sort(result);

        return result;
    }

    // ==========================================================================
    // Main method — demonstrates the solution with the provided examples
    // ==========================================================================

    /**
     * Entry point. Runs the two examples from the problem description and prints
     * the results to standard output.
     *
     * @param args command-line arguments (not used)
     */
    public static void main(String[] args) {

        Solution solution = new Solution();

        // ------------------------------------------------------------------
        // Example 1
        // appointments = [30, 120, 200, 450, 800], t = 150, k = 2
        // Expected output: [120, 200]
        //
        // Trace:
        //   appt=30  : dist=120, heap=[30]          (size < k=2, just add)
        //   appt=120 : dist=30,  heap=[30,120]       (size < k=2, just add)
        //   appt=200 : dist=50,  worst=30(dist=120)  → 50 < 120 → replace
        //              heap=[120,200]
        //   appt=450 : dist=300, worst=120(dist=30)  → 300 > 30  → skip
        //   appt=800 : dist=650, worst=120(dist=30)  → 650 > 30  → skip
        //   Final heap: [120, 200] → sorted → [120, 200] ✓
        // ------------------------------------------------------------------
        int[] appointments1 = {30, 120, 200, 450, 800};
        int t1 = 150, k1 = 2;
        List<Integer> result1 = solution.findClosestAppointments(appointments1, t1, k1);
        System.out.println("Example 1:");
        System.out.println("  Input : appointments=" + Arrays.toString(appointments1)
                + ", t=" + t1 + ", k=" + k1);
        System.out.println("  Output: " + result1);
        System.out.println("  Expected: [120, 200]");
        System.out.println();

        // ------------------------------------------------------------------
        // Example 2
        // appointments = [60, 180, 300, 420], t = 240, k = 3
        // Expected output: [60, 180, 300]
        //
        // Distances: 60→180, 180→60, 300→60, 420→180
        //
        // Trace:
        //   appt=60  : dist=180, heap=[60]            (size < k=3)
        //   appt=180 : dist=60,  heap=[60,180]         (size < k=3)
        //   appt=300 : dist=60,  heap=[60,180,300]     (size < k=3)
        //   appt=420 : dist=180
        //     worst in heap? The max-heap root is the "worst".
        //     Heap contains {60(dist=180), 180(dist=60), 300(dist=60)}.
        //     Root (worst) = 60 (dist=180, largest dist; on tie by dist, largest time).
        //     Current: dist=180, appt=420.
        //     dist equal (180==180) → prefer smaller time → 420 > 60 → NOT better → skip.
        //   Final heap: {60, 180, 300} → sorted → [60, 180, 300] ✓
        // ------------------------------------------------------------------
        int[] appointments2 = {60, 180, 300, 420};
        int t2 = 240, k2 = 3;
        List<Integer> result2 = solution.findClosestAppointments(appointments2, t2, k2);
        System.out.println("Example 2:");
        System.out.println("  Input : appointments=" + Arrays.toString(appointments2)
                + ", t=" + t2 + ", k=" + k2);
        System.out.println("  Output: " + result2);
        System.out.println("  Expected: [60, 180, 300]");
        System.out.println();

        // ------------------------------------------------------------------
        // Additional edge-case: k equals the full array length
        // All appointments should be returned, sorted ascending.
        // ------------------------------------------------------------------
        int[] appointments3 = {100, 500, 300};
        int t3 = 200, k3 = 3;
        List<Integer> result3 = solution.findClosestAppointments(appointments3, t3, k3);
        System.out.println("Edge Case (k = n):");
        System.out.println("  Input : appointments=" + Arrays.toString(appointments3)
                + ", t=" + t3 + ", k=" + k3);
        System.out.println("  Output: " + result3);
        System.out.println("  Expected: [100, 300, 500]");
        System.out.println();

        // ------------------------------------------------------------------
        // Additional edge-case: single appointment
        // ------------------------------------------------------------------
        int[] appointments4 = {720};
        int t4 = 0, k4 = 1;
        List<Integer> result4 = solution.findClosestAppointments(appointments4, t4, k4);
        System.out.println("Edge Case (single appointment):");
        System.out.println("  Input : appointments=" + Arrays.toString(appointments4)
                + ", t=" + t4 + ", k=" + k4);
        System.out.println("  Output: " + result4);
        System.out.println("  Expected: [720]");
    }
}