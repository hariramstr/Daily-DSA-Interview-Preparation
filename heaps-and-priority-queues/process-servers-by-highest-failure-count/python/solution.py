"""
Title: Process Servers by Highest Failure Count

Problem Description:
You are given a stream of server failure reports from a monitoring system. Each report
contains the name of a server that just failed. After processing all reports, the
operations team wants to repair servers in priority order.

A server with a higher total number of failures should be repaired earlier. If two
servers have the same number of failures, the server with the lexicographically smaller
name should come first.

Your task is to return the names of the top k servers in the exact order they should be
repaired.

This problem is designed for an efficient solution using a heap or priority queue.
A straightforward full sort may work for small inputs, but the intended approach should
scale well when the number of distinct servers is large and k is much smaller than that
number.

Return a list of up to k server names. If there are fewer than k distinct servers,
return all of them in the required order.

Constraints:
- 1 <= reports.length <= 200000
- 1 <= reports[i].length <= 30
- reports[i] consists of lowercase English letters, digits, and hyphens
- 1 <= k <= 200000
- Server names are case-sensitive only if your language treats strings that way;
  assume all input names are already normalized
"""

from __future__ import annotations

import heapq
from collections import Counter
from typing import Dict, List, Tuple


class _ServerEntry:
    """
    Helper object used inside the heap.

    This class defines a custom ordering so that the "worst" candidate among the current
    top-k entries is kept at the root of the min-heap.

    Heap ordering rules for this helper:
    - Smaller failure count is considered smaller (worse for top-k retention).
    - If counts are equal, lexicographically larger name is considered smaller
      (also worse, because lexicographically smaller names should rank earlier).

    That means:
    - The root of the heap is always the current weakest entry among the kept top-k.
    - When a better candidate appears, it can replace the root efficiently.
    """

    def __init__(self, count: int, name: str) -> None:
        self.count = count
        self.name = name

    def __lt__(self, other: "_ServerEntry") -> bool:
        """
        Define heap comparison.

        Args:
            other: Another heap entry.

        Returns:
            True if self should come before other in the min-heap.

        Time complexity:
            O(1)

        Space complexity:
            O(1)
        """
        if self.count != other.count:
            return self.count < other.count
        return self.name > other.name


class Solution:
    def top_k_failed_servers(self, reports: List[str], k: int) -> List[str]:
        """
        Return the top k server names ordered by:
        1) Higher failure count first
        2) Lexicographically smaller name first when counts tie

        The implementation uses:
        - A frequency map to count failures per server
        - A size-limited min-heap to keep only the best k servers seen so far

        Args:
            reports: List of server failure reports.
            k: Number of top servers to return.

        Returns:
            A list of up to k server names in the exact repair priority order.

        Time complexity:
            O(n + m log k + k log k)
            where:
            - n = number of reports
            - m = number of distinct servers

        Space complexity:
            O(m + k)
        """
        # Step 1: Count how many times each server appears in the report stream.
        #
        # Why we do this:
        # The final ranking depends on the total number of failures per server,
        # not on the order in which reports arrive. So the first essential task
        # is to aggregate all reports into a frequency table.
        #
        # Data structure choice:
        # Counter is a specialized dictionary from the collections module that
        # makes frequency counting concise and efficient.
        #
        # Example 1:
        # reports = ["db-1","api-2","db-1","cache-7","api-2","db-1"]
        # frequency becomes:
        # {
        #   "db-1": 3,
        #   "api-2": 2,
        #   "cache-7": 1
        # }
        frequency: Dict[str, int] = Counter(reports)

        # Step 2: Handle the simple edge case where k is non-positive.
        #
        # The problem constraints say k >= 1, so this is not required for valid input,
        # but adding this guard makes the method more robust and beginner-friendly.
        if k <= 0:
            return []

        # Step 3: Use a min-heap of size at most k to keep only the best k servers.
        #
        # Why a heap:
        # If the number of distinct servers is very large and k is much smaller,
        # we do not want to sort every distinct server if we can avoid it.
        #
        # Heap strategy:
        # - We keep the current top-k candidates in a min-heap.
        # - The root is the "worst" among those kept candidates.
        # - When a new candidate is better than the root, it replaces the root.
        #
        # "Better" means:
        # - Higher count is better
        # - If counts tie, lexicographically smaller name is better
        #
        # Our custom _ServerEntry ordering is intentionally reversed for tie names
        # so that lexicographically larger names are considered worse and rise to
        # the root when counts are equal.
        heap: List[_ServerEntry] = []

        # Step 4: Iterate through each distinct server and its failure count.
        #
        # For each server:
        # - If the heap has fewer than k items, push it directly.
        # - Otherwise compare it with the current worst kept candidate (heap[0]).
        # - If the new server is better, replace the root.
        for name, count in frequency.items():
            current_entry = _ServerEntry(count, name)

            if len(heap) < k:
                # The heap is not full yet, so every candidate belongs in the
                # current top-k set for now.
                heapq.heappush(heap, current_entry)
            else:
                # The heap is full, so we only keep this new candidate if it is
                # better than the current weakest candidate at heap[0].
                #
                # Because our heap stores the weakest candidate at the root,
                # the comparison "heap[0] < current_entry" means:
                # "Is the current root worse than the new candidate?"
                #
                # If yes, the new candidate deserves a place in the top-k.
                if heap[0] < current_entry:
                    heapq.heapreplace(heap, current_entry)

        # Step 5: The heap now contains up to k best servers, but not in final output order.
        #
        # Important:
        # A heap only guarantees that the smallest element is at index 0.
        # The remaining elements are not globally sorted.
        #
        # So we must sort the kept entries according to the final required ranking:
        # - Higher count first  => sort by -count
        # - Lexicographically smaller name first => sort by name
        sorted_top_entries: List[_ServerEntry] = sorted(
            heap,
            key=lambda entry: (-entry.count, entry.name),
        )

        # Step 6: Extract only the server names in the required order.
        result: List[str] = [entry.name for entry in sorted_top_entries]

        # Correctness check against the provided examples:
        #
        # Example 1:
        # reports = ["db-1","api-2","db-1","cache-7","api-2","db-1"], k = 2
        # counts: db-1=3, api-2=2, cache-7=1
        # sorted order: ["db-1", "api-2", "cache-7"]
        # top 2 => ["db-1", "api-2"]
        #
        # Example 2:
        # reports = ["node-b","node-a","node-b","node-a","node-c"], k = 3
        # counts: node-b=2, node-a=2, node-c=1
        # tie between node-a and node-b resolved lexicographically:
        # "node-a" comes before "node-b"
        # final => ["node-a", "node-b", "node-c"]
        #
        # This matches the expected outputs exactly.
        return result


if __name__ == "__main__":
    solution = Solution()

    # Sample Input 1
    reports_1: List[str] = ["db-1", "api-2", "db-1", "cache-7", "api-2", "db-1"]
    k_1: int = 2
    result_1: List[str] = solution.top_k_failed_servers(reports_1, k_1)
    print("Example 1 Output:", result_1)

    # Sample Input 2
    reports_2: List[str] = ["node-b", "node-a", "node-b", "node-a", "node-c"]
    k_2: int = 3
    result_2: List[str] = solution.top_k_failed_servers(reports_2, k_2)
    print("Example 2 Output:", result_2)

    # Additional demonstration:
    # Fewer distinct servers than k
    reports_3: List[str] = ["web-1", "web-1", "db-2"]
    k_3: int = 5
    result_3: List[str] = solution.top_k_failed_servers(reports_3, k_3)
    print("Additional Example Output:", result_3)