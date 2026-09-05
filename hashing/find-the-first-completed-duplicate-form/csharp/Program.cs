/*
Title: Find the First Completed Duplicate Form
Difficulty: Medium
Topic: Hashing

Problem Description:
A company stores online application forms as an ordered stream of field submissions. Each submission is represented by a pair [formId, fieldName], meaning that fieldName was submitted for the form with identifier formId. A form is considered complete once it has received all required field names at least once. If the same field is submitted multiple times for the same form, it should only count once toward completion.

Your task is to return the formId of the first form that becomes a completed duplicate. A form is a completed duplicate if, at the moment it becomes complete, there already exists at least one other completed form whose set of unique submitted fields is exactly the same. Since every completed form must contain all required fields, this effectively means the form has all required fields and another form has already completed with that same required set.

If multiple forms become completed duplicates during processing, return the one that reaches this condition earliest in the stream. If no form ever becomes a completed duplicate, return -1.

You must process submissions in order. A good solution should use hashing to track each form's unique fields and to count how many completed forms have already matched the required field signature.

Constraints:
- 1 <= number of submissions <= 200000
- 1 <= number of required fields <= 20
- requiredFields contains distinct strings
- 1 <= formId <= 10^9
- fieldName and each required field consist of lowercase English letters, with length between 1 and 20
- Submissions for the same form may appear many times and in any order
- Non-required fields may also appear in submissions and should be ignored

Example 1:
Input:
requiredFields = ["name","email","phone"]
submissions = [[101,"name"],[102,"email"],[101,"email"],[102,"name"],[101,"phone"],[102,"phone"]]
Output: 102

Explanation:
Form 101 becomes the first completed form after receiving name, email, and phone.
Later, form 102 also becomes complete with the same required field set, so 102 is the first completed duplicate.

Example 2:
Input:
requiredFields = ["id","photo"]
submissions = [[7,"id"],[8,"id"],[7,"photo"],[7,"photo"],[9,"photo"],[8,"photo"]]
Output: 8

Explanation:
Form 7 becomes complete first.
The repeated submission [7,"photo"] does not change anything.
Form 9 is incomplete because it never gets id.
When form 8 receives photo, it becomes complete and matches an already completed form, so the answer is 8.
*/

using System;
using System.Collections.Generic;

