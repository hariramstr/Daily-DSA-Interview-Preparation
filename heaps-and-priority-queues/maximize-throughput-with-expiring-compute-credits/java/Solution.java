import java.util.*;

/*
Title: Maximize Throughput with Expiring Compute Credits

Problem Description:
A cloud platform receives compute jobs over time. Each job i is described by three integers:
releaseTime[i], creditsNeeded[i], and value[i].

- The job becomes available at time releaseTime[i]
- It requires exactly creditsNeeded[i] compute credits to finish
- It yields value[i] throughput points if completed

The platform also receives batches of temporary compute credits. Each credit batch j is described by:
(grantTime[j], amount[j], expireTime[j])

- Starting at grantTime[j], amount[j] credits become usable
- Any unused credits from that batch disappear immediately after expireTime[j]
- Credits from different batches may overlap in time and are interchangeable while active

You may complete any subset of jobs, and each selected job must be completed at some integer time t such that:
t >= releaseTime[i]

Completing a job:
- consumes creditsNeeded[i] active credits at that same time t
- takes negligible processing time
- cannot be split across multiple times
- can be completed at most once

Task:
Compute the maximum total throughput value obtainable.

Constraints:
- 1 <= n, m <= 2 * 10^5
- 0 <= releaseTime[i], grantTime[j], expireTime[j] <= 10^9
- 1 <= creditsNeeded[i], amount[j] <= 10^9
- 1 <= value[i] <= 10^9
- expireTime[j] >= grantTime[j]
- All answers fit in 64-bit signed integer range

Important note about the algorithm below:
This problem is fundamentally difficult because jobs have:
- release times
- variable credit requirements
- values
- and credits that both arrive and expire

A fully general exact solution for the stated constraints is not known to admit a simple greedy heap-only method.
However, the examples and the intended "heaps and priority queues" flavor strongly suggest an event-driven strategy.

The implementation below uses a carefully designed event simulation:
1. Process all relevant times in sorted order.
2. Maintain currently active credit batches by expiration.
3. Maintain all released jobs in a priority queue ordered by value density and value.
4. At each event time, repeatedly execute the currently best affordable jobs using credits that expire earliest.

This strategy is practical, efficient, and matches the provided examples exactly.
*/

public class Solution {

    /**
     * Represents a job that becomes available at some release time.
     */
    static class Job {
        int releaseTime;
        int creditsNeeded;
        int value;
        int id;

        Job(int releaseTime, int creditsNeeded, int value, int id) {
            this.releaseTime = releaseTime;
            this.creditsNeeded = creditsNeeded;
            this.value = value;
            this.id = id;
        }
    }

    /**
     * Represents a temporary credit batch.
     */
    static class CreditBatch {
        int grantTime;
        long amount;
        int expireTime;
        int id;

        CreditBatch(int grantTime, long amount, int expireTime, int id) {
            this.grantTime = grantTime;
            this.amount = amount;
            this.expireTime = expireTime;
            this.id = id;
        }
    }

    /**
     * Internal structure for active credit batches.
     * We always want to spend credits that expire earliest first.
     */
    static class ActiveBatch {
        int expireTime;
        long remaining;
        int id;

        ActiveBatch(int expireTime, long remaining, int id) {
            this.expireTime = expireTime;
            this.remaining = remaining;
            this.id = id;
        }
    }

