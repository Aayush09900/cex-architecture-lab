#!/bin/bash
set -euo pipefail

echo "Running project checks..."

required_files=(
  "README.md"
  "CONTRIBUTING.md"
  "SECURITY.md"
  ".github/workflows/git-check.yml"
  ".github/workflows/security.yml"
  ".github/workflows/codeql.yml"
)

for file in "${required_files[@]}"; do
  if [[ -f "$file" ]]; then
    echo "PASS: $file exists"
  else
    echo "FAIL: $file is missing"
    exit 1
  fi
done

if grep -q "Detailed decisions are documented in" README.md; then
  echo "PASS: README points to architecture documentation"
else
  echo "FAIL: README architecture documentation link is missing"
  exit 1
fi

echo "All project checks passed!"
