import java.util.*;

/*
Problem Title: Smallest Unlocked Seat for Returning Travelers

Problem Description:
An airport lounge has numbered seats starting from 0. Travelers arrive and leave throughout the day.
Whenever a traveler arrives, they must be assigned the smallest-numbered seat that is currently
unoccupied. If no previously used seat is available, the lounge opens a new seat with the next
unused number.

You are given two integer arrays, arrivals and departures, where arrivals[i] and departures[i]
are the arrival and departure times of traveler i. All arrival times are distinct, but multiple
travelers may leave at the same time. If a traveler leaves at time t, that seat becomes available
immediately, so another traveler arriving at time t may use it.

Return the seat number assigned to traveler targetTraveler.

Your task is to simulate the seating process efficiently. A brute-force scan of all seats on every
arrival will be too slow for large inputs. Think carefully about how to track both currently
occupied seats and the pool of reusable seat numbers.

Constraints:
- 1 <= n == arrivals.length == departures.length <= 100000
- 1 <= arrivals[i] < departures[i] <= 1000000000
- All values in arrivals are distinct
- 0 <= targetTraveler < n

Example 1:
Input: arrivals = [1,4,2], departures = [5,6,3], targetTraveler = 1
Output: 1
Explanation: Traveler 0 arrives at 1 and takes seat 0. Traveler 2 arrives at 2 and takes seat 1.
Traveler 2 leaves at 3, freeing seat 1. Traveler 1 arrives at 4 and takes the smallest available
seat, which is 1.

Example 2:
Input: arrivals = [3,8,5,6], departures = [10,9,7,11], targetTraveler = 3
Output: 2
Explanation: Traveler 0 arrives at 3 and takes seat 0. Traveler 2 arrives at 5 and takes seat 1.
Traveler 3 arrives at 6 and takes seat 2 because seats 0 and 1 are still occupied. Traveler 2
leaves at 7, but that happens after traveler 3 has already been seated, so the answer remains 2.
*/

public class Solution {

    /**
     * Finds the seat number assigned to the target traveler.
     *
     * The algorithm processes travelers in order of arrival time.
     * It uses two priority queues:
     * 1. A min-heap of currently occupied seats ordered by departure time, so we can quickly free
     *    every seat whose traveler has already left before or exactly when the next traveler arrives.
     * 2. A min-heap of available seat numbers, so we can always reuse the smallest-numbered free seat.
     *
     * If no reusable seat exists, we assign the next new seat number.
     *
     * @param arrivals the arrival times of all travelers; arrivals[i] is the arrival time of traveler i
     * @param departures the departure times of all travelers; departures[i] is the departure time of traveler i
     * @param targetTraveler the index of the traveler whose assigned seat should be returned
     * @return the seat number assigned to targetTraveler
     * Time complexity: O(n log n), because we sort travelers by arrival time and each traveler is inserted
     * and removed from heaps at most once.
     * Space complexity: O(n), for the sorted traveler list and the two priority queues.
     */
    public int smallestSeat(int[] arrivals, int[] departures, int targetTraveler) {
        int n = arrivals.length;

        // Build an array of travelers where each entry stores:
        // [arrivalTime, departureTime, travelerIndex]
        //
        // We need the original traveler index so that after sorting by arrival time,
        // we can still recognize when we are processing the target traveler.
        int[][] travelers = new int[n][3];
        for (int i = 0; i < n; i++) {
            travelers[i][0] = arrivals[i];
            travelers[i][1] = departures[i];
            travelers[i][2] = i;
        }

        // Sort all travelers by arrival time.
        //
        // This is the natural order in which seat assignments happen.
        // Since all arrival times are distinct, there is no ambiguity in ordering.
        Arrays.sort(travelers, Comparator.comparingInt(a -> a[0]));

        // Min-heap of currently occupied seats.
        //
        // Each element is [departureTime, seatNumber].
        // The traveler who leaves earliest is at the top.
        //
        // Why do we need this?
        // Before seating a newly arriving traveler, we must free every seat whose departure time
        // is <= current arrival time, because those seats are immediately available.
        PriorityQueue<int[]> occupiedSeats = new PriorityQueue<>(Comparator.comparingInt(a -> a[0]));

        // Min-heap of available seat numbers.
        //
        // Whenever a traveler leaves, their seat number is pushed here.
        // When a new traveler arrives, if this heap is not empty, we must use the smallest seat number.
        PriorityQueue<Integer> availableSeats = new PriorityQueue<>();

        // This tracks the next brand-new seat number that has never been used before.
        //
        // Example:
        // If seats 0, 1, and 2 have already been created at some point, then nextNewSeat == 3.
        int nextNewSeat = 0;

        // Process travelers in increasing order of arrival time.
        for (int[] traveler : travelers) {
            int arrivalTime = traveler[0];
            int departureTime = traveler[1];
            int travelerIndex = traveler[2];

            // Step 1: Free all seats whose travelers have already left.
            //
            // IMPORTANT:
            // We use <= arrivalTime, not < arrivalTime.
            // If someone leaves exactly when another traveler arrives, that seat is available immediately.
            while (!occupiedSeats.isEmpty() && occupiedSeats.peek()[0] <= arrivalTime) {
                int[] leavingInfo = occupiedSeats.poll();
                int freedSeat = leavingInfo[1];

                // Put the freed seat back into the pool of reusable seats.
                availableSeats.offer(freedSeat);
            }

            // Step 2: Assign the smallest available seat.
            //
            // If there is any reusable seat, we must choose the smallest-numbered one.
            // Otherwise, we create a new seat using nextNewSeat.
            int assignedSeat;
            if (!availableSeats.isEmpty()) {
                assignedSeat = availableSeats.poll();
            } else {
                assignedSeat = nextNewSeat;
                nextNewSeat++;
            }

            // Step 3: If this is the target traveler, we can return immediately.
            //
            // This is safe because the seat assignment for the target traveler is fully determined
            // at the moment they arrive. We do not need to simulate later events.
            if (travelerIndex == targetTraveler) {
                return assignedSeat;
            }

            // Step 4: Mark this seat as occupied until the traveler's departure time.
            occupiedSeats.offer(new int[]{departureTime, assignedSeat});
        }

        // According to the problem constraints, targetTraveler is always valid,
        // so execution should never reach this line.
        return -1;
    }

    /**
     * Demonstrates the solution on the sample inputs from the problem statement.
     *
     * @param args command-line arguments; not used
     * @return nothing
     * Time complexity: O(1) for the demonstration itself, excluding the called algorithm.
     * Space complexity: O(1), excluding the called algorithm.
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Example 1
        int[] arrivals1 = {1, 4, 2};
        int[] departures1 = {5, 6, 3};
        int targetTraveler1 = 1;
        int result1 = solution.smallestSeat(arrivals1, departures1, targetTraveler1);
        System.out.println("Example 1 Output: " + result1); // Expected: 1

        // Example 2
        int[] arrivals2 = {3, 8, 5, 6};
        int[] departures2 = {10, 9, 7, 11};
        int targetTraveler2 = 3;
        int result2 = solution.smallestSeat(arrivals2, departures2, targetTraveler2);
        System.out.println("Example 2 Output: " + result2); // Expected: 2

        // Additional quick sanity check
        int[] arrivals3 = {1, 2, 3};
        int[] departures3 = {4, 5, 6};
        int targetTraveler3 = 2;
        int result3 = solution.smallestSeat(arrivals3, departures3, targetTraveler3);
        System.out.println("Additional Test Output: " + result3); // Expected: 2
    }
}