"""
Title: Count Reciprocal Follow Suggestions

Problem Description:
You are given a list of directed follow relationships in a social platform.
Each relationship is represented as a pair [a, b], meaning user a follows user b.

The platform wants to suggest users who should follow each other back.
A reciprocal follow suggestion exists for a pair of distinct users (u, v)
if exactly one of the two directed relationships exists:
either u follows v or v follows u, but not both.

Your task is to count how many unordered user pairs have exactly one directed
follow relationship between them.

Rules:
- Each unordered pair of users should be counted at most once.
- Duplicate input relationships may appear and should be treated as a single
  follow relationship.
- Self-follows of the form [x, x] should be ignored completely.

Return the total number of reciprocal follow suggestions.

Implementation note:
- Deduplicate directed edges first.
- Then group by unordered user pair.
- Count a pair if exactly one unique direction exists for that pair.
"""

from typing import Dict, List, Set, Tuple


class Solution:
    def count_reciprocal_follow_suggestions(
        self, relationships: List[List[int]]
    ) -> int:
        """
        Count unordered user pairs that have exactly one directed follow edge.

        The method first removes invalid and duplicate directed edges:
        - Self-follows are ignored.
        - Duplicate directed follows are collapsed into one.

        Then, for each remaining directed edge (a, b), it maps the relationship
        to its unordered pair (min(a, b), max(a, b)) and tracks which direction
        exists for that pair. If only one direction exists, that pair contributes
        to the answer. If both directions exist, it does not.

        Args:
            relationships: A list of [a, b] pairs meaning user a follows user b.

        Returns:
            The number of unordered user pairs with exactly one unique directed
            follow relationship.

        Time complexity:
            O(n), where n is the number of input relationships.

        Space complexity:
            O(n), for storing unique directed edges and pair direction states.
        """
        # ---------------------------------------------------------------------
        # STEP 1: Deduplicate directed edges while ignoring self-follows.
        #
        # Why do we do this first?
        # - The problem explicitly says duplicate relationships should count as
        #   only one follow relationship.
        # - Self-follows [x, x] must be ignored completely.
        #
        # We use a set of tuples:
        #   unique_edges = {(a, b), ...}
        #
        # A set is ideal here because:
        # - Membership checks are fast on average: O(1)
        # - Inserting duplicates has no effect
        # ---------------------------------------------------------------------
        unique_edges: Set[Tuple[int, int]] = set()

        for relationship in relationships:
            a: int = relationship[0]
            b: int = relationship[1]

            # Ignore self-follows because the problem says a user cannot be
            # suggested to follow themselves back.
            if a == b:
                continue

            # Add the directed edge to the set.
            # If it already exists, the set automatically keeps only one copy.
            unique_edges.add((a, b))

        # ---------------------------------------------------------------------
        # STEP 2: Group directed edges by unordered user pair.
        #
        # For a directed edge (a, b), the unordered pair is:
        #   (min(a, b), max(a, b))
        #
        # Example:
        # - (1, 3) and (3, 1) both belong to unordered pair (1, 3)
        #
        # For each unordered pair, we want to know whether:
        # - only one direction exists, or
        # - both directions exist
        #
        # We can represent direction using a small integer bitmask:
        # - bit 1 means smaller_user -> larger_user exists
        # - bit 2 means larger_user -> smaller_user exists
        #
        # So for a pair:
        # - state == 1  => only smaller -> larger exists
        # - state == 2  => only larger -> smaller exists
        # - state == 3  => both directions exist
        #
        # This is compact and efficient.
        # ---------------------------------------------------------------------
        pair_state: Dict[Tuple[int, int], int] = {}

        for a, b in unique_edges:
            u: int = min(a, b)
            v: int = max(a, b)

            # Determine which direction this edge represents relative to the
            # normalized unordered pair (u, v).
            #
            # If a == u and b == v, then the edge is u -> v, so use bit 1.
            # Otherwise, the edge must be v -> u, so use bit 2.
            direction_bit: int = 1 if (a == u and b == v) else 2

            # Initialize missing pairs with state 0, then OR in the direction.
            pair_state[(u, v)] = pair_state.get((u, v), 0) | direction_bit

        # ---------------------------------------------------------------------
        # STEP 3: Count how many unordered pairs have exactly one direction.
        #
        # After processing:
        # - state 1 means exactly one direction exists
        # - state 2 means exactly one direction exists
        # - state 3 means both directions exist
        #
        # Therefore, we count states that are either 1 or 2.
        # A simple way to express that is: state != 3
        # because states can only be 1, 2, or 3 for valid stored pairs.
        # ---------------------------------------------------------------------
        suggestions: int = 0

        for state in pair_state.values():
            if state != 3:
                suggestions += 1

        return suggestions

    def countReciprocalFollowSuggestions(
        self, relationships: List[List[int]]
    ) -> int:
        """
        Compatibility wrapper using camelCase naming.

        Args:
            relationships: A list of [a, b] directed follow relationships.

        Returns:
            The number of unordered user pairs with exactly one unique directed
            follow relationship.

        Time complexity:
            O(n), where n is the number of input relationships.

        Space complexity:
            O(n).
        """
        return self.count_reciprocal_follow_suggestions(relationships)


if __name__ == "__main__":
    solution = Solution()

    # Example 1 from the prompt
    relationships_1: List[List[int]] = [
        [1, 2],
        [2, 1],
        [1, 3],
        [4, 5],
        [4, 5],
        [6, 7],
    ]
    result_1: int = solution.count_reciprocal_follow_suggestions(relationships_1)
    print("Example 1 result:", result_1)  # Expected: 3

    # Example 2:
    # The prompt contains an ambiguity, but then explicitly clarifies that
    # after deduplication we should count every unordered pair independently.
    # Under that stated rule, the correct result is 3.
    relationships_2: List[List[int]] = [
        [10, 20],
        [20, 30],
        [30, 20],
        [40, 40],
        [50, 60],
        [60, 50],
        [70, 80],
        [80, 90],
    ]
    result_2: int = solution.count_reciprocal_follow_suggestions(relationships_2)
    print("Example 2 result:", result_2)  # Expected under clarified rule: 3

    # Additional quick sanity checks
    relationships_3: List[List[int]] = [
        [1, 1],
        [2, 2],
        [3, 3],
    ]
    result_3: int = solution.count_reciprocal_follow_suggestions(relationships_3)
    print("Only self-follows result:", result_3)  # Expected: 0

    relationships_4: List[List[int]] = [
        [1, 2],
        [1, 2],
        [1, 2],
    ]
    result_4: int = solution.count_reciprocal_follow_suggestions(relationships_4)
    print("Duplicate one-way follows result:", result_4)  # Expected: 1

    relationships_5: List[List[int]] = [
        [1, 2],
        [2, 1],
        [1, 2],
        [2, 1],
    ]
    result_5: int = solution.count_reciprocal_follow_suggestions(relationships_5)
    print("Duplicate two-way follows result:", result_5)  # Expected: 0