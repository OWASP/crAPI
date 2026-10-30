"""
Helpers that make resources/crapi-openapi-spec.json consumable by FastMCP.

OpenAPIRefResolver inlines every ``$ref`` so each tool's schema is
self-contained, and fix_array_responses_in_spec removes top-level array
response schemas, which FastMCP's OpenAPI import rejects. server.py applies
both before FastMCP.from_openapi. get_any_api_key returns an LLM key (from the
environment or any stored chat session in Mongo) for code paths that need one
outside a request.
"""

from chatbot.config import Config
from chatbot.extensions import db


async def get_any_api_key():
    if Config.LLM_API_KEY:
        return Config.LLM_API_KEY
    doc = await db.sessions.find_one(
        {"openai_api_key": {"$exists": True, "$ne": None}}, {"openai_api_key": 1}
    )
    if doc and "openai_api_key" in doc:
        return doc["openai_api_key"]
    return None

def fix_array_responses_in_spec(spec):
    for path_item in spec.get("paths", {}).values():
        for method, operation in path_item.items():
            if method not in ["get", "post", "put", "patch", "delete"]:
                continue
            
            for response in operation.get("responses", {}).values():
                for media in response.get("content", {}).values():
                    schema = media.get("schema", {})
                    
                    if schema.get("type") == "array":
                        del media["schema"]

class OpenAPIRefResolver:
    def __init__(self, spec):
        self.spec = spec
        self.components = spec.get("components", {}).get("schemas", {})
        
    def resolve_ref(self, ref):
        if not ref.startswith("#/components/schemas/"):
            return None
        
        schema_name = ref.split("/")[-1]
        if schema_name not in self.components:
            return None
        
        return self.components[schema_name]
    
    def inline_all_refs(self, schema, visited=None):
        if visited is None:
            visited = set()
        
        if isinstance(schema, dict):
            if "$ref" in schema:
                ref = schema["$ref"]
                if ref.startswith("#/components/schemas/"):
                    schema_name = ref.split("/")[-1]
                    
                    if schema_name in visited:
                        return {"type": "object", "description": f"Circular reference to {schema_name}"}
                    
                    visited.add(schema_name)
                    resolved = self.resolve_ref(ref)
                    if resolved:
                        inlined = self.inline_all_refs(resolved, visited.copy())
                        visited.discard(schema_name)
                        return inlined
                    else:
                        return schema
                else:
                    return schema
            else:
                return {key: self.inline_all_refs(value, visited) for key, value in schema.items()}
        elif isinstance(schema, list):
            return [self.inline_all_refs(item, visited) for item in schema]
        else:
            return schema
    
    def process_schema_recursively(self, schema):
        return self.inline_all_refs(schema)
    
    def format_openapi_spec(self):
        for path_item in self.spec.get("paths", {}).values():
            for method, operation in path_item.items():
                if method in ["get", "post", "put", "patch", "delete", "options", "head", "trace"]:
                    if "requestBody" in operation:
                        content = operation["requestBody"].get("content", {})
                        for media_obj in content.values():
                            if "schema" in media_obj:
                                media_obj["schema"] = self.process_schema_recursively(media_obj["schema"])
                    
                    for response in operation.get("responses", {}).values():
                        content = response.get("content", {})
                        for media_obj in content.values():
                            if "schema" in media_obj:
                                media_obj["schema"] = self.process_schema_recursively(media_obj["schema"])

        if "components" in self.spec and "schemas" in self.spec["components"]:
            for schema_name, schema_def in self.spec["components"]["schemas"].items():
                self.spec["components"]["schemas"][schema_name] = self.process_schema_recursively(schema_def)