{
  description = "Digital logic simulator";

  inputs = {
    nixpkgs.url = "github:NixOS/nixpkgs/nixos-unstable";
    flake-utils.url = "github:numtide/flake-utils";
  };

  outputs =
    {
      self,
      nixpkgs,
      flake-utils,
    }:
    flake-utils.lib.eachDefaultSystem (
      system:
      let
        pkgs = import nixpkgs { inherit system; };
      in
      {
        devShells.default = pkgs.mkShell {
          buildInputs = with pkgs; [
            jdk8
            maven
            git
          ];
        };

        packages.default = pkgs.maven.buildMavenPackage {
          pname = "digital";
          version = "latest";

          src = ./.;

          mvnHash = "sha256-coRY8pHDuw5Z+FWvVSW8kqK10rN4y+6ZOo9iVkKb5UY=";

          mvnParameters = "-Pno-git-rev -Dgit.commit.id.describe=v1.0-nix -DskipTests";

          nativeBuildInputs = [ pkgs.makeWrapper ];

          installPhase = ''
            runHook preInstall

            mkdir -p $out/share/java $out/bin
            cp target/Digital.jar $out/share/java/Digital.jar

            makeWrapper ${pkgs.jre8}/bin/java $out/bin/digital \
              --add-flags "-jar $out/share/java/Digital.jar"

            runHook postInstall
          '';
        };
      }
    );
}
