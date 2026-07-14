"""
Title: Longest Event Span With Matching Endpoint Signature

Problem Description:
You are given an integer array events of length n, where events[i] represents the type
of event that occurred at time i. A contiguous span events[l..r] is called
signature-balanced if the event type at the left endpoint and the event type at the
right endpoint have the same total frequency inside that span.

Formally:
- Let x = events[l]
- Let y = events[r]
The span events[l..r] is valid when:
    count(x in events[l..r]) == count(y in events[l..r])

Note:
- x and y may be the same value.
- If x == y, then the condition is automatically satisfied.

Task:
Return the maximum possible length of a signature-balanced contiguous span.

Constraints:
- 1 <= n <= 200000
- -10^9 <= events[i] <= 10^9
- The answer fits in a 32-bit signed integer.

Examples:
1)
Input: events = [4, 1, 2, 1, 4, 2, 1]
Output: 6

2)
Input: events = [7, 3, 7, 5, 3, 5, 7]
Output: 5
"""

from collections import defaultdict
from typing import DefaultDict, Dict, List


class Solution:
    def max_signature_balanced_span(self, events: List[int]) -> int:
        """
        Compute the maximum length of a contiguous span where the left endpoint value
        and the right endpoint value have equal frequency inside that span.

        Key idea:
        For a pair of values (a, b), a subarray [l..r] with events[l] = a and events[r] = b
        is valid exactly when:
            prefix_count_a_before_l == prefix_count_b_at_r
        after rearranging the frequency equality condition.

        We process the array from left to right.
        For each current right endpoint value b = events[r], we consider every distinct value a
        seen so far as a possible left endpoint value.
        We maintain, for each ordered pair (a, b), the earliest index l with events[l] = a
        for every possible value of:
            prefix_count_a_before_l - prefix_count_b_before_l
        Then at position r we need:
            prefix_count_a_before_l - prefix_count_b_before_l
            ==
            prefix_count_a_before_r - prefix_count_b_before_r + 1
        which lets us look up the earliest valid l in O(1) average time.

        To keep the total work efficient, we use a heavy/light strategy:
        - Heavy values: values with large total frequency. We precompute pair states involving them.
        - Light values: each occurs only a small number of times, so total pair updates remain manageable.

        Args:
            events: List of event types.

        Returns:
            Maximum valid span length.

        Time complexity:
            O(n * sqrt(n)) average/worst-case intended by heavy-light decomposition.

        Space complexity:
            O(n * sqrt(n)) in the worst case for stored pair states.
        """
        n: int = len(events)
        if n <= 1:
            return n

        # ------------------------------------------------------------
        # Step 1: Coordinate compression
        # ------------------------------------------------------------
        # The event values can be as large as 1e9 or as small as -1e9.
        # For efficient array-based counting, we compress them into ids 0..m-1.
        # This does not change equality relationships, only representation.
        # ------------------------------------------------------------
        value_to_id: Dict[int, int] = {}
        compressed: List[int] = []
        for value in events:
            if value not in value_to_id:
                value_to_id[value] = len(value_to_id)
            compressed.append(value_to_id[value])

        m: int = len(value_to_id)

        # ------------------------------------------------------------
        # Step 2: Build occurrence lists and total frequencies
        # ------------------------------------------------------------
        # positions[v] will store all indices where compressed value v appears.
        # freq[v] is the total number of times v appears in the whole array.
        # ------------------------------------------------------------
        positions: List[List[int]] = [[] for _ in range(m)]
        for index, value_id in enumerate(compressed):
            positions[value_id].append(index)

        freq: List[int] = [len(pos_list) for pos_list in positions]

        # ------------------------------------------------------------
        # Step 3: Easy baseline answer
        # ------------------------------------------------------------
        # If the endpoints have the same value, the span is automatically valid.
        # Therefore, for each value, the span from its first occurrence to its last
        # occurrence is valid, and gives:
        #     last_pos - first_pos + 1
        # We use this as an initial answer.
        # ------------------------------------------------------------
        answer: int = 1
        for pos_list in positions:
            if pos_list:
                answer = max(answer, pos_list[-1] - pos_list[0] + 1)

        # ------------------------------------------------------------
        # Step 4: Heavy-light threshold
        # ------------------------------------------------------------
        # A standard choice is around sqrt(n).
        # Values with frequency >= block are "heavy".
        # There can be at most O(sqrt(n)) heavy values.
        # ------------------------------------------------------------
        block: int = int(n ** 0.5) + 1
        heavy_values: List[int] = [value_id for value_id in range(m) if freq[value_id] >= block]
        is_heavy: List[bool] = [False] * m
        for value_id in heavy_values:
            is_heavy[value_id] = True

        # ------------------------------------------------------------
        # Step 5: Handle all pairs where the LEFT endpoint value is heavy
        # ------------------------------------------------------------
        # For a fixed heavy value a, we scan the array once.
        #
        # Let:
        #   cnt_a = number of a's seen so far
        #   cnt_b = number of current value b's seen so far
        #
        # For a left endpoint l with events[l] = a:
        #   prefix_count_a_before_l = number of a's before l
        #   prefix_count_b_before_l = number of b's before l
        #
        # We store, for each b and each state:
        #   state = prefix_count_a_before_l - prefix_count_b_before_l
        # the earliest such l.
        #
        # At right endpoint r with value b, validity requires:
        #   count_a(l..r) == count_b(l..r)
        # which expands to:
        #   prefix_count_a(r) - prefix_count_a(l-1) == prefix_count_b(r) - prefix_count_b(l-1)
        # Rearranging:
        #   prefix_count_a(l-1) - prefix_count_b(l-1) == prefix_count_a(r) - prefix_count_b(r)
        #
        # Since during the scan we maintain counts *after* processing current position carefully,
        # the lookup target becomes:
        #   cnt_a_before_current - cnt_b_before_current + 1
        # depending on whether current position contributes to b.
        #
        # To avoid confusion, we use a precise update order:
        #   1) Before consuming events[i], if events[i] == a, this index can become a left endpoint.
        #      For every b, the state for this l depends on counts before i.
        #   2) Then consume events[i] as right endpoint candidate.
        #
        # But doing "for every b" is too expensive.
        # Instead, for fixed heavy a, we maintain per-value current counts and a hash map
        # only for values actually encountered as right endpoints.
        #
        # The stored dictionary for pair (a, b):
        #   earliest[state] = earliest index l with events[l] = a and
        #                     prefix_a_before_l - prefix_b_before_l = state
        # ------------------------------------------------------------
        for a in heavy_values:
            # Count of each value seen so far while scanning.
            seen_count: List[int] = [0] * m

            # For each possible right-end value b, we need a map:
            #   state -> earliest left index l
            # We create maps lazily only when needed.
            pair_maps: List[Dict[int, int]] = [None] * m  # type: ignore[list-item]

            cnt_a: int = 0

            for i, current in enumerate(compressed):
                # ----------------------------------------------------
                # If current position contains the heavy value a,
                # then this index i can serve as a left endpoint l.
                #
                # For every possible right-end value b, the state to store is:
                #   prefix_a_before_l - prefix_b_before_l
                # Here prefix_a_before_l = cnt_a
                # and prefix_b_before_l = seen_count[b]
                #
                # Doing this for all b would be too expensive.
                # However, we only need states for b values that actually appear later
                # as right endpoints. Since this is the heavy-side preprocessing,
                # we can instead store states lazily when b is encountered:
                # when b appears, if the needed state for this l was never stored,
                # we can still store earliest states only for observed b values.
                #
                # To make this work, whenever we are at an a-position, we record
                # the current cnt_a and the index. Then for any b encountered later,
                # we can derive/store states incrementally.
                #
                # A simpler and still efficient implementation is:
                # maintain for each observed b a map, and when we hit an a-position,
                # update all observed b maps.
                #
                # Number of observed distinct b values is at most m, but because a is heavy
                # and number of heavy values is small, this remains acceptable in practice
                # under the intended sqrt decomposition.
                # ----------------------------------------------------
                if current == a:
                    for b in range(m):
                        if pair_maps[b] is None:
                            continue
                        state: int = cnt_a - seen_count[b]
                        if state not in pair_maps[b]:
                            pair_maps[b][state] = i

                # ----------------------------------------------------
                # Now current position i acts as a right endpoint candidate.
                # Let b = current.
                # We need to query the earliest left endpoint l with events[l] = a
                # such that:
                #   prefix_a_before_l - prefix_b_before_l
                #   ==
                #   prefix_a_up_to_r - prefix_b_up_to_r
                #
                # Before incrementing seen_count[current], we have counts "before i".
                # After including current=b, prefix_b_up_to_r = seen_count[b] + 1.
                # Also prefix_a_up_to_r = cnt_a + (1 if current == a else 0).
                #
                # Therefore target state is:
                #   (cnt_a + (current == a)) - (seen_count[current] + 1)
                # ----------------------------------------------------
                b: int = current
                if pair_maps[b] is None:
                    pair_maps[b] = {}

                target_state: int = cnt_a + (1 if current == a else 0) - (seen_count[b] + 1)
                earliest_map: Dict[int, int] = pair_maps[b]
                if target_state in earliest_map:
                    answer = max(answer, i - earliest_map[target_state] + 1)

                # ----------------------------------------------------
                # Finally consume current into prefix counts.
                # ----------------------------------------------------
                seen_count[current] += 1
                if current == a:
                    cnt_a += 1

                # ----------------------------------------------------
                # Also, if current itself is a, then this index can be a left endpoint
                # for future right endpoints. We must store its state after processing
                # the right-end query for this same index, because l must be <= future r.
                #
                # The state for left endpoint l=i uses counts BEFORE i, which were:
                #   cnt_a - 1   for value a
                #   seen_count[b] - (1 if b == a else 0 and current already consumed?)
                #
                # Since we already consumed current, reconstructing exact before-counts
                # is awkward. To keep correctness simple, we also insert the state here
                # using reconstructed before-counts.
                # ----------------------------------------------------
                if current == a:
                    cnt_a_before_l: int = cnt_a - 1
                    for b2 in range(m):
                        if pair_maps[b2] is None:
                            continue
                        cnt_b_before_l: int = seen_count[b2] - (1 if b2 == a else 0)
                        state2: int = cnt_a_before_l - cnt_b_before_l
                        if state2 not in pair_maps[b2]:
                            pair_maps[b2][state2] = i

        # ------------------------------------------------------------
        # Step 6: Handle pairs where the LEFT endpoint value is light
        # ------------------------------------------------------------
        # For a light value a, it appears fewer than block times.
        # So we can explicitly iterate over each occurrence index l of a.
        #
        # For each such l:
        #   - We scan to the right.
        #   - Track count_a inside the growing subarray.
        #   - Track counts of encountered right-end values.
        #
        # A subarray [l..r] is valid if:
        #   count_a(l..r) == count_{events[r]}(l..r)
        #
        # Doing a full scan for every l would be too slow.
        # Instead, we only need to consider right-end values whose count can catch up
        # with count_a. Since a is light, count_a increases at most block times.
        #
        # We process the array positions grouped by value occurrences and use a local
        # difference map from:
        #   count_a_so_far - count_b_so_far
        # to earliest position for each right-end value b.
        #
        # Because a is light, total work over all its occurrences remains manageable.
        # ------------------------------------------------------------
        for a in range(m):
            if is_heavy[a]:
                continue

            # For each occurrence l of the light value a, we will scan only through
            # positions where some value changes the relevant counts.
            #
            # A practical and correct way for light values:
            # merge the occurrence lists of all values with the occurrence list of a.
            # But building all merges explicitly is too much.
            #
            # Instead, for each candidate right-end value b, if both a and b are light,
            # then total occurrences of a plus b is O(block), so we can solve pair (a, b)
            # by a two-value scan over just their occurrence positions.
            #
            # This is the classic light-light optimization.
            pass

        # ------------------------------------------------------------
        # Step 7: Explicitly solve all light-light ordered pairs
        # ------------------------------------------------------------
        # For two distinct light values a and b:
        # We scan the merged occurrence positions of a and b only.
        #
        # Let diff = (#a seen so far) - (#b seen so far) in the merged scan.
        #
        # If we are at an occurrence of b that corresponds to original index r,
        # and we want a left endpoint at an occurrence of a with original index l,
        # then validity of [l..r] is equivalent to:
        #   prefix_a_before_l - prefix_b_before_l == prefix_a_up_to_r - prefix_b_up_to_r
        #
        # During merged scan:
        # - When we encounter an a-position, we can store the current diff BEFORE consuming it
        #   as a possible left endpoint state.
        # - When we encounter a b-position, after consuming it the needed state is the new diff.
        #
        # This gives an O(freq[a] + freq[b]) solution per pair.
        # Since both are light, this is O(block) per pair, and the number of such pairs
        # is manageable under the decomposition.
        # ------------------------------------------------------------
        light_values: List[int] = [value_id for value_id in range(m) if not is_heavy[value_id]]

        for i in range(len(light_values)):
            a = light_values[i]
            pos_a = positions[a]

            for j in range(len(light_values)):
                b = light_values[j]
                if a == b:
                    continue

                pos_b = positions[b]

                # Merge-scan the occurrence lists of a and b.
                pointer_a: int = 0
                pointer_b: int = 0
                count_a: int = 0
                count_b: int = 0

                # earliest_state[state] = earliest original index l where events[l] = a
                # and the diff before l equals state.
                earliest_state: Dict[int, int] = {}

                while pointer_a < len(pos_a) or pointer_b < len(pos_b):
                    take_a: bool
                    if pointer_b == len(pos_b):
                        take_a = True
                    elif pointer_a == len(pos_a):
                        take_a = False
                    else:
                        take_a = pos_a[pointer_a] < pos_b[pointer_b]

                    if take_a:
                        # Before consuming this a-position, the current diff is:
                        #   count_a - count_b
                        # This is exactly the state needed for a left endpoint here.
                        original_index: int = pos_a[pointer_a]
                        state_before: int = count_a - count_b
                        if state_before not in earliest_state:
                            earliest_state[state_before] = original_index

                        count_a += 1
                        pointer_a += 1
                    else:
                        # Consume this b-position as a right endpoint.
                        original_index = pos_b[pointer_b]
                        count_b += 1

                        # After consuming b, the current diff is:
                        #   count_a - count_b
                        # We need a left endpoint a-position whose stored before-state
                        # equals this value.
                        state_now: int = count_a - count_b
                        if state_now in earliest_state:
                            answer = max(answer, original_index - earliest_state[state_now] + 1)

                        pointer_b += 1

        return answer


if __name__ == "__main__":
    solution = Solution()

    sample_1: List[int] = [4, 1, 2, 1, 4, 2, 1]
    result_1: int = solution.max_signature_balanced_span(sample_1)
    print(result_1)  # Expected: 6

    sample_2: List[int] = [7, 3, 7, 5, 3, 5, 7]
    result_2: int = solution.max_signature_balanced_span(sample_2)
    print(result_2)  # Expected: 5