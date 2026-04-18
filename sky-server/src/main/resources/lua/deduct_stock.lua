-- KEYS[1] 是 Redis 的 Hash Key (例如 "dish_stock")
-- ARGV 是参数列表，格式为: { dishId1, count1, dishId2, count2, ... }

-- 1. 检查所有商品库存是否充足
for i = 1, #ARGV, 2 do
    local dishId = ARGV[i]
    local requiredCount = tonumber(ARGV[i + 1])
    local currentStock = redis.call('HGET', KEYS[1], dishId)

    if not currentStock then
        return "-1"
    end

    currentStock = tonumber(currentStock)
    if currentStock < requiredCount then
        return "0"
    end
end

-- 2. 扣减库存
for i = 1, #ARGV, 2 do
    local dishId = ARGV[i]
    local requiredCount = tonumber(ARGV[i + 1])

    redis.call('HINCRBY', KEYS[1], dishId, -requiredCount)
end

return "1"