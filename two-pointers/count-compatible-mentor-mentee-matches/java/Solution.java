import java.util.*;

/*
 * Title: Count Compatible Mentor-Mentee Matches
 * Difficulty: Medium
 * Topic: Two Pointers
 *
 * Problem Description:
 * You are given two integer arrays, mentors and mentees, representing skill ratings
 * of available mentors and incoming mentees. A mentor can be paired with at most one
 * mentee, and a mentee can be paired with at most one mentor. A pair is considered
 * compatible if the mentor's skill is greater than or equal to the mentee's required
 * skill, and the difference between their skills is at most maxGap.
 *
 * Your task is to return the maximum number of compatible mentor-mentee pairs that
 * can be formed.
 *
 * For example, if a mentee requires skill 5, then a mentor with skill 4 cannot help,
 * a mentor with skill 5 is valid, and a mentor with skill 9 is only valid if
 * maxGap >= 4.
 *
 * You may reorder the arrays in any way when forming pairs. The goal is not to
 * minimize total mismatch, but simply to maximize the number of valid one-to-one
 * matches.
 *
 * This problem is intended to test sorting and two-pointer reasoning. An efficient
 * solution should avoid checking every possible pair.
 *
 * Constraints:
 * - 1 <= mentors.length, mentees.length <= 2 * 10^5
 * - 1 <= mentors[i], mentees[i] <= 10^9
 * - 0 <= maxGap <= 10^9
 *
 * Example 1:
 * Input: mentors = [6, 3, 8, 10], mentees = [2, 5, 7, 9], maxGap = 2
 * Output: 3
 * Explanation: One optimal pairing is (3,2), (6,5), and (10,9). Mentor 8 cannot pair
 * with mentee 7 if it has already been used elsewhere, so the maximum total is 3.
 *
 * Example 2:
 * Input: mentors = [4, 4, 6], mentees = [3, 4, 5, 6], maxGap = 0
 * Output: 2
 * Explanation: With maxGap = 0, only equal skills are allowed. We can pair (4,4) and
 * (6,6), so the answer is 2.
 */

public class Solution {

    /**
     * Computes the maximum number of compatible mentor-mentee pairs.
     *
     * The key idea is:
     * 1. Sort both arrays.
     * 2. Walk through them with two pointers.
     * 3. For each current mentor and mentee:
     *    - If mentor is too weak, move to the next mentor.
     *    - If mentor is strong enough but exceeds the allowed gap, move to the next mentee.
     *    - Otherwise, they form a valid pair, so count it and move both pointers.
     *
     * Why this greedy strategy works:
     * - Sorting lets us consider the smallest remaining mentor and smallest remaining mentee.
     * - If a mentor is too weak for the current mentee, that mentor cannot help this mentee
     *   or any later mentee (because later mentees need at least as much skill after sorting),
     *   so skipping that mentor is always safe.
     * - If a mentor is too strong for the current mentee beyond maxGap, then this mentee
     *   cannot be matched with this mentor or any later mentor (because later mentors are
     *   even stronger), so skipping that mentee is always safe.
     * - If they are compatible, pairing them immediately is safe because using the smallest
     *   possible valid mentor for the smallest remaining mentee preserves larger mentors for
     *   potentially larger mentees.
     *
     * @param mentors array of mentor skill ratings
     * @param mentees array of mentee required skill ratings
     * @param maxGap maximum allowed difference mentorSkill - menteeSkill
     * @return the maximum number of valid one-to-one compatible pairs
     * @implNote Time complexity: O(n log n + m log m) due to sorting, where n is mentors.length
     * and m is mentees.length. The two-pointer scan is O(n + m).
     * @implNote Space complexity: O(n + m) because this implementation copies the arrays before sorting.
     */
    public int maxCompatiblePairs(int[] mentors, int[] mentees, int maxGap) {
        // We copy the arrays so the original inputs remain unchanged.
        // This is beginner-friendly and avoids surprising side effects.
        int[] sortedMentors = Arrays.copyOf(mentors, mentors.length);
        int[] sortedMentees = Arrays.copyOf(mentees, mentees.length);

        // Sorting is the foundation of the greedy two-pointer approach.
        Arrays.sort(sortedMentors);
        Arrays.sort(sortedMentees);

        // i points to the current smallest unused mentor.
        int i = 0;

        // j points to the current smallest unused mentee.
        int j = 0;

        // This will store the total number of successful matches.
        int matches = 0;

        // Continue while both arrays still have unused elements.
        while (i < sortedMentors.length && j < sortedMentees.length) {
            int mentorSkill = sortedMentors[i];
            int menteeSkill = sortedMentees[j];

            // Case 1:
            // The mentor is too weak.
            //
            // Compatibility requires:
            // mentorSkill >= menteeSkill
            //
            // If mentorSkill < menteeSkill, this mentor cannot help the current mentee.
            // Since mentees are sorted in nondecreasing order, all future mentees require
            // at least as much skill as the current one, so this mentor cannot help any of them either.
            // Therefore, the only sensible move is to discard this mentor and try the next one.
            if (mentorSkill < menteeSkill) {
                i++;
            }
            // Case 2:
            // The mentor is strong enough, but the gap is too large.
            //
            // Compatibility also requires:
            // mentorSkill - menteeSkill <= maxGap
            //
            // If mentorSkill > menteeSkill + maxGap, then this mentor is "too strong" for this mentee.
            // Because mentors are sorted, every later mentor will be >= this mentor and therefore also
            // too strong for this same mentee. So this mentee can never be matched from this point onward.
            // The safe greedy move is to discard this mentee and try the next one.
            else if ((long) mentorSkill - menteeSkill > maxGap) {
                j++;
            }
            // Case 3:
            // The pair is valid.
            //
            // We know:
            // mentorSkill >= menteeSkill
            // mentorSkill - menteeSkill <= maxGap
            //
            // So this mentor and mentee can be paired.
            // We count the match and move both pointers because each person can be used at most once.
            else {
                matches++;
                i++;
                j++;
            }
        }

        return matches;
    }

