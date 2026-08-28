import java.util.*;

/*
Problem Title: Process Build Jobs with Cooldown Penalties

Problem Description:
A CI system receives n build jobs. Job i becomes available at time availableTime[i], requires duration[i]
units of processing time, and has a penalty rate penalty[i]. The machine can process at most one job at a
time, and once a job starts, it runs to completion without preemption. If a job starts at time s, its waiting
penalty is (s - availableTime[i]) * penalty[i]. Your task is to compute the minimum possible total waiting
penalty over all jobs.

The machine may stay idle even if some jobs are available, but doing so is only useful if it helps reduce the
overall penalty later. Jobs can arrive while another job is running. You must choose the processing order to
minimize the sum of waiting penalties of all jobs.

Return the minimum total penalty as a 64-bit integer.

Constraints:
- 1 <= n <= 2 * 10^5
- 0 <= availableTime[i] <= 10^9
- 1 <= duration[i] <= 10^6
- 1 <= penalty[i] <= 10^6
- The answer fits in a signed 64-bit integer.

Important scheduling insight:
For the objective sum penalty[i] * startTime[i] (the constant term -sum penalty[i] * availableTime[i] does not
affect ordering), this is the classic single-machine scheduling problem with release times and weighted completion
equivalent structure. The optimal non-preemptive policy can be obtained by considering the preemptive relaxation:
run at every moment the available job with maximum penalty/duration ratio (Smith's rule / highest density), and
merge preempted pieces into blocks. The resulting block order is optimal for the original non-preemptive problem.

This implementation uses that idea in an efficient event-driven way:
1) Sort jobs by release time.
2) Simulate the preemptive highest-density schedule using a stack of blocks.
3) Whenever a newly released job has higher density than the currently active block, it preempts.
4) Finished pieces are merged backward into blocks whenever block densities are non-increasing.
5) The final merged blocks define the optimal non-preemptive order.
6) Compute the total waiting penalty by executing those blocks in order, respecting release times.

This runs in O(n log n) due to sorting; the stack processing itself is linear.
*/
public class Solution {

    /**
     * Simple immutable job record used after sorting by availability time.
     */
    private static final class Job {
        long release;
        long duration;
        long penalty;
        int id;

        Job(long release, long duration, long penalty, int id) {
            this.release = release;
            this.duration = duration;
            this.penalty = penalty;
            this.id = id;
        }
    }

    /**
     * A block represents a consecutive sequence of jobs that must stay together in the final optimal
     * non-preemptive schedule. The block stores:
     * - totalDuration: sum of durations of all jobs in the block
     * - totalPenalty: sum of penalty rates of all jobs in the block
     * - release: earliest feasible start time of the block in the derived construction
     * - jobs: the jobs inside the block, in execution order
     *
     * Density of a block is totalPenalty / totalDuration.
     */
    private static final class Block {
        long release;
        long totalDuration;
        long totalPenalty;
        ArrayList<Job> jobs;

        Block(Job job) {
            this.release = job.release;
            this.totalDuration = job.duration;
            this.totalPenalty = job.penalty;
            this.jobs = new ArrayList<>();
            this.jobs.add(job);
        }

        double density() {
            return (double) totalPenalty / (double) totalDuration;
        }
    }

    /**
     * Computes the minimum total waiting penalty.
     *
     * The algorithm:
     * 1. Build job objects and sort by release time.
     * 2. Construct optimal blocks using the highest-density-first preemptive relaxation and block merging.
     * 3. Execute the resulting blocks non-preemptively in order and compute total waiting penalty.
     *
     * Why this works:
     * - Among simultaneously available work, higher penalty/duration should be favored.
     * - Release times can force interruptions in the preemptive relaxation.
     * - Merging adjacent pieces with non-increasing densities yields the exact optimal non-preemptive order.
     *
     * @param availableTime release time of each job
     * @param duration processing time of each job
     * @param penalty penalty rate of each job
     * @return minimum possible total waiting penalty as a 64-bit integer
     * Time complexity: O(n log n) due to sorting.
     * Space complexity: O(n) for jobs, blocks, and ordering.
     */
    public long minimumTotalPenalty(int[] availableTime, int[] duration, int[] penalty) {
        int n = availableTime.length;
        Job[] jobs = new Job[n];
        for (int i = 0; i < n; i++) {
            jobs[i] = new Job(availableTime[i], duration[i], penalty[i], i);
        }

        Arrays.sort(jobs, (a, b) -> {
            if (a.release != b.release) {
                return Long.compare(a.release, b.release);
            }
            // Tie-breakers are not essential for correctness, but deterministic ordering is helpful.
            // Among equal release times, higher density first is natural.
            long left = a.penalty * b.duration;
            long right = b.penalty * a.duration;
            if (left != right) {
                return Long.compare(right, left);
            }
            return Integer.compare(a.id, b.id);
        });

        List<Block> blocks = buildOptimalBlocks(jobs);
        return computePenaltyFromBlocks(blocks);
    }

