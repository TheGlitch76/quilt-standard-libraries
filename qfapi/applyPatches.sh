#!/usr/bin/env bash
# This will just be the gitpatcher gradle plugin later. use git bash on windows for now
# copy the current head to the "upstream" branch
git --git-dir=fabric/.git branch -f upstream

if [ -d quilt ]; then
  # todo some kind of protection against erasing work here
  echo "repo already exists"
else
  git clone --no-checkout fabric quilt
fi

cd quilt
git fetch origin #todo: is this really needed
git checkout -B work origin/upstream
git reset --hard
git am --abort
git am --3way ../patches/*

