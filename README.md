# Back-end Template

# Prerequisites

1. [Install Nix](https://nixos.org/download/)
2. [Install direnv](https://direnv.net/docs/installation.html)
3. Run `./nix_setup.sh`
   ``` shell
   ./nix_setup.sh
   ```

4. Start the development environment
  ``` shell
  nix develop
  ```

# Setup a development environment

We'll use the [deps-new](https://github.com/seancorfield/deps-new) tool to
create a project.

Simply run the following while in the development environment:

``` shell
bb setup
```
