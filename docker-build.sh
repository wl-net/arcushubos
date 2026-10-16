#!/bin/bash
#
# Build Arcus Hub OS inside Docker
#
# Usage:
#   ./docker-build.sh hubv2           # Release Hub V2 image
#   ./docker-build.sh hubv2-dev       # Dev Hub V2 image
#   ./docker-build.sh hubv3           # Release Hub V3 image
#   ./docker-build.sh hubv3-dev       # Dev Hub V3 image
#   ./docker-build.sh shell           # Interactive shell inside container
#   ./docker-build.sh <command>       # Run arbitrary command
#
# Options (before the target):
#   --build                           # Rebuild the Docker image first
#   --dev-gateway                     # Point the agent at the dev gateway
#                                     # instead of the production one set in
#                                     # build-*/conf/local.conf
#

set -e

# Use podman if available, fall back to docker
if command -v podman &>/dev/null; then
    CONTAINER_RT=podman
elif command -v docker &>/dev/null; then
    CONTAINER_RT=docker
else
    echo "Error: neither podman nor docker found" >&2
    exit 1
fi

IMAGE_NAME="arcushubos"
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"

# Persistent volumes for downloads and sstate-cache (survive container rebuilds)
DOWNLOADS_VOL="arcushubos-downloads"
SSTATE_VOL="arcushubos-sstate"

DEV_GATEWAY_URI="wss://hub.dev.arcus.wl-net.net/hub/1.0"

FORCE_BUILD=0
GATEWAY_URI=""
while [[ "$1" == --* ]]; do
    case "$1" in
        --build)       FORCE_BUILD=1 ;;
        --dev-gateway) GATEWAY_URI="$DEV_GATEWAY_URI" ;;
        *)
            echo "Error: unknown option $1" >&2
            exit 1
            ;;
    esac
    shift
done

# Build the Docker image if it doesn't exist (or pass --build to force)
if [[ $FORCE_BUILD == 1 ]]; then
    $CONTAINER_RT build \
        --build-arg UID="$(id -u)" \
        --build-arg GID="$(id -g)" \
        -t "$IMAGE_NAME" "$SCRIPT_DIR"
fi

if ! $CONTAINER_RT image inspect "$IMAGE_NAME" &>/dev/null; then
    echo "Docker image '$IMAGE_NAME' not found, building..."
    $CONTAINER_RT build \
        --build-arg UID="$(id -u)" \
        --build-arg GID="$(id -g)" \
        -t "$IMAGE_NAME" "$SCRIPT_DIR"
fi

TARGET="${1:-shell}"
shift 2>/dev/null || true

run_docker() {
    mkdir -p "$SCRIPT_DIR/output"

    # Podman rootless needs --userns=keep-id so the host UID maps 1:1 into the
    # container, allowing the bind-mounted repo to be writable.
    local userns_flag=""
    if [[ "$CONTAINER_RT" == "podman" ]]; then
        userns_flag="--userns=keep-id"
    fi

    # local.conf assigns IRIS_GATEWAY_URI with ?=, so a value passed through
    # the environment overrides it without editing the tracked config.
    local gateway_flags=()
    if [[ -n "$GATEWAY_URI" ]]; then
        gateway_flags=(-e IRIS_GATEWAY_URI="$GATEWAY_URI"
                       -e BB_ENV_PASSTHROUGH_ADDITIONS=IRIS_GATEWAY_URI)
    fi

    $CONTAINER_RT run --rm -it \
        $userns_flag \
        -v "$SCRIPT_DIR":/home/builder/arcushubos:Z \
        -v "$DOWNLOADS_VOL":/home/builder/arcushubos/build-ti/downloads:Z \
        -v "$DOWNLOADS_VOL":/home/builder/arcushubos/build-fsl/downloads:Z \
        -v "$SSTATE_VOL":/build/sstate-cache:Z \
        -v "$SCRIPT_DIR/output":/tftpboot:Z \
        -e TERM="$TERM" \
        "${gateway_flags[@]}" \
        "$IMAGE_NAME" \
        "$@"
}

case "$TARGET" in
    shell)
        run_docker "bash"
        ;;
    hubv2|hubv2-dev|hubv3|hubv3-dev)
        echo "=== Building target: $TARGET ==="
        [[ -n "$GATEWAY_URI" ]] && echo "=== Gateway: $GATEWAY_URI ==="
        run_docker "cd /home/builder/arcushubos && make $TARGET"
        ;;
    hubv2-clean|hubv2-distclean|hubv3-clean|hubv3-distclean)
        run_docker "cd /home/builder/arcushubos && make $TARGET"
        ;;
    *)
        # Run arbitrary command
        run_docker "$TARGET $*"
        ;;
esac
