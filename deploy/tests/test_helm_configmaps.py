"""Render the chart and validate ConfigMap data without a Kubernetes cluster."""

import os
import shutil
import subprocess
from pathlib import Path

import pytest
import yaml

CHART = Path(__file__).resolve().parents[1] / "helm"
HELM = os.environ.get("HELM_BINARY") or shutil.which("helm")


def render_configmaps(tmp_path, overrides):
    if not HELM:
        pytest.skip("Helm is required; install Helm or set HELM_BINARY")
    values = tmp_path / "values.yaml"
    values.write_text(yaml.safe_dump(overrides), encoding="utf-8")
    result = subprocess.run(
        [HELM, "template", "crapi", str(CHART), "-f", str(values)],
        check=True,
        capture_output=True,
        text=True,
    )
    return {
        document["metadata"]["name"]: document["data"]
        for document in yaml.safe_load_all(result.stdout)
        if document and document["kind"] == "ConfigMap"
    }


def assert_string_data(configmaps):
    assert configmaps, "The chart must render ConfigMaps"
    for name, data in configmaps.items():
        for key, value in data.items():
            assert isinstance(value, str), f"{name}.{key}: {value!r} is not a string"


def test_default_configmap_data_is_strings(tmp_path):
    assert_string_data(render_configmaps(tmp_path, {}))


@pytest.mark.parametrize(
    "credential",
    ["123456", "true", "null", "", " leading space ", "value: # comment\nnext line"],
)
def test_database_credentials_survive_yaml_rendering(tmp_path, credential):
    configmaps = render_configmaps(
        tmp_path,
        {
            "postgresdb": {
                "config": {
                    "postgresUser": credential,
                    "postgresPassword": credential,
                    "postgresDbName": credential,
                }
            },
            "mongodb": {
                "config": {
                    "mongoUser": credential,
                    "mongoPassword": credential,
                    "mongoDbName": credential,
                }
            },
        },
    )
    assert_string_data(configmaps)
    for name in (
        "crapi-chatbot-configmap",
        "crapi-community-configmap",
        "crapi-identity-configmap",
        "crapi-workshop-configmap",
    ):
        for key in ("DB_USER", "DB_PASSWORD", "DB_NAME"):
            assert configmaps[name][key] == credential
        if name != "crapi-identity-configmap":
            for key in ("MONGO_DB_USER", "MONGO_DB_PASSWORD", "MONGO_DB_NAME"):
                assert configmaps[name][key] == credential
    for key in ("POSTGRES_USER", "POSTGRES_PASSWORD", "POSTGRES_DB"):
        assert configmaps["postgres-config"][key] == credential
    for key in ("MONGO_INITDB_ROOT_USERNAME", "MONGO_INITDB_ROOT_PASSWORD"):
        assert configmaps["mongodb-config"][key] == credential
