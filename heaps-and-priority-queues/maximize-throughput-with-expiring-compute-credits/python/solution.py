"""
Title: Maximize Throughput with Expiring Compute Credits

Problem Description:
A cloud platform receives compute jobs over time. Each job i is described by three integers:
releaseTime[i], creditsNeeded[i], and value[i].

- The job becomes available at time releaseTime[i]
- It requires exactly creditsNeeded[i] compute credits to finish
- It yields value[i] throughput points if completed

The platform also receives batches of temporary compute credits. Each credit batch j is
described by (grantTime[j], amount[j], expireTime[j]).

- Starting at grantTime[j], amount[j] credits become usable
- Any unused credits from that batch disappear immediately after expireTime[j]
- Credits from different batches may overlap in time and are interchangeable while active

You may complete any subset of jobs, and each selected job must be completed at some integer
time t such that t >= releaseTime[i]. Completing a job consumes creditsNeeded[i] active credits
at that same time t. A job can be completed at most once, takes negligible processing time,
and cannot be split across multiple times.

Task:
Compute the maximum total throughput value obtainable.

Constraints:
- 1 <= n, m <= 2 * 10^5
- 0 <= releaseTime[i], grantTime[j], expireTime[j] <= 10^9
- 1 <= creditsNeeded[i], amount[j] <= 10^9
- 1 <= value[i] <= 10^9
- expireTime[j] >= grantTime[j]
- All answers fit in 64-bit signed integer range
"""

from typing import List, Tuple
import heapq


class FenwickTree:
    """Fenwick tree / Binary Indexed Tree for prefix sums over compressed expiry indices."""

    def __init__(self, size: int) -> None:
        """
        Initialize an empty Fenwick tree.

        Args:
            size: Number of positions.

        Returns:
            None

        Time complexity:
            O(size)

        Space complexity:
            O(size)
        """
        self.n: int = size
        self.bit: List[int] = [0] * (size + 1)

    def add(self, index: int, delta: int) -> None:
        """
        Add delta to one position.

        Args:
            index: 1-based index.
            delta: Value to add.

        Returns:
            None

        Time complexity:
            O(log n)

        Space complexity:
            O(1)
        """
        i: int = index
        while i <= self.n:
            self.bit[i] += delta
            i += i & -i

    def prefix_sum(self, index: int) -> int:
        """
        Compute sum from 1..index.

        Args:
            index: 1-based index.

        Returns:
            Prefix sum.

        Time complexity:
            O(log n)

        Space complexity:
            O(1)
        """
        result: int = 0
        i: int = index
        while i > 0:
            result += self.bit[i]
            i -= i & -i
        return result

    def total_sum(self) -> int:
        """
        Compute total sum of all positions.

        Args:
            None

        Returns:
            Total sum.

        Time complexity:
            O(log n)

        Space complexity:
            O(1)
        """
        return self.prefix_sum(self.n)

    def find_first_positive(self) -> int:
        """
        Find the smallest index p such that prefix_sum(p) >= 1.
        This is used to locate the earliest expiry bucket that still has credits.

        Args:
            None

        Returns:
            1-based index of first positive position.

        Time complexity:
            O(log n)

        Space complexity:
            O(1)
        """
        idx: int = 0
        bit_mask: int = 1 << (self.n.bit_length())
        current_sum: int = 0

        while bit_mask:
            next_idx: int = idx + bit_mask
            if next_idx <= self.n and current_sum + self.bit[next_idx] < 1:
                idx = next_idx
                current_sum += self.bit[next_idx]
            bit_mask >>= 1

        return idx + 1


