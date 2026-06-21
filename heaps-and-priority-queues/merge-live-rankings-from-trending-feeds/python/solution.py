"""
Title: Merge Live Rankings from Trending Feeds

Problem Description:
A social media platform receives multiple already-sorted trending feeds from different
regions. Each regional feed is sorted in descending order by score, where a higher score
means a post is more trending. You are given k feeds, where each feed contains post
records in the form [postId, score]. The same postId may appear in multiple feeds with
different scores because regions rank posts independently.

Your task is to build a single global ranking of the top m unique posts. For every
postId, its global score is defined as the maximum score it achieves in any regional
feed. Return the top m unique postIds sorted by global score in descending order. If
two posts have the same global score, the smaller postId should come first.

You should design an efficient solution using heaps or priority queues. A brute-force
approach that flattens all feeds and sorts everything may be too slow when the total
number of feed entries is large.

Constraints:
- 1 <= k <= 10^4
- 1 <= total number of feed entries across all feeds <= 2 * 10^5
- 1 <= length of each feed <= 2 * 10^5
- 1 <= postId <= 10^9
- 1 <= score <= 10^9
- 1 <= m <= number of distinct postIds across all feeds
- Each individual feed is already sorted by score in descending order
"""

from typing import Dict, List, Tuple
import heapq


class Solution:
    def top_m_global_posts(self, feeds: List[List[List[int]]], m: int) -> List[int]:
        """
        Return the top m unique postIds by global score.

        The global score of a postId is the maximum score that post achieves in any feed.
        The result is ordered by:
        1. Higher global score first
        2. Smaller postId first when scores tie

        This solution uses a heap to perform a k-way merge over the already-sorted feeds,
        while tracking the best score seen for each postId. After all entries are processed,
        it extracts the top m unique posts using another heap-based selection step.

        Args:
            feeds: A list of regional feeds. Each feed is sorted in descending score order,
                and each item is [postId, score].
            m: Number of unique postIds to return.

        Returns:
            A list of the top m postIds ordered by global score descending, then postId ascending.

        Time Complexity:
            Let N be the total number of feed entries across all feeds.
            Let U be the number of distinct postIds.
            - K-way merge traversal: O(N log k)
            - Top-m selection from distinct posts: O(U log m)
            Total: O(N log k + U log m)

        Space Complexity:
            - Merge heap: O(k)
            - Best score map: O(U)
            - Top-m heap: O(m)
            Total: O(U + k + m)
        """
        # ---------------------------------------------------------------------
        # STEP 1: Build a max-heap for the first item of every non-empty feed.
        #
        # Python's heapq is a min-heap, so to simulate a max-heap by score we
        # store negative scores.
        #
        # Each heap entry stores:
        #   (-score, feed_index, item_index, post_id)
        #
        # Why include feed_index and item_index?
        # - feed_index tells us which feed this item came from
        # - item_index tells us where we are inside that feed
        # This lets us pop the current best item globally, then push the next
        # item from the same feed, exactly like merging k sorted lists.
        # ---------------------------------------------------------------------
        merge_heap: List[Tuple[int, int, int, int]] = []

        for feed_index, feed in enumerate(feeds):
            if feed:
                post_id, score = feed[0]
                heapq.heappush(merge_heap, (-score, feed_index, 0, post_id))

        # ---------------------------------------------------------------------
        # STEP 2: Traverse all feed entries in descending score order using the
        # heap, and compute the global maximum score for every postId.
        #
        # best_score[post_id] = maximum score seen so far for that post
        #
        # Even though feeds are individually sorted, duplicates may appear across
        # different feeds. We must consider all occurrences because the same post
        # may have a better score in another region.
        #
        # We do NOT finalize a post the first time we see it, because another feed
        # could still contain the same post with an even higher score if that feed's
        # current top entries have not yet been popped. So we safely process all
        # entries and keep the maximum.
        # ---------------------------------------------------------------------
        best_score: Dict[int, int] = {}

        while merge_heap:
            neg_score, feed_index, item_index, post_id = heapq.heappop(merge_heap)
            score = -neg_score

            # Update the global best score for this postId.
            #
            # If this is the first time we see the post, store the score.
            # If we have seen it before, keep the larger of the old and new score.
            if post_id not in best_score or score > best_score[post_id]:
                best_score[post_id] = score

            # Move forward in the same feed:
            # after taking item_index, the next candidate from this feed is
            # item_index + 1, if it exists.
            next_index = item_index + 1
            if next_index < len(feeds[feed_index]):
                next_post_id, next_score = feeds[feed_index][next_index]
                heapq.heappush(
                    merge_heap,
                    (-next_score, feed_index, next_index, next_post_id),
                )

        # ---------------------------------------------------------------------
        # STEP 3: Select the top m unique posts from best_score efficiently.
        #
        # We now have one final score per unique postId.
        #
        # Desired final ordering:
        #   - higher score first
        #   - smaller postId first on ties
        #
        # To avoid sorting all distinct posts when m may be much smaller than U,
        # we maintain a min-heap of size at most m containing the current best m
        # candidates.
        #
        # We define a ranking key:
        #   better candidate = larger score, and for equal score smaller postId
        #
        # For the min-heap, we want the "worst among the current top m" at the root
        # so it can be replaced when a better candidate appears.
        #
        # Heap entry format:
        #   (score, -post_id, post_id)
        #
        # Why this works:
        # - Smaller score is worse, so it naturally rises to the root.
        # - For equal score, larger postId is worse.
        #   Since we store -post_id, a larger postId gives a smaller value, which
        #   also rises to the root in a min-heap.
        #
        # Example for equal score 60:
        #   post 7  -> (60, -7, 7)
        #   post 8  -> (60, -8, 8)
        # Since -8 < -7, post 8 is considered worse and sits closer to the root,
        # which is exactly what we want because smaller postId should win ties.
        # ---------------------------------------------------------------------
        top_heap: List[Tuple[int, int, int]] = []

        for post_id, score in best_score.items():
            candidate = (score, -post_id, post_id)

            if len(top_heap) < m:
                # If we still have room, simply add the candidate.
                heapq.heappush(top_heap, candidate)
            else:
                # If the heap is full, compare against the current worst top-m item.
                # Replace it only if the new candidate is better.
                if candidate > top_heap[0]:
                    heapq.heapreplace(top_heap, candidate)

        # ---------------------------------------------------------------------
        # STEP 4: Convert the selected top-m heap into the required final order.
        #
        # The heap currently contains the correct set of m posts, but not in the
        # final output order. So we sort these m items by:
        #   1. score descending
        #   2. postId ascending
        #
        # Since the heap size is only m, this final sort is efficient.
        # ---------------------------------------------------------------------
        top_items = list(top_heap)
        top_items.sort(key=lambda item: (-item[0], item[2]))

        # Extract only the postIds in the required order.
        return [post_id for _, _, post_id in top_items]


if __name__ == "__main__":
    solution = Solution()

    # Example 1
    feeds1 = [
        [[101, 95], [102, 90], [103, 80]],
        [[104, 99], [101, 97], [105, 70]],
        [[102, 96], [106, 88]],
    ]
    m1 = 4
    result1 = solution.top_m_global_posts(feeds1, m1)
    print("Example 1 Output:", result1)
    # Expected: [104, 101, 102, 106]

    # Example 2
    feeds2 = [
        [[7, 50], [8, 40], [9, 30]],
        [[8, 60], [10, 55]],
        [[7, 60], [11, 20]],
    ]
    m2 = 3
    result2 = solution.top_m_global_posts(feeds2, m2)
    print("Example 2 Output:", result2)
    # Expected: [7, 8, 10]