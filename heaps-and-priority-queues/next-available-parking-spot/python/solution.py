"""
Title: Next Available Parking Spot

Problem Description:
A parking garage tracks which numbered spots are currently free. Spot numbers are
positive integers, and smaller numbers are closer to the entrance. You are given
an integer n representing spots numbered from 1 to n, and an array occupied
containing the spot numbers that are already taken when the day begins. After
that, a sequence of operations arrives.

Each operation is one of two types:
- ["park"]: assign the smallest-numbered free spot and return its number.
  If no spot is free, return -1.
- ["leave", x]: mark spot x as free again. It is guaranteed that x is currently
  occupied when this operation appears.

Return an array containing the result of every "park" operation in order.

The solution should be efficient enough to handle many operations. A common
approach is to keep all currently free spots in a min-heap so the nearest
available spot can be assigned quickly.
"""

from typing import List
import heapq


class Solution:
    def next_available_parking_spot(
        self, n: int, occupied: List[int], operations: List[List[int | str]]
    ) -> List[int]:
        """
        Process parking and leaving operations and return results of all "park" operations.

        Args:
            n: Total number of parking spots, numbered from 1 to n.
            occupied: List of spots that are already occupied initially.
            operations: Sequence of operations, where each operation is either
                ["park"] or ["leave", x].

        Returns:
            A list containing the assigned spot number for each "park" operation,
            or -1 when no free spot exists.

        Time complexity:
            O(n + k log n), where k is the number of operations.
            - Building the initial free-spot heap takes O(n).
            - Each "park" or "leave" operation uses heap operations in O(log n).

        Space complexity:
            O(n), for the occupied set, free-spot set, and min-heap.
        """
        # We store initially occupied spots in a set because:
        # 1. Membership checks are very fast on average: O(1)
        # 2. We need to quickly determine which spots are free when building the heap
        occupied_set = set(occupied)

        # This list will become our min-heap of currently free spots.
        # A min-heap always lets us remove the smallest element efficiently,
        # which is exactly what the "park" operation needs.
        free_heap: List[int] = []

        # We also keep a set of free spots.
        # Why do we need both a heap and a set?
        # - The heap gives us the smallest free spot quickly.
        # - The set lets us know whether a spot is currently free or occupied.
        #
        # In this problem, "leave" is guaranteed to only happen for occupied spots,
        # so duplicates should not occur if we manage state correctly.
        # Still, keeping a set makes the logic explicit and beginner-friendly.
        free_set = set()

        # Build the initial collection of free spots.
        # Every spot from 1 to n that is NOT in occupied_set starts as free.
        for spot in range(1, n + 1):
            if spot not in occupied_set:
                free_heap.append(spot)
                free_set.add(spot)

        # Convert the list of free spots into a valid min-heap in O(n) time.
        heapq.heapify(free_heap)

        # This list stores the answer for every "park" operation in the order received.
        results: List[int] = []

        # Process each operation one by one.
        for operation in operations:
            # The first item tells us the operation type.
            action = operation[0]

            if action == "park":
                # "park" means:
                # assign the smallest-numbered free spot.
                #
                # If the heap is empty, there are no free spots left.
                if not free_heap:
                    results.append(-1)
                else:
                    # Remove the smallest free spot from the heap.
                    assigned_spot = heapq.heappop(free_heap)

                    # Since this spot is no longer free, remove it from free_set.
                    free_set.remove(assigned_spot)

                    # Add it to occupied_set to keep the current state accurate.
                    occupied_set.add(assigned_spot)

                    # Record the assigned spot in the output.
                    results.append(assigned_spot)

            else:
                # The only other valid operation is ["leave", x].
                # The problem guarantees x is currently occupied.
                spot_to_free = int(operation[1])

                # Remove the spot from occupied_set because it is no longer taken.
                occupied_set.remove(spot_to_free)

                # Add the spot back to the free set.
                free_set.add(spot_to_free)

                # Push it into the min-heap so it becomes available for future "park" calls.
                heapq.heappush(free_heap, spot_to_free)

        return results


if __name__ == "__main__":
    solution = Solution()

    # Example 1
    n1 = 5
    occupied1 = [2, 4]
    operations1 = [["park"], ["park"], ["leave", 2], ["park"], ["park"]]
    result1 = solution.next_available_parking_spot(n1, occupied1, operations1)
    print("Example 1 Output:", result1)
    print("Expected:", [1, 3, 2, 5])

    # Example 2
    n2 = 3
    occupied2 = [1, 2, 3]
    operations2 = [["park"], ["leave", 2], ["park"], ["park"]]
    result2 = solution.next_available_parking_spot(n2, occupied2, operations2)
    print("Example 2 Output:", result2)
    print("Expected:", [-1, 2, -1])