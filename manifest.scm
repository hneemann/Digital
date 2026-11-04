;; SPDX-License-Identifier: AGPL-3.0-only
;; SPDX-FileCopyrightText: Copyright 2026 Jonas Meeuws
(use-modules (guix channels)
             (guix inferior)
             (guix profiles)
             (guix ui)
             (srfi srfi-11)
             (srfi srfi-26))

(define (pkgs channels specs)
  (let* ((inferior (inferior-for-channels channels))
         (lookup (cut lookup-inferior-packages inferior <> <>)))
    (map (lambda (spec)
           (let-values (((name version output)
                         (package-specification->name+version+output spec)))
             (list (car (lookup name version))
                   output)))
         specs)))

(packages->manifest
 (pkgs (list (channel
               (inherit %default-guix-channel)
               (commit "c7a78fd26a095f1755ac9c289863a6f8a6b9d7fd")))
       (list "bash"
             "coreutils" "findutils" "sed" "grep" "which"
             "openjdk@21:jdk" "maven" "git" ; digital
             "gcc-cross-avr-toolchain"      ; simavr/test-firmware
             "pre-commit"                   ; tools
             "nss-certs")))
