FROM mcr.microsoft.com/dotnet/sdk:10.0

# ライブラリのインストール
RUN apt-get update && apt-get install -y \
      sudo \
      git \
      vim \
      locales \
      iputils-ping \
      net-tools

# dotnet/sdk:10.0（Ubuntu ベース）には UID 1000 の ubuntu ユーザーが存在するため、それを改名して使う
ARG DOCKER_USER=docker
ARG DOCKER_PASSWORD=docker
RUN usermod -l ${DOCKER_USER} ubuntu \
      && groupmod -n ${DOCKER_USER} ubuntu \
      && usermod -d /home/${DOCKER_USER} -m ${DOCKER_USER} \
      && usermod -aG sudo ${DOCKER_USER} \
      && echo ${DOCKER_USER}:${DOCKER_PASSWORD} | chpasswd \
      && echo "docker    ALL=(ALL)       ALL" >> /etc/sudoers

# ロケール・タイムゾーン
RUN localedef -f UTF-8 -i ja_JP ja_JP.UTF-8
ENV LANG="ja_JP.UTF-8" \
    LANGUAGE="ja_JP:ja" \
    LC_ALL="ja_JP.UTF-8" \
    TZ="Asia/Tokyo"

# .NET CLI
ENV DOTNET_CLI_TELEMETRY_OPTOUT=1 \
    DOTNET_NOLOGO=1 \
    NUGET_PACKAGES=/home/${DOCKER_USER}/.nuget/packages

# NuGet キャッシュ用 named volume のマウント先。空ボリュームはイメージ側の所有者を引き継ぐため、
# ここで docker ユーザー所有にしておく
RUN mkdir -p /home/${DOCKER_USER}/.nuget && chown ${DOCKER_USER}:${DOCKER_USER} /home/${DOCKER_USER}/.nuget

WORKDIR /var/www

USER ${DOCKER_USER}