class SegmentTreeMax:
    """Segment tree storing maximum job value for each exact credit requirement."""

    def __init__(self, size: int) -> None:
        """
        Initialize a segment tree for max queries.

        Args:
            size: Number of leaves.

        Returns:
            None

        Time complexity:
            O(size)

        Space complexity:
            O(size)
        """
        self.size: int = 1
        while self.size < size:
            self.size <<= 1
        self.data: List[int] = [0] * (2 * self.size)

    def update(self, index: int, value: int) -> None:
        """
        Set one leaf to value and rebuild ancestors.

        Args:
            index: 0-based index.
            value: New value.

        Returns:
            None

        Time complexity:
            O(log n)

        Space complexity:
            O(1)
        """
        pos: int = index + self.size
        self.data[pos] = value
        pos >>= 1
        while pos:
            left: int = pos << 1
            self.data[pos] = max(self.data[left], self.data[left | 1])
            pos >>= 1

    def query_max(self, left: int, right: int) -> int:
        """
        Query maximum on inclusive range [left, right].

        Args:
            left: Left index, 0-based.
            right: Right index, 0-based.

        Returns:
            Maximum value in the range.

        Time complexity:
            O(log n)

        Space complexity:
            O(1)
        """
        if left > right:
            return 0

        l: int = left + self.size
        r: int = right + self.size
        result: int = 0

        while l <= r:
            if l & 1:
                result = max(result, self.data[l])
                l += 1
            if not (r & 1):
                result = max(result, self.data[r])
                r -= 1
            l >>= 1
            r >>= 1

        return result

    def find_first_at_least(self, left: int, right: int, target: int) -> int:
        """
        Find the smallest index in [left, right] whose stored value is at least target.

        Args:
            left: Left boundary, 0-based.
            right: Right boundary, 0-based.
            target: Required minimum value.

        Returns:
            The smallest valid index, or -1 if none exists.

        Time complexity:
            O(log n)

        Space complexity:
            O(1)
        """
        return self._find_first(1, 0, self.size - 1, left, right, target)

    def _find_first(
        self,
        node: int,
        seg_left: int,
        seg_right: int,
        query_left: int,
        query_right: int,
        target: int,
    ) -> int:
        """
        Recursive helper for find_first_at_least.

        Args:
            node: Current segment tree node.
            seg_left: Segment left boundary.
            seg_right: Segment right boundary.
            query_left: Query left boundary.
            query_right: Query right boundary.
            target: Required minimum value.

        Returns:
            Smallest valid index, or -1.

        Time complexity:
            O(log n)

        Space complexity:
            O(log n)
        """
        if seg_right < query_left or seg_left > query_right or self.data[node] < target:
            return -1

        if seg_left == seg_right:
            return seg_left

        mid: int = (seg_left + seg_right) // 2
        left_result: int = self._find_first(
            node * 2, seg_left, mid, query_left, query_right, target
        )
        if left_result != -1:
            return left_result

        return self._find_first(
            node * 2 + 1, mid + 1, seg_right, query_left, query_right, target
        )


