# Back-end Template

## Prerequisites

You'll need a number of packages to get started, you can manually install the
individual packages or use Nix.

### Manual

1. [Install Clojure](https://clojure.org/guides/install_clojure)
2. Install Java 21
3. [Install Babashka](https://github.com/babashka/babashka#installation)

### Nix

1. [Install Nix](https://nixos.org/download/)
2. [Install direnv](https://direnv.net/docs/installation.html)

Now we need to setup Nix for development, simply run:
``` shell
./nix_setup.sh
```

To enter the development environment only run

``` shell
nix develop
```

Use the command `exit` to leave the development environment.

## Clojure Project Setup

We'll use the [deps-new](https://github.com/seancorfield/deps-new) tool to
create a project.

Simply run the following while in the development environment:

``` shell
bb setup
```

## Editor

1. Get VS Code.
2. [Install Calva](https://calva.io/getting-started/#install-vs-code-and-calva)
