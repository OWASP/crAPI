
---------------------------------------------------------
----------------Auto generated code block----------------
---------------------------------------------------------

do
    local searchers = package.searchers or package.loaders
    local origin_seacher = searchers[2]
    searchers[2] = function(path)
        local files =
        {
------------------------
-- Modules part begin --
------------------------

["plugins.traceable.handler"] = function()
--------------------
-- Module: 'plugins.traceable.handler'
--------------------
--[[
Original file copied from the kong zipkin lua plugin v1.4.1 (https://github.com/Kong/kong-plugin-zipkin/tree/v1.4.1)
]]


local new_zipkin_reporter = require "plugins.traceable.reporter".new
local new_span = require "plugins.traceable.span".new
local rand_bytes = require "plugins.traceable.utils".get_rand_bytes
local tracing_headers = require "plugins.traceable.tracing_headers"
local request_tags = require "plugins.traceable.request_tags"
local traceable = require "plugins.traceable.traceable"

local subsystem = ngx.config.subsystem
local fmt = string.format

local ZipkinLogHandler = {
}

local reporter_cache = setmetatable({}, { __mode = "k" })

local ngx_req_start_time = ngx.req.start_time
local ngx_now            = ngx.now

-- These local tables are to minimize changes to the original kong zipkin plugin
local pluginContext = {}
local conf = {}

local function initConfig()
  local config = {}
  config.traceid_byte_count = 16
  config.tags_header = "Zipkin-Tags"
  local propagation_format = os.getenv("TA_PROPAGATION_FORMAT")
  if propagation_format ~= nil and propagation_format:lower()  == "b3" then
    config.header_type = "b3"
  else -- default and any invalid env var will use w3c
    config.header_type = "w3c"
  end
  config.default_service_name = os.getenv("TA_SERVICE_NAME") -- can be nil
  config.ta_address = os.getenv("TA_ADDRESS")
  if config.ta_address == nil then
    config.ta_address = "127.0.0.1" -- default to localhost for sidecar tme
  end
  config.http_endpoint = "http://" .. config.ta_address .. ":9411/api/v2/spans"
  config.traceable_endpoint = "http://" .. config.ta_address .. ":5442" -- always localhost for tme
  local disable_propagation_headers = os.getenv("TA_DISABLE_PROPAGATION_HEADERS")
  if disable_propagation_headers ~= nil and disable_propagation_headers:lower() == "true" then
    config.enable_propagation_headers = false
  else
    config.enable_propagation_headers = true
  end
  return config
end

conf = initConfig()

-- ngx.now in microseconds
local function ngx_now_mu()
  return ngx_now() * 1000000
end


-- ngx.req.start_time in microseconds
local function ngx_req_start_time_mu()
  return ngx_req_start_time() * 1000000
end


local function get_reporter(conf)
  if reporter_cache[conf] == nil then
    reporter_cache[conf] = new_zipkin_reporter(conf.http_endpoint,
                                               conf.default_service_name)
  end
  return reporter_cache[conf]
end


-- adds the proxy span to the zipkin context, unless it already exists
local function get_or_add_proxy_span(zipkin, timestamp)
  if not zipkin.proxy_span then
    local request_span = zipkin.request_span
    zipkin.proxy_span = request_span:new_child(
      "CLIENT",
      request_span.name .. " (proxy)",
      timestamp
    )
  end
  return zipkin.proxy_span
end


local function timer_log(premature, reporter)
  if premature then
    return
  end

  local ok, err = reporter:flush()
  if not ok then
    ngx.log(ngx.ERR, "reporter flush ", err)
    return
  end
end



local initialize_request


local function get_context(conf, ctx)
  local zipkin = ctx.zipkin
  if not zipkin then
    initialize_request(conf, ctx)
    zipkin = ctx.zipkin
  end
  return zipkin
end


if subsystem == "http" then
  initialize_request = function(conf, ctx)
    local req_headers = ngx.req.get_headers()

    local header_type, trace_id, span_id, parent_id, should_sample, baggage =
      tracing_headers.parse(req_headers)
    local method = ngx.req.get_method()

    if should_sample == nil then
      should_sample = true
    end

    if trace_id == nil then
      trace_id = rand_bytes(conf.traceid_byte_count)
    end

    local request_span = new_span(
      "SERVER",
      method,
      ngx_req_start_time_mu(),
      should_sample,
      trace_id,
      span_id,
      parent_id,
      baggage)

    request_span:set_tag("http.method", method)
    request_span:set_tag("http.path", ngx.var.request_uri)

    local req_tags, err = request_tags.parse(req_headers[conf.tags_header])
    if err then
      -- log a warning in case there were erroneous request tags. Don't throw the tracing away & rescue with valid tags, if any
      ngx.log(ngx.WARN, "failed to parse request", err)
    end
    if req_tags then
      for tag_name, tag_value in pairs(req_tags) do
        request_span:set_tag(tag_name, tag_value)
      end
    end

    ctx.zipkin = {
      request_span = request_span,
      header_type = header_type,
      proxy_span = nil,
      header_filter_finished = false,
    }
  end


  function ZipkinLogHandler:rewrite() -- luacheck: ignore 212
    local zipkin = get_context(conf, pluginContext)
    local block = traceable.block(conf.traceable_endpoint, zipkin.request_span, conf.header_type)
    if block then
      ngx.exit(403)
    end

    local rewrite_start_mu = ngx_now_mu()
    get_or_add_proxy_span(zipkin, rewrite_start_mu)

    if (conf.enable_propagation_headers == true) then
      if zipkin.header ~= nil and zipkin.header_type ~= conf.header_type then
        ngx.log(ngx.DEBUG, "Mismatched header types. conf: " .. conf.header_type .. ". found: " .. zipkin.header_type)
      end
      -- Yes, we are passing the same value 3 times into the following call to
      --   ensure proper header type (either b3 or w3c).
      tracing_headers.set(conf.header_type, conf.header_type, zipkin.proxy_span, conf.header_type)
    end
  end

  function ZipkinLogHandler:body_filter()
    local zipkin = get_context(conf, pluginContext)
    if (not zipkin.response_body) then
      zipkin.response_body = ngx.arg[1]
    else
      zipkin.response_body = zipkin.response_body .. ngx.arg[1]
    end
  end

elseif subsystem == "stream" then

  initialize_request = function(conf, ctx)
    local request_span = new_span(
      "SERVER",
      "stream",
      ngx_req_start_time_mu(),
      true,
      rand_bytes(conf.traceid_byte_count)
    )

    ctx.zipkin = {
      request_span = request_span,
      proxy_span = nil,
    }
  end


  function ZipkinLogHandler:preread() -- luacheck: ignore 212
    local zipkin = get_context(conf, pluginContext)
    local ngx_ctx = ngx.ctx
    local preread_start_mu = ngx_now_mu()

    local proxy_span = get_or_add_proxy_span(zipkin, preread_start_mu)
  end
end


function ZipkinLogHandler:log() -- luacheck: ignore 212
  local now_mu = ngx_now_mu()
  local zipkin = get_context(conf, pluginContext)
  local ngx_ctx = ngx.ctx
  local request_span = zipkin.request_span
  local proxy_span = get_or_add_proxy_span(zipkin, now_mu)
  local reporter = get_reporter(conf)

  local balancer_data = ngx_ctx.balancer_data
  if balancer_data then
    local balancer_tries = balancer_data.tries
    for i = 1, balancer_data.try_count do
      local try = balancer_tries[i]
      local name = fmt("%s (balancer try %d)", request_span.name, i)
      local span = request_span:new_child("CLIENT", name, try.balancer_start * 1000)
      span.ip = try.ip
      span.port = try.port

      if i < balancer_data.try_count then
        span:set_tag("error", true)
        span:set_tag("http.status_code", try.code)
      end


      if try.balancer_latency ~= nil then
        span:finish((try.balancer_start + try.balancer_latency) * 1000)
      else
        span:finish(now_mu)
      end
      reporter:report(span)
    end
    proxy_span:set_tag("peer.hostname", balancer_data.hostname) -- could be nil
    proxy_span.ip   = balancer_data.ip
    proxy_span.port = balancer_data.port
  end

  if subsystem == "http" then
    request_span:set_tag("http.status_code", ngx.status)
  end
  proxy_span:finish(now_mu)
  reporter:report(proxy_span)

  request_span:finish(now_mu)
  reporter:report(request_span)

  local ok, err = ngx.timer.at(0, timer_log, reporter)
  if not ok then
    ngx.log(ngx.ERR, "failed to create timer: ", err)
  end
  traceable.postResponse(conf.traceable_endpoint, zipkin.request_span, zipkin.response_body, conf.header_type)

  pluginContext.zipkin = nil -- reset request context
end

return ZipkinLogHandler
end,

["plugins.traceable.reporter"] = function()
--------------------
-- Module: 'plugins.traceable.reporter'
--------------------
--[[
Original file copied from the kong zipkin lua plugin v1.4.1 (https://github.com/Kong/kong-plugin-zipkin/tree/v1.4.1)
]]


local resty_http = require "resty.http"
local to_hex = require "resty.string".to_hex
local cjson = require "cjson".new()
cjson.encode_number_precision(16)

local zipkin_reporter_methods = {}
local zipkin_reporter_mt = {
  __index = zipkin_reporter_methods,
}

local localEndpoint = {
  serviceName = "crapi-web"
}

-- Utility function to set either ipv4 or ipv6 tags
-- nginx apis don't have a flag to indicate whether an address is v4 or v6
local function ip_kind(addr)
  -- use the presence of ":" to signal v6 (v4 has no colons)
  if addr:find(":", 1, true) then
    return "ipv6"
  else
    return "ipv4"
  end
end


local function new(http_endpoint, default_service_name)
  return setmetatable({
    default_service_name = default_service_name,
    http_endpoint = http_endpoint,
    pending_spans = {},
    pending_spans_n = 0,
  }, zipkin_reporter_mt)
end


function zipkin_reporter_methods:report(span)
  if not span.should_sample then
    return
  end

  local zipkin_tags = {}
  for k, v in span:each_tag() do
    -- Zipkin tag values should be strings
    -- see https://zipkin.io/zipkin-api/#/default/post_spans
    -- and https://github.com/Kong/kong-plugin-zipkin/pull/13#issuecomment-402389342
    -- Zipkin tags should be non-empty
    -- see https://github.com/openzipkin/zipkin/pull/2834#discussion_r332125458
    if v ~= "" then
      zipkin_tags[k] = tostring(v)
    end
  end

  local remoteEndpoint do
    local serviceName = span.service_name or self.default_service_name -- can be nil
    if span.port or serviceName then
      remoteEndpoint = {
        serviceName = serviceName,
        port = span.port,
      }
      if span.ip then
        remoteEndpoint[ip_kind(span.ip)] = span.ip
      end
    else
      remoteEndpoint = cjson.null
    end
  end


  if not next(zipkin_tags) then
    zipkin_tags = nil
  end

  local zipkin_span = {
    traceId = to_hex(span.trace_id),
    name = span.name,
    parentId = span.parent_id and to_hex(span.parent_id) or nil,
    id = to_hex(span.span_id),
    kind = span.kind,
    timestamp = span.timestamp,
    duration = span.duration,
    -- shared = nil, -- We don't use shared spans (server reuses client generated spanId)
    localEndpoint = localEndpoint,
    remoteEndpoint = remoteEndpoint,
    tags = zipkin_tags,
    annotations = span.annotations,
  }

  local i = self.pending_spans_n + 1
  self.pending_spans[i] = zipkin_span
  self.pending_spans_n = i
end


function zipkin_reporter_methods:flush()
  if self.pending_spans_n == 0 then
    return true
  end

  local pending_spans = cjson.encode(self.pending_spans)
  self.pending_spans = {}
  self.pending_spans_n = 0

  if self.http_endpoint == nil or self.http_endpoint == ngx.null then
    return true
  end

  local httpc = resty_http.new()
  local res, err = httpc:request_uri(self.http_endpoint, {
    method = "POST",
    headers = {
      ["content-type"] = "application/json",
    },
    body = pending_spans,
  })
  -- TODO: on failure, retry?
  if not res then
    return nil, "failed to request: " .. err
  elseif res.status < 200 or res.status >= 300 then
    return nil, "failed: " .. res.status .. " " .. res.reason
  end
  return true
end


return {
  new = new,
}

end,

["plugins.traceable.request_tags"] = function()
--------------------
-- Module: 'plugins.traceable.request_tags'
--------------------
--[[
Original file copied from the kong zipkin lua plugin v1.4.1 (https://github.com/Kong/kong-plugin-zipkin/tree/v1.4.1)
]]


-- Module for parsing Zipkin span tags introduced by requests with a special header
-- by default the header is called Zipkin-Tags
--
-- For example, the following http request header:
--
--   Zipkin-Tags: foo=bar; baz=qux
--
-- Will add two tags to the request span in Zipkin


local split = require "plugins.traceable.utils".split

local match = string.match

local request_tags = {}


-- note that and errors is an output value; we do this instead of
-- a return in order to be more efficient (allocate less tables)
local function parse_tags(tags_string, dest, errors)
  local items = split(tags_string, ";")
  local item

  for i = 1, #items do
    item = items[i]
    if item ~= "" then
      local name, value = match(item, "^%s*(%S+)%s*=%s*(.*%S)%s*$")
      if name then
        dest[name] = value

      else
        errors[#errors + 1] = item
      end
    end
  end
end


-- parses req_headers into extra zipkin tags
-- returns tags, err
-- note that both tags and err can be non-nil when the request could parse some tags but rejects others
-- tag can be nil when tags_header is nil. That is not an error (err will be empty)
function request_tags.parse(tags_header)
  if not tags_header then
    return nil, nil
  end

  local t = type(tags_header)
  local tags, errors = {}, {}

  -- "normal" requests are strings
  if t == "string" then
    parse_tags(tags_header, tags, errors)

  -- requests where the tags_header_name header is used more than once get an array
  --
  -- example - a request with the headers:
  --   zipkin-tags: foo=bar
  --   zipkin-tags: baz=qux
  --
  -- will get such array. We have to transform that into { foo=bar, baz=qux }
  elseif t == "table" then
    for i = 1, #tags_header do
      parse_tags(tags_header[i], tags, errors)
    end

  else
    return nil,
           string.format("unexpected tags_header type: %s (%s)",
                         tostring(tags_header), t)
  end

  if next(errors) then
    errors = "Could not parse the following Zipkin tags: " .. table.concat(errors, ", ")
  else
    errors = nil
  end

  return tags, errors
end

return request_tags

end,

["plugins.traceable.span"] = function()
--------------------
-- Module: 'plugins.traceable.span'
--------------------
--[[
Original file copied from the kong zipkin lua plugin v1.4.1 (https://github.com/Kong/kong-plugin-zipkin/tree/v1.4.1)
]]


--[[
The internal data structure is modeled off the ZipKin Span JSON Structure
This makes it cheaper to convert to JSON for submission to the ZipKin HTTP api;
which Jaegar also implements.
You can find it documented in this OpenAPI spec:
https://github.com/openzipkin/zipkin-api/blob/7e33e977/zipkin2-api.yaml#L280
]]

local utils = require "plugins.traceable.utils"
local rand_bytes = utils.get_rand_bytes

local floor = math.floor

local span_methods = {}
local span_mt = {
  __index = span_methods,
}


local baggage_mt = {
  __newindex = function()
    error("attempt to set immutable baggage")
  end,
}


local function generate_span_id()
  return rand_bytes(8)
end


local function new(kind, name, start_timestamp_mu,
                   should_sample, trace_id,
                   span_id, parent_id, baggage)
  assert(kind == "SERVER" or kind == "CLIENT", "invalid span kind")
  assert(type(name) == "string" and name ~= "", "invalid span name")
  assert(type(start_timestamp_mu) == "number" and start_timestamp_mu >= 0,
         "invalid span start_timestamp")
  assert(type(trace_id) == "string", "invalid trace id")

  if span_id == nil then
    span_id = generate_span_id()
  else
    assert(type(span_id) == "string", "invalid span id")
  end

  if parent_id ~= nil then
    assert(type(parent_id) == "string", "invalid parent id")
  end

  if baggage then
    setmetatable(baggage, baggage_mt)
  end

  return setmetatable({
    kind = kind,
    trace_id = trace_id,
    span_id = span_id,
    parent_id = parent_id,
    name = name,
    timestamp = floor(start_timestamp_mu),
    should_sample = should_sample,
    baggage = baggage,
    n_logs = 0,
  }, span_mt)
end


function span_methods:new_child(kind, name, start_timestamp_mu)
  return new(
    kind,
    name,
    start_timestamp_mu,
    self.should_sample,
    self.trace_id,
    generate_span_id(),
    self.span_id,
    self.baggage
  )
end


function span_methods:finish(finish_timestamp_mu)
  assert(self.duration == nil, "span already finished")
  assert(type(finish_timestamp_mu) == "number" and finish_timestamp_mu >= 0,
         "invalid span finish timestamp")
  local duration = finish_timestamp_mu - self.timestamp
  assert(duration >= 0, "invalid span duration")
  self.duration = floor(duration)
  return true
end


function span_methods:set_tag(key, value)
  assert(type(key) == "string", "invalid tag key")
  if value ~= nil then -- Validate value
    local vt = type(value)
    assert(vt == "string" or vt == "number" or vt == "boolean",
      "invalid tag value (expected string, number, boolean or nil)")
  end
  local tags = self.tags
  if tags then
    tags[key] = value
  elseif value ~= nil then
    tags = {
      [key] = value
    }
    self.tags = tags
  end
  return true
end


function span_methods:each_tag()
  local tags = self.tags
  if tags == nil then return function() end end
  return next, tags
end


function span_methods:annotate(value, timestamp_mu)
  assert(type(value) == "string", "invalid annotation value")
  assert(type(timestamp_mu) == "number" and timestamp_mu >= 0, "invalid annotation timestamp")

  local annotation = {
    value = value,
    timestamp = floor(timestamp_mu),
  }

  local annotations = self.annotations
  if annotations then
    annotations[#annotations + 1] = annotation
  else
    self.annotations = { annotation }
  end
  return true
end


function span_methods:each_baggage_item()
  local baggage = self.baggage
  if baggage == nil then return function() end end
  return next, baggage
end


return {
  new = new,
}

end,

["plugins.traceable.traceable"] = function()
--------------------
-- Module: 'plugins.traceable.traceable'
--------------------
local resty_http = require 'resty.http'
local to_hex = require "resty.string".to_hex
local cjson = require "cjson"
local version = require "plugins.traceable.version".version
local ngx = ngx

local _M = {}

-- used for debugging
local function dump(o)
  if type(o) == 'table' then
    local s = '{ '
    for k,v in pairs(o) do
      if type(k) ~= 'number' then k = '"'..k..'"' end
      s = s .. '['..k..'] = ' .. dump(v) .. ','
    end
    return s .. '} '
  else
    return tostring(o)
  end
end

local function setPropagationHeaders(headers, requestSpan, propagation_format)
  if propagation_format == "b3" then
    headers["x-b3-spanid"] = to_hex(requestSpan.span_id)
    headers["x-b3-traceid"] = to_hex(requestSpan.trace_id)
  elseif propagation_format == "w3c" then
    headers["traceparent"] = string.format("00-%s-%s-01", to_hex(requestSpan.trace_id), to_hex(requestSpan.span_id))
  end
end

function _M.block(endpoint, requestSpan, propagation_format)
  if endpoint == nil then
    return false -- let request succeed
  end

  local client = resty_http.new()
  local headers = {
    ["content-type"] = "application/json",
    ["traceableai.module.name"] = "nginx-ingress",
    ["traceableai.module.version"] = version,
    ["traceableai.skip_n_parents"] = "0",
    ["traceableai.merge_n_root"] = "1",
  }
  local body = {}
  local reqHeaders = processHeaders(ngx.req.get_headers(), headers)
  local body = makeReqCapRequestBody(reqHeaders)
  setPropagationHeaders(headers, requestSpan, propagation_format)

  local res, err = client:request_uri(endpoint .. "/ext_cap/v1/req_cap", {
  method = "POST",
  headers = headers,
  body = body,
  })

  if not res then
    ngx.log(ngx.ERR, "ext_cap: req_cap failed: ", err)
    return false -- let request succeed
  end
  if res.status ~= 200 then
    ngx.log(ngx.ERR, "ext_cap: req_cap failed: ",res.status)
    return false -- let request succeed
  end

  local authorization = cjson.decode(res.body)
  if authorization.allowRequest then
    return false
  end

  return true
end

function _M.postResponse(endpoint, requestSpan, responseBody, propagation_format)
  if endpoint == nil then 
    return
  end
  local headers = {
    ["content-type"] = "application/json",
    ["traceableai.module.name"] = "nginx-ingress",
    ["traceableai.module.version"] = version,
    ["traceableai.skip_n_parents"] = "0",
    ["traceableai.merge_n_root"] = "1",
  }
  copyTraceContextHeaders(ngx.req.get_headers(), headers)
  local resHeaders = processHeaders(ngx.resp.get_headers(), headers)
  local body = makeResCapRequestBody(resHeaders, responseBody)
  setPropagationHeaders(headers, requestSpan, propagation_format)

  local request = {
    method = "POST",
    headers = headers,
    body = body,
  }
  -- We use ngx.timer.at to work around the limitation of sockets not being available in the log
  -- phase.
  --
  -- See https://github.com/openresty/lua-nginx-module#cosockets-not-available-everywhere
  ngx.timer.at(0, sendResCapRequest, endpoint, request)
end

function processHeaders(headers, rpcHeaders)
  local result = {}
  for k, v in pairs(headers) do
    if type(v) == "table" then
      result[k] = flattenHeaderArray(v)
    else
      if isTracePropagationHeader(k) then
        rpcHeaders[k] = v
      else
        result[k] = v
      end
    end
  end
  return result
end

function makeReqCapRequestBody(reqHeaders)
  local body = ""
  if ngx.req.http_version() < 2 then
    ngx.req.read_body()
    body = ngx.req.get_body_data()
  end
  return cjson.encode({
    request = {
      timestamp = toProtoTimestamp(ngx.req.start_time()),
      method = ngx.req.get_method(),
      headers = reqHeaders,
      scheme = ngx.var.scheme,
      path = ngx.var.request_uri,
      host = ngx.var.host,
      body = ngx.encode_base64(body),
      source_address = ngx.var.remote_addr,
      source_port = ngx.var.server_port,
    }
  })
end

function copyTraceContextHeaders(contextHeaders, headers)
  for k, v in pairs(contextHeaders) do
    if isTracePropagationHeader(k) then
      headers[k] = v
    end
  end
end

function makeResCapRequestBody(resHeaders, resBody)
  local url = ""
  local scheme = ngx.var.scheme
  local host = ngx.var.host
  local port = ngx.var.server_port
  local path = ngx.var.request_uri
  if scheme ~= nil and host ~= nil and path ~= nil then
    url = scheme .. "://" .. host .. ":" .. port .. path
  end
  return cjson.encode({
    response = {
      headers = resHeaders,
      body = ngx.encode_base64(resBody),
      request_url = url
    }
  })
end

function sendResCapRequest(premature, endpoint, request)
  if premature then
    return
  end

  local client = resty_http.new()
  local res, err = client:request_uri(endpoint .. "/ext_cap/v1/res_cap", request)
end

function toProtoTimestamp(timestampInSeconds)
  -- Protobuf 64-bit numbers are serialized as strings.
  -- See https://developers.google.com/protocol-buffers/docs/proto3#json
  return string.format("%.0f", math.floor(1.0e6 * timestampInSeconds))
end

function startsWith(str, start)
   return str:sub(1, #start) == start
end

function flattenHeaderArray(values)
  local result = ""
  for index, value in ipairs(values) do
    if index > 1 then
      result = result .. "," .. value
    else
      result = value
    end
  end
  return result
end

function isTracePropagationHeader(key)
  return startsWith(key, "x-b3-") or
         key == "traceparent" or
         key == "tracestate"
end

return _M
end,

["plugins.traceable.tracing_headers"] = function()
--------------------
-- Module: 'plugins.traceable.tracing_headers'
--------------------
--[[
Original file copied from the kong zipkin lua plugin v1.4.1 (https://github.com/Kong/kong-plugin-zipkin/tree/v1.4.1)
]]


local to_hex = require "resty.string".to_hex
local table_merge = require "plugins.traceable.utils".table_merge
local unescape_uri = ngx.unescape_uri
local char = string.char
local match = string.match
local gsub = string.gsub
local fmt = string.format


local baggage_mt = {
  __newindex = function()
    error("attempt to set immutable baggage", 2)
  end,
}

local B3_SINGLE_PATTERN =
  "^(%x+)%-(%x%x%x%x%x%x%x%x%x%x%x%x%x%x%x%x)%-?([01d]?)%-?(%x*)$"
local W3C_TRACECONTEXT_PATTERN = "^(%x+)%-(%x+)%-(%x+)%-(%x+)$"
local JAEGER_TRACECONTEXT_PATTERN = "^(%x+):(%x+):(%x+):(%x+)$"
local JAEGER_BAGGAGE_PATTERN = "^uberctx%-(.*)$"
local OT_BAGGAGE_PATTERN = "^ot-baggage%-(.*)$"

local function hex_to_char(c)
  return char(tonumber(c, 16))
end


local function from_hex(str)
  if str ~= nil then -- allow nil to pass through
    str = gsub(str, "%x%x", hex_to_char)
  end
  return str
end


local function parse_baggage_headers(headers, header_pattern)
  -- account for both ot and uber baggage headers
  local baggage
  for k, v in pairs(headers) do
    local baggage_key = match(k, header_pattern)
    if baggage_key then
      if baggage then
        baggage[baggage_key] = unescape_uri(v)
      else
        baggage = { [baggage_key] = unescape_uri(v) }
      end
    end
  end

  if baggage then
    return setmetatable(baggage, baggage_mt)
  end
end


local function parse_zipkin_b3_headers(headers, b3_single_header)

  -- X-B3-Sampled: if an upstream decided to sample this request, we do too.
  local should_sample = headers["x-b3-sampled"]
  if should_sample == "1" or should_sample == "true" then
    should_sample = true
  elseif should_sample == "0" or should_sample == "false" then
    should_sample = false
  elseif should_sample ~= nil then
    ngx.log(ngx.WARN, "x-b3-sampled header invalid; ignoring.")
    should_sample = nil
  end

  -- X-B3-Flags: if it equals '1' then it overrides sampling policy
  -- We still want to warn on invalid sample header, so do this after the above
  local debug_header = headers["x-b3-flags"]
  if debug_header == "1" then
    should_sample = true
  elseif debug_header ~= nil then
    ngx.log(ngx.WARN, "x-b3-flags header invalid; ignoring.")
  end

  local trace_id, span_id, sampled, parent_id
  local had_invalid_id = false

  -- B3 single header
  -- * For speed, the "-" separators between sampled and parent_id are optional on this implementation
  --   This is not guaranteed to happen in future versions and won't be considered a breaking change
  -- * The "sampled" section activates sampling with both "1" and "d". This is to match the
  --   behavior of the X-B3-Flags header
  if b3_single_header and type(b3_single_header) == "string" then
    if b3_single_header == "1" or b3_single_header == "d" then
      should_sample = true

    elseif b3_single_header == "0" then
      should_sample = should_sample or false

    else
      trace_id, span_id, sampled, parent_id =
        match(b3_single_header, B3_SINGLE_PATTERN)

      local trace_id_len = trace_id and #trace_id or 0
      if trace_id
      and (trace_id_len == 16 or trace_id_len == 32)
      and (parent_id == "" or #parent_id == 16)
      then

        if should_sample or sampled == "1" or sampled == "d" then
          should_sample = true
        elseif sampled == "0" then
          should_sample = false
        end

        if parent_id == "" then
          parent_id = nil
        end

      else
        ngx.log(ngx.WARN, "b3 single header invalid; ignoring.")
        had_invalid_id = true
      end
    end
  end

  local trace_id_header = headers["x-b3-traceid"]
  if trace_id_header and ((#trace_id_header ~= 16 and #trace_id_header ~= 32)
                           or trace_id_header:match("%X")) then
    ngx.log(ngx.WARN, "x-b3-traceid header invalid; ignoring.")
    had_invalid_id = true
  else
    trace_id = trace_id or trace_id_header -- b3 single header overrides x-b3-traceid
  end

  local span_id_header = headers["x-b3-spanid"]
  if span_id_header and (#span_id_header ~= 16 or span_id_header:match("%X")) then
    ngx.log(ngx.WARN, "x-b3-spanid header invalid; ignoring.")
    had_invalid_id = true
  else
    span_id = span_id or span_id_header -- b3 single header overrides x-b3-spanid
  end

  local parent_id_header = headers["x-b3-parentspanid"]
  if parent_id_header and (#parent_id_header ~= 16 or parent_id_header:match("%X")) then
    ngx.log(ngx.WARN, "x-b3-parentspanid header invalid; ignoring.")
    had_invalid_id = true
  else
    parent_id = parent_id or parent_id_header -- b3 single header overrides x-b3-parentid
  end

  if trace_id == nil or had_invalid_id then
    return nil, nil, nil, should_sample
  end

  trace_id = from_hex(trace_id)
  span_id = from_hex(span_id)
  parent_id = from_hex(parent_id)

  return trace_id, span_id, parent_id, should_sample
end


local function parse_w3c_trace_context_headers(w3c_header)
  local should_sample = false

  if type(w3c_header) ~= "string" then
    return nil, nil, should_sample
  end

  local version, trace_id, parent_id, trace_flags = match(w3c_header, W3C_TRACECONTEXT_PATTERN)

  -- values are not parseable hexadecimal and therefore invalid.
  if version == nil or trace_id == nil or parent_id == nil or trace_flags == nil then
    ngx.log(ngx.WARN, "invalid W3C traceparent header; ignoring.")
    return nil, nil, nil
  end

  -- Only support version 00 of the W3C Trace Context spec.
  if version ~= "00" then
    ngx.log(ngx.WARN, "invalid W3C Trace Context version; ignoring.")
    return nil, nil, nil
  end

  -- valid trace_id is required.
  if #trace_id ~= 32 or tonumber(trace_id, 16) == 0 then
    ngx.log(ngx.WARN, "invalid W3C trace context trace ID; ignoring.")
    return nil, nil, nil
  end

  -- valid parent_id is required.
  if #parent_id ~= 16 or tonumber(parent_id, 16) == 0 then
    ngx.log(ngx.WARN, "invalid W3C trace context parent ID; ignoring.")
    return nil, nil, nil
  end

  -- valid flags are required
  if #trace_flags ~= 2 then
    ngx.log(ngx.WARN, "invalid W3C trace context flags; ignoring.")
    return nil, nil, nil
  end

  -- W3C sampled flag: https://www.w3.org/TR/trace-context/#sampled-flag
  should_sample = tonumber(trace_flags, 16) % 2 == 1

  trace_id = from_hex(trace_id)
  parent_id = from_hex(parent_id)

  return trace_id, parent_id, should_sample
end

local function parse_ot_headers(headers)
  local should_sample = headers["ot-tracer-sampled"]
  if should_sample == "1" or should_sample == "true" then
    should_sample = true
  elseif should_sample == "0" or should_sample == "false" then
    should_sample = false
  elseif should_sample ~= nil then
    ngx.log(ngx.WARN, "ot-tracer-sampled header invalid; ignoring.")
    should_sample = nil
  end

  local trace_id, span_id
  local had_invalid_id = false

  local trace_id_header = headers["ot-tracer-traceid"]
  if trace_id_header and ((#trace_id_header ~= 16 and #trace_id_header ~= 32) or trace_id_header:match("%X")) then
    ngx.log(ngx.WARN, "ot-tracer-traceid header invalid; ignoring.")
    had_invalid_id = true
  else
    trace_id = trace_id_header
  end

  local span_id_header = headers["ot-tracer-spanid"]
  if span_id_header and (#span_id_header ~= 16 or span_id_header:match("%X")) then
    ngx.log(ngx.WARN, "ot-tracer-spanid header invalid; ignoring.")
    had_invalid_id = true
  else
    span_id = span_id_header
  end

  if trace_id == nil or had_invalid_id then
    return nil, nil, should_sample
  end

  trace_id = from_hex(trace_id)
  span_id = from_hex(span_id)

  return trace_id, span_id, should_sample
end


local function parse_jaeger_trace_context_headers(jaeger_header)
  if type(jaeger_header) ~= "string" then
    return nil, nil, nil, nil
  end

  local trace_id, span_id, parent_id, trace_flags = match(jaeger_header, JAEGER_TRACECONTEXT_PATTERN)

  -- values are not parsable hexidecimal and therefore invalid.
  if trace_id == nil or span_id == nil or parent_id == nil or trace_flags == nil then
    ngx.log(ngx.WARN, "invalid jaeger uber-trace-id header; ignoring.")
    return nil, nil, nil, nil
  end

  -- valid trace_id is required.
  if (#trace_id ~= 16 and #trace_id ~= 32) or tonumber(trace_id, 16) == 0 then
    ngx.log(ngx.WARN, "invalid jaeger trace ID; ignoring.")
    return nil, nil, nil, nil
  end

  -- valid span_id is required.
  if #span_id ~= 16 or tonumber(parent_id, 16) == 0 then
    ngx.log(ngx.WARN, "invalid jaeger span ID; ignoring.")
    return nil, nil, nil, nil
  end

  -- valid parent_id is required.
  if #parent_id ~= 16 then
    ngx.log(ngx.WARN, "invalid jaeger parent ID; ignoring.")
    return nil, nil, nil, nil
  end

  -- valid flags are required
  if #trace_flags ~= 1 and #trace_flags ~= 2 then
    ngx.log(ngx.WARN, "invalid jaeger flags; ignoring.")
    return nil, nil, nil, nil
  end

  -- Jaeger sampled flag: https://www.jaegertracing.io/docs/1.17/client-libraries/#tracespan-identity
  local should_sample = tonumber(trace_flags, 16) % 2 == 1

  trace_id = from_hex(trace_id)
  span_id = from_hex(span_id)
  parent_id = from_hex(parent_id)

  return trace_id, span_id, parent_id, should_sample
end


-- This plugin understands several tracing header types:
-- * Zipkin B3 headers (X-B3-TraceId, X-B3-SpanId, X-B3-ParentId, X-B3-Sampled, X-B3-Flags)
-- * Zipkin B3 "single header" (a single header called "B3", composed of several fields)
--   * spec: https://github.com/openzipkin/b3-propagation/blob/master/RATIONALE.md#b3-single-header-format
-- * W3C "traceparent" header - also a composed field
--   * spec: https://www.w3.org/TR/trace-context/
-- * Jaeger's uber-trace-id & baggage headers
--   * spec: https://www.jaegertracing.io/docs/1.21/client-libraries/#tracespan-identity
-- * OpenTelemetry ot-tracer-* tracing headers.
--   * OpenTelemetry spec: https://github.com/open-telemetry/opentelemetry-specification
--   * Base implementation followed: https://github.com/open-telemetry/opentelemetry-java/blob/96e8523544f04c305da5382854eee06218599075/extensions/trace_propagators/src/main/java/io/opentelemetry/extensions/trace/propagation/OtTracerPropagator.java
--
-- The plugin expects request to be using *one* of these types. If several of them are
-- encountered on one request, only one kind will be transmitted further. The order is
--
--      B3-single > B3 > W3C > Jaeger > OT
--
-- Exceptions:
--
-- * When both B3 and B3-single fields are present, the B3 fields will be "ammalgamated"
--   into the resulting B3-single field. If they present contradictory information (i.e.
--   different TraceIds) then B3-single will "win".
--
-- * The erroneous formatting on *any* header (even those overriden by B3 single) results
--   in rejection (ignoring) of all headers. This rejection is logged.
local function find_header_type(headers)
  local b3_single_header = headers["b3"]
  if not b3_single_header then
    local tracestate_header = headers["tracestate"]
    if tracestate_header then
      b3_single_header = match(tracestate_header, "^b3=(.+)$")
    end
  end

  if b3_single_header then
    return "b3-single", b3_single_header
  end

  if headers["x-b3-sampled"]
  or headers["x-b3-flags"]
  or headers["x-b3-traceid"]
  or headers["x-b3-spanid"]
  or headers["x-b3-parentspanid"]
  then
    return "b3"
  end

  local w3c_header = headers["traceparent"]
  if w3c_header then
    return "w3c", w3c_header
  end

  local jaeger_header = headers["uber-trace-id"]
  if jaeger_header then
    return "jaeger", jaeger_header
  end

  local ot_header = headers["ot-tracer-traceid"]
  if ot_header then
    return "ot", ot_header
  end
end


local function parse(headers)
  -- Check for B3 headers first
  local header_type, composed_header = find_header_type(headers)
  local trace_id, span_id, parent_id, should_sample

  if header_type == "b3" or header_type == "b3-single" then
    trace_id, span_id, parent_id, should_sample = parse_zipkin_b3_headers(headers, composed_header)
  elseif header_type == "w3c" then
    trace_id, parent_id, should_sample = parse_w3c_trace_context_headers(composed_header)
  elseif header_type == "jaeger" then
    trace_id, span_id, parent_id, should_sample = parse_jaeger_trace_context_headers(composed_header)
  elseif header_type == "ot" then
    trace_id, parent_id, should_sample = parse_ot_headers(headers)
  end

  if not trace_id then
    return header_type, trace_id, span_id, parent_id, should_sample
  end

  -- Parse baggage headers
  local baggage
  local ot_baggage = parse_baggage_headers(headers, OT_BAGGAGE_PATTERN)
  local jaeger_baggage = parse_baggage_headers(headers, JAEGER_BAGGAGE_PATTERN)
  if ot_baggage and jaeger_baggage then
    baggage = table_merge(ot_baggage, jaeger_baggage)
  else
    baggage = ot_baggage or jaeger_baggage or nil
  end


  return header_type, trace_id, span_id, parent_id, should_sample, baggage
end


local function set(conf_header_type, found_header_type, proxy_span, conf_default_header_type)
  local set_header = ngx.req.set_header
  if conf_header_type ~= "preserve" and
     found_header_type ~= nil and
     conf_header_type ~= found_header_type
  then
    ngx.log(ngx.WARN, "Mismatched header types. conf: " .. conf_header_type .. ". found: " .. found_header_type)
  end

  found_header_type = found_header_type or conf_default_header_type or "b3"

  if conf_header_type == "b3" or found_header_type == "b3"
  then
    set_header("x-b3-traceid", to_hex(proxy_span.trace_id))
    set_header("x-b3-spanid", to_hex(proxy_span.span_id))
    if proxy_span.parent_id then
      set_header("x-b3-parentspanid", to_hex(proxy_span.parent_id))
    end
    local req_headers = ngx.req.get_headers()
    local Flags = req_headers["x-b3-flags"] -- Get from request headers
    if Flags then
      set_header("x-b3-flags", Flags)
    else
      set_header("x-b3-sampled", proxy_span.should_sample and "1" or "0")
    end
  end

  if conf_header_type == "b3-single" or found_header_type == "b3-single" then
    set_header("b3", fmt("%s-%s-%s-%s",
        to_hex(proxy_span.trace_id),
        to_hex(proxy_span.span_id),
        proxy_span.should_sample and "1" or "0",
      to_hex(proxy_span.parent_id)))
  end

  if conf_header_type == "w3c" or found_header_type == "w3c" then
    set_header("traceparent", fmt("00-%s-%s-%s",
        to_hex(proxy_span.trace_id),
        to_hex(proxy_span.span_id),
      proxy_span.should_sample and "01" or "00"))
  end

  if conf_header_type == "jaeger" or found_header_type == "jaeger" then
    set_header("uber-trace-id", fmt("%s:%s:%s:%s",
        to_hex(proxy_span.trace_id),
        to_hex(proxy_span.span_id),
        to_hex(proxy_span.parent_id),
      proxy_span.should_sample and "01" or "00"))
  end

  if conf_header_type == "ot" or found_header_type == "ot" then
    set_header("ot-tracer-traceid", to_hex(proxy_span.trace_id))
    set_header("ot-tracer-spanid", to_hex(proxy_span.span_id))
    set_header("ot-tracer-sampled", proxy_span.should_sample and "1" or "0")

    for key, value in proxy_span:each_baggage_item() do
      set_header("ot-baggage-"..key, ngx.escape_uri(value))
    end
  end

  for key, value in proxy_span:each_baggage_item() do
    -- XXX: https://github.com/opentracing/specification/issues/117
    set_header("uberctx-"..key, ngx.escape_uri(value))
  end
end


return {
  parse = parse,
  set = set,
  from_hex = from_hex,
}

end,

["plugins.traceable.utils"] = function()
--------------------
-- Module: 'plugins.traceable.utils'
--------------------
--[[
These utility functions are used by the kong zipkin lua plugin. Since we cannot
modify the nginx ingress controller image, all the lua files need to be self
contained. The functions are copied whole if possible.
]]

local random = require "resty.random"

local _M = {}

-- This is copied from Penlight. The only changes are commented lines for
-- utils.assert_string() and makelist() 

--- split a string into a list of strings separated by a delimiter.
-- @param s The input string
-- @param re optional A Lua string pattern; defaults to '%s+'
-- @param plain optional If truthy don't use Lua patterns
-- @param n optional maximum number of elements (if there are more, the last will remian un-split)
-- @return a list-like table
-- @raise error if s is not a string
-- @see splitv
local function usplit(s,re,plain,n)
--  utils.assert_string(1,s)
  local find,sub,append = string.find, string.sub, table.insert
  local i1,ls = 1,{}
  if not re then re = '%s+' end
  if re == '' then return {s} end
  while true do
      local i2,i3 = find(s,re,i1,plain)
      if not i2 then
          local last = sub(s,i1)
          if last ~= '' then append(ls,last) end
          if #ls == 1 and ls[1] == '' then
              return {}
          else
              return ls
          end
      end
      append(ls,sub(s,i1,i2-1))
      if n and #ls == n then
          ls[#ls] = sub(s,i1)
          return ls
      end
      i1 = i3+1
  end
end

-- This is copied from Penlight. The only changes are commented lines for
-- utils.assert_string() and makelist() 

--- split a string into a list of strings using a delimiter.
-- @function split
-- @string s the string
-- @string[opt] re a delimiter (defaults to whitespace)
-- @int[opt] n maximum number of results
-- @return List
-- @usage #(stringx.split('one two')) == 2
-- @usage stringx.split('one,two,three', ',') == List{'one','two','three'}
-- @usage stringx.split('one,two,three', ',', 2) == List{'one','two,three'}
function _M.split(s,re,n)
--    assert_string(1,s)
local find = string.find
    local plain = true
    if not re then -- default spaces
        s = lstrip(s)
        plain = false
    end
    local res = usplit(s,re,plain,n)
    if re and re ~= '' and
       find(s,re,-#re,true) and
       (n or math.huge) > #res then
        res[#res+1] = ""
    end
--    return makelist(res)
    return res
end


-- This is copied from kong.tools.utils

--- Merges two table together.
-- A new table is created with a non-recursive copy of the provided tables
-- @param t1 The first table
-- @param t2 The second table
-- @return The (new) merged table
function _M.table_merge(t1, t2)
  if not t1 then
    t1 = {}
  end
  if not t2 then
    t2 = {}
  end

  local res = {}
  for k,v in pairs(t1) do res[k] = v end
  for k,v in pairs(t2) do res[k] = v end
  return res
end


-- Here is one deviation from the kong zipkin lua plugin (ENG-11625). We use
-- resty.random instead of copying the util functions from kong. The util
-- functions pull in many dependent parts and this was quicker. If there are
-- any span id/trace id anomalies, we will need to revisit this.
function _M.get_rand_bytes(num)
  local hxbts = random.bytes(num, 'hex')
  return hxbts
end

return _M
end,

["plugins.traceable.version"] = function()
--------------------
-- Module: 'plugins.traceable.version'
--------------------
local _M = { version = "1.18.1" } return _M

end,

----------------------
-- Modules part end --
----------------------
        }
        if files[path] then
            return files[path]
        else
            return origin_seacher(path)
        end
    end
end
---------------------------------------------------------
----------------Auto generated code block----------------
---------------------------------------------------------
-- The nginx ingress controller lua plugin requires a main.lua

local handler = require "plugins.traceable.handler"
return handler
