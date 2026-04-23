-- KEYS[1] 是 Redis 的 Hash Key (例如 "dish_stock")
-- ARGV 是参数列表，格式为: { dishId1, count1, dishId2, count2, ... }

for i = 1, #ARGV, 2 do
    local dishId = ARGV[i]
    local restoreCount = tonumber(ARGV[i + 1])

    redis.call('HINCRBY', KEYS[1], dishId, restoreCount)
end

return "1"