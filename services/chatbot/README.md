# Chatbot utility tests

From the repository root:

```sh
python -m pip install -r services/chatbot/requirements-dev.txt
PYTHONPATH=services/chatbot/src python -m pytest services/chatbot/src/chatbot/tests/test_agent_utils.py -q
```

The utility tests stub the LLM and database dependencies; they do not require a
running chatbot, database or API key.

Tool-output truncation includes JSON escaping and truncation markers in
`MAX_CONTENT_LENGTH`. It preserves JSON structure when the existing minimum
retained field lengths fit. Otherwise it falls back to bounded text, reserving
space for a marker when possible. Content already within the limit is unchanged.
