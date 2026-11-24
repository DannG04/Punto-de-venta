#!/usr/bin/env bash
# Crea desde cero la base de pruebas (baseline + todas las migraciones + usuario admin/admin)
# en un contenedor Docker propio (puerto 15432). NO toca la base real "punto_de_venta".
# Uso: test/preparar_bd_pruebas.sh [contenedor] [base]
set -eu
cd "$(dirname "$0")/.."
CONT="${1:-pdv-pruebas-pg}"
PUERTO="${PUERTO_PRUEBAS:-15432}"
BD="${2:-pdv_pruebas}"
export MSYS_NO_PATHCONV=1
PSQL="docker exec -i $CONT psql -U postgres -v ON_ERROR_STOP=1 -q"

# Si el contenedor existe pero perdió el mapeo de puerto (pasa al reiniciar Docker), se recrea:
# solo tiene la base de pruebas, que de todas formas se rehace en cada corrida.
if docker inspect "$CONT" >/dev/null 2>&1; then
  docker start "$CONT" >/dev/null
  [ -n "$(docker port "$CONT" 5432 2>/dev/null)" ] || docker rm -f "$CONT" >/dev/null
fi
if ! docker inspect "$CONT" >/dev/null 2>&1; then
  # Puerto propio para no chocar con un PostgreSQL instalado en Windows (5432)
  docker run -d --name "$CONT" -e POSTGRES_PASSWORD=root -p "$PUERTO":5432 postgres:17 >/dev/null
fi
docker start "$CONT" >/dev/null
until docker exec "$CONT" pg_isready -U postgres >/dev/null 2>&1; do sleep 1; done

$PSQL -d postgres -c "DROP DATABASE IF EXISTS $BD WITH (FORCE)" -c "CREATE DATABASE $BD"
docker cp migrations/V1.0__baseline.tar "$CONT":/tmp/baseline.tar
docker exec "$CONT" pg_restore --no-owner -U postgres -d "$BD" /tmp/baseline.tar

# Migraciones en orden de versión (los nombres son irregulares: V1.14_x, v1.16__x)
for f in $(ls migrations/*.sql | sed -E 's#^.*/[Vv]([0-9.]+)_.*$#\1 &#' | sort -V | cut -d' ' -f2); do
  echo "  migracion $(basename "$f")"
  $PSQL -d "$BD" < "$f" >/dev/null
done
$PSQL -d "$BD" < seeds/seed_usuario.sql >/dev/null
echo "Base $BD lista en $CONT"
