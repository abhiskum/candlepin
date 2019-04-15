#!/bin/sh
#
# Sets a system up for a candlepin development environment (minus a db,
# handled separately), and an initial clone of candlepin.

set -ve

source /root/dockerlib.sh

export JAVA_VERSION=1.8.0
export JAVA_HOME=/usr/lib/jvm/java-$JAVA_VERSION

# Install & configure dev environment
yum install -y epel-release

PACKAGES=(
    gcc
    gettext
    git
    hostname
    java-$JAVA_VERSION-openjdk-devel
    jss
    libxml2-python
    liquibase
    mariadb
    mysql-connector-java
    mariadb-java-client
    openssl
    postgresql
    postgresql-jdbc
    python-pip
    qpid-proton-c
    qpid-proton-c-devel
    rpmlint
    rsyslog
    tig
    tmux
    tomcat
    vim-enhanced
    wget
)

yum install -y ${PACKAGES[@]}

# pg_isready is used to check if the postgres server is up
# it is not included in postgresql versions < 9.3.0.
# therefore we must build it
if ! type pg_isready 2> /dev/null; then
  yum install -y centos-release-scl
  yum install -y yum install rh-postgresql96
fi

# Setup for autoconf:
mkdir /etc/candlepin
echo "# AUTOGENERATED" > /etc/candlepin/candlepin.conf

cat > /root/.bashrc <<BASHRC
if [ -f /etc/bashrc ]; then
  . /etc/bashrc
fi

export HOME=/root
export JAVA_HOME=/usr/lib/jvm/java-$JAVA_VERSION
BASHRC

git clone https://github.com/candlepin/candlepin.git /candlepin
cd /candlepin

# Setup and install rvm, ruby and pals
gpg2 --keyserver hkp://keys.gnupg.net --recv-keys 409B6B1796C275462A1703113804BB82D39DC0E3 7D2BAF1CF37B13E2069D6956105BD0E739499BDB
# turning off verbose mode, rvm is nuts with this
set +v
curl -O https://raw.githubusercontent.com/rvm/rvm/master/binscripts/rvm-installer
curl -O https://raw.githubusercontent.com/rvm/rvm/master/binscripts/rvm-installer.asc
gpg2 --verify rvm-installer.asc && bash rvm-installer stable
source /etc/profile.d/rvm.sh || true

rvm install 2.5.3
rvm use --default 2.5.3
set -v

# Install all ruby deps
gem update --system
gem install bundler
bundle install --without=proton

# Installs all Java deps into the image, big time saver
# We run checkstyle explicitly here so it'll pull down its deps as well
buildr artifacts
buildr checkstyle || true

cd /
rm -rf /candlepin
cleanup_env
