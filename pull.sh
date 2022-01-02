git fetch --all
branch=$(git rev-parse --abbrev-ref HEAD)
git reset --hard remotes/origin/$branch