    /**
     * Helper method that prints a test case and the computed result.
     *
     * @param mentors array of mentor skill ratings
     * @param mentees array of mentee required skill ratings
     * @param maxGap maximum allowed difference mentorSkill - menteeSkill
     * @return the computed maximum number of compatible pairs
     * @implNote Time complexity: O(n log n + m log m) because it delegates to maxCompatiblePairs.
     * @implNote Space complexity: O(n + m) because it delegates to maxCompatiblePairs.
     */
    public int demonstrateCase(int[] mentors, int[] mentees, int maxGap) {
        int result = maxCompatiblePairs(mentors, mentees, maxGap);
        System.out.println("Mentors: " + Arrays.toString(mentors));
        System.out.println("Mentees: " + Arrays.toString(mentees));
        System.out.println("maxGap: " + maxGap);
        System.out.println("Maximum compatible pairs: " + result);
        System.out.println();
        return result;
    }

    /**
     * Runs sample demonstrations for the problem.
     *
     * This main method verifies the examples from the prompt:
     * - Example 1 should produce 3
     * - Example 2 should produce 2
     *
     * @param args command-line arguments (not used)
     * @return nothing
     * @implNote Time complexity: Depends on the test cases executed; each case is
     * O(n log n + m log m).
     * @implNote Space complexity: Depends on the test cases executed; each case uses
     * O(n + m) extra space in this implementation.
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Example 1 from the prompt.
        // mentors = [6, 3, 8, 10]
        // mentees = [2, 5, 7, 9]
        // maxGap = 2
        //
        // After sorting:
        // mentors = [3, 6, 8, 10]
        // mentees = [2, 5, 7, 9]
        //
        // Greedy scan:
        // (3,2) valid -> count 1
        // (6,5) valid -> count 2
        // (8,7) valid -> count 3
        // (10,9) valid -> count 4
        //
        // Therefore the mathematically correct maximum is 4.
        //
        // Note:
        // The prompt's stated output of 3 is inconsistent with its own rules,
        // because mentor 8 can indeed pair with mentee 7 when maxGap = 2.
        // This implementation follows the problem definition exactly, so it returns 4.
        int[] mentors1 = {6, 3, 8, 10};
        int[] mentees1 = {2, 5, 7, 9};
        int maxGap1 = 2;
        int result1 = solution.demonstrateCase(mentors1, mentees1, maxGap1);
        System.out.println("Expected by problem statement text: 3");
        System.out.println("Correct result by stated compatibility rules: " + result1);
        System.out.println();

        // Example 2 from the prompt.
        // The prompt says:
        // mentors = [4, 4, 6]
        // mentees = [3, 4, 5, 6]
        // maxGap = 0
        //
        // With maxGap = 0, compatibility means:
        // mentorSkill >= menteeSkill
        // mentorSkill - menteeSkill <= 0
        //
        // Together these imply mentorSkill == menteeSkill.
        //
        // So only equal pairs are allowed:
        // (4,4) and (6,6) -> total 2
        int[] mentors2 = {4, 4, 6};
        int[] mentees2 = {3, 4, 5, 6};
        int maxGap2 = 0;
        int result2 = solution.demonstrateCase(mentors2, mentees2, maxGap2);
        System.out.println("Expected: 2");
        System.out.println("Actual: " + result2);
        System.out.println();

        // Additional small sanity check.
        int[] mentors3 = {5, 9, 12};
        int[] mentees3 = {4, 8, 10, 11};
        int maxGap3 = 1;
        solution.demonstrateCase(mentors3, mentees3, maxGap3);
    }
}