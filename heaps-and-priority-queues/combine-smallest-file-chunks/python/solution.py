"""
Title: Combine Smallest File Chunks

Problem Description:
You are given an array `chunks` where `chunks[i]` is the size of the `i-th` file chunk
waiting to be merged into one final archive. In one operation, you must take the two
smallest available chunks, merge them, and pay a cost equal to the sum of their sizes.
The merged chunk is then added back to the pool of available chunks. Continue until only
one chunk remains.

Return the total cost of all merge operations.

This models a common systems task: repeatedly combining small pieces of data where each
intermediate merge also creates a new piece that may be merged again later. To minimize
the total cost, you should always merge the smallest available chunks first.

Your task is to implement a function that computes this minimum total merge cost.

Constraints:
- 1 <= chunks.length <= 100000
- 1 <= chunks[i] <= 1000000000
- The answer may not fit in a 32-bit integer, so use 64-bit arithmetic where needed.

Example 1:
Input: chunks = [4, 3, 2, 6]
Output: 29
Explanation:
- Merge 2 and 3 -> cost 5, chunks become [4, 5, 6]
- Merge 4 and 5 -> cost 9, chunks become [6, 9]
- Merge 6 and 9 -> cost 15
Total cost = 5 + 9 + 15 = 29

Example 2:
Input: chunks = [10]
Output: 0
Explanation:
There is only one chunk, so no merge is needed.

A solution using sorting once is not enough, because after each merge a new chunk is
created and must be placed back among the remaining chunk sizes efficiently. This makes
a min-heap or priority queue a natural choice.
"""

from typing import List
import heapq


class Solution:
    def min_merge_cost(self, chunks: List[int]) -> int:
        """
        Compute the minimum total cost to merge all file chunks into one chunk.

        The algorithm uses a min-heap so that the two smallest chunks can always be
        retrieved efficiently. This greedy strategy is optimal for minimizing the
        total merge cost.

        Args:
            chunks: A list of positive integers representing chunk sizes.

        Returns:
            The minimum total cost of all merge operations.

        Time complexity:
            O(n log n), where n is the number of chunks.
            - Building the heap takes O(n)
            - Each merge performs heap operations costing O(log n)
            - There are n - 1 merges

        Space complexity:
            O(n), for the heap used to store chunk sizes.
        """
        # If there is only one chunk, no merge is needed.
        # Therefore, the total cost is zero.
        if len(chunks) <= 1:
            return 0

        # We create a copy of the input list so that we do not modify the caller's data.
        # This is a beginner-friendly and safer practice unless in-place modification is desired.
        min_heap: List[int] = list(chunks)

        # Transform the list into a min-heap in O(n) time.
        # A min-heap always keeps the smallest element at index 0,
        # which allows us to efficiently remove the smallest chunk.
        heapq.heapify(min_heap)

        # This variable stores the running total of all merge costs.
        # Python integers automatically support large values, so this safely handles
        # results that exceed 32-bit integer range.
        total_cost: int = 0

        # We continue merging until only one chunk remains.
        # Why? Because the goal is to combine all chunks into one final archive.
        while len(min_heap) > 1:
            # Remove the smallest available chunk.
            # heapq.heappop() runs in O(log n).
            first_smallest: int = heapq.heappop(min_heap)

            # Remove the next smallest available chunk.
            second_smallest: int = heapq.heappop(min_heap)

            # Merging these two chunks costs the sum of their sizes.
            merged_size: int = first_smallest + second_smallest

            # Add this merge cost to the total answer.
            total_cost += merged_size

            # The merged chunk becomes a new available chunk that may be merged again later.
            # We push it back into the min-heap so it is placed in the correct order.
            heapq.heappush(min_heap, merged_size)

        # At this point, exactly one chunk remains in the heap,
        # meaning all original chunks have been merged.
        return total_cost

    def trace_examples(self) -> None:
        """
        Run and print the example cases from the problem statement.

        This helper method is included to make the solution easy to verify and
        beginner-friendly.

        Args:
            None

        Returns:
            None

        Time complexity:
            O(k log k) per example, where k is the number of chunks in that example.

        Space complexity:
            O(k) per example, due to heap usage.
        """
        examples: List[List[int]] = [
            [4, 3, 2, 6],
            [10],
        ]

        for chunks in examples:
            result: int = self.min_merge_cost(chunks)
            print(f"chunks = {chunks} -> total merge cost = {result}")


if __name__ == "__main__":
    solution = Solution()

    # Sample inputs from the problem description.
    sample_chunks_1: List[int] = [4, 3, 2, 6]
    sample_chunks_2: List[int] = [10]

    # Call the solution and print the results.
    result_1: int = solution.min_merge_cost(sample_chunks_1)
    result_2: int = solution.min_merge_cost(sample_chunks_2)

    print("Sample Run Results:")
    print(f"Input: {sample_chunks_1}")
    print(f"Output: {result_1}")
    print()

    print(f"Input: {sample_chunks_2}")
    print(f"Output: {result_2}")
    print()

    # Additional explicit verification against the expected outputs.
    print("Verification:")
    print(f"Expected: 29, Got: {result_1}")
    print(f"Expected: 0, Got: {result_2}")
    print()

    # Optional helper demonstration.
    print("Tracing problem examples:")
    solution.trace_examples()