import java.util.*;

/*
Problem Title: Find the First Completed Duplicate Form

Problem Description:
A company stores online application forms as an ordered stream of field submissions.
Each submission is represented by a pair [formId, fieldName], meaning that fieldName
was submitted for the form with identifier formId.

A form is considered complete once it has received all required field names at least once.
If the same field is submitted multiple times for the same form, it should only count once
toward completion.

Your task is to return the formId of the first form that becomes a completed duplicate.
A form is a completed duplicate if, at the moment it becomes complete, there already exists
at least one other completed form whose set of unique submitted fields is exactly the same.

Since every completed form must contain all required fields, this effectively means the form
has all required fields and another form has already completed with that same required set.

If multiple forms become completed duplicates during processing, return the one that reaches
this condition earliest in the stream. If no form ever becomes a completed duplicate, return -1.

You must process submissions in order. A good solution should use hashing to track each form's
unique fields and to count how many completed forms have already matched the required field signature.

Constraints:
- 1 <= number of submissions <= 200000
- 1 <= number of required fields <= 20
- requiredFields contains distinct strings
- 1 <= formId <= 10^9
- fieldName and each required field consist of lowercase English letters, with length between 1 and 20
- Submissions for the same form may appear many times and in any order
- Non-required fields may also appear in submissions and should be ignored
*/

public class Solution {

    /**
     * Small helper object that stores the current state of one form.
     *
     * We use:
     * - mask: a bitmask of which required fields have been seen for this form
     * - completed: whether this form has already become complete before
     *
     * Why a bitmask?
     * Because the number of required fields is at most 20, so we can map each required field
     * to one bit position. This makes updates and completion checks very fast.
     */
    private static class FormState {
        long mask;
        boolean completed;

        FormState(long mask, boolean completed) {
            this.mask = mask;
            this.completed = completed;
        }
    }

    /**
     * Finds the formId of the first form that becomes a completed duplicate while processing
     * the submissions in order.
     *
     * Detailed idea:
     * 1. Assign each required field a bit position.
     * 2. For each formId, maintain a bitmask representing which required fields have been submitted.
     * 3. Ignore any submission whose field is not required.
     * 4. When a form receives a required field, set the corresponding bit in its mask.
     * 5. If the form becomes complete for the first time (its mask equals the full required mask),
     *    then check whether another completed form already exists with that same completed signature.
     * 6. Since every completed form must have exactly all required fields, the signature is always
     *    the same full mask. So we only need to count how many forms have already completed.
     * 7. If at least one completed form already exists when the current form becomes complete,
     *    then the current form is the first completed duplicate at that moment, so return its formId.
     *
     * @param requiredFields the list of distinct required field names that define completion
     * @param submissions the ordered stream of submissions, where each element is [formId, fieldName]
     * @return the formId of the first form that becomes a completed duplicate; returns -1 if none exists
     * Time complexity: O(R + S), where R is the number of required fields and S is the number of submissions
     * Space complexity: O(R + F), where F is the number of distinct formIds seen
     */
    public int findFirstCompletedDuplicateForm(String[] requiredFields, Object[][] submissions) {
        Map<String, Integer> fieldToBit = buildFieldToBitMap(requiredFields);

        // fullMask has all required-field bits set to 1.
        // Example: if there are 3 required fields, fullMask = 0b111.
        long fullMask = buildFullMask(requiredFields.length);

        // Stores the evolving state for each formId.
        Map<Integer, FormState> forms = new HashMap<>();

        // Counts how many forms have already become complete.
        // The first completed form is not a duplicate.
        // The second completed form is the first duplicate, because it matches the same required set.
        int completedFormsCount = 0;

        // Process submissions strictly in the given order.
        for (Object[] submission : submissions) {
            int formId = (Integer) submission[0];
            String fieldName = (String) submission[1];

            // If the field is not required, it does not affect completion at all.
            Integer bitIndex = fieldToBit.get(fieldName);
            if (bitIndex == null) {
                continue;
            }

            // Get or create the state for this form.
            FormState state = forms.get(formId);
            if (state == null) {
                state = new FormState(0L, false);
                forms.put(formId, state);
            }

            // If the form is already complete, then additional submissions cannot make it
            // "become complete" again. We can still safely ignore further work.
            if (state.completed) {
                continue;
            }

            // Compute the bit corresponding to this required field.
            long bit = 1L << bitIndex;

            // If this bit is already set, then this is a duplicate submission of the same field
            // for the same form. It should count only once, so nothing changes.
            if ((state.mask & bit) != 0) {
                continue;
            }

            // Mark this required field as seen for the form.
            state.mask |= bit;

            // After adding the new field, check whether the form has now become complete.
            if (state.mask == fullMask) {
                // Mark this form as completed so future submissions do not re-trigger completion.
                state.completed = true;

                // If at least one form had already completed earlier, then this form is a
                // completed duplicate at the exact moment it becomes complete.
                if (completedFormsCount > 0) {
                    return formId;
                }

                // Otherwise, this is the first completed form overall.
                completedFormsCount++;
            }
        }

        // No form ever became a completed duplicate.
        return -1;
    }

