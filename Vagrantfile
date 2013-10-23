Vagrant.configure("2") do |config|
  config.vm.box = "precise64"
  config.vm.box_url = "http://files.vagrantup.com/precise64.box"
  config.vm.network "forwarded_port", guest: 4000, host: 4000
  config.vm.provision :shell, :path => "_vagrant/install.sh"
  config.vm.provision :shell, :path => "_vagrant/install-gems.sh"
  if Vagrant.has_plugin?("vagrant-cachier")
    config.cache.auto_detect = true
  end
end
