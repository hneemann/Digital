#!/bin/sh

# This script adds a menu item, icons and mime type for Digital for the current
# user. Based on Arduino install script.

echo "Installation starting..."

RESOURCE_NAME=digital-simulator

SCRIPT_PATH=$( cd $(dirname $0) ; pwd )
cd "${SCRIPT_PATH}"

TMP_DIR=`mktemp --directory`
echo "Generating temporary folder '${TMP_DIR}'..."

sed -e "s,<EXEC_LOCATION>,${SCRIPT_PATH}/Digital.sh,g" \
    -e "s,<ICON_LOCATION>,${SCRIPT_PATH}/icon.svg,g" "${SCRIPT_PATH}/linux/desktop.template" > "${TMP_DIR}/${RESOURCE_NAME}.desktop"

echo "Copying files..."

mkdir --verbose --parents "${HOME}/.local/share/applications/"
cp --verbose "${TMP_DIR}/${RESOURCE_NAME}.desktop" "${HOME}/.local/share/applications/"

mkdir --verbose --parents "${HOME}/.local/share/mime/packages/"
cp --verbose "${SCRIPT_PATH}/linux/${RESOURCE_NAME}.xml" "${HOME}/.local/share/mime/packages/"

echo "Removing temporary folder '${TMP_DIR}' and its contents..."
rm --verbose --recursive --force "${TMP_DIR}"

if [ -d "${HOME}/.local/share/applications" ]; then
    if command -v update-desktop-database > /dev/null; then
      echo "Updating desktop database..."
      update-desktop-database "${HOME}/.local/share/applications"
    fi
fi

if [ -d "${HOME}/.local/share/mime" ]; then
    if command -v update-mime-database > /dev/null; then
      echo "Updating mime database..."
      update-mime-database "${HOME}/.local/share/mime"
    fi
fi

echo "'Digital' installed successfully"
