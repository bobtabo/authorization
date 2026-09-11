FROM gradle:8.14-jdk21-corretto

# ライブラリのインストール
RUN dnf install -y \
      sudo \
      git \
      vim-enhanced \
      glibc-langpack-ja \
      iputils \
      net-tools \
      && dnf clean all

# gradle:8.14-jdk21-corretto には UID 1000 の gradle ユーザーが存在するため、それを改名して使う
ARG DOCKER_USER=docker
ARG DOCKER_PASSWORD=docker
RUN usermod -l ${DOCKER_USER} gradle \
      && groupmod -n ${DOCKER_USER} gradle \
      && usermod -d /home/${DOCKER_USER} -m ${DOCKER_USER} \
      && usermod -aG wheel ${DOCKER_USER} \
      && echo ${DOCKER_USER}:${DOCKER_PASSWORD} | chpasswd \
      && echo "docker    ALL=(ALL)       ALL" >> /etc/sudoers

# ロケール・タイムゾーン
ENV LANG="ja_JP.UTF-8" \
    LANGUAGE="ja_JP:ja" \
    LC_ALL="ja_JP.UTF-8" \
    TZ="Asia/Tokyo"

WORKDIR /var/www

# bindマウント配下にネストした名前付きボリューム（build/.gradle）はroot:root 0755で
# 新規作成されるため、root として起動しエントリーポイントで所有者を付け替えてから
# docker ユーザーに降格する（gosuはdnfリポジトリに無いため setpriv を使う）
USER root
COPY docker-entrypoint.sh /usr/local/bin/docker-entrypoint.sh
RUN chmod +x /usr/local/bin/docker-entrypoint.sh
ENTRYPOINT ["docker-entrypoint.sh"]
