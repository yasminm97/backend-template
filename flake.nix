{
  description = "A Nix-flake-based Clojure development environment";

  inputs.nixpkgs.url = "github:nixos/nixpkgs/nixos-24.05";

  outputs = { self, nixpkgs }:

    let
      javaVersion = 21;
      overlays = [
        (final: prev: rec {
          jdk = prev."jdk${toString javaVersion}";
          clojure = prev.clojure.override { inherit jdk; };
        })
      ];
      supportedSystems = [
        "x86_64-linux"
        "aarch64-linux"
        "x86_64-darwin"
        "aarch64-darwin"
      ];
      forEachSupportedSystem = f: nixpkgs.lib.genAttrs supportedSystems (system: f {
        pkgs = import nixpkgs { inherit overlays system; };
      });
    in
      {
        devShells = forEachSupportedSystem ({ pkgs }: {
          default = pkgs.mkShell {
            packages = with pkgs; [
              jdk
              clojure-lsp
              clojure
              babashka
              nodejs_22

              R
              rPackages.nycflights13
              rPackages.dplyr
              rPackages.reticulate

              python311Full
              python311Packages.pandas
            ];
            LOCALE_ARCHIVE = "${pkgs.glibcLocales}/lib/locale/locale-archive";
            shellHook = ''
            printf "Welcome to the iData Backend Template!\n\n"

            printf "Running version test...\n"
            bb test
            printf "\n"

            printf "If you don't have a project setup; run 'bb setup' to complete the setup or bootstrap your own project.\nOtherwise, enjoy developing!\n"
            '';
          };
        });
      };
  nixConfig.bash-prompt = "(nix) \\w \\$ ";
}
