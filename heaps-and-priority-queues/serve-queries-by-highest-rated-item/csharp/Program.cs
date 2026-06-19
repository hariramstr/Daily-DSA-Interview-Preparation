/*
Title: Serve Queries by Highest Rated Item

Problem Description:
You are building a recommendation service for an online marketplace. There are n items, and each item belongs to exactly one category.
Every item has a current integer rating. The system must process live rating updates and answer queries asking for the highest-rated
available item in a given category.

Implement a data structure that supports the following operations:

1. update(itemId, newRating): Change the rating of the given item.
2. top(category): Return the itemId of the item in that category with the highest current rating.

If multiple items in the same category have the same highest rating, return the smallest itemId among them.

You are given three arrays: itemId[i], category[i], and rating[i], describing the initial items. Then you are given a list of operations.
Each operation is either ["update", itemId, newRating] or ["top", category]. For every "top" operation, return the answer in order.

A straightforward scan per query may be too slow because both the number of items and operations can be large. Design an efficient solution
using heaps / priority queues with lazy deletion or another equivalent approach.
*/

using System;
using System.Collections.Generic;

class Solution
{
    // We store one heap per category.
    // Each heap contains candidate entries for that category.
    // Because ratings can change over time, old heap entries become stale.
    // We do NOT remove stale entries immediately from the middle of the heap,
    // because that would be expensive.
    // Instead, we use "lazy deletion":
    // - On update: push a new entry with the new rating.
    // - On top query: keep removing heap top entries while they do not match
    //   the current rating of that item.
    //
    // This is a classic and efficient heap pattern for dynamic updates.

    private sealed class ItemState
    {
        public int Category;
        public int Rating;

        public ItemState(int category, int rating)
        {
            Category = category;
            Rating = rating;
        }
    }

    private readonly struct HeapEntry
    {
        public readonly int ItemId;
        public readonly int Rating;

        public HeapEntry(int itemId, int rating)
        {
            ItemId = itemId;
            Rating = rating;
        }
    }

    private sealed class HeapEntryComparer : IComparer<HeapEntry>
    {
        public int Compare(HeapEntry a, HeapEntry b)
        {
            // We want the "best" item to come first in the heap.
            // .NET PriorityQueue is a min-heap, so smaller priority values come out first.
            //
            // To make higher rating better:
            // - Compare by rating descending => larger rating should be considered "smaller" priority.
            //
            // To break ties:
            // - Smaller itemId is better.
            //
            // Therefore:
            // 1) Higher rating comes first
            // 2) If same rating, smaller itemId comes first
            if (a.Rating != b.Rating)
            {
                return b.Rating.CompareTo(a.Rating);
            }

            return a.ItemId.CompareTo(b.ItemId);
        }
    }

    private sealed class HighestRatedService
    {
        // itemId -> current state (category + latest rating)
        private readonly Dictionary<int, ItemState> _itemState = new();

        // category -> heap of candidate entries for that category
        private readonly Dictionary<int, PriorityQueue<HeapEntry, HeapEntry>> _categoryHeaps = new();

        private static readonly HeapEntryComparer ComparerInstance = new();

        public HighestRatedService(int[] itemId, int[] category, int[] rating)
        {
            for (int i = 0; i < itemId.Length; i++)
            {
                int id = itemId[i];
                int cat = category[i];
                int rate = rating[i];

                _itemState[id] = new ItemState(cat, rate);

                if (!_categoryHeaps.TryGetValue(cat, out var heap))
                {
                    heap = new PriorityQueue<HeapEntry, HeapEntry>(ComparerInstance);
                    _categoryHeaps[cat] = heap;
                }

                var entry = new HeapEntry(id, rate);
                heap.Enqueue(entry, entry);
            }
        }

        public void Update(int itemId, int newRating)
        {
            // Step 1:
            // Find the current state of this item.
            // We need its category because the item always stays in the same category,
            // and the new heap entry must be inserted into that category's heap.
            var state = _itemState[itemId];

            // Step 2:
            // Update the authoritative current rating in the dictionary.
            // This dictionary is the source of truth.
            // Heap entries are only candidates and may become stale after updates.
            state.Rating = newRating;

            // Step 3:
            // Push a fresh heap entry into the category heap.
            // We do not try to remove the old entry from the heap because removing an arbitrary
            // element from a heap is not efficient in standard priority queue implementations.
            // Lazy deletion will clean it up later when it reaches the top.
            var entry = new HeapEntry(itemId, newRating);
            _categoryHeaps[state.Category].Enqueue(entry, entry);
        }

