git add .
git log --oneline|head -n1|grep __backup__ 1>/dev/null
#if [ $? -ne 0 ]; then
git commit -m "__backup__"
#else
#  git commit --amend --no-edit
#fi
#git push -f origin HEAD
git push origin HEAD
