# Sky Take-out 外卖点餐系统

## 项目概述

Sky Take-out 是一个完整的外卖点餐解决方案，基于现代化的技术栈构建，包含后端API服务、商家Web管理端和用户微信小程序端。系统实现了从用户下单、支付、商家接单到配送完成的完整外卖业务流程。

### 核心特性
- **用户端**: 微信小程序点餐、在线支付、地址管理、订单追踪
- **商家端**: Web管理后台，支持菜品管理、订单处理、数据统计
- **实时通信**: WebSocket实现订单状态实时推送
- **安全支付**: 微信支付V3 API集成，支持退款流程
- **智能配送**: 基于百度地图API的配送范围校验

## 技术栈

### 后端技术栈
- **Java框架**: Spring Boot 2.7.3
- **ORM框架**: MyBatis + PageHelper分页
- **数据库**: MySQL 8.0+
- **缓存**: Redis 6.0+
- **安全认证**: JWT (JSON Web Token)
- **API文档**: Knife4j (Swagger增强版)
- **构建工具**: Maven 3.6+
- **Java版本**: JDK 8+

### 前端技术栈
- **用户端**: 微信小程序 (Uni-app框架)
- **商家端**: Vue.js + HTML5 + CSS3

### 第三方服务集成
- **支付服务**: 微信支付V3 API
- **地图服务**: 百度地图API (地理编码、路线规划)
- **对象存储**: 七牛云OSS (图片文件存储)
- **实时通信**: WebSocket (订单状态实时推送)

## 系统架构

### 项目结构
```
sky-take-out/
├── sky-common/           # 公共模块
│   ├── src/main/java/com/sky/
│   │   ├── properties/   # 配置属性类
│   │   ├── utils/        # 工具类 (微信支付、HttpClient等)
│   │   └── constant/     # 常量定义
│   └── pom.xml
├── sky-pojo/             # 实体类模块
│   ├── src/main/java/com/sky/
│   │   ├── entity/       # 数据库实体类
│   │   ├── dto/          # 数据传输对象
│   │   ├── vo/           # 视图对象
│   │   └── enums/        # 枚举类
│   └── pom.xml
├── sky-server/           # 主服务模块 (Spring Boot应用)
│   ├── src/main/java/com/sky/
│   │   ├── SkyApplication.java          # 应用启动类
│   │   ├── annotation/                  # 自定义注解
│   │   ├── aspect/                      # 切面编程
│   │   ├── config/                      # 配置类
│   │   ├── controller/                  # 控制器层
│   │   │   ├── admin/                   # 管理端接口
│   │   │   ├── user/                    # 用户端接口
│   │   │   └── Notify/                  # 支付回调接口
│   │   ├── handler/                     # 全局异常处理器
│   │   ├── interceptor/                 # 拦截器
│   │   ├── mapper/                      # MyBatis Mapper接口
│   │   ├── service/                     # 服务层接口
│   │   │   └── impl/                    # 服务层实现
│   │   └── webSocket/                   # WebSocket服务
│   ├── src/main/resources/
│   │   ├── mapper/                      # MyBatis XML映射文件
│   │   ├── application.yml              # 主配置文件
│   │   ├── application-dev.yml          # 开发环境配置
│   │   └── template/                    # 模板文件
│   └── pom.xml
├── sql/                  # 数据库脚本
│   ├── create_table.sql  # 建表语句
│   └── insert_data.sql   # 初始数据
├── merchant-end/         # 商家管理前端 (Vue.js Web应用)
├── user-end/             # 用户小程序前端 (微信小程序)
├── pom.xml               # 父项目POM
└── README.md             # 项目说明文档
```

### 数据库设计
系统使用MySQL数据库，主要包含以下核心表：

| 表名 | 描述 | 核心字段 |
|------|------|----------|
| `employee` | 员工信息表 | id, username, password, phone, status |
| `category` | 菜品分类表 | id, type, name, sort, status |
| `dish` | 菜品表 | id, name, category_id, price, image, status |
| `dish_flavor` | 菜品口味表 | id, dish_id, name, value |
| `setmeal` | 套餐表 | id, name, category_id, price, image, status |
| `shopping_cart` | 购物车表 | id, user_id, dish_id, setmeal_id, number |
| `orders` | 订单表 | id, number, status, user_id, amount, pay_status |
| `order_detail` | 订单明细表 | id, order_id, dish_id, setmeal_id, number, amount |
| `address_book` | 地址簿表 | id, user_id, consignee, phone, detail, is_default |
| `user` | 用户表 | id, openid, name, phone, sex, avatar |

**订单状态流转**:
```
待付款(1) → 待接单(2) → 已接单(3) → 派送中(4) → 已完成(5) → 已取消(6)
```

