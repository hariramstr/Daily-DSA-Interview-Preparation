import java.util.*;

/*
Title: Serve Queries by Highest Rated Item
Difficulty: Medium
Topic: Heaps and Priority Queues

Problem Description:
You are building a recommendation service for an online marketplace. There are n items, and each item belongs to exactly one category. Every item has a current integer rating. The system must process live rating updates and answer queries asking for the highest-rated available item in a given category.

Implement a data structure that supports the following operations:

1. update(itemId, newRating): Change the rating of the given item.
2. top(category): Return the itemId of the item in that category with the highest current rating.

If multiple items in the same category have the same highest rating, return the smallest itemId among them.

You are given three arrays: itemId[i], category[i], and rating[i], describing the initial items. Then you are given a list of operations. Each operation is either ["update", itemId, newRating] or ["top", category]. For every "top" operation, return the answer in order.

A straightforward scan per query may be too slow because both the number of items and operations can be large. Design an efficient solution using heaps / priority queues with lazy deletion or another equivalent approach.

Constraints:
- 1 <= n <= 2 * 10^5
- 1 <= number of operations <= 2 * 10^5
- 1 <= itemId[i] <= 10^9, all itemId values are distinct
- 1 <= rating[i], newRating <= 10^9
- 1 <= category[i], category <= 10^5
- It is guaranteed that every queried category contains at least one item initially
- Updates only refer to existing itemIds

Example 1:
Input:
itemId = [10, 11, 12, 13]
category = [1, 1, 2, 1]
rating = [5, 7, 9, 7]
operations = [["top", 1], ["update", 10, 8], ["top", 1], ["update", 11, 8], ["top", 1], ["top", 2]]
Output:
[11, 10, 10, 12]

Example 2:
Input:
itemId = [21, 22, 23, 24, 25]
category = [3, 3, 4, 4, 3]
rating = [6, 6, 10, 8, 9]
operations = [["top", 3], ["update", 22, 11], ["top", 3], ["update", 25, 11], ["top", 3], ["top", 4]]
Output:
[25, 22, 22, 23]
*/

public class Solution {

    /**
     * A heap entry representing one snapshot of an item's state at some moment in time.
     *
     * Important idea:
     * We use "lazy deletion". That means when an item's rating changes, we do NOT search
     * inside the heap and remove the old entry, because removing an arbitrary element from
     * a Java PriorityQueue is too slow for this problem.
     *
     * Instead, we simply push a new entry with the updated rating.
     * Later, when we ask for the top item of a category, we repeatedly discard heap entries
     * that are no longer current.
     */
    private static class ItemEntry {
        int itemId;
        int rating;

        ItemEntry(int itemId, int rating) {
            this.itemId = itemId;
            this.rating = rating;
        }
    }

    /**
     * Core data structure that supports:
     * - updating an item's rating
     * - querying the highest-rated item in a category
     *
     * Internal state:
     * 1. itemToCategory: tells us which category an item belongs to
     * 2. itemToRating: tells us the current rating of each item
     * 3. categoryToHeap: for each category, a max-heap ordered by:
     *      - higher rating first
     *      - if tie, smaller itemId first
     */
    public static class HighestRatedItemService {
        private final Map<Integer, Integer> itemToCategory;
        private final Map<Integer, Integer> itemToRating;
        private final Map<Integer, PriorityQueue<ItemEntry>> categoryToHeap;

        /**
         * Builds the service from the initial arrays.
         *
         * @param itemIds array of unique item IDs
         * @param categories array where categories[i] is the category of itemIds[i]
         * @param ratings array where ratings[i] is the initial rating of itemIds[i]
         * @return no return value; constructs the data structure
         * Time complexity: O(n log n) in total because each insertion into a heap costs O(log n)
         * Space complexity: O(n) for maps and heaps
         */
        public HighestRatedItemService(int[] itemIds, int[] categories, int[] ratings) {
            this.itemToCategory = new HashMap<>();
            this.itemToRating = new HashMap<>();
            this.categoryToHeap = new HashMap<>();

            for (int i = 0; i < itemIds.length; i++) {
                int itemId = itemIds[i];
                int category = categories[i];
                int rating = ratings[i];

                itemToCategory.put(itemId, category);
                itemToRating.put(itemId, rating);

                categoryToHeap
                        .computeIfAbsent(category, k -> new PriorityQueue<>((a, b) -> {
                            if (a.rating != b.rating) {
                                return Integer.compare(b.rating, a.rating);
                            }
                            return Integer.compare(a.itemId, b.itemId);
                        }))
                        .offer(new ItemEntry(itemId, rating));
            }
        }

