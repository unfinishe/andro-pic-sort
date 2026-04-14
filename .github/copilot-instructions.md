## Operational Rules for Design Authority
Refer to 'Design Authority' in AGENTS.md for full brand context.

1. **UI Implementation:** - Always use the Hex-Codes defined in AGENTS.md for Compose/XML.
    - Strictly follow Material 3 (M3) standards.
    - Backgrounds must always be `#F5F5F0`.
2. **Text:** - All string resources must be in 'Sentence case'.
3. **Icons & Graphics:** - Generate flat vector drawables only.
    - Reject any requests for shadows or gradients.
    - Base all new icons on the 'Horizon Point' logic (horizontal base line).
4. **UX Logic:** - Enforce 'One task - one tool'. If a screen gets too complex, suggest a split or BottomSheet.