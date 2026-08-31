"""
Title: Find Players With Reordered Card Histories

Problem Description:
You are given the game histories of several players in an online card platform.
Each player history is represented by a list of card IDs in the exact order they
were drawn during a match. Two players are considered to have a
reordered-equivalent history if their histories contain the same multiset of
card IDs, even if the draw order is different.

For example:
- [4, 9, 4, 2] and [2, 4, 9, 4] are equivalent
- [4, 9, 2] is not equivalent to [4, 9, 4, 2] because the frequencies differ

Your task is to return all player IDs that belong to at least one
reordered-equivalent group. The result should be sorted in increasing order of
player ID.

Player IDs are 0-indexed based on their position in the input array. Each
history may have a different length. A player belongs to a reordered-equivalent
group if there exists at least one other player with exactly the same
card-frequency profile.

Constraints:
- 1 <= histories.length <= 100000
- 0 <= histories[i].length <= 100000
- 0 <= card IDs <= 1000000000
- The sum of all history lengths across all players does not exceed 200000
- Return the player IDs in sorted ascending order
"""

from collections import Counter, defaultdict
from typing import DefaultDict, Dict, List, Tuple


class Solution:
    def _build_signature(self, history: List[int]) -> Tuple[Tuple[int, int], ...]:
        """
        Build a canonical hashable signature for one player's history.

        The signature represents the frequency of every card ID in a way that is:
        - independent of original draw order
        - sensitive to frequency counts
        - hashable so it can be used as a dictionary key

        We do this by:
        1. Counting each card ID with Counter
        2. Sorting the (card_id, count) pairs
        3. Converting the sorted pairs into a tuple

        Example:
        history = [4, 9, 4, 2]
        Counter -> {4: 2, 9: 1, 2: 1}
        Sorted items -> [(2, 1), (4, 2), (9, 1)]
        Signature -> ((2, 1), (4, 2), (9, 1))

        Args:
            history: A single player's card draw history.

        Returns:
            A canonical tuple signature for the history.

        Time complexity:
            O(k + u log u), where:
            - k is the length of the history
            - u is the number of distinct card IDs in the history

        Space complexity:
            O(u), for the frequency map and signature
        """
        # Count how many times each card ID appears.
        # This removes the importance of order and keeps only frequency information,
        # which is exactly what the problem defines as equivalence.
        frequency: Counter[int] = Counter(history)

        # Sort the (card_id, count) pairs so that histories with the same frequencies
        # always produce the exact same ordering in the signature.
        #
        # Without sorting, two equivalent histories could produce dictionaries whose
        # iteration order differs, which would make direct comparison unreliable.
        sorted_items: List[Tuple[int, int]] = sorted(frequency.items())

        # Convert to tuple so the result becomes immutable and hashable.
        # Hashable objects can be used as dictionary keys, which lets us group
        # equivalent histories efficiently.
        return tuple(sorted_items)

    def find_reordered_equivalent_players(self, histories: List[List[int]]) -> List[int]:
        """
        Return all player IDs that belong to at least one reordered-equivalent group.

        The algorithm groups players by a canonical signature of their history.
        Players with the same signature have exactly the same card-frequency profile.

        High-level steps:
        1. Build a signature for each player's history
        2. Store player IDs in a dictionary keyed by signature
        3. Collect all groups whose size is at least 2
        4. Return the player IDs in sorted ascending order

        Args:
            histories: A list where histories[i] is the draw history of player i.

        Returns:
            A sorted list of player IDs that have at least one reordered-equivalent
            partner.

        Time complexity:
            O(T + S), more precisely:
            - Building counters across all histories costs O(T), where T is the total
              number of cards across all players
            - Sorting distinct items inside each history contributes
              sum(O(u_i log u_i))
            - Collecting results costs O(n)
            Given the problem constraint that total history length is at most 200000,
            this is efficient enough.

        Space complexity:
            O(T) in the worst case for stored signatures and grouping structure
        """
        # This dictionary groups player IDs by their canonical history signature.
        #
        # Key:
        #   tuple of (card_id, count) pairs
        # Value:
        #   list of player IDs that share that exact signature
        #
        # defaultdict(list) is convenient because we can append directly without
        # checking whether the key already exists.
        groups: DefaultDict[Tuple[Tuple[int, int], ...], List[int]] = defaultdict(list)

        # Process each player one by one.
        for player_id, history in enumerate(histories):
            # Build a canonical representation of this player's history.
            signature: Tuple[Tuple[int, int], ...] = self._build_signature(history)

            # Add this player to the group for that signature.
            groups[signature].append(player_id)

        # This list will store every player ID that belongs to a group of size >= 2.
        result: List[int] = []

        # Examine every signature group we built.
        for player_ids in groups.values():
            # If a group has at least two players, then every player in that group
            # satisfies the problem condition.
            if len(player_ids) >= 2:
                result.extend(player_ids)

        # The problem explicitly requires sorted ascending order.
        # Even though player IDs were processed in increasing order, once we gather
        # IDs from multiple groups the combined list order depends on dictionary
        # traversal. Sorting guarantees the required output format.
        result.sort()

        return result


if __name__ == "__main__":
    solution = Solution()

    # Example 1
    histories1: List[List[int]] = [
        [4, 9, 4, 2],
        [2, 4, 9, 4],
        [7, 7, 1],
        [1, 7, 7],
        [3, 5],
    ]
    result1: List[int] = solution.find_reordered_equivalent_players(histories1)
    print("Example 1 result:", result1)
    # Expected: [0, 1, 2, 3]

    # Example 2
    histories2: List[List[int]] = [
        [1, 2, 3],
        [3, 2, 1, 1],
        [],
        [5, 5],
        [],
    ]
    result2: List[int] = solution.find_reordered_equivalent_players(histories2)
    print("Example 2 result:", result2)
    # Expected: [2, 4]

    # Additional quick sanity check
    histories3: List[List[int]] = [
        [10],
        [10, 10],
        [10],
        [20],
        [20, 20],
        [20],
    ]
    result3: List[int] = solution.find_reordered_equivalent_players(histories3)
    print("Additional example result:", result3)
    # Expected: [0, 2, 3, 5]