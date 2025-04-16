local key = KEYS[1]
local jsonData = ARGV[1]
local expireSeconds = tonumber(ARGV[2])
local useNx = ARGV[3] == "1"
local result

if useNx then
    result = redis.call('JSON.SET', key, '$', jsonData, 'NX')
else
    result = redis.call('JSON.SET', key, '$', jsonData)
end
if result and expireSeconds and expireSeconds > 0 then
    result = redis.call('EXPIRE', key, expireSeconds)
    if result == 1 then

        return 1
    else

        return 0
    end
end
if result then

    return 1
else

    return 0
end