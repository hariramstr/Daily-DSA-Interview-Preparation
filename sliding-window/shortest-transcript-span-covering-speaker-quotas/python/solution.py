"""
Title: Shortest Transcript Span Covering Speaker Quotas

Problem Description:
You are given a meeting transcript represented by an array `speakers`, where
`speakers[i]` is the speaker ID of the person who spoke the `i`-th utterance.
You are also given a list of quota requirements `requirements`, where each
element is a pair `[speakerId, minCount]` meaning that a valid excerpt must
contain at least `minCount` utterances from that speaker.

Your task is to find the shortest contiguous span of the transcript that
satisfies all speaker quotas. If multiple spans have the same minimum length,
return the one with the smallest starting index. If no such span exists,
return `[-1, -1]`.

This problem models real interview scenarios such as extracting the smallest
meeting segment that contains enough participation from required stakeholders.
The challenge is that speaker IDs may be large, repeated many times, and only
some speakers are constrained by quotas. An efficient solution should avoid
checking every subarray.

Return the answer as a pair `[start, end]` using 0-based indices.
"""

from typing import Dict, List, Tuple


class Solution:
    def shortest_transcript_span(
        self, speakers: List[int], requirements: List[List[int]]
    ) -> List[int]:
        """
        Find the shortest contiguous span that satisfies all speaker quotas.

        Args:
            speakers: List of speaker IDs for each utterance in the transcript.
            requirements: List of [speakerId, minCount] quota requirements.

        Returns:
            A list [start, end] representing the shortest valid span using
            0-based indices. Returns [-1, -1] if no valid span exists.

        Time Complexity:
            O(n + m), where:
            - n is the number of utterances in `speakers`
            - m is the number of quota entries in `requirements`

            Each transcript index is processed at most twice:
            once when the right pointer expands, and once when the left pointer
            contracts.

        Space Complexity:
            O(m), because we only store counts for required speakers.
        """
        # If there are no requirements, the smallest valid span is ambiguous.
        # The problem constraints imply requirements.length >= 1, but we still
        # guard against an empty input for robustness.
        if not requirements:
            return [0, -1]

        # ------------------------------------------------------------------
        # Step 1: Build a dictionary of required speaker quotas.
        #
        # Example:
        # requirements = [[2, 2], [4, 2], [7, 1]]
        # required = {2: 2, 4: 2, 7: 1}
        #
        # Why a dictionary?
        # - Speaker IDs can be very large (up to 1e9), so using an array indexed
        #   by speaker ID would be wasteful or impossible.
        # - We only care about speakers that appear in requirements.
        # ------------------------------------------------------------------
        required: Dict[int, int] = {}
        for speaker_id, min_count in requirements:
            required[speaker_id] = min_count

        # ------------------------------------------------------------------
        # Step 2: Quick feasibility check.
        #
        # Before running the sliding window, we count how many times each
        # required speaker appears in the entire transcript.
        #
        # If even the full transcript does not contain enough occurrences for
        # some required speaker, then no subarray can possibly satisfy the
        # quotas, so we can immediately return [-1, -1].
        #
        # This is not strictly necessary for correctness, because the sliding
        # window would also fail naturally, but it makes the logic clearer and
        # can avoid unnecessary work in impossible cases.
        # ------------------------------------------------------------------
        total_counts: Dict[int, int] = {speaker_id: 0 for speaker_id in required}
        for speaker_id in speakers:
            if speaker_id in required:
                total_counts[speaker_id] += 1

        for speaker_id, needed in required.items():
            if total_counts[speaker_id] < needed:
                return [-1, -1]

        # ------------------------------------------------------------------
        # Step 3: Prepare sliding window state.
        #
        # We maintain a window [left, right].
        #
        # window_counts:
        #   Counts how many times each required speaker appears inside the
        #   current window.
        #
        # satisfied_types:
        #   Number of required speaker IDs whose quota is currently satisfied.
        #
        # required_types:
        #   Total number of distinct speaker IDs that have quotas.
        #
        # A window is valid exactly when:
        #   satisfied_types == required_types
        #
        # Why track "types satisfied" instead of checking all requirements every
        # time?
        # - Checking all requirements on every step would be too slow:
        #   O(n * m) in the worst case.
        # - By updating only the affected speaker when the window changes, we
        #   keep the algorithm linear.
        # ------------------------------------------------------------------
        window_counts: Dict[int, int] = {}
        satisfied_types: int = 0
        required_types: int = len(required)

        # Best answer found so far.
        # We store:
        # - best_start: starting index of best window
        # - best_end: ending index of best window
        # - best_length: length of best window
        #
        # Initialize with impossible values so that the first valid window
        # always replaces them.
        best_start: int = -1
        best_end: int = -1
        best_length: int = float("inf")

        left: int = 0

        # ------------------------------------------------------------------
        # Step 4: Expand the window with the right pointer.
        #
        # For each position `right`, we include speakers[right] in the window.
        # If that speaker is one we care about, update its count.
        #
        # If adding this speaker causes its count to reach the required quota
        # exactly, then one more requirement type becomes satisfied.
        # ------------------------------------------------------------------
        for right, speaker_id in enumerate(speakers):
            if speaker_id in required:
                new_count: int = window_counts.get(speaker_id, 0) + 1
                window_counts[speaker_id] = new_count

                # We only increment satisfied_types when we cross from
                # "not enough" to "exactly enough".
                #
                # Example:
                # required[2] = 2
                # counts go 0 -> 1 (still not satisfied)
                # counts go 1 -> 2 (now satisfied, increment)
                # counts go 2 -> 3 (still satisfied, do not increment again)
                if new_count == required[speaker_id]:
                    satisfied_types += 1

            # ------------------------------------------------------------------
            # Step 5: While the current window is valid, try to shrink it from
            # the left to make it as short as possible.
            #
            # This is the heart of the sliding window technique:
            # - Expand right until valid
            # - Then contract left while still valid
            #
            # Every time the window is valid, it is a candidate answer.
            # We update the best answer before removing anything from the left.
            # ------------------------------------------------------------------
            while satisfied_types == required_types and left <= right:
                current_length: int = right - left + 1

                # Update the best answer if:
                # 1) this window is shorter, or
                # 2) same length but smaller starting index
                #
                # The second condition handles the tie-breaking rule.
                if (
                    current_length < best_length
                    or (
                        current_length == best_length
                        and (best_start == -1 or left < best_start)
                    )
                ):
                    best_length = current_length
                    best_start = left
                    best_end = right

                # We now attempt to remove speakers[left] and move left forward.
                left_speaker: int = speakers[left]

                if left_speaker in required:
                    # Decrease the count because this speaker is leaving the
                    # current window.
                    updated_count: int = window_counts[left_speaker] - 1
                    window_counts[left_speaker] = updated_count

                    # If the count drops from "just enough" to "not enough",
                    # then the window stops being valid after this removal.
                    #
                    # Example:
                    # required[4] = 2
                    # count was 2, removing one makes it 1
                    # so this requirement is no longer satisfied.
                    if updated_count == required[left_speaker] - 1:
                        satisfied_types -= 1

                # Actually move the left boundary forward.
                left += 1

        # If no valid window was ever found, return [-1, -1].
        if best_start == -1:
            return [-1, -1]

        return [best_start, best_end]

    def solve(self, speakers: List[int], requirements: List[List[int]]) -> List[int]:
        """
        Wrapper method that calls the main algorithm.

        Args:
            speakers: List of speaker IDs for each utterance.
            requirements: List of [speakerId, minCount] quota requirements.

        Returns:
            The shortest valid span as [start, end], or [-1, -1] if impossible.

        Time Complexity:
            O(n + m)

        Space Complexity:
            O(m)
        """
        return self.shortest_transcript_span(speakers, requirements)


if __name__ == "__main__":
    solution = Solution()

    # Example 1
    speakers_1: List[int] = [4, 2, 7, 2, 4, 2, 9, 7, 4]
    requirements_1: List[List[int]] = [[2, 2], [4, 2], [7, 1]]
    result_1: List[int] = solution.solve(speakers_1, requirements_1)
    print("Example 1 Result:", result_1)  # Expected: [0, 4]

    # Example 2
    speakers_2: List[int] = [5, 1, 5, 3, 1, 5, 2, 3]
    requirements_2: List[List[int]] = [[1, 2], [3, 2], [2, 1]]
    result_2: List[int] = solution.solve(speakers_2, requirements_2)
    print("Example 2 Result:", result_2)  # Expected: [3, 7]

    # Additional impossible case
    speakers_3: List[int] = [1, 2, 1, 2]
    requirements_3: List[List[int]] = [[1, 2], [2, 3]]
    result_3: List[int] = solution.solve(speakers_3, requirements_3)
    print("Example 3 Result:", result_3)  # Expected: [-1, -1]