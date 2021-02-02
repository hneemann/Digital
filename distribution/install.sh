#!/bin/sh

# This script adds a menu item, icons and mime type for Digital for the current
# user. Based on Arduino install script.

RESOURCE_NAME=digital-simulator

SCRIPT_PATH=$( cd $(dirname $0) ; pwd )
cd "${SCRIPT_PATH}"

TMP_DIR=`mktemp --directory`

sed -e "s,<EXEC_LOCATION>,${SCRIPT_PATH}/Digital.sh,g" \
    -e "s,<ICON_LOCATION>,${SCRIPT_PATH}/icon.svg,g" "${SCRIPT_PATH}/linux/desktop.template" > "${TMP_DIR}/${RESOURCE_NAME}.desktop"
    
mkdir -p "${HOME}/.local/share/applications"
cp "${TMP_DIR}/${RESOURCE_NAME}.desktop" "${HOME}/.local/share/applications/"

mkdir -p "${HOME}/.local/share/mime/packages"
cp "${SCRIPT_PATH}/linux/${RESOURCE_NAME}.xml" "${HOME}/.local/share/mime/packages"

rm "${TMP_DIR}/${RESOURCE_NAME}.desktop"
rmdir "${TMP_DIR}"

if [ -d "${HOME}/.local/share/applications" ]; then
    if command -v update-desktop-database > /dev/null; then
      update-desktop-database "${HOME}/.local/share/applications"
    fi
fi

if [ -d "${HOME}/.local/share/mime" ]; then
    if command -v update-mime-database > /dev/null; then
      update-mime-database "${HOME}/.local/share/mime"
    fi
fi
