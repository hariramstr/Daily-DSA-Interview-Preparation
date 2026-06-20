import java.util.*;

/*
Problem Title: Process Servers by Highest Failure Count

Problem Description:
You are given a stream of server failure reports from a monitoring system. Each report contains
the name of a server that just failed. After processing all reports, the operations team wants
to repair servers in priority order.

A server with a higher total number of failures should be repaired earlier. If two servers have
the same number of failures, the server with the lexicographically smaller name should come first.

Your task is to return the names of the top k servers in the exact order they should be repaired.

This problem is designed for an efficient solution using a heap or priority queue. A straightforward
full sort may work for small inputs, but the intended approach should scale well when the number of
distinct servers is large and k is much smaller than that number.

Return a list of up to k server names. If there are fewer than k distinct servers, return all of
them in the required order.

Constraints:
- 1 <= reports.length <= 200000
- 1 <= reports[i].length <= 30
- reports[i] consists of lowercase English letters, digits, and hyphens
- 1 <= k <= 200000
- Server names are case-sensitive only if your language treats strings that way; assume all input
  names are already normalized

Example 1:
Input: reports = ["db-1","api-2","db-1","cache-7","api-2","db-1"], k = 2
Output: ["db-1","api-2"]

Example 2:
Input: reports = ["node-b","node-a","node-b","node-a","node-c"], k = 3
Output: ["node-a","node-b","node-c"]
*/

public class Solution {

    /**
     * Small helper class that stores a server name and its total failure count.
     */
    private static class ServerCount {
        String name;
        int count;

        ServerCount(String name, int count) {
            this.name = name;
            this.count = count;
        }
    }

    /**
     * Returns the top k server names ordered by:
     * 1) higher failure count first
     * 2) if counts are equal, lexicographically smaller name first
     *
     * This method uses the intended efficient heap-based approach:
     * - First count how many times each server appears.
     * - Then maintain a min-heap of size at most k.
     *   The "worst" candidate among the current top k is kept at the root,
     *   so it can be removed quickly when a better candidate appears.
     *
     * Heap ordering details:
     * - Lower count is worse.
     * - If counts are equal, lexicographically larger name is worse.
     *
     * Why lexicographically larger is worse for equal counts:
     * Because the final desired order wants lexicographically smaller names first.
     * So among tied servers, the larger name should be removed earlier from the heap.
     *
     * @param reports the array of server failure reports; each element is a server name
     * @param k the maximum number of server names to return
     * @return a list containing up to k server names in the exact repair priority order
     * Time complexity: O(n + m log k), where n is reports.length and m is the number of distinct servers
     * Space complexity: O(m + k)
     */
    public List<String> topKFailedServers(String[] reports, int k) {
        // Step 1:
        // Count how many times each server name appears in the reports array.
        // This converts the raw stream of reports into a frequency table.
        Map<String, Integer> frequencyMap = new HashMap<>();
        for (String server : reports) {
            frequencyMap.put(server, frequencyMap.getOrDefault(server, 0) + 1);
        }

        // Step 2:
        // Build a min-heap that stores only the best k candidates seen so far.
        //
        // IMPORTANT:
        // The root of this heap should be the "worst" element among the current top k.
        // That way, when the heap grows beyond size k, we remove the root and keep only
        // the better candidates.
        //
        // "Worse" means:
        // - smaller count is worse
        // - if counts tie, lexicographically larger name is worse
        //
        // Therefore:
        // - count ascending
        // - name descending for tie
        PriorityQueue<ServerCount> minHeap = new PriorityQueue<>((a, b) -> {
            if (a.count != b.count) {
                return Integer.compare(a.count, b.count);
            }
            return b.name.compareTo(a.name);
        });

        // Step 3:
        // Iterate through each distinct server and push it into the heap.
        // If heap size becomes larger than k, remove the worst candidate.
        for (Map.Entry<String, Integer> entry : frequencyMap.entrySet()) {
            minHeap.offer(new ServerCount(entry.getKey(), entry.getValue()));

            // Keep heap size at most k.
            if (minHeap.size() > k) {
                minHeap.poll();
            }
        }

        // Step 4:
        // The heap now contains up to k best servers, but NOT in final output order.
        // Since this is a min-heap of the worst among the top k, polling from it gives
        // results from worst to best within the selected top k.
        //
        // So we extract all elements, then reverse the order to get:
        // - highest count first
        // - lexicographically smaller first when counts tie
        List<ServerCount> extracted = new ArrayList<>();
        while (!minHeap.isEmpty()) {
            extracted.add(minHeap.poll());
        }

        // Reverse to transform from worst->best into best->worst.
        Collections.reverse(extracted);

        // Step 5:
        // Build the final answer list containing only server names.
        List<String> result = new ArrayList<>();
        for (ServerCount serverCount : extracted) {
            result.add(serverCount.name);
        }

        return result;
    }

