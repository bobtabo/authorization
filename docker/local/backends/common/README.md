<p align="center">
    <span style="margin-right:50px;"><img src="https://cdn.worldvectorlogo.com/logos/nginx.svg" width=120px /></span>
    <span style="margin-right:50px;"><img src="https://cdn.cdnlogo.com/logos/m/10/mysql.svg" width=100px /></span>
    <span><img src="https://www.vectorlogo.zone/logos/redis/redis-ar21.svg" width=100px /></span>
</p>

<p align="center">
    <span style="margin-right:10px;"><img src="https://img.shields.io/badge/nginx-v1.x-008000.svg?style=flat"></span>
    <span style="margin-right:10px;"><img src="https://img.shields.io/badge/mysql-v8.x-1e90ff.svg?style=flat"></span>
    <span><img src="https://img.shields.io/badge/redis-stable-ff7964.svg?style=flat"></span>
</p>


## :bulb: 起動コンテナ
* Nginx Proxy 最新版
* MySQL 8.x
* Redis 最新版
* MailPit 最新版
* AWS lambda/go:1.2024.10.04.19

## :bulb: 使い方

#### 事前準備
```
$ cd docker
$ find ./bin -type f -exec chmod 755 {} +
$ bin/docker-environment.sh
```

#### コンテナ起動
```
$ cd docker
$ bin/docker-common-up.sh
```

#### コンテナ破棄
```
$ cd docker
$ bin/docker-common-down.sh
```

## :bulb: 各ツール

| ツール     | URL |
|---------| ---- |
| MailPit | http://localhost:8025/ |
