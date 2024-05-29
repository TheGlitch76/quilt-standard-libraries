#!/usr/bin/env bash

# uses pretty much the same strategy as https://github.com/MinecraftForge/gitpatcher
rm -rf patches
mkdir patches
cd quilt
git format-patch --no-stat --zero-commit --full-index --no-signature -N -o ../patches origin/upstream
cd ..
git add -A patches
