# 社区互助平台 - 性能优化指南

## 一、数据库优化

### 1.1 索引优化

```sql
-- 为高频查询字段添加索引
CREATE INDEX idx_user_status ON sys_user(status);
CREATE INDEX idx_user_phone ON sys_user(phone);
CREATE INDEX idx_order_user_status ON orders(user_id, status);
CREATE INDEX idx_order_create_time ON orders(create_time);
CREATE INDEX idx_message_user_read ON message(user_id, is_read);

-- 使用覆盖索引避免回表
CREATE INDEX idx_order_cover ON orders(user_id, status, create_time, order_no);
```

### 1.2 查询优化

```java
// 避免 N+1 查询问题
// 错误示例
List<Order> orders = orderMapper.selectList(wrapper);
for (Order order : orders) {
    User user = userMapper.selectById(order.getUserId()); // N次查询
}

// 正确示例
List<Order> orders = orderMapper.selectList(wrapper);
List<Long> userIds = orders.stream().map(Order::getUserId).collect(Collectors.toList());
List<User> users = userMapper.selectBatchIds(userIds); // 1次查询
```

### 1.3 分页优化

```java
// 深分页优化：使用游标分页
public PageResponse<Order> getOrdersWithCursor(Long lastId, int size) {
    LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
    if (lastId != null) {
        wrapper.lt(Order::getId, lastId);
    }
    wrapper.orderByDesc(Order::getId);
    wrapper.last("LIMIT " + size);
    List<Order> orders = orderMapper.selectList(wrapper);
    return PageResponse.of(orders, orders.size(), 1, size);
}
```

## 二、缓存优化

### 2.1 多级缓存架构

```
请求 -> 本地缓存(Caffeine) -> Redis缓存 -> 数据库
       <1ms               <5ms          <50ms
```

### 2.2 缓存策略

| 数据类型 | 缓存策略 | 过期时间 |
|----------|----------|----------|
| 用户信息 | 读写穿透 | 30分钟 |
| 订单详情 | 延迟双删 | 10分钟 |
| 活动列表 | 定时刷新 | 5分钟 |
| 配置数据 | 永久缓存 | 手动更新 |

### 2.3 缓存穿透防护

```java
// 布隆过滤器 + 空值缓存
public User getUserById(Long id) {
    // 1. 检查布隆过滤器
    if (!bloomFilter.mightContain(id)) {
        return null;
    }
    
    // 2. 查询缓存
    User user = cacheService.get("user:" + id, User.class);
    if (user != null) {
        return user;
    }
    
    // 3. 查询数据库
    user = userMapper.selectById(id);
    if (user == null) {
        // 缓存空值，防止穿透
        cacheService.set("user:" + id, new User(), 5, TimeUnit.MINUTES);
        return null;
    }
    
    // 4. 写入缓存
    cacheService.set("user:" + id, user, 30, TimeUnit.MINUTES);
    return user;
}
```

### 2.4 缓存雪崩防护

```java
// 随机过期时间
public void cacheWithRandomExpire(String key, Object value, long baseSeconds) {
    long randomSeconds = baseSeconds + ThreadLocalRandom.current().nextInt(0, 300);
    cacheService.set(key, value, randomSeconds, TimeUnit.SECONDS);
}
```

## 三、接口优化

### 3.1 异步处理

```java
// 非核心流程异步化
@Async("taskExecutor")
public void sendNotification(Long userId, String message) {
    // 发送站内消息
    messageService.sendMessage(userId, message);
    // 发送微信模板消息
    wechatMessageService.sendTemplate(userId, message);
}
```

### 3.2 批量操作

```java
// 批量插入优化
public void batchInsertUsers(List<User> users) {
    int batchSize = 500;
    for (int i = 0; i < users.size(); i += batchSize) {
        int end = Math.min(i + batchSize, users.size());
        List<User> batch = users.subList(i, end);
        userMapper.insertBatchSomeColumn(batch);
    }
}
```

### 3.3 连接池优化

```yaml
spring:
  datasource:
    druid:
      initial-size: 5
      min-idle: 5
      max-active: 20
      max-wait: 60000
      time-between-eviction-runs-millis: 60000
      min-evictable-idle-time-millis: 300000
      validation-query: SELECT 1
      test-while-idle: true
      test-on-borrow: false
      test-on-return: false
```

## 四、JVM优化

### 4.1 启动参数

```bash
java -jar app.jar \
  -Xms2g \
  -Xmx2g \
  -XX:+UseG1GC \
  -XX:MaxGCPauseMillis=200 \
  -XX:+HeapDumpOnOutOfMemoryError \
  -XX:HeapDumpPath=/logs/heapdump.hprof \
  -XX:+PrintGCDetails \
  -Xloggc:/logs/gc.log
```

### 4.2 GC调优

| 场景 | 推荐GC | 参数 |
|------|--------|------|
| 低延迟 | G1GC | -XX:MaxGCPauseMillis=200 |
| 大内存 | ZGC | -XX:+UseZGC |
| 高吞吐 | ParallelGC | -XX:+UseParallelGC |

## 五、网络优化

### 5.1 网关限流

```java
// 令牌桶限流
public class RateLimitFilter implements GlobalFilter {
    private final RateLimiter rateLimiter = RateLimiter.create(100);
    
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        if (!rateLimiter.tryAcquire()) {
            exchange.getResponse().setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
            return exchange.getResponse().setComplete();
        }
        return chain.filter(exchange);
    }
}
```

### 5.2 响应压缩

```yaml
server:
  compression:
    enabled: true
    mime-types: application/json,text/html,text/xml,text/plain
    min-response-size: 1024
```

## 六、监控指标

### 6.1 关键指标

| 指标 | 阈值 | 说明 |
|------|------|------|
| 接口响应时间 | P99 < 500ms | 99%请求响应时间 |
| 数据库连接池 | 使用率 < 80% | 避免连接耗尽 |
| Redis内存使用 | < 70% | 预留内存空间 |
| JVM堆使用率 | < 75% | 避免频繁GC |
| 错误率 | < 0.1% | 业务错误率 |

### 6.2 监控配置

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,metrics,prometheus
  metrics:
    export:
      prometheus:
        enabled: true
```
