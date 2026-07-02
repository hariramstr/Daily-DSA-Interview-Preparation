"""
Title: Earliest Repeated Folder Snapshot

Problem Description:
A cloud storage system records the state of a folder once per minute. Each snapshot
is represented by an array of file IDs currently present in the folder. The order
of file IDs inside a snapshot is not meaningful, but duplicate IDs will never appear
within the same snapshot.

Two snapshots are considered identical if they contain exactly the same set of file IDs,
regardless of order. Given a list of snapshots in chronological order, return the earliest
pair of indices [i, j] such that i < j and snapshots[i] and snapshots[j] are identical.
If multiple repeated states exist, choose the pair with the smallest j. If there is still
a tie, choose the one with the smallest i. If no folder state repeats, return [-1, -1].

Your task is to design an efficient solution using hashing so that each snapshot can be
normalized and compared quickly.

Constraints:
- 1 <= snapshots.length <= 100000
- 0 <= snapshots[i].length <= 1000
- 0 <= fileID <= 1000000000
- The sum of all snapshots[i].length over the entire input does not exceed 200000
- Each individual snapshot contains distinct file IDs

Example 1:
Input: snapshots = [[5,1,9],[3,4],[9,5,1],[4,3],[7]]
Output: [0,2]

Example 2:
Input: snapshots = [[],[8],[2,6],[6,2],[]]
Output: [2,3]

Important note about Example 2:
The written explanation in the prompt says output [1,3], but that is inconsistent with
the problem definition because snapshot 1 is [8] and snapshot 3 is [6,2], which are not
identical. The correct earliest repeated pair is [2,3] because snapshots 2 and 3 both
represent the set {2,6}, and j = 3 is earlier than the empty snapshot repeat at j = 4.
"""

from typing import Dict, List, Tuple


class Solution:
    def normalize_snapshot(self, snapshot: List[int]) -> Tuple[int, ...]:
        """
        Convert one snapshot into a canonical hashable form.

        Because order does not matter, we sort the file IDs and store them in a tuple.
        Two snapshots that contain the same IDs in different orders will therefore
        produce exactly the same normalized tuple.

        Args:
            snapshot: A list of distinct file IDs representing one folder snapshot.

        Returns:
            A sorted tuple of file IDs that can be used as a dictionary key.

        Time complexity:
            O(k log k), where k is the number of file IDs in this snapshot.

        Space complexity:
            O(k), for the sorted tuple.
        """
        # Sorting is the key normalization step:
        # - [5, 1, 9] becomes (1, 5, 9)
        # - [9, 5, 1] also becomes (1, 5, 9)
        # This makes equality checking easy and reliable.
        return tuple(sorted(snapshot))

    def earliest_repeated_snapshot(self, snapshots: List[List[int]]) -> List[int]:
        """
        Find the earliest pair of indices [i, j] such that snapshots[i] and snapshots[j]
        represent the same set of file IDs.

        We scan snapshots from left to right in chronological order. For each snapshot:
        1. Normalize it into a canonical representation.
        2. Check whether we have seen this normalized state before.
        3. If yes, we immediately return the first index where it appeared and the current
           index, because scanning left to right guarantees this current index is the
           smallest possible j.
        4. If not, store its first occurrence.

        This directly satisfies the tie-breaking rules:
        - Smallest j: guaranteed by left-to-right scanning and returning on first repeat.
        - Smallest i for that j: guaranteed because we store only the first occurrence
          of each normalized snapshot.

        Args:
            snapshots: A list of snapshots, where each snapshot is a list of distinct file IDs.

        Returns:
            A list [i, j] for the earliest repeated snapshot pair, or [-1, -1] if none exists.

        Time complexity:
            O(total_elements * log m) in the worst case due to sorting each snapshot,
            more precisely sum over all snapshots of O(k log k), where k is that snapshot's size.

        Space complexity:
            O(total_normalized_data) for the dictionary keys in the worst case.
        """
        # This dictionary maps:
        #   normalized_snapshot -> first index where it appeared
        #
        # Why a dictionary?
        # - Average O(1) lookup time
        # - Average O(1) insertion time
        # - Perfect for "have we seen this before?" style problems
        first_seen: Dict[Tuple[int, ...], int] = {}

        # We process snapshots in chronological order.
        # This is extremely important for correctness:
        # the first repeated snapshot we encounter will automatically have the smallest j.
        for current_index, snapshot in enumerate(snapshots):
            # Step 1: Convert the current snapshot into a canonical form.
            # Since order does not matter, sorting ensures equivalent snapshots match.
            normalized = self.normalize_snapshot(snapshot)

            # Step 2: Check whether this exact normalized state has already appeared.
            if normalized in first_seen:
                # If it has appeared before, then:
                # - first_seen[normalized] is the earliest i for this state
                # - current_index is the current j
                #
                # Because we are scanning from left to right, this is the smallest possible j
                # among all repeated pairs in the entire input.
                #
                # Also, because we only store the first occurrence, the i we return is the
                # smallest possible i for this same normalized state.
                return [first_seen[normalized], current_index]

            # Step 3: If this state has not appeared before, record its first occurrence.
            #
            # We intentionally do NOT overwrite existing entries, because the problem wants
            # the smallest i when there is a tie on j. Keeping the first occurrence preserves
            # that earliest index.
            first_seen[normalized] = current_index

        # If we finish the entire scan without finding any repeated normalized snapshot,
        # then no folder state repeats.
        return [-1, -1]


if __name__ == "__main__":
    solution = Solution()

    # Example 1 from the prompt
    snapshots_1: List[List[int]] = [[5, 1, 9], [3, 4], [9, 5, 1], [4, 3], [7]]
    result_1 = solution.earliest_repeated_snapshot(snapshots_1)
    print("Example 1 result:", result_1)
    # Expected: [0, 2]

    # Example 2 from the prompt
    # The prompt's stated output [1,3] is inconsistent with the actual snapshot contents.
    # Correct result is [2,3]:
    # - snapshot 2 = [2,6] -> normalized (2,6)
    # - snapshot 3 = [6,2] -> normalized (2,6)
    # This repeat happens at j = 3, which is earlier than the empty snapshot repeat at j = 4.
    snapshots_2: List[List[int]] = [[], [8], [2, 6], [6, 2], []]
    result_2 = solution.earliest_repeated_snapshot(snapshots_2)
    print("Example 2 result:", result_2)
    # Correct expected: [2, 3]

    # Additional sample: no repeated snapshot
    snapshots_3: List[List[int]] = [[1], [2], [3], [4]]
    result_3 = solution.earliest_repeated_snapshot(snapshots_3)
    print("Example 3 result:", result_3)
    # Expected: [-1, -1]

    # Additional sample: repeated empty snapshots
    snapshots_4: List[List[int]] = [[], [1, 2], []]
    result_4 = solution.earliest_repeated_snapshot(snapshots_4)
    print("Example 4 result:", result_4)
    # Expected: [0, 2]