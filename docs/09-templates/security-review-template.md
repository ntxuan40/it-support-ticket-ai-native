# Security Review Template

## 1. Review Scope

- Feature or module reviewed:
- Reviewer:
- Date:
- Related requirement(s):

## 2. Security Question Checklist

- [ ] Authentication context is validated
- [ ] Authorization is enforced on every protected action
- [ ] Identity headers or actor context are consumed by the backend
- [ ] Missing or invalid identity headers are rejected
- [ ] Users can only access resources allowed by role and ownership rules
- [ ] Sensitive data is not exposed in responses
- [ ] Error responses do not leak unauthorized resource details
- [ ] Business rules prevent state-changing abuse
- [ ] Inputs are validated before processing
- [ ] No obvious injection or unsafe data flow issue is identified

## 3. Findings

### Finding 1
- Severity: Low / Medium / High
- Title:
- Description:
- Evidence:
- Recommendation:
- Status: Open / Closed

### Finding 2
- Severity: Low / Medium / High
- Title:
- Description:
- Evidence:
- Recommendation:
- Status: Open / Closed

## 4. Risk Assessment

- Overall risk level: Low / Medium / High
- Residual risk:
- Notes:

## 5. Verification Evidence

- Command or evidence reviewed:
- Result:
- Related docs:

## 6. Final Decision

- Approved / Needs changes / Rejected
- Reason:
