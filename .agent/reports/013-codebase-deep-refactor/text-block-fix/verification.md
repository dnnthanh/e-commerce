# Java single-line text block syntax fix

## Root cause

Java text blocks require a line terminator after the opening `"""`. The repository contained single-line constructs such as `@PreAuthorize("""...""")`, which Google Java Format/Spotless cannot parse and therefore cannot format.

## TDD evidence

- RED: `verification/verify_java_text_block_syntax.py` found 26 invalid single-line text blocks across 12 Java files.
- GREEN: all 26 were replaced with normal Java string literals.
- The regression gate is included in `verification/run_feature013_final_audit.sh`.

## Fresh verification

- `JAVA_TEXT_BLOCK_SYNTAX=PASS`
- `single_line_text_blocks=0`
- Feature 013 audit: 29 commands, all exit 0, `OVERALL=0`.

## Runtime note

This environment cannot execute the repository Maven/Spotless Java 25 build, so this report does not claim `mvn install` or `spotless:apply` ran here. The syntax blocker reported by Google Java Format is specifically covered by the new regression gate.
