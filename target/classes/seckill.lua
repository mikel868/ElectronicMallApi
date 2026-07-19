-- 1. 获取参数
local voucherId = ARGV[1]
local userId = ARGV[2]
local orderId = ARGV[3]

-- 2. 数据key
-- 2.1 库存key (修复key命名不一致的问题)
local stockKey = 'seckill:voucher:' .. voucherId
-- 2.2 订单key  
local orderKey = 'seckill:order:' .. voucherId

-- 3. 脚本业务逻辑
-- 3.1 判断库存是否充足
local stock = redis.call('get', stockKey)

-- 当库存key不存在时，说明库存未预热到Redis，视为库存不足，拒绝购买
if not stock or stock == false then
    redis.log(redis.LOG_WARNING, "Stock key not found in Redis: " .. stockKey .. ", rejecting as insufficient stock")
    return 1  -- 库存不足（key不存在说明未预热，拒绝购买防止超卖）
end

local stockNum = tonumber(stock)
if not stockNum or stockNum <= 0 then
    redis.log(redis.LOG_WARNING, "Insufficient stock: " .. stockKey .. " = " .. tostring(stock))
    return 1  -- 库存不足
end

-- 3.2 判断用户是否已经下单
local isOrdered = redis.call('sismember', orderKey, userId)
redis.log(redis.LOG_WARNING, "Order key: " .. orderKey .. ", user ordered: " .. tostring(isOrdered))

if isOrdered == 1 then
    redis.log(redis.LOG_WARNING, "User already ordered, returning 2 (不能重复下单)")
    return 2  -- 重复下单
end

-- 3.3 执行秒杀操作
-- 3.3.1 扣减Redis库存（与MySQL同步，初始值相同，每笔订单各自-1）
redis.call('decrby', stockKey, 1)

-- 3.3.2 记录用户订单
redis.call('sadd', orderKey, userId)
redis.call('xadd', 'stream.orders', '*', 'userId', userId, 'voucherId', voucherId, 'id', orderId)
redis.log(redis.LOG_WARNING, "Seckill successful: voucherId=" .. voucherId .. ", userId=" .. userId .. ", orderId=" .. orderId)
return 0  -- 秒杀成功