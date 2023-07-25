#!/usr/bin/env bash
git log -1 --pretty="changes.user=%an (%ae)%nchanges.date=%cd" --date=iso >> application.properties