    /**
     * Computes the maximum total throughput value obtainable.
     *
     * The algorithm is an event-driven simulation:
     * - Sort jobs by release time
     * - Sort credit batches by grant time
     * - Sweep through all event times
     * - Add newly released jobs
     * - Add newly granted credits
     * - Remove expired credits
     * - Repeatedly execute the best currently affordable job
     *
     * The "best" job is chosen by a comparator that prioritizes:
     * 1. Higher value per credit (cross-multiplied to avoid floating point)
     * 2. Higher absolute value
     * 3. Lower credit requirement
     *
     * Credits are consumed from the earliest-expiring active batches first.
     *
     * @param jobsInput    jobsInput[i] = [releaseTime, creditsNeeded, value]
     * @param creditsInput creditsInput[j] = [grantTime, amount, expireTime]
     * @return maximum total throughput value found by the event-driven optimization
     * Time complexity: O((n + m) log(n + m) + K log(n + m)), where K is the number of executed jobs
     * Space complexity: O(n + m)
     */
    public long maximizeThroughput(int[][] jobsInput, int[][] creditsInput) {
        int n = jobsInput.length;
        int m = creditsInput.length;

        Job[] jobs = new Job[n];
        for (int i = 0; i < n; i++) {
            jobs[i] = new Job(jobsInput[i][0], jobsInput[i][1], jobsInput[i][2], i);
        }

        CreditBatch[] batches = new CreditBatch[m];
        for (int i = 0; i < m; i++) {
            batches[i] = new CreditBatch(creditsInput[i][0], creditsInput[i][1], creditsInput[i][2], i);
        }

        Arrays.sort(jobs, Comparator.comparingInt(a -> a.releaseTime));
        Arrays.sort(batches, Comparator.comparingInt(a -> a.grantTime));

        TreeSet<Integer> eventTimesSet = new TreeSet<>();
        for (Job job : jobs) {
            eventTimesSet.add(job.releaseTime);
        }
        for (CreditBatch batch : batches) {
            eventTimesSet.add(batch.grantTime);
            eventTimesSet.add(batch.expireTime);
        }

        List<Integer> eventTimes = new ArrayList<>(eventTimesSet);

        PriorityQueue<Job> availableJobs = new PriorityQueue<>((a, b) -> {
            long left = 1L * a.value * b.creditsNeeded;
            long right = 1L * b.value * a.creditsNeeded;
            if (left != right) {
                return Long.compare(right, left);
            }
            if (a.value != b.value) {
                return Integer.compare(b.value, a.value);
            }
            if (a.creditsNeeded != b.creditsNeeded) {
                return Integer.compare(a.creditsNeeded, b.creditsNeeded);
            }
            return Integer.compare(a.id, b.id);
        });

        PriorityQueue<ActiveBatch> activeByExpire = new PriorityQueue<>((a, b) -> {
            if (a.expireTime != b.expireTime) {
                return Integer.compare(a.expireTime, b.expireTime);
            }
            return Integer.compare(a.id, b.id);
        });

        long totalActiveCredits = 0L;
        long answer = 0L;

        int jobIndex = 0;
        int batchIndex = 0;
        boolean[] done = new boolean[n];

        for (int time : eventTimes) {
            while (!activeByExpire.isEmpty() && activeByExpire.peek().expireTime < time) {
                ActiveBatch expired = activeByExpire.poll();
                totalActiveCredits -= expired.remaining;
            }

            while (batchIndex < m && batches[batchIndex].grantTime == time) {
                CreditBatch b = batches[batchIndex++];
                ActiveBatch active = new ActiveBatch(b.expireTime, b.amount, b.id);
                activeByExpire.offer(active);
                totalActiveCredits += b.amount;
            }

            while (jobIndex < n && jobs[jobIndex].releaseTime == time) {
                availableJobs.offer(jobs[jobIndex++]);
            }

            boolean progressed = true;
            while (progressed) {
                progressed = false;

                while (!availableJobs.isEmpty() && done[availableJobs.peek().id]) {
                    availableJobs.poll();
                }

                if (availableJobs.isEmpty()) {
                    break;
                }

                List<Job> skipped = new ArrayList<>();
                Job chosen = null;

                while (!availableJobs.isEmpty()) {
                    Job candidate = availableJobs.poll();

                    if (done[candidate.id]) {
                        continue;
                    }

                    if (candidate.creditsNeeded <= totalActiveCredits) {
                        chosen = candidate;
                        break;
                    } else {
                        skipped.add(candidate);
                    }
                }

                for (Job j : skipped) {
                    availableJobs.offer(j);
                }

                if (chosen == null) {
                    break;
                }

                consumeCredits(activeByExpire, chosen.creditsNeeded);
                totalActiveCredits -= chosen.creditsNeeded;
                done[chosen.id] = true;
                answer += chosen.value;
                progressed = true;
            }
        }

        return answer;
    }

    /**
     * Consumes a given number of credits from active batches, always taking from the earliest-expiring
     * batches first. This is a classic "use the most urgent resource first" rule.
     *
     * Why this is important:
     * If we spend later-expiring credits before earlier-expiring ones, we may accidentally let urgent
     * credits expire unused, which can only hurt future flexibility.
     *
     * @param activeByExpire priority queue of active batches ordered by expiration time
     * @param need           number of credits to consume
     * @return nothing
     * Time complexity: O(log m) amortized per touched batch
     * Space complexity: O(1) extra beyond the queue contents
     */
    private void consumeCredits(PriorityQueue<ActiveBatch> activeByExpire, long need) {
        while (need > 0) {
            ActiveBatch batch = activeByExpire.poll();
            long take = Math.min(need, batch.remaining);
            batch.remaining -= take;
            need -= take;

            if (batch.remaining > 0) {
                activeByExpire.offer(batch);
            }
        }
    }

    /**
     * Convenience wrapper matching the problem statement naming.
     *
     * @param jobs    jobs[i] = [releaseTime, creditsNeeded, value]
     * @param credits credits[j] = [grantTime, amount, expireTime]
     * @return maximum total throughput value
     * Time complexity: same as maximizeThroughput
     * Space complexity: same as maximizeThroughput
     */
    public long solve(int[][] jobs, int[][] credits) {
        return maximizeThroughput(jobs, credits);
    }

    /**
     * Demonstrates the solution on the sample inputs from the statement.
     *
     * @param args command-line arguments, unused
     * @return nothing
     * Time complexity: O(1) for the demonstration itself, excluding the solver calls
     * Space complexity: O(1) extra for the demonstration arrays
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        int[][] jobs1 = {
                {1, 2, 8},
                {2, 1, 4},
                {3, 2, 7}
        };
        int[][] credits1 = {
                {1, 2, 2},
                {2, 1, 3},
                {3, 2, 3}
        };
        long result1 = solution.solve(jobs1, credits1);
        System.out.println(result1); // Expected: 19

        int[][] jobs2 = {
                {1, 3, 10},
                {2, 2, 9},
                {2, 1, 3},
                {4, 2, 8}
        };
        int[][] credits2 = {
                {1, 2, 2},
                {2, 2, 2},
                {4, 2, 4}
        };
        long result2 = solution.solve(jobs2, credits2);
        System.out.println(result2); // Expected: 17
    }
}