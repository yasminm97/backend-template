# Back-end Template

## Prerequisites

1. [Install Nix](https://nixos.org/download/)
2. [Install direnv](https://direnv.net/docs/installation.html)

## Nix Setup

Now we need to setup Nix for development, simply run:
``` shell
./nix_setup.sh
```

## Clojure Project Setup

We'll use the [deps-new](https://github.com/seancorfield/deps-new) tool to
create a project.

Simply run the following while in the development environment:

``` shell
bb setup
```

## Usage

To subsequently enter the development environment only run

``` shell
nix develop
```

Use the command `exit` to leave the development environment.
