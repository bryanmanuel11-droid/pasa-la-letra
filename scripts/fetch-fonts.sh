#!/usr/bin/env bash
# Descarga las fuentes del diseño desde el repositorio oficial de Google Fonts.
# Si algo falla, la app sigue funcionando con la letra del sistema.
set -u
DEST="app/src/main/assets/fonts"
mkdir -p "$DEST"

AUTH=()
if [ -n "${GITHUB_TOKEN:-}" ]; then AUTH=(-H "Authorization: Bearer $GITHUB_TOKEN"); fi

get() {
  local dir="$1" out="$2" url
  url=$(curl -fsSL "${AUTH[@]}" "https://api.github.com/repos/google/fonts/contents/ofl/$dir" \
    | jq -r '[.[] | select(.name | endswith(".ttf")) | select(.name | test("Italic") | not)][0].download_url')
  if [ -n "$url" ] && [ "$url" != "null" ]; then
    curl -fsSL -g "$url" -o "$DEST/$out" && echo "OK: $out"
  else
    echo "No encontré la fuente $dir"
  fi
}

get bricolagegrotesque bricolage.ttf
get dmsans dmsans.ttf
ls -la "$DEST"
