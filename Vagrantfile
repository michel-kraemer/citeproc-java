Vagrant.configure("2") do |config|
  config.vm.box = "raring-server-cloudimg-amd64"
  config.vm.box_url = "http://cloud-images.ubuntu.com/vagrant/raring/current/raring-server-cloudimg-amd64-vagrant-disk1.box"
  config.vm.network "forwarded_port", guest: 4000, host: 4000
  config.vm.provision :shell, :path => "_vagrant/install.sh"
  config.vm.provision :shell, :path => "_vagrant/install-gems.sh"
  config.cache.auto_detect = true
end