class Solution:
    def max_throughput(self, jobs: List[List[int]], credits: List[List[int]]) -> int:
        """
        Compute the maximum total throughput value.

        Core idea:
        We process time in increasing order. At each event time:
        1. Remove expired credits.
        2. Add newly granted credits.
        3. Add newly released jobs.
        4. Repeatedly complete the currently best available job that fits in the
           currently active total credits.

        Why this greedy works:
        - Jobs never expire, only credits do.
        - If we decide to spend credits now, the safest credits to spend are the ones
          expiring earliest.
        - At any time, among all jobs that can be completed with the currently active
          credits, taking the highest-value one is optimal because delaying it cannot
          improve its value, while unused credits may expire.
        - Since jobs require an exact amount but can be done at any later time, we only
          need to know which released jobs are still uncompleted and what the current
          active credit total is.

        Data structures:
        - A Fenwick tree over compressed expiry times stores how many active credits
          remain for each expiry date. This lets us:
            * know total active credits
            * consume credits from earliest expiry first
        - For jobs, we group by creditsNeeded.
          For each requirement value, we keep a max-heap of job values.
          A segment tree stores the current best value for each requirement.
          Then, given current total active credits C, we can ask:
            "Among all requirements <= C, which requirement bucket has the maximum top value?"
          and remove one job from that bucket.

        Args:
            jobs: List of [releaseTime, creditsNeeded, value]
            credits: List of [grantTime, amount, expireTime]

        Returns:
            Maximum total throughput value.

        Time complexity:
            O((n + m) log(n + m))

        Space complexity:
            O(n + m)
        """
        # -----------------------------
        # Step 1: Prepare and sort jobs
        # -----------------------------
        # We sort jobs by release time because a job only becomes available starting
        # from that time. While sweeping through time, we will add jobs whose release
        # time has arrived.
        sorted_jobs: List[Tuple[int, int, int]] = sorted(
            (release, need, value) for release, need, value in jobs
        )

        # --------------------------------
        # Step 2: Prepare and sort credits
        # --------------------------------
        # We sort credit batches by grant time because credits only become usable
        # starting from that time.
        sorted_credits: List[Tuple[int, int, int]] = sorted(
            (grant, amount, expire) for grant, amount, expire in credits
        )

        # ---------------------------------------------------------
        # Step 3: Coordinate-compress all expiry times from credits
        # ---------------------------------------------------------
        # We need a Fenwick tree indexed by expiry date. Since expiry times can be as
        # large as 1e9, we compress them to 1..k.
        expiry_values: List[int] = sorted({expire for _, _, expire in sorted_credits})
        expiry_to_index = {value: idx + 1 for idx, value in enumerate(expiry_values)}

        # Fenwick tree stores active credit amounts by expiry bucket.
        fenwick: FenwickTree = FenwickTree(len(expiry_values))

        # ---------------------------------------------------------
        # Step 4: Compress all distinct creditsNeeded values for jobs
        # ---------------------------------------------------------
        # Jobs are grouped by exact credit requirement. For each requirement, we keep
        # a max-heap of values of released-but-not-yet-completed jobs.
        need_values: List[int] = sorted({need for _, need, _ in sorted_jobs})
        need_to_index = {need: idx for idx, need in enumerate(need_values)}

        # One heap per distinct requirement.
        # Python heapq is a min-heap, so we store negative values to simulate max-heap.
        heaps_by_need: List[List[int]] = [[] for _ in need_values]

        # Segment tree stores, for each requirement bucket, the current best value
        # available in that bucket. If bucket is empty, stored value is 0.
        seg_tree: SegmentTreeMax = SegmentTreeMax(len(need_values) if need_values else 1)

        # ---------------------------------------------------------
        # Step 5: Build the list of all event times to sweep through
        # ---------------------------------------------------------
        # Important observation:
        # Nothing changes except at:
        # - job release times
        # - credit grant times
        # - one step after a credit expiry time (because credits disappear immediately
        #   after expireTime, so they are unavailable starting at expireTime + 1)
        #
        # We process integer times, but only these event times matter.
        event_times_set = set()
        for release, _, _ in sorted_jobs:
            event_times_set.add(release)
        for grant, _, expire in sorted_credits:
            event_times_set.add(grant)
            event_times_set.add(expire + 1)

        event_times: List[int] = sorted(event_times_set)

        # ---------------------------------------------------------
        # Step 6: Sweep through event times in increasing order
        # ---------------------------------------------------------
        job_ptr: int = 0
        credit_ptr: int = 0

        # Min-heap of currently active credit batches by expiry time.
        # Each entry is (expireTime, amount, compressed_expiry_index).
        # We keep it so that when time advances, we can remove batches that have expired.
        active_batches_heap: List[Tuple[int, int, int]] = []

        # Track current total active credits explicitly for O(1) access.
        active_credit_total: int = 0

        # Final answer.
        total_value: int = 0

        for current_time in event_times:
            # -----------------------------------------------------
            # 6A. Remove credit batches that are no longer active
            # -----------------------------------------------------
            # A batch with expireTime < current_time has already disappeared.
            while active_batches_heap and active_batches_heap[0][0] < current_time:
                expire, amount, expiry_idx = heapq.heappop(active_batches_heap)
                fenwick.add(expiry_idx, -amount)
                active_credit_total -= amount

            # ---------------------------------------------
            # 6B. Add all credit batches granted right now
            # ---------------------------------------------
            while credit_ptr < len(sorted_credits) and sorted_credits[credit_ptr][0] == current_time:
                grant, amount, expire = sorted_credits[credit_ptr]
                expiry_idx = expiry_to_index[expire]
                heapq.heappush(active_batches_heap, (expire, amount, expiry_idx))
                fenwick.add(expiry_idx, amount)
                active_credit_total += amount
                credit_ptr += 1

            # -----------------------------------------
            # 6C. Add all jobs released at current time
            # -----------------------------------------
            while job_ptr < len(sorted_jobs) and sorted_jobs[job_ptr][0] == current_time:
                _, need, value = sorted_jobs[job_ptr]
                need_idx = need_to_index[need]
                heapq.heappush(heaps_by_need[need_idx], -value)

                # Update the segment tree with the new top value for this requirement.
                best_value_for_need: int = -heaps_by_need[need_idx][0]
                seg_tree.update(need_idx, best_value_for_need)
                job_ptr += 1

            # -------------------------------------------------------------------
            # 6D. Repeatedly complete the best currently feasible job, if any
            # -------------------------------------------------------------------
            # A job is feasible if creditsNeeded <= active_credit_total.
            # Among all such jobs, we want the one with maximum value.
            #
            # We find:
            # - the largest requirement bucket index whose need <= active_credit_total
            # - the maximum value among buckets in [0 .. that index]
            # - the leftmost bucket achieving that maximum
            #
            # Then we remove one job from that bucket and consume credits from the
            # earliest-expiring active batches first.
            while active_credit_total > 0 and need_values:
                # Find the rightmost requirement that can fit in current active credits.
                # We use binary search manually to avoid importing bisect unnecessarily.
                left: int = 0
                right: int = len(need_values) - 1
                feasible_right: int = -1

                while left <= right:
                    mid: int = (left + right) // 2
                    if need_values[mid] <= active_credit_total:
                        feasible_right = mid
                        left = mid + 1
                    else:
                        right = mid - 1

                # If no requirement fits, we cannot do any more jobs at this time.
                if feasible_right == -1:
                    break

                # Query the best value among all feasible requirement buckets.
                best_value: int = seg_tree.query_max(0, feasible_right)

                # If best value is 0, there is no released unfinished job that fits.
                if best_value == 0:
                    break

                # Find one requirement bucket that attains this best value.
                chosen_need_idx: int = seg_tree.find_first_at_least(0, feasible_right, best_value)
                chosen_need: int = need_values[chosen_need_idx]

                # Remove the selected job from its heap.
                heapq.heappop(heaps_by_need[chosen_need_idx])

                # Update segment tree for this requirement bucket after removal.
                new_top_value: int = -heaps_by_need[chosen_need_idx][0] if heaps_by_need[chosen_need_idx] else 0
                seg_tree.update(chosen_need_idx, new_top_value)

                # Add its value to the answer.
                total_value += best_value

                # ------------------------------------------------------------
                # Consume exactly chosen_need credits from earliest expiry first
                # ------------------------------------------------------------
                # This is the standard "earliest deadline first" way to spend a
                # fungible resource with expirations. It preserves later-expiring
                # credits for future times and is always safe.
                remaining_to_spend: int = chosen_need

                while remaining_to_spend > 0:
                    first_positive_idx: int = fenwick.find_first_positive()

                    # Amount currently available in this earliest expiry bucket.
                    amount_in_bucket: int = (
                        fenwick.prefix_sum(first_positive_idx)
                        - fenwick.prefix_sum(first_positive_idx - 1)
                    )

                    spend_here: int = min(remaining_to_spend, amount_in_bucket)
                    fenwick.add(first_positive_idx, -spend_here)
                    active_credit_total -= spend_here
                    remaining_to_spend -= spend_here

        return total_value


if __name__ == "__main__":
    solution = Solution()

    jobs1: List[List[int]] = [[1, 2, 8], [2, 1, 4], [3, 2, 7]]
    credits1: List[List[int]] = [[1, 2, 2], [2, 1, 3], [3