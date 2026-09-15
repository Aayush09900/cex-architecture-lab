#!/bin/bash

echo "Running project tests..."

if [ -f README.md ]; then
  echo "PASS: README.md exists"
else
  echo "FAIL: README.md is missing"
  exit 1
fi

if [ -f .github/workflows/git-check.yml ]; then
  echo "PASS: GitHub Actions workflow exists"
else
  echo "FAIL: GitHub Actions workflow is missing"
  exit 1
fi
echo "All project tests passed!"