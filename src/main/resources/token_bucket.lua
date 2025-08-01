-- token_bucket.lua
local key = KEYS[1]
local rate = tonumber(ARGV[1]) -- tokens per second
local capacity = tonumber(ARGV[2]) -- max tokens
local now = tonumber(ARGV[3]) -- current timestamp (milliseconds)
local requested = tonumber(ARGV[4]) -- tokens to consume

local bucket = redis.call("HMGET", key, "tokens", "last_refreshed")
local tokens = tonumber(bucket[1])
local last_refreshed = tonumber(bucket[2])

if tokens == nil then
  tokens = capacity
  last_refreshed = now
end

local delta = math.max(0, now - last_refreshed)
local new_tokens = math.floor(delta * rate / 1000)
tokens = math.min(capacity, tokens + new_tokens)

last_refreshed = now

local allowed = tokens >= requested
if allowed then
  tokens = tokens - requested
end

redis.call("HMSET", key, "tokens", tokens, "last_refreshed", last_refreshed)
redis.call("EXPIRE", key, 60)

return allowed
