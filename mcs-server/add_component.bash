BASE=$(pwd)
function msg() {
  GREEN="\033[0;32m"
  RESET='\033[0m'
  printf "${GREEN}==> ${@}${RESET}\n"
}

function setupGradle() {
  local dest=$1
  for template in $BASE/gradle_templates/*.template; do
    cp -v "$template" "${dest}"/$(basename ${template%%.template})
  done
}

if [[ -d "$BASE"/components ]]; then
  msg "Found components"
  msg "Enter a component name"
  read -r COMPONENT_NAME
  COMPONENT_BASE="$BASE"/components/"$COMPONENT_NAME"
  mkdir -p "$COMPONENT_BASE"/src/{main,test}/java/at/ac/uibk/mcsconnect/${COMPONENT_NAME//-/.}
  setupGradle "$COMPONENT_BASE"
fi