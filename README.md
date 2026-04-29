# 北冥审核系统

北冥审核系统用于 Minecraft Java 生电服务器入服审核，基于 Spring Boot 3、MyBatis、MySQL、Vue 3、Vite、Element Plus。

## 功能

- 所有者、管理员、阅卷人、玩家角色
- 建筑审核、后期审核、红石审核、普通(见习)审核分支
- 审核题库与题目提交流程
- 试卷管理、入服审核发布与权限管理
- 玩家在线审核、审核模拟、错题复盘
- 阅卷、参考表决、主考官最终判定
- 审核结果、公示、榜单与成就彩蛋
- 公告、文件上传、邮件通知
- 审核录屏上传到 Cloudreve

## 技术栈

### 后端

- Java 21
- Spring Boot 3.3.1
- MyBatis
- MySQL 8
- Maven

### 前端

- Vue 3
- Vite 5
- Vue Router
- Element Plus
- Axios

## 目录结构

```text
Beiming-OnlineExam/
├── springboot/        # 后端服务
├── vue/               # 前端项目
├── system.sql         # 数据库初始化脚本
└── files/             # 本地上传文件目录（不建议提交）
```

## 本地运行

### 1. 初始化数据库

创建 MySQL 数据库：

```sql
CREATE DATABASE `Beiming-OnlineExam` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

导入 `system.sql`。

### 2. 启动后端

进入后端目录：

```bash
cd springboot
```

设置环境变量后启动：

```bash
export DB_USERNAME=root
export DB_PASSWORD=your-password
export DB_URL='jdbc:mysql://localhost:3306/Beiming-OnlineExam?useUnicode=true&characterEncoding=utf-8&allowMultiQueries=true&useSSL=false&serverTimezone=GMT%2b8&allowPublicKeyRetrieval=true'
mvn spring-boot:run
```

后端默认运行在 `http://localhost:9090`。

可选环境变量：

| 变量 | 默认值 | 说明 |
| --- | --- | --- |
| `SERVER_PORT` | `9090` | 后端端口 |
| `DB_USERNAME` | `root` | 数据库用户名 |
| `DB_PASSWORD` | 空 | 数据库密码 |
| `DB_URL` | 本地 `Beiming-OnlineExam` | 数据库连接地址 |
| `MAIL_HOST` | `smtp.qq.com` | SMTP 地址 |
| `MAIL_PORT` | `587` | SMTP 端口 |
| `MAIL_USERNAME` | 空 | 发件邮箱 |
| `MAIL_PASSWORD` | 空 | 邮箱授权码 |
| `FILE_BASE_URL` | `http://localhost:9090` | 文件访问根地址 |
| `APP_BASE_URL` | `http://localhost:9090` | 应用后端地址 |
| `APP_UPLOAD_DIR` | `${user.dir}/beiming-files` | 北冥版本地上传文件目录 |
| `ADMIN_EMAIL` | `admin@example.com` | 管理员通知邮箱 |
| `CLOUDREVE_BASE_URL` | 空 | Cloudreve 地址 |
| `CLOUDREVE_USERNAME` | 空 | Cloudreve 用户名 |
| `CLOUDREVE_PASSWORD` | 空 | Cloudreve 密码 |
| `CLOUDREVE_UPLOAD_DIR` | `/审核录屏` | 录屏上传目录 |

### 3. 启动前端

进入前端目录：

```bash
cd vue
npm install
npm run dev
```

前端默认请求 `VITE_BASE_URL=http://localhost:9090`。

## 默认演示账号

`system.sql` 包含演示账号：

| 角色 | 用户名 | 密码 |
| --- | --- | --- |
| 管理员 | `admin` | `admin` |
| 阅卷人 | `examiner` | `examiner` |
| 玩家 | `student` | `student` |

生产环境请删除演示账号或修改密码。

## 开源前注意

- 不要提交 `node_modules/`、`dist/`、`target/`、`.idea/`、`.settings/`、本地上传文件。
- 不要提交真实数据库、邮箱、Cloudreve 等账号密码。
- `springboot/src/main/resources/sql/init_members.sql` 只保留示例成员数据；生产成员请使用私有导入脚本。
- 如果真实密码曾经提交到 Git 历史，需要轮换密码并清理 Git 历史。

## 发布前清理

发布到 GitHub/Gitee 前，建议确认以下目录不在提交内容中：

```bash
git status --ignored
```

应被忽略的本地目录包括：

- `vue/node_modules/`
- `vue/dist/`
- `springboot/target/`
- `.idea/`
