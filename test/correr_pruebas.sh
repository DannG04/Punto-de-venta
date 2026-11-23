#!/usr/bin/env bash
# Compila src/ + test/ en build/pruebas y corre cada clase *Prueba. Uso: test/correr_pruebas.sh [Clase...]
set -u
cd "$(dirname "$0")/.."
JDK="${JAVA_HOME:+$JAVA_HOME/bin}"
[ -z "$JDK" ] && JDK="$(ls -d ~/.vscode/extensions/redhat.java-*/jre/*/bin 2>/dev/null | tail -1)"
SEP=":"; case "$(uname -s)" in MINGW*|MSYS*|CYGWIN*) SEP=";";; esac
rm -rf build/pruebas && mkdir -p build/pruebas
"$JDK/javac" --release 23 -encoding UTF-8 -nowarn -cp "lib/*" -d build/pruebas $(find src test -name "*.java") 2>&1 | grep -v "^Note:" 
[ "${PIPESTATUS[0]}" -ne 0 ] && { echo "COMPILACION FALLIDA"; exit 2; }
cp src/version.properties build/pruebas/ 2>/dev/null || true
(cd src && find . -type f ! -name "*.java" ! -name "*.form" -exec cp --parents {} ../build/pruebas/ \;)
clases="$*"
[ -z "$clases" ] && clases=$(cd test && find . -name "*Prueba.java" ! -path "./pruebas/*" | sed 's#^\./##; s#\.java$##; s#/#.#g')
rc=0
for c in $clases; do "$JDK/java" -Djava.awt.headless=true -cp "build/pruebas${SEP}lib/*" "$c" || rc=1; done
exit $rc