        public int Top(int category)
        {
            // Step 1:
            // Get the heap for this category.
            // The problem guarantees that queried categories contain at least one item initially.
            var heap = _categoryHeaps[category];

            // Step 2:
            // Repeatedly inspect the top of the heap.
            // The top is the best candidate according to:
            // - highest rating
            // - if tie, smallest itemId
            //
            // However, because of lazy deletion, the top entry might be stale.
            // A stale entry means:
            // - the item still exists, but its current rating in _itemState is different
            //   from the rating stored in this heap entry.
            //
            // If stale, remove it and continue.
            while (true)
            {
                var best = heap.Peek();

                // Look up the current authoritative state of this item.
                var current = _itemState[best.ItemId];

                // If the heap entry's rating matches the current rating,
                // then this entry is valid and represents the true current state.
                // Because the heap ordering is correct, this is the answer.
                if (current.Rating == best.Rating)
                {
                    return best.ItemId;
                }

                // Otherwise, this heap entry is outdated.
                // Remove it and continue searching for the first valid top entry.
                heap.Dequeue();
            }
        }
    }

    /*
    Time Complexity:
    - Initialization: O(n log n) in the worst case overall due to heap insertions
    - update: O(log m), where m is the number of entries currently in that category heap
    - top: Amortized O(log m)
      Explanation:
      Each stale heap entry is inserted once and removed at most once later,
      so across all operations the total cleanup work is linear in the number of inserted entries.

    Space Complexity:
    - O(n + u), where u is the number of updates
      Because each update adds one new heap entry, and stale entries remain until lazily removed.
    */
    public IList<int> ProcessQueries(int[] itemId, int[] category, int[] rating, IList<IList<string>> operations)
    {
        // Step 1:
        // Build the service from the initial arrays.
        // This prepares:
        // - the current state dictionary
        // - one heap per category
        var service = new HighestRatedService(itemId, category, rating);

        // Step 2:
        // Prepare the output list.
        // We only append answers for "top" operations.
        var answers = new List<int>();

        // Step 3:
        // Process each operation in order.
        // The operation format is:
        // - ["update", itemId, newRating]
        // - ["top", category]
        foreach (var op in operations)
        {
            string type = op[0];

            if (type == "update")
            {
                // Parse the item id and new rating.
                int id = int.Parse(op[1]);
                int newRating = int.Parse(op[2]);

                // Apply the update.
                service.Update(id, newRating);
            }
            else
            {
                // Parse the category and answer the query.
                int cat = int.Parse(op[1]);
                int bestItemId = service.Top(cat);
                answers.Add(bestItemId);
            }
        }

        // Step 4:
        // Return all answers in the same order as the "top" queries appeared.
        return answers;
    }
}

// -------------------------
// Demo code
// -------------------------

var solution = new Solution();

// Example 1
int[] itemId1 = { 10, 11, 12, 13 };
int[] category1 = { 1, 1, 2, 1 };
int[] rating1 = { 5, 7, 9, 7 };

IList<IList<string>> operations1 = new List<IList<string>>
{
    new List<string> { "top", "1" },
    new List<string> { "update", "10", "8" },
    new List<string> { "top", "1" },
    new List<string> { "update", "11", "8" },
    new List<string> { "top", "1" },
    new List<string> { "top", "2" }
};

var result1 = solution.ProcessQueries(itemId1, category1, rating1, operations1);
Console.WriteLine("Example 1 Output: [" + string.Join(", ", result1) + "]");
// Expected: [11, 10, 10, 12]

// Example 2
int[] itemId2 = { 21, 22, 23, 24, 25 };
int[] category2 = { 3, 3, 4, 4, 3 };
int[] rating2 = { 6, 6, 10, 8, 9 };

IList<IList<string>> operations2 = new List<IList<string>>
{
    new List<string> { "top", "3" },
    new List<string> { "update", "22", "11" },
    new List<string> { "top", "3" },
    new List<string> { "update", "25", "11" },
    new List<string> { "top", "3" },
    new List<string> { "top", "4" }
};

var result2 = solution.ProcessQueries(itemId2, category2, rating2, operations2);
Console.WriteLine("Example 2 Output: [" + string.Join(", ", result2) + "]");
// Expected: [25, 22, 22, 23]