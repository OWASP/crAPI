# Helm rendering tests

Install Helm 3 and the Python test dependencies, then run from the repository root:

```sh
python -m pip install -r deploy/tests/requirements.txt
python -m pytest deploy/tests -q
helm lint deploy/helm
```

Helm must be on `PATH`, or set `HELM_BINARY` to its executable path. These tests
render the chart locally; they do not need a Kubernetes cluster. They verify that
ConfigMap data contains strings and database credentials survive YAML rendering,
including numeric-looking values, empty strings, whitespace and special characters.
