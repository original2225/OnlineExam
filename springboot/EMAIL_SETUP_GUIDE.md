# 邮箱配置指南

本项目支持使用 **QQ 邮箱** 或 **Gmail** 作为发件邮箱，发送注册通知邮件。

学生可以使用**任意邮箱**（QQ、Gmail、163、Outlook等）接收邮件。

---

## 📧 作为发件邮箱的配置

### 选项 1: 使用 QQ 邮箱（推荐国内用户）

#### 1. 获取 QQ 邮箱授权码

1. 登录 [QQ 邮箱网页版](https://mail.qq.com)
2. 点击顶部 **设置** → **账户**
3. 找到 **POP3/IMAP/SMTP/Exchange/CardDAV/CalDAV服务**
4. 开启 **POP3/SMTP服务** 或 **IMAP/SMTP服务**
5. 点击 **生成授权码**
6. 通过手机验证后，会获得一个 **16位授权码**（如：`your-qq-authorization-code`）

#### 2. 配置 application.yml

```yaml
spring:
  mail:
    host: smtp.qq.com
    port: 587
    username: your-email@qq.com        # 你的QQ邮箱
    password: your-qq-authorization-code         # 刚才获取的授权码
    properties:
      mail:
        smtp:
          auth: true
          starttls:
            enable: true
          ssl:
            trust: smtp.qq.com
```

---

### 选项 2: 使用 Gmail（推荐国外用户）

#### 1. 获取 Gmail 应用专用密码

**⚠️ 重要：Gmail 必须开启两步验证才能生成应用密码**

1. 登录 [Google 账户](https://myaccount.google.com)
2. 进入 **安全** 页面
3. 确保已开启 **两步验证**（如果没有，请先开启）
4. 在两步验证下方，点击 **应用专用密码**
5. 选择：
   - **应用**：邮件
   - **设备**：Windows 计算机
6. 点击 **生成**
7. 复制生成的 **16位应用密码**（如：`your-gmail-app-password`）

#### 2. 配置 application.yml

```yaml
spring:
  mail:
    host: smtp.gmail.com
    port: 587
    username: your-email@gmail.com     # 你的Gmail地址
    password: your-gmail-app-password      # 刚才获取的应用密码（带空格也行）
    properties:
      mail:
        smtp:
          auth: true
          starttls:
            enable: true
          ssl:
            trust: smtp.gmail.com
```

---

### 选项 3: 使用 163 邮箱

#### 1. 获取 163 邮箱授权码

1. 登录 [163 邮箱](https://mail.163.com)
2. 点击 **设置** → **POP3/SMTP/IMAP**
3. 开启 **POP3/SMTP服务** 或 **IMAP/SMTP服务**
4. 点击 **客户端授权密码**
5. 通过手机验证后获取授权码

#### 2. 配置 application.yml

```yaml
spring:
  mail:
    host: smtp.163.com
    port: 465
    username: your-email@163.com
    password: your-authorization-code
    properties:
      mail:
        smtp:
          auth: true
          ssl:
            enable: true
          ssl:
            trust: smtp.163.com
```

---

## 📨 配置管理员接收邮箱

在 `application.yml` 中设置接收注册申请通知的管理员邮箱：

```yaml
app:
  admin-email: admin@qq.com    # 可以是任意邮箱：QQ、Gmail、163等都行
```

---

## ✅ 验证配置是否正确

启动项目后，观察日志，如果看到类似以下信息说明配置成功：

```
Mail server connected: smtp.qq.com:587
```

如果有错误，请检查：
- 授权码是否正确（注意不是登录密码）
- 邮箱的 SMTP 服务是否已开启
- 防火墙是否阻止了 SMTP 端口

---

## 🔄 切换发件邮箱

如果你想从 QQ 邮箱切换到 Gmail，只需修改 `application.yml` 中的以下配置：

```yaml
spring:
  mail:
    host: smtp.gmail.com         # 改为 Gmail
    username: your@gmail.com     # 改为 Gmail 地址
    password: your-gmail-app-password  # 改为 Gmail 应用密码
```

重启项目即可！

---

## ❓ 常见问题

### Q: 为什么不用邮箱登录密码，而要用授权码？
**A:** 为了安全起见，QQ、Gmail 等邮箱都不允许使用登录密码通过第三方客户端发送邮件，必须使用授权码或应用专用密码。

### Q: 学生注册时用什么邮箱有限制吗？
**A:** 没有限制！学生可以使用任意邮箱注册：QQ、Gmail、163、Outlook、企业邮箱等都可以正常接收邮件。

### Q: 发送失败怎么办？
**A:** 请检查：
1. 授权码是否正确且未过期
2. 邮箱的 SMTP 服务是否已开启
3. 网络是否能访问邮件服务器（国内访问 Gmail 可能需要特殊网络环境）

### Q: Gmail 收不到邮件怎么办？
**A:** 如果你的发件邮箱在国内，而收件邮箱是 Gmail，可能需要一些时间才能送达。建议管理员使用 QQ 或 163 邮箱接收通知。
