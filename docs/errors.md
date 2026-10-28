# Error reference

Every error response is `application/problem+json` ([RFC 9457](https://www.rfc-editor.org/rfc/rfc9457)) and carries the `requestId` that also appears in the `X-Request-Id` response header and in every server log line for that request.

```json
{
  "type": "https://github.com/C-J7/SP/blob/main/docs/errors.md#not-found",
  "title": "Not Found",
  "status": 404,
  "detail": "StudyPlan 999 was not found",
  "instance": "/api/study-plan/999",
  "requestId": "2027ac97-0399-468a-9384-28141584c594"
}
```

| Type | Status | When |
|------|--------|------|
| <a id="validation"></a>`validation` | 400 | Body or parameters failed validation. `errors` maps each field to its message. |
| <a id="not-found"></a>`not-found` | 404 | The syllabus, plan or session does not exist. |
| <a id="conflict"></a>`conflict` | 409 | Another request with the same `Idempotency-Key` is still in flight. Retry shortly. |
| <a id="concurrent-modification"></a>`concurrent-modification` | 409 | The plan changed between your read and your write. Re-read and retry. |
| `about:blank` | 412 | `If-Match` did not match the plan's current ETag. |
| `about:blank` | 413 | Upload larger than 10 MB. |
| <a id="unprocessable"></a>`unprocessable` | 422 | Well-formed but not acceptable: not a PDF, encrypted, no topics found, no free time in the range, or an `Idempotency-Key` reused with a different payload. |
| <a id="rate-limited"></a>`rate-limited` | 429 | Too many requests from one client. Honour `Retry-After`. |
| <a id="internal"></a>`internal` | 500 | Unexpected failure. Internal details are logged, never returned. Quote the `requestId`. |
