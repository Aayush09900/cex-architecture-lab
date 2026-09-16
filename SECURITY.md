# Security Policy

## Reporting a vulnerability

Please do not open a public issue for a suspected security vulnerability.

Report security issues privately through GitHub's **Report a vulnerability** feature on this repository when available. Include:

- A clear description of the issue
- Steps to reproduce
- Affected files or components
- Potential impact
- Any safe remediation details

Do not include passwords, API keys, private keys, seed phrases, JWT secrets, database credentials, or other sensitive values in a report.

## Secret handling

- Never commit `.env` files, private keys, seed phrases, credentials, or production secrets.
- Use GitHub Actions encrypted secrets for CI/CD credentials.
- Use `.env.example` only for variable names and non-sensitive placeholders.
- Rotate a credential immediately if it may have been exposed.

## Security automation

This repository uses GitHub Actions, CodeQL, Gitleaks, and Dependabot to reduce common code and supply-chain risks.
