#!/usr/bin/env bash

echo "set grub-pc/install_devices /dev/sda" | debconf-communicate
aptitude update
aptitude -y safe-upgrade

aptitude -y purge ruby2.0 ruby1.9.1 ruby1.9.3

echo "export LC_ALL=en_US.UTF-8" > /etc/profile.d/default_encoding.sh
chmod +x /etc/profile.d/default_encoding.sh

su -c 'bash /vagrant/_vagrant/install-rvm.sh' vagrant
su -c 'bash /vagrant/_vagrant/install-ruby.sh' vagrant
su -c 'bash /vagrant/_vagrant/install-gems.sh' vagrant
