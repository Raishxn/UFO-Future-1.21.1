---
description: Auto commit and push to Git when the user reports a successful implementation
---

# GitHub Sync Process — UFO Future

This workflow defines the standard operating procedure for saving and uploading code to the version control system (Git) upon the successful implementation of a feature or bug fix.

---

## When to trigger this workflow

Only trigger this workflow when the user **explicitly states** that the current task was successful, using phrases such as:
- "Tivemos sucesso" (We had success)
- "Funcionou" (It worked)
- "Pode commitar" (You can commit)
- "Commit and push"

Do **NOT** trigger this if the user is just reporting a bug, asking for modifications, or giving partial success feedback where more changes are needed.

---

## The Workflow Steps

When triggered, execute the following steps using the `run_command` tool. 

// turbo-all
1. Check the status of modified files to ensure you know what is being committed:
   `git status`

2. Add all modified files:
   `git add .`

3. Commit with a meaningful, standard message summarizing the feature that was just successfully concluded. (e.g., "feat: implement Stellar Nexus big integer hatch"). Be descriptive!
   `git commit -m "feat: [descrição resumida em inglês do que foi feito]"`

4. Push the code to the repository:
   `git push`

---

## Best Practices

- **Conventional Commits**: Use prefix tags like `feat:`, `fix:`, `refactor:`, `chore:`, or `docs:` in the commit message.
- **Review before adding**: Ensure that no unwanted temporary files or `.log` files are committed. If necessary, check if `.gitignore` is properly configured.
- **Report back**: Inform the player exactly what was pushed to GitHub.
