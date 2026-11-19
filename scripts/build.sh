#!/usr/bin/env bash
set -euo pipefail

root_dir=$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)
paper_api_version=${PAPER_API_VERSION:-26.3}
# Paper rejects a plugin whose api-version is newer than the server. Every
# 1.21.x server accepts the 1.21 API line, so a single 1.21.x artifact loads on
# 1.21.1 through 1.21.11; 26.3 builds declare their own API line.
if [[ -n "${PAPER_PLUGIN_API_VERSION:-}" ]]; then
  plugin_api_version=${PAPER_PLUGIN_API_VERSION}
elif [[ "${paper_api_version}" == 1.21* ]]; then
  plugin_api_version=1.21
else
  plugin_api_version=${paper_api_version}
fi

output=${1:-"${root_dir}/dist/HammerCore-1.4.0-${paper_api_version}.jar"}
cd "${root_dir}"
mvn -q clean package \
  -Dpaper.version="${paper_api_version}" \
  -Dpaper.api-version="${plugin_api_version}"

mkdir -p "$(dirname "${output}")"
cp "${root_dir}/target/HammerCore-1.4.0-${paper_api_version}.jar" "${output}"
printf 'Built %s from %s %s for Paper %s (plugin api-version %s).\n' \
  "${output}" "HammerCore" "$(git rev-parse --short HEAD)" "${paper_api_version}" "${plugin_api_version}"