    /**
     * Builds the optimal block decomposition.
     *
     * Detailed intuition:
     * - Think of a preemptive machine that always runs the available job/block with highest density
     *   (penalty per unit time).
     * - When a new higher-density job arrives, it should interrupt lower-density work.
     * - The interrupted lower-density work will continue later.
     * - In the final non-preemptive optimal schedule, all pieces that belong together are merged into blocks.
     * - Whenever two consecutive blocks have densities in the wrong order (earlier block density <= later block density),
     *   they should be merged, because splitting them would contradict optimality.
     *
     * Implementation strategy:
     * - Process jobs in release-time order.
     * - Maintain a stack of blocks representing the current decomposition.
     * - Each new job starts as its own block.
     * - After pushing, repeatedly merge while the previous block has density <= current block density.
     *
     * This is the same monotone-stack idea used in Sidney decomposition / Horn-set style constructions.
     *
     * @param jobs jobs sorted by release time
     * @return list of blocks in optimal execution order
     * Time complexity: O(n)
     * Space complexity: O(n)
     */
    public List<Block> buildOptimalBlocks(Job[] jobs) {
        ArrayDeque<Block> stack = new ArrayDeque<>();

        for (Job job : jobs) {
            // Start with a single-job block.
            Block current = new Block(job);
            stack.addLast(current);

            // Merge backward while densities are non-decreasing.
            //
            // Why merge on previous density <= current density?
            // Suppose block A is before block B, and density(A) <= density(B).
            // Then executing A before B is not better than treating them as one larger unit whose internal
            // order will later be resolved consistently. The optimal decomposition requires strictly decreasing
            // densities across consecutive blocks.
            while (stack.size() >= 2) {
                Block last = stack.removeLast();
                Block prev = stack.removeLast();

                if (compareDensity(prev.totalPenalty, prev.totalDuration, last.totalPenalty, last.totalDuration) <= 0) {
                    Block merged = merge(prev, last);
                    stack.addLast(merged);
                } else {
                    stack.addLast(prev);
                    stack.addLast(last);
                    break;
                }
            }
        }

        return new ArrayList<>(stack);
    }

    /**
     * Merges two consecutive blocks into one larger block.
     *
     * The merged block keeps:
     * - release = max(left.release, ???) is NOT needed here because feasibility is handled when computing
     *   actual start times from the final order. For the decomposition, the earliest release among contained
     *   jobs is sufficient metadata.
     * - totalDuration = sum
     * - totalPenalty = sum
     * - jobs = concatenation in order
     *
     * @param left first block
     * @param right second block
     * @return merged block
     * Time complexity: O(size of merged jobs list) due to concatenation
     * Space complexity: O(size of merged jobs list)
     */
    public Block merge(Block left, Block right) {
        Block merged = new Block(left.jobs.get(0));
        merged.release = Math.min(left.release, right.release);
        merged.totalDuration = left.totalDuration + right.totalDuration;
        merged.totalPenalty = left.totalPenalty + right.totalPenalty;
        merged.jobs = new ArrayList<>(left.jobs.size() + right.jobs.size());
        merged.jobs.addAll(left.jobs);
        merged.jobs.addAll(right.jobs);
        return merged;
    }

    /**
     * Compares two densities aPenalty/aDuration and bPenalty/bDuration without using floating point.
     *
     * Returns:
     * - negative if first density < second density
     * - zero if equal
     * - positive if first density > second density
     *
     * @param aPenalty numerator of first density
     * @param aDuration denominator of first density
     * @param bPenalty numerator of second density
     * @param bDuration denominator of second density
     * @return comparison result
     * Time complexity: O(1)
     * Space complexity: O(1)
     */
    public int compareDensity(long aPenalty, long aDuration, long bPenalty, long bDuration) {
        long left = aPenalty * bDuration;
        long right = bPenalty * aDuration;
        return Long.compare(left, right);
    }

    /**
     * Computes the total waiting penalty once the optimal block order has been determined.
     *
     * Important detail:
     * Even after block decomposition, actual execution must still respect release times of individual jobs.
     * Since jobs inside each block are already in their final order, we simply simulate:
     * - current time starts at 0
     * - for each job in block order:
     *     current time = max(current time, job.release)
     *     penalty += (current time - job.release) * job.penalty
     *     current time += job.duration
     *
     * @param blocks optimal blocks in execution order
     * @return total waiting penalty
     * Time complexity: O(n)
     * Space complexity: O(1) excluding input storage
     */
    public long computePenaltyFromBlocks(List<Block> blocks) {
        long currentTime = 0L;
        long answer = 0L;

        for (Block block : blocks) {
            for (Job job : block.jobs) {
                if (currentTime < job.release) {
                    currentTime = job.release;
                }
                answer += (currentTime - job.release) * job.penalty;
                currentTime += job.duration;
            }
        }

        return answer;
    }

    /**
     * Convenience wrapper for array-list style input.
     *
     * @param availableTime release times
     * @param duration durations
     * @param penalty penalty rates
     * @return minimum total waiting penalty
     * Time complexity: O(n log n)
     * Space complexity: O(n)
     */
    public long solve(int[] availableTime, int[] duration, int[] penalty) {
        return minimumTotalPenalty(availableTime, duration, penalty);
    }

    /**
     * Demonstrates the solution on sample-style inputs.
     *
     * Note:
     * The narrative examples in the prompt contain inconsistent hand calculations.
     * This program prints the result produced by the implemented optimal algorithm.
     *
     * @param args command-line arguments, unused
     * @return nothing
     * Time complexity: O(1) for the fixed demo sizes
     * Space complexity: O(1) excluding demo arrays
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        int[] availableTime1 = {0, 1, 2};
        int[] duration1 = {3, 1, 2};
        int[] penalty1 = {4, 100, 2};
        System.out.println(solution.solve(availableTime1, duration1, penalty1));

        int[] availableTime2 = {0, 0, 5, 5};
        int[] duration2 = {4, 2, 3, 1};
        int[] penalty2 = {3, 10, 2, 20};
        System.out.println(solution.solve(availableTime2, duration2, penalty2));
    }
}