public class Solution
{
    /*
    Time Complexity:
    - O(S), where S is the number of submissions.
      Each submission is processed once, and every dictionary lookup / update is O(1) on average.

    Space Complexity:
    - O(F), where F is the number of distinct formIds seen in the stream.
      We store one bitmask per form, plus a small hash map for required fields.
    */
    public int FindFirstCompletedDuplicateForm(string[] requiredFields, (int formId, string fieldName)[] submissions)
    {
        // Step 1:
        // We assign each required field a unique bit position.
        //
        // Why?
        // Because the number of required fields is at most 20, which means we can represent
        // the set of fields a form has submitted using a single integer bitmask.
        //
        // Example:
        // requiredFields = ["name", "email", "phone"]
        // We might map:
        // "name"  -> bit 0
        // "email" -> bit 1
        // "phone" -> bit 2
        //
        // Then:
        // form with {"name","phone"} becomes binary 101
        // form with all three fields becomes binary 111
        //
        // This is extremely efficient for:
        // - checking whether a field was already seen
        // - adding a new field
        // - checking whether a form is complete
        var fieldToBit = new Dictionary<string, int>(requiredFields.Length);
        for (int i = 0; i < requiredFields.Length; i++)
        {
            fieldToBit[requiredFields[i]] = i;
        }

        // Step 2:
        // Build the "complete" mask.
        //
        // Why?
        // A form is complete exactly when it has all required fields.
        // If there are k required fields, then the complete mask is:
        // (1 << k) - 1
        //
        // Example:
        // k = 3 -> completeMask = 111(binary) = 7
        //
        // Any form whose current mask equals completeMask is complete.
        int completeMask = (1 << requiredFields.Length) - 1;

        // Step 3:
        // Track the current unique required-field set for each form.
        //
        // Key   = formId
        // Value = bitmask of required fields seen so far for that form
        //
        // Why dictionary?
        // formId values can be large (up to 1e9), so we cannot use an array indexed by formId.
        // A hash map lets us store only the forms that actually appear.
        var formMasks = new Dictionary<int, int>();

        // Step 4:
        // Track whether a form has already been counted as completed.
        //
        // Why is this necessary?
        // Once a form becomes complete, later duplicate submissions for that same form
        // must not cause us to count it again.
        //
        // Example:
        // If form 7 is already complete and receives "photo" again,
        // it should not be treated as "becoming complete" a second time.
        var completedForms = new HashSet<int>();

        // Step 5:
        // Track how many completed forms already exist for each completed signature.
        //
        // In this problem, every completed form must have the same required-field set:
        // exactly all required fields, which is completeMask.
        //
        // Still, using a dictionary keyed by signature keeps the solution aligned with
        // the hashing idea described in the prompt and makes the logic very explicit.
        //
        // Key   = completed field signature (bitmask)
        // Value = number of forms that have already completed with that signature
        var completedSignatureCount = new Dictionary<int, int>();

        // Step 6:
        // Process submissions in the exact order they arrive.
        //
        // Why?
        // The problem asks for the first form that becomes a completed duplicate in stream order.
        // So we must simulate the stream exactly from left to right.
        foreach (var submission in submissions)
        {
            int formId = submission.formId;
            string fieldName = submission.fieldName;

            // Step 6a:
            // Ignore non-required fields immediately.
            //
            // Why?
            // They do not contribute toward completion and should not affect the form's state.
            if (!fieldToBit.TryGetValue(fieldName, out int bitIndex))
            {
                continue;
            }

            // Step 6b:
            // Read the form's current mask. If this is the first time we see the form,
            // its mask starts at 0, meaning it has none of the required fields yet.
            formMasks.TryGetValue(formId, out int currentMask);

            // Step 6c:
            // Compute the bit corresponding to the current required field.
            int fieldBit = 1 << bitIndex;

            // Step 6d:
            // Add this field to the form's mask.
            //
            // Why OR?
            // Bitwise OR sets the bit to 1 and keeps all previously seen bits unchanged.
            //
            // Important:
            // If the same field is submitted again, OR-ing the same bit does nothing,
            // which naturally handles duplicates correctly.
            int updatedMask = currentMask | fieldBit;

            // Step 6e:
            // Save the updated mask back for this form.
            formMasks[formId] = updatedMask;

            // Step 6f:
            // If this form was already marked complete earlier, we skip further completion logic.
            //
            // Why?
            // A form only "becomes complete" once.
            // Later submissions should not trigger duplicate detection again for the same form.
            if (completedForms.Contains(formId))
            {
                continue;
            }

            // Step 6g:
            // Check whether this submission caused the form to become complete.
            //
            // This happens exactly when its updated mask equals the complete mask.
            if (updatedMask == completeMask)
            {
                // Step 6h:
                // We are at the exact moment this form becomes complete for the first time.
                // Now we must determine whether it is a completed duplicate.
                //
                // A completed duplicate means:
                // there already exists at least one other completed form with the same signature.
                //
                // Since completed forms here all have the full required set, the signature is completeMask.
                completedSignatureCount.TryGetValue(updatedMask, out int alreadyCompletedWithSameSignature);

                // Step 6i:
                // If at least one completed form with the same signature already exists,
                // then this is the first completed duplicate encountered in stream order,
                // because we process submissions from left to right and return immediately.
                if (alreadyCompletedWithSameSignature > 0)
                {
                    return formId;
                }

                // Step 6j:
                // Otherwise, this form is the first completed form with this signature.
                // We now record it as completed so future forms can match against it.
                completedForms.Add(formId);
                completedSignatureCount[updatedMask] = alreadyCompletedWithSameSignature + 1;
            }
        }

        // Step 7:
        // If we finish processing the entire stream and never returned,
        // then no form ever became a completed duplicate.
        return -1;
    }
}

// Demo code

var solution = new Solution();

// Example 1
string[] requiredFields1 = { "name", "email", "phone" };
(int formId, string fieldName)[] submissions1 =
{
    (101, "name"),
    (102, "email"),
    (101, "email"),
    (102, "name"),
    (101, "phone"),
    (102, "phone")
};

int result1 = solution.FindFirstCompletedDuplicateForm(requiredFields1, submissions1);
Console.WriteLine(result1); // Expected: 102

// Example 2
string[] requiredFields2 = { "id", "photo" };
(int formId, string fieldName)[] submissions2 =
{
    (7, "id"),
    (8, "id"),
    (7, "photo"),
    (7, "photo"),
    (9, "photo"),
    (8, "photo")
};

int result2 = solution.FindFirstCompletedDuplicateForm(requiredFields2, submissions2);
Console.WriteLine(result2); // Expected: 8

// Additional demo: no completed duplicate
string[] requiredFields3 = { "a", "b", "c" };
(int formId, string fieldName)[] submissions3 =
{
    (1, "a"),
    (1, "b"),
    (2, "a"),
    (3, "c"),
    (1, "c")
};

int result3 = solution.FindFirstCompletedDuplicateForm(requiredFields3, submissions3);
Console.WriteLine(result3); // Expected: -1