    /**
     * Alternative method that solves the same problem using full sorting.
     * This is simpler to understand, but less efficient than the heap approach
     * when the number of distinct servers is very large and k is small.
     *
     * Sorting order:
     * - higher count first
     * - if counts are equal, lexicographically smaller name first
     *
     * @param reports the array of server failure reports
     * @param k the maximum number of server names to return
     * @return a list containing up to k server names in repair priority order
     * Time complexity: O(n + m log m), where n is reports.length and m is the number of distinct servers
     * Space complexity: O(m)
     */
    public List<String> topKFailedServersBySorting(String[] reports, int k) {
        // Count frequencies exactly as in the heap solution.
        Map<String, Integer> frequencyMap = new HashMap<>();
        for (String server : reports) {
            frequencyMap.put(server, frequencyMap.getOrDefault(server, 0) + 1);
        }

        // Convert the map into a list of ServerCount objects so we can sort them.
        List<ServerCount> allServers = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : frequencyMap.entrySet()) {
            allServers.add(new ServerCount(entry.getKey(), entry.getValue()));
        }

        // Sort directly in the final desired order:
        // - count descending
        // - name ascending
        allServers.sort((a, b) -> {
            if (a.count != b.count) {
                return Integer.compare(b.count, a.count);
            }
            return a.name.compareTo(b.name);
        });

        // Take the first up to k names.
        List<String> result = new ArrayList<>();
        int limit = Math.min(k, allServers.size());
        for (int i = 0; i < limit; i++) {
            result.add(allServers.get(i).name);
        }

        return result;
    }

    /**
     * Utility method to print a labeled result list.
     *
     * @param label text label to print before the list
     * @param result the list of server names to print
     * @return nothing
     * Time complexity: O(r), where r is result.size()
     * Space complexity: O(1) auxiliary space
     */
    public void printResult(String label, List<String> result) {
        System.out.println(label + result);
    }

    /**
     * Demonstrates the solution using the sample inputs from the problem statement.
     *
     * Verified expected outputs:
     * Example 1:
     * reports = ["db-1","api-2","db-1","cache-7","api-2","db-1"], k = 2
     * counts:
     * - db-1 = 3
     * - api-2 = 2
     * - cache-7 = 1
     * result = ["db-1","api-2"]
     *
     * Example 2:
     * reports = ["node-b","node-a","node-b","node-a","node-c"], k = 3
     * counts:
     * - node-a = 2
     * - node-b = 2
     * - node-c = 1
     * tie between node-a and node-b is broken lexicographically:
     * "node-a" comes before "node-b"
     * result = ["node-a","node-b","node-c"]
     *
     * @param args command-line arguments, not used
     * @return nothing
     * Time complexity: O(1) for the driver itself, excluding called methods
     * Space complexity: O(1) auxiliary space for the driver itself
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Sample Input 1
        String[] reports1 = {"db-1", "api-2", "db-1", "cache-7", "api-2", "db-1"};
        int k1 = 2;
        List<String> result1 = solution.topKFailedServers(reports1, k1);
        System.out.println("Example 1 Output:");
        solution.printResult("", result1);
        System.out.println("Expected: [db-1, api-2]");
        System.out.println();

        // Sample Input 2
        String[] reports2 = {"node-b", "node-a", "node-b", "node-a", "node-c"};
        int k2 = 3;
        List<String> result2 = solution.topKFailedServers(reports2, k2);
        System.out.println("Example 2 Output:");
        solution.printResult("", result2);
        System.out.println("Expected: [node-a, node-b, node-c]");
        System.out.println();

        // Additional quick demonstration:
        // fewer distinct servers than k
        String[] reports3 = {"s1", "s2", "s1"};
        int k3 = 5;
        List<String> result3 = solution.topKFailedServers(reports3, k3);
        System.out.println("Additional Example Output:");
        solution.printResult("", result3);
        System.out.println("Expected: [s1, s2]");
    }
}