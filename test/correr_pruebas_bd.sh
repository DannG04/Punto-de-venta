#!/usr/bin/env bash
# Pruebas funcionales contra PostgreSQL (clases *PruebaBD).
# 1) recrea la base de pruebas en Docker, 2) compila src/ + test/, 3) corre cada clase
# con un directorio de trabajo propio (db.properties, logs y archivos generados quedan ahí).
# Uso: test/correr_pruebas_bd.sh [Clase...]   (SIN_RECREAR=1 para reutilizar la base)
set -u
cd "$(dirname "$0")/.."
RAIZ="$(pwd -W 2>/dev/null || pwd)"
JDK="${JAVA_HOME:+$JAVA_HOME/bin}"
[ -z "$JDK" ] && JDK="$(ls -d ~/.vscode/extensions/redhat.java-*/jre/*/bin 2>/dev/null | tail -1)"
SEP=":"; case "$(uname -s)" in MINGW*|MSYS*|CYGWIN*) SEP=";";; esac

[ "${SIN_RECREAR:-0}" = "1" ] || bash test/preparar_bd_pruebas.sh || { echo "NO SE PUDO PREPARAR LA BASE"; exit 2; }

CLS="build/pruebas-bd/clases"; TRABAJO="build/pruebas-bd/trabajo"
rm -rf build/pruebas-bd && mkdir -p "$CLS" "$TRABAJO"
"$JDK/javac" --release 23 -encoding UTF-8 -nowarn -cp "lib/*" -d "$CLS" $(find src test -name "*.java") 2>&1 | grep -v "^Note:"
[ "${PIPESTATUS[0]}" -ne 0 ] && { echo "COMPILACION FALLIDA"; exit 2; }
cp src/version.properties "$CLS"/ 2>/dev/null || true
(cd src && find . -type f ! -name "*.java" ! -name "*.form" -exec cp --parents {} "../$CLS/" \;)

# 127.0.0.1 y no localhost: con Docker en Windows localhost tarda ~20 s por el intento IPv6
cat > "$TRABAJO/db.properties" <<EOF
db.host=127.0.0.1
db.port=${PUERTO_PRUEBAS:-15432}
db.name=pdv_pruebas
db.user=postgres
db.password=root
EOF
cp keystore.jks quagga.min.js "$TRABAJO"/ 2>/dev/null || true

clases="$*"
[ -z "$clases" ] && clases=$(cd test && find . -name "*PruebaBD.java" | sort | sed 's#^\./##; s#\.java$##; s#/#.#g')
rc=0
for c in $clases; do
  (cd "$TRABAJO" && "$JDK/java" -Djava.awt.headless=true -Dstdout.encoding=UTF-8 -Dstderr.encoding=UTF-8 -cp "$RAIZ/$CLS${SEP}$RAIZ/lib/*" "$c") || rc=1
done
exit $rc