    /**
     * Convenience overload that accepts submissions as a String matrix where:
     * submissions[i][0] is the formId as a string
     * submissions[i][1] is the fieldName
     *
     * This is useful for simple demonstrations or environments where all input is string-based.
     *
     * @param requiredFields the list of distinct required field names
     * @param submissions the ordered stream of submissions as string pairs [formId, fieldName]
     * @return the formId of the first form that becomes a completed duplicate; returns -1 if none exists
     * Time complexity: O(R + S), where R is the number of required fields and S is the number of submissions
     * Space complexity: O(R + F), where F is the number of distinct formIds seen
     */
    public int findFirstCompletedDuplicateForm(String[] requiredFields, String[][] submissions) {
        Object[][] converted = new Object[submissions.length][2];
        for (int i = 0; i < submissions.length; i++) {
            converted[i][0] = Integer.parseInt(submissions[i][0]);
            converted[i][1] = submissions[i][1];
        }
        return findFirstCompletedDuplicateForm(requiredFields, converted);
    }

    /**
     * Builds a mapping from each required field name to a unique bit index.
     *
     * Example:
     * requiredFields = ["name", "email", "phone"]
     * might produce:
     * "name"  -> 0
     * "email" -> 1
     * "phone" -> 2
     *
     * Then a form that has seen "name" and "phone" would have mask:
     * 0b101
     *
     * @param requiredFields the distinct required field names
     * @return a map from field name to bit index
     * Time complexity: O(R)
     * Space complexity: O(R)
     */
    public Map<String, Integer> buildFieldToBitMap(String[] requiredFields) {
        Map<String, Integer> fieldToBit = new HashMap<>();
        for (int i = 0; i < requiredFields.length; i++) {
            fieldToBit.put(requiredFields[i], i);
        }
        return fieldToBit;
    }

    /**
     * Builds a mask with the lowest fieldCount bits set to 1.
     *
     * Examples:
     * fieldCount = 1 -> 0b1
     * fieldCount = 2 -> 0b11
     * fieldCount = 3 -> 0b111
     *
     * Since fieldCount <= 20, using a long is completely safe.
     *
     * @param fieldCount the number of required fields
     * @return the full completion mask
     * Time complexity: O(1)
     * Space complexity: O(1)
     */
    public long buildFullMask(int fieldCount) {
        return (1L << fieldCount) - 1L;
    }

    /**
     * Demonstrates the solution using the sample inputs from the problem statement.
     *
     * Expected outputs:
     * Example 1 -> 102
     * Example 2 -> 8
     *
     * @param args command-line arguments, not used
     * @return nothing
     * Time complexity: O(1) for the demonstration itself, excluding the called algorithm
     * Space complexity: O(1) for the demonstration itself, excluding the called algorithm
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Example 1
        // requiredFields = ["name","email","phone"]
        // submissions = [[101,"name"],[102,"email"],[101,"email"],[102,"name"],[101,"phone"],[102,"phone"]]
        //
        // Trace:
        // - 101 gets name
        // - 102 gets email
        // - 101 gets email
        // - 102 gets name
        // - 101 gets phone -> 101 becomes the first completed form
        // - 102 gets phone -> 102 becomes complete, and 101 already completed earlier
        // Therefore answer is 102.
        String[] requiredFields1 = {"name", "email", "phone"};
        Object[][] submissions1 = {
                {101, "name"},
                {102, "email"},
                {101, "email"},
                {102, "name"},
                {101, "phone"},
                {102, "phone"}
        };
        System.out.println(solution.findFirstCompletedDuplicateForm(requiredFields1, submissions1));

        // Example 2
        // requiredFields = ["id","photo"]
        // submissions = [[7,"id"],[8,"id"],[7,"photo"],[7,"photo"],[9,"photo"],[8,"photo"]]
        //
        // Trace:
        // - 7 gets id
        // - 8 gets id
        // - 7 gets photo -> 7 becomes the first completed form
        // - 7 gets photo again -> duplicate field, ignored
        // - 9 gets photo -> incomplete, still missing id
        // - 8 gets photo -> 8 becomes complete, and 7 already completed earlier
        // Therefore answer is 8.
        String[] requiredFields2 = {"id", "photo"};
        Object[][] submissions2 = {
                {7, "id"},
                {8, "id"},
                {7, "photo"},
                {7, "photo"},
                {9, "photo"},
                {8, "photo"}
        };
        System.out.println(solution.findFirstCompletedDuplicateForm(requiredFields2, submissions2));

        // Additional small demonstration with no duplicate completion.
        String[] requiredFields3 = {"a", "b"};
        Object[][] submissions3 = {
                {1, "a"},
                {2, "x"},
                {1, "b"},
                {3, "a"}
        };
        System.out.println(solution.findFirstCompletedDuplicateForm(requiredFields3, submissions3));
    }
}