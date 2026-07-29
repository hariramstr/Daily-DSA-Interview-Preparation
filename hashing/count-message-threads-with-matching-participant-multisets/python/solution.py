"""
Title: Count Message Threads With Matching Participant Multisets

Problem Description:
You are given chat logs from a messaging platform. Each thread is represented by a
list of user IDs in the order messages were sent. A user may appear multiple times
in the same thread if they sent multiple messages. Two threads are considered
equivalent if they contain exactly the same multiset of participants, meaning every
user ID appears the same number of times in both threads, regardless of message order.

Your task is to count how many unordered pairs of threads are equivalent.

Formally, let threads[i] be the list of user IDs in the i-th thread. Threads i and j
are equivalent if for every user ID x, the number of occurrences of x in threads[i]
equals the number of occurrences of x in threads[j]. Return the number of pairs
(i, j) such that 0 <= i < j < n and threads[i] and threads[j] are equivalent.

Because user IDs can be very large and each thread may contain repeated IDs, a naive
comparison of every pair will be too slow. You need to design a hashing-based
representation that uniquely identifies the participant multiset of each thread.

Constraints:
- 1 <= n <= 100000
- 1 <= total number of user IDs across all threads <= 300000
- 1 <= threads[i].length <= 100000
- Sum of all threads[i].length over all threads is at most 300000
- 1 <= user ID <= 10^9
- Return the answer as a 64-bit integer
"""

from collections import Counter, defaultdict
from typing import DefaultDict, Dict, List, Tuple


class Solution:
    def _build_signature(self, thread: List[int]) -> Tuple[Tuple[int, int], ...]:
        """
        Build a canonical hashable representation of one thread's participant multiset.

        The key idea is:
        - We do NOT care about message order.
        - We DO care about how many times each user ID appears.
        - Therefore, we count frequencies and then sort by user ID so that equivalent
          multisets always produce exactly the same representation.

        Example:
        thread = [4, 1, 4, 2]
        frequency map = {4: 2, 1: 1, 2: 1}
        sorted items = [(1, 1), (2, 1), (4, 2)]
        signature = ((1, 1), (2, 1), (4, 2))

        Args:
            thread: A single thread represented as a list of user IDs.

        Returns:
            A tuple of (user_id, count) pairs sorted by user_id.
            This tuple is immutable and hashable, so it can be used as a dictionary key.

        Time complexity:
            O(m + k log k), where:
            - m is the length of the thread
            - k is the number of distinct user IDs in the thread

        Space complexity:
            O(k), for the frequency map and the resulting signature
        """
        # Step 1:
        # Count how many times each user ID appears in this thread.
        #
        # Why use Counter?
        # - It is a built-in dictionary subclass specialized for frequency counting.
        # - It makes the code shorter and clearer.
        # - Since we need exact multiplicities, frequency counting is the natural tool.
        counts: Counter[int] = Counter(thread)

        # Step 2:
        # Convert the frequency map into a sorted tuple of pairs.
        #
        # Why sort?
        # - Dictionaries do not represent a canonical multiset ordering.
        # - Two equivalent threads may encounter IDs in different orders.
        # - Sorting ensures the same multiset always becomes the same final signature.
        #
        # Why tuple instead of list?
        # - Tuples are immutable and hashable.
        # - That means we can safely use the signature as a dictionary key.
        signature: Tuple[Tuple[int, int], ...] = tuple(sorted(counts.items()))

        return signature

    def count_equivalent_threads(self, threads: List[List[int]]) -> int:
        """
        Count how many unordered pairs of threads have identical participant multisets.

        The algorithm:
        1. Convert each thread into a canonical signature:
           sorted tuple of (user_id, frequency).
        2. Count how many times each signature appears.
        3. If a signature appears f times, it contributes f * (f - 1) // 2 pairs.
        4. Sum contributions across all signatures.

        This avoids comparing every pair of threads directly, which would be too slow.

        Args:
            threads: A list of threads, where each thread is a list of user IDs.

        Returns:
            The number of unordered equivalent-thread pairs as an integer.

        Time complexity:
            Let T be the total number of user IDs across all threads.
            Let k_i be the number of distinct IDs in thread i.
            Overall complexity is:
            O(T + sum(k_i log k_i))
            This is efficient under the given constraints.

        Space complexity:
            O(T) in the worst case, due to stored signatures and frequency maps.
        """
        # This dictionary stores:
        # signature -> how many threads have this exact participant multiset
        #
        # Example:
        # {
        #   ((1, 1), (2, 1), (4, 2)): 3,
        #   ((3, 2),): 1,
        #   ((3, 3),): 1
        # }
        signature_count: DefaultDict[Tuple[Tuple[int, int], ...], int] = defaultdict(int)

        # Process each thread independently.
        for thread in threads:
            # Build the canonical representation for this thread.
            signature: Tuple[Tuple[int, int], ...] = self._build_signature(thread)

            # Record that we have seen one more thread with this signature.
            signature_count[signature] += 1

        # Now compute the number of unordered pairs.
        #
        # If a particular signature appears f times, then the number of ways to choose
        # 2 threads from those f is:
        #
        #   C(f, 2) = f * (f - 1) // 2
        #
        # We sum this over all signatures.
        total_pairs: int = 0

        for frequency in signature_count.values():
            total_pairs += frequency * (frequency - 1) // 2

        return total_pairs


if __name__ == "__main__":
    solution = Solution()

    # Example 1
    threads1: List[List[int]] = [
        [4, 1, 4, 2],
        [2, 4, 4, 1],
        [3, 3],
        [1, 2, 4, 4],
        [3, 3, 3],
    ]
    result1: int = solution.count_equivalent_threads(threads1)
    print(result1)  # Expected: 3

    # Example 2
    threads2: List[List[int]] = [
        [8, 9],
        [9, 8, 8],
        [7],
        [8, 9],
        [7],
        [9, 8],
    ]
    result2: int = solution.count_equivalent_threads(threads2)
    print(result2)  # Expected: 4