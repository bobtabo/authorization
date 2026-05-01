FROM ruby:3.4

# ライブラリのインストール
RUN apt-get update && apt-get install -y \
      sudo \
      git \
      vim \
      locales \
      iputils-ping \
      net-tools

# ユーザーを作成
ARG DOCKER_UID=1000
ARG DOCKER_USER=docker
ARG DOCKER_PASSWORD=docker
RUN useradd -m --uid ${DOCKER_UID} --groups sudo ${DOCKER_USER} \
      && echo ${DOCKER_USER}:${DOCKER_PASSWORD} | chpasswd \
      && echo "docker    ALL=(ALL)       ALL" >> /etc/sudoers

# ロケール
RUN localedef -f UTF-8 -i ja_JP ja_JP.UTF-8
ENV LANG="ja_JP.UTF-8" \
    LANGUAGE="ja_JP:ja" \
    LC_ALL="ja_JP.UTF-8" \
    TZ="Asia/Tokyo"

# bundler を最新化
RUN gem update --system && gem install bundler

# bundle パスと作業ディレクトリを docker ユーザーに移譲
RUN chown -R ${DOCKER_USER}:${DOCKER_USER} /usr/local/bundle \
    && mkdir -p /var/www \
    && chown ${DOCKER_USER}:${DOCKER_USER} /var/www

USER ${DOCKER_USER}
WORKDIR /var/www
