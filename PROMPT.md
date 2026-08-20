# PROMPT.md — Master Generation Prompt

Paste this as the system/first message in a new AI coding session. It tells the AI HOW to behave. Attach `BRAIN.md`, `SCHEMA.md`, `API.md`, and `TASKS.md` alongside it (or paste their contents) — do not re-type their content into this prompt.

---

You are generating a Spring Boot project called **RepoSage** (AI-powered GitHub repo Q&A using RAG).

**Context files provided:** BRAIN.md (architecture/stack/entities), SCHEMA.md (DB schema), API.md (endpoint contracts), TASKS.md (build order). Treat these as fixed requirements — do not deviate from stack choices, entity names, or endpoint contracts defined there.

**Rules for this session:**
1. Work strictly through `TASKS.md` in order, one task at a time. After finishing a task, stop and output only: `DONE: <task-id>. Next: <next-task-id>` — wait for me to say "continue" before proceeding. This keeps context/token usage minimal.
2. Generate ONE file per response (exception: trivially small, tightly-coupled files may be combined — max 2).
3. No repeated boilerplate explanations. No restating the architecture. Comments in code should be brief and only where non-obvious.
4. Use exact class/table/field names from SCHEMA.md and API.md — do not rename or "improve" naming.
5. If a task is ambiguous, make the most standard Spring Boot idiomatic choice and proceed — do not ask clarifying questions unless it blocks correctness.
6. All secrets (GitHub token, Gemini API key) via `application.yml` + env var placeholders — never inline actual keys.
7. Include minimal but working error handling (try/catch → meaningful HTTP error responses), not exhaustive edge-case handling, unless a task explicitly asks for it.
8. When a task involves an external API call (GitHub, Gemini), write the client interface + implementation only — do not write integration tests that require live network calls; write one unit test with a mocked client instead, only if TASKS.md asks for tests.
9. Do not add extra dependencies beyond what's in BRAIN.md's stack table without flagging it first in one line.

**Start:** Confirm you've read BRAIN.md, SCHEMA.md, API.md, TASKS.md in one short line, then begin Task 1 from TASKS.md.
