#!/usr/bin/env bash

if [ ! -f /etc/apt/sources.list.d/brightbox-ruby-ng-experimental-raring.list ]
then
    add-apt-repository ppa:brightbox/ruby-ng-experimental
fi

aptitude update
aptitude -y install curl ruby2.0 ruby2.0-dev

echo "export LC_ALL=en_US.UTF-8" > /etc/profile.d/default_encoding.sh
chmod +x /etc/profile.d/default_encoding.sh

update-alternatives --set ruby /usr/bin/ruby2.0
update-alternatives --set gem /usr/bin/gem2.0

gem install bundler
