#!/usr/bin/env bash
HEADER="# Automatically created, do not modify."
#WORKING_DIRECTORY="$1"
OUTPUT_FILE=src/main/resources/git.properties

COMMIT_ID=HEAD

SEPARATOR=", "
COMMIT_LENGTH=40
REF_LENGTH=10
CUT=$(($COMMIT_LENGTH + $REF_LENGTH + 2))

COMMIT_PROPERTY_NAME=commit
CHANGED_FILES_PROPERTY_NAME=changedFiles
FILES_PROPERTY_NAME=files

HAS_TAG_PROPERTY_NAME=hasTag
TAG_PROPERTY_NAME=tag

HAS_UNTRACKED_FILES_PROPERTY_NAME=hasUntrackedFiles
UNTRACKED_FILES_PROPERTY_NAME=untrackedFiles

rm ${OUTPUT_FILE}
touch ${OUTPUT_FILE}

DATE=$(date)
echo "# $DATE" >> ${OUTPUT_FILE}

#cd ${WORKING_DIRECTORY}

echo "" >> ${OUTPUT_FILE}

COMMIT=$(git rev-parse --verify ${COMMIT_ID})
echo "$COMMIT_PROPERTY_NAME=$COMMIT" >> ${OUTPUT_FILE}

# shows the uncommitted files
MODIFIED=$(git diff --name-only ${COMMIT_ID})

if [[ -z "$MODIFIED" ]]; then
	echo "$CHANGED_FILES_PROPERTY_NAME=false" >> ${OUTPUT_FILE}
else
	echo "$CHANGED_FILES_PROPERTY_NAME=true" >> ${OUTPUT_FILE}
	echo "$FILES_PROPERTY_NAME=${MODIFIED//$'\n'/$SEPARATOR}" >> ${OUTPUT_FILE}
fi

# lists all tags
echo "" >> ${OUTPUT_FILE}
TAG=$(git show-ref --tags | grep ${COMMIT})
if [[ -z "$TAG" ]]; then
	echo "$HAS_TAG_PROPERTY_NAME=false" >> ${OUTPUT_FILE}
else
	echo "$HAS_TAG_PROPERTY_NAME=true" >> ${OUTPUT_FILE}
	SIZE=$(echo ${TAG} | wc -c | tr -d '[:space:]')
	TAG=$(echo ${TAG} | cut -c${CUT}-${SIZE})
	echo "$TAG_PROPERTY_NAME=$TAG" >> ${OUTPUT_FILE}
fi

# checks for untracked files
echo "" >> ${OUTPUT_FILE}
UNTRACKED=$(git ls-files --others --exclude-standard)
if [[ -z "$UNTRACKED" ]]; then
	echo "$HAS_UNTRACKED_FILES_PROPERTY_NAME=false" >> ${OUTPUT_FILE}
else
	echo "$HAS_UNTRACKED_FILES_PROPERTY_NAME=true" >> ${OUTPUT_FILE}
	SIZE=$(echo ${TAG} | wc -c | tr -d '[:space:]')
	echo "$UNTRACKED_FILES_PROPERTY_NAME=${UNTRACKED//$'\n'/$SEPARATOR}" >> ${OUTPUT_FILE}
fi
