#!/bin/sh

mkdir -p ~/.config/nix/

touch ~/.config/nix/nix.conf

echo "experimental-features = nix-command flakes" > ~/.config/nix/nix.conf

nix develop
