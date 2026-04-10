# AGENTS.md

## Codex task rules
- Treat the latest `relationship_detail.md` as the source of truth.
- Extract direct single-hop relationships only.
- Use JSqlParser for semantic parsing.
- Derive `line_no` and `line_content` from the original SQL text.
- Do not add multi-hop lineage to `relationship_detail.tsv`.
- Do not reintroduce removed columns such as:
  - `persistent_target_objects`
  - `intermediate_target_objects`
- For complex expressions:
  - `usage` rows may retain all direct participating tokens.
  - `map` rows should retain only the main direct value contributors.
- Prefer minimal, incremental changes.
- Keep tests updated with any behavior changes.