        /**
         * Updates the rating of an existing item.
         *
         * Detailed behavior:
         * - Find the item's category
         * - Update the current rating in itemToRating
         * - Push a fresh heap entry into that category's heap
         *
         * We do not remove the old heap entry immediately.
         * That old entry becomes "stale" and will be ignored later during top().
         *
         * @param itemId the item whose rating should change
         * @param newRating the new rating value
         * @return no return value
         * Time complexity: O(log m), where m is the number of heap entries in that category
         * Space complexity: O(1) extra per call, not counting the new lazy heap entry
         */
        public void update(int itemId, int newRating) {
            int category = itemToCategory.get(itemId);

            itemToRating.put(itemId, newRating);

            PriorityQueue<ItemEntry> heap = categoryToHeap.get(category);
            heap.offer(new ItemEntry(itemId, newRating));
        }

        /**
         * Returns the item ID of the highest-rated current item in the given category.
         *
         * Detailed behavior:
         * 1. Look at the heap top.
         * 2. Check whether that heap entry still matches the item's current rating.
         * 3. If it does not match, it is stale, so remove it and continue.
         * 4. The first valid entry we find is the correct answer because the heap ordering is:
         *      - highest rating first
         *      - among ties, smallest itemId first
         *
         * Why this is correct:
         * Every time an item changes rating, we insert its new state into the heap.
         * Therefore, the current valid state of every item is always present somewhere in the heap.
         * Stale states may also exist, but we discard them lazily.
         *
         * @param category the category to query
         * @return the item ID with highest current rating in that category; if tied, the smallest item ID
         * Time complexity: Amortized O(log m), where m is the number of heap entries in that category
         * Space complexity: O(1) extra
         */
        public int top(int category) {
            PriorityQueue<ItemEntry> heap = categoryToHeap.get(category);

            while (true) {
                ItemEntry best = heap.peek();

                int currentRating = itemToRating.get(best.itemId);

                if (best.rating == currentRating) {
                    return best.itemId;
                }

                heap.poll();
            }
        }
    }

    /**
     * Processes a list of operations using the required data structure.
     *
     * Operation format:
     * - ["update", itemId, newRating]
     * - ["top", category]
     *
     * For every "top" operation, we append the answer to the result list.
     *
     * @param itemIds initial item IDs
     * @param categories initial categories
     * @param ratings initial ratings
     * @param operations list of operations, each represented as a String array
     * @return list of answers for all "top" operations in order
     * Time complexity: O((n + q) log(n + q)) amortized overall
     * Space complexity: O(n + q) in the worst case due to lazy heap entries
     */
    public List<Integer> processOperations(int[] itemIds, int[] categories, int[] ratings, String[][] operations) {
        HighestRatedItemService service = new HighestRatedItemService(itemIds, categories, ratings);
        List<Integer> answers = new ArrayList<>();

        for (String[] operation : operations) {
            String type = operation[0];

            if ("update".equals(type)) {
                int itemId = Integer.parseInt(operation[1]);
                int newRating = Integer.parseInt(operation[2]);
                service.update(itemId, newRating);
            } else if ("top".equals(type)) {
                int category = Integer.parseInt(operation[1]);
                answers.add(service.top(category));
            }
        }

        return answers;
    }

    /**
     * Utility method to print a list in a clean format.
     *
     * @param values list of integers to print
     * @return no return value
     * Time complexity: O(k), where k is the number of values
     * Space complexity: O(1) extra
     */
    public static void printList(List<Integer> values) {
        System.out.println(values);
    }

    /**
     * Demonstrates the solution on the sample inputs from the problem statement.
     *
     * Verified expected outputs:
     * Example 1 -> [11, 10, 10, 12]
     * Example 2 -> [25, 22, 22, 23]
     *
     * @param args command-line arguments, not used
     * @return no return value
     * Time complexity: O(total operations log n) for the demonstrations
     * Space complexity: O(n + q)
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        int[] itemId1 = {10, 11, 12, 13};
        int[] category1 = {1, 1, 2, 1};
        int[] rating1 = {5, 7, 9, 7};
        String[][] operations1 = {
                {"top", "1"},
                {"update", "10", "8"},
                {"top", "1"},
                {"update", "11", "8"},
                {"top", "1"},
                {"top", "2"}
        };

        List<Integer> result1 = solution.processOperations(itemId1, category1, rating1, operations1);
        printList(result1);

        int[] itemId2 = {21, 22, 23, 24, 25};
        int[] category2 = {3, 3, 4, 4, 3};
        int[] rating2 = {6, 6, 10, 8, 9};
        String[][] operations2 = {
                {"top", "3"},
                {"update", "22", "11"},
                {"top", "3"},
                {"update", "25", "11"},
                {"top", "3"},
                {"top", "4"}
        };

        List<Integer> result2 = solution.processOperations(itemId2, category2, rating2, operations2);
        printList(result2);
    }
}