"""
Title: Serve Queries by Highest Rated Item

Problem Description:
You are building a recommendation service for an online marketplace. There are n items,
and each item belongs to exactly one category. Every item has a current integer rating.
The system must process live rating updates and answer queries asking for the highest-rated
available item in a given category.

Implement a data structure that supports the following operations:

1. update(itemId, newRating): Change the rating of the given item.
2. top(category): Return the itemId of the item in that category with the highest current rating.

If multiple items in the same category have the same highest rating, return the smallest itemId among them.

You are given three arrays: itemId[i], category[i], and rating[i], describing the initial items.
Then you are given a list of operations. Each operation is either ["update", itemId, newRating]
or ["top", category]. For every "top" operation, return the answer in order.

A straightforward scan per query may be too slow because both the number of items and operations
can be large. Design an efficient solution using heaps / priority queues with lazy deletion or
another equivalent approach.
"""

from typing import Dict, List, Tuple
import heapq


class HighestRatedItems:
    """
    Data structure for maintaining highest-rated items per category with fast updates and queries.
    """

    def __init__(self, item_ids: List[int], categories: List[int], ratings: List[int]) -> None:
        """
        Initialize the data structure.

        Args:
            item_ids: List of unique item IDs.
            categories: List where categories[i] is the category of item_ids[i].
            ratings: List where ratings[i] is the current rating of item_ids[i].

        Returns:
            None

        Time complexity:
            O(n log n), because each item is inserted into a heap once.

        Space complexity:
            O(n), for maps and heaps.
        """
        # Map each item to its category.
        # We need this during updates so we know which category heap should receive
        # the new rating record for that item.
        self.item_to_category: Dict[int, int] = {}

        # Map each item to its current rating.
        # This is the source of truth for whether a heap entry is still valid.
        self.item_to_rating: Dict[int, int] = {}

        # For each category, maintain a heap of candidates.
        # Python's heapq is a min-heap, so to simulate "highest rating first",
        # we store (-rating, item_id).
        #
        # Why this works:
        # - Larger rating becomes smaller negative number, so it comes first.
        # - If ratings tie, smaller item_id comes first naturally.
        self.category_heaps: Dict[int, List[Tuple[int, int]]] = {}

        # Build all structures from the initial arrays.
        for item_id, category, rating in zip(item_ids, categories, ratings):
            self.item_to_category[item_id] = category
            self.item_to_rating[item_id] = rating

            if category not in self.category_heaps:
                self.category_heaps[category] = []

            heapq.heappush(self.category_heaps[category], (-rating, item_id))

    def update(self, item_id: int, new_rating: int) -> None:
        """
        Update the rating of an existing item.

        Args:
            item_id: The item whose rating should be changed.
            new_rating: The new rating value.

        Returns:
            None

        Time complexity:
            O(log n) amortized, due to one heap push.

        Space complexity:
            O(1) extra per call, not counting heap growth from lazy deletion.
        """
        # Find the category of this item so we know which heap to update.
        category = self.item_to_category[item_id]

        # Update the authoritative current rating.
        self.item_to_rating[item_id] = new_rating

        # Push the new state into the category heap.
        #
        # Important design choice: we do NOT search the heap to remove the old entry.
        # Removing arbitrary elements from a heap is expensive.
        #
        # Instead, we use "lazy deletion":
        # - old heap entries remain in the heap
        # - when top() is called, we discard stale entries until the heap top matches
        #   the current rating stored in item_to_rating
        heapq.heappush(self.category_heaps[category], (-new_rating, item_id))

    def top(self, category: int) -> int:
        """
        Return the item ID of the highest-rated item in the given category.
        If ratings tie, return the smallest item ID.

        Args:
            category: The category to query.

        Returns:
            The best item ID in that category.

        Time complexity:
            O(log n) amortized. Stale entries are popped only once overall.

        Space complexity:
            O(1) extra.
        """
        heap = self.category_heaps[category]

        # Keep removing the heap top while it is stale.
        #
        # A heap entry (-r, item_id) is valid only if:
        # current rating of item_id == r
        #
        # If not, that means the item was updated later, and this heap entry is outdated.
        while heap:
            neg_rating, item_id = heap[0]
            current_rating = self.item_to_rating[item_id]

            # Convert back from negative to positive rating.
            if -neg_rating == current_rating:
                # This is the current best valid item for the category.
                return item_id

            # Otherwise, discard the stale entry and continue.
            heapq.heappop(heap)

        # Based on the problem constraints, every queried category always has at least one item,
        # so execution should never reach here.
        raise ValueError("No valid items found for the queried category.")


class Solution:
    """
    Solution wrapper class required by the problem statement.
    """

    def serve_queries(
        self,
        itemId: List[int],
        category: List[int],
        rating: List[int],
        operations: List[List[int]]
    ) -> List[int]:
        """
        Process update and top operations using heaps with lazy deletion.

        Args:
            itemId: List of unique item IDs.
            category: category[i] is the category of itemId[i].
            rating: rating[i] is the initial rating of itemId[i].
            operations: List of operations:
                - ["update", itemId, newRating]
                - ["top", category]

        Returns:
            A list of answers for every "top" operation, in order.

        Time complexity:
            O((n + q) log n) amortized, where:
            - n is the number of items
            - q is the number of operations

        Space complexity:
            O(n + q) in the worst case due to lazy heap entries from updates.
        """
        # Create the specialized data structure that supports:
        # - fast rating updates
        # - fast highest-rated queries per category
        data_structure = HighestRatedItems(itemId, category, rating)

        # Store answers for all "top" queries in the order they appear.
        answers: List[int] = []

        # Process each operation one by one.
        for operation in operations:
            operation_type = operation[0]

            # Handle rating update.
            if operation_type == "update":
                # operation format: ["update", itemId, newRating]
                item_id = int(operation[1])
                new_rating = int(operation[2])
                data_structure.update(item_id, new_rating)

            # Handle top query.
            elif operation_type == "top":
                # operation format: ["top", category]
                query_category = int(operation[1])
                answers.append(data_structure.top(query_category))

            else:
                raise ValueError(f"Unknown operation type: {operation_type}")

        return answers


if __name__ == "__main__":
    solution = Solution()

    # Example 1
    itemId_1 = [10, 11, 12, 13]
    category_1 = [1, 1, 2, 1]
    rating_1 = [5, 7, 9, 7]
    operations_1 = [
        ["top", 1],
        ["update", 10, 8],
        ["top", 1],
        ["update", 11, 8],
        ["top", 1],
        ["top", 2],
    ]
    result_1 = solution.serve_queries(itemId_1, category_1, rating_1, operations_1)
    print(result_1)  # Expected: [11, 10, 10, 12]

    # Example 2
    itemId_2 = [21, 22, 23, 24, 25]
    category_2 = [3, 3, 4, 4, 3]
    rating_2 = [6, 6, 10, 8, 9]
    operations_2 = [
        ["top", 3],
        ["update", 22, 11],
        ["top", 3],
        ["update", 25, 11],
        ["top", 3],
        ["top", 4],
    ]
    result_2 = solution.serve_queries(itemId_2, category_2, rating_2, operations_2)
    print(result_2)  # Expected: [25, 22, 22, 23]