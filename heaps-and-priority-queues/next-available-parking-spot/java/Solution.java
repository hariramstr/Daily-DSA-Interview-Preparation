import java.util.*;

/*
 * Title: Next Available Parking Spot
 * Difficulty: Easy
 * Topic: Heaps and Priority Queues
 *
 * Problem Description:
 * A parking garage tracks which numbered spots are currently free. Spot numbers are positive integers,
 * and smaller numbers are closer to the entrance. You are given an integer n representing spots
 * numbered from 1 to n, and an array occupied containing the spot numbers that are already taken
 * when the day begins. After that, a sequence of operations arrives.
 *
 * Each operation is one of two types:
 * - ["park"]: assign the smallest-numbered free spot and return its number. If no spot is free, return -1.
 * - ["leave", x]: mark spot x as free again. It is guaranteed that x is currently occupied when this operation appears.
 *
 * Return an array containing the result of every "park" operation in order.
 *
 * The efficient approach is to keep all currently free spots in a min-heap so the nearest available
 * spot can be assigned quickly.
 *
 * Constraints:
 * - 1 <= n <= 100000
 * - 0 <= occupied.length <= n
 * - 1 <= occupied[i] <= n
 * - All values in occupied are distinct
 * - 1 <= operations.length <= 100000
 * - Each operation is either ["park"] or ["leave", x]
 * - For every leave operation, x is guaranteed to be occupied at that moment
 *
 * Example 1:
 * Input:
 * n = 5
 * occupied = [2, 4]
 * operations = [["park"], ["park"], ["leave", 2], ["park"], ["park"]]
 * Output: [1, 3, 2, 5]
 *
 * Explanation:
 * Free spots initially are 1, 3, and 5.
 * - "park"  -> assign 1
 * - "park"  -> assign 3
 * - "leave" -> spot 2 becomes free
 * - "park"  -> assign 2
 * - "park"  -> assign 5
 *
 * Example 2:
 * Input:
 * n = 3
 * occupied = [1, 2, 3]
 * operations = [["park"], ["leave", 2], ["park"], ["park"]]
 * Output: [-1, 2, -1]
 *
 * Explanation:
 * Initially no spot is free.
 * - "park"  -> no free spot, return -1
 * - "leave" -> spot 2 becomes free
 * - "park"  -> assign 2
 * - "park"  -> no free spot, return -1
 */

public class Solution {

    /**
     * Solves the parking spot assignment problem using a min-heap.
     *
     * The key idea:
     * 1. Track which spots are currently occupied.
     * 2. Put every currently free spot into a min-heap.
     * 3. For a "park" operation:
     *    - If the heap is empty, return -1.
     *    - Otherwise, remove the smallest free spot from the heap, mark it occupied, and return it.
     * 4. For a "leave" operation:
     *    - Mark that spot as free.
     *    - Add it back into the min-heap.
     *
     * This guarantees that every "park" operation always gets the smallest-numbered free spot.
     *
     * @param n the total number of parking spots, numbered from 1 to n
     * @param occupied an array of spot numbers that are occupied at the start
     * @param operations a 2D string array of operations; each operation is either {"park"} or {"leave", "x"}
     * @return an int array containing the result of each "park" operation in the order they occur
     *
     * Time complexity:
     * O(n log n + m log n), where m is the number of operations.
     * Building the initial heap takes up to O(n log n), and each operation involving the heap costs O(log n).
     *
     * Space complexity:
     * O(n + p), where n is the number of spots and p is the number of "park" results.
     * We store occupancy information, the heap of free spots, and the output list.
     */
    public int[] nextAvailableParkingSpot(int n, int[] occupied, String[][] operations) {
        // occupiedSet[i] tells us whether spot i is currently occupied.
        // We use size n + 1 so that we can directly use 1-based indexing for spot numbers.
        boolean[] occupiedSet = new boolean[n + 1];

        // First, mark all initially occupied spots.
        for (int spot : occupied) {
            occupiedSet[spot] = true;
        }

        // Min-heap of all currently free spots.
        // The smallest spot number will always be at the top.
        PriorityQueue<Integer> freeSpots = new PriorityQueue<>();

        // Add every spot that is NOT occupied into the min-heap.
        // This prepares the structure so that "park" can immediately take the smallest free spot.
        for (int spot = 1; spot <= n; spot++) {
            if (!occupiedSet[spot]) {
                freeSpots.offer(spot);
            }
        }

        // We do not know in advance how many "park" operations there are,
        // so we collect answers in a dynamic list first.
        List<Integer> parkedResults = new ArrayList<>();

        // Process each operation in order.
        for (String[] operation : operations) {
            // The first string tells us the operation type.
            String type = operation[0];

            if ("park".equals(type)) {
                // If there are no free spots, parking is impossible.
                if (freeSpots.isEmpty()) {
                    parkedResults.add(-1);
                } else {
                    // Remove the smallest-numbered free spot from the heap.
                    int assignedSpot = freeSpots.poll();

                    // Mark it as occupied because it has now been assigned.
                    occupiedSet[assignedSpot] = true;

                    // Record the assigned spot in the answer list.
                    parkedResults.add(assignedSpot);
                }
            } else {
                // This must be a "leave" operation.
                // The problem guarantees the format is ["leave", x]
                // and that x is currently occupied.
                int spotToFree = Integer.parseInt(operation[1]);

                // Mark the spot as no longer occupied.
                occupiedSet[spotToFree] = false;

                // Add it back into the min-heap of free spots.
                // From now on, future "park" operations may assign it again.
                freeSpots.offer(spotToFree);
            }
        }

        // Convert the dynamic list of answers into a fixed-size int array.
        int[] result = new int[parkedResults.size()];
        for (int i = 0; i < parkedResults.size(); i++) {
            result[i] = parkedResults.get(i);
        }

        return result;
    }

