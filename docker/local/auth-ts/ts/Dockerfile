FROM node:22

# ライブラリのインストール
RUN apt-get update && apt-get install -y \
      sudo \
      git \
      vim \
      locales \
      iputils-ping \
      net-tools

# node:22 には UID 1000 の node ユーザーが存在するため、それを改名して使う
ARG DOCKER_USER=docker
ARG DOCKER_PASSWORD=docker
RUN usermod -l ${DOCKER_USER} node \
      && groupmod -n ${DOCKER_USER} node \
      && usermod -d /home/${DOCKER_USER} -m ${DOCKER_USER} \
      && usermod -aG sudo ${DOCKER_USER} \
      && echo ${DOCKER_USER}:${DOCKER_PASSWORD} | chpasswd \
      && echo "docker    ALL=(ALL)       ALL" >> /etc/sudoers

# ロケール
RUN localedef -f UTF-8 -i ja_JP ja_JP.UTF-8
ENV LANG="ja_JP.UTF-8" \
    LANGUAGE="ja_JP:ja" \
    LC_ALL="ja_JP.UTF-8"

WORKDIR /var/www