**支付状态**:
```
未支付(0) → 已支付(1) → 退款(2)
```

## 快速开始

### 环境要求
- JDK 8+
- MySQL 8.0+
- Redis 6.0+
- Maven 3.6+
- Node.js 14+ (前端开发)

### 数据库初始化
1. 创建数据库:
```sql
CREATE DATABASE IF NOT EXISTS `sky_take_out` DEFAULT CHARSET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

2. 执行建表脚本:
```bash
mysql -u root -p sky_take_out < sql/create_table.sql
```

3. 可选：导入初始数据:
```bash
mysql -u root -p sky_take_out < sql/insert_data.sql
```

### 后端服务启动
1. 配置数据库连接:
   编辑 `sky-server/src/main/resources/application-dev.yml`:
   ```yaml
   sky:
     datasource:
       username: root
       password: 1234
       database: sky_take_out
     redis:
       host: localhost
       port: 6379
   ```

2. 编译并启动服务:
```bash
# 在项目根目录执行
mvn clean package
cd sky-server
java -jar target/sky-server-1.0-SNAPSHOT.jar
```

3. 服务启动后访问:
   - 主服务: http://localhost:8080
   - API文档: http://localhost:8080/doc.html (Knife4j界面)

### 前端启动说明
- **商家管理端**: 位于 `merchant-end/` 目录，为Vue.js构建的Web应用，可直接部署到Web服务器
- **用户小程序端**: 位于 `user-end/` 目录，为微信小程序项目，需使用微信开发者工具导入和发布

## 核心功能模块

### 1. 用户模块
- **用户认证**: 微信小程序登录，JWT令牌认证
- **地址管理**: 收货地址增删改查，默认地址设置
- **购物车**: 菜品加入购物车，数量修改，清空购物车

### 2. 菜品模块
- **分类管理**: 菜品/套餐分类，支持启用/禁用
- **菜品管理**: 菜品CRUD，口味配置，图片上传
- **套餐管理**: 套餐组合，套餐内菜品配置

### 3. 订单模块
- **下单流程**: 购物车提交 → 地址选择 → 订单生成
- **支付流程**: 微信支付V3 API集成，支付回调处理
- **订单状态**: 完整状态流转，实时状态推送
- **订单管理**: 历史订单查询，订单详情，取消订单

### 4. 商家管理模块
- **员工管理**: 员工账号CRUD，权限控制
- **数据统计**: 营业额统计，订单统计，菜品销量排行
- **店铺管理**: 店铺信息配置，营业状态控制

### 5. 实时通知模块
- **来单提醒**: WebSocket实时推送新订单给商家
- **催单通知**: 用户催单实时通知
- **状态同步**: 订单状态变更实时同步

## API接口文档

系统使用Knife4j (Swagger增强版) 自动生成API文档，启动服务后访问:

```
http://localhost:8080/doc.html
```

### 主要API分组
- **员工管理接口** (`/admin/employee/**`)
- **分类管理接口** (`/admin/category/**`)
- **菜品管理接口** (`/admin/dish/**`)
- **套餐管理接口** (`/admin/setmeal/**`)
- **订单管理接口** (`/admin/order/**`)
- **数据统计接口** (`/admin/report/**`, `/admin/workspace/**`)
- **用户端接口** (`/user/**`)
  - 用户接口 (`/user/user/**`)
  - 地址接口 (`/user/addressBook/**`)
  - 购物车接口 (`/user/shoppingCart/**`)
  - 订单接口 (`/user/order/**`)
  - 菜品接口 (`/user/dish/**`, `/user/setmeal/**`)
- **支付回调接口** (`/notify/**`)

## 配置说明

### 关键配置文件

1. **主配置文件** (`sky-server/src/main/resources/application.yml`):
   - 服务端口配置
   - 数据源配置
   - Redis配置
   - MyBatis配置
   - 日志配置

2. **开发环境配置** (`sky-server/src/main/resources/application-dev.yml`):
   - 数据库连接信息
   - Redis连接信息
   - 微信支付配置
   - 七牛云OSS配置
   - 百度地图API配置

3. **微信支付配置**:
```yaml
sky:
  wechat:
    appid: wx599462a9f2e5288c          # 小程序appid
    secret: 9e121cc7857f2bdfc183966d3a47d8fc  # 小程序secret
    mchid: 商户号                      # 需要替换
    mchSerialNo: 商户证书序列号         # 需要替换
    privateKeyFilePath: apiclient_key.pem  # 商户私钥文件路径
    apiV3Key: APIv3密钥                # 需要替换
    weChatPayCertFilePath: wechatpay_cert.pem  # 平台证书路径
    notifyUrl: https://your-domain.com/notify/paySuccess  # 支付回调地址
```

### 环境变量配置
敏感信息建议通过环境变量注入:
```yaml
sky:
  datasource:
    username: ${DB_USERNAME:root}
    password: ${DB_PASSWORD:1234}
  wechat:
    appid: ${WECHAT_APPID}
    secret: ${WECHAT_SECRET}
```

## 部署指南

### 生产环境部署
1. **数据库准备**:
   - 创建生产数据库实例
   - 执行建表脚本
   - 配置数据库主从、备份策略

2. **Redis配置**:
   - 配置Redis集群或哨兵模式
   - 设置合适的内存策略和持久化配置

3. **应用部署**:
   ```bash
   # 编译打包
   mvn clean package -DskipTests

   # 上传JAR包到服务器
   scp sky-server/target/sky-server-1.0-SNAPSHOT.jar user@server:/opt/sky-take-out/

   # 启动应用
   java -jar -Dspring.profiles.active=prod sky-server-1.0-SNAPSHOT.jar
   ```

4. **Nginx反向代理配置**:
   ```nginx
   server {
       listen 80;
       server_name your-domain.com;

       location / {
           proxy_pass http://localhost:8080;
           proxy_set_header Host $host;
           proxy_set_header X-Real-IP $remote_addr;
       }

       # 静态资源
       location /static/ {
           alias /opt/sky-take-out/static/;
           expires 30d;
       }
   }
   ```

### Docker容器化部署 (建议)
```dockerfile
FROM openjdk:8-jre-slim
COPY sky-server/target/sky-server-1.0-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "-Dspring.profiles.active=prod", "app.jar"]
```

## 开发指南

### 代码规范
1. **包结构规范**:
   - `entity`: 数据库实体类
   - `dto`: 数据传输对象 (Controller入参)
   - `vo`: 视图对象 (Controller出参)
   - `mapper`: MyBatis Mapper接口
   - `service`: 业务逻辑层
   - `controller`: 控制器层

2. **命名规范**:
   - 实体类: 名词单数，如 `User`, `Order`
   - DTO类: 后缀 `DTO`，如 `UserLoginDTO`
   - VO类: 后缀 `VO`，如 `UserInfoVO`
   - Mapper接口: 后缀 `Mapper`，如 `UserMapper`
   - Service接口: 后缀 `Service`，如 `UserService`

### 事务管理
系统使用Spring声明式事务管理:
```java
@Service
@Transactional  // 类级别事务
public class OrderServiceImpl implements OrderService {

    @Override
    @Transactional(rollbackFor = Exception.class)  // 方法级别事务
    public OrderSubmitVO submit(OrdersSubmitDTO ordersSubmitDTO) {
        // 业务逻辑
    }
}
```

### 异常处理
统一异常处理位于 `GlobalExceptionHandler`:
- 业务异常: `BaseException` 及其子类
- SQL异常: 重复键约束等
- 其他异常: 统一处理并返回友好错误信息

## 监控与维护

### 日志配置
系统使用SLF4J + Logback进行日志管理:
```yaml
logging:
  level:
    com.sky:
      mapper: info     # SQL日志
      service: info    # 业务日志
      controller: info # 请求日志
  file:
    name: logs/sky-take-out.log  # 日志文件路径
```

### 健康检查
Spring Boot Actuator已集成，可开启健康检查端点:
```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics
```

## 常见问题

### Q1: 微信支付无法使用
**A**: 检查以下配置:
1. 微信支付商户号、证书序列号是否正确
2. 商户私钥文件和平台证书文件路径是否正确
3. APIv3密钥是否正确配置
4. 支付回调地址是否可外网访问

### Q2: Redis连接失败
**A**:
1. 检查Redis服务是否启动
2. 检查application-dev.yml中的Redis配置
3. 确认防火墙是否开放Redis端口(6379)

### Q3: 图片上传失败
**A**:
1. 检查七牛云OSS配置 (access-key, secret-key, bucket)
2. 确认网络连接正常
3. 检查上传文件大小限制

### Q4: 百度地图API调用失败
**A**:
1. 检查百度地图AK (应用密钥) 是否正确
2. 确认AK对应的服务已开通 (地理编码、路线规划)
3. 检查网络连接

### 近期目标
1. **消息队列集成**: 引入RabbitMQ处理异步任务，解除系统耦合
2. **缓存策略优化**: Redis多级缓存，热点数据缓存
3. **API网关**: 引入Spring Cloud Gateway统一API管理

## 许可证

本项目采用 MIT 许可证 - 查看 [LICENSE](LICENSE) 文件了解详情

## 联系方式

如有问题或建议，可通过以下方式联系:
- 项目维护者: Lzh-hub-theo

---

**最后更新**: 2026-04-03

*注意: 本文档基于当前项目版本编写，随着项目迭代可能会有所变化。*