    /**
     * Convenience overload that accepts operations as a list of string lists.
     * This can be useful when constructing test cases programmatically.
     *
     * @param n the total number of parking spots, numbered from 1 to n
     * @param occupied an array of spot numbers that are occupied at the start
     * @param operations a list of operations; each operation is either ["park"] or ["leave", "x"]
     * @return an int array containing the result of each "park" operation in the order they occur
     *
     * Time complexity:
     * O(n log n + m log n), where m is the number of operations.
     *
     * Space complexity:
     * O(n + p), where p is the number of "park" operations.
     */
    public int[] nextAvailableParkingSpot(int n, int[] occupied, List<List<String>> operations) {
        String[][] opsArray = new String[operations.size()][];
        for (int i = 0; i < operations.size(); i++) {
            List<String> op = operations.get(i);
            opsArray[i] = op.toArray(new String[0]);
        }
        return nextAvailableParkingSpot(n, occupied, opsArray);
    }

    /**
     * Converts an int array into a readable string representation.
     *
     * @param arr the array to convert
     * @return a string such as "[1, 3, 2, 5]"
     *
     * Time complexity:
     * O(k), where k is the length of the array.
     *
     * Space complexity:
     * O(k), due to the created string content.
     */
    public static String arrayToString(int[] arr) {
        return Arrays.toString(arr);
    }

    /**
     * Demonstrates the solution using the sample inputs from the problem statement.
     *
     * It prints:
     * - The computed output
     * - The expected output
     *
     * This allows a quick manual verification that the implementation matches the examples exactly.
     *
     * @param args command-line arguments (not used)
     *
     * Time complexity:
     * O(n log n + m log n) per demonstration case.
     *
     * Space complexity:
     * O(n + p) per demonstration case.
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Example 1
        int n1 = 5;
        int[] occupied1 = {2, 4};
        String[][] operations1 = {
                {"park"},
                {"park"},
                {"leave", "2"},
                {"park"},
                {"park"}
        };

        int[] result1 = solution.nextAvailableParkingSpot(n1, occupied1, operations1);
        System.out.println("Example 1 Output:   " + arrayToString(result1));
        System.out.println("Example 1 Expected: [1, 3, 2, 5]");

        // Example 2
        int n2 = 3;
        int[] occupied2 = {1, 2, 3};
        String[][] operations2 = {
                {"park"},
                {"leave", "2"},
                {"park"},
                {"park"}
        };

        int[] result2 = solution.nextAvailableParkingSpot(n2, occupied2, operations2);
        System.out.println("Example 2 Output:   " + arrayToString(result2));
        System.out.println("Example 2 Expected: [-1, 2, -1]");
    }
}